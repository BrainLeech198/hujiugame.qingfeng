package com.hujiugame.qingfeng.data.play;

import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.List;

public final class PlayRuntimeData
{
    private List<Player> playerList = new java.util.ArrayList<>();

    /**
     * 获取玩家列表
     *
     * @return 玩家列表
     */
    public List<Player> getPlayerList ()
    {
        return playerList;
    }

    /**
     * 设置玩家列表
     *
     * @param playerList 玩家列表
     */
    public void setPlayerList (List<Player> playerList)
    {
        this.playerList = playerList;
    }

    /**
     * 添加玩家
     *
     * @param player 玩家
     */
    public void addPlayer (Player player)
    {
        playerList.add(player);
    }

    /**
     * 移除玩家
     *
     * @param player 玩家
     */
    public void removePlayer (Player player)
    {
        playerList.remove(player);
    }

    /*
     * 销毁资源
     */
    public boolean dispose ()
    {
        try
        {
            LogUtils.debug(PlayRuntimeData.class, "dispose 销毁资源成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(PlayRuntimeData.class, "dispose", e);
            return false;
        }
    }
}
