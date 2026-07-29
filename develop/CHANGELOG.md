# 更新日志

> **文档定位**：项目变更日志，按时间倒序记录每次提交的变更内容。
>
> **文档结构**：
> - 按日期倒序排列，每个日期一个条目
> - 每个日期条目标题格式：`日期 — 核心主题1 + 核心主题2 + ...`
> - 条目内段落按 `新增 → 功能 → 变更 → 重构 → 修复 → 资产 → 文档 → 构建 → 网站 → 类型 → 移除 → 编码规范 → 优化 → 其他` 顺序排列
> - 同一日期多条独立变更用 `---` 分隔
>
> **更新规范**：
> 1. 【必须】每次提交前更新本文档，新条目插在最前面
> 2. 【必须】遵循上述文档结构的格式要求
> 3. 【如果】新增/修改 JSON 配置格式 → 同步更新 `develop/JSON_STANDARD.md`
> 4. 【如果】修改脚本指令/值系统 → 同步更新 `develop/SCRIPT_INTERNAL_STANDARD.md`
> 5. 【如果】新增/重命名/删除 `.md` 文件 → 同步更新 `DOCUMENTATION_INDEX.md`
> 6. 【如果】新建设计方案文档 → 建议在 `develop/plans/` 目录记录

## 2026-07-29 — 文档更新规范自描述 + 主题版权自动生成方案

### 文档

- **所有文档头部新增自描述规范** — develop/ 下 8 份文档（CHANGELOG.md、JSON_STANDARD.md、SCRIPT_INTERNAL_STANDARD.md、DOCUMENTATION_INDEX.md、COMMIT_STYLE.md、CODING_STYLE.md、THIRDPARTY_LICENSES_STANDARD.md、output/README.md）各自头部补充三区块：**文档定位**（职责范围）、**文档结构**（编排顺序和格式要求）、**更新规范**（变更时需遵循的规则和同步更新指引）
- **CLAUDE.md 新增启动必读指令** — 顶部添加 `> **启动必读**`，指令新会话首次回复前先读取 `temp/CLAUDE_MEMORY.md` 恢复历史上下文
- **CLAUDE.md 文档维护章节更新** — 移除已删除的 `develop/REVIEW.md` 引用，补充 `develop/plans/` 设计方案目录、CONTRIBUTING.md、docs/README.md、develop/output/README.md 等多份文档的维护提醒，新增"各文档头部自描述规范"说明；新增"本地工作记忆"章节引用 `temp/CLAUDE_MEMORY.md`
- **`temp/CLAUDE_MEMORY.md` 头部补充自描述规范** — 按统一三区块格式（文档定位/文档结构/更新规范）补充头部，gitignored 文件纳入本地工作记忆管理体系
- **CHANGELOG.md 头部检查项补充** — 新增 `develop/CHANGELOG.md` 自身（每次提交必须更新）和 `develop/plans/` 目录（新建设计方案时建议记录）
- **`develop/plans/2026-07-29-theme-copyright-generator.md`** — 主题第三方版权声明自动生成方案，声明清单 JSON + 运行时生成器模式，含许可模板库、校验告警、增量维护策略
- **`DOCUMENTATION_INDEX.md`** — 新增主题版权自动生成方案条目

## 2026-07-26 — 用户配置上载 + GameInfoKey 内类化 + 配置界面语言 + 手柄虚拟控制重写

### 新增

- **`UserConfigManager.uploadTo(GameInfoManager)`** — 将用户配置（语言、主题、视窗、全屏、分辨率、音量）统一上载至运行时 `GameInfoManager`
- **`LanguageManager.uploadTo` / `ThemeManager.uploadTo`** — 语言/主题管理器新增同名上载方法，将语言名称和主题名称写入 `GameInfoManager`
- **`ConfigDisplay` 子页面** — 新增显示配置场景（新建 `ConfigDisplay.java`），注册 `GameSubState.CONFIG_DISPLAY = 1`，`GameStatePageInfo` 映射至 `config_display` 布局
- **`ConfigBasic` 语言切换项** — 新增 `refreshItems()` 方法，根据 `itemSelectStateMap` 切换语言标签/语言选中标签的显示；`RequirementKey.Ui` 新增 `CONFIG_BASIC_LANGUAGE` / `CONFIG_BASIC_LANGUAGE_SELECTED`
- **`RequirementKey.Config` 优先级 UI 常量** — 新增 `UNIVERSAL_PRIORITY_CONFIRM_UI` 系列常量，为后续配置驱动的优先级选中做准备
- **语言文件补充** — 三语言 `requirement.json` 新增 `config.basic` 区块（`back` / `language`），旧 `resolution` 字段移至 `config.display`
- **手柄模式轮换** — `ControllerInputHandler` 移除 X/Y 开/关虚拟鼠标，改为 START 键循环：`NONE → CONTROLLER_SELECT → CONTROLLER_VIRTUAL_MOUSE → NONE`
- **虚拟选择框优先级对象** — `VirtualInputHandler` 新增 `setPrioritySelectObject(InteractableObject)`，在 `tryToKeepSameSelectObject` 失败时自动选中该优先级对象
- **取消选择保留** — `refreshSelectObject()` 拆分为 confirm/cancel 两段管线，取消框同确认框一样在页面刷新时保留上次选中对象

### 重构

- **`GameInfoKey` 内类化** — 29 个平铺常量重组为 6 个嵌套内部类（`Launcher`/`User`/`GameList`/`Game`/`Play`），`User` 含 `Resolution`/`SoundVolume` 子类，`Play` 含 `TreeStructure` 子类
- **`GameUserConfigLoader` 清理** — 删除错位的 `putInfo(USER_LANGUAGE/USER_THEME)` 调用
- **`InstanceContent` 提取 `registerRenderRegistry()`** — 将内联的渲染注册表构建逻辑提取为独立静态方法
- **`refreshSelectObject()` 管线拆分** — 确认框和取消框的逻辑分离为独立步骤，提高可维护性
- **`ControllerInputHandler` DPAD 行为** — 方向键不再自动进入 `CONTROLLER_SELECT` 模式，仅当前已在该模式时执行方向移动
- **`VirtualInputHandler.prioritySelectObject` 消耗型** — 成功选中优先对象后立即置 null，确保仅一次生效，防止后续页面刷新重复选中

### 修复

- **`uploadTo` 调用时机** — 从 `InstanceContent.init()` 移至 `Init.initUserConfig()` 中 `gameResolver.load()` 之后，避免 `UserConfigManager` 未初始化就尝试上载导致 NPE
- **`ControllerInputHandler` 模式轮换超时** — 进入 `CONTROLLER_SELECT` 模式时立即调用 `resetVirtualSelectTime()`，防止计时器残余值导致选择框瞬间超时关闭

## 2026-07-25 — Story/Config/Version/游戏服务 常量收编 + TextManager/LogLevel 内部枚举 + 页面配置修复

### 新增

- **`StoryKey`** — Story 子系统 JSON 字段常量：`Tree` (BLOCK/TYPE/ID/IN/OUT/Type.ROOT/BRANCH/NODE/LEAF)、`Role` (ID/ROOT)、`PAGE`
- **`ConfigKey`** — 配置文件 JSON 字段常量：`Game` (ID/NAME/VERSION/LAUNCHER_VERSION)、`Content` (COUNT/ROLE/SCRIPTS/TEMPLATES)、`Log` (LOG_LEVEL/FILE_LOG_LEVEL)、`Directory` (DIRECTORY/FILE)
- **`VersionKey`** — 版本相关 JSON 字段常量：`APP_VERSION`/`APP_VERSION_TYPE`/`APP_VERSION_STRING`、`NEWEST_VERSION`/`NEWEST_VERSION_TYPE`/`NEWEST_VERSION_STRING`、`Update` (PROTECT/PROHIBIT)
- **`TextManager.Field` 内部枚举** — 文本模板域标识符 `LANGUAGE("language")` / `GAME("game")`，含 `getValue()` 和 `fromValue()` 方法
- **`LogLevel.Name` 内部枚举** — 日志等级字符串常量 `DEBUG`/`INFO`/`ERROR`，含 `getValue()` 方法

### 重构

- **常量收编** — 将 Story/Game/Config/Version/User 域所有散落的 JSON 字段名和日志标签替换为 `StoryKey`、`ConfigKey`、`VersionKey`、`ThemeKey` 常量引用。波及 15 文件：`TreeStructureInfo`、`Role`、`GameStoryManager`、`GameLogicService`、`GameRoleManager`、`GameScriptManager`、`GameTemplateManager`、`GameUserConfigManager`、`UserConfigManager`、`FileUtils`、`LogUtils`、`UpdateChecker`、`Main`、`Init`、`FileName`
- **`UserConfigKey` → `ConfigKey.User`** — 将 `UserConfigKey`（含 SoundVolume 内部类）收编为 `ConfigKey.User`，删除旧文件
- **`FileName` 补充** — 新增 `UPDATE_CONFIG = "update_config.json"`
- **`TextManager.parseBraceText` 枚举派发** — switch 语句从字符串比较改为 `Field.fromValue()` + 枚举分支
- **`LogLevel` 映射常量化** — `STRING_PARSE_LEVEL_MAP`/`LEVEL_DISPLAY_STRING_MAP` 的键值从硬编码字符串改为 `Name` 枚举引用
- **`LogUtils` 默认配置常量化** — 默认日志等级值从 `"INFO"`/`"DEBUG"` 改为 `LogLevel.Name.INFO.getValue()`/`LogLevel.Name.DEBUG.getValue()`

### 修复

- **`GameStatePageInfo.GAME_STATE_CONFIG_MAP` 缺失子页面配置** — MENU 区块只注册了 `MENU_MAIN`，切换到 `MENU_LIST`(subState=1) 或 `MENU_LOAD`(subState=2) 时连锁报错"未定义的子页面配置"+"获取页面配置失败 null值"。已补上两项并设为 `true`

### 提交分组合并说明

> **注意**：以下同一日期条目的变更因依赖关系合并提交，非逐文件独立提交：
> - ConfigKey + UserConfigKey 收编 + 波及 10 文件 → 1 提交
> - ScriptKey 常量体系 + 波及 25 文件 → 1 提交
> - StoryKey 常量体系 + 波及 3 文件 → 1 提交
> - VersionKey + 波及 2 文件 → 1 提交
> - 其他 Key 类新增 + 波及 20 文件 → 1 提交
> - TextManager.Field 内部枚举 → 1 提交
> - LogLevel.Name + LogUtils 常量化 → 1 提交
> - GameStatePageInfo 修复 → 1 提交

## 2026-07-24 — UniversalKey/GameStateLayout 重命名 + 配置键修复 + MessageBox 调整

### 新增

- **3D 场景支持预想方案** — `develop/plans/2026-07-24-3d-scene-support.md`，通过 page 目录 3d.json 实现可选 3D 场景，最小架构入侵

### 变更

- **`MessageBox` UI 参数调整** — 标题高度占比从 `100/600`→`120/600`，标题内容间距从 `5/600`→`10/600`，优化视觉效果

### 重构

- **`UniversalKey`→`UniversalUIKey` 重命名** — 类名从 `UniversalKey` 统一为 `UniversalUIKey`，明确其通用 UI 按键常量的职责；新增私有构造器防止工具类实例化。波及 5 文件：`UniversalInputHandlerFunction`、`VirtualInputHandler`、`ConfigBasic`、`GameRole`、`RequirementUiKey` 的旧引用全部同步更新
- **`GameStateLayout`→`GameStatePageInfo` 重命名** — 类名从 `GameStateLayout` 改为 `GameStatePageInfo`，更准确反映其页面信息映射的职责；新增私有构造器。`SceneStack` 中的旧引用同步更新

### 修复

- **`RequirementConfigKey.MENU_LIST_PAGE_MAX_GAME` 键值对齐** — 从 snake_case 的 `page_max_game` 修正为 camelCase 的 `pageMaxGame`，与实际 JSON 格式保持一致

### 资产

- **`menu_list/config.json`** — 新增菜单列表页面配置，包含 `pageMaxGame: 8`

### 文档

- **`DOCUMENTATION_INDEX.md`** — 添加 3D 场景支持预想方案条目

## 2026-07-23 — SceneStack 重构 + 配置加载 + 文件夹化 + 语言配置合并

### 新增

- **`GAME_STATE_CONFIG_MAP`** — `GameStateLayout` 新增映射表 `Map<Integer, Map<Integer, Boolean>>`，标记需要页面配置的状态（MENU_MAIN=true），其余为 null/false
- **`loadGameConfig()`** — `SceneStack` 新增私有方法，遵循 `loadGameLayout()` 模式：查映射 → 拼路径 → 加载 `config.json`，文件不存在则返回空 `JsonEntity`
- **`FileName` 常量** — 新增 `PAGE_LAYOUT`/`PAGE_CONFIG`/`IN_GAME_PAGE_LAYOUT`/`IN_GAME_PAGE_CONFIG`

### 功能

- **`MenuList.pageMaxGame` 从页面配置读取** — 原硬编码 8 改为从 `configJson` 的 `GAME_LIST_PAGE_MAX_GAME` 键读取，布局配置可控制每页游戏数量。同时补充选中项为空时隐藏 profile 按钮的遗漏逻辑

