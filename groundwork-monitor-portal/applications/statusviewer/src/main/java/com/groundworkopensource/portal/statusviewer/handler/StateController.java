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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.bean.PerfMeasurementIPCBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.IPCHandlerConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;

/**
 * 
 * A central point of contact for all operations in Status Viewer and other
 * applications.
 * 
 * This Class provides following functions:
 * 
 * - IPC functionality (Wrapper around IPCHandler) - Storing and restoring
 * status of sub pages "tabs" - Some Page Variables
 * 
 * @author nitin_jadhav
 * 
 */
public class StateController {

	/**
	 * zero String constant
	 */
	private static final String ZERO = "0";

	/**
	 * selected node name for Entire network
	 */
	private static final String ENTIRE_NETWORK = "Entire Network";

	/**
	 * Sub page Name
	 */
	private static final String NETWORK_VIEW = "NetworkView";

	/**
	 * IPC_HANDLER managed bean name
	 */
	private static final String IPC_HANDLER = "ipcHandler";

	/**
	 * Node ID.
	 */
	private int selectedNodeID;

	/**
	 * Type of network Object.
	 */
	private NodeType selectedNodeType;

	/**
	 * Node name.
	 */
	private String selectedNodeName;

	/**
	 * Read from web.xml. Current type of application. e.g. StatusViewer and
	 * Dashboard
	 */
	public static String CURRENT_APPLICATION_TYPE;

	/**
	 * CurrentSubpageIdentifier
	 */
	private String currentSubpageIdentifier = null;

	/**
	 * session attribute for host filter
	 */
	private String filterSessionAttribute;

	/**
	 * ipcHandler
	 */
	private IPCHandler ipcHandler = null;

	/**
	 * logger
	 */
	private static final Logger logger = Logger.getLogger(StateController.class
			.getName());

	/**
	 * Portlet Request
	 */
	private PortletRequest portletRequest;

	/**
	 * This method retrieved the parameter value from render request object.
	 * While accessing portlets, the parameters are passed via URL i.e. query
	 * parameters. The interceptor sets these parameters in renderRequest
	 * object. The handler retrieves if from render request and initializes
	 * corresponding bean.
	 * 
	 * @param key
	 * @return Object for ReqParam
	 */
	private Object getRenderReqParam(String key) {
		if (portletRequest != null) {
			return portletRequest.getAttribute(key);
		}
		return null;
	}

	/**
	 * Returns PortletRequest
	 * 
	 * @return PortletRequest
	 */
	private PortletRequest getPortletRequest() {
		PortletRequest request = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (null != facesContext && null != facesContext.getExternalContext()) {
			ExternalContext externalContext = facesContext.getExternalContext();
			request = (PortletRequest) externalContext.getRequest();
		}
		return request;
	}

	/**
	 * Sets Node Id and Node Type from Request.
	 */
	private void setNodeParamsFromRequest() {
		try {
			String svNodeView = (String) getRenderReqParam(IPCHandlerConstants.SV_NODE_TYPE_ATTRIBUTE);
			if (null != svNodeView) {
				setSelectedNodeType(NodeType.getNodeTypeByView(svNodeView));
				String nodeID = (String) getRenderReqParam(IPCHandlerConstants.SV_NODE_ID_ATTRIBUTE);
				setSelectedNodeID(Integer.parseInt(nodeID));
				String nodeName = (String) getRenderReqParam(IPCHandlerConstants.SV_NODE_NAME_ATTRIBUTE);
				setSelectedNodeName(nodeName);
			}

		} catch (NumberFormatException e) {
			logger.error("Ignoring NumberFormatException while getRenderReqParam() and assigning default 0 as node ID.");
		}
	}

	/**
	 * Constructor
	 */
	public StateController() {

		try {
			/* read current application type from web.xml */
			CURRENT_APPLICATION_TYPE = FacesUtils
					.getContextParam(IPCHandlerConstants.FOUNDATION_APPLICATION_TYPE);
		} catch (Exception exc) {
			logger.debug("Exception while determining application type! Fallback to default statusViewer appliction type!");
			CURRENT_APPLICATION_TYPE = "statusViewer";
		}

		portletRequest = getPortletRequest();
		/* set the current subpage identifier */
		currentSubpageIdentifier = getCurrentSubpageIdentifier();
		filterSessionAttribute = createCurrentSessionAttribute();
	}

