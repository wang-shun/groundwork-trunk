package com.gwos.statusservice.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.tc55.agent.CatalinaSSOAgent;


public class LoginHelper {

	private static Log log = LogFactory.getLog(LoginHelper.class);
	/**
	 * Helper to validate the portal authentication
	 * 
	 * @param username
	 * @param pass
	 * @return
	 */
	public static boolean login(String username, String password) {
		/*boolean status = false;
		String sessionId = null;
		try {
			String REQUESTOR = "status-restservice";
			String SEC_DOMAIN = "josso";
			Lookup lookup = Lookup.getInstance();
			lookup.init("josso-agent-config.xml");
			CatalinaSSOAgent _agent = (CatalinaSSOAgent) lookup
					.lookupSSOAgent();
			String assertionId = _agent.getGatewayServiceLocator()
					.getSSOIdentityProvider()
					.assertIdentityWithSimpleAuthentication(REQUESTOR,
							SEC_DOMAIN, username, password);
			sessionId = _agent.getGatewayServiceLocator()
					.getSSOIdentityProvider().resolveAuthenticationAssertion(
							REQUESTOR, assertionId);
			if (sessionId != null)
				status = true;			
		} catch (Exception exc) {
			log.error(exc.getMessage());
			status = false;
		}
		return status;*/
		return true; //TODO UNCOMMENT THIS WHEN DOING JOSS INTEGRATION FOR 7.0
	}

}
