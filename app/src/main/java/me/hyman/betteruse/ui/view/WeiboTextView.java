
package me.hyman.betteruse.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.base.WeiboApplication;
import me.hyman.betteruse.support.cache.img.LruMemoryCache;
import me.hyman.betteruse.support.db.EmotionDB;
import me.hyman.betteruse.support.textspan.ClickableTextViewMentionLinkOnTouchListener;
import me.hyman.betteruse.support.textspan.MyURLSpan;
import me.hyman.betteruse.support.util.BitmapUtil;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.MD5Util;


/**
 * 完成加载表情，添加链接两个功能
 */
public class WeiboTextView extends TextView {

	private static final String TAG = "WeiboTextView";

	private static final int CORE_POOL_SIZE = 5;

	/**
	 * 默认执行最大线程是128个
	 */
	private static final int MAXIMUM_POOL_SIZE = 128;

	private static final int KEEP_ALIVE_TIME = 1;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {

		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			int count = mCount.getAndIncrement();
			Logger.v(TAG, "new Thread " + "WeiboTextView #" + count);
			return new Thread(r, "WeiboTextView #" + count);
		}
	};

	/**
	 * 执行队列，默认是10个，超过10个后会开启新的线程，如果已运行线程大于 {@link #MAXIMUM_POOL_SIZE}，执行异常策略
	 */
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

	private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			sPoolWorkQueue, sThreadFactory);

	public static LruMemoryCache<String, SpannableString> stringMemoryCache;

	private EmotionAndUrlTask emotionAndUrlTask;

	private String content;

	private boolean innerWeb = AppSettings.getUseInnerBrowser();

	public WeiboTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WeiboTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WeiboTextView(Context context) {
		super(context);
	}

	public void setContent(String text) {
		if (stringMemoryCache == null) {
			stringMemoryCache = new LruMemoryCache<String, SpannableString>(200) { };
		}

		boolean replace = false;

		if (!replace)
			replace = innerWeb != AppSettings.getUseInnerBrowser();

		innerWeb = AppSettings.getUseInnerBrowser();

		if (!replace && TextUtils.isEmpty(text)) {
			super.setText(text);
			return;
		}

		if (!replace && !TextUtils.isEmpty(content) && content.equals(text))
			return;

		content = text;

		if (emotionAndUrlTask != null)
			emotionAndUrlTask.cancel(true);

		String key = MD5Util.getMD5String(text);
		SpannableString spannableString = stringMemoryCache.get(key);
		if (spannableString != null) {
			Logger.v(TAG, "从内存中加载spannable数据");

			super.setText(spannableString);
		} else {
            Logger.v(TAG, "开启线程，开始加载spannable数据");

			super.setText(text);
			emotionAndUrlTask = new EmotionAndUrlTask(this);
			emotionAndUrlTask.executeOnExecutor(THREAD_POOL_EXECUTOR, this.getText().toString());
		}

		setClickable(false);
		setOnTouchListener(onTouchListener);
	}

	static class EmotionAndUrlTask extends AsyncTask<String, SpannableString, Boolean> {

		WeakReference<TextView> textViewRef;

		EmotionAndUrlTask(TextView textView) {
			textViewRef = new WeakReference<>(textView);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			TextView textView = textViewRef.get();
			if (textView == null)
				return false;

			if (TextUtils.isEmpty(params[0]))
				return false;

			SpannableString spannableString = SpannableString.valueOf(params[0]);
			Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(spannableString);
			while (localMatcher.find()) {
				if (isCancelled())
					break;

				String key = localMatcher.group(0);

				int k = localMatcher.start();
				int m = localMatcher.end();

				byte[] data = EmotionDB.getEmotion(key);
				if (data == null)
					continue;

				/*MyBitmap mb = ImageLoader.getInstance().getImageCache().getBitmapFromMemCache(key, null);
				Bitmap b = null;
				if (mb != null) {
					b = mb.getBitmap();
				}
				else {
					b = BitmapFactory.decodeByteArray(data, 0, data.length);
					int size = WeiboApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.emotion_size);
					b = BitmapUtil.zoomBitmap(b, size);

					// 添加到内存中
					BitmapLoader.getInstance().getImageCache().addBitmapToMemCache(key, null, new MyBitmap(b, key));
				}*/

				Bitmap b = ImageLoader.getInstance().getMemoryCache().get(key);
				/*if(b == null) {
					b = BitmapFactory.decodeByteArray(data, 0, data.length);
					int size = WeiboApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.emotion_size);
					b = BitmapUtil.zoomBitmap(b, size);
					ImageLoader.getInstance().getMemoryCache().put(key, b);
				}*/

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

			publishProgress(spannableString);

			String key = MD5Util.getMD5String(spannableString.toString());
			stringMemoryCache.put(key, spannableString);
			Log.v(TAG, String.format("添加spannable到内存中，现在共有%d个spannable", stringMemoryCache.size()));
			//return null;
			return true;
		}

		@Override
		protected void onProgressUpdate(SpannableString... values) {
			super.onProgressUpdate(values);

			TextView textView = textViewRef.get();
			if (textView == null)
				return;

			try {
				if (values != null && values.length > 0)
					textView.setText(values[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private OnTouchListener onTouchListener = new OnTouchListener() {

		ClickableTextViewMentionLinkOnTouchListener listener = new ClickableTextViewMentionLinkOnTouchListener();

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return listener.onTouch(v, event);
		}
	};

}


