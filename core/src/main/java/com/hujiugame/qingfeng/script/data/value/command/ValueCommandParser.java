package com.hujiugame.qingfeng.script.data.value.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.action.*;
import com.hujiugame.qingfeng.script.data.value.command.param.atomic.*;
import com.hujiugame.qingfeng.script.data.value.command.param.compare.*;
import com.hujiugame.qingfeng.script.data.value.command.param.logic.AndLogicValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.logic.NotLogicValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.logic.OrLogicValueCommandParam;
import com.hujiugame.qingfeng.script.data.value.command.param.math.*;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class ValueCommandParser
{
    /**
     * 解析单个 ValueCommand JSON
     *
     * @param json JSON 对象
     * @return 解析成功的 ValueCommand，失败返回 null
     */
    public static ValueCommand parse (JsonEntity json)
    {
        if (json == null || !json.isMap())
        {
            LogUtils.error(ValueCommandParser.class, "parse 非法的 JSON (json): " + json);
            return null;
        }

        if (!json.containsKey("type"))
        {
            LogUtils.error(ValueCommandParser.class, "parse 缺少 type 字段 (json): " + json);
            return null;
        }
        if (!json.containsKey("action"))
        {
            LogUtils.error(ValueCommandParser.class, "parse 缺少 action 字段 (json): " + json);
            return null;
        }

        String typeStr = json.getString("type");
        String actionStr = json.getString("action");
        JsonEntity paramJson = json.containsKey("param") ? json.getJsonEntityByKey("param") : new JsonEntity();

        ValueCommandType type = ValueCommandType.fromString(typeStr);
        ValueCommandAction action = ValueCommandAction.fromString(actionStr);

        if (type == null)
        {
            LogUtils.error(ValueCommandParser.class, "parse 未知命令类型 (type): " + typeStr);
            return null;
        }
        if (action == null)
        {
            LogUtils.error(ValueCommandParser.class, "parse 未知命令动作 (action): " + actionStr);
            return null;
        }

        // 检查 type 和 action 是否匹配
        if (action.getCommandType() != type)
        {
            LogUtils.error(ValueCommandParser.class, "parse type/action 不匹配 (type): " + typeStr + " (action): " + actionStr);
            return null;
        }

        try
        {
            ValueCommand command = dispatchCommand(action, paramJson);
            if (command != null && command.isValid())
            {
                LogUtils.debug(ValueCommandParser.class, "parse 解析 ValueCommand 成功"
                    + " (type): " + type + " (action): " + action
                    + " (param): " + command.getCommandParam().getJson());
                return command;
            }
            else
            {
                LogUtils.error(ValueCommandParser.class, "parse 解析 ValueCommand 失败 (json): " + json);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ValueCommandParser.class, "parse 异常 (json): " + json, e);
            return null;
        }
    }

    /**
     * 批量解析 ValueCommand 列表
     *
     * @param jsonList JSON 对象列表
     * @return 解析成功的命令列表，任一失败返回 null
     */
    public static List<ValueCommand> parseList (List<JsonEntity> jsonList)
    {
        if (jsonList == null || jsonList.isEmpty())
        {
            return new ArrayList<>(0);
        }

        List<ValueCommand> result = new ArrayList<>(jsonList.size());
        for (int i = 0; i < jsonList.size(); i++)
        {
            ValueCommand cmd = parse(jsonList.get(i));
            if (cmd == null)
            {
                LogUtils.error(ValueCommandParser.class, "parseList 第" + i + " 个命令解析失败");
                return null;
            }
            result.add(cmd);
        }
        return result;
    }

    // ===================================================================================================================

    private static ValueCommand dispatchCommand (ValueCommandAction action, JsonEntity paramJson)
    {
        switch (action.getCommandType())
        {
            case ATOMIC:
                return parseAtomic(action, paramJson);
            case MATH:
                return parseMath(action, paramJson);
            case COMPARE:
                return parseCompare(action, paramJson);
            case LOGIC:
                return parseLogic(action, paramJson);
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------
    // 原子指令
    // -------------------------------------------------------------------------------------------------------------------

    private static ValueCommand parseAtomic (ValueCommandAction action, JsonEntity paramJson)
    {
        switch (action)
        {
            case CONST:
                return new AtomicValueCommand(action, new ConstAtomicValueCommandParam(paramJson));
            case VARIABLE:
                return new AtomicValueCommand(action, new VariableAtomicValueCommandParam(paramJson));
            case SCOPE_VARIABLE:
                return new AtomicValueCommand(action, new ScopeVariableAtomicValueCommandParam(paramJson));
            case GAME_VARIABLE:
                return new AtomicValueCommand(action, new GameVariableAtomicValueCommandParam(paramJson));
            case TRUE:
                return new AtomicValueCommand(action, new TrueAtomicValueCommandParam(paramJson));
            case FALSE:
                return new AtomicValueCommand(action, new FalseAtomicValueCommandParam(paramJson));
            case CALL:
                return new AtomicValueCommand(action, new CallAtomicValueCommandParam(paramJson));
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------
    // 数学指令
    // -------------------------------------------------------------------------------------------------------------------

    private static ValueCommand parseMath (ValueCommandAction action, JsonEntity paramJson)
    {
        switch (action)
        {
            case ADD:
                return new MathValueCommand(action, new AddMathValueCommandParam(paramJson));
            case SUB:
                return new MathValueCommand(action, new SubMathValueCommandParam(paramJson));
            case MUL:
                return new MathValueCommand(action, new MulMathValueCommandParam(paramJson));
            case DIV:
                return new MathValueCommand(action, new DivMathValueCommandParam(paramJson));
            case NEG:
                return new MathValueCommand(action, new NegMathValueCommandParam(paramJson));
            case RANDOM:
                return new MathValueCommand(action, new RandomMathValueCommandParam(paramJson));
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------
    // 比较指令
    // -------------------------------------------------------------------------------------------------------------------

    private static ValueCommand parseCompare (ValueCommandAction action, JsonEntity paramJson)
    {
        switch (action)
        {
            case EQUAL:
                return new CompareValueCommand(action, new EqualCompareValueCommandParam(paramJson));
            case NOT_EQUAL:
                return new CompareValueCommand(action, new NotEqualCompareValueCommandParam(paramJson));
            case GREATER:
                return new CompareValueCommand(action, new GreaterCompareValueCommandParam(paramJson));
            case LESS:
                return new CompareValueCommand(action, new LessCompareValueCommandParam(paramJson));
            case GREATER_EQUAL:
                return new CompareValueCommand(action, new GreaterEqualCompareValueCommandParam(paramJson));
            case LESS_EQUAL:
                return new CompareValueCommand(action, new LessEqualCompareValueCommandParam(paramJson));
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------
    // 逻辑指令
    // -------------------------------------------------------------------------------------------------------------------

    private static ValueCommand parseLogic (ValueCommandAction action, JsonEntity paramJson)
    {
        switch (action)
        {
            case AND:
                return new LogicValueCommand(action, new AndLogicValueCommandParam(paramJson));
            case OR:
                return new LogicValueCommand(action, new OrLogicValueCommandParam(paramJson));
            case NOT:
                return new LogicValueCommand(action, new NotLogicValueCommandParam(paramJson));
            default:
                return null;
        }
    }
}
