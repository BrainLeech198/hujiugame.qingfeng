package com.hujiugame.qingfeng.script.data.trigger.command.action;

import com.hujiugame.qingfeng.script.data.trigger.command.TriggerType;

public enum TriggerAction
{
    LABEL_CLICK (TriggerType.LABEL, "label_click");

    public static final String LABEL_CLICK_STRING = "label_click";

    // ===== 字段 =====

    private final TriggerType triggerType;
    private final String displayString;

    // ===== 构造器 =====

    TriggerAction (TriggerType triggerType, String displayString)
    {
        this.triggerType = triggerType;
        this.displayString = displayString;
    }

    // ===== Getter =====

    /**
     * 获取所属指令大类
     */
    public TriggerType getCommandType ()
    {
        return triggerType;
    }

    /**
     * 获取 JSON 中使用的字符串值
     */
    public String getDisplayString ()
    {
        return displayString;
    }

    // ===== 工厂方法 =====

    /**
     * 从 JSON 字符串解析 ScriptCommandAction
     *
     * @param displayString command 字段值
     * @return 对应的枚举，不匹配时返回 null
     */
    public static TriggerAction fromString (String displayString)
    {
        if (displayString == null) return null;
        for (TriggerAction n : values())
        {
            if (n.displayString.equals(displayString)) return n; // Changed from return n; to return n; (already correct)
        }
        return null;
    }
}
