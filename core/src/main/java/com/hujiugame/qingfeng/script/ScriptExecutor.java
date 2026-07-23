package com.hujiugame.qingfeng.script;

import com.badlogic.gdx.math.MathUtils;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.story.tree.TreeStructureInfo;
import com.hujiugame.qingfeng.script.data.ArgumentInfo;
import com.hujiugame.qingfeng.script.data.Script;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.param.control.*;
import com.hujiugame.qingfeng.script.data.command.param.story.GotoPageStoryScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.variable.AssignmentVariableScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.variable.CreateVariableScriptCommandParam;
import com.hujiugame.qingfeng.script.data.trigger.command.TriggerCommand;
import com.hujiugame.qingfeng.script.data.trigger.command.param.label.LabelClickTriggerParam;
import com.hujiugame.qingfeng.script.data.value.LogicValue;
import com.hujiugame.qingfeng.script.data.value.MathValue;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.action.ValueCommandAction;
import com.hujiugame.qingfeng.script.data.value.command.param.atomic.*;
import com.hujiugame.qingfeng.script.task.*;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public class ScriptExecutor
{
    private static final int MAX_COMMAND_COUNT_PER_FRAME = 50;
    private int remainingCommandsCount = 0;

    private ScriptContent scriptContent;
    private final Map<String, TaskStack> taskMap = new HashMap<>();
    private final List<String> triggerTaskList = new ArrayList<>();

    public boolean init ()
    {
        try
        {
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ScriptExecutor.class, "init", e);
            return false;
        }
    }

    public void setScriptContent (ScriptContent scriptContent)
    {
        this.scriptContent = scriptContent;
    }

    // ===================================================================================================================

    private int executeScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        switch (command.getCommandType())
        {
            case CONTROL:
                return executeControlScriptCommand(taskStack, task, command);

            case VARIABLE:
                return executeVariableScriptCommand(taskStack, task, command);

            case STORY:
                return executeStoryScriptCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeScriptCommand 不存在的指令类型 (commandType): " + command.getCommandType());
                return 0;
        }
    }

    private int executeValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        switch (command.getCommandType())
        {
            case MATH:
                return executeMathValueCommand(taskStack, task, command);

            case COMPARE:
                return executeCompareValueValueCommand(taskStack, task, command);

            case LOGIC:
                return executeLogicValueCommand(taskStack, task, command);

            case ATOMIC:
                return executeAtomicValueCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeValueCommand 不存在的指令类型 (commandType): " + command.getCommandType());
                return 0;
        }
    }

    private int executeTriggerCommand (TaskStack taskStack, TriggerTask task, TriggerCommand command)
    {
        switch (command.getCommandType())
        {
            case LABEL:
                return executeLabelTriggerCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeTriggerCommand 不存在的指令类型 (commandType): " + command.getCommandType());
                return 0;
        }
    }

    private int executeScriptTask (TaskStack taskStack, ScriptTask task, int remainingCommandsCount)
    {
        // 循环执行指令
        while (remainingCommandsCount > 0)
        {
            // 任务指令执行到底
            if (task.isFinished())
            {
                // 如果是loop则继续
                if (task.getType() == TaskType.COMMAND_LOOP)
                {
                    task.setCurrentCommandIndex(0);
                }

                // 尝试同步作用域
                synchronizeParentTaskScopeVariables(task.getParentTask(), task);

                // 设置默认返回值
                returnParentTaskDefaultReturnValue(task.getParentTask(), task);

                // 弹出任务栈
                taskStack.pop();

                LogUtils.debug(ScriptExecutor.class, "executeScriptTask 弹出任务 目前任务栈"
                    + " (size): " + taskStack.size() + " (remainingCommandsCount): " + remainingCommandsCount);
                return remainingCommandsCount;
            }
            // 继续执行指令
            else
            {
                // 存储栈状态
                int stackSize = taskStack.size();

                // 执行单条指令
                LogUtils.debug(ScriptExecutor.class, "executeScriptTask 执行指令"
                    + " (commandIndex): " + task.getCurrentCommand() + " (remainingCommandsCount): " + remainingCommandsCount);
                remainingCommandsCount -= executeScriptCommand(taskStack, task, (ScriptCommand) task.getCurrentCommand());

                // 判断目前是否处于栈顶
                if (stackSize != taskStack.size()) break;

                // 判断是否被挂起
                if (taskStack.isSuspended()) break;
            }
        }
        return remainingCommandsCount;
    }

    private int executeValueTask (TaskStack taskStack, ValueTask task, int remainingCommandsCount)
    {
        // 循环执行指令
        while (remainingCommandsCount > 0)
        {
            // 任务指令执行到底
            if (task.isFinished())
            {
                // 设置默认返回值
                returnParentTaskDefaultReturnValue(task.getParentTask(), task);

                // 弹出任务栈
                taskStack.pop();

                LogUtils.debug(ScriptExecutor.class, "executeValueTask 弹出任务 目前任务栈"
                    + " (size): " + taskStack.size() + " (remainingCommandsCount): " + remainingCommandsCount);
                return remainingCommandsCount;
            }
            // 继续执行指令
            else
            {
                LogUtils.debug(ScriptExecutor.class, "executeValueTask 执行指令"
                    + " (commandIndex): " + task.getCurrentCommand() + " (remainingCommandsCount): " + remainingCommandsCount);
                remainingCommandsCount -= executeValueCommand(taskStack, task, (ValueCommand) task.getCurrentCommand());
            }
        }
        // 意外退出循环
        return remainingCommandsCount;
    }

    private int executeTriggerTask (TaskStack taskStack, TriggerTask task, int remainingCommandsCount)
    {
        // 每帧执行一次
        //LogUtils.debug(ScriptExecutor.class, "executeTriggerTask 每帧执行一次"
        //    + " (commandIndex): " + task.getCurrentCommand() + " (remainingCommandsCount): " + remainingCommandsCount);
        executeTriggerCommand(taskStack, task, (TriggerCommand) task.getCurrentCommand());
        return remainingCommandsCount;
    }

    private int executeTaskStack (TaskStack taskStack, int remainingCommandsCount)
    {
        // 空任务栈
        if (taskStack.isEmpty())
        {
            return remainingCommandsCount;
        }
        // 非空任务栈
        else
        {
            Task task = taskStack.peek();
            //LogUtils.debug(ScriptExecutor.class, "executeTaskStack 准备处理栈顶任务 (taskStack): " + taskStack);
            if (task instanceof ScriptTask)
            {
                //LogUtils.debug(ScriptExecutor.class, "executeTaskStack 执行脚本任务 (task): " + task + " (remainingCommandsCount): " + remainingCommandsCount);
                return executeScriptTask(taskStack, (ScriptTask) taskStack.peek(), remainingCommandsCount);
            }
            else if (task instanceof ValueTask)
            {
                //LogUtils.debug(ScriptExecutor.class, "executeTaskStack 执行值任务 (task): " + task + " (remainingCommandsCount): " + remainingCommandsCount);
                return executeValueTask(taskStack, (ValueTask) taskStack.peek(), remainingCommandsCount);
            }
            else if (task instanceof TriggerTask)
            {
                //LogUtils.debug(ScriptExecutor.class, "executeTaskStack 执行触发器任务 (task): " + task + " (remainingCommandsCount): " + remainingCommandsCount);
                return executeTriggerTask(taskStack, (TriggerTask) taskStack.peek(), remainingCommandsCount);
            }
            else
            {
                LogUtils.error(ScriptExecutor.class, "executeTaskStack 错误的任务类型 (type): " + task.getType());
                return remainingCommandsCount;
            }
        }
    }

    public void update (float deltaTime)
    {
        remainingCommandsCount = MAX_COMMAND_COUNT_PER_FRAME;

        // 需要上下文
        if (scriptContent == null) return;

        // 无序打乱任务栈列表
        List<TaskStack> taskStackList = new ArrayList<>(taskMap.values());
        Collections.shuffle(taskStackList);

        // 遍历任务栈列表
        for (TaskStack taskStack : taskStackList)
        {
            // 有可用指令数
            if (remainingCommandsCount > 0)
            {
                remainingCommandsCount = executeTaskStack(taskStack, remainingCommandsCount);
            }
            // 无可用指令数
            else
            {
                break;
            }
        }
    }

    public boolean addTask (String taskName, Task task)
    {
        if (taskMap.containsKey(taskName))
        {
            LogUtils.error(ScriptExecutor.class, "addTask 任务已存在 (taskName): " + taskName);
            return false;
        }
        else
        {
            taskMap.put(taskName, new TaskStack());
            taskMap.get(taskName).push(task);
            if (task instanceof TriggerTask) triggerTaskList.add(taskName);
            LogUtils.debug(ScriptExecutor.class, "addTask 添加任务 (taskName): " + taskName + " (taskType): " + task.getType());
            return true;
        }
    }

    public boolean hasTask (String taskName)
    {
        return taskMap.containsKey(taskName);
    }

    public boolean isTaskFinished (String taskName)
    {
        if (taskMap.containsKey(taskName))
        {
            return taskMap.get(taskName).isEmpty();
        }
        else
        {
            LogUtils.error(ScriptExecutor.class, "isTaskFinished 错误任务名 (taskName): " + taskName);
            return false;
        }
    }

    public boolean removeTask (String taskName)
    {
        if (taskMap.containsKey(taskName))
        {
            taskMap.remove(taskName);
            triggerTaskList.remove(taskName);
            LogUtils.debug(ScriptExecutor.class, "removeTask 移除任务 (taskName): " + taskName);
            return true;
        }
        else
        {
            LogUtils.error(ScriptExecutor.class, "removeTask 任务不存在 (taskName): " + taskName);
            return false;
        }
    }

    public TaskStack getTask (String taskName)
    {
        if (taskMap.containsKey(taskName))
        {
            return taskMap.get(taskName);
        }
        else
        {
            LogUtils.error(ScriptExecutor.class, "getTask 任务不存在 (taskName): " + taskName);
            return null;
        }
    }

    public List<String> getTriggerTaskList ()
    {
        return triggerTaskList;
    }

    public void removeTriggerTask ()
    {
        for (String taskName : new ArrayList<>(triggerTaskList))
        {
            removeTask(taskName);
        }
        LogUtils.debug(ScriptExecutor.class, "removeTriggerTask 移除所有触发任务");
    }

    // ===================================================================================================================

    private Map<String, Object> parseArguments (List<ArgumentInfo> arguments)
    {
        Map<String, Object> argumentsMap = new HashMap<>();
        for (ArgumentInfo argumentInfo : arguments)
        {
            switch (argumentInfo.getType())
            {
                case CONST:
                    argumentsMap.put(argumentInfo.getArgumentName(), argumentInfo.getValue());
                    break;
                case VARIABLE:
                    argumentsMap.put(argumentInfo.getArgumentName(), scriptContent.getGameVariableManager().getVariable(argumentInfo.getName()));
                    break;
                case GAME_VARIABLE:
                    argumentsMap.put(argumentInfo.getArgumentName(), scriptContent.getGameInfoManager().getInfo(argumentInfo.getName()));
                    break;

                default:
                    LogUtils.error(ScriptExecutor.class, "parseArguments 错误的参数类型 (type): " + argumentInfo.getType());
                    break;
            }
        }
        return argumentsMap;
    }

    private void returnParentTaskValue (Task parentTask, Object value)
    {
        if (parentTask != null && !parentTask.isReturnValueSet())
        {
            parentTask.setReturnValue(value);
        }
    }

    private void returnParentTaskDefaultReturnValue (Task parentTask, Task task)
    {
        if (parentTask != null && task != null && !parentTask.isReturnValueSet())
        {
            parentTask.setReturnValue(task.getDefaultReturnValue());
        }
    }

    private void synchronizeParentTaskScopeVariables (Task parentTask, Task task)
    {
        if (parentTask instanceof ScriptTask && task instanceof ScriptTask)
        {
            // 任务作用域继承于父任务作用域
            if (task.getType() == TaskType.COMMAND_NORMAL || task.getType() == TaskType.COMMAND_WHILE)
            {
                // 同步作用域
                ((ScriptTask) parentTask).setScopeVariables(((ScriptTask) task).getScopeVariables());
            }
        }
    }

    // ===================================================================================================================

    private int executeIfControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 委派过子任务
        if (task.isReturnValueSet())
        {
            // 获取条件结果
            boolean conditionResult = (boolean) task.consumeReturnValue();

            // 创建子任务脚本
            IfControlScriptCommandParam param = (IfControlScriptCommandParam) command.getCommandParam();
            List<ScriptCommand> subCommands = conditionResult ? param.getThenCommands() : param.getElseCommands();

            // 优化空分支
            if (subCommands.isEmpty())
            {
                LogUtils.debug(ScriptExecutor.class, "executeIfControlScriptCommand "
                    + (conditionResult ? "then" : "else") + "分支为空，跳过");
                task.nextCommand();
            }
            else
            {
                LogUtils.debug(ScriptExecutor.class, "executeIfControlScriptCommand "
                    + (conditionResult ? "then" : "else") + "分支，添加子任务");

                // 创建子任务
                ScriptTask subTask = new ScriptTask(TaskType.COMMAND_NORMAL, task, subCommands, 0, task.getScopeVariables());

                // 添加子任务
                taskStack.push(subTask);

                // 完成if结构
                task.nextCommand();

            }
        }
        // 未委派过子任务
        else
        {
            // 获取条件表达式
            IfControlScriptCommandParam param = (IfControlScriptCommandParam) command.getCommandParam();
            LogicValue condition = param.getCondition();

            LogUtils.debug(ScriptExecutor.class, "executeIfControlScriptCommand 创建条件求值任务");

            // 创建子求值任务
            ValueTask valueTask = new ValueTask(TaskType.VALUE_LOGIC, task, condition.getExpression(), false);

            // 添加子求值任务
            task.enableReturnValue();
            taskStack.push(valueTask);
        }
        return 1;
    }

    private int executeWhileControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 委派过子任务
        if (task.isReturnValueSet())
        {
            // 获取条件结果
            boolean conditionResult = (boolean) task.consumeReturnValue();

            // 条件为真
            if (conditionResult)
            {
                // 创建子任务脚本
                List<ScriptCommand> subCommands = ((WhileControlScriptCommandParam) command.getCommandParam()).getCommands();

                LogUtils.debug(ScriptExecutor.class, "executeWhileControlScriptCommand 创建循环任务");

                // 创建子任务
                ScriptTask subTask = new ScriptTask(TaskType.COMMAND_WHILE, task, subCommands, 0, task.getScopeVariables());

                // 添加子任务
                taskStack.push(subTask);
            }
            // 条件为假
            else
            {
                LogUtils.debug(ScriptExecutor.class, "executeWhileControlScriptCommand 表达式为假，跳出循环");

                // 不进入循环
                task.nextCommand();
            }
        }
        // 未委派过子任务
        else
        {
            // 获取条件表达式
            WhileControlScriptCommandParam param = (WhileControlScriptCommandParam) command.getCommandParam();
            LogicValue condition = param.getCondition();

            LogUtils.debug(ScriptExecutor.class, "executeWhileControlScriptCommand 创建条件求值任务");

            // 创建子任务
            ValueTask valueTask = new ValueTask(TaskType.VALUE_LOGIC, task, condition.getExpression(), false);

            // 添加子任务
            task.enableReturnValue();
            taskStack.push(valueTask);

        }
        return 1;
    }

    private int executeBreakControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 判断当前是否在循环中
        if (task.getType() == TaskType.COMMAND_WHILE)
        {
            // 同步作用域
            synchronizeParentTaskScopeVariables(task.getParentTask(), task);

            // 跳出循环
            LogUtils.debug(ScriptExecutor.class, "executeBreakControlScriptCommand 跳出循环");
            task.forceFinish();
            taskStack.pop();

            // 父任务完成while语句
            task.getParentTask().nextCommand();
        }
        // 不在循环中 忽略指令
        else
        {
            LogUtils.debug(ScriptExecutor.class, "executeBreakControlScriptCommand 不在循环中，忽略指令");

            task.nextCommand();
        }
        return 1;
    }

    private int executeContinueControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 判断当前是否在循环中
        if (task.getType() == TaskType.COMMAND_WHILE)
        {
            // 同步作用域
            synchronizeParentTaskScopeVariables(task.getParentTask(), task);

            // 强制结束这一轮
            LogUtils.debug(ScriptExecutor.class, "executeContinueControlScriptCommand 强制结束这一轮");
            task.forceFinish();
            taskStack.pop();

        }
        // 不在循环中 忽略指令
        else
        {
            LogUtils.debug(ScriptExecutor.class, "executeContinueControlScriptCommand 不在循环中，忽略指令");
            task.nextCommand();
        }
        return 1;
    }

    private int executeReturnControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 委派过子任务
        if (task.isReturnValueSet())
        {
            // 获取返回值
            Object returnValue = task.consumeReturnValue();

            // 尝试设置返回值
            LogUtils.debug(ScriptExecutor.class, "executeReturnControlScriptCommand 设置返回值，完成任务");
            returnParentTaskValue(task.getParentTask(), returnValue);

            // 完成任务
            task.forceFinish();
            taskStack.pop();

        }
        // 未委派过子任务
        else
        {
            // 解析返回值参数
            ReturnControlScriptCommandParam param = (ReturnControlScriptCommandParam) command.getCommandParam();
            MathValue returnValue = param.getValue();

            LogUtils.debug(ScriptExecutor.class, "executeReturnControlScriptCommand 创建返回值求值任务");

            // 创建子任务
            ValueTask valueTask = new ValueTask(TaskType.VALUE_MATH, task, returnValue.getExpression(), 0);

            // 添加子任务
            task.enableReturnValue();
            taskStack.push(valueTask);

        }
        return 1;
    }

    private int executeWaitControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 检查是否已经暂挂
        if (taskStack.isSuspended())
        {
            // 判断暂挂类型
            if (taskStack.getSuspendReason() == TaskStack.SuspendReason.WAIT)
            {
                // 解析参数
                WaitControlScriptCommandParam param = (WaitControlScriptCommandParam) command.getCommandParam();
                double time = param.getTime();

                // 判断等待时间是否足够
                double waitTime = System.currentTimeMillis() / 1000.0 - taskStack.getSuspendTime();

                // 判断
                if (waitTime >= time)
                {
                    LogUtils.debug(ScriptExecutor.class, "executeWaitControlScriptCommand 计时到，恢复任务");

                    // 恢复任务
                    taskStack.resume();

                    // 跳过当前任务
                    task.nextCommand();

                }
            }
        }
        else
        {
            LogUtils.debug(ScriptExecutor.class, "executeWaitControlScriptCommand 暂挂任务栈");

            // 直接暂挂任务栈
            taskStack.suspend(TaskStack.SuspendReason.WAIT);
        }
        return 1;
    }

    private int executeCallControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 解析脚本
        CallControlScriptCommandParam param = (CallControlScriptCommandParam) command.getCommandParam();

        // 脚本文件
        String scriptFileName = param.getScript();

        // 解析脚本
        Script script = scriptContent.getGameScriptManager().getScript(scriptFileName);

        // 参数列表
        List<ArgumentInfo> arguments = param.getArguments();

        if (script == null)
        {
            LogUtils.error(ScriptExecutor.class, "executeCallControlScriptCommand 脚本文件不存在 (file): " + scriptFileName);
        }
        else
        {
            LogUtils.debug(ScriptExecutor.class, "executeCallControlScriptCommand 创建脚本任务");

            // 解析参数 创建子任务
            ScriptTask subTask = new ScriptTask(TaskType.COMMAND_CALL, task, script.getCommands(), script.getReturnValue().getDefaultValue(), parseArguments(arguments));

            // 添加子任务
            taskStack.push(subTask);

        }

        // 完成委派(或者委派失败)
        task.nextCommand();

        return 1;
    }

    private int executeControlScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        switch (command.getCommandAction())
        {
            case IF:
                return executeIfControlScriptCommand(taskStack, task, command);

            case WHILE:
                return executeWhileControlScriptCommand(taskStack, task, command);

            case BREAK:
                return executeBreakControlScriptCommand(taskStack, task, command);

            case CONTINUE:
                return executeContinueControlScriptCommand(taskStack, task, command);

            case RETURN:
                return executeReturnControlScriptCommand(taskStack, task, command);

            case WAIT:
                return executeWaitControlScriptCommand(taskStack, task, command);

            case CALL:
                return executeCallControlScriptCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeControlCommand Control类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeCreateVariableScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 委派过子任务
        if (task.isReturnValueSet())
        {
            // 获取参数名
            CreateVariableScriptCommandParam param = (CreateVariableScriptCommandParam) command.getCommandParam();
            String variableName = param.getName();

            // 获取返回值
            Object variableValue = task.consumeReturnValue();

            // 已经存在变量警告
            if (scriptContent.getGameVariableManager().hasVariable(variableName))
            {
                LogUtils.debug(ScriptExecutor.class, "executeCreateVariableScriptCommand 已存在的变量 (name): " + variableName);
            }

            // 赋值变量
            scriptContent.getGameVariableManager().setVariable(variableName, variableValue);

            // 完成任务
            task.nextCommand();

            LogUtils.debug(ScriptExecutor.class, "executeCreateVariableScriptCommand 创建变量 (name): " + variableName + " (value): " + variableValue);
        }
        // 未委派过子任务
        else
        {
            // 解析参数
            CreateVariableScriptCommandParam param = (CreateVariableScriptCommandParam) command.getCommandParam();
            MathValue value = param.getValue();

            LogUtils.debug(ScriptExecutor.class, "executeCreateVariableScriptCommand 创建创建变量求默认值");

            // 创建子任务
            ValueTask valueTask = new ValueTask(TaskType.VALUE_MATH, task, value.getExpression(), 0);

            // 添加子任务
            task.enableReturnValue();
            taskStack.push(valueTask);

        }
        return 1;
    }

    private int executeAssignVariableScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 委派过子任务
        if (task.isReturnValueSet())
        {
            // 获取参数名
            AssignmentVariableScriptCommandParam param = (AssignmentVariableScriptCommandParam) command.getCommandParam();
            String variableName = param.getName();

            // 获取返回值
            Object variableValue = task.consumeReturnValue();

            // 赋值变量
            LogUtils.debug(ScriptExecutor.class, "executeAssignVariableScriptCommand 赋值变量 (name): " + variableName + " (value): " + variableValue);
            scriptContent.getGameVariableManager().setVariable(variableName, variableValue);

            // 下一条指令
            task.nextCommand();

        }
        // 未委派过子任务
        else
        {
            // 解析参数
            AssignmentVariableScriptCommandParam param = (AssignmentVariableScriptCommandParam) command.getCommandParam();
            MathValue value = param.getValue();

            LogUtils.debug(ScriptExecutor.class, "executeAssignVariableScriptCommand 创建赋值变量求值任务");

            // 创建子任务
            ValueTask valueTask = new ValueTask(TaskType.VALUE_MATH, task, value.getExpression(), 0);

            // 添加子任务
            task.enableReturnValue();
            taskStack.push(valueTask);

        }
        return 1;
    }

    private int executeVariableScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        switch (command.getCommandAction())
        {
            case CREATE:
                return executeCreateVariableScriptCommand(taskStack, task, command);

            case ASSIGNMENT:
                return executeAssignVariableScriptCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeVariableScriptCommand Variable类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeForwardPageScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        LogUtils.debug(ScriptExecutor.class, "executeForwardPageScriptCommand 前进一页");
        scriptContent.getGameSessionManager().storyForwardPage();
        task.nextCommand();
        return 1;
    }

    private int executeGotoPageScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        // 解析参数
        GotoPageStoryScriptCommandParam param = (GotoPageStoryScriptCommandParam) command.getCommandParam();
        JsonEntity treeInfoJson = param.getTree();
        TreeStructureInfo treeInfo = new TreeStructureInfo(treeInfoJson);
        String pageId = param.getPage();

        // 跳转页面
        LogUtils.debug(ScriptExecutor.class, "executeGotoPageScriptCommand 跳转页面 (treeInfo): " + treeInfo + " (pageId): " + pageId);
        scriptContent.getGameSessionManager().storyGotoPage(treeInfo, pageId);

        task.nextCommand();
        return 1;
    }

    private int executeStoryScriptCommand (TaskStack taskStack, ScriptTask task, ScriptCommand command)
    {
        switch (command.getCommandAction())
        {
            case FORWARD_PAGE:
                return executeForwardPageScriptCommand(taskStack, task, command);

            case GOTO_PAGE:
                return executeGotoPageScriptCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeStoryScriptCommand Story类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeAddMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeAddMathValueCommand 推入加法操作符");
        task.pushAction(ValueCommandAction.ADD);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeSubMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeSubMathValueCommand 推入减法操作符");
        task.pushAction(ValueCommandAction.SUB);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeMulMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeMulMathValueCommand 推入乘法操作符");
        task.pushAction(ValueCommandAction.MUL);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeDivMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeDivMathValueCommand 推入除法操作符");
        task.pushAction(ValueCommandAction.DIV);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeNegMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeNegMathValueCommand 推入取负操作符");
        task.pushAction(ValueCommandAction.NEG);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeRandomMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeRandomMathValueCommand 推入随机数操作符");
        task.pushAction(ValueCommandAction.RANDOM);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeMathValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        switch (command.getCommandAction())
        {
            case ADD:
                return executeAddMathValueCommand(taskStack, task, command);

            case SUB:
                return executeSubMathValueCommand(taskStack, task, command);

            case MUL:
                return executeMulMathValueCommand(taskStack, task, command);

            case DIV:
                return executeDivMathValueCommand(taskStack, task, command);

            case NEG:
                return executeNegMathValueCommand(taskStack, task, command);

            case RANDOM:
                return executeRandomMathValueCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeMathCommand Math类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeEqualCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeEqualCompareValueCommand 推入相等比较操作符");
        task.pushAction(ValueCommandAction.EQUAL);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeNotEqualCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeNotEqualCompareValueCommand 推入不相等比较操作符");
        task.pushAction(ValueCommandAction.NOT_EQUAL);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeGreaterCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeGreaterCompareValueCommand 推入大于比较操作符");
        task.pushAction(ValueCommandAction.GREATER);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeLessCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeLessCompareValueCommand 推入小于比较操作符");
        task.pushAction(ValueCommandAction.LESS);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeGreaterEqualCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeGreaterEqualCompareValueCommand 推入大于等于比较操作符");
        task.pushAction(ValueCommandAction.GREATER_EQUAL);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeLessEqualCompareValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeLessEqualCompareValueCommand 推入小于等于比较操作符");
        task.pushAction(ValueCommandAction.LESS_EQUAL);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeCompareValueValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        switch (command.getCommandAction())
        {
            case EQUAL:
                return executeEqualCompareValueCommand(taskStack, task, command);

            case NOT_EQUAL:
                return executeNotEqualCompareValueCommand(taskStack, task, command);

            case GREATER:
                return executeGreaterCompareValueCommand(taskStack, task, command);

            case LESS:
                return executeLessCompareValueCommand(taskStack, task, command);

            case GREATER_EQUAL:
                return executeGreaterEqualCompareValueCommand(taskStack, task, command);

            case LESS_EQUAL:
                return executeLessEqualCompareValueCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeCompareCommand Compare类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeAndLogicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeAndLogicValueCommand 推入逻辑与操作符");
        task.pushAction(ValueCommandAction.AND);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeOrLogicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeOrLogicValueCommand 推入逻辑或操作符");
        task.pushAction(ValueCommandAction.OR);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeNotLogicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeNotLogicValueCommand 推入逻辑非操作符");
        task.pushAction(ValueCommandAction.NOT);

        // 下一条指令，等待参数
        task.nextCommand();

        return 1;
    }

    private int executeLogicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        switch (command.getCommandAction())
        {
            case AND:
                return executeAndLogicValueCommand(taskStack, task, command);

            case OR:
                return executeOrLogicValueCommand(taskStack, task, command);

            case NOT:
                return executeNotLogicValueCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeLogicCommand Logic类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private float parseToFloat (Object value)
    {
        if (value instanceof String)
        {
            return (float) ((String) value).length();
        }
        else if (value instanceof Boolean)
        {
            return ((boolean) value) ? 1.0f : 0.0f;
        }
        else
        {
            return (float) value;
        }
    }

    private boolean parseToBoolean (Object value)
    {
        if (value instanceof String)
        {
            return !((String) value).isEmpty();
        }
        else if (value instanceof Boolean)
        {
            return (boolean) value;
        }
        else
        {
            return (float) value != 0.0f;
        }
    }

    private Object calculateAddValueCommand (List<Object> params)
    {
        // 判断参数类型
        if (params.get(0) instanceof String && params.get(1) instanceof String)
        {
            String string1 = (String) params.get(0);
            String string2 = (String) params.get(1);
            return string1 + string2;
        }
        else
        {
            float float1 = parseToFloat(params.get(0));
            float float2 = parseToFloat(params.get(1));
            return float1 + float2;
        }
    }

    private Object calculateSubValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return float1 - float2;
    }

    private Object calculateMulValueCommand (List<Object> params)
    {
        if (params.get(0) instanceof String)
        {
            String string = (String) params.get(0);
            int repeatCount = (int) parseToFloat(params.get(1));

            StringBuilder sb = new StringBuilder(string.length() * repeatCount);
            for (int i = 0; i < repeatCount; i++)
            {
                sb.append(string);
            }
            return sb.toString();
        }
        else
        {
            float float1 = parseToFloat(params.get(0));
            float float2 = parseToFloat(params.get(1));
            return float1 * float2;
        }
    }

    private Object calculateDivValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        if (float2 == 0.0f)
        {
            return 0.0f;
        }
        else
        {
            return float1 / float2;
        }
    }

    private Object calculateNegValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        return -float1;
    }

    private Object calculateRandomValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return MathUtils.random(float1, float2);
    }

    private Object calculateEqualValueCommand (List<Object> params)
    {
        if (params.get(0) instanceof String && params.get(1) instanceof String)
        {
            String string1 = (String) params.get(0);
            String string2 = (String) params.get(1);
            return string1.equals(string2);
        }
        else if (params.get(0) instanceof Boolean && params.get(1) instanceof Boolean)
        {
            boolean boolean1 = parseToBoolean(params.get(0));
            boolean boolean2 = parseToBoolean(params.get(1));
            return boolean1 == boolean2;
        }
        else
        {
            float float1 = parseToFloat(params.get(0));
            float float2 = parseToFloat(params.get(1));
            return float1 == float2;
        }
    }

    private Object calculateNotEqualValueCommand (List<Object> params)
    {
        if (params.get(0) instanceof String && params.get(1) instanceof String)
        {
            String string1 = (String) params.get(0);
            String string2 = (String) params.get(1);
            return !string1.equals(string2);
        }
        else if (params.get(0) instanceof Boolean && params.get(1) instanceof Boolean)
        {
            boolean boolean1 = parseToBoolean(params.get(0));
            boolean boolean2 = parseToBoolean(params.get(1));
            return boolean1 != boolean2;
        }
        else
        {
            float float1 = parseToFloat(params.get(0));
            float float2 = parseToFloat(params.get(1));
            return float1 != float2;
        }
    }

    private Object calculateGreaterValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return float1 > float2;
    }

    private Object calculateLessValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return float1 < float2;
    }

    private Object calculateGreaterEqualValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return float1 >= float2;
    }

    private Object calculateLessEqualValueCommand (List<Object> params)
    {
        float float1 = parseToFloat(params.get(0));
        float float2 = parseToFloat(params.get(1));
        return float1 <= float2;
    }

    private Object calculateAndValueCommand (List<Object> params)
    {
        boolean boolean1 = parseToBoolean(params.get(0));
        boolean boolean2 = parseToBoolean(params.get(1));
        return boolean1 && boolean2;
    }

    private Object calculateOrValueCommand (List<Object> params)
    {
        boolean boolean1 = parseToBoolean(params.get(0));
        boolean boolean2 = parseToBoolean(params.get(1));
        return boolean1 || boolean2;
    }

    private Object calculateNotValueCommand (List<Object> params)
    {
        boolean boolean1 = parseToBoolean(params.get(0));
        return !boolean1;
    }

    private void calculateValueTask (ValueTask task)
    {
        // 如果任务栈准备就绪 开始计算
        if (task.isActionParamReady())
        {
            // 解析运算类型和参数
            ValueCommandAction action = task.getCurrentAction();
            List<Object> params = task.getCurrentActionParams();
            LogUtils.debug(ScriptExecutor.class, "calculateValueTask 参数准备就绪 开始计算"
                + " (action): " + action
                + " (params): " + params);

            // 分发运算类型
            Object result = null;
            switch (action)
            {
                case ADD:
                    result = calculateAddValueCommand(params);
                    break;

                case SUB:
                    result = calculateSubValueCommand(params);
                    break;

                case MUL:
                    result = calculateMulValueCommand(params);
                    break;

                case DIV:
                    result = calculateDivValueCommand(params);
                    break;

                case NEG:
                    result = calculateNegValueCommand(params);
                    break;

                case RANDOM:
                    result = calculateRandomValueCommand(params);
                    break;

                case EQUAL:
                    result = calculateEqualValueCommand(params);
                    break;

                case NOT_EQUAL:
                    result = calculateNotEqualValueCommand(params);
                    break;

                case GREATER:
                    result = calculateGreaterValueCommand(params);
                    break;

                case LESS:
                    result = calculateLessValueCommand(params);
                    break;

                case GREATER_EQUAL:
                    result = calculateGreaterEqualValueCommand(params);
                    break;

                case LESS_EQUAL:
                    result = calculateLessEqualValueCommand(params);
                    break;

                case AND:
                    result = calculateAndValueCommand(params);
                    break;

                case OR:
                    result = calculateOrValueCommand(params);
                    break;

                case NOT:
                    result = calculateNotValueCommand(params);
                    break;

                default:
                    LogUtils.debug(ScriptExecutor.class, "calculateValueTask 存入了错误的指令 (action): " + action);
                    return;
            }

            LogUtils.debug(ScriptExecutor.class, "calculateValueTask 计算结果: " + result);

            // 结束这一层计算栈
            task.popAction();

            // 解析计算结果
            if (task.getType() == TaskType.VALUE_MATH)
            {
                tryPushValueToValueTask(task, parseToFloat(result));
            }
            else if (task.getType() == TaskType.VALUE_LOGIC)
            {
                tryPushValueToValueTask(task, parseToBoolean(result));
            }
            else
            {
                LogUtils.debug(ScriptExecutor.class, "calculateValueTask 存入了错误的任务类型 (taskType): " + task.getType());
                return;
            }
        }
    }

    private boolean tryPushValueToValueTask (ValueTask task, Object value)
    {
        // 如果栈为空，直接返回值
        if (task.actionStackEmpty())
        {
            returnParentTaskValue(task.getParentTask(), value);
            task.forceFinish();
            return false;
        }
        else
        {
            // 否则，将值推入栈顶参数列表
            task.pushActionParam(value);
            return true;
        }
    }

    private void pushValueToValueTask (ValueTask task, Object value)
    {
        // 推入操作栈成功 而不是设置返回值
        if (tryPushValueToValueTask(task, value))
        {
            // 判断参与计算
            calculateValueTask(task);
        }
    }

    private int executeConstAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 解析参数
        ConstAtomicValueCommandParam param = (ConstAtomicValueCommandParam) command.getCommandParam();
        Object value = param.getValue();

        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeConstAtomicValueCommand 推入常量值 (value): " + value);
        pushValueToValueTask(task, value);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeVariableAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 解析参数
        VariableAtomicValueCommandParam param = (VariableAtomicValueCommandParam) command.getCommandParam();
        String variableName = param.getVariableName();
        Object value = scriptContent.getGameVariableManager().getVariable(variableName);

        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeVariableAtomicValueCommand 推入变量值 (variableName): " + variableName + " (value): " + value);
        pushValueToValueTask(task, value);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeScopeVariableAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 解析参数
        ScopeVariableAtomicValueCommandParam param = (ScopeVariableAtomicValueCommandParam) command.getCommandParam();
        String scopeVariableName = param.getScopeVariableName();

        // 从父任务中获取变量
        Task parentTask = task;
        do
        {
            parentTask = parentTask.getParentTask();
            if (parentTask == null)
            {
                LogUtils.debug(ScriptExecutor.class, "executeScopeVariableAtomicValueCommand 找不到父任务 无法解析作用域变量 (scopeVariableName): " + scopeVariableName);
                return 0;
            }
        } while (!(parentTask instanceof ScriptTask));
        Object value = ((ScriptTask) parentTask).getScopeVariables().get(scopeVariableName);

        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeScopeVariableAtomicValueCommand 推入作用域变量值 (scopeVariableName): " + scopeVariableName + " (value): " + value);
        pushValueToValueTask(task, value);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeGameVariableAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 解析参数
        GameVariableAtomicValueCommandParam param = (GameVariableAtomicValueCommandParam) command.getCommandParam();
        String gameVariableKey = param.getGameVariableKey();
        Object value = scriptContent.getGameInfoManager().getInfo(gameVariableKey);

        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeGameVariableAtomicValueCommand 推入游戏变量值 (gameVariableKey): " + gameVariableKey + " (value): " + value);
        pushValueToValueTask(task, value);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeTrueAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeTrueAtomicValueCommand 推入真值");
        pushValueToValueTask(task, true);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeFalseAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 推入操作栈
        LogUtils.debug(ScriptExecutor.class, "executeFalseAtomicValueCommand 推入假值");
        pushValueToValueTask(task, false);

        // 下一条指令
        task.nextCommand();

        return 1;
    }

    private int executeCallAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        // 委托过子任务
        if (task.isReturnValueSet())
        {
            // 解析返回值
            Object returnValue = task.consumeReturnValue();

            // 推入操作栈
            LogUtils.debug(ScriptExecutor.class, "executeCallAtomicValueCommand 推入子任务返回值 (returnValue): " + returnValue);
            pushValueToValueTask(task, returnValue);

            // 下一条指令
            task.nextCommand();

        }
        // 未委托过子任务
        else
        {
            // 解析参数
            CallAtomicValueCommandParam param = (CallAtomicValueCommandParam) command.getCommandParam();
            String scriptName = param.getScript();
            Script script = scriptContent.getGameScriptManager().getScript(scriptName);
            List<ArgumentInfo> arguments = param.getArguments();

            if (script == null)
            {
                LogUtils.debug(ScriptExecutor.class, "executeCallAtomicValueCommand 找不到脚本 (scriptName): " + scriptName);

                // 推入操作栈
                LogUtils.debug(ScriptExecutor.class, "executeCallAtomicValueCommand 推入0值");
                pushValueToValueTask(task, 0);

                // 下一条指令
                task.nextCommand();
            }
            else
            {
                LogUtils.debug(ScriptExecutor.class, "executeCallAtomicValueCommand 创建子任务 (scriptName): " + scriptName);

                // 解析参数 创建子任务
                ScriptTask subTask = new ScriptTask(TaskType.COMMAND_CALL, task, script.getCommands(), 0, parseArguments(arguments));

                // 执行子任务
                task.enableReturnValue();
                taskStack.push(subTask);

            }
        }
        return 1;
    }

    private int executeAtomicValueCommand (TaskStack taskStack, ValueTask task, ValueCommand command)
    {
        switch (command.getCommandAction())
        {
            case CONST:
                return executeConstAtomicValueCommand(taskStack, task, command);

            case VARIABLE:
                return executeVariableAtomicValueCommand(taskStack, task, command);

            case SCOPE_VARIABLE:
                return executeScopeVariableAtomicValueCommand(taskStack, task, command);

            case GAME_VARIABLE:
                return executeGameVariableAtomicValueCommand(taskStack, task, command);

            case TRUE:
                return executeTrueAtomicValueCommand(taskStack, task, command);

            case FALSE:
                return executeFalseAtomicValueCommand(taskStack, task, command);

            case CALL:
                return executeCallAtomicValueCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeAtomicCommand Atomic类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    private int executeLabelClickTriggerCommand (TaskStack taskStack, TriggerTask task, TriggerCommand command)
    {
        // 解析参数
        LabelClickTriggerParam param = (LabelClickTriggerParam) command.getCommandParam();
        String tag = param.getTag();

        // 判断标签点击
        if (scriptContent.getUiManager().isLabelClicked(tag))
        {
            LogUtils.debug(ScriptExecutor.class, "executeLabelClickTriggerCommand 标签点击 (tag): " + tag + " 创建子任务");

            // 创建子任务
            ScriptTask subTask = new ScriptTask(TaskType.COMMAND_NORMAL, task, task.getTrigger().getCommands(), 0, new HashMap<>());

            // 执行子任务
            taskStack.push(subTask);

        }

        return 0;
    }

    private int executeLabelTriggerCommand (TaskStack taskStack, TriggerTask task, TriggerCommand command)
    {
        switch (command.getCommandAction())
        {
            case LABEL_CLICK:
                return executeLabelClickTriggerCommand(taskStack, task, command);

            default:
                LogUtils.debug(ScriptExecutor.class, "executeTriggerCommand Trigger类型中的未知指令 (commandAction): " + command.getCommandAction());
                return 0;
        }
    }

    // ===================================================================================================================

    public boolean dispose ()
    {
        try
        {
            taskMap.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ScriptExecutor.class, "dispose 释放脚本执行器失败", e);
            return false;
        }
    }
}
