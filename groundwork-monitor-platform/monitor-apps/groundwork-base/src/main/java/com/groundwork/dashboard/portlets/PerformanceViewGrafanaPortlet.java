package com.groundwork.dashboard.portlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.portlet.iframe.GWOSIFramePortlet;

import javax.portlet.PortletConfig;
import java.io.FileReader;
import java.util.Properties;

public class PerformanceViewGrafanaPortlet extends GWOSIFramePortlet {

    private static Log logger = LogFactory.getLog(PerformanceViewGrafanaPortlet.class);

    private static final String PARAM_URL_GRAFANA = PARAM_URL+"2";
    private static final String PARAM_UID_GRAFANA = PARAM_UID+"2";

    @Override
    protected String getIFrameSource(PortletConfig config) {
        // only called during init phase of portlet lifecycle
        // return getBackend().equalsIgnoreCase(PARAM_PERFORMANCE_DEFAULT) ? config.getInitParameter(PARAM_URL) : config.getInitParameter(PARAM_URL_GRAFANA);
        return config.getInitParameter(PARAM_URL_GRAFANA) ;
    }

    @Override
    protected boolean getIsAttachUID(PortletConfig config) {
        // only called during init phase of portlet lifecycle
        //String param = getBackend().equalsIgnoreCase(PARAM_PERFORMANCE_DEFAULT) ?  config.getInitParameter(PARAM_UID) : config.getInitParameter(PARAM_UID_GRAFANA);
        String param = config.getInitParameter(PARAM_UID_GRAFANA);
        return Boolean.parseBoolean(param);
    }

}
