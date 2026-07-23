package com.hujiugame.qingfeng.script.data.value.command.action;

import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;

public enum ValueCommandAction
{
    // 数学运算指令 MathValueCommand
    ADD(ValueCommandType.MATH, "add"),
    SUB(ValueCommandType.MATH, "sub"),
    MUL(ValueCommandType.MATH, "mul"),
    DIV(ValueCommandType.MATH, "div"),
    NEG(ValueCommandType.MATH, "neg"),
    RANDOM(ValueCommandType.MATH, "random"),

    // 比较运算指令 CompareValueCommand
    EQUAL(ValueCommandType.COMPARE, "equal"),
    NOT_EQUAL(ValueCommandType.COMPARE, "not_equal"),
    GREATER(ValueCommandType.COMPARE, "greater"),
    LESS(ValueCommandType.COMPARE, "less"),
    GREATER_EQUAL(ValueCommandType.COMPARE, "greater_equal"),
    LESS_EQUAL(ValueCommandType.COMPARE, "less_equal"),

    // 标准逻辑运算指令 LogicValueCommand
    AND(ValueCommandType.LOGIC, "and"),
    OR(ValueCommandType.LOGIC, "or"),
    NOT(ValueCommandType.LOGIC, "not"),

    // 原子值指令 AtomicValueCommand
    CONST(ValueCommandType.ATOMIC, "const"),
    VARIABLE(ValueCommandType.ATOMIC, "variable"),
    SCOPE_VARIABLE(ValueCommandType.ATOMIC, "scope_variable"),
    GAME_VARIABLE(ValueCommandType.ATOMIC, "game_variable"),
    TRUE(ValueCommandType.ATOMIC, "true"),
    FALSE(ValueCommandType.ATOMIC, "false"),
    CALL(ValueCommandType.ATOMIC, "call");

    private final ValueCommandType valueCommandType;
    private final String displayString;

    // ===== 构造器 =====

    ValueCommandAction (ValueCommandType valueCommandType, String displayString)
    {
        this.valueCommandType = valueCommandType;
        this.displayString = displayString;
    }

    // ===== Getter =====

    /**
     * 获取所属指令大类
     */
    public ValueCommandType getCommandType ()
    {
        return valueCommandType;
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
    public static ValueCommandAction fromString (String displayString)
    {
        if (displayString == null) return null;
        for (ValueCommandAction n : ValueCommandAction.values())
        {
            if (n.displayString.equals(displayString)) return n;
        }
        return null;
    }
}
