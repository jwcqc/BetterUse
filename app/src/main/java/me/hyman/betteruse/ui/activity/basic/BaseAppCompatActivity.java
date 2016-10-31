package me.hyman.betteruse.ui.activity.basic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.StatusBarCompat;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.MainActivity;
import me.hyman.betteruse.ui.fragment.basic.AppBaseFragment;
import me.hyman.betteruse.ui.view.MyToolbar;


public abstract class BaseAppCompatActivity extends AppCompatActivity implements MyToolbar.onToolbarDoubleClick {

    protected static final String TAG = BaseAppCompatActivity.class.getSimpleName();

    private static BaseAppCompatActivity runningActivity;

    private int theme = 0;// 当前界面设置的主题

    // 当有Fragment Attach到这个Activity的时候，就会保存
    private Map<String, WeakReference<AppBaseFragment>> fragmentRefs;

    /**
     * 布局文件ID
     */
    protected abstract int getContentViewId();

    /**
     * 由子类去实现，用于设置主题
     * @return
     */
    protected abstract int configTheme();

    /**
     * 由子类去实现，
     * @return
     */
    protected abstract void onCreateView(Bundle bundle);

    /**
     * 需要处理Intent时子类可重写
     * @param intent
     */
    protected void handleIntent(Intent intent) { }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            theme = configTheme();
        } else {
            theme = savedInstanceState.getInt("theme");
        }

        // 奇怪，如果不调用reload而是recreate()，主题并没有立即修改到，而需要再这里调用如下：
        // theme = ThemeUtils.themeArr[AppSettings.getThemeColor()][1];

        // 设置主题
        if (theme > 0) {
            setTheme(theme);
        }

        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        initStatusBar();

        if(getIntent() != null) {
            handleIntent(getIntent());
        }

        // 子类实现
        onCreateView(savedInstanceState);

        fragmentRefs = new HashMap<String, WeakReference<AppBaseFragment>>();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void addFragment(String tag, AppBaseFragment fragment) {
        fragmentRefs.put(tag, new WeakReference<AppBaseFragment>(fragment));

       /* if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(getFragmentContentId(), fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commitAllowingStateLoss();
        }*/
    }

    public void removeFragment(String tag) {
        fragmentRefs.remove(tag);

        /*if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }*/
    }

    /**
     * 设置状态栏透明
     */
    private void initStatusBar() {
        if(!this.getClass().equals(MainActivity.class)) {
            StatusBarCompat.translucentStatusBar(this);
        }
    }

    public void initToolbar(Toolbar toolbar, String title, int icon) {
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(icon);
    }

    @Override
    public boolean onToolbarDoubleClick() {
        Set<String> keys = fragmentRefs.keySet();
        for(String key : keys) {
            WeakReference<AppBaseFragment> fragmentRef = fragmentRefs.get(key);
            AppBaseFragment fragment = fragmentRef.get();
            if(fragment != null && fragment instanceof MyToolbar.onToolbarDoubleClick) {
                if(((AppBaseFragment)fragment).onToolbarDoubleClick()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static BaseAppCompatActivity getRunningActivity() {
        return runningActivity;
    }

    public static void setRunningActivity(BaseAppCompatActivity runningActivity) {
        BaseAppCompatActivity.runningActivity = runningActivity;
    }


    // 返回键返回事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(KeyEvent.KEYCODE_BACK == keyCode) {
            if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}

