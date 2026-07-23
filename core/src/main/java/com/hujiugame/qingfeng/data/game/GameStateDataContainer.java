package com.hujiugame.qingfeng.data.game;

import com.hujiugame.qingfeng.data.JsonEntity;

public final class GameStateDataContainer
{
    private final StateStructure stateStructure;
    private final Layout layout;
    private final JsonEntity configJson;

    /**
     * 创建游戏状态数据容器
     *
     * @param stateStructure 状态结构
     * @param layout   布局配置
     */
    public GameStateDataContainer (StateStructure stateStructure,
                                   Layout layout,
                                   JsonEntity configJson
    )
    {
        this.stateStructure = stateStructure;
        this.layout = layout;
        this.configJson = configJson;
    }

    /**
     * 获取状态结构
     */
    public StateStructure getStateStructure ()
    {
        return stateStructure;
    }

    /**
     * 获取布局配置
     */
    public Layout getLayoutConfig ()
    {
        return layout;
    }

    /**
     * 获取配置数据
     */
    public JsonEntity getConfigJson ()
    {
        return configJson;
    }
}
