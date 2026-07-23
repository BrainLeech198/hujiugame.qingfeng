package com.hujiugame.qingfeng.data;

import com.badlogic.gdx.files.FileHandle;
import com.hujiugame.qingfeng.util.json.JsonUtils;
import com.hujiugame.qingfeng.util.system.FileUtils;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.*;
import java.util.Map.Entry;

public class JsonEntity
{
    // 内部数据容器
    private Map<String, Object> mapData;
    private List<Object> listData;
    private boolean isMap;   // true: Map, false: List

    // 不可变的空对象常量（用于共享只读空容器，避免重复创建）
    private static final Map<String, Object> EMPTY_OBJECT_MAP = Collections.emptyMap ();
    private static final List<Object> EMPTY_OBJECT_LIST = Collections.emptyList ();
    private static final Map<String, Integer> EMPTY_INT_MAP = Collections.emptyMap ();
    private static final Map<String, String> EMPTY_STRING_MAP = Collections.emptyMap ();

    // ================================== 构造器 ==================================

    /**
     * 创建空 JsonEntity（默认为 Map）
     */
    public JsonEntity ()
    {
        this.isMap = true;
        this.mapData = EMPTY_OBJECT_MAP;
        this.listData = null;
    }

    /**
     * 复制构造器（深拷贝）
     */
    public JsonEntity (JsonEntity jsonEntity)
    {
        if (jsonEntity != null)
        {
            this.isMap = jsonEntity.isMap;
            if (this.isMap)
            {
                this.mapData = (jsonEntity.mapData != null && !jsonEntity.mapData.isEmpty())
                    ? deepCopyMap(jsonEntity.mapData) : EMPTY_OBJECT_MAP;
                this.listData = null;
            }
            else
            {
                this.listData = (jsonEntity.listData != null && !jsonEntity.listData.isEmpty())
                    ? deepCopyList(jsonEntity.listData) : EMPTY_OBJECT_LIST;
                this.mapData = null;
            }
        }
        else
        {
            this.isMap = true;
            this.mapData = EMPTY_OBJECT_MAP;
            this.listData = null;
        }
    }

