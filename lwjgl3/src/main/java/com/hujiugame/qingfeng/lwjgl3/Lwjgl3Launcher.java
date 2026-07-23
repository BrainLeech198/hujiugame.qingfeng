package com.hujiugame.qingfeng.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.Main;
import com.hujiugame.qingfeng.lwjgl3.imp.DesktopExplorerOpener;
import com.hujiugame.qingfeng.lwjgl3.imp.ZenityFileChooser;
import com.hujiugame.qingfeng.util.interact.CrashDialogShower;
import com.hujiugame.qingfeng.util.interact.FileChooser;
import com.hujiugame.qingfeng.util.interact.FileExplorer;
import com.hujiugame.qingfeng.util.interact.NativeDialogUtils;
import com.hujiugame.qingfeng.util.interact.interfaces.ConfirmCallback;
import com.hujiugame.qingfeng.util.interact.interfaces.NativeDialog;
import com.hujiugame.qingfeng.util.system.FilePathConfig;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

import java.io.File;

public class Lwjgl3Launcher
{
    public static void main (String[] args)
    {
        if (StartupHelper.startNewJvmIfRequired())
            return;
        createApplication(args);
    }

    private static Lwjgl3Application createApplication (String[] args)
    {
        // 先执行平台服务初始化（静态 setter），确保无论哪种降级路径都生效
        initializePlatformServices();

        // 三级自动降级：GL32 高性能 → GL20 兼容 → ANGLE 软渲染
        // 仅在 OpenGL/GLFW 兼容性异常时降级，游戏逻辑崩溃（NPE 等）直接抛出

        // 方案 1: 默认 GL32 高性能配置
        try
        {
            return new Lwjgl3Application(new Main(args), getDefaultConfiguration());
        }
        catch (Exception e)
        {
            if (!isGlCompatibilityError(e))
            {
                throw e;
            }
            System.err.println("[Lwjgl3Launcher] OpenGL GL32 模式启动失败: " + e.getMessage()
                + "，降级到 GL20 兼容模式...");
        }

        // 方案 2: GL20 兼容模式（适用于老旧显卡/驱动）
        try
        {
            Lwjgl3ApplicationConfiguration config = getDefaultConfiguration();
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL20, 0, 0);
            return new Lwjgl3Application(new Main(args), config);
        }
        catch (Exception e)
        {
            if (!isGlCompatibilityError(e))
            {
                throw e;
            }
            System.err.println("[Lwjgl3Launcher] OpenGL GL20 兼容模式启动失败: " + e.getMessage()
                + "，降级到 ANGLE 模拟...");
        }

        // 方案 3: ANGLE（DirectX 转译 OpenGL ES）+ 软渲染回退
        try
        {
            System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
            Lwjgl3ApplicationConfiguration config = getDefaultConfiguration();
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
            return new Lwjgl3Application(new Main(args), config);
        }
        catch (Exception e)
        {
            if (!isGlCompatibilityError(e))
            {
                throw e;
            }
            System.err.println("[Lwjgl3Launcher] 所有 OpenGL 模式均启动失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 判断异常是否为 OpenGL/GLFW 兼容性问题。
     * 游戏逻辑异常（NPE 等）不应触发降级，应直接崩溃退出以暴露真实错误。
     */
    private static boolean isGlCompatibilityError (Exception e)
    {
        String msg = e.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("glfw")
            || lower.contains("opengl")
            || lower.contains("pixel format")
            || lower.contains("glemuation")
            || lower.contains("could not initialize");
    }

    private static void initializePlatformServices ()
    {
        // crashDialog
        CrashDialogShower.setPlatformShower((title, message) ->
        {
            javax.swing.SwingUtilities.invokeLater(() ->
            {
                javax.swing.JOptionPane.showMessageDialog(null, message, title,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            });
        });

        // 多功能原生对话框
        NativeDialogUtils.setPlatformDialog(new NativeDialog()
        {
            @Override
            public void showInfo (String title, String message, Runnable onClose)
            {
                javax.swing.SwingUtilities.invokeLater(() ->
                {
                    javax.swing.JOptionPane.showMessageDialog(null, message, title,
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    if (onClose != null)
                    {
                        Gdx.app.postRunnable(onClose);
                    }
                });
            }

            @Override
            public void showConfirm (String title, String message, ConfirmCallback callback)
            {
                javax.swing.SwingUtilities.invokeLater(() ->
                {
                    int result = javax.swing.JOptionPane.showConfirmDialog(null, message, title,
                        javax.swing.JOptionPane.YES_NO_OPTION);
                    Gdx.app.postRunnable(() ->
                    {
                        if (result == javax.swing.JOptionPane.YES_OPTION)
                        {
                            callback.onConfirm();
                        }
                        else
                        {
                            callback.onCancel();
                        }
                    });
                });
            }

            @Override
            public void showError (String title, String message, Runnable onClose)
            {
                javax.swing.SwingUtilities.invokeLater(() ->
                {
                    javax.swing.JOptionPane.showMessageDialog(null, message, title,
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                    if (onClose != null)
                    {
                        Gdx.app.postRunnable(onClose);
                    }
                });
            }
        });

        // 注入桌面实现（Linux 用 zenity 避免 Swing/GTK Wayland 崩溃）
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
        {
            FileChooser.setFileChooser(new ZenityFileChooser());
        }
        else
        {
            FileChooser.setFileChooser(new DesktopFileChooser());
        }
        FileExplorer.setExplorerOpener(new DesktopExplorerOpener());

        // 文件选择器关闭后自动聚焦游戏窗口
        FileChooser.setWindowFocusRequester(() ->
        {
            if (Gdx.app instanceof Lwjgl3Application)
            {
                Lwjgl3Graphics lwjgl3Graphics = (Lwjgl3Graphics) Gdx.app.getGraphics();
                lwjgl3Graphics.getWindow().focusWindow();
            }
        });

        // fileHandle static injection
        setFileHandleInjection();
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration ()
    {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("氢风");
        configuration.setWindowIcon("icon128.png");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1024, 576);

        return configuration;
    }

    private static void setFileHandleInjection ()
    {
        File userHomeFile = new File(System.getProperty("user.home"));
        FilePathConfig pathConfig = new FilePathConfig(
            new FileHandle(userHomeFile),                              // userHome
            new FileHandle(new File(userHomeFile, "Downloads")),      // downloads
            new FileHandle(new File(userHomeFile, "Documents")),      // documents
            new FileHandle(new File(userHomeFile, "Pictures")),       // pictures
            new FileHandle(new File(userHomeFile, "Music")),          // music
            new FileHandle(new File(userHomeFile, "Videos")),         // movies
            new FileHandle(new File(userHomeFile, "Desktop"))         // desktopDir
        );
    }
}
