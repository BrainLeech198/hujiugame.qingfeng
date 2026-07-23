# 脚本引擎内部规范

> 本文档分为两部分：
> - **第一部分：脚本指令语言规范（Block JSON）** — 定义完整的指令集、值系统、变量作用域、运算符集、执行模型和控制流规则
> - **第二部分：脚本引擎内部编码规范** — 覆盖 `data/script` 包内的编码约定，不取代 `develop/CODING_STYLE.md`

---

# 第一部分：脚本指令语言规范（Block JSON）

> 脚本引擎采用积木式 Block JSON 作为中间格式。积木编辑器输出的也是同样的 JSON 结构，手写 `.script` 文件亦编译为此格式。

---

## 一、总览

### 1.1 通用 JSON 格式

所有指令（ScriptCommand、ValueCommand、TriggerCommand）遵循统一的三段式结构：

```json
{"type": "<类别>", "action": "<动作>", "param": {<参数>}}
```

- `type` — 指令大类（ScriptCommandType / ValueCommandType / TriggerType）
- `action` — 具体动作（ScriptCommandAction / ValueCommandAction / TriggerAction）
- `param` — 按 action 定参（ScriptCommandParam / ValueCommandParam / TriggerParam）

> `param` 在值指令（ValueCommand）中可选，缺省时自动使用空对象。

### 1.2 三层领域

| 领域 | 接口 | type 取值 | action 枚举 |
|------|------|-----------|-------------|
| 脚本指令 | `ScriptCommand` | `control` / `variable` / `story` | `ScriptCommandAction` |
| 值指令 | `ValueCommand` | `atomic` / `math` / `compare` / `logic` | `ValueCommandAction` |
| 触发器 | `TriggerCommand` | `label` / `image` / `button` | `TriggerAction` |

### 1.3 基础类型

由 `TypeMapper` 支持的四种类型及 JSON 表示：

| 类型 | JSON 字符串 |
|------|------------|
| `int` | `"int"` |
| `float` | `"float"` |
| `boolean` | `"boolean"` |
| `String` | `"string"` |

---

## 二、ScriptCommand — 脚本指令

每条 ScriptCommand 的 JSON 结构：

```json
{"type": "<大类>", "action": "<动作>", "param": {<动作参数字段>}}
```

### 2.1 ScriptCommandType（大类）

| 枚举 | JSON 字符串 | 说明 |
|------|------------|------|
| CONTROL | `"control"` | 控制流 |
| VARIABLE | `"variable"` | 变量操作 |
| STORY | `"story"` | 游戏流程 |

### 2.2 ScriptCommandAction（动作）与 Param 对照

#### CONTROL（控制流）

| action | JSON 字符串 | param 类 | param 字段 | 说明 |
|--------|------------|----------|-----------|------|
| IF | `"if"` | `IfControlScriptCommandParam` | `condition`(LogicValue), `thenCommands`(数组), `elseCommands`(数组) | 条件分支 |
| WHILE | `"while"` | `WhileControlScriptCommandParam` | `condition`(LogicValue), `commands`(数组) | 条件循环 |
| BREAK | `"break"` | `BreakControlScriptCommandParam` | 无（空对象） | 跳出 while |
| CONTINUE | `"continue"` | `ContinueControlScriptCommandParam` | 无（空对象） | 跳过本轮剩余指令 |
| RETURN | `"return"` | `ReturnControlScriptCommandParam` | `value`(MathValue) — 返回值表达式，省略时取脚本声明的 defaultValue | 从当前脚本返回 |
| WAIT | `"wait"` | `WaitControlScriptCommandParam` | `time`(float) | 阻塞等待秒数 |
| CALL | `"call"` | `CallControlScriptCommandParam` | `script`(String) | 调用 .script 文件 |

> condition 为 `LogicValue`（逻辑表达式），thenCommands/elseCommands/commands 为 `ScriptCommand[]`。

**if 示例（如果 hp > 0，则推进到下一页；否则从当前脚本返回）：**

- `condition` 使用 LogicValue，前缀表达式：运算符在前，操作数在后
- 求值过程：`greater` 是双目比较，递归求值右操作数 `var(hp)` 和左操作数 `const(0)`，判断 `hp > 0`
- `thenCommands`：条件为真时串行执行
- `elseCommands`：条件为假时串行执行

```json
{"type":"control","action":"if","param":{
  "condition":{"expression":[
    {"type":"compare","action":"greater","param":{}},
    {"type":"atomic","action":"variable","param":{"name":"hp"}},
    {"type":"atomic","action":"const","param":{"class":"int","value":0}}
  ]},
  "thenCommands":[
    {"type":"story","action":"forward_page","param":{}}
  ],
  "elseCommands":[
    {"type":"control","action":"return","param":{}}
  ]
}}
```

**while 示例（i < 10 时循环执行 i = i + 1）：**

- `condition` 使用 LogicValue，前缀表达式：`less` + 操作数 `i` + `10`
- `commands`：每次循环执行的指令数组
- 执行器在每轮循环前判断 condition，为 1 才执行 commands

