# UiKey 常量重构进度（临时记录）

## 已完成

### LayoutKey.java（独立于 UiKey，已完成，逐字段确认）
- `BACKGROUND_MUSIC`、`BACKGROUND_PICTURE`、`PICTURE`、`UI` → 全部替换进 LayoutManager.java，编译通过

### UiKey.java 结构
五个组件类型常量：`BUTTON`、`LABEL`、`IMAGE`、`FONT`、`MESSAGE_BOX`

**button kind 配置**（全部替换完毕）：
- `BUTTON_NAME`、`BUTTON_KIND`、`BUTTON_FONT`
- `BUTTON_IMAGE`/`BUTTON_IMAGE_UP`/`BUTTON_IMAGE_DOWN`/`BUTTON_IMAGE_DISABLED`
- `BUTTON_AUDIO`/`BUTTON_AUDIO_CLICK`
- `BUTTON_FONT_COLOR`、`BUTTON_BORDER_SCALE`
- 替换文件：ButtonManager.java、ButtonInfo.java

**label kind 配置**（全部替换完毕）：
- `LABEL_NAME`、`LABEL_KIND`、`LABEL_FONT`
- `LABEL_IMAGE`/`LABEL_IMAGE_BACKGROUND`
- `LABEL_FONT_COLOR`、`LABEL_BACKGROUND_COLOR`、`LABEL_BORDER_SCALE`
- 替换文件：LabelManager.java、LabelInfo.java

**image kind 配置**（全部替换完毕）：
- `IMAGE_NAME`、`IMAGE_KIND`、`IMAGE_COLOR`、`IMAGE_SOURCE`
- 替换文件：ImageManager.java、ImageInfo.java

**messageBox 配置**（全部替换完毕）：
- `MESSAGE_BOX_AUDIO`、`MESSAGE_BOX_LABEL`/`MESSAGE_BOX_LABEL_TITLE`/`MESSAGE_BOX_LABEL_CONTENT`、`MESSAGE_BOX_BUTTON`/`MESSAGE_BOX_BUTTON_NORMAL`
- 替换文件：MessageBox.java

**font 配置**（全部替换完毕）：
- `FONT_NAME`、`FONT_PATH`、`FONT_SCALE`
- 替换文件：UiManager.java（font 部分）

## 待处理

下组要处理的字段是 layout UI 元素字段（位于 JsonTextParser、JsonPositionParser、JsonSizeParser、JsonShowParser 中）：
- `show`、`position`/`x`/`y`、`width`/`height`、`text`/`textKey`、`fontName`、`fontSize`、`fontColor`、`fontFlag`、`fontArgs`
- 以及 fontArgs 子字段：`padX`、`padY`、`pad`

## 命名原则
- 每个组件有自己的命名空间前缀（BUTTON\_、LABEL\_、IMAGE\_、MESSAGE_BOX\_、FONT\_）
- 即使相同 JSON key 在不同组件中也用不同常量名
- 逐个字段确认：展示了 JSON 实际出现的位置后再写入
