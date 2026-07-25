package com.hujiugame.qingfeng.script.data.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandType;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.*;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.HashMap;
import java.util.Map;

public class ControlScriptCommand implements ScriptCommand
{
    private boolean valid;
    private final ScriptCommandType scriptCommandType;
    private final ScriptCommandAction scriptCommandAction;
    private final ScriptCommandParam commandParam;
    private JsonEntity json;

    // ==============================================================================

    private static final Map<ScriptCommandAction, Class<?>> ACTION_PARAM_MAP;

    static
    {
        ACTION_PARAM_MAP = new HashMap<>();
        ACTION_PARAM_MAP.put(ScriptCommandAction.WHILE, WhileControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.IF, IfControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.BREAK, BreakControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.CONTINUE, ContinueControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.RETURN, ReturnControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.WAIT, WaitControlScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.CALL, CallControlScriptCommandParam.class);
    }

    // ============================================================================

    public ControlScriptCommand (ScriptCommandAction scriptCommandAction, ScriptCommandParam commandParam)
    {
        this.scriptCommandType = ScriptCommandType.CONTROL;
        this.scriptCommandAction = scriptCommandAction;
        // 检查参数是否符合类型
        if (!ACTION_PARAM_MAP.get(scriptCommandAction).isInstance(commandParam))
        {
            throw new IllegalArgumentException(
                "Command parameter type : " + commandParam.getClass().getName()
                    + " does not match command action : " + scriptCommandAction
            );
        }
        else
        {
            this.commandParam = commandParam;
        }
        this.valid = true;
        buildJson();
    }

    private void buildJson ()
    {
        json = new JsonEntity();
        json.put(ScriptKey.Command.TYPE, scriptCommandType.getDisplayString());
        json.put(ScriptKey.Command.ACTION, scriptCommandAction.getDisplayString());
        json.put(ScriptKey.Command.PARAM, commandParam.getJson());
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
    public ScriptCommandType getCommandType ()
    {
        return scriptCommandType;
    }

    /**
     * 获取命令名称
     */
    @Override
    public ScriptCommandAction getCommandAction ()
    {
        return scriptCommandAction;
    }

    /**
     * 获取命令参数
     */
    @Override
    public ScriptCommandParam getCommandParam ()
    {
        return commandParam;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }

    @Override
    public String toString() {
        return "ControlScriptCommand{" +
            "valid=" + valid +
            ", scriptCommandType=" + scriptCommandType +
            ", scriptCommandAction=" + scriptCommandAction +
            ", commandParam=" + commandParam +
            ", json=" + json +
            '}';
    }
}
