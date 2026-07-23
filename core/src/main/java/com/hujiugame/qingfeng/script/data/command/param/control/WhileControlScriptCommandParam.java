package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandParser;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.value.LogicValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhileControlScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private LogicValue condition;
    private List<ScriptCommand> commands;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("condition", condition.getJson());
        json.put("commands", commands != null
            ? commands.stream().map(ScriptCommand::getJson).collect(Collectors.toList())
            : Collections.emptyList());
    }

    public WhileControlScriptCommandParam (LogicValue condition, List<ScriptCommand> commands)
    {
        valid = true;
        this.condition = condition;
        this.commands = commands;
        buildJson();
    }

    public WhileControlScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("condition"))
            {
                throw new IllegalArgumentException("Command parameter must have \"condition\" field. (json): " + json);
            }
            if (!json.containsKey("commands"))
            {
                throw new IllegalArgumentException("Command parameter must have \"commands\" field. (json): " + json);
            }
            condition = new LogicValue(json.getJsonEntityByKey("condition"));
            commands = ScriptCommandParser.parseList(json.getJsonEntityByKey("commands"));
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
        return "WhileControlScriptCommandParam{" +
            "valid=" + valid +
            ", condition=" + condition +
            ", commands=" + commands +
            ", json=" + json +
            '}';
    }

    public LogicValue getCondition ()
    {
        return condition;
    }

    public List<ScriptCommand> getCommands ()
    {
        return commands;
    }
}
