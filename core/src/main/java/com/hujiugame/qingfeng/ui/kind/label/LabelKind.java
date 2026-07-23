package com.hujiugame.qingfeng.ui.kind.label;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public final class LabelKind
{
    private final Label.LabelStyle labelStyle;
    private final Texture background;
    private final TextureRegion backgroundRegion;
    private final float borderScale;

    public LabelKind (Label.LabelStyle style, Texture background)
    {
        this(style, new TextureRegion(background), 1.0f);
    }

    public LabelKind (Label.LabelStyle style, TextureRegion backgroundRegion)
    {
        this(style, backgroundRegion, 1.0f);
    }

    public LabelKind (Label.LabelStyle style, TextureRegion backgroundRegion, float borderScale)
    {
        this.labelStyle = style;
        this.backgroundRegion = backgroundRegion;
        this.background = backgroundRegion.getTexture();
        this.borderScale = borderScale;
    }

    public Label.LabelStyle getLabelStyle ()
    {
        return labelStyle;
    }

    public Texture getBackground ()
    {
        return background;
    }

    public TextureRegion getBackgroundRegion ()
    {
        return backgroundRegion;
    }

    public float getBorderScale ()
    {
        return borderScale;
    }
}
