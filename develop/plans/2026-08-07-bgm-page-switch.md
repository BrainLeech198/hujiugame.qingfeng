# 页面切换 BGM 自动切换 — 设计方案

> **状态:** 方案已确认（2026-08-07），待实现。
>
> 背景：页面切换时新旧页面背景音乐列表不一致，当前 `AudioManager.playLayout` 只"补播"不"换播"，会导致旧页面 BGM 未停、新页面 BGM 叠加的双播问题。

---

## 背景与问题

页面切换统一走 `SceneStack.updateGameState()`（`pushGameState`/`popGameState`/`setGameState`/`resetGameState` 四个方法最终都调用它），切换后新场景 `render()` 每帧调 `audioManager.playLayout(新页面 layout)`。

`playLayout` 现状（`AudioManager.java:1132`）：

- 只检查**新页面自己的 BGM 列表**中是否有曲目已在播放记录 `bgMusicPlayingObjectMap`（用于规避 Android `Music.isPlaying()` 不可靠，2026-07-29 引入）——查的是新列表的 tag，旧页面在播的 BGM tag 不在其中，查不到，走"随机选一首播"。
- `playBackgroundMusic` 只停止**同 tag** 的旧播放源（`AudioManager.java:943-950`），不会停止其他 tag 的 BGM。

因此新旧页面 BGM 无交集时，切页后旧 BGM 继续响、新 BGM 又开播，形成双播。当前启动器 4 个页面（menu_main/menu_list/menu_load/config_basic/config_display）的 `layout.json` BGM 列表完全相同，尚未实际暴露；但用户自定义主题或游戏内页面（`GamePlay` 用独立 `gameAudioManager`，来自 `PlayLocalData`）配置不同列表时必然触发。

## 需求决策（用户确认）

| 方面 | 决策 |
|------|------|
| 切换语义 | 新旧 BGM 无交集 → **立即停旧播新** |
| 共享曲目 | 新旧列表有交集 → 共享曲目**保留继续播**，不打断；仅停非共享旧曲目 |
| 空列表 | 新页面无 BGM 配置 → **静音**（清空当前播放记录） |
| 修复范围 | **统一处理**：改 `playLayout` 一处，启动器与游戏内两个 AudioManager 实例各自生效 |

## 方案（方案 A：`playLayout` 内播放记录同步）

### 改动点

仅 `AudioManager.java` 的 `playLayout` 方法，在原有"containsKey 检查 + 随机播"之前插入清理段：

```java
List<String> bgmList = layout.getBackgroundMusicList();

// 同步播放记录到当前页面列表：
// 页面切换后旧页面 BGM 不在新列表，先停止并销毁，避免与新 BGM 双播；
// 新页面无 BGM（空列表）时全部清理，保持静音。
List<String> toDispose = new ArrayList<>();
synchronized (bgMusicPlayingObjectMap)
{
    for (String tag : bgMusicPlayingObjectMap.keySet())
    {
        if (bgmList == null || !bgmList.contains(tag))
        {
            toDispose.add(tag);
        }
    }
}
for (String tag : toDispose)
{
    disposeBackgroundMusic(tag);
}
```

需要新增 `import java.util.ArrayList;`。

### 边界行为映射

| 场景 | 行为 |
|------|------|
| 新旧列表无交集 | 旧 tag 被 `disposeBackgroundMusic` 清出四表 → 新列表无在播记录 → 随机播新曲（立即停旧播新） |
| 新旧列表有交集 | 共享 tag 不在清理范围 → 保留继续播；非共享旧 tag 被停（切换语义 + 共享曲目两条决策同时满足） |
| 新页面空列表（`bgmList == null` 或空） | 所有 tag 被 dispose → 静音 |
| 启动器 + 游戏内 | 两个 AudioManager 实例各自 `playLayout`，各自清理播放记录，行为一致 |

### 自洽性验证

- **重新加载链路**：`disposeBackgroundMusic` 清 loaded+playing 全部四表并加入销毁队列（`AudioManager.java:1182`）。切回该页面时 `LayoutManager` 加载 layout 时调 `loadBackgroundMusic` 重新进 loaded 表（`LayoutManager.java:176`），`loadBackgroundMusic` 的"同文件跳过"优化（`:832`）在 dispose 后不生效、会正常重载。
- **自然播完 listener 安全**：`setOnCompletionListener` 内 `bgMusicPlayingObjectMap.get(tag) == m` 引用检查（`:864`），dispose 后 get 返回 null ≠ m，不会误删重载后的新实例。
- **每帧幂等**：清理一次后播放记录只剩新列表的 tag（或空），后续帧不再触发清理。
- **并发安全**：先在该表锁内收集 `toDispose` 列表，退出锁再逐个 `disposeBackgroundMusic`（其内部自会上锁），避免嵌套锁；synchronized 可重入，即便嵌套也无死锁。
- **与历史修复兼容**：未回退 2026-07-29 的 containsKey 判断、不破坏 2026-08-05 的 stopAll 语义。

## 注意事项

- 清理在 render 帧内执行，切页后旧 BGM 最多多响一帧（~16ms）才被停，可接受；不在 SceneStack 通知点处理，避免动核心流程。
- `bgmList == null` 时按"空列表静音"处理（`contains` 前的空指针防护）。
- 清理遍历的是 `bgMusicPlayingObjectMap.keySet()`，tag 即文件名（`Layout.getBackgroundMusicList()` 元素），与 load/play 使用的 tag 一致。

## 实施步骤

1. `AudioManager.java`：新增 `import java.util.ArrayList;` + 修改 `playLayout`（插入清理段）。
2. 编译验证（`./gradlew :core:compileJava` 或对应任务）。
3. `develop/CHANGELOG.md` 新增修复条目（随 AudioManager 提交）。
4. `DOCUMENTATION_INDEX.md` 新增本文档索引条目。
5. 按提交偏好拆分提交：设计文档（+索引）/ AudioManager（+CHANGELOG 修复条目）。
