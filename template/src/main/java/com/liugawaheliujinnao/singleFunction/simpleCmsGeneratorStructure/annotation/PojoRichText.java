package com.liugawaheliujinnao.singleFunction.simpleCmsGeneratorStructure.annotation;

import java.lang.annotation.*;

/**
 * @Description: 属性是否需要支持富文本框编辑，反射时候修改 HTML 与 JS
 * @Author: LiugawaHeLiujinnao
 * @Date: 2019-05-20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PojoRichText {
}
