package com.hujiugame.qingfeng.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.Name;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.LanguageKey;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LanguageManager
{
    // json文件
    private JsonEntity json = new JsonEntity();

    // 使用的语言
    private String name = null;

    // 语言路径句柄
    private FileHandle pathHandle = null;

    // 语言适配版本
    private String version;

    // 状态码
    private long stateCode = 0;

    // 可用语言块列表
    private Set<String> availableBlocks = Collections.emptySet();

    // 解析结果 - 使用 LRU 缓存
    private static final int MAX_BLOCK_COUNT = 3;
    // accessOrder = true 表示按访问顺序排序，最近访问的在尾部
    private final Map<String, Map<String, String>> blockMap = new LinkedHashMap<String, Map<String, String>>(16, 0.75f, true)
    {
        protected boolean removeEldestEntry (Map.Entry<String, Map<String, String>> eldest)
        {
            return size() > MAX_BLOCK_COUNT;
        }
    };

    /**
     * 更新状态码，标记语言管理器状态发生变化
     */
    private void update ()
    {
        stateCode++;
    }

    /**
     * 解析语言路径：从语言集配置中查找指定语言，若不存在则自动修复为默认语言
     *
     * @param pathName           语言路径名称
     * @param directoryPathHandle  语言路径句柄
     * @param isLauncherLanguage 是否为启动器语言（启动器语言不存在时会自动修复）
     * @param userConfigManager  用户配置管理器，用于修复用户配置
     * @return 解析成功返回 true，失败返回 false
     */
    private boolean parseLanguagePath (String pathName, FileHandle directoryPathHandle, boolean isLauncherLanguage, UserConfigManager userConfigManager)
    {
        try
        {
            // 读取语言集json
            FileHandle dictionaryJsonPathHandle = directoryPathHandle.child(FileName.LANGUAGE_DICTIONARY_CONFIG);
            JsonEntity dictionaryJson = new JsonEntity();

            // 检查文件存在
            if (FileUtils.isFileExist(dictionaryJsonPathHandle))
            {
                dictionaryJson = new JsonEntity(dictionaryJsonPathHandle);
            }

            // 文件不存在
            if (dictionaryJson.isEmpty())
            {
                // 复制默认配置到外部
                FileHandle internalDictionaryJsonPathHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_LANGUAGE, FileName.LANGUAGE_DICTIONARY_CONFIG));
                FileUtils.copyFile(internalDictionaryJsonPathHandle, dictionaryJsonPathHandle);

                // 读取json
                dictionaryJson = new JsonEntity(dictionaryJsonPathHandle);
            }

            // 解析语言集配置
            boolean isExist = false;
            if (dictionaryJson.containsKey(pathName))
            {
                // 存在对应语言索引
                name = dictionaryJson.getString(pathName);
                pathHandle = directoryPathHandle.child(pathName);

                LogUtils.debug(LanguageManager.class, "parsePath 用户使用的语言 (name): " + name);
                LogUtils.debug(LanguageManager.class, "parsePath 用户使用的语言 (path): " + pathHandle);

                // 判断文件夹是否存在
                if (FileUtils.isDirectoryExist(pathHandle))
                {
                    isExist = true;
                }
            }

            // 不存在语言
            if (!isExist)
            {
                if (!isLauncherLanguage)
                {
                    FileHandle maybeLanguagePathHandle = directoryPathHandle.child(pathName);
                    LogUtils.error(LanguageManager.class, "parsePath 找不到语言 (path): " + maybeLanguagePathHandle);
                    return false;
                }

                // 不存在对应语言索引 默认使用默认语言
                name = Name.DEFAULT_LANGUAGE_NAME;
                pathHandle = directoryPathHandle.child(FileName.DEFAULT_LANGUAGE_PATH);

                // 修复用户配置
                userConfigManager.setLanguage(FileName.DEFAULT_LANGUAGE_PATH);
                userConfigManager.save(userConfigManager.getPathHandle());
                LogUtils.debug(LanguageManager.class, "parsePath 修复用户配置 (language): " + name);

                // 添加到语言集(使用相对路径)
                dictionaryJson.put(FileName.DEFAULT_LANGUAGE_PATH, name);
                FileUtils.createStringFile(dictionaryJson.getJsonString(), dictionaryJsonPathHandle, false);

                // 复制标准的默认语言
                FileHandle internalDefaultThemePathHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_LANGUAGE, FileName.DEFAULT_LANGUAGE_PATH));
                FileHandle externalDefaultThemePathHandle = directoryPathHandle.child(FileName.DEFAULT_LANGUAGE_PATH);
                FileUtils.copyDirectory(internalDefaultThemePathHandle, externalDefaultThemePathHandle);

                LogUtils.debug(LanguageManager.class, "parsePath 添加默认语言 (name): " + name);
                LogUtils.debug(LanguageManager.class, "parsePath 添加默认语言 (path): " + pathHandle);
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "parseLanguagePath", e);
            return false;
        }
    }

    /**
     * 解析语言JSON配置文件
     *
     * @param pathHandle 语言路径文件句柄
     * @return 解析成功返回 true，失败返回 false
     */
    private boolean parseJson (FileHandle pathHandle)
    {
        try
        {
            // 读取语言配置
            FileHandle languageJsonPathHandle = pathHandle.child(FileName.LANGUAGE_S_CONFIG);
            json = new JsonEntity(languageJsonPathHandle);

            // 语言配置不存在
            if (json.isEmpty())
            {
                LogUtils.error(LanguageManager.class, "parseJson 语言配置不存在 (file): " + languageJsonPathHandle);
                return false;
            }

            LogUtils.debug(LanguageManager.class, "parseJson 读取语言配置 (path): " + languageJsonPathHandle);

            // 解析可用语言块列表
            if (json.containsKey(LanguageKey.BLOCKS))
            {
                availableBlocks = new HashSet<>(json.getStringList(LanguageKey.BLOCKS));
                LogUtils.debug(LanguageManager.class, "parseJson 读取可用语言块 (blocks): " + availableBlocks);
            }
            else
            {
                availableBlocks = Collections.emptySet();
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "parseJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载语言版本号
     *
     * @param json 语言JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadVersionFromJson (JsonEntity json)
    {
        try
        {
            // 字体读取
            if (json.containsKey(LanguageKey.VERSION))
            {
                version = json.getString(LanguageKey.VERSION);
                LogUtils.debug(LanguageManager.class, "loadVersionFromJson 读取语言版本 (version): " + version);
            }
            else
            {
                LogUtils.error(LanguageManager.class, "loadVersionFromJson 读取语言版本 失败");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "loadVersionFromJson", e);
            return false;
        }
    }

    /**
     * 将嵌套的JSON实体扁平化为键值对映射（递归处理子对象）
     *
     * @param json   待扁平化的JSON实体
     * @param prefix 当前递归层级的前缀字符串
     * @param map    扁平化结果存储的目标映射
     * @return 扁平化成功返回 true，失败返回 false
     */
    private boolean flattenMap (JsonEntity json, String prefix, Map<String, String> map)
    {
        try
        {
            for (String key : json.keySet())
            {
                // 判断是否到底
                if (json.getObject(key) instanceof String)
                {
                    map.put(prefix + key, json.getString(key));
                }
                else
                {
                    // 递归
                    if (!flattenMap(json.getJsonEntityByKey(key), prefix + key + ".", map))
                    {
                        return false;
                    }
                }
            }
            LogUtils.debug(LanguageManager.class, "flattenMap 读取语言 (map): " + map);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "flattenMap", e);
            return false;
        }
    }

    /**
     * 初始化语言管理器：解析语言路径、加载配置和版本
     *
     * @param pathName           语言路径名称
     * @param directoryPathHandle  语言路径文件句柄
     * @param isLauncherLanguage 是否为启动器语言
     * @param userConfigManager  用户配置管理器
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init (String pathName, FileHandle directoryPathHandle, boolean isLauncherLanguage, UserConfigManager userConfigManager)
    {
        try
        {
            // 解析语言路径
            if (!parseLanguagePath(pathName, directoryPathHandle, isLauncherLanguage, userConfigManager))
            {
                LogUtils.error(LanguageManager.class, "init 解析使用的语言失败 (name): " + pathName);
                return false;
            }

            // 解析json
            if (!parseJson(pathHandle))
            {
                LogUtils.error(LanguageManager.class, "init 解析语言配置失败 (name): " + pathName);
                return false;
            }

            // 获取版本
            if (!loadVersionFromJson(json))
            {
                LogUtils.error(LanguageManager.class, "init 读取语言版本失败 (json): " + json);
                return false;
            }

            // 增加状态码
            update();

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "init", e);
            return false;
        }
    }

    /**
     * 重新加载语言：调用 init 重新初始化
     *
     * @param name                语言路径名称
     * @param directoryPathHandle 语言路径文件句柄
     * @param isLauncherLanguage  是否为启动器语言
     * @param userConfigManager   用户配置管理器
     * @return 重载成功返回 true，失败返回 false
     */
    public boolean reload (String name, FileHandle directoryPathHandle, boolean isLauncherLanguage, UserConfigManager userConfigManager)
    {
        try
        {
            return init(name, directoryPathHandle, isLauncherLanguage, userConfigManager);
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "reload", e);
            return false;
        }
    }

    /**
     * 获取语言显示名称
     *
     * @return 语言显示名称
     */
    public String getName ()
    {
        return name;
    }

    /**
     * 获取语言路径
     *
     * @return 语言路径
     */
    public FileHandle getPathHandle ()
    {
        return pathHandle;
    }

    /**
     * 获取可用语言块列表
     *
     * @return 可用语言块名称集合，未配置时返回空集合
     */
    public Set<String> getAvailableBlocks ()
    {
        return availableBlocks;
    }

    /**
     * 加载指定语言块：读取并扁平化语言文件，加入LRU缓存
     *
     * @param block 语言块名称
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadBlock (String block)
    {
        try
        {
            // 校验：如果声明了可用块列表，检查请求的块是否在清单中
            if (!availableBlocks.isEmpty() && !availableBlocks.contains(block))
            {
                // 自动检测：文件实际存在则加入清单
                // 先尝试直接用 block 原名查找，找不到则补 .json
                FileHandle probePath = pathHandle.child(block);
                if (!FileUtils.isFileExist(probePath))
                {
                    probePath = pathHandle.child(block + ".json");
                }
                if (FileUtils.isFileExist(probePath))
                {
                    availableBlocks.add(block);
                    LogUtils.debug(LanguageManager.class, "loadBlock 自动发现并加入新语言块 (block): " + block);
                }
                else
                {
                    LogUtils.error(LanguageManager.class, "loadBlock 未知的语言块 (block): " + block + " (available): " + availableBlocks);
                    return false;
                }
            }

            // 如果已经存在，直接返回（LRU 会自动更新访问顺序）
            if (blockMap.containsKey(block))
            {
                return true;
            }

            // 读取并扁平化语言图：先尝试 block 原路径，找不到则补 .json
            FileHandle mapPathHandle = pathHandle.child(block);
            if (!FileUtils.isFileExist(mapPathHandle))
            {
                mapPathHandle = pathHandle.child(block + ".json");
            }
            JsonEntity mapJson = new JsonEntity(mapPathHandle);
            Map<String, String> newFlattenMap = new HashMap<>();
            if (!flattenMap(mapJson, "", newFlattenMap))
            {
                LogUtils.error(LanguageManager.class, "loadBlock 读取新块语言图失败 (block): " + block);
                return false;
            }

            // 放入缓存，LinkedHashMap 会自动处理超出容量的淘汰
            blockMap.put(block, newFlattenMap);

            // 增加状态码
            update();
            LogUtils.debug(LanguageManager.class, "loadBlock 加载新块语言图成功 (block): " + block);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "loadBlock", e);
            return false;
        }
    }

    /**
     * 获取指定语言块中键对应的文本值，块未加载时自动加载
     *
     * @param block  语言块名称
     * @param textKey 文本键
     * @return 对应的文本值，若不存在则回退到键本身
     */
    public String getText (String block, String textKey)
    {
        try
        {
            if (!blockMap.containsKey(block))
            {
                if (!loadBlock(block))
                {
                    LogUtils.error(LanguageManager.class, "getText 重载 读取块失败 (block): " + block);
                    return textKey;  // 回退到键本身
                }
                else
                {
                    LogUtils.debug(LanguageManager.class, "getText 重载 读取块成功 (block): " + block);
                }
            }
            // 注意：get 操作会触发 LinkedHashMap 的访问顺序更新
            return blockMap.get(block).getOrDefault(textKey, textKey);
        }
        catch (Exception e)
        {
            LogUtils.error(LanguageManager.class, "getText", e);
            return textKey;
        }
    }

    /**
     * 获取当前状态码，用于检测语言管理器状态变化
     *
     * @return 当前状态码
     */
    public long getStateCode ()
    {
        return stateCode;
    }
}
