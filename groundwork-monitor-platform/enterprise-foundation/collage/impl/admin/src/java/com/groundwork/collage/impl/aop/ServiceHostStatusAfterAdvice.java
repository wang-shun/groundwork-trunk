/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.impl.aop;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rogerrut
 * 
 */
public class ServiceHostStatusAfterAdvice implements AfterReturningAdvice {
	StatisticsService statisticsService = null;

	private static final String METHOD_UPDATE_SERVICE_STATUS = "updateServiceStatus";
	private static final String METHOD_STATUS = "Status";
	private static final String METHOD_CREATE_HOSTS = "addOrUpdateHosts";
	private static final String METHOD_CREATE_HOST_LIST = "addOrUpdateHostList";
	private static final String METHOD_CREATE_HOST = "addOrUpdateHost";
	private static final String METHOD_REMOVE_HOST = "removeHost";
	private static final String METHOD_REMOVE_SERVICE = "removeService";
	private static final String METHOD_RENAME_HOST = "renameHost";
	private static final String METHOD_PROPAGATE_SERVICECHANGES_TO_HOST = "propagateServiceChangesToHost";

	Log log = LogFactory.getLog(this.getClass());

	public ServiceHostStatusAfterAdvice(StatisticsService statService) {
		statisticsService = statService;
	}

