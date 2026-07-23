# libGDX 集合类迁移计划

## 背景

当前项目全线使用 JDK 集合类（HashMap、ArrayList、HashMap\<Integer, V\> 等），零处使用 libGDX 自带的集合类。
在 Android 平台（GC 敏感环境）下，JDK 集合类的装箱、迭代器创建、扩容复制会产生额外 GC 压力，可能导致掉帧。

libGDX 提供了专为游戏优化的替代集合类，核心优势见下方对照。

## 核心替换对照表

| JDK 集合类 | libGDX 替代 | 关键优势 |
|------------|-------------|----------|
| `HashMap<K, V>` | `ObjectMap<K, V>` | 开放地址法、平行数组存储、复用迭代器 |
| `ArrayList<T>` | `Array<T>` | 无迭代器对象创建、ordered 可选 |
| `HashMap<Integer, V>` | `IntMap<V>` | 零装箱（int 直接存储） |
| `HashMap<Long, V>` | `LongMap<V>` | 零装箱 |
| `HashSet<T>` | `ObjectSet<T>` | 同 ObjectMap，平行数组 |
| 手动对象复用 | `Pool<T>` | 对象池，obtain/free 模式 |

## 性能原理

- **ObjectMap（开放地址法）**：冲突时线性探测下一个空位，无需 Node 对象。CPU 缓存友好（连续内存访问）。
- **IntMap/LongMap**：int/long 直接作为键，无 Integer/Long 装箱对象产生。
- **Array**：for-i 遍历不创建 Iterator 对象；ordered=false 时 remove 为 O(1)（与尾部交换）。
- **Pool**：obtain 优先从池取，free 回池复用，几乎消灭短生命周期对象的 GC。

详情原理参考 `docs/libgdx-collections-guide.md`（如有）。

## 替换策略

### 原则

- **不专门重构存量代码**。全项目数百处集合用法，全部替换成本过高且风险大。
- **新代码直接使用 libGDX 集合类**。在新增功能、新文件中按场景选用。
- **触碰到的旧代码顺手改**。当修改某个文件时，顺手将其中的集合类替换。

### 场景优先级

| 优先级 | 场景 | 建议 |
|--------|------|------|
| P1 | 每帧执行的循环/查找（渲染、更新） | 使用 ObjectMap、Array、IntMap |
| P2 | 频繁创建销毁的对象（粒子、子弹等） | 使用 Pool + Array |
| P3 | 配置加载、UI 构建等一次性路径 | 保持 JDK，无需改动 |
| P4 | Desktop-only 路径 | 保持 JDK，Desktop JVM GC 足够快 |

## 执行前提

当项目开始需要关注 Android GC 性能时（如出现 Android 掉帧反馈），优先从以下热点路径切入：

1. 渲染管线的对象管理
2. 事件队列中的对象分配 
3. UI 元素的频繁创建/销毁

## 备注

- libGDX 集合类**不实现** JDK 的 Collection/Map 接口，迁移时需要同时改方法签名，这是主要成本。
- `Array` 的 `for-i` 遍历写法与 JDK 一致，迁移成本低。
- `Pool` 可以独立引入，不影响现有集合框架。
