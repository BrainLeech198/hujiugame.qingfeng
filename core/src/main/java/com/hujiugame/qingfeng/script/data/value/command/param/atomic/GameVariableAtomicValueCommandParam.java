package com.hujiugame.qingfeng.script.data.value.command.param.atomic;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.value.command.param.ValueCommandParam;

public class GameVariableAtomicValueCommandParam implements ValueCommandParam
{
    private boolean valid;
    private JsonEntity json;

    private String gameVariableKey;

    private void buildJson ()
    {
        if (!valid)
        {
            throw new IllegalStateException("An invalid command parameter cannot be built.");
        }
        json = new JsonEntity();
        json.put("key", gameVariableKey);
    }

    public GameVariableAtomicValueCommandParam (String gameVariableKey)
    {
        valid = true;
        this.gameVariableKey = gameVariableKey;
        buildJson();
    }

    public GameVariableAtomicValueCommandParam (JsonEntity json)
    {
        valid = false;
        if (json.isMap())
        {
            if (!json.containsKey("key"))
            {
                throw new IllegalArgumentException("Command parameter must have \"key\" field. (json): " + json);
            }
            gameVariableKey = json.getString("key");
            this.json = json;
            valid = true;
        }
        else
        {
            throw new IllegalArgumentException("Command parameter must be a map.");
        }
    }

    @Override
    public boolean isValid ()
    {
        return valid;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }

    @Override
    public String toString() {
        return "GameVariableAtomicValueCommandParam{" +
            "valid=" + valid +
            ", gameVariableKey='" + gameVariableKey + '\'' +
            ", json=" + json +
            '}';
    }

    public String getGameVariableKey ()
    {
        return gameVariableKey;
    }
}
