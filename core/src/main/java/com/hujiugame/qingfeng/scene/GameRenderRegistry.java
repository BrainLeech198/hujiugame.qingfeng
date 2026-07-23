package com.hujiugame.qingfeng.scene;

import com.hujiugame.qingfeng.data.game.StateStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 游戏渲染器注册中心，管理和缓存不同游戏状态的渲染器实例。
 */
public final class GameRenderRegistry
{
    private final Map<Integer, Supplier<GameRender>> factories = new HashMap<>();

    /**
     * 将游戏主状态和子状态组合为唯一键值。
     *
     * @param state    游戏主状态
     * @param subState 游戏子状态
     * @return 组合后的唯一键值
     */
    private static int buildKey (int state, int subState)
    {
        return state * 1000 + subState;
    }

    /**
     * 注册指定游戏状态的渲染器工厂。
     *
     * @param state    游戏主状态
     * @param subState 游戏子状态
     * @param factory  渲染器工厂，用于创建 GameRender 实例
     */
    public void register (int state, int subState, Supplier<GameRender> factory)
    {
        factories.put(buildKey(state, subState), factory);
    }

    /**
     * 获取指定游戏状态对应的渲染器（优先返回缓存的实例）。
     *
     * @param stateStructure 游戏状态结构
     * @return 渲染器实例，未注册时返回 null
     */
    @javax.annotation.Nullable
    public GameRender get (StateStructure stateStructure)
    {
        int key = buildKey(stateStructure.getState(), stateStructure.getSubState());

        Supplier<GameRender> factory = factories.get(key);
        if (factory == null) return null;

        return factory.get();
    }
}
