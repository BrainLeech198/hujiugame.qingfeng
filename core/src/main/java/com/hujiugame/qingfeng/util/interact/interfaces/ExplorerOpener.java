package com.hujiugame.qingfeng.util.interact.interfaces;

import com.badlogic.gdx.files.FileHandle;

/**
 * 资源管理器打开器接口，各平台注入实现以打开系统文件管理器。
 */
@FunctionalInterface
public interface ExplorerOpener
{
    /**
     * 在系统资源管理器中打开指定路径
     *
     * @param path 要打开的文件或文件夹路径
     */
    void open(FileHandle path);
}
