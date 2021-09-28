# 静态换肤实现以及原理分析

[TOC]

静态换肤，也称内置换肤，默认使用系统的（白天/黑夜）模式，通过`res/values`和`res/values-night`目录下不同的颜色文件来起到换肤的效果，不需要皮肤包。

## 一、实现效果图

![image](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282014367.gif)  

## 二、实现过程浅析

### 2.1  总体思路 

实现一键全局换肤的整体思路是重写在`Activity`的`onCreate()`方法中`setContentView(...)`之前设置工厂`Factory2`，然后重写`onCreateView()`方法（布局文件从`xml`渲染到屏幕的过程中会先调用`tryCreateView()`方法，该方法中调用`Factory2`的`onCreateView()`方法），在此方法中将布局文件中的系统控件替换为自定义控件，在自定义控件中实现换肤接口，在需要换肤的时候递归遍历`decorView`中实现了换肤接口的控件，调用其换肤方法实现换肤。

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

为什么通过这句代码把布局ID丢进去之后就能把`xml`中的布局加载到`Activity`上呢？

点击`setContentView()`跟进源码，一路追踪`AppcompatActivity`-->`AppCompatDelegate`-->`AppCompatDelegateImpl`，在`AppCompatDelegateImpl`中，我们发现`resId`传给了`LayoutInflater`中的`inflate()`方法。  

![set_content_view1](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282015692.png)

继续查看`inflate()`方法  

![inflate2](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282017124.png)

![inflate3](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282017110.png)

