package com.hujiugame.qingfeng.util.interact;

import com.hujiugame.qingfeng.util.interact.interfaces.DialogShower;

/**
 * 崩溃对话框显示管理器
 * 让各平台在启动时注入实现，崩溃时用原生弹窗通知玩家
 */
public final class CrashDialogShower
{

    private CrashDialogShower()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    private static DialogShower platformShower;

    /**
     * 注入平台实现（由各平台启动器调用）
     * @param shower 平台对话框显示实现
     */
    public static void setPlatformShower(DialogShower shower)
    {
        platformShower = shower;
    }

    /**
     * 显示错误弹窗（线程安全，但应由崩溃线程调用）
     * @param title   标题
     * @param message 消息内容
     */
    public static void showErrorDialog(String title, String message)
    {
        if (platformShower != null)
        {
            platformShower.show(title, message);
        }
        else
        {
            // 若未注入实现，仅打印到错误输出
            System.err.println("CRASH: " + title + " - " + message);
        }
    }
}
