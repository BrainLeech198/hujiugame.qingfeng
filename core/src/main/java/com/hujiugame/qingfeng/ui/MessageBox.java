package com.hujiugame.qingfeng.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.ui.BoxType;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.ui.dialog.BoxAsk;
import com.hujiugame.qingfeng.ui.dialog.BoxInfo;
import com.hujiugame.qingfeng.manager.TextManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.StringPolisher;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class MessageBox
{
    private final Stage stage;
    // 在显示的弹窗类型
    private final Stack<BoxType> showingBoxTypeStack = new Stack<>();
    private final Stack<String> escapeButtonTagStack = new Stack<>();
    private final Stack<String> enterButtonTagStack = new Stack<>();
    // box 存储
    private final Map<String, BoxAsk> askMap = new HashMap<>();
    private final Map<String, BoxInfo> infoMap = new HashMap<>();
    // box 通用参数
    private final int boxWidth = 900;
    private final int boxHeight = 600;
    private final int boxPositionX = (ScreenSize.WIDTH - boxWidth) / 2;
    private final int boxPositionY = (ScreenSize.HEIGHT - boxHeight) / 2;
    private final int boxTitleHeight = (int) (120f / 600 * boxHeight);
    private final int boxTitleContentGap = (int) (10f / 600 * boxHeight);
    // ask box 参数
    private final int ASK_BOX_BUTTON_COUNT = 2;
    private final int ASK_BOX_BUTTON_X_GAP = (int) (30f / 900 * boxWidth);
    private final int ASK_BOX_BUTTON_WIDTH = (boxWidth - ASK_BOX_BUTTON_X_GAP * (ASK_BOX_BUTTON_COUNT + 1)) / ASK_BOX_BUTTON_COUNT;
    private final int ASK_BOX_BUTTON_Y_GAP = (int) (30f / 600 * boxHeight);
    private final int ASK_BOX_BUTTON_HEIGHT = (int) (100f / 600 * boxHeight);
    // info box 参数
    private final int INFO_BOX_BUTTON_COUNT = 1;
    private final int INFO_BOX_BUTTON_X_GAP = (int) (210f / 900 * boxWidth);
    private final int INFO_BOX_BUTTON_WIDTH = (boxWidth - INFO_BOX_BUTTON_X_GAP * (INFO_BOX_BUTTON_COUNT + 1)) / INFO_BOX_BUTTON_COUNT;
    private final int INFO_BOX_BUTTON_Y_GAP = (int) (30f / 600 * boxHeight);
    private final int INFO_BOX_BUTTON_HEIGHT = (int) (100f / 600 * boxHeight);

    private AudioManager audioManager;
    private UiManager uiManager;
    private ThemeManager themeManager;
    private TextManager textManager;
    private LabelInfo titleLabelInfo;
    private LabelInfo contentLabelInfo;
    private ButtonInfo buttonInfo;
    private String boxAudioFileHandleTag = StringPolisher.polished("message_box.audio");
    private FileHandle boxAudioFileHandle;

    // ===================================================================================================================

    /**
     * 构造消息弹窗管理器。
     *
     * @param stage       UI 舞台对象
     * @param textManager 文本管理器，用于文本对象创建
     */
    public MessageBox (Stage stage, TextManager textManager)
    {
        this.stage = stage;
        this.textManager = textManager;
    }

    /**
     * 获取当前的遮罩层。
     *
     * @return 遮罩层 Table 对象
     */
    public Table getMaskLayer ()
    {
        return uiManager.getMaskLayer();
    }

    /**
     * 创建遮罩层。只有在没有弹窗显示时才创建。
     *
     * @return 创建成功返回 true，失败返回 false
     */
    private boolean createMaskLayer ()
    {
        try
        {
            // 只有弹窗不存在时才创建遮盖
            if (!showingBoxTypeStack.isEmpty()) return true;

            // 初始化遮盖
            Table maskLayer = new Table();
            maskLayer.setFillParent(true);
            maskLayer.setBackground(new TextureRegionDrawable(createSolidTexture(new Color(0f, 0f, 0f, 0.5f))));
            maskLayer.setTouchable(Touchable.enabled);
            maskLayer.setVisible(true);
            uiManager.setMaskLayer(maskLayer);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "createMaskLayer", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗音频资源。
     *
     * @param uiMessageBoxKindJson 弹窗样式 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadAudioFromJson (JsonEntity uiMessageBoxKindJson)
    {
        try
        {
            // 上载audio样式
            if (!uiMessageBoxKindJson.containsKey("audio"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少audio字段");
                return false;
            }

            String boxAudioFileHandleName = uiMessageBoxKindJson.getString("audio");
            this.boxAudioFileHandle = themeManager.getPathHandle().child(PathName.ASSET_S_RESOURCE_AUDIO).child(boxAudioFileHandleName);
            audioManager.loadSound(boxAudioFileHandleTag, boxAudioFileHandle);
            LogUtils.debug(MessageBox.class, "loadAudioFromJson 加载弹窗音频 (file): " + boxAudioFileHandle);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadAudioFromJson", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗标题标签样式。
     *
     * @param titleLabelJson 标题标签 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadTitleLabelKind (JsonEntity titleLabelJson)
    {
        try
        {
            titleLabelInfo = new LabelInfo("title", titleLabelJson);
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式 (title) (labelInfo): " + titleLabelInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadTitleLabelKind", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗内容标签样式。
     *
     * @param contentLabelJson 内容标签 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadContentLabelKind (JsonEntity contentLabelJson)
    {
        try
        {
            contentLabelInfo = new LabelInfo("content", contentLabelJson);
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式 (content) (labelInfo): " + contentLabelInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadContentLabelKind", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗标签样式（包含标题和内容）。
     *
     * @param uiMessageBoxKindJson 弹窗样式 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLabelFromJson (JsonEntity uiMessageBoxKindJson)
    {
        try
        {
            // 上载label样式
            if (!uiMessageBoxKindJson.containsKey("label"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少label字段");
                return false;
            }
            JsonEntity labelKindJson = uiMessageBoxKindJson.getJsonEntityByKey("label");

            // title
            if (!labelKindJson.containsKey("title"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少label.title字段");
                return false;
            }
            JsonEntity titleLabelJson = labelKindJson.getJsonEntityByKey("title");
            if (!loadTitleLabelKind(titleLabelJson))
            {
                return false;
            }

            // content
            if (!labelKindJson.containsKey("content"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少label.content字段");
                return false;
            }
            JsonEntity contentLabelJson = labelKindJson.getJsonEntityByKey("content");
            if (!loadContentLabelKind(contentLabelJson))
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadLabelFromJson", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗按钮样式。
     *
     * @param buttonKindJson 按钮样式 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadButtonKind (JsonEntity buttonKindJson)
    {
        try
        {
            buttonInfo = new ButtonInfo("None", buttonKindJson);
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式 (button) (buttonInfo): " + buttonInfo);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadButtonKind", e);
            return false;
        }
    }

    /**
     * 从 JSON 配置中加载弹窗按钮样式。
     *
     * @param uiMessageBoxKindJson 弹窗样式 JSON 实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadButtonFromJson (JsonEntity uiMessageBoxKindJson)
    {
        try
        {
            // 读取按钮样式
            if (!uiMessageBoxKindJson.containsKey("button"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少button字段");
                return false;
            }
            else if (!uiMessageBoxKindJson.getJsonEntityByKey("button").containsKey("normal"))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 缺少button.normal字段");
                return false;
            }
            JsonEntity buttonKindJson = uiMessageBoxKindJson.getJsonEntityByKey("button").getJsonEntityByKey("normal");
            if (!loadButtonKind(buttonKindJson))
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadButtonFromJson", e);
            return false;
        }
    }

    /**
     * 从主题配置中加载弹窗样式（音频、标签、按钮）。
     *
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadMessageBoxKindFromTheme ()
    {
        try
        {
            // 获取messageBox路径
            FileHandle uiMessageBoxKindPath = themeManager.getPathHandle().child(PathName.ASSET_S_UI_MESSAGE_BOX).child(FileName.THEME_S_UI_MESSAGE_BOX_S_CONFIG);
            JsonEntity uiMessageBoxKindJson = new JsonEntity(uiMessageBoxKindPath);
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式 (path): " + uiMessageBoxKindPath);
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式 (json): " + uiMessageBoxKindJson);

            // 加载audio
            if (!loadAudioFromJson(uiMessageBoxKindJson))
            {
                LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式失败 已忽略");
                boxAudioFileHandle = null;
            }
            else
            {
                LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式成功");
            }

            // 加载label样式
            if (!loadLabelFromJson(uiMessageBoxKindJson))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式失败");
                return false;
            }
            else
            {
                LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式成功");
            }

            if (!loadButtonFromJson(uiMessageBoxKindJson))
            {
                LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式失败");
                return false;
            }
            else
            {
                LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式成功");
            }

            // debug成功
            LogUtils.debug(MessageBox.class, "loadMessageBoxKindFromTheme 加载弹窗样式成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "loadMessageBoxKindFromTheme", e);
            return false;
        }
    }

    /**
     * 初始化消息弹窗管理器，创建遮罩并加载主题样式。
     *
     * @param audioManager 音频管理器
     * @param uiManager    UI 管理器
     * @param themeManager 主题管理器
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init (AudioManager audioManager, UiManager uiManager, ThemeManager themeManager)
    {
        try
        {
            this.audioManager = audioManager;
            this.uiManager = uiManager;
            this.themeManager = themeManager;

            if (!createMaskLayer())
            {
                return false;
            }

            if (!loadMessageBoxKindFromTheme())
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "init", e);
            return false;
        }
    }

    /**
     * 重新加载消息弹窗管理器（重新初始化）。
     *
     * @param audioManager 音频管理器
     * @param themeManager 主题管理器
     * @param uiManager    UI 管理器
     * @return 重新加载成功返回 true，失败返回 false
     */
    public boolean reload (AudioManager audioManager, ThemeManager themeManager, UiManager uiManager)
    {
        try
        {
            return init(audioManager, uiManager, themeManager);
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "reload", e);
            return false;
        }
    }

    /**
     * 将弹窗信息推入栈中。
     *
     * @param boxType         弹窗类型
     * @param enterButtonTag  确认按钮标签
     * @param escapeButtonTag 取消按钮标签
     */
    private void pushBox (BoxType boxType, String enterButtonTag, String escapeButtonTag)
    {
        showingBoxTypeStack.push(boxType);
        enterButtonTagStack.push(enterButtonTag);
        escapeButtonTagStack.push(escapeButtonTag);
    }

    /**
     * 将弹窗信息从栈中弹出。
     */
    private void popBox ()
    {
        showingBoxTypeStack.pop();
        enterButtonTagStack.pop();
        escapeButtonTagStack.pop();
    }

    /**
     * 获取当前正在显示的弹窗类型。
     *
     * @return 当前弹窗类型，无弹窗时返回 UNKNOWN
     */
    public BoxType getShowingBoxType ()
    {
        if (showingBoxTypeStack.isEmpty()) return BoxType.UNKNOWN;
        return showingBoxTypeStack.peek();
    }

    /**
     * 获取当前弹窗的确认按钮标签。
     *
     * @return 确认按钮标签，无弹窗时返回 null
     */
    public String getEnterButtonTag ()
    {
        if (enterButtonTagStack.isEmpty()) return null;
        return enterButtonTagStack.peek();
    }

    /**
     * 获取当前弹窗的取消按钮标签。
     *
     * @return 取消按钮标签，无弹窗时返回 null
     */
    public String getEscapeButtonTag ()
    {
        if (escapeButtonTagStack.isEmpty()) return null;
        return escapeButtonTagStack.peek();
    }

    // ===================================================================================================================

    /**
     * 创建纯色纹理。
     *
     * @param color 颜色对象
     * @return 纯色纹理
     */
    private Texture createSolidTexture (Color color)
    {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * 将遮罩层添加到舞台。
     */
    private void putMaskLayer ()
    {
        try
        {
            stage.addActor(uiManager.getMaskLayer());
            LogUtils.debug(MessageBox.class, "putMaskLayer 添加遮盖");
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "putMaskLayer", e);
        }
    }

    /**
     * 当栈内弹窗数量为 1 时移除遮罩层。
     */
    private void removeMaskLayer ()
    {
        try
        {
            // 栈内元素数量为1时移除遮盖
            if (showingBoxTypeStack.size() != 1) return;
            uiManager.getMaskLayer().remove();
            LogUtils.debug(MessageBox.class, "removeMaskLayer 移除遮盖");
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "removeMaskLayer", e);
        }
    }

    // ===================================================================================================================

    /**
     * 获取 Ask 弹窗的标准存储标签。
     *
     * @param buttonTag 按钮标签
     * @return 标准化的 Ask 弹窗标签
     */
    private String getAskStandardTag (String buttonTag)
    {
        return "message_box.ask." + buttonTag;
    }

    /**
     * 获取 Ask 弹窗标题标签的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的标题标签名
     */
    private String getAskStandardLabelTitleTag (String askTag)
    {
        return "message_box.ask." + askTag + ".label.title";
    }

    /**
     * 获取 Ask 弹窗内容标签的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的内容标签名
     */
    private String getAskStandardLabelContentTag (String askTag)
    {
        return "message_box.ask." + askTag + ".label.content";
    }

    /**
     * 获取 Ask 弹窗"是"按钮的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的"是"按钮标签名
     */
    private String getAskStandardButtonYesTag (String askTag)
    {
        return "message_box.ask." + askTag + ".button.yes";
    }

    /**
     * 获取 Ask 弹窗"否"按钮的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的"否"按钮标签名
     */
    private String getAskStandardButtonNoTag (String askTag)
    {
        return "message_box.ask." + askTag + ".button.no";
    }

    /**
     * 显示确认弹窗（Ask），包含"是"和"否"两个按钮。
     *
     * @param tag     弹窗唯一标识
     * @param title   弹窗标题
     * @param message 弹窗内容
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean showAsk (String tag, String title, String message)
    {
        try
        {
            if (!askMap.containsKey(getAskStandardTag(tag)))
            {
                // mask层
                putMaskLayer();

                // 初始参数
                int buttonIndex = 0;
                String labelTitleTag = getAskStandardLabelTitleTag(tag);
                String labelContentTag = getAskStandardLabelContentTag(tag);
                String buttonYesTag = getAskStandardButtonYesTag(tag);
                String buttonNoTag = getAskStandardButtonNoTag(tag);

                // 显示弹窗标题
                uiManager.addLabel(
                    labelTitleTag,
                    titleLabelInfo.getLabelKindName(),
                    boxPositionX,
                    boxPositionY + (boxHeight - boxTitleHeight),
                    boxWidth,
                    boxTitleHeight,
                    new TextObject(textManager, title),
                    themeManager.getFont(),
                    titleLabelInfo.getFontSize(),
                    titleLabelInfo.getFontColor(),
                    titleLabelInfo.getFontFlag(),
                    titleLabelInfo.getFontArgs()
                );

                // 显示弹窗内容
                uiManager.addLabel(
                    labelContentTag,
                    contentLabelInfo.getLabelKindName(),
                    boxPositionX,
                    boxPositionY,
                    boxWidth,
                    boxHeight - boxTitleHeight - boxTitleContentGap,
                    new TextObject(textManager, message),
                    themeManager.getFont(),
                    contentLabelInfo.getFontSize(),
                    contentLabelInfo.getFontColor(),
                    contentLabelInfo.getFontFlag(),
                    contentLabelInfo.getFontArgs()
                );

                // 显示按钮yes
                buttonIndex++;
                uiManager.addButton(
                    buttonYesTag,
                    buttonInfo.getButtonKindName(),
                    boxPositionX + buttonIndex * (ASK_BOX_BUTTON_WIDTH + ASK_BOX_BUTTON_X_GAP) - ASK_BOX_BUTTON_WIDTH,
                    boxPositionY + ASK_BOX_BUTTON_Y_GAP,
                    ASK_BOX_BUTTON_WIDTH,
                    ASK_BOX_BUTTON_HEIGHT,
                    new TextObject(null, "YES"),
                    themeManager.getFont(),
                    buttonInfo.getFontSize(),
                    Color.valueOf("#419428FF")
                );

                // 显示按钮no
                buttonIndex++;
                uiManager.addButton(
                    buttonNoTag,
                    buttonInfo.getButtonKindName(),
                    boxPositionX + buttonIndex * (ASK_BOX_BUTTON_WIDTH + ASK_BOX_BUTTON_X_GAP) - ASK_BOX_BUTTON_WIDTH,
                    boxPositionY + ASK_BOX_BUTTON_Y_GAP,
                    ASK_BOX_BUTTON_WIDTH,
                    ASK_BOX_BUTTON_HEIGHT,
                    new TextObject(null, "NO"),
                    themeManager.getFont(),
                    buttonInfo.getFontSize(),
                    Color.valueOf("#CC2C1FFF")
                );

                // 播放音频
                if (boxAudioFileHandle != null)
                {
                    audioManager.playSound(boxAudioFileHandleTag, false);
                }

                // 上载
                askMap.put(getAskStandardTag(tag), new BoxAsk(uiManager, buttonYesTag, buttonNoTag));
                pushBox(BoxType.ASK, buttonYesTag, buttonNoTag);

                // debug
                LogUtils.debug(MessageBox.class, "showAsk 显示弹窗 (tag): " + tag + " (title):" + title + " (message):" + message);
            }
            else
            {
                LogUtils.debug(MessageBox.class, "showAsk 弹窗已存在 (tag): " + tag);
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "showAsk", e);
            return false;
        }
    }

    /**
     * 隐藏指定标签的确认弹窗，移除 UI 组件并卸载资源。
     *
     * @param tag 弹窗唯一标识
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean hideAsk (String tag)
    {
        try
        {
            if (askMap.containsKey(getAskStandardTag(tag)))
            {
                // 移除ui组件
                uiManager.deleteButton(getAskStandardButtonYesTag(tag));
                uiManager.deleteButton(getAskStandardButtonNoTag(tag));
                uiManager.deleteLabel(getAskStandardLabelTitleTag(tag));
                uiManager.deleteLabel(getAskStandardLabelContentTag(tag));

                // 移除遮盖
                removeMaskLayer();

                // 卸载tag
                askMap.remove(getAskStandardTag(tag));
                popBox();
                LogUtils.debug(MessageBox.class, "hideAsk 移除弹窗 (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "hideAsk", e);
            return false;
        }
    }

    /**
     * 获取指定标签的确认弹窗结果。
     *
     * @param tag 弹窗唯一标识
     * @return 确认弹窗结果对象，不存在时返回空结果
     */
    public BoxAsk getAskResult (String tag)
    {
        try
        {
            if (askMap.containsKey(getAskStandardTag(tag)))
            {
                return askMap.get(getAskStandardTag(tag));
            }
            else
            {
                return new BoxAsk(uiManager, null, null);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "getAskResult", e);
            return new BoxAsk(uiManager, null, null);
        }
    }

    // ===================================================================================================================

    /**
     * 获取 Info 弹窗的标准存储标签。
     *
     * @param buttonTag 按钮标签
     * @return 标准化的 Info 弹窗标签
     */
    private String getInfoStandardTag (String buttonTag)
    {
        return "message_box.info." + buttonTag;
    }

    /**
     * 获取 Info 弹窗标题标签的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的标题标签名
     */
    private String getInfoStandardLabelTitleTag (String askTag)
    {
        return "message_box.info." + askTag + ".label.title";
    }

    /**
     * 获取 Info 弹窗内容标签的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的内容标签名
     */
    private String getInfoStandardLabelContentTag (String askTag)
    {
        return "message_box.info." + askTag + ".label.content";
    }

    /**
     * 获取 Info 弹窗"确定"按钮的标准标签名。
     *
     * @param askTag 弹窗标签
     * @return 标准化的"确定"按钮标签名
     */
    private String getInfoStandardButtonOkTag (String askTag)
    {
        return "message_box.info." + askTag + ".button.ok";
    }

    /**
     * 显示信息弹窗（Info），包含"确定"按钮。
     *
     * @param tag     弹窗唯一标识
     * @param title   弹窗标题
     * @param message 弹窗内容
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean showInfo (String tag, String title, String message)
    {
        try
        {
            if (!infoMap.containsKey(getInfoStandardTag(tag)))
            {
                // mask层
                putMaskLayer();

                // 初始参数
                int buttonIndex = 1;
                String labelTitleTag = getInfoStandardLabelTitleTag(tag);
                String labelContentTag = getInfoStandardLabelContentTag(tag);
                String buttonOkTag = getInfoStandardButtonOkTag(tag);

                // 显示弹窗标题
                uiManager.addLabel(
                    labelTitleTag,
                    titleLabelInfo.getLabelKindName(),
                    boxPositionX,
                    boxPositionY + (boxHeight - boxTitleHeight),
                    boxWidth,
                    boxTitleHeight,
                    new TextObject(textManager, title),
                    themeManager.getFont(),
                    titleLabelInfo.getFontSize(),
                    titleLabelInfo.getFontColor(),
                    titleLabelInfo.getFontFlag(),
                    titleLabelInfo.getFontArgs()
                );

                // 显示弹窗内容
                uiManager.addLabel(
                    labelContentTag,
                    contentLabelInfo.getLabelKindName(),
                    boxPositionX,
                    boxPositionY,
                    boxWidth,
                    boxHeight - boxTitleHeight - boxTitleContentGap,
                    new TextObject(textManager, message),
                    themeManager.getFont(),
                    contentLabelInfo.getFontSize(),
                    contentLabelInfo.getFontColor(),
                    contentLabelInfo.getFontFlag(),
                    contentLabelInfo.getFontArgs()
                );

                // 显示按钮ok
                uiManager.addButton(
                    buttonOkTag,
                    buttonInfo.getButtonKindName(),
                    boxPositionX + buttonIndex * (INFO_BOX_BUTTON_WIDTH + INFO_BOX_BUTTON_X_GAP) - INFO_BOX_BUTTON_WIDTH,
                    boxPositionY + INFO_BOX_BUTTON_Y_GAP,
                    INFO_BOX_BUTTON_WIDTH,
                    INFO_BOX_BUTTON_HEIGHT,
                    new TextObject(null, "OK"),
                    themeManager.getFont(),
                    buttonInfo.getFontSize(),
                    Color.valueOf("#FFD700FF")
                );

                // 播放音频
                if (boxAudioFileHandle != null)
                {
                    audioManager.playSound(boxAudioFileHandleTag, false);
                }

                // 上载
                infoMap.put(getInfoStandardTag(tag), new BoxInfo(uiManager, buttonOkTag));
                pushBox(BoxType.INFO, buttonOkTag, buttonOkTag);

                // debug
                LogUtils.debug(MessageBox.class, "showInfo 显示弹窗 (tag): " + tag + " (title):" + title + " (message):" + message);
            }
            else
            {
                LogUtils.debug(MessageBox.class, "showInfo 弹窗已存在 (tag): " + tag);
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "showInfo", e);
            return false;
        }
    }

    /**
     * 隐藏指定标签的信息弹窗，移除 UI 组件并卸载资源。
     *
     * @param tag 弹窗唯一标识
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean hideInfo (String tag)
    {
        try
        {
            if (infoMap.containsKey(getInfoStandardTag(tag)))
            {
                // 移除ui组件
                uiManager.deleteButton(getInfoStandardButtonOkTag(tag));
                uiManager.deleteLabel(getInfoStandardLabelTitleTag(tag));
                uiManager.deleteLabel(getInfoStandardLabelContentTag(tag));

                // 移除遮盖
                removeMaskLayer();

                // 卸载tag
                infoMap.remove(getInfoStandardTag(tag));
                popBox();
                LogUtils.debug(MessageBox.class, "hideInfo 移除弹窗 (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "hideInfo", e);
            return false;
        }
    }

    /**
     * 获取指定标签的信息弹窗结果。
     *
     * @param tag 弹窗唯一标识
     * @return 信息弹窗结果对象，不存在时返回空结果
     */
    public BoxInfo getInfoResult (String tag)
    {
        try
        {
            if (infoMap.containsKey(getInfoStandardTag(tag)))
            {
                return infoMap.get(getInfoStandardTag(tag));
            }
            else
            {
                return new BoxInfo(uiManager, null);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "getInfoResult", e);
            return new BoxInfo(uiManager, null);
        }
    }

    // ===================================================================================================================

    /**
     * 处理确认弹窗的交互结果，根据用户选择执行对应回调。
     *
     * @param tag   弹窗唯一标识
     * @param onYes 点击"是"时的回调
     * @param onNo  点击"否"时的回调
     */
    public void handleAsk (String tag, Runnable onYes, Runnable onNo)
    {
        BoxAsk result = getAskResult(tag);
        if (result.isYes())
        {
            hideAsk(tag);
            onYes.run();
        }
        else if (result.isNo())
        {
            hideAsk(tag);
            onNo.run();
        }
    }

    /**
     * 处理确认弹窗的交互结果（仅处理"是"的情况）。
     *
     * @param tag   弹窗唯一标识
     * @param onYes 点击"是"时的回调
     */
    public void handleAsk (String tag, Runnable onYes)
    {
        handleAsk(tag, onYes, () ->
        {
        });
    }

    /**
     * 处理信息弹窗的交互结果，用户点击确定时执行回调。
     *
     * @param tag  弹窗唯一标识
     * @param onOk 点击"确定"时的回调
     */
    public void handleInfo (String tag, Runnable onOk)
    {
        BoxInfo result = getInfoResult(tag);
        if (result.isOk())
        {
            hideInfo(tag);
            onOk.run();
        }
    }

    // ===================================================================================================================

    /**
     * 销毁消息弹窗管理器，清空所有弹窗数据和遮罩层。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean dispose ()
    {
        try
        {
            askMap.clear();
            infoMap.clear();
            if (!showingBoxTypeStack.isEmpty())
            {
                if (uiManager != null && uiManager.getMaskLayer() != null)
                {
                    uiManager.getMaskLayer().remove();
                }
                showingBoxTypeStack.clear();
                enterButtonTagStack.clear();
                escapeButtonTagStack.clear();
            }

            LogUtils.debug(MessageBox.class, "dispose 销毁弹窗管理器成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(MessageBox.class, "dispose", e);
            return false;
        }
    }
}
