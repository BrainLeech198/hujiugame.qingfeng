package com.hujiugame.qingfeng.util.json.parser;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class JsonSizeParser
{
    private final JsonEntity json;
    private int width = 100;
    private int height = 100;

    /**
     * 从 JSON 中解析 size 字段的宽度和高度
     *
     * @param json 包含 size 子对象的 JSON 数据
     */
    public JsonSizeParser (JsonEntity json)
    {
        this.json = json;

        if (json.containsKey("size"))
        {
            JsonEntity sizeJson = json.getJsonEntityByKey("size");
            if (sizeJson.containsKey("width") && sizeJson.containsKey("height"))
            {
                this.width = sizeJson.getInt("width");
                this.height = sizeJson.getInt("height");
                LogUtils.debug(JsonSizeParser.class, "JsonSizeParser 获取尺寸 (width): " +  width + " (height): " + height);
            }
            else
            {
                LogUtils.debug(JsonSizeParser.class, "JsonSizeParser 获取尺寸失败 字段存在情况" +
                    " (width): " + sizeJson.containsKey("width") +
                    " (height): " + sizeJson.containsKey("height"));
            }
        }
        else
        {
            LogUtils.debug(JsonSizeParser.class, "JsonSizeParser 获取尺寸失败 字段不存在");
        }
    }

    /**
     * 获取宽度
     */
    public int getWidth ()
    {
        return width;
    }

    /**
     * 获取高度
     */
    public int getHeight ()
    {
        return height;
    }
}
