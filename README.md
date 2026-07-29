# QingFeng Launcher

**简体中文 · [正體中文](#section-zh-tw) · [English](#section-en)**

---

**简体中文**

<a id="section-zh-cn"></a>

# 氢风启动器

一款以 [libGDX](https://libgdx.com/) 构建的视觉小说游戏启动器，遵循开发团队统一规范的标准化文件结构。

本仓库包含**氢风启动器的完整源代码**，以及**官方网站**（GitHub Pages）与**发布包**。

---

## 功能特性

- **扩展游戏内容**  
  在传统对话式叙事之外，核心引擎支持 2D 地图探索、视频播放等扩展功能，为创作者提供更丰富的表达手段。

- **创作者工具**  
  内置可视化编辑器等故事开发工具，帮助快速构建游戏叙事与逻辑。启动器同时支持多语言文本配置，创作者可为目标受众添加所需语言。

- **编辑器内实时编辑**  
  内置的可视化编辑器支持实时编辑故事内容、角色对话、分支选项与地图场景，无需额外工具。创作者可在编辑器内即时预览更改。

- **游戏分享**  
  一键导出并分享所创建的游戏为 `.qfg` 包文件。这些游戏包可直接被氢风启动器加载运行，方便将作品分享给其他玩家或发布到社区。

- **界面定制**  
  启动器本身支持多套主题，用户可根据喜好选择不同外观。同时还提供多语言界面配置，面向不同区域的用户。

---

## 平台生态

氢风启动器不仅是视觉小说引擎，更是一个**编辑器+启动器一体化**的叙事游戏创作与分享平台。目标是像 Roblox、RPG Maker、Flash 那样，让创作者在一个闭环生态中完成创作、分发、游玩的全流程。

### 为什么是平台

- **玩家零门槛**：一个启动器玩所有作品
- **创作者零负担**：无需处理打包、跨平台分发、版本兼容
- **引擎开发者可控**：版本升级对所有作品透明，无需创作者操作
- **生态可扩展**：可内置社区、打赏、素材商店、排行榜等增值功能

### 目标用户

**创作者**：不会编程但想讲故事的视觉小说作者、想快速原型化叙事体验的独立游戏开发者、教育场景中需要用互动叙事表达的非技术用户。

**玩家**：喜欢视觉小说/互动叙事的读者、想体验社区创作的新鲜内容的猎奇用户、特定圈子（同人、乙女、BL、百合等）的内容消费者。

### 用户全流程

```
创作者                         玩家
   │                             │
   ├─ 编辑器制作故事              │
   │  · 剧本/对话/分支            │
   │  · 立绘/背景/音乐            │
   │  · 2D地图/轻量解谜           │
   │                             │
   ├─ 导出为 .qfg 包 ──────────────→ 双击 .qfg 或拖入启动器
   │                             ├─ 本地游玩
   │                             ├─ 从社区浏览/下载作品
   │                             │
   ├─ 提交到社区 ────────────────→ 人工审核
   │                             ├─ 作品库浏览
   │                             └─ 评分/评论/收藏
   │
   └─ 更新作品 → 玩家自动收到更新提示
```

### `.qfg` 包格式

作品通过 `.qfg` 包分发，内含作品元数据（ID、作者、版本、引擎兼容性等），启动器自动读取并加载。

### 版权保护

策略是防君子不防小人。平台通过用户协议、上传承诺勾选和举报系统建立秩序，对上架作品进行来源核验，避免搬运与冒名。结合作品血缘追溯机制，让二次创作自动关联原作。

### 社区架构（规划中）

采用云服务快速搭建：

- 后端采用托管数据库，处理用户认证、作品元数据、评论与评分
- 云存储存放 `.qfg` 包、封面图与预览图
- 前端部署为静态页面，展示社区作品库与用户主页
- 接入 AI 内容安全服务，实现文字与图片自动审查

**审核流程**：用户提交 → 自动合规检查 → 通过则发布 / 可疑则人工复审 / 违规则驳回。

**过渡策略**：初期手动接收 → 中期半自动化审核 → 远期创作者自治（高信誉用户免审直发）。

### 商业与激励模型

| 阶段 | 方式 | 说明 |
|------|------|------|
| 起步期 | 免费 | 积累用户和创作者，打磨体验 |
| 成长期 | 打赏 | 玩家可向创作者打赏，平台抽成 10-20% |
| 成熟期 | 素材商店 | 官方/第三方提供付费素材包（立绘、背景、音乐） |
| 生态期 | 会员订阅 | 去广告、优先审核、高级统计等增值服务 |

---

## 路线图

- **AI 大模型 API 集成**（计划中）  
  集成大语言模型 API（如 OpenAI、Anthropic、本地模型等）支持，实现 AI 驱动的对话生成、智能 NPC 行为与自动故事分支，帮助创作者以更少的工作量构建更丰富的叙事体验。

### 引擎原子能力增强

| 优先级 | 能力 | 说明 |
|--------|------|------|
| P0 | 变量系统 | 全局/局部变量，数值/布尔/字符串类型 |
| P0 | 条件分支 | 根据变量值选择不同对话/事件走向 |
| P1 | 好感度系统 | 角色好感数值及对应的分支解锁 |
| P1 | 立绘动画 | 立绘切换动效、Live2D 支持 |
| P1 | 点击事件 | 场景中可点击区域触发对话或事件 |
| P2 | 轻量解谜 | 物品收集、密码锁等简单交互 |

---

## 下载与发布

最新版本（Windows `.exe`、Android `.apk`）可在**[官方网站](https://brainleech198.github.io/hujiugame-qingfeng/)**与 **[GitHub Releases](https://github.com/BrainLeech198/hujiugame-qingfeng/releases)** 页面获取。

历史版本及更新日志亦可在官方网站查阅。

---

## 平台

本 libGDX 项目按标准平台模块组织：

- `core`：主模块，包含所有平台共享的应用逻辑
- `lwjgl3`：桌面端主平台，使用 LWJGL3（早期文档中称为 `desktop`）
- `android`：Android 移动平台，需要 Android SDK

---

## 从源码构建

项目使用 [Gradle](https://gradle.org/) 管理依赖。  
内含 Gradle Wrapper，可通过 `gradlew.bat`（Windows）或 `./gradlew`（macOS/Linux）运行任务。

### 常用 Gradle 任务

| 任务/标志                        | 说明                                   |
|------------------------------|--------------------------------------|
| `--continue`                 | 即使某个任务失败也继续执行                        |
| `--daemon`                   | 使用 Gradle 守护进程以加快构建速度                |
| `--offline`                  | 仅使用已缓存的依赖                            |
| `--refresh-dependencies`     | 强制重新验证依赖（对快照版本有用）                    |
| `android:lint`               | 运行 Android lint 检查                   |
| `build`                      | 构建整个项目                               |
| `clean`                      | 清除所有构建目录                             |
| `cleanEclipse` / `cleanIdea` | 清除 Eclipse / IntelliJ 项目文件           |
| `eclipse` / `idea`           | 生成 Eclipse / IntelliJ 项目文件           |
| `lwjgl3:jar`                 | 构建桌面可运行 JAR（输出在 `lwjgl3/build/libs`） |
| `lwjgl3:run`                 | 启动桌面应用程序                             |
| `test`                       | 运行单元测试                               |

> 若需针对特定模块，请在任务前加上模块 ID，例如 `core:clean`。

---

## 网站

官方网站通过 **GitHub Pages** 托管，内容来自本仓库的 `docs/` 目录。  
访问地址：**[https://brainleech198.github.io/hujiugame-qingfeng/](https://brainleech198.github.io/hujiugame-qingfeng/)**

---

## 文档

所有项目文档（贡献指南、代码规范、提交规范等）统一收录于 **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)**，按读者角色分层组织，方便快速定位。

## 反馈与贡献

- 问题报告与功能请求请使用 **[GitHub Issues](https://github.com/BrainLeech198/hujiugame-qingfeng/issues)**。
- 也欢迎在官方网站上进行讨论与社区反馈。

---

## 许可证

本项目基于 **MIT 许可证** 授权发布。详情见 [LICENSE](LICENSE) 文件。

---

**氢风启动器 —— 以灵活的多平台启动器赋能视觉小说创作者。**

---

**正體中文**

<a id="section-zh-tw"></a>

# 氫風啟動器

一款以 [libGDX](https://libgdx.com/) 建構的視覺小說遊戲啟動器，遵循開發團隊統一規範的標準化檔案結構。

本倉庫包含**氫風啟動器的完整原始碼**，以及**官方網站**（GitHub Pages）與**發佈套件**。

---

## 功能特性

- **擴展遊戲內容**  
  在傳統對話式敘事之外，核心引擎支援 2D 地圖探索、影片播放等擴展功能，為創作者提供更豐富的表現手段。

- **創作者工具**  
  內建視覺化編輯器等故事開發工具，協助快速建構遊戲敘事與邏輯。啟動器同時支援多語言文字配置，創作者可為目標受眾添加所需語言。

- **編輯器內即時編輯**  
  內建的視覺化編輯器支援即時編輯故事內容、角色對話、分支選項與地圖場景，無需額外工具。創作者可在編輯器內即時預覽變更。

- **遊戲分享**  
  一鍵匯出並分享所建立的遊戲為 `.qfg` 套件檔案。這些遊戲套件可直接被氫風啟動器載入執行，方便將作品分享給其他玩家或發佈到社群。

- **介面自訂**  
  啟動器本身支援多套主題，使用者可依喜好選擇不同外觀。同時還提供多語言介面設定，面向不同區域的使用者。

---

## 平台生態

氫風啟動器不僅是視覺小說引擎，更是一個**編輯器+啟動器一體化**的敘事遊戲創作與分享平台。目標是像 Roblox、RPG Maker、Flash 那樣，讓創作者在一個封閉生態中完成創作、分發、遊玩的全流程。

### 平台模式優勢

- **玩家零門檻**：一個啟動器玩所有作品
- **創作者零負擔**：無需處理打包、跨平台分發、版本相容
- **引擎開發者可控**：版本升級對所有作品透明，無需創作者操作
- **生態可擴展**：可內建社群、打賞、素材商店、排行榜等增值功能

### 用戶全流程

```
創作者 → 編輯器製作故事 → 匯出 .qfg → 提交社群（審核）
玩家   → 下載啟動器 → 遊玩 .qfg → 評分/評論/收藏
```

### `.qfg` 套件格式

作品透過 `.qfg` 套件分發，內含作品元資料（ID、作者、版本、引擎相容性等），啟動器自動讀取並載入。

---

## 路線圖

- **AI 大模型 API 整合**（計劃中）  
  整合大型語言模型 API（如 OpenAI、Anthropic、本地模型等）支援，實現 AI 驅動的對話生成、智慧 NPC 行為與自動故事分支，協助創作者以更少的工作量建構更豐富的敘事體驗。

### 引擎原子能力增强

| 優先級 | 能力 | 說明 |
|--------|------|------|
| P0 | 變數系統 | 全域/區域變數，數值/布林/字串 |
| P0 | 條件分支 | 根據變數值選擇對話/事件走向 |
| P1 | 好感度系統 | 角色好感數值及分支解鎖 |
| P1 | 立繪動畫 | 立繪動效、Live2D 支援 |
| P1 | 點擊事件 | 場景中可點擊區域觸發事件 |
| P2 | 輕量解謎 | 物品收集、密碼鎖等互動 |

---

## 下載與發佈

最新版本（Windows `.exe`、Android `.apk`）可在**[官方網站](https://brainleech198.github.io/hujiugame-qingfeng/)**與 **[GitHub Releases](https://github.com/BrainLeech198/hujiugame-qingfeng/releases)** 頁面取得。

歷史版本及更新日誌亦可在官方網站查閱。

---

## 平台

本 libGDX 專案依標準平台模組組織：

- `core`：主模組，包含所有平台共享的應用程式邏輯
- `lwjgl3`：桌面端主平台，使用 LWJGL3（早期文件中稱為 `desktop`）
- `android`：Android 行動平台，需要 Android SDK

---

## 從原始碼建構

專案使用 [Gradle](https://gradle.org/) 管理相依性。  
內含 Gradle Wrapper，可透過 `gradlew.bat`（Windows）或 `./gradlew`（macOS/Linux）執行任務。

### 常用 Gradle 任務

| 任務/標誌                        | 說明                                   |
|------------------------------|--------------------------------------|
| `--continue`                 | 即使某個任務失敗也繼續執行                        |
| `--daemon`                   | 使用 Gradle 守護程序以加快建構速度                |
| `--offline`                  | 僅使用已快取的相依性                           |
| `--refresh-dependencies`     | 強制重新驗證相依性（對快照版本有用）                   |
| `android:lint`               | 執行 Android lint 檢查                   |
| `build`                      | 建構整個專案                               |
| `clean`                      | 清除所有建構目錄                             |
| `cleanEclipse` / `cleanIdea` | 清除 Eclipse / IntelliJ 專案檔案           |
| `eclipse` / `idea`           | 產生 Eclipse / IntelliJ 專案檔案           |
| `lwjgl3:jar`                 | 建構桌面可執行 JAR（輸出在 `lwjgl3/build/libs`） |
| `lwjgl3:run`                 | 啟動桌面應用程式                             |
| `test`                       | 執行單元測試                               |

> 若需針對特定模組，請在任務前加上模組 ID，例如 `core:clean`。

---

## 網站

官方網站透過 **GitHub Pages** 託管，內容來自本倉庫的 `docs/` 目錄。  
訪問位址：**[https://brainleech198.github.io/hujiugame-qingfeng/](https://brainleech198.github.io/hujiugame-qingfeng/)**

---

## 文档

所有项目文档（贡献指南、代码规范、提交规范等）统一收录于 **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)**，按读者角色分层组织，方便快速定位。

## 反馈與貢獻

- 問題回報與功能請求請使用 **[GitHub Issues](https://github.com/BrainLeech198/hujiugame-qingfeng/issues)**。
- 也歡迎在官方網站上進行討論與社群回饋。

---

## 授權條款

本專案基於 **MIT 授權條款** 發佈。詳情見 [LICENSE](LICENSE) 檔案。

---

**氫風啟動器 —— 以靈活的多平台啟動器賦能視覺小說創作者。**

---

**English**

<a id="section-en"></a>

# QingFeng Launcher

A visual novel game launcher built with [libGDX](https://libgdx.com/), following the standardized file structure defined by our development team.

This repository contains the **full source code** of QingFeng Launcher, as well as the **official website** (GitHub Pages) and **release packages**.

---

## Features

- **Extended Game Content**  
  Beyond traditional dialogue-based storytelling, the core engine supports 2D map exploration, video playback, and other expansion features, providing creators with richer means of expression.

- **Creator Tools**  
  Built-in story development tools, including a visual editor, enable rapid construction of game narratives and logic. The launcher also supports multilingual text configuration, allowing creators to add desired languages for their target audiences.

- **In-Editor Game Editing**  
  Built-in visual editor supporting real-time editing of story content, character dialogues, branching choices, and map scenes — no external tools required. Creators can preview changes instantly within the editor.

- **Game Sharing**  
  One-click export and sharing of created games as `.qfg` package files. These game packages can be loaded and played directly by QingFeng Launcher, making it easy to share works with other players or publish them to the community.

- **Interface Customization**  
  The launcher itself supports multiple themes, allowing users to choose different appearances based on preference. It also provides multilingual interface configuration to accommodate users from different regions.

---

## Ecosystem

QingFeng Launcher is more than a visual novel engine — it's an **integrated editor + launcher platform** for narrative game creation and sharing. Like Roblox, RPG Maker, or Flash, the goal is a closed-loop ecosystem where creators can create, distribute, and play in one seamless flow.

### Why a Platform

| Platform | Model | Insight |
|----------|-------|---------|
| **Roblox** | Create + play integrated, platform provides engine and distribution | Closed-loop ecosystem scales |
| **RPG Maker** | Specialized creation tool, community sharing | Tools + community is the core combo |
| **Flash** | Lightweight creation, countless games/animations | Low barrier unleashes massive creativity |

**Platform advantages**: Zero threshold for players (one launcher for all works), zero distribution burden for creators, transparent engine updates, and extensible ecosystem (tipping, asset store, leaderboards).

### User Flow

```
Creator                           Player
  │                                 │
  ├─ Editor: craft story            │
  ├─ Export as .qfg ────────────────→ Double-click or drag into launcher
  │                                 ├─ Play locally
  ├─ Submit to community ──────────→ manual review
  │                                 ├─ Browse & download works
  │                                 └─ Rate, comment, collect
  │
  └─ Update work → Players receive auto-update notice
```

### `.qfg` Package Format

Works are distributed as `.qfg` packages containing work metadata (ID, author, version, engine compatibility, etc.), which the launcher reads and loads automatically.

### Copyright Protection

"Prevent honest mistakes, not determined theft." The platform uses terms of service, upload pledges, and a reporting system to prevent plagiarism and impersonation. A work provenance tracking mechanism ensures derivative works are properly attributed to their originals.

### Community Architecture (Planned)

Built with cloud services:

- Managed database for auth, work metadata, comments, and ratings
- Cloud storage for `.qfg` packages, covers, and screenshots
- Static frontend for community pages, work details, and user profiles
- AI content safety API for text/image auto-moderation

**Review flow**: Submit → auto compliance check → pass (publish) / suspicious (manual review) / violation (auto reject).

**Transition strategy**: Manual submissions (initial) → semi-automated review → full creator autonomy (trusted creators auto-publish).

### Business Model

| Stage | Model | Description |
|-------|-------|-------------|
| Launch | Free | Build user and creator base |
| Growth | Tipping | Players tip creators, platform takes 10-20% |
| Maturity | Asset Store | Official/third-party paid asset packs |
| Ecosystem | Subscription | Ad-free, priority review, advanced analytics |

---

## Roadmap

- **AI Large Model API Integration** (Planned)  
  Integrating support for large language model APIs (e.g., OpenAI, Anthropic, local models) to enable AI-driven dialogue generation, intelligent NPC behavior, and automated story branching — helping creators build richer narrative experiences with less manual effort.

### Engine Enhancements

| Priority | Feature | Description |
|----------|---------|-------------|
| P0 | Variable System | Global/local variables: numeric, boolean, string |
| P0 | Conditional Branching | Variable-based dialogue/event routing |
| P1 | Affection System | Character affinity values with branch unlocks |
| P1 | Sprite Animation | Transition effects, Live2D support |
| P1 | Click Events | Interactive hotspots in scenes |
| P2 | Light Puzzles | Item collection, lock puzzles |

---

## Download & Releases

The latest versions (Windows `.exe`, Android `.apk`) are available on the **[Official Website](https://brainleech198.github.io/hujiugame-qingfeng/)** and the **[GitHub Releases](https://github.com/BrainLeech198/hujiugame-qingfeng/releases)** page.

Historical versions and update logs can also be found on the official website.

---

## Platforms

This libGDX project is organized into standard platform modules:

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3 (was called `desktop` in older docs).
- `android`: Android mobile platform. Needs Android SDK.

---

## Building from Source

The project uses [Gradle](https://gradle.org/) to manage dependencies.  
The Gradle wrapper is included, so you can run tasks using `gradlew.bat` (Windows) or `./gradlew` (macOS/Linux).

### Common Gradle tasks

| Task / flag                | Description                                                                 |
|----------------------------|-----------------------------------------------------------------------------|
| `--continue`               | Continue execution even if a task fails                                    |
| `--daemon`                 | Use the Gradle daemon for faster builds                                    |
| `--offline`                | Use cached dependencies only                                               |
| `--refresh-dependencies`   | Force re-validation of dependencies (useful for snapshots)                  |
| `android:lint`             | Run Android lint checks                                                    |
| `build`                    | Build the entire project                                                   |
| `clean`                    | Remove all build directories                                               |
| `cleanEclipse` / `cleanIdea` | Remove Eclipse / IntelliJ project files                                   |
| `eclipse` / `idea`         | Generate Eclipse / IntelliJ project files                                   |
| `lwjgl3:jar`               | Build the desktop runnable JAR (output in `lwjgl3/build/libs`)              |
| `lwjgl3:run`               | Launch the desktop application                                              |
| `test`                     | Run unit tests                                                              |

> To target a specific module, prefix the task with the module ID, e.g., `core:clean`.

---

## Website

The official website is hosted with **GitHub Pages** from the `docs/` directory of this repository.  
Visit it at: **[https://brainleech198.github.io/hujiugame-qingfeng/](https://brainleech198.github.io/hujiugame-qingfeng/)**

---

## Docs

All project documentation (contributing guide, code style, commit conventions, etc.) is indexed in **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)**, organized by reader role for quick reference.

## Feedback & Contributions

- For bug reports and feature requests, use **[GitHub Issues](https://github.com/BrainLeech198/hujiugame-qingfeng/issues)**.
- Discussions and community feedback are also welcome on the official website.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

**QingFeng Launcher – Empowering visual novel creators with a flexible, multi-platform launcher.**