```json
{"type":"control","action":"while","param":{
  "condition":{"expression":[
    {"type":"compare","action":"less","param":{}},
    {"type":"atomic","action":"variable","param":{"name":"i"}},
    {"type":"atomic","action":"const","param":{"class":"int","value":10}}
  ]},
  "commands":[
    {"type":"variable","action":"assignment","param":{
      "name":"i",
      "value":{"expression":[
        {"type":"math","action":"add","param":{}},
        {"type":"atomic","action":"const","param":{"class":"int","value":1}},
        {"type":"atomic","action":"variable","param":{"name":"i"}}
      ]}
    }}
  ]
}}
```

> `break` 跳出当前 while，`continue` 跳过本轮剩余指令进入下一轮判断。

**wait 示例（等待 2 秒，阻塞当前帧的执行，每帧 tick 递减计时）：**

```json
{"type":"control","action":"wait","param":{"time":2.0}}
```

**call 示例（调用 calc_score.script，推新执行帧，执行完毕返回后继续当前帧）：**

- `script` 值为 .script 文件的资源路径（不含扩展名或含扩展名，由执行器定）
- 被调用脚本的参数映射通过 Script 的 `arguments` 列表定义

```json
{"type":"control","action":"call","param":{"script":"calc_score"}}
```

#### VARIABLE（变量操作）

| action | JSON 字符串 | param 类 | param 字段 | 说明 |
|--------|------------|----------|-----------|------|
| CREATE | `"create"` | `CreateVariableScriptCommandParam` | `class`(String), `name`(String), `value`(MathValue) | 创建存档变量 |
| ASSIGNMENT | `"assignment"` | `AssignmentVariableScriptCommandParam` | `name`(String), `value`(MathValue) | 为变量赋值 |

> value 使用 `MathValue`，允许：const / variable / scope_variable / game_variable 原子指令及 math 运算指令。

**create 示例（创建一个名为 click_count 的 int 类型存档变量，初始值为 0）：**

- `class`：变量类型 `int` / `float` / `boolean` / `string`
- `name`：变量名，后续通过 `variable` 原子指令引用
- `value`：初始值表达式（MathValue），这里使用单值退化：expression 中只有一个 const

```json
{"type":"variable","action":"create","param":{
  "class":"int",
  "name":"click_count",
  "value":{"expression":[{"type":"atomic","action":"const","param":{"class":"int","value":0}}]}
}}
```

**assignment 示例（click_count = click_count + 1，先读取变量值，再加 1 后写回）：**

- `name`：要赋值的变量名
- `value`：赋值表达式（MathValue），前缀语法：运算符 `add` 在前，然后是两个操作数 `const(1)` 和 `var(click_count)`

```json
{"type":"variable","action":"assignment","param":{
  "name":"click_count",
  "value":{"expression":[
    {"type":"math","action":"add","param":{}},
    {"type":"atomic","action":"const","param":{"class":"int","value":1}},
    {"type":"atomic","action":"variable","param":{"name":"click_count"}}
  ]}
}}
```

#### STORY（游戏流程）

| action | JSON 字符串 | param 类 | param 字段 | 说明 |
|--------|------------|----------|-----------|------|
| FORWARD_PAGE | `"forward_page"` | `ForwardPageStoryScriptCommandParam` | 无（空对象） | 推进到下一页 |
| GOTO_PAGE | `"goto_page"` | `GotoPageStoryScriptCommandParam` | `tree`(JsonEntity — 树结构信息), `page`(String) | 跳转到指定树和页面 |

**forward_page 示例（清除当前页的所有执行帧，加载顺序下一页的 start 指令）：**

```json
{"type":"story","action":"forward_page","param":{}}
```

**goto_page 示例（跳转到指定页面 ID，清除当前帧后加载目标页面的 start 指令）：**

- `page`：目标页面 ID，对应页面配置文件中的页面标识

```json
{"type":"story","action":"goto_page","param":{"page":"page_battle"}}
```

### 2.3 整表汇总

| type | action | action 字符串 | param 字段 |
|------|--------|-------------|-----------|
| control | IF | `"if"` | `condition`(LogicValue), `thenCommands`(ScriptCommand[]), `elseCommands`(ScriptCommand[]) |
| control | WHILE | `"while"` | `condition`(LogicValue), `commands`(ScriptCommand[]) |
| control | BREAK | `"break"` | — |
| control | CONTINUE | `"continue"` | — |
| control | RETURN | `"return"` | — |
| control | WAIT | `"wait"` | `time`(float) |
| control | CALL | `"call"` | `script`(String) |
| variable | CREATE | `"create"` | `class`(String), `name`(String), `value`(MathValue) |
| variable | ASSIGNMENT | `"assignment"` | `name`(String), `value`(MathValue) |
| story | FORWARD_PAGE | `"forward_page"` | — |
| story | GOTO_PAGE | `"goto_page"` | `page`(String) |

---

## 三、ValueCommand — 值指令系统

### 3.1 值对象包装

值表达式使用 `ValueObject` 包装，JSON 格式为：

```json
{"expression": [<ValueCommand>, <ValueCommand>, ...]}
```

`expression` 数组按**波兰表达式（前缀）**顺序排列，运算符在前、操作数在后，求值时从前往后递归求值：

1. 遇到 ATOMIC 指令 → 直接返回该值
2. 遇到 MATH/COMPARE/LOGIC 指令 → 根据目数递归求值后续 N 个指令作为操作数，执行运算
3. 整个过程如同一棵前缀表达式树的线性化

