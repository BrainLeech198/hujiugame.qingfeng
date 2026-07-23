package com.hujiugame.qingfeng.type.key;

public final class UserConfigKey
{
    private UserConfigKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String LANGUAGE = "language";

    public static final String THEME = "theme";

    public static final String USE_VIEWPORT = "useViewport";

    public static final String FULLSCREEN = "fullscreen";

    public static final String RESOLUTION = "resolution";
    public static final String RESOLUTION_WIDTH = "width";
    public static final String RESOLUTION_HEIGHT = "height";

    public static final String SOUND_VOLUME = "soundVolume";
    public static final String SOUND_VOLUME_TOTAL = "total";
    public static final String SOUND_VOLUME_MUSIC = "music";
    public static final String SOUND_VOLUME_SOUND = "sound";
}
