package com.hujiugame.qingfeng.lwjgl3.imp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.hujiugame.qingfeng.util.system.LogUtils;
import games.spooky.gdx.nativefilechooser.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Linux 原生文件选择器，使用 zenity（GNOME 原生对话框）代替 Swing JFileChooser，
 * 避免 Ubuntu Wayland 下 Swing/GTK 崩溃问题。
 * <p>
 * zenity 是 GNOME 桌面环境的预装工具，如果系统没有安装，
 * 可在终端执行 {@code sudo apt install zenity} 安装。
 */
public class ZenityFileChooser implements NativeFileChooser
{
    @Override
    public void chooseFile(NativeFileChooserConfiguration config, NativeFileChooserCallback callback)
    {
        chooseFileImpl(config, callback, false);
    }

    @Override
    public void chooseFiles(NativeFileChooserConfiguration config, NativeFilesChooserCallback callback)
    {
        chooseFilesImpl(config, callback, false);
    }

    @Override
    public void chooseFolder(NativeFolderChooserConfiguration config, NativeFolderChooserCallback callback)
    {
        chooseFolderImpl(config, callback, false);
    }

    private void chooseFileImpl(NativeFileChooserConfiguration config, NativeFileChooserCallback callback, boolean multi)
    {
        AtomicBoolean done = new AtomicBoolean(false);
        new Thread(() ->
        {
            try
            {
                String dir = (config.directory != null) ? config.directory.file().getAbsolutePath() : System.getProperty("user.home");

                ProcessBuilder pb = new ProcessBuilder("zenity", "--file-selection",
                    "--title", config.title != null ? config.title : "选择文件",
                    "--filename", dir + "/",
                    "--file-filter", "*");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                String line = reader.readLine();
                int exitCode = process.waitFor();

                if (done.compareAndSet(false, true))
                {
                    if (exitCode == 0 && line != null && !line.isEmpty())
                    {
                        FileHandle selected = Gdx.files.absolute(line.trim());
                        Gdx.app.postRunnable(() -> callback.onFileChosen(selected));
                    }
                    else
                    {
                        Gdx.app.postRunnable(callback::onCancellation);
                    }
                }
            }
            catch (Exception e)
            {
                if (done.compareAndSet(false, true))
                {
                    Gdx.app.postRunnable(() -> callback.onError(e));
                }
            }
        }, "ZenityFileChooser").start();
    }

    private void chooseFilesImpl(NativeFileChooserConfiguration config, NativeFilesChooserCallback callback, boolean multi)
    {
        AtomicBoolean done = new AtomicBoolean(false);
        new Thread(() ->
        {
            try
            {
                String dir = (config.directory != null) ? config.directory.file().getAbsolutePath() : System.getProperty("user.home");

                ProcessBuilder pb = new ProcessBuilder("zenity", "--file-selection", "--multiple",
                    "--title", config.title != null ? config.title : "选择文件",
                    "--filename", dir + "/",
                    "--file-filter", "*",
                    "--separator", "\n");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                String line;
                Array<FileHandle> files = new Array<>();
                while ((line = reader.readLine()) != null)
                {
                    files.add(Gdx.files.absolute(line.trim()));
                }
                int exitCode = process.waitFor();

                if (done.compareAndSet(false, true))
                {
                    if (exitCode == 0 && files.size > 0)
                    {
                        Gdx.app.postRunnable(() -> callback.onFilesChosen(files));
                    }
                    else
                    {
                        Gdx.app.postRunnable(callback::onCancellation);
                    }
                }
            }
            catch (Exception e)
            {
                if (done.compareAndSet(false, true))
                {
                    Gdx.app.postRunnable(() -> callback.onError(e));
                }
            }
        }, "ZenityFileChooser").start();
    }

    private void chooseFolderImpl(NativeFolderChooserConfiguration config, NativeFolderChooserCallback callback, boolean multi)
    {
        AtomicBoolean done = new AtomicBoolean(false);
        new Thread(() ->
        {
            try
            {
                String dir = (config.directory != null) ? config.directory.file().getAbsolutePath() : System.getProperty("user.home");

                ProcessBuilder pb = new ProcessBuilder("zenity", "--file-selection", "--directory",
                    "--title", config.title != null ? config.title : "选择文件夹",
                    "--filename", dir + "/");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                String line = reader.readLine();
                int exitCode = process.waitFor();

                if (done.compareAndSet(false, true))
                {
                    if (exitCode == 0 && line != null && !line.isEmpty())
                    {
                        FileHandle selected = Gdx.files.absolute(line.trim());
                        Gdx.app.postRunnable(() -> callback.onFolderChosen(selected));
                    }
                    else
                    {
                        Gdx.app.postRunnable(callback::onCancellation);
                    }
                }
            }
            catch (Exception e)
            {
                if (done.compareAndSet(false, true))
                {
                    Gdx.app.postRunnable(() -> callback.onError(e));
                }
            }
        }, "ZenityFileChooser").start();
    }
}
