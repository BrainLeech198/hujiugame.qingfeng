package com.hujiugame.qingfeng.graphic.model;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.json.parser.JsonPathParser;
import com.hujiugame.qingfeng.util.json.parser.JsonPositionParser;
import com.hujiugame.qingfeng.util.json.parser.JsonSizeParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class PictureInfo
{
    private String tag;
    private String path;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * 复制构造器
     *
     * @param pictureInfo 源 PictureInfo 对象
     */
    public PictureInfo (PictureInfo pictureInfo)
    {
        this.tag = pictureInfo.tag;
        this.path = pictureInfo.path;
        this.x = pictureInfo.x;
        this.y = pictureInfo.y;
        this.width = pictureInfo.width;
        this.height = pictureInfo.height;

        debug();
    }

    /**
     * 使用具体参数构造 PictureInfo
     *
     * @param tag    标签
     * @param path   图片路径
     * @param x      X 坐标
     * @param y      Y 坐标
     * @param width  宽度
     * @param height 高度
     */
    public PictureInfo (String tag, String path, int x, int y, int width, int height)
    {
        this.tag = tag;
        this.path = path;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        debug();
    }

    /**
     * 从 JSON 构造 PictureInfo，自动解析路径、坐标和尺寸
     * @param tag 标签
     * @param json 包含路径、位置和尺寸信息的 JSON 数据
     */
    public PictureInfo (String tag, JsonEntity json)
    {
        this.tag = tag;

        // 解析文件属性
        JsonPathParser pathInfo = new JsonPathParser(json);
        this.path = pathInfo.getPath();

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
     * 获取图片路径
     */
    public String getPath ()
    {
        return path;
    }

    /**
     * 设置图片路径
     */
    public void setPath (String path)
    {
        this.path = path;
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
        LogUtils.debug(PictureInfo.class, "PictureInfo 新的 (pictureInfo): " + this);
    }

    /**
     * 返回 PictureInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "PictureInfo{" +
            "tag='" + tag + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            '}';
    }

    /**
     * 判断两个 PictureInfo 是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PictureInfo that = (PictureInfo) o;
        return x == that.x &&
               y == that.y &&
               width == that.width &&
               height == that.height &&
               Objects.equals(tag, that.tag) &&
               Objects.equals(path, that.path);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(tag, path, x, y, width, height);
    }

}
