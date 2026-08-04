package com.hujiugame.qingfeng.type.key;

public class DialogKey
{
    private DialogKey ()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static class FileChooser
    {
        public static final String IMPORT_GAME_TAG = "import_game";
        public static final String IMPORT_GAME_NAME = "选择游戏";
        private FileChooser ()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }
    }
}
