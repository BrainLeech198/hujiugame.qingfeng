package com.hujiugame.qingfeng.type;

public final class Name

{
    private Name()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String DEFAULT_THEME_NAME = "默认主题";
    public static final String DEFAULT_LANGUAGE_NAME = "简体中文";

    public static final String GAME_DEFAULT_THEME_NAME = "theme";

    public static final String GAME_START_TASK_NAME = "start_task";
    public static final String GAME_LOOP_TASK_NAME = "loop_task";
    public static final String GAME_TRIGGER_TASK_NAME = "trigger_task";
}
