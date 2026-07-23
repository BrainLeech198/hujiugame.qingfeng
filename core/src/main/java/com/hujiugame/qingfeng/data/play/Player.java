package com.hujiugame.qingfeng.data.play;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.story.page.Page;
import com.hujiugame.qingfeng.data.story.Role;
import com.hujiugame.qingfeng.data.story.tree.TreeStructure;
import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.type.play.Hoster;
import com.hujiugame.qingfeng.game.GameInfoManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class Player
{
    private final GameInfoManager gameInfoManager;

    private FileHandle gamePathHandle;
    private String gameId;
    private String gameName;
    private String gameVersion;
    private String gameLauncherVersion;

    private Role role;
    private Hoster hoster;
    private String ipp;

    private TreeStructure treeStructure;
    private Page lastPage;
    private Page nowPage;
    private Page nextPage;

    /**
     * 创建玩家数据
     *
     * @param gameInfoManager 游戏信息管理器，用于同步记录游戏信息
     */
    public Player (GameInfoManager gameInfoManager)
    {
        this.gameInfoManager = gameInfoManager;
    }

    /**
     * 获取游戏路径句柄
     */
    public FileHandle getGamePathHandle ()
    {
        return gamePathHandle;
    }

    /**
     * 设置游戏路径并同步到游戏信息管理器
     */
    public void setGamePath (FileHandle gamePathHandle)
    {
        this.gamePathHandle = gamePathHandle;
        gameInfoManager.putInfo(GameInfoKey.GAME_PATH, gamePathHandle);//我也不确定传.path还是FileHandle
    }


    /**
     * 获取游戏 ID
     */
    public String getGameId ()
    {
        return gameId;
    }

    /**
     * 设置游戏 ID 并同步到游戏信息管理器
     */
    public void setGameId (String gameId)
    {
        this.gameId = gameId;
        gameInfoManager.putInfo(GameInfoKey.GAME_ID, gameId);
    }

    /**
     * 获取游戏名称
     */
    public String getGameName ()
    {
        return gameName;
    }

    /**
     * 设置游戏名称并同步到游戏信息管理器
     */
    public void setGameName (String gameName)
    {
        this.gameName = gameName;
        gameInfoManager.putInfo(GameInfoKey.GAME_NAME, gameName);
    }

    /**
     * 获取游戏版本
     */
    public String getGameVersion ()
    {
        return gameVersion;
    }

    /**
     * 设置游戏版本并同步到游戏信息管理器
     */
    public void setGameVersion (String gameVersion)
    {
        this.gameVersion = gameVersion;
        gameInfoManager.putInfo(GameInfoKey.GAME_VERSION, gameVersion);
    }

    /**
     * 获取游戏启动器版本
     */
    public String getGameLauncherVersion ()
    {
        return gameLauncherVersion;
    }

    /**
     * 设置游戏启动器版本并同步到游戏信息管理器
     */
    public void setGameLauncherVersion (String gameLauncherVersion)
    {
        this.gameLauncherVersion = gameLauncherVersion;
        gameInfoManager.putInfo(GameInfoKey.GAME_LAUNCHER_VERSION, gameLauncherVersion);
    }

    // ===================================================================================================================

    /**
     * 获取当前角色
     */
    public Role getRole ()
    {
        return role;
    }

    /**
     * 设置当前角色并同步角色 ID 到游戏信息管理器
     */
    public void setRole (Role role)
    {
        this.role = role;
        gameInfoManager.putInfo(GameInfoKey.PLAY_ROLE_ID, role.getId());
    }

    /**
     * 获取主持人类型
     */
    public Hoster getHoster ()
    {
        return hoster;
    }

    /**
     * 设置主持人类型并同步到游戏信息管理器
     */
    public void setHoster (Hoster hoster)
    {
        this.hoster = hoster;
        gameInfoManager.putInfo(GameInfoKey.PLAY_HOSTER, hoster.name());
    }

    /**
     * 获取网络地址
     */
    public String getIpp ()
    {
        return ipp;
    }

    /**
     * 设置网络地址
     */
    public void setIpp (String ipp)
    {
        this.ipp = ipp;
        gameInfoManager.putInfo(GameInfoKey.PLAY_IPP, ipp);
    }

    // ===================================================================================================================

    /**
     * 获取当前的树形结构
     */
    public TreeStructure getTreeStructure ()
    {
        return treeStructure;
    }

    /**
     * 设置树形结构并同步类型和 ID 到游戏信息管理器
     *
     * @return 设置成功返回 true，传入 null 返回 false
     */
    public boolean setTreeStructure (@javax.annotation.Nullable TreeStructure treeStructure)
    {
        this.treeStructure = treeStructure;
        if (treeStructure == null)
        {
            LogUtils.error(Player.class, "setTreeStructure treeStructure为null");
            return false;
        }
        gameInfoManager.putInfo(GameInfoKey.PLAY_TREE_STRUCTURE_TYPE, treeStructure.getClass().getSimpleName());
        gameInfoManager.putInfo(GameInfoKey.PLAY_TREE_STRUCTURE_ID, treeStructure.getTreeStructureInfo().getStructureId());
        return true;
    }

    /**
     * 获取上一页
     */
    public Page getLastPage ()
    {
        return lastPage;
    }

    /**
     * 设置上一页并同步 ID 到游戏信息管理器
     * @return 设置成功返回 true
     */
    private boolean setLastPage (Page lastPage)
    {
        if (lastPage == null)
        {
            LogUtils.error(Player.class, "setLastPage lastPage为null");
            return false;
        }
        this.lastPage = lastPage;
        gameInfoManager.putInfo(GameInfoKey.PLAY_LAST_PAGE_ID, lastPage.getId());
        return true;
    }

    /**
     * 获取当前页
     */
    public Page getNowPage ()
    {
        return nowPage;
    }

    /**
     * 设置当前页，自动将旧页设为上一页
     * @return 设置成功返回 true
     */
    public boolean setNowPage (@javax.annotation.Nullable Page nowPage)
    {
        if (nowPage == null)
        {
            LogUtils.error(Player.class, "setNowPage nowPage为null");
            return false;
        }

        // 检测是否需要更新
        if (this.nowPage == null || !nowPage.getId().equals(this.nowPage.getId()))
        {
            if (this.nowPage != null)
            {
                setLastPage(this.nowPage);
            }
            this.nowPage = nowPage;
            gameInfoManager.putInfo(GameInfoKey.PLAY_NOW_PAGE_ID, nowPage.getId());
        }

        return true;
    }

    /**
     * 获取下一页
     */
    public Page getNextPage ()
    {
        return nextPage;
    }

    /**
     * 设置下一页
     * @return 添加成功返回 true
     */
    public boolean setNextPage (Page nextPage)
    {
        this.nextPage = nextPage;
        if (nextPage != null)
        {
            gameInfoManager.putInfo(GameInfoKey.PLAY_NEXT_PAGE_ID, nextPage.getId());
        }
        else
        {
            gameInfoManager.putInfo(GameInfoKey.PLAY_NEXT_PAGE_ID, null);
        }
        return true;
    }

    /**
     * 进入下一页
     * @return 进入成功返回 true
     */
    public boolean enterNextPage ()
    {
        return setNowPage(nextPage) && setNextPage(null);
    }
}
