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

import com.groundwork.agents.appservers.collector.api.GWOSCollectorImpl;
import com.groundwork.agents.appservers.utils.JDMALog;
import com.groundwork.agents.appservers.utils.StatUtils;
import com.ibm.websphere.management.AdminClient;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * This class collects the performance data for websphere
 * 
 * @author Arul Shanmugam
 * 
 */

public class GWOSWebsphereCollector extends GWOSCollectorImpl {

	// private static org.apache.log4j.Logger log = Logger.getLogger(GWOSWebsphereCollector.class);
    private static JDMALog log = new JDMALog();

    private String appServerName = "was";
	private AdminClient adminclient = null;
	// private String queryName = "WebSphere:type=Perf,*";
	
	private String DELIMITER = ".";

	public GWOSWebsphereCollector() throws Exception {
		properties = StatUtils
				.readProperties(WASAdminClient.WEBSPHERE_PROPERTIES);
		WASAdminClient wasAdminClient = new WASAdminClient();
		try {
			adminclient = wasAdminClient.create();
			log.info("GWOS MBean Collector started successfully....");
		} catch (Exception exc) {
			log.error("GWOS MBean Collector Service Failed! "
					+ exc.getMessage());
		}
        if (adminclient != null) {
            this.processMBeans();
        }
	}

    public boolean isFoldCase() {
        return false;
    }


    public GWOSWebsphereCollector(Properties prop) throws Exception {
		properties = StatUtils
		.readProperties(WASAdminClient.WEBSPHERE_PROPERTIES);
		WASAdminClient wasAdminClient = new WASAdminClient(prop);
		try {
			adminclient = wasAdminClient.create();
			log.info("GWOS MBean Collector started successfully....");
		} catch (Exception exc) {
			log.error("GWOS MBean Collector Service Failed! "
					+ exc.getMessage());
		}
        if (adminclient != null) {
            this.processMBeans();
        }
	}

	public void autoDiscoverServices() throws Exception {
		log.debug("Enter");
		collector = new HashMap<String, Object>();
		this.processMBeans();		
		log.debug("Exit");

	}

	/**
	 * Process mbeans
	 */
	private void processMBeans() {
        long start = System.currentTimeMillis();
		try {
            if (log.isDebugEnabled()) {
                log.debug("Starting process MBeans");
            }
			collector = new HashMap<String, Object>();
			Set mBeans = adminclient.queryMBeans(null, null);
			if (!mBeans.isEmpty()) {
				for (Object obj : mBeans) {
					javax.management.ObjectInstance objInstName = (javax.management.ObjectInstance) obj;
					ObjectName objName = objInstName.getObjectName();
					MBeanInfo info = adminclient.getMBeanInfo(objName);
					// System.out.println("Mbean=" + objName);
					StringBuilder serviceName = new StringBuilder();
					serviceName.append(appServerName);
					serviceName.append(DELIMITER);
					serviceName.append(objName.getKeyProperty("cell"));
					serviceName.append(DELIMITER);
					serviceName.append(objName.getKeyProperty("node"));
					serviceName.append(DELIMITER);
					serviceName.append(objName.getKeyProperty("process"));
					serviceName.append(DELIMITER);
					serviceName.append(objName.getKeyProperty("type"));
					serviceName.append(DELIMITER);
					serviceName.append(objName.getKeyProperty("name"));

					MBeanAttributeInfo[] attinfos = info.getAttributes();
					for (MBeanAttributeInfo attinfo : attinfos) {
						String attLevelName = serviceName + DELIMITER
								+ attinfo.getName();
						
						if (attinfo.getType().equals("int")) {
							
							Object valueObj = adminclient.getAttribute(objName,
									attinfo.getName());
							collector.put(StatUtils
									.stripSpecialChars(attLevelName), new Long(
									valueObj.toString()));

						}
						if (attinfo.getType().equals("long")) {
							
							Object valueObj = adminclient.getAttribute(objName,
									attinfo.getName());
							collector.put(StatUtils
									.stripSpecialChars(attLevelName), new Long(
									valueObj.toString()));

						}
						if (attinfo.getType().equals(
								"javax.management.j2ee.statistics.Stats")) {
							
							javax.management.j2ee.statistics.Stats stats = (javax.management.j2ee.statistics.Stats) adminclient
									.getAttribute(objName, "stats");
							if (stats != null) {
								javax.management.j2ee.statistics.Statistic[] statsArr = stats
										.getStatistics();
								if (statsArr != null) {
									for (javax.management.j2ee.statistics.Statistic stat : statsArr) {
										String attName = null;
										long attValue = 0;
										if (stat instanceof CountStatistic) {
											attName = ((CountStatistic) stat)
													.getName();
											attValue = ((CountStatistic) stat)
													.getCount();

										} // end if
										if (stat instanceof TimeStatistic) {
											attName = ((TimeStatistic) stat)
													.getName();
											attValue = ((TimeStatistic) stat)
													.getCount();

										} // end if
										if (stat instanceof RangeStatistic) {
											attName = ((RangeStatistic) stat)
													.getName();
											attValue = ((RangeStatistic) stat)
													.getCurrent();

										} // end if

										if (stat instanceof BoundedRangeStatistic) {
											attName = ((BoundedRangeStatistic) stat)
													.getName();
											attValue = ((BoundedRangeStatistic) stat)
													.getCurrent();
										} // end if

										collector.put(StatUtils
												.stripSpecialChars(serviceName
														.toString()
														+ DELIMITER
														+ stat.getName()),
												attValue);

									}
								} // end if
							} // end if
						}
					}
				}
			}
            else {
                log.error("No MBeans found on Websphere server");
            }
			this.populateJVMInfo(
					ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, "gc",
					gcAttributes);
			// Populate Thread pool info here
			this.populateJVMInfo(ManagementFactory.THREAD_MXBEAN_NAME,
					"threadPool", threadAttributes);

			// Populate Memory pool info here
			this.populateJVMInfo(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE,
					"memoryPool", memoryPoolAttributes);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
        finally {
            if (log.isDebugEnabled()) {
                log.debug("WebSphere Process MBeans took  " + (System.currentTimeMillis() - start) + " ms" );
            }
        }
	}

	

	/**
	 * Is long
	 * 
	 * @param input
	 * @return
	 */
	public boolean isLong(String input) {
		try {
			Long.parseLong(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}