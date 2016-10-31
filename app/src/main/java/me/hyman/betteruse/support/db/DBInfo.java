package me.hyman.betteruse.support.db;

import me.hyman.betteruse.support.bean.CacheBean;

public class DBInfo {

	public static class DB {

		// 数据库名称
		public static final String DB_NAME = "BetterUse";

		// 数据库版本
		public static final int VERSION = 1;

	}

	public static class Table {

		// 账户信息
		public static final String TB_ACCOUNT = "Account";
		public static final String TB_ACCOUNT_DROP = "DROP TABLE " + TB_ACCOUNT;
		public static final String TB_ACCOUNT_CREATE = "CREATE TABLE IF NOT EXISTS  " + TB_ACCOUNT
				+ "( _id INTEGER PRIMARY KEY, " +
				"	userId TEXT, " +
				"	userName TEXT, " +
				"	description TEXT, " +
				"	accessToken TEXT, " +
				"	refreshToken TEXT, " +
				"	isDefault TEXT, " +
				"	expires_in REAL," +
				"	userIcon BLOB, " +
				"	profileImageUrl TEXT)";

		// 缓存的微博时间线
		public static final String TB_TIMELINE = CacheBean.TB_NAME;
		public static final String TB_TIMELINE_DROP = "DROP TABLE " + TB_TIMELINE;
		public static final String TB_TIMELINE_CREATE = "CREATE TABLE IF NOT EXISTS  "
				+ TB_TIMELINE + "( "
				+ CacheBean.ID  +  " INTEGER PRIMARY KEY, "
				+ CacheBean.USER_ID +  " TEXT, "    //属于哪个用户
				+ CacheBean.DESCRIPTION +  " TEXT, "
				+ CacheBean.RESULT + " TEXT, "     // 保存返回的数据字段
				+ CacheBean.FLAG + " TEXT, "       // 属于哪个fragment(flag标识)
				+ CacheBean.CREATEDAT + " TEXT"
				+  ")";

	}
}
