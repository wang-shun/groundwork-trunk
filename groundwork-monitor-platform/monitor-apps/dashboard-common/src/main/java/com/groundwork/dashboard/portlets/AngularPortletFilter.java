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

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AngularPortletFilter implements RenderFilter {

    public void doFilter(RenderRequest request, RenderResponse response,
                         FilterChain filterChain) throws IOException, PortletException {
        filterChain.doFilter(request, response);
        includeAngular(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws PortletException {
    }

    protected void includeAngular(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        String useAngular = request.getPreferences().getValue("jetapp", null);
        if (useAngular != null && useAngular.equalsIgnoreCase("true")) {
            if (!alreadyContributedAngular(request)) {
                response.getWriter().println("<script>\n    angular.element(document).ready(function() {\n" +
                        "        angular.bootstrap(document, ['myApp']);\n" +
                        "    });\n</script>\n");
            }
        }
    }

    protected final static String GW_DASHBOARD_ANGULAR_FLAG = "gw.dashboard.angular.flag";

    protected boolean alreadyContributedAngular(RenderRequest renderRequest) {
        HttpServletRequest request = DashboardPortlet.getServletRequest(renderRequest);
        if (request == null)
            return false;
        Boolean contributed = (Boolean)request.getAttribute(GW_DASHBOARD_ANGULAR_FLAG);
        if (contributed == null || contributed == false) {
            request.setAttribute(GW_DASHBOARD_ANGULAR_FLAG, Boolean.TRUE);
            return false;
        }
        return true;
    }


}
