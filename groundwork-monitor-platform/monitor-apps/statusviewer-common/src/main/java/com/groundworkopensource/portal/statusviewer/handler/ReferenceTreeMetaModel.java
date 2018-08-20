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

import com.groundworkopensource.portal.common.*;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
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
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.CustomGroupClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.RTMMClient;
import org.groundwork.rs.client.ServiceGroupClient;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;

import javax.faces.context.FacesContext;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;


/**
 * This is Global/Application scope bean, that contains the reference tree model
 * to build trees for each sub page.
 *
 * It contains all the Hosts/HostGroups as well as Services/ServiceGroups.
 */

public class ReferenceTreeMetaModel extends OnDemandServerPush implements Closeable {

	/**
	 * DELETE
	 */
	private static final String DELETE = "DELETE";

	/**
	 * UPDATE_ACKNOWLEDGE
	 */
	private static final String UPDATE_ACKNOWLEDGE = "UPDATE_ACKNOWLEDGE";

	private static final String UPDATE = "UPDATE";

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
     * Use RTMM client configuration property name.
     */
    private static final String USE_RTMM_CLIENT_PROPERTY_NAME = "portal.statusviewer.useRTMMClient";

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

	/**
	 * REST clients deployment url configuration.
	 */
	private String deploymentUrl;

	// Default Values
	/**
	 * DEFAULT_TOPIC_NAME - to be published for Tree View
	 */
	private static final String DEFAULT_TOPIC_NAME = "ui_events";

	// Contains the network entities.
    private ConcurrentReferenceTreeModel referenceTree;

	// Auto refresh properties
	private static final String AUTO_REFRESH_GAP_PROPERTY_NAME = "portal.statusviewer.autoRefreshGap";
	private static final long DISABLE_AUTO_REFRESH_GAP_VALUE = 0;
	private boolean autoRefreshEnabled = false;

	// RefreshService is used for periodic refresh (if configured) of referenceTree
	private final ScheduledExecutorService refreshService = Executors.newSingleThreadScheduledExecutor();

	// ExecutorService is used for various multi-threaded operations.  It is created globally and
    // shutdown on "close" to avoid rapid/frequent create+delete cycles as there are reports of
    // memory/thread leak possibilities and this minimizes likelihood of us experiencing these.
	private final ExecutorService executorService = Executors.newFixedThreadPool(4);

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

	private volatile boolean initialized = false;

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
     * Configuration flag for optimized RTMMClient usage.
     */
    private boolean useRTMMClient = true;

	/**
	 * Default Constructor. This builds all maps for Host Group, Host, Services
	 * and Service Group
	 */
	public ReferenceTreeMetaModel() {
		// lookup REST client deployment configuration
		this.deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
		// lookup optimized RTMMClient usage configuration
		String rttmClient = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, USE_RTMM_CLIENT_PROPERTY_NAME);
		if (rttmClient == null)
			rttmClient = "true";
		this.useRTMMClient = Boolean.parseBoolean(rttmClient);

