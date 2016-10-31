package me.hyman.betteruse.support.cache;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Hyman on 2015/9/15.
 */
public class CallbackManager {

    private ConcurrentHashMap<String, List<ImageLoaderCallback>> callbackMap;

    public CallbackManager() {
        callbackMap = new ConcurrentHashMap<String, List<ImageLoaderCallback>>();
    }

    public void put(String url, ImageLoaderCallback callback) {
        if(!callbackMap.contains(url)) {
            callbackMap.put(url, new ArrayList<ImageLoaderCallback>());
        }
        callbackMap.get(url).add(callback);
    }

    public void callback(String url, Bitmap bitmap) {
        List<ImageLoaderCallback> callbacks = callbackMap.get(url);
        if(callbacks == null) {
            return;
        }

        for (ImageLoaderCallback callback : callbacks) {
            if(callback != null) {
                callback.refresh(url, bitmap);
            }
        }

        callbacks.clear();
        callbackMap.remove(url);
    }

    /**
     * 生成一个callback
     * @return
     */
    public static ImageLoaderCallback getCallback(String url, final ImageView imageView) {
        return new ImageLoaderCallback() {
            @Override
            public void refresh(String url, Bitmap bitmap) {
                if(imageView.getTag() != null) {
                    if(url.equals(imageView.getTag().toString())) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }

        };
    }

    /**
     * 回调
     */
    public interface ImageLoaderCallback {
        void refresh(String url, Bitmap bitmap);
    }
}
