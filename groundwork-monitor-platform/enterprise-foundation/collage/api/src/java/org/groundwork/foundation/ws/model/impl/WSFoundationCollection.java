/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.model.impl;

public class WSFoundationCollection implements java.io.Serializable {
	/* Required for serialization */
	static final long serialVersionUID = 1;

	// Total count of potential matches used when retrieving "pages" of data
	private int totalCount = 0;

	private LogMessage[] logMessage;

	private HostStatus[] hostStatus;

	private ServiceStatus[] serviceStatus;

	private MonitorStatus[] monitorStatus;

	private Device[] device;

	private Host[] host;

	private HostGroup[] hostGroup;

	private HostGroupInfo[] hostGroupInfo;

	private StateStatistics[] stateStatisticCollection;

	private HostGroupStatisticProperty[] hostGroupStatisticProperties;

	private NagiosStatisticProperty[] nagiosStatisticCollection;

	private StatisticProperty[] statisticCollection;

	private AttributeData[] attributeData;

	private EntityType[] entityType;

	private EntityTypeProperty[] entityTypeProperty;

	private PropertyTypeBinding[] propertyTypeBinding;

	private Action[] action;

	private ActionReturn[] actionReturn;

	private String[] stringData;

	private StateTransition[] stateTransition;

	private Category[] category;

	private CategoryEntity[] categoryEntity;

	private IntegerProperty sessionID;

	private RRDGraph[] rrdGraph;

	private SimpleHost[] simpleHost;

	private SimpleServiceStatus[] simpleService;

	public WSFoundationCollection() {
	}

	public WSFoundationCollection(int totalCount, LogMessage[] logMessages) {
		this.totalCount = totalCount;
		this.logMessage = logMessages;
	}

	public WSFoundationCollection(int totalCount, Device[] devices) {
		this.totalCount = totalCount;
		this.device = devices;
	}

	public WSFoundationCollection(int totalCount, HostGroup[] hostGroups) {
		this.totalCount = totalCount;
		this.hostGroup = hostGroups;
	}

	public WSFoundationCollection(HostGroupInfo[] hostGroupInfo) {
		if (hostGroupInfo != null)
			this.totalCount = hostGroupInfo.length;

		this.hostGroupInfo = hostGroupInfo;
	}

	public WSFoundationCollection(int totalCount, Host[] hosts) {
		this.totalCount = totalCount;
		this.host = hosts;
	}

	public WSFoundationCollection(int totalCount, ServiceStatus[] serviceStatus) {
		this.totalCount = totalCount;
		this.serviceStatus = serviceStatus;
	}

	public WSFoundationCollection(int totalCount, EntityType[] entityType) {
		this.totalCount = totalCount;
		this.entityType = entityType;
	}

	public WSFoundationCollection(int totalCount,
			EntityTypeProperty[] entityTypeProperty) {
		this.totalCount = totalCount;
		this.entityTypeProperty = entityTypeProperty;
	}

	public WSFoundationCollection(int totalCount,
			PropertyTypeBinding[] propertyTypeBinding) {
		this.totalCount = totalCount;
		this.propertyTypeBinding = propertyTypeBinding;
	}

	public WSFoundationCollection(StateStatistics[] statistics) {
		if (statistics != null)
			this.totalCount = statistics.length;

		this.stateStatisticCollection = statistics;
	}

	public WSFoundationCollection(
			HostGroupStatisticProperty[] hostGroupStatistics) {
		if (hostGroupStatistics != null)
			this.totalCount = hostGroupStatistics.length;

		this.hostGroupStatisticProperties = hostGroupStatistics;
	}

	public WSFoundationCollection(NagiosStatisticProperty[] nagiosStatistics) {
		if (nagiosStatistics != null)
			this.totalCount = nagiosStatistics.length;

		this.nagiosStatisticCollection = nagiosStatistics;
	}

	public WSFoundationCollection(StatisticProperty[] stats) {
		if (stats != null)
			this.totalCount = stats.length;

		this.statisticCollection = stats;
	}

	public WSFoundationCollection(AttributeData[] attributeData) {
		if (attributeData != null)
			this.totalCount = attributeData.length;

		this.attributeData = attributeData;
	}

	public WSFoundationCollection(int totalCount, Action[] actions) {
		this.totalCount = totalCount;
		this.action = actions;
	}

