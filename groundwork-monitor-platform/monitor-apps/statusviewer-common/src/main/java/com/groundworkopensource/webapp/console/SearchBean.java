/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Date;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.model.impl.AttributeData;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public class SearchBean {

	/**
	 * logger.
	 */
	private static Logger logger = Logger.getLogger(SearchBean.class.getName());

	private String host;
	private String message;
	private String ageType = "preset";
	private Date ageValueFrom;
	private Date ageValueTo;
	private String presetValue;
	private boolean presetRendered = true;
	private boolean customRendered;
	public static final String PRESET_NONE = "none";
	public static final String PRESET_LAST6HR = "last6hr";
	public static final String PRESET_LAST12HR = "last12hr";
	public static final String PRESET_LAST24HR = "last24hr";
	public static final String PRESET_LASTHR = "lasthr";
	public static final String PRESET_LAST10MINS = "last10min";
	public static final String PRESET_LAST30MINS = "last30min";
	private String severity = null;
	private String opStatus = null;
	private String monStatus = null;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets the option items for severity types.
	 * 
	 * @return array of severity type items
	 */
	public SelectItem[] getSeverityItems() {
		SelectItem[] severityItems = null;
		try {
			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			AttributeQueryType queryType = AttributeQueryType.SEVERITIES;
			WSFoundationCollection col = wsCommon.getAttributeData(queryType);
			AttributeData[] severity = col.getAttributeData();
			severityItems = new SelectItem[severity.length + 1];
			int index = 1;
			SelectItem anyItem = new SelectItem();
			anyItem.setLabel("Any");
			anyItem.setValue("");
			severityItems[0] = anyItem;
			for (AttributeData data : severity) {
				SelectItem item = new SelectItem();
				item.setLabel(data.getName());
				item.setValue(data.getName());
				severityItems[index] = item;
				index++;
			} // end for
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
		return severityItems;
	}

	/**
	 * Gets the option items for opStatus types.
	 * 
	 * @return array of opStatus type items
	 */
	public SelectItem[] getOpStatusItems() {
		SelectItem[] opStatusItems = null;
		try {
			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			AttributeQueryType queryType = AttributeQueryType.OPERATION_STATUSES;
			WSFoundationCollection col = wsCommon.getAttributeData(queryType);
			AttributeData[] operationStatus = col.getAttributeData();
			opStatusItems = new SelectItem[operationStatus.length + 1];
			int index = 1;
			SelectItem anyItem = new SelectItem();
			anyItem.setLabel("Any");
			anyItem.setValue("");
			opStatusItems[0] = anyItem;
			for (AttributeData data : operationStatus) {
				SelectItem item = new SelectItem();
				item.setLabel(data.getName());
				item.setValue(data.getName());
				opStatusItems[index] = item;
				index++;
			} // end for
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}

		return opStatusItems;
	}

	/**
	 * Gets the option items for monitorStatus types.
	 * 
	 * @return array of monitorStatus type items
	 */
	public SelectItem[] getMonitorStatusItems() {
		SelectItem[] monitorStatusItems = null;
		try {
			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			AttributeQueryType queryType = AttributeQueryType.MONITOR_STATUSES;
			WSFoundationCollection col = wsCommon.getAttributeData(queryType);
			AttributeData[] monitorStatus = col.getAttributeData();
			monitorStatusItems = new SelectItem[monitorStatus.length+1];			
			SelectItem anyItem = new SelectItem();
			anyItem.setLabel("Any");
			anyItem.setValue("");
			monitorStatusItems[0] = anyItem;
			int index = 1;
			for (AttributeData data : monitorStatus) {
				SelectItem item = new SelectItem();
				item.setLabel(data.getName());
				item.setValue(data.getName());
				monitorStatusItems[index] = item;
				index++;
			} // end for
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
		return monitorStatusItems;
	}

	public String getAgeType() {
		return ageType;
	}

	public void setAgeType(String ageType) {
		this.ageType = ageType;
	}

	public Date getAgeValueFrom() {
		return ageValueFrom;
	}

	public void setAgeValueFrom(Date ageValueFrom) {
		this.ageValueFrom = ageValueFrom;
	}

	public Date getAgeValueTo() {
		return ageValueTo;
	}

	public void setAgeValueTo(Date ageValueTo) {
		this.ageValueTo = ageValueTo;
	}

	public String getPresetValue() {
		return presetValue;
	}

	public void setPresetValue(String presetValue) {
		this.presetValue = presetValue;
	}

	public void selectionChanged(ValueChangeEvent event) {
		if (ageType.equals("preset")) {
			ageType = "custom";
		} else {
			ageType = "preset";
		} // end if
	}

	public boolean isPresetRendered() {
		return presetRendered;
	}

	public void setPresetRendered(boolean presetRendered) {
		this.presetRendered = presetRendered;
	}

	public boolean isCustomRendered() {
		return customRendered;
	}

	public void setCustomRendered(boolean customRendered) {
		this.customRendered = customRendered;
	}

	public void reset() {
		host = null;
		message = null;
		ageType = "preset";
		ageValueFrom = null;
		ageValueTo = null;
		presetValue = null;
		presetRendered = true;
		customRendered = false;
		severity = "Any";
		opStatus = "Any";
		monStatus = "Any";
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getOpStatus() {
		return opStatus;
	}

	public void setOpStatus(String opStatus) {
		this.opStatus = opStatus;
	}

	public String getMonStatus() {
		return monStatus;
	}

	public void setMonStatus(String monStatus) {
		this.monStatus = monStatus;
	}

}
