# 主题第三方版权声明自动生成 — 预想方案

> **状态:** 设计预想，未排期。本文档记录思路和决策依据，供将来实施时参考。

---

## 目标

为开放给玩家自制主题（启动器主题/游戏主题均可）提供 **自动化的第三方版权声明生成机制**，让玩家无需手动编辑 `.md` 文件，系统根据用户提供的素材信息自动生成法律上合规的声明文件。

### 要解决的问题

1. **用户侧**：拖拽素材进主题包的用户不熟悉版权声明格式，不应要求他们手动写 `.md`
2. **合规侧**：发行包必须附带完整的第三方授权声明，否则项目有法律风险
3. **平台侧**：Android 文件管理器权限受限，用户无法直接访问 `THIRDPARTY_LICENSES.md` 编辑
4. **增量维护**：主题更新时，新增/删除的素材声明应自动同步，不依赖用户记忆

---

## 方案概要：声明清单 JSON + 运行时生成器

主题包内带一个结构化 JSON 声明清单（用户只需填最基本信息），游戏引擎在主题加载时读取并自动生成格式化声明文件。

```
主题包/
  theme.json              # 主题配置（现有）
  copyright.json          # [新增] 素材版权声明清单（用户填写）
  asset/
    resource/
      image/...
      audio/...
    ui/font/...
  THIRDPARTY_LICENSES.md  # [自动生成] 运行时生成的完整声明文件
```

---

## 详细设计

### 一、声明清单 JSON（用户填写）

#### 文件位置

主题根目录下固定文件名 `copyright.json`，与 `theme.json` 平级。

理由：
- `theme.json` 已较大（UI 配置、布局引用等），版权信息独立更清晰
- 版权信息可能包含多个条目，长度不可控
- 分离后 `copyright.json` 可独立校验

#### 格式定义

```json
{
  "assets": [
    {
      "type": "image",
      "path": "asset/resource/image/button_bg.png",
      "name": "按钮背景纹理",
      "author": "张三",
      "source": "https://example.com",
      "sourceName": "Example站",
      "license": "CC_BY_4_0",
      "modified": true,
      "modifiedDesc": "调色、裁切"
    }
  ]
}
```

#### 字段说明

| 字段 | 必需 | 说明 |
|------|------|------|
| `type` | 是 | 素材类型：`image` / `audio` / `font` / `other` |
| `path` | 是 | 素材在主题包内的相对路径 |
| `name` | 否 | 素材名称/描述，缺省时用文件名 |
| `author` | 依赖许可 | CC0 可不填；CC BY、OFL、须署名类**必须** |
| `source` | 否 | 来源 URL |
| `sourceName` | 否 | 来源网站名称（用于署名显示） |
| `license` | 是 | 许可协议标识（见下方许可类型表） |
| `modified` | 否 | 是否修改过，默认 `false` |
| `modifiedDesc` | 否 | 修改内容简述 |
| `customText` | 否 | `CUSTOM` 许可类型时的自定义声明文本 |

#### 许可类型表 `license` 枚举

| 标识符 | 对应许可 | 典型场景 |
|--------|----------|----------|
| `CC0` | CC0 1.0 公有领域 | 免署名音效/图标 |
| `CC_BY_4_0` | CC BY 4.0 | 需署名的图标、音效 |
| `SIL_OFL_1_1` | SIL Open Font License 1.1 | 思源黑体、Fugaz One 等字体 |
| `FREE_COMMERCIAL_WITH_CREDIT` | 免费可商用（须署名） | 乌鸦Producer 音乐 |
| `FREE_COMMERCIAL` | 免费可商用（无须署名） | 部分免版权素材库 |
| `AI_GENERATED` | AI 生成（豆包等） | 豆包AI 生成的图像 |
| `CUSTOM` | 自定义文本 | 其他未覆盖的许可，由 `customText` 提供全文 |

---

### 二、许可模板库（系统内置）

系统内置每个许可类型的 **格式化声明文本模板**，含法律责任声明段落。例如：

#### CC0 模板
```markdown
#### {name}

- **文件：** `{path}`
- **作者：** {author}
- **来源：** [{sourceName}]({source})
- **许可：** [CC0 1.0](http://creativecommons.org/publicdomain/zero/1.0/)（公有领域，无需署名）
- **修改：** {modifiedStr}

本作品{未修改/已修改}。CC0 1.0 许可意味着作者已放弃其著作权及相关权利，将作品贡献至公有领域。
```

#### CC BY 4.0 模板
```markdown
#### {name}

- **文件：** `{path}`
- **作者：** {author}（{sourceName}）
- **来源：** [{sourceName}]({source})
- **许可：** [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
- **修改：** {modifiedStr}

本作品使用了 {author} 提供的素材，按 CC BY 4.0 许可要求声明如下：

**署名：**
> {name} by {author} — {source}
> 许可：CC BY 4.0 — https://creativecommons.org/licenses/by/4.0/

**使用条款：**
- 可用于商业项目，无需额外支付版权费用
- 必须署名作者 {author} 并提供来源链接
- {modifiedText}，修改后仍需署名
- 不得作为独立文件转售、再许可或分发
- 不得用于违法或诽谤性内容
```

#### AI_GENERATED 模板
```markdown
#### {name}

- **文件：** `{path}`
- **来源：** [{sourceName}]({source})
- **许可：** {sourceName} 视觉素材可商用（需二次设计后再使用）
- **修改：** {modifiedStr}

本作品使用了 {sourceName} 生成的图像素材，按平台商用说明声明如下：

- 可用于海报、官网背景、社媒配图、包装辅助图、演示文稿等视觉场景
- 建议进行二次设计后再使用（加入品牌色、版式、文案等原创元素）
- 不建议直接原样大图售卖或作为独家 IP 素材
- 用于商标/Logo/产品外观专利时需进一步差异化设计
```

