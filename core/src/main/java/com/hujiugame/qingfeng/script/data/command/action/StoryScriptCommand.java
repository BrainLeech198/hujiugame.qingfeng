package com.hujiugame.qingfeng.script.data.command.action;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandType;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.story.ForwardPageStoryScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.story.GotoPageStoryScriptCommandParam;

import java.util.HashMap;
import java.util.Map;

public class StoryScriptCommand implements ScriptCommand
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
        ACTION_PARAM_MAP.put(ScriptCommandAction.FORWARD_PAGE, ForwardPageStoryScriptCommandParam.class);
        ACTION_PARAM_MAP.put(ScriptCommandAction.GOTO_PAGE, GotoPageStoryScriptCommandParam.class);
    }

    // ============================================================================

    public StoryScriptCommand (ScriptCommandAction scriptCommandAction, ScriptCommandParam commandParam)
    {
        this.scriptCommandType = ScriptCommandType.STORY;
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
        json.put("type", scriptCommandType.getDisplayString());
        json.put("action", scriptCommandAction.getDisplayString());
        json.put("param", commandParam.getJson());
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
        return "StoryScriptCommand{" +
            "valid=" + valid +
            ", scriptCommandType=" + scriptCommandType +
            ", scriptCommandAction=" + scriptCommandAction +
            ", commandParam=" + commandParam +
            ", json=" + json +
            '}';
    }
}
