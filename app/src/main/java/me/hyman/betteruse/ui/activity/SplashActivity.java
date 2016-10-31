package me.hyman.betteruse.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.hyman.betteruse.R;
import me.hyman.betteruse.base.AppContext;
import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.db.dao.AccountDao;


public class SplashActivity extends AppCompatActivity {

    private AccountDao accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = SplashActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        accountService = new AccountDao(SplashActivity.this);
        delayToMain();
    }

    private void delayToMain() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                jump();
            }
        }, 1200);
    }

    private void jump() {

        // 在这里判断，如果已经有了默认账号，则直接进去MainActivity
        // 如果没有授权账号才去账号管理界面

        // 先拿出所有已授权账户
        List<AccountBean> accountList = accountService.getAllAccounts();

        if(accountList == null || accountList.isEmpty()) {

            toAccount();

        } else {

            for(int i=0; i<accountList.size(); i++) {

                if(accountList.get(i).getIsDefault().equals("1")) {
                    // 设置当前用户
                    AppContext.loginAccount = accountList.get(i);
                    toMain();
                    break;
                }
            }

            // 如果还是为空，没有设置默认账号，则取第一个算了
            if(AppContext.loginAccount == null) {
                AppContext.loginAccount = accountList.get(0);
                toMain();
            }

        }

    }

    private void toMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void toAccount() {
        Intent intent = new Intent(SplashActivity.this, AccountActivity.class);
        startActivity(intent);
        finish();
    }

}
