package com.groundwork.collage.biz;

import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import org.groundwork.foundation.bs.exception.BusinessServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The current version of the Foundation REST API provides CRUD access to all Foundation entities.
 * For an application developer that wants to communicate status to the foundation backend several calls are necessary
 * (initialization, state change verification, update) to make it work.
 * To simplify the integration task additional business services are necessary to abstract the low level calls.
 * The first step is to come up with specifications for calls that would accelerate the completion of connectors and bridges
 *
 * @since 7.1.0
 */
public interface BizServices {

    public final static String SERVICE = "com.groundwork.collage.biz.BizServices";

    /**
     * Checks if the Host exists. If it doesn't exist the host entity is created and an initial event (PENDING) is created.
     * If the hostGroup is not null the hostGroup will be created if it doesn't exist.
     * Host will be assigned to the HostGroup.
     * If the hostCategory is not null the hostCategory will be created if it doesn't exist.
     * Host will be assigned to the HostCategory.
     * If the host exists status and message will be updated.
     * Method would also retrieve the current host status and if a state change occurred an Event and a Notification will be generated.
     *
     * @param host the required name of the host to be added or updated
     * @param status the required, valid monitor status to be set for the host
     * @param message the required message to be recorded for this host in event
     * @param hostGroup optional host group to associated with this host
     * @param hostCategory optional host category to associated with this host
     * @param device optional device name to be stored with this host
     * @param appType the required application type that is making this call
     * @param agentId the required identifying agent id for this call
     * @param checkIntervalMinutes the optional number of minutes to set the next check time. defaults to 5
     * @param allowInserts the optional boolean flag indicates if inserts are allowed on this call. defaults to true
     * @param mergeHosts optional flag to merge hosts with matching but different names; defaults to true
     * @param setStatusOnCreate optional flag to set status after PENDING set on create; defaults to false
     * @param created returned created flag or null
     * @return updated Host or null if not merged
     * @throws org.groundwork.foundation.bs.exception.BusinessServiceException
     */
    Host createOrUpdateHost(String host,
                            String status,
                            String message,
                            String hostGroup,
                            String hostCategory,
                            String device,
                            String appType,
                            String agentId,
                            Integer checkIntervalMinutes,
                            Boolean allowInserts,
                            Boolean mergeHosts,
                            Boolean setStatusOnCreate,
                            boolean [] created)
            throws BusinessServiceException;

    /**
     * Checks if the Host exists. If it doesn't exist the host entity is created and an initial event (PENDING) is created.
     * If the hostGroup is not null the hostGroup will be created if it doesn't exist.
     * Host will be assigned to the HostGroup.
     * If the hostCategory is not null the hostCategory will be created if it doesn't exist.
     * Host will be assigned to the HostCategory.
     * If the host exists status and message will be updated.
     * Method would also retrieve the current host status and if a state change occurred an Event and a Notification will be generated.
     * Caches of hosts, host groups, host categories, and devices are maintained for batch operation where these cannot
     * be queried for in non-flushing persistent session. Caching for hosts and services assumes consistent naming will
     * be used for hosts.
     *
     * @param host the required name of the host to be added or updated
     * @param status the required, valid monitor status to be set for the host
     * @param message the required message to be recorded for this host in event
     * @param hostGroup optional host group to associated with this host
     * @param hostCategory optional host category to associated with this host
     * @param device optional device name to be stored with this host
     * @param appType the required application type that is making this call
     * @param agentId the required identifying agent id for this call
     * @param checkIntervalMinutes the optional number of minutes to set the next check time. defaults to 5
     * @param allowInserts the optional boolean flag indicates if inserts are allowed on this call. defaults to true
     * @param mergeHosts optional flag to merge hosts with matching but different names; defaults to true
     * @param setStatusOnCreate optional flag to set status after PENDING set on create; defaults to false
     * @param hosts host cache for batch operation
     * @param hostGroups host group cache for batch operation
     * @param hostCategories host category cache for batch operation
     * @param devices device cache for batch operation
     * @param dynamicProperties dynamic Collage properties
     * @param created returned created flag or null
     * @return updated Host or null if not merged
     * @throws org.groundwork.foundation.bs.exception.BusinessServiceException
     */
    Host createOrUpdateHost(String host,
                            String status,
                            String message,
                            String hostGroup,
                            String hostCategory,
                            String device,
                            String appType,
                            String agentId,
                            Integer checkIntervalMinutes,
                            Boolean allowInserts,
                            Boolean mergeHosts,
                            Boolean setStatusOnCreate,
                            Map<String,Host> hosts,
                            Map<String,HostGroup> hostGroups,
                            Map<String,Category> hostCategories,
                            Map<String,Device> devices,
                            Map<String, String> dynamicProperties,
                            boolean [] created)
            throws BusinessServiceException;

