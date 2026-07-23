package com.hujiugame.qingfeng.script.data.value.command.param.atomic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.TypeMapper;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class ConstAtomicValueCommandParam implements ValueCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private Class<?> type;
    private Object value;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("class", TypeMapper.toTypeString(type));
        json.put("value", value);
    }

    public ConstAtomicValueCommandParam (Class<?> type, Object value)
    {
        if (!TypeMapper.matches(type, value))
        {
            throw new IllegalArgumentException(
                "Command parameter value type : " + value.getClass().getName()
                    + " does not match command parameter class : " + type
            );
        }
        valid = true;
        this.type = type;
        this.value = value;
        buildJson();
    }

    public ConstAtomicValueCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // class字段
            if (!json.containsKey("class"))
            {
                throw new IllegalArgumentException("Command parameter must have \"class\" field. (json): " + json);
            }
            // value字段
            if (!json.containsKey("value"))
            {
                throw new IllegalArgumentException("Command parameter must have \"value\" field. (json): " + json);
            }

            type = TypeMapper.parseClass(json.getString("class"));
            if (type == int.class)
            {
                value = json.getInt("value");
            }
            else if (type == float.class)
            {
                value = json.getFloat("value");
            }
            else if (type == String.class)
            {
                value = json.getString("value");
            }
            else if (type == boolean.class)
            {
                value = json.getBoolean("value");
            }
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
        return "ConstAtomicValueCommandParam{" +
            "valid=" + valid +
            ", type=" + type +
            ", value=" + value +
            ", json=" + json +
            '}';
    }

    public Class<?> getType ()
    {
        return type;
    }

    public Object getValue ()
    {
        return value;
    }
}
