package com.hujiugame.qingfeng.script.data.value.command.param;

import com.hujiugame.qingfeng.data.JsonEntity;

public interface ValueCommandParam
{
    boolean isValid ();
    JsonEntity getJson ();
}
