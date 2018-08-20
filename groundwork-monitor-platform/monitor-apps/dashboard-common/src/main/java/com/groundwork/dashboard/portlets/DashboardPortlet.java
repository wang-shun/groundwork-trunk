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
import com.groundwork.dashboard.portlets.dto.DtoError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.client.CollageRestException;
import org.w3c.dom.Element;

import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;

public abstract class DashboardPortlet extends GenericPortlet {

    protected static Log log = LogFactory.getLog(DashboardPortlet.class);

    public static final String REQUEST_CONTEXT_ATTRIBUTE = "org.apache.jetspeed.request.RequestContext";

    protected static final String GW_DASHBOARD_CSS_ID = "gw_dashboard_css";

    protected final static String SESSION_FOUNDATION_TOKEN = "FoundationToken";
    protected final static String SESSION_FOUNDATION_API = "FoundationApi";

    protected final static String PREFS_VIEW = "View";
    protected final static String PREFS_EDIT = "Edit";
    protected final static String PREFS_REFRESH_SECONDS = "refreshSeconds";
    protected final static String PREFS_ROWS = "rows";

    protected final static String MSG_FAILED_AUTHENTICATE = "<b>Failed to authenticate. Cannot connect to Foundation Server</b>";

    protected static final String COOKIE_FOUNDATION_TOKEN = "FoundationToken";
    protected static final String COOKIE_FOUNDATION_REST_SERVICE = "FoundationRestService";

    // Override These Methods
    protected abstract String getAppName();
    protected abstract void provideHeaders(RenderRequest request, RenderResponse response);

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        response.setContentType("text/html");
        if (authenticate(request, response)) {
            String url = request.getPreferences().getValue(PREFS_VIEW, "/app/views/monitor.html" );
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    protected boolean authenticate(PortletRequest request, PortletResponse response) throws PortletException, IOException {
        return authenticate(request, response, null);
    }

    protected boolean authenticate(PortletRequest request, PortletResponse response, String additionalRole) throws PortletException, IOException {

        String token = (String)request.getPortletSession(true).getAttribute(SESSION_FOUNDATION_TOKEN);
        if (token == null) {

            // Lookup credentials from ws_client.properties
            String foundationUser;
            String foundationPassword;
            // @since 7.2.0 - see http://jira/browse/GWMON-12646
            if (request.isUserInRole("GWAdmin") || request.isUserInRole("GWRoot") ||
                    additionalRole != null && request.isUserInRole(additionalRole)) {
                foundationUser = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
                foundationPassword = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
            }
            else {
                foundationUser = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_READER_USERNAME);
                foundationPassword = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_READER_PASSWORD);
            }                                                       
            String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);

            request.getPortletSession(true).setAttribute(SESSION_FOUNDATION_API, foundationRestService);

            Cookie[] cookies = request.getCookies();
            for(Cookie cookie : cookies){
                if(COOKIE_FOUNDATION_TOKEN.equals(cookie.getName())){
                    token = cookie.getValue();
                    AuthClient client = new AuthClient(foundationRestService);
                    Boolean isTokenValid = Boolean.parseBoolean(client.isTokenValid(this.getAppName(), token));
                    if (isTokenValid) {
                        request.getPortletSession().setAttribute(SESSION_FOUNDATION_TOKEN, token);
                        return true;
                    }
                }
            }

            Cookie restCookie = new Cookie(COOKIE_FOUNDATION_REST_SERVICE, foundationRestService);
            restCookie.setPath("/");
            response.addProperty(restCookie);

            try {
                AuthClient client = new AuthClient(foundationRestService);
                AuthClient.Response result = client.login(foundationUser, foundationPassword, getAppName());
                if (result.success()) {
                    token = result.getToken();
                    Cookie tokenCookie = new Cookie(COOKIE_FOUNDATION_TOKEN, token);
                    tokenCookie.setPath("/");
                    response.addProperty(tokenCookie);
                    request.getPortletSession().setAttribute(SESSION_FOUNDATION_TOKEN, token);
                    return true;
                }
                log.error("Failed authentication in Dashboard for app: " + getAppName());
                if (response instanceof RenderResponse) {
                    RenderResponse renderResponse = (RenderResponse)response;
                    renderResponse.getWriter().println(MSG_FAILED_AUTHENTICATE);
                }
                return false;
            }
            catch (CollageRestException e) {
                log.error("Exception during authentication in Dashboard: " + e.getMessage(), e);
                if (response instanceof RenderResponse) {
                    RenderResponse renderResponse = (RenderResponse) response;
                    renderResponse.getWriter().println(MSG_FAILED_AUTHENTICATE);
                }
                return false;
            }
        }
        return true;
    }

    protected void addJavaScript(RenderResponse response, String scriptPath, String scriptId) {
        Element headElem = response.createElement("script");
        headElem.setAttribute("language", "javascript");
        if (scriptId != null) {
            headElem.setAttribute("id", scriptId);
        }
        headElem.setAttribute("src", scriptPath);
        headElem.setAttribute("type", "text/javascript");
        response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
    }

    protected void addStyleLink(RenderResponse response, String cssPath, String cssId) {
        Element headElem = response.createElement("link");
        headElem.setAttribute("rel", "stylesheet");
        if (cssId != null) {
            headElem.setAttribute("id", cssId);
        }
        headElem.setAttribute("href", cssPath);
        headElem.setAttribute("type", "text/css");
        response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
    }

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
        super.doHeaders(request, response);

        if (alreadyContributedHeaders(request))
            return;

        provideHeaders(request, response);
    }

    private static final int BLOCK_SIZE = 4096;

    public static void drain(Reader r, Writer w) throws IOException {
        char[] bytes = new char[BLOCK_SIZE];
        try {
            int length = r.read(bytes);
            while (length != -1) {
                if (length != 0) {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally {
            bytes = null;
        }

    }

    public String createError(int httpCode, String message) throws IOException {
        DtoError error = new DtoError(httpCode, message);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, error);
        return writer.toString();
    }

    protected static HttpServletRequest getServletRequest(PortletRequest request) {
        try {
            return (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
        }
        catch (Exception e) {
            Object context = request.getAttribute(REQUEST_CONTEXT_ATTRIBUTE);
            if (context != null) {
                try {
                    Method getRequest = context.getClass().getMethod("getRequest");
                    if (getRequest != null) {
                        return (HttpServletRequest)getRequest.invoke(context);
                    }
                }
                catch (Exception e2) {
                    log.error("Failed to retrieve portal servlet request: " + e2.getMessage(), e2);
                    return null;
                }
            }
            log.error("Failed to retrieve portal servlet request: " + e.getMessage(), e);
        }
        return null;
    }

    protected final static String GW_DASHBOARD_PORTLET_FLAG = "gw.dashboard.portlet.flag";

    protected boolean alreadyContributedHeaders(RenderRequest renderRequest) {
        HttpServletRequest request = getServletRequest(renderRequest);
        if (request == null)
            return false;
        Boolean contributed = (Boolean)request.getAttribute(GW_DASHBOARD_PORTLET_FLAG);
        if (contributed == null || contributed == false) {
            request.setAttribute(GW_DASHBOARD_PORTLET_FLAG, Boolean.TRUE);
            return false;
        }
        return true;
    }

    protected String getRestEndPoint(PortletRequest request) {
        return WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
    }

    protected Boolean isJBossContainer(PortletRequest request) {
        return request.getAttribute(REQUEST_CONTEXT_ATTRIBUTE) == null;
    }
}



