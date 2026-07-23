package com.hujiugame.qingfeng.script.data.value;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommand;
import com.hujiugame.qingfeng.script.data.value.command.ValueCommandParser;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 脚本值对象，对应 JSON: {"expression" : [{@link ValueCommand} ...]}
 * <p>
 * 仅作为存储容器，不包含求值逻辑。求值由执行器负责。
 */
public class ValueObject
{
    protected boolean valid;
    protected JsonEntity json;
    protected List<ValueCommand> expression;

    public ValueObject (JsonEntity json)
    {
        if (json != null && json.isMap())
        {
            if (!json.containsKey("expression"))
            {
                LogUtils.error(ValueObject.class, "缺少 expression 字段 (json): " + json);
                this.valid = false;
                return;
            }

            List<JsonEntity> cmdList = json.getJsonEntityList("expression");
            this.expression = new ArrayList<>(cmdList.size());
            for (JsonEntity cmdJson : cmdList)
            {
                ValueCommand cmd = ValueCommandParser.parse(cmdJson);
                if (cmd != null && cmd.isValid())
                {
                    this.expression.add(cmd);
                }
                else
                {
                    LogUtils.error(ValueObject.class, "expression 中包含无效命令 (json): " + cmdJson);
                    this.valid = false;
                    return;
                }
            }
            this.json = json;
            this.valid = true;
        }
        else
        {
            LogUtils.error(ValueObject.class, "需要 Map 数据 (json): " + json);
            this.valid = false;
        }
    }

    // ===================================================================================================================

    public boolean isValid ()
    {
        return valid;
    }

    public JsonEntity getJson ()
    {
        return json;
    }

    public List<ValueCommand> getExpression ()
    {
        return expression;
    }

    @Override
    public String toString() {
        return "ValueObject{" +
            "valid=" + valid +
            ", expression=" + expression +
            ", json=" + json +
            '}';
    }
}
