package com.groundworkopensource.portal.statusviewer.bean;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * Bean for storing logged in users role and to check if user in Admin or
 * Operator Role.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class UserRoleBean {
    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(UserRoleBean.class
            .getName());

    /**
     * userInAdminOrOperatorRole
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * UserRoleBean Constructor
     */
    public UserRoleBean() {
        userInAdminOrOperatorRole = PortletUtils.isActionsEnabled();
        LOGGER.debug("In UserRoleBean(). Is Action portlet enabled : "
                + userInAdminOrOperatorRole + " . Portal User Name : "
                + FacesUtils.getLoggedInUser());

    }

    /**
     * Sets the userInAdminOrOperatorRole.
     * 
     * @param userInAdminOrOperatorRole
     *            the userInAdminOrOperatorRole to set
     */
    public void setUserInAdminOrOperatorRole(boolean userInAdminOrOperatorRole) {
        this.userInAdminOrOperatorRole = userInAdminOrOperatorRole;
    }

    /**
     * Returns the userInAdminOrOperatorRole.
     * 
     * @return the userInAdminOrOperatorRole
     */
    public boolean isUserInAdminOrOperatorRole() {
        return userInAdminOrOperatorRole;
    }
}
