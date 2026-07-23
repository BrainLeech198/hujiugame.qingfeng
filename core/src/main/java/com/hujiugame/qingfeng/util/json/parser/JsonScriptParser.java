package com.hujiugame.qingfeng.util.json.parser;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class JsonScriptParser
{
    private JsonScriptParser()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 解析命令 type 字段
     *
     * @param json 包含命令属性的 JSON 数据
     * @return 命令类型，不存在或失败时返回 null
     */
    public static String parseType (JsonEntity json)
    {
        try
        {
            if (json.containsKey("type"))
            {
                String type = json.getString("type");
                LogUtils.debug(JsonScriptParser.class, "parseType 获取命令类型 (type): " + type);
                return type;
            }
            else
            {
                LogUtils.debug(JsonScriptParser.class, "parseType 获取命令类型失败 字段不存在");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonScriptParser.class, "parseType", e);
            return null;
        }
    }

    /**
     * 解析命令 action 字段
     *
     * @param json 包含命令属性的 JSON 数据
     * @return 具体动作，不存在或失败时返回 null
     */
    public static String parseAction (JsonEntity json)
    {
        try
        {
            if (json.containsKey("action"))
            {
                String action = json.getString("action");
                LogUtils.debug(JsonScriptParser.class, "parseAction 获取具体动作 (action): " + action);
                return action;
            }
            else
            {
                LogUtils.debug(JsonScriptParser.class, "parseAction 获取具体动作失败 字段不存在");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonScriptParser.class, "parseAction", e);
            return null;
        }
    }

    /**
     * 解析脚本文件名字段
     *
     * @param json 包含命令属性的 JSON 数据
     * @return 脚本文件名，不存在或失败时返回 null
     */
    public static String parseScript (JsonEntity json)
    {
        try
        {
            if (json.containsKey("script"))
            {
                String script = json.getString("script");
                LogUtils.debug(JsonScriptParser.class, "parseScript 获取脚本文件 (script): " + script);
                return script;
            }
            else
            {
                LogUtils.debug(JsonScriptParser.class, "parseScript 获取脚本文件失败 字段不存在");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonScriptParser.class, "parseScript", e);
            return null;
        }
    }
}
