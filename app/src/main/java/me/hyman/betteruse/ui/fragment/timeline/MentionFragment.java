package me.hyman.betteruse.ui.fragment.timeline;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.hyman.betteruse.R;
import me.hyman.betteruse.ui.fragment.basic.AppBaseFragment;

public class MentionFragment extends AppBaseFragment {

    @Bind(R.id.tabLayout_timeline)
    TabLayout tabLayout;

    @Bind(R.id.viewPager_timeline)
    ViewPager viewPager;

    public final String[] TITLES = {
            "所有分组", "同学", "互联网", "新闻", "杂七杂八"
    };


    public static MentionFragment newInstance() {
        MentionFragment fragment = new MentionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mention, container, false);
        ButterKnife.bind(this, view);

        initViewPager();

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
            return CommonListFragment.newInstance(TITLES[position]);
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


}
