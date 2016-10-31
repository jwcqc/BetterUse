package me.hyman.betteruse.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.fragment.setting.AdvancedSettingFragment;
import me.hyman.betteruse.ui.fragment.setting.BasicSettingFragment;

public class SettingActivity extends BaseAppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    @Override
    protected void onCreateView(Bundle bundle) {

        setRunningActivity(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("设置");
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.content_setting, new BasicSettingFragment()).commit();

        tabLayout = (TabLayout) findViewById(R.id.tabs_setting);

        final List<String> tabList = new ArrayList<>();
        tabList.add("基本设置");
        tabList.add("高级设置");
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab().setText(tabList.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tabList.get(1)));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals(tabList.get(0))) {
                    getFragmentManager().beginTransaction().replace(R.id.content_setting, new BasicSettingFragment()).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.content_setting, new AdvancedSettingFragment()).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
