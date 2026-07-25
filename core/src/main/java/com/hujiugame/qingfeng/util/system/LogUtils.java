package com.hujiugame.qingfeng.util.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.type.LogLevel;
import com.hujiugame.qingfeng.type.file.PathName;
import com.hujiugame.qingfeng.type.key.ConfigKey;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogUtils
{

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static volatile int logLevel = LogLevel.INFO;
    private static volatile int fileLogLevel = LogLevel.DEBUG;

    private static volatile FileHandle logFileHandle = null;
    private static volatile LocalDate fileDate = null;

    /**
     * 私有构造函数，防止实例化工具类
     */
    private LogUtils ()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 设置控制台日志等级
     *
     * @param level 日志等级（对应 LogLevel 常量）
     */
    public static void setLogLevel (int level)
    {
        logLevel = level;
        Gdx.app.setLogLevel(logLevel);
    }

    /**
     * 设置文件日志等级
     *
     * @param level 日志等级（对应 LogLevel 常量）
     */
    public static void setFileLogLevel (int level)
    {
        fileLogLevel = level;
    }

    /**
     * 初始化日志系统：读取日志配置文件并设置日志等级，配置文件不存在或无效时创建默认配置
     *
     * @param logConfigPath 日志配置文件的路径
     * @return true 表示初始化成功
     */
    public static boolean init (String logConfigPath)
    {

        boolean needRepairConfig = false;
        FileHandle configFileHandle = Gdx.files.external(logConfigPath);

        // 优化1：先判断文件是否存在，如果存在再读取内容
        if (configFileHandle != null && configFileHandle.exists())
        {
            LogUtils.info(LogUtils.class, "init 读取log配置文件位置 (file): " + logConfigPath);
            String content = configFileHandle.readString();  // 一次性读取
            if (content == null || content.trim().isEmpty())
            {
                LogUtils.error(LogUtils.class, "init 配置文件内容为空 (file): " + logConfigPath);
                needRepairConfig = true;
            }
            else
            {
                // 直接解析字符串，避免 JsonEntity 内部再次读取文件
                JsonEntity logConfigJson = new JsonEntity(content);
                if (logConfigJson.isEmpty() || !logConfigJson.containsKey(ConfigKey.Log.LOG_LEVEL) || !logConfigJson.containsKey(ConfigKey.Log.FILE_LOG_LEVEL))
                {
                    LogUtils.error(LogUtils.class, "init 配置文件内容无效 (file): " + logConfigPath);
                    needRepairConfig = true;
                }
                else
                {
                    String configLogLevelStr = logConfigJson.getString(ConfigKey.Log.LOG_LEVEL);
                    String configFileLogLevelStr = logConfigJson.getString(ConfigKey.Log.FILE_LOG_LEVEL);
                    int configLogLevel = LogLevel.parseLevel(configLogLevelStr, LogLevel.INFO);
                    int configFileLogLevel = LogLevel.parseLevel(configFileLogLevelStr, LogLevel.DEBUG);
                    setLogLevel(configLogLevel);
                    setFileLogLevel(configFileLogLevel);
                }
            }
        }
        else
        {
            needRepairConfig = true;
            LogUtils.info(LogUtils.class, "init 配置文件不存在 (file): " + logConfigPath);
        }

        if (needRepairConfig)
        {
            // 创建默认配置时，仅当文件不存在或内容无效才写入，避免重复创建
            JsonEntity logConfigJson = new JsonEntity();
            logConfigJson.put(ConfigKey.Log.LOG_LEVEL, LogLevel.Name.INFO.getValue());
            logConfigJson.put(ConfigKey.Log.FILE_LOG_LEVEL, LogLevel.Name.DEBUG.getValue());
            String defaultContent = logConfigJson.getJsonString();
            // 如果文件已存在但内容无效，则覆盖写入；如果不存在则创建
            if (configFileHandle.exists())
            {
                configFileHandle.writeString(defaultContent, false);
            }
            else
            {
                FileUtils.createStringFile(defaultContent, Gdx.files.external(logConfigPath), false);
            }
            LogUtils.info(LogUtils.class, "init 创建log配置文件位置 (file): " + logConfigPath);

            setLogLevel(LogLevel.INFO);
            setFileLogLevel(LogLevel.DEBUG);
        }

        LogUtils.info(LogUtils.class, "init 控制台日志等级 (" + ConfigKey.Log.LOG_LEVEL + "): " + logLevel + " " + LogLevel.displayString(logLevel, "UNKNOW_LEVEL"));
        LogUtils.info(LogUtils.class, "init 文件的日志等级 (" + ConfigKey.Log.FILE_LOG_LEVEL + "): " + fileLogLevel + " " + LogLevel.displayString(fileLogLevel, "UNKNOW_LEVEL"));
        return true;
    }

    /**
     * 按日期更新日志文件句柄（每天生成一个新的日志文件）
     */
    private static void updateFileByDayTime ()
    {
        boolean needUpdate = false;
        LocalDate newDate = LocalDate.now();

        // 判断文件句柄
        if (logFileHandle == null || fileDate == null)
        {
            needUpdate = true;
        }

        // 判断日期
        if (!needUpdate)
        {
            if (fileDate.isBefore(newDate))
            {
                needUpdate = true;
            }
        }

        // 生成文件句柄
        if (needUpdate)
        {
            //更新日期
            fileDate = newDate;

            // 生成文件名
            String fileName = "log-" + FILE_DATE_FORMAT.format(fileDate) + ".txt";
            String filePath = FileUtils.pathJoin(PathName.BASE, PathName.LOG, fileName);
            logFileHandle = Gdx.files.external(filePath);
            // 确保日志目录存在
            logFileHandle.parent().mkdirs();
        }
    }

    /**
     * 生成当前时间的格式化字符串
     *
     * @return 格式为 "yyyy-MM-dd HH:mm:ss.SSS" 的时间字符串
     */
    private static String setDateTimeString ()
    {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return dateTime.format(formatter);
    }

    /**
     * 将日志消息写入文件（按日志等级过滤，同时避免在写入过程中使用 LogUtils 防止死循环）
     *
     * @param level   日志等级
     * @param message 日志消息内容
     */
    private static void writeLogFile (int level, String message)
    {
        // 对比是否需要写入文件
        if (level > fileLogLevel) return;

        // 设置文件句柄
        updateFileByDayTime();

        // message
        message = message + "\n";

        // 创建文件
        if (!FileUtils.createStringFileOfLog(message, logFileHandle))
        {
            Gdx.app.error("LogUtils", "log 记录日志文件失败");
        }
    }

    /**
     * 在控制台输出日志并写入日志文件
     *
     * @param level   日志等级
     * @param tag     日志标签（通常为类名）
     * @param message 日志消息内容
     */
    private static void consolePrintLog (int level, String tag, String message)
    {
        // 获取日志时间戳
        String logDateTime = setDateTimeString();

        // 创建日志字符串
        String displayLevel = LogLevel.displayString(level, "UNKNOW_LEVEL");
        String gdxString = logDateTime + "] [" + displayLevel + "] [" + tag;
        String fileString = "[" + logDateTime + "] [" + displayLevel + "] [" + tag + "] " + message;

        // 显示信息
        if (level == LogLevel.DEBUG)
        {
            Gdx.app.debug(gdxString, message);
        }
        else if (level == LogLevel.INFO)
        {
            Gdx.app.log(gdxString, message);
        }
        else if (level == LogLevel.ERROR)
        {
            Gdx.app.error(gdxString, message);
        }
        else
        {
            Gdx.app.error(gdxString, "不正确的LogLevel (" + ConfigKey.Log.LOG_LEVEL + "): " + level);
        }

        // 准备写入文件
        writeLogFile(level, fileString);
    }

    /**
     * 输出 DEBUG 级别日志
     *
     * @param tag     日志标签（通常为类名）
     * @param message 日志消息内容
     */
    public static void debug (String tag, String message)
    {
        consolePrintLog(LogLevel.DEBUG, tag, message);
    }

    /**
     * 输出 DEBUG 级别日志
     *
     * @param clazz   日志标签来源类
     * @param message 日志消息内容
     */
    public static void debug (Class<?> clazz, String message)
    {
        consolePrintLog(LogLevel.DEBUG, clazz.getSimpleName(), message);
    }

    /**
     * 输出 INFO 级别日志
     *
     * @param tag     日志标签（通常为类名）
     * @param message 日志消息内容
     */
    public static void info (String tag, String message)
    {
        consolePrintLog(LogLevel.INFO, tag, message);
    }

    /**
     * 输出 INFO 级别日志
     *
     * @param clazz   日志标签来源类
     * @param message 日志消息内容
     */
    public static void info (Class<?> clazz, String message)
    {
        consolePrintLog(LogLevel.INFO, clazz.getSimpleName(), message);
    }

    /**
     * 输出 ERROR 级别日志
     *
     * @param tag     日志标签（通常为类名）
     * @param message 日志消息内容
     */
    public static void error (String tag, String message)
    {
        consolePrintLog(LogLevel.ERROR, tag, message);
    }

    /**
     * 输出 ERROR 级别日志
     *
     * @param clazz   日志标签来源类
     * @param message 日志消息内容
     */
    public static void error (Class<?> clazz, String message)
    {
        consolePrintLog(LogLevel.ERROR, clazz.getSimpleName(), message);
    }

    /**
     * 将异常的堆栈跟踪信息转换为字符串
     *
     * @param exception 异常对象
     * @return 堆栈跟踪字符串，异常为 null 时返回空字符串
     */
    private static String getStackTraceAsString (Exception exception)
    {
        if (exception == null)
        {
            return "";
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter printWriter = new PrintWriter(out))
        {
            exception.printStackTrace(printWriter);
            printWriter.flush();
            return out.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception occurred while getting stack trace: " + e);
        }
    }

    /**
     * 输出 DEBUG 级别日志（带异常堆栈）
     *
     * @param tag       日志标签（通常为类名）
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void debug (String tag, String message, Exception exception)
    {
        debug(tag, message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 输出 DEBUG 级别日志（带异常堆栈）
     *
     * @param clazz     日志标签来源类
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void debug (Class<?> clazz, String message, Exception exception)
    {
        debug(clazz.getSimpleName(), message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 输出 INFO 级别日志（带异常堆栈）
     *
     * @param tag       日志标签（通常为类名）
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void info (String tag, String message, Exception exception)
    {
        info(tag, message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 输出 INFO 级别日志（带异常堆栈）
     *
     * @param clazz     日志标签来源类
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void info (Class<?> clazz, String message, Exception exception)
    {
        info(clazz.getSimpleName(), message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 输出 ERROR 级别日志（带异常堆栈）
     *
     * @param tag       日志标签（通常为类名）
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void error (String tag, String message, Exception exception)
    {
        error(tag, message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 输出 ERROR 级别日志（带异常堆栈）
     *
     * @param clazz     日志标签来源类
     * @param message   日志消息内容
     * @param exception 异常对象
     */
    public static void error (Class<?> clazz, String message, Exception exception)
    {
        error(clazz.getSimpleName(), message + "\n" + getStackTraceAsString(exception));
    }

    /**
     * 获取当前正在写入的日志文件句柄
     *
     * @return 当天的日志文件句柄，未初始化时可能为 null
     */
    public static FileHandle getNowLogFileHandle ()
    {
        return logFileHandle;
    }
}
