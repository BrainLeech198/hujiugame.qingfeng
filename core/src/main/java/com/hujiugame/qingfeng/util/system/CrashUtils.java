package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.util.interact.CrashDialogShower;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 崩溃处理工具类，生成独立崩溃日志并弹窗通知玩家，阻塞 5 秒后退出
 */
public final class CrashUtils
{

    private CrashUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 处理游戏主线程崩溃（接受 Throwable，自动包装非 Exception 类型），
     * 生成独立崩溃日志（crash-{时间戳}.txt）并弹窗告知用户
     *
     * @param e 崩溃异常（Throwable）
     */
    public static void crash (Throwable e)
    {
        if (e instanceof Exception)
        {
            crash((Exception) e);
        }
        else
        {
            crash(new RuntimeException("非受检异常: " + e.getMessage(), e));
        }
    }

    /**
     * 处理游戏主线程崩溃，生成独立崩溃日志（crash-{时间戳}.txt）并弹窗告知用户
     *
     * @param e 崩溃异常
     */
    public static void crash (Exception e)
    {
        // 时间戳
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = now.format(displayFormatter);

        // 堆栈跟踪转字符串
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        // 普通日志路径（供参考）
        String logPath = "";
        try
        {
            FileHandle logFileHandle = LogUtils.getNowLogFileHandle();
            if (logFileHandle != null) logPath = logFileHandle.file().getAbsolutePath();
        }
        catch (Exception ignored)
        {
            logPath = "（日志未初始化）";
        }

        // 崩溃日志文件：hujiugame/qingfeng/log/crash-20260604-143025.txt
        DateTimeFormatter fileTsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String crashFileName = FileName.CRASH_LOG + now.format(fileTsFormatter) + ".txt";
        String crashRelativePath = FileUtils.pathJoin(PathName.BASE, PathName.LOG, crashFileName);

        // 构建崩溃报告内容
        String crashContent = "=== 氢风 崩溃报告 ===\n"
            + "时间: " + timestamp + "\n"
            + "异常: " + e.getClass().getName() + "\n"
            + "消息: " + (e.getMessage() != null ? e.getMessage() : "null") + "\n\n"
            + "堆栈跟踪:\n" + stackTrace + "\n"
            + "日志文件: " + logPath + "\n";

        // 写入崩溃日志（直接使用 Gdx.files，不依赖 LogUtils/FileUtils 的写方法避免递归）
        String crashAbsolutePath = "";
        try
        {
            FileHandle crashFileHandle = Gdx.files.external(crashRelativePath);
            crashFileHandle.parent().mkdirs();
            crashFileHandle.writeString(crashContent, false);
            crashAbsolutePath = crashFileHandle.file().getAbsolutePath();
        }
        catch (Exception ignored)
        {
            crashAbsolutePath = crashRelativePath + "（写入失败，请查看日志文件）";
        }

        // 弹窗告知用户
        String crashMessage = "游戏主线程崩溃退出\n\n"
            + "崩溃详情已保存至：\n" + crashAbsolutePath + "\n\n"
            + "请将此文件发送给开发者以协助排查问题。\n\n"
            + "异常信息: " + (e.getMessage() != null ? e.getMessage() : "无详细信息");
        CrashDialogShower.showErrorDialog("游戏主线程崩溃", crashMessage);
        LogUtils.error(CrashUtils.class, "游戏主线程崩溃", e);

        // 阻塞10秒退出
        int delaySeconds = 10;
        try
        {
            Thread.sleep(delaySeconds * 1000);
        }
        catch (InterruptedException e1)
        {
            LogUtils.error(CrashUtils.class, "crash", e1);
        }

        Gdx.app.exit();
    }

}
