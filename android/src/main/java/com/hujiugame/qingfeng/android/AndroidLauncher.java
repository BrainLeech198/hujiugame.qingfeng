package com.hujiugame.qingfeng.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ContentResolver;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.Main;
import com.hujiugame.qingfeng.android.imp.AndroidExplorerOpener;
import com.hujiugame.qingfeng.util.interact.CrashDialogShower;
import com.hujiugame.qingfeng.util.interact.FileExplorer;
import com.hujiugame.qingfeng.util.interact.NativeDialogUtils;
import com.hujiugame.qingfeng.util.interact.interfaces.ConfirmCallback;
import com.hujiugame.qingfeng.util.interact.interfaces.NativeDialog;
import com.hujiugame.qingfeng.util.system.FilePathConfig;
import games.spooky.gdx.nativefilechooser.android.AndroidFileChooser;
import com.hujiugame.qingfeng.util.interact.FileChooser;

public class AndroidLauncher extends AndroidApplication
{
    private Main game;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        // crashDialog
        CrashDialogShower.setPlatformShower((title, message) ->
        {
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() ->
            {
                new android.app.AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Leave", (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .setCancelable(false)
                    .show();
            });
        });

        // 多功能原生对话框
        NativeDialogUtils.setPlatformDialog(new NativeDialog()
        {
            private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

            @Override
            public void showInfo (String title, String message, Runnable onClose)
            {
                mainHandler.post(() ->
                {
                    new android.app.AlertDialog.Builder(AndroidLauncher.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) ->
                        {
                            if (onClose != null) Gdx.app.postRunnable(onClose);
                        })
                        .show();
                });
            }

            @Override
            public void showConfirm (String title, String message, ConfirmCallback callback)
            {
                mainHandler.post(() ->
                {
                    new android.app.AlertDialog.Builder(AndroidLauncher.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) ->
                            Gdx.app.postRunnable(callback::onConfirm))
                        .setNegativeButton("取消", (dialog, which) ->
                            Gdx.app.postRunnable(callback::onCancel))
                        .show();
                });
            }

            @Override
            public void showError (String title, String message, Runnable onClose)
            {
                mainHandler.post(() ->
                {
                    new android.app.AlertDialog.Builder(AndroidLauncher.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) ->
                        {
                            if (onClose != null) Gdx.app.postRunnable(onClose);
                        })
                        .setCancelable(false)
                        .show();
                });
            }
        });

        // 创建安卓实现，传入 this (Activity)
        AndroidFileChooser chooser = new AndroidFileChooser(this);
        FileChooser.setFileChooser(chooser);
        FileExplorer.setExplorerOpener(new AndroidExplorerOpener(this));

        // fileHandle静态注入
        setFileHandleInjection();

        // 创建 Main 实例（无参构造，argsList 初始为 null）
        game = new Main();

        // 处理启动时的 Intent（如果是通过 .qfg 文件打开的）
        handleIntent(getIntent());

        // 初始化 LibGDX 应用
        initialize(game, configuration);
    }

    @Override
    protected void onNewIntent (Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        // 当应用已在后台，再次通过文件打开时调用
        handleIntent(intent);
    }

    private void handleIntent (Intent intent)
    {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action))
        {
            Uri uri = intent.getData();
            if (uri != null)
            {
                // 获取文件路径从Uri
                String filePath = getFilePathFromUri(uri);
                if (filePath != null)
                {
                    // 将文件路径设置到 Main 的 argsList 中
                    game.setArgsList(new String[]{filePath});
                }
                else
                {
                    // 如果无法获取路径，至少打印日志
                    System.err.println("无法解析文件路径: " + uri);
                }
            }
        }
    }

    private String getFilePathFromUri (Uri uri)
    {
        if (uri == null) return null;

        // 1. file:// 协议
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
        {
            return uri.getPath();
        }

        // 2. content:// 协议（Android 7.0+ 常见）
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()))
        {
            // 方法一：尝试通过 MediaStore 获取真实路径（适用于媒体文件、下载文件夹等）
            String[] projection = {MediaStore.MediaColumns.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null))
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    String path = cursor.getString(columnIndex);
                    if (path != null && new File(path).exists())
                    {
                        return path;
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("无法通过 MediaStore 获取文件路径: " + uri + ": " + Arrays.toString(e.getStackTrace()));
            }

            // 方法二：如果上述失败（Android 10+ 分区存储），则复制到应用私有目录
            try
            {
                String fileName = getFileNameFromUri(uri);
                if (fileName == null) fileName = "temp_file.qfg";
                File tempFile = new File(getExternalFilesDir(null), fileName);
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(tempFile))
                {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                }
                return tempFile.getAbsolutePath();
            }
            catch (IOException e)
            {
                System.err.println("无法复制文件: " + uri + ": " + Arrays.toString(e.getStackTrace()));
                return null;
            }
        }
        return null;
    }

    private String getFileNameFromUri (Uri uri)
    {
        String fileName = null;
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null))
        {
            if (cursor != null && cursor.moveToFirst())
            {
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
            }
        }
        catch (Exception e)
        {
            System.err.println("无法获取文件名: " + uri + ": " + Arrays.toString(e.getStackTrace()));
        }
        return fileName;
    }

    private void setFileHandleInjection ()
    {
        File extRoot = Environment.getExternalStorageDirectory();
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        FilePathConfig pathConfig = new FilePathConfig(
            new FileHandle(extRoot),
            new FileHandle(downloadsDir),
            new FileHandle(documentsDir),
            new FileHandle(picturesDir),
            new FileHandle(musicDir),
            new FileHandle(moviesDir),
            null  // 安卓没有桌面目录
        );
    }
}
