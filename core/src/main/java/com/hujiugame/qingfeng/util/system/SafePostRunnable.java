package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Gdx;

/**
 * 安全的 GL 线程调度工具类，包装 {@link Gdx.app.postRunnable}，
 * 自动捕获异常并触发崩溃处理，避免异步任务闪退无通知
 */
public final class SafePostRunnable
{

    private SafePostRunnable()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 安全地在 GL 线程上执行任务，异常时自动记录日志并触发崩溃处理
     *
     * @param runnable 要执行的任务
     */
    public static void post (Runnable runnable)
    {
        Gdx.app.postRunnable(() ->
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                LogUtils.error(SafePostRunnable.class, "postRunnable 异常", e);
                CrashUtils.crash(e);
            }
        });
    }

}
