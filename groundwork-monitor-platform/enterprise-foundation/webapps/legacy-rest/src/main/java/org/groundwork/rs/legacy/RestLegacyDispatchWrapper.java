package org.groundwork.rs.legacy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.Map;

public class RestLegacyDispatchWrapper extends HttpServletRequestWrapper {

    public RestLegacyDispatchWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded";
    }

    @Override
    public Enumeration getParameterNames() {
        return super.getParameterNames();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Map getParameterMap() {
        return super.getParameterMap();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getParameter(String name) {
        return super.getParameter(name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);    //To change body of overridden methods use File | Settings | File Templates.
    }

}
