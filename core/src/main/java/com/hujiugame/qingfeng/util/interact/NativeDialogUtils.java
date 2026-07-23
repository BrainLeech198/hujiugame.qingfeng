package com.hujiugame.qingfeng.util.interact;

import com.hujiugame.qingfeng.util.interact.interfaces.ConfirmCallback;
import com.hujiugame.qingfeng.util.interact.interfaces.NativeDialog;
import com.hujiugame.qingfeng.util.system.LogUtils;

/**
 * 多功能原生对话框工具类
 * <p>
 * 各平台在启动时通过 {@link #setPlatformDialog(NativeDialog)} 注入实现：
 * <ul>
 *     <li>桌面（LWJGL3）：Swing {@code JOptionPane}</li>
 *     <li>Android：原生 {@code AlertDialog}</li>
 * </ul>
 * 未注入时所有方法静默 fallback 到日志输出，不崩溃。
 */
public final class NativeDialogUtils
{

    private NativeDialogUtils ()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static NativeDialog platformDialog;

    /**
     * 注入平台原生对话框实现（由各平台启动器调用）
     *
     * @param dialog 平台对话框实现
     */
    public static void setPlatformDialog (NativeDialog dialog)
    {
        platformDialog = dialog;
    }

    // ===================================================================================================================
    // Info — 信息提示
    // ===================================================================================================================

    /**
     * 显示信息提示对话框（无回调）
     */
    public static void showInfo (String title, String message)
    {
        showInfo(title, message, null);
    }

    /**
     * 显示信息提示对话框
     *
     * @param title   对话框标题
     * @param message 提示内容
     * @param onClose 对话框关闭后的回调（可为 null），会在 GL 线程执行
     */
    public static void showInfo (String title, String message, Runnable onClose)
    {
        if (platformDialog != null)
        {
            platformDialog.showInfo(title, message, onClose);
        }
        else
        {
            LogUtils.info(NativeDialogUtils.class, "showInfo [未注入]: " + title + " - " + message);
            if (onClose != null)
            {
                onClose.run();
            }
        }
    }

    // ===================================================================================================================
    // Confirm — 确认选择
    // ===================================================================================================================

    /**
     * 显示确认选择对话框
     *
     * @param title    对话框标题
     * @param message  确认内容
     * @param callback 用户选择回调
     */
    public static void showConfirm (String title, String message, ConfirmCallback callback)
    {
        if (platformDialog != null)
        {
            platformDialog.showConfirm(title, message, callback);
        }
        else
        {
            LogUtils.info(NativeDialogUtils.class, "showConfirm [未注入]: " + title + " - " + message);
            if (callback != null)
            {
                callback.onConfirm();
            }
        }
    }

    // ===================================================================================================================
    // Error — 错误通知
    // ===================================================================================================================

    /**
     * 显示错误通知对话框（无回调）
     */
    public static void showError (String title, String message)
    {
        showError(title, message, null);
    }

    /**
     * 显示错误通知对话框
     *
     * @param title   对话框标题
     * @param message 错误信息
     * @param onClose 对话框关闭后的回调（可为 null），会在 GL 线程执行
     */
    public static void showError (String title, String message, Runnable onClose)
    {
        if (platformDialog != null)
        {
            platformDialog.showError(title, message, onClose);
        }
        else
        {
            LogUtils.error(NativeDialogUtils.class, "showError [未注入]: " + title + " - " + message);
            if (onClose != null)
            {
                onClose.run();
            }
        }
    }
}
