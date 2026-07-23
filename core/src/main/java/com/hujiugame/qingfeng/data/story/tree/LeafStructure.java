package com.hujiugame.qingfeng.data.story.tree;

import java.util.Collections;
import java.util.List;

public final class LeafStructure implements TreeStructure
{
    private final TreeStructureInfo treeStructureInfo;
    private final String leafPageId;
    private final List<String> pageIdList;
    private final List<TreeStructureInfo> parentTreeStructureInfo;

    /**
     * 创建叶子节点结构
     *
     * @param block                   所属块
     * @param structureId             结构 ID
     * @param leafPageId              叶子页面 ID
     * @param parentTreeStructureInfo 父级节点信息列表
     */
    public LeafStructure (String block, String structureId, String leafPageId, List<TreeStructureInfo> parentTreeStructureInfo)
    {
        this.treeStructureInfo = new TreeStructureInfo(block, TreeStructureType.LEAF, structureId);
        this.leafPageId = leafPageId;
        this.pageIdList = leafPageId == null ? null : Collections.singletonList(leafPageId);
        this.parentTreeStructureInfo = parentTreeStructureInfo;
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
     * 获取页面 ID 列表（叶子节点返回 null）
     */
    @Override
    public List<String> getPageIdList ()
    {
        return pageIdList;
    }

    /**
     * 获取叶子页面 ID
     */
    @Override
    public String getNowPageId ()
    {
        return leafPageId;
    }

    /**
     * 设置当前页面 ID（叶子节点不支持，返回 false）
     */
    @Override
    public boolean setNowPageId (String pageId)
    {
        return pageId.equals(leafPageId);
    }

    /**
     * 前进到下一页（叶子节点不支持，返回 false）
     */
    @Override
    public boolean forwardPage ()
    {
        return false;
    }

    /**
     * 后退到上一页（叶子节点不支持，返回 false）
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
     * 获取子级节点信息列表（叶子节点返回 null）
     */
    @Override
    public List<TreeStructureInfo> getChildTreeStructureInfo ()
    {
        return null;
    }

    /**
     * 返回叶子节点结构的字符串表示
     */
    @Override
    public String toString ()
    {
        return "LeafStructure{" +
                "treeStructureInfo=" + treeStructureInfo +
                ", leafPageId='" + leafPageId + '\'' +
                ", parentTreeStructureInfo=" + parentTreeStructureInfo +
                '}';
    }
}
