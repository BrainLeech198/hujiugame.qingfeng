# 3D 场景支持 — 预想方案

> **状态:** 设计预想，未排期。本文档记录接入思路和决策依据，供将来实施时参考。

---

## 目标

为视觉小说引擎提供可选的 3D 场景能力，让作者在需要沉浸式/自由交互场景时（如 RPG 地图探索），能以最小的架构代价在 page 中加入 3D 内容。

**设计原则:**
- 不破坏现有 2D 页面的工作流
- 不动核心流程（GameHost、RenderPipeline、Main）
- 3D 是可选附加能力，不是引擎的第二个渲染范式

---

## 方案: page 目录加一个 `3d.json`

### 思路

现有 page 目录结构:

```
pages/
  my_page/
    layout.json       # 2D 视觉布局（现有）
    behavior.json     # 脚本行为（现有）
```

追加可选文件:

```
pages/
  my_page/
    layout.json       # 2D 视觉布局（不变）
    behavior.json     # 脚本行为（不变）
    3d.json           # ← 新增：3D 场景描述（可选）
```

- 有 `3d.json` → 该 page 渲染时先渲染 3D 场景，再叠 2D Layout（UI/对话框等）
- 无 `3d.json` → 行为与现在完全一致

### 数据模型

```json
{
  "camera": {
    "position": [0, 5, 10],
    "target": [0, 0, 0],
    "fov": 60
  },
  "models": [
    {
      "id": "terrain",
      "file": "models/terrain.g3db",
      "position": [0, 0, 0],
      "rotation": [0, 0, 0, 1],
      "scale": [1, 1, 1],
      "animation": null
    },
    {
      "id": "character",
      "file": "models/char.g3db",
      "position": [2, 0, 3],
      "rotation": [0, 0.707, 0, 0.707],
      "animation": "idle"
    }
  ],
  "lights": [
    {
      "type": "directional",
      "direction": [-0.5, -1, -0.5],
      "color": [1, 1, 1],
      "intensity": 0.8
    }
  ]
}
```

| 字段 | 说明 | 必填 |
|------|------|------|
| `camera` | 透视相机初始位置/目标/FOV | 否（有默认值） |
| `models[]` | 模型列表，`id` 供脚本引用 | 否 |
| `models[].file` | `.g3db` 路径（相对 assets） | 是 |
| `models[].animation` | 初始动画名 | 否 |
| `lights[]` | 光源列表 | 否（有默认环境光） |

---

## 实施路径（未来做时参考）

### 阶段 1: 数据加载 + 基础渲染

**改动文件:**

| 文件 | 改动 |
|------|------|
| `Page.java` | + `JsonEntity scene3dConfig` 字段 + `loadScene3dConfig()` 方法 + `getScene3dConfig()` getter |
| `FileName.java` | + `SCENE_3D = "3d.json"` 常量 |
| `GamePlay.java` | `render()` 中检测 `scene3dConfig`，存在则驱动 ModelBatch 渲染 3D |

**Page.java 改动示意:**

```java
// 新增字段
private JsonEntity scene3dConfig;

// 构造函数中追加一行
loadScene3dConfig(pagePathHandle);  // 失败不置 valid=false，纯可选

// 新增加载方法
private void loadScene3dConfig (FileHandle pagePathHandle)
{
    FileHandle configPath = pagePathHandle.child(FileName.SCENE_3D);
    if (!FileUtils.isFileExist(configPath)) return;  // 没有 3d 配置，正常跳过
    this.scene3dConfig = new JsonEntity(configPath);
}

// 新增 getter
public JsonEntity getScene3dConfig () { return scene3dConfig; }
```

**GamePlay.render() 改动示意:**

```java
@Override
public void render (float deltaTime)
{
    // 渲染 3D 场景（如果当前 page 有 3d 配置）
    Page nowPage = ...;
    if (nowPage != null && nowPage.getScene3dConfig() != null)
    {
        renderScene3d(nowPage.getScene3dConfig(), deltaTime);
    }

    // 渲染 2D 布局（不变）
    gameAudioManager.playLayout(layout);
    gameGraphicsManager.putLayout(layout, deltaTime);
}
```

`renderScene3d()` 内部:
1. 解析 `scene3dConfig` 创建/更新 `PerspectiveCamera`、`Environment`、`ModelInstance` 列表
2. `modelBatch.begin(cam)` → 遍历渲染 → `modelBatch.end()`
3. `Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)` — 清深度缓冲，避免挡住 2D UI

**关于 G3dManager（可选抽取）:**

如果后续多个页面都需要 3D，或者单个 GamePlay 中 `renderScene3d` 逻辑膨胀，可以抽出独立类:

```
core/.../graphic/G3dManager.java
```

职责:
- 持有 `ModelBatch`、`PerspectiveCamera`、`Environment`
- 提供 `loadScene(json)`、`update(delta)`、`render()` 方法
- 管理 `ModelInstance` 池，避免反复加载/释放

这属于重构，不是必须的。初期直接在 GamePlay 内实现即可。

### 阶段 2: 脚本操控（可选）

新增脚本指令，让 `behavior.json` 能控制 3D 场景:

| 指令 | 作用 |
|------|------|
| `moveObject(id, x, y, z)` | 移动模型 |
| `rotateObject(id, x, y, z, w)` | 旋转模型（四元数） |
| `playAnimation(id, name)` | 播放骨骼动画 |
| `setCamera(pos, target)` | 移动相机 |
| `setCameraFollow(id)` | 相机跟随某个模型 |

这些指令通过 `ScriptCommand` 体系注册，与现有 `showImage`、`playAudio` 等指令同级，不需要新架构。

---

## 不做的范围

- **不修改核心框架** — 不碰 `GameHost`、`RenderPipeline`、`SceneStack`、`Main`
- **不引入 PBR/HDR/阴影映射** — libGDX 的 Blinn-Phong 管线够用，不做 AAA 级渲染
- **不引入物理引擎** — 不依赖 `gdx-bullet`，初期不做碰撞检测和刚体物理
- **不修改 2D 页面工作流** — 没有 `3d.json` 的页面完全不受影响

---

## 开放问题

1. **ModelBatch 生命周期管理:** GamePlay 每次进入 3D page 创建 ModelBatch？还是全局保持一个？建议全局保持一个（G3dManager 持有），page 切换时只换场景内容，不重建渲染上下文。
2. **3D 输入处理:** 自由相机需要鼠标拖拽/滚轮缩放，与现有 Stage 事件如何协调？初步思路: 通过 `behavior.json` 的 trigger 机制处理，不加全局输入拦截。
3. **模型资产管线:** 团队是否熟悉 `fbx-conv` 工作流？是否需要提供批量转换脚本？
4. **性能预算:** 移动端（Android）模型面数建议控制在多少？需要实际测试后确定。
