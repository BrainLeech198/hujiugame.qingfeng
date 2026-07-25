package com.hujiugame.qingfeng.game;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.script.data.Script;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.key.ConfigKey;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GameScriptManager
{
    private final Map<String, Script> scriptMap = new HashMap<>();
    private Set<String> availableScripts = Collections.emptySet();

    private FileHandle scriptDirectoryHandle;

    /**
     * 加载脚本配置文件（script_config.json），获取可用脚本列表
     */
    private void loadScriptConfig ()
    {
        FileHandle configPathHandle = scriptDirectoryHandle.child(FileName.IN_GAME_SCRIPT_DICTIONARY_CONFIG);
        if (FileUtils.isFileExist(configPathHandle))
        {
            JsonEntity config = new JsonEntity(configPathHandle);
            if (config.containsKey(ConfigKey.Content.SCRIPTS))
            {
                availableScripts = new HashSet<>(config.getStringList(ConfigKey.Content.SCRIPTS));
                LogUtils.debug(GameScriptManager.class, "loadScriptConfig 读取可用脚本 (" + ConfigKey.Content.SCRIPTS + "): " + availableScripts);
                return;
            }
        }
        availableScripts = Collections.emptySet();
        LogUtils.debug(GameScriptManager.class, "loadScriptConfig 脚本配置文件不存在，将扫描全部文件");
    }

    /**
     * 加载单个脚本文件
     */
    private void loadSingleScript (FileHandle scriptFile)
    {
        Script script = new Script(new JsonEntity(scriptFile));
        if (script.isValid())
        {
            String name = scriptFile.name();
            scriptMap.put(name, script);
            LogUtils.debug(GameScriptManager.class, "loadSingleScript 加载脚本 (file): " + name);
        }
        else
        {
            LogUtils.error(GameScriptManager.class, "loadSingleScript 脚本数据错误 (path): " + scriptFile);
        }
    }

    /**
     * 加载脚本数据
     *
     * @param gameDirectoryPathHandle 游戏目录路径句柄
     * @return 加载是否成功
     */
    private boolean loadScriptData (FileHandle gameDirectoryPathHandle)
    {
        try
        {
            scriptDirectoryHandle = gameDirectoryPathHandle.child(PathName.IN_GAME_ASSET_S_SCRIPT);

            if (!FileUtils.isDirectoryExist(scriptDirectoryHandle))
            {
                LogUtils.debug(GameScriptManager.class, "loadScriptData 脚本目录不存在 (path): " + scriptDirectoryHandle);
                return false;
            }

            // 加载脚本配置
            loadScriptConfig();

            if (!availableScripts.isEmpty())
            {
                // 有配置文件：只加载清单中的脚本
                for (String scriptFileName : availableScripts)
                {
                    FileHandle scriptFilePathHandle = scriptDirectoryHandle.child(scriptFileName);
                    if (FileUtils.isFileExist(scriptFilePathHandle))
                    {
                        loadSingleScript(scriptFilePathHandle);
                    }
                    else
                    {
                        LogUtils.error(GameScriptManager.class, "loadScriptData 脚本文件不存在 (path): " + scriptFilePathHandle);
                    }
                }
            }
            else
            {
                FileHandle[] scriptFileList = FileUtils.getList(scriptDirectoryHandle);
                if (scriptFileList != null)
                {
                    // 无配置文件：扫描目录全部加载（向后兼容）
                    for (FileHandle scriptFileHandle : scriptFileList)
                    {
                        if (scriptFileHandle.name().endsWith(".json")
                            && !scriptFileHandle.name().equals(FileName.IN_GAME_SCRIPT_DICTIONARY_CONFIG))
                        {
                            loadSingleScript(scriptFileHandle);
                        }
                    }
                }
            }

            LogUtils.debug(GameScriptManager.class, "loadScriptData 脚本数据缓存 (map): " + scriptMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameScriptManager.class, "loadScriptData", e);
            return false;
        }
    }

    /**
     * 初始化脚本管理器，加载脚本数据
     *
     * @param gameDirectoryPathHandle 游戏目录路径句柄
     * @return 初始化是否成功
     */
    public boolean init (FileHandle gameDirectoryPathHandle)
    {
        try
        {
            if (!loadScriptData(gameDirectoryPathHandle))
            {
                LogUtils.error(GameScriptManager.class, "init 脚本数据加载失败");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameScriptManager.class, "init", e);
            return false;
        }
    }

    /**
     * 获取可用脚本列表
     */
    public Set<String> getAvailableScripts ()
    {
        return availableScripts;
    }

    /**
     * 根据脚本文件名获取脚本
     *
     * @param scriptFileName 脚本文件名
     * @return 脚本对象，不存在则返回 null
     */
    public Script getScript (String scriptFileName)
    {
        try
        {
            if (scriptFileName == null) return null;

            // 有配置文件时，自动检测未在清单中但实际存在的脚本
            if (!availableScripts.isEmpty() && !availableScripts.contains(scriptFileName))
            {
                FileHandle probePathHandle = scriptDirectoryHandle.child(scriptFileName);
                if (FileUtils.isFileExist(probePathHandle))
                {
                    availableScripts.add(scriptFileName);
                    LogUtils.debug(GameScriptManager.class, "getScript 自动发现并加载脚本 (script): " + scriptFileName);
                    loadSingleScript(probePathHandle);
                }
                else
                {
                    LogUtils.error(GameScriptManager.class, "getScript 未知的脚本 (script): " + scriptFileName + " (available): " + availableScripts);
                    return null;
                }
            }

            return scriptMap.get(scriptFileName);
        }
        catch (Exception e)
        {
            LogUtils.error(GameScriptManager.class, "getScript", e);
            return null;
        }
    }

    /**
     * 释放脚本管理器资源，清空缓存
     *
     * @return 释放是否成功
     */
    public boolean dispose ()
    {
        try
        {
            scriptMap.clear();
            availableScripts = Collections.emptySet();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameScriptManager.class, "dispose", e);
            return false;
        }
    }
}
