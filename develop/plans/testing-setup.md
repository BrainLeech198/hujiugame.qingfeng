# 测试体系建立计划

## 现状

全项目零测试文件。核心逻辑（事件系统、数据容器、脚本执行）完全没有测试覆盖。

## 目标

用 libGDX 的 headless 后端 + JUnit 5，在不启动游戏窗口的情况下测试核心业务逻辑。

## 技术选型

- **JUnit 5** — 标准测试框架
- **libGDX headless** — `com.badlogic.gdx-backend-headless`，无窗口运行
- **Mocking** — Mockito（可选，用于模拟 AudioManager 等不可在 headless 下使用的依赖）

## 建议优先级

### P0：基础设施

1. 在 `core/src/test/` 下建立测试目录
2. 配置 `core/build.gradle` 添加 JUnit 5 + headless 依赖
3. 创建基础测试基类 `GdxTestBase`（初始化 Gdx 全局状态）

### P1：核心逻辑测试（优先级由高到低）

| 模块 | 测试内容 | 测试方式 |
|------|---------|---------|
| `EventQueue` | 入队/出队/优先级 | headless |
| `EventDispatcher` | 状态切换事件分发 | headless |
| `GameRenderRegistry` | 注册/获取渲染器 | headless |
| `JsonEntity` | 读写/嵌套访问/深拷贝 | headless |
| `PageBehavior` | 脚本命令解析 | headless |
| `ScriptExecutor` | 命令执行/任务生命周期 | headless |
| `StateStructure` | 状态组合/比较 | 纯 JUnit |

### P2：UI 层测试

UI 测试需要 `Stage` 环境，复杂度较高。建议在核心逻辑测试覆盖完善后再考虑。

## 执行前提

- 确定测试框架版本和配置
- 搭建第一个通过的 headless 测试用例
- 后续每新增核心逻辑时同步补测试
