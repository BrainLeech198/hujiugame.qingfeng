package com.hujiugame.qingfeng.event;

import com.hujiugame.qingfeng.core.SceneStack;
import com.hujiugame.qingfeng.data.game.StateStructure;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.type.game.Event;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameSubState;
import com.hujiugame.qingfeng.event.imp.EventPushGameState;
import com.hujiugame.qingfeng.event.imp.EventResetGameState;
import com.hujiugame.qingfeng.event.imp.EventSetGameState;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class EventDispatcher
{
    private SceneStack sceneStack;

    /**
     * 初始化事件服务，绑定状态服务
     *
     * @param playLocalData  游戏数据容器
     * @param sceneStack     游戏状态服务
     * @return 是否初始化成功
     */
    public boolean init (PlayLocalData playLocalData,
                         SceneStack sceneStack)
    {
        try
        {
            this.sceneStack = sceneStack;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(EventDispatcher.class, "init", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 分发事件到对应的处理方法（PUSH/POP/SET/RESET/LOAD_GAME_CONFIG）
     *
     * @param eventObject 事件对象
     */
    public void handleEvent (EventObject eventObject)
    {
        try
        {
            String eventName = eventObject.getEventName();
            LogUtils.debug(EventDispatcher.class, "handleEvent 事件名: " + eventName);

            switch (eventName)
            {
                case Event.PUSH_GAME_STATE:
                    handleEventOfPushGameState(eventObject);
                    break;

                case Event.POP_GAME_STATE:
                    handleEventOfPopGameState(eventObject);
                    break;

                case Event.SET_GAME_STATE:
                    handleEventOfSetGameState(eventObject);
                    break;

                case Event.RESET_GAME_STATE:
                    handleEventOfResetGameState();
                    break;

                case Event.ENTER_GAME:
                    handleEventOfEnterGame(eventObject);
                    break;

                case Event.QUIT_GAME:
                    handleEventOfQuitGame();
                    break;

                case Event.PLAY_GAME:
                    handleEventOfPlayGame(eventObject);
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(EventDispatcher.class, "handleEvent", e);
            throw new RuntimeException(e);
        }
    }

    private void handleEventOfPushGameState (EventObject event)
    {
        EventPushGameState pushEvent = (EventPushGameState) event;
        LogUtils.debug(EventDispatcher.class, "handleEventOfPushGameState 尝试执行推入游戏状态 (EventPushGameState):{ (State): " + pushEvent.getState() + ", (SubState): " + pushEvent.getSubState() + " }");
        sceneStack.pushGameState(new StateStructure(pushEvent.getState(), pushEvent.getSubState()));
        LogUtils.debug(EventDispatcher.class, "handleEventOfPushGameState 执行推入游戏状态成功");
    }


    private void handleEventOfPopGameState (EventObject event)
    {
        LogUtils.debug(EventDispatcher.class, "handleEventOfPopGameState 尝试执行弹出游戏状态");
        sceneStack.popGameState();
        LogUtils.debug(EventDispatcher.class, "handleEventOfPopGameState 执行弹出游戏状态成功");
    }

    private void handleEventOfSetGameState (EventObject eventObject)
    {
        EventSetGameState setEvent = (EventSetGameState) eventObject;
        LogUtils.debug(EventDispatcher.class, "handleEventOfSetGameState 尝试执行设置游戏状态 (EventSetGameState):{ (State): " + setEvent.getState() + ", (SubState): " + setEvent.getSubState() + " }");
        sceneStack.setGameState(new StateStructure(setEvent.getState(), setEvent.getSubState()));
        LogUtils.debug(EventDispatcher.class, "handleEventOfSetGameState 执行设置游戏状态成功");
    }

    private void handleEventOfResetGameState ()
    {
        LogUtils.debug(EventDispatcher.class, "handleEventOfResetGameState 尝试执行重置游戏状态");
        if (sceneStack.resetGameState())
        {
            LogUtils.debug(EventDispatcher.class, "handleEventOfResetGameState 重置游戏状态成功");
        }
        else
        {
            LogUtils.error(EventDispatcher.class, "handleEventOfResetGameState 重置游戏状态失败");
        }
    }

    private void handleEventOfEnterGame (EventObject event)
    {
        EventPushGameState pushGameStateEvent = new EventPushGameState(GameState.GAME, GameSubState.GAME_MENU);
        LogUtils.debug(EventDispatcher.class, "handleEventOfEnterGame 尝试执行进入游戏状态 (EventPushGameState):{ (State): " + pushGameStateEvent.getState() + ", (SubState): " + pushGameStateEvent.getSubState() + " }");
        handleEventOfPushGameState(pushGameStateEvent);
        LogUtils.debug(EventDispatcher.class, "handleEventOfEnterGame 执行进入游戏状态成功");
    }

    private void handleEventOfQuitGame ()
    {
        EventResetGameState resetGameStateEvent = new EventResetGameState();
        LogUtils.debug(EventDispatcher.class, "handleEventOfQuitGame 尝试执行退出游戏状态 (EventResetGameState)");
        handleEvent(resetGameStateEvent);
        LogUtils.debug(EventDispatcher.class, "handleEventOfQuitGame 执行退出游戏状态成功");
    }

    public void handleEventOfPlayGame (EventObject event)
    {
        EventPushGameState pushGameStateEvent = new EventPushGameState(GameState.GAME, GameSubState.GAME_PLAY);
        LogUtils.debug(EventDispatcher.class, "handleEventOfPlayGame 尝试执行游戏开始状态 (EventPushGameState):{ (State): " + pushGameStateEvent.getState() + ", (SubState): " + pushGameStateEvent.getSubState() + " }");
        handleEventOfPushGameState(pushGameStateEvent);
        LogUtils.debug(EventDispatcher.class, "handleEventOfPlayGame 执行游戏开始状态成功");
    }

    // ===================================================================================================================

    /**
     * 销毁事件服务
     *
     * @return 是否销毁成功
     */
    public boolean dispose ()
    {
        try
        {
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(EventDispatcher.class, "dispose", e);
            return false;
        }
    }
}
