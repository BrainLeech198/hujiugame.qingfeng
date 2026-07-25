package com.hujiugame.qingfeng.script.data.command.param.variable;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.value.MathValue;
import com.hujiugame.qingfeng.type.key.ScriptKey;

public class AssignmentVariableScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private String name;
    private MathValue value;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put(ScriptKey.Command.Param.Variable.NAME, name);
        json.put(ScriptKey.Command.Param.Variable.VALUE, value.getJson());
    }

    public AssignmentVariableScriptCommandParam (String name, MathValue value)
    {
        valid = true;
        this.name = name;
        this.value = value;
        buildJson();
    }

    public AssignmentVariableScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // name字段
            if (!json.containsKey(ScriptKey.Command.Param.Variable.NAME))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Variable.NAME + "\" field. (json): " + json);
            }
            // value字段
            if (!json.containsKey(ScriptKey.Command.Param.Variable.VALUE))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Variable.VALUE + "\" field. (json): " + json);
            }

            name = json.getString(ScriptKey.Command.Param.Variable.NAME);
            value = new MathValue(json.getJsonEntityByKey(ScriptKey.Command.Param.Variable.VALUE));
            valid = true;
            this.json = json;
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
        return "AssignmentVariableScriptCommandParam{" +
            "valid=" + valid +
            ", name='" + name + '\'' +
            ", value=" + value +
            ", json=" + json +
            '}';
    }

    public String getName ()
    {
        return name;
    }

    public MathValue getValue ()
    {
        return value;
    }
}
