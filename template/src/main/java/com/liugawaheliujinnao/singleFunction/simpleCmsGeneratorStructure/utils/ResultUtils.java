package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.utils;

import java.io.Serializable;

/**
 * @Description: 分页信息类
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public class ResultUtils implements Serializable {
    private static final long serialVersionUID = 6160978622237581462L;

    private Integer code;
    private String msg;
    private Long count = 0L;
    private Object data;

    public ResultUtils() {
        super();
    }

    public ResultUtils(Integer code) {
        super();
        this.code = code;
    }

    public ResultUtils(Integer code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static ResultUtils ok() {
        return new ResultUtils(0);
    }

    public static ResultUtils ok(Object list) {
        ResultUtils result = new ResultUtils();
        result.setCode(0);
        result.setData(list);
        return result;
    }

    public static ResultUtils ok(String msg) {
        ResultUtils result = new ResultUtils();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }

    public static ResultUtils error(String msg) {
        return new ResultUtils(500, msg);
    }
}
