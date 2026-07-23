package com.hujiugame.qingfeng.script.data.trigger.command.param;

import com.hujiugame.qingfeng.data.JsonEntity;

public interface TriggerParam
{
    boolean isValid ();
    JsonEntity getJson ();
}