> 例如 `1 + 2` 表达为 `[add, const(1), const(2)]`：add 是双目运算符，递归求值 const(1) 和 const(2) 作为操作数，计算 1+2=3。

### 3.2 受限子类

| 类 | 限制规则 | 使用场景 |
|----|---------|---------|
| `MathValue` | 仅允许 MATH + ATOMIC（const/variable/scope_variable/game_variable，**不含 true/false**） | 变量 create/assignment 的 value 字段 |
| `LogicValue` | 仅允许 ATOMIC + COMPARE + LOGIC（**不含 MATH**） | if/while 的 condition 字段 |

### 3.3 ValueCommandType（大类）

| 枚举 | JSON 字符串 | 说明 |
|------|------------|------|
| ATOMIC | `"atomic"` | 原子值（叶子节点） |
| MATH | `"math"` | 算术运算 |
| COMPARE | `"compare"` | 比较运算 |
| LOGIC | `"logic"` | 逻辑运算 |

### 3.4 ValueCommandAction（动作）与 Param 对照

#### ATOMIC（原子值）

| action | JSON 字符串 | param 字段 | 说明 |
|--------|------------|-----------|------|
| CONST | `"const"` | `class`(String), `value`(typed) | 字面常量 |
| VARIABLE | `"variable"` | `name`(String) | 全局存档变量引用 |
| SCOPE_VARIABLE | `"scope_variable"` | `name`(String) | 执行帧局部变量引用 |
| GAME_VARIABLE | `"game_variable"` | `key`(String) | 引擎只读数据引用 |
| TRUE | `"true"` | — | 布尔真（int 1） |
| FALSE | `"false"` | — | 布尔假（int 0） |

**const 示例（在 expression 中直接提供一个编译期确定的字面值）：**

- `class` 声明类型，`value` 为具体的字面值
- 支持四种类型：int（整数）、float（浮点）、boolean（布尔）、string（字符串）

```json
{"type":"atomic","action":"const","param":{"class":"int","value":42}}
{"type":"atomic","action":"const","param":{"class":"float","value":3.14}}
{"type":"atomic","action":"const","param":{"class":"boolean","value":true}}
{"type":"atomic","action":"const","param":{"class":"string","value":"hello"}}
```

**variable 示例（读取已创建的存档变量 hp，不存在时返回 int 默认值 0）：**

- 对应 `variable create` 指令创建的全局变量，存入 PlayerData 存档
- 跨页面持久化，可读写

**scope_variable 示例（读取当前作用域的局部变量 base）：**

- 遵循词法作用域：内部块（if/when 的 thenCommands/elseCommands/commands）继承外层块的 scope_variable
- 生命周期：所属块执行期间存在，块结束后销毁
- 当前仅来自 .script 中 arguments 映射为初始值，或者由外层块继承而来

**game_variable 示例（读取引擎运行时数据 player_hp，只读，不存在时返回类型默认值）：**

- 从 GameInfoManager 读取
- `key` 对应引擎运行时的 GameInfoKey

**true/false 示例（直接返回布尔真/假，等价于 const(true)/const(false) 的简写形式）：**

```json
{"type":"atomic","action":"true","param":{}}
{"type":"atomic","action":"false","param":{}}
```

> `true` 求值为 int 1，`false` 求值为 int 0。`MathValue` 中不允许使用 true/false，仅 `LogicValue` 可用。

#### MATH（算术运算）

所有 MATH 指令的 param 均为空对象 `{}`，操作数由 expression 栈消费。

| action | JSON 字符串 | 目数 | 说明 |
|--------|------------|------|------|
| ADD | `"add"` | 双目 | 加法（含字符串拼接） |
| SUB | `"sub"` | 双目 | 减法 |
| MUL | `"mul"` | 双目 | 乘法 |
| DIV | `"div"` | 双目 | 除法（除零返回 0） |
| NEG | `"neg"` | 单目 | 负号 |
| RANDOM | `"random"` | 单目 | 随机数 [0, N) |

**1 + 2 的完整表达式（前缀求值：add 是双目运算符，递归求值 const(1) 和 const(2)，计算 1+2=3）：**

```json
{"expression":[
  {"type":"math","action":"add","param":{}},
  {"type":"atomic","action":"const","param":{"class":"int","value":1}},
  {"type":"atomic","action":"const","param":{"class":"int","value":2}}
]}
```

#### COMPARE（比较运算）

所有 COMPARE 指令的 param 均为空对象，结果返回 int 1（真）/ 0（假）。

| action | JSON 字符串 | 目数 | 说明 |
|--------|------------|------|------|
| EQUAL | `"equal"` | 双目 | 等于 |
| NOT_EQUAL | `"not_equal"` | 双目 | 不等于 |
| GREATER | `"greater"` | 双目 | 大于 |
| LESS | `"less"` | 双目 | 小于 |
| GREATER_EQUAL | `"greater_equal"` | 双目 | 大于等于 |
| LESS_EQUAL | `"less_equal"` | 双目 | 小于等于 |

**hp > 0 的比较表达式（greater 双目比较，递归求值 var(hp) 和 const(0)，判断 hp > 0）：**

- type 为 `compare`，因为 `greater` 属于 COMPARE 指令大类
- 前缀求值：运算符 `greater` 在前，两个操作数 `var(hp)`、`const(0)` 在后

