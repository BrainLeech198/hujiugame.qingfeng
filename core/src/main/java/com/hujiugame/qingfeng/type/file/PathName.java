package com.hujiugame.qingfeng.type.file;

public final class PathName
{
    private PathName()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 启动器
    // 游戏文件夹
    private static final String COMPANY = "hujiugame/";
    private static final String APPLICATION = "qingfeng/";
    public static final String BASE = COMPANY + APPLICATION;

    // 临时文件夹
    public static final String TEMP = "temp/";

    // 日志路径
    public static final String LOG = "log/";

    // 资源路径
    public static final String ASSET = "asset/";

    // 主题&语言
    public static final String ASSET_S_THEME = ASSET + "theme/";
    public static final String ASSET_S_LANGUAGE = ASSET + "language/";

    // 页面构架
    public static final String ASSET_S_PAGE = ASSET + "page/";

    // 资源路径
    public static final String ASSET_S_RESOURCE = ASSET + "resource/";
    public static final String ASSET_S_RESOURCE_IMAGE = ASSET_S_RESOURCE + "image/";
    public static final String ASSET_S_RESOURCE_AUDIO = ASSET_S_RESOURCE + "audio/";

    // 样式路径
    public static final String ASSET_S_UI = ASSET + "ui/";
    public static final String ASSET_S_UI_FONT = ASSET_S_UI + "font/";
    public static final String ASSET_S_UI_LABEL = ASSET_S_UI + "label/";
    public static final String ASSET_S_UI_IMAGE = ASSET_S_UI + "image/";
    public static final String ASSET_S_UI_BUTTON = ASSET_S_UI + "button/";
    public static final String ASSET_S_UI_MESSAGE_BOX = ASSET_S_UI + "message_box/";

    // 游戏目录
    public static final String GAME = "game/";

    // 存档目录
    public static final String SAVE = "save/";

    // 游戏中
    // 资源路径
    public static final String IN_GAME_ASSET = "asset/";

    // 脚本路径
    public static final String IN_GAME_ASSET_S_SCRIPT = IN_GAME_ASSET + "script/";

    // 故事路径
    public static final String IN_GAME_ASSET_S_STORY = IN_GAME_ASSET + "story/";
    public static final String IN_GAME_ASSET_S_STORY_TEMPLATE = IN_GAME_ASSET_S_STORY + "template/";
    public static final String IN_GAME_ASSET_S_STORY_ROLE = IN_GAME_ASSET_S_STORY + "role/";
    public static final String IN_GAME_ASSET_S_STORY_ROLE_S_LAYOUT = "layout/";
    public static final String IN_GAME_ASSET_S_STORY_ROLE_S_PAGE = "page/";
    public static final String IN_GAME_ASSET_S_STORY_ROLE_S_TREE = "tree/";

    // 主题&语言
    public static final String IN_GAME_ASSET_S_THEME = IN_GAME_ASSET + "theme/";
    public static final String IN_GAME_ASSET_S_LANGUAGE = IN_GAME_ASSET + "language/";

    // 页面架构
    public static final String IN_GAME_ASSET_S_PAGE = IN_GAME_ASSET + "page/";
}
