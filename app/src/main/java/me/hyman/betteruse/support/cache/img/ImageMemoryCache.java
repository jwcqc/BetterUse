package me.hyman.betteruse.support.cache.img;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Created by Hyman on 2015/9/20.
 */
public class ImageMemoryCache {

    private static final String TAG = ImageMemoryCache.class.getName();

    /**
     * 从内存读取数据速度是最快的，为了更大限度使用内存，这里使用两层缓存。
     *  强引用缓存不会轻易被回收，用来保存常用数据，不常用的转入软引用缓存。
     */
    private static LruCache<String, Bitmap> mLruCache; // 强引用缓存

    private static LinkedHashMap<String, SoftReference<Bitmap>> mSoftCache; // 软引用缓存

    private static final int LRU_CACHE_SIZE = 4 * 1024 * 1024; // 强引用缓存容量：4MB

    private static final int SOFT_CACHE_NUM = 20; // 软引用缓存个数


    public ImageMemoryCache() {

        mLruCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                if(value != null) {
                    return value.getRowBytes() * value.getHeight();
                } else {
                    return 0;
                }
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if(oldValue != null) {
                    // 强引用缓存容量满的时候，会根据LRU算法把最近没有被使用的图片转入软引用缓存
                    mSoftCache.put(key, new SoftReference<Bitmap>(oldValue));
                }
            }
        };

        mSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_NUM, 0.75f, true) {

            /**
             * 当软引用数量大于指定值的时候，最旧的软引用将会被移出
             */
            @Override
            protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
                if(size() > SOFT_CACHE_NUM) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * 从缓存中获取图片
     */
    public Bitmap getFromMemory(String url) {

        Bitmap bitmap = null;

        synchronized (mLruCache) {
            // 先从强引用缓存中获取
            bitmap = mLruCache.get(url);
            if(bitmap != null) {
                // 如果找到的话，把元素移到最前面，从而保证在LRU算法中是最后被删除
                mLruCache.remove(url);
                mLruCache.put(url, bitmap);
                return bitmap;
            }
        }

        // 如果强引用缓存中找不到，到软引用缓存中找，找到后就把它从软引用中移到强引用缓存中
        synchronized (mSoftCache) {
            SoftReference<Bitmap> bitmapReference = mSoftCache.get(url);
            if(bitmapReference != null) {
                bitmap = bitmapReference.get();
                if(bitmap != null) {
                    mLruCache.put(url, bitmap);
                    mSoftCache.remove(url);
                    return bitmap;
                } else {
                    mSoftCache.remove(url);
                }
            }
        }

        return null;
    }

    /**
     * 添加图片到缓存
     */
    public void addToMemory(String url, Bitmap bitmap) {

        if(bitmap != null) {
            synchronized (mLruCache) {
                mLruCache.put(url, bitmap);
            }
        }
    }

    public void clearCache() {
        mSoftCache.clear();
    }
}
