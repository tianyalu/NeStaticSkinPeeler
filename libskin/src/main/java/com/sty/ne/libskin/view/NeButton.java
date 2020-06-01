package com.sty.ne.libskin.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.sty.ne.libskin.R;
import com.sty.ne.libskin.ViewsChange;
import com.sty.ne.libskin.bean.AttrsBean;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

public class NeButton extends AppCompatButton implements ViewsChange {

    private AttrsBean attrsBean;

    public NeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        attrsBean = new AttrsBean();
        //临时存储
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NeButton,
                defStyleAttr, 0);
        attrsBean.saveViewResource(typedArray, R.styleable.NeButton);
        //性能优化，回收
        typedArray.recycle();
    }

    @Override
    public void skinChangeAction() {
        int key = R.styleable.NeButton[R.styleable.NeButton_android_background];
        int backgroundID = attrsBean.getViewResource(key);
        if(backgroundID > 0) {
            //使用兼容包
            Drawable drawable = ContextCompat.getDrawable(getContext(), backgroundID);
            setBackgroundDrawable(drawable);
        }

        key = R.styleable.NeButton[R.styleable.NeButton_android_textColor];
        int textColorID = attrsBean.getViewResource(key);
        if(textColorID > 0) {
            //使用兼容包
            ColorStateList color = ContextCompat.getColorStateList(getContext(), textColorID);
            setTextColor(color);
        }
    }


}