	public void afterReturning(Object returnValue, Method method,
			Object[] arguments, Object target) throws Throwable {
		// Nothing to do with methods that have no arguments
		if (arguments == null || arguments.length == 0)
			return;

		// Host Name
		String host = null;

		// Send notification to Statistics Service
		ServiceNotify notify = null;

		String methodName = method.getName();
		
		boolean isNotifyAndPublish = true;
        // Notification Attributes
        Map<String, Object> notifyAttributes = new HashMap<String, Object>(2);

		// TODO: This mechanism of checking method name and arguments needs to
		// be reworked
		// its too brittle and error prone.
		if (methodName.equalsIgnoreCase(METHOD_UPDATE_SERVICE_STATUS)) {
            if (returnValue == null)
                return; // dont notify on no update

			if (arguments.length > 6)
                host = (String) arguments[1];
			else if (arguments.length >= 5)
				host = (String) arguments[2];
			else {
                host = (arguments.length == 3) ? (String) arguments[0] : (String) arguments[2];
            }

			if (host == null || host.length() == 0) {
				log.warn("AOP Advisor ServiceHostStatus: Status method with Null / Empty Host Name.");
				return;
			}

			// Add Host Name
			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, host);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST,
					ServiceNotifyAction.UPDATE, notifyAttributes);
		} else if (methodName.indexOf(METHOD_STATUS) > 0) {
			if ((method.toGenericString().indexOf("java.util.Collection") > 0)
					|| (method.toGenericString().indexOf("java.util.Map") > 0)
					|| (method.toGenericString()
							.indexOf("java.util.Properties") > 0)) {

				host = (arguments.length > 2) ? (String) arguments[2] : null;
			} else {
				host = (arguments.length > 1) ? (String) arguments[1] : null;
			}

			if (host == null || host.length() == 0) {
				log
						.warn("AOP Advisor ServiceHostStatus: Status method with Null / Empty Host Name.");
				return;
			}

			// Add Host Name
			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME,
					host);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST,
					ServiceNotifyAction.UPDATE, notifyAttributes);
		} else if (methodName.equalsIgnoreCase(METHOD_CREATE_HOSTS)) {
			List<Hashtable<String, String>> hostEntries = (List<Hashtable<String, String>>) arguments[0];

			if (hostEntries == null || hostEntries.size() == 0)
				return; // Nothing to do

			// Build up list of host names
			List<String> hostNames = new ArrayList<String>(hostEntries.size());
			Iterator<Hashtable<String, String>> itEntries = hostEntries
					.iterator();
			while (itEntries.hasNext()) {
				host = itEntries.next().get(
						CollageAdminInfrastructure.PROP_HOST_NAME);

				if (host != null && host.length() > 0) {
					hostNames.add(host);
				}
			}

			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_HOST_LIST, hostNames);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.UPDATE, notifyAttributes);

        } else if (methodName.equalsIgnoreCase(METHOD_CREATE_HOST_LIST)) {
            List<Map<String, String>> hostEntries = (List<Map<String, String>>) arguments[0];

            if (hostEntries == null || hostEntries.size() == 0)
                return; // Nothing to do

            // Build up list of host names
            List<String> hostNames = new ArrayList<String>(hostEntries.size());
            Iterator<Map<String, String>> itEntries = hostEntries.iterator();
            while (itEntries.hasNext()) {
                host = itEntries.next().get(CollageAdminInfrastructure.PROP_HOST_NAME);
                if (host != null && host.length() > 0) {
                    hostNames.add(host);
                }
            }

            notifyAttributes.put(StatisticsService.NOTIFY_ATTR_HOST_LIST,hostNames);
            notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.UPDATE, notifyAttributes);

		} else if (methodName.equalsIgnoreCase(METHOD_CREATE_HOST)) {
            if (returnValue == null)
                return;  // dont notify on no update

            Map<String, String> hostEntry = (Map<String, String>) arguments[arguments.length-1];

			if (hostEntry == null || hostEntry.size() == 0)
				return; // Nothing to do

			// Build up list of host names
			List<String> hostNames = new ArrayList<String>(1);

			host = hostEntry.get(CollageAdminInfrastructure.PROP_HOST_NAME);
			if (host != null && host.length() > 0) {
				hostNames.add(host);
			}

			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_HOST_LIST, hostNames);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.UPDATE, notifyAttributes);
			if (returnValue != null)
			{
				Host hostObj = (Host) returnValue;
				if (hostObj != null)
				{
					MonitorStatus monitorStatus = hostObj.getHostStatus() .getHostMonitorStatus();
					String newMonitorStatus = (monitorStatus == null) ? MonitorStatusBubbleUp.UNKNOWN : monitorStatus.getName();
					String oldMonitorStatus = hostObj.getLastMonitorStatus();
					if (oldMonitorStatus != null && oldMonitorStatus.equalsIgnoreCase(newMonitorStatus))
					{
						isNotifyAndPublish = false;
					} // end if
				} // end if				
			}

		} else if (methodName.equalsIgnoreCase(METHOD_REMOVE_HOST) && arguments[0] instanceof String) {
            host = (String) arguments[0];

            if (host == null || host.length() == 0) {
                log.warn("AOP Advisor ServiceHostStatus: RemoveHost method with Null / Empty Host Name.");
                return;
            }

            // Add Host Name
            notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, host);
            notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.DELETE, notifyAttributes);
            statisticsService.notify(notify);
            this.publishEntity(notify, returnValue);
            return;

        } else if (methodName.equalsIgnoreCase(METHOD_RENAME_HOST)) {
            host = (String) arguments[0]; // NOTE: param one is the old host name since we are still in uncommitted tx

            if (host == null || host.length() == 0) {
                log.warn("AOP Advisor ServiceHostStatus: RenameHost method with Null / Empty Host Name.");
                return;
            }

            // Add Host Name
            notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, host);
            notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.UPDATE, notifyAttributes);
            statisticsService.notify(notify);
            this.publishEntity(notify, returnValue);
            notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.RENAME, notifyAttributes);
            statisticsService.notify(notify);
            this.publishEntity(notify, returnValue);
            return;

		} else if (methodName.equalsIgnoreCase(METHOD_REMOVE_SERVICE) && arguments[0] instanceof String) {
			host = (String) arguments[0];

			if (host == null || host.length() == 0) {
				log
						.warn("AOP Advisor ServiceHostStatus: RemoveService method with Null / Empty Host Name.");
				return;
			}

			// Add Host Name
			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, host);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST, ServiceNotifyAction.UPDATE, notifyAttributes);
		} else if (methodName.equalsIgnoreCase(METHOD_PROPAGATE_SERVICECHANGES_TO_HOST)) {
			if (returnValue != null && returnValue instanceof Host)
				host = ((Host) returnValue).getHostName();

			if (host == null || host.length() == 0) {
				log.warn("AOP Advisor ServiceHostStatus: propagateServiceChangesToHost method with Null / Empty Host Name.");
				return;
			}

			// Add Host Name
			notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME,
					host);
			notify = new ServiceNotify(ServiceNotifyEntityType.HOST,
					ServiceNotifyAction.UPDATE, notifyAttributes);
		} else {
			return; // No Match
		}
		statisticsService.notify(notify);
		// Notify Statistics Service
		try {
			if (isNotifyAndPublish)
			{
				// Now publish to the topic server only if host
                if (returnValue != null && returnValue instanceof List) {
                    publishListEntity(notify, (List<Host>) returnValue);
                }
				if (returnValue != null && returnValue instanceof Host) {
					log.info("About to publish the host changes to the topic server");
					this.publishEntity(notify, returnValue);
				} // end if
			} // end if
			log.info("AOP Advice -- Host [" + host + "] updated or deleted.");
		} catch (Exception e) {
			log.error(
					"AOP Advisor ServiceHostStatus: Failed to update/delete host - "
							+ host, e);
		}
	}

    private void publishListEntity(ServiceNotify notify, List<Host> hosts) {
        for (Host host : hosts) {
            publishEntity(notify, host);
            Set<HostGroup> groups = host.getHostGroups();
            HostGroupPublisher.publish(groups);
        }
    }

	/**
	 * Publishes the Host notifications only
	 * 
	 * @param notify
	 * @param returnValue
	 */
	private void publishEntity(ServiceNotify notify, Object returnValue) {
		log.info("Publishing Host....");
		if (returnValue == null)
			return;
		CollageFactory beanFactory = CollageFactory.getInstance();
		ConcurrentHashMap<String, String> distMap = beanFactory
				.getEntityPublisher().getDistinctEntityMap();
		int hostId = -1;
		if (returnValue != null && returnValue instanceof Host) {

			if (distMap != null) {
				if (returnValue != null) {
					hostId = ((Host) returnValue).getHostId().intValue();
				} // end if

			} // end if
		} else if (returnValue != null && returnValue instanceof Integer) {
			hostId = ((Integer) returnValue).intValue();

		} // end if
		StringBuffer sb = new StringBuffer();
		sb.append(notify.getAction());
		sb.append(":");
		sb.append(hostId);
		sb.append(";");
		String existingValue = null;
		if (distMap.get(ServiceNotifyEntityType.HOST.getValue()) != null) {
			existingValue = distMap
					.get(ServiceNotifyEntityType.HOST.getValue());
		}
		String currentValue = sb.toString();
		StringBuilder builder = new StringBuilder();
		// If the host is already in the list, don't add a duplicate one
		if (existingValue == null) {
			builder.append(currentValue);
		} else {
			if (existingValue.indexOf(currentValue) == -1) {
				builder.append(existingValue);
				builder.append(currentValue);
			} else {
				builder.append(existingValue);
			} // end if
		}
		distMap
				.put(ServiceNotifyEntityType.HOST.getValue(), builder
						.toString());

	}
}
