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

        // config_basic
        public static final String CONFIG_BASIC_LANGUAGE = "language";
        public static final String CONFIG_BASIC_LANGUAGE_SELECTED = "language_selected";

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

        // 通用
        public static final String UNIVERSAL_PRIORITY_CONFIRM_UI = "priorityConfirmUi";
        public static final String UNIVERSAL_PRIORITY_CONFIRM_UI_TYPE = "type";
        public static final String UNIVERSAL_PRIORITY_CONFIRM_UI_TAG = "tag";

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

        /** 语言块文件名（language.json 的 blocks 元素） */
        public static final String REQUIREMENT_BLOCK = "requirement.json";

        // ====================================================================================================
        // 启动器语言集 requirement.json 第一层 key（含点）

        public static final String MESSAGE_BOX = "message_box";
        public static final String MENU_MAIN = "menu.main";
        public static final String MENU_LIST = "menu.list";
        public static final String MENU_LOAD = "menu.load";
        public static final String CONFIG_BASIC = "config.basic";

        // ====================================================================================================
        // message_box 节点（启动器 requirement.json）

        public static final class MessageBox
        {
            private MessageBox()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            // 弹窗标识（非 JSON key，showAsk/handleAsk 匹配用）
            public static final String OPEN_OFFICIAL_WEBSITE = "open_official_website";
            public static final String QUIT_GAME = "quit_game";
            public static final String UPDATE_DETECTED = "update_detected";
            public static final String UPDATE_REQUEST_FAILED = "update_request_failed";
            public static final String GAME_VERSION_DIFFERENT = "game_version_different";

            // JSON 子 key
            public static final String OPEN_OFFICIAL_WEBSITE_TITLE = "open_official_website.title";
            public static final String OPEN_OFFICIAL_WEBSITE_CONTENT = "open_official_website.content";
            public static final String QUIT_GAME_TITLE = "quit_game.title";
            public static final String QUIT_GAME_CONTENT = "quit_game.content";
            public static final String UPDATE_DETECTED_TITLE = "update_detected.title";
            public static final String UPDATE_DETECTED_CONTENT = "update_detected.content";
            public static final String UPDATE_REQUEST_FAILED_TITLE = "update_request_failed.title";
            public static final String UPDATE_REQUEST_FAILED_CONTENT = "update_request_failed.content";
            public static final String GAME_VERSION_DIFFERENT_TITLE = "game_version_different.title";
            public static final String GAME_VERSION_DIFFERENT_CONTENT_1 = "game_version_different.content.1";
            public static final String GAME_VERSION_DIFFERENT_CONTENT_2 = "game_version_different.content.2";
            public static final String GAME_VERSION_DIFFERENT_CONTENT_3 = "game_version_different.content.3";
            public static final String GAME_VERSION_DIFFERENT_CONTENT_4 = "game_version_different.content.4";
        }

        // ====================================================================================================
        // menu.main 节点（启动器 requirement.json）

        public static final class MenuMain
        {
            private MenuMain()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String BUTTON_START = "button.start";
            public static final String BUTTON_CREATE = "button.create";
            public static final String BUTTON_CONFIG = "button.config";
            public static final String BUTTON_QUIT = "button.quit";
        }

        // ====================================================================================================
        // menu.list 节点（启动器 requirement.json）

        public static final class MenuList
        {
            private MenuList()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String BUTTON_BACK = "button.back";
            public static final String BUTTON_IMPORT = "button.import";
            public static final String BUTTON_PROFILE = "button.profile";
            public static final String BUTTON_SHARE = "button.share";
            public static final String BUTTON_DELETE = "button.delete";
            public static final String BUTTON_LAST_PAGE = "button.last_page";
            public static final String BUTTON_NEXT_PAGE = "button.next_page";
            public static final String LABEL_ABSOLUTE_PATH = "label.absolute_path";
            public static final String LABEL_SELECTED_PATH = "label.selected_path";
            public static final String LABEL_PAGE = "label.page";
        }

        // ====================================================================================================
        // menu.load 节点（启动器 requirement.json）

        public static final class MenuLoad
        {
            private MenuLoad()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String LABEL_LOADING = "label.loading";
        }

        // ====================================================================================================
        // config.basic 节点（启动器 requirement.json）

        public static final class ConfigBasic
        {
            private ConfigBasic()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            public static final String BACK = "back";
            public static final String LANGUAGE = "language";
        }

        // ====================================================================================================
        // 游戏语言集 requirement.json（如 game/swxq/asset/language/chinese/requirement.json）

        public static final class InGame
        {
            private InGame()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** 游戏 requirement.json 第一层 key */
            public static final String MESSAGE_BOX = "message_box";

            /** message_box 节点（游戏 requirement.json） */
            public static final class MessageBox
            {
                private MessageBox()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 游戏弹窗标识（非 JSON key，showAsk/handleAsk 匹配用） */
                public static final String QUIT_GAME = "quit_game";

                /** JSON 子 key */
                public static final String QUIT_GAME_TITLE = "quit_game.title";
                public static final String QUIT_GAME_CONTENT = "quit_game.content";
            }
        }
    }
}
