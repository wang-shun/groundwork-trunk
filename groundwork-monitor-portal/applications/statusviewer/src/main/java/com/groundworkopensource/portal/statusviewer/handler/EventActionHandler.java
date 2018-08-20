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

package com.groundworkopensource.portal.statusviewer.handler;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.EventBean;
import com.groundworkopensource.portal.statusviewer.bean.EventListBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;

/**
 * This class is responsible to access web service layer and perform actions
 * 
 * @author manish_kjain
 * 
 */
public class EventActionHandler implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6752582065975452791L;
    /**
     * logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(EventActionHandler.class.getName());

    /**
     * Performs action for the given message IDs and ActionID
     * 
     * @param messageIds
     * @param actionID
     * @param appType
     * @return ActionReturn
     */
    public ActionReturn performAction(int[] messageIds, int actionID,
            String appType) {
        ActionReturn actionReturn = null;
        if (appType == null) {
            appType = Constant.SYSTEM;
        }

        try {
            ActionPerform[] actionPerforms = new ActionPerform[Constant.ONE];
            StringProperty[] parameters = new StringProperty[Constant.ELEVEN];
            parameters[Constant.ZERO] = new StringProperty(
                    FilterConstants.ACTION_PARAM_LOG_MESS_IDS,
                    convertToCommaSepString(messageIds));
            // getting logged in user name
            String userName = FacesUtils.getLoggedInUser();
            // String userName = "admin";
            LOGGER.debug((new StringBuilder()).append("Logged in user is")
                    .append(userName).toString());
            parameters[Constant.ONE] = new StringProperty(
                    FilterConstants.ACTION_PARAM_USER_NAME, userName);
            parameters[Constant.TWO] = new StringProperty(
                    FilterConstants.ACTION_PARAM_SEND_NOTIFY, Constant.TRUE);
            parameters[Constant.THREE] = new StringProperty(
                    FilterConstants.ACTION_PARAM_PERSIST_COMMENT, Constant.TRUE);
            parameters[Constant.FOUR] = new StringProperty(
                    FilterConstants.ACTION_PARAM_COMMENT,
                    (new StringBuilder()).append(
                            FilterConstants.ACTION_PARAM_VALUE_COMMENT_PREFIX)
                            .append(
                                    DateUtils.format(Calendar.getInstance()
                                            .getTime(),
                                            Constant.MODEL_POPUP_DATE_FROMAT))
                            .toString());
            parameters[Constant.FIVE] = new StringProperty(
                    FilterConstants.ACTION_PARAM_NSCA_HOST,
                    FilterConstants.DEFAULT_NSCA_HOST);
            parameters[Constant.SIX] = new StringProperty("user", userName);
            parameters[Constant.SEVEN] = new StringProperty(
                    FilterConstants.ACTION_PARAM_NSCA_COMMENT,
                    (new StringBuilder()).append(
                            FilterConstants.SUBMIT_PASSIVE_RESET_COMMENT)
                            .append(userName).toString());
            parameters[Constant.EIGHT] = new StringProperty(
                    FilterConstants.ACTION_PARAM_HOST,
                    generateHostString(messageIds));
            if (appType.equalsIgnoreCase(FilterConstants.APP_TYPE_SNMPTRAP)) {
                parameters[Constant.NINE] = new StringProperty(
                        FilterConstants.ACTION_PARAM_SERVICE,
                        FilterConstants.SERVICE_SNMPTRAP_LAST);
            } else {
                parameters[Constant.NINE] = new StringProperty(
                        FilterConstants.ACTION_PARAM_SERVICE,
                        FilterConstants.SERVICE_SYSLOG_LAST);
            }
            parameters[Constant.TEN] = new StringProperty(
                    FilterConstants.ACTION_PARAM_STATE,
                    FilterConstants.DEFAULT_NSCA_STATE);
            actionPerforms[Constant.ZERO] = new ActionPerform(actionID,
                    parameters);
            IWSFacade webServiceInstance = new WebServiceFactory()
                    .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
            WSFoundationCollection wsfoundationCollection = webServiceInstance
                    .performActions(actionPerforms);
            actionReturn = wsfoundationCollection.getActionReturn()[0];
        } catch (GWPortalException e) {
            LOGGER.error("Exectpion in performAction method:- "
                    + e.getMessage());
        } catch (WSDataUnavailableException e) {
            LOGGER.error("Exectpion in performAction method:- "
                    + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exectpion in performAction method:- "
                    + e.getMessage());
        }

        return actionReturn;
    }

    /**
     * return comma separated message id string
     * 
     * @param messageIds
     * @return String
     */
    private String convertToCommaSepString(int[] messageIds) {
        StringBuffer buf = new StringBuffer();
        String delim = Constant.COMMA;
        for (int i = 0; i < messageIds.length; i++) {
            buf.append(messageIds[i]);
            buf.append(delim);
        }

        return buf.toString().substring(0, buf.toString().length() - 1);
    }

    /**
     * return comma separated host name String
     * 
     * @param messageIds
     * @return String
     */
    private String generateHostString(int[] messageIds) {
        String result = null;
        String delimiter = Constant.COMMA;
        StringBuffer sb = new StringBuffer();
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventBean[] events = eventListBean.getDataTableBean().getEvents();
        for (int i = 0; i < messageIds.length; i++) {
            for (int j = 0; j < events.length; j++) {
                if (messageIds[0] == events[j].getLogMessageID()) {
                    sb.append(events[j].getDevice());
                    sb.append(delimiter);
                }
            }

        }

        if (sb.toString() != null && sb.toString().endsWith(delimiter)) {
            result = sb.toString().substring(0, sb.toString().length() - 1);
            LOGGER.debug((new StringBuilder()).append(
                    "Generated host String is ").append(result).toString());
        }
        return result;
    }

}
