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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.groundwork.dashboard.portlets.dto.EnvironmentMapPrefs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.StringWriter;

public class BlackListPortlet extends GroundworkDashboardPortlet {

    protected static Log log = LogFactory.getLog(BlackListPortlet.class);

    protected static final String PREFS_SORT_ORDER = "sortOrder";

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        super.doView(request, response);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = null;
            String url = request.getPreferences().getValue(PREFS_EDIT, "/app/views/black-list-edit.jsp" );
            dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();
        if (resourceID == null) {
            response.addProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            response.getWriter().println(createError(400, "invalid resource id"));
            return;
        }
        if (resourceID.equals("writePrefs")) {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            EnvironmentMapPrefs update = writeMapper.readValue(json, EnvironmentMapPrefs.class);
            request.getPreferences().setValue(PREFS_REFRESH_SECONDS , Integer.toString(update.getRefreshSeconds()));
            request.getPreferences().setValue(PREFS_SORT_ORDER, update.getSortOrder());
            request.getPreferences().store();
        }
        EnvironmentMapPrefs prefs = new EnvironmentMapPrefs();
        prefs.setRefreshSeconds(Integer.parseInt(request.getPreferences().getValue(PREFS_REFRESH_SECONDS , "60")));
        prefs.setSortOrder(request.getPreferences().getValue(PREFS_SORT_ORDER, "name"));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, prefs);
        response.getWriter().println(writer);
    }



}
