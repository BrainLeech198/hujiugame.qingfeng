#!/usr/bin/env python3
"""氢风 自动打包工具

工作流：输入版本 → 编译 JAR → 编译 APK → 组装启动器 → 安装包 → 输出
依赖：JDK 21+、Android SDK、Inno Setup 6（Windows）、Python + PyInstaller（Windows）

用法：
    python build_package.py                # 正常打包（Windows + Linux）
    python build_package.py --linux-only    # 仅打包 Linux
    python build_package.py --mac           # 打包 Windows + Linux + macOS（从 Windows 交叉编译）
    python build_package.py --config-only   # 仅检测并保存环境配置
"""

import os
import sys
import json
import shutil
import subprocess
import tempfile
import tkinter as tk
from tkinter import filedialog
from pathlib import Path
from typing import Optional
import struct
import io
import tarfile
import gzip

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_DIR = SCRIPT_DIR.parent.parent
SETUP_DIR = PROJECT_DIR / "lwjgl3" / "setup"
CONFIG_FILE = SCRIPT_DIR / "build_config.env"
CONSTRUO_OUTPUT_DIR = PROJECT_DIR / "lwjgl3" / "build" / "construo"

# Construo 目标平台配置（对应 lwjgl3/build.gradle 中的 targets）
CONSTRUO_TARGETS = {
    "linux": "linuxX64",
    "mac": "macX64",
    "macM1": "macM1",
}

# 版本类型映射（与 Java VersionType 保持一致）
VERSION_TYPE_MAP = {0: "beta", 1: "release"}


def _get_type_name (type_int: int) -> str:
    """将整型版本类型转为名称字符串"""
    return VERSION_TYPE_MAP.get(type_int, "beta")


def _is_windows() -> bool:
    return sys.platform.startswith("win")


def _exe(name: str) -> str:
    """返回平台对应的可执行文件名（Windows 加 .exe，其他平台不加）"""
    return f"{name}.exe" if _is_windows() else name


class BuildConfig:
    """持久化构建环境配置"""

    def __init__(self):
        self.jdk_path: Optional[str] = None
        self.iscc_path: Optional[str] = None
        self._loaded = False

    def load(self):
        if not CONFIG_FILE.exists():
            return
        print("[配置] 加载环境配置...")
        for line in CONFIG_FILE.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                continue
            key, _, val = line.partition("=")
            key = key.strip()
            val = val.strip()
            if key == "JDK_PATH":
                self.jdk_path = val
            elif key == "ISCC_PATH":
                self.iscc_path = val
        self._loaded = True
        print("[配置] 已加载")

    def save(self):
        print("[配置] 保存环境配置...")
        lines = [
            "# 氢风打包工具 - 构建环境配置",
            "# 由 build_package.py 自动生成，删除此文件可重新检测",
            f"JDK_PATH={self.jdk_path or ''}",
            f"ISCC_PATH={self.iscc_path or ''}",
        ]
        CONFIG_FILE.write_text("\n".join(lines) + "\n", encoding="utf-8")
        print(f"[配置] 已保存: {CONFIG_FILE}")


