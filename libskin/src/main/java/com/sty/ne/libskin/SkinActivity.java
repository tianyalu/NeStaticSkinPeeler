package com.sty.ne.libskin;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sty.ne.libskin.utils.ActionBarUtils;
import com.sty.ne.libskin.utils.NavigationUtils;
import com.sty.ne.libskin.utils.StatusBarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

//换肤Activity
public class SkinActivity extends AppCompatActivity {
    private MyAppCompatViewInflater viewInflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //我们要抢先一步，比系统还有早，拿到主动权
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), this);

        super.onCreate(savedInstanceState);

        //避免错误： 300行代码 暴力反射修改 mFactorySet 为 false
    }

    //此函数 比系统的 onCreateView 函数更早执行，我们就能够采集布局里面所有的View
    //把xml中的系统控件替换成你自己的控件
    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name,
                             @NonNull Context context, @NonNull AttributeSet attrs) {
        if(openSkin()) {
            if(null == viewInflater) {
                viewInflater = new MyAppCompatViewInflater(context);
            }
            viewInflater.setName(name); //TextView
            viewInflater.setAttrs(attrs); //TextView所有的属性集
            return viewInflater.createViewAction(); // 如果返回null,系统判断时 null, 就会走系统的，不影响
        }
        return super.onCreateView(parent, name, context, attrs); //继续正常走系统的
    }

    //默认关闭换肤功能
    public boolean openSkin() {
        return false;
    }

    /**
     * 暴露给使用者使用，换肤操作（内置换肤，没有皮肤包，只有黑夜和白天）
     * @param mode
     */
    public void setDayNightMode(int mode) {
        getDelegate().setLocalNightMode(mode); //兼容包，提供了黑夜，白天模式

        final boolean isPost21 = Build.VERSION.SDK_INT >= 21;
        if(isPost21) {
            StatusBarUtils.forStatusBar(this); //改变状态栏颜色
            ActionBarUtils.forActionBar(this); //标题栏颜色
            NavigationUtils.forNavigation(this); //虚拟按钮栏颜色
        }
        View decorView = getWindow().getDecorView();
        changeSkinAction(decorView);
    }

    /**
     * 使用递归 + 接口调用方式 换肤
     * @param decorView
     */
    private void changeSkinAction(View decorView) {
        if(decorView instanceof ViewsChange) { //只有成为了ViewsChange标准，才有资格换肤
            ViewsChange viewsChange = (ViewsChange) decorView;
            viewsChange.skinChangeAction();
        }

        if(decorView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) decorView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childAt = viewGroup.getChildAt(i);
                changeSkinAction(childAt);
            }
        }
    }
}
