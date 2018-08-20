/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. This program is free software; you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License
 *  version 2 as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.dashboard.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ws.ServiceState;

/**
 * This bean contains entire network statistics at high-level. It is used to
 * display NetstatProtlet - part of layout.
 * 
 * TODO: Currently, foundation web service APdoes not provide API to retrieve to
 * network status. This bean implements a temporary logic to switch network
 * status between "Good", "Bad", "Fair". Once Foundation API is available,
 * change it.
 * 
 * @author rashmi_tambe
 * 
 */
public class NetworkStatistics extends RenderableBean {

    /**
     * STAT COUNT 3
     */
    private static final int STAT_COUNT_3 = 3;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1526472295622776147L;

    /**
     * logger
     */
    private static Logger logger = Logger.getLogger(NetworkStatistics.class
            .getName());

    /**
     * Count of services critical in entire network.
     */
    private Long criticalServicesCount;

    /**
     * Count of services in warning state in entire network.
     */
    private Long warningServicesCount;

    /**
     * A string representation of network status. E.g. "Good", "Bad", "Fair".
     * The backend foundation API for getting this is not ready yet.
     */
    private String networkStatus;

    /**
     * Foundation WebService Facade instance.<br>
     * FIXME Remove commented code afterwards
     */
    // private static IWSFacade foundationWSFacade = new WebServiceFactory()
    // .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
    /**
     * A temporary implementation to simulate "netstat" update every interval.
     */
    private String[] netStatArr = { "Good", "Bad", "Fair" };
    /**
     * Status Count
     */
    private int statCount = 0;

    /**
     * critical Count
     */
    private static final long CRITICAL_COUNT = 5;
    /**
     * warning Count
     */
    private static final long WARNING_COUNT = 10;

    /**
     * Default Constructor
     */
    public NetworkStatistics() {
        /*
         * Following code is commented out to avoid error and build failure.
         * Instead we have provided dummy values for critical and warning count
         * in order to be able to run the portlet.
         * 
         * FIXME replace this method call with appropriate call.
         */
        // Map<ServiceState, Long> statistics =
        // foundationWSFacade.getStatistics(StatisticQueryType.TOTALS_BY_SERVICES);
        Map<ServiceState, Long> statistics = new HashMap<ServiceState, Long>();
        statistics.put(ServiceState.CRITICAL, CRITICAL_COUNT);
        statistics.put(ServiceState.WARNING, WARNING_COUNT);

        criticalServicesCount = statistics.get(ServiceState.CRITICAL);
        warningServicesCount = statistics.get(ServiceState.WARNING);

        logger
                .debug("Initialized NetworkStatistics bean with criticalServicesCount = "
                        + criticalServicesCount
                        + " | warningServicesCount = "
                        + warningServicesCount);

        // TODO: The backend foundation API for getting networkStatus is not
        // ready yet.So networkStatus is hard coded.
        networkStatus = netStatArr[statCount++];
    }

    /**
     * @return the criticalCount
     */
    public Long getCriticalServicesCount() {
        return criticalServicesCount;
    }

    /**
     * @return the warningServicesCount
     */
    public Long getWarningServicesCount() {
        return warningServicesCount;
    }

    /**
     * @return the networkStatus
     */
    public String getNetworkStatus() {
        // This is a temporary implementation to simulate network statistics
        // update after every time interval.
        if (statCount == STAT_COUNT_3) {
            statCount = 0;
        }
        networkStatus = netStatArr[statCount++];
        logger.debug("current networkStatus = " + networkStatus);
        return networkStatus;
    }
}
