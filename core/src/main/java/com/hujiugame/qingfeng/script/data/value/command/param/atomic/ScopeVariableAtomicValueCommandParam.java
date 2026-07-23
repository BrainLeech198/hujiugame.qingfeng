package com.hujiugame.qingfeng.script.data.value.command.param.atomic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class ScopeVariableAtomicValueCommandParam implements ValueCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private String scopeVariableName;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("name", scopeVariableName);
    }

    public ScopeVariableAtomicValueCommandParam (String scopeVariableName)
    {
        valid = true;
        this.scopeVariableName = scopeVariableName;
        buildJson();
    }

    public ScopeVariableAtomicValueCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("name"))
            {
                throw new IllegalArgumentException("Command parameter must have \"name\" field. (json): " + json);
            }
            scopeVariableName = json.getString("name");
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
        return "ScopeVariableAtomicValueCommandParam{" +
            "valid=" + valid +
            ", scopeVariableName='" + scopeVariableName + '\'' +
            ", json=" + json +
            '}';
    }

    public String getScopeVariableName ()
    {
        return scopeVariableName;
    }
}
