package com.hujiugame.qingfeng.util.interact.interfaces;

/**
 * 确认对话框回调接口
 */
public interface ConfirmCallback
{

    /**
     * 用户点击"确定"
     */
    void onConfirm ();

    /**
     * 用户点击"取消"或关闭对话框
     */
    void onCancel ();
}
