package me.hyman.betteruse.support.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.hyman.betteruse.support.bean.CacheBean;
import me.hyman.betteruse.support.db.DBHelper;

/**
 * Created by Hyman on 2016/6/10.
 */
public class CacheDao {

    private DBHelper dbHelper;

    private SQLiteDatabase db;

    private String[] columns = {CacheBean.ID, CacheBean.USER_ID, CacheBean.DESCRIPTION,
            CacheBean.RESULT, CacheBean.FLAG, CacheBean.CREATEDAT };

    public CacheDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 插入缓存数据
     * @param result
     */
    public void saveCacheData(String result, String userId, String flag, boolean deleteOld) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        //删除之前的数据，再保存（保证每次只保存最新刷新的20条即可）
        // 如果刷新到的条数少于设置的每次刷新值，则不执行删除旧数据
        if(deleteOld) {
            db.delete(CacheBean.TB_NAME,
                    CacheBean.USER_ID + "=? and " + CacheBean.FLAG + "=?", new String[]{userId, flag});
        }

        ContentValues values = new ContentValues();
        values.put(CacheBean.USER_ID, userId);
        values.put(CacheBean.DESCRIPTION, "");
        values.put(CacheBean.RESULT, result);
        values.put(CacheBean.FLAG, flag);
        values.put(CacheBean.CREATEDAT, new Date().toString());
        db.insert(CacheBean.TB_NAME, null, values);

        //设置成功
        db.setTransactionSuccessful();
        //结束事务
        db.endTransaction();
        //关闭数据库
        db.close();
    }

    public List<CacheBean> getCacheData(String userId, String flag) {
        db = dbHelper.getReadableDatabase();
        List<CacheBean> list = null;

        Cursor cursor = db.query(CacheBean.TB_NAME, columns,
                CacheBean.USER_ID + "=? and " + CacheBean.FLAG + "=?",
                new String[]{userId, flag}, null, null, CacheBean.ID + " desc");
        if(cursor != null && cursor.getCount() > 0) {

            list = new ArrayList<CacheBean>();
            CacheBean bean = null;

            while(cursor.moveToNext()) {

                long id = cursor.getLong(cursor.getColumnIndex(CacheBean.ID));
                String uId = cursor.getString(cursor.getColumnIndex(CacheBean.USER_ID));
                String description = cursor.getString(cursor.getColumnIndex(CacheBean.DESCRIPTION));
                String result = cursor.getString(cursor.getColumnIndex(CacheBean.RESULT));
                String fl = cursor.getString(cursor.getColumnIndex(CacheBean.FLAG));
                String createdAt = cursor.getString(cursor.getColumnIndex(CacheBean.CREATEDAT));

                bean = new CacheBean();
                bean.setId(id);
                bean.setUserId(uId);
                bean.setDescription(description);
                bean.setResult(result);
                bean.setFlag(fl);
                bean.setCreatedAt(createdAt);

                list.add(bean);
            }

        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean deleteAll(String userId, String flag) {
        db = dbHelper.getWritableDatabase();
        int i = db.delete(CacheBean.TB_NAME,
                CacheBean.USER_ID + "=? and " + CacheBean.FLAG + "=?", new String[]{userId, flag});
        db.close();
        return i > 0;
    }
}
