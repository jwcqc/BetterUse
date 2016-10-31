package me.hyman.betteruse.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppContext;
import me.hyman.betteruse.base.AppSettings;
import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.db.dao.AccountDao;
import me.hyman.betteruse.support.util.AccessTokenKeeper;
import me.hyman.betteruse.support.util.AppConst;
import me.hyman.betteruse.support.util.MyToast;
import me.hyman.betteruse.support.util.ThemeUtils;
import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.view.CircleImageView;


/**
 * 账户管理activity
 */
public class AccountActivity extends BaseAppCompatActivity {

    private static final String TAG = AccountActivity.class.getName();

    private ListView lvAccount;

    private Toolbar toolbar;

    private AccountDao accountService;

    private View emptyView;
    private View divider;  //list列表下面的那条分割线
    private TextView emptyViewAccountAdd;

    /**
     * 保存当前授权用户的信息
     */
    private AccountBean accountBean;

    private List<AccountBean> accountList;

    private AccountListAdapter accountAdapter;


    private AuthInfo mAuthInfo;
    private Oauth2AccessToken mAccessToken;
    private SsoHandler mSsoHandler;

    private boolean alreadyOnPause = false;


    @Override
    protected int getContentViewId() {
        return R.layout.activity_account;
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {

        setRunningActivity(this);

        accountService = new AccountDao(AccountActivity.this);

        initView();
        initData();
        initEvent();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_account_mgr);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("授权账号管理");
        actionBar.setDisplayHomeAsUpEnabled(true);

        lvAccount = (ListView) findViewById(R.id.lv_account_mgr);

        emptyView = findViewById(R.id.layoutEmpty);
        emptyViewAccountAdd = (TextView) findViewById(R.id.btnAccountAdd);

        divider = findViewById(R.id.divider);
    }

    private void initData() {
        // 去数据库查询所有已授权用户信息
        accountList = accountService.getAllAccounts();

        accountAdapter = new AccountListAdapter(accountList, AccountActivity.this);
        lvAccount.setAdapter(accountAdapter);
    }

