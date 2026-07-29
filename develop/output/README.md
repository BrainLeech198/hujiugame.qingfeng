# 氢风打包工具

> **文档定位**：一键打包脚本 `build_package.py` 的使用说明和流水线详解，涵盖版本管理体系和常见问题。
>
> **文档结构**：
> - 按 `概述 → 流水线速查 → 版本管理体系 → 分步说明 → 常见问题 → 命令速查` 顺序编排
> - 版本管理体系用表格列出三字段 × 六存储位置的对应关系
> - 流水线速查用 7 步列表概括完整流程
>
> **更新规范**：
> 1. 【必须】更新 `develop/CHANGELOG.md` 记录本次变更
> 2. 【必须】修改打包流程或脚本行为时同步更新本文档
> 3. 【必须】修改版本编码规则时同步更新版本管理体系表格
> 4. 【如果】修改构建说明 → 同步更新 `CONTRIBUTING.md`

`build_package.py` 是一键打包脚本，将氢风项目从源码编译为可分发的安装包。支持 Windows（exe 安装包）、Android（APK）、Linux（deb/一键 sh）三平台。

项目构建依赖 Gradle，本脚本在其之上串联了版本注入、原生启动器编译、JRE 裁剪和安装包制作等步骤。

---

## 使用方法

### 前置条件

- JDK 21+（自动检测 JAVA_HOME 或 PATH）
- Android SDK（路径配置在 `local.properties: sdk.dir`）
- Windows 额外需要：
  - Inno Setup 6（安装包制作）
  - MinGW-w64（C 启动器编译，未安装则使用预构建 `launcher.exe`）

### 命令

```bash
# 完整打包（Windows 安装包 + APK + Linux deb）
python build_package.py

# 仅打包 Linux
python build_package.py --linux-only

# 打包 Windows + Linux + macOS
python build_package.py --mac

# 仅检测工具链环境，不打包
python build_package.py --config-only
```

### 交互流程

1. 脚本自动检测 JDK 21、Inno Setup、MinGW-w64、Android SDK
2. 交互输入版本信息（展示上次打包的值作为参考）：

   ```
   请输入版本号 (例如 1.0.0) [上次: 1.0.0]:
   请输入发布类型 (beta/release) [上次: release]:
   请输入版本整型编码 (通常递增) [上次: 1]:
   ```

   留空则沿用上次值，整型编码默认上次 +1

3. 确认版本信息无误
4. 自动执行 7 步流水线

---

## 构建流水线

```
step_update_version   → 版本注入
    ↓
step_build_jar        → JAR 编译
    ↓
step_build_apk        → APK 编译
    ↓
step_build_construo   → Linux/macOS 原生包
    ↓
step_assemble_launcher → Windows 启动器组装
    ↓
step_build_installer  → Inno Setup 安装包
    ↓
step_copy_outputs     → 输出成品
```

### 各步骤说明

#### step 1/7: 更新版本号

| 操作 | 目标文件 | 说明 |
|------|---------|------|
| 写入 `projectVersion` | `gradle.properties` | Gradle 编译版本，影响 JAR 文件名 |
| 写入 `appVersion` / `appVersionString` / `appVersionType` | `assets/asset/app_version.json` | 运行时版本显示 + 更新检测 |
| 写入 `versionCode` / `versionName` | `android/build.gradle` | Android 系统版本号 |
| 写入 `#define MyAppVersion` | `lwjgl3/setup/inno_setup.iss` | Windows 安装包版本显示 |

#### step 2/7: 编译 JAR

- 执行 `gradlew lwjgl3:jar`
- 产出：`lwjgl3/build/libs/qingfeng-{version}.jar`
- JAR manifest 包含 `Implementation-Version` 属性，值为 `projectVersion`

#### step 3/7: 编译 APK

- 临时切换视口模式为 `fit`（Android），编译后恢复 `stretch`（桌面端）
- 执行 `gradlew android:assembleRelease`
- APK 的 `versionCode` 和 `versionName` 已由 step 1 同步

#### step 4/7: 跨平台包（construo）

