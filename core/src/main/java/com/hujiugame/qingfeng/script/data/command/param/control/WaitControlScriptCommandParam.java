package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;

public class WaitControlScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private float time;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("time", time);
    }

    public WaitControlScriptCommandParam (float time)
    {
        valid = true;
        this.time = time;
        buildJson();
    }

    public WaitControlScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("time"))
            {
                throw new IllegalArgumentException("Command parameter must have \"time\" field. (json): " + json);
            }
            time = json.getFloat("time");
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
        return "WaitControlScriptCommandParam{" +
            "valid=" + valid +
            ", time=" + time +
            ", json=" + json +
            '}';
    }

    public float getTime ()
    {
        return time;
    }
}
