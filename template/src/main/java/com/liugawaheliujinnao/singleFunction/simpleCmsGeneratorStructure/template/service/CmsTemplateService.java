package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service;

import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplateCriteria;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplateExample;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplatePojo;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.utils.ResultUtils;

import java.util.List;

/**
 * @Description: 所有 Service 接口的父类，抽象了后续一键构建需要用到的方法，基本涵盖了 CMS 项目需要使用到的所有的单表 CURD 方法
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public interface CmsTemplateService<T extends CmsTemplatePojo, P extends CmsTemplateExample, K extends CmsTemplateCriteria> {

    ResultUtils getList(String defaultOrder, Integer page, Integer limit) throws InstantiationException, IllegalAccessException;

    ResultUtils searchList(String defaultOrder, Integer page, Integer limit, T model);

    T getById(Integer id);

    boolean insert(T model);

    boolean update(T model);

    boolean delete(Integer id);

    boolean delete(T model);

    boolean fakeDelete(T model);

    boolean batchDelete(String modelIds);

    boolean batchDelete(List<T> models);

    boolean batchFakeDelete(List<T> models);

    boolean audit(T model);
}
