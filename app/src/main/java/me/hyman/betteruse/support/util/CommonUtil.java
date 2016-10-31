package me.hyman.betteruse.support.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import me.hyman.betteruse.base.WeiboApplication;

/**
 * Created by Hyman on 2015/9/26.
 */
public class CommonUtil {

    public static void copyToClipboard(String text) {
        // 得到剪贴板管理器
        try {
            ClipboardManager cmb = (ClipboardManager) WeiboApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText(null, text.trim()));
        } catch (Exception e) {
        }
    }

    public static void launchBrowser(Activity from, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        from.startActivity(intent);
    }

    public static Intent getShareIntent(String title, String content, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra("imageURL", url);

        /*if (!TextUtils.isEmpty(url)) {
            File file = BitmapLoader.getInstance().getCacheFile(url);

            if (file.exists()) {
                shareIntent.setType("image*//*");

                Uri uri = Uri.fromFile(file);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }*/

        if (TextUtils.isEmpty(content)) {
            content = title;
        } else {
            if (!TextUtils.isEmpty(title))
                shareIntent.putExtra(Intent.EXTRA_TITLE, title);
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        return shareIntent;
    }

    public static int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) WeiboApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) WeiboApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int dip2px(int dipValue) {
        float reSize = WeiboApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    public static int px2dip(int pxValue) {
        float reSize = WeiboApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((pxValue / reSize) + 0.5);
    }

    public static float sp2px(int spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, WeiboApplication.getInstance().getResources().getDisplayMetrics());
    }

}
