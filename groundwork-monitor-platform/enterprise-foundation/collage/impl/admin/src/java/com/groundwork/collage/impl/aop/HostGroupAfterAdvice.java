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

import com.groundwork.collage.model.HostGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author rogerrut
 * 
 */
public class HostGroupAfterAdvice implements AfterReturningAdvice {
	StatisticsService statisticsService = null;

	Log log = LogFactory.getLog(this.getClass());

	public HostGroupAfterAdvice(StatisticsService statService) {
		statisticsService = statService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang
	 * .Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	public void afterReturning(Object returnValue, Method method, Object[] arg2,
			Object arg3) throws Throwable {
		try {
			// Get the statistics module and call the notification
			String hostGroupName = null;

			// Send notification to Statistics Service
			ServiceNotify notify = null;
            Map<String, Object> notifyAttributes = new HashMap<String, Object>(3);

			if ((method.getName().equalsIgnoreCase("removeHostGroup"))
					|| (method.getName()
							.equalsIgnoreCase("removeApplicationGroup"))) {
				// Get the statistics module and call the notification
				hostGroupName = (String) arg2[0];
				// Add HostGroup Name
				notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME,
						hostGroupName);

				notify = new ServiceNotify(ServiceNotifyEntityType.HOSTGROUP,
						ServiceNotifyAction.DELETE, notifyAttributes);
				if (log.isInfoEnabled())
					log.info("AOP Advice -- HostGroup [" + hostGroupName
							+ "] removed.");
				// Notify Service
				statisticsService.notify(notify);
				HostGroupPublisher.publishHostGroup(notify, returnValue);
			} else if ((method.getName().equalsIgnoreCase("addHostsToHostGroup"))
					|| (method.getName().equalsIgnoreCase("updateHostGroup"))) {
				hostGroupName = (String) arg2[1];

				// Add HostGroup Name
				notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME,
						hostGroupName);

				notify = new ServiceNotify(ServiceNotifyEntityType.HOSTGROUP,
						ServiceNotifyAction.UPDATE, notifyAttributes);

				if (log.isInfoEnabled())
					log.info("AOP Advice -- HostGroup [" + hostGroupName
							+ "] updated.");
				// Notify Service
				statisticsService.notify(notify);
                HostGroupPublisher.publishHostGroup(notify, returnValue);
			} else if (method.getName().equalsIgnoreCase(
					"propagateHostChangesToHostGroup")) {
				Set<HostGroup> hostGroups = (Set) returnValue;
				if (hostGroups != null && hostGroups.size() > 0) {
					Iterator<HostGroup> iter = hostGroups.iterator();
					while (iter.hasNext()) {
						HostGroup hostGroup = iter.next();
						hostGroupName = hostGroup.getName();
						notifyAttributes.put(
								StatisticsService.NOTIFY_ATTR_ENTITY_NAME,
								hostGroupName);
						notify = new ServiceNotify(
								ServiceNotifyEntityType.HOSTGROUP,
								ServiceNotifyAction.UPDATE, notifyAttributes);
						statisticsService.notify(notify);
                        HostGroupPublisher.publishHostGroup(notify, hostGroup);
					} // end while
				} // end if
			}
		} catch (Exception e) {
			log.error("AOP Advisor HostGroup: Failed to extract host name. Error: ", e);
		}
	}


}
