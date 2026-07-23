package com.hujiugame.qingfeng.script.task;

import com.hujiugame.qingfeng.script.data.command.ScriptCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScriptTask implements Task
{
    private final TaskType taskType;
    private Task parentTask;

    private final List<ScriptCommand> commands;
    private int currentCommandIndex = 0;

    private final Object defaultReturnValue;
    private boolean doEnableReturnValue = false;
    private boolean returnValueSet = false;
    private Object returnValue;

    private Map<String, Object> scopeVariables;

    public ScriptTask (TaskType taskType, Task parentTask, List<ScriptCommand> commands, Object defaultReturnValue, Map<String, Object> scope_variables)
    {
        this.taskType = taskType;
        this.parentTask = parentTask;
        this.commands = commands;
        this.defaultReturnValue = defaultReturnValue;
        this.scopeVariables = scope_variables;
    }

    @Override
    public TaskType getType ()
    {
        return taskType;
    }

    @Override
    public boolean isFinished ()
    {
        return currentCommandIndex >= commands.size();
    }

    @Override
    public void forceFinish ()
    {
        currentCommandIndex = commands.size();
    }

    @Override
    public Task getParentTask ()
    {
        return parentTask;
    }

    @Override
    public void setParentTask (Task parentTask)
    {
        this.parentTask = parentTask;
    }

    @Override
    public boolean setCurrentCommandIndex (int index)
    {
        if (index >= 0 && index <= commands.size())
        {
            currentCommandIndex = index;
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean nextCommand ()
    {
        return setCurrentCommandIndex(currentCommandIndex + 1);
    }

    @Override
    public List<Object> getCommands ()
    {
        return Collections.singletonList(commands);
    }

    @Override
    public Object getCurrentCommand ()
    {
        if (currentCommandIndex >= 0 && currentCommandIndex < commands.size())
        {
            return commands.get(currentCommandIndex);
        }
        else
        {
            return null;
        }
    }

    @Override
    public Object getDefaultReturnValue ()
    {
        return defaultReturnValue;
    }

    @Override
    public void enableReturnValue ()
    {
        doEnableReturnValue = true;
    }

    @Override
    public void disableReturnValue ()
    {
        doEnableReturnValue = false;
    }

    @Override
    public boolean isReturnValueSet ()
    {
        if (doEnableReturnValue)
        {
            return returnValueSet;
        }
        else
        {
            return false;
        }
    }

    @Override
    public Object consumeReturnValue ()
    {
        if (doEnableReturnValue)
        {
            if (returnValueSet)
            {
                doEnableReturnValue = false;
                returnValueSet = false;
                return returnValue;
            }
            else
            {
                return defaultReturnValue;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setReturnValue (Object returnValue)
    {
        if (doEnableReturnValue)
        {
            this.returnValue = returnValue;
            returnValueSet = true;
        }
    }

    public Map<String, Object> getScopeVariables ()
    {
        return scopeVariables;
    }

    public void setScopeVariables (Map<String, Object> scope_variables)
    {
        this.scopeVariables = scope_variables;
    }

    @Override
    public String toString ()
    {
        return "ScriptTask{" +
            "taskType=" + taskType +
            ", parentTask=" + parentTask +
            ", commands=" + commands +
            ", currentCommandIndex=" + currentCommandIndex +
            ", defaultReturnValue=" + defaultReturnValue +
            ", doEnableReturnValue=" + doEnableReturnValue +
            ", returnValueSet=" + returnValueSet +
            ", returnValue=" + returnValue +
            ", scopeVariables=" + scopeVariables +
            '}';
    }
}
