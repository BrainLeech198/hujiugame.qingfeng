package com.hujiugame.qingfeng.game.loader;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.game.*;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GamePlayDataLoader
{
    private final LayoutManager layoutManager;
    private final PlayLocalData playLocalData;

    /**
     * 构造游戏数据加载器
     *
     * @param layoutManager   布局管理器
     * @param playLocalData 游戏数据内容
     */
    public GamePlayDataLoader (LayoutManager layoutManager, PlayLocalData playLocalData)
    {
        this.layoutManager = layoutManager;
        this.playLocalData = playLocalData;
    }

    /**
     * 加载游戏数据，包括页面模板、故事和角色数据
     *
     * @param gamePathHandle 游戏路径句柄
     * @return 加载是否成功
     */
    public boolean loadData (FileHandle gamePathHandle)
    {
        try
        {
            // 页面模板
            GameTemplateManager gameTemplateManager = new GameTemplateManager();
            if (!gameTemplateManager.init(gamePathHandle, playLocalData.getThemeManager(), layoutManager))
            {
                LogUtils.error(GamePlayDataLoader.class, "loadData 加载页面模板失败");
                return false;
            }
            else
            {
                playLocalData.setGameTemplateManager(gameTemplateManager);
                LogUtils.debug(GamePlayDataLoader.class, "loadData 加载页面模板成功");
            }

            // 故事
            GameStoryManager gameStoryManager = new GameStoryManager();
            if (!gameStoryManager.init(playLocalData.getThemeManager(), layoutManager))
            {
                LogUtils.error(GamePlayDataLoader.class, "loadData 加载故事失败");
                return false;
            }
            else
            {
                playLocalData.setGameStoryManager(gameStoryManager);
                LogUtils.debug(GamePlayDataLoader.class, "loadData 加载故事成功");
            }

            // 角色
            GameRoleManager gameRoleManager = new GameRoleManager();
            if (!gameRoleManager.init(gamePathHandle, playLocalData.getThemeManager(), layoutManager))
            {
                LogUtils.error(GamePlayDataLoader.class, "loadData 加载角色失败");
                return false;
            }
            else
            {
                playLocalData.setGameRoleManager(gameRoleManager);
                LogUtils.debug(GamePlayDataLoader.class, "loadData 加载角色成功");
            }

            // 变量
            GameVariableManager gameVariableManager = new GameVariableManager();
            if (!gameVariableManager.init())
            {
                LogUtils.error(GamePlayDataLoader.class, "loadData 加载变量失败");
                return false;
            }
            else
            {
                playLocalData.setGameVariableManager(gameVariableManager);
                LogUtils.debug(GamePlayDataLoader.class, "loadData 加载变量成功");
            }

            // 脚本
            GameScriptManager gameScriptManager = new GameScriptManager();
            if (!gameScriptManager.init(gamePathHandle))
            {
                LogUtils.error(GamePlayDataLoader.class, "loadData 加载脚本失败");
                return false;
            }
            else
            {
                playLocalData.setGameScriptManager(gameScriptManager);
                LogUtils.debug(GamePlayDataLoader.class, "loadData 加载脚本成功");
            }

            LogUtils.debug(GamePlayDataLoader.class, "loadData 加载游戏数据成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GamePlayDataLoader.class, "loadData", e);
            return false;
        }
    }

    /**
     * 销毁游戏数据，依次释放角色、故事和模板管理器
     * @return 销毁是否成功
     */
    public boolean disposeData ()
    {
        try
        {
            GameRoleManager gameRoleManager = playLocalData.getGameRoleManager();
            if (gameRoleManager != null)
            {
                if (!gameRoleManager.dispose())
                {
                    LogUtils.error(GamePlayDataLoader.class, "disposeData 销毁角色失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GamePlayDataLoader.class, "disposeData 销毁角色成功");
                }
            }

            GameStoryManager gameStoryManager = playLocalData.getGameStoryManager();
            if (gameStoryManager != null)
            {
                if (!gameStoryManager.dispose())
                {
                    LogUtils.error(GamePlayDataLoader.class, "disposeData 销毁故事失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GamePlayDataLoader.class, "disposeData 销毁故事成功");
                }
            }

            GameTemplateManager gameTemplateManager = playLocalData.getGameTemplateManager();
            if (gameTemplateManager != null)
            {
                if (!gameTemplateManager.dispose())
                {
                    LogUtils.error(GamePlayDataLoader.class, "disposeData 销毁页面模板失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GamePlayDataLoader.class, "disposeData 销毁页面模板成功");
                }
            }

            GameScriptManager gameScriptManager = playLocalData.getGameScriptManager();
            if (gameScriptManager != null)
            {
                if (!gameScriptManager.dispose())
                {
                    LogUtils.error(GamePlayDataLoader.class, "disposeData 销毁脚本失败");
                    return false;
                }
                else
                {
                    LogUtils.debug(GamePlayDataLoader.class, "disposeData 销毁脚本成功");
                }
            }

            LogUtils.debug(GamePlayDataLoader.class, "disposeData 销毁游戏数据成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GamePlayDataLoader.class, "disposeData", e);
            return false;
        }
    }
}
