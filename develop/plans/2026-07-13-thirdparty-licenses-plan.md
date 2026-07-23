# 第三方素材版权声明 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在项目中建立 `THIRDPARTY_LICENSES.md` 文件，作为第三方素材版权声明的法律底线保障。

**Architecture:** 单 Markdown 文件，流水号条目，随发行包分发。

**Tech Stack:** Markdown

**前置条件:** 设计文档已写入 `develop/specs/2026-07-13-thirdparty-licenses-design.md`，模板文件 `assets/THIRDPARTY_LICENSES.md` 已创建（含一个空条目示例）。

---

### Task 1: 提交设计文档 + 模板到 git

**Files:**
- Create: `develop/specs/2026-07-13-thirdparty-licenses-design.md`
- Create: `assets/THIRDPARTY_LICENSES.md`

- [ ] **Step 1: 在 CHANGELOG 中记录**

在 `develop/CHANGELOG.md` 添加条目。

- [ ] **Step 2: 更新 DOCUMENTATION_INDEX.md**

在文档索引中新增 `develop/specs/` 和 `assets/THIRDPARTY_LICENSES.md` 的引用。

- [ ] **Step 3: 暂存文件**

```bash
git add develop/specs/2026-07-13-thirdparty-licenses-design.md
git add assets/THIRDPARTY_LICENSES.md
git add develop/CHANGELOG.md
git add DOCUMENTATION_INDEX.md
```

- [ ] **Step 4: 提交**

```bash
git commit -m "feat(asset): 新增第三方素材版权声明框架（THIRDPARTY_LICENSES.md）"
```

---

### Task 2: 后续 — 填充素材条目

**前置条件:** 用户确认哪些素材是第三方需署名的，并提供作者/来源/许可信息。

**Files:**
- Modify: `assets/THIRDPARTY_LICENSES.md`

**操作:**
- 将模板中的占位条目替换为实际条目
- 每确定一个素材，按模板格式追加一条
- 条目之间用 `---` 分隔
- 如需修改（裁切/调色等），在"说明"字段注明

**识别范围参考:**
| 路径 | 类型 | 需确认 |
|------|------|--------|
| `resource/image/app_init.png` | 图片 | 是否第三方 |
| `resource/image/app_repair.png` | 图片 | 是否第三方 |
| `resource/image/controller_*.png` | 图片 | 是否第三方 |
| `resource/image/keyboard_*.png` | 图片 | 是否第三方 |
| `resource/image/error.png` | 图片 | 是否第三方 |
| `resource/image/virtual_*.png` | 图片 | 是否第三方 |
| `theme/.../audio/menu.mp3` | 音频 | 是否第三方 |
| `theme/.../audio/button/*.ogg` | 音频 | 是否第三方 |
| `theme/.../audio/message_box/*.ogg` | 音频 | 是否第三方 |
| `theme/.../font/Source_Han_Sans/` | 字体 | 是否第三方（OFL 协议？） |
| `theme/.../image/menu.*.png` | 图片 | 是否第三方 |
| `theme/.../image/button/*.png` | 图片 | 是否第三方 |
