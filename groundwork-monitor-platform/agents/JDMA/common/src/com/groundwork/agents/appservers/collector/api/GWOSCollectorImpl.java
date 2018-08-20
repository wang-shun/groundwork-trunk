package com.groundwork.agents.appservers.collector.api;

import com.googlecode.jsendnsca.Level;
import com.groundwork.agents.appservers.utils.SendNSCA;
import com.groundwork.agents.appservers.utils.StatUtils;
import org.apache.log4j.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

public abstract class GWOSCollectorImpl implements GWOSCollector, Runnable {

	protected Properties properties;

	protected HashMap<String, Object> collector = null;

	private static Logger log = Logger.getLogger(GWOSCollectorImpl.class);
    //private static JDMALog log = new JDMALog();

	protected String[] gcAttributes = { "CollectionCount", "CollectionTime" };

	protected String[] memoryPoolAttributes = { "CollectionUsageThreshold",
			"CollectionUsageThresholdCount", "UsageThreshold",
			"UsageThresholdCount" };

	protected String[] memoryAttributes = { "HeapMemoryUsage",
			"NonHeapMemoryUsage", "ObjectPendingFinalizationCount" };

	protected String[] threadAttributes = { "CurrentThreadCpuTime",
			"CurrentThreadUserTime", "DaemonThreadCount", "PeakThreadCount",
			"ThreadCount", "ThreadCpuTime", "ThreadUserTime",
			"TotalStartedThreadCount" };

	protected String[] runTimeAttributes = { "Uptime" };

	protected String[] memoryUsageAttributes = { "Init", "Max", "Used",
			"Committed" };

	public HashMap<String, Object> getPerformanceData() throws Exception {
		log.debug("Enter");
		collector = new HashMap<String, Object>();
		this.autoDiscoverServices();
		log.debug("Exit");
		return collector;
	}

	public void run() {
		try {
			log.info("Started collecting Perf data... ");
			sendNSCA(getPerformanceData());
			log.info("Finished collecting Perf data... ");
		} catch (Exception exc) {
			log.error(exc.getMessage());
		} // end try/catch
	}

    public boolean isFoldCase() {
        return true;
    }

    /**
	 * Sends NSCA message
	 */

