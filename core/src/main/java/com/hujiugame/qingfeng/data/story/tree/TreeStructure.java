package com.hujiugame.qingfeng.data.story.tree;

import java.util.List;

public interface TreeStructure
{
    /**
     * 获取树形结构信息
     */
    TreeStructureInfo getTreeStructureInfo ();

    /**
     * 获取页面 ID 列表
     */
    List<String> getPageIdList ();

    /**
     * 获取当前页面 ID
     */
    String getNowPageId ();

    /**
     * 设置当前页面 ID
     *
     * @return 设置成功返回 true
     */
    boolean setNowPageId (String pageId);

    /**
     * 前进到下一页
     *
     * @return 执行成功返回 true
     */
    boolean forwardPage ();

    /**
     * 后退到上一页
     * @return 执行成功返回 true
     */
    boolean backPage ();

    /**
     * 获取父级树形结构信息列表
     */
    List<TreeStructureInfo> getParentTreeStructureInfo ();

    /**
     * 获取子级树形结构信息列表
     */
    List<TreeStructureInfo> getChildTreeStructureInfo ();

}
