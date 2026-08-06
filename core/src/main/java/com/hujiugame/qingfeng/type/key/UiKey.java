package com.hujiugame.qingfeng.type.key;

/**
 * UI 主题配置 JSON 字段常量。
 *
 * 嵌套结构对齐真实 JSON：ui_config.json 第一层是组件类型 key（{@link #Button#KEY} 等），
 * 各组件内部类字段对应 kind 配置文件（asset/ui/xxx/*.json）的实际嵌套。
 * 布局引用字段（kind/show）转发自 {@link LayoutKey.Ui}，字符串值以 LayoutKey 为唯一来源。
 */
public final class UiKey
{
    private UiKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // Button 组件（对应 asset/ui/button/*.json）

    public static final class Button
    {
        private Button()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "button";

        /** 元素引用的 kind 名（转发自 {@link LayoutKey.Ui}） */
        public static final String KIND = LayoutKey.Ui.KIND;

        /** 元素是否显示（转发自 {@link LayoutKey.Ui}） */
        public static final String SHOW = LayoutKey.Ui.SHOW;

        /** kind 配置中的 name 字段（kind 名） */
        public static final String NAME = "name";

        /** kind 配置中的字体引用名 */
        public static final String FONT = "font";

        /** kind 配置中的字体颜色 */
        public static final String FONT_COLOR = "fontColor";

        /** 按钮边框缩放 */
        public static final String BORDER_SCALE = "borderScale";

        /** image 对象（按钮状态图片） */
        public static final class Image
        {
            private Image()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** image 对象在 kind 配置中的 key */
            public static final String KEY = "image";

            /** 按钮普通状态图片 */
            public static final String UP = "up";

            /** 按钮按下状态图片 */
            public static final String DOWN = "down";

            /** 按钮禁用状态图片 */
            public static final String DISABLED = "disabled";
        }

        /** audio 对象（按钮音效） */
        public static final class Audio
        {
            private Audio()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** audio 对象在 kind 配置中的 key */
            public static final String KEY = "audio";

            /** 按钮点击音效 */
            public static final String CLICK = "click";
        }
    }

    // ====================================================================================================
    // Label 组件（对应 asset/ui/label/*.json）

    public static final class Label
    {
        private Label()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "label";

        /** 元素引用的 kind 名（转发自 {@link LayoutKey.Ui}） */
        public static final String KIND = LayoutKey.Ui.KIND;

        /** 元素是否显示（转发自 {@link LayoutKey.Ui}） */
        public static final String SHOW = LayoutKey.Ui.SHOW;

        /** kind 配置中的 name 字段（kind 名） */
        public static final String NAME = "name";

        /** kind 配置中的字体引用名 */
        public static final String FONT = "font";

        /** kind 配置中的字体颜色 */
        public static final String FONT_COLOR = "fontColor";

        /** 标签背景颜色 */
        public static final String BACKGROUND_COLOR = "backgroundColor";

        /** 标签边框缩放 */
        public static final String BORDER_SCALE = "borderScale";

        /** image 对象（标签背景图片） */
        public static final class Image
        {
            private Image()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** image 对象在 kind 配置中的 key */
            public static final String KEY = "image";

            /** 标签背景图片 */
            public static final String BACKGROUND = "background";
        }
    }

    // ====================================================================================================
    // Image 组件（对应 asset/ui/image/*.json）

    public static final class Image
    {
        private Image()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "image";

        /** 元素引用的 kind 名（转发自 {@link LayoutKey.Ui}） */
        public static final String KIND = LayoutKey.Ui.KIND;

        /** 元素是否显示（转发自 {@link LayoutKey.Ui}） */
        public static final String SHOW = LayoutKey.Ui.SHOW;

        /** kind 配置中的 name 字段（kind 名） */
        public static final String NAME = "name";

        /** kind 配置中的颜色值 */
        public static final String COLOR = "color";

        /** kind 配置中的图片文件路径 */
        public static final String SOURCE = "image";
    }

    // ====================================================================================================
    // Font 配置（对应 asset/ui/font/*/font.json）

    public static final class Font
    {
        private Font()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** ui_config.json 中的组件类型 key */
        public static final String KEY = "font";

        /** kind 配置中的 name 字段（kind 名） */
        public static final String NAME = "name";

        /** 字体文件路径 */
        public static final String PATH = "path";

        /** 字体缩放 */
        public static final String SCALE = "scale";
    }

    // ====================================================================================================
    // MessageBox 组件（对应 asset/ui/message_box/message_box.json）

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

        /** label 对象（弹窗标题/内容标签） */
        public static final class Label
        {
            private Label()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** label 对象在 kind 配置中的 key */
            public static final String KEY = "label";

            /** 弹窗标题标签 */
            public static final String TITLE = "title";

            /** 弹窗内容标签 */
            public static final String CONTENT = "content";
        }

        /** button 对象（弹窗按钮） */
        public static final class Button
        {
            private Button()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** button 对象在 kind 配置中的 key */
            public static final String KEY = "button";

            /** 弹窗普通按钮 */
            public static final String NORMAL = "normal";
        }
    }
}
