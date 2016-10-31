package me.hyman.betteruse.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.hyman.betteruse.R;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.widget.viewPageIndicator.CirclePageIndicator;
import me.hyman.betteruse.ui.widget.viewPageIndicator.HackyViewPager;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePagerActivity extends BaseAppCompatActivity {

    private static final String INTENT_URLS = "status";
    private static final String INTENT_SELECTED_INDEX = "selected_index";
    private static final String STATE_POSITION = "state_position";

    private String[] imageUrls;
    private int currentIndex;

    private HackyViewPager viewPager;
    private CirclePageIndicator mIndicator;

    private DisplayImageOptions options;

    public static void launch(Activity from, String[] urls, int index) {
        Intent intent = new Intent(from, ImagePagerActivity.class);
        intent.putExtra(INTENT_URLS, urls);
        intent.putExtra(INTENT_SELECTED_INDEX, index);
        from.startActivity(intent);
    }


    @Override
    protected int getContentViewId() {
        return R.layout.activity_image_pager;
    }

    @Override
    protected int configTheme() {
        return 0;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {

        Bundle bundle = getIntent().getExtras();
        imageUrls = bundle.getStringArray(INTENT_URLS);
        currentIndex = bundle.getInt(INTENT_SELECTED_INDEX);

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt(STATE_POSITION);
        }

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        viewPager = (HackyViewPager) findViewById(R.id.viewpager_timeline_pics);
        viewPager.setAdapter(new ImagePagerAdapter(this, imageUrls));
        viewPager.setCurrentItem(currentIndex);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, viewPager.getCurrentItem());
    }

    class ImagePagerAdapter extends PagerAdapter {

        private Context context;
        private String[] imageUrls;
        private LayoutInflater mInflater;

        public ImagePagerAdapter(Context context, String[] imageUrls) {
            this.context = context;
            this.imageUrls = imageUrls;
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View imageLyt = mInflater.inflate(R.layout.item_timeline_pic, container, false);

            PhotoView photoView = (PhotoView) imageLyt.findViewById(R.id.timeline_pic);
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    ImagePagerActivity.this.finish();
                }
            });


            final ProgressBar mProgressBar = (ProgressBar) imageLyt.findViewById(R.id.timeline_pic_loading);
            ImageLoader.getInstance().displayImage(imageUrls[position], photoView, options, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    MyToast.showShort(ImagePagerActivity.this, "加载图片失败");
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });

            ((ViewPager) container).addView(imageLyt, 0);

            return imageLyt;

        }
    }

}
