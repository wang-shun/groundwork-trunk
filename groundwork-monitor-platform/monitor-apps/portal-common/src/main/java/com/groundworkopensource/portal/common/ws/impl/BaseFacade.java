package com.groundworkopensource.portal.common.ws.impl;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.groundwork.rs.common.GWRestConstants;

import javax.portlet.PortletSession;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by ArulShanmugam on 4/2/14.
 */
public class BaseFacade {

    protected static final String PORTAL_REST_ENDPOINT = WebServiceLocator.getInstance()
            .portalExtnRESTeasyURL();

}
