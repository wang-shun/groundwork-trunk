/*
 * EventQueryManager.java
 *
 * Created on June 6, 2007, 3:25 PM
 * Copyright rdandridge
 */
package com.groundworkopensource.webapp.console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.impl.WSEventServiceLocator;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.LogMessage;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * <p>
 * Application scope data bean for your application. Create properties here to
 * represent cached data that should be made available to all users and pages in
 * the application.
 * </p>
 * 
 * <p>
 * An instance of this class will be created for you automatically, the first
 * time your application evaluates a value binding expression or method binding
 * expression that references a managed bean using this class.
 * </p>
 */
public class EventQueryManager {

	private static Logger logger = Logger.getLogger(EventQueryManager.class
			.getName());
	private WSEventServiceLocator locator = new WSEventServiceLocator();

	/**
	 * <p>
	 * Construct a new application data bean instance.
	 * </p>
	 */
	public EventQueryManager() {
	}

	/**
	 * Queries events by filter
	 * 
	 * @param filter
	 * @param startIndex
	 * @return
	 */
	public List<EventBean> queryForEventsByFilter(Filter filter,
			int startIndex, Sort sort) {
		return this.queryForEventsByFilter(filter, null, startIndex, sort);
	}

	/**
	 * Queries events by Ids
	 * 
	 * @param ids
	 * @param sort
	 * @param firstIndex
	 * @param maxResults
	 * @return
	 */
	public List<EventBean> queryForEventsByIds(int[] ids, Sort sort,
			int firstIndex, int maxResults) {
		Vector<EventBean> events = new Vector<EventBean>();
		WSFoundationCollection col = null;
		try {
			locator.setEndpointAddress(
					ConsoleConstants.FOUNDATION_END_POINT_EVENT, PropertyUtils
							.getProperty(ConsoleConstants.PROP_WS_URL)
							+ ConsoleConstants.FOUNDATION_END_POINT_EVENT);
			WSEvent wsEvent = locator.getwsevent();
			col = wsEvent.getEventsByIds(ids, sort, firstIndex, maxResults);
			LogMessage[] logMsgs = col.getLogMessage();
			if (logMsgs == null)
				return events;
			this.loadEvents(logMsgs, events, null, col.getTotalCount());
		} catch (Exception ex) {
			logger.error("Exception Occurred - " + ex);
			ex.printStackTrace();
		}
		return events;
	}

	/**
	 * Queries events by filter
	 * 
	 * @param filter
	 * @param entityTypeProperties
	 * @param startIndex
	 * @return
	 */
	public List<EventBean> queryForEventsByFilter(Filter filter,
			EntityTypeProperty[] entityTypeProperties, int startIndex, Sort sort) {
		Vector<EventBean> events = new Vector<EventBean>();
		WSFoundationCollection col = null;
		// default sort
		// if (sort == null)
		// sort = new Sort(false, "reportDate");
		try {
			locator.setEndpointAddress(
					ConsoleConstants.FOUNDATION_END_POINT_EVENT, PropertyUtils
							.getProperty(ConsoleConstants.PROP_WS_URL)
							+ ConsoleConstants.FOUNDATION_END_POINT_EVENT);
			WSEvent wsEvent = locator.getwsevent();
			logger.debug("Start Index=" + startIndex + "-Filter=" + filter);
			String sortColumnName = sort.getSortItem(0).getPropertyName();
			SortItem sortItem = new SortItem();
			sortItem.setPropertyName(ConsoleORMappingUtil
					.findORMapping(sortColumnName));
			sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
			sort.setSortItem(0, sortItem);
			logger.debug("Sorting by " + sortColumnName + "----"
					+ sort.getSortItem(0).isSortAscending());
			int maxResults = Integer.parseInt(PropertyUtils
					.getProperty(ConsoleConstants.PROP_PAGE_SIZE));
			Filter leftFilter = filter.getLeftFilter();
			Filter innerFilter = this.checkCategoryInLeftFilter(leftFilter);
			if ((filter.getPropertyName() != null && filter.getPropertyName()
					.equalsIgnoreCase(ConsoleConstants.PROP_NAME_CATEGORY_NAME))
					|| (innerFilter != null && innerFilter != null &&  innerFilter
							.getStringProperty()  != null  && innerFilter
							.getStringProperty().getName().equalsIgnoreCase(
									ConsoleConstants.PROP_NAME_CATEGORY_NAME))) {
				String categoryName = null;
				if (filter.getValue() != null)
					categoryName = filter.getValue().toString();
				else
					categoryName = innerFilter.getStringProperty().getValue()
							.toString();
				col = wsEvent
						.getEventsByCategory(categoryName,
								ConsoleConstants.ENTITY_NAME_SERVICEGROUP,
								filter.getRightFilter(), sort, startIndex,
								maxResults);
			} else
				col = wsEvent.getEventsByCriteria(filter, sort, startIndex,
						maxResults);
			LogMessage[] logMsgs = col.getLogMessage();
			if (logMsgs == null)
				return events;
			this.loadEvents(logMsgs, events, entityTypeProperties, col
					.getTotalCount());
		} catch (Exception ex) {
			logger.error("Exception Occurred - " + ex);
			ex.printStackTrace();
		}
		return events;
	}

