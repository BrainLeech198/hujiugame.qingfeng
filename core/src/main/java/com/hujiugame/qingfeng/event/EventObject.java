package com.hujiugame.qingfeng.event;

/**
 * 事件对象接口，所有具体事件类型需实现此接口。
 */
public interface EventObject
{
    /**
     * 获取事件名称
     *
     * @return 事件名称字符串
     */
    String getEventName ();
}
