package com.liuxiaozhu.router_compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Author：Created by liuxiaozhu on 2018/3/26.
 * Email: chenhuixueba@163.com
 * 判断集合是否为空
 */

public class Utils {

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
