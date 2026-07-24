package com.hujiugame.qingfeng.core;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.game.GameLogicService;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.util.system.CrashUtils;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.data.game.StateStructure;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameStatePageInfo;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.manager.LanguageManager;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Map;
import java.util.Stack;

public final class SceneStack
{
    private final Stack<StateStructure> stateStack = new Stack<>();
    private ThemeManager themeManager;
    private LayoutManager layoutManager;
    private PlayLocalData playLocalData;
    private RenderPipeline renderPipeline;

    // 游戏状态栈，初始化时推入默认状态 (0,0)，后续通过 push/pop/set/reset 管理
    {
        stateStack.push(new StateStructure(0, 0));
    }
    private boolean isInGame = false;

    /**
     * 初始化状态服务，绑定各管理器
     *
     * @param themeManager    主题管理器
     * @param languageManager 语言管理器
     * @param audioManager    音频管理器
     * @param graphicsManager 图形管理器
     * @param uiManager       UI管理器
     * @param layoutManager   布局管理器
     * @param playLocalData   游戏数据容器
     * @param renderPipeline  游戏渲染器
     * @param gameLogicService 游戏逻辑
     * @return 是否初始化成功
     */
    public boolean init (ThemeManager themeManager,
                         LanguageManager languageManager,
                         AudioManager audioManager,
                         GraphicsManager graphicsManager,
                         UiManager uiManager,
                         LayoutManager layoutManager,
                         PlayLocalData playLocalData,
                         RenderPipeline renderPipeline,
                         GameLogicService gameLogicService)
    {
        try
        {
            this.themeManager = themeManager;
            this.layoutManager = layoutManager;

            this.playLocalData = playLocalData;
            this.renderPipeline = renderPipeline;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "init", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 更新游戏状态：重新加载布局并切换渲染机
     *
     * @return 是否更新成功
     */
    public boolean updateGameState ()
    {
        try
        {
            // debug更新状态
            LogUtils.debug(SceneStack.class, "updateGameState 状态 (stateStructure): " + getCurrentState()
                + " (name): " + GameState.getGameStateName(getCurrentState().getState(), getCurrentState().getSubState()));

            // 读取页面结构
            Layout layout = loadGameLayout();
            if (layout == null)
            {
                LogUtils.error(SceneStack.class, "updateGameState 读取页面结构失败");
                return false;
            }
            else
            {
                LogUtils.debug(SceneStack.class, "updateGameState 读取页面结构成功");
            }

            // 读取页面配置
            JsonEntity configJson = loadGameConfig();
            if (configJson == null)
            {
                LogUtils.error(SceneStack.class, "updateGameState 读取页面配置失败");
                return false;
            }
            else
            {
                LogUtils.debug(SceneStack.class, "updateGameState 读取页面配置成功");
            }

            // 更新游戏渲染机
            if (!updateGameRender(layout, configJson))
            {
                LogUtils.error(SceneStack.class, "updateGameState 更新游戏渲染机失败");
                return false;
            }
            else
            {
                LogUtils.debug(SceneStack.class, "updateGameState 更新游戏渲染机成功");
            }

            // 显示状态栈
            LogUtils.debug(SceneStack.class, "updateGameState 状态栈 (stateStack): " + stateStack);

            // debug显示成功
            LogUtils.debug(SceneStack.class, "updateGameState 页面更新成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "updateGameState", e);
            return false;
        }
    }

    /**
     * 推入新状态到状态栈并更新
     *
     * @param newState 新状态结构
     */
    public void pushGameState (StateStructure newState)
    {
        stateStack.push(newState);

        // 调用更新
        if (!updateGameState())
        {
            LogUtils.error(SceneStack.class, "pushGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!");
            throw new RuntimeException("游戏出现致命错误，请将崩溃报告导出并求助!");
        }

        LogUtils.debug(SceneStack.class, "pushGameState 状态 (stateStructure): " + getCurrentState());
    }

    /**
     * 弹出栈顶状态并更新（栈空时重置到菜单）
     */
    public void popGameState ()
    {
        try
        {
            // 判断栈是否为空
            if (!stateStack.empty() && stateStack.size() > 1)
            {
                stateStack.pop();

                if (!updateGameState())
                {
                    LogUtils.error(SceneStack.class, "popGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!");
                    CrashUtils.crash(new RuntimeException("popGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!"));
                }
            }
            else
            {
                if (!resetGameState())
                {
                    LogUtils.error(SceneStack.class, "popGameState 栈为空，重置游戏状态失败");
                }
                else
                {
                    LogUtils.error(SceneStack.class, "popGameState 栈为空，已重置游戏状态");
                }
            }

            LogUtils.debug(SceneStack.class, "popGameState 状态 (stateStructure): " + getCurrentState());
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "popGameState", e);
            CrashUtils.crash(e);
        }
    }

    /**
     * 设置新状态（清空栈后推入）并更新
     *
     * @param newState 新状态结构
     */
    public void setGameState (StateStructure newState)
    {
        try
        {
            stateStack.clear();
            stateStack.push(newState);

            if (!updateGameState())
            {
                LogUtils.error(SceneStack.class, "setGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!");
                CrashUtils.crash(new RuntimeException("setGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!"));
            }

            LogUtils.debug(SceneStack.class, "setGameState 状态 (stateStructure): " + getCurrentState());
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "setGameState", e);
            CrashUtils.crash(e);
        }
    }

    /**
     * 重置状态到主菜单并更新
     *
     * @return 是否重置成功
     */
    public boolean resetGameState ()
    {
        try
        {
            stateStack.clear();
            stateStack.push(new StateStructure(GameState.MENU, 0));

            if (!updateGameState())
            {
                LogUtils.error(SceneStack.class, "resetGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!");
                CrashUtils.crash(new RuntimeException("resetGameState 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!"));
            }

            LogUtils.debug(SceneStack.class, "resetGameState 状态 (stateStructure): " + getCurrentState());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "resetGameState", e);
            CrashUtils.crash(e);
            return false;
        }
    }

