# 氢风 JSON 配置标准总览

> **文档定位**：项目内所有 JSON 配置格式的权威参考手册。供开发者快速查阅字段含义、默认值、解析行为；供新人理解项目 JSON 体系的全貌。
>
> **文档结构**：
> - 按 `主题系统 → UI 种类定义 → 布局 JSON → 特殊 UI 配置 → 页面配置 → 脚本指令 → 语言文件 → 配置定义 → 游戏定义 → 目录结构 → 配置标准` 顺序编排
> - 顶部设**格式速查表**，提供所有 JSON 格式的一站式概览
> - 每种 JSON 格式独立一节，包含：文件路径、字段表（名称/类型/必需/默认值/说明）、解析类引用
>
> **更新规范**：
> 1. 【必须】更新 `develop/CHANGELOG.md` 记录本次变更
> 2. 【必须】遵循"如何新增一种 JSON 标准"章节的流程操作
> 3. 【必须】新增的 JSON 格式在**格式速查表**中有一行记录
> 4. 【必须】为新增的 JSON 格式编写独立章节，包含字段类型、默认值、解析类
> 5. 【如果】新增/删除 JSON 文件 → 同步更新 `DOCUMENTATION_INDEX.md`
> 6. 【如果】修改脚本引擎相关 JSON → 同步更新 `develop/SCRIPT_INTERNAL_STANDARD.md`

---

## 目录