	public WSFoundationCollection(int totalCount, ActionReturn[] actionReturn) {
		this.totalCount = totalCount;
		this.actionReturn = actionReturn;
	}

	public WSFoundationCollection(int totalCount, String[] stringData) {
		this.totalCount = totalCount;
		this.stringData = stringData;
	}

	public WSFoundationCollection(int totalCount,
			StateTransition[] stateTransition) {
		this.totalCount = totalCount;
		this.stateTransition = stateTransition;
	}

	public WSFoundationCollection(int totalCount, Category[] category) {
		this.totalCount = totalCount;
		this.category = category;
	}

	public WSFoundationCollection(int totalCount,
			CategoryEntity[] categoryEntity) {
		this.totalCount = totalCount;
		this.categoryEntity = categoryEntity;
	}

	public WSFoundationCollection(int totalCount, RRDGraph[] rrdGraph) {
		this.totalCount = totalCount;
		this.rrdGraph = rrdGraph;
	}

	public WSFoundationCollection(int totalCount, SimpleHost[] simpleHost) {
		this.totalCount = totalCount;
		this.simpleHost = simpleHost;
	}

	public WSFoundationCollection(int totalCount,
			SimpleServiceStatus[] simpleService) {
		this.totalCount = totalCount;
		this.simpleService = simpleService;
	}

