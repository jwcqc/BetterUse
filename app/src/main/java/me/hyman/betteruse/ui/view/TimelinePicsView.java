
package me.hyman.betteruse.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.base.WeiboApplication;
import me.hyman.betteruse.support.util.AppConst;
import me.hyman.betteruse.support.util.CommonUtil;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.NetUtil;
import me.hyman.betteruse.ui.activity.ImagePagerActivity;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;


/**
 * timeline的图片容器，根据图片个数动态布局每一个ImageView
 */
public class TimelinePicsView extends ViewGroup {

    public static final String TAG = TimelinePicsView.class.getSimpleName();

    private TimelinePicsClickListener timelinePicsClickListener = new TimelinePicsClickListener();

    private int mWidth;

    // 照片之间的空隙
    private int gap;

    private Rect[] picRects;

    private Status mStatus;

    private String[] picUrls;

    // 九宫格
    private static Rect[] small9ggRectArr = null;

    private PicQuality picQuality =  PicQuality.middle;
    private enum PicQuality {
        // 高清、中等、普通，分别对应large bmiddle thumbnail
        large, middle, small
    }

    public TimelinePicsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TimelinePicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimelinePicsView(Context context) {
        super(context);
        init();
    }

    private void init() {
        gap = getResources().getDimensionPixelSize(R.dimen.gap_pics);

        //Logger.v(TAG, String.format("screenWidth = %d", CommonUtil.getScreenWidth()));
        //Logger.v(TAG, String.format("gap = %d, width = %d", gap, mWidth));
    }

    private void recycle() {
        for (int i = 0; i < getChildCount(); i++) {
            ImageView imgView = (ImageView) getChildAt(i);
            imgView.setImageResource(R.drawable.bg_timeline_loading);
        }
    }

    public void release() {
        //Logger.v(TAG, "释放资源");

        mStatus = null;

        for (int i = 0; i < getChildCount(); i++) {
            ImageView imgView = (ImageView) getChildAt(i);
            imgView.setImageResource(R.drawable.bg_timeline_loading);
        }
    }

    public void setPics(Status status) {

        if (mStatus != null && String.valueOf(mStatus.id).equals(status.id)) {
            //return;
        }

        // 如果图片模式是大图，或者当前是wifi网络
        if(AppSettings.getPictureMode() == AppConst.PIC_MODE_LARGE || NetUtil.getNetworkStatus(WeiboApplication.getInstance()) == NetUtil.NETWORK_WIFI) {
            //TODO picQuality = PicQuality.large;
            picQuality = PicQuality.middle;

        } else if (AppSettings.getPictureMode() == AppConst.PIC_MODE_MIDDLE) {
            picQuality = PicQuality.middle;

        } else {
            picQuality = PicQuality.small;
        }

        mStatus = status;

        if (mStatus.pic_urls == null || mStatus.pic_urls.isEmpty()) {

            recycle();
            setVisibility(View.GONE);

        } else {

            picUrls = getSettingPicSize(mStatus.pic_urls, picQuality);

            setVisibility(View.VISIBLE);

            setTimelinePicsView();
        }
    }

    /**
     * // 根据设置的图片模式修改url
     * @param urls
     * @param quality 图片质量
     * @return
     */
    private String[] getSettingPicSize(ArrayList<String> urls, PicQuality quality) {
        String[] picUrls = new String[urls.size()];
        if(quality == null) {
            quality = picQuality;
        }
        // 根据设置的图片模式修改url
        for(int i=0; i<urls.size(); i++) {
            String str = urls.get(i);
            switch (quality) {
                case large:
                    str = str.replace("thumbnail", "large");
                    break;

                case middle:
                    str = str.replace("thumbnail", "bmiddle");
                    break;

                case small:

                default: break;
            }
            picUrls[i] = str;
        }

        return picUrls;
    }

    private void setTimelinePicsView() {

        // 不全占满，右侧留一些空白
        int maxWidth = CommonUtil.getScreenWidth() - CommonUtil.dip2px(36);
        mWidth = Math.round(maxWidth * 1.0f * 4 / 5);

        picRects = null;

        int size = picUrls.length;

        int imgW = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
        int imgH = imgW;
        LinearLayout.LayoutParams layoutParams = null;

        // 4张图片的特殊情况，上下各2张
        if (size == 4) {

            layoutParams = new LinearLayout.LayoutParams(mWidth, imgH * 2 + gap);

            picRects = new Rect[4];

            Rect rect = new Rect(0, 0, imgW, imgH);
            picRects[0] = rect;
            rect = new Rect(imgW + gap, 0, imgW * 2 + gap, imgH);
            picRects[1] = rect;
            rect = new Rect(0, imgH + gap, imgW, imgH * 2 + gap);
            picRects[2] = rect;
            rect = new Rect(imgW + gap, imgH + gap, imgW * 2 + gap, imgH * 2 + gap);
            picRects[3] = rect;

        } else {
            int oneWidth = 0;
            if (picQuality != PicQuality.small)
                //oneWidth = mWidth * 3 / 5;
                oneWidth = mWidth;
            else
                oneWidth = maxWidth / 3;
            int height = 0;

            switch (size) {
                case 1:
                    float maxRadio = 13 * 1.0f / 16;
                    // 初始状态
                    height = oneWidth;
                    break;
                case 2:
                case 3:
                    height = imgH;
                    break;
                case 5:
                case 6:
                    height = imgH * 2 + gap;
                    break;
                case 7:
                case 8:
                case 9:
                    height = imgH * 3 + gap * 2;
                    break;
            }

            layoutParams = new LinearLayout.LayoutParams(mWidth, height);

            // 当只有一张图片的时候，特殊处理
            if (size == 1) {
                Rect oneRect = new Rect();
                oneRect.left = 0;
                oneRect.top = 0;
                oneRect.right = oneWidth;
                oneRect.bottom = height;

                int imgSize = oneRect.right - oneRect.left;
                if (picQuality != PicQuality.small) {
                    if (imgSize < CommonUtil.dip2px(100))
                        oneRect.left = (maxWidth - imgSize) / 2;
                } else {
                    oneRect.left = CommonUtil.dip2px(32);
                }
                oneRect.right += oneRect.left;

                picRects = new Rect[]{oneRect};
            } else {
                picRects = new Rect[size];
                for (int i = 0; i < size; i++)
                    picRects[i] = getSmallRectArr()[i];
            }
        }

        setLayoutParams(layoutParams);

        displayPics();

        // 重新绘制
        requestLayout();
    }

