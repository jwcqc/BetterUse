package me.hyman.betteruse.ui.fragment.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import me.hyman.betteruse.R;
import me.hyman.betteruse.support.util.IntentUtils;


public class AdvancedSettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Preference pAccountMgr; // 账号设置


    public AdvancedSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_advanced_item);

        pAccountMgr =  findPreference("setting_account_mgr");
        pAccountMgr.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equals("setting_account_mgr")) {
            IntentUtils.startAccountActivity(getActivity());
        }

        return true;
    }
}
