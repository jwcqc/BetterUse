package me.hyman.betteruse.support.util;

import android.content.Context;
import android.content.Intent;

import me.hyman.betteruse.ui.activity.AccountActivity;
import me.hyman.betteruse.ui.activity.SettingActivity;

/**
 * Created by Hyman on 2016/6/9.
 */
public class IntentUtils {

    public static void startSettingActivity(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), SettingActivity.class);
        context.startActivity(intent);
    }

    public static void startAccountActivity(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), AccountActivity.class);
        context.startActivity(intent);
    }
}
