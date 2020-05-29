package com.sty.ne.staticskinpeeler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //之前 会有工厂（布局相关的工厂）
        //分析：布局工厂 是不是 应该
        super.onCreate(savedInstanceState);

        //为什么把布局ID丢进去，之后就能显示页面
        setContentView(R.layout.activity_main);

        //看到画面

        //已经结束
        btnChange = findViewById(R.id.btn_change);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Log.d(TAG, "MainActivity onCreate: 3");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