### 变更

- **`PathName` 常量重命名** — `ASSET_S_LAYOUT`→`ASSET_S_PAGE`，`IN_GAME_ASSET_S_LAYOUT`→`IN_GAME_ASSET_S_PAGE`
- **`GameStateLayout` 布局映射** — 去掉 `.json` 后缀（`"menu_main"` 而非 `"menu_main.json"`），适配文件夹化路径

### 重构

- **`SceneStack` 更新流程拆分** — `updateGameState()` 从单一方法拆分为三阶段：`loadGameLayout()` → `loadGameConfig()` → `updateGameRender(layout, configJson)`。`loadGameLayout()` 返回类型从 `boolean` 改为 `Layout`，对无需布局的状态（INIT）返回空 `Layout` 而非 `null`，消除空布局误判崩溃
- **`GameStateDataContainer`** — 新增 `configJson` 构造参数及 `getConfigJson()` 方法，config 作为独立数据公民传递
- **`LayoutConfig`→`Layout` 类重命名** — 类声明、构造器、toString 同步更新。波及 11 文件：`Layout.java`、`LayoutManager.java`、`AudioManager.java`、`GraphicsManager.java`、`UiManager.java`、`GamePlay.java`、`GameRole.java`、`Role.java`、`Page.java`、`GameTemplateManager.java`。全部参数/变量/注释同步更名

### 修复

- **`safeCrash` 安全崩溃包装** — `Main.java` 新增私有静态方法，先尝试 `CrashUtils.crash`，失败时退化为 `System.err` + `RuntimeException`，避免 CrashUtils 类加载失败导致原始崩溃信息丢失。替换全部 5 处 `CrashUtils.crash` 调用
- **UI 管理器 JSON 字段空值防护** — `ButtonManager.loadButtonKind` 的 `fontColor`、`ImageManager.loadImageKind` 的 `color`、`LabelManager.loadLabelKind` 的 `fontColor`/`backgroundColor` 在 `getString()` 返回值 null 时不再 NPE，改为 ERROR 日志后 `return false`
- **`JsonTextParser.parseFontColor` 空值防护** — `fontColor` 字段值类型非字符串时打 ERROR 日志，不再 `Color.valueOf(null)` NPE
- **`Init.loadProcessColor` 空值防护** — `process_color` 字段值类型非字符串时打 ERROR 日志，不再 NPE
- **三级 OpenGL 降级仅对 GL/GLFW 异常生效** — `Lwjgl3Launcher` 新增 `isGlCompatibilityError()` 判断，NPE 等游戏逻辑异常直接抛出，不再被降级流程掩盖
- **虚拟鼠标光标路径 `external`→`internal`** — `ControllerInputHandler` 的虚拟鼠标图片文件句柄从 `Gdx.files.external` 改为 `Gdx.files.internal`，修复打包后光标图片找不到的问题

### 资产

- **布局文件文件夹化** — 四个页面从扁平 JSON 迁移至 `page/页面名/layout.json` 结构：
  - `layout/config_basic.json` → `page/config_basic/layout.json`
  - `layout/menu_list.json` → `page/menu_list/layout.json`
  - `layout/menu_load.json` → `page/menu_load/layout.json`
  - `layout/menu_main.json` → `page/menu_main/layout.json`
- **语言配置合并** — 将 `main.json` 中的 UI 文本键（menu/config 块）平面合并入 `requirement.json`，`language.json` 默认块从 `"main"` 改为 `"requirement"`，三语言已合并的 `main.json` 删除
- **`RequirementLanguageKey`** — 新增启动器菜单/配置页面的语言键常量定义
- **`RequirementUiKey`** — 新增 `MENU_LIST_BUTTON_PROFILE = "profile"`
- **`assets/THIRDPARTY_LICENSES.md` 按钮/标签图片更新** — `de.img.*.png`、`mb.img.background.png` 从待替换清单移至原创素材（自行绘制）
- **de 默认按钮纹理替换** — 三态 PNG 替换为自生成 UI 纹理，约 260px→680px，消除第三方素材依赖
- **mb 旧按钮纹理删除** — 三态 PNG 已无引用，对应 UI 配置已迁移至 de
- **mb 标签背景纹理更新** — `mb.img.background.png` 尺寸 630→1123 字节
- **`button/de.json` 字体颜色改白** — `#00008BFF`→`#FFFFFFFF`，适应深色纹理按钮
- **`button/mb.json` 删除** — 弹窗按钮样式已整合至 de
- **弹窗标签移除 `borderScale`** — `mb.content.json`、`mb.title.json` 移除不再需要的 borderScale 字段
- **`message_box.json` 按钮样式改 `default`** — 从已删除的 `message_box.button` 改为 `default`
- **`ui_config.json` 配置列表更新** — 按钮列表移除 `message_box.button`，标签列表新增 `default2`
- **新增 `default2` 按钮/标签样式** — 使用透明纹理 `black16.png`/`black32.png`，适用于纯文字 UI 元素
- **UI 纹理生成脚本入库** — `temp_ui_preview/generate.py`（8 配色）、`generate_styles.py`（6 样式变体）、六种风格预览图集

### 文档

- **`directory_structure.json`** — 同步更新为 `page/` 目录结构
- **`docs/THIRDPARTY_LICENSES.html`** — 新增官网素材版权声明页面，涵盖平台下载图标（Smashicons CC BY 4.0 + 自创混合）、Fugaz One 字体（SIL OFL 1.1）、原创素材清单

### 构建

- **`android/build.gradle` 签名验证加固** — 在 `projectsEvaluated` 外部捕获 `android` 扩展引用，避免 lint 等非 release 任务的同名属性遮蔽导致 doFirst 中 NPE

### 网站

- **页脚增加版权声明** — 三页面（index/community_share/history_versions）统一加入 `© HujiuGame` 和第三方素材版权声明链接
- **符号与 i18n 文本分离** — 所有 emoji（💡⚡🛠️🔧🎮📥📜💬✨）从 `data-i18n` 元素移出到 HTML 硬编码，语言文件只存纯文本，避免切换语言后符号丢失
- **9 语言 `community_description` 移除 ✨** — 符号移至 HTML，对齐符号分离策略
- **讨论区链接更新** — `docs/data/community.json` 更新为新的 Gitee/GitHub Issue 地址，仓库名从 `hujiugame-qingfeng` → `hujiugame.qingfeng`

### 类型

- **`RequirementConfigKey` 工具类填充** — 空类补全为 final utility class，新增 `MENU_LIST_PAGE_MAX_GAME = "page_max_game"` 配置键常量
- **`RequirementUiKey` 分隔符补充** — 按规范补充节分隔符注释，区分启动器/游戏中区域
- **`MenuList` 配置键引用更新** — `ConfigKey.GAME_LIST_PAGE_MAX_GAME` → `RequirementConfigKey.MENU_LIST_PAGE_MAX_GAME`

---

## 2026-07-22 — 主菜单标题图替换为 Fugaz One 字体

### 新增

- **`temp_ui_preview/generate_title.py`** — 标题图生成脚本（Font: Fugaz One, PIL 渲染）

### 变更

- **`menu.title.png`** — 主菜单标题字体从 Pacifico 替换为 **Fugaz One**（Google Fonts / SIL OFL 1.1），"Qing"=#3F48CCFF（蓝），"Feng"=#FDA1FFFF（粉）

### 文档

- **`assets/THIRDPARTY_LICENSES.md` 条目 #9** — 更新为 Fugaz One 字体声明（作者：LatinoType Limitada / Luciano Vergara）

---

## 2026-07-19 — loadPicture 首次加载失败用 errorTexture 占位 + UI 纹理生成工具 + 第三方素材版权清查

### 新增

- **`temp_ui_preview/generate.py`** — UI 纹理 Python 生成工具，8 套配色方案（蓝紫/翠绿/暖橙红/暗黑透明/粉紫/极简黑白/深蓝海洋/琥珀怀旧），圆角渐变风格
- **`temp_ui_preview/generate_styles.py`** — UI 纹理样式变体生成器，支持 6 种样式（线框风格/新粗野主义 4px 2px 1px/极简细边框/磨砂质感），2x 输出分辨率 + 8x AA 超采样抗锯齿

### 变更

- **`assets/THIRDPARTY_LICENSES.md` 条目 #12 待替换素材** — 将 MC 摺纸材质包纹理列明替换计划

### 修复

- **`GraphicsManager.loadPicture` 首次加载失败无限重试** — 当 `getTexture` 返回 `errorTexture` 时，对首次加载的 tag 将其写入 `pictureMap` 占位，避免 `hasPicture` 永远返 false 导致每帧重试和日志刷屏

### 文档

- **`assets/THIRDPARTY_LICENSES.md`** — 完整补全第三方素材声明条目 #9~#12，涵盖 Pacifico 字体、豆包 AI 图像合集、原创素材、待替换素材
- **`C:\Users\11067\hujiugame\qingfeng\game\swxq\THIRDPARTY_LICENSES.md`** — 新增 swxq 游戏完整的第三方版权声明
- **`assets/asset/resource/image/error.png`** — 替换为自生成的 2×2 像素占位图，消除版权风险

---

## 2026-07-19 — JSON 配置标准总览 + 文档体系清理 + enterGame 回滚修复

### 新增

- **`develop/JSON_STANDARD.md`** — JSON 配置标准总览文档，覆盖全部 32 种 JSON 格式，含字段类型、默认值、解析类、新增标准流程

### 变更

- **`LabelManager` 打字速度常量重命名** — `LABEL_TEXT_TYPING_SPEED` → `DEFAULT_LABEL_TEXT_TYPING_SPEED`，与命名规范对齐

### 修复

- **`GameSessionManager.enterGame` 失败回滚** — `loadResource`/`loadData` 失败或 `enterGame` 异常时按 LIFO 顺序回滚已加载的资源、数据和用户配置，避免残留加载状态
- **`GameUserConfigLoader` 主题加载失败语言回滚** — 主题加载失败或异常时回滚已切换的语言管理器，避免 `textManager` 停留在游戏语言

### 文档

- **`MANUAL_TEST.md` 删除** — 经评估该文件从未作为实际检查清单使用，条目过于笼统或琐碎，予以删除
- **`DOCUMENTATION_INDEX.md` 更新** — 移除 MANUAL_TEST.md 引用，新增 JSON_STANDARD.md 条目，更新文档更新原则
- **`CHANGELOG.md` 增加更新检查提醒** — 顶部新增警示框，提醒每次更新日志后检查 `JSON_STANDARD.md`、`SCRIPT_INTERNAL_STANDARD.md`、`DOCUMENTATION_INDEX.md` 是否需要同步更新

---

## 2026-07-19 — 默认主题音效替换 + THIRDPARTY_LICENSES 补充 + JSON 标准文档增强

### 变更

- **默认主题按钮/弹窗音效替换** — 按钮点击音效从 `de.aud.click.ogg` 替换为 `862694__cat-fox_alex__random-click-2.wav`（CC0）；弹窗提示音从 `de.aud.ogg` 替换为 `849886__wavewire__ui_textblip_08.wav`（CC BY 4.0）。涉及 `de.json`、`mb.json`、`message_box.json`、`directory_structure.json`

### 文档

- **`THIRDPARTY_LICENSES.md`** — 新增条目 #7（Wavewire / CC BY 4.0）和 #8（CAT-FOX_ALEX / CC0）
- **`develop/JSON_STANDARD.md`** — 故事树章节全面重写（Section 7 + 14），补充四种节点差异对比、page 解析流程、in/out 连接机制、跨块跳转和缓存策略；全文档补充上下文敏感字段说明（language 双作用域、textKey 解析上下文、theme/user_config 游戏覆盖关系）

---

## 2026-07-19 — 图片版权全面清查 + error.png 替换 + THIRDPARTY_LICENSES 补全

### 变更

- **`THIRDPARTY_LICENSES.md`** — 全面清查项目所有图片素材版权：
  - 新增 #9：`menu.title.png`（Pacifico 字体 / SIL OFL 1.1）
  - 新增 #10：豆包AI 图像素材合集（`app_init.png`、`controller_*`、`keyboard_*`，均已二次修改）
  - 新增 #11：原创素材清单（`menu.masker.png`、透明图、虚拟输入框等）
  - 新增 #12：待替换素材清单（MC 第三方材质包纹理 3 项）
- **`error.png` 替换** — 原来源不明的 17KB 图标替换为程序自创的 2x2 四色 PNG（白/黑/深灰/浅灰）

### 文档

- **`CHANGELOG.md`** — 记录本次变更

---

## 2026-07-18 — 颜色配置修复 + 死代码清理

### 变更

- **`ThemeManager.java` 默认颜色** — 三个兜底色值从 `#FF000000`（全透明红）改为 `#000000FF`（纯黑不透明）
- **`Init.java` 默认进度条颜色** — 硬编码 int 移位改为 `Color.valueOf("#3F47B5FF")` 可读形式

### 修复

