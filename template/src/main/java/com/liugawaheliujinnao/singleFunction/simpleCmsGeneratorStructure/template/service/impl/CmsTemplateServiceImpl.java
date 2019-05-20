package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.impl;

import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.dao.CmsTemplateMapper;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplateCriteria;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplateExample;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplatePojo;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.CmsTemplateService;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.utils.ResultUtils;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: LiujinnaoHeLiugawa
 * @Date: 2019-05-20
 */
@Service
public abstract class CmsTemplateServiceImpl<T extends CmsTemplatePojo, P extends CmsTemplateExample, K extends CmsTemplateCriteria> implements CmsTemplateService<T, P, K> {

    public static final byte DATA_STATUS_0 = 0;

    private Class<T> objectClass;
    private Class<P> exampleClass;
    private Class<K> criteriaClass;

    public CmsTemplateServiceImpl() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        objectClass = (Class) params[0];
        exampleClass = (Class) params[1];
        criteriaClass = (Class) params[2];
    }

    /**
     * 获取默认列表数据扩展点（前）
     */
    public void beforeGetList() {
        getLogger().info("获取默认列表数据 Service 扩展点（前）");
    }

    /**
     * 获取默认列表数据
     * @param defaultOrder
     * @param page
     * @param limit
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Override
    public ResultUtils getList(String defaultOrder, Integer page, Integer limit) throws InstantiationException, IllegalAccessException {
//        PageHelper.startPage(page, limit); //(使用 Pagehepler 进行分页代理)
        beforeGetList();
        getLogger().info("获取默认列表数据 Service 方法");
        ResultUtils result = convertPage(getMapper().selectByExample(initDefaultCondition(defaultOrder)));
        afterGetList();
        return result;
    }

    /**
     * 获取默认列表数据扩展点（后）
     */
    public void afterGetList() {
        getLogger().info("获取默认列表数据 Service 扩展点（后）");
    }

    /**
     * 根据 ID 获取
     * @param id
     * @return
     */
    @Override
    public T getById(Integer id) {
        return getMapper().selectByPrimaryKey(id);
    }

    /**
     * 获取搜索列表数据扩展点（前）
     * @param model
     */
    public void beforeSearchList(T model) {
        getLogger().info("获取搜索列表数据 Service 扩展点（前）");
    }

    /**
     * 获取搜索列表数据
     * @param defaultOrder
     * @param page
     * @param limit
     * @param model
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Override
    public ResultUtils searchList(String defaultOrder, Integer page, Integer limit, T model) {
//        PageHelper.startPage(page, limit); //使用 Pagehelper 进行动态代理
        beforeSearchList(model);
        getLogger().info("获取搜索列表数据 Service 方法");
        ResultUtils result = convertPage(getMapper().selectByExample(initSearchCondition(defaultOrder, model)));
        afterSearchList(model);
        return result;
    }

    /**
     * 获取搜索列表数据扩展点（后）
     * @param model
     */
    public void afterSearchList(T model) {
        getLogger().info("获取搜索列表数据 Service 扩展点（后）");
    }

    private ResultUtils convertPage(List<T> list) {
        // 请自行实现分页，比如 Pagehelper
        return null;
    }

    /**
     * 插入扩展点（前）
     * @param model
     */
    public void beforeInsert(T model) {
        getLogger().info("插入 Service 扩展点（前）");
    }

    /**
     * 插入
     * @param model
     * @return
     */
    @Override
    public boolean insert(T model) {
        this.beforeInsert(model);
        getLogger().info("插入 Service 方法");
        model.setCdt(new Date());
        Integer c_user_id = model.getcUserId();
        model.setcUserId(c_user_id == null ? 1 : c_user_id);
        this.getMapper().insert(model);
        this.afterInsert(model);
        return true;
    }

    /**
     * 插入扩展点（后）
     * @param model
     */
    public void afterInsert(T model) {
        getLogger().info("插入 Service 扩展点（后）");
    }

    /**
     * 更新扩展点（前）
     * @param model
     */
    public void beforeUpdate(T model) {
        getLogger().info("更新 Service 扩展点（前）");
    }

    /**
     * 更新
     * @param model
     * @return
     */
    @Override
    public boolean update(T model) {
        beforeUpdate(model);
        getLogger().info("更新 Service 方法");
        getMapper().updateByPrimaryKeySelective(model);
        afterUpdate(model);
        return true;
    }

    /**
     * 更新扩展点（后）
     * @param model
     */
    public void afterUpdate(T model) {
        getLogger().info("更新 Service 扩展点（后）");
    }

    /**
     * 删除扩展点（前）
     * @param model
     */
    public void beforeDelete(T model) {
        getLogger().info("删除 Service 扩展点（前）");
    }

    /**
     * 删除
     * @param model
     * @return
     */
    @Override
    public boolean delete(T model) {
        this.beforeDelete(model);
        getLogger().info("删除 Service 方法");
        this.getMapper().deleteByPrimaryKey(model.getId());
        this.afterDelete(model);
        return true;
    }

    /**
     * 删除扩展点（后）
     * @param model
     */
    public void afterDelete(T model) {
        getLogger().info("删除 Service 扩展点（后）");
    }

    /**
     * 根据 ID 删除扩展点（前）
     * @param id
     */
    public void beforeDelete(Integer id) {
        getLogger().info("根据 ID 删除 Service 扩展点（前）");
    }

    /**
     * 根据 ID 删除
     * @param id
     * @return
     */
    @Override
    public boolean delete(Integer id) {
        this.beforeDelete(id);
        getLogger().info("根据 ID 删除 Service 方法");
        this.getMapper().deleteByPrimaryKey(id);
        this.afterDelete(id);
        return true;
    }

    /**
     * 根据 ID 删除扩展点（后）
     * @param id
     */
    public void afterDelete(Integer id) {
        getLogger().info("根据 ID 删除 Service 扩展点（后）");
    }

    /**
     * 虚删扩展点（前）
     * @param model
     */
    public void beforeFakeDelete(T model) {
        getLogger().info("虚删 Service 扩展点（前）");
    }

    /**
     * 虚删
     * @param model
     * @return
     */
    @Override
    public boolean fakeDelete(T model) {
        this.beforeFakeDelete(model);
        getLogger().info("虚删 Service 方法");
        this.getMapper().updateByPrimaryKey(model);
        this.afterFakeDelete(model);
        return true;
    }

    /**
     * 虚删扩展点（后）
     * @param model
     */
    public void afterFakeDelete(T model) {
        getLogger().info("虚删 Service 扩展点（后）");
    }

    /**
     * 批量删除扩展点（前）
     * @param models
     */
    public void beforeBatchDelete(List<T> models) {
        getLogger().info("批量删除 Service 扩展点（前）");
    }

    /**
     * 批量删除
     * @param models
     * @return
     */
    @Override
    public boolean batchDelete(List<T> models) {
        this.beforeBatchDelete(models);
        getLogger().info("批量删除 Service 方法");
        models.forEach(k -> {
            this.delete(k);
        });
        this.afterBatchDelete(models);
        return true;
    }

    /**
     * 批量删除扩展点（后）
     * @param models
     */
    public void afterBatchDelete(List<T> models) {
        getLogger().info("批量删除 Service 扩展点（后）");
    }

    /**
     * 通过ID批量删除扩展点（前）
     * @param modelIds
     */
    public void beforeBatchDelete(String modelIds) {
        getLogger().info("通过ID批量删除 Service 扩展点（前）");
    }

    /**
     * 通过ID批量删除
     * @param modelIds
     * @return
     */
    @Override
    public boolean batchDelete(String modelIds) {
        this.beforeBatchDelete(modelIds);
        String[] ids = modelIds.split(",");
        for (String id : ids) {
            this.delete(Integer.parseInt(id));
        }
        this.afterBatchDelete(modelIds);
        return true;
    }

    /**
     * 通过ID批量删除扩展点（后）
     * @param modelIds
     */
    public void afterBatchDelete(String modelIds) {
        getLogger().info("通过ID批量删除 Service 扩展点（后）");
    }

    /**
     * 批量虚删扩展点（前）
     * @param model
     */
    public void beforeBatchFakeDelete(List<T> model) {
        getLogger().info("批量虚删 Service 扩展点（前）");
    }

    /**
     * 批量虚删
     * @param models
     * @return
     */
    @Override
    public boolean batchFakeDelete(List<T> models) {
        this.beforeBatchFakeDelete(models);
        getLogger().info("批量虚删 Service 方法");
        models.forEach(k -> {
            this.fakeDelete(k);
        });
        this.afterBatchFakeDelete(models);
        return true;
    }

    /**
     * 批量虚删扩展点（后）
     * @param model
     */
    public void afterBatchFakeDelete(List<T> model) {
        getLogger().info("批量虚删 Service 扩展点（后）");
    }

    /**
     * 审核扩展点（前）
     * @param model
     */
    public void beforeAudit(T model) {
        getLogger().info("审核 Service 扩展点（前）");
    }

    /**
     * 审核
     * @param model
     * @return
     */
    @Override
    public boolean audit(T model) {
        this.beforeAudit(model);
        getLogger().info("审核 Service 方法");
        getMapper().updateByPrimaryKeySelective(model);
        this.afterAudit(model);
        return true;
    }

    /**
     * 审核扩展点（后）
     * @param model
     */
    public void afterAudit(T model) {
        getLogger().info("审核 Service 扩展点（后）");
    }


    public P initDefaultCondition(String defaultOrder) throws InstantiationException, IllegalAccessException {
        P example = exampleClass.newInstance();
        example.setOrderByClause(defaultOrder);
        CmsTemplateCriteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(DATA_STATUS_0);
        return example;
    }

    public abstract P initSearchCondition(String defaultOrder, T model);

    public abstract CmsTemplateMapper<T, P> getMapper();

    public abstract Logger getLogger();
}
