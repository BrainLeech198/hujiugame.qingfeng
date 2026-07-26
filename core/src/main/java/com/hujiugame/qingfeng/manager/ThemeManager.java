package com.hujiugame.qingfeng.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.Name;
import com.hujiugame.qingfeng.game.GameInfoManager;
import com.hujiugame.qingfeng.type.key.GameInfoKey;
import com.hujiugame.qingfeng.type.key.ThemeKey;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.ui.kind.ColorConfig;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Arrays;
import java.util.List;

public final class ThemeManager
{
    // json文件
    private JsonEntity json = new JsonEntity();

    // 使用的语主题
    private String name = null;

    // 主题路径句柄
    private FileHandle pathHandle = null;

    // 版本
    private String version;

    // 默认字体
    private String font = null;

    // 字体预缓存尺寸列表（缩放系数）
    private float[] fontUseSize = null;

    // 颜色
    private ColorConfig colorConfig = null;

    /**
     * 解析主题路径：从主题集配置中查找指定主题，若不存在则自动修复为默认主题
     *
     * @param pathName            主题路径名称
     * @param directoryPathHandle 主题集目录路径句柄
     * @param isLauncherTheme     是否为启动器主题（启动器主题不存在时会自动修复）
     * @param userConfigManager   用户配置管理器，用于修复用户配置
     * @return 解析成功返回 true，失败返回 false
     */
    private boolean parseThemePath (String pathName, FileHandle directoryPathHandle, boolean isLauncherTheme, UserConfigManager userConfigManager)
    {
        try
        {
            // 读取主题集json
            FileHandle dictionaryJsonPathHandle = directoryPathHandle.child(FileName.THEME_DICTIONARY_CONFIG);
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
                FileHandle internalDictionaryJsonPathHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_THEME, FileName.THEME_DICTIONARY_CONFIG));
                FileUtils.copyFile(internalDictionaryJsonPathHandle, dictionaryJsonPathHandle);

