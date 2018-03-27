package com.liuxiaozhu.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.liuxiaozhu.router_annotation.Route;
import com.liuxiaozhu.router_annotation.model.RouteMeta;
import com.liuxiaozhu.router_compiler.utils.Constens;
import com.liuxiaozhu.router_compiler.utils.Log;
import com.liuxiaozhu.router_compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Author：Created by liuxiaozhu on 2018/3/26.
 * Email: chenhuixueba@163.com
 */

/**
 * 使用google注解处理器工具动态注册该类
 * 该类不会打包到apk文件
 */
@AutoService(Processor.class)

/**
 * 指定使用的Java版本
 * 替代 {@link AbstractProcessor#getSupportedSourceVersion()} 函数
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 注册给哪些注解的(指明当前注解处理器能够处理的注解)
 * 替代 {@link AbstractProcessor#getSupportedAnnotationTypes()} 函数
 */
@SupportedAnnotationTypes({Constens.ANN_TYPE_ROUTE})
/**
 * 处理器接收的参数
 * 替代 {@link AbstractProcessor#getSupportedOptions()} 函数
 */
@SupportedOptions(Constens.ARGUMENTS_NAME)
public class RouteProcessor extends AbstractProcessor {

    private Log log;
    /**
     * 节点工具类 (类、函数、属性都是节点)
     */
    private Elements elementUtils;
    /**
     * type(类信息)工具类
     */
    private Types typeUtils;
    /**
     * 类/资源生成器
     */
    private Filer filerUtils;
    /**
     * 分组 key:组名 value:对应组的路由信息
     */
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();
    /**
     * key:组名 value:类名
     */
    private Map<String, String> rootMap = new TreeMap<>();
    private String moduleName;


    /**
     * 相当于构造函数
     * 初始化
     * 从 {@link ProcessingEnvironment} 中获得一系列处理器工具
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //获得apt的日志输出
        log = Log.newLog(processingEnv.getMessager());
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filerUtils = processingEnv.getFiler();
        //参数是模块名 为了防止多模块/组件化开发的时候 生成相同的 xx$$ROOT$$文件
        Map<String, String> options = processingEnv.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Constens.ARGUMENTS_NAME);
        }
        log.i("RouteProcessor Parmaters:" + moduleName);
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set Processor Parmaters.");
        }
    }

    /**
     * 相当于main函数,正式处理注解
     *
     * @param annotations 节点集合（使用注解的节点，使用注解的set集合）
     * @param roundEnv    表示当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理(已经处理)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //节点集合为空时交给系统处理
        //不为空拦截处理
        if (!Utils.isEmpty(annotations)) {
            Set<? extends Element> roundEnvironment = roundEnv.getElementsAnnotatedWith(Route.class);
            if (!Utils.isEmpty(roundEnvironment)) {
                processRoute(roundEnvironment);
            }
            return true;
        }
        return false;
    }

    /**
     * 处理被注解的 节点
     * @param roundEnvironment
     */
    private void processRoute(Set<? extends Element> roundEnvironment) {
        //获得类的节点信息
        TypeElement activity = elementUtils.getTypeElement(Constens.ACTIVITY);
        //暴露出接口
        TypeElement service = elementUtils.getTypeElement(Constens.ISERVICE);
        for (Element e : roundEnvironment) {
            RouteMeta routeMeta;
            //类节点的反射
            TypeMirror typeMirror = e.asType();
            log.i("Route class" + typeMirror.toString());
            //获取自定义注解的信息
            Route route = e.getAnnotation(Route.class);
            //判断是否在activity上使用
            if (typeUtils.isSubtype(typeMirror, activity.asType())) {
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, e);
            } else if (typeUtils.isSubtype(typeMirror, service.asType())) {
                routeMeta = new RouteMeta(RouteMeta.Type.ISERVICE, route, e);
            } else {
                throw new RuntimeException("[just support] Activity Route:" + e);
            }
            categories(routeMeta);
        }
        //groupMap
        TypeElement iRouteGroup = elementUtils.getTypeElement(Constens.IROUTE_GROUP);
        TypeElement iRouteRoot = elementUtils.getTypeElement(Constens.IROUTE_ROOT);
        //生成$$Group$$ 记录分组表,实现接口IRouteGroup
        generatedGroup(iRouteGroup);
        //生成$$Rout$$ 记录路由表
        try {
            /**
             * 生成Root类 作用:记录 <分组，对应的Group类>
             */
            generatedRoot(iRouteRoot, iRouteGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generatedRoot(TypeElement iRouteRoot, TypeElement iRouteGroup) throws IOException {
        //类型 Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                )
        );

        //参数 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec rootParamSpec = ParameterSpec.builder(routes, "routes")
                .build();
        //函数 public void loadInfo(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder
                (Constens.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(rootParamSpec);

        //函数体
        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            loadIntoMethodOfRootBuilder.addStatement("routes.put($S, $T.class)", entry
                    .getKey(), ClassName.get(Constens.PACKAGE_OF_GENERATE_FILE, entry.getValue
                    ()));
        }
        //生成 $Root$类
        String rootClassName = Constens.NAME_OF_ROOT + moduleName;
        JavaFile.builder(Constens.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootClassName)
                        .addSuperinterface(ClassName.get(iRouteRoot))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(loadIntoMethodOfRootBuilder.build())
                        .build()
        ).build().writeTo(filerUtils);
        log.i("Generated RouteRoot: " + Constens.PACKAGE_OF_GENERATE_FILE + "." + rootClassName);
    }


