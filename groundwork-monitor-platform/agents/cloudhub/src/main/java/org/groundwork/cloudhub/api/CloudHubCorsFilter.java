package org.groundwork.cloudhub.api;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dtaylor on 6/5/17.
 */
public class CloudHubCorsFilter  implements Filter {

    private String origins = null;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        if (this.origins != null) {
            response.setHeader("Access-Control-Allow-Origin", this.origins);
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with, X-Auth-Token, Content-Type,GWOS-API-TOKEN,ContentType");
        }
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
        String originsParam = filterConfig.getInitParameter("Access-Control-Allow-Origin");
        if (originsParam != null) {
            this.origins = originsParam;
        }
    }

    public void destroy() {}
}

