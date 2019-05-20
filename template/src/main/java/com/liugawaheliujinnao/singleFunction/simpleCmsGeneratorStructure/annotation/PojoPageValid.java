package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation;

import java.lang.annotation.*;

/**
 * @Description: 属性是否需要在 web 端验证，反射在修改 js
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PojoPageValid {
}
