package com.hujiugame.qingfeng.script;

import com.hujiugame.qingfeng.game.GameInfoManager;
import com.hujiugame.qingfeng.game.GameScriptManager;
import com.hujiugame.qingfeng.game.GameSessionManager;
import com.hujiugame.qingfeng.game.GameVariableManager;
import com.hujiugame.qingfeng.ui.UiManager;

public class ScriptContent
{
    private final UiManager uiManager;
    private final GameSessionManager gameSessionManager;
    private final GameVariableManager gameVariableManager;
    private final GameInfoManager gameInfoManager;
    private final GameScriptManager gameScriptManager;

    public ScriptContent (UiManager uiManager,
                          GameSessionManager gameSessionManager,
                          GameVariableManager gameVariableManager,
                          GameInfoManager gameInfoManager,
                          GameScriptManager gameScriptManager)
    {
        this.uiManager = uiManager;
        this.gameSessionManager = gameSessionManager;
        this.gameVariableManager = gameVariableManager;
        this.gameInfoManager = gameInfoManager;
        this.gameScriptManager = gameScriptManager;
    }

    public UiManager getUiManager ()
    {
        return uiManager;
    }

    public GameSessionManager getGameSessionManager ()
    {
        return gameSessionManager;
    }

    public GameVariableManager getGameVariableManager ()
    {
        return gameVariableManager;
    }

    public GameInfoManager getGameInfoManager ()
    {
        return gameInfoManager;
    }

    public GameScriptManager getGameScriptManager ()
    {
        return gameScriptManager;
    }
}
