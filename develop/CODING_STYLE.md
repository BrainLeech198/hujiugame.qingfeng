# 氢风项目代码规范

> **文档定位**：项目 Java 代码的编码规范约定，包括大括号风格、命名规则、注释要求等。
>
> **文档结构**：
> - 按 `大括号 → 缩进 → 空格 → 修饰符顺序 → 注解 → 导入顺序 → 工具类 → 异常处理 → 日志 → 节分隔符 → 命名 → 注释 → 枚举 → 匿名内部类 → JSON` 顺序编排
> - 每条规范包含：规则说明 + 正确示例 + 错误示例对比
> - 命名规范用表格列出各元素类型的格式要求
>
> **更新规范**：
> 1. 【必须】更新 `develop/CHANGELOG.md` 记录本次变更
> 2. 【必须】修改命名约定时同步更新命名规范表格
> 3. 【必须】新增规范时确保同时给出正反示例
> 4. 【如果】修改提交规范 → 同步更新 `develop/COMMIT_STYLE.md`

## 1. 大括号

### Allman 风格（括号另起一行）
类、方法、控制流（if/for/while/try/catch）、静态初始化块的大括号都另起一行：

```java
public final class ClassName
{
    public void methodName (int param)
    {
        if (condition)
        {
            // body
        }
        else
        {
            // body
        }

        try
        {
            // body
        }
        catch (Exception e)
        {
            LogUtils.error(ClassName.class, "methodName", e);
        }
    }
}
```

## 2. 缩进

- 使用 **4 个空格**，不使用制表符。
- 多行参数保持对齐：
  ```java
  public GameHost (UserConfigManager userConfigManager,
                   ThemeManager themeManager,
                   LanguageManager languageManager)
  ```
- 长方法调用链换行使用 8 空格（双缩进）：
  ```java
  instanceContent.gameHost = new GameHost(
      instanceContent.userConfigManager,
      instanceContent.themeManager);
  ```

## 3. 空格

- **方法/构造函数声明**：左括号前要有一个空格：
  ```java
  public void setGamePath (String gamePath)
  public StateStructure (int state, int subState)
  ```

- **方法调用**：左括号前**没有**空格（标准 Java）：
  ```java
  player.setGamePath(gamePath);
  sceneStack.pushGameState(new StateStructure(...));
  ```

- **接口方法**：同样要遵循声明空格规则：
  ```java
  boolean init ();
  void run ();
  boolean hasEvent ();
  ```

- **控制流关键字**：后跟一个空格：
  ```java
  if (condition)
  for (Type var : collection)
  while (condition)
  catch (Exception e)
  ```

## 4. 修饰符顺序

按照 JLS 推荐顺序：`public/private/protected` → `static` → `final` → `transient/volatile` → `synchronized`：

```java
public static final int CONSTANT = 1;
private static final Map<String, Integer> MAP = new HashMap<>();
private final String fieldName;
```

## 5. 注解

- `@Override`、`@SuppressWarnings` 等独占一行，在目标声明之上。
- `@javax.annotation.Nullable` 使用完全限定名，不单独 import：
  ```java
  @javax.annotation.Nullable
  public String getRootPath ()
  ```

## 6. 导入顺序

按以下分组，每组之间用空行分隔：

1. Java 标准库（`java.*`）
2. libGDX（`com.badlogic.*`）
3. 项目内部（`com.hujiugame.qingfeng.*`）

每个组内按字母顺序排列。避免通配符导入（`*`）。

## 7. 工具类模式

```java
public final class ClassName
{
    private ClassName()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 静态常量和方法
}
```

- 类声明为 `public final`
- 私有构造函数使用 Allman 风格
- 体为 `throw new UnsupportedOperationException("Utility class cannot be instantiated")`
- 不能有实例字段

## 8. 异常处理

```java
try
{
    // 操作
}
catch (Exception e)
{
    LogUtils.error(ClassName.class, "methodName 说明", e);
    return false; // 或返回默认值
}
```

