package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.script.data.value.MathValue;

public class ReturnControlScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private MathValue value;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("value", value.getJson());
    }

    public ReturnControlScriptCommandParam (MathValue value)
    {
        valid = true;
        this.value = value;
        buildJson();
    }

    public ReturnControlScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // value字段
            if (!json.containsKey("value"))
            {
                throw new IllegalArgumentException("Command parameter must have \"value\" field. (json): " + json);
            }

            value = new MathValue(json.getJsonEntityByKey("value"));
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
        return "ReturnControlScriptCommandParam{" +
            "valid=" + valid +
            ", value=" + value +
            ", json=" + json +
            '}';
    }

    public MathValue getValue ()
    {
        return value;
    }
}