    /**
     * 生成Group表
     *
     * @param iRouteGroup
     */
    private void generatedGroup(TypeElement iRouteGroup) {
        //创建参数类型 Map<String,RouteMeta>
        ParameterizedTypeName parameterizedType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class)
        );
        //创建参数 Map<String,RouteMeta> atlas
        ParameterSpec atlas = ParameterSpec.builder(parameterizedType, "atlas").build();
        //遍历分组  每个分组创建一个 $$Group$$类
        for (Map.Entry<String, List<RouteMeta>> entry : groupMap.entrySet()) {
            //创建方法
            MethodSpec.Builder method = MethodSpec.methodBuilder("loadInto")
                    .addModifiers(Modifier.PUBLIC)//方法类型public
                    .returns(TypeName.VOID)//方法返回值void
                    .addAnnotation(Override.class)//方法注解
                    .addParameter(atlas);//方法参数
            String groupName = entry.getKey();
            List<RouteMeta> groupData = entry.getValue();
            for (RouteMeta routeMeta : groupData) {
                //添加方法中的代码
                //atlas.put("/main/test", RouteMeta.build(RouteMeta.Type.ACTIVITY,SecondActivity.class, "/main/test", "main"));
                // $S = String(字符串"")，$占位符
                // $T = Class 类
                // $L = 字面量（不加""）
                method.addStatement(
                        "atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
                        routeMeta.getPath(),//替换$S
                        ClassName.get(RouteMeta.class),//替换$T
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath(),
                        routeMeta.getGroup());
            }
//            类名
            String className = Constens.NAME_OF_GROUP + groupName;
            //创建类
            TypeSpec typeSpec = TypeSpec.classBuilder(className)
                    .addSuperinterface(ClassName.get(iRouteGroup))//类实现接口IRouteGroup
                    .addModifiers(Modifier.PUBLIC)//类的类型public
                    .addMethod(method.build())//添加方法
                    .build();
            //生成java文件
            JavaFile javaFile = JavaFile.builder(Constens.PACKAGE_OF_GENERATE_FILE, typeSpec).build();
            //将java文件存放指定位置
            try {
                javaFile.writeTo(filerUtils);
            } catch (IOException e) {
                e.printStackTrace();
            }
            rootMap.put(groupName, className);
        }
    }

    /**
     * 检查是否配置 Group 如果没有配置 则从path能取出组名
     *
     * @param routeMeta
     */
    private void categories(RouteMeta routeMeta) {
        if (routeVerify(routeMeta)) {
            log.i("Group：" + routeMeta.getGroup() + "，path：" + routeMeta.getPath());
            //分组与组中的路由信息
            List<RouteMeta> routeMetaList = groupMap.get(routeMeta.getGroup());
            if (Utils.isEmpty(routeMetaList)) {
                routeMetaList = new ArrayList<>();
                routeMetaList.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetaList);
            } else {
                routeMetaList.add(routeMeta);
            }
        } else {
            log.i("Group Info Error" + routeMeta.getPath());
        }

    }

    /**
     * 检查注解的格式是否正确
     *
     * @param routeMeta
     */
    private boolean routeVerify(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        //path必须以/开头来指定路由地址
        if (!path.startsWith("/")) {
            return false;
        }
        //如果group没有配置，我们从path中获得group
        if (Utils.isEmpty(group)) {
            String defultGroup = path.substring(1, path.indexOf("/", 1));
            if (Utils.isEmpty(defultGroup)) {
                return false;
            }
            routeMeta.setGroup(defultGroup);
        }
        return true;
    }
}
