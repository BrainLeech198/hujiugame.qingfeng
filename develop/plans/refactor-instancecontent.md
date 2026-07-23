# InstanceContent 职责拆分计划

## 现状

`InstanceContent.java` 承担了以下多重职责：

1. **单例管理** — 整个类就是单例
2. **依赖容器** — 持有所有管理器、控制器、渲染器的引用
3. **对象工厂** — `init()` 中 150+ 行按顺序创建所有管理器和控制器实例
4. **初始化编排** — 控制创建顺序、耗时日志
5. **Setter 注入点** — `setVirtualInputHandler()` 等，供外部注入

这是典型的上帝对象倾向——既是 IoC 容器，又是实例工厂，还负责启动编排。

## 核心问题

- 150+ 行的 `init()` 方法，顺序依赖隐含（必须先创建 AudioManager 才能传给 UiManager）
- 15+ 个管理器引用全部平铺在一个类中，没有分组
- 每次新增管理器都要修改这个类（开闭原则违反）

## 建议方案

### 方案 A：按领域分组为多个 Factories（推荐）

```java
// DataServicesFactory — 创建 ThemeManager, TextManager, LayoutManager 等
// AudioVisualFactory — 创建 AudioManager, GraphicsManager
// UIFactory — 创建 UiManager
// GameServicesFactory — 创建 EventQueue, SceneStack, RenderPipeline 等
```

每个 Factory 返回创建好的实例列表，InstanceContent 负责组装。

### 方案 B：保留单例但拆分 init()

```java
private void initDataServices();
private void initAudioVisual();
private void initUI();
private void initGameServices();
private void initControls();
```

### 方案 C：依赖注入容器

将 InstanceContent 改造成真正的 DI 容器（按需懒加载 + 依赖自动解析），代价较大。

## 执行前提

- 确定方案方向（推荐 A）
- 各 Factory 的定义和接口设计
- 因为不改变运行时行为，可逐步迁移
