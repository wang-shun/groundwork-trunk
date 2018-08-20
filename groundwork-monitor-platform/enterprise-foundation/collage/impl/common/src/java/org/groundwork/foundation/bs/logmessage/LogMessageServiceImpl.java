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
package org.groundwork.foundation.bs.logmessage;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.impl.StateTransition;
import com.groundwork.collage.util.Nagios;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.jms.TextMessage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LogMessage Service Implementation Class
 */
public class LogMessageServiceImpl extends EntityBusinessServiceImpl implements LogMessageService {

    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private static final String DATE_FORMAT_US = "MM/dd/yyyy H:mm:ss";

    private static final String DATE_ONLY_FORMAT = "MM/dd/yyyy";

    private static final String WHERE_BEGIN = " WHERE 1=1 AND ";
    private static final String OR = " OR ";
    private static final String ID_EQUALS = "logMessageId=";

    private static final String IS_ACKNOWLEDGED = "isAcknowledged";
    private static final String OP_STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";

	/*
     * JIRA:GWMON-4295 cpora Tuesday Feb 12 2008, 10:05am this has been used in
	 * isStateChanged to do a bulkUpdate which caused a deadlock. private static
	 * final String UPDATE_STATE_CHANGED =
	 * "update LogMessage set stateChanged	= true where stateChanged = false and statelessHash = "
	 * ;
	 */

    /**
     * Default Sort Criteria
     */
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.desc(LogMessage.HP_LAST_INSERT_DATE);

    private static final String LOG_MESSAGE_EVENT_XML_TAG = "<"+LOG_MESSAGE_EVENT_XML_TAG_NAME+"/>";

    private StatusService _serviceStatusService;
    private MetadataService _metadataService;
    private LogMessageWindowService _logMessageWindowService;

    /**
     * Enable Logging *
     */
    protected static Log log = LogFactory.getLog(LogMessageServiceImpl.class);

    private int maxQueryAgeHours = 0;

    public LogMessageServiceImpl(FoundationDAO foundationDAO, StatusService ss,
                                 MetadataService ms, LogMessageWindowService lmws) {
        super(foundationDAO, LogMessage.INTERFACE_NAME, LogMessage.COMPONENT_NAME);

        _serviceStatusService = ss;
        _metadataService = ms;
        _logMessageWindowService = lmws;

        String value = FoundationConfiguration.getProperty(LogMessage.EVENT_MAX_QUERY_AGE_HOURS);
        if (StringUtils.isNotBlank(value)) {
            try {
                maxQueryAgeHours = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Unable to parse value '" + value + "' from property '" + LogMessage.EVENT_MAX_QUERY_AGE_HOURS + "'.  Ignoring value.");
                maxQueryAgeHours = 0;
            }
        }
        if (log.isDebugEnabled()) log.debug("Value of '" + LogMessage.EVENT_MAX_QUERY_AGE_HOURS +"' is " + maxQueryAgeHours);
    }

    public FoundationQueryList getLogMessages(FilterCriteria filter,
                                              SortCriteria sortCriteria, int firstResult, int maxResults) {
        return this.getLogMessages(null, null, filter, sortCriteria, firstResult, maxResults);
    }

    public FoundationQueryList getLogMessages(String startDate, String endDate,
                                              FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
                                              int maxResults) {
        CollageTimer timer = startMetricsTimer();
        try {

            DateFormat date = new SimpleDateFormat(DATE_FORMAT);

            Date parsedStartDate = null;
            if (StringUtils.isNotBlank(startDate)) {
                parsedStartDate = date.parse(startDate);
            } else if (maxQueryAgeHours > 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR, -maxQueryAgeHours);
                parsedStartDate = cal.getTime();
            }
            if (parsedStartDate != null) {
                FilterCriteria startDateCriteria = FilterCriteria.gt(LogMessage.HP_LAST_INSERT_DATE, parsedStartDate);
                if (filter != null) {
                    filter.and(startDateCriteria);
                } else {
                    filter = startDateCriteria;
                }
            }

            if (StringUtils.isNotBlank(endDate)) {
                FilterCriteria endDateCriteria = FilterCriteria.lt(LogMessage.HP_LAST_INSERT_DATE, date.parse(endDate));
                if (filter != null) {
                    filter.and(endDateCriteria);
                } else {
                    filter = endDateCriteria;
                }
            }
        } catch (ParseException e) {
            throw new BusinessServiceException(e.getMessage());
        }

        if (sortCriteria == null)
            sortCriteria = DEFAULT_SORT_CRITERIA;

