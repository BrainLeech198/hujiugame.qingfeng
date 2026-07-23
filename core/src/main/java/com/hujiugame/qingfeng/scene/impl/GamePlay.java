package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.data.play.Player;
import com.hujiugame.qingfeng.data.story.page.Page;
import com.hujiugame.qingfeng.data.story.page.PageBehavior;
import com.hujiugame.qingfeng.script.ScriptContent;
import com.hujiugame.qingfeng.script.ScriptExecutor;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.trigger.Trigger;
import com.hujiugame.qingfeng.script.task.ScriptTask;
import com.hujiugame.qingfeng.script.task.TaskType;
import com.hujiugame.qingfeng.script.task.TriggerTask;
import com.hujiugame.qingfeng.type.Name;
import com.hujiugame.qingfeng.type.play.Hoster;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.manager.LayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class GamePlay implements GameRender
{
    private final EventQueue eventQueue;
    private final LayoutManager layoutManager;
    private final GameHost gameHost;
    private AudioManager gameAudioManager;
    private GraphicsManager gameGraphicsManager;
    private UiManager gameUiManager;
    private ScriptExecutor gameScriptExecutor;
    private GameStateDataContainer gameStateDataContainer;

    private Layout layout;

    // ===================================================================================================================

    public GamePlay (EventQueue eventQueue, LayoutManager layoutManager,
                     GameHost gameHost)
    {
        this.eventQueue = eventQueue;
        this.layoutManager = layoutManager;
        this.gameHost = gameHost;
    }

    /**
     * 生成游戏布局
     *
     * @param pageTemplateLayout 模板布局
     * @param pageLayout         实际布局
     */
    private void updateLayout (Layout pageTemplateLayout, Layout pageLayout)
    {
        // 销毁旧布局
        if (layout != null) gameUiManager.deleteLayout(layout);

        // 合并模板布局和实际布局
        layout = layoutManager.mergedLayout(pageTemplateLayout, pageLayout);

        // 添加布局
        gameUiManager.addLayout(layout);
    }

    /**
     * 显示当前玩家的页面布局
     * <p>
     * 该方法从玩家获取当前页面，提取页面布局配置和对应的模板布局配置，
     * 然后通过 {@link #updateLayout(Layout, Layout)} 方法更新并显示布局。
     *
     * @param player 当前玩家对象，用于获取其正在查看的页面
     */
    private void showLayout (Player player)
    {
        // 获取页面
        Page nowPage = player.getNowPage();
        if (nowPage == null) return;
        Layout pageLayout = nowPage.getLayoutConfig();
        Layout pageTemplateLayout = gameHost.getPlayLocalData().getGameTemplateManager().getTemplate(pageLayout.getTemplate());
        updateLayout(pageTemplateLayout, pageLayout);
    }

    /**
     * 本地主机更新逻辑
     */
    private void localHostUpdate (float deltaTime)
    {
        // 玩家数据
        Player player = gameHost.getPlayLocalData().getPlayer();

        // 是否正在进入页面
        if (player.getNextPage() != null)
        {
            // 删除loop任务
            if (gameScriptExecutor.hasTask(Name.GAME_LOOP_TASK_NAME))
            {
                gameScriptExecutor.removeTask(Name.GAME_LOOP_TASK_NAME);
            }
            // 删除trigger任务
            gameScriptExecutor.removeTriggerTask();

            // 是否已经委派初始化代码
            if (gameScriptExecutor.hasTask(Name.GAME_START_TASK_NAME))
            {
                // 判断任务已完成
                if (gameScriptExecutor.isTaskFinished(Name.GAME_START_TASK_NAME))
                {
                    // 删除start任务
                    gameScriptExecutor.removeTask(Name.GAME_START_TASK_NAME);

                    // 进入页面
                    player.enterNextPage();
                    // 展示页面
                    showLayout(player);

                    // 获取页面信息
                    Page nowPage = player.getNowPage();
                    PageBehavior pageBehavior = nowPage.getPageBehavior();

                    // 分发loop任务
                    List<ScriptCommand> scriptCommands = pageBehavior.getLoopScriptCommands();
                    scriptCommands = scriptCommands == null ? new ArrayList<>() : scriptCommands;

                    // 创建任务
                    ScriptTask startTask = new ScriptTask(TaskType.COMMAND_LOOP, null, scriptCommands, 0, new HashMap<>());

                    // 开始任务
                    gameScriptExecutor.addTask(Name.GAME_LOOP_TASK_NAME, startTask);

                    // 分发trigger任务
                    List<Trigger> triggers = pageBehavior.getTriggers();
                    for (int i = 0; i < triggers.size(); i++)
                    {
                        Trigger trigger = triggers.get(i);

                        //创建任务
                        TriggerTask triggerTask = new TriggerTask(TaskType.TRIGGER, trigger);

                        // 开始任务
                        gameScriptExecutor.addTask(Name.GAME_TRIGGER_TASK_NAME + i, triggerTask);
                    }
                }
            }
            // 初始化页面
            else
            {
                // 提取任务
                Page nextPage = player.getNextPage();
                PageBehavior pageBehavior = nextPage.getPageBehavior();
                List<ScriptCommand> scriptCommands = pageBehavior.getStartScriptCommands();
                scriptCommands = scriptCommands == null ? new ArrayList<>() : scriptCommands;

                // 创建任务
                ScriptTask startTask = new ScriptTask(TaskType.COMMAND_NORMAL, null, scriptCommands, 0, new HashMap<>());

                // 开始任务
                gameScriptExecutor.addTask(Name.GAME_START_TASK_NAME, startTask);
            }
        }

        // 更新代码
        gameScriptExecutor.update(deltaTime);
    }

    /**
     * 远程主机更新逻辑
     */
    private void remoteHostUpdate (float deltaTime)
    {
    }

    /**
     * 初始化游戏内播放布局和资源
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
        gameScriptExecutor = gameHost.getPlayLocalData().getScriptExecutor();
        gameScriptExecutor.setScriptContent(new ScriptContent(
            gameUiManager,
            gameHost.getGameSessionManager(),
            gameHost.getPlayLocalData().getGameVariableManager(),
            gameHost.getGameInfoManager(),
            gameHost.getPlayLocalData().getGameScriptManager()
        ));
    }

    /**
     * 处理游戏播放逻辑（当前暂无帧更新逻辑）
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 根据Player的主机类型进行逻辑处理
        if (gameHost.getPlayLocalData().getPlayer().getHoster() == Hoster.LOCAL_HOST) localHostUpdate(deltaTime);
        else if (gameHost.getPlayLocalData().getPlayer().getHoster() == Hoster.REMOTE_HOST) remoteHostUpdate(deltaTime);
    }

    /**
     * 渲染游戏播放布局和音频
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
     * 释放游戏播放布局资源
     */
    @Override
    public void dispose ()
    {
        gameUiManager.deleteLayout(layout);

    }
}
