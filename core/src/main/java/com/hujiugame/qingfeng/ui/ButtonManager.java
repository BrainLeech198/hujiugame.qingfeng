package com.hujiugame.qingfeng.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.button.ButtonKind;
import com.hujiugame.qingfeng.ui.kind.button.ButtonState;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public final class ButtonManager
{
    private final Stage stage;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final HashSet<InteractableObject> interactableObjectSet;
    private final UiManager.FontResolver fontResolver;

    // ========== button kind ==========
    private final Map<String, ButtonKind> buttonKindMap = new HashMap<>();
    // ========== button object ==========
    private final Map<String, UiManager.CustomTextButton> buttonMap = new HashMap<>();
    // ========== button kind name tracking ==========
    private final Map<String, String> buttonKindNameMap = new HashMap<>();
    // ========== button state ==========
    private final Map<String, ButtonState> buttonStateMap = new HashMap<>();
    // ========== button base text ==========
    private final Map<String, String> buttonBaseTextMap = new HashMap<>();
    // ========== button click callback ==========
    private Map<String, Runnable> buttonClickCallbackMap = new HashMap<>();

    public ButtonManager (Stage stage, AudioManager audioManager,
                          GraphicsManager graphicsManager,
                          HashSet<InteractableObject> interactableObjectSet,
                          UiManager.FontResolver fontResolver,
                          UiManager uiManager)
    {
        this.stage = stage;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.interactableObjectSet = interactableObjectSet;
        this.fontResolver = fontResolver;
        this.uiManager = uiManager;
    }

    // ===================================================================================================================
    // Kind loading
    // ===================================================================================================================

    /**
     * 从 JSON 配置文件加载按钮样式（写入 pendingPixmapMap，等 init 完成后合并）
     */
    public boolean loadButtonKind (FileHandle file, FileHandle themePath,
                                   Map<String, Pixmap> pendingPixmapMap,
                                   Map<String, TextButton.TextButtonStyle> pendingButtonStyles,
                                   Map<String, FileHandle> pendingButtonAudios,
                                   Map<String, Float> pendingButtonBorderScales)
    {
        try
        {
            JsonEntity buttonKindJson = new JsonEntity(file);
            LogUtils.debug(ButtonManager.class, "loadButtonKind 读取按钮配置: " + buttonKindJson);

            String buttonKindName = buttonKindJson.getString(UiKey.Button.NAME);
            if (buttonKindName == null)
            {
                LogUtils.error(ButtonManager.class, "loadButtonKind 缺少 name 字段: " + buttonKindJson);
                return false;
            }

            String fontName = buttonKindJson.getString(UiKey.Button.FONT);
            if (fontName == null)
            {
                LogUtils.error(ButtonManager.class, "loadButtonKind 缺少 font 字段: " + buttonKindJson);
                return false;
            }

            float borderScale = 1.0f;
            if (buttonKindJson.containsKey(UiKey.Button.BORDER_SCALE))
            {
                borderScale = buttonKindJson.getFloat(UiKey.Button.BORDER_SCALE);
            }

            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = fontResolver.getFont(fontName, 1.0f);

            String fontColorStr = buttonKindJson.getString(UiKey.Button.FONT_COLOR);
            if (fontColorStr == null)
            {
                LogUtils.error(ButtonManager.class, "loadButtonKind fontColor 字段缺失或类型不是字符串 (name): " + buttonKindName);
                return false;
            }
            Color fontColor = Color.valueOf(fontColorStr);
            style.fontColor = new Color(fontColor);
            style.downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
            style.disabledFontColor = fontColor.cpy().mul(0.5f, 0.5f, 0.5f, 1f);

            JsonEntity imageJson = buttonKindJson.getJsonEntityByKey(UiKey.Button.Image.KEY);
            FileHandle resImgDir = themePath.child(PathName.ASSET_S_RESOURCE_IMAGE);
            Pixmap upPix = new Pixmap(resImgDir.child(imageJson.getString(UiKey.Button.Image.UP)));
            Pixmap downPix = new Pixmap(resImgDir.child(imageJson.getString(UiKey.Button.Image.DOWN)));
            Pixmap disabledPix = new Pixmap(resImgDir.child(imageJson.getString(UiKey.Button.Image.DISABLED)));

            pendingPixmapMap.put(UiManager.PIXMAP_BUTTON + buttonKindName + "_up", upPix);
            pendingPixmapMap.put(UiManager.PIXMAP_BUTTON + buttonKindName + "_down", downPix);
            pendingPixmapMap.put(UiManager.PIXMAP_BUTTON + buttonKindName + "_disabled", disabledPix);

            JsonEntity audioJson = buttonKindJson.getJsonEntityByKey(UiKey.Button.Audio.KEY);
            FileHandle audioFileHandle = themePath.child(PathName.ASSET_S_RESOURCE_AUDIO)
                .child(audioJson.getString(UiKey.Button.Audio.CLICK));

            pendingButtonStyles.put(buttonKindName, style);
            pendingButtonAudios.put(buttonKindName, audioFileHandle);
            pendingButtonBorderScales.put(buttonKindName, borderScale);
            LogUtils.debug(ButtonManager.class, "暂存按钮 pixmap: " + buttonKindName);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "loadButtonKind", e);
            return false;
        }
    }

    /**
     * 获取所有按钮样式映射表
     */
    public Map<String, ButtonKind> getButtonKindMap ()
    {
        return buttonKindMap;
    }

    /**
     * 根据名称获取按钮样式
     */
    public ButtonKind getButtonKind (String buttonKindName)
    {
        return buttonKindMap.get(buttonKindName);
    }

    /**
     * 移除指定名称的按钮样式
     */
    public boolean removeButtonKind (String buttonKindName)
    {
        try
        {
            ButtonKind buttonKind = buttonKindMap.get(buttonKindName);
            if (buttonKind != null)
            {
                buttonKindMap.remove(buttonKindName);
                LogUtils.debug(ButtonManager.class, "removeButtonKind 移除按钮样式成功 (name): " + buttonKindName);
                return true;
            }
            else
            {
                LogUtils.error(ButtonManager.class, "removeButtonKind 移除按钮样式失败 (name): " + buttonKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "removeButtonKind", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Object creation
    // ===================================================================================================================

    private boolean createButton (String buttonTag, String buttonKindName,
                                  float x, float y, float width, float height,
                                  TextObject text, String fontName, float fontSize,
                                  Color fontColor)
    {
        try
        {
            ButtonKind buttonKind = getButtonKind(buttonKindName);
            if (buttonKind == null)
            {
                LogUtils.error(ButtonManager.class, "createButton 找不到按钮样式 (kind): " + buttonKindName);
                return false;
            }

            TextButton.TextButtonStyle buttonStyle = buttonKind.getButtonStyle();
            TextButton.TextButtonStyle finalButtonStyle = new TextButton.TextButtonStyle(buttonStyle);
            finalButtonStyle.font = fontResolver.getFont(fontName, fontSize);
            if (fontColor != null)
            {
                finalButtonStyle.fontColor = fontColor;
                finalButtonStyle.downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
                finalButtonStyle.disabledFontColor = fontColor.cpy().mul(0.5f, 0.5f, 0.5f, 1f);
            }

            // 计算九宫格裁切位置（sourceBorder）与绘制大小（renderBorder）
            int sourceBorder = Math.max(1, (int)(Math.min(width, height) / 16));
            int renderBorder = Math.max(1, (int)(sourceBorder * buttonKind.getBorderScale()));
            if (sourceBorder * 2 < Math.min(width, height))
            {
                finalButtonStyle.up = adaptDrawable(finalButtonStyle.up, sourceBorder, renderBorder);
                finalButtonStyle.down = adaptDrawable(finalButtonStyle.down, sourceBorder, renderBorder);
                if (finalButtonStyle.disabled != null)
                    finalButtonStyle.disabled = adaptDrawable(finalButtonStyle.disabled, sourceBorder, renderBorder);
                LogUtils.debug(ButtonManager.class,
                    "按钮 NinePatch: sourceBorder=" + sourceBorder + " renderBorder=" + renderBorder
                        + " tag=" + buttonTag + " 控件=" + (int) width + "x" + (int) height);
            }
            else
            {
                LogUtils.debug(ButtonManager.class,
                    "按钮 跳过 NinePatch: sourceBorder=" + sourceBorder + " renderBorder=" + renderBorder
                        + " tag=" + buttonTag + " 控件=" + (int) width + "x" + (int) height);
            }

            UiManager.CustomTextButton buttonContainer = new UiManager.CustomTextButton(text, finalButtonStyle, buttonTag, uiManager);
            buttonContainer.setPosition(x, y);
            buttonContainer.setSize(width, height);

            if (!audioManager.loadSound(getButtonStandardTag(buttonTag), buttonKind.getAudioClick()))
            {
                return false;
            }

            buttonContainer.setClickRunnable(() ->
            {
                LogUtils.debug(ButtonManager.class, "button clicked " + getButtonStandardTag(buttonTag));
                try
                {
                    if (!buttonStateMap.containsKey(buttonTag))
                    {
                        buttonStateMap.put(buttonTag, new ButtonState());
                    }
                    buttonStateMap.get(buttonTag).setClicked();
                    audioManager.playSound(getButtonStandardTag(buttonTag), false);

                    Runnable callback = buttonClickCallbackMap.get(buttonTag);
                    if (callback != null)
                    {
                        callback.run();
                    }
                }
                catch (Exception e)
                {
                    LogUtils.error(ButtonManager.class, "button clicked", e);
                }
            });

            stage.addActor(buttonContainer);
            buttonMap.put(buttonTag, buttonContainer);
            buttonKindNameMap.put(buttonTag, buttonKindName);
            buttonStateMap.put(buttonTag, new ButtonState());
            interactableObjectSet.add(buttonContainer);
            LogUtils.debug(ButtonManager.class, "createButton 放置按钮成功 (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "createButton", e);
            return false;
        }
    }

    /**
     * 将 Drawable 包装为自适应 NinePatchDrawable。
     * <p>
     * sourceBorder 为纹理裁切位置，renderBorder 为绘制大小，
     * 二者分离使 borderScale 能真正缩放边框视觉大小。
     * </p>
     */
    private static Drawable adaptDrawable (Drawable drawable, int sourceBorder, int renderBorder)
    {
        if (drawable instanceof TextureRegionDrawable)
        {
            TextureRegion r = ((TextureRegionDrawable) drawable).getRegion();
            int maxSource = Math.min(r.getRegionWidth(), r.getRegionHeight()) / 2 - 1;
            sourceBorder = Math.min(sourceBorder, Math.max(1, maxSource));
            if (sourceBorder * 2 < r.getRegionWidth() && sourceBorder * 2 < r.getRegionHeight())
            {
                NinePatch patch = new NinePatch(r, sourceBorder, sourceBorder, sourceBorder, sourceBorder);
                patch.setLeftWidth(renderBorder);
                patch.setRightWidth(renderBorder);
                patch.setTopHeight(renderBorder);
                patch.setBottomHeight(renderBorder);
                return new NinePatchDrawable(patch);
            }
            LogUtils.debug(ButtonManager.class,
                "adaptDrawable 纹理太小，跳过 NinePatch: 纹理="
                    + r.getRegionWidth() + "x" + r.getRegionHeight());
        }
        return drawable;
    }

    // ===================================================================================================================
    // Public API
    // ===================================================================================================================

    public String getButtonStandardTag (String buttonTag)
    {
        return "[Button] " + buttonTag;
    }

    public boolean addButton (String buttonTag, String buttonKindName,
                              float x, float y, float width, float height,
                              TextObject text, String fontName, float fontSize,
                              Color fontColor)
    {
        try
        {
            if (!buttonMap.containsKey(buttonTag))
            {
                return createButton(buttonTag, buttonKindName, x, y, width, height, text, fontName, fontSize, fontColor);
            }
            else
            {
                return updateButton(buttonTag, buttonKindName, x, y, width, height, text, fontName, fontSize, fontColor);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "addButton", e);
            return false;
        }
    }

    public boolean addButton (ButtonInfo buttonInfo)
    {
        try
        {
            return addButton(
                buttonInfo.getButtonTag(),
                buttonInfo.getButtonKindName(),
                buttonInfo.getX(),
                buttonInfo.getY(),
                buttonInfo.getWidth(),
                buttonInfo.getHeight(),
                buttonInfo.getTextObject(),
                buttonInfo.getFontName(),
                buttonInfo.getFontSize(),
                buttonInfo.getFontColor()
            );
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "addButton", e);
            return false;
        }
    }

    public boolean addButton (List<ButtonInfo> buttonInfoList)
    {
        try
        {
            boolean result = true;
            if (buttonInfoList != null)
            {
                for (ButtonInfo buttonInfo : buttonInfoList)
                {
                    if (!addButton(buttonInfo)) result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "addButton", e);
            return false;
        }
    }

    public boolean addButton (List<String> buttonTagList, Map<String, ButtonInfo> buttonInfoMap)
    {
        try
        {
            boolean result = true;
            if (buttonTagList != null && buttonInfoMap != null)
            {
                for (String buttonTag : buttonTagList)
                {
                    if (buttonInfoMap.containsKey(buttonTag))
                    {
                        if (!addButton(buttonInfoMap.get(buttonTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "addButton", e);
            return false;
        }
    }

    public boolean updateButton (String buttonTag, String buttonKindName,
                                 float x, float y, float width, float height,
                                 TextObject text, String fontName, float fontSize,
                                 Color fontColor)
    {
        try
        {
            LogUtils.debug(ButtonManager.class, "updateButton (tag): " + buttonTag + " (kind): " + buttonKindName);
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "updateButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }

            if (buttonKindName != null && !buttonKindName.isEmpty())
            {
                ButtonKind newKind = buttonKindMap.get(buttonKindName);
                if (newKind != null)
                {
                    buttonKindNameMap.put(buttonTag, buttonKindName);
                }
            }

            if (fontName != null && !fontName.isEmpty())
            {
                BitmapFont newFont = fontResolver.getFont(fontName, fontSize);
                if (newFont != null)
                {
                    buttonContainer.getButtonStyle().font = newFont;
                }
            }

            if (fontColor != null)
            {
                buttonContainer.getButtonStyle().fontColor = fontColor;
                buttonContainer.getButtonStyle().downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
                buttonContainer.getButtonStyle().disabledFontColor = fontColor.cpy().mul(0.5f, 0.5f, 0.5f, 1f);
            }

            if (text != null)
            {
                buttonContainer.setTextObject(text);
            }

            buttonContainer.setPosition(x, y);
            buttonContainer.setSize(width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "updateButton", e);
            return false;
        }
    }

    // ========== Position / Size ==========

    public boolean setButtonPosition (String buttonTag, float x, float y)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonPosition 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setPosition(x, y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonPosition", e);
            return false;
        }
    }

    public float getButtonX (String buttonTag)
    {
        UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
        return buttonContainer != null ? buttonContainer.getX() : 0;
    }

    public boolean setButtonX (String buttonTag, float x)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonX 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setX(x);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonX", e);
            return false;
        }
    }

    public float getButtonY (String buttonTag)
    {
        UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
        return buttonContainer != null ? buttonContainer.getY() : 0;
    }

    public boolean setButtonY (String buttonTag, float y)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonY 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setY(y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonY", e);
            return false;
        }
    }

    public boolean setButtonSize (String buttonTag, float width, float height)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonSize 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setSize(width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonSize", e);
            return false;
        }
    }

    public float getButtonWidth (String buttonTag)
    {
        UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
        return buttonContainer != null ? buttonContainer.getWidth() : 0;
    }

    public boolean setButtonWidth (String buttonTag, float width)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonWidth 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setWidth(width);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonWidth", e);
            return false;
        }
    }

    public float getButtonHeight (String buttonTag)
    {
        UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
        return buttonContainer != null ? buttonContainer.getHeight() : 0;
    }

    public boolean setButtonHeight (String buttonTag, float height)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonHeight 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setHeight(height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonHeight", e);
            return false;
        }
    }

    // ========== Text ==========

    public boolean setButtonText (String buttonTag, TextObject textObject)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "setButtonText 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setTextObject(textObject);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonText", e);
            return false;
        }
    }

    public TextObject getButtonText (String buttonTag)
    {
        UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
        return buttonContainer != null ? buttonContainer.getTextObject() : null;
    }

    public String getButtonBaseText (String buttonTag)
    {
        return buttonBaseTextMap.get(buttonTag);
    }

    // ========== Query ==========

    public boolean containsButton (String buttonTag)
    {
        return buttonMap.containsKey(buttonTag);
    }

    public InteractableObject getButton (String buttonTag)
    {
        return buttonMap.get(buttonTag);
    }

    /**
     * 获取 buttonMap（供 UiManager 布局管理使用）
     */
    public Map<String, UiManager.CustomTextButton> getButtonMap ()
    {
        return buttonMap;
    }

    public Map<String, String> getButtonKindNameMap ()
    {
        return buttonKindNameMap;
    }

    public Map<String, ButtonState> getButtonStateMap ()
    {
        return buttonStateMap;
    }

    public Map<String, String> getButtonBaseTextMap ()
    {
        return buttonBaseTextMap;
    }

    public Map<String, Runnable> getButtonClickCallbackMap ()
    {
        return buttonClickCallbackMap;
    }

    // ========== Enable / Disable ==========

    public boolean enableButton (String buttonTag)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "enableButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setDisabled(false);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "enableButton", e);
            return false;
        }
    }

    public boolean disableButton (String buttonTag)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "disableButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setDisabled(true);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "disableButton", e);
            return false;
        }
    }

    // ========== Show / Hide ==========

    public boolean showButton (String buttonTag)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "showButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setVisible(true);
            LogUtils.debug(ButtonManager.class, "showButton 成功 (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "showButton", e);
            return false;
        }
    }

    public boolean hideButton (String buttonTag)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "hideButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.setVisible(false);
            LogUtils.debug(ButtonManager.class, "hideButton 成功 (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "hideButton", e);
            return false;
        }
    }

    // ========== Click ==========

    public boolean setButtonClicked (String buttonTag, boolean clicked)
    {
        try
        {
            if (!buttonStateMap.containsKey(buttonTag))
            {
                if (!clicked) return true;
                buttonStateMap.put(buttonTag, new ButtonState());
            }
            if (clicked)
            {
                buttonStateMap.get(buttonTag).setClicked();
            }
            else
            {
                buttonStateMap.get(buttonTag).setClicked(false);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "setButtonClicked", e);
            return false;
        }
    }

    public void setButtonClickCallback (String buttonTag, Runnable callback)
    {
        if (callback != null)
        {
            buttonClickCallbackMap.put(buttonTag, callback);
        }
        else
        {
            buttonClickCallbackMap.remove(buttonTag);
        }
    }

    public boolean isButtonClicked (String buttonTag)
    {
        return buttonStateMap.containsKey(buttonTag) && buttonStateMap.get(buttonTag).isClicked();
    }

    // ========== Delete ==========

    public boolean deleteButton (String buttonTag)
    {
        try
        {
            UiManager.CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            if (buttonContainer == null)
            {
                LogUtils.error(ButtonManager.class, "deleteButton 找不到按钮对象 (tag): " + buttonTag);
                return false;
            }
            buttonContainer.remove();
            buttonMap.remove(buttonTag);
            buttonKindNameMap.remove(buttonTag);
            buttonStateMap.remove(buttonTag);
            buttonBaseTextMap.remove(buttonTag);
            buttonClickCallbackMap.remove(buttonTag);
            interactableObjectSet.remove(buttonContainer);
            LogUtils.debug(ButtonManager.class, "deleteButton (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "deleteButton", e);
            return false;
        }
    }

    public boolean deleteAllButton ()
    {
        try
        {
            LogUtils.debug(ButtonManager.class, "deleteAllButton");
            for (UiManager.CustomTextButton buttonContainer : buttonMap.values())
            {
                buttonContainer.remove();
                interactableObjectSet.remove(buttonContainer);
            }
            buttonMap.clear();
            buttonKindNameMap.clear();
            buttonStateMap.clear();
            buttonBaseTextMap.clear();
            buttonClickCallbackMap.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ButtonManager.class, "deleteAllButton", e);
            return false;
        }
    }
}
