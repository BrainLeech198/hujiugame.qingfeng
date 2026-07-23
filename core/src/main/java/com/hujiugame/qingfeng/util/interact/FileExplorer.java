package com.hujiugame.qingfeng.util.interact;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.util.interact.interfaces.ExplorerOpener;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class FileExplorer
{

    private FileExplorer()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    private static ExplorerOpener opener;

    /**
     * 注入平台资源管理器打开器实现（由各平台启动器调用）
     *
     * @param opener 平台资源管理器打开器实现
     */
    public static void setExplorerOpener(ExplorerOpener opener)
    {
        FileExplorer.opener = opener;
    }

    /**
     * 在系统资源管理器中显示指定路径。
     * - 文件夹：打开该文件夹
     * - 文件：打开父文件夹并尝试高亮文件
     * @param path 要显示的文件或文件夹路径
     */
    public static void showInExplorer(FileHandle path)
    {
        if (path == null) return;
        if (opener == null)
        {
            LogUtils.error(FileExplorer.class, "ExplorerOpener 未注入");
            return;
        }
        LogUtils.debug(FileExplorer.class, "showInExplorer 尝试启动资源管理器 (path): " + path);
        postFileExplorerSafely(() -> opener.open(path));
    }

    // ==================== 安全的 GL 线程调度（只日志，不崩溃） ====================

    private static void postFileExplorerSafely (Runnable runnable)
    {
        Gdx.app.postRunnable(() ->
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                LogUtils.error(FileExplorer.class, "postRunnable 回调异常", e);
            }
        });
    }

}
