/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.PortletRequest;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.reports.common.FacesUtils;
import com.groundworkopensource.portal.reports.common.ReportConstants;
import com.icesoft.faces.context.effects.JavascriptContext;

import com.groundworkopensource.portal.model.ExtendedUIRole;
import java.util.List;

/**
 * The back-end bean for the Reports viewing functionality.
 * 
 * @author nitin_jadhav
 */

public class ReportViewerBean {

    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Method to handle the option click in report tree.
     * 
     * @param event
     */

    private String secureAccess;

    private String userName;

    /**
     * Do currently logged in user user
     */
    private boolean userHasHGAccess;

    /**
     * variable to enable Popup for access denied warning
     */
    private boolean accessPopupVisible;
    
    /**
     * PARTIAL_RESTRICTION
     */
    private static final String PARTIAL_RESTRICTION = "P";

    /**
     * NO_RESTRICTION
     */
    private static final String NO_RESTRICTION = "N";

    
    /**
     * COMMA
     */
    private static final String COMMA = ",";

    /**
     * Constructor
     */
    public ReportViewerBean() {
    	logger.setLevel(Level.SEVERE);
        // get the secure.access.enabled property
        secureAccess = PropertyUtils.getProperty(ApplicationType.REPORT_VIEWER,
                ReportConstants.SECURE_ACCESS_ENABLED);

        userName = getPortletRequest().getRemoteUser();
        try {
        	
            // check if user have permissions to access at least one host group
            userHasHGAccess = isUserHasHGAccess();
        } catch (IOException e) {
            // Exception occurred while checking out roles. Do not grant user
            // any HostGroup access.
            logger
                    .severe("Error occurred while checking out permissions for current user. User will not have permission on any hostgroup.");
        }
    }
    
    /**
     * Do user have access to atleast one host group?
     * 
     * @throws IOException
     * @throws HibernateException
     */
    private boolean isUserHasHGAccess()
            throws IOException {
    	List<ExtendedUIRole> extnRoleList = FacesUtils.getExtendedRoleAttributes();
        if (extnRoleList != null) {
            for (ExtendedUIRole extnRole : extnRoleList) {

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of host groups.

                if ((extnRole.getRestrictionType()).equals(NO_RESTRICTION)) {
                    // unrestricted access
                    return true;
                } else if ((extnRole.getRestrictionType()).equals(PARTIAL_RESTRICTION)) {
                    // Partial access.
                    if (extnRole.getHgList() != null
                            && (extnRole.getHgList()).split(COMMA).length > 0) {
                        return true;
                    }
                }
            }
        }
        // no host group found in any of list => user do not have access to any
        // of host group
        return false;
    }

    /**
     * show report on UI
     * 
     * @param event
     */
    public void showReport(final ActionEvent event) {

        // TODO: find a better method of building URL with parameters (may be
        // tokens)

        if (!userHasHGAccess) {
            setAccessPopupVisible(true);
            return;
        }

        /*
         * URL that will be supplied to the iFrame when option in the tree is
         * clicked.
         */

        String reportURL;

        String reportId = FacesUtils
                .getRequestParameter(ReportConstants.REPORT_ID);

        if (reportId != null && !reportId.equalsIgnoreCase("null")) {

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            PortletRequest request = (PortletRequest) externalContext
                    .getRequest();
            String host;
            /* the host contains hostname:port */
            if ("true".equalsIgnoreCase(secureAccess)) {
                host = ReportConstants.HTTPS + request.getServerName();

            } else {
                host = ReportConstants.HTTP + request.getServerName()
                        + ReportConstants.COLAN + request.getServerPort();
            }

            /* building the actual report URL */

            reportURL = host + ReportConstants.REPORT_URL_PART1 + reportId
                    + ReportConstants.REPORT_URL_PART2 + "&user=" + userName;

            logger.info("Report URL: " + reportURL);

            /*
             * The iFrame in the report viewer page will be refreshed with above
             * URL with a simple javascript function call on that page
             */

            String jsCall = ReportConstants.JSCALL_STRING1 + reportURL
                    + ReportConstants.JSCALL_STRING2 + "resetReportFrame();";

            JavascriptContext.addJavascriptCall(FacesContext
                    .getCurrentInstance(), jsCall);
        }

    }

    /**
     * Returns PortletRequest
     * 
     * @return PortletRequest
     */
    private static PortletRequest getPortletRequest() {
        PortletRequest request = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (null != facesContext && null != facesContext.getExternalContext()) {
            ExternalContext externalContext = facesContext.getExternalContext();
            request = (PortletRequest) externalContext.getRequest();
        }
        return request;
    }

    /**
     * Sets the accessPopupVisible.
     * 
     * @param accessPopupVisible
     *            the accessPopupVisible to set
     */
    public void setAccessPopupVisible(boolean accessPopupVisible) {
        this.accessPopupVisible = accessPopupVisible;
    }

    /**
     * Returns the accessPopupVisible.
     * 
     * @return the accessPopupVisible
     */
    public boolean isAccessPopupVisible() {
        return accessPopupVisible;
    }

    /**
     * Method called when close button on UI is called.
     */
    public void closePopup() {
        setAccessPopupVisible(false);
    }

}
