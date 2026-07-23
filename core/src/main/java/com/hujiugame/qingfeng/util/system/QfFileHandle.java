package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * 继承 libGDX FileHandle，重写 toString() 输出格式为 "type:path"，
 * 避免调试/日志中只有路径没有文件类型。所有返回 FileHandle 的方法
 * 均重写以返回 QfFileHandle，确保整条调用链上的 toString 行为一致
 */
public class QfFileHandle extends FileHandle
{

    /**
     * 包装一个已有的 FileHandle
     *
     * @param source 源 FileHandle
     */
    public QfFileHandle (FileHandle source)
    {
        this.file = source.file();
        this.type = source.type();
    }

    @Override
    public String toString ()
    {
        return type + ":" + file.getPath().replace('\\', '/');
    }

    @Override
    public QfFileHandle child (String name)
    {
        return new QfFileHandle(super.child(name));
    }

    @Override
    public QfFileHandle sibling (String name)
    {
        return new QfFileHandle(super.sibling(name));
    }

    @Override
    public QfFileHandle parent ()
    {
        return new QfFileHandle(super.parent());
    }

    @Override
    public QfFileHandle[] list ()
    {
        return wrapArray(super.list());
    }

    @Override
    public QfFileHandle[] list (FileFilter filter)
    {
        return wrapArray(super.list(filter));
    }

    @Override
    public QfFileHandle[] list (FilenameFilter filter)
    {
        return wrapArray(super.list(filter));
    }

    @Override
    public QfFileHandle[] list (String suffix)
    {
        return wrapArray(super.list(suffix));
    }

    /**
     * 将 FileHandle[] 全部包装为 QfFileHandle[]
     */
    private static QfFileHandle[] wrapArray (FileHandle[] source)
    {
        QfFileHandle[] result = new QfFileHandle[source.length];
        for (int i = 0; i < source.length; i++)
        {
            result[i] = new QfFileHandle(source[i]);
        }
        return result;
    }
}
