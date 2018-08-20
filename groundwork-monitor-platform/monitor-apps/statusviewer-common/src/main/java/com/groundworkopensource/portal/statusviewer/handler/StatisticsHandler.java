package com.groundworkopensource.portal.statusviewer.handler;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateStatistics;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.bean.AcknowledgePopupBean;
import com.groundworkopensource.portal.statusviewer.bean.HostGroupStatisticsBean;
import com.groundworkopensource.portal.statusviewer.bean.HostStatisticsBean;
import com.groundworkopensource.portal.statusviewer.bean.ModelPopUpDataBean;
import com.groundworkopensource.portal.statusviewer.bean.PopUpSelectBean;
import com.groundworkopensource.portal.statusviewer.bean.ServiceGroupStatistics;
import com.groundworkopensource.portal.statusviewer.bean.ServiceStatisticsBean;
import com.groundworkopensource.portal.statusviewer.bean.StatisticsBean;
import com.groundworkopensource.portal.statusviewer.bean.StatisticsModelPopUpListBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.FilterComputer;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * This Class provide Statistical data from foundation web service.
 * 
 * @author manish_kjain
 * 
 */
public class StatisticsHandler /* extends ServerPush */
implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1831378605607611581L;

	/**
	 * host name string property
	 */
	private static final String HOST_HOST_NAME_STRING_PROPERTY = "host.hostName";

	/**
	 * logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(StatisticsHandler.class.getName());

	/**
	 * list for applied filter on host.
	 */
	private List<ModelPopUpDataBean> filteredHostList = null;
	/**
	 * list for All host.
	 */
	private List<ModelPopUpDataBean> allHostList = null;

	/**
	 * list for applied filter on host Group.
	 */
	private List<ModelPopUpDataBean> filteredHostGroupList = null;
	/**
	 * list for All host Group.
	 */
	private List<ModelPopUpDataBean> allServiceGroupList = null;
	/**
	 * list for All Service Group Popup data.
	 */
	private List<ModelPopUpDataBean> filteredServiceGroupList = null;

	/**
	 * list for All Filtered Popup data.
	 */
	private List<ModelPopUpDataBean> allHostGroupList = null;

	/**
	 * current monitor status of Host group model pop up.
	 */
	private String hostGroupPopUpStatus = null;

	/**
	 * Network object status Enum
	 */
	private NetworkObjectStatusEnum status;

	/**
	 * if hostAcknowledgedRender is true then render Acknowledged column in
	 * model pop up other wise not.
	 */
	private boolean hostAcknowledgedRender = true;

	/**
	 * if serviceAcknowledgedRender is true then render Acknowledged column in
	 * model pop up other wise not.
	 */
	private boolean serviceAcknowledgedRender = true;

	/**
	 * if hostDateTimeRender is true then render Date-Time column in model pop
	 * up other wise not.
	 */
	private boolean hostDateTimeRender = true;

	/**
	 * if hostDateTimeRender is true then render Date-Time column in model pop
	 * up other wise not.
	 */
	private boolean serviceDateTimeRender = true;

	/**
	 * StatisticsModelPopUpListBean
	 */
	private StatisticsModelPopUpListBean statisticsModelPopUpListBean;

	/**
	 * Host Status Array.
	 */
	private final String[] hostStatusArray = { Constant.UN_SCHEDULED_DOWN,
			Constant.SCHEDULED_DOWN, Constant.UNREACHABLE_CAMEL_CASE,
			Constant.PENDING_CAMEL_CASE, Constant.UP_CAMEL_CASE };
	/**
	 * Service Status Array.
	 */

	private final String[] serviceStatusArray = {
			Constant.UNSCHEDULED_CRITICAL, Constant.SCHEDULED_CRITICAL,
			Constant.WARNING, Constant.UNKNOWN_CAMEL_CASE,
			Constant.PENDING_CAMEL_CASE, Constant.OK_CAMEL_CASE };

	/**
	 * host portlet Pop up monitor status.
	 */
	private String hostPopUpStatus = null;

	/**
	 * IWSFacade instance variable.
	 */
	private IWSFacade foundFacade = null;

	/**
	 * stateController instance variable
	 */
	private StateController stateController = null;
	/**
	 * current model pop status of service portlet.
	 */
	private String serviceCurrentStatus = null;
	/**
	 * current model pop status of service Group portlet.
	 */
	private String serviceGroupCurrentStatus = null;

	/**
	 * HostGroupStatisticsBean instance.
	 */
	private HostGroupStatisticsBean statisbeanHG = null;

	/**
	 * ServiceGroupStatistics instance.
	 */
	private ServiceGroupStatistics statisbeanSG = null;

	/**
	 * HostStatisticsBean instance.
	 */
	private HostStatisticsBean statishostbeanHS = null;

	/**
	 * ServiceStatisticsBean instance.
	 */
	private ServiceStatisticsBean statisbeanSS = null;

	/**
	 * Error boolean to set if error occurred in host statistics
	 */
	private boolean hostError = false;

	/**
	 * info boolean to set if information occurred in host statistics
	 */
	private boolean hostInfo = false;

	/**
	 * boolean variable message set true when display any type of messages
	 * (Error,info or warning) in UI
	 */
	private boolean hostMessage = false;

	/**
	 * information message to show on UI
	 */
	private String hostInfoMessage;

	/**
	 * Error message to show on UI
	 */
	private String hostErrorMessage;

	/**
	 * Error boolean to set if error occurred in Service statistics
	 */
	private boolean serviceError = false;

	/**
	 * info boolean to set if information occurred in Service statistics
	 */
	private boolean serviceInfo = false;

	/**
	 * boolean variable message set true when display any type of messages
	 * (Error,info or warning) in UI
	 */
	private boolean serviceMessage = false;

	/**
	 * information message to show on UI
	 */
	private String serviceInfoMessage;

	/**
	 * info boolean to set if information occurred
	 */
	private boolean info = false;

	/**
	 * error boolean to set if information occurred
	 */
	private boolean error = false;

	/**
	 * boolean variable message set true when display any type of messages
	 * (Error,info or warning) in UI
	 */
	private boolean message = false;

	/**
	 * information message to show on UI
	 */
	private String infoMessage;

	/**
	 * Error message to show on UI
	 */
	private String errorMessage;

	/**
	 * Error boolean to set if error occurred
	 */
	private boolean errorPopUp = false;
	/**
	 * Error message to show on UI
	 */
	private String serviceErrorMessage;

	/**
	 * Error message to show on UI
	 */
	private String errorMessagePopUp;

	/**
	 * selectedNodeId
	 */
	private int selectedNodeId;
	/**
	 * selectedNodeType
	 */
	private NodeType selectedNodeType;
	/**
	 * selectedNodeName
	 */
	private String selectedNodeName;

	/**
	 * Error boolean to set if error occurred in host group statistics
	 */
	private boolean hgError = false;

	/**
	 * info boolean to set if information occurred in host group statistics
	 */
	private boolean hgInfo = false;

	/**
	 * boolean variable message set true when display any type of messages
	 * (Error,info or warning) in UI
	 */
	private boolean hgMessage = false;

	/**
	 * information message to show on UI
	 */
	private String hgInfoMessage;

	/**
	 * Error message to show on UI
	 */
	private String hgErrorMessage;

	/**
	 * Error boolean to set if error occurred in service group statistics
	 */
	private boolean sgError = false;

	/**
	 * info boolean to set if information occurred in service group statistics
	 */
	private boolean sgInfo = false;

	/**
	 * boolean variable message set true when display any type of messages
	 * (Error,info or warning) in UI
	 */
	private boolean sgMessage = false;

	/**
	 * information message to show on UI
	 */
	private String sgInfoMessage;

	/**
	 * Error message to show on UI
	 */
	private String sgErrorMessage;

	/**
	 * Parameter for host name
	 */
	private static final String PARAM_HOST_NAME = "hostName";

	/**
	 * Parameter for service name
	 */
	private static final String PARAM_SERVICE_NAME = "serviceName";
	/**
	 * serviceStyle for service group view
	 */
	private String serviceStyle = Constant.EMPTY_STRING;

	/**
	 * blank panel group render variable .true if selected node type is host
	 * other wise false
	 */
	private boolean pnlGroupBlankRender = false;
	/**
	 * dashboardInfo variable indicate weather info message for dash board or
	 * not
	 */
	private boolean dashboardInfo = false;

	/**
	 * SubpageIntegrator
	 */
	private SubpageIntegrator subpageIntegrator;

	/**
	 * ReferenceTreeMetaModel instance
	 * <p>
	 * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
	 * referenceTreeModel.
	 */
	private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
			.getManagedBean(Constant.REFERENCE_TREE);

	/**
	 * preferences Keys Map to be used for reading preferences.
	 */
	private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
	static {
		/*
		 * NOTE: Statistics Portlets are Network View portlets. So default
		 * preferences must NOT have been set/specified in portlet.xml in order
		 * to work properly in status-viewer => Entire Network view. (Refer
		 * Roger's mail)
		 */
		PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
				NodeType.HOST_GROUP);
		PREFERENCE_KEYS_MAP.put(Constant.NODE_NAME_PREF, NodeType.SERVICE);
	}
	/**
	 * Flag to identify if portlet is placed in StatusViewer sub-pages apart
	 * from Network View.
	 */
	private boolean inStatusViewer;

	/**
	 * UserExtendedRoleBean instance.
	 */
	private UserExtendedRoleBean userExtendedRoleBean;

	/**
	 * constructor
	 */
	public StatisticsHandler() {
		subpageIntegrator = new SubpageIntegrator();
		foundFacade = new FoundationWSFacade();

		// get the UserRoleBean managed instance
		userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

		// as per application type, read parameters from request or from
		// preferences
		handleSubpageIntegration();
	}

	/**
	 * Handles the subpage integration: Reads parameters from request in case of
	 * Status Viewer. If portlet is in dashboard, reads preferences.
	 */
	private void handleSubpageIntegration() {
		boolean isPrefSet = subpageIntegrator
				.doSubpageIntegration(PREFERENCE_KEYS_MAP);
		this.stateController = subpageIntegrator.getStateController();
		inStatusViewer = subpageIntegrator.isInStatusViewer();
		String nodeTypePreference = null;
		if (!inStatusViewer) {
			try {
				nodeTypePreference = FacesUtils
						.getPreference(Constant.NODE_TYPE_PREF);
			} catch (PreferencesException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				LOGGER.debug("unable to get 'node type' preference.");
			}
		}
		if (!isPrefSet && null == nodeTypePreference) {
			/*
			 * Statistics Portlets are applicable for "Network View". So we
			 * should not show error here - instead assign Node Type as NETWORK
			 * with NodeId as 0.
			 */
			selectedNodeType = NodeType.NETWORK;
			selectedNodeId = 0;
			selectedNodeName = Constant.EMPTY_STRING;
			// handleInfo(new PreferencesException().getMessage());
			return;
		}

		// get the required data from SubpageIntegrator
		selectedNodeType = subpageIntegrator.getNodeType();
		selectedNodeId = subpageIntegrator.getNodeID();
		selectedNodeName = subpageIntegrator.getNodeName();

		if (!inStatusViewer) {
			try {
				// Node Name
				String nodeNamePreference = FacesUtils
						.getPreference(Constant.NODE_NAME_PREF);
				if (null != nodeNamePreference
						&& !nodeNamePreference.trim().equals(
								Constant.EMPTY_STRING)) {
					selectedNodeName = nodeNamePreference;
				}
			} catch (PreferencesException e) {
				// handleInfo(new PreferencesException().getMessage());
				dashboardInfo = true;
				return;
			}

			// Node Type Preference
			if (null == nodeTypePreference
					|| nodeTypePreference.trim().equals(Constant.EMPTY_STRING)
					|| (!nodeTypePreference.equals(NodeType.NETWORK
							.getTypeName()) && (selectedNodeName == null || selectedNodeName
							.equals(Constant.EMPTY_STRING)))) {
				List<String> extRoleHostGroupList = userExtendedRoleBean
						.getExtRoleHostGroupList();
				List<String> extRoleServiceGroupList = userExtendedRoleBean
						.getExtRoleServiceGroupList();
				if (extRoleHostGroupList.isEmpty()
						&& extRoleServiceGroupList.isEmpty()) {
					selectedNodeType = NodeType.NETWORK;
					selectedNodeName = Constant.EMPTY_STRING;

				} else if (!extRoleHostGroupList.isEmpty()
						&& !extRoleHostGroupList
								.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
					selectedNodeType = NodeType.HOST_GROUP;
					selectedNodeName = userExtendedRoleBean
							.getDefaultHostGroup();

				} else if (!extRoleServiceGroupList.isEmpty()
						&& !extRoleServiceGroupList
								.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
					selectedNodeType = NodeType.SERVICE_GROUP;
					selectedNodeName = userExtendedRoleBean
							.getDefaultServiceGroup();
				}
			} else {

				if (NodeType.SERVICE_GROUP.getTypeName().equals(
						nodeTypePreference)) {
					// Service Group
					selectedNodeType = NodeType.SERVICE_GROUP;

				} else if (NodeType.HOST_GROUP.getTypeName().equals(
						nodeTypePreference)) {
					// Host Group
					selectedNodeType = NodeType.HOST_GROUP;
				} else if (NodeType.HOST.getTypeName().equals(
						nodeTypePreference)) {
					// Host Group
					selectedNodeType = NodeType.HOST;
				} else if (NodeType.NETWORK.getTypeName().equals(
						nodeTypePreference)) {
					// Entire Network
					selectedNodeType = NodeType.NETWORK;
				}

				if (selectedNodeType == null
						|| NodeType.NETWORK.equals(selectedNodeType)) {
					selectedNodeType = NodeType.NETWORK;
					return;
				}

			}
			// in dashboard. Fetch node type and node name. Do the
			// validations
			// as per the node type.
			handleDashboardProcessing();

		}

		// check if selected node name is null then set default node type and
		// name.
		if (selectedNodeName == null) {
			LOGGER.debug("selectedNodeName is null ...Setting default(Entire network) node type and name");
			selectedNodeType = NodeType.NETWORK;
			selectedNodeId = 0;
			selectedNodeName = Constant.EMPTY_STRING;
		}

		LOGGER.debug("################### [Statistics Portlets] # Node Type ["
				+ selectedNodeType + "] # Node Name [" + selectedNodeName
				+ "] # Node ID [" + selectedNodeId + "]");
	}

	/**
	 * Handles Dashboard Processing for statistics portlets (Currently only Host
	 * and Service statistics preferences are allowed to be edited in dashboard.
	 * Host Group and Service Group statistics / summary portlets are applicable
	 * only to Entire Network node type.)
	 * 
	 * @return
	 * 
	 */
	private boolean handleDashboardProcessing() {
		/*
		 * if in the dashboard, retrieve the ID from HostName / ServiceGroup
		 * Name / HostGroup Name passed as preference. Assign this Id to the
		 * selectedNodeId
		 */

		switch (selectedNodeType) {
		case HOST:

			// validate if entered Host exists
			try {
				SimpleHost hostByName = foundFacade.getSimpleHostByName(
						selectedNodeName, false);
				if (null == hostByName) {
					throw new WSDataUnavailableException();
				}
				/*
				 * set the selected node Id here (seems weird but required for
				 * JMS Push in Dashboard)
				 */
				selectedNodeId = hostByName.getHostID();

				// check for extended role permissions
				if (!referenceTreeModel.checkNodeForExtendedRolePermissions(
						selectedNodeId, NodeType.HOST, selectedNodeName,
						userExtendedRoleBean.getExtRoleHostGroupList(),
						userExtendedRoleBean.getExtRoleServiceGroupList())) {
					String inadequatePermissionsMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
							+ " [" + selectedNodeName + "]";
					handleInfo(inadequatePermissionsMessage);
					dashboardInfo = true;
					return false;
				}
			} catch (WSDataUnavailableException e) {
				String hostNotAvailableErrorMessage = ResourceUtils
						.getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
						+ " [" + selectedNodeName + "]";
				handleInfo(hostNotAvailableErrorMessage);
				dashboardInfo = true;
				return false;
			} catch (GWPortalGenericException e) {
				LOGGER.warn(
						"Exception while retrieving Host By Name in Dashboard. JMS PUSH may not work. Exception ["
								+ e.getMessage() + "]", e);
			}

			break;

		case SERVICE_GROUP:

			// validate if entered Service Group exists
			try {
				Category category = foundFacade
						.getCategoryByName(selectedNodeName);
				if (null == category) {
					// show appropriate error message to the user
					String serviceGroupNotAvailableErrorMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_serviceGroupUnavailable")
							+ " [" + selectedNodeName + "]";
					LOGGER.error(serviceGroupNotAvailableErrorMessage);
					handleInfo(serviceGroupNotAvailableErrorMessage);
					dashboardInfo = true;
					return false;
				}
				// set the selected node Id
				selectedNodeId = category.getCategoryId();

				// check for extended role permissions
				if (!userExtendedRoleBean.getExtRoleServiceGroupList()
						.isEmpty()
						&& !referenceTreeModel
								.checkNodeForExtendedRolePermissions(
										selectedNodeId, NodeType.SERVICE_GROUP,
										selectedNodeName, userExtendedRoleBean
												.getExtRoleHostGroupList(),
										userExtendedRoleBean
												.getExtRoleServiceGroupList())) {
					String inadequatePermissionsMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
							+ " [" + selectedNodeName + "]";
					handleInfo(inadequatePermissionsMessage);
					dashboardInfo = true;
					return false;
				}
			} catch (GWPortalGenericException e) {
				LOGGER.warn(
						"Exception while retrieving Service Group By Name in Dashboard. JMS PUSH may not work. Exception ["
								+ e.getMessage() + "]", e);
			}

			break;

		case HOST_GROUP:
			try {
				HostGroup hostGroupsByName = foundFacade
						.getHostGroupsByName(selectedNodeName);
				selectedNodeId = hostGroupsByName.getHostGroupID();

				// check for extended role permissions
				if (!userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()
						&& !referenceTreeModel
								.checkNodeForExtendedRolePermissions(
										selectedNodeId, NodeType.HOST_GROUP,
										selectedNodeName, userExtendedRoleBean
												.getExtRoleHostGroupList(),
										userExtendedRoleBean
												.getExtRoleServiceGroupList())) {
					String inadequatePermissionsMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
							+ " [" + selectedNodeName + "]";
					handleInfo(inadequatePermissionsMessage);
					dashboardInfo = true;
					return false;
				}
			} catch (WSDataUnavailableException e) {
				String hostGroupNotAvailableErrorMessage = ResourceUtils
						.getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
						+ " [" + selectedNodeName + "]";
				LOGGER.error(hostGroupNotAvailableErrorMessage);
				handleInfo(hostGroupNotAvailableErrorMessage);
				dashboardInfo = true;
				return false;

			} catch (GWPortalGenericException e) {
				LOGGER.warn(
						"Exception while retrieving Host Group By Name. JMS PUSH may not work. Exception ["
								+ e.getMessage() + "]", e);
			}

			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * get host group statistics from foundation web service. set host group
	 * statistics list in host group statistics bean. create pie chart byte
	 * array and set in host group statistics bean.
	 */
	public void setHostGroupStatistics() {
		if (!inStatusViewer
				&& !userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()) {
			// user does not have access to entire network
			String inadequatePermissionsMessage = ResourceUtils
					.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
					+ " [ Entire Network ] data.";
			handleHGInfo(inadequatePermissionsMessage);
			dashboardInfo = true;
			return;
		}

		List<StatisticsBean> networkHostGroupCountList = Collections
				.synchronizedList(new ArrayList<StatisticsBean>());
		// hold bytes of pie chart
		byte[] pieChartBytes = null;
		Map<String, Long> filteredHostGroupStatisticsMap = null;
		/**
		 * Host group statistics Map
		 */
		Map<String, Long> hostGroupStatisticsMap = null;

		try {
			if (foundFacade == null) {
				LOGGER.debug("foundFacade is null in setHostGroupStatistics method  ");
				throw new GWPortalException();
			}
			// Arul: Need to remove this null check to refresh the entirenetwork
			// when new message arrives.Offshore to check this
			// if (getHostGroupStatisticsMap() == null) {
			hostGroupStatisticsMap = foundFacade
					.getEntireNetworkHostGroupStatistics();
			LOGGER.debug("get host group statistics for entire network is Successful");
			// }

			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String currentHostGFilter = stateController.getCurrentHostFilter();
			// check if host filter is selected
			if (!Constant.EMPTY_STRING.equalsIgnoreCase(currentHostGFilter)) {
				FilterComputer filterComputerHG = new FilterComputer();
				Filter hostGroupFilter = filterComputerHG
						.getHostGroupFilter(currentHostGFilter);
				filteredHostGroupStatisticsMap = foundFacade
						.getGroupStatisticsForHostGroup(
								StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER,
								hostGroupFilter, null,
								Constant.NAGIOS.toUpperCase());
			}

			// Iterator<String> statusListitr = statusList.iterator();
			if (hostGroupStatisticsMap != null
					&& !hostGroupStatisticsMap.isEmpty()) {
				for (int i = 0; i < hostStatusArray.length; i++) {
					StatisticsBean statisticsbean = new StatisticsBean();
					status = NetworkObjectStatusEnum
							.getStatusEnumFromMonitorStatus(hostStatusArray[i],
									NodeType.HOST_GROUP);
					if (hostStatusArray[i]
							.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
									.getStatus())) {
						statisticsbean
								.setStatus(Constant.DOWN_COLON_UN_SCHEDULED);

					} else if (hostStatusArray[i]
							.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
									.getStatus())) {
						statisticsbean.setStatus(Constant.DOWN_COLON_SCHEDULED);

					} else {
						statisticsbean.setStatus(hostStatusArray[i]);
					}
					statisticsbean.setTotal(hostGroupStatisticsMap
							.get(hostStatusArray[i].toUpperCase()));
					if (filteredHostGroupStatisticsMap == null
							|| filteredHostGroupStatisticsMap.isEmpty()) {
						statisticsbean.setFiltered(hostGroupStatisticsMap.get(
								hostStatusArray[i].toUpperCase()).intValue());
					} else {
						statisticsbean
								.setFiltered(filteredHostGroupStatisticsMap
										.get(hostStatusArray[i].toUpperCase())
										.intValue());
					}
					// set monitor status icon path.
					statisticsbean.setImgsrc(status.getIconPath());

					networkHostGroupCountList.add(statisticsbean);

					// nullify the statistics bean used
					statisticsbean = null;
				}
				// check if filter host group statistics is null or empty then
				// draw pie chart with total statistics
				if (filteredHostGroupStatisticsMap == null
						|| filteredHostGroupStatisticsMap.isEmpty()) {
					if (hostGroupStatisticsMap.get(Constant.TOTAL) > 0) {
						pieChartBytes = getHGOrHPieChartBytes(hostGroupStatisticsMap);
					}

				} else {
					if (filteredHostGroupStatisticsMap.get(Constant.TOTAL) > 0) {
						pieChartBytes = getHGOrHPieChartBytes(filteredHostGroupStatisticsMap);
					}
				}

				/**
				 * Faces context will be null on JMS or Non JSF thread. Perform
				 * a null check. Make increase the visibility of the statisbean
				 * to class level for the JMS thread.
				 */
				if (FacesContext.getCurrentInstance() != null) {

					statisbeanHG = (HostGroupStatisticsBean) FacesUtils
							.getManagedBean(Constant.HOST_GROUP_STATISTICS_BEAN);
				}
				if (statisbeanHG != null) {
					statisbeanHG
							.setHostGroupCountList(networkHostGroupCountList);
					statisbeanHG.setHostGroupPieChart(pieChartBytes);
					// set current filter name
					statisbeanHG
							.setPreviousSelectedHostFilter(currentHostGFilter);
					statisbeanHG.setTotalHostGroupCount(hostGroupStatisticsMap
							.get(Constant.TOTAL));
					if (filteredHostGroupStatisticsMap == null
							|| filteredHostGroupStatisticsMap.isEmpty()) {
						statisbeanHG
								.setFilteredHostGroupCount(hostGroupStatisticsMap
										.get(Constant.TOTAL).intValue());
					} else {
						statisbeanHG
								.setFilteredHostGroupCount(filteredHostGroupStatisticsMap
										.get(Constant.TOTAL).intValue());
					}

				} else {
					LOGGER.debug("StatisBean HG is null");
				}

			} else {
				LOGGER.debug("hostGroupStatisticsMap is null or empty in setHostGroupStatistics method");
				throw new GWPortalException();
			}
		} catch (WSDataUnavailableException e) {
			handleHGError(e.getMessage());

		} catch (GWPortalException e) {
			handleHGError(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Exception While Drawing Pie chart in setHostGroupStatistics method ."
					+ e);
			handleHGError(new GWPortalException().getMessage());
		}

	}

	/**
	 * @param monitorStatus
	 * @param nodeName
	 * @return
	 */
	private Filter getHostMonitorStatusFilterForHostGroupName(
			String monitorStatus, String nodeName) {
		Filter filter;
		// Filter leftFilter = new Filter(FilterConstants.HOST_GROUP_ID,
		// FilterOperator.EQ, Integer.parseInt(nodeId));
		// using name instead - to support dashboard
		Filter leftFilter = new Filter(FilterConstants.HOST_GROUP_NAME,
				FilterOperator.EQ, nodeName);
		Filter rightFilter = new Filter(
				FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
				FilterOperator.EQ, monitorStatus.toUpperCase());
		filter = Filter.AND(leftFilter, rightFilter);
		return filter;
	}

	/**
	 * get Service group statistics from foundation web service and set in to
	 * Statistics Bean.
	 */
	public void setServiceGroupStatistics() {
		if (!inStatusViewer
				&& !userExtendedRoleBean.getExtRoleServiceGroupList().isEmpty()) {
			// user does not have access to entire network
			String inadequatePermissionsMessage = ResourceUtils
					.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
					+ " [ Entire Network ] data.";
			handleSGInfo(inadequatePermissionsMessage);
			dashboardInfo = true;
			return;
		}

		List<StatisticsBean> networkServiceGroupCountList = Collections
				.synchronizedList(new ArrayList<StatisticsBean>());
		byte[] sgPieChartBytes = null;
		// filtered service group statistics map
		Map<String, Long> filteredServiceGroupStatisticsMap = null;
		/**
		 * service group statistics Map
		 */
		Map<String, Long> serviceGroupStatisticsMap = null;

		try {
			if (foundFacade == null) {
				LOGGER.debug("foundFacade is null on setServiceGroupStatistics method");
				throw new GWPortalException();
			}
			serviceGroupStatisticsMap = foundFacade
					.getEntireNetworkServiceGroupStatistics();
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String serviceGroupFilterName = stateController
					.getCurrentServiceFilter();
			// check if any filter applied.
			if (!Constant.EMPTY_STRING.equalsIgnoreCase(serviceGroupFilterName)) {
				FilterComputer filterComputerSG = new FilterComputer();
				Filter serviceGroupFilter = filterComputerSG
						.getServiceGroupFilter(serviceGroupFilterName);
				// getting filtered statistics
				filteredServiceGroupStatisticsMap = foundFacade
						.getGroupStatisticsForServicegGroup(
								StatisticQueryType.SERVICEGROUP_STATISTICS_BY_FILTER,
								serviceGroupFilter, null,
								Constant.NAGIOS.toUpperCase());
			}

			if (serviceGroupStatisticsMap != null
					&& !serviceGroupStatisticsMap.isEmpty()) {
				for (int i = 0; i < serviceStatusArray.length; i++) {
					StatisticsBean statisticsbean = new StatisticsBean();
					if (NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
							.getMonitorStatusName().equalsIgnoreCase(
									serviceStatusArray[i])) {
						statisticsbean
								.setStatus(Constant.CRITICAL_COLON_UN_SCHEDULED);
					} else if (NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
							.getMonitorStatusName().equalsIgnoreCase(
									serviceStatusArray[i])) {
						statisticsbean
								.setStatus(Constant.CRITICAL_COLON_SCHEDULED);
					} else {
						statisticsbean.setStatus(serviceStatusArray[i]);
					}

					statisticsbean.setTotal(serviceGroupStatisticsMap
							.get(serviceStatusArray[i].toUpperCase()));
					if (filteredServiceGroupStatisticsMap == null
							|| filteredServiceGroupStatisticsMap.isEmpty()) {
						statisticsbean.setFiltered(serviceGroupStatisticsMap
								.get(serviceStatusArray[i].toUpperCase())
								.intValue());
					} else {
						statisticsbean
								.setFiltered(filteredServiceGroupStatisticsMap
										.get(serviceStatusArray[i]
												.toUpperCase()).intValue());
					}
					status = NetworkObjectStatusEnum
							.getStatusEnumFromMonitorStatus(
									serviceStatusArray[i],
									NodeType.SERVICE_GROUP);
					if (status != null) {
						statisticsbean.setImgsrc(status.getIconPath());
					}
					networkServiceGroupCountList.add(statisticsbean);

					// nullify the statistics bean used
					statisticsbean = null;
				}
				// check if filter host group statistics is null or empty then
				// draw pie chart with total statistics
				if (filteredServiceGroupStatisticsMap == null
						|| filteredServiceGroupStatisticsMap.isEmpty()) {
					// check if total count is zero then display image with no
					// service groups available
					if (serviceGroupStatisticsMap.get(Constant.TOTAL) > 0) {
						sgPieChartBytes = getSGOrSPieChartBytes(serviceGroupStatisticsMap);
					}
				} else {
					if (filteredServiceGroupStatisticsMap.get(Constant.TOTAL) > 0) {
						sgPieChartBytes = getSGOrSPieChartBytes(filteredServiceGroupStatisticsMap);
					}
				}

				/**
				 * Faces context will be null on JMS or Non JSF thread. Perform
				 * a null check. Make increase the visibility of the statisbean
				 * to class level for the JMS thread.
				 */
				if (FacesContext.getCurrentInstance() != null) {
					statisbeanSG = (ServiceGroupStatistics) FacesUtils
							.getManagedBean(Constant.SERVICE_GROUP_STATISTICS);
				}
				if (statisbeanSG != null) {
					statisbeanSG
							.setServicesGroupsCountList(networkServiceGroupCountList);
					statisbeanSG.setServiceGroupPieChart(sgPieChartBytes);
					statisbeanSG
							.setPreviousServiceFilter(serviceGroupFilterName);
					statisbeanSG
							.setTotalServicesGroupsCount(serviceGroupStatisticsMap
									.get(Constant.TOTAL));
					if (filteredServiceGroupStatisticsMap == null
							|| filteredServiceGroupStatisticsMap.isEmpty()) {
						statisbeanSG
								.setFilteredServicesGroupsCount(serviceGroupStatisticsMap
										.get(Constant.TOTAL).intValue());

					} else {
						statisbeanSG
								.setFilteredServicesGroupsCount(filteredServiceGroupStatisticsMap
										.get(Constant.TOTAL).intValue());
					}

				}

			}

		} catch (WSDataUnavailableException e) {
			handleSGError(e.getMessage());

		} catch (GWPortalException e) {
			handleSGError(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Exception While Drawing Pie chart in setServiceGroupStatistics method ."
					+ e);
			handleSGError(new GWPortalException().getMessage());
		}
	}

	/**
	 * get service statistics from foundation facade and set to service
	 * statistics bean.
	 */
	public void setServiceStatistics() {
		if (!inStatusViewer) {
			if (!selectedNodeType.equals(NodeType.NETWORK)
					&& (!userExtendedRoleBean.getExtRoleHostGroupList()
							.isEmpty() || !userExtendedRoleBean
							.getExtRoleServiceGroupList().isEmpty())) {
				List<String> extRoleHostGroupList = userExtendedRoleBean
						.getExtRoleHostGroupList();
				if (extRoleHostGroupList
						.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
					String inadequatePermissionsMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
							+ " [ Entire Network ] data";
					handleInfo(inadequatePermissionsMessage);
					return;
				}

				if (selectedNodeType == NodeType.HOST_GROUP
						&& (selectedNodeName == null || selectedNodeName
								.equals("")))
					selectedNodeName = userExtendedRoleBean
							.getDefaultHostGroup();

				if (selectedNodeType == NodeType.SERVICE_GROUP
						&& (selectedNodeName == null || selectedNodeName
								.equals("")))
					selectedNodeName = userExtendedRoleBean
							.getDefaultServiceGroup();

			}

		}

		// re-initialize the bean so as to reload UI
		setServiceError(false);
		setServiceInfo(false);
		setServiceMessage(false);
		setMessage(false);
		List<StatisticsBean> networkServiceCountList = Collections
				.synchronizedList(new ArrayList<StatisticsBean>());
		byte[] serviceChartBytes = null;
		try {
			Map<String, Long> filteredServiceStatisticsMap = null;
			/**
			 * service statistics Map
			 */
			Map<String, Long> serviceStatisticsMap = null;

			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String serviceFilterName = stateController
					.getCurrentServiceFilter();
			// Arul: Commented the code temporarily.Offshore to check this
			// if (getServiceStatisticsMap() == null) {
			// getting total service statistics Map

			serviceStatisticsMap = getServiceStatisticsMapByNodeType();
			// }
			// check filter is selected or not
			if (serviceFilterName != null
					&& !Constant.EMPTY_STRING
							.equalsIgnoreCase(serviceFilterName)) {
				// getting Filtered Statistics Map.
				filteredServiceStatisticsMap = getFilteredServiceStatisticsMapByNodeType(serviceFilterName);
			}

			if (serviceStatisticsMap != null && !serviceStatisticsMap.isEmpty()) {
				for (int i = 0; i < serviceStatusArray.length; i++) {
					StatisticsBean statisticsbean = new StatisticsBean();
					if (NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
							.getMonitorStatusName().equalsIgnoreCase(
									serviceStatusArray[i])) {
						statisticsbean
								.setStatus(Constant.CRITICAL_COLON_UN_SCHEDULED);
					} else if (NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
							.getMonitorStatusName().equalsIgnoreCase(
									serviceStatusArray[i])) {
						statisticsbean
								.setStatus(Constant.CRITICAL_COLON_SCHEDULED);
					} else {
						statisticsbean.setStatus(serviceStatusArray[i]);
					}
					statisticsbean.setTotal(serviceStatisticsMap
							.get(serviceStatusArray[i].toUpperCase()));
					if (filteredServiceStatisticsMap == null
							|| filteredServiceStatisticsMap.isEmpty()) {
						statisticsbean
								.setFiltered(serviceStatisticsMap.get(
										serviceStatusArray[i].toUpperCase())
										.intValue());

					} else {

						statisticsbean.setFiltered(filteredServiceStatisticsMap
								.get(serviceStatusArray[i].toUpperCase())
								.intValue());

					}
					status = NetworkObjectStatusEnum
							.getStatusEnumFromMonitorStatus(
									serviceStatusArray[i], NodeType.SERVICE);
					statisticsbean.setImgsrc(status.getIconPath());
					networkServiceCountList.add(statisticsbean);

					// nullify the statistics bean used
					statisticsbean = null;
				}
				// check if filter service statistics is null or empty then
				// draw pie chart with total statistics
				if (filteredServiceStatisticsMap == null
						|| filteredServiceStatisticsMap.isEmpty()) {
					// jira GWMON-6274:- check if total count is greater then
					// zero then create pie
					// chart other wise Default image - 'no service available'.
					if (serviceStatisticsMap.get(Constant.TOTAL) > 0) {
						serviceChartBytes = getSGOrSPieChartBytes(serviceStatisticsMap);
					}
				} else {
					// jira GWMON-6274:- check if total count is greater then
					// zero then create pie
					// chart other wise Default image - 'no service available'.
					if (filteredServiceStatisticsMap.get(Constant.TOTAL) > 0) {
						serviceChartBytes = getSGOrSPieChartBytes(filteredServiceStatisticsMap);
					}
				}

				/**
				 * Faces context will be null on JMS or Non JSF thread. Perform
				 * a null check. Make increase the visibility of the statisbean
				 * to class level for the JMS thread.
				 */
				if (FacesContext.getCurrentInstance() != null) {
					statisbeanSS = (ServiceStatisticsBean) FacesUtils
							.getManagedBean(Constant.SERVICE_STATISTICS_BEAN);
				}

				if (statisbeanSS != null) {
					statisbeanSS.setServiceCountList(networkServiceCountList);
					statisbeanSS.setTotalServiceCount(serviceStatisticsMap
							.get(Constant.TOTAL));
					statisbeanSS.setServicePieChart(serviceChartBytes);
					statisbeanSS.setPreviousServiceFilter(serviceFilterName);
					if (filteredServiceStatisticsMap == null
							|| filteredServiceStatisticsMap.isEmpty()) {
						statisbeanSS
								.setFilteredServiceCount(serviceStatisticsMap
										.get(Constant.TOTAL).intValue());
					} else {
						statisbeanSS
								.setFilteredServiceCount(filteredServiceStatisticsMap
										.get(Constant.TOTAL).intValue());
					}
				}
			}
		} catch (WSDataUnavailableException e) {
			handleServiceError(e.getMessage());

		} catch (GWPortalException e) {
			handleServiceError(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Exception While Drawing Pie chart in setServiceStatistics method ."
					+ e);
			handleServiceError(new GWPortalException().getMessage());
		}

	}

	/**
	 * return Service statistics Map depending on current node type is selected.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private Map<String, Long> getServiceStatisticsMapByNodeType()
			throws GWPortalException, WSDataUnavailableException {

		// if selected node type is still null, then return from here.
		if (null == selectedNodeType) {
			return null;
		}

		Map<String, Long> serviceStatistics = null;

		switch (selectedNodeType) {
		case NETWORK:
			if ((!userExtendedRoleBean.getExtRoleHostGroupList().isEmpty() || !userExtendedRoleBean
					.getExtRoleServiceGroupList().isEmpty()))
				serviceStatistics = referenceTreeModel
						.getAllowedServiceStatistics();
			else
				serviceStatistics = foundFacade
						.getEntireNetworkServiceStatistics();
			break;

		case HOST_GROUP:
			// serviceStatistics = foundFacade
			// .getServiceStatisticsByHostGroupId(String
			// .valueOf(selectedNodeId));
			serviceStatistics = foundFacade
					.getServiceStatisticsByHostGroupName(selectedNodeName);
			break;

		case HOST:
			serviceStatistics = foundFacade
					.getServiceStatisticsByHostName(selectedNodeName);
			break;

		case SERVICE_GROUP:
			try {
				serviceStatistics = foundFacade
						.getServiceStatisticsByServiceGroupName(selectedNodeName);
			} catch (WSDataUnavailableException e) {
				String serviceGroupNotAvailableErrorMessage = ResourceUtils
						.getLocalizedMessage("com_groundwork_portal_statusviewer_serviceGroupUnavailable")
						+ " [" + selectedNodeName + "]";
				LOGGER.error(serviceGroupNotAvailableErrorMessage);
				handleInfo(serviceGroupNotAvailableErrorMessage);
			}
			break;

		default:
			break;

		}
		return serviceStatistics;
	}

	/**
	 * return Filtered Service statistics Map depending on current node type is
	 * selected.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private Map<String, Long> getFilteredServiceStatisticsMapByNodeType(
			String serviceFilterName) throws GWPortalException,
			WSDataUnavailableException {
		Map<String, Long> filteredServiceStatistics = null;
		FilterComputer filterComputer = new FilterComputer();
		// StatisticsHandler get current selected Service filter
		Filter serviceFilter = filterComputer
				.getServiceFilter(serviceFilterName);
		switch (selectedNodeType) {
		case NETWORK:

			filteredServiceStatistics = foundFacade
					.getFilteredServiceStatistics(
							StatisticQueryType.SERVICE_STATISTICS_BY_FILTER,
							serviceFilter, null, Constant.NAGIOS.toUpperCase());
			break;
		case HOST_GROUP:
			filteredServiceStatistics = getFilteredServiceStatisticsbyHostGroupName(
					selectedNodeName, serviceFilter);
			break;
		case HOST:
			filteredServiceStatistics = getFilteredServiceStatisticsbyHostName(
					selectedNodeName, serviceFilter);
			break;
		case SERVICE_GROUP:
			filteredServiceStatistics = foundFacade
					.getFilteredServiceStatistics(
							StatisticQueryType.SERVICE_STATISTICS_BY_FILTER,
							serviceFilter, selectedNodeName,
							Constant.NAGIOS.toUpperCase());
			break;
		default:
			break;

		}
		return filteredServiceStatistics;
	}

	/**
	 * return ServiceStatus array for host group NAme by filter
	 * 
	 * @param hostGroupName
	 * @return ServiceStatus Array
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private SimpleServiceStatus[] getServicesByHostGroupName(
			String hostGroupName, Filter serviceFilter)
			throws GWPortalException, WSDataUnavailableException {
		SimpleServiceStatus[] serviceStatusArr = null;
		Filter leftfilter = new Filter(
				FilterConstants.SERVICES_BY_HOST_GROUP_NAME_STRING_PROPERTY,
				FilterOperator.EQ, hostGroupName);

		Filter filter = Filter.AND(leftfilter, serviceFilter);
		serviceStatusArr = foundFacade.getSimpleServicesbyCriteria(filter,
				null, -1, -1);
		return serviceStatusArr;
	}

	/**
	 * return filtered service statistics by host Group ID.
	 * 
	 * 
	 * @param hostGroupName
	 * @return Map
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private Map<String, Long> getFilteredServiceStatisticsbyHostGroupName(
			String hostGroupName, Filter serviceFilter)
			throws GWPortalException, WSDataUnavailableException {
		String commaSepServiceIdString = null;
		Map<String, Long> serviceStatistics = new HashMap<String, Long>();
		// ServiceStatus[] servicesArr = getServicesByHostGroupId(hostGroupId,
		// serviceFilter);

		SimpleServiceStatus[] servicesArr = getServicesByHostGroupName(
				hostGroupName, serviceFilter);
		commaSepServiceIdString = getCommaSepServiceIdString(servicesArr);
		if (commaSepServiceIdString != null
				&& !Constant.EMPTY_STRING
						.equalsIgnoreCase(commaSepServiceIdString)) {
			serviceStatistics = foundFacade
					.getServiceStatisticsByServiceIds(commaSepServiceIdString);
		} else {
			// if ServiceStatus array is null hence no service available .
			for (int i = 0; i < serviceStatusArray.length; i++) {
				serviceStatistics.put(serviceStatusArray[i].toUpperCase(),
						(long) Constant.ZERO);
			}
			serviceStatistics.put(Constant.TOTAL, (long) Constant.ZERO);

		}

		return serviceStatistics;
	}

	/**
	 * return filtered service statistics by host Name.
	 * 
	 * 
	 * @param hostGroupId
	 * @return Map
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	private Map<String, Long> getFilteredServiceStatisticsbyHostName(
			String hostName, Filter serviceFilter) throws GWPortalException,
			WSDataUnavailableException {
		String commaSepServiceIdString = null;
		Map<String, Long> serviceStatistics = new HashMap<String, Long>();
		Filter leftfilter = new Filter(HOST_HOST_NAME_STRING_PROPERTY,
				FilterOperator.EQ, hostName);

		Filter filter = Filter.AND(leftfilter, serviceFilter);
		SimpleServiceStatus[] servicesArr = foundFacade
				.getSimpleServicesbyCriteria(filter, null, -1, -1);
		commaSepServiceIdString = getCommaSepServiceIdString(servicesArr);
		if (commaSepServiceIdString != null
				&& !Constant.EMPTY_STRING.equals(commaSepServiceIdString)) {
			serviceStatistics = foundFacade
					.getServiceStatisticsByServiceIds(commaSepServiceIdString);
		} else {
			// if ServiceStatus array is null hence no service available .
			for (int i = 0; i < serviceStatusArray.length; i++) {
				serviceStatistics.put(serviceStatusArray[i].toUpperCase(),
						(long) Constant.ZERO);
			}
			serviceStatistics.put(Constant.TOTAL, (long) Constant.ZERO);

		}

		return serviceStatistics;
	}

	/**
	 * getting data from foundation web service and set in to Host statistics
	 * Bean.
	 */
	public void setHostStatistics() {
		boolean showEntireNetworkData = false;
		List<String> extRoleHostGroupList = userExtendedRoleBean
				.getExtRoleHostGroupList();
		if (!inStatusViewer && selectedNodeType.equals(NodeType.NETWORK)) {

			if (extRoleHostGroupList
					.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
				String inadequatePermissionsMessage = ResourceUtils
						.getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
						+ " [ Host Groups ] data";
				handleInfo(inadequatePermissionsMessage);
				return;
			}

			if (extRoleHostGroupList.isEmpty()
					|| selectedNodeType.equals(NodeType.NETWORK)) {
				showEntireNetworkData = true;
				// user has access to Entire Network
				// so do nothing
			}
			// user does not have access to entire network
			// String inadequatePermissionsMessage = ResourceUtils
			// .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
			// + " [ Entire Network ] data.";
			// handleInfo(inadequatePermissionsMessage);
			// dashboardInfo = true;
			// return;
		}

		if (!inStatusViewer && !showEntireNetworkData) {
			selectedNodeType = NodeType.HOST_GROUP;
			if (null == selectedNodeName
					|| selectedNodeName.equals(Constant.EMPTY_STRING)) {
				selectedNodeName = userExtendedRoleBean.getDefaultHostGroup();
			}
		}
		// re-initialize the bean so as to reload UI
		setHostError(false);
		setHostInfo(false);
		setHostMessage(false);
		setMessage(false);
		/*
		 * filtered host statistics map contain monitor status as key and
		 * filtered statistics as value
		 */
		Map<String, Long> filteredHostStatisticsMap = null;
		List<StatisticsBean> hostCountlist = Collections
				.synchronizedList(new ArrayList<StatisticsBean>());
		byte[] hostChartBytes = null;
		/**
		 * host statistics map contain monitor status as key and statistics as
		 * value Catch host statistics for future use with in session.
		 */
		Map<String, Long> hostStatisticsMap = null;

		// get current selected node type
		try {
			if (foundFacade == null) {
				LOGGER.debug("foundFacade is null in setHostStatistics(),This time not able to call foundation web service ");
				throw new GWPortalException();
			}
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			// get the current selected host filter name
			String hostFilterName = stateController.getCurrentHostFilter();

			if (selectedNodeType.equals(NodeType.NETWORK)) {
				if (extRoleHostGroupList.isEmpty())
					hostStatisticsMap = foundFacade
							.getEntireNetworkHostStatistics();
				else {
					StringBuilder authHostGroupsBuilder = new StringBuilder();
					for (String authorizedHostGroup : extRoleHostGroupList) {
						authHostGroupsBuilder.append(authorizedHostGroup);
						authHostGroupsBuilder.append(",");
					} // end for
					String authHostGroups = authHostGroupsBuilder.substring(0,
							authHostGroupsBuilder.length() - 1);
					Filter hgFilter = new Filter("name", FilterOperator.IN,
							authHostGroups);
					hostStatisticsMap = foundFacade
							.getGroupStatisticsForHostGroup(
									StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER,
									hgFilter, null, null);

				} // end if else

				if (!Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
					filteredHostStatisticsMap = getFilteredHostStatistics(hostFilterName);
				}
			} else if (selectedNodeType.equals(NodeType.HOST_GROUP)) {
				try {
					hostStatisticsMap = foundFacade
							.getHostStatisticsForHostGroupByHostgroupName(selectedNodeName);
				} catch (WSDataUnavailableException e) {
					String hostGroupNotAvailableErrorMessage = ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
							+ " [" + selectedNodeName + "]";
					LOGGER.error(hostGroupNotAvailableErrorMessage);
					handleInfo(hostGroupNotAvailableErrorMessage);
					return;
				}
				if (!Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
					filteredHostStatisticsMap = getFilteredHostStatisticsUnderHostGroup(
							hostFilterName, selectedNodeName);
				}
			}
			if (hostStatisticsMap != null && !hostStatisticsMap.isEmpty()) {
				for (int i = 0; i < hostStatusArray.length; i++) {
					StatisticsBean statisticsbean = new StatisticsBean();
					status = NetworkObjectStatusEnum
							.getStatusEnumFromMonitorStatus(hostStatusArray[i],
									NodeType.HOST);
					if (hostStatusArray[i]
							.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
									.getStatus())) {
						statisticsbean
								.setStatus(Constant.DOWN_COLON_UN_SCHEDULED);
					} else if (hostStatusArray[i]
							.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
									.getStatus())) {
						statisticsbean.setStatus(Constant.DOWN_COLON_SCHEDULED);
					} else {
						statisticsbean.setStatus(hostStatusArray[i]);
					}
					statisticsbean.setTotal(hostStatisticsMap
							.get(hostStatusArray[i].toUpperCase()));
					// if filteredHostStatisticsMap is null hence filter if not
					// applied .
					if (filteredHostStatisticsMap == null
							|| filteredHostStatisticsMap.isEmpty()) {
						statisticsbean.setFiltered(hostStatisticsMap.get(
								hostStatusArray[i].toUpperCase()).intValue());
					} else {
						statisticsbean.setFiltered(filteredHostStatisticsMap
								.get(hostStatusArray[i].toUpperCase())
								.intValue());
					}
					statisticsbean.setImgsrc(status.getIconPath());
					hostCountlist.add(statisticsbean);
					// nullify the statistics bean used
					statisticsbean = null;
				}
				// check if filter host group statistics is null or empty then
				// draw pie chart with total statistics
				if (filteredHostStatisticsMap == null
						|| filteredHostStatisticsMap.isEmpty()) {
					// jira GWMON-6274:- check if total count is greater then
					// zero then create pie chart other wise Default image - 'no
					// host available'.
					if (hostStatisticsMap.get(Constant.TOTAL) > 0) {
						hostChartBytes = getHGOrHPieChartBytes(hostStatisticsMap);
					}
				} else {
					if (filteredHostStatisticsMap.get(Constant.TOTAL) > 0) {
						hostChartBytes = getHGOrHPieChartBytes(filteredHostStatisticsMap);
					}
				}
				/**
				 * Faces context will be null on JMS or Non JSF thread. Perform
				 * a null check. Make increase the visibility of the statisbean
				 * to class level for the JMS thread.
				 */
				if (FacesContext.getCurrentInstance() != null) {
					statishostbeanHS = (HostStatisticsBean) FacesUtils
							.getManagedBean(Constant.HOST_STATISTICS_BEAN);
				}

				if (statishostbeanHS != null) {
					statishostbeanHS.setHostCountList(hostCountlist);
					statishostbeanHS.setTotalHostCount(hostStatisticsMap
							.get(Constant.TOTAL));
					statishostbeanHS.setHostPieChart(hostChartBytes);
					statishostbeanHS
							.setPreviousSelectedHostFilter(hostFilterName);
					// check if filtered host map is null
					if (filteredHostStatisticsMap == null
							|| hostStatisticsMap.isEmpty()) {
						statishostbeanHS.setFilteredHostCount(hostStatisticsMap
								.get(Constant.TOTAL).intValue());
					} else {
						statishostbeanHS
								.setFilteredHostCount(filteredHostStatisticsMap
										.get(Constant.TOTAL).intValue());
					}
				} // end if
			} else {
				LOGGER.debug("hostStatisticsMap is null or empty");
				throw new GWPortalException();
			}
		} catch (WSDataUnavailableException e) {
			handleHostError(e.getMessage());
		} catch (GWPortalException e) {
			handleHostError(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Exception While Drawing Pie chart in setHostStatistics method ."
					+ e);
			handleHostError(new GWPortalException().getMessage());
		}
	}

	/**
	 * return filtered host statistics map for particular host group
	 * 
	 * @param hostFilterName
	 * @param nodeName
	 * @return map
	 * @throws WSDataUnavailableException
	 */
	private Map<String, Long> getFilteredHostStatisticsUnderHostGroup(
			String hostFilterName, String nodeName)
			throws WSDataUnavailableException {
		Map<String, Long> filteredHostStatisticsMap;
		FilterComputer filterComputer = new FilterComputer();
		Filter hostFilter = filterComputer.getHostFilter(hostFilterName);
		// get filtered count statistics
		filteredHostStatisticsMap = foundFacade.getGroupStatisticsForHostGroup(
				StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER, hostFilter,
				nodeName, Constant.NAGIOS.toUpperCase());
		return filteredHostStatisticsMap;
	}

	/**
	 * @param hostFilterName
	 * @return Map
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private Map<String, Long> getFilteredHostStatistics(String hostFilterName)
			throws GWPortalException, WSDataUnavailableException {
		Map<String, Long> filteredHostStatisticsMap = new HashMap<String, Long>();

		FilterComputer filterComputer = new FilterComputer();
		// get current selected filter
		Filter filter = filterComputer.getHostFilter(hostFilterName);
		if (foundFacade == null) {
			LOGGER.debug("foundFacade is null in getFilteredHostStatistics method,This time not able to call foundation web service ");
			throw new GWPortalException();
		}

		// get host array depending on filter
		WSFoundationCollection wsFoundHostArr = foundFacade
				.getSimpleHostsbyCriteria(filter, null, -1, -1, false);
		// get comma separated host name list
		String hostName = getCommaSepHostName(wsFoundHostArr.getSimpleHost());
		// if host is not null then call web service to get the filtered
		// statistics other wise add all filter count as zero.
		if (hostName != null) {
			filteredHostStatisticsMap = foundFacade
					.getFilteredStatisticsForHost(hostName);
		} else {
			for (int i = 0; i < hostStatusArray.length; i++) {
				filteredHostStatisticsMap.put(hostStatusArray[i].toUpperCase(),
						(long) 0);
			}
			// filtered total count
			filteredHostStatisticsMap.put(Constant.TOTAL, (long) 0);

		}

		return filteredHostStatisticsMap;
	}

	/**
	 * return comma separated Host name String
	 * 
	 * @param simpleHosts
	 * @return String
	 */
	private String getCommaSepHostName(SimpleHost[] simpleHosts) {
		String hostnames = null;
		// create String for service status ID
		StringBuffer hostNameBuilder = new StringBuffer(Constant.EMPTY_STRING);
		if (null != simpleHosts && simpleHosts.length > 0
				&& simpleHosts[0] != null) {
			// creating comma Separated service Status ID String
			for (SimpleHost host : simpleHosts) {
				hostNameBuilder.append(host.getName());
				hostNameBuilder.append(Constant.COMMA);

			}
			int lastcommaindex = hostNameBuilder.lastIndexOf(Constant.COMMA);
			// remove comma at last
			hostnames = hostNameBuilder.substring(0, lastcommaindex);
		}

		return hostnames;

	}

	/**
	 * show host group list in pop up window.
	 * 
	 * @param event
	 */
	public void showHostGroups(ActionEvent event) {
		// get HostGroupStatisticsBean instance from current context
		HostGroupStatisticsBean hostGroupStatisBean = (HostGroupStatisticsBean) FacesUtils
				.getManagedBean(Constant.HOST_GROUP_STATISTICS_BEAN);
		try {
			if (hostGroupStatisBean == null) {
				LOGGER.debug("hostGroupStatisBean is null in showHostGroups method");
				throw new GWPortalException();
			}

			// get the monitor status request parameter
			hostGroupPopUpStatus = FacesUtils
					.getRequestParameter(Constant.HOSTGROUP_STATUS);
			// map UI Status name with actual monitor status name
			if (Constant.DOWN_COLON_UN_SCHEDULED
					.equalsIgnoreCase(hostGroupPopUpStatus)) {
				hostGroupPopUpStatus = NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
						.getMonitorStatusName();
			}
			if (Constant.DOWN_COLON_SCHEDULED
					.equalsIgnoreCase(hostGroupPopUpStatus)) {
				hostGroupPopUpStatus = NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
						.getMonitorStatusName();
			}

			// create filter
			Filter leftFilter = getHostORHostGroupFilter(
					Constant.HOSTGROUP_MONITORSTATUS_NAME, hostGroupPopUpStatus);
			Filter rightFilter = null;
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			// get the current selected host group filter name
			String hostGrpFilterName = stateController.getCurrentHostFilter();
			// check if stateController return empty string means no filter
			// should be applied on pop up window.
			if (!Constant.EMPTY_STRING.equalsIgnoreCase(hostGrpFilterName)) {
				FilterComputer filterComputer = new FilterComputer();
				rightFilter = filterComputer
						.getHostGroupFilter(hostGrpFilterName);

			}
			// get host group list.
			filteredHostGroupList = getHostGroupPopUpDataList(leftFilter,
					rightFilter);
			hostGroupStatisBean.setHostGroupList(filteredHostGroupList);
			hostGroupStatisBean.setRowCount(filteredHostGroupList.size());
			String hostGroupSubTitle = ResourceUtils
					.getLocalizedMessage(Constant.HOST_GROUP_GROUP_SUB_TITLE)
					+ Constant.SPACE_COLON_SPACE;
			hostGroupStatisBean
					.setCurrentPopstatus(getHostOrHostGroupPopUpTitle(
							hostGrpFilterName, hostGroupPopUpStatus,
							hostGroupSubTitle));
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			if (popUpSelectBean != null) {
				popUpSelectBean.setHgSelectValue(Constant.FILTEREDHOSTGROUPS);
			}
		} catch (GWPortalException e) {
			handleError(e.getMessage());
		} catch (WSDataUnavailableException e) {
			handleError(e.getMessage());
		}

	}

	/**
	 * return model popup Title for Host or Host Group Status portlet.
	 * 
	 * @param filterName
	 * @return String
	 */
	private String getHostOrHostGroupPopUpTitle(String filterName,
			String currentStatus, String portletTitle) {
		String popUpStatus;
		String title = Constant.EMPTY_STRING;
		String filterTitle;
		if (currentStatus
				.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
						.getMonitorStatusName())) {
			popUpStatus = NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
					.getStatus();
		} else if (currentStatus
				.equalsIgnoreCase(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
						.getMonitorStatusName())) {
			popUpStatus = NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
					.getStatus();
		} else {
			popUpStatus = currentStatus;
		}
		String subTitle = portletTitle + popUpStatus;

		if (Constant.EMPTY_STRING.equalsIgnoreCase(filterName)) {

			title = subTitle;

		} else {
			int subtitleLength = subTitle.length();
			int remFilterLength = Constant.FIFTY - subtitleLength - 2;
			if (remFilterLength < filterName.length()) {
				filterTitle = filterName.substring(0, remFilterLength);
				title = subTitle + Constant.OPEN_PARENTHESES + filterTitle
						+ Constant.DOTS + Constant.CLOSED_PARENTHESES;
			} else {
				filterTitle = filterName;
				title = subTitle + Constant.OPEN_PARENTHESES + filterTitle
						+ Constant.CLOSED_PARENTHESES;
			}

		}

		return title;
	}

	/**
	 * return model popup Title for Service or Service Group Status portlet.
	 * 
	 * @param filterName
	 * @return String
	 */
	private String getServiceOrServiceGroupPopUpTitle(String filterName,
			String currentStatus, String portletTitle) {
		String popUpStatus;
		String title = Constant.EMPTY_STRING;
		String filterTitle;
		if (NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
				.getMonitorStatusName().equalsIgnoreCase(currentStatus)) {
			popUpStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
					.getStatus();
		} else if (NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
				.getMonitorStatusName().equalsIgnoreCase(currentStatus)) {
			popUpStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
					.getStatus();
		} else {
			popUpStatus = currentStatus;
		}
		String subTitle = portletTitle + popUpStatus;

		if (Constant.EMPTY_STRING.equalsIgnoreCase(filterName)) {

			title = subTitle;

		} else {
			int subtitleLength = subTitle.length();
			int remFilterLength = Constant.FIFTY - subtitleLength
					- Constant.TWO;
			if (remFilterLength < filterName.length()) {
				filterTitle = filterName.substring(0, remFilterLength);
				title = subTitle + Constant.OPEN_PARENTHESES + filterTitle
						+ Constant.DOTS + Constant.CLOSED_PARENTHESES;
			} else {
				filterTitle = filterName;
				title = subTitle + Constant.OPEN_PARENTHESES + filterTitle
						+ Constant.CLOSED_PARENTHESES;
			}

		}

		return title;
	}

	/**
	 * 
	 * This method provide the list of ModelPopUpDataBean for host group.
	 * 
	 * @param leftFilter
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private List<ModelPopUpDataBean> getHostGroupPopUpDataList(
			Filter leftFilter, Filter rightFilter) throws GWPortalException,
			WSDataUnavailableException {

		List<ModelPopUpDataBean> hostGroupList = Collections
				.synchronizedList(new ArrayList<ModelPopUpDataBean>());
		Map<String, Integer> hostGroupMap = Collections
				.synchronizedMap(new HashMap<String, Integer>());
		// call web service to get host group array

		if (foundFacade == null) {
			LOGGER.debug("foundFacade is null in setHostGroupStatistics method  ");
			throw new GWPortalException();
		}
		HostGroup[] hostGrouparr = foundFacade.getHostGroupsbyCriteria(
				leftFilter, null, -1, -1, false);
		if (hostGrouparr != null) {
			for (int i = 0; i < hostGrouparr.length; i++) {
				if (hostGrouparr[i].getName() != null) {
					hostGroupMap.put(hostGrouparr[i].getName().toLowerCase(),
							hostGrouparr[i].getHostGroupID());
				}
			}
			// check is right is null then no filter applied in filter portlet.
			if (rightFilter == null) {
				// create host group list
				for (int i = 0; i < hostGrouparr.length; i++) {
					ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
					modelpopupdatabean.setName(hostGrouparr[i].getName());
					modelpopupdatabean.setSubPageURL(NodeURLBuilder
							.buildNodeURL(NodeType.HOST_GROUP,
									hostGrouparr[i].getHostGroupID(),
									hostGrouparr[i].getName()));
					hostGroupList.add(modelpopupdatabean);
				}
			} else {
				StateStatistics[] filteredHostGroupName = foundFacade
						.getFilteredHostGroupName(
								StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER,
								rightFilter, null,
								Constant.NAGIOS.toUpperCase());
				if (filteredHostGroupName != null) {
					for (int i = 0; i < filteredHostGroupName.length; i++) {
						if (filteredHostGroupName[i].getName() != null) {
							if (hostGroupMap
									.containsKey(filteredHostGroupName[i]
											.getName().toLowerCase())) {

								ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
								modelpopupdatabean
										.setName(filteredHostGroupName[i]
												.getName());
								modelpopupdatabean
										.setSubPageURL(NodeURLBuilder
												.buildNodeURL(
														NodeType.HOST_GROUP,
														hostGroupMap
																.get(filteredHostGroupName[i]
																		.getName()
																		.toLowerCase()),
														filteredHostGroupName[i]
																.getName()));
								hostGroupList.add(modelpopupdatabean);

							}
						}
					}

				}

			}
		}
		return hostGroupList;
	}

	/**
	 * show service group list in pop up window.
	 * 
	 * @param event
	 * 
	 * 
	 */
	public void showServiceGroupsPopUp(ActionEvent event) {

		serviceGroupCurrentStatus = FacesUtils
				.getRequestParameter(Constant.SERVICEGRPSTATUS_PARAMETER);

		ServiceGroupStatistics statisbean = (ServiceGroupStatistics) FacesUtils
				.getManagedBean(Constant.SERVICE_GROUP_STATISTICS);
		try {
			if (statisbean == null) {
				LOGGER.debug("ServiceGroupStatistics is null in showServiceGroupsPopUp method");
				throw new GWPortalException();
			}
			if (serviceGroupCurrentStatus != null) {
				if (Constant.CRITICAL_COLON_SCHEDULED
						.equalsIgnoreCase(serviceGroupCurrentStatus)) {
					serviceGroupCurrentStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
							.getStatus();
				}
				if (Constant.CRITICAL_COLON_UN_SCHEDULED
						.equalsIgnoreCase(serviceGroupCurrentStatus)) {
					serviceGroupCurrentStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
							.getStatus();
				}
			} else {
				LOGGER.debug("current Service Group pop up Model status is null ");
				serviceGroupCurrentStatus = Constant.OK.toUpperCase();
			}

			// getting service Group filter
			Filter filter = getServiceGroupFilter(serviceGroupCurrentStatus,
					true);
			if (filter != null) {
				// getting filtered Service Group LIst
				filteredServiceGroupList = getServiceGroupFilteredDataLIst(filter);
			} else {
				// Assign empty list
				filteredServiceGroupList = Collections
						.synchronizedList(new ArrayList<ModelPopUpDataBean>());
			}

			statisbean.setServiceGroupRowCount(filteredServiceGroupList.size());
			statisbean.setServicesGroupsList(filteredServiceGroupList);

			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String currentServiceFilter = stateController
					.getCurrentServiceFilter();
			String subTitle = ResourceUtils
					.getLocalizedMessage(Constant.SERVICE_GROUP_SUB_TITLE)
					+ Constant.SPACE_COLON_SPACE;
			statisbean.setCurrentPopstatus(getServiceOrServiceGroupPopUpTitle(
					currentServiceFilter, serviceGroupCurrentStatus, subTitle));
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			if (popUpSelectBean != null) {
				popUpSelectBean.setSgSelectValue(Constant.FILTEREDSERVICEGROUP);
			}

		} catch (WSDataUnavailableException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in showServiceGroupsPopUp method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}

	}

	/**
	 * @param filter
	 * @return
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private List<ModelPopUpDataBean> getServiceGroupFilteredDataLIst(
			Filter filter) throws WSDataUnavailableException, GWPortalException {
		List<ModelPopUpDataBean> serviceGroupList = Collections
				.synchronizedList(new ArrayList<ModelPopUpDataBean>());

		// getting service group
		Category[] category = foundFacade.getCategory(filter, -1, -1, null,
				false, false);

		if (category != null) {
			for (int i = 0; i < category.length; i++) {
				ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
				modelpopupdatabean.setName(category[i].getName());
				modelpopupdatabean.setSubPageURL(NodeURLBuilder.buildNodeURL(
						NodeType.SERVICE_GROUP, category[i].getCategoryId(),
						category[i].getName()));
				serviceGroupList.add(modelpopupdatabean);
			}
		}
		return serviceGroupList;
	}

	/**
	 * get service Group filter for Service Group name depending monitor status.
	 * 
	 * @param currentStatus
	 * @return Filter
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */

	private Filter getServiceGroupFilter(String currentStatus,
			boolean isFilteredSG) throws GWPortalException,
			WSDataUnavailableException {

		// Create filter to get all service status ID
		Filter serviceFilter = new Filter(FilterConstants.MONITOR_STATUS_NAME,
				FilterOperator.EQ, currentStatus.toUpperCase());
		Filter serviceGFilter;
		if (isFilteredSG) {
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String currentServiceFilterKey = stateController
					.getCurrentServiceFilter();
			if (Constant.EMPTY_STRING.equalsIgnoreCase(currentServiceFilterKey)) {
				serviceGFilter = serviceFilter;
			} else {
				FilterComputer filterComputer = new FilterComputer();
				Filter currentserviceFilter = filterComputer
						.getServiceFilter(currentServiceFilterKey);
				serviceGFilter = Filter
						.AND(serviceFilter, currentserviceFilter);
			}
		} else {
			serviceGFilter = serviceFilter;
		}

		// get service status array from foundation web service.
		// get service status array from foundation web service.
		SimpleServiceStatus[] serviceStatusArr = foundFacade
				.getSimpleServicesbyCriteria(serviceGFilter, null, -1, -1);
		String serviceStatusIds = getCommaSepServiceIdString(serviceStatusArr);
		// check if serviceStatusIds is empty
		if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceStatusIds.trim())) {
			return null;

		}
		// create the filter to get service group.
		Filter serviceGroupCategoryidFilter = new Filter(
				FilterConstants.CATEGORY_ENTITIES_OBJECT_I_D,
				FilterOperator.IN, serviceStatusIds);
		Filter serviceGroupCategoryNameFilter = new Filter(
				FilterConstants.ENTITY_TYPE_NAME, FilterOperator.EQ,
				FilterConstants.SERVICE_GROUP);
		Filter filter = Filter.AND(serviceGroupCategoryidFilter,
				serviceGroupCategoryNameFilter);

		return filter;
	}

	/**
	 * return Comma separated Service ID String.
	 * 
	 * @param serviceStatusArr
	 * @return String
	 */
	private String getCommaSepServiceIdString(
			SimpleServiceStatus[] serviceStatusArr) {
		// create String for service status ID
		StringBuffer serviceStatusIdBuilder = new StringBuffer(
				Constant.EMPTY_STRING);
		String serviceStatusIds = Constant.EMPTY_STRING;
		if (serviceStatusArr != null && serviceStatusArr.length > 0
				&& serviceStatusArr[0] != null) {
			// creating comma Separated service Status ID String
			for (int i = 0; i < serviceStatusArr.length; i++) {

				int servicestatusid = serviceStatusArr[i].getServiceStatusID();
				serviceStatusIdBuilder.append(servicestatusid);
				serviceStatusIdBuilder.append(Constant.COMMA);

			}
			int lastcommaindex = serviceStatusIdBuilder
					.lastIndexOf(Constant.COMMA);

			// remove last comma
			if (lastcommaindex > 0) {
				serviceStatusIds = serviceStatusIdBuilder.substring(0,
						lastcommaindex);
			}
		}

		return serviceStatusIds;
	}

	/**
	 * get the Host list from foundation layer and display in model pop up for
	 * host
	 * 
	 * @param event
	 */
	public void showHostsPopUp(ActionEvent event) {
		Filter filter = null;
		hostPopUpStatus = FacesUtils.getRequestParameter(Constant.HOSTSTATUS);
		// map UI Status name with actual monitor status name
		if (Constant.DOWN_COLON_UN_SCHEDULED.equalsIgnoreCase(hostPopUpStatus)) {
			hostPopUpStatus = NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
					.getMonitorStatusName();
		}
		if (Constant.DOWN_COLON_SCHEDULED.equalsIgnoreCase(hostPopUpStatus)) {
			hostPopUpStatus = NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
					.getMonitorStatusName();
		}
		// check if hostPopUpStatus is up then set hostAcknowledgedRender as
		// false
		if (NetworkObjectStatusEnum.HOST_UP.getStatus().equalsIgnoreCase(
				hostPopUpStatus)
				|| NetworkObjectStatusEnum.HOST_PENDING.getStatus()
						.equalsIgnoreCase(hostPopUpStatus)) {
			hostAcknowledgedRender = false;
		} else {
			hostAcknowledgedRender = true;
		}
		// check if hostPopUpStatus is up then set hostAcknowledgedRender as
		// false
		if (NetworkObjectStatusEnum.HOST_PENDING.getStatus().equalsIgnoreCase(
				hostPopUpStatus)) {
			hostDateTimeRender = false;
		} else {
			hostDateTimeRender = true;
		}

		try {
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			// get the current selected host filter name
			String hostFilterName = stateController.getCurrentHostFilter();

			filter = getHostFilterbyNodeType(hostFilterName, selectedNodeType,
					selectedNodeName);

			HostStatisticsBean statisbean = (HostStatisticsBean) FacesUtils
					.getManagedBean(Constant.HOST_STATISTICS_BEAN);

			filter = buildAuthorizedHostsFilter(selectedNodeType, filter,
					hostFilterName, null);

			// on demand model pop up pagination
			setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
					statisbean.getPopupRowSize(), filter, hostPopUpStatus,
					NodeType.HOST, "hostName"));

			// pop up title String for down unscheduled and scheduled
			String hostSubTitle = ResourceUtils
					.getLocalizedMessage(Constant.HOST_SUB_TITLE)
					+ Constant.SPACE_COLON_SPACE;
			statisbean.setCurrentPopstatus(getHostOrHostGroupPopUpTitle(
					hostFilterName, hostPopUpStatus, hostSubTitle));
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			if (popUpSelectBean == null) {
				LOGGER.debug("popUpSelectBean is null in showHostsPopUp method");
				throw new GWPortalException();
			}
			popUpSelectBean.setHostSelectValue(Constant.FILTEREDHOST);
		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in showHostsPopUp method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}

	}

	/**
	 * Helper for building the filter
	 */
	private Filter buildAuthorizedHostsFilter(NodeType selectedNodeType,
			Filter filter, String hostFilterName, String hostSelectValue) {
		if (selectedNodeType.equals(NodeType.NETWORK)) {
			List<String> extRoleHostGroupList = userExtendedRoleBean
					.getExtRoleHostGroupList();
			if (extRoleHostGroupList.isEmpty())
				filter = getHostFilterbyNodeType(hostFilterName,
						selectedNodeType, selectedNodeName);
			else {
				StringBuilder authHostGroupsBuilder = new StringBuilder();
				for (String authorizedHostGroup : extRoleHostGroupList) {
					authHostGroupsBuilder.append(authorizedHostGroup);
					authHostGroupsBuilder.append(",");
				} // end for
				String authHostGroups = authHostGroupsBuilder.substring(0,
						authHostGroupsBuilder.length() - 1);
				if (hostSelectValue == null
						|| hostSelectValue
								.equalsIgnoreCase(Constant.FILTEREDHOST)) {
					Filter tempfilter = new Filter("hostGroups.name",
							FilterOperator.IN, authHostGroups);
					filter = Filter.AND(new Filter(
							"hostStatus.hostMonitorStatus.name",
							FilterOperator.EQ, hostPopUpStatus), tempfilter);
				} else {
					filter = new Filter("hostGroups.name", FilterOperator.IN,
							authHostGroups);
				}
			} // end if
		} // end if
		return filter;
	}

	/**
	 * return the filter to get host list according to filter selected on filter
	 * portlet.
	 * 
	 * @param hostFilterName
	 * @param nodeTypeEnum
	 * @return filter
	 */
	private Filter getHostFilterbyNodeType(String hostFilterName,
			NodeType nodeTypeEnum, String nodeName) {
		Filter filter = null;
		if (hostPopUpStatus == null) {
			LOGGER.debug("current model pop up status is null");
			// if current pop up status is null then set current pop up status
			// as Ok .
			hostPopUpStatus = Constant.OK.toUpperCase();
		}

		Filter leftFilter = getHostORHostGroupFilter(
				FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
				hostPopUpStatus);
		if (NodeType.NETWORK == nodeTypeEnum) {
			// check if stateController return empty string means no filter
			// should be applied on pop up window.
			if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
				filter = leftFilter;
			} else {
				FilterComputer filterComputer = new FilterComputer();
				Filter rightFilter = filterComputer
						.getHostFilter(hostFilterName);
				filter = Filter.AND(leftFilter, rightFilter);
			}
		} else {
			// check if stateController return empty string means no filter
			// should be applied on pop up window.
			if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
				filter = getHostMonitorStatusFilterForHostGroupName(
						hostPopUpStatus, nodeName);
			} else {
				FilterComputer filterComputer = new FilterComputer();
				Filter rightFilter = filterComputer
						.getHostFilter(hostFilterName);
				Filter leftfilterHG = getHostMonitorStatusFilterForHostGroupName(
						hostPopUpStatus, nodeName);
				filter = Filter.AND(leftfilterHG, rightFilter);

			}

		}

		return filter;
	}

	/**
	 * 
	 * Method get the data from web services and set in to host statistics bean.
	 * 
	 * @param filter
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	@SuppressWarnings("unused")
	private List<ModelPopUpDataBean> getHostPopUpDataList(Filter filter)
			throws WSDataUnavailableException, GWPortalException {
		WSFoundationCollection collection = foundFacade
				.getSimpleHostsbyCriteria(filter, null, -1, -1, false);
		SimpleHost[] hostArr = collection.getSimpleHost();
		List<ModelPopUpDataBean> hostList = Collections
				.synchronizedList(new ArrayList<ModelPopUpDataBean>());

		if (hostArr != null) {
			for (int i = 0; i < hostArr.length; i++) {
				ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
				if (hostArr[i] != null) {

					if (hostArr[i].isAcknowledged()) {
						modelpopupdatabean.setAcknowledged(Constant.YES);
					} else {
						modelpopupdatabean.setAcknowledged(Constant.NO);
					}
					// end if

					modelpopupdatabean.setName(hostArr[i].getName());
					modelpopupdatabean.setSubPageURL(NodeURLBuilder
							.buildNodeURL(NodeType.HOST,
									hostArr[i].getHostID(),
									hostArr[i].getName()));
					// TODO : date format pattern string should be come from
					// application property.
					// check current pop status is not in pending state because
					// LastCheckTime is null in pending monitor status
					if (!NetworkObjectStatusEnum.HOST_PENDING.getStatus()
							.equalsIgnoreCase(hostPopUpStatus)) {
						Date lastCheckTime = hostArr[i].getLastCheckTime();

						// check for lastCheckTime if null then display N/A on
						// UI
						if (null == lastCheckTime) {
							modelpopupdatabean
									.setDatetime(Constant.NOT_AVAILABLE_STRING);
						} else {
							modelpopupdatabean.setDatetime(DateUtils.format(
									lastCheckTime,
									Constant.MODEL_POPUP_DATE_FROMAT));
						}
					}
					hostList.add(modelpopupdatabean);
				}
			}

		}
		return hostList;

	}

	/**
	 * get monitor status filter for host and host group.
	 * 
	 * @param filter
	 * @param currentStatus
	 * @return
	 */
	private Filter getHostORHostGroupFilter(String stringProperty,
			String currentStatus) {
		Filter filter = null;
		if (NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED.getStatus()
				.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED.getStatus()
							.toUpperCase());
		} else if (NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED.getStatus()
				.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED.getStatus()
							.toUpperCase());
		} else if (Constant.HOST_OR_GROUP_DOWN.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					Constant.HOST_OR_GROUP_DOWN);
		} else if (Constant.HOST_OR_GROUP_UP.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					Constant.HOST_OR_GROUP_UP);
		} else if (Constant.HOST_OR_SERVICE_PENDING
				.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					Constant.HOST_OR_SERVICE_PENDING);
		} else if (Constant.HOST_OR_GROUP_UNREACHABLE
				.equalsIgnoreCase(currentStatus)) {
			filter = new Filter(stringProperty, FilterOperator.EQ,
					Constant.HOST_OR_GROUP_UNREACHABLE);
		}
		return filter;
	}

	/**
	 * get the service list from foundation layer and display in model pop up
	 * for service
	 * 
	 * @param event
	 */
	public void showservicePopUp(ActionEvent event) {
		Filter filter = null;
		try {
			serviceCurrentStatus = FacesUtils
					.getRequestParameter("servicestatus");
			ServiceStatisticsBean statisbean = (ServiceStatisticsBean) FacesUtils
					.getManagedBean(Constant.SERVICE_STATISTICS_BEAN);
			if (statisbean == null) {
				LOGGER.debug("ServiceStatisticsBean is null in showHostsPopUp method");
				throw new GWPortalException();
			}
			if (Constant.CRITICAL_COLON_SCHEDULED
					.equalsIgnoreCase(serviceCurrentStatus)) {
				serviceCurrentStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
						.getStatus();
			}
			if (Constant.CRITICAL_COLON_UN_SCHEDULED
					.equalsIgnoreCase(serviceCurrentStatus)) {
				serviceCurrentStatus = NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
						.getStatus();
			}
			if (NetworkObjectStatusEnum.SERVICE_OK.getStatus()
					.equalsIgnoreCase(serviceCurrentStatus)
					|| NetworkObjectStatusEnum.SERVICE_PENDING.getStatus()
							.equalsIgnoreCase(serviceCurrentStatus)) {
				serviceAcknowledgedRender = false;
			} else {
				serviceAcknowledgedRender = true;
			}
			if (NetworkObjectStatusEnum.SERVICE_PENDING.getStatus()
					.equalsIgnoreCase(serviceCurrentStatus)) {
				serviceDateTimeRender = false;
			} else {
				serviceDateTimeRender = true;
			}

			Filter leftFilter = null;
			// get the left filter for service.
			leftFilter = getServiceFilter();
			// get filter depending on applied filter in filter portlet.
			filter = getFilteredServiceFilter(leftFilter);

			// on demand model pop up pagination

			setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
					statisbean.getPopupRowSize(), filter, serviceCurrentStatus,
					NodeType.SERVICE, "serviceDescription"));
			// update state-controller
			stateController.update(selectedNodeType, selectedNodeName,
					selectedNodeId);
			String serviceFilterName = stateController
					.getCurrentServiceFilter();
			String serviceSubTitle = ResourceUtils
					.getLocalizedMessage(Constant.SERVICE_SUB_TITLE)
					+ Constant.SPACE_COLON_SPACE;
			statisbean.setCurrentPopstatus(getServiceOrServiceGroupPopUpTitle(
					serviceFilterName, serviceCurrentStatus, serviceSubTitle));
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			if (popUpSelectBean == null) {
				LOGGER.debug("PopUpSelectBean is null in showHostsPopUp method");
				throw new GWPortalException();
			}
			popUpSelectBean.setServiceSelectValue(Constant.FILTEREDSERVICE);

		} catch (WSDataUnavailableException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in showservicePopUp method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}
	}

	/**
	 * returns filter depending on applied filter in filter portlet.
	 * 
	 * @param leftFilter
	 * @return
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private Filter getFilteredServiceFilter(Filter leftFilter)
			throws WSDataUnavailableException, GWPortalException {
		Filter filter = null;
		// update state-controller
		stateController.update(selectedNodeType, selectedNodeName,
				selectedNodeId);
		// get the current selected service filter name
		String serviceFilterName = stateController.getCurrentServiceFilter();

		FilterComputer filterComputer = new FilterComputer();

		// check if stateController return empty string means no filter
		// should be applied on pop up window.
		switch (selectedNodeType) {
		case NETWORK:
			if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterName)) {
				List<String> extRoleHostGroupList = userExtendedRoleBean
						.getExtRoleHostGroupList();
				List<String> extRoleServiceGroupList = userExtendedRoleBean
						.getExtRoleServiceGroupList();
				if (extRoleHostGroupList.isEmpty()
						&& extRoleServiceGroupList.isEmpty()) {
					filter = leftFilter;
				} else {
					StringBuilder authHostGroupsBuilder = new StringBuilder();
					for (String authorizedHostGroup : extRoleHostGroupList) {
						authHostGroupsBuilder.append(authorizedHostGroup);
						authHostGroupsBuilder.append(",");
					} // end for
					String authHostGroups = authHostGroupsBuilder.substring(0,
							authHostGroupsBuilder.length() - 1);

					Filter tempfilter = new Filter("host.hostGroups.name",
							FilterOperator.IN, authHostGroups);
					filter = Filter.AND(leftFilter, tempfilter);
				}

			} else {
				// get final filter
				Filter rightFilter = filterComputer
						.getServiceFilter(serviceFilterName);
				filter = Filter.AND(leftFilter, rightFilter);
			}
			break;
		case HOST_GROUP:
			Filter leftHgFilter = new Filter(
					FilterConstants.SERVICES_BY_HOST_GROUP_NAME_STRING_PROPERTY,
					FilterOperator.EQ, selectedNodeName);
			if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterName)) {
				Filter rightHgFilter = leftFilter;
				filter = Filter.AND(leftHgFilter, rightHgFilter);
			} else {
				// get final filter
				Filter rightFilter = filterComputer
						.getServiceFilter(serviceFilterName);
				filter = Filter.AND(leftHgFilter,
						Filter.AND(leftFilter, rightFilter));

			}

			break;
		case HOST:
			Filter leftHFilter = new Filter(HOST_HOST_NAME_STRING_PROPERTY,
					FilterOperator.EQ, selectedNodeName);
			if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterName)) {
				Filter rightHFilter = leftFilter;
				filter = Filter.AND(leftHFilter, rightHFilter);
			} else {
				// get final filter
				Filter rightFilter = filterComputer
						.getServiceFilter(serviceFilterName);
				filter = Filter.AND(leftHFilter,
						Filter.AND(leftFilter, rightFilter));

			}

			break;
		case SERVICE_GROUP:
			Filter leftSFilter = getServiceFilterByServiceGroupName(selectedNodeName);
			Filter leftSGFilter = Filter.AND(leftSFilter, leftFilter);
			if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterName)) {
				filter = leftSGFilter;
			} else {
				// get final filter
				Filter rightSGFilter = filterComputer
						.getServiceFilter(serviceFilterName);
				filter = Filter.AND(leftSGFilter, rightSGFilter);
			}
			break;
		default:
			filter = new Filter(Constant.MONITOR_STATUS_NAME,
					FilterOperator.EQ, Constant.OK.toUpperCase());
			break;

		}

		return filter;
	}

	/**
	 * returns filter depending on current selected node type.
	 * 
	 * @param leftFilter
	 * @return
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private Filter getAllServiceFilterByNodeType(Filter leftFilter)
			throws WSDataUnavailableException, GWPortalException {
		Filter filter = null;

		switch (selectedNodeType) {
		case NETWORK:
			List<String> extRoleHostGroupList = userExtendedRoleBean
					.getExtRoleHostGroupList();
			List<String> extRoleServiceGroupList = userExtendedRoleBean
					.getExtRoleServiceGroupList();
			if (extRoleHostGroupList.isEmpty()
					&& extRoleServiceGroupList.isEmpty()) {
				filter = leftFilter;
			} else {
				StringBuilder authHostGroupsBuilder = new StringBuilder();
				for (String authorizedHostGroup : extRoleHostGroupList) {
					authHostGroupsBuilder.append(authorizedHostGroup);
					authHostGroupsBuilder.append(",");
				} // end for
				String authHostGroups = authHostGroupsBuilder.substring(0,
						authHostGroupsBuilder.length() - 1);

				filter = new Filter("host.hostGroups.name", FilterOperator.IN,
						authHostGroups);
				// filter = Filter.AND(leftFilter, tempfilter);
			}

			break;
		case HOST_GROUP:
			Filter leftHgFilter = new Filter(
					FilterConstants.SERVICES_BY_HOST_GROUP_NAME_STRING_PROPERTY,
					FilterOperator.EQ, selectedNodeName);
			Filter rightHgFilter = leftFilter;
			filter = Filter.AND(leftHgFilter, rightHgFilter);
			break;
		case HOST:
			Filter leftHFilter = new Filter(HOST_HOST_NAME_STRING_PROPERTY,
					FilterOperator.EQ, selectedNodeName);
			Filter rightHFilter = leftFilter;
			filter = Filter.AND(leftHFilter, rightHFilter);
			break;
		case SERVICE_GROUP:
			Filter leftSFilter = getServiceFilterByServiceGroupName(selectedNodeName);
			filter = Filter.AND(leftSFilter, leftFilter);
			break;
		default:
			filter = new Filter(Constant.MONITOR_STATUS_NAME,
					FilterOperator.EQ, Constant.OK.toUpperCase());
			break;

		}

		return filter;
	}

	/**
	 * return service filter to get all services by service group name
	 * 
	 * @param serviceGroupName
	 * @return Filter
	 * @throws GWPortalException
	 * @throws WSDataUnavailableException
	 */
	private Filter getServiceFilterByServiceGroupName(String serviceGroupName)
			throws WSDataUnavailableException, GWPortalException {

		StringBuilder categoryStringBuilder = new StringBuilder();
		// Get the list of CategoryEntities for ServiceGroup
		CategoryEntity[] categoryEntities = foundFacade
				.getCategoryEntities(serviceGroupName);
		if (categoryEntities != null) {
			for (CategoryEntity categoryEntity : categoryEntities) {

				// build a comma separated string of Service IDs under
				// ServiceGroup
				categoryStringBuilder.append(categoryEntity.getObjectID()
						+ Constant.COMMA);
			}
		}

		// Filter to pass to "getServicesbyCriteria" method of web
		// service
		// so as to get Services List
		Filter filter = new Filter(
				com.groundworkopensource.portal.common.FilterConstants.SERVICE_STATUS_ID,
				FilterOperator.IN, categoryStringBuilder.toString());

		return filter;
	}

	/**
	 * returns service Model window data list.
	 * 
	 * @param filter
	 * @return List
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */

	@SuppressWarnings("unused")
	private List<ModelPopUpDataBean> getServicePopUpDataList(Filter filter)
			throws GWPortalException, WSDataUnavailableException {
		List<ModelPopUpDataBean> serviceList = Collections
				.synchronizedList(new ArrayList<ModelPopUpDataBean>());
		SimpleServiceStatus[] serviceArr = foundFacade
				.getSimpleServicesbyCriteria(filter, null, -1, -1);
		if (serviceArr != null) {
			for (int i = 0; i < serviceArr.length; i++) {
				ModelPopUpDataBean modelpopupbean = new ModelPopUpDataBean();
				if (serviceArr[i] != null) {

					if (serviceArr[i].isAcknowledged()) {
						modelpopupbean.setAcknowledged(Constant.YES);
					} else {
						modelpopupbean.setAcknowledged(Constant.NO);
					}
					modelpopupbean.setName(serviceArr[i].getDescription());
					modelpopupbean.setSubPageURL(NodeURLBuilder.buildNodeURL(
							NodeType.SERVICE,
							serviceArr[i].getServiceStatusID(),
							serviceArr[i].getDescription()));
					modelpopupbean.setParentName(serviceArr[i].getHostName());
					// TODO :
					if (!NetworkObjectStatusEnum.SERVICE_PENDING.getStatus()
							.equalsIgnoreCase(serviceCurrentStatus)) {
						Date lastCheckTime = serviceArr[i].getLastCheckTime();

						// check for lastchecktime if null then display N/A on
						// UI
						if (null == lastCheckTime) {
							modelpopupbean
									.setDatetime(Constant.NOT_AVAILABLE_STRING);

						} else {
							modelpopupbean.setDatetime(DateUtils.format(
									lastCheckTime,
									Constant.MODEL_POPUP_DATE_FROMAT));
						}

					}
					// Setting service parent name(Host)

					String hostName = serviceArr[i].getHostName();
					if (hostName != null) {
						modelpopupbean.setParentName(hostName);
					}
					// Setting service parent sub page URL
					modelpopupbean.setParentPageURL(NodeURLBuilder
							.buildNodeURL(NodeType.HOST,
									serviceArr[i].getHostId(), hostName));

					serviceList.add(modelpopupbean);
				}
			}

		}
		return serviceList;

	}

	/**
	 * 
	 * method provide host group list according to filter selected on host group
	 * model pop up window.
	 * 
	 * @param e
	 */
	public void applyHostGroupFilters(ActionEvent e) {
		Filter filter = null;
		// get current instance of PopUpSelectBean
		PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
				.getManagedBean(Constant.POP_UP_SELECT_BEAN);
		// get current instance of Host Group StatisticsBean
		HostGroupStatisticsBean hostGroupStatisBean = (HostGroupStatisticsBean) FacesUtils
				.getManagedBean(Constant.HOST_GROUP_STATISTICS_BEAN);
		try {
			if (popUpSelectBean == null) {
				LOGGER.debug("PopUpSelectBean is null in applyHostGroupFilters method");
				throw new GWPortalException();
			}
			if (hostGroupStatisBean == null) {
				LOGGER.debug("HostGroupStatisticsBean is null in applyHostGroupFilters method");
				throw new GWPortalException();
			}
			if (popUpSelectBean.getHgSelectValue().equalsIgnoreCase(
					Constant.FILTEREDHOSTGROUPS)) {
				if (filteredHostGroupList == null) {
					// create filter
					Filter leftFilter = getHostORHostGroupFilter(
							Constant.HOSTGROUP_MONITORSTATUS_NAME,
							hostGroupPopUpStatus);
					Filter rightFilter = null;
					// update state-controller
					stateController.update(selectedNodeType, selectedNodeName,
							selectedNodeId);
					// get the current selected host Group filter name
					String hostGrpFilterName = stateController
							.getCurrentHostFilter();
					if (!Constant.EMPTY_STRING
							.equalsIgnoreCase(hostGrpFilterName)) {

						FilterComputer filterComputer = new FilterComputer();
						rightFilter = filterComputer
								.getHostFilter(hostGrpFilterName);

					}
					filteredHostGroupList = getHostGroupPopUpDataList(
							leftFilter, rightFilter);
					hostGroupStatisBean.setHostGroupList(filteredHostGroupList);
					hostGroupStatisBean.setRowCount(filteredHostGroupList
							.size());

				} else {
					hostGroupStatisBean.setHostGroupList(filteredHostGroupList);
					hostGroupStatisBean.setRowCount(filteredHostGroupList
							.size());
				}
				popUpSelectBean.setHgSelectValue(Constant.FILTEREDHOSTGROUPS);
			} else {
				if (allHostGroupList == null) {
					filter = getHostORHostGroupFilter(
							Constant.HOSTGROUP_MONITORSTATUS_NAME,
							hostGroupPopUpStatus);

					allHostGroupList = getHostGroupPopUpDataList(filter, null);
					hostGroupStatisBean.setHostGroupList(allHostGroupList);
					hostGroupStatisBean.setRowCount(allHostGroupList.size());
				} else {
					hostGroupStatisBean.setHostGroupList(allHostGroupList);
					hostGroupStatisBean.setRowCount(allHostGroupList.size());
				}
				popUpSelectBean.setHgSelectValue(Constant.ALLHOSTGROUPS);
			}

		} catch (GWPortalException ex) {
			handleError(ex.getMessage());
		} catch (WSDataUnavailableException ex) {
			handleError(ex.getMessage());
		}

	}

	/**
	 * 
	 * method provide host list according to filter selected on host model pop
	 * up window.
	 * 
	 * @param e
	 */
	public void applyHostFilters(ActionEvent e) {

		Filter filter = null;
		// get current instance of PopUpSelectBean
		PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
				.getManagedBean(Constant.POP_UP_SELECT_BEAN);
		// get current instance of HostStatisticsBean
		HostStatisticsBean statisbean = (HostStatisticsBean) FacesUtils
				.getManagedBean(Constant.HOST_STATISTICS_BEAN);

		try {
			DataPaginator dataPaginator = (DataPaginator) e.getComponent()
					.findComponent("hostmodelpopupdatatable");
			if (null != dataPaginator) {
				dataPaginator.gotoFirstPage();
			}
			if (popUpSelectBean == null || statisbean == null) {
				LOGGER.debug("HostStatisticsBean or PopUpSelectBean is null in applyHostFilters method  ");
				throw new GWPortalException();
			}
			if (popUpSelectBean.getHostSelectValue().equalsIgnoreCase(
					Constant.FILTEREDHOST)) {

				Filter leftFilter = getHostORHostGroupFilter(
						FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
						hostPopUpStatus);
				// update state-controller
				stateController.update(selectedNodeType, selectedNodeName,
						selectedNodeId);
				// get the current selected host filter name
				String hostFilterName = stateController.getCurrentHostFilter();

				if (NodeType.NETWORK.equals(selectedNodeType)) {
					if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
						filter = leftFilter;
					} else {
						FilterComputer filterComputer = new FilterComputer();
						Filter rightFilter = filterComputer
								.getHostFilter(hostFilterName);
						filter = Filter.AND(leftFilter, rightFilter);
					}
				} else {
					// check if stateController return empty string means no
					// filter
					// should be applied on pop up window.
					if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterName)) {
						filter = getHostMonitorStatusFilterForHostGroupName(
								hostPopUpStatus, selectedNodeName);
					} else {
						FilterComputer filterComputer = new FilterComputer();
						Filter rightFilter = filterComputer
								.getHostFilter(hostFilterName);
						Filter leftfilterHG = getHostMonitorStatusFilterForHostGroupName(
								hostPopUpStatus, selectedNodeName);
						filter = Filter.AND(leftfilterHG, rightFilter);

					}
				}

				filter = buildAuthorizedHostsFilter(selectedNodeType, filter,
						hostFilterName, popUpSelectBean.getHostSelectValue());

				// on demand model pop up pagination
				setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
						statisbean.getPopupRowSize(), filter, hostPopUpStatus,
						NodeType.HOST, "hostName"));
				// filteredHostList = getHostPopUpDataList(filter);
				// statisbean.setHostList(filteredHostList);
				// statisbean.setHostRowCount(filteredHostList.size());

				popUpSelectBean.setHostSelectValue(Constant.FILTEREDHOST);
			} else {

				if (NodeType.NETWORK.equals(selectedNodeType)) {
					filter = getHostORHostGroupFilter(
							FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
							hostPopUpStatus);
				} else {

					filter = getHostMonitorStatusFilterForHostGroupName(
							hostPopUpStatus, selectedNodeName);
				}

				filter = buildAuthorizedHostsFilter(selectedNodeType, filter,
						null, popUpSelectBean.getHostSelectValue());

				// on demand model pop up pagination
				setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
						statisbean.getPopupRowSize(), filter, hostPopUpStatus,
						NodeType.HOST, "hostName"));
				// allHostList = getHostPopUpDataList(filter);
				// statisbean.setHostList(allHostList);
				// statisbean.setHostRowCount(allHostList.size());

				popUpSelectBean.setHostSelectValue(Constant.ALLHOST);
			}

		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in applyHostFilters method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}
	}

	/**
	 * 
	 * method provide service group list according to filter selected on service
	 * group model pop up window.
	 * 
	 * @param e
	 */
	public void applyServiceGroupFilters(ActionEvent e) {

		try {
			// get current instance of PopUpSelectBean
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			// get current instance of ServiceStatisticsBean
			ServiceGroupStatistics statisbean = (ServiceGroupStatistics) FacesUtils
					.getManagedBean(Constant.SERVICE_GROUP_STATISTICS_BEAN);
			if (popUpSelectBean == null || statisbean == null) {
				LOGGER.debug("PopUpSelectBean or ServiceGroupStatistics bean is null in applyServiceGroupFilters ");
				throw new GWPortalException();
			}

			if (popUpSelectBean.getSgSelectValue().equalsIgnoreCase(
					Constant.FILTEREDSERVICEGROUP)) {
				if (filteredServiceGroupList == null) {

					// getting service Group filter
					Filter filterServiceGroup = getServiceGroupFilter(
							serviceGroupCurrentStatus, true);
					if (filterServiceGroup != null) {
						// getting filtered Service Group LIst
						filteredServiceGroupList = getServiceGroupFilteredDataLIst(filterServiceGroup);
					} else {
						// Assign empty list
						filteredServiceGroupList = Collections
								.synchronizedList(new ArrayList<ModelPopUpDataBean>());
					}

				}
				statisbean.setServicesGroupsList(filteredServiceGroupList);
				statisbean.setServiceGroupRowCount(filteredServiceGroupList
						.size());
				popUpSelectBean.setSgSelectValue(Constant.FILTEREDSERVICEGROUP);
			} else {
				if (allServiceGroupList == null) {
					// getting total service Group
					Filter totalServiceGroup = getServiceGroupFilter(
							serviceGroupCurrentStatus, false);
					if (totalServiceGroup != null) {
						// getting total Service Group LIst
						allServiceGroupList = getServiceGroupFilteredDataLIst(totalServiceGroup);
					} else {
						// Assign empty list
						allServiceGroupList = Collections
								.synchronizedList(new ArrayList<ModelPopUpDataBean>());
					}

				}
				statisbean.setServicesGroupsList(allServiceGroupList);
				if (allServiceGroupList != null) {
					statisbean.setServiceGroupRowCount(allServiceGroupList
							.size());
				}
				popUpSelectBean.setSgSelectValue(Constant.ALLSERVICEGROUP);
			}
		} catch (WSDataUnavailableException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in applyServiceGroupFilters method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}

	}

	/**
	 * 
	 * method provide service list according to filter selected on service model
	 * pop up window.
	 * 
	 * @param e
	 */
	public void applyServiceFilters(ActionEvent e) {
		Filter filter = null;
		Filter leftFilter = null;

		try {
			DataPaginator dataPaginator = (DataPaginator) e.getComponent()
					.findComponent("servicemodelpagination");
			if (null != dataPaginator) {
				dataPaginator.gotoFirstPage();
			}
			// get current instance of PopUpSelectBean
			PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
					.getManagedBean(Constant.POP_UP_SELECT_BEAN);
			// get current instance of ServiceStatisticsBean
			ServiceStatisticsBean statisbean = (ServiceStatisticsBean) FacesUtils
					.getManagedBean(Constant.SERVICE_STATISTICS_BEAN);

			if (popUpSelectBean == null || statisbean == null) {
				LOGGER.debug("PopUpSelectBean or ServiceGroupStatistics bean is null in applyServiceFilters ");
				throw new GWPortalException();
			}
			if (popUpSelectBean.getServiceSelectValue().equalsIgnoreCase(
					Constant.FILTEREDSERVICE)) {

				// get default filter for service
				leftFilter = getServiceFilter();
				// get final filter to be applied on service.
				filter = getFilteredServiceFilter(leftFilter);
				// get filtered service list

				// on demand model pop up pagination
				setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
						statisbean.getPopupRowSize(), filter,
						serviceCurrentStatus, NodeType.SERVICE,
						"serviceDescription"));

				popUpSelectBean.setServiceSelectValue(Constant.FILTEREDSERVICE);
			} else {

				// getting service filter depending on current monitor
				// status
				Filter leftfilter = getServiceFilter();
				// getting filter for all service by node type
				filter = getAllServiceFilterByNodeType(leftfilter);

				// on demand model pop up pagination
				setStatisticsModelPopUpListBean(new StatisticsModelPopUpListBean(
						statisbean.getPopupRowSize(), filter,
						serviceCurrentStatus, NodeType.SERVICE,
						"serviceDescription"));

				popUpSelectBean.setServiceSelectValue(Constant.ALL_SERVICE);
			}
		} catch (WSDataUnavailableException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (GWPortalException e1) {
			setErrorPopUp(true);
			setErrorMessagePopUp(e1.getMessage());
		} catch (Exception ex) {
			LOGGER.error("unexpected exception occur in applyServiceFilters method "
					+ ex);
			setErrorPopUp(true);
			setErrorMessagePopUp(new GWPortalException().getMessage());
		}

	}

	/**
	 * Returns service filter according to monitor status.
	 * 
	 * @param leftFilter
	 * @return Filter
	 */
	private Filter getServiceFilter() {
		Filter leftFilter = null;
		for (int i = 0; i < serviceStatusArray.length; i++) {
			if (serviceStatusArray[i].equalsIgnoreCase(serviceCurrentStatus)) {
				leftFilter = new Filter(FilterConstants.MONITOR_STATUS_NAME,
						FilterOperator.EQ, serviceStatusArray[i].toUpperCase());
				break;
			}

		}
		return leftFilter;
	}

	/**
	 * Sets the filteredHostList.
	 * 
	 * @param filteredHostList
	 *            the filteredHostList to set
	 */
	public void setFilteredHostList(List<ModelPopUpDataBean> filteredHostList) {
		this.filteredHostList = filteredHostList;
	}

	/**
	 * Returns the allHostList.
	 * 
	 * @return the allHostList
	 */
	public List<ModelPopUpDataBean> getAllHostList() {
		return allHostList;
	}

	/**
	 * Sets the allHostList.
	 * 
	 * @param allHostList
	 *            the allHostList to set
	 */
	public void setAllHostList(List<ModelPopUpDataBean> allHostList) {
		this.allHostList = allHostList;
	}

	/**
	 * Returns the filteredHostGroupList.
	 * 
	 * @return the filteredHostGroupList
	 */
	public List<ModelPopUpDataBean> getFilteredHostGroupList() {
		return filteredHostGroupList;
	}

	/**
	 * Sets the filteredHostGroupList.
	 * 
	 * @param filteredHostGroupList
	 *            the filteredHostGroupList to set
	 */
	public void setFilteredHostGroupList(
			List<ModelPopUpDataBean> filteredHostGroupList) {
		this.filteredHostGroupList = filteredHostGroupList;
	}

	/**
	 * Returns the allHostGroupList.
	 * 
	 * @return the allHostGroupList
	 */
	public List<ModelPopUpDataBean> getAllHostGroupList() {
		return allHostGroupList;
	}

	/**
	 * Sets the allHostGroupList.
	 * 
	 * @param allHostGroupList
	 *            the allHostGroupList to set
	 */
	public void setAllHostGroupList(List<ModelPopUpDataBean> allHostGroupList) {
		this.allHostGroupList = allHostGroupList;
	}

	/**
	 * Returns the filteredHostList.
	 * 
	 * @return the filteredHostList
	 */
	public List<ModelPopUpDataBean> getFilteredHostList() {
		return filteredHostList;
	}

	/**
	 * Returns the filteredServiceGroupList.
	 * 
	 * @return the filteredServiceGroupList
	 */
	public List<ModelPopUpDataBean> getFilteredServiceGroupList() {
		return filteredServiceGroupList;
	}

	/**
	 * Sets the filteredServiceGroupList.
	 * 
	 * @param filteredServiceGroupList
	 *            the filteredServiceGroupList to set
	 */
	public void setFilteredServiceGroupList(
			List<ModelPopUpDataBean> filteredServiceGroupList) {
		this.filteredServiceGroupList = filteredServiceGroupList;
	}

	/**
	 * Returns the allServiceGroupList.
	 * 
	 * @return the allServiceGroupList
	 */
	public List<ModelPopUpDataBean> getAllServiceGroupList() {
		return allServiceGroupList;
	}

	/**
	 * Sets the allServiceGroupList.
	 * 
	 * @param allServiceGroupList
	 *            the allServiceGroupList to set
	 */
	public void setAllServiceGroupList(
			List<ModelPopUpDataBean> allServiceGroupList) {
		this.allServiceGroupList = allServiceGroupList;
	}

	// /**
	// * Callback method
	// */
	// @Override
	// public void refresh(String xmlTopic) {
	//
	// // try {
	// // if (xmlTopic != null) {
	// // if ((xmlTopic.indexOf(HOSTGROUP) > 1 || xmlTopic.indexOf(HOST) > 1)
	// // && statisbeanHG != null) {
	// // LOGGER.debug("Refreshing HostGroup....");
	// // this.setHostGroupStatistics();
	// // SessionRenderer.render(groupRenderName);
	// // } else if (xmlTopic.indexOf(HOST) > 1
	// // && statishostbeanHS != null) {
	// // LOGGER.debug("Refreshing Host....");
	// // this.setHostStatistics();
	// // SessionRenderer.render(groupRenderName);
	// // } else if (xmlTopic.indexOf(SERVICEGROUP) > 1
	// // || (xmlTopic.indexOf(SERVICESTATUS) > 1)
	// // && statisbeanSG != null) {
	// // LOGGER.debug("Refreshing ServiceGroup....");
	// // this.setServiceGroupStatistics();
	// // SessionRenderer.render(groupRenderName);
	// // } else if (xmlTopic.indexOf(SERVICESTATUS) > 1
	// // && statisbeanSS != null) {
	// // LOGGER.debug("Refreshing ServiceStatus....");
	// // this.setServiceStatistics();
	// // SessionRenderer.render(groupRenderName);
	// // } // end if
	// // } // end if
	// // // Arul:Generate a graph here.Offshore to check this
	// // } catch (Exception exc) {
	// // LOGGER.error(exc.getMessage());
	// //
	// // }
	// }

	/**
	 * draw host group pie chart return host group pie chart byte array.
	 * 
	 * @param statisticsMap
	 * @return bytes
	 * @throws IOException
	 */
	public byte[] getHGOrHPieChartBytes(Map<String, Long> statisticsMap)
			throws IOException {
		byte[] encodeAsPNG = null;

		ChartHandler chartHandler = new ChartHandler();
		Color[] colors = chartHandler.getHostorHostGroupColorArray();
		DefaultPieDataset data = chartHandler
				.getHostorHostGroupPieDataSet(statisticsMap);
		// get jfree chart object from factory.
		JFreeChart chart = ChartFactory.createPieChart(Constant.EMPTY_STRING,
				data, false, false, false);
		// chart.setBackgroundPaint(Color.BLACK);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);
		// plot.setForegroundAlpha(0.5f);
		plot.setLabelLinksVisible(true);
		chartHandler.setPieChartColor(plot, colors, data);
		encodeAsPNG = ChartUtilities.encodeAsPNG(chart.createBufferedImage(
				Constant.DIAL_WIDTH, Constant.DIAL_HEIGHT));

		return encodeAsPNG;

	}

	/**
	 * draw host group pie chart return host group pie chart byte array.
	 * 
	 * @param statisticsMap
	 * @return bytes
	 * @throws IOException
	 */
	public byte[] getSGOrSPieChartBytes(Map<String, Long> statisticsMap)
			throws IOException {
		byte[] encodeAsPNG = null;

		ChartHandler chartHandler = new ChartHandler();
		Color[] colors = chartHandler.getSetviceorServiceGroupColorArray();
		DefaultPieDataset data = chartHandler
				.getServiceorServiceGroupPieDataSet(statisticsMap);
		// get jfree chart object from factory.
		JFreeChart chart = ChartFactory.createPieChart(Constant.EMPTY_STRING,
				data, false, false, false);
		// chart.setBackgroundPaint(Color.BLACK);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);
		// plot.setForegroundAlpha(0.5f);
		plot.setLabelLinksVisible(true);
		chartHandler.setPieChartColor(plot, colors, data);
		encodeAsPNG = ChartUtilities.encodeAsPNG(chart.createBufferedImage(
				Constant.DIAL_WIDTH, Constant.DIAL_HEIGHT));

		return encodeAsPNG;

	}

	/**
	 * Handles error : sets error flag and message.
	 */
	private void handleError(String errorMessage) {
		setMessage(true);
		setError(true);
		setErrorMessage(errorMessage);
	}

	/**
	 * Handles error for host : sets error flag and message.
	 */
	private void handleHostError(String errorMessage) {
		setHostMessage(true);
		setHostError(true);
		setHostErrorMessage(errorMessage);
	}

	/**
	 * Handles error for hostGroup : sets error flag and message.
	 */
	private void handleHGError(String errorMessage) {
		setHgMessage(true);
		setHgError(true);
		setHgErrorMessage(errorMessage);
	}

	/**
	 * Handles error for ServiceGroup : sets error flag and message.
	 */
	private void handleSGError(String errorMessage) {
		setSgMessage(true);
		setSgError(true);
		setSgErrorMessage(errorMessage);
	}

	/**
	 * Handles error : sets error flag and message.
	 */
	private void handleServiceError(String errorMessage) {
		setServiceMessage(true);
		setServiceError(true);
		setServiceErrorMessage(errorMessage);
	}

	/**
	 * Handles Info : sets Info flag and message.
	 */
	private void handleInfo(String infoMessage) {
		setMessage(true);
		setInfo(true);
		setInfoMessage(infoMessage);
	}

	// /**
	// * Handles Info for host statistics : sets Info flag and message.
	// */
	// private void handleHostInfo(String infoMessage) {
	// setHostMessage(true);
	// setHostInfo(true);
	// setHostInfoMessage(infoMessage);
	// }
	//
	/**
	 * Handles Info for host group statistics : sets Info flag and message.
	 */
	private void handleHGInfo(String infoMessage) {
		setHgMessage(true);
		setHgInfo(true);
		setHgInfoMessage(infoMessage);
	}

	/**
	 * Handles Info for Service group statistics : sets Info flag and message.
	 */
	private void handleSGInfo(String infoMessage) {
		setSgMessage(true);
		setSgInfo(true);
		setSgInfoMessage(infoMessage);
	}

	//
	// /**
	// * Handles Info for service statistics : sets Info flag and message.
	// */
	// private void handleServiceInfo(String infoMessage) {
	// setServiceMessage(true);
	// setServiceInfo(true);
	// setServiceInfoMessage(infoMessage);
	// }

	/**
	 * Sets the error.
	 * 
	 * @param error
	 *            the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * Returns the error.
	 * 
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Sets the errorMessage.
	 * 
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the errorMessage.
	 * 
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(boolean message) {
		this.message = message;
	}

	/**
	 * Returns the message.
	 * 
	 * @return the message
	 */
	public boolean isMessage() {
		return message;
	}

	/**
	 * Sets the info.
	 * 
	 * @param info
	 *            the info to set
	 */
	public void setInfo(boolean info) {
		this.info = info;
	}

	/**
	 * Returns the info.
	 * 
	 * @return the info
	 */
	public boolean isInfo() {
		return info;
	}

	/**
	 * Sets the infoMessage.
	 * 
	 * @param infoMessage
	 *            the infoMessage to set
	 */
	public void setInfoMessage(String infoMessage) {
		this.infoMessage = infoMessage;
	}

	/**
	 * Returns the infoMessage.
	 * 
	 * @return the infoMessage
	 */
	public String getInfoMessage() {
		return infoMessage;
	}

	/**
	 * Method that will be called on click of "Retry now" button on error page.
	 * 
	 * @param event
	 */
	public void reloadHGPortlet(ActionEvent event) {
		// re-initialize the bean so as to reload UI
		setHgError(false);
		setHgInfo(false);
		setHgMessage(false);

		this.setHostGroupStatistics();
	}

	/**
	 * Method that will be called on click of "Retry now" button on error page.
	 * 
	 * @param event
	 */
	public void reloadSGPortlet(ActionEvent event) {
		// re-initialize the bean so as to reload UI
		setSgError(false);
		setSgInfo(false);
		setSgMessage(false);

		this.setServiceGroupStatistics();
	}

	/**
	 * Method that will be called on click of "Retry now" button on error page.
	 * 
	 * @param event
	 */
	public void reloadServicePortlet(ActionEvent event) {
		if (!inStatusViewer && !handleDashboardProcessing()) {
			return;
		}
		this.setServiceStatistics();
	}

	/**
	 * Method that will be called on click of "Retry now" button on error page.
	 * 
	 * @param event
	 */
	public void reloadHostPortlet(ActionEvent event) {
		if (!inStatusViewer && !handleDashboardProcessing()) {
			return;
		}
		this.setHostStatistics();
	}

	/**
	 * Sets the errorPopUp.
	 * 
	 * @param errorPopUp
	 *            the errorPopUp to set
	 */
	public void setErrorPopUp(boolean errorPopUp) {
		this.errorPopUp = errorPopUp;
	}

	/**
	 * Returns the errorPopUp.
	 * 
	 * @return the errorPopUp
	 */
	public boolean isErrorPopUp() {
		return errorPopUp;
	}

	/**
	 * Sets the errorMessagePopUp.
	 * 
	 * @param errorMessagePopUp
	 *            the errorMessagePopUp to set
	 */
	public void setErrorMessagePopUp(String errorMessagePopUp) {
		this.errorMessagePopUp = errorMessagePopUp;
	}

	/**
	 * Returns the errorMessagePopUp.
	 * 
	 * @return the errorMessagePopUp
	 */
	public String getErrorMessagePopUp() {
		return errorMessagePopUp;
	}

	/**
	 * Method used to navigate to Host-Acknowledgment pop up page
	 * 
	 * @param event
	 */
	public void showHostAcknowledgementPopup(ActionEvent event) {

		try {
			UIComponent parent = event.getComponent().getParent().getParent()
					.getParent().getParent().getParent();
			List<UIComponent> children = parent.getChildren();
			if (children != null && children.size() >= Constant.THREE) {
				DataPaginator dataPaginator = (DataPaginator) children.get(2);
				if (null != dataPaginator) {
					dataPaginator.gotoFirstPage();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception while getting DataPaginator UIComponent in showHostAcknowledgementPopup() method");
		}

		DataPaginator dataPaginator = (DataPaginator) event.getComponent()
				.findComponent("hostmodelpopupdatatable");
		if (null != dataPaginator) {
			dataPaginator.gotoFirstPage();
		}

		// Retrieve parameters
		String hostName = (String) event.getComponent().getAttributes()
				.get(PARAM_HOST_NAME);
		String userName = FacesUtils.getLoggedInUser();

		// Set parameters in pop-up display bean
		AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
				.getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);
		if (acknowledgePopupBean == null) {
			LOGGER.debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
			return;
		}

		acknowledgePopupBean.setHostAck(true);
		acknowledgePopupBean.setHostName(hostName);
		acknowledgePopupBean.setAuthor(userName);
		acknowledgePopupBean.setUserName(userName);

		// set if in dashboard or in status viewer
		boolean inDashbord = PortletUtils.isInDashbord();
		acknowledgePopupBean.setInStatusViewer(!inDashbord);
		if (inDashbord) {
			acknowledgePopupBean
					.setPopupStyle(Constant.ACK_POPUP_DASHBOARD_STYLE);
		}

		// Close underlying pop-up bean
		// PopupBean popup = (PopupBean) FacesUtils
		// .getManagedBean(Constant.POP_UP_MANAGED_BEAN);
		// popup.closePopup();
		statishostbeanHS.setVisible(false);

		// Set pop-up visible
		acknowledgePopupBean.setVisible(true);
	}

	/**
	 * Method used to navigate to Service-Acknowledgment pop up page
	 * 
	 * @param event
	 */
	public void showServiceAcknowledgementPopup(ActionEvent event) {

		// Fetching UI data Paginator component.
		try {
			UIComponent parent = event.getComponent().getParent().getParent()
					.getParent().getParent().getParent();
			List<UIComponent> children = parent.getChildren();
			if (children != null && children.size() >= Constant.THREE) {
				DataPaginator dataPaginator = (DataPaginator) children.get(2);
				if (null != dataPaginator) {
					dataPaginator.gotoFirstPage();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception while getting DataPaginator UIComponent in showServiceAcknowledgementPopup() method");
		}

		// Get runtime attributes for particular service
		String serviceName = (String) event.getComponent().getAttributes()
				.get(PARAM_SERVICE_NAME);
		String hostName = (String) event.getComponent().getAttributes()
				.get(PARAM_HOST_NAME);
		String userName = FacesUtils.getLoggedInUser();

		// Set parameters in pop-up display bean
		AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
				.getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);

		if (acknowledgePopupBean == null) {
			LOGGER.debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
			return;
		}

		// Indicates this is a service acknowledgment pop-up
		acknowledgePopupBean.setHostAck(false);
		acknowledgePopupBean.setHostName(hostName);
		acknowledgePopupBean.setAuthor(userName);
		acknowledgePopupBean.setUserName(userName);
		acknowledgePopupBean.setServiceDescription(serviceName);
		// code added for making sure the persistent comments field by default
		// checked
		acknowledgePopupBean.setPersistentComment(true);

		// set if in dashboard or in status viewer
		boolean inDashbord = PortletUtils.isInDashbord();
		acknowledgePopupBean.setInStatusViewer(!inDashbord);
		if (inDashbord) {
			acknowledgePopupBean
					.setPopupStyle(Constant.ACK_POPUP_DASHBOARD_STYLE);
		}

		// Close underlying pop-up bean
		// PopupBean popup = (PopupBean) FacesUtils
		// .getManagedBean(Constant.POP_UP_MANAGED_BEAN);
		// popup.closePopup();
		statisbeanSS.setVisible(false);

		// Set pop-up visible
		acknowledgePopupBean.setVisible(true);
	}

	/**
	 * Sets the hostAcknowledgedRender.
	 * 
	 * @param hostAcknowledgedRender
	 *            the hostAcknowledgedRender to set
	 */
	public void setHostAcknowledgedRender(boolean hostAcknowledgedRender) {
		this.hostAcknowledgedRender = hostAcknowledgedRender;
	}

	/**
	 * Returns the hostAcknowledgedRender.
	 * 
	 * @return the hostAcknowledgedRender
	 */
	public boolean isHostAcknowledgedRender() {
		return hostAcknowledgedRender;
	}

	/**
	 * Sets the serviceAcknowledgedRender.
	 * 
	 * @param serviceAcknowledgedRender
	 *            the serviceAcknowledgedRender to set
	 */
	public void setServiceAcknowledgedRender(boolean serviceAcknowledgedRender) {
		this.serviceAcknowledgedRender = serviceAcknowledgedRender;
	}

	/**
	 * Returns the serviceAcknowledgedRender.
	 * 
	 * @return the serviceAcknowledgedRender
	 */
	public boolean isServiceAcknowledgedRender() {
		return serviceAcknowledgedRender;
	}

	/**
	 * Sets the hostDateTimeRender.
	 * 
	 * @param hostDateTimeRender
	 *            the hostDateTimeRender to set
	 */
	public void setHostDateTimeRender(boolean hostDateTimeRender) {
		this.hostDateTimeRender = hostDateTimeRender;
	}

	/**
	 * Returns the hostDateTimeRender.
	 * 
	 * @return the hostDateTimeRender
	 */
	public boolean isHostDateTimeRender() {
		return hostDateTimeRender;
	}

	/**
	 * Sets the serviceDateTimeRender.
	 * 
	 * @param serviceDateTimeRender
	 *            the serviceDateTimeRender to set
	 */
	public void setServiceDateTimeRender(boolean serviceDateTimeRender) {
		this.serviceDateTimeRender = serviceDateTimeRender;
	}

	/**
	 * Returns the serviceDateTimeRender.
	 * 
	 * @return the serviceDateTimeRender
	 */
	public boolean isServiceDateTimeRender() {
		return serviceDateTimeRender;
	}

	/**
	 * Returns the pnlGroupBlankRender.
	 * 
	 * @return the pnlGroupBlankRender
	 */
	public boolean isPnlGroupBlankRender() {
		boolean inDashbord = PortletUtils.isInDashbord();
		if (inDashbord) {
			return false;
		}
		if (NodeType.HOST == selectedNodeType) {
			pnlGroupBlankRender = true;
		} else {
			pnlGroupBlankRender = false;
		}

		return pnlGroupBlankRender;
	}

	/**
	 * Sets the pnlGroupBlankRender.
	 * 
	 * @param pnlGroupBlankRender
	 *            the pnlGroupBlankRender to set
	 */
	public void setPnlGroupBlankRender(boolean pnlGroupBlankRender) {
		this.pnlGroupBlankRender = pnlGroupBlankRender;
	}

	/**
	 * Returns the serviceStyle.
	 * 
	 * @return the serviceStyle
	 */
	public String getServiceStyle() {
		boolean inDashbord = PortletUtils.isInDashbord();
		if (!inDashbord && NodeType.SERVICE_GROUP == selectedNodeType) {
			serviceStyle = "width: 270px;";
		}
		return serviceStyle;
	}

	/**
	 * Sets the serviceStyle.
	 * 
	 * @param serviceStyle
	 *            the serviceStyle to set
	 */
	public void setServiceStyle(String serviceStyle) {
		this.serviceStyle = serviceStyle;
	}

	/**
	 * Sets the dashboardInfo.
	 * 
	 * @param dashboardInfo
	 *            the dashboardInfo to set
	 */
	public void setDashboardInfo(boolean dashboardInfo) {
		this.dashboardInfo = dashboardInfo;
	}

	/**
	 * Returns the dashboardInfo.
	 * 
	 * @return the dashboardInfo
	 */
	public boolean isDashboardInfo() {
		return dashboardInfo;
	}

	// /**
	// * (non-Javadoc)
	// *
	// * @see
	// com.groundworkopensource.portal.statusviewer.bean.ServerPush#free()
	// */
	// @Override
	// public void free() {
	// LOGGER.warn("$$$$ In free() of Statistics Handler $$$$");
	// statishostbeanHS = null;
	// statisbeanHG = null;
	// statisbeanSG = null;
	// statisbeanSS = null;
	//
	// FacesContext.getCurrentInstance().getApplication().createValueBinding(
	// Constant.HOST_GROUP_STATISTICS_BEAN).setValue(
	// FacesContext.getCurrentInstance(), null);
	//
	// super.free();
	// }

	/**
	 * Returns the selectedNodeId.
	 * 
	 * @return the selectedNodeId
	 */
	public int getSelectedNodeId() {
		return selectedNodeId;
	}

	/**
	 * Sets the selectedNodeId.
	 * 
	 * @param selectedNodeId
	 *            the selectedNodeId to set
	 */
	public void setSelectedNodeId(int selectedNodeId) {
		this.selectedNodeId = selectedNodeId;
	}

	/**
	 * Returns the selectedNodeType.
	 * 
	 * @return the selectedNodeType
	 */
	public NodeType getSelectedNodeType() {
		return selectedNodeType;
	}

	/**
	 * Sets the selectedNodeType.
	 * 
	 * @param selectedNodeType
	 *            the selectedNodeType to set
	 */
	public void setSelectedNodeType(NodeType selectedNodeType) {
		this.selectedNodeType = selectedNodeType;
	}

	/**
	 * Returns the selectedNodeName.
	 * 
	 * @return the selectedNodeName
	 */
	public String getSelectedNodeName() {
		return selectedNodeName;
	}

	/**
	 * Sets the selectedNodeName.
	 * 
	 * @param selectedNodeName
	 *            the selectedNodeName to set
	 */
	public void setSelectedNodeName(String selectedNodeName) {
		this.selectedNodeName = selectedNodeName;
	}

	/**
	 * Returns the subpageIntegrator.
	 * 
	 * @return the subpageIntegrator
	 */
	public SubpageIntegrator getSubpageIntegrator() {
		return subpageIntegrator;
	}

	/**
	 * Updates current node type, id and name values.
	 * 
	 * @param nodeType
	 *            the node type
	 * @param nodeName
	 *            the node name
	 * @param nodeId
	 *            the node id
	 */
	public void update(NodeType nodeType, String nodeName, String nodeId) {
		selectedNodeType = nodeType;
		selectedNodeName = nodeName;
		selectedNodeId = Integer.parseInt(nodeId);
	}

	/**
	 * Sets the hostError.
	 * 
	 * @param hostError
	 *            the hostError to set
	 */
	public void setHostError(boolean hostError) {
		this.hostError = hostError;
	}

	/**
	 * Returns the hostError.
	 * 
	 * @return the hostError
	 */
	public boolean isHostError() {
		return hostError;
	}

	/**
	 * Sets the hostInfo.
	 * 
	 * @param hostInfo
	 *            the hostInfo to set
	 */
	public void setHostInfo(boolean hostInfo) {
		this.hostInfo = hostInfo;
	}

	/**
	 * Returns the hostInfo.
	 * 
	 * @return the hostInfo
	 */
	public boolean isHostInfo() {
		return hostInfo;
	}

	/**
	 * Sets the hostMessage.
	 * 
	 * @param hostMessage
	 *            the hostMessage to set
	 */
	public void setHostMessage(boolean hostMessage) {
		this.hostMessage = hostMessage;
	}

	/**
	 * Returns the hostMessage.
	 * 
	 * @return the hostMessage
	 */
	public boolean isHostMessage() {
		return hostMessage;
	}

	/**
	 * Sets the hostInfoMessage.
	 * 
	 * @param hostInfoMessage
	 *            the hostInfoMessage to set
	 */
	public void setHostInfoMessage(String hostInfoMessage) {
		this.hostInfoMessage = hostInfoMessage;
	}

	/**
	 * Returns the hostInfoMessage.
	 * 
	 * @return the hostInfoMessage
	 */
	public String getHostInfoMessage() {
		return hostInfoMessage;
	}

	/**
	 * Sets the hostErrorMessage.
	 * 
	 * @param hostErrorMessage
	 *            the hostErrorMessage to set
	 */
	public void setHostErrorMessage(String hostErrorMessage) {
		this.hostErrorMessage = hostErrorMessage;
	}

	/**
	 * Returns the hostErrorMessage.
	 * 
	 * @return the hostErrorMessage
	 */
	public String getHostErrorMessage() {
		return hostErrorMessage;
	}

	/**
	 * Sets the serviceError.
	 * 
	 * @param serviceError
	 *            the serviceError to set
	 */
	public void setServiceError(boolean serviceError) {
		this.serviceError = serviceError;
	}

	/**
	 * Returns the serviceError.
	 * 
	 * @return the serviceError
	 */
	public boolean isServiceError() {
		return serviceError;
	}

	/**
	 * Sets the serviceInfo.
	 * 
	 * @param serviceInfo
	 *            the serviceInfo to set
	 */
	public void setServiceInfo(boolean serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	/**
	 * Returns the serviceInfo.
	 * 
	 * @return the serviceInfo
	 */
	public boolean isServiceInfo() {
		return serviceInfo;
	}

	/**
	 * Sets the serviceMessage.
	 * 
	 * @param serviceMessage
	 *            the serviceMessage to set
	 */
	public void setServiceMessage(boolean serviceMessage) {
		this.serviceMessage = serviceMessage;
	}

	/**
	 * Returns the serviceMessage.
	 * 
	 * @return the serviceMessage
	 */
	public boolean isServiceMessage() {
		return serviceMessage;
	}

	/**
	 * Sets the serviceInfoMessage.
	 * 
	 * @param serviceInfoMessage
	 *            the serviceInfoMessage to set
	 */
	public void setServiceInfoMessage(String serviceInfoMessage) {
		this.serviceInfoMessage = serviceInfoMessage;
	}

	/**
	 * Returns the serviceInfoMessage.
	 * 
	 * @return the serviceInfoMessage
	 */
	public String getServiceInfoMessage() {
		return serviceInfoMessage;
	}

	/**
	 * Sets the serviceErrorMessage.
	 * 
	 * @param serviceErrorMessage
	 *            the serviceErrorMessage to set
	 */
	public void setServiceErrorMessage(String serviceErrorMessage) {
		this.serviceErrorMessage = serviceErrorMessage;
	}

	/**
	 * Returns the serviceErrorMessage.
	 * 
	 * @return the serviceErrorMessage
	 */
	public String getServiceErrorMessage() {
		return serviceErrorMessage;
	}

	/**
	 * Sets the hgError.
	 * 
	 * @param hgError
	 *            the hgError to set
	 */
	public void setHgError(boolean hgError) {
		this.hgError = hgError;
	}

	/**
	 * Returns the hgError.
	 * 
	 * @return the hgError
	 */
	public boolean isHgError() {
		return hgError;
	}

	/**
	 * Sets the hgInfo.
	 * 
	 * @param hgInfo
	 *            the hgInfo to set
	 */
	public void setHgInfo(boolean hgInfo) {
		this.hgInfo = hgInfo;
	}

	/**
	 * Returns the hgInfo.
	 * 
	 * @return the hgInfo
	 */
	public boolean isHgInfo() {
		return hgInfo;
	}

	/**
	 * Sets the hgMessage.
	 * 
	 * @param hgMessage
	 *            the hgMessage to set
	 */
	public void setHgMessage(boolean hgMessage) {
		this.hgMessage = hgMessage;
	}

	/**
	 * Returns the hgMessage.
	 * 
	 * @return the hgMessage
	 */
	public boolean isHgMessage() {
		return hgMessage;
	}

	/**
	 * Sets the hgInfoMessage.
	 * 
	 * @param hgInfoMessage
	 *            the hgInfoMessage to set
	 */
	public void setHgInfoMessage(String hgInfoMessage) {
		this.hgInfoMessage = hgInfoMessage;
	}

	/**
	 * Returns the hgInfoMessage.
	 * 
	 * @return the hgInfoMessage
	 */
	public String getHgInfoMessage() {
		return hgInfoMessage;
	}

	/**
	 * Sets the hgErrorMessage.
	 * 
	 * @param hgErrorMessage
	 *            the hgErrorMessage to set
	 */
	public void setHgErrorMessage(String hgErrorMessage) {
		this.hgErrorMessage = hgErrorMessage;
	}

	/**
	 * Returns the hgErrorMessage.
	 * 
	 * @return the hgErrorMessage
	 */
	public String getHgErrorMessage() {
		return hgErrorMessage;
	}

	/**
	 * Sets the sgError.
	 * 
	 * @param sgError
	 *            the sgError to set
	 */
	public void setSgError(boolean sgError) {
		this.sgError = sgError;
	}

	/**
	 * Returns the sgError.
	 * 
	 * @return the sgError
	 */
	public boolean isSgError() {
		return sgError;
	}

	/**
	 * Sets the sgInfo.
	 * 
	 * @param sgInfo
	 *            the sgInfo to set
	 */
	public void setSgInfo(boolean sgInfo) {
		this.sgInfo = sgInfo;
	}

	/**
	 * Returns the sgInfo.
	 * 
	 * @return the sgInfo
	 */
	public boolean isSgInfo() {
		return sgInfo;
	}

	/**
	 * Sets the sgMessage.
	 * 
	 * @param sgMessage
	 *            the sgMessage to set
	 */
	public void setSgMessage(boolean sgMessage) {
		this.sgMessage = sgMessage;
	}

	/**
	 * Returns the sgMessage.
	 * 
	 * @return the sgMessage
	 */
	public boolean isSgMessage() {
		return sgMessage;
	}

	/**
	 * Sets the sgInfoMessage.
	 * 
	 * @param sgInfoMessage
	 *            the sgInfoMessage to set
	 */
	public void setSgInfoMessage(String sgInfoMessage) {
		this.sgInfoMessage = sgInfoMessage;
	}

	/**
	 * Returns the sgInfoMessage.
	 * 
	 * @return the sgInfoMessage
	 */
	public String getSgInfoMessage() {
		return sgInfoMessage;
	}

	/**
	 * Sets the sgErrorMessage.
	 * 
	 * @param sgErrorMessage
	 *            the sgErrorMessage to set
	 */
	public void setSgErrorMessage(String sgErrorMessage) {
		this.sgErrorMessage = sgErrorMessage;
	}

	/**
	 * Returns the sgErrorMessage.
	 * 
	 * @return the sgErrorMessage
	 */
	public String getSgErrorMessage() {
		return sgErrorMessage;
	}

	/**
	 * Sets the statisticsModelPopUpListBean.
	 * 
	 * @param statisticsModelPopUpListBean
	 *            the statisticsModelPopUpListBean to set
	 */
	public void setStatisticsModelPopUpListBean(
			StatisticsModelPopUpListBean statisticsModelPopUpListBean) {
		this.statisticsModelPopUpListBean = statisticsModelPopUpListBean;
	}

	/**
	 * Returns the statisticsModelPopUpListBean.
	 * 
	 * @return the statisticsModelPopUpListBean
	 */
	public StatisticsModelPopUpListBean getStatisticsModelPopUpListBean() {
		return statisticsModelPopUpListBean;
	}

}
