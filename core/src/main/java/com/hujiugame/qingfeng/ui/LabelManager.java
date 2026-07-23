package com.hujiugame.qingfeng.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelKind;
import com.hujiugame.qingfeng.ui.kind.label.LabelState;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.ui.FontFlag;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public final class LabelManager
{
    private final Stage stage;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final HashSet<InteractableObject> interactableObjectSet;
    private final UiManager.FontResolver fontResolver;

    private static final float DEFAULT_LABEL_TEXT_TYPING_SPEED = 25.0f;

    // ========== label kind ==========
    private final Map<String, LabelKind> labelKindMap = new HashMap<>();
    // ========== label object ==========
    private final Map<String, UiManager.CustomLabel> labelMap = new HashMap<>();
    // ========== label kind name tracking ==========
    private final Map<String, String> labelKindNameMap = new HashMap<>();
    // ========== label state ==========
    private final Map<String, LabelState> labelStateMap = new HashMap<>();
    // ========== label base text ==========
    private final Map<String, String> labelBaseTextMap = new HashMap<>();

    public LabelManager (Stage stage, GraphicsManager graphicsManager,
                         HashSet<InteractableObject> interactableObjectSet,
                         UiManager.FontResolver fontResolver,
                         UiManager uiManager)
    {
        this.stage = stage;
        this.graphicsManager = graphicsManager;
        this.interactableObjectSet = interactableObjectSet;
        this.fontResolver = fontResolver;
        this.uiManager = uiManager;
    }

    // ===================================================================================================================
    // Kind loading
    // ===================================================================================================================

    /**
     * 从 JSON 配置文件加载标签样式（写入 pendingPixmapMap，等 init 完成后合并）
     */
    public boolean loadLabelKind (FileHandle file, FileHandle themePath,
                                  Map<String, Pixmap> pendingPixmapMap,
                                  Map<String, Label.LabelStyle> pendingLabelStyles,
                                  Map<String, Float> pendingLabelBorderScales)
    {
        try
        {
            JsonEntity labelKindJson = new JsonEntity(file);
            LogUtils.debug(LabelManager.class, "loadLabelKind 读取标签配置: " + labelKindJson);

            String labelKindName = labelKindJson.getString("name");
            if (labelKindName == null)
            {
                LogUtils.error(LabelManager.class, "loadLabelKind 缺少 name 字段: " + labelKindJson);
                return false;
            }

            String fontName = labelKindJson.getString("font");
            if (fontName == null)
            {
                LogUtils.error(LabelManager.class, "loadLabelKind 缺少 font 字段: " + labelKindJson);
                return false;
            }

            float borderScale = 1.0f;
            if (labelKindJson.containsKey("borderScale"))
            {
                borderScale = labelKindJson.getFloat("borderScale");
            }

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = fontResolver.getFont(fontName, 1.0f);
            String fontColorStr = labelKindJson.getString("fontColor");
            if (fontColorStr == null)
            {
                LogUtils.error(LabelManager.class, "loadLabelKind fontColor 字段缺失或类型不是字符串 (name): " + labelKindName);
                return false;
            }
            labelStyle.fontColor = Color.valueOf(fontColorStr);

            // 生成背景 Pixmap
            Pixmap bgPixmap = null;
            JsonEntity imageJson = labelKindJson.getJsonEntityByKey("image");
            FileHandle resImagePath = themePath.child(PathName.ASSET_S_RESOURCE_IMAGE);

            if (imageJson.isEmpty())
            {
                bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                bgPixmap.setColor(Color.CLEAR);
                bgPixmap.fill();
            }
            else if (imageJson.containsKey("background"))
            {
                FileHandle bgFileHandle = resImagePath.child(imageJson.getString("background"));
                if (!bgFileHandle.exists())
                {
                    LogUtils.error(LabelManager.class, "loadLabelKind 背景文件不存在: " + bgFileHandle.path());
                    return false;
                }
                bgPixmap = new Pixmap(bgFileHandle);
            }
            else if (labelKindJson.containsKey("backgroundColor"))
            {
                String bgColorStr = labelKindJson.getString("backgroundColor");
                if (bgColorStr == null)
                {
                    LogUtils.error(LabelManager.class, "loadLabelKind backgroundColor 字段类型不是字符串 (name): " + labelKindName);
                    return false;
                }
                Color bgColor = Color.valueOf(bgColorStr);
                bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                bgPixmap.setColor(bgColor);
                bgPixmap.fill();
            }
            else
            {
                bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                bgPixmap.setColor(Color.CLEAR);
                bgPixmap.fill();
            }

            pendingPixmapMap.put(UiManager.PIXMAP_LABEL + labelKindName, bgPixmap);
            pendingLabelStyles.put(labelKindName, labelStyle);
            pendingLabelBorderScales.put(labelKindName, borderScale);
            LogUtils.debug(LabelManager.class, "暂存标签背景 pixmap: " + labelKindName);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "loadLabelKind", e);
            return false;
        }
    }

    /**
     * 获取所有标签样式映射表
     */
    public Map<String, LabelKind> getLabelKindMap ()
    {
        return labelKindMap;
    }

    /**
     * 根据名称获取标签样式
     */
    public LabelKind getLabelKind (String labelKindName)
    {
        return labelKindMap.get(labelKindName);
    }

    /**
     * 移除指定名称的标签样式
     */
    public boolean removeLabelKind (String labelKindName)
    {
        try
        {
            LabelKind labelKind = labelKindMap.get(labelKindName);
            if (labelKind != null)
            {
                labelKindMap.remove(labelKindName);
                LogUtils.debug(LabelManager.class, "removeLabelKind 移除标签样式成功 (name): " + labelKindName);
                return true;
            }
            else
            {
                LogUtils.error(LabelManager.class, "removeLabelKind 移除标签样式失败 (name): " + labelKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "removeLabelKind", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Object creation
    // ===================================================================================================================

    private Label.LabelStyle parseFinalLabelStyle (Label.LabelStyle labelStyle, String fontName, float fontSize, Color fontColor)
    {
        try
        {
            Label.LabelStyle finalLabelStyle = new Label.LabelStyle(labelStyle);
            finalLabelStyle.font = fontResolver.getFont(fontName, fontSize);
            if (fontColor != null)
            {
                finalLabelStyle.fontColor = fontColor;
            }
            return finalLabelStyle;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "parseFinalLabelStyle", e);
            return null;
        }
    }

    /**
     * 创建标签背景 Image。
     * <p>
     * sourceBorder 决定从源纹理边缘取多少像素作为九宫格裁切位置（固定比例 = 控件/16），
     * renderBorder 决定这些像素在屏幕上绘制多大（= sourceBorder × borderScale）。
     * 二者分离使 borderScale 能真正缩放边框视觉大小，而非改变裁切深度。
     * </p>
     */
    private Image createLabelBackground (LabelKind labelKind, int sourceBorder, int renderBorder)
    {
        TextureRegion r = labelKind.getBackgroundRegion();
        if (sourceBorder * 2 < r.getRegionWidth() && sourceBorder * 2 < r.getRegionHeight())
        {
            NinePatch patch = new NinePatch(r, sourceBorder, sourceBorder, sourceBorder, sourceBorder);
            patch.setLeftWidth(renderBorder);
            patch.setRightWidth(renderBorder);
            patch.setTopHeight(renderBorder);
            patch.setBottomHeight(renderBorder);
            LogUtils.debug(LabelManager.class,
                "标签背景 NinePatch: sourceBorder=" + sourceBorder
                    + " renderBorder=" + renderBorder
                    + " 纹理=" + r.getRegionWidth() + "x" + r.getRegionHeight());
            return new Image(new NinePatchDrawable(patch));
        }
        LogUtils.debug(LabelManager.class,
            "标签背景 纹理太小，跳过 NinePatch: 纹理="
                + r.getRegionWidth() + "x" + r.getRegionHeight());
        return new Image(r);
    }

    /**
     * 创建标签控件并添加到舞台
     */
    private boolean createLabel (String labelTag, String labelKindName,
                                 float x, float y, float width, float height,
                                 TextObject text, String fontName, float fontSize,
                                 Color fontColor, FontFlag fontFlag, JsonEntity fontArgs)
    {
        try
        {
            LabelKind labelKind = getLabelKind(labelKindName);
            if (labelKind == null)
            {
                LogUtils.error(LabelManager.class, "createLabel 找不到标签样式: " + labelKindName);
                return false;
            }

            Label.LabelStyle finalStyle = parseFinalLabelStyle(
                labelKind.getLabelStyle(), fontName, fontSize, fontColor);

            float padX = 50, padY = 50;
            if (fontArgs != null)
            {
                if (fontArgs.containsKey("padX") && fontArgs.containsKey("padY"))
                {
                    padX = fontArgs.getFloat("padX");
                    padY = fontArgs.getFloat("padY");
                }
                else if (fontArgs.containsKey("pad"))
                {
                    padX = fontArgs.getFloat("pad");
                    padY = fontArgs.getFloat("pad");
                }
            }

            // 计算九宫格裁切位置（sourceBorder）与绘制大小（renderBorder）
            TextureRegion bgRegion = labelKind.getBackgroundRegion();
            int sourceBorder = Math.max(1, (int)(Math.min(width, height) / 16));
            int maxSource = Math.min(bgRegion.getRegionWidth(), bgRegion.getRegionHeight()) / 2 - 1;
            sourceBorder = Math.min(sourceBorder, Math.max(1, maxSource));
            int renderBorder = Math.max(1, (int)(sourceBorder * labelKind.getBorderScale()));

            Image bgImage = createLabelBackground(labelKind, sourceBorder, renderBorder);
            bgImage.setPosition(0, 0);
            bgImage.setSize(width, height);

            Label label = new Label(text.getDisplayText(), finalStyle);
            label.setPosition(padX, padY);
            label.setSize(width - padX * 2, height - padY * 2);
            label.setWrap(true);

            boolean enableTyping = false;
            if (fontFlag != null)
            {
                switch (fontFlag)
                {
                    case W_TYPING:
                        enableTyping = true;
                    case W:
                        label.setAlignment(Align.left);
                        break;

                    case E_TYPING:
                        enableTyping = true;
                    case E:
                        label.setAlignment(Align.right);
                        break;

                    case N_TYPING:
                        enableTyping = true;
                    case N:
                        label.setAlignment(Align.top);
                        break;

                    case S_TYPING:
                        enableTyping = true;
                    case S:
                        label.setAlignment(Align.bottom);
                        break;

                    case NW_TYPING:
                        enableTyping = true;
                    case NW:
                        label.setAlignment(Align.topLeft);
                        break;

                    case NE_TYPING:
                        enableTyping = true;
                    case NE:
                        label.setAlignment(Align.topRight);
                        break;

                    case SE_TYPING:
                        enableTyping = true;
                    case SE:
                        label.setAlignment(Align.bottomRight);
                        break;

                    case SW_TYPING:
                        enableTyping = true;
                    case SW:
                        label.setAlignment(Align.bottomLeft);
                        break;

                    case CENTER_TYPING:
                        enableTyping = true;
                    case CENTER:
                    default:
                        label.setAlignment(Align.center);
                }
            }
            else
            {
                label.setAlignment(Align.center);
            }

            UiManager.CustomLabel labelContainer = new UiManager.CustomLabel(text, bgImage, label, labelTag, uiManager);
            labelContainer.setPosition(x, y);
            labelContainer.setSize(width, height);

            if (enableTyping) labelContainer.enableTyping(DEFAULT_LABEL_TEXT_TYPING_SPEED);

            labelContainer.setClickListener(() ->
            {
                LogUtils.debug(LabelManager.class, "label clicked: " + getLabelStandardTag(labelTag));
                labelContainer.completeTyping();
                if (!labelStateMap.containsKey(labelTag))
                {
                    labelStateMap.put(labelTag, new LabelState());
                }
                labelStateMap.get(labelTag).setClicked();
            });

            stage.addActor(labelContainer);
            labelMap.put(labelTag, labelContainer);
            labelKindNameMap.put(labelTag, labelKindName);
            labelStateMap.put(labelTag, new LabelState());
            interactableObjectSet.add(labelContainer);
            LogUtils.debug(LabelManager.class, "createLabel 成功: " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "createLabel 异常", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Public API
    // ===================================================================================================================

    public String getLabelStandardTag (String labelTag)
    {
        return "[Label] " + labelTag;
    }

    public boolean addLabel (String labelTag, String labelKindName,
                             float x, float y, float width, float height,
                             TextObject textObject, String fontName, float fontSize,
                             Color fontColor, FontFlag fontFlag, JsonEntity fontArgs)
    {
        try
        {
            if (!labelMap.containsKey(labelTag))
            {
                return createLabel(labelTag, labelKindName, x, y, width, height, textObject, fontName, fontSize, fontColor, fontFlag, fontArgs);
            }
            else
            {
                return updateLabel(labelTag, labelKindName, x, y, width, height, textObject, fontName, fontSize, fontColor, fontFlag, fontArgs);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "addLabel", e);
            return false;
        }
    }

    public boolean addLabel (LabelInfo labelInfo)
    {
        try
        {
            return addLabel(
                labelInfo.getLabelTag(),
                labelInfo.getLabelKindName(),
                labelInfo.getX(),
                labelInfo.getY(),
                labelInfo.getWidth(),
                labelInfo.getHeight(),
                labelInfo.getTextObject(),
                labelInfo.getFontName(),
                labelInfo.getFontSize(),
                labelInfo.getFontColor(),
                labelInfo.getFontFlag(),
                labelInfo.getFontArgs()
            );
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "addLabel", e);
            return false;
        }
    }

    public boolean addLabel (List<LabelInfo> labelInfoList)
    {
        try
        {
            boolean result = true;
            if (labelInfoList != null)
            {
                for (LabelInfo labelInfo : labelInfoList)
                {
                    if (!addLabel(labelInfo)) result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "addLabel", e);
            return false;
        }
    }

    public boolean addLabel (List<String> labelTagList, Map<String, LabelInfo> labelInfoMap)
    {
        try
        {
            boolean result = true;
            if (labelTagList != null && labelInfoMap != null)
            {
                for (String labelTag : labelTagList)
                {
                    if (labelInfoMap.containsKey(labelTag))
                    {
                        if (!addLabel(labelInfoMap.get(labelTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "addLabel", e);
            return false;
        }
    }

    public boolean updateLabel (String labelTag, String labelKindName,
                                float x, float y, float width, float height,
                                TextObject text, String fontName, float fontSize,
                                Color fontColor, FontFlag fontFlag, JsonEntity fontArgs)
    {
        try
        {
            LogUtils.debug(LabelManager.class, "updateLabel (tag): " + labelTag + " (kind): " + labelKindName);
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "updateLabel 找不到标签对象 (tag): " + labelTag);
                return false;
            }

            // 切换 kind
            if (labelKindName != null && !labelKindName.isEmpty())
            {
                LabelKind newKind = labelKindMap.get(labelKindName);
                if (newKind != null)
                {
                    labelContainer.setPosition(x, y);
                    labelContainer.setSize(width, height);
                    labelKindNameMap.put(labelTag, labelKindName);
                }
            }

            // 更新文字字体
            if (fontName != null && !fontName.isEmpty())
            {
                BitmapFont newFont = fontResolver.getFont(fontName, fontSize);
                if (newFont != null)
                {
                    labelContainer.getLabel().setFontScale(fontSize, fontSize);
                    LogUtils.debug(LabelManager.class, "updateLabel 字体已更新 (font): " + fontName);
                }

                labelBaseTextMap.put(labelTag, fontName);
            }

            // 更新颜色
            if (fontColor != null)
            {
                labelContainer.getLabel().setColor(fontColor);
            }

            // 更新基础文本
            if (text != null)
            {
                labelContainer.setTextObject(text);
            }

            // 更新对齐方式
            if (fontFlag != null)
            {
                switch (fontFlag)
                {
                    case W:
                    case W_TYPING:
                        labelContainer.getLabel().setAlignment(Align.left);
                        break;

                    case E:
                    case E_TYPING:
                        labelContainer.getLabel().setAlignment(Align.right);
                        break;

                    case N:
                    case N_TYPING:
                        labelContainer.getLabel().setAlignment(Align.top);
                        break;

                    case S:
                    case S_TYPING:
                        labelContainer.getLabel().setAlignment(Align.bottom);
                        break;

                    case NW:
                    case NW_TYPING:
                        labelContainer.getLabel().setAlignment(Align.topLeft);
                        break;

                    case NE:
                    case NE_TYPING:
                        labelContainer.getLabel().setAlignment(Align.topRight);
                        break;

                    case SE:
                    case SE_TYPING:
                        labelContainer.getLabel().setAlignment(Align.bottomRight);
                        break;

                    case SW:
                    case SW_TYPING:
                        labelContainer.getLabel().setAlignment(Align.bottomLeft);
                        break;

                    case CENTER:
                    case CENTER_TYPING:
                    default:
                        labelContainer.getLabel().setAlignment(Align.center);
                }
                LogUtils.debug(LabelManager.class, "updateLabel 更新标签对齐 (tag): " + labelTag + " (flag): " + fontFlag);
            }

            // 更新内边距
            if (fontArgs != null)
            {
                float padX = 50, padY = 50;
                if (fontArgs.containsKey("padX") && fontArgs.containsKey("padY"))
                {
                    padX = fontArgs.getFloat("padX");
                    padY = fontArgs.getFloat("padY");
                }
                else if (fontArgs.containsKey("pad"))
                {
                    padX = fontArgs.getFloat("pad");
                    padY = fontArgs.getFloat("pad");
                }
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "updateLabel", e);
            return false;
        }
    }

    // ========== Position / Size ==========

    public boolean setLabelPosition (String labelTag, float x, float y)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelPosition 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setPosition(x, y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelPosition", e);
            return false;
        }
    }

    public float getLabelX (String labelTag)
    {
        UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
        return labelContainer != null ? labelContainer.getX() : 0;
    }

    public boolean setLabelX (String labelTag, float x)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelX 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setX(x);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelX", e);
            return false;
        }
    }

    public float getLabelY (String labelTag)
    {
        UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
        return labelContainer != null ? labelContainer.getY() : 0;
    }

    public boolean setLabelY (String labelTag, float y)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelY 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setY(y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelY", e);
            return false;
        }
    }

    public boolean setLabelSize (String labelTag, float width, float height)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelSize 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setSize(width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelSize", e);
            return false;
        }
    }

    public float getLabelWidth (String labelTag)
    {
        UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
        return labelContainer != null ? labelContainer.getWidth() : 0;
    }

    public boolean setLabelWidth (String labelTag, float width)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelWidth 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setWidth(width);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelWidth", e);
            return false;
        }
    }

    public float getLabelHeight (String labelTag)
    {
        UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
        return labelContainer != null ? labelContainer.getHeight() : 0;
    }

    public boolean setLabelHeight (String labelTag, float height)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelHeight 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setHeight(height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelHeight", e);
            return false;
        }
    }

    // ========== Text ==========

    public TextObject getLabelText (String labelTag)
    {
        UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
        return labelContainer != null ? labelContainer.getTextObject() : null;
    }

    public boolean setLabelText (String labelTag, TextObject textObject)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelText 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setTextObject(textObject);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelText", e);
            return false;
        }
    }

    public boolean setLabelFontSize (String labelTag, float fontSize)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelFontSize 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            // 标签字体大小由 parser 在创建时设定，运行时不支持动态修改
            LogUtils.debug(LabelManager.class, "setLabelFontSize 暂不支持运行时修改 (tag): " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelFontSize", e);
            return false;
        }
    }

    public boolean setLabelFontColor (String labelTag, Color fontColor)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelFontColor 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            if (labelContainer.getLabel() != null)
            {
                labelContainer.getLabel().setColor(fontColor);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelFontColor", e);
            return false;
        }
    }

    // ========== Query ==========

    public boolean containsLabel (String labelTag)
    {
        return labelMap.containsKey(labelTag);
    }

    public InteractableObject getLabel (String labelTag)
    {
        return labelMap.get(labelTag);
    }

    /**
     * 获取 labelMap（供 UiManager 布局管理使用）
     */
    public Map<String, UiManager.CustomLabel> getLabelMap ()
    {
        return labelMap;
    }

    public Map<String, String> getLabelKindNameMap ()
    {
        return labelKindNameMap;
    }

    public Map<String, LabelState> getLabelStateMap ()
    {
        return labelStateMap;
    }

    public Map<String, String> getLabelBaseTextMap ()
    {
        return labelBaseTextMap;
    }

    // ========== Show / Hide ==========

    public boolean showLabel (String labelTag)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "showLabel 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setVisible(true);
            LogUtils.debug(LabelManager.class, "showLabel 成功 (tag): " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "showLabel", e);
            return false;
        }
    }

    public boolean hideLabel (String labelTag)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "hideLabel 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.setVisible(false);
            LogUtils.debug(LabelManager.class, "hideLabel 成功 (tag): " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "hideLabel", e);
            return false;
        }
    }

    // ========== Base text ==========

    public String getLabelBaseText (String labelTag)
    {
        return labelBaseTextMap.get(labelTag);
    }

    // ========== Click ==========

    public boolean setLabelClicked (String labelTag, boolean clicked)
    {
        try
        {
            if (!labelStateMap.containsKey(labelTag))
            {
                if (!clicked) return true;
                labelStateMap.put(labelTag, new LabelState());
            }
            if (clicked)
            {
                labelStateMap.get(labelTag).setClicked();
            }
            else
            {
                labelStateMap.get(labelTag).setClicked(false);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelClicked", e);
            return false;
        }
    }

    public boolean isLabelClicked (String labelTag)
    {
        return labelStateMap.containsKey(labelTag) && labelStateMap.get(labelTag).isClicked();
    }

    // ========== Read progress ==========

    public boolean setLabelReadProgress (String labelTag, float progress)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "setLabelReadProgress 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            // 阅读进度暂未实现
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "setLabelReadProgress", e);
            return false;
        }
    }

    public float getLabelReadProgress (String labelTag)
    {
        return 0;
    }

    // ========== Delete ==========

    public boolean deleteLabel (String labelTag)
    {
        try
        {
            UiManager.CustomLabel labelContainer = labelMap.get(labelTag);
            if (labelContainer == null)
            {
                LogUtils.error(LabelManager.class, "deleteLabel 找不到标签对象 (tag): " + labelTag);
                return false;
            }
            labelContainer.remove();
            labelMap.remove(labelTag);
            labelKindNameMap.remove(labelTag);
            labelStateMap.remove(labelTag);
            labelBaseTextMap.remove(labelTag);
            interactableObjectSet.remove(labelContainer);
            LogUtils.debug(LabelManager.class, "deleteLabel (tag): " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "deleteLabel", e);
            return false;
        }
    }

    public boolean deleteAllLabel ()
    {
        try
        {
            LogUtils.debug(LabelManager.class, "deleteAllLabel");
            for (UiManager.CustomLabel labelContainer : labelMap.values())
            {
                labelContainer.remove();
                interactableObjectSet.remove(labelContainer);
            }
            labelMap.clear();
            labelKindNameMap.clear();
            labelStateMap.clear();
            labelBaseTextMap.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LabelManager.class, "deleteAllLabel", e);
            return false;
        }
    }
}
