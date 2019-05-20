package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.dao;

import java.util.List;

/**
 * @Description: 本项目使用 Mybatis 生成 Pojo， mappers 与 dao 与即相应 Pojo 操作 Example 类，所有 Mybatis 自动生成 Mapper 的父接口，抽象了后续一键生成需要用到的方法
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public interface CmsTemplateMapper<T, P> {

    List<T> selectByExample(P example);

    T selectByPrimaryKey(Integer id);

    int deleteByPrimaryKey(Integer id);

    int insert(T record);

    int updateByPrimaryKey(T record);

    int updateByPrimaryKeyWithBLOBs(T record);

    int updateByPrimaryKeySelective(T record);
}
