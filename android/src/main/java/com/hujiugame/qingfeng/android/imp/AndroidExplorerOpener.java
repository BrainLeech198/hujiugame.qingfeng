package com.hujiugame.qingfeng.android.imp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.util.interact.interfaces.ExplorerOpener;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.List;

public class AndroidExplorerOpener implements ExplorerOpener
{
    private final Context context;

    public AndroidExplorerOpener(Context context)
    {
        this.context = context;
    }

    @Override
    public void open(FileHandle path)
    {
        if (path == null)
        {
            LogUtils.error(AndroidExplorerOpener.class, "open 参数为空 (path): null");
            return;
        }

        java.io.File javaFile = path.file().getAbsoluteFile();
        String absolutePath = javaFile.getAbsolutePath();
        FileHandle absolutePathHandle = new FileHandle(absolutePath);

        LogUtils.debug(AndroidExplorerOpener.class, "open 请求路径 (path): " + absolutePath);
        LogUtils.debug(AndroidExplorerOpener.class, "open 是否为目录 (isDirectory): " + absolutePathHandle.isDirectory());

        if (!javaFile.exists())
        {
            LogUtils.debug(AndroidExplorerOpener.class, "open 文件不存在 (exists): false");
            return;
        }

        Uri uri;
        try
        {
            uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                javaFile
            );
            LogUtils.debug(AndroidExplorerOpener.class, "open 生成URI (uri): " + uri.toString());
        }
        catch (IllegalArgumentException e)
        {
            LogUtils.error(AndroidExplorerOpener.class,
                "open 无法生成URI (path): " + absolutePath + " 请检查 file_paths.xml");
            // 即使 URI 生成失败也显示路径
            showPathToast(absolutePath);
            return;
        }

        if (absolutePathHandle.isDirectory())
        {
            tryOpenDirectory(uri);
        }
        else
        {
            tryOpenFile(uri, absolutePathHandle);
        }

        // 无论如何都显示路径提示
        showPathToast(absolutePath);
    }

    /**
     * 尝试使用最宽松的条件打开目录
     */
    private void tryOpenDirectory(Uri uri)
    {
        // 1. 仅 data，不设 type
        if (tryOpenIntentWithDataOnly(uri)) return;
        // 2. 通配 type
        if (tryOpenIntentWithType(uri, "*/*")) return;
        // 3. 标准目录 type
        if (tryOpenIntentWithType(uri, "vnd.android.document/directory")) return;
        // 4. resource/folder
        if (tryOpenIntentWithType(uri, "resource/folder")) return;
    }

    private boolean tryOpenIntentWithDataOnly(Uri uri)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        return tryStartChooser(intent, "文件夹(无type)");
    }

    private boolean tryOpenIntentWithType(Uri uri, String mime)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, mime);
        return tryStartChooser(intent, "文件夹(type=" + mime + ")");
    }

    private boolean tryStartChooser(Intent intent, String strategy)
    {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        LogUtils.debug(AndroidExplorerOpener.class, strategy + " 可用应用数: " + (list != null ? list.size() : 0));
        if (list != null)
        {
            for (ResolveInfo info : list)
            {
                LogUtils.debug(AndroidExplorerOpener.class, "  " + info.activityInfo.packageName + "/" + info.activityInfo.name);
            }
        }

        if (list != null && !list.isEmpty())
        {
            try
            {
                context.startActivity(Intent.createChooser(intent, "选择文件管理器"));
                LogUtils.debug(AndroidExplorerOpener.class, strategy + " 已弹出选择器");
                return true;
            }
            catch (Exception e)
            {
                LogUtils.error(AndroidExplorerOpener.class, strategy + " 启动失败: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 处理文件打开
     */
    private void tryOpenFile(Uri uri, FileHandle absolutePathHandle)
    {
        String mime = getMimeType(absolutePathHandle.name());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
            | Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setDataAndType(uri, mime);
        LogUtils.debug(AndroidExplorerOpener.class, "open 文件尝试 MIME: " + mime);

        try
        {
            context.startActivity(Intent.createChooser(intent, "选择应用"));
            LogUtils.debug(AndroidExplorerOpener.class, "open 文件选择器已启动");
        }
        catch (Exception e)
        {
            LogUtils.error(AndroidExplorerOpener.class, "open 文件选择器启动失败 (exception): " + e.toString());
            // 尝试打开父文件夹
            open(absolutePathHandle.parent());
        }
    }

    private void showPathToast(String path)
    {
        if (context instanceof Activity)
        {
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "文件路径：" + path, Toast.LENGTH_LONG).show();
            });
        }
    }

    private String getMimeType(String fileName)
    {
        if (fileName == null) return "*/*";
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) ext = fileName.substring(i).toLowerCase();

        switch (ext)
        {
            case ".zip": return "application/zip";
            case ".rar": return "application/x-rar-compressed";
            case ".7z":  return "application/x-7z-compressed";
            case ".tar": return "application/x-tar";
            case ".gz":  return "application/gzip";
            case ".png":
            case ".jpg":
            case ".jpeg":
            case ".bmp":
            case ".gif": return "image/*";
            case ".txt": return "text/plain";
            case ".json": return "application/json";
            case ".xml":  return "application/xml";
            case ".pdf":  return "application/pdf";
            default:      return "*/*";
        }
    }
}
