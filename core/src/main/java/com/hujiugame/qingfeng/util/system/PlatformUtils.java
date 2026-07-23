package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public final class PlatformUtils
{

    /**
     * 私有构造函数，防止实例化工具类
     */
    private PlatformUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    private static final Application.ApplicationType platformType;
    private static final boolean isDesktop;
    private static final boolean isNotDesktop;
    private static final boolean isAndroid;
    private static final boolean isNotAndroid;
    static
    {
        platformType = Gdx.app.getType();
        isDesktop = platformType == Application.ApplicationType.Desktop;
        isNotDesktop = !isDesktop;
        isAndroid = platformType == Application.ApplicationType.Android;
        isNotAndroid = !isAndroid;
        LogUtils.info(PlatformUtils.class, "init 运行平台 (platform): " + PlatformUtils.getPlatformType());
    }

    /**
     * 获取当前运行平台的类型
     *
     * @return 平台类型枚举值
     */
    public static Application.ApplicationType getPlatformType ()
    {
        return platformType;
    }

    /**
     * 判断当前是否运行在桌面平台
     *
     * @return true 表示桌面平台
     */
    public static boolean isDesktop ()
    {
        return isDesktop;
    }

    /**
     * 判断当前是否不是桌面平台
     *
     * @return true 表示非桌面平台
     */
    public static boolean isNotDesktop ()
    {
        return isNotDesktop;
    }

    /**
     * 判断当前是否运行在 Android 平台
     *
     * @return true 表示 Android 平台
     */
    public static boolean isAndroid ()
    {
        return isAndroid;
    }

    /**
     * 判断当前是否不是 Android 平台
     *
     * @return true 表示非 Android 平台
     */
    public static boolean isNotAndroid ()
    {
        return isNotAndroid;
    }
}
