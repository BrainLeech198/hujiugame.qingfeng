package com.hujiugame.qingfeng.scene.impl;

import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.type.key.RequirementKey;
import com.hujiugame.qingfeng.type.key.UniversalUiKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPopGameState;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigBasic implements GameRender
{
    private final EventQueue eventQueue;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private GameStateDataContainer gameStateDataContainer;

    private static final List<String> itemTagList;
    private static final List<String> ItemSelectedTagList;
    static
    {
        List<String> list = new ArrayList<>();
        list.add(RequirementKey.Ui.CONFIG_BASIC_LANGUAGE);
        itemTagList = list;

        List<String> listSelected = new ArrayList<>();
        listSelected.add(RequirementKey.Ui.CONFIG_BASIC_LANGUAGE_SELECTED);
        ItemSelectedTagList = listSelected;
    }
    private static final Map<String, Boolean> itemSelectStateMap;
    static
    {
        Map<String, Boolean> map = new HashMap<>();
        for (String tag : itemTagList)
        {
            map.put(tag, false);
        }
        itemSelectStateMap = map;
    }

    // ===================================================================================================================

    public ConfigBasic (EventQueue eventQueue, AudioManager audioManager,
                        GraphicsManager graphicsManager, UiManager uiManager)
    {
        this.eventQueue = eventQueue;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.uiManager = uiManager;
    }

    /**
     * 刷新配置项显示
     */
    private void refreshItems ()
    {
        LogUtils.debug(ConfigBasic.class, "refreshItems 配置项状态 (map): " + itemSelectStateMap);
        for (int i = 0; i < itemTagList.size(); i++)
        {
            String tag = itemTagList.get(i);
            String selectedTag = ItemSelectedTagList.get(i);
            if (itemSelectStateMap.containsKey(tag))
            {
                if (itemSelectStateMap.get(tag))
                {
                    uiManager.hideLabel(tag);
                    uiManager.showLabel(selectedTag);
                }
                else
                {
                    uiManager.showLabel(tag);
                    uiManager.hideLabel(selectedTag);
                }
            }
        }
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
        refreshItems();
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