- **颜色读取 String→Int 不匹配** — `Init.java` 进度条颜色、`UiManager.java` 标签/按钮颜色通过 `getInt()` 读取 hex 字符串，静默返回 0 导致颜色不生效。统一改为 `getString()` + `Color.valueOf()`
- **`theme.json` 字体颜色透明** — `fontColor` 值为 `#00000100`（alpha=0），改为 `#000000FF`（纯黑不透明）
- **`app_config.json` 尾随逗号** — 删除 JSON 末尾多余逗号
- **`MessageBox.java` 颜色格式** — `Color.valueOf("#FFD700")` 缺少 alpha 位，补全为 `#FFD700FF`

### 移除

- **`UiManager.java` 废弃方法** — 移除重构遗留的死代码 `loadLabelKind(FileHandle, FileHandle)` 和 `loadButtonKind(FileHandle, FileHandle)`，原逻辑已由 `LabelManager` / `ButtonManager` 替代

  完整源码存档如下：

  ```java
  // ==================== 已移除：UiManager.loadLabelKind (line 1674-1745) ====================
  public boolean loadLabelKind (FileHandle file, FileHandle themePath)
  {
      try
      {
          JsonEntity labelKindJson = new JsonEntity(file);
          LogUtils.debug(UiManager.class, "loadLabelKind 读取标签配置: " + labelKindJson);

          String labelKindName = labelKindJson.getString("name");
          if (labelKindName == null)
          {
              LogUtils.error(UiManager.class, "loadLabelKind 缺少 name 字段: " + labelKindJson);
              return false;
          }

          String fontName = labelKindJson.getString("font");
          if (fontName == null)
          {
              LogUtils.error(UiManager.class, "loadLabelKind 缺少 font 字段: " + labelKindJson);
              return false;
          }

          Label.LabelStyle labelStyle = new Label.LabelStyle();
          labelStyle.font = getFont(fontName, 1.0f);
          labelStyle.fontColor = Color.valueOf(labelKindJson.getString("fontColor"));

          Pixmap bgPixmap = null;
          JsonEntity imageJson = labelKindJson.getJsonEntityByKey("image");
          FileHandle resImagePath = themePath.child(PathName.ASSET_S_RESOURCE_IMAGE);

          if (imageJson.isEmpty())
          {
              bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
              bgPixmap.setColor(Color.CLEAR);
              bgPixmap.fill();
          }
          else if (imageJson.containsKey("background"))
          {
              FileHandle bgFileHandle = resImagePath.child(imageJson.getString("background"));
              if (!bgFileHandle.exists())
              {
                  LogUtils.error(UiManager.class, "loadLabelKind 背景文件不存在: " + bgFileHandle.path());
                  return false;
              }
              bgPixmap = new Pixmap(bgFileHandle);
          }
          else if (labelKindJson.containsKey("backgroundColor"))
          {
              Color bgColor = Color.valueOf(labelKindJson.getString("backgroundColor"));
              bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
              bgPixmap.setColor(bgColor);
              bgPixmap.fill();
          }
          else
          {
              bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
              bgPixmap.setColor(Color.CLEAR);
              bgPixmap.fill();
          }

          pendingPixmapMap.put(PIXMAP_LABEL + labelKindName, bgPixmap);
          pendingLabelStyles.put(labelKindName, labelStyle);
          LogUtils.debug(UiManager.class, "暂存标签背景 pixmap: " + labelKindName);
          return true;
      }
      catch (Exception e)
      {
          LogUtils.error(UiManager.class, "loadLabelKind", e);
          return false;
      }
  }

  // ==================== 已移除：UiManager.loadButtonKind (line 2996-3054) ====================
  public boolean loadButtonKind (FileHandle file, FileHandle themePath)
  {
      try
      {
          JsonEntity buttonKindJson = new JsonEntity(file);
          LogUtils.debug(UiManager.class, "loadButtonKind 读取按钮配置: " + buttonKindJson);

          String buttonKindName = buttonKindJson.getString("name");
          if (buttonKindName == null)
          {
              LogUtils.error(UiManager.class, "loadButtonKind 缺少 name 字段: " + buttonKindJson);
              return false;
          }

          String fontName = buttonKindJson.getString("font");
          if (fontName == null)
          {
              LogUtils.error(UiManager.class, "loadButtonKind 缺少 font 字段: " + buttonKindJson);
              return false;
          }

          TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
          style.font = getFont(fontName, 1.0f);

          Color fontColor = Color.valueOf(buttonKindJson.getString("fontColor"));
          style.fontColor = new Color(fontColor);
          style.downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
          style.disabledFontColor = fontColor.cpy().mul(0.5f);

          JsonEntity imageJson = buttonKindJson.getJsonEntityByKey("image");
          FileHandle resImgDir = themePath.child(PathName.ASSET_S_RESOURCE_IMAGE);
          Pixmap upPix = new Pixmap(resImgDir.child(imageJson.getString("up")));
          Pixmap downPix = new Pixmap(resImgDir.child(imageJson.getString("down")));
          Pixmap disabledPix = new Pixmap(resImgDir.child(imageJson.getString("disabled")));

          pendingPixmapMap.put(PIXMAP_BUTTON + buttonKindName + "_up", upPix);
          pendingPixmapMap.put(PIXMAP_BUTTON + buttonKindName + "_down", downPix);
          pendingPixmapMap.put(PIXMAP_BUTTON + buttonKindName + "_disabled", disabledPix);

          JsonEntity audioJson = buttonKindJson.getJsonEntityByKey("audio");
          FileHandle audioFileHandle = themePath.child(PathName.ASSET_S_RESOURCE_AUDIO)
              .child(audioJson.getString("click"));

          pendingButtonStyles.put(buttonKindName, style);
          pendingButtonAudios.put(buttonKindName, audioFileHandle);
          LogUtils.debug(UiManager.class, "暂存按钮 pixmap: " + buttonKindName);
          return true;
      }
      catch (Exception e)
      {
          LogUtils.error(UiManager.class, "loadButtonKind", e);
          return false;
      }
  }
  ```

---

## 2026-07-16 — VirtualInput 选中框保持 + 虚拟输入优化

### 新增

- **`tryToKeepSameSelectObject`** — `VirtualInputHandler` 新增引用搜索实现，交互对象集合刷新后恢复原选中对象行列位置，避免选中框跳跃。仅 O(n) 扫描变更时刻，侵入性最低

---

## 2026-07-15 — 新增两首背景音乐 + 布局层配置集成

### 新增

- **menu2.mp3（Campus）** / **menu3.mp3（Circulation）** — 乌鸦Producer 免费可商用音乐包，署名"音乐由乌鸦Producer提供"，`THIRDPARTY_LICENSES.md` 新增条目 #5/#6

### 变更

- **layout JSON 背景音乐扩展** — `menu_main.json`、`menu_list.json`、`config_basic.json` 三文件 `backgroundMusic` 从单曲/单元素组改为三曲数组 `["menu.mp3", "menu2.mp3", "menu3.mp3"]`，对应界面支持随机播放

---

## 2026-07-14 — 第三方素材版权声明框架

### 新增

- **`THIRDPARTY_LICENSES.md`** — `assets/` 下新建第三方素材著作权声明文件，随发行包分发，记录素材来源、作者、许可协议及署名要求
- **设计文档** — `develop/specs/2026-07-13-thirdparty-licenses-design.md`：版权声明方案选型（单文件 NOTICE）、格式规格、条目模板
- **实施计划** — `develop/plans/2026-07-13-thirdparty-licenses-plan.md`：分步实施计划

### 条目

- **menu.background.png** — 豆包AI 生成，视觉素材可商用（需二次设计）
- **menu.mp3（Scorching Sun）** — 乌鸦Producer 免费可商用音乐包，署名"音乐由乌鸦Producer提供"
- **app_repair.png** — Smashicons（icon-icons.com）CC BY 4.0，署名 Icons by Smashicons
- **icon.png（主题封面图标）** — Smashicons（icon-icons.com）CC BY 4.0，署名 Icons by Smashicons

---

## 2026-07-13 — 官网 HTML 结构优化：语义化 + 内联样式提取 + 修复步骤面板

### 新增

- **修复步骤面板** — 替换 repair 按钮的 `alert()` 为可折叠步骤面板，展示 repair1.png（维修标识位置）和 repair2.png（确定退出）两张步骤图片
- **按钮文字切换** — 点击"查看修复方法"/"收起修复步骤"切换，点击面板外部自动关闭
- **多语言支持** — 全部 9 个语言文件新增 7 个 i18n 键（步骤标题/说明/图片加载提示）

### 重构

- **HTML 结构语义化** — `div.hero-card` → `<header>`；内容区域包裹 `<main>`；`div.tip-card`/`div.card` → `<section>`；`div.footnote` → `<footer>`；品牌名称 `div` → `<h1>`
- **内联样式提取为 CSS 类** — 游戏介绍卡片的内联 `font-size/line-height/color/padding/margin` → `.intro-body`/`.intro-body p`/`.card-intro`；`download-header`/`share-header` 合并为 `.card-header`；模态框错误文本 → `.modal-error` 类
- **CSS 按组件分节** — 基础重置/头部/提示卡片/修复步骤/通用卡片/下载/更新日志/社区/页脚/模态框/响应式
- **卡片间距统一** — `.card + .card` 替代手动 `margin-bottom`

### 修复

- **`:last-child` / `:first-child` 选择器失效** — `.tip-card:last-child` 因 `<body>` 中后续元素（`div.card`、`div.modal`、`<script>` 等）存在而匹配不到任何元素，导致 `bindRepairButton()` 形同虚设。改为 ID 选择器（`#tipCardWatt`、`#tipCardRepair`）
- **`applyI18n` 中的 Watt/Repair 文本选择器** — 同上问题统一修复

### 文档

- **步骤文案调整** — 匹配自动修复流程：点击维修标识 → 自动修复 → 点击确定退出 → 重启程序

---

## 2026-07-13 — 更新检测三段式判断 + 打包脚本版本管理体系增强

### 新增

- **JAR manifest `Implementation-Version`** — `lwjgl3/build.gradle` manifest 增加 `Implementation-Version` 属性，编译时自动从 `projectVersion` 注入，运行时可通过 `Package.getImplementationVersion()` 读取
- **打包工具文档** — `develop/output/README.md` 覆盖打包工具使用方法、7 步流水线详解、版本管理体系（三字段 × 六存储位置）、运行时更新检测机制

### 重构

- **`UpdateChecker` 版本检测三段式逻辑** — `doFileVersionDifferent()` 补充读取 `appVersion` 整型字段并存储为 `internalAppVersion`/`internalAppVersionType`；`checkWebVersion()` 重写为双段判断：正常时使用 `newest_version` 整型比较，整型字段不存在时回退字符串比较，字符串一致时进一步对比版本类型（beta→release 升级检测）
- **`appVersion` 整型字段完整链路** — 从 `app_version.json` 读取 → `UpdateChecker` 存储 → 远程 `newest_version` 整型对比，覆盖了此前只比较版本字符串的盲区

### 构建

- **`build_package.py` 版本管理增强** — 输入版本号、发行类型、整型编码时展示 `[上次: xxx]` 并默认沿用；新增整型编码独立输入（自动上次+1）；`step_update_version()` 补充写入 `appVersion` 整型字段；新增同步 `android/build.gradle` 的 `versionCode`/`versionName`；写入前增加版本确认步骤（展示新旧对比）
- **`DOCUMENTATION_INDEX.md`** 打包工具条目从 `build_package.py` 指向 `develop/output/README.md`

## 2026-07-11 — repairGame 保护文件还原修复 + init 重入守卫

### 新增

- **repairGame(Runnable) 优雅修复** — 不崩溃退出，在子线程中完成资源同步后通过 GL 线程回调通知修复结果
- **NativeDialogUtils 原生对话框** — 支持原生弹窗，Init 修复流程完成后弹窗提示"修复完成，请重启游戏"
- **mb.img.background.png 图片资源修正**

### 重构

- **Init 场景集成修复流程** — 修复中设置 `isRepairing` 标志跳过状态机，完成后弹出原生对话框并退出，不再直接 `CrashUtils.crash()`

### 修复

- **restoreProtectExternalFile 保护文件还原丢失** — 还原前先删除目标文件，避免 Windows `File.renameTo` 因目标已存在而静默失败；检查 `moveFile` 返回值，失败时中断流程而非继续清理 temp — 修复 `user_config.json` 等保护文件被内部默认文件覆盖的问题
- **init() 重入守卫** — 新增 `volatile initRunning` 标志位，`finally` 块确保所有退出路径复位；防止 `Main.threadUpdateVersion()` 和 `repairGame` 并发调用 `init()` 导致文件状态不一致

## 2026-07-11 — NinePatch 边框视觉缩放：sourceBorder/renderBorder 分离

### 重构

- **NinePatch 边框从单值改为 sourceBorder/renderBorder 分离** — 之前 borderScale 修改 NinePatch 裁切深度（`new NinePatch(r, border, border, border, border)`），但对于只有 1px 可见边框的纹理无效（多裁的部分与中心同色）。现在引入两张概念：
  - `sourceBorder`：固定比例 = 控件/16，决定从源纹理边缘取多少像素作为九宫格裁切位置
  - `renderBorder`：= sourceBorder × borderScale，决定裁出的像素在屏幕上绘制多大
  - 使用 `NinePatch.setLeftWidth/setRightWidth/setTopHeight/setBottomHeight` 实现视觉缩放，即使用户图片只有 1px 边线也能拉伸变粗
  - 涉及文件：`UiManager.java`、`LabelManager.java`、`ButtonManager.java`
