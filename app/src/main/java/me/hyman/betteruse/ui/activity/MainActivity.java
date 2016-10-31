package me.hyman.betteruse.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppContext;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.util.IntentUtils;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.fragment.timeline.CommentFragment;
import me.hyman.betteruse.ui.fragment.timeline.MentionFragment;
import me.hyman.betteruse.ui.fragment.timeline.MsgFragment;
import me.hyman.betteruse.ui.fragment.timeline.TimelineFragment;
import me.hyman.betteruse.ui.view.CircleImageView;
import me.hyman.betteruse.ui.view.MyToolbar;

public class MainActivity extends BaseAppCompatActivity {

    @Bind(R.id.toolbar)
    MyToolbar toolbar;

    @Bind(R.id.id_drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.id_navi_menu)
    NavigationView mNavigationView;

    @Bind(R.id.txt_header_account_name)
    TextView txtAccountName;

    @Bind(R.id.txt_header_account_desc)
    TextView txtAccountDesc;

    @Bind(R.id.img_header_account_head)
    CircleImageView imgAccountHead;

    private Context context;
    private TimelineFragment timelineFragment;
    private MentionFragment mentionFragment;
    private CommentFragment commentFragment;
    private MsgFragment msgFragment;

    private AccountBean loginAccount;

    private long exitTime = 0;

    private int themeColor = 0;


    @Override
    protected void onCreateView(Bundle savedInstanceState) {

        ButterKnife.bind(this);
        context = this;
        setRunningActivity(this);

        initToolbar(toolbar, "首页", R.drawable.ic_menu);

        initNavigationView(mNavigationView);

        setMenuSelection(0); // 默认设置第一个菜单选中

        initData();

        imgAccountHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);*/
            }
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    private void initData() {
        loginAccount = AppContext.loginAccount;
        txtAccountName.setText(loginAccount.getUserName());
        txtAccountDesc.setText(loginAccount.getDescription());
        imgAccountHead.setImageDrawable(loginAccount.getUserIcon());
    }

    private void setMenuSelection(int flag) {
        // 开启一个Fragment事务
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(fragmentTransaction);
        toolbar.setVisibility(View.VISIBLE);
        switch (flag) {
            case 0:
                if(timelineFragment == null) {
                    timelineFragment = TimelineFragment.newInstance();
                    fragmentTransaction.add(R.id.frame_main_content, timelineFragment);
                } else {
                    fragmentTransaction.show(timelineFragment);
                }
                toolbar.setVisibility(View.GONE);
                break;
            case 1:
                if(mentionFragment == null) {
                    mentionFragment = MentionFragment.newInstance();
                    fragmentTransaction.add(R.id.frame_main_content, mentionFragment);
                } else {
                    fragmentTransaction.show(mentionFragment);
                }
                break;
            case 2:
                if(commentFragment == null) {
                    commentFragment = CommentFragment.newInstance();
                    fragmentTransaction.add(R.id.frame_main_content, commentFragment);
                } else {
                    fragmentTransaction.show(timelineFragment);
                }
                break;
            case 3:
                if(msgFragment == null) {
                    msgFragment = MsgFragment.newInstance();
                    fragmentTransaction.add(R.id.frame_main_content, msgFragment);
                } else {
                    fragmentTransaction.show(msgFragment);
                }
                break;
        }

        fragmentTransaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if(timelineFragment != null) {
            transaction.hide(timelineFragment);
        }
        if(mentionFragment != null) {
            transaction.hide(mentionFragment);
        }
        if(commentFragment != null) {
            transaction.hide(commentFragment);
        }
        if(msgFragment != null) {
            transaction.hide(msgFragment);
        }
    }

    private void initNavigationView(NavigationView mNavigationView) {
        //mNavigationView.setCheckItem(R.id.menu_timeline);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);     // 改变item选中状态
                setTitle(menuItem.getTitle()); // 改变页面标题，标明导航状态
                mDrawerLayout.closeDrawers();  // 关闭导航菜单

                switch (menuItem.getItemId()) {
                    case R.id.menu_timeline:
                        //toolbar.setTitle(menuItem.getTitle());
                        setMenuSelection(0);
                        break;
                    case R.id.menu_mention:
                        //toolbar.setTitle(menuItem.getTitle());
                        setMenuSelection(1);
                        break;
                    case R.id.menu_comment:
                        //toolbar.setTitle(menuItem.getTitle());
                        setMenuSelection(2);
                        break;
                    case R.id.menu_private_msg:
                        //toolbar.setTitle(menuItem.getTitle());
                        setMenuSelection(3);
                        break;

                    case R.id.menu_settings:
                        menuItem.setChecked(false);// 改变item选中状态
                        IntentUtils.startSettingActivity(context);
                        break;
                }

                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                IntentUtils.startSettingActivity(context);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        // 微博时间线作为首页
        if(timelineFragment.isHidden()) {
            //toolbar.setTitle("首页");
            setMenuSelection(0);
            Menu menu = mNavigationView.getMenu();
            menu.findItem(R.id.menu_timeline).setChecked(true);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if(currentTime - exitTime > 2000) {
            MyToast.showShort(this, R.string.msg_press_twice_exit_app);
            exitTime = currentTime;
            return;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        themeColor = AppSettings.getThemeColor();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 若设置中更改主题颜色了，则重启
        if(themeColor > 0 && themeColor != AppSettings.getThemeColor()) {
            reload();
        }
    }
}
