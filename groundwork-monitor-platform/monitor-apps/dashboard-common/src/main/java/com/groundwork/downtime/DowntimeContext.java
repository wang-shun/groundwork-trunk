package com.groundwork.downtime;

import javax.ws.rs.core.Cookie;
import java.util.List;

public class DowntimeContext {

    private String groundworkServer;
    private List<Cookie> credentials;
    private boolean loggedOn;
    private String username;
    private String password;

    public DowntimeContext() {}

    public DowntimeContext(String groundworkServer, List<Cookie> credentials) {
        this.groundworkServer = groundworkServer;
        this.credentials = credentials;
    }

    public DowntimeContext(String groundworkServer, List<Cookie> credentials, boolean loggedOn) {
        this.groundworkServer = groundworkServer;
        this.credentials = credentials;
        this.loggedOn = loggedOn;
    }

    public DowntimeContext(String groundworkServer, List<Cookie> credentials, boolean loggedOn, String username, String password) {
        this.groundworkServer = groundworkServer;
        this.credentials = credentials;
        this.loggedOn = loggedOn;
        this.username = username;
        this.password = password;
    }

    public String getGroundworkServer() {
        return groundworkServer;
    }

    public void setGroundworkServer(String groundworkServer) {
        this.groundworkServer = groundworkServer;
    }

    public List<Cookie> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Cookie> credentials) {
        this.credentials = credentials;
    }

    public boolean isLoggedOn() {
        return loggedOn;
    }

    public void setLoggedOn(boolean loggedOn) {
        this.loggedOn = loggedOn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
