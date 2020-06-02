package com.sty.ne.staticskinpeeler;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sty.ne.libskin.SkinActivity;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends SkinActivity {
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
                onBtnChangeClicked();
            }
        });
        Log.d(TAG, "MainActivity onCreate: 3");
    }

    /**
     * 切换白天和黑夜模式，利用了兼容库（本身就实现了黑夜和白天模式的管理）
     */
    private void onBtnChangeClicked() {
        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (uiMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean openSkin() {
        return true;
    }


}
