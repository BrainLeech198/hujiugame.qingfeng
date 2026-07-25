package com.hujiugame.qingfeng.manager;

import com.hujiugame.qingfeng.game.GameInfoManager;
import com.hujiugame.qingfeng.util.system.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextManager
{
    public enum Field
    {
        LANGUAGE("language"),
        GAME("game");

        private final String value;

        Field (String value)
        {
            this.value = value;
        }

        public String getValue ()
        {
            return value;
        }

        public static Field fromValue (String value)
        {
            for (Field field : values())
            {
                if (field.value.equals(value))
                {
                    return field;
                }
            }
            return null;
        }
    }

    private long stateCode;
    private long languageStateCode;
    private long gameInfoStateCode;
    private LanguageManager languageManager = null;
    private GameInfoManager gameInfoManager = null;

    private String startKey = "{";
    private String endKey = "}";
    private char fieldSeparator = '$';
    private char blockSeparator = '#';

    /**
     * 构造方法，初始化状态码为0
     */
    public TextManager ()
    {
        stateCode = 0;
        languageStateCode = 0;
        gameInfoStateCode = 0;
    }

    /**
     * 更新状态码，标记文本管理器状态发生变化
     */
    public void update ()
    {
        stateCode++;
    }

    /**
     * 设置语言管理器并更新状态
     *
     * @param languageManager 语言管理器实例
     */
    public void setLanguageManager (LanguageManager languageManager)
    {
        this.languageManager = languageManager;
        update();
    }

    /**
     * 设置游戏信息管理器并更新状态
     *
     * @param gameInfoManager 游戏信息管理器实例
     */
    public void setGameInfoManager (GameInfoManager gameInfoManager)
    {
        this.gameInfoManager = gameInfoManager;
        update();
    }

    /**
     * 设置变量起始标记并更新状态
     *
     * @param startKey 起始标记字符串（默认"{"）
     */
    public void setStartKey (String startKey)
    {
        this.startKey = startKey;
        update();
    }

    /**
     * 设置变量结束标记并更新状态
     *
     * @param endKey 结束标记字符串（默认"}"）
     */
    public void setEndKey (String endKey)
    {
        this.endKey = endKey;
        update();
    }

    /**
     * 设置域分隔符并更新状态
     *
     * @param fieldSeparator 域分隔符字符（默认'$'）
     */
    public void setFieldSeparator (char fieldSeparator)
    {
        this.fieldSeparator = fieldSeparator;
        update();
    }

    /**
     * 设置块分隔符并更新状态
     *
     * @param blockSeparator 块分隔符字符（默认'#'）
     */
    public void setBlockSeparator (char blockSeparator)
    {
        this.blockSeparator = blockSeparator;
        update();
    }

    // ===================================================================================================================

    /**
     * 获取当前状态码，同时轮询语言管理器和游戏信息管理器的状态变化
     *
     * @return 当前状态码
     */
    public long getStateCode ()
    {
        // 轮询语言
        if (languageManager.getStateCode() != languageStateCode)
        {
            languageStateCode = languageManager.getStateCode();
            update();
        }

        // 轮询游戏信息
        if (gameInfoManager.getStateCode() != gameInfoStateCode)
        {
            gameInfoStateCode = gameInfoManager.getStateCode();
            update();
        }

        return stateCode;
    }

    /**
     * 解析语言文本：从语言管理器中获取指定块和键对应的文本
     *
     * @param block 语言块名称
     * @param key   文本键
     * @return 解析后的文本字符串，解析失败返回错误信息
     */
    private String parseLanguageText (String block, String key)
    {
        try
        {
            return languageManager.getText(block, key);
        }
        catch (Exception e)
        {
            LogUtils.error(TextManager.class, "parseText", e);
            return "Parse language key error (block): " + block + " (key): " + key;
        }
    }

    /**
     * 解析游戏信息文本：从游戏信息管理器中获取指定键对应的信息
     *
     * @param key 游戏信息键
     * @return 解析后的信息文本，解析失败返回错误信息
     */
    private String parseGameInfoText (String key)
    {
        try
        {
            if (gameInfoManager != null)
            {
                Object info = gameInfoManager.getInfo(key);
                return info == null ? "null" : info.toString();
            }
            else
            {
                return "GameInfoMap is null";
            }
        }
        catch (Exception e)
        {
            LogUtils.error(TextManager.class, "parseText", e);
            return " Parse gameInfo key error (key): " + key;
        }
    }

    /**
     * 解析花括号内的变量文本：按域分隔符和块分隔符拆解并获取实际值
     *
     * @param braceText 花括号内的变量文本，格式为 "域$块#键" 或 "域$键"
     * @return 解析后的实际文本，解析失败返回原文本
     */
    private String parseBraceText (String braceText)
    {
        try
        {
            // 分割域
            String[] splitField = braceText.split(Pattern.quote(String.valueOf(fieldSeparator)));

            // 域和主键
            String field;
            String mainKey;
            if (splitField.length == 2)
            {
                field = splitField[0];
                mainKey = splitField[1];
            }
            else
            {
                LogUtils.error(TextManager.class, "parseText 出现错误，不正确的分隔符数量 (braceText): " + braceText);
                return braceText;
            }


            // 分割块
            String[] splitBlock = mainKey.split(Pattern.quote(String.valueOf(blockSeparator)));

            // 块和键
            String block = null;
            String key;
            if (splitBlock.length == 1)
            {
                key = splitBlock[0];
            }
            else if (splitBlock.length == 2)
            {
                block = splitBlock[0];
                key = splitBlock[1];
            }
            else
            {
                LogUtils.error(TextManager.class, "parseText 错误，不正确的分隔符数量 (braceText): " + braceText);
                return braceText;
            }

            // 获取实际值
            Field f = Field.fromValue(field);
            if (f == null)
            {
                return "Field is not exist (field): " + field;
            }

            switch (f)
            {
                case LANGUAGE:
                    return parseLanguageText(block, key);

                case GAME:
                    return parseGameInfoText(key);

                default:
                    return "Field is not exist (field): " + field;
            }
        }
        catch (Exception e)
        {
            LogUtils.error(TextManager.class, "parseText", e);
            return braceText;
        }
    }

    /**
     * 解析文本中的变量标记（如 {language$block#key}），替换为实际内容
     *
     * @param text 包含变量标记的原始文本
     * @return 解析后的文本，所有变量标记已被替换为实际内容
     */
    public String parseText (String text)
    {
        try
        {
            // 语言包为空
            if (languageManager == null)
            {
                return text;
            }

            LogUtils.debug(TextManager.class, "parseText 解析对象 text(String): " + text);
            LogUtils.debug(TextManager.class, "parseText 起始键串 (startKey): " + startKey + "，结束键串 (endKey): " + endKey);
            LogUtils.debug(TextManager.class, "parseText 分隔键 (separator): " + fieldSeparator);

            // 假设 startKey 和 endKey 是类的成员变量（例如通过构造方法或 setter 注入）
            String startKey = this.startKey; // 例如 "{"
            String endKey = this.endKey;     // 例如 "}"

            // 对分隔符中的正则特殊字符进行转义
            String quotedStart = Pattern.quote(startKey);
            String quotedEnd = Pattern.quote(endKey);

            // 正则表达式：匹配 startKey + 任意内容（非贪婪） + endKey，或者匹配不包含 startKey/endKey 的连续文本
            // 注意：这里用 group(1) 捕获 startKey 和 endKey 之间的内容
            String regex = quotedStart + "(.*?)" + quotedEnd + "|(?:(?!" + quotedStart + "|" + quotedEnd + ").)+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            List<String> textList = new ArrayList<>();
            List<Boolean> isBraceList = new ArrayList<>();

            while (matcher.find())
            {
                if (matcher.group(1) != null)
                {
                    // 匹配到 startKey...endKey 区域，group(1) 是内部内容（不含标记）
                    textList.add(matcher.group(1));
                    isBraceList.add(true);
                }
                else
                {
                    // 匹配到普通文本
                    textList.add(matcher.group());
                    isBraceList.add(false);
                }
            }

            // 显示拆分对象结果列表
            LogUtils.debug(TextManager.class, "parseText 初次拆分结果 textList(List): " + textList);
            LogUtils.debug(TextManager.class, "parseText 初次拆分结果 isBraceList(List): " + isBraceList);

            // 遍历文本片段
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < textList.size(); i++)
            {
                if (isBraceList.get(i))
                {
                    // 注意：这里传入的是已经去掉标记的内部内容，parseBraceText 可以直接处理语言键
                    result.append(parseBraceText(textList.get(i)));
                }
                else
                {
                    result.append(textList.get(i));
                }
            }

            LogUtils.debug(TextManager.class, "parseText text: " + text + " -> " + result);
            return result.toString();
        }
        catch (Exception e)
        {
            LogUtils.error(TextManager.class, "parseText", e);
            return text;
        }
    }
}
