package com.liuxiaozhu.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author：Created by liuxiaozhu on 2018/3/24.
 * Email: chenhuixueba@163.com
 * 编译期生成class文件
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Extra {
    String name() default "";
}
