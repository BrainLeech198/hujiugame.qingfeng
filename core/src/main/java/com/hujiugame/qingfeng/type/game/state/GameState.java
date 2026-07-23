package com.hujiugame.qingfeng.type.game.state;

import java.util.HashMap;
import java.util.Map;

public final class GameState
{
    private GameState()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final int INIT = 0;
    public static final int MENU = 1;
    public static final int CONFIG = 2;
    public static final int GAME = 3;
    public static final int SAVE = 4;
    public static final int EDIT = 5;

    private final static Map<Integer, Map<Integer, String>> game_state_and_sub_state_name = new HashMap<>();
    static
    {
        game_state_and_sub_state_name.put(INIT, new HashMap<Integer, String>(){{
            put(GameSubState.INIT, "初始化");
        }});

        game_state_and_sub_state_name.put(MENU, new HashMap<Integer, String>(){{
            put(GameSubState.MENU_MAIN, "主菜单");
            put(GameSubState.MENU_LIST, "游戏列表");
            put(GameSubState.MENU_LOAD, "游戏加载");
        }});

        game_state_and_sub_state_name.put(CONFIG, new HashMap<Integer, String>(){{
            put(GameSubState.CONFIG_BASIC, "基础配置");
        }});

        game_state_and_sub_state_name.put(GAME, new HashMap<Integer, String>(){{
            put(GameSubState.GAME_MENU, "游戏菜单");
            put(GameSubState.GAME_ROLE, "游戏角色");
            put(GameSubState.GAME_PLAY, "游戏游玩");
        }});
    }

    /**
     * 获取游戏状态的中文名称
     *
     * @param gameState 游戏主状态
     * @param subState  游戏子状态
     * @return 状态对应的中文名称，未知状态返回"未知"
     */
    public static String getGameStateName (int gameState, int subState)
    {
        try
        {
            if (game_state_and_sub_state_name.containsKey(gameState))
            {
                return game_state_and_sub_state_name.get(gameState).getOrDefault(subState,"未知");
            }
            else
            {
                return "未知";
            }
        }
        catch (Exception e)
        {
            return "未知";
        }
    }
}
