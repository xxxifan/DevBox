# DevBox
一个集成了很多功能的app框架。

# Feature
目前这个框架还比较简单，我主要是写了这些类来简洁开发：

UI

    |BaseActivity
         --BasePagerActivity
         --BaseDrawerActivity
    |BaseFragment
         --BasePagerFragment
         --BaseRecyclerFragment
             --SimpleRecyclerFragment
             --SwipeRecyclerFragment

下面单独介绍一下每个类的功能

>BaseActivity

继承自 AppCompatActivity, 支持父布局为 LinearLayout 或者 Framelayout 设置 Toolbar,
透明状态栏，透明导航栏。同时，我将一个类叫做 ActivityConfig，来实现上面所述功能，从而
达到实现根据不同需求来进行配置的效果。

>BasePagerActivity

继承自 BaseActivity, 支持ViewPager。

>BaseFragment

继承自 android.support.v4.app.Fragment, 支持数据加载管理，懒加载，自定义标题（配合ViewPager）
加载对话框等。

这里要提一下数据加载功能，默认情况下，我们在 onResume() 实现自动加载数据，在 ViewPager
中，是默认开启了懒加载支持的，所以会在 setUserVisibleHint() 方法被调用后加载数据。

为了灵活控制，这里将数据加载管理类抽出来作为 DataLoadManager 来使用。

>BaseRecyclerFragment

一个简单的 RecyclerView 的实现在 BaseFragment 中。特点是将adapter跟fragment结合到了
一起，方便使用。为了支持多种下拉刷新布局，我特意将齐抽出来，只需要继承这个类，在你的布局
中包含 R.id.fragment_recycler_swipe_layout 并实现相关方法即可。

UTILS

    包含了很多开发相关的工具类，请看 javadoc 或源码

# 基本使用
为了使这个框架能用，你必须先初始化这个库在你自定义的 Application 里面用这个方法

    AppDelegate.install(this);

像这样：

    public class App extends Application {

        @Override
        public void onCreate() {
            super.onCreate();

            // init module
            AppDelegate.install(this);
        }
    }

然后就可以开始使用这个框架了。

## ActivityConfig
是 BaseActivity 的一部分，能够快速实现一些界面效果。只需要在 onConfigureActivity 方法
里面设置即可，不设置会按照默认值进行配置。

如：

    @Override
    protected void onConfigureActivity(ActivityConfig config) {
        super.onConfigureActivity(config);
        config.setToolbarTransparent(true);
    }

## UIController
是自己提出的一个概念，相当于把一些比较重的 UI 控件分模块，并通过一些公开的方法或接口与
Activity 进行交互。

## 一些在 BaseActivity 中便利的方法或者需要实现的方法

必须实现的

> onConfigureActivity()
> getLayoutId()
> initView()
> getSimpleName()

Protected

> setupToolbar() 在初始化界面时进行配置 Toolbar
> onCreateFragment() 在 onCreate() 中最后会触发这个方法来帮助你创建或配置 fragment
> setContainerFragment() 设置 fragment 到有 ContainerId 的布局上去
> getFragment() findFragmentByTag 的简便方法
> registerUiControllers() 注册 UIController 以便在 Activity 生命周期中响应动作

public

> getCompatColor()/getCompatDrawable() 比较推荐的两个获取资源的方法
> setBackKeyListener() 通过这个方法设置的 BackListener, 会在 Activity 的 onBackKeyPressed()
方法中被调用。
