package com.hujiugame.qingfeng.script.data.trigger.command;

public enum TriggerType
{
    IMAGE("image"),
    LABEL("label"),
    BUTTON("button");

    public static final String IMAGE_STRING = "image";
    public static final String LABEL_STRING = "label";
    public static final String BUTTON_STRING = "button";

    private final String displayString;

    TriggerType (String displayString)
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
    public static TriggerType fromString (String jsonValue)
    {
        if (jsonValue == null) return null;
        for (TriggerType t : TriggerType.values())
        {
            if (t.displayString.equals(jsonValue)) return t;
        }
        return null;
    }
}
