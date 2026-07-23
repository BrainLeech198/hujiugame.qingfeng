package com.hujiugame.qingfeng.script.data;

/**
 * JSON 类型字符串与 Java Class 之间的双向映射工具。
 * <p>
 * 支持的映射：int / float / boolean / string
 */
public class TypeMapper
{
    /**
     * 将类型字符串解析为 Java Class（严格模式，不识别时抛异常）
     */
    public static Class<?> parseClass (String typeString)
    {
        if (typeString == null)
        {
            throw new IllegalArgumentException("typeString cannot be null");
        }
        switch (typeString)
        {
            case "int":
                return int.class;
            case "float":
                return float.class;
            case "boolean":
                return boolean.class;
            case "string":
                return String.class;
            default:
                throw new IllegalArgumentException("Unsupported type: " + typeString);
        }
    }

    /**
     * 将类型字符串解析为 Java Class（宽松模式，不识别时返回 Object.class）
     * <p>
     * 注意：返回 Object.class 时，调用者需要自行处理该回退情况。
     */
    public static Class<?> parseClassLenient (String typeString)
    {
        try
        {
            return parseClass(typeString);
        }
        catch (IllegalArgumentException e)
        {
            return Object.class;
        }
    }

    /**
     * 将 Java Class 转换为 JSON 类型字符串
     */
    public static String toTypeString (Class<?> type)
    {
        if (type == int.class)     return "int";
        if (type == float.class)   return "float";
        if (type == boolean.class) return "boolean";
        if (type == String.class)  return "string";
        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    /**
     * 判断值与声明的类型是否匹配（考虑原始类型与包装类型的自动装箱）
     */
    public static boolean matches (Class<?> declaredType, Object value)
    {
        if (declaredType == int.class)     return value instanceof Integer;
        if (declaredType == float.class)   return value instanceof Float;
        if (declaredType == boolean.class) return value instanceof Boolean;
        return declaredType.isInstance(value);
    }
}
