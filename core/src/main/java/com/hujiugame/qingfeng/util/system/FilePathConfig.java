package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.files.FileHandle;

public class FilePathConfig {
    public final FileHandle userHome;
    public final FileHandle downloads;
    public final FileHandle documents;
    public final FileHandle pictures;
    public final FileHandle music;
    public final FileHandle movies;
    public final FileHandle desktopDir;  // 桌面有，安卓传 null

    /**
     * 构造平台路径配置对象
     *
     * @param userHome   用户主目录
     * @param downloads  下载目录
     * @param documents  文档目录
     * @param pictures   图片目录
     * @param music      音乐目录
     * @param movies     影片目录
     * @param desktopDir 桌面目录（安卓上传 null）
     */
    public FilePathConfig(FileHandle userHome, FileHandle downloads,
                          FileHandle documents, FileHandle pictures,
                          FileHandle music, FileHandle movies,
                          FileHandle desktopDir) {
        this.userHome = userHome;
        this.downloads = downloads;
        this.documents = documents;
        this.pictures = pictures;
        this.music = music;
        this.movies = movies;
        this.desktopDir = desktopDir;
    }
}
