package me.hyman.betteruse.support.cache;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import me.hyman.betteruse.base.WeiboApplication;


/**
 * Created by Hyman on 2015/9/15.
 */
public class LazyImageLoader {

    public static final String EXTRA_IMG_URL = "extra_img_url";
    public static final String EXTRA_IMG = "extra_img";

    private static final int MESSAGE_ID = 1;

    private ImageManager imgManager = new ImageManager(WeiboApplication.getInstance().getApplicationContext());
    private BlockingQueue<String> urlQueue = new ArrayBlockingQueue<String>(50);
    private CallbackManager callbackManager = new CallbackManager();
    private DownloadImageThread downloadImgThread = new DownloadImageThread();

    public Bitmap load(String url, CallbackManager.ImageLoaderCallback callback, int defaultDrawable) {
        //先加载默认
        Bitmap bitmap = imgManager.drawableToBitmap(WeiboApplication.getInstance().getApplicationContext().getResources().getDrawable(defaultDrawable));
        if(TextUtils.isEmpty(url)) {

        } else if(imgManager.contains(url)) {
            bitmap = imgManager.getFromCache(url);
        } else {
            callbackManager.put(url, callback);
            startDownloadThread(url);
        }
        return bitmap;
    }

    private void startDownloadThread(String url) {

        putToUrlQueue(url);

        Thread.State state = downloadImgThread.getState();

        if(state == Thread.State.NEW) {
            downloadImgThread.start();
        } else if(state == Thread.State.TERMINATED) {
            downloadImgThread = new DownloadImageThread();
            downloadImgThread.start();
        }
    }

    private void putToUrlQueue(String url) {
        if(!urlQueue.contains(url)) {
            try {
                urlQueue.put(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadImageThread extends Thread {
        private boolean isRun = true;

        private void shutDown() {
            isRun = false;
        }

        public void run() {
            try {
                while(isRun) {
                    String url = urlQueue.poll();
                    if(url == null) {
                        break;
                    }

                    Bitmap bitmap = imgManager.safeGet(url);

                    Message msg = handler.obtainMessage(MESSAGE_ID);
                    Bundle bundle = msg.getData();
                    bundle.putSerializable(EXTRA_IMG_URL, url);
                    bundle.putParcelable(EXTRA_IMG, bitmap);

                    handler.sendMessage(msg);
                }

            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                shutDown();
            }
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_ID:

                    Bundle bundle =  msg.getData();
                    String url = bundle.getString(EXTRA_IMG_URL);
                    Bitmap bitmap = bundle.getParcelable(EXTRA_IMG);

                    callbackManager.callback(url, bitmap);

                    break;

                default:
                    break;
            }

        }

    };
}
