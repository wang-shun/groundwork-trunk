package com.gwos.statusservice.rest;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.gwos.statusservice.beans.Host;
import com.gwos.statusservice.beans.HostGroup;
import com.gwos.statusservice.beans.Map;
import com.gwos.statusservice.beans.Maps;
import com.gwos.statusservice.beans.Service;
import com.gwos.statusservice.beans.ServiceGroup;
import com.gwos.statusservice.beans.SubMap;
import com.gwos.statusservice.utils.ResponseHelper;

@Path("/entityStatus")
public class EntityStatus {

	private Log log = LogFactory.getLog(this.getClass());
	private static final String DATETIME_FORMAT_US = "MM/dd/yyyy hh:mm:ss a";

	private ReferenceTreeMetaModel rtmm = null;

	@Context
	ServletContext servContext;

	/** Empty string variable. */
	private static final String EMPTY_STRING = "";

	/**
	 * Gets the status info for the given XML Map Collection NOTE: THIS METHOD
	 * SHOULD ONLY BE USED FOR NAGVIS APP. HAS A LOT OF NAGVIS BUSINESS LOGIC IN
	 * IT.
	 *
	 * @param inputXML
	 * @return
	 */
	@POST
	@Path("/statusInfo")
	@Produces(MediaType.APPLICATION_XML + ";charset=UTF-8")
	public String statusInfo(@FormParam("inputXML") String inputXML) {
        String response = null;
        if (inputXML == null) {
			response = ResponseHelper.buildStatus("2", "INVALID INPUT");
			return response;
		} // end if

		try {
			long start = System.currentTimeMillis();
			JAXBContext context = JAXBContext.newInstance(Maps.class);
			Unmarshaller um = context.createUnmarshaller();
			Maps maps = (Maps) um.unmarshal(new StreamSource(new StringReader(
					inputXML.toString())));
			long end = System.currentTimeMillis();
			log.debug("Time taken to convert XML to Java Object "
					+ (end - start) + " millisecs");
			ServletContext statusViewerContext = servContext
					.getContext("/portal-statusviewer");
			if (statusViewerContext != null) {
				rtmm = (ReferenceTreeMetaModel) statusViewerContext
						.getAttribute("referenceTree");
				if (rtmm == null) {
					log.error("!!!!!!!!!!!RTMM not initialized!!!!!!!!!");
					//this.initializeStatusViewer(username, password);
				} // end if
				if (rtmm != null) {
					long start2 = System.currentTimeMillis();
					for (Map map : maps.getMap()) {
						this.fillTheBlanks(map, false);
					} // end for
					long end2 = System.currentTimeMillis();
					log.debug("Time taken to process from RTMM cache "
							+ (end2 - start2) + " millisecs");
					long start3 = System.currentTimeMillis();

					StringWriter responseWriter = new StringWriter();
					Marshaller m = context.createMarshaller();
					m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
							Boolean.TRUE);
					m.marshal(maps, responseWriter);
					response = responseWriter.toString();
					long end3 = System.currentTimeMillis();
					log.debug("Time taken to convert Java Object to XML "
							+ (end3 - start3) + " millisecs");
				} // end if
			} else {
				String errMessage = "Unable to get the servletcontext from the portal-statusviewer";
				log.error(errMessage);
				response = ResponseHelper.buildStatus("99", errMessage);
			} // end if
		} catch (JAXBException ioe) {
			String errMessage = "Internal error. " + ioe.getMessage()
					+ "Please try again later";
			response = ResponseHelper.buildStatus("99", errMessage);
			log.error(ioe.getMessage());
		} catch (RuntimeException re) {
			String errMessage = "Internal error. Hint: pluginoutput not available. Please try again later";
			response = ResponseHelper.buildStatus("99", errMessage);
			log.error(re.getMessage());
		}
		return response;
	}

	/**
	 * Helper method to fill in the blanks for the inputXML
	 * 
	 * @param map
	 */
	private void fillTheBlanks(Map map, boolean isSubmap) {
		if (map != null) {
			HostGroup[] hostgroups = map.getHostgroup();
			if (hostgroups != null) {
				for (HostGroup hostgroup : hostgroups) {
					NetworkMetaEntity netMeta = rtmm.getEntityByName(
							NodeType.HOST_GROUP, hostgroup.getName());
					if (netMeta != null) {
						hostgroup.setStatus(netMeta.getStatus().getStatus()
								.toUpperCase());
						hostgroup.setOutput(netMeta.getSummary());
						hostgroup.setAck(netMeta.isAcknowledged() ? 1 : 0);
						hostgroup.setIn_downtime(netMeta.getInScheduledDown());
						hostgroup.setAlias(netMeta.getAlias());
						List<Integer> hostList = netMeta.getChildNodeList();
						Host[] hostArr = new Host[hostList.size()];
                        /* Service statistics summary */
                        ServiceSummaryHelper serviceSummaryStatistics = null;
						int index = 0;
						for (Integer hostId : hostList) {
                            /* Create a new object to collect service statistics for Host */
                            serviceSummaryStatistics = new ServiceSummaryHelper();
							NetworkMetaEntity netMetaChild = rtmm
									.getHostById(hostId);
							Host host = new Host();
							host.setName(netMetaChild.getName());
							host.setAlias(netMetaChild.getAlias());
							host.setAck(netMetaChild.isAcknowledged() ? 1 : 0);
							host.setStatus(translate2ServiceStatus(netMetaChild
									.getStatus().getStatus().toUpperCase()));
							StringTokenizer stkn = new StringTokenizer(
									netMetaChild.getLastPluginOutputString(),
									"^^^");
							host.setCurrentAttempt(stkn.hasMoreTokens() ? stkn
									.nextToken() : "NA");
							host.setMaxAttempts(stkn.hasMoreTokens() ? stkn
									.nextToken() : "NA");
							host.setIn_downtime(Integer.parseInt(stkn
									.hasMoreTokens() ? stkn.nextToken() : "-1"));
							host.setLastStateChange(stkn.hasMoreTokens() ? stkn
									.nextToken() : "NA");
							// Skip plugin output
							log.debug(stkn.hasMoreTokens() ? stkn.nextToken()
									: "NA");
							host.setOutput("The host is "
									+ netMetaChild.getMonitorStatus() + ". "
									+ netMetaChild.getSummary());
							host.setNextCheckTime(stkn.hasMoreTokens() ? stkn
									.nextToken() : "NA");
							DateFormat date = new SimpleDateFormat(
									DATETIME_FORMAT_US);
							if (netMetaChild.getLastCheckDateTime() != null) {
								String strLastCheckTime = date
										.format(netMetaChild
												.getLastCheckDateTime());
								host.setLastCheckTime(strLastCheckTime);
							}
                            /* Get services for the host and calculate summary statistics */
                            List<Integer> serviceList = netMetaChild.getChildNodeList();
                            for (Integer serviceId : serviceList) {
                                NetworkMetaEntity netMetaChildService = rtmm
                                        .getServiceById(serviceId);
								/* Extract is in downtime value
								  TBD: Lots of parsing and dependency on string format. Needs improvement
								 */
                                StringTokenizer stknChildService = new StringTokenizer(
                                        netMetaChildService.getLastPluginOutputString(),
                                        "^^^");
                                String servCurrentAttempt = stknChildService
                                        .hasMoreTokens() ? stknChildService.nextToken()
                                        : "NA";
                                String servMaxAttempts = stknChildService.hasMoreTokens() ? stknChildService
                                        .nextToken() : "NA";
                                String strServiceDownTime = stknChildService.hasMoreTokens() ? stknChildService
                                        .nextToken() : "-1";
                                int serviceInDownTime = Integer.parseInt(strServiceDownTime);

                                serviceSummaryStatistics.updateStatistics(netMetaChildService.getStatus().getStatus().toUpperCase(),
                                        netMetaChildService.isAcknowledged(), serviceInDownTime);
                            }

							/* Generate the service summary data for the host */
                            serviceSummaryStatistics.syncronizeStatistics(host);

							hostArr[index] = host;
							index++;
						}
						if (hostArr != null && hostArr.length > 0)
							hostgroup.setHost(hostArr);
					} // end if
				} // end for
			} // end if
			Host[] hosts = map.getHost();
			if (hosts != null) {
				for (Host host : hosts) {
					NetworkMetaEntity netMeta = rtmm.getEntityByName(
							NodeType.HOST, host.getName());
					if (netMeta != null) {
						StringTokenizer stkn = new StringTokenizer(
								netMeta.getLastPluginOutputString(), "^^^");
						String currentAttempt = stkn.hasMoreTokens() ? stkn
								.nextToken() : "NA";
						String maxAttempts = stkn.hasMoreTokens() ? stkn
								.nextToken() : "NA";
						String inDownTime = stkn.hasMoreTokens() ? stkn
								.nextToken() : "-1";
						String lastStateChange = stkn.hasMoreTokens() ? stkn
								.nextToken() : "NA";
						String pluginOutput = stkn.hasMoreTokens() ? stkn
								.nextToken() : "NA";
						String nextCheckTime = stkn.hasMoreTokens() ? stkn
								.nextToken() : "NA";
						host.setCurrentAttempt(currentAttempt);
						host.setMaxAttempts(maxAttempts);
						host.setLastStateChange(lastStateChange);

						DateFormat date = new SimpleDateFormat(
								DATETIME_FORMAT_US);
						if (netMeta.getLastCheckDateTime() != null) {
							String strLastCheckTime = date.format(netMeta
									.getLastCheckDateTime());
							host.setLastCheckTime(strLastCheckTime);
						}
						host.setNextCheckTime(nextCheckTime);
						host.setAlias(netMeta.getAlias());
						List<Integer> serviceList = netMeta.getChildNodeList();
						Service[] serviceArr = new Service[serviceList.size()];
						int index = 0;
						int serviceDownTimeCount = 0;
						int serviceAcknowledgeCount = 0;
                        /* Create service summary helper object to keep track of the statistics for services per host */
                        ServiceSummaryHelper serviceSummaryStatistics = new ServiceSummaryHelper();
						for (Integer serviceId : serviceList) {
							NetworkMetaEntity netMetaChild = rtmm
									.getServiceById(serviceId);
							Service service = new Service();
							service.setName(netMetaChild.getName());
							// now populate hostname
							int parentId = netMetaChild.getParentId();
							if (parentId > 0) {
								NetworkMetaEntity parentObj = rtmm
										.getHostById(parentId);
								if (parentObj != null)
									service.setHostname(parentObj.getName());
							} // end if
							service.setAlias(netMetaChild.getAlias());
							boolean acknowledged = netMetaChild
									.isAcknowledged();
							if (acknowledged) {
								serviceAcknowledgeCount++;
							}
							service.setAck(acknowledged ? 1 : 0);
							service.setStatus(netMetaChild.getStatus()
									.getStatus().toUpperCase());
							StringTokenizer stknChild = new StringTokenizer(
									netMetaChild.getLastPluginOutputString(),
									"^^^");
							String servCurrentAttempt = stknChild
									.hasMoreTokens() ? stknChild.nextToken()
									: "NA";
							String servMaxAttempts = stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA";
							String strDownTime = stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "-1";
							int servInDownTime = Integer.parseInt(strDownTime);
							if (servInDownTime == 1)
								serviceDownTimeCount++;
							String servLastStateChange = stknChild
									.hasMoreTokens() ? stknChild.nextToken()
									: "NA";
							String servPluginOutput = stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA";
							service.setCurrentAttempt(servCurrentAttempt);
							service.setMaxAttempts(servMaxAttempts);
							service.setIn_downtime(servInDownTime);
							service.setLastStateChange(servLastStateChange);
							service.setOutput(servPluginOutput);
							service.setPerfData(stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA");
							if (netMetaChild.getNextCheckDateTime() != null) {
								String serNextCheckTime = date
										.format(netMetaChild
												.getNextCheckDateTime());
								service.setNextCheckTime(serNextCheckTime);
							}
							if (netMetaChild.getLastCheckDateTime() != null) {
								String serLastCheckTime = date
										.format(netMetaChild
												.getLastCheckDateTime());
								service.setLastCheckTime(serLastCheckTime);
							}
							serviceArr[index] = service;
							index++;
                            /* Service statistics update. Will be summarized for the Host */
                            serviceSummaryStatistics.updateStatistics(netMetaChild.getStatus()
                                    .getStatus().toUpperCase(), acknowledged, servInDownTime);
						}

						if (!isSubmap) {
							host.setStatus(netMeta.getMonitorStatus());
							host.setOutput(pluginOutput);
							host.setAck(netMeta.isAcknowledged() ? 1 : 0);
							host.setIn_downtime(Integer.parseInt(inDownTime));
						} else {
							String hostStatus = netMeta.getMonitorStatus();
							// One small exception for scenario :
							// If host status is UP, then get the worst service
							// bubbleup
							// else hoststatus takes precedence
							if (hostStatus.equalsIgnoreCase("UP")) {
								hostStatus = translate2ServiceStatus(netMeta
										.getStatus().getStatus().toUpperCase());
							} // end if
								// Calculated info
							host.setStatus(hostStatus);
							host.setOutput("The host is "
									+ netMeta.getMonitorStatus() + ". "
									+ netMeta.getSummary());
							host.setAck(serviceAcknowledgeCount > 0 ? 1 : 0);
							host.setIn_downtime(serviceDownTimeCount > 0 ? 1
									: 0);
						} // end if
						if (serviceArr != null && serviceArr.length > 0)
							host.setService(serviceArr);

                        /* Generate the service summary data for the host */
                        serviceSummaryStatistics.syncronizeStatistics(host);
						/* Debugging */
                        if (log.isDebugEnabled())
                            log.debug("Host Object status: Critical normal (" + host.getCriticalNormal() +") Downtime (" + host.getCriticalDowntime() +") Ack (" + host.getCriticalAck() +") + AckDown (" + host.getCriticalAckdown() +")");

                    }
				} // end for
			} // end if
			ServiceGroup[] servicegroups = map.getServicegroup();
			if (servicegroups != null) {
				for (ServiceGroup servicegroup : servicegroups) {
					NetworkMetaEntity netMeta = rtmm.getEntityByName(
							NodeType.SERVICE_GROUP, servicegroup.getName());
					if (netMeta != null) {
						servicegroup.setStatus(netMeta.getStatus().getStatus()
								.toUpperCase());
						servicegroup.setOutput(netMeta.getSummary());
						servicegroup.setAck(netMeta.isAcknowledged() ? 1 : 0);
						servicegroup.setIn_downtime(netMeta
								.getInScheduledDown());
						List<Integer> serviceList = netMeta.getChildNodeList();
						Service[] serviceArr = new Service[serviceList.size()];
						int index = 0;
						for (Integer serviceId : serviceList) {
							NetworkMetaEntity netMetaChild = rtmm
									.getServiceById(serviceId);
							Service service = new Service();
							service.setName(netMetaChild.getName());
							// now populate hostname
							int parentId = netMetaChild.getParentId();
							if (parentId > 0) {
								NetworkMetaEntity parentObj = rtmm
										.getHostById(parentId);
								if (parentObj != null)
									service.setHostname(parentObj.getName());
							} // end if
							service.setAlias(netMetaChild.getAlias());
							service.setAck(netMetaChild.isAcknowledged() ? 1
									: 0);
							service.setStatus(netMetaChild.getStatus()
									.getStatus().toUpperCase());
							StringTokenizer stknChild = new StringTokenizer(
									netMetaChild.getLastPluginOutputString(),
									"^^^");
							service.setCurrentAttempt(stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA");
							service.setMaxAttempts(stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA");
							String strDownTime = stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA";
							int servInDownTime = Integer.parseInt(strDownTime);
							service.setIn_downtime(servInDownTime);
							service.setLastStateChange(stknChild
									.hasMoreTokens() ? stknChild.nextToken()
									: "NA");
							service.setOutput(stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA");
							service.setPerfData(stknChild.hasMoreTokens() ? stknChild
									.nextToken() : "NA");
							DateFormat date = new SimpleDateFormat(
									DATETIME_FORMAT_US);
							if (netMetaChild.getNextCheckDateTime() != null) {
								String serNextCheckTime = date
										.format(netMetaChild
												.getNextCheckDateTime());
								service.setNextCheckTime(serNextCheckTime);
							}
							if (netMetaChild.getLastCheckDateTime() != null) {
								String serLastCheckTime = date
										.format(netMetaChild
												.getLastCheckDateTime());
								service.setLastCheckTime(serLastCheckTime);
							}
							serviceArr[index] = service;
							index++;
						}
						if (serviceArr != null && serviceArr.length > 0)
							servicegroup.setService(serviceArr);
					}
				} // end for
			} // end if
			Service[] services = map.getService();
			if (services != null) {
				for (Service service : services) {
					NetworkMetaEntity netMeta = rtmm.getServiceEntityByHostAndServiceName(
							service.getHostname(), service.getName());
					if (netMeta != null) {
						service.setStatus(netMeta.getStatus().getStatus()
								.toUpperCase());
						service.setAck(netMeta.isAcknowledged() ? 1 : 0);
						service.setIn_downtime(netMeta.getInScheduledDown());
						DateFormat date = new SimpleDateFormat(
								DATETIME_FORMAT_US);
						if (netMeta.getNextCheckDateTime() != null) {
							String serNextCheckTime = date.format(netMeta
									.getNextCheckDateTime());
							service.setNextCheckTime(serNextCheckTime);
						}
						if (netMeta.getLastCheckDateTime() != null) {
							String serLastCheckTime = date.format(netMeta
									.getLastCheckDateTime());
							service.setLastCheckTime(serLastCheckTime);
						}
						service.setAlias(netMeta.getAlias());
						StringTokenizer stknChild = new StringTokenizer(
								netMeta.getLastPluginOutputString(), "^^^");
						service.setCurrentAttempt(stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA");
						service.setMaxAttempts(stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA");
						String strDownTime = stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA";
						int servInDownTime = Integer.parseInt(strDownTime);
						service.setIn_downtime(servInDownTime);
						service.setLastStateChange(stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA");
						service.setOutput(stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA");
						service.setPerfData(stknChild.hasMoreTokens() ? stknChild
								.nextToken() : "NA");
					} // end if
				} // end for
			} // end of
			SubMap[] submaps = map.getSubmap();
			if (submaps != null) {
				for (SubMap submap : submaps) {
					this.fillTheBlanks(submap, true);
					// Calculate submap details here
					int ack = 0;
					int inDownTime = 0;
					StringBuffer output = new StringBuffer();
					String status = null;
					long downCount = 0;
					long warningCount = 0;
					long unknownCount = 0;
					long pendingCount = 0;
					long upCount = 0;
					HostGroup[] subHostgroups = submap.getHostgroup();
					if (subHostgroups != null) {
						for (HostGroup hostgroup : subHostgroups) {
							// DO Summary here
							String statusName = hostgroup.getStatus();
							if (statusName != null) {
								if (statusName.equalsIgnoreCase("DOWN")
										|| statusName
												.equalsIgnoreCase("SCHEDULED DOWN")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED DOWN")

										|| statusName
												.equalsIgnoreCase("SUSPENDED")) {
									downCount++;
								}
								if (statusName.equalsIgnoreCase("UP")) {
									upCount++;
								}
								if (statusName.equalsIgnoreCase("WARNING_HOST")
										|| statusName
												.equalsIgnoreCase("WARNING")) {
									warningCount++;
								}
								if (statusName.equalsIgnoreCase("UNKNOWN")
										|| statusName
												.equalsIgnoreCase("UNREACHABLE")) {
									unknownCount++;
								}
								if (statusName.equalsIgnoreCase("PENDING_HOST")
										|| statusName
												.equalsIgnoreCase("PENDING")) {
									pendingCount++;
								}
							}

						}
					}
					Host[] subHosts = submap.getHost();
					if (subHosts != null) {
						for (Host host : subHosts) {

							// DO Summary here
							String statusName = host.getStatus();
							if (statusName != null) {
								if (statusName.equalsIgnoreCase("DOWN")
										|| statusName
												.equalsIgnoreCase("SCHEDULED DOWN")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED DOWN")

										|| statusName
												.equalsIgnoreCase("SUSPENDED")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("SCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("CRITICAL")) {
									downCount++;
								}
								if (statusName.equalsIgnoreCase("UP")
										|| statusName.equalsIgnoreCase("OK")) {
									upCount++;
								}
								if (statusName.equalsIgnoreCase("WARNING_HOST")
										|| statusName
												.equalsIgnoreCase("WARNING")) {
									warningCount++;
								}
								if (statusName.equalsIgnoreCase("UNKNOWN")
										|| statusName
												.equalsIgnoreCase("UNREACHABLE")) {
									unknownCount++;
								}
								if (statusName.equalsIgnoreCase("PENDING_HOST")
										|| statusName
												.equalsIgnoreCase("PENDING")) {
									pendingCount++;
								}
							}

						}
					}
					ServiceGroup[] subServicegroups = submap.getServicegroup();
					if (subServicegroups != null) {
						for (ServiceGroup servicegroup : subServicegroups) {
							String statusName = servicegroup.getStatus();
							if (statusName != null) {
								if (statusName
										.equalsIgnoreCase("SCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("CRITICAL")
										|| statusName
												.equalsIgnoreCase("SUSPENDED")) {
									downCount++;
								}
								if (statusName.equalsIgnoreCase("OK")) {
									upCount++;
								}
								if (statusName.equalsIgnoreCase("UNKNOWN")
										|| statusName
												.equalsIgnoreCase("UNREACHABLE")) {
									unknownCount++;
								}
								if (statusName
										.equalsIgnoreCase("PENDING_SERVICE")
										|| statusName
												.equalsIgnoreCase("PENDING")) {
									pendingCount++;
								}
								if (statusName
										.equalsIgnoreCase("WARNING_SERVICE")
										|| statusName
												.equalsIgnoreCase("WARNING")) {
									warningCount++;
								}
							}
						}
					}
					Service[] subServices = submap.getService();
					if (subServices != null) {
						for (Service service : subServices) {

							String statusName = service.getStatus();
							if (statusName != null) {
								if (statusName
										.equalsIgnoreCase("SCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("CRITICAL")
										|| statusName
												.equalsIgnoreCase("SUSPENDED")) {
									downCount++;
								}
								if (statusName.equalsIgnoreCase("OK")) {
									upCount++;
								}
								if (statusName.equalsIgnoreCase("UNKNOWN")
										|| statusName
												.equalsIgnoreCase("UNREACHABLE")) {
									unknownCount++;
								}
								if (statusName
										.equalsIgnoreCase("PENDING_SERVICE")
										|| statusName
												.equalsIgnoreCase("PENDING")) {
									pendingCount++;
								}
								if (statusName
										.equalsIgnoreCase("WARNING_SERVICE")
										|| statusName
												.equalsIgnoreCase("WARNING")) {
									warningCount++;
								}
							}
						}
					}
					output.append("There are ");
					output.append(downCount > 0 ? downCount
							+ " CRITICAL objects, " : "");
					output.append(warningCount > 0 ? warningCount
							+ " WARNING objects, " : "");
					output.append(unknownCount > 0 ? unknownCount
							+ " UNKNOWN objects, " : "");
					output.append(pendingCount > 0 ? pendingCount
							+ " PENDING objects, " : "");
					output.append(upCount > 0 ? upCount + " OK objects" : "");
					submap.setOutput(output.toString());
					// Bubble status
					submap.setStatus(downCount > 0 ? "CRITICAL"
							: (warningCount > 0 ? "WARNING"
									: (pendingCount > 0 ? "PENDING"
											: (unknownCount > 0 ? "UNKNOWN"
													: "OK"))));
				} // end for
			} // end if
		} // end if

	}

	/**
	 * Translates hoststatus to service status. Used for bubbleup
	 * 
	 * @param monitorStatus
	 *            the monitor status
	 * 
	 * @return the string
	 */
	private String translate2ServiceStatus(String monitorStatus) {
		String result = null;
		String UNSCHEDULED_DOWN = "UNSCHEDULED DOWN";
		String PENDING = "PENDING";
		String SCHEDULED_DOWN = "SCHEDULED DOWN";
		String UP = "UP";
		String UNSCHEDULED_CRITICAL = "UNSCHEDULED CRITICAL";
		String SCHEDULED_CRITICAL = "SCHEDULED CRITICAL";
		String CRITICAL = "CRITICAL";
		String WARNING = "WARNING";
		String UNKNOWN = "UNKNOWN";
		String UNREACHABLE = "UNREACHABLE";
		String OK = "OK";
		HashMap<String, String> hostServiceMap = new HashMap<String, String>();
		hostServiceMap.put(SCHEDULED_DOWN, SCHEDULED_CRITICAL);
		hostServiceMap.put(UNSCHEDULED_DOWN, CRITICAL);
		hostServiceMap.put(WARNING, WARNING);
		hostServiceMap.put(PENDING, PENDING);
		hostServiceMap.put(UNREACHABLE, UNKNOWN);
		hostServiceMap.put(UP, OK);
		if (monitorStatus != null
				&& !EMPTY_STRING.equalsIgnoreCase(monitorStatus))
			result = hostServiceMap.get(monitorStatus);
		return result;
	}

}
