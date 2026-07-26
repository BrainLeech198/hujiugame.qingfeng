package com.hujiugame.qingfeng.type.game.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GameStatePageInfo
{
    private GameStatePageInfo ()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final Map<Integer, Map<Integer, String>> GAME_STATE_LAYOUT_MAP;
    static
    {
        Map<Integer, Map<Integer, String>> map = new HashMap<>();

        // INIT
        map.put(GameState.INIT, null);

        // MENU
        Map<Integer, String> menuMap = new HashMap<>();
        menuMap.put(GameSubState.MENU_MAIN, "menu_main");
        menuMap.put(GameSubState.MENU_LIST, "menu_list");
        menuMap.put(GameSubState.MENU_LOAD, "menu_load");
        map.put(GameState.MENU, Collections.unmodifiableMap(menuMap));

        // CONFIG
        Map<Integer, String> configMap = new HashMap<>();
        configMap.put(GameSubState.CONFIG_BASIC, "config_basic");
        configMap.put(GameSubState.CONFIG_DISPLAY, "config_display");
        map.put(GameState.CONFIG, Collections.unmodifiableMap(configMap));

        // GAME
        Map<Integer, String> gameMap = new HashMap<>();
        gameMap.put(GameSubState.GAME_MENU, "game_menu");
        gameMap.put(GameSubState.GAME_ROLE, "game_role");
        gameMap.put(GameSubState.GAME_PLAY, null);
        map.put(GameState.GAME, Collections.unmodifiableMap(gameMap));

        GAME_STATE_LAYOUT_MAP = Collections.unmodifiableMap(map);
    }

    public static final Map<Integer, Map<Integer, Boolean>> GAME_STATE_CONFIG_MAP;
    static
    {
        Map<Integer, Map<Integer, Boolean>> map = new HashMap<>();

        // INIT
        map.put(GameState.INIT, null);

        // MENU
        Map<Integer, Boolean> menuMap = new HashMap<>();
        menuMap.put(GameSubState.MENU_MAIN, true);
        menuMap.put(GameSubState.MENU_LIST, true);
        menuMap.put(GameSubState.MENU_LOAD, true);
        map.put(GameState.MENU, Collections.unmodifiableMap(menuMap));

        // CONFIG
        map.put(GameState.CONFIG, null);

        // GAME
        map.put(GameState.GAME, null);

        GAME_STATE_CONFIG_MAP = Collections.unmodifiableMap(map);
    }
}
