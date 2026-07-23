package com.hujiugame.qingfeng.script.data;

import com.hujiugame.qingfeng.data.JsonEntity;

public class ArgumentInfo
{
    private boolean valid;
    private JsonEntity json;

    private String argumentName;
    private ArgumentType type;
    private String name;
    private Object value;

    public static enum ArgumentType
    {
        CONST,
        VARIABLE,
        SCOPE_VARIABLE,
        GAME_VARIABLE,
    }

    public void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid argument info cannot be built.");
        }
        json = new JsonEntity();
        json.put("argumentName", argumentName);
        json.put("type", type.toString());
        if (type == ArgumentType.CONST)
        {
            json.put("value", value);
        }
        else
        {
            json.put("name", name);
        }
    }

    public ArgumentInfo (String argumentName, ArgumentType type, String name)
    {
        if (type != ArgumentType.CONST)
        {
            valid = true;
            this.argumentName = argumentName;
            this.type = type;
            this.name = name;
            this.value = null;
            buildJson();
        }
    }

    public ArgumentInfo (String argumentName, ArgumentType type, Object value)
    {
        if (type == ArgumentType.CONST)
        {
            valid = true;
            this.argumentName = argumentName;
            this.type = type;
            this.name = null;
            this.value = value;
            buildJson();
        }
    }

    public ArgumentInfo (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // argumentName字段
            if (!json.containsKey("argumentName"))
            {
                throw new IllegalArgumentException("Argument info must have \"argumentName\" field. (json): " + json);
            }
            // type字段
            if (!json.containsKey("type"))
            {
                throw new IllegalArgumentException("Argument info must have \"type\" field. (json): " + json);
            }

            argumentName = json.getString("argumentName");
            type = ArgumentType.valueOf(json.getString("type"));
            if (type == ArgumentType.CONST)
            {
                // value字段
                if (!json.containsKey("value"))
                {
                    throw new IllegalArgumentException("Argument info must have \"value\" field. (json): " + json);
                }
                value = json.getObject("value");
            }
            else
            {
                // name字段
                if (!json.containsKey("name"))
                {
                    throw new IllegalArgumentException("Argument info must have \"name\" field. (json): " + json);
                }
                name = json.getString("name");
            }

            this.json = json;
            valid = true;
        }
        else
        {
            throw new IllegalArgumentException("Argument info must be a map.");
        }
    }

    public boolean isValid ()
    {
        return valid;
    }

    public JsonEntity getJson ()
    {
        return json;
    }

    public ArgumentType getType ()
    {
        return type;
    }

    public String getArgumentName ()
    {
        return argumentName;
    }

    public String getName ()
    {
        return name;
    }

    public Object getValue ()
    {
        return value;
    }

    @Override
    public String toString() {
        return "ArgumentInfo{" +
            "valid=" + valid +
            ", argumentName='" + argumentName + '\'' +
            ", type=" + type +
            ", name='" + name + '\'' +
            ", value=" + value +
            ", json=" + json +
            '}';
    }
}
