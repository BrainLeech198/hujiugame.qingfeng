package com.hujiugame.qingfeng.script.data.command.action;

import com.hujiugame.qingfeng.script.data.command.ScriptCommandType;

/**
 * 具体指令枚举。
 * <p>
 * 对应 ScriptCommand JSON 中的 {@code command} 字段值（原名 action）。
 * 每个指令关联其所属的 {@link ScriptCommandType}。
 */
public enum ScriptCommandAction
{
    // ===== control =====

    IF(ScriptCommandType.CONTROL, "if"),
    WHILE(ScriptCommandType.CONTROL, "while"),
    BREAK(ScriptCommandType.CONTROL, "break"),
    CONTINUE(ScriptCommandType.CONTROL, "continue"),
    RETURN(ScriptCommandType.CONTROL, "return"),
    WAIT(ScriptCommandType.CONTROL, "wait"),
    CALL(ScriptCommandType.CONTROL, "call"),

    // ===== variable =====

    CREATE(ScriptCommandType.VARIABLE, "create"),
    ASSIGNMENT(ScriptCommandType.VARIABLE, "assignment"),

    // ===== story =====

    FORWARD_PAGE(ScriptCommandType.STORY, "forward_page"),
    GOTO_PAGE(ScriptCommandType.STORY, "goto_page");

    public static final String IF_STRING = "if";
    public static final String WHILE_STRING = "while";
    public static final String BREAK_STRING = "break";
    public static final String CONTINUE_STRING = "continue";
    public static final String RETURN_STRING = "return";
    public static final String WAIT_STRING = "wait";
    public static final String CALL_STRING = "call";

    public static final String CREATE_STRING = "create";
    public static final String ASSIGNMENT_STRING = "assignment";

    public static final String FORWARD_PAGE_STRING = "forward_page";
    public static final String GOTO_PAGE_STRING = "goto_page";

    // ===== 字段 =====

    private final ScriptCommandType scriptCommandType;
    private final String displayString;

    // ===== 构造器 =====

    ScriptCommandAction (ScriptCommandType scriptCommandType, String displayString)
    {
        this.scriptCommandType = scriptCommandType;
        this.displayString = displayString;
    }

    // ===== Getter =====

    /**
     * 获取所属指令大类
     */
    public ScriptCommandType getCommandType ()
    {
        return scriptCommandType;
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
    public static ScriptCommandAction fromString (String displayString)
    {
        if (displayString == null) return null;
        for (ScriptCommandAction n : values())
        {
            if (n.displayString.equals(displayString)) return n;
        }
        return null;
    }
}
