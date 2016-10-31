package me.hyman.betteruse.ui.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.support.util.WeiboUtil;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.view.CircleImageView;
import me.hyman.betteruse.ui.view.TimelinePicsView;
import me.hyman.betteruse.ui.view.WeiboTextView;

/**
 * Created by Hyman on 2015/9/22.
 */
public class WeiboTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<Status> mData;
    private Context context;

    private boolean isLoading = false;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private OnLoadMoreListener onLoadMoreListener;

    public WeiboTimelineAdapter(Context context, RecyclerView recyclerView, List mData) {
        this.mData = (mData==null ? new ArrayList<>() : mData);
        this.context = context;

        setScrollListener(recyclerView);
    }

    public Status getItem(int position) {
        if(mData != null && !mData.isEmpty()) {
            return mData.get(position);
        }
        return null;
    }

    public List<Status> getAllItems() {
        return mData;
    }

    public void addItemsInfront(List<Status> statuses) {
        mData.addAll(0, statuses);
        //notifyDataSetChanged();
        notifyItemRangeInserted(0, statuses.size());
    }

    public void addItemsAtBack(List<Status> statuses) {
        int pos = mData.size();
        mData.addAll(statuses);
        //notifyDataSetChanged();
        notifyItemRangeInserted(pos, statuses.size());

        isLoading = false;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private boolean canScrollDown(RecyclerView recyclerView) {
        return ViewCompat.canScrollVertically(recyclerView, 1);
    }

    private void setScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!canScrollDown(recyclerView)) {

                    Logger.i("滑动到列表底部");

                    if(!isLoading) {
                        isLoading = true;

                        //notifyItemInserted();
                        if(onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }

                    }
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {

            View view = LayoutInflater.from(context).inflate(R.layout.item_weibo_timeline, null);
            return new ContentViewHolder(view);

        } else if (viewType == TYPE_FOOTER) {

            View view = LayoutInflater.from(context).inflate(R.layout.def_loading, null);
            return new LoadingViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, int position) {
        if (holder_ instanceof ContentViewHolder) {

            Status status = mData.get(position);
            ContentViewHolder holder = (ContentViewHolder)holder_;

            holder.txtName.setText(WeiboUtil.getScreenName(status.user));

            //holder.imgLike TODO 待处理

            // TODO 处理头像显示的质量
            /* if(AppSettings.getPictureMode() == AppConst.PIC_MODE_LARGE || NetUtil.getNetworkStatus(WeiboApplication.getInstance()) == NetUtil.NETWORK_WIFI) {
                ImageLoader.getInstance().displayImage(status.user.avatar_large, holder.imgPhoto);
            } else {
                ImageLoader.getInstance().displayImage(status.user.profile_image_url, holder.imgPhoto);
            }*/
            ImageLoader.getInstance().displayImage(status.user.profile_image_url, holder.imgPhoto);

            WeiboUtil.setImageVerified(holder.imgVerified, status.user);

            // desc
            String createAt = "";
            if (!TextUtils.isEmpty(status.created_at))
                createAt = WeiboUtil.formatCreateTime(status.created_at);
            String from = "";
            if (!TextUtils.isEmpty(status.source))
                from = String.format("%s", Html.fromHtml(status.source));
            String desc = String.format("%s %s", createAt, from);
            holder.txtFromAndTime.setText(desc);

            holder.txtContent.setContent(status.text);

            holder.txtComment.setText(WeiboUtil.formatCount(status.comments_count));
            holder.txtRepost.setText(WeiboUtil.formatCount(status.reposts_count));

            holder.txtLike.setText(status.attitudes_count == 0 ? "" : (status.attitudes_count + "人赞了"));
            holder.txtComment.setTag(status);
            holder.txtRepost.setTag(status);

            // 设置onClickListener
            holder.btnLike.setOnClickListener(this);
            holder.btnRepost.setOnClickListener(this);
            holder.btnCmt.setOnClickListener(this);
            holder.btnMenus.setOnClickListener(this);
            holder.imgPhoto.setOnClickListener(this);
            holder.imgPhoto.setTag(status);

            // 显示转发内容
            if(status.retweeted_status != null) {
                holder.layRepost.setVisibility(View.VISIBLE);
                holder.layRepost.setTag(status.retweeted_status);

                User retweetedUser = status.retweeted_status.user;
                String reUserName = "";
                if (retweetedUser != null && !TextUtils.isEmpty(retweetedUser.screen_name)) {
                    reUserName = String.format("@%s :", retweetedUser.screen_name);
                }
                holder.txtReContent.setContent(reUserName + status.retweeted_status.text);

            } else {
                holder.layRepost.setVisibility(View.GONE);
            }


            // 显示图片
            Status showPicStatus = (status.retweeted_status == null) ? status : status.retweeted_status;
            if(AppSettings.getIsShowPic()) {

                if(showPicStatus.pic_urls != null && showPicStatus.pic_urls.size() > 0) {
                    holder.txtPics.setVisibility(View.GONE);
                    holder.layPicturs.setPics(showPicStatus);
                } else {
                    holder.layPicturs.setVisibility(View.GONE);
                    holder.txtPics.setVisibility(View.GONE);
                }
            } else {
                holder.layPicturs.setVisibility(View.GONE);
                if(showPicStatus.pic_urls != null && showPicStatus.pic_urls.size() > 0) {
                    holder.txtPics.setText(String.format("%d张图片", showPicStatus.pic_urls.size()));
                    holder.txtPics.setVisibility(View.VISIBLE);
                    holder.txtPics.setTag(showPicStatus);
                    //holder.txtPics.setOnClickListener(this);
                } else {
                    holder.txtPics.setVisibility(View.GONE);
                }
            }


            // TODO 待处理 微博的可见性及指定可见分组信息
            holder.txtVisible.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }


    //正常条目
    public class ContentViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imgPhoto;
        public TextView txtName;
        public ImageView imgVerified;
        public TextView txtFromAndTime;
        //public TextView txtContent;

        public ImageView imgLike;

        public TextView txtLike;
        public TextView txtComment;
        public TextView txtRepost;

        public ImageView btnMenus;
        public TextView txtVisible;

        // 转发相关
        public View layRepost;
        //public TextView txtReContent;
        public TextView txtPics;

        public WeiboTextView txtContent;
        public WeiboTextView txtReContent;

        public LinearLayout btnLike;
        public LinearLayout btnRepost;
        public LinearLayout btnCmt;

        public TimelinePicsView layPicturs;

        public ContentViewHolder(View view) {
            super(view);

            imgPhoto = (CircleImageView) view.findViewById(R.id.imgPhoto);

            txtName = (TextView) view.findViewById(R.id.txtName);
            txtFromAndTime = (TextView) view.findViewById(R.id.txtFromAndTime);
            //txtContent = (TextView) view.findViewById(R.id.txtContent);
            txtContent = (WeiboTextView) view.findViewById(R.id.txtContent);

            txtLike = (TextView) view.findViewById(R.id.txtLike);
            txtComment = (TextView) view.findViewById(R.id.txtComment);
            txtRepost = (TextView) view.findViewById(R.id.txtRepost);

            imgVerified = (ImageView) view.findViewById(R.id.imgVerified);
            imgLike = (ImageView) view.findViewById(R.id.imgLike);

            btnMenus = (ImageView) view.findViewById(R.id.btnMenus);
            txtVisible = (TextView) view.findViewById(R.id.txtVisible);

            layRepost =  view.findViewById(R.id.layRepost);
            //txtReContent = (TextView) view.findViewById(R.id.txtReContent);
            txtReContent = (WeiboTextView) view.findViewById(R.id.txtReContent);
            txtPics = (TextView) view.findViewById(R.id.txtPics);

            btnLike = (LinearLayout) view.findViewById(R.id.btnLike);
            btnRepost = (LinearLayout) view.findViewById(R.id.btnRepost);
            btnCmt = (LinearLayout) view.findViewById(R.id.btnCmt);

            layPicturs = (TimelinePicsView) view.findViewById(R.id.layPicturs);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.i("微博时间线当前点击的位置：" + getAdapterPosition());

                  /*  Intent intent = new Intent(BaseAppCompatActivity.getRunningActivity(), WeiboDetailActivity.class);
                    Status s = mData.get(getAdapterPosition());
                    intent.putExtra("currentStatus", s);
                    BaseAppCompatActivity.getRunningActivity().startActivity(intent);*/
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Logger.i("微博时间线当前长按的位置：" + getAdapterPosition());
                    return true;
                }
            });
        }
    }

    //正在加载条目
    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public TextView tvLoading;
        public LinearLayout llyLoading;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.loading_progress);
            tvLoading = (TextView) view.findViewById(R.id.loading_text);
            llyLoading = (LinearLayout) view.findViewById(R.id.loading_view);
        }
    }


    @Override
    public void onClick(View v) {

        // 进入个人主页
        if (v.getId() == R.id.imgPhoto) {
           /* Status status = (Status) v.getTag();
            Intent intent = new Intent(BaseAppCompatActivity.getRunningActivity(), UserProfileActivity.class);
           *//* Bundle bundle = intent.getExtras();
            bundle.putSerializable("user", status.user);
            intent.putExtras(bundle);*//*
            BaseAppCompatActivity.getRunningActivity().startActivity(intent);
*/
            MyToast.showShort(BaseAppCompatActivity.getRunningActivity(), "点头像应该要进个人主页，不过还没做...");
        }
        // 转发
        else if (v.getId() == R.id.btnRepost) {
            //Status status = (Status) v.getTag();
            MyToast.showShort(BaseAppCompatActivity.getRunningActivity(), "转发功能还没做...");

        }
        // 评论
        else if (v.getId() == R.id.btnCmt) {
            //Status status = (Status) v.getTag();
            MyToast.showShort(BaseAppCompatActivity.getRunningActivity(), "评论功能还没做...");

        }
        // 点赞
        else if (v.getId() == R.id.btnLike) {

            MyToast.showShort(BaseAppCompatActivity.getRunningActivity(), "点赞功能还没做...");

            //Status status = (Status) v.getTag();
            //v.findViewById(R.id.imgLike).setSelected(boolean);

        }
        // 溢出菜单
        else if (v.getId() == R.id.btnMenus) {
            MyToast.showShort(BaseAppCompatActivity.getRunningActivity(), "别乱点，功能还没做...");
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
