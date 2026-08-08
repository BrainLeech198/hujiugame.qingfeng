# 页面切换过渡动画 — 设计方案

> **状态:** 方案框架已确认（2026-08-07），2026-08-08 深化：取消主题底色清屏（纯 alpha 渐变 + 黑色清屏）、动画期间屏蔽输入；实现分阶段推进；每帧推进挂钩点待定。
>
> 背景：当前页面切换经 `SceneStack.updateGameState()` → `renderPipeline.clear()`（销毁旧渲染机）+ `renderPipeline.update()`（创建新渲染机）同步完成，页面瞬间替换，无任何过渡动画。目标是在尽量不动核心流程的前提下，为页面切换增加淡出淡入动画。

---

## 背景与目标

当前切换链路（同步、无动画）：

- `SceneStack.pushGameState/popGameState/setGameState/resetGameState` → 统一走 `updateGameState()`
- `updateGameState()` → `renderPipeline.clear()`（旧渲染机 dispose）+ `renderPipeline.update()`（新渲染机 init）
- 结果：切页瞬间完成，无过渡

目标：

- 页面切换时可播放过渡动画：旧页内容 alpha 递减至黑屏 → 新页内容自黑屏递增（淡出淡入，纯 alpha 渐变）
- 可关闭动画：页面强制立即（config `immediatelyOut/In`）或用户禁用（user_config `allowFadeOut/In`）
- 不修改 `GameHost`/`UiManager`/`Main` 的核心流程，尽量少动 `RenderPipeline`
- 为控件级动画（graphics/ui 元素自身切入/切出）预留扩展位，作为后续迭代

## 需求决策（用户确认）

| 方面 | 决策 |
|------|------|
| 实现路线 | **先顺序、后交叉（迭代）**：阶段一 = 顺序淡出淡入（内容 alpha + 主题底色清屏）；阶段二 = 交叉淡化（旧页淡出的同时新页淡入） |
| 时长 | 淡出/淡入**分开配置**（`outDuration` / `inDuration`） |
| 方向 | 淡出/淡入**独立开关**（config `immediatelyOut/In`、user_config `allowFadeOut/In`） |
| 输入 | 动画期间**禁止任何用户操作导致的游戏逻辑更新**（屏蔽输入） |
| 优先级 | **页面强制立即 > 用户配置**：config `immediatelyOut/In=true` 时，无论用户配置如何都立即切换 |
| 过渡遮罩 | 否决"遮罩盖屏"方案（不自然、不好看），也**取消主题底色清屏**，改为**纯内容 alpha 渐变 + 保持现有黑色清屏** |

## 方案

### 阶段一：顺序淡出淡入（先实现）

**流程：**

1. 收到切换请求 → 若对应方向动画未激活（config `immediately` 或 user_config `allowFade` 任一禁用）→ 走原同步链路，立即切换
2. 若动画激活 → 进入过渡态：**旧页面保持"活着"继续渲染**，透明度 `1 → 0`（历时 `outDuration`），底层保持现有黑色清屏
3. 淡出完成 → 真正执行 `renderPipeline.clear()` + `update()`（加载新页面）
4. 新页面渲染，透明度 `0 → 1`（历时 `inDuration`）
5. 过渡结束，恢复正常

**关键点：延迟切换。** 淡出期间旧页面必须继续渲染，因此切换请求需被过渡管理模块"拦截"，持有为待执行切换（pendingSwitch），淡出完成后才真正调 `updateGameState` 内部链路。这是阶段一唯一的架构新增点，不碰 `RenderPipeline` 本身。

**透明度控制层级（两层都要乘，保证整页一致淡出）：**

- Scene2D：`stage.getRoot().getColor().a`——UiManager 的 CustomImage/CustomLabel/CustomTextButton 均实现 `draw(Batch, parentAlpha)`，原生支持透明度向下传递
- GraphicsManager：背景与图片绘制时 tint alpha 乘全局过渡透明度（`putPicture` 已支持 `Color tint`）

**清屏（2026-08-08 深化：取消主题底色清屏）：**

