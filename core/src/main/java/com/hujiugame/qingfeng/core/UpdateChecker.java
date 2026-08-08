package com.hujiugame.qingfeng.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.hujiugame.qingfeng.di.InstanceContent;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.type.VersionType;
import com.hujiugame.qingfeng.type.file.FileName;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.ConfigKey;
import com.hujiugame.qingfeng.type.key.VersionKey;
import com.hujiugame.qingfeng.type.url.WebSite;
import com.hujiugame.qingfeng.util.system.CrashUtils;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.List;

public final class UpdateChecker
{
    /**
     * 资源修复模式
     */
    public enum RepairMode
    {
        /** 保守修复：仅删除 app_version.json，触发增量更新，保留玩家配置 */
        CONSERVATIVE,
        /** 彻底修复：删除全部外部资源文件后重新同步 */
        FULL
    }

    private final InstanceContent instanceContent;

    private String internalVersionString;
    private int internalMajor;
    private int internalMinor;
    private int internalPatch;
    private String displayVersionString;

    private int internalAppVersion;
    private int internalAppVersionType;

    private boolean doDetectUpdateFinish = false;
    private boolean doDetectSuccess = false;
    private boolean needVersionUpdate = false;
    private boolean doFileUpdateFinish = false;

    /** init() 正在执行中，防止重入 */
    private volatile boolean initRunning = false;

    // 外部存储根目录句柄
    private final FileHandle baseExternalHandle;

    /**
     * 构造更新控制器
     *
     * @param instanceContent 游戏实例内容管理器，用于访问全局共享数据
     */
    public UpdateChecker (InstanceContent instanceContent)
    {
        this.instanceContent = instanceContent;
        this.baseExternalHandle = Gdx.files.external(PathName.BASE);
    }