class BuildEnvironment:
    """检测并验证构建工具链"""

    def __init__(self, config: BuildConfig):
        self.config = config
        self.jdk_path: Optional[str] = None
        self.iscc_path: Optional[str] = None
        self.has_mingw = False
        self.android_sdk: Optional[str] = None

    def run_cmd(self, cmd: list[str], timeout=30) -> tuple[int, str]:
        """运行命令并返回 (返回码, stdout)"""
        try:
            r = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
            return r.returncode, r.stdout.strip() + r.stderr.strip()
        except (subprocess.TimeoutExpired, FileNotFoundError, OSError) as e:
            return -1, str(e)

    def check_jdk21(self, javac_path: str) -> bool:
        """检查指定 javac 是否为 JDK 21"""
        rc, out = self.run_cmd([javac_path, "-version"])
        return rc == 0 and "21" in out

    def find_jdk(self):
        """按优先级查找 JDK 21"""
        # 1. 配置路径
        if self.config.jdk_path:
            javac = Path(self.config.jdk_path) / "bin" / _exe("javac")
            if javac.exists() and self.check_jdk21(str(javac)):
                self.jdk_path = self.config.jdk_path
                print(f"[通过] JDK 21: {self.jdk_path}")
                return

        print("[检测] 查找 JDK 21...")

        # 2. JAVA_HOME
        jh = os.environ.get("JAVA_HOME")
        if jh:
            javac = Path(jh) / "bin" / _exe("javac")
            self.jdk_path = jh
            print(f"[通过] JDK 21 (来自 JAVA_HOME): {self.jdk_path}")
            self.config.jdk_path = jh
            return

        # 3. PATH 中的 javac
        javac_in_path = shutil.which("javac")
        if javac_in_path:
            jdk_from_path = str(Path(javac_in_path).resolve().parent.parent)
            if self.check_jdk21(javac_in_path):
                self.jdk_path = jdk_from_path
                print(f"[通过] JDK 21 (来自 PATH): {self.jdk_path}")
                self.config.jdk_path = jdk_from_path
                return

        # 4. Program Files 自动查找
        for pf in [os.environ.get("ProgramFiles", "C:\\Program Files"),
                   os.environ.get("ProgramFiles(x86)", "C:\\Program Files (x86)")]:
            if not pf:
                continue
            java_dir = Path(pf) / "Java"
            if not java_dir.exists():
                continue
            for d in sorted(java_dir.glob("jdk-21*"), reverse=True):
                javac = d / "bin" / _exe("javac")
                if javac.exists() and self.check_jdk21(str(javac)):
                    self.jdk_path = str(d)
                    print(f"[通过] JDK 21 (自动查找): {self.jdk_path}")
                    self.config.jdk_path = str(d)
                    return

        # 5. 手动选择
        print("未能自动找到 JDK 21，请手动选择...")
        root = tk.Tk()
        root.withdraw()
        selected = filedialog.askdirectory(
            title="请选择 JDK 21 安装目录",
            initialdir="C:\\Program Files\\Java"
        )
        root.destroy()
        if selected:
            javac = Path(selected) / "bin" / _exe("javac")
            self.jdk_path = selected
            print(f"[通过] JDK 21: {self.jdk_path}")
            self.config.jdk_path = selected
            return

        print("[失败] 未找到 JDK 21。下载: https://adoptium.net/")
        sys.exit(1)

    def find_iscc(self):
        """查找 Inno Setup 编译器"""
        # 1. 配置路径
        if self.config.iscc_path:
            if Path(self.config.iscc_path).exists():
                self.iscc_path = self.config.iscc_path
                print(f"[通过] Inno Setup: {self.iscc_path}")
                return

        print("[检测] 查找 Inno Setup 编译器...")

        # 2. ISCC 环境变量
        iscc_env = os.environ.get("ISCC")
        if iscc_env and Path(iscc_env).exists():
            self.iscc_path = iscc_env
            print(f"[通过] Inno Setup (来自 ISCC 环境变量): {self.iscc_path}")
            self.config.iscc_path = iscc_env
            return

        # 3. Program Files 自动查找
        candidates = [
            f"{pf}\\Inno Setup 6\\ISCC.exe"
            for pf in [
                os.environ.get("ProgramFiles", "C:\\Program Files"),
                os.environ.get("ProgramFiles(x86)", "C:\\Program Files (x86)"),
            ] if pf
        ] + [
            f"{pf}\\Inno Setup 5\\ISCC.exe"
            for pf in [
                os.environ.get("ProgramFiles", "C:\\Program Files"),
                os.environ.get("ProgramFiles(x86)", "C:\\Program Files (x86)"),
            ] if pf
        ]
        for c in candidates:
            if Path(c).exists():
                self.iscc_path = c
                print(f"[通过] Inno Setup (自动查找): {self.iscc_path}")
                self.config.iscc_path = c
                return

        # 4. 手动选择
        print("未能自动找到 Inno Setup...")
        root = tk.Tk()
        root.withdraw()
        selected = filedialog.askopenfilename(
            title="请选择 ISCC.exe",
            initialdir=os.environ.get("ProgramFiles", "C:\\Program Files"),
            filetypes=[("ISCC.exe", "ISCC.exe")]
        )
        root.destroy()
        if selected and Path(selected).exists():
            self.iscc_path = selected
            print(f"[通过] Inno Setup: {self.iscc_path}")
            self.config.iscc_path = selected
            return

        print("[失败] 未找到 Inno Setup。下载: https://jrsoftware.org/isdl.php")
        sys.exit(1)

    def check_mingw(self):
        """检查或自动安装 MinGW-w64 编译器"""
        def _find_existing() -> str | None:
            # 1. x86_64-w64-mingw32-gcc（cross-compiler 命名）
            found = shutil.which("x86_64-w64-mingw32-gcc")
            if found: return found
            # 2. w64devkit（推荐的轻量 MinGW，约 50MB，完整工具链）
            w64devkit = Path("C:/tools/w64devkit/w64devkit/bin/gcc.exe")
            if w64devkit.exists():
                return str(w64devkit)
            # 3. CLion 捆绑的 MinGW（注意：部分版本工具链不完整）
            for pf in [os.environ.get("ProgramFiles", "C:\\Program Files")]:
                clion_dir = Path(pf) / "JetBrains"
                if clion_dir.exists():
                    for d in sorted(clion_dir.glob("CLion*/bin/mingw/bin"), reverse=True):
                        candidate = d / "gcc.exe"
                        if candidate.exists():
                            return str(candidate)
            # 4. PATH 中的 gcc
            found = shutil.which("gcc")
            if found: return found
            return None

        self.mingw_gcc = _find_existing()
        if self.mingw_gcc:
            self.has_mingw = True
            # w64devkit/bin 目录需在 PATH 中，gcc 才能找到 as/ld
            self.mingw_bin = str(Path(self.mingw_gcc).parent)
            print(f"[通过] MinGW-w64: {self.mingw_gcc}")
            return

        print("[检测] 未找到 MinGW-w64，尝试自动安装...")
        if shutil.which("winget"):
            print("  运行: winget install BrechtSanders.WinLibs.POSIX.MSVCRT")
            r = subprocess.run(
                ["winget", "install", "BrechtSanders.WinLibs.POSIX.MSVCRT",
                 "--accept-package-agreements", "--accept-source-agreements"],
                capture_output=True, text=True)
            if r.returncode == 0:
                # winget 安装后可能需要刷新 PATH
                self.mingw_gcc = _find_existing()
                if self.mingw_gcc:
                    self.has_mingw = True
                    print(f"[通过] MinGW-w64 已自动安装: {self.mingw_gcc}")
                    return
            print(f"  自动安装失败: {r.stderr[-200:] if r.stderr else 'unknown'}")
        else:
            print("  winget 不可用，请手动安装:")
            print("  https://github.com/brechtsanders/winlibs_mingw/releases")

        print("[跳过] MinGW-w64 未安装，将使用已有 launcher.exe")

    def find_android_sdk(self):
        """从 local.properties 读取 Android SDK 路径"""
        lp = PROJECT_DIR / "local.properties"
        if lp.exists():
            for line in lp.read_text(encoding="utf-8").splitlines():
                if line.startswith("sdk.dir") or line.startswith("sdk.dir"):
                    _, _, val = line.partition("=")
                    self.android_sdk = val.strip()
                    return
        print("[提示] 未找到 Android SDK 路径配置（local.properties）")


