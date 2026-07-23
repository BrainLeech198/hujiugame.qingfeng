package com.hujiugame.qingfeng.data.story.tree;

import java.util.List;

public final class BranchStructure implements TreeStructure
{
    private final TreeStructureInfo treeStructureInfo;
    private final List<String> pageIdList;
    private final List<TreeStructureInfo> parentTreeStructureInfo;
    private final List<TreeStructureInfo> childTreeStructureInfo;
    private int nowPageIndex;

    /**
     * 创建分支节点结构
     *
     * @param block                   所属块
     * @param structureId             结构 ID
     * @param pageIdList              页面 ID 列表
     * @param parentTreeStructureInfo 父级节点信息列表
     * @param childTreeStructureInfo  子级节点信息列表
     */
    public BranchStructure (String block, String structureId, List<String> pageIdList, List<TreeStructureInfo> parentTreeStructureInfo, List<TreeStructureInfo> childTreeStructureInfo)
    {
        treeStructureInfo = new TreeStructureInfo(block, TreeStructureType.BRANCH, structureId);
        this.pageIdList = pageIdList;
        this.parentTreeStructureInfo = parentTreeStructureInfo;
        this.childTreeStructureInfo = childTreeStructureInfo;
        nowPageIndex = 0;
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
     * 获取页面 ID 列表
     */
    @Override
    public List<String> getPageIdList ()
    {
        return pageIdList;
    }

    /**
     * 获取当前页面 ID
     */
    @Override
    public String getNowPageId ()
    {
        return pageIdList.get(nowPageIndex);
    }

    /**
     * 设置当前页面 ID
     *
     * @return 如果页面 ID 在列表中则设置成功返回 true
     */
    @Override
    public boolean setNowPageId (String pageId)
    {
        if (pageIdList.contains(pageId))
        {
            nowPageIndex = pageIdList.indexOf(pageId);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 前进到下一页（分支节点不支持，返回 false）
     */
    @Override
    public boolean forwardPage ()
    {
        return false;
    }

    /**
     * 后退到上一页（分支节点不支持，返回 false）
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
     * 返回分支节点结构的字符串表示
     */
    @Override
    public String toString ()
    {
        return "BranchStructure{" +
                "treeStructureInfo=" + treeStructureInfo +
                ", pageIdList=" + pageIdList +
                ", parentTreeStructureInfo=" + parentTreeStructureInfo +
                ", childTreeStructureInfo=" + childTreeStructureInfo +
                ", nowPageIndex=" + nowPageIndex +
                '}';
    }
}
