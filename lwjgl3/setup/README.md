# 氢风 Windows 原生启动器

## 概述

`launcher.exe` 是氢风在 Windows 上的原生启动程序，替换了旧版 PyInstaller 启动器。
职责单一：**读取配置 → 查找 Java → 定位 JAR → 启动游戏进程**。

## 设计目标

| 目标 | 说明 |
|---|---|
| **消除 Win7 崩溃** | PyInstaller bootloader 调用了 Win8+ API（AddDllDirectory），在 Win7 上直接崩溃。C 启动器仅用 Win7 SP1 可用 API |
| **无运行时依赖** | 静态链接 CRT，不依赖 VC++ 运行库或 .NET |
| **体积极小** | 编译后仅 ~69KB |
| **启动快速** | 无需 Python 解释器加载，毫秒级启动 |
| **无外部依赖** | 单 exe 文件，不依赖 `_internal/` 目录 |

## 工作流程

```
launcher.exe
├─ 0. 单实例检测：已有氢风在运行则弹窗「氢风已经在运行中了」并退出
├─ 1. 获取自身所在目录
├─ 2. 读取 lib/set.json（控制台显隐等配置）
├─ 3. 隐藏控制台（若 set.json 中 console=false）
├─ 4. 检查 Windows 版本 → Win7 弹实验性支持警告，Vista 及更早报错
├─ 5. 查找 Java 运行时
│   ├─ 5a. 尝试内置 JRE（lib/jre*/bin/java.exe）
│   ├─ 5b. 若无可读 set.json 中 jre 字段指定路径
│   ├─ 5c. 回退系统 Java（JAVA_HOME → PATH → Program Files）
│   └─ 5d. 验证 Java 版本 ≥ 17
├─ 6. 查找游戏 JAR（lib/jar/qingfeng-*.jar）
├─ 7. 构建命令行并启动 Java 进程（CREATE_NO_WINDOW，管道捕获 stderr）
├─ 8. 等待 Java 退出，获取退出码
├─ 9. 若退出码非零，弹窗显示 Java 的错误输出和退出码
└─ 10. 正常退出
```

## 单实例限制

启动器使用命名互斥体（`CreateMutexW`，`Local\com.hujiugame.qingfeng.launcher`）限制同一登录会话内只能运行一个氢风实例：

- 双击 `launcher.exe`（或经文件关联双击 `.qfg`）时，若已有氢风在运行，会弹出「氢风已经在运行中了」提示并立即退出，不再启动第二个进程
- 互斥体句柄在整个进程存活期间保持，随进程退出自动释放
- 命名使用 `Local\` 前缀限定当前登录会话，避免跨 RDP/服务会话误判

## 文件关联

安装器（`inno_setup.iss`）注册 5 种文件类型关联，双击后通过 `launcher.exe "%1"` 打开：

| 后缀 | 关联名 | 用途 | 图标文件 |
|------|--------|------|----------|
| `.qfg` | QF Game File | 游戏文件 | `qfg.ico` |
| `.qfl` | QF Language Pack | 语言包 | `qfl.ico` |
| `.qft` | QF Theme Pack | 主题包 | `qft.ico` |
| `.qfgl` | QF Game Language Pack | 游戏语言包 | `qfgl.ico` |
| `.qfgt` | QF Game Theme Pack | 游戏主题包 | `qfgt.ico` |

> 每种后缀可独立设置图标：替换 `lwjgl3/setup/` 下对应的 `qf?.ico` 文件后重新打包即可。
> 当前 5 个图标已基于默认主题配色二创填充（主主题色 `#3F48CC` + 副主题色 `#FDA1FF` + 黑色描边）。
> `.ico` 已包含 16/24/32/48/64/128/256 多尺寸，避免小图标模式下模糊。
> 安装后若资源管理器未刷新图标，需刷新图标缓存（`ie4uinit.exe -show` 或重启资源管理器）。

## 编译

### 前置条件

安装 MinGW-w64（二选一）：

- **w64devkit**（推荐，~50MB）：下载 [w64devkit](https://github.com/skeeto/w64devkit/releases) 解压到 `C:\tools\w64devkit`
- **完整版**：`winget install BrechtSanders.WinLibs.POSIX.MSVCRT`（~262MB）

### 编译命令

```bash
# 自动编译（推荐）
package.bat

# 手动编译
x86_64-w64-mingw32-gcc -O2 -s -static -mwindows ^
    -D_WIN32_WINNT=0x0601 -D_WIN32_IE=0x0601 ^
    -o dist\launcher\launcher.exe launcher.c -lshlwapi

# 带图标编译（需要 windres）
windres -O coff launcher.rc launcher_res.o
x86_64-w64-mingw32-gcc -O2 -s -static -mwindows ^
    -D_WIN32_WINNT=0x0601 -D_WIN32_IE=0x0601 ^
    -o dist\launcher\launcher.exe launcher.c launcher_res.o -lshlwapi
```

> 在 Git Bash / MSYS2 环境中运行时，需用 `cmd //c` 前缀避免路径转义问题。
> `package.bat` 已处理此情况。

## 目录结构

```
lwjgl3/setup/
├── launcher.c          # 启动器源码（826 行）
├── launcher.rc         # 图标资源脚本
├── console.ico         # 控制台图标
├── icon.ico            # 通用图标（默认占位）
├── qfg.ico             # .qfg 游戏文件关联图标
├── qfl.ico             # .qfl 语言包关联图标
├── qft.ico             # .qft 主题包关联图标
├── qfgl.ico            # .qfgl 游戏语言包关联图标
├── qfgt.ico            # .qfgt 游戏主题包关联图标
├── package.bat         # 一键编译脚本（自动检测 MinGW）
├── inno_setup.iss      # Windows 安装包配置
├── dist/launcher/      # 编译产物（gitignore）
│   ├── launcher.exe
│   ├── api-ms-win-core-path-l1-1-0.dll  # Win7 兼容 shim
│   └── lib/
│       ├── set.json          # 启动器配置（jre 路径、console 显隐）
│       ├── jar/qingfeng-*.jar # 游戏主程序
│       └── jre-*/             # 内置 JRE（jlink 生成）
└── old/                 # PyInstaller 启动器备份
    ├── launcher.py
    └── launcher.spec
```

## Win7 实验性支持说明

本启动器自身已确保仅调用 Win7 SP1 可用 API（通过 `_WIN32_WINNT=0x0601` 编译约束），
理论上可在 Win7 SP1 上正常启动。

但能否成功运行游戏，还取决于以下因素：

| 因素 | 说明 |
|---|---|
| **系统补丁** | Win7 需安装 KB2533623 和 Universal C Runtime，否则部分现代 API 不可用 |
| **Java 运行时** | JDK 21 对 Win7 的支持有限，可能存在未预见的兼容问题 |
| **硬件/驱动** | libGDX 基于 OpenGL，Win7 上的显卡驱动可能不完整或已过时 |
| **系统环境** | 第三方安全软件、精简版系统、缺少系统组件等均可能造成干扰 |

若在 Win7 上遇到启动失败，建议依次尝试：

1. 安装所有可选系统更新，特别是 **KB2533623** 和 **Universal C Runtime**
2. 从官网重新下载安装包（确保文件完整）
3. 更新显卡驱动至 Win7 可用的最新版本

如问题依旧，则属当前系统环境不满足运行条件，
建议升级至 **Windows 10 或更高版本** 以获得完整支持。

## 旧版 Python 启动器

`old/launcher.py` + `old/launcher.spec` 是旧版 PyInstaller 启动器的备份。
保留仅作参考，不再维护和使用。
