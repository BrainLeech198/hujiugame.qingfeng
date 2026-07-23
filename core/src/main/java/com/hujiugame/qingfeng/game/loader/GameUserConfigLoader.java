package com.hujiugame.qingfeng.game.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.manager.LanguageManager;
import com.hujiugame.qingfeng.manager.TextManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.game.GameInfoManager;
import com.hujiugame.qingfeng.game.GameUserConfigManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameUserConfigLoader
{
    private final TextManager textManager;
    private final LanguageManager defaultLanguageManager;
    private final GameInfoManager gameInfoManager;
    private final PlayLocalData playLocalData;

    /**
     * 构造游戏用户配置加载器
     *
     * @param textManager            文本管理器
     * @param defaultLanguageManager 默认语言管理器
     * @param gameInfoManager        游戏信息管理器
     * @param playLocalData        游戏数据内容
     */
    public GameUserConfigLoader (TextManager textManager, LanguageManager defaultLanguageManager, GameInfoManager gameInfoManager, PlayLocalData playLocalData)
    {
        this.textManager = textManager;
        this.defaultLanguageManager = defaultLanguageManager;
        this.gameInfoManager = gameInfoManager;
        this.playLocalData = playLocalData;
    }

    /**
     * 加载用户游戏配置，包括偏好设置、语言和主题
     *
     * @param gamePathHandle     游戏路径句柄
     * @param gameId             游戏ID
     * @return 加载是否成功
     */
    public boolean loadUserConfig (FileHandle gamePathHandle, String gameId)
    {
        try
        {
            // 加载用户游戏偏好设置
            GameUserConfigManager gameUserConfigManager = new GameUserConfigManager();
            if (!gameUserConfigManager.init(gamePathHandle, gameId))
            {
                LogUtils.error(GameUserConfigLoader.class, "loadUserConfig 加载用户游戏偏好json失败 (path): " + gamePathHandle);
                return false;
            }
            else
            {
                playLocalData.setGameUserConfigManager(gameUserConfigManager);
                LogUtils.debug(GameUserConfigLoader.class, "loadUserConfig 加载用户游戏偏好json成功 (path): " + gamePathHandle);
            }

            // 加载使用的语言
            String languagePathName = gameUserConfigManager.getLanguage();
            FileHandle languageConfigPathHandle = gamePathHandle.child(PathName.IN_GAME_ASSET_S_LANGUAGE);
            LanguageManager gameLanguageManager = new LanguageManager();
            if (!gameLanguageManager.init(languagePathName, languageConfigPathHandle, false, null))
            {
                LogUtils.error(GameUserConfigLoader.class, "loadUserConfig 加载语言失败 (language): " + languagePathName);
                return false;
            }
            else
            {
                playLocalData.setLanguageManager(gameLanguageManager);
                gameInfoManager.putInfo(GameInfoKey.USER_LANGUAGE, languagePathName);
                LogUtils.debug(GameUserConfigLoader.class, "loadUserConfig 加载语言成功 (language): " + languagePathName);
            }

            textManager.setLanguageManager(playLocalData.getLanguageManager());

            // 加载使用的主题
            String themePathName = gameUserConfigManager.getTheme();
            FileHandle themeConfigPathHandle = gamePathHandle.child(PathName.IN_GAME_ASSET_S_THEME);
            ThemeManager gameThemeManager = new ThemeManager();
            if (!gameThemeManager.init(themePathName, themeConfigPathHandle, false, null))
            {
                LogUtils.error(GameUserConfigLoader.class, "loadUserConfig 加载主题失败 (theme): " + themePathName);
                // 主题加载失败时回滚已切换的语言，避免 textManager 停留在游戏语言
                textManager.setLanguageManager(defaultLanguageManager);
                return false;
            }
            else
            {
                playLocalData.setThemeManager(gameThemeManager);
                gameInfoManager.putInfo(GameInfoKey.USER_THEME, themePathName);
                LogUtils.debug(GameUserConfigLoader.class, "loadUserConfig 加载主题成功 (theme): " + themePathName);
            }

            LogUtils.debug(GameUserConfigLoader.class, "loadUserConfig 加载用户游戏偏好设置成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigLoader.class, "loadUserConfig", e);
            // 异常时回滚已切换的语言
            textManager.setLanguageManager(defaultLanguageManager);
            return false;
        }
    }

    /**
     * 销毁用户游戏配置，重置文本语言管理和主题管理器
     * @return 销毁是否成功
     */
    public boolean disposeUserConfig ()
    {
        try
        {
            playLocalData.setThemeManager(null);
            textManager.setLanguageManager(defaultLanguageManager);
            playLocalData.setLanguageManager(null);
            playLocalData.setGameUserConfigManager(null);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigLoader.class, "disposeUserConfig", e);
            return false;
        }
    }
}
