package me.hyman.betteruse.support.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class NetUtil {

    public static final int NETWORK_NONE = -1;
    public static final int NETWORK_MOBILE = 0;
    public static final int NETWORK_WIFI = 1;

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return 返回具体的的网络类型
     */
    public static int getNetworkStatus(Context context) {

        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // wifi
        State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (state == State.CONNECTED || state == State.CONNECTING) {
            return NETWORK_WIFI;
        }

        // 手机数据网络
        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .getState();
        if (state == State.CONNECTED || state == State.CONNECTING) {
            return NETWORK_MOBILE;
        }

        // 没有连接到网络
        return NETWORK_NONE;
    }


    /**
     * 判断当前是否已连接网络
     *
     * @param context
     * @return
     */
    public static boolean getNetWorkStatus(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 请求服务器，获取返回数据
     * @param url
     * @return
     */
    public static String getWebContent(Context context, String url) {
        HttpGet httpRequest = new HttpGet(url);
        String strResult = "";
        if (NetUtil.getNetWorkStatus(context)) {
            try {
                // HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                // 获得HttpResponse对象
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    // 取得返回的数据
                    strResult = EntityUtils.toString(httpResponse.getEntity());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strResult; // 返回结果
    }

    /**
     * 请求服务器，获取返回数据
     * @param url
     * @return
     */
    public static String getWebContent(Context context, String url, String headerName, String headerValue) {
        HttpGet httpRequest = new HttpGet(url);
        httpRequest.addHeader(headerName, headerValue);
        String strResult = "";
        if (NetUtil.getNetWorkStatus(context)) {
            try {
                // HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                // 获得HttpResponse对象
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    // 取得返回的数据
                    strResult = EntityUtils.toString(httpResponse.getEntity());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strResult; // 返回结果
    }


    public static void main(String[] args) {

    }
}
