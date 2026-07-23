package com.hujiugame.qingfeng.event.imp;

import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 重置游戏状态事件，用于重置游戏状态至初始状态。
 */
public class EventResetGameState implements EventObject
{
    private final String eventName;

    /**
     * 构造重置游戏状态事件
     */
    public EventResetGameState ()
    {
        eventName = Event.RESET_GAME_STATE;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称字符串
     */
    @Override
    public String getEventName ()
    {
        return eventName;
    }

}
