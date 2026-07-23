package com.hujiugame.qingfeng.scene.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.core.GameHost;
import com.hujiugame.qingfeng.core.UpdateChecker;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.type.key.RequirementConfigKey;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.type.key.RequirementLanguageKey;
import com.hujiugame.qingfeng.type.key.RequirementUiKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPopGameState;
import com.hujiugame.qingfeng.util.interact.FileChooser;
import com.hujiugame.qingfeng.util.interact.FileExplorer;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class MenuList implements GameRender
{
    private final UpdateChecker updateChecker;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final EventQueue eventQueue;
    private final GameHost gameHost;
    private final String rootPath;
    private GameStateDataContainer gameStateDataContainer;

    private String gameListAbsolutePath;
    private String gameListPath;

    private int pageMaxGame = 8;
    private int nowPage = 1;
    private int maxPage = 1;

    private int headIndex = -1;
    private int tailIndex = -1;
    private int selectedIndex = -1;

    private final String unSelectFrameTag = RequirementUiKey.MENU_LIST_IMAGE_UNSELECT_FRAME;
    private final String selectFrameTag = RequirementUiKey.MENU_LIST_IMAGE_SELECT_FRAME;
    private final String gameCoverTag = RequirementUiKey.MENU_LIST_IMAGE_GAME_COVER;
    private final String gameCoverKind = RequirementUiKey.MENU_LIST_IMAGE_GAME_COVER_KIND;

    private final List<String> gamePathNameList = new LinkedList<>();
    private String selectedGamePath = "";
    private FileHandle selectedGamePathDirectory;
    private String selectedGameName = "";
    private String selectedGameLauncherVersion = "";

    // ===================================================================================================================

    public MenuList (UpdateChecker updateChecker, AudioManager audioManager,
                     GraphicsManager graphicsManager, UiManager uiManager,
                     EventQueue eventQueue,
                     GameHost gameHost, String rootPath)
    {
        this.updateChecker = updateChecker;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.uiManager = uiManager;
        this.eventQueue = eventQueue;
        this.gameHost = gameHost;
        this.rootPath = rootPath;
    }

    // ===================================================================================================================

    private void checkGameList ()
    {
        // 获取游戏目录下的文件夹列表
        FileHandle[] gamePathList = FileUtils.getList(Gdx.files.external(gameListPath));

        // 遍历
        gamePathNameList.clear();
        for (FileHandle gamePathFileHandle : gamePathList)
        {
            // 检测并添加到列表
            if (gameHost.getGameLogicService().checkGameDirectory(gamePathFileHandle))
            {
                gamePathNameList.add(gamePathFileHandle.path());
            }
        }

        // 显示列表
        LogUtils.debug(MenuList.class, "checkGameList 获取游戏列表 (list): " + gamePathNameList);
    }

    private void calculatePage ()
    {
        // 计算最大页数
        maxPage = (
            gamePathNameList.size() / pageMaxGame
                + (gamePathNameList.size() % pageMaxGame == 0 ? 0 : 1));

        // 计算指引
        headIndex = (nowPage - 1) * pageMaxGame;
        tailIndex = Math.min(headIndex + pageMaxGame, gamePathNameList.size()) - 1;

        // 防止页数为0
        if (maxPage == 0)
        {
            maxPage = 1;
        }
    }

    private void refreshSelectFrame ()
    {
        for (int i = 0; i < pageMaxGame; i++)
        {
            // 如果选中
            if (i == selectedIndex)
            {
                // 显示选中
                uiManager.showImage(selectFrameTag + i);
                uiManager.hideImage(unSelectFrameTag + i);
            }
            else
            {
                // 显示未选中
                uiManager.hideImage(selectFrameTag + i);
                if (i <= tailIndex - headIndex)
                    uiManager.showImage(unSelectFrameTag + i);
                else
                    uiManager.hideImage(unSelectFrameTag + i);
            }
        }
    }

    private void refreshGameCover ()
    {
        //
        for (int i = 0; i < pageMaxGame; i++)
        {
            // 全局索引
            int globalIndex = headIndex + i;

            // 图片标签
            String tag = gameCoverTag + i;
            String kind = gameCoverKind + i;

            // 显示存在的image
            if (globalIndex <= tailIndex)
            {
                // 载入image
                FileHandle file = gameHost.getGameLogicService().parseGameIcon(Gdx.files.external(gamePathNameList.get(globalIndex)));
                uiManager.loadImageKind(kind, file);

                // 更新image
                ImageInfo info = gameStateDataContainer.getLayoutConfig().getImageMap().get(tag);
                uiManager.updateImage(tag, kind, info.getX(), info.getY(), info.getWidth(), info.getHeight());
                uiManager.showImage(tag);
            }
            else
            {
                // 隐藏image
                uiManager.hideImage(tag);
            }
        }
    }

    private void refreshUi ()
    {
        // 功能按键
        if (selectedIndex == -1)
        {
            // 隐藏功能按键
            uiManager.hideButton(RequirementUiKey.MENU_LIST_BUTTON_PROFILE);
            uiManager.hideButton(RequirementUiKey.MENU_LIST_BUTTON_SHARE);
            uiManager.hideButton(RequirementUiKey.MENU_LIST_BUTTON_DELETE);
            uiManager.hideLabel(RequirementUiKey.MENU_LIST_LABEL_SELECTED_PATH);
            uiManager.showLabel(RequirementUiKey.MENU_LIST_LABEL_ABSOLUTE_PATH);
        }
        else
        {
            // 显示功能按键
            uiManager.showButton(RequirementUiKey.MENU_LIST_BUTTON_PROFILE);
            uiManager.showButton(RequirementUiKey.MENU_LIST_BUTTON_SHARE);
            uiManager.showButton(RequirementUiKey.MENU_LIST_BUTTON_DELETE);
            uiManager.showLabel(RequirementUiKey.MENU_LIST_LABEL_SELECTED_PATH);
            uiManager.hideLabel(RequirementUiKey.MENU_LIST_LABEL_ABSOLUTE_PATH);
        }

        // 设置页数显示
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_NOW_PAGE, nowPage);
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_MAX_PAGE, maxPage);

        // 判断是否单独一页
        if (maxPage == 1)
        {
            uiManager.hideLabel(RequirementUiKey.MENU_LIST_LABEL_PAGE);
            uiManager.hideButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_NEXT_PAGE);
            uiManager.hideButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_LAST_PAGE);
        }
        else
        {
            uiManager.showLabel(RequirementUiKey.MENU_LIST_LABEL_PAGE);
            uiManager.showButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_NEXT_PAGE);
            uiManager.showButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_LAST_PAGE);
        }

        // 判断翻页情况
        // 第一页
        if (nowPage == 1)
        {
            uiManager.disableButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_LAST_PAGE);
        }
        else
        {
            uiManager.enableButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_LAST_PAGE);
        }

        // 最后一页
        if (nowPage == maxPage)
        {
            uiManager.disableButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_NEXT_PAGE);
        }
        else
        {
            uiManager.enableButton(RequirementUiKey.MENU_LIST_BUTTON_SELECT_NEXT_PAGE);
        }
    }

    private void refreshPage ()
    {
        // 计算页数
        calculatePage();

        // 刷新选中框
        refreshSelectFrame();

        // 刷新游戏封面
        refreshGameCover();

        // 刷新ui
        refreshUi();
    }

    private void resetSelected ()
    {
        // 重置
        selectedIndex = -1;
        selectedGamePath = "";
        selectedGamePathDirectory = null;
        selectedGameName = "";
        selectedGameLauncherVersion = "";
        refreshPage();
    }

    private void setSelected (int relativeIndex)
    {
        // 选中的索引 绝对索引 文件路径
        selectedIndex = relativeIndex;
        selectedGamePath = gamePathNameList.get(headIndex + relativeIndex);
        selectedGamePathDirectory = Gdx.files.external(selectedGamePath);

        // 解析游戏名称和版本
        JsonEntity gameConfigJson = gameHost.getGameLogicService().parseGameConfig(selectedGamePathDirectory);
        selectedGameName = gameHost.getGameLogicService().parseGameName(gameConfigJson);
        selectedGameLauncherVersion = gameHost.getGameLogicService().parseGameLauncherVersion(gameConfigJson);

        // 存储数据
        String selectedGamePathAbsolutePath = Gdx.files.external(selectedGamePath).file().getAbsolutePath();
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_SELECTED_PATH, selectedGamePathAbsolutePath);// 修正显示为绝对路径，方便查找
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_SELECTED_NAME, selectedGameName);
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_SELECTED_LAUNCHER_VERSION, selectedGameLauncherVersion);

        refreshPage();
        LogUtils.info(MenuList.class, "detectClickGameCover 选中游戏 (path): " + selectedGamePath);
    }

    // ===================================================================================================================

    private void lastPage ()
    {
        if (nowPage > 1)
        {
            nowPage--;
            resetSelected();

            LogUtils.debug(MenuList.class, "lastPage 翻上页: " + nowPage);
        }
    }

    private void nextPage ()
    {
        if (nowPage < maxPage)
        {
            nowPage++;
            resetSelected();

            LogUtils.debug(MenuList.class, "nextPage 翻下页: " + nowPage);
        }
    }

    // ===================================================================================================================

    private void loadGame ()
    {
        if (gameHost.getGameSessionManager().loadGame(Gdx.files.external(selectedGamePath)))
        {
            LogUtils.debug(MenuList.class, "loadGame 成功载入游戏 (path): " + selectedGamePath);
        }
        else
        {
            LogUtils.error(MenuList.class, "loadGame 载入游戏失败 (path): " + selectedGamePath);
        }
    }

    private void judgeGame ()
    {
        // 判断版本是否一致
        if (!updateChecker.doMinorCompatible(selectedGameLauncherVersion))
        {
            uiManager.getMessageBox().showInfo(RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_KEY,
                "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_TITLE + "}",
                "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_1 + "}"
                    + "{game$" + GameInfoKey.GAME_LIST_SELECTED_NAME + "}"
                    + "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_2 + "}"
                    + "{game$" + GameInfoKey.GAME_LIST_SELECTED_LAUNCHER_VERSION + "}"
                    + "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_3 + "}"
                    + "{game$" + GameInfoKey.LAUNCHER_VERSION + "}"
                    + "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_CONTENT_4 + "}"
            );
        }
    }

    private void functionImport ()
    {
        if (FileChooser.isFileChosen("import_game"))
        {
            // TODO: 实现游戏文件导入功能
            FileHandle file = FileChooser.getChosenFile("import_game");
        }
    }

    private void functions ()
    {
        // 检测文件导入
        functionImport();
    }

    // ===================================================================================================================

    /**
     * 初始化游戏列表，扫描游戏目录并刷新页面
     *
     * @param gameStateDataContainer 游戏状态数据容器
     */
    @Override
    public void init (GameStateDataContainer gameStateDataContainer)
    {
        this.gameStateDataContainer = gameStateDataContainer;

        // config
        if (gameStateDataContainer.getConfigJson().containsKey(RequirementConfigKey.MENU_LIST_PAGE_MAX_GAME))
        {
            pageMaxGame = gameStateDataContainer.getConfigJson().getInt(RequirementConfigKey.MENU_LIST_PAGE_MAX_GAME);
        }

        // ui
        uiManager.addLayout(gameStateDataContainer.getLayoutConfig());

        // 获取游戏列表路径
        gameListAbsolutePath = FileUtils.pathJoin(rootPath, PathName.BASE, PathName.GAME);
        gameListPath = FileUtils.pathJoin(PathName.BASE, PathName.GAME);
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_ABSOLUTE_PATH, gameListAbsolutePath);
        gameHost.getGameInfoManager().putInfo(GameInfoKey.GAME_LIST_SELECTED_PATH, "");

        // 检查游戏列表
        checkGameList();

        // 刷新页面
        refreshPage();
    }

    /**
     * 处理游戏封面点击、翻页、路径标签点击和文件导入
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 功能列表
        functions();

        // 检测按下封面
        if (!gamePathNameList.isEmpty())
        {
            for (int i = 0; i <= tailIndex - headIndex; i++)
            {
                if (uiManager.isImageClicked(gameCoverTag + i))
                {
                    // 再次选中
                    if (selectedIndex == i)
                    {
                        loadGame();
                    }
                    else
                    {
                        setSelected(i);
                        judgeGame();
                    }
                }
            }
        }

        // 按下返回按钮
        if (uiManager.isButtonClicked(RequirementUiKey.MENU_LIST_BUTTON_BACK))
        {
            eventQueue.addEvent(new EventPopGameState());
        }

        // 点击路径标签
        if (uiManager.isLabelClicked(RequirementUiKey.MENU_LIST_LABEL_ABSOLUTE_PATH))
        {
            FileHandle gameListPathHandle = Objects.requireNonNull(Gdx.files.external(gameListPath));
            FileExplorer.showInExplorer(gameListPathHandle);
        }
        else if (uiManager.isLabelClicked(RequirementUiKey.MENU_LIST_LABEL_SELECTED_PATH))
        {
            FileHandle selectedGamePathHandle = Objects.requireNonNull(Gdx.files.external(selectedGamePath));
            FileExplorer.showInExplorer(selectedGamePathHandle);
        }

        // 功能按钮
        if (uiManager.isButtonClicked(RequirementUiKey.MENU_LIST_BUTTON_IMPORT))
        {
            FileChooser.createFileChooser("import_game");
            FileChooser.showFileChooser("import_game", "选择游戏", null, FileChooser.EXT_GAME);
        }

        // 按下上一页按钮
        else if (uiManager.isButtonClicked(RequirementUiKey.MENU_LIST_BUTTON_SELECT_LAST_PAGE))
        {
            lastPage();
        }
        // 按下下一页按钮
        else if (uiManager.isButtonClicked(RequirementUiKey.MENU_LIST_BUTTON_SELECT_NEXT_PAGE))
        {
            nextPage();
        }

        // 检测弹窗返回值
        uiManager.getMessageBox().handleInfo(RequirementLanguageKey.MESSAGE_BOX_GAME_VERSION_DIFFERENT_KEY, () ->
        {
        });
    }

    /**
     * 渲染游戏列表布局和音频
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        audioManager.playLayout(gameStateDataContainer.getLayoutConfig());
        graphicsManager.putLayout(gameStateDataContainer.getLayoutConfig(), deltaTime);
    }

    /**
     * 释放游戏列表资源，清空游戏路径列表
     */
    @Override
    public void dispose ()
    {
        gameListAbsolutePath = null;
        gameListPath = null;
        gamePathNameList.clear();

        resetSelected();

        uiManager.deleteLayout(gameStateDataContainer.getLayoutConfig());
    }

}
