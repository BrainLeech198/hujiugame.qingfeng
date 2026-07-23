package com.hujiugame.qingfeng.lwjgl3.imp;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.util.interact.interfaces.ExplorerOpener;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.awt.Desktop;
import java.io.File;

public class DesktopExplorerOpener implements ExplorerOpener
{
    @Override
    public void open(FileHandle path)
    {
        if (path == null)
        {
            LogUtils.error(DesktopExplorerOpener.class, "open Parameter is empty (path): null");
            return;
        }

        String absolutePath = path.file().getAbsolutePath();
        File file = new File(absolutePath);

        LogUtils.debug(DesktopExplorerOpener.class, "open Request path (path): " + absolutePath);
        LogUtils.debug(DesktopExplorerOpener.class, "open Is directory (isDirectory): " + path.isDirectory());

        // 优先使用 java.awt.Desktop（需要 java.desktop 模块）
        if (Desktop.isDesktopSupported())
        {
            Desktop desktop = Desktop.getDesktop();
            try
            {
                if (file.isDirectory())
                {
                    desktop.open(file);
                    LogUtils.debug(DesktopExplorerOpener.class, "open Desktop.open success (dir): " + absolutePath);
                    return;
                }
                else
                {
                    // 文件则打开其父目录
                    File parent = file.getParentFile();
                    if (parent != null)
                    {
                        desktop.open(parent);
                        LogUtils.debug(DesktopExplorerOpener.class, "open Desktop.open success (parent): " + parent.getAbsolutePath());
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                LogUtils.debug(DesktopExplorerOpener.class, "open Desktop API failed, fallback to CLI: " + e.getMessage());
            }
        }

        // 回退：使用平台命令
        try
        {
            String os = System.getProperty("os.name").toLowerCase();
            LogUtils.debug(DesktopExplorerOpener.class, "open Operating system (os): " + os);

            ProcessBuilder pb;
            if (os.contains("win"))
            {
                if (file.isDirectory())
                {
                    pb = new ProcessBuilder("explorer", absolutePath);
                }
                else
                {
                    pb = new ProcessBuilder("explorer", "/select,", absolutePath);
                }
            }
            else if (os.contains("mac"))
            {
                if (file.isDirectory())
                {
                    pb = new ProcessBuilder("open", absolutePath);
                }
                else
                {
                    pb = new ProcessBuilder("open", "-R", absolutePath);
                }
            }
            else // Linux and others
            {
                if (file.isDirectory())
                {
                    pb = new ProcessBuilder("xdg-open", absolutePath);
                }
                else
                {
                    File parent = file.getParentFile();
                    if (parent != null)
                    {
                        pb = new ProcessBuilder("xdg-open", parent.getAbsolutePath());
                    }
                    else
                    {
                        LogUtils.error(DesktopExplorerOpener.class, "open Cannot get parent directory (parent): null");
                        return;
                    }
                }
            }

            // 合并 stderr 到 stdout，并丢弃输出，防止缓冲区满导致进程挂起
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // 消费输出流，防止缓冲区死锁（Java 8 兼容写法）
            java.io.InputStream is = process.getInputStream();
            while (is.read(new byte[4096]) != -1) { /* discard */ }
            is.close();
            process.waitFor();

            LogUtils.debug(DesktopExplorerOpener.class, "open Process completed (exitCode): " + process.exitValue());
        }
        catch (Exception e)
        {
            LogUtils.error(DesktopExplorerOpener.class, "open Execution exception", e);
        }
    }
}
