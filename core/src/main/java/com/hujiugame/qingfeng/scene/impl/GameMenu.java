package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameSubState;
import com.hujiugame.qingfeng.type.key.RequirementKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPushGameState;
import com.hujiugame.qingfeng.event.imp.EventResetGameState;

public final class GameMenu implements GameRender
{
    private final EventQueue eventQueue;
    private final GameHost gameHost;
    private AudioManager gameAudioManager;
    private GraphicsManager gameGraphicsManager;
    private UiManager gameUiManager;
    private GameStateDataContainer gameStateDataContainer;

    // ===================================================================================================================

    public GameMenu (EventQueue eventQueue, GameHost gameHost)
    {
        this.eventQueue = eventQueue;
        this.gameHost = gameHost;
    }

    /**
     * 初始化游戏内菜单布局
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
        gameUiManager.addLayout(gameStateDataContainer.getLayoutConfig());
    }

    /**
     * 处理游戏内菜单的开始、退出按钮和弹窗回调
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 开始按钮
        if (gameUiManager.isButtonClicked(RequirementKey.Ui.GAME_MENU_BUTTON_START))
        {
            eventQueue.addEvent(new EventPushGameState(GameState.GAME, GameSubState.GAME_ROLE));
        }

        // 按下返回按钮
        if (gameUiManager.isButtonClicked(RequirementKey.Ui.GAME_MENU_BUTTON_QUIT))
        {
            gameUiManager.getMessageBox().showAsk(RequirementKey.Language.IN_GAME_MESSAGE_BOX_QUIT_GAME_KEY,
                "{language$" + RequirementKey.Language.REQUIREMENT_BLOCK + "#" + RequirementKey.Language.IN_GAME_MESSAGE_BOX_FIRST_KEY + "." + RequirementKey.Language.IN_GAME_MESSAGE_BOX_QUIT_GAME_TITLE + "}",
                "{language$" + RequirementKey.Language.REQUIREMENT_BLOCK + "#" + RequirementKey.Language.IN_GAME_MESSAGE_BOX_FIRST_KEY + "." + RequirementKey.Language.IN_GAME_MESSAGE_BOX_QUIT_GAME_CONTENT + "}"
            );
        }

        // 检测弹窗
        gameUiManager.getMessageBox().handleAsk(RequirementKey.Language.IN_GAME_MESSAGE_BOX_QUIT_GAME_KEY,
            () ->
            {
                gameHost.getGameSessionManager().quitGame();
                eventQueue.addEvent(new EventResetGameState());
            });
    }

    /**
     * 渲染游戏内菜单布局和音频
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        gameAudioManager.playLayout(gameStateDataContainer.getLayoutConfig());
        gameGraphicsManager.putLayout(gameStateDataContainer.getLayoutConfig(), deltaTime);
    }

    /**
     * 释放游戏内菜单布局资源
     */
    @Override
    public void dispose ()
    {
        gameUiManager.deleteLayout(gameStateDataContainer.getLayoutConfig());
    }

}
