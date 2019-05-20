package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation;

import java.lang.annotation.*;

/**
 * @Description: 类多语言适配，反射在页面中显示
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pojo {

    String value() default "";

    String enValue() default "";
}
