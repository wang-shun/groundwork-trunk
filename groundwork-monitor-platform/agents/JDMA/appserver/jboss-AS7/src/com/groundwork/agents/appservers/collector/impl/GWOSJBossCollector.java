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

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.api.GWOSCollectorImpl;
import com.groundwork.agents.appservers.utils.StatUtils;

public class GWOSJBossCollector extends GWOSCollectorImpl {
	private MBeanServerConnection adminclient = null;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSJBossCollector.class);

	public GWOSJBossCollector() throws Exception {
		adminclient = JBossAdminClient.createMBeanServerConnection();
		properties = StatUtils
				.readProperties(JBossAdminClient.JBOSS_PROPERTIES);
	}

	public GWOSJBossCollector(Properties prop) throws Exception {
		adminclient = JBossAdminClient.createMBeanServerConnection(prop);
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
			log.debug(mBeans.size());
			if (!mBeans.isEmpty()) {
				for (Object obj : mBeans) {
					javax.management.ObjectInstance objInstName = (javax.management.ObjectInstance) obj;
					ObjectName objName = objInstName.getObjectName();
					MBeanAttributeInfo[] attInfoArray = adminclient
							.getMBeanInfo(objName).getAttributes();
                    log.debug("Processing ..." + objName);
					/*
					 * System.out.println(objName.getDomain()); if (attInfoArray
					 * != null && attInfoArray.length > 0) {
					 * System.out.println("==>" + objName.getCanonicalName()); }
					 */
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
                        log.debug("Processing int..");
						try {
							if (attInfo.getType() != null
                                    && attInfo.getType().equals("int")) {
								if (attInfo.getName() != null
										&& !attInfo.getName().equals("debug")
										&& !attInfo.getName().equals(
												"RolesCount")
										&& !attInfo.getName().equals(
												"UserCount")
                                        && !attInfo.getName().equals(
                                        "locks held")
										&& attInfo.isReadable()) {
                                    Integer att = (Integer) adminclient
											.getAttribute(objName,
													attInfo.getName());
									long value = -1;
									try {
										if (att != null)
											value = att.intValue();
										serviceName
												.append(CollectorConstants.COMPONENT_DELIMITER);
										serviceName.append(StatUtils
												.stripSpecialChars(attInfo
														.getName()));
										collector.put(serviceName.toString().toLowerCase(),
												value);
									} catch (Exception exc) {
										log.warn(exc.getMessage());
									}
								} // end if
							} // end if
                            log.debug("Processing long...");
							if (attInfo.getType() != null
                                    && attInfo.getType().equals("long")) {
								if (attInfo.getName() != null
										&& !attInfo.getName().equals("debug")
										&& attInfo.isReadable()) {
									long value = -1;
									try {
										Long att = (Long) adminclient
												.getAttribute(objName,
														attInfo.getName());
										if (att != null)
											value = att.longValue();
										serviceName
												.append(CollectorConstants.COMPONENT_DELIMITER);
										serviceName.append(StatUtils
												.stripSpecialChars(attInfo
														.getName()));
										// System.out.println(serviceName + "="
										// +
										// value);
										collector.put(serviceName.toString().toLowerCase(),
												value);
									} catch (Exception exc) {
										log.warn(exc.getMessage());
									}
								} // end if
							} // end if
                            log.debug("Processing double...");
                            if (attInfo.getType().equals("double")) {
                                if (attInfo.getName() != null
                                        && !attInfo.getName().equals("debug")
                                        && attInfo.isReadable()) {
                                    double value = -1;
                                    try {
                                        Double att = (Double) adminclient
                                                .getAttribute(objName,
                                                        attInfo.getName());
                                        if (att != null)
                                            value = att.doubleValue();
                                        serviceName
                                                .append(CollectorConstants.COMPONENT_DELIMITER);
                                        serviceName.append(StatUtils
                                                .stripSpecialChars(attInfo
                                                        .getName()));
                                        // System.out.println(serviceName + "="
                                        // +
                                        // value);
                                        collector.put(serviceName.toString().toLowerCase(),
                                                value);
                                    } catch (Exception exc) {
                                        log.warn(exc.getMessage());
                                    }
                                } // end if
                            } // end if
                            log.debug("Processing boolean...");
							if (attInfo.getType() != null
									&& attInfo.getType().equals("boolean")) {
								if (attInfo.getName() != null
										&& !attInfo.getName().equals("debug")
										&& attInfo.isReadable()) {
									boolean value = false;
									try {
										Boolean att = (Boolean) adminclient
												.getAttribute(objName,
														attInfo.getName());
										if (att != null) {
											value = att.booleanValue();
										}
										serviceName
												.append(CollectorConstants.COMPONENT_DELIMITER);
										serviceName.append(StatUtils
												.stripSpecialChars(attInfo
														.getName()));
										collector.put(serviceName.toString()
												.toLowerCase(), value);
									} catch (Exception exc) {
										log.warn(exc.getMessage());
									}
								} // end if
							} // end if
                            log.debug("Processing Stats...");
							if (attInfo.getType() != null
                                    && attInfo.getType().equals(
									"javax.management.j2ee.statistics.Stats")) {
								javax.management.j2ee.statistics.Stats att = (javax.management.j2ee.statistics.Stats) adminclient
										.getAttribute(objName,
												attInfo.getName());

								for (Statistic stat : att.getStatistics()) {
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
									serviceName
											.append(CollectorConstants.COMPONENT_DELIMITER);
									serviceName.append(StatUtils
											.stripSpecialChars(attInfo
													.getName()));
									// System.out.println(serviceName + "=" +
									// attValue);
									collector.put(serviceName.toString().toLowerCase(),
											attValue);
								} // end for
							} // end if

                            log.debug("Processing CompositeData...");
                            if (attInfo.getType() != null
                                    && attInfo.getType().equals(
                                    "javax.management.openmbean.CompositeData")) {
                                javax.management.openmbean.CompositeData att = (javax.management.openmbean.CompositeData) adminclient
                                        .getAttribute(objName,
                                                attInfo.getName());
                                log.debug("Before iterating.." + objName + "===>" + attInfo.getName() + "===>" + att);
                                if (att != null) {
                                    for (String attName : (Set<String>) att.getCompositeType().keySet()) {
                                        log.debug("ATT name..." + attName);
                                        StringBuilder serviceNameSub = new StringBuilder(serviceName);
                                        serviceNameSub
                                                .append(CollectorConstants.COMPONENT_DELIMITER);
                                        serviceNameSub.append(attInfo.getName());
                                        Object attValue;
                                        try {
                                            attValue = att.get(attName);
                                        } catch (Exception e) {
                                            log.error("Exception..." + e.getMessage());
                                            attValue = -1;
                                        }
                                        if (Number.class.isInstance(attValue)) {
                                            log.debug(attName + " is number. adding");
                                            serviceNameSub
                                                    .append(CollectorConstants.COMPONENT_DELIMITER);
                                            serviceNameSub.append(StatUtils
                                                    .stripSpecialChars(attName));
                                            collector.put(serviceNameSub.toString().toLowerCase(),
                                                    attValue);
                                        }
                                        else {
                                            log.debug(attName + " is not number. Skipping");
                                        }

                                    } // end for
                                } // end if
                            } // end if
						} catch (Exception mbe) {
							log.error(mbe.getMessage() + " while processing " + objName);
							continue;
						} // end if
					} // end for
				} // end for
			} // end if
		}
	}
}
