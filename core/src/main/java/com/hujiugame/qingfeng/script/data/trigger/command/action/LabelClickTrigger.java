package com.hujiugame.qingfeng.script.data.trigger.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.trigger.command.TriggerCommand;
import com.hujiugame.qingfeng.script.data.trigger.command.TriggerType;
import com.hujiugame.qingfeng.script.data.trigger.command.param.TriggerParam;
import com.hujiugame.qingfeng.script.data.trigger.command.param.label.LabelClickTriggerParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.HashMap;
import java.util.Map;

public class LabelClickTrigger implements TriggerCommand
{
    private boolean valid;
    private final TriggerType triggerType;
    private final TriggerAction triggerAction;
    private final TriggerParam triggerParam;
    private JsonEntity json;

    // ==============================================================================

    private static final Map<TriggerAction, Class<?>> ACTION_PARAM_MAP;

    static
    {
        ACTION_PARAM_MAP = new HashMap<>();
        ACTION_PARAM_MAP.put(TriggerAction.LABEL_CLICK, LabelClickTriggerParam.class);
    }

    // ============================================================================

    public LabelClickTrigger (TriggerAction triggerAction, TriggerParam triggerParam)
    {
        this.triggerType = TriggerType.LABEL;
        this.triggerAction = triggerAction;
        // 检查参数是否符合类型
        if (!ACTION_PARAM_MAP.get(triggerAction).isInstance(triggerParam))
        {
            throw new IllegalArgumentException(
                "Command parameter type : " + triggerParam.getClass().getName()
                    + " does not match command action : " + triggerAction
            );
        }
        else
        {
            this.triggerParam = triggerParam;
        }
        this.valid = true;
        buildJson();
    }

    private void buildJson ()
    {
        json = new JsonEntity();
        json.put(ScriptKey.Command.TYPE, triggerType.getDisplayString());
        json.put(ScriptKey.Command.ACTION, triggerAction.getDisplayString());
        json.put(ScriptKey.Command.PARAM, triggerParam.getJson());
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
    public TriggerType getCommandType ()
    {
        return triggerType;
    }

    /**
     * 获取命令名称
     */
    @Override
    public TriggerAction getCommandAction ()
    {
        return triggerAction;
    }

    /**
     * 获取命令参数
     */
    @Override
    public TriggerParam getCommandParam ()
    {
        return triggerParam;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }

    @Override
    public String toString() {
        return "LabelClickTrigger{" +
            "valid=" + valid +
            ", triggerType=" + triggerType +
            ", triggerAction=" + triggerAction +
            ", triggerParam=" + triggerParam +
            ", json=" + json +
            '}';
    }
}
