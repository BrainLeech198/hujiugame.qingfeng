package com.hujiugame.qingfeng.type.key;

/**
 * theme.json 主题配置文件顶层字段常量
 */
public final class ThemeKey
{
    private ThemeKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** 主题版本号 */
    public static final String VERSION = "version";

    /** 默认字体引用名 */
    public static final String FONT = "font";

    /** 字体预缓存尺寸列表 */
    public static final String FONT_USE_SIZE = "fontUseSize";

    /** 主色调 */
    public static final String PRIMARY_COLOR = "primaryColor";

    /** 辅色 */
    public static final String SECONDARY_COLOR = "secondaryColor";

    /** 字体颜色 */
    public static final String FONT_COLOR = "fontColor";

    /** 进度条颜色（写入 app_config.json） */
    public static final String PROCESS_COLOR = "process_color";

    public static class Config
    {
        /** 主题显示名称 */
        public static final String NAME = "name";

        /** 主题路径类型 */
        public static final String KIND = "kind";
    }
}
