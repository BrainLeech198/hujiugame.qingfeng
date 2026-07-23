package com.hujiugame.qingfeng.game;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.manager.LayoutManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GameTemplateManager
{
    private ThemeManager themeManager;
    private LayoutManager layoutManager;

    private FileHandle templateDirectoryPathHandle;

    private final Map<String, Layout> layoutConfigMap = new HashMap<>();
    private Set<String> availableTemplates = Collections.emptySet();

    /**
     * 加载模板配置文件（template_config.json），获取可用模板列表
     *
     * @param templateDirectoryPathHandle 模板目录路径句柄
     */
    private void loadTemplateConfig (FileHandle templateDirectoryPathHandle)
    {
        FileHandle configPathHandle = templateDirectoryPathHandle.child(FileName.IN_GAME_STORY_TEMPLATE_DICTIONARY_CONFIG);
        if (FileUtils.isFileExist(configPathHandle))
        {
            JsonEntity config = new JsonEntity(configPathHandle);
            if (config.containsKey("templates"))
            {
                availableTemplates = new HashSet<>(config.getStringList("templates"));
                LogUtils.debug(GameTemplateManager.class, "loadTemplateConfig 读取可用模板 (templates): " + availableTemplates);
                return;
            }
        }
        // 无配置文件 → 空集合，后续通过扫描目录加载（向后兼容）
        availableTemplates = Collections.emptySet();
        LogUtils.debug(GameTemplateManager.class, "loadTemplateConfig 模板配置文件不存在，将扫描全部文件");
    }

    /**
     * 加载单个模板文件
     *
     * @param templateFile  模板文件
     * @param layoutManager 布局管理器
     */
    private void loadSingleTemplate (FileHandle templateFile, LayoutManager layoutManager)
    {
        Layout templateLayout = layoutManager.loadLayout(templateFile, themeManager.getPathHandle(), true);

        if (templateLayout != null)
        {
            layoutConfigMap.put(templateLayout.getName(), templateLayout);
            LogUtils.debug(GameTemplateManager.class, "loadSingleTemplate 加载模板 (name): " + templateLayout.getName() + " (json): " + templateLayout.getJson());
        }
        else
        {
            LogUtils.error(GameTemplateManager.class, "loadSingleTemplate 模板数据错误 (path): " + templateFile);
        }
    }

    /**
     * 加载模板数据
     *
     * @param gameDirectoryPathHandle 游戏路径句柄
     * @param layoutManager           布局管理器
     * @return 加载是否成功
     */
    private boolean loadTemplateData (FileHandle gameDirectoryPathHandle, LayoutManager layoutManager)
    {
        try
        {
            templateDirectoryPathHandle = gameDirectoryPathHandle.child(PathName.IN_GAME_ASSET_S_STORY_TEMPLATE);

            if (!FileUtils.isDirectoryExist(templateDirectoryPathHandle))
            {
                LogUtils.debug(GameTemplateManager.class, "loadTemplateData 模板目录不存在 (path): " + templateDirectoryPathHandle);
                return false;
            }

            // 加载模板配置
            loadTemplateConfig(templateDirectoryPathHandle);

            if (!availableTemplates.isEmpty())
            {
                // 有配置文件：只加载清单中的模板
                for (String templateFileName : availableTemplates)
                {
                    FileHandle templateFilePathHandle = templateDirectoryPathHandle.child(templateFileName);
                    if (FileUtils.isFileExist(templateFilePathHandle))
                    {
                        loadSingleTemplate(templateFilePathHandle, layoutManager);
                    }
                    else
                    {
                        LogUtils.error(GameTemplateManager.class, "loadTemplateData 模板文件不存在 (path): " + templateFilePathHandle);
                    }
                }
            }

            FileHandle[] templateFiles = FileUtils.getList(templateDirectoryPathHandle);
            if (templateFiles != null)
            {
                // 无配置文件：扫描目录全部加载（向后兼容）
                for (FileHandle templateFile : templateFiles)
                {
                    if (templateFile.name().endsWith(".json"))
                    {
                        loadSingleTemplate(templateFile, layoutManager);
                    }
                }
            }

            LogUtils.debug(GameTemplateManager.class, "loadTemplateData 模板数据缓存 (map): " + layoutConfigMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameTemplateManager.class, "loadTemplateData", e);
            return false;
        }
    }

    /**
     * 初始化模板管理器，加载模板数据
     */
    public boolean init (FileHandle gameDirectoryPathHandle, ThemeManager themeManager, LayoutManager layoutManager)
    {
        try
        {
            this.themeManager = themeManager;
            this.layoutManager = layoutManager;

            if (!loadTemplateData(gameDirectoryPathHandle, layoutManager))
            {
                LogUtils.error(GameTemplateManager.class, "init 模板数据加载失败");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameTemplateManager.class, "init", e);
            return false;
        }
    }

    /**
     * 获取可用模板列表
     */
    public Set<String> getAvailableTemplates ()
    {
        return availableTemplates;
    }

    /**
     * 根据模板名称获取模板的深拷贝副本
     */
    public Layout getTemplate (String templateName)
    {
        try
        {
            if (templateName == null || templateName.isEmpty()) return new Layout();

            // 有配置文件时，自动检测未在清单中但实际存在的模板
            if (!availableTemplates.isEmpty() && !availableTemplates.contains(templateName))
            {
                FileHandle probePathHandle = templateDirectoryPathHandle.child(templateName);
                if (!FileUtils.isFileExist(probePathHandle))
                {
                    probePathHandle = templateDirectoryPathHandle.child(templateName + ".json");
                }
                if (FileUtils.isFileExist(probePathHandle))
                {
                    availableTemplates.add(templateName);
                    LogUtils.debug(GameTemplateManager.class, "getTemplate 自动发现并加载模板 (template): " + templateName);
                    loadSingleTemplate(probePathHandle, layoutManager);
                }
                else
                {
                    LogUtils.error(GameTemplateManager.class, "getTemplate 未知的模板 (template): " + templateName + " (available): " + availableTemplates);
                    return null;
                }
            }

            return new Layout(layoutConfigMap.get(templateName));
        }
        catch (Exception e)
        {
            LogUtils.error(GameTemplateManager.class, "getTemplate", e);
            return null;
        }
    }

    /**
     * 释放模板管理器资源
     */
    public boolean dispose ()
    {
        try
        {
            this.themeManager = null;
            this.layoutManager = null;
            layoutConfigMap.clear();
            availableTemplates = Collections.emptySet();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameTemplateManager.class, "dispose", e);
            return false;
        }
    }
}
