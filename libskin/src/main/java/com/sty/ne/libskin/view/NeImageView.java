package com.sty.ne.libskin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.sty.ne.libskin.R;
import com.sty.ne.libskin.ViewsChange;
import com.sty.ne.libskin.bean.AttrsBean;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

public class NeImageView extends AppCompatImageView implements ViewsChange {

    private AttrsBean attrsBean;

    public NeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        attrsBean = new AttrsBean();
        //临时存储
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NeImageView,
                defStyleAttr, 0);
        attrsBean.saveViewResource(typedArray, R.styleable.NeImageView);
        //性能优化，回收
        typedArray.recycle();
    }

    @Override
    public void skinChangeAction() {
        int key = R.styleable.NeImageView[R.styleable.NeImageView_android_src];
        int srcID = attrsBean.getViewResource(key);
        if(srcID > 0) {
            //使用兼容包
            Drawable drawable = ContextCompat.getDrawable(getContext(), srcID);
            setImageDrawable(drawable);
        }
    }
}
