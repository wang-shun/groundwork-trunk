/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.groundwork.collage.impl.admin;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.CollageCommand;
import com.groundwork.collage.CollageEntity;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.CollageSeverity;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.ConsolidationCriteria;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.util.DateTime;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import com.groundwork.collage.util.Nagios;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * CollageAdminImpl
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe
 *         Paravicini</a>
 * @version $Id: CollageAdminImpl.java 19310 2012-08-15 23:13:13Z rruttimann $
 */
public class CollageAdminImpl extends HibernateDaoSupport implements
		CollageAdminInfrastructure {
	/* String Constants */
	private static final String UNDEFINED = "UNDEFINED";
	private static final String PRIORITY_1 = "1";
	private static final String OP_STATUS_OPEN = "OPEN";
	private static final String OP_STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";

	private static String DEFAULT_MONITOR_SERVER = "localhost";
	private static String NAGIOS = "NAGIOS";

	private static final String HOST_DOWN = "DOWN";
	private static final String SERVICE_DOWN = "DOWN";
	private static final String SCHEDULED = "SCHEDULED";
	private static final String UNSCHEDULED = "UNSCHEDULED";
	private static final String CRITICAL = "CRITICAL";

    private static final String SYSTEM_CONSOLIDATION = "SYSTEM";

    public static final String DUPLICATE_ERROR = "Duplicate Key detected. New host name already exists";

	/**
	 * Get an accessor to the Bean factory
	 */
	private CollageFactory _collage = CollageFactory.getInstance();

	private MetadataService metadataService = null;
	private MonitorServerService monitorService = null;
	private DeviceService deviceService = null;
	private HostGroupService hgService = null;
	private HostService hostService = null;
	private StatusService statusService = null;
	private LogMessageService logMsgService = null;
	private CategoryService categoryService = null;
	private ConsolidationService consolidationService = null;
	private PerformanceDataService performanceService = null;
    private HostIdentityService hostIdentityService = null;

	private CollageAdminMetadata adminMeta = null;

	private static final String DEFAULT_APP_TYPE = "NAGIOS";

	/** Event Acknowledgment Fields */
	private static final String ACKNOWLEDGEDBY = "AcknowledgedBy";
	private static final String ACKNOWLEDGE_COMMENT = "AcknowledgeComment";
    private static final String NAGIOS_ACK_COMMENT = "Nagios ACK pending";
	/** Entity Command Constants **/
	private static final String ADMIN_ACTION_ADD = "add";
	private static final String ADMIN_ACTION_MODIFY = "modify";
	private static final String ADMIN_ACTION_REMOVE = "remove";

	private static final String ADMIN_ACTION_CLEAR = "clear";

	// entity types
	private static final String ADMIN_TYPE_LOG_MESSAGE = "LogMessage";
	private static final String ADMIN_TYPE_HOST = "Host";
	private static final String ADMIN_TYPE_HOSTGROUP = "HostGroup";
	private static final String ADMIN_TYPE_DEVICE = "Device";
	private static final String ADMIN_TYPE_DEVICE_CHILD = "DeviceChild";
	private static final String ADMIN_TYPE_DEVICE_PARENT = "DeviceParent";
	private static final String ADMIN_TYPE_SERVICE_STATUS = "Service";
	private static final String ADMIN_TYPE_SERVICEGROUP = "ServiceGroup";
	private static final String ADMIN_TYPE_CONSOLIDATION = "ConsolidationCriteria";
	private static final String ADMIN_TYPE_MONITOR_SERVER = "MonitorServer";
	private static final String ADMIN_TYPE_NAGIOS_LOG = "NAGIOS_LOG";

	/* Performance Data feed into Foundation using Listener */
	private static final String ADMIN_TYPE_PERFORMANCE_DATA = "PerformanceData";

	/* PerformanceData attributes */
	private static final String PERFORMANCE_DATA_HOST = "hostname";
	private static final String PERFORMANCE_DATA_SERVICE = "servicedescription";
	private static final String PERFORMANCE_DATA_LABEL = "performancedatalabel";
	private static final String PERFORMANCE_DATA_VALUE = "performancevalue";
	private static final String PERFORMANCE_DATA_CHECKDATE = "checkdate";

	private static final String PERFORMANCE_DATA_INTERVAL_PROP = "fp.data.rollup";
	private static final String PERFORMANCE_DATA_DEFAULT_INTERVAL = "day";

	/* Value will be initialized on first invocation of performance data post */
	private static String rollupInterval = null;

	private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/foundation.properties";

	// key = "host;service", value = consolidation hash
	private static final Map<String, Integer> hashCache = new ConcurrentHashMap<>();

	// key = "host;service", value = most recent log message ID
	private static final Map<String, Integer> messageIdCache = new ConcurrentHashMap<>();
	private LogMessage getLogMessageFromCachedId(String cacheKey) {
		Integer logMessageId = messageIdCache.get(cacheKey);
		return (logMessageId == null ? null : logMsgService.getLogMessageById(logMessageId));
	}

	/** Use log4j */
	protected Log log = LogFactory.getLog(this.getClass());

	private Properties configuration = null;

	private CollageMetrics collageMetrics = null;

	private CollageMetrics getCollageMetrics() {
		if (collageMetrics == null) {
			collageMetrics = CollageFactory.getInstance().getCollageMetrics();
		}
		return collageMetrics;
	}

	public CollageTimer startMetricsTimer() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
		CollageMetrics collageMetrics = getCollageMetrics();
		return (collageMetrics == null ? null : collageMetrics.startTimer(className, element.getMethodName()));
	}

	public void stopMetricsTimer(CollageTimer timer) {
		CollageMetrics collageMetrics = getCollageMetrics();
		if (collageMetrics != null) collageMetrics.stopTimer(timer);
	}

	public CollageAdminImpl(CollageAdminMetadata adminMeta,
			MetadataService mds, MonitorServerService mss, DeviceService ds,
			HostService hs, StatusService ss, HostGroupService hgs,
			LogMessageService lms, CategoryService cats,
			ConsolidationService cs, PerformanceDataService pds,
            HostIdentityService his) {
		super();
		this.adminMeta = adminMeta;
		metadataService = mds;
		monitorService = mss;
		deviceService = ds;
		hgService = hgs;
		hostService = hs;
		statusService = ss;
		logMsgService = lms;
		categoryService = cats;
		consolidationService = cs;
		performanceService = pds;
        hostIdentityService = his;
		String configFile = System.getProperty("configuration",
				FOUNDATION_PROPERTY_FILE);
		configuration = new Properties();
		try {
			FileInputStream fis = new FileInputStream(configFile);
			configuration.load(fis);

		} catch (Exception e) {
			log
					.warn("WARNING: Could not load foundation properties. Event Preprocessing may not happen");
		}
	}

    public ServiceStatus updateServiceStatus(String monitorServerName,
                                             String applicationType, String hostName, String deviceIdent,
                                             Map properties) {
        return updateServiceStatus(null, monitorServerName, applicationType, hostName, deviceIdent, null, null, null,
                true, properties);
    }

    public ServiceStatus updateServiceStatus(String monitorServerName,
                                             String applicationType, String hostName, String deviceIdent,
                                             String agentId,
                                             Map properties) {
        return updateServiceStatus(null, monitorServerName, applicationType, hostName, deviceIdent, agentId, null, null,
                true, properties);
    }

    public ServiceStatus updateServiceStatus(ServiceStatus serviceStatus, String monitorServerName,
			String applicationType, String hostName, String deviceIdent,
            String agentId, Host host, Device device, boolean mergeHosts,
            Map properties) {
		CollageTimer timer = startMetricsTimer();
		String monitorStatus = (String) properties.get(PROP_MONITOR_STATUS);
		// update critical monitor status depending on effective downtime
		Object updateDowntime = properties.get(PROP_SCHEDULED_DOWNTIME_DEPTH);
		String effectiveDowntime = ((updateDowntime != null) ? updateDowntime.toString() : null);
		if ((effectiveDowntime == null) && (serviceStatus != null)) {
			Integer downtimeProperty = (Integer)host.getHostStatus().getProperty(PROP_SCHEDULED_DOWNTIME_DEPTH);
			effectiveDowntime = ((downtimeProperty) != null ? downtimeProperty.toString() : null);
		}
		String newMonitorStatus = updateServiceDownTimeStatus(monitorStatus, effectiveDowntime);
		if (newMonitorStatus != null) {
			properties.remove(PROP_MONITOR_STATUS);
			properties.put(PROP_MONITOR_STATUS, newMonitorStatus);
			String lastPluginOutput = (String)properties.get(PROP_LAST_PLUGIN_OUTPUT);
			if (lastPluginOutput != null) {
				lastPluginOutput = lastPluginOutput.replace(monitorStatus, newMonitorStatus);
				properties.remove(PROP_LAST_PLUGIN_OUTPUT);
				properties.put(PROP_LAST_PLUGIN_OUTPUT, lastPluginOutput);
			}
			monitorStatus = newMonitorStatus;
		}
        if (((hostName == null) || (hostName.length() == 0)) && (host != null)) {
            hostName = host.getHostName();
        }
        if (((deviceIdent == null) || (deviceIdent.length() == 0)) && (device != null)) {
            deviceIdent = device.getIdentification();
        }
		String serviceDescr = (String) properties
				.remove(ServiceStatus.EP_SERVICE_DESCRIPTION);
		if (serviceDescr == null) {
			String msg = "CollageAdminAPI - attempting to update ServiceStatus with null ServiceDescription on host '"
					+ hostName + "' - '" + deviceIdent + "'";
			log.error(msg);
			throw new CollageException(msg);
		}

        ApplicationType appType = null;
        if (applicationType != null) {
            appType = metadataService
                    .getApplicationTypeByName(applicationType);
            if (appType == null)
                appType = metadataService
                        .getApplicationTypeByName(DEFAULT_APP_TYPE);
        }

		if (_collage.isAutoCreateUnknownProperties())
			adminMeta.createOrAssignUnknownProperties(applicationType,
					ServiceStatus.ENTITY_TYPE_CODE, properties);

		try {
            if (serviceStatus == null) {
                serviceStatus = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(serviceDescr, hostName);
            }
            // validate merging host host name and host name, (if not an alias
            // lookup, host names differing by only case is considered a merge
            // since names are matching but different)
            Host serviceStatusHost = ((serviceStatus != null) ? serviceStatus.getHost() : null);
            if (!mergeHosts && (serviceStatusHost != null) && !hostName.equals(serviceStatusHost.getHostName()) &&
                    hostName.equalsIgnoreCase(serviceStatusHost.getHostName())) {
                sendHostsMergeMessage(hostName, serviceStatusHost.getHostName(), serviceStatusHost, applicationType);
				return null;
            }

			String lastMonitorStatus = null;
            boolean newService = false;
			if (serviceStatus == null) {
                if (appType == null) {
                    throw new CollageException("Adding service, appType was not provided, appType is required on insert");
                }
                if (host == null) {
                    host = this.getOrCreateHost(appType, monitorServerName,
                            hostName, deviceIdent, device);
                    // validate merging host host name and host name, (if not an alias
                    // lookup, host names differing by only case is considered a merge
                    // since names are matching but different)
                    if (!mergeHosts && (host != null) && !hostName.equals(host.getHostName()) &&
                            hostName.equalsIgnoreCase(host.getHostName())) {
                        sendHostsMergeMessage(hostName, host.getHostName(), host, applicationType);
						return null;
                    }
                }
                newService = true;
				serviceStatus = statusService.createService(serviceDescr,
						applicationType, host);

                // save application host name if different than host name
                if (!host.getHostName().equalsIgnoreCase(hostName)) {
                    serviceStatus.setApplicationHostName(hostName);
                }
			} else {
                // check owning application type
                boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                        (serviceStatus.getApplicationType() == null) ||
                        applicationType.equals(serviceStatus.getApplicationType().getName()));
                // validate owner application type updates
                if (!owner) {
                    sendNotOwnerServiceMessage(serviceStatus, applicationType);
					return null;
                }

				lastMonitorStatus = serviceStatus.getMonitorStatus().getName();
			}

            updateAdditionalServiceStatusProperties(serviceStatus, properties, lastMonitorStatus, monitorStatus, newService);

            serviceStatus.setProperties(properties);
			if (lastMonitorStatus != null) {
				serviceStatus.setLastMonitorStatus(lastMonitorStatus);
			} // end if

            if (agentId != null)
                serviceStatus.setAgentId(agentId);
			statusService.saveService(serviceStatus);
			
		} catch (Exception e) {
			String err = "CollageAdminAPI - updateServiceStatus failed while saving status for service '"
					+ serviceDescr + "' on Host '" + hostName + "'...";
			log.error(err, e);
			throw new CollageException(err, e);
		} finally {
			stopMetricsTimer(timer);
		}
		return serviceStatus;
	}

    /**
     * Warn and send not-owner service update log message event. This method
     * should be invoked and the update aborted when a service update is
     * attempted from a different application type than the service application
     * type.
     *
     * @param service updating service
     * @param applicationType application type
     */
    private void sendNotOwnerServiceMessage(ServiceStatus service, String applicationType) {
		CollageTimer timer = startMetricsTimer();
        Host host = service.getHost();
        String message = "Cannot update service from different application: " + host.getHostName() + ":" +
                service.getServiceDescription() + " owned by " + service.getApplicationType().getName() +
                " update ignored from " + applicationType;
        log.warn(message);
        try {
            String deviceIdentification = host.getDevice().getIdentification();
            String severity = CollageSeverity.WARNING.name();
            Properties logMessageProperties = new Properties();
            logMessageProperties.put(LogMessage.EP_HOST_STATUS_ID, host.getHostStatus());
            logMessageProperties.put(LogMessage.EP_MONITOR_STATUS_NAME, "UNKNOWN");
            logMessageProperties.put(LogMessage.KEY_CONSOLIDATION, SYSTEM_CONSOLIDATION);
            updateLogMessage(DEFAULT_MONITOR_SERVER, service.getApplicationType().getName(), deviceIdentification,
                    severity, message, host.getDevice(), host, service, logMessageProperties);
        } catch (Exception e) {
            log.error("Cannot log update service message: " + e, e);
        }
        stopMetricsTimer(timer);
    }

    protected void updateAdditionalServiceStatusProperties(ServiceStatus serviceStatus, Map<String,String> properties,
                                                           String lastMonitorStatus, String monitorStatus, boolean newService) {
        CollageTimer timer = startMetricsTimer();
        String stateType = properties.get(CollageAdminInfrastructure.PROP_STATE_TYPE);
        if (stateType != null) {
            StateType st = metadataService.getStateTypeByName(stateType);
            if (st != null)
                serviceStatus.setStateType(st);
        }
        properties.remove(CollageAdminInfrastructure.PROP_STATE_TYPE);
        if (serviceStatus.getStateType() == null) {
            serviceStatus.setStateType(metadataService.getStateTypeByName("UNKNOWN"));
        }
        String checkType = properties.get(CollageAdminInfrastructure.PROP_CHECK_TYPE);
        if (checkType != null) {
            CheckType ct = metadataService.getCheckTypeByName(checkType);
            if (ct != null)
                serviceStatus.setCheckType(ct);
        }
        properties.remove(CollageAdminInfrastructure.PROP_CHECK_TYPE);
        if (serviceStatus.getCheckType() == null) {
            serviceStatus.setCheckType(metadataService.getCheckTypeByName("ACTIVE"));
        }
        String lastHardState = properties.get(CollageAdminInfrastructure.PROP_LAST_HARD_STATE);
        if ((lastHardState == null) && (serviceStatus.getLastHardState() == null)) {
            lastHardState = monitorStatus;
        }
        if (lastHardState != null) {
            MonitorStatus mt = metadataService.getMonitorStatusByName(lastHardState);
            if (mt != null)
                serviceStatus.setLastHardState(mt);
        }
        properties.remove(CollageAdminInfrastructure.PROP_LAST_HARD_STATE);
        String domain = properties.get(CollageAdminInfrastructure.PROP_SERVICE_DOMAIN);
        if (domain != null) {
            serviceStatus.setDomain(domain);
        }
        properties.remove(CollageAdminInfrastructure.PROP_SERVICE_DOMAIN);
        String metricType = properties.get(CollageAdminInfrastructure.PROP_SERVICE_METRIC_TYPE);
        if (metricType != null) {
            serviceStatus.setMetricType(metricType);
        }
        properties.remove(CollageAdminInfrastructure.PROP_SERVICE_METRIC_TYPE);

        String lastCheckTime = properties.get(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME);
        if (lastCheckTime != null) {
            serviceStatus.setLastCheckTime(parseDate(lastCheckTime));
        }
        properties.remove(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME);
        String nextCheckTime = properties.get(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME);
        if (nextCheckTime != null) {
            serviceStatus.setNextCheckTime(parseDate(nextCheckTime));
        }
        properties.remove(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME);
        // set last state change if provided and state changed
        String lastStateChange = properties.get(CollageAdminInfrastructure.PROP_SERVICE_LAST_STATE_CHANGE);
        if ((lastStateChange != null) &&
                (newService || (lastMonitorStatus == null) || !lastMonitorStatus.equals(monitorStatus))) {
            serviceStatus.setLastStateChange(parseDate(lastStateChange));
            // Reset the service acknowledgement as this represents a state change
			properties.put(CollageAdminInfrastructure.PROP_IS_PROB_ACKNOWLEDGED, String.valueOf(false));
			properties.remove(CollageAdminInfrastructure.PROP_ACKNOWLEDGED_BY);
			properties.remove(CollageAdminInfrastructure.PROP_ACKNOWLEDGE_COMMENT);
        }
        properties.remove(CollageAdminInfrastructure.PROP_SERVICE_LAST_STATE_CHANGE);
        stopMetricsTimer(timer);
    }

    private Date parseDate(String date) {
        Date result = DateTime.parse(date);
        if (date == null)
            return new Date();
        return result;
    }

	/* bulk implementation */
	public void updateServiceStatus(String monitorServerName,
			String applicationType, String hostName, String deviceIdent,
			Collection<Map> serviceStatuses) throws CollageException {
		CollageTimer timer = startMetricsTimer();
		if (log.isDebugEnabled())
			log.debug("attempting to update the status of "
					+ serviceStatuses.size() + " services on Host '" + hostName
					+ "'...");

		if (serviceStatuses == null || serviceStatuses.size() == 0) {
			log
					.error("updateServiceStatus() - Invalid null / empty service status collection parameter.");
			throw new IllegalArgumentException(
					"Invalid null / empty service status collection parameter.");
		}

		ApplicationType appType = metadataService
				.getApplicationTypeByName(applicationType);
		if (appType == null)
			appType = metadataService
					.getApplicationTypeByName(DEFAULT_APP_TYPE);

		String serviceDescr = null;
		Host host = hostService.getHostByHostName(hostName);
		if (host == null) host = this.getOrCreateHost(appType, monitorServerName, hostName, deviceIdent, null);
		for (Map properties : serviceStatuses) {


			try {
				serviceDescr = (String) properties
						.get(ServiceStatus.EP_SERVICE_DESCRIPTION);
				if (serviceDescr == null) {
					log.warn("CollageAdminAPI - attempting to update ServiceStatus with null ServiceDescription on host '" + hostName + "' - '" + deviceIdent + "'");
					continue;
				}

				if (log.isDebugEnabled())
					log.debug("attempting to update ServiceStatus for service '" + serviceDescr + "' on Host '" + hostName + "'...");

				if (_collage.isAutoCreateUnknownProperties())
					adminMeta.createOrAssignUnknownProperties(applicationType,
							ServiceStatus.ENTITY_TYPE_CODE, properties);

                ServiceStatus serviceStatus = host.getServiceStatus(serviceDescr);
				if (serviceStatus == null) {
					// No need to query for host again if it has been created
					// previously
					if (host == null)
						host = this.getOrCreateHost(appType, monitorServerName,
								hostName, deviceIdent, null);

					serviceStatus = statusService.createService(serviceDescr,
							applicationType, host);

                    // save application host name if different than host name
                    if (!host.getHostName().equalsIgnoreCase(hostName)) {
                        serviceStatus.setApplicationHostName(hostName);
                    }
				} else {
                    // check owning application type
                    boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                            (serviceStatus.getApplicationType() == null) ||
                            applicationType.equals(serviceStatus.getApplicationType().getName()));
                    // validate owner application type updates
                    if (!owner) {
                        sendNotOwnerServiceMessage(serviceStatus, applicationType);
                        return;
                    }
                }

				serviceStatus.setProperties(properties);
				statusService.saveService(serviceStatus);

				if (log.isDebugEnabled())
					log.debug("updated ServiceStatus for service '"
							+ serviceDescr + "' on Host '" + hostName + "'");
			} // If an error occurs we log and continue to process the next
			catch (Exception e) {
				String err = "CollageAdminAPI - updateServiceStatus failed while saving status for service '"
						+ serviceDescr + "' on Host '" + hostName + "'...";
				log.error(err, e);
			}
		} // end for

		if (log.isDebugEnabled()) {
			log.debug("updated the status of " + serviceStatuses.size()
					+ " services on Host '" + hostName + "'");
		}
		stopMetricsTimer(timer);
	}

	public void updateServiceStatus(String monitorServerName, String host,
			String deviceIdent, String serviceDescription,
			String lastPluginOutput, String monitorStatus, String retryNumber,
			String stateType, String lastCheckTime, String nextCheckTime,
			String checkType, String isChecksEnabled,
			String isAcceptPassiveChecks, String isEventHandlersEnabled,
			String lastStateChange, String isProblemAcknowledged,
			String lastHardState, String timeOK, String timeUnknown,
			String timeWarning, String timeCritical,
			String lastNotificationTime, String currentNotificationNumber,
			String isNotificationsEnabled, String latency,
			String executionTime, String isFlapDetectionEnabled,
			String isServiceFlapping, String percentStateChange,
			String scheduledDowntimeDepth, String isFailurePredictionEnabled,
			String isProcessPerformanceData, String isObsessOverService, String PerformanceData)
			throws CollageException {
		if (log.isDebugEnabled()) {
			log.debug("Before updating the service status monitor_status="
					+ monitorStatus + ", scheduledDowntimeDepth="
					+ scheduledDowntimeDepth);
		}

		Properties properties = Nagios.createServiceStatusProps(
				serviceDescription, lastPluginOutput, monitorStatus,
				retryNumber, stateType, lastCheckTime, nextCheckTime,
				checkType, isChecksEnabled, isAcceptPassiveChecks,
				isEventHandlersEnabled, lastStateChange, isProblemAcknowledged,
				lastHardState, timeOK, timeUnknown, timeWarning, timeCritical,
				lastNotificationTime, currentNotificationNumber,
				isNotificationsEnabled, latency, executionTime,
				isFlapDetectionEnabled, isServiceFlapping, percentStateChange,
				scheduledDowntimeDepth, isFailurePredictionEnabled,
				isProcessPerformanceData, isObsessOverService, PerformanceData);

		this.updateServiceStatus(monitorServerName, Nagios.APPLICATION_TYPE,
				host, deviceIdent, (String)null, properties);
	}

	/*
	 * This method records the state of a logical Host (xyz.domain.com) or
	 * possibly an application (xyz.domain.com/webapp) at a given point in time;
	 * see the parameter docs of {@link
	 * #updateHostStatus(String,String,String,String,Map)} for more
	 */
	public void updateHostStatus(String monitorServerName,
			String applicationType, String hostName, String deviceIdent,
			Map properties) {
		CollageTimer timer = startMetricsTimer();
		if (hostName == null || hostName.length() == 0) {
			String msg = "CollageAdminAPI - attempting to update HostStatus with null / empty hostName on Device '"
					+ deviceIdent + "'";
			log.error(msg);
			throw new CollageException(msg);
		}

		if (log.isDebugEnabled()) {
			log.debug("attempting to update HostStatus for Host '" + hostName
					+ " on Device'" + deviceIdent + "'...");
		}

		ApplicationType appType = metadataService
				.getApplicationTypeByName(applicationType);
		if (appType == null)
			appType = metadataService
					.getApplicationTypeByName(DEFAULT_APP_TYPE);

		if (_collage.isAutoCreateUnknownProperties())
			adminMeta.createOrAssignUnknownProperties(applicationType,
					HostStatus.ENTITY_TYPE_CODE, properties);

		try {
			Host host = this.getOrCreateHost(appType, monitorServerName,
					hostName, deviceIdent, null);
			HostStatus hostStatus = host.getHostStatus();

			if (hostStatus != null) {
                // check owning application type
                boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                        (host.getApplicationType() == null) || applicationType.equals(host.getApplicationType().getName()));
                if (owner) {
                    // merge last plugin output when owner
                    String lastPluginOutput = (String)properties.remove(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
                    updateOwnerHostStatusMessage(host, lastPluginOutput);
                    hostStatus.setProperties(properties);
                } else {
                    // update only status message if not owner
                    String monitorStatus = (String)properties.remove(CollageAdminInfrastructure.PROP_MONITOR_STATUS);
                    String lastPluginOutput = (String)properties.remove(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
                    updateNotOwnerHostStatusMessage(host, applicationType, monitorStatus, lastPluginOutput);
                }
            }

			hostService.saveHost(host);
		} catch (Exception e) {
			String msg = "CollageAdmin API: unable to updateHostStatus";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}

		if (log.isDebugEnabled())
			log.debug("updated HostStatus of Host, " + hostName);
		stopMetricsTimer(timer);
	}

	public void updateHostStatus(String monitorServerName, String host,
			String deviceIdent, String lastPluginOutput, String monitorStatus,
			String lastCheckTime, String lastStateChange,
			String isAcknowledged, String timeUp, String timeDown,
			String timeUnreachable, String lastNotificationTime,
			String currentNotificationNumber, String isNotificationsEnabled,
			String isChecksEnabled, String isEventHandlersEnabled,
			String isFlapDetectionEnabled, String isHostFlapping,
			String percentStateChange, String scheduledDowntimeDepth,
			String isFailurePredictionEnabled, String isProcessPerformanceData,
			String CheckType, String Latency, String ExecutionTime,
			String isPassiveChecksEnabled,String PerformanceData) throws CollageException {

		Properties properties = Nagios.createHostStatusProps(lastPluginOutput,
				monitorStatus, lastCheckTime, lastStateChange, isAcknowledged,
				timeUp, timeDown, timeUnreachable, lastNotificationTime,
				currentNotificationNumber, isNotificationsEnabled,
				isChecksEnabled, isEventHandlersEnabled,
				isFlapDetectionEnabled, isHostFlapping, percentStateChange,
				scheduledDowntimeDepth, isFailurePredictionEnabled,
				isProcessPerformanceData, CheckType, Latency, ExecutionTime,
				isPassiveChecksEnabled, PerformanceData);

		this.updateHostStatus(monitorServerName, Nagios.APPLICATION_TYPE, host,
				deviceIdent, properties);
	}

    public void resetHostStatusMessage(String hostName) throws CollageException {
		CollageTimer timer = startMetricsTimer();
        // validate host name
        if (hostName == null || hostName.length() == 0) {
            String msg = "CollageAdminAPI - attempting reset HostStatus message with null / empty hostName";
            log.error(msg);
            throw new CollageException(msg);
        }
        try {
            // lookup host status
            Host host = hostIdentityService.getHostByIdOrHostName(hostName);
            if (host == null) {
                log.error("resetHostStatusMessage. Host [" + hostName + "] doesn't exist.");
                throw new CollageException("CollageAdmin API. resetHostStatusMessage. Host ["  + hostName + "] doesn't exist.");
            }
            HostStatus hostStatus = host.getHostStatus();
            if (hostStatus != null) {
                // reset status message, (last plugin output)
                hostStatus.setProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, null);
                hostService.saveHost(host);
            }
        } catch (CollageException ce) {
            throw ce;
        } catch (Exception e) {
            String msg = "CollageAdmin API: unable to resetHostStatusMessage";
            log.error(msg, e);
            throw new CollageException(msg, e);
        }
        stopMetricsTimer(timer);
    }

    public LogMessage updateLogMessage(String monitorServerName,
                                       String applicationType, String deviceIdent, String severity,
                                       String textMessage, Map properties) {
        return updateLogMessage(monitorServerName, applicationType, deviceIdent, severity, textMessage,
                null, null, null, properties);
    }

	public LogMessage updateLogMessage(String monitorServerName,
                                       String applicationType, String deviceIdent, String severity,
                                       String textMessage, Device device, Host host, ServiceStatus serviceStatus,
                                       Map properties) {
	    CollageTimer timer = startMetricsTimer();
		if (log.isDebugEnabled()) {
			log.debug("Starting update log message: " + monitorServerName);
		}
		if (applicationType == null || applicationType.length() == 0) {
			String msg = "CollageAdminAPI - attempting to update LogMessage with null ApplicationType name";
			log.error(msg);
			throw new CollageException(msg);
		}

		if (monitorServerName == null || monitorServerName.length() == 0) {
			String msg = "CollageAdminAPI - attempting to update LogMessage with null MonitorServer name";
			log.error(msg);
			throw new CollageException(msg);
		}

        if (((deviceIdent == null) || (deviceIdent.length() == 0)) && (device != null)) {
            deviceIdent = device.getIdentification();
        }
		if (deviceIdent == null || deviceIdent.length() == 0) {
			String msg = "CollageAdminAPI - attempting to update LogMessage with null Device name";
			log.error(msg);
			throw new CollageException(msg);
		}

		/* Init For consolidation values */
		int consolidationHash = 0;
		int statelessHash = 0;
		String cacheKey = properties.get(LogMessage.EP_HOST_NAME) + ";" + properties.get(LogMessage.EP_SERVICE_STATUS_DESCRIPTION);

		/*
		 * If OperationStatus is not defined add the default ("OPEN") so that
		 * the consolidation rules apply.
		 */
		String operationStatusValue = (String) properties.get(LogMessage.EP_OPERATION_STATUS_NAME);
		if (operationStatusValue == null) {
			if (log.isDebugEnabled()) log.debug("Added default OperationStatus 'OPEN' to incoming message.");
			properties.put(LogMessage.EP_OPERATION_STATUS_NAME, OP_STATUS_OPEN);
		}

		// Check if Consolidation is enabled
		String consolidationName = null;
		if (properties.containsKey(LogMessage.KEY_CONSOLIDATION)) {
			consolidationName = (String) properties.get(LogMessage.KEY_CONSOLIDATION);

			// Add Properties To Be Consolidated - These properties will be
			// removed before persisting
			if (monitorServerName != null && monitorServerName.length() > 0) {
				properties.put(LogMessage.KEY_MONITOR_SERVER, monitorServerName);
			}

			if (deviceIdent != null && deviceIdent.length() > 0) {
				properties.put(LogMessage.EP_DEVICE_IDENTIFICATION, deviceIdent);
			}

			if (severity != null && severity.length() > 0) {
				properties.put(LogMessage.EP_SEVERITY_NAME, severity);
			}

			/**
			 * TextMessage might be part of the consolidation criteria and
			 * therefore it needs to be added to the properties if it doesn't
			 * exist. Some of the feeders (snmp, syslog) already add the
			 * property
			 */
			String textMessageValue = (String) properties.get(LogMessage.EP_TEXT_MESSAGE);
			if (textMessageValue == null || textMessageValue.length() == 0) {
				properties.put(LogMessage.EP_TEXT_MESSAGE, textMessage);
			}

			// Remove the property so that it doesn't get added
			properties.remove(LogMessage.KEY_CONSOLIDATION);

			// log4j logging
			if (log.isDebugEnabled()) log.debug("Use Consolidation for message [" + consolidationName + "]");
		}

		if (log.isDebugEnabled()) log.debug("attempting to update LogMessage for Device'" + deviceIdent + "'...");

		if (_collage.isAutoCreateUnknownProperties()) {
			adminMeta.createOrAssignUnknownProperties(applicationType, LogMessage.ENTITY_TYPE_CODE, properties);
		}

		String hostName = (String) properties.remove(LogMessage.EP_HOST_NAME);
		if (StringUtils.isBlank(hostName) && (host != null)) {
			hostName = host.getHostName();
		}

		String serviceDescr = (String) properties.remove(LogMessage.EP_SERVICE_STATUS_DESCRIPTION);
        if (((serviceDescr == null) || (serviceDescr.length() == 0)) && (serviceStatus != null)) {
            serviceDescr = serviceStatus.getServiceDescription();
        }

		Date reportDate = DateTime.parse((String) properties.remove(LogMessage.EP_REPORT_DATE));

		// Check if report date us empty or 0. In this case set it to current
		// date.
		// This addresses the issue when Report Date is not provided by Feeder
		// and shows up as 12/31/1969 in UI (GWMON-4523)
		if (reportDate == null) reportDate = new Date();

		LogMessage msg = null;

		try {
			// Check if consolidation criteria matches
			if (consolidationName != null) {
				// Calculate the hash
				consolidationHash = consolidationService.getConsolidationHash(properties, consolidationName, "");

                Integer cachedHash = hashCache.get(cacheKey);
                if (cachedHash != null && consolidationHash == cachedHash) {
                    LogMessage cachedMsg = getLogMessageFromCachedId(cacheKey);
                    if (cachedMsg != null) {
						if (!cachedMsg.getStateChanged()) {
							msg = cachedMsg;
                        }
                    }
                }

				if (msg == null) {
					/*
					 * No consolidation a new entry will be created. Check all
					 * previous entries as StateChanged
					 */
					statelessHash = consolidationService.getConsolidationHash(properties, consolidationName, PROP_MONITOR_STATUS);

					// Remove consolidation specific properties that were added
					// previously
					properties.remove(LogMessage.KEY_MONITOR_SERVER);
					properties.remove(LogMessage.EP_DEVICE_IDENTIFICATION);
					properties.remove(LogMessage.EP_SEVERITY_NAME);
					properties.remove(LogMessage.EP_TEXT_MESSAGE);
				} else {
					/*
					 * Apply update rules If the consolidation criteria matches
					 * the message counter for an existing message will
					 * increased and the date fields will be updated as
					 * following: Field change FirstInserDate unchanged
					 * LastInsertDate ReportDate ReportDate System (current
					 * time)
					 */

					msg.setLastInsertDate(reportDate);
					msg.setReportDate(new Date());

					// Increment message counter
					msg.setMsgCount(msg.getMsgCount() + 1);

					// JIRA GWMON-3492 -- Consolidation should update text
					// messages as well
					msg.setTextMessage(textMessage);

					// JIRA 9781
					Object consAllFieldsObj = configuration.getProperty("consolidate.all.fields");
					boolean consAllFields = new Boolean((String) consAllFieldsObj);
					if (consAllFields) {
						Severity severityObj = metadataService.getSeverityByName(severity);
						msg.setSeverity(severityObj);
						properties.remove(LogMessage.KEY_MONITOR_SERVER);
						properties.remove(LogMessage.EP_DEVICE_IDENTIFICATION);
						properties.remove(LogMessage.EP_SEVERITY_NAME);
						properties.remove(LogMessage.EP_TEXT_MESSAGE);
						msg.setProperties(properties);
					} // end if
				}
			}

			// If msg is null it means that no consolidation could be applied or consolidation is not configured.
			// Create new message entry
			if (msg == null) {

				// Fix for JIRA 9073
				
				Object objEventPreProcessSwitch = configuration.get(LogMessage.EVENT_PRE_PROCESS_SWITCH);
				Object objEventPreProcessDestState = configuration.get(LogMessage.EVENT_PRE_PROCESS_DEST_STATE);
				if (objEventPreProcessSwitch != null && objEventPreProcessDestState != null) {
					boolean isEventPreProcessEnabled = Boolean.parseBoolean(((String) objEventPreProcessSwitch).trim());
					String eventPreProcessDestState = ((String) objEventPreProcessDestState) .trim();
					if (isEventPreProcessEnabled && applicationType.equalsIgnoreCase(NAGIOS)) {
						log.debug("NAGIOS preprocessing is enabled and now preprocessing the NAGIOS appType");
						preProcessEvents(hostName, serviceDescr, eventPreProcessDestState);
					}
				}

				// Now pre process the events for the apptype
				log.debug("Now preprocessing the appType : " + applicationType );
				Map<String, String> builtInProperties = new HashMap<>();
				builtInProperties.put(LogMessage.KEY_MONITOR_SERVER, monitorServerName);
				builtInProperties.put(LogMessage.EP_DEVICE_IDENTIFICATION, deviceIdent);
				builtInProperties.put(LogMessage.EP_SEVERITY_NAME, severity);
				builtInProperties.put(LogMessage.EP_TEXT_MESSAGE, textMessage);
				builtInProperties.put(LogMessage.EP_REPORT_DATE, reportDate.toString());
				preProcessEventsByAppType(applicationType, configuration, builtInProperties, properties);

				msg = logMsgService.createLogMessage();

                // Ensure that we only use a host if possible.  Not having a host to use implies that this is a system
				// event which should not result in failures or warnings.

				if ((host == null) && StringUtils.isNotBlank(hostName)) {
					host = hostIdentityService.getHostByIdOrHostName(hostName);
				}

				if (host != null) {
					msg.setDevice(host.getDevice());
					msg.setHostStatus(host.getHostStatus());

					// GWRK-373 The service description can be empty. Don't do a lookup
					if ((serviceStatus == null) && StringUtils.isNotBlank(serviceDescr)) {
						serviceStatus = statusService.getServiceByDescription(serviceDescr, host.getHostName());
					}
					msg.setServiceStatus(serviceStatus);
				} else if (device != null) {
					msg.setDevice(device);
				} else {
					msg.setDevice(this.getOrCreateDevice(monitorServerName, deviceIdent));
				}

				msg.setApplicationType(metadataService.getApplicationTypeByName(applicationType));

				// Consolidation Hash values
				msg.setConsolidationHash(consolidationHash);
				msg.setStatelessHash(statelessHash);

				Severity severityObj = metadataService.getSeverityByName(severity);
				msg.setSeverity(severityObj);
				msg.setTextMessage(textMessage);
				msg.setApplicationSeverity(severityObj);

				/*
				 * Property Values Action FirstInsertDate LastInsertDate
				 * --------
				 * ------------------------------------------------------
				 * ------------------------------ date null FirstInsertDate =
				 * date, LastInsertDate = ReportDate null null Set both Fields
				 * to Report Date date date Set both fields to Date null date
				 * Set FirstInsert Date to LastInsertDate
				 * ------------------------
				 * --------------------------------------
				 * -------------------------------
				 */

				String lastInsertDate = (String) properties
						.remove(LogMessage.EP_LAST_INSERT_DATE);
				String firstInsertDate = (String) properties
						.remove(LogMessage.EP_FIRST_INSERT_DATE);

				if ((firstInsertDate == null || firstInsertDate.length() == 0)
						&& (lastInsertDate == null || lastInsertDate.length() == 0)) {
					msg.setLastInsertDate(reportDate);
					msg.setFirstInsertDate(reportDate);
				} else if ((lastInsertDate == null || lastInsertDate.length() == 0)
						&& (firstInsertDate != null)) {
					msg.setLastInsertDate(reportDate);
					msg.setFirstInsertDate(DateTime.parse(firstInsertDate));
				} else if ((lastInsertDate != null)
						&& (firstInsertDate != null)) {
					msg.setLastInsertDate(DateTime.parse(lastInsertDate));
					msg.setFirstInsertDate(DateTime.parse(firstInsertDate));
				} else if ((lastInsertDate != null)
						&& (firstInsertDate == null || firstInsertDate.length() == 0)) {
					msg.setLastInsertDate(DateTime.parse(lastInsertDate));
					msg.setFirstInsertDate(DateTime.parse(lastInsertDate));
				} else {
					// Should not run into this case
					log.warn("Event Feeder LastInsertDate/FirstInsertDate combination mismatch. Set it to default");
					msg.setLastInsertDate(reportDate);
					msg.setFirstInsertDate(reportDate);
				}

				/* Set Reporting Date to current date */
				if (reportDate != null) {
					// http://jira/browse/GWMON-13060
					msg.setReportDate(reportDate);
				}
				else {
					msg.setReportDate(new Date());
				}

				// TODO Component -- needs to be defined
				msg.setComponent(metadataService.getComponentByName(UNDEFINED));

				// TODO Type -- needs to be defined
				msg.setTypeRule(metadataService.getTypeRuleByName(UNDEFINED));

				// TODO Priority -- needs to be defined
				msg.setPriority(metadataService.getPriorityByName(PRIORITY_1));

				// TODO OperationStatus -- needs to be defined
				msg.setOperationStatus(metadataService.getOperationStatusByName(OP_STATUS_OPEN));

				/* set remaining properties */
				msg.setProperties(properties);

				// Determine if this is a state transition based on our message cache
				LogMessage cachedMsg = getLogMessageFromCachedId(cacheKey);
				if (cachedMsg != null && !(cachedMsg.getMonitorStatus().equals(msg.getMonitorStatus()))) {
				    // Pre-populate the state transition hash to ensure that log message service does not need to
					// do this
					Integer stateTransitionHash = logMsgService.buildStateTransitionHash(msg);
					msg.setStateTransitionHash(stateTransitionHash);
				}
			}

			/* validate properties */
			if (msg.getApplicationType() == null | msg.getSeverity() == null || msg.getApplicationSeverity() == null ||
					msg.getPriority() == null || msg.getTypeRule() == null || msg.getComponent() == null ||
					msg.getOperationStatus() == null) {
				throw new IllegalArgumentException("Missing or invalid required LogMessage fields");
			}

			// Update existing or add new one
			logMsgService.saveLogMessage(msg);

            // save updated log message in cache
			hashCache.put(cacheKey, consolidationHash);
			messageIdCache.put(cacheKey, msg.getLogMessageId());
		} catch (Exception e) {
			String text = "CollageAdmin API: unable to updateLogMessage";
			log.error(text, e);
			throw new CollageException(text, e);
		}

		stopMetricsTimer(timer);
		return msg;
	}

	/**
	 * Removes all message caching for a given host/service
	 *
	 * @param host
	 * @param service
	 * @return
	 */
	public void clearMessageCache(String host, String service) {
		String key = host + ";" + service;
		hashCache.remove(key);
		messageIdCache.remove(key);
	}

	/**
	 * Builds the criteria for the preProcessEventsByAppType
	 * 
	 * @param preProcessFields
	 * @param builtInProperties
	 * @param dynaProperties
	 * @return
	 */
	private FilterCriteria buildCriteria(String preProcessFields,
			Map<String, String> builtInProperties,
			Map<String, String> dynaProperties) {
		FilterCriteria criteria = null;

		// EntityProperty to Hibernate Property mappings
		Map<String, String> hibMap = new HashMap<String, String>();
		hibMap.put(LogMessage.EP_DEVICE_IDENTIFICATION,
				LogMessage.HP_DEVICE_IDENTIFICATION);
		hibMap.put(LogMessage.EP_APP_SEVERITY_NAME,
				LogMessage.HP_APP_SEVERITY_NAME);
		hibMap.put(LogMessage.EP_TEXT_MESSAGE, LogMessage.HP_TEXT_MESSAGE);
		hibMap.put(LogMessage.EP_OPERATION_STATUS_NAME,
				LogMessage.HP_OPERATION_STATUS_NAME);
		hibMap.put(LogMessage.EP_REPORT_DATE, LogMessage.HP_REPORT_DATE);
		hibMap.put(LogMessage.EP_MONITOR_STATUS_NAME,
				LogMessage.HP_MONITOR_STATUS_NAME);

		StringTokenizer stkn = new StringTokenizer(preProcessFields, ",");
		int criteriaCount = stkn.countTokens();
		int matchCount = 0;
		int dynamicPropCount = 0;
		while (stkn.hasMoreTokens()) {
			String field = stkn.nextToken().trim();
			if (builtInProperties != null
					&& builtInProperties.containsKey(field)) {
				matchCount++;
				if (criteria == null)
					criteria = FilterCriteria.eq(hibMap.get(field),
							builtInProperties.get(field));
				else
					criteria.and(FilterCriteria.eq(hibMap.get(field),
							builtInProperties.get(field)));
			} // end if
			if (dynaProperties != null && dynaProperties.containsKey(field)) {
				matchCount++;
				// An exception for monitorstatus name as this field comes as
				// dynamic properties.
				if (field.equals(LogMessage.EP_MONITOR_STATUS_NAME)) {
					if (criteria == null)
						criteria = FilterCriteria.eq(field, builtInProperties
								.get(field));
					else
						criteria.and(FilterCriteria.eq(field, builtInProperties
								.get(field)));
				} // end if
				else {
					if (criteria == null) {
						criteria = FilterCriteria.eq("propertyValues.name"+ "_" + dynamicPropCount,
								field);
						criteria.and(FilterCriteria.eq(
								"propertyValues.valueString"+ "_" + dynamicPropCount, dynaProperties
										.get(field)));
					} else {
						criteria.and(FilterCriteria.eq(
								"propertyValues.name"+ "_" + dynamicPropCount, field));
						criteria.and(FilterCriteria.eq(
								"propertyValues.valueString"+ "_" + dynamicPropCount, dynaProperties
										.get(field)));
					} // end if
				} // end if
				dynamicPropCount++;
			} // end if
		} // end while
		if (matchCount == criteriaCount) {
			// if all the fields matches then append the open criteria
			criteria.and(FilterCriteria.eq(LogMessage.HP_OPERATION_STATUS_NAME,
					"OPEN"));
			return criteria;
		} // end if
		return null;
	}

	/**
	 * Preprocess the event by application type.
	 * 
	 * @param applicationType
	 * @param configuration
	 * @param builtInProperties
	 * @param dynaProperties
	 */
	private void preProcessEventsByAppType(String applicationType,
			Properties configuration, Map<String, String> builtInProperties,
			Map<String, String> dynaProperties) {
		String EVENT_PRE_PROCESS = "event.pre.process.";
		String eventPreprocessPropName = EVENT_PRE_PROCESS + applicationType
				+ ".enabled";
		boolean appTypePreprocessSwitch = new Boolean(configuration
				.getProperty(eventPreprocessPropName));
		if (appTypePreprocessSwitch) {
			log.debug("Preprocess switch for appType " + applicationType + " is enabled..Now processing..");
			String preProcessFields = configuration
					.getProperty(EVENT_PRE_PROCESS + applicationType
							+ ".fields");
			String destOpStatus = configuration.getProperty(EVENT_PRE_PROCESS
					+ applicationType + ".opstatus");
			// Ignore Auto acknowledge GWMON-11422
			if (preProcessFields != null && !destOpStatus.equalsIgnoreCase(OP_STATUS_ACKNOWLEDGED)) {
				FilterCriteria criteria = this.buildCriteria(preProcessFields,
						builtInProperties, dynaProperties);
				if (criteria != null) {
					log.debug("Criteria is : "   + criteria.getPropertyValuePairs());
					List<LogMessage> events = logMsgService.getLogMessagesByCriteria(criteria);
					if (events != null) {
						log.debug("No of matching events : " + events.size());
						for (LogMessage event : events) {
							event.setOperationStatus(metadataService
									.getOperationStatusByName(destOpStatus));
							logMsgService.saveLogMessage(event);
						} // end for
					} // end if

				} else {
					// Create a new log message with a warning
				}
			} // end if
		}

	}

	/**
	 * Preprocesses the NAGIOS events based on the swtich set in the
	 * foundation.properties
	 * 
	 * @param hostName
	 * @param serviceDescr
	 * @param destOpStatus
	 */
	private void preProcessEvents(String hostName, String serviceDescr,
			String destOpStatus) {
		// Ignore Auto acknowledge GWMON-11422
		if (hostName != null && destOpStatus != null && !destOpStatus.equalsIgnoreCase(OP_STATUS_ACKNOWLEDGED)) {
			FoundationQueryList list = null;
			if (serviceDescr != null && !serviceDescr.equalsIgnoreCase("")) {
				list = logMsgService.getLogMessagesByService(hostName,
						serviceDescr, null, null, FilterCriteria.eq(
								LogMessage.HP_OPERATION_STATUS_NAME,
								OP_STATUS_OPEN), null, -1, -1);
			} else {
				list = logMsgService.getLogMessagesByHostName(hostName, null,
						null, FilterCriteria.eq(
								LogMessage.HP_OPERATION_STATUS_NAME,
								OP_STATUS_OPEN), null, -1, -1);
			}
			List<LogMessage> events = list.getResults();
			if (events != null) {
				for (LogMessage event : events) {
					event.setOperationStatus(metadataService
							.getOperationStatusByName(destOpStatus));
					logMsgService.saveLogMessage(event);
				} // end for
			} // end if
		} // end if
	}

	public LogMessage updateLogMessage(String consolidationCriteria,
			String logType, String monitorServerName, String hostName,
			String deviceIdent, String severity, String monitorStatus,
			String textMessage, String reportDate, String lastInsertDate,
			String subComponent, String errorType, String serviceDescription,
			String serviceStatus, String loggerName, String applicationName,
			String firstInserDate) throws CollageException {
		Properties properties = Nagios.createLogMessageProps(hostName,
				monitorStatus, reportDate, lastInsertDate, subComponent,
				errorType, serviceDescription, loggerName, applicationName,
				firstInserDate, textMessage);

		if (consolidationCriteria != null)
			properties.setProperty(LogMessage.KEY_CONSOLIDATION,
					consolidationCriteria);

		return this.updateLogMessage(monitorServerName,
				Nagios.APPLICATION_TYPE, deviceIdent, severity, textMessage,
				properties);
	}

	/*************************************************************************/
	/* SystemConfig methods */
	/*************************************************************************/

	/*
	 * Adds Devices to a MonitorServer; if a MonitorServer with the name
	 * provided does not exist, it is created.
	 */
	public void addDevicesToMonitorServer(String monitorServer,
			List<String> deviceList) throws CollageException {
		try {
			deviceService.addDevicesToMonitorServer(monitorServer, deviceList);
		} catch (Exception e) {
			String msg = "Error occurred in addDevicesToMonitorServer(), MonitorServer: "
					+ monitorServer + ", deviceList: " + deviceList;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * @see
	 * com.groundwork.collage.CollageAdmin#addDevicesToChildDevice(java.lang
	 * .String, java.lang.String)
	 */
	public void addDevicesToChildDevice(String childDevice,
			List<String> deviceList) throws CollageException {
		try {
			deviceService.attachParentDevices(childDevice, deviceList);
		} catch (Exception e) {
			String msg = "Error occurred in addDevicesToChildDevice(), Child: "
					+ childDevice + ", deviceList: " + deviceList;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * @see
	 * com.groundwork.collage.CollageAdmin#addDevicesToParentDevice(java.lang
	 * .String, java.lang.String)
	 */
	public void addDevicesToParentDevice(String parentDevice,
			List<String> deviceList) throws CollageException {
		try {
			deviceService.attachChildDevices(parentDevice, deviceList);
		} catch (Exception e) {
			String msg = "Error occurred in addDevicesToParentDevice(), Parent: "
					+ parentDevice + ", deviceList: " + deviceList;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/* Original entry point with no description and alias fields */
	public HostGroup addHostsToHostGroup(String applicationType,
			String hostGroupName, List<String> hostList)
			throws CollageException {
		return addHostsToHostGroup(applicationType, hostGroupName, hostList,
				null, null);
	}

	/* GWMEE 6.1 added description field for Host Group */
	public HostGroup addHostsToHostGroup(String applicationType,
			String hostGroupName, List<String> hostList, String description)
			throws CollageException {
		return addHostsToHostGroup(applicationType, hostGroupName, hostList,
				description, null);
	}

	/*
	 * GWMEE 6.4 started to support Alias field as well (non-Javadoc)
	 * 
	 * @see com.groundwork.collage.CollageAdmin#sToHostGroup(java.lang.String,
	 * java.lang.String)
	 */
	public HostGroup addHostsToHostGroup(String applicationType,
			String hostGroupName, List<String> hostList, String description,
			String alias) throws CollageException {
		log.debug("Collage Admin.addHostsToHostGroup - applicationType ["
				+ applicationType + "]  hostGroupName [" + hostGroupName + "]");
		if (hostGroupName == null || hostGroupName.length() == 0) {
			log.error("addHostsToHostGroup. HostGroup Name not defined!");
			throw new CollageException(
					"CollageAdmin API. addHostsToHostGroup. HostGroup Name not defined!");
		}

		ApplicationType appType = null;

		if (applicationType != null && applicationType.length() > 0)
			appType = metadataService.getApplicationTypeByName(applicationType);

		if (appType == null)
			appType = metadataService
					.getApplicationTypeByName(DEFAULT_APP_TYPE);

		/*
		 * Check if HostGroup exists If HostGroup exists hosts will be added
		 * otherwise the hostgroup will be created
		 */
		HostGroup hg = hgService.getHostGroupByName(hostGroupName);

		if (hg == null) {
			try {
				// create new hostgroup
				hg = hgService.createHostGroup();
				hg.setName(hostGroupName);
				hg.setApplicationType(appType);
				if (description != null)
					hg.setDescription(description.isEmpty() ? null : description);
				if (alias != null)
					hg.setAlias(alias.isEmpty() ? null : alias);

				// Save host group before updating each host
				hgService.saveHostGroup(hg);
			} catch (Exception e) {
				log.error("addHostsToHostGroup. Failed to create Hostgroup ["
						+ hostGroupName + "].", e);
				throw new CollageException(
						"CollageAdmin API. addHostsToHostGroup. Failed to create Hostgroup ["
								+ hostGroupName + "].", e);
			}
		}

		// Nothing to do if host list is empty
		if (hostList == null || hostList.size() == 0) {
			if (hg != null)
				return hg;
			else
				return null;
		} // end if

		// Add hostgroup to hosts - For a large hostgroups, it is faster to add
		// a hostgroup to a host than a host to hostgroups because
		// of the need to load all hosts for the hostgroup.
		Collection<Host> hosts = hostIdentityService.getHostsByIdOrHostNames(hostList);

		Iterator<Host> it = hosts.iterator();
		Host host = null;
		while (it.hasNext()) {
			host = it.next();

			// Add host group to host
			host.getHostGroups().add(hg);
		}

		try {
			// Save and commit all hosts
			hostService.saveHost(hosts);
		} catch (Exception e) {
			log.error("addHostsToHostGroup. Failed adding Hosts [" + hostList
					+ "] to Hostgroup [" + hostGroupName + "]", e);
			throw new CollageException(
					"CollageAdmin API. addHostsToHostGroup. Failed adding Hosts ["
							+ hostList + "] to Hostgroup [" + hostGroupName
							+ "]" + e);
		}
		return hg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.groundwork.collage.CollageAdmin#sToHostGroup(java.lang.String,
	 * java.lang.String)
	 */
	/*
	 * public void addServicesToServiceGroup(String applicationType, String
	 * serviceGroupName, List<String> serviceList) throws CollageException { if
	 * (serviceGroupName == null || serviceGroupName.length() == 0 ) {
	 * log.error("addServicesToServiceGroup. ServiceGroup Name not defined!");
	 * throw newCollageException(
	 * "CollageAdmin API. addServicesToServiceGroup. ServiceGroup Name not defined!"
	 * ); }
	 * 
	 * ApplicationType appType = null;
	 * 
	 * if (applicationType != null && applicationType.length() > 0 ) appType =
	 * metadataService.getApplicationTypeByName(applicationType);
	 * 
	 * if (appType == null) appType =
	 * metadataService.getApplicationTypeByName(DEFAULT_APP_TYPE);
	 * 
	 * // Check if HostGroup exists // If HostGroup exists hosts will be added
	 * otherwise the hostgroup will be // created //
	 * 
	 * 
	 * ServiceGroup sg = sgService.getServiceGroupByName(serviceGroupName);
	 * 
	 * if (sg == null) { try { // create new hostgroup sg =
	 * sgService.createServiceGroup(); sg.setName(serviceGroupName);
	 * sg.setApplicationType(appType);
	 * 
	 * // Save host group before updating each host sgService.saveHostGroup(sg);
	 * } catch (Exception e) {
	 * log.error("addHostsToHostGroup. Failed to create Hostgroup [" +
	 * serviceGroupName + "].", e); throw newCollageException(
	 * "CollageAdmin API. addHostsToHostGroup. Failed to create Hostgroup [" +
	 * serviceGroupName +"].", e); } }
	 * 
	 * // Nothing to do if host list is empty if (serviceList == null ||
	 * serviceList.size() == 0) return;
	 * 
	 * // Add hostgroup to hosts - For a large hostgroups, it is faster to add a
	 * hostgroup to a host than a host to hostgroups because // of the need to
	 * load all hosts for the hostgroup. Collection<Service> services =
	 * ServiceService.getHosts(serviceList);
	 * 
	 * Iterator<Service> it = Services.iterator(); Service host = null; while
	 * (it.hasNext()) { service = (Service)it.next();
	 * 
	 * // Add host group to host service.getServiceGroups().add(sg); }
	 * 
	 * try { // Save and commit all hosts ServiceService.saveService(service); }
	 * catch (Exception e) {
	 * log.error("addHostsToHostGroup. Failed adding Hosts [" +serviceList
	 * +"] to Hostgroup [" + serviceGroupName + "]", e); throw new
	 * CollageException
	 * ("CollageAdmin API. addHostsToHostGroup. Failed adding Hosts ["
	 * +serviceList +"] to Hostgroup [" + serviceGroupName + "]" + e); } }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdmin#addHostsToHostGroup(java.lang.String,
	 * java.lang.String)
	 */
	public HostGroup updateHostGroup(String applicationType,
			String hostGroupName, List<String> hostList)
			throws CollageException {
		return updateHostGroup(applicationType, hostGroupName, hostList, null, null, null, null, null);
	}

    /*
     * (non-Javadoc)
     */
    public HostGroup updateHostGroup(String applicationType,
                                     String hostGroupName, List<String> hostList, HostGroup hostGroup,
                                     List<Host> hosts)
            throws CollageException {
        return updateHostGroup(applicationType, hostGroupName, hostList, null, null, null, hostGroup, hosts);
    }

	/**
	 * Propagate host changes to the hostgroup
	 * 
	 * @param host
	 * @return
	 */
	public Set<HostGroup> propagateHostChangesToHostGroup(Host host)
			throws CollageException {
		if (host == null) {
			log.error("propagateHostChangesToHostGroup. Host not defined!");
			throw new CollageException(
					"CollageAdmin API. propagateHostChangesToHostGroup. Host not defined!");
		} // end if
		Set<HostGroup> result = host.getHostGroups();
		return result;
	}

	/**
	 * Propagate host changes to the hostgroup
	 *
	 * @param hostGroups
	 * @return
	 */
	public Set<HostGroup> propagateHostChangesToHostGroup(
			Set<HostGroup> hostGroups) throws CollageException {
		if (hostGroups == null || hostGroups.size() < 1) {
			log
					.error("propagateHostChangesToHostGroup. HostGroups not defined!");
			throw new CollageException(
					"CollageAdmin API. propagateHostChangesToHostGroup. HostGroups not defined!");
		} // end if

		return hostGroups;
	}

	/**
	 * Propagate Service changes to the Host
	 * 
	 * @param serviceStatus
	 * @return
	 */
	public Host propagateServiceChangesToHost(ServiceStatus serviceStatus)
			throws CollageException {
		if (serviceStatus == null) {
			log
					.error("propagateServiceChangesToHost. ServiceStatus not defined!");
			throw new CollageException(
					"CollageAdmin API. propagateServiceChangesToHost. ServiceStatus not defined!");
		}
		return serviceStatus.getHost();
	}

	/**
	 * Propagate Service changes to the Host.Just to stimulate the AOP
	 * 
	 * @param host
	 * @return
	 */
	public Host propagateServiceChangesToHost(Host host)
			throws CollageException {
		if (host == null) {
			log.error("propagateServiceChangesToHost. Host not defined!");
			throw new CollageException(
					"CollageAdmin API. propagateServiceChangesToHost. Host not defined!");
		}
		return host;
	}

	/**
	 * Propagate Service changes to the ServiceGroup
	 * 
	 * @param serviceStatus
	 * @return
	 */
	public Collection<Category> propagateServiceChangesToServiceGroup(
			ServiceStatus serviceStatus) throws CollageException {
		if (serviceStatus == null) {
			log
					.error("propagateServiceChangesToServiceGroup. ServiceStatus not defined!");
			throw new CollageException(
					"CollageAdmin API. propagateServiceChangesToServiceGroup. ServiceStatus not defined!");
		} // end if

        EntityType serviceGroupEntityType = metadataService
                .getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        EntityType serviceStatusEntityType = metadataService
                .getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
        if ((serviceGroupEntityType == null) || (serviceStatusEntityType == null)) {
            log
                    .error("propagateServiceChangesToServiceGroup. ServiceGroup EntityTypes not found!");
            throw new CollageException(
                    "CollageAdmin API. propagateServiceChangesToServiceGroup. ServiceGroup EntityTypes not found!");
        }
        // TODO: may need to propagate root ServiceGroups
        return categoryService.getEntityCategoriesByObjectId(serviceGroupEntityType,
                serviceStatus.getServiceStatusId(), serviceStatusEntityType);
	}

    /*
     * (non-Javadoc)
     */
    public HostGroup updateHostGroup(String applicationType,
                                     String hostGroupName, List<String> hostList, String alias,
                                     String description,
                                     String agentId) throws CollageException {
        return updateHostGroup(applicationType, hostGroupName, hostList, alias, description, agentId, null, null);
    }

	/**
	 * updateHostGroup that takes all fields
	 * 
	 * @param applicationType
	 * @param hostGroupName
	 * @param hostList
	 * @param alias
	 * @param description
     * @param hostGroup
     * @param hosts
	 * @return
	 * @throws CollageException
	 */
	public HostGroup updateHostGroup(String applicationType,
			String hostGroupName, List<String> hostList, String alias,
            String description,
			String agentId, HostGroup hostGroup, List<Host> hosts) throws CollageException {

		CollageTimer timer = startMetricsTimer();
        if ((hostGroup != null) && ((hostGroupName == null) || (hostGroupName.length() == 0))) {
            hostGroupName = hostGroup.getName();
        }
		if (hostGroupName == null || hostGroupName.length() == 0) {
			log.error("addHostsToHostGroup. HostGroup Name not defined!");
			throw new CollageException(
					"CollageAdmin API. addHostsToHostGroup. HostGroup Name not defined!");
		}

		ApplicationType appType = null;

		if (applicationType != null && applicationType.length() > 0)
			appType = metadataService.getApplicationTypeByName(applicationType);

		if (appType == null)
			appType = metadataService
					.getApplicationTypeByName(DEFAULT_APP_TYPE);

		/*
		 * Check if HostGroup exists If HostGroup exists hosts will be added
		 * otherwise the hostgroup will be created
		 */
        if (hostGroup == null) {
            hostGroup = hgService.getHostGroupByName(hostGroupName);
        }
		boolean hgNull = false;
		if (hostGroup == null) {
			try {
				// create new hostgroup
                hostGroup = hgService.createHostGroup();
                hostGroup.setName(hostGroupName);
                hostGroup.setApplicationType(appType);

				/* Set the values if they are defined */
				if (alias != null)
                    hostGroup.setAlias(alias.isEmpty() ? null : alias);
				if (description != null)
                    hostGroup.setDescription(description.isEmpty() ? null : description);
                if (agentId != null)
                    hostGroup.setAgentId(agentId);

				// Save host group before updating each host
				hgService.saveHostGroup(hostGroup);
				hgNull = true;
			} catch (Exception e) {
				log.error("addHostsToHostGroup. Failed to create Hostgroup ["
						+ hostGroupName + "].", e);
				throw new CollageException(
						"CollageAdmin API. addHostsToHostGroup. Failed to create Hostgroup ["
								+ hostGroupName + "].", e);
			}
		}

		// Set /update description
		if (description != null)
			hostGroup.setDescription(description.isEmpty() ? null : description);
		if (alias != null)
			hostGroup.setAlias(alias.isEmpty() ? null : alias);

        // save hosts cleared or added to host group
        Set<Host> hostsToUpdate = new HashSet<Host>();

		// First we clear all the hosts
		Set hgHosts = hostGroup.getHosts();
		if (hgNull) {
            // For performance reasons, the underlying Hibernate bidirectional relationship
            // for this collection is inverse. Changes must be made on the Host side to ensure
            // the change is persisted. See addHostsToHostGroup() and HostGroup.hbm.xml.
            for (Host hgHost : new ArrayList<Host>((Set<Host>)hgHosts)) {
                hgHost.getHostGroups().remove(hostGroup);
                hgHosts.remove(hgHost);
                hostsToUpdate.add(hgHost);
            }
        }

        // Add hosts to host group
        Collection<Host> addHosts = Collections.EMPTY_LIST;
        if (hosts != null) {
            addHosts = hosts;
        } else if (hostList != null && hostList.size() > 0) {
			addHosts = hostIdentityService.getHostsByIdOrHostNames(hostList);
		}
        // For performance reasons, the underlying Hibernate bidirectional relationship
        // for this collection is inverse. Changes must be made on the Host side to ensure
        // the change is persisted. See addHostsToHostGroup() and HostGroup.hbm.xml.
        for (Host host : addHosts) {
            host.getHostGroups().add(hostGroup);
            hgHosts.add(host);
            hostsToUpdate.add(host);
        }

		try {
			// Save and commit hosts and host group
            hostService.saveHost(hostsToUpdate);
			hgService.saveHostGroup(hostGroup);
		} catch (Exception e) {
			log.error("updateHostGroup. Failed updating Hosts [" + hostList
					+ "] to Hostgroup [" + hostGroupName + "]", e);
			throw new CollageException(
					"CollageAdmin API. updateHostGroup. Failed updating Hosts ["
							+ hostList + "] to Hostgroup [" + hostGroupName
							+ "]" + e);
		}
		stopMetricsTimer(timer);
		return hostGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdmin#removeDevicesFromChildDevice(java
	 * .lang.String, java.lang.String)
	 */
	public void removeDevicesFromChildDevice(String childDevice,
			List<String> deviceList) throws CollageException {
		// Nothing to do
		if (deviceList == null || deviceList.size() == 0)
			return;

		if (childDevice == null || childDevice.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty child device identification");

		try {
			deviceService.detachParentDevices(childDevice, deviceList);
		} catch (Exception e) {
			log.error("Unable to detach parent devices - Child: " + childDevice
					+ ", device list: " + deviceList, e);
			throw new CollageException("Unable to detach parent devices", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdmin#removeDevicesFromParentDevice(java
	 * .lang.String, java.lang.String)
	 */
	public void removeDevicesFromParentDevice(String parentDevice,
			List<String> deviceList) throws CollageException {
		// Nothing to do
		if (deviceList == null || deviceList.size() == 0)
			return;

		if (parentDevice == null || parentDevice.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty parent device identification");

		try {
			deviceService.detachChildDevices(parentDevice, deviceList);
		} catch (Exception e) {
			log.error("Unable to detach child devices - Parent: "
					+ parentDevice + ", device list: " + deviceList, e);
			throw new CollageException("Unable to detach child devices", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdmin#removeDevicesFromMonitorServer(java
	 * .lang.String, java.lang.String)
	 */
	public void removeDevicesFromMonitorServer(String monitorServer,
			List<String> deviceList) throws CollageException {
		try {
			deviceService.removeDevicesFromMonitorServer(monitorServer,
					deviceList);
		} catch (Exception e) {
			String msg = "Error occurred in removeDevicesFromMonitorServer(), MonitorServer: "
					+ monitorServer + ", deviceList: " + deviceList;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdmin#removeHostsFromHostGroup(java.lang
	 * .String, java.lang.String)
	 */
	public void removeHostsFromHostGroup(String hostGroupName,
			List<String> hostList) throws CollageException {
		if (hostGroupName == null || hostGroupName.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty host group name parameter.");

		// Nothing to do if no hosts names are provided.
		if (hostList == null || hostList.size() == 0)
			return;

		// HostGroup must exist
		HostGroup hg = hgService.getHostGroupByName(hostGroupName);

		if (hg == null) {
			log.error("removeHostsFromHostGroup. Hostgroup [" + hostGroupName
					+ "] doesn't exist.");
			throw new CollageException(
					"CollageAdmin API. removeHostsFromHostGroup. Hostgroup ["
							+ hostGroupName + "] doesn't exist.");
		}

		// Remove hostgroup from host
		try {

			Host host = null;
			Collection<Host> colHosts = new ArrayList<Host>(hostList.size());
			Iterator<String> itHostNames = hostList.iterator();
			while (itHostNames.hasNext()) {
				host = hostIdentityService.getHostByIdOrHostName(itHostNames.next());
				if (host != null) {
					host.getHostGroups().remove(hg);
					// Add to collection of hosts and save all at once
					colHosts.add(host);
				}
			}

			// Save all hosts
			hostService.saveHost(colHosts);
		} catch (Exception e) {
			log.error("removeHostsFromHostGroup. Failed removing Hosts ["
					+ hostList + "] from Hostgroup [" + hostGroupName + "]", e);
			throw new CollageException(
					"CollageAdmin API. removeHostsFromHostGroup. Failed removing Hosts ["
							+ hostList + "] from Hostgroup [" + hostGroupName
							+ "]" + e);
		}
	}

	/*
	 * Deletes the Service with the name provided - unlinks (but does not
	 * delete) all LogMessages that were previously attached to this Service
	 * 
	 * @param serviceDescription a string identifying the service
	 */
	public Integer removeService(String hostName, String serviceDescription)
			throws CollageException {
		Integer serviceStatusId = -1;

		try {
            ServiceStatus service =
                    hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(serviceDescription, hostName);
			if (service != null) {
				serviceStatusId = service.getServiceStatusId();
				statusService.deleteService(service.getHost().getHostName(), serviceDescription);
			} else {
				log.warn("Unable to remove service, hostName: " + hostName
						+ ", Service: " + serviceDescription
						+ ". Service doesn't exist");
			}

		} catch (Exception e) {
			log.error("Unable to remove service, hostName: " + hostName
					+ ", Service: " + serviceDescription, e);
			throw new CollageException("Unable to remove service, hostName: "
					+ hostName + ", Service: " + serviceDescription, e);

		}

		/* Indicate no service since caller checks for ID > 0 */
		return serviceStatusId;
	}

	/*
	 * Deletes the Service with the id provided - unlinks (but does not delete)
	 * all LogMessages that were previously attached to this Service
	 * 
	 * @param serviceId a string identifying the service
	 */
	public void removeService(int serviceId) throws CollageException {
		try {
			statusService.deleteService(serviceId);
		} catch (Exception e) {
			log.error("Unable to remove service, Service Id: " + serviceId, e);
			throw new CollageException("Unable to remove service, Service Id: "
					+ serviceId, e);
		}
	}

	/*
	 * deletes the Host for the host name provided, and the related HostStatus,
	 * and ServiceStatus - unlinks (but does not delete) all LogMessages that
	 * were previously attached to this Host
	 * 
	 * @param hostName
	 */
	public Integer removeHost(String hostName) throws CollageException {
		Integer result = null;
		try {
			Host host = hostIdentityService.getHostByIdOrHostName(hostName);
			if (host != null) {
				result = host.getHostId();
				hostService.deleteHostByName(host.getHostName());
			} // end if
		} catch (Exception e) {
			String msg = "Error occurred in removeHost(), Host: " + hostName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return result;
	}

	/*
	 * deletes the Host for the host id provided, and the related HostStatus,
	 * and ServiceStatus - unlinks (but does not delete) all LogMessages that
	 * were previously attached to this Host
	 * 
	 * @param hostId
	 */
	public Integer removeHost(int hostId) throws CollageException {
		try {
			hostService.deleteHostById(hostId);
		} catch (Exception e) {
			String msg = "Error occurred in removeHost(), Host Id: " + hostId;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return new Integer(hostId);
	}

	/*
	 * Deletes the HostGroup with the name provided, but does not affect any of
	 * the Hosts within that HostGroup
	 * 
	 * @param hostGroupName
	 */
	public Integer removeHostGroup(String hostGroupName)
			throws CollageException {
		Integer hostGroupId = null;
		try {
			HostGroup hostGroup = hgService.getHostGroupByName(hostGroupName);
			if (hostGroup != null) {
				hostGroupId = hostGroup.getHostGroupId();
				hgService.deleteHostGroupByName(hostGroupName);
			} // end if
		} catch (Exception e) {
			String msg = "Error occurred in removeHostGroup(), HostGroup: "
					+ hostGroupName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return hostGroupId;
	}

	/*
	 * Deletes the HostGroup with the id provided, but does not affect any of
	 * the Hosts within that HostGroup
	 * 
	 * @param hostGroupId
	 */
	public Integer removeHostGroup(int hostGroupId) throws CollageException {
		try {
			hgService.deleteHostGroupById(hostGroupId);
		} catch (Exception e) {
			String msg = "Error occurred in removeHostGroup(), HostGroup Id: "
					+ hostGroupId;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return new Integer(hostGroupId);
	}

	/*
	 * Deletes the Device with the name provided, including all its Hosts,
	 * Services and LogMessages
	 * 
	 * @param serverIdent the IP or MAC address of the server (Device) to be
	 * deleted
	 */
	public void removeDevice(String serverIdent) throws CollageException {
		try {
			deviceService.deleteDeviceByIdentification(serverIdent);
		} catch (Exception e) {
			String msg = "Error occurred in removeDevice(), Identification: "
					+ serverIdent;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * Deletes the Device with the id provided, including all its Hosts,
	 * Services and LogMessages
	 * 
	 * @param serverIdent the IP or MAC address of the server (Device) to be
	 * deleted
	 */
	public void removeDevice(int deviceId) throws CollageException {
		try {
			deviceService.deleteDeviceById(deviceId);
		} catch (Exception e) {
			String msg = "Error occurred in removeDevice(), Id: " + deviceId;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	public Properties createNagiosServiceStatusProps(String ServiceDescription,
			String LastPluginOutput, String MonitorStatus, String RetryNumber,
			String StateType, String LastCheckTime, String NextCheckTime,
			String CheckType, String isChecksEnabled,
			String isAcceptPassiveChecks, String isEventHandlersEnabled,
			String LastStateChange, String isProblemAcknowledged,
			String LastHardState, String TimeOK, String TimeUnknown,
			String TimeWarning, String TimeCritical,
			String LastNotificationTime, String CurrentNotificationNumber,
			String isNotificationsEnabled, String Latency,
			String ExecutionTime, String isFlapDetectionEnabled,
			String isServiceFlapping, String PercentStateChange,
			String ScheduledDowntimeDepth, String isFailurePredictionEnabled,
			String isProcessPerformanceData, String isObsessOverService) {
		// Call into static Nagios class for getting a property MAP
		Properties properties = Nagios.createServiceStatusProps(
				ServiceDescription, LastPluginOutput, MonitorStatus,
				RetryNumber, StateType, LastCheckTime, NextCheckTime,
				CheckType, isChecksEnabled, isAcceptPassiveChecks,
				isEventHandlersEnabled, LastStateChange, isProblemAcknowledged,
				LastHardState, TimeOK, TimeUnknown, TimeWarning, TimeCritical,
				LastNotificationTime, CurrentNotificationNumber,
				isNotificationsEnabled, Latency, ExecutionTime,
				isFlapDetectionEnabled, isServiceFlapping, PercentStateChange,
				ScheduledDowntimeDepth, isFailurePredictionEnabled,
				isProcessPerformanceData, isObsessOverService, ""/*PerformanceData*/);

		return properties;
	}

	/*
	 * API to manage Categories which are nothing more than nested groups. The
	 * methods are called from the adapter so that third party applications are
	 * able to do inserts and updates.
	 */

	/*
	 * Legacy method for adding categories
	 */
	public Category addCategoryEntity(String categoryName, String entityType, String entityEntityType,
			String objectID) throws CollageException {

		return addCategoryEntity(categoryName, entityType, entityEntityType, objectID, null);
	}

	/*
	 * @param categoryName
	 * @param entityType
	 * @param entityEntityType
	 * @param entityObjectID
	 * @return CategoryID of the created Category
	 * @throws CollageException
	 * 
	 * Note if ObjectID is not null a CategoryEntity for that Object type will
	 * be created. If ObjectID is null only a CategoryEntry will be created. If
	 * the category already exists only the CategoryEntity will be added.
	 */
	public Category addCategoryEntity(String categoryName, String entityType, String entityEntityType,
			String entityObjectID, String description) throws CollageException {
		CollageTimer timer = startMetricsTimer();
		Category category = null;
        if (categoryName != null && categoryName.length() > 0 &&
                entityType != null && entityType.length() > 0) {
			try {
				category = categoryService.getCategoryByName(categoryName, entityType);
				if (category == null) {
					EntityType et_service_group = metadataService.getEntityTypeByName(entityType);
					if (description != null)
						category = categoryService.createCategory(categoryName,
								description, et_service_group);
					else
						category = categoryService.createCategory(categoryName,
								"", et_service_group);

					if (category == null) {
						log
								.error("creation of new Category failed.Category Name ["
										+ categoryName + "]");
						throw new CollageException(
								"CollageAdmin API. Creation of new Category failed.Category Name ["
										+ categoryName + "]");

					}
					categoryService.saveCategory(category);
				}

                if ((entityEntityType != null && entityEntityType.length() > 0) &&
                        (entityObjectID != null && entityObjectID.length() > 0)) {
					/*
					 * Check if the Entity already exists for the given
					 * Categories. Make sure duplicates don't exists
					 */
                    int objectIdValue = Integer.parseInt(entityObjectID);
                    for (CategoryEntity categoryEntity : category.getCategoryEntities()) {
                        if ((categoryEntity.getObjectID() != null) && (categoryEntity.getObjectID() == objectIdValue) &&
                                (categoryEntity.getEntityType() != null) &&
                                categoryEntity.getEntityType().getName().equals(entityEntityType)) {
                            log.warn("Object ID [" + entityObjectID
                                    + "] already exists in Category ["
                                    + categoryName + "]");
                            // Done no action to take
                            return category;
                        }
                    }

                    EntityType et = metadataService.getEntityTypeByName(entityEntityType);
                    if (et == null) {
                        log.error("EntityType [" + entityEntityType + "] doesn't exist");
                        throw new CollageException("CollageAdmin API. EntityType ["
                                + entityEntityType + "]");
                    }

                    CategoryEntity ce = categoryService.createCategoryEntity();

					if (ce == null) {
						log
								.error("Failed to create CategoryEntity for categoryName ["
										+ categoryName
										+ "] and  entityType ["
										+ entityEntityType + "]");
						throw new CollageException(
								"CollageAdmin API. Failed to create EntityType ["
										+ entityEntityType + "]");
					}

					ce.setEntityType(et);
					ce.setObjectID(new Integer(entityObjectID));
					ce.setCategory(category);
					// Save and commit
					categoryService.saveCategoryEntity(ce);
				}
			} catch (CollageException ce) {
				log.error("Error occurred in addCategoryEntity()", ce);
				throw ce;
			} catch (Exception e) {
				log
						.error("save Category and CategoryEntity failed.Category Name ["
								+ categoryName
								+ "] EntityType ["
                                + entityType
                                + "] EntityEntityType ["
								+ entityEntityType
								+ "]" + e);
				throw new CollageException(
						"CollageAdmin API. save Category and CategoryEntity failed.Category Name ["
								+ categoryName + "] EntityType [" + entityType
                                + "] EntityEntityType [" + entityEntityType
								+ "]" + e);
			}
			//
			/*
			 * try { EntityType entityT = category.getEntityType();
			 * log.debug("CollageAdminImpl.addEntities: EntityTypeId ["
			 * +entityT.getEntityTypeId()+"]"); // Save and commit
			 * categoryService.saveCategory(category); } catch (Exception e) {
			 * log.error("save new Category failed.Category Name [" +
			 * categoryName +"]" + e); throw newCollageException(
			 * "CollageAdmin API. save new Category failed.Category Name [" +
			 * categoryName +"]" + e); }
			 */
		} else {
			log
					.error("CollageAdmin API. Creation of new Category failed.Category Name ["
							+ categoryName
							+ "] EntityType ["
							+ entityType
                            + "] EntityEntityType ["
                            + entityEntityType
							+ "] ObjectID [" + entityObjectID + "]");
			throw new CollageException(
					"CollageAdmin API. Creation of new Category failed.Category Name ["
							+ categoryName + "] EntityType [" + entityType
                            + "] EntityEntityType [" + entityEntityType
							+ "] ObjectID [" + entityObjectID + "]");
		}
		/*
		 * else { try { // Save and commit
		 * categoryService.saveCategory(category); } catch (Exception e) {
		 * log.error("save new Category failed.Category Name [" + categoryName
		 * +"]" + e); throw newCollageException(
		 * "CollageAdmin API. save new Category failed.Category Name [" +
		 * categoryName +"]" + e); } }
		 */
		stopMetricsTimer(timer);
		return category;
	}

	/**
	 * @param categoryName
	 * @param entityType
     * @return removed category
	 * @throws CollageException
	 */
	public Category removeCategory(String categoryName, String entityType) throws CollageException {
		Integer result = null;
		if (categoryName == null || categoryName.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty category name parameter.");
        if (entityType == null || entityType.length() == 0)
            throw new IllegalArgumentException(
                    "Invalid null / empty entity type parameter.");

        Category category = null;
		try {
			category = categoryService.getCategoryByName(categoryName, entityType);
			categoryService.deleteCategoryByName(categoryName, entityType);
		} catch (Exception e) {
			String msg = "Unable to delete category - " + categoryName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return category;
	}

	/**
	 * @param categoryID
     * @return removed category
	 * @throws CollageException
	 */
	public Category removeCategory(Integer categoryID) throws CollageException {
		if (categoryID == null)
			throw new IllegalArgumentException(
					"Invalid null category id parameter.");

        Category category = null;
		try {
            category = categoryService.getCategoryById(categoryID);
			categoryService.deleteCategoryById(categoryID);
		} catch (Exception e) {
			String msg = "Unable to delete category id - " + categoryID;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return category;
	}

	public Category updateCategory(Integer categoryID, String name,
			String description) throws CollageException {
		if (categoryID == null)
			throw new IllegalArgumentException(
					"Invalid null category id parameter.");

		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty category name parameter.");

        Category category = null;
        try {
			category = categoryService.getCategoryById(categoryID.intValue());
			if (category == null) {
				if (log.isWarnEnabled())
					log
							.warn("Unable to update category.  Category not found for id - "
									+ categoryID);
			} else {
				category.setName(name);
				category.setDescription(description.isEmpty() ? null : description);
			}

			// Persist changes
			categoryService.saveCategory(category);
		} catch (Exception e) {
			String msg = "Unable to update category id - " + categoryID;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
        return category;
	}

	/*
	 * @param categoryName
	 * @param entityType
	 * @param entityEntityType
	 * @param entityObjectID
	 * 
	 * @throws CollageException
	 */

	public Category removeCategoryEntity(String categoryName, String entityType,
			String entityEntityType, String objectID) throws CollageException {
		Category category = null;
		try {
			category = categoryService.getCategoryByName(categoryName, entityType);
			if (category == null) {
				log
						.warn("AdminAPI. removeCategoryEntity method. Category doesn't exuist.Category Name ["
								+ categoryName + "]");
				return null;
			}

			// Check for Entity Type
			EntityType et = metadataService.getEntityTypeByName(entityEntityType);
			if (et == null) {
				log
						.warn("AdminAPI. removeCategoryEntity method. EntityType doesn't exist.EntityType Name ["
								+ entityEntityType + "]");
				return null;
			}

			Iterator itEntities = category.getCategoryEntities().iterator();
			while (itEntities.hasNext()) {
				CategoryEntity ce = (CategoryEntity) itEntities.next();

				if (ce != null
						&& (ce.getEntityType().getEntityTypeId().intValue() == et
								.getEntityTypeId().intValue())
						&& (ce.getObjectID().toString().compareTo(objectID) == 0)
						&& (ce.getCategory().getCategoryId().intValue() == category
								.getCategoryId().intValue())) {
					// Found
					if (log.isDebugEnabled())
						log.debug("EntityType Name [" + entityEntityType
								+ "] and ObjectID [" + objectID + "]");

					// Save and commit
					itEntities.remove();
                    categoryService.deleteCategoryEntity(ce);
                    categoryService.saveCategory(category);
					if (log.isDebugEnabled())
						log
								.debug("AdminAPI. removeCategoryEntity method. CategoryEntity for EntityType Name ["
										+ entityEntityType
										+ "] and ObjectID ["
										+ objectID
										+ "] Removed from Category ["
										+ categoryName + "]");

					return category;
				}
			}
		} catch (Exception e) {
			String msg = "CollageAdmin API. remove CategoryEntity failed.Category Name ["
					+ categoryName + "] EntityType [" + entityEntityType + "]";

			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdminInfrastructure#removeCategoryEntity
	 * (java.lang.String, int)
	 */
	public Collection<Category> removeCategoryEntity(String entityType,
			int objectID) throws CollageException {
		if (entityType == null || objectID <= 0) {
			throw new CollageException(
					"Cannot remove CategoryEntity.Invalid entitytype or object id");
		} // end if
		return categoryService.deleteCategoryEntityByObjectID(objectID, entityType);
	}

	/*
	 * @param parentCategoryName
	 * @param categoryName
	 * @param entityType
	 * @throws CollageException
	 */
	public Category addCategoryToParent(String parentCategoryName,
			String categoryName, String entityType) throws CollageException {
		Category category = categoryService.getCategoryByName(categoryName, entityType);
		Category parent = categoryService.getCategoryByName(parentCategoryName, entityType);
		if (category == null || parent == null) {
			if (log.isWarnEnabled())
				log
						.warn("AdminAPI. addCategoryToParent method. Category Name ["
								+ categoryName
								+ "] or Parent Category Name ["
								+ parentCategoryName + "] doesn't exist.");
			return null;
		}

		category.getParents().add(parent);

		try {
			// Save and commit
			categoryService.saveCategory(category);
			if (log.isInfoEnabled()) {
				log.info("Admin API. addCategoryToParent was successful");
			}
		} catch (Exception e) {
			String msg = "addCategoryToParent failed.Category Name ["
					+ categoryName + "] ParentCategory [" + parentCategoryName
					+ "]";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return category;
	}

	/*
	 * @param parentCategoryName
	 * @param categoryName
	 * @param entityType
	 * @throws CollageException
	 */
	public void removeCategoryFromParent(String parentCategoryName,
			String categoryName, String entityType) throws CollageException {
		Category category = categoryService.getCategoryByName(categoryName, entityType);
		Category parent = categoryService.getCategoryByName(parentCategoryName, entityType);
		if (category == null || parent == null) {
			if (log.isWarnEnabled())
				log
						.warn("AdminAPI. removeCategoryFromParent method. Category Name ["
								+ categoryName
								+ "] or Parent Category Name ["
								+ parentCategoryName + "] doesn't exist.");
			return;
		}

		category.getParents().remove(parent);

		try {
			// Save and commit
			categoryService.saveCategory(category);
			if (log.isInfoEnabled()) {
				log.info("Admin API. removeCategoryFromParent was successful");
			}
		} catch (Exception e) {
			String msg = "removeCategoryFromParent failed.Category Name ["
					+ categoryName + "] ParentCategory [" + parentCategoryName
					+ "]";

			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

    public void saveCategory(Category category) throws CollageException {
        categoryService.saveCategory(category);
    }

    public void propagateCreatedCategories(Collection<Category> categories) throws CollageException {
        // stub to be intercepted by AOP
    }

    public void propagateDeletedCategories(Collection<Category> categories) throws CollageException {
        // stub to be intercepted by AOP
    }

    public void propagateModifiedCategories(Collection<Category> categories) throws CollageException {
        // stub to be intercepted by AOP
    }

	/*
	 * Manage Consolidation Criterias. Consolidation criterias will be applied
	 * to any inserts to LogMessage that identify the consolidation criteria to
	 * be applied
	 */

	/*
	 * 
	 * @param Name
	 * 
	 * @param ConsolidationCriteria
	 * 
	 * @throws CollageException
	 */
	public void addOrUpdateConsolidationCriteria(String name,
			String consolidationCriteria) throws CollageException {
		// Lookup consolidation criteria by name
		ConsolidationCriteria criteria = consolidationService
				.getConsolidationCriteriaByName(name);

		// Create New Consolidation Criteria
		if (criteria == null) {
			criteria = consolidationService.createConsolidationCriteria(name,
					consolidationCriteria);
			if (criteria == null) {
				log
						.error("addOrUpdateConsolidationCriteria failed.Consolidation Name ["
								+ name
								+ "] Criteria ["
								+ consolidationCriteria
								+ "]");
				throw new CollageException(
						"CollageAdmin API. addOrUpdateConsolidationCriteria failed.Consolidation Name ["
								+ name
								+ "] Criteria ["
								+ consolidationCriteria
								+ "]");
			}
		} else {
			// Update existing criteria
			criteria.setCriteria(consolidationCriteria);
		}

		try {
			// Save and commit
			consolidationService.saveConsolidationCriteria(criteria);
			if (log.isInfoEnabled()) {
				log
						.info("Admin API. addOrUpdateConsolidationCriteria was successful");
			}
		} catch (Exception e) {
			String msg = "addOrUpdateConsolidationCriteria failed.Consolidation Name ["
					+ name + "] Criteria [" + consolidationCriteria + "]";

			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * 
	 * @param Name
	 * 
	 * @throws CollageException
	 */
	public boolean removeConsolidationCriteria(String name)
			throws CollageException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty consolidation criteria name parameter.");

		try {
            ConsolidationCriteria criteria = consolidationService.getConsolidationCriteriaByName(name);
            if (criteria == null) {
                return false;
            }
			consolidationService.deleteConsolidationCriteriaByName(name);
            return true;
		} catch (Exception e) {
			String msg = "Unable to remove consolidation criteria - " + name;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/*
	 * 
	 * @param Name
	 * 
	 * @throws CollageException
	 */
	public void removeConsolidationCriteria(Integer consolidationCriteriaID)
			throws CollageException {
		if (consolidationCriteriaID == null)
			throw new IllegalArgumentException(
					"Invalid null consolidation criteria id parameter.");

		try {
			consolidationService
					.deleteConsolidationCriteriaById(consolidationCriteriaID
							.intValue());
		} catch (Exception e) {
			String msg = "Unable to remove consolidation criteria, ID: "
					+ consolidationCriteriaID;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/**
	 * 
	 * @param consolidationCriteriaID
	 * @param name
	 * @param consolidationCriteria
	 * @throws CollageException
	 */
	public void updateConsolidationCriteria(Integer consolidationCriteriaID,
			String name, String consolidationCriteria) throws CollageException {
		if (consolidationCriteriaID == null)
			throw new IllegalArgumentException(
					"Invalid null consolidation criteria id parameter.");

		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null consolidation criteria name parameter.");

		if (consolidationCriteria == null
				|| consolidationCriteria.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null consolidation criteria parameter.");

		ConsolidationCriteria criteria = consolidationService
				.getConsolidationCriteriaById(consolidationCriteriaID
						.intValue());
		if (criteria == null) {
			log.error("Unable to find consolidation criteria, ID: "
					+ consolidationCriteriaID);
			throw new CollageException(
					"Unable to remove consolidation criteria, ID: "
							+ consolidationCriteriaID);
		}

		try {
			criteria.setName(name);
			criteria.setCriteria(consolidationCriteria);
			consolidationService.saveConsolidationCriteria(criteria);
		} catch (Exception e) {
			String msg = "Unable to update consolidation criteria, ID: "
					+ consolidationCriteriaID;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	/**
	 * 
	 * @param ServiceStatusID
	 * @param properties
	 * @throws CollageException
	 */
	public void updateServiceStatusByID(Integer ServiceStatusID, String applicationType, Map properties)
			throws CollageException {
		CollageTimer timer = startMetricsTimer();
		if (ServiceStatusID == null) {
			String msg = "ServiceStatusID is null. Can't update Object.";
			log.error(msg);
			throw new CollageException(msg);
		}

		ServiceStatus service = statusService.getServiceById(ServiceStatusID
				.intValue());

		if (service == null) {
			String msg = "Service for ServiceStatusID [" + ServiceStatusID
					+ "] doesn't exist.";
			log.error(msg);
			throw new CollageException(msg);
		}

        // check owning application type
        boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                (service.getApplicationType() == null) ||
                applicationType.equals(service.getApplicationType().getName()));
        // validate owner application type updates
        if (!owner) {
            sendNotOwnerServiceMessage(service, applicationType);
			stopMetricsTimer(timer);
            return;
        }

		// Update properties
		try {
			service.setProperties(properties);
			statusService.saveService(service);
		} catch (Exception e) {
			String err = "CollageAdminAPI - updateServiceStatusByID failed!";
			log.error(err, e);
			throw new CollageException(err, e);
		}

		if (log.isInfoEnabled())
			log.info("updated ServiceStatus for ServiceStatusID ["
					+ ServiceStatusID + "]");
        stopMetricsTimer(timer);
	}

	/**
	 * 
	 * @param hostStatusID
	 * @param properties
	 * @throws CollageException
	 */
	public void updateHostStatusByID(Integer hostStatusID, String applicationType, Map properties)
			throws CollageException {
		CollageTimer timer = startMetricsTimer();
		if (hostStatusID == null) {
			String msg = "HostStatusID is null. Can't update Object.";
			log.error(msg);
			throw new CollageException(msg);
		}

		HostStatus hostStatus = hostService.getStatusByHostId(hostStatusID
				.intValue());

		if (hostStatus == null) {
			String msg = "hostStatus for HostStatusID [" + hostStatusID
					+ "] doesn't exist.";
			log.error(msg);
			throw new CollageException(msg);
		}

		// Update properties
		try {
            // check owning application type
            Host host = hostStatus.getHost();
            boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                    (host.getApplicationType() == null) || applicationType.equals(host.getApplicationType().getName()));
            if (owner) {
                // merge last plugin output when owner
                String lastPluginOutput = (String)properties.remove(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
                updateOwnerHostStatusMessage(host, lastPluginOutput);
                hostStatus.setProperties(properties);
            } else {
                // update only status message if not owner
                String monitorStatus = (String)properties.remove(CollageAdminInfrastructure.PROP_MONITOR_STATUS);
                String lastPluginOutput = (String)properties.remove(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
                updateNotOwnerHostStatusMessage(host, applicationType, monitorStatus, lastPluginOutput);
            }
			hostService.saveHostStatus(hostStatus);
		} catch (Exception e) {
			String err = "CollageAdminAPI - updateServiceStatusByID failed!";
			log.error(err, e);
			throw new CollageException(err, e);
		}

		if (log.isInfoEnabled())
			log.info("updated hostStatus for HostStatusID - " + hostStatusID);
		stopMetricsTimer(timer);
	}

	/**
	 * 
	 * @param logMessageID
	 * @param properties
	 * @throws CollageException
	 */

	public LogMessage updateLogMessageByID(Integer logMessageID, Map properties)
			throws CollageException {
		CollageTimer timer = startMetricsTimer();
		if (logMessageID == null) {
			String msg = "LogMessageID is null. Can't update Object.";
			log.error(msg);
			throw new CollageException(msg);
		}

		LogMessage logMessage = logMsgService.getLogMessageById(logMessageID
				.intValue());

		if (logMessage == null) {
			String msg = "LogMessage for LogMessageID [" + logMessageID
					+ "] doesn't exist.";
			log.error(msg);
			throw new CollageException(msg);
		}

		// Update properties
		try {
			logMessage.setProperties(properties);

			/*
			 * Fix for GWMON-641 Any updates to the status need to reset the
			 * hash keys to 0. No other incoming messages should be consolidated
			 * on this modified message.
			 */
			Integer resetHash = new Integer(0);
			logMessage.setStatelessHash(resetHash);
			logMessage.setConsolidationHash(resetHash);

			logMsgService.saveLogMessage(logMessage);
		} catch (Exception e) {
			String err = "CollageAdminAPI - updateLogMessageByID failed!";
			log.error(err, e);
			throw new CollageException(err, e);
		}

		if (log.isInfoEnabled())
			log.info("updated LogMessage for LogMessageID - " + logMessageID);
		stopMetricsTimer(timer);
		return logMessage;
	}

	/**
	 * Update log message operation status for the specified log message.
	 * 
	 * @param logMessageId
	 * @param opStatus
	 * @throws CollageException
	 */
	public LogMessage updateLogMessageOperationStatus(String logMessageId,
			String opStatus) throws CollageException {
		CollageTimer timer = startMetricsTimer();
        if (logMessageId == null || logMessageId.length() < 1) {
			throw new IllegalArgumentException(
					"Invalid log message id parameter,  ID=" + logMessageId);
		}

		if (opStatus == null || opStatus.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid null operation status parameter.");
		}

		LogMessage logMessage = null;
		try {
			logMessage = logMsgService.getLogMessageById(Integer
					.parseInt(logMessageId));
			if (logMessage == null)
				throw new CollageException(
						"Unable to log message operation status - Log Message Not Found, ID: "
								+ logMessageId);

			OperationStatus operationStatus = metadataService
					.getOperationStatusByName(opStatus);
			if (operationStatus == null)
				throw new CollageException(
						"Unable to log message operation status - Operation Status Not Found, ID: "
								+ opStatus);

			logMessage.setOperationStatus(operationStatus);
			logMsgService.saveLogMessage(logMessage);
		} catch (CollageException e) {
			log.error(
					"CollageAdminAPI.updateLogMessageOperationStatus() failed. LogMessageId="
							+ logMessageId, e);
			throw e;
		} catch (Exception e) {
			String msg = "CollageAdminAPI.updateLogMessageOperationStatus() failed. LogMessageId="
					+ logMessageId;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
    	stopMetricsTimer(timer);
		return logMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdminInfrastructure#acknowledgeEvent(java
	 * .lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public boolean acknowledgeEvent(String applicationType, String typeRule,
			String host, String serviceDescription, String acknowledgedBy,
			String acknowledgeComment) throws CollageException {
		CollageTimer timer = startMetricsTimer();
        Collection<LogMessage> events = null;
        String hostId = "", serviceId = "";
        int count = 0;
        ArrayList<Integer> messageIds = new ArrayList<Integer>();
        try {
            FilterCriteria filterCriteria = null;
            if (serviceDescription != null) {
                filterCriteria = FilterCriteria.eq(LogMessage.HP_SERVICE_STATUS_DESCRIPTION,serviceDescription);
            //    filterCriteria.and(FilterCriteria.eq(LogMessage.HP_STATE_CHANGED, false));
                FoundationQueryList list = logMsgService.getLogMessagesByHostName(host, null, null,filterCriteria, null, -1, -1);
                events = list.getResults();
                if (log.isInfoEnabled()) {
                    log.info("Number of Host/Service events for acknowledge Events [" + list.size() + "]");
                }
                ServiceStatus serviceStatus =
                        hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(serviceDescription, host);
                if (serviceStatus != null) {
                    serviceId = serviceStatus.getServiceStatusId().toString();
                    hostId = serviceStatus.getHost().getHostId().toString();
                    host = serviceStatus.getHost().getHostName();
                }
            } else {
                //filterCriteria = FilterCriteria.eq(LogMessage.HP_STATE_CHANGED, false);
                /* Restrict records to Host with no service */
                filterCriteria = FilterCriteria.isNull(LogMessage.HP_SERVICE_STATUS_ID);
                FoundationQueryList list = logMsgService.getLogMessagesByHostName(host, null, null,filterCriteria, null, -1, -1);
                if (log.isInfoEnabled()) {
                    log.info("Number of Host events for acknowledge Events [" + list.size() + "]");
                }
                events = list.getResults();
                Host hostByHostName = hostIdentityService.getHostByIdOrHostName(host);
                if (hostByHostName != null) {
                    hostId = hostByHostName.getHostId().toString();
                    host = hostByHostName.getHostName();
                }
            }
        } catch (Exception e) {
            String err = "CollageAdminAPI - updateLogMessageByID failed!";
            log.error(err, e);
            throw new CollageException(err, e);
        }
        if (events != null) {
            String serviceMessage = (serviceDescription == null) ? "" : " and service " + serviceDescription;
            if (typeRule.compareToIgnoreCase("ACKNOWLEDGE") == 0) {
                count = ackEvent(events, messageIds, applicationType, host, acknowledgedBy, acknowledgeComment);
                if (count == 0) {
                    log.error("Acknowledge Event: failed to find Nagios acknowledged comment for host " + host + serviceMessage);
                }
            }
            else {
                count = unackEvent(events, messageIds, applicationType, host);
                if (count == 0) {
                    log.error("UnAcknowledge Event: failed to find Nagios acknowledge status for host " + host + serviceMessage);
                }
            }
        }
        else {
            StringBuilder warn = new StringBuilder(
                    "acknowledgeEvents -- no match found for host [");
            log.warn(warn.append(host).append("] Service [").append(
                    serviceDescription).append("]"));
        }
        if (count > 0) {
            CollageAdminInfrastructure admin = (CollageAdminInfrastructure) (_collage)
                    .getAPIObject("com.groundwork.collage.CollageAdmin");
            admin.triggerAcknowledgeEventAOP(messageIds, hostId, serviceId);
            stopMetricsTimer(timer);
            return true;
        }
        stopMetricsTimer(timer);
        return false;
    }

    private int ackEvent(Collection<LogMessage> events, ArrayList<Integer> messageIds, String applicationType, String host,
                          String acknowledgedBy, String acknowledgeComment) throws CollageException {
		CollageTimer timer = startMetricsTimer();
        long begin = System.currentTimeMillis();
        Iterator eventIT = events.iterator();
        LogMessage event = null;
        int count = 0;
        try {
            // Set the fields
            while (eventIT.hasNext()) {
                event = (LogMessage) eventIT.next();
                if (event != null) {
                    String comment = (String)event.getProperty(ACKNOWLEDGE_COMMENT);
                    if (comment != null && comment.equalsIgnoreCase(NAGIOS_ACK_COMMENT)) {
                        if (acknowledgedBy != null) {
                            event.setProperty(ACKNOWLEDGEDBY, acknowledgedBy);
                        }
                        if (acknowledgeComment != null) {
                            event.setProperty(ACKNOWLEDGE_COMMENT, acknowledgeComment);
                        }
                        OperationStatus operationStatus = metadataService.getOperationStatusByName(OP_STATUS_ACKNOWLEDGED);
                        if (operationStatus != null) {
                            event.setOperationStatus(operationStatus);
                        }
                        // Update Message
                        logMsgService.saveLogMessage(event);
                        count++;
                        messageIds.add(event.getLogMessageId());
                        if (log.isInfoEnabled()) {
                            StringBuilder logMsg = new StringBuilder();
                            log
                                    .warn(logMsg.append("Acknowledged Event for ApplicationType [")
                                            .append(applicationType)
                                            .append("] and host ")
                                            .append(host)
                                            .append(" done in ")
                                            .append(System.currentTimeMillis() - begin)
                                            .append(" ms").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            String err = "CollageAdminAPI - updateLogMessageByID failed!";
            log.error(err, e);
            throw new CollageException(err, e);
        }
        stopMetricsTimer(timer);
        return count;
    }

    private int unackEvent(Collection<LogMessage> events, ArrayList<Integer> messageIds, String applicationType, String host) throws CollageException {
		CollageTimer timer = startMetricsTimer();
        long begin = System.currentTimeMillis();
        int count = 0;
        Iterator eventIT = events.iterator();
        LogMessage event = null;
        try {
            while (eventIT.hasNext()) {
                event = (LogMessage) eventIT.next();
                if (event != null) {
                    OperationStatus eventStatus = event.getOperationStatus();
                    if (eventStatus != null && eventStatus.getName().equals(OP_STATUS_ACKNOWLEDGED)) {
                        event.setProperty(ACKNOWLEDGEDBY, "");
                        event.setProperty(ACKNOWLEDGE_COMMENT, "");
                        OperationStatus operationStatus = metadataService.getOperationStatusByName(OP_STATUS_OPEN);
                        if (operationStatus != null) {
                            event.setOperationStatus(operationStatus);
                        }
                        // Update Event Message
                        logMsgService.saveLogMessage(event);
                        count++;
                        messageIds.add(event.getLogMessageId());
                        if (log.isInfoEnabled()) {
                            StringBuilder logMsg = new StringBuilder();
                            log
                                    .warn(logMsg.append("UnAcknowledge Event for ApplicationType [")
                                            .append(applicationType)
                                            .append("] an host ")
                                            .append(host)
                                            .append(" done in ")
                                            .append(System.currentTimeMillis() - begin)
                                            .append(" ms").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            String err = "CollageAdminAPI - updateLogMessageByID failed!";
            log.error(err, e);
            throw new CollageException(err, e);
        }
		stopMetricsTimer(timer);
        return count;
    }

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.groundwork.collage.CollageAdminInfrastructure#triggerAcknowledgeEventAOP(java.util.ArrayList,
	 *      java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> triggerAcknowledgeEventAOP(ArrayList messageIds,
			String hostId, String serviceId) {
		return messageIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.groundwork.collage.CollageAdminInfrastructure#insertPerformanceData
	 * (java.lang.String, java.lang.String, java.lang.String, double,
	 * java.lang.String)
	 */
	public void insertPerformanceData(final String hostName,
			final String serviceDescription, final String performanceDataLabel,
			double performanceValue, String checkDate) throws CollageException {
		CollageTimer timer = startMetricsTimer();

		/* Verify is rollup interval was defined */
		if (rollupInterval == null) {
			log.info("Initialize PerformanceData rollup interval");

			String rollup = "day";
			String configFile = System.getProperty("configuration",
					FOUNDATION_PROPERTY_FILE);
			Properties configuration = new Properties();
			try {
				FileInputStream fis = new FileInputStream(configFile);
				configuration.load(fis);
				rollup = configuration.getProperty(
						PERFORMANCE_DATA_INTERVAL_PROP,
						PERFORMANCE_DATA_DEFAULT_INTERVAL).trim();
			} catch (Exception e) {
				log
						.warn("WARNING: Could not load foundation properties. Using default rollup inetval "
								+ PERFORMANCE_DATA_DEFAULT_INTERVAL);
			}
			rollupInterval = rollup;
		}

		// Call into the DAO
		try {
			performanceService.createOrUpdatePerformanceData(hostName,
					serviceDescription, performanceDataLabel, performanceValue,
					checkDate, rollupInterval);
		} catch (Exception e) {
			String msg = "Error while updating performance data. Host: "
					+ hostName + ", ServiceDesc: " + serviceDescription
					+ "DataLabel: " + performanceDataLabel + ", Value: "
					+ performanceValue + ", CheckDate: " + checkDate;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		stopMetricsTimer(timer);
	}

	/**
	 * Create hosts specified. If a host already exists it is updated. If no
	 * application type is defined it will default to NAGIOS. If a device and /
	 * or monitor server is specified which does not currently exist it will be
	 * created.
	 * 
	 * @param hostEntries
     * @deprecated
	 */
	public void addOrUpdateHosts(List<Hashtable<String, String>> hostEntries)
			throws CollageException {
		CollageTimer timer = startMetricsTimer();
		if (hostEntries == null || hostEntries.size() == 0)
			return;

		try {
			Iterator<Hashtable<String, String>> itEntries = hostEntries
					.iterator();
			while (itEntries.hasNext()) {
				addOrUpdateHost(itEntries.next());
			}
		} catch (Exception e) {
			String err = "Unable to create hosts - Error occurred in createHosts()";
			log.error(err, e);
			throw new CollageException(err, e);
		}
		stopMetricsTimer(timer);
	}

    /**
     * Create hosts specified. If a host already exists it is updated. If no
     * application type is defined it will default to NAGIOS. If a device and /
     * or monitor server is specified which does not currently exist it will be
     * created.
     *
     * @param hostEntries
     * @deprecated
     */
    public List<Host> addOrUpdateHostList(List<Map<String, String>> hostEntries)
            throws CollageException {
		CollageTimer timer = startMetricsTimer();
        List<Host> results = new ArrayList<Host>();
        if (hostEntries == null || hostEntries.size() == 0)
            return results;

        try {
            Iterator<Map<String, String>> itEntries = hostEntries
                    .iterator();
            while (itEntries.hasNext()) {
                Host host = addOrUpdateHost(itEntries.next());
                results.add(host);
            }
        } catch (Exception e) {
            String err = "Unable to create hosts - Error occurred in createHosts()";
            log.error(err, e);
            throw new CollageException(err, e);
        }
		stopMetricsTimer(timer);
        return results;
    }

    public Host addOrUpdateHost(Map<String, String> inHostAttributes) {
        return addOrUpdateHost(null, null, true, inHostAttributes);
    }

	public Host addOrUpdateHost(Host host, Device device, boolean mergeHosts, Map<String, String> inHostAttributes) {
		if (inHostAttributes == null)
			return null;
        CollageTimer timer = startMetricsTimer();
        Map<String, String> hostAttributes = new HashMap<String, String>();
        hostAttributes.putAll(inHostAttributes);
		// Retrieve and remove host attributes from map
		String hostName = hostAttributes.remove(PROP_HOST_NAME);
		String hostDescription = hostAttributes.remove(PROP_DESCRIPTION);
        String agentId = hostAttributes.remove(PROP_AGENT_ID);
		String identification = hostAttributes.remove(PROP_DEVICE_IDENTIFICATION);
        if ((device != null) && ((identification == null) || (identification.length() == 0))) {
            identification = device.getIdentification();
        }
		String deviceDisplayName = hostAttributes.remove(PROP_DISPLAY_NAME);
		String applicationType = hostAttributes
				.remove(PROP_APPLICATION_TYPE_NAME);
		String monitorServerName = hostAttributes.remove(PROP_MONITOR_SERVER);
		String monitorStatus = hostAttributes.get(PROP_MONITOR_STATUS);
        String lastPluginOutput = hostAttributes.get(PROP_LAST_PLUGIN_OUTPUT);
        String lastCheckTime = hostAttributes.remove(PROP_SERVICE_LAST_CHECK_TIME);
        String nextCheckTime = hostAttributes.remove(PROP_SERVICE_NEXT_CHECK_TIME);
		// update down monitor status depending on effective downtime
		String effectiveDowntime = hostAttributes.get(PROP_SCHEDULED_DOWNTIME_DEPTH);
		if ((effectiveDowntime == null) && (host != null) && (host.getHostStatus() != null)) {
			Integer downtimeProperty = (Integer)host.getHostStatus().getProperty(PROP_SCHEDULED_DOWNTIME_DEPTH);
			effectiveDowntime = ((downtimeProperty) != null ? downtimeProperty.toString() : null);
		}
		String newMonitorStatus = updateHostDownTimeStatus(monitorStatus, effectiveDowntime);
		if (newMonitorStatus != null) {
			monitorStatus = newMonitorStatus;
			hostAttributes.remove(PROP_MONITOR_STATUS);
			hostAttributes.put(PROP_MONITOR_STATUS, newMonitorStatus);
		}
		/*
		 * Required
		 */
        if ((host != null) && ((hostName == null) || (hostName.length() == 0))) {
            hostName = host.getHostName();
        }
		if ((hostName == null || hostName.length() == 0)) {
			log.error("Can't add / update host to system. HostName required");
			throw new CollageException(
					"Can't add host to system. HostName is required");
		}

        // lookup host identity/host if they already exist
        HostIdentity hostIdentity = null;
        if (host == null) {
            // lookup host identity
            hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(hostName);
            if (hostIdentity != null) {
                // validate merging host identity host name and host name, (if not
                // an alias lookup, host names differing by only case is considered
                // a merge since names are matching but different)
                if (!mergeHosts && (hostIdentity != null) && !hostName.equals(hostIdentity.getHostName()) &&
                        hostName.equalsIgnoreCase(hostIdentity.getHostName())) {
                    sendHostsMergeMessage(hostName, hostIdentity.getHostName(), hostIdentity.getHost(), applicationType);
					stopMetricsTimer(timer);
                    return null;
                }
                // use host or host name from host identity
                if (hostIdentity.getHost() != null) {
                    host = hostIdentity.getHost();
                } else {
                    hostName = hostIdentity.getHostName();
                }
            } else {
                // fallback to host lookup
                host = hostService.getHostByHostName(hostName);
            }
        }
        // validate merging host host name and host name, (if not an alias
        // lookup, host names differing by only case is considered a merge
        // since names are matching but different)
        if (!mergeHosts && (host != null) && !hostName.equals(host.getHostName()) &&
                hostName.equalsIgnoreCase(host.getHostName())) {
            sendHostsMergeMessage(hostName, host.getHostName(), host, applicationType);
            stopMetricsTimer(timer);
            return null;
        }
        // ensure host and host name are in sync
        if (host != null) {
            hostName = host.getHostName();
        }

		boolean newHost = false; // Set flag indicating existing host
		String lastMonitorStatus = null;
		// If host exists its an update otherwise create new host
		if (host == null) {
			if (identification == null || identification.length() == 0) {
				log
						.error("Can't add host to system. Device Identification required");
				throw new CollageException(
						"Can't add host to system. Device Identification is required");
			}

			// Set flag indicating new host
			newHost = true;

			host = hostService.createHost();

			/* Populate Host */
			host.setHostName(hostName);

			// Get Device
            if (device == null) {
                device = deviceService.getDeviceByIdentification(identification);
            }
		}
		// See if host device is changing
		else {
			device = host.getDevice();

			// If identification is not provided then device is not changing.
			// Otherwise,
			// see if they are the same.
			if (device == null
                    && identification != null
					&& identification.length() > 0
					&& (identification.equalsIgnoreCase(device
							.getIdentification()) == false)) {
				device = deviceService
						.getDeviceByIdentification(identification);
			}
			lastMonitorStatus = host.getHostStatus().getHostMonitorStatus()
					.getName();

		}

		/* Initialize hostgroup associations for the given host */
		host.getHostGroups().size();

		/*
		 * Make sure that the device doesn't exist otherwise update the
		 * reference to the existing device
		 */
		// create Device
		if (device == null) {
			device = deviceService.createDevice();

			/* Populate Device */
			device.setIdentification(identification);
			if (deviceDisplayName != null && deviceDisplayName.length() > 0)
				device.setDisplayName(deviceDisplayName);
			else
				device.setDisplayName(hostName);

			/* Default */
			if (monitorServerName == null || monitorServerName.length() == 0) {
				monitorServerName = DEFAULT_MONITOR_SERVER;
			}

			/* Check if Monitor Server is defined */
			MonitorServer monitorServer = monitorService
					.getMonitorServerByName(monitorServerName);

			if (monitorServer == null) {
				monitorServer = monitorService
						.createMonitorServer(monitorServerName);
			}

			device.getMonitorServers().add(monitorServer); // NOTE: The monitor
			// server will be
			// saved with
			// host->device
		}

		// Set hosts device - Device will be saved with host
		host.setDevice(device);

		/* Assign application Type ID */
        if (newHost) {
            if (applicationType == null || applicationType.length() == 0
                    || applicationType.equals(NAGIOS)) {
                // If its a new host then app type defaults to Nagios if an
                // application type is not provided.
                host.setApplicationType(metadataService
                        .getApplicationTypeByName(NAGIOS));
            } else {
                // Lookup application Type ID
                try {
                    ApplicationType userApp = metadataService
                            .getApplicationTypeByName(applicationType);
                    if (userApp == null) {
                        host.setApplicationType(metadataService
                                .getApplicationTypeByName(NAGIOS));
                        log.warn("Application Type [" + applicationType
                                + "] doesn't exist use default NAGIOS");
                    } else {
                        host.setApplicationType(userApp);
                    }
                } catch (CollageException ce) // Default to nagios
                {
                    host.setApplicationType(metadataService
                            .getApplicationTypeByName(NAGIOS));
                    log.warn("Application Type [" + applicationType
                            + "] doesn't exist use default NAGIOS");
                }
            }
		}
        // check owning application type
        boolean owner = ((applicationType == null) || applicationType.isEmpty() ||
                (host.getApplicationType() == null) || applicationType.equals(host.getApplicationType().getName()));

        // update description/agent id
        if (owner) {
            if (hostDescription != null)
                host.setDescription(hostDescription.isEmpty() ? null : hostDescription);
            if (agentId != null && agentId.length() > 0)
                host.setAgentId(agentId);
        }

		// Add / update host status, if necessary - Note: All remaining
		// attributes are considered to be
		// HostStatus attributes
		HostStatus hostStatus = host.getHostStatus();
		if (hostStatus == null) {
			// Host status is automatically set to pending and related to the
			// host
			hostStatus = hostService.createHostStatus(applicationType, host);
            host.setHostStatus(hostStatus);
		}
        if (owner) {
            // update all status and properties if owner
            if (lastCheckTime != null) {
                Date date = new Date(Long.parseLong(lastCheckTime));
                hostStatus.setLastCheckTime(date);
            }
            if (nextCheckTime != null) {
                Date date = new Date(Long.parseLong(nextCheckTime));
                hostStatus.setNextCheckTime(date);
            }
            String stateType = hostAttributes.get(CollageAdminInfrastructure.PROP_STATE_TYPE);
            if (stateType != null) {
                StateType st = metadataService.getStateTypeByName(stateType);
                if (st != null)
                    hostStatus.setStateType(st);
            }
            hostAttributes.remove(CollageAdminInfrastructure.PROP_STATE_TYPE);
            if (hostStatus.getStateType() == null) {
                hostStatus.setStateType(metadataService.getStateTypeByName("UNKNOWN"));
            }
            String checkType = hostAttributes.get(CollageAdminInfrastructure.PROP_CHECK_TYPE);
            if (checkType != null) {
                CheckType ct = metadataService.getCheckTypeByName(checkType);
                if (ct != null)
                    hostStatus.setCheckType(ct);
            }
            hostAttributes.remove(CollageAdminInfrastructure.PROP_CHECK_TYPE);
            if (hostStatus.getCheckType() == null) {
                hostStatus.setCheckType(metadataService.getCheckTypeByName("ACTIVE"));
            }
            // merge last plugin output when owner
            hostAttributes.remove(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
            updateOwnerHostStatusMessage(host, lastPluginOutput);
            // set last state change if provided and state changed
            String lastStateChange = hostAttributes.get(CollageAdminInfrastructure.PROP_LAST_STATE_CHANGE);
            if ((lastStateChange != null) &&
                    (!newHost && (lastMonitorStatus != null) && lastMonitorStatus.equals(monitorStatus))) {
                hostAttributes.remove(CollageAdminInfrastructure.PROP_LAST_STATE_CHANGE);
            }
            if (hostAttributes.size() > 0) {
                hostStatus.setProperties(hostAttributes);
            }
            if (host != null) {
                host.setLastMonitorStatus(lastMonitorStatus);
            }
        } else {
            // update only status message if not owner
            updateNotOwnerHostStatusMessage(host, applicationType, monitorStatus, lastPluginOutput);
        }
        hostService.saveHost(host);
        if (newHost && (hostIdentity != null)) {
            hostIdentity.setHost(host);
            hostIdentityService.saveHostIdentity(hostIdentity);
        }
		stopMetricsTimer(timer);
		return host;
	}

    /**
     * Merge last plugin output when owner and non-owner status appear
     * in the output; for example:
     * VEMA:ds:///vmfs/volumes/26d73f43-969f8f19/ NETAPP:UP:UP
     * See non-owner merge below, (owner output always appears first).
     *
     * @param host update host
     * @param lastPluginOutput update status message
     */
    private void updateOwnerHostStatusMessage(Host host, String lastPluginOutput) {
        HostStatus hostStatus = host.getHostStatus();
        if ((lastPluginOutput != null) && !lastPluginOutput.isEmpty()) {
            String output = (String)hostStatus.getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
            output = (((output != null) && !output.isEmpty()) ? output : "");
            if (output.startsWith(host.getApplicationType().getName() + ":")) {
                Pattern pattern = Pattern.compile("(" + host.getApplicationType().getName() + ":.*?)(?:(?: (?!DOWN:)[A-Z]+:)|$)");
                Matcher matcher = pattern.matcher(output);
                if (matcher.find() && (matcher.start(1) == 0)) {
                    output = host.getApplicationType().getName() + ":" + lastPluginOutput + output.substring(matcher.end(1));
                } else {
                    output = host.getApplicationType().getName() + ":" + lastPluginOutput;
                }
            } else {
                output = lastPluginOutput;
            }
            hostStatus.setProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, output);
        }
    }

    /**
     * Merge last plugin output if not owner, (assume application type
     * is valid and host application type is set), merge last plugin output
     * since owner and non-owner status appear in the output; for example:
     * VEMA:ds:///vmfs/volumes/26d73f43-969f8f19/ NETAPP:UP:UP
     * See owner merge above, (owner output always appears first).
     *
     * @param host update host
     * @param applicationType update application type
     * @param monitorStatus update monitor status
     * @param lastPluginOutput update status message
     */
    private void updateNotOwnerHostStatusMessage(Host host, String applicationType, String monitorStatus, String lastPluginOutput) {
        HostStatus hostStatus = host.getHostStatus();
        if (((monitorStatus != null) && !monitorStatus.isEmpty()) || ((lastPluginOutput != null) && !lastPluginOutput.isEmpty())) {
            lastPluginOutput = applicationType+ ":" +
                    ((monitorStatus != null) ? monitorStatus : "") + ":" +
                    ((lastPluginOutput != null) ? lastPluginOutput : "");
            String output = (String)hostStatus.getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT);
            output = (((output != null) && !output.isEmpty()) ? output : "");
            Pattern pattern = Pattern.compile("(" + applicationType + ":.*?)(?:(?: (?!DOWN:)[A-Z]+:)|$)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                output = output.substring(0, matcher.start(1)) + lastPluginOutput + output.substring(matcher.end(1));
            } else if (output.startsWith(host.getApplicationType().getName() + ":")) {
                output = output + " " + lastPluginOutput;
            } else if (!output.isEmpty()) {
                output = host.getApplicationType().getName() + ":" + output + " " + lastPluginOutput;
            } else {
                output = lastPluginOutput;
            }
            hostStatus.setProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, output);
        }
    }

    /**
     * Warn and send hosts merge log message event. This method should be
     * invoked and the update aborted when a host host name and a lookup host
     * name are matching but differ by case only. Alias lookups for update
     * should not be considered merges.
     *
     * @param hostName host name lookup
     * @param matchedHostName host identity or host name
     * @param host hosts merge target or null
     * @param applicationType application type or null
     */
    private void sendHostsMergeMessage(String hostName, String matchedHostName, Host host, String applicationType) {
        String message = "Cannot update/merge hosts with matching names: " + hostName + " into " + matchedHostName;
        log.error(message);
        if (host != null) {
            try {
                if (applicationType == null) {
                    applicationType = host.getApplicationType().getName();
                }
                String deviceIdentification = host.getDevice().getIdentification();
                String severity = CollageSeverity.WARNING.name();
                Properties logMessageProperties = new Properties();
                logMessageProperties.put(LogMessage.EP_HOST_STATUS_ID, host.getHostStatus());
                logMessageProperties.put(LogMessage.EP_MONITOR_STATUS_NAME, "UNKNOWN");
                logMessageProperties.put(LogMessage.KEY_CONSOLIDATION, SYSTEM_CONSOLIDATION);
                updateLogMessage(DEFAULT_MONITOR_SERVER, applicationType, deviceIdentification, severity, message,
                        host.getDevice(), host, null, logMessageProperties);
            } catch (Exception e) {
                log.error("Cannot log update/merge hosts message: " + e, e);
            }
        }
    }

	/**
	 * Execute command list in one transaction.
	 */
	public void executeCommands(List<CollageCommand> commandList) {
		if (commandList == null)
			throw new IllegalArgumentException(
					"Invalid null Command parameter.");

		// Note: We go through the factory to get a reference to the bean in
		// order
		// for the AOP / AfterAdvice to work properly. If we just call the
		// methods
		// locally the AfterAdvice will not be called.
		CollageAdminInfrastructure admin = (CollageAdminInfrastructure) (_collage)
				.getAPIObject("com.groundwork.collage.CollageAdmin");

		Iterator<CollageCommand> itCommands = commandList.iterator();
		CollageCommand cmd = null;
		String action = null;
		while (itCommands.hasNext()) {
			cmd = itCommands.next();
			action = cmd.getAction();
			log.debug("CollageAminImpl.executeCommands: action [" + action
					+ "]");
			if (action == null || action.length() == 0) {
				// Note: We are just ignoring the invalid command and continuing
				// to process other commands.
				log
						.warn("excuteCommands() - Command cannot be performed.  No Action defined!");
				continue;
			}

			long commandStart = 0;
			int threadID = 0;
			if (log.isInfoEnabled()) {
				commandStart = System.currentTimeMillis();
				threadID = this.hashCode();
				log.info("Command start. ObjectID[" + threadID + "]");
			}

			if (action.equalsIgnoreCase(ADMIN_ACTION_REMOVE)) {
				removeEntities(admin, cmd);
			} else if (action.equalsIgnoreCase(ADMIN_ACTION_ADD)) {
				addEntities(admin, cmd);
			} else if (action.equalsIgnoreCase(ADMIN_ACTION_MODIFY)) {
				modifyEntities(admin, cmd);
			} else if (action.equalsIgnoreCase(ADMIN_ACTION_CLEAR)) {
				clearEntities(admin, cmd);
			} else
			// Note: We are just ignoring the invalid command and
			// continuing to process other commands.
			{
				log
						.warn("excuteCommands() - Command cannot be performed.  Unknown Action - "
								+ action);
			}
			if (log.isInfoEnabled()) {
				log.info("Command execution complete. ObjectID[" + threadID
						+ "] took "
						+ (System.currentTimeMillis() - commandStart) + " ms");
			}
		}
	}

	/*************************************************************************/
	/*
	 * Private Methods /
	 */

	/**
	 * Performs remove command
	 */
	private void removeEntities(CollageAdminInfrastructure admin,
			CollageCommand cmd) {
		CollageTimer timer = startMetricsTimer();
		if (cmd == null
				|| (cmd.getAction().equalsIgnoreCase(ADMIN_ACTION_REMOVE) == false))
			return;

		// Nothing to do if there are no entities listed.
		List<CollageEntity> entityList = cmd.getEntities();
		if (entityList == null || entityList.size() == 0)
			return;

		// Go through each entity in the remove command
		Iterator<CollageEntity> itEntities = entityList.iterator();
		CollageEntity entity = null;
		String type = null;
		while (itEntities.hasNext()) {
			entity = itEntities.next();
			type = entity.getType();

			// Host Remove
			if (type.equalsIgnoreCase(ADMIN_TYPE_HOST)) {
				// Use host id to delete if it is provided.
				Host host = hostIdentityService.getHostByIdOrHostName(entity.getProperty(PROP_HOST_NAME));
				if (host == null) {
					log.warn("Host [" + entity.getProperty(PROP_HOST_NAME)
							+ "] doesn't exist");
				} else {
                    // save host groups for change propagation
                    Set<HostGroup> hostHostGroups = ((host.getHostGroups() != null) ?
                            new HashSet<HostGroup>(host.getHostGroups()) : Collections.EMPTY_SET);

					String id = entity.getProperty(PROP_HOST_ID);
                    Integer hostID;
					if (id == null || id.length() == 0) {
						hostID = admin.removeHost(host.getHostName());
					} else {
						hostID = admin.removeHost(Integer.parseInt(id));
					}

                    // Now remove host from host category as well.
                    if (hostID != null && hostID > 0) {
                        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOST, hostID);
                    }
                    // propagate changes
                    if (!hostHostGroups.isEmpty()) {
                        admin.propagateHostChangesToHostGroup(hostHostGroups);
                    }
                }
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS)) {

				// Use service id to delete if it is provided.
				String id = entity.getProperty(PROP_SERVICE_ID);
				Host host = hostIdentityService.getHostByIdOrHostName(entity.getProperty(PROP_HOST_NAME));
				if (id == null || id.length() == 0) {
                    if (host != null) {
                        Integer serviceID = admin.removeService(host.getHostName(),
                                entity.getProperty(PROP_SERVICE_DESC));

                        // Now remove the service from service group/category as well.
                        if (serviceID != null && serviceID > 0) {
                            admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS, serviceID);
                        }
                        // propagate changes
                        admin.propagateServiceChangesToHost(host);
                        admin.propagateHostChangesToHostGroup(host);
                    } else {
                        log.warn("Host [" + entity.getProperty(PROP_HOST_NAME)
                                + "] doesn't exist");
                    }
				} else {
					admin.removeService(Integer.parseInt(id));
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_HOSTGROUP)) {
				List<CollageEntity> hostEntities = entity.getSubEntities();

				// TODO: Update HostGroup By ID
				// If there are no hosts for the host group then we are removing
				// the host group
				if (hostEntities == null || hostEntities.size() == 0) {
					String id = entity.getProperty(PROP_HOSTGROUP_ID);
					if (id == null || id.length() == 0) {
                        Integer hostGroupID = admin.removeHostGroup(entity.getProperty(PROP_HOSTGROUP_NAME));

                        // Now remove the host group from custom group as well.
                        if (hostGroupID > 0) {
                            admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP, hostGroupID);
                        }
					} else {
						admin.removeHostGroup(Integer.parseInt(id));
					}
				}
				// If there are hosts then we remove the hosts that are listed
				else {
					List<String> hostNames = new ArrayList<String>(hostEntities
							.size());
					Iterator<CollageEntity> itHostEntities = hostEntities
							.iterator();
					String host = null;
					while (itHostEntities.hasNext()) {
						host = itHostEntities.next()
								.getProperty(PROP_HOST_NAME);
						if (host != null && host.length() > 0)
							hostNames.add(host);
					}

					admin.removeHostsFromHostGroup(entity
							.getProperty(PROP_HOSTGROUP_NAME), hostNames);
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_DEVICE)) {
				List<CollageEntity> deviceEntities = entity.getSubEntities();

				// If there are no devices for the device then we are removing
				// the device
				if (deviceEntities == null || deviceEntities.size() == 0) {
					String id = entity.getProperty(PROP_DEVICE_ID);
					if (id == null || id.length() == 0) {
						admin.removeDevice(entity
								.getProperty(PROP_DEVICE_IDENTIFICATION));
					} else {
						admin.removeDevice(Integer.parseInt(id));
					}
				}
				// If there are devices then we remove the parent and /or child
				// devices
				else {
					Iterator<CollageEntity> itDeviceEntities = deviceEntities
							.iterator();
					CollageEntity deviceEntity = null;
					String subType = null;
					List<String> childDevices = new ArrayList<String>();
					List<String> parentDevices = new ArrayList<String>();

					// Build up list of child and / or parent devices
					while (itDeviceEntities.hasNext()) {
						deviceEntity = itDeviceEntities.next();
						subType = deviceEntity.getType();

						// Remove parent device
						if (subType.equalsIgnoreCase(ADMIN_TYPE_DEVICE_PARENT)) {
							parentDevices.add(deviceEntity
									.getProperty(PROP_DEVICE_IDENTIFICATION));

						} // Remove child device
						else if (subType
								.equalsIgnoreCase(ADMIN_TYPE_DEVICE_CHILD)) {
							childDevices.add(deviceEntity
									.getProperty(PROP_DEVICE_IDENTIFICATION));
						}
					}

					String deviceIdentification = entity
							.getProperty(PROP_DEVICE_IDENTIFICATION);

					if (parentDevices.size() > 0)
						admin.removeDevicesFromChildDevice(
								deviceIdentification, parentDevices);

					if (childDevices.size() > 0)
						admin.removeDevicesFromParentDevice(
								deviceIdentification, childDevices);
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_MONITOR_SERVER)) {
				List<CollageEntity> deviceEntities = entity.getSubEntities();

				// If there are no devices for the device then we are removing
				// the monitor server
				if (deviceEntities == null || deviceEntities.size() == 0) {
					// TODO: MonitorServer removal is NOT IMPLEMENTED - NEED TO
					// ADD FUNCTIONALITY TO CollageAdmin
					throw new CollageException(
							"Collage Admin - Removal of MonitorServer is not implemented.");
				}
				// If there are devices then we remove the parent and /or child
				// devices
				else {
					Iterator<CollageEntity> itDeviceEntities = deviceEntities
							.iterator();
					CollageEntity deviceEntity = null;
					List<String> devices = new ArrayList<String>(deviceEntities
							.size());

					// Build up list device identifications
					String deviceIdent = null;
					while (itDeviceEntities.hasNext()) {
						deviceEntity = itDeviceEntities.next();
						if (deviceEntity != null) {
							deviceIdent = deviceEntity
									.getProperty(PROP_DEVICE_IDENTIFICATION);

							if (deviceIdent != null && deviceIdent.length() > 0)
								devices.add(deviceIdent);
						}
					}

					admin.removeDevicesFromMonitorServer(entity
							.getProperty(PROP_MONITOR_SERVER), devices);
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_CONSOLIDATION)) {
				String id = entity.getProperty(PROP_CONSOLIDATION_ID);
				if (id == null || id.length() == 0) {
					String name = entity.getProperty(PROP_CONSOLIDATION_NAME);

					admin.removeConsolidationCriteria(name);
				} else {
					admin.removeConsolidationCriteria(Integer.parseInt(id));
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICEGROUP)) {
				List<CollageEntity> serviceEntities = entity.getSubEntities();
				// If there are no services for the service group then we are
				// removing the service group
				if (serviceEntities == null || serviceEntities.size() == 0) {
					String id = entity.getProperty(PROP_SERVICEGROUP_ID);
					if (id == null || id.length() == 0) {
                        Category serviceGroup = admin.removeCategory(entity.getProperty(PROP_SERVICEGROUP_NAME),
                                CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);

                        // Now remove the service group from custom group as well.
                        if (serviceGroup != null) {
                            admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
                                    serviceGroup.getID());
                        }
                    } else {
                        admin.removeCategory(Integer.parseInt(id));
                    }
				}
				// If there are services then we remove the checked services
				else {
					List<String> serviceNames = new ArrayList<String>(
							serviceEntities.size());
					Iterator<CollageEntity> itServiceEntities = serviceEntities
							.iterator();
					String service = null;
					String hostName = null;
					while (itServiceEntities.hasNext()) {
						CollageEntity cEntity = itServiceEntities.next();
						service = cEntity.getProperty(PROP_SERVICE_DESC);
						hostName = cEntity.getProperty(PROP_HOST_NAME);
						if (service != null && service.length() > 0
								& hostName != null && hostName.length() > 0) {
                            ServiceStatus serviceStatus =
                                    hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service, hostName);
							Integer sID = serviceStatus.getServiceStatusId();
							String serviceStatusID = sID.toString();

							admin.removeCategoryEntity(
                                    entity.getProperty(PROP_SERVICEGROUP_NAME),
                                    CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
                                    CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
                                    serviceStatusID);
						}
					}
				}
			} else {
				throw new CollageException(
						"Collage Admin - remove not implemented for entity type - "
								+ type);
			}
		}
		stopMetricsTimer(timer);
	}

	/**
	 * Performs add command
	 * 
	 * @param cmd
	 */
	private void addEntities(CollageAdminInfrastructure admin,
			CollageCommand cmd) {
		CollageTimer timer = startMetricsTimer();
		if (cmd == null
				|| (cmd.getAction().equalsIgnoreCase(ADMIN_ACTION_ADD) == false))
			return;

		// Go through each entity in the add command
		List<CollageEntity> entityList = cmd.getEntities();
		if (entityList == null || entityList.size() == 0)
			return;

		Iterator<CollageEntity> itEntities = entityList.iterator();
		CollageEntity entity = null;
		String type = null;

		while (itEntities.hasNext()) {
			entity = itEntities.next();
			type = entity.getType();
			log.debug("CollageAdminImpl.addEntitities type [" + type + "] ");
			// Host Add
			if (type.equalsIgnoreCase(ADMIN_TYPE_HOST)) {
				/* ApplicationType for Host needs to be specified otherwise it defaults to NAGIOS*/
				String applicationTypeForHost = cmd.getApplicationType();
				if (applicationTypeForHost != null)
					entity.addProperty(PROP_APPLICATION_TYPE_NAME, applicationTypeForHost);
				admin.addOrUpdateHost(entity.getProperties());
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS)) {
				// Add or update service status
				// Note: We are removing the properties that we pass in
				// explicitly
				// so they are not considered dynamic properties.
				admin.updateServiceStatus(entity
						.removeProperty(PROP_MONITOR_SERVER), cmd
						.getApplicationType(), entity
						.removeProperty(PROP_HOST_NAME), entity
						.removeProperty(PROP_DEVICE_IDENTIFICATION),
                        (String)null,
                        entity.getProperties());

			} else if (type.equalsIgnoreCase(ADMIN_TYPE_HOSTGROUP)) {
				List<CollageEntity> hostEntities = entity.getSubEntities();
				List<String> hostNames = null;

				// If there are hosts then we add them to the host group
				if (hostEntities != null && hostEntities.size() > 0) {
					hostNames = new ArrayList<String>(hostEntities.size());
					Iterator<CollageEntity> itHostEntities = hostEntities
							.iterator();
					String host = null;
					while (itHostEntities.hasNext()) {
						host = itHostEntities.next()
								.getProperty(PROP_HOST_NAME);
						if (host != null && host.length() > 0)
							hostNames.add(host);
					}
				}

				String description = entity
						.getProperty(PROP_HOSTGROUP_DESCRIPTION);
				String alias = entity.getProperty(PROP_HOSTGROUP_ALIAS);
				// Create host group and add hosts
				admin.addHostsToHostGroup(cmd.getApplicationType(), entity
						.getProperty(PROP_HOSTGROUP_NAME), hostNames,
						description, alias);
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_DEVICE)) {
				List<CollageEntity> deviceEntities = entity.getSubEntities();

				// If there are no child / parent devices for the device then we
				// do nothing.
				// Device add is not supported and devices are always added
				// implicitly through a host add
				if (deviceEntities == null || deviceEntities.size() == 0) {
					log
							.warn("Collage Admin - No Device child or parents defined for Device Add Command.");
					continue;
				}

				// If there are devices then we remove the parent and /or child
				// devices
				Iterator<CollageEntity> itDeviceEntities = deviceEntities
						.iterator();
				CollageEntity deviceEntity = null;
				String subType = null;
				List<String> childDevices = new ArrayList<String>();
				List<String> parentDevices = new ArrayList<String>();

				// Build up list of child and / or parent devices
				while (itDeviceEntities.hasNext()) {
					deviceEntity = itDeviceEntities.next();
					subType = deviceEntity.getType();

					// Remove parent device
					if (subType.equalsIgnoreCase(ADMIN_TYPE_DEVICE_PARENT)) {
						parentDevices.add(deviceEntity
								.getProperty(PROP_DEVICE_IDENTIFICATION));

					} // Remove child device
					else if (subType.equalsIgnoreCase(ADMIN_TYPE_DEVICE_CHILD)) {
						childDevices.add(deviceEntity
								.getProperty(PROP_DEVICE_IDENTIFICATION));
					}
				}

				String deviceIdentification = entity
						.getProperty(PROP_DEVICE_IDENTIFICATION);

				if (parentDevices.size() > 0)
					admin.addDevicesToChildDevice(deviceIdentification,
							parentDevices);

				if (childDevices.size() > 0)
					admin.addDevicesToParentDevice(deviceIdentification,
							childDevices);
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_MONITOR_SERVER)) {
				List<CollageEntity> deviceEntities = entity.getSubEntities();

				// If there are no devices for the device then we are removing
				// the monitor server
				if (deviceEntities == null || deviceEntities.size() == 0) {
					// TODO: MonitorServer add is NOT IMPLEMENTED - NEED TO ADD
					// FUNCTIONALITY TO CollageAdmin
					throw new CollageException(
							"Collage Admin - Add of MonitorServer is not implemented.");
				}
				// If there are devices then we remove the parent and /or child
				// devices
				else {
					Iterator<CollageEntity> itDeviceEntities = deviceEntities
							.iterator();
					CollageEntity deviceEntity = null;
					List<String> devices = new ArrayList<String>(deviceEntities
							.size());

					// Build up list device identifications
					String deviceIdent = null;
					while (itDeviceEntities.hasNext()) {
						deviceEntity = itDeviceEntities.next();
						if (deviceEntity != null) {
							deviceIdent = deviceEntity
									.getProperty(PROP_DEVICE_IDENTIFICATION);

							if (deviceIdent != null && deviceIdent.length() > 0)
								devices.add(deviceIdent);
						}
					}

					admin.addDevicesToMonitorServer(entity
							.getProperty(PROP_MONITOR_SERVER), devices);
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_CONSOLIDATION)) {
				admin.addOrUpdateConsolidationCriteria(entity
						.getProperty(PROP_CONSOLIDATION_NAME), entity
						.getProperty(PROP_CRITERIA));
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_LOG_MESSAGE)) {
				/*
				 * JIRA GWMON-4905 TextMessage can be empty and should not fail.
				 * Make sure when the remove property returns null to initialize
				 * properly.
				 */
				String txtMsgInput = entity.removeProperty(PROP_TEXT_MESSAGE);
				if (txtMsgInput == null)
					txtMsgInput = "";

				// Check if we are running in test mode
				// JIRA GWMON-4482
				if (entity.removeProperty("test") != null) {
					StringBuilder msgTextMessage = new StringBuilder(
							txtMsgInput);
					msgTextMessage.append(" RBF=").append(
							System.currentTimeMillis()).append(" ");

					txtMsgInput = msgTextMessage.toString();
				}

				// Add or update log message
				// Note: We are removing the properties that we pass in
				// explicitly
				// so they are not considered dynamic properties.
				admin.updateLogMessage(entity
						.removeProperty(PROP_MONITOR_SERVER), cmd
						.getApplicationType(), entity
						.removeProperty(PROP_DEVICE_IDENTIFICATION), entity
						.removeProperty(PROP_SEVERITY), txtMsgInput, // entity
						// .
						// removeProperty
						// (
						// PROP_TEXT_MESSAGE
						// ),
						entity.getProperties());

			} else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICEGROUP)) {
				List<CollageEntity> serviceEntities = entity.getSubEntities();
				List<String> serviceNames = null;

				/* Get Service Group description */
				String sgDescription = entity
						.getProperty(PROP_SERVICEGROUP_DESCRIPTION);

				// If there are hosts then we add them to the host group
				if (serviceEntities != null && serviceEntities.size() > 0) {
					serviceNames = new ArrayList<String>(serviceEntities.size());
					Iterator<CollageEntity> itServiceEntities = serviceEntities
							.iterator();
					String service = null;
					String hostName = null;
					while (itServiceEntities.hasNext()) {
						CollageEntity cEntity = itServiceEntities.next();
						service = cEntity.getProperty(PROP_SERVICE_DESC);
						hostName = cEntity.getProperty(PROP_HOST_NAME);
                        ServiceStatus serviceStatus =
                                hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service, hostName);

						if (serviceStatus != null) {

							if (service != null && service.length() > 0) {

								serviceNames.add(service);
								// Create service group and add services
								// admin.addCategoryToParent(entity.getProperty(
								// PROP_SERVICEGROUP_NAME), service);
								// addCategoryEntity("ServiceGroup",
								// entity.getProperty(PROP_SERVICEGROUP_NAME),
								// "");
								Integer sID = serviceStatus
										.getServiceStatusId();
								String serviceStatusID = sID.toString();

								admin
										.addCategoryEntity(
												entity
														.getProperty(PROP_SERVICEGROUP_NAME),
												CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
                                                CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
												serviceStatusID, sgDescription);
							}
						} else {
							log
									.warn("Service ["
											+ service
											+ "] for host ["
											+ hostName
											+ "] does not exist. Can't be added to service group");
						}
					}

				} else
					admin.addCategoryEntity(entity
							.getProperty(PROP_SERVICEGROUP_NAME),
                            CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
							null, null,	sgDescription);
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_NAGIOS_LOG)) {
				this.processNagiosLogs(entity, admin);
			} // end if

			else {
				log
						.debug("Collage Admin - add not implemented for entity type - "
								+ type);
				throw new CollageException(
						"Collage Admin - add not implemented for entity type - "
								+ type);
			}
		}
		stopMetricsTimer(timer);
	}

	/**
	 * Processes the NAGIOS_LOG tags for acknowledge
	 * 
	 * @param entity
	 * @param admin
	 */
	private void processNagiosLogs(CollageEntity entity,
			CollageAdminInfrastructure admin) throws CollageException {
	    CollageTimer timer = startMetricsTimer();
		/**
		 * Pre-defined Fields / Attributes
		 */
		String TYPE_RULE = "TypeRule";
		String ACKNOWLEDGEDBY = "AcknowledgedBy";
		String ACKNOWLEDGE_COMMENT = "AcknowledgeComment";

		/* TypeRule field */
		String ACKNOWLEDGE = "ACKNOWLEDGE";
		String UNACKNOWLEDGE = "UNACKNOWLEDGE";
		String HOST_NAME = "Host";
		String SERVICE_DESCRIPTION = "ServiceDescription";
		String APPLICATION_TYPE = "ApplicationType";

		String typeRule = entity.getProperty(TYPE_RULE);
		if (typeRule != null
				&& ((typeRule.compareTo(ACKNOWLEDGE) == 0) || (typeRule
						.compareTo(UNACKNOWLEDGE) == 0))) {
			// update existing log message
			String host = entity.getProperty(HOST_NAME);
			String appType = entity.getProperty(APPLICATION_TYPE);
			String acknowledgedBy = entity.getProperty(ACKNOWLEDGEDBY);

			/* HostName is required in order to acknowledge messages */
			if ((typeRule.compareTo(ACKNOWLEDGE) == 0)
					&& (host == null || appType == null || acknowledgedBy == null)) {
				log
						.error("HostName, ApplicationType and AcknowledgedBy attributes are required to Acknowledge events.");
				return;
			}

			if ((typeRule.compareTo(UNACKNOWLEDGE) == 0)
					&& (host == null || appType == null)) {
				log
						.error("HostName and ApplicationType attributes are required to Un-Acknowledge events.");
				return;
			}

			/* Optional attributes */
			String serviceDescription = entity.getProperty(SERVICE_DESCRIPTION);
			String acknowledgeComment = entity.getProperty(ACKNOWLEDGE_COMMENT);

			log
					.info("Acknowledge message received by NAGIOS_LOG adapter. Host["
							+ host
							+ "] Service ["
							+ serviceDescription
							+ "] Type ["
							+ typeRule
							+ "] AckBy["
							+ acknowledgedBy
							+ "] AckComment ["
							+ acknowledgeComment + "]\n");

			admin.acknowledgeEvent(appType, typeRule, host, serviceDescription,
					acknowledgedBy, acknowledgeComment);
		} else {
			log
					.debug("Collage Admin - TypeRule is null.Not supported for entity type - "
							+ entity.getType());
			throw new CollageException(
					"Collage Admin - TypeRule is null.Not supported for entity type - "
							+ entity.getType());
		}
		stopMetricsTimer(timer);
	}

	/**
	 * Performs modify command
	 * 
	 * @param cmd
	 */
	private void modifyEntities(CollageAdminInfrastructure admin,
			CollageCommand cmd) {
		CollageTimer timer = startMetricsTimer();
		if (cmd == null
				|| (cmd.getAction().equalsIgnoreCase(ADMIN_ACTION_MODIFY) == false))
			return;

		// Go through each entity in the modify command
		List<CollageEntity> entityList = cmd.getEntities();
		if (entityList == null || entityList.size() == 0)
			return;

		Iterator<CollageEntity> itEntities = entityList.iterator();
		CollageEntity entity = null;
		String type = null;

		while (itEntities.hasNext()) {
			entity = itEntities.next();
			type = entity.getType();

			// Host Update
			if (type.equalsIgnoreCase(ADMIN_TYPE_HOST)) {
				long startTime = System.currentTimeMillis();

				String newMonitorStatus = translateNewHostMonitorStatus(entity
						.getProperties());

				// Note: This will also add any hosts that do no exist
				Host host = admin.addOrUpdateHost(entity.getProperties());
				if (host != null) {
					String oldMonitorStatus = host.getLastMonitorStatus();
					// Propagate only if there are is a change in monitorStatus
					if (oldMonitorStatus != null
							&& !oldMonitorStatus
									.equalsIgnoreCase(newMonitorStatus)) {
						admin.propagateHostChangesToHostGroup(host);
					} // end if
				}
				log.debug("****Host Updated in "
						+ (System.currentTimeMillis() - startTime) + " ms");
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS)) {
				long startTime = System.currentTimeMillis();
				// Get Service Id and make sure to remove
				String serviceId = entity.removeProperty(PROP_SERVICE_ID);
				if (serviceId != null && serviceId.length() > 0) {
					admin.updateServiceStatusByID(new Integer(serviceId),
							cmd.getApplicationType(), entity.getProperties());
				} else {
					// Add or update service status
					// Note: We are removing the properties that we pass in
					// explicitly
					// so they are not considered dynamic properties.
					String newMonitorStatus = translateNewServiceMonitorStatus(entity
							.getProperties());

					ServiceStatus serviceStatus = admin.updateServiceStatus(
							entity.removeProperty(PROP_MONITOR_SERVER), cmd
									.getApplicationType(), entity
									.removeProperty(PROP_HOST_NAME),
							entity.removeProperty(PROP_DEVICE_IDENTIFICATION),
                            (String)null,
							entity.getProperties());
					// Check the lastmonitorStatus from the DB and if it is not
					// same, then propagate.
					if (serviceStatus != null) {
						String oldMonitorStatus = serviceStatus
								.getLastMonitorStatus();
						if (oldMonitorStatus != null
								&& !oldMonitorStatus
										.equalsIgnoreCase(newMonitorStatus)) {
							// propagate service changes to service group
							admin
									.propagateServiceChangesToServiceGroup(serviceStatus);
							// propage service changes to host
							admin.propagateServiceChangesToHost(serviceStatus);

							// propagate host changes to hostgroup
							admin.propagateHostChangesToHostGroup(serviceStatus
									.getHost());
						} // end if
					} // end if
					log.debug("****Service Updated in "
							+ (System.currentTimeMillis() - startTime) + " ms");
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_HOSTGROUP)) {
				String hgName = entity.getProperty(PROP_HOSTGROUP_NAME);
				String alias = entity.getProperty(PROP_HOSTGROUP_ALIAS);
				String description = entity
						.getProperty(PROP_HOSTGROUP_DESCRIPTION);

				if (hgName == null || hgName.length() == 0)
					continue;

				List<CollageEntity> hostEntities = entity.getSubEntities();
				List<String> hostNames = null;

				if (hostEntities != null && hostEntities.size() > 0) {
					hostNames = new ArrayList<String>(hostEntities.size());
					Iterator<CollageEntity> itHostEntities = hostEntities
							.iterator();
					String host = null;
					while (itHostEntities.hasNext()) {
						host = itHostEntities.next()
								.getProperty(PROP_HOST_NAME);
						if (host != null && host.length() > 0)
							hostNames.add(host);
					}
				}

				// Update HostGroup and add / delete hosts to match hosts
				// specified
				admin.updateHostGroup(cmd.getApplicationType(), entity
						.getProperty(PROP_HOSTGROUP_NAME), hostNames, alias, description, null);
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_DEVICE)) {
				throw new CollageException(
						"Collage Admin - modify device is not implemented.");
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_MONITOR_SERVER)) {
				throw new CollageException(
						"Collage Admin - modify monitor server is not implemented.");

			} else if (type.equalsIgnoreCase(ADMIN_TYPE_CONSOLIDATION)) {
				// If id is provided we update by id otherwise we lookup by name
				String strId = entity.getProperty(PROP_CONSOLIDATION_ID);
				if (strId != null && strId.length() > 0) {
					admin.updateConsolidationCriteria(new Integer(Integer
							.parseInt(strId)), entity
							.getProperty(PROP_CONSOLIDATION_NAME), entity
							.getProperty(PROP_CRITERIA));
				} else {
					admin.addOrUpdateConsolidationCriteria(entity
							.getProperty(PROP_CONSOLIDATION_NAME), entity
							.getProperty(PROP_CRITERIA));
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_LOG_MESSAGE)) {
				// Get Log Message Id and make sure to remove from property list
				String msgId = entity.removeProperty(PROP_LOG_MESSAGE_ID);
				if (msgId == null || msgId.length() == 0) {
					log
							.warn("Unable to update log message without log message id.");
				} else {
					// If OperationStatus is the only property then we only
					// update operation status and avoid resetting
					// stateless hash
					Map<String, String> properties = entity.getProperties();
					if (properties != null && properties.size() > 0) {
						String opStatus = properties.get(PROP_OPERATION_STATUS);
						if (opStatus == null || properties.size() > 1) {
							admin.updateLogMessageByID(new Integer(msgId),
									properties);
						} else {
							admin.updateLogMessageOperationStatus(msgId,
									opStatus);
						}
					}
				}
			}

			else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICEGROUP)) {
				String sgName = entity.getProperty(PROP_SERVICEGROUP_NAME);
				if (sgName == null || sgName.length() == 0)
					continue;

				/* Get Service Group description */
				String sgDescription = entity
						.getProperty(PROP_SERVICEGROUP_DESCRIPTION);

				List<CollageEntity> serviceEntities = entity.getSubEntities();
				List<String> serviceNames = null;

				if (serviceEntities != null && serviceEntities.size() > 0) {
					serviceNames = new ArrayList<String>(serviceEntities.size());
					Iterator<CollageEntity> itServiceEntities = serviceEntities
							.iterator();
					String service = null;
					String hostName = null;
					while (itServiceEntities.hasNext()) {
						CollageEntity cEntity = itServiceEntities.next();
						service = cEntity.getProperty(PROP_SERVICE_DESC);
						hostName = cEntity.getProperty(PROP_HOST_NAME);

						if (service != null && service.length() > 0) {
                            ServiceStatus serviceStatus =
                                    hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service, hostName);
							if (serviceStatus != null) {
								Integer sID = serviceStatus
										.getServiceStatusId();
								String serviceStatusID = sID.toString();
								admin
										.addCategoryEntity(
												sgName,
                                                CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
												CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
												serviceStatusID, sgDescription);
							} else {
								log
										.warn("Cannot add invalid services to ServiceGroup");
							}
						}
					}
				} else {
					// Update description if available
					if (sgDescription != null) {
						Category cat = categoryService
								.getCategoryByName(sgName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
						if (cat != null) {
                            admin.updateCategory(cat.getCategoryId(), sgName, sgDescription);
						}
					}
				}
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_NAGIOS_LOG)) {
				this.processNagiosLogs(entity, admin);
			} else if (type.equalsIgnoreCase(ADMIN_TYPE_PERFORMANCE_DATA)) {

				/* Input validation */
				String hostName = entity.getProperty("hostname");
				String serviceDescription = entity
						.getProperty("servicedescription");
				String performanceDataLabel = entity
						.getProperty("performancedatalabel");
				String performanceValueStr = entity
						.getProperty("performancevalue");
				String checkDate = entity.getProperty("checkdate");

				double performanceValue = 0;
				try {
					performanceValue = new Double(performanceValueStr)
							.doubleValue();
				} catch (Exception e) {

					log
							.error("PerformanceValue parameter is not provided or is not a double. Value = "
									+ performanceValueStr);
				}

				if (hostName == null || serviceDescription == null
						|| performanceDataLabel == null
						|| performanceValueStr == null || checkDate == null) {

					log
							.error("Not all required values provided. Make sure that you define hostname, servicedescription, performancedatalabel, performancevalue and checkdate as parameters for the Performance Data feed");
				} else {
					/* Call into API */
					insertPerformanceData(hostName, serviceDescription,
							performanceDataLabel, performanceValue, checkDate);
				}
			}// end if
			else {
				throw new CollageException(
						"Collage Admin - modify not implemented for entity type - "
								+ type);
			}
		}
		stopMetricsTimer(timer);
	}

	/**
	 * Performs clear command.Clear just removes the associations.
	 * 
	 * @param cmd
	 */
	private void clearEntities(CollageAdminInfrastructure admin,
			CollageCommand cmd) {
		if (cmd == null
				|| (cmd.getAction().equalsIgnoreCase(ADMIN_ACTION_CLEAR) == false))
			return;

		// Go through each entity in the clear command
		List<CollageEntity> entityList = cmd.getEntities();
		if (entityList == null || entityList.size() == 0)
			return;

		Iterator<CollageEntity> itEntities = entityList.iterator();
		CollageEntity entity = null;
		String type = null;

		while (itEntities.hasNext()) {
			entity = itEntities.next();
			type = entity.getType();

			// HostGROUP Update
			if (type.equalsIgnoreCase(ADMIN_TYPE_HOSTGROUP)) {
				FoundationQueryList list = hostService.getHostsByHostGroupName(
						entity.getProperty(PROP_HOSTGROUP_NAME), null, null,
						-1, -1);
				List<Host> hosts = list.getResults();
				List<String> hostNames = new ArrayList<String>(hosts.size());
				for (Host host : hosts) {
					hostNames.add(host.getHostName());
				} // end if
				admin.removeHostsFromHostGroup(entity
						.getProperty(PROP_HOSTGROUP_NAME), hostNames);
			}

			// ServiceGROUP Update
			if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICEGROUP)) {
				Category category = categoryService.getCategoryByName(entity
						.getProperty(PROP_SERVICEGROUP_NAME),
                        CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
				int localCatID = category.getCategoryId().intValue();
				Collection<CategoryEntity> catEntities = category
						.getCategoryEntities();
				List<Integer> serviceStatusIDs = new ArrayList<Integer>(
						catEntities.size());
				for (CategoryEntity catEntity : catEntities) {
					Integer serviceStatusID = catEntity.getObjectID();
					if (serviceStatusID != null) {
						serviceStatusIDs.add(serviceStatusID);
					} // end if
				} // end for
				for (Integer objectID : serviceStatusIDs) {
					admin.removeCategoryEntity(category.getName(),
                            CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP,
                            CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
                            objectID.toString());
				} // end if
			} // end if
		}
	}

	// TODO: implement this as part of the API ?
	private Host getOrCreateHost(ApplicationType appType,
			String monitorServerName, String hostName, String deviceIdent, Device device) {
		CollageTimer timer = startMetricsTimer();
		if (appType == null)
			throw new IllegalArgumentException(
					"Invalid null ApplicationType parameter.");
        if ((device != null) && ((deviceIdent == null) || (deviceIdent.length() == 0))) {
            deviceIdent = device.getIdentification();
        }

		Host host = null;

		try {
            // lookup host identity/host if they already exist
            HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(hostName);
            if (hostIdentity != null) {
                // use host or host name from host identity
                if (hostIdentity.getHost() != null) {
                    host = hostIdentity.getHost();
                } else {
                    hostName = hostIdentity.getHostName();
                }
            } else {
                // fallback to host lookup
                host = hostService.getHostByHostName(hostName);
            }

            // create host if it is not found
			if (host == null) {
                if (device == null) {
                    device = this.getOrCreateDevice(monitorServerName, deviceIdent);
                }
				host = hostService.createHost(hostName, device);
				host.setApplicationType(appType);

				HostStatus hostStatus = host.getHostStatus();
				if (hostStatus == null) {
					// Host Status automatically set to pending and associated
					// with the host
					hostService.createHostStatus(appType.getName(), host);
				}

				hostService.saveHost(host);
                if (hostIdentity != null) {
                    hostIdentity.setHost(host);
                    hostIdentityService.saveHostIdentity(hostIdentity);
                }
			}
		} catch (Exception e) {
			String err = "CollageAdminAPI - getOrCreateHost failed for host '"
					+ hostName + "' on Device '" + deviceIdent
					+ "' monitored by '" + monitorServerName + "'";
			log.error(err, e);
			throw new CollageException(err, e);
		}
		stopMetricsTimer(timer);
		return host;
	}

	private Device getOrCreateDevice(String monitorServerName,
			String deviceIdent) {
		CollageTimer timer = startMetricsTimer();
		Device device = deviceService.getDeviceByIdentification(deviceIdent);

		if (device == null)
			device = deviceService.createDevice(deviceIdent, deviceIdent);

		if (monitorServerName == null || monitorServerName.length() == 0)
			monitorServerName = DEFAULT_MONITOR_SERVER;

		// create Monitor Server if need be
		MonitorServer monitor = monitorService
				.getMonitorServerByName(monitorServerName);

		if (monitor == null) {
			monitor = monitorService.createMonitorServer(monitorServerName);
			monitorService.saveMonitorServer(monitor);
		}

		// Add monitor to device instead of adding device to monitor server b/c
		// the later will
		// load all devices for the monitor server instead of just loading all
		// monitor servers for the device.
		device.getMonitorServers().add(monitor);

		// Save Device
		deviceService.saveDevice(device);
		stopMetricsTimer(timer);
		return device;
	}

    public void storeEventOperationalStatus(List<Integer> eventIds, String opStatus, String updatedBy, String comments)
        throws CollageException {
		CollageTimer timer = startMetricsTimer();
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure) (_collage)
                .getAPIObject("com.groundwork.collage.CollageAdmin");
        try {
            logMsgService.updateLogMessageOperationStatus(eventIds, opStatus, updatedBy, comments);
        }
        catch (Exception e) {
            throw new CollageException("Could not update some or all eventIds", e);
        }
        // determine notifications
        Map<Integer, List<Integer>> hosts = new HashMap<>();
        Map<Integer, List<Integer>> services = new HashMap<>();
        for (Integer id : eventIds) {
            if (id > 0) {
                LogMessage message = logMsgService.getLogMessageById(id);
                if (message != null) {
                    Integer hostId = (message.getHostStatus() != null)
                            ? message.getHostStatus().getHostStatusId() : null;
                    Integer statusId = (message.getServiceStatus() != null)
                            ? message.getServiceStatus().getServiceStatusId() : null;
                    if (statusId != null && statusId > 0) {
                        List<Integer> eventsPerStatus = services.get(statusId);
                        if (eventsPerStatus == null) {
                            eventsPerStatus = new ArrayList<>();
                        }
                        eventsPerStatus.add(id);
                        services.put(statusId, eventsPerStatus);
                    }
                    else if (hostId != null && hostId > 0) {
                        List<Integer> eventsPerHost = hosts.get(hostId);
                        if (eventsPerHost == null) {
                            eventsPerHost = new ArrayList<>();
                        }
                        eventsPerHost.add(id);
                        hosts.put(hostId, eventsPerHost);
                    }
                }
            }
        }
        // notify by Service
        for (Map.Entry<Integer, List<Integer>> entry : services.entrySet()) {
            admin.triggerAcknowledgeEventAOP((ArrayList) entry.getValue(), null, entry.getKey().toString());
            if (log.isDebugEnabled())
                log.debug("notifying service " + entry.getKey().toString() + " for # of services " + entry.getValue().size());
        }
        // notify by Host
        for (Map.Entry<Integer, List<Integer>> entry : hosts.entrySet()) {
            admin.triggerAcknowledgeEventAOP((ArrayList) entry.getValue(), entry.getKey().toString(), null);
            if (log.isDebugEnabled())
                log.debug("notifying host " + entry.getKey().toString() + " for # of hosts " + entry.getValue().size());
        }
        stopMetricsTimer(timer);
    }

    public void removeLogMessage(int id) throws CollageException {
        if (id > 0) {
            CollageAdminInfrastructure admin = (CollageAdminInfrastructure) (_collage)
                    .getAPIObject("com.groundwork.collage.CollageAdmin");
            LogMessage message = logMsgService.getLogMessageById(id);
            if (message != null) {
                logMsgService.removeLogMessage(id);
                if ((message.getHostStatus() != null) &&
                        (message.getHostStatus().getHost() != null) &&
                        (message.getHostStatus().getHost().getHostId() != null)) {
                    String hostId = message.getHostStatus().getHost().getHostId().toString();
                    List<Integer> ids = new ArrayList<>();
                    ids.add(id);
                    admin.triggerAcknowledgeEventAOP((ArrayList) ids, hostId, null);
                }
            }
        }
    }

    public Host renameHost(String oldHostName, String newHostName, String newDescription, String deviceIdentification)
            throws CollageException {
        try {
            Host dupeCheck = hostIdentityService.getHostByIdOrHostName(newHostName);
            if (dupeCheck != null) {
                throw new CollageException(DUPLICATE_ERROR);
            }
            Host host = hostIdentityService.getHostByIdOrHostName(oldHostName);
            if (host != null) {
                host.setHostName(newHostName);
                if (newDescription != null) {
                    host.setDescription(newDescription.isEmpty() ? null : newDescription);
                }
                hostIdentityService.renameHostIdentity(oldHostName, newHostName);
            } else {
                String errorMessage = "Attempting to rename host (oldHostName: " + oldHostName + ", newHostName: " + newHostName + ") failed. Host not found";
                log.error(errorMessage);
                return null;
            }
            host.setHostName(newHostName);
            if (newDescription != null) {
                host.setDescription(newDescription.isEmpty() ? null : newDescription);
            }
            hostService.saveHost(host);
            if (deviceIdentification != null) {
                Device device = deviceService.getDeviceByIdentification(host.getDevice().getIdentification());
                if (device != null) {
                    if (device.getDisplayName() == null || device.getDisplayName() != null && device.getIdentification().equals(device.getDisplayName())) {
                        device.setDisplayName(deviceIdentification);
                    }
                    device.setIdentification(deviceIdentification);
                    deviceService.saveDevice(device);
                }
            }
            return host;
        }
        catch (Exception e) {
            log.error(e);
            throw new CollageException(e.getMessage(), e);
        }
    }

    private String updateHostDownTimeStatus(String monitorStatus, String scheduledDowntimeDepth) {
        String newMonitorStatus = null;
        if (monitorStatus != null) {
			int depth = ((scheduledDowntimeDepth != null) ? Integer.parseInt(scheduledDowntimeDepth) : 0);
            if (monitorStatus.equalsIgnoreCase(MonitorStatusBubbleUp.DOWN)) {
				// Fix for GWPORTAL-33
                if (depth == 0) {
                    newMonitorStatus = MonitorStatusBubbleUp.UNSCHEDULED_DOWN;
                } else {
                    newMonitorStatus = MonitorStatusBubbleUp.SCHEDULED_DOWN;
                }
            } else if (monitorStatus.equalsIgnoreCase(MonitorStatusBubbleUp.UNSCHEDULED_DOWN)) {
				// GWMON-12236 (2015-08-31)
				if (depth > 0) {
					newMonitorStatus = MonitorStatusBubbleUp.SCHEDULED_DOWN;
				}
            }
        }
        return newMonitorStatus;
    }

    private String updateServiceDownTimeStatus(String monitorStatus, String scheduledDowntimeDepth) {
        String newMonitorStatus = null;
		if (monitorStatus != null) {
			int depth = ((scheduledDowntimeDepth != null) ? Integer.parseInt(scheduledDowntimeDepth) : 0);
			if (monitorStatus.equalsIgnoreCase(MonitorStatusBubbleUp.CRITICAL)) {
				// Fix for GWPORTAL-33
				if (depth == 0) {
					newMonitorStatus = MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL;
				} else {
					newMonitorStatus = MonitorStatusBubbleUp.SCHEDULED_CRITICAL;
				}
			} else if (monitorStatus.equalsIgnoreCase(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL)) {
				// GWMON-12236 (2015-08-31)
				if (depth > 0) {
					newMonitorStatus = MonitorStatusBubbleUp.SCHEDULED_CRITICAL;
				}
			}
		}
        return newMonitorStatus;
    }

    /**
     * Helper to translate the newMonitorStatus
     *
     * @param serviceProperties
     * @return
     */
    public String translateNewServiceMonitorStatus(Map<String,String> serviceProperties) {
        String monitorStatus = serviceProperties.get(PROP_MONITOR_STATUS);
        String scheduledDowntimeDepth = serviceProperties.get(PROP_SCHEDULED_DOWNTIME_DEPTH);
        if (scheduledDowntimeDepth != null) {
            String result = updateServiceDownTimeStatus(monitorStatus, scheduledDowntimeDepth);
            return (result == null) ? monitorStatus : result;
        }
        return monitorStatus;
    }

    /**
     * Helper to translate the newMonitorStatus
     *
     * @param hostProperties
     * @return
     */
    public String translateNewHostMonitorStatus(Map<String,String> hostProperties) {
        String monitorStatus = hostProperties.get(PROP_MONITOR_STATUS);
        String scheduledDowntimeDepth = hostProperties.get(PROP_SCHEDULED_DOWNTIME_DEPTH);
        if (scheduledDowntimeDepth != null) {
            String result = updateHostDownTimeStatus(monitorStatus, scheduledDowntimeDepth);
            return (result == null) ? monitorStatus : result;
        }
        return monitorStatus;
    }

} // end class CollageAdminImpl
