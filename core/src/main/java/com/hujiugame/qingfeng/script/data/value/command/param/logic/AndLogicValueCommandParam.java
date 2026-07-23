package com.hujiugame.qingfeng.script.data.value.command.param.logic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class AndLogicValueCommandParam implements ValueCommandParam
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

    public AndLogicValueCommandParam ()
    {
        valid = true;
        buildJson();
    }

    public AndLogicValueCommandParam (JsonEntity json)
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
        return "AndLogicValueCommandParam{" +
            "valid=" + valid +
            ", json=" + json +
            '}';
    }
}