- `try`/`catch` 使用 Allman 风格
- 异常变量命名 `e`
- 使用 `LogUtils.error()` 记录日志（三个参数：标签、消息、异常）
- 向调用者返回一个合理的默认/哨兵值，避免静默吞异常

## 9. 日志规范

```java
LogUtils.debug(ClassName.class, "methodName 操作描述");
LogUtils.info(ClassName.class, "methodName 操作描述 (key): " + value);
LogUtils.error(ClassName.class, "methodName 错误描述");
LogUtils.error(ClassName.class, "methodName", e); // 带异常的日志
```

- 第一个参数使用 `ClassName.class`（编译器保证类名与所在类型一致），不得使用过时字符串标签或带 `Imp` 后缀的类名字符串
- 第二个参数描述操作或错误
- 关键数据以 `(key): value` 格式追加

## 10. 节分隔符

长类中使用以下格式分割逻辑段落：

```java
// ===================================================================================================================
// 节标题（可选）
// ===================================================================================================================
```

分隔符总宽度为 100 个 `=` 号。

## 11. 命名规范

| 类型                 | 规范                      | 示例                                    |
|--------------------|-------------------------|---------------------------------------|
| 类/接口               | PascalCase              | `GameHost`, `StateStructure`    |
| 方法                 | camelCase               | `getGameStateName()`, `loadGame()`    |
| 字段/变量              | camelCase               | `gamePathDirectory`, `player`         |
| 常量（`static final`） | UPPER_SNAKE_CASE        | `MENU_MAIN`, `STRING_PARSE_LEVEL_MAP` |
| 枚举常量和值             | UPPER_SNAKE_CASE        | `LOCAL_HOST`, `ROOT`                  |
| 参数                 | camelCase               | `String gamePath`, `int subState`     |
| Getter/Setter      | `getXxx()` / `setXxx()` | `getState()`, `setGamePath()`         |
| 消耗型 getter         | `consumeXxx()`          | `consumeClicked()`（会重置状态的 getter）     |
| 布尔查询               | `isXxx()` / `hasXxx()`  | `isClicked()`, `hasEvent()`           |
| 动作型布尔方法（执行+返回是否成功） | `doXxx()`               | `doInit()`, `doDetectUpdateFinish()`  |

## 12. 注释

- 描述性注释使用中文
- 使用 `//` 单行注释，不使用 `/* */` 块注释
- 关键决策或复杂逻辑处加注释说明"为什么"而非"做了什么"
- 私有方法/内部类使用 `/** */` Javadoc 注释描述功能，私有变量不要求注释
- 公开 API 方法必须有 `/** */` Javadoc 注释，包含 `@param` 和 `@return`（如适用）

## 13. 枚举格式

```java
public enum EnumName
{
    VALUE_A,
    VALUE_B,
    VALUE_C;

    // 方法
}
```

- 类声明使用 Allman 风格
- 枚举常量使用 UPPER_SNAKE，每行一个
- 如果枚举有方法，需要在最后一个常量后加分号
- 纯常量枚举（无方法）不需要尾部分号

## 14. 匿名内部类

匿名内部类同样遵循 Allman 风格大括号：

```java
Gdx.net.sendHttpRequest (request, new Net.HttpResponseListener()
{
    @Override
    public void handleHttpResponse (Net.HttpResponse httpResponse)
    {
        // body
    }

    @Override
    public void failed (Throwable t)
    {
        // body
    }
});
```

## 15. JSON 格式

assets/ 目录下的 JSON 配置文件遵循以下格式：

```json
{
  "key" : "value",
  "object" : {
    "field" : true
  },
  "array" : [
    "item1",
    "item2"
  ]
}
```

- **冒号前后各一个空格**：`"key" : "value"`，不使用 `"key": "value"` 风格
- 缩进使用 4 空格（与 Java 代码一致）
- 末尾不保留逗号
- 文件末尾保留一个空行

此规范适用于 `assets/` 下所有 JSON 配置文件，包括主题配置、语言文件、布局文件等。
