package com.hujiugame.qingfeng.ui.dialog;

import com.hujiugame.qingfeng.ui.UiManager;

public final class BoxInfo implements BoxObject
{
    private UiManager uiManager;

    private String buttonOkTag = null;

    /**
     * 构造信息弹窗结果对象。
     *
     * @param uiManager   UI 管理器
     * @param buttonOkTag "确定"按钮标签
     */
    public BoxInfo (UiManager uiManager, String buttonOkTag)
    {
        this.uiManager = uiManager;
        this.buttonOkTag = buttonOkTag;
    }

    /**
     * 检查用户是否点击了"确定"按钮。
     *
     * @return 点击"确定"返回 true，否则返回 false
     */
    public boolean isOk ()
    {
        if (buttonOkTag != null)
        {
            return uiManager.isButtonClicked(buttonOkTag);
        }
        else
        {
            return false;
        }
    }
}
