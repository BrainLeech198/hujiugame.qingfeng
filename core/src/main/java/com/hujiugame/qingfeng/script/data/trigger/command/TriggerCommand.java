package com.hujiugame.qingfeng.script.data.trigger.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.trigger.command.action.TriggerAction;
import com.hujiugame.qingfeng.script.data.trigger.command.param.TriggerParam;

public interface TriggerCommand
{
    boolean isValid ();
    JsonEntity getJson ();
    TriggerType getCommandType ();
    TriggerAction getCommandAction ();
    TriggerParam getCommandParam ();
}
