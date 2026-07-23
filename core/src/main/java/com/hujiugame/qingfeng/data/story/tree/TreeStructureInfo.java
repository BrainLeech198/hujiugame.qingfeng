package com.hujiugame.qingfeng.data.story.tree;

import com.hujiugame.qingfeng.data.JsonEntity;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.Objects;

public class TreeStructureInfo
{
    private String block;
    private TreeStructureType structureType;
    private String structureId;

    /**
     * 使用具体参数构造树形结构信息
     *
     * @param block         所属块
     * @param structureType 结构类型
     * @param structureId   结构 ID
     */
    public TreeStructureInfo (String block, TreeStructureType structureType, String structureId)
    {
        this.block = block;
        this.structureType = structureType;
        this.structureId = structureId;
    }

    /**
     * 从 JSON 构造树形结构信息
     *
     * @param json 包含 block、type、id 字段的 JSON 数据
     */
    public TreeStructureInfo (JsonEntity json)
    {
        loadFromJson(json);
    }

    /**
     * 从 JSON 构造树形结构信息，如果 JSON 中没有 block 则使用默认值
     *
     * @param defaultBlock 默认的 block 值
     * @param json         包含 type、id 字段的 JSON 数据
     */
    public TreeStructureInfo (String defaultBlock, JsonEntity json)
    {
        if (json.getString("block") == null) json.put("block", defaultBlock);
        loadFromJson(json);
    }

    /**
     * 从 JSON 解析树形结构信息
     *
     * @param json 包含 block、type、id 字段的 JSON 数据
     */
    private void loadFromJson (JsonEntity json)
    {
        String block = json.getString("block");
        String typeString = json.getString("type");
        String id = json.getString("id");
        if (block != null && typeString != null && id != null)
        {
            try
            {
                TreeStructureType type = TreeStructureType.valueOf(typeString.toUpperCase());
                this.block = block;
                this.structureType = type;
                this.structureId = id;
                LogUtils.debug(TreeStructureInfo.class, "TreeStructureInfo 节点信息 (json): " + json + " (block): " + block + " (type): " + type + " (id): " + id);
            }
            catch (IllegalArgumentException e)
            {
                LogUtils.error(TreeStructureInfo.class, "TreeStructureInfo 未知节点类型 (json): " + json + " (type): " + typeString);
            }
        }
        else
        {
            LogUtils.error(TreeStructureInfo.class, "TreeStructureInfo 节点信息缺失 (json): " + json + " 正确应该包括 (block) (type) (id)");
        }
    }

    /**
     * 获取所属块
     */
    public String getBlock ()
    {
        return block;
    }

    /**
     * 获取结构类型
     */
    public TreeStructureType getStructureType ()
    {
        return structureType;
    }

    /**
     * 获取结构 ID
     */
    public String getStructureId ()
    {
        return structureId;
    }

    /**
     * 判断两个树形结构信息是否相等
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TreeStructureInfo)) return false;
        TreeStructureInfo that = (TreeStructureInfo) o;
        return Objects.equals(block, that.block) &&
            structureType == that.structureType &&
            Objects.equals(structureId, that.structureId);
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode ()
    {
        return Objects.hash(block, structureType, structureId);
    }

    /**
     * 返回树形结构信息的字符串表示
     */
    @Override
    public String toString ()
    {
        return "TreeStructureInfo{" +
            "block='" + block + '\'' +
            ", structureType=" + structureType +
            ", structureId='" + structureId + '\'' +
            '}';
    }
}
