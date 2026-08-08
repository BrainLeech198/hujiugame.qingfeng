package com.hujiugame.qingfeng.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.ui.kind.TextObject;
import com.hujiugame.qingfeng.ui.kind.UiInfo;
import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.button.ButtonKind;
import com.hujiugame.qingfeng.ui.kind.button.ButtonState;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.ui.kind.image.ImageKind;
import com.hujiugame.qingfeng.ui.kind.image.ImageState;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelKind;
import com.hujiugame.qingfeng.ui.kind.label.LabelState;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.JsonKey;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.type.ui.FontFlag;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.manager.TextManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;
import com.hujiugame.qingfeng.util.system.SafePostRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class UiManager
{
    // Pixmap 合并时用于标识不同类型的 key 前缀
    public static final String PIXMAP_IMAGE = "IMG_";
    public static final String PIXMAP_LABEL = "LB_";
    public static final String PIXMAP_BUTTON = "BT_";
    private final Stage stage;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final Map<String, CustomFont> fontMap = new HashMap<>();
    // 标签打字机速度常量（仍在现有 createLabel 方法中使用）
    private static final float LABEL_TEXT_TYPING_SPEED = 25.0f;

    // ========== Manager 实例 ==========
    private final ImageManager imageManager;
    private final LabelManager labelManager;
    private final ButtonManager buttonManager;

    // 全局存在的可交互对象（引用传递给各 Manager）
    private final HashSet<InteractableObject> interactableObjectSet = new HashSet<>();

    // ========== 兼容性映射（引用自各 Manager，保持现有方法代码兼容） ==========
    private Map<String, CustomImage> imageMap;
    private Map<String, ImageKind> imageKindMap;
    private Map<String, String> imageKindNameMap;
    private Map<String, ImageState> imageStateMap;
    private Map<String, CustomLabel> labelMap;
    private Map<String, LabelKind> labelKindMap;
    private Map<String, String> labelKindNameMap;
    private Map<String, String> labelBaseTextMap;
    private Map<String, LabelState> labelStateMap;
    private Map<String, CustomTextButton> buttonMap;
    private Map<String, ButtonKind> buttonKindMap;
    private Map<String, String> buttonKindNameMap;
    private Map<String, ButtonState> buttonStateMap;
    private Map<String, String> buttonBaseTextMap;
    private Map<String, Runnable> buttonClickCallbackMap;

    // ========== UI 配置清单 ==========
    private Set<String> availableButtonStyles = Collections.emptySet();
    private Set<String> availableLabelStyles = Collections.emptySet();
    private Set<String> availableImageStyles = Collections.emptySet();
    private Set<String> availableFontStyles = Collections.emptySet();
    private Set<String> availableMessageBoxStyles = Collections.emptySet();
    // 弹窗管理器
    private MessageBox messageBox;

    // ========== Pixmap 合并相关 ==========
    // 暂存所有待合并的小 Pixmap，Key 为唯一标识
    private final Map<String, Pixmap> pendingPixmapMap = new LinkedHashMap<>();
    // 暂存还未成型的样式信息，等 Pixmap 合并完后才能生成 Kind
    private final Map<String, Label.LabelStyle> pendingLabelStyles = new HashMap<>();
    private final Map<String, TextButton.TextButtonStyle> pendingButtonStyles = new HashMap<>();
    private final Map<String, FileHandle> pendingButtonAudios = new HashMap<>();
    private final Map<String, Float> pendingLabelBorderScales = new HashMap<>();
    private final Map<String, Float> pendingButtonBorderScales = new HashMap<>();
    // ========== 异步销毁相关 ==========
    private final ScheduledExecutorService disposeExecutor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        Thread thread = new Thread(r, "UiManager-Dispose-Thread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) ->
            LogUtils.error(UiManager.class, "Dispose线程异常", (Exception) e));
        return thread;
    });
    private final Map<Texture, Object> disposeTextureQueue = new ConcurrentHashMap<>();  // 待销毁纹理
    private final Map<BitmapFont, Object> disposeFontQueue = new ConcurrentHashMap<>();   // 待销毁字体
    // 弹窗遮盖
    private Table maskLayer;
    // ========= font ==========
    // 字体样式缓存
    private String defaultFontName = null;
    // 合并后的大纹理 identifier -> 大纹理上的区域
    private Texture atlasTexture;
    private Map<String, TextureRegion> regionMap = new HashMap<>();
    // ========== layout Group ==========
    // Layout → Group，用于快速 show/hide 整个布局
    private final Map<Layout, Group> layoutGroupMap = new IdentityHashMap<>();

    // ===================================================================================================================

    /**
     * 字体解析接口，供子管理器获取字体
     */
    @FunctionalInterface
    public interface FontResolver
    {
        BitmapFont getFont (String fontName, float fontSize);
    }

    /**
     * 构造一个 UiManager 实例
     *
     * @param stage           舞台对象，用于管理 UI 控件
     * @param audioManager    音频管理器，用于加载和播放音效
     * @param graphicsManager 图形管理器，用于纹理加载等图形操作
     * @param textManager     文本管理器，用于弹窗文本解析
     */
    public UiManager (Stage stage, AudioManager audioManager, GraphicsManager graphicsManager, TextManager textManager)
    {
        this.stage = stage;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.imageManager = new ImageManager(stage, graphicsManager, interactableObjectSet, this);
        this.labelManager = new LabelManager(stage, graphicsManager, interactableObjectSet, this::getFont, this);
        this.buttonManager = new ButtonManager(stage, audioManager, graphicsManager, interactableObjectSet, this::getFont, this);

        // 兼容性映射：引用自各 Manager，使现有方法代码无需修改
        this.imageMap = imageManager.getImageMap();
        this.imageKindMap = imageManager.getImageKindMap();
        this.imageKindNameMap = imageManager.getImageKindNameMap();
        this.imageStateMap = imageManager.getImageStateMap();
        this.labelMap = labelManager.getLabelMap();
        this.labelKindMap = labelManager.getLabelKindMap();
        this.labelKindNameMap = labelManager.getLabelKindNameMap();
        this.labelStateMap = labelManager.getLabelStateMap();
        this.labelBaseTextMap = labelManager.getLabelBaseTextMap();
        this.buttonMap = buttonManager.getButtonMap();
        this.buttonKindMap = buttonManager.getButtonKindMap();
        this.buttonKindNameMap = buttonManager.getButtonKindNameMap();
        this.buttonStateMap = buttonManager.getButtonStateMap();
        this.buttonBaseTextMap = buttonManager.getButtonBaseTextMap();
        this.buttonClickCallbackMap = buttonManager.getButtonClickCallbackMap();

        // 弹窗
        this.messageBox = new MessageBox(stage, textManager);
    }

    /**
     * 获取弹窗管理器
     *
     * @return MessageBox 实例
     */
    public MessageBox getMessageBox ()
    {
        return messageBox;
    }

    /**
     * 加载 UI 配置清单（ui_config.json），记录所有可用的组件样式名
     *
     * @param themeManager 主题管理器
     */
    private void loadUiConfig (ThemeManager themeManager)
    {
        try
        {
            FileHandle configPath = themeManager.getPathHandle().child(PathName.ASSET_S_UI).child(FileName.THEME_S_UI_CONFIG);
            JsonEntity configJson = new JsonEntity(configPath);

            if (configJson.isEmpty())
            {
                return;
            }

            for (String category : configJson.keySet())
            {
                List<String> styles = configJson.getStringList(category);
                if (styles == null || styles.isEmpty())
                {
                    continue;
                }
                Set<String> styleSet = new HashSet<>(styles);
                switch (category)
                {
                    case UiKey.Button.KEY:
                        availableButtonStyles = styleSet;
                        break;
                    case UiKey.Label.KEY:
                        availableLabelStyles = styleSet;
                        break;
                    case UiKey.Image.KEY:
                        availableImageStyles = styleSet;
                        break;
                    case UiKey.Font.KEY:
                        availableFontStyles = styleSet;
                        break;
                    case UiKey.MessageBox.KEY:
                        availableMessageBoxStyles = styleSet;
                        break;
                }
            }

            LogUtils.debug(UiManager.class,
                "loadUiConfig 加载 UI 配置清单");
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadUiConfig", e);
        }
    }

    /**
     * 从主题中加载字体样式
     *
     * @param themeManager 主题管理器，提供主题路径和字体配置
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadFontFromTheme (ThemeManager themeManager)
    {
        try
        {
            defaultFontName = themeManager.getFont();

            FileHandle uiFontDirectory = themeManager.getPathHandle().child(PathName.ASSET_S_UI_FONT);
            FileHandle themeDirectory = themeManager.getPathHandle();
            LogUtils.debug(UiManager.class, "loadFontFromTheme 加载目录下的字体样式 (file): " + uiFontDirectory);

            float[] fontUseSize = themeManager.getFontUseSize();

            if (uiFontDirectory != null)
            {
                FileHandle[] fontDirectoryList = FileUtils.getList(uiFontDirectory);
                if (fontDirectoryList != null)
                {
                    for (FileHandle fontDirectory : fontDirectoryList)
                    {
                        if (!loadFont(fontDirectory, fontUseSize))
                        {
                            LogUtils.error(UiManager.class, "loadFontFromTheme 字体样式加载失败 (file): " + fontDirectory.path());
                            return false;
                        }
                    }
                }
            }

            LogUtils.debug(UiManager.class, "loadFontFromTheme 加载字体样式完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadButtonKindFromTheme", e);
            return false;
        }
    }

    /**
     * 从主题中加载图像样式
     *
     * @param themeManager 主题管理器，提供主题路径和图像配置
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadImageKindFromTheme (ThemeManager themeManager)
    {
        try
        {
            FileHandle uiImageDirectory = themeManager.getPathHandle().child(PathName.ASSET_S_UI_IMAGE);
            FileHandle themeDirectory = themeManager.getPathHandle();
            LogUtils.debug(UiManager.class, "loadImageKindFromTheme 加载目录下的图像样式 (file): " + uiImageDirectory);

            if (uiImageDirectory != null)
            {
                FileHandle[] imageFileList = FileUtils.getList(uiImageDirectory);
                if (imageFileList != null)
                {
                    for (FileHandle imageFileHandle : imageFileList)
                    {
                        if (!imageManager.loadImageKind(imageFileHandle, themeDirectory, pendingPixmapMap))
                        {
                            return false;
                        }
                    }
                }
            }
            else
            {
                LogUtils.error(UiManager.class, "loadImageKindFromTheme 加载目录下的图像样式失败 找不到图像样式目录");
                return false;
            }

            LogUtils.debug(UiManager.class, "loadImageKindFromTheme 加载图像样式完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadImageKindFromTheme", e);
            return false;
        }
    }

    /**
     * 从主题中加载标签样式
     *
     * @param themeManager 主题管理器，提供主题路径和标签配置
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLabelKindFromTheme (ThemeManager themeManager)
    {
        try
        {
            FileHandle uiLabelDirectory = themeManager.getPathHandle().child(PathName.ASSET_S_UI_LABEL);
            FileHandle themeDirectory = themeManager.getPathHandle();
            LogUtils.debug(UiManager.class, "loadLabelKindFromTheme 加载目录下的标签样式 (file): " + uiLabelDirectory);

            if (uiLabelDirectory != null)
            {
                FileHandle[] labelFileList = FileUtils.getList(uiLabelDirectory);
                if (labelFileList != null)
                {
                    for (FileHandle labelFileHandle : labelFileList)
                    {
                        if (!labelManager.loadLabelKind(labelFileHandle, themeDirectory, pendingPixmapMap, pendingLabelStyles, pendingLabelBorderScales))
                            return false;
                    }
                }
            }
            else
            {
                LogUtils.error(UiManager.class, "loadLabelKindFromTheme 加载目录下的标签样式失败 找不到标签样式目录");
                return false;
            }

            LogUtils.debug(UiManager.class, "loadLabelKindFromTheme 加载标签样式完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadLabelKindFromTheme", e);
            return false;
        }
    }

    /**
     * 从主题中加载按钮样式
     *
     * @param themeManager 主题管理器，提供主题路径和按钮配置
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadButtonKindFromTheme (ThemeManager themeManager)
    {
        try
        {
            FileHandle uiButtonDirectory = themeManager.getPathHandle().child(PathName.ASSET_S_UI_BUTTON);
            FileHandle themeDirectory = themeManager.getPathHandle();
            LogUtils.debug(UiManager.class, "loadButtonKindFromTheme 加载目录下的按钮样式 (file): " + uiButtonDirectory);

            if (uiButtonDirectory != null)
            {
                FileHandle[] buttonFileList = FileUtils.getList(uiButtonDirectory);
                if (buttonFileList != null)
                {
                    for (FileHandle buttonFileHandle : buttonFileList)
                    {
                        if (!buttonManager.loadButtonKind(buttonFileHandle, themeDirectory, pendingPixmapMap, pendingButtonStyles, pendingButtonAudios, pendingButtonBorderScales))
                        {
                            return false;
                        }
                    }
                }
            }
            else
            {
                LogUtils.error(UiManager.class, "loadButtonKindFromTheme 加载目录下的按钮样式失败 找不到按钮样式目录");
                return false;
            }

            LogUtils.debug(UiManager.class, "loadButtonKindFromTheme 加载按钮样式完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadButtonKindFromTheme", e);
            return false;
        }
    }

    /**
     * 根据纹理 region 和 border key 创建 Drawable。
     * 有有效 NinePatch border 时创建 NinePatchDrawable，否则回退为 TextureRegionDrawable。
     *
     * @param region    纹理区域
     * @param borderKey pendingPixmapBorders 中对应的 key
     * @return Drawable 实例
     */
    private Drawable buildDrawable (TextureRegion region, String borderKey)
    {
        return new TextureRegionDrawable(region);
    }

    /**
     * 构建标签背景 Image。
     * <p>
     * sourceBorder 决定从源纹理边缘取多少像素作为九宫格裁切位置（固定比例 = 控件/16），
     * renderBorder 决定这些像素在屏幕上绘制多大（= sourceBorder × borderScale）。
     * 二者分离使 borderScale 能真正缩放边框视觉大小，而非改变裁切深度。
     * </p>
     */
    private Image buildLabelBackground (LabelKind labelKind, int sourceBorder, int renderBorder)
    {
        TextureRegion r = labelKind.getBackgroundRegion();
        if (sourceBorder * 2 < r.getRegionWidth() && sourceBorder * 2 < r.getRegionHeight())
        {
            NinePatch patch = new NinePatch(r, sourceBorder, sourceBorder, sourceBorder, sourceBorder);
            patch.setLeftWidth(renderBorder);
            patch.setRightWidth(renderBorder);
            patch.setTopHeight(renderBorder);
            patch.setBottomHeight(renderBorder);
            LogUtils.debug(UiManager.class,
                "buildLabelBackground NinePatch: sourceBorder=" + sourceBorder
                    + " renderBorder=" + renderBorder
                    + " 纹理=" + r.getRegionWidth() + "x" + r.getRegionHeight());
            return new Image(new NinePatchDrawable(patch));
        }
        LogUtils.debug(UiManager.class,
            "buildLabelBackground 纹理太小，跳过 NinePatch: 纹理="
                + r.getRegionWidth() + "x" + r.getRegionHeight());
        return new Image(r);
    }

    /**
     * 构建标签背景 Drawable，有有效 NinePatch border 时使用 NinePatchDrawable
     *
     * @param labelKind     标签样式
     * @param labelKindName 标签样式名称（用于查找 border）
     * @return Drawable 实例
     */
    private Drawable buildLabelDrawable (LabelKind labelKind, String labelKindName)
    {
        return new TextureRegionDrawable(labelKind.getBackgroundRegion());
    }

    /**
     * 使用合并后的纹理区域填充所有暂存的样式（图片、标签、按钮）
     */
    private void fillKindsWithRegions ()
    {
        // 图片
        Map<String, ImageKind> imgKindMap = imageManager.getImageKindMap();
        for (Map.Entry<String, ImageKind> entry : imgKindMap.entrySet())
        {
            if (entry.getValue() == null)
            {
                String regionKey = PIXMAP_IMAGE + entry.getKey();
                TextureRegion region = regionMap.get(regionKey);
                if (region != null)
                {
                    entry.setValue(new ImageKind(region));
                }
                else
                {
                    LogUtils.error(UiManager.class,
                        "fillKindsWithRegions 找不到图片 region: " + regionKey);
                }
            }
        }

        // 标签
        Map<String, LabelKind> lblKindMap = labelManager.getLabelKindMap();
        for (Map.Entry<String, Label.LabelStyle> entry : pendingLabelStyles.entrySet())
        {
            String name = entry.getKey();
            String regionKey = PIXMAP_LABEL + name;
            TextureRegion region = regionMap.get(regionKey);
            if (region != null)
            {
                float labelBorderScale = pendingLabelBorderScales.containsKey(name)
                    ? pendingLabelBorderScales.get(name) : 1.0f;
                lblKindMap.put(name, new LabelKind(entry.getValue(), region, labelBorderScale));
                LogUtils.debug(UiManager.class,
                    "标签 kind 绑定: " + name + " region=" + region.getRegionWidth() + "x" + region.getRegionHeight());
            }
            else
            {
                LogUtils.error(UiManager.class,
                    "fillKindsWithRegions 找不到标签背景 region: " + regionKey);
            }
        }
        pendingLabelStyles.clear();

        // 按钮
        Map<String, ButtonKind> btnKindMap = buttonManager.getButtonKindMap();
        for (Map.Entry<String, TextButton.TextButtonStyle> entry : pendingButtonStyles.entrySet())
        {
            String name = entry.getKey();
            TextButton.TextButtonStyle style = entry.getValue();

            TextureRegion upRegion = regionMap.get(PIXMAP_BUTTON + name + "_up");
            TextureRegion downRegion = regionMap.get(PIXMAP_BUTTON + name + "_down");
            TextureRegion disRegion = regionMap.get(PIXMAP_BUTTON + name + "_disabled");

            if (upRegion == null || downRegion == null || disRegion == null)
            {
                LogUtils.error(UiManager.class,
                    "fillKindsWithRegions 按钮 region 缺失: " + name);
                continue;
            }

            style.up = buildDrawable(upRegion, PIXMAP_BUTTON + name + "_up");
            style.down = buildDrawable(downRegion, PIXMAP_BUTTON + name + "_down");
            style.disabled = buildDrawable(disRegion, PIXMAP_BUTTON + name + "_disabled");

            LogUtils.debug(UiManager.class,
                "按钮 kind 绑定: " + name + " up=" + upRegion.getRegionWidth() + "x" + upRegion.getRegionHeight()
                    + " down=" + downRegion.getRegionWidth() + "x" + downRegion.getRegionHeight()
                    + " disabled=" + disRegion.getRegionWidth() + "x" + disRegion.getRegionHeight());

            float btnBorderScale = pendingButtonBorderScales.containsKey(name)
                ? pendingButtonBorderScales.get(name) : 1.0f;
            btnKindMap.put(name,
                new ButtonKind(style, pendingButtonAudios.get(name), btnBorderScale));
        }
        pendingButtonStyles.clear();
        pendingButtonAudios.clear();
        pendingLabelBorderScales.clear();
        pendingButtonBorderScales.clear();
    }

    /**
     * 将所有暂存的小 Pixmap 合并为一张大纹理，生成对应的 TextureRegion
     *
     * @return 合并成功返回 true，失败返回 false
     */
    private boolean packPendingPixmaps ()
    {
        if (pendingPixmapMap.isEmpty())
        {
            return true;
        }

        try
        {
            // 计算大图尺寸（垂直堆叠：宽度取最大，高度累加）
            int maxWidth = 0;
            int totalHeight = 0;
            for (Pixmap p : pendingPixmapMap.values())
            {
                maxWidth = Math.max(maxWidth, p.getWidth());
                totalHeight += p.getHeight();
            }

            // 创建大 Pixmap，按顺序粘贴小图
            Pixmap atlasPixmap = new Pixmap(maxWidth, totalHeight, Pixmap.Format.RGBA8888);
            Map<String, Rectangle> rectMap = new LinkedHashMap<>();
            int currentY = 0;
            for (Map.Entry<String, Pixmap> entry : pendingPixmapMap.entrySet())
            {
                Pixmap p = entry.getValue();
                atlasPixmap.drawPixmap(p, 0, currentY);
                rectMap.put(entry.getKey(),
                    new Rectangle(0, currentY, p.getWidth(), p.getHeight()));
                LogUtils.debug(UiManager.class,
                    "合并 Pixmap: " + entry.getKey() + " " + p.getWidth() + "x" + p.getHeight()
                        + " @(0," + currentY + ")");
                currentY += p.getHeight();
                p.dispose();
            }

            // 生成 GPU 纹理
            atlasTexture = new Texture(atlasPixmap);
            atlasPixmap.dispose();

            // 生成所有区域
            regionMap.clear();
            for (Map.Entry<String, Rectangle> e : rectMap.entrySet())
            {
                Rectangle r = e.getValue();
                regionMap.put(e.getKey(),
                    new TextureRegion(atlasTexture, (int) r.x, (int) r.y,
                        (int) r.width, (int) r.height));
            }

            // 用区域填充 Kind
            fillKindsWithRegions();
            pendingPixmapMap.clear();
            LogUtils.debug(UiManager.class, "Pixmap 合并完成，纹理尺寸: "
                + maxWidth + "x" + totalHeight);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "packPendingPixmaps 合并失败", e);
            return false;
        }
    }

    /**
     * 初始化 UiManager，依次加载字体、图像、标签和按钮样式，最后合并所有小图纹理
     *
     * @param themeManager 主题管理器，提供所有 UI 资源的配置路径
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init (ThemeManager themeManager)
    {
        try
        {
            // 加载 UI 配置清单
            loadUiConfig(themeManager);

            if (!loadFontFromTheme(themeManager))
            {
                LogUtils.error(UiManager.class, "init 加载目录下的字体失败");
                return false;
            }
            else
            {
                LogUtils.debug(UiManager.class, "init 加载目录下的字体成功");
            }

            if (!loadImageKindFromTheme(themeManager))
            {
                LogUtils.error(UiManager.class, "init 加载目录下的图像失败");
                return false;
            }
            else
            {
                LogUtils.debug(UiManager.class, "init 加载目录下的图像成功");
            }

            if (!loadLabelKindFromTheme(themeManager))
            {
                LogUtils.error(UiManager.class, "init 加载目录下的标签失败");
                return false;
            }
            else
            {
                LogUtils.debug(UiManager.class, "init 加载目录下的标签成功");
            }

            if (!loadButtonKindFromTheme(themeManager))
            {
                LogUtils.error(UiManager.class, "init 加载目录下的按钮失败");
                return false;
            }
            else
            {
                LogUtils.debug(UiManager.class, "init 加载目录下的按钮成功");
            }

            // 所有小图收集完毕，执行合并
            if (!packPendingPixmaps())
            {
                LogUtils.error(UiManager.class, "init 合并 Pixmap 失败");
                return false;
            }
            else
            {
                LogUtils.debug(UiManager.class, "init 纹理合并成功");
            }

            // 初始化弹窗管理器
            if (!messageBox.init(audioManager, this, themeManager))
            {
                LogUtils.error(UiManager.class, "init 弹窗初始化失败");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "init", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 将 UiManager 注册到 GraphicsManager，以便图形模块引用 UI 字体
     *
     * @param graphicsManager 图形管理器实例
     * @return 注册成功返回 true，失败返回 false
     */
    public boolean setGraphicsQuoteFont (GraphicsManager graphicsManager)
    {
        try
        {
            return graphicsManager.quoteUiManager(this);
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setGraphicsQuoteFont", e);
            return false;
        }
    }

    /**
     * 获取弹窗遮盖层
     *
     * @return 遮盖层 Table 对象
     */
    public Table getMaskLayer ()
    {
        return maskLayer;
    }

    /**
     * 设置弹窗遮盖层
     *
     * @param maskLayer 遮盖层 Table 对象
     */
    public void setMaskLayer (Table maskLayer)
    {
        this.maskLayer = maskLayer;
    }

    /**
     * 从指定目录加载一个字体样式
     *
     * @param directory 字体配置所在目录
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadFont (FileHandle directory, float[] fontUseSize)
    {
        try
        {
            if (FileUtils.isDirectoryExist(directory))
            {
                FileHandle fontJsonFileHandle = directory.child(FileName.THEME_S_UI_FONT_S_CONFIG);
                JsonEntity fontJson = new JsonEntity(fontJsonFileHandle);
                LogUtils.debug(UiManager.class, "loadFont 读取字体配置 (json): " + fontJson);

                String fontTag = fontJson.getString(UiKey.Font.NAME);
                if (fontTag == null)
                {
                    LogUtils.error(UiManager.class, "loadFont 字体配置缺少name字段 (json): " + fontJson);
                    return false;
                }

                String fontFile = fontJson.getString(UiKey.Font.PATH);
                if (fontFile == null)
                {
                    LogUtils.error(UiManager.class, "loadFont 字体配置缺少path字段 (json): " + fontJson);
                    return false;
                }

                float fontScale = fontJson.getFloat(UiKey.Font.SCALE);
                if (fontScale <= 0)
                {
                    LogUtils.error(UiManager.class, "loadFont 字体配置缺少scale字段 (json): " + fontJson);
                    return false;
                }

                FileHandle bitmapFontFileHandle = directory.child(fontFile);
                BitmapFont bitmapFont = new BitmapFont(bitmapFontFileHandle);
                CustomFont customFont = new CustomFont(bitmapFont, fontScale, fontUseSize);
                fontMap.put(fontTag, customFont);

                LogUtils.debug(UiManager.class, "loadFont 加载字体成功 (tag): " + fontTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "loadFont 加载字体失败 传入错误的FileHandle");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "loadFont 加载字体失败", e);
            return false;
        }
    }

    /**
     * 获取指定标签和大小的字体
     *
     * @param tag      字体标签，为 null 时使用默认字体
     * @param fontSize 字体大小缩放系数
     * @return 对应的 BitmapFont，失败返回 null
     */
    public BitmapFont getFont (String tag, float fontSize)
    {
        try
        {
            if (tag == null) tag = defaultFontName;
            if (fontMap.containsKey(tag))
            {
                return fontMap.get(tag).getFont(fontSize);
            }
            else
            {
                LogUtils.error(UiManager.class, "getFont 获取字体失败 找不到字体 (tag): " + tag);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "scaleFont 缩放字体失败", e);
            return null;
        }
    }

    // ===================================================================================================================

    /**
     * 获取图片的标准标签（添加 ui.image. 前缀）
     *
     * @param imageTag 图片原始标签
     * @return 标准化的标签字符串
     */
    public String getImageStandardTag (String imageTag)
    {
        return "ui.image." + imageTag;
    }

    /**
     * 从文件加载图像样式（读取 JSON 配置，暂存 Pixmap 待后续合并）
     *
     * @param file      图像样式配置文件
     * @param themePath 主题根目录
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadImageKind (FileHandle file, FileHandle themePath)
    {
        return imageManager.loadImageKind(file, themePath, pendingPixmapMap);
    }

    /**
     * 直接从纹理文件加载图像样式（不经过 Pixmap 合并流程）
     *
     * @param imageKindName 图像样式名称
     * @param file          纹理文件
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadImageKind (String imageKindName, FileHandle file)
    {
        return imageManager.loadImageKind(imageKindName, file);
    }

    /**
     * 获取所有图像样式映射表
     *
     * @return 图像样式名称到 ImageKind 的映射
     */
    public Map<String, ImageKind> getImageKindMap ()
    {
        return imageManager.getImageKindMap();
    }

    /**
     * 根据名称获取图像样式
     *
     * @param imageKindName 图像样式名称
     * @return 对应的 ImageKind，不存在返回 null
     */
    public ImageKind getImageKind (String imageKindName)
    {
        return imageManager.getImageKind(imageKindName);
    }

    /**
     * 移除指定名称的图像样式，并异步销毁其纹理
     *
     * @param imageKindName 图像样式名称
     * @return 移除成功返回 true，失败返回 false
     */
    public boolean removeImageKind (String imageKindName)
    {
        try
        {
            ImageKind imageKind = imageKindMap.get(imageKindName);
            if (imageKind != null)
            {
                imageKindMap.remove(imageKindName);
                Texture tex = imageKind.getTexture();
                if (tex != null)
                {
                    scheduleDisposeTexture(tex);
                }
                LogUtils.debug(UiManager.class, "removeImageKind 移除图片样式成功 (name): " + imageKindName);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "removeImageKind 移除图片样式失败 (name): " + imageKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "removeImageKind", e);
            return false;
        }
    }

    /**
     * 创建一个图片控件并添加到舞台
     *
     * @param imageTag      图片标签
     * @param imageKindName 图像样式名称
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         宽度
     * @param height        高度
     * @return 创建成功返回 true，失败返回 false
     */
    private boolean createImage (String imageTag, String imageKindName,
                                 float x, float y, float width, float height)
    {
        try
        {
            ImageKind imageKind = getImageKind(imageKindName);
            Image image;
            if (imageKind != null)
            {
                image = new Image(imageKind.getRegion());
            }
            else
            {
                image = new Image();
            }
            CustomImage imageContainer = new CustomImage(image, imageTag, this);
            imageContainer.setPosition(x, y);
            imageContainer.setSize(width, height);

            imageContainer.addListener(new ClickListener()
            {
                public void clicked (InputEvent event, float x, float y)
                {
                    LogUtils.debug(UiManager.class,
                        "image clicked: " + getImageStandardTag(imageTag));
                    if (!imageStateMap.containsKey(imageTag))
                    {
                        imageStateMap.put(imageTag, new ImageState());
                    }
                    imageStateMap.get(imageTag).setClicked();
                }
            });

            stage.addActor(imageContainer);
            imageMap.put(imageTag, imageContainer);
            imageKindNameMap.put(imageTag, imageKindName);
            imageStateMap.put(imageTag, new ImageState());
            addInteractableObject(imageContainer);
            LogUtils.debug(UiManager.class, "createImage 成功: " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "createImage 异常", e);
            return false;
        }
    }

    /**
     * 更新已有图片控件的样式、位置和大小
     *
     * @param imageTag      图片标签
     * @param imageKindName 新的图像样式名称
     * @param x             新的 x 坐标
     * @param y             新的 y 坐标
     * @param width         新的宽度
     * @param height        新的高度
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateImage (
        String imageTag,
        String imageKindName,
        float x,
        float y,
        float width,
        float height
    )
    {
        try
        {
            CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null) return false;

            ImageKind newKind = getImageKind(imageKindName);
            if (newKind != null)
            {
                imageContainer.setDrawable(new TextureRegionDrawable(newKind.getRegion()));
                imageKindNameMap.put(imageTag, imageKindName);
            }
            if (imageContainer.getX() != x || imageContainer.getY() != y)
            {
                imageContainer.setPosition(x, y);
                LogUtils.debug(UiManager.class, "updateImage 更新图片位置 (tag): " + imageTag + " (x): " + x + " (y): " + y);
            }
            if (imageContainer.getWidth() != width || imageContainer.getHeight() != height)
            {
                imageContainer.setSize(width, height);
                LogUtils.debug(UiManager.class, "updateImage 更新图片大小 (tag): " + imageTag + " (width): " + width + " (height): " + height);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "updateImage", e);
            return false;
        }
    }

    /**
     * 设置图片控件位置
     *
     * @param imageTag 图片标签
     * @param x        x 坐标
     * @param y        y 坐标
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImagePosition (String imageTag, float x, float y)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setPosition(x, y);
                LogUtils.debug(UiManager.class, "setImagePosition 配置图片位置成功 (tag): " + imageTag + " (x): " + x + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImagePosition 配置图片位置失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImagePosition", e);
            return false;
        }
    }

    /**
     * 获取图片控件的 x 坐标
     *
     * @param imageTag 图片标签
     * @return x 坐标值，失败返回 0
     */
    public float getImageX (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                return imageMap.get(imageTag).getX();
            }
            else
            {
                LogUtils.error(UiManager.class, "getImageX 错误:不存在标签 (tag):");
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getImageX", e);
            return 0;
        }
    }

    /**
     * 设置图片控件的 x 坐标
     *
     * @param imageTag 图片标签
     * @param x       x 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageX (String imageTag, float x)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setX(x);
                LogUtils.debug(UiManager.class, "setImageX 配置图片位置成功 (tag): " + imageTag + " (x): " + x);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageX 配置图片位置失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageX", e);
            return false;
        }
    }

    /**
     * 获取图片控件的 y 坐标
     *
     * @param imageTag 图片标签
     * @return y 坐标值，失败返回 0
     */
    public float getImageY (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                return imageMap.get(imageTag).getY();
            }
            else
            {
                LogUtils.error(UiManager.class, "getImageY 错误:不存在标签 (tag):");
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getImageY", e);
            return 0;
        }
    }

    /**
     * 设置图片控件的 y 坐标
     *
     * @param imageTag 图片标签
     * @param y       y 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageY (String imageTag, float y)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setY(y);
                LogUtils.debug(UiManager.class, "setImageY 配置图片位置成功 (tag): " + imageTag + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageY 配置图片位置失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageY", e);
            return false;
        }
    }

    /**
     * 设置图片控件的大小
     *
     * @param imageTag 图片标签
     * @param width    宽度
     * @param height   高度
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageSize (String imageTag, float width, float height)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setSize(width, height);
                LogUtils.debug(UiManager.class, "setImageSize 配置图片大小成功 (tag): " + imageTag + " (width): " + width + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageSize 配置图片大小失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageSize", e);
            return false;
        }
    }

    /**
     * 获取图片控件的宽度
     *
     * @param imageTag 图片标签
     * @return 宽度值，失败返回 0
     */
    public float getImageWidth (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                return imageMap.get(imageTag).getWidth();
            }
            else
            {
                LogUtils.error(UiManager.class, "getImageWidth 错误:不存在标签 (tag):");
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getImageWidth", e);
            return 0;
        }
    }

    /**
     * 设置图片控件的宽度
     *
     * @param imageTag 图片标签
     * @param width    宽度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageWidth (String imageTag, float width)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setWidth(width);
                LogUtils.debug(UiManager.class, "setImageWidth 配置图片大小成功 (tag): " + imageTag + " (width): " + width);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageWidth 配置图片大小失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageWidth", e);
            return false;
        }
    }

    /**
     * 获取图片控件的高度
     *
     * @param imageTag 图片标签
     * @return 高度值，失败返回 0
     */
    public float getImageHeight (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                return imageMap.get(imageTag).getHeight();
            }
            else
            {
                LogUtils.error(UiManager.class, "getImageHeight 错误:不存在标签 (tag):" + imageTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getImageHeight", e);
            return 0;
        }
    }

    /**
     * 设置图片控件的高度
     *
     * @param imageTag 图片标签
     * @param height   高度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageHeight (String imageTag, float height)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                imageMap.get(imageTag).setHeight(height);
                LogUtils.debug(UiManager.class, "setImageHeight 配置图片大小成功 (tag): " + imageTag + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageHeight 配置图片大小失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageHeight", e);
            return false;
        }
    }

    /**
     * 添加图片控件。如果图片已存在则更新，不存在则创建
     *
     * @param imageTag      图片标签
     * @param imageKindName 图像样式名称
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         宽度
     * @param height        高度
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addImage (
        String imageTag,
        String imageKindName,
        float x,
        float y,
        float width,
        float height
    )
    {
        try
        {
            if (!imageMap.containsKey(imageTag))
            {
                return createImage(imageTag, imageKindName, x, y, width, height);
            }
            else
            {
                return updateImage(imageTag, imageKindName, x, y, width, height);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addImage", e);
            return false;
        }
    }

    /**
     * 根据 ImageInfo 添加图片控件
     *
     * @param imageInfo 图片信息对象
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addImage (ImageInfo imageInfo)
    {
        try
        {
            return addImage(
                imageInfo.getImageTag(),
                imageInfo.getImageKindName(),
                imageInfo.getX(),
                imageInfo.getY(),
                imageInfo.getWidth(),
                imageInfo.getHeight()
            );
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addImage", e);
            return false;
        }
    }

    /**
     * 批量添加图片控件
     *
     * @param imageInfoList 图片信息列表
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addImage (List<ImageInfo> imageInfoList)
    {
        try
        {
            boolean result = true;
            if (imageInfoList != null)
            {
                for (ImageInfo imageInfo : imageInfoList)
                {
                    if (!addImage(imageInfo)) result = false;
                }
            }
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addImage", e);
            return false;
        }
    }

    /**
     * 根据标签列表从映射表中选取图片信息并批量添加
     *
     * @param imageTagList 图片标签列表
     * @param imageInfoMap 标签到图片信息的映射
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addImage (List<String> imageTagList, Map<String, ImageInfo> imageInfoMap)
    {
        try
        {
            boolean result = true;
            if (imageTagList != null && imageInfoMap != null)
            {
                for (String imageTag : imageTagList)
                {
                    if (imageInfoMap.containsKey(imageTag))
                    {
                        if (!addImage(imageInfoMap.get(imageTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addImage", e);
            return false;
        }
    }

    /**
     * 检查指定标签的图片是否存在
     *
     * @param imageTag 图片标签
     * @return 存在返回 true，否则返回 false
     */
    public boolean containsImage (String imageTag)
    {
        return imageMap.containsKey(imageTag);
    }

    /**
     * 获取指定标签的图片控件
     *
     * @param imageTag 图片标签
     * @return 图片控件对象，不存在返回 null
     */
    public InteractableObject getImage (String imageTag)
    {
        try
        {
            return imageMap.get(imageTag);
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getImage", e);
            return null;
        }
    }

    /**
     * 显示指定标签的图片控件
     *
     * @param imageTag 图片标签
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean showImage (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                CustomImage imageContainer = imageMap.get(imageTag);
                imageContainer.setVisible(true);
                addInteractableObject(imageContainer);
                LogUtils.debug(UiManager.class, "showImage 显示图片 (tag): " + imageTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "showImage 图片不存在 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "showImage", e);
            return false;
        }
    }

    /**
     * 隐藏指定标签的图片控件
     *
     * @param imageTag 图片标签
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hideImage (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                CustomImage imageContainer = imageMap.get(imageTag);
                imageContainer.setVisible(false);
                removeInteractableObject(imageContainer);
                LogUtils.debug(UiManager.class, "hideImage 隐藏图片 (tag): " + imageTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "hideImage 图片不存在 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "hideImage", e);
            return false;
        }
    }

    /**
     * 设置图片控件的点击状态
     *
     * @param imageTag 图片标签
     * @param clicked  是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setImageClicked (String imageTag, boolean clicked)
    {
        try
        {
            if (imageMap.containsKey(imageTag) && imageStateMap.containsKey(imageTag))
            {
                imageStateMap.get(imageTag).setClicked(clicked);
                LogUtils.debug(UiManager.class, "setImageClicked 设置图片点击状态成功 (tag): " + imageTag + " (clicked): " + clicked);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setImageClicked 设置图片点击状态失败 (tag): " + imageTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setImageClicked", e);
            return false;
        }
    }

    /**
     * 检查图片控件是否被点击（消费型，读取后重置状态）
     *
     * @param imageTag 图片标签
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isImageClicked (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag) && imageStateMap.containsKey(imageTag))
            {
                return imageStateMap.get(imageTag).consumeClicked();
            }
            return false;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "isImageClicked", e);
            return false;
        }
    }

    /**
     * 删除指定标签的图片控件
     *
     * @param imageTag 图片标签
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteImage (String imageTag)
    {
        try
        {
            if (imageMap.containsKey(imageTag))
            {
                CustomImage imageContainer = imageMap.get(imageTag);
                removeInteractableObject(imageContainer);
                imageContainer.remove();
                imageMap.remove(imageTag);
                imageKindNameMap.remove(imageTag);
            }
            LogUtils.debug(UiManager.class, "deleteImage 删除图片成功 (tag): " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteImage", e);
            return false;
        }
    }

    /**
     * 删除所有图片控件
     *
     * @return 全部删除成功返回 true，否则返回 false
     */
    public boolean deleteAllImage ()
    {
        try
        {
            ArrayList<String> imageTagList = new ArrayList<>(imageMap.keySet());
            for (String imageTag : imageTagList)
            {
                if (!deleteImage(imageTag)) return false;
            }
            imageMap.clear();
            LogUtils.debug(UiManager.class, "deleteAllImage 成功清除所有图片");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteAllImage", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 获取标签的标准标签（添加 ui.label. 前缀）
     *
     * @param labelTag 标签原始标签
     * @return 标准化的标签字符串
     */
    public String getLabelStandardTag (String labelTag)
    {
        return "ui.label." + labelTag;
    }

    /**
     * 获取所有标签样式映射表
     *
     * @return 标签样式名称到 LabelKind 的映射
     */
    public Map<String, LabelKind> getLabelKindMap ()
    {
        return labelKindMap;
    }

    /**
     * 根据名称获取标签样式
     *
     * @param labelKindName 标签样式名称
     * @return 对应的 LabelKind，不存在返回 null
     */
    public LabelKind getLabelKind (String labelKindName)
    {
        return labelKindMap.get(labelKindName);
    }

    /**
     * 移除指定名称的标签样式，并异步销毁其背景纹理
     *
     * @param labelKindName 标签样式名称
     * @return 移除成功返回 true，失败返回 false
     */
    public boolean removeLabelKind (String labelKindName)
    {
        try
        {
            LabelKind labelKind = labelKindMap.get(labelKindName);
            if (labelKind != null)
            {
                labelKindMap.remove(labelKindName);
                Texture bg = labelKind.getBackground();
                if (bg != null)
                {
                    scheduleDisposeTexture(bg);
                }
                LogUtils.debug(UiManager.class, "removeLabelKind 移除标签样式成功 (name): " + labelKindName);
                return true;
            }
            else
            {
                LogUtils.debug(UiManager.class, "removeLabelKind 移除标签样式失败 (name): " + labelKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "removeLabelKind", e);
            return false;
        }
    }

    /**
     * 根据基样式和字体参数生成最终的标签样式
     *
     * @param labelStyle 基础标签样式
     * @param fontName   字体名称
     * @param fontSize   字体大小缩放系数
     * @param fontColor  字体颜色，为 null 时使用样式的默认颜色
     * @return 最终标签样式，失败返回 null
     */
    private Label.LabelStyle parseFinalLabelStyle (Label.LabelStyle labelStyle, String fontName, float fontSize, Color fontColor)
    {
        try
        {
            Label.LabelStyle finalLabelStyle = new Label.LabelStyle(labelStyle);
            finalLabelStyle.font = getFont(fontName, fontSize);
            if (fontColor != null)
            {
                finalLabelStyle.fontColor = fontColor;
            }
            return finalLabelStyle;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "parseFinalLabelStyle", e);
            return null;
        }
    }

    /**
     * 创建一个标签控件并添加到舞台
     *
     * @param labelTag      标签标识
     * @param labelKindName 标签样式名称
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         宽度
     * @param height        高度
     * @param text          文本对象
     * @param fontName      字体名称
     * @param fontSize      字体大小缩放系数
     * @param fontColor     字体颜色
     * @param fontFlag      文本对齐和打字机模式标志
     * @param fontArgs      字体内边距参数
     * @return 创建成功返回 true，失败返回 false
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
                LogUtils.error(UiManager.class,
                    "createLabel 找不到标签样式: " + labelKindName);
                return false;
            }

            Label.LabelStyle finalStyle = parseFinalLabelStyle(
                labelKind.getLabelStyle(), fontName, fontSize, fontColor);

            float padX = Numeric.Layout.DEFAULT_COMPONENT_PADDING, padY = Numeric.Layout.DEFAULT_COMPONENT_PADDING;
            if (fontArgs != null)
            {
                if (fontArgs.containsKey(JsonKey.Font.Args.PAD_X) && fontArgs.containsKey(JsonKey.Font.Args.PAD_Y))
                {
                    padX = fontArgs.getFloat(JsonKey.Font.Args.PAD_X);
                    padY = fontArgs.getFloat(JsonKey.Font.Args.PAD_Y);
                }
                else if (fontArgs.containsKey(JsonKey.Font.Args.PAD))
                {
                    padX = fontArgs.getFloat(JsonKey.Font.Args.PAD);
                    padY = fontArgs.getFloat(JsonKey.Font.Args.PAD);
                }
            }

            // 计算九宫格裁切位置（sourceBorder）与绘制大小（renderBorder）
            TextureRegion bgRegion = labelKind.getBackgroundRegion();
            int sourceBorder = Math.max(1, (int)(Math.min(width, height) / 16));
            int maxSource = Math.min(bgRegion.getRegionWidth(), bgRegion.getRegionHeight()) / 2 - 1;
            sourceBorder = Math.min(sourceBorder, Math.max(1, maxSource));
            int renderBorder = Math.max(1, (int)(sourceBorder * labelKind.getBorderScale()));

            // 创建标签背景（自适应 NinePatch）
            Image bgImage = buildLabelBackground(labelKind, sourceBorder, renderBorder);
            bgImage.setPosition(0, 0);
            bgImage.setSize(width, height);

            Label label = new Label(text.getDisplayText(), finalStyle);
            label.setPosition(padX, padY);
            label.setSize(width - padX * 2, height - padY * 2);
            label.setWrap(true);

            // 处理文本样式
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

            CustomLabel labelContainer = new CustomLabel(text, bgImage, label, labelTag, this);
            labelContainer.setPosition(x, y);
            labelContainer.setSize(width, height);

            // 处理阅读模式
            if (enableTyping) labelContainer.enableTyping(LABEL_TEXT_TYPING_SPEED);

            labelContainer.setClickListener(() ->
            {
                LogUtils.debug(UiManager.class, "label clicked: " + getLabelStandardTag(labelTag));
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
            addInteractableObject(labelContainer);
            LogUtils.debug(UiManager.class, "createLabel 成功: " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "createLabel 异常", e);
            return false;
        }
    }

    /**
     * 更新已有标签控件的样式、位置、大小、文本和字体属性
     *
     * @param labelTag      标签标识
     * @param labelKindName 新的标签样式名称
     * @param x             新的 x 坐标
     * @param y             新的 y 坐标
     * @param width         新的宽度
     * @param height        新的高度
     * @param text          新的文本对象
     * @param fontName      新的字体名称
     * @param fontSize      新的字体大小缩放系数
     * @param fontColor     新的字体颜色
     * @param fontFlag      新的文本对齐标志
     * @param fontArgs      新的字体内边距参数
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateLabel (
        String labelTag,
        String labelKindName,
        float x,
        float y,
        float width,
        float height,
        TextObject text,
        String fontName,
        float fontSize,
        Color fontColor,
        FontFlag fontFlag,
        JsonEntity fontArgs
    )
    {
        try
        {
            CustomLabel container = (CustomLabel) labelMap.get(labelTag);
            if (container == null) return false;
            Label label = (Label) container.getChild(1);

            LabelKind newKind = getLabelKind(labelKindName);
            if (newKind != null)
            {
                Image bgImage = (Image) container.getChild(0);
                bgImage.setDrawable(buildLabelDrawable(newKind, labelKindName));
                labelKindNameMap.put(labelTag, labelKindName);
            }
            if (x != container.getX() || y != container.getY())
            {
                container.setPosition(x, y);
                LogUtils.debug(UiManager.class, "updateLabel 更新标签位置 (tag): " + labelTag + " (x): " + x + " (y): " + y);
            }
            if (width != container.getWidth() || height != container.getHeight())
            {
                container.setSize(width, height);
                LogUtils.debug(UiManager.class, "updateLabel 更新标签大小 (tag): " + labelTag + " (width): " + width + " (height): " + height);
            }
            if (text != null && !text.getDisplayText().equals(label.getText()))
            {
                label.setText(text.getDisplayText());
                LogUtils.debug(UiManager.class, "updateLabel 更新标签文字 (tag): " + labelTag + " (text): " + text.getDisplayText());
            }
            if (fontSize != label.getFontScaleX())
            {
                label.setFontScale(fontSize, fontSize);
                LogUtils.debug(UiManager.class, "updateLabel 更新标签大小 (tag): " + labelTag + " (fontSize): " + fontSize);
            }
            if (fontColor != null && !label.getColor().equals(fontColor))
            {
                label.setColor(fontColor);
                LogUtils.debug(UiManager.class, "updateLabel 更新标签颜色 (tag): " + labelTag + " (color): " + fontColor);
            }
            if (fontFlag != null)
            {
                switch (fontFlag)
                {
                    case W:
                    case W_TYPING:
                        label.setAlignment(Align.left);
                        break;

                    case E:
                    case E_TYPING:
                        label.setAlignment(Align.right);
                        break;

                    case N:
                    case N_TYPING:
                        label.setAlignment(Align.top);
                        break;

                    case S:
                    case S_TYPING:
                        label.setAlignment(Align.bottom);
                        break;

                    case NW:
                    case NW_TYPING:
                        label.setAlignment(Align.topLeft);
                        break;

                    case NE:
                    case NE_TYPING:
                        label.setAlignment(Align.topRight);
                        break;

                    case SE:
                    case SE_TYPING:
                        label.setAlignment(Align.bottomRight);
                        break;

                    case SW:
                    case SW_TYPING:
                        label.setAlignment(Align.bottomLeft);
                        break;

                    case CENTER:
                    case CENTER_TYPING:
                    default:
                        label.setAlignment(Align.center);
                }
                LogUtils.debug(UiManager.class, "updateLabel 更新标签对齐 (tag): " + labelTag + " (flag): " + fontFlag);
            }
            if (fontArgs != null)
            {
                float padX = Numeric.Layout.DEFAULT_COMPONENT_PADDING, padY = Numeric.Layout.DEFAULT_COMPONENT_PADDING;
                if (fontArgs.containsKey(JsonKey.Font.Args.PAD_X) && fontArgs.containsKey(JsonKey.Font.Args.PAD_Y))
                {
                    padX = fontArgs.getFloat(JsonKey.Font.Args.PAD_X);
                    padY = fontArgs.getFloat(JsonKey.Font.Args.PAD_Y);
                }
                else if (fontArgs.containsKey(JsonKey.Font.Args.PAD))
                {
                    padX = fontArgs.getFloat(JsonKey.Font.Args.PAD);
                    padY = fontArgs.getFloat(JsonKey.Font.Args.PAD);
                }
                label.setPosition(padX, padY);
                label.setSize(width - padX * 2, height - padY * 2);
                LogUtils.debug(UiManager.class, "updateLabel 更新标签内边距 (tag): " + labelTag + " (padX): " + padX + " (padY): " + padY);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "updateLabel", e);
            return false;
        }
    }

    /**
     * 设置标签控件的位置
     *
     * @param labelTag 标签标识
     * @param x       x 坐标
     * @param y       y 坐标
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelPosition (String labelTag, float x, float y)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setPosition(x, y);
                LogUtils.debug(UiManager.class, "setLabelPosition 配置标签位置 (tag): " + labelTag + " (x): " + x + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelPosition 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelPosition", e);
            return false;
        }
    }

    /**
     * 获取标签控件的 x 坐标
     *
     * @param labelTag 标签标识
     * @return x 坐标值，失败返回 0
     */
    /**
     * 获取标签控件的 x 坐标
     *
     * @param labelTag 标签标识
     * @return x 坐标值，失败返回 0
     */
    public float getLabelX (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                return labelMap.get(labelTag).getX();
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelX 错误:不存在标签 (tag): " + labelTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelX", e);
            return 0;
        }
    }

    /**
     * 设置标签控件的 x 坐标
     *
     * @param labelTag 标签标识
     * @param x       x 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelX (String labelTag, float x)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setX(x);
                LogUtils.debug(UiManager.class, "setLabelX 配置标签位置 (tag): " + labelTag + " (x): " + x);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelX 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelX", e);
            return false;
        }
    }

    /**
     * 获取标签控件的 y 坐标
     *
     * @param labelTag 标签标识
     * @return y 坐标值，失败返回 0
     */
    public float getLabelY (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                return labelMap.get(labelTag).getY();
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelY 错误:不存在标签 (tag): " + labelTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelY", e);
            return 0;
        }
    }

    /**
     * 设置标签控件的 y 坐标
     *
     * @param labelTag 标签标识
     * @param y       y 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelY (String labelTag, float y)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setY(y);
                LogUtils.debug(UiManager.class, "setLabelY 配置标签位置 (tag): " + labelTag + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelY 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelY", e);
            return false;
        }
    }

    /**
     * 设置标签控件的大小
     *
     * @param labelTag 标签标识
     * @param width    宽度
     * @param height   高度
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelSize (String labelTag, float width, float height)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setSize(width, height);
                LogUtils.debug(UiManager.class, "setLabelSize 配置标签大小 (tag): " + labelTag + " (width): " + width + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelSize 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelSize", e);
            return false;
        }
    }

    /**
     * 获取标签控件的宽度
     *
     * @param labelTag 标签标识
     * @return 宽度值，失败返回 0
     */
    public float getLabelWidth (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                return (int) labelMap.get(labelTag).getWidth();
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelWidth 错误:不存在标签 (tag): " + labelTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelWidth", e);
            return 0;
        }
    }

    /**
     * 设置标签控件的宽度
     *
     * @param labelTag 标签标识
     * @param width    宽度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelWidth (String labelTag, float width)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setWidth(width);
                LogUtils.debug(UiManager.class, "setLabelWidth 配置标签宽度 (tag): " + labelTag + " (width): " + width);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelWidth 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelWidth", e);
            return false;
        }
    }

    /**
     * 获取标签控件的高度
     *
     * @param labelTag 标签标识
     * @return 高度值，失败返回 0
     */
    public float getLabelHeight (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                return labelMap.get(labelTag).getHeight();
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelHeight 错误:不存在标签 (tag): " + labelTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelHeight", e);
            return 0;
        }
    }

    /**
     * 设置标签控件的高度
     *
     * @param labelTag 标签标识
     * @param height   高度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelHeight (String labelTag, float height)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                labelMap.get(labelTag).setHeight(height);
                LogUtils.debug(UiManager.class, "setLabelHeight 配置标签高度 (tag): " + labelTag + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelHeight 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelHeight", e);
            return false;
        }
    }

    /**
     * 获取标签控件的文本对象
     *
     * @param labelTag 标签标识
     * @return 文本对象，不存在返回 null
     */
    public TextObject getLabelText (String labelTag)
    {
        try
        {
            if (this.labelMap.containsKey(labelTag))
            {
                return labelMap.get(labelTag).getTextObject();
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelText 错误:不存在标签 (tag): " + labelTag);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelText", e);
            return null;
        }
    }

    /**
     * 设置标签控件的文本对象
     *
     * @param labelTag   标签标识
     * @param textObject 文本对象
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelText (String labelTag, TextObject textObject)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                CustomLabel container = (CustomLabel) labelMap.get(labelTag);
                container.setTextObject(textObject);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelText 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelText", e);
            return false;
        }
    }

    /**
     * 设置标签控件的字体大小
     *
     * @param labelTag 标签标识
     * @param fontSize 字体大小缩放系数
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelFontSize (String labelTag, float fontSize)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                CustomLabel container = (CustomLabel) labelMap.get(labelTag);
                Label label = (Label) container.getChild(1);
                label.setFontScale(fontSize);
                LogUtils.debug(UiManager.class, "setLabelFontSize 配置标签字体大小 (tag): " + labelTag + " (fontSize): " + fontSize);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelFontSize 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelFontSize", e);
            return false;
        }
    }

    /**
     * 设置标签控件的字体颜色
     *
     * @param labelTag  标签标识
     * @param fontColor 字体颜色
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelFontColor (String labelTag, Color fontColor)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                CustomLabel container = (CustomLabel) labelMap.get(labelTag);
                Label label = (Label) container.getChild(1);
                label.setColor(fontColor);
                LogUtils.debug(UiManager.class, "setLabelFontColor 配置标签字体颜色 (tag): " + labelTag + " (fontColor): " + fontColor);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelFontColor 错误:不存在标签 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelFontColor", e);
            return false;
        }
    }

    /**
     * 添加标签控件。如果标签已存在则更新，不存在则创建
     *
     * @param labelTag      标签标识
     * @param labelKindName 标签样式名称
     * @param x             x 坐标
     * @param y             y 坐标
     * @param width         宽度
     * @param height        高度
     * @param textObject    文本对象
     * @param fontName      字体名称
     * @param fontSize      字体大小缩放系数
     * @param fontColor     字体颜色
     * @param fontFlag      文本对齐和打字机模式标志
     * @param fontArgs      字体内边距参数
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addLabel (
        String labelTag,
        String labelKindName,
        float x,
        float y,
        float width,
        float height,
        TextObject textObject,
        String fontName,
        float fontSize,
        Color fontColor,
        FontFlag fontFlag,
        JsonEntity fontArgs
    )
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
            LogUtils.error(UiManager.class, "addLabel", e);
            return false;
        }
    }

    /**
     * 根据 LabelInfo 添加标签控件
     *
     * @param labelInfo 标签信息对象
     * @return 添加成功返回 true，失败返回 false
     */
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
            LogUtils.error(UiManager.class, "addLabel", e);
            return false;
        }
    }

    /**
     * 批量添加标签控件
     *
     * @param labelInfoList 标签信息列表
     * @return 全部添加成功返回 true，否则返回 false
     */
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
            LogUtils.error(UiManager.class, "addLabel", e);
            return false;
        }
    }

    /**
     * 根据标签列表从映射表中选取标签信息并批量添加
     *
     * @param labelTagList  标签标识列表
     * @param labelInfoMap  标签标识到标签信息的映射
     * @return 全部添加成功返回 true，否则返回 false
     */
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
            LogUtils.error(UiManager.class, "addLabel", e);
            return false;
        }
    }

    /**
     * 检查指定标识的标签是否存在
     *
     * @param labelTag 标签标识
     * @return 存在返回 true，否则返回 false
     */
    public boolean containsLabel (String labelTag)
    {
        return labelMap.containsKey(labelTag);
    }

    /**
     * 获取指定标识的标签控件
     *
     * @param labelTag 标签标识
     * @return 标签控件对象，不存在返回 null
     */
    public InteractableObject getLabel (String labelTag)
    {
        try
        {
            return labelMap.get(labelTag);
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabel", e);
            return null;
        }
    }

    /**
     * 显示指定标识的标签控件
     *
     * @param labelTag 标签标识
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean showLabel (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                CustomLabel labelContainer = labelMap.get(labelTag);
                labelContainer.setVisible(true);
                addInteractableObject(labelContainer);
                LogUtils.debug(UiManager.class, "showLabel 显示标签 (tag): " + labelTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "showLabel 标签不存在 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "showLabel", e);
            return false;
        }
    }

    /**
     * 隐藏指定标识的标签控件
     *
     * @param labelTag 标签标识
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hideLabel (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag))
            {
                CustomLabel labelContainer = labelMap.get(labelTag);
                removeInteractableObject(labelContainer);
                labelContainer.setVisible(false);
                LogUtils.debug(UiManager.class, "hideLabel 隐藏标签 (tag): " + labelTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "hideLabel 标签不存在 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "hideLabel", e);
            return false;
        }
    }

    /**
     * 获取标签的基础文本
     *
     * @param labelTag 标签标识
     * @return 基础文本字符串，不存在返回 null
     */
    public String getLabelBaseText (String labelTag)
    {
        try
        {
            if (labelBaseTextMap.containsKey(labelTag))
            {
                return labelBaseTextMap.get(labelTag);
            }
            else
            {
                LogUtils.error(UiManager.class, "getLabelBaseText 标签不存在 (tag): " + labelTag);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelBaseText", e);
            return null;
        }
    }

    /**
     * 设置标签控件的点击状态
     *
     * @param labelTag 标签标识
     * @param clicked  是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setLabelClicked (String labelTag, boolean clicked)
    {
        try
        {
            if (labelMap.containsKey(labelTag) && labelStateMap.containsKey(labelTag))
            {
                labelStateMap.get(labelTag).setClicked(clicked);
                LogUtils.debug(UiManager.class, "setLabelClicked 设置标签点击状态成功 (tag): " + labelTag + " (clicked): " + clicked);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setLabelClicked 设置标签点击状态失败 (tag): " + labelTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelClicked", e);
            return false;
        }
    }

    /**
     * 检查标签控件是否被点击（消费型，读取后重置状态）
     *
     * @param labelTag 标签标识
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isLabelClicked (String labelTag)
    {
        try
        {
            if (labelMap.containsKey(labelTag) && labelStateMap.containsKey(labelTag))
            {
                return labelStateMap.get(labelTag).consumeClicked();
            }
            return false;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "isLabelClicked", e);
            return false;
        }
    }

    // TODO: 实现真正的读写进度逻辑

    /**
     * 设置标签的阅读进度
     *
     * @param labelTag 标签标识
     * @param progress 进度值（0.0 ~ 1.0）
     * @return 设置成功返回 true
     */
    public boolean setLabelReadProgress (String labelTag, float progress)
    {
        try
        {
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setLabelReadProgress", e);
            return false;
        }
    }

    // TODO: 实现真正的读写进度逻辑

    /**
     * 获取标签的阅读进度
     *
     * @param labelTag 标签标识
     * @return 进度值（0.0 ~ 1.0），失败返回 -1.0
     */
    public float getLabelReadProgress (String labelTag)
    {
        try
        {
            return 1.0f;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getLabelReadProgress", e);
            return -1.0f;
        }
    }

    /**
     * 删除指定标识的标签控件
     *
     * @param labelTag 标签标识
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteLabel (String labelTag)
    {
        try
        {
            if (!labelMap.containsKey(labelTag))
            {
                LogUtils.error(UiManager.class, "clearLabel 标签不存在 (tag): " + labelTag);
                return false;
            }
            CustomLabel labelContainer = labelMap.get(labelTag);
            removeInteractableObject(labelContainer);
            labelContainer.remove();
            labelStateMap.remove(labelTag);
            labelBaseTextMap.remove(labelTag);
            labelKindNameMap.remove(labelTag);
            labelMap.remove(labelTag);
            LogUtils.debug(UiManager.class, "deleteLabel 成功清除标签 (tag): " + labelTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteLabel", e);
            return false;
        }
    }

    /**
     * 删除所有标签控件
     *
     * @return 全部删除成功返回 true，否则返回 false
     */
    public boolean deleteAllLabel ()
    {
        try
        {
            ArrayList<String> labelTagList = new ArrayList<>(labelMap.keySet());
            for (String labelTag : labelTagList)
            {
                if (!deleteLabel(labelTag)) return false;
            }
            labelMap.clear();
            LogUtils.debug(UiManager.class, "deleteAllLabel 成功清除所有标签");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteAllLabel", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 获取按钮的标准标签（添加 ui.button. 前缀）
     *
     * @param buttonTag 按钮原始标签
     * @return 标准化的标签字符串
     */
    public String getButtonStandardTag (String buttonTag)
    {
        return "ui.button." + buttonTag;
    }


    /**
     * 获取所有按钮样式映射表
     *
     * @return 按钮样式名称到 ButtonKind 的映射
     */
    public Map<String, ButtonKind> getButtonKindMap ()
    {
        return this.buttonKindMap;
    }

    /**
     * 移除指定名称的按钮样式
     *
     * @param buttonKindName 按钮样式名称
     * @return 移除成功返回 true，失败返回 false
     */
    public boolean removeButtonKind (String buttonKindName)
    {
        try
        {
            ButtonKind buttonKind = buttonKindMap.get(buttonKindName);
            if (buttonKind != null)
            {
                buttonKindMap.remove(buttonKindName);
                // 注意：ButtonKind 内部不直接持有纹理，纹理由 TextureRegionDrawable 持有，而 TextureRegionDrawable 持有 TextureRegion，最终持有 Texture。
                // 由于纹理是通过 graphicsManager.getTexture 获取的，理论上由 GraphicsManager 管理生命周期，此处不主动销毁。
                // 但如果需要强制释放，可以遍历 drawable 中的纹理，但会增加复杂度。保持现状，相信 GraphicsManager 的缓存机制。
                LogUtils.debug(UiManager.class, "removeButtonKind 移除按钮样式成功 (name): " + buttonKindName);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "removeButtonKind 移除按钮样式失败 (name): " + buttonKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "removeButtonKind", e);
            return false;
        }
    }

    /**
     * 根据名称获取按钮样式
     *
     * @param buttonKindName 按钮样式名称
     * @return 对应的 ButtonKind，不存在返回 null
     */
    public ButtonKind getButtonKind (String buttonKindName)
    {
        return this.buttonKindMap.get(buttonKindName);
    }

    /**
     * 创建一个按钮控件并添加到舞台
     *
     * @param buttonTag      按钮标签
     * @param buttonKindName 按钮样式名称
     * @param x              x 坐标
     * @param y              y 坐标
     * @param width          宽度
     * @param height         高度
     * @param text           文本对象
     * @param fontName       字体名称
     * @param fontSize       字体大小缩放系数
     * @param fontColor      字体颜色
     * @return 创建成功返回 true，失败返回 false
     */
    private boolean createButton (
        String buttonTag,
        String buttonKindName,
        float x,
        float y,
        float width,
        float height,
        TextObject text,
        String fontName,
        float fontSize,
        Color fontColor
    )
    {
        try
        {
            ButtonKind buttonKind = getButtonKind(buttonKindName);
            if (buttonKind == null)
            {
                LogUtils.error(UiManager.class, "createButton 找不到按钮样式 (kind): " + buttonKindName);
                return false;
            }

            TextButton.TextButtonStyle buttonStyle = buttonKind.getButtonStyle();
            TextButton.TextButtonStyle finalButtonStyle = new TextButton.TextButtonStyle(buttonStyle);
            finalButtonStyle.font = getFont(fontName, fontSize);
            if (fontColor != null)
            {
                finalButtonStyle.fontColor = fontColor;
                finalButtonStyle.downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
                finalButtonStyle.disabledFontColor = fontColor.cpy().mul(Numeric.Alpha.DISABLED_DARKEN);
            }

            CustomTextButton buttonContainer = new CustomTextButton(text, finalButtonStyle, buttonTag, this);
            buttonContainer.setPosition(x, y);
            buttonContainer.setSize(width, height);

            if (!audioManager.loadSound(getButtonStandardTag(buttonTag), buttonKind.getAudioClick()))
            {
                return false;
            }

            buttonContainer.setClickRunnable(() ->
            {
                LogUtils.debug(UiManager.class, "button clicked " + getButtonStandardTag(buttonTag));
                try
                {
                    // 标记按钮状态（供 isButtonClicked 轮询用）
                    if (!buttonStateMap.containsKey(buttonTag))
                    {
                        buttonStateMap.put(buttonTag, new ButtonState());
                    }
                    buttonStateMap.get(buttonTag).setClicked();
                    audioManager.playSound(getButtonStandardTag(buttonTag), false);

                    // 触发注册的点击回调（替代轮询方案）
                    Runnable callback = buttonClickCallbackMap.get(buttonTag);
                    if (callback != null)
                    {
                        callback.run();
                    }
                }
                catch (Exception e)
                {
                    LogUtils.error(UiManager.class, "button clicked", e);
                }
            });

            stage.addActor(buttonContainer);
            buttonMap.put(buttonTag, buttonContainer);
            buttonKindNameMap.put(buttonTag, buttonKindName);
            buttonStateMap.put(buttonTag, new ButtonState());
            addInteractableObject(buttonContainer);
            LogUtils.debug(UiManager.class, "createButton 放置按钮成功 (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "createButton", e);
            return false;
        }
    }

    /**
     * 更新已有按钮控件的样式、位置、大小、文本和字体属性
     *
     * @param buttonTag      按钮标签
     * @param buttonKindName 新的按钮样式名称
     * @param x              新的 x 坐标
     * @param y              新的 y 坐标
     * @param width          新的宽度
     * @param height         新的高度
     * @param text           新的文本对象
     * @param fontName       新的字体名称
     * @param fontSize       新的字体大小缩放系数
     * @param fontColor      新的字体颜色
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateButton (
        String buttonTag,
        String buttonKindName,
        float x,
        float y,
        float width,
        float height,
        TextObject text,
        String fontName,
        float fontSize,
        Color fontColor
    )
    {
        try
        {
            CustomTextButton button = buttonMap.get(buttonTag);
            if (button == null) return false;

            ButtonKind newKind = getButtonKind(buttonKindName);
            if (newKind != null)
            {
                TextButton.TextButtonStyle newStyle = newKind.getButtonStyle();
                TextButton.TextButtonStyle btnStyle = button.getButtonStyle();
                btnStyle.up = newStyle.up;
                btnStyle.down = newStyle.down;
                btnStyle.disabled = newStyle.disabled;
                btnStyle.over = newStyle.over;
                buttonKindNameMap.put(buttonTag, buttonKindName);
            }
            if (text != null)
            {
                button.setTextObject(text);
                LogUtils.debug(UiManager.class, "updateButton 更新按钮文字 (tag): " + buttonTag + " (text): " + text.getDisplayText());
            }
            if (fontName != null)
            {
                TextButton.TextButtonStyle btnStyle = button.getButtonStyle();
                btnStyle.font = getFont(fontName, fontSize);
                LogUtils.debug(UiManager.class, "updateButton 更新按钮字体 (tag): " + buttonTag + " (font): " + fontName);
            }
            if (fontColor != null)
            {
                TextButton.TextButtonStyle btnStyle = button.getButtonStyle();
                btnStyle.fontColor = fontColor;
                btnStyle.downFontColor = new Color(1f - fontColor.r, 1f - fontColor.g, 1f - fontColor.b, fontColor.a);
                btnStyle.disabledFontColor = fontColor.cpy().mul(Numeric.Alpha.DISABLED_DARKEN);
                LogUtils.debug(UiManager.class, "updateButton 更新按钮颜色 (tag): " + buttonTag + " (color): " + fontColor);
            }
            if (button.getX() != x || button.getY() != y)
            {
                button.setPosition(x, y);
                LogUtils.debug(UiManager.class, "updateButton 更新按钮位置 (tag): " + buttonTag + " (x): " + x + " (y): " + y);
            }
            if (button.getWidth() != width || button.getHeight() != height)
            {
                button.setSize(width, height);
                LogUtils.debug(UiManager.class, "updateButton 更新按钮大小 (tag): " + buttonTag + " (width): " + width + " (height): " + height);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "updateButton", e);
            return false;
        }
    }

    /**
     * 设置按钮控件的位置
     *
     * @param buttonTag 按钮标签
     * @param x        x 坐标
     * @param y        y 坐标
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonPosition (String buttonTag, float x, float y)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton button = buttonMap.get(buttonTag);
                button.setPosition(x, y);
                LogUtils.debug(UiManager.class, "setButtonPosition 设置按钮位置成功 (tag): " + buttonTag + " (x): " + x + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonPosition 找不到按钮 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonPosition", e);
            return false;
        }
    }

    /**
     * 获取按钮控件的 x 坐标
     *
     * @param buttonTag 按钮标签
     * @return x 坐标值，失败返回 0
     */
    public float getButtonX (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                return buttonMap.get(buttonTag).getX();
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonX 错误:不存在标签 (tag): " + buttonTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonX", e);
            return 0;
        }
    }

    /**
     * 设置按钮控件的 x 坐标
     *
     * @param buttonTag 按钮标签
     * @param x       x 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonX (String buttonTag, float x)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                buttonMap.get(buttonTag).setX(x);
                LogUtils.debug(UiManager.class, "setButtonX 配置按钮X坐标成功 (tag): " + buttonTag + " (x): " + x);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonX 错误:不存在标签 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonX", e);
            return false;
        }
    }

    /**
     * 获取按钮控件的 y 坐标
     *
     * @param buttonTag 按钮标签
     * @return y 坐标值，失败返回 0
     */
    public float getButtonY (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                return buttonMap.get(buttonTag).getY();
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonY 错误:不存在标签 (tag): " + buttonTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonY", e);
            return 0;
        }
    }

    /**
     * 设置按钮控件的 y 坐标
     *
     * @param buttonTag 按钮标签
     * @param y       y 坐标值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonY (String buttonTag, float y)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                buttonMap.get(buttonTag).setY(y);
                LogUtils.debug(UiManager.class, "setButtonY 配置按钮Y坐标成功 (tag): " + buttonTag + " (y): " + y);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonY 错误:不存在标签 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonY", e);
            return false;
        }
    }

    /**
     * 设置按钮控件的大小
     *
     * @param buttonTag 按钮标签
     * @param width     宽度
     * @param height    高度
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonSize (String buttonTag, float width, float height)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton button = buttonMap.get(buttonTag);
                button.setSize(width, height);
                LogUtils.debug(UiManager.class, "setButtonSize 改变按钮大小成功 (tag): " + buttonTag + " (width): " + width + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonSize 找不到按钮 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonSize", e);
            return false;
        }
    }

    /**
     * 获取按钮控件的宽度
     *
     * @param buttonTag 按钮标签
     * @return 宽度值，失败返回 0
     */
    public float getButtonWidth (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                return buttonMap.get(buttonTag).getWidth();
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonWidth 错误:不存在标签 (tag): " + buttonTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonWidth", e);
            return 0;
        }
    }

    /**
     * 设置按钮控件的宽度
     *
     * @param buttonTag 按钮标签
     * @param width     宽度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonWidth (String buttonTag, float width)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                buttonMap.get(buttonTag).setWidth(width);
                LogUtils.debug(UiManager.class, "setButtonWidth 配置按钮宽度成功 (tag): " + buttonTag + " (width): " + width);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonWidth 错误:不存在标签 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonWidth", e);
            return false;
        }
    }

    /**
     * 获取按钮控件的高度
     *
     * @param buttonTag 按钮标签
     * @return 高度值，失败返回 0
     */
    public float getButtonHeight (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                return buttonMap.get(buttonTag).getHeight();
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonHeight 错误:不存在标签 (tag): " + buttonTag);
                return 0;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonHeight", e);
            return 0;
        }
    }

    /**
     * 设置按钮控件的高度
     *
     * @param buttonTag 按钮标签
     * @param height    高度值
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonHeight (String buttonTag, float height)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                buttonMap.get(buttonTag).setHeight(height);
                LogUtils.debug(UiManager.class, "setButtonHeight 配置按钮高度成功 (tag): " + buttonTag + " (height): " + height);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonHeight 错误:不存在标签 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonHeight", e);
            return false;
        }
    }

    /**
     * 设置按钮控件的文本对象
     *
     * @param buttonTag  按钮标签
     * @param textObject 文本对象
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonText (String buttonTag, TextObject textObject)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                buttonMap.get(buttonTag).setTextObject(textObject);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonText 找不到按钮 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonText", e);
            return false;
        }
    }

    /**
     * 添加按钮控件。如果按钮已存在则更新，不存在则创建
     *
     * @param buttonTag      按钮标签
     * @param buttonKindName 按钮样式名称
     * @param x              x 坐标
     * @param y              y 坐标
     * @param width          宽度
     * @param height         高度
     * @param text           文本对象
     * @param fontName       字体名称
     * @param fontSize       字体大小缩放系数
     * @param fontColor      字体颜色
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addButton (
        String buttonTag,
        String buttonKindName,
        float x,
        float y,
        float width,
        float height,
        TextObject text,
        String fontName,
        float fontSize,
        Color fontColor
    )
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
            LogUtils.error(UiManager.class, "addButton", e);
            return false;
        }
    }

    /**
     * 根据 ButtonInfo 添加按钮控件
     *
     * @param buttonInfo 按钮信息对象
     * @return 添加成功返回 true，失败返回 false
     */
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
            LogUtils.error(UiManager.class, "addButton", e);
            return false;
        }
    }

    /**
     * 批量添加按钮控件
     *
     * @param buttonInfoList 按钮信息列表
     * @return 全部添加成功返回 true，否则返回 false
     */
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
            LogUtils.error(UiManager.class, "addButton", e);
            return false;
        }
    }

    /**
     * 根据标签列表从映射表中选取按钮信息并批量添加
     *
     * @param buttonTagList  按钮标签列表
     * @param buttonInfoMap  按钮标签到按钮信息的映射
     * @return 全部添加成功返回 true，否则返回 false
     */
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
            LogUtils.error(UiManager.class, "addButton", e);
            return false;
        }
    }

    /**
     * 检查指定标签的按钮是否存在
     *
     * @param buttonTag 按钮标签
     * @return 存在返回 true，否则返回 false
     */
    public boolean containsButton (String buttonTag)
    {
        return buttonMap.containsKey(buttonTag);
    }

    /**
     * 获取指定标签的按钮控件
     *
     * @param buttonTag 按钮标签
     * @return 按钮控件对象，不存在返回 null
     */
    public InteractableObject getButton (String buttonTag)
    {
        try
        {
            return buttonMap.get(buttonTag);
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButton", e);
            return null;
        }
    }

    /**
     * 启用指定标签的按钮
     *
     * @param buttonTag 按钮标签
     * @return 启用成功返回 true，失败返回 false
     */
    public boolean enableButton (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton buttonContainer = buttonMap.get(buttonTag);
                buttonContainer.setDisabled(false);
                addInteractableObject(buttonContainer);
                LogUtils.debug(UiManager.class, "enableButton 启用按钮 (tag): " + buttonTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "enableButton 按钮不存在 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "enableButton", e);
            return false;
        }
    }

    /**
     * 禁用指定标签的按钮
     *
     * @param buttonTag 按钮标签
     * @return 禁用成功返回 true，失败返回 false
     */
    public boolean disableButton (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton buttonContainer = buttonMap.get(buttonTag);
                removeInteractableObject(buttonContainer);
                buttonContainer.setDisabled(true);
                LogUtils.debug(UiManager.class, "disableButton 禁用按钮 (tag): " + buttonTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "disableButton 按钮不存在 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "disableButton", e);
            return false;
        }
    }

    /**
     * 显示指定标签的按钮
     *
     * @param buttonTag 按钮标签
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean showButton (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton buttonContainer = buttonMap.get(buttonTag);
                buttonContainer.setVisible(true);
                addInteractableObject(buttonContainer);
                LogUtils.debug(UiManager.class, "showButton 显示按钮 (tag): " + buttonTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "showButton 按钮不存在 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "showButton", e);
            return false;
        }
    }

    /**
     * 隐藏指定标签的按钮
     *
     * @param buttonTag 按钮标签
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hideButton (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                CustomTextButton buttonContainer = buttonMap.get(buttonTag);
                removeInteractableObject(buttonContainer);
                buttonContainer.setVisible(false);
                LogUtils.debug(UiManager.class, "hideButton 隐藏按钮 (tag): " + buttonTag);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "hideButton 按钮不存在 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "hideButton", e);
            return false;
        }
    }

    /**
     * 获取按钮的基础文本
     *
     * @param buttonTag 按钮标签
     * @return 基础文本字符串，不存在返回 null
     */
    public String getButtonBaseText (String buttonTag)
    {
        try
        {
            if (buttonBaseTextMap.containsKey(buttonTag))
            {
                return buttonBaseTextMap.get(buttonTag);
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonBaseText 按钮不存在 (tag): " + buttonTag);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonBaseText", e);
            return null;
        }
    }

    /**
     * 获取按钮控件的文本对象
     *
     * @param buttonTag 按钮标签
     * @return 文本对象，不存在返回 null
     */
    public TextObject getButtonText (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag))
            {
                return buttonMap.get(buttonTag).getTextObject();
            }
            else
            {
                LogUtils.error(UiManager.class, "getButtonText 按钮不存在 (tag): " + buttonTag);
                return null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getButtonText", e);
            return null;
        }
    }

    /**
     * 设置按钮控件的点击状态（设为 true 时会播放点击音效）
     *
     * @param buttonTag 按钮标签
     * @param clicked   是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setButtonClicked (String buttonTag, boolean clicked)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag) && buttonStateMap.containsKey(buttonTag))
            {
                if (clicked) audioManager.playSound(getButtonStandardTag(buttonTag), false);
                buttonStateMap.get(buttonTag).setClicked(clicked);
                LogUtils.debug(UiManager.class, "setButtonClicked 设置按钮点击状态成功 (tag): " + buttonTag + " (clicked): " + clicked);
                return true;
            }
            else
            {
                LogUtils.error(UiManager.class, "setButtonClicked 设置按钮点击状态失败 (tag): " + buttonTag);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "setButtonClicked", e);
            return false;
        }
    }

    /**
     * 注册按钮点击回调（替代轮询 isButtonClicked），触发时同时保留按钮状态标记
     *
     * @param buttonTag 按钮标签
     * @param callback  点击时执行的回调
     */
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

    /**
     * 检查按钮控件是否被点击（消费型，读取后重置状态）
     *
     * @param buttonTag 按钮标签
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isButtonClicked (String buttonTag)
    {
        try
        {
            if (buttonMap.containsKey(buttonTag) && buttonStateMap.containsKey(buttonTag))
            {
                return buttonStateMap.get(buttonTag).consumeClicked();
            }
            return false;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "isButtonClicked", e);
            return false;
        }
    }

    /**
     * 删除指定标签的按钮控件
     *
     * @param buttonTag 按钮标签
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteButton (String buttonTag)
    {
        try
        {
            if (!buttonMap.containsKey(buttonTag))
            {
                LogUtils.debug(UiManager.class, "deleteButton 按钮不存在 (tag): " + buttonTag);
                return false;
            }
            CustomTextButton buttonContainer = buttonMap.get(buttonTag);
            removeInteractableObject(buttonContainer);
            buttonContainer.remove();
            buttonStateMap.remove(buttonTag);
            buttonBaseTextMap.remove(buttonTag);
            buttonKindNameMap.remove(buttonTag);
            buttonClickCallbackMap.remove(buttonTag);
            buttonMap.remove(buttonTag);
            LogUtils.debug(UiManager.class, "deleteButton 成功清除按钮 (tag): " + buttonTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteButton", e);
            return false;
        }
    }

    /**
     * 删除所有按钮控件
     *
     * @return 全部删除成功返回 true，否则返回 false
     */
    public boolean deleteAllButton ()
    {
        try
        {
            ArrayList<String> buttonTagList = new ArrayList<>(buttonMap.keySet());
            for (String buttonTag : buttonTagList)
            {
                if (!deleteButton(buttonTag)) return false;
            }
            buttonMap.clear();
            LogUtils.debug(UiManager.class, "deleteAllButton 成功清除所有按钮");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteAllButton", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 添加单个 UI 对象。根据对象类型自动分发到 addImage/addLabel/addButton
     *
     * @param object UI 对象（ImageInfo/LabelInfo/ButtonInfo）
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addObject (Object object)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (object): " + object);
            if (object instanceof ImageInfo)
            {
                return addImage((ImageInfo) object);
            }
            else if (object instanceof LabelInfo)
            {
                return addLabel((LabelInfo) object);
            }
            else if (object instanceof ButtonInfo)
            {
                return addButton((ButtonInfo) object);
            }
            else
            {
                LogUtils.debug(UiManager.class, "addObject 不支持的对象类型: " + object.getClass().getName());
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 批量添加 UI 对象
     *
     * @param objectList UI 对象列表
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (List<Object> objectList)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (objectList): " + objectList);
            boolean result = true;
            if (objectList != null)
            {
                for (Object object : objectList)
                {
                    if (!addObject(object)) result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 根据标签列表从映射表中选取 UI 对象并批量添加
     *
     * @param objectTagList UI 对象标签列表
     * @param objectMap     标签到 UI 对象的映射
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (List<String> objectTagList, Map<String, Object> objectMap)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (objectTagList): " + objectTagList + " (objectMap): " + objectMap);
            boolean result = true;
            if (objectTagList != null && objectMap != null)
            {
                for (String objectTag : objectTagList)
                {
                    if (objectMap.containsKey(objectTag))
                    {
                        if (!addObject(objectMap.get(objectTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 根据 UiInfo 添加其中的所有 UI 对象
     *
     * @param uiInfo UI 信息容器
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (UiInfo uiInfo)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (uiInfo): " + uiInfo);
            boolean result = true;
            if (uiInfo != null)
            {
                for (int i = 0; i < uiInfo.size(); i++)
                {
                    if (!addObject(uiInfo.get(i))) result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 根据标签列表从 UiInfo 中选取对象并批量添加
     *
     * @param objectTagList 标签列表
     * @param uiInfo        UI 信息容器
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (List<String> objectTagList, UiInfo uiInfo)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (objectTagList): " + objectTagList + " (uiInfo): " + uiInfo);
            boolean result = true;
            if (objectTagList != null && uiInfo != null)
            {
                for (String ObjectTag : uiInfo.keySet())
                {
                    if (uiInfo.containsKey(ObjectTag))
                    {
                        if (!addObject(uiInfo.get(ObjectTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 根据类型约束从 UiInfo 中选取对象并批量添加（同时校验对象类型）
     *
     * @param objectMap 标签到期望类型的映射
     * @param uiInfo    UI 信息容器
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (Map<String, Class> objectMap, UiInfo uiInfo)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (objectMap): " + objectMap + " (uiInfo): " + uiInfo);
            boolean result = true;
            if (objectMap != null && uiInfo != null)
            {
                for (String objectTag : uiInfo.keySet())
                {
                    if (objectMap.containsKey(objectTag) && objectMap.get(objectTag).equals(uiInfo.getClass(objectTag)))
                    {
                        if (!addObject(objectMap.get(objectTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 根据指定类型从 UiInfo 中筛选并批量添加对象
     *
     * @param objectClass 对象类型
     * @param uiInfo      UI 信息容器
     * @return 全部添加成功返回 true，否则返回 false
     */
    public boolean addObject (Class objectClass, UiInfo uiInfo)
    {
        try
        {
            LogUtils.debug(UiManager.class, "addObject (objectClass): " + objectClass + " (uiInfo): " + uiInfo);
            boolean result = true;
            if (objectClass != null && uiInfo != null)
            {
                for (String objectTag : uiInfo.keySet())
                {
                    if (uiInfo.getClass(objectTag).equals(objectClass))
                    {
                        if (!addObject(uiInfo.get(objectTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addObject", e);
            return false;
        }
    }

    /**
     * 删除指定标签的 UI 对象（自动识别类型）
     *
     * @param objectTag 对象标签
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteObject (String objectTag)
    {
        try
        {
            LogUtils.debug(UiManager.class, "deleteObject (objectTag): " + objectTag);
            if (objectTag != null)
            {
                if (imageMap.containsKey(objectTag))
                {
                    return deleteImage(objectTag);
                }
                else if (labelMap.containsKey(objectTag))
                {
                    return deleteLabel(objectTag);
                }
                else if (buttonMap.containsKey(objectTag))
                {
                    return deleteButton(objectTag);
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteObject", e);
            return false;
        }
    }

    /**
     * 删除所有 UI 对象（图片、标签、按钮）
     *
     * @return 全部删除成功返回 true，否则返回 false
     */
    public boolean deleteAllObject ()
    {
        try
        {
            LogUtils.debug(UiManager.class, "deleteAllObject");
            boolean result = deleteAllImage() && deleteAllLabel() && deleteAllButton();
            // 清理 Layout Group 映射（元素已由 deleteAll* 清理）
            for (Group group : layoutGroupMap.values())
            {
                group.remove();
            }
            layoutGroupMap.clear();
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteAllObject", e);
            return false;
        }
    }

    /**
     * 添加布局配置中的所有 UI 控件
     *
     * @param layout 布局配置对象
     * @return 添加成功返回 true，失败返回 false
     */
    public boolean addLayout (Layout layout)
    {
        try
        {
            if (layout != null)
            {
                for (ImageInfo imageInfo : layout.getImageMap().values())
                {
                    if (imageInfo.getShow())
                    {
                        if (!addImage(imageInfo))
                        {
                            LogUtils.error(UiManager.class, "addLayout 显示Layout图片出错");
                            return false;
                        }
                    }
                }
                for (ImageInfo imageInfo : layout.getImageMap().values())
                {
                    if (!imageInfo.getShow())
                    {
                        if (!addImage(imageInfo))
                        {
                            LogUtils.error(UiManager.class, "addLayout 显示Layout图片出错");
                            return false;
                        }
                        hideImage(imageInfo.getImageTag());
                    }
                }
                for (LabelInfo labelInfo : layout.getLabelMap().values())
                {
                    if (!addLabel(labelInfo))
                    {
                        LogUtils.error(UiManager.class, "addLayout 显示Layout标签出错");
                        return false;
                    }
                    if (!labelInfo.getShow())
                    {
                        hideLabel(labelInfo.getLabelTag());
                    }
                }
                for (ButtonInfo buttonInfo : layout.getButtonMap().values())
                {
                    if (!addButton(buttonInfo))
                    {
                        LogUtils.error(UiManager.class, "addLayout 显示Layout按钮出错");
                        return false;
                    }
                    if (!buttonInfo.getShow())
                    {
                        hideButton(buttonInfo.getButtonTag());
                    }
                }

                // 将所有元素归入一个 scene2d Group，支持整组 show/hide
                Group group = new Group();
                group.setName("layout_" + (layout.getName() != null ? layout.getName() : System.identityHashCode(layout)));
                moveLayoutActorsToGroup(layout, group);
                stage.addActor(group);
                layoutGroupMap.put(layout, group);
            }
            LogUtils.debug(UiManager.class, "addLayout 添加页面结构 (layout): " + layout);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "addLayout", e);
            return false;
        }
    }

    /**
     * 将 Layout 中所有已创建的 Actor 从 stage 根节点移至指定 Group 下，
     * 保持与 addLayout 创建顺序完全一致（可见图片 → 隐藏图片 → 标签 → 按钮）
     */
    private void moveLayoutActorsToGroup (Layout layout, Group group)
    {
        // 第一轮：可见图片（匹配 addLayout 第一个循环）
        for (ImageInfo imageInfo : layout.getImageMap().values())
        {
            if (!imageInfo.getShow()) continue;
            Actor actor = imageMap.get(imageInfo.getImageTag());
            if (actor != null && actor.getParent() != group)
            {
                actor.remove();
                group.addActor(actor);
            }
        }
        // 第二轮：隐藏图片（匹配 addLayout 第二个循环）
        for (ImageInfo imageInfo : layout.getImageMap().values())
        {
            if (imageInfo.getShow()) continue;
            Actor actor = imageMap.get(imageInfo.getImageTag());
            if (actor != null && actor.getParent() != group)
            {
                actor.remove();
                group.addActor(actor);
            }
        }
        // 标签
        for (LabelInfo labelInfo : layout.getLabelMap().values())
        {
            Actor actor = labelMap.get(labelInfo.getLabelTag());
            if (actor != null && actor.getParent() != group)
            {
                actor.remove();
                group.addActor(actor);
            }
        }
        // 按钮
        for (ButtonInfo buttonInfo : layout.getButtonMap().values())
        {
            Actor actor = buttonMap.get(buttonInfo.getButtonTag());
            if (actor != null && actor.getParent() != group)
            {
                actor.remove();
                group.addActor(actor);
            }
        }
    }

    /**
     * 显示布局配置中的所有 UI 控件
     *
     * @param layout 布局配置对象
     * @return 全部显示成功返回 true，否则返回 false
     */
    public boolean showLayout (Layout layout)
    {
        try
        {
            Group group = layoutGroupMap.get(layout);
            if (group != null)
            {
                group.setVisible(true);
                LogUtils.debug(UiManager.class, "showLayout 显示页面结构 (layout): " + layout);
                return true;
            }
            // 没有 Group 映射时回退到逐元素操作（兼容通过 addImage/addButton 直接添加的布局）
            boolean result = true;
            for (String imageTag : layout.getImageMap().keySet())
                if (!showImage(imageTag)) result = false;
            for (String labelTag : layout.getLabelMap().keySet())
                if (!showLabel(labelTag)) result = false;
            for (String buttonTag : layout.getButtonMap().keySet())
                if (!showButton(buttonTag)) result = false;
            LogUtils.debug(UiManager.class, "showLayout 显示页面结构 (layout): " + layout);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "showLayout", e);
            return false;
        }
    }

    /**
     * 隐藏布局配置中的所有 UI 控件
     *
     * @param layout 布局配置对象
     * @return 全部隐藏成功返回 true，否则返回 false
     */
    public boolean hideLayout (Layout layout)
    {
        try
        {
            Group group = layoutGroupMap.get(layout);
            if (group != null)
            {
                group.setVisible(false);
                LogUtils.debug(UiManager.class, "hideLayout 隐藏页面结构 (layout): " + layout);
                return true;
            }
            // 没有 Group 映射时回退到逐元素操作
            boolean result = true;
            for (String imageTag : layout.getImageMap().keySet())
                if (!hideImage(imageTag)) result = false;
            for (String labelTag : layout.getLabelMap().keySet())
                if (!hideLabel(labelTag)) result = false;
            for (String buttonTag : layout.getButtonMap().keySet())
                if (!hideButton(buttonTag)) result = false;
            LogUtils.debug(UiManager.class, "hideLayout 隐藏页面结构 (layout): " + layout);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "hideLayout", e);
            return false;
        }
    }

    /**
     * 删除布局配置中的所有 UI 控件
     *
     * @param layout 布局配置对象
     * @return 全部删除成功返回 true，否则返回 false
     */
    public boolean deleteLayout (Layout layout)
    {
        try
        {
            boolean result = true;
            if (layout == null) return true;
            for (String imageTag : layout.getImageMap().keySet())
                if (!deleteImage(imageTag)) result = false;
            for (String labelTag : layout.getLabelMap().keySet())
                if (!deleteLabel(labelTag)) result = false;
            for (String buttonTag : layout.getButtonMap().keySet())
                if (!deleteButton(buttonTag)) result = false;

            // 移除对应的 scene2d Group
            Group group = layoutGroupMap.remove(layout);
            if (group != null)
            {
                group.remove();
            }

            LogUtils.debug(UiManager.class, "deleteLayout 删除页面结构 (layout): " + layout);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "deleteLayout", e);
            return false;
        }
    }

    /**
     * 打印所有可交互对象（调试用）
     */
    private void displayInteractableObject ()
    {
        LogUtils.info(UiManager.class, "displayInteractableObject (hashSet): " + interactableObjectSet);
    }

    /**
     * 将可交互对象添加到集合中
     *
     * @param interactableObject 可交互对象
     */
    private void addInteractableObject (InteractableObject interactableObject)
    {
        interactableObjectSet.add(interactableObject);
        //displayInteractableObject();
    }

    /**
     * 从集合中移除可交互对象
     *
     * @param interactableObject 可交互对象
     */
    private void removeInteractableObject (InteractableObject interactableObject)
    {
        interactableObjectSet.remove(interactableObject);
        //displayInteractableObject();
    }

    /**
     * 获取所有可交互对象的集合
     *
     * @return 可交互对象集合
     */
    public HashSet<InteractableObject> getInteractableObjectSet ()
    {
        return interactableObjectSet;
    }

    // ===================================================================================================================
    // 异步销毁辅助方法
    // ===================================================================================================================

    /**
     * 安排纹理在 50ms 后异步销毁（在主线程中执行）
     *
     * @param texture 待销毁的纹理
     */
    private void scheduleDisposeTexture (Texture texture)
    {
        if (texture == null) return;
        disposeTextureQueue.put(texture, new Object());
        disposeExecutor.schedule(() ->
        {
            if (Gdx.app != null)
            {
                SafePostRunnable.post(() ->
                {
                    if (disposeTextureQueue.containsKey(texture))
                    {
                        texture.dispose();
                        disposeTextureQueue.remove(texture);
                        LogUtils.debug(UiManager.class, "scheduleDisposeFont 异步销毁纹理: " + texture);
                    }
                });
            }
            else
            {
                // 如果Gdx.app已空，直接同步销毁（兜底）
                texture.dispose();
            }
        }, Numeric.Time.DISPOSE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 安排字体在 50ms 后异步销毁（在主线程中执行）
     *
     * @param font 待销毁的字体
     */
    private void scheduleDisposeFont (BitmapFont font)
    {
        if (font == null) return;
        disposeFontQueue.put(font, new Object());
        disposeExecutor.schedule(() ->
        {
            if (Gdx.app != null)
            {
                SafePostRunnable.post(() ->
                {
                    if (disposeFontQueue.containsKey(font))
                    {
                        font.dispose();
                        disposeFontQueue.remove(font);
                        LogUtils.debug(UiManager.class, "scheduleDisposeFont 异步销毁字体: " + font);
                    }
                });
            }
            else
            {
                font.dispose();
            }
        }, Numeric.Time.DISPOSE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    // ===================================================================================================================

    /**
     * 释放所有资源，包括 UI 控件、字体、纹理和调度器
     *
     * @return 释放成功返回 true，失败返回 false
     */
    public boolean dispose ()
    {
        try
        {
            deleteAllObject();

            // 释放字体
            for (CustomFont cf : fontMap.values())
            {
                cf.dispose();
            }
            fontMap.clear();

            // 只释放一次大纹理，避免重复释放
            if (atlasTexture != null)
            {
                scheduleDisposeTexture(atlasTexture);
                atlasTexture = null;
            }

            imageKindMap.clear();
            labelKindMap.clear();
            buttonKindMap.clear();
            imageMap.clear();
            imageKindNameMap.clear();
            imageStateMap.clear();
            labelMap.clear();
            labelKindNameMap.clear();
            labelStateMap.clear();
            labelBaseTextMap.clear();
            buttonMap.clear();
            buttonKindNameMap.clear();
            buttonStateMap.clear();
            buttonBaseTextMap.clear();
            buttonClickCallbackMap.clear();

            // 移除所有 Layout Group
            for (Group group : layoutGroupMap.values())
            {
                group.remove();
            }
            layoutGroupMap.clear();

            // 关闭调度器
            disposeExecutor.shutdown();
            try
            {
                if (!disposeExecutor.awaitTermination(Numeric.Time.ASYNC_TERMINATE_WAIT_SECONDS, TimeUnit.SECONDS)) disposeExecutor.shutdownNow();
            }
            catch (InterruptedException e)
            {
                disposeExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // 释放弹窗管理器
            if (messageBox != null)
            {
                messageBox.dispose();
            }

            LogUtils.debug(UiManager.class, "dispose 资源释放完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "dispose", e);
            return false;
        }
    }

    // ===================================================================================================================
    // UI 配置清单
    // ===================================================================================================================

    /**
     * 获取所有可用的按钮样式名
     *
     * @return 按钮样式名集合，未配置时返回空集合
     */
    public Set<String> getAvailableButtonStyles ()
    {
        return availableButtonStyles;
    }

    /**
     * 获取所有可用的标签样式名
     *
     * @return 标签样式名集合，未配置时返回空集合
     */
    public Set<String> getAvailableLabelStyles ()
    {
        return availableLabelStyles;
    }

    /**
     * 获取所有可用的图片样式名
     *
     * @return 图片样式名集合，未配置时返回空集合
     */
    public Set<String> getAvailableImageStyles ()
    {
        return availableImageStyles;
    }

    /**
     * 获取所有可用的字体样式名
     *
     * @return 字体样式名集合，未配置时返回空集合
     */
    public Set<String> getAvailableFontStyles ()
    {
        return availableFontStyles;
    }

    /**
     * 获取所有可用的消息框样式名
     *
     * @return 消息框样式名集合，未配置时返回空集合
     */
    public Set<String> getAvailableMessageBoxStyles ()
    {
        return availableMessageBoxStyles;
    }

// ===================================================================================================================
// 内部类 CustomFont (修复内存泄漏)
// ===================================================================================================================

/**
 * 自定义字体类，支持按缩放系数缓存不同大小的 BitmapFont，修复内存泄漏
 */
final class CustomFont
{
    private final float baseScale;
    private final Map<Float, BitmapFont> fontMap;
    private BitmapFont bitmapFont;       // 原始字体

    /**
     * 构造一个自定义字体，按主题指定的尺寸列表预缓存字体
     *
     * @param bitmapFont  原始位图字体
     * @param baseScale   基础缩放系数
     * @param fontUseSize 预缓存尺寸列表（缩放系数），为 null 时使用 {@link Numeric#getFontNormalScaleList()}
     */
    public CustomFont (BitmapFont bitmapFont, float baseScale, float[] fontUseSize)
    {
        this.bitmapFont = bitmapFont;
        this.bitmapFont.getData().setScale(baseScale);
        this.baseScale = baseScale;
        this.fontMap = new HashMap<>();
        float[] sizes = fontUseSize != null ? fontUseSize : Numeric.getFontNormalScaleList();
        for (float scale : sizes) getFont(scale);
    }

    /**
     * 获取指定缩放大小的字体。如果字体大小为 1.0 则返回原始字体，否则从缓存取或创建
     *
     * @param fontSize 字体大小缩放系数
     * @return 对应的 BitmapFont，失败返回 null
     */
    public BitmapFont getFont (float fontSize)
    {
        try
        {
            if (fontSize == 1.0f)
            {
                return bitmapFont;
            }
            if (fontMap.containsKey(fontSize))
            {
                return fontMap.get(fontSize);
            }
            else
            {
                BitmapFont scaledFont = new BitmapFont(bitmapFont.getData().fontFile);
                float finalScale = baseScale * fontSize;
                scaledFont.getData().setScale(finalScale);
                fontMap.put(fontSize, scaledFont);
                return scaledFont;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UiManager.class, "getFont", e);
            return null;
        }
    }

    /**
     * 释放所有缓存的字体和原始字体
     */
    public void dispose ()
    {
        // 释放缩放缓存的字体
        for (BitmapFont font : fontMap.values())
        {
            if (font != null) font.dispose();
        }
        fontMap.clear();
        // 修复内存泄漏：释放原始字体
        if (bitmapFont != null)
        {
            bitmapFont.dispose();
            bitmapFont = null;
        }
    }
}


// ===================================================================================================================
// 内部类 CustomImage (保持不变)
// ===================================================================================================================

/**
 * 自定义图片控件，包装 Image 并实现 InteractableObject 接口
 */
static final class CustomImage extends Image implements InteractableObject
{
    private final String tag;
    private final UiManager uiManager;

    /**
     * 构造一个自定义图片控件
     *
     * @param image     原始 Image 对象
     * @param tag       图片标签
     * @param uiManager UiManager 实例
     */
    public CustomImage (Image image, String tag, UiManager uiManager)
    {
        super(image.getDrawable());
        this.tag = tag;
        this.uiManager = uiManager;
        setTouchable(Touchable.enabled);
    }

//    public Actor hit(float x, float y, boolean touchable) {
//        // 如果点击在 CustomLabel 的范围内，返回 this，而不是子 Actor
//        if (touchable && getTouchable() == Touchable.enabled && getParent() != null && isVisible() && contains(x, y)) {
//            return this;
//        }
//        return null;
//    }

    // interactable interface

    /**
     * 获取图片控件的标签
     *
     * @return 标签字符串
     */
    public String getTag ()
    {
        return tag;
    }

    /**
     * 获取图片控件上边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectTop ()
    {
        return getY();
    }

    /**
     * 获取图片控件左边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectLeft ()
    {
        return getX();
    }

    /**
     * 获取图片控件下边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectBottom ()
    {
        return getY() + getHeight();
    }

    /**
     * 获取图片控件右边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectRight ()
    {
        return getX() + getWidth();
    }

    /**
     * 判断指定坐标是否在图片控件区域内
     *
     * @param x 检测点的 x 坐标
     * @param y 检测点的 y 坐标
     * @return 在区域内返回 true，否则返回 false
     */
    public boolean contains (float x, float y)
    {
        return getRectLeft() <= x && x <= getRectRight() && getRectTop() <= y && y <= getRectBottom();
    }

    /**
     * 设置图片控件的点击状态
     *
     * @param clicked 是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setClicked (boolean clicked)
    {
        return uiManager.setImageClicked(tag, clicked);
    }

    /**
     * 检查图片控件是否被点击
     *
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isClicked ()
    {
        return uiManager.isImageClicked(tag);
    }

    /**
     * 检查图片控件是否真正可见（考虑遮挡检测）
     *
     * @return 可见且未被遮挡返回 true，否则返回 false
     */
    public boolean isShown ()
    {
        // 基础可见性检查
        if (!isVisible()) return false;

        Stage stage = getStage();
        if (stage == null) return false;

        // 获取控件中心点的舞台坐标（避免边缘情况，使用中心点更稳定）
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        Vector2 localCenter = new Vector2(cx, cy);
        Vector2 stageCenter = localToStageCoordinates(localCenter);

        // hit 检测，touchable 为 true 表示只考虑可触摸的 Actor
        Actor hitActor = stage.hit(stageCenter.x, stageCenter.y, true);

        // 如果命中自己，说明没有任何上层可触摸 Actor 遮挡
        return hitActor == this;
    }

    /**
     * 显示图片控件
     *
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean show ()
    {
        return uiManager.showImage(tag);
    }

    /**
     * 隐藏图片控件
     *
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hide ()
    {
        return uiManager.hideImage(tag);
    }

    public String toString ()
    {
        return "CustomImage : " + " " + tag;
    }
}

// ===================================================================================================================
// 内部类 CustomLabel (保持不变)
// ===================================================================================================================

/**
 * 自定义标签控件，支持打字机效果和点击事件
 */
static final class CustomLabel extends Group implements InteractableObject
{
    private final Label label;
    private final String tag;
    private final UiManager uiManager;
    private Runnable clickRunnable;
    private boolean isPressed = false;
    private TextObject textObject;
    private boolean isTyping;
    private boolean isCompleteTyping;
    private float typingSpeed;
    private float typingProgress;
    private double typingLastUpdateTime;

    /**
     * 构造一个自定义标签控件
     *
     * @param textObject 文本对象
     * @param image      背景图片
     * @param label      标签组件
     * @param tag        标签标识
     * @param uiManager  UiManager 实例
     */
    public CustomLabel (TextObject textObject, Image image, Label label, String tag, UiManager uiManager)
    {
        this.textObject = textObject;
        this.label = label;
        this.tag = tag;
        this.uiManager = uiManager;
        setTouchable(Touchable.enabled);
        initTouchListener();
        addActor(image);
        addActor(label);
    }

    /**
     * 获取文本对象
     *
     * @return TextObject 实例
     */
    public TextObject getTextObject ()
    {
        return textObject;
    }

    /**
     * 设置文本对象
     *
     * @param textObject 文本对象
     */
    public void setTextObject (TextObject textObject)
    {
        this.textObject = textObject;
    }

    /**
     * 获取内部的 Label 控件
     *
     * @return Label 实例
     */
    public Label getLabel ()
    {
        return label;
    }

    /**
     * 启用打字机逐字显示效果
     *
     * @param speed 打字速度（字符/秒）
     */
    public void enableTyping (float speed)
    {
        isTyping = true;
        isCompleteTyping = false;
        typingSpeed = speed;
        typingProgress = 0;
        typingLastUpdateTime = System.currentTimeMillis() / 1000.0;
    }

    /**
     * 完成打字机效果（直接显示全部文本）
     */
    public void completeTyping ()
    {
        isTyping = false;
        isCompleteTyping = true;
    }

    /**
     * 检测点击是否命中标签控件（覆盖整个 Group 区域）
     */
    public Actor hit (float x, float y, boolean touchable)
    {
        if (touchable && getTouchable() == Touchable.enabled && isVisible() && x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight())
        {
            return this;
        }
        return null;
    }

    /**
     * 初始化触摸事件监听器，处理按下和抬起事件
     */
    private void initTouchListener ()
    {
        addListener(new ClickListener()
        {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
            {
                isPressed = true;
                return true;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button)
            {
                if (isPressed && x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight())
                {
                    if (clickRunnable != null)
                    {
                        clickRunnable.run();
                    }
                }
                isPressed = false;
            }

            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                isPressed = false;
            }
        });
    }

    /**
     * 绘制标签控件
     */
    public void draw (Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
    }

    /**
     * 更新标签文本内容，如果正在打字机模式则逐字显示
     */
    public void updateText ()
    {
        if (label != null)
        {
            // 获取当前文本
            String text = textObject.getDisplayText();

            // 逐字显示文本
            if (isTyping)
            {
                float deltaTime = (float) (System.currentTimeMillis() / 1000.0 - typingLastUpdateTime);
                typingLastUpdateTime = System.currentTimeMillis() / 1000.0;
                typingProgress += deltaTime * typingSpeed;

                if (typingProgress >= text.length())
                {
                    typingProgress = text.length();
                    completeTyping();
                }

                text = text.substring(0, (int) typingProgress);
                label.setText(text);
            }
            else
            {
                label.setText(text);
            }
        }
    }

    /**
     * 每帧更新逻辑，用于驱动打字机效果
     *
     * @param delta 距离上一帧的时间间隔
     */
    public void act (float delta)
    {
        super.act(delta);
        updateText();
    }

    /**
     * 设置点击监听器
     *
     * @param clickListener 点击回调
     */
    public void setClickListener (Runnable clickListener)
    {
        this.clickRunnable = clickListener;
    }

    /**
     * 设置点击运行器（同 setClickListener）
     *
     * @param clickRunnable 点击回调
     */
    public void setClickRunnable (Runnable clickRunnable)
    {
        this.clickRunnable = clickRunnable;
    }

    /**
     * 检查标签是否被按下
     *
     * @return 按下返回 true，否则返回 false
     */
    public boolean isPressed ()
    {
        return isPressed;
    }

    /**
     * 设置标签是否可触摸
     *
     * @param enabled 启用触摸返回 true，禁用返回 false
     */
    public void setEnabled (boolean enabled)
    {
        setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }

    // interactable interface

    /**
     * 获取标签标识
     *
     * @return 标签字符串
     */
    public String getTag ()
    {
        return tag;
    }

    /**
     * 获取标签控件上边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectTop ()
    {
        return getY();
    }

    /**
     * 获取标签控件左边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectLeft ()
    {
        return getX();
    }

    /**
     * 获取标签控件下边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectBottom ()
    {
        return getY() + getHeight();
    }

    /**
     * 获取标签控件右边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectRight ()
    {
        return getX() + getWidth();
    }

    /**
     * 判断指定坐标是否在标签控件区域内
     *
     * @param x 检测点的 x 坐标
     * @param y 检测点的 y 坐标
     * @return 在区域内返回 true，否则返回 false
     */
    public boolean contains (float x, float y)
    {
        return getRectLeft() <= x && x <= getRectRight() && getRectTop() <= y && y <= getRectBottom();
    }

    /**
     * 设置标签的点击状态
     *
     * @param clicked 是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setClicked (boolean clicked)
    {
        return uiManager.setLabelClicked(tag, clicked);
    }

    /**
     * 检查标签是否被点击
     *
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isClicked ()
    {
        return uiManager.isLabelClicked(tag);
    }

    /**
     * 检查标签是否真正可见（考虑遮挡检测）
     *
     * @return 可见且未被遮挡返回 true，否则返回 false
     */
    public boolean isShown ()
    {
        // 基础可见性检查
        if (!isVisible()) return false;

        Stage stage = getStage();
        if (stage == null) return false;

        // 获取控件中心点的舞台坐标（避免边缘情况，使用中心点更稳定）
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        Vector2 localCenter = new Vector2(cx, cy);
        Vector2 stageCenter = localToStageCoordinates(localCenter);

        // hit 检测，touchable 为 true 表示只考虑可触摸的 Actor
        Actor hitActor = stage.hit(stageCenter.x, stageCenter.y, true);

        // 如果命中自己，说明没有任何上层可触摸 Actor 遮挡
        return hitActor == this;
    }

    /**
     * 显示标签控件
     *
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean show ()
    {
        return uiManager.showLabel(tag);
    }

    /**
     * 隐藏标签控件
     *
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hide ()
    {
        return uiManager.hideLabel(tag);
    }

    public String toString ()
    {
        return "CustomLabel : " + " " + tag + " " + label.getText();
    }
}

// ===================================================================================================================
// 内部类 CustomTextButton (保持不变)
// ===================================================================================================================

/**
 * 自定义文本按钮控件，直接使用 Actor 绘制，不依赖 Scene2d 的 TextButton
 */
static final class CustomTextButton extends Actor implements InteractableObject
{
    private final TextButton.TextButtonStyle style;
    private final BitmapFont font;
    private final String tag;
    private final UiManager uiManager;
    private Runnable clickRunnable;
    private boolean isPressed = false;
    private boolean isDisabled = false;
    private TextObject textObject;
    private String text;

    /**
     * 构造一个自定义文本按钮
     *
     * @param textObject 文本对象
     * @param style      按钮样式
     * @param tag        按钮标签
     * @param uiManager  UiManager 实例
     */
    public CustomTextButton (TextObject textObject, TextButton.TextButtonStyle style, String tag, UiManager uiManager)
    {
        this.style = style;
        this.font = style.font;
        this.textObject = textObject;
        this.tag = tag;
        this.uiManager = uiManager;
        initTouchListener();
    }

    /**
     * 获取文本对象
     *
     * @return TextObject 实例
     */
    public TextObject getTextObject ()
    {
        return textObject;
    }

    /**
     * 设置文本对象
     *
     * @param textObject 文本对象
     */
    public void setTextObject (TextObject textObject)
    {
        this.textObject = textObject;
    }

    /**
     * 获取按钮样式
     *
     * @return 按钮样式对象
     */
    public TextButton.TextButtonStyle getButtonStyle ()
    {
        return style;
    }

    /**
     * 初始化触摸事件监听器，处理按下、抬起和退出事件
     */
    private void initTouchListener ()
    {
        setTouchable(Touchable.enabled);
        addListener(new ClickListener()
        {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
            {
                if (!isDisabled)
                {
                    isPressed = true;
                    return true;
                }
                return false;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button)
            {
                if (!isDisabled && isPressed)
                {
                    isPressed = false;
                    if (clickRunnable != null)
                    {
                        clickRunnable.run();
                    }
                }
            }

            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                isPressed = false;
            }
        });
    }

    /**
     * 绘制按钮，包括背景和文字
     */
    public void draw (Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
        if (font == null || style == null) return;

        Drawable currentBg = getCurrentBackground();
        if (currentBg != null)
        {
            currentBg.draw(batch, getX(), getY(), getWidth(), getHeight());
        }

        Color currentFontColor = getCurrentFontColor();
        font.setColor(currentFontColor);
        font.draw(
            batch,
            text,
            getX(),
            getY() + getHeight() / 2 + font.getCapHeight() / 2,
            getWidth(),
            Align.center,
            false
        );
    }

    /**
     * 更新按钮显示的文本内容
     */
    public void updateText ()
    {
        text = textObject.getDisplayText();
    }

    /**
     * 每帧更新逻辑
     *
     * @param delta 距离上一帧的时间间隔
     */
    public void act (float delta)
    {
        super.act(delta);
        updateText();
    }

    /**
     * 根据按钮状态（禁用/按下/正常）获取当前背景 Drawable
     *
     * @return 对应的背景 Drawable
     */
    private Drawable getCurrentBackground ()
    {
        if (isDisabled)
        {
            return style.disabled != null ? style.disabled : style.up;
        }
        else if (isPressed)
        {
            return style.down != null ? style.down : style.up;
        }
        else
        {
            return style.up;
        }
    }

    /**
     * 根据按钮状态（禁用/按下/正常）获取当前字体颜色
     *
     * @return 对应的字体颜色
     */
    private Color getCurrentFontColor ()
    {
        if (isDisabled)
        {
            return style.disabledFontColor != null ? style.disabledFontColor : style.fontColor;
        }
        else if (isPressed)
        {
            return style.downFontColor != null ? style.downFontColor : style.fontColor;
        }
        else
        {
            return style.fontColor;
        }
    }

    /**
     * 检查按钮是否被禁用
     *
     * @return 禁用返回 true，否则返回 false
     */
    public boolean isDisabled ()
    {
        return isDisabled;
    }

    /**
     * 设置按钮的禁用状态
     *
     * @param disabled 禁用为 true，启用为 false
     */
    public void setDisabled (boolean disabled)
    {
        this.isDisabled = disabled;
        setTouchable(disabled ? Touchable.disabled : Touchable.enabled);
    }

    /**
     * 设置点击运行器
     *
     * @param clickRunnable 点击回调
     */
    public void setClickRunnable (Runnable clickRunnable)
    {
        this.clickRunnable = clickRunnable;
    }

    // interactable interface

    /**
     * 获取按钮标签
     *
     * @return 标签字符串
     */
    public String getTag ()
    {
        return tag;
    }

    /**
     * 获取按钮控件上边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectTop ()
    {
        return getY();
    }

    /**
     * 获取按钮控件左边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectLeft ()
    {
        return getX();
    }

    /**
     * 获取按钮控件下边界 y 坐标
     *
     * @return y 坐标值
     */
    public float getRectBottom ()
    {
        return getY() + getHeight();
    }

    /**
     * 获取按钮控件右边界 x 坐标
     *
     * @return x 坐标值
     */
    public float getRectRight ()
    {
        return getX() + getWidth();
    }

    /**
     * 判断指定坐标是否在按钮控件区域内
     *
     * @param x 检测点的 x 坐标
     * @param y 检测点的 y 坐标
     * @return 在区域内返回 true，否则返回 false
     */
    public boolean contains (float x, float y)
    {
        return getRectLeft() <= x && x <= getRectRight() && getRectTop() <= y && y <= getRectBottom();
    }

    /**
     * 设置按钮的点击状态
     *
     * @param clicked 是否被点击
     * @return 设置成功返回 true，失败返回 false
     */
    public boolean setClicked (boolean clicked)
    {
        return uiManager.setButtonClicked(tag, clicked);
    }

    /**
     * 检查按钮是否被点击
     *
     * @return 被点击返回 true，否则返回 false
     */
    public boolean isClicked ()
    {
        return uiManager.isButtonClicked(tag);
    }

    /**
     * 检查按钮是否真正可见（考虑遮挡检测）
     *
     * @return 可见且未被遮挡返回 true，否则返回 false
     */
    public boolean isShown ()
    {
        // 基础可见性检查
        if (!isVisible()) return false;

        Stage stage = getStage();
        if (stage == null) return false;

        // 获取控件中心点的舞台坐标（避免边缘情况，使用中心点更稳定）
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        Vector2 localCenter = new Vector2(cx, cy);
        Vector2 stageCenter = localToStageCoordinates(localCenter);

        // hit 检测，touchable 为 true 表示只考虑可触摸的 Actor
        Actor hitActor = stage.hit(stageCenter.x, stageCenter.y, true);

        // 如果命中自己，说明没有任何上层可触摸 Actor 遮挡
        return hitActor == this;
    }

    /**
     * 显示按钮控件
     *
     * @return 显示成功返回 true，失败返回 false
     */
    public boolean show ()
    {
        return uiManager.showButton(tag);
    }

    /**
     * 隐藏按钮控件
     *
     * @return 隐藏成功返回 true，失败返回 false
     */
    public boolean hide ()
    {
        return uiManager.hideButton(tag);
    }

    public String toString ()
    {
        return "CustomTextButton : " + " " + tag + " " + text;
    }
}
}
