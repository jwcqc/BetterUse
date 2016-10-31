package me.hyman.betteruse.support.util;

public class AppConst {

	public static final String APP_KEY = "4199759241";
	
	public static final String APP_SECRET = "0b8759e51f4c1d8c8f5c3d4355114710";
	
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
	
	public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
	
	public static final String MD5 = "c756f5460ac7745bd562c5ea19457889";

	/**
	 * 用于默认账号
	 */
	public static final String IS_DEFAULT = "1";
	public static final String IS_NOT_DEFAULT = "0";

	/**
	 * 当前登录的账号
	 */
	public static final String LOGIN_USER = "loginUser";
	
	public static final String USER = "user";



	/**
	 * 对应setting_pic_quality_value
	 *
	 * thumbnail  bmiddle  large
	 */
	public static final int PIC_MODE_SMALL = 0; // 普通
	public static final int PIC_MODE_MIDDLE	 = 1;  // 中等
	public static final int PIC_MODE_LARGE = 2;  // 高清


	/**
	 * 对应setting_text_size_value
	 */
	public static final int TEXT_SIZE_SMALL = 0;  		// 小
	public static final int TEXT_SIZE_MIDDLE	 = 1;   // 标准
	public static final int TEXT_SIZE_LARGE = 2;        // 大


	/**
	 * 标签
	 */
	public static final String FlagFragment = "Flag";

	/**
	 * 默认刷新条数
	 */
	public static final int DEFAULT_REFRESH_COUNT = 20;
}
