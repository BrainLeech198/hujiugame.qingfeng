package com.hujiugame.qingfeng.util.json.parser;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class JsonShowParser
{

    private JsonShowParser()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 解析 JSON 中的 show 字段，判断是否显示
     *
     * @param json 包含 show 字段的 JSON 数据
     * @return 显示状态，默认为 true
     */
    public static boolean parseShow (JsonEntity json)
    {
        if (json == null) return true;
        if (json.containsKey(UiKey.Button.SHOW))
        {
            LogUtils.debug(JsonShowParser.class, "parseShow 获取显示状态 (show): " + json.getBoolean(UiKey.Button.SHOW));
            return json.getBoolean(UiKey.Button.SHOW);
        }
        else
        {
            LogUtils.debug(JsonShowParser.class, "parseShow 获取显示状态失败 字段不存在");
            return true;
        }
    }
}
