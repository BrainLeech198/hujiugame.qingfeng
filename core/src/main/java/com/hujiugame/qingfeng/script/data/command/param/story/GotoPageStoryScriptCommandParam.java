package com.hujiugame.qingfeng.script.data.command.param.story;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;

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
        json.put(ScriptKey.Command.Param.Story.TREE, tree);
        json.put(ScriptKey.Command.Param.Story.PAGE, page);
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
            if (!json.containsKey(ScriptKey.Command.Param.Story.TREE))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Story.TREE + "\" field. (json): " + json);
            }
            // page字段
            if (!json.containsKey(ScriptKey.Command.Param.Story.PAGE))
            {
                throw new IllegalArgumentException("Command parameter must have \"" + ScriptKey.Command.Param.Story.PAGE + "\" field. (json): " + json);
            }
            tree = json.getJsonEntityByKey(ScriptKey.Command.Param.Story.TREE);
            page = json.getString(ScriptKey.Command.Param.Story.PAGE);
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
