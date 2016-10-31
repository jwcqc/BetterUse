package me.hyman.betteruse.support.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import me.hyman.betteruse.base.WeiboApplication;
import me.hyman.betteruse.support.bean.Emotion;
import me.hyman.betteruse.support.bean.Emotions;
import me.hyman.betteruse.support.util.FileUtil;
import me.hyman.betteruse.support.util.Logger;


/**
 * Created by Hyman on 2015/9/24.
 */
public class EmotionDB {

    private static final String TAG = EmotionDB.class.getSimpleName();

    private static SQLiteDatabase emotionsDb;


    // 创建表情库
    static {
        String path = WeiboApplication.getInstance().getExternalCacheDir().getAbsolutePath() + File.separator + "emotions_v5.db";
        File dbf = new File(path);
        if (!dbf.exists()) {
            Logger.w(TAG, "表情DB不存在，执行新建");
            dbf.getParentFile().mkdirs();
            try {
                if (dbf.createNewFile())
                    emotionsDb = SQLiteDatabase.openOrCreateDatabase(dbf, null);
            } catch (IOException ioex) {
            }
        } else {
            Logger.w(TAG, "表情DB已存在");
            emotionsDb = SQLiteDatabase.openOrCreateDatabase(dbf, null);
        }
    }

    public static void checkEmotions() {

        Cursor cursor = null;
        boolean tableExist = false;

        try {
            //String sql = "SELECT COUNT(*) AS c FROM " + "sqlite_master" + " WHERE type ='table' AND name ='\" + EmotionTable.table + \"' ";
            String sql = "SELECT COUNT(*) AS c FROM " + "sqlite_master" + " WHERE type ='table' AND name ='" + EmotionTable.table + "' ";
            cursor = emotionsDb.rawQuery(sql, null);

            if(cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if(count > 0) {
                    tableExist = true;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
            cursor = null;
        }

        if(!tableExist) {
            Logger.v(TAG, "emotions table does't exist, create emotions table");

            String sql = String.format("create table %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT , %s TEXT, %s BOLB)", EmotionTable.table,
                    EmotionTable.id, EmotionTable.key, EmotionTable.file, EmotionTable.value);
            emotionsDb.execSQL(sql);
        } else {
            Logger.v(TAG, "emotions table exist");
        }

        boolean insertEmotions = true;
        // 表情不存在或者不全，插入表情
        try {
            cursor = emotionsDb.rawQuery(" select count(*) as c from " + EmotionTable.table, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                if (count == 159)
                    insertEmotions = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(insertEmotions) {
            Logger.v(TAG, "insert emotions");

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    InputStream in;
                    try {
                        in = WeiboApplication.getInstance().getAssets().open("emotions.properties");
                        Properties properties = new Properties();
                        properties.load(new InputStreamReader(in, "utf-8"));
                        Set<Object> keySet = properties.keySet();

                        // 开启事物
                        emotionsDb.beginTransaction();
                        emotionsDb.execSQL(String.format("delete from %s", EmotionTable.table));
                        for(Object key : keySet) {
                            String value = properties.getProperty(key.toString());

                            Logger.w(TAG, String.format("emotion's key(%s), value(%s)", key, value));

                            ContentValues values = new ContentValues();
                            values.put(EmotionTable.key, key.toString());
                            byte[] emotion = FileUtil.readStreamToBytes(WeiboApplication.getInstance().getAssets().open(value));
                            values.put(EmotionTable.value, emotion);
                            values.put(EmotionTable.file, value);

                            emotionsDb.insert(EmotionTable.table, EmotionTable.id, values);
                        }
                        // 结束事务
                        emotionsDb.setTransactionSuccessful();
                        emotionsDb.endTransaction();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute();

        } else {
            Logger.v(TAG, "emotions exist");
        }
    }


    public static byte[] getEmotion(String key) {
        Cursor cursor = emotionsDb.rawQuery(" SELECT " + EmotionTable.value + " FROM " + EmotionTable.table + " WHERE " + EmotionTable.key + " = ? ",
                new String[] { key });

        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(cursor.getColumnIndex(EmotionTable.value));
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    public static Emotions getEmotions(String type) {
        Emotions emotions = new Emotions();
        emotions.setEmotions(new ArrayList<Emotion>());

        String query = type.indexOf("lxh_") == -1 ? "unlike" : "like";
        query = "like";
        Cursor cursor = emotionsDb.rawQuery(" SELECT * FROM " + EmotionTable.table + " WHERE " + EmotionTable.file + " " + query + " '" + type
                + "%' order by " + EmotionTable.file, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    byte[] data = cursor.getBlob(cursor.getColumnIndex(EmotionTable.value));
                    String key = cursor.getString(cursor.getColumnIndex(EmotionTable.key));

                    Emotion emotion = new Emotion();
                    emotion.setData(data);
                    emotion.setKey(key);

                    emotions.getEmotions().add(emotion);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return emotions;
    }

    static class EmotionTable {

        static final String table = "emotions";

        static final String id = "emotions_id";

        static final String key = "emotions_key";

        static final String file = "emotions_file";

        static final String value = "emotions_value";

    }
}
