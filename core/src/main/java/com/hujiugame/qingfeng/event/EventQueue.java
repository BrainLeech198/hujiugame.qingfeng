package com.hujiugame.qingfeng.event;

import java.util.LinkedList;
import java.util.Queue;

public final class EventQueue
{
    private final Queue<EventObject> queue = new LinkedList<>();

    /**
     * 向事件队列中添加一个新事件
     *
     * @param event 事件对象
     */
    public void addEvent (EventObject event)
    {
        queue.add(event);
    }

    /**
     * 从事件队列中取出并移除最早的事件
     *
     * @return 最早的事件对象，队列为空时返回 null
     */
    public EventObject getEvent ()
    {
        return queue.poll();
    }

    /**
     * 检查事件队列中是否还有未处理的事件
     *
     * @return 队列中有事件返回 true，为空返回 false
     */
    public boolean hasEvent ()
    {
        return !queue.isEmpty();
    }
}