class Builder:
    """构建执行器"""

    def __init__(self, env: BuildEnvironment, linux_only: bool = False, extra_targets: list[str] = None):
        self.env = env
        self.version = ""
        self.release_type = ""
        self.app_version_int = 0
        self.version_tag = ""
        self.filename_base = "qing-feng_setup"
        self.linux_only = linux_only
        self.extra_targets = extra_targets or []

    def _decode(self, data: bytes) -> str:
        """解码子进程输出，优先 UTF-8，回退 GBK（中文 Windows）"""
        try:
            return data.decode("utf-8")
        except UnicodeDecodeError:
            try:
                return data.decode("gbk")
            except UnicodeDecodeError:
                return data.decode("utf-8", errors="replace")

    def run_gradle(self, task: str, print_output: bool = True) -> bool:
        """运行 Gradle task（实时输出）"""
        if _is_windows():
            gradlew = str(PROJECT_DIR / "gradlew.bat")
            cmd = f'"{gradlew}" {task}'
            r = subprocess.run(cmd, cwd=str(PROJECT_DIR),
                               env=self._gradle_env(), capture_output=True, shell=True)
        else:
            gradlew = str(PROJECT_DIR / "gradlew")
            cmd = [gradlew, task]
            r = subprocess.run(cmd, cwd=str(PROJECT_DIR),
                               env=self._gradle_env(), capture_output=True)
        if print_output and r.stdout:
            print(self._decode(r.stdout))
        if r.returncode != 0:
            if r.stderr:
                lines = self._decode(r.stderr).strip().splitlines()
                print(f"  [stderr] {'; '.join(lines[-10:])}")
            print(f"  [Gradle 退出码: {r.returncode}]")
        return r.returncode == 0

    def _gradle_env(self) -> dict:
        env = os.environ.copy()
        env["JAVA_HOME"] = self.env.jdk_path
        return env

    def step_update_version(self):
        """1/7: 更新版本号"""
        print("[1/7] 更新版本号...")

        # 展示版本变更确认
        app_ver = PROJECT_DIR / "assets" / "asset" / "app_version.json"
        old_data = {}
        if app_ver.exists():
            old_data = json.loads(app_ver.read_text(encoding="utf-8"))
        old_type = old_data.get("appVersionType", -1)
        old_type_name = _get_type_name(old_type) if old_type in (0, 1) else "(无)"
        print(f"  版本整型编码: {old_data.get('appVersion', '(无)')} → {self.app_version_int}")
        print(f"  版本字符串:   {old_data.get('appVersionString', '(无)')} → {self.version}")
        print(f"  发行类型:     {old_type_name} → {self.release_type}")
        confirm = input("确认以上信息无误？(Y/n): ").strip().lower()
        if confirm == "n":
            print("取消打包")
            sys.exit(1)

        gp = PROJECT_DIR / "gradle.properties"
        content = gp.read_text(encoding="utf-8")
        import re
        content = re.sub(r'^projectVersion=.*', f'projectVersion={self.version}',
                         content, flags=re.MULTILINE)
        gp.write_text(content, encoding="utf-8")
        print(f"  [信息] gradle.properties projectVersion={self.version}")

        # 同步更新 app_version.json（运行时版本显示 + 更新检测用）
        if app_ver.exists():
            ver_data = json.loads(app_ver.read_text(encoding="utf-8"))
            ver_data["appVersion"] = self.app_version_int
            ver_data["appVersionString"] = self.version
            # appVersionType: beta=0, release=1
            ver_data["appVersionType"] = 0 if self.release_type == "beta" else 1
            app_ver.write_text(
                json.dumps(ver_data, ensure_ascii=False, indent=2),
                encoding="utf-8"
            )
            print(f"  [信息] app_version.json 已同步: v{self.version} ({self.release_type})")
        else:
            print(f"  [警告] 未找到 app_version.json: {app_ver}")

        # 同步 android/build.gradle（APK 系统版本号）
        android_build = PROJECT_DIR / "android" / "build.gradle"
        if android_build.exists():
            content = android_build.read_text(encoding="utf-8")
            content = re.sub(r'versionCode \d+', f'versionCode {self.app_version_int}', content)
            content = re.sub(r'versionName ".*"', f'versionName "{self.version}"', content)
            android_build.write_text(content, encoding="utf-8")
            print(f"  [信息] android/build.gradle 已同步: versionCode={self.app_version_int}, versionName={self.version}")

        # 同步 inno_setup.iss（安装包版本显示用）
        iss_path = SETUP_DIR / "inno_setup.iss"
        iss_bak = SETUP_DIR / "inno_setup.iss.bak"
        if iss_path.exists():
            # 首次运行时备份原始文件，用于构建结束后恢复
            if not iss_bak.exists():
                shutil.copy2(iss_path, iss_bak)
            content = iss_path.read_text(encoding="utf-8")
            content = content.replace(
                '#define MyAppVersion "1.0.0"',
                f'#define MyAppVersion "{self.version}-{self.release_type}"'
            )
            import re
            content = re.sub(r'qingfeng-.*\.jar', f'qingfeng-{self.version}.jar', content)

            # 同步 MyAppURL 为 WebSite.OFFICIAL
            web_site_path = PROJECT_DIR / "core" / "src" / "main" / "java" / "com" / "hujiugame" / "qingfeng" / "type" / "url" / "WebSite.java"
            if web_site_path.exists():
                ws_content = web_site_path.read_text(encoding="utf-8")
                m = re.search(r'OFFICIAL\s*=\s*"([^"]+)"', ws_content)
                if m:
                    official_url = m.group(1)
                    old_url_match = re.search(r'#define MyAppURL "([^"]*)"', content)
                    if old_url_match and old_url_match.group(1) != official_url:
                        content = content.replace(
                            f'#define MyAppURL "{old_url_match.group(1)}"',
                            f'#define MyAppURL "{official_url}"'
                        )
                        print(f"  [信息] MyAppURL 已同步: {official_url}")
                    else:
                        print(f"  [信息] MyAppURL 无需更新: {official_url}")
                else:
                    print("  [警告] 未找到 WebSite.OFFICIAL 常量")

            iss_path.write_text(content, encoding="utf-8")
            print(f"  [信息] inno_setup.iss 已同步: v{self.version} ({self.release_type})")
        print()

    def step_build_jar(self) -> bool:
        """2/7: 编译桌面 JAR"""
        print("[2/7] 编译桌面 JAR...")
        ok = self.run_gradle("lwjgl3:jar")
        if not ok:
            print("[错误] JAR 打包失败")
        else:
            self.jar_path = PROJECT_DIR / "lwjgl3" / "build" / "libs" / f"qingfeng-{self.version}.jar"
        print()
        return ok

    def _set_use_viewport(self, value: str):
        """修改 user_config.json 中的 useViewport（Android=fit，桌面端=stretch）"""
        config_path = PROJECT_DIR / "assets" / "asset" / "user_config.json"
        config = json.loads(config_path.read_text(encoding="utf-8"))
        config["useViewport"] = value
        config_path.write_text(
            json.dumps(config, ensure_ascii=False, indent=2),
            encoding="utf-8"
        )
        print(f"  [信息] useViewport 已切换为: {value}")

    def step_build_apk(self) -> bool:
        """3/7: 编译 Android APK"""
        print("[3/7] 编译 Android APK...")

        # Android 使用 fit 视口模式，编译后恢复 stretch（桌面端）
        self._set_use_viewport("fit")

        try:
            # 读取签名密码（环境变量 or 交互输入）
            store_pass = os.environ.get("STORE_PASSWORD") or input("请输入 Android storePassword: ").strip()
            key_pass = os.environ.get("KEY_PASSWORD") or input("请输入 Android keyPassword: ").strip()
            password_flags = f"-PstorePassword={store_pass} -PkeyPassword={key_pass}"

            ok = self.run_gradle(f"android:assembleRelease {password_flags}")
        finally:
            # 确保无论打包成败，user_config.json 都恢复为桌面端视口模式
            self._set_use_viewport("stretch")

        if not ok:
            print("[错误] Android 打包失败")
            return False

        # 查找 APK
        apk_dir = PROJECT_DIR / "android" / "build" / "outputs" / "apk"
        apks = list(apk_dir.rglob("*.apk"))
        if not apks:
            print("[错误] 未找到编译完成的 APK 文件")
            return False
        self.apk_file = str(apks[0])
        print(f"[信息] APK: {self.apk_file}")
        print()
        return True

    def step_build_construo(self, target_name: str, platform_label: str) -> bool:
        """4/7: 通过 construo 插件构建跨平台包"""
        print(f"[4/7] 构建 {platform_label} 包（construo）...")
        # 首字母大写，对应 Gradle 任务名（如 register("linuxX64") → packageLinuxX64）
        target_cap = target_name[0].upper() + target_name[1:]
        task = f"lwjgl3:package{target_cap}"
        ok = self.run_gradle(task, print_output=True)
        if not ok:
            print(f"[错误] {platform_label} 包构建失败")
            return False

        # 查找 construo 输出
        pkg_dir = CONSTRUO_OUTPUT_DIR / target_name
        artifacts = list(pkg_dir.glob("*")) if pkg_dir.exists() else []
        if not artifacts:
            # 尝试 construo 默认输出路径
            artifacts = list(CONSTRUO_OUTPUT_DIR.glob("*"))
        if artifacts:
            self.construo_artifacts = getattr(self, "construo_artifacts", [])
            for a in artifacts:
                self.construo_artifacts.append((a, platform_label))
                print(f"  [信息] {platform_label} 产物: {a.name}")

        # 非 Windows 平台：移除 JAR 中的 Windows DLL
        if platform_label != "windows":
            roast_jar = pkg_dir / "roast" / f"qingfeng-{self.version}.jar"
            if roast_jar.exists():
                print(f"  [信息] 瘦身 JAR：移除 Windows 原生库...")
                exclude_patterns = (".dll", "windows/", "windows32/", "windows64/")
                import zipfile, tempfile, shutil
                temp_jar = tempfile.NamedTemporaryFile(delete=False, suffix=".jar")
                temp_jar.close()
                removed = 0
                with zipfile.ZipFile(roast_jar, "r") as src:
                    entries = [e for e in src.infolist() if not any(p in e.filename for p in exclude_patterns)]
                    removed = len(src.infolist()) - len(entries)
                    with zipfile.ZipFile(temp_jar.name, "w", zipfile.ZIP_DEFLATED) as dst:
                        for e in entries:
                            dst.writestr(e, src.read(e.filename))
                shutil.move(temp_jar.name, roast_jar)
                print(f"  [信息] 移除了 {removed} 个 Windows 文件，JAR 已瘦身")
        print()
        return True

    def step_assemble_launcher(self) -> bool:
        """5/7: 组装 Windows 启动器"""
        print("[5/7] 组装 Windows 启动器...")

        launcher_dir = SETUP_DIR / "dist" / "launcher"

        # 4a. 编译 launcher.exe（原生 C，仅依赖 Win7 原生 API）
        launcher_c = SETUP_DIR / "launcher.c"
        launcher_rc = SETUP_DIR / "launcher.rc"
        if self.env.has_mingw and launcher_c.exists():
            gcc = self.env.mingw_gcc
            gcc_dir = Path(self.env.mingw_bin) if self.env.mingw_bin else None
            # gcc 需要 bin 目录在 PATH 中才能找到 as/ld 等子工具
            gcc_env = os.environ.copy()
            if gcc_dir:
                gcc_env["PATH"] = str(gcc_dir) + os.pathsep + gcc_env["PATH"]
            print(f"[信息] 使用 MinGW-w64 编译 launcher.exe ...")

            # 编译 .rc 资源（嵌入 console.ico）
            if launcher_rc.exists() and gcc_dir:
                windres = str(gcc_dir / "windres.exe")
                res_o = launcher_dir / "launcher_res.o"
                r = subprocess.run([windres, "-O", "coff", str(launcher_rc), str(res_o)],
                                   capture_output=True, text=True, env=gcc_env)
                if r.returncode != 0:
                    print(f"[错误] 图标资源编译失败: {r.stderr}")
                    return False
                rc_o = str(res_o)
                print("  [信息] 图标资源已编译")
            else:
                rc_o = None

            out_exe = str(launcher_dir / "launcher.exe")
            cmd = [gcc, "-O2", "-s", "-static", "-mwindows",
                   "-D_WIN32_WINNT=0x0601", "-D_WIN32_IE=0x0601",
                   "-o", out_exe, str(launcher_c)]
            if rc_o:
                cmd.append(rc_o)
            cmd.append("-lshlwapi")
            r = subprocess.run(cmd, capture_output=True, text=True, env=gcc_env)
            if r.returncode != 0:
                print(f"[错误] launcher.exe 编译失败: {r.stderr}")
                return False
            print("[通过] launcher.exe 编译成功（已嵌入图标）")
        else:
            if not (launcher_dir / "launcher.exe").exists():
                prebuilt = SETUP_DIR / "launcher.exe"
                if prebuilt.exists():
                    shutil.copy2(prebuilt, launcher_dir / "launcher.exe")
                    print(f"[信息] 使用预构建启动器: {prebuilt}")
                elif launcher_c.exists():
                    print("[信息] 未找到 MinGW-w64 和预构建 launcher.exe")
                    print("  安装 w64devkit（推荐，50MB）:")
                    print("    https://github.com/skeeto/w64devkit/releases")
                    print("  或安装完整 MinGW:")
                    print("    winget install BrechtSanders.WinLibs.POSIX.MSVCRT")
                    return False
                else:
                    print("[错误] launcher.c 不存在，无法编译")
                    return False
            else:
                print("[信息] 使用已有启动器: dist/launcher/launcher.exe")

        # 4b. 清理旧构建产物，创建运行时目录
        # 清理 PyInstaller 旧产物（C 启动器不再需要 _internal/）
        old_internal = launcher_dir / "_internal"
        if old_internal.exists():
            shutil.rmtree(old_internal)

        jar_dir = launcher_dir / "lib" / "jar"
        if jar_dir.exists():
            shutil.rmtree(jar_dir)
        jar_dir.mkdir(parents=True)

        # 清理旧 JRE
        for old_jre in launcher_dir.glob("lib/jre-*"):
            shutil.rmtree(old_jre)

        set_json = launcher_dir / "lib" / "set.json"
        set_json.write_text(
            json.dumps({"console": False, "jre": "jre"},
                        ensure_ascii=False, indent=2),
            encoding="utf-8"
        )

        # 4c. 复制 JAR
        jar_src = getattr(self, "jar_path", None)
        if not jar_src or not jar_src.exists():
            print(f"[错误] JAR 文件不存在: {jar_src}")
            return False
        shutil.copy2(jar_src, jar_dir / f"qingfeng-{self.version}.jar")
        print(f"[信息] JAR 已复制: {jar_src.name}")

        # 4d. jlink 生成最小 JRE（固定命名 jre，每次覆盖）
        jre_target = launcher_dir / "lib" / "jre"
        if jre_target.exists():
            shutil.rmtree(jre_target)

        jlink = Path(self.env.jdk_path) / "bin" / _exe("jlink")
        jmods = Path(self.env.jdk_path) / "jmods"
        if not jmods.exists():
            print(f"[错误] JDK 缺少 jmods 目录: {jmods}")
            return False

        print(f"[信息] 使用 jlink 生成最小 JRE 到 {jre_target} ...")
        r = subprocess.run([
            str(jlink),
            "--module-path", str(jmods),
            "--add-modules", "java.base,java.desktop,jdk.unsupported",
            "--output", str(jre_target),
            "--strip-debug",
            "--compress", "zip-9",
            "--no-header-files",
            "--no-man-pages",
        ])
        if r.returncode != 0:
            print("[错误] jlink 生成 JRE 失败")
            return False

        # 4e. 下载 api-ms-win-core-path-l1-1-0.dll（Win7 兼容性补丁）
        # 注意：Win7 上 launcher.exe（PyInstaller）和 java.exe 都需要此 DLL，
        # 因此必须同时放在启动器根目录和 jre/bin/ 两处。
        # 使用开源 shim：https://github.com/adang1345/api-ms-win-core-path
        dll_name = "api-ms-win-core-path-l1-1-0.dll"
        dll_targets = [
            launcher_dir / dll_name,            # launcher.exe 启动需要
            jre_target / "bin" / dll_name,      # java.exe 启动需要
        ]
        if not all(t.exists() for t in dll_targets):
            import urllib.request, zipfile
            dll_cache = SCRIPT_DIR / ".dll_cache"
            dll_cache.mkdir(parents=True, exist_ok=True)
            cache_extracted = dll_cache / dll_name

            # 从缓存或网络获取 DLL
            if cache_extracted.exists():
                dll_src = cache_extracted
                print("[信息] 使用缓存的 Win7 兼容补丁...")
            else:
                # 查找缓存 ZIP（支持新旧两种命名，兼容之前入库的旧文件名）
                cache_zip_names = [
                    f"{dll_name}.zip",              # 新命名: api-ms-win-core-path-l1-1-0.dll.zip
                    "api-ms-win-core-path.zip",     # 旧命名: GitHub 原始下载名
                ]
                cache_zip = None
                for name in cache_zip_names:
                    candidate = dll_cache / name
                    if candidate.exists():
                        cache_zip = candidate
                        break

                if not cache_zip or not cache_zip.exists():
                    dll_url = "https://github.com/adang1345/api-ms-win-core-path/releases/download/v1.0.0/api-ms-win-core-path.zip"
                    print("[信息] 下载 Win7 兼容补丁...")
                    try:
                        cache_zip = dll_cache / cache_zip_names[0]
                        urllib.request.urlretrieve(dll_url, cache_zip)
                    except Exception as e:
                        print(f"[警告] 下载失败: {e}")
                        print("       在 Windows 7 上运行时可能报错")
                        cache_zip = None
                if cache_zip and cache_zip.exists():
                    with zipfile.ZipFile(cache_zip, 'r') as zf:
                        zf.extract(f"x64/{dll_name}", dll_cache)
                    (dll_cache / "x64" / dll_name).rename(cache_extracted)
                    (dll_cache / "x64").rmdir()
                dll_src = cache_extracted if cache_extracted.exists() else None

            if dll_src:
                for t in dll_targets:
                    t.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(dll_src, t)
                    os.chmod(t, 0o755)
                print("[通过] Win7 兼容补丁已就绪（launcher + jre/bin）")
        else:
            print("[通过] Win7 兼容补丁已存在")

        # 瘦身 JAR：移除 Linux/macOS 原生库（仅保留 Windows 的 .dll）
        jar_path = jar_dir / f"qingfeng-{self.version}.jar"
        if jar_path.exists():
            import zipfile, tempfile
            print("[信息] 瘦身 JAR：移除 Linux/macOS 原生库...")
            exclude_patterns = (".so", ".dylib", "linux/", "macos/", "mac/")
            temp_jar = tempfile.NamedTemporaryFile(delete=False, suffix=".jar")
            temp_jar.close()
            removed = 0
            with zipfile.ZipFile(jar_path, "r") as src:
                entries = [e for e in src.infolist() if not any(p in e.filename for p in exclude_patterns)]
                removed = len(src.infolist()) - len(entries)
                with zipfile.ZipFile(temp_jar.name, "w", zipfile.ZIP_DEFLATED, 9) as dst:
                    for e in entries:
                        dst.writestr(e, src.read(e.filename))
            shutil.move(temp_jar.name, jar_path)
            print(f"  [信息] 移除了 {removed} 个非 Windows 文件")
        else:
            print(f"  [跳过] JAR 不存在: {jar_path}")

        # 验证
        checks = [
            ("launcher.exe", launcher_dir / "launcher.exe"),
            ("JAR", jar_dir / f"qingfeng-{self.version}.jar"),
            ("set.json", set_json),
            ("JRE java", jre_target / "bin" / _exe("java")),
        ]
        for name, path in checks:
            if not path.exists():
                print(f"[错误] 缺少 {name}")
                return False
        print("[通过] 启动器结构完整")
        print()
        return True

    def step_build_installer(self) -> bool:
        """6/7: 编译 Windows 安装包"""
        print("[6/7] 更新安装包脚本并编译 Windows 安装包...")

        # 更新 ISS 中的 JAR 引用（版本号已在 1/7 中同步）
        iss_path = SETUP_DIR / "inno_setup.iss"
        content = iss_path.read_text(encoding="utf-8")
        import re
        content = re.sub(r'qingfeng-.*\.jar', f'qingfeng-{self.version}.jar', content)
        iss_path.write_text(content, encoding="utf-8")

        # 复制 LICENSE 到安装包目录
        license_src = PROJECT_DIR / "LICENSE"
        license_dst = SETUP_DIR / "LICENSE"
        if license_src.exists():
            shutil.copy2(license_src, license_dst)
            print(f"  [信息] 已添加许可证: {license_dst.name}")

        # 运行 ISCC
        print(f"  > ISCC {iss_path}")
        r = subprocess.run([self.env.iscc_path, str(iss_path)],
                           cwd=str(SETUP_DIR))
        if r.returncode != 0:
            print("[错误] Inno Setup 打包失败")
            return False
        print()
        return True

    def step_copy_outputs(self):
        """7/7: 复制成品到 output 目录"""
        print("[7/7] 复制成品到 output 目录...")

        output_dir = SCRIPT_DIR
        tag = f"v{self.version}-{self.release_type}"

        # APK
        if hasattr(self, "apk_file"):
            apk_dst = output_dir / f"{self.filename_base}_android_{tag}.apk"
            shutil.copy2(self.apk_file, apk_dst)
            print(f"[成功] APK: {apk_dst.name}")

        # Windows 安装包
        setup_exe = SETUP_DIR / "dist" / "qingfeng_setup_windows.exe"
        if setup_exe.exists():
            exe_dst = output_dir / f"{self.filename_base}_windows_{tag}.exe"
            shutil.copy2(setup_exe, exe_dst)
            print(f"[成功] EXE: {exe_dst.name}")
        else:
            print(f"[警告] 未找到安装包: {setup_exe}")

        # Linux / macOS 产物（construo）— Linux 已使用 .sh 一键安装包，跳过 tar.gz
        for artifact_path, platform_label in getattr(self, "construo_artifacts", []):
            if platform_label == "linux":
                continue
            suffix = ".tar.gz" if artifact_path.is_dir() else artifact_path.suffix
            dst = output_dir / f"{self.filename_base}_{platform_label}_{tag}{suffix}"
            if artifact_path.is_dir():
                # 目录打包为 tar.gz
                import tarfile
                with tarfile.open(dst, "w:gz") as tar:
                    tar.add(artifact_path, arcname=artifact_path.name)
                print(f"[成功] {platform_label}: {dst.name}")
            elif artifact_path.is_file():
                shutil.copy2(artifact_path, dst)
                print(f"[成功] {platform_label}: {dst.name}")

        # .deb 安装包 + 自解压 .sh 安装器
        deb_path = getattr(self, "deb_path", None)
        if deb_path:
            if deb_path.exists():
                print(f"[成功] DEB: {deb_path.name}（蓝奏云等平台分发用）")
            sh_path = deb_path.with_suffix(".sh")
            if sh_path.exists():
                print(f"[成功] 一键安装: {sh_path.name}")

        print()

    def step_build_install_sh(self, deb_path: Path) -> bool:
        """生成一键安装包（自解压式，.deb 内嵌在脚本末尾）

        输出单个 .sh 文件，用户双击即可图形化安装。
        Linux 版 Windows .exe 安装包。
        """
        # 命名: qing-feng_setup_linux_v1.0.0-beta.deb → qing-feng_setup_linux_v1.0.0-beta.sh
        platform_tag = deb_path.stem.replace("qing-feng_setup_", "")
        installer_path = deb_path.parent / f"qing-feng_setup_{platform_tag}.sh"

        deb_data = deb_path.read_bytes()

        # 脚本头：校验 → pkexec 提权 → 自解压 → dpkg -i
        header = (
            "#!/bin/bash\n"
            "# 氢风 一键安装包（自解压）\n"
            "\n"
            'SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"\n'
            'INSTALLER="$0"\n'
            "\n"
            '# 如果是被 pkexec 调用，$0 可能是临时文件，从原路径读取\n'
            'if [ ! -f "$INSTALLER" ] || [ "$(head -c 4 "$INSTALLER" 2>/dev/null)" != "#!/b" ]; then\n'
            '    INSTALLER="$SCRIPT_DIR/$(basename "$0")"\n'
            "fi\n"
            "\n"
            "# 非 root → pkexec 提权（图形密码框）\n"
            'if [ "$EUID" -ne 0 ]; then\n'
            '    if command -v pkexec >/dev/null 2>&1; then\n'
            '        pkexec bash "$INSTALLER" --install\n'
            '        EXIT_CODE=$?\n'
            '        if command -v zenity >/dev/null 2>&1; then\n'
            '            if [ $EXIT_CODE -eq 0 ]; then\n'
            '                zenity --info --title="氢风安装" \\\n'
            '                    --text="安装成功！\\n请在应用程序菜单中启动 氢风。" \\\n'
            '                    --width=300\n'
            '            else\n'
            '                zenity --error --title="氢风安装" \\\n'
            '                    --text="安装失败，请尝试在终端中运行:\\nchmod +x $INSTALLER\\nsudo bash $INSTALLER" \\\n'
            '                    --width=300\n'
            '            fi\n'
            '        fi\n'
            '        exit $EXIT_CODE\n'
            '    else\n'
            '        echo "此安装需要 root 权限。请尝试: sudo bash $INSTALLER"\n'
            '        read -rp "按 Enter 键退出..."\n'
            '        exit 1\n'
            '    fi\n'
            "fi\n"
            "\n"
            "# === 以下以 root 执行 ===\n"
            'if [ "$1" != "--install" ]; then\n'
            '    exec bash "$INSTALLER" --install\n'
            "fi\n"
            "\n"
            'echo "正在安装 氢风..."\n'
            "\n"
            "# 自解压：提取脚本末尾的内嵌 .deb\n"
            'ARCHIVE_START=$(grep -an "^#__DEB_ARCHIVE__$" "$INSTALLER" | cut -d: -f1)\n'
            'if [ -z "$ARCHIVE_START" ]; then\n'
            '    echo "错误: 安装包数据损坏" >&2\n'
            '    exit 1\n'
            "fi\n"
            "\n"
            'DEB_TMP=$(mktemp --tmpdir qingfeng-install.XXXXXX.deb)\n'
            'trap "rm -f $DEB_TMP" EXIT\n'
            "\n"
            'tail -n +$((ARCHIVE_START + 1)) "$INSTALLER" > "$DEB_TMP"\n'
            "\n"
            'if [ "$(head -c 7 "$DEB_TMP")" != "!<arch>" ]; then\n'
            '    echo "错误: 提取的安装包数据无效" >&2\n'
            '    exit 1\n'
            "fi\n"
            "\n"
            'dpkg -i "$DEB_TMP"\n'
            'EXIT_CODE=$?\n'
            "\n"
            'if [ $EXIT_CODE -ne 0 ] && command -v apt-get >/dev/null 2>&1; then\n'
            '    echo "正在修复依赖关系..."\n'
            '    apt-get install -f -y -qq\n'
            '    EXIT_CODE=$?\n'
            "fi\n"
            "\n"
            'exit $EXIT_CODE\n'
            "#__DEB_ARCHIVE__\n"
        ).encode("utf-8")

        installer_path.write_bytes(header + deb_data)
        installer_path.chmod(0o755)
        deb_kb = len(deb_data) // 1024
        print(f"[成功] 一键安装包: {installer_path.name} ({deb_kb} KB 内嵌 .deb)")
        print(f"[成功] DEB: {deb_path.name}（保留，供蓝奏云等不支持 .sh 的平台分发）")
        return True

    def step_build_deb(self, construo_dir: Path, platform_label: str) -> bool:
        """将 construo 构建产物打包为 .deb（Linux 安装包）"""
        print(f"[额外] 打包 {platform_label} .deb 安装包...")

        roast_dir = construo_dir / "roast"
        if not roast_dir.exists():
            print(f"[错误] 未找到 construo 产物: {roast_dir}")
            return False

        tag = f"v{self.version}-{self.release_type}"
        deb_name = f"{self.filename_base}_{platform_label}_{tag}"
        deb_path = SCRIPT_DIR / f"{deb_name}.deb"

        # 生成 .desktop 文件
        desktop_content = (
            "[Desktop Entry]\n"
            "Type=Application\n"
            f"Name=氢风\n"
            f"Comment=QingFeng Launcher\n"
            f"Exec=/usr/lib/qingfeng/qingfeng %f\n"
            f"Icon=qingfeng\n"
            "Categories=Game;\n"
            "MimeType=application/x-qingfeng-game;\n"
            "Terminal=false\n"
        )

        # 生成 postinst 脚本
        postinst_content = (
            "#!/bin/bash\n"
            "set -e\n"
            # 更新 MIME 数据库
            "if command -v update-mime-database >/dev/null 2>&1; then\n"
            "    update-mime-database /usr/share/mime || true\n"
            "fi\n"
            # 更新桌面数据库
            "if command -v update-desktop-database >/dev/null 2>&1; then\n"
            "    update-desktop-database /usr/share/applications || true\n"
            "fi\n"
            # 更新图标缓存
            "if command -v gtk-update-icon-cache >/dev/null 2>&1; then\n"
            "    gtk-update-icon-cache -f /usr/share/icons/hicolor || true\n"
            "fi\n"
            "exit 0\n"
        )

        # 生成 postrm 脚本
        postrm_content = (
            "#!/bin/bash\n"
            "set -e\n"
            "if command -v update-desktop-database >/dev/null 2>&1; then\n"
            "    update-desktop-database /usr/share/applications || true\n"
            "fi\n"
            "if command -v update-mime-database >/dev/null 2>&1; then\n"
            "    update-mime-database /usr/share/mime || true\n"
            "fi\n"
            "exit 0\n"
        )

        # 构建 control.tar.gz
        control_files = {
            "control": (
                f"Package: qingfeng\n"
                f"Version: {self.version}-{self.release_type}\n"
                f"Section: games\n"
                f"Priority: optional\n"
                f"Architecture: amd64\n"
                f"Maintainer: QingFeng Team\n"
                f"Installed-Size: {self._dir_size(roast_dir) // 1024}\n"
                f"Description: QingFeng Launcher\n"
                f" A visual novel game launcher built with libGDX.\n"
            ),
            "postinst": postinst_content,
            "postrm": postrm_content,
        }
        control_tar = self._make_tar(control_files, mode=0o755)

        # 构建 data.tar.gz 的内容映射
        data_files = {}

        # Launcher 脚本（入口）
        data_files["usr/bin/qingfeng"] = (
            "#!/bin/bash\n"
            'exec /usr/lib/qingfeng/qingfeng "$@"\n'
        )

        # .desktop 文件
        data_files["usr/share/applications/qingfeng.desktop"] = desktop_content

        # 图标
        icon_path = PROJECT_DIR / "lwjgl3" / "icons" / "logo.png"
        if icon_path.exists():
            data_files["usr/share/icons/hicolor/256x256/apps/qingfeng.png"] = icon_path.read_bytes()

        # MIME 类型注册（.qfg 文件关联）
        data_files["usr/share/mime/packages/x-qingfeng-game.xml"] = (
            '<?xml version="1.0" encoding="UTF-8"?>\n'
            '<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">\n'
            '    <mime-type type="application/x-qingfeng-game">\n'
            '        <comment>QingFeng Game Package</comment>\n'
            '        <glob pattern="*.qfg"/>\n'
            '        <icon name="qingfeng"/>\n'
            '    </mime-type>\n'
            '</mime-info>\n'
        )

        # 应用文件（roast 目录全部内容）
        for f in roast_dir.rglob("*"):
            if f.is_file():
                rel = f.relative_to(roast_dir)
                target = f"usr/lib/qingfeng/{rel.as_posix()}"
                data_files[target] = f.read_bytes()

        data_tar = self._make_tar(data_files, mode=0o755)

        # 生成 .deb（ar 归档格式）
        try:
            self._write_ar(deb_path, control_tar, data_tar)
            print(f"[成功] .deb: {deb_path.name}")
            self.deb_path = deb_path
            return True
        except Exception as e:
            print(f"[错误] .deb 生成失败: {e}")
            return False

    def _dir_size(self, path: Path) -> int:
        total = 0
        for f in path.rglob("*"):
            if f.is_file():
                total += f.stat().st_size
        return total

    def _make_tar(self, files: dict, mode: int = 0o644) -> bytes:
        """创建 tar.gz，files 为 {路径: 内容}"""
        buf = io.BytesIO()

        # 收集所有需要创建的目录
        dirs: set[str] = set()
        for name in files:
            parent = Path(name).parent
            while parent and parent.name:
                dirs.add(parent.as_posix())
                parent = parent.parent

        with gzip.GzipFile(fileobj=buf, mode="w", mtime=0) as gz:
            with tarfile.open(fileobj=gz, mode="w|", format=tarfile.USTAR_FORMAT) as tar:
                # 先写目录条目
                for dir_name in sorted(dirs):
                    info = tarfile.TarInfo(name=dir_name + "/")
                    info.type = tarfile.DIRTYPE
                    info.mtime = 0
                    info.mode = 0o755
                    info.uname = "root"
                    info.gname = "root"
                    tar.addfile(info)
                # 再写文件
                for name, content in files.items():
                    info = tarfile.TarInfo(name=name)
                    if isinstance(content, str):
                        content = content.encode("utf-8")
                    info.size = len(content)
                    info.mtime = 0
                    info.mode = mode
                    info.uname = "root"
                    info.gname = "root"
                    tar.addfile(info, io.BytesIO(content))
        return buf.getvalue()

    def _write_ar(self, path: Path, control_tar: bytes, data_tar: bytes):
        """将 control.tar.gz + data.tar.gz 打包为 .deb（ar 格式）"""
        # debian-binary
        debian_binary = b"2.0\n"

        # ar 全局头
        buf = io.BytesIO()
        buf.write(b"!<arch>\n")

        def ar_write(file_name: str, content: bytes):
            """写入一个 ar 文件条目"""
            # 补齐到偶数长度
            if len(content) % 2 == 1:
                content += b"\n"
            # ar header: 文件名必须用空格填充
            name = file_name.ljust(16, " ")[:16].encode("ascii")
            size = f"{len(content):10}".encode("ascii")
            hdr = (
                name +
                b"0           " +   # timestamp (12 spaces)
                b"0     " +          # owner (6 spaces)
                b"0     " +          # group (6 spaces)
                b"100644  " +        # mode (8 spaces)
                size +
                b"\x60\x0a"          # ar magic
            )
            buf.write(hdr)
            buf.write(content)

        ar_write("debian-binary", debian_binary)
        ar_write("control.tar.gz", control_tar)
        ar_write("data.tar.gz", data_tar)

        path.write_bytes(buf.getvalue())

    def restore_backups (self):
        """恢复被修改的配置文件"""
        # gradle.properties — 由 step_update_version 修改，不需恢复（版本号本应更新）
        # inno_setup.iss
        iss_bak = SETUP_DIR / "inno_setup.iss.bak"
        iss_src = SETUP_DIR / "inno_setup.iss"
        if iss_bak.exists():
            shutil.copy2(iss_bak, iss_src)
            iss_bak.unlink()

    def run(self):
        print()
        print("=" * 44)
        print("   氢风 自动打包工具")
        print("=" * 44)
        print()

        # 读取上次版本信息，用于输入提示和默认值
        last_ver_data = {}
        last_ver_path = PROJECT_DIR / "assets" / "asset" / "app_version.json"
        if last_ver_path.exists():
            try:
                last_ver_data = json.loads(last_ver_path.read_text(encoding="utf-8"))
            except (json.JSONDecodeError, OSError):
                last_ver_data = {}
        last_version_str = last_ver_data.get("appVersionString", "")
        last_version_type = last_ver_data.get("appVersionType", -1)
        last_release_type = _get_type_name(last_version_type) if last_version_type in (0, 1) else ""
        last_version_int = last_ver_data.get("appVersion", 0)

        # 输入版本（优先读取环境变量，支持非交互运行）
        self.version = os.environ.get("PACKAGE_VERSION") or ""
        self.release_type = os.environ.get("RELEASE_TYPE") or ""
        app_version_int_env = os.environ.get("APP_VERSION_INT") or ""
        if not self.version:
            while True:
                try:
                    prompt = "请输入版本号"
                    if last_version_str:
                        prompt += f" (回车使用上次: {last_version_str})"
                    prompt += ": "
                    raw = input(prompt).strip()
                except (EOFError, OSError):
                    raw = ""
                self.version = raw or last_version_str
                if self.version:
                    break
                print("版本号不能为空")
        if not self.release_type:
            while True:
                try:
                    prompt = "请输入发布类型 (beta/release)"
                    if last_release_type:
                        prompt += f" (回车使用上次: {last_release_type})"
                    prompt += ": "
                    raw = input(prompt).strip().lower()
                except (EOFError, OSError):
                    raw = ""
                self.release_type = raw or last_release_type
                if self.release_type in ("beta", "release"):
                    break
                print("发布类型只能是 beta 或 release")
        if not app_version_int_env:
            while True:
                try:
                    default_int = last_version_int if last_version_int else 1
                    prompt = "请输入版本整型编码"
                    if last_version_int:
                        prompt += f" (回车使用上次: {last_version_int})"
                    prompt += ": "
                    raw = input(prompt).strip()
                except (EOFError, OSError):
                    raw = ""
                if not raw:
                    self.app_version_int = default_int
                    break
                try:
                    self.app_version_int = int(raw)
                    break
                except ValueError:
                    print("版本整型编码必须为整数")
        else:
            self.app_version_int = int(app_version_int_env)

        self.version_tag = f"v{self.version}-{self.release_type}"
        print()
        print("=" * 44)
        print(f"   开始打包: {self.version_tag}")
        print("=" * 44)
        print()

        # 备份被修改的文件
        bak_gradle = PROJECT_DIR / "gradle.properties.bak"
        if not bak_gradle.exists():
            shutil.copy2(PROJECT_DIR / "gradle.properties", bak_gradle)

        try:
            ok = True
            self.step_update_version()
            ok = self.step_build_jar()

            # APK 编译
            if ok:
                ok = self.step_build_apk()
            else:
                print("[跳过] APK 编译因 JAR 失败跳过")

            # Linux / macOS 构建（construo，不依赖 JAR/APK 之外的其他步骤）
            for target_name in self.extra_targets:
                platform_label = {
                    "linuxX64": "linux",
                    "macX64": "mac",
                    "macM1": "macM1",
                }.get(target_name, target_name)
                if ok:
                    if self.step_build_construo(target_name, platform_label):
                        # Linux 额外生成 .deb 安装包 + 一键安装脚本
                        if target_name == "linuxX64":
                            constr_dir = CONSTRUO_OUTPUT_DIR / target_name
                            if self.step_build_deb(constr_dir, platform_label):
                                deb_path = getattr(self, "deb_path", None)
                                if deb_path:
                                    self.step_build_install_sh(deb_path)
                            else:
                                print("[警告] .deb 打包失败，但 tar.gz 已生成")
                    else:
                        # 跨平台包失败不阻断其他平台打包（如网络超时等）
                        print(f"[警告] {platform_label} 包构建失败，继续构建其他平台")
                else:
                    print(f"[跳过] {platform_label} 包因 JAR 失败跳过")

            # Windows 启动器 + 安装包
            if ok and not self.linux_only:
                ok = self.step_assemble_launcher()
            else:
                if not self.linux_only:
                    print("[跳过] 启动器组装因 JAR 失败跳过")

            if ok and not self.linux_only:
                ok = self.step_build_installer()
            else:
                if not self.linux_only:
                    print("[跳过] 安装包因启动器失败跳过")

            if ok:
                self.step_copy_outputs()
        finally:
            self.restore_backups()

        print()
        print("=" * 44)
        if ok:
            print("   打包完成")
            tag = f"v{self.version}-{self.release_type}"
            for f in SCRIPT_DIR.glob(f"{self.filename_base}_*_{tag}.*"):
                print(f"   {f.name}")
        else:
            print("   打包失败")
        print("=" * 44)
        print()


def main():
    os.chdir(str(PROJECT_DIR))

    config = BuildConfig()
    config.load()

    env = BuildEnvironment(config)

    # 解析命令行参数
    linux_only = "--linux-only" in sys.argv
    build_linux = linux_only or "--linux" in sys.argv
    build_mac = "--mac" in sys.argv

    extra_targets = ["linuxX64"]
    if build_mac:
        extra_targets.append("macX64")

    # 检测工具链（linux-only 模式跳过 Windows 特有检测）
    env.find_jdk()
    if not linux_only:
        env.find_iscc()
        env.check_mingw()
    env.find_android_sdk()

    # 保存配置
    config.save()
    print()

    # --config-only 模式：仅检测配置
    if "--config-only" in sys.argv:
        print("配置检测完成，已保存。")
        return

    # 开始打包
    builder = Builder(env, linux_only=linux_only, extra_targets=extra_targets)
    builder.run()


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"\n[错误] 未捕获的异常: {e}")
    finally:
        try:
            input("按 Enter 键退出...")
        except (EOFError, OSError):
            pass