	private void sendNSCA(HashMap<String, Object> collector) {
        if (log.isDebugEnabled()) {
            log.debug("Sending Perf Data to Nagios... ");
        }
		String encryptionType = properties.getProperty("nagios_encryption");
		String encryptionKey = properties.getProperty("nagios_password");
		String nagiosHostName = properties.getProperty("nagios_hostname");
		String nagiosPortName = properties.getProperty("nagios_port");
		String instanceId = properties.getProperty("instanceId");
        if (log.isDebugEnabled()) {
            log.debug("Pref Data Properties: ");
            log.debug("--- encryptionType: " + encryptionType);
            log.debug("--- encryptionKey: " + encryptionKey);
            log.debug("--- nagiosHostName: " + nagiosHostName);
            log.debug("--- nagiosPortName: " + nagiosPortName);
            log.debug("--- instanceId: " + instanceId);
            log.debug("--- processing item count: " + collector.size());
        }
        if (nagiosHostName != null && nagiosPortName != null) {
            if (log.isDebugEnabled()) {
                log.debug("creating Send NSCA session...");
            }
			SendNSCA nsca = new SendNSCA(nagiosHostName,
					Integer.parseInt(nagiosPortName),
					Integer.parseInt(encryptionType), encryptionKey);
            if (log.isDebugEnabled()) {
                log.debug("... successfully created Send NSCA session");
            }

			Iterator<String> iterator = collector.keySet().iterator();

			String localhost = null;

			//InetAddress addr = InetAddress.getLocalHost(); // Get IP Address
			// Use the hostname from the UI or config.Reason is because we dont know
            // how nagios knows the agent. i.e short name or fqdn. Better get it from user/properties file.
		    localhost = properties.getProperty("hostname");

			while (iterator.hasNext()) {
                String serviceName = (String) iterator.next();
                String key = (isFoldCase()) ? serviceName.toLowerCase() : serviceName;
				if (properties.containsKey(key)) {
                    if (log.isDebugEnabled()) {
                        log.debug("+++ Matched service: " + serviceName);
                    }
					String label = serviceName
							.substring(serviceName
									.lastIndexOf(CollectorConstants.COMPONENT_DELIMITER) + 1);
					Object valueObj = collector.get(serviceName);
					long value = 0;

					String message = null;
					String propValue = properties.getProperty(serviceName);
					StringTokenizer stkn = new StringTokenizer(propValue, ";");
					Level status = Level.UNKNOWN;
					while (stkn.hasMoreTokens()) {
						serviceName = stkn.nextToken();
						String warning = stkn.nextToken();
						String critical = stkn.nextToken();
						if (valueObj instanceof Long) {
							value = (Long) valueObj;
							if (value >= Long.parseLong(warning)
									&& value < Long.parseLong(critical))
								status = Level.WARNING;
							else if (value >= Long.parseLong(critical))
								status = Level.CRITICAL;
							else
								status = Level.OK;
						}
						if (valueObj instanceof Boolean) {
							boolean booleanValue = (Boolean) valueObj;
							// false is 0, true is 1
							if (!booleanValue)
								value = 0;
							else
								value = 1;

							if (value == Long.parseLong(critical))
								status = Level.CRITICAL;
							else
								status = Level.OK;
						}
						message = label + "=" + value + ", w=" +warning + ",c=" + critical + "|" + label + "=" + value + ";" + warning + ";" + critical;

					} // end while
					// Now if you are running multiple instances, append the instance id
					StringBuffer jdmaHost = new StringBuffer();
					jdmaHost.append(localhost);
					if (instanceId != null && !instanceId.equals("")) {
						jdmaHost.append(instanceId);
					}

                    if (log.isDebugEnabled()) {
                        log.debug("JDMA Host==>" + jdmaHost.toString());
                        log.debug("Service Name==>" + serviceName);

                        log.debug("Sending message " + message + ", status "
                                + status.name());
                    }
					
					nsca.send(jdmaHost.toString(), status, serviceName, message);

                    if (log.isDebugEnabled()) {
                        log.debug("Successfully sent message " + message + ", status "
                                + status.name());
                    }
				} // end if
//                else {
//                    if (log.isDebugEnabled()) {
//                        log.debug("+++ Failed to match service: " + serviceName);
//                    }
//                }
			} // end while
		} // end if
        log.debug("Finished sending Perf data to Nagios");
	}

	public abstract void autoDiscoverServices() throws Exception;

	/**
	 * Gets JVM info
	 */
	protected void populateJVMInfo(String mxBean, String jvmPrefix,
			String[] attributes) {
        if (log.isDebugEnabled()) {
            log.debug("Populating JVM Info for ... " + jvmPrefix);
        }

		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName gcName = new ObjectName(mxBean + ",*");
			Set<ObjectName> names = server.queryNames(gcName, null);
			for (ObjectName name : names) {
				AttributeList list = server.getAttributes(name, attributes);
				for (Object obj : list) {
					Attribute att = (Attribute) obj;
					Object objValue = att.getValue();

					String nameProp = name.getKeyProperty("name");
					String serviceName = null;
					if (nameProp == null)
						serviceName = "jvm." + jvmPrefix + "." + att.getName();
					else
						serviceName = "jvm." + jvmPrefix + "."
								+ name.getKeyProperty("name") + "."
								+ att.getName();
					if (objValue instanceof Long)
						collector.put(StatUtils.stripSpecialChars(serviceName),
								(Long) att.getValue());

					if (objValue instanceof Integer)
						collector.put(StatUtils.stripSpecialChars(serviceName),
								((Integer) att.getValue()).longValue());

					if (objValue instanceof CompositeData) {
						CompositeData cd = (CompositeData) objValue;
						if (cd != null) {
							for (String memUsageAtt : memoryUsageAttributes) {
								Object memUsageAttValue = cd.get(memUsageAtt
										.toLowerCase());
								if (memUsageAttValue instanceof Long)
									collector.put(StatUtils
											.stripSpecialChars(serviceName
													+ "." + memUsageAtt),
											(Long) memUsageAttValue);
							}
						} // end if
					}

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
        if (log.isDebugEnabled()) {
            log.debug("...Done Populating JVM Info for ... " + jvmPrefix);
        }

	}
}
