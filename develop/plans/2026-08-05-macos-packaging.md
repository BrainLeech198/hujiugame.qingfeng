# macOS 打包支持 — 预想方案

> **状态:** 设计预想，未排期。本文档记录 macOS 打包的现状盘点与差距清单，供将来实施时参考。
>
> 背景：同学有 Mac / iPhone，考虑让同学跑起游戏，问"mac 打包还差什么"与"mac 能否打开自动安装自动运行"。

---

## 现状盘点（已就绪，无需再补）

| 项 | 状态 | 说明 |
|---|---|---|
| construo 双 target | ✅ | `lwjgl3/build.gradle` 已注册 `macX64`（Intel）/ `macM1`（Apple Silicon），含 JDK 21 南大镜像源、`identifier=com.hujiugame.qingfeng.qingfeng`、`macIcon` |
| ICNS 图标 | ✅ | `lwjgl3/icons/logo.icns` 已存在（与 logo.png/logo.ico 并列） |
| 原生库 | ✅ | `gdx-platform:natives-desktop` 含 macos/arm64 与 macos/x64 的 `.dylib` |
| jlink 模块 | ✅ | 已加 `java.desktop`（AWT/文件对话框/崩溃弹窗需要） |
| 交叉编译入口 | ✅ | `develop/output/build_package.py --mac` 从 Windows 交叉编译，非 Windows 产物自动瘦身 JAR 移除 Windows DLL |
| run task mac 适配 | ✅ | `gradlew run` 在 mac 自动加 `-XstartOnFirstThread`（`build.gradle:57`） |

## 差距清单（按优先级）

### 1. `-XstartOnFirstThread` 未进 construo 启动脚本（最可能致命）

- **问题**：`build.gradle:57` 只把该参数加在 `run` task（仅 `gradlew run` 生效）；construo 块（127-174 行）**没有任何 jvmArgs 配置**，第 139 行注释"JVM 参数配置（生成启动脚本用）"后面是空的。
- **影响**：lwjgl3 在 macOS 缺该参数，GLFW 初始化会崩溃。产出的 `.app` 很可能跑不起来。
- **待办**：给 construo 补 mac target 的 jvmArgs（具体 DSL 写法需在 Mac 上实测，并确认 construo 2.0.1 是否已默认处理）。

### 2. 真实 Mac 实测

- 开发机是 Windows，产物只能在同学 Mac 上验证：JDK 下载源通畅、`.app` 能启动、HiDPI/字体/音频/文件对话框/手柄。
- 同学是 M 芯片且用 `--mac`（macX64）时，需要安装 Rosetta 才能运行。

### 3. 代码签名 / Gatekeeper

- 未签名 `.app` 首次双击被 Gatekeeper 拦截（quarantine 属性），只能右键→打开或系统设置放行。
- 至少做 ad-hoc 签名：`codesign --force --deep -s -`（在 Mac 上执行）。
- 正式公开分发需 Developer ID 签名 + 公证（notarization），要求 Apple Developer 账号（$99/年），工具链 `xcrun notarytool` 仅 macOS 可用。

### 4. `--macM1` 参数未接线

- `build_package.py:1306-1310` 只解析 `--mac` → 追加 `macX64`；construo 已配 `macM1` target 但脚本无入口。
- 原生支持 Apple Silicon 需手动 `gradlew lwjgl3:packageMacM1` 或给脚本补参数。

### 5. 文档同步

- 产物清单补 mac 条目：CONTRIBUTING.md / develop/output/README.md / CHANGELOG.md。

---

## "打开自动安装自动运行" 可行性结论

| 场景 | 做法 | 代价 |
|---|---|---|
| 同学之间测试 | `.app` 打 zip 发过去，解压后右键→打开 | 零成本，操作说明多一句 |
| 接近 Windows 体验 | `.dmg`（双击→拖到 Applications）+ ad-hoc 签名 | 需一台 Mac 跑 `hdiutil` |
| 公开分发 | DMG/pkg + Developer ID 签名 + 公证 | Mac + $99 账号，每次打包都要跑公证 |

**结论**：
- macOS 生态无"安装器结束自动运行"约定；`.app` 本质是目录，拷贝即安装，"解压即用"是用户习惯。
- 真正拦路的是 Gatekeeper 安全模型 + 签名/公证工具链仅 macOS 可用。**无账号的第三方 mac 分发上限就是"右键打开"**，无法用代码跳过。
- 建议当前阶段用 zip 传 `.app`，等公开分发时再做 DMG + 签名/公证。
- 与"平台/启动器"定位相关的预告：将来 `.qfg` 双击关联、社区分发同样绕不开签名。
