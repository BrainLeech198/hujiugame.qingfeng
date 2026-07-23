package com.hujiugame.qingfeng.script.data.value.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandType;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.compare.*;

import java.util.HashMap;
import java.util.Map;

public class CompareValueCommand implements ValueCommand
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
        ACTION_PARAM_MAP.put(ValueCommandAction.EQUAL, EqualCompareValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.NOT_EQUAL, NotEqualCompareValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.GREATER, GreaterCompareValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.LESS, LessCompareValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.GREATER_EQUAL, GreaterEqualCompareValueCommandParam.class);
        ACTION_PARAM_MAP.put(ValueCommandAction.LESS_EQUAL, LessEqualCompareValueCommandParam.class);
    }

    // ============================================================================

    public CompareValueCommand (ValueCommandAction valueCommandAction, ValueCommandParam valueCommandParam)
    {
        this.valueCommandType = ValueCommandType.COMPARE;
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
        json.put("type", valueCommandType.getDisplayString());
        json.put("action", valueCommandAction.getDisplayString());
        json.put("param", valueCommandParam.getJson());
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
        return "CompareValueCommand{" +
            "valid=" + valid +
            ", valueCommandType=" + valueCommandType +
            ", valueCommandAction=" + valueCommandAction +
            ", valueCommandParam=" + valueCommandParam +
            ", json=" + json +
            '}';
    }
}
