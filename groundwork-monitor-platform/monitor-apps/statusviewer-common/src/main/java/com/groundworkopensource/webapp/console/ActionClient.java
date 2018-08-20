/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * ActionClient class populates the actions from the database and caches its for
 * its clients.
 * 
 * @author ashanmugam
 * 
 */
public class ActionClient {

	private static ActionClient client;
	private static Action[] actions = null;
	private static HashMap<String, Action[]> actionsMap = new HashMap<String, Action[]>();

	public static Logger logger = Logger
			.getLogger(ActionClient.class.getName());

	private ActionClient() {

	}

	public static ActionClient getInstance() {
		if (client == null) {
			client = new ActionClient();
		}
		return client;
	}

	/**
	 * Gets actions by application type.Actions are cached at the application
	 * level in order to save i/o access.Any new action configuration requires
	 * restart of gwservices.
	 * 
	 * @param appType
	 * @return
	 */
	public static Action[] getActionsByApplicationType(String appType) {
		logger.debug("Enter method getActionsByApplicationType ");
		if (!CommonUtils.isEmpty(appType)) {
			actions = actionsMap.get(appType);
			if (actions == null) {
				logger.debug("First call for " + appType);
				try {
					WSCommon wsCommon = ServiceLocator.commonLocator()
							.getcommon();
					WSFoundationCollection col = wsCommon
							.getActionsByApplicationType(appType, true);
					actionsMap.put(appType, col.getAction());
					logger.debug("Actions cached successfully for " + appType);
					actions = actionsMap.get(appType);
					logger.debug("Actions=" + actions.length);
				} catch (Exception exc) {
					logger.error(exc.getMessage());
				} // end try/catch
			} // end if
		} else {
			logger.error("Invalid appType passed to ActionClient");
		} // end if
		logger.debug("Exit method getActionsByApplicationType ");
		return actions;
	}

}
