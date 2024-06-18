/*
 * Copyright © 2024 PCI Technology Group Co.,Ltd. All Rights Reserved.
 * Site: http://www.pcitech.com/
 * Address：PCI Intelligent Building, No.2 Xincen Fourth Road, Tianhe District, Guangzhou，China（Zip code：510653）
 */
package com.sjhy.plugin.tool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 命名工具测试类
 *
 * @author Gensokyo V.L.
 * @version 1.0.0
 * @since 2024/5/31 , Version 1.0.0
 */
public class NameUtilsTest {

    @Test
    public void getUri() {
        assertEquals(NameUtils.getInstance().getUri("" , "CAR_DETECT_INFO"), "/car/detect/info");
    }

    @Test
    public void split() {
        String input = "Hello,世界! This is a test. 这是一个测试。";

        // Regular expression to match any symbol except English letters and Chinese characters
        String regex = "[^\\p{L}\\p{IsHan}]";

        // Split the input string using the regular expression
        String[] parts = input.split(regex);

        // Print the resulting parts
        for (String part : parts) {
            if (!part.isEmpty()) {
                System.out.println(part);
            }
        }
    }
}
