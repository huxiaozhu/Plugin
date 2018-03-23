# Plugin
测试组件化的Demo

1.在项目的根目录下创建一个新的Gradle，并在系统级Gradle里面引用
（apply from: "base.gradle"）新建的Gradle。类似于c里的incloud xxx

2. 在base.gradle的ext {  }扩展项里面配置基本信息，如：minSdkVersion    : 19,targetSdkVersion : 26,

3.在app.Gradle中拿到base.gradle里的数据，配置常用的版本信息
//定义一个变量用rootProject得到Project对象的实例
//通过实例可以获取Base.Gradle的扩展项
def cfg= rootProject.ext.android
def appId= rootProject.ext.appId
//将base.gradle中的基本数据设置进来。两种方式均可
applicationId appId["app"]
minSdkVersion cfg.minSdkVersion  

4.为各个lib的Gradle配置集成模式和组件模式
   （1）首先根据isModule判断是当前moudle是组合还是集成模式  
    if (isModule) {  
        //集成模式
        apply plugin: 'com.android.library'  
    } else {  
        //组件模式  
        apply plugin: 'com.android.application'  
    }  
    （2）为组件模式配置appid  
    （3）根据isModule加载不同的manifest，组件模式设置MainActivity
        sourceSets {  
                    main {  
                        if (!isModule) {  
                            //组件模式下加载带Main函数的manifest  
                            manifest.srcFile 'src/main/debug/AndroidManifest.xml'  
                        } else {  
                            //集成模式
                            manifest.srcFile 'src/main/java/AndroidManifest.xml'  
                        }  
                    }  
                }  
     其他配置同3  
     
5.

