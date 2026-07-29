package com.hujiugame.qingfeng.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.manager.UserConfigManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AudioManager
{
    // ===================================================================================================================
    // Sound — 音效。一次性播放，不追踪"当前播放"状态。
    //   soundLoadedObjectMap: 加载的 Sound 对象
    //   soundLoadedPathMap:   加载的文件路径（用于同文件跳过优化）
    // ===================================================================================================================
    private final Map<String, Sound> soundLoadedObjectMap = new HashMap<>();
    private final Map<String, String> soundLoadedPathMap = new HashMap<>();

    // ===================================================================================================================
    // Music（普通音乐）— 四张 Map 共同管理同一个 tag：
    //
    //   loadedObjectMap   tag → 最新加载的 Music 对象（"预备队列"）
    //     调用 loadMusic 时写入。只载入资源，不碰正在播放的对象。
    //     如果旧 loadedObject 与当前 playingObject 引用不同，
    //     说明该对象是"预加载但从未切换到播放"的孤儿，在此清理。
    //
    //   loadedPathMap     tag → 最新加载的文件路径
    //     调用 loadMusic 时同步写入。用于同文件跳过优化，以及与 playingPathMap 比对。
    //
    //   playingObjectMap  tag → 当前在播的 Music 对象（"播放队列"）
    //     三种操作路径：
    //       a. playMusic 检测到 loadedPath != playingPath → 停止旧 playingObject，
    //          将 loadedObject 提升为此，执行 play()
    //       b. playMusic 检测到路径相同且未播 → 直接用 playingObject 执行 play()
    //       c. playMusic 检测到路径相同且正在播 → 不做任何事
    //     stop/pause/resume 等操作统一以此表为准。
    //
    //   playingPathMap    tag → 当前在播的文件路径
    //     与 playingObjectMap 同步更新，用于与 loadedPathMap 比对。
    //
    //  核心生命周期：
    //     load("A")                  → loadedObject = musicA, playingObject = （无）
    //     play()                     → loadedPath == playingPath（首次）→ 启动
    //                                   loadedObject(musicA) 提升为 playingObject
    //     load("B") [正在播 A]       → loadedObject = musicB,
    //                                   playingObject = musicA（不变，继续播）
    //     play() 检测到 path 不一致  → stop A, dispose A, play B
    //                                   loadedObject(musicB) 提升为 playingObject
    //     load("C") [B 已提升为 play] → oldLoaded(musicB) == playingObject(musicB)
    //                                   引用相同 → 不清理
    //     load("D") [从未 play C]     → oldLoaded(musicC) != playingObject(musicB)
    //                                   引用不同 → 清理孤儿 musicC
    // ===================================================================================================================
    private final Map<String, Music> musicLoadedObjectMap = new HashMap<>();
    private final Map<String, String> musicLoadedPathMap = new HashMap<>();
    private final Map<String, Music> musicPlayingObjectMap = new HashMap<>();
    private final Map<String, String> musicPlayingPathMap = new HashMap<>();

    // ===================================================================================================================
    // Background Music（背景音乐）— 四张 Map 结构与普通 Music 完全一致
    //
    //   bgMusicLoadedObjectMap  tag → 最新加载的 Music（预备队列）
    //   bgMusicLoadedPathMap    tag → 最新加载的文件路径
    //   bgMusicPlayingObjectMap tag → 当前在播的 Music（播放队列）
    //   bgMusicPlayingPathMap   tag → 当前在播的文件路径
    //
    //  ！！两套音乐的区分：
    //     普通 Music（如 BGM 列表内的曲目）    与背景音乐 bgMusic 使用完全相同的机制，
    //     但彼此独立存储。互不干扰。
    // ===================================================================================================================
    private final Map<String, Music> bgMusicLoadedObjectMap = new HashMap<>();
    private final Map<String, String> bgMusicLoadedPathMap = new HashMap<>();
    private final Map<String, Music> bgMusicPlayingObjectMap = new HashMap<>();
    private final Map<String, String> bgMusicPlayingPathMap = new HashMap<>();

    // 待销毁音频资源
    private final Map<String, Sound> disposeSoundMap = new HashMap<>();
    private final Map<String, Music> disposeMusicMap = new HashMap<>();
    private final Map<String, Music> disposeBgMusicMap = new HashMap<>();

    // 后台销毁线程池
    private final ScheduledExecutorService disposeExecutor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        Thread thread = new Thread(r, "AudioManager-Dispose-Thread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) ->
            LogUtils.error(AudioManager.class, "Dispose线程异常", (Exception) e));
        return thread;
    });

    private final UserConfigManager userConfigManager;

    // 默认音量
    private float totalVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float soundVolume = 1.0f;

    // ===================================================================================================================

    /**
     * 构造音频管理器。
     *
     * @param userConfigManager 用户配置管理器，用于读取音量设置
     */
    public AudioManager (UserConfigManager userConfigManager)
    {
        this.userConfigManager = userConfigManager;
    }

    /**
     * 从用户配置中加载音量设置（总音量、音效音量、音乐音量）。
     *
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadSoundVolumeFromUserConfig ()
    {
        try
        {
            setTotalVolume(userConfigManager.getSoundVolumeTotal());
            setSoundVolume(userConfigManager.getSoundVolumeSound());
            setMusicVolume(userConfigManager.getSoundVolumeMusic());
            // 背景音乐音量可以从配置读取，如果没有则默认与 musicVolume 相同
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "loadSoundVolumeFromUserConfig", e);
            return false;
        }
    }

    /**
     * 初始化音频管理器，从用户配置中加载音量设置。
     *
     * @return 初始化成功返回 true，失败返回 false
     */
    public boolean init ()
    {
        try
        {
            return loadSoundVolumeFromUserConfig();
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "init", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Sound 方法
    // ===================================================================================================================

    /**
     * 加载音效资源。如果该标签已存在且文件路径不同，则旧资源加入待销毁队列。
     *
     * @param tag  音效标签
     * @param file 音效文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadSound (String tag, FileHandle file)
    {
        try
        {
            synchronized (soundLoadedObjectMap)
            {
                // 同文件跳过优化：文件路径相同则不做任何操作
                if (soundLoadedPathMap.containsKey(tag) && soundLoadedPathMap.get(tag).equals(file.path()))
                {
                    LogUtils.debug(AudioManager.class, "loadSound (tag): " + tag + " 已存在，文件相同，不重复加载");
                    return true;
                }

                // 文件路径不同：清理旧资源，加载新资源
                if (soundLoadedObjectMap.containsKey(tag))
                {
                    disposeSound(tag);
                    LogUtils.debug(AudioManager.class, "loadSound (tag):" + tag + " 已存在，旧资源加入待销毁队列");
                }

                if (FileUtils.isFileExist(file))
                {
                    Sound sound = Gdx.audio.newSound(file);
                    soundLoadedObjectMap.put(tag, sound);
                    soundLoadedPathMap.put(tag, file.path());
                    LogUtils.debug(AudioManager.class, "loadSound 加载sound (tag): " + tag + " (file): " + file.path());
                }
                else
                {
                    soundLoadedObjectMap.put(tag, null);
                    soundLoadedPathMap.put(tag, null);
                    LogUtils.error(AudioManager.class, "loadSound 加载空音频sound (tag): " + tag + " (file): " + file.path() + " 文件不存在");
                }
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "loadSound", e);
            return false;
        }
    }

    /**
     * 播放指定标签的音效。
     *
     * @param tag    音效标签
     * @param volume 播放音量（0.0 ~ 1.0）
     * @param loop   是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playSound (String tag, float volume, boolean loop)
    {
        try
        {
            Sound sound;
            String filePath;
            synchronized (soundLoadedObjectMap)
            {
                sound = soundLoadedObjectMap.get(tag);
                filePath = soundLoadedPathMap.get(tag);
            }

            if (sound != null)
            {
                float validVolume = clampVolume(volume) * totalVolume;
                long soundId = sound.play();
                sound.setVolume(soundId, validVolume);
                sound.setLooping(soundId, loop);
                LogUtils.debug(AudioManager.class, "playSound 播放sound (tag): " + tag + " (file): " + filePath);
            }
            else
            {
                LogUtils.debug(AudioManager.class, "playSound 播放sound (tag): " + tag + " sound不存在");
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "playSound", e);
            return false;
        }
    }

    /**
     * 以默认音效音量播放指定标签的音效。
     *
     * @param tag  音效标签
     * @param loop 是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playSound (String tag, boolean loop)
    {
        return playSound(tag, soundVolume, loop);
    }

    /**
     * 暂停指定标签的音效。
     *
     * @param tag 音效标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean pauseSound (String tag)
    {
        try
        {
            Sound sound;
            synchronized (soundLoadedObjectMap)
            {
                sound = soundLoadedObjectMap.get(tag);
            }
            if (sound != null)
            {
                sound.pause();
                LogUtils.debug(AudioManager.class, "pauseSound 暂停sound (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "pauseSound", e);
            return false;
        }
    }

    /**
     * 恢复播放指定标签的音效。
     *
     * @param tag 音效标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean resumeSound (String tag)
    {
        try
        {
            Sound sound;
            synchronized (soundLoadedObjectMap)
            {
                sound = soundLoadedObjectMap.get(tag);
            }
            if (sound != null)
            {
                sound.resume();
                LogUtils.debug(AudioManager.class, "resumeSound 恢复sound (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "resumeSound", e);
            return false;
        }
    }

    /**
     * 停止播放指定标签的音效。
     *
     * @param tag 音效标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopSound (String tag)
    {
        try
        {
            Sound sound;
            synchronized (soundLoadedObjectMap)
            {
                sound = soundLoadedObjectMap.get(tag);
            }
            if (sound != null)
            {
                sound.stop();
                LogUtils.debug(AudioManager.class, "stopSound 停止sound (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopSound", e);
            return false;
        }
    }

    /**
     * 停止所有正在播放的音效。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopAllSound ()
    {
        try
        {
            synchronized (soundLoadedObjectMap)
            {
                for (Sound sound : soundLoadedObjectMap.values())
                {
                    if (sound != null)
                    {
                        sound.stop();
                    }
                }
            }
            LogUtils.debug(AudioManager.class, "stopAllSound 已停止所有音效");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopAllSound", e);
            return false;
        }
    }

    /**
     * 销毁指定标签的音效资源，将其移入待销毁队列。
     *
     * @param tag 音效标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposeSound (String tag)
    {
        try
        {
            Sound sound;
            String filePath;
            synchronized (soundLoadedObjectMap)
            {
                sound = soundLoadedObjectMap.remove(tag);
                filePath = soundLoadedPathMap.remove(tag);
            }
            if (sound != null)
            {
                sound.stop();
                synchronized (disposeSoundMap)
                {
                    disposeSoundMap.put(tag, sound);
                }
                LogUtils.debug(AudioManager.class, "disposeSound 销毁sound (tag): " + tag + " (file): " + filePath);
                triggerAsyncDispose();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "disposeSound", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Music 方法（普通音乐）
    // ===================================================================================================================

    /**
     * 加载音乐资源。<br>
     * <br>
     * 只更新 Loaded 表，不碰 Playing 表，不停止任何正在播放的音乐。
     * 如需切换播放源，由 {@link #playMusic(String, float, boolean)} 在检测到路径不一致时执行停止和切换。
     *
     * @param tag  音乐标签
     * @param file 音乐文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadMusic (String tag, FileHandle file)
    {
        try
        {
            synchronized (musicLoadedObjectMap)
            {
                // 同文件跳过优化：文件路径相同则不做任何操作
                String currentLoadedPath = musicLoadedPathMap.get(tag);
                if (currentLoadedPath != null && currentLoadedPath.equals(file.path()))
                {
                    LogUtils.debug(AudioManager.class, "loadMusic (tag): " + tag + " 已存在，文件相同，不重复加载");
                    return true;
                }

                // ============================================================
                // 清理"预加载但未切换"的孤儿 loadedObject
                // ============================================================
                // 在最新的 loadedObject 被重新加载前，需要检查旧的 loadedObject
                // 是否已被 playMusic 提升为 playingObject：
                //
                //   情景 1：oldLoaded == currentPlaying（引用相同）
                //     说明 playMusic 上次已执行切换，两者是同一个 Music 对象。
                //     当前正在播放中，不能清理。
                //
                //   情景 2：oldLoaded != currentPlaying（引用不同）
                //     说明 playMusic 尚未执行切换，oldLoaded 是"预加载孤儿"。
                //     例如：load(A) → play(A) → load(B)【不 play】→ load(C)
                //     此时 oldLoaded(B) 未被播放过，currentPlaying(A) 仍在播，
                //     清理 oldLoaded(B)。
                //
                //  ⚠ 注意：情景 2 中的 currentPlaying 不会被清理（正在播放），
                //    它在 playMusic 被调用切换到新资源时才会停止。
                // ============================================================
                Music oldLoaded = musicLoadedObjectMap.get(tag);
                Music currentPlaying;
                synchronized (musicPlayingObjectMap)
                {
                    currentPlaying = musicPlayingObjectMap.get(tag);
                }
                if (oldLoaded != null && oldLoaded != currentPlaying)
                {
                    oldLoaded.stop();
                    synchronized (disposeMusicMap)
                    {
                        disposeMusicMap.put(tag, oldLoaded);
                    }
                    LogUtils.debug(AudioManager.class, "loadMusic (tag): " + tag + " 清理预加载孤儿资源");
                }

                // 加载新资源
                if (FileUtils.isFileExist(file))
                {
                    Music music = Gdx.audio.newMusic(file);
                    musicLoadedObjectMap.put(tag, music);
                    musicLoadedPathMap.put(tag, file.path());
                    LogUtils.debug(AudioManager.class, "loadMusic 加载music (tag): " + tag + " (file): " + file.path());
                }
                else
                {
                    musicLoadedObjectMap.put(tag, null);
                    musicLoadedPathMap.put(tag, null);
                    LogUtils.error(AudioManager.class, "loadMusic 加载空音频music (tag): " + tag + " (file): " + file.path() + " 文件不存在");
                }
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "loadMusic", e);
            return false;
        }
    }

    /**
     * 播放指定标签的音乐。<br>
     * <br>
     * 当检测到 loadedPath 与 playingPath 不一致时，说明文件已被重新加载但未切换播放。
     * 此时停止旧 playingObject 并加入销毁队列，将新 loadedObject 提升为 playingObject 并播放。
     * 如果路径一致，则检查当前播放状态，未播则 resume。
     *
     * @param tag    音乐标签
     * @param volume 播放音量（0.0 ~ 1.0）
     * @param loop   是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playMusic (String tag, float volume, boolean loop)
    {
        try
        {
            if (tag == null) return stopAllMusic();

            String loadedPath;
            String playingPath;
            Music loadedObject;
            Music playingObject;

            synchronized (musicLoadedObjectMap)
            {
                loadedPath = musicLoadedPathMap.get(tag);
                loadedObject = musicLoadedObjectMap.get(tag);
            }
            synchronized (musicPlayingObjectMap)
            {
                playingPath = musicPlayingPathMap.get(tag);
                playingObject = musicPlayingObjectMap.get(tag);
            }

            if (loadedObject == null)
            {
                LogUtils.debug(AudioManager.class, "playMusic 播放music (tag): " + tag + " music不存在");
                return true;
            }

            // ================================================================
            // 检测是否需要切换播放源
            // ================================================================
            // playingPath == null                         → 从未播放过，首次启动
            // playingPath != null 且 loadedPath != playingPath → 文件已重新加载，切换
            // playingPath != null 且 loadedPath == playingPath → 同一资源，按状态处理
            // ================================================================
            boolean needSwitch = (playingPath == null)
                || (loadedPath != null && !loadedPath.equals(playingPath));

            if (needSwitch)
            {
                // 切换播放源：停止旧播放，启动新播放
                // 此时 playingObject 是 loadMusic 时未被清理的资源：
                //   - 如果 play 上次已被调用，playingObject 是上次提升的那个对象
                //   - 如果 play 从未被调用，playingObject 为 null
                if (playingObject != null)
                {
                    playingObject.stop();
                    synchronized (disposeMusicMap)
                    {
                        // 使用 tag + ".current" 作为 key，防止与 loadedObject 同 key 冲突
                        disposeMusicMap.put(tag + ".current", playingObject);
                    }
                }

                // 将 loadedObject 提升为 playingObject，正式播放
                float validVolume = clampVolume(volume) * totalVolume;
                loadedObject.setVolume(validVolume);
                loadedObject.setLooping(loop);
                loadedObject.play();

                synchronized (musicPlayingObjectMap)
                {
                    musicPlayingObjectMap.put(tag, loadedObject);
                    musicPlayingPathMap.put(tag, loadedPath);
                }
                LogUtils.debug(AudioManager.class, "playMusic 播放music (tag): " + tag + " (file): " + loadedPath);
            }
            else
            {
                // 同一资源：检查播放状态
                if (!loadedObject.isPlaying())
                {
                    float validVolume = clampVolume(volume) * totalVolume;
                    loadedObject.setVolume(validVolume);
                    loadedObject.setLooping(loop);
                    loadedObject.play();
                    LogUtils.debug(AudioManager.class, "playMusic 恢复music (tag): " + tag);
                }
                // 已在播放中，什么都不做
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "playMusic", e);
            return false;
        }
    }

    /**
     * 以默认音乐音量播放指定标签的音乐。
     *
     * @param tag  音乐标签
     * @param loop 是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playMusic (String tag, boolean loop)
    {
        return playMusic(tag, musicVolume, loop);
    }

    /**
     * 暂停指定标签的音乐。<br>
     * <br>
     * 操作的是当前在播的 Music 对象（playingObjectMap），不影响已加载但未切换的资源。
     *
     * @param tag 音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean pauseMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (musicPlayingObjectMap)
            {
                music = musicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                music.pause();
                LogUtils.debug(AudioManager.class, "pauseMusic 暂停music (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "pauseMusic", e);
            return false;
        }
    }

    /**
     * 恢复播放指定标签的音乐。
     *
     * @param tag 音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean resumeMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (musicPlayingObjectMap)
            {
                music = musicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                if (!music.isPlaying())
                    music.play();
                LogUtils.debug(AudioManager.class, "resumeMusic 恢复music (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "resumeMusic", e);
            return false;
        }
    }

    /**
     * 停止播放指定标签的音乐。
     *
     * @param tag 音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (musicPlayingObjectMap)
            {
                music = musicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                if (music.isPlaying()) LogUtils.debug(AudioManager.class, "stopMusic 停止music (tag): " + tag);
                music.stop();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopMusic", e);
            return false;
        }
    }

    /**
     * 停止所有正在播放的音乐。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopAllMusic ()
    {
        try
        {
            boolean isStopped = true;
            synchronized (musicPlayingObjectMap)
            {
                for (Music music : musicPlayingObjectMap.values())
                {
                    if (music != null)
                    {
                        if (music.isPlaying()) isStopped = false;
                        music.stop();
                    }
                }
            }
            if (!isStopped) LogUtils.debug(AudioManager.class, "stopAllMusic 已停止所有音乐");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopAllMusic", e);
            return false;
        }
    }

    /**
     * 销毁指定标签的音乐资源，将其移入待销毁队列。<br>
     * <br>
     * 清理 Loaded 和 Playing 全部四张表。
     * 如果 LoadedObject 与 PlayingObject 引用不同（预加载但未切换），
     * 两者分别加入销毁队列，确保不泄漏。
     *
     * @param tag 音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposeMusic (String tag)
    {
        try
        {
            Music loadedObject;
            Music playingObject;
            String loadedPath;

            synchronized (musicLoadedObjectMap)
            {
                loadedObject = musicLoadedObjectMap.remove(tag);
                loadedPath = musicLoadedPathMap.remove(tag);
            }
            synchronized (musicPlayingObjectMap)
            {
                playingObject = musicPlayingObjectMap.remove(tag);
                musicPlayingPathMap.remove(tag);
            }

            // 停止并销毁 loadedObject（"预备队列"中的资源）
            if (loadedObject != null)
            {
                loadedObject.stop();
                synchronized (disposeMusicMap)
                {
                    disposeMusicMap.put(tag, loadedObject);
                }
            }

            // 停止并销毁 playingObject（"播放队列"中的资源）
            // 仅在 loadedObject 与 playingObject 引用不同时执行，
            // 因为引用相同时它们就是同一个对象，上面已经销毁过了。
            if (playingObject != null && playingObject != loadedObject)
            {
                playingObject.stop();
                synchronized (disposeMusicMap)
                {
                    // 使用 tag + ".playing" 作为 key，防止覆盖上面已入队但 key 相同的 loadedObject
                    disposeMusicMap.put(tag + ".playing", playingObject);
                }
            }

            if (loadedPath != null)
            {
                LogUtils.debug(AudioManager.class, "disposeMusic 销毁music (tag): " + tag + " (file): " + loadedPath);
                triggerAsyncDispose();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "disposeMusic", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Background Music 方法（背景音乐）
    // ===================================================================================================================

    /**
     * 加载背景音乐资源。<br>
     * <br>
     * 只更新 Loaded 表，不碰 Playing 表，不停止任何正在播放的背景音乐。
     * 切换播放源由 {@link #playBackgroundMusic(String, float, boolean)} 在检测到路径不一致时执行。
     *
     * @param tag  背景音乐标签
     * @param file 背景音乐文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    public boolean loadBackgroundMusic (String tag, FileHandle file)
    {
        try
        {
            synchronized (bgMusicLoadedObjectMap)
            {
                // 同文件跳过优化：文件路径相同则不做任何操作
                String currentLoadedPath = bgMusicLoadedPathMap.get(tag);
                if (currentLoadedPath != null && currentLoadedPath.equals(file.path()))
                {
                    LogUtils.debug(AudioManager.class, "loadBackgroundMusic (tag): " + tag + " 已存在，文件相同，不重复加载");
                    return true;
                }

                // ============================================================
                // 清理"预加载但未切换"的孤儿 loadedObject
                // 逻辑与 musicLoadedObjectMap 完全一致，详见 loadMusic 注释。
                // ============================================================
                Music oldLoaded = bgMusicLoadedObjectMap.get(tag);
                Music currentPlaying;
                synchronized (bgMusicPlayingObjectMap)
                {
                    currentPlaying = bgMusicPlayingObjectMap.get(tag);
                }
                if (oldLoaded != null && oldLoaded != currentPlaying)
                {
                    oldLoaded.stop();
                    synchronized (disposeBgMusicMap)
                    {
                        disposeBgMusicMap.put(tag, oldLoaded);
                    }
                    LogUtils.debug(AudioManager.class, "loadBackgroundMusic (tag): " + tag + " 清理预加载孤儿资源");
                }

                // 加载新资源
                if (FileUtils.isFileExist(file))
                {
                    Music music = Gdx.audio.newMusic(file);
                    music.setOnCompletionListener(m ->
                    {
                        synchronized (bgMusicPlayingObjectMap)
                        {
                            if (bgMusicPlayingObjectMap.get(tag) == m)
                            {
                                bgMusicPlayingObjectMap.remove(tag);
                                bgMusicPlayingPathMap.remove(tag);
                                LogUtils.debug(AudioManager.class,
                                    "loadBackgroundMusic 背景音乐自然播放完毕 (tag): " + tag + " 已从播放记录中移除，允许 playLayout 随机下一首");
                            }
                        }
                    });
                    bgMusicLoadedObjectMap.put(tag, music);
                    bgMusicLoadedPathMap.put(tag, file.path());
                    LogUtils.debug(AudioManager.class, "loadBackgroundMusic 加载背景音乐 (tag): " + tag + " (file): " + file.path());
                }
                else
                {
                    bgMusicLoadedObjectMap.put(tag, null);
                    bgMusicLoadedPathMap.put(tag, null);
                    LogUtils.error(AudioManager.class, "loadBackgroundMusic 加载空背景音乐 (tag): " + tag + " (file): " + file.path() + " 文件不存在");
                }
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "loadBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 播放指定标签的背景音乐。如果 tag 为 null，则停止所有背景音乐。<br>
     * <br>
     * 当 detected 到 loadedPath 与 playingPath 不一致时，说明文件已被重新加载但未切换播放。
     * 此时停止旧 playingObject 并加入销毁队列，将新 loadedObject 提升为 playingObject 并播放。
     * 如果路径一致，则检查当前播放状态，未播则 resume。
     *
     * @param tag    背景音乐标签（为 null 时停止所有背景音乐）
     * @param volume 播放音量（0.0 ~ 1.0）
     * @param loop   是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playBackgroundMusic (String tag, float volume, boolean loop)
    {
        try
        {
            if (tag == null) return stopAllBackgroundMusic();

            String loadedPath;
            String playingPath;
            Music loadedObject;
            Music playingObject;

            synchronized (bgMusicLoadedObjectMap)
            {
                loadedPath = bgMusicLoadedPathMap.get(tag);
                loadedObject = bgMusicLoadedObjectMap.get(tag);
            }
            synchronized (bgMusicPlayingObjectMap)
            {
                playingPath = bgMusicPlayingPathMap.get(tag);
                playingObject = bgMusicPlayingObjectMap.get(tag);
            }

            if (loadedObject == null)
            {
                LogUtils.debug(AudioManager.class, "playBackgroundMusic 播放背景音乐 (tag): " + tag + " 背景音乐不存在");
                return true;
            }

            // ================================================================
            // 检测是否需要切换播放源
            // 逻辑与 playMusic 完全一致，详见该处注释。
            // ================================================================
            boolean needSwitch = (playingPath == null)
                || (loadedPath != null && !loadedPath.equals(playingPath));

            if (needSwitch)
            {
                // 停止旧播放对象
                if (playingObject != null)
                {
                    playingObject.stop();
                    synchronized (disposeBgMusicMap)
                    {
                        disposeBgMusicMap.put(tag + ".current", playingObject);
                    }
                }

                // 启动新加载对象
                float validVolume = clampVolume(volume) * totalVolume;
                loadedObject.setVolume(validVolume);
                loadedObject.setLooping(loop);
                loadedObject.play();

                synchronized (bgMusicPlayingObjectMap)
                {
                    bgMusicPlayingObjectMap.put(tag, loadedObject);
                    bgMusicPlayingPathMap.put(tag, loadedPath);
                }
                LogUtils.debug(AudioManager.class, "playBackgroundMusic 播放背景音乐 (tag): " + tag + " (file): " + loadedPath);
            }
            else
            {
                // 同一资源，检查播放状态
                if (!loadedObject.isPlaying())
                {
                    float validVolume = clampVolume(volume) * totalVolume;
                    loadedObject.setVolume(validVolume);
                    loadedObject.setLooping(loop);
                    loadedObject.play();
                    LogUtils.debug(AudioManager.class, "playBackgroundMusic 恢复背景音乐 (tag): " + tag);
                }
                // 已在播放中，什么都不做
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "playBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 以默认音乐音量播放指定标签的背景音乐。
     *
     * @param tag  背景音乐标签
     * @param loop 是否循环播放
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playBackgroundMusic (String tag, boolean loop)
    {
        return playBackgroundMusic(tag, musicVolume, loop);
    }

    /**
     * 暂停指定标签的背景音乐。<br>
     * <br>
     * 操作的是当前在播的 Music 对象（playingObjectMap），不影响已加载但未切换的资源。
     *
     * @param tag 背景音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean pauseBackgroundMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (bgMusicPlayingObjectMap)
            {
                music = bgMusicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                music.pause();
                LogUtils.debug(AudioManager.class, "pauseBackgroundMusic 暂停背景音乐 (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "pauseBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 恢复播放指定标签的背景音乐。
     *
     * @param tag 背景音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean resumeBackgroundMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (bgMusicPlayingObjectMap)
            {
                music = bgMusicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                if (!music.isPlaying())
                    music.play();
                LogUtils.debug(AudioManager.class, "resumeBackgroundMusic 恢复背景音乐 (tag): " + tag);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "resumeBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 停止播放指定标签的背景音乐。
     *
     * @param tag 背景音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopBackgroundMusic (String tag)
    {
        try
        {
            Music music;
            synchronized (bgMusicPlayingObjectMap)
            {
                music = bgMusicPlayingObjectMap.get(tag);
            }
            if (music != null)
            {
                if (music.isPlaying()) LogUtils.debug(AudioManager.class, "stopBackgroundMusic 停止背景音乐 (tag): " + tag);
                music.stop();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 停止所有正在播放的背景音乐。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean stopAllBackgroundMusic ()
    {
        try
        {
            boolean isStopped = true;
            synchronized (bgMusicPlayingObjectMap)
            {
                for (Music bgMusic : bgMusicPlayingObjectMap.values())
                {
                    if (bgMusic != null)
                    {
                        if (bgMusic.isPlaying()) isStopped = false;
                        bgMusic.stop();
                    }
                }
            }
            if (!isStopped) LogUtils.debug(AudioManager.class, "stopAllBackgroundMusic 已停止所有背景音乐");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopAllBackgroundMusic", e);
            return false;
        }
    }

    /**
     * 根据布局配置播放对应的背景音乐。<br>
     * <br>
     * 从背景音乐列表中检测是否有曲目正在播放：
     * <ul>
     *   <li>有 → 保持当前播放，不重新随机（避免 {@code render()} 每帧切换导致音频异常）</li>
     *   <li>无 → 随机选一首播放</li>
     * </ul>
     *
     * @param layout 布局配置对象
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean playLayout (Layout layout)
    {
        try
        {
            if (layout != null)
            {
                List<String> bgmList = layout.getBackgroundMusicList();
                if (bgmList != null && !bgmList.isEmpty())
                {
                    // 检查列表中是否有曲目已在播放记录中（避免每帧切换）
                    // 注意：不用 Music.isPlaying() 判断，因为 Android 生命周期后
                    // 该状态不可靠。用 playingObjectMap 的 containsKey 代替——
                    // BGM 只要通过 playBackgroundMusic 启动过就会在该表中登记，
                    // 显式停止/销毁时才移除。不受 native 音频状态影响。
                    synchronized (bgMusicPlayingObjectMap)
                    {
                        for (String tag : bgmList)
                        {
                            if (bgMusicPlayingObjectMap.containsKey(tag))
                            {
                                // 已有曲目在播，保持当前播放不切换
                                return true;
                            }
                        }
                    }

                    // 没有曲目在播：随机选一首播放
                    String selected = bgmList.get(MathUtils.random(bgmList.size() - 1));
                    return playBackgroundMusic(selected, false);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "playLayout", e);
            return false;
        }
    }

    /**
     * 销毁指定标签的背景音乐资源，将其移入待销毁队列。<br>
     * <br>
     * 清理 Loaded 和 Playing 全部四张表。
     * 如果 LoadedObject 与 PlayingObject 引用不同（预加载但未切换），
     * 两者分别加入销毁队列，确保不泄漏。
     *
     * @param tag 背景音乐标签
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean disposeBackgroundMusic (String tag)
    {
        try
        {
            Music loadedObject;
            Music playingObject;
            String loadedPath;

            synchronized (bgMusicLoadedObjectMap)
            {
                loadedObject = bgMusicLoadedObjectMap.remove(tag);
                loadedPath = bgMusicLoadedPathMap.remove(tag);
            }
            synchronized (bgMusicPlayingObjectMap)
            {
                playingObject = bgMusicPlayingObjectMap.remove(tag);
                bgMusicPlayingPathMap.remove(tag);
            }

            // 停止并销毁 loadedObject（"预备队列"中的资源）
            if (loadedObject != null)
            {
                loadedObject.stop();
                synchronized (disposeBgMusicMap)
                {
                    disposeBgMusicMap.put(tag, loadedObject);
                }
            }

            // 停止并销毁 playingObject（"播放队列"中的资源）
            // 仅在引用不同时执行，防止重复销毁同一对象。
            if (playingObject != null && playingObject != loadedObject)
            {
                playingObject.stop();
                synchronized (disposeBgMusicMap)
                {
                    disposeBgMusicMap.put(tag + ".current", playingObject);
                }
            }

            if (loadedPath != null)
            {
                LogUtils.debug(AudioManager.class, "disposeBackgroundMusic 销毁背景音乐 (tag): " + tag + " (file): " + loadedPath);
                triggerAsyncDispose();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "disposeBackgroundMusic", e);
            return false;
        }
    }

    // ===================================================================================================================
    // 全局控制与工具方法
    // ===================================================================================================================

    /**
     * 停止所有音频播放（包括音效、音乐和背景音乐）。
     *
     * @return 全部停止成功返回 true，否则返回 false
     */
    public boolean stopAll ()
    {
        try
        {
            boolean soundResult = stopAllSound();
            boolean musicResult = stopAllMusic();
            boolean bgMusicResult = stopAllBackgroundMusic();
            return soundResult && musicResult && bgMusicResult;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "stopAll", e);
            return false;
        }
    }

    /**
     * 获取总音量。
     *
     * @return 总音量值（0.0 ~ 1.0）
     */
    public float getTotalVolume ()
    {
        return totalVolume;
    }

    /**
     * 设置总音量。
     *
     * @param totalVolume 总音量值（0.0 ~ 1.0），超出范围的值会被自动限制
     */
    public void setTotalVolume (float totalVolume)
    {
        this.totalVolume = clampVolume(totalVolume);
    }

    /**
     * 获取音效音量。
     *
     * @return 音效音量值（0.0 ~ 1.0）
     */
    public float getSoundVolume ()
    {
        return soundVolume;
    }

    /**
     * 设置音效音量。
     *
     * @param soundVolume 音效音量值（0.0 ~ 1.0），超出范围的值会被自动限制
     */
    public void setSoundVolume (float soundVolume)
    {
        this.soundVolume = clampVolume(soundVolume);
    }

    /**
     * 获取音乐音量。
     *
     * @return 音乐音量值（0.0 ~ 1.0）
     */
    public float getMusicVolume ()
    {
        return musicVolume;
    }

    /**
     * 设置音乐音量。
     *
     * @param musicVolume 音乐音量值（0.0 ~ 1.0），超出范围的值会被自动限制
     */
    public void setMusicVolume (float musicVolume)
    {
        this.musicVolume = clampVolume(musicVolume);
    }

    /**
     * 将音量值限制在 0.0 ~ 1.0 范围内。
     *
     * @param volume 原始音量值
     * @return 限制后的音量值
     */
    private float clampVolume (float volume)
    {
        return Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * 检查待销毁队列中是否有资源，如果有则触发异步销毁。
     */
    private void triggerAsyncDispose ()
    {
        synchronized (disposeSoundMap)
        {
            synchronized (disposeMusicMap)
            {
                synchronized (disposeBgMusicMap)
                {
                    if (!disposeSoundMap.isEmpty() || !disposeMusicMap.isEmpty() || !disposeBgMusicMap.isEmpty())
                    {
                        asyncDisposeCachedResources();
                    }
                }
            }
        }
    }

    /**
     * 异步销毁队列中的音频资源（音效、音乐、背景音乐），延迟 50 毫秒后执行。
     */
    private void asyncDisposeCachedResources ()
    {
        disposeExecutor.schedule(() ->
        {
            try
            {
                // 销毁音效
                synchronized (disposeSoundMap)
                {
                    for (Sound sound : disposeSoundMap.values())
                    {
                        try
                        {
                            if (sound != null) sound.dispose();
                        }
                        catch (Exception e)
                        {
                            LogUtils.error(AudioManager.class, "asyncDispose Sound", e);
                        }
                    }
                    disposeSoundMap.clear();
                }
                // 销毁普通音乐
                synchronized (disposeMusicMap)
                {
                    for (Music music : disposeMusicMap.values())
                    {
                        try
                        {
                            if (music != null) music.dispose();
                        }
                        catch (Exception e)
                        {
                            LogUtils.error(AudioManager.class, "asyncDispose Music", e);
                        }
                    }
                    disposeMusicMap.clear();
                }
                // 销毁背景音乐
                synchronized (disposeBgMusicMap)
                {
                    for (Music bgMusic : disposeBgMusicMap.values())
                    {
                        try
                        {
                            if (bgMusic != null) bgMusic.dispose();
                        }
                        catch (Exception e)
                        {
                            LogUtils.error(AudioManager.class, "asyncDispose BackgroundMusic", e);
                        }
                    }
                    disposeBgMusicMap.clear();
                }
                LogUtils.debug(AudioManager.class, "asyncDisposeCachedResources 异步销毁缓存音频资源完成");
            }
            catch (Exception e)
            {
                LogUtils.error(AudioManager.class, "asyncDisposeCachedResources", e);
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * 销毁所有音频资源，停止所有播放，并将活跃资源移入待销毁队列进行异步清理。
     *
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean dispose ()
    {
        try
        {
            // ================================================================
            // 停止所有音效、音乐、背景音乐
            // 停止顺序：loadedObjectMap 在 playingObjectMap 之后停止。
            // 因为 loadedObject 与 playingObject 可能指向同一 Music 对象，
            // 先停 playingObjectMap 再停 loadedObjectMap，
            // 即使同一对象被 stop 两次也安全（libGDX Music.stop() 是幂等的）。
            // ================================================================
            synchronized (soundLoadedObjectMap)
            {
                for (Sound sound : soundLoadedObjectMap.values()) if (sound != null) sound.stop();
            }
            synchronized (musicPlayingObjectMap)
            {
                for (Music music : musicPlayingObjectMap.values()) if (music != null) music.stop();
            }
            synchronized (bgMusicPlayingObjectMap)
            {
                for (Music bgMusic : bgMusicPlayingObjectMap.values()) if (bgMusic != null) bgMusic.stop();
            }
            synchronized (musicLoadedObjectMap)
            {
                for (Music music : musicLoadedObjectMap.values()) if (music != null) music.stop();
            }
            synchronized (bgMusicLoadedObjectMap)
            {
                for (Music bgMusic : bgMusicLoadedObjectMap.values()) if (bgMusic != null) bgMusic.stop();
            }

            // 清空路径记录
            soundLoadedPathMap.clear();
            musicLoadedPathMap.clear();
            musicPlayingPathMap.clear();
            bgMusicLoadedPathMap.clear();
            bgMusicPlayingPathMap.clear();

            // ================================================================
            // 将活跃资源移到待销毁队列
            //
            // loadedObjectMap 与 playingObjectMap 指向同一个 Music 对象时，
            // putAll 两次到 disposeMusicMap/disposeBgMusicMap 会以第二次为准，
            // 第一次 put 的 key 被覆盖。但 dispose 阶段只关心 values()，
            // 只要同一个对象被至少 put 一次就能被正确销毁。
            // 分开 putAll 是为了避免代码逻辑异常时漏处理任一 map 中的资源。
            // ================================================================
            synchronized (soundLoadedObjectMap)
            {
                synchronized (disposeSoundMap)
                {
                    disposeSoundMap.putAll(soundLoadedObjectMap);
                }
                soundLoadedObjectMap.clear();
            }
            synchronized (musicLoadedObjectMap)
            {
                synchronized (disposeMusicMap)
                {
                    disposeMusicMap.putAll(musicLoadedObjectMap);
                }
                musicLoadedObjectMap.clear();
            }
            synchronized (musicPlayingObjectMap)
            {
                synchronized (disposeMusicMap)
                {
                    disposeMusicMap.putAll(musicPlayingObjectMap);
                }
                musicPlayingObjectMap.clear();
            }
            synchronized (bgMusicLoadedObjectMap)
            {
                synchronized (disposeBgMusicMap)
                {
                    disposeBgMusicMap.putAll(bgMusicLoadedObjectMap);
                }
                bgMusicLoadedObjectMap.clear();
            }
            synchronized (bgMusicPlayingObjectMap)
            {
                synchronized (disposeBgMusicMap)
                {
                    disposeBgMusicMap.putAll(bgMusicPlayingObjectMap);
                }
                bgMusicPlayingObjectMap.clear();
            }

            // 立即异步销毁
            disposeExecutor.submit(() ->
            {
                try
                {
                    synchronized (disposeSoundMap)
                    {
                        for (Sound sound : disposeSoundMap.values()) if (sound != null) sound.dispose();
                        disposeSoundMap.clear();
                    }
                    synchronized (disposeMusicMap)
                    {
                        for (Music music : disposeMusicMap.values()) if (music != null) music.dispose();
                        disposeMusicMap.clear();
                    }
                    synchronized (disposeBgMusicMap)
                    {
                        for (Music bgMusic : disposeBgMusicMap.values()) if (bgMusic != null) bgMusic.dispose();
                        disposeBgMusicMap.clear();
                    }
                }
                catch (Exception e)
                {
                    LogUtils.error(AudioManager.class, "dispose 异步销毁", e);
                }
            });
            disposeExecutor.shutdown();

            LogUtils.debug(AudioManager.class, "dispose 完成");
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(AudioManager.class, "dispose", e);
            return false;
        }
    }
}
