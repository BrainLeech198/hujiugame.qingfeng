package com.hujiugame.qingfeng.script.data.value.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.action.ValueCommandAction;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public interface ValueCommand
{
    boolean isValid ();
    ValueCommandType getCommandType ();
    ValueCommandAction getCommandAction ();
    ValueCommandParam getCommandParam ();
    JsonEntity getJson ();
}
