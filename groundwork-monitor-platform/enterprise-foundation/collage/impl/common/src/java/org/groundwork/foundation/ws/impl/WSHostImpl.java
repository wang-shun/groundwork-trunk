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

/* Created on: Mar 20, 2006 */

package org.groundwork.foundation.ws.impl;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSHost;
import org.groundwork.foundation.ws.model.HostQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * WebServiec Implementation for WSHost interface.
 * 
 * @author rogerrut
 */
public class WSHostImpl extends WebServiceImpl implements WSHost {

	/** Empty string variable. */
	private static final String EMPTY_STRING = "";

	private static final String DATETIME_FORMAT_US = "MM/dd/yyyy hh:mm:ss a";
	/* Enable logging */
	/** The log. */
	protected static Log log = LogFactory.getLog(WSHostImpl.class);

	/**
	 * Instantiates a new wS host impl.
	 */
	public WSHostImpl() {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.ws.api.WSHost#getHosts(org.groundwork.foundation
	 *      .ws.model.HostQueryType, java.lang.String, java.lang.String, int,
	 *      int, org.groundwork.foundation.ws.model.impl.SortCriteria)
	 */
	public WSFoundationCollection getHosts(HostQueryType hostQueryType,
			String value, String applicationType, int fromRange, int toRange,
			SortCriteria orderedBy) throws WSFoundationException,
			RemoteException {
	    CollageTimer timer = startMetricsTimer();
		WSFoundationCollection hosts = null;

		// check first for null type and if so, return all Hosts
		if (hostQueryType == null) {
			log.error("Invalid HostQueryType specified in getHosts");
			throw new WSFoundationException(
					"Invalid HostQueryType specified in getHosts",
					ExceptionType.WEBSERVICE);
		}

		try {
			if (org.groundwork.foundation.ws.model.impl.HostQueryType.ALL
					.equals(hostQueryType))
				hosts = getHosts(applicationType, fromRange, toRange);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.HOSTGROUPID
					.equals(hostQueryType))
				hosts = getHostsForHostGroupID(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.HOSTGROUPNAME
					.equals(hostQueryType))
				hosts = getHostsForHostGroupName(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.SERVICEDESCRIPTION
					.equals(hostQueryType))
				hosts = getHostsWithService(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.MONITORSERVERNAME
					.equals(hostQueryType))
				hosts = getHostsForMonitorServer(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.HOSTNAME
					.equals(hostQueryType))
				hosts = getHostByName(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.HOSTID
					.equals(hostQueryType))
				hosts = getHostById(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.DEVICEIDENTIFICATION
					.equals(hostQueryType))
				hosts = getHostsForDeviceIdentification(value);
			else if (org.groundwork.foundation.ws.model.impl.HostQueryType.DEVICEID
					.equals(hostQueryType))
				hosts = getHostsForDeviceID(value);
			else
				throw new WSFoundationException(
						"Invalid HostQueryType specified in getHosts",
						ExceptionType.WEBSERVICE);

			return hosts;
		} catch (WSFoundationException wsfe) {
			log.error("Exception occurred in getHosts()", wsfe);
			throw wsfe;
		} catch (Exception e) {
			log.error("Exception occurred in getHosts()", e);
			throw new WSFoundationException(
					"Exception occurred in getHosts() - " + e,
					ExceptionType.WEBSERVICE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * String parameter version of getHosts().
	 * 
	 * @param type
	 *            the type
	 * @param value
	 *            the value
	 * @param applicationType
	 *            the application type
	 * @param fromRange
	 *            the from range
	 * @param toRange
	 *            the to range
	 * @param sortOrder
	 *            the sort order
	 * @param sortField
	 *            the sort field
	 * 
	 * @return the hosts by string
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 * @throws RemoteException
	 *             the remote exception
	 */
	public WSFoundationCollection getHostsByString(String type, String value,
			String applicationType, String fromRange, String toRange,
			String sortOrder, String sortField) throws WSFoundationException,
			RemoteException {
		CollageTimer timer = startMetricsTimer();
		// Do parameter conversion then delegate
		org.groundwork.foundation.ws.model.impl.HostQueryType queryType = org.groundwork.foundation.ws.model.impl.HostQueryType.ALL;

		if (type != null) {
			queryType = org.groundwork.foundation.ws.model.impl.HostQueryType
					.fromValue(type);
		}

		int intFromRange = 0;
		int intToRange = 0;

		if (fromRange != null && fromRange.length() > 0) {
			try {
				intFromRange = Integer.parseInt(fromRange);
			} catch (Exception e) {
			} // Suppress and just use default value
		}

		if (toRange != null && toRange.length() > 0) {
			try {
				intToRange = Integer.parseInt(toRange);
			} catch (Exception e) {
			} // Suppress and just use default value
		}

		SortCriteria sortCriteria = null;
		if (sortOrder != null && sortOrder.trim().length() > 0
				&& sortField != null && sortField.trim().length() > 0) {
			sortCriteria = new SortCriteria(sortOrder, sortField);
		}

		WSFoundationCollection hosts = getHosts(queryType, value, applicationType, intFromRange, intToRange, sortCriteria);
		stopMetricsTimer(timer);
		return hosts;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.ws.api.WSHost#hostLookup(java.lang.String)
	 */
	public WSFoundationCollection hostLookup(String hostName)
			throws RemoteException, WSFoundationException {
	    CollageTimer timer = startMetricsTimer();
		try {
			Collection<Host> hibernateHosts = getHostIdentityService().getHostsByIdOrHostNamesLookup(hostName);
			if (hibernateHosts == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(hibernateHosts.size(),
					getConverter().convertHost(hibernateHosts));
		} catch (Exception e) {
			log.error("Exception occurred in hostLookup()", e);
			throw new WSFoundationException(
					"Exception occurred in hostLookup()" + e,
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.ws.api.WSHost#getHostsByCriteria(org.groundwork
	 *      .foundation.ws.model.impl.Filter,
	 *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
	 */
	public WSFoundationCollection getHostsByCriteria(Filter filter, Sort sort,
			int firstResult, int maxResults) throws RemoteException,
			WSFoundationException {
		try {
			FilterCriteria filterCriteria = getConverter().convert(filter);
			org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
					.convert(sort);

			FoundationQueryList list = getHostService().getHosts(
					filterCriteria, sortCriteria, firstResult, maxResults);

			return new WSFoundationCollection(list.getTotalCount(),
					getConverter()
							.convertHost((Collection<Host>) list.getResults()));
		} catch (Exception e) {
			log.error("Exception occurred in getHostsByCriteria()", e);
			throw new WSFoundationException(
					"Exception occurred in getHostsByCriteria()" + e,
					ExceptionType.DATABASE);
		}
	}

	/**
	 * Gets list of host names only.Does not get the complete hierarchy of inner
	 * objects.
	 * 
	 * @return WSFoundationCollection(String[])
	 * 
	 * @throws RemoteException
	 *             the remote exception
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	public WSFoundationCollection getHostList() throws RemoteException,
			WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		Collection<String> list = getHostService().getHostList();
		WSFoundationCollection hosts = new WSFoundationCollection(list.size(),
				list.toArray(new String[list.size()]));
		stopMetricsTimer(timer);
		return hosts;
	}

	/**
	 * Gets Lightweight Host and service information.Does not return dynamic
	 * properties
	 * 
	 * @return WSFoundationCollection(String[])
	 * 
	 * @throws RemoteException
	 *             the remote exception
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	public WSFoundationCollection getSimpleHosts() throws RemoteException,
			WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		FoundationQueryList list = getHostService().getHostsByMonitorServer(
				"localhost", -1, -1);

		// Stopwatch.getInstance().start();
		List<Host> hosts = list.getResults();
		SimpleHost[] simpleHosts = new SimpleHost[hosts.size()];
		for (int i = 0; i < hosts.size(); i++) {
			Host host = hosts.get(i);
			SimpleHost simpleHost = new SimpleHost();
			if (null != host) {
				simpleHost.setHostID(host.getHostId().intValue());
				simpleHost.setName(host.getHostName());
				String alias = EMPTY_STRING;
				HostStatus hostStatus = host.getHostStatus();
				if (hostStatus != null) {
					Object aliasObj = hostStatus.getProperty("Alias");
					if (aliasObj != null) {
						alias = (String) aliasObj;
					}

					simpleHost.setMonitorStatus(hostStatus
							.getHostMonitorStatus().getName());
					simpleHost.setLastCheckTime(hostStatus.getLastCheckTime());
					// set the acknowledged property for the host
					boolean isHostAcknowledged = false;
					Object acknowledgedObj = hostStatus
							.getProperty("isAcknowledged");
					if (acknowledgedObj != null) {
						isHostAcknowledged = ((Boolean) acknowledgedObj)
								.booleanValue();
					}
					simpleHost.setAcknowledged(isHostAcknowledged);

                    Date lastStateChange = null;
                    Object lastStateChangeObj = hostStatus
                            .getProperty("LastStateChange");
                    if (lastStateChangeObj != null) {
                        lastStateChange = (Date) lastStateChangeObj;
                    }
                    simpleHost.setLastStateChange(lastStateChange);
				} // end if
				simpleHost.setAlias(alias);
				simpleHost.setLastPlugInOutput(this
						.buildLastPluginOutputStringForHost(hostStatus));

				Set serviceStatuses = host.getServiceStatuses();
				Iterator<ServiceStatus> serviceIter = serviceStatuses
						.iterator();
				SimpleServiceStatus[] simpleServices = new SimpleServiceStatus[serviceStatuses
						.size()];
				double count = 0;
				int j = 0; // count for services
				while (serviceIter.hasNext()) {
					ServiceStatus serviceStatus = serviceIter.next();
					SimpleServiceStatus simpleServiceStatus = new SimpleServiceStatus();
					simpleServiceStatus.setServiceStatusID(serviceStatus
							.getServiceStatusId().intValue());
					simpleServiceStatus.setDescription(serviceStatus
							.getServiceDescription());

					String status = serviceStatus.getMonitorStatus().getName();
					simpleServiceStatus.setMonitorStatus(status);
					if (!"OK".equalsIgnoreCase(status)) {
						count++;
					}

					simpleServiceStatus.setLastCheckTime(serviceStatus
							.getLastCheckTime());
					simpleServiceStatus.setNextCheckTime(serviceStatus
							.getNextCheckTime());
					boolean acknowledged = false;
					if (serviceStatus != null) {
						Object acknowledgedObj = serviceStatus
								.getProperty("isProblemAcknowledged");
						if (acknowledgedObj != null)
							acknowledged = ((Boolean) acknowledgedObj)
									.booleanValue();
					} // end if
					simpleServiceStatus.setAcknowledged(acknowledged);
					Date lastServiceStateChange = null;
					if (serviceStatus != null) {
						Object lastServiceStateChangeObj = serviceStatus
								.getProperty("LastStateChange");
						if (lastServiceStateChangeObj != null)
							lastServiceStateChange = (Date) lastServiceStateChangeObj;
					} // end if
					simpleServiceStatus
							.setLastStateChange(lastServiceStateChange);
					simpleServiceStatus
							.setLastPlugInOutput(this
									.buildLastPluginOutputStringForService(serviceStatus));
					simpleServices[j] = simpleServiceStatus;
					j++;

				}
				simpleHost.setSimpleServiceStatus(simpleServices);

				// Calculate only if there are services
				if (count > 0.0) {
					DecimalFormat formater = new DecimalFormat("##.##");
					String serviceAvailability = formater.format(count
							/ serviceStatuses.size() * 100);
					simpleHost.setServiceAvailability(Double
							.parseDouble(serviceAvailability));
				} else {
					simpleHost
							.setServiceAvailability(Double.parseDouble("0.0"));
				} // end if

				this.determineBubbleUpStatus(simpleHost);
			}
			simpleHosts[i] = simpleHost;
		}
		// Stopwatch.getInstance().stop();
		// log.info("Execution time to getSimpleHosts " +
		// Stopwatch.getInstance());
		WSFoundationCollection results = new WSFoundationCollection(list.size(), simpleHosts);
		stopMetricsTimer(timer);
		return results;
	}

	/**
	 * Helper to buildLastPluginOutputStringForHost. This is later parsed by
	 * statusrestservice for Nagvis Client
	 * 
	 * @return
	 */
	private String buildLastPluginOutputStringForHost(HostStatus hostStatus) {
	    CollageTimer timer = startMetricsTimer();
		StringBuffer output = new StringBuffer();
		String delimiter = "^^^";
		DateFormat date = new SimpleDateFormat(DATETIME_FORMAT_US);
		Object dynaObj = hostStatus.getProperty("CurrentAttempt");
		if (dynaObj != null) {
			output.append(((Long) dynaObj).longValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = hostStatus.getProperty("MaxAttempts");
		if (dynaObj != null) {
			output.append(((Long) dynaObj).longValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = hostStatus.getProperty("ScheduledDowntimeDepth");
		if (dynaObj != null) {
			output.append(((Integer) dynaObj).intValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = hostStatus.getProperty("LastStateChange");
		if (dynaObj != null) {
			String strDate = date.format((Date) dynaObj);
			output.append(strDate);
			output.append(delimiter);
		} else {
			output.append("01/01/1970 12:00:00 AM");
			output.append(delimiter);
		}
		dynaObj = hostStatus.getProperty("LastPluginOutput");
		if (dynaObj != null) {
			output.append((String) dynaObj);
			output.append(delimiter);
		} else {
			output.append("NA");
			output.append(delimiter);
		}
		Date nextCheckTime = hostStatus.getNextCheckTime();
		if (nextCheckTime != null) {
			String strDate = date.format(nextCheckTime);
			output.append(strDate);
		} else {
			output.append("NA");
		}
		stopMetricsTimer(timer);
		return output.toString();
	}

	/**
	 * Helper to buildLastPluginOutputStringForHost. This is later parsed by
	 * statusrestservice for Nagvis Client
	 * 
	 * @return
	 */
	private String buildLastPluginOutputStringForService(
			ServiceStatus serviceStatus) {
		CollageTimer timer = startMetricsTimer();
		StringBuffer output = new StringBuffer();
		String delimiter = "^^^";
		DateFormat date = new SimpleDateFormat(DATETIME_FORMAT_US);
		Object dynaObj = serviceStatus.getProperty("CurrentAttempt");
		if (dynaObj != null) {
			output.append(((Long) dynaObj).longValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = serviceStatus.getProperty("MaxAttempts");
		if (dynaObj != null) {
			output.append(((Long) dynaObj).longValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = serviceStatus.getProperty("ScheduledDowntimeDepth");
		if (dynaObj != null) {
			output.append(((Integer) dynaObj).intValue());
			output.append(delimiter);
		} else {
			output.append("-1");
			output.append(delimiter);
		}
		dynaObj = serviceStatus.getProperty("LastStateChange");
		if (dynaObj != null) {
			String strDate = date.format((Date) dynaObj);
			output.append(strDate);
			output.append(delimiter);
		} else {
			output.append("01/01/1970 12:00:00 AM");
			output.append(delimiter);
		}
		dynaObj = serviceStatus.getProperty("LastPluginOutput");
		if (dynaObj != null) {
			output.append((String) dynaObj);
			output.append(delimiter);
		} else {
			output.append("NA");
			output.append(delimiter);
		}
		dynaObj = serviceStatus.getProperty("PerformanceData");
		if (dynaObj != null) {
			output.append((String) dynaObj);
		} else {
			output.append("NA");
			output.append(delimiter);
		}
		stopMetricsTimer(timer);
		return output.toString();
	}

	/**
	 * Gets Lightweight Host and service information.Does not return dynamic
	 * properties
	 * 
	 * @param hostName
	 *            the host name
	 * @param deep
	 *            the deep
	 * 
	 * @return WSFoundationCollection(String[])
	 * 
	 * @throws RemoteException
	 *             the remote exception
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	public WSFoundationCollection getSimpleHost(String hostName, boolean deep)
			throws RemoteException, WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		if (hostName == null || hostName.length() < 1)
			throw new WSFoundationException("Invalid host",
					ExceptionType.WEBSERVICE);
		Host host = getHostIdentityService().getHostByIdOrHostName(hostName);
		if (host == null) {
			return new WSFoundationCollection(0, new SimpleHost[1]);
		} // end if
		SimpleHost simpleHost = new SimpleHost();
		simpleHost.setHostID(host.getHostId().intValue());
		simpleHost.setName(host.getHostName());
		Date lastStateChange = null;
		if (host != null && host.getHostStatus() != null) {
			Object lastStateChangeObj = host.getHostStatus().getProperty(
					"LastStateChange");
			if (lastStateChangeObj != null)
				lastStateChange = (Date) lastStateChangeObj;
		} // end if

		simpleHost.setLastStateChange(lastStateChange);
		simpleHost.setMonitorStatus(host.getHostStatus().getHostMonitorStatus()
				.getName());
		simpleHost.setLastCheckTime(host.getHostStatus().getLastCheckTime());
		Collection<ServiceStatus> serviceStatuses = host.getServiceStatuses();
		double count = 0;
		SimpleServiceStatus[] simpleServices = new SimpleServiceStatus[host
				.getServiceStatuses().size()];
		int j = 0; // count for services
		if (serviceStatuses != null) {
			for (ServiceStatus serviceStatus : serviceStatuses) {
				String status = serviceStatus.getMonitorStatus().getName();
				if (deep) {
					SimpleServiceStatus simpleServiceStatus = new SimpleServiceStatus();
					simpleServiceStatus.setServiceStatusID(serviceStatus
							.getServiceStatusId().intValue());
					simpleServiceStatus.setDescription(serviceStatus
							.getServiceDescription());
					simpleServiceStatus.setMonitorStatus(serviceStatus
							.getMonitorStatus().getName());
					simpleServiceStatus.setLastCheckTime(serviceStatus
							.getLastCheckTime());
					boolean acknowledged = false;
					if (serviceStatus != null) {
						Object acknowledgedObj = serviceStatus
								.getProperty("isProblemAcknowledged");
						if (acknowledgedObj != null)
							acknowledged = ((Boolean) acknowledgedObj)
									.booleanValue();
					} // end if
					simpleServiceStatus.setAcknowledged(acknowledged);

					Date lastServiceStateChange = null;
					if (serviceStatus != null) {
						Object lastServiceStateChangeObj = serviceStatus
								.getProperty("LastStateChange");
						if (lastServiceStateChangeObj != null)
							lastServiceStateChange = (Date) lastServiceStateChangeObj;
					} // end if
					simpleServiceStatus
							.setLastStateChange(lastServiceStateChange);
					simpleServices[j] = simpleServiceStatus;
					j++;
				} // end if
				if (!"OK".equalsIgnoreCase(status)) {
					count++;
				} // end if
			} // end for
			if (deep) {
				simpleHost.setSimpleServiceStatus(simpleServices);
				this.determineBubbleUpStatus(simpleHost);
			} // end if
		} // end if
			// Calculate only if there are services
		if (count > 0.0) {
			DecimalFormat formater = new DecimalFormat("##.##");
			String serviceAvailability = formater.format(count
					/ serviceStatuses.size() * 100);
			simpleHost.setServiceAvailability(Double
					.parseDouble(serviceAvailability));
		} else {
			simpleHost.setServiceAvailability(Double.parseDouble("0.0"));
		} // end if

		boolean acknowledged = false;
		if (host != null && host.getHostStatus() != null) {
			Object acknowledgedObj = host.getHostStatus().getProperty(
					"isAcknowledged");
			if (acknowledgedObj != null)
				acknowledged = ((Boolean) acknowledgedObj).booleanValue();
		} // end if
		simpleHost.setAcknowledged(acknowledged);
		SimpleHost[] simpleHosts = new SimpleHost[1];
		simpleHosts[0] = simpleHost;
		WSFoundationCollection result = new WSFoundationCollection(1, simpleHosts);
		stopMetricsTimer(timer);
		return result;

	}

	/**
	 * Gets Lightweight Host and service information.Does not return dynamic
	 * properties
	 * 
	 * @param filter
	 *            the filter
	 * @param sort
	 *            the sort
	 * @param firstResult
	 *            the first result
	 * @param maxResults
	 *            the max results
	 * @param deep
	 *            the deep
	 * 
	 * @return WSFoundationCollection(String[])
	 * 
	 * @throws RemoteException
	 *             the remote exception
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	public WSFoundationCollection getSimpleHostByCriteria(Filter filter,
			Sort sort, int firstResult, int maxResults, boolean deep)
			throws RemoteException, WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		if (filter == null)
			throw new WSFoundationException("Invalid filter",
					ExceptionType.WEBSERVICE);
		FilterCriteria filterCriteria = getConverter().convert(filter);
		org.groundwork.foundation.dao.SortCriteria sortCriteria = null;
		if (sort != null)
			sortCriteria = getConverter().convert(sort);
		FoundationQueryList list = getHostService().getHosts(filterCriteria,
				sortCriteria, firstResult, maxResults);
		List<Host> hosts = list.getResults();
		SimpleHost[] simpleHosts = new SimpleHost[hosts.size()];
		int i = 0;
		for (Host host : hosts) {
			this.createSimpleHost(host, deep, simpleHosts, i);
			i++;
		}
		WSFoundationCollection result = new WSFoundationCollection(list.getTotalCount(), simpleHosts);
		stopMetricsTimer(timer);
		return result;
	}

	/**
	 * Creates the simple host.
	 * 
	 * @param host
	 *            the host
	 * @param deep
	 *            the deep
	 * @param simpleHosts
	 *            the simple hosts
	 * @param i
	 *            the i
	 */
	private void createSimpleHost(Host host, boolean deep,
			SimpleHost[] simpleHosts, int i) {
		CollageTimer timer = startMetricsTimer();

		SimpleHost simpleHost = new SimpleHost();
		simpleHost.setHostID(host.getHostId().intValue());
		simpleHost.setName(host.getHostName());
		Date lastStateChange = null;
		String lastPluginOutput = null;
		if (host != null && host.getHostStatus() != null) {
			Object lastStateChangeObj = host.getHostStatus().getProperty(
					"LastStateChange");
			if (lastStateChangeObj != null)
				lastStateChange = (Date) lastStateChangeObj;

			Object lastPluginOuputObj = host.getHostStatus().getProperty(
					"LastPluginOutput");
			if (lastPluginOuputObj != null)
				lastPluginOutput = (String) lastPluginOuputObj;
		} // end if

		simpleHost.setLastStateChange(lastStateChange);
		simpleHost.setLastPlugInOutput(lastPluginOutput);
		simpleHost.setMonitorStatus(host.getHostStatus().getHostMonitorStatus()
				.getName());
		simpleHost.setLastCheckTime(host.getHostStatus().getLastCheckTime());
		Collection<ServiceStatus> serviceStatuses = host.getServiceStatuses();
		double count = 0;
		SimpleServiceStatus[] simpleServices = new SimpleServiceStatus[host
				.getServiceStatuses().size()];
		int j = 0; // count for services
		if (serviceStatuses != null) {
			for (ServiceStatus serviceStatus : serviceStatuses) {
				String status = serviceStatus.getMonitorStatus().getName();
				if (deep) {
					SimpleServiceStatus simpleServiceStatus = new SimpleServiceStatus();
					simpleServiceStatus.setServiceStatusID(serviceStatus
							.getServiceStatusId().intValue());
					simpleServiceStatus.setDescription(serviceStatus
							.getServiceDescription());
					simpleServiceStatus.setMonitorStatus(serviceStatus
							.getMonitorStatus().getName());
					simpleServiceStatus.setLastCheckTime(serviceStatus
							.getLastCheckTime());
					simpleServiceStatus.setNextCheckTime(serviceStatus
							.getNextCheckTime());
					boolean acknowledged = false;
					if (serviceStatus != null) {
						Object acknowledgedObj = serviceStatus
								.getProperty("isProblemAcknowledged");
						if (acknowledgedObj != null)
							acknowledged = ((Boolean) acknowledgedObj)
									.booleanValue();
					} // end if
					Date lastServiceStateChange = null;
					if (serviceStatus != null) {
						Object lastServiceStateChangeObj = serviceStatus
								.getProperty("LastStateChange");
						if (lastServiceStateChangeObj != null)
							lastServiceStateChange = (Date) lastServiceStateChangeObj;
					} // end if
					simpleServiceStatus
							.setLastStateChange(lastServiceStateChange);
					simpleServiceStatus.setAcknowledged(acknowledged);
					simpleServices[j] = simpleServiceStatus;
					j++;
					simpleServiceStatus
							.setLastPlugInOutput(buildLastPluginOutputStringForService(serviceStatus));
				} // end if
				if (!"OK".equalsIgnoreCase(status)) {
					count++;
				} // end if

			} // end for
			if (deep) {
				simpleHost.setSimpleServiceStatus(simpleServices);
				this.determineBubbleUpStatus(simpleHost);
			} // end if
		} // end if
			// Calculate only if there are services
		if (count > 0.0) {
			DecimalFormat formater = new DecimalFormat("##.##");
			String serviceAvailability = formater.format(count
					/ serviceStatuses.size() * 100);
			simpleHost.setServiceAvailability(Double
					.parseDouble(serviceAvailability));
		} else {
			simpleHost.setServiceAvailability(Double.parseDouble("0.0"));
		} // end if

		boolean acknowledged = false;
		if (host != null && host.getHostStatus() != null) {
			Object acknowledgedObj = host.getHostStatus().getProperty(
					"isAcknowledged");
			if (acknowledgedObj != null)
				acknowledged = ((Boolean) acknowledgedObj).booleanValue();
		} // end if
		simpleHost.setAcknowledged(acknowledged);
		// Set the nagios stats info and have it parsed in the hostlist if that
		// requires just the plugin output
		simpleHost.setLastPlugInOutput(buildLastPluginOutputStringForHost(host
				.getHostStatus()));
		simpleHosts[i] = simpleHost;
		stopMetricsTimer(timer);
	}

	/**
	 * Gets Lightweight Host and service information for the given hostGroup
	 * name.Does not return dynamic properties
	 * 
	 * @param hostGroupName
	 *            the host group name
	 * @param deep
	 *            the deep
	 * 
	 * @return WSFoundationCollection(String[])
	 * 
	 * @throws RemoteException
	 *             the remote exception
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	public WSFoundationCollection getSimpleHostsByHostGroupName(
			String hostGroupName, boolean deep) throws RemoteException,
			WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		FoundationQueryList list = null;
		/*
		 * When the HostGroup name is null or empty we want to return the data
		 * for all the hosts on the server. (Fix for GWMON-8360)
		 */
		if (hostGroupName == null || hostGroupName.equals(EMPTY_STRING)) {
			list = getHostService().getHosts(null, null, -1, -1);
		} else {
			list = getHostService().getHostsByHostGroupName(hostGroupName,
					null, null, -1, -1);
		}

		List<Host> hosts = list.getResults();
		SimpleHost[] simpleHosts = new SimpleHost[hosts.size()];
		int i = 0;
		for (Host host : hosts) {
			this.createSimpleHost(host, deep, simpleHosts, i);
			i++;
		}
		WSFoundationCollection results = new WSFoundationCollection(list.getTotalCount(), simpleHosts);
		stopMetricsTimer(timer);
		return results;
	}

	/*
	 * Get all hosts
	 */
	/**
	 * Gets the hosts.
	 * 
	 * @param appType
	 *            the app type
	 * @param startRange
	 *            the start range
	 * @param maxResults
	 *            the max results
	 * 
	 * @return the hosts
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHosts(String appType, int startRange,
			int maxResults) throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			FilterCriteria filterCriteria = null;

			if (appType != null && appType.length() > 0) {
				filterCriteria = FilterCriteria.eq(
						Host.HP_APPLICATION_TYPE_NAME, appType);
			}

			FoundationQueryList list = getHostService().getHosts(
					filterCriteria, null, startRange, maxResults);

			return new WSFoundationCollection(list.getTotalCount(),
					getConverter()
							.convertHost((Collection<Host>) list.getResults()));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the hosts for host group id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the hosts for host group id
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsForHostGroupID(String id)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			FoundationQueryList list = getHostService().getHostsByHostGroupId(
					Integer.valueOf(id), null, null, -1, -1);

			return new WSFoundationCollection(list.getTotalCount(),
					getConverter()
							.convertHost((Collection<Host>) list.getResults()));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the hosts for host group name.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the hosts for host group name
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsForHostGroupName(String name)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			FoundationQueryList list = getHostService()
					.getHostsByHostGroupName(name, null, null, -1, -1);

			return new WSFoundationCollection(list.getTotalCount(),
					getConverter()
							.convertHost((Collection<Host>) list.getResults()));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/*
	 * Get all hosts with the specified service.
	 */
	/**
	 * Gets the hosts with service.
	 * 
	 * @param serviceDescription
	 *            the service description
	 * 
	 * @return the hosts with service
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsWithService(String serviceDescription)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			Collection<Host> hosts = getHostService().getHostsByServiceName(
					serviceDescription);
			if (hosts == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(hosts.size(), getConverter()
					.convertHost(hosts));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the hosts for device id.
	 * 
	 * @param deviceID
	 *            the device id
	 * 
	 * @return the hosts for device id
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsForDeviceID(String deviceID)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			Collection<Host> hosts = getHostService().getHostsByDeviceId(
					Integer.valueOf(deviceID));
			if (hosts == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(hosts.size(), getConverter()
					.convertHost(hosts));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the hosts for device identification.
	 * 
	 * @param deviceIdentification
	 *            the device identification
	 * 
	 * @return the hosts for device identification
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsForDeviceIdentification(
			String deviceIdentification) throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			Collection<Host> hosts = getHostService()
					.getHostsByDeviceIdentification(deviceIdentification);
			if (hosts == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(hosts.size(), getConverter()
					.convertHost(hosts));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the hosts for monitor server.
	 * 
	 * @param monitorServer
	 *            the monitor server
	 * 
	 * @return the hosts for monitor server
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostsForMonitorServer(String monitorServer)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			FoundationQueryList list = getHostService()
					.getHostsByMonitorServer(monitorServer, -1, -1);

			return new WSFoundationCollection(list.getTotalCount(),
					getConverter()
							.convertHost((Collection<Host>) list.getResults()));
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the host by name.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the host by name
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostByName(String name)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			Host host = getHostIdentityService().getHostByIdOrHostName(name);
			if (host == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(
					1,
					new org.groundwork.foundation.ws.model.impl.Host[] { getConverter()
							.convert(host) });
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

	/**
	 * Gets the host by id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the host by id
	 * 
	 * @throws WSFoundationException
	 *             the WS foundation exception
	 */
	private WSFoundationCollection getHostById(String id)
			throws WSFoundationException {
		CollageTimer timer = startMetricsTimer();
		try {
			Host host = getHostService().getHostByHostId(Integer.valueOf(id));
			if (host == null)
				return new WSFoundationCollection(0,
						new org.groundwork.foundation.ws.model.impl.Host[0]);

			return new WSFoundationCollection(
					1,
					new org.groundwork.foundation.ws.model.impl.Host[] { getConverter()
							.convert(host) });
		} catch (CollageException e) {
			throw new WSFoundationException(e.getMessage(),
					ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}

    /**
     * SimpleServiceStatus monitor status extractor for bubble up computation.
     */
    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<SimpleServiceStatus> BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<SimpleServiceStatus>() {
                @Override
                public String extractMonitorStatus(SimpleServiceStatus obj) {
                    return obj.getMonitorStatus();
                }
            };

	/**
	 * Bubbleup status is calculated here.
	 * 
	 * @param host the host
	 */
	private void determineBubbleUpStatus(SimpleHost host) {
		if (host != null) {
			CollageTimer timer = startMetricsTimer();
            String bubbleUpStatus = MonitorStatusBubbleUp.computeHostMonitorStatusBubbleUp(host.getMonitorStatus(),
                    Arrays.asList(host.getSimpleServiceStatus()), BUBBLE_UP_EXTRACTOR);
            host.setBubbleUpStatus(bubbleUpStatus);
            stopMetricsTimer(timer);
		}
	}
}
