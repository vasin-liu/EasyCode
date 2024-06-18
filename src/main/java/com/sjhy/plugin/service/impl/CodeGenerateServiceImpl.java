package com.sjhy.plugin.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.ListUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.constant.Const;
import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.enums.ExcelExportType;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.SettingsStorageService;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 12:50
 */
public class CodeGenerateServiceImpl implements CodeGenerateService {
    private static final Logger LOG = Logger.getInstance(CodeGenerateServiceImpl.class);
    /**
     * 项目对象
     */
    private final Project project;
    /**
     * 模型管理
     */
    private final ModuleManager moduleManager;
    /**
     * 表信息服务
     */
    private final TableInfoSettingsService tableInfoService;
    /**
     * 缓存数据工具
     */
    private final CacheDataUtils cacheDataUtils;
    private final FileUtils fileUtils;
    /**
     * 导入包时过滤的包前缀
     */
    private static final String FILTER_PACKAGE_NAME = "java.lang";

    public CodeGenerateServiceImpl(Project project) {
        this.project = project;
        this.moduleManager = ModuleManager.getInstance(project);
        this.tableInfoService = TableInfoSettingsService.getInstance();
        this.cacheDataUtils = CacheDataUtils.getInstance();
        this.fileUtils = FileUtils.getInstance();
    }

