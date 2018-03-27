package com.liuxiaozhu.router_compiler.processor;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Author：Created by liuxiaozhu on 2018/3/26.
 * Email: chenhuixueba@163.com
 */
public class ExtraProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
