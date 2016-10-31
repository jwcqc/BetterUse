package me.hyman.betteruse.support.cache.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Hyman on 2015/9/20.
 */
public class MySimpleImageLoader {

    private static final String TAG = MySimpleImageLoader.class.getName();

    private static MySimpleImageLoader instance;

    private ImageMemoryCache imageMemoryCache;

    private ImageFileCache imageFileCache;

    private OnImageLoadListener onImageLoadListener;

    //同时下载图片的最大线程个数，默认为3
    private int maxDownloadThreadNum = 3;

    //等待下载的image列表
    private static HashMap<String, ImageLoaderCallback> waitingTaskMap = new HashMap<String, ImageLoaderCallback>();

    //正在下载的image列表
    private static HashMap<String, ImageLoaderCallback> onGoingTaskMap = new HashMap<String, ImageLoaderCallback>();


    public static MySimpleImageLoader getInstance(Context context) {
        if(instance == null) {
            instance = new MySimpleImageLoader(context);
        }
        return instance;
    }

    private MySimpleImageLoader(Context context) {
        imageMemoryCache = new ImageMemoryCache();
        imageFileCache = new ImageFileCache(context);
    }

    public void loadImage(String url, final ImageView imageView) {

        if(TextUtils.isEmpty(url)) {
            if(onImageLoadListener != null) {
                onImageLoadListener.onLoadFail("传入url链接为空！");
            }
            return;
        }

        imageView.setTag(url);

        //先从内存缓存中获取，若取到则直接加载
        Bitmap bitmap = imageMemoryCache.getFromMemory(url);

        if(bitmap != null) {

            if(imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            if(onImageLoadListener != null) {
                onImageLoadListener.onLoadSuccess(url, bitmap);
            }

        } else {

            // 内存缓存中未获取到，则通过线程从文件系统或网络去获取
            getByThread(url, new ImageLoaderCallback() {

                @Override
                public void refresh(String url, Bitmap bitmap) {
                    if (imageView.getTag() != null) {
                        if (url.equals(imageView.getTag().toString())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }
            });

        }

    }


    public void loadImage(String url, final ImageView imageView, int defResId) {
        imageView.setImageResource(defResId);
        this.loadImage(url, imageView);
    }

    private void getByThread(final String url, final ImageLoaderCallback callback) {

        // 会大于最大线程数，则先加入等待任务队列
        if(onGoingTaskMap.size() >= maxDownloadThreadNum) {

            waitingTaskMap.put(url, callback);

        } else {

            synchronized (onGoingTaskMap) {
                onGoingTaskMap.put(url, callback);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Bitmap bitmap = imageFileCache.getFromFile(url);

                    if(bitmap == null) {
                        // 在方法内部已添加到文件系统
                        bitmap = getFromHttp(url);
                    }

                    // 同时添加到内存缓存中去
                    imageMemoryCache.addToMemory(url, bitmap);

                    Message msg = Message.obtain();
                    msg.obj = bitmap;
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                }
            }).start();

        }

    }

    private Bitmap getFromHttp(String urlStr) {

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String filePath = imageFileCache.writeToFile(urlStr, conn.getInputStream());
            return BitmapFactory.decodeFile(filePath);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bitmap bitmap = (Bitmap)msg.obj;
            String url = (String)msg.getData().get("url");

            if(bitmap == null) {
                if(onImageLoadListener != null) {
                    onImageLoadListener.onLoadFail("未获取到Bitmap");
                    return;
                }
            }

            ImageLoaderCallback callback = onGoingTaskMap.get(url);

            //  刷新界面的ImageView
            callback.refresh(url, bitmap);

            if(onImageLoadListener != null) {
                onImageLoadListener.onLoadSuccess(url, bitmap);
            }

            synchronized (onGoingTaskMap) {
                onGoingTaskMap.remove(url);
            }

            startDownloadNext();
        }
    };

    private void startDownloadNext() {
        synchronized (waitingTaskMap) {
            Iterator it = waitingTaskMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if(entry != null) {
                    waitingTaskMap.remove(entry.getKey());
                    getByThread((String)entry.getKey(), (ImageLoaderCallback)entry.getValue());
                }
            }
        }
    }

    public interface OnImageLoadListener {
        void onLoadSuccess(String url, Bitmap bitmap);
        void onLoadFail(String msg);
    }

    private interface ImageLoaderCallback {
        //  回调函数，用于设置ImageView的Bitmap
        void refresh(String url, Bitmap bitmap);
    }

    public void setMaxDownloadThreadNum(int maxDownloadThreadNum) {
        this.maxDownloadThreadNum = maxDownloadThreadNum;
    }

    public void setOnImageLoadListener(OnImageLoadListener onImageLoadListener) {
        this.onImageLoadListener = onImageLoadListener;
    }

    public void removeListener() {
        this.onImageLoadListener = null;
    }
}
