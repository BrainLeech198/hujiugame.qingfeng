package com.hujiugame.qingfeng.data.game;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.graphic.model.GifInfo;
import com.hujiugame.qingfeng.graphic.model.PictureInfo;
import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Layout
{
    // 页面信息
    private JsonEntity json = null;

    // 页面信息
    private String name = null;
    private String template = null;

    // 背景图片
    private String backgroundPicture = null;

    // 背景音乐列表（随机选一首播放）
    private final List<String> backgroundMusicList = new ArrayList<>();

    // 音乐列表
    private final List<String> musicList = new ArrayList<>();

    // 图片map
    private final Map<String, PictureInfo> pictureMap = new HashMap<>();

    // 动图map
    private final Map<String, GifInfo> gifMap = new HashMap<>();

    // 图像map
    private final Map<String, ImageInfo> imageMap = new HashMap<>();

    // 标签map
    private final Map<String, LabelInfo> labelMap = new HashMap<>();

    // 按钮map
    private Map<String, ButtonInfo> buttonMap = new HashMap<>();

    /**
     * 创建空的布局配置
     */
    public Layout ()
    {
    }

    /**
     * 复制构造器（深拷贝）
     *
     * @param source 源布局配置
     */
    public Layout (Layout source)
    {
        if (source != null)
        {
            this.json = source.json != null ? new JsonEntity(source.json) : null;
            this.name = source.name;
            this.template = source.template;
            this.backgroundPicture = source.backgroundPicture;
            this.backgroundMusicList.clear();
            if (source.backgroundMusicList != null)
            {
                this.backgroundMusicList.addAll(source.backgroundMusicList);
            }

            this.musicList.clear();
            if (source.musicList != null)
            {
                this.musicList.addAll(source.musicList);
            }

            this.pictureMap.clear();
            if (source.pictureMap != null)
            {
                for (Map.Entry<String, PictureInfo> entry : source.pictureMap.entrySet())
                {
                    this.pictureMap.put(entry.getKey(), new PictureInfo(entry.getValue()));
                }
            }

            this.gifMap.clear();
            if (source.gifMap != null)
            {
                for (Map.Entry<String, GifInfo> entry : source.gifMap.entrySet())
                {
                    this.gifMap.put(entry.getKey(), new GifInfo(entry.getValue()));
                }
            }

            this.imageMap.clear();
            if (source.imageMap != null)
            {
                for (Map.Entry<String, ImageInfo> entry : source.imageMap.entrySet())
                {
                    this.imageMap.put(entry.getKey(), new ImageInfo(entry.getValue()));
                }
            }

            this.labelMap.clear();
            if (source.labelMap != null)
            {
                for (Map.Entry<String, LabelInfo> entry : source.labelMap.entrySet())
                {
                    this.labelMap.put(entry.getKey(), new LabelInfo(entry.getValue()));
                }
            }

            this.buttonMap.clear();
            if (source.buttonMap != null)
            {
                for (Map.Entry<String, ButtonInfo> entry : source.buttonMap.entrySet())
                {
                    this.buttonMap.put(entry.getKey(), new ButtonInfo(entry.getValue()));
                }
            }
        }
    }

    /**
     * 获取 JSON 数据
     */
    public JsonEntity getJson ()
    {
        return json;
    }

    /**
     * 设置 JSON 数据
     */
    public void setJson (JsonEntity json)
    {
        this.json = json;
    }

    /**
     * 获取页面名称
     */
    public String getName ()
    {
        return name;
    }

    /**
     * 设置页面名称
     */
    public void setName (String name)
    {
        this.name = name;
    }

    /**
     * 获取模板名称
     */
    public String getTemplate ()
    {
        return template;
    }

    /**
     * 设置模板名称
     */
    public void setTemplate (String template)
    {
        this.template = template;
    }

    /**
     * 获取背景图片路径
     */
    public String getBackgroundPicture ()
    {
        return backgroundPicture;
    }

    /**
     * 设置背景图片路径
     */
    public void setBackgroundPicture (String backgroundPicture)
    {
        this.backgroundPicture = backgroundPicture;
    }

    /**
     * 获取背景音乐列表
     */
    public List<String> getBackgroundMusicList ()
    {
        return backgroundMusicList;
    }

    /**
     * 设置背景音乐列表
     */
    public void setBackgroundMusicList (List<String> backgroundMusicList)
    {
        this.backgroundMusicList.clear();
        this.backgroundMusicList.addAll(backgroundMusicList);
    }

    /**
     * 获取音乐列表
     */
    public List<String> getMusicList ()
    {
        return musicList;
    }

    /**
     * 设置音乐列表
     */
    public void setMusicList (List<String> musicList)
    {
        this.musicList.clear();
        this.musicList.addAll(musicList);
    }

    /**
     * 获取图片映射表
     */
    public Map<String, PictureInfo> getPictureMap ()
    {
        return pictureMap;
    }

    /**
     * 设置图片映射表
     */
    public void setPictureMap (Map<String, PictureInfo> pictureMap)
    {
        this.pictureMap.clear();
        this.pictureMap.putAll(pictureMap);
    }

    /**
     * 获取动图映射表
     */
    public Map<String, GifInfo> getGifMap ()
    {
        return gifMap;
    }

    /**
     * 设置动图映射表
     */
    public void setGifMap (Map<String, GifInfo> gifMap)
    {
        this.gifMap.clear();
        this.gifMap.putAll(gifMap);
    }

    /**
     * 获取图像映射表
     */
    public Map<String, ImageInfo> getImageMap ()
    {
        return imageMap;
    }

    /**
     * 设置图像映射表
     */
    public void setImageMap (Map<String, ImageInfo> imageMap)
    {
        this.imageMap.clear();
        this.imageMap.putAll(imageMap);
    }

    /**
     * 获取标签映射表
     */
    public Map<String, LabelInfo> getLabelMap ()
    {
        return labelMap;
    }

    /**
     * 设置标签映射表
     */
    public void setLabelMap (Map<String, LabelInfo> labelMap)
    {
        this.labelMap.clear();
        this.labelMap.putAll(labelMap);
    }

    /**
     * 获取按钮映射表
     */
    public Map<String, ButtonInfo> getButtonMap ()
    {
        return buttonMap;
    }

    /**
     * 设置按钮映射表
     */
    public void setButtonMap (Map<String, ButtonInfo> buttonMap)
    {
        this.buttonMap.clear();
        this.buttonMap.putAll(buttonMap);
    }

    /**
     * 返回布局配置的字符串表示
     */
    @Override
    public String toString ()
    {
        return "Layout{" +
                "json=" + json +
                ", backgroundPicture='" + backgroundPicture + '\'' +
                ", backgroundMusicList=" + backgroundMusicList +
                ", musicList=" + musicList +
                ", pictureMap=" + pictureMap +
                ", gifMap=" + gifMap +
                ", imageMap=" + imageMap +
                ", labelMap=" + labelMap +
                ", buttonMap=" + buttonMap +
                '}';
    }
}