	/**
	 * Returns SubPageId of current sub-page. SubPageID = SubPageName as per
	 * NodeType + "_" + node Id
	 * 
	 * @return SubPageId of current sub-page
	 */
	private String getCurrentSubpageIdentifier() {
		// parse subpage URL and retrieve selected Node Type as well
		// as Node Id. (use NodeType.getNodeType() method).
		// Also set current NodeType and NodeId after getting.
		setNodeParamsFromRequest();

		// construct subpage identifier
		// return selectedNodeType.getSubPageName()
		// + IPCHandlerConstants.UNDERSCORE + this.selectedNodeID;
		return Constant.EMPTY_STRING + IPCHandlerConstants.UNDERSCORE
				+ this.selectedNodeID;
	}

	/**
	 * Sets the Node ID.
	 * 
	 * @param nodeID
	 *            the selectedNodeId to set
	 */
	public void setSelectedNodeID(int nodeID) {
		this.selectedNodeID = nodeID;
	}

	/**
	 * Returns selected Node ID.
	 * 
	 * @return the selectedNodeId
	 */
	public int getSelectedNodeID() {
		return selectedNodeID;
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
	 * returns selected node Name. it retrieves name from
	 * ReferenceTreeMetaModel, to avoid unnecessary web service calls.
	 * 
	 * @return String name
	 */
	public String getSelectedNodeName() {
		return this.selectedNodeName;
	}

	/**
	 * Sets Node Type.
	 * 
	 * @param nodeType
	 *            the selectedNodeType to set
	 */
	public void setSelectedNodeType(NodeType nodeType) {
		this.selectedNodeType = nodeType;
	}

	/**
	 * Returns selected Node Type.
	 * 
	 * @return the selectedNodeType
	 */
	public NodeType getSelectedNodeType() {
		return selectedNodeType;
	}

	/**
	 * Returns the current application type. This method will be used by each
	 * portlet (afterwards) to display contents as per the application (Status
	 * Viewer / Dashboard / ...).
	 * 
	 * @return the currentApplicationType
	 */
	public String getCurrentApplicationType() {
		return CURRENT_APPLICATION_TYPE;
	}

	/**
	 * Read from web.xml. Current type of application. e.g. StatusViewer and
	 * Dashboard
	 * 
	 * The method for applying filters to other portlets on same sub page.
	 * 
	 * @param hostFilter
	 * @param serviceFilter
	 */

	public void applyFilter(String hostFilter, String serviceFilter) {
		getIPCHandlerInstance().applyFilter(hostFilter, serviceFilter,
				filterSessionAttribute);
	}

	/**
	 * This method create session attribute.
	 * 
	 * @return CurrentSessionAttribute
	 */
	private String createCurrentSessionAttribute() {
		String subPageName = Constant.EMPTY_STRING;
		String nodeName = Constant.EMPTY_STRING;
		String nodeID = Constant.EMPTY_STRING;
		String applicationType = CommonConstants.STATUS_VIEWER_APPLICATION;

		try {
			NodeType nodeType = getSelectedNodeType();
			if (nodeType == null) {
				subPageName = NETWORK_VIEW;
				nodeName = ENTIRE_NETWORK;
				nodeID = ZERO;
			} else {
				subPageName = nodeType.getSubPageName();
				nodeName = getSelectedNodeName();
				nodeID = Integer.toString(getSelectedNodeID());
			}
			// retrieve icesoft attribute to determine if the portlet is in
			// dashboard or status viewer.
			String namespaceRenderReqParam = (String) getRenderReqParam(IPCHandlerConstants.ICESOFT_NAMESPACE);
			if (null != namespaceRenderReqParam
					&& namespaceRenderReqParam
							.contains(CommonConstants.DASHBOARD_APPLICATION)) {
				applicationType = CommonConstants.DASHBOARD_APPLICATION;
			}

		} catch (Exception e) {
			logger.error("Exception while creating session attribute in createCurrentSessionAttribute() method :-"
					+ e);
		}

		return new StringBuilder(applicationType).append(subPageName)
				.append(Constant.UNSERSCORE).append(nodeName)
				.append(Constant.UNSERSCORE).append(nodeID).toString();
	}

	/**
	 * The method for applying time filters from availability portlet to other
	 * perf measurement portlet on same sub page.
	 * 
	 * @param perfmeasurementIPCBean
	 */
	public void applyPerfTimeFilter(
			PerfMeasurementIPCBean perfmeasurementIPCBean) {

		getIPCHandlerInstance().applyPerfTimeFilter(perfmeasurementIPCBean);
	}

	/**
	 * @return PerfMeasurementIPCBean
	 */
	public PerfMeasurementIPCBean getCurrentPerfTimeFilter() {
		PerfMeasurementIPCBean perfMeasurementIPCBean = null;
		if (getIPCHandlerInstance() != null) {
			perfMeasurementIPCBean = getIPCHandlerInstance()
					.getPerfTimeFilter();
		}
		return perfMeasurementIPCBean;
	}

	/**
	 * Returns current sub page specific Host Filter.
	 * 
	 * @return Host Filter
	 */

	public String getCurrentHostFilter() {
		ipcHandler = getIPCHandlerInstance();
		if (ipcHandler != null) {
			return ipcHandler.getHostFilter(filterSessionAttribute);
		}
		return Constant.EMPTY_STRING;
	}

	/**
	 * Returns current sub page specific Service Filter.
	 * 
	 * @return Service Filter
	 */

	public String getCurrentServiceFilter() {
		ipcHandler = getIPCHandlerInstance();
		if (ipcHandler != null) {
			return ipcHandler.getServiceFilter(filterSessionAttribute);
		}
		return Constant.EMPTY_STRING;
	}

	/**
	 * Basic IPC method for sending messages (variables) to other portlets.
	 * 
	 * @param attributeName
	 * @param attributeValue
	 * @param rerenderSession
	 */

	public void addSessionAttribute(String attributeName,
			Object attributeValue, boolean rerenderSession) {
		getIPCHandlerInstance().addSessionAttribute(attributeName,
				attributeValue);
	}

	/**
	 * Returns attribute stored in Session.
	 * 
	 * @param attributeName
	 * @return Attribute stored in Session
	 */
	public Object getSessionAttribute(String attributeName) {
		return getIPCHandlerInstance().getSessionAttribute(attributeName);
	}

	/**
	 * Basic IPC method for deleting variables from session.
	 * 
	 * @param attributeName
	 */

	public void deleteSessionAttribute(String attributeName) {
		getIPCHandlerInstance().deleteSessionAttribute(attributeName,
				PortletSession.APPLICATION_SCOPE);
	}

	/**
	 * Removes filter attributes from session.
	 * 
	 * @param nodeType
	 * @param nodeName
	 * @param nodeID
	 */
	public void removeFilterSessionAttributes(NodeType nodeType,
			String nodeName, String nodeID) {
		String sessionAttribute = new StringBuilder(
				CommonConstants.STATUS_VIEWER_APPLICATION)
				.append(nodeType.getSubPageName()).append(Constant.UNSERSCORE)
				.append(nodeName).append(Constant.UNSERSCORE).append(nodeID)
				.toString();

		deleteSessionAttribute(IPCHandlerConstants.HOST_FILTER
				+ Constant.UNSERSCORE + sessionAttribute);

		deleteSessionAttribute(IPCHandlerConstants.SERVICE_FILTER
				+ Constant.UNSERSCORE + sessionAttribute);
	}

	/**
	 * returns instance of IPChandler from session
	 */
	private IPCHandler getIPCHandlerInstance() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			ipcHandler = (IPCHandler) FacesUtils.getManagedBean(IPC_HANDLER);
			FacesUtils.setFacesContext(facesContext);
		}
		return ipcHandler;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return currentSubpageIdentifier;
	}

	/**
	 * Updates noe type, name and Id
	 * 
	 * @param nodeType
	 * @param nodeName
	 * @param nodeId
	 */
	public void update(NodeType nodeType, String nodeName, int nodeId) {
		setSelectedNodeID(nodeId);
		setSelectedNodeType(nodeType);
		setSelectedNodeName(nodeName);
		filterSessionAttribute = createCurrentSessionAttribute();
	}

}