    private void initEvent() {

        // 不需要listview的点击事件
       /* lvAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 设置当前用户
                AppContext.loginAccount = accountList.get(position);

                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                AccountActivity.this.finish();
            }
        });*/

        if(accountList == null || accountList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.GONE);
            emptyViewAccountAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuthInfo = new AuthInfo(AccountActivity.this, AppConst.APP_KEY, AppConst.REDIRECT_URL, AppConst.SCOPE);
                    mSsoHandler = new SsoHandler(AccountActivity.this, mAuthInfo);
                    mSsoHandler.authorize(new AuthListener());
                }
            });
        } else {
            emptyView.setVisibility(View.GONE);
            divider.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onPause() {
        alreadyOnPause = true;
        super.onPause();
    }

    @Override
    protected void onResume() {

        if(alreadyOnPause) {
            getNewGrantData();
        }
        super.onResume();
    }

    // 获取新授权的信息
    private void getNewGrantData() {

        final Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(AccountActivity.this);
        if(!TextUtils.isEmpty(accessToken.getUid())) {
            accountBean = new AccountBean();
            accountBean.setUserId(accessToken.getUid());
            accountBean.setAccessToken(accessToken.getToken());
            accountBean.setRefreshToken(accessToken.getRefreshToken());
            accountBean.setExpires_in(accessToken.getExpiresTime());
            // 这儿应该是新添加的为默认，设为1，然后后续会再去修改数据库中其他账号，把其他账号都设为0
            accountBean.setIsDefault("1");

            UsersAPI mUsersAPI = new UsersAPI(AccountActivity.this, AppConst.APP_KEY, accessToken);
            long uid = Long.parseLong(accessToken.getUid());
            mUsersAPI.show(uid, mListener);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_account) {
            mAuthInfo = new AuthInfo(this, AppConst.APP_KEY, AppConst.REDIRECT_URL, AppConst.SCOPE);
            mSsoHandler = new SsoHandler(AccountActivity.this, mAuthInfo);
            mSsoHandler.authorize(new AuthListener());
            return true;

        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(accountBean != null) {

                //TODO  ========== 判断重复 ===========
                //TODO  ========== 如果重复了，不必提醒，直接覆盖掉原来的授权记录即可 ===========

                //  先把以保存的所有授权账户全部设为非默认
                accountService.updateSavedAccountDefaultInfo("0");

                // 再保存新的授权到数据库
                accountService.insertAccount(accountBean);

                // 页面显示
                if(accountList == null) {
                    accountList = new ArrayList<AccountBean>();
                }
                accountList.add(accountBean);
                accountAdapter.setData(accountList);

                // 保存到数据库后清除数据
                AccessTokenKeeper.clear(AccountActivity.this);

                emptyView.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
            }
        }
    };


    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     *
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {

               // 暂时先保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(AccountActivity.this, mAccessToken);
                Toast.makeText(AccountActivity.this, R.string.auth_succeed, Toast.LENGTH_SHORT).show();

            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.auth_fail);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(AccountActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(AccountActivity.this, R.string.auth_cancel, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
           Toast.makeText(AccountActivity.this, "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {

            if (!TextUtils.isEmpty(response)) {
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {

                    accountBean.setUserName(user.screen_name);
                    //accountBean.setProfileImageUrl(user.profile_image_url);
                    accountBean.setProfileImageUrl(user.avatar_large); //这个地方还是用高清图吧
                    accountBean.setDescription(user.description);

                    handler.sendEmptyMessage(1);

                } else {
                    Toast.makeText(AccountActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(AccountActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private class AccountListAdapter extends BaseAdapter {

        private List<AccountBean> accountList;
        private Context context;

        public AccountListAdapter(List<AccountBean> accountList, Context context) {
            if(accountList == null) {
                this.accountList = new ArrayList<AccountBean>();
            } else {
                this.accountList = accountList;
            }

            this.context = context;
        }

        public void setData(List<AccountBean> newList) {
            this.accountList = newList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return accountList.size();
        }

        @Override
        public AccountBean getItem(int position) {
            return accountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final AccountBean account = accountList.get(position);
            if(convertView == null) {

                convertView = LayoutInflater.from(context).inflate(R.layout.item_lv_account, null);
                CircleImageView imgAvatar = (CircleImageView) convertView.findViewById(R.id.img_account_avatar);
                TextView txtAccountName = (TextView) convertView.findViewById(R.id.txtAccountName);
                TextView txtAccountDesc = (TextView) convertView.findViewById(R.id.txtAccountDesc);
                TextView txtAccountExpired = (TextView) convertView.findViewById(R.id.txtAccountExpired);
                ImageView imgAccountDelete = (ImageView) convertView.findViewById(R.id.img_account_delete);
                RelativeLayout lytAccountDesc = (RelativeLayout) convertView.findViewById(R.id.lyt_account_desc);

                txtAccountName.setText(account.getUserName());
                txtAccountDesc.setText(account.getDescription());


                // TODO 账号授权过期的显示待处理

                if(account.getUserIcon() == null) {

                    ImageLoader.getInstance().displayImage(account.getProfileImageUrl(), imgAvatar, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String url, View view, Bitmap bitmap) {
                            System.out.println("success");
                            if (url.equals(account.getProfileImageUrl())) {
                                if (bitmap != null) {

                                    // 将头像信息保存到数据库
                                    accountService.updateAccountHead(account.getUserId(), bitmap);
                                    account.setUserIcon(new BitmapDrawable(bitmap));
                                }
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });

                } else {
                    imgAvatar.setImageDrawable(account.getUserIcon());
                }

                imgAccountDelete.setTag(account);
                imgAccountDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                        builder.setMessage("确定删除该账号吗？");
                        //builder.setTitle("");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                AccountBean bean = (AccountBean) v.getTag();
                                if (bean != null) {
                                    accountService.deleteAccount(bean.getUserId());
                                    accountList.remove(bean);
                                    notifyDataSetChanged();

                                    if (accountList.size() == 0) {
                                        emptyView.setVisibility(View.VISIBLE);
                                        divider.setVisibility(View.GONE);
                                    }
                                } else {
                                    MyToast.showShort(getApplicationContext(), "出错了，bean为空");
                                }
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                lytAccountDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // 设置当前用户
                        AppContext.loginAccount = account;

                        Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                        startActivity(intent);
                        AccountActivity.this.finish();
                    }
                });

            }

            return convertView;
        }
    }
}
