package com.groundwork.agents.appservers.collector.servlet;

import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.beans.ConnectorBean;
import com.groundwork.agents.appservers.collector.impl.GWOSWeblogicCollectorService;
import com.groundwork.agents.appservers.collector.impl.WLSAdminClient;
import com.groundwork.agents.appservers.collector.servlet.GWOSCollectorServlet;

/**
 * Servlet implementation class GWOSWeblogicServlet
 */
public class GWOSWeblogicServlet extends GWOSCollectorServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSWeblogicServlet.class);

	/**
	 * Default constructor.
	 */
	public GWOSWeblogicServlet() {

	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		String[] staticProps = { "username",
				"password", "hostname",
				"port","protocol", "nagios_hostname", "nagios_port", "nagios_encryption",
				"nagios_password", "exec_interval","object.name.filter" };

		String[] defaultCheckList = { };
		gwosService = new GWOSWeblogicCollectorService();
		super.init(config, staticProps, defaultCheckList,
				CollectorConstants.APP_SERVER_WEBLOGIC,
				WLSAdminClient.WEBLOGIC_PROPERTIES, gwosService);
	}
	
	public Properties createPropFileFromParams(HttpServletRequest request) {
		Properties prop = new Properties();
		ConnectorBean bean = xferReqValuesToBean(request);	
		if (bean != null) {
		String hostname = bean.getHostName();
		String port = bean.getPort();
		
		String protocol = bean.getProtocol();
		String username = bean.getUserName();
		String password = bean.getPassword();
		String objectFilter = bean.getObjectFilter();
		prop.put("hostname", hostname);
		prop.put("port", port);
		prop.put("protocol", protocol);
		prop.put("username", username);
		prop.put("password", password);
		prop.put("object.name.filter",objectFilter);
		this.createGWProperties(bean, prop);
		} // end if
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
			bean.setProtocol((String) request.getParameter("protocol"));
			bean.setNagiosHostname((String) request.getParameter("nagiosHostname"));
			bean.setNagiosPort((String) request.getParameter("nagiosPort"));
			bean.setNagiosPassword((String) request.getParameter("nagiosPassword"));
			bean.setNagiosEncryption((String) request.getParameter("nagiosEncryption"));
			bean.setPassiveCheckInterval((String) request.getParameter("passiveCheckInterval"));
			bean.setObjectFilter((String) request.getParameter("object.name.filter"));
		} // end if
		return bean;
	}

}
