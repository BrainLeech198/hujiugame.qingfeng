package com.hujiugame.qingfeng.type.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameInfoKey
{
    private GameInfoKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 启动器相关
    public static final String LAUNCHER_VERSION = "launcher.version";

    public static final String USER_LANGUAGE = "user.language";
    public static final String USER_THEME = "user.theme";

    // 游戏列表
    public static final String GAME_LIST_NOW_PAGE = "game_list.now_page";
    public static final String GAME_LIST_MAX_PAGE = "game_list.max_page";
    public static final String GAME_LIST_ABSOLUTE_PATH = "game_list.absolute_path";
    public static final String GAME_LIST_SELECTED_PATH = "game_list.selected_path";
    public static final String GAME_LIST_SELECTED_NAME = "game_list.selected_name";
    public static final String GAME_LIST_SELECTED_LAUNCHER_VERSION = "game_list.selected_launcher_version";

    // 游戏相关
    public static final String GAME_PATH = "game.path";
    public static final String GAME_ID = "game.id";
    public static final String GAME_NAME = "game.name";
    public static final String GAME_VERSION = "game.version";
    public static final String GAME_LAUNCHER_VERSION = "game.launcher_version";

    public static final String PLAY_ROLE_ID = "play.role.id";
    public static final String PLAY_HOSTER = "play.hoster";
    public static final String PLAY_IPP = "play.ipp";

    public static final String PLAY_TREE_STRUCTURE_TYPE = "play.tree_structure.type";
    public static final String PLAY_TREE_STRUCTURE_ID = "play.tree_structure.id";

    public static final String PLAY_LAST_PAGE_ID = "play.last_page.id";
    public static final String PLAY_NOW_PAGE_ID = "play.now_page.id";
    public static final String PLAY_NEXT_PAGE_ID = "play.next_page.id";

    public static final List<String> KEYS;

    static
    {
        List<String> keys = new ArrayList<>();
        keys.add(LAUNCHER_VERSION);

        keys.add(USER_LANGUAGE);
        keys.add(USER_THEME);

        keys.add(GAME_LIST_NOW_PAGE);
        keys.add(GAME_LIST_MAX_PAGE);
        keys.add(GAME_LIST_ABSOLUTE_PATH);
        keys.add(GAME_LIST_SELECTED_PATH);
        keys.add(GAME_LIST_SELECTED_NAME);
        keys.add(GAME_LIST_SELECTED_LAUNCHER_VERSION);

        keys.add(GAME_PATH);
        keys.add(GAME_ID);
        keys.add(GAME_NAME);
        keys.add(GAME_VERSION);
        keys.add(GAME_LAUNCHER_VERSION);

        keys.add(PLAY_HOSTER);
        keys.add(PLAY_ROLE_ID);
        keys.add(PLAY_IPP);

        keys.add(PLAY_TREE_STRUCTURE_TYPE);
        keys.add(PLAY_TREE_STRUCTURE_ID);

        keys.add(PLAY_LAST_PAGE_ID);
        keys.add(PLAY_NOW_PAGE_ID);
        keys.add(PLAY_NEXT_PAGE_ID);

        KEYS = Collections.unmodifiableList(keys);
    }
}
