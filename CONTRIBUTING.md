# 贡献指南

## 欢迎

欢迎加入 QingFeng Launcher 项目！本文档帮助新成员快速上手。

## 技术栈

- **语言**: Java 17+（编译目标 Java 8，运行 Gradle 需要 JDK 17+，推荐 JDK 21）
- **框架**: [libGDX 1.13.1](https://libgdx.com/) (基于 OpenGL 的跨平台游戏框架)
- **UI 系统**: scene2d (libGDX 内置的保留模式 UI 框架)
- **构建工具**: Gradle 8.14 (多项目结构: core / lwjgl3 / android)
- **编码规范**: 见 [CODING_STYLE.md](develop/CODING_STYLE.md)

## 项目架构

### 一句话概括

> 一个基于 libGDX 的状态驱动游戏启动器，用事件系统解耦模块，用渲染管线分发游戏画面。

### 主循环链路 (每帧执行顺序)

```
Main.render(deltaTime)
  └─ GameHost.run(deltaTime)
       ├─ 1. renderPipeline.updateFrame(deltaTime)     ← 处理输入、更新逻辑
       ├─ 2. while eventQueue.hasEvent()               ← 遍历事件队列
       │      eventDispatcher.handleEvent(eventObject)  ← 事件分发
       └─ 3. renderPipeline.render(deltaTime)          ← 渲染当前画面
            ├─ currentRender.updateFrame(deltaTime)    ← 游戏逻辑更新
            ├─ UiManager 操作                           ← UI 更新
            └─ GraphicsManager 渲染                     ← 绘制到屏幕
  └─ stage.act(deltaTime)                              ← 驱动 scene2d 动画和输入
  └─ stage.draw()                                      ← 绘制 scene2d UI
```

### 包结构速查

```
com.hujiugame.qingfeng
  ├─ Main.java             入口，主循环入口（Main.render → GameHost.run）
  ├─ core/                  GameHost (总控)、SceneStack (状态栈)、RenderPipeline (渲染注册表)
  ├─ audio/                 AudioManager (音频)
  ├─ data/                  数据类 (LayoutConfig、UiInfo、ImageInfo、ButtonInfo 等)
  ├─ di/                    InstanceContent (服务定位器)
  ├─ event/                 事件系统 (EventObject → EventQueue → EventDispatcher)
  ├─ game/                  游戏会话管理 (GameSessionManager 等)
  ├─ graphic/               GraphicsManager (纹理/渲染管理)
  ├─ input/                 ControllerInputHandler、VirtualInputHandler 等输入处理
  ├─ manager/               ThemeManager、LanguageManager、TextManager 等配置管理
  ├─ scene/                 GameRender 实现 (GameMenu、GamePlay、GameSettings 等)
  ├─ script/                脚本引擎 (Block/Action/Param/Parser)
  ├─ type/                  类型与常量 (VersionType、RequirementKey、UiKey 等)
  ├─ ui/                    UiManager (UI 管理, 内部委托给 ImageManager/LabelManager/ButtonManager)
  └─ util/                  工具类 (日志、文件、JSON、输入处理)
```

### 状态机

`SceneStack (state + subState)` → `RenderPipeline (state×1000+subState → GameRender)`

当前状态决定显示哪个画面（主菜单、游戏中、设置页等）。

## 建议阅读顺序

如果你是第一次接触代码库，按这个顺序阅读：

1. **`Main.java`** (`com.hujiugame.qingfeng.Main`) — 入口，了解主循环结构
2. **`GameHost.java`** (`core/`) — 总控，了解状态流转
3. **`GameSessionManager.java`** (`game/`) — 会话管理，理解游戏加载/进入/退出流程
4. **`InstanceContent.java`** (`di/`) — 服务定位器，了解各管理器如何装配
5. 选择以下之一深入：
   - **`UiManager.java`** (`ui/`) → UI 管理（内部委托给 ImageManager/LabelManager/ButtonManager）
   - **`GraphicsManager.java`** (`graphic/`) → 图形渲染
   - 某一个 `GameRender` 实现（如 `scene/GameMenu.java`）→ 具体画面如何运作

## 开发环境

### 前置要求

- JDK 17+（Gradle 8.x 要求，推荐 JDK 21，`java -version` 确认）
- Android SDK（仅构建 Android 版本时需要）
- MinGW-w64（仅全量打包时需要，编译原生 C 启动器 `launcher.exe`）
  - 推荐 [w64devkit](https://github.com/skeeto/w64devkit/releases)（~50MB，解压即用）
  - 或 `winget install BrechtSanders.WinLibs.POSIX.MSVCRT`
  - `lwjgl3/setup/package.bat` 会自动检测 PATH / w64devkit / CLion 捆绑的 MinGW

### 常用命令

```bash
./gradlew lwjgl3:run        # 运行桌面版
./gradlew lwjgl3:jar        # 打包可执行 JAR
./gradlew build              # 完整构建
./gradlew clean              # 清理构建产物
./gradlew android:assembleDebug  # 构建 Android APK
```

> Windows 用户使用 `gradlew.bat` 替代 `./gradlew`。

### 发布打包

使用 `develop/output/build_package.py` 一键打包所有平台：

```bash
python develop/output/build_package.py
```

脚本工作流：**输入版本号 → 更新文件 → 编译 JAR（剔除 .so/.dylib 瘦身）→ 编译 APK → 编译 Linux/macOS 平台包 → 编译原生 C 启动器（`launcher.c`，替换旧 PyInstaller）→ jlink 生成最小 JRE（~40MB）→ Inno Setup 打包 → 输出成品**

**自动更新的文件：**

| 文件                              | 字段                                    | 说明                         |
|---------------------------------|---------------------------------------|----------------------------|
| `gradle.properties`             | `projectVersion`                      | Gradle 构建版本号               |
| `assets/asset/app_version.json` | `appVersionString` / `appVersionType` | 运行时版本显示（beta=0, release=1） |
| `lwjgl3/setup/inno_setup.iss`   | `MyAppVersion`                        | Windows 安装包版本号             |

**输出成品（`develop/output/`）：**

| 文件                                           | 格式     | 说明                         |
|----------------------------------------------|--------|----------------------------|
| `qing-feng_setup_android_v{ver}-{type}.apk`  | APK    | Android 安装包                |
| `qing-feng_setup_windows_v{ver}-{type}.exe`  | EXE    | Windows 安装包（Inno Setup）    |
| `qing-feng_setup_linux_v{ver}-{type}.sh`     | SH     | Linux 自解压安装包（双击运行，自动图形化安装） |
| `qing-feng_setup_mac_v{ver}-{type}.tar.gz`   | tar.gz | macOS 压缩包                  |

**命令行选项：**

- `--linux-only` — 仅打包 Linux（跳过 Windows）
- `--mac` — 额外构建 macOS 平台
- `--config-only` — 仅检测并保存环境配置，不打包

### 网络受限环境

如果依赖下载出现 `Connection timed out` 错误，按以下顺序排查：

1. **确认 JDK 已安装**：`java -version`，若无则手动安装 JDK 21（[Adoptium](https://adoptium.net/)）
2. **指定 JDK 路径**：在 `gradle.properties`（项目根目录）中添加：
   ```
   org.gradle.java.home=C:\\Program Files\\...\\jdk-21
   ```
3. **配置全局 Gradle 镜像**（可选）：在 `C:\Users\<用户名>\.gradle\init.gradle` 中写入阿里云镜像配置，强制所有依赖优先走国内源
4. **配置代理**（仅公司内网）：在 `gradle.properties` 中添加：
   ```
   systemProp.http.proxyHost=your-proxy-host
   systemProp.http.proxyPort=your-proxy-port
   ```

### Windows 原生启动器源码

`lwjgl3/setup/launcher.c` — 替换了旧版 PyInstaller 启动器，解决 Win7 兼容问题（PyInstaller bootloader 依赖 Win8+ API）。

仅使用 Win7 SP1 可用 API，静态链接 CRT，无运行时库分发需求。编译命令（需 MinGW-w64）：
```bash
lwjgl3/setup/package.bat    # 一键编译（含图标嵌入）
```

旧版 Python 启动器备份在 `lwjgl3/setup/old/`。

### IDE 配置

推荐 IntelliJ IDEA。导入方式：`File → Open → 选择项目根目录的 build.gradle`。

确保 IDE 的编译编码为 **UTF-8**（项目已统一配置）。

## 代码规范

- Allman 风格大括号（左大括号独占一行）
- 4 空格缩进，不 Tab
- 日志使用 `LogUtils.debug/info/error(ClassName.class, "message")` 格式
- 公开方法必须写 Javadoc（含 `@param`、`@return`）
- 详细规范见 [CODING_STYLE.md](develop/CODING_STYLE.md)

## 提交流程

1. 从 `main` 创建特性分支：`git checkout -b feat/your-feature`
2. 实现功能或修复 bug
3. 更新 [CHANGELOG.md](develop/CHANGELOG.md)
4. 提交 PR 到 `main` 分支

如果修改了核心逻辑（UiManager、GameHost、主循环相关），提交前运行一遍完整的 Gradle 构建验证。