        FoundationQueryList results = this.query(filter, sortCriteria, firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByApplicationTypeName(
            String appTypeName, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (appTypeName == null || appTypeName.length() == 0) {
            throw new IllegalArgumentException(
                    "An Application Type Name must be provided.");
        }

        ApplicationType appType = _metadataService
                .getApplicationTypeByName(appTypeName);
        if (appType == null) {
            throw new IllegalArgumentException(
                    "Invalid Application Type provided.");
        }

        FilterCriteria appTypeFilter = FilterCriteria.eq(
                LogMessage.HP_APPLICATION_TYPE_ID,
                appType.getApplicationTypeId());
        if (filter != null)
            filter.and(appTypeFilter);
        else
            filter = appTypeFilter;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByApplicationTypeId(int appTypeId,
                                                                 String startDate, String endDate, FilterCriteria filter,
                                                                 SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (appTypeId < 1) {
            throw new IllegalArgumentException(
                    "A valid Application Type ID must be provided.");
        }

        ApplicationType appType = _metadataService
                .getApplicationTypeById(appTypeId);
        if (appType == null) {
            throw new IllegalArgumentException(
                    "Invalid Application Type provided.");
        }

        FilterCriteria appTypeFilter = FilterCriteria.eq(
                LogMessage.HP_APPLICATION_TYPE_ID,
                appType.getApplicationTypeId());
        if (filter != null)
            filter.and(appTypeFilter);
        else
            filter = appTypeFilter;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByDeviceId(int deviceId,
                                                        String startDate, String endDate, FilterCriteria filter,
                                                        SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (deviceId <= 0)
            throw new IllegalArgumentException("Invalid device id");

        FilterCriteria deviceIdCriteria = FilterCriteria.eq(
                LogMessage.HP_DEVICE_ID, deviceId);
        if (filter != null)
            filter.and(deviceIdCriteria);
        else
            filter = deviceIdCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByDeviceIdentification(
            String deviceIdentification, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (deviceIdentification == null || deviceIdentification.length() == 0) {
            throw new IllegalArgumentException(
                    "Device Identification cannot be empty or null");
        }

        FilterCriteria deviceIentificationCriteria = FilterCriteria.eq(
                LogMessage.HP_DEVICE_IDENTIFICATION, deviceIdentification);
        if (filter != null)
            filter.and(deviceIentificationCriteria);
        else
            filter = deviceIentificationCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByDeviceIdentifications(
            String[] deviceIdentifications, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (deviceIdentifications == null || deviceIdentifications.length == 0) {
            throw new IllegalArgumentException(
                    "Device Identification list cannot be empty or null");
        }

        for (int i = 0; i < deviceIdentifications.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria.eq(
                        LogMessage.HP_DEVICE_IDENTIFICATION,
                        deviceIdentifications[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_DEVICE_IDENTIFICATION,
                        deviceIdentifications[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByDeviceIds(int[] deviceIds,
                                                         String startDate, String endDate, FilterCriteria filter,
                                                         SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (deviceIds == null || deviceIds.length == 0) {
            throw new IllegalArgumentException(
                    "Device Id list cannot be empty or null");
        }

        for (int i = 0; i < deviceIds.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria.eq(LogMessage.HP_DEVICE_ID,
                        deviceIds[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_DEVICE_ID,
                        deviceIds[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostName(String hostName,
                                                        String startDate, String endDate, FilterCriteria filter,
                                                        SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostName == null || hostName.length() == 0)
            throw new IllegalArgumentException(
                    "Host name cannot be empty or null");

        FilterCriteria hostCriteria = FilterCriteria.eq(
                LogMessage.HP_HOST_NAME, hostName);
        if (filter != null)
            filter.and(hostCriteria);
        else
            filter = hostCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostId(int hostId,
                                                      String startDate, String endDate, FilterCriteria filter,
                                                      SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostId <= 0)
            throw new IllegalArgumentException("Invalid HostId");

        FilterCriteria hostCriteria = FilterCriteria.eq(
                LogMessage.HP_HOST_STATUS_ID, hostId);
        if (filter != null)
            filter.and(hostCriteria);
        else
            filter = hostCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostIds(int[] hostIds,
                                                       String startDate, String endDate, FilterCriteria filter,
                                                       SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostIds == null || hostIds.length == 0) {
            throw new IllegalArgumentException(
                    "HostGroup Id list cannot be empty or null");
        }

        for (int i = 0; i < hostIds.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria.eq(LogMessage.HP_HOST_STATUS_ID,
                        hostIds[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_HOST_STATUS_ID,
                        hostIds[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostNames(String[] hostNames,
                                                         String startDate, String endDate, FilterCriteria filter,
                                                         SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostNames == null || hostNames.length == 0) {
            throw new IllegalArgumentException(
                    "HostGroup Id list cannot be empty or null");
        }

        for (int i = 0; i < hostNames.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria.eq(LogMessage.HP_HOST_NAME,
                        hostNames[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_HOST_NAME,
                        hostNames[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostGroupId(int hgId,
                                                           String startDate, String endDate, FilterCriteria filter,
                                                           SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer();
        if (hgId <= 0)
            throw new IllegalArgumentException("Invalid hostgroup Id");

        FilterCriteria hostGroupCriteria = FilterCriteria.eq(
                LogMessage.HP_HOST_GROUP_ID, hgId);
        if (filter != null)
            filter.and(hostGroupCriteria);
        else
            filter = hostGroupCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostGroupName(
            String hostGroupName, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostGroupName == null || hostGroupName.length() == 0)
            throw new IllegalArgumentException(
                    "HostGroup name cannot be empty or null");

        FilterCriteria hostGroupCriteria = FilterCriteria.eq(
                LogMessage.HP_HOST_GROUP_NAME, hostGroupName);
        if (filter != null)
            filter.and(hostGroupCriteria);
        else
            filter = hostGroupCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostGroupIds(int[] ids,
                                                            String startDate, String endDate, FilterCriteria filter,
                                                            SortCriteria sortCriteria, int firstResult, int maxResults)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException(
                    "HostGroup Id list cannot be empty or null");
        }

        for (int i = 0; i < ids.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria
                        .eq(LogMessage.HP_HOST_GROUP_ID, ids[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_HOST_GROUP_ID, ids[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByHostGroupNames(
            String[] hostGroupNames, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostGroupNames == null || hostGroupNames.length == 0) {
            throw new IllegalArgumentException(
                    "HostGroup Id list cannot be empty or null");
        }

        for (int i = 0; i < hostGroupNames.length; i++) {
            if (filter != null)
                filter.or(FilterCriteria.eq(LogMessage.HP_HOST_GROUP_NAME,
                        hostGroupNames[i]));
            else
                filter = FilterCriteria.eq(LogMessage.HP_HOST_GROUP_NAME,
                        hostGroupNames[i]);
        }

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByService(String hostName,
                                                       String serviceDescr, String startDate, String endDate,
                                                       FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
                                                       int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (hostName == null || hostName.length() == 0)
            throw new IllegalArgumentException(
                    "Host name cannot be empty or null");
        if (serviceDescr == null || serviceDescr.length() == 0)
            throw new IllegalArgumentException(
                    "Service Description cannot be empty or null");

        FilterCriteria hostCriteria = FilterCriteria.eq(
                LogMessage.HP_HOST_NAME, hostName);
        if (filter != null)
            filter.and(hostCriteria);
        else
            filter = hostCriteria;

        FilterCriteria serviceCriteria = FilterCriteria.eq(
                LogMessage.HP_SERVICE_STATUS_DESCRIPTION, serviceDescr);
        filter.and(serviceCriteria);

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public FoundationQueryList getLogMessagesByServiceStatusId(
            int serviceStatusId, String startDate, String endDate,
            FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
            int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (serviceStatusId < 1) {
            throw new IllegalArgumentException("Invalid service status id.");
        }

        FilterCriteria serviceCriteria = FilterCriteria.eq(
                LogMessage.HP_SERVICE_STATUS_ID, serviceStatusId);
        if (filter != null)
            filter.and(serviceCriteria);
        else
            filter = serviceCriteria;

        FoundationQueryList results = this.getLogMessages(startDate, endDate, filter, sortCriteria,
                firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    public LogMessage getLogMessageById(int logMessageId)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (logMessageId <= 0)
            throw new IllegalArgumentException(
                    "Invalid LogMessageId specified.");

        LogMessage result = (LogMessage) this.queryById(logMessageId);
        stopMetricsTimer(timer);
        return result;
    }

    public LogMessage getLogMessageForConsolidationCriteria(
            int consolidationHash) {
        CollageTimer timer = startMetricsTimer();
        FilterCriteria filter = FilterCriteria.eq(
                LogMessage.HP_CONSOLIDATIONHASH, consolidationHash);
        FilterCriteria stateFilter = FilterCriteria.eq(
                LogMessage.HP_STATE_CHANGED, false);
        filter.and(stateFilter);
        FoundationQueryList messages = this.getLogMessages(null, null, filter,
                null, -1, -1);
        stopMetricsTimer(timer);
        if (messages == null || messages.size() == 0) {
            return null;
        }
        return (LogMessage) messages.get(0);
    }

    /**
     * Creates new un-persisted LogMessage
     *
     * @return
     * @throws BusinessServiceException
     */
    public LogMessage createLogMessage() throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        LogMessage logMessage = (LogMessage) create(LogMessage.INTERFACE_NAME);
        stopMetricsTimer(timer);
        return logMessage;
    }

    /**
     * Persists the specified log message - Note: We update the state transition
     * automatically All saving of log messages should go through this business
     * message
     *
     * @param logMsg
     * @throws BusinessServiceException
     */
    public void saveLogMessage(LogMessage logMsg)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (logMsg == null)
            throw new IllegalArgumentException(
                    "Invalid null LogMessage parameter.");

        // Check for a state transition iff the message is new and the state transition hash has not
        // been pre-populated
        // (Note: This assumes MonitorStatus is never changed for a log message)
        Integer logMsgId = logMsg.getLogMessageId();
        boolean isNew = (logMsgId == null || logMsgId < 1);
        if (isNew && logMsg.getStateTransitionHash() == null) {
            Integer stateTransitionHash = buildStateTransitionHash(logMsg);
            // See if this is a state transition - If there is a state
            // transition then
            // we store the hash value otherwise no hashvalue is stored which
            // indicates the
            // log message was not related to a status change
            if (isStateTransition(stateTransitionHash, logMsg.getMonitorStatus()))
                logMsg.setStateTransitionHash(stateTransitionHash);
        }
        save(logMsg);

        // publish log message event if new
        if (isNew) {
            try {
                TextMessage message = new TextMessageImpl(LOG_MESSAGE_EVENT_XML_TAG);
                if (logMsg.getServiceStatus() != null) {
                    message.setIntProperty("serviceId", logMsg.getServiceStatus().getServiceStatusId());
                }
                if (logMsg.getHostStatus() != null) {
                    message.setIntProperty("hostId", logMsg.getHostStatus().getHostStatusId());
                }
                if (logMsg.getMonitorStatus() != null) {
                    message.setIntProperty("monitorStatusId", logMsg.getMonitorStatus().getMonitorStatusId());
                }
                message.setLongProperty("firstInsertDate", logMsg.getFirstInsertDate().getTime());
                if (_logMessageWindowService != null) {
                    ((LogMessageWindowServiceImpl) _logMessageWindowService).eventTextMessage(message);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
        stopMetricsTimer(timer);
    }

    // =========================== original setIsStateChanged ============
    /*
	 * JIRA:GWMON-4295 public void setIsStateChanged(int statelessHash) {
	 * Perform bulk update String hqlUpdate = UPDATE_STATE_CHANGED +
	 * statelessHash;
	 * 
	 * this.getHibernateTemplate().bulkUpdate(hqlUpdate); }
	 */
    @SuppressWarnings("unchecked")
    public void setIsStateChanged(int statelessHash) {
        CollageTimer timer = startMetricsTimer();
        // Perform bulk update
        LogMessage logMSG = new com.groundwork.collage.model.impl.LogMessage();
        Session session = this.getSession();

        List<LogMessage> logmsgs = session.createCriteria(logMSG.getClass())
                .add(Restrictions.eq("stateChanged", false))
                .add(Restrictions.eq("statelessHash", statelessHash)).list();
        Iterator<LogMessage> msgIterate = logmsgs.iterator();
        while (msgIterate.hasNext()) {
            LogMessage lmsg = msgIterate.next();
            lmsg.setStateChanged(true);
            save(lmsg);
        }
        stopMetricsTimer(timer);
    }

    public IntegerProperty createPreparedQuery(String query, String appType,
                                               String startRange, String endRange,
                                               org.groundwork.foundation.ws.model.impl.SortCriteria orderedBy,
                                               int firstResult, int maxResults) {
        throw new NotImplementedException(
                "LogMessageService.createPreparedQuery() is not implemented.");
    }

    public int deleteLogMessagesForDevice(String deviceIdentification) {
        CollageTimer timer = startMetricsTimer();
        FoundationQueryList messages = this
                .getLogMessagesByDeviceIdentification(deviceIdentification,
                        null, null, null, null, -1, -1);
        int messageDeleteCount = messages.size();
        this.delete(messages);
        stopMetricsTimer(timer);
        return messageDeleteCount;
    }

    // NOTE: this method is used to notify hibernate that a host has been
    // deleted.
    public int unlinkLogMessagesFromHost(String hostName) {
        CollageTimer timer = startMetricsTimer();
        // get the logmessages for the specified host
        FoundationQueryList messages = this.getLogMessagesByHostName(hostName,
                null, null, null, null, -1, -1);

        // iterate through the messages and set the host status to null
        Iterator msgIt = messages.iterator();
        while (msgIt.hasNext()) {
            LogMessage msg = (LogMessage) msgIt.next();
            msg.setHostStatus(null);
            this.save(msg);
        }
        stopMetricsTimer(timer);
        return messages.size();
    }

    // NOTE: this method is used to notify hibernate that a servicestatus has
    // been deleted.
    public int unlinkLogMessagesFromService(int serviceStatusId) {
        CollageTimer timer = startMetricsTimer();
        FoundationQueryList messages = this.getLogMessagesByServiceStatusId(
                serviceStatusId, null, null, null, null, -1, -1);

        Iterator msgIt = messages.iterator();
        while (msgIt.hasNext()) {
            LogMessage msg = (LogMessage) msgIt.next();
            msg.setServiceStatus(null);
            this.save(msg);
        }
        stopMetricsTimer(timer);

        return messages.size();
    }

    /**
     * Update log message operation status for the specified log messages.
     *
     * @param logMessageIds
     * @param opStatus
     * @throws CollageException
     */
    public int updateLogMessageOperationStatus(
            Collection<Integer> logMessageIds, String opStatus,
            String updatedBy, String comments) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (logMessageIds == null || logMessageIds.size() < 1) {
            throw new IllegalArgumentException(
                    "Invalid null or empty log message id array parameter");
        }

        if (opStatus == null || opStatus.length() == 0) {
            throw new IllegalArgumentException(
                    "Invalid null or empty operation status parameter.");
        }

        try {
            OperationStatus operationStatus = this._metadataService
                    .getOperationStatusByName(opStatus);
            if (operationStatus == null)
                throw new BusinessServiceException(
                        "Unable to log message operation status - Operation Status Not Found, ID: "
                                + opStatus);

            // Perform bulk update
			/*
			 * GWMON-4827 Fix If the Operation Status changes the hash for
			 * consolidation needs to be reset as well. The has is no longer
			 * valid.
			 */
            StringBuilder hqlUpdate = new StringBuilder(
                    "update LogMessage set StatelessHash=-1, ConsolidationHash=-1, operationStatus.operationStatusId = "
                            + operationStatus.getOperationStatusId());
            // February 14 2008, 3:12pm cpora - I am trying to fix the deadlock
            // bug
            // StringBuilder hqlUpdate = new StringBuilder(
            // "update LogMessage set OperationStatusID = " +
            // operationStatus.getOperationStatusId());
            // Build where clause
            StringBuilder sbWhere = new StringBuilder(
                    (16 * logMessageIds.size()) + 16);
            Iterator<Integer> it = logMessageIds.iterator();
            while (it.hasNext()) {
                if (sbWhere.length() == 0) {
                    sbWhere.append(WHERE_BEGIN);
                } else {
                    sbWhere.append(OR);
                }

                sbWhere.append(ID_EQUALS);
                sbWhere.append(it.next());
            }

            hqlUpdate.append(sbWhere.toString());
            if (comments != null)
                this.updateLogMessageProperty(logMessageIds, opStatus, updatedBy, comments);
            return this.getHibernateTemplate().bulkUpdate(hqlUpdate.toString());
        } catch (Exception e) {
            String msg = "LogMessageService.updateLogMessageOperationStatus() failed.";
            log.error(msg, e);
            throw new BusinessServiceException(msg, e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Update log message operation status for the specified log messages.
     *
     * @param logMessageIds
     * @param opStatus
     * @throws CollageException
     */
    public int updateLogMessageOperationStatus(
            Collection<Integer> logMessageIds, String opStatus,
            HashMap<String, Object> prop) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (logMessageIds == null || logMessageIds.size() < 1) {
            throw new IllegalArgumentException(
                    "Invalid null or empty log message id array parameter");
        }

        if (opStatus == null || opStatus.length() == 0) {
            throw new IllegalArgumentException(
                    "Invalid null or empty operation status parameter.");
        }

        try {
            OperationStatus operationStatus = this._metadataService
                    .getOperationStatusByName(opStatus);
            if (operationStatus == null)
                throw new BusinessServiceException(
                        "Unable to log message operation status - Operation Status Not Found, ID: "
                                + opStatus);

            // Perform bulk update
			/*
			 * GWMON-4827 Fix If the Operation Status changes the hash for
			 * consolidation needs to be reset as well. The has is no longer
			 * valid.
			 */
            StringBuilder hqlUpdate = new StringBuilder(
                    "update LogMessage set StatelessHash=-1, ConsolidationHash=-1, operationStatus.operationStatusId = "
                            + operationStatus.getOperationStatusId());
            // February 14 2008, 3:12pm cpora - I am trying to fix the deadlock
            // bug
            // StringBuilder hqlUpdate = new StringBuilder(
            // "update LogMessage set OperationStatusID = " +
            // operationStatus.getOperationStatusId());
            // Build where clause
            StringBuilder sbWhere = new StringBuilder(
                    (16 * logMessageIds.size()) + 16);
            Iterator<Integer> it = logMessageIds.iterator();
            while (it.hasNext()) {
                if (sbWhere.length() == 0) {
                    sbWhere.append(WHERE_BEGIN);
                } else {
                    sbWhere.append(OR);
                }

                sbWhere.append(ID_EQUALS);
                sbWhere.append(it.next());
            }

            hqlUpdate.append(sbWhere.toString());
            if (prop != null)
                this.updateLogMessageProperty(logMessageIds, opStatus, prop);
            return this.getHibernateTemplate().bulkUpdate(hqlUpdate.toString());
        } catch (Exception e) {
            String msg = "LogMessageService.updateLogMessageOperationStatus() failed.";
            log.error(msg, e);
            throw new BusinessServiceException(msg, e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Helper to update logmessage property. Filters out System and Nagios
     * messages
     *
     * @param logMessageIds
     * @param opStatus
     * @param prop
     * @return
     * @throws BusinessServiceException
     */
    private void updateLogMessageProperty(Collection<Integer> logMessageIds, String opStatus,
                                          HashMap<String, Object> prop) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        String PROP_COMMENTS = "Comments";
        String PROP_ACKNOWLEDGED_BY = "AcknowledgedBy";
        if (logMessageIds == null || logMessageIds.size() < 1) {
            throw new IllegalArgumentException(
                    "Invalid null or empty log message id array parameter");
        }

        try {
            Iterator<Integer> it = logMessageIds.iterator();
            while (it.hasNext()) {
                Integer logMessageId = it.next();

                // Filter out system and Nagios messages
                LogMessage logMessage = this.getLogMessageById(logMessageId);
                if (logMessage == null) {
                    throw new BusinessServiceException(
                            "Event id not found in log message db: "
                                    + logMessageId);
                }
                Iterator propIter = prop.entrySet().iterator();
                while (propIter.hasNext()) {
                    Map.Entry<String, Object> pairs = (Map.Entry<String, Object>) propIter
                            .next();
                    DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
                    String formattedDate = date.format(Calendar
                            .getInstance().getTime());
                    Object comments = prop.get(PROP_COMMENTS);
                    Object acknowledgedBy = prop.get(PROP_ACKNOWLEDGED_BY);
                    String prevComments = (String) logMessage
                            .getProperty(PROP_COMMENTS);
                    if (prevComments == null) {
                        comments = "{" + comments + "}" + "--" + formattedDate + "(" + acknowledgedBy + ")";
                    } else {
                        comments = "{" + comments + "}" + "--" + formattedDate + "(" + acknowledgedBy + ")" + ";"
                                + prevComments;
                    }
                    String key = pairs.getKey();
                    Object value = pairs.getValue();
                    if (key.equalsIgnoreCase(PROP_COMMENTS)) {
                        value = comments;
                    }
                    logMessage.set(key, value);
                }
                this.saveLogMessage(logMessage);
                // Now acknowledge host or service for non-nagios apptypes
                this.acknowledgeHostOrService(opStatus, logMessage);

            } // end while
        } catch (Exception e) {
            String msg = "LogMessageService.updateLogMessageProperty() failed.";
            log.error(msg, e);
            throw new BusinessServiceException(msg, e);
        } finally {
            stopMetricsTimer(timer);
        }

    }

    /**
     * Helper to acknowledge host or service for nan nagios app types
     *
     * @param opStatus
     * @param logMessage
     */
    private void acknowledgeHostOrService(String opStatus, LogMessage logMessage) {
        CollageTimer timer = startMetricsTimer();
        // if non-nagios acknowledge, acknowledge host or corresponding service.
        if (!logMessage.getApplicationType().getName()
                .equals(Nagios.APPLICATION_TYPE)) {
            CollageFactory factory = CollageFactory.getInstance();
            if (logMessage.getServiceStatus() == null) {
                // Acknowledge Host
                HostService hostService = factory.getHostService();
                HostStatus hostStatus = logMessage.getHostStatus();
                if (hostStatus != null) {
                    if (opStatus != null && opStatus.equalsIgnoreCase(OP_STATUS_ACKNOWLEDGED))
                        hostStatus.setProperty(IS_ACKNOWLEDGED, Boolean.valueOf("true"));
                    else
                        hostStatus.setProperty(IS_ACKNOWLEDGED, Boolean.valueOf("false"));
                    hostService.saveHostStatus(hostStatus);
                }
            } else {
                // Acknowledge service
                ServiceStatus serviceStatus = logMessage.getServiceStatus();
                if (serviceStatus != null) {
                    if (opStatus != null && opStatus.equalsIgnoreCase(OP_STATUS_ACKNOWLEDGED))
                        serviceStatus.setProperty(IS_ACKNOWLEDGED, Boolean.valueOf("true"));
                    else
                        serviceStatus.setProperty(IS_ACKNOWLEDGED, Boolean.valueOf("false"));
                    _serviceStatusService.saveService(serviceStatus);
                }
            }
        }
        stopMetricsTimer(timer);
    }

    /**
     * Helper to update logmessage property. Filters out System and Nagios
     * messages
     *
     * @param logMessageIds
     * @param updatedBy
     * @param comments
     * @return
     * @throws BusinessServiceException
     */
    private void updateLogMessageProperty(Collection<Integer> logMessageIds, String opStatus,
                                          String updatedBy, String comments) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (logMessageIds == null || logMessageIds.size() < 1) {
            throw new IllegalArgumentException(
                    "Invalid null or empty log message id array parameter");
        }

        try {
            String PROP_UPDATED_BY = "UpdatedBy";
            String PROP_COMMENTS = "Comments";
            Iterator<Integer> it = logMessageIds.iterator();
            while (it.hasNext()) {
                Integer logMessageId = it.next();

                // Filter out system and Nagios messages
                LogMessage logMessage = this.getLogMessageById(logMessageId);
                if (logMessage == null) {
                    throw new BusinessServiceException(
                            "Event id not found in log message db: "
                                    + logMessageId);
                }
                if (!logMessage.getApplicationType().getName()
                        .equals(ApplicationType.SYSTEM_APPLICATION_TYPE_NAME)
                        && !logMessage.getApplicationType().getName()
                        .equals(Nagios.APPLICATION_TYPE)) {
                    logMessage.set(PROP_UPDATED_BY, updatedBy);
                    DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
                    String formattedDate = date.format(Calendar.getInstance()
                            .getTime());
                    String prevComments = (String) logMessage
                            .getProperty(PROP_COMMENTS);
                    logMessage
                            .set(PROP_COMMENTS,
                                    ((prevComments == null) ? ("{" + comments + "}" + "--" + formattedDate + "(" + updatedBy + ")")
                                            : ("{" + comments + "}" + "--" + formattedDate + "(" + updatedBy + ")"
                                            + ";" + prevComments)));
                    this.saveLogMessage(logMessage);
                    this.acknowledgeHostOrService(opStatus, logMessage);
                } // end if
            } // end while
        } catch (Exception e) {
            String msg = "LogMessageService.updateLogMessageProperty() failed.";
            log.error(msg, e);
            throw new BusinessServiceException(msg, e);
        } finally {
            stopMetricsTimer(timer);
        }

    }

    /**
     * Returns a List of StateTransition instances for the specified host for the date range provided.
     * Uses optimized LogMessageWindowService if date range in window.
     *
     * @param hostName
     * @param startDate
     * @param endDate
     * @return list of state transitions
     * @throws BusinessServiceException
     */
    public List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate)
            throws BusinessServiceException {
        return getHostStateTransitions(hostName, startDate, endDate, true);
    }

    /**
     * Returns a List of StateTransition instances for the specified host for the date range provided.
     * Optionally use optimized LogMessageWindowService if date range in window.
     *
     * @param hostName
     * @param startDate
     * @param endDate
     * @param useWindow
     * @return list of state transitions
     * @throws BusinessServiceException
     */
    @Override
    public List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate,
                                                         boolean useWindow) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        try {
            if (hostName == null || hostName.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty host name parameter");

            if (startDate == null || startDate.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty start date parameter");

            if (endDate == null || endDate.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty end date parameter");

            if (startDate.equalsIgnoreCase(endDate) && startDate.length() == 10
                    && endDate.length() == 10) {
                startDate = startDate.concat(" 00:00:00");
                endDate = endDate.concat(" 23:59:59");
            }

            // delegate to log message window service
            if (useWindow && _logMessageWindowService != null) {
                try {
                    if (_logMessageWindowService.isWindowInitialized() && _logMessageWindowService.isInWindow(startDate, endDate)) {
                        List<StateTransition> stateTransitionList =
                                _logMessageWindowService.getHostStateTransitions(hostName, startDate, endDate);
                        if (stateTransitionList == null) {
                            log.error("LogMessageWindowService no result for host: "+hostName);
                        }
                        return stateTransitionList != null ? stateTransitionList : Collections.EMPTY_LIST;
                    }
                } catch (BusinessServiceException bse) {
                }
                if (log.isDebugEnabled()) {
                    log.debug("**** missed window: " + hostName + ", " + startDate + ", " + endDate);
                }
            }

            Date currDate = Calendar.getInstance().getTime();
            DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
            java.util.Date dtStartDate = null;
            java.util.Date dtEndDate = null;
            try {
                dtStartDate = date.parse(startDate);
                dtEndDate = date.parse(endDate);
            } catch (ParseException pe) {
                // Now check just for the date
                try {
                    DateFormat dateOnly = new SimpleDateFormat(DATE_ONLY_FORMAT);
                    dtStartDate = dateOnly.parse(startDate);
                    dtEndDate = dateOnly.parse(endDate);
                } catch (ParseException e) {
                    // Still if you don't recognize the format then throw the
                    // exception
                    throw new BusinessServiceException(e.getMessage());
                }
            }

            List<StateTransition> stateTransitionList = new ArrayList<>();

            if (dtStartDate.after(currDate)) {
                return stateTransitionList;
            }

            Object[] parameters = new Object[]{hostName, dtStartDate, hostName, dtStartDate, dtEndDate, hostName, dtEndDate};

            // Attempt to fetch all events in the range, as well as the last event before the range and the first event after the range to ensure the entire range is covered
            String sql = "(select l.monitorstatusid, m.name, l.firstinsertdate from logmessage l, monitorstatus m, host h, ApplicationType A where A.applicationtypeid=l.applicationtypeid and h.hostid=l.hoststatusid and h.hostname=?\n" +
                    "and A.name!='SYSTEM' and l.servicestatusid is null and m.monitorstatusid=l.monitorstatusid\n" +
                    "and l.firstinsertdate < ? order by l.firstinsertdate desc limit 1)\n" +
                    "UNION ALL (select distinct L.MonitorStatusID, M.Name, L.FirstInsertDate from LogMessage L, Host H, MonitorStatus M, ApplicationType A where A.applicationtypeid=L.applicationtypeid\n" +
                    "and A.name!='SYSTEM' and L.HostStatusID=H.HostID and L.servicestatusid is null and H.HostName=? and L.MonitorStatusID=M.MonitorStatusID and L.FirstInsertDate between ? and ? order by L.firstinsertdate)\n" +
                    "UNION ALL (select l.monitorstatusid, m.name, l.firstinsertdate from logmessage l, monitorstatus m, host h, ApplicationType A where A.applicationtypeid=l.applicationtypeid and h.hostid=l.hoststatusid and h.hostname=?\n" +
                    "and A.name!='SYSTEM' and l.servicestatusid is null and m.monitorstatusid=l.monitorstatusid\n" +
                    "and l.firstinsertdate > ? order by l.firstinsertdate asc limit 1)";

            List l = _foundationDAO.sqlQuery(sql, parameters);

            if (l.size() > 1) {
                for (int i = 0; i < l.size() - 1; i++) {
                    Object[] vals = (Object[]) l.get(i);
                    Object[] nextvals = (Object[]) l.get(i + 1);

                    int fromStateId = (Integer) vals[0];
                    String fromState = (String) vals[1];
                    Date fromDate = (Date) vals[2];

                    int toStateId = (Integer) nextvals[0];
                    String toState = (String) nextvals[1];
                    Date toDate = (Date) nextvals[2];

                    MonitorStatus fromStatus = new com.groundwork.collage.model.impl.MonitorStatus(fromStateId, fromState, fromState);
                    MonitorStatus toStatus = new com.groundwork.collage.model.impl.MonitorStatus(toStateId, toState, toState);

                    StateTransition transition = StateTransition.createStateTransition(hostName, null, fromStatus, fromDate, toStatus, toDate);
                    transition.setDurationInState(toDate.getTime() - fromDate.getTime());
                    stateTransitionList.add(transition);
                }
            } else if (l.size() == 1) {
                Object[] vals = (Object[]) l.get(0);

                int toStateId = (Integer) vals[0];
                String toState = (String) vals[1];
                Date toDate = (Date) vals[2];

                // Only add this event to the list if it is before or during the time window, else it is irrelevant and we should return with no data
                if (!toDate.after(dtEndDate)) {
                    MonitorStatus toStatus = new com.groundwork.collage.model.impl.MonitorStatus(toStateId, toState, toState);
                    StateTransition transition = StateTransition.createStateTransition(hostName, null, null, null, toStatus, toDate);
                    stateTransitionList.add(transition);
                }
            }
            return stateTransitionList;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Returns a List of StateTransition instances for the specified service for the date range provided.
     * If no service name is provided then all service state transitions for the host will be returned.
     * The list will be ordered by service and then each service transition will be in ascending transition date order
     * For example, Service1 Transition1, Service1 Transition2, Service2 Transition1, Service2, Transition2, etc.
     * Uses optimized LogMessageWindowService if date range in window.
     *
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
     * @return list of state transitions
     * @throws BusinessServiceException
     */
    public List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate,
                                                            String endDate) throws BusinessServiceException {
        return getServiceStateTransitions(hostName, serviceName, startDate, endDate, true);
    }

    /**
     * Returns a List of StateTransition instances for the specified service for the date range provided.
     * If no service name is provided then all service state transitions for the host will be returned.
     * The list will be ordered by service and then each service transition will be in ascending transition date order
     * For example, Service1 Transition1, Service1 Transition2, Service2 Transition1, Service2, Transition2, etc.
     * Optionally use optimized LogMessageWindowService if date range in window.
     *
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
     * @param useWindow
     * @return list of state transitions
     * @throws BusinessServiceException
     */
    @Override
    public List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate,
                                                            String endDate, boolean useWindow)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        try {
            if (hostName == null || hostName.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty host name parameter");

            if (startDate == null || startDate.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty start date parameter");

            if (endDate == null || endDate.length() == 0)
                throw new IllegalArgumentException(
                        "Invalid null / empty end date parameter");

            if (startDate.equalsIgnoreCase(endDate) && startDate.length() == 10
                    && endDate.length() == 10) {
                startDate = startDate.concat(" 00:00:00");
                endDate = endDate.concat(" 23:59:59");
            }

            // delegate to log message window service
            if (useWindow && _logMessageWindowService != null && serviceName != null && serviceName.length() > 0) {
                try {
                    if (_logMessageWindowService.isWindowInitialized() && _logMessageWindowService.isInWindow(startDate, endDate)) {
                        List<StateTransition> stateTransitionList =
                                _logMessageWindowService.getServiceStateTransitions(hostName, serviceName, startDate, endDate);
                        if (stateTransitionList == null) {
                            log.error("LogMessageWindowService no result for service: "+hostName+", "+serviceName);
                        }
                        return stateTransitionList != null ? stateTransitionList : Collections.EMPTY_LIST;
                    }
                } catch (BusinessServiceException bse) {
                }
                if (log.isDebugEnabled()) {
                    log.debug("**** missed window: " + serviceName + ", " + hostName + ", " + startDate + ", " + endDate);
                }
            }
            Date currDate = Calendar.getInstance().getTime();
            DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
            java.util.Date dtStartDate = null;
            java.util.Date dtEndDate = null;
            try {
                dtStartDate = date.parse(startDate);
                dtEndDate = date.parse(endDate);
            } catch (ParseException pe) {
                // Now check just for the date
                try {
                    DateFormat dateOnly = new SimpleDateFormat(DATE_ONLY_FORMAT);
                    dtStartDate = dateOnly.parse(startDate);
                    dtEndDate = dateOnly.parse(endDate);
                } catch (ParseException e) {
                    // Still if you don't recognize the format then throw the
                    // exception
                    throw new BusinessServiceException(e.getMessage());
                }
            }
            List<StateTransition> stateTransitionList = new ArrayList<>();

            if (dtStartDate.after(currDate)) {
                return stateTransitionList;
            }

            if (serviceName != null && serviceName.length() > 0) {
                Object[] parameters = new Object[]{hostName, serviceName, dtStartDate, hostName, serviceName, dtStartDate, dtEndDate, hostName, serviceName, dtEndDate};

                // Attempt to fetch all events in the range, as well as the last event before the range and the first event after the range to ensure the entire range is covered
                String sql = "(select l.monitorstatusid, m.name, l.firstinsertdate from servicestatus s, logmessage l, monitorstatus m, host h, ApplicationType A where A.applicationtypeid=l.applicationtypeid and h.hostid=l.hoststatusid and h.hostname=?\n" +
                        "and A.name!='SYSTEM' and servicedescription=? and s.servicestatusid=l.servicestatusid and l.hoststatusid=s.hostid and m.monitorstatusid=l.monitorstatusid\n" +
                        "and l.firstinsertdate < ? order by l.firstinsertdate desc limit 1)\n" +
                        "UNION ALL (select distinct L.MonitorStatusID, M.Name, L.FirstInsertDate from LogMessage L, Host H, MonitorStatus M, servicestatus S, ApplicationType A where A.applicationtypeid=L.applicationtypeid\n" +
                        "and A.name!='SYSTEM' and L.HostStatusID=H.HostID and L.servicestatusid is not null and S.servicestatusid=L.servicestatusid and H.HostName=? and L.MonitorStatusID=M.MonitorStatusID and S.servicedescription=? and L.FirstInsertDate between ? and ? order by L.firstinsertdate)\n" +
                        "UNION ALL (select l.monitorstatusid, m.name, l.firstinsertdate from servicestatus s, logmessage l, monitorstatus m, host h, ApplicationType A where A.applicationtypeid=l.applicationtypeid and h.hostid=l.hoststatusid and h.hostname=?\n" +
                        "and A.name!='SYSTEM' and servicedescription=? and s.servicestatusid=l.servicestatusid and l.hoststatusid=s.hostid and m.monitorstatusid=l.monitorstatusid\n" +
                        "and l.firstinsertdate > ? order by l.firstinsertdate asc limit 1)";
                List l = _foundationDAO.sqlQuery(sql, parameters);

                if (l.size() > 1) {
                    for (int i = 0; i < l.size() - 1; i++) {
                        Object[] vals = (Object[]) l.get(i);
                        Object[] nextvals = (Object[]) l.get(i + 1);

                        int fromStateId = (Integer) vals[0];
                        String fromState = (String) vals[1];
                        Date fromDate = (Date) vals[2];

                        int toStateId = (Integer) nextvals[0];
                        String toState = (String) nextvals[1];
                        Date toDate = (Date) nextvals[2];

                        MonitorStatus fromStatus = new com.groundwork.collage.model.impl.MonitorStatus(fromStateId, fromState, fromState);
                        MonitorStatus toStatus = new com.groundwork.collage.model.impl.MonitorStatus(toStateId, toState, toState);

                        StateTransition transition = StateTransition.createStateTransition(hostName, serviceName, fromStatus, fromDate, toStatus, toDate);
                        transition.setDurationInState(toDate.getTime() - fromDate.getTime());
                        stateTransitionList.add(transition);
                    }
                } else if (l.size() == 1) {
                    Object[] vals = (Object[]) l.get(0);

                    int toStateId = (Integer) vals[0];
                    String toState = (String) vals[1];
                    Date toDate = (Date) vals[2];

                    // Only add this event to the list if it is before or during the time window, else it is irrelevant and we should return with no data
                    if (!toDate.after(dtEndDate)) {
                        MonitorStatus toStatus = new com.groundwork.collage.model.impl.MonitorStatus(toStateId, toState, toState);
                        StateTransition transition = StateTransition.createStateTransition(hostName, serviceName, null, null, toStatus, toDate);
                        stateTransitionList.add(transition);
                    }
                }
            }
            return stateTransitionList;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*************************************************************************
     * Private Methods
     *************************************************************************/


    private boolean isStateTransition(Integer stateTransitionHash,
                                      Integer monitorStatusID) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        if (stateTransitionHash == null)
            throw new IllegalArgumentException(
                    "Invalid null State Transition Hash Integer parameter.");

        // utilize HQL to first efficiently select the id of the most
        // recent log message and then use it to return a single
        // LogMessage result, (avoid sorting potentially many full log
        // message result sets if objects are queried)
        List results =_foundationDAO.queryLimit(
                "select lm." + LogMessage.HP_MONITOR_STATUS_ID + " from LogMessage lm " +
                        "where " +
                        "lm." + LogMessage.HP_STATETRANSITIONHASH + " = ? " +
                        "order by " +
                        "lm." + LogMessage.HP_FIRST_INSERT_DATE + " desc, " +
                        "lm." + LogMessage.HP_ID + " desc",
                stateTransitionHash, 1);

        if (results == null || results.size() == 0) {
            return true; // If no LogMessage was found then we consider it a
        }
        // state transition

        // Compare monitor status ids - If the are not equal then it is a state
        // transition
        stopMetricsTimer(timer);
        Integer lastMonitorStatusID = (Integer)results.get(0);
        return !((lastMonitorStatusID != null && lastMonitorStatusID.equals(monitorStatusID)) ||
                (lastMonitorStatusID == null && monitorStatusID == null));
    }

    private boolean isStateTransition(Integer stateTransitionHash,
                                      MonitorStatus monitorStatus) throws BusinessServiceException {
        if (stateTransitionHash == null)
            throw new IllegalArgumentException(
                    "Invalid null State Transition Hash Integer parameter.");

        // Compare monitor status ids
        return isStateTransition(stateTransitionHash,
                monitorStatus != null ? monitorStatus.getMonitorStatusId() : null);
    }

    public Integer buildStateTransitionHash(LogMessage logMsg) {
        CollageTimer timer = startMetricsTimer();
        ApplicationType appType = logMsg.getApplicationType();
        if (appType == null)
            throw new BusinessServiceException(
                    "Invalid LogMessage - no application type");

        List<String> criteriaList = appType.getStateTransitionCriteriaList();

        if (criteriaList == null || criteriaList.size() == 0)
            return null;

        StringBuilder hashString = new StringBuilder(32);
        Object value = null;
        Iterator<String> propertyList = criteriaList.iterator();
        while (propertyList.hasNext()) {
            value = logMsg.getProperty(propertyList.next());

            if (value != null) {
                hashString.append(value.toString());
            }
        }

        stopMetricsTimer(timer);
        // Return hash code for string;
        return new Integer(hashString.toString().hashCode());
    }

    /**
     * Gets the log messages when criteria is passed as input. Supports more
     * than one dynamic property in the criteria.
     *
     * @param filter
     * @return
     * @throws BusinessServiceException
     */
    public List<LogMessage> getLogMessagesByCriteria(FilterCriteria filter)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
        List<LogMessage> logMsgs = null;

		/*
		 * log.warn(filter.getPropertyValuePairs() + ": Count : " +
		 * filter.getPropertyValuePairs().size());
		 */
        logMsgs = new ArrayList<LogMessage>();
        Map critMap = filter.getPropertyValuePairs();
        StringBuilder sql = new StringBuilder("from LogMessage ");
        boolean addWhereClause = false;

        // Basically a where clause exists only if there is a condition
        if (critMap.size() > 0)
            addWhereClause = true;

        if (addWhereClause)
            sql.append("where ");

        String dynaSQLSuffix_0 = "logMessageId in (select logMessageId from LogMessage where  "
                + " propertyValues.name='";
        String dynaSQLSuffix_1 = "' and propertyValues.valueString='";
        String dynaSQLSuffix_2 = "') ";

        // EntityProperty to Hibernate Property mappings
        Map<String, String> hibMap = new HashMap<String, String>();
        hibMap.put(LogMessage.EP_DEVICE_IDENTIFICATION,
                LogMessage.HP_DEVICE_IDENTIFICATION);
        hibMap.put(LogMessage.EP_HOST_NAME, LogMessage.HP_HOST_NAME);
        hibMap.put(LogMessage.EP_SERVICE_STATUS_DESCRIPTION,
                LogMessage.HP_SERVICE_STATUS_DESCRIPTION);
        hibMap.put(LogMessage.EP_APP_SEVERITY_NAME,
                LogMessage.HP_APP_SEVERITY_NAME);
        hibMap.put(LogMessage.EP_TEXT_MESSAGE, LogMessage.HP_TEXT_MESSAGE);
        hibMap.put(LogMessage.EP_OPERATION_STATUS_NAME,
                LogMessage.HP_OPERATION_STATUS_NAME);
        hibMap.put(LogMessage.EP_REPORT_DATE, LogMessage.HP_REPORT_DATE);
        hibMap.put(LogMessage.EP_LAST_INSERT_DATE,
                LogMessage.HP_LAST_INSERT_DATE);
        hibMap.put(LogMessage.EP_MONITOR_STATUS_NAME,
                LogMessage.HP_MONITOR_STATUS_NAME);

        Iterator it = critMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String key = (String) pairs.getKey();
            String value = (String) pairs.getValue();
            // log.warn("Key=" + key + ";" + "Value=" + value );
            if (hibMap.containsValue(key)) {
                if (i != 0)
                    sql.append("and ");
                sql.append(key + " = '" + value + "' ");
                i++;
            } // endif
            else {
                if (key.startsWith("propertyValues.name")) {
                    if (i != 0)
                        sql.append("and ");
                    sql.append(dynaSQLSuffix_0);
                    String dynaPropName = (String) critMap.get(key);
                    sql.append(dynaPropName);
                    sql.append(dynaSQLSuffix_1);
                    String replacedKey = key
                            .replaceFirst("name", "valueString");
                    String dynaPropValue = (String) critMap.get(replacedKey);
                    sql.append(dynaPropValue);
                    sql.append(dynaSQLSuffix_2);
                }
                i++;
            } // end if
        }
        log.debug("SQL:" + sql.toString());
        logMsgs = _foundationDAO.query(sql.toString());
        stopMetricsTimer(timer);
        return logMsgs;
    }

    public FoundationQueryList queryEvents(String hql, String hqlCount,
                                           int firstResult, int maxResults) {
        CollageTimer timer = startMetricsTimer();
        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
        stopMetricsTimer(timer);
        return list;
    }

    public void removeLogMessage(int id) {
        this.delete(id);
    }

}
