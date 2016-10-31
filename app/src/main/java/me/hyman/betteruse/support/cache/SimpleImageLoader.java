package me.hyman.betteruse.support.cache;

import android.widget.ImageView;

/**
 * Created by Hyman on 2015/9/15.
 */
public class SimpleImageLoader {

    public static void loadImage(ImageView imageView, String url, int defaultDrawable) {

        /*imageView.setTag(url);

        imageView.setImageBitmap(ImageManager.drawableToBitmap(WeiboApplication.getInstance().getApplicationContext().getResources().getDrawable(defaultDrawable)));

        Bitmap bitmap = WeiboApplication.lazyImageLoader.load(url, CallbackManager.getCallback(url, imageView), defaultDrawable);

        imageView.setImageBitmap(bitmap);*/
    }

}
