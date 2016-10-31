package me.hyman.betteruse.support.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast统一管理类
 *
 * @author hyman
 *
 */
public class MyToast {

	private static Toast toast;

	/**
	 * 短时间显示Toast
	 * @param context
	 * @param message
	 */
	public static void showShort(Context context, CharSequence message) {
		if(toast == null) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	public static void showShort(Context context, int messageId) {
		if(toast == null) {
			toast = Toast.makeText(context, messageId, Toast.LENGTH_SHORT);
		} else {
			toast.setText(messageId);
		}
		toast.show();
	}


	/**
	 * 长时间显示Toast
	 * @param context
	 * @param message
	 */
	public static void showLong(Context context, CharSequence message) {
		if(toast == null) {
			toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	public static void showLong(Context context, int messageId) {
		if(toast == null) {
			toast = Toast.makeText(context, messageId, Toast.LENGTH_LONG);
		} else {
			toast.setText(messageId);
		}
		toast.show();
	}

	/**
	 * 自定义时间显示Toast
	 * @param context
	 * @param message
	 * @param duration
	 */
	public static void show(Context context, CharSequence message, int duration) {
		if(toast == null) {
			toast = Toast.makeText(context, message, duration);
		} else {
			toast.setText(message);
			toast.setDuration(duration);
		}
		toast.show();
	}

	public static void show(Context context, int messageId, int duration) {
		if(toast == null) {
			toast = Toast.makeText(context, messageId, duration);
		} else {
			toast.setText(messageId);
			toast.setDuration(duration);
		}
		toast.show();
	}
}
