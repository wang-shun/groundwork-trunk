package com.groundworkopensource.portal.statusviewer.common;

import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;

import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * Utility class for Monitor status Related Utilities
 * 
 * @author nitin_jadhav
 * 
 */
public class MonitorStatusUtilities {

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected MonitorStatusUtilities() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Web Services Foundation instance
     */
    private static final IWSFacade FOUNDATION_WS_FACADE = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * Priority based ranking for Host Group: UNSCHEDULED_DOWN, PENDING,
     * UNREACHABLE, SCHEDULED_DOWN, UP
     */
    private static String[] hostGroupStatusPriority = {
            NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                    .getMonitorStatusName(),
            NetworkObjectStatusEnum.HOST_UNREACHABLE.getMonitorStatusName(),
            NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED.getMonitorStatusName(),
            NetworkObjectStatusEnum.PENDING_STATUS_CONSTANT,
            NetworkObjectStatusEnum.HOST_UP.getMonitorStatusName() };

    /**
     * Priority based ranking for Service Group: UNSCHEDULED_CRITICAL, WARNING,
     * PENDING, SCHEDULED_CRITICAL, UNKNOWN, OK
     */
    private static String[] serviceGroupStatusPriority = {
            NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
                    .getMonitorStatusName(),
            NetworkObjectStatusEnum.SERVICE_WARNING.getMonitorStatusName(),
            NetworkObjectStatusEnum.PENDING_STATUS_CONSTANT,
            NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
                    .getMonitorStatusName(),
            NetworkObjectStatusEnum.SERVICE_UNKNOWN.getMonitorStatusName(),
            NetworkObjectStatusEnum.SERVICE_OK.getMonitorStatusName() };

    // /**
    // * Returns Service entity status.
    // *
    // * @param service
    // * @return Service entity status
    // */
    // public static NetworkObjectStatusEnum getEntityStatus(ServiceStatus
    // service) {
    // String statusString = service.getMonitorStatus().getName();
    // return (NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
    // statusString, false));
    // }
    //
    // /**
    // * Returns Host entity status.
    // *
    // * @param host
    // * @return Host entity status
    // */
    // public static NetworkObjectStatusEnum getEntityStatus(Host host) {
    // String statusString = host.getMonitorStatus().getName();
    // return NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
    // statusString, true);
    // }

