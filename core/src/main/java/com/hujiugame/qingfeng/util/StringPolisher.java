package com.hujiugame.qingfeng.util;

import java.util.UUID;

public final class StringPolisher
{

    /**
     * 私有构造函数，防止实例化工具类
     */
    private StringPolisher()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 为字符串添加 UUID 前缀以生成唯一标识
     *
     * @param pure 原始字符串，可为 null
     * @return 添加 UUID 前缀后的字符串，格式为 "UUID.原始字符串"；输入为 null 时返回 null
     */
    public static @javax.annotation.Nullable String polished (@javax.annotation.Nullable String pure)
    {
        if (pure == null) return null;
        return UUID.randomUUID() + "." + pure;
    }
}
