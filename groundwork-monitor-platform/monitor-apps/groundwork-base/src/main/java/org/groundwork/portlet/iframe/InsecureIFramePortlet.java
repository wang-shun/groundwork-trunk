package org.groundwork.portlet.iframe;

import javax.portlet.PortletException;

public class InsecureIFramePortlet extends GWOSIFramePortlet {

    protected String validateURL(String userURL, String serverName)
            throws PortletException {
        return userURL;
    }
}
