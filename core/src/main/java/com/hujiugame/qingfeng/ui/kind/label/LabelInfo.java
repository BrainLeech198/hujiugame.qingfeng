package com.hujiugame.qingfeng.ui.kind.label;

import com.badlogic.gdx.graphics.Color;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.type.ui.FontFlag;
import com.hujiugame.qingfeng.util.json.parser.JsonPositionParser;
import com.hujiugame.qingfeng.util.json.parser.JsonShowParser;
import com.hujiugame.qingfeng.util.json.parser.JsonSizeParser;
import com.hujiugame.qingfeng.util.json.parser.JsonTextParser;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class LabelInfo
{
    private String labelTag;
    private String labelKindName;
    private boolean show;
    private float x;
    private float y;
    private float width;
    private float height;
    private TextObject textObject;
    private String fontName;
    private float fontSize;
    private Color fontColor = null;
    private FontFlag fontFlag;
    private JsonEntity fontArgs;

    /**
     * 复制构造器
     *
     * @param labelInfo 源 LabelInfo 对象
     */
    public LabelInfo (LabelInfo labelInfo)
    {
        this.labelTag = labelInfo.labelTag;
        this.labelKindName = labelInfo.labelKindName;
        this.show = labelInfo.show;
        this.x = labelInfo.x;
        this.y = labelInfo.y;
        this.width = labelInfo.width;
        this.height = labelInfo.height;
        this.textObject = labelInfo.textObject;
        this.fontName = labelInfo.fontName;
        this.fontSize = labelInfo.fontSize;
        this.fontColor = labelInfo.fontColor != null ? new Color(labelInfo.fontColor) : null;
        this.fontFlag = labelInfo.fontFlag;
        this.fontArgs = labelInfo.fontArgs != null ? new JsonEntity(labelInfo.fontArgs) : null;

        debug();
    }

    /**
     * 使用具体参数构造 LabelInfo
     *
     * @param labelTag      标签
     * @param labelKindName 标签种类名称
     * @param show          是否显示
     * @param x             X 坐标
     * @param y             Y 坐标
     * @param width         宽度
     * @param height        高度
     * @param textObject    文本对象
     * @param fontName      字体名称
     * @param fontSize      字体大小
     * @param fontColor     字体颜色
     * @param fontFlag      字体对齐标志
     * @param fontArgs      字体参数
     */
    public LabelInfo (String labelTag, String labelKindName, boolean show, float x, float y, float width, float height, TextObject textObject, String fontName, float fontSize, Color fontColor, FontFlag fontFlag, JsonEntity fontArgs)
    {
        this.labelTag = labelTag;
        this.labelKindName = labelKindName;
        this.show = show;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textObject = textObject;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontColor = new Color(fontColor);
        this.fontFlag = fontFlag;
        this.fontArgs = new JsonEntity(fontArgs);

        debug();
    }

    /**
     * 从 JSON 构造 LabelInfo，自动解析所有属性
     * @param labelTag 标签
     * @param json 包含标签各项属性的 JSON 数据
     */
    public LabelInfo (String labelTag, JsonEntity json)
    {
        LogUtils.debug(LabelInfo.class, "LabelInfo 创建 (labelTag, json): " + labelTag + ", " + json);

        // 记录tag
        this.labelTag = labelTag;

        // 解析kind属性
        this.labelKindName = json.getString(UiKey.Label.KIND);

        // 解析show属性
        this.show = JsonShowParser.parseShow(json);

        // 解析坐标属性
        JsonPositionParser jsonPositionParser = new JsonPositionParser(json);
        this.x = jsonPositionParser.getX();
        this.y = jsonPositionParser.getY();

        // 解析宽高属性
        JsonSizeParser jsonSizeParser = new JsonSizeParser(json);
        this.width = jsonSizeParser.getWidth();
        this.height = jsonSizeParser.getHeight();

        // 解析文本属性
        this.textObject = JsonTextParser.parseText(json);

        // 解析字体属性
        this.fontName = JsonTextParser.parseFontName(json);

        // 解析字体大小
        this.fontSize = JsonTextParser.parseFontSize(json);

        // 文字颜色可无
        this.fontColor = JsonTextParser.parseFontColor(json);

        // 文字样式可无
        this.fontFlag = JsonTextParser.parseFontFlag(json, FontFlag.NW);

        // 字体参数可无
        this.fontArgs = JsonTextParser.parseFontArgs(json);

        debug();
    }

    /**
     * 获取标签
     */
    public String getLabelTag ()
    {
        return labelTag;
    }

    /**
     * 设置标签
     */
    public LabelInfo setLabelTag (String labelTag)
    {
        this.labelTag = labelTag;
        return this;
    }

    /**
     * 获取标签种类名称
     */
    public String getLabelKindName ()
    {
        return labelKindName;
    }

    /**
     * 设置标签种类名称
     */
    public LabelInfo setLabelKindName (String labelKindName)
    {
        this.labelKindName = labelKindName;
        return this;
    }

    /**
     * 获取显示状态
     */
    public boolean getShow ()
    {
        return show;
    }

    /**
     * 设置显示状态（私有）
     */
    private LabelInfo setShow (boolean show)
    {
        this.show = show;
        return this;
    }

    /**
     * 获取 X 坐标
     */
    public float getX ()
    {
        return x;
    }

    /**
     * 设置 X 坐标
     */
    public LabelInfo setX (int x)
    {
        this.x = x;
        return this;
    }

    /**
     * 获取 Y 坐标
     */
    public float getY ()
    {
        return y;
    }

    /**
     * 设置 Y 坐标
     */
    public LabelInfo setY (int y)
    {
        this.y = y;
        return this;
    }

    /**
     * 获取宽度
     */
    public float getWidth ()
    {
        return width;
    }

    /**
     * 设置宽度
     */
    public LabelInfo setWidth (int width)
    {
        this.width = width;
        return this;
    }

    /**
     * 获取高度
     */
    public float getHeight ()
    {
        return height;
    }

    /**
     * 设置高度
     */
    public LabelInfo setHeight (int height)
    {
        this.height = height;
        return this;
    }

    /**
     * 同时设置 X 和 Y 坐标
     */
    public LabelInfo setPosition (int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * 获取文本对象
     */
    public TextObject getTextObject ()
    {
        return textObject;
    }

    /**
     * 获取字体名称
     */
    public String getFontName ()
    {
        return fontName;
    }

    /**
     * 设置字体名称
     */
    public LabelInfo setFontName (String fontName)
    {
        this.fontName = fontName;
        return this;
    }

    /**
     * 获取字体大小
     */
    public float getFontSize ()
    {
        return fontSize;
    }

    /**
     * 设置字体大小
     */
    public LabelInfo setFontSize (float fontSize)
    {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * 获取字体颜色
     */
    public Color getFontColor ()
    {
        return fontColor;
    }

    /**
     * 设置字体颜色
     */
    public LabelInfo setFontColor (Color fontColor)
    {
        this.fontColor = fontColor;
        return this;
    }

    /**
     * 获取字体对齐标志
     */
    public FontFlag getFontFlag ()
    {
        return fontFlag;
    }

    /**
     * 设置字体对齐标志
     */
    public LabelInfo setFontFlag (FontFlag fontFlag)
    {
        this.fontFlag = fontFlag;
        return this;
    }

    /**
     * 获取字体参数
     */
    public JsonEntity getFontArgs ()
    {
        return fontArgs;
    }

    /**
     * 设置字体参数
     */
    public LabelInfo setFontArgs (JsonEntity fontArgs)
    {
        this.fontArgs = fontArgs;
        return this;
    }

    /**
     * 输出调试日志
     */
    private void debug ()
    {
        LogUtils.debug(LabelInfo.class, "LabelInfo 新的 (labelInfo): " + this);
    }

    /**
     * 返回 LabelInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "LabelInfo{" +
            "labelTag='" + labelTag + '\'' +
            ", labelKindName='" + labelKindName + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            ", text='" + textObject + '\'' +
            ", fontSize=" + fontSize +
            ", fontColor=" + fontColor +
            ", fontFlag=" + fontFlag +
            ", fontArgs=" + fontArgs +
            '}';
    }

    /**
     * 判断两个 LabelInfo 是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelInfo that = (LabelInfo) o;
        return show == that.show &&
               Float.compare(that.x, x) == 0 &&
               Float.compare(that.y, y) == 0 &&
               Float.compare(that.width, width) == 0 &&
               Float.compare(that.height, height) == 0 &&
               Float.compare(that.fontSize, fontSize) == 0 &&
               Objects.equals(labelTag, that.labelTag) &&
               Objects.equals(labelKindName, that.labelKindName) &&
               Objects.equals(textObject, that.textObject) &&
               Objects.equals(fontName, that.fontName) &&
               Objects.equals(fontColor, that.fontColor) &&
               Objects.equals(fontFlag, that.fontFlag) &&
            Objects.equals(fontArgs, that.fontArgs);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(labelTag, labelKindName, show, x, y, width, height, textObject, fontName, fontSize, fontColor, fontFlag, fontArgs);
    }

}