    /**
     * Checks if the host and the service exist. If they don't exist both entities would be created and an initial event (PENDING)
     * for both entities would be created.
     * If the serviceGroup is not null the serviceGroup will be created if it doesn't exist.
     * Service will be assigned to the serviceGroup.
     * If the serviceCategory is not null the serviceCategory will be created if it doesn't exist.
     * Service will be assigned to the ServiceCategory.
     * If the hostGroup is not null the hostGroup will be created if it doesn't exist.
     * Host will be assigned to the HostGroup.
     * If the service exists status and message will be updated.
     * Method would also retrieve the current service status and if a state change occurred an Event and a Notification will be generated.
     *
     * @param host the required name of the host to be added or updated
     * @param service the required service name to be added or updated
     * @param status the required, valid monitor status to be set for the host
     * @param message the required message to be recorded for this host in event
     * @param serviceGroup optional service group to be associated with this service
     * @param serviceCategory optional service category to be associated with this service
     * @param hostGroup optional host group to associated with this host
     * @param hostCategory optional host category to associated with this host
     * @param device optional device name to be stored with this host
     * @param appType the required application type that is making this call
     * @param agentId the required identifying agent id for this call
     * @param checkIntervalMinutes the optional number of minutes to set the next check time. defaults to 5
     * @param allowInserts the optional boolean flag indicates if inserts are allowed on this call. defaults to true
     * @param mergeHosts optional flag to merge hosts with matching but different names; defaults to true
     * @param setStatusOnCreate optional flag to set status after PENDING set on create; defaults to false
     * @param serviceValue the required value to set for this service
     * @param warningLevel the optional warning level to set for this service. Set to -1 to skip sending to performance
     * @param criticalLevel the optional critical level to set for this service. Set to -1 to skip sending to performance
     * @param metricType the type of metric being stored such as hypervisor or vm
     * @param hostCreated returned host created flag or null
     * @param serviceCreated returned service created flag or null
     * @return updated Service or null if Host not merged
     * @throws org.groundwork.foundation.bs.exception.BusinessServiceException
     */
    ServiceStatus createOrUpdateService(String host,
                                        String service,
                                        String status,
                                        String message,
                                        String serviceGroup,
                                        String serviceCategory,
                                        String hostGroup,
                                        String hostCategory,
                                        String device,
                                        String appType,
                                        String agentId,
                                        Integer checkIntervalMinutes,
                                        Boolean allowInserts,
                                        Boolean mergeHosts,
                                        Boolean setStatusOnCreate,
                                        String serviceValue,
                                        long warningLevel,
                                        long criticalLevel,
                                        String metricType,
                                        boolean [] hostCreated,
                                        boolean [] serviceCreated,
                                        boolean processLogPerf)
        throws BusinessServiceException;

