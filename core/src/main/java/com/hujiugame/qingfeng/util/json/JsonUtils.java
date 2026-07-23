package com.hujiugame.qingfeng.util.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.HashMap;
import java.util.Map;

public final class JsonUtils
{

    /**
     * 私有构造函数，防止实例化工具类
     */
    private JsonUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 要转换的对象
     * @return JSON 字符串，转换失败时返回 null
     */
    public static String objectToJsonString (Object object)
    {
        try
        {
            return JSON.toJSONString(object);
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "objectToJson", e);
            return null;
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串（带缩进）
     *
     * @param object 要转换的对象
     * @return 格式化后的 JSON 字符串，转换失败时返回 null
     */
    public static String objectToPrettyJsonString (Object object)
    {
        try
        {
            return JSON.toJSONString(object, true);
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "objectToPrettyJson", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串解析为 Map
     *
     * @param jsonString JSON 字符串
     * @return 解析后的 Map，解析失败或为空时返回空 HashMap
     */
    public static Map<String, Object> jsonStringToMap (String jsonString)
    {
        try
        {
            // 使用 TypeReference 保持泛型信息
            Map<String, Object> map = JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>() {});
            if (map == null) return new HashMap<>();
            else return map;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "jsonToMap", e);
            return new HashMap<>();
        }
    }

    /**
     * 将 JSON 字符串解析为通用 Object（保持字段顺序）
     *
     * @param jsonString JSON 字符串
     * @return 解析后的 Object，解析失败时返回 null
     */
    public static Object jsonStringToObject (String jsonString)
    {
        try
        {
            return JSON.parseObject(jsonString, Feature.OrderedField);
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "jsonToObject", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串解析为指定类型的对象
     *
     * @param jsonString JSON 字符串
     * @param clazz      目标类型
     * @return 解析后的类型实例，解析失败时返回 null
     */
    public static <T> T jsonStringToObject (String jsonString, Class<T> clazz)
    {
        try
        {
            return JSON.parseObject(jsonString, clazz);
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "jsonToObject", e);
            return null;
        }
    }

    /**
     * 合并两个 Map（以 protectMap 为基础，合并 newMap 中的内容）
     *
     * @param protectMap 基础 Map（不会被修改）
     * @param newMap     要合并的 Map
     * @return 合并后的新 Map
     */
    public static Map<String, Object> combineMap (Map<String, Object> protectMap, Map<String, Object> newMap)
    {
        try
        {
            return combineMapRecursive(protectMap, newMap);
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "combineJson", e);
            return protectMap;
        }
    }

    /**
     * 合并两个 JSON 字符串（解析后合并再序列化）
     *
     * @param protectJson 基础 JSON 字符串
     * @param newJson     要合并的 JSON 字符串
     * @return 合并后的 JSON 字符串
     */
    public static String combineJson (String protectJson, String newJson)
    {
        try
        {
            return objectToJsonString(combineMap(jsonStringToMap(protectJson), jsonStringToMap(newJson)));
        }
        catch (Exception e)
        {
            LogUtils.error(JsonUtils.class, "combineJson", e);
            return protectJson;
        }
    }

    /**
     * 递归合并两个 Map（深拷贝 protectMap 后合并 newMap 的内容）
     *
     * @param protectMap 基础 Map
     * @param newMap     要合并的 Map
     * @return 合并后的新 Map
     */
    private static Map<String, Object> combineMapRecursive (Map<String, Object> protectMap, Map<String, Object> newMap)
    {
        Map<String, Object> result = deepCopy(protectMap);
        mergeInto(result, newMap);
        return result;
    }

    /**
     * 深拷贝 Map（递归复制嵌套的 Map）
     *
     * @param map 源 Map
     * @return 深拷贝后的新 Map
     */
    private static Map<String, Object> deepCopy (Map<String, Object> map)
    {
        if (map == null)
        {
            return null;
        }
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>)
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                copy.put(key, deepCopy(nestedMap));
            }
            else
            {
                copy.put(key, value);
            }
        }
        return copy;
    }

    /**
     * 将 newMap 的内容合并到 target Map 中（若 key 已存在且两者都是 Map 则递归合并）
     *
     * @param target 目标 Map（会被修改）
     * @param newMap 源 Map
     */
    private static void mergeInto (Map<String, Object> target, Map<String, Object> newMap)
    {
        if (newMap == null)
        {
            return;
        }
        for (Map.Entry<String, Object> entry : newMap.entrySet())
        {
            String key = entry.getKey();
            Object newValue = entry.getValue();

            if (target.containsKey(key))
            {
                Object targetValue = target.get(key);
                if (targetValue instanceof Map<?, ?> && newValue instanceof Map<?, ?>)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> targetNested = (Map<String, Object>) targetValue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> newNested = (Map<String, Object>) newValue;
                    mergeInto(targetNested, newNested);
                }
            }
            else
            {
                if (newValue instanceof Map<?, ?>)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> newNested = (Map<String, Object>) newValue;
                    target.put(key, deepCopy(newNested));
                }
                else
                {
                    target.put(key, newValue);
                }
            }
        }
    }

// 以下方法保留备用，可能在后续版本恢复使用
//    public static  <T> T jsonToObject (String jsonString, Class<T> clazz)
//    {
//        try
//        {
//            return JSON.parseObject (jsonString, clazz);
//        }
//        catch (Exception e)
//        {
//            LogUtils.error ("JsonUtils", "jsonToObject", e);
//            return null;
//        }
//    }
//
//    public static  <T> List<T> jsonToList (String jsonString, Class<T> clazz)
//    {
//        try
//        {
//            return JSON.parseArray (jsonString, clazz);
//        }
//        catch (Exception e)
//        {
//            LogUtils.error ("JsonUtils", "jsonToList", e);
//            return new ArrayList<> ();
//        }
//    }
}
