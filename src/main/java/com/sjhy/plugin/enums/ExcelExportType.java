/*
 * Copyright © 2024 PCI Technology Group Co.,Ltd. All Rights Reserved.
 * Site: http://www.pcitech.com/
 * Address：PCI Intelligent Building, No.2 Xincen Fourth Road, Tianhe District, Guangzhou，China（Zip code：510653）
 */
package com.sjhy.plugin.enums;

import lombok.Getter;

/**
 * Excel导出类型枚举
 *
 * @author Gensokyo V.L.
 * @version 1.0.0
 * @since 2024/6/11 , Version 1.0.0
 */
@Getter
public enum ExcelExportType {

    REGULAR("默认"), TEMPLATE("模板");

    private final String name;

    ExcelExportType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // Display name in combo box
    }
}
