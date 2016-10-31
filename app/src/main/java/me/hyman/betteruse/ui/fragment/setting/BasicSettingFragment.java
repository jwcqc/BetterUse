package me.hyman.betteruse.ui.fragment.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.util.DataCleanManager;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.MainActivity;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;


public class BasicSettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private Preference pChooseTheme;// 主题设置

    private CheckBoxPreference pShowRemark;   // 显示备注
    private CheckBoxPreference pAutoRefresh;  // 列表自动刷新
    private CheckBoxPreference pAutoLoadMore; // 底部自动加载更多
    private CheckBoxPreference pUseInnerBrowser; // 使用内置浏览器
    private CheckBoxPreference pWifiOptimize;// wifi下优化设置

    private ListPreference pPicQuality;      // 图片质量
    private ListPreference pTimelineCount;   // 一次微博加载条数
    private ListPreference pTextSize;         // 字体大小

    private Preference pClearCache;// 清理缓存
    private Preference pFeedback;  // 意见反馈
    private Preference pVersion;   // 版本信息
    private Preference pAbout;     // 关于

    public BasicSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_basic_item);

        pTextSize = (ListPreference) findPreference("setting_list_font_size");
        pTextSize.setOnPreferenceChangeListener(this);
        pTextSize.setDefaultValue(AppSettings.getTextSize());
        pTextSize.setSummary(getResources().getStringArray(R.array.setting_text_size)[AppSettings.getTextSize()]);

        pShowRemark = (CheckBoxPreference) findPreference("setting_show_remark");
        pShowRemark.setOnPreferenceChangeListener(this);
        pShowRemark.setChecked(AppSettings.getIsShowRemark());

        pAutoRefresh = (CheckBoxPreference) findPreference("setting_auto_refresh");
        pAutoRefresh.setOnPreferenceChangeListener(this);
        pAutoRefresh.setChecked(AppSettings.getAutoRefreshSetting());

        pAutoLoadMore = (CheckBoxPreference) findPreference("setting_auto_load_more");
        pAutoLoadMore.setOnPreferenceChangeListener(this);
        pAutoLoadMore.setChecked(AppSettings.getAutoLoadMoreSetting());

        pUseInnerBrowser = (CheckBoxPreference) findPreference("setting_auto_use_inner_browser");
        pUseInnerBrowser.setOnPreferenceChangeListener(this);
        pUseInnerBrowser.setChecked(AppSettings.getUseInnerBrowser());

        pAbout = findPreference("setting_about");
        pAbout.setOnPreferenceClickListener(this);

        pChooseTheme =  findPreference("setting_choose_theme");
        pChooseTheme.setOnPreferenceClickListener(this);

        pPicQuality = (ListPreference) findPreference("setting_list_pic_quality");
        pPicQuality.setOnPreferenceChangeListener(this);
        pPicQuality.setDefaultValue(AppSettings.getPictureMode());
        pPicQuality.setSummary(getResources().getStringArray(R.array.setting_pic_quality)[AppSettings.getPictureMode()]);

        pTimelineCount = (ListPreference) findPreference("setting_list_timeline_count");
        pTimelineCount.setOnPreferenceChangeListener(this);
        pTimelineCount.setDefaultValue(AppSettings.getTimelineRefreshCountValue());
        pTimelineCount.setSummary(getResources().getStringArray(R.array.setting_timeline_count)[AppSettings.getTimelineRefreshCountValue()]);


        // 获取缓存大小
        try {
            String cacheSize = DataCleanManager.getCacheSize(getActivity());
            pClearCache = findPreference("setting_clear_cache");
            pClearCache.setSummary(cacheSize);
            pClearCache.setOnPreferenceClickListener(this);
            //Logger.i("BasicSettingFragment - 获取到的应用缓存大小：" + cacheSize);
        } catch (Exception e) {
            Logger.e("BasicSettingFragment - 获取应用缓存大小时出错！");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Logger.i(preference.getKey());

        if(preference.getKey().equals("setting_show_remark")) {

            AppSettings.setIsShowRemark(Boolean.parseBoolean(newValue.toString()));

        } else if(preference.getKey().equals("setting_auto_refresh")) {

            AppSettings.setAutoRefreshSetting(Boolean.parseBoolean(newValue.toString()));

        } else if(preference.getKey().equals("setting_auto_load_more")) {

            AppSettings.setAutoLoadMoreSetting(Boolean.parseBoolean(newValue.toString()));

        } else if(preference.getKey().equals("setting_auto_use_inner_browser")) {

            AppSettings.setUseInnerBrowser(Boolean.parseBoolean(newValue.toString()));

        } else if(preference.getKey().equals("setting_list_font_size")) {

            if(preference instanceof ListPreference){

                AppSettings.setTextSize(setSummaryAndGetIndex(preference, newValue));
            }
        } else if(preference.getKey().equals("setting_list_timeline_count")) {

            if(preference instanceof ListPreference){

                AppSettings.setTimelineRefreshCountValue(setSummaryAndGetIndex(preference, newValue));
            }
        } else if(preference.getKey().equals("setting_list_pic_quality")) {

            if(preference instanceof ListPreference){

                AppSettings.setPictureMode(setSummaryAndGetIndex(preference, newValue));
            }
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equals("setting_about")) {

            MyToast.showShort(getActivity(), "作者先不告诉你哈");

        } else if(preference.getKey().equals("setting_clear_cache")) {

            new AlertDialog.Builder(getActivity())
                    .setMessage("是否清空缓存？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 缓存的数据库文件还没删
                            DataCleanManager.cleanApplicationData(getActivity());
                            dialog.dismiss();

                            MyToast.showLong(getActivity(), "已成功清除缓存");
                            pClearCache.setSummary("0M");
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
            }).show();

        } else if(preference.getKey().equals("setting_choose_theme")) {

            int theme = AppSettings.getThemeColor();
            Logger.e("before: " + theme);
            theme = (theme + 1) % ThemeUtils.themeArr.length;
            AppSettings.setThemeColor(theme);
            Logger.e("after: " + theme);
            ((BaseAppCompatActivity)getActivity()).reload();

        } else {
            MyToast.showShort(getActivity(), "别到处乱点，还没实现");
        }

        return true;
    }

    private int setSummaryAndGetIndex(Preference preference, Object newValue) {
        //把preference强制转化为ListPreference类型
        ListPreference listPreference = (ListPreference)preference;
        //获取ListPreference中的实体内容
        CharSequence[] entries = listPreference.getEntries();
        //获取ListPreference中的实体内容的下标值
        int index = listPreference.findIndexOfValue((String)newValue);
        //把listPreference中的摘要显示为当前ListPreference的实体内容中选择的那个项目
        listPreference.setSummary(entries[index]);
        return index;
    }
}
