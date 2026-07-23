package com.hujiugame.qingfeng.ui.kind.image;

public final class ImageState
{
    private boolean clicked;

    /**
     * 创建图片状态，默认为未点击
     */
    public ImageState ()
    {
        clicked = false;
    }

    /**
     * 标记为已点击
     */
    public void setClicked ()
    {
        clicked = true;
    }

    /**
     * 消费点击事件（如果已点击则返回 true 并重置状态）
     */
    public boolean consumeClicked ()
    {
        if (clicked)
        {
            clicked = false;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 是否已被点击
     */
    public boolean isClicked ()
    {
        return clicked;
    }

    /**
     * 设置点击状态
     */
    public void setClicked (boolean clicked)
    {
        this.clicked = clicked;
    }

}
