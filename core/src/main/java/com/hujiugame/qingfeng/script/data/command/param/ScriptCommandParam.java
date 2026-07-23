package com.hujiugame.qingfeng.script.data.command.param;

import com.hujiugame.qingfeng.data.JsonEntity;

public interface ScriptCommandParam
{
    boolean isValid ();
    JsonEntity getJson ();
}
