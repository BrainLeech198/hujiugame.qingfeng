package com.hujiugame.qingfeng.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.manager.LanguageManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.manager.UserConfigManager;
import com.hujiugame.qingfeng.util.system.CrashUtils;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameResolver
{
    private UserConfigManager userConfigManager;
    private ThemeManager themeManager;
    private LanguageManager languageManager;

    private static final int USER_CONFIG_RETRY_COUNT = 5;
    private static final int USER_CONFIG_REPAIR_COUNT = 5;

    /**
     * 初始化配置加载器，绑定用户配置、主题和语言管理器
     *
     * @param userConfigManager 用户配置管理器
     * @param themeManager      主题管理器
     * @param languageManager   语言管理器
     * @return 是否初始化成功
     */
    public boolean init (UserConfigManager userConfigManager,
                         ThemeManager themeManager,
                         LanguageManager languageManager)
    {
        try
        {
            this.userConfigManager = userConfigManager;
            this.themeManager = themeManager;
            this.languageManager = languageManager;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameResolver.class, "init", e);
            return false;
        }
    }

    /**
     * 加载用户配置、语言和主题信息
     *
     * @return 是否加载成功
     */
    public boolean load ()
    {
        try
        {
            // 用户设置读取
            FileHandle userConfigPathHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET, FileName.USER_CONFIG));
            int configInitialRetryCount = 0;
            while (!userConfigManager.init(userConfigPathHandle))
            {
                // 记录重试次数
                configInitialRetryCount++;
                if (configInitialRetryCount >= USER_CONFIG_RETRY_COUNT)
                {
                    LogUtils.error(GameResolver.class, "load 读取用户设置重试次数已达上限，准备崩溃");
                    CrashUtils.crash(new RuntimeException("load 用户配置初始化失败，已重试" + configInitialRetryCount + "次"));
                    return false;
                }
                LogUtils.error(GameResolver.class, "load 读取用户设置失败 第" + configInitialRetryCount + "次重试");

                // 尝试修复
                int configRepairCount = 0;
                while (!userConfigManager.repair())
                {
                    // 记录重试次数
                    configRepairCount++;
                    if (configRepairCount >= USER_CONFIG_REPAIR_COUNT)
                    {
                        LogUtils.error(GameResolver.class, "load 修复用户设置重试次数已达上限，准备崩溃");
                        CrashUtils.crash(new RuntimeException("load 用户配置修复失败，已重试" + configRepairCount + "次"));
                        return false;
                    }
                    LogUtils.error(GameResolver.class, "load 尝试修复用户设置失败 第" + configRepairCount + "次重试");
                }
                LogUtils.debug(GameResolver.class, "load 尝试修复用户设置成功");
            }
            LogUtils.debug(GameResolver.class, "load 读取用户设置成功");

            // 根据用户设置 读取语言设置
            String languagePathName = userConfigManager.getLanguage();
            FileHandle languageConfigPathHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET_S_LANGUAGE));
            if (!languageManager.init(languagePathName, languageConfigPathHandle, true, userConfigManager))
            {
                LogUtils.error(GameResolver.class, "load 读取语言文件失败");
                return false;
            }
            else
            {
                LogUtils.debug(GameResolver.class, "load 读取语言文件成功");
            }

            // 根据用户数据 读取主题设置
            String themePathName = userConfigManager.getTheme();
            FileHandle themeConfigPathHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET_S_THEME));
            if (!themeManager.init(themePathName, themeConfigPathHandle, true, userConfigManager))
            {
                LogUtils.error(GameResolver.class, "load 读取主题信息失败");
                return false;
            }
            else
            {
                LogUtils.debug(GameResolver.class, "load 读取主题信息成功");
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameResolver.class, "load", e);
            return false;
        }
    }

    /**
     * 销毁配置加载器并清空引用
     *
     * @return 是否销毁成功
     */
    public boolean dispose ()
    {
        try
        {
            userConfigManager = null;
            themeManager = null;
            languageManager = null;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameResolver.class, "dispose", e);
            return false;
        }
    }
}
