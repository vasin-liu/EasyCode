/*
 * Copyright © 2024 PCI Technology Group Co.,Ltd. All Rights Reserved.
 * Site: http://www.pcitech.com/
 * Address：PCI Intelligent Building, No.2 Xincen Fourth Road, Tianhe District, Guangzhou，China（Zip code：510653）
 */
package com.sjhy.plugin.tool;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * 正则测试类
 *
 * @author Gensokyo V.L.
 * @version 1.0.0
 * @since 2024/6/13 , Version 1.0.0
 */
public class RegexTests {

    @Test
    public void case1() {
        Assert.assertTrue(Pattern.compile("datetime\\d+|datetime(\\(\\d+\\,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("DATETIME(36,6)").matches());
        Assert.assertTrue(Pattern.compile("datetime\\d+|datetime(\\(\\d+\\,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("DATETIME64").matches());
        Assert.assertTrue(Pattern.compile("datetime\\d+|datetime(\\(\\d+\\,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("DATETIME").matches());

        Assert.assertTrue(Pattern.compile("(uint|int)16", Pattern.CASE_INSENSITIVE).matcher("Int16").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)16", Pattern.CASE_INSENSITIVE).matcher("UInt16").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("Int16").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("Int32").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("Int64").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("UInt16").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("UInt32").matches());
        Assert.assertTrue(Pattern.compile("(uint|int)(16|32|64)?", Pattern.CASE_INSENSITIVE).matcher("UInt64").matches());

        Assert.assertTrue(Pattern.compile("float\\d+?", Pattern.CASE_INSENSITIVE).matcher("Float32").matches());
        Assert.assertTrue(Pattern.compile("float\\d+?", Pattern.CASE_INSENSITIVE).matcher("Float64").matches());

        Assert.assertTrue(Pattern.compile("(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("String").matches());
        Assert.assertTrue(Pattern.compile("(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("FixedString(64)").matches());

        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*", Pattern.CASE_INSENSITIVE).matcher("text(max)").matches());
        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*", Pattern.CASE_INSENSITIVE).matcher("longtext").matches());
        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*|(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("text(max)").matches());
        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*|(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("String").matches());
        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*|(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("FixedString(64)").matches());
        Assert.assertTrue(Pattern.compile("(tiny|medium|long)*text(\\(max\\))*|(String|FixedString)(\\(\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("longtext").matches());

        Assert.assertTrue(Pattern.compile("Decimal|Decimal\\d+\\(\\d+\\)?|decimal(\\(\\d+,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("Decimal").matches());
        Assert.assertTrue(Pattern.compile("Decimal|Decimal\\d+\\(\\d+\\)?|decimal(\\(\\d+,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("Decimal32(2)").matches());
        Assert.assertTrue(Pattern.compile("Decimal|Decimal\\d+\\(\\d+\\)?|decimal(\\(\\d+,\\d+\\))?", Pattern.CASE_INSENSITIVE).matcher("Decimal(15,2)").matches());
    }
}
