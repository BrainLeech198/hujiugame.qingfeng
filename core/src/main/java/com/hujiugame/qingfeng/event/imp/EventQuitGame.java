package com.hujiugame.qingfeng.event.imp;

import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 退出游戏事件
 */
public class EventQuitGame implements EventObject
{
    private final String eventName;

    /**
     * 构造退出游戏事件
     */
    public EventQuitGame ()
    {
        this.eventName = Event.QUIT_GAME;
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