	public WSFoundationCollection(IntegerProperty sessionID) {
		this.totalCount = 1;
		this.sessionID = sessionID;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return the sessionID
	 */
	public IntegerProperty getSessionID() {
		return sessionID;
	}

	/**
	 * @param sessionID
	 *            the sessionID to set
	 */
	public void setSessionID(IntegerProperty sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * Gets the logMessage value for this WSFoundationCollection.
	 * 
	 * @return logMessage
	 */
	public LogMessage[] getLogMessage() {
		return logMessage;
	}

	/**
	 * Sets the logMessage value for this WSFoundationCollection.
	 * 
	 * @param logMessage
	 */
	public void setLogMessage(
			org.groundwork.foundation.ws.model.impl.LogMessage[] logMessage) {
		this.logMessage = logMessage;
	}

	public LogMessage getLogMessage(int i) {
		return this.logMessage[i];
	}

	public void setLogMessage(int i,
			org.groundwork.foundation.ws.model.impl.LogMessage _value) {
		this.logMessage[i] = _value;
	}

	/**
	 * Gets the hostStatus value for this WSFoundationCollection.
	 * 
	 * @return hostStatus
	 */
	public HostStatus[] getHostStatus() {
		return hostStatus;
	}

	/**
	 * Sets the hostStatus value for this WSFoundationCollection.
	 * 
	 * @param hostStatus
	 */
	public void setHostStatus(
			org.groundwork.foundation.ws.model.impl.HostStatus[] hostStatus) {
		this.hostStatus = hostStatus;
	}

	public HostStatus getHostStatus(int i) {
		return this.hostStatus[i];
	}

	public void setHostStatus(int i,
			org.groundwork.foundation.ws.model.impl.HostStatus _value) {
		this.hostStatus[i] = _value;
	}

	/**
	 * Gets the serviceStatus value for this WSFoundationCollection.
	 * 
	 * @return serviceStatus
	 */
	public ServiceStatus[] getServiceStatus() {
		return serviceStatus;
	}

	/**
	 * Sets the serviceStatus value for this WSFoundationCollection.
	 * 
	 * @param serviceStatus
	 */
	public void setServiceStatus(
			org.groundwork.foundation.ws.model.impl.ServiceStatus[] serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	public ServiceStatus getServiceStatus(int i) {
		return this.serviceStatus[i];
	}

	public void setServiceStatus(int i,
			org.groundwork.foundation.ws.model.impl.ServiceStatus _value) {
		this.serviceStatus[i] = _value;
	}

	/**
	 * Gets the monitorStatus value for this WSFoundationCollection.
	 * 
	 * @return monitorStatus
	 */
	public MonitorStatus[] getMonitorStatus() {
		return monitorStatus;
	}

	/**
	 * Sets the monitorStatus value for this WSFoundationCollection.
	 * 
	 * @param monitorStatus
	 */
	public void setMonitorStatus(
			org.groundwork.foundation.ws.model.impl.MonitorStatus[] monitorStatus) {
		this.monitorStatus = monitorStatus;
	}

	public MonitorStatus getMonitorStatus(int i) {
		return this.monitorStatus[i];
	}

	public void setMonitorStatus(int i,
			org.groundwork.foundation.ws.model.impl.MonitorStatus _value) {
		this.monitorStatus[i] = _value;
	}

	/**
	 * Gets the device value for this WSFoundationCollection.
	 * 
	 * @return device
	 */
	public Device[] getDevice() {
		return device;
	}

	/**
	 * Sets the device value for this WSFoundationCollection.
	 * 
	 * @param device
	 */
	public void setDevice(
			org.groundwork.foundation.ws.model.impl.Device[] device) {
		this.device = device;
	}

	public Device getDevice(int i) {
		return this.device[i];
	}

	public void setDevice(int i,
			org.groundwork.foundation.ws.model.impl.Device _value) {
		this.device[i] = _value;
	}

	/**
	 * Gets the host value for this WSFoundationCollection.
	 * 
	 * @return host
	 */
	public Host[] getHost() {
		return host;
	}

	/**
	 * Sets the host value for this WSFoundationCollection.
	 * 
	 * @param host
	 */
	public void setHost(Host[] host) {
		this.host = host;
	}

	public Host getHost(int i) {
		return this.host[i];
	}

	public void setHost(int i, Host _value) {
		this.host[i] = _value;
	}

	/**
	 * Gets the hostGroup value for this WSFoundationCollection.
	 * 
	 * @return hostGroup
	 */
	public HostGroup[] getHostGroup() {
		return hostGroup;
	}

	/**
	 * Sets the hostGroup value for this WSFoundationCollection.
	 * 
	 * @param hostGroup
	 */
	public void setHostGroup(HostGroup[] hostGroup) {
		this.hostGroup = hostGroup;
	}

	public HostGroup getHostGroup(int i) {
		return this.hostGroup[i];
	}

	public void setHostGroup(int i, HostGroup _value) {
		this.hostGroup[i] = _value;
	}

	/**
	 * Gets the hostGroupInfo value for this WSFoundationCollection.
	 * 
	 * @return hostGroupInfo
	 */
	public HostGroupInfo[] getHostGroupInfo() {
		return hostGroupInfo;
	}

	/**
	 * Sets the hostGroupInfo value for this WSFoundationCollection.
	 * 
	 * @param hostGroupInfo
	 */
	public void setHostGroupInfo(HostGroupInfo[] hostGroupInfo) {
		this.hostGroupInfo = hostGroupInfo;
	}

	public HostGroupInfo getHostGroupInfo(int i) {
		return this.hostGroupInfo[i];
	}

	public void setHostGroupInfo(int i, HostGroupInfo _value) {
		this.hostGroupInfo[i] = _value;
	}

	/**
	 * Gets the hostGroupStatisticCollection value for this
	 * WSFoundationCollection.
	 * 
	 * @return hostGroupStatisticCollection
	 */
	public StateStatistics[] getStateStatisticCollection() {
		return stateStatisticCollection;
	}

	/**
	 * Sets the hostGroupStatisticCollection value for this
	 * WSFoundationCollection.
	 * 
	 * @param hostGroupStatisticCollection
	 */
	public void setStateStatisticCollection(
			StateStatistics[] stateStatisticCollection) {
		this.stateStatisticCollection = stateStatisticCollection;
	}

	public StateStatistics getStateStatisticCollection(int i) {
		return this.stateStatisticCollection[i];
	}

	public void setstateStatisticCollection(int i, StateStatistics _value) {
		this.stateStatisticCollection[i] = _value;
	}

	/**
	 * Gets the nagiosStatisticCollection value for this WSFoundationCollection.
	 * 
	 * @return nagiosStatisticCollection
	 */
	public NagiosStatisticProperty[] getNagiosStatisticCollection() {
		return nagiosStatisticCollection;
	}

	/**
	 * Sets the nagiosStatisticCollection value for this WSFoundationCollection.
	 * 
	 * @param nagiosStatisticCollection
	 */
	public void setNagiosStatisticCollection(
			NagiosStatisticProperty[] nagiosStatisticCollection) {
		this.nagiosStatisticCollection = nagiosStatisticCollection;
	}

	public NagiosStatisticProperty getNagiosStatisticCollection(int i) {
		return this.nagiosStatisticCollection[i];
	}

	public void setNagiosStatisticCollection(int i,
			NagiosStatisticProperty _value) {
		this.nagiosStatisticCollection[i] = _value;
	}

	/**
	 * Gets the statisticCollection value for this WSFoundationCollection.
	 * 
	 * @return StatisticProperty[]
	 */
	public StatisticProperty[] getStatisticCollection() {
		return statisticCollection;
	}

	/**
	 * Sets the statisticCollection value for this WSFoundationCollection.
	 * 
	 * @param stats
	 */
	public void setStatisticCollection(StatisticProperty[] stats) {
		this.statisticCollection = stats;
	}

	public StatisticProperty getStatisticCollection(int i) {
		return this.statisticCollection[i];
	}

	public void setSatisticCollection(int i, StatisticProperty _value) {
		this.statisticCollection[i] = _value;
	}

	/**
	 * Gets the hostGroupStaticProperty collection value for this
	 * WSFoundationCollection.
	 * 
	 * @return StatisticProperty[]
	 */
	public HostGroupStatisticProperty[] getHostGroupStatisticProperties() {
		return hostGroupStatisticProperties;
	}

	/**
	 * Sets the hostGroupStaticProperty collection value for this
	 * WSFoundationCollection.
	 * 
	 * @param stats
	 */
	public void setHostGroupStatisticProperties(
			HostGroupStatisticProperty[] stats) {
		this.hostGroupStatisticProperties = stats;
	}

	public HostGroupStatisticProperty getHostGroupStatisticProperty(int i) {
		return this.hostGroupStatisticProperties[i];
	}

	public void setHostGroupStatisticProperties(int i,
			HostGroupStatisticProperty _value) {
		this.hostGroupStatisticProperties[i] = _value;
	}

	/**
	 * Gets the attribute data value for this WSFoundationCollection.
	 * 
	 * @return attributeData
	 */
	public AttributeData[] getAttributeData() {
		return attributeData;
	}

	/**
	 * Sets the attribute data value for this WSFoundationCollection.
	 * 
	 * @param attribute
	 *            data
	 */
	public void setAttributeData(AttributeData[] attributeData) {
		this.attributeData = attributeData;
	}

	public AttributeData getAttributeData(int i) {
		return this.attributeData[i];
	}

	public void setAttributeData(int i, AttributeData _value) {
		this.attributeData[i] = _value;
	}

	/**
	 * Gets the entity type value for this WSFoundationCollection.
	 * 
	 * @return entityType
	 */
	public EntityType[] getEntityType() {
		return entityType;
	}

	/**
	 * Sets the entity type value for this WSFoundationCollection.
	 * 
	 * @param entityType
	 */
	public void setEntityType(EntityType[] entityType) {
		this.entityType = entityType;
	}

	public EntityType getEntityType(int i) {
		return this.entityType[i];
	}

	public void setEntityType(int i, EntityType _value) {
		this.entityType[i] = _value;
	}

	/**
	 * Gets the entity type property value for this WSFoundationCollection.
	 * 
	 * @return entityType
	 */
	public EntityTypeProperty[] getEntityTypeProperty() {
		return entityTypeProperty;
	}

	/**
	 * Sets the entity type value for this WSFoundationCollection.
	 * 
	 * @param entityType
	 */
	public void setEntityTypeProperty(EntityTypeProperty[] entityTypeProperty) {
		this.entityTypeProperty = entityTypeProperty;
	}

	public EntityTypeProperty getEntityTypeProperty(int i) {
		return this.entityTypeProperty[i];
	}

	public void setEntityTypeProperty(int i, EntityTypeProperty _value) {
		this.entityTypeProperty[i] = _value;
	}

	/**
	 * Gets the entity type property value for this WSFoundationCollection.
	 * 
	 * @return entityType
	 */
	public PropertyTypeBinding[] getPropertyTypeBinding() {
		return propertyTypeBinding;
	}

	/**
	 * Sets the entity type value for this WSFoundationCollection.
	 * 
	 * @param entityType
	 */
	public void setPropertyTypeBinding(PropertyTypeBinding[] propertyTypeBinding) {
		this.propertyTypeBinding = propertyTypeBinding;
	}

	public PropertyTypeBinding getPropertyTypeBinding(int i) {
		return this.propertyTypeBinding[i];
	}

	public void setPropertyTypeBinding(int i, PropertyTypeBinding _value) {
		this.propertyTypeBinding[i] = _value;
	}

	/**
	 * Gets the Action property value for this WSFoundationCollection.
	 * 
	 * @return action
	 */
	public Action[] getAction() {
		return action;
	}

	/**
	 * Sets the action value for this WSFoundationCollection.
	 * 
	 * @param entityType
	 */
	public void setAction(Action[] action) {
		this.action = action;
	}

	public Action getAction(int i) {
		return this.action[i];
	}

	public void setAction(int i, Action _value) {
		this.action[i] = _value;
	}

	/**
	 * Gets the ActionReturn property value for this WSFoundationCollection.
	 * 
	 * @return action
	 */
	public ActionReturn[] getActionReturn() {
		return actionReturn;
	}

	/**
	 * Sets the ActionReturn value for this WSFoundationCollection.
	 * 
	 * @param entityType
	 */
	public void setActionReturn(ActionReturn[] actionReturn) {
		this.actionReturn = actionReturn;
	}

	public ActionReturn getActionReturn(int i) {
		return this.actionReturn[i];
	}

	public void setActionReturn(int i, ActionReturn _value) {
		this.actionReturn[i] = _value;
	}

	public String getStringData(int i) {
		return this.stringData[i];
	}

	public void setStringData(int i, String _value) {
		this.stringData[i] = _value;
	}

	public String[] getStringData() {
		return stringData;
	}

	public StateTransition getStateTransition(int i) {
		return this.stateTransition[i];
	}

	public void setStateTransition(int i, StateTransition _value) {
		this.stateTransition[i] = _value;
	}

	public void setStateTransition(StateTransition[] stateTransition) {
		this.stateTransition = stateTransition;
	}

	public StateTransition[] getStateTransition() {
		return stateTransition;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof WSFoundationCollection))
			return false;
		WSFoundationCollection other = (WSFoundationCollection) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& this.totalCount == other.getTotalCount()
				&& ((this.logMessage == null && other.getLogMessage() == null) || (this.logMessage != null && java.util.Arrays
						.equals(this.logMessage, other.getLogMessage())))
				&& ((this.hostStatus == null && other.getHostStatus() == null) || (this.hostStatus != null && java.util.Arrays
						.equals(this.hostStatus, other.getHostStatus())))
				&& ((this.serviceStatus == null && other.getServiceStatus() == null) || (this.serviceStatus != null && java.util.Arrays
						.equals(this.serviceStatus, other.getServiceStatus())))
				&& ((this.monitorStatus == null && other.getMonitorStatus() == null) || (this.monitorStatus != null && java.util.Arrays
						.equals(this.monitorStatus, other.getMonitorStatus())))
				&& ((this.device == null && other.getDevice() == null) || (this.device != null && java.util.Arrays
						.equals(this.device, other.getDevice())))
				&& ((this.host == null && other.getHost() == null) || (this.host != null && java.util.Arrays
						.equals(this.host, other.getHost())))
				&& ((this.hostGroup == null && other.getHostGroup() == null) || (this.hostGroup != null && java.util.Arrays
						.equals(this.hostGroup, other.getHostGroup())))
				&& ((this.stateStatisticCollection == null && other
						.getStateStatisticCollection() == null) || (this.stateStatisticCollection != null && java.util.Arrays
						.equals(this.stateStatisticCollection, other
								.getStateStatisticCollection())))
				&& ((this.statisticCollection == null && other
						.getStatisticCollection() == null) || (this.statisticCollection != null && java.util.Arrays
						.equals(this.statisticCollection, other
								.getStatisticCollection())))
				&& ((this.hostGroupStatisticProperties == null && other
						.getHostGroupStatisticProperties() == null) || (this.hostGroupStatisticProperties != null && java.util.Arrays
						.equals(this.hostGroupStatisticProperties, other
								.getHostGroupStatisticProperties())))
				&& ((this.nagiosStatisticCollection == null && other
						.getNagiosStatisticCollection() == null) || (this.nagiosStatisticCollection != null && java.util.Arrays
						.equals(this.nagiosStatisticCollection, other
								.getNagiosStatisticCollection())))
				&& ((this.attributeData == null && other.getAttributeData() == null) || (this.attributeData != null && java.util.Arrays
						.equals(this.attributeData, other.getAttributeData())))
				&& ((this.propertyTypeBinding == null && other
						.getPropertyTypeBinding() == null) || (this.propertyTypeBinding != null && java.util.Arrays
						.equals(this.entityTypeProperty, other
								.getPropertyTypeBinding())))
				&& ((this.entityTypeProperty == null && other
						.getEntityTypeProperty() == null) || (this.entityTypeProperty != null && java.util.Arrays
						.equals(this.entityTypeProperty, other
								.getEntityTypeProperty())))
				&& ((this.entityType == null && other.getEntityType() == null) || (this.entityType != null && java.util.Arrays
						.equals(this.entityType, other.getEntityType())))
				&& ((this.action == null && other.getAction() == null) || (this.action != null && java.util.Arrays
						.equals(this.action, other.getAction())))
				&& ((this.actionReturn == null && other.getActionReturn() == null) || (this.actionReturn != null && java.util.Arrays
						.equals(this.actionReturn, other.getActionReturn())))
				&& ((this.stringData == null && other.getStringData() == null) || (this.stringData != null && java.util.Arrays
						.equals(this.stringData, other.getStringData())))
				&& ((this.stateTransition == null && other.getStateTransition() == null) || (this.stateTransition != null && java.util.Arrays
						.equals(this.stateTransition, other
								.getStateTransition())))
				&& ((this.category == null && other.getCategory() == null) || (this.category != null && java.util.Arrays
						.equals(this.category, other.getCategory())))
				&& ((this.categoryEntity == null && other.getCategoryEntity() == null) || (this.categoryEntity != null && java.util.Arrays
						.equals(this.categoryEntity, other.getCategoryEntity())))
				&& ((this.rrdGraph == null && other.getRrdGraph() == null) || (this.rrdGraph != null && java.util.Arrays
						.equals(this.rrdGraph, other.getRrdGraph())))
				&& ((this.simpleHost == null && other.getSimpleHost() == null) || (this.simpleHost != null && java.util.Arrays
						.equals(this.simpleHost, other.getSimpleHost())))
				&& ((this.simpleService == null && other.getSimpleService() == null) || (this.simpleService != null && java.util.Arrays
						.equals(this.simpleService, other.getSimpleService())));

		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1 + totalCount;

		if (getLogMessage() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getLogMessage()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getLogMessage(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getHostStatus() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getHostStatus()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getHostStatus(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getServiceStatus() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getServiceStatus()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getServiceStatus(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getMonitorStatus() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getMonitorStatus()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getMonitorStatus(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getDevice() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getDevice()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getDevice(),
						i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getHost() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getHost()); i++) {
				java.lang.Object obj = java.lang.reflect.Array
						.get(getHost(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getHostGroup() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getHostGroup()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getHostGroup(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getStateStatisticCollection() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getStateStatisticCollection()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getStateStatisticCollection(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getNagiosStatisticCollection() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getNagiosStatisticCollection()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getNagiosStatisticCollection(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getStatisticCollection() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getStatisticCollection()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getStatisticCollection(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getHostGroupStatisticProperties() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getHostGroupStatisticProperties()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getHostGroupStatisticProperties(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getAttributeData() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getAttributeData()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getAttributeData(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getHostGroupInfo() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getHostGroupInfo()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getHostGroupInfo(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getEntityTypeProperty() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getEntityTypeProperty()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getEntityTypeProperty(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getEntityType() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getEntityType()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getEntityType(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getPropertyTypeBinding() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getPropertyTypeBinding()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getPropertyTypeBinding(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getAction() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getAction()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getAction(),
						i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getActionReturn() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getActionReturn()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getActionReturn(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getStringData() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getStringData()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getStringData(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getStateTransition() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getStateTransition()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getStateTransition(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getCategory() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getCategory()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getCategory(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getCategoryEntity() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getCategoryEntity()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getCategoryEntity(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getRrdGraph() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getRrdGraph()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getRrdGraph(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getSimpleHost() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getSimpleHost()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getSimpleHost(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}

		if (getSimpleService() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getSimpleService()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getSimpleService(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			WSFoundationCollection.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("totalCount");
		elemField.setXmlName(new javax.xml.namespace.QName("", "TotalCount"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setMinOccurs(1);
		elemField.setNillable(true);
		elemField.setMaxOccurs(1);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("logMessage");
		elemField.setXmlName(new javax.xml.namespace.QName("", "LogMessage"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "LogMessage"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("hostStatus");
		elemField.setXmlName(new javax.xml.namespace.QName("", "HostStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "HostStatus"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("serviceStatus");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "ServiceStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "ServiceStatus"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("monitorStatus");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "MonitorStatus"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("device");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Device"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "Device"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("host");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Host"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "Host"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("hostGroup");
		elemField.setXmlName(new javax.xml.namespace.QName("", "HostGroup"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "HostGroup"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("hostGroupInfo");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "HostGroupInfo"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "HostGroupInfo"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("stateStatisticCollection");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"StateStatistics"));
		elemField
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"StateStatistics"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("nagiosStatisticCollection");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"NagiosStatisticCollection"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"NagiosStatisticProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("statisticCollection");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"StatisticCollection"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"StatisticProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("hostGroupStatisticProperties");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"HostGroupStatisticProperties"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"StatisticProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("sessionID");
		elemField.setXmlName(new javax.xml.namespace.QName("", "SessionID"));
		elemField
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"IntegerProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("attributeData");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "AttributeData"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "AttributeData"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("entityType");
		elemField.setXmlName(new javax.xml.namespace.QName("", "EntityType"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "EntityType"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("entityTypeProperty");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"EntityTypeProperty"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"EntityTypeProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("propertyTypeBinding");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"PropertyTypeBinding"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"PropertyTypeBinding"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("action");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Action"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "Action"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("actionReturn");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ActionReturn"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "ActionReturn"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("stateTransition");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"StateTransition"));
		elemField
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"StateTransition"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("category");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Category"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "Category"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("categoryEntity");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "CategoryEntity"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "CategoryEntity"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("rrdGraph");
		elemField.setXmlName(new javax.xml.namespace.QName("", "RRDGraph"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "RRDGraph"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("simpleHost");
		elemField.setXmlName(new javax.xml.namespace.QName("", "SimpleHost"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "SimpleHost"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("simpleService");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"SimpleServiceStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"SimpleServiceStatus"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);

	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
				_xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
				_xmlType, typeDesc);
	}

	public Category getCategory(int i) {
		return this.category[i];
	}

	public void setCategory(int i, Category _value) {
		this.category[i] = _value;
	}

	public CategoryEntity getCategoryEntity(int i) {
		return this.categoryEntity[i];
	}

	public void setCategoryEntity(int i, CategoryEntity _value) {
		this.categoryEntity[i] = _value;
	}

	public Category[] getCategory() {
		return category;
	}

	public void setCategory(Category[] category) {
		this.category = category;
	}

	public CategoryEntity[] getCategoryEntity() {
		return categoryEntity;
	}

	public void setCategoryEntity(CategoryEntity[] categoryEntity) {
		this.categoryEntity = categoryEntity;
	}

	public RRDGraph[] getRrdGraph() {
		return rrdGraph;
	}

	public void setRrdGraph(RRDGraph[] rrdGraph) {
		this.rrdGraph = rrdGraph;
	}

	public RRDGraph getRrdGraph(int i) {
		return this.rrdGraph[i];
	}

	public void setRrdGraph(int i, RRDGraph _value) {
		this.rrdGraph[i] = _value;
	}

	public SimpleHost[] getSimpleHost() {
		return simpleHost;
	}

	public void setSimpleHost(SimpleHost[] simpleHost) {
		this.simpleHost = simpleHost;
	}

	public SimpleHost getSimpleHost(int i) {
		return this.simpleHost[i];
	}

	public void setSimpleHost(int i, SimpleHost _value) {
		this.simpleHost[i] = _value;
	}

	public SimpleServiceStatus[] getSimpleService() {
		return simpleService;
	}

	public void setSimpleService(SimpleServiceStatus[] simpleService) {
		this.simpleService = simpleService;
	}

	public SimpleServiceStatus getSimpleService(int i) {
		return this.simpleService[i];
	}

	public void setSimpleService(int i, SimpleServiceStatus _value) {
		this.simpleService[i] = _value;
	}

}
