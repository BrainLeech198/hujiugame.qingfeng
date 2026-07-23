package com.hujiugame.qingfeng.type.key;

public final class RequirementUiKey
{
    private RequirementUiKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ===================================================================================================================
    // 启动器

    // ===================================================================================================================

    // menu main
    public static final String MENU_MAIN_BUTTON_START = "start";
    public static final String MENU_MAIN_BUTTON_CREATE = "create";
    public static final String MENU_MAIN_BUTTON_CONFIG = "config";
    public static final String MENU_MAIN_BUTTON_QUIT = UniversalKey.BUTTON_QUIT;

    // ===================================================================================================================

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

    public static final String MENU_LIST_BUTTON_BACK = UniversalKey.BUTTON_BACK;
    public static final String MENU_LIST_BUTTON_IMPORT = "import";
    public static final String MENU_LIST_BUTTON_PROFILE = "profile";
    public static final String MENU_LIST_BUTTON_SHARE = "share";
    public static final String MENU_LIST_BUTTON_DELETE = "delete";

    public static final String MENU_LIST_BUTTON_SELECT_LAST_PAGE = "select.last_page";
    public static final String MENU_LIST_BUTTON_SELECT_NEXT_PAGE = "select.next_page";

    // ===================================================================================================================
    // 游戏中

    // ===================================================================================================================

    // game_menu
    public static final String GAME_MENU_BUTTON_START = "start";
    public static final String GAME_MENU_BUTTON_QUIT = UniversalKey.BUTTON_QUIT;
}
