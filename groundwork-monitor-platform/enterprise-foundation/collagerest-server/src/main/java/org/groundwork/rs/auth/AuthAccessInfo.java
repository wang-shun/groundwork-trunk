package org.groundwork.rs.auth;

public class AuthAccessInfo {

    private String appName;
    private String token;
    private Boolean isReadonly;

    public AuthAccessInfo(String appName, String token, Boolean isReadonly) {
        this.appName = appName;
        this.token = token;
        this.isReadonly = isReadonly;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getReadonly() {
        return isReadonly;
    }

    public void setReadonly(Boolean reader) {
        isReadonly = reader;
    }
}
