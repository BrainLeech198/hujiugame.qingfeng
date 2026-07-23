package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.data.story.Role;
import com.hujiugame.qingfeng.type.key.UniversalKey;
import com.hujiugame.qingfeng.type.play.Hoster;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPopGameState;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.List;

public final class GameRole implements GameRender
{
    private final EventQueue eventQueue;
    private final LayoutManager layoutManager;
    private final GameHost gameHost;
    private AudioManager gameAudioManager;
    private GraphicsManager gameGraphicsManager;
    private UiManager gameUiManager;
    private GameStateDataContainer gameStateDataContainer;

    private Layout layout;

    private List<String> roleIdList;
    private int roleIndex = 0;
    private Layout roleShowLayout;

    // ===================================================================================================================

    public GameRole (EventQueue eventQueue, LayoutManager layoutManager,
                     GameHost gameHost)
    {
        this.eventQueue = eventQueue;
        this.layoutManager = layoutManager;
        this.gameHost = gameHost;
    }

    // ===================================================================================================================

    void refreshUi()
    {
        if (roleIdList.size() > 1)
        {
            gameUiManager.showButton("select_lastRole");
            gameUiManager.showButton("select_nextRole");
        }
        else
        {
            gameUiManager.hideButton("select_lastRole");
            gameUiManager.hideButton("select_nextRole");
        }

        if (roleIndex > 0)
        {
            gameUiManager.enableButton("select_lastRole");
        }
        else
        {
            gameUiManager.disableButton("select_lastRole");
        }

        if (roleIndex < roleIdList.size() - 1)
        {
            gameUiManager.enableButton("select_nextRole");
        }
        else
        {
            gameUiManager.disableButton("select_nextRole");
        }
    }

    private void loadShowLayout ()
    {
        // 擦除上一次的显示
        if (roleShowLayout != null)
        {
            gameUiManager.deleteLayout(layout);
        }

        // 读取角色展示layout
        roleShowLayout = gameHost.getPlayLocalData().getGameRoleManager().getRole(roleIdList.get(roleIndex)).getShowLayout();

        // 绘制角色展示layout
        layout = layoutManager.mergedLayout(gameStateDataContainer.getLayoutConfig(), roleShowLayout);
        gameUiManager.addLayout(layout);
        LogUtils.debug(GameRole.class, "loadShowLayout 展示角色 (id): " + roleIdList.get(roleIndex) + " (layout): " + layout);

        refreshUi();
    }

    private void nextRole ()
    {
        if (roleIndex < roleIdList.size() - 1)
        {
            roleIndex++;
            loadShowLayout();
        }
    }

    private void lastRole ()
    {
        if (roleIndex > 0)
        {
            roleIndex--;
            loadShowLayout();
        }
    }

    /**
     * 初始化角色选择布局和角色列表
     *
     * @param gameStateDataContainer 游戏状态数据容器
     */
    @Override
    public void init (GameStateDataContainer gameStateDataContainer)
    {
        this.gameStateDataContainer = gameStateDataContainer;

        gameAudioManager = gameHost.getPlayLocalData().getAudioManager();
        gameGraphicsManager = gameHost.getPlayLocalData().getGraphicsManager();
        gameUiManager = gameHost.getPlayLocalData().getUiManager();

        layout = gameStateDataContainer.getLayoutConfig();

        roleIdList = gameHost.getPlayLocalData().getGameRoleManager().getRoleIdList();
        loadShowLayout();
    }

    /**
     * 处理角色选择、切换和返回操作
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 按下返回按钮
        if (gameUiManager.isButtonClicked(UniversalKey.BUTTON_BACK))
        {
            eventQueue.addEvent(new EventPopGameState());
        }

        // 单人模式角色
        if (gameUiManager.isButtonClicked("select"))
        {
            Hoster hoster = Hoster.LOCAL_HOST;
            Role role = gameHost.getPlayLocalData().getGameRoleManager().getRole(roleIdList.get(roleIndex));
            if (!gameHost.getGameSessionManager().playNewStory(hoster, role))
            {
                gameHost.getGameSessionManager().quitGame();
                LogUtils.error(GameRole.class, "playNewStory 创建单人新故事游戏失败");
            }
        }

        // 下一个角色
        if (gameUiManager.isButtonClicked("select_nextRole"))
        {
            nextRole();
        }

        // 上一个角色
        if (gameUiManager.isButtonClicked("select_lastRole"))
        {
            lastRole();
        }
    }

    /**
     * 渲染角色选择布局和音频
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        gameAudioManager.playLayout(layout);
        gameGraphicsManager.putLayout(layout, deltaTime);
    }

    /**
     * 释放角色选择布局资源
     */
    @Override
    public void dispose ()
    {
        gameUiManager.deleteLayout(layout);

    }

}
