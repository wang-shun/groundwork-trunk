package com.groundworkopensource.portal.model;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by ArulShanmugam on 12/19/14.
 */
public class ExtendedUIRolePermissionList implements Serializable{

    @XmlElement(name = "permission")
    public Collection<ExtendedUIRolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(Collection<ExtendedUIRolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }

    private Collection<ExtendedUIRolePermission> rolePermissions = null;
}
