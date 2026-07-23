package com.hujiugame.qingfeng.script.data.command.param.story;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;

public class ForwardPageStoryScriptCommandParam implements ScriptCommandParam
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

    public ForwardPageStoryScriptCommandParam ()
    {
        valid = true;
        buildJson();
    }

    public ForwardPageStoryScriptCommandParam (JsonEntity json)
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
        return "ForwardPageStoryScriptCommandParam{" +
            "valid=" + valid +
            ", json=" + json +
            '}';
    }
}
