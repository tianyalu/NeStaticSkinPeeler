package com.sty.ne.libskin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.sty.ne.libskin.R;
import com.sty.ne.libskin.ViewsChange;
import com.sty.ne.libskin.bean.AttrsBean;

public class NeLinearLayout extends LinearLayoutCompat implements ViewsChange {
    private AttrsBean attrsBean;
    public NeLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        attrsBean = new AttrsBean();
        //临时存储
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NeLinearLayout,
                defStyleAttr, 0);
        attrsBean.saveViewResource(typedArray, R.styleable.NeLinearLayout);
        //性能优化，回收
        typedArray.recycle();
    }

    @Override
    public void skinChangeAction() {
        int key = R.styleable.NeLinearLayout[R.styleable.NeLinearLayout_android_background];
        int backgroundID = attrsBean.getViewResource(key);
        if(backgroundID > 0) {
            //使用兼容包
            Drawable drawable = ContextCompat.getDrawable(getContext(), backgroundID);
            setBackground(drawable);
        }
    }
}
