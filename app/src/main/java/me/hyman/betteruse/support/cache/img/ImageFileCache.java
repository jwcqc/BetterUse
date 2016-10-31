package me.hyman.betteruse.support.cache.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import me.hyman.betteruse.support.util.MD5Util;

/**
 * Created by Hyman on 2015/9/20.
 */
public class ImageFileCache {

    private static final String TAG = ImageFileCache.class.getName();

    private Context context;

    private String imgCacheDir;

    private final int MB = 1024*1024;

    private final int CACHE_SIZE = 1;

    //当SD卡剩余空间小于10M的时候会清理缓存
    private final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

    //保存的cache文件宽展名
    private final String CACHETAIL = ".cache";

    public ImageFileCache(Context context) {
        this.context = context;
        imgCacheDir = context.getCacheDir().getAbsolutePath();

        //清理部分文件缓存
        removeCache(imgCacheDir);
    }

    public Bitmap getFromFile(String url) {

        Bitmap bitmap = null;
        String path = imgCacheDir + File.separator + convertUrlToFileName(url);
        File file = new File(path);

        if(file != null && file.exists()) {
            bitmap = BitmapFactory.decodeFile(path);
            if(bitmap == null) {
                file.delete();
            } else {
                // 修改文件的最后修改时间
                file.setLastModified(System.currentTimeMillis());
            }
        }

        return bitmap;
    }

    public String writeToFile(String url, InputStream is) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String fileName = convertUrlToFileName(url);

        try {
            bis = new BufferedInputStream(is);
            File file = new File(imgCacheDir + File.separator + fileName);
            bos = new BufferedOutputStream(new FileOutputStream(file));

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

        return imgCacheDir + File.separator + fileName;
    }

    /**
     * 计算存储目录下的文件大小，
     * 当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
    private boolean removeCache(String imgCacheDir) {

        File dir = new File(imgCacheDir);
        File[] files = dir.listFiles();

        if (files == null) {
            return true;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(CACHETAIL)) {
                dirSize += files[i].length();
            }
        }

        if (dirSize > CACHE_SIZE * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > calculateSdCardFreeSpace()) {
            int removeFactor = (int) (0.4 * files.length);
            Arrays.sort(files, new FileLastModifiedSort());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(CACHETAIL)) {
                    files[i].delete();
                }
            }
        }

        if (calculateSdCardFreeSpace() <= CACHE_SIZE) {
            return false;
        }

        return true;
    }

    /**
     * 计算SD卡上的剩余空间
     */
    private int calculateSdCardFreeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
        return (int) sdFreeMB;
    }

    /**
     * 将url转成文件名
     * @param url
     * @return
     */
    private String convertUrlToFileName(String url) {
        return MD5Util.getMD5String(url) + CACHETAIL;
    }

    private class FileLastModifiedSort implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {

            if(lhs.lastModified() > rhs.lastModified()) {
                return 1;
            } else if(lhs.lastModified() == rhs.lastModified()) {
                return 0;
            } else {
                return -1;
            }

        }
    }
}
