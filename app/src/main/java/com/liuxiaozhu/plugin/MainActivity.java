package com.liuxiaozhu.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.liuxiaozhu.router_annotation.Route;
import com.liuxiaozhu.router_core.DNRouter;

import java.io.File;


/**
 * 测试插件化的DEMO
 */
@Route(path="/app/main")
public class MainActivity extends AppCompatActivity {
    EditText editText;
    InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DNRouter.init(getApplication());
        editText = findViewById(R.id.edittext);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * 获取焦点
     * @param view
     */
    public void get(View view) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);//设置触摸聚焦
        editText.requestFocus();//请求焦点
        editText.findFocus();//获取焦点
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);// 显示输入法
    }

    /**
     * 失去焦点
     * @param view
     */
    public void cancle(View view) {
        editText.setFocusable(false);//设置输入框不可聚焦，即失去焦点和光标
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);// 隐藏输入法
        }
    }

}
