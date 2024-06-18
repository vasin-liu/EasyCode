package com.sjhy.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.sjhy.plugin.constant.Const;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.tool.JSON;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 生成默认配置测试
 * 由于resources目录下的配置文件不便与遍历，利用测试用例提前遍历并生成配置文件。运行时再直接加载配置
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 11:11
 */
public class GenerateDefaultConfigTest {

    private final String encoding = "UTF-8";


    @Test
    public void generate() throws IOException {
        SettingsStorageDTO settingsStorage = new SettingsStorageDTO();
        settingsStorage.setAuthor(Const.AUTHOR);
        settingsStorage.setVersion(Const.VERSION);
        settingsStorage.setFileVersion(Const.FILE_VERSION);
        settingsStorage.setUserSecure("");
        File templateDir = new File(GenerateDefaultConfigTest.class.getResource("/template").getFile());
        loadTemplate(settingsStorage, templateDir);

        File globalConfigDir = new File(GenerateDefaultConfigTest.class.getResource("/globalConfig").getFile());
        loadGlobalConfig(settingsStorage, globalConfigDir);

        File typeMapperDir = new File(GenerateDefaultConfigTest.class.getResource("/typeMapper").getFile());
        loadTypeMapper(settingsStorage, typeMapperDir);

        File columnConfigDir = new File(GenerateDefaultConfigTest.class.getResource("/columnConfig").getFile());
        loadColumnConfig(settingsStorage, columnConfigDir);

        String json = JSON.toJson(settingsStorage);
        // 所有的换行符号均改为\n
        // 1.windows处理
        json = json.replace("\\r\\n" , "\\n");
        // 2.mac处理
        json = json.replace("\\r" , "\\n");
        FileUtil.writeToFile(new File(GenerateDefaultConfigTest.class.getResource("/").getFile().replace("out" , "src").replace("test" , "main").replace("classes" , "resources") + "defaultConfig.json"), json);
    }

    private void loadTemplate(SettingsStorageDTO settingsStorage, File root) throws IOException {
        settingsStorage.setCurrTemplateGroupName(Const.DEFAULT_GROUP_NAME);
        settingsStorage.setTemplateGroupMap(new HashMap<>(root.listFiles().length));
        for (File dir : root.listFiles()) {
            TemplateGroup templateGroup = new TemplateGroup();
            templateGroup.setName(dir.getName());
            templateGroup.setElementList(new ArrayList<>());
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    Template template = new Template();
                    template.setName(file.getName());
                    template.setCode(FileUtilRt.loadFile(file, encoding));
                    templateGroup.getElementList().add(template);
                }
            }
            settingsStorage.getTemplateGroupMap().put(templateGroup.getName(), templateGroup);
        }
    }

    private void loadGlobalConfig(SettingsStorageDTO settingsStorage, File root) throws IOException {
        settingsStorage.setCurrGlobalConfigGroupName(Const.DEFAULT_GROUP_NAME);
        settingsStorage.setGlobalConfigGroupMap(new HashMap<>(root.listFiles().length));
        for (File dir : root.listFiles()) {
            GlobalConfigGroup globalConfigGroup = new GlobalConfigGroup();
            globalConfigGroup.setName(dir.getName());
            globalConfigGroup.setElementList(new ArrayList<>());
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    GlobalConfig globalConfig = new GlobalConfig();
                    globalConfig.setName(file.getName());
                    globalConfig.setValue(FileUtilRt.loadFile(file, encoding));
                    globalConfigGroup.getElementList().add(globalConfig);
                }
            }
            settingsStorage.getGlobalConfigGroupMap().put(globalConfigGroup.getName(), globalConfigGroup);
        }
    }

    private void loadTypeMapper(SettingsStorageDTO settingsStorage, File root) throws IOException {
        settingsStorage.setCurrTypeMapperGroupName(Const.DEFAULT_GROUP_NAME);
        settingsStorage.setTypeMapperGroupMap(new HashMap<>(root.listFiles().length));
        for (File file : root.listFiles()) {
            TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
            typeMapperGroup.setName(file.getName().replace(".json" , ""));
            if (file.isFile()) {
                String json = FileUtilRt.loadFile(file, encoding);
                typeMapperGroup.setElementList(JSON.parse(json, new TypeReference<>() {
                }));
            }
            settingsStorage.getTypeMapperGroupMap().put(typeMapperGroup.getName(), typeMapperGroup);
        }
    }

    private void loadColumnConfig(SettingsStorageDTO settingsStorage, File root) throws IOException {
        settingsStorage.setCurrColumnConfigGroupName(Const.DEFAULT_GROUP_NAME);
        settingsStorage.setColumnConfigGroupMap(new HashMap<>(root.listFiles().length));
        for (File file : root.listFiles()) {
            ColumnConfigGroup columnConfigGroup = new ColumnConfigGroup();
            columnConfigGroup.setName(file.getName().replace(".json" , ""));
            if (file.isFile()) {
                String json = FileUtilRt.loadFile(file, encoding);
                columnConfigGroup.setElementList(JSON.parse(json, new TypeReference<>() {
                }));
            }
            settingsStorage.getColumnConfigGroupMap().put(columnConfigGroup.getName(), columnConfigGroup);
        }
    }

}
