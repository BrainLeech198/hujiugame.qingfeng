# 氢风项目提交规范

> **文档定位**：项目提交信息格式的权威约定，包含 type/scope 列表、提交粒度规则和完整示例。
>
> **文档结构**：
> - 按 `信息结构 → type → scope → subject → issue → 破坏性变更 → 合并提交 → 版本提交 → 提交粒度 → 示例` 顺序编排
> - type/scope 列表用表格呈现，每个条目含说明和示例
> - 示例代码块用 ` ``` ` 包裹，标注语言标识
>
> **更新规范**：
> 1. 【必须】更新 `develop/CHANGELOG.md` 记录本次变更
> 2. 【必须】修改 type/scope 列表时确保示例同步更新
> 3. 【如果】修改编码规范 → 同步更新 `develop/CODING_STYLE.md`

## 1. 提交信息结构

每行提交信息不超过 72 个字符（中文字符计为 2 个宽度）。

```
<type>(<scope>): <subject>（<issue-ref>）
```

其中 type 是提交类型， scope 是提交相关文件 ，subject 使用中文描述改动内容。

### 示例

```
修复(GameHost): 修复事件队列空指针异常（issue#IJ0001@Gitee）
新增(UiManager): ImageManager 新增圆角裁剪支持（pr#1234@Github）
优化(EventDispatcher): 优化高频事件的批量处理性能
文档(README): 更新构建说明和依赖列表
```

## 2. type（必填）

| type  | 说明               | 示例                            |
|-------|------------------|-------------------------------|
| `新增`  | 新功能              | `新增(render): 新增粒子特效支持`        |
| `修复`  | 修复 bug           | `修复(audio): 修复音效重复播放问题`       |
| `优化`  | 性能优化             | `优化(core): 优化渲染管线批处理`         |
| `重构`  | 重构（不修 bug 也不加功能） | `重构(ui): Layout 加载逻辑重写`       |
| `测试 ` | 增补测试             | `测试(GameState): 增加状态转换单元测试`   |
| `文档`  | 文档变更             | `文档(develop): 更新架构文档`         |
| `构建`  | 构建工具、CI 脚本、依赖包更新 | `构建(build): 升级 Gradle 到 8.12` |

## 3. scope（可选但推荐）

scope 使用英文小写，不加反引号。

常用 scope：

| scope             | 对应模块                        |
|-------------------|-------------------------------|
| `core`            | GameHost / SceneStack / 核心引擎           |
| `render`          | 渲染层 / RenderPipeline                  |
| `ui`              | UiManager / ImageManager / 界面管理       |
| `audio`           | 音效模块                                  |
| `event`           | EventDispatcher / EventQueue / 事件系统   |
| `build`           | Gradle / 构建脚本               |
| `package`         | 打包脚本 / jlink / Inno Setup   |
| `config`          | 配置文件                        |

允许使用具体类名作 scope（如 `ImageManager`、`SceneStack`）：\
情况允许的话，可以使用文件名或详细路径名

```
修复(SceneStack): 状态推送重复触发的问题
```

## 4. subject（必填）

- 使用**中文**描述
- 简洁明了，不超过 50 个字符宽度
- 关键类名用反引号包裹

### 好：

```
修复`GameHost`事件队列未清空问题
ImageManager 新增圆角矩形裁剪方法
移除`UiManager`中已废弃的 setPosition 重载
```

### 不好：

```
更新了一些代码和修复了一些 bug
本次提交修改了多个模块，包括渲染、UI、事件处理等方面的内容，具体请查看代码
```

## 5. issue 引用（可选）

格式：

| 平台    | 格式                                  | 示例                                      |
|--------|-------------------------------------|-----------------------------------------|
| Gitee  | `（issue#XXXXX@Gitee）`               | `（issue#IJ0001@Gitee）`                  |
| GitHub | `（issue#XXXX@Github）` / `（pr#XXXX@Github）` | `（pr#1234@Github）`                      |

注意：使用中文全角括号 `（）`，与 subject 之间**不**加空格。

## 6. 破坏性变更

如果提交包含不兼容的 API 变更，在 scope后添加 `[BREAKING]`：

```
重构(render)[BREAKING]: 重绘方法签名改为传递 TextureRegion
```

## 7. 合并提交

- 合并分支：`Merge branch '<branch-name>'`
- 合并 PR（GitHub）：`Merge pull request #XXXX from <user>/<branch>`
- 合并 PR（Gitee）：`!1234 <pr-title>`

## 8. 版本提交

```
构建(release): 5.8.46
构建(prepare): 5.8.46
```

## 9. 提交粒度

一个提交只做一件事。以下情况应当拆分为多个提交：

- 修复 bug + 新增功能 → 两个提交
- 重构 + 修复 bug → 两个提交
- 代码格式化 + 逻辑变更 → 两个提交

### 允许合并的场景

- 文档更新与代码变更在同一概念单元内
- bug 修复与其对应的测试用例
- 新功能与其配套的配置文件

## 10. 完整示例

```
新增(UiManager): ImageManager 新增圆角裁剪支持（pr#1234@Github）
优化(EventDispatcher): 优化高频事件的批量处理性能
文档(README): 添加构建说明
构建(release): 5.8.46
构建(prepare): 5.8.46
测试(GameState): 添加状态转换单元测试
测试(GameHost): 添加事件队列空指针异常单元测试
```

## 附：消息格式模板

```text
<type>(<scope>): <subject>

<可选正文，每行不超过 72 字符>

<可选 footer：issue 引用等>
```

大部分提交只写标题行即可，正文仅当需要额外解释设计决策时使用。
