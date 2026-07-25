package com.hujiugame.qingfeng.ui.kind.image;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.json.parser.JsonPositionParser;
import com.hujiugame.qingfeng.util.json.parser.JsonShowParser;
import com.hujiugame.qingfeng.util.json.parser.JsonSizeParser;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class ImageInfo
{
    private String imageTag;
    private String imageKindName;
    private boolean show;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * 复制构造器
     *
     * @param imageInfo 源 ImageInfo 对象
     */
    public ImageInfo (ImageInfo imageInfo)
    {
        this.imageTag = imageInfo.imageTag;
        this.imageKindName = imageInfo.imageKindName;
        this.show = imageInfo.show;
        this.x = imageInfo.x;
        this.y = imageInfo.y;
        this.width = imageInfo.width;
        this.height = imageInfo.height;

        debug();
    }

    /**
     * 使用具体参数构造 ImageInfo
     *
     * @param imageTag      图片标签
     * @param imageKindName 图片种类名称
     * @param show          是否显示
     * @param x             X 坐标
     * @param y             Y 坐标
     * @param width         宽度
     * @param height        高度
     */
    public ImageInfo (String imageTag, String imageKindName, boolean show, int x, int y, int width, int height)
    {
        this.imageTag = imageTag;
        this.imageKindName = imageKindName;
        this.show = show;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        debug();
    }

    /**
     * 从 JSON 构造 ImageInfo，自动解析种类、显示状态、坐标和尺寸
     * @param imageTag 图片标签
     * @param json 包含种类、位置和尺寸信息的 JSON 数据
     */
    public ImageInfo (String imageTag, JsonEntity json)
    {
        LogUtils.debug(ImageInfo.class, "ImageInfo 创建 (imageTag, json): " + imageTag + ", " + json);

        // 记录tag
        this.imageTag = imageTag;

        // 解析kind属性
        this.imageKindName = json.getString(UiKey.Image.KIND);

        // 解析show属性
        this.show = JsonShowParser.parseShow(json);

        // 解析坐标属性
        JsonPositionParser jsonPositionParser = new JsonPositionParser(json);
        this.x = jsonPositionParser.getX();
        this.y = jsonPositionParser.getY();

        // 解析高宽属性
        JsonSizeParser jsonSizeParser = new JsonSizeParser(json);
        this.width = jsonSizeParser.getWidth();
        this.height = jsonSizeParser.getHeight();

        debug();
    }

    /**
     * 获取图片标签
     */
    public String getImageTag ()
    {
        return imageTag;
    }

    /**
     * 设置图片标签
     */
    public ImageInfo setImageTag (String imageTag)
    {
        this.imageTag = imageTag;
        return this;
    }

    /**
     * 获取图片种类名称
     */
    public String getImageKindName ()
    {
        return imageKindName;
    }

    /**
     * 设置图片种类名称
     */
    public ImageInfo setImageKindName (String imageKindName)
    {
        this.imageKindName = imageKindName;
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
    public ImageInfo setShow (boolean show)
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
    public ImageInfo setX (int x)
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
    public ImageInfo setY (int y)
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
    public ImageInfo setWidth (int width)
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
    public ImageInfo setHeight (int height)
    {
        this.height = height;
        return this;
    }

    /**
     * 同时设置 X 和 Y 坐标
     */
    public ImageInfo setPosition (int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * 同时设置宽度和高度
     */
    public ImageInfo setSize (int width, int height)
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
        LogUtils.debug(ImageInfo.class, "ImageInfo 新的 (imageInfo): " + this);
    }

    /**
     * 返回 ImageInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "ImageInfo{" +
            "imageTag='" + imageTag + '\'' +
            ", imageKindName='" + imageKindName + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            '}';
    }

    /**
     * 判断两个 ImageInfo 是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageInfo that = (ImageInfo) o;
        return show == that.show &&
               x == that.x &&
               y == that.y &&
               width == that.width &&
               height == that.height &&
               Objects.equals(imageTag, that.imageTag) &&
            Objects.equals(imageKindName, that.imageKindName);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(imageTag, imageKindName, show, x, y, width, height);
    }

}
