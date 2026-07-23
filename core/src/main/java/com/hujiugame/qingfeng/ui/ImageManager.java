package com.hujiugame.qingfeng.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.ui.kind.InteractableObject;
import com.hujiugame.qingfeng.ui.kind.image.ImageInfo;
import com.hujiugame.qingfeng.ui.kind.image.ImageKind;
import com.hujiugame.qingfeng.ui.kind.image.ImageState;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.graphic.GraphicsManager;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;

public final class ImageManager
{
    private final Stage stage;
    private final GraphicsManager graphicsManager;
    private final UiManager uiManager;
    private final HashSet<InteractableObject> interactableObjectSet;

    // ========== image kind ==========
    private final Map<String, ImageKind> imageKindMap = new HashMap<>();
    // ========== image object ==========
    private final Map<String, UiManager.CustomImage> imageMap = new HashMap<>();
    // ========== image kind name tracking ==========
    private final Map<String, String> imageKindNameMap = new HashMap<>();
    // ========== image state ==========
    private final Map<String, ImageState> imageStateMap = new HashMap<>();

    public ImageManager (Stage stage, GraphicsManager graphicsManager,
                         HashSet<InteractableObject> interactableObjectSet,
                         UiManager uiManager)
    {
        this.stage = stage;
        this.graphicsManager = graphicsManager;
        this.interactableObjectSet = interactableObjectSet;
        this.uiManager = uiManager;
    }

    // ===================================================================================================================
    // Kind loading
    // ===================================================================================================================

