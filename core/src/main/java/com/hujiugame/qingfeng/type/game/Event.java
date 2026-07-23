package com.hujiugame.qingfeng.type.game;

public final class Event
{
    private Event()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String PUSH_GAME_STATE = "push_game_state";
    public static final String POP_GAME_STATE = "pop_game_state";
    public static final String SET_GAME_STATE = "set_game_state";
    public static final String RESET_GAME_STATE = "reset_game_state";

    public static final String ENTER_GAME = "enter_game";
    public static final String QUIT_GAME = "quit_game";
    public static final String PLAY_GAME = "play_game";
}
