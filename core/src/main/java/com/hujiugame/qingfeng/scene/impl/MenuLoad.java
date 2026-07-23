package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class MenuLoad implements GameRender
{
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final EventQueue eventQueue;
    private final GameHost gameHost;
    private GameStateDataContainer gameStateDataContainer;
    // 用帧数不用时间：enterGame() 内有阻塞，deltaTime 会突变，帧数计数更可靠
    private final int showCountMax = 1;
    private final int showCountWatchDog = 2;
    private int showCount = 0;

    public MenuLoad (AudioManager audioManager, GraphicsManager graphicsManager,
                     UiManager uiManager, EventQueue eventQueue,
                     GameHost gameHost)
    {
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.uiManager = uiManager;
        this.eventQueue = eventQueue;
        this.gameHost = gameHost;
    }

    /**
     * 初始化加载闪屏布局
     *
     * @param gameStateDataContainer 游戏状态数据容器
     */
    @Override
    public void init (GameStateDataContainer gameStateDataContainer)
    {
        this.gameStateDataContainer = gameStateDataContainer;

        uiManager.addLayout(gameStateDataContainer.getLayoutConfig());
    }

    /**
     * 等待加载帧数到达后进入游戏，超时则强制重置
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 如果循环在本界面，前置重置
        if (showCount >= showCountWatchDog)
        {
            LogUtils.error(MenuLoad.class, "MenuLoad 载入游戏数据出错");
            gameHost.getGameSessionManager().quitGame();
        }

        // 直接进入游戏，本渲染是闪图
        if (showCount >= showCountMax)
        {
            LogUtils.info(MenuLoad.class, "MenuLoad 载入游戏数据");
            gameHost.getGameSessionManager().enterGame();
        }

        // 加载计数
        showCount++;
    }

    /**
     * 渲染加载闪屏布局
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        audioManager.playLayout(gameStateDataContainer.getLayoutConfig());
        graphicsManager.putLayout(gameStateDataContainer.getLayoutConfig(), deltaTime);
    }

    /**
     * 重置加载计数器并释放布局资源
     */
    @Override
    public void dispose ()
    {
        showCount = 0;

        uiManager.deleteLayout(gameStateDataContainer.getLayoutConfig());
    }
}
