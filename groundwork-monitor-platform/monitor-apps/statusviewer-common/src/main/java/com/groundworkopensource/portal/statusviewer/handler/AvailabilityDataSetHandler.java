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

import com.groundworkopensource.portal.statusviewer.bean.StateTransitionBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateTransition;

import java.util.*;

/**
 * This class handles the creation of dataset required to generate the stacked
 * bar chart for the availability portlets. It also adds up the fillers if there
 * are gaps between the data of state transitions.
 *
 * @author Ryan Graffy
 */
class AvailabilityDataSetHandler {

    private Calendar startDate;

    private static final Logger LOGGER = Logger
            .getLogger(AvailabilityDataSetHandler.class.getName());

    /**
     * @param startDate start of time window
     */
    AvailabilityDataSetHandler(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * Create a set of duration-oriented transitions beans to be used for graphing given a list of transitions and
     * current status.
     *
     * @param stateTransitions list of transitions
     * @param serviceCount number of services (if isHost)
     * @param entityName name of host/service
     * @param isHost specifies if this is host versus service status
     * @return stateTransitionBeanList
     */
    List<StateTransitionBean> createAvailabilityData(
            StateTransition[] stateTransitions, int serviceCount,
            String entityName, boolean isHost) {


        // Normalize all valid transitions in a sorted map so that we can process them in a straightforward manner
        TreeMap<Long, String> transitionMap = new TreeMap<>();

        // Verify that we have the pieces of information we need before processing transitions
        if (stateTransitions != null) {
            for (StateTransition transition : stateTransitions) {
                // Only add transitions if they occurred before the current status so that current status "trumps"
                // any pending statuses, etc.
                Date fromDate = transition.getFromTransitionDate();
                if (fromDate != null) {
                    transitionMap.put(fromDate.getTime(), transition.getFromStatus().getName());
                }
                Date toDate = transition.getToTransitionDate();
                if (toDate != null) {
                    transitionMap.put(toDate.getTime(), transition.getToStatus().getName());
                }
            }
        }

        // Add a end transition to cut data off at the current time
        transitionMap.put(Calendar.getInstance().getTimeInMillis(), Constant.NO_STATUS);

        // Add a beginning filler status if required
        long startTime = this.startDate.getTimeInMillis();
        if (transitionMap.firstKey() > startTime) {
            transitionMap.put(startTime, Constant.NO_STATUS);
        }

        // If there are no transitions that occur within the window, create a filler to ensure the row has at least one
        // transition
        if (transitionMap.lastKey() <= startTime) {
            transitionMap.put(startTime + 1, Constant.NO_STATUS);
        }

        // Determine appropriate display name
        String displayNameForEntity = isHost ? createHostName(entityName, serviceCount) : createServiceName(entityName);

        // Create transition beans using the normalized data
        List<StateTransitionBean> stateTransitionBeanList = new ArrayList<>();
        int stateCounter = 0;
        long previousTime = transitionMap.firstKey();
        for (Long time : transitionMap.keySet()) {
            if (time > startTime) {
                StateTransitionBean transition = new StateTransitionBean();
                transition.setEntityName(displayNameForEntity);
                transition.setTimeInState(time - Math.max(startTime, previousTime));
                transition.setToState(transitionMap.get(previousTime) + stateCounter++ + entityName);
                stateTransitionBeanList.add(transition);
            }
            previousTime = time;
        }

        return stateTransitionBeanList;
    }

    /*
    This method constructs the name of the host to be displayed as a label on
      the stacked bar chart. The format - Name of the host (number of service
      on the host)[current monitor status] . e.g. localhost(10 Services)[UP]
     */
    private static String createHostName(String name, int serviceCount) {
        StringBuilder entityName = new StringBuilder(Constant.EMPTY_STRING);

        String serviceCntString = Constant.OPEN_PARENTHESES + serviceCount
                + Constant.SPACE + Constant.SERVICES
                + Constant.CLOSED_PARENTHESES;
        int lenghtOfServiceCntString = serviceCntString.length();

        /*
         * The length of the host name must be <= (35 -
         * lenghtOfServiceCntString). If not ,truncate the host name and append
         * ...
         */
        int lengthOfName = name.trim().length();
        if ((lengthOfName + lenghtOfServiceCntString) > Constant.THIRTY_FIVE) {
            int allowedLengthForName = Constant.THIRTY_FIVE
                    - lenghtOfServiceCntString;
            if (lengthOfName > allowedLengthForName) {
                // Remove last 3 characters from name and append ...
                String subString = name.substring(0, allowedLengthForName
                        - Constant.THREE);
                entityName.append(subString).append(Constant.DOTS).append(
                        serviceCntString);
                return entityName.toString();
            }
        }
        entityName.append(name).append(serviceCntString);
        return entityName.toString();
    }

    /**
     * This method constructs the name of the service to be displayed as a label
     * on the stacked bar chart. The format - Name of the service [current
     * monitor status] . e.g. current_load[OK]
     *
     * @param name service name
     * @return entityName
     */
    private static String createServiceName(String name) {
        StringBuilder entityName = new StringBuilder(Constant.EMPTY_STRING);

        if (name != null) {
            // Get the length of the name.
            int lengthOfName = name.trim().length();
            if (lengthOfName != Constant.ZERO) {
                // length < 40
                if (lengthOfName < Constant.THIRTY_FIVE) {
                    entityName.append(name);
                    return entityName.toString();
                } else if (lengthOfName > Constant.THIRTY_FIVE) {
                    // length > 40
                    // Get the first 40 characters from the name.
                    String subString = name.substring(Constant.ZERO,
                            Constant.THIRTY_FIVE);
                    // Remove last 3 chars from the substring and append ... to
                    // it.
                    subString = subString.substring(0, subString.length()
                            - Constant.THREE);
                    // Append ... to the substring.
                    entityName.append(subString).append(Constant.DOTS);
                    return entityName.toString();
                }
            }
        }
        entityName.append(name);
        return entityName.toString();
    }

    /**
     * Get the current monitor status name for the service. This method is used
     * by ServiceAvailability - as SimpleService is not returned by the
     * 'getServicesById' and 'getServiceByHostAndServiceName' APIs.
     *
     * @param service current status
     * @return current monitor status.
     */
    static String getCurrentServiceStatusName(ServiceStatus service) {
        // Fetch the current monitor status for the host.
        NetworkObjectStatusEnum serviceStatus = MonitorStatusUtilities
                .getEntityStatus(service, NodeType.SERVICE);
        if (serviceStatus == null) {
            LOGGER
                    .debug(Constant.METHOD
                            + "getCurrentHostStatus() : Null Monitor status found for host :"
                            + service.getDescription());
            return Constant.EMPTY_STRING;
        }
        /*
         * Special case - Since for pending host, status returned from database
         * is "PENDING" and not "HOST_PENDING".
         */
        if (serviceStatus == NetworkObjectStatusEnum.SERVICE_PENDING) {
            return serviceStatus.getStatus().toUpperCase();
        }
        return serviceStatus.getMonitorStatusName();
    }

}
