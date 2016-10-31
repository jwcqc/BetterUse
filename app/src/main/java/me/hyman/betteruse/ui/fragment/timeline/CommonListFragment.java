package me.hyman.betteruse.ui.fragment.timeline;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppContext;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.base.WeiboApplication;
import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.bean.CacheBean;
import me.hyman.betteruse.support.db.dao.CacheDao;
import me.hyman.betteruse.support.util.AppConst;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.NetUtil;
import me.hyman.betteruse.ui.adapter.BaseLoadingMoreAdapter;
import me.hyman.betteruse.ui.adapter.BaseRecyclerViewAdapter;
import me.hyman.betteruse.ui.adapter.WeiboTimelineAdapter;
import me.hyman.betteruse.ui.fragment.basic.AppBaseFragment;

public class CommonListFragment extends AppBaseFragment implements SwipeRefreshLayout.OnRefreshListener, WeiboTimelineAdapter.OnLoadMoreListener, TimelineFragment.OnFABClickedListener {

    @Bind(R.id.recyclerview_timeline)
    RecyclerView recyclerView;

    @Bind(R.id.swipeRefresh_timeline)
    SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = CommonListFragment.class.getName();

    //标记来自哪个标签的
    private String flagFragment;

    private AccountBean loginAccount;

    private WeiboTimelineAdapter adapter;

    private List<Status> mDatas = new LinkedList<>();

    Oauth2AccessToken accessToken;

    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;

    private int refreshCount = AppConst.DEFAULT_REFRESH_COUNT;

    private CacheDao cacheDao;

    private LOAD_MODE loadMode = LOAD_MODE.from_cache;

    enum LOAD_MODE {
        refresh_new, //下拉刷新
        load_more,    //上拉加载更多
        from_cache   //
    }


    public static CommonListFragment newInstance(String flag) {
        CommonListFragment fragment = new CommonListFragment();
        Bundle args = new Bundle();
        args.putString(AppConst.FlagFragment, flag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            flagFragment = getArguments().getString(AppConst.FlagFragment);
        }

        loginAccount = AppContext.loginAccount;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_list, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
                android.R.color.holo_blue_bright, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshCount = Integer.parseInt(getResources().getStringArray(R.array.setting_timeline_count)[AppSettings.getTimelineRefreshCountValue()]);

        initAdapter();

        // 在这里获取缓存数据
        getDBData();

        // 如果设置了自动刷新
        if(AppSettings.getAutoRefreshSetting()) {
            //loadNewData();
        }
    }

    private void getDBData() {
        if(cacheDao == null) {
            cacheDao = new CacheDao(getActivity());
        }

        List<CacheBean> cacheList =  cacheDao.getCacheData(loginAccount.getUserId(), flagFragment);

        if(cacheList != null) {

            Logger.e("从缓存中获取的数据：" + cacheList==null ? 0:cacheList.size());

            List<Status> statuses = new ArrayList<>();
            for(CacheBean cacheBean : cacheList) {

                Logger.e(cacheBean);

                StatusList statusList = StatusList.parse(cacheBean.getResult());
                statuses.addAll(statusList.statusList);
            }

            loadMode = LOAD_MODE.from_cache;
            adapter.addItemsInfront(statuses);

        } else {
            Logger.i("缓存中没有数据，去获取网络数据");

            loadMode = LOAD_MODE.refresh_new;
            loadNewData(0L);
        }
    }

    @Override
    public void onFABClicked() {
        loadMode = LOAD_MODE.refresh_new;
        swipeRefreshLayout.setRefreshing(true);
        if(mDatas.isEmpty()) {
            loadNewData(0L);
        } else {
            loadNewData(Long.parseLong(mDatas.get(0).id));
        }
    }

    @Override
    public void onRefresh() {
        loadMode = LOAD_MODE.refresh_new;
        if(mDatas.isEmpty()) {
            loadNewData(0L);
        } else {
            loadNewData(Long.parseLong(mDatas.get(0).id));
        }
    }

