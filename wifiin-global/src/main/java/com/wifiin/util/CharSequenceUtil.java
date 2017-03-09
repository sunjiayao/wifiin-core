package com.wifiin.util;

import com.wifiin.util.regex.RegexUtil;

/**
 * 字符工具
 * @author Running
 *
 */
public class CharSequenceUtil {
    /**
     * 检查content是否包含中文
     * @param content
     * @return
     */
    public static boolean hasChineseCharacter(CharSequence content){
        return Help.isNotEmpty(content) && RegexUtil.matches(content, ".*[\u4e00-\u9fa5]+.*");
    }
}
