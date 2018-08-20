package org.groundwork.cloudhub.web;

import org.josso.servlet.agent.GenericServletSSOAgentFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CloudhubJossoFilter extends GenericServletSSOAgentFilter implements Filter {

    private String statusPath;
    private final String DEFAULT_STATUS_PATH = "/cloudhub/api/status";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        statusPath = filterConfig.getInitParameter("STATUS_PATH");
        if (statusPath == null) {
            statusPath = DEFAULT_STATUS_PATH;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) servletRequest;
        final String uri = hreq.getRequestURI();
        if (uri != null && uri.startsWith(statusPath)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else{
            super.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    @Override
    public void destroy() {
    }

}
