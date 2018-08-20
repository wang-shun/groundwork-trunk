package com.groundworkopensource.portal.model;

import java.io.Serializable;

/**
 * Created by ArulShanmugam on 12/23/14.
 */
public class ExtendedUIResource implements Serializable{

    public Byte getResourceId() {
        return resourceId;
    }

    public void setResourceId(Byte resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    private Byte resourceId = null;

    private String resourceName = null;

}
