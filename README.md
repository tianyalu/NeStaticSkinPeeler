# 静态换肤实现以及原理分析

[TOC]

静态换肤，也称内置换肤，默认使用系统的（白天/黑夜）模式，通过`res/values`和`res/values-night`目录下不同的颜色文件来起到换肤的效果，不需要皮肤包。

## 一、实现效果图

![image](https://github.com/tianyalu/NeStaticSkinPeeler/raw/master/show/show.gif)  

## 二、实现过程浅析

### 2.1  总体思路 

实现一键全局换肤的整体思路是重写`onCreateView()`方法，在此方法中将布局文件中的系统控件替换为自定义控件，在自定义控件中实现换肤接口，在需要换肤的时候遍历`decorView`中实现了换肤接口的控件，调用其换肤方法实现换肤。

### 2.2 详细步骤

#### 2.2.1 自定义`NeTextView`等控件
自定义`NeTextView`等控件，此类控件继承原生控件，并实现了`ViewsChange`接口。控件在初始化时，保存其自定义属性（主要为`background`和`textColor`等）和其对应的资源`id`到`AttrsBean`的`resourcesMap`中。换肤时，通过`ViewsChange`接口的实现方法，经`AttrsBean`由`resourcesMap`中的资源`id`获取对应资源文件夹（`res/values`或`res/values-night`）下相应的资源：`background drawable`和`textColor`，然后设置给该自定义控件即可完成换肤。  

#### 2.2.2 实现`ActionBarUtils`等工具类
实现`ActionBarUtils`等工具类，该类中的方法获取主题颜色属性资源，即`res/values`或`res/values-night`目录下`colors.xml`中颜色资源，设置给`ActionBar`，每次换肤时调用一次，即可完成换肤。  

#### 2.2.3 所有需要换肤的`Activity`都需继承`SkinActivity`  
该基类的核心在于三点：  

* `onCreate()`方法中提前设置工厂  

  ```java
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
      //我们要抢先一步，比系统还有早，拿到主动权
      LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), this);
      super.onCreate(savedInstanceState);
  }
  ```

* `onCreateView()`方法中将原生控件替换为自定义控件

  ```java
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
  ```

* 设置白天/黑夜模式时，递归遍历`View`，对于实现了`ViewsChange`接口的`View`，调用其接口方法，实现换肤操作

  ```java
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
  ```

  

### 2.3 需要注意的点

在`AndroidManifest.xml`文件中的`activity`标签中设置`android:confiChanges`可以避免换肤时屏幕闪烁。  

```xml
<activity android:name=".MainActivity"
    android:configChanges="uiMode"> <!-- 如果不加这个模式，换肤时屏幕会抖，看起来不爽-->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### 2.4 本文换肤与网上换肤框架的比较

#### 2.4.1 网上换肤框架缺点  
没有搞清楚 系统流程，各种暴力反射，增加了无需的代码；没有做兼容，没有考虑后续维护性；使用几个集合去存储 所有的控件，当焕肤的时候，多重for去遍历集合 （性能很低）；作死的遍，非常暴力，自己去解析布局文件，用多个集合存储，多重for去遍历集合 ，去焕肤（崩溃）。

> 1. 收集所有的控件里面的属性集  
>
> 2. 没有考虑到V7 V4兼容  
> 3. 用两套集合去装控件+属性集合  
> 4. 换肤时，多重for循环，性能损耗
> 5. 代码量太多，写了很多无用代码
> 6. 暴力的方式 mFactorySet

#### 2.4.2 本文优点  

> 1. 知道系统流程，可以节省很多代码，利用系统的功能
> 2. 时时刻刻考虑兼容
> 3. 性能高
> 4. 可扩展性，维护性 高
> 5. 看系统源码，去拦截，灵活的操作

## 三、原理分析

### 3.1 `setContentView(resID)`分析  

resID  --> XmlResourceParser ---> while遍历当前布局里面所有的控件
{
    要添加进去的View View view = createViewFromTag() {
        1.区分是否是自定义控件
        不是自定义控件： android.view.TextView
        无论是系统控件还是自定义控件，最终通过反射实例化
        return view;
    }
    根布局.addView(view);
}

### 3.2 `Factory2`的分析  

系统的原始，我们什么都没有设置： setContentView
一定会调用 系统的onCreateView函数(全部都让系统来控制来) 如果让系统控制来，我们就没法采集所有的控件了

工厂分析的时候  结论 先记住：mFactory2 = factory 就结束了，那么 factory到底是什么=onCreateView函数
又一次分析setContentView  mFactory2.onCreateView()

if (mFactory2 != null) mFactory2  mFactory  都是接口   mFactory2 extends mFactory

本文参考：  
[android暗黑模式学习记录](https://www.yuque.com/docs/share/a80426d9-7607-49c3-9c4d-47e86f972f7c?#)