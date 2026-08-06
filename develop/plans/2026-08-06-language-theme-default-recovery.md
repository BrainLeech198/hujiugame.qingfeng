# 语言/主题默认配置损坏恢复 — 预想方案

> **状态:** 设计预想，未排期。记录"用户手动删除 External 默认配置（语言/主题）"场景的现状盘点与将来实现方向。
>
> 背景：用户实测清空 External `hujiugame/qingfeng/asset/language/` 后重启，配置（language_config.json）补全，但语言包只恢复 zh_CN（zh_TW/en_US 缺失）。当前 Internal 化已消除该问题的主路径，剩余边界场景见下文。

---

## 现状盘点（2026-08-06 已实施）

官方语言/主题走 Internal 句柄化（kind="internal"），官方内容不再复制到 External，从根上消除了"官方备选语言/主题目录缺失"问题。

| 项 | 现状 |
|---|---|
| 官方默认语言/主题 | kind="internal"，Internal 句柄直读，不依赖 External 目录 |
| 第三方语言/主题 | 省略 kind（或非 internal）→ External 句柄，独立目录 + 在词典注册字段 |
| 词典保护 | `language_config.json` / `theme_config.json` 在 `update_config.json` 的 protect 列表 |
| 词典融合 | 回退分支中 External 词典 `.combined(内部词典)`，补回缺失的官方条目 |
| 损坏修复兜底 | INIT 修复按钮 → `UpdateChecker.repairGame` → 全量重同步 |

## 场景描述（"用户把默认配置删掉"）

1. **外部词典官方条目被删**：用户删除 External `language_config.json` / `theme_config.json` 里的官方条目（或整个文件）。当前融合**只在回退分支触发**；若当前语言/主题命中（如 zh_CN 正常），不会触发融合，外部词典永久缺官方条目，用户切换 zh_TW 时解析失败。
2. **用户配置指向已删语言/主题**：`user_config.json` 的 language/theme 指向已删除项 → 回退分支兜底（Internal 默认 + 修复配置 + 融合词典），当前已覆盖。
3. **外部官方目录被清空**：Internal 化后官方目录不从 External 读，此场景不再致命。

## 将来实现方向

| 编号 | 方向 | 说明 |
|---|---|---|
| A | 融合前置/统一化 | 将"融合内部词典"从回退分支抽出：命中分支（或 init 末尾）也执行，只要 External 词典存在就与内部词典 `combined()`（旧优先），确保官方条目常驻 |
| B | 词典就绪后校验官方条目 | parse 后遍历内部词典 keySet，缺失的官方条目从内部词典补入外部词典并回写 |
| C | 外部官方目录补齐 | Internal 化后**不需要**复制官方目录到 External（官方 Internal 引用）；仅当未来某个官方主题/语言需外部可写时才考虑 |
| D | 用户配置完整性校验 | 检查 user_config 的 language/theme 是否仍在词典中，缺失则回退默认并修复配置（当前已在回退分支覆盖） |

## 注意事项

- 融合 `combined()` 语义为"this 旧优先，other 只补缺失"，第三方字段不会被覆盖
- 游戏内语言/主题（isLauncher=false）词典缺失时 return false 回滚，不适用启动器兜底
- 实现时注意：融合写回 External 词典需保留第三方条目与嵌套结构（name/kind）
