package com.hujiugame.qingfeng.util.interact.interfaces;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * 原生文件选择器提供者接口，各平台注入实现以提供平台相关的文件选择器。
 */
@FunctionalInterface
public interface NativeFileChooserProvider
{
    /**
     * 获取平台相关的原生文件选择器实例
     *
     * @return NativeFileChooser 实例
     */
    NativeFileChooser getFileChooser();
}