		String autoRefreshPeriodString = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, AUTO_REFRESH_GAP_PROPERTY_NAME);
		String defaultDisableAutoRefreshGapValue = Long.toString(DISABLE_AUTO_REFRESH_GAP_VALUE);
		if (autoRefreshPeriodString == null)
			autoRefreshPeriodString = defaultDisableAutoRefreshGapValue;

		long autoRefreshPeriod = DISABLE_AUTO_REFRESH_GAP_VALUE;
		try {
			autoRefreshPeriod = Long.parseLong(autoRefreshPeriodString);
			if (autoRefreshPeriod < 0) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			LOGGER.error("Unable to process value for " + AUTO_REFRESH_GAP_PROPERTY_NAME + ": " + autoRefreshPeriodString + " seconds.  Auto refresh has been disabled.");
		}

		if (autoRefreshPeriod != DISABLE_AUTO_REFRESH_GAP_VALUE) {
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Scheduling auto refresh service with gap=" + autoRefreshPeriod + " seconds");
			this.autoRefreshEnabled = true;
			// Schedule the thread to have a delay between runs of the refresh gaps.  Wait for double
            // the configured gap to start running to prevent taxing the system with SV refreshes
            // during initialization
			refreshService.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					referenceTree = createModels();
				}
			}, 0, autoRefreshPeriod, TimeUnit.SECONDS);
		} else {
			// create all "models" i.e. populate all map
			rebuildModel();
		}
	}

	public void close() {
		if (LOGGER.isInfoEnabled()) LOGGER.info("Closing RTMM");
		try {
			if (executorService != null) {
				executorService.shutdown();
				executorService.awaitTermination(1, TimeUnit.SECONDS);
				if (!executorService.isShutdown()) {
					LOGGER.error("Forceful shutdown of executorService required");
					executorService.shutdownNow();
				}
			}
			if (refreshService != null) {
				// wait 1 second for closing all threads
				refreshService.shutdown();
				refreshService.awaitTermination(1, TimeUnit.SECONDS);
				if (!refreshService.isShutdown()) {
					LOGGER.error("Forceful shutdown of refreshService required");
					refreshService.shutdownNow();
				}
			}
		} catch (InterruptedException e) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();
		}
		if (LOGGER.isInfoEnabled()) LOGGER.info("RTMM closed");
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

	public void rebuildModel() {
	    // If auto-refresh is enabled, then we should rely on that to execute the potentially expensive createModels
	    if (!autoRefreshEnabled) {
	    	referenceTree = createModels();
		}
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
	private synchronized ConcurrentReferenceTreeModel createModels() {

		if (LOGGER.isInfoEnabled()) LOGGER.info("creating models for Tree view... in createModels()");

		// Use an executor with FutureTasks to allow these potentially large rtmm operations to
		// execute in parallel

		FutureTask<List<DtoHost>> listHosts = new FutureTask<>(new Callable<List<DtoHost>>() {
			@Override public List<DtoHost> call() {
				if (useRTMMClient) {
					RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return rtmmClient.listHosts();
				} else {
					HostClient hostClient = new HostClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return hostClient.list(DtoDepthType.Deep);
				}
			}
		});
		executorService.execute(listHosts);

		FutureTask<List<DtoHostGroup>> listHostGroups = new FutureTask<>(new Callable<List<DtoHostGroup>>() {
			@Override public List<DtoHostGroup> call() {
			    if (useRTMMClient) {
					RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return rtmmClient.listHostGroups();
				} else {
					HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return hostGroupClient.list();
				}
			}
		});
		executorService.execute(listHostGroups);

		FutureTask<List<DtoServiceGroup>> listServiceGroups = new FutureTask<>(new Callable<List<DtoServiceGroup>>() {
			@Override public List<DtoServiceGroup> call() {
				if (useRTMMClient) {
					RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return rtmmClient.listServiceGroups();
				} else {
                    ServiceGroupClient serviceGroupClient = new ServiceGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
                    return serviceGroupClient.list();
				}
			}
		});
		executorService.execute(listServiceGroups);

		FutureTask<List<DtoCustomGroup>> listCustomGroups = new FutureTask<>(new Callable<List<DtoCustomGroup>>() {
			@Override public List<DtoCustomGroup> call() {
			    if (useRTMMClient) {
					RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return rtmmClient.listCustomGroups();
				} else {
					CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					return customGroupClient.list();
				}
			}
		});
		executorService.execute(listCustomGroups);

		List<DtoHost> dtoHosts;
		List<DtoHostGroup> dtoHostGroups;
		List<DtoServiceGroup> dtoServiceGroups;
		List<DtoCustomGroup> dtoCustomGroups;

		try {
			dtoHosts = listHosts.get();
			dtoHostGroups = listHostGroups.get();
			dtoServiceGroups = listServiceGroups.get();
			dtoCustomGroups = listCustomGroups.get();
		} catch (InterruptedException e) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();
            // If there is an error updating, then return the master reference tree
			return referenceTree;
		} catch (ExecutionException e) {
			LOGGER.error("Exeception retrieving RTMM results concurrently: " + e);
			// If there is an error updating, then return the master reference tree
			return referenceTree;
		}

		// Create a reference tree from scratch to use for building.  Do not edit the primary copy
        // to avoid any consistency issues if reads occur during this process
		ConcurrentReferenceTreeModel partialReferenceTree = new ConcurrentReferenceTreeModel();

		// Note: DO NOT CHANGE the sequence of calls for model creation!! They
		// are interdependent.
		try {
			restCreateHostAndServiceModel(dtoHosts, partialReferenceTree);
			restCreateServiceGroupModel(dtoServiceGroups, partialReferenceTree);
			restCreateHostGroupModel(dtoHostGroups, partialReferenceTree);
			resetCustomGroupModel(partialReferenceTree);
			createCustomGroupModel(partialReferenceTree);
			restCreateCustomGroupModel(dtoCustomGroups, partialReferenceTree);
		}
		catch (Exception e) {
			LOGGER.error("Failed to Create RTMM Models: " + e.getMessage(), e);
			return referenceTree;
		}

        if (LOGGER.isInfoEnabled()) LOGGER.info("Completed Creating models for Tree view");
        // The partialReferenceTree is now fully built and can be provided to the caller
        return partialReferenceTree;
	}

	/**
	 * Call back method for JMS.
	 *
	 * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
	 */
	@Override
	public void refresh(String xml) {
		if (autoRefreshEnabled) {
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Bypassing JMS-based refresh logic due to use of auto-refresh");
			return;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("*****************************");
			LOGGER.debug(xml);
			LOGGER.debug("*****************************");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Processing RTMM refresh ...");
		}
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
			Map<NodeType, List<JMSUpdate>> updatesMapFromXML = JMSUtils.getJMSUpdatesMapFromXML(xml);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Parsed RTMM Updates successfully, processing  " + updatesMapFromXML.size() + " updates");
			}
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
						Collection<NetworkMetaEntity> serviceGroups = referenceTree.getServiceGroupMap()
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
							referenceTree.getServiceMap().remove(serviceId);
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
				else if (hostUpdate.getAction().equals(UPDATE)) {
					// process host updates
					int updatedHostId = hostUpdate.getId();
					updateHost(updatedHostId);
				}
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Done Processing RTMM Host updates, count: " + hostJMSUpdates.size());
			}

			// process Host Group updates
			List<JMSUpdate> hostGroupJMSUpdates = updatesMapFromXML.get(NodeType.HOST_GROUP);
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
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Done Processing RTMM Host Group updates, count: " + hostGroupJMSUpdates.size());
			}

			// process Service Group updates
			List<JMSUpdate> serviceGroupJMSUpdates = updatesMapFromXML.get(NodeType.SERVICE_GROUP);
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
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Done Processing RTMM Service Group updates, count: " + serviceGroupJMSUpdates.size());
			}

			// process Custom Group updates
			List<JMSUpdate> customGroupJMSUpdates = updatesMapFromXML.get(NodeType.CUSTOM_GROUP);
			for (JMSUpdate update : customGroupJMSUpdates) {
				// long cgStartTime = System.currentTimeMillis();
				if (update.getAction().equals(DELETE)) {
					removeRestCustomGroup(update.getId());
				} else {
					updateRestCustomGroup(update.getId());
				}
				// LOGGER.debug("********Time taken to " + update.getAction()
				// + "  Custom Group is "
				// + (System.currentTimeMillis() - cgStartTime)
				// + " ms********");
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Done Processing RTMM Custom Group updates, count: " + customGroupJMSUpdates.size());
			}

			// process Service updates - just Acknowledge
			List<JMSUpdate> serviceJMSUpdates = updatesMapFromXML.get(NodeType.SERVICE);
			for (JMSUpdate serviceUpdate : serviceJMSUpdates) {
				if (serviceUpdate.getAction().equals(UPDATE_ACKNOWLEDGE)) {
					// process service acknowledgment updates
					int updatedServiceId = serviceUpdate.getId();
					// get this service from map
					NetworkMetaEntity serviceById = getServiceById(updatedServiceId);
					serviceById.setAcknowledged(!serviceById.isAcknowledged());
				}
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Done Processing RTMM Service updates, count: " + serviceJMSUpdates.size());
			}

		} catch (Exception exc) {
			LOGGER.error("Exception in JMS Push refresh() method of Tree View portlet. Actual Exception : " + exc.getMessage(), exc);
			initialized = false; // force refresh
			//startRebuildTreeModelJob(); // GWMON-12050
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
			NetworkMetaEntity serviceEntity = referenceTree.getServiceMap().get(serviceId);
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

		referenceTree.getServiceGroupMap().put(serviceGroupMetaEntity.getObjectId(),
				serviceGroupMetaEntity);
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
			LOGGER.error("Failed publish tree view updates: " + exc.getMessage(), exc);
			throw new GWPortalGenericException(exc.getMessage());
		} finally {
			if (session != null) {
				try {
					session.commit();
					session.close();
					session = null;
				} catch (Exception exc) {
					LOGGER.error("Closing session error: " + exc.getMessage(), exc);
				}
			}
		}

	}

	/**
	 * Create ServiceGroup tree model. Requires Service tree model already built.
	 */
	private void restCreateServiceGroupModel(List<DtoServiceGroup> dtoServiceGroups, ConcurrentReferenceTreeModel tree) {
		// clear service group model
		tree.getServiceGroupMap().clear();

		restUpdateServiceGroupModel(dtoServiceGroups, tree);

		// dump model if logging in debug mode
		debugDumpNetworkMetaEntity("RESTServiceGroupMap", tree.getServiceGroupMap());
	}

	/**
	 * Update ServiceGroup tree model for specific service groups. Requires
	 * Service tree model already built.
	 *
	 * @param dtoServiceGroups service groups to create or update in model
	 */
	private void restUpdateServiceGroupModel(List<DtoServiceGroup> dtoServiceGroups, ConcurrentReferenceTreeModel tree) {
		for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
			if ((dtoServiceGroup.getServices() != null) && !dtoServiceGroup.getServices().isEmpty()) {
				// get list of service status ids
				List<Integer> serviceStatusIds = new ArrayList<Integer>();
				for (DtoService dtoService : dtoServiceGroup.getServices()) {
					serviceStatusIds.add(dtoService.getId());
				}

				// get ordered service group services and status
				NetworkObjectStatusEnum [] status = new NetworkObjectStatusEnum[1];
				String [] tooltip = new String[1];
				String [] serviceSummary = new String[1];
				List<Integer> serviceList = getSortedServiceGroupServicesAndStatus(serviceStatusIds, status, tooltip,
						serviceSummary, tree);

				// create service group Network Meta Entity and put in service group map;
				// (app type prefix not specified to suppress their display)
				NetworkMetaEntity serviceGroupMetaEntity = new NetworkMetaEntity(dtoServiceGroup.getId(), null,
						dtoServiceGroup.getName(), dtoServiceGroup.getAppType(), status[0], NodeType.SERVICE_GROUP,
						tooltip[0], null, serviceList);
				serviceGroupMetaEntity.setSummary(serviceSummary[0]);
				tree.getServiceGroupMap().put(dtoServiceGroup.getId(), serviceGroupMetaEntity);
			}
		}
	}

	private List<Integer> getSortedServiceGroupServicesAndStatus(List<Integer> serviceStatusIds,
																 NetworkObjectStatusEnum [] status, String [] tooltip,
																 String [] serviceSummary,
                                                                 ConcurrentReferenceTreeModel tree) {

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
				NetworkMetaEntity serviceEntity = tree.getServiceMap().get(serviceId);

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

		// return sorted service ids and status
		status[0] = determineBubbleUpStatusForServiceGroup(serviceEntityList);
		tooltip[0] = new StringBuilder().append(SERVICES_TEXT).append(serviceList.size()).append(Constant.BR)
				.append(troubledString).append(SERVICES_TEXT).append(troubledServices).toString();
		StringBuilder serviceSummaryBuilder = new StringBuilder();
		if (downCount > 0) {
			serviceSummaryBuilder.append(downCount).append(" services CRITICAL, ");
		}
		if (warningCount > 0) {
			serviceSummaryBuilder.append(warningCount).append(" services WARNING, ");
		}
		if (unknownCount > 0) {
			serviceSummaryBuilder.append(unknownCount).append(" services UNKNOWN, ");
		}
		if (pendingCount > 0) {
			serviceSummaryBuilder.append(pendingCount).append(" services PENDING, ");
		}
		if (upCount > 0) {
			serviceSummaryBuilder.append(upCount).append(" services OK");
		}
		serviceSummary[0] = serviceSummaryBuilder.toString();
		return serviceList;
	}

	/**
	 * Reset CustomGroup tree model
	 */
	private void resetCustomGroupModel(ConcurrentReferenceTreeModel tree) {
		// reset custom group model
		tree.getCustomGroupMap().clear();
		tree.getCustomGroupRootMap().clear();
	}

	/**
	 * Create legacy CustomGroup tree model. Note that ids for legacy custom
	 * groups are negated.
	 *
	 * @throws WSDataUnavailableException
	 */
	private void createCustomGroupModel(ConcurrentReferenceTreeModel tree) throws WSDataUnavailableException {
		// reload custom group model
		Map<Long,CustomGroup> customGroups = populateCustomGroups();
		if (customGroups != null) {
			for (CustomGroup customGroup : customGroups.values()) {
				updateCustomGroupModel(customGroup, customGroups, tree);
			}
		}

		// dump model if logging in debug mode
		debugDumpNetworkMetaEntity("LegacyCustomGroupRootMap", tree.getCustomGroupRootMap());
		debugDumpNetworkMetaEntity("LegacyCustomGroupMap", tree.getCustomGroupMap());
	}

	/**
	 * Construct or update legacy custom group model instance. Note that ids for
	 * legacy custom groups are negated.
	 *
	 * @param customGroup custom group
	 * @param customGroups custom groups map
	 */
	private void updateCustomGroupModel(CustomGroup customGroup, Map<Long,CustomGroup> customGroups, ConcurrentReferenceTreeModel tree) {
		// add custom group model
		NetworkMetaEntity customGroupMetaEntity = createCustomGroupModel(customGroup, customGroups, tree);
		// map all custom groups
		tree.getCustomGroupMap().put(customGroupMetaEntity.getObjectId(), customGroupMetaEntity);
		// map published roots
		if (customGroup.getGroupState().equalsIgnoreCase("P") &&
				((customGroup.getParents() == null) || customGroup.getParents().isEmpty())) {
			tree.getCustomGroupRootMap().put(customGroupMetaEntity.getObjectId(), customGroupMetaEntity);
		} else {
			tree.getCustomGroupRootMap().remove(customGroupMetaEntity.getObjectId());
		}
	}

	/**
	 * Construct legacy custom group model instance. Note that ids for legacy
	 * custom groups are negated.
	 *
	 * @param customGroup custom group
	 * @param customGroups custom groups map
	 * @return new custom group
	 */
	private NetworkMetaEntity createCustomGroupModel(CustomGroup customGroup, Map<Long,CustomGroup> customGroups, ConcurrentReferenceTreeModel tree) {
		String entityType = customGroup.getEntityType().getEntityType();
		NodeType nodeType = null;
		if (entityType.equalsIgnoreCase(Constant.DB_HOST_GROUP)) {
			nodeType = NodeType.HOST_GROUP;
		}
		if (entityType.equalsIgnoreCase(Constant.DB_SERVICE_GROUP)) {
			nodeType = NodeType.SERVICE_GROUP;
		}
		if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
			nodeType = NodeType.CUSTOM_GROUP;
		}
		ArrayList<Integer> childrenList = findCustomGroupChildren(customGroup, customGroups, tree);
		NetworkObjectStatusEnum status = NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus("PENDING",
				NodeType.HOST_GROUP);
		int customGroupId = -(int)customGroup.getGroupId(); // negate legacy custom group ids
		NetworkMetaEntity customGroupMetaEntity = new NetworkMetaEntity(customGroupId, null, customGroup.getGroupName(),
				null, status, nodeType, null, null, childrenList);
		customGroupMetaEntity.setCustom(true);
		return customGroupMetaEntity;
	}

	/**
	 * Helper to find the children for the legacy custom group
	 */
	private ArrayList<Integer> findCustomGroupChildren(CustomGroup cusGroup, Map<Long,CustomGroup> customGroups, ConcurrentReferenceTreeModel tree) {
		EntityType entType = cusGroup.getEntityType();
		ArrayList<Integer> childrenList = new ArrayList<Integer>();
		if (entType.getEntityType().equalsIgnoreCase(Constant.DB_HOST_GROUP)) {
			ArrayList<NetworkMetaEntity> sortList = new ArrayList<NetworkMetaEntity>();
			for (CustomGroupElement element : cusGroup.getElements()) {
				for (Map.Entry<Integer, NetworkMetaEntity> entry : tree.getHostGroupMap()
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
				for (Map.Entry<Integer, NetworkMetaEntity> entry : tree.getServiceGroupMap()
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
				CustomGroup group = customGroups.get(element.getElementId());
				int customGroupId = -(int)group.getGroupId(); // negate legacy custom group ids
				childrenList.add(customGroupId);
			} // end if
		} // end if
		return childrenList;
	}

	/**
	 * Create CustomGroup tree model. Requires HostGroup and ServiceGroup
	 * tree models already be built.
	 */
	private void restCreateCustomGroupModel(List<DtoCustomGroup> dtoCustomGroups, ConcurrentReferenceTreeModel tree) {
		restUpdateCustomGroupModel(dtoCustomGroups, tree);

		// dump model if logging in debug mode
		debugDumpNetworkMetaEntity("RESTCustomGroupRootMap", tree.getCustomGroupRootMap());
		debugDumpNetworkMetaEntity("RESTCustomGroupMap", tree.getCustomGroupMap());
	}

	/**
	 * Update CustomGroup tree model for specific custom groups. Requires
	 * HostGroup and ServiceGroup tree models already be built.
	 *
	 * @param dtoCustomGroups custom groups to create or update in model
	 */
	private void restUpdateCustomGroupModel(List<DtoCustomGroup> dtoCustomGroups, ConcurrentReferenceTreeModel tree) {
		// create CustomGroup tree model entries
		for (DtoCustomGroup dtoCustomGroup : dtoCustomGroups) {
			NodeType nodeType = null;
			if ((dtoCustomGroup.getHostGroups() != null) && !dtoCustomGroup.getHostGroups().isEmpty()) {
				nodeType = NodeType.HOST_GROUP;
			} else if ((dtoCustomGroup.getServiceGroups() != null) && !dtoCustomGroup.getServiceGroups().isEmpty()) {
				nodeType = NodeType.SERVICE_GROUP;
			} else {
				nodeType = NodeType.CUSTOM_GROUP;
			}
			NetworkObjectStatusEnum status = NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus("PENDING",
					NodeType.HOST_GROUP);
			NetworkMetaEntity customGroupMetaEntity = new NetworkMetaEntity(dtoCustomGroup.getId(), null,
					dtoCustomGroup.getName(), null, status, nodeType, null, null, null);
			customGroupMetaEntity.setCustom(true);
			tree.getCustomGroupMap().put(dtoCustomGroup.getId(), customGroupMetaEntity);
			if ((dtoCustomGroup.isRoot() == null) || dtoCustomGroup.isRoot()) {
				tree.getCustomGroupRootMap().put(dtoCustomGroup.getId(), customGroupMetaEntity);
			} else {
				tree.getCustomGroupRootMap().remove(customGroupMetaEntity.getObjectId());
			}
		}
		// set CustomGroup tree model entries children
		for (DtoCustomGroup dtoCustomGroup : dtoCustomGroups) {
			List<Integer> nodeChildIds = new ArrayList<Integer>();
			if ((dtoCustomGroup.getHostGroups() != null) && !dtoCustomGroup.getHostGroups().isEmpty()) {
				List<NetworkMetaEntity> hostGroupMetaEntities = new ArrayList<NetworkMetaEntity>();
				for (DtoHostGroup dtoHostGroup : dtoCustomGroup.getHostGroups()) {
					NetworkMetaEntity hostGroupMetaEntity = tree.getHostGroupMap().get(dtoHostGroup.getId());
					if (hostGroupMetaEntity != null) {
						hostGroupMetaEntities.add(hostGroupMetaEntity);
					}
				}
				Collections.sort(hostGroupMetaEntities);
				for (NetworkMetaEntity hostGroupMetaEntity : hostGroupMetaEntities) {
					nodeChildIds.add(hostGroupMetaEntity.getObjectId());
				}
			} else if ((dtoCustomGroup.getServiceGroups() != null) && !dtoCustomGroup.getServiceGroups().isEmpty()) {
				List<NetworkMetaEntity> serviceGroupMetaEntities = new ArrayList<NetworkMetaEntity>();
				for (DtoServiceGroup dtoServiceGroup : dtoCustomGroup.getServiceGroups()) {
					NetworkMetaEntity serviceGroupMetaEntity = tree.getServiceGroupMap().get(dtoServiceGroup.getId());
					if (serviceGroupMetaEntity != null) {
						serviceGroupMetaEntities.add(serviceGroupMetaEntity);
					}
				}
				Collections.sort(serviceGroupMetaEntities);
				for (NetworkMetaEntity serviceGroupMetaEntity : serviceGroupMetaEntities) {
					nodeChildIds.add(serviceGroupMetaEntity.getObjectId());
				}
			} else if ((dtoCustomGroup.getChildren() != null) && !dtoCustomGroup.getChildren().isEmpty()) {
				List<NetworkMetaEntity> customGroupMetaEntities = new ArrayList<NetworkMetaEntity>();
				for (DtoCustomGroup dtoCustomGroupChild : dtoCustomGroup.getChildren()) {
					NetworkMetaEntity customGroupMetaEntity = tree.getCustomGroupMap().get(dtoCustomGroupChild.getId());
					if (customGroupMetaEntity != null) {
						customGroupMetaEntities.add(customGroupMetaEntity);
					}
				}
				Collections.sort(customGroupMetaEntities);
				for (NetworkMetaEntity customGroupMetaEntity : customGroupMetaEntities) {
					nodeChildIds.add(customGroupMetaEntity.getObjectId());
				}
			}
			NetworkMetaEntity customGroupMetaEntity = tree.getCustomGroupMap().get(dtoCustomGroup.getId());
			customGroupMetaEntity.setChildNodeList(nodeChildIds);
		}
	}

	/**
	 * Create HostGroup tree model. Requires Host tree model already built. Updates
	 * Host tree model.
	 */
	private void restCreateHostGroupModel(List<DtoHostGroup> dtoHostGroups, ConcurrentReferenceTreeModel tree) {
		// clear host group model
		tree.getHostGroupMap().clear();

		restUpdateHostGroupModel(dtoHostGroups, tree);

		// dump model if logging in debug mode
		debugDumpNetworkMetaEntity("RESTHostGroupMap", tree.getHostGroupMap());
		debugDumpNetworkMetaEntity("RESTHostMap", tree.getHostMap());
	}

	/**
	 * Update HostGroup tree model for specific host groups. Requires Host
	 * tree model already built. Updates Host tree model.
	 *
	 * @param dtoHostGroups host groups to create or update in model
	 */
	private void restUpdateHostGroupModel(List<DtoHostGroup> dtoHostGroups, ConcurrentReferenceTreeModel tree) {
		for (DtoHostGroup dtoHostGroup : dtoHostGroups) {
			if ((dtoHostGroup.getHosts() != null) && !dtoHostGroup.getHosts().isEmpty()) {
				// get list of host ids
				List<Integer> hostIds = new ArrayList<Integer>();
				for (DtoHost dtoHost : dtoHostGroup.getHosts()) {
					hostIds.add(dtoHost.getId());
				}

				// get ordered host group host list and status
				NetworkObjectStatusEnum [] status = new NetworkObjectStatusEnum[1];
				String [] tooltip = new String[1];
				String [] hostSummary = new String[1];
				String [] serviceSummary = new String[1];
				boolean [] acknowledged = new boolean[1];
				int [] scheduledDown = new int[1];
				List<Integer> hostList = getSortedHostGroupHostsAndStatus(hostIds, status, dtoHostGroup.getAlias(),
						tooltip, hostSummary, serviceSummary, acknowledged, scheduledDown, tree);

				// create host group Network Meta Entity and put in host group map;
				// (app type prefix not specified to suppress their display)
				NetworkMetaEntity hostGroupMetaEntity = new NetworkMetaEntity(dtoHostGroup.getId(), null,
						dtoHostGroup.getName(), dtoHostGroup.getAppType(), status[0], NodeType.HOST_GROUP, tooltip[0],
						null, hostList);
				hostGroupMetaEntity.setAlias(dtoHostGroup.getAlias());
				hostGroupMetaEntity.setSummary(hostSummary[0] + serviceSummary[0]);
				hostGroupMetaEntity.setAcknowledged(acknowledged[0]);
				hostGroupMetaEntity.setInScheduledDown(scheduledDown[0]);
				tree.getHostGroupMap().put(dtoHostGroup.getId(), hostGroupMetaEntity);
			}
		}
	}

	private List<Integer> getSortedHostGroupHostsAndStatus(List<Integer> hostIds, NetworkObjectStatusEnum [] status,
														   String hostGroupAlias, String [] tooltip,
														   String [] hostSummary, String [] serviceSummary,
														   boolean [] acknowledged, int [] scheduledDown,
                                                           ConcurrentReferenceTreeModel tree) {

		List<Integer> hostList = new ArrayList<Integer>();
		int troubledHosts = 0;
		int troubledServices = 0;
		long downCount = 0;
		long warningCount = 0;
		long unknownCount = 0;
		long pendingCount = 0;
		long upCount = 0;
		long serviceDownCount = 0;
		long serviceWarningCount = 0;
		long serviceUnknownCount = 0;
		long servicePendingCount = 0;
		long serviceUpCount = 0;
		int hostAcknowledgedCount = 0;
		int hostScheduledDownCount = 0;
		ArrayList<NetworkMetaEntity> hostEntityList = new ArrayList<NetworkMetaEntity>();
		for (int hostId : hostIds) {
			// calculate troubled Hosts and Services
			NetworkMetaEntity hostEntity = tree.getHostMap().get(hostId);
			if (hostEntity != null) {
				if (hostEntity.getStatus() != NetworkObjectStatusEnum.HOST_UP) {
					troubledHosts++;
				}
				Iterator<Integer> serviceEntities = hostEntity.getChildNodeList().iterator();
				if (serviceEntities != null) {
					while (serviceEntities.hasNext()) {
						NetworkMetaEntity service = tree.getServiceMap()
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
								serviceDownCount++;
							}
							if (statusName.equalsIgnoreCase("OK")) {
								serviceUpCount++;
							}
							if (statusName.equalsIgnoreCase("UNREACHABLE")
									|| statusName
									.equalsIgnoreCase("UNKNOWN")) {
								serviceUnknownCount++;
							}
							if (statusName
									.equalsIgnoreCase("PENDING_SERVICE")
									|| statusName
									.equalsIgnoreCase("PENDING")) {
								servicePendingCount++;
							}
							if (statusName
									.equalsIgnoreCase("WARNING_SERVICE")
									|| statusName
									.equalsIgnoreCase("WARNING")) {
								serviceWarningCount++;
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

				// If at least one host is unacknowledged or scheduled down
				// then hostgroup becomes unacknowledged & scheduled down
				if (hostEntity.isAcknowledged()) {
					hostAcknowledgedCount++;
				}
				if (hostEntity.getInScheduledDown() > 0) {
					hostScheduledDownCount++;
				}
			}

			hostEntityList.add(hostEntity);
			// Also update the hostMap
			tree.getHostMap().put(hostEntity.getObjectId(), hostEntity);
		}

		Collections.sort(hostEntityList);
		for (NetworkMetaEntity hostEntity : hostEntityList) {
			// Add to host list
			hostList.add(hostEntity.getObjectId());
		}

		// return sorted host list and status
		status[0] = determineBubbleUpStatusForHostGroup(hostEntityList);
		tooltip[0] = new StringBuilder().append(aliasString).append(Constant.SPACE)
				.append((hostGroupAlias != null) ? hostGroupAlias : "")
				.append(Constant.BR).append(HOSTS_TEXT).append(hostList.size()).append(Constant.BR)
				.append(troubledString).append(HOSTS_TEXT).append(troubledHosts).append(Constant.BR)
				.append(troubledString).append(SERVICES_TEXT ).append(troubledServices).toString();
		StringBuilder hostSummaryBuilder = new StringBuilder();
		if (downCount > 0) {
			hostSummaryBuilder.append(downCount).append(" hosts DOWN, ");
		}
		if (warningCount > 0) {
			hostSummaryBuilder.append(warningCount).append(" hosts WARNING, ");
		}
		if (unknownCount > 0) {
			hostSummaryBuilder.append(unknownCount).append(" hosts UNREACHABLE, ");
		}
		if (pendingCount > 0) {
			hostSummaryBuilder.append(pendingCount).append(" hosts PENDING, ");
		}
		if (upCount > 0) {
			hostSummaryBuilder.append(downCount).append(" hosts UP. ");
		}
		hostSummary[0] = hostSummaryBuilder.toString();
		StringBuilder serviceSummaryBuilder = new StringBuilder();
		if (serviceDownCount > 0) {
			serviceSummaryBuilder.append(serviceDownCount).append(" services CRITICAL, ");
		}
		if (serviceWarningCount > 0) {
			serviceSummaryBuilder.append(serviceWarningCount).append(" services WARNING, ");
		}
		if (serviceUnknownCount > 0) {
			serviceSummaryBuilder.append(serviceUnknownCount).append(" services UNKNOWN, ");
		}
		if (servicePendingCount > 0) {
			serviceSummaryBuilder.append(servicePendingCount).append(" services PENDING, ");
		}
		if (serviceUpCount > 0) {
			serviceSummaryBuilder.append(serviceUpCount).append(" services OK");
		}
		serviceSummary[0] = serviceSummaryBuilder.toString();
		acknowledged[0] = (hostAcknowledgedCount > 0);
		scheduledDown[0] = ((hostScheduledDownCount > 0) ? 1 : 0);
		return hostList;
	}

	/**
	 * NetworkMetaEntity host monitor status extractor for bubble up computation.
	 */
	private static final MonitorStatusBubbleUp.MonitorStatusExtractor<NetworkMetaEntity> HOST_BUBBLE_UP_EXTRACTOR =
			new MonitorStatusBubbleUp.MonitorStatusExtractor<NetworkMetaEntity>() {
				@Override
				public String extractMonitorStatus(NetworkMetaEntity obj) {
					if (obj == null) {
						return null;
					} else {
						// network meta entity may be either host, host group, or custom group:
						// in either case monitor status will be host status compatible
						String hostStatus = obj.getStatus().getMonitorStatusName();
						return hostStatus;
					}
				}
			};

	/**
	 * NetworkMetaEntity service monitor status extractor for bubble up computation.
	 */
	private static final MonitorStatusBubbleUp.MonitorStatusExtractor<NetworkMetaEntity> SERVICE_BUBBLE_UP_EXTRACTOR =
			new MonitorStatusBubbleUp.MonitorStatusExtractor<NetworkMetaEntity>() {
				@Override
				public String extractMonitorStatus(NetworkMetaEntity obj) {
					if (obj == null) {
						return null;
					} else {
						// network meta entity may be either service, service group, or custom
						// group: custom group monitor status needs to be converted from a host
						// to service status
						String serviceStatus;
						if ((obj.getType() == NodeType.SERVICE) || (obj.getType() == NodeType.SERVICE_GROUP)) {
							serviceStatus = obj.getStatus().getMonitorStatusName();
						} else {
							String hostStatus = obj.getStatus().getMonitorStatusName();
							String aliasedHostStatus = MonitorStatusBubbleUp.HOST_ALIAS_MONITOR_STATUS_MAP.get(hostStatus);
							hostStatus = ((aliasedHostStatus != null) ? aliasedHostStatus : hostStatus);
							serviceStatus = MonitorStatusBubbleUp.HOST_TO_SERVICE_MONITOR_STATUS_TRANSLATOR.get(hostStatus);
							serviceStatus = ((serviceStatus != null) ? serviceStatus : hostStatus);
						}
						return serviceStatus;
					}
				}
			};

	/**
	 * Determines the bubble up status for the hostGroup here.
	 *
	 * @param hostEntityList host entities
	 * @return bubble up status
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForHostGroup(
			ArrayList<NetworkMetaEntity> hostEntityList) {
		String bubbleUpStatus = MonitorStatusBubbleUp.computeHostGroupMonitorStatusBubbleUp(hostEntityList,
				HOST_BUBBLE_UP_EXTRACTOR);
		return ((bubbleUpStatus != null) ?
				NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(bubbleUpStatus, NodeType.HOST_GROUP) :
				NetworkObjectStatusEnum.NO_STATUS);
	}

	/**
	 * Determines the bubble up status for the hostGroup list here.
	 *
	 * @param hostGroupList host groups
	 * @param nodeType node type
	 * @return bubble up status
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForHostGroupList(
			ArrayList<NetworkMetaEntity> hostGroupList, NodeType nodeType) {
		String bubbleUpStatus = MonitorStatusBubbleUp.computeHostGroupMonitorStatusBubbleUp(hostGroupList,
				HOST_BUBBLE_UP_EXTRACTOR);
		return ((bubbleUpStatus != null) ?
				NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(bubbleUpStatus, nodeType) :
				NetworkObjectStatusEnum.NO_STATUS);
	}

	/**
	 * Recursively check custom group node type depth first. Typically, only one
	 * deep child needs to be checked: custom group hierarchy is assumed to be
	 * homogeneous. However, empty custom groups must be skipped to find a typed
	 * child and the hierarchy is walked in this case.
	 *
	 * @param customGroupMetaEntity custom group meta entity
	 * @return node type
	 */
	public NodeType checkConcreteEntityType(NetworkMetaEntity customGroupMetaEntity) {
		NodeType concreteEntityType = customGroupMetaEntity.getType();
		if (concreteEntityType == NodeType.CUSTOM_GROUP) {
			for (Integer customGroupId : customGroupMetaEntity.getChildNodeList()) {
				NetworkMetaEntity childCustomGroupMetaEntity = referenceTree.getCustomGroupMap().get(customGroupId);
				if (childCustomGroupMetaEntity.getType() == NodeType.CUSTOM_GROUP) {
					concreteEntityType = checkConcreteEntityType(childCustomGroupMetaEntity);
				} else {
					concreteEntityType = childCustomGroupMetaEntity.getType();
				}
				if (concreteEntityType != NodeType.CUSTOM_GROUP) {
					return concreteEntityType;
				}
			}
		}
		return concreteEntityType;
	}

	/**
	 * Determines the bubble up status for the customGroup here.
	 *
	 * @param customGroupObject custom group
	 * @return bubble up status
	 */
	public NetworkObjectStatusEnum determineBubbleUpStatusForCustomGroup(
			NetworkMetaEntity customGroupObject) {
		List<Integer> children = customGroupObject.getChildNodeList();
		String entityType = customGroupObject.getType().getTypeName();
		ArrayList<NetworkMetaEntity> tempHostGroupList = new ArrayList();
		ArrayList<NetworkMetaEntity> tempServiceGroupList = new ArrayList();
		NodeType nodeType = NodeType.CUSTOM_GROUP;
		NetworkObjectStatusEnum aggregatedBubbleUpStatus = NetworkObjectStatusEnum.NO_STATUS;
		for (Integer childId : children) {
			NetworkMetaEntity child = null;
			if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
				child = getCustomGroupById(childId);
				if (child != null) {
					child.setStatus(determineBubbleUpStatusForCustomGroup(child));
					NodeType concreteEntityType = checkConcreteEntityType(child);
					if (concreteEntityType == NodeType.HOST_GROUP) {
						tempHostGroupList.add(child);
					} else if (concreteEntityType == NodeType.SERVICE_GROUP) {
						tempServiceGroupList.add(child);
					}
				}
			} else if (entityType.equalsIgnoreCase(Constant.UI_HOST_GROUP)) {
				child = this.getHostGroupById(childId);
				if (getExtendedRoleHostGroupList().isEmpty() || getExtendedRoleHostGroupList().contains(
						child.getName())) {
					tempHostGroupList.add(child);
				}
				nodeType = NodeType.HOST_GROUP;
			} else if (entityType.equalsIgnoreCase(Constant.UI_SERVICE_GROUP)) {
				child = this.getServiceGroupById(childId);
				if (getExtendedRoleServiceGroupList().isEmpty() || getExtendedRoleServiceGroupList().contains(
						child.getName())) {
					tempServiceGroupList.add(child);
				}
				nodeType = NodeType.SERVICE_GROUP;
			}
		}
		if (tempHostGroupList.size() > 0) {
			NetworkObjectStatusEnum aggregatedHGBubbleUpStatus =
					determineBubbleUpStatusForHostGroupList(tempHostGroupList, nodeType);
			customGroupObject.setStatus(aggregatedHGBubbleUpStatus);
			return aggregatedHGBubbleUpStatus;
		}
		if (tempServiceGroupList.size() > 0) {
			NetworkObjectStatusEnum aggregatedSGBubbleUpStatus =
					determineBubbleUpStatusForServiceGroupList(tempServiceGroupList, nodeType);
			customGroupObject.setStatus(aggregatedSGBubbleUpStatus);
			return aggregatedSGBubbleUpStatus;
		}
		return aggregatedBubbleUpStatus;
	}

	/**
	 * Finds the bubble up status for the hostgroup
	 */
	private NetworkObjectStatusEnum reverseLookupByHostGroupName(
			String hostgroup) {
		for (Map.Entry<Integer, NetworkMetaEntity> entry : referenceTree.getHostGroupMap()
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
	 * Determines the bubble up status for the serviceGroup here.
	 *
	 * @param serviceEntityList service entities
	 * @return bubble up status
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForServiceGroup(
			List<NetworkMetaEntity> serviceEntityList) {
		String bubbleUpStatus = MonitorStatusBubbleUp.computeServiceGroupMonitorStatusBubbleUp(serviceEntityList,
				SERVICE_BUBBLE_UP_EXTRACTOR);
		return ((bubbleUpStatus != null) ?
				NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(bubbleUpStatus, NodeType.SERVICE_GROUP) :
				NetworkObjectStatusEnum.NO_STATUS);
	}

	/**
	 * Determines the bubble up status for the serviceGroup list here.
	 *
	 * @param serviceGroupList service groups
	 * @param nodeType node type
	 * @return bubble up status
	 */
	private NetworkObjectStatusEnum determineBubbleUpStatusForServiceGroupList(
			List<NetworkMetaEntity> serviceGroupList, NodeType nodeType) {
		String bubbleUpStatus = MonitorStatusBubbleUp.computeServiceGroupMonitorStatusBubbleUp(serviceGroupList,
				SERVICE_BUBBLE_UP_EXTRACTOR);
		return ((bubbleUpStatus != null) ?
				NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(bubbleUpStatus, nodeType) :
				NetworkObjectStatusEnum.NO_STATUS);
	}

	/**
	 * Create Host and Service tree model.
	 */
	private void restCreateHostAndServiceModel(List<DtoHost> dtoHosts, ConcurrentReferenceTreeModel tree) {
		// clear host and service model
		tree.getHostMap().clear();
		tree.getServiceMap().clear();

		restUpdateHostAndServiceModel(dtoHosts, tree);

		// dump models if logging in debug mode
		debugDumpNetworkMetaEntity("RESTHostMap", tree.getHostMap());
		debugDumpNetworkMetaEntity("RESTServiceMap", tree.getServiceMap());
	}

	/**
	 * return prefix based on applicationtype.displayname
	 * gets property value based on name: hostService.prefix.displayName
	 * @param displayName applicationtype.displayname
	 * @param hostService "host" or "service"
	 * @return if property value no then return null else return displayName
	 */
	private String getPrefix(String displayName, String hostService) {
		String prefix = null;
		String propval;

		if (hostService.equalsIgnoreCase("host")) {
			propval = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, "host.prefix." + displayName);
			if (propval == null || propval.equalsIgnoreCase("yes")) {
				prefix = displayName;
			}
		}
		else if (hostService.equalsIgnoreCase("service")) {
			propval = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, "service.prefix." + displayName);
			if (propval != null && propval.equalsIgnoreCase("yes")) {
				prefix = displayName;
			}
		}

		return prefix;
	}

	/**
	 * Update Host and Service tree model for specific hosts.
	 *
	 * @param dtoHosts hosts, (with services), to create or update in model
	 */
	private void restUpdateHostAndServiceModel(List<DtoHost> dtoHosts, ConcurrentReferenceTreeModel tree) {
		for (DtoHost dtoHost : dtoHosts) {
			// clear host services
			clearHostServices(dtoHost.getId(), tree);

			// create service model
			Map<Integer,String> hostServiceIdMonitorStatuses = new HashMap<Integer,String>();
			if (dtoHost.getServices() != null) {
				for (DtoService dtoService : dtoHost.getServices()) {
					hostServiceIdMonitorStatuses.put(dtoService.getId(), dtoService.getMonitorStatus());

					// get host service status
					NetworkObjectStatusEnum[] status = new NetworkObjectStatusEnum[1];
					String[] tooltip = new String[1];
					String[] extendedName = new String[1];
					getHostServiceStatus(dtoService.getMonitorStatus(), status, dtoHost.getHostName(), tooltip,
							dtoService.getDescription(), extendedName);

					// create service Network Meta Entity and put in service map
					String servicePrefix = getPrefix(dtoService.getAppTypeDisplayName(), "service");

					boolean isAcknowledged = dtoService.getPropertyBoolean("isProblemAcknowledged");
					NetworkMetaEntity serviceMetaEntity = new NetworkMetaEntity(dtoService.getId(),
							servicePrefix, dtoService.getDescription(), dtoService.getAppType(),
							status[0], NodeType.SERVICE, tooltip[0], dtoService.getLastCheckTime(), null, 0,
							isAcknowledged, dtoService.getLastStateChange());
					serviceMetaEntity.setExtendedName(extendedName[0]);
					serviceMetaEntity.setParentId(dtoHost.getId());
					serviceMetaEntity.setNextCheckDateTime(dtoService.getNextCheckTime());
					serviceMetaEntity.setLastPluginOutputString(dtoService.getLastPlugInOutput());
					tree.getServiceMap().put(dtoService.getId(), serviceMetaEntity);
				}
			}

			// get ordered host services and status
			String bubbleUpStatus = dtoHost.getBubbleUpStatus();
			if ((bubbleUpStatus != null) && bubbleUpStatus.equalsIgnoreCase("WARNING")) {
				bubbleUpStatus = "WARNING_HOST";
			}
			NetworkObjectStatusEnum [] status = new NetworkObjectStatusEnum[1];
			String [] tooltip = new String[1];
			String [] serviceSummary = new String[1];
			List<Integer> serviceList = getSortedHostServicesAndStatus(hostServiceIdMonitorStatuses, bubbleUpStatus,
					status, dtoHost.getAlias(), tooltip, serviceSummary, tree);

			// create host Network Meta Entity and put in host map
			Double serviceAvailability = 0.0;
			try {
				serviceAvailability = Double.parseDouble(dtoHost.getServiceAvailability());
			} catch (Exception e) {
			}
			boolean isAcknowledged = (dtoHost.getPropertyBoolean("isProblemAcknowledged") || dtoHost.isAcknowledged());
			String hostPrefix = getPrefix(dtoHost.getAppTypeDisplayName(), "host");

			NetworkMetaEntity hostMetaEntity = new NetworkMetaEntity(dtoHost.getId(),hostPrefix,
					dtoHost.getHostName(), dtoHost.getAppType(), status[0], NodeType.HOST, tooltip[0],
					dtoHost.getLastCheckTime(), serviceList, serviceAvailability, isAcknowledged,
					dtoHost.getLastStateChange());
			hostMetaEntity.setLastPluginOutputString(dtoHost.getLastPlugInOutput());
			hostMetaEntity.setMonitorStatus(dtoHost.getMonitorStatus());
			hostMetaEntity.setSummary(serviceSummary[0]);
			hostMetaEntity.setInScheduledDown(dtoHost.getPropertyInteger("ScheduledDowntimeDepth"));
			tree.getHostMap().put(dtoHost.getId(), hostMetaEntity);
		}
	}

	private void clearHostServices(int hostId, ConcurrentReferenceTreeModel tree) {
		// remove services from serviceMap associated with
		// this host (if host exists)
		NetworkMetaEntity hostEntity = tree.getHostMap().get(hostId);
		if (hostEntity != null) {
			List<Integer> hostServicesList = hostEntity.getChildNodeList();
			if (hostServicesList != null) {
				for (Integer serviceId : hostServicesList) {
					tree.getServiceMap().remove(serviceId);
				}
			}
		}
	}

	private void getHostServiceStatus(String serviceMonitorStatus, NetworkObjectStatusEnum [] status, String hostName,
									  String [] tooltip, String serviceDescription, String [] extendedName) {
		status[0] = NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(serviceMonitorStatus, NodeType.SERVICE);
		tooltip[0] = HOST_TEXT + hostName;
		extendedName[0] = new StringBuilder(serviceDescription).append(Constant.OPENING_ROUND_BRACE)
				.append(hostName).append(Constant.CLOSING_ROUND_BRACE).toString();
	}

	private List<Integer> getSortedHostServicesAndStatus(Map<Integer,String> hostServiceIdMonitorStatuses,
														 String bubbleUpStatus, NetworkObjectStatusEnum [] status,
														 String hostAlias, String [] tooltip,
														 String [] serviceSummary,
                                                         ConcurrentReferenceTreeModel tree) {
		int troubledServiceCount = 0;
		long downCount = 0;
		long warningCount = 0;
		long unknownCount = 0;
		long pendingCount = 0;
		long upCount = 0;
		List<NetworkMetaEntity> serviceEntityList = new ArrayList<NetworkMetaEntity>();
		if (hostServiceIdMonitorStatuses != null) {
			// Now: update services map under this host
			for (Map.Entry<Integer,String> hostServiceIdMonitorStatus : hostServiceIdMonitorStatuses.entrySet()) {
				NetworkMetaEntity serviceEntity = tree.getServiceMap().get(hostServiceIdMonitorStatus.getKey());
				if (serviceEntity != null) {
					serviceEntityList.add(serviceEntity);
					if (serviceEntity.getStatus() != NetworkObjectStatusEnum.SERVICE_OK) {
						troubledServiceCount++;
					}
					// DO Summary here
					String statusName = hostServiceIdMonitorStatus.getValue();
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
			}
		} // end if

		List<Integer> serviceList = new ArrayList<Integer>();
		// sort list
		Collections.sort(serviceEntityList);
		for (NetworkMetaEntity entity : serviceEntityList) {
			serviceList.add(entity.getObjectId());
		}

		// return sorted service list and status
		status[0] = NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(bubbleUpStatus, NodeType.HOST);
		StringBuilder tooltipBuilder = new StringBuilder().append(aliasString).append(Constant.SPACE)
				.append((hostAlias != null) ? hostAlias : "").append(Constant.BR);
		tooltip[0] = tooltipBuilder.append(troubledString).append(SERVICES_TEXT).append(troubledServiceCount).toString();
		StringBuilder serviceSummaryBuilder = new StringBuilder();
		if (downCount > 0) {
			serviceSummaryBuilder.append(downCount).append(" services CRITICAL, ");
		}
		if (warningCount > 0) {
			serviceSummaryBuilder.append(warningCount).append(" services WARNING, ");
		}
		if (unknownCount > 0) {
			serviceSummaryBuilder.append(unknownCount).append(" services UNKNOWN, ");
		}
		if (pendingCount > 0) {
			serviceSummaryBuilder.append(pendingCount).append(" services PENDING, ");
		}
		if (upCount > 0) {
			serviceSummaryBuilder.append(upCount).append(" services OK");
		}
		serviceSummary[0] = serviceSummaryBuilder.toString();
		return serviceList;
	}

	/**
	 * Returns all Host Group NetworkMeta Entities list
	 *
	 * @return all Host Group NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getAllHostGroups() {
		ArrayList<NetworkMetaEntity> array = new ArrayList<NetworkMetaEntity>(
				referenceTree.getHostGroupMap().values());
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
					referenceTree.getHostGroupMap().values());
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
		return referenceTree.getHostMap().values().iterator();
	}

	/**
	 * Returns all Service NetworkMeta Entities list
	 *
	 * @return all Service NetworkMeta Entities list
	 */
	public Iterator<NetworkMetaEntity> getAllServices() {
		return referenceTree.getServiceMap().values().iterator();
	}

	/**
	 * Returns all Host NetworkMeta Entities list under a particular Host Group
	 *
	 * @param hostGroupId
	 * @return all Hosts under a HostGroup
	 */
	// TODO use iterator
	public Iterator<Integer> getHostsUnderHostGroup(Integer hostGroupId) {

		if (null != hostGroupId && referenceTree.getHostGroupMap().containsKey(hostGroupId)) {
			NetworkMetaEntity hostGroupMetaEntity = referenceTree.getHostGroupMap()
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

		if (null != hostId && referenceTree.getHostMap().containsKey(hostId)) {
			NetworkMetaEntity hostMetaEntity = referenceTree.getHostMap().get(hostId);
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
				referenceTree.getServiceGroupMap().values());
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
					referenceTree.getServiceGroupMap().values());
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
	 * Populates the legacy Custom Groups
	 *
	 * @return
	 */
	private Map<Long,CustomGroup> populateCustomGroups() {
		Map<Long,CustomGroup> customGroupsLocal = null;
		try {
			Collection<CustomGroup> custGroupCol = getWSFacade()
					.findCustomGroups();
			customGroupsLocal = new HashMap<Long,CustomGroup>();
			for (CustomGroup customGroup : custGroupCol) {
				customGroupsLocal.put(customGroup.getGroupId(), customGroup);
			}
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage(), exc);
		}
		return customGroupsLocal;
	}

	/**
	 * Return sorted published root custom group models.
	 *
	 * @return collection of custom group models
	 */
	public List<NetworkMetaEntity> getRootCustomGroups() {
		// sorted published root custom group models
		List<NetworkMetaEntity> customGroups = new ArrayList<NetworkMetaEntity>(referenceTree.getCustomGroupRootMap().values());
		Collections.sort(customGroups);
		return customGroups;
	}

	/**
	 * Return custom group model by id. Updates model status before returning.
	 *
	 * @param groupId custom group id
	 * @return custom group model
	 */
	public NetworkMetaEntity getCustomGroupById(Integer groupId) {
		// get custom group model
		NetworkMetaEntity customGroupMetaEntity = referenceTree.getCustomGroupMap().get(groupId);
		if (customGroupMetaEntity != null) {
			// update status
			NetworkObjectStatusEnum status = determineBubbleUpStatusForCustomGroup(customGroupMetaEntity);
			if (status != null) {
				NetworkObjectStatusEnum adjustedStatus = NetworkObjectStatusEnum.getStatusEnumFromMonitorStatus(
						status.getMonitorStatusName(), NodeType.CUSTOM_GROUP);
				customGroupMetaEntity.setStatus(adjustedStatus);
				return customGroupMetaEntity;
			}
		}
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
				&& referenceTree.getServiceGroupMap().containsKey(serviceGroupId)) {
			NetworkMetaEntity serviceGroupMetaEntity = referenceTree.getServiceGroupMap()
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
		return referenceTree.getServiceGroupMap().get(serviceGroupId);
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
		return referenceTree.getServiceMap().get(serviceStatusID);
	}

	/**
	 * Returns Host Group Entity by Id. This method can return null!
	 *
	 * @param hostGroupId
	 *
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getHostGroupById(Integer hostGroupId) {
		return referenceTree.getHostGroupMap().get(hostGroupId);
	}

	/**
	 * Returns Host Entity by Id. This method can return null!
	 *
	 * @param hostID
	 * @return NetworkMetaEntity
	 */
	public NetworkMetaEntity getHostById(Integer hostID) {
		return referenceTree.getHostMap().get(hostID);
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
				values = referenceTree.getHostMap().values();
				break;
			case HOST_GROUP:
				values = referenceTree.getHostGroupMap().values();
				break;
			case SERVICE:
				values = referenceTree.getServiceMap().values();
				break;
			case SERVICE_GROUP:
				values = referenceTree.getServiceGroupMap().values();
				break;
			case CUSTOM_GROUP:
				values = referenceTree.getCustomGroupMap().values();
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
		Collection<NetworkMetaEntity> serviceGroupMetaEntities = referenceTree.getServiceGroupMap()
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
	 * updates host (JMS push)
	 *
	 * @param id
	 */
	public void updateHost(Integer id) {
		try {
			// update host model using REST host client
			List<DtoHost> dtoHosts = null;
			if (useRTMMClient) {
				RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				DtoHost dtoHost = rtmmClient.lookupHost((id != null) ? id : 0);
				dtoHosts = ((dtoHost != null) ? Collections.singletonList(dtoHost) : null);
			} else {
				HostClient hostClient = new HostClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				dtoHosts = hostClient.query("id = " + ((id != null) ? id : 0), DtoDepthType.Deep);
			}
			if ((dtoHosts != null) && !dtoHosts.isEmpty()) {
				// update host and service model
				restUpdateHostAndServiceModel(dtoHosts, referenceTree);
			} else {
				// remove deleted host
                if (id != null) referenceTree.getHostMap().remove(id);
			}
		} catch (Exception e) {
			// log error and force refresh
			LOGGER.error("Unexpected host update exception: "+ e.getMessage(), e);
			initialized = false;
			//startRebuildTreeModelJob();
		}
	}

	/**
	 * updates host group (JMS push)
	 *
	 * @param id
	 */
	public void updateHostGroup(Integer id) {
		try {
			// update host group model using REST host group client
			List<DtoHostGroup> dtoHostGroups;
			if (useRTMMClient) {
				RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				DtoHostGroup dtoHostGroup = rtmmClient.lookupHostGroup((id != null) ? id : 0);
				dtoHostGroups = ((dtoHostGroup != null) ? Collections.singletonList(dtoHostGroup) : null);
			} else {
				HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				dtoHostGroups = hostGroupClient.query("id = " + ((id != null) ? id : 0));
			}
			if ((dtoHostGroups != null) && (dtoHostGroups.size() == 1) && (dtoHostGroups.get(0).getHosts() != null) &&
					!dtoHostGroups.get(0).getHosts().isEmpty()) {
				// update host group host and service model using REST host client
				List<DtoHost> dtoHosts;
				if (useRTMMClient) {
					RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					List<Integer> hostIds = new ArrayList<>();
					for (DtoHost dtoHost : dtoHostGroups.get(0).getHosts()) {
						hostIds.add(dtoHost.getId());
					}
					dtoHosts = rtmmClient.lookupHosts(hostIds);
				} else {
					HostClient hostClient = new HostClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
					StringBuilder query = new StringBuilder("id in (");
					for (DtoHost dtoHost : dtoHostGroups.get(0).getHosts()) {
						query.append((query.charAt(query.length() - 1) != '(') ? "," : "").append(dtoHost.getId());
					}
					dtoHosts = hostClient.query(query.append(")").toString(), DtoDepthType.Deep);
				}
				// update host and service model
				restUpdateHostAndServiceModel(dtoHosts, referenceTree);
				// update host group model
				restUpdateHostGroupModel(dtoHostGroups, referenceTree);
			} else {
				// remove deleted or empty host group
				if (id != null) referenceTree.getHostGroupMap().remove(id);
			}
		} catch (Exception e) {
			// log error and force refresh
			LOGGER.error("Unexpected host group update exception: " + e, e);
			initialized = false;
			//startRebuildTreeModelJob();
		}
	}

	/**
	 * Updates Service group
	 *
	 * @param id
	 */
	public void updateServiceGroup(Integer id) {
		try {
			// update service group model using REST service group client
			List<DtoServiceGroup> dtoServiceGroups;
			if (useRTMMClient) {
				RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				DtoServiceGroup dtoServiceGroup = rtmmClient.lookupServiceGroup((id != null) ? id : 0);
				dtoServiceGroups = ((dtoServiceGroup != null) ? Collections.singletonList(dtoServiceGroup) : null);
			} else {
                ServiceGroupClient serviceGroupClient = new ServiceGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
                dtoServiceGroups = serviceGroupClient.query("id = " + ((id != null) ? id : 0));
			}
			if ((dtoServiceGroups != null) && (dtoServiceGroups.size() == 1) && (dtoServiceGroups.get(0).getServices() != null) &&
					!dtoServiceGroups.get(0).getServices().isEmpty()) {
				// update service group model
				restUpdateServiceGroupModel(dtoServiceGroups, referenceTree);
			} else {
				// remove deleted or empty service group
				if (id != null) referenceTree.getServiceGroupMap().remove(id);
			}
		} catch (Exception e) {
			// log error and force refresh
			LOGGER.error("Unexpected service group update exception: "+e, e);
			initialized = false;
			//startRebuildTreeModelJob();
		}
	}

	/**
	 * Update legacy custom group in maps.
	 *
	 * @param id custom group id or 0
	 * @param dependentIds dependent custom group ids or null
	 */
	public void updateCustomGroup(long id, Collection<Long> dependentIds) {
		try {
			// get updated custom group and update model
			Map<Long,CustomGroup> updatedCustomGroups = populateCustomGroups();
			if (id != 0L) {
				CustomGroup updateCustomGroup = updatedCustomGroups.get(id);
				if (updateCustomGroup != null) {
					updateCustomGroupModel(updateCustomGroup, updatedCustomGroups, referenceTree);
				} else {
					referenceTree.getCustomGroupMap().remove(-(int)id); // negate legacy custom group ids
					referenceTree.getCustomGroupRootMap().remove(-(int)id); // negate legacy custom group ids
				}
			}
			// update dependent custom groups
			if ((dependentIds != null) && !dependentIds.isEmpty()) {
				for (Long dependentId : dependentIds) {
					CustomGroup updateCustomGroup = updatedCustomGroups.get(dependentId);
					if (updateCustomGroup != null) {
						updateCustomGroupModel(updateCustomGroup, updatedCustomGroups, referenceTree);
					} else {
						referenceTree.getCustomGroupMap().remove(-dependentId.intValue()); // negate legacy custom group ids
						referenceTree.getCustomGroupRootMap().remove(-dependentId.intValue()); // negate legacy custom group ids
					}
				}
			}
		} catch (Exception e) {
			// log error and force refresh
			LOGGER.error("Unexpected custom group update exception: "+e, e);
			initialized = false;
			//startRebuildTreeModelJob();
		}
	}

	/**
	 * Update custom group in maps.
	 *
	 * @param id custom group
	 */
	private void updateRestCustomGroup(int id) {
		try {
			// get updated custom group model using REST custom group client
			List<DtoCustomGroup> dtoCustomGroups = null;
			if (useRTMMClient) {
				RTMMClient rtmmClient = new RTMMClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				DtoCustomGroup customGroup = rtmmClient.lookupCustomGroup(id);
				dtoCustomGroups = ((customGroup != null) ? Collections.singletonList(customGroup) : null);
			} else {
				CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
				dtoCustomGroups = customGroupClient.query("id = " + id);
			}
			if ((dtoCustomGroups != null) && (dtoCustomGroups.size() == 1)) {
				// Update custom group model; updating only single custom group
				// assuming notifications arrive here in created, modified, and
				// removed custom group order for effected group, parents, and
				// children. Also assumes host group and service groups have been
				// previously created.
				restUpdateCustomGroupModel(dtoCustomGroups, referenceTree);
			} else {
				// remove deleted custom group; updating single custom group
				// assuming notifications arrive here for effected group, parents,
				// and children.
				referenceTree.getCustomGroupMap().remove(id);
				referenceTree.getCustomGroupRootMap().remove(id);
			}
		} catch (Exception e) {
			// log error and force refresh
			LOGGER.error("Unexpected custom group update exception: "+e, e);
			initialized = false;
		}
	}

	/**
	 * Removes the host from hostMap
	 *
	 * @param id
	 */
	public void removeHost(int id) {
		referenceTree.getHostMap().remove(Integer.valueOf(id));
	}

	/**
	 * Removes the host from serviceMap
	 *
	 * @param id
	 */
	public void removeService(int id) {
		referenceTree.getServiceMap().remove(Integer.valueOf(id));
	}

	/**
	 * Remove service group
	 *
	 * @param id
	 */
	private void removeServiceGroup(Integer id) {
		referenceTree.getServiceGroupMap().remove(id);
	}

	/**
	 * Remove host group
	 *
	 * @param id
	 */
	private void removeHostGroup(Integer id) {
		referenceTree.getHostGroupMap().remove(id);
	}

	/**
	 * Remove legacy custom group from maps.
	 *
	 * @param id custom group id
	 * @param dependentIds dependent custom group ids or null
	 */
	public void removeCustomGroup(long id, Collection<Long> dependentIds) {
		// remove deleted custom group
		referenceTree.getCustomGroupMap().remove(-(int)id); // negate legacy custom group ids
		referenceTree.getCustomGroupRootMap().remove(-(int)id); // negate legacy custom group ids
		// update dependent custom groups
		if ((dependentIds != null) && !dependentIds.isEmpty()) {
			updateCustomGroup(0L, dependentIds);
		}
	}

	/**
	 * Remove custom group from maps.
	 *
	 * @param id custom group id
	 */
	private void removeRestCustomGroup(int id) {
		// remove deleted custom group; updating single custom group
		// assuming notifications arrive here for effected group,
		// parents, and children.
		referenceTree.getCustomGroupMap().remove(id);
		referenceTree.getCustomGroupRootMap().remove(id);
	}

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
		Collection<NetworkMetaEntity> hostGroupMetaEntities = referenceTree.getHostGroupMap()
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
		NetworkMetaEntity serviceMetaEntity = referenceTree.getServiceMap().get(serviceStatusId);
		if (null != serviceMetaEntity) {
			return referenceTree.getHostMap().get(serviceMetaEntity.getParentId());
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
					NetworkMetaEntity hostEntity = referenceTree.getHostMap().get(hostId);
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
						NetworkMetaEntity serviceEntity = referenceTree.getServiceMap()
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
						NetworkMetaEntity hostEntity = referenceTree.getHostMap().get(hostId);
						if (null != hostEntity) {
							List<Integer> serviceList = hostEntity
									.getChildNodeList();
							if (null != serviceList) {
								for (Integer serviceId : serviceList) {
									NetworkMetaEntity serviceEntity = referenceTree.getServiceMap()
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
					NetworkMetaEntity hgEntity = referenceTree.getHostGroupMap().get(nodeID);
					if (null != hgEntity) {
						return hgEntity;
					}
				}
				break;

			case SERVICE_GROUP:
				if (extRoleServiceGroupList.isEmpty()
						|| extRoleServiceGroupList.contains(nodeName)) {
					NetworkMetaEntity sgEntity = referenceTree.getServiceGroupMap().get(nodeID);
					if (null != sgEntity) {
						return sgEntity;
					}
				}
				break;

			case HOST:
				if (extRoleHostGroupList.isEmpty()) {
					NetworkMetaEntity hostEntity = referenceTree.getHostMap().get(nodeID);
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
							return referenceTree.getHostMap().get(nodeID);
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
							return referenceTree.getServiceMap().get(nodeID);
						}
					}
				}
			/*
			 * Service is not in the list of allowed service groups. Hence check
			 * for hosts in the allowed Host Groups list
			 */
				NetworkMetaEntity serviceMetaEntity = referenceTree.getServiceMap().get(nodeID);
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
			new NetworkMetaEntity(0, null, "Entire Network", null,
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
			metaEntity = referenceTree.getHostGroupMap().get(referenceTree.getHostGroupMap().keySet().iterator()
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
			metaEntity = referenceTree.getServiceGroupMap().get(referenceTree.getServiceGroupMap().keySet()
					.iterator().next());
			if (null != metaEntity) {
				return metaEntity;
			}
		}

		// else return Entire Network meta entity, as both lists are empty
		return new NetworkMetaEntity(0, null, "Entire Network", null,
				NetworkObjectStatusEnum.ENTIRE_NETWORK_STATUS,
				NodeType.NETWORK, null, null, null);
	}

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Dump meta model map if logging in debug mode
	 *
	 * @param name meta model map name
	 * @param networkMetaEntityMap meta model map
	 */
	private void debugDumpNetworkMetaEntity(String name, Map<Integer,NetworkMetaEntity> networkMetaEntityMap) {
		if (LOGGER.isDebugEnabled()) {
			String EOL = System.getProperty("line.separator");
			StringBuilder dump = new StringBuilder("ReferenceTreeMetaModel ").append(name).append(EOL);
			List<Integer> keys = new ArrayList<Integer>(networkMetaEntityMap.keySet());
			Collections.sort(keys);
			for (Integer key : keys) {
				dump.append(networkMetaEntityMap.get(key).toString()).append(EOL);
			}
			LOGGER.debug(dump.toString());
		}
	}

}
