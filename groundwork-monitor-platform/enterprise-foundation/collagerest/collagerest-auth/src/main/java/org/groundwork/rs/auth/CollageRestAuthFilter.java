package org.groundwork.rs.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.common.GWRestConstants;

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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollageRestAuthFilter implements Filter {
    protected static Log log = LogFactory.getLog(CollageRestAuthFilter.class);

    private static final String APP_NAME = "APP-NAME";

    protected static final String DEPLOYMENT_URL = WSClientConfiguration.getProperty("foundation_rest_url");

    // If the incoming request URI contains any of these string tokens, then it token based security is bypassed
    private static final String[] byPassTokens = {"api/auth/", "restwebservices", "legacy-rest/api", "/api/autoRegister", "/api/eventGeneration", "/api/noma", "/api/performanceData", "/api/pluginUpdates", "/api/uploadProfiles", "/api/vemaProfile"};
    private List<String> byPassTokensList = Arrays.asList(byPassTokens);

    private ExpiringMap<String, String> tokenCache = null;
    private int maxSessions = 500;

    private static final int EXPIRATION_INTERVAL_SECONDS = 120; // 2 minutes

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            AuthorizationConfiguration configuration = new AuthorizationConfiguration();
            int maxIntervalMinutes = configuration.getMaxInactiveIntervalMinutes();
            maxSessions = configuration.getMaxSessions();
            log.info("initializing CollageRestAuthFilter with max inactive interval of " + maxIntervalMinutes + " minutes");
            log.info("initializing CollageRestAuthFilter with max sessions of " + maxSessions);
            tokenCache = new ExpiringMap<String, String>(maxIntervalMinutes * 60, EXPIRATION_INTERVAL_SECONDS);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            log.warn("Request isn't an instance of HttpServletRequest, skipping...");
            return;
        }

        HttpServletRequestWrapper req =
                new HttpServletRequestWrapper((HttpServletRequest) servletRequest);

        HttpServletResponseWrapper res =
                new HttpServletResponseWrapper((HttpServletResponse) servletResponse);
        for (String byPassToken : byPassTokensList) {
            if (req.getRequestURI().contains(byPassToken)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        String appName = req.getHeader(GWRestConstants.HEADER_GWOS_APP_NAME);
        // Support for APP-NAME headers until transition period.
        if (appName == null || appName.equalsIgnoreCase(""))
            appName = req.getHeader(APP_NAME);

        String token = req.getHeader(GWRestConstants.HEADER_GWOS_API_TOKEN);

        // If token is not null try validating the token. Throw error if bad token
        if (token != null && appName != null) {
            if (tokenCache.get(token) != null) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Token Miss in Cache: " + token);
                }
                AuthClient authClient = new AuthClient(DEPLOYMENT_URL);
                Boolean isTokenValid = Boolean.parseBoolean(authClient.isTokenValid(appName, token));
                if (isTokenValid) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token lookup is valid: " + token);
                    }
                    tokenCache.put(token, appName);
                    if (tokenCache.size() > maxSessions) {
                        log.error("Exceeded max sessions for authorization token cache. Number of sessions: " + tokenCache.size()
                                + ", appName: " + appName + ", remote: " + req.getRemoteAddr() + ", resource: " + req.getPathInfo());
                        logTokenCounts();
                    }
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Token lookup invalid: " + token);
                    }
                    res.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                }
            }
        } else {
            // If token is null or app_name is null, then throw bad request exception
            res.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        }
    } // end if

    /**
     * Logs token counts by appName
     */
    private void logTokenCounts() {
        HashMap<String, Integer> countMap = new HashMap<String, Integer>();
        int counter = 1;
        for (Map.Entry<String, String> entry : tokenCache.entrySet()) {
            if (countMap.containsKey(entry.getValue()))
                counter= countMap.get(entry.getValue()) +1;
            else
                counter = 1;
            countMap.put(entry.getValue(), counter);
        }
        log.error("Number of sessions by appName:" + countMap.toString());
    }

    @Override
    public void destroy() {
        if (tokenCache != null && tokenCache.getExpirer() != null) { // redeploy
            if (tokenCache.getExpirer().isRunning()) {
                tokenCache.getExpirer().stopExpiring();
            }
        }
    }

}
