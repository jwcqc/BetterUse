package me.hyman.betteruse.support.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.openapi.models.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.base.WeiboApplication;
import me.hyman.betteruse.support.db.EmotionDB;
import me.hyman.betteruse.support.textspan.MyURLSpan;

/**
 * Created by Hyman on 2015/9/23.
 */
public class WeiboUtil {

    /**
     * 显示备注或微博名称
     * @return
     */
    public static String getScreenName(User user) {
        if(AppSettings.getIsShowRemark() && !TextUtils.isEmpty(user.remark)) {
            return user.remark;
        }
        return user.screen_name;
    }

    public static String formatCreateTime(String createTime) {

        Resources res = WeiboApplication.getInstance().getResources();

        StringBuffer buffer = new StringBuffer();

        Calendar createCal = Calendar.getInstance();
        createCal.setTimeInMillis(Date.parse(createTime));
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(System.currentTimeMillis());

        long diffTime = (currentCal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;

        // 同一月
        if (currentCal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");

            // 同一天
            if (currentCal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                if (diffTime < 3600 && diffTime >= 60) {
                    buffer.append((diffTime / 60) + res.getString(R.string.msg_few_minutes_ago));
                } else if (diffTime < 60) {
                    buffer.append(res.getString(R.string.msg_now));
                } else {
                    buffer.append(res.getString(R.string.msg_today)).append(" ").append(format.format(createCal.getTime()));
                }
            }
            // 前一天
            else if (currentCal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {
                buffer.append(res.getString(R.string.msg_yesterday)).append(" ").append(format.format(createCal.getTime()));
            }
        }

        if (buffer.length() == 0) {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            buffer.append(format.format(createCal.getTime()));
        }

        // 不在同一年，加年份
        String timeStr = buffer.toString();
        if (currentCal.get(Calendar.YEAR) != createCal.get(Calendar.YEAR)) {
            timeStr = createCal.get(Calendar.YEAR) + " " + timeStr;
        }
        return timeStr;
    }

    /**
     * 评论、转发数若很多，则需要格式化一下
     * @param count
     * @return
     */
    public static String formatCount(int count) {
        Resources res = WeiboApplication.getInstance().getResources();
        if (count < 10000)
            return String.valueOf(count);
        else if (count < 100 * 10000)
            return new DecimalFormat("#.0" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
        else
            return new DecimalFormat("#" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
    }

    /**
     * 处理各种认证
     * @param imgVerified
     * @param user
     */
    public static void setImageVerified(ImageView imgVerified, User user) {
        // 新增判断，VerifiedType存在为null的情况
        if (user == null) {
            imgVerified.setVisibility(View.GONE);
            return;
        }

        // 黄V
        if (user.verified_type == 0) {
            imgVerified.setImageResource(R.drawable.avatar_vip);
        }
        // 200:初级达人 220:高级达人
        else if (user.verified_type == 200 || user.verified_type == 220) {
            imgVerified.setImageResource(R.drawable.avatar_grassroot);
        }
        // 蓝V
        else if (user.verified_type > 0) {
            imgVerified.setImageResource(R.drawable.avatar_enterprise_vip);
        }
        if (user.verified_type >= 0)
            imgVerified.setVisibility(View.VISIBLE);
        else
            imgVerified.setVisibility(View.GONE);
    }


    public static SpannableString parseWeibo(String content) {

        SpannableString spannableString = SpannableString.valueOf(content);
        Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(spannableString);
        while (localMatcher.find()) {

            String key = localMatcher.group(0);

            int k = localMatcher.start();
            int m = localMatcher.end();

            byte[] data = EmotionDB.getEmotion(key);
            if (data == null) {
                System.out.println("data is null");

                continue;
            }

            Bitmap b = ImageLoader.getInstance().getMemoryCache().get(key);
            if(b == null) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);
                int size = WeiboApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.emotion_size);
                b = BitmapUtil.zoomBitmap(b, size);
                ImageLoader.getInstance().getMemoryCache().put(key, b);
            }

            ImageSpan l = new ImageSpan(WeiboApplication.getInstance(), b, ImageSpan.ALIGN_BASELINE);
            spannableString.setSpan(l, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        // 用户名称
        //Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        Pattern pattern = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
        String scheme = "me.hyman.betteruse.userinfo://";
        Linkify.addLinks(spannableString, pattern, scheme);

        // 网页链接
        scheme = "http://";
        // 启用内置浏览器
        if (AppSettings.getUseInnerBrowser())
            scheme = "betteruse://";
        Linkify.addLinks(spannableString, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), scheme);

        // 话题
        Pattern dd = Pattern.compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
        //Pattern dd = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
        scheme = "me.hyman.betteruse.topics://";
        Linkify.addLinks(spannableString, dd, scheme);

        URLSpan[] urlSpans = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
        MyURLSpan weiboSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboSpan = new MyURLSpan(urlSpan.getURL());
            int start = spannableString.getSpanStart(urlSpan);
            int end = spannableString.getSpanEnd(urlSpan);
            try {
                spannableString.removeSpan(urlSpan);
            } catch (Exception e) {
            }
            spannableString.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

}
