package com.sty.ne.libskin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.sty.ne.libskin.view.NeButton;
import com.sty.ne.libskin.view.NeImageView;
import com.sty.ne.libskin.view.NeLinearLayout;
import com.sty.ne.libskin.view.NeRelativeLayout;
import com.sty.ne.libskin.view.NeTextView;

//兼容
public class MyAppCompatViewInflater {
    private String name; //控件名称：Button TextView...
    private Context context; //环境
    private AttributeSet attrs; //控件对应的属性集合

    public MyAppCompatViewInflater(Context context) {
        this.context = context;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttrs(AttributeSet attrs) {
        this.attrs = attrs;
    }

    public View createViewAction() {
        View resultView = null;

        switch (name) {
            case "TextView":
                resultView = new NeTextView(context, attrs);
                break;
            case "ImageView":
                resultView = new NeImageView(context, attrs);
                break;
            case "Button":
                resultView = new NeButton(context, attrs);
                break;
            case "LinearLayout":
                resultView = new NeLinearLayout(context, attrs);
                break;
            case "RelativeLayout":
                resultView = new NeRelativeLayout(context, attrs);
                break;
            default:
                break;
        }
        return resultView; //可能为null
    }
}
