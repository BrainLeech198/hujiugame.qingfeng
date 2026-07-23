package com.hujiugame.qingfeng.script.data.command.param.control;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;

public class BreakControlScriptCommandParam implements ScriptCommandParam
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

    public BreakControlScriptCommandParam ()
    {
        valid = true;
        buildJson();
    }

    public BreakControlScriptCommandParam (JsonEntity json)
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
        return "BreakControlScriptCommandParam{" +
            "valid=" + valid +
            ", json=" + json +
            '}';
    }
}
