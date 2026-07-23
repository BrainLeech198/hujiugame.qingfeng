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
├── launcher.c          # 启动器源码（469 行）
├── launcher.rc         # 图标资源脚本
├── console.ico         # 控制台图标
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
