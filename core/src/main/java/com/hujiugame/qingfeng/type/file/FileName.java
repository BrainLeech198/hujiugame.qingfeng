package com.hujiugame.qingfeng.type.file;

public final class FileName
{
    private FileName()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 启动器
    public static final String INTERNAL_DIRECTORY_STRUCTURE_CONFIG = "directory_structure.json";

    public static final String IMAGE_ERROR = "error.png";
    public static final String DEFAULT_SPLASH = "app_init.png";
    public static final String DEFAULT_REPAIR = "app_repair.png";

    public static final String KEYBOARD_BUTTON_ENTER = "keyboard_button_enter.png";
    public static final String KEYBOARD_BUTTON_ESCAPE = "keyboard_button_escape.png";
    public static final String KEYBOARD_BUTTON_ENTER_OR_ESCAPE = "keyboard_button_enter_or_escape.png";

    public static final String CONTROLLER_CURSOR = "controller_cursor.png";
    public static final String CONTROLLER_BUTTON_A = "controller_button_a.png";
    public static final String CONTROLLER_BUTTON_B = "controller_button_b.png";
    public static final String CONTROLLER_BUTTON_A_OR_B = "controller_button_a_or_b.png";

    public static final String VIRTUAL_CONFIRM_RECT = "virtual_confirm_rect.png";
    public static final String VIRTUAL_CANCEL_RECT = "virtual_cancel_rect.png";

    public static final String LOG_CONFIG = "log_config.json";
    public static final String UPDATE_CONFIG = "update_config.json";
    public static final String CRASH_LOG = "crash-";       // 崩溃日志前缀（后接时间戳）
    public static final String APP_VERSION = "app_version.json";
    public static final String APP_CONFIG = "app_config.json";
    public static final String USER_CONFIG = "user_config.json";

    public static final String DEFAULT_LANGUAGE_PATH = "zh_CN";
    public static final String LANGUAGE_DICTIONARY_CONFIG = "language_config.json";
    public static final String LANGUAGE_S_CONFIG = "language.json";

    public static final String DEFAULT_THEME = "default_theme";
    public static final String THEME_DICTIONARY_CONFIG = "theme_config.json";
    public static final String THEME_S_CONFIG = "theme.json";
    public static final String THEME_S_ICON = "icon.png";

    public static final String THEME_S_UI_FONT_S_CONFIG = "font.json";
    public static final String THEME_S_UI_MESSAGE_BOX_S_CONFIG = "message_box.json";
    public static final String THEME_S_UI_CONFIG = "ui_config.json";

    public static final String PAGE_LAYOUT = "layout.json";
    public static final String PAGE_CONFIG = "config.json";

    public static final String GAME_DICTIONARY_CONFIG = "game_config.json";
    public static final String SAVE_DICTIONARY_CONFIG = "save_config.json";
    public static final String IMPORT_DICTIONARY_CONFIG = "import_config.json";
    public static final String EXPORT_DICTIONARY_CONFIG = "export_config.json";

    // 游戏内
    public static final String IN_GAME_CONFIG = "game.json";
    public static final String IN_GAME_ICON = "icon.png";
    public static final String IN_GAME_USER_CONFIG = "user_config.json";

    public static final String IN_GAME_SCRIPT_DICTIONARY_CONFIG = "script_config.json";

    public static final String IN_GAME_PAGE_LAYOUT = "layout.json";
    public static final String IN_GAME_PAGE_CONFIG = "config.json";

    public static final String IN_GAME_STORY_TEMPLATE_DICTIONARY_CONFIG = "template_config.json";
    public static final String IN_GAME_STORY_S_ROLE_DICTIONARY_CONFIG = "role_config.json";
    public static final String IN_GAME_STORY_S_ROLE_CONFIG = "role.json";
    public static final String IN_GAME_STORY_S_ROLE_SHOW_LAYOUT = "show.json";

    public static final String IN_GAME_STORY_S_ROLE_PAGE_S_LAYOUT = "layout.json";
    public static final String IN_GAME_STORY_S_ROLE_PAGE_S_BEHAVIOR = "behavior.json";
}
