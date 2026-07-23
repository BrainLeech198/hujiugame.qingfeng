package com.hujiugame.qingfeng.scene.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.core.UpdateChecker;
import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.game.state.GameState;
import com.hujiugame.qingfeng.type.game.state.GameSubState;
import com.hujiugame.qingfeng.type.key.RequirementLanguageKey;
import com.hujiugame.qingfeng.type.key.RequirementUiKey;
import com.hujiugame.qingfeng.type.url.WebSite;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.event.EventQueue;
import com.hujiugame.qingfeng.event.imp.EventPushGameState;
import com.hujiugame.qingfeng.util.system.FileUtils;

public final class MenuMain implements GameRender
{
    private final UpdateChecker updateChecker;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private final ThemeManager themeManager;
    private final UiManager uiManager;
    private final EventQueue eventQueue;
    private GameStateDataContainer gameStateDataContainer;

    public MenuMain (UpdateChecker updateChecker, AudioManager audioManager,
                     GraphicsManager graphicsManager, ThemeManager themeManager,
                     UiManager uiManager,
                     EventQueue eventQueue)
    {
        this.updateChecker = updateChecker;
        this.audioManager = audioManager;
        this.graphicsManager = graphicsManager;
        this.themeManager = themeManager;
        this.uiManager = uiManager;
        this.eventQueue = eventQueue;
    }

    /**
     * 初始化主菜单布局，缓存背景图和网站图标
     *
     * @param gameStateDataContainer 游戏状态数据容器
     */
    @Override
    public void init (GameStateDataContainer gameStateDataContainer)
    {
        this.gameStateDataContainer = gameStateDataContainer;

        // 缓存当前背景图到app_init.png
        FileHandle backgroundPicturePath = themeManager.getPathHandle().child(PathName.ASSET_S_RESOURCE_IMAGE).child(gameStateDataContainer.getLayoutConfig().getBackgroundPicture());
        FileHandle appInitPicturePath = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET_S_RESOURCE_IMAGE, FileName.DEFAULT_SPLASH));
        FileUtils.copyFile(backgroundPicturePath, appInitPicturePath);

        uiManager.addLayout(gameStateDataContainer.getLayoutConfig());
    }

    /**
     * 处理主菜单按钮点击和版本更新检测
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void update (float deltaTime)
    {
        // 点击左下角版本号区域打开官方网站（将屏幕坐标转换到虚拟坐标系）
        if (Gdx.input.justTouched())
        {
            float touchX = Gdx.input.getX() * ((float) ScreenSize.WIDTH / Gdx.graphics.getWidth());
            float touchY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * ((float) ScreenSize.HEIGHT / Gdx.graphics.getHeight());
            if (touchX >= 0 && touchX <= 180 && touchY >= 0 && touchY <= 40)
            {
                uiManager.getMessageBox().showAsk(RequirementLanguageKey.MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_KEY,
                    "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_TITLE + "}",
                    "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_CONTENT + "}");
            }
        }

        // 按下开始按钮
        if (uiManager.isButtonClicked(RequirementUiKey.MENU_MAIN_BUTTON_START))
        {
            eventQueue.addEvent(new EventPushGameState(GameState.MENU, GameSubState.MENU_LIST));
        }
        // 按下创作按钮
        else if (uiManager.isButtonClicked(RequirementUiKey.MENU_MAIN_BUTTON_CREATE))
        {

        }
        // 按下配置按钮
        else if (uiManager.isButtonClicked(RequirementUiKey.MENU_MAIN_BUTTON_CONFIG))
        {
            eventQueue.addEvent(new EventPushGameState(GameState.CONFIG, GameSubState.CONFIG_BASIC));
        }
        // 按下退出按钮
        else if (uiManager.isButtonClicked(RequirementUiKey.MENU_MAIN_BUTTON_QUIT))
        {
            uiManager.getMessageBox().showAsk(RequirementLanguageKey.MESSAGE_BOX_QUIT_GAME_KEY,
                "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_QUIT_GAME_TITLE + "}",
                "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_QUIT_GAME_CONTENT+ "}");
        }

        // 更新检测
        if (updateChecker.doDetectUpdateFinish())
        {
            // 请求成功
            if (updateChecker.doDetectSuccess())
            {
                if (updateChecker.isNeedVersionUpdate())
                {
                    uiManager.getMessageBox().showAsk(RequirementLanguageKey.MESSAGE_BOX_UPDATE_DETECTED_KEY,
                        "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_UPDATE_DETECTED_TITLE + "}",
                        "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_UPDATE_DETECTED_CONTENT + "}");
                }
            }
            // 请求失败
            else
            {
                uiManager.getMessageBox().showAsk(RequirementLanguageKey.MESSAGE_BOX_UPDATE_REQUEST_FAILED_KEY,
                    "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_UPDATE_REQUEST_FAILED_TITLE + "}",
                    "{language$" + RequirementLanguageKey.REQUIREMENT_BLOCK + "#" + RequirementLanguageKey.MESSAGE_BOX_FIRST_KEY + "." + RequirementLanguageKey.MESSAGE_BOX_UPDATE_REQUEST_FAILED_CONTENT + "}");
            }
            // 重置检测更新完成状态
            updateChecker.setDoDetectUpdateFinish(false);
        }

        uiManager.getMessageBox().handleAsk(RequirementLanguageKey.MESSAGE_BOX_OPEN_OFFICIAL_WEBSITE_KEY,
            () -> Gdx.net.openURI(WebSite.OFFICIAL));

        uiManager.getMessageBox().handleAsk(RequirementLanguageKey.MESSAGE_BOX_QUIT_GAME_KEY,
            Gdx.app::exit);

        uiManager.getMessageBox().handleAsk(RequirementLanguageKey.MESSAGE_BOX_UPDATE_DETECTED_KEY,
            () -> Gdx.net.openURI(WebSite.OFFICIAL));

        uiManager.getMessageBox().handleAsk(RequirementLanguageKey.MESSAGE_BOX_UPDATE_REQUEST_FAILED_KEY,
            () -> Gdx.net.openURI(WebSite.OFFICIAL));

    }

    /**
     * 渲染主菜单布局和版本号文字
     *
     * @param deltaTime 距上一帧的时间差
     */
    @Override
    public void render (float deltaTime)
    {
        audioManager.playLayout(gameStateDataContainer.getLayoutConfig());
        graphicsManager.putLayout(gameStateDataContainer.getLayoutConfig(), deltaTime);
        graphicsManager.putText(updateChecker.getDisplayVersionString(), 1.0f, 5, 5);
    }

    /**
     * 释放主菜单布局资源
     */
    @Override
    public void dispose ()
    {
        uiManager.deleteLayout(gameStateDataContainer.getLayoutConfig());
    }
}
