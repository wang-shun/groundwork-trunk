/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.handler;

import java.io.Serializable;

import javax.portlet.PortletSession;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.bean.PerfMeasurementIPCBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.IPCHandlerConstants;
import com.icesoft.faces.async.render.SessionRenderer;

/**
 * 
 * This Class works behind the scene, provides all IPC related operations in
 * Status Viewer.
 * 
 * Don't Use this class directly! Use Wrapper methods in StateController class
 * instead.
 * 
 * @author nitin_jadhav
 * 
 */
public class IPCHandler implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8390598884106119836L;
    /**
     * Render group name to be used in IPC.
     */
    private String renderGroupName;

    /**
     * The method for applying filters to other portlets on same sub page.
     * 
     * @param hostFilter
     * @param serviceFilter
     * @param sessionAttribute
     */
    public void applyFilter(final String hostFilter,
            final String serviceFilter, String sessionAttribute) {

        String hostFilterSessionAttribute = IPCHandlerConstants.HOST_FILTER
                + Constant.UNSERSCORE + sessionAttribute;
        String serviceFilterSessionAttribute = IPCHandlerConstants.SERVICE_FILTER
                + Constant.UNSERSCORE + sessionAttribute;
        // add filters to session and re-render the session
        addSessionAttribute(hostFilterSessionAttribute, hostFilter, false);
        addSessionAttribute(serviceFilterSessionAttribute, serviceFilter, true);
    }

    /**
     * The method for applying time filter to other perf measurement portlet.
     * 
     * @param perfMeasurementIPCBean
     */
    public void applyPerfTimeFilter(
            PerfMeasurementIPCBean perfMeasurementIPCBean) {

        // add filters to session and re-render the session
        addSessionAttribute(IPCHandlerConstants.PERF_TIME_FILTER,
                perfMeasurementIPCBean, true);

    }

    /**
     * Returns current selected time filter in availability portlet .
     * 
     * @return Host Filter
     */
    public PerfMeasurementIPCBean getPerfTimeFilter() {

        PerfMeasurementIPCBean perfMeasurementIPCBean = (PerfMeasurementIPCBean) getSessionAttribute(IPCHandlerConstants.PERF_TIME_FILTER);

        return perfMeasurementIPCBean;
    }

    /**
     * Returns current sub page specific Host Filter.
     * 
     * @param sessionAttribute
     * 
     * @return Host Filter
     */
    public String getHostFilter(String sessionAttribute) {

        String hostsessionAttribute = IPCHandlerConstants.HOST_FILTER
                + Constant.UNSERSCORE + sessionAttribute;
        String filter = (String) getSessionAttribute(hostsessionAttribute);

        // TODO remove following code for optimization

        if (filter != null) {
            return filter;
        }
        return CommonConstants.DEFAULT_FILTER;
    }

    /**
     * Returns current sub page specific Service Filter.
     * 
     * @param sessionAttribute
     * 
     * @return Service Filter
     */
    public String getServiceFilter(String sessionAttribute) {
        String servicesessionAttribute = IPCHandlerConstants.SERVICE_FILTER
                + Constant.UNSERSCORE + sessionAttribute;
        String filter = (String) getSessionAttribute(servicesessionAttribute);

        // TODO remove following code for optimization

        if (filter != null) {
            return filter;
        }
        return CommonConstants.DEFAULT_FILTER;
    }

    /**
     * Basic IPC method for sending messages (variables) to other portlets.
     * 
     * @param attributeName
     * @param attributeValue
     */
    public void addSessionAttribute(final String attributeName,
            final Object attributeValue) {
        addSessionAttribute(attributeName, attributeValue, true);
    }

    /**
     * Basic IPC method for sending messages (variables) to other portlets.
     * 
     * @param attributeName
     * @param attributeValue
     */
    private void addSessionAttribute(final String attributeName,
            final Object attributeValue, boolean rerenderSession) {

        if (renderGroupName == null) {
            renderGroupName = FacesUtils.getPortletSession(false).getId();
            SessionRenderer.addCurrentSession(renderGroupName);
        }

        /*
         * Note: we need to add attributes to SESSION scope. Here we are adding
         * to APPLICATION_SCOPE. But as for PortletSession, SESSION scope is not
         * available and this logic is working with different user sessions; its
         * fine.
         */
        FacesUtils.getPortletSession(false).setAttribute(attributeName,
                attributeValue, PortletSession.APPLICATION_SCOPE);

        // render the session as per the flag
        if (rerenderSession) {
            rerenderSession();
        }
    }

    /**
     * Returns attribute stored in Session.
     * 
     * @param attributeName
     * @return Attribute stored in Session
     */
    public Object getSessionAttribute(final String attributeName) {
        return FacesUtils.getPortletSession(false).getAttribute(attributeName,
                PortletSession.APPLICATION_SCOPE);
    }

    /**
     * Basic IPC method for deleting variables from session.
     * 
     * @param attributeName
     * @param attributeScope
     */
    public void deleteSessionAttribute(final String attributeName,
            int attributeScope) {
        FacesUtils.getPortletSession(false).removeAttribute(attributeName,
                attributeScope);
    }

    /**
     * re-renders session.
     */
    private void rerenderSession() {
        SessionRenderer.render(renderGroupName);
    }

}
