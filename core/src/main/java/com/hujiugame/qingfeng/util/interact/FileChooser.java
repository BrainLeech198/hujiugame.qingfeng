package com.hujiugame.qingfeng.util.interact;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.util.system.LogUtils;
import com.hujiugame.qingfeng.util.system.PlatformUtils;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FileChooser
{

    private FileChooser()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ---------- 内部类：存放选择结果 ----------

    // 文件选择对话框关闭后自动聚焦游戏窗口的回调（由各平台启动器注入）
    private static Runnable windowFocusRequester;

    // 本次运行中最后选择的文件目录（程序重启后消失）
    private static FileHandle lastSelectedDirectory;

    // ---------- 全局存储 ----------
    private static final Map<String, FileChooserResult> resultMap = new ConcurrentHashMap<>();
    private static final Map<String, Object> activeChoosers = new ConcurrentHashMap<>();

    // ---------- 平台注入 ----------
    private static NativeFileChooser fileChooser;

    /**
     * 注入平台原生文件选择器实现
     *
     * @param chooser 原生文件选择器实例
     */
    public static void setFileChooser (NativeFileChooser chooser)
    {
        fileChooser = chooser;
        if (Gdx.app != null)
            LogUtils.debug(FileChooser.class, "setFileChooser 已注入: " + chooser.getClass().getSimpleName());
    }

    /**
     * 注入窗口聚焦回调，在文件选择/取消后自动将游戏窗口调到前台
     *
     * @param requester 窗口聚焦执行器
     */
    public static void setWindowFocusRequester (Runnable requester)
    {
        windowFocusRequester = requester;
    }

    /**
     * 创建文件选择器监听实例
     * @param tag 监听器标识
     * @return 是否创建成功（已存在则返回 false）
     */
    public static boolean createFileChooser(String tag)
    {
        if (resultMap.containsKey(tag))
        {
            LogUtils.debug(FileChooser.class, "createFileChooser 已存在: " + tag);
            return false;
        }
        resultMap.put(tag, new FileChooserResult());
        LogUtils.debug(FileChooser.class, "createFileChooser 创建: " + tag);
        return true;
    }

    // ---------- 创建监听器 ----------

    /**
     * 弹出文件选择框，只允许选择指定后缀的一个文件。
     * @param tag        选择器标识
     * @param title      对话框标题
     * @param initialDir 初始目录
     * @param ext        允许的文件后缀（为空则不过滤）
     */
    public static void showFileChooser(String tag, String title, FileHandle initialDir, String ext)
    {
        String[] extensions = (ext != null && !ext.isEmpty()) ? new String[]{ext} : new String[0];
        showFileChooser(tag, title, initialDir, extensions);
    }

    // ==================== 显示对话框（重载） ====================

    /**
     * 弹出文件选择框，允许同时选择多个后缀的文件。
     * @param tag        选择器标识
     * @param title      对话框标题
     * @param initialDir 初始目录
     * @param extensions 允许的文件后缀列表（为空则不过滤）
     */
    public static void showFileChooser(String tag, String title, FileHandle initialDir, String... extensions)
    {
        if (fileChooser == null)
        {
            LogUtils.error(FileChooser.class, "showFileChooser 未注入 NativeFileChooser");
            return;
        }

        // 标准化后缀列表
        List<String> cleanExtensions = new ArrayList<>();
        if (extensions != null)
        {
            for (String raw : extensions)
            {
                if (raw == null || raw.isEmpty()) continue;
                String low = raw.toLowerCase().trim();
                if (!low.startsWith(".")) low = "." + low;
                cleanExtensions.add(low);
            }
        }

        // 如果之前有对话框，先移除标记
        activeChoosers.remove(tag);

        FileChooserResult result = resultMap.get(tag);
        if (result == null)
        {
            LogUtils.error(FileChooser.class, "showFileChooser 未注册: " + tag);
            return;
        }

        // 平台差异化配置
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();

        // 设置标题
        config.title = (title != null) ? title : "选择文件";

        // 设置init目录
        FileHandle defaultDir;
        if (initialDir != null)
        {
            defaultDir = initialDir;
        }
        else if (lastSelectedDirectory != null)
        {
            defaultDir = lastSelectedDirectory;
        }
        else
        {
            if (PlatformUtils.isAndroid())
            {
                defaultDir = Gdx.files.external("");    // 安卓通用默认路径
            }
            else
            {
                defaultDir = Gdx.files.absolute(System.getProperty("user.home"));
            }
        }
        config.directory = defaultDir;

        // 过滤器
        if (PlatformUtils.isAndroid()) // Android
        {
            // 安卓端必须使用 "*/*" 才能正常弹出文件选择器
            config.mimeFilter = "*/*";
            config.nameFilter = null;
            LogUtils.debug(FileChooser.class, "showFileChooser 安卓端 - 使用 MIME: */*");
        }
        else // Desktop 等
        {
            // 桌面端避免枚举所有 MIME 导致卡顿，用 nameFilter 接受所有文件
            config.mimeFilter = null;
            config.nameFilter = (dir, name) -> true;
            LogUtils.debug(FileChooser.class, "showFileChooser 桌面端 - 使用 nameFilter 接受所有文件");
        }

        LogUtils.debug(FileChooser.class, "showFileChooser 显示文件选择器 (tag): " + tag + " (extensions): " + cleanExtensions + " (dir): " + config.directory.path());

        // 标记对话框已打开
        activeChoosers.put(tag, Boolean.TRUE);

        // 创建回调校验列表
        final List<String> requiredExtensions = cleanExtensions.isEmpty() ? null : new ArrayList<>(cleanExtensions);

        postFileChooserSafely(() ->
            fileChooser.chooseFile(config, new NativeFileChooserCallback()
            {
                @Override
                public void onFileChosen (FileHandle file)
                {
                    activeChoosers.remove(tag);
                    // 后缀校验
                    if (requiredExtensions != null && !requiredExtensions.isEmpty())
                    {
                        String name = file.name().toLowerCase();
                        boolean valid = false;
                        for (String ext : requiredExtensions)
                        {
                            if (name.endsWith(ext))
                            {
                                valid = true;
                                break;
                            }
                        }
                        if (!valid)
                        {
                            LogUtils.debug(FileChooser.class, "onFileChosen 文件后缀不符合要求 重新 (name): " + file.name());
                            postFileChooserSafely(() -> showFileChooser(tag, title, initialDir, extensions));
                            return;
                        }
                    }
                    result.setChosen(file);
                    lastSelectedDirectory = file.parent();
                    LogUtils.debug(FileChooser.class, "onFileChosen 用户选择了文件 (path): " + file.path() + " (lastDir): " + lastSelectedDirectory.path());
                    requestWindowFocus();
                }

                @Override
                public void onCancellation ()
                {
                    activeChoosers.remove(tag);
                    result.setCancelled();
                    LogUtils.debug(FileChooser.class, "onCancellation 用户取消了选择 (tag): " + tag);
                    requestWindowFocus();
                }

                @Override
                public void onError (Exception exception)
                {
                    activeChoosers.remove(tag);
                    LogUtils.error(FileChooser.class, "onError (tag): " + tag, exception);
                    requestWindowFocus();
                }
            })
        );
    }

    /**
     * 检查指定标识的文件选择器是否已选择了文件
     * @param tag 选择器标识
     * @return 是否已选择文件
     */
    public static boolean isFileChosen(String tag)
    {
        FileChooserResult r = resultMap.get(tag);
        return r != null && r.isChosen();
    }

    // ---------- 状态查询 ----------

    /**
     * 检查指定标识的文件选择器是否被取消了
     * @param tag 选择器标识
     * @return 是否已取消
     */
    public static boolean isFileCancelled(String tag)
    {
        FileChooserResult r = resultMap.get(tag);
        return r != null && r.isCancelled();
    }

    /**
     * 获取选中文件（同时重置选择状态）
     * @param tag 选择器标识
     * @return 选中的文件句柄，未选中则返回 null
     */
    public static FileHandle getChosenFile(String tag)
    {
        FileChooserResult r = resultMap.get(tag);
        if (r == null) return null;
        FileHandle file = r.getFile();
        resultMap.put(tag, new FileChooserResult());
        return file;
    }

    // ---------- 窗口聚焦 ----------

    /**
     * 请求游戏窗口重新获得焦点（在原生文件选择器关闭后调用）
     */
    private static void requestWindowFocus ()
    {
        if (windowFocusRequester != null)
        {
            postFileChooserSafely(windowFocusRequester);
        }
    }

    // ---------- 获取选中文件（同时重置状态） ----------

    /**
     * 删除指定标识的文件选择器
     * @param tag 选择器标识
     */
    public static void deleteFileChooser(String tag)
    {
        activeChoosers.remove(tag);
        resultMap.remove(tag);
        LogUtils.debug(FileChooser.class, "deleteFileChooser 已删除: " + tag);
    }

    // ---------- 安全的 GL 线程调度（只日志，不崩溃） ----------

    private static void postFileChooserSafely (Runnable runnable)
    {
        Gdx.app.postRunnable(() ->
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                LogUtils.error(FileChooser.class, "postRunnable 回调异常", e);
            }
        });
    }

    // ---------- 销毁 ----------

    /**
     * 文件选择结果，记录用户是否已选择或取消。
     */
    public static class FileChooserResult
    {
        private boolean chosen = false;
        private boolean cancelled = false;
        private FileHandle file = null;

        /**
         * 设置为已取消
         */
        void setCancelled ()
        {
            this.cancelled = true;
        }

        /**
         * 检查用户是否已选择文件
         *
         * @return 是否已选择
         */
        public boolean isChosen ()
        {
            return chosen;
        }

        /**
         * 设置已选择文件
         *
         * @param file 选中的文件句柄
         */
        void setChosen (FileHandle file)
        {
            this.chosen = true;
            this.file = file;
        }

        /**
         * 检查用户是否已取消选择
         *
         * @return 是否已取消
         */
        public boolean isCancelled ()
        {
            return cancelled;
        }

        /**
         * 获取选中的文件句柄
         *
         * @return 选中的文件句柄，未选择则返回 null
         */
        public FileHandle getFile ()
        {
            return file;
        }
    }
}