- **纹理尺寸安全校验** — 对 sourceBorder * 2 超过纹理尺寸的图片（如 1×1 像素的 `black64.png`）跳过 NinePatch，回退到整图拉伸，防止九宫格无效导致控件不显示

## 2026-07-09 — 初次启动屏幕自适应分辨率 + UseViewport 视口工厂方法

### 新功能

- **初次启动自适应分辨率** — 首次运行检测屏幕尺寸，取 80% × 16:9 设窗口并写入配置文件：
  - 检测 `Gdx.graphics.getDisplayMode()` 获取屏幕分辨率
  - 以宽度为基准取 80% 后按 16:9 等比算高度，超出屏幕 80% 高度则反算
  - 检测失败回退 1024×576 兜底
  - 写入仅含 resolution 字段的配置到外部路径，由 UpdateChecker protect 自动合并为完整配置
  - 后续启动直接读取已存配置，不再检测

### 重构

- **UseViewport 提取视口创建** — 将 `Main.initLibGDX()` 中的 switch 视口创建逻辑内聚到 `UseViewport.getViewport()` 工厂方法，消除 import 重复和 ScreenSize 直接耦合

## 2026-07-08 — FileHandle 命名规范化 + FileUtils JavaDoc 完善 + .list() 安全替换

### 重构

- **FileHandle 变量命名统一** — 消除所有 `xxxFile` 后缀的 FileHandle 变量，统一为 `xxxFileHandle`（文件）或 `xxxPathHandle`（目录）。涉及 15 个文件约 40+ 个变量：
  - `UiManager.java`（4 处）：`fontJsonFile`、`bitmapFontFile`、`bgFile`、`audioFile`
  - `VirtualInputHandler.java`（8 处）：`controllerConfirmPictureFile` 等按钮图片变量
  - `LayoutManager.java`（5 处）：`backgroundMusicFile`、`musicFile`、`backgroundPictureFile`、`pictureFile`、`gifFile`
  - `ImageManager.java`（2 处）：`imageFile`
  - `LabelManager.java`（3 处）：`bgFile`
  - `ButtonManager.java`（1 处）：`audioFile`
  - `GraphicsManager.java`（1 处）：`errorFile`
  - `MessageBox.java`（1 处）：字段 `boxAudio`
  - `ControllerInputHandler.java`（1 处）：字段 `virtualMousePictureFile`
  - `MenuList.java`（2 处）：`filehandle`
  - `LogUtils.java`（2 处）：`logFileHandle` 字段、`configFileHandle` 局部变量
  - `GameLogicService.java`（1 处）：`gameConfigFile`
  - `CrashUtils.java`（2 处）：`logFile`、`crashFile`
- **FileUtils `copyDirectoryRecursiveFix` 参数/局部变量重命名** — `oldFile`/`newFile` → `sourceDirectoryPath`/`destDirectory`，消除误导性命名；`old_DIRECTORY_STRUCTURE` 等局部变量同步精简

### 修复

- **`.list()` 安全替换** — 将 `FileUtils.deleteDirectoryRecursive` 中的原始 `file.list()` 替换为 `FileUtils.getList(file)`，确保 Android Internal 目录安全删除
- **`GameScriptManager` 循环变量修正** — `scriptFile` → `scriptFileHandle`，保持命名一致

### 文档

- **FileUtils.java 全量 JavaDoc 完善** — 为 14 个公开/私有方法补充详细的 @param 和 @return 描述：
  - 区分 `isExist`（文件或目录）/ `isFileExist`（仅文件）/ `isDirectoryExist`（仅目录）的语义差异
  - `getList` 说明 Internal 类型使用 `directory_structure.json` 替代 `File.list()` 的跨平台策略
  - `copyDirectory`/`deleteDirectory`/`moveDirectory`/`clearDirectory` 说明操作行为和注意事项
  - `createStringFileOfLog` 说明日志专用场景和 UTF-8 编码
  - `copyDirectoryRecursiveFix` 说明 Android Internal 专用遍历策略

## 2026-07-08 — PathType→FileHandle 全量迁移 + QfFiles 包装 + 路径翻倍调试

### 新增

- **QfFiles/QfFileHandle** — `util/system/` 下新增 Files 包装层，`toString()` 输出 `"type:path"` 格式，便于日志中区分 External/Internal 等文件类型。`Main.create()` 中通过 `Gdx.files = new QfFiles(Gdx.files)` 一行替换全局生效
- **LogUtils 日志目录预创建** — `updateFileByDayTime()` 创建日志文件句柄后主动 `file.parent().mkdirs()`，避免首次写入时因目录不存在而失败
- **FileUtils.createStringFileOfLog mkdirs** — 写入前调用 `file.parent().mkdirs()`，确保日志文件父目录已创建

### 重构

- **PathType 全量删除** — 从项目 35+ 个文件中移除 `PathType` 传参模式，统一使用 libGDX `FileHandle` 作为文件路径载体，共 -1083/+651 行变更。涉及 `FileUtils`、`ThemeManager`、`LanguageManager`、`UpdateChecker`、`UserConfigManager`、`GameTemplateManager`、`GameRoleManager`、`GameStoryManager`、`GameScriptManager`、`LayoutManager`、`SceneStack`、`MenuList`、`MenuMain`、`Init`、`Page`、`Role`、`Player`、`PlayLocalData`、`EventEnterGame`、`GamePlayDataLoader`、`GameUserConfigLoader` 等核心类
- **PathType.java 删除** — `type/file/PathType.java` 已无引用，从版本库移除
- **FileUtils 精简** — 删除所有 `PathType` 重载方法（`createStringFile`/`readStringFile`/`isExist` 等的 PathType 变体），保留纯 `FileHandle` API；`DIRECTORY_STRUCTURE` 重命名为 `INTERNAL_DIRECTORY_STRUCTURE`
- **Main.java 路径修复** — `rootPath` 不再通过 `Gdx.files.external("").file().getAbsolutePath()` 获取（QfFiles 包装后路径翻倍），改为直接 `System.getProperty("user.home")`

### 编码规范

- **JsonEntity** — 移除 `FileUtils`/`LogUtils` 导入（不再使用 `readStringFile` 和日志）
- **import 清理** — `PathType` 删除后波及文件同步移除已不再使用的 import

### 待修复

- **QfFiles 路径翻倍** — QfFiles 包装启用后，`Gdx.files.external()` 产生的路径出现翻倍（如 `C:\Users\11067\C:\Users\11067\hujiugame\...`），从第 2 次调用起持续累加。临时禁用 QfFiles 包装（Main.java:162 已注释），QfFiles/QfFileHandle 源文件保留供后续排查。根因推测为 QfFileHandle 包装链与 Lwjgl3Files.delegate 交互中的状态污染，具体待二次介入

---

## 2026-06-20 — 静默崩溃审计：异常分级 + CrashUtils/SafePostRunnable 提取 + 全路径修复

### 新增

- **CrashUtils.java** — 从 Main.java 提取崩溃处理逻辑到 `util/system/CrashUtils.java`，提供 `crash(Throwable)` 和 `crash(Exception)` 两个重载，自动生成独立崩溃日志 + 弹窗通知 + 阻塞退出
- **SafePostRunnable.java** — 安全的 GL 线程调度工具，包装 `Gdx.app.postRunnable`，异常时自动触发 CrashUtils.crash()

### 重构

- **EventDispatcher.java** — `handleEvent()` 外层 catch 改为 `throw new RuntimeException(e)`，7 个子 handler 移除冗余 try-catch，异常自然传播到 GameHost → Main → CrashUtils.crash()；保留 `handleEventOfLoadGameConfig` 内层 catch（L2 降级）
- **Main.java** — 移除 `crash()` 方法（已提取到 CrashUtils）；移除 `import com.hujiugame.qingfeng.Main` 的无效引用的传播
- **所有 postRunnable** — 关键路径（GraphicsManager/UiManager 的纹理销毁）保留 SafePostRunnable.crash；非关键路径（TextInputUtils/FileChooser/FileExplorer 的回调）降级为内部辅助方法 + 仅日志，避免对话框/文件选择器异常导致游戏崩溃

### 修复

- **Main.render()** — `catch (Exception)` 改为 `catch (Throwable)`，所有未捕获异常弹出崩溃对话框而非静默闪退
- **GameHost.run()** — catch 块追加 `throw e`，异常传播到 Main → crash，不再被静默吞噬
- **RenderPipeline.updateFrame/render** — catch 块追加 `throw e`，异常传播到 GameHost → crash
- **Init.java** — `initAudio`/`initGraphics`/`initUi` 三处 CrashUtils.crash() 后补加 `return`，消除"崩溃后仍执行后续代码"的逻辑错误
- **SceneStack.java** — `popGameState`/`setGameState`/`resetGameState` 外层 catch 追加 `CrashUtils.crash(e)`，意外异常不再静默吞掉

### 优化

- **updateVersionThread** — UncaughtExceptionHandler 降级为只 `LogUtils.error`，版本检查线程崩溃不影响游戏运行
- **UpdateChecker.repairGame** — 失败路径降级为只 `LogUtils.error`，删文件失败不触发崩溃
- **CrashUtils.java** — `InterruptedException` 处理从 `e1.printStackTrace()` 改为 `LogUtils.error`
- **AudioManager/GraphicsManager/UiManager** — dispose 子线程添加 UncaughtExceptionHandler

---

## 2026-06-18 — 文档全面审查：修复过时类名/包路径/主循环链路 + CHANGELOG

### 修复

- **CLAUDE.md** — 主循环链路修正为 `GameHost.run → renderPipeline.updateFrame → eventQueue/dispatcher → renderPipeline.render`；命名示例 `GameController`→`GameHost`；日志格式更新为 `ClassName.class` 参数
- **CONTRIBUTING.md** — 主循环链路同上修正；包结构表更新（`controller/`→`core/`、`GameStateService`→`SceneStack`、`EventManager→EventQueue→EventDispatcher`）；建议阅读顺序 `GameController.java`→`GameHost.java`；日志格式同步更新
- **COMMIT_STYLE.md** — scope 表删除废弃 `controller` 项，更新 `core`/`event`/`ui`/`render` 描述；示例中过时类名全部替换（`GameController`→`GameHost`、`GameEventService`→`EventDispatcher`、`GameStateService`→`SceneStack`）
- **CODING_STYLE.md** — 示例代码中的 `GameControllerImp`/`gameController`/`gameStateService` 全部替换为当前类名
- **REVIEW.md** — 事件系统描述 `EventManager + GameEventService`→`EventQueue + EventDispatcher`；`GameController`→`GameHost`；脚本引擎状态更新为 ScriptExecutor + Page/GamePlay 集成已完成

### 文档

- **MANUAL_TEST.md** — `GameController`→`GameHost`

---



### 新增

- **个人代码成长分析** — 在 `develop/grow/` 下新增四代项目（SGL/PGL/Qingfeng/Java）全量分析合集 `MERGED_ANALYSIS_20260618.md`（386KB），含 SGL 代码评价、14 维度演化轨迹、注释考古报告（情绪曲线），已基于源码交叉验证修正多处错误
- **分析法则框架** — `GROW_UP_ANALYSE_RULE.md` 定义溯源原则、五维模型、五阶段分析流

### 修复

- **DOCUMENTATION_INDEX.md 路径修复** — 第四层引用的文件路径从根目录修正为 `develop/grow/`，`SGL_MYCODE.md`/`GA2026061801.md`/`GA2026061802.md` 三份独立文档已合并为 `MERGED_ANALYSIS_20260618.md`

### 文档

- **VirtualInputHandler.java 方法注释** — 为 `moveVirtualConfirmSelect`、`refreshInteractableObjectMap`、`refreshSelectObject` 等关键方法补充中文注释（约 30 处），说明边界处理、空集合跳过、行分组刷新等行为的"为什么"
- **assets/asset_trash/trash_config.json** — 新增空资源回收配置框架

---

## 2026-06-17 — Button/Label NinePatch 自适应 border + 预加载流程精简 + Debug 日志增强

### 新增

- **按钮/标签自适应 NinePatch** — Button 和 Label 的背景纹理不再用固定 border 值，改为根据**控件实际宽高**动态计算：`border = min(w, h) / 16`。控件放大时四角等比放大，不再出现"角小脸大"的不协调，接近圆/椭圆的纹理也适用
- **所有 UI 背景统一 NinePatch** — 所有按钮（up/down/disabled 三态）和标签背景纹理自动应用自适应 NinePatch，无须手动配置纹理 assets
- **Debug 日志打点** — 在 NinePatch 启用/跳过、纹理合并过程、kind 绑定等关键路径添加 LogUtils.debug，崩溃后可从日志反查 border 值、控件尺寸、纹理尺寸

### 重构

