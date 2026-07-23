# UiManager 拆分计划

## 现状

`UiManager.java` 约 4200+ 行，承担了以下职责：

1. **UI 元素管理** — addLayout/deleteLayout、按钮/图片/标签的增删改
2. **UI 交互查询** — isButtonClicked、isImageClicked 等
3. **UI 状态管理** — show/hide/enable/disable
4. **弹窗管理** — 委托给 MessageBox
5. **UI 初始化** — init/setGraphicsQuoteFont
6. **布局加载** — 从 LayoutManager 获取布局数据并创建 UI 元素

## 拆分建议

按 UI 元素类型拆分为多个管理类，UiManager 作为外观（Facade）：

```
UiManager (Facade, ~500 行)
├── ButtonManager     → 按钮相关（已存在）
├── ImageManager      → 图片相关（已存在）
├── LabelManager      → 标签相关（已存在）
├── LayoutManager     → 布局组合逻辑
└── MessageBox        → 弹窗管理（已独立）
```

现有 ButtonManager/ImageManager/LabelManager 已在 `ui/` 包下，但 UiManager 中仍有大量直接操作这些元素的方法。核心任务是将 UiManager 中这些操作**委托**给已有的 Manager，而不是自己直接处理。

## 执行前提

- 统计 UiManager 中每类方法行数，按占比确定优先级
- 避免大规模重构，建议每次修改相关功能时顺手迁移
