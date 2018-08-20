package com.groundwork.agents.appservers.collector.impl;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.Properties;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.remote.JMXConnector;

import org.apache.log4j.Logger;

import com.groundwork.agents.appservers.collector.api.CollectorConstants;
import com.groundwork.agents.appservers.collector.api.GWOSCollectorImpl;
import com.groundwork.agents.appservers.utils.StatUtils;

public class GWOSWeblogicCollector extends GWOSCollectorImpl {
	private MBeanServerConnection adminclient = null;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GWOSWeblogicCollector.class);

	public GWOSWeblogicCollector() throws Exception {
		adminclient = WLSAdminClient.createMBeanServer();
		properties = StatUtils
				.readProperties(WLSAdminClient.WEBLOGIC_PROPERTIES);
	}
	
	public GWOSWeblogicCollector(Properties prop) throws Exception {
		adminclient = WLSAdminClient.createMBeanServer(prop);
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
					// System.out.println("mBean Name : " +
					// objName.getCanonicalName());
					MBeanAttributeInfo[] attInfoArray = adminclient
							.getMBeanInfo(objName).getAttributes();

					for (MBeanAttributeInfo attInfo : attInfoArray) {
						StringBuilder serviceName = new StringBuilder();
						serviceName.append(objName.getDomain());

						String typeProperty = objName.getKeyProperty("Type");
						if (typeProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(typeProperty));
						} // end if

						String nameProperty = objName.getKeyProperty("Name");
						if (nameProperty != null) {
							serviceName
									.append(CollectorConstants.COMPONENT_DELIMITER);
							serviceName.append(StatUtils
									.stripSpecialChars(nameProperty));
						} // end if

						if (attInfo.getType().equals("java.lang.Long")) {
							if (attInfo.getName() != null
									&& !attInfo.getName().equals("debug")
									&& attInfo.isReadable()) {
								long value = -1;
								Long att = (Long) adminclient.getAttribute(
										objName, attInfo.getName());
								if (att != null)
									value = att.longValue();
								serviceName
										.append(CollectorConstants.COMPONENT_DELIMITER);
								serviceName.append(StatUtils
										.stripSpecialChars(attInfo.getName()));

								collector.put(serviceName.toString(), value);
							} // end if
						} // end if

						if (attInfo.getType().equals(
								"javax.management.j2ee.statistics.Stats")) {
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
								collector.put(serviceName.toString(), attValue);
							} // end for
						} // end if
					} // end for
				} // end for
			} // end if
		} // end if
			// Populate GC info here
		this.populateJVMInfo(
				ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, "gc",
				gcAttributes);
		// Populate Thread pool info here
		this.populateJVMInfo(ManagementFactory.THREAD_MXBEAN_NAME,
				"threadPool", threadAttributes);

		// Populate Memory pool info here
		this.populateJVMInfo(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE,
				"memoryPool", memoryPoolAttributes);
	}
}
