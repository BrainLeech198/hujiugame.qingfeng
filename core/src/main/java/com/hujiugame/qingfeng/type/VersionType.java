package com.hujiugame.qingfeng.type;

public final class VersionType
{
    private VersionType()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final int BETA = 0;
    public static final int RELEASE = 1;
    private static final String[] VERSION_TYPE_MAP = {"beta", "release"};

    public static String getVersionTypeName (int versionType)
    {
        return VERSION_TYPE_MAP[versionType];
    }
}