    /**
     * 获取当前状态结构
     *
     * @return 当前状态结构
     */
    public StateStructure getStateStructure ()
    {
        return getCurrentState();
    }

    // ===================================================================================================================

    /**
     * 获取当前栈顶状态，栈永不为空（初始化时已推入默认状态）
     *
     * @return 当前栈顶状态结构
     */
    private StateStructure getCurrentState ()
    {
        return stateStack.peek();
    }

    // ===================================================================================================================

    private Layout loadGameLayout ()
    {
        try
        {
            Layout newLayout = null;
            FileHandle layoutFilePathHandle = null;
            FileHandle resourceRootPathHandle = null;

            // 获取当前状态结构
            StateStructure currentState = getCurrentState();
            int state = currentState.getState();
            int subState = currentState.getSubState();
            boolean isInGame = state == GameState.GAME;

            // 设置加载到哪个管理器theme,auid,graphics
            ThemeManager usedThemeManager = isInGame ? playLocalData.getThemeManager() : themeManager;

            // 判断以及获取游戏页面结构路径
            boolean isNeedLayout = true;
            if (GameStatePageInfo.GAME_STATE_LAYOUT_MAP.containsKey(state))
            {
                // 获取游戏子页面结构
                Map<Integer, String> subStateMap = GameStatePageInfo.GAME_STATE_LAYOUT_MAP.get(state);
                // 主页面结构如果不需要
                if (subStateMap == null)
                {
                    isNeedLayout = false;
                    newLayout = new Layout();
                    LogUtils.debug(SceneStack.class, "updateGameLayout 不需要页面结构 (state): " + state + " (subState): " + subState);
                }
                // 获取游戏页面结构
                else if (subStateMap.containsKey(subState))
                {
                    // 页面结构文件名为 null 表示该状态不需要布局
                    if (subStateMap.get(subState) == null)
                    {
                        isNeedLayout = false;
                        newLayout = new Layout();
                        LogUtils.debug(SceneStack.class, "updateGameLayout 不需要页面结构 (state): " + state + " (subState): " + subState);
                    }
                    // 游戏页面结构存在于游戏子目录
                    else if (isInGame)
                    {
                        layoutFilePathHandle = usedThemeManager.getPathHandle()
                            .child(PathName.IN_GAME_ASSET_S_PAGE)
                            .child(subStateMap.get(subState))
                            .child(FileName.IN_GAME_PAGE_LAYOUT);
                        resourceRootPathHandle = usedThemeManager.getPathHandle();
                    }
                    // 启动器页面结构存在固定目录
                    else
                    {
                        layoutFilePathHandle = usedThemeManager.getPathHandle()
                            .child(PathName.ASSET_S_PAGE)
                            .child(subStateMap.get(subState))
                            .child(FileName.PAGE_LAYOUT);
                        resourceRootPathHandle = usedThemeManager.getPathHandle();
                    }
                }
                // 子页面结构未知
                else
                {
                    LogUtils.error(SceneStack.class, "updateGameLayout 未定义的子页面结构 (state): " + state + " (subState): " + subState);
                }
            }
            else
            {
                LogUtils.error(SceneStack.class, "updateGameLayout 未定义的游戏页面结构 (state): " + state);
            }

            // 加载游戏页面结构
            if (layoutFilePathHandle != null)
            {
                // 第一次进游戏需要强制重读
                boolean isFirstInGame = isInGame && !this.isInGame;
                this.isInGame = isInGame;

                // 读取缓存机制
                newLayout = layoutManager.loadLayout(layoutFilePathHandle, resourceRootPathHandle, isFirstInGame);
                LogUtils.debug(SceneStack.class, "updateGameLayout 加载页面结构成功 (file): " + layoutFilePathHandle);
                LogUtils.debug(SceneStack.class, "updateGameLayout 加载页面结构成功 (resourceRoot): " + resourceRootPathHandle);
            }
            else if (isNeedLayout)
            {
                LogUtils.error(SceneStack.class, "updateGameLayout 获取页面结构失败 null值");
            }

            return newLayout;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "updateGameLayout", e);
            return null;
        }
    }

    private JsonEntity loadGameConfig ()
    {
        try
        {
            JsonEntity newConfig = null;
            FileHandle configFilePathHandle = null;

            // 获取当前状态结构
            StateStructure currentState = getCurrentState();
            int state = currentState.getState();
            int subState = currentState.getSubState();
            boolean isInGame = state == GameState.GAME;

            // 设置加载到哪个管理器
            ThemeManager usedThemeManager = isInGame ? playLocalData.getThemeManager() : themeManager;

            // 判断以及获取游戏页面配置路径
            boolean isNeedConfig = true;
            if (GameStatePageInfo.GAME_STATE_CONFIG_MAP.containsKey(state))
            {
                // 获取游戏子页面配置
                Map<Integer, Boolean> subStateMap = GameStatePageInfo.GAME_STATE_CONFIG_MAP.get(state);
                // 主页面结构如果不需要
                if (subStateMap == null)
                {
                    isNeedConfig = false;
                    newConfig = new JsonEntity();
                    LogUtils.debug(SceneStack.class, "loadGameConfig 不需要页面配置 (state): " + state + " (subState): " + subState);
                }
                // 获取游戏页面配置
                else if (subStateMap.containsKey(subState))
                {
                    // 页面配置为 null 或 false 表示该状态不需要配置
                    Boolean needConfig = subStateMap.get(subState);
                    if (needConfig == null || !needConfig)
                    {
                        isNeedConfig = false;
                        newConfig = new JsonEntity();
                        LogUtils.debug(SceneStack.class, "loadGameConfig 不需要页面配置 (state): " + state + " (subState): " + subState);
                    }
                    // 需要配置，从 GAME_STATE_LAYOUT_MAP 获取页面目录名
                    else
                    {
                        String pageDirName = null;
                        Map<Integer, String> layoutSubStateMap = GameStatePageInfo.GAME_STATE_LAYOUT_MAP.get(state);
                        if (layoutSubStateMap != null && layoutSubStateMap.containsKey(subState))
                        {
                            pageDirName = layoutSubStateMap.get(subState);
                        }

                        if (pageDirName != null)
                        {
                            // 游戏页面配置存在于游戏子目录
                            if (isInGame)
                            {
                                configFilePathHandle = usedThemeManager.getPathHandle()
                                    .child(PathName.IN_GAME_ASSET_S_PAGE)
                                    .child(pageDirName)
                                    .child(FileName.IN_GAME_PAGE_CONFIG);
                            }
                            // 启动器页面配置存在固定目录
                            else
                            {
                                configFilePathHandle = usedThemeManager.getPathHandle()
                                    .child(PathName.ASSET_S_PAGE)
                                    .child(pageDirName)
                                    .child(FileName.PAGE_CONFIG);
                            }
                        }
                    }
                }
                // 子页面配置未知
                else
                {
                    LogUtils.error(SceneStack.class, "loadGameConfig 未定义的子页面配置 (state): " + state + " (subState): " + subState);
                }
            }
            else
            {
                LogUtils.error(SceneStack.class, "loadGameConfig 未定义的页面配置 (state): " + state);
            }

            // 加载页面配置
            if (configFilePathHandle != null)
            {
                if (FileUtils.isFileExist(configFilePathHandle))
                {
                    newConfig = new JsonEntity(configFilePathHandle);
                    LogUtils.debug(SceneStack.class, "loadGameConfig 加载页面配置成功 (file): " + configFilePathHandle);
                }
                else
                {
                    // 定义了配置但文件不存在，使用空配置避免 null
                    newConfig = new JsonEntity();
                    LogUtils.debug(SceneStack.class, "loadGameConfig 页面配置不存在，使用空配置 (file): " + configFilePathHandle);
                }
            }
            else if (isNeedConfig)
            {
                LogUtils.error(SceneStack.class, "loadGameConfig 获取页面配置失败 null值");
            }

            // 确保不返回 null（空对象代表"无配置"）
            if (newConfig == null)
            {
                newConfig = new JsonEntity();
            }

            return newConfig;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "loadGameConfig", e);
            return null;
        }
    }

    private boolean updateGameRender (Layout layout, JsonEntity configJson)
    {
        try
        {
            LogUtils.debug(SceneStack.class, "updateGameRender 状态更新开始");

            // 渲染器销毁旧游戏渲染机
            if (!renderPipeline.clear())
                return false;
            LogUtils.debug(SceneStack.class, "updateGameRender 渲染器销毁旧游戏渲染机成功");

            // 整合游戏页面配置数据
            GameStateDataContainer gameStateDataContainer = new GameStateDataContainer(getCurrentState(), layout, configJson);
            LogUtils.debug(SceneStack.class, "updateGameRender 整合游戏页面配置数据成功");

            // 渲染器更新游戏渲染机
            if (!renderPipeline.update(gameStateDataContainer))
                return false;
            LogUtils.debug(SceneStack.class, "updateGameRender 渲染器更新游戏渲染机成功");

            LogUtils.debug(SceneStack.class, "updateGameRender 状态更新成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "updateGameRender 状态更新失败，游戏出现致命错误，请将崩溃报告导出并求助!", e);
            CrashUtils.crash(e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 销毁状态服务，清空引用
     *
     * @return 是否销毁成功
     */
    public boolean dispose ()
    {
        try
        {
            themeManager = null;
            layoutManager = null;

            playLocalData = null;
            renderPipeline = null;

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(SceneStack.class, "dispose 销毁游戏状态服务异常", e);
            return false;
        }
    }
}