    @Override
    public void onLoadMore() {
        loadMode = LOAD_MODE.load_more;
        loadNewData(Long.parseLong(mDatas.get(mDatas.size()-1).id));
    }

    private void loadNewData(long id) {

        if(NetUtil.getNetWorkStatus(getActivity())) {

            mStatusesAPI = new StatusesAPI(getActivity(), AppConst.APP_KEY, getAccessToken());

            switch (loadMode) {
                case from_cache:

                    break;
                case refresh_new:
                    swipeRefreshLayout.setRefreshing(true);
                    mStatusesAPI.friendsTimeline(id, 0L, refreshCount, 1, false, 0, false, mRequestListener);
                    break;

                case load_more:
                    mStatusesAPI.friendsTimeline(0L, id, refreshCount, 1, false, 0, false, mRequestListener);
                    break;
            }

        } else {
            swipeRefreshLayout.setRefreshing(false);
            showSnakeBar(recyclerView, R.string.msg_network_not_available);
        }
    }


    private void initAdapter() {
        adapter = new WeiboTimelineAdapter(getActivity(), recyclerView, mDatas);
        adapter.setOnLoadMoreListener(this);
        //adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void saveToDB(final String result, final boolean deleteOld) {
        if(cacheDao == null) {
            cacheDao = new CacheDao(getActivity());
        }

        //cacheDao.saveCacheData(result);
        new Thread(new Runnable() {
            @Override
            public void run() {
                cacheDao.saveCacheData(result, loginAccount.getUserId(), flagFragment, deleteOld);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private Oauth2AccessToken getAccessToken() {

        if(accessToken == null) {
            accessToken = new Oauth2AccessToken();
            accessToken.setUid(loginAccount.getUserId());
            accessToken.setToken(loginAccount.getAccessToken());
            accessToken.setRefreshToken(loginAccount.getRefreshToken());
            accessToken.setExpiresTime(loginAccount.getExpires_in());
        }

        //AppContext.accessToken = accessToken;
        return accessToken;
    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mRequestListener = new RequestListener() {
        @Override
        public void onComplete(final String response) {

            if (!TextUtils.isEmpty(response)) {
                //LogUtil.i(TAG, response);

                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    final StatusList statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {

                        if(statuses.statusList != null && !statuses.statusList.isEmpty()) {
                            Toast.makeText(getActivity(),
                                    "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Logger.i("获取微博信息流条数为0");
                    }


                    List<Status> list = statuses.statusList;

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    // 在这里处理上拉刷新、下拉加载更多这两个请求返回数据后的逻辑

                    if (loadMode == LOAD_MODE.refresh_new) {

                        if (list == null || list.isEmpty()) {

                            showSnakeBar(recyclerView, R.string.msg_no_more_new_weibo);
                        } else {
                            adapter.addItemsInfront(list);
                            recyclerView.smoothScrollToPosition(0);

                            // 只有下拉刷新的才保存到数据库，上拉加载更多的不保存
                            saveToDB(response, list.size()<refreshCount ? false: true);
                        }

                    } else if (loadMode == LOAD_MODE.load_more) {

                        if (list == null || list.isEmpty()) {

                            showSnakeBar(recyclerView, R.string.msg_no_weibo_loaded);
                        } else {

                            //尼玛的返回的是大于等于的，所以会重复一条，直接删除！
                            list.remove(0);

                            adapter.addItemsAtBack(list);

                            // 是不是应该要通知adapter数据已加载完成，先隐藏掉底部刷新的view？
                        }

                    } else {
                        Logger.e("should not be there");
                    }

                    /*WeiboApplication.getHandler().post(new Runnable() {
                        @Override
                        public void run() {  }
                    });
                    */
                } else if (response.startsWith("{\"created_at\"")) {
                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(getActivity(),
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                }
            } else {
                Logger.e("获取到的response为空");
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }

            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getActivity(), info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private void showSnakeBar(View view, int msg) {
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_red_dark));
        snackbar.show();
        snackbar.setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
    }
}
