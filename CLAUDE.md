# CLAUDE.md

> **启动必读**：新会话首次回复前，先读取 `temp/CLAUDE_MEMORY.md` 了解历史设计决策和协作约定。

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

- **文档统一索引**：[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) — 新增/重命名/删除任何 `.md` 文件后，必须同步更新此索引
- **变更日志**：`develop/CHANGELOG.md` — 每次提交前必须更新，条目按时间倒序，段落按标准顺序排列；**CHANGELOG 条目不独立提交**，每个内容改动 = 一笔提交（对应改动文件 + 该改动对应的 CHANGELOG 条目一并提交），按内容逐条拆分，禁止攒一堆条目最后统一提交
- **各文档头部自描述规范** — develop/ 下每份文档头部均包含"文档定位 + 文档结构 + 更新规范"，修改文档前先读头部了解其维护要求
- **设计方案**：`develop/plans/` 目录 — 新建设计方案/预想方案时在此记录，文件名格式 `yyyy-MM-dd-topic.md`
- **贡献指南**：[CONTRIBUTING.md](CONTRIBUTING.md) — 修改构建流程、技术栈或开发环境时同步更新
- **Windows 启动器**：[lwjgl3/setup/README.md](lwjgl3/setup/README.md) — 修改启动器行为或 Win7 兼容策略后同步更新
- **官方网站**：[docs/README.md](docs/README.md) — 修改网站功能或下载入口时同步更新
- **打包工具**：[develop/output/README.md](develop/output/README.md) — 修改打包流程或版本管理体系时同步更新

## 本地工作记忆

- **设计决策日志**：[temp/CLAUDE_MEMORY.md](temp/CLAUDE_MEMORY.md) — gitignored 的本地工作记忆，记录精确的设计决策、架构分析记录。新记录追加到末尾，设计决策用表格呈现。此文件仅当前开发机可见，不随项目分发。
