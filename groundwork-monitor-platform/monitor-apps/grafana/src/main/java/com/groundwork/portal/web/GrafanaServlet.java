/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.groundwork.portal.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.client.CollageRestException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


public final class GrafanaServlet extends HttpServlet {

    private static Log logger = LogFactory.getLog(GrafanaServlet.class);

    private static final String GRAFANA_APP_NAME = "grafana";
    private static final String GW_APP_NAME = "monitor-dashboard";

    private static final String PARAM_GW_USERID = "gwuid";

    private static final String SESSION_GW_USERID = "gwuid";
    private static final String SESSION_FOUNDATION_TOKEN = "FoundationToken";
    private static final String SESSION_FOUNDATION_API = "FoundationApi";

    private static final String COOKIE_GW_USERID = "gwuid";
    private static final String COOKIE_FOUNDATION_TOKEN = "FoundationToken";
    private static final String COOKIE_FOUNDATION_REST_SERVICE = "FoundationRestService";

    private static final String MSG_GROUNDWORK_USER_MISSING = "<b>Groundwork user not specified</b>";
    private static final String MSG_FAILED_AUTHENTICATE = "<b>Failed to authenticate. Cannot connect to Foundation Server</b>";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {
        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // using common GroundWork application name temporarily to
        // prevent cookie/session authentication token overlap: GWMON-12262
        if (authenticate(request, response, GW_APP_NAME)) {
            request.setAttribute("src", getInitParameter("grafana-src"));
            request.getRequestDispatcher(getInitParameter("path")).forward(request, response);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    protected boolean authenticate(HttpServletRequest request, HttpServletResponse response, String appName) throws IOException {
        // check session for groundwork user id
        HttpSession session = request.getSession(true);
        String gwuid = (String)session.getAttribute(SESSION_GW_USERID);
        if (gwuid == null) {
            gwuid = request.getParameter(PARAM_GW_USERID);
            if (gwuid != null) {
                // save groundwork user id in session
                session.setAttribute(SESSION_GW_USERID, gwuid);
                // pass groundwork user id to client
                Cookie tokenCookie = new Cookie(COOKIE_GW_USERID, gwuid);
                tokenCookie.setPath("/");
                response.addCookie(tokenCookie);
            } else {
                logger.error("Groundwork user not specified");
                response.getWriter().println(MSG_GROUNDWORK_USER_MISSING);
                return false;
            }
        }
        // check session for authenticated token
        String token = (String)session.getAttribute(SESSION_FOUNDATION_TOKEN);
        if (token == null) {
            // lookup credentials from ws_client.properties
            String foundationUser = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_READER_USERNAME);
            String foundationPassword = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_READER_PASSWORD);

            String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
            try {
                AuthClient client = new AuthClient(foundationRestService);
                // check for existing authenticated token
                Cookie[] cookies = request.getCookies();
                for (Cookie cookie : cookies) {
                    if (COOKIE_FOUNDATION_TOKEN.equals(cookie.getName())) {
                        token = cookie.getValue();
                        if (Boolean.parseBoolean(client.isTokenValid(appName, token))) {
                            // save authenticated token in session
                            session.setAttribute(SESSION_FOUNDATION_TOKEN, token);
                            return true;
                        }
                        break;
                    }
                }
                // save REST service url in session
                session.setAttribute(SESSION_FOUNDATION_API, foundationRestService);
                // pass REST service url to client
                Cookie restCookie = new Cookie(COOKIE_FOUNDATION_REST_SERVICE, foundationRestService);
                restCookie.setPath("/");
                response.addCookie(restCookie);
                // authenticate with service
                AuthClient.Response result = client.login(foundationUser, foundationPassword, appName);
                if (result.success()) {
                    token = result.getToken();
                    // save authenticated token in session
                    session.setAttribute(SESSION_FOUNDATION_TOKEN, token);
                    // pass authenticated token to client
                    Cookie tokenCookie = new Cookie(COOKIE_FOUNDATION_TOKEN, token);
                    tokenCookie.setPath("/");
                    response.addCookie(tokenCookie);
                } else {
                    logger.error("Failed authentication for app: " + appName);
                    response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                    return false;
                }
            } catch (CollageRestException e) {
                logger.error("Exception during authentication: " + e.getMessage(), e);
                response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                return false;
            }
        }
        return true;
    }
}
