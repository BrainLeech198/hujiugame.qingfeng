package com.hujiugame.qingfeng.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.UserConfigKey;
import com.hujiugame.qingfeng.type.ui.UseViewport;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

public final class UserConfigManager
{
    // json文件
    private JsonEntity json;

    // 存储路径句柄
    private FileHandle pathHandle;

    // 使用的语言(文件名字)
    private String language;

    // 使用的主题(文件夹名字)
    private String theme;

    // 使用的视窗
    private UseViewport useViewport;

    // 是否全屏
    private boolean fullscreen;

    // 窗口分辨率
    private int resolutionWidth;
    private int resolutionHeight;

    // 声音音量
    private float soundVolumeTotal;
    private float soundVolumeMusic;
    private float soundVolumeSound;

    /**
     * 解析用户配置JSON文件，若文件不存在则从内部默认配置复制
     *
     * @param pathHandle     配置文件的路径句柄
     * @return 解析成功返回 true，失败返回 false
     */
    private boolean parseJson (FileHandle pathHandle)
    {
        try
        {
            JsonEntity userConfigJson = new JsonEntity();

            // 检查文件存在
            if (FileUtils.isFileExist(pathHandle))
            {
                userConfigJson = new JsonEntity(pathHandle);
            }

            // 如果文件读取失败
            if (userConfigJson.isEmpty())
            {
                // 复制默认配置到外部
                String internalPath = FileUtils.pathJoin(PathName.ASSET, FileName.USER_CONFIG);
                FileHandle internalPathHandle = Gdx.files.internal(internalPath);
                FileUtils.copyFile(internalPathHandle, pathHandle);

                // 读取用户设置json
                userConfigJson = new JsonEntity(pathHandle);
                LogUtils.info(UserConfigManager.class, "parseJson 用户配置文件不存在, 已复制默认配置 (path): " + pathHandle);
            }

            // 存储json
            this.json = userConfigJson;
            LogUtils.debug(UserConfigManager.class, "parseJson 用户配置文件 (json): " + userConfigJson);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "parseJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载语言配置
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLanguageFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.LANGUAGE))
            {
                this.language = userConfigJson.getString(UserConfigKey.LANGUAGE);
                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置缺少language字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadLanguageFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载主题配置
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadThemeFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.THEME))
            {
                this.theme = userConfigJson.getString(UserConfigKey.THEME);
                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置缺少theme字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadThemeFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载视窗模式配置
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadUseViewportFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.USE_VIEWPORT))
            {
                this.useViewport = UseViewport.valueOf(userConfigJson.getString(UserConfigKey.USE_VIEWPORT).toUpperCase());
                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置缺少useViewport字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadUseViewportFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载全屏模式配置
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadFullscreenFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.FULLSCREEN))
            {
                this.fullscreen = userConfigJson.getBoolean(UserConfigKey.FULLSCREEN);
                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置缺少fullscreen字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadFullscreenFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载窗口分辨率配置（宽和高）
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadResolutionFromJson (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.RESOLUTION))
            {
                JsonEntity resolutionJson = userConfigJson.getJsonEntityByKey(UserConfigKey.RESOLUTION);

                // width
                if (resolutionJson.containsKey(UserConfigKey.RESOLUTION_WIDTH))
                {
                    this.resolutionWidth = resolutionJson.getInt(UserConfigKey.RESOLUTION_WIDTH);
                }
                else
                {
                    LogUtils.error(UserConfigManager.class, "loadResolutionFromJson 用户配置缺少resolution.width字段");
                    return false;
                }

                // height
                if (resolutionJson.containsKey(UserConfigKey.RESOLUTION_HEIGHT))
                {
                    this.resolutionHeight = resolutionJson.getInt(UserConfigKey.RESOLUTION_HEIGHT);
                }
                else
                {
                    LogUtils.error(UserConfigManager.class, "loadResolutionFromJson 用户配置缺少resolution.height字段");
                    return false;
                }

                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置缺少resolution字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadResolutionFromJson", e);
            return false;
        }
    }

    /**
     * 从JSON中加载声音音量配置（总音量、音乐音量、音效音量）
     *
     * @param userConfigJson 用户配置的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadSoundVolume (JsonEntity userConfigJson)
    {
        try
        {
            if (userConfigJson.containsKey(UserConfigKey.SOUND_VOLUME))
            {
                JsonEntity soundVolumeJson = userConfigJson.getJsonEntityByKey(UserConfigKey.SOUND_VOLUME);

                // total
                if (soundVolumeJson.containsKey(UserConfigKey.SOUND_VOLUME_TOTAL))
                {
                    this.soundVolumeTotal = soundVolumeJson.getFloat(UserConfigKey.SOUND_VOLUME_TOTAL);
                }
                else
                {
                    LogUtils.error(UserConfigManager.class, "loadSoundVolume 用户配置缺少soundVolume.total字段");
                    return false;
                }

                // music
                if (soundVolumeJson.containsKey(UserConfigKey.SOUND_VOLUME_MUSIC))
                {
                    this.soundVolumeMusic = soundVolumeJson.getFloat(UserConfigKey.SOUND_VOLUME_MUSIC);
                }
                else
                {
                    LogUtils.error(UserConfigManager.class, "loadSoundVolume 用户配置缺少soundVolume.music字段");
                    return false;
                }

                // sound
                if (soundVolumeJson.containsKey(UserConfigKey.SOUND_VOLUME_SOUND))
                {
                    this.soundVolumeSound = soundVolumeJson.getFloat(UserConfigKey.SOUND_VOLUME_SOUND);
                }
                else
                {
                    LogUtils.error(UserConfigManager.class, "loadSoundVolume 用户配置缺少soundVolume.sound字段");
                    return false;
                }

                return true;
            }
            else
            {
                LogUtils.error(UserConfigManager.class, "loadSoundVolume 用户配置缺少soundVolume字段");
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "loadSoundVolume", e);
            return false;
        }
    }

    /**
     * 初始化用户配置管理器，依次解析并加载所有配置项
     *
     * @param pathHandle     配置文件路径句柄
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init (FileHandle pathHandle)
    {
        try
        {
            // 存储路径
            this.pathHandle = pathHandle;

            // 读取json
            if (!parseJson(pathHandle))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置json读取失败");
                return false;
            }

            // language
            if (!loadLanguageFromJson(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置language读取失败");
                return false;
            }

            // theme
            if (!loadThemeFromJson(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置theme读取失败");
                return false;
            }

            // useViewport
            if (!loadUseViewportFromJson(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置useViewport读取失败");
                return false;
            }

            // fullscreen
            if (!loadFullscreenFromJson(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置fullscreen读取失败");
                return false;
            }

            // resolution
            if (!loadResolutionFromJson(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置resolution读取失败");
                return false;
            }

            // soundVolume
            if (!loadSoundVolume(json))
            {
                LogUtils.error(UserConfigManager.class, "init 用户配置soundVolume读取失败");
                return false;
            }

            // debug
            LogUtils.info(UserConfigManager.class, "init 初始化配置成功 (json): " + json);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "init", e);
            return false;
        }
    }

    /**
     * 修复用户配置文件：从内部默认配置复制覆盖外部配置并重新初始化
     *
     * @return 修复成功返回 true，失败返回 false
     */
    public boolean repair ()
    {
        try
        {
            // 内部路径
            String internalPath = FileUtils.pathJoin(PathName.ASSET, FileName.USER_CONFIG);
            FileHandle internalPathHandle = Gdx.files.internal(internalPath);

            // 维修
            FileUtils.copyFile(internalPathHandle, pathHandle);
            LogUtils.debug(UserConfigManager.class, "repair 修复用户配置 (path): " + pathHandle);

            // 重新读取
            return init(pathHandle);
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "repair", e);
            return false;
        }
    }

    /**
     * 保存当前用户配置到指定路径
     *
     * @param pathHandle  保存路径句柄
     * @return 保存成功返回 true，失败返回 false
     */
    public boolean save (FileHandle pathHandle)
    {
        try
        {
            return FileUtils.createStringFile(json.toString(), pathHandle, false);
        }
        catch (Exception e)
        {
            LogUtils.error(UserConfigManager.class, "save", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 获取用户配置的JSON实体
     *
     * @return 用户配置的JSON实体
     */
    public JsonEntity getJson ()
    {
        return json;
    }

    /**
     * 获取配置文件路径
     *
     * @return 配置文件路径字符串
     */
    public FileHandle getPathHandle ()
    {
        return pathHandle;
    }

    /**
     * 获取当前使用的语言
     *
     * @return 语言名称
     */
    public String getLanguage ()
    {
        return language;
    }

    /**
     * 设置当前使用的语言并更新JSON配置
     *
     * @param language 语言名称
     */
    public void setLanguage (String language)
    {
        this.language = language;
        json.put("language", language);
    }

    /**
     * 获取当前使用的主题
     *
     * @return 主题名称
     */
    public String getTheme ()
    {
        return theme;
    }

    /**
     * 设置当前使用的主题并更新JSON配置
     *
     * @param theme 主题名称
     */
    public void setTheme (String theme)
    {
        this.theme = theme;
        json.put("theme", theme);
    }

    /**
     * 获取当前使用的视窗模式
     *
     * @return 视窗模式枚举
     */
    public UseViewport getUseViewport ()
    {
        return useViewport;
    }

    /**
     * 设置当前使用的视窗模式
     *
     * @param useViewport 视窗模式枚举
     */
    public void setUseViewport (UseViewport useViewport)
    {
        this.useViewport = useViewport;
    }

    /**
     * 判断当前是否为全屏模式
     *
     * @return 全屏返回 true，窗口化返回 false
     */
    public boolean isFullscreen ()
    {
        return fullscreen;
    }

    /**
     * 设置是否全屏
     *
     * @param fullscreen 全屏标志
     */
    public void setFullscreen (boolean fullscreen)
    {
        this.fullscreen = fullscreen;
    }

    /**
     * 获取窗口分辨率宽度
     *
     * @return 分辨率宽度（像素）
     */
    public int getResolutionWidth ()
    {
        return resolutionWidth;
    }

    /**
     * 设置窗口分辨率宽度
     *
     * @param resolutionWidth 分辨率宽度（像素）
     */
    public void setResolutionWidth (int resolutionWidth)
    {
        this.resolutionWidth = resolutionWidth;
    }

    /**
     * 获取窗口分辨率高度
     *
     * @return 分辨率高度（像素）
     */
    public int getResolutionHeight ()
    {
        return resolutionHeight;
    }

    /**
     * 设置窗口分辨率高度
     *
     * @param resolutionHeight 分辨率高度（像素）
     */
    public void setResolutionHeight (int resolutionHeight)
    {
        this.resolutionHeight = resolutionHeight;
    }

    /**
     * 获取总音量
     *
     * @return 总音量值（0.0 ~ 1.0）
     */
    public float getSoundVolumeTotal ()
    {
        return soundVolumeTotal;
    }

    /**
     * 设置总音量并更新JSON配置
     *
     * @param soundVolumeTotal 总音量值（0.0 ~ 1.0）
     */
    public void setSoundVolumeTotal (float soundVolumeTotal)
    {
        this.soundVolumeTotal = soundVolumeTotal;
        json.getJsonEntityByKey("soundVolume").put("total", soundVolumeTotal);
    }

    /**
     * 获取音乐音量
     *
     * @return 音乐音量值（0.0 ~ 1.0）
     */
    public float getSoundVolumeMusic ()
    {
        return soundVolumeMusic;
    }

    /**
     * 设置音乐音量并更新JSON配置
     *
     * @param soundVolumeMusic 音乐音量值（0.0 ~ 1.0）
     */
    public void setSoundVolumeMusic (float soundVolumeMusic)
    {
        this.soundVolumeMusic = soundVolumeMusic;
        json.getJsonEntityByKey("soundVolume").put("music", soundVolumeMusic);
    }

    /**
     * 获取音效音量
     *
     * @return 音效音量值（0.0 ~ 1.0）
     */
    public float getSoundVolumeSound ()
    {
        return soundVolumeSound;
    }

    /**
     * 设置音效音量并更新JSON配置
     *
     * @param soundVolumeSound 音效音量值（0.0 ~ 1.0）
     */
    public void setSoundVolumeSound (float soundVolumeSound)
    {
        this.soundVolumeSound = soundVolumeSound;
        json.getJsonEntityByKey("soundVolume").put("sound", soundVolumeSound);
    }
}
