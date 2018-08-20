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
package org.groundwork.foundation.bs.statistics;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.AttributeData;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.impl.HostStateTransition;
import com.groundwork.collage.model.impl.HostStatistic;
import com.groundwork.collage.model.impl.LogMessageStatistic;
import com.groundwork.collage.model.impl.NagiosStatisticProperty;
import com.groundwork.collage.model.impl.ServiceStateTransition;
import com.groundwork.collage.model.impl.ServiceStatistic;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StateTransition;
import com.groundwork.collage.model.impl.StatisticProperty;
import com.groundwork.collage.util.StatisticGroupState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.BusinessServiceImpl;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.events.EntityPublisher;
import org.groundwork.foundation.bs.events.PerformanceDataPublisher;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.performancedata.NagiosPerformanceDataService;
import org.groundwork.foundation.bs.performancedata.NagiosPerformanceDataServiceImpl;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author rogerrut
 *         <p/>
 *         Created: Feb 22, 2007
 */
public class StatisticsServiceImpl extends BusinessServiceImpl implements
        StatisticsService {
    private static final String COLON = ":";
    private static final String END_OF_DAY_TIME = " 23:59:59";

    private static final String DATE_FORMAT_US = "MM/dd/yyyy";

    // Query String Constants
    private static final String SELECT_HOST_SERVICE_STATUS_COUNT = "select count(*) from Host h, ServiceStatus ss  where h.hostId = ss.host.hostId AND h.hostName = ? AND ss.monitorStatus.monitorStatusId = ?";
    private static final String SELECT_SERVICE_STATUS_COUNT = "select count(*) from ServiceStatus ss where ss.monitorStatus.monitorStatusId = ?";
    private static final String SELECT_HOST_STATUS_COUNT = "select count(*) from HostStatus hs where hs.hostMonitorStatus.monitorStatusId = ?";
    private static final String SELECT_FILTERED_HOST_STATUS_COUNT = "select (select M.Name from MonitorStatus M where M.MonitorStatusID=HS.MonitorStatusID) as Status,count(HS.MonitorStatusID) as count from HostStatus HS where HS.HostStatusID in (select H.HostID from Host H  where H.HostName in (#)) group by HS.MonitorStatusID";
    private static final String SELECT_FILTERED_SERVICE_STATUS_COUNT = "select (select M.Name from MonitorStatus M where M.MonitorStatusID=SS.MonitorStatusID) as Status,count(SS.MonitorStatusID) as count from ServiceStatus SS where SS.ServiceStatusID in (#) group by SS.MonitorStatusID";
    private static final String SELECT_SERVICE_STATUS_COUNT_BY_HOSTGROUP = "select count(*) from ServiceStatus ss, HostGroup hg left join hg.hosts as h WHERE h.hostId = ss.host.hostId AND hg.name=? AND ss.monitorStatus.monitorStatusId=?";
    private static final String SELECT_HOST_STATUS_COUNT_BY_HOSTGROUP = "select count(*) from HostStatus hs, HostGroup hg left join hg.hosts as h WHERE h.hostId = hs.hostStatusId AND hg.name=? AND hs.hostMonitorStatus.monitorStatusId = ?";
    private static final String SELECT_HOST_COUNT = "select count(*) from Host";
    private static final String SELECT_SERVICE_COUNT = "select count(*) from ServiceStatus";
    private static final String SELECT_SERVICEGROUP_SERVICE_COUNT =
            "select count(*) from Category c, CategoryEntity ce, EntityType etc, EntityType etce where " +
                    "ce.EntityTypeId=etce.EntityTypeId and " +
                    "etce.Name='SERVICE_STATUS' and " +
                    "c.CategoryID=ce.CategoryID and " +
                    "c.EntityTypeId=etc.EntityTypeId and " +
                    "etc.Name='SERVICE_GROUP' and " +
                    "c.Name=? ";
    private static final String SELECT_SERVICEGROUP_STATUS_COUNT =
            "select count(*) from Category c, CategoryEntity ce, EntityType etc, EntityType etce, ServiceStatus ss, MonitorStatus ms where " +
                    "c.CategoryID=ce.CategoryID and " +
                    "ce.EntityTypeId=etce.EntityTypeId and " +
                    "etce.Name='SERVICE_STATUS' and " +
                    "ce.ObjectID=ss.ServiceStatusID and " +
                    "ss.MonitorStatusID=ms.MonitorStatusID and " +
                    "c.EntityTypeId=etc.EntityTypeId and " +
                    "etc.Name='SERVICE_GROUP' and " +
                    "c.Name=? and " +
                    "ms.Name=? ";

    /**
     * NOTE: We are casting the sum results to a SIGNED integer b/c the
     * hibernate MYSQL Dialect does not have a type mapping for the java sql
     * type DECIMAL. This sql will not work on Oracle and is MySQL specific. The
     * following link is for Oracle9 but MySQL has the same issue.
     * http://forum.hibernate
     * .org/viewtopic.php?t=959583&sid=735281df4d3a13629c7968b41620a8b8
     * <p/>
     * The JIRA is HHH-1806 as seems to be fixed in version 3.2 of hibernate
     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-1806
     */
    private static final String SELECT_HOST_STATUS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from (select hg.Name As HostGroupName, hsp.HostStatusID, pt.Name as PropertyName, "
            + "case when (pt.isInteger and hsp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and hsp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Disabled "
            + "from HostStatusProperty hsp "
            + "inner join PropertyType pt on hsp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join HostGroupCollection hgc on hsp.HostStatusID = hgc.HostID "
            + "inner join HostStatus hs on hsp.HostStatusID = hs.HostStatusID "
            + "inner join MonitorStatus ms on hs.MonitorStatusID = ms.MonitorStatusID "
            + "inner join HostGroup hg on hgc.HostGroupID = hg.HostGroupID "
            + "where hg.ApplicationTypeID = ? and hg.Name = ? and (pt.isInteger or pt.isBoolean) ) A "
            + "group by HostGroupName, PropertyName";

    private static final String SELECT_SERVICE_STATUS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from ( select hg.Name As HostGroupName, ssp.ServiceStatusID, pt.Name as PropertyName, "
            + "case when (pt.isInteger and ssp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and ssp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Disabled "
            + "from ServiceStatusProperty ssp "
            + "inner join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join ServiceStatus ss on ssp.ServiceStatusID = ss.ServiceStatusID "
            + "inner join HostGroupCollection hgc on ss.HostID = hgc.HostID "
            + "inner join MonitorStatus ms on ss.MonitorStatusID = ms.MonitorStatusID "
            + "inner join HostGroup hg on hgc.HostGroupID = hg.HostGroupID "
            + "where hg.ApplicationTypeID = ? and hg.Name = ? and (pt.isInteger or pt.isBoolean) ) A "
            + "group by HostGroupName, PropertyName";

    private static final String SELECT_HOST_SERVICE_STATUS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from ( select h.HostName, ssp.ServiceStatusID, pt.Name as PropertyName, "
            + "case when (pt.isInteger and ssp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and ssp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Disabled "
            + "from ServiceStatusProperty ssp "
            + "inner join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join ServiceStatus ss on ssp.ServiceStatusID = ss.ServiceStatusID "
            + "inner join Host h on ss.HostID = h.HostID "
            + "inner join MonitorStatus ms on ss.MonitorStatusID = ms.MonitorStatusID "
            + "where h.HostName = ? and (pt.isInteger or pt.isBoolean) ) A "
            + "group by HostName, PropertyName";

    private static final String SELECT_HOST_LEVEL_STATUS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from ( select h.HostName, hsp.HostStatusID, pt.Name as PropertyName, "
            + "case when (pt.isInteger and hsp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and hsp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Disabled "
            + "from HostStatusProperty hsp "
            + "inner join PropertyType pt on hsp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join Host h on hsp.HostStatusID = h.HostID "
            + "inner join HostStatus hs on hsp.HostStatusID = hs.HostStatusID "
            + "inner join MonitorStatus ms on hs.MonitorStatusID = ms.MonitorStatusID "
            + "where h.HostName = ? and (pt.isInteger or pt.isBoolean) ) A "
            + "group by HostName, PropertyName";

    /*
     * Queries for total counts for nagios properties
	 */
    private static final String SELECT_SERVICE_TOTALS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from ( select pt.Name as PropertyName, "
            + "case when (pt.isInteger and ssp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and ssp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1 "
            + "when (pt.isBoolean and not ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged' and ms.Name <> 'OK' ) then 1 "
            + "else 0 end as Disabled "
            + "from ServiceStatusProperty ssp "
            + "inner join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join ServiceStatus ss on ssp.ServiceStatusID = ss.ServiceStatusID "
            + "inner join Host h on ss.HostID = h.HostID "
            + "inner join MonitorStatus ms on ss.MonitorStatusID = ms.MonitorStatusID "
            + "where (pt.isInteger or pt.isBoolean) ) A "
            + "group by PropertyName";

    private static final String SELECT_HOST_TOTALS_STATISTICS = "select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled "
            + "from ( select pt.Name as PropertyName, "
            + "case when (pt.isInteger and hsp.ValueInteger > 0) then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Enabled, "
            + "case when (pt.isInteger and hsp.ValueInteger < 1) then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name <> 'isAcknowledged') then 1 "
            + "when (pt.isBoolean and not hsp.ValueBoolean and pt.Name = 'isAcknowledged' and ms.Name <> 'UP' ) then 1 "
            + "else 0 end as Disabled "
            + "from HostStatusProperty hsp "
            + "inner join PropertyType pt on hsp.PropertyTypeID = pt.PropertyTypeID "
            + "inner join Host h on hsp.HostStatusID = h.HostID "
            + "inner join HostStatus hs on hsp.HostStatusID = hs.HostStatusID "
            + "inner join MonitorStatus ms on hs.MonitorStatusID = ms.MonitorStatusID "
            + "where (pt.isInteger or pt.isBoolean) ) A "
            + "group by PropertyName";

    private static final String SELECT_SERVICE_GROUP_STATS = " select PropertyName, sum(Enabled) as NumEnabled, sum(Disabled) as NumDisabled from "
            + "(select pt.Name as PropertyName,   "
            + "case when (pt.isInteger and ssp.ValueInteger > 0) then 1   "
            + "when (pt.isBoolean and ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged') then 1  "
            + "when (pt.isBoolean and  ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged'  "
            + "  and ms.Name <> 'OK' ) then 1  else 0 end as Enabled,   "
            + "  case when (pt.isInteger and ssp.ValueInteger < 1) then 1   "
            + "  when (pt.isBoolean and not ssp.ValueBoolean and pt.Name <> 'isProblemAcknowledged')  "
            + "  then 1  when (pt.isBoolean and not ssp.ValueBoolean and pt.Name = 'isProblemAcknowledged'  "
            + "    and ms.Name <> 'OK' ) then 1  else 0 end as Disabled from "
            + "    ServiceStatusProperty ssp "
            + "      inner join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID  "
            + "      inner join CategoryEntity ce on ssp.ServiceStatusID = ce.ObjectID  "
            + "      inner join EntityType etce on ce.EntityTypeID = etce.EntityTypeID  "
            + "      inner join ServiceStatus ss on ssp.ServiceStatusID = ss.ServiceStatusID  "
            + "      inner join MonitorStatus ms on ss.MonitorStatusID = ms.MonitorStatusID, "
            + "    Category sg "
            + "      inner join EntityType etsg on sg.EntityTypeID = etsg.EntityTypeID  "
            + "    where "
            + "      ss.ApplicationTypeID = ? and "
            + "      etsg.Name='SERVICE_GROUP' and "
            + "      sg.Name = ? and "
            + "      sg.CategoryID = ce.CategoryID and "
            + "      etce.Name='SERVICE_STATUS' and "
            + "      (pt.isInteger or pt.isBoolean) ) A  "
            + "    group by PropertyName";

    // String constants
    private static final String SEMICOLON = ";";
    private static final String ALL_HOSTGROUP = "_ALL_";
    private final static String APP_TYPE_NAGIOS = "NAGIOS";
    private final static String EMPTY_STRING = "";
    private final static String SINGLE_QUOTE = "'";
    private final static String ALL = "ALL";

    private double hostAvailability;
    private double serviceAvailability;

    /* Enable Log4j */
    Log log = LogFactory.getLog(this.getClass());

    private FoundationDAO foundationDAO = null;
    private MetadataService metadataService = null;
    private HostGroupService hostGroupService = null;
    private HostService hostService = null;
    private LogMessageService logMessageService = null;
    private CategoryService categoryService = null;

    private int maxQueryAgeHours = 0;
	/*
     * Data structures that hold all the statistic data
	 */

    private static ConcurrentHashMap<String, Vector<StateStatistics>> HOSTGROUP_HOST_STATISTIC = new ConcurrentHashMap<String, Vector<StateStatistics>>();
    private static ConcurrentHashMap<String, Vector<StateStatistics>> HOSTGROUP_SERVICE_STATISTIC = new ConcurrentHashMap<String, Vector<StateStatistics>>();
    private static ConcurrentHashMap<String, Vector<StateStatistics>> SERVICEGROUP_SERVICE_STATISTIC = new ConcurrentHashMap<String, Vector<StateStatistics>>();
    private static ConcurrentHashMap<String, Collection<NagiosStatisticProperty>> NAGIOS_FEATURES_STATISTIC = new ConcurrentHashMap<String, Collection<NagiosStatisticProperty>>();

    /* Host Statistics */
    private static ConcurrentHashMap<String, Collection<NagiosStatisticProperty>> NAGIOS_HOST_FEATURES_STATISTIC = new ConcurrentHashMap<String, Collection<NagiosStatisticProperty>>();

    /* Host Statistics */
    private static ConcurrentHashMap<String, Collection<NagiosStatisticProperty>> NAGIOS_SERVICEGROUP_STATISTIC = new ConcurrentHashMap<String, Collection<NagiosStatisticProperty>>();

    private static StateStatistics ALL_HOST_STATISTICS = null;
    private static StateStatistics ALL_SERVICE_STATISTICS = null;

    /* Application Statistics */
    private static ConcurrentHashMap<Integer, Collection<NagiosStatisticProperty>> APPLICATION_STATISTIC_TOTALS = new ConcurrentHashMap<Integer, Collection<NagiosStatisticProperty>>();

    /* Structures to keep track of internal state */
    private static ConcurrentHashMap<String, StatisticGroupState> HOSTGROUPS = new ConcurrentHashMap<String, StatisticGroupState>();

    /* Structures to keep track of internal state */
    private static ConcurrentHashMap<String, StatisticGroupState> SERVICEGROUPS = new ConcurrentHashMap<String, StatisticGroupState>();

    /* Keep trackof the hosts */
    private static ConcurrentHashMap<String, Boolean> HOSTS = new ConcurrentHashMap<String, Boolean>();

    /* Nagios property matching lookup table */
    private static Hashtable<String, String> NAGIOS_HOST_PROPERTY_MAP = new Hashtable<String, String>();
    private static Hashtable<String, String> NAGIOS_SERVICE_PROPERTY_MAP = new Hashtable<String, String>();

    /* Structures to keep track of hostgroup event statistics */
    private static ConcurrentHashMap<String, Collection<StatisticProperty>> HOSTGROUP_OPEN_EVENT_STATS = new ConcurrentHashMap<String, Collection<StatisticProperty>>();

    /*
     * Keep track of the time it takes to calculate the ststistics. Value will
	 * be used to define idle time between regenerating statistics
	 */
    private static long MINIMAL_RECALC_INTERVAL = 30000;

    /**
     * Maximum amount of time for which the thread can sleep
     */
    private static final int MAX_RECALC_INTERVAL = 2 * 60 * 1000;
    private static AtomicLong RECALCULATION_TIME = new AtomicLong(
            MINIMAL_RECALC_INTERVAL);

    /* store list of host/service statuses locally */
    private static List<String> HOST_STATUS_LIST = new ArrayList<String>(5);
    private static List<String> SERVICE_STATUS_LIST = new ArrayList<String>(5);
    private static List<String> NAGIOS_PROPERTY_LIST = new ArrayList<String>(5);

    private static String DEFAULT_HOST_STATUS_LIST = "DOWN;UNREACHABLE;PENDING;UP;SCHEDULED DOWN;UNSCHEDULED DOWN";
    private static String DEFAULT_SERVICE_STATUS_LIST = "CRITICAL;WARNING;UNKNOWN;OK;PENDING;SCHEDULED CRITICAL;UNSCHEDULED CRITICAL";
    private static String DEFAULT_NAGIOS_STATUS_LIST = "isNotificationsEnabled;isEventHandlersEnabled;ScheduledDowntimeDepth;isChecksEnabled;Acknowledged;PassiveChecks;isFlapDetectionEnabled";
    private static StatisticCalculationThread CALCULATE_STATISTICS_THREAD = null;

    // Error message table
    private final String ERROR_HOST_STATUS_LIST = "StateStatistics -- List of Statuses to check can't be empty.";
    private final String ERROR_SERVICE_STATUS_LIST = "StateStatistics -- List of Statuses to check can't be empty.";
    private final String ERROR_HOSTGROUP_NOT_EXIST = "StateStatistics -- HostGroup doesn't exist: Name: ";
    private final String ERROR_HOSTGROUPID_NOT_EXIST = "StateStatistics -- HostGroup ID doesn't exist: HostGroupID: ";
    private final String WARN_NO_HOSTGROUP_DEFINED = "No Host Groups defined in Foundation.";
    private final String WARN_NO_SERVICEGROUP_DEFINED = "No Service Groups defined in Foundation.";
    private final String ERROR_HOSTID_NOT_EXIST = "StateStatistics -- Host ID doesn't exist: HostID: ";
    private final String ERROR_HOSTNAME_NOT_EXIST = "StateStatistics -- Host Name doesn't exist: Host Name: ";
    private final String WARNING_NO_STATISTICS_FOR_HOST = "StateStatistics -- No Statistics available for Host: ";
    private final String WARN_NO_HOST_DEFINED = "StateSTatistics -- No Hosts defined in Foundation ";
    private final String WARNING_NO_APPLICATION_HOST_STATISTICS_TOTALS = "No Hosts Statistcs totals for Application. Application ID: ";


    // private final String WARNING_NO_APPLICATION_SERVICE_STATISTICS_TOTALS =
    // "No Service Statistcs totals for Application. Application ID: ";


    // Static Initializer
    static {
		/*
		 * Nagios property mapping - Internal property name is the key and the
		 * Nagios property is the value
		 */

        // Host
        NAGIOS_HOST_PROPERTY_MAP.put("isPassiveChecksEnabled", "PassiveChecks");
        NAGIOS_HOST_PROPERTY_MAP.put("isAcknowledged", "Acknowledged");

        // Services
        NAGIOS_SERVICE_PROPERTY_MAP.put("isAcceptPassiveChecks",
                "PassiveChecks");
        NAGIOS_SERVICE_PROPERTY_MAP
                .put("isProblemAcknowledged", "Acknowledged");
    }

    /*************************************************************************/
	/* Constructors */

    /**
     * *********************************************************************
     */

    public StatisticsServiceImpl(FoundationDAO foundationDAO,
                                 MetadataService ms, HostGroupService hgs, HostService hs,
                                 LogMessageService lms, CategoryService cs) {
        this.foundationDAO = foundationDAO;

        this.metadataService = ms;
        this.hostGroupService = hgs;
        this.hostService = hs;
        this.logMessageService = lms;
        this.categoryService = cs;

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

    /*************************************************************************/
	/* Public Methods */

    /**
     * *********************************************************************
     */

    public Collection<StateStatistics> getAllHostStatistics()
            throws BusinessServiceException {
        Collection<StateStatistics> listStatistics = new ArrayList<StateStatistics>(
                HOSTGROUP_HOST_STATISTIC.size());

        Enumeration<Vector<StateStatistics>> enumStatistics = HOSTGROUP_HOST_STATISTIC
                .elements();
        while (enumStatistics.hasMoreElements()) {
            Vector hg = (Vector) ((Vector) enumStatistics.nextElement())
                    .clone();

            // Return StateStatistic in collection
            // Note: There is only one StateStatistic in each vector
            listStatistics.add((StateStatistics) hg.get(0));
        }

        return listStatistics;
    }

    /**
     * All Statistics for each host
     */
    public StateStatistics getAllHostStatisticsByNames(String[] hostNames)
            throws BusinessServiceException {
        if (hostNames == null) {
            throw new BusinessServiceException("Invalid hostNames");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hostNames.length; i++) {
            sb.append("'");
            sb.append(hostNames[i]);
            sb.append("'");
            sb.append(",");
        }
        String hostList = sb.substring(0, sb.length() - 1);
        String sql = SELECT_FILTERED_HOST_STATUS_COUNT.replaceFirst("#",
                hostList);
        List result = foundationDAO.sqlQuery(sql);

        List<StatisticProperty> statProps = new ArrayList<StatisticProperty>();
        for (int i = 0; i < HOST_STATUS_LIST.size(); i++) {
            StatisticProperty prop = new StatisticProperty();
            prop.setName(HOST_STATUS_LIST.get(i));
            prop.setCount(0);
            for (int j = 0; j < result.size(); j++) {
                Object[] statusObj = (Object[]) result.get(j);
                String status = (String) statusObj[0];
                if (status.equalsIgnoreCase(HOST_STATUS_LIST.get(i))) {
                    prop.setCount(((BigInteger) statusObj[1]).longValue());
                } // end if
            } // end for
            statProps.add(prop);

        }
        StateStatistics stat = new StateStatistics(hostList, hostNames.length,
                0, statProps);
        return stat;
    }

    /**
     * All Statistics for each host
     */
    public StateStatistics getServiceStatisticsByServiceIDs(int[] serviceIds)
            throws BusinessServiceException {
        if (serviceIds == null) {
            throw new BusinessServiceException("Invalid hostNames");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < serviceIds.length; i++) {
            sb.append(serviceIds[i]);
            sb.append(",");
        }
        String serviceList = sb.substring(0, sb.length() - 1);
        String sql = SELECT_FILTERED_SERVICE_STATUS_COUNT.replaceFirst("#",
                serviceList);
        List result = foundationDAO.sqlQuery(sql);

        List<StatisticProperty> statProps = new ArrayList<StatisticProperty>();
        for (int i = 0; i < SERVICE_STATUS_LIST.size(); i++) {
            StatisticProperty prop = new StatisticProperty();
            prop.setName(SERVICE_STATUS_LIST.get(i));
            prop.setCount(0);
            for (int j = 0; j < result.size(); j++) {
                Object[] statusObj = (Object[]) result.get(j);
                String status = (String) statusObj[0];
                if (status.equalsIgnoreCase(SERVICE_STATUS_LIST.get(i))) {
                    prop.setCount(((BigInteger) statusObj[1]).longValue());
                } // end if
            } // end for
            statProps.add(prop);

        }
        StateStatistics stat = new StateStatistics(serviceList, 0,
                serviceIds.length, statProps);
        return stat;
    }

    public Collection<StateStatistics> getAllServiceStatistics()
            throws BusinessServiceException {
        Collection<StateStatistics> listStatistics = new ArrayList<StateStatistics>(
                HOSTGROUP_SERVICE_STATISTIC.size());

        Enumeration<Vector<StateStatistics>> enumStatistics = HOSTGROUP_SERVICE_STATISTIC
                .elements();
        while (enumStatistics.hasMoreElements()) {
            Vector hg = (Vector) ((Vector) enumStatistics.nextElement())
                    .clone();

            // Return StateStatistic in collection
            // Note: There is only one StateStatistic in each vector
            listStatistics.add((StateStatistics) hg.get(0));
        }

        return listStatistics;
    }

    /*
	 * Business logic for calculating number of hostgroups that have any of the
	 * properties greater than 0
	 *
	 * TODO: Put this method into recalc thread so that the values are
	 * pre-calculated (non-Javadoc)
	 *
	 * @seeorg.groundwork.foundation.bs.statistics.StatisticsService#
	 * getHostGroupStateCountService()
	 */
    public Collection<StatisticProperty> getHostGroupStateCountService()
            throws BusinessServiceException {
        StateStatistics stateStatisticsCount = null;
        StatisticProperty statisticProperty = null;
        StatisticProperty currentProperty = null;

        // TODO: class variable accessible from recalc thread and API
        ConcurrentHashMap<String, StatisticProperty> HOSTGROUP_COUNT_SERVICE_STATISTIC = new ConcurrentHashMap<String, StatisticProperty>();

        Iterator<String> it = SERVICE_STATUS_LIST.iterator();
        String statusName = null;

        while (it.hasNext()) {
            statusName = it.next();
            if (statusName != null && statusName.length() > 0) {
                HOSTGROUP_COUNT_SERVICE_STATISTIC.put(statusName,
                        new StatisticProperty(statusName, 0));
            }
        }

        Enumeration<Vector<StateStatistics>> enumStatistics = HOSTGROUP_SERVICE_STATISTIC
                .elements();
        while (enumStatistics.hasMoreElements()) {
            Vector hg = (Vector) ((Vector) enumStatistics.nextElement());

            // Return StateStatistic in collection
            // Note: There is only one StateStatistic in each vector
            stateStatisticsCount = (StateStatistics) hg.get(0);

            Iterator itProperties = stateStatisticsCount
                    .getStatisticProperties().iterator();
            while (itProperties.hasNext()) {
                statisticProperty = (StatisticProperty) itProperties.next();
                if (statisticProperty != null) {
                    if (statisticProperty.getCount() > 0) {
                        // Hostgroup has property set increment value
                        currentProperty = (StatisticProperty) HOSTGROUP_COUNT_SERVICE_STATISTIC
                                .get(statisticProperty.getName());
                        if (currentProperty != null) {
                            currentProperty
                                    .setCount(currentProperty.getCount() + 1);

                            log.info("HostGroup Count: Property ["
                                    + currentProperty.getName()
                                    + "] incremenetd. New value: "
                                    + currentProperty.getCount());
                        }
                    }
                }
            }
        }

        return HOSTGROUP_COUNT_SERVICE_STATISTIC.values();
    }

    /*
	 * TODO: Put this method into recalc thread so that the values are
	 * pre-calculated (non-Javadoc)
	 *
	 * @seeorg.groundwork.foundation.bs.statistics.StatisticsService#
	 * getHostGroupStateCountHost()
	 */
    public Collection<StatisticProperty> getHostGroupStateCountHost()
            throws BusinessServiceException {
        StateStatistics stateStatisticsCount = null;
        StatisticProperty statisticProperty = null;
        StatisticProperty currentProperty = null;

        // TODO: class variable accessible from recalc thread and API
        ConcurrentHashMap<String, StatisticProperty> HOSTGROUP_COUNT_HOST_STATISTIC = new ConcurrentHashMap<String, StatisticProperty>();

        Iterator<String> it = HOST_STATUS_LIST.iterator();
        String statusName = null;
        while (it.hasNext()) {
            statusName = it.next();
            if (statusName != null && statusName.length() > 0) {
                HOSTGROUP_COUNT_HOST_STATISTIC.put(statusName,
                        new StatisticProperty(statusName, 0));
            }
        }

        Enumeration<Vector<StateStatistics>> enumStatistics = HOSTGROUP_HOST_STATISTIC
                .elements();

        log.info("HostGroup Host statistics count: "
                + HOSTGROUP_HOST_STATISTIC.size());

        while (enumStatistics.hasMoreElements()) {
            Vector hg = (Vector) ((Vector) enumStatistics.nextElement());

            // Return StateStatistic in collection
            // Note: There is only one StateStatistic in each vector
            stateStatisticsCount = (StateStatistics) hg.get(0);

            Iterator itProperties = stateStatisticsCount
                    .getStatisticProperties().iterator();
            while (itProperties.hasNext()) {
                statisticProperty = (StatisticProperty) itProperties.next();
                if (statisticProperty != null) {
                    if (statisticProperty.getCount() > 0) {
                        // Hostgroup has property set increment value
                        currentProperty = (StatisticProperty) HOSTGROUP_COUNT_HOST_STATISTIC
                                .get(statisticProperty.getName());
                        if (currentProperty != null) {
                            currentProperty
                                    .setCount(currentProperty.getCount() + 1);

                            log.info("HostGroup Count: Property ["
                                    + currentProperty.getName()
                                    + "] incremenetd. New value: "
                                    + currentProperty.getCount());
                        }
                    }
                }
            }
        }

        return HOSTGROUP_COUNT_HOST_STATISTIC.values();
    }

    public Collection<NagiosStatisticProperty> getApplicationStatistics(
            int appId, int hostGroupId) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        return getApplicationStatistics(APP_TYPE_NAGIOS, hostGroupId);
    }

    public Collection<NagiosStatisticProperty> getApplicationStatistics(
            int appId, String hostGroupName) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        return getApplicationStatistics(APP_TYPE_NAGIOS, hostGroupName);
    }

    public Collection<NagiosStatisticProperty> getApplicationStatistics(
            String appName, int hostGroupId) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        HostGroup hg = this.hostGroupService.getHostGroupById(hostGroupId);

        if (hg != null)
            return this.getApplicationStatistics(APP_TYPE_NAGIOS, hg.getName());
        else {
            throw new BusinessServiceException(this.ERROR_HOSTGROUPID_NOT_EXIST
                    + hostGroupId);
        }
    }

    public Collection<NagiosStatisticProperty> getApplicationStatistics(
            String appName, String hostGroupName)
            throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        Collection<NagiosStatisticProperty> hg = NAGIOS_FEATURES_STATISTIC
                .get(hostGroupName);

        if (hg == null) {
			/*
			 * If HostGroup exists but doesn't contain any hosts no statistics
			 * will be available just return an empty Vector
			 */
            if (HOSTGROUPS.containsKey(hostGroupName))
                return new Vector<NagiosStatisticProperty>();
            else
                throw new BusinessServiceException(
                        this.ERROR_HOSTGROUP_NOT_EXIST + hostGroupName);
        }

        return hg;
    }

    public Collection<NagiosStatisticProperty> getNagiosStatisticsForServiceGroup(
            int appId, String serviceGroupName) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        Collection<NagiosStatisticProperty> sg = NAGIOS_SERVICEGROUP_STATISTIC
                .get(serviceGroupName);

        if (sg == null) {
			/*
			 * If ServiceGroup exists but doesn't contain any hosts no
			 * statistics will be available just return an empty Vector
			 */
            if (SERVICEGROUPS.containsKey(serviceGroupName))
                return new Vector<NagiosStatisticProperty>();
            else
                throw new BusinessServiceException(
                        "Service Group doesn't exist" + serviceGroupName);
        }

        return sg;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @seeorg.groundwork.foundation.bs.statistics.StatisticsService#
	 * getApplicationStatisticsHost(int, int)
	 */
    public Collection<NagiosStatisticProperty> getApplicationStatisticsHost(
            int appId, int hostId) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        Host h = this.hostService.getHostByHostId(hostId);

        if (h != null)
            return this.getApplicationStatisticsHost(APP_TYPE_NAGIOS, h
                    .getHostName());
        else {
            throw new BusinessServiceException(this.ERROR_HOSTID_NOT_EXIST
                    + hostId);
        }

    }

    /*
	 * (non-Javadoc)
	 *
	 * @seeorg.groundwork.foundation.bs.statistics.StatisticsService#
	 * getApplicationStatisticsHost(int, java.lang.String)
	 */
    public Collection<NagiosStatisticProperty> getApplicationStatisticsHost(
            int appId, String hostName) throws BusinessServiceException {
        if (hostName != null && hostName.length() > 0) {
            return this.getApplicationStatisticsHost(APP_TYPE_NAGIOS, hostName);
        } else {
            throw new BusinessServiceException(this.ERROR_HOSTNAME_NOT_EXIST
                    + hostName);
        }
    }

    /**
     * Get Application Statistics for a Host identified by the array of Host
     * Name
     *
     * @param appId
     * @param hostNames
     * @return
     * @throws BusinessServiceException
     */
    public Collection<NagiosStatisticProperty> getApplicationStatisticsHostList(
            int appId, String[] hostNames) throws BusinessServiceException {
        if (hostNames != null && hostNames.length > 0) {
            Collection<NagiosStatisticProperty> aggregateCollection = new ArrayList<NagiosStatisticProperty>();

            for (int i = 0; i < hostNames.length; i++) {
                Collection<NagiosStatisticProperty> nagiosHostCollection = NAGIOS_HOST_FEATURES_STATISTIC
                        .get(hostNames[i]);
                if (nagiosHostCollection == null) {
                    return aggregateCollection;
                }
                for (Iterator iter = nagiosHostCollection.iterator(); iter
                        .hasNext(); ) {
                    NagiosStatisticProperty nagiosStatProp = (NagiosStatisticProperty) iter
                            .next();
                    aggregateCollection.add(nagiosStatProp);
                } // end for
            } // end for

            HashMap<String, NagiosStatisticProperty> initMap = new HashMap<String, NagiosStatisticProperty>();
            Iterator iter = aggregateCollection.iterator();
            while (iter.hasNext()) {
                NagiosStatisticProperty nagiosStatProp = (NagiosStatisticProperty) iter
                        .next();
                String propName = nagiosStatProp.getPropertyName().trim();
                if (!initMap.containsKey(propName))
                    initMap.put(propName, nagiosStatProp);
                else {
                    NagiosStatisticProperty initNagiosProp = initMap
                            .get(propName);
                    long hse = initNagiosProp.getHostStatisticEnabled()
                            + nagiosStatProp.getHostStatisticEnabled();
                    long hsd = initNagiosProp.getHostStatisticDisabled()
                            + nagiosStatProp.getHostStatisticDisabled();
                    long sse = initNagiosProp.getServiceStatisticEnabled()
                            + nagiosStatProp.getServiceStatisticEnabled();
                    long ssd = initNagiosProp.getServiceStatisticDisabled()
                            + nagiosStatProp.getServiceStatisticDisabled();
                    NagiosStatisticProperty newNagiosProp = new NagiosStatisticProperty(
                            propName, hse, hsd, sse, ssd);
                    initMap.remove(propName);
                    initMap.put(propName, newNagiosProp);
                } // end if
            } // end while
            return initMap.values();
        } else {
            throw new BusinessServiceException("Invalid Host List");
        } // end if
    }

    private NagiosStatisticProperty getNagiosStatPropByPropName(
            Collection<NagiosStatisticProperty> aggregateCollection,
            String propName) {
        Iterator iter = aggregateCollection.iterator();
        NagiosStatisticProperty nagiosStatProp = null;
        while (iter.hasNext()) {
            log.info("Iterating aggregate collection..");
            nagiosStatProp = (NagiosStatisticProperty) iter.next();
            if (nagiosStatProp.getPropertyName().equals(propName)) {
                log.info("Match on " + propName);
                return nagiosStatProp;
            } else {
                nagiosStatProp = null;
            } // end if

        } // end while
        log.info("Before match return");
        return nagiosStatProp;
    }

    private Collection<NagiosStatisticProperty> getApplicationStatisticsHost(
            String appName, String hostName) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        Collection<NagiosStatisticProperty> hs = NAGIOS_HOST_FEATURES_STATISTIC
                .get(hostName);

        if (hs == null) {
			/*
			 * If Host exists but doesn't contain any statistics just return an
			 * empty set
			 */
            log.warn(WARNING_NO_STATISTICS_FOR_HOST + hostName);
            return new Vector<NagiosStatisticProperty>();
        }

        return hs;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @seeorg.groundwork.foundation.bs.statistics.StatisticsService#
	 * getApplicationStatisticsHostTotal(int)
	 */
    public Collection<NagiosStatisticProperty> getApplicationStatisticsTotals(
            int appId) throws BusinessServiceException {
        Collection<NagiosStatisticProperty> hs = APPLICATION_STATISTIC_TOTALS
                .get(new Integer(appId));

        if (hs == null) {
			/*
			 * If Host exists but doesn't contain any statistics just return an
			 * empty set
			 */
            log.warn(WARNING_NO_APPLICATION_HOST_STATISTICS_TOTALS + appId);
            return new Vector<NagiosStatisticProperty>();
        }

        return hs;
    }

    public Collection<NagiosStatisticProperty> getApplicationStatisticTotals(
            int appId) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios
        return getApplicationStatisticTotals(APP_TYPE_NAGIOS);
    }

    public Collection<NagiosStatisticProperty> getApplicationStatisticTotals(
            String appName) throws BusinessServiceException {
        // TODO Implement ability to query by application type name - For now,
        // its just Nagios

        // Iterate over all host groups and create a sum of all Properties
        Enumeration enumHostGroups = HOSTGROUPS.keys();
        String hostGroupName;

        ConcurrentHashMap<String, NagiosStatisticProperty> properties = new ConcurrentHashMap<String, NagiosStatisticProperty>();

        while (enumHostGroups.hasMoreElements()) {
            hostGroupName = (String) enumHostGroups.nextElement();

            if (hostGroupName != null && hostGroupName.length() > 0) {
                Collection<NagiosStatisticProperty> nagiosStatistics = NAGIOS_FEATURES_STATISTIC
                        .get(hostGroupName);
                if (nagiosStatistics != null && nagiosStatistics.size() > 0) {
                    // Iterate over all Nagios statistics for the HostGroup
                    NagiosStatisticProperty nagiosStatisticHGProp = null;
                    NagiosStatisticProperty nagiosStatisticOverall = null;

                    Iterator itNagiosProp = nagiosStatistics.iterator();
                    while (itNagiosProp.hasNext()) {
                        nagiosStatisticHGProp = (NagiosStatisticProperty) itNagiosProp
                                .next();
                        if (nagiosStatisticHGProp != null) {
                            // Lookup the property. If it exists update
                            // otherwise add it to the map
                            nagiosStatisticOverall = (NagiosStatisticProperty) properties
                                    .get(nagiosStatisticHGProp
                                            .getPropertyName());

                            if (nagiosStatisticOverall == null) {
                                // First iteration
                                properties
                                        .put(
                                                nagiosStatisticHGProp
                                                        .getPropertyName(),
                                                (NagiosStatisticProperty) nagiosStatisticHGProp
                                                        .clone());
                            } else {
                                // Update the existing property
                                nagiosStatisticOverall
                                        .setHostStatisticDisabled(nagiosStatisticOverall
                                                .getHostStatisticDisabled()
                                                + nagiosStatisticHGProp
                                                .getHostStatisticDisabled());
                                nagiosStatisticOverall
                                        .setHostStatisticEnabled(nagiosStatisticOverall
                                                .getHostStatisticEnabled()
                                                + nagiosStatisticHGProp
                                                .getHostStatisticEnabled());
                                nagiosStatisticOverall
                                        .setServiceStatisticDisabled(nagiosStatisticOverall
                                                .getServiceStatisticDisabled()
                                                + nagiosStatisticHGProp
                                                .getServiceStatisticDisabled());
                                nagiosStatisticOverall
                                        .setServiceStatisticEnabled(nagiosStatisticOverall
                                                .getServiceStatisticEnabled()
                                                + nagiosStatisticHGProp
                                                .getServiceStatisticDisabled());
                            }
                        }
                    }
                }
            }
        }

        // return the property collection
        if (log.isInfoEnabled()) {
            log.info("Get All Nagios Statistics contains " + properties.size()
                    + " elements.");
        }

        return properties.values();
    }

    public StateStatistics getHostStatisticsByHostGroupId(int hostGroupId)
            throws BusinessServiceException {
        HostGroup hg = this.hostGroupService.getHostGroupById(hostGroupId);

        if (hg != null)
            return this.getHostStatisticsByHostGroupName(hg.getName());
        else {
            throw new BusinessServiceException(this.ERROR_HOSTGROUPID_NOT_EXIST
                    + hostGroupId);
        }
    }

    public Collection<StateStatistics> getHostStatisticsByHostGroupIds(
            Collection<Integer> hostGroupIds) throws BusinessServiceException {
        Collection<StateStatistics> col = new ArrayList<StateStatistics>();

        if (hostGroupIds == null || hostGroupIds.size() == 0)
            return col;

        Iterator<Integer> it = hostGroupIds.iterator();
        while (it.hasNext()) {
            col.add(getHostStatisticsByHostGroupId(it.next().intValue()));
        }

        return col;
    }

    public StateStatistics getHostStatisticsByHostGroupName(String hostGroupName)
            throws BusinessServiceException {
		/* Get host statistics */
        Vector<StateStatistics> hg = HOSTGROUP_HOST_STATISTIC
                .get(hostGroupName);

        if (hg == null || hg.size() == 0) {
			/*
			 * If HostGroup exists but doesn't contain any hosts no statistics
			 * will be available just return null
			 */
            if (HOSTGROUPS.containsKey(hostGroupName))
                return null;
            else
                throw new BusinessServiceException(
                        this.ERROR_HOSTGROUP_NOT_EXIST + hostGroupName);
        }
        return hg.elementAt(0);
    }

    public StateStatistics getServiceStatisticsByServiceGroupName(
            String serviceGroupName) throws BusinessServiceException {
		/* Get host statistics */
        Vector<StateStatistics> sg = SERVICEGROUP_SERVICE_STATISTIC
                .get(serviceGroupName);

        if (sg == null || sg.size() == 0) {
			/*
			 * If HostGroup exists but doesn't contain any hosts no statistics
			 * will be available just return null
			 */
            if (SERVICEGROUPS.containsKey(serviceGroupName))
                return null;
            else
                throw new BusinessServiceException("Service not in "
                        + serviceGroupName);
        }
        return sg.elementAt(0);
    }

    public Collection<StateStatistics> getHostStatisticsByHostGroupNames(
            Collection<String> hostGroupNames) throws BusinessServiceException {
        Collection<StateStatistics> col = new ArrayList<StateStatistics>();

        if (hostGroupNames == null || hostGroupNames.size() == 0)
            return col;

        Iterator<String> it = hostGroupNames.iterator();
        while (it.hasNext()) {
            col.add(getHostStatisticsByHostGroupName(it.next()));
        }

        return col;
    }

    /**
     *
     */
    public Collection<StateStatistics> getServiceStatisticsForAllServiceGroups()
            throws BusinessServiceException {
        Collection<StateStatistics> col = new ArrayList<StateStatistics>();

        Enumeration<String> enumeration = SERVICEGROUPS.keys();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                col.add(getServiceStatisticsByServiceGroupName(enumeration
                        .nextElement()));
            } // end while
        } // end if

        return col;
    }

    public StateStatistics getHostStatisticTotals()
            throws BusinessServiceException {
        return ALL_HOST_STATISTICS;
    }

    public double getHostAvailabilityForHostgroup(String hgName)
            throws BusinessServiceException {
        StateStatistics state;
        Vector<StateStatistics> stateList = (Vector<StateStatistics>) HOSTGROUP_HOST_STATISTIC
                .get(hgName);
        if (hgName == null || hgName.length() == 0 || hgName.equals("ALL"))
            state = ALL_HOST_STATISTICS;
        else
            state = (StateStatistics) stateList.elementAt(0);

        log
                .debug("StatisticsServiceImpl.getHostAvailabilityForHostgroup: hgName ["
                        + hgName + "] value=[" + state.getAvailability() + "]");
        return state.getAvailability();
    }

    public double getServiceAvailabilityForHostGroup(String hgName)
            throws BusinessServiceException {
        StateStatistics state;
        Vector<StateStatistics> stateList = (Vector<StateStatistics>) HOSTGROUP_SERVICE_STATISTIC
                .get(hgName);
        if (hgName == null || hgName.length() == 0 || hgName.equals("ALL"))
            state = ALL_SERVICE_STATISTICS;
        else
            state = (StateStatistics) stateList.elementAt(0);
        log
                .debug("StatisticsServiceImpl.getServiceAvailabilityForHostgroup: hgName ["
                        + hgName + "] value=[" + state.getAvailability() + "]");
        return state.getAvailability();
    }

    public double getServiceAvailabilityForServiceGroup(String sgName)
            throws BusinessServiceException {
        StateStatistics state;
        Vector<StateStatistics> stateList = (Vector<StateStatistics>) SERVICEGROUP_SERVICE_STATISTIC
                .get(sgName);
        if (sgName == null || sgName.length() == 0 || sgName.equals("ALL"))
            state = ALL_SERVICE_STATISTICS;
        else
            state = (StateStatistics) stateList.elementAt(0);
        log
                .debug("StatisticsServiceImpl.getServiceAvailabilityForServicegroup: sgName ["
                        + sgName + "] value=[" + state.getAvailability() + "]");
        return state.getAvailability();
    }

    public StateStatistics getServiceStatisticByHostId(int hostId)
            throws BusinessServiceException {
        // Get host by id
        Host host = hostService.getHostByHostId(hostId);
        if (host == null)
            throw new BusinessServiceException("Unknown host with id - "
                    + hostId);

        return getServiceStatisticByHostName(host.getHostName());
    }

    public StateStatistics getServiceStatisticByHostName(String hostName)
            throws BusinessServiceException {
        if (hostName == null || hostName.length() == 0) {
            throw new BusinessServiceException(
                    "Invalid null / empty host name parameter.");
        }

        // NOTE: We assume listOfServiceStatuses has been initialized
        Iterator<String> it = SERVICE_STATUS_LIST.iterator();

        MonitorStatus monitorStatus = null;
        String statusName = null;
        ArrayList<StatisticProperty> list = new ArrayList<StatisticProperty>(5);
        long serviceCount = 0;
        long count = 0;
        Object[] parameterValues = new Object[]{hostName, null};

        while (it.hasNext()) {
            statusName = it.next();
            if (statusName != null && statusName.length() > 0) {
                if (log.isInfoEnabled())
                    log.info("Retrieving Service Status Count for "
                            + statusName);

                monitorStatus = metadataService
                        .getMonitorStatusByName(statusName);

                if (log.isInfoEnabled())
                    log.info("Service Status Found for " + statusName);

                if (monitorStatus == null)
                    continue;

                parameterValues[1] = monitorStatus.getMonitorStatusId();

                List result = foundationDAO.query(
                        SELECT_HOST_SERVICE_STATUS_COUNT, parameterValues);

                count = ((Long) result.get(0)).longValue();
                serviceCount += count; // Note: We are assuming each host
                // service has a state (at least
                // unknown)

                list.add(new StatisticProperty(monitorStatus.getName(), count));

                if (log.isInfoEnabled())
                    log.info("Statistic Property Count: " + count + " for "
                            + statusName);
            }
        }

        // Wrap in state statistics - Note: The host group name is actually the
        // host name from the query
        return new StateStatistics(hostName, 1, serviceCount, list);
    }

    public StateStatistics getServiceStatisticsByHostGroupId(int hostGroupId)
            throws BusinessServiceException {
        HostGroup hg = this.hostGroupService.getHostGroupById(hostGroupId);

        if (hg != null)
            return this.getServiceStatisticsByHostGroupName(hg.getName());
        else {
            throw new BusinessServiceException(this.ERROR_HOSTGROUPID_NOT_EXIST
                    + hostGroupId);
        }
    }

    public Collection<StateStatistics> getServiceStatisticsByHostGroupIds(
            Collection<Integer> hostGroupIds) throws BusinessServiceException {
        Collection<StateStatistics> col = new ArrayList<StateStatistics>();

        if (hostGroupIds == null || hostGroupIds.size() == 0)
            return col;

        Iterator<Integer> it = hostGroupIds.iterator();
        while (it.hasNext()) {
            col.add(getServiceStatisticsByHostGroupId(it.next()));
        }

        return col;
    }

    public StateStatistics getServiceStatisticsByHostGroupName(
            String hostGroupName) throws BusinessServiceException {
        Vector<StateStatistics> hg = null;

        try {
			/* Get host statistics */
            hg = (Vector<StateStatistics>) HOSTGROUP_SERVICE_STATISTIC
                    .get(hostGroupName);
            if (hg == null || hg.size() == 0) {
                /*
				 * If HostGroup exists but doesn't contain any hosts no
				 * statistics will be available just return an empty Vector
				 */
                if (HOSTGROUPS.containsKey(hostGroupName)) {
                    return null;
                } else {
                    throw new BusinessServiceException(this.ERROR_HOSTGROUP_NOT_EXIST + hostGroupName);
                }
            }
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Exception occurred in getStatisticsForHostGroup()", e);
        }
        return hg.elementAt(0);
    }

    public Collection<StateStatistics> getServiceStatisticsByHostGroupNames(
            Collection<String> hostGroupNames) throws BusinessServiceException {
        Collection<StateStatistics> col = new ArrayList<StateStatistics>();

        if (hostGroupNames == null || hostGroupNames.size() == 0)
            return col;

        Iterator<String> it = hostGroupNames.iterator();
        while (it.hasNext()) {
            col.add(getServiceStatisticsByHostGroupName(it.next()));
        }

        return col;
    }

    public StateStatistics getServiceStatisticTotals()
            throws BusinessServiceException {
        return ALL_SERVICE_STATISTICS;
    }

    public Collection<StatisticProperty> getEventStatisticsByHostGroupName(
            String appType, String hostGroupName, String startDate,
            String endDate, String statisticType)
            throws BusinessServiceException {
        final StringBuilder where = new StringBuilder(64);
        String BASE_EVENT_STATISTIC = null;

        if ((appType != null && appType.length() > 0)
                && (appType.equalsIgnoreCase(ALL) == false)) {
            ApplicationType applicationType = this.metadataService
                    .getApplicationTypeByName(appType);
            if (applicationType == null) {
                StringBuilder err = new StringBuilder(
                        "getEventStatisticsByHostGroupName requires the ApplicationType. ApplicationType[");
                err.append(appType)
                        .append(" doesn't exist or is of value null");
                throw new BusinessServiceException(err.toString());
            }

            where.append(" AND lm.applicationType.applicationTypeId=");
            where.append(applicationType.getApplicationTypeId().intValue());
        }

        if (hostGroupName != null && hostGroupName.length() > 0
                && (hostGroupName.equalsIgnoreCase(ALL) == false)) {
            where.append(" AND hg.name='");
            where.append(hostGroupName);
            where.append(SINGLE_QUOTE);

            // Since host is a NAGIOS construct, we relate the log message to
            // the host through the host status field instead of device
            // Also, when log message are queried host or host group through the
            // LogMessageService the same relation is used
            // BASE_EVENT_STATISTIC =
            // "SELECT count(distinct lm.logMessageId) from LogMessage lm, HostGroup hg left join hg.hosts as h where lm.device.deviceId = h.device.deviceId";
            BASE_EVENT_STATISTIC = "SELECT count(distinct lm.logMessageId) from LogMessage lm, HostGroup hg left join hg.hosts as h where lm.hostStatus.hostStatusId = h.hostId";
        } else {
            // NOTE: We will be returning the event statistics for all events
            // regardless of association with a host group or not.
            BASE_EVENT_STATISTIC = "SELECT count(*) from LogMessage lm WHERE 1=1";
        }

        if (startDate != null && startDate.length() > 0) {
            where.append(" AND lm.lastInsertDate>='");
            where.append(startDate);
            where.append(SINGLE_QUOTE);
        }

        if (endDate != null && endDate.length() > 0) {
            where.append(" AND lm.lastInsertDate<='");

            // Add time to end date so it inclusive if it was not provided
            if (endDate.contains(COLON) == false)
                endDate = endDate.trim() + END_OF_DAY_TIME;

            where.append(endDate);
            where.append(SINGLE_QUOTE);
        }

        final StringBuilder query = new StringBuilder(BASE_EVENT_STATISTIC);
        Collection metaInfo = null;

        if (statisticType == null) {
            where.append(" AND lm.monitorStatus.monitorStatusId=?");
            metaInfo = metadataService.getMonitorStatusValues();
        } else if (statisticType.compareToIgnoreCase(STAT_TYPE_SEVERITY_STATUS) == 0) {
            where.append(" AND lm.severity.severityId=?");
            metaInfo = metadataService.getSeverityValues();
        } else if (statisticType.compareToIgnoreCase(STAT_TYPE_MONITOR_STATUS_WITH_OPEN) == 0) {
            if (hostGroupName != null) {
                if (hostGroupName.equalsIgnoreCase("ALL")) {
                    Collection<StatisticProperty> allStats = new ArrayList<>();
                    for (Collection<StatisticProperty> value : HOSTGROUP_OPEN_EVENT_STATS.values()) {
                        allStats.addAll(value);
                    } // end for
                    return allStats;
                } else {
                    Collection<StatisticProperty> stats = HOSTGROUP_OPEN_EVENT_STATS.get(hostGroupName);
                    // if for some reason hostgroup has no events, then just create collection of zero stat counts
                    if (stats == null) {
                        stats = new ArrayList<StatisticProperty>();
                        for (MonitorStatus monitorstatus : metadataService.getMonitorStatusValues()) {
                            StatisticProperty stat = new StatisticProperty();
                            stat.setName(monitorstatus.getName());
                            stat.setCount(0);
                            stats.add(stat);
                        } // end for
                    }  // end if
                    return stats;
                }
            } else {
                Collection<StatisticProperty> allStats = new ArrayList<>();
                for (Collection<StatisticProperty> value : HOSTGROUP_OPEN_EVENT_STATS.values()) {
                    allStats.addAll(value);
                }
                return allStats;
            }
        } else if (statisticType.compareToIgnoreCase(STAT_TYPE_SEVERITY_STATUS) == 0) {
            where.append(" AND lm.priority.priorityId=?");
            metaInfo = metadataService.getPriorityValues();
        } else if (statisticType
                .compareToIgnoreCase(STAT_TYPE_OPERATION_STATUS) == 0) {
            where.append(" AND lm.operationStatus.operationStatusId=?");
            metaInfo = metadataService.getOperationStatusValues();
        } else { // Default to Monitor Status
            where.append(" AND lm.monitorStatus.monitorStatusId=?");
            metaInfo = metadataService.getMonitorStatusValues();
        }

        // Append where clause
        query.append(where.toString());

        // Iterate over Metadata objects and call into the objects
        Iterator it = metaInfo.iterator();
        AttributeData attributeData = null;
        Vector<StatisticProperty> countVector = new Vector<StatisticProperty>(
                10);
        long count = 0;
        while (it.hasNext()) {
            attributeData = (AttributeData) it.next();

            if (attributeData != null) {
                Integer attributeID = attributeData.getID();

                count = getStateCount(query.toString(), attributeID.intValue());

                countVector.add(new StatisticProperty(attributeData.getName(),
                        count));

                if (log.isInfoEnabled())
                    log.info("Attribute Count: " + count + " for "
                            + attributeData.getName());
            }
        }

        return countVector;
    }

    /**
     * Method to getOpenEventStatisticsForHostgroups.
     *
     * @return
     */
    private void populateEventStatisticsForHostgroups() {
        CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "populateEventStatisticsForHostgroups");
        // Adding where clause join for operation statusid on operationstatus table is 10 times expensive
        // Than querying by value. Hence, first find the operationstatusid for open and then run the query
        Collection metaInfo = metadataService.getOperationStatusValues();
        Iterator it = metaInfo.iterator();
        AttributeData attributeData = null;
        int openStatusId = 0;
        while (it.hasNext()) {
            attributeData = (AttributeData) it.next();
            if (attributeData != null && attributeData.getName().equalsIgnoreCase("OPEN")) {
                openStatusId = attributeData.getID();
            }  // end if
        } // end while

        String maxAgeCriteria = (maxQueryAgeHours > 0 ? "AND lm.lastinsertdate > current_timestamp - interval '" + maxQueryAgeHours + " hour' " : "");

        String sql = "select hg.name as hostgroup, m.name as monitorstatus, count(lm.logMessageId) as count from LogMessage lm, HostGroup hg, host h," +
                "hostgroupcollection hc, monitorstatus m where lm.hostStatusId = h.hostId AND " +
                "lm.monitorStatusId=m.monitorstatusid AND lm.operationStatusid=? and hc.hostgroupid=hg.hostgroupid " +
                "and hc.hostid=h.hostid " +
                maxAgeCriteria +
                "group by hg.name,m.name order by hg.name";


        Object[] parameters = new Object[]{
                openStatusId};

        List l = foundationDAO.sqlQuery(sql.toString(),
                parameters);
        Vector<StatisticProperty> countVector = new Vector<StatisticProperty>(
                10);
        String lastHostgroup = null;
        if (l != null && l.size() > 0) {
            // first clear the cache
            HOSTGROUP_OPEN_EVENT_STATS.clear();
            for (int i=0;i < l.size(); i++) {
                Object[] currVal = (Object[]) l.get(i) ;
                String hostgroup = (String) currVal[0];
                String monitorStatus = (String) currVal[1];
                BigInteger count = (BigInteger) currVal[2];
                countVector.add(new StatisticProperty(monitorStatus,
                        count.longValue()));
                String nextHostgroup = (i+1) < l.size() ? (String) ((Object[])  l.get(i + 1) )[0] : hostgroup;
                if (!hostgroup.equalsIgnoreCase(nextHostgroup)) {
                    HOSTGROUP_OPEN_EVENT_STATS.put(hostgroup, countVector);
                    // Reinitialize the vector
                    countVector = new Vector<StatisticProperty>(
                            10);
                } // end if
                lastHostgroup = hostgroup;
            }
            // Now add the last hostgroup
            HOSTGROUP_OPEN_EVENT_STATS.put(lastHostgroup, countVector);
        } // end if
        stopMetricsTimer(timer);
    }

    public Collection<StatisticProperty> getEventStatisticsByHostName(
            String appType, String hostName, String startDate, String endDate,
            String statisticType) throws BusinessServiceException {
        final StringBuilder where = new StringBuilder(64);
        String BASE_EVENT_STATISTIC = null;

        if (appType != null && appType.length() > 0
                && (appType.equalsIgnoreCase(ALL) == false)) {
            ApplicationType applicationType = this.metadataService
                    .getApplicationTypeByName(appType);
            if (applicationType == null) {
                StringBuilder err = new StringBuilder(
                        "getEventStatisticsByHostName requires the ApplicationType. ApplicationType[");
                err.append(appType)
                        .append(" doesn't exist or is of value null");
                throw new BusinessServiceException(err.toString());
            }

            where.append(" AND lm.applicationType.applicationTypeId=");
            where.append(applicationType.getApplicationTypeId());
        }

        if (hostName != null && hostName.length() > 0
                && (hostName.equalsIgnoreCase(ALL) == false)) {
            where.append(" AND h.hostName='");
            where.append(hostName);
            where.append(SINGLE_QUOTE);

            // Since host is a NAGIOS construct, we relate the log message to
            // the host through the host status field instead of device
            // Also, when log message are queried host or host group through the
            // LogMessageService the same relation is used
            // BASE_EVENT_STATISTIC =
            // "SELECT count(distinct lm.logMessageId) from LogMessage lm, Host h where lm.device.deviceId = h.device.deviceId";
            BASE_EVENT_STATISTIC = "SELECT count(distinct lm.logMessageId) from LogMessage lm, Host h where lm.hostStatus.hostStatusId = h.hostId";
        } else {
            BASE_EVENT_STATISTIC = "SELECT count(*) from LogMessage lm WHERE 1=1";
        }

        if (startDate != null && startDate.length() > 0) {
            where.append(" AND lm.lastInsertDate>='");
            where.append(startDate);
            where.append(SINGLE_QUOTE);
        }

        if (endDate != null && endDate.length() > 0) {
            where.append(" AND lm.lastInsertDate<='");

            // Add time to end date so it inclusive if it was not provided
            if (endDate.contains(COLON) == false)
                endDate = endDate.trim() + END_OF_DAY_TIME;

            where.append(endDate);
            where.append(SINGLE_QUOTE);
        }

        final StringBuilder query = new StringBuilder(BASE_EVENT_STATISTIC);
        Collection metaInfo = null;

        if (statisticType == null) {
            where.append(" AND lm.monitorStatus.monitorStatusId=?");
            metaInfo = metadataService.getMonitorStatusValues();
        } else if (statisticType.equalsIgnoreCase(STAT_TYPE_SEVERITY_STATUS) == true) {
            where.append(" AND lm.severity.severityId=?");
            metaInfo = metadataService.getSeverityValues();
        } else if (statisticType.equalsIgnoreCase(STAT_TYPE_PRIORITY_STATUS) == true) {
            where.append(" AND lm.priority.priorityId=?");
            metaInfo = metadataService.getPriorityValues();
        } else if (statisticType.equalsIgnoreCase(STAT_TYPE_OPERATION_STATUS) == true) {
            where.append(" AND lm.operationStatus.operationStatusId=?");
            metaInfo = metadataService.getOperationStatusValues();
        } else { // Default to Monitor Status
            where.append(" AND lm.monitorStatus.monitorStatusId=?");
            metaInfo = metadataService.getMonitorStatusValues();
        }

        // Append where clause
        query.append(where.toString());

        // Iterate over Metadata objects and call into the objects
        Iterator it = metaInfo.iterator();
        AttributeData attributeData = null;
        Vector<StatisticProperty> countVector = new Vector<StatisticProperty>(
                10);
        long count = 0;
        String queryString = query.toString();

        while (it.hasNext()) {
            attributeData = (AttributeData) it.next();

            if (attributeData != null) {
                Integer attributeID = attributeData.getID();

                count = getStateCount(queryString, attributeID.intValue());

                countVector.add(new StatisticProperty(attributeData.getName(),
                        count));

                if (log.isInfoEnabled())
                    log.info("Attribute Count: " + count + " for "
                            + attributeData.getName());
            }
        }

        return countVector;
    }

    /**
     * Start default statistic calculations.
     */
    @Override
    public void startStatisticsCalculation() {
        startStatisticsCalculation(null, null, null);
    }

    /**
     * Start statistic calculations.
     *
     * @param propListOfHostStatuses
     * @param propListOfServiceStatuses
     * @param propListOfNagiosProperties
     */
    @Override
    public void startStatisticsCalculation(String propListOfHostStatuses,
                                           String propListOfServiceStatuses, String propListOfNagiosProperties) {
        if (propListOfHostStatuses == null
                || propListOfHostStatuses.length() == 0) {
            propListOfHostStatuses = DEFAULT_HOST_STATUS_LIST;
        }

        if (propListOfServiceStatuses == null
                || propListOfServiceStatuses.length() == 0) {
            propListOfServiceStatuses = DEFAULT_SERVICE_STATUS_LIST;
        }

        if (propListOfNagiosProperties == null
                || propListOfNagiosProperties.length() == 0) {
            propListOfNagiosProperties = DEFAULT_NAGIOS_STATUS_LIST;
        }

        // Make sure lists are cleared
        StatisticsServiceImpl.SERVICE_STATUS_LIST.clear();
        StatisticsServiceImpl.HOST_STATUS_LIST.clear();
        StatisticsServiceImpl.NAGIOS_PROPERTY_LIST.clear();

        String statusName = null;
        StringTokenizer tokenizer = new StringTokenizer(propListOfHostStatuses,
                SEMICOLON);
        while (tokenizer.hasMoreTokens()) {
            statusName = tokenizer.nextToken();
            if (statusName != null && statusName.length() > 0) {
                StatisticsServiceImpl.HOST_STATUS_LIST.add(statusName);
            }
        }

        statusName = null;
        tokenizer = new StringTokenizer(propListOfServiceStatuses, SEMICOLON);
        while (tokenizer.hasMoreTokens()) {
            statusName = tokenizer.nextToken();
            if (statusName != null && statusName.length() > 0) {
                StatisticsServiceImpl.SERVICE_STATUS_LIST.add(statusName);
            }
        }

        statusName = null;
        tokenizer = new StringTokenizer(propListOfNagiosProperties, SEMICOLON);
        while (tokenizer.hasMoreTokens()) {
            statusName = tokenizer.nextToken();
            if (statusName != null && statusName.length() > 0) {
                StatisticsServiceImpl.NAGIOS_PROPERTY_LIST.add(statusName);
            }
        }

        if (CALCULATE_STATISTICS_THREAD == null) {
            CALCULATE_STATISTICS_THREAD = new StatisticCalculationThread(this);
            log.debug("starting statistical calculation thread");
            CALCULATE_STATISTICS_THREAD.start();
        } else if (CALCULATE_STATISTICS_THREAD.isCalculating() == false) {
            // Already exists -- kill and restart
            log
                    .warn("Statistics calculation thread already running. Stop and recreate thread.");
            CALCULATE_STATISTICS_THREAD.stopThread();
            CALCULATE_STATISTICS_THREAD = new StatisticCalculationThread(this);
            CALCULATE_STATISTICS_THREAD.start();
        }
    }

    /**
     * Un Initialize statistics gathering -- shutdown threads
     */
    @Override
    public void stopStatisticsCalculation() {
        if (CALCULATE_STATISTICS_THREAD != null) {
            CALCULATE_STATISTICS_THREAD.stopThread();
            CALCULATE_STATISTICS_THREAD = null;
        }
    }

    public void notify(ServiceNotify notify) throws BusinessServiceException {
        if (notify == null)
            throw new IllegalArgumentException(
                    "Invalid null ServiceNotify parameter.");

        ServiceNotifyEntityType entityType = notify.getEntityType();
        ServiceNotifyAction action = notify.getAction();

        if (entityType.equals(ServiceNotifyEntityType.HOSTGROUP)) {
            String name = (String) notify.getAttribute(NOTIFY_ATTR_ENTITY_NAME);
            if (name == null || name.length() == 0)
                throw new BusinessServiceException(
                        "Invalid ServiceNotify instance.  No "
                                + NOTIFY_ATTR_ENTITY_NAME
                                + " attribute defined.");

            notifyHostGroupUpdate(name,
                    (action == ServiceNotifyAction.DELETE) ? true : false);
        } else if (entityType.equals(ServiceNotifyEntityType.HOST)) {
            String name = (String) notify.getAttribute(NOTIFY_ATTR_ENTITY_NAME);
            if (name != null && name.length() > 0) {
                notifyHostUpdate(name,
                        (action == ServiceNotifyAction.DELETE) ? true : false);
            } else // Host List
            {
                // Go through each host and update
                List<String> hostNames = (List<String>) notify
                        .getAttribute(NOTIFY_ATTR_HOST_LIST);
                if (hostNames == null || hostNames.size() == 0)
                    return; // Nothing to do

                Iterator<String> it = hostNames.iterator();
                while (it.hasNext()) {
                    name = it.next();

                    if (name != null)
                        notifyHostUpdate(name,
                                (action == ServiceNotifyAction.DELETE) ? true
                                        : false);
                }
            }
        } else if (entityType.equals(ServiceNotifyEntityType.SERVICEGROUP)) {
            String name = (String) notify.getAttribute(NOTIFY_ATTR_ENTITY_NAME);
            if (name == null || name.length() == 0)
                throw new BusinessServiceException(
                        "Invalid ServiceNotify instance.  No "
                                + NOTIFY_ATTR_ENTITY_NAME
                                + " attribute defined.");

            notifyServiceGroupUpdate(name,
                    (action == ServiceNotifyAction.DELETE) ? true : false);
        } else if (entityType.equals(ServiceNotifyEntityType.SERVICESTATUS)) {
            Integer id = (Integer) notify.getAttribute(NOTIFY_ATTR_ENTITY_ID);
            if (id == null || id.intValue() <= 0)
                throw new BusinessServiceException(
                        "Invalid ServiceNotify instance.  No "
                                + NOTIFY_ATTR_ENTITY_ID
                                + " attribute defined.");

            notifyServiceStatusUpdate(id.intValue());
        }
    }

    public List<String> getHostStatusList() {
        return HOST_STATUS_LIST;
    }

    public List<String> getServiceStatusList() {
        return SERVICE_STATUS_LIST;
    }

    public List<String> getApplicationPropertyList(int appId) {
        // For now, just return Nagios property list
        if (appId == 100)
            return NAGIOS_PROPERTY_LIST;

        return null;
    }

    public FoundationQueryList getStatistics(EntityType entityType,
                                             Map<String, Object> parameters) {
        if (entityType == null)
            throw new IllegalArgumentException(
                    "Invalid null EntityType parameter.");

        String entityName = entityType.getName();
        if (entityName == null)
            throw new IllegalArgumentException(
                    "Invalid EntityType parameter - Entity name not defined.");

        try {
            if (entityName
                    .equalsIgnoreCase(LogMessageStatistic.ENTITY_TYPE_CODE)) {
                return getEventStatistics(parameters);
            } else if (entityName
                    .equalsIgnoreCase(HostStatistic.ENTITY_TYPE_CODE)) {
                return getHostStatistics(parameters);
            } else if (entityName
                    .equalsIgnoreCase(ServiceStatistic.ENTITY_TYPE_CODE)) {
                return getServiceStatistics(parameters);
            } else if (entityName
                    .equalsIgnoreCase(HostStateTransition.ENTITY_TYPE_CODE)) {
                return getHostStateTransitions(parameters);
            } else if (entityName
                    .equalsIgnoreCase(ServiceStateTransition.ENTITY_TYPE_CODE)) {
                return getServiceStateTransitions(parameters);
            }
        } catch (Exception e) {
            // Note: We are only logging the error and returning an empty
            // FoundationQueryList to
            // avoid exceptions in reports. For example, if a query for a host
            // that does not exist
            // occurs we just return the empty list instead of the exception
            // that is thrown from the
            // business service
            log
                    .error(
                            "Exception performing getStatistics(EntityType, Map<String, Object>)",
                            e);
            return new FoundationQueryList(new ArrayList(0), 0);
        }

        throw new BusinessServiceException(
                "Statistics not handled for entity - " + entityType);
    }

    /*************************************************************************/
	/* Protected Methods */
    /*************************************************************************/

    /**
     * Update/re-query all statistic counts for HostGroups that are marked dirty
     * <p/>
     * The update will be done for Service, Host and Naiso statistics
     */
    protected void updateHostGroupStatistics(List<String> listOfHostStatuses,
                                             List<String> listOfServiceStatuses,
                                             List<String> listOfNagiosProperties)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "updateHostGroupStatistics");
		/* Requires at least a list of HostStatuses to calculate statistics */
        if (listOfHostStatuses == null || listOfHostStatuses.size() == 0)
            throw new BusinessServiceException(ERROR_HOST_STATUS_LIST);

        StatisticGroupState state = null;
        String hgNameKey = null;
        Enumeration enumHG = HOSTGROUPS.keys();
        Iterator<String> it = null;

        boolean overallHostStatisticsUpdated = false;
        boolean overallServiceStatisticsChanged = false;
        boolean overallHostGroupStatisticsChanged = false;

        Vector<StatisticProperty> allHostStatistics = new Vector<StatisticProperty>(
                10);
        Vector<StatisticProperty> allServiceStatistics = new Vector<StatisticProperty>(
                10);

        long hostCount = 0;
        long serviceCount = 0;

        while (enumHG.hasMoreElements()) {
            hgNameKey = (String) enumHG.nextElement();
            if (hgNameKey != null) {
                state = (StatisticGroupState) HOSTGROUPS.get(hgNameKey);

				/*
				 * If any host or/and any associated service has changed (call
				 * into notifyHostUpdate) the isDirty flag is set and will
				 * trigger an update of the counts for Host, Services and Nagios
				 * Properties
				 */
                if (state != null && state.getIsDirty() == true) {
                    overallHostGroupStatisticsChanged = true;
                    hostCount = this.hostGroupService
                            .getHostGroupHostCount(hgNameKey);
                    serviceCount = this.hostGroupService
                            .getHostGroupServiceCount(hgNameKey);

                    // HostGroup and host member changed update counts
                    if (log.isInfoEnabled())
                        log
                                .info("StateStaistics.updateHostGroupStatistics. HostGroup ["
                                        + hgNameKey
                                        + "] has been updated. Recalculate statistics.");

					/*
					 * Tokenize the list of Host Statuses
					 *
					 * The list of Hosts Statuses is defined as a property
					 * (statistics.hoststatus) in the listener
					 * foundation.properties file
					 */
                    if (listOfHostStatuses != null
                            && listOfHostStatuses.size() > 0) {
                        it = listOfHostStatuses.iterator();

                        Vector<StatisticProperty> hostStatistics = new Vector<StatisticProperty>(
                                listOfHostStatuses.size());

                        long count = 0;
                        double hostAvailability = 0;
                        while (it.hasNext()) {
                            String token = it.next();
                            if (token != null && token.length() > 0) {
                                // get count for properties and update statistic
                                // lists
                                count = this.getHostStatusCountForHostGroup(
                                        hgNameKey, token);
                                StatisticProperty prop = new StatisticProperty(
                                        token, count);
                                if (token.equals("UP"))
                                    if (hostCount == 0)
                                        hostAvailability = 0;
                                    else
                                        hostAvailability = count * 100 / hostCount;

                                if (log.isInfoEnabled())
                                    log
                                            .info("Update HostGroup Host Statistics. MonitorStatus ["
                                                    + token
                                                    + "] count ["
                                                    + count
                                                    + "] for HostGroup ["
                                                    + hgNameKey + "] ");

                                hostStatistics.add(prop);

                                if (overallHostStatisticsUpdated == false) {
                                    count = this.getHostStatusForAll(token);
                                    StatisticProperty statProp = new StatisticProperty(
                                            token, count);

                                    if (log.isInfoEnabled())
                                        log
                                                .info("All Host Statistics MonitorStatus ["
                                                        + token
                                                        + "] count ["
                                                        + count + "] ");

                                    allHostStatistics.add(statProp);
                                }
                            }
                        }

                        // Overall Statistics updated. Needs to be calculated
                        // only for one HostGroup
                        overallHostStatisticsUpdated = true;
                        log
                                .debug("updateHostGroupStatistics: hostAvailability ["
                                        + hostAvailability + "]");
                        StateStatistics stat = new StateStatistics(hgNameKey,
                                hostCount, serviceCount, hostStatistics,
                                hostAvailability);

                        // Updated the cached list for this hostgroup
                        // this.hostGroupHostStatistic.put(hgNameKey,
                        // hostStatistics);
                        Vector<StateStatistics> statisticsVector = new Vector<StateStatistics>(
                                1);
                        statisticsVector.add(stat);
                        HOSTGROUP_HOST_STATISTIC.put(hgNameKey,
                                statisticsVector);
                    }

					/*
					 * Tokenize the list of Service Statuses
					 *
					 * The list of Service Statuses is defined as a property
					 * (statistics.servicestatus) in the listener
					 * service.properties file
					 */
                    if (listOfServiceStatuses != null
                            && listOfServiceStatuses.size() > 0) {
                        it = listOfServiceStatuses.iterator();

                        Vector<StatisticProperty> serviceStatistics = new Vector<StatisticProperty>(
                                listOfServiceStatuses.size());

                        long count = 0;
                        double serviceAvailability = 0.00;
                        while (it.hasNext()) {
                            String token = it.next();
                            if (token != null && token.length() > 0) {
                                // get count for properties and update statistic
                                // lists
                                count = this.getServiceStatusCountForHostGroup(
                                        hgNameKey, token);
                                StatisticProperty prop = new StatisticProperty(
                                        token, count);
                                serviceStatistics.add(prop);
                                if (token.equals("OK") && serviceCount > 0)
                                    serviceAvailability = count * 100
                                            / serviceCount;

                                if (log.isInfoEnabled())
                                    log
                                            .info("Update HostGroup Service Statistics. MonitorStatus ["
                                                    + token
                                                    + "] count ["
                                                    + count
                                                    + "] for HostGroup ["
                                                    + hgNameKey + "] ");

                                if (overallServiceStatisticsChanged == false) {
                                    count = this.getServiceStatusForAll(token);
                                    StatisticProperty statProp = new StatisticProperty(
                                            token, count);
                                    if (log.isInfoEnabled())
                                        log
                                                .info("All Service Statistics MonitorStatus ["
                                                        + token
                                                        + "] count ["
                                                        + count + "] ");

                                    allServiceStatistics.add(statProp);
                                }
                            }
                        }
                        log
                                .debug("updateHostGroupStatistics: serviceAvailability ["
                                        + serviceAvailability + "]");
                        // Get the overall statistics
                        StateStatistics stat = new StateStatistics(hgNameKey,
                                hostCount, serviceCount, serviceStatistics,
                                serviceAvailability);

                        // this.hostGroupServiceStatistic.put(hgNameKey,
                        // serviceStatistics);
                        Vector<StateStatistics> statisticsVector = new Vector<StateStatistics>(
                                1);
                        statisticsVector.add(stat);
                        HOSTGROUP_SERVICE_STATISTIC.put(hgNameKey,
                                statisticsVector);

                        // Overall Statistics updated. Needs to be calculated
                        // only for one HostGroup
                        overallServiceStatisticsChanged = true;

						/*
						 * Tokenize the list of Nagios properties used for
						 * Statistics
						 *
						 * The list of Nagios properties is defined as a
						 * property (statistics.nagios) in the listener
						 * foundation.properties file
						 */
                        if (listOfNagiosProperties != null
                                && listOfNagiosProperties.size() > 0) {
                            Collection<NagiosStatisticProperty> nagiosStatistics = getNagiosStatisticsForHostGroup(
                                    hgNameKey, listOfNagiosProperties);
                            NAGIOS_FEATURES_STATISTIC.put(hgNameKey,
                                    nagiosStatistics);
                        }
                    }

                    // Reset isDirty since counters were updated
                    state.setIsDirty(false);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log
                    .debug("Statistic DAO -- Calculated all Host statistics Vector size ["
                            + allHostStatistics.size() + "]");
            log
                    .debug("Statistic DAO -- Calculated all Service statistics size ["
                            + allServiceStatistics.size() + "]");
        }

        if (overallHostGroupStatisticsChanged) {
            log.debug("Host groups stats has changed...");
            // Reassign recalculated Host and Service statistics
            hostCount = this.getHostsCount();
            serviceCount = this.getServicesCount();

            if (allHostStatistics.size() > 0) {
                double statistics = 0;
                if (hostCount == 0)
                    statistics = 0;
                else
                    statistics = getHostStatusForAll("UP") * 100 / hostCount;

                ALL_HOST_STATISTICS = new StateStatistics(ALL_HOSTGROUP,
                        hostCount, serviceCount, allHostStatistics,
                        statistics);
            }

            if (allServiceStatistics.size() > 0) {
                double statistics = 0;
                if (hostCount == 0)
                    statistics = 0;
                else
                    statistics = getServiceStatusForAll("UP") * 100 / hostCount;

                ALL_SERVICE_STATISTICS = new StateStatistics(ALL_HOSTGROUP,
                        hostCount, serviceCount, allServiceStatistics,
                        statistics);
            }

			/* Update all Hosts statistics */
            Enumeration enumHosts = HOSTS.keys();
            String hostNameKey = null;
            Boolean hostState = null;

            while (enumHosts.hasMoreElements()) {
                hostNameKey = (String) enumHosts.nextElement();
                if (hostNameKey != null) {
                    hostState = (Boolean) HOSTS.get(hostNameKey);
                    if (hostState == null || hostState.booleanValue() == true) {
                        // Recalculate

                        log.info("Statistics for Host [" + hostNameKey
                                + "] needs to be recalculated");

                        if (listOfNagiosProperties != null) {
                            Collection<NagiosStatisticProperty> nagiosHostStatistics = getNagiosStatisticsForHost(
                                    hostNameKey, listOfNagiosProperties);
                            NAGIOS_HOST_FEATURES_STATISTIC.put(hostNameKey,
                                    nagiosHostStatistics);
                        }
                        // Done
                        HOSTS.put(hostNameKey, new Boolean(false));
                    } else {

                        log.info("Statistics for Host [" + hostNameKey
                                + "] have not changed. No recalc");
                    }
                }
            }

			/*
			 * Recalculate application Statistics totals (Hosts and services)
			 * for application NAGIOS
			 */
            try {
                if (listOfNagiosProperties != null) {
                    Collection<NagiosStatisticProperty> nagiosStatisticsTotals = getApplicationStatisticsTotals(listOfNagiosProperties);
                    APPLICATION_STATISTIC_TOTALS.put(new Integer(100),
                            nagiosStatisticsTotals);
                }

            } catch (Exception e) {
                log
                        .error("Application Statistic Totals calculation failed. Error: "
                                + e);
            }
        } else {
            log.info("Host groups stats has NOT changed...");
        } // end if
        stopMetricsTimer(timer);
    }

    /**
     * Update/re-query all statistic counts for ServiceGroups that are marked
     * dirty
     * <p/>
     * The update will be done for Service, and Nagios statistics
     */
    protected void updateServiceGroupStatistics(
            List<String> listOfServiceStatuses,
            List<String> listOfNagiosProperties)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "updateServiceGroupStatistics");
        log.info("Enter updateServiceGroupStatistics: ");
        if (listOfNagiosProperties != null && listOfNagiosProperties.size() > 0) {

            if (listOfServiceStatuses == null
                    || listOfServiceStatuses.size() == 0)
                throw new BusinessServiceException(ERROR_SERVICE_STATUS_LIST);

            StatisticGroupState state = null;
            String sgNameKey = null;
            Enumeration enumSG = SERVICEGROUPS.keys();
            Iterator<String> it = null;

            boolean overallServiceStatisticsChanged = false;
            boolean overallServiceGroupStatisticsChanged = false;

            Vector<StatisticProperty> allServiceStatistics = new Vector<StatisticProperty>(
                    10);

            long serviceCount = 0;

            // No need to recalc
            while (enumSG.hasMoreElements()) {
                sgNameKey = (String) enumSG.nextElement();
                if (sgNameKey != null) {
                    state = (StatisticGroupState) SERVICEGROUPS.get(sgNameKey);

					/*
					 * If any host or/and any associated service has changed
					 * (call into notifyHostUpdate) the isDirty flag is set and
					 * will trigger an update of the counts for Host, Services
					 * and Nagios Properties
					 */
                    log.info("State is : " + state.getIsDirty());
                    if (state != null && state.getIsDirty() == true) {
                        overallServiceGroupStatisticsChanged = true;
                        serviceCount = this
                                .getServiceGroupServiceCount(sgNameKey);

                        // HostGroup and host member changed update counts
                        if (log.isInfoEnabled())
                            log
                                    .info("StateStistics.updateServiceGroupStatistics. ServiceGroup ["
                                            + sgNameKey
                                            + "] has been updated. Recalculate statistics.");

						/*
						 * Tokenize the list of Service Statuses
						 *
						 * The list of Service Statuses is defined as a property
						 * (statistics.servicestatus) in the listener
						 * service.properties file
						 */
                        if (listOfServiceStatuses != null
                                && listOfServiceStatuses.size() > 0) {
                            it = listOfServiceStatuses.iterator();

                            Vector<StatisticProperty> serviceStatistics = new Vector<StatisticProperty>(
                                    listOfServiceStatuses.size());

                            long count = 0;
                            double serviceAvailability = 0.00;
                            while (it.hasNext()) {
                                String token = it.next();
                                if (token != null && token.length() > 0) {
                                    // get count for properties and update
                                    // statistic
                                    // lists
                                    count = this
                                            .getServiceStatusCountForServiceGroup(
                                                    sgNameKey, token);
                                    StatisticProperty prop = new StatisticProperty(
                                            token, count);
                                    serviceStatistics.add(prop);
                                    if (token.equals("OK") && serviceCount > 0)
                                        serviceAvailability = count * 100
                                                / serviceCount;

                                    if (log.isInfoEnabled())
                                        log
                                                .info("Update ServiceGroup Service Statistics. MonitorStatus ["
                                                        + token
                                                        + "] count ["
                                                        + count
                                                        + "] for ServiceGroup ["
                                                        + sgNameKey + "] ");

                                    if (overallServiceStatisticsChanged == false) {
                                        count = this
                                                .getServiceStatusForAll(token);
                                        StatisticProperty statProp = new StatisticProperty(
                                                token, count);
                                        if (log.isInfoEnabled())
                                            log
                                                    .info("All Service Statistics MonitorStatus ["
                                                            + token
                                                            + "] count ["
                                                            + count + "] ");

                                        allServiceStatistics.add(statProp);
                                    }
                                }
                            }
                            log
                                    .debug("updateServiceGroupStatistics: serviceAvailability ["
                                            + serviceAvailability + "]");
                            // Get the overall statistics
                            StateStatistics stat = new StateStatistics(
                                    sgNameKey, 0, serviceCount,
                                    serviceStatistics, serviceAvailability);

                            Vector<StateStatistics> statisticsVector = new Vector<StateStatistics>(
                                    1);
                            statisticsVector.add(stat);
                            SERVICEGROUP_SERVICE_STATISTIC.put(sgNameKey,
                                    statisticsVector);

                            // Overall Statistics updated. Needs to be
                            // calculated
                            // only for one HostGroup
                            overallServiceStatisticsChanged = true;

							/*
							 * Tokenize the list of Nagios properties used for
							 * Statistics
							 *
							 * The list of Nagios properties is defined as a
							 * property (statistics.nagios) in the listener
							 * foundation.properties file
							 */
                            if (listOfNagiosProperties != null
                                    && listOfNagiosProperties.size() > 0) {
                                Collection<NagiosStatisticProperty> nagiosStatistics = getNagiosStatisticsForServiceGroup(
                                        sgNameKey, listOfNagiosProperties);
                                NAGIOS_SERVICEGROUP_STATISTIC.put(sgNameKey,
                                        nagiosStatistics);
                            }
                        }

                        // Reset isDirty since counters were updated
                        state.setIsDirty(false);
                    } else {
                        log.info("Service groups stats has NOT changed...");
                    } // end if/else
                }
            }
        }
        stopMetricsTimer(timer);
    }

	/* Private Helpers for retrieving statistics information from the backend */

	/*
	 * Generate the list of host groups and it's members
	 */

    protected boolean generateHostGroupCollections()
            throws BusinessServiceException {
        try {
			/* Clean all existing host groups */
            HOSTGROUPS.clear();

            // Query list of Host Groups and Hosts. Each item returned is an
            // Object[] of two String elements. Note, host name may be
            // null if there are no hosts related to the host group
            List l = foundationDAO
                    .query("SELECT hg.name, h.hostName FROM HostGroup hg LEFT OUTER JOIN hg.hosts h ORDER BY hg.name, h.hostName");

            if (l == null || l.size() == 0) {
                log.warn(WARN_NO_HOSTGROUP_DEFINED);
            } else {
                StatisticGroupState state = null;
                Object[] row = null;
                String hostGroupName = null;
                String lastHostGroupName = null;
                String hostName = null;
                int hostgroupCount = 0;

                Iterator it = l.iterator();
                while (it.hasNext()) {
                    row = (Object[]) it.next();

                    // The number of objects in the array match the number of
                    // columns defined in the SELECT
                    hostGroupName = (String) row[0];
                    hostName = (String) row[1];

                    if (lastHostGroupName == null
                            || lastHostGroupName
                            .equalsIgnoreCase(hostGroupName) == false) {
                        state = new StatisticGroupState();

                        // Add State To Host Group Collection
                        HOSTGROUPS.put(hostGroupName, state);

                        // Make sure empty collection is not included in lookup
                        state.setIsDirty(false);

                        lastHostGroupName = hostGroupName;
                        hostgroupCount++;
                    }

                    if (hostName != null && hostName.length() > 0) {
                        state.addElementToLookup(hostName);

                        // Make sure state is marked as dirty since we have
                        // added a host
                        state.setIsDirty(true);
                    }
                }

                if (log.isInfoEnabled())
                    log
                            .info("StateStatistics.generateHostGroupCollections found ["
                                    + hostgroupCount + "] hostgroups.");
            }
        } catch (Exception e) {
            // NOTE: We are only logging an error and not throwing an exception
            log.error("GenerateHostGroupCollections() failed. Error: " + e);
            return false; // Indicate that we were unable to get host groups
        }

        return true;
    }

	/* Private Helpers for retrieving statistics information from the backend */

	/*
	 * Generate the list of service groups and it's members
	 */

    protected boolean generateServiceGroupCollections()
            throws BusinessServiceException {
        try {
			/* Clean all existing host groups */
            SERVICEGROUPS.clear();

            // Query list of Host Groups and Hosts. Each item returned is an
            // Object[] of two String elements. Note, host name may be
            // null if there are no hosts related to the host group
            List l = foundationDAO
                    .sqlQuery("select c.Name,ss.ServiceStatusID from " +
                            "Category c, ServiceStatus ss , CategoryEntity ce, EntityType etc, EntityType etce where " +
                            "c.EntityTypeId=etc.EntityTypeId and " +
                            "etc.Name='SERVICE_GROUP' and " +
                            "c.CategoryID=ce.CategoryID and " +
                            "ce.ObjectID=ss.ServiceStatusID and " +
                            "ce.EntityTypeId=etce.EntityTypeId and " +
                            "etce.Name='SERVICE_STATUS' " +
                            "order by Name, ServiceDescription");

            if (l == null || l.size() == 0) {
                log.warn(WARN_NO_SERVICEGROUP_DEFINED);
            } else {
                StatisticGroupState state = null;
                Object[] row = null;
                String serviceGroupName = null;
                String lastServiceGroupName = null;
                Integer serviceStatusID = null;
                int serviceGroupCount = 0;

                Iterator it = l.iterator();
                while (it.hasNext()) {
                    row = (Object[]) it.next();

                    // The number of objects in the array match the number of
                    // columns defined in the SELECT
                    serviceGroupName = (String) row[0];
                    serviceStatusID = (Integer) row[1];

                    if (lastServiceGroupName == null
                            || lastServiceGroupName
                            .equalsIgnoreCase(serviceGroupName) == false) {
                        state = new StatisticGroupState();

                        // Add State To Host Group Collection
                        SERVICEGROUPS.put(serviceGroupName, state);

                        // Make sure empty collection is not included in lookup
                        state.setIsDirty(false);

                        lastServiceGroupName = serviceGroupName;
                        serviceGroupCount++;
                    }

                    if (serviceStatusID != null && serviceStatusID.intValue() > 0) {
                        state.addElementToLookup(String.valueOf(serviceStatusID.intValue()));

                        // Make sure state is marked as dirty since we have
                        // added a host
                        state.setIsDirty(true);
                    }
                }

                if (log.isInfoEnabled())
                    log
                            .info("StateStatistics.generateServiceGroupCollections found ["
                                    + serviceGroupCount + "] servicegroups.");
            }
        } catch (Exception e) {
            // NOTE: We are only logging an error and not throwing an exception
            log.error("GenerateServiceGroupCollections() failed. Error: " + e);
            return false; // Indicate that we were unable to get host groups
        }

        return true;
    }

    /*
	 * Generate a collection of Hosts which are in the system A boolean is
	 * returned indicating whether the query was successful
	 */
    protected boolean generateHostCollection() throws BusinessServiceException {
		/* Generate list of hosts */
        try {
			/* Clean all existing hosts */
            HOSTS.clear();

			/* Get all Host Names */
            List l = foundationDAO.query("SELECT h.hostName FROM Host h");

            if (l == null || l.size() == 0) {
                log.warn(WARN_NO_HOST_DEFINED);
            } else {
                String hostName = null;

                Iterator it = l.iterator();
                while (it.hasNext()) {
                    hostName = (String) it.next();

                    // The number of objects in the array match the number of
                    // columns defined in the SELECT
                    if (hostName != null && hostName.length() > 0) {
                        // Host Name and state to lookup list and mark it as
                        // dirty
                        HOSTS.put(hostName, new Boolean(true));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Generate Host List failed. Error: " + e);
            return false; // Indicate that we were unable to get hosts
        }

        return true;
    }

    /*************************************************************************/
	/* Private Methods */
    /*************************************************************************/

    /**
     * notifyHostGroupUpdate
     *
     * @param hostGroupName name of the hostgroup that changed
     * @param isDeleted     Indicates if Hostgroup was deleted (true) or modified (false)
     */
    private void notifyHostGroupUpdate(String hostGroupName, boolean isDeleted) {
        StatisticGroupState state = null;
        if (hostGroupName == null) {
            log.warn("HostGroup Name can't be null in notifyHostGroupUpdate");
            return;
        }

        if (isDeleted) {
            // Remove hostgroup from all cached statistics
            state = (StatisticGroupState) HOSTGROUPS.remove(hostGroupName);
            HOSTGROUP_HOST_STATISTIC.remove(hostGroupName);
            HOSTGROUP_SERVICE_STATISTIC.remove(hostGroupName);
            NAGIOS_FEATURES_STATISTIC.remove(hostGroupName);

            if (log.isInfoEnabled()) {
                if (state == null)
                    log.info("HostGroup " + hostGroupName
                            + " doesn't exist in statistics hostgroup cache");
                else
                    log.info("HostGroup " + hostGroupName
                            + " removed from statistics hostgroup cache");
            }
        } else {
            // Make sure hostgroup is update to date with the correct hosts.
            updateHostGroupState(hostGroupName);
        }
    }

    /**
     * notifyHostGroupUpdate
     *
     * @param serviceGroupName name of the hostgroup that changed
     * @param isDeleted        Indicates if Hostgroup was deleted (true) or modified (false)
     */
    private void notifyServiceGroupUpdate(String serviceGroupName, boolean isDeleted) {
        StatisticGroupState state = null;
        if (serviceGroupName == null) {
            log.warn("ServiceGroup Name can't be null in notifyServiceGroupUpdate");
            return;
        }

        if (isDeleted) {
            // Remove hostgroup from all cached statistics
            state = (StatisticGroupState) SERVICEGROUPS.remove(serviceGroupName);
            SERVICEGROUP_SERVICE_STATISTIC.remove(serviceGroupName);
            NAGIOS_FEATURES_STATISTIC.remove(serviceGroupName);

            if (log.isInfoEnabled()) {
                if (state == null)
                    log.info("ServiceGroup " + serviceGroupName
                            + " doesn't exist in statistics servicegroup cache");
                else
                    log.info("ServiceGroup " + serviceGroupName
                            + " removed from statistics servicegroup cache");
            }
        } else {
            // Make sure servicegroup is update to date with the correct services.
            updateServiceGroupState(serviceGroupName);
        }
    }

    /**
     * Notifies the statistic module about any updates to the system. The
     * function marks all internal structures about the update
     *
     * @param hostName
     */
    private void notifyHostUpdate(String hostName, boolean isDeleted) {
        // Update Host list
        if (hostName != null && hostName.length() > 0) {
            if (isDeleted == true) {
                HOSTS.remove(hostName);
                NAGIOS_HOST_FEATURES_STATISTIC.remove(hostName);

                log.info("Host Statistics Host removed: " + hostName);
            } else {
                // Mark as dirty
                HOSTS.put(hostName, new Boolean(true));

                log.info("Host Statistics Host updated. Mark as dirty: "
                        + hostName);
            }
        }

        // Check where the Host belongs to
        Enumeration enumHostGroups = HOSTGROUPS.keys();
        String hostGroupName = null;
        StatisticGroupState state = null;

        while (enumHostGroups.hasMoreElements()) {
            hostGroupName = (String) enumHostGroups.nextElement();
            if (hostGroupName != null && hostGroupName.length() > 0) {
                state = (StatisticGroupState) HOSTGROUPS.get(hostGroupName);
                if (state != null) {
                    state.updateElement(hostName);
                }
            }
        }
    }

    /**
     * Notifies the statistic module about any updates to the system. The
     * function marks all internal structures about the update
     *
     * @param serviceId
     */
    private void notifyServiceStatusUpdate(int serviceId) {
        log.info("Enter notifyServiceStatusUpdate...");
        // Update service groups
        Enumeration enumServiceGroups = SERVICEGROUPS.keys();
        String serviceGroupName = null;
        StatisticGroupState state = null;

        while (enumServiceGroups.hasMoreElements()) {
            serviceGroupName = (String) enumServiceGroups.nextElement();
            if (serviceGroupName != null && serviceGroupName.length() > 0) {
                state = (StatisticGroupState) SERVICEGROUPS.get(serviceGroupName);
                if (state != null) {
                    log.info("Updating servicestatus ID(Marking as dirty): " + serviceId);
                    state.updateElement(String.valueOf(serviceId));
                    state.setIsDirty(true);
                } // end if
            } // end if
        }
    }


    /**
     * Adds or updates the StatisticGroupState for the identified host group.
     *
     * @param hostGroupName
     */
    private void updateHostGroupState(String hostGroupName) {
        if (hostGroupName == null || hostGroupName.length() == 0) {
            log
                    .warn("updateHostGroupState() - Invalid null / empty host group name parameter.");
            return;
        }

        try {
            // Query list of Host Groups and Hosts. Each item returned is an
            // Object[] of two String elements. Note, host name may be
            // null if there are no hosts related to the host group
            List l = (List) foundationDAO
                    .query(
                            "SELECT h.hostName FROM HostGroup hg LEFT OUTER JOIN hg.hosts h WHERE hg.name = ?",
                            hostGroupName);

            if (l == null || l.size() == 0) {
                log.warn(WARN_NO_HOSTGROUP_DEFINED);
            } else {
                StatisticGroupState state = new StatisticGroupState();
                String hostName = null;
                int hostCount = 0;

                // Initially, make sure empty collection is not included in
                // lookup unless there are hosts
                state.setIsDirty(false);

                // Add / Replace State in Host Group Collection
                HOSTGROUPS.put(hostGroupName, state);

                Iterator it = l.iterator();
                while (it.hasNext()) {
                    hostName = (String) it.next();

                    if (hostName != null && hostName.length() > 0) {
                        state.addElementToLookup(hostName);

                        // Make sure state is marked as dirty since we have
                        // added a host
                        state.setIsDirty(true);

                        hostCount++;
                    }
                }

                if (log.isInfoEnabled())
                    log.info("StateStatistics.updateHostGroupState found ["
                            + hostCount + "] host for host group, ."
                            + hostGroupName);
            }
        } catch (Exception e) {
            log.error("updateHostGroupState() failed. Error: " + e);
        }
    }


    /**
     * Adds or updates the StatisticGroupState for the identified service group.
     *
     * @param serviceGroupName
     */
    private void updateServiceGroupState(String serviceGroupName) {
        if (serviceGroupName == null || serviceGroupName.length() == 0) {
            log
                    .warn("updateServiceGroupState() - Invalid null / empty host group name parameter.");
            return;
        }

        try {
            // Query list of Service Groups and Services. Each item returned is an
            // Object[] of two String elements. Note, host name may be
            // null if there are no hosts related to the host group
            List l = (List) foundationDAO
                    .sqlQuery(
                            "select ss.ServiceDescription from " +
                                    "ServiceStatus ss, CategoryEntity ce, Category c, EntityType etc, EntityType etce where " +
                                    "ce.EntityTypeId=etce.EntityTypeId and " +
                                    "etce.Name='SERVICE_STATUS' and " +
                                    "ce.ObjectID=ss.ServiceStatusID and " +
                                    "ce.CategoryID=c.CategoryID and " +
                                    "c.EntityTypeId=etc.EntityTypeId and " +
                                    "etc.Name='SERVICE_GROUP' and " +
                                    "c.Name = ?",
                            serviceGroupName);

            if (l == null || l.size() == 0) {
                log.warn(WARN_NO_SERVICEGROUP_DEFINED);
            } else {
                StatisticGroupState state = new StatisticGroupState();
                String serviceDesc = null;
                int serviceCount = 0;

                // Initially, make sure empty collection is not included in
                // lookup unless there are hosts
                state.setIsDirty(false);

                // Add / Replace State in service Group Collection
                SERVICEGROUPS.put(serviceGroupName, state);

                Iterator it = l.iterator();
                while (it.hasNext()) {
                    serviceDesc = (String) it.next();

                    if (serviceDesc != null && serviceDesc.length() > 0) {
                        state.addElementToLookup(serviceDesc);

                        // Make sure state is marked as dirty since we have
                        // added a host
                        state.setIsDirty(true);

                        serviceCount++;
                    }
                }

                if (log.isInfoEnabled())
                    log.info("StateStatistics.updateServiceGroupState found ["
                            + serviceCount + "] service for service group, ."
                            + serviceGroupName);
            }
        } catch (Exception e) {
            log.error("updateServiceGroupState() failed. Error: " + e);
        }
    }

    /*
	 * Nagiso properties lookup
	 */
    private Collection<NagiosStatisticProperty> getNagiosStatisticsForHostGroup(
            final String hgNameKey, List<String> listOfNagiosProperties)
            throws BusinessServiceException {
        if (hgNameKey == null || hgNameKey.length() == 0) {
            throw new IllegalArgumentException(
                    "Invalid null / empty host group name parameter.");
        }

        if (listOfNagiosProperties == null
                || listOfNagiosProperties.size() == 0) {
            log
                    .warn("getNagiosStatisticsForHostGroup - No nagios properties identified for statistic calculation.");
            return new ArrayList<NagiosStatisticProperty>(); // Return empty
            // collection
            // since there
            // are no
            // properties
            // requested.
        }

        // Create statistic list based on requested nagios properties
        Hashtable<String, NagiosStatisticProperty> htNagiosStatistics = new Hashtable<String, NagiosStatisticProperty>(
                10);

        Iterator<String> it = listOfNagiosProperties.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (token != null && token.length() > 0)
                htNagiosStatistics.put(token, new NagiosStatisticProperty(
                        token, 0, 0, 0, 0));
        }

        try {
            final ApplicationType appType = metadataService
                    .getApplicationTypeByName(APP_TYPE_NAGIOS);

            long begin = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForHostGroup() - Calculating Nagios Statistis For Host Group, "
                                + hgNameKey);

            // Query Host Statistics

            /**
             * Note: We are querying all integer and boolean property
             * statistics, but only returning the requested nagios statistics.
             * The query is fast enough so it should not be a problem, but we
             * need to identify whether we really need to filter the statistics
             * that are calculated or just leave it up to the client to decide
             * which ones to use, especially since the calculations are quick.
             */
            Object[] parameters = new Object[]{
                    appType.getApplicationTypeId(), hgNameKey};

            List l = foundationDAO.sqlQuery(SELECT_HOST_STATUS_STATISTICS,
                    parameters);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No HostGroup Status for HostGroup [" + hgNameKey
                            + "] and ApplicationType NAGIOS found");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(true,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setHostStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setHostStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            // Query Service Statistics
            l = foundationDAO.sqlQuery(SELECT_SERVICE_STATUS_STATISTICS,
                    parameters);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No ServiceStatus for HostGroup [" + hgNameKey
                            + "] and ApplicationType NAGIOS found");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(false,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setServiceStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setServiceStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForHostGroup() - Finished calculating Nagios Statistis For Host Group, "
                                + hgNameKey
                                + ", Calculations took (ms): "
                                + (System.currentTimeMillis() - begin));

            return htNagiosStatistics.values();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Error in getNagiosStatisticsForHostGroup() for hostgroup, "
                            + hgNameKey, e);
        }
    }

    /*
	 * Gets the Nagios statistics for the service group.
	 */
    private Collection<NagiosStatisticProperty> getNagiosStatisticsForServiceGroup(
            final String sgNameKey, List<String> listOfNagiosProperties)
            throws BusinessServiceException {
        if (sgNameKey == null || sgNameKey.length() == 0) {
            throw new IllegalArgumentException(
                    "Invalid null / empty service group name parameter.");
        }

        if (listOfNagiosProperties == null
                || listOfNagiosProperties.size() == 0) {
            log
                    .warn("getNagiosStatisticsForServiceGroup - No nagios properties identified for statistic calculation.");
            return new ArrayList<NagiosStatisticProperty>(); // Return empty
            // collection
            // since there
            // are no
            // properties
            // requested.
        }

        // Create statistic list based on requested nagios properties
        Hashtable<String, NagiosStatisticProperty> htNagiosStatistics = new Hashtable<String, NagiosStatisticProperty>(
                10);

        Iterator<String> it = listOfNagiosProperties.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (token != null && token.length() > 0)
                htNagiosStatistics.put(token, new NagiosStatisticProperty(
                        token, 0, 0, 0, 0));
        }

        try {
            final ApplicationType appType = metadataService
                    .getApplicationTypeByName(APP_TYPE_NAGIOS);

            long begin = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForServiceGroup() - Calculating Nagios Statistis For Service Group, "
                                + sgNameKey);

            /**
             * Note: We are querying all integer and boolean property
             * statistics, but only returning the requested nagios statistics.
             * The query is fast enough so it should not be a problem, but we
             * need to identify whether we really need to filter the statistics
             * that are calculated or just leave it up to the client to decide
             * which ones to use, especially since the calculations are quick.
             */
            Object[] parameters = new Object[]{
                    appType.getApplicationTypeId(), sgNameKey};

            // Query Service Statistics
            List l = foundationDAO.sqlQuery(SELECT_SERVICE_GROUP_STATS,
                    parameters);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No ServiceStatus for ServiceGroup [" + sgNameKey
                            + "] and ApplicationType NAGIOS found");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(false,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setServiceStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setServiceStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForServiceGroup() - Finished calculating Nagios Statistis For ServiceGroup, "
                                + sgNameKey
                                + ", Calculations took (ms): "
                                + (System.currentTimeMillis() - begin));

            return htNagiosStatistics.values();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Error in getNagiosStatisticsForServiceGroup() for servicegroup, "
                            + sgNameKey, e);
        }
    }

    /* Calculate the host statistics -- Services and Host */
    private Collection<NagiosStatisticProperty> getNagiosStatisticsForHost(
            final String hostName, List<String> listOfNagiosProperties)
            throws BusinessServiceException {
        if (hostName == null || hostName.length() == 0) {
            throw new IllegalArgumentException(
                    "Invalid null / empty host name parameter.");
        }

        if (listOfNagiosProperties == null
                || listOfNagiosProperties.size() == 0) {
            log
                    .warn("getNagiosStatisticsForHostGroup - No nagios properties identified for statistic calculation.");
            return new ArrayList<NagiosStatisticProperty>(); // Return empty
            // collection
            // since there
            // are no
            // properties
            // requested.
        }

        // Create statistic list based on requested nagios properties
        Hashtable<String, NagiosStatisticProperty> htNagiosStatistics = new Hashtable<String, NagiosStatisticProperty>(
                10);

        Iterator<String> it = listOfNagiosProperties.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (token != null && token.length() > 0)
                htNagiosStatistics.put(token, new NagiosStatisticProperty(
                        token, 0, 0, 0, 0));
        }

        try {
            long begin = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForHost() - Calculating Nagios Statistis For Host , "
                                + hostName);

            // Query Host Statistics

            /**
             * Note: We are querying all integer and boolean property
             * statistics, but only returning the requested nagios statistics.
             * The query is fast enough so it should not be a problem, but we
             * need to identify whether we really need to filter the statistics
             * that are calculated or just leave it up to the client to decide
             * which ones to use, especially since the calculations are quick.
             */
            Object[] parameters = new Object[]{hostName};

            List l = foundationDAO.sqlQuery(
                    SELECT_HOST_LEVEL_STATUS_STATISTICS, parameters);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No Host Status for Host [" + hostName
                            + "] and ApplicationType NAGIOS found");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(true,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setHostStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setHostStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            // Query Service Statistics
            l = foundationDAO.sqlQuery(SELECT_HOST_SERVICE_STATUS_STATISTICS,
                    parameters);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No ServiceStatus for Host [" + hostName
                            + "] and ApplicationType NAGIOS found");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(false,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setServiceStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setServiceStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            if (log.isInfoEnabled())
                log
                        .info("getNagiosStatisticsForHost() - Finished calculating Nagios Statistis For Host, "
                                + hostName
                                + ", Calculations took (ms): "
                                + (System.currentTimeMillis() - begin));

            return htNagiosStatistics.values();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Error in getNagiosStatisticsForHost() for Host: "
                            + hostName, e);
        }
    }

    /* Calculate the host statistics -- Services and Host */
    private Collection<NagiosStatisticProperty> getApplicationStatisticsTotals(
            List<String> listOfNagiosProperties)
            throws BusinessServiceException {

        if (listOfNagiosProperties == null
                || listOfNagiosProperties.size() == 0) {
            log
                    .warn("getNagiosStatisticsForHostGroup - No nagios properties identified for statistic calculation.");
            return new ArrayList<NagiosStatisticProperty>(); // Return empty
            // collection
            // since there
            // are no
            // properties
            // requested.
        }

        // Create statistic list based on requested nagios properties
        Hashtable<String, NagiosStatisticProperty> htNagiosStatistics = new Hashtable<String, NagiosStatisticProperty>(
                10);

        Iterator<String> it = listOfNagiosProperties.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (token != null && token.length() > 0)
                htNagiosStatistics.put(token, new NagiosStatisticProperty(
                        token, 0, 0, 0, 0));
        }

        try {
            long begin = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log
                        .info("getApplicationStatisticsTotals() - Calculating Nagios Statistis Totals");

            // Query Host Statistics

            /**
             * Note: We are querying all integer and boolean property
             * statistics, but only returning the requested nagios statistics.
             * The query is fast enough so it should not be a problem, but we
             * need to identify whether we really need to filter the statistics
             * that are calculated or just leave it up to the client to decide
             * which ones to use, especially since the calculations are quick.
             */
            List l = foundationDAO.sqlQuery(SELECT_HOST_TOTALS_STATISTICS);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No Application Statistic Totals for Host");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(true,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setHostStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setHostStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            // Query Service Statistics
            l = foundationDAO.sqlQuery(SELECT_SERVICE_TOTALS_STATISTICS);
            if (l == null || l.size() == 0) {
                if (log.isInfoEnabled())
                    log.info("No Application Statistic Totals for Services");
            } else {
                NagiosStatisticProperty statisticProperty = null;
                String nagiosProperty = null;

                Iterator itStats = l.iterator();
                while (itStats.hasNext()) {
                    Object[] vals = (Object[]) itStats.next();

                    // Get Nagios Property Name from map based on the internal
                    // property name
                    nagiosProperty = this.nagiosPropertyMap(false,
                            (String) vals[0]);

                    statisticProperty = htNagiosStatistics.get(nagiosProperty);
                    if (statisticProperty != null) {
                        statisticProperty
                                .setServiceStatisticEnabled(((BigInteger) vals[1])
                                        .longValue());
                        statisticProperty
                                .setServiceStatisticDisabled(((BigInteger) vals[2])
                                        .longValue());
                    }
                }
            }

            if (log.isInfoEnabled())
                log
                        .info("getApplicationStatisticsTotals() - Finished calculating Nagios Statistis totals, Calculations took (ms): "
                                + (System.currentTimeMillis() - begin));

            return htNagiosStatistics.values();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Error in getApplicationStatisticsTotals(). Error: ", e);
        }
    }

    private long getHostStatusCountForHostGroup(final String hostgroup,
                                                final String monStatus) throws BusinessServiceException {
        final MonitorStatus monStatusObj = this.metadataService
                .getMonitorStatusByName(monStatus);

        if (monStatusObj == null) {
            throw new BusinessServiceException("Unknown Monitor Type ["
                    + monStatus + "] can't retrive count.");
        }

        try {
            long begin = System.currentTimeMillis();

            List result = foundationDAO.query(
                    SELECT_HOST_STATUS_COUNT_BY_HOSTGROUP, new Object[]{
                            hostgroup, monStatusObj.getMonitorStatusId()});

            if (log.isInfoEnabled()) {
                // Profiling of Statistic Count
                log.info("HostStatus Count for HostGroup '" + hostgroup
                        + "' and MonitorStatus [" + monStatus + "] took "
                        + (System.currentTimeMillis() - begin) + " ms");
            }

            return ((Long) result.get(0)).longValue();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Unable to get host status counts  for HostGroup:  "
                            + hostgroup + ", MonitorStatus: " + monStatus, e);
        }
    }

    private long getServiceStatusCountForHostGroup(final String hostgroup,
                                                   final String monStatus) throws BusinessServiceException {
        final MonitorStatus monStatusObj = this.metadataService
                .getMonitorStatusByName(monStatus);

        if (monStatusObj == null) {
            throw new BusinessServiceException("Unknown Monitor Type ["
                    + monStatus + "] can't retrive count.");
        }

        try {
            long begin = System.currentTimeMillis();

            List result = foundationDAO.query(
                    SELECT_SERVICE_STATUS_COUNT_BY_HOSTGROUP, new Object[]{
                            hostgroup, monStatusObj.getMonitorStatusId()});

            if (log.isInfoEnabled()) {
                // Profiling of Statistic Count
                log.info("ServiceStatus Count for HostGroup '" + hostgroup
                        + "' and MonitorStatus [" + monStatus + "] took "
                        + (System.currentTimeMillis() - begin) + " ms");
            }

            return ((Long) result.get(0)).longValue();

        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Unable to get Services for HostGroup with name: '"
                            + hostgroup + "'", e);
        }
    }

    // Overall statistics count
    private long getHostStatusForAll(String monitorStatus)
            throws BusinessServiceException {
        final MonitorStatus monStatusObj = this.metadataService
                .getMonitorStatusByName(monitorStatus);

        if (monStatusObj == null) {
            throw new BusinessServiceException("Unknown Monitor Type ["
                    + monitorStatus + "] can't retrive count.");
        }

        try {
            long begin = System.currentTimeMillis();

            List result = foundationDAO.query(SELECT_HOST_STATUS_COUNT,
                    monStatusObj.getMonitorStatusId());

            long count = ((Long) result.get(0)).longValue();

            if (log.isInfoEnabled()) {
                log.info("Retrieved " + count + " Hosts  with Status ["
                        + monitorStatus + "]");

                // Profiling of Statistic Count
                log.info("HostStatus Count for MonitorStatus [" + monitorStatus
                        + "] took " + (System.currentTimeMillis() - begin)
                        + " ms");
            }

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Unable to get host status counts for  MonitorStatus: "
                            + monitorStatus, e);
        }
    }

    private long getServiceStatusForAll(String monitorStatus)
            throws BusinessServiceException {
        final MonitorStatus monStatusObj = this.metadataService
                .getMonitorStatusByName(monitorStatus);

        if (monStatusObj == null) {
            throw new BusinessServiceException("Unknown Monitor Type ["
                    + monitorStatus + "] can't retrive count.");
        }

        try {
            long begin = System.currentTimeMillis();

            List result = foundationDAO.query(SELECT_SERVICE_STATUS_COUNT,
                    monStatusObj.getMonitorStatusId());

            long count = ((Long) result.get(0)).longValue();

            if (log.isInfoEnabled()) {
                log.info("Retrieved " + count + " Services  with Status ["
                        + monitorStatus + "]");

                // Profiling of Statistic Count
                log.info("ServiceStatus  Count for MonitorStatus ["
                        + monitorStatus + "] took "
                        + (System.currentTimeMillis() - begin) + " ms");
            }

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Unable to get service status counts for  MonitorStatus: "
                            + monitorStatus, e);
        }
    }

    private String nagiosPropertyMap(boolean isHost, String internalPropertyName) {
		/* Input validation */
        if (internalPropertyName == null)
            return EMPTY_STRING;

        // local members
        String mappedProperty = null;

        // Lookup
        if (isHost) {
            mappedProperty = NAGIOS_HOST_PROPERTY_MAP.get(internalPropertyName);
            if (mappedProperty == null)
                mappedProperty = internalPropertyName;
        } else {
            mappedProperty = NAGIOS_SERVICE_PROPERTY_MAP
                    .get(internalPropertyName);
            if (mappedProperty == null)
                mappedProperty = internalPropertyName;
        }

        return mappedProperty;
    }

    /**
     * Returns count of hosts
     *
     * @return
     */
    private long getHostsCount() {
        try {
            List result = foundationDAO.query(SELECT_HOST_COUNT);

            long count = ((Long) result.get(0)).longValue();

            if (log.isDebugEnabled())
                log.debug("Retrieved all " + count + " Hosts");

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException("Unable to get host count", e);
        }
    }

    private long getServicesCount() {
        try {
            List result = foundationDAO.query(SELECT_SERVICE_COUNT);

            long count = ((Long) result.get(0)).longValue();

            if (log.isDebugEnabled())
                log.debug("Retrieved all " + count + " Services");

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException("Unable to get services count",
                    e);
        }
    }

    private long getServiceGroupServiceCount(String sgName) {
        try {
            Object[] parameters = new Object[]{sgName};
            List result = foundationDAO.sqlQuery(
                    SELECT_SERVICEGROUP_SERVICE_COUNT, parameters);

            long count = ((BigInteger) result.get(0)).longValue();

            if (log.isDebugEnabled())
                log.debug("Retrieved all " + count + " Services");

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException("Unable to get services count",
                    e);
        }
    }

    private long getServiceStatusCountForServiceGroup(String sgName,
                                                      String status) {
        try {
            Object[] parameters = new Object[]{sgName, status};
            List result = foundationDAO.sqlQuery(
                    SELECT_SERVICEGROUP_STATUS_COUNT, parameters);

            long count = ((BigInteger) result.get(0)).longValue();

            if (log.isDebugEnabled())
                log.debug("Retrieved all " + count + " Services");

            return count;
        } catch (Exception e) {
            throw new BusinessServiceException("Unable to get services count",
                    e);
        }
    }

    private long getStateCount(final String query, final int statusCheckID)
            throws BusinessServiceException {

        try {
            List result = (List) foundationDAO.query(query, statusCheckID);

            if (result == null || result.size() == 0) {
                log.warn("No count returned for query - " + query);
                return 0;
            }

            if (log.isInfoEnabled())
                log.info("Retrieved:" + ((Long) result.get(0))
                        + " LogMessages for query - " + query + " - ID: "
                        + statusCheckID);

            return ((Long) result.get(0)).longValue();
        } catch (Exception e) {
            throw new BusinessServiceException(
                    "Unable to get LogMessages for query - " + query, e);
        }
    }

    private FoundationQueryList getEventStatistics(
            Map<String, Object> parameters) {
        // Log Message Statistic Parameters
        String appType = null;
        String hostName = null;
        String hostGroupName = null;
        Date startDate = null;
        Date endDate = null;
        String statisticType = STAT_TYPE_MONITOR_STATUS;

        if (parameters != null && parameters.size() > 0) {
            appType = (String) parameters
                    .get(LogMessageStatistic.PROP_APPLICATION_TYPE_NAME
                            .getName());
            hostName = (String) parameters
                    .get(LogMessageStatistic.PROP_HOST_NAME.getName());
            hostGroupName = (String) parameters
                    .get(LogMessageStatistic.PROP_HOST_GROUP_NAME.getName());
            startDate = (Date) parameters
                    .get(LogMessageStatistic.PROP_START_DATE.getName());
            endDate = (Date) parameters.get(LogMessageStatistic.PROP_END_DATE
                    .getName());

            // Required parameter - defaults to STAT_TYPE_MONITOR_STATUS
            String statType = (String) parameters
                    .get(LogMessageStatistic.PROP_STATISTIC_TYPE.getName());
            if (statType != null && statType.length() > 0)
                statisticType = statType;
        }

        Collection<StatisticProperty> stats = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDateSQLString = null;
        String endDateSQLString = null;

        if (startDate != null)
            startDateSQLString = sdf.format(startDate);

        if (endDate != null)
            endDateSQLString = sdf.format(endDate);

        // NOTE: Since host name is more restrictive than host group we check if
        // host name is provided
        // If it is not then we use the getEventStatisticsByHostGroupName
        // method.
        if (hostName != null && hostName.length() > 0) {
            stats = getEventStatisticsByHostName(appType, hostName,
                    startDateSQLString, endDateSQLString, statisticType);
        } else {
            stats = getEventStatisticsByHostGroupName(appType, hostGroupName,
                    startDateSQLString, endDateSQLString, statisticType);
        }

        // Instantiate the entity type that will hold the statistic information
        if (stats == null || stats.size() == 0)
            return new FoundationQueryList(
                    new ArrayList<LogMessageStatistic>(0), 0);

        List<LogMessageStatistic> entityList = new ArrayList<LogMessageStatistic>(
                stats.size());
        StatisticProperty statProperty = null;
        Iterator<StatisticProperty> it = stats.iterator();
        while (it.hasNext()) {
            statProperty = it.next();

            entityList.add(new LogMessageStatistic(statProperty));
        }

        return new FoundationQueryList(entityList, entityList.size());
    }

    private FoundationQueryList getHostStatistics(Map<String, Object> parameters) {
        // Host Statistic Parameters
        String appType = null;
        String hostGroupName = null;

        if (parameters != null && parameters.size() > 0) {
            // TODO: Application type is currently ignored
            // appType =
            // (String)parameters.get(HostStatistic.PROP_APPLICATION_TYPE_NAME.getName());
            hostGroupName = (String) parameters
                    .get(HostStatistic.PROP_HOST_GROUP_NAME.getName());
        }

        Collection<StatisticProperty> stats = null;
        List<HostStatistic> entityList = new ArrayList<HostStatistic>(5);

        // If host group name is not provided we return host statistic totals
        if (hostGroupName == null || hostGroupName.length() == 0) {
            StateStatistics stateStatistics = getHostStatisticTotals();
            if (stateStatistics != null)
                stats = stateStatistics.getStatisticProperties();

            if (stats == null || stats.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StatisticProperty statProperty = null;
            Iterator<StatisticProperty> it = stats.iterator();
            while (it.hasNext()) {
                statProperty = it.next();

                entityList.add(new HostStatistic(stateStatistics
                        .getHostGroupName(), statProperty));
            }

        } // If HostGroup name equals ALL_HOSTGROUP (_ALL_) then we return host
        // stats for each host group
        else if (hostGroupName.equalsIgnoreCase(ALL_HOSTGROUP)) {
            Collection<StateStatistics> colStateStatistics = getAllHostStatistics();
            if (colStateStatistics == null || colStateStatistics.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StateStatistics stateStatistics = null;
            Iterator<StateStatistics> it = colStateStatistics.iterator();
            while (it.hasNext()) {
                stateStatistics = it.next();
                if (stateStatistics == null)
                    continue;

                stats = stateStatistics.getStatisticProperties();
                if (stats == null || stats.size() == 0)
                    continue;

                StatisticProperty statProperty = null;
                Iterator<StatisticProperty> itStats = stats.iterator();
                while (itStats.hasNext()) {
                    statProperty = itStats.next();

                    entityList.add(new HostStatistic(stateStatistics
                            .getHostGroupName(), statProperty));
                }
            }

        } // Specific HostGroup totals
        else {
            StateStatistics stateStatistics = getHostStatisticsByHostGroupName(hostGroupName);

            if (stateStatistics != null)
                stats = stateStatistics.getStatisticProperties();

            if (stats == null || stats.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StatisticProperty statProperty = null;
            Iterator<StatisticProperty> it = stats.iterator();
            while (it.hasNext()) {
                statProperty = it.next();

                entityList.add(new HostStatistic(stateStatistics
                        .getHostGroupName(), statProperty));
            }
        }

        return new FoundationQueryList(entityList, entityList.size());
    }

    private FoundationQueryList getServiceStatistics(
            Map<String, Object> parameters) {
        // Host Statistic Parameters
        String appType = null;
        String hostGroupName = null;
        String hostName = null;

        if (parameters != null && parameters.size() > 0) {
            // TODO: Application type is currently ignored
            // appType =
            // (String)parameters.get(ServiceStatistic.PROP_APPLICATION_TYPE_NAME.getName());
            hostGroupName = (String) parameters
                    .get(ServiceStatistic.PROP_HOST_GROUP_NAME.getName());
            hostName = (String) parameters.get(ServiceStatistic.PROP_HOST_NAME
                    .getName());
        }

        Collection<StatisticProperty> stats = null;
        List<ServiceStatistic> entityList = new ArrayList<ServiceStatistic>(5);

        // If HostGroup name or Host Name equals ALL_HOSTGROUP (_ALL_) then we
        // return service stats for each host group
        if ((hostGroupName != null && hostGroupName
                .equalsIgnoreCase(ALL_HOSTGROUP))
                || (hostName != null && hostName
                .equalsIgnoreCase(ALL_HOSTGROUP))) {
            Collection<StateStatistics> colStateStatistics = getAllServiceStatistics();
            if (colStateStatistics == null || colStateStatistics.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StateStatistics stateStatistics = null;
            Iterator<StateStatistics> it = colStateStatistics.iterator();
            while (it.hasNext()) {
                stateStatistics = it.next();
                if (stateStatistics == null)
                    continue;

                stats = stateStatistics.getStatisticProperties();
                if (stats == null || stats.size() == 0)
                    continue;

                StatisticProperty statProperty = null;
                Iterator<StatisticProperty> itStats = stats.iterator();
                while (itStats.hasNext()) {
                    statProperty = itStats.next();

                    entityList.add(new ServiceStatistic(stateStatistics
                            .getHostGroupName(), statProperty));
                }
            }

        } // Specific HostGroup service totals
        else if (hostGroupName != null && hostGroupName.length() > 0) {
            StateStatistics stateStatistics = getServiceStatisticsByHostGroupName(hostGroupName);

            if (stateStatistics != null)
                stats = stateStatistics.getStatisticProperties();

            if (stats == null || stats.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StatisticProperty statProperty = null;
            Iterator<StatisticProperty> it = stats.iterator();
            while (it.hasNext()) {
                statProperty = it.next();

                entityList.add(new ServiceStatistic(stateStatistics
                        .getHostGroupName(), statProperty));
            }
        } // Specific Host Service Totals
        else if (hostName != null && hostName.length() > 0) {
            StateStatistics stateStatistics = getServiceStatisticByHostName(hostName);

            if (stateStatistics != null)
                stats = stateStatistics.getStatisticProperties();

            if (stats == null || stats.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StatisticProperty statProperty = null;
            Iterator<StatisticProperty> it = stats.iterator();
            while (it.hasNext()) {
                statProperty = it.next();

                entityList.add(new ServiceStatistic(stateStatistics
                        .getHostGroupName(), statProperty));
            }
        } // Host Group and Host Name are not provided so we return totals
        else {
            StateStatistics stateStatistics = getServiceStatisticTotals();
            if (stateStatistics != null)
                stats = stateStatistics.getStatisticProperties();

            if (stats == null || stats.size() == 0)
                return new FoundationQueryList(entityList, 0);

            StatisticProperty statProperty = null;
            Iterator<StatisticProperty> it = stats.iterator();
            while (it.hasNext()) {
                statProperty = it.next();

                entityList.add(new ServiceStatistic(stateStatistics
                        .getHostGroupName(), statProperty));
            }
        }

        return new FoundationQueryList(entityList, entityList.size());
    }

    private FoundationQueryList getHostStateTransitions(
            Map<String, Object> parameters) {
        if (parameters == null || parameters.size() == 0)
            throw new BusinessServiceException(
                    "Missing required parameters - Host and ServiceDescription.");

        // Host State Transition Parameters
        String hostName = (String) parameters
                .get(HostStateTransition.PROP_HOST_NAME.getName());

        if (hostName == null || hostName.length() == 0)
            throw new BusinessServiceException("Missing required parameters - "
                    + HostStateTransition.PROP_HOST_NAME.getName());

        Date startDate = (Date) parameters
                .get(LogMessageStatistic.PROP_START_DATE.getName());
        Date endDate = (Date) parameters.get(LogMessageStatistic.PROP_END_DATE
                .getName());
        DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
        String strStartDate = date.format(startDate);
        String strEndDate = date.format(endDate);
        List<StateTransition> stateTransitions = logMessageService
                .getHostStateTransitions(hostName, strStartDate, strEndDate);

        return new FoundationQueryList(stateTransitions,
                (stateTransitions == null) ? 0 : stateTransitions.size());
    }

    private FoundationQueryList getServiceStateTransitions(
            Map<String, Object> parameters) throws BusinessServiceException {
        if (parameters == null || parameters.size() == 0)
            throw new BusinessServiceException(
                    "Missing required parameters - Host and ServiceDescription.");

        // Service State Transition Parameters
        String hostName = (String) parameters
                .get(ServiceStateTransition.PROP_HOST_NAME.getName());

        if (hostName == null || hostName.length() == 0)
            throw new BusinessServiceException("Missing required parameters - "
                    + ServiceStateTransition.PROP_HOST_NAME.getName());

        String serviceName = (String) parameters
                .get(ServiceStateTransition.PROP_SERVICE_NAME.getName());
        Date startDate = (Date) parameters
                .get(LogMessageStatistic.PROP_START_DATE.getName());
        Date endDate = (Date) parameters.get(LogMessageStatistic.PROP_END_DATE
                .getName());
        DateFormat date = new SimpleDateFormat(DATE_FORMAT_US);
        String strStartDate = date.format(startDate);
        String strEndDate = date.format(endDate);
        // NOTE: If service name is not provided we will be returning all
        // service state transitions for the host
        List<StateTransition> stateTransitions = logMessageService
                .getServiceStateTransitions(hostName, serviceName,
                        strStartDate, strEndDate);

        return new FoundationQueryList(stateTransitions,
                (stateTransitions == null) ? 0 : stateTransitions.size());
    }

    /**
     * Thread to recalculate the statistics
     */

    class StatisticCalculationThread extends Thread {

        private StatisticsServiceImpl service = null;

        private boolean isRunning = true;
        private boolean isCalculating = false;

        // Default constructor
        public StatisticCalculationThread(StatisticsServiceImpl service) {
            this.service = service;
        }

        public void run() {
            // First time update the HostGroup Collections
            boolean bHostGroupSuccess = service.generateHostGroupCollections();
            boolean bHostSuccess = service.generateHostCollection();

            log.debug("Statistics GenerateGenerateHostGroupCollection done.");
            boolean bServiceGroupSuccess = service
                    .generateServiceGroupCollections();
            log.debug("Statistics GenerateGenerateHostGroupCollection done.");

            while (isRunning == true) {
                CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "run");
                // We make sure that we have successfully retrieved host groups
                // and hosts
                // the first time. Otherwise, we continue to try to retrieve
                // them.
                // Otherwise, we could be returning invalid statistics since
                // there would
                // be no hostgroups and / or hosts. One situation where this
                // scenario
                // can occur is if Foundation is started before MySQL.
                if (bHostGroupSuccess == false) {
                    bHostGroupSuccess = service.generateHostGroupCollections();
                }

                if (bHostSuccess == false) {
                    bHostSuccess = service.generateHostCollection();
                }

                if (bServiceGroupSuccess == false) {
                    bServiceGroupSuccess = service
                            .generateServiceGroupCollections();
                }

				/*
				 * Make sure that it was not interruppted or stopped by a
				 * shutdown command.
				 */
                if (isRunning == true && SERVICE_STATUS_LIST != null) {
                    if (log.isInfoEnabled())
                        log.info("Recalculating statistics");

                    isCalculating = true;
                    try {
						/* Start of recalc */
                        long calcTime = System.currentTimeMillis();
                        service.updateHostGroupStatistics(HOST_STATUS_LIST,
                                SERVICE_STATUS_LIST, NAGIOS_PROPERTY_LIST);
                        // Now update ServiceGroupStatistics
                        service.updateServiceGroupStatistics(
                                SERVICE_STATUS_LIST, NAGIOS_PROPERTY_LIST);

                        // Now populate nagios performance data into the topic.
                        service.populateNagiosPerformanceInfo();

                        // Now publish all the entities
                        service.publishEntities();

                        // Now populate open event stats
                        service.populateEventStatisticsForHostgroups();

						/*
						 * Recalculation done. Adjust time for next
						 * recalculation
						 */

                        calcTime = System.currentTimeMillis() - calcTime;
                        if (log.isInfoEnabled()) {
                            log
                                    .info("updateHostGroupStatistics() and updateServiceGroupStatistics()- Time to perform update of statistic calculations (ms):  "
                                            + calcTime);
                        }

                        // Multiple the calculation time by a factor to insure
                        // the system is not
                        // always performing calculations
                        calcTime *= 4;

                        if (calcTime < MINIMAL_RECALC_INTERVAL) {
                            calcTime = MINIMAL_RECALC_INTERVAL;
                        } else if (calcTime > MAX_RECALC_INTERVAL) {
                            // If the sleep time is greater than MAX_RECALC_INTERVAL, sleep only for MAX_RECALC_INTERVAL.
                            calcTime = MAX_RECALC_INTERVAL;
                        }

                        RECALCULATION_TIME.set(calcTime);

                        if (log.isInfoEnabled()) {
                            log
                                    .info("Interval before next statistics update (ms):  "
                                            + calcTime);
                        }

                    }
                    // NOTE: We suppress the exception and continually try to
                    // calculate statistics just in case the
                    // the server recovers (e.g MySQL restarts).
                    catch (Exception e) {
                        log
                                .error(
                                        "StatisticCalculationThread.run() - Error updating host group/Service group statistics",
                                        e);

                        // Force recalculation of all host groups and hosts
                        bHostGroupSuccess = false;
                        bHostSuccess = false;
                        bServiceGroupSuccess = false;
                    }
                    isCalculating = false;
                }

                stopMetricsTimer(timer);

                // Sleep for the specified amount before doing the next
                // calculation
                synchronized (this) {
                    if (isRunning) {
                        try {
                            wait(RECALCULATION_TIME.get());
                        } catch (InterruptedException ie) {
                        }
                    }
                }
            }

            if (log.isInfoEnabled())
                log.info("Statistics Calculation Thread exit.");
        }

        public synchronized void stopThread() {
            log.debug("stopThread() called. Statistics Calculation Thread sttopping now.");
            isRunning = false;
            notifyAll();
        }

        public boolean isCalculating() {
            return isCalculating;
        }
    }

    /**
     * Populates the nagios performance data to the toipc server
     */
    protected void populateNagiosPerformanceInfo() {
        logger.debug("Populating nagios performance info");
        CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "populateNagiosPerformanceInfo");
        NagiosPerformanceDataService perfData = new NagiosPerformanceDataServiceImpl();
        String perfXML = perfData.fetchPerformanceData();
        if (perfXML != null) {
            logger.debug("New Message : " + perfXML);
            try {
                CollageFactory beanFactory = CollageFactory.getInstance();
                PerformanceDataPublisher publisher = beanFactory.getPerformanceDataPublisher();
                if (publisher != null) {
                    publisher.publish(perfXML);
                } else {
                    log.warn("PerformanceDataPublisher not configured, JMS publishing disabled.");
                }
            } catch (NoSuchBeanDefinitionException nsbde) {
                log.warn("PerformanceDataPublisher not configured, JMS publishing disabled.");
            }
        } // end if
        stopMetricsTimer(timer);
    }

    /**
     * Publishes the entity from the distinct hashmap
     */
    private void publishEntities() {
        CollageTimer timer = startMetricsTimer("StatisticsServiceImpl", "publishEntities");
        StringBuffer combinedXML = new StringBuffer();
        try {
            CollageFactory beanFactory = CollageFactory.getInstance();
            EntityPublisher entityPublisher = beanFactory.getEntityPublisher();
            if (entityPublisher != null) {
                int count = 0;
                for (Iterator i = entityPublisher.getDistinctEntityMap().keySet().iterator(); i.hasNext(); ) {
                    String key = (String) i.next();
                    String value = (String) entityPublisher.getDistinctEntityMap().get(key);
                    StringBuffer sb = new StringBuffer();
                    sb.append("<ENTITY ");
                    sb.append("TYPE=");
                    sb.append("\"");
                    sb.append(key);
                    sb.append("\" ");
                    sb.append("TEXT=");
                    sb.append("\"");
                    sb.append(value);
                    sb.append("\" />");
                    String XML = sb.toString();
                    combinedXML.append(XML);
                    //beanFactory.getEntityPublisher().publishEntity(XML);
                    count++;
                } // end for
                // Add the combined XML at the end.
                if (count > 0)
                    entityPublisher.publishEntity("<AGGREGATE>" + combinedXML.toString() + "</AGGREGATE>");
                entityPublisher.getDistinctEntityMap().clear();
            } else {
                log.warn("EntityPublisher not configured, JMS publishing disabled.");
            }
        }
        catch (NoSuchBeanDefinitionException NSBDE)  {
            log.warn("EntityPublisher not configured, JMS publishing disabled.");
        }
        stopMetricsTimer(timer);
    }

}