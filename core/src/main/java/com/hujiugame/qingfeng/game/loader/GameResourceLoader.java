package com.hujiugame.qingfeng.game.loader;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.script.ScriptExecutor;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.TextManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.manager.UserConfigManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameResourceLoader
{
    private final UserConfigManager userConfigManager;
    private final SpriteBatch spriteBatch;
    private final Stage stage;
    private final TextManager textManager;
    private final AudioManager launcherAudioManager;
    private final GraphicsManager launcherGraphicsManager;
    private final UiManager launcherUiManager;
    private final LayoutManager layoutManager;
    private final PlayLocalData playLocalData;

    /**
     * 构造游戏资源加载器
     *
     * @param userConfigManager       用户配置管理器
     * @param spriteBatch             精灵批处理
     * @param stage                   舞台对象
     * @param textManager             文本管理器
     * @param launcherAudioManager    启动器音频管理器
     * @param launcherGraphicsManager 启动器图形管理器
     * @param launcherUiManager       启动器UI管理器
     * @param layoutManager           布局管理器
     * @param playLocalData         游戏数据内容
     */
    public GameResourceLoader (UserConfigManager userConfigManager,
                               SpriteBatch spriteBatch,
                               Stage stage,
                               TextManager textManager,
                               AudioManager launcherAudioManager,
                               GraphicsManager launcherGraphicsManager,
                               UiManager launcherUiManager,
                               LayoutManager layoutManager,
                               PlayLocalData playLocalData)
    {
        this.userConfigManager = userConfigManager;
        this.spriteBatch = spriteBatch;
        this.stage = stage;
        this.textManager = textManager;
        this.launcherAudioManager = launcherAudioManager;
        this.launcherGraphicsManager = launcherGraphicsManager;
        this.launcherUiManager = launcherUiManager;
        this.layoutManager = layoutManager;
        this.playLocalData = playLocalData;
    }

    /**
     * 加载游戏资源，包括音频、图形、UI和消息框
     *
     * @param gameThemeManager 游戏主题管理器
     * @return 加载是否成功
     */
    public boolean loadResource (ThemeManager gameThemeManager)
    {
        try
        {
            // 音频
            launcherAudioManager.stopAll();
            AudioManager gameAudioManager = new AudioManager(userConfigManager);
            if (!gameAudioManager.init())
            {
                LogUtils.error(GameResourceLoader.class, "loadResource 音频初始化失败");
                return false;
            }
            else
            {
                playLocalData.setAudioManager(gameAudioManager);
                LogUtils.debug(GameResourceLoader.class, "loadResource 音频初始化成功");
            }
            layoutManager.setAudioManager(gameAudioManager);

            // 绘图
            GraphicsManager gameGraphicsManager = new GraphicsManager(spriteBatch, gameThemeManager);
            if (!gameGraphicsManager.init())
            {
                LogUtils.error(GameResourceLoader.class, "loadResource 绘图初始化失败");
                return false;
            }
            else
            {
                playLocalData.setGraphicsManager(gameGraphicsManager);
                LogUtils.debug(GameResourceLoader.class, "loadResource 绘图初始化成功");
            }
            layoutManager.setGraphicsManager(gameGraphicsManager);

            // UI
            UiManager gameUiManager = new UiManager(stage, launcherAudioManager, launcherGraphicsManager, textManager);
            if (!gameUiManager.init(gameThemeManager))
            {
                LogUtils.error(GameResourceLoader.class, "loadResource ui初始化失败");
                return false;
            }
            else
            {
                playLocalData.setUiManager(gameUiManager);
                LogUtils.debug(GameResourceLoader.class, "loadResource ui初始化成功");
            }
            if (!launcherGraphicsManager.quoteUiManager(launcherUiManager))
            {
                LogUtils.error(GameResourceLoader.class, "loadResource 绘图引用字体失败");
                return false;
            }
            else
            {
                LogUtils.debug(GameResourceLoader.class, "loadResource 绘图引用字体成功");
            }

            // script
            ScriptExecutor scriptExecutor = new ScriptExecutor();
            if (!scriptExecutor.init())
            {
                LogUtils.error(GameResourceLoader.class, "loadResource script初始化失败");
                return false;
            }
            else
            {
                playLocalData.setScriptExecutor(scriptExecutor);
                LogUtils.debug(GameResourceLoader.class, "loadResource script初始化成功");
            }

            LogUtils.debug(GameResourceLoader.class, "loadResource 加载游戏资源成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameResourceLoader.class, "loadResource", e);
            return false;
        }
    }

    /**
     * 销毁游戏资源，依次释放消息框、UI、图形和音频
     * @return 销毁是否成功
     */
    public boolean disposeResource ()
    {
        try
        {
            // 销毁脚本执行器
            ScriptExecutor scriptExecutor = playLocalData.getScriptExecutor();
            if (scriptExecutor != null)
            {
                if (!scriptExecutor.dispose())
                {
                    LogUtils.error(GameResourceLoader.class, "disposeResource script销毁失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GameResourceLoader.class, "disposeResource script销毁成功");
                }
            }

            // 销毁UI（内部会同时销毁弹窗）
            UiManager gameUiManager = playLocalData.getUiManager();
            if (gameUiManager != null)
            {
                if (!gameUiManager.dispose())
                {
                    LogUtils.error(GameResourceLoader.class, "disposeResource ui销毁失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GameResourceLoader.class, "disposeResource ui销毁成功");
                }
            }

            // 销毁图形
            GraphicsManager gameGraphicsManager = playLocalData.getGraphicsManager();
            if (gameGraphicsManager != null)
            {
                if (!gameGraphicsManager.dispose())
                {
                    LogUtils.error(GameResourceLoader.class, "disposeResource graphics销毁失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GameResourceLoader.class, "disposeResource graphics销毁成功");
                }
            }
            layoutManager.setGraphicsManager(launcherGraphicsManager);

            // 销毁音频
            AudioManager gameAudioManager = playLocalData.getAudioManager();
            if (gameAudioManager != null)
            {
                if (!gameAudioManager.dispose())
                {
                    LogUtils.error(GameResourceLoader.class, "disposeResource audio销毁失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GameResourceLoader.class, "disposeResource audio销毁成功");
                }
            }
            layoutManager.setAudioManager(launcherAudioManager);

            LogUtils.debug(GameResourceLoader.class, "disposeResource 销毁游戏资源成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameResourceLoader.class, "disposeResource", e);
            return false;
        }
    }
}
