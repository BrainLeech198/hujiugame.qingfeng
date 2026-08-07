# 文档索引

> **文档定位**：项目所有文档的统一入口索引，按读者角色分层组织。
>
> **文档结构**：
> - 按读者角色分三层：**所有读者** → **贡献者/开发者** → **工具链/构建**
> - 第二层内部按职能分类：开发入门、变更记录、编码规范、配置标准、版权管理、重构计划
> - 每个条目包含：文档名称、路径链接、一句话说明
> - 底部设**文档更新原则**，列出提交前必检查项
>
> **更新规范**：
> 1. 【必须】更新 `develop/CHANGELOG.md` 记录本次变更
> 2. 【必须】新增/重命名/删除任何 `.md` 文件后同步更新本文档
> 3. 【必须】新增文档时按角色和职能分类归入正确的位置
> 4. 【如果】文档分类结构调整 → 同步更新底部的"文档更新原则"

---

## 第一层：所有读者

| 文档 | 位置 | 说明 |
|------|------|------|
| **项目介绍** | [README.md](README.md) | 功能特性、平台生态、路线图、构建指南、三语言（简中/繁中/英文） |
| **官方网站** | [docs/README.md](docs/README.md) | GitHub Pages 安装包分发站说明（.exe / .apk 下载） |

---

## 第二层：贡献者 / 开发者

### 开发入门

| 文档 | 位置 | 说明 |
|------|------|------|
| **贡献指南** | [CONTRIBUTING.md](CONTRIBUTING.md) | 技术栈、架构概览、开发环境、打包发布、提交流程 |
| **AI 助手指令** | [CLAUDE.md](CLAUDE.md) | Claude Code 行为约束：代码规范摘要、主循环链路、注意事项 |

### 变更记录

| 文档 | 位置 | 说明 |
|------|------|------|
| **变更日志** | [develop/CHANGELOG.md](develop/CHANGELOG.md) | 每次开发的变更记录（时间倒序），提交前必须更新 |

### 编码规范

| 文档 | 位置 | 说明 |
|------|------|------|
| **代码规范** | [develop/CODING_STYLE.md](develop/CODING_STYLE.md) | Allman 风格大括号、4 空格缩进、命名约定、Javadoc 要求、日志格式 |
| **提交规范** | [develop/COMMIT_STYLE.md](develop/COMMIT_STYLE.md) | 提交信息格式（中文 type/scope/subject）、粒度规则、完整示例 |

### 配置标准

| 文档 | 位置 | 说明 |
|------|------|------|
| **JSON 配置标准总览** | [develop/JSON_STANDARD.md](develop/JSON_STANDARD.md) | **所有 JSON 格式的权威参考手册**（主题/UI 种类/Layout/脚本指令/值系统/故事树等 10 类 28+ 格式），含字段类型、默认值、解析类、新增标准流程 |
| **脚本引擎内部规范** | [develop/SCRIPT_INTERNAL_STANDARD.md](develop/SCRIPT_INTERNAL_STANDARD.md) | 两部分：Block JSON 指令语言规范（指令集、值系统、运算符、执行模型）+ 包内编码约定（三层架构、Action-Param、Parser、枚举） |

### 版权管理

| 文档 | 位置 | 说明 |
|------|------|------|
| **第三方素材版权声明规范** | [develop/THIRDPARTY_LICENSES_STANDARD.md](develop/THIRDPARTY_LICENSES_STANDARD.md) | 新增/修改第三方素材的统一流程、条目模板、字段说明、许可类型处理 |
| **第三方素材版权声明** | [assets/THIRDPARTY_LICENSES.md](assets/THIRDPARTY_LICENSES.md) | 第三方素材著作权声明及署名要求（随发行包分发） |

### 重构计划

| 文档 | 位置 | 说明 |
|------|------|------|
| **libGDX 集合类迁移** | [develop/plans/libgdx-collections-migration.md](develop/plans/libgdx-collections-migration.md) | IntMap/Array/ObjectMap/Pool 替换方案 |
| **InstanceContent 拆分** | [develop/plans/refactor-instancecontent.md](develop/plans/refactor-instancecontent.md) | 初始化链/上帝对象重构方案 |
| **3D 场景支持预想方案** | [develop/plans/2026-07-24-3d-scene-support.md](develop/plans/2026-07-24-3d-scene-support.md) | 通过 page 目录 3d.json 实现可选 3D 场景，最小架构入侵 |
| **主题版权声明自动生成** | [develop/plans/2026-07-29-theme-copyright-generator.md](develop/plans/2026-07-29-theme-copyright-generator.md) | 玩家主题第三方版权声明的自动生成机制，声明清单 JSON + 运行时生成器 |
| **macOS 打包支持预想方案** | [develop/plans/2026-08-05-macos-packaging.md](develop/plans/2026-08-05-macos-packaging.md) | mac 打包现状盘点 + 差距清单（XstartOnFirstThread/签名/M1 接线），一条龙可行性结论 |
| **语言/主题默认配置损坏恢复** | [develop/plans/2026-08-06-language-theme-default-recovery.md](develop/plans/2026-08-06-language-theme-default-recovery.md) | "用户删除默认配置"场景盘点 + 将来实现方向（融合前置/词典校验），Internal 化已消除主路径 |
| **页面切换 BGM 自动切换** | [develop/plans/2026-08-07-bgm-page-switch.md](develop/plans/2026-08-07-bgm-page-switch.md) | 页面切换旧 BGM 未停导致双播，playLayout 播放记录同步方案 |

---

## 第三层：工具链 / 构建（内嵌文档）

| 文档位置 | 说明 |
|----------|------|
| `lwjgl3/setup/launcher.c`（头部注释） | C 原生启动器编译命令、设计原则（Win7 SP1 兼容、静态 CRT） |
| `lwjgl3/setup/package.bat` | 启动器一键编译脚本（自动检测 MinGW-w64、图标嵌入） |
| `lwjgl3/setup/inno_setup.iss` | Windows Inno Setup 安装包配置（KnownDLL 绕过、文件关联） |
| `develop/output/build_package.py` | 一键打包脚本（版本号→编译→jlink→Inno Setup→输出成品） |

### 工具链关键技术文档

| 文档 | 位置 | 说明 |
|------|------|------|
| **Windows 启动器说明** | [lwjgl3/setup/README.md](lwjgl3/setup/README.md) | 启动器设计目标、工作流程、编译方法、Win7 兼容说明 |
| **C 启动器源码** | [lwjgl3/setup/launcher.c](lwjgl3/setup/launcher.c) | 469 行，仅用 Win7 SP1 API，静态 CRT |
| **MinGW 编译脚本** | [lwjgl3/setup/package.bat](lwjgl3/setup/package.bat) | 自动检测 MinGW-w64 路径、windres 图标嵌入 |
| **Windows 安装脚本** | [lwjgl3/setup/inno_setup.iss](lwjgl3/setup/inno_setup.iss) | KnownDLL 绕过（{app}+{sys} 双目录部署） |
| **全平台打包** | [develop/output/README.md](develop/output/README.md) | 打包工具使用方法、流水线说明、版本管理体系、常见问题 |

---

## 文档更新原则

1. **每次提交前**：更新 `develop/CHANGELOG.md`
2. **引入新约定**：更新 `develop/CODING_STYLE.md` 或 `develop/COMMIT_STYLE.md`
3. **修改构建流程**：同步更新 `CONTRIBUTING.md` 和相关脚本内嵌注释