    /**
     * Returns Service entity status.
     * 
     * @param service
     * @param nodeType
     * @return Service entity status
     */
    public static NetworkObjectStatusEnum getEntityStatus(
            ServiceStatus service, NodeType nodeType) {
        String statusString = service.getMonitorStatus().getName();
        return (NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
                statusString, nodeType));
    }

    /**
     * Returns Host entity status.
     * 
     * @param host
     * @param nodeType
     * @return Host entity status
     */
    public static NetworkObjectStatusEnum getEntityStatus(Host host,
            NodeType nodeType) {
        String statusString = host.getMonitorStatus().getName();
        return NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
                statusString, nodeType);
    }

    /**
     * Decides the status of HostGroup by calling statistics web service <br>
     * 
     * @param hostGroup
     * @return Host Group entity status
     */
    public static NetworkObjectStatusEnum getEntityStatus(HostGroup hostGroup) {
        /*
         * How to get HostGroup Status? Answer from Arul: Use getStatistics for
         * HostGroupBy Name and get the unscheduled down or unreachable or
         * pending. If any of these are at least one then that is the status.
         */
        StatisticProperty[] hostStatisticsForHostGroup;
        try {
            hostStatisticsForHostGroup = FOUNDATION_WS_FACADE
                    .getHostStatisticsForHostGroup(hostGroup.getName());
        } catch (WSDataUnavailableException e) {
            return NetworkObjectStatusEnum.NO_STATUS;
        }
        // compute the entity (Host Group) status and return it. since the
        // entity is HostGroup, pass true as third argument
        return computeEntityStatus(hostStatisticsForHostGroup,
                hostGroupStatusPriority, true, NodeType.HOST_GROUP);
    }

    /**
     * Computes Entity Status from statistic properties based on priority
     * passed.
     * 
     * @param statisticProperties
     * @param hostGroupStatusPriority
     * @return NetworkObjectStatusEnum
     */
    private static NetworkObjectStatusEnum computeEntityStatus(
            StatisticProperty[] statisticProperties, String[] statusPriority,
            boolean isHost, NodeType nodeType) {
        // for each monitor status check if statistic properties count returned
        // by web service is > 0.
        for (String monitorStatus : statusPriority) {
            // go through each statisticProperty
            for (StatisticProperty statisticProperty : statisticProperties) {
                String statName = statisticProperty.getName();
                if (statName != null
                        && (statName.equalsIgnoreCase(monitorStatus))
                        && statisticProperty.getCount() > 0) {
                    // if count > 0, then thats it. Return the Status Enum for
                    // that particular monitor status.
                    return NetworkObjectStatusEnum
                            .getStatusEnumFromMonitorStatus(monitorStatus,
                                    nodeType);
                }
            } // end of statisticProperties for
        } // end of monitorStatus for loop
        return NetworkObjectStatusEnum.NO_STATUS;
    }

    /**
     * Decides the status of ServiceGroup
     * 
     * @param serviceGroup
     * @return NetworkObjectStatusEnum
     */
    public static NetworkObjectStatusEnum getEntityStatus(Category serviceGroup) {
        /*
         * How to get Service Group Status? Answer from Arul: Use getStatistics
         * for ServiceGroup By Name and get the unscheduled critical or
         * unreachable or pending. If any of these are at least one then that is
         * the status.
         */
        StatisticProperty[] serviceStatisticsForServiceGroup = FOUNDATION_WS_FACADE
                .getServiceStatisticsForServiceGroup(serviceGroup.getName());
        // compute the entity (Service Group) status and return it. since the
        // entity is serviceGroup, pass false as third argument
        return computeEntityStatus(serviceStatisticsForServiceGroup,
                serviceGroupStatusPriority, false, NodeType.SERVICE_GROUP);
    }

    /**
     * return stating status in camel case
     * 
     * @param status
     * @return String
     */
    public static String getCamelCaseStatus(String status) {
        String monitorStatus = Constant.EMPTY_STRING;
        if (status != null) {
            String firstLatter = status.substring(Constant.ZERO, Constant.ONE);
            String remLatter = status.substring(Constant.ONE, status.length());
            monitorStatus = firstLatter + remLatter.toLowerCase();
        }
        return monitorStatus;
    }

    /**
     * This method checks the monitor status and value of
     * 'isProblemAcknowledged' property for a service. If the monitor status for
     * the service is 'ok' or 'pending' or the service is already
     * acknowledged,then return false,else return true indicating that the
     * service can be acknowledged.
     * 
     * @param service
     * @return whether the service is acknowledgeable or not.
     */
    public static boolean isServiceAcknowledgeable(ServiceStatus service) {
        if (service != null) {
            PropertyTypeBinding propertyTypeBinding = service
                    .getPropertyTypeBinding();
            // Get the boolean property 'isProblemAcknowledged'
            if (propertyTypeBinding != null) {
                BooleanProperty booleanProperty = propertyTypeBinding
                        .getBooleanProperty(Constant.IS_PROBLEM_ACKNOWLEDGED);
                if (booleanProperty != null) {
                    if (booleanProperty.isValue()) {
                        // service is non-acknowledgeable
                        return false;
                    }
                }
                // get the monitorStatus of the service
                MonitorStatus monitorStatus = service.getMonitorStatus();
                if (monitorStatus != null) {
                    /*
                     * Check if the monitor status is OK or Pending or the
                     * service is already acknowledged. Such service should not
                     * be acknowledged.
                     */
                    if ((monitorStatus.getName().equalsIgnoreCase(Constant.OK))
                            || (monitorStatus.getName()
                                    .equalsIgnoreCase(Constant.PENDING))) {
                        // service is non-acknowledgeable
                        return false;
                    }
                } // (monitorStatus != null)
            } // (propertyTypeBinding != null)
        }
        // service is acknowledgeable
        return true;
    }

    /**
     * This method retrieves NetworkObjectStatusEnum for the simpleHost
     * 
     * @param host
     * @param type
     * @return NetworkObjectStatusEnum
     */
    public static NetworkObjectStatusEnum getEntityStatus(SimpleHost host,
            NodeType type) {
        String statusString = host.getMonitorStatus();
        return NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
                statusString, type);
    }

    /**
     * This method retrieves NetworkObjectStatusEnum for the simpleService
     * 
     * @param simpleService
     * @param type
     * @return NetworkObjectStatusEnum
     */
    public static NetworkObjectStatusEnum getEntityStatus(
            SimpleServiceStatus simpleService, NodeType type) {
        String statusString = simpleService.getMonitorStatus();
        return NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
                statusString, type);
    }
}
