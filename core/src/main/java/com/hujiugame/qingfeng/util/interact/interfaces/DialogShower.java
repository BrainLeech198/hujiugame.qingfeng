package com.hujiugame.qingfeng.util.interact.interfaces;

/**
 * 平台原生对话框显示接口
 */
@FunctionalInterface
public interface DialogShower
{
    /**
     * 显示模态对话框
     * @param title   标题
     * @param message 内容
     */
    void show(String title, String message);
}
