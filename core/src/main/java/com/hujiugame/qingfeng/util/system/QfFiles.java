package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/**
 * 包装 libGDX 的 Files 实现，将 getFileHandle / classpath / internal / external /
 * absolute / local 返回的 FileHandle 替换为 QfFileHandle，使 toString() 统一
 * 输出 "type:path" 格式。在启动器中用 {@code Gdx.files = new QfFiles(Gdx.files)}
 * 一行替换即可生效
 */
public class QfFiles implements Files
{

    private final Files delegate;

    /**
     * @param delegate 真实的平台 Files 实现（Lwjgl3Files / AndroidFiles）
     */
    public QfFiles (Files delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public FileHandle getFileHandle (String path, FileType type)
    {
        return wrap(delegate.getFileHandle(path, type));
    }

    @Override
    public FileHandle classpath (String path)
    {
        return wrap(delegate.classpath(path));
    }

    @Override
    public FileHandle internal (String path)
    {
        return wrap(delegate.internal(path));
    }

    @Override
    public FileHandle external (String path)
    {
        return wrap(delegate.external(path));
    }

    @Override
    public FileHandle absolute (String path)
    {
        return wrap(delegate.absolute(path));
    }

    @Override
    public FileHandle local (String path)
    {
        return wrap(delegate.local(path));
    }

    @Override
    public String getExternalStoragePath ()
    {
        return delegate.getExternalStoragePath();
    }

    @Override
    public boolean isExternalStorageAvailable ()
    {
        return delegate.isExternalStorageAvailable();
    }

    @Override
    public String getLocalStoragePath ()
    {
        return delegate.getLocalStoragePath();
    }

    @Override
    public boolean isLocalStorageAvailable ()
    {
        return delegate.isLocalStorageAvailable();
    }

    /**
     * 将 FileHandle 包装为 QfFileHandle，null 安全
     */
    private static FileHandle wrap (FileHandle source)
    {
        return source == null ? null : new QfFileHandle(source);
    }
}