                // 读取json
                dictionaryJson = new JsonEntity(dictionaryJsonPathHandle);
            }

            // 解析主题集配置
            boolean isExist = false;
            if (dictionaryJson.containsKey(pathName))
            {
                // 存在对应主题索引
                name = dictionaryJson.getString(pathName);
                pathHandle = directoryPathHandle.child(pathName);

                LogUtils.debug(ThemeManager.class, "parseTheme 用户使用的主题 (name): " + name);
                LogUtils.debug(ThemeManager.class, "parseTheme 用户使用的主题 (path): " + pathHandle);

                // 判断文件夹存不存在
                if (FileUtils.isDirectoryExist(pathHandle))
                {
                    isExist = true;
                }
            }

            // 不存在修正主题
            if (!isExist)
            {
                if (!isLauncherTheme)
                {
                    FileHandle maybeThemePathHandle = directoryPathHandle.child(pathName);
                    LogUtils.error(ThemeManager.class, "parseTheme 找不到主题 (path): " + maybeThemePathHandle);
                    return false;
                }

                // 不存在对应主题索引 默认使用默认主题
                name = Name.DEFAULT_THEME_NAME;
                pathHandle = directoryPathHandle.child(FileName.DEFAULT_THEME);

                // 修复用户配置
                userConfigManager.setTheme(FileName.DEFAULT_THEME);
                userConfigManager.save(userConfigManager.getPathHandle());
                LogUtils.debug(ThemeManager.class, "parseTheme 修复用户配置 (theme): " + name);

                // 添加到主题集(使用相对路径)
                dictionaryJson.put(FileName.DEFAULT_THEME, name);
                FileUtils.createStringFile(dictionaryJson.getJsonString(), dictionaryJsonPathHandle, false);

                // 复制标准的默认主题文件
                FileHandle internalDefaultThemePathHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_THEME, FileName.DEFAULT_THEME));
                FileHandle externalDefaultThemePathHandle = directoryPathHandle.child(FileName.DEFAULT_THEME);
                FileUtils.copyDirectory(internalDefaultThemePathHandle, externalDefaultThemePathHandle);

                LogUtils.debug(ThemeManager.class, "parseTheme 添加默认主题 (json): " + name);
                LogUtils.debug(ThemeManager.class, "parseTheme 添加默认主题 (path): " + pathHandle);
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "parseTheme", e);
            return false;
        }
    }

    /**
     * 解析主题JSON配置文件
     *
     * @param pathHandle 主题路径句柄
     * @return 解析成功返回 true，失败返回 false
     */
    private boolean parseJson (FileHandle pathHandle)
    {
        try
        {
            // 读取主题配置
            FileHandle themeConfigPathHandle = pathHandle.child(FileName.THEME_S_CONFIG);
            json = new JsonEntity(themeConfigPathHandle);

            // 主题配置不存在
            if (json.isEmpty())
            {
                LogUtils.error(ThemeManager.class, "parseJson 主题配置不存在 (file): " + themeConfigPathHandle);
                return false;
            }

            LogUtils.debug(ThemeManager.class, "parseJson 读取主题配置 (json): " + json);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "parseJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载主题版本号
     *
     * @param themeJson 主题JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadVersionFromJson (JsonEntity themeJson)
    {
        try
        {
            // 字体读取
            if (themeJson.containsKey(ThemeKey.VERSION))
            {
                version = themeJson.getString(ThemeKey.VERSION);
                LogUtils.debug(ThemeManager.class, "loadFontFromJson 读取主题版本 (version): " + version);
            }
            else
            {
                LogUtils.error(ThemeManager.class, "loadFontFromJson 读取主题版本 失败");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "loadVersionFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载主题默认字体
     *
     * @param json 主题JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadFontFromJson (JsonEntity json)
    {
        try
        {
            // 字体读取
            if (json.containsKey(ThemeKey.FONT))
            {
                font = json.getString(ThemeKey.FONT);
                LogUtils.debug(ThemeManager.class, "loadFontFromJson 读取主题字体 (font): " + font);
            }
            else
            {
                LogUtils.error(ThemeManager.class, "loadFontFromJson 读取主题字体 失败");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "loadFontFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载字体预缓存尺寸列表，缺失时使用 {@link Numeric#getFontNormalScaleList()} 的默认值
     *
     * @param json 主题JSON实体
     */
    private void loadFontUseSizeFromJson (JsonEntity json)
    {
        try
        {
            if (json.containsKey(ThemeKey.FONT_USE_SIZE))
            {
                List<Float> sizeList = json.getFloatList(ThemeKey.FONT_USE_SIZE);
                fontUseSize = new float[sizeList.size()];
                for (int i = 0; i < sizeList.size(); i++)
                {
                    fontUseSize[i] = sizeList.get(i);
                }
                LogUtils.debug(ThemeManager.class, "loadFontUseSizeFromJson 读取字体尺寸 (fontUseSize): " + Arrays.toString(fontUseSize));
            }
            else
            {
                fontUseSize = Numeric.getFontNormalScaleList();
                LogUtils.debug(ThemeManager.class, "loadFontUseSizeFromJson 缺失字段，使用默认尺寸: " + Arrays.toString(fontUseSize));
            }
        }
        catch (Exception e)
        {
            fontUseSize = Numeric.getFontNormalScaleList();
            LogUtils.error(ThemeManager.class, "loadFontUseSizeFromJson 解析失败，使用默认尺寸", e);
        }
    }

    /**
     * 从JSON中加载主题颜色配置（主色、辅色、字体色）
     *
     * @param json 主题JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadColorFromJson (JsonEntity json)
    {
        try
        {
            // 颜色读取
            String[] colorKeys = new String[]{ThemeKey.PRIMARY_COLOR, ThemeKey.SECONDARY_COLOR, ThemeKey.FONT_COLOR};
            String[] colorHexs = new String[]{"#000000FF", "#000000FF", "#000000FF"};

            for (int i = 0; i < colorKeys.length; i++)
            {
                if (json.containsKey(colorKeys[i]))
                {
                    colorHexs[i] = json.getString(colorKeys[i]);
                    LogUtils.debug(ThemeManager.class, "loadColorFromJson 读取主题颜色 (key): " + colorKeys[i] + " (hex): " + colorHexs[i]);
                }
                else
                {
                    LogUtils.error(ThemeManager.class, "loadColorFromJson 读取主题颜色 (key): " + colorKeys[i] + " 失败, 忽略默认为 (hex): #FF000000");
                }
            }

            // 存储到颜色对象
            colorConfig = new ColorConfig(colorHexs[0], colorHexs[1], colorHexs[2]);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "loadColorFromJson", e);
            return false;
        }
    }

    /**
     * 将主题主色调保存到外部 app_config.json，供启动器进度条使用
     */
    private void saveProcessColorToAppConfig ()
    {
        try
        {
            FileHandle appConfigFileHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.ASSET, FileName.APP_CONFIG));
            JsonEntity appConfig = new JsonEntity();
            if (FileUtils.isFileExist(appConfigFileHandle))
            {
                appConfig = new JsonEntity(appConfigFileHandle);
            }
            Color primaryColor = colorConfig.getPrimaryColor();
            String colorHex = String.format("#%02X%02X%02X%02X",
                (int)(primaryColor.r * 255), (int)(primaryColor.g * 255),
                (int)(primaryColor.b * 255), (int)(primaryColor.a * 255));
            appConfig.put(ThemeKey.PROCESS_COLOR, colorHex);
            FileUtils.createStringFile(appConfig.getJsonString(), appConfigFileHandle, false);
            LogUtils.debug(ThemeManager.class, "saveProcessColorToAppConfig 写入主色调: " + colorHex);
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "saveProcessColorToAppConfig", e);
        }
    }

    /**
     * 初始化主题管理器：解析主题路径、加载配置、版本、字体和颜色
     *
     * @param pathName            主题路径名称
     * @param directoryPathHandle 主题集目录路径句柄
     * @param isLauncherTheme     是否为启动器主题
     * @param userConfigManager   用户配置管理器
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init (String pathName, FileHandle directoryPathHandle, boolean isLauncherTheme, UserConfigManager userConfigManager)
    {
        try
        {
            // 解析主题名字和路径
            if (!parseThemePath(pathName, directoryPathHandle, isLauncherTheme, userConfigManager))
            {
                LogUtils.error(ThemeManager.class, "init 解析使用的主题失败 (name): " + pathName);
                return false;
            }

            // 解析json
            if (!parseJson(pathHandle))
            {
                LogUtils.error(ThemeManager.class, "init 解析主题配置失败 (name): " + pathName);
                return false;
            }

            // 解析版本
            if (!loadVersionFromJson(json))
            {
                LogUtils.error(ThemeManager.class, "init 读取主题版本失败 (json): " + json);
                return false;
            }

            // 读取字体
            if (!loadFontFromJson(json))
            {
                LogUtils.error(ThemeManager.class, "init 读取主题字体失败 (json): " + json);
                return false;
            }

            // 读取字体预缓存尺寸
            loadFontUseSizeFromJson(json);

            // 读取颜色
            if (!loadColorFromJson(json))
            {
                LogUtils.error(ThemeManager.class, "init 读取主题颜色失败 (json): " + json);
                return false;
            }

            // 非游戏主题：将主色调原值写入外部 app_config.json 的 process_color 字段
            if (isLauncherTheme)
            {
                saveProcessColorToAppConfig();
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "init", e);
            return false;
        }
    }

    /**
     * 重新加载主题：调用 init 重新初始化
     *
     * @param name                主题路径名称
     * @param directoryPathHandle 主题集目录路径句柄
     * @param isLauncherTheme     是否为启动器主题
     * @param userConfigManager   用户配置管理器
     * @return 重载成功返回 true，失败返回 false
     */
    public boolean reload (String name, FileHandle directoryPathHandle, boolean isLauncherTheme, UserConfigManager userConfigManager)
    {
        try
        {
            return init(name, directoryPathHandle, isLauncherTheme, userConfigManager);
        }
        catch (Exception e)
        {
            LogUtils.error(ThemeManager.class, "reload", e);
            return false;
        }
    }

    /**
     * 解析图标的完整路径
     *
     * @param path 图标所在目录路径句柄
     * @return 图标文件的完整路径
     */
    public FileHandle parseIconPath (FileHandle path)
    {
        return path.child(FileName.THEME_S_ICON);
    }

    /**
     * 使用当前主题路径解析图标的完整路径
     *
     * @return 图标文件的完整路径
     */
    public FileHandle parseIconPath ()
    {
        return parseIconPath(pathHandle);
    }

    /**
     * 获取主题JSON实体
     *
     * @return 主题JSON实体
     */
    public JsonEntity getJson ()
    {
        return json;
    }

    /**
     * 获取主题路径句柄
     *
     * @return 主题路径句柄
     */
    public FileHandle getPathHandle ()
    {
        return pathHandle;
    }

    /**
     * 获取主题显示名称
     *
     * @return 主题显示名称
     */
    public String getName ()
    {
        return name;
    }

    /**
     * 获取主题默认字体
     *
     * @return 字体名称
     */
    public String getFont ()
    {
        return font;
    }

    /**
     * 获取主题颜色配置对象
     *
     * @return 颜色配置对象（含主色、辅色、字体色）
     */
    public ColorConfig getColorConfig ()
    {
        return colorConfig;
    }

    /**
     * 获取字体预缓存尺寸列表
     *
     * @return 字体缩放系数数组，不会为 null
     */
    public float[] getFontUseSize ()
    {
        return fontUseSize;
    }

    // ===================================================================================================================
    // 上载到 GameInfoManager

    /**
     * 将主题信息上载到运行时信息管理器
     *
     * @param gameInfoManager 运行时信息管理器
     */
    public void uploadTo (GameInfoManager gameInfoManager)
    {
        gameInfoManager.putInfo(GameInfoKey.User.THEME_NAME, name);
    }
}
