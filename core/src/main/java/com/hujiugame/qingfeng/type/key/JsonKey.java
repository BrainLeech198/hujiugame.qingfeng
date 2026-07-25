package com.hujiugame.qingfeng.type.key;

/**
 * 通用的 JSON key 常量，适用于跨多种配置上下文的字段名
 */
public final class JsonKey
{
    private JsonKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // 通用字段

    /** 尺寸相关 */
    public static final class Size
    {
        private Size()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String KEY = "size";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
    }

    /** 坐标相关 */
    public static final class Position
    {
        private Position()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String KEY = "position";
        public static final String X = "x";
        public static final String Y = "y";
    }

    /** 文本相关 */
    public static final class Text
    {
        private Text()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 语言 key 引用 */
        public static final String TEXT_KEY = "textKey";

        /** 纯文本 */
        public static final String TEXT = "text";
    }

    /** 字体渲染配置相关 */
    public static final class Font
    {
        private Font()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String FONT_NAME = "fontName";
        public static final String FONT_SIZE = "fontSize";
        public static final String FONT_COLOR = "fontColor";
        public static final String FONT_FLAG = "fontFlag";
        public static final String FONT_ARGS = "fontArgs";

        /** fontArgs 子字段 */
        public static final class Args
        {
            private Args()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String PAD_X = "padX";
            public static final String PAD_Y = "padY";
            public static final String PAD = "pad";
        }
    }
}
