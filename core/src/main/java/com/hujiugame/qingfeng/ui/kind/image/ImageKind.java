package com.hujiugame.qingfeng.ui.kind.image;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class ImageKind
{
    private final Texture texture;          // 保留，兼容旧代码
    private final TextureRegion region;     // 合并后使用的区域

    /**
     * 构造器：用于独立纹理（未使用合并方案时）
     *
     * @param texture 纹理对象
     */
    public ImageKind (Texture texture)
    {
        this.texture = texture;
        this.region = new TextureRegion(texture);
    }

    /**
     * 构造器：用于合并后的纹理区域
     *
     * @param region 纹理区域
     */
    public ImageKind (TextureRegion region)
    {
        this.region = region;
        this.texture = region.getTexture(); // 纹理是大图的引用
    }

    /**
     * 获取纹理
     */
    public Texture getTexture ()
    {
        return texture;
    }

    /**
     * 获取纹理区域
     */
    public TextureRegion getRegion ()
    {
        return region;
    }
}
