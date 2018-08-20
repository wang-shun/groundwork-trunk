/*
 * EventQueryManager.java
 * 
 * Created on June 6, 2007, 3:25 PM Copyright rdandridge
 */
package com.groundworkopensource.webapp.console;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.statusviewer.common.EventMenuActionManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.impl.WSEventServiceLocator;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ApplicationType;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.LogMessage;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

	/** The logger. */
	private static Logger logger = Logger.getLogger(EventQueryManager.class
			.getName());

	/** The locator. */
	private WSEventServiceLocator locator = new WSEventServiceLocator();

	private HashMap<String, EntityTypeProperty[]> entityPropMap = new HashMap<String, EntityTypeProperty[]>();

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
     * @param sort
     * @param pageSize
     * @return List
     * @throws com.groundworkopensource.portal.common.exception.WSDataUnavailableException
     * @throws com.groundworkopensource.portal.common.exception.GWPortalException
     */
    public List<EventBean> queryForEventsByFilter(Filter filter,
                                                  int startIndex, Sort sort, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        return this.queryForEventsByFilter(filter, null, startIndex, sort,
                pageSize);
    }

    /**
     * Queries events by filter
     *
     * @param filter
     * @param entityTypeProperties
     * @param startIndex
     * @param sort
     * @param pageSize
     * @return List
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public List<EventBean> queryForEventsByFilter(Filter filter,
                                                  EntityTypeProperty[] entityTypeProperties, int startIndex,
                                                  Sort sort, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        Vector<EventBean> events = new Vector<EventBean>();
        WSFoundationCollection wsfoundationCollection = null;

        String sortColumnName = sort.getSortItem(0).getPropertyName();
        SortItem sortItem = new SortItem();
        sortItem.setPropertyName(ConsoleORMappingUtil
                .findORMapping(sortColumnName));
        sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
        sort.setSortItem(0, sortItem);
        try {
        locator.setEndpointAddress(
                ConsoleConstants.FOUNDATION_END_POINT_EVENT,
                PropertyUtils.getProperty(ConsoleConstants.PROP_WS_URL)
                        + ConsoleConstants.FOUNDATION_END_POINT_EVENT);
        WSEvent wsEvent = locator.getwsevent();
        wsfoundationCollection = wsEvent.getEventsByCriteria(filter, sort,
                startIndex, pageSize);
        }
        catch (Exception exc) {
             throw new GWPortalException(exc.getMessage());
        }
        if (wsfoundationCollection != null) {
            LogMessage[] logMsgs = wsfoundationCollection.getLogMessage();
            if (logMsgs == null) {
                return events;
            }

            this.loadEvents(logMsgs, events, entityTypeProperties,
                    wsfoundationCollection.getTotalCount());
        }

        return events;
    }

	/**
	 * Queries events by filter.
	 * 
	 * @param filter
	 *            the filter
	 * @param startIndex
	 *            the start index
	 * @param sort
	 *            the sort
	 * @param hostGroupList
	 * @param ServiceGroupList
	 * @return the list< event bean>
	 */
	public List<EventBean> queryForEventsByFilter(Filter filter,
			int startIndex, Sort sort, String hostGroupList,
			String ServiceGroupList) {
		return this.queryForEventsByFilter(filter, null, startIndex, sort,
				hostGroupList, ServiceGroupList);
	}

	/**
	 * Queries events by Ids.
	 * 
	 * @param ids
	 *            the ids
	 * @param sort
	 *            the sort
	 * @param firstIndex
	 *            the first index
	 * @param maxResults
	 *            the max results
	 * 
	 * @return the list< event bean>
	 */
	public List<EventBean> queryForEventsByIds(int[] ids, Sort sort,
			int firstIndex, int maxResults) {
		Vector<EventBean> events = new Vector<EventBean>();
		WSFoundationCollection col = null;
		try {
			locator.setEndpointAddress(
					ConsoleConstants.FOUNDATION_END_POINT_EVENT,
					PropertyUtils.getProperty(ConsoleConstants.PROP_WS_URL)
							+ ConsoleConstants.FOUNDATION_END_POINT_EVENT);
			WSEvent wsEvent = locator.getwsevent();
			col = wsEvent.getEventsByIds(ids, sort, firstIndex, maxResults);
			LogMessage[] logMsgs = col.getLogMessage();
			if (logMsgs == null) {
				return events;
			}
			this.loadEvents(logMsgs, events, null, col.getTotalCount());
		} catch (Exception ex) {
			logger.error("Exception Occurred - " + ex);
			ex.printStackTrace();
		}
		return events;
	}

	/**
	 * Queries events by filter.
	 * 
	 * @param filter
	 *            the filter
	 * @param entityTypeProperties
	 *            the entity type properties
	 * @param startIndex
	 *            the start index
	 * @param sort
	 *            the sort
	 * @param hostGroupList
	 * @param serviceGroupList
	 * 
	 * @return list of events as per the passed filter criteria
	 */
	public List<EventBean> queryForEventsByFilter(Filter filter,
			EntityTypeProperty[] entityTypeProperties, int startIndex,
			Sort sort, String hostGroupList, String serviceGroupList) {
		Vector<EventBean> events = new Vector<EventBean>();
		WSFoundationCollection col = null;
		// default sort
		// if (sort == null)
		// sort = new Sort(false, "reportDate");
		try {
			locator.setEndpointAddress(
					ConsoleConstants.FOUNDATION_END_POINT_EVENT,
					PropertyUtils.getProperty(ConsoleConstants.PROP_WS_URL)
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
			if (filter != null) {
				Filter leftFilter = filter.getLeftFilter();
				Filter innerFilter = this.checkCategoryInLeftFilter(leftFilter);
				if ((filter.getPropertyName() != null && filter
						.getPropertyName().equalsIgnoreCase(
								ConsoleConstants.PROP_NAME_CATEGORY_NAME))
						|| (innerFilter != null && innerFilter != null
								&& innerFilter.getStringProperty() != null && innerFilter
								.getStringProperty()
								.getName()
								.equalsIgnoreCase(
										ConsoleConstants.PROP_NAME_CATEGORY_NAME))) {
					String categoryName = null;
					if (filter.getValue() != null) {
						categoryName = filter.getValue().toString();
					} else {
						categoryName = innerFilter.getStringProperty()
								.getValue().toString();
					}
					col = wsEvent.getEventsByCategory(categoryName,
							ConsoleConstants.ENTITY_NAME_SERVICEGROUP,
							filter.getRightFilter(), sort, startIndex,
							maxResults);
				} else {
					col = wsEvent
							.getEventsByRestrictedHostGroupsAndServiceGroups(
									hostGroupList, serviceGroupList, filter,
									sort, startIndex, maxResults);
				}
			} else {
				col = wsEvent.getEventsByRestrictedHostGroupsAndServiceGroups(
						hostGroupList, serviceGroupList, filter, sort,
						startIndex, maxResults);
			}
			LogMessage[] logMsgs = col.getLogMessage();
			if (logMsgs == null) {
				return events;
			}
			this.loadEvents(logMsgs, events, entityTypeProperties,
					col.getTotalCount());
		} catch (Exception ex) {
			logger.error("Exception Occurred - " + ex);
			ex.printStackTrace();
		}
		return events;
	}

	/**
	 * Checks is category property is in the inner filters.
	 * 
	 * @param leftFilter
	 *            the left filter
	 * 
	 * @return the filter
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

	private static final String ACK_PREFIX = "ACKNOWLEDGEMENT (";

	private String mapSeverityToStyle(String severity) {

		if (severity.startsWith(ACK_PREFIX)) {
			String result = severity.substring(ACK_PREFIX.length());
			return result.substring(0, result.length()-1);
		}
		else {
			return "severity_" + severity;
		}
	}

	/**
	 * Loads the events.
	 * 
	 * @param logMsgs
	 *            the log msgs
	 * @param events
	 *            the events
	 * @param entityTypeProperties
	 *            the entity type properties
	 * @param totalCount
	 *            the total count
	 */
	private void loadEvents(LogMessage[] logMsgs, Vector<EventBean> events,
			EntityTypeProperty[] entityTypeProperties, int totalCount) {
		logger.debug("No of messages fetched=" + logMsgs.length);
		for (int i = 0; i < logMsgs.length; i++) {
			EventBean event = new EventBean();
			event.setTotalCount(totalCount);
			event.setLogMessageID(logMsgs[i].getLogMessageID());
            String serviceDescription = null;
            if (logMsgs[i].getServiceStatus() != null)
                serviceDescription =  logMsgs[i].getServiceStatus().getDescription();
			event.setServiceDescription(serviceDescription);
            if (serviceDescription != null && serviceDescription.length()>0 && serviceDescription.length() > 15) {
                event.setServiceDescriptionShort(serviceDescription.substring(0, 14)+ "...");
            }
            else {
                event.setServiceDescriptionShort(serviceDescription);
            }
			event.setHost(logMsgs[i].getHost() != null ? logMsgs[i].getHost()
					.getName() : logMsgs[i].getDevice().getName());
			event.setDevice(logMsgs[i].getDevice().getName());
			event.setMsgCount(logMsgs[i].getMessageCount());
			event.setReportDate(DateUtils.format(logMsgs[i].getReportDate(),
					PropertyUtils
							.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));
			SeverityBean sevBean = new SeverityBean();
			String severity = logMsgs[i].getSeverity().getName();
			sevBean.setValue(severity);
			sevBean.setStyleClass(mapSeverityToStyle(severity));
			event.setSeverity(sevBean);
			StatusBean statBean = new StatusBean();
			String status = logMsgs[i].getMonitorStatus().getName();
			statBean.setValue(status);
			if (status.toLowerCase().contains(ConsoleConstants.MON_STATUS_WARN)) {
				statBean.setStyleClass("warning");
			} else if (status.toLowerCase().contains(
					ConsoleConstants.MON_STATUS_CRITICAL)
					&& status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_UNSCHEDULED)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_CRITICAL)
					&& !status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_SCHEDULED)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_DOWN)
					&& status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_UNSCHEDULED)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_DOWN)
					&& !status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_SCHEDULED)) {
				statBean.setStyleClass("critical");
            } else if (status.toLowerCase().contains(
					ConsoleConstants.MON_STATUS_CRITICAL)
					&& status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_SCHEDULED)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_DOWN)
					&& status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_SCHEDULED)) {
				statBean.setStyleClass("severityScheduled");
			} else if (status.toLowerCase().contains(
					ConsoleConstants.MON_STATUS_UNKNOWN)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_UNREACHABLE)) {
				statBean.setStyleClass("severityUnknown");
			} else if (status.toLowerCase().contains(
					ConsoleConstants.MON_STATUS_PENDING)) {
				statBean.setStyleClass("severityPending");
			} else if (status.toLowerCase().contains(
					ConsoleConstants.MON_STATUS_OK)
					|| status.toLowerCase().contains(
							ConsoleConstants.MON_STATUS_UP)) {
				statBean.setStyleClass("okay");
			}
			event.setMonitorStatus(statBean);
			event.setOperationStatus(logMsgs[i].getOperationStatus().getName());
			String appType = logMsgs[i].getApplicationName();
			event.setApplicationType(appType);
			String textMessage = StringEscapeUtils.unescapeXml(logMsgs[i]
					.getTextMessage());
			String textMessageSize = PropertyUtils
					.getProperty(ConsoleConstants.PROP_TEXT_MESSAGE_SIZE);
			if (textMessageSize != null) {
				int textLength = Integer.parseInt(textMessageSize);
				if (textMessage != null && textMessage.length() > textLength) {
					event.setTextMessageFull(textMessage);
					textMessage = textMessage.substring(0, textLength - 1)
							+ "....";
				} // end if
			} // end if

			event.setTextMessage(textMessage);
			event.setLastInsertDate(DateUtils.format(logMsgs[i]
					.getLastInsertDate(), PropertyUtils
					.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));
			event.setFirstInsertDate(DateUtils.format(logMsgs[i]
					.getFirstInsertDate(), PropertyUtils
					.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));

			if (entityTypeProperties == null) {
				if (entityPropMap.containsKey(appType))
					entityTypeProperties = entityPropMap.get(appType);
				else {
					EntityTypeProperty[] newProp = this
							.populateEntityTypePropByAppType(appType);
					if (newProp != null && newProp.length > 0) {
						entityPropMap.put(appType, newProp);
						entityTypeProperties = entityPropMap.get(appType);
					} // end if
				} // end if/else
			} // end if

			if (entityTypeProperties != null) {
				Map<String, Object> map = new HashMap<String, Object>();

				for (int j = 0; j < entityTypeProperties.length; j++) {
					String propertyName = entityTypeProperties[j].getName();
					// logger.info(propertyName);
					Object propertyValue = logMsgs[i].getPropertyTypeBinding()
							.getPropertyValue(propertyName);
					if (propertyName != null
							&& !propertyName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SERVICE_COLUMN) && propertyValue != null) {
						map.put(propertyName, propertyValue);
					}
					// For Nagios service
					/*if (propertyName != null
							&& propertyName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SUBCOMPONENT_COLUMN)) {
						if (propertyValue != null
								&& propertyValue.toString().indexOf(
										ConsoleConstants.DELIM_COLON) != -1) {
							StringTokenizer stkn = new StringTokenizer(
									propertyValue.toString(),
									ConsoleConstants.DELIM_COLON);
							// get the deviceName. IMP do not remove below line
							// of code. Viz stkn.nextToken()
							stkn.nextToken();
							String serviceValue = stkn.nextToken();
							map.put(ConsoleConstants.NAGIOS_SERVICE_COLUMN,
									serviceValue);
						} // end if
					} // end if*/
					if (propertyName != null
							&& (propertyName
									.equalsIgnoreCase(ConsoleConstants.COMMENTS) || propertyName
                            .equalsIgnoreCase(ConsoleConstants.NAGIOS_ACK_COMMENTS))) {
						if (propertyValue != null) {
							String comments = (String) propertyValue;
							if (comments.length() > 20) {
								comments = comments.substring(0, 16) + "...";	
							}
							event.setCommentsShort(comments);
						}
					} // end if
				} // end for
                if (!map.isEmpty())
				    event.setDynamicProperty(map);
			} // end if
			entityTypeProperties = null;
			events.add(event);
		} // end for
	} // end loadEvents method

	/**
	 * Helper to populateEntityTypeProp By AppType
	 * 
	 * @param appType
	 * @return
	 */
	private EntityTypeProperty[] populateEntityTypePropByAppType(String appType) {
		EntityTypeProperty[] entityTypeProperties = null;
		try {

			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			WSFoundationCollection col = wsCommon.getEntityTypeProperties(
					ConsoleConstants.ENTITY_TYPE_LOGMESSAGE, appType, true);
			entityTypeProperties = col.getEntityTypeProperty();
			// Set the dynamic columns
			if (appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS)) {
				EntityTypeProperty[] tempEntityTypeProperties = new EntityTypeProperty[entityTypeProperties.length + 1];
				for (int i = 0; i < entityTypeProperties.length; i++) {
					tempEntityTypeProperties[i] = entityTypeProperties[i];
				}
				EntityTypeProperty nagiosServiceProp = new EntityTypeProperty();
				nagiosServiceProp
						.setName(ConsoleConstants.NAGIOS_SERVICE_COLUMN);
				ApplicationType nagiosAppType = new ApplicationType();
				nagiosAppType.setName(ConsoleConstants.APP_TYPE_NAGIOS);
				nagiosServiceProp.setApplicationType(nagiosAppType);
				tempEntityTypeProperties[entityTypeProperties.length] = nagiosServiceProp;
				return tempEntityTypeProperties;
			} // end if

		} catch (Exception exc) {
			logger.error("Failed to retrieve entityTypeProperties for App Type: " +  appType, exc);
		}
		return entityTypeProperties;
	}

    /**
     * Gets actions by application type
     *
     * @param appType
     * @return Action[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Action[] getActionsByApplicationType(String appType)
            throws WSDataUnavailableException, GWPortalException {
        return EventMenuActionManager.getActionsByApplicationType(appType);

    }
}
