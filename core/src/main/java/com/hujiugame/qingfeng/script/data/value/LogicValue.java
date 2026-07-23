package com.hujiugame.qingfeng.script.data.value;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;
import com.hujiugame.qingfeng.util.system.LogUtils;

/**
 * 逻辑表达式，expression 只允许 {@link ValueCommandType#ATOMIC}、{@link ValueCommandType#COMPARE}
 * 和 {@link ValueCommandType#LOGIC} 指令。
 */
public class LogicValue extends ValueObject
{
    public LogicValue (JsonEntity json)
    {
        super(json);
        if (valid)
        {
            for (ValueCommand cmd : expression)
            {
                if (!isLogicAllowed(cmd))
                {
                    LogUtils.error(LogicValue.class, "expression 中包含不允许的指令"
                        + " (type): " + cmd.getCommandType() + " (action): " + cmd.getCommandAction()
                        + " (json): " + json);
                    this.valid = false;
                    return;
                }
            }
        }
    }

    // ===================================================================================================================

    private static boolean isLogicAllowed (ValueCommand cmd)
    {
        ValueCommandType type = cmd.getCommandType();
        return type == ValueCommandType.ATOMIC
            || type == ValueCommandType.COMPARE
            || type == ValueCommandType.LOGIC;
    }

    @Override
    public String toString() {
        return "LogicValue{" +
            "valid=" + valid +
            ", expression=" + expression +
            ", json=" + json +
            '}';
    }
}
