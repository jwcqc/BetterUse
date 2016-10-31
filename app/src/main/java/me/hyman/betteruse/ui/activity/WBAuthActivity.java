package me.hyman.betteruse.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import me.hyman.betteruse.R;
import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.util.AppConst;
import me.hyman.betteruse.support.util.MyToast;


/**
 * @author SINA & hyman
 *
 */
public class WBAuthActivity extends Activity {

    private static final String TAG = WBAuthActivity.class.getName();

    private AuthInfo mAuthInfo;

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

    /**
     * 保存当前授权用户的信息
     */
    private AccountBean accountBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        mAuthInfo = new AuthInfo(this, AppConst.APP_KEY, AppConst.REDIRECT_URL, AppConst.SCOPE);
        mSsoHandler = new SsoHandler(WBAuthActivity.this, mAuthInfo);
        mSsoHandler.authorize(new AuthListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {

                // 保存 Token 到 SharedPreferences
                // AccessTokenKeeper.writeAccessToken(WBAuthActivity.this, mAccessToken);

                // 调用UsersAPI
                /*UsersAPI mUsersAPI = new UsersAPI(WBAuthActivity.this, AppConst.APP_KEY, mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mListener);*/

                accountBean = new AccountBean();
                accountBean.setUserId(mAccessToken.getUid());
                accountBean.setAccessToken(mAccessToken.getToken());
                accountBean.setExpires_in(mAccessToken.getExpiresTime());

                Map params = new HashMap();
                params.put("uid", mAccessToken.getUid());
                params.put("accessToken", mAccessToken.getToken());

                Intent intent = new Intent(WBAuthActivity.this, AccountActivity.class);
                startActivity(intent);

                WBAuthActivity.this.finish();

            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = "授权失败";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                MyToast.showShort(getApplicationContext(), message);
                WBAuthActivity.this.finish();
            }
        }

        @Override
        public void onCancel() {
            MyToast.showShort(getApplicationContext(), "已取消授权");
            WBAuthActivity.this.finish();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            MyToast.showShort(getApplicationContext(), "Auth exception : " + e.getMessage());
            WBAuthActivity.this.finish();
        }
    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {

            if (!TextUtils.isEmpty(response)) {

                // 输出日志信息
                LogUtil.i(TAG, response);

                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);

                if (user != null) {

                    // ======================================================================================
                    System.out.println("idStr: " + user.idstr + " - status:" + user.status + "-Id:" + user.id +
                            " - screen_name: " + user.screen_name + " - name: " + user.name );
                    // ======================================================================================

                    /*if(userInfo != null) {
                        userInfo.setUserName(user.screen_name);
                        userInfo.setUserIconUrl(user.profile_image_url);

                        // 再保存 Token 到 数据库
                        uService.insertUserInfo(userInfo);

                        Toast.makeText(WBAuthActivity.this,
                                R.string.toast_auth_success, Toast.LENGTH_SHORT).show();

                        // 获取头像的操作因需要联网，不再此处进行，交给service
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put(AppConst.USER, userInfo);
                        Task task = new Task(Task.GET_USER_HEAD, params);
                        MainService.newTask(task);

                    }*/

                   /* if(accountBean != null) {
                        accountBean.setUserName(user.screen_name);
                        accountBean.setProfileImageUrl(user.profile_image_url);

                        MyToast.showShort(getApplicationContext(), "=====授权成功！");

                        Intent intent = new Intent(WBAuthActivity.this, AccountActivity.class);
                        intent.putExtra("username", accountBean.getUserName());
                        startActivity(intent);
                        WBAuthActivity.this.finish();
                    }*/

                } else {
                    Toast.makeText(WBAuthActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WBAuthActivity.this, info.toString(), Toast.LENGTH_LONG).show();
            WBAuthActivity.this.finish();
        }
    };

}