    /**
     * 解析版本字符串为三个整数
     *
     * @param version 格式为 "major.minor.patch"
     * @return int[]{major, minor, patch}
     * @throws IllegalArgumentException 格式不正确或包含非数字
     */
    private int[] parseVersion (String version)
    {
        if (version == null || version.trim().isEmpty())
        {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        String[] parts = version.split("\\.");
        if (parts.length != 3)
        {
            throw new IllegalArgumentException("Invalid version format: " + version +
                ". Expected format: major.minor.patch");
        }
        try
        {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return new int[]{major, minor, patch};
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Version parts must be integers: " + version, e);
        }
    }

    /**
     * 判断外部版本（内容）是否 Major 兼容
     * 条件：主版本号必须相等（不同主版本完全不兼容）
     *
     * @param versionString 外部版本字符串（内容版本）
     * @return true 如果主版本号相等；否则 false
     */
    public boolean doMajorCompatible (String versionString)
    {
        try
        {
            int[] external = parseVersion(versionString);
            return this.internalMajor == external[0];
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    /**
     * 判断外部版本（内容）是否 Minor 兼容
     * 条件：主版本号相等，且软件次版本号 >= 内容次版本号
     *
     * @param versionString 外部版本字符串（内容版本）
     * @return true 如果满足条件；否则 false
     */
    public boolean doMinorCompatible (String versionString)
    {
        try
        {
            int[] external = parseVersion(versionString);
            return this.internalMajor == external[0] && this.internalMinor >= external[1];
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    /**
     * 判断外部版本（内容）是否 Patch 兼容
     * 条件：主版本号相等，次版本号相等，且软件修订号 >= 内容修订号
     *
     * @param versionString 外部版本字符串（内容版本）
     * @return true 如果满足条件；否则 false
     */
    public boolean doPatchCompatible (String versionString)
    {
        try
        {
            int[] external = parseVersion(versionString);
            return this.internalMajor == external[0] &&
                this.internalMinor == external[1] &&
                this.internalPatch >= external[2];
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    /**
     * 比较两个版本号字符串的大小
     *
     * @param version1 第一个版本号，格式为 "major.minor.patch"
     * @param version2 第二个版本号，格式为 "major.minor.patch"
     * @return 负数表示 version1 &lt; version2，0 表示相等，正数表示 version1 &gt; version2
     */
    public int compare (String version1, String version2)
    {
        int[] v1 = parseVersion(version1);
        int[] v2 = parseVersion(version2);

        // 先比较 major
        if (v1[0] != v2[0])
        {
            return Integer.compare(v1[0], v2[0]);
        }
        // major 相等，比较 minor
        if (v1[1] != v2[1])
        {
            return Integer.compare(v1[1], v2[1]);
        }
        // major 和 minor 都相等，比较 patch
        return Integer.compare(v1[2], v2[2]);
    }

    /**
     * 生成格式化的版本显示字符串
     *
     * @param appVersionType   应用版本类型
     * @param appVersionString 应用版本号字符串
     * @return 格式化后的版本显示字符串，格式如 "v1.0.0-beta"
     */
    private String generateVersionString (int appVersionType, String appVersionString)
    {
        // v1.0.0(0)-beta
        return "v" + appVersionString + "-" + VersionType.getVersionTypeName(appVersionType);
    }

    /**
     * 检测本地资源文件版本与内部资源版本是否不同，并初始化版本号字段
     *
     * @return true 表示版本不同需要更新；false 表示版本一致无需更新
     */
    private boolean doFileVersionDifferent ()
    {
        try
        {
            // 直接引用 FileUtils 公开的目录结构常量

            // 资源初始化
            FileHandle internalVersionFileHandle = Gdx.files.internal(FileUtils.pathJoin(PathName.ASSET, FileName.APP_VERSION));
            FileHandle externalVersionFileHandle = baseExternalHandle.child(PathName.ASSET).child(FileName.APP_VERSION);

            // 获取运行内部资源版本号
            JsonEntity internalAppVersionJson = new JsonEntity(internalVersionFileHandle);
            int internalAppVersion = internalAppVersionJson.getInt(VersionKey.APP_VERSION);
            int internalAppVersionType = internalAppVersionJson.getInt(VersionKey.APP_VERSION_TYPE);
            String internalAppVersionString = internalAppVersionJson.getString(VersionKey.APP_VERSION_STRING);

            // 是否需要差异更新
            boolean needUpdate = false;

            // 文件丢失
            if (!FileUtils.isFileExist(externalVersionFileHandle))
            {
                needUpdate = true;
                LogUtils.error(UpdateChecker.class, "doVersionDifferent 找不到版本文件 file: " + externalVersionFileHandle);
            }
            // 读取判断
            else
            {
                // 读取外部资源配置版本号
                JsonEntity externalAppVersionJson = new JsonEntity(externalVersionFileHandle);
                int externalAppVersionType = externalAppVersionJson.getInt(VersionKey.APP_VERSION_TYPE);
                String externalAppVersionString = externalAppVersionJson.getString(VersionKey.APP_VERSION_STRING);

                // 版本不一致
                if (!internalAppVersionString.equals(externalAppVersionString))
                {
                    needUpdate = true;
                    LogUtils.info(UpdateChecker.class, "doVersionDifferent 资源文件版本不一致，进行文件更新 "
                        + "文件版本号: " + generateVersionString(internalAppVersionType, internalAppVersionString) + " "
                        + "运行版本号: " + generateVersionString(externalAppVersionType, externalAppVersionString));
                }
            }

            // 存储版本
            this.internalVersionString = internalAppVersionString;
            this.internalAppVersion = internalAppVersion;
            this.internalAppVersionType = internalAppVersionType;
            this.displayVersionString = generateVersionString(internalAppVersionType, internalAppVersionString);
            int[] parts = parseVersion(internalVersionString);
            this.internalMajor = parts[0];
            this.internalMinor = parts[1];
            this.internalPatch = parts[2];

            // 显示版本信息
            LogUtils.info(UpdateChecker.class, "doVersionDifferent "
                + "当前版本号: " + this.displayVersionString);

            // 返回更新需求
            return needUpdate;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "doVersionDifferent", e);
            throw (e);
        }
    }

    /**
     * 加载文件更新配置（update_config.json）
     *
     * @return 更新配置的 JsonEntity，加载失败或内部资源损坏时返回 null
     */
    @javax.annotation.Nullable
    private JsonEntity loadFileUpdateConfig ()
    {
        try
        {
            // 读取更新文件规则
            FileHandle updateConfigHandle = Gdx.files.internal(FileName.UPDATE_CONFIG);
            JsonEntity updateConfigJson = new JsonEntity(updateConfigHandle);

            // 读取不到正确的json配置
            if (updateConfigJson.isEmpty())
            {
                LogUtils.error(UpdateChecker.class, "loadUpdateConfig 内部资源损坏 (file): " + updateConfigHandle);
            }

            // 返回json配置
            return updateConfigJson;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "loadUpdateConfig", e);
            return null;
        }
    }

    /**
     * 将被保护的文件移动到备份目录，并在备份中合并新旧 JSON 内容
     *
     * @param protectFileList 需要保护的文件路径列表
     * @return true 表示所有保护文件处理成功；false 表示处理过程中出错
     */
    private boolean moveProtectExternalFile (List<String> protectFileList)
    {
        try
        {
            // 把被保护的文件移动到备份目录
            LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 开始保护文件");

            // 遍历保护列表
            for (String internalPath : protectFileList)
            {
                FileHandle externalPathHandle = baseExternalHandle.child(internalPath);
                FileHandle backupPathHandle = baseExternalHandle.child(PathName.TEMP).child(internalPath);

                // 被保护文件不存在
                if (!FileUtils.isFileExist(externalPathHandle))
                {
                    LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 找不到被保护文件 (file): " + externalPathHandle);
                    // 直接复制内部
                    if (!FileUtils.copyFile(Gdx.files.internal(internalPath), backupPathHandle))
                    {
                        return false;
                    }
                }
                else
                {
                    // 如果是不是json文件则不做操作
                    if (!internalPath.endsWith(".json"))
                    {
                        LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 保护文件 忽略非json文件: " + externalPathHandle);
                    }
                    else
                    {
                        // 复制文件
                        LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 保护文件 (file): " + externalPathHandle);
                        if (!FileUtils.copyFile(externalPathHandle, backupPathHandle))
                        {
                            return false;
                        }

                        // 并追加内容
                        JsonEntity oldFileContent = new JsonEntity(backupPathHandle);
                        JsonEntity newFileContent = new JsonEntity(Gdx.files.internal(internalPath));
                        LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 旧版文件 (json): " + oldFileContent);
                        LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 新版文件 (json): " + newFileContent);

                        // 合并
                        JsonEntity combineFileContent = oldFileContent.combined(newFileContent);
                        LogUtils.debug(UpdateChecker.class, "moveProtectExternalFile 合并文件 (json): " + combineFileContent);

                        // 覆盖
                        if (!FileUtils.createStringFile(combineFileContent.getJsonString(), backupPathHandle, false))
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "moveProtectExternalFile", e);
            return false;
        }
    }

    /**
     * 删除外部资源中内部版本不再包含的冗余文件和目录
     *
     * @return true 表示冗余文件删除成功；false 表示删除过程中出错
     */
    private boolean deleteRedundantExternalFile ()
    {
        try
        {
            // 删除冗余文件
            LogUtils.debug(UpdateChecker.class, "deleteRedundantExternalFile 删除冗余文件");

            // 读取外部的结构文件
            JsonEntity externalDirectoryStructure = new JsonEntity(baseExternalHandle.child(FileName.INTERNAL_DIRECTORY_STRUCTURE_CONFIG));

            // 遍历
            for (String path : externalDirectoryStructure.keySet())
            {
                // 内部就没有这个目录
                if (!FileUtils.INTERNAL_DIRECTORY_STRUCTURE.containsKey(path))
                {
                    // 判断目录存在
                    if (!FileUtils.isDirectoryExist(baseExternalHandle.child(path)))
                    {
                        continue;
                    }
                    // 删除文件
                    if (!FileUtils.deleteDirectory(baseExternalHandle.child(path)))
                    {
                        LogUtils.error(UpdateChecker.class, "deleteRedundantExternalFile 在某次删除外部资源文件出错");
                        return false;
                    }
                }

                // 遍历目录下的文件
                for (String file : externalDirectoryStructure.getJsonEntityByKey(path).getStringList(ConfigKey.Directory.FILE))
                {
                    // 如果内部目录下没有这个文件
                    if (!FileUtils.INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey(path).getStringList(ConfigKey.Directory.FILE).contains(file))
                    {
                        // 判断文件存在
                        if (!FileUtils.isFileExist(baseExternalHandle.child(path).child(file)))
                        {
                            continue;
                        }
                        // 删除文件
                        if (!FileUtils.deleteFile(baseExternalHandle.child(path).child(file)))
                        {
                            LogUtils.error(UpdateChecker.class, "deleteRedundantExternalFile 在某次删除外部资源文件出错");
                            return false;
                        }
                    }
                }

                // 遍历目录下的目录
                for (String directory : externalDirectoryStructure.getJsonEntityByKey(path).getStringList(ConfigKey.Directory.DIRECTORY))
                {
                    // 如果内部目录下没有这个目录
                    if (!FileUtils.INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey(path).getStringList(ConfigKey.Directory.DIRECTORY).contains(directory))
                    {
                        // 删除目录
                        if (!FileUtils.deleteDirectory(baseExternalHandle.child(path).child(directory)))
                        {
                            LogUtils.error(UpdateChecker.class, "deleteRedundantExternalFile 在某次删除外部资源文件出错");
                            return false;
                        }
                    }
                }

            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "deleteRedundantExternalFile", e);
            return false;
        }
    }

    /**
     * 将内部资源文件复制到外部存储中
     *
     * @return true 表示所有文件复制成功；false 表示复制过程中出错
     */
    private boolean copyInternalFile ()
    {
        try
        {
            // 复制内部资源文件
            LogUtils.debug(UpdateChecker.class, "copyInternalFile 拷贝内部资源文件");

            // 文件复制
            List<String> rootFilePathList = FileUtils.INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey("").getStringList(ConfigKey.Directory.FILE);
            LogUtils.debug(UpdateChecker.class, "copyInternalFile 根目录文件: " + rootFilePathList);
            for (String internalFilePath : rootFilePathList)
            {
                FileHandle sourceHandle = Gdx.files.internal(internalFilePath);
                FileHandle destHandle = baseExternalHandle.child(internalFilePath);

                if (!FileUtils.copyFile(sourceHandle, destHandle))
                {
                    LogUtils.error(UpdateChecker.class, "copyInternalFile 在某次复制内部资源文件出错");
                    return false;
                }
            }

            // 目录复制
            List<String> rootDirectoryPathList = FileUtils.INTERNAL_DIRECTORY_STRUCTURE.getJsonEntityByKey("").getStringList(ConfigKey.Directory.DIRECTORY);
            LogUtils.debug(UpdateChecker.class, "copyInternalFile 根目录目录: " + rootDirectoryPathList);
            for (String internalDirectoryPath : rootDirectoryPathList)
            {
                FileHandle sourceHandle = Gdx.files.internal(internalDirectoryPath);
                FileHandle destHandle = baseExternalHandle.child(internalDirectoryPath);

                //  遍历复制
                if (!FileUtils.copyDirectory(sourceHandle, destHandle))
                {
                    LogUtils.error(UpdateChecker.class, "copyInternalFile 在某次复制内部资源文件出错");
                    return false;
                }
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "copyInternalFile", e);
            return false;
        }
    }

    /**
     * 删除外部存储中的禁止文件/目录列表
     *
     * @param prohibitFileList 需要删除的文件或目录路径列表
     * @return true 表示所有禁止文件删除成功；false 表示删除过程中出错
     */
    private boolean deleteProhibitExternalFile (List<String> prohibitFileList)
    {
        try
        {
            // 删除禁止文件
            LogUtils.debug(UpdateChecker.class, "deleteProhibitExternalFile 删除禁止文件");
            for (String path : prohibitFileList)
            {
                // 初始化
                FileHandle externalPathHandle = baseExternalHandle.child(path);

                // 判断文件还是目录
                if (FileUtils.isFileExist(externalPathHandle))
                {
                    // 删除文件
                    if (!FileUtils.deleteFile(externalPathHandle))
                    {
                        LogUtils.error(UpdateChecker.class, "deleteProhibitExternalFile 在某次删除外部资源文件出错");
                        return false;
                    }
                }
                else
                {
                    // 删除目录
                    if (!FileUtils.deleteDirectory(externalPathHandle))
                    {
                        LogUtils.error(UpdateChecker.class, "deleteProhibitExternalFile 在某次删除外部资源文件出错");
                        return false;
                    }
                }
            }

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "deleteProhibitExternalFile", e);
            return false;
        }
    }

    /**
     * 从备份目录还原被保护的文件，并清理临时文件夹
     *
     * @param protectFileList 需要还原的保护文件路径列表
     * @return true 表示所有文件还原成功；false 表示还原过程中出错
     */
    private boolean restoreProtectExternalFile (List<String> protectFileList)
    {
        try
        {
            // 还原保护文件
            LogUtils.debug(UpdateChecker.class, "restoreProtectExternalFile 还原保护文件");

            // 遍历保护文件
            for (String internalFilePath : protectFileList)
            {

                // 如果是不是json文件则不做操作
                if (!internalFilePath.endsWith(".json"))
                {
                    LogUtils.debug(UpdateChecker.class, "restoreProtectExternalFile 还原保护的文件 不是json文件不做处理");
                }
                else
                {
                    // 初始化
                    FileHandle backupHandle = baseExternalHandle.child(PathName.TEMP).child(internalFilePath);
                    FileHandle externalPathHandle = baseExternalHandle.child(internalFilePath);

                    // 先删除目标文件，避免 moveFile 在 Windows 上因目标已存在而失败
                    FileUtils.deleteFile(externalPathHandle);

                    // 还原文件
                    if (!FileUtils.moveFile(backupHandle, externalPathHandle))
                    {
                        LogUtils.error(UpdateChecker.class,
                            "restoreProtectExternalFile 还原保护文件失败 (file): " + backupHandle);
                        return false;
                    }

                    LogUtils.debug(UpdateChecker.class, "restoreProtectExternalFile 还原保护的文件 (file): " + backupHandle);
                }
            }

            // 清理temp文件夹
            FileUtils.clearDirectory(baseExternalHandle.child(PathName.TEMP));

            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "restoreProtectExternalFile", e);
            return false;
        }
    }

    /**
     * 执行完整的资源更新流程：检测版本差异、备份保护文件、删除冗余文件、
     * 复制内部资源、删除禁止文件、还原保护文件
     *
     * @return true 表示更新流程执行成功；false 表示执行过程中出错
     */
    public boolean init ()
    {
        // 防止重入：正在执行时直接返回
        if (initRunning)
        {
            LogUtils.error(UpdateChecker.class, "init 上一次 init() 尚未执行完毕，跳过重入请求");
            return false;
        }
        initRunning = true;
        try
        {
            // 检测版本是否需要更新
            if (!doFileVersionDifferent())
            {
                LogUtils.info(UpdateChecker.class, "init 检测资源文件版本不需要更新");
                doFileUpdateFinish = true;
                return true;
            }
            else
            {
                LogUtils.info(UpdateChecker.class, "init 检测资源文件版本需要更新");
            }

            // 读取更新文件配置
            JsonEntity updateConfigJson = loadFileUpdateConfig();
            if (updateConfigJson == null)
            {
                return false;
            }

            // 读取更新文件配置
            List<String> protectFileList = updateConfigJson.getStringList(VersionKey.Update.PROTECT);
            List<String> prohibitFileList = updateConfigJson.getStringList(VersionKey.Update.PROHIBIT);

            // 移动保护的文件
            if (!moveProtectExternalFile(protectFileList))
            {
                LogUtils.error(UpdateChecker.class, "init 移动保护文件失败");
                return false;
            }

            // 删除冗余资源
            if (!deleteRedundantExternalFile())
            {
                LogUtils.error(UpdateChecker.class, "init 删除冗余资源失败");
                return false;
            }

            // 复制内部更新资源
            if (!copyInternalFile())
            {
                LogUtils.error(UpdateChecker.class, "init 复制内部资源失败");
                return false;
            }

            // 删除禁止文件
            if (!deleteProhibitExternalFile(prohibitFileList))
            {
                LogUtils.error(UpdateChecker.class, "init 删除禁止文件失败");
                return false;
            }

            // 还原被保护文件
            if (!restoreProtectExternalFile(protectFileList))
            {
                LogUtils.error(UpdateChecker.class, "init 还原保护文件失败");
                return false;
            }

            // debug
            LogUtils.debug(UpdateChecker.class, "init 差异更新检测正常完成");
            doFileUpdateFinish = true;
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "init", e);
            return false;
        }
        finally
        {
            initRunning = false;
        }
    }

    /**
     * 重置更新状态，允许重新执行 {@link #init()}
     */
    public void resetUpdateState ()
    {
        doFileUpdateFinish = false;
    }

    /**
     * 强制更新（旧版），删除 app_version.json 后直接崩溃退出。
     * 仅在需要立即终止进程的场景使用。
     */
    public void repairGame ()
    {
        try
        {
            FileHandle appVersionHandle = baseExternalHandle.child(PathName.ASSET).child(FileName.APP_VERSION);
            FileUtils.deleteFile(appVersionHandle);
            LogUtils.info(UpdateChecker.class, "repairGame 删除外部app_version.json达到强制修复");
            CrashUtils.crash(new RuntimeException("repairGame 触发强制更新，退出游戏"));
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "repairGame", e);
        }
    }

    /**
     * 优雅修复（默认保守模式）：删除 app_version.json → 重新同步全部资源 → 回调通知完成。
     * 不崩溃退出，由调用方决定后续操作（如弹窗提示重启）。
     *
     * @param onComplete 修复完成后的回调（GL 线程执行），可为 null
     */
    public void repairGame (Runnable onComplete)
    {
        repairGame(RepairMode.CONSERVATIVE, onComplete);
    }

    /**
     * 按指定模式修复资源
     *
     * @param mode       修复模式（CONSERVATIVE 保留玩家配置，FULL 删除全部）
     * @param onComplete 修复完成后的回调（GL 线程执行），可为 null
     */
    public void repairGame (RepairMode mode, Runnable onComplete)
    {
        try
        {
            if (mode == RepairMode.FULL)
            {
                LogUtils.info(UpdateChecker.class, "repairGame 彻底修复模式：删除全部外部资源文件");
                FileUtils.deleteDirectory(baseExternalHandle);
                baseExternalHandle.mkdirs();
            }
            else
            {
                FileHandle appVersionHandle = baseExternalHandle.child(PathName.ASSET).child(FileName.APP_VERSION);
                FileUtils.deleteFile(appVersionHandle);
                LogUtils.info(UpdateChecker.class, "repairGame 保守修复模式：仅删除 app_version.json");
            }

            resetUpdateState();

            new Thread(() ->
            {
                boolean success = init();
                Gdx.app.postRunnable(() ->
                {
                    if (success)
                    {
                        LogUtils.info(UpdateChecker.class, "repairGame 资源修复成功");
                    }
                    else
                    {
                        LogUtils.error(UpdateChecker.class, "repairGame 资源修复失败");
                    }
                    if (onComplete != null)
                    {
                        onComplete.run();
                    }
                });
            }).start();
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "repairGame", e);
            if (onComplete != null)
            {
                Gdx.app.postRunnable(onComplete);
            }
        }
    }

    /**
     * 获取内部版本号字符串
     *
     * @return 内部版本号，格式为 "major.minor.patch"
     */
    public String getInternalVersionString ()
    {
        return internalVersionString;
    }

    /**
     * 获取格式化的显示版本字符串
     *
     * @return 显示版本字符串，格式如 "v1.0.0-beta"
     */
    public String getDisplayVersionString ()
    {
        return displayVersionString;
    }

    // 检测网页请求情况

    /**
     * 设置网页版本检测是否完成
     *
     * @param doDetectUpdateFinish 检测完成状态
     */
    public void setDoDetectUpdateFinish (boolean doDetectUpdateFinish)
    {
        this.doDetectUpdateFinish = doDetectUpdateFinish;
    }

    /**
     * 查询网页版本检测是否已完成
     *
     * @return true 表示检测已完成
     */
    public boolean doDetectUpdateFinish ()
    {
        return doDetectUpdateFinish;
    }

    // 网页请求成功情况

    /**
     * 设置网页版本检测是否成功
     *
     * @param doDetectSuccess 检测成功状态
     */
    public void setDoDetectSuccess (boolean doDetectSuccess)
    {
        this.doDetectSuccess = doDetectSuccess;
    }

    /**
     * 查询网页版本检测是否成功
     *
     * @return true 表示检测成功
     */
    public boolean doDetectSuccess ()
    {
        return doDetectSuccess;
    }

    /**
     * 查询是否需要版本更新
     *
     * @return true 表示存在新版本需要更新
     */
    public boolean isNeedVersionUpdate ()
    {
        return needVersionUpdate;
    }

    // 网站版本更新情况

    /**
     * 设置是否需要版本更新
     *
     * @param needVersionUpdate 版本更新需求状态
     */
    public void setNeedVersionUpdate (boolean needVersionUpdate)
    {
        this.needVersionUpdate = needVersionUpdate;
    }

    /**
     * 查询文件更新是否已完成
     *
     * @return true 表示文件更新流程已完成
     */
    public boolean doFileUpdateFinish ()
    {
        return doFileUpdateFinish;
    }

    // ===================================================================================================================
    // 网络版本检测
    // ===================================================================================================================

    /**
     * 启动异步版本检测（支持自动重试）
     * 结果通过 doDetectFinish / doDetectSuccess / isNeedVersionUpdate 查询
     */
    public void checkWebVersion ()
    {
        try
        {
            requestWebVersion(0);
        }
        catch (Exception e)
        {
            LogUtils.error(UpdateChecker.class, "checkWebVersion 启动失败", e);
            this.doDetectSuccess = false;
            this.doDetectUpdateFinish = true;
        }
    }

    /**
     * 发起网络版本请求（支持自动重试机制）
     *
     * @param retry 当前重试次数（0 表示首次请求）
     */
    private void requestWebVersion (final int retry)
    {
        final int MAX_RETRY = 3;
        final long RETRY_DELAY_MS = 5000;

        // 构建请求
        final String versionUrl = WebSite.OFFICIAL + "data/versions.json";
        final Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);

        // 设置请求参数
        request.setUrl(versionUrl);
        request.setTimeOut(Numeric.Time.HTTP_TIMEOUT_MS);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
        {
            /**
             * 完成版本检测并更新结果状态
             *
             * @param isSuccess 检测是否成功
             */
            private void finish (boolean isSuccess)
            {
                doDetectSuccess = isSuccess;
                doDetectUpdateFinish = true;
            }

            @Override
            public void handleHttpResponse (Net.HttpResponse httpResponse)
            {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == Numeric.Http.STATUS_OK)
                {
                    try
                    {
                        // 获取响应体
                        String responseBody = httpResponse.getResultAsString();
                        JsonReader reader = new JsonReader();
                        JsonValue json = reader.parse(responseBody);

                        // 获取版本信息
                        int webVersionInt = json.getInt(VersionKey.NEWEST_VERSION, -1);
                        int webVersionType = json.getInt(VersionKey.NEWEST_VERSION_TYPE, -1);
                        String webVersionStr = json.getString(VersionKey.NEWEST_VERSION_STRING, null);

                        // 主判断: 远程有整型字段 → 整型比较
                        if (webVersionInt != -1)
                        {
                            if (webVersionInt > internalAppVersion)
                            {
                                LogUtils.info(UpdateChecker.class,
                                    "checkWebVersion 需要更新 整型版本升级: "
                                    + internalAppVersion + " → " + webVersionInt);
                                needVersionUpdate = true;
                            }
                            else
                            {
                                LogUtils.info(UpdateChecker.class,
                                    "checkWebVersion 当前已是最新版本: "
                                    + internalVersionString + " (code=" + internalAppVersion + ")");
                                needVersionUpdate = false;
                            }
                        }
                        // 备选: 远程无整型字段 → 回退字符串比较
                        else if (webVersionStr != null)
                        {
                            int cmp = compare(webVersionStr, internalVersionString);
                            if (cmp > 0)
                            {
                                LogUtils.info(UpdateChecker.class,
                                    "checkWebVersion 需要更新 字串版本升级: "
                                    + internalVersionString + " → " + webVersionStr);
                                needVersionUpdate = true;
                            }
                            else if (cmp == 0
                                     && webVersionType > internalAppVersionType)
                            {
                                LogUtils.info(UpdateChecker.class,
                                    "checkWebVersion 需要更新 字串一致但类型升级: "
                                    + VersionType.getVersionTypeName(internalAppVersionType)
                                    + " → " + VersionType.getVersionTypeName(webVersionType));
                                needVersionUpdate = true;
                            }
                            else
                            {
                                LogUtils.info(UpdateChecker.class,
                                    "checkWebVersion 当前已是最新版本: " + internalVersionString);
                                needVersionUpdate = false;
                            }
                        }
                        else
                        {
                            LogUtils.info(UpdateChecker.class,
                                "checkWebVersion 当前已是最新版本: " + internalVersionString);
                            needVersionUpdate = false;
                        }
                        finish(true);
                    }
                    catch (Exception e)
                    {
                        LogUtils.error(UpdateChecker.class, "checkWebVersion 解析版本信息失败", e);
                        handleFailure();
                    }
                }
                else
                {
                    LogUtils.error(UpdateChecker.class, "checkWebVersion HTTP请求失败，状态码：" + statusCode);
                    handleFailure();
                }
            }

            @Override
            public void failed (Throwable t)
            {
                LogUtils.error(UpdateChecker.class, "checkWebVersion 网络请求失败", new Exception(t.toString()));
                handleFailure();
            }

            @Override
            public void cancelled ()
            {
                LogUtils.error(UpdateChecker.class, "checkWebVersion 网络请求被取消");
                handleFailure();
            }

            /**
             * 处理请求失败：未达最大重试次数则调度重试，否则标记检测完成
             */
            private void handleFailure ()
            {
                int nextRetry = retry + 1;
                if (nextRetry < MAX_RETRY)
                {
                    LogUtils.debug(UpdateChecker.class, "checkWebVersion 第 " + (retry + 1)
                        + " 次尝试失败，将在 " + (RETRY_DELAY_MS / 1000)
                        + " 秒后进行第 " + (nextRetry + 1) + " 次重试");
                    scheduleRetry(nextRetry);
                }
                else
                {
                    LogUtils.error(UpdateChecker.class, "checkWebVersion 经过 " + MAX_RETRY + " 次尝试，更新检测最终失败");
                    finish(false);
                }
            }

            /**
             * 通过 postRunnable 循环检查延时，到达重试时间后递归发起新请求
             */
            private void scheduleRetry (final int nextRetry)
            {
                final long startTime = System.currentTimeMillis();
                Gdx.app.postRunnable(new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        if (System.currentTimeMillis() - startTime >= RETRY_DELAY_MS)
                        {
                            requestWebVersion(nextRetry);
                        }
                        else
                        {
                            Gdx.app.postRunnable(this);
                        }
                    }
                });
            }
        });
    }

    // ===================================================================================================================

    /**
     * 释放更新控制器资源
     *
     * @return 始终返回 true
     */
    public boolean dispose ()
    {
        LogUtils.debug(UpdateChecker.class, "dispose 释放更新控制器资源成功");
        return true;
    }
}
