/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundwork.agents.appservers.collector.impl;

import com.groundwork.agents.appservers.collector.api.GWOSCollectorService;
import com.groundwork.agents.appservers.utils.StatUtils;
import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class collects the performance data for JBoss
 * 
 * @author Arul Shanmugam
 * 
 */
public class GWOSTomcatCollectorService implements GWOSCollectorService {

	private static org.apache.log4j.Logger log = Logger.getLogger(GWOSTomcatCollectorService.class);

	private GWOSTomcatCollector tomcatCollector = null;

	private ScheduledExecutorService _scheduler = Executors
			.newScheduledThreadPool(1, new ThreadFactory() {
				public Thread newThread(Runnable task) {
					Thread thread = new Thread(task);
					thread.setName("gwos_tomcat_collector");
					thread.setDaemon(true);
					return thread;
				}
			});

	/**
	 * Shutsdown the collector service
	 */
	public void shutdown() {
		_scheduler.shutdownNow();
		if (tomcatCollector != null) {
			tomcatCollector.shutdown();
		}
	}

	/**
	 * Starts the collector Service
	 */
	public void start() {

		Properties properties = StatUtils.readProperties(TomcatAdminClient.TOMCAT_PROPERTIES);
		if (properties.size() > 0) {
			String execInterval = properties.getProperty("exec_interval");
			long interval = 60;
			if (execInterval != null && !execInterval.equalsIgnoreCase("")) {
				interval = Long.parseLong(execInterval);
			} // end if
			try {
				tomcatCollector = new GWOSTomcatCollector(properties);
				_scheduler.scheduleAtFixedRate(tomcatCollector, 0, interval,
						TimeUnit.SECONDS);
			} catch (Exception exc) {
				log.error(exc.getMessage());
			} // end if
		} // end if
		else {
			String message = "Fatal Error. Could not find JDMA Properties file. Not starting JDMA: " + TomcatAdminClient.TOMCAT_PROPERTIES;
			log.error(message);
			System.out.println(message);
		}
	}

	/**
	 * Discovers environmental components
	 * 
	 * @return
	 */
	public HashMap<String, Object> autoDiscoverComponents() {
		log.debug("Enter");
		HashMap<String, Object> perfData = null;
		try {
			log.debug("tomcatcollector " + tomcatCollector);
			if (tomcatCollector == null) {
				tomcatCollector = new GWOSTomcatCollector();
			} // end if
			perfData = tomcatCollector.getPerformanceData();
		} catch (Exception exc) {
			log.error(exc.getMessage());
		}
		log.debug("Exit");
		return perfData;
	}

	/**
	 * Discovers environmental components
	 * 
	 */
	public HashMap<String, Object> autoDiscoverComponents(Properties prop) {
		log.debug("Enter");
		HashMap<String, Object> perfData = null;
		try {
			log.debug("tomcatcollector " + tomcatCollector);
			tomcatCollector = new GWOSTomcatCollector(prop);
			perfData = tomcatCollector.getPerformanceData();
		} catch (Exception exc) {
			log.error(exc.getMessage());
		}
		log.debug("Exit");
		return perfData;
	}

	/**
	 * Tests MBean Connection
	 */
	public boolean testConnection(Properties prop) {
		boolean result = false;
		TomcatAdminClient tam = null;
		try {
			tam = new TomcatAdminClient();
			MBeanServerConnection admin = tam.createMBeanServerConnection(prop);
			String queryName = "*:*";
			Set mBeans = admin.queryMBeans(new ObjectName(queryName), null);
			Object obj = mBeans.iterator().next();
			javax.management.ObjectInstance objInstName = (javax.management.ObjectInstance) obj;
			ObjectName objName = objInstName.getObjectName();
			MBeanInfo info = admin.getMBeanInfo(objName);
			MBeanAttributeInfo[] attinfos = info.getAttributes();
			Object valueObj = admin.getAttribute(objName, attinfos[0].getName());
			if (valueObj != null)
				result = true;
			else
				result = false;
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
			log.error("Test connection failed: " + exc.getMessage());
			prop.put(GWOSCollectorService.ERROR_MESSAGE_PROP, exc.getMessage());
			result = false;
		} // end try catch
		finally {
			if (tam != null) {
				tam.shutdown();
			}
		}
		return result;
	}
}
