package com.liuxiaozhu.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author：Created by liuxiaozhu on 2018/3/24.
 * Email: chenhuixueba@163.com
 * 编译期生成class文件
 * 元注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    /**
     * 路由路径，标识一个路由节点（必须传path）
     */
    String path();

    /**
     * 将路由节点进行分组，可以实现按组动态加载
     * 设置默认值“”
     */
    String group() default "";
}
