package com.hujiugame.qingfeng.type.key;

/**
 * 故事子系统 JSON 字段名常量
 */
public final class StoryKey
{
    private StoryKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // Tree / TreeStructureInfo 字段

    public static final class Tree
    {
        private Tree()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 所属块 */
        public static final String BLOCK = "block";
        /** 结构类型（对应 {@link Tree.Type} 的值） */
        public static final String TYPE = "type";
        /** 结构 ID */
        public static final String ID = "id";
        /** 入向连接列表 */
        public static final String IN = "in";
        /** 出向连接列表 */
        public static final String OUT = "out";

        /**
         * TreeStructureType 的 JSON 值
         */
        public static final class Type
        {
            private Type()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** 根节点 */
            public static final String ROOT = "root";
            /** 分支节点 */
            public static final String BRANCH = "branch";
            /** 普通节点 */
            public static final String NODE = "node";
            /** 叶子节点 */
            public static final String LEAF = "leaf";
        }
    }

    // ====================================================================================================
    // Role 字段

    public static final class Role
    {
        private Role()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 角色 ID */
        public static final String ID = "id";
        /** 起始根节点 */
        public static final String ROOT = "root";
    }

    // ====================================================================================================
    // 其他 JSON 字段（无独立 Java 类）

    /** 页面引用 */
    public static final String PAGE = "page";
}
