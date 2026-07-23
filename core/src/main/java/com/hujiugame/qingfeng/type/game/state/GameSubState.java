package com.hujiugame.qingfeng.type.game.state;

public final class GameSubState
{
    private GameSubState()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public final static int INIT = 0;

    public final static int MENU_MAIN = 0;
    public final static int MENU_LIST = 1;
    public final static int MENU_LOAD = 2;

    public final static int CONFIG_BASIC = 0;

    public final static int GAME_MENU = 0;
    public final static int GAME_ROLE = 1;
    public final static int GAME_PLAY = 2;
}