    /**
     * Checks if the host and the service exist. If they don't exist both entities would be created and an initial event (PENDING)
     * for both entities would be created.
     * If the serviceGroup is not null the serviceGroup will be created if it doesn't exist.
     * Service will be assigned to the serviceGroup.
     * If the serviceCategory is not null the serviceCategory will be created if it doesn't exist.
     * Service will be assigned to the ServiceCategory.
     * If the hostGroup is not null the hostGroup will be created if it doesn't exist.
     * Host will be assigned to the HostGroup.
     * If the service exists status and message will be updated.
     * Method would also retrieve the current service status and if a state change occurred an Event and a Notification will be generated.
     * Caches of host groups, host categories, hosts, devices, services, service groups, and service categories are
     * maintained for batch operation where these cannot be queried for in non-flushing persistent session. Caching for
     * hosts and services assumes consistent naming will be used for hosts.
     *
     * @param host the required name of the host to be added or updated
     * @param service the required service name to be added or updated
     * @param status the required, valid monitor status to be set for the host
     * @param message the required message to be recorded for this host in event
     * @param serviceGroup optional service group to be associated with this service
     * @param serviceCategory optional service category to be associated with this service
     * @param hostGroup optional host group to associated with this host
     * @param hostCategory optional host category to associated with this host
     * @param device optional device name to be stored with this host
     * @param appType the required application type that is making this call
     * @param agentId the required identifying agent id for this call
     * @param checkIntervalMinutes the optional number of minutes to set the next check time. defaults to 5
     * @param allowInserts the optional boolean flag indicates if inserts are allowed on this call. defaults to true
     * @param mergeHosts optional flag to merge hosts with matching but different names; defaults to true
     * @param setStatusOnCreate optional flag to set status after PENDING set on create; defaults to false
     * @param serviceValue the required value to set for this service
     * @param warningLevel the optional warning level to set for this service. Set to -1 to skip sending to performance
     * @param criticalLevel the optional critical level to set for this service. Set to -1 to skip sending to performance
     * @param metricType the type of metric being stored such as hypervisor or vm
     * @param hostGroups host group cache for batch operation
     * @param hostCategories host category cache for batch operation
     * @param hosts host cache for batch operation
     * @param devices device cache for batch operation
     * @param services service cache for batch operation
     * @param serviceGroups service group cache for batch operation
     * @param serviceCategories service category cache for batch operation
     * @param dynamicProperties dynamic Collage properties
     * @param hostCreated returned host created flag or null
     * @param serviceCreated returned service created flag or null
     * @return updated Service or null if Host not merged
     * @throws org.groundwork.foundation.bs.exception.BusinessServiceException
     */
    ServiceStatus createOrUpdateService(String host,
                                        String service,
                                        String status,
                                        String message,
                                        String serviceGroup,
                                        String serviceCategory,
                                        String hostGroup,
                                        String hostCategory,
                                        String device,
                                        String appType,
                                        String agentId,
                                        Integer checkIntervalMinutes,
                                        Boolean allowInserts,
                                        Boolean mergeHosts,
                                        Boolean setStatusOnCreate,
                                        String serviceValue,
                                        long warningLevel,
                                        long criticalLevel,
                                        String metricType,
                                        Map<String,HostGroup> hostGroups,
                                        Map<String,Category> hostCategories,
                                        Map<String,Host> hosts,
                                        Map<String,Device> devices,
                                        Map<String,ServiceStatus> services,
                                        Map<String,Category> serviceGroups,
                                        Map<String,Category> serviceCategories,
                                        Map<String,String> dynamicProperties,
                                        boolean [] hostCreated,
                                        boolean [] serviceCreated,
                                        boolean processLogPerf)
            throws BusinessServiceException;

    /**
     * Checks if service exists. If it doesn't exist, the service entity and an initial event (PENDING)
     * will be created.
     * If the serviceGroup is not null the serviceGroup will be created if it doesn't exist.
     * Service will be assigned to the serviceGroup.
     * If the serviceCategory is not null the serviceCategory will be created if it doesn't exist.
     * Service will be assigned to the ServiceCategory.
     * If the hostGroup is not null the hostGroup will be created if it doesn't exist.
     * Host will be assigned to the HostGroup.
     * If the service exists status and message will be updated.
     * Method would also retrieve the current service status and if a state change occurred an Event and a Notification will be generated.
     * Caches of services, service groups, and service categories are maintained for batch operation where these cannot
     * be queried for in non-flushing persistent session. Caching for hosts and services assumes consistent naming will
     * be used for hosts.
     *
     * @param host the required host for the service
     * @param hostName the required name of the host for the service
     * @param service the required service name to be added or updated
     * @param status the required, valid monitor status to be set for the host
     * @param message the required message to be recorded for this host in event
     * @param serviceGroup optional service group to be associated with this service
     * @param serviceCategory optional service category to be associated with this service
     * @param hostGroup host group associated with this host
     * @param device device name associated with this host
     * @param appType the required application type that is making this call
     * @param agentId the required identifying agent id for this call
     * @param checkIntervalMinutes the optional number of minutes to set the next check time. defaults to 5
     * @param mergeHosts optional flag to merge hosts with matching but different names; defaults to true
     * @param setStatusOnCreate optional flag to set status after PENDING set on create; defaults to false
     * @param serviceValue the required value to set for this service
     * @param warningLevel the optional warning level to set for this service. Set to -1 to skip sending to performance
     * @param criticalLevel the optional critical level to set for this service. Set to -1 to skip sending to performance
     * @param metricType the type of metric being stored such as hypervisor or vm
     * @param services service cache for batch operation
     * @param serviceGroups service group cache for batch operation
     * @param serviceCategories service category cache for batch operation
     * @param dynamicProperties dynamic Collage properties
     * @param created returned created flag or null
     * @return updated Service
     * @throws org.groundwork.foundation.bs.exception.BusinessServiceException
     */
    ServiceStatus createOrUpdateHostService(Host host,
                                            String hostName,
                                            String service,
                                            String status,
                                            String message,
                                            String serviceGroup,
                                            String serviceCategory,
                                            String hostGroup,
                                            String device,
                                            String appType,
                                            String agentId,
                                            Integer checkIntervalMinutes,
                                            Boolean mergeHosts,
                                            Boolean setStatusOnCreate,
                                            String serviceValue,
                                            long warningLevel,
                                            long criticalLevel,
                                            String metricType,
                                            Map<String,ServiceStatus> services,
                                            Map<String,Category> serviceGroups,
                                            Map<String,Category> serviceCategories,
                                            Map<String,String> dynamicProperties,
                                            boolean [] created,
                                            boolean processLogPerf)
            throws BusinessServiceException;

