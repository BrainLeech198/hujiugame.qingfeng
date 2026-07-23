package com.hujiugame.qingfeng.ui.kind.button;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public final class ButtonKind
{
    private final TextButton.TextButtonStyle buttonStyle;
    private final FileHandle audioClick;
    private final float borderScale;

    public ButtonKind (TextButton.TextButtonStyle buttonStyle, FileHandle audioClick)
    {
        this(buttonStyle, audioClick, 1.0f);
    }

    public ButtonKind (TextButton.TextButtonStyle buttonStyle, FileHandle audioClick, float borderScale)
    {
        this.buttonStyle = buttonStyle;
        this.audioClick = audioClick;
        this.borderScale = borderScale;
    }

    public TextButton.TextButtonStyle getButtonStyle ()
    {
        return buttonStyle;
    }

    public FileHandle getAudioClick ()
    {
        return audioClick;
    }

    public float getBorderScale ()
    {
        return borderScale;
    }
}
