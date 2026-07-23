# CLAUDE.md

## 代码规范

本项目编码规范详见 `develop/CODING_STYLE.md`，以下为摘要：

### 大括号

Allman 风格（左括号独占一行）：

- 类、方法、控制流、匿名内部类全部另起一行

### 缩进

- 4 空格，不使用 Tab
- 多行参数对齐，长调用链使用 8 空格

### 空格

- 方法/构造函数声明：左括号前加空格 `void method (int param)`
- 方法调用：左括号前不加空格 `method(param)`
- 控制流关键字后加空格 `if (condition)`

### 命名

| 类型         | 规范                     | 示例                     |
|------------|------------------------|------------------------|
| 类/接口       | PascalCase             | `GameHost`             |
| 方法/字段/参数   | camelCase              | `loadGame()`, `player` |
| 常量         | UPPER_SNAKE_CASE       | `MENU_MAIN`            |
| Getter     | `getXxx()`             | `getState()`           |
| 消耗型 Getter | `consumeXxx()`         | `consumeClicked()`     |
| 布尔查询       | `isXxx()` / `hasXxx()` | `isClicked()`          |
| 动作型布尔      | `doXxx()`              | `doInit()`             |

### 其他

- 修饰符顺序：`public/private` → `static` → `final`
- 导入顺序：Java 标准库 → libGDX → 项目内部，各分组内字母排序
- 日志：`LogUtils.debug/info/error(ClassName.class, "message")`
- 节分隔符：100 个 `=` 号
- 公开 API 必须有 Javadoc，关键逻辑写中文注释说明"为什么"

## 注意

- 主循环链路：`Main.render` → `GameHost.run` → `renderPipeline.updateFrame` → `while eventQueue.hasEvent / eventDispatcher.handleEvent` → `renderPipeline.render`
- 服务定位器在 `InstanceContent.java`
- 不要随意修改 `GameHost`、`UiManager`、`Main` 的核心流程

## 文档维护

- **项目文档统一索引**：[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) — 新增/重命名/删除任何 .md 文件后，必须同步更新此索引
- **Windows 启动器说明**：[lwjgl3/setup/README.md](lwjgl3/setup/README.md) — 修改启动器行为或 Win7 兼容策略后同步更新
- **更新日志**：`develop/CHANGELOG.md` 每次提交前必须更新
- **翻新进度**：`develop/REVIEW.md` 实现新功能后更新对应条目
