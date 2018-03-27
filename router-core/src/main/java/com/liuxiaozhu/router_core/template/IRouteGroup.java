package com.liuxiaozhu.router_core.template;


import com.liuxiaozhu.router_annotation.model.RouteMeta;

import java.util.Map;

/**
 *
 */
public interface IRouteGroup {

    void loadInto(Map<String, RouteMeta> atlas);
}
