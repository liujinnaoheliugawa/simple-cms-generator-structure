package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation;

import java.lang.annotation.*;

/**
 * @Description: 类 ID 属性标志，反射时使用
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PojoId {
}
