package com.hujiugame.qingfeng.type.key;

/**
 * 配置文件 JSON 字段名常量
 */
public final class ConfigKey
{
    private ConfigKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // game_config.json 字段

    public static final class Game
    {
        private Game()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String VERSION = "version";
        public static final String LAUNCHER_VERSION = "launcher_version";
    }

    // ====================================================================================================
    // 内容列表配置字段（role_config.json / script_config.json / template_config.json 共用）

    public static final class Content
    {
        private Content()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 条目总数 */
        public static final String COUNT = "count";
        /** 角色列表 */
        public static final String ROLE = "role";
        /** 脚本列表 */
        public static final String SCRIPTS = "scripts";
        /** 模板列表 */
        public static final String TEMPLATES = "templates";
    }

    // ====================================================================================================
    // log_config.json 字段

    public static final class Log
    {
        private Log()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String LOG_LEVEL = "logLevel";
        public static final String FILE_LOG_LEVEL = "fileLogLevel";
    }

    // ====================================================================================================
    // directory_structure.json 字段

    public static final class Directory
    {
        private Directory()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String DIRECTORY = "directory";
        public static final String FILE = "file";
    }

    // ====================================================================================================
    // user_config.json 字段

    public static final class User
    {
        private User()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String LANGUAGE = "language";
        public static final String THEME = "theme";
        public static final String USE_VIEWPORT = "useViewport";
        public static final String FULLSCREEN = "fullscreen";

        public static final class Resolution
        {
            private Resolution()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String KEY = "resolution";
            public static final String WIDTH = "width";
            public static final String HEIGHT = "height";
        }

        public static final class Volume
        {
            private Volume()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String KEY = "soundVolume";
            public static final String TOTAL = "total";
            public static final String MUSIC = "music";
            public static final String SOUND = "sound";
        }
    }
}
