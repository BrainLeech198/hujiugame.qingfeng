package com.hujiugame.qingfeng.data.story.tree;

import java.util.Collections;
import java.util.List;

public final class RootStructure implements TreeStructure
{
    private final TreeStructureInfo treeStructureInfo;
    private final String rootPageId;
    private final List<String> pageIdList;
    private final List<TreeStructureInfo> childTreeStructureInfo;

    /**
     * 创建根节点结构
     *
     * @param block                  所属块
     * @param structureId            结构 ID
     * @param rootPageId             根页面 ID
     * @param childTreeStructureInfo 子节点信息列表
     */
    public RootStructure (String block, String structureId, String rootPageId, List<TreeStructureInfo> childTreeStructureInfo)
    {
        treeStructureInfo = new TreeStructureInfo(block, TreeStructureType.ROOT, structureId);
        this.rootPageId = rootPageId;
        this.pageIdList = rootPageId == null ? null : Collections.singletonList(rootPageId);
        this.childTreeStructureInfo = childTreeStructureInfo;
    }

    /**
     * 获取树形结构信息
     */
    @Override
    public TreeStructureInfo getTreeStructureInfo ()
    {
        return treeStructureInfo;
    }

    /**
     * 获取页面 ID 列表（根节点返回 null）
     */
    @Override
    public List<String> getPageIdList ()
    {
        return pageIdList;
    }

    /**
     * 获取根页面 ID
     */
    @Override
    public String getNowPageId ()
    {
        return rootPageId;
    }

    /**
     * 设置当前页面 ID（根节点不支持，返回 false）
     */
    @Override
    public boolean setNowPageId (String pageId)
    {
        return pageId.equals(rootPageId);
    }

    /**
     * 前进到下一页（根节点不支持，返回 false）
     */
    @Override
    public boolean forwardPage ()
    {
        return false;
    }

    /**
     * 后退到上一页（根节点不支持，返回 false）
     */
    @Override
    public boolean backPage ()
    {
        return false;
    }

    /**
     * 获取父级节点信息（根节点返回 null）
     */
    @Override
    public List<TreeStructureInfo> getParentTreeStructureInfo ()
    {
        return null;
    }

    /**
     * 获取子级节点信息列表
     */
    @Override
    public List<TreeStructureInfo> getChildTreeStructureInfo ()
    {
        return childTreeStructureInfo;
    }

    /**
     * 返回根节点结构的字符串表示
     */
    @Override
    public String toString ()
    {
        return "RootStructure{" +
                "treeStructureInfo=" + treeStructureInfo +
                ", rootPageId='" + rootPageId + '\'' +
                ", childTreeStructureInfo=" + childTreeStructureInfo +
                '}';
    }
}
