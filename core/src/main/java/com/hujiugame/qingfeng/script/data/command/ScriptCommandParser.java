package com.hujiugame.qingfeng.script.data.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.action.ControlScriptCommand;
import com.hujiugame.qingfeng.script.data.command.action.ScriptCommandAction;
import com.hujiugame.qingfeng.script.data.command.action.StoryScriptCommand;
import com.hujiugame.qingfeng.script.data.command.action.VariableScriptCommand;
import com.hujiugame.qingfeng.script.data.command.param.control.BreakControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.CallControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.ContinueControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.IfControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.ReturnControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.WaitControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.control.WhileControlScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.story.ForwardPageStoryScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.story.GotoPageStoryScriptCommandParam;
import com.hujiugame.qingfeng.type.key.ScriptKey;
import com.hujiugame.qingfeng.script.data.command.param.variable.AssignmentVariableScriptCommandParam;
import com.hujiugame.qingfeng.script.data.command.param.variable.CreateVariableScriptCommandParam;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptCommandParser
{
    public static ScriptCommand parse (JsonEntity json)
    {
        if (json.isMap())
        {
            LogUtils.debug(ScriptCommandParser.class, "parse 尝试解析脚本命令 (json): " + json);

            // type字段
            if (!json.containsKey(ScriptKey.Command.TYPE))
            {
                LogUtils.error(ScriptCommandParser.class, "parse 此json缺少" + ScriptKey.Command.TYPE + "字段 (json): " + json);
                return null;
            }
            // action字段
            if (!json.containsKey(ScriptKey.Command.ACTION))
            {
                LogUtils.error(ScriptCommandParser.class, "parse 此json缺少" + ScriptKey.Command.ACTION + "字段 (json): " + json);
                return null;
            }
            // param字段
            if (!json.containsKey(ScriptKey.Command.PARAM))
            {
                LogUtils.error(ScriptCommandParser.class, "parse 此json缺少" + ScriptKey.Command.PARAM + "字段 (json): " + json);
                return null;
            }

            // 获取json数据
            String type = json.getString(ScriptKey.Command.TYPE);
            String action = json.getString(ScriptKey.Command.ACTION);
            JsonEntity paramJson = json.getJsonEntityByKey(ScriptKey.Command.PARAM);

            // 尝试解析脚本命令
            ScriptCommand command = dispatchScriptCommandJson(type, action, paramJson);
            if (command == null)
            {
                LogUtils.error(ScriptCommandParser.class, "parse 解析脚本命令失败 (json): " + json);
                return null;
            }
            else
            {
                if (command.isValid())
                {
                    LogUtils.debug(ScriptCommandParser.class, "parse 解析脚本命令成功"
                        + " (type): " + command.getCommandType() + " (action): " + command.getCommandAction() + " (param): " + command.getCommandParam()
                        + " (json): " + json);
                    return command;
                }
                else
                {
                    LogUtils.error(ScriptCommandParser.class, "parse 解析脚本命令不可用 (command): " + command + " (json): " + json);
                    return null;
                }
            }
        }
        else
        {
            LogUtils.error(ScriptCommandParser.class, "parse 此json不是字典对象 (json): " + json);
            return null;
        }
    }

    public static List<ScriptCommand> parseList (JsonEntity json)
    {
        if (json.isList())
        {
            LogUtils.debug(ScriptCommandParser.class, "parseList 尝试解析脚本命令列表 (json): " + json);
            List<ScriptCommand> commandList = new ArrayList<>();
            List<Object> jsonList = json.getObjectList();
            for (int i = 0; i < jsonList.size(); i++)
            {
                Object jsonObject = jsonList.get(i);
                JsonEntity itemJson;
                if (jsonObject instanceof JsonEntity)
                {
                    itemJson = (JsonEntity) jsonObject;
                }
                else if (jsonObject instanceof Map)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) jsonObject;
                    itemJson = new JsonEntity(map);
                }
                else
                {
                    LogUtils.error(ScriptCommandParser.class, "parseList 第" + i + " 个对象不是json对象 (json): " + json);
                    return null;
                }
                ScriptCommand command = parse(itemJson);
                if (command != null)
                {
                    commandList.add(command);
                    LogUtils.debug(ScriptCommandParser.class, "parseList 第" + i + " 个脚本命令指令解析成功");
                }
                else
                {
                    LogUtils.error(ScriptCommandParser.class, "parseList 第" + i + " 个脚本命令指令解析失败 (json): " + jsonObject);
                    return null;
                }
            }
            LogUtils.debug(ScriptCommandParser.class, "parseList 解析脚本命令列表成功 (commandList): " + commandList);
            return commandList;
        }
        else
        {
            LogUtils.error(ScriptCommandParser.class, "parseList 此json不是列表对象 (json): " + json);
            return null;
        }
    }

    private static ScriptCommand dispatchScriptCommandJson (String type, String action, JsonEntity paramJson)
    {
        switch (type)
        {
            case ScriptCommandType.CONTROL_STRING:
                return parseControlScriptCommand(action, paramJson);

            case ScriptCommandType.VARIABLE_STRING:
                return parseVariableScriptCommand(action, paramJson);

            case ScriptCommandType.STORY_STRING:
                return parseStoryScriptCommand(action, paramJson);

            default:
                LogUtils.error(ScriptCommandParser.class, "dispatchScriptCommandJson 没有对应的脚本命令类型 (type): " + type);
                return null;
        }
    }

    private static ScriptCommand parseControlScriptCommand (String action, JsonEntity paramJson)
    {
        switch (action)
        {
            case ScriptCommandAction.IF_STRING:
                return new ControlScriptCommand(ScriptCommandAction.IF, new IfControlScriptCommandParam(paramJson));

            case ScriptCommandAction.WHILE_STRING:
                return new ControlScriptCommand(ScriptCommandAction.WHILE, new WhileControlScriptCommandParam(paramJson));

            case ScriptCommandAction.BREAK_STRING:
                return new ControlScriptCommand(ScriptCommandAction.BREAK, new BreakControlScriptCommandParam());

            case ScriptCommandAction.CONTINUE_STRING:
                return new ControlScriptCommand(ScriptCommandAction.CONTINUE, new ContinueControlScriptCommandParam());

            case ScriptCommandAction.RETURN_STRING:
                return new ControlScriptCommand(ScriptCommandAction.RETURN, new ReturnControlScriptCommandParam(paramJson));

            case ScriptCommandAction.WAIT_STRING:
                return new ControlScriptCommand(ScriptCommandAction.WAIT, new WaitControlScriptCommandParam(paramJson));

            case ScriptCommandAction.CALL_STRING:
                return new ControlScriptCommand(ScriptCommandAction.CALL, new CallControlScriptCommandParam(paramJson));

            default:
                LogUtils.error(ScriptCommandParser.class, "parseControlScriptCommand control类脚本命令没有对应的脚本命令动作 (action): " + action);
                return null;
        }
    }

    private static ScriptCommand parseVariableScriptCommand (String action, JsonEntity paramJson)
    {
        switch (action)
        {
            case ScriptCommandAction.CREATE_STRING:
                return new VariableScriptCommand(ScriptCommandAction.CREATE, new CreateVariableScriptCommandParam(paramJson));

            case ScriptCommandAction.ASSIGNMENT_STRING:
                return new VariableScriptCommand(ScriptCommandAction.ASSIGNMENT, new AssignmentVariableScriptCommandParam(paramJson));

            default:
                LogUtils.error(ScriptCommandParser.class, "parseVariableScriptCommand variable类脚本命令没有对应的脚本命令动作 (action): " + action);
                return null;
        }
    }

    private static ScriptCommand parseStoryScriptCommand (String action, JsonEntity paramJson)
    {
        switch (action)
        {
            case ScriptCommandAction.FORWARD_PAGE_STRING:
                return new StoryScriptCommand(ScriptCommandAction.FORWARD_PAGE, new ForwardPageStoryScriptCommandParam(paramJson));

            case ScriptCommandAction.GOTO_PAGE_STRING:
                return new StoryScriptCommand(ScriptCommandAction.GOTO_PAGE, new GotoPageStoryScriptCommandParam(paramJson));

            default:
                LogUtils.error(ScriptCommandParser.class, "parseStoryScriptCommand story类脚本命令没有对应的脚本命令动作 (action): " + action);
                return null;
        }
    }
}
