package com.hujiugame.qingfeng.type.key;

/**
 * graphics 分类下的子字段常量，对应 layout.json → graphics 内部的 picture / gif 子分类
 */
public final class GraphicsKey
{
    private GraphicsKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** graphics → picture 子分类 */
    public static final String PICTURE = "picture";

    /** graphics → gif 子分类 */
    public static final String GIF = "gif";

    /** 图片/GIF 文件路径 */
    public static final String PATH = "path";

    // ====================================================================================================
    // GIF 条目字段

    /** GIF 条目内部字段 */
    public static final class Gif
    {
        private Gif()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** GIF 帧数 */
        public static final String LENGTH = "length";

        /** 每帧时长（秒） */
        public static final String DURATION = "duration";
    }
}