#### FREE_COMMERCIAL_WITH_CREDIT 模板
```markdown
#### {name}

- **文件：** `{path}`
- **作者：** {author}
- **来源：** [{sourceName}]({source})
- **许可：** 免费可商用，须署名
- **修改：** {modifiedStr}

**署名：**
> 素材由{author}提供
> {author}的个人网站：{source}

**使用条款：**
- 可用于商业项目（广告、游戏、视频制作等），无需额外支付版权费用
- 不得以任何形式修改或声称拥有著作权与署名权
- 不享有独家使用权
- 不得作为商品直接转售
- 使用时须遵守适用法律法规，不得用于违法违规事项，不得抹黑作者形象
```

---

### 三、运行时生成器（系统实现）

#### 核心类

**`CopyrightGenerator`** — `util/system/CopyrightGenerator.java`

职责：
1. 读取主题目录下的 `copyright.json`
2. 校验字段完整性（必填项缺失时记 ERROR 日志）
3. 按类型 → 许可分组排序
4. 套用许可模板生成格式化文本
5. 写入 `THIRDPARTY_LICENSES.md`
6. 返回生成结果（成功/失败+条目数）

#### 分组排序规则

1. 一级分组：**素材类型**（图片 → 音频 → 字体 → 其他）
2. 二级分组：**许可类型**（同一许可的条目归在一起）
3. 组内按 `path` 字母排序

#### 增量维护

- **新增素材**：用户在 `copyright.json` 追加条目，下次加载自动追加到声明文件
- **删除素材**：用户从 `copyright.json` 移除条目，下次加载自动从声明文件移除（全量重新生成）
- **修改素材**：字段变更后重新生成对应条目

#### 触发时机

| 时机 | 说明 |
|------|------|
| **主题加载时** | `ThemeManager.loadTheme()` 末尾调用，自动检测 `copyright.json` |
| **文件不存在** | 静默跳过，不打 ERROR（允许主题无第三方素材） |
| **生成失败** | 记录 ERROR 日志，不影响主题正常加载 |

---

### 四、校验与告警

#### 生成时校验

| 检查项 | 严重级别 | 处理方式 |
|--------|----------|----------|
| `author` 为空但许可须署名 | WARN | 记录日志，生成时署名位置标记 `[未指定作者]` |
| `source` 格式异常 | WARN | 记录日志，生成时原样输出 |
| 未知 `license` 标识 | ERROR | 跳过该条目，继续处理其他条目 |
| `path` 文件不存在 | WARN | 记录日志，仍生成声明（可能是后续才放入文件） |
| `customText` 为空但许可为 `CUSTOM` | ERROR | 跳过该条目 |

#### 游戏内提示

- 主题加载完成后，若有 WARN 级别的校验问题，在游戏内 UI 右上角显示 **"主题版权声明有 N 项信息待补充"** 的提示
- 点击可查看具体哪些条目的哪些字段缺失
- 该提示仅对**当前主题的作者**可见（通过检测主题路径是否在可写目录下判断），普通玩家看不到

---

## 与现有体系的集成

### 涉及文件

| 文件 | 变更 |
|------|------|
| 新增 `CopyrightGenerator.java` | 运行时生成器核心类 |
| 新增 `LicenseTemplate.java` | 许可模板枚举 + 模板文本 |
| 新增 `CopyrightEntry.java` | 单条版权声明的数据模型（对应 JSON 结构） |
| 新增 `CopyrightJson.java` | copyright.json 的完整数据模型 |
| `ThemeManager.java` | `loadTheme()` 末尾加入生成调用 |
| `PathName.java` | 新增 `COPYRIGHT_CONFIG = "copyright.json"`、`THIRD_PARTY_LICENSES = "THIRDPARTY_LICENSES.md"` |

### 包位置

```
com.hujiugame.qingfeng.util.system
  ├── CopyrightGenerator.java    # 生成器
  ├── CopyrightEntry.java        # 单条版权数据(record)
  ├── CopyrightJson.java         # copyright.json 数据模型
  └── LicenseTemplate.java       # 许可模板枚举
```

---

## 开放问题

1. **多语言输出**：声明文件用中文还是英文？现有声明文件是中文，但主题可能面向国际玩家。方案：优先主题语言，回退到中文。
2. **自定义许可扩展**：是否允许用户自注册许可模板？（例如通过另一个 JSON 字段提供模板文本）
3. **`CUSTOM` 类型的 UI 输入**：游戏内编辑主题版权时，自定义许可是自由文本输入还是有限制？
4. **主题商店集成**：将来如果有主题商店，上传前的版权校验是否由服务端 `CopyrightGenerator` 执行一遍并拒绝缺失必填字段的主题？
5. **现有主题迁移**：默认主题已有的 `THIRDPARTY_LICENSES.md` 是否保留手写版本，还是也改为 `copyright.json` 驱动？

---

## 实施步骤（参考）

| 步骤 | 内容 |
|------|------|
| 1 | 定义 `CopyrightEntry` record + `CopyrightJson` 数据模型 |
| 2 | 实现 `LicenseTemplate` 枚举及全部许可模板文本 |
| 3 | 实现 `CopyrightGenerator`：读取 → 校验 → 排序 → 模板填充 → 写入 |
| 4 | `ThemeManager` 集成：`loadTheme()` 末尾调用 |
| 5 | `PathName` 补充常量 |
| 6 | 默认主题试点：将现有声明改为 `copyright.json` 驱动 |
| 7 | 游戏内提示 UI（可选，可后续再做） |
