package com.hujiugame.qingfeng.data.story.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandParser;
import com.hujiugame.qingfeng.script.data.trigger.Trigger;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class PageBehavior
{
    private boolean valid;
    private JsonEntity json;

    private List<ScriptCommand> startScriptCommands;
    private List<ScriptCommand> loopScriptCommands;
    private List<Trigger> triggers;

    private void buildJson ()
    {
        if (!valid)
        {
            LogUtils.debug(PageBehavior.class, "buildJson 无效的页面行为不能构建json");
            return;
        }
        json = new JsonEntity();

        JsonEntity startJson = new JsonEntity();
        startJson.put("type", "inline");
        startJson.put("commands", startScriptCommands.stream().map(ScriptCommand::getJson).collect(Collectors.toList()));
        json.put("start", startJson);

        JsonEntity loopJson = new JsonEntity();
        loopJson.put("type", "inline");
        loopJson.put("commands", loopScriptCommands.stream().map(ScriptCommand::getJson).collect(Collectors.toList()));
        json.put("loop", loopJson);

        json.put("triggers", triggers.stream().map(Trigger::getJson).collect(Collectors.toList()));
    }

    public PageBehavior (List<ScriptCommand> startScriptCommands, List<ScriptCommand> loopScriptCommands, List<Trigger> triggers)
    {
        this.startScriptCommands = startScriptCommands;
        this.loopScriptCommands = loopScriptCommands;
        this.triggers = triggers;
        valid = true;
        buildJson();
    }

    public PageBehavior ()
    {
        this.startScriptCommands = new ArrayList<>();
        this.loopScriptCommands = new ArrayList<>();
        this.triggers = new ArrayList<>();
        valid = true;
        buildJson();
    }

    public PageBehavior (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            // start
            if (json.containsKey("start"))
            {
                JsonEntity startJson = json.getJsonEntityByKey("start");
                String type = startJson.getString("type");

                if (!"inline".equals(type))
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 start未知type类型 (type): " + type);
                    return;
                }
                if (!startJson.containsKey("commands"))
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 start缺少commands字段 (json): " + json);
                    return;
                }
                List<ScriptCommand> commands = ScriptCommandParser.parseList(startJson.getJsonEntityByKey("commands"));
                if (commands == null)
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 start commands解析失败 (json): " + json);
                    return;
                }
                this.startScriptCommands = commands;
            }
            else
            {
                LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 缺少start字段 (json): " + json);
                return;
            }

            // loop
            if (json.containsKey("loop"))
            {
                JsonEntity loopJson = json.getJsonEntityByKey("loop");
                String type = loopJson.getString("type");

                if (!"inline".equals(type))
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 loop未知type类型 (type): " + type);
                    return;
                }
                if (!loopJson.containsKey("commands"))
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 loop缺少commands字段 (json): " + json);
                    return;
                }
                List<ScriptCommand> commands = ScriptCommandParser.parseList(loopJson.getJsonEntityByKey("commands"));
                if (commands == null)
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 loop commands解析失败 (json): " + json);
                    return;
                }
                this.loopScriptCommands = commands;
            }
            else
            {
                LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 缺少loop字段 (json): " + json);
                return;
            }

            // triggers
            if (json.containsKey("triggers"))
            {
                List<Trigger> parsedTriggers = parseTriggers(json.getJsonEntityByKey("triggers"));
                if (parsedTriggers == null)
                {
                    LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 triggers解析失败 (json): " + json);
                    return;
                }
                this.triggers = parsedTriggers;
            }
            else
            {
                LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 缺少triggers字段 (json): " + json);
                return;
            }

            this.json = json;
            valid = true;
        }
        else
        {
            LogUtils.debug(PageBehavior.class, "PageBehavior 解析失败 需要Map数据 (json): " + json);
        }
    }

    // ===================================================================================================================
    // 解析方法
    // ===================================================================================================================

    /**
     * 解析 triggers 列表
     *
     * @param json triggers 的 JsonEntity（List 类型）
     * @return 触发器列表，解析失败返回 null
     */
    private List<Trigger> parseTriggers (JsonEntity json)
    {
        if (!json.isList())
        {
            LogUtils.debug(PageBehavior.class, "parseTriggers 需要List数据");
            return null;
        }

        List<Trigger> result = new ArrayList<>();
        List<Object> items = json.getObjectList();
        for (Object item : items)
        {
            if (item instanceof Map)
            {
                @SuppressWarnings("unchecked")
                Trigger trigger = new Trigger(new JsonEntity((Map<String, Object>) item));
                if (trigger.isValid())
                {
                    result.add(trigger);
                }
            }
        }
        return result;
    }

    // ===================================================================================================================
    // Getter
    // ===================================================================================================================

    public boolean isValid ()
    {
        return valid;
    }

    public JsonEntity getJson ()
    {
        return json;
    }

    public List<ScriptCommand> getStartScriptCommands ()
    {
        return startScriptCommands;
    }

    public List<ScriptCommand> getLoopScriptCommands ()
    {
        return loopScriptCommands;
    }

    public List<Trigger> getTriggers ()
    {
        return triggers;
    }
}
