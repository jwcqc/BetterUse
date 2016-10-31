package me.hyman.betteruse.support.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		//第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类
		//  设置为null,代表使用系统默认的工厂类
		super(context, DBInfo.DB.DB_NAME, null, DBInfo.DB.VERSION);
	}
	
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	//用于初次使用软件时生成数据库表
	//当调用SQLiteOpenHelper的getWritableDatabase()或者getReadableDatabase()方法获取用于操作数据库的SQLiteDatabase实例的时候，
	//如果数据库不存在，Android系统会自动生成一个数据库，接着调用onCreate()方法，
	//onCreate()方法在初次生成数据库时才会被调用，
	//在onCreate()方法里可以生成数据库表结构及添加一些应用使用到的初始化数据
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DBInfo.Table.TB_ACCOUNT_CREATE);
		db.execSQL(DBInfo.Table.TB_TIMELINE_CREATE);
	}

	
	//onUpgrade()方法在数据库的版本发生变化时会被调用，一般在软件升级时才需改变版本号
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("DROP TABLE IF EXISTS Customer");
		// onCreate(db);
		//上面两句在数据库版本每次发生变化时都会把用户手机上的数据库表删除，然后再重新创建。
		//一般在实际项目中是不能这样做的，正确的做法是在更新数据库表结构时，还要考虑用户存放于数据库中的数据不会丢失
	}

}
