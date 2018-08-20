package org.exoplatform.organization.webui.component;

import java.io.*;
import java.util.Properties;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import javax.portlet.PortletSession;
import javax.ws.rs.core.Response;

public class RESTInfo {
	static private RESTInfo _instance = null;
	static public String portal_rest_url = null;
	/** The logger. */
	private static final Logger logger = Logger.getLogger(RESTInfo.class);

	protected RESTInfo() {
		try (InputStream file = new FileInputStream(new File(
				"/usr/local/groundwork/config/status-viewer.properties"))) {
			Properties props = new Properties();
			props.load(file);
			portal_rest_url = props
					.getProperty("portal.extension.resteasy.service.url");
		} catch (Exception e) {
			logger.error("error" + e);
		}
	}

	static public RESTInfo instance() {
		if (_instance == null) {
			_instance = new RESTInfo();
		}
		return _instance;
	}
}