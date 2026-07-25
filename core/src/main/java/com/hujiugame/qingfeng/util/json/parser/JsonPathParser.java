package com.hujiugame.qingfeng.util.json.parser;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.key.GraphicsKey;

public class JsonPathParser
{
    private final JsonEntity json;
    private String path;

    /**
     * 从 JSON 中解析 path 字段
     *
     * @param json 包含 path 字段的 JSON 数据
     */
    public JsonPathParser (JsonEntity json)
    {
        this.json = json;

        if (json.containsKey(GraphicsKey.PATH))
        {
            path = json.getString(GraphicsKey.PATH);
        }
    }

    /**
     * 获取解析后的路径
     */
    public String getPath ()
    {
        return path;
    }

}
