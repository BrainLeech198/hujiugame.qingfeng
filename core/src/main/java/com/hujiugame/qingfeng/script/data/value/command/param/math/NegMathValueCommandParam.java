package com.hujiugame.qingfeng.script.data.value.command.param.math;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class NegMathValueCommandParam implements ValueCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
    }

    public NegMathValueCommandParam ()
    {
        valid = true;
        buildJson();
    }

    public NegMathValueCommandParam (JsonEntity json)
    {
        valid = true;
        this.json = new JsonEntity();
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
        return "NegMathValueCommandParam{" +
            "valid=" + valid +
            ", json=" + json +
            '}';
    }
}
