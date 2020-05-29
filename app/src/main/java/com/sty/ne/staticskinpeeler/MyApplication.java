package com.sty.ne.staticskinpeeler;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyApplication onCreate  1");

        //监听Activity生命周期方法
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            //切块
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                Log.d(TAG, "MyApplication onActivityCreated 2");

                //可以在这里做换肤初始化工作
                //凡是想在生命周期函数执行之前干活，都可以在这里干
            }

            //切块
            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            //切块
            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            //切块
            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            //切块
            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            //切块
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            //切块
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }
}
