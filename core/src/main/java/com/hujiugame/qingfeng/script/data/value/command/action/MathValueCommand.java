package com.hujiugame.qingfeng.script.data.value.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.math.*;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.HashMap;
import java.util.Map;

public class MathValueCommand implements ValueCommand
{
    private boolean valid;
    private final ValueCommandType valueCommandType;
    private final ValueCommandAction valueCommandAction;
    private final ValueCommandParam valueCommandParam;
    private JsonEntity json;

    // ==============================================================================

    private static final Map<ValueCommandAction, Class<?>> ACTION_PARAM_MAP;

    static
    {
        ACTION_PARAM_MAP = new HashMap<>();
        ACTION_PARAM_MAP.put(ValueCommandAction.ADD, AddMathValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.SUB, SubMathValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.MUL, MulMathValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.DIV, DivMathValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.NEG, NegMathValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.RANDOM, RandomMathValueCommandParam.class);
    }

    // ============================================================================

    public MathValueCommand (ValueCommandAction valueCommandAction, ValueCommandParam valueCommandParam)
    {
        this.valueCommandType = ValueCommandType.MATH;
        this.valueCommandAction = valueCommandAction;
        if (!ACTION_PARAM_MAP.get(valueCommandAction).isInstance(valueCommandParam))
        {
            throw new IllegalArgumentException(
                "Command parameter type : " + valueCommandParam.getClass().getName()
                + " does not match command action : " + valueCommandAction
            );
        }
        else
        {
            this.valueCommandParam = valueCommandParam;
        }
        this.valid = true;
        buildJson();
    }

    private void buildJson ()
    {
        json = new JsonEntity();
        json.put(ScriptKey.Command.TYPE, valueCommandType.getDisplayString());
        json.put(ScriptKey.Command.ACTION, valueCommandAction.getDisplayString());
        json.put(ScriptKey.Command.PARAM, valueCommandParam.getJson());
    }

    /**
     * 指令是否有效
     */
    @Override
    public boolean isValid ()
    {
        return valid;
    }

    /**
     * 获取命令类型
     */
    @Override
    public ValueCommandType getCommandType ()
    {
        return valueCommandType;
    }

    /**
     * 获取命令名称
     */
    @Override
    public ValueCommandAction getCommandAction ()
    {
        return valueCommandAction;
    }

    /**
     * 获取命令参数
     */
    @Override
    public ValueCommandParam getCommandParam ()
    {
        return valueCommandParam;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }

    @Override
    public String toString() {
        return "MathValueCommand{" +
            "valid=" + valid +
            ", valueCommandType=" + valueCommandType +
            ", valueCommandAction=" + valueCommandAction +
            ", valueCommandParam=" + valueCommandParam +
            ", json=" + json +
            '}';
    }
}
