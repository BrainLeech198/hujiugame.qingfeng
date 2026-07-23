package com.hujiugame.qingfeng.script.data.command;

/**
 * 指令大类枚举。
 * <p>
 * 对应 ScriptCommand JSON 中的 {@code type} 字段值。
 */
public enum ScriptCommandType
{
    CONTROL("control"),
    VARIABLE("variable"),
    STORY("story");

    public static final String CONTROL_STRING = "control";
    public static final String VARIABLE_STRING = "variable";
    public static final String STORY_STRING = "story";

    private final String displayString;

    ScriptCommandType (String displayString)
    {
        this.displayString = displayString;
    }

    /**
     * 获取 JSON 中使用的字符串值
     */
    public String getDisplayString ()
    {
        return displayString;
    }

    /**
     * 从 JSON 字符串解析 ScriptCommandType
     *
     * @param jsonValue type 字段值
     * @return 对应的枚举，不匹配时返回 null
     */
    public static ScriptCommandType fromString (String jsonValue)
    {
        if (jsonValue == null) return null;
        for (ScriptCommandType t : values())
        {
            if (t.displayString.equals(jsonValue)) return t;
        }
        return null;
    }
}
