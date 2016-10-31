package me.hyman.betteruse.support.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import me.hyman.betteruse.support.util.MD5Util;


/**
 * Created by Hyman on 2015/9/15.
 */
public class ImageManager {

    Map<String, SoftReference<Bitmap>> imgCache;

    private Context context;

    public ImageManager(Context context) {
        this.context = context;
        imgCache = new HashMap<String, SoftReference<Bitmap>>();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable temp = (BitmapDrawable) drawable;
        return temp.getBitmap();
    }

    public boolean contains(String url) {
        return imgCache.containsKey(url);
    }

    public Bitmap safeGet(String url) {
        Bitmap bitmap = this.getFromFile(url);
        if(bitmap != null) {
            synchronized(this) {
                imgCache.put(url, new SoftReference<Bitmap>(bitmap));
            }
            return bitmap;
        }
        return downloadImg(url);
    }

    /**
     * 从缓存中获取Bitmap，包括从内存中和从文件系统中
     * @param url
     * @return
     */
    public Bitmap getFromCache(String url) {
        Bitmap bitmap = null;

        bitmap = this.getFromMapCache(url);

        if(bitmap == null) {
            bitmap = getFromFile(url);
        }

        return bitmap;
    }

    /**
     * 从文件中获取Bitmap
     * @param url
     * @return
     */
    private Bitmap getFromFile(String url) {
        String fileName = getMd5String(url);
        FileInputStream input = null;

        try {
            input = context.openFileInput(fileName);
            return BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            return null;
        } finally {
            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从Map缓存中获取Bitmap
     * @param url
     * @return
     */
    private Bitmap getFromMapCache(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> ref = null;
        synchronized(this) {
            ref = imgCache.get(url);
        }
        if(ref != null) {
            bitmap = ref.get();
        }

        return bitmap;
    }

    private String getMd5String(String src) {
        return MD5Util.getMD5String(src);
    }

    public Bitmap downloadImg(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String fileName = writeToFile(getMd5String(urlStr), conn.getInputStream());
            return BitmapFactory.decodeFile(fileName);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private String writeToFile(String fileName, InputStream is) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            //File file = new File(context.getCacheDir().getPath() + File.separator + fileName);
            //bos = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[1024];
            int length;
            while((length = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(bis != null) {
                    bis.close();
                }
                if(bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        //return context.getCacheDir().getPath() + File.separator + fileName;
        return context.getFilesDir() + File.separator + fileName;
    }

}
