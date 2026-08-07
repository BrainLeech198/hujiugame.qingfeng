package com.hujiugame.qingfeng.manager;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.data.game.Layout;
import com.hujiugame.qingfeng.graphic.model.GifInfo;
import com.hujiugame.qingfeng.graphic.model.PictureInfo;
import com.hujiugame.qingfeng.ui.kind.button.ButtonInfo;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.ui.kind.label.LabelInfo;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.GraphicsKey;
import com.hujiugame.qingfeng.type.key.LayoutKey;
import com.hujiugame.qingfeng.type.key.UiKey;
import com.hujiugame.qingfeng.audio.AudioManager;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.ui.UiManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public final class LayoutManager
{
    private AudioManager audioManager;
    private GraphicsManager graphicsManager;
    private UiManager uiManager;

    private Map<String, Layout> layoutConfigMap = new HashMap<>();

    /**
     * 构造方法，创建空的布局管理器
     */
    public LayoutManager ()
    {
    }

    /**
     * 设置音频管理器
     *
     * @param audioManager 音频管理器实例
     */
    public void setAudioManager (AudioManager audioManager)
    {
        this.audioManager = audioManager;
    }

    /**
     * 设置图形管理器
     *
     * @param graphicsManager 图形管理器实例
     */
    public void setGraphicsManager (GraphicsManager graphicsManager)
    {
        this.graphicsManager = graphicsManager;
    }

    /**
     * 设置UI管理器
     *
     * @param uiManager UI管理器实例
     */
    public void setUiManager (UiManager uiManager)
    {
        this.uiManager = uiManager;
    }

    // ===================================================================================================================

    /**
     * 从文件中安全读取布局JSON实体
     *
     * @param file 文件句柄
     * @return 布局JSON实体，读取失败返回空实体
     */
    private JsonEntity readLayoutJson (FileHandle file)
    {
        try
        {
            return new JsonEntity(file);
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "readLayoutJson", e);
            return new JsonEntity();
        }
    }

    /**
     * 加载布局基本信息（名称和模板）
     *
     * @param layout     布局配置对象
     * @param layoutJson 布局JSON实体
     * @param file       布局文件句柄（用于名称回退）
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutBasicInfo (Layout layout, JsonEntity layoutJson, FileHandle file)
    {
        try
        {
            if (layoutJson.containsKey(LayoutKey.NAME))
            {
                layout.setName(layoutJson.getString(LayoutKey.NAME));
                LogUtils.debug(LayoutManager.class, "loadLayoutBasicInfo 页面结构名称 (name): " + layout.getName());
            }
            else
            {
                layout.setName(file.nameWithoutExtension());
                LogUtils.debug(LayoutManager.class, "loadLayoutBasicInfo 获取页面结构名称失败，使用文件名作为页面结构名称 (name): " + layout.getName());
            }

            if (layoutJson.containsKey(LayoutKey.TEMPLATE))
            {
                layout.setTemplate(layoutJson.getString(LayoutKey.TEMPLATE));
                LogUtils.debug(LayoutManager.class, "loadLayoutBasicInfo 模板 (template): " + layout.getTemplate());
            }
            else
            {
                LogUtils.debug(LayoutManager.class, "loadLayoutBasicInfo 模板不存在，已忽略");
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadBasicInfo", e);
            return false;
        }
    }

    /**
     * 加载布局中的背景音乐和音乐列表
     *
     * @param layout                布局配置对象
     * @param layoutJson            布局JSON实体
     * @param resourceRootDirectory 资源根目录文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutMusic (Layout layout, JsonEntity layoutJson, FileHandle resourceRootDirectory)
    {
        try
        {
            // 解析背景音乐
            // ===================================================================================================================
            // 音频字段统一收敛到 audio 节点：
            //   "audio" : { "backgroundMusic" : "menu.mp3" }               → 单曲，自动包装为单值列表
            //   "audio" : { "backgroundMusic" : ["menu.mp3", "menu2.mp3"] } → 列表，每首分别加载
            // ===================================================================================================================
            JsonEntity audioJson = layoutJson.getJsonEntityByKey(LayoutKey.AUDIO);
            List<String> backgroundMusicNames = null;

            // 优先尝试解析为列表（JSON 数组）
            List<String> nameList = audioJson.getStringList(LayoutKey.Audio.BACKGROUND_MUSIC);
            if (nameList != null && !nameList.isEmpty())
            {
                backgroundMusicNames = nameList;
            }
            else
            {
                // 回退到单个字符串（JSON 单值，向后兼容）
                String singleName = audioJson.getString(LayoutKey.Audio.BACKGROUND_MUSIC);
                if (singleName != null)
                {
                    backgroundMusicNames = Collections.singletonList(singleName);
                }
            }

            if (backgroundMusicNames != null)
            {
                // 逐首加载背景音乐
                for (String bgmName : backgroundMusicNames)
                {
                    if (bgmName == null) continue;

                    // 上载背景音乐
                    FileHandle bgmFileHandle = resourceRootDirectory.child(PathName.ASSET_S_RESOURCE_AUDIO).child(bgmName);
                    if (audioManager.loadBackgroundMusic(bgmName, bgmFileHandle))
                    {
                        LogUtils.debug(LayoutManager.class, "loadLayoutMusic 背景音乐加载成功 (tag): " + bgmName
                            + " (file): " + FileUtils.pathJoin(resourceRootDirectory.path(), PathName.ASSET_S_RESOURCE_AUDIO, bgmName));
                    }
                    else
                    {
                        LogUtils.debug(LayoutManager.class, "loadLayoutMusic 背景音乐加载失败 (tag): " + bgmName
                            + " (file): " + FileUtils.pathJoin(resourceRootDirectory.path(), PathName.ASSET_S_RESOURCE_AUDIO, bgmName));
                    }
                }
            }

            // 存入 Layout
            layout.setBackgroundMusicList(backgroundMusicNames != null ? backgroundMusicNames : new ArrayList<String>());
            LogUtils.debug(LayoutManager.class, "loadLayoutMusic 背景音乐列表: " + backgroundMusicNames);

            // 解析音乐list
            // ===================================================================================================================
            JsonEntity musicMap = audioJson.getJsonEntityByKey(LayoutKey.Audio.MUSIC);
            List<String> newMusicList = new ArrayList<>();
            if (!musicMap.isEmpty())
            {
                for (String musicTag : musicMap.keySet())
                {
                    // 读取音乐路径
                    String musicPath = FileUtils.pathJoin(resourceRootDirectory.path(), PathName.ASSET_S_RESOURCE_AUDIO, musicMap.getString(musicTag));

                    // 上载音乐
                    FileHandle musicFileHandle = resourceRootDirectory.child(PathName.ASSET_S_RESOURCE_AUDIO).child(musicMap.getString(musicTag));

                    if (audioManager.loadMusic(musicTag, musicFileHandle))
                    {
                        newMusicList.add(musicTag);
                        LogUtils.debug(LayoutManager.class, "loadLayoutMusic 音乐加载成功 (tag): " + musicTag + " (file): " + musicPath);
                    }
                    else
                    {
                        LogUtils.error(LayoutManager.class, "loadLayoutMusic 音乐加载失败 (tag): " + musicTag + " (file): " + musicPath);
                    }
                }
            }

            layout.setMusicList(newMusicList);
            LogUtils.debug(LayoutManager.class, "loadLayoutMusic 音乐表: " + musicMap);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutMusic", e);
            return false;
        }
    }

    /**
     * 加载布局中的背景图片、图片列表和动图（GIF）列表
     *
     * @param layout                布局配置对象
     * @param layoutJson            布局JSON实体
     * @param resourceRootDirectory 资源根目录文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutGraphics (Layout layout, JsonEntity layoutJson, FileHandle resourceRootDirectory)
    {
        try
        {
            // 解析背景图片路径
            // ===================================================================================================================
            String backgroundPictureName = layoutJson.getString(LayoutKey.BACKGROUND_PICTURE);
            String backgroundPicturePath = null;

            if (backgroundPictureName != null)
            {
                // 获取路径
                backgroundPicturePath = FileUtils.pathJoin(resourceRootDirectory.path(), PathName.ASSET_S_RESOURCE_IMAGE, backgroundPictureName);
                FileHandle backgroundPictureFileHandle = resourceRootDirectory.child(PathName.ASSET_S_RESOURCE_IMAGE).child(backgroundPictureName);

                // 上载图片
                graphicsManager.loadBackgroundPicture(backgroundPictureName, backgroundPictureFileHandle);
            }

            // 上载
            layout.setBackgroundPicture(backgroundPictureName);
            LogUtils.debug(LayoutManager.class, "loadLayoutGraphics 背景图片 (file): " + backgroundPicturePath);

            // 解析 graphics 子分类
            // ===================================================================================================================
            JsonEntity graphicsJson = layoutJson.getJsonEntityByKey(LayoutKey.GRAPHICS);
            if (!graphicsJson.isEmpty())
            {
                if (!loadLayoutGraphicsPicture(layout, graphicsJson, resourceRootDirectory)) return false;
                if (!loadLayoutGraphicsGif(layout, graphicsJson, resourceRootDirectory)) return false;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutGraphics", e);
            return false;
        }
    }

    /**
     * 加载 graphics → picture 图片条目
     *
     * @param layout          布局配置对象
     * @param graphicsJson    graphics 部分 JSON 实体
     * @param resourceRootDirectory 资源根目录文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutGraphicsPicture (Layout layout, JsonEntity graphicsJson, FileHandle resourceRootDirectory)
    {
        try
        {
            JsonEntity pictureMapJson = graphicsJson.getJsonEntityByKey(GraphicsKey.PICTURE);
            Map<String, PictureInfo> newPictureMap = new HashMap<>();

            if (!pictureMapJson.isEmpty())
            {
                // 完善实体
                for (String pictureTag : pictureMapJson.keySet())
                {
                    // 读取pictureInfoJson
                    JsonEntity pictureInfoJson = pictureMapJson.getJsonEntityByKey(pictureTag);

                    // 上载图片
                    String pictureFileName = pictureInfoJson.getString(GraphicsKey.PATH);
                    FileHandle pictureFileHandle = resourceRootDirectory.child(PathName.ASSET_S_RESOURCE_IMAGE).child(pictureFileName);
                    if (graphicsManager.loadPicture(pictureFileName, pictureFileHandle))
                    {
                        // 成功再添加
                        PictureInfo pictureInfo = new PictureInfo(pictureTag, pictureInfoJson);
                        newPictureMap.put(pictureTag, pictureInfo);
                    }
                }
            }

            // 上载
            layout.setPictureMap(newPictureMap);

            LogUtils.debug(LayoutManager.class, "loadLayoutGraphicsPicture 图片 (list): " + newPictureMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutGraphicsPicture", e);
            return false;
        }
    }

    /**
     * 加载 graphics → gif 动图条目
     *
     * @param layout          布局配置对象
     * @param graphicsJson    graphics 部分 JSON 实体
     * @param resourceRootDirectory 资源根目录文件句柄
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutGraphicsGif (Layout layout, JsonEntity graphicsJson, FileHandle resourceRootDirectory)
    {
        try
        {
            JsonEntity gifMapJson = graphicsJson.getJsonEntityByKey(GraphicsKey.GIF);
            Map<String, GifInfo> newGifMap = new HashMap<>();

            if (!gifMapJson.isEmpty())
            {
                // 完善实体
                for (String gifTag : gifMapJson.keySet())
                {
                    // 读取gifInfoJson
                    JsonEntity gifInfoJson = gifMapJson.getJsonEntityByKey(gifTag);

                    // 上载动图
                    List<FileHandle> gifFileList = new ArrayList<>();

                    // tag : {"length": n, "duration": s, "path":{"0": "...."}}
                    int gifSize = gifInfoJson.getInt(GraphicsKey.Gif.LENGTH);
                    float gifDuration = gifInfoJson.getFloat(GraphicsKey.Gif.DURATION);
                    JsonEntity gifPathJson = gifInfoJson.getJsonEntityByKey(GraphicsKey.PATH);
                    for (int i = 1; i <= gifSize; i++)
                    {
                        if (gifPathJson.containsKey(i + ""))
                        {
                            String gifFileName = gifPathJson.getString(i + "");
                            String gifPath = FileUtils.pathJoin(resourceRootDirectory.path(), PathName.ASSET_S_RESOURCE_IMAGE, gifFileName);
                            FileHandle gifFileHandle = resourceRootDirectory.child(PathName.ASSET_S_RESOURCE_IMAGE).child(gifFileName);
                            gifFileList.add(gifFileHandle);
                        }
                        else
                        {
                            LogUtils.error(LayoutManager.class, "loadLayoutGraphicsGif 动图 (tag): " + gifTag + " 丢失第 " + i + " 张图片");
                            return false;
                        }
                    }

                    // 上载动图
                    if (graphicsManager.loadGif(gifTag, gifFileList, gifDuration))
                    {
                        // 成功再添加
                        GifInfo gifInfo = new GifInfo(gifTag, gifInfoJson);
                        newGifMap.put(gifTag, gifInfo);
                    }
                }
            }

            // 上载
            layout.setGifMap(newGifMap);

            LogUtils.debug(LayoutManager.class, "loadLayoutGraphicsGif Gif (list): " + newGifMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutGraphicsGif", e);
            return false;
        }
    }

    /**
     * 加载布局UI中的图像配置
     *
     * @param layout 布局配置对象
     * @param uiJson UI部分的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutUiImage (Layout layout, JsonEntity uiJson)
    {
        try
        {
            // 解析图像
            // ===================================================================================================================

            // 解析图像json
            JsonEntity imageMapJson = uiJson.getJsonEntityByKey(UiKey.Image.KEY);
            Map<String, ImageInfo> imageMap = new LinkedHashMap<>();

            for (String imageTag : imageMapJson.keySet())
            {
                // 获取每个独立json
                JsonEntity imageJson = imageMapJson.getJsonEntityByKey(imageTag);

                // 添加到新图像列表
                ImageInfo imageInfo = new ImageInfo(imageTag, imageJson);
                imageMap.put(imageTag, imageInfo);
            }

            // 上载
            layout.setImageMap(imageMap);

            LogUtils.debug(LayoutManager.class, "loadLayoutUiImage 图像 (list): " + imageMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutImage", e);
            return false;
        }
    }

    /**
     * 加载布局UI中的标签配置
     *
     * @param layout 布局配置对象
     * @param uiJson UI部分的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutUiLabel (Layout layout, JsonEntity uiJson)
    {
        try
        {
            // 解析标签
            // ===================================================================================================================

            // 解析标签json
            JsonEntity labelMapJson = uiJson.getJsonEntityByKey(UiKey.Label.KEY);
            Map<String, LabelInfo> labelMap = new HashMap<>();

            for (String labelTag : labelMapJson.keySet())
            {
                // 获取每个独立json
                JsonEntity labelJson = labelMapJson.getJsonEntityByKey(labelTag);

                // 添加到新标签列表
                LabelInfo labelInfo = new LabelInfo(labelTag, labelJson);
                labelMap.put(labelTag, labelInfo);
            }

            // 上载
            layout.setLabelMap(labelMap);

            LogUtils.debug(LayoutManager.class, "loadLayoutUiLabel 标签 (list): " + labelMap);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutUiLabel", e);
            return false;
        }
    }

    /**
     * 加载布局UI中的按钮配置
     *
     * @param layout 布局配置对象
     * @param uiJson UI部分的JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutUiButton (Layout layout, JsonEntity uiJson)
    {
        try
        {
            // 解析按钮
            // ===================================================================================================================

            // 解析按钮json
            JsonEntity buttonMapJson = uiJson.getJsonEntityByKey(UiKey.Button.KEY);
            Map<String, ButtonInfo> buttonMap = new HashMap<>();

            // 添加到新按钮列表
            for (String buttonTag : buttonMapJson.keySet())
            {
                // 获取每个独立json
                JsonEntity buttonJson = buttonMapJson.getJsonEntityByKey(buttonTag);

                // 添加到新按钮列表
                ButtonInfo buttonInfo = new ButtonInfo(buttonTag, buttonJson);
                buttonMap.put(buttonTag, buttonInfo);

                // debug
                LogUtils.debug(LayoutManager.class, "loadLayoutUiButton 按钮 (tag):" + buttonTag + " (json): " + buttonJson);
            }

            // 上载
            layout.setButtonMap(buttonMap);

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutUiButton", e);
            return false;
        }
    }

    /**
     * 加载布局中的UI配置（图像、标签、按钮）
     *
     * @param layout     布局配置对象
     * @param layoutJson 布局JSON实体
     * @return 加载成功返回 true，失败返回 false
     */
    private boolean loadLayoutUi (Layout layout, JsonEntity layoutJson)
    {
        try
        {
            if (!layoutJson.isEmpty())
            {
                // 解析ui
                if (layoutJson.containsKey(LayoutKey.UI))
                {
                    JsonEntity uiJson = layoutJson.getJsonEntityByKey(LayoutKey.UI);

                    if (!loadLayoutUiImage(layout, uiJson))
                        return false;

                    if (!loadLayoutUiLabel(layout, uiJson))
                        return false;

                    if (!loadLayoutUiButton(layout, uiJson))
                        return false;

                }
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayoutUi", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 从文件加载布局配置：读取JSON、解析基本信息、音乐、图片和UI
     *
     * @param fileHandle                  布局文件句柄
     * @param resourceRootDirectoryHandle 资源根目录文件句柄
     * @param reload                      是否重新加载（当前未使用，保留参数）
     * @return 布局配置对象，加载失败返回 null
     */
    public Layout loadLayout (FileHandle fileHandle, FileHandle resourceRootDirectoryHandle, boolean reload)
    {
        try
        {
            // 读取缓存
            String layoutKey = fileHandle.path() + "@" + resourceRootDirectoryHandle.path();
            Layout cached = layoutConfigMap.get(layoutKey);

            if (cached != null && !reload)
            {
                // 缓存命中，重载图片和音乐资源（可能已被 quitGame 的 dispose 清空）
                LogUtils.debug(LayoutManager.class, "loadLayout 读取缓存 (key): " + layoutKey);
                JsonEntity layoutJson = readLayoutJson(fileHandle);
                loadLayoutMusic(cached, layoutJson, resourceRootDirectoryHandle);
                loadLayoutGraphics(cached, layoutJson, resourceRootDirectoryHandle);
                return cached;
            }

            // 安全读取结构json
            JsonEntity layoutJson = readLayoutJson(fileHandle);
            LogUtils.debug(LayoutManager.class, "loadLayout 读取结构 (json): " + layoutJson);

            // 创建结构
            Layout layout = new Layout();
            layout.setJson(layoutJson);

            // 空结构
            if (layoutJson.isEmpty())
            {
                return layout;
            }

            // 读取基本信息
            if (!loadLayoutBasicInfo(layout, layoutJson, fileHandle))
            {
                LogUtils.error(LayoutManager.class, "loadLayout 读取基本信息出现问题");
            }

            // 先配置音乐
            if (!loadLayoutMusic(layout, layoutJson, resourceRootDirectoryHandle))
            {
                LogUtils.error(LayoutManager.class, "loadLayout 配置音乐出现问题");
            }

            // 配置draw图片
            if (!loadLayoutGraphics(layout, layoutJson, resourceRootDirectoryHandle))
            {
                LogUtils.error(LayoutManager.class, "loadLayout 配置图片出现问题");
            }

            // 配置ui
            if (!loadLayoutUi(layout, layoutJson))
            {
                LogUtils.error(LayoutManager.class, "loadLayout 配置UI出现问题");
            }

            // 上载缓存
            layoutConfigMap.put(layoutKey, layout);
            return layout;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "loadLayout", e);
            return null;
        }
    }

    /**
     * 将合并布局的配置项合并到主布局中（JSON、背景图片、背景音乐、音乐列表、图片、GIF、图像、标签、按钮）
     *
     * @param mainLayout  主布局配置对象（将被修改）
     * @param mergeLayout 待合并的布局配置对象
     */
    public void mergeLayout (Layout mainLayout, Layout mergeLayout)
    {
        try
        {
            if (mainLayout == null)
            {
                LogUtils.error(LayoutManager.class, "mergeLayout 主页面结构为null");
                return;
            }
            else if (mergeLayout == null)
            {
                LogUtils.debug(LayoutManager.class, "mergeLayout 合并页面结构为空，省去合并");
                return;
            }

            LogUtils.debug(LayoutManager.class, "mergeLayout 配置 (main): " + mainLayout.getJson() + " (merge): " + mergeLayout.getJson());

            // 保存主布局原始 JSON（字段级融合需要）
            JsonEntity mainOriginalJson = mainLayout.getJson();

            if (mainOriginalJson != null && !mainOriginalJson.isEmpty())
            {
                JsonEntity graphicsJson = mainOriginalJson.getJsonEntityByKey(LayoutKey.GRAPHICS);
                LogUtils.debug(LayoutManager.class, "mergeLayout 主布局原始JSON (" + LayoutKey.UI + "/" + UiKey.Image.KEY + "): " + hasUiSection(mainOriginalJson, UiKey.Image.KEY) +
                    " (" + LayoutKey.UI + "/" + UiKey.Label.KEY + "): " + hasUiSection(mainOriginalJson, UiKey.Label.KEY) +
                    " (" + LayoutKey.UI + "/" + UiKey.Button.KEY + "): " + hasUiSection(mainOriginalJson, UiKey.Button.KEY) +
                    " (" + LayoutKey.GRAPHICS + "/" + GraphicsKey.PICTURE + "): " + graphicsJson.containsKey(GraphicsKey.PICTURE) +
                    " (" + LayoutKey.GRAPHICS + "/" + GraphicsKey.GIF + "): " + graphicsJson.containsKey(GraphicsKey.GIF));
            }
            else
            {
                LogUtils.debug(LayoutManager.class, "mergeLayout 主布局原始JSON 为空或null，字段级融合将降级为保留主布局原对象");
            }

            JsonEntity layoutJson = mainLayout.getJson().combined(mergeLayout.getJson());
            mainLayout.setJson(layoutJson);
            LogUtils.debug(LayoutManager.class, "mergeLayout 配置 (result): " + mainLayout);

            LogUtils.debug(LayoutManager.class, "mergeLayout 背景图片 (main): " + mainLayout.getBackgroundPicture() + " (merge): " + mergeLayout.getBackgroundPicture());
            mainLayout.setBackgroundPicture(mergeLayout.getBackgroundPicture() != null ? mergeLayout.getBackgroundPicture() : mainLayout.getBackgroundPicture());
            LogUtils.debug(LayoutManager.class, "mergeLayout 背景图片 (result): " + mainLayout.getBackgroundPicture());

            LogUtils.debug(LayoutManager.class, "mergeLayout 背景音乐列表 (main): " + mainLayout.getBackgroundMusicList() + " (merge): " + mergeLayout.getBackgroundMusicList());
            List<String> mergedBgmList = new ArrayList<>(mainLayout.getBackgroundMusicList());
            mergedBgmList.addAll(mergeLayout.getBackgroundMusicList());
            mainLayout.setBackgroundMusicList(mergedBgmList);
            LogUtils.debug(LayoutManager.class, "mergeLayout 背景音乐列表 (result): " + mergedBgmList);

            LogUtils.debug(LayoutManager.class, "mergeLayout 音乐列表 (main): " + mainLayout.getMusicList() + " (merge): " + mergeLayout.getMusicList());
            List<String> musicList = new ArrayList<>(mainLayout.getMusicList());
            musicList.addAll(mergeLayout.getMusicList());
            mainLayout.setMusicList(musicList);
            LogUtils.debug(LayoutManager.class, "mergeLayout 音乐列表 (result): " + musicList);

            // 字段级融合：相同 tag 时 merge 覆盖 main 字段，main 填补缺失字段
            mergePictureMap(mainLayout, mergeLayout, mainOriginalJson);
            mergeGifMap(mainLayout, mergeLayout, mainOriginalJson);
            mergeImageMap(mainLayout, mergeLayout, mainOriginalJson);
            mergeLabelMap(mainLayout, mergeLayout, mainOriginalJson);
            mergeButtonMap(mainLayout, mergeLayout, mainOriginalJson);
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "mergeLayout", e);
        }
    }

    /**
     * 将主布局与合并布局合并，返回一个新的布局配置对象（不修改原始对象）
     *
     * @param mainLayout  主布局配置对象
     * @param mergeLayout 待合并的布局配置对象
     * @return 合并后的新布局配置对象，失败返回 null
     */
    public Layout mergedLayout (Layout mainLayout, Layout mergeLayout)
    {
        try
        {
            LogUtils.debug(LayoutManager.class, "mergedLayout (main): " + (mainLayout != null ? mainLayout.getName() : "null") + " (merge): " + (mergeLayout != null ? mergeLayout.getName() : "null"));

            Layout resultLayout = new Layout(mainLayout);
            mergeLayout(resultLayout, mergeLayout);

            LogUtils.debug(LayoutManager.class, "mergedLayout 完成 (result): " + resultLayout.getName() +
                " label:" + resultLayout.getLabelMap().size() +
                " button:" + resultLayout.getButtonMap().size() +
                " image:" + resultLayout.getImageMap().size() +
                " picture:" + resultLayout.getPictureMap().size() +
                " gif:" + resultLayout.getGifMap().size());

            return resultLayout;
        }
        catch (Exception e)
        {
            LogUtils.error(LayoutManager.class, "mergeLayout", e);
            return null;
        }
    }

    // ===================================================================================================================
    //  字段级融合：相同 tag 时 merge 覆盖 main 字段，main 填补缺失字段
    // ===================================================================================================================

    /**
     * 检查布局 JSON 的 ui 分类下是否存在指定 section
     */
    private static boolean hasUiSection (JsonEntity layoutJson, String section)
    {
        if (layoutJson == null || layoutJson.isEmpty()) return false;
        JsonEntity uiJson = layoutJson.getJsonEntityByKey(LayoutKey.UI);
        if (uiJson.isEmpty()) return false;
        JsonEntity sectionJson = uiJson.getJsonEntityByKey(section);
        return !sectionJson.isEmpty();
    }

    /**
     * 从布局 JSON 的 ui 分类（image、label、button）下提取指定 tag 的 JSON
     *
     * @param layoutJson 布局 JSON
     * @param section    ui 下的分类名称
     * @param tag        标签
     * @return tag 对应的 JSON，不存在返回空对象
     */
    private static JsonEntity extractUiSectionTagJson (JsonEntity layoutJson, String section, String tag)
    {
        if (layoutJson == null || layoutJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractUiSectionTagJson layoutJson 为空 (section): " + section + " (tag): " + tag);
            return new JsonEntity();
        }
        JsonEntity uiJson = layoutJson.getJsonEntityByKey(LayoutKey.UI);
        if (uiJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractUiSectionTagJson ui 不存在 (section): " + section + " (tag): " + tag);
            return new JsonEntity();
        }
        JsonEntity sectionJson = uiJson.getJsonEntityByKey(section);
        if (sectionJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractUiSectionTagJson ui/" + section + " 不存在 (tag): " + tag);
            return new JsonEntity();
        }
        JsonEntity tagJson = sectionJson.getJsonEntityByKey(tag);
        if (tagJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractUiSectionTagJson ui/" + section + "/" + tag + " 不存在");
        }
        return tagJson;
    }

    /**
     * 从布局 JSON 的分类下提取指定 tag 的 JSON
     *
     * @param layoutJson 布局 JSON
     * @param section    分类名称（如 picture、gif）
     * @param tag        标签
     * @return tag 对应的 JSON，不存在返回空对象
     */
    private static JsonEntity extractSectionTagJson (JsonEntity layoutJson, String section, String tag)
    {
        if (layoutJson == null || layoutJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractSectionTagJson layoutJson 为空 (section): " + section + " (tag): " + tag);
            return new JsonEntity();
        }
        JsonEntity sectionJson = layoutJson.getJsonEntityByKey(section);
        if (sectionJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractSectionTagJson " + section + " 不存在 (tag): " + tag);
            return new JsonEntity();
        }
        JsonEntity tagJson = sectionJson.getJsonEntityByKey(tag);
        if (tagJson.isEmpty())
        {
            LogUtils.debug(LayoutManager.class, "extractSectionTagJson " + section + "/" + tag + " 不存在");
        }
        return tagJson;
    }

    /**
     * 字段级融合 picture 映射表
     */
    private void mergePictureMap (Layout main, Layout merge, JsonEntity mainOriginalJson)
    {
        LogUtils.debug(LayoutManager.class, "mergePictureMap (main): " + main.getPictureMap().size() + " 个 (merge): " + merge.getPictureMap().size() + " 个");

        Map<String, PictureInfo> result = new HashMap<>(main.getPictureMap());
        for (Map.Entry<String, PictureInfo> entry : merge.getPictureMap().entrySet())
        {
            String tag = entry.getKey();
            if (result.containsKey(tag))
            {
                JsonEntity mainTagJson = extractSectionTagJson(mainOriginalJson.getJsonEntityByKey(LayoutKey.GRAPHICS), GraphicsKey.PICTURE, tag);
                JsonEntity mergeTagJson = extractSectionTagJson(merge.getJson().getJsonEntityByKey(LayoutKey.GRAPHICS), GraphicsKey.PICTURE, tag);
                if (!mainTagJson.isEmpty() && !mergeTagJson.isEmpty())
                {
                    result.put(tag, new PictureInfo(tag, mergeTagJson.combined(mainTagJson)));
                    LogUtils.debug(LayoutManager.class, "mergePictureMap 融合 (tag): " + tag + " (main): " + mainTagJson + " (merge): " + mergeTagJson);
                }
                else
                {
                    LogUtils.debug(LayoutManager.class, "mergePictureMap 保留 (tag): " + tag + " (原因): JSON提取失败");
                }
            }
            else
            {
                result.put(tag, new PictureInfo(entry.getValue()));
                LogUtils.debug(LayoutManager.class, "mergePictureMap 新增 (tag): " + tag);
            }
        }
        main.setPictureMap(result);
        LogUtils.debug(LayoutManager.class, "mergePictureMap 结果: " + result.size() + " 个");
    }

    /**
     * 字段级融合 gif 映射表
     */
    private void mergeGifMap (Layout main, Layout merge, JsonEntity mainOriginalJson)
    {
        LogUtils.debug(LayoutManager.class, "mergeGifMap (main): " + main.getGifMap().size() + " 个 (merge): " + merge.getGifMap().size() + " 个");

        Map<String, GifInfo> result = new HashMap<>(main.getGifMap());
        for (Map.Entry<String, GifInfo> entry : merge.getGifMap().entrySet())
        {
            String tag = entry.getKey();
            if (result.containsKey(tag))
            {
                JsonEntity mainTagJson = extractSectionTagJson(mainOriginalJson.getJsonEntityByKey(LayoutKey.GRAPHICS), GraphicsKey.GIF, tag);
                JsonEntity mergeTagJson = extractSectionTagJson(merge.getJson().getJsonEntityByKey(LayoutKey.GRAPHICS), GraphicsKey.GIF, tag);
                if (!mainTagJson.isEmpty() && !mergeTagJson.isEmpty())
                {
                    result.put(tag, new GifInfo(tag, mergeTagJson.combined(mainTagJson)));
                    LogUtils.debug(LayoutManager.class, "mergeGifMap 融合 (tag): " + tag + " (main): " + mainTagJson + " (merge): " + mergeTagJson);
                }
                else
                {
                    LogUtils.debug(LayoutManager.class, "mergeGifMap 保留 (tag): " + tag + " (原因): JSON提取失败");
                }
            }
            else
            {
                result.put(tag, new GifInfo(entry.getValue()));
                LogUtils.debug(LayoutManager.class, "mergeGifMap 新增 (tag): " + tag);
            }
        }
        main.setGifMap(result);
        LogUtils.debug(LayoutManager.class, "mergeGifMap 结果: " + result.size() + " 个");
    }

    /**
     * 字段级融合 image 映射表
     */
    private void mergeImageMap (Layout main, Layout merge, JsonEntity mainOriginalJson)
    {
        LogUtils.debug(LayoutManager.class, "mergeImageMap (main): " + main.getImageMap().size() + " 个 (merge): " + merge.getImageMap().size() + " 个");

        Map<String, ImageInfo> result = new HashMap<>(main.getImageMap());
        for (Map.Entry<String, ImageInfo> entry : merge.getImageMap().entrySet())
        {
            String tag = entry.getKey();
            if (result.containsKey(tag))
            {
                JsonEntity mainTagJson = extractUiSectionTagJson(mainOriginalJson, UiKey.Image.KEY, tag);
                JsonEntity mergeTagJson = extractUiSectionTagJson(merge.getJson(), UiKey.Image.KEY, tag);
                if (!mainTagJson.isEmpty() && !mergeTagJson.isEmpty())
                {
                    result.put(tag, new ImageInfo(tag, mergeTagJson.combined(mainTagJson)));
                    LogUtils.debug(LayoutManager.class, "mergeImageMap 融合 (tag): " + tag + " (main): " + mainTagJson + " (merge): " + mergeTagJson);
                }
                else
                {
                    LogUtils.debug(LayoutManager.class, "mergeImageMap 保留 (tag): " + tag + " (原因): JSON提取失败");
                }
            }
            else
            {
                result.put(tag, new ImageInfo(entry.getValue()));
                LogUtils.debug(LayoutManager.class, "mergeImageMap 新增 (tag): " + tag);
            }
        }
        main.setImageMap(result);
        LogUtils.debug(LayoutManager.class, "mergeImageMap 结果: " + result.size() + " 个");
    }

    /**
     * 字段级融合 label 映射表
     */
    private void mergeLabelMap (Layout main, Layout merge, JsonEntity mainOriginalJson)
    {
        LogUtils.debug(LayoutManager.class, "mergeLabelMap (main): " + main.getLabelMap().size() + " 个 (merge): " + merge.getLabelMap().size() + " 个");

        Map<String, LabelInfo> result = new HashMap<>(main.getLabelMap());
        for (Map.Entry<String, LabelInfo> entry : merge.getLabelMap().entrySet())
        {
            String tag = entry.getKey();
            if (result.containsKey(tag))
            {
                JsonEntity mainTagJson = extractUiSectionTagJson(mainOriginalJson, UiKey.Label.KEY, tag);
                JsonEntity mergeTagJson = extractUiSectionTagJson(merge.getJson(), UiKey.Label.KEY, tag);
                if (!mainTagJson.isEmpty() && !mergeTagJson.isEmpty())
                {
                    result.put(tag, new LabelInfo(tag, mergeTagJson.combined(mainTagJson)));
                    LogUtils.debug(LayoutManager.class, "mergeLabelMap 融合 (tag): " + tag + " (main): " + mainTagJson + " (merge): " + mergeTagJson);
                }
                else
                {
                    LogUtils.debug(LayoutManager.class, "mergeLabelMap 保留 (tag): " + tag + " (原因): JSON提取失败");
                }
            }
            else
            {
                result.put(tag, new LabelInfo(entry.getValue()));
                LogUtils.debug(LayoutManager.class, "mergeLabelMap 新增 (tag): " + tag);
            }
        }
        main.setLabelMap(result);
        LogUtils.debug(LayoutManager.class, "mergeLabelMap 结果: " + result.size() + " 个");
    }

    /**
     * 字段级融合 button 映射表
     */
    private void mergeButtonMap (Layout main, Layout merge, JsonEntity mainOriginalJson)
    {
        LogUtils.debug(LayoutManager.class, "mergeButtonMap (main): " + main.getButtonMap().size() + " 个 (merge): " + merge.getButtonMap().size() + " 个");

        Map<String, ButtonInfo> result = new HashMap<>(main.getButtonMap());
        for (Map.Entry<String, ButtonInfo> entry : merge.getButtonMap().entrySet())
        {
            String tag = entry.getKey();
            if (result.containsKey(tag))
            {
                JsonEntity mainTagJson = extractUiSectionTagJson(mainOriginalJson, UiKey.Button.KEY, tag);
                JsonEntity mergeTagJson = extractUiSectionTagJson(merge.getJson(), UiKey.Button.KEY, tag);
                if (!mainTagJson.isEmpty() && !mergeTagJson.isEmpty())
                {
                    result.put(tag, new ButtonInfo(tag, mergeTagJson.combined(mainTagJson)));
                    LogUtils.debug(LayoutManager.class, "mergeButtonMap 融合 (tag): " + tag + " (main): " + mainTagJson + " (merge): " + mergeTagJson);
                }
                else
                {
                    LogUtils.debug(LayoutManager.class, "mergeButtonMap 保留 (tag): " + tag + " (原因): JSON提取失败");
                }
            }
            else
            {
                result.put(tag, new ButtonInfo(entry.getValue()));
                LogUtils.debug(LayoutManager.class, "mergeButtonMap 新增 (tag): " + tag);
            }
        }
        main.setButtonMap(result);
        LogUtils.debug(LayoutManager.class, "mergeButtonMap 结果: " + result.size() + " 个");
    }

}
