package com.hujiugame.qingfeng.event.imp;

import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 设置游戏状态事件，用于直接替换当前游戏状态。
 */
public class EventSetGameState implements EventObject
{
    private final String eventName;
    private final int state;
    private final int subState;

    /**
     * 构造设置游戏状态事件
     *
     * @param state    目标状态
     * @param subState 目标子状态
     */
    public EventSetGameState (int state, int subState)
    {
        eventName = Event.SET_GAME_STATE;
        this.state = state;
        this.subState = subState;
    }

    /**
     * 获取目标状态
     *
     * @return 状态值
     */
    public int getState ()
    {
        return state;
    }

    /**
     * 获取目标子状态
     * @return 子状态值
     */
    public int getSubState ()
    {
        return subState;
    }

    /**
     * 获取事件名称
     * @return 事件名称字符串
     */
    @Override
    public String getEventName ()
    {
        return eventName;
    }

}
