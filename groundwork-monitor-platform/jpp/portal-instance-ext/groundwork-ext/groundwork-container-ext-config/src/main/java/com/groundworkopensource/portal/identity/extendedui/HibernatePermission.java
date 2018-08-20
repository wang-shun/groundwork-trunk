package com.groundworkopensource.portal.identity.extendedui;

/**
 * Created by ArulShanmugam on 12/15/14.
 */
public class HibernatePermission implements java.io.Serializable {

    public Byte getPermId() {
        return permId;
    }

    public void setPermId(Byte permId) {
        this.permId = permId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private Byte permId;

    private String action;
}
