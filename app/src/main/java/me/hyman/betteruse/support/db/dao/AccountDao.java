package me.hyman.betteruse.support.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import me.hyman.betteruse.support.bean.AccountBean;
import me.hyman.betteruse.support.db.DBHelper;


/**
 * Created by Hyman on 2015/9/14.
 */
public class AccountDao {

    private DBHelper dbHelper;

    private String[] columns = {AccountBean.ID, AccountBean.USER_ID, AccountBean.USER_NAME, AccountBean.DESCRIPTION,
            AccountBean.ACCESS_TOKEN, AccountBean.REFRESH_TOKEN, AccountBean.IS_DEFAULT,
            AccountBean.USER_ICON, AccountBean.EXPIRES, AccountBean.PROFILE_IMAGE_URL };

    public AccountDao(Context context) {
        super();
        dbHelper = new DBHelper(context);
    }

    /**
     * 添加授权账户
     */
    public void insertAccount(AccountBean account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(8);

        values.put(AccountBean.USER_ID, account.getUserId());
        values.put(AccountBean.USER_NAME, account.getUserName());
        values.put(AccountBean.DESCRIPTION, account.getDescription());
        values.put(AccountBean.ACCESS_TOKEN,account.getAccessToken());
        values.put(AccountBean.REFRESH_TOKEN, account.getRefreshToken());
        values.put(AccountBean.IS_DEFAULT,account.getIsDefault());
        values.put(AccountBean.EXPIRES, account.getExpires_in());
        values.put(AccountBean.PROFILE_IMAGE_URL, account.getProfileImageUrl());

        db.insert(AccountBean.TB_NAME, null, values);
        db.close();
    }

    /**
     * 根据用户ID获取用户对象
     * @param userId
     * @return
     */
    public AccountBean getAccountBeanByUserId(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        AccountBean account = null;
        Cursor cursor = db.query(AccountBean.TB_NAME, columns,
                AccountBean.USER_ID + "=?", new String[]{userId}, null, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                account = new AccountBean();

                Long id = cursor.getLong(cursor.getColumnIndex(AccountBean.ID));
                String uId = cursor.getString(cursor.getColumnIndex(AccountBean.USER_ID));
                String userName = cursor.getString(cursor.getColumnIndex(AccountBean.USER_NAME));
                String description = cursor.getString(cursor.getColumnIndex(AccountBean.DESCRIPTION));
                String accessToken = cursor.getString(cursor.getColumnIndex(AccountBean.ACCESS_TOKEN));
                String refreshToken = cursor.getString(cursor.getColumnIndex(AccountBean.REFRESH_TOKEN));
                String isDefault = cursor.getString(cursor.getColumnIndex(AccountBean.IS_DEFAULT));
                String profileImgUrl = cursor.getString(cursor.getColumnIndex(AccountBean.PROFILE_IMAGE_URL));
                Long expires_in = cursor.getLong(cursor.getColumnIndex(AccountBean.EXPIRES));
                byte[] byteIcon = cursor.getBlob(cursor.getColumnIndex(AccountBean.USER_ICON));

                account.setUserId(uId);
                account.setUserName(userName);
                account.setDescription(description);
                account.setAccessToken(accessToken);
                account.setRefreshToken(refreshToken);
                account.setIsDefault(isDefault);
                account.setProfileImageUrl(profileImgUrl);
                account.setExpires_in(expires_in);

                if(null != byteIcon) {
                    ByteArrayInputStream is = new ByteArrayInputStream(byteIcon);
                    Drawable userIcon = Drawable.createFromStream(is, "image");
                    account.setUserIcon(userIcon);
                }
            }
        }
        cursor.close();
        db.close();
        return account;
    }

    /**
     * 查询所有的已授权用户信息
     * @return
     */
    public List<AccountBean> getAllAccounts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AccountBean> accounts = null;
        Cursor cursor = db.query(AccountBean.TB_NAME, columns, null, null, null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            accounts = new ArrayList<AccountBean>(cursor.getCount());
            AccountBean account = null;
            while(cursor.moveToNext()) {
                account = new AccountBean();

                Long id = cursor.getLong(cursor.getColumnIndex(AccountBean.ID));
                String uId = cursor.getString(cursor.getColumnIndex(AccountBean.USER_ID));
                String userName = cursor.getString(cursor.getColumnIndex(AccountBean.USER_NAME));
                String description = cursor.getString(cursor.getColumnIndex(AccountBean.DESCRIPTION));
                String accessToken = cursor.getString(cursor.getColumnIndex(AccountBean.ACCESS_TOKEN));
                String refreshToken = cursor.getString(cursor.getColumnIndex(AccountBean.REFRESH_TOKEN));
                String isDefault = cursor.getString(cursor.getColumnIndex(AccountBean.IS_DEFAULT));
                String profileImgUrl = cursor.getString(cursor.getColumnIndex(AccountBean.PROFILE_IMAGE_URL));
                Long expires_in = cursor.getLong(cursor.getColumnIndex(AccountBean.EXPIRES));
                byte[] byteIcon = cursor.getBlob(cursor.getColumnIndex(AccountBean.USER_ICON));

                account.setUserId(uId);
                account.setUserName(userName);
                account.setDescription(description);
                account.setAccessToken(accessToken);
                account.setRefreshToken(refreshToken);
                account.setIsDefault(isDefault);
                account.setProfileImageUrl(profileImgUrl);
                account.setExpires_in(expires_in);

                if(null != byteIcon) {
                    ByteArrayInputStream is = new ByteArrayInputStream(byteIcon);
                    Drawable userIcon = Drawable.createFromStream(is, "image");
                    account.setUserIcon(userIcon);
                }

                accounts.add(account);
            }
        }
        cursor.close();
        db.close();
        return accounts;
    }

    /**
     * 更新用户头像
     * @param userId
     * @param userIcon
     */
    public void updateAccountHead(String userId, Bitmap userIcon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        userIcon.compress(Bitmap.CompressFormat.PNG, 100, output);
        values.put(AccountBean.USER_ICON, output.toByteArray());

        db.update(AccountBean.TB_NAME, values, AccountBean.USER_ID + "=?", new String[]{userId});
        db.close();
    }

    /**
     * 默认全部设为0,
     * @param df
     */
    public void updateSavedAccountDefaultInfo(String df) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(AccountBean.IS_DEFAULT, df);

        db.update(AccountBean.TB_NAME, values, null, null);
        db.close();
    }

    /**
     * 删除已授权账户
     * @param userId
     */
    public void deleteAccount(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(AccountBean.TB_NAME, AccountBean.USER_ID + "=?", new String[]{userId});
        db.close();
    }
}