    /**
     * 从 Map 构造（深拷贝）
     */
    public JsonEntity (Map<String, Object> mapData)
    {
        try
        {
            this.isMap = true;
            this.mapData = (mapData == null || mapData.isEmpty()) ? EMPTY_OBJECT_MAP : deepCopyMap(mapData);
            this.listData = null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "JsonEntity(Map)", e);
            this.isMap = true;
            this.mapData = EMPTY_OBJECT_MAP;
            this.listData = null;
        }
    }

    /**
     * 从 List 构造（深拷贝）
     */
    public JsonEntity (List<Object> listData)
    {
        try
        {
            this.isMap = false;
            this.listData = (listData == null || listData.isEmpty()) ? EMPTY_OBJECT_LIST : deepCopyList(listData);
            this.mapData = null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "JsonEntity(List)", e);
            this.isMap = false;
            this.listData = EMPTY_OBJECT_LIST;
            this.mapData = null;
        }
    }

    /**
     * 从 JSON 字符串构造（自动识别类型）
     */
    public JsonEntity (String jsonString)
    {
        try
        {
            Object parsed = JsonUtils.jsonStringToObject(jsonString);
            if (parsed instanceof Map)
            {
                this.isMap = true;
                Map<String, Object> src = (Map<String, Object>) parsed;
                this.mapData = (src == null || src.isEmpty()) ? EMPTY_OBJECT_MAP : deepCopyMap(src);
                this.listData = null;
            }
            else if (parsed instanceof List)
            {
                this.isMap = false;
                List<Object> src = (List<Object>) parsed;
                this.listData = (src == null || src.isEmpty()) ? EMPTY_OBJECT_LIST : deepCopyList(src);
                this.mapData = null;
            }
            else
            {
                // 非对象非数组，按空对象处理
                this.isMap = true;
                this.mapData = EMPTY_OBJECT_MAP;
                this.listData = null;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "JsonEntity(String)", e);
            this.isMap = true;
            this.mapData = EMPTY_OBJECT_MAP;
            this.listData = null;
        }
    }

    /**
     * 从文件句柄构造（自动识别类型）
     */
    public JsonEntity (FileHandle file)
    {
        this(FileUtils.readStringFile(file));
    }

    // ================================== 内部工具方法 ==================================

    /**
     * 深拷贝 Map（空Map返回不可变空常量）
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopyMap (Map<String, Object> source)
    {
        if (source == null || source.isEmpty())
        {
            return EMPTY_OBJECT_MAP;
        }
        Map<String, Object> copy = new LinkedHashMap<>(source.size());
        for (Entry<String, Object> entry : source.entrySet())
        {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    /**
     * 深拷贝 List（空List返回不可变空常量）
     */
    @SuppressWarnings("unchecked")
    private static List<Object> deepCopyList (List<Object> source)
    {
        if (source == null || source.isEmpty())
        {
            return EMPTY_OBJECT_LIST;
        }
        List<Object> copy = new ArrayList<>(source.size());
        for (Object item : source)
        {
            copy.add(deepCopyValue(item));
        }
        return copy;
    }

    /**
     * 递归深拷贝值对象
     */
    @SuppressWarnings("unchecked")
    private static Object deepCopyValue (Object value)
    {
        if (value instanceof Map)
        {
            return deepCopyMap((Map<String, Object>) value);
        }
        else if (value instanceof List)
        {
            return deepCopyList((List<Object>) value);
        }
        else
        {
            // 基本类型、String、Number 等不可变对象直接返回
            return value;
        }
    }

    /**
     * 确保数据是可变的（用于修改操作）
     */
    private void ensureMutable ()
    {
        if (isMap)
        {
            if (mapData == EMPTY_OBJECT_MAP)
            {
                mapData = new HashMap<>();
            }
        }
        else
        {
            if (listData == EMPTY_OBJECT_LIST)
            {
                listData = new ArrayList<>();
            }
        }
    }

    // ================================== 类型判断与基本信息 ==================================

    /**
     * 是否为 Map 类型
     */
    public boolean isMap ()
    {
        return isMap;
    }

    /**
     * 是否为 List 类型
     */
    public boolean isList ()
    {
        return !isMap;
    }

    /**
     * 获取元素个数
     */
    public int size ()
    {
        return isMap ? mapData.size() : listData.size();
    }

    /**
     * 是否为空容器
     */
    public boolean isEmpty ()
    {
        return isMap ? mapData.isEmpty() : listData.isEmpty();
    }

    /**
     * 获取 JSON 格式的字符串表示
     */
    public String getJsonString ()
    {
        if (isMap)
        {
            return JsonUtils.objectToJsonString(mapData);
        }
        else
        {
            return JsonUtils.objectToJsonString(listData);
        }
    }

    /**
     * 返回 JSON 字符串表示
     */
    @Override
    public String toString ()
    {
        return getJsonString();
    }

    // ================================== Map 相关方法 ==================================

    /**
     * 返回内部 Map 的不可变视图（仅当 isMap 为 true 时有效，否则返回空 Map）
     */
    public Map<String, Object> getObjectMap ()
    {
        return isMap ? Collections.unmodifiableMap(mapData) : Collections.emptyMap();
    }

    /**
     * 转换为 Map<String, Integer>（仅当 isMap 为 true 时有效）
     */
    public Map<String, Integer> getIntMap ()
    {
        if (!isMap) return EMPTY_INT_MAP;
        try
        {
            Map<String, Integer> intMap = new HashMap<>();
            for (Entry<String, Object> entry : mapData.entrySet())
            {
                Object value = entry.getValue();
                if (value instanceof Number)
                {
                    intMap.put(entry.getKey(), ((Number) value).intValue());
                }
                else if (value instanceof String)
                {
                    try
                    {
                        intMap.put(entry.getKey(), Integer.parseInt((String) value));
                    }
                    catch (NumberFormatException e)
                    {
                        intMap.put(entry.getKey(), 0);
                    }
                }
                else
                {
                    intMap.put(entry.getKey(), 0);
                }
            }
            return intMap;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getIntMap", e);
            return EMPTY_INT_MAP;
        }
    }

    /**
     * 转换为 Map<String, String>（仅当 isMap 为 true 时有效）
     */
    public Map<String, String> getStringMap ()
    {
        if (!isMap) return EMPTY_STRING_MAP;
        try
        {
            Map<String, String> stringMap = new HashMap<>();
            for (Entry<String, Object> entry : mapData.entrySet())
            {
                Object value = entry.getValue();
                stringMap.put(entry.getKey(), value != null ? value.toString() : "");
            }
            return stringMap;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getStringMap", e);
            return EMPTY_STRING_MAP;
        }
    }

    /**
     * 获取所有键名列表（仅 Map 有效，List 返回空列表）
     */
    public List<String> keySet ()
    {
        if (isMap)
        {
            return new ArrayList<>(mapData.keySet());
        }
        return new ArrayList<>(); // List 无 key，返回空列表
    }

    /**
     * 判断是否包含指定键（仅 Map 有效）
     */
    public boolean containsKey (String key)
    {
        return isMap && mapData.containsKey(key);
    }

    // ================================== List 相关方法 ==================================

    /**
     * 获取整个数组的浅拷贝（仅当 isList 时有效，否则返回空列表）
     */
    public List<Object> getObjectList ()
    {
        if (!isMap)
        {
            return new ArrayList<>(listData);
        }
        return new ArrayList<>();
    }

    /**
     * 获取索引处的元素（仅当 isList 时有效，否则返回 null）
     */
    public Object get (int index)
    {
        if (!isMap && index >= 0 && index < listData.size())
        {
            return listData.get(index);
        }
        return null;
    }

    /**
     * 获取索引处的元素并包装为 JsonEntity（仅当 isList 时有效）
     */
    public JsonEntity getJsonEntityByIndex (int index)
    {
        if (!isMap && index >= 0 && index < listData.size())
        {
            Object obj = listData.get(index);
            if (obj instanceof Map)
            {
                return new JsonEntity(deepCopyMap((Map<String, Object>) obj));
            }
            else if (obj instanceof List)
            {
                return new JsonEntity(deepCopyList((List<Object>) obj));
            }
            else
            {
                // 非对象非数组，返回一个新的空 JsonEntity
                return new JsonEntity();
            }
        }
        return new JsonEntity(); // 越界或类型不符，返回空对象
    }

    /**
     * 向数组末尾添加元素（仅当 isList 时有效）
     */
    public void add (Object value)
    {
        if (!isMap)
        {
            ensureMutable();
            listData.add(deepCopyValue(value));
        }
    }

    /**
     * 批量添加元素（仅当 isList 时有效）
     */
    public void addAll (Collection<?> values)
    {
        if (!isMap && values != null)
        {
            ensureMutable();
            for (Object v : values)
            {
                listData.add(deepCopyValue(v));
            }
        }
    }

    /**
     * 删除指定位置的元素（仅当 isList 时有效）
     */
    public void remove (int index)
    {
        if (!isMap && index >= 0 && index < listData.size())
        {
            ensureMutable();
            listData.remove(index);
        }
    }

    /**
     * 清空数组（仅当 isList 时有效）
     */
    public void clearList ()
    {
        if (!isMap)
        {
            ensureMutable();
            listData.clear();
        }
    }

    // ================================== 通用访问 ==================================

    /**
     * 根据键获取子 JsonEntity（仅 Map 有效，List 返回空）
     */
    public JsonEntity getJsonEntityByKey (String key)
    {
        if (isMap)
        {
            try
            {
                Object obj = mapData.get(key);
                if (obj instanceof Map)
                {
                    return new JsonEntity(deepCopyMap((Map<String, Object>) obj));
                }
                else if (obj instanceof List)
                {
                    return new JsonEntity(deepCopyList((List<Object>) obj));
                }
                return new JsonEntity(); // 不存在或非容器类型，返回空对象
            }
            catch (Exception e)
            {
                LogUtils.error(JsonEntity.class, "get", e);
                return new JsonEntity();
            }
        }
        return new JsonEntity();
    }

    /**
     * 根据键获取原始 Object 值（仅 Map 有效）
     */
    public Object getObject (String key)
    {
        return isMap ? mapData.get(key) : null;
    }

    /**
     * 根据键获取 int 值（仅 Map 有效，不存在或类型不匹配时返回 0）
     */
    public int getInt (String key)
    {
        if (!isMap) return 0;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof Number)
            {
                return ((Number) obj).intValue();
            }
            return 0;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getInt", e);
            return 0;
        }
    }

    /**
     * 根据键获取 long 值（仅 Map 有效，不存在或类型不匹配时返回 0）
     */
    public long getLong (String key)
    {
        if (!isMap) return 0;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof Number)
            {
                return ((Number) obj).longValue();
            }
            return 0;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getLong", e);
            return 0;
        }
    }

    /**
     * 根据键获取 float 值（仅 Map 有效，不存在或类型不匹配时返回 0）
     */
    public float getFloat (String key)
    {
        if (!isMap) return 0;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof Number)
            {
                return ((Number) obj).floatValue();
            }
            return 0;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getFloat", e);
            return 0;
        }
    }

    /**
     * 根据键获取 double 值（仅 Map 有效，不存在或类型不匹配时返回 0）
     */
    public double getDouble (String key)
    {
        if (!isMap) return 0;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof Number)
            {
                return ((Number) obj).doubleValue();
            }
            return 0;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getDouble", e);
            return 0;
        }
    }

    /**
     * 根据键获取 String 值（仅 Map 有效，不存在或类型不匹配时返回 null）
     */
    public String getString (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            return obj instanceof String ? (String) obj : null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getString", e);
            return null;
        }
    }

    /**
     * 根据键获取 boolean 值（仅 Map 有效，不存在或类型不匹配时返回 false）
     */
    public boolean getBoolean (String key)
    {
        if (!isMap) return false;
        try
        {
            Object obj = mapData.get(key);
            return obj instanceof Boolean && (Boolean) obj;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getBoolean", e);
            return false;
        }
    }

    // ================================== 修改操作（Map） ==================================

    /**
     * 存入键值对（仅 Map 有效）
     */
    public boolean put (String key, Object value)
    {
        if (!isMap) return false;
        try
        {
            ensureMutable();
            mapData.put(key, deepCopyValue(value));
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "put", e);
            return false;
        }
    }

    /**
     * 移除指定键（仅 Map 有效）
     */
    public boolean remove (String key)
    {
        if (!isMap) return false;
        try
        {
            ensureMutable();
            mapData.remove(key);
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "remove", e);
            return false;
        }
    }

    /**
     * 清空所有数据
     */
    public boolean clear ()
    {
        try
        {
            if (isMap)
            {
                ensureMutable();
                mapData.clear();
            }
            else
            {
                ensureMutable();
                listData.clear();
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "clear", e);
            return false;
        }
    }

    /**
     * 合并另一个 JsonEntity（仅当两个都是 Map 时生效，当前实例会被修改）
     */
    public boolean combine (JsonEntity jsonEntity)
    {
        if (jsonEntity == null) return true;
        if (!isMap || !jsonEntity.isMap) return false; // 仅 Map 可合并
        try
        {
            ensureMutable();
            Map<String, Object> combined = JsonUtils.combineMap(mapData, jsonEntity.mapData);
            if (combined != null)
            {
                mapData = combined;
            }
            return true;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "combine", e);
            return false;
        }
    }

    /**
     * 返回合并后的新 JsonEntity（不修改当前实例，仅当两个都是 Map 时有效）
     */
    public JsonEntity combined (JsonEntity jsonEntity)
    {
        if (jsonEntity == null) return new JsonEntity(this);
        if (!isMap || !jsonEntity.isMap) return new JsonEntity(this);
        try
        {
            Map<String, Object> merged = JsonUtils.combineMap(mapData, jsonEntity.mapData);
            return new JsonEntity(merged != null ? merged : new HashMap<>());
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "combined", e);
            return new JsonEntity(); // 异常时返回空对象
        }
    }

    // ================================== 列表获取方法（Map 内嵌套数组） ==================================

    public List<JsonEntity> getJsonEntityList (String key)
    {
        if (!isMap) return Collections.emptyList();
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<JsonEntity> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Map)
                    {
                        result.add(new JsonEntity(deepCopyMap((Map<String, Object>) item)));
                    }
                    else if (item instanceof List)
                    {
                        result.add(new JsonEntity(deepCopyList((List<Object>) item)));
                    }
                    else
                    {
                        result.add(new JsonEntity()); // 无效元素用空对象占位
                    }
                }
                return result;
            }
            // 非 List 时返回包含一个空 JsonEntity 的列表（保持原行为）
            return Collections.singletonList(new JsonEntity());
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getJsonEntityList", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据键获取原始 Object 列表（仅 Map 有效）
     */
    public List<Object> getObjectList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                return new ArrayList<>((List<?>) obj);
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getObjectList", e);
            return null;
        }
    }

    /**
     * 根据键获取 Integer 列表（仅 Map 有效）
     */
    public List<Integer> getIntList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<Integer> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Number)
                    {
                        result.add(((Number) item).intValue());
                    }
                    else if (item instanceof String)
                    {
                        try
                        {
                            result.add(Integer.parseInt((String) item));
                        }
                        catch (NumberFormatException e)
                        {
                            result.add(0);
                        }
                    }
                    else
                    {
                        result.add(0);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getIntList", e);
            return null;
        }
    }

    /**
     * 根据键获取 Long 列表（仅 Map 有效）
     */
    public List<Long> getLongList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<Long> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Number)
                    {
                        result.add(((Number) item).longValue());
                    }
                    else if (item instanceof String)
                    {
                        try
                        {
                            result.add(Long.parseLong((String) item));
                        }
                        catch (NumberFormatException e)
                        {
                            result.add(0L);
                        }
                    }
                    else
                    {
                        result.add(0L);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getLongList", e);
            return null;
        }
    }

    /**
     * 根据键获取 Float 列表（仅 Map 有效）
     */
    public List<Float> getFloatList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<Float> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Number)
                    {
                        result.add(((Number) item).floatValue());
                    }
                    else if (item instanceof String)
                    {
                        try
                        {
                            result.add(Float.parseFloat((String) item));
                        }
                        catch (NumberFormatException e)
                        {
                            result.add(0f);
                        }
                    }
                    else
                    {
                        result.add(0f);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getFloatList", e);
            return null;
        }
    }

    /**
     * 根据键获取 Double 列表（仅 Map 有效）
     */
    public List<Double> getDoubleList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<Double> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Number)
                    {
                        result.add(((Number) item).doubleValue());
                    }
                    else if (item instanceof String)
                    {
                        try
                        {
                            result.add(Double.parseDouble((String) item));
                        }
                        catch (NumberFormatException e)
                        {
                            result.add(0.0);
                        }
                    }
                    else
                    {
                        result.add(0.0);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getDoubleList", e);
            return null;
        }
    }

    /**
     * 根据键获取 String 列表（仅 Map 有效）
     */
    public List<String> getStringList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<String> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    result.add(item != null ? item.toString() : null);
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getStringList", e);
            return null;
        }
    }

    /**
     * 根据键获取 Boolean 列表（仅 Map 有效）
     */
    public List<Boolean> getBooleanList (String key)
    {
        if (!isMap) return null;
        try
        {
            Object obj = mapData.get(key);
            if (obj instanceof List)
            {
                List<?> rawList = (List<?>) obj;
                List<Boolean> result = new ArrayList<>(rawList.size());
                for (Object item : rawList)
                {
                    if (item instanceof Boolean)
                    {
                        result.add((Boolean) item);
                    }
                    else if (item instanceof String)
                    {
                        result.add(Boolean.parseBoolean((String) item));
                    }
                    else
                    {
                        result.add(false);
                    }
                }
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            LogUtils.error(JsonEntity.class, "getBooleanList", e);
            return null;
        }
    }
}
