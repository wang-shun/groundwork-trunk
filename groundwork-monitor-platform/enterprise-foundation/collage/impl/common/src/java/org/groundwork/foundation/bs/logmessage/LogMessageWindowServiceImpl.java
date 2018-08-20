package org.groundwork.foundation.bs.logmessage;

import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.impl.StateTransition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.events.EntityPublisher;
import org.groundwork.foundation.bs.events.EntityPublisherListener;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.hibernate.StatelessSession;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogMessageWindowServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LogMessageWindowServiceImpl extends HibernateDaoSupport implements LogMessageWindowService, EntityPublisherListener {

    private static Log log = LogFactory.getLog(LogMessageWindowServiceImpl.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final DateFormat DATE_FORMAT_US = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");

    private static final String LOGMESSAGE_WINDOW_ENABLED_PROPERTY_NAME = "logmessage.window.enabled";
    private static final String LOGMESSAGE_WINDOW_ENABLED_PROPERTY_DEFAULT = "false";
    private static final String LOGMESSAGE_WINDOW_SIZE_HOURS_PROPERTY_NAME = "logmessage.window.size.hours";
    private static final String LOGMESSAGE_WINDOW_SIZE_HOURS_PROPERTY_DEFAULT = "24";
    private static final String LOGMESSAGE_WINDOW_UPDATE_INTERVAL_SECONDS_PROPERTY_NAME = "logmessage.window.update.interval.seconds";
    private static final String LOGMESSAGE_WINDOW_UPDATE_INTERVAL_SECONDS_PROPERTY_DEFAULT = "5";

    private static final long WINDOW_UPDATE_THREAD_SHUTDOWN_WAIT = 10000L;
    private static final int LOGMESSAGE_FETCH_SIZE = 32;

    private static final String APPLICATION_TYPE_NAMES_AND_IDS =
            "select name, applicationtypeid "+
            "from applicationtype";

    private static final String MONITOR_STATUS_NAMES_AND_IDS =
            "select name, monitorstatusid "+
            "from monitorstatus";

    private static final String HOST_NAMES_AND_IDS =
            "select hostname, hostid "+
            "from host";

    private static final String HOST_NAME =
            "select hostname "+
            "from host "+
            "where "+
            "hostid = ?";

    private static final String SERVICE_NAMES_AND_IDS =
            "select h.hostname, ss.servicedescription, h.hostid, ss.servicestatusid "+
            "from servicestatus ss "+
            "join host h on (h.hostid = ss.hostid)";

    private static final String SERVICE_NAME =
            "select h.hostname, ss.servicedescription, h.hostid "+
            "from servicestatus ss "+
            "join host h on (h.hostid = ss.hostid) "+
            "where "+
            "ss.servicestatusid = ?";

    private static final String PREVIOUS_SERVICE_LOG_MESSAGES =
            "with previousstatusdate as ("+
                    "select max(firstinsertdate) "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "applicationtypeid != ? and "+     // downtime application type id
                    "applicationtypeid != ? and "+     // system application type id
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid = ? and "+        // service id
                    "firstinsertdate < ?) "+           // window start
            "(" +
                    "select firstinsertdate, monitorstatusid "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid = ? and "+        // service id
                    "applicationtypeid = ? and "+      // downtime application type id
                    "firstinsertdate < (select max from previousstatusdate) "+
                    "order by firstinsertdate desc "+
                    "limit 1"+
            ") union all ("+
                    "select firstinsertdate, monitorstatusid "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid = ? and "+        // service id
                    "applicationtypeid != ? and "+     // system application type id
                    "firstinsertdate >= (select max from previousstatusdate) and firstinsertdate < ?"+  // window start
                    "order by firstinsertdate asc"+
            ")";

    private static final String WINDOW_SERVICE_LOG_MESSAGES =
            "select firstinsertdate, monitorstatusid, servicestatusid "+
            "from logmessage "+
            "where "+
            "monitorstatusid is not null and "+
            "applicationtypeid != ? and "+             // system application type id
            "hoststatusid is not null and "+
            "servicestatusid is not null and "+
            "firstinsertdate >= ? "+                   // window start
            "order by firstinsertdate asc";

    private static final String PREVIOUS_HOST_LOG_MESSAGES =
            "with previousstatusdate as ("+
                    "select max(firstinsertdate) "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "applicationtypeid != ? and "+     // downtime application type id
                    "applicationtypeid != ? and "+     // system application type id
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid is null and "+
                    "firstinsertdate < ?) "+           // window start
            "(" +
                    "select firstinsertdate, monitorstatusid "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid is null and "+
                    "applicationtypeid = ? and "+      // downtime application type id
                    "firstinsertdate < (select max from previousstatusdate) "+
                    "order by firstinsertdate desc "+
                    "limit 1"+
            ") union all ("+
                    "select firstinsertdate, monitorstatusid "+
                    "from logmessage "+
                    "where "+
                    "monitorstatusid is not null and "+
                    "hoststatusid = ? and "+           // host id
                    "servicestatusid is null and "+
                    "applicationtypeid != ? and "+     // system application type id
                    "firstinsertdate >= (select max from previousstatusdate) and firstinsertdate < ?"+  // window start
                    "order by firstinsertdate asc"+
            ")";

    private static final String WINDOW_HOST_LOG_MESSAGES =
            "select firstinsertdate, monitorstatusid, hoststatusid "+
            "from logmessage "+
            "where "+
            "monitorstatusid is not null and "+
            "applicationtypeid != ? and "+             // system application type id
            "hoststatusid is not null and "+
            "servicestatusid is null and "+
            "firstinsertdate >= ? "+                   // window start
            "order by firstinsertdate asc";

    private static final String FOUNDATION_ENTITIES_XML_AGGREGATE_TAG_NAME = "AGGREGATE";
    private static final String FOUNDATION_ENTITIES_XML_ENTITY_TAG_NAME = "ENTITY";
    private static final String FOUNDATION_ENTITIES_XML_TYPE_ATTR_NAME = "TYPE";
    private static final String FOUNDATION_ENTITIES_XML_TEXT_ATTR_NAME = "TEXT";

    private static final String LOG_MESSAGE_EVENT_XML_TAG = "<"+LogMessageService.LOG_MESSAGE_EVENT_XML_TAG_NAME+"/>";
    private static final String FOUNDATION_ENTITIES_XML_AGGREGATE_TAG = "<"+FOUNDATION_ENTITIES_XML_AGGREGATE_TAG_NAME+">";
    private static final ConcurrentHashMap<String, Pattern> FOUNDATION_ENTITIES_PATTERNS_CACHE = new ConcurrentHashMap<>();

    private final EntityPublisher entityPublisher;
    private boolean windowEnabled;
    private final long windowSizeMillis;
    private final long windowUpdateIntervalMillis;
    private final Queue<TextMessage> logMessageEventMessageQueue = new ConcurrentLinkedQueue<TextMessage>();
    private final Queue<TextMessage> foundationEntityMessageQueue = new ConcurrentLinkedQueue<TextMessage>();

    private Thread windowUpdateThread;
    private volatile long windowStartMillis;
    private int downtimeApplicationTypeId;
    private int systemApplicationTypeId;
    private int startDowntimeMonitorStatusId;
    private int inDowntimeMonitorStatusId;
    private int endDowntimeMonitorStatusId;
    private int criticalMonitorStatusId;
    private int scheduledCriticalMonitorStatusId;
    private int unscheduledCriticalMonitorStatusId;
    private int downMonitorStatusId;
    private int scheduledDownMonitorStatusId;
    private int unscheduledDownMonitorStatusId;
    private Map<Integer, MonitorStatus> monitorStatuses = new HashMap<>();
    private Map<String, Integer> hostIds = new ConcurrentHashMap<>();
    private Map<Integer, String> hostNames = new ConcurrentHashMap<>();
    private Map<String, Integer []> serviceKeyIds = new ConcurrentHashMap<>();
    private Map<Integer, String> serviceKeys = new ConcurrentHashMap<>();
    private Map<Integer, List<LogMessageEvent>> hostLogMessageEvents = new ConcurrentHashMap<>();
    private Map<Integer, List<LogMessageEvent>> serviceLogMessageEvents = new ConcurrentHashMap<>();
    private volatile boolean windowInitialized;

    /**
     * Construct LogMessage window service implementation.
     *
     * @param entityPublisher entity publisher
     * @param configuration foundation properties configuration
     */
    public LogMessageWindowServiceImpl(EntityPublisher entityPublisher, Properties configuration) {
        this.entityPublisher = entityPublisher;
        this.windowEnabled = Boolean.parseBoolean(configuration.getProperty(LOGMESSAGE_WINDOW_ENABLED_PROPERTY_NAME,
                LOGMESSAGE_WINDOW_ENABLED_PROPERTY_DEFAULT));
        int windowSizeHours = Math.max(
                Integer.parseInt(configuration.getProperty(LOGMESSAGE_WINDOW_SIZE_HOURS_PROPERTY_NAME,
                        LOGMESSAGE_WINDOW_SIZE_HOURS_PROPERTY_DEFAULT)),
                Integer.parseInt(LOGMESSAGE_WINDOW_SIZE_HOURS_PROPERTY_DEFAULT));
        this.windowSizeMillis = windowSizeHours*3600000L;
        int windowUpdateIntervalSeconds = Math.max(
                Integer.parseInt(configuration.getProperty(LOGMESSAGE_WINDOW_UPDATE_INTERVAL_SECONDS_PROPERTY_NAME,
                        LOGMESSAGE_WINDOW_UPDATE_INTERVAL_SECONDS_PROPERTY_DEFAULT)),
                Integer.parseInt(LOGMESSAGE_WINDOW_UPDATE_INTERVAL_SECONDS_PROPERTY_DEFAULT));
        this.windowUpdateIntervalMillis = windowUpdateIntervalSeconds*1000L;
    }

    /**
     * Initialize background window processing.
     *
     * @throws BusinessServiceException
     */
    public void initialize() throws BusinessServiceException {
        // clear event queues
        logMessageEventMessageQueue.clear();
        foundationEntityMessageQueue.clear();
        // start window update thread
        if (windowEnabled && windowUpdateThread == null) {
            windowUpdateThread = new Thread(new WindowUpdateThread(), "LogMessageWindowServiceWindowUpdateThread");
            windowUpdateThread.setDaemon(true);
            windowUpdateThread.start();
        }
        // start listening to entity events
        if (entityPublisher != null) {
            entityPublisher.addEntityPublisherListener(this);
        }
    }

    @Override
    public void eventTextMessage(TextMessage textMessage) throws JMSException {
        // queue log message and foundation event text message for window update
        if (windowEnabled && windowUpdateThread != null && textMessage.getText() != null) {
            if (textMessage.getText().equals(LOG_MESSAGE_EVENT_XML_TAG)) {
                logMessageEventMessageQueue.add(textMessage);
            } else if (textMessage.getText().startsWith(FOUNDATION_ENTITIES_XML_AGGREGATE_TAG)) {
                foundationEntityMessageQueue.add(textMessage);
            }
        }
    }

    private class WindowUpdateThread implements Runnable {
        @Override
        public synchronized void run() {
            try {
                windowInitialized = false;
                log.info("LogMessageWindowService window update started");
                // initialize window
                initializeWindow();
                windowInitialized = true;
                // update window based on interval
                log.info("LogMessageWindowService window initialized");
                while (windowUpdateThread != null) {
                    try {
                        wait(windowUpdateIntervalMillis);
                    } catch (InterruptedException ie) {
                    }
                    if (windowUpdateThread == null) {
                        break;
                    }
                    // update interval
                    log.debug("LogMessageWindowService window updating");
                    updateWindow();
                    log.debug("LogMessageWindowService window updated");
                }
            } catch (Exception e) {
                log.error("LogMessageWindowService window update unexpected exception: "+e, e);
            } finally {
                windowInitialized = false;
                log.info("LogMessageWindowService window update stopped");
            }
        }

        private void initializeWindow() throws Exception {
            // clear window
            downtimeApplicationTypeId = 0;
            systemApplicationTypeId = 0;
            startDowntimeMonitorStatusId = 0;
            inDowntimeMonitorStatusId = 0;
            endDowntimeMonitorStatusId = 0;
            criticalMonitorStatusId = 0;
            scheduledCriticalMonitorStatusId = 0;
            unscheduledCriticalMonitorStatusId = 0;
            downMonitorStatusId = 0;
            scheduledDownMonitorStatusId = 0;
            unscheduledDownMonitorStatusId = 0;
            hostIds.clear();
            hostNames.clear();
            serviceKeyIds.clear();
            serviceKeys.clear();
            hostLogMessageEvents.clear();
            serviceLogMessageEvents.clear();
            long initWindowStartMillis = System.currentTimeMillis()-windowSizeMillis;

            // load application types
            executeQuery(APPLICATION_TYPE_NAMES_AND_IDS, null, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    String applicationTypeName = results.getString(1);
                    int applicationTypeId = results.getInt(2);
                    if ("DOWNTIME".equals(applicationTypeName)) {
                        downtimeApplicationTypeId = applicationTypeId;
                    } else if ("SYSTEM".equals(applicationTypeName)) {
                        systemApplicationTypeId = applicationTypeId;
                    }
                }
            });
            if (downtimeApplicationTypeId == 0 || systemApplicationTypeId == 0) {
                throw new RuntimeException("Application type ids not loaded");
            }

            // load monitor status
            executeQuery(MONITOR_STATUS_NAMES_AND_IDS, null, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    String monitorStatusName = results.getString(1);
                    int monitorStatusId = results.getInt(2);
                    MonitorStatus monitorStatus =
                            new com.groundwork.collage.model.impl.MonitorStatus(monitorStatusId, monitorStatusName,
                                    monitorStatusName);
                    monitorStatuses.put(monitorStatusId, monitorStatus);
                    if ("START DOWNTIME".equals(monitorStatusName)) {
                        startDowntimeMonitorStatusId = monitorStatusId;
                    } else if ("IN DOWNTIME".equals(monitorStatusName)) {
                        inDowntimeMonitorStatusId = monitorStatusId;
                    } else if ("END DOWNTIME".equals(monitorStatusName)) {
                        endDowntimeMonitorStatusId = monitorStatusId;
                    } else if ("CRITICAL".equals(monitorStatusName)) {
                        criticalMonitorStatusId = monitorStatusId;
                    } else if ("SCHEDULED CRITICAL".equals(monitorStatusName)) {
                        scheduledCriticalMonitorStatusId = monitorStatusId;
                    } else if ("UNSCHEDULED CRITICAL".equals(monitorStatusName)) {
                        unscheduledCriticalMonitorStatusId = monitorStatusId;
                    } else if ("DOWN".equals(monitorStatusName)) {
                        downMonitorStatusId = monitorStatusId;
                    } else if ("SCHEDULED DOWN".equals(monitorStatusName)) {
                        scheduledDownMonitorStatusId = monitorStatusId;
                    } else if ("UNSCHEDULED DOWN".equals(monitorStatusName)) {
                        unscheduledDownMonitorStatusId = monitorStatusId;
                    }
                }
            });
            if (startDowntimeMonitorStatusId == 0 || inDowntimeMonitorStatusId == 0 || endDowntimeMonitorStatusId == 0 ||
                    criticalMonitorStatusId == 0 || scheduledCriticalMonitorStatusId == 0 ||
                    unscheduledCriticalMonitorStatusId == 0 || downMonitorStatusId == 0 ||
                    scheduledDownMonitorStatusId == 0 || unscheduledDownMonitorStatusId == 0) {
                throw new RuntimeException("Monitor status ids not loaded");
            }

            // load hosts
            executeQuery(HOST_NAMES_AND_IDS, null, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    String hostName = results.getString(1);
                    int hostId = results.getInt(2);
                    hostIds.put(hostName, hostId);
                    hostNames.put(hostId, hostName);
                }
            });

            // load services
            executeQuery(SERVICE_NAMES_AND_IDS, null, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    String hostName = results.getString(1);
                    String serviceName = results.getString(2);
                    int hostId = results.getInt(3);
                    int serviceId = results.getInt(4);
                    String serviceKey = hostName + ":" + serviceName;
                    serviceKeyIds.put(serviceKey, new Integer[]{hostId, serviceId});
                    serviceKeys.put(serviceId, serviceKey);
                }
            });

            // load previous service log messages
            for (Integer[] hostAndServiceIds : serviceKeyIds.values()) {
                int hostId = hostAndServiceIds[0];
                final int serviceId = hostAndServiceIds[1];
                executeQuery(PREVIOUS_SERVICE_LOG_MESSAGES, new Object[]{
                        downtimeApplicationTypeId,
                        systemApplicationTypeId,
                        hostId,
                        serviceId,
                        initWindowStartMillis,
                        hostId,
                        serviceId,
                        downtimeApplicationTypeId,
                        hostId,
                        serviceId,
                        systemApplicationTypeId,
                        initWindowStartMillis
                }, new QueryResults() {
                    @Override
                    public void result(ResultSet results) throws Exception {
                        // log message result
                        Timestamp firstInsertDate = results.getTimestamp(1);
                        int monitorStatusId = results.getInt(2);
                        // add to service log message events
                        List<LogMessageEvent> logMessageEvents = getServiceLogMessageEvents(serviceId, true);
                        insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDate.getTime(), monitorStatusId));
                    }
                });
            }

            // load previous host log messages
            for (final Integer hostId : hostIds.values()) {
                executeQuery(PREVIOUS_HOST_LOG_MESSAGES, new Object[]{
                                downtimeApplicationTypeId,
                                systemApplicationTypeId,
                                hostId,
                                initWindowStartMillis,
                                hostId,
                                downtimeApplicationTypeId,
                                hostId,
                                systemApplicationTypeId,
                                initWindowStartMillis
                }, new QueryResults() {
                    @Override
                    public void result(ResultSet results) throws Exception {
                        // log message result
                        Timestamp firstInsertDate = results.getTimestamp(1);
                        int monitorStatusId = results.getInt(2);
                        // add to host log message events
                        List<LogMessageEvent> logMessageEvents = getHostLogMessageEvents(hostId, true);
                        insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDate.getTime(), monitorStatusId));
                    }
                });
            }

            // load window service log messages
            executeQuery(WINDOW_SERVICE_LOG_MESSAGES, new Object[]{
                    systemApplicationTypeId,
                    initWindowStartMillis
            }, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    // log message result
                    Timestamp firstInsertDate = results.getTimestamp(1);
                    int monitorStatusId = results.getInt(2);
                    final int serviceId = results.getInt(3);
                    // add to service log message events
                    List<LogMessageEvent> logMessageEvents = getServiceLogMessageEvents(serviceId, true);
                    insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDate.getTime(), monitorStatusId));
                    // add new service if required
                    addNewService(serviceId);
                }
            });

            // load window host log messages
            executeQuery(WINDOW_HOST_LOG_MESSAGES, new Object[]{
                    systemApplicationTypeId,
                    initWindowStartMillis
            }, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    // log message result
                    Timestamp firstInsertDate = results.getTimestamp(1);
                    int monitorStatusId = results.getInt(2);
                    final int hostId = results.getInt(3);
                    // add to host log message events
                    List<LogMessageEvent> logMessageEvents = getHostLogMessageEvents(hostId, true);
                    insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDate.getTime(), monitorStatusId));
                    // add new host if necessary
                    addNewHost(hostId);
                }
            });

            // window initialized
            windowStartMillis = initWindowStartMillis;
        }

        private void updateWindow() throws Exception {
            // compute new window start
            long newWindowStartMillis = System.currentTimeMillis()-windowSizeMillis;

            // trim/extend window log message events to new window
            trimLogMessageEvents(newWindowStartMillis, serviceLogMessageEvents.values());
            trimLogMessageEvents(newWindowStartMillis, hostLogMessageEvents.values());

            // load log message events
            while (true) {
                // pull and validate log message event from queue
                TextMessage message = logMessageEventMessageQueue.poll();
                if (message == null) {
                    break;
                }
                Integer serviceId = message.propertyExists("serviceId") ? message.getIntProperty("serviceId") : null;
                Integer hostId = message.propertyExists("hostId") ? message.getIntProperty("hostId") : null;
                Integer monitorStatusId = message.propertyExists("monitorStatusId") ? message.getIntProperty("monitorStatusId") : null;
                Long firstInsertDateMillis = message.propertyExists("firstInsertDate") ? message.getLongProperty("firstInsertDate") : null;
                if ((serviceId == null && hostId == null) || monitorStatusId == null || firstInsertDateMillis == null) {
                    continue;
                }
                if (firstInsertDateMillis < newWindowStartMillis) {
                    continue;
                }
                // add log message event
                if (serviceId != null) {
                    // add to service log message events
                    List<LogMessageEvent> logMessageEvents = getServiceLogMessageEvents(serviceId, true);
                    insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDateMillis, monitorStatusId));
                    // add new service if required
                    addNewService(serviceId);
                } else {
                    // add to host log message events
                    List<LogMessageEvent> logMessageEvents = getHostLogMessageEvents(hostId, true);
                    insertLogMessageEvent(logMessageEvents, new LogMessageEvent(firstInsertDateMillis, monitorStatusId));
                    // add new host if necessary
                    addNewHost(hostId);
                }
            }

            // parse foundation entities for service or host delete
            while (true) {
                // pull and parse foundation entities from queue
                TextMessage message = foundationEntityMessageQueue.poll();
                if (message == null) {
                    break;
                }
                List<Integer> deletedServiceIds =
                        parseEntityIdsForAction(message, ServiceNotifyEntityType.SERVICESTATUS, ServiceNotifyAction.DELETE);
                List<Integer> deletedHostIds =
                        parseEntityIdsForAction(message, ServiceNotifyEntityType.HOST, ServiceNotifyAction.DELETE);
                List<Integer> renamedHostIds =
                        parseEntityIdsForAction(message, ServiceNotifyEntityType.HOST, ServiceNotifyAction.RENAME);
                // remove or rename service or host
                for (Integer removeServiceId : deletedServiceIds) {
                    removeDeletedService(removeServiceId);
                }
                for (Integer removeHostId : deletedHostIds) {
                    removeDeletedHost(removeHostId);
                }
                for (Integer renameHostId : renamedHostIds) {
                    renameHost(renameHostId);
                }
            }

            // window update
            windowStartMillis = newWindowStartMillis;
        }

        private void trimLogMessageEvents(long windowStartMillis, Collection<List<LogMessageEvent>> logMessageEvents) {
            for (List<LogMessageEvent> logMessageEventList : logMessageEvents) {
                if (!logMessageEventList.isEmpty()) {
                    synchronized (logMessageEventList) {
                        // find previous status log message events
                        int previousStatusLogMessageEventIndex = -1;
                        int previousStartDowntimeLogMessageEventIndex = -1;
                        int previousInDowntimeStatusLogMessageEventIndex = -1;
                        int initialMonitorStatusId = logMessageEventList.get(0).monitorStatusId;
                        boolean inDowntime = (initialMonitorStatusId == inDowntimeMonitorStatusId ||
                                initialMonitorStatusId == endDowntimeMonitorStatusId);
                        int lastStartDowntimeLogMessageEventIndex = -1;
                        for (int i = 0, limit = logMessageEventList.size(); i < limit; i++) {
                            LogMessageEvent logMessageEvent = logMessageEventList.get(i);
                            // done if log message event in window
                            if (logMessageEvent.firstInsertDateMillis >= windowStartMillis) {
                                break;
                            }
                            int monitorStatusId = logMessageEvent.monitorStatusId;
                            if (monitorStatusId == startDowntimeMonitorStatusId ||
                                    monitorStatusId == inDowntimeMonitorStatusId) {
                                // log message event (re)sets in downtime
                                inDowntime = true;
                                lastStartDowntimeLogMessageEventIndex = i;
                            } else if (monitorStatusId == endDowntimeMonitorStatusId) {
                                // log message event clears in downtime
                                inDowntime = false;
                            } else if (!inDowntime) {
                                // previous status log message event not in downtime
                                previousStatusLogMessageEventIndex = i;
                            } else {
                                // previous status log message event in downtime
                                previousStartDowntimeLogMessageEventIndex = lastStartDowntimeLogMessageEventIndex;
                                previousInDowntimeStatusLogMessageEventIndex = i;
                            }
                        }
                        // trim to previous status log message events not in downtime
                        while (previousStatusLogMessageEventIndex-- > 0) {
                            logMessageEventList.remove(0);
                            previousStartDowntimeLogMessageEventIndex--;
                            previousInDowntimeStatusLogMessageEventIndex--;
                        }
                        // trim to previous start downtime log message event
                        while (previousStartDowntimeLogMessageEventIndex-- > 0) {
                            logMessageEventList.remove(0);
                            previousInDowntimeStatusLogMessageEventIndex--;
                        }
                        // trim to previous status log message events in downtime
                        while (previousInDowntimeStatusLogMessageEventIndex-- > 1) {
                            logMessageEventList.remove(1);
                        }
                    }
                }
            }
        }

        private void addNewService(final int serviceId) throws Exception {
            if (!serviceKeys.containsKey(serviceId)) {
                executeQuery(SERVICE_NAME, new Object[]{serviceId}, new QueryResults() {
                    @Override
                    public void result(ResultSet results) throws Exception {
                        String hostName = results.getString(1);
                        String serviceName = results.getString(2);
                        int hostId = results.getInt(3);
                        String serviceKey = hostName + ":" + serviceName;
                        serviceKeyIds.put(serviceKey, new Integer[]{hostId, serviceId});
                        serviceKeys.put(serviceId, serviceKey);
                    }
                });
            }
        }

        private void addNewHost(final int hostId) throws Exception {
            if (!hostNames.containsKey(hostId)) {
                executeQuery(HOST_NAME, new Object[]{hostId}, new QueryResults() {
                    @Override
                    public void result(ResultSet results) throws Exception {
                        String hostName = results.getString(1);
                        hostIds.put(hostName, hostId);
                        hostNames.put(hostId, hostName);
                    }
                });
            }
        }

        private List<Integer> parseEntityIdsForAction(TextMessage message, ServiceNotifyEntityType entityType, ServiceNotifyAction action) throws JMSException {
            List<Integer> entityIds = new ArrayList<>();
            Pattern entityPattern = FOUNDATION_ENTITIES_PATTERNS_CACHE.get(entityType.toString());
            if (entityPattern == null) {
                StringBuilder patternBuilder = new StringBuilder('<').append(FOUNDATION_ENTITIES_XML_ENTITY_TAG_NAME).append(' ');
                patternBuilder.append(FOUNDATION_ENTITIES_XML_TYPE_ATTR_NAME).append("=\"").append(entityType.toString()).append("\" ");
                patternBuilder.append(FOUNDATION_ENTITIES_XML_TEXT_ATTR_NAME).append("=\"([^\"]*)\"").append(" />");
                entityPattern = Pattern.compile(patternBuilder.toString());
                FOUNDATION_ENTITIES_PATTERNS_CACHE.put(entityType.toString(), entityPattern);
            }
            Matcher entityMatcher = entityPattern.matcher(message.getText());
            if (entityMatcher.find()) {
                Pattern actionPattern = FOUNDATION_ENTITIES_PATTERNS_CACHE.get(action.toString());
                if (actionPattern == null) {
                    StringBuilder patternBuilder = new StringBuilder(action.toString()).append(":([0-9]*);");
                    actionPattern = Pattern.compile(patternBuilder.toString());
                    FOUNDATION_ENTITIES_PATTERNS_CACHE.put(action.toString(), actionPattern);
                }
                Matcher actionMatcher = actionPattern.matcher(entityMatcher.group(1));
                while (actionMatcher.find()) {
                    entityIds.add(Integer.parseInt(actionMatcher.group(1)));
                }
            }
            return entityIds;
        }

        private void removeDeletedService(int serviceId) {
            String serviceKey = serviceKeys.get(serviceId);
            if (serviceKey != null) {
                serviceKeyIds.remove(serviceKey);
                serviceKeys.remove(serviceId);
                serviceLogMessageEvents.remove(serviceId);
            }
        }

        private void removeDeletedHost(int hostId) {
            String hostName = hostNames.get(hostId);
            if (hostName != null) {
                hostIds.remove(hostName);
                hostNames.remove(hostId);
                hostLogMessageEvents.remove(hostId);
            }
        }

        private void renameHost(final int hostId) throws Exception {
            // update window host name and service names if renamed
            executeQuery(HOST_NAME, new Object[]{hostId}, new QueryResults() {
                @Override
                public void result(ResultSet results) throws Exception {
                    String hostName = results.getString(1);
                    String windowHostName = hostNames.get(hostId);
                    if (!hostName.equals(windowHostName)) {
                        hostIds.remove(windowHostName);
                        hostIds.put(hostName, hostId);
                        hostNames.put(hostId, hostName);
                        String windowServiceKeyPrefix = windowHostName + ":";
                        for (String windowServiceKey : new ArrayList<>(serviceKeys.values())) {
                            if (windowServiceKey.startsWith(windowServiceKeyPrefix)) {
                                String serviceKey = hostName + ":" + windowServiceKey.substring(windowServiceKeyPrefix.length());
                                Integer [] serviceIds = serviceKeyIds.remove(windowServiceKey);
                                serviceKeyIds.put(serviceKey, serviceIds);
                                serviceKeys.put(serviceIds[1], serviceKey);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Terminate background window processing.
     *
     * @throws BusinessServiceException
     */
    public void uninitialize() throws BusinessServiceException {
        // stop listening to entity events
        if (entityPublisher != null) {
            entityPublisher.removeEntityPublisherListener(this);
        }
        // stop window update thread
        Thread stopThread = windowUpdateThread;
        if (stopThread != null) {
            synchronized(stopThread) {
                windowUpdateThread = null;
                stopThread.notifyAll();
            }
            try {
                stopThread.join(WINDOW_UPDATE_THREAD_SHUTDOWN_WAIT);
            } catch (InterruptedException ie) {
            }
        }
        // clear event queues
        logMessageEventMessageQueue.clear();
        foundationEntityMessageQueue.clear();
    }

    /**
     * Return whether date range is within window.
     *
     * @return in window
     */
    public boolean isInWindow(String startDate, String endDate) throws BusinessServiceException {
        if (!windowEnabled) {
            return false;
        }
        try {
            long startMillis = parseDateTimeToMillis(startDate);
            if (startMillis < windowStartMillis - windowUpdateIntervalMillis) {
                return false;
            }
            long endMillis = parseDateTimeToMillis(endDate);
            return endMillis > windowStartMillis;
        } catch (BusinessServiceException bse) {
            return false;
        }
    }

    /**
     * Returns a List of StateTransition instances for the specified host for the date range provided or null
     * if the date range is outside the managed window.
     *
     * @param hostName host name
     * @param startDate start of date range
     * @param endDate end of date range
     * @return state transitions in date range
     * @throws BusinessServiceException
     */
    @Override
    public List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate)
            throws BusinessServiceException {
        // get if only enabled and initialized
        if (!windowEnabled || !windowInitialized) {
            throw new BusinessServiceException("LogMessageWindowService not enabled or initialized");
        }
        // lookup host log message events
        List<LogMessageEvent> logMessageEvents = null;
        Integer hostId = hostIds.get(hostName);
        if (hostId != null) {
            logMessageEvents = hostLogMessageEvents.get(hostId);
        }
        // make and return state transitions
        return makeStateTransitions(hostName, null, logMessageEvents, startDate, endDate);
    }

    /**
     * Returns a List of StateTransition instances for the specified service for the date range provided or null
     * if the date range is outside the managed window.
     *
     * @param hostName service host name
     * @param serviceName service description
     * @param startDate state of date range
     * @param endDate end of date range
     * @return state transitions in date range
     * @throws BusinessServiceException
     */
    @Override
    public List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate,
                                                            String endDate)
            throws BusinessServiceException {
        // get if only enabled and initialized
        if (!windowEnabled || !windowInitialized) {
            throw new BusinessServiceException("LogMessageWindowService not enabled or initialized");
        }
        // lookup service log message events
        List<LogMessageEvent> logMessageEvents = null;
        String serviceKey = hostName + ":" + serviceName;
        Integer[] hostAndServiceId = serviceKeyIds.get(serviceKey);
        if (hostAndServiceId != null) {
            logMessageEvents = serviceLogMessageEvents.get(hostAndServiceId[1]);
        }
        // make and return state transitions
        return makeStateTransitions(hostName, serviceName, logMessageEvents, startDate, endDate);
    }

    /**
     * Return window enabled configuration.
     *
     * @return window enabled
     */
    @Override
    public boolean isWindowEnabled() {
        return windowEnabled;
    }

    /**
     * Set window enabled configuration.
     */
    public void setWindowEnabled(boolean windowEnabled) {
        this.windowEnabled = windowEnabled;
    }

    /**
     * Return window initialized status.
     *
     * @return window initialized
     */
    @Override
    public boolean isWindowInitialized() {
        return windowEnabled && windowUpdateThread != null && windowInitialized;
    }

    private interface QueryResults {
        void result(ResultSet results) throws Exception;
    }

    private static class LogMessageEvent implements Comparable<LogMessageEvent> {

        private long firstInsertDateMillis;
        private int monitorStatusId;

        private LogMessageEvent(long firstInsertDateMillis, int monitorStatusId) {
            this.firstInsertDateMillis = firstInsertDateMillis;
            this.monitorStatusId = monitorStatusId;
        }

        @Override
        public int compareTo(LogMessageEvent other) {
            long delta = firstInsertDateMillis-other.firstInsertDateMillis;
            return delta < 0L ? -1 : delta == 0L ? 0 : 1;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LogMessageEvent)) {
                return false;
            }
            return (firstInsertDateMillis == ((LogMessageEvent) other).firstInsertDateMillis &&
                    monitorStatusId == ((LogMessageEvent) other).monitorStatusId);
        }
    }

    private void executeQuery(String query, Object[] parameters, QueryResults queryResults) throws Exception {
        StatelessSession session = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            // open new stateless hibernate session for JDBC query
            session = getSessionFactory().openStatelessSession();
            // disable autocommit to enable cursor-based result set
            session.connection().setAutoCommit(false);
            // create query statement
            statement = session.connection().prepareStatement(query);
            // set fetch size for results cursor
            statement.setFetchSize(LOGMESSAGE_FETCH_SIZE);
            // execute query
            if (parameters != null) {
                int parameterIndex = 1;
                for (Object parameter : parameters) {
                    if (parameter instanceof Integer) {
                        statement.setInt(parameterIndex++, (Integer) parameter);
                    } else if (parameter instanceof Long) {
                        statement.setTimestamp(parameterIndex++, new Timestamp((Long) parameter));
                    } else {
                        throw new RuntimeException("Unsupported query parameter type: "+
                                (parameter != null ? parameter.getClass().getSimpleName() : "null"));
                    }
                }
            }
            results = statement.executeQuery();
            // pump results to query results implementation, abort if exit set
            while ((windowUpdateThread != null) && results.next()) {
                queryResults.result(results);
            }
        } finally {
            // cleanup query resources
            try {
                if (results != null) {
                    results.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private List<LogMessageEvent> getServiceLogMessageEvents(int serviceId, boolean create) {
        List<LogMessageEvent> logMessageEvents = serviceLogMessageEvents.get(serviceId);
        if (logMessageEvents == null && create) {
            logMessageEvents = Collections.synchronizedList(new ArrayList<LogMessageEvent>());
            serviceLogMessageEvents.put(serviceId, logMessageEvents);
        }
        return logMessageEvents;
    }

    private List<LogMessageEvent> getHostLogMessageEvents(int hostId, boolean create) {
        List<LogMessageEvent> logMessageEvents = hostLogMessageEvents.get(hostId);
        if (logMessageEvents == null && create) {
            logMessageEvents = Collections.synchronizedList(new ArrayList<LogMessageEvent>());
            hostLogMessageEvents.put(hostId, logMessageEvents);
        }
        return logMessageEvents;
    }

    private void insertLogMessageEvent(List<LogMessageEvent> logMessageEvents, LogMessageEvent logMessageEvent) {
        synchronized (logMessageEvents) {
            int index = logMessageEvents.size()-1;
            while (index >= 0 && logMessageEvents.get(index).compareTo(logMessageEvent) > 0) {
                index--;
            }
            if (index == -1 || !logMessageEvents.get(index).equals(logMessageEvent)) {
                logMessageEvents.add(index+1, logMessageEvent);
            }
        }
    }

    private List<StateTransition> makeStateTransitions(String hostName, String serviceName,
                                                       List<LogMessageEvent> logMessageEventList, String startDate,
                                                       String endDate) {
        // validate log message event list
        if (logMessageEventList == null || logMessageEventList.isEmpty()) {
            return null;
        }
        // limit start date and end date to window
        long startMillis = Math.max(parseDateTimeToMillis(startDate), windowStartMillis);
        long endMillis = Math.min(parseDateTimeToMillis(endDate), System.currentTimeMillis());
        // determine initial monitor status
        int initialMonitorStatusId = logMessageEventList.get(0).monitorStatusId;
        boolean inDowntime = (initialMonitorStatusId == inDowntimeMonitorStatusId ||
                initialMonitorStatusId == endDowntimeMonitorStatusId);
        // iterate over log message event list generating state transitions
        List<StateTransition> stateTransitions = new ArrayList<>();
        LogMessageEvent lastLogMessageEvent = null;
        Boolean lastLogMessageEventInDowntime = null;
        for (LogMessageEvent logMessageEvent : logMessageEventList) {
            // done when after window end
            if (logMessageEvent.firstInsertDateMillis >= endMillis) {
                break;
            }
            boolean inWindow = logMessageEvent.firstInsertDateMillis >= startMillis;
            int monitorStatusId = logMessageEvent.monitorStatusId;
            if (monitorStatusId == startDowntimeMonitorStatusId || monitorStatusId == inDowntimeMonitorStatusId) {
                // log message event (re)sets in downtime
                inDowntime = true;
                // potentially generate unscheduled/scheduled critical/down state transition
                LogMessageEvent newLogMessageEvent = generateLogMessageEvent(hostName, serviceName, lastLogMessageEvent,
                        lastLogMessageEventInDowntime, logMessageEvent, true, inWindow, stateTransitions);
                if (newLogMessageEvent != null) {
                    lastLogMessageEvent = newLogMessageEvent;
                    lastLogMessageEventInDowntime = true;
                }
            } else if (monitorStatusId == endDowntimeMonitorStatusId) {
                // log message event clears in downtime
                inDowntime = false;
                // potentially generate scheduled/unscheduled critical/down state transition
                LogMessageEvent newLogMessageEvent = generateLogMessageEvent(hostName, serviceName, lastLogMessageEvent,
                        lastLogMessageEventInDowntime, logMessageEvent, false, inWindow, stateTransitions);
                if (newLogMessageEvent != null) {
                    lastLogMessageEvent = newLogMessageEvent;
                    lastLogMessageEventInDowntime = false;
                }
            } else {
                // add state transition
                if (addLogMessageEvent(hostName, serviceName, lastLogMessageEvent, lastLogMessageEventInDowntime,
                        logMessageEvent, inDowntime, inWindow, stateTransitions)) {
                    lastLogMessageEvent = logMessageEvent;
                    lastLogMessageEventInDowntime = inDowntime;
                }
            }
        }
        // add placeholder state transition if no state transitions returned
        if (lastLogMessageEvent != null && stateTransitions.isEmpty()) {
            addLogMessageEvent(hostName, serviceName, lastLogMessageEvent, inDowntime, stateTransitions);
        }
        // return state transitions
        return !stateTransitions.isEmpty() ? stateTransitions : null;
    }

    private LogMessageEvent generateLogMessageEvent(String hostName, String serviceName,
                                                    LogMessageEvent lastLogMessageEvent,
                                                    Boolean lastLogMessageEventInDowntime,
                                                    LogMessageEvent logMessageEvent, boolean inDowntime,
                                                    boolean inWindow, List<StateTransition> stateTransitions) {
        if (lastLogMessageEvent == null) {
            return null;
        }
        if (isCriticalLogMessageEvent(lastLogMessageEvent) || isDownLogMessageEvent(lastLogMessageEvent)) {
            int newMonitorStatusId = effectiveLogMessageEventMonitorStatusId(lastLogMessageEvent, inDowntime);
            int lastMonitorStatusId = effectiveLogMessageEventMonitorStatusId(lastLogMessageEvent,
                    lastLogMessageEventInDowntime);
            if (newMonitorStatusId != lastMonitorStatusId) {
                LogMessageEvent newLogMessageEvent = new LogMessageEvent(logMessageEvent.firstInsertDateMillis,
                        lastLogMessageEvent.monitorStatusId);
                if (inWindow) {
                    StateTransition newStateTransition = makeStateTransition(hostName, serviceName, lastMonitorStatusId,
                            lastLogMessageEvent, newMonitorStatusId, newLogMessageEvent);
                    stateTransitions.add(newStateTransition);
                }
                return newLogMessageEvent;
            }
        }
        return null;
    }

    private boolean addLogMessageEvent(String hostName, String serviceName, LogMessageEvent lastLogMessageEvent,
                                       Boolean lastLogMessageEventInDowntime, LogMessageEvent logMessageEvent,
                                       boolean inDowntime, boolean inWindow, List<StateTransition> stateTransitions) {
        if (lastLogMessageEvent == null) {
            return true;
        }
        int newMonitorStatusId = effectiveLogMessageEventMonitorStatusId(logMessageEvent, inDowntime);
        int lastMonitorStatusId = effectiveLogMessageEventMonitorStatusId(lastLogMessageEvent,
                lastLogMessageEventInDowntime);
        if (newMonitorStatusId != lastMonitorStatusId) {
            if (inWindow) {
                StateTransition newStateTransition = makeStateTransition(hostName, serviceName, lastMonitorStatusId,
                        lastLogMessageEvent, newMonitorStatusId, logMessageEvent);
                stateTransitions.add(newStateTransition);
            }
            return true;
        }
        return false;
    }

    private boolean addLogMessageEvent(String hostName, String serviceName, LogMessageEvent logMessageEvent,
                                       boolean inDowntime, List<StateTransition> stateTransitions) {
        int newMonitorStatusId = effectiveLogMessageEventMonitorStatusId(logMessageEvent, inDowntime);
        StateTransition newStateTransition = makeStateTransition(hostName, serviceName, 0, null, newMonitorStatusId,
                logMessageEvent);
        stateTransitions.add(newStateTransition);
        return true;
    }

    private StateTransition makeStateTransition(String hostName, String serviceName, int lastMonitorStatusId,
                                                LogMessageEvent lastLogMessageEvent, int newMonitorStatusId,
                                                LogMessageEvent logMessageEvent) {
        StateTransition stateTransition;
        if (lastLogMessageEvent != null) {
            stateTransition = StateTransition.createStateTransition(hostName, serviceName,
                    monitorStatuses.get(lastMonitorStatusId), new Date(lastLogMessageEvent.firstInsertDateMillis),
                    monitorStatuses.get(newMonitorStatusId), new Date(logMessageEvent.firstInsertDateMillis));
            stateTransition.setDurationInState(logMessageEvent.firstInsertDateMillis -
                    lastLogMessageEvent.firstInsertDateMillis);
        } else {
            stateTransition = StateTransition.createStateTransition(hostName, serviceName, null, null,
                    monitorStatuses.get(newMonitorStatusId), new Date(logMessageEvent.firstInsertDateMillis));
        }
        return stateTransition;
    }

    private long parseDateTimeToMillis(String date) {
        try {
            synchronized (DATE_FORMAT) {
                return DATE_FORMAT.parse(date).getTime();
            }
        } catch (ParseException pe) {
        }
        try {
            synchronized (DATE_FORMAT_US) {
                return DATE_FORMAT_US.parse(date).getTime();
            }
        } catch (ParseException pe) {
        }
        throw new BusinessServiceException("Invalid date format: "+date);
    }

    private boolean isCriticalLogMessageEvent(LogMessageEvent logMessageEvent) {
        return logMessageEvent.monitorStatusId == criticalMonitorStatusId ||
                logMessageEvent.monitorStatusId == scheduledCriticalMonitorStatusId ||
                logMessageEvent.monitorStatusId == unscheduledCriticalMonitorStatusId;
    }

    private boolean isDownLogMessageEvent(LogMessageEvent logMessageEvent) {
        return logMessageEvent.monitorStatusId == downMonitorStatusId ||
                logMessageEvent.monitorStatusId == scheduledDownMonitorStatusId ||
                logMessageEvent.monitorStatusId == unscheduledDownMonitorStatusId;
    }

    private int effectiveLogMessageEventMonitorStatusId(LogMessageEvent logMessageEvent, boolean inDowntime) {
        if (logMessageEvent.monitorStatusId == criticalMonitorStatusId) {
            return inDowntime ? scheduledCriticalMonitorStatusId : unscheduledCriticalMonitorStatusId;
        }
        if (logMessageEvent.monitorStatusId == downMonitorStatusId) {
            return inDowntime ? scheduledDownMonitorStatusId : unscheduledDownMonitorStatusId;
        }
        return logMessageEvent.monitorStatusId;
    }
}
