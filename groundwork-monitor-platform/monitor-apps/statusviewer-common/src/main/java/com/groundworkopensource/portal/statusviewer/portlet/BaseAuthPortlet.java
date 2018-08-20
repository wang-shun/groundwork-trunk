package com.groundworkopensource.portal.statusviewer.portlet;

import com.groundworkopensource.portal.common.BasePortlet;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.client.CollageRestException;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public abstract class BaseAuthPortlet extends BasePortlet {

    private static final Logger logger = Logger.getLogger(BaseAuthPortlet.class);

    /**
     * Groundwork Portlet headers contributed flag.
     */
    private final static String GW_PORTLET_FLAG = "gw.portlet.flag";

    /**
     * Foundation API token portlet session attribute name.
     */
    protected final static String SESSION_FOUNDATION_TOKEN = "FoundationToken";

    /**
     * Foundation API rest service base url portlet session attribute name.
     */
    protected final static String SESSION_FOUNDATION_API = "FoundationApi";

    /**
     * Foundation API token cookie name.
     */
    protected final static String COOKIE_FOUNDATION_TOKEN = "FoundationToken";

    /**
     * Foundation API rest service base url cookie name.
     */
    protected final static String COOKIE_FOUNDATION_REST_SERVICE = "FoundationRestService";

    /**
     * Failed authentication message.
     */
    protected final static String MSG_FAILED_AUTHENTICATE = "<b>Failed to authenticate. Cannot connect to Foundation Server</b>";


    public static final String GW_APP_NAME = "monitor-dashboard";

    /**
     * Check Groundwork Portlet headers contributed flag.
     *
     * @param renderRequest render request
     * @return already contributed headers
     */
    protected boolean alreadyContributedHeaders(RenderRequest renderRequest) {
        HttpServletRequest request = getServletRequest(renderRequest);
        if (request == null)
            return false;
        Boolean contributed = (Boolean)request.getAttribute(GW_PORTLET_FLAG);
        if ((contributed == null) || (contributed == false)) {
            request.setAttribute(GW_PORTLET_FLAG, Boolean.TRUE);
            return false;
        }
        return true;
    }

    /**
     * Override protocol used to add head scripts and links. Derived classes
     * should override this method and invoke addJavaScript() and addStyleList()
     * methods as needed.
     *
     * @param request render request
     * @param response render response
     */
    protected abstract void provideHeaders(RenderRequest request, RenderResponse response);

    /**
     * Add javascript head script tag. This method should be called from provideHeaders().
     *
     * @param response render response
     * @param scriptPath script src path
     * @param scriptId script id or null
     */
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

    /**
     * Add style head link tag. This method should be called from provideHeaders().
     *
     * @param response render response
     * @param cssPath link href path
     * @param cssId link id or null
     */
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

    /**
     * Add response headers and construct head tags.
     *
     * @param request
     * @param response
     */
    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
        super.doHeaders(request, response);
        if (!alreadyContributedHeaders(request)) {
            provideHeaders(request, response);
        }
    }

    /**
     * Setup portlet session attributes and cookies to support client
     * side authentication. Writes to response on error.
     *
     * @param request render request
     * @param response render response
     * @param appName application name
     * @return authenticated
     */
    protected boolean authenticate(RenderRequest request, RenderResponse response, String appName) throws PortletException, IOException {

        String token = (String)request.getPortletSession(true).getAttribute(SESSION_FOUNDATION_TOKEN);
        if (token == null) {

            // Lookup credentials from ws_client.properties
            String foundationUser;
            String foundationPassword; 
            // @since 7.2.0 - see http://jira/browse/GWMON-12646
            if (request.isUserInRole("GWAdmin") || request.isUserInRole("GWRoot")) {
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
                    Boolean isTokenValid = Boolean.parseBoolean(client.isTokenValid(appName, token));
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
                AuthClient.Response result = client.login(foundationUser, foundationPassword, appName);
                if (result.success()) {
                    token = result.getToken();
                    Cookie tokenCookie = new Cookie(COOKIE_FOUNDATION_TOKEN, token);
                    tokenCookie.setPath("/");
                    response.addProperty(tokenCookie);
                    request.getPortletSession().setAttribute(SESSION_FOUNDATION_TOKEN, token);
                    return true;
                }
                logger.error("Failed authentication in Dashboard for app: " + appName);
                response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                return false;
            }
            catch (CollageRestException e) {
                logger.error("Exception during authentication in Dashboard: " + e.getMessage(), e);
                response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                return false;
            }
        }
        return true;
    }


}
