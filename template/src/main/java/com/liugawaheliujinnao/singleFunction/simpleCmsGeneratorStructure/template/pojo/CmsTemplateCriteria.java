package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo;

/**
 * @Description: 本项目使用 Mybatis 生成 Pojo， mappers 与 dao 与即相应 Pojo 操作 Example 类，所有 Example 中内部类 Criteria 的抽象父类，抽象出后续一键构建需要用到的方法
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public class CmsTemplateCriteria {

    public CmsTemplateCriteria andStatusEqualTo(byte status) {
        return this;
    }
}
