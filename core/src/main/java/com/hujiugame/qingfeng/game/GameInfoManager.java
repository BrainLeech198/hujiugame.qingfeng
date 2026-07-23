package com.hujiugame.qingfeng.game;

import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.HashMap;
import java.util.Map;

public final class GameInfoManager
{
    private final Map<String, Object> gameInfoMap = new HashMap<>();
    private long gameInfoMapStateCode = 0;

    /**
     * 更新状态码，标记游戏信息发生变化
     */
    private void updateStateCode ()
    {
        long stateCode = gameInfoMapStateCode;
        gameInfoMapStateCode = stateCode + 1;
    }

    /**
     * 获取当前状态码，用于检测游戏信息变化
     *
     * @return 当前状态码
     */
    public Long getStateCode ()
    {
        return gameInfoMapStateCode;
    }

    /**
     * 获取指定键对应的游戏信息值
     *
     * @param key 游戏信息键，必须在 GameInfoKey.KEYS 中
     * @return 对应的信息值，键不存在或无效时返回 null
     */
    public Object getInfo (String key)
    {
        if (!GameInfoKey.KEYS.contains(key))
        {
            LogUtils.error(GameInfoManager.class, "getInfo 键值错误 (key): " + key);
            return null;
        }
        if (gameInfoMap.containsKey(key))
        {
            return gameInfoMap.get(key);
        }
        else
        {
            LogUtils.debug(GameInfoManager.class, "getInfo 值不存在 (key): " + key);
            return null;
        }
    }

    /**
     * 设置指定键的游戏信息值并更新状态码
     *
     * @param key   游戏信息键，必须在 GameInfoKey.KEYS 中
     * @param value 待设置的值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean putInfo (String key, Object value)
    {
        if (!GameInfoKey.KEYS.contains(key))
        {
            LogUtils.error(GameInfoManager.class, "putInfo 键值错误 (key): " + key);
            return false;
        }
        updateStateCode();
        gameInfoMap.put(key, value);
        LogUtils.debug(GameInfoManager.class, "putInfo 上载键值 (key, value): " + key + ", " + value);
        return true;
    }

    /**
     * 删除指定键的游戏信息并更新状态码
     *
     * @param key 游戏信息键，必须在 GameInfoKey.KEYS 中
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean removeInfo (String key)
    {
        if (!GameInfoKey.KEYS.contains(key))
        {
            LogUtils.error(GameInfoManager.class, "removeInfo 键值错误 (key): " + key);
            return false;
        }
        updateStateCode();
        if (gameInfoMap.containsKey(key))
        {
            gameInfoMap.remove(key);
            LogUtils.debug(GameInfoManager.class, "removeInfo 删除键值 (key): " + key);
            return true;
        }
        else
        {
            LogUtils.error(GameInfoManager.class, "removeInfo 删除键值失败 (key): " + key);
            return false;
        }
    }
}
