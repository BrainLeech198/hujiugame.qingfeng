package com.hujiugame.qingfeng.util.json.parser;

import com.badlogic.gdx.graphics.Color;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.key.JsonKey;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.type.ui.FontFlag;
import com.hujiugame.qingfeng.manager.TextManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class JsonTextParser
{

    private JsonTextParser()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    private static TextManager textManager = null;

    /**
     * 初始化文本管理器
     *
     * @param textManager 文本管理器实例
     * @return 初始化成功返回 true
     */
    public static boolean init (TextManager textManager)
    {
        try
        {
            JsonTextParser.textManager = textManager;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "init", e);
            return false;
        }
    }

    /**
     * 解析 JSON 中的文本字段，优先使用 textKey（需解析），其次使用 text（纯文本）
     *
     * @param json 包含文本字段的 JSON 数据
     * @return 解析得到的 TextObject
     */
    public static TextObject parseText (JsonEntity json)
    {
        try
        {
            if (json.containsKey(JsonKey.Text.TEXT_KEY))
            {
                LogUtils.debug(JsonTextParser.class, "parseText 获取文本成功(包含解析)");
                return new TextObject(textManager, json.getString(JsonKey.Text.TEXT_KEY));
            }
            else if (json.containsKey(JsonKey.Text.TEXT))
            {
                LogUtils.debug(JsonTextParser.class, "parseText 获取文本成功(纯文本)");
                return new TextObject(null, json.getString(JsonKey.Text.TEXT));
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseText 获取文本失败 字段不存在");
                return new TextObject(null, "");
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseText", e);
            return new TextObject(null, "");
        }
    }

    /**
     * 解析 JSON 中的 fontName 字段
     * @param json 包含字体名称字段的 JSON 数据
     * @return 字体名称，不存在时返回 null
     */
    @javax.annotation.Nullable
    public static String parseFontName (JsonEntity json)
    {
        try
        {
            if (json.containsKey(JsonKey.Font.FONT_NAME))
            {
                String fontName = json.getString(JsonKey.Font.FONT_NAME);
                LogUtils.debug(JsonTextParser.class, "parseFontName 获取字体名称成功 (fontName): " + fontName);
                return fontName;
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseFontName 获取字体名称失败 字段不存在");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseFontName", e);
            return null;
        }
    }

    /**
     * 解析 JSON 中的 fontSize 字段
     * @param json 包含字体大小字段的 JSON 数据
     * @return 字体大小，不存在时返回 1.0f
     */
    public static float parseFontSize (JsonEntity json)
    {
        try
        {
            float fontSize = 1.0f;

            if (json.containsKey(JsonKey.Font.FONT_SIZE))
            {
                fontSize = json.getFloat(JsonKey.Font.FONT_SIZE);
                LogUtils.debug(JsonTextParser.class, "parseFontSize 获取文本大小 (fontSize): " + fontSize);
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseFontSize 获取文本大小失败 字段不存在");
            }

            return fontSize;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseFontSize", e);
            return 1.0f;
        }
    }

    /**
     * 解析 JSON 中的 fontColor 字段
     * @param json 包含字体颜色字段的 JSON 数据
     * @return 字体颜色，不存在时返回 null
     */
    @javax.annotation.Nullable
    public static Color parseFontColor (JsonEntity json)
    {
        try
        {
            Color fontColor = null;

            if (json.containsKey(JsonKey.Font.FONT_COLOR))
            {
                String fontColorStr = json.getString(JsonKey.Font.FONT_COLOR);
                if (fontColorStr != null)
                {
                    fontColor = Color.valueOf(fontColorStr);
                    LogUtils.debug(JsonTextParser.class, "parseFontColor 获取文本颜色 (fontColor): " + fontColor);
                }
                else
                {
                    LogUtils.error(JsonTextParser.class, "parseFontColor fontColor 字段值类型不是字符串");
                }
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseFontColor 获取文本颜色失败 字段不存在");
            }

            return fontColor;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseFontColor", e);
            return null;
        }
    }

    /**
     * 解析 JSON 中的 fontFlag 字段
     * @param json 包含字体对齐标志字段的 JSON 数据
     * @param defaultFontFlag 默认字体对齐标志
     * @return 字体对齐标志
     */
    public static FontFlag parseFontFlag (JsonEntity json, FontFlag defaultFontFlag)
    {
        if (defaultFontFlag == null)
        {
            defaultFontFlag = FontFlag.NW;
        }

        try
        {
            FontFlag fontFlag = defaultFontFlag;

            if (json.containsKey(JsonKey.Font.FONT_FLAG))
            {
                fontFlag = FontFlag.valueOf(json.getString(JsonKey.Font.FONT_FLAG).toUpperCase());
                LogUtils.debug(JsonTextParser.class, "parseFontFlag 获取文本对齐方式 (fontFlag): " + fontFlag);
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseFontFlag 获取文本对齐方式失败 字段不存在");
            }

            return fontFlag;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseFontFlag", e);
            return defaultFontFlag;
        }
    }

    /**
     * 解析 JSON 中的 fontArgs 字段
     * @param json 包含字体参数字段的 JSON 数据
     * @return 字体参数 JsonEntity
     */
    public static JsonEntity parseFontArgs (JsonEntity json)
    {
        try
        {
            JsonEntity fontArgs = new JsonEntity();

            if (json.containsKey(JsonKey.Font.FONT_ARGS))
            {
                fontArgs = json.getJsonEntityByKey(JsonKey.Font.FONT_ARGS);
                LogUtils.debug(JsonTextParser.class, "parseFontArgs 获取文本参数 (fontArgs): " + fontArgs);
            }
            else
            {
                LogUtils.debug(JsonTextParser.class, "parseFontArgs 获取文本参数失败 字段不存在");
            }

            return fontArgs;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonTextParser.class, "parseFontArgs", e);
            return new JsonEntity();
        }
    }
}
