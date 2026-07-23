package com.hujiugame.qingfeng.graphic.model;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.json.parser.JsonPositionParser;
import com.hujiugame.qingfeng.util.json.parser.JsonSizeParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class GifInfo
{
    private String tag;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * 复制构造器
     *
     * @param gifInfo 源 GifInfo 对象
     */
    public GifInfo (GifInfo gifInfo)
    {
        this.tag = gifInfo.tag;
        this.x = gifInfo.x;
        this.y = gifInfo.y;
        this.width = gifInfo.width;
        this.height = gifInfo.height;

        debug();
    }

    /**
     * 使用具体参数构造 GifInfo
     *
     * @param tag    标签
     * @param x      X 坐标
     * @param y      Y 坐标
     * @param width  宽度
     * @param height 高度
     */
    public GifInfo (String tag, int x, int y, int width, int height)
    {
        this.tag = tag;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        debug();
    }

    /**
     * 从 JSON 构造 GifInfo，自动解析坐标和尺寸
     * @param tag 标签
     * @param json 包含位置和尺寸信息的 JSON 数据
     */
    public GifInfo (String tag, JsonEntity json)
    {
        this.tag = tag;

        // 解析坐标属性
        JsonPositionParser positionInfo = new JsonPositionParser(json);
        this.x = positionInfo.getX();
        this.y = positionInfo.getY();

        // 解析宽高属性
        JsonSizeParser sizeInfo = new JsonSizeParser(json);
        this.width = sizeInfo.getWidth();
        this.height = sizeInfo.getHeight();

        debug();
    }

    /**
     * 获取标签
     */
    public String getTag ()
    {
        return tag;
    }

    /**
     * 设置标签
     */
    public void setTag (String tag)
    {
        this.tag = tag;
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
    public void setX (int x)
    {
        this.x = x;
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
    public void setY (int y)
    {
        this.y = y;
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
    public void setWidth (int width)
    {
        this.width = width;
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
    public void setHeight (int height)
    {
        this.height = height;
    }

    /**
     * 输出调试日志
     */
    public void debug ()
    {
        LogUtils.debug(GifInfo.class, "GifInfo 新的 (gifInfo): " + this);
    }

    /**
     * 返回 GifInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "GifInfo{" +
            "tag='" + tag + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            '}';
    }

    /**
     * 判断两个 GifInfo 是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GifInfo that = (GifInfo) o;
        return x == that.x &&
               y == that.y &&
               width == that.width &&
               height == that.height &&
               Objects.equals(tag, that.tag);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(tag, x, y, width, height);
    }

}
