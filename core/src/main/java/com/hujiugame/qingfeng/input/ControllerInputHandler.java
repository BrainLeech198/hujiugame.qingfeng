package com.hujiugame.qingfeng.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.hujiugame.qingfeng.di.InstanceContent;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.VirtualInputType;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.util.StringPolisher;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class ControllerInputHandler implements ControllerListener
{
    private final VirtualInputHandler virtualInputHandler;

    private final Stage stage;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final GameHost gameHost;

    // 当前摇杆值（由 axisMoved 实时更新）
    private final Vector2 leftAxis = new Vector2(0, 0);
    private final Vector2 rightAxis = new Vector2(0, 0);

    // 虚拟鼠标相关
    private final int virtualMouseWidth = 64;
    private final int virtualMouseHeight = 64;
    private float virtualMouseSensitivity = 900f; // 像素/秒

    private final String virtualMousePictureTag = StringPolisher.polished("virtual_cursor");
    private final FileHandle virtualMousePictureFileHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_RESOURCE_IMAGE, FileName.CONTROLLER_CURSOR));

    private final Vector2 virtualMousePosition = new Vector2(ScreenSize.WIDTH / 2f, ScreenSize.HEIGHT / 2f);
    private final Vector2 virtualMousePicturePosition = new Vector2(0, 0);

    // 按钮键值常量
    private static final int BUTTON_A = 0;
    private static final int BUTTON_B = 1;
    private static final int BUTTON_X = 2;
    private static final int BUTTON_Y = 3;
    private static final int BUTTON_BACK = 4;
    private static final int BUTTON_MENU = 5;
    private static final int BUTTON_START = 6;
    private static final int BUTTON_LS = 7;
    private static final int BUTTON_RS = 8;
    private static final int BUTTON_LB = 9;
    private static final int BUTTON_RB = 10;
    private static final int BUTTON_UP = 11;
    private static final int BUTTON_DOWN = 12;
    private static final int BUTTON_LEFT = 13;
    private static final int BUTTON_RIGHT = 14;

    // 功能映射
    private static final int CYCLE_MODE_KEY = BUTTON_START;

    private static final int MOVE_VIRTUAL_INDEX_UP = BUTTON_UP;
    private static final int MOVE_VIRTUAL_INDEX_DOWN = BUTTON_DOWN;
    private static final int MOVE_VIRTUAL_INDEX_LEFT = BUTTON_LEFT;
    private static final int MOVE_VIRTUAL_INDEX_RIGHT = BUTTON_RIGHT;

    private static final int CONFIRM_KEY = BUTTON_A;
    private static final int CANCEL_KEY = BUTTON_B;

    private static final int FULLSCREEN_KEY = BUTTON_RB;


    /**
     * 构造手柄输入处理器
     *
     * @param instanceContent     实例管理器
     * @param virtualInputHandler 虚拟输入处理器
     */
    public ControllerInputHandler (InstanceContent instanceContent, VirtualInputHandler virtualInputHandler)
    {
        this.virtualInputHandler = virtualInputHandler;
        this.stage = instanceContent.getStage();
        this.graphicsManager = instanceContent.getGraphicsManager();
        this.uiManager = instanceContent.getUiManager();
        this.gameHost = instanceContent.getGameHost();
    }

    /**
     * 必须在游戏主循环的每帧调用，传入帧时间增量（秒），用于平滑移动虚拟鼠标。
     * @param deltaTime 帧时间增量（秒）
     */
    public void update (float deltaTime)
    {
        // 更新虚拟鼠标移动（轮询左摇杆）
        updateVirtualMouse(deltaTime);
    }

    /**
     * 确保虚拟鼠标图片已加载
     */
    private void ensureVirtualMousePicture ()
    {
        // 加载图标
        GraphicsManager graphicsManager;
        if (gameHost.getGameSessionManager().isInGame())
        {
            graphicsManager = gameHost.getPlayLocalData().getGraphicsManager();
        }
        else
        {
            graphicsManager = this.graphicsManager;
        }
        if (!graphicsManager.hasPicture(virtualMousePictureTag))
        {
            graphicsManager.loadPicture(virtualMousePictureTag, virtualMousePictureFileHandle);
        }
    }

    /**
     * 顶层绘制，绘制虚拟鼠标图标
     */
    public void topRender ()
    {
        if (virtualInputHandler.getVirtualInputType() == VirtualInputType.CONTROLLER_VIRTUAL_MOUSE)
        {
            // 确保图标
            ensureVirtualMousePicture();

            // 绘制图标
            GraphicsManager graphicsManager;
            if (gameHost.getGameSessionManager().isInGame())
            {
                graphicsManager = gameHost.getPlayLocalData().getGraphicsManager();
            }
            else
            {
                graphicsManager = this.graphicsManager;
            }
            graphicsManager.putPicture(virtualMousePictureTag, (int) virtualMousePicturePosition.x, (int) virtualMousePicturePosition.y, virtualMouseWidth, virtualMouseHeight);
        }
    }

    // ==================== 虚拟鼠标功能 ====================

    /**
     * 更新虚拟鼠标位置，根据左摇杆输入移动鼠标
     *
     * @param deltaTime 帧时间增量（秒）
     */
    private void updateVirtualMouse (float deltaTime)
    {
        if (virtualInputHandler.getVirtualInputType() != VirtualInputType.CONTROLLER_VIRTUAL_MOUSE) return;

        // 左摇杆映射：水平轴 0，垂直轴 1（需要取负使上推对应鼠标上移）
        float moveX = leftAxis.x;
        float moveY = -leftAxis.y;

        // 死区处理
        if (Math.abs(moveX) < Numeric.Input.STICK_DEAD_ZONE) moveX = 0;
        if (Math.abs(moveY) < Numeric.Input.STICK_DEAD_ZONE) moveY = 0;

        // 移动虚拟鼠标（仅当摇杆有移动时才更新）
        if (moveX != 0 || moveY != 0)
        {
            float dx = moveX * virtualMouseSensitivity * deltaTime;
            float dy = moveY * virtualMouseSensitivity * deltaTime;
            virtualMousePosition.add(dx, dy);
            // 边界裁剪
            virtualMousePosition.x = Math.max(0, Math.min(ScreenSize.WIDTH, virtualMousePosition.x));
            virtualMousePosition.y = Math.max(0, Math.min(ScreenSize.HEIGHT, virtualMousePosition.y));

        }

        // 计算左下角
        virtualMousePicturePosition.set(virtualMousePosition.x - virtualMouseWidth / 2f, virtualMousePosition.y - virtualMouseHeight / 2f);
    }

    /**
     * 轮换输入模式：NONE → CONTROLLER_SELECT → CONTROLLER_VIRTUAL_MOUSE → NONE
     */
    private void cycleInputMode ()
    {
        VirtualInputType current = virtualInputHandler.getVirtualInputType();
        VirtualInputType next;
        switch (current)
        {
            case NONE:
                next = VirtualInputType.CONTROLLER_SELECT;
                virtualInputHandler.resetVirtualSelectTime();
                break;
            case CONTROLLER_SELECT:
                next = VirtualInputType.CONTROLLER_VIRTUAL_MOUSE;
                break;
            default:
                next = VirtualInputType.NONE;
                break;
        }
        virtualInputHandler.setVirtualInputType(next);
        LogUtils.debug(ControllerInputHandler.class, "cycleInputMode 切换输入模式: " + current + " → " + next);
    }

    // ==================== 业务逻辑 ====================

    /**
     * 处理取消（返回）操作
     * @return 是否成功处理
     */
    private boolean handleCancel ()
    {
        return UniversalInputHandlerFunction.handleEscape(uiManager, uiManager.getMessageBox(), gameHost, UniversalInputType.CONTROLLER);
    }

    /**
     * 处理确认操作
     * @return 是否成功处理
     */
    private boolean handleConfirm ()
    {
        return UniversalInputHandlerFunction.handleEnter(uiManager, uiManager.getMessageBox(), gameHost, UniversalInputType.CONTROLLER);
    }

    /**
     * 处理全屏切换操作
     * @return 是否成功切换
     */
    private boolean handleFullscreen ()
    {
        return UniversalInputHandlerFunction.handleToggleFullscreen();
    }

    /**
     * 处理虚拟鼠标点击操作，检测舞台中点击到的交互对象并触发点击事件
     * @return 是否成功处理
     */
    private boolean handleVirtualClick ()
    {
        try
        {
            // 注意：stage.hit 需要传入的是舞台坐标（Stage coordinates）
            Vector2 stageCords = virtualMousePosition;

            Actor hitActor = stage.hit(stageCords.x, stageCords.y, true);
            if (hitActor == null)
            {
                LogUtils.debug(ControllerInputHandler.class, "handleMainClickInteractableObject 未点击到任何 Actor (virtualMousePosition): " + stageCords);
                return false;
            }

            // 尝试获取实现了 InteractableObject 的 Actor
            InteractableObject interactable = null;
            if (hitActor instanceof InteractableObject)
            {
                interactable = (InteractableObject) hitActor;
            }

            if (interactable != null)
            {
                LogUtils.debug(ControllerInputHandler.class, "handleMainClickInteractableObject 点击到交互对象 (tag): " + interactable.getTag());
                return interactable.setClicked(true);
            }
            else
            {
                LogUtils.debug(ControllerInputHandler.class, "handleMainClickInteractableObject 点击的 Actor 不是 InteractableObject (objectSimpleName): " + hitActor.getClass().getSimpleName());
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ControllerInputHandler.class, "handleMainClickInteractableObject", e);
            return false;
        }
    }

    // ==================== ControllerListener 回调 ====================

    /**
     * 手柄按钮按下事件处理，处理确认、取消、方向选择、虚拟鼠标、全屏等操作。
     * @param controller 手柄控制器
     * @param buttonCode 按钮代码
     * @return 是否已处理该事件
     */
    @Override
    public boolean buttonDown (Controller controller, int buttonCode)
    {
        LogUtils.debug(ControllerInputHandler.class, "buttonDown 按钮按下 (code): " + buttonCode);

        // 轮换输入模式
        if (buttonCode == CYCLE_MODE_KEY)
        {
            cycleInputMode();
            return true;
        }

        VirtualInputType currentType = virtualInputHandler.getVirtualInputType();

        // 默认的按钮处理（NONE 模式）
        if (currentType == VirtualInputType.NONE)
        {
            if (buttonCode == CONFIRM_KEY)
            {
                return handleConfirm();
            }
            else if (buttonCode == CANCEL_KEY)
            {
                return handleCancel();
            }
        }
        // 虚拟鼠标模式
        else if (currentType == VirtualInputType.CONTROLLER_VIRTUAL_MOUSE)
        {
            if (buttonCode == CONFIRM_KEY)
            {
                return handleVirtualClick();
            }
            else if (buttonCode == CANCEL_KEY)
            {
                return handleCancel();
            }
        }
        // 虚拟选择模式
        else if (currentType == VirtualInputType.CONTROLLER_SELECT)
        {
            if (buttonCode == CONFIRM_KEY)
            {
                return virtualInputHandler.clickVirtualConfirmSelect();
            }
            else if (buttonCode == CANCEL_KEY)
            {
                return virtualInputHandler.clickVirtualCancelSelect();
            }
            else if (buttonCode == MOVE_VIRTUAL_INDEX_UP)
            {
                return virtualInputHandler.moveVirtualConfirmSelectUp();
            }
            else if (buttonCode == MOVE_VIRTUAL_INDEX_DOWN)
            {
                return virtualInputHandler.moveVirtualConfirmSelectDown();
            }
            else if (buttonCode == MOVE_VIRTUAL_INDEX_LEFT)
            {
                return virtualInputHandler.moveVirtualConfirmSelectLeft();
            }
            else if (buttonCode == MOVE_VIRTUAL_INDEX_RIGHT)
            {
                return virtualInputHandler.moveVirtualConfirmSelectRight();
            }
        }
        // 抢回控制权（键盘模式下按手柄 A/B 切回手柄模式）
        else if (currentType == VirtualInputType.KEYBOARD_SELECT)
        {
            if (buttonCode == CONFIRM_KEY || buttonCode == CANCEL_KEY)
            {
                virtualInputHandler.setVirtualInputType(VirtualInputType.CONTROLLER_SELECT);
                return true;
            }
        }

        // 全屏功能
        if (buttonCode == FULLSCREEN_KEY)
        {
            return handleFullscreen();
        }

        return true;
    }

    /**
     * 手柄按钮抬起事件
     * @param controller 手柄控制器
     * @param buttonCode 按钮代码
     * @return 是否已处理该事件
     */
    @Override
    public boolean buttonUp (Controller controller, int buttonCode)
    {
        LogUtils.debug(ControllerInputHandler.class, "buttonUp 按钮抬起 (code): " + buttonCode);
        return true;
    }

    /**
     * 手柄摇杆移动事件，更新左右摇杆的轴值
     * @param controller 手柄控制器
     * @param axisCode   轴代码
     * @param value      轴当前值
     * @return 是否已处理该事件
     */
    @Override
    public boolean axisMoved (Controller controller, int axisCode, float value)
    {
        // 仅记录摇杆值，不在此处进行鼠标移动
        if (axisCode == 0)
        {
            leftAxis.x = value;
        }
        else if (axisCode == 1)
        {
            leftAxis.y = value;
        }
        else if (axisCode == 2)
        {
            rightAxis.x = value;
        }
        else if (axisCode == 3)
        {
            rightAxis.y = value;
        }
        return true;
    }

    /**
     * 手柄连接回调
     * @param controller 已连接的手柄控制器
     */
    @Override
    public void connected (Controller controller)
    {
        LogUtils.debug(ControllerInputHandler.class, "手柄已连接: " + controller.getName());
    }

    /**
     * 手柄断开回调
     * @param controller 已断开的手柄控制器
     */
    @Override
    public void disconnected (Controller controller)
    {
        LogUtils.debug(ControllerInputHandler.class, "手柄已断开: " + controller.getName());
    }
}
