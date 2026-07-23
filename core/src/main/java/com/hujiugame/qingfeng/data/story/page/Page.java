package com.hujiugame.qingfeng.data.story.page;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public class Page
{
    // valid
    private boolean valid = false;

    private String id;
    private Layout layout;
    private PageBehavior pageBehavior;

    /**
     * 创建页面实例并加载布局
     *
     * @param id             页面 ID
     * @param pagePathHandle 页面路径句柄
     * @param layoutManager  布局管理器
     * @param themeManager   主题管理器
     */
    public Page(String id, FileHandle pagePathHandle, LayoutManager layoutManager, ThemeManager themeManager)
    {
        try
        {
            valid = true;
            this.id = id;

            if (!loadLayout(pagePathHandle, layoutManager, themeManager))
            {
                valid = false;
                LogUtils.error(Page.class, "loadLayout 加载页面布局失败");
                return;
            }

            if (!loadPageBehavior(pagePathHandle))
            {
                valid = false;
                LogUtils.error(Page.class, "loadPageBehavior 加载页面行为失败");
                return;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(Page.class, "loadLayout", e);
            valid = false;
        }
    }

    /**
     * 加载页面布局配置
     *
     * @param pagePathHandle 页面路径句柄
     * @param layoutManager  布局管理器
     * @param themeManager   主题管理器
     * @return 加载成功返回 true
     */
    private boolean loadLayout (FileHandle pagePathHandle, LayoutManager layoutManager, ThemeManager themeManager)
    {
        try
        {
            FileHandle layoutFilePathHandle = pagePathHandle.child(FileName.IN_GAME_STORY_S_ROLE_PAGE_S_LAYOUT);
            if (!FileUtils.isFileExist(layoutFilePathHandle))
            {
                LogUtils.error(Page.class, "loadLayout 角色页面布局不存在 (path): " + layoutFilePathHandle);
            }

            this.layout = layoutManager.loadLayout(layoutFilePathHandle, themeManager.getPathHandle(), false);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(Page.class, "loadLayout", e);
            return false;
        }
    }

    /**
     * 加载页面行为配置
     *
     * @param pagePathHandle 页面路径句柄
     * @return 加载成功返回 true
     */
    private boolean loadPageBehavior (FileHandle pagePathHandle)
    {
        try
        {
            FileHandle behaviorFilePathHandle = pagePathHandle.child(FileName.IN_GAME_STORY_S_ROLE_PAGE_S_BEHAVIOR);
            if (!FileUtils.isFileExist(behaviorFilePathHandle))
            {
                LogUtils.debug(Page.class, "loadPageBehavior 角色页面未定义行为 创建占位行为");
                this.pageBehavior = new PageBehavior();
                return true;
            }

            JsonEntity pageBehaviorJson = new JsonEntity(behaviorFilePathHandle);
            this.pageBehavior = new PageBehavior(pageBehaviorJson);
            if (!this.pageBehavior.isValid())
            {
                LogUtils.error(Page.class, "loadPageBehavior 角色页面行为加载失败");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(Page.class, "loadPageBehavior", e);
            return false;
        }
    }

    /**
     * 页面是否有效
     */
    public boolean isValid ()
    {
        return valid;
    }

    /**
     * 获取布局配置
     */
    public Layout getLayoutConfig ()
    {
        return layout;
    }

    /**
     * 获取页面行为
     */
    public PageBehavior getPageBehavior ()
    {
        return pageBehavior;
    }

    /**
     * 获取页面 ID
     */
    public String getId ()
    {
        return id;
    }

    /**
     * 返回页面的字符串表示
     */
    @Override
    public String toString ()
    {
        return "Page{" +
                "id='" + id + '\'' +
                ", layout=" + layout +
                ", pageBehavior=" + pageBehavior +
                '}';
    }
}