    /**
     * 从 JSON 配置文件加载图像样式（写入 pendingPixmapMap，等 init 完成后合并）
     */
    public boolean loadImageKind (FileHandle file, FileHandle themePath, Map<String, Pixmap> pendingPixmapMap)
    {
        try
        {
            JsonEntity imageKindJson = new JsonEntity(file);
            LogUtils.debug(ImageManager.class, "loadImageKind 读取图片配置: " + imageKindJson);

            String imageKindName = imageKindJson.getString("name");
            if (imageKindName == null)
            {
                LogUtils.error(ImageManager.class, "loadImageKind 缺少 name 字段: " + imageKindJson);
                return false;
            }

            Pixmap pixmap = null;
            if (imageKindJson.containsKey("image"))
            {
                String imagePath = imageKindJson.getString("image");
                FileHandle imageFileHandle = themePath.child(PathName.ASSET_S_RESOURCE_IMAGE).child(imagePath);
                if (!imageFileHandle.exists())
                {
                    LogUtils.error(ImageManager.class, "loadImageKind 图片文件不存在: " + imageFileHandle.path());
                    return false;
                }
                pixmap = new Pixmap(imageFileHandle);
            }
            else if (imageKindJson.containsKey("color"))
            {
                String colorStr = imageKindJson.getString("color");
                if (colorStr == null)
                {
                    LogUtils.error(ImageManager.class, "loadImageKind color 字段类型不是字符串 (name): " + imageKindName);
                    return false;
                }
                Color color = Color.valueOf(colorStr);
                pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(color);
                pixmap.fill();
            }
            else
            {
                LogUtils.error(ImageManager.class, "loadImageKind 既无 image 也无 color: " + imageKindJson);
                return false;
            }

            pendingPixmapMap.put(UiManager.PIXMAP_IMAGE + imageKindName, pixmap);
            imageKindMap.put(imageKindName, null);
            LogUtils.debug(ImageManager.class, "暂存图片 pixmap: " + imageKindName);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "loadImageKind", e);
            return false;
        }
    }

    /**
     * 直接从纹理文件加载图像样式（不经过 Pixmap 合并流程）
     */
    public boolean loadImageKind (String imageKindName, FileHandle file)
    {
        try
        {
            Texture texture = graphicsManager.getTexture(file);
            ImageKind imageKind = new ImageKind(texture);
            imageKindMap.put(imageKindName, imageKind);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "loadImage 加载图片失败", e);
            return false;
        }
    }

    /**
     * 获取所有图像样式映射表
     */
    public Map<String, ImageKind> getImageKindMap ()
    {
        return imageKindMap;
    }

    /**
     * 根据名称获取图像样式
     */
    public ImageKind getImageKind (String imageKindName)
    {
        return imageKindMap.get(imageKindName);
    }

    /**
     * 移除指定名称的图像样式，并异步销毁其纹理
     */
    public boolean removeImageKind (String imageKindName)
    {
        try
        {
            ImageKind imageKind = imageKindMap.get(imageKindName);
            if (imageKind != null)
            {
                TextureRegion region = imageKind.getRegion();
                if (region != null)
                {
                    Texture texture = region.getTexture();
                    if (texture != null)
                    {
                        // 注意：此处的纹理可能来自 Pixmap 合并后的共用大纹理，不应销毁
                        // 仅当是大纹理上的 region 时才跳过销毁
                        LogUtils.debug(ImageManager.class, "removeImageKind 移除图像样式 (name): " + imageKindName);
                    }
                }
                imageKindMap.remove(imageKindName);
                return true;
            }
            else
            {
                LogUtils.error(ImageManager.class, "removeImageKind 移除图像样式失败 (name): " + imageKindName);
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "removeImageKind", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Object creation
    // ===================================================================================================================

    /**
     * 创建图片控件并添加到舞台
     */
    private boolean createImage (String imageTag, String imageKindName,
                                 float x, float y, float width, float height)
    {
        try
        {
            ImageKind imageKind = getImageKind(imageKindName);
            Image image;
            if (imageKind != null)
            {
                image = new Image(imageKind.getRegion());
            }
            else
            {
                image = new Image();
            }
            UiManager.CustomImage imageContainer = new UiManager.CustomImage(image, imageTag, uiManager);
            imageContainer.setPosition(x, y);
            imageContainer.setSize(width, height);

            imageContainer.addListener(new ClickListener()
            {
                public void clicked (InputEvent event, float x, float y)
                {
                    LogUtils.debug(ImageManager.class,
                        "image clicked: " + getImageStandardTag(imageTag));
                    if (!imageStateMap.containsKey(imageTag))
                    {
                        imageStateMap.put(imageTag, new ImageState());
                    }
                    imageStateMap.get(imageTag).setClicked();
                }
            });

            stage.addActor(imageContainer);
            imageMap.put(imageTag, imageContainer);
            imageKindNameMap.put(imageTag, imageKindName);
            imageStateMap.put(imageTag, new ImageState());
            interactableObjectSet.add(imageContainer);
            LogUtils.debug(ImageManager.class, "createImage 成功: " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "createImage 异常", e);
            return false;
        }
    }

    // ===================================================================================================================
    // Public API
    // ===================================================================================================================

    public String getImageStandardTag (String imageTag)
    {
        return "[Image] " + imageTag;
    }

    public boolean addImage (String imageTag, String imageKindName,
                             float x, float y, float width, float height)
    {
        try
        {
            if (!imageMap.containsKey(imageTag))
            {
                return createImage(imageTag, imageKindName, x, y, width, height);
            }
            else
            {
                return updateImage(imageTag, imageKindName, x, y, width, height);
            }
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "addImage", e);
            return false;
        }
    }

    public boolean addImage (ImageInfo imageInfo)
    {
        try
        {
            return addImage(
                imageInfo.getImageTag(),
                imageInfo.getImageKindName(),
                imageInfo.getX(),
                imageInfo.getY(),
                imageInfo.getWidth(),
                imageInfo.getHeight()
            );
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "addImage", e);
            return false;
        }
    }

    public boolean addImage (List<ImageInfo> imageInfoList)
    {
        try
        {
            boolean result = true;
            if (imageInfoList != null)
            {
                for (ImageInfo imageInfo : imageInfoList)
                {
                    if (!addImage(imageInfo)) result = false;
                }
            }
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "addImage", e);
            return false;
        }
    }

    public boolean addImage (List<String> imageTagList, Map<String, ImageInfo> imageInfoMap)
    {
        try
        {
            boolean result = true;
            if (imageTagList != null && imageInfoMap != null)
            {
                for (String imageTag : imageTagList)
                {
                    if (imageInfoMap.containsKey(imageTag))
                    {
                        if (!addImage(imageInfoMap.get(imageTag))) result = false;
                    }
                    else result = false;
                }
            }
            else result = false;
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "addImage", e);
            return false;
        }
    }

    public boolean updateImage (String imageTag, String imageKindName,
                                float x, float y, float width, float height)
    {
        try
        {
            LogUtils.debug(ImageManager.class, "updateImage (tag): " + imageTag + " (kind): " + imageKindName);
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "updateImage 找不到图片对象 (tag): " + imageTag);
                return false;
            }

            // 切换 kind
            if (imageKindName != null && !imageKindName.isEmpty())
            {
                ImageKind newKind = imageKindMap.get(imageKindName);
                if (newKind != null)
                {
                    imageContainer.setDrawable(new TextureRegionDrawable(newKind.getRegion()));
                    imageKindNameMap.put(imageTag, imageKindName);
                }
            }

            imageContainer.setPosition(x, y);
            imageContainer.setSize(width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "updateImage", e);
            return false;
        }
    }

    // ========== Position / Size ==========

    public boolean setImagePosition (String imageTag, float x, float y)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImagePosition 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setPosition(x, y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImagePosition", e);
            return false;
        }
    }

    public float getImageX (String imageTag)
    {
        UiManager.CustomImage imageContainer = imageMap.get(imageTag);
        return imageContainer != null ? imageContainer.getX() : 0;
    }

    public boolean setImageX (String imageTag, float x)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImageX 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setX(x);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageX", e);
            return false;
        }
    }

    public float getImageY (String imageTag)
    {
        UiManager.CustomImage imageContainer = imageMap.get(imageTag);
        return imageContainer != null ? imageContainer.getY() : 0;
    }

    public boolean setImageY (String imageTag, float y)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImageY 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setY(y);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageY", e);
            return false;
        }
    }

    public boolean setImageSize (String imageTag, float width, float height)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImageSize 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setSize(width, height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageSize", e);
            return false;
        }
    }

    public float getImageWidth (String imageTag)
    {
        UiManager.CustomImage imageContainer = imageMap.get(imageTag);
        return imageContainer != null ? imageContainer.getWidth() : 0;
    }

    public boolean setImageWidth (String imageTag, float width)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImageWidth 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setWidth(width);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageWidth", e);
            return false;
        }
    }

    public float getImageHeight (String imageTag)
    {
        UiManager.CustomImage imageContainer = imageMap.get(imageTag);
        return imageContainer != null ? imageContainer.getHeight() : 0;
    }

    public boolean setImageHeight (String imageTag, float height)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "setImageHeight 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setHeight(height);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageHeight", e);
            return false;
        }
    }

    // ========== Query ==========

    public boolean containsImage (String imageTag)
    {
        return imageMap.containsKey(imageTag);
    }

    public InteractableObject getImage (String imageTag)
    {
        return imageMap.get(imageTag);
    }

    /**
     * 获取 imageMap（供 UiManager 布局管理使用）
     */
    public Map<String, UiManager.CustomImage> getImageMap ()
    {
        return imageMap;
    }

    public Map<String, String> getImageKindNameMap ()
    {
        return imageKindNameMap;
    }

    public Map<String, ImageState> getImageStateMap ()
    {
        return imageStateMap;
    }

    // ========== Show / Hide ==========

    public boolean showImage (String imageTag)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "showImage 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setVisible(true);
            LogUtils.debug(ImageManager.class, "showImage 成功 (tag): " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "showImage", e);
            return false;
        }
    }

    public boolean hideImage (String imageTag)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "hideImage 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.setVisible(false);
            LogUtils.debug(ImageManager.class, "hideImage 成功 (tag): " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "hideImage", e);
            return false;
        }
    }

    // ========== Click ==========

    public boolean setImageClicked (String imageTag, boolean clicked)
    {
        try
        {
            if (!imageStateMap.containsKey(imageTag))
            {
                if (!clicked) return true;
                imageStateMap.put(imageTag, new ImageState());
            }
            if (clicked)
            {
                imageStateMap.get(imageTag).setClicked();
            }
            else
            {
                imageStateMap.get(imageTag).setClicked(false);
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "setImageClicked", e);
            return false;
        }
    }

    public boolean isImageClicked (String imageTag)
    {
        return imageStateMap.containsKey(imageTag) && imageStateMap.get(imageTag).isClicked();
    }

    // ========== Delete ==========

    public boolean deleteImage (String imageTag)
    {
        try
        {
            UiManager.CustomImage imageContainer = imageMap.get(imageTag);
            if (imageContainer == null)
            {
                LogUtils.error(ImageManager.class, "deleteImage 找不到图片对象 (tag): " + imageTag);
                return false;
            }
            imageContainer.remove();
            imageMap.remove(imageTag);
            imageKindNameMap.remove(imageTag);
            imageStateMap.remove(imageTag);
            interactableObjectSet.remove(imageContainer);
            LogUtils.debug(ImageManager.class, "deleteImage (tag): " + imageTag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "deleteImage", e);
            return false;
        }
    }

    public boolean deleteAllImage ()
    {
        try
        {
            LogUtils.debug(ImageManager.class, "deleteAllImage");
            for (UiManager.CustomImage imageContainer : imageMap.values())
            {
                imageContainer.remove();
                interactableObjectSet.remove(imageContainer);
            }
            imageMap.clear();
            imageKindNameMap.clear();
            imageStateMap.clear();
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(ImageManager.class, "deleteAllImage", e);
            return false;
        }
    }
}