```json
{"expression":[
  {"type":"compare","action":"greater","param":{}},
  {"type":"atomic","action":"variable","param":{"name":"hp"}},
  {"type":"atomic","action":"const","param":{"class":"int","value":0}}
]}
```

#### LOGIC（逻辑运算）

所有 LOGIC 指令的 param 均为空对象，结果返回 int 1（真）/ 0（假）。

| action | JSON 字符串 | 目数 | 说明 |
|--------|------------|------|------|
| AND | `"and"` | 双目 | 逻辑与 |
| OR | `"or"` | 双目 | 逻辑或 |
| NOT | `"not"` | 单目 | 逻辑非 |

**a > 0 and b < 10 的逻辑表达式（前缀求值，运算符递归求子表达式）：**

- 等价于 `and(greater(a, 0), less(b, 10))`
- `greater`/`less` 用 `type: "compare"`，`and` 用 `type: "logic"`

```json
{"expression":[
  {"type":"logic","action":"and","param":{}},
  {"type":"compare","action":"greater","param":{}},
  {"type":"atomic","action":"variable","param":{"name":"a"}},
  {"type":"atomic","action":"const","param":{"class":"int","value":0}},
  {"type":"compare","action":"less","param":{}},
  {"type":"atomic","action":"variable","param":{"name":"b"}},
  {"type":"atomic","action":"const","param":{"class":"int","value":10}}
]}
```

> 求值过程：and 双目，递归左子表达式 = greater(a, 0)，递归右子表达式 = less(b, 10)，两个子表达式结果逻辑与。等价于 if 条件中的 `(a > 0) && (b < 10)`。

### 3.5 波兰表达式对照表

| 数学表达式 | 波兰表达式（前缀） | expression 数组（缩写） |
|-----------|------------------|----------------------|
| `1 + 2` | `+ 1 2` | `[add, const(1), const(2)]` |
| `a + b * 2` | `+ a * b 2` | `[add, var(a), mul, var(b), const(2)]` |
| `(a + b) * 2` | `* + a b 2` | `[mul, add, var(a), var(b), const(2)]` |
| `a > 0` | `> a 0` | `[greater, var(a), const(0)]` |
| `-a` | `neg a` | `[neg, var(a)]` |
| `random(100)` | `random 100` | `[random, const(100)]` |
| `a > 0 and b < 10` | `and > a 0 < b 10` | `[and, greater, var(a), const(0), less, var(b), const(10)]` |

---

## 四、Trigger — 触发器

### 4.1 容器结构

`Trigger.java` 包装触发器命令和指令列表：

```json
{
  "trigger": {<TriggerCommand>},
  "commands": [{<ScriptCommand>}, ...]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `trigger` | TriggerCommand | 是 | 触发器命令（type/action/param） |
| `commands` | ScriptCommand[] | 是 | 触发后串行执行的指令列表 |

### 4.2 TriggerType（触发器大类）

| 枚举 | JSON 字符串 |
|------|------------|
| IMAGE | `"image"` |
| LABEL | `"label"` |
| BUTTON | `"button"` |

### 4.3 TriggerAction（动作）与 Param

当前已实现：

| action | JSON 字符串 | type | param 类 | param 字段 |
|--------|------------|------|----------|-----------|
| LABEL_CLICK | `"label_click"` | label | `LabelClickTriggerParam` | `tag`(String) |

**label_click 示例（当标签名为 dialogue_textbox 的 UI 组件被点击时，串行执行 commands 中的指令）：**

- `trigger.param.tag`：监听的标签名称
- `commands`：触发后串行执行的指令列表，这里执行 forward_page 推进到下一页

```json
{"trigger":{"type":"label","action":"label_click","param":{"tag":"dialogue_textbox"}},
 "commands":[
   {"type":"story","action":"forward_page","param":{}}
 ]}
