package com.hujiugame.qingfeng.type;

public final class Numeric
{
    private Numeric()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final float[] FONT_NORMAL_SCALE_LIST = {0.5f, 0.8f, 1.2f};
    // {0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f}

    public static float[] getFontNormalScaleList ()
    {
        return FONT_NORMAL_SCALE_LIST.clone();
    }
}
