package com.groundworkopensource.portal.identity.extendedui;

/**
 * Created by ArulShanmugam on 12/12/14.
 */
public class HibernateExtendedRolePermission implements java.io.Serializable{



    public HibernateExtendedRole getRole() {
        return role;
    }

    public void setRole(HibernateExtendedRole role) {
        this.role = role;
    }

    private HibernateExtendedRole role;

    public HibernateResource getResource() {
        return resource;
    }

    public void setResource(HibernateResource resource) {
        this.resource = resource;
    }

    public HibernatePermission getPermission() {
        return permission;
    }

    public void setPermission(HibernatePermission permission) {
        this.permission = permission;
    }

    private HibernateResource resource;

    private HibernatePermission permission;
}
