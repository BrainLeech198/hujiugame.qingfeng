package com.hujiugame.qingfeng.script.task;

import java.util.Stack;

import com.hujiugame.qingfeng.util.system.LogUtils;

public class TaskStack
{
    private final Stack<Task> taskStack;
    private Object returnValue;
    private boolean suspended;
    private double suspendTime;
    private SuspendReason suspendReason;

    public static enum SuspendReason
    {
        NONE,
        FORCE,
        WAIT
    }

    public TaskStack ()
    {
        taskStack = new Stack<Task>();
        suspended = false;
        suspendTime = -1.0;
        suspendReason = SuspendReason.NONE;
    }

    public void push (Task task)
    {
        taskStack.push(task);
        LogUtils.debug(TaskStack.class, "push 推入任务 目前任务栈"
            + " (size): " + taskStack.size() + " 任务信息 (type): " + task.getType() + " (stackSize): " + taskStack.size());
    }

    public Task pop ()
    {
        if (taskStack.isEmpty())
        {
            LogUtils.error(TaskStack.class, "pop 栈为空");
            return null;
        }
        else
        {
            Task task = taskStack.pop();
            LogUtils.debug(TaskStack.class, "pop 弹出任务 目前任务栈"
                + " (size): " + taskStack.size() + " 任务信息 (type): " + task.getType() + " (stackSize): " + taskStack.size());
            return task;
        }
    }

    public Task peek ()
    {
        if (taskStack.isEmpty())
        {
            LogUtils.error(TaskStack.class, "peek 栈为空");
            return null;
        }
        else
        {
            return taskStack.peek();
        }
    }

    public boolean isEmpty ()
    {
        return taskStack.isEmpty();
    }

    public int size ()
    {
        return taskStack.size();
    }

    public Object getReturnValue ()
    {
        return returnValue;
    }

    public void setReturnValue (Object returnValue)
    {
        this.returnValue = returnValue;
    }

    public boolean isSuspended ()
    {
        return suspended;
    }

    public double getSuspendTime ()
    {
        return suspendTime;
    }

    public SuspendReason getSuspendReason ()
    {
        return suspendReason;
    }

    public void suspend (SuspendReason reason)
    {
        suspended = true;
        suspendTime = System.currentTimeMillis() / 1000.0;
        suspendReason = reason;
        LogUtils.debug(TaskStack.class, "suspend 暂挂脚本任务栈 (reason): " + reason + " (time): " + suspendTime);
    }

    public void resume ()
    {
        LogUtils.debug(TaskStack.class, "resume 接触暂挂脚本任务栈 (reason): " + suspendReason + " (duration): " + (System.currentTimeMillis() / 1000.0 - suspendTime));
        suspended = false;
        suspendTime = -1.0;
        suspendReason = SuspendReason.NONE;
    }

}
