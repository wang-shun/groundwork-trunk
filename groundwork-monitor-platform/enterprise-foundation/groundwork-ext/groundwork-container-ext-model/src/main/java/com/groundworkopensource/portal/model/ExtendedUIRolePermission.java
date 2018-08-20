package com.groundworkopensource.portal.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Created by ArulShanmugam on 12/19/14.
 */
@XmlType(propOrder = { "resource", "action"} )
public class ExtendedUIRolePermission implements Serializable {

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    private String resource = null;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String action = null;
}