我们发现`resId`最终通过`Resources`的`getLayout()`方法将`int`型的资源`id`转换成了一个`XmlResourceParser`对象，该对象是一个xml的解析工具，具体用法可参考这里：[使用XmlResourceParser动态解析XML](https://www.jianshu.com/p/36b04fcf3d38)。

然后再一次调用`inflate()`方法，并将转换后的`XmlResourceParser`对象传入。我们继续查看inflate()方法：

![inflate4](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282017989.png)

![inflate5](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282017716.png)    

在该方法中，如果`xml`的根布局为`<merge/>`标签的话，调用`rInflate()`方法递归创建`View`，否则的话就调用`createViewFromTag()`方法和`rInflateChildren()`方法，该方法又会调用`rInflate()`方法。我们先看一下`rInflate()`方法做了什么：

![rinflate6](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282018393.png)  

可以看到该方法先是做了一些检查，然后同样调用了`createViewFromTag()`方法和`rInflateChildren()`方法，我们先进入`createViewFromTag()`方法：

![create_view_from_tag7](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282019342.png)

这里我们关注两点，首先是调用`tryCreateView()`方法创建`View`；其次如果为空的话，会调用`onCreateView()`或`createView()`方法来创建`View`。

这里可以看到一个知识点是系统如何区分这个View到底是系统控件还是自定义控件呢？关键在于`-1 == name.indexOf('.')`这个判断条件，由此我们可以联想到系统控件是不需要加完整包名的，如`TextView`，而我们的自定义控件则必须使用完整包名，如`com.xxx.xxx.MyView`。这里没有`.`的就是系统控件，有`.`的就是自定义控件。对于系统控件，调用`onCreateView()`来创建`View`，对于自定义控件，则调用`createView()`方法来创建`View`。

我们先看一下`tryCreateView()`方法做了什么：  

![try_create_view8](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282019912.png)

在`tryCreateView()`中用到了几个工厂`mFactory`、`mFactory2`和`mPrivateFactory`，我们来看一下这几个工厂：

```java
    @UnsupportedAppUsage
    private Factory mFactory;
    @UnsupportedAppUsage
    private Factory2 mFactory2;
    @UnsupportedAppUsage
    private Factory2 mPrivateFactory;

    public interface Factory {
        /**
         * Hook you can supply that is called when inflating from a LayoutInflater.
         * You can use this to customize the tag names available in your XML
         * layout files.
         *
         * <p>
         * Note that it is good practice to prefix these custom names with your
         * package (i.e., com.coolcompany.apps) to avoid conflicts with system
         * names.
         *
         * @param name Tag name to be inflated.
         * @param context The context the view is being created in.
         * @param attrs Inflation attributes as specified in XML file.
         *
         * @return View Newly created view. Return null for the default
         *         behavior.
         */
        @Nullable
        View onCreateView(@NonNull String name, @NonNull Context context,
                @NonNull AttributeSet attrs);
    }

    public interface Factory2 extends Factory {
        /**
         * Version of {@link #onCreateView(String, Context, AttributeSet)}
         * that also supplies the parent that the view created view will be
         * placed in.
         *
         * @param parent The parent that the created view will be placed
         * in; <em>note that this may be null</em>.
         * @param name Tag name to be inflated.
         * @param context The context the view is being created in.
         * @param attrs Inflation attributes as specified in XML file.
         *
         * @return View Newly created view. Return null for the default
         *         behavior.
         */
        @Nullable
        View onCreateView(@Nullable View parent, @NonNull String name,
                @NonNull Context context, @NonNull AttributeSet attrs);
    }
```

这两个工厂其实就是接口，而且`Factory2`是继承自`Factory`的，里面都有一个`onCreateView()`方法。

回过头来继续看`tryCreateView()`，我们发现如果这几个工厂有不为空的话，就调用它的`onCreateView()`方法来创建`View`，否则的话就返回空的`View`。即`tryCreateView()`方法尝试用工厂接口来创建`View`，如果创建的`View`不为空的话，就不会走系统的创建`View`的过程了，否则走系统的创建`View`的过程。

假如我们没有设置工厂的话，继续分析系统创建`View`的流程，先看创建系统控件`View`的`onCreateView()`方法：

![on_create_view9](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282020314.png)

![on_create_view10](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282020857.png)

![on_create_view11](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282020721.png)

这里我们可以看到在`createView()`方法中传入了`android.view.`字符串参数，然后看一下在这个方法里面做了什么：

![create_view12](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282021559.png)

![create_view13](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282021548.png)

![create_view14](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282021807.png)

我们可以看到在最终的`createView()`方法中传进来的`prefix`参数`android.view.`和`name`参数拼接成系统`View`的完整路径名，如`android.view.TextView`，并通过反射拿到实例对象，并在854行将通过反射创建出来的`View`返回出去，这样就完成了系统自定义控件从`XML`到`View`的创建流程。

再看看自定义`View`和系统控件`View`的区别：

![create_view_from_tag15](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282022729.png)

可以看到自定义`View`和系统`View`的创建流程其实是一样的，不过这里没有传前缀`prefix`参数，这样通过反射创建`View`的时候就不用拼接前缀了，直接用完整路径就可以拿到对象实例，直接创建`View`了。

**总结：**
```java
resID  --> XmlResourceParser ---> while遍历当前布局里面所有的控件  
{  
    要添加进去的View view = createViewFromTag() {  
        1.区分是否是自定义控件  
        不是自定义控件： android.view.TextView  
        无论是系统控件还是自定义控件，最终通过反射实例化  
        return view;  
    }  
    根布局.addView(view);  
}  
```

### 3.2 `Factory2`的分析  

从3.1的分析中有这样一个疑问：为什么会有`tryCreateView()`这个方法呢，直接走`onCreateView()`创建`View`不就行了吗？

因为`android`为开发者提供了一个可以自行决定如何创建`View`的接口，即`LayoutInflater.Factory2`，在`tryCreateView()`中当`mFactory`不为空的时候就会走`Factory`中的`onCreateView()`方法，开发者重写`onCreateView()`方法就可以决定`View`的创建方式，以及创建什么样的`View`。

通过查看`Activity`源码我们可以发现`Activity`是实现了`LayoutInflater.Factory2`接口的，所以在`SkinActivity`中可以直接重写`onCreateView()`方法。

![activity_factory2_16](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282022100.png)

不过重写`onCreateView()`还不够，还需要通过：  

```java
LayoutInflater.from(this).setFactory2(this);
```

将自己实现的`LayoutInflater.Factory2`接口设置给`LayoutInflater`才能让自己的`onCreateView()`方法代替系统的`onCreateView()`方法。

需要注意的是`setFactory2()`方法需要写在`super.onCreate(saveInstanceState)`之前：

```java
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //我们要抢先一步，比系统还有早，拿到主动权
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), this);
        super.onCreate(savedInstanceState);
    }
```

因为`Activity`的`onCreate()`方法中会取设置一遍`factory`:  

![install_factory17](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282022425.png)

![install_factory18](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282023111.png)

可见，如果我们提前设置了`factory`，系统就不会再设置了。

![set_factory19](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282023743.png)

![set_factory20](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202109282023002.png)

可以看到368行有一个`mFactorySet`标记，这个标记的初始值为`false`，一旦设置过一次`factory`，这个标记会在374行设置为`true`，下次再设置时，会在369行抛出异常。所以要在`Activity`的`super.onCreate()`方法之前调用`setFactory()`方法。


**这就解释了2.2.3中为什么要在`onCreate()`方法中提前设置工厂，并且为什么要复写`onCreateView()`方法，而且在该方法中可以替换原生控件为自定义控件了。**

本文参考：  
[android暗黑模式学习记录](https://www.yuque.com/docs/share/a80426d9-7607-49c3-9c4d-47e86f972f7c?#)