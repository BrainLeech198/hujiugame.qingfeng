package com.hujiugame.qingfeng.ui.kind;

import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.LinkedList;
import java.util.List;

public class UiInfo
{
    private List<String> uiObjectTag = new LinkedList<>();
    private List<Class<?>> uiInfoClassList = new LinkedList<>();
    private List<Object> uiInfoList = new LinkedList<Object>();

    /**
     * 创建空的 UiInfo 容器
     */
    public UiInfo ()
    {
    }

    /**
     * 从对象列表构造 UiInfo，自动分类添加 ImageInfo、LabelInfo、ButtonInfo
     *
     * @param objectList 包含 UI 对象的列表
     */
    public UiInfo (List<Object> objectList)
    {
        for (Object object : objectList)
        {
            if (object instanceof ImageInfo)
            {
                add((ImageInfo) object);
            }
            else if (object instanceof LabelInfo)
            {
                add((LabelInfo) object);
            }
            else if (object instanceof ButtonInfo)
            {
                add((ButtonInfo) object);
            }
        }
    }

    /**
     * 复制构造器（浅拷贝）
     *
     * @param uiInfo 源 UiInfo 对象
     */
    public UiInfo (UiInfo uiInfo)
    {
        this.uiObjectTag = uiInfo.uiObjectTag;
        this.uiInfoClassList = uiInfo.uiInfoClassList;
        this.uiInfoList = uiInfo.uiInfoList;
    }

    /**
     * 容器是否为空
     */
    public boolean isEmpty ()
    {
        return uiObjectTag.isEmpty() || uiInfoClassList.isEmpty() || uiInfoList.isEmpty();
    }

    /**
     * 获取容器大小
     */
    public int size ()
    {
        return uiObjectTag.size();
    }

    /**
     * 获取所有标签的列表
     */
    public List<String> keySet ()
    {
        return uiObjectTag;
    }

