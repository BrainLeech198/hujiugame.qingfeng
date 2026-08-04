package com.hujiugame.qingfeng.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.di.InstanceContent;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.VirtualInputType;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.RequirementKey;
import com.hujiugame.qingfeng.type.key.UniversalUiKey;
import com.hujiugame.qingfeng.ui.MessageBox;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.util.StringPolisher;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public class VirtualInputHandler
{
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final GameHost gameHost;

    private VirtualInputType virtualInputType = VirtualInputType.NONE;
    private static final double DEFAULT_REMAINING_VIRTUAL_SELECT_TIME = 5.0;
    private double lastVirtualSelectTime = System.currentTimeMillis()/1000.0;
    private double remainingVirtualSelectTime;

    private HashSet<InteractableObject> interactableObjectSet = new HashSet<>();
    private final List<List<InteractableObject>> interactableObjectMap = new LinkedList<>();
    private static final float ROW_GROUP_ABSOLUTE_THRESHOLD = 30f;
    private static final float VIRTUAL_SELECT_SCALE = 0.6f;
    private static final float VIRTUAL_RECT_SIZE = 4f;

    private int virtualConfirmSelectRaw = -1;
    private int virtualConfirmSelectCol = -1;

    private InteractableObject confirmSelectObject;
    private InteractableObject lastConfirmSelectObject;
    private float virtualConfirmSelectBottom;
    private float virtualConfirmSelectLeft;
    private float virtualConfirmSelectWidth;
    private float virtualConfirmSelectHeight;
    private List<List<Float>> drawConfirmInfoList = new LinkedList<>();

    private InteractableObject cancelSelectObject;
    private InteractableObject lastCancelSelectObject;
    private InteractableObject prioritySelectObject;
    private float virtualCancelSelectBottom;
    private float virtualCancelSelectLeft;
    private float virtualCancelSelectWidth;
    private float virtualCancelSelectHeight;
    private List<List<Float>> drawCancelInfoList = new LinkedList<>();

    private final String controllerConfirmPictureTag = StringPolisher.polished("virtual_input_controller_confirm");
    private final String controllerCancelPictureTag = StringPolisher.polished("virtual_input_controller_cancel");
    private final String controllerConfirmCancelPictureTag = StringPolisher.polished("virtual_input_controller_confirm_cancel");

    private final String keyboardConfirmPictureTag = StringPolisher.polished("virtual_input_keyboard_confirm");
    private final String keyboardCancelPictureTag = StringPolisher.polished("virtual_input_keyboard_cancel");
    private final String keyboardConfirmCancelPictureTag = StringPolisher.polished("virtual_input_keyboard_confirm_cancel");

    private final String virtualConfirmRectPictureTag = StringPolisher.polished("virtual_confirm_rect");
    private final String virtualCancelRectPictureTag = StringPolisher.polished("virtual_cancel_rect");

    private final List<String> virtualInputPictureTagList;
    private final List<FileHandle> virtualInputPictureFileList;

    /**
     * 构造虚拟输入处理器，初始化图片资源标签和文件句柄列表
     *
     * @param instanceContent 实例管理器，用于获取各个管理器组件
     */
    public VirtualInputHandler (InstanceContent instanceContent)
    {
        this.graphicsManager = instanceContent.getGraphicsManager();
        this.uiManager = instanceContent.getUiManager();
        this.gameHost = instanceContent.getGameHost();

        // 图片资源
        FileHandle externalImageDirectory = Gdx.files.internal(PathName.ASSET_S_RESOURCE_IMAGE);

        FileHandle controllerConfirmPictureFileHandle = externalImageDirectory.child(FileName.CONTROLLER_BUTTON_A);
        FileHandle controllerCancelPictureFileHandle = externalImageDirectory.child(FileName.CONTROLLER_BUTTON_B);
        FileHandle controllerConfirmCancelPictureFileHandle = externalImageDirectory.child(FileName.CONTROLLER_BUTTON_A_OR_B);

        FileHandle keyboardConfirmPictureFileHandle = externalImageDirectory.child(FileName.KEYBOARD_BUTTON_ENTER);
        FileHandle keyboardCancelPictureFileHandle = externalImageDirectory.child(FileName.KEYBOARD_BUTTON_ESCAPE);
        FileHandle keyboardConfirmCancelPictureFileHandle = externalImageDirectory.child(FileName.KEYBOARD_BUTTON_ENTER_OR_ESCAPE);

        FileHandle virtualConfirmRectPictureFileHandle = externalImageDirectory.child(FileName.VIRTUAL_CONFIRM_RECT);
        FileHandle virtualCancelRectPictureFileHandle = externalImageDirectory.child(FileName.VIRTUAL_CANCEL_RECT);

        virtualInputPictureTagList = Arrays.asList(
            controllerConfirmPictureTag,
            controllerCancelPictureTag,
            keyboardConfirmPictureTag,
            keyboardCancelPictureTag,
            keyboardConfirmCancelPictureTag,
            controllerConfirmCancelPictureTag,
            virtualConfirmRectPictureTag,
            virtualCancelRectPictureTag
        );

        virtualInputPictureFileList = Arrays.asList(
            controllerConfirmPictureFileHandle,
            controllerCancelPictureFileHandle,
            keyboardConfirmPictureFileHandle,
            keyboardCancelPictureFileHandle,
            keyboardConfirmCancelPictureFileHandle,
            controllerConfirmCancelPictureFileHandle,
            virtualConfirmRectPictureFileHandle,
            virtualCancelRectPictureFileHandle
        );

    }

    /**
     * 获取当前虚拟输入类型
     *
     * @return 当前虚拟输入类型
     */
    public VirtualInputType getVirtualInputType ()
    {
        return virtualInputType;
    }

    /**
     * 设置当前虚拟输入类型
     * @param virtualInputType 要设置的虚拟输入类型
     */
    public void setVirtualInputType (VirtualInputType virtualInputType)
    {
        LogUtils.debug(VirtualInputHandler.class, "setVirtualInputType 设置虚拟输入类型: " + virtualInputType);
        this.virtualInputType = virtualInputType;
    }

    /**
     * 设置页面级优先选中对象。当进入新页面时，如果原确认选中对象已消失，
     * 将自动选中此对象。
     *
     * @param object 优先选中的交互对象，传 null 取消
     */
    public void setPrioritySelectObject (InteractableObject object)
    {
        prioritySelectObject = object;
    }

    /**
     * 根据页面配置设置优先选中对象。从 configJson 中读取 priorityConfirmUi 配置，
     * 按 tag 匹配交互对象并设为优先选中。
     *
     * @param configJson 页面配置数据，包含 priorityConfirmUi.type + tag 字段
     */
    public void setPriorityConfirmSelectObject (JsonEntity configJson)
    {
        try
        {
            if (configJson == null || !configJson.containsKey(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI))
            {
                return;
            }

            // 优先选中配置json
            JsonEntity priorityConfig = configJson.getJsonEntityByKey(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI);

            // 优先选中控件解析
            String type = priorityConfig.containsKey(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI_TYPE)
                ? priorityConfig.getString(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI_TYPE) : "";
            String tag = priorityConfig.containsKey(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI_TAG)
                ? priorityConfig.getString(RequirementKey.Config.UNIVERSAL_PRIORITY_CONFIRM_UI_TAG) : "";

            // 配置格式校验
            if (!"tag".equals(type) || tag.isEmpty())
            {
                LogUtils.error(VirtualInputHandler.class, "setPriorityConfirmSelectObject 配置格式错误 (type): " + type + " (tag): " + tag);
                return;
            }

            // 从 UiManager 全量交互对象集合中按 tag 查找
            for (InteractableObject obj : uiManager.getInteractableObjectSet())
            {
                if (tag.equals(obj.getTag()))
                {
                    setPrioritySelectObject(obj);
                    LogUtils.debug(VirtualInputHandler.class,
                        "setPriorityConfirmSelectObject 配置驱动优先选中 (tag): " + tag + " (obj): " + obj);
                    return;
                }
            }

            LogUtils.debug(VirtualInputHandler.class, "setPriorityConfirmSelectObject 未找到匹配 (tag): " + tag);
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "setPriorityConfirmSelectObject", e);
        }
    }

    /**
     * 重置虚拟选择剩余时间
     */
    public void resetVirtualSelectTime ()
    {
        remainingVirtualSelectTime = DEFAULT_REMAINING_VIRTUAL_SELECT_TIME;
        lastVirtualSelectTime = System.currentTimeMillis()/1000.0;
        LogUtils.debug(VirtualInputHandler.class, "resetVirtualSelectTime 重置虚拟输入时间: " + remainingVirtualSelectTime);
    }

    /**
     * 移动虚拟确认选择索引
     * @param deltaRaw 行偏移量
     * @param deltaCol 列偏移量
     * @return 是否移动成功
     */
    private boolean moveVirtualConfirmSelect (int deltaRaw, int deltaCol)
    {
        try
        {
            // 如果列表为空，则返回失败
            if (interactableObjectMap.isEmpty()) return false;

            // 如果当前索引为空，则初始化为第一个
            // 如果没有启用虚拟选择索引，则初始化为第一个
            if (virtualConfirmSelectRaw == -1 || virtualConfirmSelectCol == -1 || virtualInputType == VirtualInputType.NONE)
            {
                if (!interactableObjectMap.get(0).isEmpty())
                {
                    virtualConfirmSelectRaw = 0;
                    virtualConfirmSelectCol = 0;
                    confirmSelectObject = interactableObjectMap.get(0).get(0);
                    LogUtils.debug(VirtualInputHandler.class, "moveVirtualIndex 默认选中第一个 (x): 0 (y): 0 (obj): " + confirmSelectObject);
                    return true;
                }
                else
                {
                    confirmSelectObject = null;
                    LogUtils.debug(VirtualInputHandler.class, "moveVirtualIndex 无选择对象");
                    return false;
                }
            }

            // 获取当前索引
            int newRaw = virtualConfirmSelectRaw + deltaRaw;
            int newCol = virtualConfirmSelectCol + deltaCol;

            // 行边界处理
            if (newRaw < 0 || newRaw >= interactableObjectMap.size())
            {
                newRaw = newRaw < 0 ? interactableObjectMap.size() - 1 : 0;
            }

            // 获取目标行
            List<InteractableObject> targetRow = interactableObjectMap.get(newRaw);
            if (targetRow.isEmpty())
            {
                LogUtils.error(VirtualInputHandler.class, "moveVirtualIndex 错误的行信息 (map): " + interactableObjectMap + " (raw): " + newRaw);
            }

            // 列边界处理
            if (newCol < 0 || newCol >= targetRow.size())
            {
                if (newCol < 0)
                {
                    if (newRaw > 0)
                    {
                        newRaw--;
                    }
                    else
                    {
                        newRaw = interactableObjectMap.size() - 1;
                    }
                    targetRow = interactableObjectMap.get(newRaw);
                    newCol = targetRow.size() - 1;
                }
                else
                {
                    if (newRaw < interactableObjectMap.size() - 1)
                    {
                        newRaw++;
                    }
                    else
                    {
                        newRaw = 0;
                    }
                    targetRow = interactableObjectMap.get(newRaw);
                    newCol = 0;
                }
            }

            if (targetRow == null)
            {
                // 如果行不存在，则向上移动
                moveVirtualConfirmSelect(-1, 0);
            }
            else if (targetRow.get(newCol) == null)
            {
                // 如果列不存在，则向左移动
                moveVirtualConfirmSelect(0, -1);
            }

            // 更新虚拟确认选择索引
            virtualConfirmSelectRaw = newRaw;
            virtualConfirmSelectCol = newCol;
            if (targetRow != null) confirmSelectObject = targetRow.get(newCol);
            else confirmSelectObject = null;

            LogUtils.debug(VirtualInputHandler.class, "moveVirtualIndex 移动成功 " +
                " (x): " + virtualConfirmSelectCol +
                " (y): " + virtualConfirmSelectRaw +
                " (obj): " + confirmSelectObject);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "moveVirtualIndex", e);
            return false;
        }
    }

    /**
     * 向上移动虚拟确认选择
     * @return 是否移动成功
     */
    public boolean moveVirtualConfirmSelectUp ()
    {
        resetVirtualSelectTime();
        return moveVirtualConfirmSelect(-1, 0);
    }

    /**
     * 向下移动虚拟确认选择
     * @return 是否移动成功
     */
    public boolean moveVirtualConfirmSelectDown ()
    {
        resetVirtualSelectTime();
        return moveVirtualConfirmSelect(1, 0);
    }

    /**
     * 向左移动虚拟确认选择
     * @return 是否移动成功
     */
    public boolean moveVirtualConfirmSelectLeft ()
    {
        resetVirtualSelectTime();
        return moveVirtualConfirmSelect(0, -1);
    }

    /**
     * 向右移动虚拟确认选择
     * @return 是否移动成功
     */
    public boolean moveVirtualConfirmSelectRight ()
    {
        resetVirtualSelectTime();
        return moveVirtualConfirmSelect(0, 1);
    }

    /**
     * 点击当前虚拟确认选择对象
     * @return 是否点击成功
     */
    public boolean clickVirtualConfirmSelect ()
    {
        try
        {
            // 更新虚拟选择时间
            resetVirtualSelectTime();
            if (confirmSelectObject != null)
            {
                LogUtils.debug(VirtualInputHandler.class, "clickVirtualConfirmSelect 点击虚拟确认选择: " + confirmSelectObject);
                return confirmSelectObject.setClicked(true);
            }
            else
            {
                LogUtils.debug(VirtualInputHandler.class, "clickVirtualConfirmSelect null");
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "clickVirtualConfirmSelect", e);
            return false;
        }
    }

    /**
     * 点击当前虚拟取消选择对象
     * @return 是否点击成功
     */
    public boolean clickVirtualCancelSelect ()
    {
        try
        {
            // 更新虚拟选择时间
            resetVirtualSelectTime();
            if (cancelSelectObject != null)
            {
                LogUtils.debug(VirtualInputHandler.class, "clickVirtualCancelSelect 点击虚拟取消选择: " + cancelSelectObject);
                return cancelSelectObject.setClicked(true);
            }
            else
            {
                LogUtils.debug(VirtualInputHandler.class, "clickVirtualCancelSelect null");
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "clickVirtualCancelSelect", e);
            return false;
        }
    }

    /**
     * 每帧更新虚拟输入状态，包含时间计算、交互对象刷新、活动对象检测和位置更新
     */
    public void update ()
    {
        // 计算剩余时间
        calculateRemainingVirtualSelectTime();
        // 刷新交互对象地图
        refreshInteractableObjectMap();
        // 检测活动对象
        detectActiveInteractableObjects();
        // 更新虚拟输入图片位置
        updateVirtualInputPictureRect();
    }

    /**
     * 计算虚拟选择的剩余时间，超时后自动退出虚拟选择模式
     */
    private void calculateRemainingVirtualSelectTime ()
    {
        // 如果处于虚拟选择模式
        if (virtualInputType == VirtualInputType.CONTROLLER_SELECT
            || virtualInputType == VirtualInputType.KEYBOARD_SELECT)
        {
            // 计算时间间隔
            double deltaTime = System.currentTimeMillis()/1000.0 - lastVirtualSelectTime;
            if (remainingVirtualSelectTime <= deltaTime) remainingVirtualSelectTime = 0;
            else remainingVirtualSelectTime -= deltaTime;
            lastVirtualSelectTime = System.currentTimeMillis()/1000.0;

            // 如果已超时
            if (remainingVirtualSelectTime <= 0)
            {
                LogUtils.debug(VirtualInputHandler.class, "calculateRemainingVirtualSelectTime 虚拟选择时间已到 自动退出");
                virtualInputType = VirtualInputType.NONE;
            }
        }
    }

    /**
     * 将可见的交互对象按行分组，每行内按水平位置排序
     * @param visibleObjects 可见交互对象列表
     * @param rowThreshold   行合并阈值，垂直中心距离小于此值视为同一行
     * @return 按行分组的交互对象列表
     */
    private List<List<InteractableObject>> getLists (List<InteractableObject> visibleObjects, float rowThreshold)
    {
        // 如果可见对象列表为空
        if (visibleObjects == null || visibleObjects.isEmpty())
        {
            return new ArrayList<>();
        }

        // 创建行列表
        List<List<InteractableObject>> rows = new ArrayList<>();
        List<InteractableObject> currentRow = null;
        float currentRowTopY = 0;

        // 遍历可见对象列表
        for (InteractableObject obj : visibleObjects)
        {
            // 获取对象中心Y坐标
            float objCenterY = (obj.getRectTop() + obj.getRectBottom()) / 2;
            // 如果当前行为空
            if (currentRow == null)
            {
                // 创建行
                currentRow = new ArrayList<>();
                currentRow.add(obj);
                currentRowTopY = objCenterY;
                continue;
            }
            // 判断是否属于同一行
            if (Math.abs(objCenterY - currentRowTopY) <= rowThreshold)
            {
                currentRow.add(obj);
            }
            // 否则创建新行
            else
            {
                // 排序
                currentRow.sort((a, b) -> Float.compare(a.getRectLeft(), b.getRectLeft()));
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                currentRow.add(obj);
                currentRowTopY = objCenterY;
            }
        }
        // 添加最后一行
        if (currentRow != null && !currentRow.isEmpty())
        {
            // 排序
            currentRow.sort((a, b) -> Float.compare(a.getRectLeft(), b.getRectLeft()));
            rows.add(currentRow);
        }
        return rows;
    }

    /**
     * 刷新确认选择对象，优先选择弹窗确认按钮
     */
    private void refreshConfirmSelectObject ()
    {
        try
        {
            // 获取UI管理器
            UiManager uiManager;
            if (gameHost.getGameSessionManager().isInGame())
            {
                uiManager = gameHost.getPlayLocalData().getUiManager();
            }
            else
            {
                uiManager = this.uiManager;
            }

            // 弹窗优先级
            MessageBox messageBox = uiManager.getMessageBox();
            {
                if (messageBox.getEnterButtonTag() != null)
                {
                    // 获取可交互状态的确认型按钮控件对象
                    InteractableObject enterButton = uiManager.getButton(messageBox.getEnterButtonTag());
                    if (enterButton != null)
                    {
                        // 设置确认选择对象
                        confirmSelectObject = enterButton;
                        LogUtils.debug(VirtualInputHandler.class, "refreshConfirmSelectObject confirm弹窗按钮 (obj): " + enterButton);

                        // 遍历获取该对象的坐标
                        if (interactableObjectMap.get(virtualConfirmSelectRaw).get(virtualConfirmSelectCol) != confirmSelectObject)
                        {
                            for (int i = 0; i < interactableObjectMap.size(); i++)
                            {
                                for (int j = 0; j < interactableObjectMap.get(i).size(); j++)
                                {
                                    if (interactableObjectMap.get(i).get(j) == confirmSelectObject)
                                    {
                                        virtualConfirmSelectRaw = i;
                                        virtualConfirmSelectCol = j;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "refreshConfirmSelectObject", e);
        }
    }

    /**
     * 刷新取消选择对象，优先选择弹窗返回按钮，然后是返回/退出按钮
     */
    private void refreshCancelSelectObject ()
    {
        try
        {
            // 获取UI管理器
            UiManager uiManager;
            if (gameHost.getGameSessionManager().isInGame())
            {
                uiManager = gameHost.getPlayLocalData().getUiManager();
            }
            else
            {
                uiManager = this.uiManager;
            }

            // 弹窗优先级
            MessageBox messageBox = uiManager.getMessageBox();
            {
                InteractableObject escapeButton = uiManager.getButton(messageBox.getEscapeButtonTag());
                if (escapeButton != null)
                {
                    cancelSelectObject = escapeButton;
                    LogUtils.debug(VirtualInputHandler.class, "refreshCancelSelectObject cancel弹窗返回按钮 (obj): " + cancelSelectObject);
                    return;
                }
            }

            // 返回/退出按钮优先级
            if (uiManager.containsButton(UniversalUiKey.BUTTON_BACK))
            {
                InteractableObject backButton = uiManager.getButton(UniversalUiKey.BUTTON_BACK);
                if (backButton != null)
                {
                    cancelSelectObject = backButton;
                    LogUtils.debug(VirtualInputHandler.class, "refreshCancelSelectObject cancel返回按钮 (obj): " + cancelSelectObject);
                    return;
                }
            }

            // 退出按钮优先级
            if (uiManager.containsButton(UniversalUiKey.BUTTON_QUIT))
            {
                InteractableObject quitButton = uiManager.getButton(UniversalUiKey.BUTTON_QUIT);
                if (quitButton != null)
                {
                    cancelSelectObject = quitButton;
                    LogUtils.debug(VirtualInputHandler.class, "refreshCancelSelectObject cancel退出按钮 (obj): " + cancelSelectObject);
                    return;
                }
            }

            // 无优先级按钮，保留 tryToKeepSameCancelObject 的结果
            if (cancelSelectObject != null) return;
            cancelSelectObject = null;
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "refreshCancelSelectObject", e);
        }
    }

    /**
     * 交互对象集合刷新后，尝试保持之前在网格中选中的确认对象不变，避免选中框跳跃
     *
     * @return 保持成功返回 true
     */
    private boolean tryToKeepSameSelectObject ()
    {
        if (confirmSelectObject == null)
        {
            LogUtils.debug(VirtualInputHandler.class, "tryToKeepSameSelectObject 当前无选中对象");
            return false;
        }
        if (interactableObjectMap.isEmpty())
        {
            LogUtils.debug(VirtualInputHandler.class, "tryToKeepSameSelectObject 交互对象列表为空");
            return false;
        }
        for (int i = 0; i < interactableObjectMap.size(); i++)
        {
            List<InteractableObject> row = interactableObjectMap.get(i);
            for (int j = 0; j < row.size(); j++)
            {
                // 用 == 引用比较找原对象在新 map 中的位置
                if (row.get(j) == confirmSelectObject)
                {
                    virtualConfirmSelectRaw = i;
                    virtualConfirmSelectCol = j;
                    LogUtils.debug(VirtualInputHandler.class,
                        "tryToKeepSameSelectObject 保持选中 (raw): " + i + " (col): " + j + " (obj): " + confirmSelectObject);
                    return true;
                }
            }
        }
        LogUtils.debug(VirtualInputHandler.class, "tryToKeepSameSelectObject 原对象已不在新列表中，走默认选中");
        return false;
    }

    /**
     * 确认对象丢失后，尝试选中预设的优先对象
     */
    private void tryPrioritySelectObject ()
    {
        if (prioritySelectObject == null) return;

        // 在网格中找到优先对象
        for (int i = 0; i < interactableObjectMap.size(); i++)
        {
            List<InteractableObject> row = interactableObjectMap.get(i);
            for (int j = 0; j < row.size(); j++)
            {
                if (row.get(j) == prioritySelectObject)
                {
                    confirmSelectObject = prioritySelectObject;
                    prioritySelectObject = null; // 消耗型，仅一次有效
                    virtualConfirmSelectRaw = i;
                    virtualConfirmSelectCol = j;
                    LogUtils.debug(VirtualInputHandler.class,
                        "tryPrioritySelectObject 选中优先对象 (raw): " + i + " (col): " + j);
                    return;
                }
            }
        }
    }

    /**
     * 交互对象集合刷新后，尝试保持之前在网格中选中的取消对象不变，避免选中框跳跃
     *
     * @return 保持成功返回 true
     */
    private boolean tryToKeepSameCancelObject ()
    {
        if (cancelSelectObject == null)
        {
            return false;
        }
        if (interactableObjectMap.isEmpty())
        {
            cancelSelectObject = null;
            return false;
        }
        for (int i = 0; i < interactableObjectMap.size(); i++)
        {
            List<InteractableObject> row = interactableObjectMap.get(i);
            for (int j = 0; j < row.size(); j++)
            {
                // 用 == 引用比较找原对象在新 map 中的位置
                if (row.get(j) == cancelSelectObject)
                {
                    LogUtils.debug(VirtualInputHandler.class,
                        "tryToKeepSameCancelObject 保持取消选中 (raw): " + i + " (col): " + j + " (obj): " + cancelSelectObject);
                    return true;
                }
            }
        }
        // 原对象已不在新列表中
        cancelSelectObject = null;
        LogUtils.debug(VirtualInputHandler.class, "tryToKeepSameCancelObject 原对象已不在新列表中");
        return false;
    }

    /**
     * 刷新确认和取消选择对象
     */
    private void refreshSelectObject ()
    {
        try
        {
            // 尽量保证确认选择对象和刷新之前一致
            // 保持失败时再尝试优先对象
            if (!tryToKeepSameSelectObject()) tryPrioritySelectObject();
            // 空移动刷新确认选中
            moveVirtualConfirmSelect(0, 0);
            // 设置快捷确认（优先级覆盖）
            refreshConfirmSelectObject();

            // 尽量保证取消选择对象和刷新之前一致
            tryToKeepSameCancelObject();
            // 设置快捷取消（优先级覆盖）
            refreshCancelSelectObject();
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "updateSelectObject", e);
        }
    }

    /**
     * 刷新可交互对象映射表，将当前可见的交互对象按行分组
     */
    public void refreshInteractableObjectMap ()
    {
        try
        {
            // 获取可见交互对象
            HashSet<InteractableObject> interactableObjectSet;
            if (gameHost.getGameSessionManager().isInGame())
            {
                interactableObjectSet = gameHost.getPlayLocalData().getUiManager().getInteractableObjectSet();
            }
            else
            {
                interactableObjectSet = uiManager.getInteractableObjectSet();
            }

            // 判断是否需要刷新 判断两个集合是否相等
            if (this.interactableObjectSet.equals(interactableObjectSet))
            {
                return;
            }
            else
            {
                this.interactableObjectSet = new HashSet<>(interactableObjectSet);
            }

            // 可交互对象集合为空 即无可交互对象 也不操作
            if (interactableObjectSet.isEmpty())
            {
                confirmSelectObject = null;
                cancelSelectObject = null;
                interactableObjectMap.clear();
                return;
            }

            // 获取可见交互对象集合
            List<InteractableObject> visibleObjects = new ArrayList<>();
            for (InteractableObject obj : interactableObjectSet)
            {
                // 筛选可见交互对象
                if (obj.isShown())
                {
                    visibleObjects.add(obj);
                }
            }

            // 可交互对象集合为空 即无可见交互对象 也不操作
            if (visibleObjects.isEmpty())
            {
                interactableObjectMap.clear();
                return;
            }

            // 此时集合和上次集合不一致

            // 按行分组
            visibleObjects.sort((a, b) ->
            {
                float centerA = (a.getRectTop() + a.getRectBottom()) / 2;
                float centerB = (b.getRectTop() + b.getRectBottom()) / 2;
                return Float.compare(centerB, centerA);
            });

            // 获取行分组
            List<List<InteractableObject>> rows = getLists(visibleObjects, ROW_GROUP_ABSOLUTE_THRESHOLD);

            // 上载行分组
            interactableObjectMap.clear();
            interactableObjectMap.addAll(rows);

            // 刷新选择对象
            refreshSelectObject();

            LogUtils.debug(VirtualInputHandler.class, "refreshInteractableObjectMap 刷新交互对象表成功 (map): " + interactableObjectMap);
        }
        catch (Exception e)
        {
            LogUtils.error(VirtualInputHandler.class, "refreshInteractableObjectMap", e);
        }
    }

    /**
     * 检查交互对象是否在当前交互对象集合中
     * @param interactableObject 待检查的交互对象
     * @return 是否在集合中
     */
    private boolean isInteractableObjectInInteractableObjectSet (InteractableObject interactableObject)
    {
        return interactableObjectSet.contains(interactableObject);
    }

    /**
     * 检查交互对象是否在当前选择网格中（引用比较）
     */
    private boolean isInGrid (InteractableObject target)
    {
        if (target == null) return false;
        for (List<InteractableObject> row : interactableObjectMap)
        {
            for (InteractableObject obj : row)
            {
                if (obj == target) return true;
            }
        }
        return false;
    }

    /**
     * 检测活动的交互对象，清除已不在集合中的选择对象
     */
    private void detectActiveInteractableObjects ()
    {
        if (!isInteractableObjectInInteractableObjectSet(confirmSelectObject)) confirmSelectObject = null;
        if (!isInteractableObjectInInteractableObjectSet(cancelSelectObject)) cancelSelectObject = null;
    }

    /**
     * 更新虚拟输入选择框的图片位置和尺寸信息
     */
    private void updateVirtualInputPictureRect ()
    {
        if (interactableObjectMap.isEmpty())
        {
            return;
        }

        for (int i = 0; i < 2; i++)
        {
            InteractableObject interactableObject;
            float selectLeft, selectBottom;
            float rectLeft, rectRight, rectTop, rectBottom;
            float selectWidth, selectHeight;

            if (i == 0)
            {
                if (lastConfirmSelectObject == confirmSelectObject) continue;
                interactableObject = confirmSelectObject;
                lastConfirmSelectObject = confirmSelectObject;
            }
            else
            {
                if (lastCancelSelectObject == cancelSelectObject) continue;
                interactableObject = cancelSelectObject;
                lastCancelSelectObject = cancelSelectObject;
            }

            if (interactableObject == null) continue;

            rectLeft = interactableObject.getRectLeft();
            rectRight = interactableObject.getRectRight();
            rectTop = interactableObject.getRectTop();
            rectBottom = interactableObject.getRectBottom();

            selectWidth = rectBottom - rectTop;
            boolean badRightDoLeft = (rectRight + selectWidth > ScreenSize.WIDTH);
            float virtualSelectGap = (selectWidth * (1 - VIRTUAL_SELECT_SCALE)) / 2f;
            if (badRightDoLeft)
            {
                selectLeft = rectLeft - selectWidth + virtualSelectGap;
            }
            else
            {
                selectLeft = rectRight + virtualSelectGap;
            }
            selectBottom = rectTop + virtualSelectGap;
            selectWidth *= VIRTUAL_SELECT_SCALE;
            selectHeight = selectWidth;

            if (i == 0)
            {
                virtualConfirmSelectLeft = selectLeft;
                virtualConfirmSelectBottom = selectBottom;
                virtualConfirmSelectWidth = selectWidth;
                virtualConfirmSelectHeight = selectHeight;

                float virtualConfirmRectLeft = rectLeft;
                float virtualConfirmRectTop = rectBottom;
                float virtualConfirmRectRight = rectRight;
                float virtualConfirmRectBottom = rectTop;

                float virtualConfirmRectWidth = virtualConfirmRectRight - virtualConfirmRectLeft + (VIRTUAL_RECT_SIZE - 1);
                float virtualConfirmRectHeight = virtualConfirmRectTop - virtualConfirmRectBottom + (VIRTUAL_RECT_SIZE - 1);

                drawConfirmInfoList = new LinkedList<>();
                drawConfirmInfoList.add(Arrays.asList(virtualConfirmRectLeft, virtualConfirmRectBottom, VIRTUAL_RECT_SIZE, virtualConfirmRectHeight));
                drawConfirmInfoList.add(Arrays.asList(virtualConfirmRectLeft, virtualConfirmRectBottom, virtualConfirmRectWidth, VIRTUAL_RECT_SIZE));
                drawConfirmInfoList.add(Arrays.asList(virtualConfirmRectRight, virtualConfirmRectBottom, VIRTUAL_RECT_SIZE, virtualConfirmRectHeight));
                drawConfirmInfoList.add(Arrays.asList(virtualConfirmRectLeft, virtualConfirmRectTop, virtualConfirmRectWidth, VIRTUAL_RECT_SIZE));
            }
            else
            {
                virtualCancelSelectLeft = selectLeft;
                virtualCancelSelectBottom = selectBottom;
                virtualCancelSelectWidth = selectWidth;
                virtualCancelSelectHeight = selectHeight;

                float virtualCancelRectLeft = rectLeft;
                float virtualCancelRectTop = rectBottom;
                float virtualCancelRectRight = rectRight;
                float virtualCancelRectBottom = rectTop;

                float virtualCancelRectWidth = virtualCancelRectRight - virtualCancelRectLeft + (VIRTUAL_RECT_SIZE - 1);
                float virtualCancelRectHeight = virtualCancelRectTop - virtualCancelRectBottom + (VIRTUAL_RECT_SIZE - 1);

                drawCancelInfoList = new LinkedList<>();
                drawCancelInfoList.add(Arrays.asList(virtualCancelRectLeft, virtualCancelRectBottom, VIRTUAL_RECT_SIZE, virtualCancelRectHeight));
                drawCancelInfoList.add(Arrays.asList(virtualCancelRectLeft, virtualCancelRectBottom, virtualCancelRectWidth, VIRTUAL_RECT_SIZE));
                drawCancelInfoList.add(Arrays.asList(virtualCancelRectRight, virtualCancelRectBottom, VIRTUAL_RECT_SIZE, virtualCancelRectHeight));
                drawCancelInfoList.add(Arrays.asList(virtualCancelRectLeft, virtualCancelRectTop, virtualCancelRectWidth, VIRTUAL_RECT_SIZE));
            }
        }
    }

    /**
     * 确保虚拟输入相关的图片资源已加载
     */
    private void ensureVirtualMousePicture ()
    {
        for (int i = 0; i < virtualInputPictureTagList.size(); i++)
        {
            String tag = virtualInputPictureTagList.get(i);
            FileHandle file = virtualInputPictureFileList.get(i);

            GraphicsManager graphicsManager;
            if (gameHost.getGameSessionManager().isInGame())
            {
                graphicsManager = gameHost.getPlayLocalData().getGraphicsManager();
            }
            else
            {
                graphicsManager = this.graphicsManager;
            }
            if (!graphicsManager.hasPicture(tag))
            {
                graphicsManager.loadPicture(tag, file);
            }
        }
    }

    /**
     * 顶层绘制，绘制虚拟输入选择框和确认/取消图标
     */
    public void topRender ()
    {
        ensureVirtualMousePicture();
        if (virtualInputType != VirtualInputType.NONE)
        {
            String confirmPictureTag = null;
            String cancelPictureTag = null;
            String confirmCancelPictureTag = null;

            if (virtualInputType == VirtualInputType.CONTROLLER_SELECT)
            {
                confirmPictureTag = controllerConfirmPictureTag;
                cancelPictureTag = controllerCancelPictureTag;
                confirmCancelPictureTag = controllerConfirmCancelPictureTag;
            }
            else if (virtualInputType == VirtualInputType.KEYBOARD_SELECT)
            {
                confirmPictureTag = keyboardConfirmPictureTag;
                cancelPictureTag = keyboardCancelPictureTag;
                confirmCancelPictureTag = keyboardConfirmCancelPictureTag;
            }

            if (confirmPictureTag != null && cancelPictureTag != null)
            {
                if (confirmSelectObject == cancelSelectObject)
                {
                    if (confirmSelectObject != null)
                    {

                        for (List<Float> drawInfo : drawConfirmInfoList)
                        {
                            graphicsManager.putPicture(virtualConfirmRectPictureTag,
                                (int) drawInfo.get(0).floatValue(),
                                (int) drawInfo.get(1).floatValue(),
                                (int) drawInfo.get(2).floatValue(),
                                (int) drawInfo.get(3).floatValue());
                        }

                        graphicsManager.putPicture(confirmCancelPictureTag,
                            (int) virtualConfirmSelectLeft,
                            (int) virtualConfirmSelectBottom,
                            (int) virtualConfirmSelectWidth,
                            (int) virtualConfirmSelectHeight);
                    }
                }
                else
                {
                    if (confirmSelectObject != null)
                    {
                        for (List<Float> drawInfo : drawConfirmInfoList)
                        {
                            graphicsManager.putPicture(virtualConfirmRectPictureTag,
                                (int) drawInfo.get(0).floatValue(),
                                (int) drawInfo.get(1).floatValue(),
                                (int) drawInfo.get(2).floatValue(),
                                (int) drawInfo.get(3).floatValue());
                        }

                        graphicsManager.putPicture(confirmPictureTag,
                            (int) virtualConfirmSelectLeft,
                            (int) virtualConfirmSelectBottom,
                            (int) virtualConfirmSelectWidth,
                            (int) virtualConfirmSelectHeight);
                    }
                    if (cancelSelectObject != null)
                    {
                        for (List<Float> drawInfo : drawCancelInfoList)
                        {
                            graphicsManager.putPicture(virtualCancelRectPictureTag,
                                (int) drawInfo.get(0).floatValue(),
                                (int) drawInfo.get(1).floatValue(),
                                (int) drawInfo.get(2).floatValue(),
                                (int) drawInfo.get(3).floatValue());
                        }

                        graphicsManager.putPicture(cancelPictureTag,
                            (int) virtualCancelSelectLeft,
                            (int) virtualCancelSelectBottom,
                            (int) virtualCancelSelectWidth,
                            (int) virtualCancelSelectHeight);
                    }
                }
            }
        }
    }
}
