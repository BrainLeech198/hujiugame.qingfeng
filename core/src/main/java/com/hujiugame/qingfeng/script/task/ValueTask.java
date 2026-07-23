package com.hujiugame.qingfeng.script.task;

import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.action.ValueCommandAction;

import java.util.*;

public class ValueTask implements Task
{
    private final TaskType taskType;
    private Task parentTask;

    private final List<ValueCommand> commands;
    private int currentCommandIndex = 0;

    private final Object defaultReturnValue;
    private boolean doEnableReturnValue = false;
    private boolean returnValueSet = false;
    private Object returnValue;

    private final Stack<ValueCommandAction> actionStack = new Stack<>();
    private final Stack<List<Object>> actionParamStack = new Stack<>();
    private static final Map<ValueCommandAction, Integer> actionParamCount = new HashMap<>();
    static
    {
        actionParamCount.put(ValueCommandAction.ADD, 2);
        actionParamCount.put(ValueCommandAction.SUB, 2);
        actionParamCount.put(ValueCommandAction.MUL, 2);
        actionParamCount.put(ValueCommandAction.DIV, 2);
        actionParamCount.put(ValueCommandAction.NEG, 1);
        actionParamCount.put(ValueCommandAction.RANDOM, 2);

        actionParamCount.put(ValueCommandAction.EQUAL, 2);
        actionParamCount.put(ValueCommandAction.NOT_EQUAL, 2);
        actionParamCount.put(ValueCommandAction.GREATER, 2);
        actionParamCount.put(ValueCommandAction.LESS, 2);
        actionParamCount.put(ValueCommandAction.GREATER_EQUAL, 2);
        actionParamCount.put(ValueCommandAction.LESS_EQUAL, 2);

        actionParamCount.put(ValueCommandAction.AND, 2);
        actionParamCount.put(ValueCommandAction.OR, 2);
        actionParamCount.put(ValueCommandAction.NOT, 1);
    }

    public ValueTask (TaskType taskType, Task parentTask, List<ValueCommand> commands, Object defaultReturnValue)
    {
        this.taskType = taskType;
        this.parentTask = parentTask;
        this.commands = commands;
        this.defaultReturnValue = defaultReturnValue;
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
    public Object getDefaultReturnValue ()
    {
        return defaultReturnValue;
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

    public boolean actionStackEmpty ()
    {
        return actionStack.isEmpty();
    }

    public ValueCommandAction getCurrentAction ()
    {
        if (actionStack.isEmpty())
        {
            return null;
        }
        else
        {
            return actionStack.peek();
        }
    }

    public List<Object> getCurrentActionParams ()
    {
        if (actionStack.isEmpty())
        {
            return null;
        }
        else
        {
            return actionParamStack.peek();
        }
    }

    public void pushAction (ValueCommandAction action)
    {
        actionStack.push(action);
        actionParamStack.push(new ArrayList<>());
    }

    public void popAction ()
    {
        actionStack.pop();
        actionParamStack.pop();
    }

    public void pushActionParam (Object param)
    {
        if (actionStack.isEmpty())
        {
            return;
        }
        ValueCommandAction action = actionStack.peek();
        int paramCount = actionParamStack.peek().size();
        int maxParamCount = actionParamCount.get(action);
        if (paramCount < maxParamCount)
        {
            actionParamStack.peek().add(param);
        }
    }

    public boolean isActionParamReady ()
    {
        if (actionStack.isEmpty())
        {
            return false;
        }
        ValueCommandAction action = actionStack.peek();
        int paramCount = actionParamStack.peek().size();
        int maxParamCount = actionParamCount.get(action);
        return paramCount == maxParamCount;
    }

    @Override
    public String toString() {
        return "ValueTask{" +
            "taskType=" + taskType +
            ", parentTask=" + parentTask +
            ", commands=" + commands +
            ", currentCommandIndex=" + currentCommandIndex +
            ", defaultReturnValue=" + defaultReturnValue +
            ", doEnableReturnValue=" + doEnableReturnValue +
            ", returnValueSet=" + returnValueSet +
            ", returnValue=" + returnValue +
            ", actionStack=" + actionStack +
            ", actionParamStack=" + actionParamStack +
            '}';
    }
}
