# 一键生成 CMS 插件

## 目的

一键生成 CMS 项目中的 Service 层，Controller 层，CMS CURD 页面以及 JS 文件。

插件使用样例项目请参考 simple-cms-generator-project-example 项目



## 注意

Pojo 层与 Dao 层需要使用 Mybatis 插件自动生成，暂不支持 Hibernate 框架。

CMS CURD 页面使用 layui 框架，暂不支持其它框架，使用时需引入相关 JS/CSS 文件。

页面文件部署使用 SpringBoot + Thymeleaf 结构，resources 文件夹下需要有 static 与 templates 文件夹，暂不支持 SpringMVC 框架。
