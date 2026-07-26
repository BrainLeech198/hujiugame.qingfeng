package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.type.key.UniversalUiKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPopGameState;

public final class ConfigDisplay implements GameRender
{
    private final EventQueue eventQueue;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private GameStateDataContainer gameStateDataContainer;

    // ===================================================================================================================

    public ConfigDisplay (EventQueue eventQueue, AudioManager audioManager,
                        GraphicsManager graphicsManager, UiManager uiManager)
    {
        this.eventQueue = eventQueue;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.uiManager = uiManager;
    }

    /**
     * 初始化配置界面布局
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
     * 处理配置界面返回按钮点击
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 按下返回按钮
        if (uiManager.isButtonClicked(UniversalUiKey.BUTTON_BACK))
        {
            eventQueue.addEvent(new EventPopGameState());
        }
    }

    /**
     * 渲染配置界面布局和音频
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
     * 释放配置界面布局资源
     */
    @Override
    public void dispose ()
    {
        uiManager.deleteLayout(gameStateDataContainer.getLayoutConfig());

        this.gameStateDataContainer = null;
    }
}
