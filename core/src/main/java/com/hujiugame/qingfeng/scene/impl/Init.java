package com.hujiugame.qingfeng.scene.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.core.UpdateChecker;
import com.hujiugame.qingfeng.util.interact.NativeDialogUtils;
import com.hujiugame.qingfeng.util.system.CrashUtils;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.game.InitState;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameSubState;
import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.type.key.ThemeKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPushGameState;
import com.hujiugame.qingfeng.manager.LanguageManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.manager.UserConfigManager;
import com.hujiugame.qingfeng.util.StringPolisher;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class Init implements GameRender
{
    private final UpdateChecker updateChecker;
    private final GameHost gameHost;
    private final UserConfigManager userConfigManager;
    private final LanguageManager languageManager;
    private final ThemeManager themeManager;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final EventQueue eventQueue;

    private static final float STEP_DELAY = 0.4f;

    /** 进度条高度占屏幕高度比例 */
    private static final float PROCESS_BAR_HEIGHT_RATIO = 0.02f;

    /** 维修图标位置与尺寸（虚拟坐标） */
    private static final int REPAIR_IMAGE_X = 2400;
    private static final int REPAIR_IMAGE_Y = 48;
    private static final int REPAIR_IMAGE_SIZE = 96;

    private int initState = 0;
    private float initTimer = 0f;

    private String backgroundPictureTag;
    private String processPictureTag;
    private String repairImageTag;
    private String repairImageKind;

    private Color processColor = null;

    /** 正在执行资源修复，阻止状态机继续推进 */
    private boolean isRepairing = false;

    // ===================================================================================================================

    public Init (UpdateChecker updateChecker, GameHost gameHost,
                 UserConfigManager userConfigManager,
                 LanguageManager languageManager, ThemeManager themeManager, AudioManager audioManager,
                 GraphicsManager graphicsManager, UiManager uiManager,
                 EventQueue eventQueue)
    {
        this.updateChecker = updateChecker;
        this.gameHost = gameHost;
        this.userConfigManager = userConfigManager;
        this.languageManager = languageManager;
        this.themeManager = themeManager;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.uiManager = uiManager;
        this.eventQueue = eventQueue;
    }

    // ===================================================================================================================

    private void showProcess ()
    {
        // 计算进度
        float processPercent = (float) (initState + 1 ) / (InitState.TOTAL + 1);
        int processPictureX = 0;
        int processPictureY = 0;
        int processPictureWidth = (int) (ScreenSize.WIDTH * processPercent);
        int processPictureHeight = (int) (ScreenSize.HEIGHT * PROCESS_BAR_HEIGHT_RATIO);

        // 绘制进度（使用主题色叠加）
        if (processColor != null)
        {
            graphicsManager.putPicture(processPictureTag, processPictureX, processPictureY,
                processPictureWidth, processPictureHeight, processColor);
        }
        else
        {
            graphicsManager.putPicture(processPictureTag, processPictureX, processPictureY,
                processPictureWidth, processPictureHeight);
        }
    }

    /**
     * 从外部 app_config.json 读取进度条颜色，读取失败时使用默认色
     */
    private void loadProcessColor ()
    {
        try
        {
            FileHandle appConfigHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET, FileName.APP_CONFIG));
            if (FileUtils.isFileExist(appConfigHandle))
            {
                JsonEntity appConfig = new JsonEntity(appConfigHandle);
                if (appConfig.containsKey(ThemeKey.PROCESS_COLOR))
                {
                    String processColorStr = appConfig.getString(ThemeKey.PROCESS_COLOR);
                    if (processColorStr != null)
                    {
                        processColor = Color.valueOf(processColorStr);
                        LogUtils.debug(Init.class, "loadProcessColor 读取进度条颜色: " + processColor);
                        return;
                    }
                    else
                    {
                        LogUtils.error(Init.class, "loadProcessColor " + ThemeKey.PROCESS_COLOR + " 字段类型不是字符串");
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogUtils.error(Init.class, "loadProcessColor", e);
        }

        // 读取失败时使用默认色
        processColor = Color.valueOf("#3F47B5FF");
        LogUtils.debug(Init.class, "loadProcessColor 使用默认进度条颜色: " + processColor);
    }

    private void repairGame ()
    {
        isRepairing = true;
        updateChecker.repairGame(() ->
        {
            NativeDialogUtils.showInfo("修复完成", "游戏资源已修复完成，请重启游戏。", Gdx.app::exit);
        });
    }

    // ===================================================================================================================

    private void initUserConfig ()
    {
        // 等待文件差异化更新
        if (updateChecker.doFileUpdateFinish())
        {
            // 记录版本，以及读取用户&游戏配置
            gameHost.getGameInfoManager().putInfo(GameInfoKey.Launcher.VERSION, updateChecker.getInternalVersionString());
            if (!gameHost.getGameResolver().load())
            {
                LogUtils.error(Init.class, "initUserConfig 读取游戏配置失败");
                CrashUtils.crash(new RuntimeException("initUserConfig gameResolver.load() 读取游戏配置失败"));
                return;
            }
            LogUtils.debug(Init.class, "initUserConfig 读取游戏配置成功");
            // 上传用户&游戏配置 到游戏信息管理器
            userConfigManager.uploadTo(gameHost.getGameInfoManager());
            languageManager.uploadTo(gameHost.getGameInfoManager());
            themeManager.uploadTo(gameHost.getGameInfoManager());
            initState++;
        }
    }

    private void initAudio ()
    {
        if (!audioManager.init())
        {
            LogUtils.error(Init.class, "initAudio audioManager.init() 音频初始化失败");
            CrashUtils.crash(new RuntimeException("initAudio audioManager.init() 音频初始化失败"));
            return;
        }
        else
        {
            LogUtils.debug(Init.class, "initAudio audioManager.init() 音频初始化成功");
        }
        initState++;
    }

    private void initGraphics ()
    {
        if (!graphicsManager.init())
        {
            LogUtils.error(Init.class, "initGraphics graphicsManager.init() 绘图初始化失败");
            CrashUtils.crash(new RuntimeException("initGraphics graphicsManager.init() 绘图初始化失败"));
            return;
        }
        else
        {
            LogUtils.debug(Init.class, "initGraphics graphicsManager.init() 绘图初始化成功");
        }
        initState++;
    }

    private void initUi ()
    {
        if (!uiManager.init(themeManager))
        {
            LogUtils.error(Init.class, "initUi uiManager.init() ui初始化失败");
            CrashUtils.crash(new RuntimeException("initUi uiManager.init() ui初始化失败"));
            return;
        }
        else
        {
            LogUtils.debug(Init.class, "initUi uiManager.init() ui初始化成功");
        }
        if (!uiManager.setGraphicsQuoteFont(graphicsManager))
        {
            LogUtils.error(Init.class, "initUi uiManager.setGraphicsQuoteFont(graphicsManager) 设置字体失败");
            CrashUtils.crash(new RuntimeException("initUi uiManager.setGraphicsQuoteFont(graphicsManager) 设置字体失败"));
            return;
        }
        else
        {
            LogUtils.debug(Init.class, "initUi uiManager.setGraphicsQuoteFont(graphicsManager) 设置字体成功");
        }
        initState++;
    }

    private void initStop()
    {
        // 链接到网页判断需不需要更新
        checkUpdate();

        // 跳转菜单
        eventQueue.addEvent(new EventPushGameState(GameState.MENU, GameSubState.MENU_MAIN));
    }

    // ===================================================================================================================

    private void checkUpdate()
    {
        updateChecker.checkWebVersion();
    }

    // ===================================================================================================================

    /**
     * 初始化启动画面，缓存背景图、进度条、维修图标资源
     *
     * @param gameStateDataContainer 游戏状态数据容器
     */
    @Override
    public void init (GameStateDataContainer gameStateDataContainer)
    {
        backgroundPictureTag = StringPolisher.polished("init");
        processPictureTag = StringPolisher.polished("process");
        repairImageTag = StringPolisher.polished("repair");
        repairImageKind = StringPolisher.polished("repair.image");

        // 背景图缓存
        FileHandle internalSplashPictureHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_RESOURCE_IMAGE, FileName.DEFAULT_SPLASH));
        FileHandle externalSplashPictureHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET_S_RESOURCE_IMAGE, FileName.DEFAULT_SPLASH));

        // 存在外部的背景图缓存文件
        if (!FileUtils.isFileExist(externalSplashPictureHandle))
        {
            graphicsManager.loadBackgroundPicture(backgroundPictureTag, internalSplashPictureHandle);
        }
        else
        {
            graphicsManager.loadBackgroundPicture(backgroundPictureTag, externalSplashPictureHandle);
        }

        // 创建 1x1 白色纹理作为进度条基底（通过 tint 叠加主题色）
        graphicsManager.loadWhitePicture(processPictureTag);

        // 维修图标缓存和显示
        FileHandle repairPictureHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_RESOURCE_IMAGE, FileName.DEFAULT_REPAIR));
        uiManager.loadImageKind(repairImageKind, repairPictureHandle);
        uiManager.addImage(repairImageTag, repairImageKind, REPAIR_IMAGE_X, REPAIR_IMAGE_Y, REPAIR_IMAGE_SIZE, REPAIR_IMAGE_SIZE);

        // 从 app_config.json 读取进度条颜色
        loadProcessColor();
    }

    /**
     * 逐帧执行初始化步骤（用户配置→音频→图形→UI→弹窗→完成）
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 每步间隔，让 splash 画面有时间显示
        initTimer += deltaTime;
        if (initTimer < STEP_DELAY) return;
        initTimer = 0f;

        // 检测修复软件按钮按下
        if (uiManager.isImageClicked(repairImageTag))
        {
            repairGame();
        }

        // 修复中跳过状态机，等待修复完成后弹窗退出
        if (isRepairing) return;

        // 初始化状态
        switch (initState)
        {
            case InitState.USER_CONFIG:
                initUserConfig();
                break;

            case InitState.AUDIO:
                initAudio();
                break;

            case InitState.GRAPHICS:
                initGraphics();
                break;

            case InitState.UI:
                initUi();
                break;

            case InitState.TOTAL:
                initStop();
                break;
        }
    }

    /**
     * 渲染启动画面背景和进度条
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        // 显示闪图背景
        graphicsManager.putBackgroundPicture(backgroundPictureTag);

        // 进度
        showProcess();
    }

    /**
     * 释放启动画面资源
     */
    @Override
    public void dispose ()
    {
        graphicsManager.disposePicture(processPictureTag);
        uiManager.deleteImage(repairImageTag);
        uiManager.removeImageKind(repairImageKind);
    }
}
