package com.hujiugame.qingfeng.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.ConfigKey;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class GameUserConfigManager
{
    // json文件
    private JsonEntity json;

    // 配置路径句柄
    private FileHandle pathHandle;

    // 使用的语言(文件名字)
    private String language;

    // 使用的主题(文件夹名字)
    private String theme;

    /**
     * 解析用户游戏配置文件路径
     *
     * @param gameId 游戏ID
     * @return 解析是否成功
     */
    private boolean parseUserGameConfigPath (String gameId)
    {
        try
        {
            // 用户游戏配置路径
            pathHandle = Gdx.files.external(FileUtils.pathJoin(PathName.BASE, PathName.SAVE, gameId, FileName.IN_GAME_USER_CONFIG));
            LogUtils.debug(GameUserConfigManager.class, "parseUserGameConfigPath 用户游戏配置路径: " + pathHandle);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "parseUserGameConfigPath", e);
            return false;
        }
    }

    /**
     * 解析用户游戏配置JSON文件，若文件不存在则从游戏默认配置复制
     *
     * @param gamePathHandle 游戏路径句柄
     * @param jsonPathHandle 配置文件路径句柄
     * @return 解析是否成功
     */
    private boolean parseJson (FileHandle gamePathHandle, FileHandle jsonPathHandle)
    {
        try
        {
            JsonEntity userConfigJson = new JsonEntity();

            // 检查文件存在
            if (FileUtils.isFileExist(jsonPathHandle))
            {
                userConfigJson = new JsonEntity(jsonPathHandle);
            }

            // 如果文件读取失败
            if (userConfigJson.isEmpty())
            {
                // 复制游戏默认配置到存档处
                FileHandle gameDefaultConfigPathHandle = gamePathHandle.child(PathName.IN_GAME_ASSET).child(FileName.IN_GAME_USER_CONFIG);
                FileUtils.copyFile(gameDefaultConfigPathHandle, jsonPathHandle);

                // 读取用户设置json
                userConfigJson = new JsonEntity(jsonPathHandle);

                LogUtils.info(GameUserConfigManager.class, "parseJson 用户游戏配置文件不存在, 已复制游戏默认配置到存档处 (path): " + pathHandle);
            }

            // 存储json
            this.json = userConfigJson;
            LogUtils.info(GameUserConfigManager.class, "parseJson 用户游戏配置文件 (json): " + userConfigJson);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "parseJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载语言设置
     *
     * @param userConfigJson 用户配置JSON对象
     * @return 加载是否成功
     */
    private boolean loadLanguageFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(ConfigKey.User.LANGUAGE))
            {
                this.language = userConfigJson.getString(ConfigKey.User.LANGUAGE);
                return true;
            }
            else
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置缺少" + ConfigKey.User.LANGUAGE + "字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "loadLanguageFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载主题设置
     *
     * @param userConfigJson 用户配置JSON对象
     * @return 加载是否成功
     */
    private boolean loadThemeFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(ConfigKey.User.THEME))
            {
                this.theme = userConfigJson.getString(ConfigKey.User.THEME);
                return true;
            }
            else
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置缺少" + ConfigKey.User.THEME + "字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "loadThemeFromJson", e);
            return false;
        }
    }

    /**
     * 初始化用户游戏配置管理器，依次解析路径、读取JSON、加载语言和主题
     *
     * @param gamePathHandle 游戏路径句柄
     * @param gameId         游戏ID
     * @return 初始化是否成功
     */
    public boolean init (FileHandle gamePathHandle, String gameId)
    {
        try
        {
            // 获取用户游戏配置路径
            if (!parseUserGameConfigPath(gameId))
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置路径解析失败");
                return false;
            }

            // 读取json
            if (!parseJson(gamePathHandle, pathHandle))
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置json读取失败");
                return false;
            }

            // language
            if (!loadLanguageFromJson(json))
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置language读取失败");
                return false;
            }

            // theme
            if (!loadThemeFromJson(json))
            {
                LogUtils.error(GameUserConfigManager.class, "init 用户游戏配置theme读取失败");
                return false;
            }

            // debug
            LogUtils.debug(GameUserConfigManager.class, "init 初始化游戏配置成功");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "init", e);
            return false;
        }
    }

    /**
     * 保存用户游戏配置到文件
     *
     * @param gamePathHandle 游戏路径句柄
     * @return 保存是否成功
     */
    public boolean save (FileHandle gamePathHandle)
    {
        try
        {
            // TODO:...

            return FileUtils.createStringFile(json.toString(), gamePathHandle, false);
        }
        catch (Exception e)
        {
            LogUtils.error(GameUserConfigManager.class, "save", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 获取用户游戏配置的JSON对象
     *
     * @return JSON对象
     */
    public JsonEntity getJson ()
    {
        return json;
    }

    /**
     * 获取当前语言设置
     *
     * @return 语言名称
     */
    public String getLanguage ()
    {
        return language;
    }

    /**
     * 设置语言，并同步更新JSON数据
     *
     * @param language 语言名称
     */
    public void setLanguage (String language)
    {
        this.language = language;
        json.put(ConfigKey.User.LANGUAGE, language);
    }

    /**
     * 获取当前主题设置
     *
     * @return 主题名称
     */
    public String getTheme ()
    {
        return theme;
    }

    /**
     * 设置主题，并同步更新JSON数据
     *
     * @param theme 主题名称
     */
    public void setTheme (String theme)
    {
        this.theme = theme;
        json.put(ConfigKey.User.THEME, theme);
    }
}
