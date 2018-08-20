package org.groundwork.rs.legacy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class RestDispatcherServlet  extends HttpServlet {

    protected static Log log = LogFactory.getLog(RestDispatcherServlet.class);

    private static final String FOUNDATION_WEBAPP = "/foundation-webapp";

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext foundationWebapp = getServletContext().getContext(FOUNDATION_WEBAPP);
        if (foundationWebapp == null) {
            log.error("Could not find delegating webapp '" + FOUNDATION_WEBAPP + "' - can not dispatch.");
            return;
        }
        if (foundationWebapp != null) {
            String queryString = (request.getQueryString() == null) ? "" : request.getQueryString();
            String dispatchedPath = request.getPathInfo() + queryString;
            if (log.isDebugEnabled()) {
                log.debug("dispatching GET to " + dispatchedPath);
            }
            RequestDispatcher dispatcher = foundationWebapp.getRequestDispatcher(dispatchedPath);
            dispatcher.include(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext foundationWebapp = getServletContext().getContext(FOUNDATION_WEBAPP);
        if (foundationWebapp == null) {
            log.error("Could not find delegating webapp '" + FOUNDATION_WEBAPP + "' - can not dispatch.");
            return;
        }
        if (foundationWebapp != null) {
            String queryString = (request.getQueryString() == null) ? "" : request.getQueryString();
            String dispatchedPath = request.getPathInfo() + queryString;
            if (log.isDebugEnabled()) {
                log.debug("dispatching POST to " + dispatchedPath);
            }
            RequestDispatcher dispatcher = foundationWebapp.getRequestDispatcher(dispatchedPath);
            dispatcher.forward(request, response);
        }
    }

    private void debug(HttpServletRequest request) {
        log.debug("-------");
        log.debug("Content Length: " + request.getContentLength());
        log.debug("URI: " + request.getRequestURI());
        log.debug("URL: " + request.getRequestURL());
        log.debug("QueryString: " + request.getQueryString());
        /***
         * Don't do this, it will invalidate the input stream for servlets or filters down chain...
         */
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            log.debug("param: " + name + ", " + request.getParameter(name));
        }
        e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            log.debug("header: " + name + ", " + request.getHeader(name));
        }
    }

}
