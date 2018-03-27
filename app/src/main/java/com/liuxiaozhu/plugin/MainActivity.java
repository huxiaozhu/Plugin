package com.liuxiaozhu.plugin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.liuxiaozhu.router_annotation.Route;
import com.liuxiaozhu.router_core.DNRouter;

/**
 * 测试插件化的DEMO
 */
@Route(path="/app/main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        DNRouter.init(getApplication());
    }
}
