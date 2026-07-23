package com.hujiugame.qingfeng.data.story.tree;

import java.util.Collections;
import java.util.List;

public final class NodeStructure implements TreeStructure
{
    private final TreeStructureInfo treeStructureInfo;
    private final String nodePageId;
    private final List<String> pageIdList;
    private final List<TreeStructureInfo> parentTreeStructureInfo;
    private final List<TreeStructureInfo> childTreeStructureInfo;

    /**
     * 创建节点结构
     *
     * @param block                   所属块
     * @param structureId             结构 ID
     * @param nodePageId              节点页面 ID
     * @param parentTreeStructureInfo 父级节点信息列表
     * @param childTreeStructureInfo  子级节点信息列表
     */
    public NodeStructure (String block, String structureId, String nodePageId, List<TreeStructureInfo> parentTreeStructureInfo, List<TreeStructureInfo> childTreeStructureInfo)
    {
        this.treeStructureInfo = new TreeStructureInfo(block, TreeStructureType.NODE, structureId);
        this.nodePageId = nodePageId;
        this.pageIdList = nodePageId == null ? null : Collections.singletonList(nodePageId);
        this.parentTreeStructureInfo = parentTreeStructureInfo;
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
     * 获取页面 ID 列表（节点返回 null）
     */
    @Override
    public List<String> getPageIdList ()
    {
        return pageIdList;
    }

    /**
     * 获取节点页面 ID
     */
    @Override
    public String getNowPageId ()
    {
        return nodePageId;
    }

    /**
     * 设置当前页面 ID（节点不支持，返回 false）
     */
    @Override
    public boolean setNowPageId (String pageId)
    {
        return pageId.equals(nodePageId);
    }

    /**
     * 前进到下一页（节点不支持，返回 false）
     */
    @Override
    public boolean forwardPage ()
    {
        return false;
    }

    /**
     * 后退到上一页（节点不支持，返回 false）
     */
    @Override
    public boolean backPage ()
    {
        return false;
    }

    /**
     * 获取父级节点信息列表
     */
    @Override
    public List<TreeStructureInfo> getParentTreeStructureInfo ()
    {
        return parentTreeStructureInfo;
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
     * 返回节点结构的字符串表示
     */
    @Override
    public String toString ()
    {
        return "NodeStructure{" +
                "treeStructureInfo=" + treeStructureInfo +
                ", nodePageId='" + nodePageId + '\'' +
                ", parentTreeStructureInfo=" + parentTreeStructureInfo +
                ", childTreeStructureInfo=" + childTreeStructureInfo +
                '}';
    }
}
