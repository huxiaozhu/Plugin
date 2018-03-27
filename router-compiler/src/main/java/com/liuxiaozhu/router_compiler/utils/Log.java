package com.liuxiaozhu.router_compiler.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Author：Created by liuxiaozhu on 2018/3/26.
 * Email: chenhuixueba@163.com
 * 输出日志的工具类
 */

public class Log {
    //用来打印日志的工具
    private Messager messager;

    public Log(Messager messager) {
        this.messager = messager;
    }

    public static Log newLog(Messager messager) {
        return new Log(messager);
    }

    public void i(String msg) {
        //用Error会出现问题
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