	/**
	 * Checks is category property is in the inner filters
	 * @param leftFilter
	 * @return
	 */
	private Filter checkCategoryInLeftFilter(Filter leftFilter) {
		Filter innerEndFilter = leftFilter;
		while (leftFilter != null) {
			Filter child = leftFilter.getLeftFilter();
			if (child == null) {
				innerEndFilter = leftFilter;
				break;
			} else if (child.getLeftFilter() == null) {
				innerEndFilter = child;
				break;
			} else {
				leftFilter = child;
			}
		}
		/*
		 * String propertyName = innerEndFilter.getStringProperty().getName();
		 * String propertyValue = innerEndFilter.getStringProperty().getValue();
		 * System.out.println("PropertyName=" + propertyName + ",Value =" +
		 * propertyValue);
		 */
		return innerEndFilter;
	}

	/**
	 * Loads the events
	 * 
	 * @param logMsgs
	 * @param events
	 * @param entityTypeProperties
	 */
	private void loadEvents(LogMessage[] logMsgs, Vector<EventBean> events,
			EntityTypeProperty[] entityTypeProperties, int totalCount) {
		logger.debug("No of messages fetched=" + logMsgs.length);
		for (int i = 0; i < logMsgs.length; i++) {
			EventBean event = new EventBean();
			event.setTotalCount(totalCount);
			event.setLogMessageID(logMsgs[i].getLogMessageID());
			event.setDevice(logMsgs[i].getDevice().getName());
			event.setMsgCount(logMsgs[i].getMessageCount());
			event.setReportDate(DateUtils.format(logMsgs[i].getReportDate(),
					PropertyUtils
							.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));

			event.setSeverity(logMsgs[i].getSeverity().getName());
			StatusBean statBean = new StatusBean();
			String status = logMsgs[i].getMonitorStatus().getName();
			statBean.setValue(status);
			if (status.equalsIgnoreCase(ConsoleConstants.MON_STATUS_WARN)) {
				statBean.setStyleClass("warning");
			} else if (status
					.equalsIgnoreCase(ConsoleConstants.MON_STATUS_CRITICAL)
					|| status
							.equalsIgnoreCase(ConsoleConstants.MON_STATUS_UNREACHABLE)
					|| status
							.equalsIgnoreCase(ConsoleConstants.MON_STATUS_DOWN)) {
				statBean.setStyleClass("critical");
			} else if (status
					.equalsIgnoreCase(ConsoleConstants.MON_STATUS_UNKNOWN)
					|| status
							.equalsIgnoreCase(ConsoleConstants.MON_STATUS_PENDING)) {
				statBean.setStyleClass("severityUnknown");
			} else if (status.equalsIgnoreCase(ConsoleConstants.MON_STATUS_OK)
					|| status.equalsIgnoreCase(ConsoleConstants.MON_STATUS_UP)) {
				statBean.setStyleClass("okay");
			}
			event.setMonitorStatus(statBean);
			event.setApplicationType(logMsgs[i].getApplicationName());
			event.setTextMessage(StringEscapeUtils.unescapeXml(logMsgs[i]
					.getTextMessage()));
			event.setLastInsertDate(DateUtils.format(logMsgs[i]
					.getLastInsertDate(), PropertyUtils
					.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));
			event.setFirstInsertDate(DateUtils.format(logMsgs[i]
					.getFirstInsertDate(), PropertyUtils
					.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));
			if (entityTypeProperties != null) {
				Map<String, Object> map = new HashMap<String, Object>();

				for (int j = 0; j < entityTypeProperties.length; j++) {
					String propertyName = entityTypeProperties[j].getName();
					// logger.info(propertyName);
					Object propertyValue = logMsgs[i].getPropertyTypeBinding()
							.getPropertyValue(propertyName);
					if (propertyName != null
							&& !propertyName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SERVICE_COLUMN))
						map.put(propertyName, propertyValue);
					// For Nagios service
					if (propertyName != null
							&& propertyName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SUBCOMPONENT_COLUMN)) {
						if (propertyValue != null
								&& propertyValue.toString().indexOf(
										ConsoleConstants.DELIM_COLON) != -1) {
							StringTokenizer stkn = new StringTokenizer(
									propertyValue.toString(),
									ConsoleConstants.DELIM_COLON);
							String deviceName = stkn.nextToken();
							String serviceValue = stkn.nextToken();
							map.put(ConsoleConstants.NAGIOS_SERVICE_COLUMN,
									serviceValue);
						} // end if
					} // end if
				} // end for

				event.setDynamicProperty(map);
			} // end if
			events.add(event);
		} // end for
	} // end loadEvents method
}
