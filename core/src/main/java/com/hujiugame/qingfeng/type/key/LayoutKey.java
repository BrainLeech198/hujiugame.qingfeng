package com.hujiugame.qingfeng.type.key;

public final class LayoutKey
{
    private LayoutKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // layout.json 顶层字段

    /** 背景音乐列表 */
    public static final String BACKGROUND_MUSIC = "backgroundMusic";

    /** 背景图片文件名 */
    public static final String BACKGROUND_PICTURE = "backgroundPicture";

    /** 装饰图片/动图容器 */
    public static final String GRAPHICS = "graphics";

    /** 页面名称 */
    public static final String NAME = "name";

    /** 模板布局名 */
    public static final String TEMPLATE = "template";

    /** 音乐列表 */
    public static final String MUSIC = "music";

    /** UI 组件容器 */
    public static final String UI = "ui";
}
