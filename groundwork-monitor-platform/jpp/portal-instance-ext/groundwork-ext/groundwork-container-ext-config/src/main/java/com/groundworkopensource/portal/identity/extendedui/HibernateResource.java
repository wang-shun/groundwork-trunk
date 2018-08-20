package com.groundworkopensource.portal.identity.extendedui;

/**
 * Created by ArulShanmugam on 12/18/14.
 */
public class HibernateResource implements java.io.Serializable{

    public Byte getResourceId() {
        return resourceId;
    }

    public void setResourceId(Byte resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Byte resourceId;

    private String name;
}
