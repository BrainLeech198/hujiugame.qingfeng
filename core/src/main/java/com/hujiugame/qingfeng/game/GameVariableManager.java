package com.hujiugame.qingfeng.game;

import java.util.HashMap;
import java.util.Map;

import com.hujiugame.qingfeng.util.system.LogUtils;

public class GameVariableManager
{
    private final Map<String, Object> variables = new HashMap<>();

    public boolean init ()
    {
        try
        {
            variables.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameVariableManager.class, "init", e);
            return false;
        }
    }

    public boolean hasVariable (String name)
    {
        return variables.containsKey(name);
    }

    public Object getVariable (String name)
    {
        if (variables.containsKey(name))
        {
            return variables.get(name);
        }
        else
        {
            LogUtils.error(GameVariableManager.class, "getVariable 未定义的变量 (name): " + name);
            return null;
        }
    }

    public void setVariable (String name, Object value)
    {
        variables.put(name, value);
    }

    public void removeVariable (String name)
    {
        if (variables.containsKey(name))
        {
            variables.remove(name);
        }
        else
        {
            LogUtils.error(GameVariableManager.class, "removeVariable 未定义的变量 (name): " + name);
        }
    }

    public boolean dispose ()
    {
        try
        {
            variables.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameVariableManager.class, "dispose", e);
            return false;
        }
    }
}