    /**
     * 生成
     *
     * @param templates       模板
     * @param generateOptions 生成选项
     */
    @Override
    public void generate(Collection<Template> templates, GenerateOptions generateOptions) {
        // 获取选中表信息
        TableInfo selectedTableInfo;
        List<TableInfo> tableInfoList;
        if (Boolean.TRUE.equals(generateOptions.getEntityModel())) {
            selectedTableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectPsiClass());
            tableInfoList = cacheDataUtils.getPsiClassList()
                    .stream()
                    .map(tableInfoService::getTableInfo)
                    .collect(Collectors.toList());
        } else {
            selectedTableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectDbTable());
            tableInfoList = cacheDataUtils.getDbTableList()
                    .stream()
                    .map(tableInfoService::getTableInfo)
                    .collect(Collectors.toList());
        }
        // 校验选中表的保存路径是否正确
        if (StringUtils.isEmpty(selectedTableInfo.getSavePath())) {
            if (selectedTableInfo.getObj() != null) {
                Messages.showInfoMessage(selectedTableInfo.getObj().getName() + "表配置信息不正确，请尝试重新配置", Const.TITLE_INFO);
            } else if (selectedTableInfo.getPsiClassObj() != null) {
                PsiClass psiClassObj = (PsiClass) selectedTableInfo.getPsiClassObj();
                Messages.showInfoMessage(psiClassObj.getName() + "类配置信息不正确，请尝试重新配置", Const.TITLE_INFO);
            } else {
                Messages.showInfoMessage("配置信息不正确，请尝试重新配置", Const.TITLE_INFO);
            }
            return;
        }
        // 将未配置的表进行配置覆盖
        TableInfo finalSelectedTableInfo = selectedTableInfo;
        tableInfoList.forEach(tableInfo -> {
            if (StringUtils.isEmpty(tableInfo.getSavePath())) {
                tableInfo.setSaveModelName(finalSelectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(finalSelectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(finalSelectedTableInfo.getSavePath());
                tableInfo.setPreName(finalSelectedTableInfo.getPreName());
                tableInfoService.saveTableInfo(tableInfo);
            }
        });
        // 如果使用统一配置，直接全部覆盖
        if (Boolean.TRUE.equals(generateOptions.getUnifiedConfig())) {
            tableInfoList.forEach(tableInfo -> {
                tableInfo.setSaveModelName(finalSelectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(finalSelectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(finalSelectedTableInfo.getSavePath());
                tableInfo.setPreName(finalSelectedTableInfo.getPreName());
            });
        }

        // 生成代码
        generate(templates, tableInfoList, generateOptions, null);
    }

    /**
     * 生成代码，并自动保存到对应位置
     *
     * @param templates       模板
     * @param tableInfoList   表信息对象
     * @param generateOptions 生成配置
     * @param otherParam      其他参数
     */
    public void generate(Collection<Template> templates, Collection<TableInfo> tableInfoList,
                         GenerateOptions generateOptions, Map<String, Object> otherParam) {
        if (CollectionUtil.isEmpty(templates) || CollectionUtil.isEmpty(tableInfoList)) {
            return;
        }
        // 处理模板，注入全局变量（克隆一份，防止篡改）
        templates = CloneUtils.cloneByJson(templates, new TypeReference<ArrayList<Template>>() {
        });
        TemplateUtils.addGlobalConfig(templates);
        // 生成代码
        for (TableInfo tableInfo : tableInfoList) {
            // 表名去除前缀
            if (!StringUtils.isEmpty(tableInfo.getPreName()) && tableInfo.getObj().getName().startsWith(tableInfo.getPreName())) {
                String newName = tableInfo.getObj().getName().substring(tableInfo.getPreName().length());
                tableInfo.setName(NameUtils.getInstance().getClassName(newName));
            }
            // 构建参数
            Map<String, Object> param = getDefaultParam();
            // 其他参数
            if (otherParam != null) {
                param.putAll(otherParam);
            }
            // 所有表信息对象
            param.put("tableInfoList", tableInfoList);
            // 表信息对象
            param.put("tableInfo", tableInfo);
            // 设置模型路径与导包列表
            setModulePathAndImportList(param, tableInfo);
            // 设置额外代码生成服务
            param.put("generateService", new ExtraCodeGenerateUtils(this, tableInfo, generateOptions));
            for (Template template : templates) {
                Callback callback = new Callback();
                callback.setWriteFile(true);
                callback.setReformat(generateOptions.getReFormat());
                callback.setExportExcelWithTemplate(ExcelExportType.TEMPLATE.equals(generateOptions.getExcelExportType()));
                callback.setTruncateComment(generateOptions.getTruncateComment());
                // 默认名称
                callback.setFileName(tableInfo.getName() + "Default.java");
                // 默认路径
                callback.setSavePath(tableInfo.getSavePath());
                // 设置回调对象
                param.put("callback", callback);
                // 开始生成
                String code = VelocityUtils.generate(template.getCode(), param);
                // 设置一个默认保存路径与默认文件名
                String path = callback.getSavePath();
                path = path.replace("\\", "/");
                // 针对相对路径进行处理
                if (path.startsWith(".")) {
                    path = project.getBasePath() + path.substring(1);
                }
                callback.setSavePath(path);
                new SaveFile(project, code, callback, generateOptions).write();
            }
            // 生成Excel模板
            genExcelTemplate(project, tableInfo, generateOptions);
        }
    }

    /**
     * 生成代码
     *
     * @param template  模板
     * @param tableInfo 表信息对象
     * @return 生成好的代码
     */
    @Override
    public String generate(Template template, TableInfo tableInfo) {
        // 获取默认参数
        Map<String, Object> param = getDefaultParam();
        // 表信息对象，进行克隆，防止篡改
        param.put("tableInfo", tableInfo);
        // 设置模型路径与导包列表
        setModulePathAndImportList(param, tableInfo);
        // 处理模板，注入全局变量
        TemplateUtils.addGlobalConfig(template);
        return VelocityUtils.generate(template.getCode(), param);
    }

    /**
     * 设置模型路径与导包列表
     *
     * @param param     参数
     * @param tableInfo 表信息对象
     */
    private void setModulePathAndImportList(Map<String, Object> param, TableInfo tableInfo) {
        Module module = null;
        if (!StringUtils.isEmpty(tableInfo.getSaveModelName())) {
            module = this.moduleManager.findModuleByName(tableInfo.getSaveModelName());
        }
        if (module != null) {
            // 设置modulePath
            param.put("modulePath", ModuleUtils.getModuleDir(module).getPath());
        }
        // 设置要导入的包
        param.put("importList", getImportList(tableInfo));
    }

    /**
     * 获取默认参数
     *
     * @return 参数
     */
    private Map<String, Object> getDefaultParam() {
        // 系统设置
        SettingsStorageDTO settings = SettingsStorageService.getSettingsStorage();
        Map<String, Object> param = new HashMap<>(20);
        // 作者
        param.put("author", settings.getAuthor());
        param.put("year", LocalDate.now().getYear());
        param.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        param.put("version", settings.getFileVersion());
        //工具类
        param.put("tool", GlobalTool.getInstance());
        param.put("time", TimeUtils.getInstance());
        // 项目路径
        param.put("projectPath", project.getBasePath());
        // Database数据库工具
        param.put("dbUtil", ReflectionUtil.newInstance(DbUtil.class));
        param.put("dasUtil", ReflectionUtil.newInstance(DasUtil.class));
        return param;
    }

    /**
     * 获取导入列表
     *
     * @param tableInfo 表信息对象
     * @return 导入列表
     */
    private Set<String> getImportList(TableInfo tableInfo) {
        // 创建一个自带排序的集合
        Set<String> result = new TreeSet<>();
        tableInfo.getFullColumn().forEach(columnInfo -> {
            if (!columnInfo.getType().startsWith(FILTER_PACKAGE_NAME)) {
                String type = NameUtils.getInstance().getClsFullNameRemoveGeneric(columnInfo.getType());
                result.add(type);
            }
        });
        return result;
    }

    private void genExcelTemplate(Project project, TableInfo table, GenerateOptions generateOptions) {
        if (!ExcelExportType.TEMPLATE.equals(generateOptions.getExcelExportType())) {
            return;
        }
        Module module = null;
        if (!StringUtils.isEmpty(table.getSaveModelName())) {
            module = this.moduleManager.findModuleByName(table.getSaveModelName());
        }
        VirtualFile moduleDir = null;
        if (module != null) {
            // 设置modulePath
            moduleDir = ModuleUtils.getModuleDir(module);
        }
        
        // 无法获取到模块基本目录，可能是Default项目，直接返回非项目文件
        if (moduleDir == null) {
            LOG.error("无法获取到模块基本目录，请检查模块目录选择是否正确！");
            return;
        }
        //生成Excel模板文件
        // 资源路径
        String resourcePath = "/src/main/resources/static/template/excel";
        String excelFileName = table.getComment() + "导出模板.xlsx";
        VirtualFile saveDir = fileUtils.createChildDirectory(project, moduleDir, resourcePath);
        VirtualFile psiFile = saveDir.findChild(excelFileName);
        if (Objects.isNull(psiFile)) {
            psiFile = fileUtils.createChildFile(project, saveDir, excelFileName);
        }
        List<List<String>> head = new ArrayList<>();
        List<List<Object>> list = ListUtils.newArrayList();
        head.add(Collections.singletonList("序号"));
        List<Object> data = ListUtils.newArrayList();
        data.add("{data1.idx}");
        for (ColumnInfo column : table.getFullColumn()) {
            String comment = column.getComment();
            if (Boolean.TRUE.equals(generateOptions.getTruncateComment())) {
                comment = StringUtils.getFirstFragment(column.getComment());
            }
            head.add(Collections.singletonList(comment));
            data.add("{data1." + column.getName() + "}");
        }

        list.add(data);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            EasyExcelFactory.write(baos)
                    .head(head)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("Sheet1")
                    .doWrite(list);
            fileUtils.writeFileContent(project, psiFile, excelFileName, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
