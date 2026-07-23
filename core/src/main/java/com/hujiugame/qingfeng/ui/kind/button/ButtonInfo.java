package com.hujiugame.qingfeng.ui.kind.button;

import com.badlogic.gdx.graphics.Color;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.util.json.parser.JsonPositionParser;
import com.hujiugame.qingfeng.util.json.parser.JsonShowParser;
import com.hujiugame.qingfeng.util.json.parser.JsonSizeParser;
import com.hujiugame.qingfeng.util.json.parser.JsonTextParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class ButtonInfo
{
    private String buttonTag;
    private String buttonKindName;
    private boolean show;
    private int x;
    private int y;
    private int width;
    private int height;
    private TextObject textObject;
    private String fontName;
    private float fontSize;
    private Color fontColor = null;

    /**
     * 复制构造器
     *
     * @param buttonInfo 源 ButtonInfo 对象
     */
    public ButtonInfo (ButtonInfo buttonInfo)
    {
        this.buttonTag = buttonInfo.buttonTag;
        this.buttonKindName = buttonInfo.buttonKindName;
        this.show = buttonInfo.show;
        this.x = buttonInfo.x;
        this.y = buttonInfo.y;
        this.width = buttonInfo.width;
        this.height = buttonInfo.height;
        this.textObject = buttonInfo.textObject;
        this.fontName = buttonInfo.fontName;
        this.fontSize = buttonInfo.fontSize;
        this.fontColor = buttonInfo.fontColor != null ? new Color(buttonInfo.fontColor) : null;

        debug();
    }

    /**
     * 使用具体参数构造 ButtonInfo
     *
     * @param buttonTag      按钮标签
     * @param buttonKindName 按钮种类名称
     * @param show           是否显示
     * @param x              X 坐标
     * @param y              Y 坐标
     * @param width          宽度
     * @param height         高度
     * @param textObject     文本对象
     * @param fontName       字体名称
     * @param fontSize       字体大小
     * @param fontColor      字体颜色
     */
    public ButtonInfo (String buttonTag, String buttonKindName, boolean show, int x, int y, int width, int height, TextObject textObject, String fontName, float fontSize, Color fontColor)
    {
        this.buttonTag = buttonTag;
        this.buttonKindName = buttonKindName;
        this.show = show;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textObject = textObject;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontColor = new Color(fontColor);

        debug();

    }

    /**
     * 从 JSON 构造 ButtonInfo，自动解析所有属性
     * @param buttonTag 按钮标签
     * @param json 包含按钮各项属性的 JSON 数据
     */
    public ButtonInfo (String buttonTag, JsonEntity json)
    {
        LogUtils.debug(ButtonInfo.class, "ButtonInfo 创建 (buttonTag, json): " + buttonTag + ", " + json);

        // 记录tag
        this.buttonTag = buttonTag;

        // 解析kind属性
        this.buttonKindName = json.getString("kind");

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

        debug();
    }

    /**
     * 获取按钮标签
     */
    public String getButtonTag ()
    {
        return buttonTag;
    }

    /**
     * 设置按钮标签
     */
    public ButtonInfo setButtonTag (String buttonTag)
    {
        this.buttonTag = buttonTag;
        return this;
    }

    /**
     * 获取按钮种类名称
     */
    public String getButtonKindName ()
    {
        return buttonKindName;
    }

    /**
     * 设置按钮种类名称
     */
    public ButtonInfo setButtonKindName (String buttonKindName)
    {
        this.buttonKindName = buttonKindName;
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
     * 设置显示状态
     */
    public ButtonInfo setShow (boolean show)
    {
        this.show = show;
        return this;
    }

    /**
     * 获取 X 坐标
     */
    public int getX ()
    {
        return x;
    }

    /**
     * 设置 X 坐标
     */
    public ButtonInfo setX (int x)
    {
        this.x = x;
        return this;
    }

    /**
     * 获取 Y 坐标
     */
    public int getY ()
    {
        return y;
    }

    /**
     * 设置 Y 坐标
     */
    public ButtonInfo setY (int y)
    {
        this.y = y;
        return this;
    }

    /**
     * 获取宽度
     */
    public int getWidth ()
    {
        return width;
    }

    /**
     * 设置宽度
     */
    public ButtonInfo setWidth (int width)
    {
        this.width = width;
        return this;
    }

    /**
     * 获取高度
     */
    public int getHeight ()
    {
        return height;
    }

    /**
     * 设置高度
     */
    public ButtonInfo setHeight (int height)
    {
        this.height = height;
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
     * 设置文本对象
     */
    public ButtonInfo setTextObject (TextObject textObject)
    {
        this.textObject = textObject;
        return this;
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
    public ButtonInfo setFontName (String fontName)
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
    public ButtonInfo setFontSize (float fontSize)
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
    public ButtonInfo setFontColor (Color fontColor)
    {
        this.fontColor = fontColor;
        return this;
    }

    /**
     * 复制当前 ButtonInfo
     */
    public ButtonInfo copy ()
    {
        return new ButtonInfo(this);
    }


    /**
     * 同时设置 X 和 Y 坐标
     */
    public ButtonInfo setPosition (int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }


    /**
     * 同时设置宽度和高度
     */
    public ButtonInfo setSize (int width, int height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 输出调试日志
     */
    private void debug ()
    {
        LogUtils.debug(ButtonInfo.class, "ButtonInfo 新的 (buttonInfo): " + this);
    }

    /**
     * 返回 ButtonInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "ButtonInfo{" +
            "buttonTag='" + buttonTag + '\'' +
            ", buttonKindName='" + buttonKindName + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            ", text='" + textObject + '\'' +
            ", fontSize=" + fontSize +
            ", fontColor=" + fontColor +
            '}';
    }

    /**
     * 判断两个 ButtonInfo 是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ButtonInfo that = (ButtonInfo) o;
        return show == that.show &&
               x == that.x &&
               y == that.y &&
               width == that.width &&
               height == that.height &&
               Float.compare(that.fontSize, fontSize) == 0 &&
               Objects.equals(buttonTag, that.buttonTag) &&
               Objects.equals(buttonKindName, that.buttonKindName) &&
               Objects.equals(textObject, that.textObject) &&
               Objects.equals(fontName, that.fontName) &&
            Objects.equals(fontColor, that.fontColor);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(buttonTag, buttonKindName, show, x, y, width, height, textObject, fontName, fontSize, fontColor);
    }

}
