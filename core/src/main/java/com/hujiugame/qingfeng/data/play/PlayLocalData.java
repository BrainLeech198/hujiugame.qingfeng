package com.hujiugame.qingfeng.data.play;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.game.*;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.script.ScriptExecutor;
import com.hujiugame.qingfeng.ui.MessageBox;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.manager.LanguageManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class PlayLocalData
{
    // 游玩信息
    private FileHandle gamePathHandle;
    private Player player;

    // 游戏的用户配置信息管理器
    private GameUserConfigManager gameUserConfigManager;
    private ThemeManager themeManager;
    private LanguageManager languageManager;

    // 游戏运行引擎管理
    private AudioManager audioManager;
    private GraphicsManager graphicsManager;
    private UiManager uiManager;
    private ScriptExecutor scriptExecutor;

    // 游戏信息管理器
    private GameTemplateManager gameTemplateManager;
    private GameStoryManager gameStoryManager;
    private GameRoleManager gameRoleManager;
    private GameVariableManager gameVariableManager;
    private GameScriptManager gameScriptManager;

    /**
     * 创建 PlayLocalData 实例
     */
    public PlayLocalData ()
    {
        player = null;
    }

    // ===================================================================================================================
    // 游戏路径与玩家
    // ===================================================================================================================

    /**
     * 获取游戏路径目录
     */
    public FileHandle getGamePathHandle ()
    {
        return gamePathHandle;
    }

    /**
     * 设置游戏路径目录
     */
    public void setGamePathHandle (FileHandle gamePathHandle)
    {
        this.gamePathHandle = gamePathHandle;
    }

    /**
     * 获取玩家
     */
    public Player getPlayer ()
    {
        return player;
    }

    /**
     * 设置玩家
     */
    public void setPlayer (Player player)
    {
        this.player = player;
    }

    // ===================================================================================================================
    // 用户配置
    // ===================================================================================================================

    /**
     * 获取用户配置管理器
     */
    public GameUserConfigManager getGameUserConfigManager ()
    {
        return gameUserConfigManager;
    }

    /**
     * 设置用户配置管理器
     */
    public void setGameUserConfigManager (GameUserConfigManager gameUserConfigManager)
    {
        this.gameUserConfigManager = gameUserConfigManager;
    }

    /**
     * 获取主题管理器
     */
    public ThemeManager getThemeManager ()
    {
        return themeManager;
    }

    /**
     * 设置主题管理器
     */
    public void setThemeManager (ThemeManager themeManager)
    {
        this.themeManager = themeManager;
    }

    /**
     * 获取语言管理器
     */
    public LanguageManager getLanguageManager ()
    {
        return languageManager;
    }

    /**
     * 设置语言管理器
     */
    public void setLanguageManager (LanguageManager languageManager)
    {
        this.languageManager = languageManager;
    }

    // ===================================================================================================================
    // 游戏运行引擎
    // ===================================================================================================================

    /**
     * 获取音频管理器
     */
    public AudioManager getAudioManager ()
    {
        return audioManager;
    }

    /**
     * 设置音频管理器
     */
    public void setAudioManager (AudioManager audioManager)
    {
        this.audioManager = audioManager;
    }

    /**
     * 获取图形管理器
     */
    public GraphicsManager getGraphicsManager ()
    {
        return graphicsManager;
    }

    /**
     * 设置图形管理器
     */
    public void setGraphicsManager (GraphicsManager graphicsManager)
    {
        this.graphicsManager = graphicsManager;
    }

    /**
     * 获取 UI 管理器
     */
    public UiManager getUiManager ()
    {
        return uiManager;
    }

    /**
     * 设置 UI 管理器
     */
    public void setUiManager (UiManager uiManager)
    {
        this.uiManager = uiManager;
    }

    /**
     * 获取消息框（委托给 UiManager）
     */
    public MessageBox getMessageBox ()
    {
        return uiManager != null ? uiManager.getMessageBox() : null;
    }

    /**
     * 获取脚本执行器
     */
    public ScriptExecutor getScriptExecutor ()
    {
        return scriptExecutor;
    }

    /**
     * 设置脚本执行器
     */
    public void setScriptExecutor (ScriptExecutor scriptExecutor)
    {
        this.scriptExecutor = scriptExecutor;
    }

    // ===================================================================================================================
    // 游戏信息管理器
    // ===================================================================================================================

    /**
     * 获取模板管理器
     */
    public GameTemplateManager getGameTemplateManager ()
    {
        return gameTemplateManager;
    }

    /**
     * 设置模板管理器
     */
    public void setGameTemplateManager (GameTemplateManager gameTemplateManager)
    {
        this.gameTemplateManager = gameTemplateManager;
    }

    /**
     * 获取故事管理器
     */
    public GameStoryManager getGameStoryManager ()
    {
        return gameStoryManager;
    }

    /**
     * 设置故事管理器
     */
    public void setGameStoryManager (GameStoryManager gameStoryManager)
    {
        this.gameStoryManager = gameStoryManager;
    }

    /**
     * 获取角色管理器
     */
    public GameRoleManager getGameRoleManager ()
    {
        return gameRoleManager;
    }

    /**
     * 设置角色管理器
     */
    public void setGameRoleManager (GameRoleManager gameRoleManager)
    {
        this.gameRoleManager = gameRoleManager;
    }

    /**
     * 获取变量管理器
     */
    public GameVariableManager getGameVariableManager ()
    {
        return gameVariableManager;
    }

    /**
     * 设置变量管理器
     */
    public void setGameVariableManager (GameVariableManager gameVariableManager)
    {
        this.gameVariableManager = gameVariableManager;
    }

    /**
     * 获取脚本管理器
     */
    public GameScriptManager getGameScriptManager ()
    {
        return gameScriptManager;
    }

    /**
     * 设置脚本管理器
     */
    public void setGameScriptManager (GameScriptManager gameScriptManager)
    {
        this.gameScriptManager = gameScriptManager;
    }

    // ===================================================================================================================

    /**
     * 销毁资源
     */
    public boolean dispose ()
    {
        try
        {
            // 销毁游戏资源（音频、图形、UI）
            if (audioManager != null && !audioManager.dispose()) return false;
            if (graphicsManager != null && !graphicsManager.dispose()) return false;
            if (uiManager != null && !uiManager.dispose()) return false;

            LogUtils.info(PlayLocalData.class, "dispose 销毁资源成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(PlayLocalData.class, "dispose", e);
            return false;
        }
    }

}
