package com.hujiugame.qingfeng.script.task;

import java.util.List;

public interface Task
{
    TaskType getType ();
    boolean isFinished ();
    void forceFinish ();

    Task getParentTask ();
    void setParentTask (Task parentTask);

    boolean setCurrentCommandIndex (int index);
    boolean nextCommand ();

    List<Object> getCommands ();
    Object getCurrentCommand ();

    Object getDefaultReturnValue ();
    void enableReturnValue ();
    void disableReturnValue ();
    boolean isReturnValueSet ();
    void setReturnValue (Object returnValue);
    Object consumeReturnValue ();
}
