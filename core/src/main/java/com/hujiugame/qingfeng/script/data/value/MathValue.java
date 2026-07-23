package com.hujiugame.qingfeng.script.data.value;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;
import com.hujiugame.qingfeng.script.data.value.command.action.ValueCommandAction;
import com.hujiugame.qingfeng.util.system.LogUtils;

/**
 * 数值表达式，expression 只允许 {@link ValueCommandType#MATH} 指令
 * 和 {@link ValueCommandType#ATOMIC}（const / variable / scope_variable / game_variable）指令。
 */
public class MathValue extends ValueObject
{
    public MathValue (JsonEntity json)
    {
        super(json);
        if (valid)
        {
            for (ValueCommand cmd : expression)
            {
                if (!isMathAllowed(cmd))
                {
                    LogUtils.error(MathValue.class, "expression 中包含不允许的指令"
                        + " (type): " + cmd.getCommandType() + " (action): " + cmd.getCommandAction()
                        + " (json): " + json);
                    this.valid = false;
                    return;
                }
            }
        }
    }

    // ===================================================================================================================

    private static boolean isMathAllowed (ValueCommand cmd)
    {
        if (cmd.getCommandType() == ValueCommandType.MATH) return true;
        if (cmd.getCommandType() == ValueCommandType.ATOMIC)
        {
            ValueCommandAction action = cmd.getCommandAction();
            return action == ValueCommandAction.CONST
                || action == ValueCommandAction.VARIABLE
                || action == ValueCommandAction.SCOPE_VARIABLE
                || action == ValueCommandAction.GAME_VARIABLE;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MathValue{" +
            "valid=" + valid +
            ", expression=" + expression +
            ", json=" + json +
            '}';
    }
}
