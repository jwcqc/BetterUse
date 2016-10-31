package me.hyman.betteruse.support.bean;

import android.graphics.drawable.Drawable;

public class AccountBean {

    private Long id;
    private String userId;
    private String userName;
    private String description;
    private String accessToken;
    private String refreshToken;
    private String isDefault;  // 1代表默认，0不是
    private Long expires_in;
    private Drawable userIcon;
    private String profileImageUrl;


    public static final String TB_NAME = "Account";

    public static final String ID = "_id";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String DESCRIPTION = "description";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String IS_DEFAULT = "isDefault";
    public static final String EXPIRES = "expires_in";
    public static final String USER_ICON = "userIcon";
    public static final String PROFILE_IMAGE_URL = "profileImageUrl";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

    public Drawable getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(Drawable userIcon) {
        this.userIcon = userIcon;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public String toString() {
        return "AccountBean{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", isDefault='" + isDefault + '\'' +
                ", expires_in=" + expires_in +
                ", userIcon=" + userIcon +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