- [如何新增一种 JSON 标准](#如何新增一种-json-标准)
- [格式速查表](#格式速查表)
- [一、主题系统](#一主题系统)
  - [1.1 theme_config.json（主题词典）](#11-theme_configjson主题词典)
  - [1.2 theme.json（主题配置）](#12-themejson主题配置)
  - [1.3 ui_config.json（UI 种类注册）](#13-ui_configjsonui-种类注册)
- [二、UI 种类定义](#二ui-种类定义)
  - [2.1 font.json（字体定义）](#21-fontjson字体定义)
  - [2.2 Label Kind JSON（标签样式）](#22-label-kind-json标签样式)
  - [2.3 Button Kind JSON（按钮样式）](#23-button-kind-json按钮样式)
  - [2.4 Image Kind JSON（图像样式）](#24-image-kind-json图像样式)
- [三、布局 JSON（Layout）](#三布局-jsonlayout)
  - [3.1 Layout 顶层结构](#31-layout-顶层结构)
  - [3.2 背景音乐配置](#32-背景音乐配置)
  - [3.3 背景图片配置](#33-背景图片配置)
  - [3.4 graphics 条目（picture / gif）](#34-graphics-条目picture--gif)
  - [3.5 ui.image 条目](#35-uiimage-条目)
  - [3.6 ui.label 条目](#36-uilabel-条目)
  - [3.7 ui.button 条目](#37-uibutton-条目)
- [四、特殊 UI 配置](#四特殊-ui-配置)
  - [4.1 message_box.json（弹窗布局）](#41-message_boxjson弹窗布局)
- [五、游戏系统](#五游戏系统)
  - [5.1 game.json（游戏定义）](#51-gamejson游戏定义)
  - [5.2 game_config.json（游戏配置）](#52-game_configjson游戏配置)
- [六、脚本指令系统（详见 SCRIPT_INTERNAL_STANDARD.md）](#六脚本指令系统详见-script_internal_standardmd)
  - [6.1 指令 JSON 通用结构](#61-指令-json-通用结构)
- [七、故事树系统](#七故事树系统)
  - [7.1 TreeStructureInfo JSON](#71-treestructureinfo-json)
- [八、值系统（详见 SCRIPT_INTERNAL_STANDARD.md）](#八值系统详见-script_internal_standardmd)
  - [8.1 LogicValue JSON 通用结构](#81-logicvalue-json-通用结构)
- [九、外部应用配置](#九外部应用配置)
  - [9.1 app_config.json（应用配置）](#91-app_configjson应用配置)
- [十、版本与更新系统](#十版本与更新系统)
  - [10.1 app_version.json（版本信息）](#101-app_versionjson版本信息)
  - [10.2 update_config.json（更新配置）](#102-update_configjson更新配置)
- [十一、语言系统](#十一语言系统)
  - [11.1 language_config.json（语言词典）](#111-language_configjson语言词典)
  - [11.2 language.json（语言定义）](#112-languagejson语言定义)
  - [11.3 语言块文件](#113-语言块文件)
  - [11.4 文本插值语法](#114-文本插值语法)
- [十二、用户配置系统](#十二用户配置系统)
  - [12.1 启动器 user_config.json](#121-启动器-user_configjson)
  - [12.2 游戏 user_config.json](#122-游戏-user_configjson)
- [十三、游戏运行时配置](#十三游戏运行时配置)
  - [13.1 role_config.json（角色配置）](#131-role_configjson角色配置)
  - [13.2 role.json（角色定义）](#132-rolejson角色定义)
  - [13.3 script_config.json（脚本配置）](#133-script_configjson脚本配置)
  - [13.4 template_config.json（模板配置）](#134-template_configjson模板配置)
- [十四、故事树块文件](#十四故事树块文件)
  - [14.1 故事树 JSON 结构](#141-故事树-json-结构)
  - [14.2 连接对象（TreeStructureInfo）](#142-连接对象treestructureinfo)
- [十五、页面行为系统（详见 SCRIPT_INTERNAL_STANDARD.md）](#十五页面行为系统详见-script_internal_standardmd)
  - [15.1 behavior.json 结构](#151-behaviorjson-结构)
- [十六、可调用脚本文件（详见 SCRIPT_INTERNAL_STANDARD.md）](#十六可调用脚本文件详见-script_internal_standardmd)
  - [16.1 脚本文件 JSON 结构](#161-脚本文件-json-结构)
- [十七、目录结构配置](#十七目录结构配置)
  - [17.1 directory_structure.json](#171-directory_structurejson)
- [附录一：解析类一览](#附录一解析类一览)
- [附录二：默认值汇总](#附录二默认值汇总)

---

## 如何新增一种 JSON 标准

当你在项目中新增一种 JSON 配置文件时，按以下流程操作：

### 第一步：定位解析代码

查找（或用 `Grep` 搜索）负责解析该 JSON 的 Java 类：
- 解析器在 `util/json/parser/` 下：`JsonXxxParser.java`
- Info/Config 类在各自包中：`ui/kind/xxx/`, `data/game/`, `graphic/model/` 等
- 解析调用的入口通常在 `Manager` 类（`ThemeManager`, `LayoutManager`, `ButtonManager` 等）

关键问题：
- 解析类在哪里？用什么方法读取 JSON？
- 使用 `JsonEntity` 的 `getString()` / `getInt()` / `getFloat()` / `getBoolean()` / `getJsonEntityByKey()` 的哪几种？
- 是否使用了 `Gson` 直接映射到 POJO？（本项目统一使用 `JsonEntity` 作为抽象层）

### 第二步：归类

判断该 JSON 属于以下哪个类别：
- **主题系统**（`assets/asset/theme/`）→ 归入第一章
- **UI 种类定义**（`asset/ui/` 下的 `label/` `button/` `image/` 等）→ 归入第二章
- **布局 JSON**（`asset/layout/`）→ 归入第三章
- **脚本/指令**（`script/` 相关）→ 归入第六章
- **值系统** → 归入第八章
- **其他配置** → 酌情新增章节

### 第三步：记录字段

为每个字段记录以下信息：
1. **字段名**：JSON 中的键名
2. **类型**：String / int / float / boolean / Object / Array
3. **必需/可选**：解析时是否必须存在
4. **默认值**：字段缺失时 Java 代码中使用的默认值
5. **解析逻辑**：读取方式和特殊行为（如 `fontColor` 要求 hex 字符串）
6. **示例值**：一个真实的用法示例

### 第四步：更新本文档

1. 在对应章节添加新格式的说明
2. 如果新增了章节，更新目录
3. 更新 [附录一：解析类一览](#附录一解析类一览) 和 [附录二：默认值汇总](#附录二默认值汇总)

---

## 格式速查表

| # | JSON 格式 | 文件位置（相对 `assets/`） | 解析类 | 章节 |
|---|-----------|--------------------------|--------|------|
| 1 | 主题词典 | `asset/theme/theme_config.json` | `ThemeManager.parseThemePath()` | 1.1 |
| 2 | 主题配置 | `asset/theme/{theme_name}/theme.json` | `ThemeManager` | 1.2 |
| 3 | UI 注册 | `asset/theme/{theme}/asset/ui/ui_config.json` | `UiManager` | 1.3 |
| 4 | 字体定义 | `asset/theme/{theme}/asset/ui/font/{name}/font.json` | `UiManager` | 2.1 |
| 5 | 标签种类 | `asset/theme/{theme}/asset/ui/label/{name}.json` | `LabelManager.loadLabelKind()` | 2.2 |
| 6 | 按钮种类 | `asset/theme/{theme}/asset/ui/button/{name}.json` | `ButtonManager.loadButtonKind()` | 2.3 |
| 7 | 图像种类 | `asset/theme/{theme}/asset/ui/image/{name}.json` | `ImageManager.loadImageKind()` | 2.4 |
| 8 | 页面布局 | `asset/theme/{theme}/asset/layout/{name}.json` | `LayoutManager.loadLayout()` | 3.1~3.7 |
| 9 | 弹窗布局 | `asset/theme/{theme}/asset/ui/message_box/message_box.json` | `MessageBox` | 4.1 |
| 10 | 游戏定义 | `game/{game_id}/game.json` | — | 5.1 |
| 11 | 游戏配置 | `game/game_config.json` | — | 5.2 |
| 12 | 脚本指令 | 故事脚本中的 JSON 指令 | 详见 `SCRIPT_INTERNAL_STANDARD.md` | 6.1 |
| 13 | 触发器指令 | 触发器定义中的 JSON | 详见 `SCRIPT_INTERNAL_STANDARD.md` | 6.1 |
| 14 | 值指令 | 条件/表达式中的 JSON | 详见 `SCRIPT_INTERNAL_STANDARD.md` | 8.1 |
| 15 | 参数信息 | 指令参数中的嵌套 JSON | 详见 `SCRIPT_INTERNAL_STANDARD.md` | 6.1 |
| 16 | 故事树节点 | 故事树 JSON | `TreeStructureInfo` | 7.1 |
| 17 | 应用配置 | 外部 `app_config.json` | `ThemeManager.saveProcessColorToAppConfig()` | 9.1 |
| 18 | 版本信息 | `asset/app_version.json` | `UpdateChecker` | 10.1 |
| 19 | 更新配置 | `asset/update_config.json` | `UpdateChecker` | 10.2 |
| 20 | 语言词典 | `asset/language/language_config.json` | `LanguageManager` | 11.1 |
| 21 | 语言定义 | `asset/language/{lang}/language.json` | `LanguageManager` | 11.2 |
| 22 | 语言块 | `asset/language/{lang}/{block}.json` | `LanguageManager` | 11.3 |
| 23 | 启动器用户配置 | 外部 `user_config.json` | `UserConfigManager` | 12.1 |
| 24 | 游戏用户配置 | `{game}/user_config.json` | `GameUserConfigManager` | 12.2 |
| 25 | 角色配置 | `{game}/role_config.json` | `GameRoleManager` | 13.1 |
| 26 | 角色定义 | `{game}/{role}/role.json` | `GameRoleManager` | 13.2 |
| 27 | 脚本列表 | `{game}/{role}/script_config.json` | `GameScriptManager` | 13.3 |
| 28 | 模板列表 | `{game}/{role}/template_config.json` | `GameTemplateManager` | 13.4 |
| 29 | 故事树块 | `{game}/asset/story/{block}.json` | `GameStoryManager` | 14.1 |
| 30 | 页面行为 | `{game}/asset/page/{id}/behavior.json` | `PageBehavior` | 15.1 |
| 31 | 可调用脚本 | `{game}/asset/script/{name}.json` | `Script` | 16.1 |
| 32 | 目录结构 | `asset/directory_structure.json` | 手动验证 | 17.1 |

---

## 一、主题系统

### 1.1 theme_config.json（主题词典）

**位置**：
- 启动器：`assets/asset/theme/theme_config.json`
- 游戏：`{game_path}/{game_id}/asset/theme/theme_config.json`

将主题目录名映射为主题配置（显示名称 + 路径类型）。每个 key 是主题目录名，value 是嵌套对象。

**解析**：`ThemeManager.parseThemePath()` 读取

| 字段 | 类型 | 必需 | 默认值 | 说明 |
|------|------|------|--------|------|
| `{dir_name}` | Object | — | — | 键为主题目录名，值为主题配置对象 |
| `{dir_name}.name` | String | 是 | — | 主题显示名称 |
| `{dir_name}.kind` | String | 否 | `external` | 路径类型：`internal`（官方，Internal 句柄直读）或省略（外部主题，External 句柄） |

**示例**：
```json
{
  "default_theme": {
    "name": "默认主题",
    "kind": "internal"
  }
}
```

**无主题时的自动修复行为**：
1. 如果 `theme_config.json` 不存在，从 internal 复制到 external
2. 如果指定的主题目录不存在，回退到 `default_theme`（Internal 句柄，不再复制目录到 external），修复用户配置并融合内部词典补回官方条目

---

### 1.2 theme.json（主题配置）

**位置**：
- 启动器主题：`assets/asset/theme/{theme_name}/theme.json`
- 游戏主题：`{game_path}/{game_id}/asset/theme/{theme_name}/theme.json`

**解析**：`ThemeManager`（`parseJson` → `loadVersionFromJson` → `loadFontFromJson` → `loadFontUseSizeFromJson` → `loadColorFromJson`）

**字段含义**：所有字段在启动器主题和游戏主题中的含义一致（都是描述该主题自身的属性），但作用域不同——启动器主题全局生效，游戏主题仅在该游戏会话内生效。

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `name` | String | — | — | 该主题的人类可读名称 |
| `version` | String | 必需 | — | 该主题的版本号，如 `"1.0.0"` |
| `icon` | String | — | — | 主题图标的文件名，位于主题根目录下 |
| `font` | String | 必需 | — | 默认字体名称，引用 `ui_config.json` 中注册的字体 |
| `fontUseSize` | Array\<Float\> | 可选 | `Numeric.getFontNormalScaleList()` | 字体预缓存缩放系数列表，如 `[0.8, 1.2, 1.5]` |
| `primaryColor` | String (hex) | 可选 | `"#000000FF"` | 主题主色调，`Color.valueOf()` 解析 |
| `secondaryColor` | String (hex) | 可选 | `"#000000FF"` | 主题辅色 |
| `fontColor` | String (hex) | 可选 | `"#000000FF"` | 主题默认字体色 |

**注意**：启动器主题（非游戏主题）初始化时，会将 `primaryColor` 写入外部 `app_config.json` 的 `process_color` 字段，供进度条使用。

**示例**：
```json
{
  "name": "默认主题",
  "version": "1.0.0",
  "icon": "icon.png",
  "font": "default",
  "fontUseSize": [0.8, 1.2, 1.5],
  "primaryColor": "#3F48CCFF",
  "secondaryColor": "#FDA1FFFF",
  "fontColor": "#000000FF"
}
```

---

### 1.3 ui_config.json（UI 种类注册）

**位置**：`assets/asset/theme/{theme}/asset/ui/ui_config.json`

**解析**：`UiManager`（按分类读取种类名称列表，然后逐个加载对应文件）

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `button` | Array\<String\> | — | 要加载的按钮种类名称列表 |
| `label` | Array\<String\> | — | 要加载的标签种类名称列表 |
| `image` | Array\<String\> | — | 要加载的图像种类名称列表 |
| `font` | Array\<String\> | — | 要加载的字体名称列表 |
| `messageBox` | Array\<String\> | — | 要加载的弹窗种类名称列表 |

**加载规则**：列表中的名称会被拼接为文件路径查找：
- **button**: `asset/ui/button/{name}.json`
- **label**: `asset/ui/label/{name}.json`
- **image**: `asset/ui/image/{name}.image.json`
- **font**: `asset/ui/font/{name}/font.json`
- **messageBox**: `asset/ui/message_box/message_box.json`

**示例**：
```json
{
  "button": ["default", "message_box.button"],
  "label": ["default", "message_box.content", "message_box.title"],
  "image": ["select_frame.image", "unselect_frame.image"],
  "font": ["default"],
  "messageBox": ["default"]
}
```

---

## 二、UI 种类定义

### 2.1 font.json（字体定义）

**位置**：`asset/ui/font/{name}/font.json`

**解析**：`UiManager`（使用 `BitmapFont` 加载 `.fnt` 文件）

| 字段 | 类型 | 必需 | 默认值 | 说明 |
|------|------|------|--------|------|
| `name` | String | 必需 | — | 字体名称，与 `font.json` 所在目录名一致 |
| `path` | String | 必需 | — | BitmapFont 的 `.fnt` 文件路径，相对 `font/{name}/` 目录 |
| `scale` | float | 必需 | — | 字体缩放系数 |

**示例**：
```json
{
  "name": "default",
  "path": "Source_Han_Sans.fnt",
  "scale": 1.4
}
```

---

### 2.2 Label Kind JSON（标签样式）

**位置**：`asset/ui/label/{name}.json`

**解析**：`LabelManager.loadLabelKind()` → `LabelKind`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `name` | String | 必需 | — | 标签种类名称，在 `ui_config.json` 中引用 |
| `font` | String | 必需 | — | 使用的字体名称 |
| `image.background` | String | 可选 | — | 九宫格背景图片文件名，相对 `resource/image/` |
| `borderScale` | float | 可选 | `1.0` | 九宫格边框缩放系数 |
| `fontColor` | String (hex) | 必需 | — | 默认字体颜色，`Color.valueOf()` 解析 |
| `backgroundColor` | String (hex) | 可选 | `Color.CLEAR` | 纯色背景（与 `image.background` 二选一） |

**背景优先级**：`image.background` > `backgroundColor` > 透明

**示例**（带背景图片）：
```json
{
  "name": "message_box.title",
  "font": "default",
  "image": { "background": "label/mb.img.background.png" },
  "borderScale": 4.0,
  "fontColor": "#FFFFFFFF"
}
```

**示例**（透明背景）：
```json
{
  "name": "default",
  "font": "default",
  "image": { "background": "transparent/black64.png" },
  "fontColor": "#FFFFFFFF"
}
```

---

### 2.3 Button Kind JSON（按钮样式）

**位置**：`asset/ui/button/{name}.json`

**解析**：`ButtonManager.loadButtonKind()` → `ButtonKind`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `name` | String | 必需 | — | 按钮种类名称 |
| `font` | String | 必需 | — | 使用的字体名称 |
| `image.up` | String | 必需 | — | 普通状态图片，相对 `resource/image/` |
| `image.down` | String | 必需 | — | 按下状态图片 |
| `image.disabled` | String | 必需 | — | 禁用状态图片 |
| `audio.click` | String | 必需 | — | 点击音效文件，相对 `resource/audio/` |
| `borderScale` | float | 可选 | `1.0` | 九宫格边框缩放系数 |
| `fontColor` | String (hex) | 必需 | — | 默认字体颜色 |

**自动颜色派生**：
- `downFontColor` = 1 - R, 1 - G, 1 - B（反色）
- `disabledFontColor` = fontColor × 0.5（半透明度）

**示例**：
```json
{
  "name": "default",
  "font": "default",
  "image": {
    "up": "button/de.img.up.png",
    "down": "button/de.img.down.png",
    "disabled": "button/de.img.disabled.png"
  },
  "audio": { "click": "button/de.aud.click.ogg" },
  "fontColor": "#00008BFF"
}
```

---

### 2.4 Image Kind JSON（图像样式）

**位置**：`asset/ui/image/{name}.image.json`

**解析**：`ImageManager.loadImageKind()` → `ImageKind`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `name` | String | 必需 | — | 图像种类名称 |
| `image` | String | 二选一 | — | 图片文件路径，相对 `resource/image/` |
| `color` | String (hex) | 二选一 | — | 纯色，`Color.valueOf()` 解析，生成 1x1 px 纹理 |

**约束**：`image` 和 `color` 必须且只能设置一个。如果都没有，解析报错。

**示例**（纯色）：
```json
{
  "name": "select_frame.image",
  "color": "#FDA1FFFF"
}
```

**示例**（图片文件）：
```json
{
  "name": "game_cover.image",
  "image": "cover/frame.png"
}
```

---

## 三、布局 JSON（Layout）

### 3.1 Layout 顶层结构

**位置**：
- 启动器布局：`asset/layout/{name}.json`
- 游戏布局：`{game_path}/{game_id}/asset/layout/{name}.json`

**解析**：`LayoutManager.loadLayout()` → `LayoutConfig`

**上下文说明**：布局 JSON 的字段结构在启动器和游戏中完全一致，但 `textKey` 的文本插值解析目标取决于布局所在上下文——启动器布局解析启动器的语言/变量，游戏布局解析该游戏的语言/变量（详见 [11.4 文本插值语法](#114-文本插值语法)）。

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `name` | String | 可选 | 文件名（不含扩展名） | 页面名称 |
| `template` | String | 可选 | — | 模板布局名，加载后与本布局合并（`mergeLayout`） |
| `backgroundMusic` | String 或 Array\<String\> | 可选 | — | 背景音乐，可单曲（String）或多曲（Array），随机播放 |
| `backgroundPicture` | String | 可选 | — | 背景图片文件名，相对 `resource/image/` |
| `graphics` | Object | 可选 | — | 图片/动图容器，内部分 `picture` / `gif` 子分类，每一项见 [3.4](#34-graphics-条目) |
| `ui.image` | Object | 可选 | — | UI 图像映射表，每一项见 [3.5](#35-uiimage-条目) |
| `ui.label` | Object | 可选 | — | 标签映射表，每一项见 [3.6](#36-uilabel-条目) |
| `ui.button` | Object | 可选 | — | 按钮映射表，每一项见 [3.7](#37-uibutton-条目) |

**模板合并行为**：
当 `template` 指定后，会先加载模板布局，然后将两者的字段按以下规则合并：
- 基础字段（backgroundPicture、backgroundMusicList、musicList）：merge 覆盖 main
- 映射表字段（graphics/picture、graphics/gif、image、label、button）：相同 tag 时 merge 的字段覆盖 main，main 填补缺失字段
- JSON 数据：`layoutJson.combined(mergeJson)`

---

### 3.2 背景音乐配置

**字段**：`backgroundMusic`（Layout 顶层）

| 值类型 | 示例 | 说明 |
|--------|------|------|
| String（单曲） | `"menu.mp3"` | 单首背景音乐 |
| Array（多曲） | `["bgm1.mp3", "bgm2.mp3"]` | 多首背景音乐，运行时随机选择一首播放 |

**加载**：音频文件放置在 `resource/audio/` 目录下。

---

### 3.3 背景图片配置

**字段**：`backgroundPicture`（Layout 顶层）

| 值类型 | 示例 | 说明 |
|--------|------|------|
| String | `"menu.background.png"` | 背景图片文件名，相对 `resource/image/` |

---

### 3.4 graphics 条目（picture / gif）

#### PictureInfo（静态图片）

**位置**：Layout JSON 中的 `graphics.picture.{tag}`

**解析**：`PictureInfo(JsonEntity)` → `PictureTag` → `JsonPathParser` + `JsonPositionParser` + `JsonSizeParser`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `path` | String | 必需 | — | 图片文件名，相对 `resource/image/` |
| `position.x` | int | 必需 | `0` | X 坐标 |
| `position.y` | int | 必需 | `0` | Y 坐标 |
| `size.width` | int | 必需 | `100` | 宽度 |
| `size.height` | int | 必需 | `100` | 高度 |

**示例**：
```json
{
  "title": {
    "path": "menu.title.png",
    "position": { "x": 16, "y": 1000 },
    "size": { "width": 1170, "height": 333 }
  }
}
```

#### GifInfo（动图/序列帧）

**位置**：Layout JSON 中的 `graphics.gif.{tag}`

**解析**：`GifInfo(JsonEntity)` 及 `LayoutManager.loadLayoutPicture()` 中的序列帧加载逻辑

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `length` | int | 必需 | — | 帧数 |
| `duration` | float | 必需 | — | 每帧播放时长（秒） |
| `path.{index}` | String | 必需 | — | 帧图片文件名，`path` 对象下键为 1 到 `length` 的数字字符串 |
| `position.x` | int | 必需 | `0` | X 坐标 |
| `position.y` | int | 必需 | `0` | Y 坐标 |
| `size.width` | int | 必需 | `100` | 宽度 |
| `size.height` | int | 必需 | `100` | 高度 |

**示例**：
```json
{
  "walking": {
    "length": 4,
    "duration": 0.15,
    "path": {
      "1": "ani/walk_0001.png",
      "2": "ani/walk_0002.png",
      "3": "ani/walk_0003.png",
      "4": "ani/walk_0004.png"
    },
    "position": { "x": 100, "y": 200 },
    "size": { "width": 200, "height": 300 }
  }
}
```

---

### 3.5 ui.image 条目

**位置**：Layout JSON 中的 `ui.image.{tag}`

**解析**：`ImageInfo(JsonEntity)` → `JsonPositionParser` + `JsonSizeParser` + `JsonShowParser`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `kind` | String | 必需 | — | 引用 `ImageKind` 的名称，格式为 `{name}.image` |
| `show` | boolean | 可选 | `true` | 是否初始显示 |
| `position.x` | int | 必需 | `0` | X 坐标 |
| `position.y` | int | 必需 | `0` | Y 坐标 |
| `size.width` | int | 必需 | `100` | 宽度 |
| `size.height` | int | 必需 | `100` | 高度 |

**注意**：`ImageInfo` 不持有颜色、文本等属性，仅用于显示静态图像。

**示例**：
```json
{
  "unselect_frame0": {
    "kind": "unselect_frame.image",
    "position": { "x": 694, "y": 737 },
    "size": { "width": 320, "height": 440 }
  },
  "hidden_cover": {
    "kind": "game_cover.image",
    "show": false,
    "position": { "x": 704, "y": 747 },
    "size": { "width": 300, "height": 420 }
  }
}
```

---

### 3.6 ui.label 条目

**位置**：Layout JSON 中的 `ui.label.{tag}`

**解析**：`LabelInfo(JsonEntity)` → `JsonTextParser` + `JsonPositionParser` + `JsonSizeParser` + `JsonShowParser`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `kind` | String | 必需 | — | 引用 `LabelKind` 的名称 |
| `show` | boolean | 可选 | `true` | 是否初始显示 |
| `position.x` | int | 必需 | `0` | X 坐标 |
| `position.y` | int | 必需 | `0` | Y 坐标 |
| `size.width` | int | 必需 | `100` | 宽度 |
| `size.height` | int | 必需 | `100` | 高度 |
| `textKey` | String | 与 `text` 二选一 | `""` | 可解析文本键，格式见 [第十章](#101-语言-json-与文本插值) |
| `text` | String | 与 `textKey` 二选一 | `""` | 纯文本（不解析） |
| `fontName` | String | 可选 | `null` | 覆盖种类的默认字体名称 |
| `fontSize` | float | 可选 | `1.0` | 字体缩放系数 |
| `fontColor` | String (hex) | 可选 | `null`（继承种类颜色） | 字体颜色。**必须使用 `"#RRGGBBAA"` 格式的字符串**，不可使用整数 |
| `fontFlag` | String | 可选 | `"NW"` | 对齐+打字机标志，见 [FontFlag 枚举](#fontflag-枚举) |
| `fontArgs` | Object | 可选 | `{}` | 字体参数对象，见下方 `fontArgs` 子字段 |

#### fontArgs 子字段

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `padX` | float | 可选 | `50` | 文本内边距（水平方向），与 `padY` 同时设置 |
| `padY` | float | 可选 | `50` | 文本内边距（垂直方向），与 `padX` 同时设置 |
| `pad` | float | 可选 | `50` | 统一内边距（同时设置 padX 和 padY） |

**优先级**：`padX + padY` > `pad` > 默认 `50`

#### FontFlag 枚举

| 枚举值 | 对齐 | 打字机效果 |
|--------|------|-----------|
| `CENTER` | 居中 | 否 |
| `W` | 左对齐 | 否 |
| `E` | 右对齐 | 否 |
| `N` | 顶部 | 否 |
| `S` | 底部 | 否 |
| `NW` | 左上 | 否 |
| `NE` | 右上 | 否 |
| `SW` | 左下 | 否 |
| `SE` | 右下 | 否 |
| `CENTER_TYPING` | 居中 | 是 |
| `W_TYPING` | 左对齐 | 是 |
| `E_TYPING` | 右对齐 | 是 |
| `N_TYPING` | 顶部 | 是 |
| `S_TYPING` | 底部 | 是 |
| `NW_TYPING` | 左上 | 是 |
| `NE_TYPING` | 右上 | 是 |
| `SW_TYPING` | 左下 | 是 |
| `SE_TYPING` | 右下 | 是 |

**打字机效果**：启用后，文本会逐字显示，速度默认 25 字符/秒。点击标签可立即显示完整文本。

**示例**：
```json
{
  "dialogue_textbox": {
    "kind": "default",
    "position": { "x": 50, "y": 50 },
    "size": { "width": 2460, "height": 450 },
    "textKey": "{language$ui.json#layout.menu.dialogue}",
    "fontSize": 1.0,
    "fontColor": "#FFFFFFFF",
    "fontFlag": "nw_typing",
    "fontArgs": { "pad": 30 }
  }
}
```

---

### 3.7 ui.button 条目

**位置**：Layout JSON 中的 `ui.button.{tag}`

**解析**：`ButtonInfo(JsonEntity)` → `JsonTextParser` + `JsonPositionParser` + `JsonSizeParser` + `JsonShowParser`

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `kind` | String | 必需 | — | 引用 `ButtonKind` 的名称 |
| `show` | boolean | 可选 | `true` | 是否初始显示 |
| `position.x` | int | 必需 | `0` | X 坐标 |
| `position.y` | int | 必需 | `0` | Y 坐标 |
| `size.width` | int | 必需 | `100` | 宽度 |
| `size.height` | int | 必需 | `100` | 高度 |
| `textKey` | String | 与 `text` 二选一 | `""` | 可解析文本键 |
| `text` | String | 与 `textKey` 二选一 | `""` | 纯文本 |
| `fontName` | String | 可选 | `null` | 覆盖种类的默认字体名称 |
| `fontSize` | float | 可选 | `1.0` | 字体缩放系数 |
| `fontColor` | String (hex) | 可选 | `null`（继承种类颜色） | 字体颜色 |

**与 LabelInfo 的区别**：`ButtonInfo` 没有 `fontFlag`、`fontArgs` 字段。

**示例**：
```json
{
  "start": {
    "kind": "default",
    "position": { "x": 230, "y": 746 },
    "size": { "width": 540, "height": 140 },
    "textKey": "{language$main.json#menu.main.button.start}",
    "fontSize": 1.5
  }
}
```

---

## 四、特殊 UI 配置

### 4.1 message_box.json（弹窗布局）

**位置**：`asset/ui/message_box/message_box.json`

**解析**：`MessageBox`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `audio` | String | — | 弹窗提示音，相对 `resource/audio/` |
| `label.title` | Object | — | 标题标签配置 |
| `label.title.kind` | String | 必需 | 标签种类名称 |
| `label.title.fontSize` | float | 可选 | `1.0` |
| `label.title.fontFlag` | String | 可选 | `"center"` |
| `label.title.fontArgs` | Object | 可选 | 字体参数，支持 `pad` |
| `label.content` | Object | — | 内容标签配置，同上 |
| `button.normal` | Object | — | 确定按钮配置 |
| `button.normal.kind` | String | 必需 | 按钮种类名称 |
| `button.normal.fontSize` | float | 可选 | `1.0` |

**示例**：
```json
{
  "audio": "message_box/de.aud.ogg",
  "label": {
    "title": {
      "kind": "message_box.title",
      "fontSize": 1.2,
      "fontFlag": "center",
      "fontArgs": { "pad": 20 }
    },
    "content": {
      "kind": "message_box.content",
      "fontSize": 1.0,
      "fontFlag": "nw",
      "fontArgs": { "pad": 25 }
    }
  },
  "button": {
    "normal": {
      "kind": "message_box.button",
      "fontSize": 1.2
    }
  }
}
```

---

## 五、游戏系统

### 5.1 game.json（游戏定义）

**位置**：`game/{game_id}/game.json`

**解析**：手动读取，供游戏列表显示用

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | String | — | 游戏名称 |
| `author` | String | — | 作者 |
| `profile` | String | — | 简介 |

**示例**：
```json
{
  "name": "GameSample",
  "author": "hujiugame",
  "profile": "The sample of normal game files."
}
```

### 5.2 game_config.json（游戏配置）

**位置**：`game/game_config.json`

目前为空占位文件。预留用于存储游戏列表相关的配置。

**示例**：
```json
{

}
```

---

## 六、脚本指令系统（详见 SCRIPT_INTERNAL_STANDARD.md）

> **脚本指令的完整规范不在本文档范围**，详见 `develop/SCRIPT_INTERNAL_STANDARD.md`（指令集、值系统、运算符、执行模型）。

### 6.1 指令 JSON 通用结构

本文档仅列出脚本指令 JSON 的顶层通用结构，用于识别此类 JSON：

```json
{
  "type": "control|variable|story",
  "action": "...",
  "param": { ... }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `type` | String | 是 | 指令大类 |
| `action` | String | 是 | 具体动作名 |
| `param` | Object | 是 | 参数对象，内容因 type+action 而异 |

**解析类**：
- `ScriptCommandParser`：通用脚本指令
- `TriggerCommandParser`：触发器指令
- `ValueCommandParser`：值指令
- `ArgumentInfo`：参数信息

---

## 七、故事树系统

### 7.1 TreeStructureInfo JSON（连接对象）

`TreeStructureInfo` 是故事树的通用"指针"，通过 `(block, type, id)` 三元组唯一确定故事树中的某个节点。用于 `in`（入边）和 `out`（出边）连接列表以及 `role.json` 中的入口定义。

**解析**：`TreeStructureInfo(JsonEntity)`，也支持 `TreeStructureInfo(defaultBlock, JsonEntity)` —— 当 JSON 中不提供 `block` 时使用传入的默认值。

**字段**：

| 字段 | 类型 | 必需 | 默认值 | 解析说明 |
|------|------|------|--------|----------|
| `block` | String | 可选 | 由调用方传入的 `defaultBlock` | 目标节点所在的 **块文件名**，不含 `.json`。用于跨块跳转（如 `main.json` 中的出边指向 `chapter1.json` 中的节点） |
| `type` | String | 必需 | — | 节点类型枚举值，**大小写不敏感**：`ROOT` / `BRANCH` / `NODE` / `LEAF` |
| `id` | String | 必需 | — | 目标节点在对应类型分区中的 **结构 ID**，与 JSON 块文件中的键名对应 |

**校验规则**：`block`、`type`、`id` 三个字段缺一不可，任一缺失则解析失败并打 ERROR 日志。

**示例**（指向 `chapter1.json` 中 `type=BRANCH, id=scene_01` 的节点）：
```json
{
  "block": "chapter1",
  "type": "BRANCH",
  "id": "scene_01"
}
```

**示例**（省略 `block`，使用调用方传入的默认块名）：
```json
{
  "type": "LEAF",
  "id": "ending"
}
```

---

## 八、值系统（详见 SCRIPT_INTERNAL_STANDARD.md）

> **值表达式的完整规范不在本文档范围**，详见 `develop/SCRIPT_INTERNAL_STANDARD.md`（原子值、数学运算、比较运算、逻辑运算）。

### 8.1 LogicValue JSON 通用结构

本文档仅列出值指令 JSON 的顶层通用结构：

```json
{
  "type": "atomic|compare|math|logic",
  "action": "...",
  "param": { ... }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `type` | String | 是 | 值指令大类 |
| `action` | String | 是 | 具体运算 |
| `param` | Object | 可选 | 参数，可缺省 |

---

## 九、外部应用配置

### 9.1 app_config.json（应用配置）

**位置**：外部文件，路径由 `PathName.BASE` + `asset/app_config.json` 决定

**写入**：`ThemeManager.saveProcessColorToAppConfig()` 在启动器主题初始化时写入

| 字段 | 类型 | 说明 |
|------|------|------|
| `process_color` | String (hex) | 主题主色调的 `#RRGGBBAA` hex 字符串，供启动器进度条使用 |

**示例**：
```json
{
  "process_color": "#3F48CCFF"
}
```

---

## 十、版本与更新系统

### 10.1 app_version.json（版本信息）

**位置**：`assets/asset/app_version.json`

**解析**：`UpdateChecker`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `appVersion` | int | 是 | 版本代码，单调递增 |
| `appVersionType` | int | 是 | 发布类型（1=稳定版，2=测试版等） |
| `appVersionString` | String | 是 | 语义版本号，如 `"1.0.0"` |

**示例**：
```json
{
  "appVersion": 1,
  "appVersionType": 1,
  "appVersionString": "1.0.0"
}
```

### 10.2 update_config.json（更新配置）

**位置**：`assets/asset/update_config.json`

**解析**：`UpdateChecker`

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `protect` | Array\<String\> | 空数组 | 更新时需要保护（不删除）的文件/目录路径 |
| `prohibit` | Array\<String\> | 空数组 | 更新时需要删除的文件/目录路径 |

**示例**：
```json
{
  "protect": ["path/to/save/", "path/to/config.json"],
  "prohibit": ["path/to/delete/"]
}
```

---

## 十一、语言系统

> **上下文说明**：语言文件存在于两个独立的作用域——
> - **启动器语言**（`assets/asset/language/`）：全局生效，`version` 指适配的启动器版本号
> - **游戏语言**（`{game_path}/{game_id}/asset/language/`）：仅在该游戏会话内生效，`version` 指适配的游戏版本号
>
> 两者 JSON 结构完全一致，字段含义相同，但 `version` 字段的语义由文件的"所在位置"决定。
> 解析时统一使用 `LanguageManager`，通过构造参数 `isLauncherLanguage` 区分行为（启动器语言在目录缺失时会自动修复，游戏语言不会）。

### 11.1 language_config.json（语言词典）

**位置**：
- 启动器：`assets/asset/language/language_config.json`
- 游戏：`{game_path}/{game_id}/asset/language/language_config.json`

**解析**：`LanguageManager.parseLanguagePath()` 读取

将语言目录名映射为语言配置（显示名称 + 路径类型）。每个 key 是语言目录名，value 是嵌套对象。

| 字段 | 类型 | 必需 | 默认值 | 说明 |
|------|------|------|--------|------|
| `{lang}` | Object | — | — | 键为语言目录名，值为语言配置对象 |
| `{lang}.name` | String | 是 | — | 语言显示名称 |
| `{lang}.kind` | String | 否 | `external` | 路径类型：`internal`（官方，Internal 句柄直读）或省略（第三方语言，External 句柄） |

**示例**：
```json
{
  "zh_CN": {
    "name": "简体中文",
    "kind": "internal"
  },
  "zh_TW": {
    "name": "繁體中文 (台灣)",
    "kind": "internal"
  },
  "en_US": {
    "name": "English (US)",
    "kind": "internal"
  }
}
```

### 11.2 language.json（语言定义）

**位置**：
- 启动器：`assets/asset/language/{lang}/language.json`
- 游戏：`{game_path}/{game_id}/asset/language/{lang}/language.json`

**解析**：`LanguageManager`
**自动修复行为**：启动器语言文件缺失时，自动从 internal 复制。游戏语言文件缺失时**不会**自动修复（游戏应自带语言包）。

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 语言显示名称 |
| `version` | String | 是 | **语义由文件位置决定**：`assets/asset/language/` 下的 → 适配的**启动器版本号**；`{game_path}/{game_id}/asset/language/` 下的 → 适配的**游戏版本号** |
| `default` | String | 是 | 默认加载的块名称（不含 `.json`） |
| `blocks` | Array\<String\> | 是 | 要加载的语言块文件名列表，如 `["main.json", "ui.json"]` |

**示例**：
```json
{
  "name": "简体中文",
  "version": "1.0.0",
  "default": "main",
  "blocks": ["main.json", "requirement.json"]
}
```
**注意**：上方的 `version: "1.0.0"` 如果位于启动器目录，表示该语言包适配启动器 v1.0.0；如果位于游戏目录，表示该语言包适配游戏 v1.0.0。

### 11.3 语言块文件

**位置**：
- 启动器：`assets/asset/language/{lang}/{block}.json`
- 游戏：`{game_path}/{game_id}/asset/language/{lang}/{block}.json`

**结构**：嵌套的 key-value 映射，通过 `LanguageManager` 展平为点号分隔的路径。

**示例**（main.json）：
```json
{
  "menu": {
    "main": {
      "button": {
        "start": "开始游戏",
        "create": "创建游戏",
        "config": "设置",
        "quit": "退出游戏"
      }
    },
    "list": {
      "label": {
        "page": "页",
        "absolute_path": "绝对路径"
      }
    }
  }
}
```

展平后得到以下键：
- `menu.main.button.start` → `"开始游戏"`
- `menu.main.button.create` → `"创建游戏"`
- `menu.list.label.page` → `"页"`

### 11.4 文本插值语法

在布局 JSON 的 `textKey` 字段中使用 `TextManager` 解析变量：

| 格式 | 示例 | 说明 |
|------|------|------|
| `{language$file.json#dotted.key}` | `{language$main.json#menu.main.button.start}` | 从**当前上下文**的语言文件查找翻译文本 |
| `{game$variable_name}` | `{game$game_list.now_page}` | 从**当前上下文**的 `GameInfoManager` 读取运行时变量值 |

**上下文说明**：
- `{language$...}`：在**启动器布局**中 → 从 `assets/asset/language/` 查找；在**游戏布局**中 → 从 `{game_path}/{game_id}/asset/language/` 查找。两者共享 `LanguageManager` 实现，但 `pathHandle` 指向不同的目录。
- `{game$...}`：在**启动器界面**中 → 读取启动器的 `GameInfoManager`（如 `game_list.now_page` 表示游戏列表页码）；在**游戏界面**中 → 读取该游戏的 `GameInfoManager`（如 `hp`、`score` 等游戏运行时变量）。

**解析优先级**：`textKey` > `text` > 空字符串 `""`

**示例**：
```json
{
  "textKey": "{language$main.json#menu.list.label.page}"
}
```
```json
{
  "textKey": "{language$main.json#menu.list.label.absolute_path} : {game$game_list.absolute_path}"
}
```

---

## 十二、用户配置系统

### 12.1 启动器 user_config.json（全局设置）

**位置**：外部文件，`{BASE}/user_config.json`

**作用域**：全局生效，影响启动器界面和所有游戏的默认行为。

**解析**：`UserConfigManager`

| 字段 | 类型 | 说明 |
|------|------|------|
| `language` | String | **启动器全局**语言设置 |
| `theme` | String | **启动器全局**主题设置 |
| `useViewport` | String | 视口缩放策略（如 `"stretch"`） |
| `fullscreen` | boolean | 全屏标志 |
| `resolution.width` | int | 分辨率宽度 |
| `resolution.height` | int | 分辨率高度 |
| `soundVolume.total` | float | 总音量（0.0~1.0） |
| `soundVolume.music` | float | 音乐音量（0.0~1.0） |
| `soundVolume.sound` | float | 音效音量（0.0~1.0） |

**示例**：
```json
{
  "language": "zh_CN",
  "theme": "default_theme",
  "useViewport": "stretch",
  "fullscreen": false,
  "resolution": { "width": 1024, "height": 576 },
  "soundVolume": { "total": 1, "music": 0.5, "sound": 0.8 }
}
```

### 12.2 游戏 user_config.json（游戏内覆盖）

**位置**：`{game_path}/{game_id}/user_config.json`

**作用域**：仅在该游戏会话内生效，覆盖启动器全局设置中的对应项。
**解析机制**：游戏进入时，`GameUserConfigLoader` 读取该配置后新建 `ThemeManager` 和 `LanguageManager` 实例，替换游戏会话内的主题和语言。关闭游戏后恢复启动器的全局设定。

**解析**：`GameUserConfigManager`

| 字段 | 类型 | 说明 |
|------|------|------|
| `language` | String | **该游戏使用的语言**，覆盖启动器全局语言 |
| `theme` | String | **该游戏使用的主题**，覆盖启动器全局主题 |

**示例**：
```json
{
  "language": "zh_CN",
  "theme": "default_theme"
}
```

---

## 十三、游戏运行时配置

### 13.1 role_config.json（角色配置）

**位置**：`{game_path}/{game_id}/role_config.json`

**解析**：`GameRoleManager`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `count` | int | 是 | 角色数量 |
| `role` | Array\<String\> | 是 | 角色目录名列表 |

**示例**：
```json
{
  "count": 1,
  "role": ["chu_zheng"]
}
```

### 13.2 role.json（角色定义）

**位置**：`{game_path}/{game_id}/{role_name}/role.json`

**解析**：`GameRoleManager`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `id` | String | 是 | 角色 ID |
| `root` | Object | 是 | 故事树根节点，见 [故事树章节](#142-连接对象treestructureinfo) |

**示例**：
```json
{
  "id": "chu_zheng",
  "root": {
    "block": "main",
    "type": "ROOT",
    "id": "start"
  }
}
```

### 13.3 script_config.json（脚本配置）

**位置**：`{game_path}/{game_id}/{role_name}/script_config.json`

**解析**：`GameScriptManager`

| 字段 | 类型 | 说明 |
|------|------|------|
| `scripts` | Array\<String\> | 脚本文件名列表 |

**示例**：
```json
{
  "scripts": ["script1.json", "script2.json"]
}
```

### 13.4 template_config.json（模板配置）

**位置**：`{game_path}/{game_id}/{role_name}/template_config.json`

**解析**：`GameTemplateManager`

| 字段 | 类型 | 说明 |
|------|------|------|
| `templates` | Array\<String\> | 模板文件名列表 |

**示例**：
```json
{
  "templates": ["template1.json", "template2.json"]
}
```

---

## 十四、故事树块文件

### 14.1 故事树 JSON 结构

**位置**：`{game_path}/{game_id}/asset/story/{block}.json`

**解析**：`GameStoryManager.parseStoryTreeBlock()`，解析结果缓存于 `storyTreeBlockMap`（LRU 策略，最多缓存 2 个块）。

故事树按文件分块（block），每个块文件包含 **至多四个部分**（section），对应四种节点类型。每个部分下是一个映射表，键为节点 ID，值为节点数据。

#### 块文件顶层结构

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `root` | Object | 可选 | **入口节点**映射表，每个节点是故事的起点 |
| `branch` | Object | 可选 | **分支节点**映射表，表示存在多条路径的段落 |
| `node` | Object | 可选 | **普通节点**映射表，故事的中间线性段落 |
| `leaf` | Object | 可选 | **结束节点**映射表，故事的终点/结局 |

#### 节点通用字段

所有四种节点共享以下字段，但各类型在 `page`、`in`、`out` 的取值上有差异：

| 字段 | 类型 | 适用于 | 说明 |
|------|------|---------|------|
| `page` | String 或 Array\<String\> | 全部四种 | 关联的页面 ID。String = 单个页面，Branch 可为 Array = 多个页面顺序播放 |
| `in` | Array\<[TreeStructureInfo](#71-treestructureinfo-json)\> | branch/node/leaf | **入边**连接列表，表示"从哪些节点可以到达此节点"。用于剧情回溯 |
| `out` | Array\<[TreeStructureInfo](#71-treestructureinfo-json)\> | root/branch/node | **出边**连接列表，表示"此节点可以前往哪些节点"。用于剧情推进 |

#### 四种节点的差异

| 特性 | root | branch | node | leaf |
|------|------|--------|------|------|
| `page` 取值 | **String**（单页） | **Array\<String\>** 或 String | **String**（单页） | **String**（单页） |
| 支持 `in` | 否（根节点无入边） | 是 | 是 | 是 |
| 支持 `out` | 是 | 是 | 是 | 否（叶节点无出边） |
| `forwardPage()` / `backPage()` | 均失败 | 均失败 | 均失败 | 均失败 |
| 父节点列表 | 无（始终无父） | 有（`parentTreeStructureInfo`） | 有 | 有 |
| 子节点列表 | 有（`childTreeStructureInfo`） | 有 | 有 | 无（始终无子） |

#### `page` 字段解释

`page` 的值在 JSON 中可以是 **字符串** 或 **字符串数组**，解析流程如下（`GameStoryManager.parsePageList()`）：
1. 优先尝试 `nodeData.getStringList("page")` 解析为数组
2. 若结果为空，回退到 `nodeData.getString("page")` 解析为单个字符串
3. 两者皆空则返回空列表

- **String 值**（root/node/leaf 专用）：单个页面 ID，例如 `"page_opening"`。页面数据位于 `{game}/asset/story/{role}/page/page_opening/` 目录下（含 `behavior.json`、布局配置等）
- **Array 值**（branch 专用）：多个页面 ID，按顺序播放，例如 `["page_01", "page_02"]`。`BranchStructure` 内部维护 `nowPageIndex` 追踪当前页面

#### `in` / `out` 连接机制

`in` 和 `out` 数组中的每个元素是一个 [TreeStructureInfo](#71-treestructureinfo-json)（`(block, type, id)` 三元组）。
- **`out` 遍历**：当前节点播放完毕后，遍历 `out` 列表获取下一个可前往的节点。可配合脚本引擎驱动选择（多出边表示分支选项）
- **`in` 回溯**：记录剧情到达当前节点的来源，用于返回/回溯功能
- **跨块跳转**：如果 `block` 字段不等于当前块文件名，自动通过 `GameStoryManager.getTreeStructure()` 加载目标块文件并查找节点

#### 节点唯一标识与缓存

每个节点在 `GameStoryManager` 中以 `(block, type, id)` 三元组为键，存入 `storyTreeBlockMap`。
- `storyTreeBlockMap` 是 `LinkedHashMap`，LRU 驱逐策略（`accessOrder=true`），最多保留 2 个块
- 通过 `getTreeStructure(TreeStructureInfo)` 访问节点时，若对应块未加载则自动调用 `loadStoryTreeBlock(block)` 加载
- `loadStoryTreeBlock` 读取 `{block}.json` 文件，逐条解析 root/branch/node/leaf 四个分区，构造对应的 `RootStructure`/`BranchStructure`/`NodeStructure`/`LeafStructure` 对象

#### 完整示例

以下示例展示一个带跨块跳转的故事树。`main.json` 为入口，通过 `out` 跳转到 `chapter1.json` 中的分支节点：

**main.json**：
```json
{
  "root": {
    "start": {
      "page": "opening",
      "out": [
        { "block": "chapter1", "type": "BRANCH", "id": "scene_01" }
      ]
    }
  }
}
```

**chapter1.json**：
```json
{
  "branch": {
    "scene_01": {
      "page": ["scene1_intro", "scene1_explain"],
      "in": [
        { "block": "main", "type": "ROOT", "id": "start" }
      ],
      "out": [
        { "block": "main", "type": "LEAF", "id": "good_ending" },
        { "block": "main", "type": "LEAF", "id": "bad_ending" }
      ]
    }
  }
}
```

**main.json**（续）：
```json
{
  "leaf": {
    "good_ending": {
      "page": "ending_happy",
      "in": [
        { "block": "chapter1", "type": "BRANCH", "id": "scene_01" }
      ]
    },
    "bad_ending": {
      "page": "ending_sad",
      "in": [
        { "block": "chapter1", "type": "BRANCH", "id": "scene_01" }
      ]
    }
  }
}
```

**剧情流程解读**：
1. 入口 `main.json` → `root.start` → 播放页面 `opening`
2. `root.start.out` → 跨块跳转到 `chapter1.json` → `branch.scene_01` → 顺序播放页面 `scene1_intro` → `scene1_explain`
3. `branch.scene_01.out` 有两个出边，表示分支选项：`good_ending` 或 `bad_ending`
4. 根据选择，跨块回到 `main.json` → `leaf.good_ending` 或 `leaf.bad_ending` → 播放对应结局页面

### 14.2 连接对象（TreeStructureInfo）

`in` 和 `out` 数组中的元素是 [TreeStructureInfo](#71-treestructureinfo-json) 对象，详见第七章。

---

## 十五、页面行为系统（详见 SCRIPT_INTERNAL_STANDARD.md）

> **behavior.json 的完整规范不在本文档范围**，详见 `develop/SCRIPT_INTERNAL_STANDARD.md`。

### 15.1 behavior.json 结构

**位置**：`{game_path}/{game_id}/asset/page/{page_id}/behavior.json`

**解析**：`PageBehavior`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `start` | Object | 可选 | 页面进入时执行的脚本（含 `type` + `commands`） |
| `loop` | Object | 可选 | 页面循环时执行的脚本（含 `type` + `commands`） |
| `triggers` | Array\<Object\> | 可选 | 触发器列表（含 `trigger` + `commands`） |

其中 `commands` 数组的元素为脚本指令对象，格式见 `SCRIPT_INTERNAL_STANDARD.md`。

---

## 十六、可调用脚本文件（详见 SCRIPT_INTERNAL_STANDARD.md）

> **可调用脚本文件的完整规范不在本文档范围**，详见 `develop/SCRIPT_INTERNAL_STANDARD.md`。

### 16.1 脚本文件 JSON 结构

**位置**：`{game_path}/{game_id}/asset/script/{filename}.json`

**解析**：`Script`

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `arguments` | Array\<Object\> | 可选 | 参数定义列表 |
| `commands` | Array\<Object\> | 是 | 脚本指令列表 |
| `return` | Object | 是 | 返回值定义（含 `class` + `defaultValue`） |

---

## 十七、目录结构配置

### 17.1 directory_structure.json

**位置**：`assets/asset/directory_structure.json`

定义游戏加载期间验证的已知目录结构。

| 字段 | 类型 | 说明 |
|------|------|------|
| `{path}.directory` | Array\<String\> | 该路径下预期的子目录列表 |
| `{path}.file` | Array\<String\> | 该路径下预期的文件列表 |

**示例**：
```json
{
  "path/name": {
    "directory": ["subdir1", "subdir2"],
    "file": ["file1.json", "file2.png"]
  }
}
```

---

#### 语言 JSON 文件

**位置**：主题目录下的 `language/` 目录，按语言代码组织。

**结构**：层级化的 key-value 映射，叶子节点为字符串值。

#### 文本键格式（textKey）

`LabelInfo` 和 `ButtonInfo` 的 `textKey` 字段支持两种插值语法：

| 格式 | 示例 | 说明 |
|------|------|------|
| `{language$file.json#path.to.key}` | `{language$main.json#menu.main.button.start}` | 从语言文件中查找翻译文本 |
| `{game$game_info_key}` | `{game$game_list.now_page}` | 从 `GameInfoManager` 中读取运行时值 |

**解析**：`TextManager` + `TextObject`

**textKey 优先级**：如果在 JSON 中同时设置了 `textKey` 和 `text`，优先使用 `textKey`。

**示例**：
```json
{
  "textKey": "{language$main.json#menu.list.label.page}"
}
```

表示：从 `language/` 目录加载 `main.json`，查找 `menu.list.label.page` 键对应的值。

#### 纯文本格式（text）

当使用 `text` 字段而非 `textKey` 时，字符串直接作为显示文本使用，不经过语言解析系统。

```json
{
  "text": "直接显示的纯文本"
}
```

---

## 附录一：解析类一览

| 解析类 | 所在包 | 解析方法 | 对应 JSON |
|--------|--------|----------|-----------|
| `ThemeManager` | `manager` | `init()` → `parseThemePath()` + `parseJson()` + `loadVersionFromJson()` + `loadFontFromJson()` + `loadFontUseSizeFromJson()` + `loadColorFromJson()` | theme_config.json, theme.json |
| `UiManager` | `ui` | — | ui_config.json, font.json |
| `LabelManager` | `ui` | `loadLabelKind()` | label kind JSON |
| `ButtonManager` | `ui` | `loadButtonKind()` | button kind JSON |
| `ImageManager` | `ui` | `loadImageKind()` | image kind JSON |
| `LayoutManager` | `manager` | `loadLayout()` → `loadLayoutBasicInfo()` + `loadLayoutMusic()` + `loadLayoutPicture()` + `loadLayoutUi()` + `loadLayoutUiImage()` + `loadLayoutLabel()` + `loadLayoutButton()` | layout JSON |
| `MessageBox` | `ui` | — | message_box.json |
| `JsonTextParser` | `util.json.parser` | `parseText()`, `parseFontName()`, `parseFontSize()`, `parseFontColor()`, `parseFontFlag()`, `parseFontArgs()` | 文本/字体字段 |
| `JsonPositionParser` | `util.json.parser` | 构造器直接解析 `position` | `position` 子对象 |
| `JsonSizeParser` | `util.json.parser` | 构造器直接解析 `size` | `size` 子对象 |
| `JsonShowParser` | `util.json.parser` | `parseShow()` | `show` 字段 |
| `JsonPathParser` | `util.json.parser` | 构造器直接解析 `path` | `path` 字段 |
| `JsonScriptParser` | `util.json.parser` | `parseType()`, `parseAction()`, `parseScript()` | 脚本命令的 type/action/script 字段 |
| `ScriptCommandParser` | `script.data.command` | `parse()` | 脚本指令 JSON（详见 `SCRIPT_INTERNAL_STANDARD.md`） |
| `TriggerCommandParser` | `script.data.trigger.command` | `parse()` | 触发器指令 JSON（详见 `SCRIPT_INTERNAL_STANDARD.md`） |
| `ValueCommandParser` | `script.data.value.command` | `parse()` | 值指令 JSON（详见 `SCRIPT_INTERNAL_STANDARD.md`） |
| `ArgumentInfo` | `script.data` | 构造器解析 `argumentName`/`type`/`value`/`name` | 参数信息 JSON（详见 `SCRIPT_INTERNAL_STANDARD.md`） |
| `LanguageManager` | `manager` | — | language_config.json, language.json, 语言块文件 |
| `UserConfigManager` | `manager` | — | 启动器 user_config.json |
| `GameUserConfigManager` | `game` | — | 游戏 user_config.json |
| `GameRoleManager` | `game` | — | role_config.json, role.json |
| `GameScriptManager` | `game` | — | script_config.json |
| `GameTemplateManager` | `game` | — | template_config.json |
| `GameStoryManager` | `game` | `parseStoryTreeBlock()` | 故事树块 JSON |
| `PageBehavior` | `script` | — | behavior.json |
| `UpdateChecker` | — | — | app_version.json, update_config.json |

---

## 附录二：默认值汇总

| JSON 字段 | 对应 Java 代码 | 默认值 |
|-----------|---------------|--------|
| `show`（布尔显隐） | `JsonShowParser.parseShow()` | `true` |
| `position.x` / `position.y` | `JsonPositionParser` | `0` / `0` |
| `size.width` / `size.height` | `JsonSizeParser` | `100` / `100` |
| `fontSize` | `JsonTextParser.parseFontSize()` | `1.0f` |
| `fontColor`（缺省） | `JsonTextParser.parseFontColor()` | `null` |
| `fontFlag`（缺省） | `JsonTextParser.parseFontFlag()` | `FontFlag.NW` |
| `fontName`（缺省） | `JsonTextParser.parseFontName()` | `null` |
| `fontArgs`（缺省） | `JsonTextParser.parseFontArgs()` | 空 `JsonEntity` |
| `fontArgs.padX` / `fontArgs.padY` | `LabelManager.createLabel()` | `50` / `50` |
| `textKey`/`text` 均缺省 | `JsonTextParser.parseText()` | 空字符串 `""` |
| `borderScale`（按钮/标签） | `ButtonManager.loadButtonKind()` / `LabelManager.loadLabelKind()` | `1.0f` |
| `fontUseSize`（主题） | `ThemeManager.loadFontUseSizeFromJson()` | `Numeric.getFontNormalScaleList()` |
| `primaryColor` / `secondaryColor` / `fontColor`（主题色） | `ThemeManager.loadColorFromJson()` | `"#000000FF"` |
| `name`（Layout 名称） | `LayoutManager.loadLayoutBasicInfo()` | 文件名（不含扩展名） |
| 打字机速度 | `LabelManager` | `25.0f` 字符/秒 |
| 标签 `fontFlag` 缺省时的对齐 | `LabelManager.createLabel()` | `Align.center` |

---

> **本文档更新原则**：新增 JSON 格式后，需在此文档对应章节添加字段说明；修改解析行为后，需同步更新默认值和解析说明。保持与 `develop/CHANGELOG.md` 的更新同步。
