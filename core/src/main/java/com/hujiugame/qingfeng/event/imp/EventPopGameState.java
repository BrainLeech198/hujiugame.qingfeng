package com.hujiugame.qingfeng.event.imp;

import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 弹出游戏状态事件，用于从状态栈中移除当前状态。
 */
public class EventPopGameState implements EventObject
{
    private final String eventName;

    /**
     * 构造弹出游戏状态事件
     */
    public EventPopGameState ()
    {
        eventName = Event.POP_GAME_STATE;
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
