package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.file.FileName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public final class FileUtils
{
    public static final JsonEntity INTERNAL_DIRECTORY_STRUCTURE;
    static
    {
        // 读取内部的结构文件
        INTERNAL_DIRECTORY_STRUCTURE = new JsonEntity(Gdx.files.internal(FileName.INTERNAL_DIRECTORY_STRUCTURE_CONFIG));
        if (INTERNAL_DIRECTORY_STRUCTURE.isEmpty())
        {
            LogUtils.error(FileUtils.class, "static DIRECTORY_STRUCTURE.json not found");
        }
        else
        {
            LogUtils.debug(FileUtils.class, "static DIRECTORY_STRUCTURE.json loaded");
        }
    }

    /**
     * 私有构造函数，防止实例化工具类
     */
    private FileUtils ()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 静态常量，用于存储平台路径
    private static volatile FileHandle USER_HOME_FILEHANDLE;
    private static volatile FileHandle DOWNLOADS_FILEHANDLE;
    private static volatile FileHandle DOCUMENTS_FILEHANDLE;
    private static volatile FileHandle PICTURES_FILEHANDLE;
    private static volatile FileHandle MUSIC_FILEHANDLE;
    private static volatile FileHandle MOVIES_FILEHANDLE;
    private static volatile FileHandle DESKTOP_DIR_FILEHANDLE;

    /**
     * 获取用户主目录的 FileHandle
     *
     * @return 用户主目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getUserHomeFileHandle ()
    {
        return USER_HOME_FILEHANDLE;
    }

    /**
     * 获取下载目录的 FileHandle
     *
     * @return 下载目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getDownloadsFileHandle ()
    {
        return DOWNLOADS_FILEHANDLE;
    }

    /**
     * 获取文档目录的 FileHandle
     *
     * @return 文档目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getDocumentsFileHandle ()
    {
        return DOCUMENTS_FILEHANDLE;
    }

    /**
     * 获取图片目录的 FileHandle
     *
     * @return 图片目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getPicturesFileHandle ()
    {
        return PICTURES_FILEHANDLE;
    }

    /**
     * 获取音乐目录的 FileHandle
     *
     * @return 音乐目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getMusicFileHandle ()
    {
        return MUSIC_FILEHANDLE;
    }

    /**
     * 获取影片目录的 FileHandle
     *
     * @return 影片目录的 FileHandle，未初始化时可能为 null
     */
    public static FileHandle getMoviesFileHandle ()
    {
        return MOVIES_FILEHANDLE;
    }

    /**
     * 获取桌面目录的 FileHandle
     *
     * @return 桌面目录的 FileHandle，未初始化时可能为 null（Android 上为 null）
     */
    public static FileHandle getDesktopDirFileHandle ()
    {
        return DESKTOP_DIR_FILEHANDLE;
    }

    /**
     * 将平台传入的路径配置写入静态常量，供后续业务使用。
     * 请在 Main.create() 中调用一次。
     */
    public static void applyPlatformPaths(FilePathConfig config)
    {
        if (config == null) return;
        USER_HOME_FILEHANDLE = config.userHome;
        DOWNLOADS_FILEHANDLE = config.downloads;
        DOCUMENTS_FILEHANDLE = config.documents;
        PICTURES_FILEHANDLE = config.pictures;
        MUSIC_FILEHANDLE = config.music;
        MOVIES_FILEHANDLE = config.movies;
        DESKTOP_DIR_FILEHANDLE = config.desktopDir;
        LogUtils.debug(FileUtils.class, "平台路径常量已注入");
    }

    /**
     * 规范化路径：统一使用正斜杠、合并多个斜杠、移除首尾斜杠
     *
     * @param path 原始路径
     * @return 规范化后的路径，null 输入返回空字符串
     */
    public static String normalizePath (String path)
    {
        if (path == null)
            return "";

        return path
            .replace("\\", "/")  // 统一使用正斜杠
            .replaceAll("/+", "/") // 合并多个斜杠
            .replaceAll("^/|/$", ""); // 移除首尾斜杠
    }

    /**
     * 将两个路径部分拼接为一个路径（中间以 / 分隔）
     *
     * @param base 基路径
     * @param path 追加的路径部分
     * @return 拼接后的路径字符串
     */
    public static String pathCombine (String base, String path)
    {
        // 检查基路径
        if (base.isEmpty())
            return path;

        // 检查增加路径
        if (path.isEmpty())
            return base;

        return base + "/" + path;
    }

    /**
     * 将多个路径部分拼接为一个完整路径（自动规范化各段路径）
     *
     * @param base  基路径
     * @param paths 可变参数，后续要拼接的路径段
     * @return 拼接并规范化后的完整路径
     */
    public static String pathJoin (String base, String ... paths)
    {
        // 检查基路径
        if (base == null)
            base = "";

        // 检查路径集
        if (paths == null || paths.length == 0)
            return base;

        // 规范化基础路径
        String result = normalizePath(base);

        // 逐个处理路径部分
        for (String path : paths)
        {
            // 如果为空则跳过
            if (path == null)
                continue;

            // 检查路径合法性并规范化
            String normalizedPath = normalizePath(path);
            if (normalizedPath.isEmpty())
                continue;

            // 最后累加路径
            result = pathCombine(result, normalizedPath);
        }

        return result;
    }

    /**
     * 比较两个路径是否相等（规范化后比较）
     *
     * @param path1 第一个路径
     * @param path2 第二个路径
     * @return true 表示两个路径指向同一个位置
     */
    public static boolean comparePath (String path1, String path2)
    {
        // 标准化路径
        path1 = normalizePath(path1);
        path2 = normalizePath(path2);

        // 比较路径
        return path1.equals(path2);
    }

    // ===================================================================================================================

    /**
     * 获取指定目录下的文件和子目录列表。
     * Internal 类型（Android APK 内部）无法直接使用 File.list()，改用预生成的
     * directory_structure.json 替代，确保跨平台行为一致。
     *
     * @param directory 要列出文件列表的目录句柄
     * @return 文件句柄数组，目录为空或不存在时返回空数组
     */
    public static FileHandle[] getList (FileHandle directory)
    {
        if (directory.type() == Files.FileType.Internal)
        {
            // Android APK 内无法用 File.list() 列出目录，改用预生成的 directory_structure.json 替代
            String dirPath = normalizePath(directory.path());
            JsonEntity dirEntity = INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey(dirPath);
            if (dirEntity.isEmpty())
            {
                return new FileHandle[0];
            }

            List<String> childDirs = dirEntity.getStringList("directory");
            List<String> childFiles = dirEntity.getStringList("file");

            int dirCount = childDirs != null ? childDirs.size() : 0;
            int fileCount = childFiles != null ? childFiles.size() : 0;
            int total = dirCount + fileCount;

            if (total == 0)
            {
                return new FileHandle[0];
            }

            // 合并目录和文件名为统一数组后排序，保证确定性（JSON 无自然顺序）
            String[] allNames = new String[total];
            int idx = 0;
            if (childDirs != null)
            {
                for (String name : childDirs)
                {
                    allNames[idx++] = name;
                }
            }
            if (childFiles != null)
            {
                for (String name : childFiles)
                {
                    allNames[idx++] = name;
                }
            }
            Arrays.sort(allNames);

            // 与官方 FileHandle.list() 相同模式：child(relativePath)
            FileHandle[] handles = new FileHandle[total];
            for (int i = 0; i < total; i++)
            {
                handles[i] = directory.child(allNames[i]);
            }
            return handles;
        }
        else
        {
            return directory.list();
        }
    }

    // ===================================================================================================================

    /**
     * 判断文件或目录是否存在（不区分文件/目录类型）。
     * Android 内部资源通过 directory_structure.json 判断，避免 File.exists() 异常。
     *
     * @param file 要检查存在性的文件或目录句柄
     * @return true 表示存在（无论是文件还是目录）
     */
    public static boolean isExist (FileHandle file)
    {
        try
        {
            boolean result = false;
            if (file != null)
            {
                if (file.type() == Files.FileType.Internal || file.type() == Files.FileType.Local)
                {
                    if (PlatformUtils.isAndroid())
                    {
                        return INTERNAL_DIRECTORY_STRUCTURE.containsKey(file.parent().path()) || INTERNAL_DIRECTORY_STRUCTURE.containsKey(file.path());
                    }
                }

                result = file.exists();
            }
            if (result) LogUtils.debug(FileUtils.class, "isExist 检查 " + file.type() + " : " + file.path() + " 存在结果 " + true);
            else LogUtils.debug(FileUtils.class, "isExist 检查 " + file.type() + " : " + file.path() + " 不存在结果 " + false);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "isExist", e);
            return false;
        }
    }


    /**
     * 判断文件是否存在（排除目录）。
     * 先通过 isExist 确认存在性，再排除目录类型以确保返回值确实对应一个文件。
     *
     * @param file 要检查存在性的文件句柄（返回值 true 保证不是目录）
     * @return true 表示存在且是文件（不是目录）
     */
    public static boolean isFileExist (FileHandle file)
    {
        try
        {
            boolean result = false;
            if (isExist(file))
            {
                if (file.type() == Files.FileType.Internal)
                {
                    if (PlatformUtils.isAndroid())
                    {
                        return INTERNAL_DIRECTORY_STRUCTURE.containsKey(file.parent().path());
                    }
                }

                result = !file.isDirectory();
            }
            if (result) LogUtils.debug(FileUtils.class, "isFileExist 检查 " + file.type() + " : " + file.path() + " 文件存在结果 " + true);
            else LogUtils.debug(FileUtils.class, "isFileExist 检查 " + file.type() + " : " + file.path() + " 文件不存在结果 " + false);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "isFileExist", e);
            return false;
        }
    }

    /**
     * 判断目录是否存在（排除文件）。
     * Android/Desktop 的 Internal 类型通过 directory_structure.json 判断；
     * 其他类型通过 FileHandle.isDirectory() 判断。
     *
     * @param file 要检查存在性的目录句柄（返回值 true 保证不是文件）
     * @return true 表示存在且是目录（不是文件）
     */
    public static boolean isDirectoryExist (FileHandle file)
    {
        try
        {
            boolean result = false;
            if (isExist(file))
            {
                if (file.type() == Files.FileType.Internal)
                {
                    if (PlatformUtils.isAndroid() || PlatformUtils.isDesktop())
                    {
                        return INTERNAL_DIRECTORY_STRUCTURE.containsKey(file.path());
                    }
                }
                result = file.isDirectory();
            }
            if (result) LogUtils.debug(FileUtils.class, "isDirectoryExist 检查 " + file.type() + " : " + file.path() + " 文件夹存在结果 " + true);
            else LogUtils.debug(FileUtils.class, "isDirectoryExist 检查 " + file.type() + " : " + file.path() + " 文件夹不存在结果 " + false);
            return result;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "isDirectoryExist", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 以追加模式将字符串写入文件（日志专用）。
     * 写入过程中不再使用 LogUtils 以避免死循环；自动创建父目录。
     *
     * @param content 要写入的日志文本内容（UTF-8 编码）
     * @param file    日志目标文件句柄（追加写入，不覆盖已有内容）
     * @return true 表示写入成功
     */
    public static boolean createStringFileOfLog (String content, FileHandle file)
    {
        try
        {
            // 确保父目录存在，避免因目录不存在导致写失败
            file.parent().mkdirs();
            // 将字符串按 UTF-8 转成字节数组（核心：指定编码，避免乱码）
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            // 写入字节数组（append=true 追加，false 覆盖，与原逻辑一致）
            file.writeBytes(contentBytes, true);
            // 严禁Log debug,因为Log 时也会触发此方法，会造成死循环
            return true;
        }
        catch (Exception e)
        {
            // 严禁Log debug,因为失败Log 时也会触发此方法，会造成死循环
            System.out.println("FileUtils createStringFileOfLog 创建文件失败：" + file.path() + " 错误：" + Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    /**
     * 将字符串写入文件（UTF-8 编码）。
     * 不自动创建父目录，调用前需确保父目录存在。
     *
     * @param content 要写入的字符串内容（UTF-8 编码）
     * @param file    目标文件句柄（写入的目标路径）
     * @param append  true 追加到文件末尾，false 覆盖写入
     * @return true 表示写入成功
     */
    public static boolean createStringFile (String content, FileHandle file, boolean append)
    {
        try
        {
            // 将字符串按 UTF-8 转成字节数组（核心：指定编码，避免乱码）
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            // 写入字节数组（append=true 追加，false 覆盖，与原逻辑一致）
            file.writeBytes(contentBytes, append);
            // debug
            LogUtils.debug(FileUtils.class, "createStringFile 写入文件：" + file.path());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "createStringFile", e);
            return false;
        }
    }

    /**
     * 从文件中读取字符串内容（UTF-8 编码）。
     * 以字节流方式读取，避免平台默认编码导致的乱码问题。
     *
     * @param file 要读取的文件句柄
     * @return 文件内容的 UTF-8 字符串，读取失败时返回 null
     */
    public static String readStringFile (FileHandle file)
    {
        try
        {
            // 读取文件全部字节（字节流不涉及编码，完全保留原始内容）
            byte[] fileBytes = file.readBytes();

            // 用 UTF-8 编码将字节数组转成字符串（仅解码，不修改内容）
            String fileString = new String(fileBytes, StandardCharsets.UTF_8);

            // debug
            LogUtils.debug(FileUtils.class, "readStringFile 读取文件: " + file.path());
            return fileString;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "readStringFile", e);
            return null;
        }
    }

    // ===================================================================================================================

    /**
     * 复制文件
     *
     * @param sourceFile 源文件句柄
     * @param destFile   目标文件句柄
     * @return true 表示复制成功
     */
    public static boolean copyFile (FileHandle sourceFile, FileHandle destFile)
    {
        try
        {
            // 复制文件
            sourceFile.copyTo(destFile);
            LogUtils.debug(FileUtils.class, "copyFile 复制 " + sourceFile.type() + ":" + sourceFile.path() + " 到 " + destFile.type() + ":" + destFile.path());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "copyFile", e);
            return false;
        }
    }

    /**
     * 移动文件
     *
     * @param sourceFile 源文件句柄
     * @param destFile   目标文件句柄
     * @return true 移动成功
     */
    public static boolean moveFile (FileHandle sourceFile, FileHandle destFile)
    {
        try
        {
            sourceFile.moveTo(destFile);
            LogUtils.debug(FileUtils.class, "moveFile 移动 " + sourceFile.type() + ":" + sourceFile.path() + " 到 " + destFile.type() + ":" + destFile.path());
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "moveFile", e);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param file 文件句柄
     * @return true 删除成功
     */
    public static boolean deleteFile (FileHandle file)
    {
        try
        {
            return file.delete();
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "deleteFile", e);
            return false;
        }
    }

    // ===================================================================================================================

    /**
     * 递归复制目录（通用版本，通过 FileHandle.list() 遍历）
     *
     * @param sourceDirectory 源目录句柄
     * @param destDirectory   目标目录句柄（必须已存在或由调用方创建）
     */
    private static void copyDirectoryRecursive (FileHandle sourceDirectory, FileHandle destDirectory)
    {
        try
        {
            FileHandle [] files = sourceDirectory.list();

            // 遍历目录下
            for (FileHandle childFile : files)
            {
                FileHandle newChildFile = destDirectory.child(childFile.name());

                if (isFileExist(childFile))
                {
                    childFile.copyTo(newChildFile);
                    LogUtils.debug(FileUtils.class, "copyDirectoryRecursive 复制 " + childFile.type() + ":" + childFile.path() + " 到 " + newChildFile.type() + ":" + newChildFile.path());
                }
                else
                {
                    newChildFile.mkdirs();
                    copyDirectoryRecursive(childFile, newChildFile);
                }
            }
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "copyDirectoryRecursive", e);
            throw e;
        }
    }

    /**
     * 递归复制目录（内部资源专用版本，通过目录结构配置遍历）。
     * Android Internal 类型无法使用 File.list()，改用 directory_structure.json 遍历。
     *
     * @param sourceDirectoryPath 源目录相对于 Internal 根部的路径（如 "ui/theme/default"）
     * @param destDirectory       目标目录句柄（写入目标）
     */
    private static void copyDirectoryRecursiveFix (String sourceDirectoryPath, FileHandle destDirectory)
    {
        try
        {
            // 获取自己目录的结构
            JsonEntity dirStructure = INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey(sourceDirectoryPath);
            List<String> childDirectoryList = dirStructure.getStringList("directory");
            List<String> childFileList = dirStructure.getStringList("file");

            // 遍历目录继续递归复制
            for (String childDirectory : childDirectoryList)
            {
                FileHandle destChildDirectory = destDirectory.child(childDirectory);
                destChildDirectory.mkdirs();
                copyDirectoryRecursiveFix(pathCombine(sourceDirectoryPath, childDirectory), destChildDirectory);
            }

            // 遍历文件复制
            for (String childFileName : childFileList)
            {
                FileHandle destChildFile = destDirectory.child(childFileName);
                FileHandle sourceChildFile = Gdx.files.internal(pathCombine(sourceDirectoryPath, childFileName));
                sourceChildFile.copyTo(destChildFile);
                LogUtils.debug(FileUtils.class,"copyDirectoryRecursiveFix 复制 " + sourceChildFile.type() + ":" + sourceChildFile.path() + " 到 " + destChildFile.type() + ":" + destChildFile.path());
            }
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "copyDirectoryRecursiveFix", e);
            throw e;
        }
    }

    /**
     * 递归删除目录及其所有子文件和子目录
     *
     * @param file 要递归删除的目录句柄（删除后该句柄将不可用）
     */
    private static void deleteDirectoryRecursive (FileHandle file)
    {
        try
        {
            // 获取文件列表
            FileHandle [] files = getList(file);

            // 遍历目录下
            for (FileHandle childFile : files)
            {
                if (isFileExist(childFile))
                {
                    childFile.delete();
                    LogUtils.debug(FileUtils.class,"删除 " + childFile.type() + ":" + childFile.path());
                }
                else
                {
                    deleteDirectoryRecursive(childFile);
                }
            }

            // 删除自己
            file.delete();
            LogUtils.debug(FileUtils.class,"删除 " + file.type() + ":" + file.path());
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "deleteDirectoryRecursive", e);
            throw e;
        }
    }

    /**
     * 复制目录（支持文件和目录两种源类型）。
     * Internal 类型使用 directory_structure.json 加速遍历，避免 File.list() 异常。
     * 源为文件时直接调用 copyFile；源为目录时递归复制所有子内容。
     *
     * @param sourceDirectory 源目录（或文件）句柄
     * @param destDirectory   目标目录句柄
     * @return true 表示复制成功
     */
    public static boolean copyDirectory (FileHandle sourceDirectory, FileHandle destDirectory)
    {
        try
        {
            // 判断源基路径是文件
            if (!isDirectoryExist(sourceDirectory))
            {
                if (isFileExist(sourceDirectory))
                {
                    copyFile(sourceDirectory, destDirectory);
                    LogUtils.debug(FileUtils.class,"复制 " + sourceDirectory.type() + ":" + sourceDirectory.path() + " 到 " + destDirectory.type() + ":" + destDirectory.path());
                    return true;
                }
                else
                {
                    LogUtils.error(FileUtils.class,"源路径 " + sourceDirectory.type() + ":" + sourceDirectory.path() + " 不存在");
                    return false;
                }

            }

            // 判断目标基路径文件夹是否存在
            if (!isDirectoryExist(destDirectory))
            {
                destDirectory.mkdirs();
            }

            // 递归复制文件
            if (sourceDirectory.type() == Files.FileType.Internal)
                copyDirectoryRecursiveFix(sourceDirectory.path(), destDirectory);
            else
                copyDirectoryRecursive(sourceDirectory, destDirectory);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "copyDirectory", e);
            return false;
        }
    }

    /**
     * 递归删除目录及其所有内容。
     * 先递归删除子文件和子目录，再删除自身。删除过程不可逆。
     *
     * @param directory 要递归删除的目录句柄（删除后该句柄将不可用）
     * @return true 表示删除成功
     */
    public static boolean deleteDirectory (FileHandle directory)
    {
        try
        {
            // 递归删除
            deleteDirectoryRecursive(directory);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "deleteDirectory", e);
            return false;
        }
    }

    /**
     * 移动目录（先复制源目录到目标，再递归删除源目录）。
     * 复制阶段失败时不会删除源目录，保证数据安全。
     *
     * @param sourceDirectory 源目录句柄（移动后将被删除）
     * @param destDirectory   目标目录句柄（移动后源内容出现在此路径下）
     * @return true 表示移动成功
     */
    public static boolean moveDirectory (FileHandle sourceDirectory, FileHandle destDirectory)
    {
        try
        {
            // 复制
            if (copyDirectory(sourceDirectory, destDirectory))
            {
                // 删除
                deleteDirectory(sourceDirectory);
            }
            else
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "moveDirectory", e);
            return false;
        }
    }

    /**
     * 清空目录下的所有文件和子目录（但不删除目录本身）。
     * 使用 getList 安全遍历，Internal 类型通过 directory_structure.json 判断。
     *
     * @param directory 要清空的目录句柄（清空后目录本身仍然存在）
     * @return true 表示清空成功
     */
    public static boolean clearDirectory (FileHandle directory)
    {
        try
        {
            // 获取文件列表
            FileHandle [] files = getList(directory);

            // 遍历目录下
            for (FileHandle childFile : files)
            {
                if (isFileExist(childFile))
                {
                    childFile.delete();
                    LogUtils.debug(FileUtils.class, "删除 " + childFile.type() + ":" + childFile.path());
                }
                else
                {
                    deleteDirectoryRecursive(childFile);
                }
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(FileUtils.class, "clearDirectory", e);
            return false;
        }
    }

}
