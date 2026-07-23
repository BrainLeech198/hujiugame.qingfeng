# 文档索引

> 氢风启动器项目所有文档的统一入口。按读者角色分四层。

---

## 第一层：所有读者

| 文档 | 位置 | 说明 |
|------|------|------|
| **项目介绍** | [README.md](README.md) | 功能特性、平台生态、路线图、构建指南、三语言（简中/繁中/英文） |
| **官方网站** | [docs/README.md](docs/README.md) | GitHub Pages 安装包分发站说明（.exe / .apk 下载） |

---

## 第二层：贡献者 / 开发者

| 文档 | 位置 | 说明 |
|------|------|------|
| **贡献指南** | [CONTRIBUTING.md](CONTRIBUTING.md) | 技术栈、架构概览、开发环境、打包发布、提交流程 |
| **AI 助手指令** | [CLAUDE.md](CLAUDE.md) | Claude Code 行为约束：代码规范摘要、主循环链路、注意事项 |

### develop/ 子目录

| 文档 | 位置 | 说明 |
|------|------|------|
| **架构审查与翻新路线图** | [develop/REVIEW.md](develop/REVIEW.md) | **核心参考文档**。架构评分 7/10、Python→Java 翻新进度（P0~P4）、状态码对照、资源路径映射、作品包规范、社区架构设计、生态下一步行动 |
| **变更日志** | [develop/CHANGELOG.md](develop/CHANGELOG.md) | 每次开发的变更记录（时间倒序），提交前必须更新 |
| **代码规范** | [develop/CODING_STYLE.md](develop/CODING_STYLE.md) | Allman 风格大括号、4 空格缩进、命名约定、Javadoc 要求、日志格式 |
| **提交规范** | [develop/COMMIT_STYLE.md](develop/COMMIT_STYLE.md) | 提交信息格式（中文 type/scope/subject）、粒度规则、完整示例 |
| **JSON 配置标准总览** | [develop/JSON_STANDARD.md](develop/JSON_STANDARD.md) | **所有 JSON 格式的权威参考手册**（主题/UI 种类/Layout/脚本指令/值系统/故事树等 10 类 28+ 格式），含字段类型、默认值、解析类、新增标准流程 |
| **脚本引擎内部规范** | [develop/SCRIPT_INTERNAL_STANDARD.md](develop/SCRIPT_INTERNAL_STANDARD.md) | 两部分：Block JSON 指令语言规范（指令集、值系统、运算符、执行模型）+ 包内编码约定（三层架构、Action-Param、Parser、枚举） |
| **版权声明设计文档** | [develop/specs/2026-07-13-thirdparty-licenses-design.md](develop/specs/2026-07-13-thirdparty-licenses-design.md) | 第三方素材版权声明方案选型、文件规格、条目模板 |
| **版权声明实施计划** | [develop/plans/2026-07-13-thirdparty-licenses-plan.md](develop/plans/2026-07-13-thirdparty-licenses-plan.md) | 分步实施计划、素材确认清单 |
| **libGDX 集合类迁移** | [develop/plans/libgdx-collections-migration.md](develop/plans/libgdx-collections-migration.md) | IntMap/Array/ObjectMap/Pool 替换方案 |
| **InstanceContent 拆分** | [develop/plans/refactor-instancecontent.md](develop/plans/refactor-instancecontent.md) | 初始化链/上帝对象重构方案 |
| **UiManager 拆分** | [develop/plans/refactor-uimanager.md](develop/plans/refactor-uimanager.md) | 4200+ 行外观模式拆分方案 |
| **测试体系建立** | [develop/plans/testing-setup.md](develop/plans/testing-setup.md) | JUnit 5 + headless 后端测试方案 |
| **第三方素材版权声明** | [assets/THIRDPARTY_LICENSES.md](assets/THIRDPARTY_LICENSES.md) | 第三方素材著作权声明及署名要求（随发行包分发） |

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

---

## 第四层：个人代码回顾 / 成长分析

| 文档 | 位置 | 说明 |
|------|------|------|
| **四代项目完整分析合集** | [MERGED_ANALYSIS_20260618.md](develop/history/grow/MERGED_ANALYSIS_20260618.md) | 含 SGL 代码评价、14 维度成长分析（GA2026061801）、注释考古报告（GA2026061802）的全量合并版，已基于源码交叉验证 |
| **分析规则** | [GROW_UP_ANALYSE_RULE.md](develop/history/grow/GROW_UP_ANALYSE_RULE.md) | 成长分析的方法论和观察维度框架 |

## 文档更新原则

1. **每次提交前**：更新 `develop/CHANGELOG.md`
2. **实现新功能/修复 bug**：更新 `develop/REVIEW.md` 翻新路线图状态
3. **引入新约定**：更新 `develop/CODING_STYLE.md` 或 `develop/COMMIT_STYLE.md`
4. **修改构建流程**：同步更新 `CONTRIBUTING.md` 和相关脚本内嵌注释
5. **重大架构调整**：更新 `develop/REVIEW.md` 架构审查章节
