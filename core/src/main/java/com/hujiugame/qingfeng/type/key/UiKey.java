package com.hujiugame.qingfeng.type.key;

public final class UiKey
{
    private UiKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // Button 组件

    public static final class Button
    {
        private Button()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "button";

        /** kind 唯一标识名 */
        public static final String NAME = "name";

        /** 引用哪个 kind */
        public static final String KIND = "kind";

        /** kind 配置中的字体引用名 */
        public static final String FONT = "font";

        /** kind 配置中的图片状态对象 */
        public static final String IMAGE = "image";

        /** 按钮普通状态图片 */
        public static final String IMAGE_UP = "up";

        /** 按钮按下状态图片 */
        public static final String IMAGE_DOWN = "down";

        /** 按钮禁用状态图片 */
        public static final String IMAGE_DISABLED = "disabled";

        /** kind 配置中的音频对象 */
        public static final String AUDIO = "audio";

        /** 按钮点击音效 */
        public static final String AUDIO_CLICK = "click";

        /** kind 配置中的字体颜色 */
        public static final String FONT_COLOR = "fontColor";

        /** 按钮边框缩放 */
        public static final String BORDER_SCALE = "borderScale";

        /** 是否显示 */
        public static final String SHOW = "show";
    }

    // ====================================================================================================
    // Label 组件

    public static final class Label
    {
        private Label()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "label";

        /** kind 唯一标识名 */
        public static final String NAME = "name";

        /** 引用哪个 kind */
        public static final String KIND = "kind";

        /** kind 配置中的字体引用名 */
        public static final String FONT = "font";

        /** kind 配置中的图片对象 */
        public static final String IMAGE = "image";

        /** 标签背景图片 */
        public static final String IMAGE_BACKGROUND = "background";

        /** kind 配置中的字体颜色 */
        public static final String FONT_COLOR = "fontColor";

        /** 标签背景颜色 */
        public static final String BACKGROUND_COLOR = "backgroundColor";

        /** 标签边框缩放 */
        public static final String BORDER_SCALE = "borderScale";

        /** 是否显示 */
        public static final String SHOW = "show";
    }

    // ====================================================================================================
    // Image 组件

    public static final class Image
    {
        private Image()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "image";

        /** kind 唯一标识名 */
        public static final String NAME = "name";

        /** 引用哪个 kind */
        public static final String KIND = "kind";

        /** kind 配置中的颜色值 */
        public static final String COLOR = "color";

        /** kind 配置中的图片文件路径 */
        public static final String SOURCE = "image";

        /** 是否显示 */
        public static final String SHOW = "show";
    }

    // ====================================================================================================
    // MessageBox 组件

    public static final class MessageBox
    {
        private MessageBox()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "messageBox";

        /** 弹窗音效 */
        public static final String AUDIO = "audio";

        /** 弹窗标签对象 */
        public static final String LABEL = "label";

        /** 弹窗标题标签 */
        public static final String LABEL_TITLE = "title";

        /** 弹窗内容标签 */
        public static final String LABEL_CONTENT = "content";

        /** 弹窗按钮对象 */
        public static final String BUTTON = "button";

        /** 弹窗普通按钮 */
        public static final String BUTTON_NORMAL = "normal";
    }

    // ====================================================================================================
    // Font 配置

    public static final class Font
    {
        private Font()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "font";

        /** kind 唯一标识名 */
        public static final String NAME = "name";

        /** 字体文件路径 */
        public static final String PATH = "path";

        /** 字体缩放 */
        public static final String SCALE = "scale";
    }

}
