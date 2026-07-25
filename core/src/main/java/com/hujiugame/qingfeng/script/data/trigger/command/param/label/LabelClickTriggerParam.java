package com.hujiugame.qingfeng.script.data.trigger.command.param.label;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.trigger.command.param.TriggerParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

public class LabelClickTriggerParam implements TriggerParam
{
    private boolean valid;
    private JsonEntity json;

    private String tag;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put(ScriptKey.Trigger.Param.Label.TAG, tag);
    }

    public LabelClickTriggerParam (String tag)
    {
        this.tag = tag;
        valid = true;
        buildJson();
    }

    public LabelClickTriggerParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // tag字段
            if (!json.containsKey(ScriptKey.Trigger.Param.Label.TAG))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Trigger.Param.Label.TAG + "\" field. (json): " + json);
            }
            tag = json.getString(ScriptKey.Trigger.Param.Label.TAG);
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
        return "LabelClickTriggerParam{" +
            "valid=" + valid +
            ", tag='" + tag + '\'' +
            ", json=" + json +
            '}';
    }

    public String getTag ()
    {
        return tag;
    }
}