    /**
     * 判断是否包含指定标签
     */
    public boolean containsKey (String tag)
    {
        try
        {
            return uiObjectTag.contains(tag);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "containsKey", e);
            return false;
        }
    }

    /**
     * 判断是否包含指定标签且类型匹配
     */
    public boolean containsKey (String tag, Class<?> clazz)
    {
        try
        {
            int index = uiObjectTag.indexOf(tag);
            if (index != -1)
            {
                return clazz.equals(uiInfoClassList.get(index));
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "containsKey", e);
            return false;
        }
    }

    /**
     * 返回 UiInfo 的字符串表示
     */
    @Override
    public String toString ()
    {
        return "UiInfo{" +
                "uiObjectTag=" + uiObjectTag +
                ", uiInfoClassList=" + uiInfoClassList +
                ", uiInfoList=" + uiInfoList +
                '}';
    }

    /**
     * 添加 UI 对象，自动识别类型
     */
    public boolean add (Object object)
    {
        try
        {
            if (object instanceof ImageInfo)
            {
                return add((ImageInfo) object);
            }
            else if (object instanceof LabelInfo)
            {
                return add((LabelInfo) object);
            }
            else if (object instanceof ButtonInfo)
            {
                return add((ButtonInfo) object);
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "add", e);
            return false;
        }
    }


    /**
     * 添加 ImageInfo 对象
     */
    public boolean add (ImageInfo imageInfo)
    {
        try
        {
            uiObjectTag.add(imageInfo.getImageTag());
            uiInfoClassList.add(ImageInfo.class);
            uiInfoList.add(imageInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "add", e);
            return false;
        }
    }

    /**
     * 添加 LabelInfo 对象
     */
    public boolean add (LabelInfo labelInfo)
    {
        try
        {
            uiObjectTag.add(labelInfo.getLabelTag());
            uiInfoClassList.add(LabelInfo.class);
            uiInfoList.add(labelInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "add", e);
            return false;
        }
    }

    /**
     * 添加 ButtonInfo 对象
     */
    public boolean add (ButtonInfo buttonInfo)
    {
        try
        {
            uiObjectTag.add(buttonInfo.getButtonTag());
            uiInfoClassList.add(ButtonInfo.class);
            uiInfoList.add(buttonInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "add", e);
            return false;
        }
    }

    /**
     * 批量添加 UI 对象
     */
    public boolean addAll (List<Object> objectList)
    {
        try
        {
            boolean result = true;

            if (objectList != null)
            {
                for (Object object : objectList)
                {
                    if (!add(object))
                    {
                        result = false;
                    }
                }
            }
            else
            {
                result = false;
            }

            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "addAll", e);
            return false;
        }
    }

    /**
     * 在指定位置插入 ImageInfo
     */
    public boolean insert (int index, ImageInfo imageInfo)
    {
        try
        {
            uiObjectTag.add(index, imageInfo.getImageTag());
            uiInfoClassList.add(index, ImageInfo.class);
            uiInfoList.add(index, imageInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "insert", e);
            return false;
        }
    }

    /**
     * 在指定位置插入 LabelInfo
     */
    public boolean insert (int index, LabelInfo labelInfo)
    {
        try
        {
            uiObjectTag.add(index, labelInfo.getLabelTag());
            uiInfoClassList.add(index, LabelInfo.class);
            uiInfoList.add(index, labelInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "insert", e);
            return false;
        }
    }

    /**
     * 在指定位置插入 ButtonInfo
     */
    public boolean insert (int index, ButtonInfo buttonInfo)
    {
        try
        {
            uiObjectTag.add(index, buttonInfo.getButtonTag());
            uiInfoClassList.add(index, ButtonInfo.class);
            uiInfoList.add(index, buttonInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "insert", e);
            return false;
        }
    }

    /**
     * 根据标签获取 UI 对象的 Class 类型
     */
    public Class<?> getClass (String tag)
    {
        try
        {
            int index = uiObjectTag.indexOf(tag);
            if (index != -1)
            {
                return uiInfoClassList.get(index);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getClass", e);
            return null;
        }
    }

    /**
     * 根据索引获取 UI 对象的 Class 类型
     */
    public Class<?> getClass (int  index)
    {
        try
        {
            return uiInfoClassList.get(index);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getClass", e);
            return null;
        }
    }

    /**
     * 根据标签获取 UI 对象
     */
    public Object get (String tag)
    {
        try
        {
            int index = uiObjectTag.indexOf(tag);
            if (index != -1)
            {
                return uiInfoList.get(index);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "get", e);
            return null;
        }
    }

    /**
     * 根据标签和类型获取 UI 对象
     */
    public Object get (String tag, Class<?> clazz)
    {
        try
        {
            int index = uiObjectTag.indexOf(tag);
            if (index != -1)
            {
                if (clazz.equals(uiInfoClassList.get(index)))
                {
                    return uiInfoList.get(index);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "get", e);
            return null;
        }
    }

    /**
     * 根据标签获取 ImageInfo
     */
    public ImageInfo getImageInfo (String tag)
    {
        try
        {
            return (ImageInfo) get(tag, ImageInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getImageInfo", e);
            return null;
        }
    }

    /**
     * 根据标签获取 LabelInfo
     */
    public LabelInfo getLabelInfo (String tag)
    {
        try
        {
            return (LabelInfo) get(tag, LabelInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getLabelInfo", e);
            return null;
        }
    }

    /**
     * 根据标签获取 ButtonInfo
     */
    public ButtonInfo getButtonInfo (String tag)
    {
        try
        {
            return (ButtonInfo) get(tag, ButtonInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getButtonInfo", e);
            return null;
        }
    }

    /**
     * 根据索引获取 UI 对象
     */
    public Object get (int index)
    {
        try
        {
            return uiInfoList.get(index);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "get", e);
            return null;
        }
    }

    /**
     * 根据索引和类型获取 UI 对象
     */
    public Object get (int index, Class<?> clazz)
    {
        try
        {
            if (clazz.equals(uiInfoClassList.get(index)))
            {
                return uiInfoList.get(index);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "get", e);
            return null;
        }
    }

    /**
     * 根据索引获取 ImageInfo
     */
    public ImageInfo getImageInfo (int index)
    {
        try
        {
            return (ImageInfo) get(index, ImageInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getImageInfo", e);
            return null;
        }
    }

    /**
     * 根据索引获取 LabelInfo
     */
    public LabelInfo getLabelInfo (int index)
    {
        try
        {
            return (LabelInfo) get(index, LabelInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getLabelInfo", e);
            return null;
        }
    }

    /**
     * 根据索引获取 ButtonInfo
     */
    public ButtonInfo getButtonInfo (int index)
    {
        try
        {
            return (ButtonInfo) get(index, ButtonInfo.class);
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "getButtonInfo", e);
            return null;
        }
    }

    /**
     * 根据标签移除 UI 对象
     */
    public boolean remove (String tag)
    {
        try
        {
            int index = uiObjectTag.indexOf(tag);
            if (index != -1)
            {
                uiObjectTag.remove(index);
                uiInfoClassList.remove(index);
                uiInfoList.remove(index);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "remove", e);
            return false;
        }
    }

    /**
     * 根据索引移除 UI 对象
     */
    public boolean remove (int index)
    {
        try
        {
            uiObjectTag.remove(index);
            uiInfoClassList.remove(index);
            uiInfoList.remove(index);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "remove", e);
            return false;
        }
    }

    /**
     * 清空所有 UI 对象
     */
    public boolean clear ()
    {
        try
        {
            uiObjectTag.clear();
            uiInfoClassList.clear();
            uiInfoList.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiInfo.class, "clear", e);
            return false;
        }
    }
}
