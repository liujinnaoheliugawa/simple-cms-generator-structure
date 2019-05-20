package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.exception;

/**
 * @Description: 当 Pojo 反射解析出现错误时报出
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
public class PojoException extends Exception {

    private int value;

    public PojoException() {
        super();
    }

    public PojoException(String msg, int value) {
        super(msg);
        this.value=value;
    }

    public PojoException(String msg) {
        super(msg);
        this.value = -1;
    }

    public int getValue() {
        return value;
    }

    public String getMsg() {
        return super.getMessage();
    }
}