    /**
     * API class capturing primary keys, {@link com.groundwork.collage.util.Nagios#SCHEDULED_DOWNTIME_DEPTH} properties,
     * and entities in downtime metadata for {@link com.groundwork.collage.model.Host} and
     * {@link com.groundwork.collage.model.ServiceStatus} instances in downtime.
     */
    static class HostServiceInDowntime {
        /** host name, (required) */
        public String hostName;
        /** service description, (null for Host, required for ServiceStatus) */
        public String serviceDescription;
        /** {@link com.groundwork.collage.util.Nagios#SCHEDULED_DOWNTIME_DEPTH} property value, (read only) */
        public Integer scheduledDowntimeDepth;
        /** entity type in downtime, (HOST, SERVICE_STATUS, HOSTGROUP, or CATEGORY) */
        public String entityType;
        /** entity name in downtime */
        public String entityName;

        /**
         * Host constructor.
         *
         * @param hostName host name
         * @param entityType entity type in downtime
         * @param entityName entity name in downtime
         */
        public HostServiceInDowntime(String hostName, String entityType, String entityName) {
            this(hostName, null, entityType, entityName);
        }

        /**
         * Service constructor.
         *
         * @param hostName service host name
         * @param serviceDescription service description
         * @param entityType entity type in downtime
         * @param entityName entity name in downtime
         */
        public HostServiceInDowntime(String hostName, String serviceDescription, String entityType, String entityName) {
            this(hostName, serviceDescription, null, entityType, entityName);
        }

        /**
         * General constructor.
         *
         * @param hostName service host name
         * @param serviceDescription service description
         * @param scheduledDowntimeDepth scheduled downtime depth
         * @param entityType entity type in downtime
         * @param entityName entity name in downtime
         */
        public HostServiceInDowntime(String hostName, String serviceDescription, Integer scheduledDowntimeDepth, String entityType, String entityName) {
            this.hostName = hostName;
            this.serviceDescription = serviceDescription;
            this.scheduledDowntimeDepth = scheduledDowntimeDepth;
            this.entityType = entityType;
            this.entityName = entityName;
        }
    }

