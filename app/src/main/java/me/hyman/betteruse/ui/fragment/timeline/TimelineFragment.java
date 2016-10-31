package me.hyman.betteruse.ui.fragment.timeline;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.hyman.betteruse.R;
import me.hyman.betteruse.support.util.IntentUtils;
import me.hyman.betteruse.ui.fragment.basic.AppBaseFragment;
import me.hyman.betteruse.ui.view.MyToolbar;

public class TimelineFragment extends AppBaseFragment {

    @Bind(R.id.tabLayout_timeline)
    TabLayout tabLayout;

    @Bind(R.id.viewPager_timeline)
    ViewPager viewPager;

    @Bind(R.id.toolbar_frag_timeline)
    MyToolbar toolbar;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    public final String[] TITLES = {
         "所有分组", "同学", "互联网", "新闻", "杂七杂八"
    };

    public static TimelineFragment newInstance() {
        TimelineFragment fragment = new TimelineFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, view);

        initToolbar(toolbar, "首页", R.drawable.ic_menu, R.menu.menu_main, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_settings:
                        IntentUtils.startSettingActivity(getActivity());
                        break;
                }
                return true;
            }
        });

        initViewPager();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onFABClickedListener != null) {
                    onFABClickedListener.onFABClicked();
                }
            }
        });

        return view;
    }

    private void initViewPager() {
        MyTimelineAdpater adapter = new MyTimelineAdpater(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(20);

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private class MyTimelineAdpater extends FragmentStatePagerAdapter {

        public MyTimelineAdpater(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //return CommonListFragment.newInstance(TITLES[position]);

            //由于现在取不到分组信息，为避免其他tab报错，暂时都用这个flag，方便取缓存数据
            CommonListFragment c =  CommonListFragment.newInstance("所有分组");
            //setOnFABClickedListener(c);
            return c;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

    private OnFABClickedListener onFABClickedListener;

    public interface OnFABClickedListener {
        void onFABClicked();
    }

    public void setOnFABClickedListener(OnFABClickedListener onFABClickedListener) {
        this.onFABClickedListener = onFABClickedListener;
    }
}
