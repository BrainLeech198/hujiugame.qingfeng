# 今日工作进度：硬编码 JSON Key → 常量类重构

日期：2026-07-24

## 概述

将 Java 代码中所有 kind 配置相关 JSON key 硬编码字符串提取到 `UiKey.java` 常量类，将 layout 配置 key 提取到 `LayoutKey.java`。逐个字段确认后再写入/替换，编译验证。

## 新增文件

### `type/key/LayoutKey.java`
4 个常量，对应 layout.json 顶层字段：
- `BACKGROUND_MUSIC` = "backgroundMusic"
- `BACKGROUND_PICTURE` = "backgroundPicture"
- `PICTURE` = "picture"
- `UI` = "ui"

### `type/key/UiKey.java`
分 6 个区域，共 32 个常量：

**组件类型列表（5）**：`BUTTON`、`LABEL`、`IMAGE`、`FONT`、`MESSAGE_BOX`

**button kind 配置（11）**：`BUTTON_NAME`、`BUTTON_KIND`、`BUTTON_FONT`、`BUTTON_IMAGE`/`BUTTON_IMAGE_UP`/`BUTTON_IMAGE_DOWN`/`BUTTON_IMAGE_DISABLED`、`BUTTON_AUDIO`/`BUTTON_AUDIO_CLICK`、`BUTTON_FONT_COLOR`、`BUTTON_BORDER_SCALE`

**label kind 配置（8）**：`LABEL_NAME`、`LABEL_KIND`、`LABEL_FONT`、`LABEL_IMAGE`/`LABEL_IMAGE_BACKGROUND`、`LABEL_FONT_COLOR`、`LABEL_BACKGROUND_COLOR`、`LABEL_BORDER_SCALE`

**image kind 配置（4）**：`IMAGE_NAME`、`IMAGE_KIND`、`IMAGE_COLOR`、`IMAGE_SOURCE`

**messageBox 配置（6）**：`MESSAGE_BOX_AUDIO`、`MESSAGE_BOX_LABEL`/`MESSAGE_BOX_LABEL_TITLE`/`MESSAGE_BOX_LABEL_CONTENT`、`MESSAGE_BOX_BUTTON`/`MESSAGE_BOX_BUTTON_NORMAL`

**font 配置（3）**：`FONT_NAME`、`FONT_PATH`、`FONT_SCALE`

## 修改文件（9 个）

| 文件 | 修改内容 |
|------|----------|
| `manager/LayoutManager.java` | 8 处常量替换 |
| `ui/ButtonManager.java` | 全部硬编码替换为 UiKey 常量 |
| `ui/LabelManager.java` | 全部硬编码替换 + 修复上一会话误删的 FontFlag 导入 |
| `ui/ImageManager.java` | 全部硬编码替换 + 添加 UiKey 导入 |
| `ui/UiManager.java` | font 配置 name/path/scale 替换 |
| `ui/MessageBox.java` | audio/label(title,content)/button(normal) 替换 |
| `ui/kind/button/ButtonInfo.java` | "kind" → BUTTON_KIND |
| `ui/kind/label/LabelInfo.java` | "kind" → LABEL_KIND |
| `ui/kind/image/ImageInfo.java` | "kind" → IMAGE_KIND |

## 命名原则
- 每个组件有自己的命名空间前缀（`BUTTON_`、`LABEL_`、`IMAGE_`、`MESSAGE_BOX_`、`FONT_`）
- 即使相同 JSON key（如 "name"）在不同组件中也用不同常量名
- Allman 大括号，4 空格缩进，方法声明左括号前加空格

## 待处理
layout UI 元素字段（位于 `JsonTextParser`、`JsonPositionParser`、`JsonSizeParser`、`JsonShowParser`）：
- `show`、`position`/`x`/`y`、`width`/`height`、`text`/`textKey`、`fontName`、`fontSize`、`fontColor`、`fontFlag`、`fontArgs`
- fontArgs 子字段：`padX`、`padY`、`pad`

## 编译状态
通过，仅 3 个警告（JDK 8 源版本过时警告，与修改无关）
