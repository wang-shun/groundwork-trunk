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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateTransition;

import com.groundworkopensource.portal.statusviewer.bean.StateTransitionBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;

/**
 * This class handles the creation of dataset required to generate the stacked
 * bar chart for the availability portlets. It also adds up the fillers if there
 * are gaps between the data of state transitions.
 * 
 * @author shivangi_walvekar
 * 
 */
public class AvailabilityDataSetHandler {

    /**
     * start date
     */
    private Calendar startDate;

    /**
     * @return startDate
     */
    public Calendar getStartDate() {
        return startDate;
    }

    /**
     * @param startDate
     */
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(AvailabilityDataSetHandler.class.getName());

    /**
     * 
     * @param startDate
     */
    public AvailabilityDataSetHandler(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * For each stateTransition - 1) get the toTransitionDate 2) Set the
     * previousDate 3) check if (prevDate < toTransitionDate) 4) If yes , Add
     * filler data. (durationInState = toTransitionDate - previousDate) 5) if
     * No, Add actual data . (= durationInState). set previous date =
     * toTransitionDate + durationInState 6) Add these records in
     * stateTransitionBeanList. 7) after the last transition record,add a filler
     * for current status.
     * 
     * @param stateTransitions
     * @param serviceCount
     * @param entityName
     * @param currentStatus
     * @param isHost
     * @return stateTransitionBeanList
     */
    public List<StateTransitionBean> createAvailabilityData(
            StateTransition[] stateTransitions, int serviceCount,
            String entityName, String currentStatus, boolean isHost) {
        List<StateTransitionBean> stateTransitionBeanList = new ArrayList<StateTransitionBean>();
        if (stateTransitions == null) {
            LOGGER
                    .debug(Constant.METHOD
                            + "createAvailabilityData() : Null state transitions data found for :"
                            + entityName);
            return stateTransitionBeanList;
        }
        Date prevToDate = null;
        // Assign startDate in milliseconds as previousDuration.
        long previousDuration = getStartDate().getTimeInMillis();
        int toStateCounter = 0;

        String displayNameForEntity = Constant.EMPTY_STRING;
        if (isHost) {
            displayNameForEntity = createHostName(entityName, serviceCount);
        } else {
            displayNameForEntity = createServiceName(entityName);
        }
        if (stateTransitions[0] == null) {
            LOGGER
                    .debug(Constant.METHOD
                            + "createAvailabilityData() : Null state transition data found for :"
                            + entityName);
        } else {
            StateTransition stateTransition0 = stateTransitions[0];
            prevToDate = stateTransition0.getToTransitionDate();
            if (previousDuration < stateTransition0.getToTransitionDate()
                    .getTime()) {
                /**
                 * In this case, Set the FromStatus as the toStatus in
                 * createFillerData() method so that this transition will be
                 * displayed in the graph resulting in a continuous bar.
                 */
                String fromState = Constant.EMPTY_STRING;
                if (stateTransition0.getFromStatus() != null) {
                    fromState = stateTransition0.getFromStatus().getName();
                }
                StateTransitionBean fillerBean = createFillerData(
                        stateTransition0.getToTransitionDate(),
                        previousDuration, fromState, toStateCounter++,
                        entityName);
                fillerBean.setEntityName(displayNameForEntity);
                // Create a filler record
                stateTransitionBeanList.add(fillerBean);
            }
        }
        /**
         * Since the prevToDate.equals(toTransitionDate) is always true in case
         * of 0th transition,we use this flag so that 0th transition won't be
         * skipped.
         */
        boolean initialTransition = true;
        for (StateTransition stateTransition : stateTransitions) {
            /**
             * This counter is appended to the toState. StackedBar chart groups
             * the data on rowKey.In our case toState is the rowKey. Hence we
             * set this counter and postfix it to the toState.
             */
            if (stateTransition == null) {
                LOGGER
                        .debug(Constant.METHOD
                                + "createAvailabilityData() : Null state transition data found for :"
                                + entityName);
                return stateTransitionBeanList;
            }
            // Get the toTransitionDate from stateTransition object.
            Date toTransitionDate = stateTransition.getToTransitionDate();
            if ((prevToDate != null) && (!initialTransition)) {
                /*
                 * http://jira/browse/GWMON-7650 (If notifications are enabled
                 * globally ,2 events get generated in event console application
                 * after submit passive checks for host/services. ) As a
                 * consequence of this JIRa,2 events are generated for for a
                 * state change. Hence we check if the previous toTransitionDate
                 * <= toTransitionDate of the current transition,then skip that
                 * transition.
                 */
                if ((prevToDate.after(toTransitionDate))
                        || (prevToDate.equals(toTransitionDate))) {
                    continue;
                }
            }
            // Get the durationInState from stateTransition object.
            long durationInState = stateTransition.getDurationInState();
            // create actual data - The transition data exists.
            StateTransitionBean actualDataBean = createActualData(
                    stateTransition, toStateCounter++, entityName);
            actualDataBean.setEntityName(displayNameForEntity);
            stateTransitionBeanList.add(actualDataBean);

            // Reset the previousDuration
            previousDuration = toTransitionDate.getTime() + durationInState;
            // Reset the prevToDate
            prevToDate = toTransitionDate;
            initialTransition = false;
        } // for

        /*
         * Create a state transition for the current status. Assume 'Duration in
         * state' as till end date.
         */
        Calendar calendar = Calendar.getInstance();
        long timeInMillis = calendar.getTimeInMillis();
        if (timeInMillis > previousDuration) {
            StateTransitionBean stateTransitionBean = new StateTransitionBean();
            // Add current status as toState
            stateTransitionBean.setToState(currentStatus + (toStateCounter++)
                    + entityName);
            // Set entityName
            stateTransitionBean.setEntityName(displayNameForEntity);
            // Set time in state
            stateTransitionBean.setTimeInState(timeInMillis - previousDuration);
            stateTransitionBeanList.add(stateTransitionBean);
        }
        return stateTransitionBeanList;
    }

    /**
     * This method creates the dummy/filler data for the date ranges in which
     * there are no state transition available.
     * 
     * @param stateTransitionBean
     * @param toTransitionDate
     * @return
     */
    private StateTransitionBean createFillerData(Date toTransitionDate,
            long previousDuration, String fromState, int toStateCounter,
            String entityName) {
        StateTransitionBean stateTransitionBean = new StateTransitionBean();
        // Set the toState
        if (!Constant.EMPTY_STRING.equals(fromState)) {
            stateTransitionBean.setToState(fromState + toStateCounter
                    + entityName);
        } else {
            stateTransitionBean.setToState(Constant.NO_STATUS);
        }
        // Set the duration in state.
        stateTransitionBean.setTimeInState(toTransitionDate.getTime()
                - previousDuration);
        return stateTransitionBean;
    }

    /**
     * This method populates StateTransitionBean with the data from
     * stateTransition.
     * 
     * @param stateTransition
     * @param serviceCount
     * @return
     */
    private StateTransitionBean createActualData(
            StateTransition stateTransition, int toStateCounter,
            String entityName) {
        StateTransitionBean stateTransitionBean = new StateTransitionBean();
        // String entityName = Constant.EMPTY_STRING;
        if (stateTransition.getToStatus() == null) {
            LOGGER
                    .debug(Constant.METHOD
                            + "getHostStateTransitions() : stateTransition.getToStatus() found to be null ");
            return stateTransitionBean;
        }
        // The state to which host has transitioned to.
        stateTransitionBean.setToState(stateTransition.getToStatus().getName()
                + toStateCounter + entityName);

        // Set the duration in state.
        stateTransitionBean
                .setTimeInState(stateTransition.getDurationInState());
        return stateTransitionBean;
    }

    /**
     * This method constructs the name of the host to be displayed as a label on
     * the stacked bar chart. The format - Name of the host (number of service
     * on the host)[current monitor status] . e.g. localhost(10 Services)[UP]
     * 
     * @param name
     * @param serviceCount
     * @return entityName
     */
    public static String createHostName(String name, int serviceCount) {
        StringBuffer entityName = new StringBuffer(Constant.EMPTY_STRING);

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
     * @param name
     * @return entityName
     */
    public static String createServiceName(String name) {
        StringBuffer entityName = new StringBuffer(Constant.EMPTY_STRING);

        if (name != null) {
            // Get the length of the name.
            int lengthOfName = name.trim().length();
            if (lengthOfName != Constant.ZERO) {
                // length < 40
                if (lengthOfName < Constant.THIRTY_FIVE) {
                    int numberOfspacesToPad = Constant.THIRTY_FIVE
                            - lengthOfName;
                    // Pad blank spaces to the left.
                    for (int i = 0; i < numberOfspacesToPad; i++) {
                        entityName.append(Constant.SPACE);
                    }
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
     * @param service
     * @return current monitor status.
     * 
     */
    public static String getCurrentServiceStatusName(ServiceStatus service) {
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
