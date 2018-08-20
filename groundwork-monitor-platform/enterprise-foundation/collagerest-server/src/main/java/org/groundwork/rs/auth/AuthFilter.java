/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.common.ConfiguredObjectMapper;
import org.groundwork.rs.common.GWRestConstants;
import org.groundwork.rs.dto.DtoError;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * AuthFilter - filter to check authorization access tokens.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuthFilter implements Filter {

    private static final Log log = LogFactory.getLog(AuthFilter.class);

    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APP_NAME_HEADER = "APP-NAME";

    /** URI contains rules for requests that should not be authenticated. */
    private static final List<String> BYPASS_AUTH_URIS = new ArrayList<String>();
    static {
        BYPASS_AUTH_URIS.add("api/auth/");
        BYPASS_AUTH_URIS.add("restwebservices");
        BYPASS_AUTH_URIS.add("legacy-rest/api");
        BYPASS_AUTH_URIS.add("/api/autoRegister");
        BYPASS_AUTH_URIS.add("/api/eventGeneration");
        BYPASS_AUTH_URIS.add("/api/noma");
        BYPASS_AUTH_URIS.add("/api/performanceData");
        BYPASS_AUTH_URIS.add("/api/pluginUpdates");
        BYPASS_AUTH_URIS.add("/api/uploadProfiles");
        BYPASS_AUTH_URIS.add("/api/vemaProfile");
    }

    private static final List<String> READONLY_POST_URIS =  new ArrayList<>();
    static {
        READONLY_POST_URIS.add("/api/biz/getindowntime");
        READONLY_POST_URIS.add("/api/biz/getauthorizedservices");
        READONLY_POST_URIS.add("/api/events/stateTransitions");
    }

    private static ObjectMapper OBJECT_MAPPER;
    private static JAXBContext JAXB_CONTEXT;
    static {
        try {
            OBJECT_MAPPER = new ConfiguredObjectMapper();
            JAXB_CONTEXT = JAXBContext.newInstance(DtoError.class);
        } catch (Exception e) {
            log.error("Unable to configure DtoError serialization: "+e, e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Check request access token/application name for requests that must be
     * authenticated. Returns HTTP UNAUTHORIZED if not authenticated. HTTP BAD
     * REQUEST is returned if access token not included in request headers.
     *
     * @param servletRequest HTTP servlet request
     * @param servletResponse HTTP servlet response
     * @param filterChain filter chain
     * @throws IOException if thrown by subsequent filters or request handling
     * @throws ServletException if thrown by subsequent filters or request handling
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // HTTP filter
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final HttpServletRequestWrapper req = new HttpServletRequestWrapper((HttpServletRequest) servletRequest);
        final HttpServletResponseWrapper res = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);
        final String origin = req.getHeader("Origin");

        // Process PreFlight CORS validation
        if (req.getMethod().equals("OPTIONS")) {
            addCorsHeaders(req, res, origin);
            return;
        }
        // bypass authentication for matching URIs
        for (String byPassToken : BYPASS_AUTH_URIS) {
            if (req.getRequestURI().contains(byPassToken)) {
                addCorsHeaders(req, res, origin);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        // get application name and authentication token for request
        String appName = req.getHeader(GWRestConstants.HEADER_GWOS_APP_NAME);
        if (appName == null || appName.equalsIgnoreCase("")) {
            appName = req.getHeader(APP_NAME_HEADER);
        }
        String token = req.getHeader(GWRestConstants.HEADER_GWOS_API_TOKEN);

        if (log.isDebugEnabled()) {
            log.debug("appName = " + appName + ", token = " + token);
        }

        if ((token == null) || (appName == null)) {
            log.error("No token or appName provided. Rejecting request");
            setStatus(req, res, Response.Status.BAD_REQUEST);
            return;
        }

        // validate authentication token/application name
        if (!AuthService.getInstance().checkAccessToken(token, appName)) {
            // This doesn't require ERROR level logging as this will happen frequently during a piecemeal restart of
            // the system.  It is up to the client to handle this scenario and either retry or log it as an error on
            // their end.
            log.warn("Access token not valid from app " + appName + ". Rejecting request");
            setStatus(req, res, Response.Status.UNAUTHORIZED);
            return;
        }

        if (!authorizeAccess(req, token, appName)) {
            log.warn("Access token not  from app " + appName + ". Rejecting request");
            setStatus(req, res, Response.Status.UNAUTHORIZED);
            return;
        }

        // add/preserve CORS headers and continue request, (intercept calls
        // to reset() on error that clear headers)
        addCorsHeaders(req, res, origin);
        ServletResponse preserveCorsHeadersServletResponse =
                new HttpServletResponseWrapper((HttpServletResponse) servletResponse) {
                    @Override
                    public void reset() {
                        // reset response, clearing CORS headers
                        try {
                            super.reset();
                            // reinstate CORS headers for error return
                            addCorsHeaders(req, this, origin);
                        }
                        catch (Exception e) {
                            log.error("Error resetting response: " + e.getMessage() + ", " + e.getCause(), e);
                        }
                    }
                };
        filterChain.doFilter(servletRequest, preserveCorsHeadersServletResponse);
    }

    @Override
    public void destroy() {
    }

    //  GWMON-11729 - Support W3 Cross Site Origin Resource Sharing Specification
    protected void addCorsHeaders(HttpServletRequest request, HttpServletResponse response, String origin) {
        if (origin == null || origin.isEmpty()) {
            return;
        }
        String validOrigin =  getValidOrigin(origin);
        if (validOrigin.equals("")) {
            setStatus(request, response, Response.Status.UNAUTHORIZED); // unauthorized
        }
        else {
            response.addHeader("Access-Control-Allow-Origin", validOrigin);
            response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,Access,GWOS-API-TOKEN,GWOS-APP-NAME");
            response.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    protected String getValidOrigin(String requestingOrigin) {
        return requestingOrigin;
    }

    protected String getValidatedOrigin(String requestingOrigin) {
        String origins = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ORIGINS);
        if (origins == null || origins.trim().isEmpty()) {
            return "";
        }
        String[] validOrigins = origins.trim().split("\\s*,\\s*");
        for (int ix = 0; ix < validOrigins.length; ix++) {
            if (requestingOrigin != null && requestingOrigin.equals(validOrigins[ix])) {
                return requestingOrigin;
            }
        }
        return "";
    }

    /**
     * Set HTTP status and XML/JSON error content.
     *
     * @param req HTTP request
     * @param res HTTP response
     * @param status HTTP status
     */
    protected void setStatus(HttpServletRequest req, HttpServletResponse res, Response.Status status) {
        // return status
        res.setStatus(status.getStatusCode());
        // if options method return status only
        if (req.getMethod().equals("OPTIONS")) {
            return;
        }
        // check if request accepts XML or JSON
        boolean defaultAcceptXML = true;
        boolean acceptXML = false;
        boolean acceptJSON = false;
        for (Enumeration<String> acceptHeaders = req.getHeaders(ACCEPT_HEADER); acceptHeaders.hasMoreElements();) {
            defaultAcceptXML = false;
            String acceptHeader = acceptHeaders.nextElement();
            if (acceptHeader.equalsIgnoreCase(MediaType.APPLICATION_XML)) {
                acceptXML = true;
                break;
            } else if (acceptHeader.equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
                acceptJSON = true;
                break;
            }
        }
        // return default error response if not accepting XML or JSON
        if (!acceptXML && !acceptJSON) {
            return;
        }
        // build and output error object response
        DtoError dtoError = new DtoError(status.getReasonPhrase(), status.getStatusCode());
        try {
            OutputStream out = res.getOutputStream();
            if (acceptXML) {
                // JAXB XML serialization
                res.setHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_XML);
                JAXB_CONTEXT.createMarshaller().marshal(dtoError, out);
            } else if (acceptJSON) {
                // Jackson JSON serialization
                res.setHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON);
                OBJECT_MAPPER.writeValue(out, dtoError);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("Unable to output error: "+dtoError+", "+e, e);
        }
    }

    /**
     * Basic Authorization. Restrict access by provisioned access keys by HTTP Method (with some exceptions)
     * Reader access can only access GET Method with exceptions
     * Admin access has full access to APIs
     *
     * http://jira/browse/GWMON-12646
     *
     * Exception cases:
     * POST /api/auth/*
     * POST /api/biz/getindowntime
     * POST /api/biz/getauthorizedservices
     *
     * @since 7.2.0
     * @return true if access is authorized
     */
    protected boolean authorizeAccess(HttpServletRequest request, String token, String appName) {
        String method = request.getMethod();
        if (method.equalsIgnoreCase(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
            return true;
        }
        if (AuthService.getInstance().isAdmin(token, appName)) {
            return true;
        }
        String uri = request.getRequestURI();
        for (String byPassToken : READONLY_POST_URIS) {
            if (uri.contains(byPassToken)) {
                return true;
            }
        }
        // this is a read only user
        // POST, PUT, DELETE not allowed
        return false;
    }
}
