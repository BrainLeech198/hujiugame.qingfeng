package com.hujiugame.qingfeng.script.data.command.param.story;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;

public class GotoPageStoryScriptCommandParam implements ScriptCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private JsonEntity tree;
    private String page;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("tree", tree);
        json.put("page", page);
    }

    public GotoPageStoryScriptCommandParam (String targetPageId)
    {
        valid = true;
        this.page = targetPageId;
        buildJson();
    }

    public GotoPageStoryScriptCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // tree字段
            if (!json.containsKey("tree"))
            {
                throw new IllegalArgumentException("Command parameter must have \"tree\" field. (json): " + json);
            }
            // page字段
            if (!json.containsKey("page"))
            {
                throw new IllegalArgumentException("Command parameter must have \"page\" field. (json): " + json);
            }
            tree = json.getJsonEntityByKey("tree");
            page = json.getString("page");
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
        return "GotoPageStoryScriptCommandParam{" +
            "valid=" + valid +
            ", tree=" + tree +
            ", page='" + page + '\'' +
            ", json=" + json +
            '}';
    }

    public JsonEntity getTree ()
    {
        return tree;
    }

    public String getPage ()
    {
        return page;
    }
}
