package com.hujiugame.qingfeng.ui.dialog;

import com.hujiugame.qingfeng.ui.UiManager;

public final class BoxAsk implements BoxObject
{
    private UiManager uiManager;

    private String buttonYesTag = null;
    private String buttonNoTag = null;

    /**
     * 构造确认弹窗结果对象。
     *
     * @param uiManager    UI 管理器
     * @param buttonYesTag "是"按钮标签
     * @param buttonNoTag  "否"按钮标签
     */
    public BoxAsk (UiManager uiManager, String buttonYesTag, String buttonNoTag)
    {
        this.uiManager = uiManager;
        this.buttonYesTag = buttonYesTag;
        this.buttonNoTag = buttonNoTag;
    }

    /**
     * 检查用户是否点击了"是"按钮。
     *
     * @return 点击"是"返回 true，否则返回 false
     */
    public boolean isYes ()
    {
        if (buttonYesTag != null)
        {
            return uiManager.isButtonClicked(buttonYesTag);
        }
        else
        {
            return false;
        }
    }

    /**
     * 检查用户是否点击了"否"按钮。
     *
     * @return 点击"否"返回 true，否则返回 false
     */
    public boolean isNo ()
    {
        if (buttonNoTag != null)
        {
            return uiManager.isButtonClicked(buttonNoTag);
        }
        else
        {
            return false;
        }
    }
}
