# DevBox
A simple app framework for myself.

# Feature
For now it's really SIMPLE. I have those classes to quicker development:

UI

    |BaseActivity
    |-BasePagerActivity
    |BaseFragment
    |-BasePagerFragment
    |-BaseRecyclerFragment

>BaseActivity

is a Activity extended from AppCompatActivity, which support Toolbar with LinearLayout or Framelayout, translucent status bar and nav bar. Also, I introduced a class named ActivityConfig, and a fragment version especially for my BaseRecyclerFragment, which integrated a set of visual configs, like the functions I've talked above.

>BasePagerActivity

Extended from BaseActivity with ViewPager support

>BaseFragment

Extended from android.support.v4.app.Fragment, support Auto dataload management, LazyDataLoad, Custom Title, Loading dialog. BaseFragment will start load data by specified method automatically, with LazyDataload, it will begin load at setUserVisibleHint(), or onResume() if lazyDataload set to false.

>BaseRecyclerFragment

RecyclerView implements for BaseFragment, which contains SwipeRefreshLayout and a simple pager controll inside. And I made a BaseRecyclerAdapter which extends RecyclerView.Adapter<ViewHolder> in it and you can simply override the funtions I exposed like createViewHolder(ViewGroup parent, int viewType), bindViewHolder(RecyclerView.ViewHolder holder, int position), getViewType(int position), getCount() to complete adapter methods. One more thing, scroll end to load more is also supported.

UTILS

    ViewUtils

> ViewUtils support methods for views, like dp-px converter, helper method for keyboard close/open and create a MaterialDialog.

COMMON

    AppConfig
    AppDelegate

>AppConfig is a helper class to access SharedPreference and AppDelegate is a class to hold your App Application, usage will be shown below.

# Usage
To use this framework, you need to init this module on your Custom Application using

    AppDelegate.install(this);

like this:

    public class App extends Application {

        @Override
        public void onCreate() {
            super.onCreate();

            // init module
            AppDelegate.install(this);
        }
    }

and now, you can using this framework as your wish.


