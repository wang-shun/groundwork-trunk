package com.groundwork.agents.appservers.collector.servlet;

import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.beans.ConnectorBean;
import com.groundwork.agents.appservers.collector.impl.GWOSJBossCollectorService;
import com.groundwork.agents.appservers.collector.impl.JBossAdminClient;

/**
 * Servlet implementation class GWOSJbossServlet
 */
public class GWOSJBossServlet extends GWOSCollectorServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSJBossServlet.class);

	/**
	 * Default constructor.
	 */
	public GWOSJBossServlet() {

	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		String[] staticProps = { "java.naming.provider.url",
				"java.naming.factory.initial", "java.naming.factory.url.pkgs",
				"nagios_hostname", "nagios_port", "nagios_encryption",
				"nagios_password", "exec_interval", "object.name.filter" };
		String[] defaultCheckList = { };
		gwosService = new GWOSJBossCollectorService();
		super.init(config, staticProps, defaultCheckList,
				CollectorConstants.APP_SERVER_JBOSS,
				JBossAdminClient.JBOSS_PROPERTIES, gwosService);		
	}
	
	public Properties createPropFileFromParams(HttpServletRequest request) {
		Properties prop = new Properties();
		ConnectorBean bean = xferReqValuesToBean(request);	
		if (bean != null) {
		String javaNamingProviderURL = bean.getJavaNamingProviderURL();
		String javaNamingFactoryInitial = bean.getJavaNamingFactoryInitial();
		String javaNamingFactoryURLPackages = bean.getJavaNamingFactoryURLPackages();
		prop.put("java.naming.provider.url", javaNamingProviderURL);
		prop.put("java.naming.factory.initial", javaNamingFactoryInitial);
		prop.put("java.naming.factory.url.pkgs", javaNamingFactoryURLPackages);
		createGWProperties(bean, prop);
		} // end if
		return prop;
	}
	/**
	 * Appends the GW properties
	 * 
	 * @param bean
	 * @param gwProps
	 */
	private void createGWProperties(ConnectorBean bean, Properties gwProps) {
		if (gwProps != null) {
			String nagiosHostName = bean.getNagiosHostname();
			String nagiosPort = bean.getNagiosPort();
			String nagiosPassword = bean.getNagiosPassword();
			String nagiosEncryption = bean.getNagiosEncryption();
			String passiveCheckInterval = bean.getPassiveCheckInterval();
			String objectNameFilter = bean.getObjectNameFilter();
			String instanceId = bean.getInstanceId();
			gwProps.put("nagios_hostname", nagiosHostName);
			gwProps.put("nagios_port", nagiosPort);
			gwProps.put("nagios_password", nagiosPassword);
			gwProps.put("nagios_encryption", nagiosEncryption);
			gwProps.put("exec_interval", passiveCheckInterval);
			gwProps.put("object.name.filter", objectNameFilter);
			gwProps.put("instanceId", instanceId);
		}

	}

	/**
	 * Helper for transfering request values to bean
	 * 
	 * @param request
	 * @return
	 */
	private ConnectorBean xferReqValuesToBean(HttpServletRequest request) {
		Object connectorObj = request.getSession()
				.getAttribute("connectorBean");
		ConnectorBean bean = null;
		if (connectorObj != null) {
			bean = (ConnectorBean) connectorObj;
			bean.setJavaNamingProviderURL((String) request.getParameter("java.naming.provider.url"));
			bean.setJavaNamingFactoryInitial((String) request.getParameter("java.naming.factory.initial"));
			bean.setJavaNamingFactoryURLPackages((String) request.getParameter("java.naming.factory.url.pkgs"));
			bean.setNagiosHostname((String) request
					.getParameter("nagiosHostname"));
			bean.setNagiosPort((String) request.getParameter("nagiosPort"));
			bean.setNagiosPassword((String) request
					.getParameter("nagiosPassword"));
			bean.setNagiosEncryption((String) request
					.getParameter("nagiosEncryption"));
			bean.setPassiveCheckInterval((String) request
					.getParameter("passiveCheckInterval"));
			bean.setObjectNameFilter((String) request
					.getParameter("object.name.filter"));
			bean.setInstanceId((String) request.getParameter("instanceId"));
		} // end if
		return bean;
	}


}
