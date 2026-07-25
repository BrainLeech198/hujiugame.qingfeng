package com.hujiugame.qingfeng.script.data.value.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.atomic.*;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.HashMap;
import java.util.Map;

public class AtomicValueCommand implements ValueCommand
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
        ACTION_PARAM_MAP.put(ValueCommandAction.CONST, ConstAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.VARIABLE, VariableAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.SCOPE_VARIABLE, ScopeVariableAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.GAME_VARIABLE, GameVariableAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.TRUE, TrueAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.FALSE, FalseAtomicValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.CALL, CallAtomicValueCommandParam.class);

    }

    public AtomicValueCommand (ValueCommandAction valueCommandAction, ValueCommandParam valueCommandParam)
    {
        this.valueCommandType = ValueCommandType.ATOMIC;
        this.valueCommandAction = valueCommandAction;
        // 检查参数是否符合类型
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
        return "AtomicValueCommand{" +
            "valid=" + valid +
            ", valueCommandType=" + valueCommandType +
            ", valueCommandAction=" + valueCommandAction +
            ", valueCommandParam=" + valueCommandParam +
            ", json=" + json +
            '}';
    }
}
