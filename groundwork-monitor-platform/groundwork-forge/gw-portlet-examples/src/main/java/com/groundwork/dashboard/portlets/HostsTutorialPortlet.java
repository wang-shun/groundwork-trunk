/*
 * Copyright (C) 2010 GroundWork Open Source, Inc. (GroundWork) All rights
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
package com.groundwork.dashboard.portlets;

import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoService;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ClientDataRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.List;

public class HostsTutorialPortlet extends DashboardPortlet {

    public static final String TUTORIAL_APP_NAME = "monitor-dashboard";

    protected String getAppName() {
        return TUTORIAL_APP_NAME;
    }

    protected final static String VIEW_HOSTS = "/WEB-INF/view/hosts-tutorial.jsp";
    protected final static String VIEW_SERVICES = "/WEB-INF/view/services-tutorial.jsp";

    protected final static String ROWS_PREFERENCE = "rows";

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            String hostName = (String)PortletMessaging.consume(request, "hostName");
            String view = VIEW_HOSTS;
            if (hostName == null) {
                request.setAttribute("hosts", retrieveHosts(request));
            }
            else {
                request.setAttribute("services", PortletMessaging.consume(request, "services"));
                request.setAttribute("hostName", hostName);
                view = VIEW_SERVICES;
            }

            // Retrieve a preference example
            String rows = request.getPreferences().getValue(ROWS_PREFERENCE, "20");
            request.setAttribute(ROWS_PREFERENCE, rows);

            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(view);
            dispatcher.include(request, response);
        }
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String hostName = request.getParameter("hostName");
        if (hostName != null) {
            PortletMessaging.publish(request, "services", retrieveServices(request, hostName));
            PortletMessaging.publish(request, "hostName", hostName);
        }
    }

    protected List<DtoHost> retrieveHosts(PortletRequest request) {
        HostClient hostClient = new HostClient(getRestEndPoint(request));
        return hostClient.list();
    }

    protected List<DtoService> retrieveServices(ClientDataRequest request, String hostName) {
        ServiceClient serviceClient = new ServiceClient(getRestEndPoint(request));
        return serviceClient.list(hostName);
    }

    protected void provideHeaders(RenderRequest request, RenderResponse response) {
    }

}
