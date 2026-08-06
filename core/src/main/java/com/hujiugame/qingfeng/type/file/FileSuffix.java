package com.hujiugame.qingfeng.type.file;

public final class FileSuffix
{
    private FileSuffix()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** 不过滤 */
    public static final String EXT_ALL = "";

    /** 游戏文件 */
    public static final String EXT_GAME = ".qfg";

    /** 资源包文件 */
    /** 语言包 */
    public static final String EXT_LANGUAGE_PACK = ".qfl";
    /** 主题包 */
    public static final String EXT_THEME_PACK = ".qft";
    /** 游戏语言包 */
    public static final String EXT_GAME_LANGUAGE_PACK = ".qfgl";
    /** 游戏主题包 */
    public static final String EXT_GAME_THEME_PACK = ".qfgt";

    /** 压缩包 */
    public static final String EXT_ZIP  = ".zip";
    public static final String EXT_RAR  = ".rar";
    public static final String EXT_7Z   = ".7z";
    public static final String EXT_TAR  = ".tar";
    public static final String EXT_GZ   = ".gz";

    /** 图片 */
    public static final String EXT_PNG  = ".png";
    public static final String EXT_JPG  = ".jpg";
    public static final String EXT_JPEG = ".jpeg";
    public static final String EXT_BMP  = ".bmp";
    public static final String EXT_GIF  = ".gif";

    /** 文本 */
    public static final String EXT_TXT  = ".txt";
    public static final String EXT_JSON = ".json";
    public static final String EXT_XML  = ".xml";
    public static final String EXT_CSV  = ".csv";
}
