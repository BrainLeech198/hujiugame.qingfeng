package com.hujiugame.qingfeng.util.json.parser;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class JsonPositionParser
{
    private final JsonEntity json;
    private int x = 0;
    private int y = 0;

    /**
     * 从 JSON 中解析 position 字段的 x 和 y 坐标
     *
     * @param json 包含 position 子对象的 JSON 数据
     */
    public JsonPositionParser (JsonEntity json)
    {
        this.json = json;

        if (json.containsKey("position"))
        {
            JsonEntity positionJson = json.getJsonEntityByKey("position");
            if (positionJson.containsKey("x") && positionJson.containsKey("y"))
            {
                x = positionJson.getInt("x");
                y = positionJson.getInt("y");
                LogUtils.debug(JsonPositionParser.class, "JsonPositionParser 获取坐标 (x): " +  x + " (y): " + y);
            }
            else
            {
                LogUtils.debug(JsonPositionParser.class, "JsonPositionParser 获取坐标失败 字段存在情况" +
                    " (x): " + positionJson.containsKey("x") +
                    " (y): " + positionJson.containsKey("y"));
            }
        }
        else
        {
            LogUtils.debug(JsonPositionParser.class, "JsonPositionParser 获取坐标失败 字段不存在");
        }
    }

    /**
     * 获取 X 坐标
     */
    public int getX ()
    {
        return x;
    }

    /**
     * 获取 Y 坐标
     */
    public int getY ()
    {
        return y;
    }
}
