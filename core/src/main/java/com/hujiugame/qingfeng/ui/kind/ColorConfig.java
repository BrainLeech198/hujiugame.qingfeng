package com.hujiugame.qingfeng.ui.kind;

import com.badlogic.gdx.graphics.Color;

import java.util.Objects;

public final class ColorConfig
{
    private Color primaryColor = Color.valueOf("#FF000000");
    private Color secondaryColor = Color.valueOf("#FF000000");
    private Color fontColor = Color.valueOf("#FFFFFFFF");
    private Color reverseColor = Color.valueOf("#FFFFFFFF");

    /**
     * 创建颜色配置
     *
     * @param primaryColorHex   主颜色（hex 字符串，如 "#FF000000"）
     * @param secondaryColorHex 次要颜色（hex 字符串）
     * @param fontColorHex      字体颜色（hex 字符串）
     */
    public ColorConfig (String primaryColorHex, String secondaryColorHex, String fontColorHex)
    {
        this.primaryColor = Color.valueOf(primaryColorHex);
        this.secondaryColor = Color.valueOf(secondaryColorHex);
        this.fontColor = Color.valueOf(fontColorHex);
        this.reverseColor = new Color(1f - this.primaryColor.r, 1f - this.primaryColor.g, 1f - this.primaryColor.b, this.primaryColor.a);
    }

    /**
     * 获取主颜色
     */
    public Color getPrimaryColor ()
    {
        return primaryColor;
    }

    /**
     * 获取反色
     */
    public Color getReverseColor ()
    {
        return reverseColor;
    }

    /**
     * 获取字体颜色
     */
    public Color getFontColor ()
    {
        return fontColor;
    }

    /**
     * 获取次要颜色
     */
    public Color getSecondaryColor ()
    {
        return secondaryColor;
    }

    /**
     * 判断两个颜色配置是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorConfig that = (ColorConfig) o;
        return Objects.equals(primaryColor, that.primaryColor) &&
               Objects.equals(secondaryColor, that.secondaryColor) &&
               Objects.equals(fontColor, that.fontColor) &&
               Objects.equals(reverseColor, that.reverseColor);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(primaryColor, secondaryColor, fontColor, reverseColor);
    }

}
