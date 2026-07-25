package com.hujiugame.qingfeng.type.key;

/**
 * 脚本引擎 JSON 字段名常量
 */
public final class ScriptKey
{
    private ScriptKey()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ====================================================================================================
    // Script 顶层字段

    /** 参数列表 */
    public static final String ARGUMENTS = "arguments";

    /** 指令列表 */
    public static final String COMMANDS = "commands";

    /** 返回值声明 */
    public static final String RETURN = "return";

    // ====================================================================================================
    // 脚本参数声明（Script.Argument / Script.ArgumentInfo）

    /** 脚本参数声明 */
    public static final class Argument
    {
        private Argument()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 参数类型 */
        public static final String CLASS = "class";
        /** 参数名 */
        public static final String NAME = "name";

        /** 实参传递信息（ArgumentInfo） */
        public static final class Info
        {
            private Info()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** 形参名 */
            public static final String ARGUMENT_NAME = "argumentName";
            /** 传递方式（CONST/VARIABLE/SCOPE_VARIABLE/GAME_VARIABLE） */
            public static final String TYPE = "type";
            /** 常量值 */
            public static final String VALUE = "value";
            /** 变量名 */
            public static final String NAME = "name";
        }
    }

    // ====================================================================================================
    // 返回值声明（Script.Return）

    public static final class Return
    {
        private Return()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 返回值类型 */
        public static final String CLASS = "class";
        /** 默认值 */
        public static final String DEFAULT_VALUE = "defaultValue";
    }

    // ====================================================================================================
    // 值对象包装（ValueObject）

    public static final class ValueObject
    {
        private ValueObject()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 表达式指令列表 */
        public static final String EXPRESSION = "expression";
    }

    // ====================================================================================================
    // 触发器容器（Trigger）

    public static final class Trigger
    {
        private Trigger()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 触发器命令 */
        public static final String TRIGGER = "trigger";

        public static final class Param
        {
            private Param()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** 标签点击触发器参数 */
            public static final class Label
            {
                private Label()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 标签标记 */
                public static final String TAG = "tag";
            }
        }
    }

    // ====================================================================================================
    // 页面行为（PageBehavior）

    public static final class PageBehavior
    {
        private PageBehavior()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 页面启动行为 */
        public static final String START = "start";
        /** 页面循环行为 */
        public static final String LOOP = "loop";
        /** 触发器列表 */
        public static final String TRIGGERS = "triggers";
        /** 行为类型（inline / reference） */
        public static final String TYPE = "type";
    }

    // ====================================================================================================
    // 指令通用信封（ScriptCommand / ValueCommand / TriggerCommand 共用）

    public static final class Command
    {
        private Command()
        {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        /** 指令大类 */
        public static final String TYPE = "type";

        /** 具体动作 */
        public static final String ACTION = "action";

        /** 参数对象 */
        public static final String PARAM = "param";

        // ====================================================================================================
        // 指令参数字段（ScriptCommand / ValueCommand 的参数内部结构）

        public static final class Param
        {
            private Param()
            {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
            }

            /** 控制流指令参数 */
            public static final class Control
            {
                private Control()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 条件表达式 */
                public static final String CONDITION = "condition";
                /** If 的条件成立分支 */
                public static final String THEN_COMMANDS = "thenCommands";
                /** If 的条件不成立分支 */
                public static final String ELSE_COMMANDS = "elseCommands";
                /** Wait 的等待时间 */
                public static final String TIME = "time";
                /** Call 的脚本名称 */
                public static final String SCRIPT = "script";
                /** Return 的返回值表达式 */
                public static final String VALUE = "value";
            }

            /** 故事指令参数 */
            public static final class Story
            {
                private Story()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 树形结构数据 */
                public static final String TREE = "tree";
                /** 页面 ID */
                public static final String PAGE = "page";
            }

            /** 变量指令参数 */
            public static final class Variable
            {
                private Variable()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 变量类型 */
                public static final String CLASS = "class";
                /** 变量名 */
                public static final String NAME = "name";
                /** 变量值表达式 */
                public static final String VALUE = "value";
            }

            /** 原子值参数 */
            public static final class Atomic
            {
                private Atomic()
                {
                    throw new UnsupportedOperationException("Utility class cannot be instantiated");
                }

                /** 常量类型 */
                public static final String CLASS = "class";
                /** 常量值 */
                public static final String VALUE = "value";
                /** 游戏变量键名 */
                public static final String KEY = "key";
            }
        }
    }
}
