package com.hujiugame.qingfeng.event.imp;

import com.hujiugame.qingfeng.data.story.Role;
import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.type.play.Hoster;
import com.hujiugame.qingfeng.event.EventObject;

/**
 * 退出游戏事件
 */
public class EventPlayGame implements EventObject
{
    private final String eventName;
    private final Hoster hoster;
    private final Role role;

    /**
     * 构造游戏事件对象
     *
     * @param hoster 游戏主持类型
     * @param role   角色对象
     */
    public EventPlayGame (Hoster hoster, Role role)
    {
        eventName = Event.PLAY_GAME;
        this.hoster = hoster;
        this.role = role;
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

    /**
     * 获取游戏主持类型
     *
     * @return 游戏主持类型
     */
    public Hoster getHoster ()
    {
        return hoster;
    }

    /**
     * 获取角色对象
     *
     * @return 角色对象
     */
    public Role getRole ()
    {
        return role;
    }

}