- 仅 `--linux` / `--mac` 参数时执行
- 使用 Gradle `construo` 插件生成 Linux/macOS 原生包
- Linux 额外生成 `.deb` 安装包 + 自解压 `.sh` 一键安装脚本

#### step 5/7: 组装 Windows 启动器

- 编译 C 原生启动器 `launcher.exe`（MinGW-w64，静态 CRT）
- 复制 JAR 到 `dist/launcher/lib/jar/`
- `jlink` 生成最小 JRE（仅 `java.base` + `java.desktop` + `java.logging` + `jdk.unsupported` + `jdk.crypto.ec`）
- 部署 Win7 兼容补丁 `api-ms-win-core-path-l1-1-0.dll`
- 瘦身 JAR：移除 Linux/macOS 原生库（仅保留 `.dll`）

#### step 6/7: 编译安装包

- 使用 Inno Setup 编译 `inno_setup.iss`
- 产出：`lwjgl3/setup/dist/qingfeng_setup_windows.exe`

#### step 7/7: 输出成品

| 平台 | 命名格式 | 说明 |
|------|---------|------|
| Windows | `qing-feng_setup_windows_v{version}-{type}.exe` | 安装包 |
| Android | `qing-feng_setup_android_v{version}-{type}.apk` | APK |
| Linux | `qing-feng_setup_linux_v{version}-{type}.deb` | deb 安装包 |
| Linux | `qing-feng_setup_linux_v{version}-{type}.sh` | 自解压一键安装脚本 |

---

## 版本管理体系

### 三个版本号字段

| 字段 | 类型 | 说明 | 对比用途 |
|------|------|------|---------|
| `appVersion` | int | 单调递增的整型编码 | 运行时更新检测的主依据 |
| `appVersionString` | string | `major.minor.patch` 格式 | 展示给用户看 |
| `appVersionType` | int | `0`=beta, `1`=release | 同版本号时判断 beta→release 升级 |

### 版本存储位置

| 存储位置 | 文件 | 用途 |
|---------|------|------|
| 源码内 | `assets/asset/app_version.json` | 打包到 JAR/APK 内部，运行时读取 |
| 用户目录 | `~/hujiugame/qingfeng/asset/app_version.json` | 首次运行由 UpdateChecker 复制到外部 |
| 官网 | `docs/data/versions.json`（GitHub Pages） | 远程版本检测对照 |
| Gradle | `gradle.properties: projectVersion` | JAR 文件名和 manifest |
| Android | `android/build.gradle: versionCode/versionName` | 系统级版本标识 |
| 安装包 | `lwjgl3/setup/inno_setup.iss: MyAppVersion` | 安装包属性显示 |

### 版本号使用规范

- **appVersion（整型）**: 每次发布递增 1。原则上不跳跃、不回退
- **appVersionString**: 遵循语义化版本 `major.minor.patch`。major 不兼容时递增 major，功能新增递增 minor，bug 修复递增 patch
- **appVersionType**: beta 阶段用 `0`，正式发布用 `1`。同版本号从 beta 升级到 release 时触发更新提醒

---

## 运行时更新检测机制

`UpdateChecker` 在应用启动后异步请求 `https://brainleech198.github.io/hujiugame-qingfeng/data/versions.json`，三段式判断是否需要更新：

```
远程 newest_version  >  本地 appVersion    → 需要更新
远程 newest_version == 本地 appVersion
  && 远程 type > 本地 type                  → 需要更新（beta→release）
远程无整型字段                    → 回退字符串比较
```

检测结果在游戏主菜单以弹窗提示用户前往官网下载新版本。

---

## 常见问题

### 打包后 APK versionCode 未更新

检查 `android/build.gradle` 是否被 Git 恢复。脚本在 `step_update_version()` 阶段写入，如果打包前 `git checkout` 过该文件，需要重新运行。

### 如何跳过版本确认直接打包

设置环境变量可非交互运行：

```bash
export PACKAGE_VERSION=1.0.1
export RELEASE_TYPE=release
export APP_VERSION_INT=2
python build_package.py
```

### 产出版本号与预期不符

`gradle.properties` 中的 `projectVersion` 是 Gradle 编译入口。如果 JAR 文件名版本不对，检查 `step_update_version()` 是否成功修改了 `projectVersion`。
