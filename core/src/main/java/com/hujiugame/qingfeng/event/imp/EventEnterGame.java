package com.hujiugame.qingfeng.event.imp;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 进入游戏事件
 */
public class EventEnterGame implements EventObject
{
    private final String eventName;
    private final FileHandle gamePathHandle;

    private final String gameId;
    private final String gameName;
    private final String gameVersion;
    private final String gameLauncherVersion;

    /**
     * 注入关键游戏数据
     *
     * @param gamePathHandle      游戏目录句柄
     * @param gameId              游戏ID
     * @param gameName            游戏名称
     * @param gameVersion         游戏版本
     * @param gameLauncherVersion 游戏启动器版本
     */
    public EventEnterGame (FileHandle gamePathHandle, String gameId, String gameName, String gameVersion, String gameLauncherVersion)
    {
        eventName = Event.ENTER_GAME;
        this.gamePathHandle = gamePathHandle;
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameVersion = gameVersion;
        this.gameLauncherVersion = gameLauncherVersion;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称字符串
     */
    @Override
    public String getEventName ()
    {
        return eventName;
    }

    /**
     * 获取游戏ID
     *
     * @return 游戏ID
     */
    public String getGameId ()
    {
        return gameId;
    }

    /**
     * 获取游戏名称
     *
     * @return 游戏名称
     */
    public String getGameName ()
    {
        return gameName;
    }

    /**
     * 获取游戏目录句柄
     *
     * @return 游戏目录句柄
     */
    public FileHandle getGamePathHandle ()
    {
        return gamePathHandle;
    }

    /**
     * 获取游戏版本
     *
     * @return 游戏版本
     */
    public String getGameVersion ()
    {
        return gameVersion;
    }

    /**
     * 获取游戏启动器版本
     *
     * @return 游戏启动器版本
     */
    public String getGameLauncherVersion ()
    {
        return gameLauncherVersion;
    }

}
