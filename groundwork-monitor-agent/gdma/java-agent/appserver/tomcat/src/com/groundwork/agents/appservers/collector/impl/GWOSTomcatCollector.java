package com.groundwork.agents.appservers.collector.impl;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.openmbean.CompositeData;

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.api.GWOSCollectorImpl;
import com.groundwork.agents.appservers.utils.StatUtils;

public class GWOSTomcatCollector extends GWOSCollectorImpl {
	private MBeanServerConnection adminclient = null;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSTomcatCollector.class);

	public GWOSTomcatCollector() throws Exception {
		adminclient = TomcatAdminClient.createMBeanServerConnection();
		properties = StatUtils
				.readProperties(TomcatAdminClient.TOMCAT_PROPERTIES);
	}

	public GWOSTomcatCollector(Properties prop) throws Exception {
		adminclient = TomcatAdminClient.createMBeanServerConnection(prop);
		properties = prop;
	}

	/**
	 * Auto discover the services
	 * 
	 * @throws Exception
	 */
	public void autoDiscoverServices() throws Exception {
		String objFilter = properties.getProperty("object.name.filter");
		if (objFilter != null && !"".equals(objFilter)) {
			Set mBeans = adminclient.queryMBeans(new ObjectName(objFilter),
					null);

			// Set mBeans = adminclient.queryMBeans(null, null);
			log.debug(mBeans.size());
			if (!mBeans.isEmpty()) {
				for (Object obj : mBeans) {
					javax.management.ObjectInstance objInstName = (javax.management.ObjectInstance) obj;
					ObjectName objName = objInstName.getObjectName();
					MBeanAttributeInfo[] attInfoArray = adminclient
							.getMBeanInfo(objName).getAttributes();
					for (MBeanAttributeInfo attInfo : attInfoArray) {
						StringBuilder serviceName = new StringBuilder();
						serviceName.append(objName.getDomain());
						String serviceProperty = objName
								.getKeyProperty("service");
						String j2eeTypeProperty = objName
								.getKeyProperty("j2eeType");
						String typeProperty = objName.getKeyProperty("type");
						String pathProperty = objName.getKeyProperty("path");
						if (serviceProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(objName
											.getKeyProperty("service")));
						} // end if
						if (j2eeTypeProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(objName
											.getKeyProperty("j2eeType")));
						} // end if
						if (typeProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(objName
											.getKeyProperty("type")));
						} // end if
						if (pathProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(objName
											.getKeyProperty("path")));
						}
						String nameProperty = objName.getKeyProperty("name");
						if (nameProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(nameProperty));
						} // end if

						if (attInfo.getType() != null
								&& attInfo.getType().equals("int")) {
							if (attInfo.getName() != null
									&& !attInfo.getName().equals("debug")
									&& attInfo.isReadable()) {
								try {
									Object attObj = adminclient.getAttribute(
											objName, attInfo.getName());
									long value = -1;
									if (attObj != null) {
										if (attObj
												.getClass()
												.getName()
												.equalsIgnoreCase(
														"java.lang.Long")) {
											Long att = (Long) attObj;
											value = att.longValue();
										}

										if (attObj
												.getClass()
												.getName()
												.equalsIgnoreCase(
														"java.lang.Integer")) {
											Integer att = (Integer) attObj;
											value = att.intValue();
										}
									} // end if
									serviceName
											.append(CollectorConstants.COMPONENT_DELIMITER);
									serviceName.append(StatUtils
											.stripSpecialChars(attInfo
													.getName()));
									collector
											.put(serviceName.toString().toLowerCase(), value);
								} catch (Exception exc) {
									log.warn(exc.getMessage());
								}
							} // end if
						} // end if
						if (attInfo.getType() != null
								&& attInfo.getType().equals("long")) {
							if (attInfo.getName() != null
									&& !attInfo.getName().equals("debug")
									&& attInfo.isReadable()) {
								long value = -1;
								try {
									Long att = (Long) adminclient.getAttribute(
											objName, attInfo.getName());
									if (att != null)
										value = att.longValue();
									serviceName
											.append(CollectorConstants.COMPONENT_DELIMITER);
									serviceName.append(StatUtils
											.stripSpecialChars(attInfo
													.getName()));
									// System.out.println(serviceName + "=" +
									// value);
									collector
											.put(serviceName.toString().toLowerCase(), value);
								} catch (Exception exc) {
									log.warn(exc.getMessage());
								}
							} // end if
						} // end if
						
						if (attInfo.getType() != null
								&& attInfo.getType().equals("boolean")) {
							if (attInfo.getName() != null
									&& !attInfo.getName().equals("debug")
									&& attInfo.isReadable()) {
								boolean value = false;
								try {
									Boolean att = (Boolean) adminclient.getAttribute(
											objName, attInfo.getName());
									if (att != null) {
										value = att.booleanValue();
									}
									serviceName
											.append(CollectorConstants.COMPONENT_DELIMITER);
									serviceName.append(StatUtils
											.stripSpecialChars(attInfo
													.getName()));
									collector
											.put(serviceName.toString().toLowerCase(), value);
								} catch (Exception exc) {
									log.warn(exc.getMessage());
								}
							} // end if
						} // end if

						if (attInfo.getType() != null
								&& attInfo
										.getType()
										.equals("javax.management.j2ee.statistics.Stats")) {
							javax.management.j2ee.statistics.Stats att = (javax.management.j2ee.statistics.Stats) adminclient
									.getAttribute(objName, attInfo.getName());

							for (Statistic stat : att.getStatistics()) {
								String attName = null;
								long attValue = 0;
								if (stat instanceof CountStatistic) {
									attName = ((CountStatistic) stat).getName();
									attValue = ((CountStatistic) stat)
											.getCount();

								} // end if
								if (stat instanceof TimeStatistic) {
									attName = ((TimeStatistic) stat).getName();
									attValue = ((TimeStatistic) stat)
											.getCount();

								} // end if
								if (stat instanceof RangeStatistic) {
									attName = ((RangeStatistic) stat).getName();
									attValue = ((RangeStatistic) stat)
											.getCurrent();

								} // end if

								if (stat instanceof BoundedRangeStatistic) {
									attName = ((BoundedRangeStatistic) stat)
											.getName();
									attValue = ((BoundedRangeStatistic) stat)
											.getCurrent();
								} // end if
								serviceName
										.append(CollectorConstants.COMPONENT_DELIMITER);
								serviceName.append(StatUtils
										.stripSpecialChars(attInfo.getName()));
								// System.out.println(serviceName + "=" +
								// attValue);
								collector.put(serviceName.toString().toLowerCase(), attValue);
							} // end for
						} // end if

						if (attInfo.getType() != null
								&& attInfo
										.getType()
										.equals("javax.management.openmbean.CompositeData")) {
							javax.management.openmbean.CompositeData cd = (javax.management.openmbean.CompositeData) adminclient
									.getAttribute(objName, attInfo.getName());
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(attInfo.getName()));
							if (cd != null
									&& cd.getCompositeType() != null
									&& cd.getCompositeType()
											.getTypeName()
											.equals("java.lang.management.MemoryUsage")) {
								for (String memUsageAtt : memoryUsageAttributes) {
									try {
										Object memUsageAttValue = cd
												.get(memUsageAtt.toLowerCase());
										if (memUsageAttValue instanceof Long)
											collector
													.put((StatUtils
															.stripSpecialChars(serviceName.toString() + CollectorConstants.COMPONENT_DELIMITER+  memUsageAtt)).toLowerCase(),
															(Long) memUsageAttValue);
									} catch (Exception exc) {
										log.warn(exc.getMessage());
									}
								}
							} // end if
						}
					} // end for
				} // end for
			} // end if
		} // end if
	}
}
