package com.hujiugame.qingfeng.util.interact.interfaces;

/**
 * 多功能原生对话框接口，支持信息提示、确认选择、错误通知三种类型。
 * 各平台在启动时注入实现（桌面用 Swing，Android 用 AlertDialog）。
 */
public interface NativeDialog
{

    /**
     * 显示信息提示对话框（带确定按钮）
     *
     * @param title   对话框标题
     * @param message 提示内容
     * @param onClose 对话框关闭后的回调（可为 null）
     */
    void showInfo (String title, String message, Runnable onClose);

    /**
     * 显示确认选择对话框（带确定/取消按钮）
     *
     * @param title    对话框标题
     * @param message  提示内容
     * @param callback 用户选择回调（onConfirm / onCancel）
     */
    void showConfirm (String title, String message, ConfirmCallback callback);

    /**
     * 显示错误通知对话框
     *
     * @param title   对话框标题
     * @param message 错误信息
     * @param onClose 对话框关闭后的回调（可为 null）
     */
    void showError (String title, String message, Runnable onClose);
}
