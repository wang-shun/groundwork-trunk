/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.statusviewer.handler;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.model.CommonUtils;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.model.ExtendedUIRole;
import com.groundworkopensource.portal.statusviewer.bean.OnDemandServerPush;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.JMSUpdate;
import com.groundworkopensource.portal.statusviewer.common.JMSUtils;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;

import javax.faces.context.FacesContext;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



/**
 * This is Global/Application scope bean, that contains the reference tree model
 * to build trees for each sub page.
 * 
 * It contains all the Hosts/HostGroups as well as Services/ServiceGroups.
 * 
 * @author nitin_jadhav
 */

public class ReferenceTreeMetaModel extends OnDemandServerPush {

	/**
	 * DELETE
	 */
	private static final String DELETE = "DELETE";

	/**
	 * UPDATE_ACKNOWLEDGE
	 */
	private static final String UPDATE_ACKNOWLEDGE = "UPDATE_ACKNOWLEDGE";

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7679843444450753760L;

	/**
	 * HOST_TEXT
	 */
	private static final String HOST_TEXT = "Host: ";

	/**
	 * HOSTS_TEXT
	 */
	private static final String HOSTS_TEXT = "Hosts: ";

	/**
	 * SERVICES_TEXT
	 */
	private static final String SERVICES_TEXT = "Services: ";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ReferenceTreeMetaModel.class.getName());

	/**
	 * foundationWSFacade Object to call web services.
	 */
	private IWSFacade foundationWSFacade = null;
    private Object foundationLock = new Object();

	// Default Values
	/**
	 * DEFAULT_TOPIC_NAME - to be published for Tree View
	 */
	private static final String DEFAULT_TOPIC_NAME = "ui_events";

	// Maps the contains the particular network entities.
	// ConcurrentHashMap is used to synchronize code and avoid deadlocks.

	/**
	 * Map for storing HostGroup entities.
	 */
	private ConcurrentHashMap<Integer, NetworkMetaEntity> hostGroupMap = new ConcurrentHashMap<Integer, NetworkMetaEntity>();

	/**
	 * Map for storing Host entities.
	 */
	private ConcurrentHashMap<Integer, NetworkMetaEntity> hostMap = new ConcurrentHashMap<Integer, NetworkMetaEntity>();
	/**
	 * Map for storing Service entities.
	 */
	private ConcurrentHashMap<Integer, NetworkMetaEntity> serviceMap = new ConcurrentHashMap<Integer, NetworkMetaEntity>();

	/**
	 * Map for storing Service group entities.
	 */
	private ConcurrentHashMap<Integer, NetworkMetaEntity> serviceGroupMap = new ConcurrentHashMap<Integer, NetworkMetaEntity>();

	/**
	 * Map for storing CustomGroup entities.
	 */
	private ConcurrentHashMap<Integer, NetworkMetaEntity> customGroupMap = new ConcurrentHashMap<Integer, NetworkMetaEntity>();

	private List<CustomGroup> customGroups = null;
	/**
	 * Localized String for "Alias"
	 */
	private String aliasString = ResourceUtils
			.getLocalizedMessage("com_groundwork_portal_statusviewer_networktree_tooltip_alias");

	/**
	 * Localized String for "Troubled"
	 */
	private String troubledString = ResourceUtils
			.getLocalizedMessage("com_groundwork_portal_statusviewer_networktree_tooltip_troubled");

	/**
	 * facesContextInstance
	 */
	private FacesContext facesContextInstance;

	private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";

    private boolean initialized = false;

	/**
	 * Comparator for sorting services under service group
	 * (service_name(host_name))
	 */
	private Comparator<NetworkMetaEntity> serviceUnderServiceGroupComparator = new Comparator<NetworkMetaEntity>() {
		public int compare(NetworkMetaEntity entity1, NetworkMetaEntity entity2) {
			return entity1.getExtendedName().compareTo(
					entity2.getExtendedName());
		}
	};

	/**
	 * Default Constructor. This builds all maps for Host Group, Host, Services
	 * and Service Group
	 */

	public ReferenceTreeMetaModel() {
		// Note: DO NOT CHANGE the sequence of calls for model creation!! They
		// are interdependent.
		try {
			// create all "models" i.e. populate all map
			createModels();
		} catch (GWPortalGenericException e) {
			LOGGER.error("Error occured while creating Tree Model in ReferenceTreeMetaModel()."
					+ " Tree portlet and other dependent portlets will not work.");
		}
	}

    private IWSFacade getWSFacade() throws GWPortalException {
        if (foundationWSFacade == null) {
            synchronized (foundationLock) {
                try {
                    foundationWSFacade = new WebServiceFactory()
                            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
                }
                catch (Exception e) {
                    String message = "Failed to retrieve foundation web service " + e.getMessage();
                    LOGGER.error(message, e);
                    throw new GWPortalException("Failed to retrieve foundation web service " + e.getMessage());
                }
            }
        }
        return foundationWSFacade;
    }

    /**
	 * This method creates all "models" i.e. populates all maps.
	 * 
	 * This method is called only once in application life time (and may be
	 * another time if error occurred). Its important to retain data accuracy.
	 * Thats why it is synchronized.
	 * 
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private synchronized void createModels() throws GWPortalException,
			WSDataUnavailableException {

        if (LOGGER.isInfoEnabled())
		    LOGGER.info("creating models for Tree view... in createModels()");

        initialized = false;
		// Create Host and Service Tree Reference Model
		createHostAndServiceModel();
		// Create Service Group Tree Reference Model
		createServiceGroupModel();
		// Create Host Tree Reference Model
		createHostGroupModel();

		createCustomGroupModel();
        initialized = true;
	}

	/**
	 * Call back method for JMS.
	 * 
	 * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
	 */
	@Override
	public void refresh(String xml) {
		LOGGER.debug("*****************************");
		LOGGER.debug(xml);
		LOGGER.debug("*****************************");
		// instead of processing entire tree for push, we are just refreshing
		// the changed Host/Service Group and its children.
		// Update operations are synchronized as we use ConcurrentHashMap data
		// structure.
		if (xml == null) {
			return;
		}
		try {
			// set the FacesContext initialized earlier - as it will be
			// null in JMS thread.
			// LOGGER.warn("JMS PUSH for RTMM MSG : " + xml);
			if (null != facesContextInstance) {
				FacesUtils.setFacesContext(facesContextInstance);
			}

			// get the JMS Updates Map (as per the NodeType) from xml
			Map<NodeType, List<JMSUpdate>> updatesMapFromXML = JMSUtils
					.getJMSUpdatesMapFromXML(xml);

			// process Host updates - DELETE and Acknowledge
			List<JMSUpdate> hostJMSUpdates = updatesMapFromXML
					.get(NodeType.HOST);
			for (JMSUpdate hostUpdate : hostJMSUpdates) {
				// host: process updates for removal - deletion only
				if (hostUpdate.getAction().equals(DELETE)) {
					int deleteHostId = hostUpdate.getId();
					// get this host from map
					NetworkMetaEntity hostById = getHostById(deleteHostId);
                    if (hostById != null) {
                        // get list of services associated with this host
                        List<Integer> servicesList = hostById.getChildNodeList();

                        // remove deleted host's deleted services from serviceGroup.
                        Collection<NetworkMetaEntity> serviceGroups = serviceGroupMap
                                .values();
                        for (NetworkMetaEntity serviceGroupMetaEntity : serviceGroups) {
                            List<Integer> servicesInServiceGroup = serviceGroupMetaEntity
                                    .getChildNodeList();
                            boolean serviceRemoved = false;
                            for (Integer deletedServiceId : servicesList) {
                                if (servicesInServiceGroup.remove(deletedServiceId)) {
                                    serviceRemoved = true;
                                }
                            }
                            if (serviceRemoved) {
                                // update tool-tip and bubble up status of service
                                // group
                                updateServiceGroupTooltip(serviceGroupMetaEntity,
                                        servicesInServiceGroup);
                            }
                        }

                        // remove service from service map
                        for (Integer serviceId : servicesList) {
                            serviceMap.remove(serviceId);
                        }
                    }
                    // remove host from host map
                    removeHost(deleteHostId);

				} else if (hostUpdate.getAction().equals(UPDATE_ACKNOWLEDGE)) {
					// process host acknowledgment updates
					int updatedHostId = hostUpdate.getId();
					// get this host from map
					NetworkMetaEntity hostById = getHostById(updatedHostId);
					hostById.setAcknowledged(!hostById.isAcknowledged());
				}
			}

			// process Host Group updates
			List<JMSUpdate> hostGroupJMSUpdates = updatesMapFromXML
					.get(NodeType.HOST_GROUP);
			for (JMSUpdate update : hostGroupJMSUpdates) {
				// long hgStartTime = System.currentTimeMillis();
				if (update.getAction().equals(DELETE)) {
					removeHostGroup(update.getId());
				} else {
					updateHostGroup(update.getId());
				}
				// LOGGER.debug("********Time taken to " + update.getAction()
				// + "  HOST-GROUP is "
				// + (System.currentTimeMillis() - hgStartTime)
				// + " ms********");
			}

			// process Service Group updates
			List<JMSUpdate> serviceGroupJMSUpdates = updatesMapFromXML
					.get(NodeType.SERVICE_GROUP);
			for (JMSUpdate update : serviceGroupJMSUpdates) {
				// long sgStartTime = System.currentTimeMillis();
				if (update.getAction().equals(DELETE)) {
					removeServiceGroup(update.getId());
				} else {
					updateServiceGroup(update.getId());
				}
				// LOGGER.debug("********Time taken to " + update.getAction()
				// + "  Service Group is "
				// + (System.currentTimeMillis() - sgStartTime)
				// + " ms********");
			}

			// process Service updates - just Acknowledge
			List<JMSUpdate> serviceJMSUpdates = updatesMapFromXML
					.get(NodeType.SERVICE);
			for (JMSUpdate serviceUpdate : serviceJMSUpdates) {
				if (serviceUpdate.getAction().equals(UPDATE_ACKNOWLEDGE)) {
					// process service acknowledgment updates
					int updatedServiceId = serviceUpdate.getId();
					// get this service from map
					NetworkMetaEntity serviceById = getServiceById(updatedServiceId);
					serviceById.setAcknowledged(!serviceById.isAcknowledged());
				}
			}

		} catch (Exception exc) {
			LOGGER.error("Exception in JMS Push refresh() method of Tree View portlet. Actual Exception : "
					+ exc, exc);
            exc.printStackTrace();
            initialized = false; // force refresh
		}
		return;
	}

	/**
	 * Updates Service Group Tool-tip and Bubble-up status.
	 * 
	 * @param serviceGroupMetaEntity
	 * @param servicesInServiceGroup
	 */
	private void updateServiceGroupTooltip(
			NetworkMetaEntity serviceGroupMetaEntity,
			List<Integer> servicesInServiceGroup) {
		int troubledServices = 0;
		List<NetworkMetaEntity> serviceMetaEntities = new ArrayList<NetworkMetaEntity>();
		for (Integer serviceId : servicesInServiceGroup) {
			NetworkMetaEntity serviceEntity = serviceMap.get(serviceId);
			if (serviceEntity != null) {
				serviceMetaEntities.add(serviceEntity);
				if (serviceEntity.getStatus() != NetworkObjectStatusEnum.SERVICE_OK) {
					troubledServices++;
				}
			}
		}
		StringBuilder tooltip = new StringBuilder();
		tooltip.append(SERVICES_TEXT + servicesInServiceGroup.size()
				+ Constant.BR);
		tooltip.append(troubledString + SERVICES_TEXT + troubledServices);
		serviceGroupMetaEntity.setToolTip(tooltip.toString());

		serviceGroupMetaEntity
				.setStatus(determineBubbleUpStatusForServiceGroup(serviceMetaEntities));

		serviceGroupMap.put(serviceGroupMetaEntity.getObjectId(),
				serviceGroupMetaEntity);
	}

	/**
	 * Remove service group
	 * 
	 * @param id
	 */
	private void removeServiceGroup(Integer id) {
		serviceGroupMap.remove(id);
	}

	/**
	 * Remove host group
	 * 
	 * @param id
	 */
	private void removeHostGroup(Integer id) {
		hostGroupMap.remove(id);
	}

	/**
	 * This method publishes updates for Tree View portlet.
	 * 
	 * @param treeViewPushString
	 * @throws GWPortalGenericException
	 * 
	 */
	@SuppressWarnings("unused")
	private void publishTreeViewUpdates(String treeViewPushString)
			throws GWPortalGenericException {
		if (null == treeViewPushString
				|| treeViewPushString.equals(Constant.EMPTY_STRING)) {
			return;
		}
		Session session = null;
		try {

			session = this.jmsConnection.getConnection().createSession(true,
					Session.SESSION_TRANSACTED);

			// finds the topic and build a publisher:
			Topic topic = (Topic) this.jmsConnection.getJndi().lookup(
					DEFAULT_TOPIC_NAME);
			MessageProducer publisher = session.createProducer(topic);
			TextMessage message = session.createTextMessage();
			message.setText(treeViewPushString);
			publisher.send(message);
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());
			throw new GWPortalGenericException(exc.getMessage());
		} finally {
			if (session != null) {
				try {
					session.commit();
					session.close();
					session = null;
				} catch (Exception exc) {
					LOGGER.error(exc.getMessage());
				}
			}
		}

	}

	/**
	 * create ServiceGroup tree model by calling web service call getCategories
	 * 
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private void createServiceGroupModel() throws GWPortalException,
			WSDataUnavailableException {

		// Get the list of Service groups aka "CATEGORIES"
		// LOGGER
		// .debug("!!!!!!!!!!!!!!!!!! Getting all SERVICE GROUPS !!!!!!!!!!");
		// long start = System.currentTimeMillis();
		Category[] serviceGroups = getWSFacade().getAllServiceGroups();
		// long end = System.currentTimeMillis();
		// LOGGER
		// .debug(
		// "!!!!!!!!!!!!!!!!!! FINISHED Getting all SERVICE GROUPS !!!!!!!!!! TIME REQUIRED in milliseconds ["
		// + (end - start) + "]");

		// synchronized (serviceGroupMap) {
		serviceGroupMap.clear();
		// add service groups in map
		if (serviceGroups != null) {
			List<Integer> serviceIds = new ArrayList<Integer>();
			for (Category serviceGroup : serviceGroups) {
				CategoryEntity[] categoryEntities = serviceGroup
						.getCategoryEntities();
				if (null != categoryEntities) {
					for (CategoryEntity categoryEntity : categoryEntities) {
						LOGGER.debug("Adding Service with Id ["
								+ categoryEntity.getObjectID()
								+ "] in Service Group ["
								+ serviceGroup.getName() + "]");
						serviceIds.add(categoryEntity.getObjectID());
					}
				}

				addServiceGroupInMap(serviceGroup, serviceIds);
				serviceIds.clear();
			} // end for (Category serviceGroup : serviceGroups)
		} // end if (serviceGroups != null)
	}

	// }

	/**
	 * Adds service group into map in the form of NetworkMetaEntity
	 * 
	 * @param serviceGroup
	 * @param serviceStatusIds
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private void addServiceGroupInMap(Category serviceGroup,
			List<Integer> serviceStatusIds) throws GWPortalException,
			WSDataUnavailableException {

		// List to add services IDs under service group
		List<Integer> serviceList = new ArrayList<Integer>();
		int troubledServices = 0;
		long downCount = 0;
		long warningCount = 0;
		long unknownCount = 0;
		long pendingCount = 0;
		long upCount = 0;
		List<NetworkMetaEntity> serviceEntityList = new ArrayList<NetworkMetaEntity>();

		if (serviceStatusIds != null && !serviceStatusIds.isEmpty()) {
			for (Integer serviceId : serviceStatusIds) {
				NetworkMetaEntity serviceEntity = getServiceById(serviceId);

				if (serviceEntity != null) {
					if (serviceEntity.getStatus() != NetworkObjectStatusEnum.SERVICE_OK) {
						troubledServices++;
					}
					serviceEntityList.add(serviceEntity);
					// DO Summary here
					String statusName = serviceEntity.getStatus().getStatus();
					if (statusName.equalsIgnoreCase("SCHEDULED CRITICAL")
							|| statusName
									.equalsIgnoreCase("UNSCHEDULED CRITICAL")
							|| statusName.equalsIgnoreCase("CRITICAL")
							|| statusName.equalsIgnoreCase("SUSPENDED")) {
						downCount++;
					}
					if (statusName.equalsIgnoreCase("OK")) {
						upCount++;
					}
					if (statusName.equalsIgnoreCase("UNREACHABLE")
							|| statusName.equalsIgnoreCase("UNKNOWN")) {
						unknownCount++;
					}
					if (statusName.equalsIgnoreCase("PENDING_SERVICE")
							|| statusName.equalsIgnoreCase("PENDING")) {
						pendingCount++;
					}
					if (statusName.equalsIgnoreCase("WARNING_SERVICE")
							|| statusName.equalsIgnoreCase("WARNING")) {
						warningCount++;
					}
				} // end if
			}

			// sort services list with service_under_servicegroup__comparator
			Collections.sort(serviceEntityList,
					serviceUnderServiceGroupComparator);

			// add ID of service to list
			for (NetworkMetaEntity entity : serviceEntityList) {
				serviceList.add(entity.getObjectId());
			}
		}

		// set ToolTip data as List
		StringBuilder tooltip = new StringBuilder();
		tooltip.append(SERVICES_TEXT + serviceList.size() + Constant.BR);
		tooltip.append(troubledString + SERVICES_TEXT + troubledServices);
		// create Network Meta Entity to be put in for each Group
		NetworkMetaEntity serviceGroupMetaEntity = new NetworkMetaEntity(
				serviceGroup.getCategoryId(), serviceGroup.getName(),
				determineBubbleUpStatusForServiceGroup(serviceEntityList),
				NodeType.SERVICE_GROUP, tooltip.toString(), null, serviceList);
		StringBuffer serviceSummary = new StringBuffer();
		serviceSummary.append(downCount > 0 ? downCount
				+ " services CRITICAL, " : "");
		serviceSummary.append(warningCount > 0 ? warningCount
				+ " services WARNING, " : "");
		serviceSummary.append(unknownCount > 0 ? unknownCount
				+ " services UNKNOWN, " : "");
		serviceSummary.append(pendingCount > 0 ? pendingCount
				+ " services PENDING, " : "");
		serviceSummary.append(upCount > 0 ? upCount + " services OK" : "");
		serviceGroupMetaEntity.setSummary(serviceSummary.toString());
		// synchronized (serviceGroupMap) {
		Integer key = Integer.valueOf(serviceGroup.getCategoryId());
		if (serviceGroupMap.containsKey(key)) {
			serviceGroupMap.put(key, serviceGroupMetaEntity);
		} else {
			serviceGroupMap.putIfAbsent(key, serviceGroupMetaEntity);
		}
		// }
	}

	/**
	 * create HostGroup tree model by calling web service call getAllHostGroups
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private void createHostGroupModel() throws WSDataUnavailableException,
			GWPortalException {

		// LOGGER.debug(" Getting all HOST GROUPS !!!!!!!!!!");
		long start = System.currentTimeMillis();
		HostGroup[] allHostGroups = getWSFacade().getAllHostGroups();
		long end = System.currentTimeMillis();
		LOGGER.debug("@@@@@@@@@@@@@@ FINISHED Getting all HOST GROUPS !!!!!!!!!! TIME REQUIRED in milliseconds ["
				+ (end - start) + "]");

		// synchronized (hostGroupMap) {
		hostGroupMap.clear();
		if (allHostGroups != null) {
			for (HostGroup hostGroup : allHostGroups) {
				addHostGroupInMap(hostGroup);
			}
		}
		// }
	}

	/**
	 * create CustomGroup tree model by getting it from the user session
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private void createCustomGroupModel() throws WSDataUnavailableException,
			GWPortalException {
		customGroupMap.clear();
		customGroups = populateCustomGroups();
		if (customGroups != null) {
			for (CustomGroup cusGroup : customGroups) {
				NetworkMetaEntity customGroupMetaEntity = null;
				if (cusGroup.getParents() == null
						|| cusGroup.getParents().size() == 0) {
					String entityType = cusGroup.getEntityType()
							.getEntityType();
					ArrayList<Integer> childrenList = findCustomGroupChildren(cusGroup);
					customGroupMetaEntity = new NetworkMetaEntity(new Long(
							cusGroup.getGroupId()).intValue(),
							cusGroup.getGroupName(),
							NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.HOST_GROUP),
							NodeType.getNodeTypeByTypeName(entityType), null,
							null, childrenList);
					customGroupMetaEntity.setCustom(true);
					// NetworkObjectStatusEnum status =
					// determineBubbleUpStatusForCustomGroup(customGroupMetaEntity);
					// customGroupMetaEntity.setStatus(status);
					customGroupMap.put(
							new Long(cusGroup.getGroupId()).intValue(),
							customGroupMetaEntity);
				} // end if
			}
			// determineBubbleUpStatusForCustomGroups();
		} // end if

	}

	/**
	 * Helper to find the children for the customgroup
	 */
	private ArrayList<Integer> findCustomGroupChildren(CustomGroup cusGroup) {
		EntityType entType = cusGroup.getEntityType();
		ArrayList<Integer> childrenList = new ArrayList<Integer>();
		if (entType.getEntityType().equalsIgnoreCase(Constant.DB_HOST_GROUP)) {
			ArrayList<NetworkMetaEntity> sortList = new ArrayList<NetworkMetaEntity>();
			for (CustomGroupElement element : cusGroup.getElements()) {
				for (Map.Entry<Integer, NetworkMetaEntity> entry : hostGroupMap
						.entrySet()) {
					if (entry.getValue().getObjectId() == element
							.getElementId()) {
						sortList.add(entry.getValue());
						//childrenList.add(entry.getKey());
					}
				} // end for
			} // end for
			Collections.sort(sortList);
			for (NetworkMetaEntity meta : sortList) {
				childrenList.add(meta.getObjectId());
			}
		} // end if
		else if (entType.getEntityType().equalsIgnoreCase(
				Constant.DB_SERVICE_GROUP)) {
			ArrayList<NetworkMetaEntity> sortList = new ArrayList<NetworkMetaEntity>();
			for (CustomGroupElement element : cusGroup.getElements()) {
				for (Map.Entry<Integer, NetworkMetaEntity> entry : serviceGroupMap
						.entrySet()) {
					if (entry.getValue().getObjectId() == element
							.getElementId()) {
						sortList.add(entry.getValue());
						//childrenList.add(entry.getKey());
					} // end if
				} // end for
			} // end for
			Collections.sort(sortList);
			for (NetworkMetaEntity meta : sortList) {
				childrenList.add(meta.getObjectId());
			}
		} // end elseif
		else if (entType.getEntityType().equalsIgnoreCase(
				Constant.DB_CUSTOM_GROUP)) {
			for (CustomGroupElement element : cusGroup.getElements()) {
				CustomGroup group = findCustomGroupById(element.getElementId());
				childrenList.add((int) group.getGroupId());
			} // end if
		} // end if
		return childrenList;
	}

	/**
	 * helper to get the custom group based on name.
	 */
	public CustomGroup findCustomGroupById(long id) {
		customGroups = populateCustomGroups();
		for (CustomGroup group : customGroups) {
			if (group.getGroupId() == id) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Adds hsotgroup in host group map in form of NetworkMetaEntity
	 * 
	 * @param hostGroup
	 */
	private void addHostGroupInMap(HostGroup hostGroup) {
		// create Host Group children list (here Host list)
		Host[] hosts = hostGroup.getHosts();
		if (hosts != null) {
			List<Integer> hostList = new ArrayList<Integer>();
			int troubledHosts = 0;
			int troubledServices = 0;

			long downCount = 0;
			long warningCount = 0;
			long unknownCount = 0;
			long pendingCount = 0;
			long upCount = 0;
			long servicedownCount = 0;
			long servicewarningCount = 0;
			long serviceunknownCount = 0;
			long servicependingCount = 0;
			long serviceupCount = 0;
			int hostAcknowledgedCount = 0;
			int hostScheduledDownCount = 0;

			ArrayList<NetworkMetaEntity> hostEntityArr = new ArrayList<NetworkMetaEntity>();
			for (Host host : hosts) {
				// calculate troubled Hosts and Services
				NetworkMetaEntity hostEntity = getHostById(host.getHostID());
				if (hostEntity != null) {
					if (hostEntity.getStatus() != NetworkObjectStatusEnum.HOST_UP) {
						troubledHosts++;
					}
					Iterator<Integer> serviceEntities = getServicesUnderHost(Integer
							.valueOf(host.getHostID()));
					if (serviceEntities != null) {
						while (serviceEntities.hasNext()) {
							NetworkMetaEntity service = serviceMap
									.get(serviceEntities.next());
							if (service != null) {
								if (service.getStatus() != NetworkObjectStatusEnum.SERVICE_OK) {
									troubledServices++;
								}
								// DO Summary here
								String statusName = service.getStatus()
										.getStatus();
								if (statusName
										.equalsIgnoreCase("SCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("UNSCHEDULED CRITICAL")
										|| statusName
												.equalsIgnoreCase("CRITICAL")
										|| statusName
												.equalsIgnoreCase("SUSPENDED")) {
									servicedownCount++;
								}
								if (statusName.equalsIgnoreCase("OK")) {
									serviceupCount++;
								}
								if (statusName.equalsIgnoreCase("UNREACHABLE")
										|| statusName
												.equalsIgnoreCase("UNKNOWN")) {
									serviceunknownCount++;
								}
								if (statusName
										.equalsIgnoreCase("PENDING_SERVICE")
										|| statusName
												.equalsIgnoreCase("PENDING")) {
									servicependingCount++;
								}
								if (statusName
										.equalsIgnoreCase("WARNING_SERVICE")
										|| statusName
												.equalsIgnoreCase("WARNING")) {
									servicewarningCount++;
								}
							}
						}
					}
					// DO Summary here
					String statusName = hostEntity.getMonitorStatus();
					if (statusName.equalsIgnoreCase("DOWN")
							|| statusName.equalsIgnoreCase("SCHEDULED DOWN")
							|| statusName.equalsIgnoreCase("UNSCHEDULED DOWN")

							|| statusName.equalsIgnoreCase("SUSPENDED")) {
						downCount++;
					}
					if (statusName.equalsIgnoreCase("UP")
							|| statusName.equalsIgnoreCase("WARNING_HOST")
							|| statusName.equalsIgnoreCase("WARNING")) {
						upCount++;
					}
					if (statusName.equalsIgnoreCase("UNKNOWN")
							|| statusName.equalsIgnoreCase("UNREACHABLE")) {
						unknownCount++;
					}
					if (statusName.equalsIgnoreCase("PENDING_HOST")
							|| statusName.equalsIgnoreCase("PENDING")) {
						pendingCount++;
					}

					// If atleast one host is unacknowledged or scheduled down
					// then hostgroup becomes unacknowledged & scheduled down
					if (host.getPropertyTypeBinding() != null) {
						boolean acknowledged = false;
						Object acknowledgedObj = host.getPropertyTypeBinding()
								.getPropertyValue("isProblemAcknowledged");
						if (acknowledgedObj != null)
							acknowledged = ((Boolean) acknowledgedObj)
									.booleanValue();
						if (acknowledged)
							hostAcknowledgedCount++;

						hostEntity.setAcknowledged(acknowledged);

						int inDownTime = 0;
						Object inDownTimeObj = host.getPropertyTypeBinding()
								.getPropertyValue("ScheduledDowntimeDepth");
						if (inDownTimeObj != null)
							inDownTime = ((Integer) inDownTimeObj).intValue();
						hostEntity.setInScheduledDown(inDownTime);

						if (inDownTime == 1)
							hostScheduledDownCount++;
					} // end if

				}

				hostEntityArr.add(hostEntity);
				// Also update the hostMap
				hostMap.put(hostEntity.getObjectId(), hostEntity);
			}

			Collections.sort(hostEntityArr);
			for (NetworkMetaEntity hostEntity : hostEntityArr) {
				// Add to host list
				hostList.add(hostEntity.getObjectId());
			}

			// set ToolTip data as List
			StringBuilder tooltipList = new StringBuilder();
			tooltipList
					.append(aliasString + Constant.SPACE + hostGroup.getAlias()
							+ Constant.BR)
					.append(HOSTS_TEXT + hostList.size() + Constant.BR)
					.append(troubledString + HOSTS_TEXT + troubledHosts
							+ Constant.BR)
					.append(troubledString + SERVICES_TEXT + troubledServices);
			// create Network Meta Entity to be put in for each Host Group
			NetworkMetaEntity hostGroupMetaEntity = new NetworkMetaEntity(
					hostGroup.getHostGroupID(), hostGroup.getName(),
					determineBubbleUpStatusForHostGroup(hostEntityArr),
					NodeType.HOST_GROUP, tooltipList.toString(), null, hostList);

			// set the alias of host group
			hostGroupMetaEntity.setAlias(hostGroup.getAlias());
			StringBuffer hostSummary = new StringBuffer();
			hostSummary
					.append(downCount > 0 ? downCount + " hosts DOWN, " : "");
			hostSummary.append(warningCount > 0 ? warningCount
					+ " hosts WARNING, " : "");
			hostSummary.append(unknownCount > 0 ? unknownCount
					+ " hosts UNREACHABLE, " : "");
			hostSummary.append(pendingCount > 0 ? pendingCount
					+ " hosts PENDING, " : "");
			hostSummary.append(upCount > 0 ? upCount + " hosts UP. " : "");
			StringBuffer serviceSummary = new StringBuffer();
			serviceSummary.append(servicedownCount > 0 ? servicedownCount
					+ " services CRITICAL, " : "");
			serviceSummary.append(servicewarningCount > 0 ? servicewarningCount
					+ " services WARNING, " : "");
			serviceSummary.append(serviceunknownCount > 0 ? serviceunknownCount
					+ " services UNKNOWN, " : "");
			serviceSummary.append(servicependingCount > 0 ? servicependingCount
					+ " services PENDING, " : "");
			serviceSummary.append(serviceupCount > 0 ? serviceupCount
					+ " services OK" : "");
			hostGroupMetaEntity.setSummary(hostSummary.toString()
					+ serviceSummary.toString());

			// If atleast one host is unacknowledged or scheduled down then
			// hostgroup becomes unacknowledged & scheduled down
			hostGroupMetaEntity
					.setAcknowledged(hostAcknowledgedCount > 0 ? true : false);
			hostGroupMetaEntity
					.setInScheduledDown(hostScheduledDownCount > 0 ? 1 : 0);

			// insert to host group map

			// synchronized (hostGroupMap) {
			Integer key = Integer.valueOf(hostGroup.getHostGroupID());
			if (hostGroupMap.containsKey(key)) {
				hostGroupMap.put(key, hostGroupMetaEntity);
			} else {
				hostGroupMap.putIfAbsent(key, hostGroupMetaEntity);
			}
			// }
		} // end if
	}

	/**
	 * Determines the bubble up status for the hosrGroup here.
	 * 
	 * @param hostEntityList
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForHostGroup(
			ArrayList<NetworkMetaEntity> hostEntityList) {
		if (hostEntityList != null && hostEntityList.size() > 0) {
			String[] ranking = { "UNSCHEDULED DOWN", "WARNING_HOST",
					"UNREACHABLE", "SCHEDULED DOWN", "PENDING_HOST", "UP" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < hostEntityList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(hostEntityList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("WARNING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("WARNING",
											NodeType.HOST_GROUP);
						} else if ("PENDING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.HOST_GROUP);
						} else {

							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus(ranking[i],
											NodeType.HOST_GROUP);
						}
					} // end if
				} // end for
			} // end if
		} // end if
		return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * Determines the bubble up status for the hosrGroup here.
	 * 
	 * @param hostGroupList
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForHostGroupList(
			ArrayList<NetworkMetaEntity> hostGroupList) {
		if (hostGroupList != null && hostGroupList.size() > 0) {
			String[] ranking = { "UNSCHEDULED DOWN", "WARNING_HOST",
					"UNREACHABLE", "SCHEDULED DOWN", "PENDING_HOST", "UP" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < hostGroupList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(hostGroupList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("WARNING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("WARNING",
											NodeType.HOST_GROUP);
						} else if ("PENDING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.HOST_GROUP);
						} else {

							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus(ranking[i],
											NodeType.HOST_GROUP);
						}
					} // end if
				} // end for
			} // end if
		} // end if
		return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * Determines the bubble up status for the hosrGroup here.
	 * 
	 * @param customGroupList
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForCustomGroupList(
			ArrayList<NetworkMetaEntity> customGroupList) {
		if (customGroupList != null && customGroupList.size() > 0) {
			String[] ranking = { "UNSCHEDULED DOWN", "WARNING_HOST",
					"UNREACHABLE", "SCHEDULED DOWN", "PENDING_HOST", "UP" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < customGroupList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(customGroupList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("WARNING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("WARNING",
											NodeType.CUSTOM_GROUP);
						} else if ("PENDING_HOST".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.CUSTOM_GROUP);
						} else {

							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus(ranking[i],
											NodeType.CUSTOM_GROUP);
						}
					} // end if
				} // end for
			} // end if
		} // end if
			// If you reach here, then it means custom group is of service group
		return determineBubbleUpStatusForCustomServiceGroupList(customGroupList);
		// return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * Determines the bubbleupstatus for the servicegroup list here.
	 * 
	 * @param serviceGroupList
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForCustomServiceGroupList(
			List<NetworkMetaEntity> serviceGroupList) {
		if (serviceGroupList != null && serviceGroupList.size() > 0) {
			// Ranking is done in the following order for services
			String[] ranking = { "UNSCHEDULED CRITICAL", "WARNING",
					"PENDING_SERVICE", "SCHEDULED CRITICAL", "UNKNOWN", "OK" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < serviceGroupList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(serviceGroupList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("PENDING_SERVICE".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.CUSTOM_GROUP);
						}
						return NetworkObjectStatusEnum
								.getStatusEnumFromMonitorStatus(ranking[i],
										NodeType.CUSTOM_GROUP);
					} // end if
				} // end for
			} // end if
		} // end if
		return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * Checks the concrete entitytype for the given custom group. Returns either
	 * Hostgroup or servicegroup
	 */
	public String checkConcreteEntityType(CustomGroup group) {
		List<CustomGroupElement> elements = group.getElements();
		String entityType = group.getEntityType().getEntityType();
		for (CustomGroupElement element : elements) {
			// String elementName = element.getElementName();
			if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
				CustomGroup nextLevel = findCustomGroupById(element
						.getElementId());
				return checkConcreteEntityType(nextLevel);
			} else
				break;
		} // end for
		return entityType;
	}

	/**
	 * Determines the bubble up status for the customgroup here.
	 * 
	 * @param customGroupObject
	 * @return
	 */
	public NetworkObjectStatusEnum determineBubbleUpStatusForCustomGroup(
			NetworkMetaEntity customGroupObject) {
		List<Integer> children = customGroupObject.getChildNodeList();
		String entityType = customGroupObject.getType().getTypeName();
		ArrayList<NetworkMetaEntity> tempCustomGroupList = new ArrayList();
		ArrayList<NetworkMetaEntity> tempServiceGroupList = new ArrayList();
		ArrayList<NetworkMetaEntity> tempHostGroupList = new ArrayList();
		NetworkObjectStatusEnum aggregatedBubbleUpStatus = null;
		for (Integer childId : children) {
			NetworkMetaEntity child = null;
			if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
				child = this.getCustomGroupById(childId);
				if (child != null) {
					NetworkObjectStatusEnum cusGroupStatus = this
							.determineBubbleUpStatusForCustomGroup(child);
					child.setStatus(cusGroupStatus);
					tempCustomGroupList.add(child);
				} // end if
			}
			if (entityType.equalsIgnoreCase(Constant.UI_HOST_GROUP)) {
				child = this.getHostGroupById(childId);
				if (getExtendedRoleHostGroupList().isEmpty() || getExtendedRoleHostGroupList().contains(
						child.getName())) {
					tempHostGroupList.add(child);
				}
			}
			if (entityType.equalsIgnoreCase(Constant.UI_SERVICE_GROUP)) {
				child = this.getServiceGroupById(childId);
				if (getExtendedRoleServiceGroupList().isEmpty() || getExtendedRoleServiceGroupList().contains(
						child.getName())) {
					tempServiceGroupList.add(child);
				}
			}
		} // end for
		if (tempCustomGroupList.size() > 0) {
			aggregatedBubbleUpStatus = this
					.determineBubbleUpStatusForCustomGroupList(tempCustomGroupList);
			return aggregatedBubbleUpStatus;
		} // end if
		if (tempServiceGroupList.size() > 0) {
			NetworkObjectStatusEnum aggregatedSGBubbleUpStatus = this
					.determineBubbleUpStatusForCustomServiceGroupList(tempServiceGroupList);
			customGroupObject.setStatus(aggregatedSGBubbleUpStatus);
			return aggregatedSGBubbleUpStatus;
		}
		if (tempHostGroupList.size() > 0) {
			NetworkObjectStatusEnum aggregatedHGBubbleUpStatus = this
					.determineBubbleUpStatusForHostGroupList(tempHostGroupList);

			customGroupObject.setStatus(aggregatedHGBubbleUpStatus);
			return aggregatedHGBubbleUpStatus;
		}
		return aggregatedBubbleUpStatus;
	}

	/**
	 * Finds the bubleup status for the hostgroup
	 */
	private NetworkObjectStatusEnum reverseLookupByHostGroupName(
			String hostgroup) {
		for (Map.Entry<Integer, NetworkMetaEntity> entry : hostGroupMap
				.entrySet()) {
			Integer key = entry.getKey();
			NetworkMetaEntity value = entry.getValue();
			if (value.getName().equalsIgnoreCase(hostgroup)) {
				return value.getStatus();
			}
		}
		return null;
	}

	/**
	 * Determines the bubbleupstatus for the hosrGroup here.
	 * 
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForServiceGroup(
			List<NetworkMetaEntity> serviceEntityList) {
		if (serviceEntityList != null && serviceEntityList.size() > 0) {
			// Ranking is done in the following order for services
			String[] ranking = { "UNSCHEDULED CRITICAL", "WARNING",
					"PENDING_SERVICE", "SCHEDULED CRITICAL", "UNKNOWN", "OK" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < serviceEntityList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(serviceEntityList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("PENDING_SERVICE".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.SERVICE_GROUP);
						}
						return NetworkObjectStatusEnum
								.getStatusEnumFromMonitorStatus(ranking[i],
										NodeType.SERVICE_GROUP);
					} // end if
				} // end for
			} // end if
		} // end if
		return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * Determines the bubbleupstatus for the servicegroup list here.
	 * 
	 * @return
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForServiceGroupList(
			List<NetworkMetaEntity> serviceGroupList) {
		if (serviceGroupList != null && serviceGroupList.size() > 0) {
			// Ranking is done in the following order for services
			String[] ranking = { "UNSCHEDULED CRITICAL", "WARNING",
					"PENDING_SERVICE", "SCHEDULED CRITICAL", "UNKNOWN", "OK" };
			for (int i = 0; i < ranking.length; i++) {
				for (int j = 0; j < serviceGroupList.size(); j++) {
					if (ranking[i].equalsIgnoreCase(serviceGroupList.get(j)
							.getStatus().getMonitorStatusName())) {
						if ("PENDING_SERVICE".equalsIgnoreCase(ranking[i])) {
							return NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.SERVICE_GROUP);
						}
						return NetworkObjectStatusEnum
								.getStatusEnumFromMonitorStatus(ranking[i],
										NodeType.SERVICE_GROUP);
					} // end if
				} // end for
			} // end if
		} // end if
		return NetworkObjectStatusEnum.NO_STATUS;
	}

	/**
	 * create host and service tree model by calling web service call
	 * getAllHosts
	 * 
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private void createHostAndServiceModel() throws GWPortalException,
			WSDataUnavailableException {
		// Stopwatch.getInstance().start();
		// LOGGER
		// .debug(
		// "!!!!!!!!#$#$#$%#$!!!!!!!!!! Getting all SIMPLE HOSTS !!!!!#$@#$@#$!!!!!"
		// );
		// long start = System.currentTimeMillis();
		SimpleHost[] allHosts = getWSFacade().getSimpleHosts();
		// long end = System.currentTimeMillis();
		// LOGGER
		// .debug(
		// "!!!!!!!!!#$@#$@#$!!!!!!!!! FINISHED Getting all SIMPLE HOSTS !!!!#$@#$@#$!!!!!! TIME REQUIRED in milliseconds ["
		// + (end - start) + "]");

		// synchronized (this) {
		// clear both maps
		hostMap.clear();
		serviceMap.clear();
		// add hosts and services into maps
		addHostInMap(allHosts);
		// }
		// Stopwatch.getInstance().stop();
		// LOGGER.info("Execution time is " + Stopwatch.getInstance());
	}

	/**
	 * @param simpleHosts
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private void addHostInMap(SimpleHost[] simpleHosts)
			throws GWPortalException, WSDataUnavailableException {

		if (simpleHosts != null) {

			// synchronized (hostMap) {
			for (SimpleHost simpleHost : simpleHosts) {
				SimpleServiceStatus[] allServices = simpleHost
						.getSimpleServiceStatus();
				// create Host children list (here Service list)

				int troubledServiceCount = 0;

				long downCount = 0;
				long warningCount = 0;
				long unknownCount = 0;
				long pendingCount = 0;
				long upCount = 0;
				List<NetworkMetaEntity> serviceEntityList = new ArrayList<NetworkMetaEntity>();
				if (allServices != null) {
					// First: remove services from serviceMap associated with
					// this host (if host exists)
					NetworkMetaEntity hostById = getHostById(simpleHost
							.getHostID());
					if (null != hostById) {
						List<Integer> hostServicesList = hostById
								.getChildNodeList();
						if (null != hostServicesList) {
							for (Integer serviceId : hostServicesList) {
								serviceMap.remove(serviceId);
							}
						}
					}

					// Now: update services map under this host
					for (SimpleServiceStatus service : allServices) {
						NetworkObjectStatusEnum serviceStatus = addServiceInMap(
								simpleHost, service);
						serviceEntityList.add(getServiceById(Integer
								.valueOf(service.getServiceStatusID())));
						if (serviceStatus != NetworkObjectStatusEnum.SERVICE_OK) {
							troubledServiceCount++;
						}
						// DO Summary here
						String statusName = service.getMonitorStatus();
						if (statusName.equalsIgnoreCase("SCHEDULED CRITICAL")
								|| statusName
										.equalsIgnoreCase("UNSCHEDULED CRITICAL")
								|| statusName.equalsIgnoreCase("CRITICAL")
								|| statusName.equalsIgnoreCase("SUSPENDED")) {
							downCount++;
						}
						if (statusName.equalsIgnoreCase("OK")) {
							upCount++;
						}
						if (statusName.equalsIgnoreCase("UNREACHABLE")
								|| statusName.equalsIgnoreCase("UNKNOWN")) {
							unknownCount++;
						}
						if (statusName.equalsIgnoreCase("PENDING_SERVICE")
								|| statusName.equalsIgnoreCase("PENDING")) {
							pendingCount++;
						}
						if (statusName.equalsIgnoreCase("WARNING_SERVICE")
								|| statusName.equalsIgnoreCase("WARNING")) {
							warningCount++;
						}
					}
				} // end if

				List<Integer> serviceList = new ArrayList<Integer>();
				// sort list
				Collections.sort(serviceEntityList);
				for (NetworkMetaEntity entity : serviceEntityList) {
					serviceList.add(entity.getObjectId());
				}

				// set ToolTip data as List
				StringBuilder tooltip = new StringBuilder();
				// get Alias
				if (simpleHost.getAlias() != null) {
					tooltip.append(aliasString + Constant.SPACE
							+ simpleHost.getAlias() + Constant.BR);
				}

				tooltip.append(troubledString + SERVICES_TEXT
						+ troubledServiceCount);

				// If status is warning, rename it here
				String bubbleUpStatus = simpleHost.getBubbleUpStatus();
				if (bubbleUpStatus != null
						&& bubbleUpStatus.equalsIgnoreCase("WARNING")) {
					bubbleUpStatus = "WARNING_HOST";
				}

				// create Network Meta Entity to be put in for each Host
				NetworkMetaEntity hostMetaEntity = new NetworkMetaEntity(
						simpleHost.getHostID(), simpleHost.getName(),
						NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
								bubbleUpStatus, NodeType.HOST), NodeType.HOST,
						tooltip.toString(), simpleHost.getLastCheckTime(),
						serviceList, simpleHost.getServiceAvailability(),
						simpleHost.isAcknowledged(),
						simpleHost.getLastStateChange());
				hostMetaEntity.setLastPluginOutputString(simpleHost
						.getLastPlugInOutput());
				// hostMetaEntity.setNextCheckDateTime(simpleHost.getNextCheckTime());
				hostMetaEntity.setMonitorStatus(simpleHost.getMonitorStatus());
				StringBuffer serviceSummary = new StringBuffer();
				serviceSummary.append(downCount > 0 ? downCount
						+ " services CRITICAL, " : "");
				serviceSummary.append(warningCount > 0 ? warningCount
						+ " services WARNING, " : "");
				serviceSummary.append(unknownCount > 0 ? unknownCount
						+ " services UNKNOWN, " : "");
				serviceSummary.append(pendingCount > 0 ? pendingCount
						+ " services PENDING, " : "");
				serviceSummary.append(upCount > 0 ? upCount + " services OK"
						: "");
				hostMetaEntity.setSummary(serviceSummary.toString());
				// insert to host map
				Integer key = Integer.valueOf(simpleHost.getHostID());
				if (hostMap.containsKey(key)) {
					hostMap.put(key, hostMetaEntity);
				} else {
					hostMap.putIfAbsent(key, hostMetaEntity);
				}
				LOGGER.debug("added simple host in map successfully. ["
						+ simpleHost.getName() + "]");
			}
			// }
		} // end if (simpleHosts...)
	}

	/**
	 * Add services in map in form of NetworkMetaEntity
	 * 
	 * @param host
	 * @param service
	 * @return
	 */
	private NetworkObjectStatusEnum addServiceInMap(SimpleHost host,
			SimpleServiceStatus service) {
		// set ToolTip data as List
		String tooltip = HOST_TEXT + host.getName();
		NetworkObjectStatusEnum serviceStatus = NetworkObjectStatusEnum
				.getStatusEnumFromMonitorStatus(service.getMonitorStatus(),
						NodeType.SERVICE);
		NetworkMetaEntity serviceMetaEntity = new NetworkMetaEntity(
				service.getServiceStatusID(), service.getDescription(),
				serviceStatus, NodeType.SERVICE, tooltip,
				service.getLastCheckTime(), null, 0, service.isAcknowledged(),
				service.getLastStateChange());

		serviceMetaEntity.setExtendedName(new StringBuilder(service
				.getDescription()).append(Constant.OPENING_ROUND_BRACE)
				.append(host.getName()).append(Constant.CLOSING_ROUND_BRACE)
				.toString());

		// set the parent Host Id
		serviceMetaEntity.setParentId(host.getHostID());
		serviceMetaEntity.setNextCheckDateTime(service.getNextCheckTime());
		serviceMetaEntity.setLastPluginOutputString(service
				.getLastPlugInOutput());
		// serviceMetaEntity.setSummary(service.getLastPlugInOutput());

		// synchronized (serviceMap) {
		Integer key = Integer.valueOf(service.getServiceStatusID());
		if (serviceMap.containsKey(key)) {
			serviceMap.put(key, serviceMetaEntity);
		} else {
			serviceMap.putIfAbsent(key, serviceMetaEntity);
		}
		// }
		// LOGGER.debug("added simple service in map with id "
		// + service.getServiceStatusID() + " successfully");
		return serviceStatus;
	}

	/**
	 * Returns all Host Group NetworkMeta Entities list
	 * 
	 * @return all Host Group NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getAllHostGroups() {
		ArrayList<NetworkMetaEntity> array = new ArrayList<NetworkMetaEntity>(
				hostGroupMap.values());
		Collections.sort(array);
		return array.iterator();
	}

	/**
	 * Returns extended role based Host Group NetworkMeta Entities list
	 * 
	 * @param extHostGroupList
	 * 
	 * @return extended Host Group NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getExtRoleHostGroups(
			List<String> extHostGroupList) {

		ArrayList<NetworkMetaEntity> extRoleArrayList = new ArrayList<NetworkMetaEntity>();
		/*
		 * if host group is null or restricted then return empty extended Role
		 * Array List iterator.
		 */
		if (null != extHostGroupList
				&& !extHostGroupList
						.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			if (extHostGroupList.isEmpty()) {
				return this.getAllHostGroups();
			}

			ArrayList<NetworkMetaEntity> allHostGroups = new ArrayList<NetworkMetaEntity>(
					hostGroupMap.values());
			for (NetworkMetaEntity networkMetaEntity : allHostGroups) {
				if (extHostGroupList.contains(networkMetaEntity.getName())) {
					extRoleArrayList.add(networkMetaEntity);
				}
			}
			Collections.sort(extRoleArrayList);
		}
		return extRoleArrayList.iterator();
	}

	/**
	 * Returns all Host NetworkMeta Entities list
	 * 
	 * @return all Host NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getAllHosts() {
		return hostMap.values().iterator();
	}

	/**
	 * Returns all Service NetworkMeta Entities list
	 * 
	 * @return all Service NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getAllServices() {
		return serviceMap.values().iterator();
	}

	/**
	 * Returns all Host NetworkMeta Entities list under a particular Host Group
	 * 
	 * @param hostGroupId
	 * @return all Hosts under a HostGroup
	 */
	// TODO use iterator
	public Iterator<Integer> getHostsUnderHostGroup(Integer hostGroupId) {

		if (null != hostGroupId && hostGroupMap.containsKey(hostGroupId)) {
			NetworkMetaEntity hostGroupMetaEntity = hostGroupMap
					.get(hostGroupId);
			if (null != hostGroupMetaEntity) {
				List<Integer> childNodeList = hostGroupMetaEntity
						.getChildNodeList();
				return childNodeList.iterator();
			}
		}
		LOGGER.info("Cant get hosts for host group in ReferenceTreeMetaModel for host group Id: "
				+ hostGroupId);
		return null;
	}

	/**
	 * Returns all Service NetworkMeta Entities list under a particular Host
	 * Group
	 * 
	 * @param hostId
	 * @return all Services under a Host
	 */
	public Iterator<Integer> getServicesUnderHost(Integer hostId) {

		if (null != hostId && hostMap.containsKey(hostId)) {
			NetworkMetaEntity hostMetaEntity = hostMap.get(hostId);
			if (null != hostMetaEntity) {
				List<Integer> childNodeList = hostMetaEntity.getChildNodeList();
				return childNodeList.iterator();
			}
		}
		LOGGER.info("Cant get services for host in ReferenceTreeMetaModel for hostId: "
				+ hostId);
		return null;
	}

	/**
	 * Helper method for preferences only Group
	 * 
	 * @return all Services under a Host
	 */
	public String getAllServicesWithHostName() {
		Iterator<NetworkMetaEntity> allServiceNames = getAllowedServicesList();

		StringBuilder serviceNames = new StringBuilder();
		if (allServiceNames != null) {
			while (allServiceNames.hasNext()) {
				NetworkMetaEntity service = allServiceNames.next();
				serviceNames.append(service.getToolTip()
						.substring(Constant.SIX) + "^" + service.getName());
				serviceNames.append(CommonConstants.COMMA);
			}
		} // end if
		return serviceNames.toString();
	}

	/**
	 * Returns all Service Groups.
	 * 
	 * @return List
	 */
	public Iterator<NetworkMetaEntity> getAllServiceGroups() {
		ArrayList<NetworkMetaEntity> array = new ArrayList<NetworkMetaEntity>(
				serviceGroupMap.values());
		Collections.sort(array);
		return array.iterator();
	}

	/**
	 * Returns user role extended role Service Groups .
	 * 
	 * @param extServiceGroupList
	 * 
	 * @return List
	 */
	public Iterator<NetworkMetaEntity> getExtRoleServiceGroups(
			List<String> extServiceGroupList) {
		ArrayList<NetworkMetaEntity> extRoleArray = new ArrayList<NetworkMetaEntity>();
		/*
		 * if service group is null or restricted then return empty extended
		 * Role Array List iterator.
		 */
		if (extServiceGroupList != null
				&& !extServiceGroupList
						.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			if (extServiceGroupList.isEmpty()) {
				return this.getAllServiceGroups();
			}

			ArrayList<NetworkMetaEntity> allServiceGroups = new ArrayList<NetworkMetaEntity>(
					serviceGroupMap.values());
			for (NetworkMetaEntity networkMetaEntity : allServiceGroups) {
				if (extServiceGroupList.contains(networkMetaEntity.getName())) {
					extRoleArray.add(networkMetaEntity);
				}
			}
			Collections.sort(extRoleArray);
		}
		return extRoleArray.iterator();
	}

	/**
	 * Populates the CustomGroups
	 * 
	 * @return
	 */
	private List<CustomGroup> populateCustomGroups() {
		List<CustomGroup> customGroupsLocal = null;
		try {
			Collection<CustomGroup> custGroupCol = getWSFacade()
					.findCustomGroups();
			customGroupsLocal = new ArrayList(custGroupCol);
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());
		}
		return customGroupsLocal;
	}

	/**
	 * Returns CustomGroups if any .
	 * 
	 * @return List
	 */
	public Iterator<NetworkMetaEntity> getCustomGroups() {
		try {
			createCustomGroupModel();
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());
		}
		ArrayList<NetworkMetaEntity> array = new ArrayList<NetworkMetaEntity>(
				customGroupMap.values());
		Collections.sort(array);
		return array.iterator();
	}

	/**
	 * Returns raw CustomGroups if any .
	 *
	 * @return List
	 */
	public List<CustomGroup> getRawCustomGroups() {
		if (customGroups != null)
			return customGroups;
		else
			return populateCustomGroups();

	}

	/**
	 * Returns root CustomGroups if any .
	 *
	 * @return List
	 */
	public List<CustomGroup> getRootCustomGroups() {
		List<CustomGroup> rootCustomGroups = new ArrayList<CustomGroup>();
		customGroups = populateCustomGroups();

		for (CustomGroup cusGroup : customGroups) {
			if (cusGroup.getParents() == null
					|| cusGroup.getParents().size() == 0) {
				rootCustomGroups.add(cusGroup);
			} // end if
		} // end for
		return rootCustomGroups;
	}

	/**
	 * Returns CustomGroupsbyID.
	 *
	 * @return List
	 */
	public NetworkMetaEntity getCustomGroupById(Integer groupId) {

		if (customGroups != null) {
			for (CustomGroup group : customGroups) {
				if (group.getGroupId() == groupId) {
					ArrayList<Integer> childrenList = findCustomGroupChildren(group);
					NodeType nodeType = null;
					if (group.getEntityType().getEntityType()
							.equalsIgnoreCase(Constant.DB_HOST_GROUP)) {
						nodeType = NodeType.HOST_GROUP;
					}
					if (group.getEntityType().getEntityType()
							.equalsIgnoreCase(Constant.DB_SERVICE_GROUP)) {
						nodeType = NodeType.SERVICE_GROUP;
					}
					if (group.getEntityType().getEntityType()
							.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
						nodeType = NodeType.CUSTOM_GROUP;
					}
					NetworkMetaEntity customGroupMetaEntity = new NetworkMetaEntity(
							new Long(group.getGroupId()).intValue(),
							group.getGroupName(),
							NetworkObjectStatusEnum
									.getStatusEnumFromMonitorStatus("PENDING",
											NodeType.HOST_GROUP), nodeType,
							null, null, childrenList);
					customGroupMetaEntity.setCustom(true);
					NetworkObjectStatusEnum status = determineBubbleUpStatusForCustomGroup(customGroupMetaEntity);
					if (status != null) {
						NetworkObjectStatusEnum adjustedStatus = NetworkObjectStatusEnum
								.getStatusEnumFromMonitorStatus(
										status.getMonitorStatusName(),
										NodeType.CUSTOM_GROUP);
						customGroupMetaEntity.setStatus(adjustedStatus);
						return customGroupMetaEntity;
					} else
						return null;
				}
			}
		} // end if
		return null;
	}

	/**
	 * Returns all Services under a particular Service Group.
	 * 
	 * @param serviceGroupId
	 * @return List
	 */
	public Iterator<Integer> getServicesUnderServiceGroup(Integer serviceGroupId) {
		if (null != serviceGroupId
				&& serviceGroupMap.containsKey(serviceGroupId)) {
			NetworkMetaEntity serviceGroupMetaEntity = serviceGroupMap
					.get(serviceGroupId);
			if (null != serviceGroupMetaEntity) {
				return serviceGroupMetaEntity.getChildNodeList().iterator();
			}
		}
		LOGGER.info("Cant get services for service Group in ReferenceTreeMetaModel for service group id: "
				+ serviceGroupId);
		return null;
	}

	/**
	 * Returns Service Group Entity by Id. This method can return null!
	 * 
	 * @param serviceGroupId
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getServiceGroupById(Integer serviceGroupId) {
		return serviceGroupMap.get(serviceGroupId);
	}

	/**
	 * Returns Service Entity by Id. This method can return null!
	 * 
	 * @param serviceStatusID
	 * 
	 * @return NetworkMetaEntity
	 */

	public NetworkMetaEntity getServiceById(Integer serviceStatusID) {
		// TODO handle a case where RTMM does not have this Service object. In
		// this case fetch the service using WS APIs
		// (getSimpleServiceByCriteria()) and create ServiceStatus
		// NetworkMetaEntity object.
		return serviceMap.get(serviceStatusID);
	}

	/**
	 * Returns Host Group Entity by Id. This method can return null!
	 * 
	 * @param hostGroupId
	 * 
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getHostGroupById(Integer hostGroupId) {
		return hostGroupMap.get(hostGroupId);
	}

	/**
	 * Returns Host Entity by Id. This method can return null!
	 * 
	 * @param hostID
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getHostById(Integer hostID) {
		return hostMap.get(hostID);
	}

	/**
	 * Returns Entity by Name. This method can return null!
	 * 
	 * @param nodeType
	 * @param entityName
	 * @return Entity by Name. Null if not found.
	 */
	public NetworkMetaEntity getEntityByName(NodeType nodeType,
			String entityName) {
		Collection<NetworkMetaEntity> values = null;
		switch (nodeType) {
		case HOST:
			values = hostMap.values();
			break;
		case HOST_GROUP:
			values = hostGroupMap.values();
			break;
		case SERVICE:
			values = serviceMap.values();
			break;
		case SERVICE_GROUP:
			values = serviceGroupMap.values();
			break;
		default:
			break;
		}

		if (values != null) {
			for (NetworkMetaEntity networkMetaEntity : values) {
				if (networkMetaEntity.getName().equalsIgnoreCase(entityName)) {
					return networkMetaEntity;
				}
			}
		}
		return null;
	}

	/**
	 * Returns Service NetworkMetaEntity by Service and Host name
	 * 
	 * @param hostName
	 * @param serviceName
	 * @return Service NetworkMetaEntity by Service and Host name
	 */
	public NetworkMetaEntity getServiceEntityByHostAndServiceName(
			String hostName, String serviceName) {
		NetworkMetaEntity hostMetaEntity = getEntityByName(NodeType.HOST,
				hostName);
		if (hostMetaEntity != null) {
			List<Integer> childNodeList = hostMetaEntity.getChildNodeList();
			for (Integer serviceId : childNodeList) {
				NetworkMetaEntity serviceEntityById = getServiceById(serviceId);
				if (serviceEntityById.getName().equalsIgnoreCase(serviceName)) {
					return serviceEntityById;
				}
			}
		} // end if
		return null;
	}

	/**
	 * Returns service group list for service
	 * 
	 * @param serviceId
	 * @return service group list for service
	 */
	public List<NetworkMetaEntity> getServiceGroupListForService(int serviceId) {
		List<NetworkMetaEntity> serviceGroupList = new ArrayList<NetworkMetaEntity>();
		Collection<NetworkMetaEntity> serviceGroupMetaEntities = serviceGroupMap
				.values();
		for (NetworkMetaEntity serviceGroupEntity : serviceGroupMetaEntities) {
			if (serviceGroupEntity.getChildNodeList() != null
					&& serviceGroupEntity.getChildNodeList().contains(
							Integer.valueOf(serviceId))) {
				serviceGroupList.add(serviceGroupEntity);
			}
		}
		return serviceGroupList;
	}

	/**
	 * *CURRENTLY UNUSED*
	 * 
	 * This method is called when there is no data is Host Tree, and user wants
	 * to refresh/rebuild Tree. It constructs entire Host Maps again.
	 */
	public void rebuildModel() {
		// Create all models again
		LOGGER.info("Rebuilding data structures in referencetreeMetaModel, to rebuild trees in tree view.");

		try {
			createModels();
		} catch (GWPortalGenericException e) {
			LOGGER.error("Error occured while creating Tree Model in ReferenceTreeMetaModel(). "
					+ "Tree portlet and other dependent portlets will not work.");
		}
	}

	/**
	 * updates host group (JMS push)
	 * 
	 * @param id
	 */
	public void updateHostGroup(Integer id) {
		try {
            if (id == null)
                id = 0;
			HostGroup hostGroup = getWSFacade().getHostGroupsById(id,
					false);
			if (hostGroup != null) {
				// update hosts under group
				SimpleHost[] hosts = getWSFacade().getHostsUnderHostGroup(
						hostGroup.getName(), true);

				// synchronized (this) {
				// duplicate entries will be overwritten
				addHostInMap(hosts);
				addHostGroupInMap(hostGroup);
				// }
				return;
			}
		} catch (GWPortalGenericException ge) {
			LOGGER.error("cant get host group for id: " + id
					+ " probably its deleted.");
            initialized = false; // force refresh
        }
		// HostGroup not found, delete form map
		// synchronized (hostGroupMap) {
		hostGroupMap.remove(id);
		// }
	}

	/**
	 * Updates Service group
	 * 
	 * @param id
	 */
	public void updateServiceGroup(Integer id) {
		try {
			Category serviceGroup = getWSFacade().getCategoryByID(id);
			if (null != serviceGroup) {
				List<Integer> serviceIds = new ArrayList<Integer>();

				CategoryEntity[] categoryEntities = serviceGroup
						.getCategoryEntities();
				if (null != categoryEntities) {
					for (CategoryEntity categoryEntity : categoryEntities) {
						// LOGGER.warn("##### Adding Service with Id ["
						// + categoryEntity.getObjectID()
						// + "] in Service Group ["
						// + serviceGroup.getName() + "]");
						serviceIds.add(categoryEntity.getObjectID());
					}
				}

				addServiceGroupInMap(serviceGroup, serviceIds);
				return;
			}
		} catch (GWPortalGenericException ge) {
			LOGGER.error("cant get service group for id: " + id
					+ " probably its deleted.");
            initialized = false; // force refresh
		}
		// not found, delete from map
		serviceGroupMap.remove(id);
	}

	/**
	 * Removes the host from hostMap
	 * 
	 * @param id
	 */
	public void removeHost(int id) {
		// not found, remove from map
		// synchronized (hostMap) {
		hostMap.remove(Integer.valueOf(id));
		// }

	}

	/**
	 * Removes the host from serviceMap
	 * 
	 * @param id
	 */
	public void removeService(int id) {
		// synchronized (serviceMap) {
		serviceMap.remove(Integer.valueOf(id));
		// }
	}

	// /**
	// * UNUSED - please don't delete this method //add host into map
	// *
	// * @param host
	// * @throws GWPortalException
	// * @throws WSDataUnavailableException
	// */
	// @SuppressWarnings("unused")
	// private void addHostInMap(Host host) throws GWPortalException,
	// WSDataUnavailableException {
	// // create Host children list (here Service list)
	// ServiceStatus[] allServices = getWSFacade()
	// .getServicesByHostId(host.getHostID());
	// int troubledServiceCount = 0;
	// List<Integer> serviceList = new ArrayList<Integer>();
	// if (allServices != null) {
	// for (ServiceStatus service : allServices) {
	// serviceList.add(Integer.valueOf(service.getServiceStatusID()));
	// NetworkObjectStatusEnum serviceStatus = addServiceInMap(
	// service, host);
	// if (serviceStatus ==
	// NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED) {
	// troubledServiceCount++;
	// }
	// }
	//
	// // set ToolTip data as List
	// StringBuilder tooltip = new StringBuilder();
	// // get Alias
	// PropertyTypeBinding binding = host.getPropertyTypeBinding();
	// if (binding != null) {
	// StringProperty stringProperty = binding
	// .getStringProperty(Constant.ALIAS);
	// if (stringProperty != null) {
	// tooltip.append(aliasString + Constant.SPACE
	// + stringProperty.getValue() + Constant.BR);
	// }
	// }
	// tooltip.append(troubledString + SERVICES_TEXT
	// + troubledServiceCount);
	// // create Network Meta Entity to be put in for each Host
	// NetworkMetaEntity hostMetaEntity = new NetworkMetaEntity(host
	// .getHostID(), host.getName(), MonitorStatusUtilities
	// .getEntityStatus(host, NodeType.HOST), NodeType.HOST,
	// tooltip.toString(), host.getLastCheckTime(), serviceList);
	// // synchronized (hostMap) {
	// // insert to host map
	// hostMap.put(host.getHostID(), hostMetaEntity);
	// // }
	// }
	// }

	// /**
	// * UNUSED - please don't delete this method
	// *
	// * @param host
	// * @param service
	// * @return
	// */
	// private NetworkObjectStatusEnum addServiceInMap(ServiceStatus service,
	// Host host) {
	// // set ToolTip data as List
	// String tooltip = HOST_TEXT + host.getName();
	// NetworkObjectStatusEnum serviceStatus = MonitorStatusUtilities
	// .getEntityStatus(service, NodeType.SERVICE);
	// NetworkMetaEntity serviceMetaEntity = new NetworkMetaEntity(service
	// .getServiceStatusID(), service.getDescription(), serviceStatus,
	// NodeType.SERVICE, tooltip, service.getLastCheckTime(), null);
	// // SYNC BLOCK
	// // synchronized (serviceMetaEntity) {
	// serviceMap.put(service.getServiceStatusID(), serviceMetaEntity);
	// // }
	// return serviceStatus;
	// }

	/**
	 * Returns count of hosts under host group identified by provided Id
	 * 
	 * @param hostGroupId
	 * @return int
	 */
	public int getHostCountForHostGroup(Integer hostGroupId) {
		NetworkMetaEntity hostGroup = getHostGroupById(hostGroupId);
		if (hostGroup != null && hostGroup.getChildNodeList() != null) {
			return hostGroup.getChildNodeList().size();
		} else {
			return 0;
		}
	}

	/**
	 * Returns host group list for host
	 * 
	 * @param hostId
	 * @return host group list for host
	 */
	public List<NetworkMetaEntity> getHostGroupListForHost(int hostId) {
		List<NetworkMetaEntity> groupList = new ArrayList<NetworkMetaEntity>();
		Collection<NetworkMetaEntity> hostGroupMetaEntities = hostGroupMap
				.values();
		for (NetworkMetaEntity hostGroupEntity : hostGroupMetaEntities) {
			if (hostGroupEntity.getChildNodeList() != null
					&& hostGroupEntity.getChildNodeList().contains(
							Integer.valueOf(hostId))) {
				groupList.add(hostGroupEntity);
			}
		}
		return groupList;
	}

	/**
	 * Returns Parent Host (Host NetworkMetaEntity) Of Service by taking service
	 * Id as a parameter.
	 * 
	 * @param serviceStatusId
	 * @return Parent Host NetworkMetaEntity.
	 */
	public NetworkMetaEntity getParentHostOfService(Integer serviceStatusId) {
		NetworkMetaEntity serviceMetaEntity = serviceMap.get(serviceStatusId);
		if (null != serviceMetaEntity) {
			return hostMap.get(serviceMetaEntity.getParentId());
		}
		return null;
	}

	/**
	 * Return comma separated all host name list
	 * 
	 * @return comma separated all host name list
	 */
	public String getAllHostNameList() {
		// get extended role based host group list
		List<String> extRoleHostGroupList = getExtendedRoleHostGroupList();
		// if role based HG list is not defined, return all Hosts
		if (extRoleHostGroupList.isEmpty()) {
			Iterator<NetworkMetaEntity> allHostNames = getAllHosts();
			StringBuilder hostNames = new StringBuilder();
			if (allHostNames != null) {
				while (allHostNames.hasNext()) {
					NetworkMetaEntity host = allHostNames.next();
					hostNames.append(host.getName());
					hostNames.append(CommonConstants.COMMA);
				}
			} // end if
			return hostNames.toString();
		}
		// if restricted, return empty string
		if (extRoleHostGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			return Constant.EMPTY_STRING;
		}
		// else return hosts under the allowed HG list
		Set<String> allowedHosts = new HashSet<String>();
		for (Iterator<String> hgIterator = extRoleHostGroupList.iterator(); hgIterator
				.hasNext();) {
			NetworkMetaEntity hgEntity = getEntityByName(NodeType.HOST_GROUP,
					hgIterator.next());
			List<Integer> hostList = hgEntity.getChildNodeList();
			if (null != hostList) {
				for (Integer hostId : hostList) {
					NetworkMetaEntity hostEntity = hostMap.get(hostId);
					if (null != hostEntity) {
						allowedHosts.add(hostEntity.getName());
					}
				}
			}
		}
		return getCommaSeparatedString(allowedHosts.iterator());
	}

	/**
	 * Gets the allowed service statistics
	 */
	public Map<String, Long> getAllowedServiceStatistics() {
		long CRITICAL_UNSCHEDULED_COUNTER = 0;
		long CRITICAL_SCHEDULED_COUNTER = 0;
		long WARNING_COUNTER = 0;
		long UNKNOWN_COUNTER = 0;
		long PENDING_COUNTER = 0;
		long OK_COUNTER = 0;
		Map<String, Long> serviceStatistics = new HashMap<String, Long>();
		Iterator<NetworkMetaEntity> allowedServiceList = this
				.getAllowedServicesList();
		while (allowedServiceList.hasNext()) {
			NetworkMetaEntity allowedService = allowedServiceList.next();
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_OK)
				OK_COUNTER++;
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED)
				CRITICAL_UNSCHEDULED_COUNTER++;
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED)
				CRITICAL_SCHEDULED_COUNTER++;
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_WARNING)
				WARNING_COUNTER++;
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_PENDING)
				PENDING_COUNTER++;
			if (allowedService.getStatus() == NetworkObjectStatusEnum.SERVICE_UNKNOWN)
				UNKNOWN_COUNTER++;
		}
		serviceStatistics.put("OK", OK_COUNTER);
		serviceStatistics.put("UNSCHEDULED CRITICAL",
				CRITICAL_UNSCHEDULED_COUNTER);
		serviceStatistics.put("SCHEDULED CRITICAL", CRITICAL_SCHEDULED_COUNTER);
		serviceStatistics.put("WARNING", WARNING_COUNTER);
		serviceStatistics.put("PENDING", PENDING_COUNTER);
		serviceStatistics.put("UNKNOWN", UNKNOWN_COUNTER);
		serviceStatistics.put("total", OK_COUNTER
				+ CRITICAL_UNSCHEDULED_COUNTER + CRITICAL_SCHEDULED_COUNTER
				+ WARNING_COUNTER + UNKNOWN_COUNTER);
		return serviceStatistics;
	}

	/**
	 * Returns allowed services list
	 * 
	 * @return list of allowed services
	 */
	public Iterator<NetworkMetaEntity> getAllowedServicesList() {
		// get extended role based host group list
		List<String> extRoleHostGroupList = getExtendedRoleHostGroupList();
		// get extended role based service group list
		List<String> extRoleServiceGroupList = getExtendedRoleServiceGroupList();

		// if both the lists are empty, return all service names
		if (extRoleHostGroupList.isEmpty() && extRoleServiceGroupList.isEmpty()) {
			return getAllServices();
		}

		// or else return the allowed - filtered services list by parsing SG and
		// HG list
		Set<NetworkMetaEntity> allowedServices = new HashSet<NetworkMetaEntity>();
		// first check in SG list
		if (!extRoleServiceGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			for (Iterator<String> sgIterator = extRoleServiceGroupList
					.iterator(); sgIterator.hasNext();) {
				NetworkMetaEntity sgEntity = getEntityByName(
						NodeType.SERVICE_GROUP, sgIterator.next());
				List<Integer> serviceList = sgEntity.getChildNodeList();
				if (null != serviceList) {
					for (Integer serviceId : serviceList) {
						NetworkMetaEntity serviceEntity = serviceMap
								.get(serviceId);
						if (null != serviceEntity) {
							allowedServices.add(serviceEntity);
						}
					}
				}
			}
		}
		// Now check for HG list. Get all allowed hosts under HG and then
		// services inside those hosts
		// if restricted, return empty string
		if (!extRoleHostGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			for (Iterator<String> hgIterator = extRoleHostGroupList.iterator(); hgIterator
					.hasNext();) {
				NetworkMetaEntity hgEntity = getEntityByName(
						NodeType.HOST_GROUP, hgIterator.next());
				List<Integer> hostList = hgEntity.getChildNodeList();
				if (null != hostList) {
					for (Integer hostId : hostList) {
						NetworkMetaEntity hostEntity = hostMap.get(hostId);
						if (null != hostEntity) {
							List<Integer> serviceList = hostEntity
									.getChildNodeList();
							if (null != serviceList) {
								for (Integer serviceId : serviceList) {
									NetworkMetaEntity serviceEntity = serviceMap
											.get(serviceId);
									if (null != serviceEntity) {
										allowedServices.add(serviceEntity);
									}
								}
							}
						}
					}
				}
			}
		}
		return allowedServices.iterator();
	}

	/**
	 * Return comma separated list of all service group names
	 * 
	 * @return comma separated list of all service group names
	 */
	public String getAllServiceGroupNameList() {
		// get extended role based service group list
		List<String> extRoleServiceGroupList = getExtendedRoleServiceGroupList();

		// if role based SG list is defined, return it
		if (!extRoleServiceGroupList.isEmpty()) {
			return getCommaSeparatedString(extRoleServiceGroupList.iterator());
		}
		if (extRoleServiceGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			return Constant.EMPTY_STRING;
		}

		// else return all service groups
		Iterator<NetworkMetaEntity> allServiceGroups = getAllServiceGroups();
		StringBuilder serviceGroupNames = new StringBuilder();
		if (allServiceGroups != null) {
			while (allServiceGroups.hasNext()) {
				NetworkMetaEntity serviceGroup = allServiceGroups.next();
				serviceGroupNames.append(serviceGroup.getName());
				serviceGroupNames.append(CommonConstants.COMMA);
			}
		} // end if
		return serviceGroupNames.toString();
	}

	/**
	 * Return comma separated list of all host group names
	 * 
	 * @return comma separated list of all host group names
	 */
	public String getAllHostGroupNameList() {
		// get extended role based host group list
		List<String> extRoleHostGroupList = getExtendedRoleHostGroupList();

		// if role based HG list is defined, return it
		if (!extRoleHostGroupList.isEmpty()) {
			return getCommaSeparatedString(extRoleHostGroupList.iterator());
		}
		// if restricted, return empty string
		if (extRoleHostGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			return Constant.EMPTY_STRING;
		}

		// else return all host groups
		Iterator<NetworkMetaEntity> allHostGroupNames = getAllHostGroups();
		StringBuilder hostGroupNames = new StringBuilder();
		if (allHostGroupNames != null) {
			while (allHostGroupNames.hasNext()) {
				NetworkMetaEntity hostGroup = allHostGroupNames.next();
				hostGroupNames.append(hostGroup.getName());
				hostGroupNames.append(CommonConstants.COMMA);
			}
		} // end if
		return hostGroupNames.toString();
	}

	/**
	 * Returns comma separated string.
	 * 
	 * @param iterator
	 *            of list / set ...
	 * @return Comma separated string
	 */
	private String getCommaSeparatedString(Iterator<String> iterator) {
		StringBuilder commaSeparatedString = new StringBuilder();
		while (null != iterator && iterator.hasNext()) {
			commaSeparatedString.append(iterator.next());
			commaSeparatedString.append(CommonConstants.COMMA);
		}
		return commaSeparatedString.toString();
	}

	/**
	 * Gets extended role based host group list.
	 * 
	 * @return extended role based host group list
	 */
	public List<String> getExtendedRoleHostGroupList() {
		// get the UserRoleBean managed instance
		UserExtendedRoleBean userExtendedRoleBean = PortletUtils
				.getUserExtendedRoleBean();

		if (null != userExtendedRoleBean) {
			// get extended role based host group list
			return userExtendedRoleBean.getExtRoleHostGroupList();
		}

		List<String> extRoleHostGroupList = new ArrayList<String>();
		// getting user role based host group and service group list
		List<ExtendedUIRole> extRoleList = PortletUtils
				.getExtendedRoleAttributes();
		/*
		 * Loop through the List of extendedAttributes. Remember, a user can be
		 * assigned to more than one role!
		 */
		if (extRoleList != null) {
			for (ExtendedUIRole extRole : extRoleList) {
				if (null != extRole.getHgList()) {
					extRoleHostGroupList.addAll(CommonUtils
							.convert2HGList(extRole.getHgList()));
				}
			} // end for
		} // end if
		return extRoleHostGroupList;
	}

	/**
	 * Gets extended role based service group list.
	 * 
	 * @return extended role based service group list
	 */
	public List<String> getExtendedRoleServiceGroupList() {
		// get the UserRoleBean managed instance
		UserExtendedRoleBean userExtendedRoleBean = PortletUtils
				.getUserExtendedRoleBean();

		if (null != userExtendedRoleBean) {
			// get extended role based service group list
			return userExtendedRoleBean.getExtRoleServiceGroupList();
		}

		// getting user role based host group and service group list
		List<ExtendedUIRole> extRoleList = PortletUtils
				.getExtendedRoleAttributes();
		List<String> extRoleServiceSroupList = new ArrayList<String>();
		/*
		 * Loop through the List of extendedAttributes. Remember, a user can be
		 * assigned to more than one role!
		 */
		if (extRoleList != null) {
			for (ExtendedUIRole extRole : extRoleList) {
				if (null != extRole.getSgList()) {
					extRoleServiceSroupList.addAll(CommonUtils
							.convert2SGList(extRole.getSgList()));
				}
			} // end for
		} // end if
		return extRoleServiceSroupList;
	}

	/**
	 * Checks if user can access entire network data as per the ext. roles
	 * defined for him.
	 * 
	 * @return true if user can access entire network data as per the ext. roles
	 *         defined for him.
	 */
	public boolean canAccessEntireNetworkData() {
		if (!getExtendedRoleHostGroupList().isEmpty()
				|| !getExtendedRoleServiceGroupList().isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Returns Service NetworkMetaEntity by Service and Service group name
	 * 
	 * @param serviceGroup
	 *            group name
	 * @param serviceName
	 * @return Service NetworkMetaEntity
	 */
	public NetworkMetaEntity getServiceEntityByServiceGroupAndServiceName(
			String serviceGroup, String serviceName) {
		NetworkMetaEntity hostMetaEntity = getEntityByName(
				NodeType.SERVICE_GROUP, serviceGroup);
		List<Integer> childNodeList = hostMetaEntity.getChildNodeList();
		for (Integer serviceId : childNodeList) {
			NetworkMetaEntity serviceEntityById = getServiceById(serviceId);
			if (serviceEntityById.getName().equalsIgnoreCase(serviceName)) {
				return serviceEntityById;
			}
		}
		return null;
	}

	/**
	 * This method checks any Node against Extended Role Permissions passed to
	 * it. Returns default node to be displayed if permissions are not matching.
	 * 
	 * @param nodeID
	 * @param nodeType
	 * @param nodeName
	 * @param extRoleHostGroupList
	 * @param extRoleServiceGroupList
	 * @param defaultHostGroup
	 * @param defaultServiceGroup
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getEntityByExtendedRolePermissions(int nodeID,
			NodeType nodeType, String nodeName,
			List<String> extRoleHostGroupList,
			List<String> extRoleServiceGroupList, String defaultHostGroup,
			String defaultServiceGroup) {
		switch (nodeType) {
		case HOST_GROUP:
			if (extRoleHostGroupList.isEmpty()
					|| extRoleHostGroupList.contains(nodeName)) {
				NetworkMetaEntity hgEntity = hostGroupMap.get(nodeID);
				if (null != hgEntity) {
					return hgEntity;
				}
			}
			break;

		case SERVICE_GROUP:
			if (extRoleServiceGroupList.isEmpty()
					|| extRoleServiceGroupList.contains(nodeName)) {
				NetworkMetaEntity sgEntity = serviceGroupMap.get(nodeID);
				if (null != sgEntity) {
					return sgEntity;
				}
			}
			break;

		case HOST:
			if (extRoleHostGroupList.isEmpty()) {
				NetworkMetaEntity hostEntity = hostMap.get(nodeID);
				if (null == hostEntity) {
					return getDefaultEntityByUserRole(defaultHostGroup,
							defaultServiceGroup, extRoleHostGroupList,
							extRoleServiceGroupList);
				}
				return hostEntity;
			}
			// check if this host is in the allowed HG list
			for (Iterator<String> hgIterator = extRoleHostGroupList.iterator(); hgIterator
					.hasNext();) {
				NetworkMetaEntity hgEntity = getEntityByName(
						NodeType.HOST_GROUP, hgIterator.next());
				if (null != hgEntity) {
					List<Integer> childNodeList = hgEntity.getChildNodeList();
					if (null != childNodeList && childNodeList.contains(nodeID)) {
						return hostMap.get(nodeID);
					}
				}
			}
			break;

		case SERVICE:
			// firstly, check if this service is in the allowed SG list
			for (Iterator<String> sgIterator = extRoleServiceGroupList
					.iterator(); sgIterator.hasNext();) {
				NetworkMetaEntity sgEntity = getEntityByName(
						NodeType.SERVICE_GROUP, sgIterator.next());
				if (null != sgEntity) {
					List<Integer> childNodeList = sgEntity.getChildNodeList();
					if (null != childNodeList && childNodeList.contains(nodeID)) {
						return serviceMap.get(nodeID);
					}
				}
			}
			/*
			 * Service is not in the list of allowed service groups. Hence check
			 * for hosts in the allowed Host Groups list
			 */
			NetworkMetaEntity serviceMetaEntity = serviceMap.get(nodeID);
			if (null != serviceMetaEntity) {
				if (extRoleHostGroupList.isEmpty()) {
					return serviceMetaEntity;
				}

				Integer hostParentId = serviceMetaEntity.getParentId();
				for (Iterator<String> hgIterator = extRoleHostGroupList
						.iterator(); hgIterator.hasNext();) {
					NetworkMetaEntity hgEntity = getEntityByName(
							NodeType.HOST_GROUP, hgIterator.next());
					if (null != hgEntity) {
						List<Integer> hostList = hgEntity.getChildNodeList();
						if (null != hostList && !hostList.isEmpty()
								&& hostList.contains(hostParentId)) {
							return serviceMetaEntity;
						}
					}
				}
			}
			/*
			 * service is neither in the list of allowed service groups nor in
			 * hosts contained in allowed host groups. Hence return the default
			 * allowed node for the user.
			 */
			break;

		default:
			break;
		}

		// now return default entity
		return getDefaultEntityByUserRole(defaultHostGroup,
				defaultServiceGroup, extRoleHostGroupList,
				extRoleServiceGroupList);
	}

	/**
	 * This method checks any Node against Extended Role Permissions passed to
	 * it.
	 * 
	 * @param nodeID
	 * @param nodeType
	 * @param nodeName
	 * @param extRoleHostGroupList
	 * @param extRoleServiceGroupList
	 * @return true if Node has Extended Role Permissions
	 */
	public boolean checkNodeForExtendedRolePermissions(int nodeID,
			NodeType nodeType, String nodeName,
			List<String> extRoleHostGroupList,
			List<String> extRoleServiceGroupList) {
		NetworkMetaEntity entityByExtendedRolePermissions = getEntityByExtendedRolePermissions(
				nodeID, nodeType, nodeName, extRoleHostGroupList,
				extRoleServiceGroupList, null, null);
		if (entityByExtendedRolePermissions.getObjectId() == nodeID
				&& entityByExtendedRolePermissions.getType().equals(nodeType)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the default entity as per the passed list of groups
	 * 
	 * @param defaultHostGroup
	 * @param defaultServiceGroup
	 * @param extRoleHostGroupList
	 * @param extRoleServiceGroupList
	 * 
	 * @return the default allowed node for the user
	 */
	public NetworkMetaEntity getDefaultEntityByUserRole(
			String defaultHostGroup, String defaultServiceGroup,
			List<String> extRoleHostGroupList,
			List<String> extRoleServiceGroupList) {

		if (extRoleHostGroupList.isEmpty() && extRoleServiceGroupList.isEmpty()) {
			// No Restrictions - return Entire Network
			new NetworkMetaEntity(0, "Entire Network",
					NetworkObjectStatusEnum.ENTIRE_NETWORK_STATUS,
					NodeType.NETWORK, null, null, null);
		}

		NetworkMetaEntity metaEntity = null;
		if (!extRoleHostGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			// Host Group - return default HG if set
			if (null != defaultHostGroup) {
				metaEntity = getEntityByName(NodeType.HOST_GROUP,
						defaultHostGroup);
				if (null != metaEntity) {
					return metaEntity;
				}
			}
			// else return first valid HG in the list
			if (!extRoleHostGroupList.isEmpty()) {
				for (Iterator<String> hgIterator = extRoleHostGroupList
						.iterator(); hgIterator.hasNext();) {
					String hgName = hgIterator.next();
					metaEntity = getEntityByName(NodeType.HOST_GROUP, hgName);
					if (null != metaEntity) {
						return metaEntity;
					}
				}
			}
			// else return 1st HG from RTMM
			metaEntity = hostGroupMap.get(hostGroupMap.keySet().iterator()
					.next());
			if (null != metaEntity) {
				return metaEntity;
			}
		}

		// if HG list is empty, return first allowed SG
		if (!extRoleServiceGroupList
				.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
			// Service Group
			if (null != defaultServiceGroup) {
				metaEntity = getEntityByName(NodeType.SERVICE_GROUP,
						defaultServiceGroup);
				if (null != metaEntity) {
					return metaEntity;
				}
			}
			if (!extRoleServiceGroupList.isEmpty()) {
				for (Iterator<String> sgIterator = extRoleServiceGroupList
						.iterator(); sgIterator.hasNext();) {
					String sgName = sgIterator.next();
					metaEntity = getEntityByName(NodeType.SERVICE_GROUP, sgName);
					if (null != metaEntity) {
						return metaEntity;
					}
				}
			}

			// else return 1st SG from RTMM
			metaEntity = serviceGroupMap.get(serviceGroupMap.keySet()
					.iterator().next());
			if (null != metaEntity) {
				return metaEntity;
			}
		}

		// else return Entire Network meta entity, as both lists are empty
		return new NetworkMetaEntity(0, "Entire Network",
				NetworkObjectStatusEnum.ENTIRE_NETWORK_STATUS,
				NodeType.NETWORK, null, null, null);
	}

    public boolean isInitialized() {
        return initialized;
    }
}
