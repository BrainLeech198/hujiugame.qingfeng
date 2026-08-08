package com.hujiugame.qingfeng.di;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.core.GameResolver;
import com.hujiugame.qingfeng.core.RenderPipeline;
import com.hujiugame.qingfeng.core.SceneStack;
import com.hujiugame.qingfeng.core.UpdateChecker;
import com.hujiugame.qingfeng.event.EventDispatcher;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.game.GameLogicService;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameSubState;
import com.hujiugame.qingfeng.type.ui.UseViewport;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRenderRegistry;
import com.hujiugame.qingfeng.scene.impl.*;
import com.hujiugame.qingfeng.ui.MessageBox;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.manager.*;
import com.hujiugame.qingfeng.input.ControllerInputHandler;
import com.hujiugame.qingfeng.input.KeyboardInputHandler;
import com.hujiugame.qingfeng.input.VirtualInputHandler;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class InstanceContent
{
    private static volatile InstanceContent instanceContent;

    private String rootPath = null;

    private UseViewport useViewport;
    private SpriteBatch spriteBatch;
    private Stage stage;

    private VirtualInputHandler virtualInputHandler;
    private KeyboardInputHandler keyboardInputHandler;
    private ControllerInputHandler controllerInputHandler;

    private UserConfigManager userConfigManager;
    private ThemeManager themeManager;
    private LanguageManager languageManager;
    private EventQueue eventQueue;

    private GraphicsManager graphicsManager;
    private AudioManager audioManager;

    private UiManager uiManager;

    private TextManager textManager;
    private LayoutManager layoutManager;

    private RenderPipeline renderPipeline;
    private SceneStack sceneStack;
    private GameResolver gameResolver;
    private EventDispatcher eventDispatcher;
    private GameLogicService gameLogicService;

    private UpdateChecker updateChecker;
    private GameHost gameHost;

    // ===================================================================================================================

    private InstanceContent ()
    {
    }

    /**
     * 获取 InstanceContent 单例
     *
     * @return InstanceContent 实例，未初始化则返回 null
     */
    @javax.annotation.Nullable
    public static InstanceContent getInstance ()
    {
        return instanceContent;
    }

    /*
     * 创建渲染注册表，注册所有状态对应的渲染机
     */
    private static void registerRenderRegistry ()
    {
        GameRenderRegistry registry = new GameRenderRegistry();
        registry.register(GameState.INIT, GameSubState.INIT,
            () -> new Init(instanceContent.updateChecker, instanceContent.gameHost,
                instanceContent.userConfigManager,
                instanceContent.languageManager, instanceContent.themeManager, instanceContent.audioManager,
                instanceContent.graphicsManager, instanceContent.uiManager,
                instanceContent.eventQueue));

        registry.register(GameState.MENU, GameSubState.MENU_MAIN,
            () -> new MenuMain(instanceContent.updateChecker, instanceContent.audioManager,
                instanceContent.graphicsManager, instanceContent.themeManager,
                instanceContent.uiManager,
                instanceContent.eventQueue,
                instanceContent.useViewport,
                instanceContent.virtualInputHandler));
        registry.register(GameState.MENU, GameSubState.MENU_LIST,
            () -> new MenuList(instanceContent.updateChecker, instanceContent.audioManager,
                instanceContent.graphicsManager, instanceContent.uiManager,
                instanceContent.eventQueue,
                instanceContent.gameHost, instanceContent.rootPath));
        registry.register(GameState.MENU, GameSubState.MENU_LOAD,
            () -> new MenuLoad(instanceContent.audioManager, instanceContent.graphicsManager,
                instanceContent.uiManager, instanceContent.eventQueue,
                instanceContent.gameHost));

        registry.register(GameState.CONFIG, GameSubState.CONFIG_BASIC,
            () -> new ConfigBasic(instanceContent.eventQueue, instanceContent.audioManager,
                instanceContent.graphicsManager, instanceContent.uiManager));

        registry.register(GameState.GAME, GameSubState.GAME_MENU,
            () -> new GameMenu(instanceContent.eventQueue, instanceContent.gameHost));
        registry.register(GameState.GAME, GameSubState.GAME_ROLE,
            () -> new GameRole(instanceContent.eventQueue, instanceContent.layoutManager,
                instanceContent.gameHost));
        registry.register(GameState.GAME, GameSubState.GAME_PLAY,
            () -> new GamePlay(instanceContent.eventQueue, instanceContent.layoutManager,
                instanceContent.gameHost));
        instanceContent.renderPipeline = new RenderPipeline(registry, deltaTime -> instanceContent.update(deltaTime));
    }

    /**
     * 初始化 InstanceContent 单例并创建所有管理器和控制器实例
     *
     * @param rootPath    游戏资源根目录路径
     * @param useViewport 使用的视口类型
     * @param spriteBatch 画笔
     * @param stage       舞台
     * @return 是否初始化成功
     */
    public static boolean init (String rootPath, UseViewport useViewport, SpriteBatch spriteBatch, Stage stage)
    {
        try
        {
            // 基本赋值
            long start = System.nanoTime();
            instanceContent = new InstanceContent();
            instanceContent.rootPath = rootPath;
            instanceContent.useViewport = useViewport;
            instanceContent.spriteBatch = spriteBatch;
            instanceContent.stage = stage;
            LogUtils.debug(InstanceContent.class, "init - 基本赋值耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // 数据管理类
            start = System.nanoTime();
            instanceContent.userConfigManager = new UserConfigManager();
            LogUtils.debug(InstanceContent.class, "init - UserConfigManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.themeManager = new ThemeManager();
            LogUtils.debug(InstanceContent.class, "init - ThemeManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.textManager = new TextManager();
            LogUtils.debug(InstanceContent.class, "init - TextManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.layoutManager = new LayoutManager();
            LogUtils.debug(InstanceContent.class, "init - LayoutManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.languageManager = new LanguageManager();
            LogUtils.debug(InstanceContent.class, "init - LanguageManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.eventQueue = new EventQueue();
            LogUtils.debug(InstanceContent.class, "init - EventQueue 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // 音频图像管理类
            start = System.nanoTime();
            instanceContent.audioManager = new AudioManager(instanceContent.userConfigManager);
            LogUtils.debug(InstanceContent.class, "init - AudioManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.graphicsManager = new GraphicsManager(instanceContent.spriteBatch, instanceContent.themeManager);
            LogUtils.debug(InstanceContent.class, "init - GraphicsManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // UI管理类
            start = System.nanoTime();
            instanceContent.uiManager = new UiManager(instanceContent.stage, instanceContent.audioManager, instanceContent.graphicsManager, instanceContent.textManager);
            LogUtils.debug(InstanceContent.class, "init - UiManager 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // 游戏管理类
            start = System.nanoTime();

            // 创建渲染注册表，注册所有状态对应的渲染机
            registerRenderRegistry();

            LogUtils.debug(InstanceContent.class, "init - RenderPipeline 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.sceneStack = new SceneStack();
            LogUtils.debug(InstanceContent.class, "init - SceneStack 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.gameResolver = new GameResolver();
            LogUtils.debug(InstanceContent.class, "init - GameResolver 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.eventDispatcher = new EventDispatcher();
            LogUtils.debug(InstanceContent.class, "init - EventDispatcher 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.gameLogicService = new GameLogicService();
            LogUtils.debug(InstanceContent.class, "init - GameLogicService 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // 控制类
            start = System.nanoTime();
            instanceContent.updateChecker = new UpdateChecker(instanceContent);
            LogUtils.debug(InstanceContent.class, "init - UpdateChecker 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");
            start = System.nanoTime();
            instanceContent.gameHost = new GameHost(
                instanceContent.userConfigManager,
                instanceContent.themeManager,
                instanceContent.languageManager,
                instanceContent.eventQueue,
                instanceContent.audioManager,
                instanceContent.graphicsManager,
                instanceContent.uiManager,
                instanceContent.layoutManager,
                instanceContent.textManager,
                instanceContent.spriteBatch,
                instanceContent.stage,
                instanceContent.renderPipeline,
                instanceContent.sceneStack,
                instanceContent.gameResolver,
                instanceContent.eventDispatcher,
                instanceContent.gameLogicService
            );
            LogUtils.debug(InstanceContent.class, "init - GameHost 耗时: " + (System.nanoTime() - start) / 1000000 + "ms");

            // 后续注入一般很快，不单独计时
            instanceContent.textManager.setLanguageManager(instanceContent.languageManager);
            instanceContent.textManager.setGameInfoManager(instanceContent.gameHost.getGameInfoManager());
            instanceContent.layoutManager.setAudioManager(instanceContent.audioManager);
            instanceContent.layoutManager.setGraphicsManager(instanceContent.graphicsManager);
            instanceContent.layoutManager.setUiManager(instanceContent.uiManager);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(InstanceContent.class, "init", e);
            return false;
        }
    }

    // 输入器逐个添加

    /**
     * 获取游戏资源根目录路径
     * @return 根目录路径，未初始化则返回 null
     */
    @javax.annotation.Nullable
    public String getRootPath ()
    {
        return rootPath;
    }

    /**
     * 获取使用的视口类型
     * @return 视口类型
     */
    public UseViewport getUseViewport ()
    {
        return useViewport;
    }

    /**
     * 获取画笔
     * @return 精灵画笔
     */
    public SpriteBatch getSpriteBatch ()
    {
        return spriteBatch;
    }

    /**
     * 获取舞台
     * @return 舞台
     */
    public Stage getStage ()
    {
        return stage;
    }

    /**
     * 获取虚拟输入处理器
     * @return 虚拟输入处理器
     */
    public VirtualInputHandler getVirtualInputHandler ()
    {
        return virtualInputHandler;
    }

    /**
     * 设置虚拟输入处理器
     *
     * @param virtualInputHandler 虚拟输入处理器
     */
    public void setVirtualInputHandler (VirtualInputHandler virtualInputHandler)
    {
        this.virtualInputHandler = virtualInputHandler;
    }

    /**
     * 获取键盘输入处理器
     * @return 键盘输入处理器
     */
    public KeyboardInputHandler getKeyboardInputHandler ()
    {
        return keyboardInputHandler;
    }

    /**
     * 设置键盘输入处理器
     *
     * @param keyboardInputHandler 键盘输入处理器
     */
    public void setKeyboardInputHandler (KeyboardInputHandler keyboardInputHandler)
    {
        this.keyboardInputHandler = keyboardInputHandler;
    }

    /**
     * 获取手柄输入处理器
     * @return 手柄输入处理器
     */
    public ControllerInputHandler getControllerInputHandler ()
    {
        return controllerInputHandler;
    }

    /**
     * 设置手柄输入处理器
     *
     * @param controllerInputHandler 手柄输入处理器
     */
    public void setControllerInputHandler (ControllerInputHandler controllerInputHandler)
    {
        this.controllerInputHandler = controllerInputHandler;
    }

    /**
     * 获取用户配置管理器
     * @return 用户配置管理器
     */
    public UserConfigManager getUserConfigManager ()
    {
        return userConfigManager;
    }

    /**
     * 获取主题管理器
     * @return 主题管理器
     */
    public ThemeManager getThemeManager ()
    {
        return themeManager;
    }

    /**
     * 获取语言管理器
     * @return 语言管理器
     */
    public LanguageManager getLanguageManager ()
    {
        return languageManager;
    }

    /**
     * 获取事件队列
     * @return 事件队列
     */
    public EventQueue getEventQueue ()
    {
        return eventQueue;
    }

    /**
     * 获取图形管理器
     * @return 图形管理器
     */
    public GraphicsManager getGraphicsManager ()
    {
        return graphicsManager;
    }

    /**
     * 获取音频管理器
     * @return 音频管理器
     */
    public AudioManager getAudioManager ()
    {
        return audioManager;
    }

    /**
     * 获取文本管理器
     * @return 文本管理器
     */
    public TextManager getTextManager ()
    {
        return textManager;
    }

    /**
     * 获取 UI 管理器
     * @return UI 管理器
     */
    public UiManager getUiManager ()
    {
        return uiManager;
    }

    /**
     * 获取消息弹窗管理器（委托给 UiManager）
     * @return 消息弹窗管理器
     */
    public synchronized MessageBox getMessageBox ()
    {
        return uiManager.getMessageBox();
    }

    /**
     * 获取布局管理器
     * @return 布局管理器
     */
    public LayoutManager getLayoutManager ()
    {
        return layoutManager;
    }

    /**
     * 获取游戏渲染器
     * @return 游戏渲染器
     */
    public RenderPipeline getRenderPipeline ()
    {
        return renderPipeline;
    }

    /**
     * 获取场景栈
     * @return 场景栈
     */
    public SceneStack getSceneStack ()
    {
        return sceneStack;
    }

    /**
     * 获取游戏解析器
     * @return 游戏解析器
     */
    public GameResolver getGameResolver ()
    {
        return gameResolver;
    }

    /**
     * 获取事件分发器
     * @return 事件分发器
     */
    public EventDispatcher getEventDispatcher ()
    {
        return eventDispatcher;
    }

    /**
     * 获取游戏逻辑服务
     * @return 游戏逻辑服务
     */
    public GameLogicService getGameLogicService ()
    {
        return gameLogicService;
    }

    /**
     * 获取更新检测器
     * @return 更新检测器
     */
    public UpdateChecker getUpdateChecker ()
    {
        return updateChecker;
    }

    /**
     * 获取游戏主机
     * @return 游戏主机
     */
    public GameHost getGameHost ()
    {
        return gameHost;
    }

    /**
     * 每帧更新输入处理器状态
     * @param deltaTime 帧时间增量
     */
    public void update (float deltaTime)
    {
        if (controllerInputHandler != null) controllerInputHandler.update(deltaTime);
        if (virtualInputHandler != null) virtualInputHandler.update();
    }

    /**
     * 顶层绘制，绘制虚拟输入覆盖层
     */
    public void topRender ()
    {
        if (instanceContent.getVirtualInputHandler() != null) instanceContent.getVirtualInputHandler().topRender();
        if (instanceContent.getControllerInputHandler() != null) instanceContent.getControllerInputHandler().topRender();
    }

    /**
     * 释放所有管理器资源
     */
    public void dispose ()
    {
        updateChecker.dispose();
        gameHost.dispose();

        uiManager.dispose();

        graphicsManager.dispose();
        audioManager.dispose();
    }

}
