package com.groundwork.agents.appservers.collector.servlet;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.beans.ConnectorBean;
import com.groundwork.agents.appservers.collector.impl.GWOSWebsphereCollectorService;
import com.groundwork.agents.appservers.collector.impl.WASAdminClient;

/**
 * Servlet implementation class GWOSWebsphereServlet
 */
public class GWOSWebsphereServlet extends GWOSCollectorServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSWebsphereServlet.class);

	/**
	 * Default constructor.
	 */
	public GWOSWebsphereServlet() {

	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		String[] staticProps = { "connector_security_enabled", "username",
				"password", "ssl_trustStore", "ssl_keyStore",
				"ssl_trustStorePassword", "ssl_keyStorePassword", "hostname",
				"port", "nagios_hostname", "nagios_port", "nagios_encryption",
				"nagios_password", "exec_interval",};

		String[] defaultCheckList = { };
		gwosService = new GWOSWebsphereCollectorService();
		super.init(config, staticProps, defaultCheckList,
				CollectorConstants.APP_SERVER_WEBSPHERE,
				WASAdminClient.WEBSPHERE_PROPERTIES, gwosService);
	}
	
	public Properties createPropFileFromParams(HttpServletRequest request) {
		
		/*for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements() ;) {
	        String param = params.nextElement(); 
			log.info(param);
	     }*/

		Properties prop = new Properties();			
		ConnectorBean bean = xferReqValuesToBean(request);	
		String hostname = bean.getHostName();
		String port = bean.getPort();		
		String ssl_trustStore = bean.getSslTruststorePath();
		String ssl_keyStore = bean.getSslKeystorePath();
		String ssl_trustStorePassword = bean.getSslTruststorePassword();
		String ssl_keyStorePassword = bean.getSslKeystorePassword();
		String username = bean.getUserName();
		String password = bean.getPassword();
		prop.put("hostname", hostname);
		prop.put("port", port);
		prop.put("connector_security_enabled", "true");
		prop.put("ssl_trustStore", ssl_trustStore);
		prop.put("ssl_keyStore", ssl_keyStore);
		prop.put("ssl_trustStorePassword", ssl_trustStorePassword);
		prop.put("ssl_keyStorePassword", ssl_keyStorePassword);
		prop.put("username", username);
		prop.put("password", password);		
		this.createGWProperties(bean, prop);
		return prop;
	}
	
	/**
	 * Appends the GW properties
	 * @param bean
	 * @param gwProps
	 */
	private void createGWProperties(ConnectorBean bean, Properties gwProps) {
		if (gwProps!= null) {
			String nagiosHostName = bean.getNagiosHostname();
			String nagiosPort = bean.getNagiosPort();
			String nagiosPassword = bean.getNagiosPassword();
			String nagiosEncryption = bean.getNagiosEncryption();
			String passiveCheckInterval = bean.getPassiveCheckInterval();
			gwProps.put("nagios_hostname", nagiosHostName);
			gwProps.put("nagios_port", nagiosPort);
			gwProps.put("nagios_password", nagiosPassword);
			gwProps.put("nagios_encryption", nagiosEncryption);
			gwProps.put("exec_interval", passiveCheckInterval);		
			
		}
		
	}
	
	/**
	 * Helper for transfering request values to bean
	 * @param request
	 * @return
	 */
	private ConnectorBean xferReqValuesToBean(HttpServletRequest request)
	{
		Object connectorObj = request.getSession().getAttribute("connectorBean");
		ConnectorBean bean = null;
		if (connectorObj != null) {
			bean = (ConnectorBean) connectorObj;
			bean.setHostName((String) request.getParameter("hostName"));
			bean.setPort((String) request.getParameter("port"));
			bean.setUserName((String) request.getParameter("userName"));
			bean.setPassword((String) request.getParameter("password"));
			bean.setSslTruststorePath((String) request.getParameter("sslTruststorePath"));
			bean.setSslKeystorePath((String) request.getParameter("sslKeystorePath"));
			bean.setSslTruststorePassword((String) request.getParameter("sslTruststorePassword"));
			bean.setSslKeystorePassword((String) request.getParameter("sslKeystorePassword"));
			bean.setNagiosHostname((String) request.getParameter("nagiosHostname"));
			bean.setNagiosPort((String) request.getParameter("nagiosPort"));
			bean.setNagiosPassword((String) request.getParameter("nagiosPassword"));
			bean.setNagiosEncryption((String) request.getParameter("nagiosEncryption"));
			bean.setPassiveCheckInterval((String) request.getParameter("passiveCheckInterval"));
		} // end if
		return bean;
	}

}
