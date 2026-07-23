package com.hujiugame.qingfeng.input;

import com.hujiugame.qingfeng.Main;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.type.key.UniversalKey;
import com.hujiugame.qingfeng.ui.MessageBox;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class UniversalInputHandlerFunction
{

    private UniversalInputHandlerFunction()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 处理确认（Enter）输入，优先响应弹窗按钮，然后处理游戏内外逻辑。
     *
     * @param uiManager          UI管理器
     * @param messageBox         弹窗管理器
     * @param gameHost     游戏主机
     * @param universalInputType 输入类型（键盘/手柄）
     * @return 是否成功处理
     */
    public static boolean handleEnter(UiManager uiManager, MessageBox messageBox, GameHost gameHost, UniversalInputType universalInputType)
    {
        try
        {
            // 游戏中
            if (gameHost.getGameSessionManager().isInGame())
            {
                MessageBox gameMessageBox = gameHost.getPlayLocalData().getMessageBox();
                UiManager gameUiManager = gameHost.getPlayLocalData().getUiManager();
                if (gameUiManager == null)
                    return true;

                // 弹窗按钮
                if (gameMessageBox != null)
                {
                    if (gameMessageBox.getEnterButtonTag() != null)
                    {
                        LogUtils.debug(UniversalInputHandlerFunction.class, "handleEnter (input): " + universalInputType +" 游戏内弹窗按钮Enter");
                        gameUiManager.setButtonClicked(gameMessageBox.getEnterButtonTag(), true);
                        return true;
                    }
                }
            }
            // 游戏外
            else
            {
                // 弹窗按钮
                if (messageBox.getEnterButtonTag() != null)
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEnter (input): " + universalInputType +" 游戏外弹窗按钮Enter");
                    uiManager.setButtonClicked(messageBox.getEnterButtonTag(), true);
                    return true;
                }
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UniversalInputHandlerFunction.class, "handleEnter (input): " + universalInputType, e);
            return false;
        }
    }

    /**
     * 处理取消（Escape）输入，优先响应弹窗按钮，然后处理返回/退出按钮逻辑。
     *
     * @param uiManager          UI管理器
     * @param messageBox         弹窗管理器
     * @param gameHost     游戏主机
     * @param universalInputType 输入类型（键盘/手柄）
     * @return 是否成功处理
     */
    public static boolean handleEscape (UiManager uiManager, MessageBox messageBox, GameHost gameHost, UniversalInputType universalInputType)
    {
        try
        {
            // 游戏中
            if (gameHost.getGameSessionManager().isInGame())
            {
                MessageBox gameMessageBox = gameHost.getPlayLocalData().getMessageBox();
                UiManager gameUiManager = gameHost.getPlayLocalData().getUiManager();
                if (gameUiManager == null)
                    return true;

                // 弹窗按钮
                if (gameMessageBox.getEnterButtonTag() != null)
                {
                    if (gameMessageBox.getEscapeButtonTag() != null)
                    {
                        LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏内弹窗按钮Escape");
                        gameUiManager.setButtonClicked(gameMessageBox.getEscapeButtonTag(), true);
                        return true;
                    }
                }

                // 返回按钮
                if (gameUiManager.containsButton(UniversalKey.BUTTON_BACK))
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏内按钮Back");
                    gameUiManager.setButtonClicked(UniversalKey.BUTTON_BACK, true);
                    return true;
                }

                if (gameUiManager.containsButton(UniversalKey.BUTTON_QUIT))
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏内按钮Quit");
                    gameUiManager.setButtonClicked(UniversalKey.BUTTON_QUIT, true);
                    return true;
                }
            }
            // 游戏外
            else
            {
                // 弹窗按钮
                if (messageBox.getEscapeButtonTag() != null)
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏外弹窗按钮Escape");
                    uiManager.setButtonClicked(messageBox.getEscapeButtonTag(), true);
                    return true;
                }

                if (uiManager.containsButton(UniversalKey.BUTTON_BACK))
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏外按钮Back");
                    uiManager.setButtonClicked(UniversalKey.BUTTON_BACK, true);
                    return true;
                }

                if (uiManager.containsButton(UniversalKey.BUTTON_QUIT))
                {
                    LogUtils.debug(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType +" 游戏外按钮Quit");
                    uiManager.setButtonClicked(UniversalKey.BUTTON_QUIT, true);
                    return true;
                }
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UniversalInputHandlerFunction.class, "handleEscape (input): " + universalInputType, e);
            return false;
        }
    }

    /**
     * 处理全屏切换输入
     * @return 是否成功切换
     */
    public static boolean handleToggleFullscreen ()
    {
        try
        {
            Main.toggleFullscreen();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UniversalInputHandlerFunction.class, "handleToggleFullscreen", e);
            return false;
        }
    }
}
