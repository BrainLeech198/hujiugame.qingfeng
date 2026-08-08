package com.hujiugame.qingfeng.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.hujiugame.qingfeng.type.Numeric;
import com.hujiugame.qingfeng.util.system.LogUtils;
import com.hujiugame.qingfeng.util.system.PlatformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TextInputUtils
{

    private TextInputUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 桌面输入对话框布局尺寸 ====================
    /** 对话框内容四周内边距（面板/按钮区共用） */
    private static final int DIALOG_EDGE_PADDING = 15;
    /** 内容面板底部内边距 */
    private static final int DIALOG_PANEL_PADDING_BOTTOM = 10;
    /** 按钮区顶部/底部内边距 */
    private static final int BUTTON_PANEL_PADDING_TOP = 5;
    private static final int BUTTON_PANEL_PADDING_BOTTOM = 15;
    /** 输入框首选尺寸 */
    private static final int TEXT_FIELD_WIDTH = 280;
    private static final int TEXT_FIELD_HEIGHT = 28;
    /** 按钮水平/垂直间距 */
    private static final int BUTTON_GAP_H = 20;
    private static final int BUTTON_GAP_V = 5;

    // ==================== 监听器实现 ====================

    // ==================== 全局存储 ====================
    private static final Map<String, TextInputListener> textInputListenerMap = new ConcurrentHashMap<>();
    private static final Map<String, Object> activeDialogs = new ConcurrentHashMap<>();

    /**
     * 创建文本输入监听器
     *
     * @param tag 监听器标识
     * @return 是否创建成功（已存在则返回 false）
     */
    public static boolean createTextInputListener (String tag)
    {
        try
        {
            if (textInputListenerMap.containsKey(tag))
            {
                LogUtils.debug(TextInputUtils.class, "createTextInputListener 输入框监听器已存在 (tag): " + tag);
                return false;
            }
            else
            {
                textInputListenerMap.computeIfAbsent(tag, k -> new TextInputListener());
                LogUtils.debug(TextInputUtils.class, "createTextInputListener 创建输入框监听器 (tag) : " + tag);
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(TextInputUtils.class, "createTextInputListener", e);
            return false;
        }
    }

    // ==================== 公开 API ====================

    /**
     * 显示文本输入对话框
     * @param tag   监听器标识
     * @param title 对话框标题
     * @param text  初始文本内容
     * @param hint  输入提示文本
     */
    public static void showTextInput (String tag, String title, String text, String hint)
    {
        try
        {
            Object existingDialog = activeDialogs.get(tag);
            if (existingDialog != null)
            {
                if (PlatformUtils.isDesktop() && existingDialog instanceof JDialog)
                {
                    JDialog dialog = (JDialog) existingDialog;
                    if (dialog.isDisplayable())
                    {
                        SwingUtilities.invokeLater(() ->
                        {
                            dialog.toFront();
                            dialog.requestFocus();
                        });
                        LogUtils.info(TextInputUtils.class, "showTextInput 已有对话框，已提到前台 (tag): " + tag);
                    }
                    else
                    {
                        activeDialogs.remove(tag);
                    }
                }
                else
                {
                    LogUtils.debug(TextInputUtils.class, "showTextInput 已有输入对话框正在显示，忽略新请求 (tag): " + tag);
                }
                return;
            }

            TextInputListener listener = textInputListenerMap.computeIfAbsent(tag, k -> new TextInputListener());
            listener.reset();

            if (PlatformUtils.isDesktop())
            {
                showDesktopInputDialog(tag, title, text, hint, listener);
            }
            else
            {
                activeDialogs.put(tag, Boolean.TRUE);
                Gdx.input.getTextInput(new Input.TextInputListener()
                {
                    @Override
                    public void input (String input)
                    {
                        activeDialogs.remove(tag);
                        listener.input(input);
                    }

                    @Override
                    public void canceled ()
                    {
                        activeDialogs.remove(tag);
                        listener.canceled();
                    }
                }, title, text, hint);
            }

            LogUtils.info(TextInputUtils.class, "showTextInput 显示输入框 (tag): " + tag);
        }
        catch (Exception e)
        {
            LogUtils.error(TextInputUtils.class, "showTextInput", e);
        }
    }

    /**
     * 检查指定标识的输入框是否已有输入
     * @param tag 监听器标识
     * @return 是否已输入
     */
    public static boolean isTextInput (String tag)
    {
        try
        {
            TextInputListener listener = textInputListenerMap.get(tag);
            if (listener == null) return false;
            boolean isInput = listener.isInput();
            if (isInput) LogUtils.info(TextInputUtils.class, "isTextInput 用户已输入 (tag): " + tag);
            return isInput;
        }
        catch (Exception e)
        {
            LogUtils.error(TextInputUtils.class, "isTextInput", e);
            return false;
        }
    }

    /**
     * 获取指定标识的输入框内容（获取后重置）
     * @param tag 监听器标识
     * @return 用户输入的文本内容，不存在则返回空字符串
     */
    public static String getTextInput (String tag)
    {
        try
        {
            TextInputListener listener = textInputListenerMap.get(tag);
            if (listener == null)
            {
                LogUtils.debug(TextInputUtils.class, "getTextInput 输入框监听器不存在 (tag): " + tag);
                return "";
            }
            String text = listener.getInput();
            listener.reset();
            LogUtils.info(TextInputUtils.class, "getTextInput 获取输入框内容 (tag): " + tag + " (input): " + text);
            return text;
        }
        catch (Exception e)
        {
            LogUtils.error(TextInputUtils.class, "getTextInput", e);
            return "";
        }
    }

    /**
     * 删除指定标识的文本输入监听器
     * @param tag 监听器标识
     * @return 是否成功删除
     */
    public static boolean deleteTextInputListener (String tag)
    {
        try
        {
            Object dialogObj = activeDialogs.remove(tag);
            if (dialogObj != null && PlatformUtils.isDesktop() && dialogObj instanceof JDialog)
            {
                JDialog dialog = (JDialog) dialogObj;
                if (dialog.isDisplayable())
                {
                    SwingUtilities.invokeLater(dialog::dispose);
                }
            }
            textInputListenerMap.remove(tag);
            LogUtils.debug(TextInputUtils.class, "deleteTextInputListener 删除输入框监听器 (tag): " + tag);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(TextInputUtils.class, "deleteTextInputListener", e);
            return false;
        }
    }

    /**
     * 在桌面端显示原生 Swing 输入对话框
     * @param tag         监听器标识
     * @param title       对话框标题
     * @param initialText 初始文本内容
     * @param hint        输入提示文本
     * @param listener    文本输入监听器
     */
    private static void showDesktopInputDialog (String tag, String title, String initialText, String hint,
                                                TextInputListener listener)
    {
        SwingUtilities.invokeLater(() ->
        {
            // 注意：LWJGL3 窗口不是 AWT 窗口，无法作为 parent，因此传 null
            JDialog dialog = new JDialog((Frame) null, title, true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(DIALOG_EDGE_PADDING, DIALOG_EDGE_PADDING, DIALOG_PANEL_PADDING_BOTTOM, DIALOG_EDGE_PADDING));
            JTextField textField = new JTextField(initialText);
            textField.setToolTipText(hint);
            textField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
            panel.add(textField, BorderLayout.CENTER);
            dialog.add(panel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(BUTTON_PANEL_PADDING_TOP, DIALOG_EDGE_PADDING, BUTTON_PANEL_PADDING_BOTTOM, DIALOG_EDGE_PADDING));
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, BUTTON_GAP_H, BUTTON_GAP_V));
            JButton okButton = new JButton("CONFIRM");
            JButton cancelButton = new JButton("CANCEL");
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // 键盘事件
            textField.addActionListener(e ->
            {
                dialog.dispose();
                activeDialogs.remove(tag);
                postSafely(() -> listener.input(textField.getText()));
            });
            textField.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed (KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    {
                        dialog.dispose();
                        activeDialogs.remove(tag);
                        postSafely(listener::canceled);
                    }
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(false);
            activeDialogs.put(tag, dialog);

            okButton.addActionListener((ActionEvent e) ->
            {
                dialog.dispose();
                activeDialogs.remove(tag);
                postSafely(() -> listener.input(textField.getText()));
            });
            cancelButton.addActionListener((ActionEvent e) ->
            {
                dialog.dispose();
                activeDialogs.remove(tag);
                postSafely(listener::canceled);
            });

            dialog.addWindowListener(new java.awt.event.WindowAdapter()
            {
                @Override
                public void windowClosing (java.awt.event.WindowEvent e)
                {
                    activeDialogs.remove(tag);
                    postSafely(listener::canceled);
                }
            });

            dialog.setVisible(true);
        });
    }

    // ==================== 安全的 GL 线程调度（只日志，不崩溃） ====================

    private static void postSafely (Runnable runnable)
    {
        Gdx.app.postRunnable(() ->
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                LogUtils.error(TextInputUtils.class, "postRunnable 回调异常", e);
            }
        });
    }

    // ==================== 桌面端私有实现 ====================

    /**
     * 文本输入监听器实现，记录用户的输入结果。
     */
    public static class TextInputListener implements Input.TextInputListener
    {
        private boolean isInput = false;
        private String input = "";

        /**
         * 构造文本输入监听器，重置输入状态。
         */
        public TextInputListener ()
        {
            reset();
        }

        /**
         * 重置输入状态和输入内容。
         */
        public void reset ()
        {
            isInput = false;
            input = "";
        }

        /**
         * 用户输入回调
         *
         * @param text 用户输入的文本内容
         */
        @Override
        public void input (String text)
        {
            LogUtils.debug(TextInputUtils.class, "input 用户输入了 (text)：" + text);
            isInput = true;
            input = text;
        }

        /**
         * 用户取消输入回调
         */
        @Override
        public void canceled ()
        {
            LogUtils.debug(TextInputUtils.class, "canceled 用户取消了输入");
        }

        /**
         * 检查用户是否已输入
         *
         * @return 是否已输入
         */
        public boolean isInput ()
        {
            return isInput;
        }

        /**
         * 获取用户输入的文本内容
         *
         * @return 用户输入的文本
         */
        public String getInput ()
        {
            return input;
        }
    }
}
