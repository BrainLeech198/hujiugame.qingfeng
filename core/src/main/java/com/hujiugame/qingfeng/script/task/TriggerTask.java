package com.hujiugame.qingfeng.script.task;

import com.hujiugame.qingfeng.script.data.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;

public class TriggerTask implements Task
{
    private final TaskType taskType;
    private final Trigger trigger;

    public TriggerTask (TaskType taskType, Trigger trigger)
    {
        this.taskType = taskType;
        this.trigger = trigger;
    }

    @Override
    public TaskType getType ()
    {
        return taskType;
    }

    @Override
    public boolean isFinished ()
    {
        return false;
    }

    @Override
    public void forceFinish ()
    {
    }

    @Override
    public Task getParentTask ()
    {
        return null;
    }

    @Override
    public void setParentTask (Task parentTask)
    {
    }

    @Override
    public boolean setCurrentCommandIndex (int index)
    {
        return false;
    }

    @Override
    public boolean nextCommand ()
    {
        return false;
    }

    @Override
    public List<Object> getCommands ()
    {
        return new ArrayList<>();
    }

    @Override
    public Object getCurrentCommand ()
    {
        return trigger.getTriggerCommand();
    }

    @Override
    public Object getDefaultReturnValue ()
    {
        return null;
    }

    @Override
    public void enableReturnValue ()
    {
    }

    @Override
    public void disableReturnValue ()
    {
    }

    @Override
    public boolean isReturnValueSet ()
    {
        return false;
    }

    @Override
    public Object consumeReturnValue ()
    {
        return null;
    }

    @Override
    public void setReturnValue (Object returnValue)
    {
    }

    public Trigger getTrigger ()
    {
        return trigger;
    }

    @Override
    public String toString() {
        return "TriggerTask{" +
            "taskType=" + taskType +
            ", trigger=" + trigger +
            '}';
    }
}
