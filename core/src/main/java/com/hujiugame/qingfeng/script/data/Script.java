package com.hujiugame.qingfeng.script.data;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.command.ScriptCommand;
import com.hujiugame.qingfeng.script.data.command.ScriptCommandParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Script
{
    private boolean valid;
    private JsonEntity json;
    private List<Argument> arguments;
    private List<ScriptCommand> commands;
    private Return returnValue;

    @Override
    public String toString() {
        return "Script{" +
            "valid=" + valid +
            ", arguments=" + arguments +
            ", commands=" + commands +
            ", returnValue=" + returnValue +
            ", json=" + json +
            '}';
    }

    public static class Argument
    {
        private boolean valid;
        private JsonEntity json;
        private Class<?> type;
        private String name;

        private void buildJson ()
        {
            this.json = new JsonEntity();
            this.json.put("class", TypeMapper.toTypeString(type));
            this.json.put("name", name);
        }

        public Argument (Class<?> type, String name)
        {
            this.type = type;
            this.name = name;
            valid = true;
            buildJson();
        }

        public Argument (JsonEntity json)
        {
            if (json.isMap())
            {
                String classString = json.getString("class");
                this.type = TypeMapper.parseClassLenient(classString);
                this.name = json.getString("name");
                this.json = json;
                valid = true;
            }
            else
            {
                valid = false;
            }
        }

        public boolean isValid ()
        {
            return valid;
        }

        public JsonEntity getJson ()
        {
            return json;
        }

        public Class<?> getType ()
        {
            return type;
        }

        public String getName ()
        {
            return name;
        }

        @Override
        public String toString() {
            return "Argument{" +
                "valid=" + valid +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", json=" + json +
                '}';
        }
    }

    private void buildJson ()
    {
        if (!valid)
        {
            LogUtils.error(Script.class, "buildJson 无效的脚本不能构建json");
            return;
        }
        json = new JsonEntity();
        json.put("arguments", arguments.stream().map(Argument::getJson).collect(Collectors.toList()));
        json.put("commands", commands.stream().map(ScriptCommand::getJson).collect(Collectors.toList()));
        json.put("return", returnValue.getJson());
    }

    public Script (List<Argument> arguments, List<ScriptCommand> commands, Return returnValue)
    {
        this.arguments = arguments;
        this.commands = commands;
        this.returnValue = returnValue;
        valid = true;
        buildJson();
    }

    public Script (JsonEntity json)
    {
        if (json.isMap())
        {
            // commands
            if (!json.containsKey("commands"))
            {
                LogUtils.error(Script.class, "Script 解析失败 缺少 commands 字段 (json): " + json);
                this.valid = false;
                return;
            }
            // return
            if (!json.containsKey("return"))
            {
                LogUtils.error(Script.class, "Script 解析失败 缺少 return 字段 (json): " + json);
                this.valid = false;
                return;
            }

            this.arguments = parseArguments(json);
            this.commands = ScriptCommandParser.parseList(json.getJsonEntityByKey("commands"));
            this.returnValue = new Return(json.getJsonEntityByKey("return"));

            if (this.commands == null || !this.returnValue.isValid())
            {
                LogUtils.error(Script.class, "Script 解析失败 命令列表或返回值无效 (json): " + json);
                this.valid = false;
                return;
            }

            this.json = json;
            valid = true;
        }
        else
        {
            LogUtils.error(Script.class, "Script 解析失败 需要Map数据 (json): " + json);
            this.valid = false;
            return;
        }
    }

    public List<Argument> getArguments ()
    {
        return arguments;
    }

    public List<ScriptCommand> getCommands ()
    {
        return commands;
    }

    public Return getReturnValue ()
    {
        return returnValue;
    }

    public boolean isValid ()
    {
        return valid;
    }

    public JsonEntity getJson ()
    {
        return json;
    }

    private List<Argument> parseArguments (JsonEntity json)
    {
        if (!json.containsKey("arguments"))
        {
            return Collections.emptyList();
        }
        List<JsonEntity> argumentList = json.getJsonEntityList("arguments");
        if (argumentList == null || argumentList.isEmpty())
        {
            return Collections.emptyList();
        }
        return argumentList.stream()
                .map(Argument::new)
                .collect(Collectors.toList());
    }

    public static class Return
    {
        private boolean valid;
        private JsonEntity json;
        private Class<?> type;
        private Object defaultValue;

        private void buildJson ()
        {
            this.json = new JsonEntity();
            this.json.put("class", TypeMapper.toTypeString(type));
            this.json.put("defaultValue", defaultValue);
        }

        public Return (Class<?> type, Object defaultValue)
        {
            this.type = type;
            this.defaultValue = defaultValue;
            valid = true;
            buildJson();
        }

        public Return (JsonEntity json)
        {
            if (json.isMap())
            {
                String classString = json.getString("class");
                this.type = TypeMapper.parseClass(classString);

                if (this.type == int.class)
                {
                    this.defaultValue = json.getInt("defaultValue");
                }
                else if (this.type == float.class)
                {
                    this.defaultValue = json.getFloat("defaultValue");
                }
                else if (this.type == boolean.class)
                {
                    this.defaultValue = json.getBoolean("defaultValue");
                }
                else if (this.type == String.class)
                {
                    this.defaultValue = json.getString("defaultValue");
                }
                this.json = json;
                valid = true;
            }
            else
            {
                valid = false;
            }
        }

        public boolean isValid ()
        {
            return valid;
        }

        public JsonEntity getJson ()
        {
            return json;
        }

        public Class<?> getType ()
        {
            return type;
        }

        public Object getDefaultValue ()
        {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Return{" +
                "valid=" + valid +
                ", type=" + type +
                ", defaultValue=" + defaultValue +
                ", json=" + json +
                '}';
        }
    }
}
