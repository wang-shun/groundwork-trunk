/*
 * Copyright (C) 2017 GroundWork Open Source, Inc. (GroundWork) All rights
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
import com.groundwork.dashboard.configuration.DashboardConfiguration;
import com.groundwork.dashboard.configuration.DashboardConfigurationFactory;
import com.groundwork.dashboard.configuration.DashboardConfigurationService;
import com.groundwork.dashboard.portlets.dto.DashboardConfigLookup;
import com.groundwork.dashboard.portlets.dto.NocBoardResult;
import com.groundwork.dashboard.portlets.dto.SimpleResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class NocBoardConfigPortlet extends GroundworkDashboardPortlet {

    protected static Log log = LogFactory.getLog(NocBoardConfigPortlet.class);

    protected DashboardConfigurationService dashboardService;

    public static final String MAX_AVAILABILITY_WINDOW_SIZE_HOURS = "logmessage.window.size.hours";
    public static final int MAX_AVAILABILITY_WINDOW_SIZE_HOURS_DEFAULT = 48;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        dashboardService = DashboardConfigurationFactory.getConfigurationService(DashboardConfigurationFactory.ServiceType.NOC);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        request.setAttribute("jboss", isJBossContainer(request));
        super.doView(request, response);
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();
        if (!validateResourceRequest(request, response, resourceID)) {
            return;
        }
        if (resourceID.equals("listConfigs")) {
            listConfigs(request, response);
            return;
        }
        if (resourceID.equals("lookupConfig")) {
            lookupConfig(request, response);
            return;
        }
        if (resourceID.equals("saveConfig")) {
            saveConfig(request, response);
            return;
        }
        if (resourceID.equals("removeConfig")) {
            removeConfig(request, response);
            return;
        }
        if (resourceID.equals("configExists")) {
            existsConfig(request, response);
            return;
        }
        NocBoardResult result = new NocBoardResult();
        result.setSuccess(false);
        result.setMessage("Invalid Resource");

    }



    protected void listConfigs(ResourceRequest request, ResourceResponse response) {
        try {
            List<DashboardConfiguration> dashboards = dashboardService.list();
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, dashboards);
            response.getWriter().println(writer);
        } catch (Exception e) {
            logErrorAndSimpleResult(e, response);
        }
    }

    protected void lookupConfig(ResourceRequest request, ResourceResponse response) {
        try {
            String name = request.getParameter("name");
            DashboardConfiguration dashboard;
            if (name == null) {
                dashboard = new DashboardConfiguration();
                DashboardConfiguration.setDefaults(dashboard);
            }
            else {
                dashboard = dashboardService.read(name);
            }
            DashboardConfigLookup configLookup = new DashboardConfigLookup(dashboard);
            configLookup.setMaxAvailabilityWindow(readMaxAvailabilityWindow());
             configLookup.setHostGroups(NocBoardEngine.listHostGroups());
            configLookup.setServiceGroups(NocBoardEngine.listServiceGroups());
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, configLookup);
            response.getWriter().println(writer);
        } catch (Exception e) {
            logErrorAndSimpleResult(e, response);
        }
    }

    protected Integer readMaxAvailabilityWindow() {
        try {
            Integer maxAvailabilityWindow = Integer.parseInt(FoundationConfiguration.getProperty(MAX_AVAILABILITY_WINDOW_SIZE_HOURS));
            if (maxAvailabilityWindow == 0) {
                 return MAX_AVAILABILITY_WINDOW_SIZE_HOURS_DEFAULT;
            }
            return maxAvailabilityWindow - 1;
        }
        catch (Exception e) {
            return MAX_AVAILABILITY_WINDOW_SIZE_HOURS_DEFAULT;
        }
    }

    protected void existsConfig(ResourceRequest request, ResourceResponse response) {
        try {
            Boolean exists = dashboardService.exists(request.getParameter("name"));
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, exists);
            response.getWriter().println(writer);
        } catch (Exception e) {
            logErrorAndSimpleResult(e, response);
        }
    }

    protected void removeConfig(ResourceRequest request, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        try {
            Boolean success = dashboardService.remove(request.getParameter("name"));
            result.setSuccess(success);
            writeSimpleResult(response, result);
        } catch (Exception e) {
            logErrorAndSimpleResult(e, response);
        }
    }

    protected void saveConfig(ResourceRequest request, ResourceResponse response) {
        try {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            DashboardConfiguration configuration = writeMapper.readValue(json, DashboardConfiguration.class);
            dashboardService.save(configuration);
            writeSimpleResult(response, new SimpleResult());
        } catch (Exception e) {
            logErrorAndSimpleResult(e, response);
        }
    }


    protected void logErrorAndSimpleResult(Exception e, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        log.error(e);
        result.setSuccess(false);
        result.setMessage(e.getMessage());
        writeSimpleResult(response, result);
    }

}
