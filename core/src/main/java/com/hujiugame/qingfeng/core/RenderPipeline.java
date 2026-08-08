package com.hujiugame.qingfeng.core;

import com.hujiugame.qingfeng.data.game.GameStateDataContainer;
import com.hujiugame.qingfeng.data.play.PlayLocalData;
import com.hujiugame.qingfeng.scene.GameRender;
import com.hujiugame.qingfeng.scene.GameRenderRegistry;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.function.Consumer;

public final class RenderPipeline
{
    private final GameRenderRegistry registry;
    private final Consumer<Float> inputUpdater;

    private GameRender gameRender;

    public RenderPipeline (GameRenderRegistry registry, Consumer<Float> inputUpdater)
    {
        this.registry = registry;
        this.inputUpdater = inputUpdater;
    }

    /**
     * 初始化渲染器，清空当前渲染引用
     *
     * @param playLocalData 游戏数据容器
     * @return 是否初始化成功
     */
    public boolean init (PlayLocalData playLocalData)
    {
        try
        {
            this.gameRender = null;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(RenderPipeline.class, "init", e);
            return false;
        }
    }

    /**
     * 根据状态结构从注册表获取对应的渲染机并初始化
     *
     * @param gameStateDataContainer 游戏状态数据容器
     * @return 是否成功获取并初始化渲染机
     */
    public boolean update (GameStateDataContainer gameStateDataContainer)
    {
        try
        {
            LogUtils.debug(RenderPipeline.class, "update 开始创建游戏渲染机");

            // 创建游戏渲染机
            this.gameRender = registry.get(gameStateDataContainer.getStateStructure());
            LogUtils.debug(RenderPipeline.class, "update 获取游戏渲染机成功");

            // 初始化游戏渲染机
            if (this.gameRender != null)
            {
                this.gameRender.init(gameStateDataContainer);
                LogUtils.debug(RenderPipeline.class, "update 游戏渲染机初始化成功" +
                    " (state): " + gameStateDataContainer.getStateStructure().getState() +
                    " (subState): " + gameStateDataContainer.getStateStructure().getSubState());
                return true;
            }
            else
            {
                LogUtils.error(RenderPipeline.class, "update 游戏渲染机为null" +
                    " (state): " + gameStateDataContainer.getStateStructure().getState() +
                    " (subState): " + gameStateDataContainer.getStateStructure().getSubState());
                return false;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(RenderPipeline.class, "update", e);
            return false;
        }
    }

    /**
     * 调用当前渲染机的帧更新（包含输入处理）
     *
     * @param deltaTime 距上一帧的时间差
     */
    public void updateFrame (float deltaTime)
    {
        try
        {
            // 调用当前渲染机的帧更新（包含输入处理）
            if (gameRender != null)
            {
                // 调用当前渲染机的帧更新
                gameRender.update(deltaTime);
                if (inputUpdater != null) inputUpdater.accept(deltaTime); // 调用输入处理
            }
            else
            {
                LogUtils.error(RenderPipeline.class, "updateFrame 游戏渲染机为null");
            }
        }
        catch (Exception e)
        {
            LogUtils.error(RenderPipeline.class, "updateFrame", e);
            throw e;
        }
    }

    /**
     * 渲染当前渲染机的内容
     *
     * @param deltaTime 距上一帧的时间差
     */
    public void render (float deltaTime)
    {
        try
        {
            // 调用当前渲染机的渲染
            if (gameRender != null)
            {
                gameRender.render(deltaTime);
            }
            else
            {
                LogUtils.error(RenderPipeline.class, "render 游戏渲染机为null");
            }
        }
        catch (Exception e)
        {
            LogUtils.error(RenderPipeline.class, "render", e);
            throw e;
        }
    }

    /**
     * 销毁当前渲染机（不报错的重置版本）
     *
     * @return 始终返回 true
     */
    public boolean clear ()
    {
        try
        {
            if (gameRender != null)
            {
                gameRender.dispose();
                gameRender = null;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.debug(RenderPipeline.class, "clear 删除渲染机出现意外 问题不大", e);
            return true;
        }
    }

    /**
     * 销毁当前渲染机并释放资源
     *
     * @return 是否销毁成功
     */
    public boolean dispose ()
    {
        try
        {
            if (gameRender != null)
            {
                gameRender.dispose();
                gameRender = null;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(RenderPipeline.class, "dispose", e);
            return false;
        }
    }
}
