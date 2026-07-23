package com.hujiugame.qingfeng.script.data.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.action.ScriptCommandAction;
import com.hujiugame.qingfeng.script.data.command.param.ScriptCommandParam;

public interface ScriptCommand
{
    boolean isValid ();
    JsonEntity getJson ();
    ScriptCommandType getCommandType ();
    ScriptCommandAction getCommandAction ();
    ScriptCommandParam getCommandParam ();
}
