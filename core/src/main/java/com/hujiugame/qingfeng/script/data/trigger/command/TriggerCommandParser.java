package com.hujiugame.qingfeng.script.data.trigger.command;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.trigger.command.action.LabelClickTrigger;
import com.hujiugame.qingfeng.script.data.trigger.command.action.TriggerAction;
import com.hujiugame.qingfeng.script.data.trigger.command.param.label.LabelClickTriggerParam;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class TriggerCommandParser
{
    public static TriggerCommand parse (JsonEntity json)
    {
        if (json.isMap())
        {
            LogUtils.debug(TriggerCommandParser.class, "parse 尝试解析触发器命令 (json): " + json);

            // type字段
            if (!json.containsKey("type"))
            {
                LogUtils.error(TriggerCommandParser.class, "parse 此json缺少type字段 (json): " + json);
                return null;
            }
            // action字段
            if (!json.containsKey("action"))
            {
                LogUtils.error(TriggerCommandParser.class, "parse 此json缺少action字段 (json): " + json);
                return null;
            }
            // param字段
            if (!json.containsKey("param"))
            {
                LogUtils.error(TriggerCommandParser.class, "parse 此json缺少param字段 (json): " + json);
                return null;
            }

            // 获取json数据
            String type = json.getString("type");
            String action = json.getString("action");
            JsonEntity paramJson = json.getJsonEntityByKey("param");

            // 尝试解析触发器命令
            TriggerCommand command = dispatchTriggerCommandJson(type, action, paramJson);
            if (command == null)
            {
                LogUtils.error(TriggerCommandParser.class, "parse 解析触发器命令失败 (json): " + json);
                return null;
            }
            else
            {
                if (command.isValid())
                {
                    LogUtils.debug(TriggerCommandParser.class, "parse 解析触发器命令成功"
                        + " (type): " + command.getCommandType() + " (action): " + command.getCommandAction() + " (param): " + command.getCommandParam()
                        + " (json): " + json);
                    return command;
                }
                else
                {
                    LogUtils.error(TriggerCommandParser.class, "parse 解析触发器命令不可用 (command): " + command + " (json): " + json);
                    return null;
                }
            }
        }
        else
        {
            LogUtils.error(TriggerCommandParser.class, "parse 此json不是字典对象 (json): " + json);
            return null;
        }
    }

    private static TriggerCommand dispatchTriggerCommandJson (String type, String action, JsonEntity paramJson)
    {
        switch (type)
        {
            case TriggerType.LABEL_STRING:
                return parseLabelTriggerCommand(action, paramJson);

            default:
                LogUtils.error(TriggerCommandParser.class, "dispatchTriggerCommandJson 没有对应的触发器类型 (type): " + type);
                return null;
        }
    }

    private static TriggerCommand parseLabelTriggerCommand (String action, JsonEntity paramJson)
    {
        switch (action)
        {
            case TriggerAction.LABEL_CLICK_STRING:
                return new LabelClickTrigger(TriggerAction.LABEL_CLICK, new LabelClickTriggerParam(paramJson));

            default:
                LogUtils.error(TriggerCommandParser.class, "parseLabelTriggerCommand label类触发器没有对应的触发器动作 (action): " + action);
                return null;
        }
    }
}
