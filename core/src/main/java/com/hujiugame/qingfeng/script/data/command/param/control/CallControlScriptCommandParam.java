package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.ArgumentInfo;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

import java.util.List;
import java.util.stream.Collectors;

public class CallControlScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private String script;
    private List<ArgumentInfo> arguments;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put(ScriptKey.Command.Param.Control.SCRIPT, script);
        json.put(ScriptKey.ARGUMENTS, arguments.stream()
                .map(ArgumentInfo::getJson)
                .collect(Collectors.toList()));
    }

    public CallControlScriptCommandParam (String script, List<ArgumentInfo> arguments)
    {
        valid = true;
        this.script = script;
        this.arguments = arguments;
        buildJson();
    }

    public CallControlScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // script字段
            if (!json.containsKey(ScriptKey.Command.Param.Control.SCRIPT))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Control.SCRIPT + "\" field. (json): " + json);
            }
            // arguments字段
            if (!json.containsKey(ScriptKey.ARGUMENTS))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.ARGUMENTS + "\" field. (json): " + json);
            }
            script = json.getString(ScriptKey.Command.Param.Control.SCRIPT);
            arguments = json.getJsonEntityList(ScriptKey.ARGUMENTS).stream()
                    .map(ArgumentInfo::new)
                    .collect(Collectors.toList());
            this.json = json;
            valid = true;
        }
        else
        {
            throw new IllegalArgumentException("Command parameter must be a map.");
        }
    }

    @Override
    public boolean isValid ()
    {
        return valid;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }

    @Override
    public String toString() {
        return "CallControlScriptCommandParam{" +
            "valid=" + valid +
            ", script='" + script + '\'' +
            ", arguments=" + arguments +
            ", json=" + json +
            '}';
    }

    public String getScript ()
    {
        return script;
    }

    public List<ArgumentInfo> getArguments ()
    {
        return arguments;
    }
}
