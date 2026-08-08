package com.hujiugame.qingfeng.graphic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.BufferUtils;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.graphic.model.GifInfo;
import com.hujiugame.qingfeng.graphic.model.PictureInfo;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.ScreenSize;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.manager.ThemeManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;
import com.hujiugame.qingfeng.util.system.SafePostRunnable;

import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GraphicsManager
{
    private final SpriteBatch spriteBatch;
    // 纹理缓存（路径 → 纹理，全局复用）
    private final Map<String, Texture> textureCache = new ConcurrentHashMap<>();
    // 活跃图片资源
    private final Map<String, Texture> pictureMap = new ConcurrentHashMap<>();
    // 活跃背景图片资源（独立管理，全屏使用）
    private final Map<String, Texture> backgroundPictureMap = new ConcurrentHashMap<>();
    // GIF 资源
    private final Map<String, Gif> gifMap = new ConcurrentHashMap<>();
    // 待销毁队列
    private final Map<String, Texture> disposePictureMap = new ConcurrentHashMap<>();
    private final Map<String, Texture> disposeBackgroundPictureMap = new ConcurrentHashMap<>();
    private final Map<String, Gif> disposeGifMap = new ConcurrentHashMap<>();
    private final ThemeManager themeManager;
    private final ScheduledExecutorService disposeExecutor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        Thread thread = new Thread(r, "GraphicsManager-Dispose-Thread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) ->
            LogUtils.error(GraphicsManager.class, "Dispose线程异常", (Exception) e));
        return thread;
    });
    private final AtomicBoolean disposeScheduled = new AtomicBoolean(false);
    private int maxTextureSize = 2048; // 默认值，初始化后会被查询覆盖
    private Texture errorTexture;
    private UiManager uiManager = null;

    /**
     * 构造图形管理器。
     *
     * @param spriteBatch  精灵批处理对象，用于绘制图形
     * @param themeManager 主题管理器，用于获取主题资源路径
     */
    public GraphicsManager (SpriteBatch spriteBatch, ThemeManager themeManager)
    {
        this.spriteBatch = spriteBatch;
        this.themeManager = themeManager;
    }

    /**
     * 加载错误占位纹理，用于文件加载失败时的回退显示。
     *
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadErrorPicture ()
    {
        try
        {
            // 2026.7.8 我也不知道怎么想的，当时会复制一份到外面使用error图片 去掉了
            FileHandle errorImageFileHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET_S_RESOURCE_IMAGE, FileName.IMAGE_ERROR));
            errorTexture = new Texture(errorImageFileHandle);
            if (errorImageFileHandle != null)
            {
                textureCache.put(errorImageFileHandle.path(), errorTexture);
            }
            else
            {
                LogUtils.error(GraphicsManager.class, "loadErrorPicture 错误图片加载失败，无法继续");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "loadErrorPicture", e);
            return false;
        }
    }

    /**
     * 初始化图形管理器，加载错误纹理并查询设备最大纹理尺寸。
     *
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init ()
    {
        try
        {
            // 错误纹理必须加载成功
            if (!loadErrorPicture())
            {
                LogUtils.error(GraphicsManager.class, "init 错误纹理加载失败，无法继续");
                return false;
            }

            // 查询设备最大纹理尺寸
            IntBuffer buf = BufferUtils.newIntBuffer(1);
            Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
            maxTextureSize = buf.get();
            LogUtils.debug(GraphicsManager.class, "最大纹理尺寸: " + maxTextureSize);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "init", e);
            return false;
        }
    }

    /**
     * 引用 UI 管理器实例，用于获取字体等 UI 相关资源。
     *
     * @param uiManager UI 管理器实例
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean quoteUiManager (UiManager uiManager)
    {
        try
        {
            this.uiManager = uiManager;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "quoteUiManager", e);
            return false;
        }
    }

    // ==================== 纹理获取（带缓存） ====================

    /**
     * 检查指定标签的图片是否已加载。
     *
     * @param tag 图片标签
     * @return 存在返回 true，否则返回 false
     */
    public boolean hasPicture (String tag)
    {
        try
        {
            return pictureMap.containsKey(tag);
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "hasPicture", e);
            return false;
        }
    }

    /**
     * 为指定标签创建 1x1 白色纹理（用于纯色进度条等场景）。
     *
     * @param tag 图片标签
     * @return 创建成功返回 true，失败返回 false
     */
    public boolean loadWhitePicture (String tag)
    {
        try
        {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();

            synchronized (pictureMap)
            {
                Texture old = pictureMap.put(tag, texture);
                if (old != null && old != errorTexture)
                {
                    disposePictureMap.put(tag, old);
                    scheduleAsyncDispose();
                }
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "loadWhitePicture", e);
            return false;
        }
    }

    /**
     * 获取文件对应的纹理，优先从缓存返回。文件不存在时返回错误纹理。
     *
     * @param file 文件句柄
     * @return 纹理对象，文件不存在时返回错误纹理，null 入参返回 null
     */
    public Texture getTexture (FileHandle file)
    {
        if (file == null)
        {
            LogUtils.debug(GraphicsManager.class, "getTexture (file): null");
            return null;
        }

        if (!FileUtils.isFileExist(file))
        {
            LogUtils.error(GraphicsManager.class, "getTexture 文件不存在: " + file.path());
            return errorTexture;
        }

        String key = file.path();
        Texture tex = textureCache.get(key);
        if (tex != null)
        {
            return tex;
        }

        try
        {
            tex = new Texture(file);
            textureCache.put(key, tex);
            return tex;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "getTexture 加载失败: " + key);
            return errorTexture;
        }
    }

    // ==================== 普通图片 ====================

    /**
     * 加载普通图片资源。如果该标签已存在，旧纹理加入待销毁队列。
     *
     * @param tag  图片标签
     * @param file 图片文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadPicture (String tag, FileHandle file)
    {
        synchronized (pictureMap)
        {
            try
            {
                Texture newTexture = getTexture(file);
                if (newTexture == null)
                {
                    LogUtils.error(GraphicsManager.class, "loadPicture getTexture 返回 null (tag): " + tag);
                    return false;
                }
                if (newTexture == errorTexture)
                {
                    // 首次加载失败：用 errorTexture 占位，阻止无限重试
                    if (!pictureMap.containsKey(tag))
                    {
                        pictureMap.put(tag, errorTexture);
                        LogUtils.debug(GraphicsManager.class, "loadPicture 首次加载失败，已用 errorTexture 占位 (tag): " + tag);
                    }
                    else
                    {
                        LogUtils.error(GraphicsManager.class, "loadPicture 加载失败，不替换旧图 (tag): " + tag);
                    }
                    return false;
                }

                Texture old = pictureMap.get(tag);
                // 关键：如果新旧纹理是同一个对象，无需替换，避免误销毁
                if (newTexture == old)
                {
                    return true;
                }

                // 替换旧纹理
                old = pictureMap.put(tag, newTexture);
                if (old != null && old != errorTexture)
                {
                    disposePictureMap.remove(tag);
                    disposePictureMap.put(tag, old);
                    scheduleAsyncDispose();
                    LogUtils.debug(GraphicsManager.class, "loadPicture 旧纹理待销毁 (tag): " + tag);
                }
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(GraphicsManager.class, "loadPicture", e);
                return false;
            }
        }
    }

    /**
     * 在指定位置绘制图片。
     *
     * @param tag    图片标签
     * @param x      绘制位置 x 坐标
     * @param y      绘制位置 y 坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putPicture (String tag, int x, int y, int width, int height)
    {
        try
        {
            Texture texture = pictureMap.get(tag);
            if (texture == null)
            {
                LogUtils.error(GraphicsManager.class, "putPicture 图片文件不存在 (Picture): " + tag + " (map): " + pictureMap);
                return false;
            }
            spriteBatch.draw(texture, x, y, width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putPicture", e);
            return false;
        }
    }

    /**
     * 在指定位置绘制图片（带颜色叠加）。
     *
     * @param tag    图片标签
     * @param x      绘制位置 x 坐标
     * @param y      绘制位置 y 坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     * @param tint   叠加颜色
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putPicture (String tag, int x, int y, int width, int height, Color tint)
    {
        try
        {
            Texture texture = pictureMap.get(tag);
            if (texture == null)
            {
                LogUtils.error(GraphicsManager.class, "putPicture 图片文件不存在 (Picture): " + tag + " (map): " + pictureMap);
                return false;
            }
            Color originalColor = new Color(spriteBatch.getColor());
            spriteBatch.setColor(tint);
            spriteBatch.draw(texture, x, y, width, height);
            spriteBatch.setColor(originalColor);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putPicture", e);
            return false;
        }
    }

    /**
     * 根据图片信息对象绘制单张图片。
     *
     * @param pictureInfo 图片信息对象，包含路径、位置和尺寸
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putPicture (PictureInfo pictureInfo)
    {
        return putPicture(pictureInfo.getPath(), pictureInfo.getX(), pictureInfo.getY(),
            pictureInfo.getWidth(), pictureInfo.getHeight());
    }

    /**
     * 批量绘制多张图片。
     *
     * @param pictureInfoList 图片信息对象列表
     * @return 全部绘制成功返回 true，否则返回 false
     */
    public boolean putPicture (List<PictureInfo> pictureInfoList)
    {
        boolean result = true;
        if (pictureInfoList != null)
        {
            for (PictureInfo info : pictureInfoList)
            {
                if (!putPicture(info)) result = false;
            }
        }
        return result;
    }

    /**
     * 根据标签列表和图片信息映射批量绘制图片。
     *
     * @param pictureTagList 图片标签列表
     * @param pictureInfoMap 标签到图片信息的映射
     * @return 全部绘制成功返回 true，否则返回 false
     */
    public boolean putPicture (List<String> pictureTagList, Map<String, PictureInfo> pictureInfoMap)
    {
        boolean result = true;
        if (pictureTagList != null && pictureInfoMap != null)
        {
            for (String tag : pictureTagList)
            {
                PictureInfo info = pictureInfoMap.get(tag);
                if (info != null)
                {
                    if (!putPicture(info)) result = false;
                }
                else result = false;
            }
        }
        return result;
    }

    /**
     * 全屏绘制指定标签的图片。
     *
     * @param tag 图片标签
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putPictureFullScreen (String tag)
    {
        return putPicture(tag, 0, 0, ScreenSize.WIDTH, ScreenSize.HEIGHT);
    }

    /**
     * 销毁指定标签的普通图片资源，将其移入待销毁队列。
     *
     * @param tag 图片标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposePicture (String tag)
    {
        synchronized (pictureMap)
        {
            try
            {
                Texture texture = pictureMap.remove(tag);
                if (texture != null && texture != errorTexture)
                {
                    disposePictureMap.put(tag, texture);
                    scheduleAsyncDispose();
                    LogUtils.debug(GraphicsManager.class, "disposePicture 图片加入待销毁队列 (tag): " + tag);
                }
                else if (texture == null)
                {
                    LogUtils.error(GraphicsManager.class, "disposePicture 图片不存在 (tag): " + tag);
                }
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(GraphicsManager.class, "disposePicture", e);
                return false;
            }
        }
    }

    // ==================== 背景图片 ====================

    /**
     * 加载背景图片资源。如果该标签已存在，旧纹理加入待销毁队列。
     *
     * @param tag  背景图片标签
     * @param file 背景图片文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadBackgroundPicture (String tag, FileHandle file)
    {
        synchronized (backgroundPictureMap)
        {
            try
            {
                Texture newTexture = getTexture(file);
                if (newTexture == null || newTexture == errorTexture)
                {
                    LogUtils.error(GraphicsManager.class, "loadBackgroundPicture 加载失败，不替换旧图 (tag): " + tag);
                    return false;
                }

                Texture old = backgroundPictureMap.get(tag);
                // 关键：新旧为同一对象则不操作
                if (newTexture == old)
                {
                    return true;
                }

                old = backgroundPictureMap.put(tag, newTexture);
                if (old != null && old != errorTexture)
                {
                    disposeBackgroundPictureMap.remove(tag);
                    disposeBackgroundPictureMap.put(tag, old);
                    scheduleAsyncDispose();
                    LogUtils.debug(GraphicsManager.class, "loadBackgroundPicture 旧背景待销毁 (tag): " + tag);
                }
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(GraphicsManager.class, "loadBackgroundPicture", e);
                return false;
            }
        }
    }

    /**
     * 全屏绘制指定标签的背景图片。
     *
     * @param tag 背景图片标签（为 null 时跳过绘制）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putBackgroundPicture (String tag)
    {
        try
        {
            if (tag == null) return true;
            Texture texture = backgroundPictureMap.get(tag);
            if (texture == null)
            {
                LogUtils.error(GraphicsManager.class, "putBackgroundPicture 背景图片文件不存在 (BackgroundPicture): " + tag + " (map): " + backgroundPictureMap);
                return false;
            }
            spriteBatch.draw(texture, 0, 0, ScreenSize.WIDTH, ScreenSize.HEIGHT);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putBackgroundPicture", e);
            return false;
        }
    }

    /**
     * 销毁指定标签的背景图片资源，将其移入待销毁队列。
     *
     * @param tag 背景图片标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposeBackgroundPicture (String tag)
    {
        synchronized (backgroundPictureMap)
        {
            try
            {
                Texture texture = backgroundPictureMap.remove(tag);
                if (texture != null && texture != errorTexture)
                {
                    disposeBackgroundPictureMap.put(tag, texture);
                    scheduleAsyncDispose();
                    LogUtils.debug(GraphicsManager.class, "disposeBackgroundPicture 背景待销毁 (tag): " + tag);
                }
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(GraphicsManager.class, "disposeBackgroundPicture", e);
                return false;
            }
        }
    }

    // ==================== GIF（帧合并优化） ====================

    /**
     * 加载 GIF 动画资源。将多帧图片合并为单纹理或独立纹理。
     *
     * @param tag      动画标签
     * @param fileList 各帧图片文件列表
     * @param duration 动画总时长（秒）
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadGif (String tag, List<FileHandle> fileList, float duration)
    {
        synchronized (gifMap)
        {
            try
            {
                if (gifMap.containsKey(tag))
                {
                    disposeGif(tag);
                }

                if (fileList == null || fileList.isEmpty())
                {
                    LogUtils.error(GraphicsManager.class, "loadGif 帧文件列表为空 (tag): " + tag);
                    return false;
                }

                List<Pixmap> pixList = new ArrayList<>(fileList.size());
                for (FileHandle file : fileList)
                {
                    if (!FileUtils.isFileExist(file))
                    {
                        LogUtils.error(GraphicsManager.class, "loadGif 帧文件不存在: " + file.path());
                        for (Pixmap p : pixList) p.dispose();
                        return false;
                    }
                    pixList.add(new Pixmap(file));
                }

                Gif gif = createMergedGif(pixList, duration);
                if (gif == null)
                {
                    LogUtils.error(GraphicsManager.class, "loadGif 帧合并失败 (tag): " + tag);
                    for (Pixmap p : pixList) p.dispose();
                    return false;
                }

                gifMap.put(tag, gif);
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(GraphicsManager.class, "loadGif", e);
                return false;
            }
        }
    }

    /**
     * 将多个 Pixmap 帧合并为单纹理，返回对应的 Gif 对象。
     * 如果尺寸超出最大纹理限制，回退为独立纹理（每帧一个纹理）。
     *
     * @param pixList  各帧 Pixmap 列表
     * @param duration 动画总时长（秒）
     * @return 合并后的 Gif 对象，失败返回 null
     */
    private Gif createMergedGif (List<Pixmap> pixList, float duration)
    {
        if (pixList == null || pixList.isEmpty()) return null;

        int maxWidth = 0, totalHeight = 0;
        for (Pixmap p : pixList)
        {
            maxWidth = Math.max(maxWidth, p.getWidth());
            totalHeight += p.getHeight();
        }

        // 检查是否超出设备最大纹理尺寸
        if (maxWidth > maxTextureSize || totalHeight > maxTextureSize)
        {
            LogUtils.debug(GraphicsManager.class,
                "GIF 帧合并后尺寸 " + maxWidth + "x" + totalHeight + " 超出限制 " + maxTextureSize + "，回退为独立纹理");
            return createMultiTextureGif(pixList, duration);
        }

        // 创建大 Pixmap，垂直堆叠粘贴帧
        Pixmap atlasPixmap = new Pixmap(maxWidth, totalHeight, Pixmap.Format.RGBA8888);
        Map<Integer, Rectangle> rectMap = new LinkedHashMap<>();
        int currentY = 0;
        int index = 0;
        for (Pixmap p : pixList)
        {
            atlasPixmap.drawPixmap(p, 0, currentY);
            rectMap.put(index, new Rectangle(0, currentY, p.getWidth(), p.getHeight()));
            currentY += p.getHeight();
            p.dispose();
            index++;
        }

        // 生成合并纹理
        Texture atlasTexture = new Texture(atlasPixmap);
        atlasPixmap.dispose();

        // 生成帧区域
        TextureRegion[] frames = new TextureRegion[pixList.size()];
        for (Map.Entry<Integer, Rectangle> entry : rectMap.entrySet())
        {
            Rectangle r = entry.getValue();
            frames[entry.getKey()] = new TextureRegion(atlasTexture,
                (int) r.x, (int) r.y, (int) r.width, (int) r.height);
        }

        return new Gif(frames, duration, true);
    }

    /**
     * 回退方案：每帧独立纹理。当合并纹理超出设备最大纹理尺寸限制时使用。
     *
     * @param pixList  各帧 Pixmap 列表
     * @param duration 动画总时长（秒）
     * @return 独立纹理的 Gif 对象
     */
    private Gif createMultiTextureGif (List<Pixmap> pixList, float duration)
    {
        TextureRegion[] frames = new TextureRegion[pixList.size()];
        for (int i = 0; i < pixList.size(); i++)
        {
            Texture tex = new Texture(pixList.get(i));
            frames[i] = new TextureRegion(tex);
            pixList.get(i).dispose();
        }
        return new Gif(frames, duration, false);
    }

    /**
     * 在指定位置绘制 GIF 动画的当前帧。
     *
     * @param tag    动画标签
     * @param x      绘制位置 x 坐标
     * @param y      绘制位置 y 坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     * @param delta  距上一帧的时间差（秒）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putGif (String tag, int x, int y, int width, int height, float delta)
    {
        try
        {
            Gif gif = gifMap.get(tag);
            if (gif == null)
            {
                LogUtils.error(GraphicsManager.class, "putGif 动图不存在 (Gif): " + tag);
                return false;
            }
            spriteBatch.draw(gif.getFrame(delta), x, y, width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putGif", e);
            return false;
        }
    }

    /**
     * 根据 GIF 信息对象绘制动画的当前帧。
     *
     * @param gifInfo GIF 信息对象，包含标签、位置和尺寸
     * @param delta   距上一帧的时间差（秒）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putGif (GifInfo gifInfo, float delta)
    {
        return putGif(gifInfo.getTag(), gifInfo.getX(), gifInfo.getY(),
            gifInfo.getWidth(), gifInfo.getHeight(), delta);
    }

    /**
     * 批量绘制多个 GIF 动画的当前帧。
     *
     * @param gifInfoList GIF 信息对象列表
     * @param delta       距上一帧的时间差（秒）
     * @return 全部绘制成功返回 true，否则返回 false
     */
    public boolean putGif (List<GifInfo> gifInfoList, float delta)
    {
        boolean result = true;
        if (gifInfoList != null)
        {
            for (GifInfo info : gifInfoList)
            {
                if (!putGif(info, delta)) result = false;
            }
        }
        return result;
    }

    /**
     * 根据标签列表和 GIF 信息映射批量绘制多个动画的当前帧。
     *
     * @param gifTagList GIF 标签列表
     * @param gifInfoMap 标签到 GIF 信息的映射
     * @param delta      距上一帧的时间差（秒）
     * @return 全部绘制成功返回 true，否则返回 false
     */
    public boolean putGif (List<String> gifTagList, Map<String, GifInfo> gifInfoMap, float delta)
    {
        boolean result = true;
        if (gifTagList != null && gifInfoMap != null)
        {
            for (String tag : gifTagList)
            {
                GifInfo info = gifInfoMap.get(tag);
                if (info != null)
                {
                    if (!putGif(info, delta)) result = false;
                }
                else result = false;
            }
        }
        return result;
    }

    /**
     * 销毁指定标签的 GIF 动画资源，将其移入待销毁队列。
     *
     * @param tag 动画标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposeGif (String tag)
    {
        synchronized (gifMap)
        {
            Gif gif = gifMap.remove(tag);
            if (gif != null)
            {
                disposeGifMap.put(tag, gif);
                scheduleAsyncDispose();
            }
            return true;
        }
    }

    // ==================== 文本与布局 ====================

    /**
     * 在指定位置绘制文本内容。
     *
     * @param content  文本内容
     * @param fontName 字体名称
     * @param size     字体大小
     * @param x        绘制位置 x 坐标
     * @param y        绘制位置 y 坐标（基线）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putText (String content, String fontName, float size, int x, int y)
    {
        try
        {
            BitmapFont font = uiManager.getFont(fontName, size);
            font.draw(spriteBatch, content, x, y + font.getCapHeight());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putText", e);
            return false;
        }
    }

    /**
     * 使用主题默认字体在指定位置绘制文本内容。
     *
     * @param content 文本内容
     * @param size    字体大小
     * @param x       绘制位置 x 坐标
     * @param y       绘制位置 y 坐标（基线）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putText (String content, float size, int x, int y)
    {
        return putText(content, themeManager.getFont(), size, x, y);
    }

    /**
     * 根据布局配置绘制完整的界面层（背景图片、普通图片和 GIF 动画）。
     *
     * @param layout 布局配置对象
     * @param delta        距上一帧的时间差（秒）
     * @return 绘制成功返回 true，失败返回 false
     */
    public boolean putLayout (Layout layout, float delta)
    {
        try
        {
            if (layout != null)
            {
                if (!putBackgroundPicture(layout.getBackgroundPicture())) return false;
                if (!putPicture(new ArrayList<>(layout.getPictureMap().values()))) return false;
                if (!putGif(new ArrayList<>(layout.getGifMap().values()), delta)) return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "putLayout", e);
            return false;
        }
    }

    // ==================== 异步销毁 ====================

    /**
     * 安排异步销毁任务，延迟 50 毫秒后执行。使用 AtomicBoolean 确保同一时间只调度一次。
     */
    private void scheduleAsyncDispose ()
    {
        if (disposeScheduled.compareAndSet(false, true))
        {
            disposeExecutor.schedule(() ->
            {
                if (Gdx.app != null)
                {
                    SafePostRunnable.post(() ->
                    {
                        executeAsyncDispose();
                        disposeScheduled.set(false);
                    });
                }
                else
                {
                    executeAsyncDispose();
                    disposeScheduled.set(false);
                }
            }, Numeric.Time.DISPOSE_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 执行异步销毁操作，清理待销毁队列中的普通图片、背景图片和 GIF 资源。
     */
    private void executeAsyncDispose ()
    {
        // 普通图片
        if (!disposePictureMap.isEmpty())
        {
            for (Texture tex : disposePictureMap.values())
            {
                if (tex != null && tex != errorTexture)
                {
                    tex.dispose();
                    removeFromTextureCache(tex);
                }
            }
            disposePictureMap.clear();
        }

        // 背景图片
        if (!disposeBackgroundPictureMap.isEmpty())
        {
            for (Texture tex : disposeBackgroundPictureMap.values())
            {
                if (tex != null && tex != errorTexture)
                {
                    tex.dispose();
                    removeFromTextureCache(tex);
                }
            }
            disposeBackgroundPictureMap.clear();
        }

        // GIF
        if (!disposeGifMap.isEmpty())
        {
            for (Gif gif : disposeGifMap.values())
            {
                gif.dispose(errorTexture);
            }
            disposeGifMap.clear();
        }

        LogUtils.debug(GraphicsManager.class, "executeAsyncDispose 完成");
    }

    /**
     * 从纹理缓存中安全移除已销毁的纹理，避免并发问题。
     *
     * @param tex 要移除的纹理对象
     */
    private void removeFromTextureCache (Texture tex)
    {
        if (tex == null) return;
        // ConcurrentHashMap 的 entrySet 迭代并删除是安全的
        for (Map.Entry<String, Texture> entry : textureCache.entrySet())
        {
            if (entry.getValue() == tex)
            {
                textureCache.remove(entry.getKey());
                break; // 每个纹理在缓存中只出现一次，找到即可停止
            }
        }
    }

    // ==================== 全局销毁 ====================

    /**
     * 销毁所有图形资源，将活跃资源移入销毁队列执行异步清理，并关闭销毁线程池。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean dispose ()
    {
        try
        {
            // 将所有活跃资源移入销毁队列
            for (Texture tex : pictureMap.values())
            {
                if (tex != null && tex != errorTexture)
                {
                    disposePictureMap.put(String.valueOf(tex.hashCode()), tex);
                }
            }
            pictureMap.clear();

            for (Texture tex : backgroundPictureMap.values())
            {
                if (tex != null && tex != errorTexture)
                {
                    disposeBackgroundPictureMap.put(String.valueOf(tex.hashCode()), tex);
                }
            }
            backgroundPictureMap.clear();

            disposeGifMap.putAll(gifMap);
            gifMap.clear();

            // 执行销毁
            if (Gdx.app != null)
            {
                SafePostRunnable.post(this::executeAsyncDispose);
            }
            else
            {
                executeAsyncDispose();
            }

            // 清理错误纹理
            if (errorTexture != null)
            {
                errorTexture.dispose();
                errorTexture = null;
            }

            // 清理所有缓存纹理
            for (Texture tex : textureCache.values())
            {
                if (tex != null && tex != errorTexture)
                {
                    tex.dispose();
                }
            }
            textureCache.clear();

            disposeExecutor.shutdown();
            try
            {
                if (!disposeExecutor.awaitTermination(Numeric.Time.ASYNC_TERMINATE_WAIT_SECONDS, TimeUnit.SECONDS)) disposeExecutor.shutdownNow();
            }
            catch (InterruptedException e)
            {
                disposeExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            LogUtils.debug(GraphicsManager.class, "dispose 完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(GraphicsManager.class, "dispose", e);
            return false;
        }
    }
}

/**
 * GIF 动画封装（支持合并纹理与独立纹理）
 */
final class Gif
{
    private final Animation<TextureRegion> animation;
    private final boolean mergedTexture;
    private final Texture atlasTexture;
    private float time;

    /**
     * 构造 GIF 动画对象。
     *
     * @param frames   帧序列
     * @param duration 动画总时长（秒）
     * @param merged   是否为合并纹理模式
     */
    public Gif (TextureRegion[] frames, float duration, boolean merged)
    {
        animation = new Animation<>(duration / frames.length, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        this.time = 0f;
        this.mergedTexture = merged;
        this.atlasTexture = merged && frames.length > 0 ? frames[0].getTexture() : null;
    }

    /**
     * 获取当前帧纹理区域，并累加时间进度。
     *
     * @param delta 距上一帧的时间差（秒）
     * @return 当前帧纹理区域
     */
    public TextureRegion getFrame (float delta)
    {
        time += delta;
        return animation.getKeyFrame(time);
    }

    /**
     * 销毁 GIF 动画的纹理资源。合并纹理模式直接销毁合图纹理，
     * 独立纹理模式遍历去重后逐个销毁。
     *
     * @param errorTexture 错误纹理，在销毁时需要跳过避免误销毁
     */
    public void dispose (Texture errorTexture)
    {
        if (animation == null) return;

        if (mergedTexture && atlasTexture != null)
        {
            if (atlasTexture != errorTexture)
            {
                atlasTexture.dispose();
            }
        }
        else
        {
            Set<Texture> unique = new HashSet<>();
            for (TextureRegion region : animation.getKeyFrames())
            {
                if (region != null && region.getTexture() != null)
                {
                    unique.add(region.getTexture());
                }
            }
            for (Texture t : unique)
            {
                if (t != errorTexture)
                {
                    t.dispose();
                }
            }
        }
        LogUtils.debug(GraphicsManager.class, "dispose 完成");
    }
}
