package com.hujiugame.qingfeng.type.ui;

import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hujiugame.qingfeng.type.ScreenSize;

public enum UseViewport
{
    STRETCH,
    FIT,
    FILL;

    private static final StretchViewport stretchViewport = new StretchViewport(ScreenSize.WIDTH, ScreenSize.HEIGHT);
    private static final FitViewport fitViewport = new FitViewport(ScreenSize.WIDTH, ScreenSize.HEIGHT);
    private static final FillViewport fillViewport = new FillViewport(ScreenSize.WIDTH, ScreenSize.HEIGHT);

    public Viewport getViewport ()
    {
        switch (this)
        {
            case FIT:
                return fitViewport;
            case FILL:
                return fillViewport;
            case STRETCH:
            default:
                    return stretchViewport;
        }
    }
}
