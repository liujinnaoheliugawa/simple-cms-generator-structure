package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation;

import java.lang.annotation.*;

/**
 * @Description: 属性描述，反射在页面上显示
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PojoAttrDes {

    String des() default "";

    String desEn() default "";

    String example() default "";
}
