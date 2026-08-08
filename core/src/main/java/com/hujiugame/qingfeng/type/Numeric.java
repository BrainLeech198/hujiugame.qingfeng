package com.hujiugame.qingfeng.type;

/**
 * 数字型常量统一收编类。
 *
 * 嵌套结构按语义分组，存放跨类共用或业务通用的魔法数字；仅类内私有语义的数字保留在各类的
 * {@code private static final} 常量中。字符串型常量见 {@code type.key} 包下各 xxxKey 类。
 */
public final class Numeric
{
    private Numeric()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ===================================================================================================================
    // 时间/延迟

    public static final class Time
    {
        /** 资源异步销毁延迟（毫秒，AudioManager/GraphicsManager/UiManager 共用） */
        public static final int DISPOSE_DELAY_MS = 120;

        /** 更新检查 HTTP 请求超时（毫秒，UpdateChecker） */
        public static final int HTTP_TIMEOUT_MS = 8000;

        /** 主线程 join 等待（毫秒，Main） */
        public static final int THREAD_JOIN_MS = 3000;

        /** 异步线程池 awaitTermination 等待（秒，GraphicsManager/UiManager 共用） */
        public static final int ASYNC_TERMINATE_WAIT_SECONDS = 5;
    }

    // ===================================================================================================================
    // 颜色/透明度

    public static final class Alpha
    {
        /** 控件禁用态压暗因子（ButtonManager/UiManager 共用） */
        public static final float DISABLED_DARKEN = 0.5f;

        /** 消息框遮罩透明度（MessageBox） */
        public static final float MESSAGE_BOX_MASK = 0.5f;
    }

    // ===================================================================================================================
    // 布局

    public static final class Layout
    {
        /** 组件内边距（UiManager/LabelManager 共用） */
        public static final int DEFAULT_COMPONENT_PADDING = 50;
    }

    // ===================================================================================================================
    // 输入

    public static final class Input
    {
        /** 摇杆死区（ControllerInputHandler） */
        public static final float STICK_DEAD_ZONE = 0.1f;
    }

    // ===================================================================================================================
    // HTTP

    public static final class Http
    {
        /** HTTP 成功状态码（UpdateChecker） */
        public static final int STATUS_OK = 200;
    }

    // ===================================================================================================================

    /** 默认字体预缓存缩放档位（缺省/解析失败时兜底），与 default_theme/theme.json 的 fontUseSize 对齐 */
    private static final float[] FONT_NORMAL_SCALE_LIST = {0.8f, 1.2f, 1.5f};

    public static float[] getFontNormalScaleList ()
    {
        return FONT_NORMAL_SCALE_LIST.clone();
    }
}
