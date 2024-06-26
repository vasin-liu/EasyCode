package com.sjhy.plugin.tool;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 添加字符串工具类，为了兼容JB的各种产品，尽量不要用第三方工具包
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/07 11:52
 */
@SuppressWarnings("WeakerAccess")
public class StringUtils {

    /**
     * 首字母处理方法
     */
    private static final BiFunction<String, Function<Integer, Integer>, String> FIRST_CHAR_HANDLER_FUN = (str, firstCharFun) -> {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = firstCharFun.apply(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        // cannot be longer than the char array
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        // copy the first codepoint
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            // copy the remaining ones
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    };

    /**
     * 判断是空字符串
     *
     * @param cs 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * 首字母大写方法
     *
     * @param str 字符串
     * @return 首字母大写结果
     */
    public static String capitalize(final String str) {
        return FIRST_CHAR_HANDLER_FUN.apply(str, Character::toTitleCase);
    }

    /**
     * 首字母小写方法
     *
     * @param str 字符串
     * @return 首字母小写结果
     */
    public static String uncapitalize(final String str) {
        return FIRST_CHAR_HANDLER_FUN.apply(str, Character::toLowerCase);
    }

    public static String getFirstFragment(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String regex = "[^\\p{L}\\p{IsHan}]";
        return str.split(regex)[0];
    }
}
