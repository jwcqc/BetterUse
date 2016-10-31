package me.hyman.betteruse.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.List;

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
public class WeiboTimelineAdapter_bak extends RecyclerView.Adapter<WeiboTimelineAdapter_bak.ViewHolder> implements View.OnClickListener {

    private List<Status> mData;

    public WeiboTimelineAdapter_bak(List<Status> statuses) {
        hasStableIds();
        this.mData = statuses;
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

    public void addItems(List<Status> statuses) {
        mData.addAll(0, statuses);
        //Collections.sort(mData, new WeiboStatusSort());
        //notifyDataSetChanged();
        notifyItemRangeInserted(0, statuses.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weibo_timeline, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Status status = mData.get(position);

        holder.txtName.setText(WeiboUtil.getScreenName(status.user));

        //holder.imgLike TODO 待处理

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

        //holder.txtContent.setText(WeiboUtil.parseWeibo(status.text));
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
            // holder.txtReContent
            holder.layRepost.setVisibility(View.VISIBLE);
            holder.layRepost.setTag(status.retweeted_status);

            User retweetedUser = status.retweeted_status.user;
            String reUserName = "";
            if (retweetedUser != null && !TextUtils.isEmpty(retweetedUser.screen_name)) {
                reUserName = String.format("@%s :", retweetedUser.screen_name);
            }
            //holder.txtReContent.setText(WeiboUtil.parseWeibo(reUserName + status.retweeted_status.text));
            holder.txtReContent.setContent(reUserName + status.retweeted_status.text);

        } else {
            holder.layRepost.setVisibility(View.GONE);
        }


        // 显示图片
        Status showPicStatus = (status.retweeted_status == null) ? status : status.retweeted_status;
        if(AppSettings.getIsShowPic()) {

            if(showPicStatus.pic_urls != null && showPicStatus.pic_urls.size() > 0) {
                //holder.txtPics.setText(String.format("%d张图片等待我去完成加载显示...", showPicStatus.pic_urls.size()));
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


        // 微博的可见性及指定可见分组信息
        holder.txtVisible.setVisibility(View.GONE);  //TODO 待处理
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

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


        public ViewHolder(View view) {
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
                    Logger.e("BaseAppCompatActivity", "微博时间线当前点击的位置：" + getAdapterPosition());

                  /*  Intent intent = new Intent(BaseAppCompatActivity.getRunningActivity(), WeiboDetailActivity.class);
                    Status s = mData.get(getAdapterPosition());
                    intent.putExtra("currentStatus", s);
                    BaseAppCompatActivity.getRunningActivity().startActivity(intent);*/
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Logger.e("BaseAppCompatActivity", "当前长按的位置：" + getAdapterPosition());
                    return true;
                }
            });
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
}
