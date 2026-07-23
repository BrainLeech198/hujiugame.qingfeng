package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandParser;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.value.LogicValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IfControlScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private LogicValue condition;
    private List<ScriptCommand> thenCommands;
    private List<ScriptCommand> elseCommands;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("condition", condition.getJson());
        json.put("thenCommands", thenCommands != null
            ? thenCommands.stream().map(ScriptCommand::getJson).collect(Collectors.toList())
            : Collections.emptyList());
        json.put("elseCommands", elseCommands != null
            ? elseCommands.stream().map(ScriptCommand::getJson).collect(Collectors.toList())
            : Collections.emptyList());
    }

    public IfControlScriptCommandParam (LogicValue condition, List<ScriptCommand> thenCommands, List<ScriptCommand> elseCommands)
    {
        valid = true;
        this.condition = condition;
        this.thenCommands = thenCommands;
        this.elseCommands = elseCommands;
        buildJson();
    }

    public IfControlScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("condition"))
            {
                throw new IllegalArgumentException("Command parameter must have \"condition\" field. (json): " + json);
            }
            if (!json.containsKey("thenCommands"))
            {
                throw new IllegalArgumentException("Command parameter must have \"thenCommands\" field. (json): " + json);
            }
            if (!json.containsKey("elseCommands"))
            {
                throw new IllegalArgumentException("Command parameter must have \"elseCommands\" field. (json): " + json);
            }
            condition = new LogicValue(json.getJsonEntityByKey("condition"));
            thenCommands = ScriptCommandParser.parseList(json.getJsonEntityByKey("thenCommands"));
            elseCommands = ScriptCommandParser.parseList(json.getJsonEntityByKey("elseCommands"));
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
        return "IfControlScriptCommandParam{" +
            "valid=" + valid +
            ", condition=" + condition +
            ", thenCommands=" + thenCommands +
            ", elseCommands=" + elseCommands +
            ", json=" + json +
            '}';
    }

    public LogicValue getCondition ()
    {
        return condition;
    }

    public List<ScriptCommand> getThenCommands ()
    {
        return thenCommands;
    }

    public List<ScriptCommand> getElseCommands ()
    {
        return elseCommands;
    }
}
