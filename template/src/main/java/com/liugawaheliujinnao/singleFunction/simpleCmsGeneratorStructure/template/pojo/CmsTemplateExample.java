package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo;

/**
 * @Description: 本项目使用 Mybatis 生成 Pojo， mappers 与 dao 与即相应 Pojo 操作 Example 类，全局 Example 抽象父类，抽象出后续一键构建需要用到的方法
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public abstract class CmsTemplateExample {

    public abstract void setOrderByClause(String orderByClause);

    public abstract CmsTemplateCriteria createCriteria();
}
