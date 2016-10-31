package me.hyman.betteruse.support.bean;

public class CacheBean {

    public static final String TB_NAME = "TimelineCache";

    public static final String ID = "_id";
    public static final String USER_ID = "userId";
    public static final String DESCRIPTION = "description";
    public static final String RESULT = "result";
    public static final String FLAG = "flag";
    public static final String CREATEDAT = "createdAt";


    private Long id;
    private String userId;
    private String description;
    private String result;
    private String flag;
    private String createdAt;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CacheBean{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", description='" + description + '\'' +
                ", result='" + result + '\'' +
                ", flag='" + flag + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