- `Main.java:238` 的 `ScreenUtils.clear(0, 0, 0, 1f)` 保持现状，不做主题色改造
- 过渡期间底层就是黑色清屏，旧页/新页 alpha 渐变在黑色上完成，视觉最简单
- 若后续想要"主题底色"，可作为独立增强（新增 theme.json `backgroundColor` 字段），不在本次范围

### 阶段二：交叉淡化（迭代方向）

- 旧页淡出的同时新页淡入，两层交叉，消除顺序过渡中间的主题底色间隙
- 实现方向：双渲染机层叠渲染，或旧页截图（FBO/帧缓冲）作为纹理淡出
- 需动 `RenderPipeline`，架构改动大、资源占用高，作为阶段一落地后的迭代项

## 配置结构

### 页面 config.json 新增 `animation` 节点

```json
{
  "animation" : {
    "immediatelyOut" : false,
    "immediatelyIn" : false,
    "outDuration" : 0.3,
    "inDuration" : 0.3
  }
}
```

- `immediatelyOut/In`：`true` 时强制**立即**切换该方向（不播动画），**优先级高于用户配置**
- `outDuration/inDuration`：淡出/淡入时长（秒），未配置时用引擎默认值

### user_config.json 新增 `animation` 节点

```json
{
  "animation" : {
    "allowFadeOut" : true,
    "allowFadeIn" : true
  }
}
```

- 用户总开关：`false` 时禁用对应方向动画（立即切换）

**激活判定：** `方向播放动画 = !config.immediately<方向> && user_config.allowFade<方向>`（config 强制立即 > 用户配置）。

## 控件级 animation（待确认扩展，不在本次实现范围）

用户构想：layout 的 `graphics` / `ui` 内每个元素增加 `animation` 字段，子字段为"切入"/"切出"，其下再为动画类型 + 参数。示意：

```json
"animation" : {
  "in" : { "type" : "fade", "duration" : 0.2 },
  "out" : { "type" : "fade", "duration" : 0.2 }
}
```

- 字段命名与 JSON 风格待确认（建议英文 `in`/`out`，与页面级 `immediatelyIn/Out` 命名呼应）
- 动画类型集（fade / slide / zoom 等）与参数集待设计
- 与页面级过渡的关系、触发时机（随页面切换 or 独立）待确认
- 作为阶段二之后或独立迭代

## 待确认点

| 项目 | 现状 |
|------|------|
| 每帧推进挂钩点 | SceneStack 无每帧回调；候选：RenderPipeline.updateFrame 加可注入回调（推荐，不动 GameHost）、GameHost.run 加一行、scene2d Actions。待定（2026-08-08 先不急） |
| 淡出期间再次切换 | 已解决：动画期间屏蔽输入，无输入触发切换，pendingSwitch 竞争自然消除 |
| 音频是否随过渡淡出 | 本次不涉及，保持现状 |
| 游戏内页面切换 | `GamePlay` 页面切换（`Player.nextPage` 链路）与启动器 `SceneStack` 不同，是否也要动画待确认 |

## 实施步骤（阶段一，待细化）

1. 过渡管理模块：新增过渡状态机（NONE → FADING_OUT → LOADING → FADING_IN → NONE），拦截切换请求、持有 pendingSwitch；淡出期间先不 push stateStack，避免状态与渲染不一致
2. 输入屏蔽：动画期间禁止用户操作导致的游戏逻辑更新（输入处理器不响应触发状态切换的操作）
3. 透明度应用：UiManager 新增 `setGlobalAlpha`（stage root alpha）+ GraphicsManager 新增 `setTransitionAlpha`（putLayout 内统一乘 batch color）
4. 每帧推进挂钩点：待定（见待确认点）
5. config/user_config 解析：新增 `animation` 节点解析与激活判定
6. 编译验证（`./gradlew :core:compileJava` 或对应任务）
7. 文档：`develop/JSON_STANDARD.md` 补 config.json / user_config.json 的 animation 节点、`DOCUMENTATION_INDEX.md` 新增本文档条目、`develop/CHANGELOG.md` 记录（随提交）
