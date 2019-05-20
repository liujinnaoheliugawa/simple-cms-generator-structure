package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure;

import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation.*;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.exception.PojoException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 核心类，自动生成插件，自动生成 Pojo 对应的 Service 层，Controller 层以及 HTML 页面和 JS 文件
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Mojo(name = "simple-cms-generator", defaultPhase = LifecyclePhase.COMPILE)
public class SimpleCmsGeneratorPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter
    private String projectPacakge;

    @Parameter
    private String srcPath;

    @Parameter
    private String classesPath;

    @Parameter
    private String resorucePath;

    private static List<String> sysFields = new ArrayList<>(Arrays.asList("adt", "aUserId", "cdt", "cUserId", "udt", "uUserId", "serialVersionUID", "status", "remarks"));

    @Parameter
    private List<String> selectList;

    @Parameter
    private List<String> jumpList;

    // class类的集合
    private Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    @Parameter
    private String classPath;


    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static String semicolon_ln = ";\r\n";
    private static String ln = "\r\n";


    @Override
    public void execute() {
        System.out.println("CMS 自动生成 插件");
        List<Class> generatorClasses = new ArrayList<>();
        URL[] urls = new URL[1];
        try {
            urls[0] = new URL("file:" + classPath);
        } catch (MalformedURLException e) {
            System.out.println("Url part is already wrong");
        }
        URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        String projectDaoPackage = projectPacakge + ".dao";
        String projectServicePackage = projectPacakge + ".service";
        String projectServiceImplPackage = projectPacakge + ".service.impl";
        String projectControllerPackage = projectPacakge + ".controller";
        String serviceSrcFolder = srcPath + "service/";
        String serviceImplSrcFolder = srcPath + "service/impl/";
        String controllerSrcFolder = srcPath + "controller/";
        String templateSrcFolder = resorucePath + "templates/";
        String staticSrcFolder = resorucePath + "static/";
        StringBuffer logImportBuffer = new StringBuffer()
                .append("import org.slf4j.Logger" + semicolon_ln)
                .append("import org.slf4j.LoggerFactory" + semicolon_ln);
        StringBuffer springServiceImportBuffer = new StringBuffer()
                .append("import org.springframework.beans.factory.annotation.Autowired" + semicolon_ln)
                .append("import org.springframework.stereotype.Service" + semicolon_ln);
        StringBuffer springControllerImportBuffer = new StringBuffer()
                .append("import org.springframework.beans.factory.annotation.Autowired" + semicolon_ln)
                .append("import org.springframework.stereotype.Controller" + semicolon_ln)
                .append("import org.springframework.web.bind.annotation.PathVariable" + semicolon_ln)
                .append("import org.springframework.web.bind.annotation.RequestMapping" + semicolon_ln)
                .append("import org.springframework.web.bind.annotation.ResponseBody" + semicolon_ln);
        StringBuffer servletRequestImportBuffer = new StringBuffer()
                .append("import javax.servlet.http.HttpServletRequest" + semicolon_ln);
        String projectPojoClassPath = project.getArtifact().getFile().getAbsolutePath() + "/" + projectPacakge.replaceAll("\\.", "/") + "/pojo";
        String pojoFilePath;
        File file = new File(projectPojoClassPath);
        if (file.isDirectory()) {
            File[] pojoFiles = file.listFiles();
            for (File pojoFile : pojoFiles) {
                pojoFilePath = pojoFile.getPath();
                Integer startIndex = pojoFilePath.indexOf("/pojo") + 6;
                String pojoName = pojoFilePath.substring(startIndex, pojoFilePath.lastIndexOf("."));
                if (pojoFilePath.indexOf(".class") > 0 && pojoName.indexOf("Example") < 0 && pojoName.indexOf("$") < 0) {
                    String pojoFullName = projectPacakge + ".pojo." + pojoName;
                    try {
                        Class pojoClass = urlClassLoader.loadClass(pojoFullName);
                        if (selectList == null || (selectList != null && selectList.contains(pojoName))) {
                            generatorClasses.add(pojoClass);
                        }
                        if (jumpList != null && jumpList.contains(pojoName)) {
                            generatorClasses.remove(pojoClass);
                        }
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
        generatorClasses.forEach(k -> {
            generateServiceFiles(k, projectServicePackage, projectServiceImplPackage, serviceSrcFolder, projectDaoPackage, serviceImplSrcFolder,  logImportBuffer, springServiceImportBuffer);
            generateControllerFile(k, projectControllerPackage, projectServicePackage, controllerSrcFolder, logImportBuffer, springControllerImportBuffer, servletRequestImportBuffer);
            generateHtmlFiles(k, templateSrcFolder);
            generateJsFiles(k, staticSrcFolder);
        });
    }

    private static void generateServiceFiles(Class pojoClass, String projectServicePackage, String projectServiceImplPackage, String serviceSrcFolder, String projectDaoPackage, String serviceImplSrcFolder, StringBuffer logImportBuffer, StringBuffer springServiceImportBuffer) {
        generateServiceFile(pojoClass, projectServicePackage, serviceSrcFolder);
        generateServiceImplFile(pojoClass, projectServicePackage, projectServiceImplPackage, projectDaoPackage, serviceImplSrcFolder, logImportBuffer, springServiceImportBuffer);
    }

    private static void generateServiceFile(Class pojoClass, String projectServicePackage, String serviceSrcFolder) {
        StringBuffer src = new StringBuffer();
        src.append("package " + projectServicePackage + semicolon_ln + ln);
        src.append("import " + pojoClass.getName() + semicolon_ln);
        src.append("import " + pojoClass.getName() + "Example" + semicolon_ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.CmsTemplateService" + semicolon_ln + ln);
        src.append("/**" + ln);
        src.append(" * @Description: Automatically Generated Service File" + ln);
        src.append(" * @Author: LiugawaHeLiuJinNao" + ln);
        src.append(" * @Date: " + sdf.format(new Date()) + ln);
        src.append(" */" + ln);
        src.append("public interface " + pojoClass.getSimpleName() + "Service extends CmsTemplateService<" + pojoClass.getSimpleName() + ", " + pojoClass.getSimpleName() + "Example, " + pojoClass.getSimpleName() + "Example.Criteria> {" + ln + ln);
        src.append("}");
        generateFile(serviceSrcFolder + pojoClass.getSimpleName() + "Service.java", src);
    }

    private static void generateServiceImplFile(Class pojoClass, String projectServicePackage, String projectServiceImplPackage, String projectDaoPackage, String serviceImplSrcFolder, StringBuffer logImportBuffer, StringBuffer springServiceImportBuffer) {
        StringBuffer src = new StringBuffer();
        String pojo = pojoClass.getSimpleName();
        String mapper = pojoClass.getSimpleName() + "Mapper";
        String lowerMapper = getLowerName(mapper);
        String example = pojoClass.getSimpleName() + "Example";
        String exampleCriteria = example + ".Criteria";
        String service = pojoClass.getSimpleName() + "Service";
        String serviceImpl = service + "Impl";
        src.append("package " + projectServiceImplPackage + semicolon_ln + ln);
        src.append("import " + projectDaoPackage + "." + mapper + semicolon_ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.dao.CmsTemplateMapper" + semicolon_ln);
        src.append("import " + pojoClass.getName() + semicolon_ln);
        src.append("import " + pojoClass.getName() + "Example" + semicolon_ln);
        src.append("import " + projectServicePackage + "." + service +semicolon_ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.impl.CmsTemplateServiceImpl" + semicolon_ln);
        src.append(logImportBuffer);
        src.append(springServiceImportBuffer + ln);
        src.append("/**" + ln);
        src.append(" * @Description: Automatically Generated Service Implementation File" + ln);
        src.append(" * @Author: LiugawaHeLiuJinNao" + ln);
        src.append(" * @Date: " + sdf.format(new Date()) + ln);
        src.append(" */" + ln);
        src.append("@Service" + ln);
        src.append("public class " + serviceImpl + " extends CmsTemplateServiceImpl<" + pojo + ", " + example +  ", " + exampleCriteria + "> implements " + service +  " {" + ln + ln);
        src.append("\tprivate final " + mapper + " " + lowerMapper + semicolon_ln + ln);
        src.append("\tprivate static final Logger LOGGER = LoggerFactory.getLogger(" + serviceImpl + ".class)" + semicolon_ln + ln);
        src.append("\t@Autowired" + ln);
        src.append("\tpublic " + serviceImpl +  "(" + mapper + " " + lowerMapper + ") {" + ln);
        src.append("\t\tthis." + lowerMapper + " = " + lowerMapper + ";" + ln);
        src.append("\t}" + ln + ln);
        src.append("\t@Override" + ln);
        src.append("\tpublic " + example + " initSearchCondition(String defaultOrder, " + pojo + " model) {" +ln);
        src.append("\t\t" + example + " exception = new " + example + "()" + semicolon_ln);
        src.append("\t\t" + "return exception" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@Override" + ln);
        src.append("\tpublic CmsTemplateMapper<" + pojo + ", " + example + "> getMapper() {" + ln);
        src.append("\t\treturn " + lowerMapper + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@Override");
        src.append("\tpublic Logger getLogger() {" + ln);
        src.append("\t\treturn LOGGER" + semicolon_ln);
        src.append("\t}" + ln);
        src.append("}");
        generateFile(serviceImplSrcFolder + pojoClass.getSimpleName() + "ServiceImpl.java", src);
    }

    private static void generateControllerFile(Class pojoClass, String projectControllerPackage, String projectServicePackage, String controllerSrcFolder, StringBuffer logImportBuffer, StringBuffer springControllerImportBuffer, StringBuffer servletRequestImportBuffer) {
        StringBuffer src = new StringBuffer();
        String pojo = pojoClass.getSimpleName();
        String service = pojoClass.getSimpleName() + "Service";
        String lowerService = getLowerName(service);
        String controller = pojoClass.getSimpleName() + "Controller";
        String levelNames = getLevelNamesSrc(pojo);
        String pojoId = "";
        String pojoDes = ((Pojo)pojoClass.getAnnotation(Pojo.class)).value();
        for (Field field : pojoClass.getDeclaredFields()) {
            if (field.getAnnotation(PojoId.class) != null) {
                pojoId = field.getName();
                break;
            }
        }
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        String pageFolder = fstLevelName + "/" + sndLevelName + ((trdLevelName != null && !trdLevelName.isEmpty()) ? "/" + trdLevelName : "");
        String listPageCustom = pageFolder + "/list";
        String addPageCustom = pageFolder + "/add";
        String editPageCustom = pageFolder + "/edit";
        String auditPageCustom = pageFolder + "/audit";
        src.append("package " + projectControllerPackage + semicolon_ln + ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.controller.CmsTemplateController" + semicolon_ln);
        src.append("import " + pojoClass.getName() + semicolon_ln);
        src.append("import " + projectServicePackage + "." + service  + semicolon_ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.CmsTemplateService" + semicolon_ln);
        src.append("import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.utils.ResultUtils" + semicolon_ln);
        src.append(logImportBuffer);
        src.append(springControllerImportBuffer + ln);
        src.append(servletRequestImportBuffer + ln);
        src.append("/**" + ln);
        src.append(" * @Description: Automatically Generated Controller File" + ln);
        src.append(" * @Author: LiugawaHeLiuJinNao" + ln);
        src.append(" * @Date: " + sdf.format(new Date()) + ln);
        src.append(" */" + ln);
        src.append("@Controller" + ln);
        src.append("@RequestMapping(\"" + levelNames + "\")" + ln);
        src.append("public class " + controller + " extends CmsTemplateController<" + pojo + "> {" + ln + ln);
        src.append("\tprivate final " + service + " " + lowerService + semicolon_ln + ln);
        src.append("\tprivate static final Logger LOGGER = LoggerFactory.getLogger(" + controller + ".class)" + semicolon_ln + ln);
        src.append("\t@Autowired" + ln);
        src.append("\tpublic " + controller + "(" + service + " " + lowerService + ") {" + ln);
        src.append("\t\tthis." + lowerService + " = " + lowerService + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/listPage\")" + ln);
        src.append("\tpublic String listPage() {" + ln);
        src.append("\t\treturn super.getListPage(\"" + listPageCustom + "\")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/getList\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils getList(Integer page, Integer limit) {" + ln);
        src.append("\t\tResultUtils result = null" + semicolon_ln);
        src.append("\t\ttry {" + ln);
        src.append("\t\t\tresult = super.getList(\"" + convertToColumnId(pojoId) + "\", page, limit)" + semicolon_ln);
        src.append("\t\t} catch (InstantiationException e) {" + ln + ln);
        src.append("\t\t} catch (IllegalAccessException e) {" + ln + ln);
        src.append("\t\t}" + ln);
        src.append("\t\treturn result" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/addPage\")" + ln);
        src.append("\tpublic String addPage() {" + ln);
        src.append("\t\treturn super.getAddPage(\"" + addPageCustom + "\")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/insert\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils insert(" + pojo + " model) {" + ln);
        src.append("\t\treturn super.insert(model)" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/editPage/{" + pojoId + "}\")" + ln);
        src.append("\tpublic String editPage(HttpServletRequest request, @PathVariable(\"" + pojoId + "\") Integer " + pojoId + ") {"+ ln);
        src.append("\t\treturn super.getEditPage(request, " + pojoId + ", \"" + editPageCustom + "\")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/update\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils update(" + pojo + " model) {" + ln);
        src.append("\t\treturn super.update(model)" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/auditPage/{" + pojoId + "}\")" + ln);
        src.append("\tpublic String auditPage(HttpServletRequest request, @PathVariable(\"" + pojoId + "\") Integer " + pojoId + ") {" + ln);
        src.append("\t\treturn super.getAuditPage(request, " + pojoId + ", \"" + auditPageCustom + "\")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/audit\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils audit(" + pojo + " model) {" + ln);
        src.append("\t\treturn super.audit(model)" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/delete/{" + pojoId + "}\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils delete(@PathVariable(\"" + pojoId + "\") Integer " + pojoId + ") {" + ln);
        src.append("\t\treturn super.delete(" + pojoId + ")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@RequestMapping(\"/batchDelete/{" + pojoId + "s}\")" + ln);
        src.append("\t@ResponseBody" + ln);
        src.append("\tpublic ResultUtils batchDelete(@PathVariable(\"" + pojoId + "s\") String " + pojoId + "s) {" + ln);
        src.append("\t\treturn getService().batchDelete(" + pojoId + "s) ? ResultUtils.ok(\"刪除成功！\") : ResultUtils.error(\"系統錯誤！\")" + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@Override");
        src.append("\tpublic CmsTemplateService getService() {" + ln);
        src.append("\t\treturn " + lowerService + semicolon_ln);
        src.append("\t}" + ln + ln);
        src.append("\t@Override" + ln);
        src.append("\tpublic Logger getLogger() {" + ln);
        src.append("\t\treturn LOGGER" + semicolon_ln);
        src.append("\t}" + ln);
        src.append("}");
        generateFile(controllerSrcFolder + pojoClass.getSimpleName() + "Controller.java", src);
    }

    private static void generateHtmlFiles(Class pojoClass, String templateSrcFolder) {
        try {
            generateListHtml(pojoClass, templateSrcFolder);
            generateAddHtml(pojoClass, templateSrcFolder);
            generateEditHtml(pojoClass, templateSrcFolder);
            generateAuditHtml(pojoClass, templateSrcFolder);
        } catch (PojoException e) {
            
        }
    }

    private static void generateListHtml(Class pojoClass, String templateSrcFolder) {
        StringBuffer src = new StringBuffer();
        String pojo = pojoClass.getSimpleName();
        String levelNames = getLevelNamesSrc(pojo);
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        String tableId = "TABLE-" + fstLevelName.toUpperCase() + "-" + sndLevelName.toUpperCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toUpperCase() : "");
        String toolbarId = "TEST-toolbar-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String btnClass = "TEST-btn-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        src.append("<!DOCTYPE html>" + ln);
        src.append("<html xmlns:th='http://www.thymeleaf.org' xmlns:shiro='http://www.pollix.at/thymeleaf/shiro' >" + ln);
        src.append("<head>" + ln);
        src.append("<meta charset='utf-8'>" + ln);
        src.append("<title>[[#{common.site.name}]]</title>" + ln);
        src.append("<meta name='renderer' content='webkit'>" + ln);
        src.append("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>" + ln);
        src.append("<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0'>" + ln);
        src.append("<link rel='stylesheet' href='/layuiadmin/layui/css/layui.css' media='all'>" + ln);
        src.append("<link rel='stylesheet' href='/layuiadmin/style/admin.css' media='all'>" + ln);
        src.append("<link rel='stylesheet' href='/css/zTree/zTreeStyle/zTreeStyle.css' media='all' type='text/css' />" + ln);
        src.append("<script type='text/javascript' src='/js/zTree/jquery-1.4.4.min.js'></script>" + ln);
        src.append("<script type='text/javascript' src='/js/zTree/jquery.ztree.all.js'></script>" + ln);
        src.append("</head>" + ln);
        src.append("<body>" + ln);
        src.append("\t<div class='layui-fluid'>" + ln);
        src.append("\t\t<input type='hidden' name='language' id='language' th:value='${language}' />" + ln);
        src.append("\t\t<div class='layui-card'>" + ln);
        src.append("\t\t\t<div class='layui-card-body'>" + ln);
        src.append("\t\t\t\t<div style='padding-bottom: 10px;'>" + ln);
        src.append("\t\t\t\t\t<button class='layui-btn " + btnClass + "' data-type='add'>[[#{common.add}]]</button>" + ln);
        src.append("\t\t\t\t\t<button class='layui-btn " + btnClass + "' data-type='batchdel'>[[#{common.batchdelete}]]</button>" + ln);
        src.append("\t\t\t\t</div>" + ln + ln);
        src.append("\t\t\t\t<table id='" + tableId + "' lay-filter='" + tableId + "'></table>" + ln);
        src.append("\t\t\t\t<script type='text/html' id='" + toolbarId + "'>" + ln);
        src.append("\t\t\t\t\t<a class='layui-btn layui-btn-normal layui-btn-xs' lay-event='audit'><i class='layui-icon layui-icon-set-fill'></i>[[#{common.audit}]]</a>" + ln);
        src.append("\t\t\t\t\t<a class='layui-btn layui-btn-normal layui-btn-xs' lay-event='edit'><i class='layui-icon layui-icon-edit'></i>[[#{common.edit}]]</a>" + ln);
        src.append("\t\t\t\t\t<a class='layui-btn layui-btn-danger layui-btn-xs' lay-event='del'><i class='layui-icon layui-icon-delete'></i>[[#{common.delete}]]</a>" + ln);
        src.append("\t\t\t\t</script>" + ln);
        src.append("\t\t\t</div>" + ln);
        src.append("\t\t</div>" + ln);
        src.append("\t</div>" + ln + ln);
        src.append("\t<script src='/layuiadmin/layui/layui.js'></script>" + ln);
        src.append("\t<script>" + ln);
        src.append("\t\tvar lang = $('#language').val()" + semicolon_ln);
        src.append("\t\tvar langScript = document.createElement('script')" + semicolon_ln);
        src.append("\t\tlangScript.setAttribute('src', '" + levelNames + "/list.js?lang=' + lang)" + semicolon_ln);
        src.append("\t\tlangScript.id = 'lang'" + semicolon_ln);
        src.append("\t\tdocument.body.appendChild(langScript)" + semicolon_ln);
        src.append("\t</script>" + ln);
        src.append("</body>" + ln);
        src.append("</html>");
        generateFile(templateSrcFolder + fstLevelName + "/" + sndLevelName + (trdLevelName != null && !trdLevelName.isEmpty() ? "/" + trdLevelName : "") + "/list.html", src);
    }

    private static void generateAddHtml(Class pojoClass, String templateSrcFolder) throws PojoException {
        StringBuffer src = new StringBuffer();
        Annotation pojoAnnotation = pojoClass.getAnnotation(Pojo.class);
        if (pojoAnnotation == null) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation required! Pojo is " + pojoClass.getSimpleName());
        }
        String pojoDes = ((Pojo)pojoAnnotation).value();
        String pojoDesEn = ((Pojo)pojoAnnotation).enValue();
        if (pojoDes == null || pojoDes.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation Des value required! Pojo is " + pojoClass.getSimpleName());
        }
        if (pojoDesEn == null || pojoDesEn.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation DesEn value required! Pojo is " + pojoClass.getSimpleName());
        }
        String pojo = pojoClass.getSimpleName();
        String levelNames = getLevelNamesSrc(pojo);
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        String layId = "TEST-add-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String initJsStart = "\t$(function(){" + ln;
        StringBuffer fieldsJsBody = new StringBuffer();
        StringBuffer validJsBody = new StringBuffer();
        String initJsEnd = "\t})" + semicolon_ln;
        src.append("<!DOCTYPE html>" + ln);
        src.append("<html>" + ln);
        src.append("<head>" + ln);
        src.append("\t<meta charset='utf-8'>" + ln);
        src.append("\t<title>添加" + pojoDes + "</title>" + ln);
        src.append("\t<meta name='renderer' content='webkit'>" + ln);
        src.append("\t<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>" + ln);
        src.append("\t<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0'>" + ln);
        src.append("\t<link rel='stylesheet' href='/layuiadmin/layui/css/layui.css' media='all'>" + ln);
        src.append("\t<link rel='stylesheet' href='/richEditor/css/wangEditor.css' media='all'>" + ln);
        src.append("</head>" + ln);
        src.append("<body>" + ln);
        src.append("\t<div class='layui-form layui-form-pane' style='padding: 20px 20px 20px 20px;'>" + ln);
        //遍历 Pojo 中的字段，设计完毕，待验证
        for (Field f : pojoClass.getDeclaredFields()) {
            if (f.getAnnotation(PojoId.class) == null && !isSysField(f)) {
                Annotation attrAnnotation = f.getAnnotation(PojoAttrDes.class);
                if (attrAnnotation == null) {
                    throw new PojoException("Investor Exception Happened, Attr Annotation required! Pojo is " + pojoClass.getSimpleName() + ", Attr is " + f.getName());
                }
                String des;
                String desEn;
                if (f.getName().endsWith("En")) {
                    String fieldChineseName = f.getName().substring(0, f.getName().length() - 2);
                    try {
                        Field chineseField = pojoClass.getDeclaredField(fieldChineseName);
                        des = chineseField.getAnnotation(PojoAttrDes.class).des() + "英文名";
                        desEn = chineseField.getAnnotation(PojoAttrDes.class).desEn() + " English Name";
                    } catch (NoSuchFieldException e) {
                        throw new PojoException("Investor Exception Happened, No Such Chinese Attr! Pojo is " + pojoClass.getSimpleName() + ", Chinese Attr is " + fieldChineseName);
                    }
                } else {
                    des = f.getAnnotation(PojoAttrDes.class).des();
                    desEn = f.getAnnotation(PojoAttrDes.class).desEn();
                }
                String placeholder = "请输入" + des;
                String placeholderEn = "Please Input " + desEn;
                String example = f.getAnnotation(PojoAttrDes.class).example();
                Boolean valid = f.getAnnotation(PojoPageValid.class) != null;
                if (f.getAnnotation(PojoRichText.class) == null) {
                    src.append("\t\t<div class='layui-form-item'>" + ln);
                    src.append("\t\t\t<label class='layui-form-label' id='" + f.getName() + "-label" + "'>" + des + "</label>" + ln);
                    src.append("\t\t\t<div class='layui-input-block'>" + ln);
                    src.append("\t\t\t\t<input type='text' id='" + f.getName() + "-placeholder" + "' name='" + f.getName() + "' placeholder='" + placeholder + "' class='layui-input'> <label id='" + f.getName() + "-example-label" + "'>（例：" + example + "）</label>" + ln);
                    src.append("\t\t\t</div>" + ln);
                    src.append("\t\t</div>" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-label" + "').text('" + desEn + "')" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-placeholder" + "').attr('placeholder','" + placeholderEn + "')" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-example-label" + "').text('Example: " + example + "')" + ln);
                } else if (f.getAnnotation(PojoRichText.class) != null) {
                    src.append("\t\t<div class='layui-form-item'>" + ln);
                    src.append("\t\t\t<div id='" + f.getName() + "-rich-des" + "' style='width:100%' class='layui-form-label'>" + des + "</div>" + ln);
                    src.append("\t\t\t<div>" +ln);
                    src.append("\t\t<div style='display:none'>" + ln);
                    src.append("\t\t\t<input text='text' name='" + f.getName() + "' id='" + f.getName() + "'>" + ln);
                    src.append("\t\t</div>" + ln);
                    src.append("\t\t</div id='" + f.getName() + "-text-div" + "'>" + ln);
                    src.append("\t\t\t\t<p id='" + f.getName() + "-text" + "' onmouseleave='updateRich(this.id)'>" + placeholder + "</p>" + ln);
                    src.append("\t\t\t</div>" + ln);
                    src.append("\t\t</div>" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-rich-des" + "').text('" + desEn + "')" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-text" + "').text('" + placeholderEn + "')" + ln);
                }
                if (valid) {
                    validJsBody.append("\t\t\t" + f.getName() + ": [ /^[a-zA-Z0-9_-]+$/, (lang == 'zh-TW' || lang == 'zh-CN') ? '" + des + "输入不正确！' : '" + desEn + " input incorrect!' ]," + ln);
                }
            }
        }
        if (validJsBody.length() > 0) {
            validJsBody = new StringBuffer(validJsBody.substring(0, validJsBody.length() - 3));
        }
        src.append("\t<div class='layui-form-item layui-hide'>" + ln);
        src.append("\t\t<button class='layui-btn' lay-submit lay-filter='" + layId + "' id='" + layId + "'>[[#{common.confirm}]]</button>" + ln);
        src.append("\t</div>" + ln);
        src.append("</div>" + ln + ln);
        src.append("<script src='/layuiadmin/layui/layui.js'></script>" + ln);
        src.append("<script src='/richEditor/js/wangEditor.js'></script>" + ln);
        src.append("<script src='/js/zTree/jquery-1.4.4.min.js'></script>" + ln);
        src.append("<script>" + ln);
        src.append("\tfunction updateRich(textId) {" + ln);
        src.append("\t\tvar valId = textId.split('-')[0]" + semicolon_ln);
        src.append("\t\t$('#' + valId).attr('value', $('#' + textId).html())" + semicolon_ln);
        src.append("\t}" + ln);
        src.append("</script>" + ln);
        src.append("<script>" + ln);
        src.append(initJsStart + ln);
        src.append(fieldsJsBody + ln);
        src.append(initJsEnd + ln);
        src.append("\tvar lang = $('#language').val()" + semicolon_ln);
        src.append("\t\tif (lang != 'zh-TW' && lang != 'zh-CN') {" + ln);
        src.append("\t\t\t$(document).attr('title','" + pojoDesEn + "')" + semicolon_ln);
        src.append("\t\t}" + ln);
        src.append("\tlayui.config({" + ln);
        src.append("\t\tbase : '/layuiadmin/'//静态资源所在路径\\Static Resources Path" + ln);
        src.append("\t}).extend({" + ln);
        src.append("\t\tindex : 'lib/index'//主入口模块\\Main Entrance Module" + ln);
        src.append("\t}).use([ 'index', 'form' ], function() {" + ln);
        src.append("\t\t}).use([ 'index', 'form' ], function() {" + ln + ln);
        src.append("\t\tvar $ = layui.$, form = layui.form" + semicolon_ln);
        src.append("\t\t//验证\\Validate" + ln);
        src.append("\t\tform.verify({" + ln);
        src.append(validJsBody + ln);
        src.append("\t\t})" + semicolon_ln);
        src.append("\t})" + semicolon_ln);
        src.append("</script>" + ln);
        src.append("</body>" + ln);
        src.append("</html>");
        generateFile(templateSrcFolder + fstLevelName + "/" + sndLevelName + (trdLevelName != null && !trdLevelName.isEmpty() ? "/" + trdLevelName : "") + "/add.html", src);
    }

    private static void generateEditHtml(Class pojoClass, String templateSrcFolder) throws PojoException {
        StringBuffer src = new StringBuffer();
        String pojoId = "";
        Annotation pojoAnnotation = pojoClass.getAnnotation(Pojo.class);
        if (pojoAnnotation == null) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation required! Pojo is " + pojoClass.getSimpleName());
        }
        int fieldsCount = 0;
        for (Field field : pojoClass.getDeclaredFields()) {
            if (field.getAnnotation(PojoId.class) != null) {
                pojoId = field.getName();
                break;
            }
            fieldsCount++;
        }
        if (fieldsCount == pojoClass.getDeclaredFields().length) {
            throw new PojoException("Investor Exception Happened, Pojo doesn't have PojoId Annotation! Pojo is " + pojoClass.getSimpleName());
        }
        String pojoDes = ((Pojo)pojoClass.getAnnotation(Pojo.class)).value();
        String pojoDesEn = ((Pojo)pojoClass.getAnnotation(Pojo.class)).enValue();
        if (pojoDes == null || pojoDes.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation Des value required! Pojo is " + pojoClass.getSimpleName());
        }
        if (pojoDesEn == null || pojoDesEn.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation DesEn value required! Pojo is " + pojoClass.getSimpleName());
        }
        String pojo = pojoClass.getSimpleName();
        String levelNames = getLevelNamesSrc(pojo);
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        String initJsStart = "\t$(function(){" + ln;
        StringBuffer fieldsJsBody = new StringBuffer();
        StringBuffer validJsBody = new StringBuffer();
        String initJsEnd = "\t})" + semicolon_ln;
        String layId = "TEST-edit-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        src.append("<!DOCTYPE html>" + ln);
        src.append("<html xmlns:th='http://www.thymeleaf.org'>" + ln);
        src.append("<head>" + ln);
        src.append("\t<meta charset='utf-8'>" + ln);
        src.append("\t<title>编辑" + pojoDes + "</title>" + ln);
        src.append("\t<meta name='renderer' content='webkit'>");
        src.append("\t<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>" + ln);
        src.append("\t<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0'>" + ln);
        src.append("\t<link rel='stylesheet' href='/layuiadmin/layui/css/layui.css' media='all'>" + ln);
        src.append("\t<link rel='stylesheet' href='/richEditor/css/wangEditor.css' media='all'>" + ln);
        src.append("</head>" + ln);
        src.append("<body>" + ln);
        src.append("<div class='layui-form layui-form-pane' style='padding: 20px 20px 20px 20px;'>" + ln);
        src.append("\t<input type='hidden' name='" + pojoId + "' th:value='${model."+ pojoId + "}' />" + ln);
        //遍历 Pojo 字段，需要设计
        for (Field f : pojoClass.getDeclaredFields()) {
            if (f.getAnnotation(PojoId.class) == null && !isSysField(f)) {
                Annotation attrAnnotation =  f.getAnnotation(PojoAttrDes.class);
                if (attrAnnotation == null) {
                    throw new PojoException("Investor Exception Happened, Attr Annotation required! Pojo is " + pojoClass.getSimpleName() + ", Attr is " + f.getName());
                }
                String des;
                String desEn;
                if (f.getName().endsWith("En")) {
                    String fieldChineseName = f.getName().substring(0, f.getName().length() - 2);
                    try {
                        Field chineseField = pojoClass.getDeclaredField(fieldChineseName);
                        des = chineseField.getAnnotation(PojoAttrDes.class).des() + "英文名";
                        desEn = chineseField.getAnnotation(PojoAttrDes.class).desEn() + " English Name";
                    } catch (NoSuchFieldException e) {
                        throw new PojoException("Investor Exception Happened, No Such Chinese Attr! Pojo is " + pojoClass.getSimpleName() + ", Chinese Attr is " + fieldChineseName);
                    }
                } else {
                    des = f.getAnnotation(PojoAttrDes.class).des();
                    desEn = f.getAnnotation(PojoAttrDes.class).desEn();
                }
                String placeholder = "请输入" + des;
                String placeholderEn = "Please Input " + desEn;
                String example = f.getAnnotation(PojoAttrDes.class).example();
                boolean valid = f.getAnnotation(PojoPageValid.class) != null;
                if (f.getAnnotation(PojoRichText.class) == null) {
                    src.append("\t\t<div class='layui-form-item'>" + ln);
                    src.append("\t\t\t<label class='layui-form-label' id='" + f.getName() + "-label" + "'>" + des + "</label>" + ln);
                    src.append("\t\t\t<div class='layui-input-block'>" + ln);
                    src.append("\t\t\t\t<input type='text' id='" + f.getName() + "-placeholder" + "' name='" + f.getName() + "' th:value='${model." + f.getName() + "}' class='layui-input'> <label id='" + f.getName() + "-example-label'>（例：" + example + "）</label>" + ln);
                    src.append("\t\t\t</div>" + ln);
                    src.append("\t\t</div>" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-label" + "').text('" + desEn + "')" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-placeholder" + "').attr('placeholder'," + placeholderEn + ")" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-example-label').text('Example:" + example + "')" + ln);
                } else if (f.getAnnotation(PojoRichText.class) != null) {
                    src.append("\t\t<div class='layui-form-item'>" + ln);
                    src.append("\t\t\t<div id='" + f.getName() + "-rich-des" + "' style='width:100%' class='layui-form-label'>" + des + "</div>" + ln);
                    src.append("\t\t\t<div>" +ln);
                    src.append("\t\t<div style='display:none'>" + ln);
                    src.append("\t\t\t<input text='text' name='" + f.getName() + "' id='" + f.getName() + "'>" + ln);
                    src.append("\t\t</div>" + ln);
                    src.append("\t\t</div id='" + f.getName() + "-text-div" + "'>" + ln);
                    src.append("\t\t\t\t<p id='" + f.getName() + "-text" + "' onmouseleave='updateRich(this.id)'>" + placeholder + "</p>" + ln);
                    src.append("\t\t\t</div>" + ln);
                    src.append("\t\t</div>" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-rich-des" + "').text('" + desEn + "')" + ln);
                    fieldsJsBody.append("\t\t$('#" + f.getName() + "-text" + "').text('" + placeholderEn + "')" + ln);
                }
                if (valid) {
                    validJsBody.append("\t\t\t" + f.getName() + ": [ /^[a-zA-Z0-9_-]+$/, (lang == 'zh-TW' || lang == 'zh-CN') ? '" + des + "输入不正确！' : '" + desEn + " input incorrect!' ]" + ln);
                }
            }
        }
        src.append("\t<div class='layui-form-item layui-hide'>" + ln);
        src.append("\t\t<button class='layui-btn' lay-submit lay-filter='" + layId + "' id='" + layId + "'>[[#{common.confirm}]]</button>" + ln);
        src.append("\t</div>" + ln);
        src.append("</div>" + ln + ln);
        src.append("<script src='/layuiadmin/layui/layui.js'></script>" + ln);
        src.append("<script src='/richEditor/js/wangEditor.js'></script>" + ln);
        src.append("<script src='/js/zTree/jquery-1.4.4.min.js'></script>" + ln);
        src.append("<script>" + ln);
        src.append("\tfunction updateRich(textId) {" + ln);
        src.append("\t\tvar valId = textId.split('-')[0]" + semicolon_ln);
        src.append("\t\t$('#' + valId).attr('value', $('#' + textId).html())" + semicolon_ln);
        src.append("\t}" + ln);
        src.append("</script>" + ln);
        src.append("<script>" + ln);
        src.append(initJsStart +  ln);
        src.append("\t\tvar lang = $('#language').val()" + semicolon_ln);
        src.append("\t\tif (lang != 'zh-TW' && lang != 'zh-CN') {" + ln);
        src.append("\t\t\t$(document).attr('title','" + pojoDesEn + "')" + semicolon_ln);
        src.append("\t\t}" + ln);
        src.append(initJsEnd + semicolon_ln);
        src.append("\tlayui.config({" + ln);
        src.append("\t\tbase : '/layuiadmin/'//静态资源所在路径\\Static Resources Path" + ln);
        src.append("\t}).extend({" + ln);
        src.append("\t\tindex : 'lib/index'//主入口模块\\Main Entrance Module" + ln);
        src.append("\t}).use([ 'index', 'form' ], function() {" + ln);
        src.append("\t\tvar $ = layui.$, form = layui.form" + semicolon_ln);
        src.append("\t\t//验证\\Validate" + ln);
        src.append("\t\tform.verify({" + ln);
        src.append(validJsBody + ln);
        src.append("\t\t})" + semicolon_ln);
        src.append("\t})" + semicolon_ln);
        src.append("</script>" + ln);
        src.append("</body>" + ln);
        src.append("</html>");
        generateFile(templateSrcFolder + fstLevelName + "/" + sndLevelName + (trdLevelName != null && !trdLevelName.isEmpty() ? "/" + trdLevelName : "") + "/edit.html", src);
    }

    private static void generateAuditHtml(Class pojoClass, String templateSrcFolder) throws PojoException {
        StringBuffer src = new StringBuffer();
        Annotation pojoAnnotation = pojoClass.getAnnotation(Pojo.class);
        if (pojoAnnotation == null) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation required! Pojo is " + pojoClass.getSimpleName());
        }
        String pojoId = "";
        Integer fieldsCount = 0;
        for (Field field : pojoClass.getDeclaredFields()) {
            if (field.getAnnotation(PojoId.class) != null) {
                pojoId = field.getName();
                break;
            }
            fieldsCount++;
        }
        if (fieldsCount == pojoClass.getDeclaredFields().length) {
            throw new PojoException("Investor Exception Happened, Pojo doesn't have PojoId Annotation! Pojo is " + pojoClass.getSimpleName());
        }
        String pojoDes = ((Pojo)pojoAnnotation).value();
        String pojoDesEn = ((Pojo)pojoAnnotation).enValue();
        if (pojoDes == null || pojoDes.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation Des value required! Pojo is " + pojoClass.getSimpleName());
        }
        if (pojoDesEn == null || pojoDesEn.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation DesEn value required! Pojo is " + pojoClass.getSimpleName());
        }
        String pojo = pojoClass.getSimpleName();
        String levelNames = getLevelNamesSrc(pojo);
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        src.append("<!DOCTYPE html>" + ln);
        src.append("<html xmlns:th='http://www.thymeleaf.org'>" + ln);
        src.append("<head>" + ln);
        src.append("<meta charset='utf-8'>" + ln);
        src.append("<title>审核" + pojoDes + "</title>" + ln);
        src.append("<meta name='renderer' content='webkit'>" + ln);
        src.append("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>" + ln);
        src.append("<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0'>" + ln);
        src.append("<link rel='stylesheet' href='/layuiadmin/layui/css/layui.css' media='all'>" + ln);
        src.append("</head>" + ln);
        src.append("<body>" + ln);
        src.append("\t<div class='layui-form layui-form-pane' style='padding: 20px 20px 20px 20px;'>" + ln);
        src.append("\t\t<input type='hidden' name='language' id='language' th:value='${language}' />" + ln);
        src.append("\t\t<input type='hidden' name='" + pojoId + "' th:value='${model." + pojoId + "}' />" + ln);
        src.append("\t\t<div class='layui-form-item'>" + ln);
        src.append("\t\t\t<label class='layui-form-label'>[[#{common.audit.status}]]</label>" + ln);
        src.append("\t\t\t<div class='layui-input-block'>" + ln);
        src.append("\t\t\t\t<select name='status'>" + ln);
        src.append("\t\t\t\t\t<option th:selected='${model.status eq 0}' th:value='0' th:text='待审核' disabled='disabled'></option>" + ln);
        src.append("\t\t\t\t\t<option th:selected='${model.status eq 1}' th:value='1' th:text='已审核'></option>" + ln);
        src.append("\t\t\t\t\t<option th:selected='${model.status eq 2}' th:value='2' th:text='未通过'></option>" + ln);
        src.append("\t\t\t\t</select>" + ln);
        src.append("\t\t\t</div>" + ln);
        src.append("\t\t</div>" + ln);
        src.append("\t\t<div class='layui-form-item layui-form-text'>" + ln);
        src.append("\t\t\t<label class='layui-form-label'>[[#{common.table.th.remarks}]]</label>" + ln);
        src.append("\t\t\t<div class='layui-input-block'>" + ln);
        src.append("\t\t\t\t<textarea name='remarks' th:text='${model.remarks}' lay-verify='required' placeholder='请输入备注' class='layui-textarea'></textarea>" + ln);
        src.append("\t\t\t</div>" + ln);
        src.append("\t\t</div>" + ln);
        src.append("\t\t<div class='layui-form-item layui-hide'>" + ln);
        src.append("\t\t\t<button class='layui-btn' lay-submit lay-filter='TEST-audit-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "") + "' id='TEST-audit-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "") +  "'>[[#{common.confirm}]]</button>" + ln);
        src.append("\t\t</div>" + ln);
        src.append("\t</div>" + ln + ln);
        src.append("\t<script src='/layuiadmin/layui/layui.js'></script>" + ln);
        src.append("\t<script src='/js/zTree/jquery-1.4.4.min.js'></script>" + ln);
        src.append("\t<script>" + ln);
        src.append("\t\t$(function(){" + ln);
        src.append("\t\t\tvar lang = $('#language').val()" + ln);
        src.append("\t\t\tif (lang != 'zh-TW' && lang != 'zh-CN') {" + ln);
        src.append("\t\t\t\t$('#audit-status').find('option[value=\"0\"]').text('Waiting for Auditing')" + semicolon_ln);
        src.append("\t\t\t\t$('#audit-status').find('option[value=\"1\"]').text('Audited')" + semicolon_ln);
        src.append("\t\t\t\t$('#audit-status').find('option[value=\"2\"]').text('Not pass')" + semicolon_ln);
        src.append("\t\t\t\t$('#audit-remarks').attr('placeholder', 'Please Input Remarks')" + semicolon_ln);
        src.append("\t\t\t\t$(document).attr('title','" + pojoDesEn + "')" + semicolon_ln);
        src.append("\t\t\t}" + ln);
        src.append("\t\t})" + semicolon_ln + ln);
        src.append("\t\tlayui.config({" + ln);
        src.append("\t\t\tbase : '/layuiadmin/' //静态资源所在路径\\Static Resources Path" + ln);
        src.append("\t\t}).extend({" + ln);
        src.append("\t\t\tindex : 'lib/index' //主入口模块\\Main Entrance Module" + ln);
        src.append("\t\t}).use([ 'index', 'form' ], function() {" + ln);
        src.append("\t\t\tvar $ = layui.$, form = layui.form" + semicolon_ln);
        src.append("\t\t})" + semicolon_ln);
        src.append("\t</script>" + ln);
        src.append("</body>" + ln);
        src.append("</html>" + ln);
        generateFile(templateSrcFolder + fstLevelName + "/" + sndLevelName + (trdLevelName != null && !trdLevelName.isEmpty() ? "/" + trdLevelName : "") + "/audit.html", src);
    }

    private static void generateJsFiles(Class pojoClass, String staticSrcFolder) {
        try {
            generateListJsFile(pojoClass, staticSrcFolder);
        } catch (PojoException e) {
            e.printStackTrace();
        }
    }

    private static void generateListJsFile(Class pojoClass, String staticSrcFolder) throws PojoException {
        StringBuffer src = new StringBuffer();
        String pojo = pojoClass.getSimpleName();
        String pojoId = "";
        Annotation pojoAnnotation = pojoClass.getAnnotation(Pojo.class);
        if (pojoAnnotation == null) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation required! Pojo is " + pojoClass.getSimpleName());
        }
        Integer fieldsCount = 0;
        for (Field field : pojoClass.getDeclaredFields()) {
            if (field.getAnnotation(PojoId.class) != null) {
                pojoId = field.getName();
                break;
            }
            fieldsCount++;
        }
        if (fieldsCount == pojoClass.getDeclaredFields().length) {
            throw new PojoException("Investor Exception Happened, Pojo doesn't have PojoId Annotation! Pojo is " + pojoClass.getSimpleName());
        }
        String levelNames = getLevelNamesSrc(pojo);
        Integer levels = levelNames.split("/").length - 1;
        String[] levelNamesArr = levelNames.split("/");
        String fstLevelName = levelNamesArr[1];
        String sndLevelName = levelNamesArr[2];
        String trdLevelName = levels == 3 ? levelNamesArr[3] : null;
        String tableId = "TABLE-" + fstLevelName.toUpperCase() + "-" + sndLevelName.toUpperCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toUpperCase() : "");
        String addId = "TEST-add-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String pojoDes = ((Pojo)pojoClass.getAnnotation(Pojo.class)).value();
        String pojoDesEn = ((Pojo)pojoClass.getAnnotation(Pojo.class)).enValue();
        if (pojoDes == null || pojoDes.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation Des value required! Pojo is " + pojoClass.getSimpleName());
        }
        if (pojoDesEn == null || pojoDesEn.isEmpty()) {
            throw new PojoException("Investor Exception Happened, Pojo Annotation DesEn value required! Pojo is " + pojoClass.getSimpleName());
        }
        String toolbarId = "TEST-toolbar-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String submitId = "TEST-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String layuiBtnClass = ".layui-btn.TEST-btn-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String editClass = "TEST-edit-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        String auditClass = "TEST-audit-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + (trdLevelName != null && !trdLevelName.isEmpty() ? "-" + trdLevelName.toLowerCase() : "");
        src.append("layui.config({" + ln);
        src.append("\tbase: '/layuiadmin/' //静态资源所在路径\\Static Resources Path" + ln);
        src.append("}).extend({" + ln);
        src.append("\tindex: 'lib/index' //主入口模块\\Main Entrance Module" + ln);
        src.append("}).use(['index', 'table' ]," + ln);
        src.append("\tfunction() {" + ln);
        src.append("\t\tvar $ = layui.$," + ln);
        src.append("\t\t\tadmin = layui.admin," + ln);
        src.append("\t\t\tform = layui.form," + ln);
        src.append("\t\t\ttable = layui.table," + ln);
        src.append("\t\t\tlang = getLang('lang')" + semicolon_ln + ln);
        src.append("\t\ttable.render({" + ln);
        src.append("\t\t\tid: '" + tableId + "'," + ln);
        src.append("\t\t\telem: '#" + tableId + "'," + ln);
        src.append("\t\t\turl: '" + levelNames + "/getList'," + ln);
        src.append("\t\t\tcols: [[{" + ln);
        src.append("\t\t\t\ttype: 'checkbox'," + ln);
        src.append("\t\t\t\tfixed: 'left'" + ln);
        src.append("\t\t\t}," + ln);
        //遍历 Pojo
        for (Field f : pojoClass.getDeclaredFields()) {
            if (!isSysField(f)) {
                Annotation attrAnnotation = f.getAnnotation(PojoAttrDes.class);
                if (attrAnnotation == null) {
                    throw new PojoException("Investor Exception Happened, Attr Des Annotation required! Pojo is " + pojoClass.getSimpleName() + ", Attr is " + f.getName());
                }
                String des;
                String desEn;
                if (f.getName().endsWith("En")) {
                    String fieldChineseName = f.getName().substring(0, f.getName().length() - 2);
                    try {
                        Field chineseField = pojoClass.getDeclaredField(fieldChineseName);
                        des = chineseField.getAnnotation(PojoAttrDes.class).des() + "英文名";
                        desEn = chineseField.getAnnotation(PojoAttrDes.class).desEn() + " English Name";
                    } catch (NoSuchFieldException e) {
                        throw new PojoException("Investor Exception Happened, No Such Chinese Attr! Pojo is " + pojoClass.getSimpleName() + ", Chinese Attr is " + fieldChineseName);
                    }
                } else {
                    des = f.getAnnotation(PojoAttrDes.class).des();
                    desEn = f.getAnnotation(PojoAttrDes.class).desEn();
                }
                if (f.getAnnotation(PojoId.class) != null) {
                    src.append("\t\t\t{" + ln);
                    src.append("\t\t\t\tfield: '" + f.getName() + "'," + ln);
                    src.append("\t\t\t\twidth: 100," + ln);
                    src.append("\t\t\t\ttitle: 'ID'," + ln);
                    src.append("\t\t\t\tsort: true" + ln);
                    src.append("\t\t\t}," + ln);
                } else {
                    src.append("\t\t\t{" + ln);
                    src.append("\t\t\t\tfield: '" + f.getName() + "'," + ln);
                    src.append("\t\t\t\tminWidth: 160," + ln);
                    src.append("\t\t\t\ttitle: (lang == 'zh-TW' || lang == 'zh-CN') ? '" + des + "' : '" + desEn + "'" + ln);
                    src.append("\t\t\t}," + ln);
                }
            }
        }
        src.append("\t\t\t{" + ln);
        src.append("\t\t\t\ttitle: (lang == 'zh-TW' || lang == 'zh-CN') ? '操作' : 'Operations'," + ln);
        src.append("\t\t\t\twidth: 350," + ln);
        src.append("\t\t\t\talign: 'center'," + ln);
        src.append("\t\t\t\tfixed: 'right'," + ln);
        src.append("\t\t\t\ttoolbar: '#" + toolbarId + "'," + ln);
        src.append("\t\t\t}]]," + ln);
        src.append("\t\t\tpage: true," + ln);
        src.append("\t\t\tlimit: 50," + ln);
        src.append("\t\t\tlimits: [10, 20, 30, 40, 50, 100, 200, 500]," + ln);
        src.append("\t\t\ttext: {" + ln);
        src.append("\t\t\t\tnone: (lang == 'zh-TW' || lang == 'zh-CN') ? '无数据' : 'No Data'" + ln);
        src.append("\t\t\t}" + ln);
        src.append("\t\t})" + semicolon_ln);
        src.append("\t\t\t//监听搜索\\Search Listener" + ln);
        src.append("\t\t\tform.on('submit(" + submitId + ")'," + ln);
        src.append("\t\t\t\tfunction(data) {" + ln);
        src.append("\t\t\t\t\tvar field = data.field" +semicolon_ln + ln);
        src.append("\t\t\t\t\t//执行重载\\Reload" + ln);
        src.append("\t\t\t\t\ttable.reload('" + tableId + "', {" + ln);
        src.append("\t\t\t\t\t\twhere: field" + ln);
        src.append("\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t})" + semicolon_ln + ln);
        src.append("\t\t\t//事件\\Events" + ln);
        src.append("\t\t\tvar active = {" + ln);
        src.append("\t\t\t\tbatchdel: function() {" + ln);
        src.append("\t\t\t\t\tvar checkStatus = table.checkStatus('" + tableId + "')," + ln);
        src.append("\t\t\t\t\t\tcheckData = checkStatus.data; //得到选中的数据\\Get Selected Data" + ln);
        src.append("\t\t\t\t\tvar ids = ''," + ln);
        src.append("\t\t\t\t\t\tflag = false" + semicolon_ln);
        src.append("\t\t\t\t\tif (checkData.length === 0) {" + ln);
        src.append("\t\t\t\t\t\treturn layer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '请选择" + pojoDes + "！' : 'Please Select " + pojoDesEn + "!')" + semicolon_ln);
        src.append("\t\t\t\t\t}" + ln + ln);
        src.append("\t\t\t\t\tlayui.each(checkData, function(n, value) {" + ln);
        src.append("\t\t\t\t\t\tids += value." + pojoId + " + ','" + semicolon_ln);
        src.append("\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\tif (flag) {" + ln);
        src.append("\t\t\t\t\t\treturn" + semicolon_ln);
        src.append("\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\tids = ids.substring(0, ids.length - 1)" + semicolon_ln);
        src.append("\t\t\t\t\t\tlayer.confirm(((lang == 'zh-TW' || lang == 'zh-CN') ? '确认要删除' : 'Confirm to Delete') + '<strong>' + checkData.length + '</strong>' + ((lang == 'zh-TW' || lang == 'zh-CN') ? '条数据吗？' : 'Data?')," + ln);
        src.append("\t\t\t\t\t\t\tfunction(index) {" + ln);
        src.append("\t\t\t\t\t\t\t\tvar loadingDialog" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t$.ajax({" + ln);
        src.append("\t\t\t\t\t\t\t\t\turl: '" + levelNames + "/batchDelete/' + ids," + ln);
        src.append("\t\t\t\t\t\t\t\t\ttype: \"get\"," + ln);
        src.append("\t\t\t\t\t\t\t\t\tbeforeSend: function () {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tloadingDialog = showLoadingDialog((lang == 'zh-TW' || lang == 'zh-CN') ? '处理中...' : 'Processing...')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t}," + ln);
        src.append("\t\t\t\t\t\t\t\t\tsuccess: function(d) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tcloseLoadingDialog(loadingDialog)" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\tif (d.code == 0) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '删除成功！' : 'Deleted Successfully', {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\ticon: 1" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.close(index); //关闭弹层\\Closing Layer" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\ttable.reload('TABLE-" + fstLevelName.toUpperCase() + "-" + sndLevelName.toUpperCase() + ((trdLevelName != null && !trdLevelName.isEmpty()) ? "-" + trdLevelName.toUpperCase() : "") + "'); //数据刷新\\Refresh Data" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.msg(d.msg, {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\ticon: 5" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t}," + ln);
        src.append("\t\t\t\tadd: function() {" + ln);
        src.append("\t\t\t\t\tlayer.open({" + ln);
        src.append("\t\t\t\t\t\ttype: 2," + ln);
        src.append("\t\t\t\t\t\tcontent: '" + levelNames + "/addPage'," + ln);
        src.append("\t\t\t\t\t\tmaxmin: true," + ln);
        src.append("\t\t\t\t\t\tarea: ['1040px', '600px']," + ln);
        src.append("\t\t\t\t\t\tbtn: [(lang == 'zh-TW' || lang == 'zh-CN') ? '确定' : 'Confirm', (lang == 'zh-TW' || lang == 'zh-CN') ? '取消' : 'Cancel']," + ln);
        src.append("\t\t\t\t\t\tyes: function(index, layero) {" + ln);
        src.append("\t\t\t\t\t\t\tvar iframeWindow = window['layui-layer-iframe' + index]," + ln);
        src.append("\t\t\t\t\t\t\t\tsubmit = layero.find('iframe').contents().find('#" + addId + "')" + semicolon_ln + ln);
        src.append("\t\t\t\t\t\t\t//监听提交\\Submit Listener" + ln);
        src.append("\t\t\t\t\t\t\tiframeWindow.layui.form.on('submit(TEST-add-" + fstLevelName.toLowerCase() + "-" + sndLevelName.toLowerCase() + ((trdLevelName != null && !trdLevelName.isEmpty()) ? "-" + trdLevelName.toLowerCase() : "") + ")'," + ln);
        src.append("\t\t\t\t\t\t\t\tfunction(data) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\tvar loadingDialog" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t$.ajax({" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\ttype: 'post'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\turl: '" + levelNames + "/insert'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tdata: data.field," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tdataType: 'json'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tbeforeSend: function () {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tloadingDialog = showLoadingDialog((lang == 'zh-TW' || lang == 'zh-CN') ? '处理中...' : 'Processing...')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t}," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tsuccess: function(d) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tcloseLoadingDialog(loadingDialog)" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tif (d.code == 0) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '添加成功！' : 'Added Successfully!', {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 1" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tlayer.close(index); //关闭弹层\\Closing Layer" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\ttable.reload('TABLE-" + fstLevelName.toUpperCase() + "-" + sndLevelName.toUpperCase() + ((trdLevelName != null && !trdLevelName.isEmpty()) ? "-" + trdLevelName.toUpperCase() : "") + "'); //数据刷新\\Refresh Data" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg(d.msg, {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 5" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\tsubmit.trigger('click')" + semicolon_ln);
        src.append("\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t}" + ln);
        src.append("\t\t\t}" + ln + ln);
        src.append("\t\t\t$('" + layuiBtnClass + "').on('click'," + ln);
        src.append("\t\t\t\tfunction() {" + ln);
        src.append("\t\t\t\t\tvar type = $(this).data('type')" + semicolon_ln);
        src.append("\t\t\t\t\tactive[type] ? active[type].call(this) : ''" + semicolon_ln);
        src.append("\t\t\t\t})" + semicolon_ln + ln);
        src.append("\t\t\ttable.on('tool(" + tableId + ")'," + ln);
        src.append("\t\t\t\tfunction(obj) {" + ln + ln);
        src.append("\t\t\t\t\tvar data = obj.data" + semicolon_ln + ln);
        src.append("\t\t\t\t\tif (obj.event === 'del') {" + ln + ln);
        src.append("\t\t\t\t\t\tlayer.confirm((lang == 'zh-TW' || lang == 'zh-CN') ? '确定删除此" + pojoDes + "吗？' : 'Confirm to Delete this " + pojoDesEn + "?'," + ln);
        src.append("\t\t\t\t\t\t\tfunction(index) {" + ln);
        src.append("\t\t\t\t\t\t\t\tvar loadingDialog" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t$.ajax({" + ln);
        src.append("\t\t\t\t\t\t\t\t\turl: '" + levelNames + "/delete/' + data." + pojoId + "," + ln + ln);
        src.append("\t\t\t\t\t\t\t\t\ttype: \"get\"," + ln);
        src.append("\t\t\t\t\t\t\t\t\tbeforeSend: function () {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tloadingDialog = showLoadingDialog((lang == 'zh-TW' || lang == 'zh-CN') ? '处理中...' : 'Processing...')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t}," + ln);
        src.append("\t\t\t\t\t\t\t\t\tsuccess: function(d) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tcloseLoadingDialog(loadingDialog)" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\tif (d.code == 0) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '删除成功！' : 'Deleted Successfully!', {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t icon: 1" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.close(index); //关闭弹层\\Closing Layer" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\ttable.reload('TABLE-" + fstLevelName.toUpperCase() + "-" + sndLevelName.toUpperCase() + ((trdLevelName != null && !trdLevelName.isEmpty()) ? "-" + trdLevelName.toUpperCase() : "") + "'); //数据刷新\\Refresh Data" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tlayer.msg(d.msg, {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\ticon: 5" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t} else if (obj.event === 'edit') {" + ln);
        src.append("\t\t\t\t\t\tlayer.open({" + ln);
        src.append("\t\t\t\t\t\t\ttype: 2," + ln);
        src.append("\t\t\t\t\t\t\ttitle: (lang == 'zh-TW' || lang == 'zh-CN') ? '编辑" + pojoDes + "' : 'Edit " + pojoDesEn + "'," + ln);
        src.append("\t\t\t\t\t\t\tcontent: '" + levelNames + "/editPage/' + data." + pojoId + "," + ln);
        src.append("\t\t\t\t\t\t\tmaxmin: true," + ln);
        src.append("\t\t\t\t\t\t\tarea: ['1040px', '600px']," + ln);
        src.append("\t\t\t\t\t\t\tbtn: [(lang == 'zh-TW' || lang == 'zh-CN') ? '确定' : 'Confirm', (lang == 'zh-TW' || lang == 'zh-CN') ? '取消' : 'Cancel']," + ln);
        src.append("\t\t\t\t\t\t\tyes: function(index, layero) {" + ln);
        src.append("\t\t\t\t\t\t\t\tvar iframeWindow = window['layui-layer-iframe' + index]," + ln);
        src.append("\t\t\t\t\t\t\t\t\tsubmit = layero.find('iframe').contents().find('#" + editClass + "')" + semicolon_ln + ln);
        src.append("\t\t\t\t\t\t\t\t//监听提交\\Submit Listener" + ln);
        src.append("\t\t\t\t\t\t\t\tiframeWindow.layui.form.on('submit(" + editClass + ")'," + ln);
        src.append("\t\t\t\t\t\t\t\t\tfunction(data) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tvar loadingDialog" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t$.ajax({" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\ttype: 'post'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\turl: '" + levelNames + "/update'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tdata: data.field," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tdataType: 'json'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tbeforeSend: function () {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tloadingDialog = showLoadingDialog((lang == 'zh-TW' || lang == 'zh-CN') ? '处理中...' : 'Processing...')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t}," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tsuccess: function(d) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tcloseLoadingDialog(loadingDialog)" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tif (d.code == 0) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '更新成功！' : 'Edited Successfully', {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 1" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.close(index); //关闭弹层\\Closing Layer" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\ttable.reload('TABLE-COR-COM-BASICINFO'); //数据刷新\\Refreshing Data" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg(d.msg, {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 5" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\tsubmit.trigger('click')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t})" + ln);
        src.append("\t\t\t\t\t} else if (obj.event === 'audit') {" + ln);
        src.append("\t\t\t\t\t\tlayer.open({" + ln);
        src.append("\t\t\t\t\t\t\ttype: 2," + ln);
        src.append("\t\t\t\t\t\t\ttitle: (lang == 'zh-TW' || lang == 'zh-CN') ? '审核" + pojoDes + "' : 'Audit " + pojoDesEn + "'," + ln);
        src.append("\t\t\t\t\t\t\tcontent: '" + levelNames + "/auditPage/' + data." + pojoId + "," + ln);
        src.append("\t\t\t\t\t\t\tmaxmin: true," + ln);
        src.append("\t\t\t\t\t\t\tarea: ['1040px', '600px']," + ln);
        src.append("\t\t\t\t\t\t\tbtn: [(lang == 'zh-TW' || lang == 'zh-CN') ? '确定' : 'Confirm', (lang == 'zh-TW' || lang == 'zh-CN') ? '取消' : 'Cancel']," + ln);
        src.append("\t\t\t\t\t\t\tyes: function(index, layero) {" + ln);
        src.append("\t\t\t\t\t\t\t\tvar iframeWindow = window['layui-layer-iframe' + index]," + ln);
        src.append("\t\t\t\t\t\t\t\t\tsubmit = layero.find('iframe').contents().find('#" + auditClass + "')" + semicolon_ln + ln);
        src.append("\t\t\t\t\t\t\t\t//监听提交\\Submit Listener" + ln);
        src.append("\t\t\t\t\t\t\t\tiframeWindow.layui.form.on('submit(" + auditClass + ")'," + ln);
        src.append("\t\t\t\t\t\t\t\t\tfunction(data) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\tvar loadingDialog" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t$.ajax({" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\ttype: 'post'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\turl: '" + levelNames + "/audit'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tdata: data.field," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tdataType: 'json'," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tbeforeSend: function () {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tloadingDialog = showLoadingDialog((lang == 'zh-TW' || lang == 'zh-CN') ? '处理中...' : 'Processing...')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t}," + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\tsuccess: function(d) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tcloseLoadingDialog(loadingDialog)" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\tif (d.code == 0) {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg((lang == 'zh-TW' || lang == 'zh-CN') ? '审核完成！' : 'Audited Successfully!', {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 1" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.close(index); //關閉彈層\\Closing Layer" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\ttable.reload('TABLE-COR-COM-BASICINFO'); //数据刷新\\Refresh Data" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t} else {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\tlayer.msg(d.msg, {" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\ticon: 5" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t\tsubmit.trigger('click')" + semicolon_ln);
        src.append("\t\t\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t\t\t})" + ln);
        src.append("\t\t\t\t\t}" + ln);
        src.append("\t\t\t\t})" + semicolon_ln + ln);
        src.append("\t\t\tfunction showLoadingDialog(msg) {" + ln);
        src.append("\t\t\t\treturn layer.msg(msg, {" + ln);
        src.append("\t\t\t\t\ticon: 16" + ln);
        src.append("\t\t\t\t})" + semicolon_ln);
        src.append("\t\t\t}" + ln + ln);
        src.append("\t\t\tfunction closeLoadingDialog(index) {" + ln);
        src.append("\t\t\t\tlayer.close(index)" + semicolon_ln);
        src.append("\t\t\t}" + ln + ln);
        src.append("\t\t\tfunction getLang(paramName) {" + ln);
        src.append("\t\t\t\tvar src = document.body.childNodes[6].src" + semicolon_ln);
        src.append("\t\t\t\tvar reg = new RegExp('(^|/?|&)' + paramName + '=([^&]*)(/s|&|$)', 'i')" + semicolon_ln);
        src.append("\t\t\t\tif (reg.test(src)) //test为script ID" + ln);
        src.append("\t\t\t\t\treturn RegExp.$2" + semicolon_ln);
        src.append("\t\t\t\telse" + ln);
        src.append("\t\t\t\t\treturn ''" + semicolon_ln);
        src.append("\t\t\t}" + ln);
        src.append("\t\t})" + semicolon_ln);
        generateFile(staticSrcFolder + fstLevelName + "/" + sndLevelName + (trdLevelName != null && !trdLevelName.isEmpty() ? "/" + trdLevelName : "") + "/list.js", src);
    }

    private static void generateFile(String filePath, StringBuffer src) {
        try {
            String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
            File folderFile = new File(folderPath);
            if (!(folderFile).exists()) {
                (new File(folderPath)).mkdirs();
            }
            FileWriter fw = new FileWriter(filePath);
            fw.write(src.toString());
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println("error happened");
        }
    }

    private static String getLowerName(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private static String getLevelNamesSrc(String pojoName) {
        List<Integer> breakPoints = new ArrayList<>();
        String levelNames = pojoName.substring(4); //substring 4 是因为 Pojo 前缀是 Test，使用时需调整
        String result = "";
        Integer index = 0;
        for (char c : levelNames.toCharArray()) {
            if (Character.isUpperCase(c)) {
                breakPoints.add(index);
            }
            index++;
        }
        if (breakPoints.size() == 4) {
            result = "/" + getLowerName(levelNames.substring(breakPoints.get(0), breakPoints.get(1))) + "/" + getLowerName(levelNames.substring(breakPoints.get(2)));
        } else if (breakPoints.size() == 6) {
            result = "/" + getLowerName(levelNames.substring(breakPoints.get(0), breakPoints.get(1))) + "/" + getLowerName(levelNames.substring(breakPoints.get(2), breakPoints.get(3))) + "/" + getLowerName(levelNames.substring(breakPoints.get(4)));
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getLevelNamesSrc("TestCorInfoComInfoBasicInfo"));
    }

    private static boolean isSysField(Field field) {
        return sysFields.contains(field.getName());
    }

    private static String convertToColumnId(String attrId) {
        List<Integer> breakPoints = new ArrayList<>();
        String result = "";
        Integer index = 0;
        for (char c : attrId.toCharArray()) {
            if (Character.isUpperCase(c)) {
                breakPoints.add(index);
            }
            index++;
        }
        Integer breakPointCount = breakPoints.size();
        for (int i = 0; i < breakPointCount; i++) {
            if (i == 0) {
                result = attrId.substring(0, breakPoints.get(0)) + "_";
            } else {
                result += attrId.substring(breakPoints.get(i - 1), breakPoints.get(i - 1) + 1).toLowerCase() + attrId.substring(breakPoints.get(i - 1) + 1, breakPoints.get(i)) + "_";
            }
        }
        result += "id";
        return result;
    }
}
