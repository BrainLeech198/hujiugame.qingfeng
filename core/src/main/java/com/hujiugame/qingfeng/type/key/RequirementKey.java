package com.hujiugame.qingfeng.type.key;

public final class RequirementKey
{
    private RequirementKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // UI 标识名

    public static final class Ui
    {
        private Ui()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        // menu main
        public static final String MENU_MAIN_BUTTON_START = "start";
        public static final String MENU_MAIN_BUTTON_CREATE = "create";
        public static final String MENU_MAIN_BUTTON_CONFIG = "config";
        public static final String MENU_MAIN_BUTTON_QUIT = UniversalUiKey.BUTTON_QUIT;

        // menu list
        public static final String MENU_LIST_IMAGE_GAME_COVER = "game_cover";
        public static final String MENU_LIST_IMAGE_GAME_COVER_KIND = "game_cover.image";
        public static final String MENU_LIST_IMAGE_SELECT_FRAME = "select_frame";
        public static final String MENU_LIST_IMAGE_SELECT_FRAME_KIND = "select_frame.image";
        public static final String MENU_LIST_IMAGE_UNSELECT_FRAME = "unselect_frame";
        public static final String MENU_LIST_IMAGE_UNSELECT_FRAME_KIND = "unselect_frame.image";

        public static final String MENU_LIST_LABEL_ABSOLUTE_PATH = "absolute_path";
        public static final String MENU_LIST_LABEL_SELECTED_PATH = "selected_path";
        public static final String MENU_LIST_LABEL_PAGE = "page";

        public static final String MENU_LIST_BUTTON_BACK = UniversalUiKey.BUTTON_BACK;
        public static final String MENU_LIST_BUTTON_IMPORT = "import";
        public static final String MENU_LIST_BUTTON_PROFILE = "profile";
        public static final String MENU_LIST_BUTTON_SHARE = "share";
        public static final String MENU_LIST_BUTTON_DELETE = "delete";

        public static final String MENU_LIST_BUTTON_SELECT_LAST_PAGE = "select.last_page";
        public static final String MENU_LIST_BUTTON_SELECT_NEXT_PAGE = "select.next_page";

        // game_menu
        public static final String GAME_MENU_BUTTON_START = "start";
        public static final String GAME_MENU_BUTTON_QUIT = UniversalUiKey.BUTTON_QUIT;
    }

    // ====================================================================================================
    // Config 配置 key

    public static final class Config
    {
        private Config()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        // menu list
        public static final String MENU_LIST_PAGE_MAX_GAME = "pageMaxGame";
    }

    // ====================================================================================================
    // 语言 key

    public static final class Language
    {
        private Language()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String REQUIREMENT_BLOCK = "requirement.json";

        // ====================================================================================================
        // 启动器语言

        // 弹窗系
        public static final String MESSAGE_BOX_FIRST_KEY = "message_box";

        public static final String MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_KEY = "open_official_website";
        public static final String MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_TITLE = "open_official_website.title";
        public static final String MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_CONTENT = "open_official_website.content";

        public static final String MESSAGE_BOX_QUIT_GAME_KEY = "quit_game";
        public static final String MESSAGE_BOX_QUIT_GAME_TITLE = "quit_game.title";
        public static final String MESSAGE_BOX_QUIT_GAME_CONTENT = "quit_game.content";

        public static final String MESSAGE_BOX_UPDATE_DETECTED_KEY = "update_detected";
        public static final String MESSAGE_BOX_UPDATE_DETECTED_TITLE = "update_detected.title";
        public static final String MESSAGE_BOX_UPDATE_DETECTED_CONTENT = "update_detected.content";
        public static final String MESSAGE_BOX_UPDATE_REQUEST_FAILED_KEY = "update_request_failed";
        public static final String MESSAGE_BOX_UPDATE_REQUEST_FAILED_TITLE = "update_request_failed.title";
        public static final String MESSAGE_BOX_UPDATE_REQUEST_FAILED_CONTENT = "update_request_failed.content";

        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_KEY = "game_version_different";
        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_TITLE = "game_version_different.title";
        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_1 = "game_version_different.content.1";
        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_2 = "game_version_different.content.2";
        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_3 = "game_version_different.content.3";
        public static final String MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_4 = "game_version_different.content.4";

        // Menu 系
        public static final String MENU_FIRST_KEY = "menu";

        public static final String MENU_MAIN_BUTTON_START = "main.button.start";
        public static final String MENU_MAIN_BUTTON_CREATE = "main.button.create";
        public static final String MENU_MAIN_BUTTON_CONFIG = "main.button.config";
        public static final String MENU_MAIN_BUTTON_QUIT = "main.button.quit";

        public static final String MENU_LIST_BUTTON_BACK = "list.button.back";
        public static final String MENU_LIST_BUTTON_IMPORT = "list.button.import";
        public static final String MENU_LIST_BUTTON_PROFILE = "list.button.profile";
        public static final String MENU_LIST_BUTTON_SHARE = "list.button.share";
        public static final String MENU_LIST_BUTTON_DELETE = "list.button.delete";
        public static final String MENU_LIST_BUTTON_LAST_PAGE = "list.button.last_page";
        public static final String MENU_LIST_BUTTON_NEXT_PAGE = "list.button.next_page";
        public static final String MENU_LIST_LABEL_ABSOLUTE_PATH = "list.label.absolute_path";
        public static final String MENU_LIST_LABEL_SELECTED_PATH = "list.label.selected_path";
        public static final String MENU_LIST_LABEL_PAGE = "list.label.page";
        public static final String MENU_LOAD_LABEL_LOADING = "load.label.loading";

        // Config 系
        public static final String CONFIG_FIRST_KEY = "config";

        public static final String CONFIG_BASIC_BACK = "basic.back";

        // ====================================================================================================
        // 游戏内语言

        // 弹窗系
        public static final String IN_GAME_MESSAGE_BOX_FIRST_KEY = "message_box";

        public static final String IN_GAME_MESSAGE_BOX_QUIT_GAME_KEY = "quit_game";
        public static final String IN_GAME_MESSAGE_BOX_QUIT_GAME_TITLE = "quit_game.title";
        public static final String IN_GAME_MESSAGE_BOX_QUIT_GAME_CONTENT = "quit_game.content";
    }
}
