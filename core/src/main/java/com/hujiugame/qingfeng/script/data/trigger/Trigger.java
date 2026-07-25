package com.hujiugame.qingfeng.script.data.trigger;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandParser;
import com.hujiugame.qingfeng.script.data.trigger.command.TriggerCommand;
import com.hujiugame.qingfeng.type.key.ScriptKey;
import com.hujiugame.qingfeng.script.data.trigger.command.TriggerCommandParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Trigger
{
    private boolean valid;
    private JsonEntity json;
    private TriggerCommand triggerCommand;
    private List<ScriptCommand> commands;

    public Trigger (TriggerCommand triggerCommand, List<ScriptCommand> commands)
    {
        this.triggerCommand = triggerCommand;
        this.commands = commands;
        this.valid = true;
        buildJson();
    }

    public Trigger (JsonEntity json)
    {
        if (json.isMap())
        {
            LogUtils.debug(Trigger.class, "Trigger 尝试解析触发器 (json): " + json);

            // trigger字段
            if (!json.containsKey(ScriptKey.Trigger.TRIGGER))
            {
                LogUtils.error(Trigger.class, "Trigger 解析失败 缺少 " + ScriptKey.Trigger.TRIGGER + " 字段 (json): " + json);
                this.valid = false;
                return;
            }
            // commands字段
            if (!json.containsKey(ScriptKey.COMMANDS))
            {
                LogUtils.error(Trigger.class, "Trigger 解析失败 缺少 " + ScriptKey.COMMANDS + " 字段 (json): " + json);
                this.valid = false;
                return;
            }

            this.triggerCommand = TriggerCommandParser.parse(json.getJsonEntityByKey(ScriptKey.Trigger.TRIGGER));
            this.commands = ScriptCommandParser.parseList(json.getJsonEntityByKey(ScriptKey.COMMANDS));

            if (this.triggerCommand == null || !this.triggerCommand.isValid() || this.commands == null)
            {
                LogUtils.error(Trigger.class, "Trigger 解析失败 触发器命令或指令列表无效 (json): " + json);
                this.valid = false;
                return;
            }

            this.json = json;
            this.valid = true;
        }
        else
        {
            LogUtils.error(Trigger.class, "Trigger 解析失败 需要Map数据 (json): " + json);
            this.valid = false;
        }
    }

    private void buildJson ()
    {
        json = new JsonEntity();
        json.put(ScriptKey.Trigger.TRIGGER, triggerCommand.getJson());
        json.put(ScriptKey.COMMANDS, commands.stream().map(ScriptCommand::getJson).collect(Collectors.toList()));
    }

    public boolean isValid ()
    {
        return valid;
    }

    public JsonEntity getJson ()
    {
        return json;
    }

    public TriggerCommand getTriggerCommand ()
    {
        return triggerCommand;
    }

    public List<ScriptCommand> getCommands ()
    {
        return commands;
    }

    @Override
    public String toString() {
        return "Trigger{" +
            "valid=" + valid +
            ", triggerCommand=" + triggerCommand +
            ", commands=" + commands +
            ", json=" + json +
            '}';
    }
}
