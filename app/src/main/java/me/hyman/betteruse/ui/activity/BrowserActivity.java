package me.hyman.betteruse.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.util.CommonUtil;
import me.hyman.betteruse.support.util.Logger;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.view.MyToolbar;

public class BrowserActivity extends BaseAppCompatActivity {

    @Bind(R.id.webview)
    WebView mWebView;

    @Bind(R.id.progressbar_browser)
    SmoothProgressBar progressbar;

    @Bind(R.id.toolbar_browser)
    MyToolbar toolbar;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_browser;
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    @Override
    protected void onCreateView(Bundle bundle) {

        ButterKnife.bind(this);

        initToolbar(toolbar, "", R.drawable.ic_back);

        progressbar.setIndeterminate(true);

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    view.loadUrl("http://" + url);
                else
                    view.loadUrl(url);

                return true;
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressbar.setVisibility(View.VISIBLE);
                } else if (newProgress == 100) {
                    progressbar.setVisibility(View.GONE);
                    invalidateOptionsMenu();
                }
                progressbar.setProgress(newProgress);

                super.onProgressChanged(view, newProgress);
            }

        });


        if (bundle == null) {
            String url = null;
            String action = getIntent().getAction();
            if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && getIntent().getData() != null) {
                url = getIntent().getData().toString();
            } else {
                url = getIntent().getStringExtra("url");
            }
            if (url.startsWith("betteruse://"))
                url = url.replace("betteruse://", "");

            Logger.i("BrowserActivity: " + url);
            mWebView.loadUrl(url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);

        String shareContent = String.format("%s %s ", mWebView.getTitle() + "", mWebView.getUrl() + "");
        Intent shareIntent = CommonUtil.getShareIntent(shareContent, "", null);

        MenuItem shareItem = menu.findItem(R.id.share);
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        shareProvider.setShareHistoryFileName("channe_share.xml");
        shareProvider.setShareIntent(shareIntent);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == R.id.refresh) {
            mWebView.reload();

        } else if (item.getItemId() == R.id.copy) {
            CommonUtil.copyToClipboard(mWebView.getUrl());
            MyToast.showShort(BrowserActivity.this, R.string.msg_url_copyed);

        } else if (item.getItemId() == R.id.to_browser) {
            try {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(mWebView.getUrl());
                intent.setData(content_url);
                startActivity(intent);
            } catch (Exception e) { }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }
}
