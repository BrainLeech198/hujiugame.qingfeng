package com.hujiugame.qingfeng.game;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameLogicService
{
    private PlayLocalData playLocalData;

    /**
     * 初始化游戏逻辑，设置游戏数据内容
     *
     * @param playLocalData 游戏数据内容对象
     * @return 初始化是否成功
     */
    public boolean init (PlayLocalData playLocalData)
    {
        try
        {
            this.playLocalData = playLocalData;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "init", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 检查游戏目录是否为可运行的游戏文件夹
     *
     * @param gamePathDirectory 游戏目录文件句柄
     * @return 目录有效则返回true，否则返回false
     */
    public boolean checkGameDirectory (FileHandle gamePathDirectory)
    {
        try
        {
            String gameDirectoryPath = gamePathDirectory.path();
            // TODO: 实现完整的目录校验逻辑
            boolean isOk = true;

            // 只要目录才可以检测
            if (!FileUtils.isDirectoryExist(gamePathDirectory))
            {
                LogUtils.debug(GameLogicService.class, "checkGameDirectory 不属于目录 (path): " + gameDirectoryPath);
                return false;
            }

            // 检测结果
            if (isOk)
            {
                LogUtils.debug(GameLogicService.class, "checkGameDirectory 目录属于可运行游戏文件夹 (path): " + gameDirectoryPath);
                return true;
            }
            else
            {
                LogUtils.error(GameLogicService.class, "checkGameDirectory 目录不属于可运行游戏文件夹 (path): " + gameDirectoryPath);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "checkGameDirectory", e);
            return false;
        }
    }

    /**
     * 解析游戏目录下的配置文件
     * @param gamePathDirectory 游戏目录文件句柄
     * @return 游戏配置JSON对象
     */
    public JsonEntity parseGameConfig (FileHandle gamePathDirectory)
    {
        try
        {
            FileHandle gameConfigFileHandle = gamePathDirectory.child(FileName.IN_GAME_CONFIG);
            JsonEntity gameConfigJson = new JsonEntity(gameConfigFileHandle);
            if (gameConfigJson.isEmpty()) LogUtils.error(GameLogicService.class, "parseConfig 配置文件不存在");
            return gameConfigJson;
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "parseGameConfig", e);
            return new JsonEntity();
        }
    }

    /**
     * 从游戏配置中解析游戏ID
     * @param gameConfigJson 游戏配置JSON对象
     * @return 游戏ID，若缺少字段则返回null
     */
    @javax.annotation.Nullable
    public String parseGameId (JsonEntity gameConfigJson)
    {
        try
        {
            if (gameConfigJson.containsKey("id"))
            {
                return gameConfigJson.getString("id");
            }
            else
            {
                LogUtils.error(GameLogicService.class, "parseGameId 配置文件缺少 id 字段");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "parseGameId", e);
            return null;
        }
    }

    /**
     * 从游戏配置中解析游戏名称
     * @param gameConfigJson 游戏配置JSON对象
     * @return 游戏名称，若缺少字段则返回null
     */
    @javax.annotation.Nullable
    public String parseGameName (JsonEntity gameConfigJson)
    {
        try
        {
            if (gameConfigJson.containsKey("name"))
            {
                return gameConfigJson.getString("name");
            }
            else
            {
                LogUtils.error(GameLogicService.class, "parseGameName 配置文件缺少 name 字段");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "parseGameName", e);
            return null;
        }
    }

    /**
     * 从游戏配置中解析游戏版本号
     * @param gameConfigJson 游戏配置JSON对象
     * @return 游戏版本号，若缺少字段则返回null
     */
    @javax.annotation.Nullable
    public String parseGameVersion (JsonEntity gameConfigJson)
    {
        try
        {
            if (gameConfigJson.containsKey("version"))
            {
                return gameConfigJson.getString("version");
            }
            else
            {
                LogUtils.error(GameLogicService.class, "parseGameVersion 配置文件缺少 version 字段");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "parseGameVersion", e);
            return null;
        }
    }

    /**
     * 从游戏配置中解析需要的启动器版本号
     * @param gameConfigJson 游戏配置JSON对象
     * @return 启动器版本号，若缺少字段则返回null
     */
    @javax.annotation.Nullable
    public String parseGameLauncherVersion (JsonEntity gameConfigJson)
    {
        try
        {
            if (gameConfigJson.containsKey("launcher_version"))
            {
                return gameConfigJson.getString("launcher_version");
            }
            else
            {
                LogUtils.error(GameLogicService.class, "parseGameLauncherVersion 配置文件缺少 launcher_version 字段");
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "parseGameLauncherVersion", e);
            return null;
        }
    }

    /**
     * 获取游戏图标文件句柄
     * @param gamePathDirectory 游戏目录文件句柄
     * @return 游戏图标文件句柄
     */
    public FileHandle parseGameIcon (FileHandle gamePathDirectory)
    {
        return gamePathDirectory.child(FileName.IN_GAME_ICON);
    }

    // ===================================================================================================================

    /**
     * 释放游戏逻辑资源
     * @return 释放是否成功
     */
    public boolean dispose ()
    {
        try
        {
            playLocalData = null;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameLogicService.class, "dispose", e);
            return false;
        }
    }
}
