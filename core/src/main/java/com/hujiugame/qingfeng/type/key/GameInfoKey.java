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

    // ====================================================================================================
    // 启动器相关

    public static final class Launcher
    {
        private Launcher()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String VERSION = "launcher.version";
    }

    // ====================================================================================================
    // 用户配置

    public static final class User
    {
        private User()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String LANGUAGE = "user.language";
        public static final String LANGUAGE_NAME = "user.language.name";
        public static final String THEME = "user.theme";
        public static final String THEME_NAME = "user.theme.name";
        public static final String USE_VIEWPORT = "user.use_viewport";
        public static final String FULLSCREEN = "user.fullscreen";

        public static final class Resolution
        {
            private Resolution()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String WIDTH = "user.resolution.width";
            public static final String HEIGHT = "user.resolution.height";
        }

        public static final class SoundVolume
        {
            private SoundVolume()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String TOTAL = "user.sound_volume.total";
            public static final String MUSIC = "user.sound_volume.music";
            public static final String SOUND = "user.sound_volume.sound";
        }
    }

    // ====================================================================================================
    // 游戏列表

    public static final class GameList
    {
        private GameList()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String NOW_PAGE = "game_list.now_page";
        public static final String MAX_PAGE = "game_list.max_page";
        public static final String ABSOLUTE_PATH = "game_list.absolute_path";
        public static final String SELECTED_PATH = "game_list.selected_path";
        public static final String SELECTED_NAME = "game_list.selected_name";
        public static final String SELECTED_LAUNCHER_VERSION = "game_list.selected_launcher_version";
    }

    // ====================================================================================================
    // 游戏相关

    public static final class Game
    {
        private Game()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String PATH = "game.path";
        public static final String ID = "game.id";
        public static final String NAME = "game.name";
        public static final String VERSION = "game.version";
        public static final String LAUNCHER_VERSION = "game.launcher_version";
    }

    // ====================================================================================================
    // 游戏进行中

    public static final class Play
    {
        private Play()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String ROLE_ID = "play.role.id";
        public static final String HOSTER = "play.hoster";
        public static final String IPP = "play.ipp";

        public static final String LAST_PAGE_ID = "play.last_page.id";
        public static final String NOW_PAGE_ID = "play.now_page.id";
        public static final String NEXT_PAGE_ID = "play.next_page.id";

        public static final class TreeStructure
        {
            private TreeStructure()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String TYPE = "play.tree_structure.type";
            public static final String ID = "play.tree_structure.id";
        }
    }

    // ====================================================================================================

    /**
     * 所有 key 的不可变列表，用于 GameInfoManager 校验
     */
    public static final List<String> KEYS;

    static
    {
        List<String> keys = new ArrayList<>();
        keys.add(Launcher.VERSION);

        keys.add(User.LANGUAGE);
        keys.add(User.LANGUAGE_NAME);
        keys.add(User.THEME);
        keys.add(User.THEME_NAME);
        keys.add(User.USE_VIEWPORT);
        keys.add(User.FULLSCREEN);
        keys.add(User.Resolution.WIDTH);
        keys.add(User.Resolution.HEIGHT);
        keys.add(User.SoundVolume.TOTAL);
        keys.add(User.SoundVolume.MUSIC);
        keys.add(User.SoundVolume.SOUND);

        keys.add(GameList.NOW_PAGE);
        keys.add(GameList.MAX_PAGE);
        keys.add(GameList.ABSOLUTE_PATH);
        keys.add(GameList.SELECTED_PATH);
        keys.add(GameList.SELECTED_NAME);
        keys.add(GameList.SELECTED_LAUNCHER_VERSION);

        keys.add(Game.PATH);
        keys.add(Game.ID);
        keys.add(Game.NAME);
        keys.add(Game.VERSION);
        keys.add(Game.LAUNCHER_VERSION);

        keys.add(Play.ROLE_ID);
        keys.add(Play.HOSTER);
        keys.add(Play.IPP);

        keys.add(Play.TreeStructure.TYPE);
        keys.add(Play.TreeStructure.ID);

        keys.add(Play.LAST_PAGE_ID);
        keys.add(Play.NOW_PAGE_ID);
        keys.add(Play.NEXT_PAGE_ID);

        KEYS = Collections.unmodifiableList(keys);
    }
}