    /**
     * Increment the {@link com.groundwork.collage.util.Nagios#SCHEDULED_DOWNTIME_DEPTH} dynamic property for hosts and
     * services selected by hosts, service descriptions, host groups, and service groups. Together these select hosts
     * and/or services that will have their downtime properties set. Hosts and services can be specified as a list of
     * names and descriptions or as a single wildcard '*'. Host groups and service groups can be used to define the set
     * of hosts and services when wildcards are used, but their usage alone will not result in downtime properties being
     * set. Specifying a wildcard, empty, or null host/service matching parameter are equivalent. Also generates
     * {@link com.groundwork.collage.model.LogMessage} "events" to capture the downtime activity per host and service
     * for audit and SLA reporting. These events will have a monitor status of START_DOWNTIME or IN_DOWNTIME depending
     * on the initial downtime level of the corresponding host or service. Returns primary keys, resulting downtime
     * level, and entities in downtime metadata for the set of {@link com.groundwork.collage.model.Host} and
     * {@link com.groundwork.collage.model.ServiceStatus} instances that had their downtime level property incremented.
     *
     * @param hostNames collection of {@link com.groundwork.collage.model.Host} names or '*'
     * @param serviceDescriptions collection of {@link com.groundwork.collage.model.ServiceStatus} descriptions or '*'
     * @param hostGroupNames collection of {@link com.groundwork.collage.model.HostGroup} names
     * @param serviceGroupCategoryNames collection of service group {@link com.groundwork.collage.model.Category} names
     * @param setHosts set host downtime properties
     * @param setServices set service downtime properties
     * @return list of {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} for instances set
     * @throws {@link org.groundwork.foundation.bs.exception.BusinessServiceException}
     */
    List<HostServiceInDowntime> setHostsAndServicesInDowntime(Collection<String> hostNames,
                                                              Collection<String> serviceDescriptions,
                                                              Collection<String> hostGroupNames,
                                                              Collection<String> serviceGroupCategoryNames,
                                                              boolean setHosts, boolean setServices)
            throws BusinessServiceException;

    /**
     * Decrement the {@link com.groundwork.collage.util.Nagios#SCHEDULED_DOWNTIME_DEPTH} dynamic property for specified
     * hosts and services. These, along with metadata about the entities in downtime, are specified by
     * {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} elements returned by the
     * {@link com.groundwork.collage.biz.BizServices#setHostsAndServicesInDowntime(java.util.Collection,
     * java.util.Collection, java.util.Collection, java.util.Collection, boolean, boolean)} method. This method also
     * generates {@link com.groundwork.collage.model.LogMessage} "events" to capture the downtime activity per host and
     * service for audit and SLA reporting. These events will have a monitor status of IN_DOWNTIME or END_DOWNTIME
     * depending on the resulting downtime level of the corresponding host or service. Returns primary keys and
     * resulting downtime level for the set of {@link com.groundwork.collage.model.Host} and
     * {@link com.groundwork.collage.model.ServiceStatus} instances that had their downtime level property decremented.
     *
     * @param hostsAndServices list of {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} to clear
     * @return list of {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} for instances cleared
     * @throws BusinessServiceException
     */
    List<HostServiceInDowntime> clearHostsAndServicesInDowntime(List<HostServiceInDowntime> hostsAndServices)
            throws BusinessServiceException;

    /**
     * Get the {@link com.groundwork.collage.util.Nagios#SCHEDULED_DOWNTIME_DEPTH} dynamic property for specified
     * hosts and services. Returns primary keys and downtime level for the {@link com.groundwork.collage.model.Host}
     * and {@link com.groundwork.collage.model.ServiceStatus} instances.
     *
     * @param hostsAndServices list of {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} to get
     * @return list of {@link com.groundwork.collage.biz.BizServices.HostServiceInDowntime} instances
     * @throws BusinessServiceException
     */
    List<HostServiceInDowntime> getHostsAndServicesInDowntime(List<HostServiceInDowntime> hostsAndServices)
            throws BusinessServiceException;

    /**
     * API class holding authorized hosts and service names for a user.
     */
    static class AuthorizedServices {
        public List<String> hostNames;
        public Map<String,List<String>> serviceHostNames;

        public AuthorizedServices(List<String> hostNames, Map<String,List<String>> serviceHostNames) {
            this.hostNames = hostNames;
            this.serviceHostNames = serviceHostNames;
        }
    }

    /**
     * Returns full access authorized services.
     *
     * @return full access authorized services
     */
    AuthorizedServices getAuthorizedServices();

    /**
     * Returns authorized services expanded from from authorized host groups and authorized service groups for a user.
     * Names of hosts fully accessible from host groups and a mapping of all service descriptions and their accessible
     * hosts are returned. Services access is determined from both the host groups, (all services per host), and service
     * groups as specified. A null return indicates full access.
     *
     * @param authorizedHostGroups authorized host groups or null
     * @param authorizedServiceGroups authorized service groups or null
     * @return authorized services or null
     */
    AuthorizedServices getAuthorizedServices(List<String> authorizedHostGroups, List<String> authorizedServiceGroups);
}
