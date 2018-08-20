package com.groundwork.dashboard.portlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.portlet.iframe.GWOSIFramePortlet;

import javax.portlet.PortletConfig;
import java.io.FileReader;
import java.util.Properties;

public class PerformanceViewPortlet extends GWOSIFramePortlet {

    private static Log logger = LogFactory.getLog(PerformanceViewPortlet.class);

    private String backend;

    @Override
    protected String getIFrameSource(PortletConfig config) {
        // only called during init phase of portlet lifecycle
        return  config.getInitParameter(PARAM_URL) ;
    }

    @Override
    protected boolean getIsAttachUID(PortletConfig config) {
        // only called during init phase of portlet lifecycle
        String param = config.getInitParameter(PARAM_UID) ;
        return Boolean.parseBoolean(param);
    }

}