- **`UiManager.buildDrawable` 简化** — 去掉预存的 `pendingPixmapBorders` 查找，仅返回 `TextureRegionDrawable`；border 计算推迟到控件创建时刻
- **`UiManager.buildLabelDrawable` 简化** — 同样去掉 border 查找，仅返回 `TextureRegionDrawable`
- **`UiManager.buildLabelBackground` 重构** — 改为接收控件宽高参数，在此动态计算 border 并创建 NinePatch
- **`LabelManager.createLabelBackground` 重构** — 同上，去掉 `getLabelKindBorders` 查询，改为接收控件宽高
- **`ButtonManager.createButton` 增强** — 在按钮创建时根据控件宽高计算 border，通过 `adaptDrawable` 工具方法将 up/down/disabled 三个 Drawable 包装为自适应 NinePatchDrawable
- **删除 `pendingPixmapBorders` / `labelKindBorders`** — 移除两张 border 缓存 Map，不再需要预存 border 值
- **删除 `NinePatchHelper`** — 自动 border 检测方案已废弃，对应辅助类移除

### 修复

- **半透明纯色小纹理不显示** — 自适应的 border 算法自带兜底：`border*2 >= 控件短边` 时跳过 NinePatch，回退到普通 TextureRegionDrawable，1×1 等极小纹理正常渲染

### 编码规范

- **`ButtonManager` imports** — 补全 `NinePatch`/`TextureRegion`/`Drawable`/`NinePatchDrawable`/`TextureRegionDrawable` 导入
- **`UiManager` imports** — 清理不再使用的 `NinePatch`/`NinePatchDrawable` 导入；随后因 `buildLabelBackground` 使用 NinePatch 又重新加入
- **`LabelManager` imports** — 移除废弃的 `TextureRegionDrawable`/`NinePatchHelper` 导入，补充 `TextureRegion` 导入

---

## 2026-06-16 — P0/P1 逻辑漏洞修复 + 页面进入机制 + 编码规范对齐 + 树结构修复

### 新增

- **页面进入机制** — `Player` 新增 `nextPage` + `setNextPage`/`enterNextPage`/`getNextPage` 双缓冲页面切换；`GamePlay.localHostUpdate` 新增完整页面进入流程：初始脚本（start）→ 循环任务（loop）→ 触发器任务（trigger）+ `ScriptExecutor.update` 每帧驱动
- **TriggerTask 完整实现** — 从空占位类变为完整 `Task` 接口实现，支持每帧轮询触发条件并在满足时推入子任务
- **PlayLocalData.scriptExecutor** — 新增脚本执行器字段，替代通过 DI 容器获取

### 重构

- **命名规范对齐** — 全局重命名 `eventManager`→`eventQueue`、`gameController`→`gameHost`、`gameStateService`→`sceneStack`，涉及 17 个文件（GamePlay/GameMenu/GameRole/Init/MenuList/MenuLoad/MenuMain/ConfigBasic/ControllerInputHandler/KeyboardInputHandler/UniversalInputHandlerFunction/VirtualInputHandler/TextManager 等）
- **PlayLocalData 字段精简** — 移除 `gamePath`/`gamePathType` 字段，改为由 `gamePathDirectory` 推导
- **GameInfoKey** — 新增 `PLAY_NEXT_PAGE_ID`
- **Name** — 新增 `GAME_LOOP_TASK_NAME`、`GAME_START_TASK_NAME`、`GAME_TRIGGER_TASK_NAME`

### 修复

- **ScriptExecutor P0 死循环** — `executeCallAtomicValueCommand` 缺少脚本时返回 0 不推进指令，导致 `executeValueTask` while 循环无限执行同一条指令。改为推入默认值 0 并 `nextCommand()`
- **ScriptExecutor P0 参数错位** — `executeCallAtomicValueCommand` 创建子任务时 `new HashMap<>()` 误传入 `defaultReturnValue` 位置（应为 0 或脚本声明的默认值），导致脚本返回值被替换为空 HashMap，参与后续计算时 ClassCastException 崩溃
- **ScriptExecutor P1 CONST 参数 key=null** — `parseArguments` 中 CONST 类型参数的 `getName()` 返回 null，导致所有 CONST 参数以 null 为 key 存入 Map，无法被调用脚本访问。新增 `ArgumentInfo.argumentName` 字段区分参数名与变量名
- **ScriptExecutor P1 并发修改** — `removeTriggerTask` 在 for-each 遍历 `triggerTaskList` 的同时调用 `removeTask` 间接修改同一列表，修复为遍历快照副本 `new ArrayList<>(triggerTaskList)`
- **ScriptExecutor 低版本 JDK 兼容** — `String.repeat()` 替换为 `StringBuilder` 循环拼接，兼容 JDK 8
- **GameVariableManager.setVariable 静默创建** — 对未定义变量赋值时先打 ERROR 日志再继续创建，行为矛盾。改为直接 `put`，不再误报
- **GameScriptManager 扫描排除自身** — `loadScriptData` 在无配置清单时扫描目录全部 `.json`，将 `script_config.json` 自身也当作 Script 加载，导致 `缺少 commands 字段` 误报。新增文件名过滤排除配置清单自身
- **TreeStructure setNowPageId 硬编码返回 false** — `RootStructure`/`NodeStructure`/`LeafStructure` 的 `setNowPageId` 之前一律返回 false，导致 `storyGotoPage` 对非 Branch 类型永远跳转失败。改为与各自单页 ID 比对
- **TreeStructure getPageIdList 返回 null** — 三个树结构实现类返回 null 改为 `Collections.singletonList`，消除 `storyGotoPage` 中 NPE 隐患

---

## 2026-06-16 — 脚本引擎包迁移 + 帧驱动执行器 + 任务系统 + 编码规范修复

### 新增

- **ScriptExecutor** — 帧驱动脚本执行引擎，每帧 MAX_COMMAND_COUNT_PER_FRAME=50 条指令上限，支持多任务栈并发（HashMap 打乱遍历）
- **Task 任务系统** — `Task` 接口 + `ScriptTask`（命令序列执行）/ `ValueTask`（前缀表达式求值）双实现，基于 `TaskStack`（Stack<Task>）管理父子任务委派
- **TaskType** — `COMMAND_NORMAL` / `COMMAND_WHILE` / `COMMAND_CALL` / `VALUE_MATH` / `VALUE_LOGIC` 五种任务类型
- **ScriptContent** — 脚本执行上下文，聚合 UiManager / GameSessionManager / GameVariableManager / GameInfoManager / GameScriptManager
- **GameVariableManager** — 游戏变量管理器，支持 has/get/set/remove 操作

### 重构

- **包迁移** — `data/script` → `script`（`com.hujiugame.qingfeng.script`），消除深层 data 包层级
- **Task 类重命名** — `ScriptTask` → `Task`（接口）、`ScriptTaskStack` → `TaskStack`、`ScriptTaskType` → `TaskType`，新增 `ValueTask` 值求值任务

### 修复

- **GameSessionManager.storyGotoPage** — setNowPageId 失败分支补充 `return false;`
- **ScriptCommandParser** — `ReturnControlScriptCommandParam` 使用无参构造改为 `new ReturnControlScriptCommandParam(paramJson)`
- **CallAtomicValueCommandParam** — 序列化 bug 修复：`json.put("arguments", arguments)` 直接存 List<ArgumentInfo> 改为 `stream().map(ArgumentInfo::getJson).collect()`
- **LabelClickTriggerParam** — 移除过时 `script` 字段及其 JSON 校验
- **ValueCommandParser** — `parseAtomic` switch 补充 `case CALL:` 分支

### 编码规范

- **TaskType** — 移除枚举尾部逗号（Java 不允许）
- **Task 接口** — 统一方法声明空格（`method ()` 而非 `method()`）
- **ScriptTask / ValueTask** — 修复 `getCurrentCommand` 双空格，统一方法声明空格
- **TaskStack** — 方法声明添加空格（Allman 风格）
- **LabelClickTriggerParam** — `tag` 字段添加 `private` 修饰符
- **导入顺序** — 修复 TaskStack / ScriptTask / ValueTask / ScriptExecutor / GameVariableManager / PageBehavior / GameStoryManager / Page 共 8 个文件的 import 分组顺序（Java 标准库 → libGDX → 项目内部）
- **Page** — 项目内部 import 按字母重排

---

## 2026-06-15 — ScriptExecutor 栈安全修复 + PageBehavior 纯内联化

### 重构

- **PageBehavior** — 移除 reference 脚本引用模式，仅保留 inline 内联。删除字段 `isStartScriptInline`/`isLoopScriptInline`/`startScriptName`/`loopScriptName`/`startScript`/`loopScript`；删除三个引用构造器（预加载 Script、动态加载 .script、路径参数）；删除 `buildScriptFromCommands()`/`parseScriptByName()` 及关联 Getter；JSON 解析构造器简化去掉 `scriptPath`/`pathType` 参数
- **Page** — `new PageBehavior(pageBehaviorJson, scriptPath, scriptPathType)` 同步改为 `new PageBehavior(pageBehaviorJson)`

### 修复

- **ScriptExecutor.executeScriptTask** — IF/WHILE 指令 push 子任务后，while 循环仍用局部缓存的 `task` 引用消费指令，导致子任务未执行时父任务已 advance 到后续指令。新增栈大小对比检测（`stackSize != taskStack.size()` 时 break），每帧只处理单层栈顶，下一帧 `executeTaskStack` re-peek 后自动消费子任务。同步修复 `executeBreakControlScriptCommand` 缺少 `forceFinish()` 和 `return 1` 的问题

### 编码规范

- **PageBehavior** — 删除不再需要的 `Script`/`PathType`/`FileUtils` 导入

---

## 2026-06-10 — 主题字体预缓存 + Page 包迁移 + PageBehavior 骨架

### 新增

- **主题字体预缓存配置** — `theme.json` 新增 `fontUseSize` 字段，指定启动时需预缓存的字体缩放尺寸；`ThemeManager` 新增解析逻辑，缺失时回退至 `Numeric.getFontNormalScaleList()` 默认值；`UiManager.CustomFont` 改为使用主题配置的尺寸列表
- **两个主题已配置** — `default_theme` 按 layout 实际使用配置 `[0.8, 1.2, 1.6]`，`swxq` 游戏主题配置 `[0.8, 1.2, 1.3, 1.6]`

### 重构

- **Page 类包迁移** — `data.story.Page` → `data.story.page.Page`，对齐 story 包内按功能划分子包的规范（page/tree）；同步更新 `Player`、`GameStoryManager`、`GamePlay` 中的导入路径

### 基建

- **PageBehavior 骨架** — 在 `story.page` 包下新建 `PageBehavior.java`，为后续 start/loop/trigger 三区行为模型做准备

---

## 2026-06-10 — LogUtils 重构：字符串标签改为 Class<?> 传参

### 重构

- **LogUtils 接口扩展** — 新增 `debug/info/error` 的 `Class<?> clazz` 重载，内部通过 `clazz.getSimpleName()` 获取标签名，消除字符串标签在类重命名时不同步的风险；旧 `String tag` 方法保留向后兼容
- **全部 71 个源文件调用点迁移** — 1528 处 `LogUtils.xxx("ClassName", ...)` 统一替换为 `LogUtils.xxx(ClassName.class, ...)`，充分利用编译期类型安全
- **修复 5 处历史错误日志标签** — `ButtonManager` 误用 `"UiManager"`、`LabelManager` 误用 `"UiManager"`、`ImageManager` 误用 `"UiManager"`、`UniversalInputHandlerFunction` 误用 `"UniversalFunction"`、`UpdateChecker` 误用 `"Init"` 等，均修正为所在类自身的 `.class` 引用

### 编码规范

- **日志标签标准化** — 消除所有与类名不匹配的字符串标签，类重命名后日志标签自动跟随

---

## 2026-06-10 — 脚本引擎解析器实现 + 工具包结构重组 + 值对象重构

### 新增

- **命令/值解析器** — `ScriptCommandParser.parse()` / `ValueCommandParser.parse()` 完整实现，支持 control/variable/story 三类命令和 atomic/math/compare/logic 四类值命令的分发解析（含校验/日志/异常处理）
- **TypeMapper 类型系统** — 新增 `TypeMapper.java`，提供 Java 类与类型字符串的双向映射（int/float/boolean/String），支持 Lenient 宽松解析
- **Variable/Story 命令支持** — 新增 `VariableScriptCommand` + `CreateVariableScriptCommandParam` / `AssignmentVariableScriptCommandParam`；`ForwardPageStoryScriptCommandParam` / `GotoPageStoryScriptCommandParam`
- **Script JSON 构造** — `Script` 类新增 `Script(JsonEntity)` 构造函数，支持从 JSON 反序列化完整脚本（含参数、命令列表、返回值）
- **序列化支持** — 全部 ScriptCommand / ValueCommand 实现 `getJson()`，支持命令对象序列化为 JSON
- **值对象重构** — `ValueObject` 从接口改为基类，`LogicValue`/`MathValue` 继承并增加指令类型合法性校验

### 重构

