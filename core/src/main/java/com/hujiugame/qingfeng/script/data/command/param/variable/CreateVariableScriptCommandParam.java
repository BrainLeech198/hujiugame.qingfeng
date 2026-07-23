package com.hujiugame.qingfeng.script.data.command.param.variable;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.TypeMapper;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.value.MathValue;

public class CreateVariableScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private Class<?> type;
    private String name;
    private MathValue value;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("class", TypeMapper.toTypeString(type));
        json.put("name", name);
        json.put("value", value.getJson());
    }

    public CreateVariableScriptCommandParam (Class<?> type, String name, MathValue value)
    {
        valid = true;
        this.type = type;
        this.name = name;
        this.value = value;
        buildJson();
    }

    public CreateVariableScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // class字段
            if (!json.containsKey("class"))
            {
                throw new IllegalArgumentException("Command parameter must have \"class\" field. (json): " + json);
            }
            // name字段
            if (!json.containsKey("name"))
            {
                throw new IllegalArgumentException("Command parameter must have \"name\" field. (json): " + json);
            }
            // value字段
            if (!json.containsKey("value"))
            {
                throw new IllegalArgumentException("Command parameter must have \"value\" field. (json): " + json);
            }

            name = json.getString("name");
            type = TypeMapper.parseClass(json.getString("class"));
            value = new MathValue(json.getJsonEntityByKey("value"));
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
        return "CreateVariableScriptCommandParam{" +
            "valid=" + valid +
            ", type=" + type +
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
