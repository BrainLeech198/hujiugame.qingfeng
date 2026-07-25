package com.hujiugame.qingfeng.script.data.value.command.param.atomic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.TypeMapper;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

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
        json.put(ScriptKey.Command.Param.Atomic.CLASS, TypeMapper.toTypeString(type));
        json.put(ScriptKey.Command.Param.Atomic.VALUE, value);
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
            if (!json.containsKey(ScriptKey.Command.Param.Atomic.CLASS))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Atomic.CLASS + "\" field. (json): " + json);
            }
            // value字段
            if (!json.containsKey(ScriptKey.Command.Param.Atomic.VALUE))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Atomic.VALUE + "\" field. (json): " + json);
            }

            type = TypeMapper.parseClass(json.getString(ScriptKey.Command.Param.Atomic.CLASS));
            if (type == int.class)
            {
                value = json.getInt(ScriptKey.Command.Param.Atomic.VALUE);
            }
            else if (type == float.class)
            {
                value = json.getFloat(ScriptKey.Command.Param.Atomic.VALUE);
            }
            else if (type == String.class)
            {
                value = json.getString(ScriptKey.Command.Param.Atomic.VALUE);
            }
            else if (type == boolean.class)
            {
                value = json.getBoolean(ScriptKey.Command.Param.Atomic.VALUE);
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
