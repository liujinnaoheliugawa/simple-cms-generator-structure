package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.controller;

import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo.CmsTemplatePojo;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.service.CmsTemplateService;
import com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.utils.ResultUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 所有 Controller 的父抽象类，抽象了一键构建需要用到的方法，扩展点为默认实现，需要使用时请自行扩展
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public abstract class CmsTemplateController<T extends CmsTemplatePojo> {

    /**
     * 获取列表页面扩展点
     * @return
     */
    public boolean beforeListPage() {
        getLogger().info("获取列表页面 Controller 扩展点方法");
        return true;
    }

    /**
     * 获取列表页面
     * @param listPage
     * @return
     */
    public String getListPage(String listPage) {
        getLogger().info("获取列表页面 Controller 方法");
        return beforeListPage() ? listPage : "admin/active";
    }

    /**
     * 获取添加页面扩展点
     * @return
     */
    public boolean beforeAddPage() {
        getLogger().info("获取添加页面 Controller 扩展点方法");
        return true;
    }

    /**
     * 获取添加页面
     * @param addPage
     * @return
     */
    public String getAddPage(String addPage) {
        getLogger().info("获取添加页面 Controller 方法");
        return beforeAddPage() ? addPage : "admin/active";
    }

    /**
     * 获取编辑页面扩展点
     * @return
     */
    public boolean beforeEditPage() {
        getLogger().info("获取编辑页面 Controller 扩展点方法");
        return true;
    }

    /**
     * 获取编辑页面
     * @param editPage
     * @return
     */
    public String getEditPage(HttpServletRequest request, Integer id, String editPage) {
        getLogger().info("获取编辑页面 Controller 方法");
        CmsTemplatePojo model = getService().getById(id);
        if (beforeEditPage()) {
            request.setAttribute("model", model);
        } else {
            request.setAttribute("msg", "不允许操作！");
        }
        return beforeEditPage() ? editPage : "admin/active";
    }

    /**
     * 获取审核页面扩展点
     * @return
     */
    public boolean beforeAuditPage() {
        getLogger().info("获取審核页面 Controller 扩展点方法");
        return true;
    }

    /**
     * 获取审核页面
     * @param auditPage
     * @return
     */
    public String getAuditPage(HttpServletRequest request, Integer id, String auditPage) {
        getLogger().info("获取審核页面 Controller 方法");
        CmsTemplatePojo model = getService().getById(id);
        if (beforeAuditPage()) {
            request.setAttribute("model", model);
        } else {
            request.setAttribute("msg", "不允许操作！");
        }
        return beforeAuditPage() ? auditPage : "admin/active";
    }

    /**
     * 获取列表页数据扩展点（前）
     */
    public void beforeGet() {
        getLogger().info("获取列表页数据 Controller 扩展点（前）");
    }

    /**
     * 获取列表页数据
     * @param defaultOrder
     * @param page
     * @param limit
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ResultUtils getList (String defaultOrder, Integer page, Integer limit) throws InstantiationException, IllegalAccessException {
        this.beforeGet();
        getLogger().info("获取列表页面数据 Controller 方法");
        ResultUtils result = this.getService().getList(defaultOrder, page, limit);
        this.afterGet();
        return result;
    }

    /**
     * 获取列表页面数据扩展点（后）
     */
    public void afterGet() {
        getLogger().info("获取列表页数据 Controller 扩展点（后）");
    }

    /**
     * 获取搜索列表页数据扩展点（前）
     * @param model
     */
    public void beforeSearch(T model) {
        getLogger().info("获取搜索列表页数据 Controller 扩展点（前）");
    }

    /**
     * 获取搜索列表页数据
     * @param defaultOrder
     * @param page
     * @param limit
     * @param model
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ResultUtils searchList(String defaultOrder, Integer page, Integer limit, T model) throws InstantiationException, IllegalAccessException {
        this.beforeSearch(model);
        getLogger().info("获取搜索列表页数据 Controller 方法");
        ResultUtils result = this.getService().searchList(defaultOrder, page, limit, model);
        this.afterSearch(model);
        return result;
    }

    /**
     * 获取搜索列表也数据扩展点（后）
     * @param model
     */
    public void afterSearch(T model) {
        getLogger().info("获取搜索列表页数据 Controller 扩展点（后）");
    }

    /**
     * 添加扩展点（前）
     * @param model
     */
    public void beforeInsert(T model) {
        getLogger().info("添加 Controller 扩展点（前）");
    }

    /**
     * 添加
     * @param model
     * @return
     */
    public ResultUtils insert(T model) {
        this.beforeInsert(model);
        getLogger().info("添加 Controller 扩展点方法");
        ResultUtils result = this.getService().insert(model) ? ResultUtils.ok("添加成功！") : ResultUtils.error("系统错误！");
        this.afterInsert(model);
        return result;
    }

    /**
     * 添加扩展点（后）
     * @param model
     */
    public void afterInsert(T model) {
        getLogger().info("添加 Controller 扩展点（后）");
    }

    /**
     * 更新扩展点（前）
     * @param model
     */
    public void beforeUpdate(T model) {
        getLogger().info("更新 Controller 扩展点（前）");
    }

    /**
     * 更新
     * @param model
     * @return
     */
    public ResultUtils update(T model) {
        this.beforeUpdate(model);
        getLogger().info("更新 Controller 扩展点方法");
        ResultUtils result = this.getService().update(model) ? ResultUtils.ok("添加成功！") : ResultUtils.error("系统错误！");
        this.afterUpdate(model);
        return result;
    }

    /**
     * 更新扩展点（后）
     * @param model
     */
    public void afterUpdate(T model) {
        getLogger().info("更新 Controller 扩展点（后）");
    }

    /**
     * 删除扩展点（前）
     * @param model
     */
    public void beforeDelete(T model) {
        getLogger().info("删除 Controller 扩展点（前）");
    }

    /**
     * 删除
     * @param model
     * @return
     */
    public ResultUtils delete(T model) {
        this.beforeDelete(model);
        getLogger().info("删除 Controller 方法");
        ResultUtils result = this.getService().delete(model) ? ResultUtils.ok("添加成功！") : ResultUtils.error("系统错误！");
        this.afterDelete(model);
        return result;
    }

    /**
     * 删除扩展点（后）
     * @param model
     */
    public void afterDelete(T model) {
        getLogger().info("删除 Controller 扩展点（后）");
    }

    /**
     * 根据 ID 删除扩展点（前）
     * @param id
     */
    public void beforeDelete(Integer id) {
        getLogger().info("根据 ID 删除 Controller 扩展点（后）");
    }

    /**
     * 根据 ID 删除
     * @param id
     * @return
     */
    public ResultUtils delete(Integer id) {
        this.beforeDelete(id);
        getLogger().info("根据 ID 删除 Controller 方法");
        ResultUtils result = this.getService().delete(id) ? ResultUtils.ok("添加成功！") : ResultUtils.error("系统错误！");
        this.afterDelete(id);
        return result;
    }

    /**
     * 根据 ID 删除扩展点（后）
     * @param id
     */
    public void afterDelete(Integer id) {
        getLogger().info("根据 ID 删除 Controller 扩展点（后）");
    }

    /**
     * 审核扩展点（前）
     * @param model
     */
    public void beforeAudit(T model) {
        getLogger().info("审核 Controller 扩展点（前）");
    }

    /**
     * 审核
     * @param model
     * @return
     */
    public ResultUtils audit(T model) {
        this.beforeAudit(model);
        getLogger().info("审核 Controller 方法");
        ResultUtils result = this.getService().audit(model) ? ResultUtils.ok("添加成功！") : ResultUtils.error("系统错误！");
        this.afterAudit(model);
        return result;
    }

    /**
     * 审核扩展点（后）
     * @param model
     */
    public void afterAudit(T model) {
        getLogger().info("审核 Controller 扩展点（后）");
    }

    public abstract CmsTemplateService getService();

    public abstract Logger getLogger();
}