```

---

## 五、Script — 脚本定义

`.script` 文件的 JSON 结构：

```json
{
  "arguments": [
    {"class": "<类型>", "name": "<参数名>"}
  ],
  "commands": [
    {<ScriptCommand>}
  ],
  "return": {
    "class": "<类型>",
    "name": "<返回值名称>",
    "defaultValue": <字面值>
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `arguments` | Argument[] | 否 | 参数列表。缺省为空数组 |
| `commands` | ScriptCommand[] | 是 | 指令列表 |
| `return` | Return | 是 | 返回值声明 |

### Argument

| 字段 | 说明 |
|------|------|
| `class` | 类型：`int` / `float` / `boolean` / `string` |
| `name` | 参数名，调用时按序映射为 scope_variable（顶层块级作用域） |

### Return

| 字段 | 说明 |
|------|------|
| `class` | 返回值类型 |
| `name` | 返回值名称（暂无语义，预留扩展） |
| `defaultValue` | **字面值**（不是表达式），没有 return 时的兜底值。按 class 类型存储：int/float/boolean/String |

> `defaultValue` 是原始字面值（如 `0`、`3.14`、`true`、`"ok"`），**不是** MathValue 或 ValueObject。

**完整示例（接收两个 int 参数 base 和 extra，执行空 return 后返回 defaultValue = 0）：**

- `arguments`：定义两个参数，按序映射为 scope_variable（base 和 extra），归属当前 .script 的顶层块
- 调用方通过 `control/call` 传入参数，第 N 个参数值赋给 arguments[N].name
- `commands`：执行体，这里直接 return（取脚本的 defaultValue 作为返回值）
- 如果 commands 中有 if/when/while 等嵌套块，内部可直接读取 base 和 extra（词法继承）
- 如果 commands 中没有 return 指令，执行完后自动以 `defaultValue` 为返回值

```json
{
  "arguments":[
    {"class":"int","name":"base"},
    {"class":"int","name":"extra"}
  ],
  "commands":[
    {"type":"control","action":"return","param":{}}
  ],
  "return":{"class":"int","name":"","defaultValue":0}
}
```

> 对应的调用方使用 control/call：`{"type":"control","action":"call","param":{"script":"calc_score"}}`，调用后推新执行帧，arguments[0] 映射为 scope_variable base，arguments[1] 映射为 scope_variable extra，这两个变量在脚本的顶层块及所有嵌套块中均可见。执行完返回后原帧继续。

---

## 六、变量作用域

| 值类型 | action | 作用域 | 读写 | 存储位置 | 生命周期 |
|--------|--------|--------|------|---------|---------|
| variable | `"variable"` | 全局 | 读写 | PlayerData 存档 | 跨页面持久，随存档保存/加载 |
| scope_variable | `"scope_variable"` | 块级（词法作用域） | 读 | 执行栈帧（内存） | 属于当前代码块，子块（if/when 内部）继承可见，块结束销毁 |
| game_variable | `"game_variable"` | 引擎全局 | 只读 | GameInfoManager | 引擎运行时，页面无关 |

> `scope_variable` 遵循常规语言的词法作用域规则：在 .script 中通过 arguments 映射为初始值，嵌套块（如 if 的 thenCommands、while 的 commands）内部可直接读取外层块的 scope_variable。目前仅支持读取，暂无内部创建指令。

---

## 七、执行模型

### 7.1 帧驱动调度

`ScriptExecutor.update(deltaTime)` 每帧执行，分配 `MAX_COMMAND_COUNT_PER_FRAME=50` 条指令配额：

```
ScriptExecutor.update(deltaTime)
    │
    ├── remainingCommandsCount = 50
    ├── 随机打乱 taskMap.values() → 遍历每个 TaskStack
    │       └── executeTaskStack(taskStack, remainingCommandsCount)
    │               ├── peek 栈顶 Task → instanceof 分发
    │               │     ├── ScriptTask  → executeScriptTask (控制流/变量/故事指令)
    │               │     ├── ValueTask   → executeValueTask  (RPN 前缀表达式求值)
    │               │     └── TriggerTask → executeTriggerTask (每帧轮询触发条件)
    │               └── 子任务通过 taskStack.push() 推入栈顶，下一帧自动 re-peek
    └── 配额耗尽或所有栈处理完毕 → 返回
```

### 7.2 任务类型

| 任务 | TaskType | 行为 |
|------|----------|------|
| ScriptTask | `COMMAND_NORMAL` / `COMMAND_WHILE` / `COMMAND_LOOP` / `COMMAND_CALL` | 顺序执行 ScriptCommand 列表，支持 if/while/call 子任务委派 |
| ValueTask | `VALUE_MATH` / `VALUE_LOGIC` | RPN 操作栈求值，操作符推栈→参数推栈→就绪自动计算→结果回流 |
| TriggerTask | `TRIGGER` | 每帧轮询触发条件（如 isLabelClicked），命中时推入 ScriptTask 子任务 |

### 7.3 作用域变量链

```
ScriptTask.scopeVariables: Map<String, Object>
    │
    ├── 继承自父任务（构造时传入父任务的 scopeVariables）
    ├── call 指令通过 parseArguments 将实参映射为子任务的 scopeVariables
    └── break/continue/task-finish 时通过 synchronizeParentTaskScopeVariables
        将子任务修改回写到父任务
```

### 7.4 返回值委派

```
父任务.enableReturnValue()                    // 开启"等待子任务返回"模式
    → 创建 ValueTask/ScriptTask 子任务
    → taskStack.push(subTask)                  // 子任务入栈
    → 子任务执行完毕 finish
        → returnParentTaskValue(parent, value) // 回写返回值
        → 父任务.isReturnValueSet() == true    // 父任务继续
```

### 7.5 脚本调用链路

```
behavior.json / 页面行为入口
    │
    ├── start: [...]     ← 进入画面前执行一次（ScriptTask COMMAND_NORMAL）
    ├── loop: [...]      ← 进入后创建 ScriptTask COMMAND_LOOP，每轮执行完回到开头
    └── trigger[{trigger, commands}]
           │
           └── TriggerTask 每帧轮询 → 命中后推 ScriptTask COMMAND_NORMAL

control/call
    │
    └── 推 ScriptTask COMMAND_CALL 子任务，执行目标 .script，返回后继续原帧

return
    └── 推 ValueTask 求值表达式 → 结果 setReturnValue → 弹出当前任务
```

### 7.6 容错规则

| 场景 | 行为 |
|------|------|
| 变量不存在 | 返回类型的默认值（int:0, float:0.0f, string:"", boolean:false） |
| 除零 | 返回 0.0f |
| 类型不匹配 | 先尝试自动转换，失败返回 0 |
| Expression 为空 | 返回 0 |
| Operation 目数不足 | 缺失的操作数视为 0 |
| call 脚本文件不存在 | 推入默认值 0 并跳过，打 error 日志 |
| callAtomic 脚本文件不存在 | 推入默认值 0 并跳过，打 debug 日志 |

---

# 第二部分：脚本引擎内部编码规范

> 本文档覆盖 `script` 包内的编码约定，**不取代** `develop/CODING_STYLE.md`（全局语法风格）。
> 适用目录：`com.hujiugame.qingfeng.script` 及其所有子包。

---

## 1. 三层架构

每个领域遵循 **Type → Action → Impl(+Param)** 三层结构：

```
              Type 枚举（大类）       →      Action 枚举（具体动作）     →      Impl(接口) + Param(接口)
  command/   ScriptCommandType       →    ScriptCommandAction          →    ScriptCommand + ScriptCommandParam
  value/     ValueCommandType        →    ValueCommandAction           →    ValueCommand + ValueCommandParam
  trigger/   TriggerType             →    TriggerAction                →    TriggerCommand + TriggerParam
```

### 文件布局

```
<domain>/
  <Type>Enum.java               # 大类枚举
  <Command>Interface.java       # 命令接口
  <Command>Parser.java          # 解析器（仅静态方法）
  action/
    <Action>Enum.java           # 动作枚举
    <Type>Impl.java             # 实现类，每个文件一个
  param/
    <Param>Interface.java       # 参数接口
    <sub-type>/
      <Specific>Param.java      # 具体参数类，每个文件一个
```

### 对应关系表

| 领域 | 接口 | 类型枚举 | 动作枚举 | 实现文件 | 参数接口 | 参数目录 |
|------|------|----------|----------|----------|----------|----------|
| command | `ScriptCommand` | `ScriptCommandType` | `ScriptCommandAction` | `XxxScriptCommand.java` | `ScriptCommandParam` | `param/<type>/` |
| value | `ValueCommand` | `ValueCommandType` | `ValueCommandAction` | `XxxValueCommand.java` | `ValueCommandParam` | `param/<type>/` |
| trigger | `TriggerCommand` | `TriggerType` | `TriggerAction` | `XxxTrigger.java` | `TriggerParam` | `param/<type>/` |

---

## 2. Action-Param 类型安全

每个命令实现类必须包含 `ACTION_PARAM_MAP`：

```java
private static final Map<ActionEnum, Class<?>> ACTION_PARAM_MAP;

static
{
    ACTION_PARAM_MAP = new HashMap<>();
    ACTION_PARAM_MAP.put(ActionEnum.FOO, FooParam.class);
    ACTION_PARAM_MAP.put(ActionEnum.BAR, BarParam.class);
}
```

构造器中校验参数类型：

```java
public ConcreteCommand (ActionEnum action, ParamInterface param)
{
    this.commandType = TypeEnum.TYPE;
    this.commandAction = action;
    if (!ACTION_PARAM_MAP.get(action).isInstance(param))
    {
        throw new IllegalArgumentException(
            "Command parameter type : " + param.getClass().getName()
                + " does not match command action : " + action
        );
    }
    else
    {
        this.commandParam = param;
    }
    this.valid = true;
    buildJson();
}
```

规则：
- Action 枚举值必须独占一个 key，不可复用
- 无参数的命令也必须创建对应的 Param 类（见第 5 节）
- `isInstance()` 而非 `==` 或 `equals()`，允许子类

---

## 3. JSON 双向构造（Round-Trip）

每个数据类支持两种构造方式：

### 3.1 领域对象构造器

```java
public SomeClass (FieldType field)
{
    this.field = field;
    this.valid = true;
    buildJson();
}
```

- 直接赋值字段
- 立即设 `valid = true`
- 最后调用 `buildJson()`

### 3.2 JSON 构造器

```java
public SomeClass (JsonEntity json)
{
    this.valid = false;  // 起始为 false
    if (json.isMap())
    {
        // 校验必填字段
        if (!json.containsKey("field"))
        {
            throw new IllegalArgumentException("...");
        }
        this.field = json.getXxx("field");
        this.json = json;  // 保留原始 JSON
        this.valid = true;
    }
    else
    {
        throw new IllegalArgumentException("Command parameter must be a map.");
    }
}
```

规则：
- JSON 构造器抛出 `IllegalArgumentException`（运行时异常）
- 构造成功后保留 `this.json = json`
- `buildJson()` 仅在无效时抛 `IllegalStateException`

### 3.3 buildJson

- 方法名固定为 `buildJson`，`private` 无参
- 构建 `this.json = new JsonEntity()`
- 只写必要字段，不反写 `valid` 等内部状态

---

## 4. 验证模式（valid flag）

```java
private boolean valid;

public boolean isValid ()
{
    return valid;
}
```

- `valid` 默认 `false`（JDK 默认值即是，无需显式赋值）
- 构造过程任何失败**不抛异常**（JSON 构造器除外），设 `valid = false`
- `isValid()` 是调用方判断是否可用的依据

---

## 5. 无参数命令

没有字段的命令参数仍需创建独立类：

```java
public class NoopParam implements ScriptCommandParam
{
    private boolean valid = true;
    private JsonEntity json;

    public NoopParam ()
    {
        valid = true;
        buildJson();
    }

    public NoopParam (JsonEntity json)
    {
        valid = true;
        buildJson();
    }

    private void buildJson ()
    {
        json = new JsonEntity();
    }

    @Override
    public boolean isValid ()
    {
        return valid;
    }

    @Override
    public JsonEntity getJson ()
    {
        return json;
    }
}
```

原因：`ACTION_PARAM_MAP` 需要唯一的 `Class<?>` 来区分动作类型，空类也必不可少。

---

## 6. 枚举规范

```java
public enum XxxType
{
    FOO("foo"),
    BAR("bar");

    // 字符串常量（给 Parser switch 用）
    public static final String FOO_STRING = "foo";
    public static final String BAR_STRING = "bar";

    private final String displayString;

    XxxType (String displayString)
    {
        this.displayString = displayString;
    }

    // JSON 输出用
    public String getDisplayString ()
    {
        return displayString;
    }

    // JSON 输入用，不匹配返回 null（不抛异常）
    public static XxxType fromString (String jsonValue)
    {
        if (jsonValue == null) return null;
        for (XxxType t : values())
        {
            if (t.displayString.equals(jsonValue)) return t;
        }
        return null;
    }
}
```

规则：
- `XXX_STRING` 常量为 `public static final String`，首字母大写全称
- `getDisplayString()` 给 JSON 序列化用
- `fromString()` 给反序列化用，返回 `null` 而非抛异常

---

## 7. 解析器规范

```java
public class XxxCommandParser
{
    public static XxxCommand parse (JsonEntity json)
    {
        if (json.isMap())
        {
            LogUtils.debug(XxxCommandParser.class, "parse ...");
            // 1. 校验必填字段：type, action[, param]
            if (!json.containsKey("type"))  { LogUtils.error(...); return null; }
            if (!json.containsKey("action")) { LogUtils.error(...); return null; }
            if (!json.containsKey("param"))  { LogUtils.error(...); return null; }

            // 2. 提取原始字符串
            String type = json.getString("type");
            String action = json.getString("action");
            JsonEntity paramJson = json.getJsonEntityByKey("param");

            // 3. 分发
            XxxCommand command = dispatchXxxCommandJson(type, action, paramJson);
            if (command == null || !command.isValid())
            {
                LogUtils.error(...);
                return null;
            }
            return command;
        }
        else
        {
            LogUtils.error(...);
            return null;
        }
    }
}
```

### 分发模式

```java
private static XxxCommand dispatchXxxCommandJson (String type, String action, JsonEntity paramJson)
{
    switch (type)
    {
        case XxxType.FOO_STRING:
            return parseFooCommand(action, paramJson);
        default:
            LogUtils.error(...);
            return null;
    }
}

private static XxxCommand parseFooCommand (String action, JsonEntity paramJson)
{
    switch (action)
    {
        case ActionEnum.BAZ_STRING:
            return new FooCommand(ActionEnum.BAZ, new BazParam(paramJson));
        default:
            LogUtils.error(...);
            return null;
    }
}
```

规则：
- 解析器**仅含静态方法**，无状态
- switch 使用 `XXX_STRING` 常量，非硬编码字符串
- 失败返回 `null`，由调用方处理
- `parseList(JsonEntity)` 可选，用于解析 JSON 数组

---

## 8. 接口签名规范

```java
public interface XxxCommand
{
    boolean isValid ();
    JsonEntity getJson ();
    XxxType getCommandType ();       // 返回该命令所属大类枚举
    XxxAction getCommandName ();     // 返回该命令的具体动作枚举
    XxxParam getCommandParam ();     // 返回该命令的参数对象
}
```

```java
public interface XxxParam
{
    boolean isValid ();
    JsonEntity getJson ();
}
```

---

## 9. 命令接口命名对照表

| 领域 | 命令接口 | 动作枚举 → getter | 命令实现 |
|------|----------|-------------------|----------|
| command | `ScriptCommand` | `ScriptCommandAction` | `XxxScriptCommand` |
| value | `ValueCommand` | `ValueCommandAction` | `XxxValueCommand` |
| trigger | `TriggerCommand` | `TriggerAction` | `XxxTrigger` |

命令实现类命名风格：
- **command 领域**：`ControlScriptCommand`、`StoryScriptCommand`、`VariableScriptCommand`
- **value 领域**：`AtomicValueCommand`、`MathValueCommand`、`CompareValueCommand`、`LogicValueCommand`
- **trigger 领域**：`LabelClickTrigger`

规则：实现类名 = `[特征][领域]`，不使用 `Impl` 后缀。

---

## 10. 日志规范

```java
LogUtils.debug(ClassName.class, "parse 解析成功 (type): " + type + " (json): " + json);
LogUtils.error(ClassName.class, "parse 解析失败 (json): " + json);
```

规则：
- 所有 LogUtils 调用使用 `ClassName.class` 而非字符串标签
- 关键分支入口打 debug，失败打 error
- 日志信息中包含相关 JSON 或 key 字段，便于排查

---

## 11. 外部容器类

直接包装子结构的顶层容器，JSON 结构包含多个命名 key：

```java
public class Trigger
{
    public Trigger (JsonEntity json)
    {
        if (json.isMap())
        {
            // 校验每个 key
            if (!json.containsKey("trigger"))  { ... valid = false; return; }
            if (!json.containsKey("commands")) { ... valid = false; return; }

            this.triggerCommand = TriggerCommandParser.parse(json.getJsonEntityByKey("trigger"));
            this.commands = ScriptCommandParser.parseList(json.getJsonEntityByKey("commands"));

            if (this.triggerCommand == null || !this.triggerCommand.isValid() || this.commands == null)
            {
                ... valid = false; return;
            }
            this.json = json;
            this.valid = true;
        }
    }
}
```

规则：
- JSON 构造器不抛异常，失败设 `valid = false` 并 return
- `buildJson()` 委托子结构：`json.put("key", sub.getJson())`
- Trigger 容器包含 `trigger`（TriggerCommand）和 `commands`（ScriptCommand[]），不再包含废弃的 `script` 字段

---

## 12. ArgumentInfo — 参数传递模型

`ArgumentInfo` 区分**参数名**（调用方声明的参数标识）和**变量名**（实际查找的变量），通过 `argumentName` 和 `name` 分离两个概念：

```java
public class ArgumentInfo
{
    private String argumentName;      // 调用方参数名 → 作为 scopeVariables Map 的 key
    private ArgumentType type;        // CONST / VARIABLE / SCOPE_VARIABLE / GAME_VARIABLE
    private String name;              // 实际变量名（仅 VARIABLE/SCOPE_VARIABLE/GAME_VARIABLE 使用）
    private Object value;             // 字面值（仅 CONST 使用）
}
```

| Type | argumentName | name | value | 说明 |
|------|-------------|------|-------|------|
| CONST | 调用方参数名 | null | 字面值 | 常量参数，直接传值 |
| VARIABLE | 调用方参数名 | 全局变量名 | null | 从 GameVariableManager 查找 |
| GAME_VARIABLE | 调用方参数名 | 游戏变量 key | null | 从 GameInfoManager 查找 |

JSON 格式示例：
```json
{"argumentName": "speed", "type": "VARIABLE", "name": "player_speed"}
{"argumentName": "count", "type": "CONST", "value": 42}
```

---

## 13. 任务系统架构

任务系统位于 `script/task/` 包（非 `data/script`），核心组件：

```
Task (interface)
├── ScriptTask   — 顺序执行 ScriptCommand 列表，支持作用域变量和返回值委派
├── ValueTask    — RPN 操作栈求值，actionStack + actionParamStack 双栈模型
└── TriggerTask  — 每帧轮询 TriggerCommand，命中时推入 ScriptTask 子任务

TaskStack      — Stack<Task>，管理单条命名任务的栈式调用链
TaskType       — TRIGGER / COMMAND_NORMAL / COMMAND_LOOP / COMMAND_WHILE / COMMAND_CALL / VALUE_MATH / VALUE_LOGIC
```

### ScriptTask

```java
public class ScriptTask implements Task
{
    private final TaskType taskType;           // COMMAND_NORMAL / COMMAND_WHILE / COMMAND_LOOP / COMMAND_CALL
    private Task parentTask;                   // 父任务（作用域和返回值来源）
    private final List<ScriptCommand> commands; // 指令列表
    private int currentCommandIndex;           // 指令游标
    private Map<String, Object> scopeVariables; // 当前作用域变量
    // ... 返回值委派字段
}
```

### ValueTask

```java
public class ValueTask implements Task
{
    private final List<ValueCommand> commands;              // 前缀表达式指令
    private final Stack<ValueCommandAction> actionStack;    // 操作符栈
    private final Stack<List<Object>> actionParamStack;     // 操作数栈
    // actionParamCount 定义每个操作符的目数（ADD:2, NEG:1, etc.）
}
```

规则：
- `pushAction` → `pushActionParam` → `isActionParamReady` → `calculateValueTask` → `popAction`
- 计算结果通过 `tryPushValueToValueTask` 回流到上层（栈非空则推入上层参数列表，栈空则设置父任务返回值）
- ValueTask 的 while 循环不检查栈大小变化（与 ScriptTask 不同），同一帧内持续求值直到 finish 或配额耗尽

### TriggerTask

```java
public class TriggerTask implements Task
{
    private final TaskType taskType;    // TRIGGER
    private final Trigger trigger;      // 触发器数据（triggerCommand + commands）
    // isFinished() 永远返回 false（持续轮询）
    // getCurrentCommand() 返回 trigger.getTriggerCommand()
    // 返回值相关方法全为空操作
}
```

规则：
- TriggerTask 永远不被弹出，`isFinished()` 返回 false
- 每帧只执行一次 `executeTriggerCommand`，不消费指令配额
- 命中时通过 `taskStack.push(subTask)` 推入 ScriptTask 子任务

### ScriptExecutor 任务管理

```
ScriptExecutor
    taskMap: Map<String, TaskStack>     — 命名任务到任务栈的映射
    triggerTaskList: List<String>       — 触发器任务名列表（用于批量删除）

    addTask(name, task)
        → 创建新 TaskStack → push(task) → 如果是 TriggerTask 则记入 triggerTaskList

    removeTask(name)
        → 从 taskMap 和 triggerTaskList 中移除

    removeTriggerTask()
        → 遍历 triggerTaskList 快照 → 逐个 removeTask
```
