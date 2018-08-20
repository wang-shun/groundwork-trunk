/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * This class provide the action id depending on application
 * 
 * @author manish_kjain
 * 
 */
public class EventMenuActionManager {

    /**
     * EventMenuActionManager instance variable
     */
    private static EventMenuActionManager actionManager;
    /**
     * action array contain actions
     */
    private static Action[] actions = null;
    /**
     * action map variable contain action ID as key and action name as value
     */
    private static Map<String, Action[]> actionsMap = new HashMap<String, Action[]>();
    /**
     * logger
     */
    private static Logger logger = Logger
            .getLogger(EventMenuActionManager.class.getName());

    /**
     * get EventMenuActionManager object
     * 
     * @return EventMenuActionManager
     */
    public static EventMenuActionManager getInstance() {
        if (actionManager == null) {
            actionManager = new EventMenuActionManager();
        }
        return actionManager;
    }

    /**
     * @param appType
     * @return Action[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public static Action[] getActionsByApplicationType(String appType)
            throws WSDataUnavailableException, GWPortalException {
        if (!isEmpty(appType)) {
            actions = actionsMap.get(appType);
            if (actions == null) {
                // logger.debug((new StringBuilder()).append("First call for ")
                // .append(appType).toString());

                IWSFacade webServiceInstance = new WebServiceFactory()
                        .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
                WSFoundationCollection wsfoundationCollection = webServiceInstance
                        .getActionsByApplicationType(appType, true);
                try {
                    actionsMap.put(appType, wsfoundationCollection.getAction());
                    actions = actionsMap.get(appType);
                    if (logger.isDebugEnabled()) {
                        logger.debug((new StringBuilder()).append(
                                "Actions cached successfully for ").append(
                                appType).toString());
                        logger.debug((new StringBuilder()).append("Actions=")
                                .append(actions.length).toString());
                    }
                } catch (Exception e) {
                    logger
                            .error("Exception in getActionsByApplicationType method "
                                    + e);
                }

            }
        } else {
            logger.error("Invalid appType passed to ActionClient");
        }
        return actions;
    }

    /**
     * 
     * @param value
     * @return
     */
    private static boolean isEmpty(String value) {
        return value == null || value.equalsIgnoreCase(Constant.EMPTY_STRING);
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected EventMenuActionManager() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