    private Rect[] getSmallRectArr() {
        if (small9ggRectArr != null)
            return small9ggRectArr;

        // 总宽度减去两个图片间距，再除以三，即是每张图片的宽度
        int imgW = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
        int imgH = imgW;

        Rect[] tempRects = new Rect[9];

        Rect rect = new Rect(0, 0, imgW, imgH);
        tempRects[0] = rect;
        rect = new Rect(imgW + gap, 0, imgW * 2 + gap, imgH);
        tempRects[1] = rect;
        rect = new Rect(mWidth - imgW, 0, mWidth, imgH);
        tempRects[2] = rect;

        rect = new Rect(0, imgH + gap, imgW, imgH * 2 + gap);
        tempRects[3] = rect;
        rect = new Rect(imgW + gap, imgH + gap, imgW * 2 + gap, imgH * 2 + gap);
        tempRects[4] = rect;
        rect = new Rect(mWidth - imgW, imgH + gap, mWidth, imgH * 2 + gap);
        tempRects[5] = rect;

        rect = new Rect(0, imgH * 2 + gap * 2, imgW, imgH * 3 + gap * 2);
        tempRects[6] = rect;
        rect = new Rect(imgW + gap, imgH * 2 + gap * 2, imgW * 2 + gap, imgH * 3 + gap * 2);
        tempRects[7] = rect;
        rect = new Rect(mWidth - imgW, imgH * 2 + gap * 2, mWidth, imgH * 3 + gap * 2);
        tempRects[8] = rect;

        small9ggRectArr = tempRects;
        return small9ggRectArr;
    }

    public void displayPics() {

        if (picRects == null || picUrls == null || picUrls.length == 0)
            return;

        for (int i = 0; i < getChildCount(); i++) {
            ImageView imgView = (ImageView) getChildAt(i);

            // 隐藏多余的View
            if (i >= picRects.length) {
                getChildAt(i).setVisibility(View.GONE);

            } else {
                Rect imgRect = picRects[i];

                imgView.setVisibility(View.VISIBLE);
                // 如果是一个图片，就显示大一点
                int size = picUrls.length;
                if (size == 1) {
                    imgView.setScaleType(ScaleType.FIT_CENTER); // FIT_XY会把高度进行拉伸，影响效果
                } else {
                    imgView.setScaleType(ScaleType.CENTER_CROP);
                }
                imgView.setLayoutParams(new LayoutParams(imgRect.right - imgRect.left, imgRect.bottom - imgRect.top));

                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.bg_timeline_loading)     //加载图片时的图片
                        .showImageForEmptyUri(R.drawable.bg_timeline_loading)   //没有图片资源时的默认图片
                        .showImageOnFail(R.drawable.bg_timeline_loading)        //加载失败时的图片
                        .cacheInMemory(true)                               //启用内存缓存
                        .cacheOnDisk(true)                                 //启用外存缓存
                        //.considerExifParams(true)                          //启用EXIF和JPEG图像格式
                        //.displayer(new FadeInBitmapDisplayer(100))         //比如RoundedBitmapDisplayer代表圆角矩形
                        .build();
                ImageLoader.getInstance().displayImage(picUrls[i], imgView, options);


                // 为每一个设置onClickListener,i代表selected index
                Object[] tag = new Object[]{mStatus, i};
                imgView.setTag(tag);
                imgView.setOnClickListener(timelinePicsClickListener);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(picRects == null) {
            return;
        }

        for(int i=0; i<getChildCount(); i++) {
            if(i < picRects.length) {

                Rect imgRect = picRects[i];

                ImageView childView = (ImageView) getChildAt(i);

                childView.layout(imgRect.left, imgRect.top, imgRect.right, imgRect.bottom);

            } else {
                break;
            }
        }
    }


    private class TimelinePicsClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            Object[] tag = (Object[]) v.getTag();
            Status status = (Status) tag[0];
            int selectedIndex = Integer.parseInt(tag[1].toString());

            String[] urls = getSettingPicSize(status.pic_urls, PicQuality.large);
            ImagePagerActivity.launch(BaseAppCompatActivity.getRunningActivity(), urls, selectedIndex);
        }
    }
}

