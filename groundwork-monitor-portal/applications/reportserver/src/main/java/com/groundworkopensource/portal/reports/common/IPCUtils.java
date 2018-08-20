/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.common;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletSession;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.exception.GWPortalException;

/**
 * IPC related Utilities
 * 
 * @author nitin_jadhav
 * 
 */
public class IPCUtils {

    /**
     * MESSAGE_GOT_PORTLET_SESSION_NULL
     */
    private static final String MESSAGE_GOT_PORTLET_SESSION_NULL = "Got PortletSession null";
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(IPCUtils.class.getName());

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected IPCUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * returns application attribute
     * 
     * @param name
     * @return application attribute
     * @throws GWPortalException
     */
    public static Object getApplicationAttribute(final String name)
            throws GWPortalException {
        return getPortletSession().getAttribute(name,
                PortletSession.APPLICATION_SCOPE);
    }

    /**
     * Set application attribute in session
     * 
     * @param name
     * @param val
     * @throws GWPortalException
     */
    public static void setApplicationAttribute(String name, Object val)
            throws GWPortalException {
        getPortletSession().setAttribute(name, val,
                PortletSession.APPLICATION_SCOPE);
    }

    /**
     * @return SessionID
     * @throws GWPortalException
     */
    public static String getSessionID() throws GWPortalException {
        return getPortletSession().getId();
    }

    /**
     * @return PortletSession
     * @throws GWPortalException
     */
    public static PortletSession getPortletSession() throws GWPortalException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        Object sessObj = ec.getSession(false);
        if (sessObj != null && sessObj instanceof PortletSession) {
            return (PortletSession) sessObj;
        }
        logger.warn(MESSAGE_GOT_PORTLET_SESSION_NULL);
        throw new GWPortalException(MESSAGE_GOT_PORTLET_SESSION_NULL);
    }

}
