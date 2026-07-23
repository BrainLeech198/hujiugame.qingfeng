package com.hujiugame.qingfeng.type;

import com.badlogic.gdx.Application;

import java.util.HashMap;
import java.util.Map;

public final class LogLevel
{
    private LogLevel()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final int DEBUG = Application.LOG_DEBUG;
    public static final int INFO = Application.LOG_INFO;
    public static final int ERROR = Application.LOG_ERROR;

    private static final Map<String, Integer> STRING_PARSE_LEVEL_MAP = new HashMap<>();
    static
    {
        STRING_PARSE_LEVEL_MAP.put("DEBUG", DEBUG);
        STRING_PARSE_LEVEL_MAP.put("INFO", INFO);
        STRING_PARSE_LEVEL_MAP.put("ERROR", ERROR);
    }

    private static final Map<Integer, String> LEVEL_DISPLAY_STRING_MAP = new HashMap<>();
    static
    {
        LEVEL_DISPLAY_STRING_MAP.put(DEBUG, "DEBUG");
        LEVEL_DISPLAY_STRING_MAP.put(INFO, "INFO ");
        LEVEL_DISPLAY_STRING_MAP.put(ERROR, "ERROR");
    }

    /**
     * 将字符串解析为日志等级常量
     *
     * @param string       日志等级字符串（DEBUG/INFO/ERROR）
     * @param defaultLevel 解析失败时返回的默认等级
     * @return 日志等级常量
     */
    public static int parseLevel (String string, int defaultLevel)
    {
        return STRING_PARSE_LEVEL_MAP.getOrDefault(string, defaultLevel);
    }

    /**
     * 将日志等级常量转换为显示字符串
     *
     * @param level         日志等级常量
     * @param defaultString 未知等级时返回的默认字符串
     * @return 日志等级显示字符串
     */
    public static String displayString (int level, String defaultString)
    {
        return LEVEL_DISPLAY_STRING_MAP.getOrDefault(level, defaultString);
    }
}