- **工具包结构重组** — `util/parser/` → `util/json/parser/`；`util/interfaces/` → `util/interact/interfaces/`；`LogUtils/FileUtils/PlatformUtils/FilePathConfig` → `util/system/`；`TextInputUtils` 包路径修正
- **Control 命令参数重命名** — 7 个类去掉 `Command` 后缀统一为 `*CommandParam` 命名规范
- **IfControl/WhileControl 参数增强** — 子命令 JSON 构建改为流式映射；getter 重命名（`getTrueScript/getFalseScript` → `getThenCommands/getElseCommands`）

### 编码规范

- **泛型修复** — 多处 `Map<..., Class>` 原始类型 → `Map<..., Class<?>>`
- **间距规范化** — `ScriptCommandParam`/`ValueCommandParam` 接口方法空格对齐
- **导入清理** — 移除未使用的引用

---

## 2026-06-10 — 框架重构：controller→core 类名变更 + 事件系统迁移

### 重构

- **包结构重组** — `controller/GameController`→`core/GameHost`、`GameStateService`→`core/SceneStack`、`GameRenderService`→`core/RenderPipeline`、`GameConfigLoader`→`core/GameResolver`、`UpdateController`→`core/UpdateChecker`
- **事件系统迁移** — `controller/GameEventService`→`event/EventDispatcher`、`manager/EventManager`→`event/EventQueue`
- **引用全面更新** — 同步更新 15 个引用文件的 import、类型声明、getter 调用
- **旧文件清理** — 删除 `controller/` 下 6 个文件和 `manager/EventManager.java`

---

## 2026-06-07 — 脚本引擎基础框架：Script + ScriptCommand + 解析器

### 新增

- **脚本数据模型** — 新增 `data/play/script/Script` 和 `ScriptCommand` 类，支持从 `.script` 文件（JSON 数组格式）、`FileHandle`、`JsonEntity`、命令列表多种构造方式；包含有效性校验（`isValid()`）、复制构造、深拷贝、`equals/hashCode/toString` 完整覆写
- **脚本解析器** — 新增 `utils/json/parser/JsonScriptParser`，提供 `parseType/parseAction/parseScript` 等静态方法，遵循项目 Parser 模式（try-catch + 存在/不存在/异常三级调试反馈）

### 文档

- **`develop/REVIEW.md`** — 第 72 项"脚本引擎"标记为 🚧 进行中

---

## 2026-06-07 — 布局字段级融合 + Label 全方向对齐修复

### 新增

- **Linux `.deb` 文件关联** — `build_package.py` 的 `.desktop` 添加 `MimeType` 和 `%f` 参数；新增 freedesktop MIME XML 注册 `.qfg` → `application/x-qingfeng-game`；`postinst`/`postrm` 添加 `update-mime-database` 刷新

### 修复

- **`FileChooser.java`** — `EXT_GAME` 常量从 `.qgf` 修正为 `.qfg`，与 README 文档及用户流程一致
- **`AndroidManifest.xml` / `AndroidLauncher.java`** — `pathPattern`、注释、临时文件名同步修正 `.qgf` → `.qfg`
- **README / docs / locales** — 三语言及网站文案中 `.qgf` 全部替换为 `.qfg`

### 文档

- **`develop/REVIEW.md`** — 第 4 项 `.qfg` 文件关联标记完成 ✅

---

## 2026-06-06 — 代码命名优化 + 启动器控制台修复 + dispose 调试信息补充

### 命名优化与包结构调整

- **`GameController`及相关类重构** — 重命名 `GameRenderer`→`GameRenderService`、`GameLogic`→`GameLogicService`、`GamePlayDataContent`→`PlayDataContent`；部分类移动至 `loader/`、`play/` 子包归类（13 文件，含 InstanceContent/多个 Render 实现适配）
- **Event eventName 构造函数赋值** — 8 个 Event 类（EventEnterGame、EventLoadGameConfig、EventPlayGame、EventPopGameState、EventPushGameState、EventQuitGame、EventResetGameState、EventSetGameState）将 `eventName` 字段赋值统一移至构造函数中，消除外部手动 set
- **`RequirementUiKey`** — 新增 GameMenu 所需的 UI 标签 Key

### Bug 修复

- **`launcher.c`** — 修复 GUI 子系统下 `console:true` 无控制台窗口的问题：新增 `show_console()` 调用 `AllocConsole()` 显式创建控制台并重定向 stdout/stderr；控制台模式下 Java 进程直接继承启动器控制台（不创建管道、不使用 `CREATE_NO_WINDOW`），实现游戏日志实时输出
- **`build_package.py`** — jlink 模块列表追加 `jdk.crypto.ec`，修复因缺少 EC 加密提供者导致 Let's Encrypt（ECDSA 证书）SSL 握手失败的问题

### 优化

- **`UpdateController.java`** — 官网更新检测重试间隔从 2 秒延长至 5 秒（`RETRY_DELAY_MS`: 2000→5000），最大尝试次数从 2 次增加至 3 次（`MAX_RETRY`: 2→3）
- **dispose 调试信息** — `AudioManager`、`MessageBox`、`UpdateController`、`GraphicsManager` 统一添加 `dispose()` 完成情况的调试日志
- **`LogUtils`** — 日志加载配置输出从原来仅显示等级数字，额外附加等级字符串信息，使日志更直观
- **`menu.mp3`** — 当前默认主题的菜单音乐换回 v0.0.0-beta 版本

### 代码清理

- **`PlayLocalData`** — 字段与方法命名对齐：`getPlayerData`/`setPlayerData` 统一为 `getPlayer`/`setPlayer`；getter/setter 按字段声明顺序重排
- **`GameController`** — 构造函数中 `GameUserConfigLoader`/`GameResourceLoader`/`GamePlayDataLoader` 三个中间局部变量内联至 `sessionManager` 赋值语句，消除冗余局部变量

### 文档

- **`develop/COMMIT_STYLE.md`** — 修复部分英文提交头的残留信息，全部替换为中文格式

---

## 2026-06-06 — 命名对齐 + GamePlay 主机模式 + 布局安全增强 + 数据结构扩展

### 新增

- **`PlayRuntimeData`** — 新增 `playerList` 字段及 CRUD 方法（`getPlayerList`/`setPlayerList`/`addPlayer`/`removePlayer`），支持多人玩家列表管理
- **`Player`** — 新增 `ipp` 网络地址字段，`setIpp` 同步写入 `GameInfoManager`
- **`GameInfoKey`** — 新增 `PLAY_IPP` 常量并注册到 keys 列表

### 命名优化

- **`GameController.getGameDataContent` → `getPlayLocalData`** — 与 `playLocalData` 字段名对齐，消除歧义，统一 6 个文件 24 处调用点（GameMenu、GameRole、3 个 InputHandler）

### 重构

- **`GamePlay` 主机模式布局系统** — 新增 `generateLayout()` / `localHostUpdate()` / `remoteHostUpdate()` 方法；`update()` 按 `Hoster` 类型分发（LOCAL_HOST 从 Page 获取真实布局，REMOTE_HOST 预留）；`doInit()` 移除直接赋值 `layout`，由主机更新逻辑负责

### 修复

- **`inno_setup.iss`** — `.qfg` 文件关联的 `DefaultIcon` 从 `{app}\launcher.exe,0`（console.ico）改为 `{app}\icon.ico`，修复 .qfg 文件图标显示控制台图标的问题

### 优化

- **`GameStateService.updateGameLayout` 布局安全增强** — 子状态映射值为 null 时跳过布局加载；获取布局文件失败时重置为空 `LayoutConfig`，避免残留前一状态的布局数据

## 2026-06-04 — 文档体系重构 + 启动器错误捕获增强 + 崩溃日志独立输出

### 文档体系重构

- **新增根文档索引** — `DOCUMENTATION_INDEX.md` 统一统领所有 .md 文件，按读者角色分层（所有读者 / 贡献者 / 工具链维护者），README.md 三语言段均添加入口链接
- **新增启动器说明文档** — `lwjgl3/setup/README.md` 详细说明启动器设计目标、工作流程、编译方法、Win7 实验性支持及排查指引
- **项目文档更新** — `CONTRIBUTING.md` 补充 MinGW-w64 前置要求、launcher.c 位置说明、打包流程细节
- **Claude 助手指令** — `CLAUDE.md` 新增「文档维护」章节，规定新增/重命名文档文件后必须同步更新 `DOCUMENTATION_INDEX.md`
- **记忆文件全面刷新** — 10 个记忆文件同步更新至当前项目状态（JDK 21、launcher.c 迁移完成、develop/ 文件合并）

### 启动器错误捕获增强

- **`launcher.c`** — Java 启动段改为管道捕获：创建 `CreatePipe` 绑定 Java 进程 stdout/stderr，`CreateProcessW` 失败时显示错误码，Java 非零退出时弹窗显示 stderr 错误输出 + 退出码
- **`launcher.c` Win7 弹窗润色** — 实验性支持提示改写为更周详的排查指引，明确"当前系统环境不满足"是可能原因

### 崩溃日志独立输出

- **`Main.java` `crash()` 重写** — 崩溃时自动生成独立崩溃日志文件 `hujiugame/qingfeng/log/crash-{yyyyMMdd-HHmmss}.txt`，包含异常类名、消息、完整堆栈跟踪、日志文件引用
- **`FileName.java`** — 新增 `CRASH_LOG` 常量（`"crash-"`）
- **崩溃弹窗优化** — 弹窗直接显示崩溃日志的绝对路径，引导用户将此文件发送给开发者

### JDK 21 升级

- **构建工具链升级至 JDK 21** — `gradlew.bat` 自动下载从 JDK 17→21（清华镜像 `21.0.11_10`），`build_package.py` 打包检测同步升级
- **construo 跨平台 JDK 同步升级** — `lwjgl3/build.gradle` 中 Linux/macOS/Windows 四个平台 JDK 下载全部更新至 21.0.11_10
- **jlink `--compress` 参数适配 JDK 21** — `"2"` → `"zip-2"`（JDK 21 废弃旧语法）

### Windows 7 兼容性修复

