package com.hujiugame.qingfeng.script.data.value.command;

/**
 * 指令大类枚举。
 * <p>
 * 对应 ValueCommand JSON 中的 {@code type} 字段值。
 */
public enum ValueCommandType
{
    MATH("math"),
    COMPARE("compare"),
    LOGIC("logic"),
    ATOMIC("atomic");

    private final String displayString;

    ValueCommandType (String displayString)
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
    public static ValueCommandType fromString (String jsonValue)
    {
        if (jsonValue == null) return null;
        for (ValueCommandType t : values())
        {
            if (t.displayString.equals(jsonValue)) return t;
        }
        return null;
    }
}
