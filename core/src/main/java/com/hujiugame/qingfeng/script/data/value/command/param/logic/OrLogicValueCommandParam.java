package com.hujiugame.qingfeng.script.data.value.command.param.logic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class OrLogicValueCommandParam implements ValueCommandParam
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

    public OrLogicValueCommandParam ()
    {
        valid = true;
        buildJson();
    }

    public OrLogicValueCommandParam (JsonEntity json)
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
        return "OrLogicValueCommandParam{" +
            "valid=" + valid +
            ", json=" + json +
            '}';
    }
}
