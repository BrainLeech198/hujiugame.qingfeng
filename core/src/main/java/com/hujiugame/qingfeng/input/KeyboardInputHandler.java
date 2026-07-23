package com.hujiugame.qingfeng.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.hujiugame.qingfeng.di.InstanceContent;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.type.VirtualInputType;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class KeyboardInputHandler extends InputAdapter
{
    private final VirtualInputHandler virtualInputHandler;

    private final UiManager uiManager;
    private final GameHost gameHost;

    /**
     * 构造键盘输入处理器
     *
     * @param instanceContent     实例管理器
     * @param virtualInputHandler 虚拟输入处理器
     */
    public KeyboardInputHandler (InstanceContent instanceContent, VirtualInputHandler virtualInputHandler)
    {
        this.virtualInputHandler = virtualInputHandler;
        this.uiManager = instanceContent.getUiManager();
        this.gameHost = instanceContent.getGameHost();
    }

    /**
     * 键盘按下事件处理，处理方向键、回车、ESC、F11/F12 等按键。
     *
     * @param keycode 按键码
     * @return 是否已处理该事件
     */
    @Override
    public boolean keyDown (int keycode)
    {
        switch (keycode)
        {
            case Input.Keys.ESCAPE:
            {
                if (virtualInputHandler.getVirtualInputType() == VirtualInputType.NONE)
                {
                    return UniversalInputHandlerFunction.handleEscape(uiManager, uiManager.getMessageBox(), gameHost, UniversalInputType.KEYBOARD);
                }
                else if (virtualInputHandler.getVirtualInputType() == VirtualInputType.KEYBOARD_SELECT)
                {
                    return virtualInputHandler.clickVirtualCancelSelect();
                }
                return false;
            }

            case Input.Keys.ENTER:
            {
                if (virtualInputHandler.getVirtualInputType() == VirtualInputType.NONE)
                {
                    return UniversalInputHandlerFunction.handleEnter(uiManager, uiManager.getMessageBox(), gameHost, UniversalInputType.KEYBOARD);
                }
                else if (virtualInputHandler.getVirtualInputType() == VirtualInputType.KEYBOARD_SELECT)
                {
                    return virtualInputHandler.clickVirtualConfirmSelect();
                }
                return false;
            }

            case Input.Keys.UP:
            {
                virtualInputHandler.setVirtualInputType(VirtualInputType.KEYBOARD_SELECT);
                return virtualInputHandler.moveVirtualConfirmSelectUp();
            }

            case Input.Keys.DOWN:
            {
                virtualInputHandler.setVirtualInputType(VirtualInputType.KEYBOARD_SELECT);
                return virtualInputHandler.moveVirtualConfirmSelectDown();
            }

            case Input.Keys.LEFT:
            {
                virtualInputHandler.setVirtualInputType(VirtualInputType.KEYBOARD_SELECT);
                return virtualInputHandler.moveVirtualConfirmSelectLeft();
            }

            case Input.Keys.RIGHT:
            {
                virtualInputHandler.setVirtualInputType(VirtualInputType.KEYBOARD_SELECT);
                return virtualInputHandler.moveVirtualConfirmSelectRight();
            }

            case Input.Keys.F3:
            {     // 空实现，保留原有占位
                return true;
            }

            case Input.Keys.F11:
            {
                return UniversalInputHandlerFunction.handleToggleFullscreen();
            }

            case Input.Keys.F12:
            {
                LogUtils.info(KeyboardInputHandler.class, "当前帧数 (FPS): " + Gdx.graphics.getFramesPerSecond());
                return true;
            }
            default:
                return false;
        }
    }
}
