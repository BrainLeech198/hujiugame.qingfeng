package com.hujiugame.qingfeng.script.data.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandType;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.variable.AssignmentVariableScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.variable.CreateVariableScriptCommandParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.HashMap;
import java.util.Map;

public class VariableScriptCommand implements ScriptCommand
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
        ACTION_PARAM_MAP.put(ScriptCommandAction.CREATE, CreateVariableScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.ASSIGNMENT, AssignmentVariableScriptCommandParam.class);
    }

    // ============================================================================

    public VariableScriptCommand (ScriptCommandAction scriptCommandAction, ScriptCommandParam commandParam)
    {
        this.scriptCommandType = ScriptCommandType.VARIABLE;
        this.scriptCommandAction = scriptCommandAction;
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

    @Override
    public boolean isValid ()
    {
        return valid;
    }

    @Override
    public ScriptCommandType getCommandType ()
    {
        return scriptCommandType;
    }

    @Override
    public ScriptCommandAction getCommandAction ()
    {
        return scriptCommandAction;
    }

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
        return "VariableScriptCommand{" +
            "valid=" + valid +
            ", scriptCommandType=" + scriptCommandType +
            ", scriptCommandAction=" + scriptCommandAction +
            ", commandParam=" + commandParam +
            ", json=" + json +
            '}';
    }
}
