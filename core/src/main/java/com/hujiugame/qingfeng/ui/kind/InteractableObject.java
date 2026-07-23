package com.hujiugame.qingfeng.ui.kind;

public interface InteractableObject
{
    /**
     * 获取对象标签
     */
    String getTag ();

    /**
     * 获取矩形上边界
     */
    float getRectTop ();

    /**
     * 获取矩形左边界
     */
    float getRectLeft ();

    /**
     * 获取矩形下边界
     */
    float getRectBottom ();

    /**
     * 获取矩形右边界
     */
    float getRectRight ();

    /**
     * 判断指定坐标是否在对象区域内
     */
    boolean contains (float x, float y);

    /**
     * 设置点击状态
     *
     * @return 设置成功返回 true
     */
    boolean setClicked (boolean clicked);

    /**
     * 是否被点击
     */
    boolean isClicked ();

    /**
     * 是否正在显示
     */
    boolean isShown ();

    /**
     * 显示对象
     *
     * @return 执行成功返回 true
     */
    boolean show ();

    /**
     * 隐藏对象
     * @return 执行成功返回 true
     */
    boolean hide ();

}