- **`build_package.py`** — jlink 生成 JRE 后从 [adang1345/api-ms-win-core-path](https://github.com/adang1345/api-ms-win-core-path) 自动下载开源 shim DLL（~114KB，MIT 协议），复制到 `jre/bin/` 目录。`java.exe` 在 Win7 上启动时将优先加载同目录下的 shim，解决 "api-ms-win-core-path-l1-1-0.dll 缺失" 报错
- **`launcher.py`** — Windows 7 从"阻断错误"改为"实验性支持警告"，提示用户保留 shim DLL 或安装 KB2533623+UCRT

### 跨平台零配置构建

- **`gradlew`（Unix）新增 JDK 21 自动下载** — Mac/Linux 用户首次运行自动从清华镜像下载对应平台 JDK（支持 Linux x64、macOS x64、macOS ARM），无需手动安装
- **`.java-version`** — 新建文件，IntelliJ IDEA 2024.1+ 自动识别项目需要 JDK 21 并提供下载
- **`build_package.py` 流程解耦** — 跨平台包（construo）构建失败不再阻断 Windows 安装包生成，网络超时等不影响主平台

### 构建脚本改进

- **`build_package.py`** — ISS 版本号同步移入 step1，消除 step6 重复修改；提取 `restore_backups` 为独立方法；修复 `check_jdk17`→`check_jdk21` 等方法命名
- **`build_package_server.py`** — 启动时自动切换到脚本所在目录，确保双击运行时正确提供打包产物
- **`build_package_server.py`** — IP 检测补充 `172.16.0.0/12` 私有网段，过滤 IPv6 地址

### 代码清理

- **`Lwjgl3Launcher.java`** — 简化窗口聚焦代码，移除冗余类型转换

## 2026-06-02 — Linux 安装包改为自解压 .sh 一键安装

### 文档更新

- **`CONTRIBUTING.md`** — 输出成品表更新：Linux `.tar.gz` / `.deb` 列替换为 `.sh` 一键安装包

### 打包优化

- **`build_package.py`** — Linux 打包产物从 `.deb` + `.tar.gz` 改为单个自解压 `.sh` 文件（`.deb` 内嵌于脚本末尾），用户双击即可通过 `pkexec` 图形化安装，无需手动输入终端命令
- **保留 .deb** — 因蓝奏云不支持 `.sh` 分发，保留 `.deb` 作为蓝奏云等平台的分发格式，`.sh` 自解压安装包用于官网直链下载
- **`.gitignore`** — 新增 `/develop/output/*.sh` 忽略规则

### 脚本改进

- **`build_package.py`** — 控制台窗口异常关闭修复：入口 `__main__` 改为 try/finally 确保任何情况下（成功/异常）最后都会暂停等待用户按 Enter 退出；移除 `run()` 和 `main()` 中的重复暂停代码；异常时打印完整堆栈后再暂停
- **`develop/output/build_package_server.py`** — 新增局域网文件分享服务器脚本，双击即可运行，自动显示本机 IP 地址和端口，无需手动输入 `python -m http.server`
- **移除 `server.py`** — 重命名为 `build_package_server.py`，与 `build_package.py` 命名风格统一

## 2026-06-01 — 官网下载区支持 Linux + 打包脚本瘦身优化

### 打包优化

- **JAR 瘦身** — 构建 Windows 安装包时自动移除 `.so`/`.dylib` 等非 Windows 原生库，JAR 体积减少 20-30MB
- **镜像源加速** — JDK 下载地址从 GitHub 切换至南京大学镜像（`mirror.nju.edu.cn/adoptium/`）
- **Inno Setup 压缩增强** — 改为 `lzma2/ultra64` 提升安装包压缩率
- **修复 ISS 冗余引用** — 移除 `inno_setup.iss` 中重复的 JAR 引用，避免瘦身结果被覆盖
- **Linux 打包默认开启** — `build_package.py` 默认同时打包 Linux，不再需要 `--linux` 参数
- **jlink 补充 `java.desktop` 模块** — 修复 Linux 端文件选择器（Swing/JFileChooser）闪退问题

### 官网下载

- **`docs/index.html` / `docs/html/history_versions.html`** — 新增 Linux 下载面板，网格布局从 2 列扩为 3 列，旧版本缺少某平台字段时优雅降级提示
- **`docs/data/locales/*.json`（9 种语言）** — 新增 `linux_button` 字段
- **`docs/data/image.json`** — 新增 `download-linux` 路径配置（后因 CDN 加载问题回退，改用本地文件 + SVG fallback）
- **`docs/data/versions.json`** — v1.0.0-beta 新增 linux 下载入口；版本日志增加英文国际化
- **`docs/resource/image/download-linux.png`** — Linux 下载图标
- **README 介绍同步至官网** — 游戏介绍板块更新为 README 平台生态文案

### 提交规范

- **`develop/COMMIT_STYLE.md`** — 提交类型改为中文（新增/修复/优化/重构/测试/文档/构建），BREAKING 格式调整，移除重复行

## 2026-06-01 — Linux 桌面修复：文件选择器改用 zenity、.deb 目录修复

### Bug 修复

- **文件选择器 Ubuntu Wayland 崩溃/卡死** — Swing JFileChooser 在 Ubuntu 22.04 Wayland 下无论 GTK2/GTK3 均无法正常工作（GTK2 断言卡死、GTK3 段错误）。改用 `zenity --file-selection`（GNOME 原生文件选择器）完全绕过 Swing/GTK 栈，新增 `ZenityFileChooser.java` 实现 `NativeFileChooser` 接口。Linux 端在 `Lwjgl3Launcher` 中自动选择 ZenityFileChooser，其他平台仍用原有 DesktopFileChooser。
- **.deb 安装时 `/usr/lib/qingfeng/` 目录不存在** — `_make_tar()` 只写文件不写目录条目，dpkg 在解压时因父目录缺失而失败。改为在 tar 包中显式写入目录条目，按排序顺序先写目录再写文件。
- **官网 Linux 下载链接改为 .deb** — `versions.json` 中 GitHub/Gitee 下载路径从 `.tar.gz` 改为 `.deb`（蓝奏云暂不支持 .deb 托管，URL 暂时留空）。
- **Inno Setup 版本号修正** — `inno_setup.iss` 版本号由 `1.0.0` 改为 `1.0.0-beta`，与 gradle.properties 保持一致。

### 构建配置

- **`build.gradle`** — 移除 construo `roast` 块中的 `-Djdk.gtk.version=2` 参数（不再需要，文件选择器不依赖 Swing GTK）；更新 jlink 注释说明 `java.desktop` 模块实际用途（资源管理器 + 崩溃弹窗）。

### 打包脚本完善

- **`_make_tar()` 目录条目修复** — tar 包生成时自动收集所有父目录路径，按排序写入 DIRTYPE 条目。
- **`input("按下回车...")` 暂停** — `main()` 末尾增加暂停，防止非交互环境下控制台窗口在打包完成前自动关闭。

## 2026-05-31 — 架构审查报告：Python→Java 翻新综合评价

### 新增文档

- **`develop/REVIEW.md`** — 系统性架构审查与翻新进度追踪（合并原三份 review 文档）：
    - 架构评分 **7/10**（Python 版基线 3/10），10 大进步 + 11 项待改进
    - Python→Java 翻新路线图（P0~P4 优先排序）
    - 状态码对照表与资源路径映射

## 2026-05-31 — UiManager 拆分为三子管理器 + 桌面打包脚本便携化

### ⚠️ 新人必看：构建环境配置

具体可以看项目根目录文件:MANUAL_TEST.md 和 CONTRIBUTING.md\
本项目使用 **Gradle** 构建，桌面端打包需要额外工具。克隆后请按以下流程操作：

**快速开始（开发运行）：**
```
./gradlew lwjgl3:run        # 运行桌面端
./gradlew android:run        # 运行 Android 端（需连接设备）
```

**打包分发的完整流程**（`develop/output/build_package.py`），支持自动检测环境：

1. JDK 17 — 检测顺序：`JAVA_HOME` → `PATH` → `C:\Program Files\Java\` → 弹窗手动选择
2. Inno Setup 6 — 检测顺序：`ISCC` 环境变量 → `Program Files` → 弹窗手动选择
3. Android SDK — 从 `local.properties` 读取 `sdk.dir`
4. PyInstaller — 可选，用于构建 `launcher.exe`（也可直接用已有的）

首次运行时自动检测以上工具路径并保存到 `develop/output/build_config.env`（已 gitignore）。运行 `python develop/output/build_package.py` 后依次：更新版本号 → 编译 JAR（Windows 专用，排除其他平台） → 编译 APK → 组装启动器（含自动 jlink 生成最小 JRE）→ Inno Setup 打包 → 输出到 `develop/output/`。

> **注意**：Android SDK 路径在项目根目录 `local.properties` 中配置（格式：`sdk.dir=D\:/Android/Sdk`），此文件已 gitignore，新成员需自行创建。

---

### UiManager 拆分（5600 行 → 3 个子管理器）

- **ImageManager**：提取图片相关的全部逻辑（loadImageKind、createImage、addImage、updateImage、show/hide、delete、位置/大小操作），通过 `imageMap`/`imageKindMap`/`imageKindNameMap`/`imageStateMap` 管理
- **LabelManager**：提取标签相关逻辑（loadLabelKind、createLabel、addLabel、updateLabel 等），通过 `labelMap`/`labelKindMap`/`labelKindNameMap`/`labelStateMap`/`labelBaseTextMap` 管理
- **ButtonManager**：提取按钮相关逻辑（loadButtonKind、createButton、addButton、updateButton、点击回调等），通过 `buttonMap`/`buttonKindMap`/`buttonKindNameMap`/`buttonStateMap`/`buttonBaseTextMap`/`buttonClickCallbackMap` 管理
- **兼容性**：三个管理器通过 `compatibilityMap` 保留旧的外部直接访问 `imageMap`/`labelMap`/`buttonMap` 的路径，拆分解耦后零调用点改动
- 内部类 `CustomImage`/`CustomLabel`/`CustomTextButton` 改为 `static final class`，使外部类在包内可访问

### 桌面端打包脚本入库

- **`lwjgl3/setup/` 目录纳入版本控制**：包含了 `inno_setup.iss`、`launcher.py`、`launcher.spec`、`setup.ico`、`console.ico`
- **`inno_setup.iss` 便携化**：移除 `D:\File\idea\...` 硬编码路径，全部改为相对路径；`OutputDir=.\dist`、`SetupIconFile=.\setup.ico`、`PrivilegesRequired=lowest`
- **`.gitignore` 新增规则**：排除 `lwjgl3/setup/build/`、`lwjgl3/setup/dist/`、`lwjgl3/setup/qingfeng_setup_windows.exe` 等构建产物

### 便携打包脚本 `develop/output/build_package.bat`

- **环境自动检测 + 持久化**：`build_config.env` 缓存 JDK 和 Inno Setup 路径，支持自动检测 + 手动选择弹窗
- **运行时目录自举**：自动创建 `dist/launcher/lib/jar/`、自动生成 `set.json`、自动 `jlink` 生成最小 JRE（约 40MB，包含 java.base/java.desktop/java.logging 等模块）
- **6 步构建流程**：① 更新版本号 → ② `lwjgl3:jar` → ③ `android:assembleRelease` → ④ 组装启动器 → ⑤ Inno Setup 打包 → ⑥ 复制成品到 `develop/output/`
- **成品命名**：`qing-feng_setup_android_v{ver}-{type}.apk` + `qing-feng_setup_windows_v{ver}-{type}.exe`
- **配置文件模板**：`develop/output/build_config.env.template` 已入库，供参考

### Python 版打包脚本（替代 .bat）

`develop/output/build_package.py` 是同功能 Python 版本，完全避免 cmd.exe 编码问题：

- **直接运行**：`python develop/output/build_package.py`
- **打包为 .exe**：`pyinstaller --onefile --console develop/output/build_package.py`
- 自动检测 JDK/Inno Setup/Android SDK/PyInstaller，结果持久化到 `build_config.env`
- 支持 `--config-only` 参数仅检测环境不打包

> 建议优先使用 Python 版。`.bat` 版保留但限于 Windows cmd.exe 在 UTF-8 BOM + 中文环境下有已知解析 bug。

#### 修复：gradlew.bat 尾部文本损坏导致 Windows 构建崩溃

`gradlew.bat` 末尾 `:omega` 标签后残留了多余文本 `\r`（字面反斜杠 + r），导致 cmd.exe 将其解析为命令执行，报错 `文件名、目录名或卷标语法不正确`。任何通过 `gradlew.bat` 的 Gradle 构建（包括 Python 脚本的 `subprocess` 调用）均受影响。已删除尾部垃圾字符。

#### 修复：build_package.py 非交互模式 EOFError

打包成功后的 `input("按 Enter 键退出...")` 在 CI/后台等非 TTY 环境抛 `EOFError`，导致脚本以非零退出码结束。已加 `try/except (EOFError, OSError)` 保护。

#### 修复：发布类型提示在部分 Windows 终端显示异常

提示文字中使用 `/` 分隔选项（`beta / alpha / release`）在部分 Windows 终端中渲染为 "betaherelease"。已改为中文顿号分隔。

#### 新增：LICENSE 加入 Android APK 打包

LICENSE 复制到 `assets/` 目录，Android 构建时自动打包进 APK，桌面端同步可用。

#### 移除：淘汰 build_package.bat

Python 版已稳定，`.bat` 版因 Windows cmd.exe 在 UTF-8 BOM + 中文环境下的解析 bug 不再维护，已删除。

## 2026-05-31 — GameController 委托方法消除，直调 GameSessionManager

- **消除 GameController 的 5 个委托包装方法**：移除 `loadGame`、`enterGame`、`quitGame`、`isInGame`、`playNewStory`，改为直接暴露 `getGameSessionManager()` getter
- **全部调用点更新**（8 文件）：`GameRole.java`、`GameMenu.java`、`MenuLoad.java`、`MenuList.java`、`ControllerInputHandler.java`、`UniversalInputHandlerFunction.java`、`VirtualInputHandler.java` 统一改为 `gameController.getGameSessionManager().xxx()` 模式
- **清理未使用导入**：`GameController.java` 移除 `FileHandle`、`Role`、`Hoster` 三个不再需要的 import

### UI 架构改进

- **Layout Group 支持（a）**：`addLayout` 现在将同一布局的所有 Actor 归入一个 scene2d `Group`，通过 `layoutGroupMap` 跟踪。`showLayout`/`hideLayout` 直接调用 `group.setVisible()`（O(1)），不再逐元素迭代。`deleteLayout`/`deleteAllObject`/`dispose` 同步清理 Group。未通过 `addLayout` 添加的元素仍然兼容旧逐元素路径
- **按钮点击回调（b）**：新增 `setButtonClickCallback(tag, Runnable)` 方法，在按钮点击时同时触发回调 + 保留 `isButtonClicked()` 状态标记。`createButton` 的 clickRunnable 增加回调调用，`deleteButton` 同步清理回调映射。现有轮询代码无需改动，逐步迁移即可

### 编码修复

- **修复 LWJGL3 窗口标题中文乱码**：`lwjgl3/build.gradle` 缺少 UTF-8 编译编码配置，`setTitle("氢风")` 在 Windows 默认 GBK 编码下编译产生乱码。将 `compileJava.options.encoding = 'UTF-8'` 提升到根 `build.gradle` 的 `configure(subprojects...)` 块中，对所有非 Android 子项目生效；同步添加
  `compileTestJava.options.encoding = 'UTF-8'`；移除 `core/build.gradle` 中重复的局部配置

### 窗口聚焦优化

- **文件选择器关闭后自动聚焦游戏窗口**：`FileChooser` 新增 `setWindowFocusRequester` 注入回调，在 `onFileChosen`/`onCancellation`/`onError` 三种结束路径均通过 `Gdx.app.postRunnable` 触发窗口聚焦
- **LWJGL3 端注入 GLFW 聚焦**：`Lwjgl3Launcher.getDefaultConfiguration()` 中注入实现，通过 `((Lwjgl3Application) Gdx.app).getWindow().focusWindow()` 将游戏窗口调到前台

## 2026-05-31 — 日志标签统一、全量 Javadoc 与方法注释补全

### 日志标签统一（25+ 文件）

- 消除所有带 `Imp` 后缀的日志标签：`UserConfigManagerImp`、`ThemeManagerImp`、`TextManagerImp`、`LanguageManagerImp`、`GameInfoManagerImp`、`GameTemplateManagerImp`、`GameStoryManagerImp`、`GameRoleManagerImp` 等 → 对应类名
- 修复错误类名标签：`UserGameConfigManagerImp` → `GameUserConfigManager`；`JsonServiceImp` → `JsonUtils`；`gameRoleManagerImp`（小写）→ `GameRoleManager`
- 修复跨类误用标签：`UiManager.java` 中 `LogUtils.error("GameStateServiceImp", ...)` → `"UiManager"`
- 修复 `GraphicsManager.java` 中 `LogUtils.error` 参数顺序颠倒（tag/message 互换）的 bug

### Bug 修复

- **GamePlay.java 空指针修复**：`layout` 字段在 `init()` 中从未初始化，`render()` 和 `dispose()` 使用时始终为 null。改为从 `gameStateDataContainer.getLayoutConfig()` 获取
- **硬编码字符串 → 常量引用**：`ConfigBasic.java` 中的 `"back"` → `UniversalKey.BUTTON_BACK`；`GameMenu.java` 中的 `"start"/"quit"` → `RequirementUiKey.MENU_MAIN_BUTTON_START` / `UniversalKey.BUTTON_QUIT`；`GameRole.java` 中的 `"back"` → `UniversalKey.BUTTON_BACK`

### 全量 Javadoc 与方法注释补全

- 为全部 ~92 个 Java 源文件的公开 API 方法添加 `/** */` Javadoc（含 `@param`、`@return`）
- 为全部私有方法添加 `/** */` 功能描述注释
- 覆盖范围：8 个 GameRender 实现、6 个核心 Controller、7 个 Manager、4 个 GameManager、5 个 GameLogic 类、AudioManager（992 行）、GraphicsManager（836 行）、UiManager（4221 行 / ~180 方法）、MessageBox、20 个 Event/Handler/Interact 类、36 个 Data/Define/Parser 类

### 命名规范

- `FileUtils.java`：`directoryStructure` → `DIRECTORY_STRUCTURE`（static final 常量 UPPER_SNAKE）
- `LogUtils.java`：`fileDateFormat` → `FILE_DATE_FORMAT`

## 2026-05-31 — UpdateController 重构与 deltaTime 统一

### UpdateController 重构

- **消除重复代码**：移除与 `parseVersion()` 逻辑重复的 `parseVersionOrThrow()`，`compare()` 改为直接调用 `parseVersion()`
- **网络版本检测归位**：将散落在 `Init.java` 中的 HTTP 版本检测逻辑（270+ 行，含递归重试）移入 `UpdateController`，新增 `checkWebVersion()` / `requestWebVersion()` 方法。`Init.java` 的 `initStop()` 简化为一行 `updateController.checkWebVersion()`
- **命名与日志修正**：`InternalVersionFilePath` → `internalVersionFilePath`；日志标签 `"UpdateControllerImp"` → `"UpdateController"`；`dispose()` 去除无意义的 try/catch 空壳

### deltaTime 传递链路统一

- **GameRenderer**：`updateFrame()` 和 `render()` 改为从参数接收 `float deltaTime`，不再各自内部调用 `Gdx.graphics.getDeltaTime()`
- **GameController**：`run()` 改为 `run(float deltaTime)`，向下游传递
- **Main**：`render()` 中在 `mainRender(deltaTime)` 入口处取一次 `Gdx.graphics.getDeltaTime()`，逐层传入 `gameController.run(deltaTime)` 和 `stage.act(deltaTime)`。delta 源头统一为一处，为后续帧率控制/暂停时间缩放做准备

### 文档增强

- 大幅增强 `develop/REVIEW.md`：P0-P4 每条展开为完整实现指引表格，新增架构审查、代码质量问题、资源映射、技术对比等章节

## 2026-05-31 — UI 样式系统增强与多 bug 修复

### UiManager 样式运行时更新支持

- `updateImage`、`updateLabel`、`updateButton` 由 `private` 提升为 `public`，支持在 UI 创建后动态切换样式（kind）、位置、大小
- 新增 `imageKindNameMap`、`labelKindNameMap`、`buttonKindNameMap` 追踪各元素的当前样式名，确保 `deleteAllObject` 时完整清理
- `updateImage` 新增 `TextureRegionDrawable` 热切换：修改 kind 后即时更新显示的纹理区域
- `updateLabel` 新增对齐标志（`fontFlag`: W/E/N/S 及 _TYPING 变体）、内边距支持（`padX/padY/pad`）、字体颜色、背景图切换
- `updateButton` 新增样式（`up/down/disabled/over`）、文字、字体、颜色热切换
- `CustomTextButton` 新增 `getButtonStyle()` 公开方法，支持按钮样式运行时修改

### addLayout 逻辑重构

- 图片/标签/按钮的添加逻辑改为：**无条件创建全部元素**，再根据 `getShow()` 隐藏不需要显示的。旧逻辑仅在 `show=true` 时才创建元素，导致反复 `addLayout/deleteLayout` 时元素注册不一致
- 修复由此引发的 `deleteLayout` 报 `"标签不存在 (tag): path"` 的误报（因之前跳过的元素从未被注册到 UiManager）

### Map 顺序一致性保障

- **MergeUtils**: `mergedMap`、`deepCopyMapGeneric` 的返回值由 `HashMap` 改为 `LinkedHashMap`，保证合并后的 Map 按插入顺序迭代
- **JsonUtils**: `jsonStringToObject` 添加 `Feature.OrderedField`，使 JSON 反序列化保持字段声明顺序
- **JsonEntity**: `deepCopy` 改为 `LinkedHashMap`，保持深拷贝后的字段顺序
- **LayoutManager**: `loadLayoutUiImage` 中的 `imageMap` 改为 `LinkedHashMap`，确保 UI 图像的 z-order 按配置顺序渲染

### MessageBox 遮盖层残留修复

- **修复 MessageBox 遮盖层（mask）在游戏→主菜单状态切换后残留的问题。** 根本原因：`handleAsk` 中 `onYes.run()`（触发 `quitGame` → `disposeResource` → `messageBox.dispose()` 清空 `askMap`）在 `hideAsk` 之前执行，导致 `hideAsk` 因 `askMap` 为空而跳过 `removeMaskLayer`，遮盖层永久留在 Stage 上
- `MessageBox.dispose()` 增加遮盖层显式移除逻辑：遍历 `showingBoxTypeStack` 并调用 `uiManager.getMaskLayer().remove()`，同时清空 `showingBoxTypeStack`、`enterButtonTagStack`、`escapeButtonTagStack`
- 日志验证：`removeMaskLayer 移除遮盖` → `hideAsk 移除弹窗` → `disposeResource messageBox销毁成功`，状态切换后遮盖层正确移除

### MenuList 游戏封面渲染优化

- `refreshGameCover()` 改为 `updateImage` + `showImage/hideImage` 模式，替代旧的 `deleteImage + addImage` 模式
- 消除页面切换时的封面闪烁问题，提升翻页流畅度

## 2026-05-30 — 构建配置调整

- Gradle wrapper 镜像源切换：`services.gradle.org` → `mirrors.cloud.tencent.com`，解决国内网络 SSL 握手失败问题
- 构建环境改用 JDK 17（系统默认 JDK 8 证书库过旧，无法验证境外 HTTPS 证书）
- 删除 `gradle/gradle-daemon-jvm.properties`（内含硬编码的 foojay JDK 21 下载链接），根除成员首次构建时自动连接 `api.foojay.io` 导致超时的问题
- 注释 `settings.gradle` 中的 `foojay-resolver-convention` 插件，彻底禁用 Gradle 自动下载 JDK 机制。原因：国内网络无法访问 `api.foojay.io`，团队成员在首次构建时卡在 JDK 下载阶段，出现 `Connection timed out: getsockopt` 错误
- 在 `build.gradle` 的 `subprojects.repositories` 块中新增阿里云镜像 `https://maven.aliyun.com/repository/public/`，解决子项目依赖（gdx、gdx-platform、gdx-controllers 等）下载超时问题。此前阿里云镜像仅配置在 `buildscript.repositories` 中，只对 Gradle 插件生效，子项目依赖仍走 `mavenCentral()` 境外源
- 修改 `gradlew.bat`，新增 JDK 17+ 自动检测与下载功能：运行 Gradle 前检测系统 JDK 版本，如果低于 17 则自动从阿里云 Adoptium 镜像下载 JDK 17 到 `.jdk/` 目录并设置 `JAVA_HOME`。新成员克隆后直接 `./gradlew lwjgl3:run` 即可，零手动配置
- `.gitignore` 添加 `.jdk/` 忽略规则，防止自动下载的 JDK 被提交到仓库

## 2026-05-29 — 代码规范统一与架构优化

- 拆解上帝类 `GameController`，将数据加载、资源加载、会话管理、用户配置加载拆分为独立类
- 新增 `GameDataLoader`、`GameResourceLoader`、`GameSessionManager`、`GameUserConfigLoader`
- 移除 `RenderInstanceContent`，将渲染注册逻辑整合到 `InstanceContent`
- 优化类名与常量命名规范（`GameSonState` → `GameSubState` 等）
- 事件数据处理优化：简化 `EventPopGameState`、`EventResetGameState`，规范 `EventPushGameState`、`EventSetGameState`
- 代码风格统一优化（3 轮）：对齐 Allman 风格、修饰符顺序、导入顺序、日志格式等
- 完善 `CODING_STYLE.md` 代码规范文档
- 新增 `README.md` 项目说明文档

## 2026-05-25 — 代码质量改进

- 修复多处潜在漏洞
- 添加 `@Nullable`、`@Override` 等注解
- 增加 `final` 修饰词，强化不可变性
- 新增 `ColorConfig`、`PictureInfo`、`GifInfo`、`ButtonInfo`、`ImageInfo`、`LabelInfo` 数据类
- 调整包结构：`event`、`game` 相关类移动到 `data` 包；`box` 相关类移动到 `engine` 包
- 优化 `GameInfoKey` 常量定义（54 处变更）

## 2026-05-24 — 功能完善与工具升级

- 修正 `playNewStory` 流程，增加 `TreeStructure` 和 `Page` 的 setter 校验返回值
- Android Gradle 插件升级 8.12.0 → 8.13.0
- Gradle wrapper 工具版本升级

## 2026-05-16 — 构建配置调整

- JDK toolchain 版本修改
- 新增 `gradle-daemon-jvm.properties`

## 2026-05-12 — 故事系统完善

- 完善 `GameStoryManagerImp`，重构故事管理器实现（178 行变更）
- 新增 `Page`、`TextObject` 数据类
- 完善 `TreeStructureInfo`，增强故事树节点信息
- 新增 `GameInfoManager` 接口与实现
- `Main` 主函数增加崩溃弹窗显示，方便用户求助（`CrashDialogShower`）
- 优化 `MessageBoxImp`、`UiManagerImp`，重构消息框与 UI 管理器
- 重构 `Init` 渲染器（112 行变更）
- 新增 Android 崩溃对话框支持（`AndroidLauncher`）
- 扩展 `FileName`、`PathName`、`PathType` 路径常量定义

## 2026-05-10 — 剧情树与多人游戏准备

- 完成剧情树的区块加载方法（`RootStructure` / `BranchStructure` / `NodeStructure` / `LeafStructure`）
- 提取 `PlayerData`、`GameDataContent` 为多人游戏做准备
- 新增文件选择器（`FileChooser`）和资源管理器打开工具（`FileExplorer`）
- 重构故事树：删除旧的 `Root` / `Node` / `Branch` 类，替换为 `imp` 包下的结构化实现
- 新增 `GameInfoManager`、`FilePathConfig`
- 配置文件统一命名：`theme_config.json`、`language_config.json`、`game_config.json` 等
- Android 端新增 `AndroidExplorerOpener` 实现
- 桌面端新增 `DesktopExplorerOpener` 实现

## 2026-05-07 — 渲染性能优化

- **GraphicsManager**：大纹理优化，重构纹理管理（475 行变更，+360/-115）
- **UiManager**：废弃旧隔离纹理模式，大纹理优化显著提升渲染性能（528 行变更，+370/-210）
- 修正官网下载链接，添加发行版本下载入口
- 新增多语言下载提示字段

## 2026-05-06 — 仓库合并

- **源码仓库与官网仓库合并**，统一管理
- 移动网站部署目录

## 2026-04-23 — 网站功能完善

- 修复历史版本列表无法下载的 bug
- 添加讨论区链接
- 增加更多下载途径：GitHub、蓝奏云

## 2026-04-22 — 多语言支持

- 添加官网多语言支持（中/英/日/韩/俄/德/法/葡）
- 修正 README.md

## 2026-04-21 — 官网重构

- 优化 HTML 代码，图片与下载链接 JSON 配置化
- 整理仓库结构

## 2026-03 — 网站初期建设

- 新增字符型版本字段（2026-03-31）
- 网站小更新（2026-03-16）
- 网站图标更新（2026-03-05）

## 2026-02-28 — 初始提交

- 项目初始提交，基于 libGDX 框架搭建跨平台工程结构
