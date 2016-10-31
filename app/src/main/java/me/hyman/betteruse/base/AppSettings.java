package me.hyman.betteruse.base;

import me.hyman.betteruse.support.data.SharedPreferencesHelper;
import me.hyman.betteruse.support.util.AppConst;

/**
 * app的设置
 */
public class AppSettings {

    private static String KEY_SETTING_THEME_COLOR = "key_setting_theme_color";     // 主题颜色
    private static String KEY_SETTING_AUTO_REFRESH = "key_setting_auto_refresh";   // 进入时是否自动刷新，默认是
    private static String KEY_SETTING_AUTO_LOADMORE = "key_setting_auto_loadMore"; // 下拉是否自动加载更多，默认否
    private static String KEY_SETTING_SHOW_REMARK = "key_setting_show_remark";     // 是否显示备注，默认是
    private static String KEY_SETTING_USE_INNER_BROWSER = "key_setting_use_inner_browser";     // 是否使用内置浏览器，默认 是
    private static String KEY_SETTING_TIMELINE_REFRESH_COUNT = "key_setting_timeline_refresh_count";   // 一次刷新加载微博条数，默认20
    private static String KEY_SETTING_TEXT_SIZE = "key_setting_text_size";         // 字体大小
    private static String KEY_SETTING_SHOW_PIC = "key_setting_show_pic";           // 是否显示微博图片，默认是
    private static String KEY_SETTING_PIC_MODE = "key_setting_pic_mode";           // 图片质量


    public static int getThemeColor() {
        return (int) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_THEME_COLOR, 4);
    }

    public static void setThemeColor(int value) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_THEME_COLOR, value);
    }

    public static boolean getAutoRefreshSetting() {
        return (boolean) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_AUTO_REFRESH, true);
    }

    public static void setAutoRefreshSetting(boolean flag) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_AUTO_REFRESH, flag);
    }

    public static boolean getAutoLoadMoreSetting() {
        return (boolean) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_AUTO_LOADMORE, false);
    }

    public static void setAutoLoadMoreSetting(boolean flag) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_AUTO_LOADMORE, flag);
    }

    public static boolean getIsShowRemark() {
        return (boolean) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_SHOW_REMARK, true);
    }

    public static void setIsShowRemark(boolean flag) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_SHOW_REMARK, flag);
    }

    public static boolean getIsShowPic() {
        return (boolean) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_SHOW_PIC, true);
    }

    public static void setIsShowPic(boolean flag) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_SHOW_PIC, flag);
    }

    public static boolean getUseInnerBrowser() {
        return (boolean) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_USE_INNER_BROWSER, true);
    }

    public static void setUseInnerBrowser(boolean flag) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_USE_INNER_BROWSER, flag);
    }

    // 保存的是对应的 value
    public static int getTimelineRefreshCountValue() {
        return (int) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_TIMELINE_REFRESH_COUNT, 0);
    }

    public static void setTimelineRefreshCountValue(int value) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_TIMELINE_REFRESH_COUNT, value);
    }

    public static int getTextSize() {
        return (int) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_TEXT_SIZE, AppConst.TEXT_SIZE_MIDDLE);
    }

    public static void setTextSize(int size) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_TEXT_SIZE, size);
    }

    public static int getPictureMode() {
        return (int) SharedPreferencesHelper.getInstance().getData(KEY_SETTING_PIC_MODE, AppConst.PIC_MODE_MIDDLE);
    }

    public static void setPictureMode(int mode) {
        SharedPreferencesHelper.getInstance().saveData(KEY_SETTING_PIC_MODE, mode);
    }
}
