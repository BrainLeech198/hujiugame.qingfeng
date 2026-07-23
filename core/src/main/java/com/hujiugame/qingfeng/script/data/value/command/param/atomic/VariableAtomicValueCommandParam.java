package com.hujiugame.qingfeng.script.data.value.command.param.atomic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class VariableAtomicValueCommandParam implements ValueCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private String variableName;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("name", variableName);
    }

    public VariableAtomicValueCommandParam (String variableName)
    {
        valid = true;
        this.variableName = variableName;
        buildJson();
    }

    public VariableAtomicValueCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("name"))
            {
                throw new IllegalArgumentException("Command parameter must have \"name\" field. (json): " + json);
            }
            variableName = json.getString("name");
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
        return "VariableAtomicValueCommandParam{" +
            "valid=" + valid +
            ", variableName='" + variableName + '\'' +
            ", json=" + json +
            '}';
    }

    public String getVariableName ()
    {
        return variableName;
    }
}
