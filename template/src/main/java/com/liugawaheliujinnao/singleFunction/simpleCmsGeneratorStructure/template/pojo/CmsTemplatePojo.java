package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.template.pojo;

import java.util.Date;

/**
 * @Description: 所有 Pojo 的父类，抽象出全局 Pojo 对象都必须使用的属性，便于之后对整个框架的抽象
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public class CmsTemplatePojo {

    private Integer id;

    private Date cdt;

    private Integer cUserId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCdt() {
        return cdt;
    }

    public void setCdt(Date cdt) {
        this.cdt = cdt;
    }

    public Integer getcUserId() {
        return cUserId;
    }

    public void setcUserId(Integer cUserId) {
        this.cUserId = cUserId;
    }

    public CmsTemplatePojo() {
        this.cdt = new Date();
    }
}
