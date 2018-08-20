/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage.biz;

import com.groundwork.collage.biz.model.RTMMCustomGroup;
import com.groundwork.collage.biz.model.RTMMHost;
import com.groundwork.collage.biz.model.RTMMHostGroup;
import com.groundwork.collage.biz.model.RTMMService;
import com.groundwork.collage.biz.model.RTMMServiceGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.StatelessSession;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RTMMServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMServicesImpl extends HibernateDaoSupport implements RTMMServices {

    private static Log log = LogFactory.getLog(RTMMServicesImpl.class);

    private static final long MAX_WAIT = 30000;

    private static final int HOST_QUERY_FETCH_SIZE = 100;
    private static final String HOST_QUERY = "select " +
            "h.hostid as id, " +
            "h.hostname as hostName, " +
            "a.name as appTypeName, " +
            "a.displayname as appTypeDisplayName, " +
            "hs.lastchecktime as lastCheckTime, " +
            "hs.nextchecktime as nextCheckTime, " +
            "ms.name as monitorStatus " +
            "from " +
            "host h " +
            "left outer join applicationtype a on h.applicationtypeid = a.applicationtypeid " +
            "join hoststatus hs on h.hostid = hs.hoststatusid " +
            "join monitorstatus ms on hs.monitorstatusid = ms.monitorstatusid";
    private static final String HOST_QUERY_WITH_IDS_PREDICATE = HOST_QUERY +
            " where h.hostid in ( %s )";

    private static final int HOST_STATUS_PROPERTY_QUERY_FETCH_SIZE = 100;
    private static final String HOST_STATUS_PROPERTY_QUERY = "select " +
            "hsp.hoststatusid as id, " +
            "hsp.valuestring as valueString, " +
            "hsp.valuedate as valueDate, " +
            "hsp.valueboolean as valueBoolean, " +
            "hsp.valueinteger as valueInteger, " +
            "hsp.valuelong as valueLong, " +
            "pt.name as name " +
            "from " +
            "hoststatusproperty hsp " +
            "join propertytype pt on hsp.propertytypeid = pt.propertytypeid " +
            "where " +
            "pt.name in (" +
            "'Alias'," +
            "'isAcknowledged'," +
            "'isProblemAcknowledged'," +
            "'LastStateChange'," +
            "'CurrentAttempt'," +
            "'MaxAttempts'," +
            "'ScheduledDowntimeDepth'," +
            "'LastPluginOutput'" +
            ")";
    private static final String HOST_STATUS_PROPERTY_QUERY_WITH_IDS_PREDICATE = HOST_STATUS_PROPERTY_QUERY +
            " and hsp.hoststatusid in ( %s )";

    private static final int SERVICE_STATUS_QUERY_FETCH_SIZE = 100;
    private static final String SERVICE_STATUS_QUERY = "select " +
            "ss.servicestatusid as id, " +
            "ss.servicedescription as description, " +
            "ss.lastchecktime as lastCheckTime, " +
            "ss.nextchecktime as nextCheckTime, " +
            "ss.laststatechange as lastStateChange, " +
            "ss.hostid as hostId, " +
            "a.name as appTypeName, " +
            "a.displayname as appTypeDisplayName, " +
            "ms.name as monitorStatus " +
            "from " +
            "servicestatus ss " +
            "left outer join applicationtype a on ss.applicationtypeid = a.applicationtypeid " +
            "join monitorstatus ms on ss.monitorstatusid = ms.monitorstatusid";
    private static final String SERVICE_STATUS_QUERY_WITH_IDS_PREDICATE = SERVICE_STATUS_QUERY +
            " where ss.hostid in ( %s )";

    private static final int SERVICE_STATUS_PROPERTY_QUERY_FETCH_SIZE = 100;
    private static final String SERVICE_STATUS_PROPERTY_QUERY = "select " +
            "ssp.servicestatusid as id, " +
            "ssp.valuestring as valueString, " +
            "ssp.valuedate as valueDate, " +
            "ssp.valueboolean as valueBoolean, " +
            "ssp.valueinteger as valueInteger, " +
            "ssp.valuelong as valueLong, " +
            "pt.name as name " +
            "from " +
            "servicestatusproperty ssp " +
            "join propertytype pt on ssp.propertytypeid = pt.propertytypeid " +
            "where " +
            "pt.name in (" +
            "'isProblemAcknowledged'," +
            "'CurrentAttempt'," +
            "'MaxAttempts'," +
            "'ScheduledDowntimeDepth'," +
            "'LastPluginOutput'," +
            "'PerformanceData'" +
            ")";
    private static final String SERVICE_STATUS_PROPERTY_QUERY_WITH_IDS_PREDICATE = "select " +
            "ssp.servicestatusid as id, " +
            "ssp.valuestring as valueString, " +
            "ssp.valuedate as valueDate, " +
            "ssp.valueboolean as valueBoolean, " +
            "ssp.valueinteger as valueInteger, " +
            "ssp.valuelong as valueLong, " +
            "pt.name as name " +
            "from " +
            "servicestatusproperty ssp " +
            "join propertytype pt on ssp.propertytypeid = pt.propertytypeid " +
            "join servicestatus ss on ssp.servicestatusid = ss.servicestatusid " +
            "where " +
            "pt.name in (" +
            "'isProblemAcknowledged'," +
            "'CurrentAttempt'," +
            "'MaxAttempts'," +
            "'ScheduledDowntimeDepth'," +
            "'LastPluginOutput'," +
            "'PerformanceData'" +
            ") and " +
            "ss.hostid in ( %s )";

    private static final int HOST_GROUP_QUERY_FETCH_SIZE = 50;
    private static final String HOST_GROUP_QUERY = "select " +
            "hg.hostgroupid as id, " +
            "hg.name as name, " +
            "hg.alias as alias, " +
            "a.name as appTypeName " +
            "from " +
            "hostgroup hg " +
            "left outer join applicationtype a on hg.applicationtypeid = a.applicationtypeid";
    private static final String HOST_GROUP_QUERY_WITH_IDS_PREDICATE = HOST_GROUP_QUERY +
            " where hg.hostgroupid in ( %s )";

    private static final int HOST_GROUP_COLLECTION_QUERY_FETCH_SIZE = 100;
    private static final String HOST_GROUP_COLLECTION_QUERY = "select " +
            "hgc.hostgroupid as id, " +
            "hgc.hostid as hostId " +
            "from " +
            "hostgroupcollection hgc";
    private static final String HOST_GROUP_COLLECTION_QUERY_WITH_IDS_PREDICATE = HOST_GROUP_COLLECTION_QUERY +
            " where hgc.hostgroupid in ( %s )";

    private static final int SERVICE_GROUP_QUERY_FETCH_SIZE = 50;
    private static final String SERVICE_GROUP_QUERY = "select " +
            "c.categoryid as id, " +
            "c.name as name, " +
            "a.name as appTypeName " +
            "from " +
            "category c " +
            "left outer join applicationtype a on c.applicationtypeid = a.applicationtypeid " +
            "where " +
            "c.root and " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'SERVICE_GROUP')";
    private static final String SERVICE_GROUP_QUERY_WITH_IDS_PREDICATE = SERVICE_GROUP_QUERY +
            " and c.categoryid in ( %s )";

    private static final int SERVICE_GROUP_ENTITY_QUERY_FETCH_SIZE = 100;
    private static final String SERVICE_GROUP_ENTITY_QUERY = "select " +
            "ce.categoryid as id, " +
            "ce.objectid as serviceId " +
            "from " +
            "categoryentity ce " +
            "join category c on ce.categoryid = c.categoryid " +
            "where " +
            "ce.entitytypeid = (select entitytypeid from entitytype where name = 'SERVICE_STATUS') and " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'SERVICE_GROUP')";
    private static final String SERVICE_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE = SERVICE_GROUP_ENTITY_QUERY +
            " and ce.categoryid in ( %s )";

    private static final int CUSTOM_GROUP_QUERY_FETCH_SIZE = 50;
    private static final String CUSTOM_GROUP_QUERY = "select " +
            "c.categoryid as id, " +
            "c.name as name, " +
            "c.root as isRoot " +
            "from " +
            "category c " +
            "where " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'CUSTOM_GROUP')";
    private static final String CUSTOM_GROUP_QUERY_WITH_IDS_PREDICATE = CUSTOM_GROUP_QUERY +
            " and c.categoryid in ( %s )";

    private static final int CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY_FETCH_SIZE = 50;
    private static final String CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY = "select " +
            "ce.categoryid as id, " +
            "ce.objectid as hostGroupId " +
            "from " +
            "categoryentity ce " +
            "join category c on ce.categoryid = c.categoryid " +
            "where " +
            "ce.entitytypeid = (select entitytypeid from entitytype where name = 'HOSTGROUP') and " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'CUSTOM_GROUP')";
    private static final String CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE = CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY +
            " and ce.categoryid in ( %s )";

    private static final int CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY_FETCH_SIZE = 50;
    private static final String CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY = "select " +
            "ce.categoryid as id, " +
            "ce.objectid as serviceGroupId " +
            "from " +
            "categoryentity ce " +
            "join category c on ce.categoryid = c.categoryid " +
            "where " +
            "ce.entitytypeid = (select entitytypeid from entitytype where name = 'SERVICE_GROUP') and " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'CUSTOM_GROUP')";
    private static final String CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE = CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY +
            " and ce.categoryid in ( %s )";

    private static final int CUSTOM_GROUP_HIERARCHY_QUERY_FETCH_SIZE = 100;
    private static final String CUSTOM_GROUP_HIERARCHY_QUERY = "select " +
            "ch.parentid as id, " +
            "ch.categoryid as childId " +
            "from " +
            "categoryhierarchy ch " +
            "join category c on ch.parentid = c.categoryid " +
            "where " +
            "c.entitytypeid = (select entitytypeid from entitytype where name = 'CUSTOM_GROUP')";
    private static final String CUSTOM_GROUP_HIERARCHY_QUERY_WITH_IDS_PREDICATE = CUSTOM_GROUP_HIERARCHY_QUERY +
            " and ch.parentid in ( %s )";

    @Override
    public Collection<RTMMHost> getHosts() {
        return queryHosts(null);
    }

    @Override
    public RTMMHost getHost(int hostId) {
        Collection<RTMMHost> host = queryHosts(new Integer[]{hostId});
        return (((host != null) && (host.size() == 1)) ? host.iterator().next() : null);
    }

    @Override
    public Collection<RTMMHost> getHosts(Integer [] hostIds) {
        return queryHosts(hostIds);
    }

    /**
     * Query hosts or host by id using parallel direct SQL access, returning
     * optimized RTMM host instance(s) with RTMM services. Concurrent queries
     * merge host and service data members into returned RTMM hosts and RTMM
     * services.
     *
     * @param hostIdsPredicate host ids or null
     * @return collection of RTMM host instances with RTMM services
     */
    private Collection<RTMMHost> queryHosts(Integer [] hostIdsPredicate) {

        final CountDownLatch done = new CountDownLatch(4);
        final AtomicBoolean exit = new AtomicBoolean(false);
        final ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        final ConcurrentSkipListMap<Integer,RTMMHost> hosts = new ConcurrentSkipListMap<Integer,RTMMHost>();
        final ConcurrentSkipListMap<Integer,RTMMService> services = new ConcurrentSkipListMap<Integer,RTMMService>();

        new QueryThread(HOST_QUERY, HOST_QUERY_FETCH_SIZE, HOST_QUERY_WITH_IDS_PREDICATE, hostIdsPredicate,
                new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup host
                Integer id = new Integer(results.getInt("id"));
                RTMMHost host = lookupHost(hosts, id);
                // load host data members
                host.setHostName(results.getString("hostName"));
                host.setAppTypeName(results.getString("appTypeName"));
                host.setAppTypeDisplayName(results.getString("appTypeDisplayName"));
                host.setLastCheckTime(results.getTimestamp("lastCheckTime"));
                host.setNextCheckTime(results.getTimestamp("nextCheckTime"));
                host.setMonitorStatus(results.getString("monitorStatus"));
            }
        }, done, exit, exceptions, "RTMMServices.queryHosts.host");

        new QueryThread(HOST_STATUS_PROPERTY_QUERY, HOST_STATUS_PROPERTY_QUERY_FETCH_SIZE,
                HOST_STATUS_PROPERTY_QUERY_WITH_IDS_PREDICATE, hostIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup host
                Integer id = new Integer(results.getInt("id"));
                RTMMHost host = lookupHost(hosts, id);
                // load host data members
                String name = results.getString("name");
                if ("Alias".equals(name)) {
                    host.setAlias(results.getString("valueString"));
                } else if ("isAcknowledged".equals(name)) {
                    host.setIsAcknowledged(results.getBoolean("valueBoolean"));
                } else if ("isProblemAcknowledged".equals(name)) {
                    host.setIsProblemAcknowledged(results.getBoolean("valueBoolean"));
                } else if ("LastStateChange".equals(name)) {
                    host.setLastStateChange(results.getTimestamp("valueDate"));
                } else if ("CurrentAttempt".equals(name)) {
                    host.setCurrentAttempt(results.getLong("valueLong"));
                } else if ("MaxAttempts".equals(name)) {
                    host.setMaxAttempts(results.getLong("valueLong"));
                } else if ("ScheduledDowntimeDepth".equals(name)) {
                    host.setScheduledDowntimeDepth(results.getInt("valueInteger"));
                } else if ("LastPluginOutput".equals(name)) {
                    host.setLastPluginOutput(results.getString("valueString"));
                }
            }
        }, done, exit, exceptions, "RTMMServices.queryHosts.hoststatusproperty");

        new QueryThread(SERVICE_STATUS_QUERY, SERVICE_STATUS_QUERY_FETCH_SIZE, SERVICE_STATUS_QUERY_WITH_IDS_PREDICATE,
                hostIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup service
                Integer id = new Integer(results.getInt("id"));
                RTMMService service = lookupService(services, id);
                // lookup host
                Integer hostId = new Integer(results.getInt("hostId"));
                RTMMHost host = lookupHost(hosts, hostId);
                // load service data members
                service.setDescription(results.getString("description"));
                service.setAppTypeName(results.getString("appTypeName"));
                service.setAppTypeDisplayName(results.getString("appTypeDisplayName"));
                service.setLastCheckTime(results.getTimestamp("lastCheckTime"));
                service.setNextCheckTime(results.getTimestamp("nextCheckTime"));
                service.setLastStateChange(results.getTimestamp("lastStateChange"));
                service.setMonitorStatus(results.getString("monitorStatus"));
                // add service to host
                host.getServices().add(service);
            }
        }, done, exit, exceptions, "RTMMServices.queryHosts.servicestatus");

        new QueryThread(SERVICE_STATUS_PROPERTY_QUERY, SERVICE_STATUS_PROPERTY_QUERY_FETCH_SIZE,
                SERVICE_STATUS_PROPERTY_QUERY_WITH_IDS_PREDICATE, hostIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup service
                Integer id = new Integer(results.getInt("id"));
                RTMMService service = lookupService(services, id);
                // load service data members
                String name = results.getString("name");
                if ("isProblemAcknowledged".equals(name)) {
                    service.setIsProblemAcknowledged(results.getBoolean("valueBoolean"));
                } else if ("CurrentAttempt".equals(name)) {
                    service.setCurrentAttempt(results.getLong("valueLong"));
                } else if ("MaxAttempts".equals(name)) {
                    service.setMaxAttempts(results.getLong("valueLong"));
                } else if ("ScheduledDowntimeDepth".equals(name)) {
                    service.setScheduledDowntimeDepth(results.getInt("valueInteger"));
                } else if ("LastPluginOutput".equals(name)) {
                    service.setLastPluginOutput(results.getString("valueString"));
                } else if ("PerformanceData".equals(name)) {
                    service.setPerformanceData(results.getString("valueString"));
                }
            }
        }, done, exit, exceptions, "RTMMServices.queryHosts.servicestatusproperty");

        waitForDone("queryHosts", done, exit, exceptions);

        return hosts.values();
    }

    @Override
    public Collection<RTMMHostGroup> getHostGroups() {
        return queryHostGroups(null);
    }

    @Override
    public RTMMHostGroup getHostGroup(int hostGroupId) {
        Collection<RTMMHostGroup> hostGroup = queryHostGroups(new Integer[]{hostGroupId});
        return (((hostGroup != null) && (hostGroup.size() == 1)) ? hostGroup.iterator().next() : null);
    }

    @Override
    public Collection<RTMMHostGroup> getHostGroups(Integer [] hostGroupIds) {
        return queryHostGroups(hostGroupIds);
    }

    /**
     * Query host groups or host group by id using parallel direct SQL access,
     * returning optimized RTMM host group instance(s). Concurrent queries
     * merge host group data members into returned RTMM host groups.
     *
     * @param hostGroupIdsPredicate host group ids or null
     * @return collection of RTMM host group instances
     */
    private Collection<RTMMHostGroup> queryHostGroups(Integer [] hostGroupIdsPredicate) {

        final CountDownLatch done = new CountDownLatch(2);
        final AtomicBoolean exit = new AtomicBoolean(false);
        final ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        final ConcurrentSkipListMap<Integer,RTMMHostGroup> hostGroups = new ConcurrentSkipListMap<Integer,RTMMHostGroup>();

        new QueryThread(HOST_GROUP_QUERY, HOST_GROUP_QUERY_FETCH_SIZE, HOST_GROUP_QUERY_WITH_IDS_PREDICATE,
                hostGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup host group
                Integer id = new Integer(results.getInt("id"));
                RTMMHostGroup hostGroup = lookupHostGroup(hostGroups, id);
                // load host group data members
                hostGroup.setName(results.getString("name"));
                hostGroup.setAlias(results.getString("alias"));
                hostGroup.setAppTypeName(results.getString("appTypeName"));
            }
        }, done, exit, exceptions, "RTMMServices.queryHostGroups.hostgroup");

        new QueryThread(HOST_GROUP_COLLECTION_QUERY, HOST_GROUP_COLLECTION_QUERY_FETCH_SIZE,
                HOST_GROUP_COLLECTION_QUERY_WITH_IDS_PREDICATE, hostGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup host group
                Integer id = new Integer(results.getInt("id"));
                RTMMHostGroup hostGroup = lookupHostGroup(hostGroups, id);
                // load host group data members
                hostGroup.getHostIds().add(results.getInt("hostId"));
            }
        }, done, exit, exceptions, "RTMMServices.queryHostGroups.hostgroupcollection");

        waitForDone("queryHostGroups", done, exit, exceptions);

        return hostGroups.values();
    }

    @Override
    public Collection<RTMMServiceGroup> getServiceGroups() {
        return queryServiceGroups(null);
    }

    @Override
    public RTMMServiceGroup getServiceGroup(int serviceGroupId) {
        Collection<RTMMServiceGroup> serviceGroup = queryServiceGroups(new Integer[]{serviceGroupId});
        return (((serviceGroup != null) && (serviceGroup.size() == 1)) ? serviceGroup.iterator().next() : null);
    }

    @Override
    public Collection<RTMMServiceGroup> getServiceGroups(Integer [] serviceGroupIds) {
        return queryServiceGroups(serviceGroupIds);
    }

    /**
     * Query service groups or service group by id using parallel direct SQL
     * access, returning optimized RTMM service group instance(s). Concurrent
     * queries merge service group data members into returned RTMM service groups.
     *
     * @param serviceGroupIdsPredicate service group ids or null
     * @return collection of RTMM service group instances
     */
    private Collection<RTMMServiceGroup> queryServiceGroups(Integer [] serviceGroupIdsPredicate) {

        final CountDownLatch done = new CountDownLatch(2);
        final AtomicBoolean exit = new AtomicBoolean(false);
        final ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        final ConcurrentSkipListMap<Integer,RTMMServiceGroup> serviceGroups = new ConcurrentSkipListMap<Integer,RTMMServiceGroup>();

        new QueryThread(SERVICE_GROUP_QUERY, SERVICE_GROUP_QUERY_FETCH_SIZE, SERVICE_GROUP_QUERY_WITH_IDS_PREDICATE,
                serviceGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup service group
                Integer id = new Integer(results.getInt("id"));
                RTMMServiceGroup serviceGroup = lookupServiceGroup(serviceGroups, id);
                // load service group data members
                serviceGroup.setName(results.getString("name"));
                serviceGroup.setAppTypeName(results.getString("appTypeName"));
            }
        }, done, exit, exceptions, "RTMMServices.queryServiceGroups.category");

        new QueryThread(SERVICE_GROUP_ENTITY_QUERY, SERVICE_GROUP_ENTITY_QUERY_FETCH_SIZE,
                SERVICE_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE, serviceGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup service group
                Integer id = new Integer(results.getInt("id"));
                RTMMServiceGroup serviceGroup = lookupServiceGroup(serviceGroups, id);
                // load service group data members
                serviceGroup.getServiceIds().add(results.getInt("serviceId"));
            }
        }, done, exit, exceptions, "RTMMServices.queryServiceGroups.categoryentity");

        waitForDone("queryServiceGroups", done, exit, exceptions);

        return serviceGroups.values();
    }

    @Override
    public Collection<RTMMCustomGroup> getCustomGroups() {
        return queryCustomGroups(null);
    }

    @Override
    public RTMMCustomGroup getCustomGroup(int customGroupId) {
        Collection<RTMMCustomGroup> customGroup = queryCustomGroups(new Integer[]{customGroupId});
        return (((customGroup != null) && (customGroup.size() == 1)) ? customGroup.iterator().next() : null);
    }

    @Override
    public Collection<RTMMCustomGroup> getCustomGroups(Integer [] customGroupIds) {
        return queryCustomGroups(customGroupIds);
    }

    /**
     * Query custom groups or custom group by id using parallel direct SQL
     * access, returning optimized RTMM custom group instance(s). Concurrent
     * queries merge custom group data members into returned RTMM custom groups.
     *
     * @param customGroupIdsPredicate custom group ids or null
     * @return collection of RTMM custom group instances
     */
    private Collection<RTMMCustomGroup> queryCustomGroups(Integer [] customGroupIdsPredicate) {

        final CountDownLatch done = new CountDownLatch(4);
        final AtomicBoolean exit = new AtomicBoolean(false);
        final ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        final ConcurrentSkipListMap<Integer,RTMMCustomGroup> customGroups = new ConcurrentSkipListMap<Integer,RTMMCustomGroup>();

        new QueryThread(CUSTOM_GROUP_QUERY, CUSTOM_GROUP_QUERY_FETCH_SIZE, CUSTOM_GROUP_QUERY_WITH_IDS_PREDICATE,
                customGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup custom group
                Integer id = new Integer(results.getInt("id"));
                RTMMCustomGroup customGroup = lookupCustomGroup(customGroups, id);
                // load custom group data members
                customGroup.setName(results.getString("name"));
                customGroup.setIsRoot(results.getBoolean("isRoot"));
            }
        }, done, exit, exceptions, "RTMMServices.queryCustomGroups.category");

        new QueryThread(CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY, CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY_FETCH_SIZE,
                CUSTOM_GROUP_HOST_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE, customGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup custom group
                Integer id = new Integer(results.getInt("id"));
                RTMMCustomGroup customGroup = lookupCustomGroup(customGroups, id);
                // load custom group data members
                customGroup.getHostGroupIds().add(results.getInt("hostGroupId"));
            }
        }, done, exit, exceptions, "RTMMServices.queryCustomGroups.categoryentity(host group)");

        new QueryThread(CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY, CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY_FETCH_SIZE,
                CUSTOM_GROUP_SERVICE_GROUP_ENTITY_QUERY_WITH_IDS_PREDICATE, customGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup custom group
                Integer id = new Integer(results.getInt("id"));
                RTMMCustomGroup customGroup = lookupCustomGroup(customGroups, id);
                // load custom group data members
                customGroup.getServiceGroupIds().add(results.getInt("serviceGroupId"));
            }
        }, done, exit, exceptions, "RTMMServices.queryCustomGroups.categoryentity(service group)");

        new QueryThread(CUSTOM_GROUP_HIERARCHY_QUERY, CUSTOM_GROUP_HIERARCHY_QUERY_FETCH_SIZE,
                CUSTOM_GROUP_HIERARCHY_QUERY_WITH_IDS_PREDICATE, customGroupIdsPredicate, new QueryResults() {
            @Override
            public void result(ResultSet results) throws Exception {
                // lookup custom group
                Integer id = new Integer(results.getInt("id"));
                RTMMCustomGroup customGroup = lookupCustomGroup(customGroups, id);
                // load custom group data members
                customGroup.getChildIds().add(results.getInt("childId"));
            }
        }, done, exit, exceptions, "RTMMServices.queryCustomGroups.categoryhierarchy");

        waitForDone("queryCustomGroups", done, exit, exceptions);

        return customGroups.values();
    }

    /**
     * Query results interface invoked by query thread to return results.
     */
    private interface QueryResults {
        public void result(ResultSet results) throws Exception;
    }

    /**
     * Query thread used to return RTMM object data members concurrently.
     */
    private class QueryThread {
        /**
         * Create and start concurrent query thread performing specified query
         * or query with predicate. When complete, the thread counts down the
         * specified latch. If the exit flag is set, the query will abort results
         * processing. Results are returned via the {@link com.groundwork.collage.biz.RTMMServicesImpl.QueryResults}
         * interface and exceptions are added to the exceptions collection.
         *
         * @param query query to perform
         * @param fetchSize fetch size used with query
         * @param idPredicateQuery id predicate query to perform
         * @param idsPredicate id predicate or null
         * @param queryResults query results implementation to receive results
         * @param done done latch
         * @param exit exit flag
         * @param exceptions returned exceptions collection
         * @param threadName thread name
         */
        public QueryThread(final String query, final int fetchSize, final String idPredicateQuery,
                           final Integer [] idsPredicate, final QueryResults queryResults, final CountDownLatch done,
                           final AtomicBoolean exit, final Collection<Exception> exceptions, String threadName) {
            // create and start query thread
            Thread queryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // track query resources for cleanup
                    StatelessSession session = null;
                    Statement statement = null;
                    ResultSet results = null;
                    try {
                        // open new stateless hibernate session for JDBC query
                        session = getSessionFactory().openStatelessSession();
                        // disable autocommit to enable cursor-based result set
                        session.connection().setAutoCommit(false);
                        // create query statement
                        statement = session.connection().createStatement();
                        if (idsPredicate == null) {
                            // set fetch size for results cursor
                            statement.setFetchSize(fetchSize);
                            // execute query
                            results = statement.executeQuery(query);
                        } else {
                            // disable cursor-based result set for small N
                            if (idsPredicate.length <= fetchSize) {
                                statement.setFetchSize(0);
                            } else {
                                statement.setFetchSize(fetchSize);
                            }
                            // execute id predicate query, substituting ids into query
                            String idsPredicateCSV = makeSQLCSV(idsPredicate);
                            results = statement.executeQuery(String.format(idPredicateQuery, idsPredicateCSV));
                        }
                        // pump results to query results implementation, abort if exit set
                        while (!exit.get() && results.next()) {
                            queryResults.result(results);
                        }
                    } catch (Exception e) {
                        // return exceptions
                        exceptions.add(e);
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
                            // return exceptions
                            exceptions.add(e);
                        }
                    }

                    // count down latch to signal done
                    done.countDown();
                }
            }, threadName);
            queryThread.setDaemon(true);
            queryThread.start();
        }
    }

    /**
     * Convert array of values to CSV string suitable for use with SQL 'in' operator.
     *
     * @param values array of values
     * @return CSV string
     */
    private static String makeSQLCSV(Object [] values) {
        StringBuilder csv = new StringBuilder();
        for (Object value : values) {
            if (csv.length() > 0) {
                csv.append(", ");
            }
            if (value instanceof CharSequence) {
                csv.append('\'').append(value.toString()).append('\'');
            } else {
                csv.append(value.toString());
            }
        }
        return csv.toString();
    }

    /**
     * Lookup or create target RTMM host in concurrent map.
     *
     * @param hosts concurrent hosts map by id
     * @param id host id to lookup or create
     * @return target RTMM host instance
     */
    private static RTMMHost lookupHost(ConcurrentMap<Integer,RTMMHost> hosts, Integer id) {
        RTMMHost host = hosts.get(id);
        if (host == null) {
            RTMMHost newHost = new RTMMHost(id);
            host = hosts.putIfAbsent(id, newHost);
            host = ((host != null) ? host : newHost);
        }
        return host;
    }

    /**
     * Lookup or create target RTMM service in concurrent map.
     *
     * @param services concurrent services map by id
     * @param id service id to lookup or create
     * @return target RTMM service instance
     */
    private static RTMMService lookupService(ConcurrentMap<Integer,RTMMService> services, Integer id) {
        RTMMService service = services.get(id);
        if (service == null) {
            RTMMService newService = new RTMMService(id);
            service = services.putIfAbsent(id, newService);
            service = ((service != null) ? service : newService);
        }
        return service;
    }

    /**
     * Lookup or create target RTMM host group in concurrent map.
     *
     * @param hostGroups concurrent host groups map by id
     * @param id host group id to lookup or create
     * @return target RTMM host group instance
     */
    private static RTMMHostGroup lookupHostGroup(ConcurrentMap<Integer,RTMMHostGroup> hostGroups, Integer id) {
        RTMMHostGroup hostGroup = hostGroups.get(id);
        if (hostGroup == null) {
            RTMMHostGroup newHostGroup = new RTMMHostGroup(id);
            hostGroup = hostGroups.putIfAbsent(id, newHostGroup);
            hostGroup = ((hostGroup != null) ? hostGroup : newHostGroup);
        }
        return hostGroup;
    }

    /**
     * Lookup or create target RTMM service group in concurrent map.
     *
     * @param serviceGroups concurrent service groups map by id
     * @param id service group id to lookup or create
     * @return target RTMM service group instance
     */
    private static RTMMServiceGroup lookupServiceGroup(ConcurrentMap<Integer,RTMMServiceGroup> serviceGroups, Integer id) {
        RTMMServiceGroup serviceGroup = serviceGroups.get(id);
        if (serviceGroup == null) {
            RTMMServiceGroup newServiceGroup = new RTMMServiceGroup(id);
            serviceGroup = serviceGroups.putIfAbsent(id, newServiceGroup);
            serviceGroup = ((serviceGroup != null) ? serviceGroup : newServiceGroup);
        }
        return serviceGroup;
    }

    /**
     * Lookup or create target RTMM custom group in concurrent map.
     *
     * @param customGroups concurrent custom groups map by id
     * @param id custom group id to lookup or create
     * @return target RTMM custom group instance
     */
    private static RTMMCustomGroup lookupCustomGroup(ConcurrentMap<Integer,RTMMCustomGroup> customGroups, Integer id) {
        RTMMCustomGroup customGroup = customGroups.get(id);
        if (customGroup == null) {
            RTMMCustomGroup newCustomGroup = new RTMMCustomGroup(id);
            customGroup = customGroups.putIfAbsent(id, newCustomGroup);
            customGroup = ((customGroup != null) ? customGroup : newCustomGroup);
        }
        return customGroup;
    }

    /**
     * Wait for parallel query threads to complete or timeout. Uses
     * count down latch to wait on concurrent queries. On timeout, sets
     * the exit flag to abort running queries. Logs exceptions if
     * returned by the queries.
     *
     * @param request RTMM request name
     * @param done done latch
     * @param exit exit flag
     * @param exceptions returned exceptions collection
     */
    private static void waitForDone(String request, CountDownLatch done, AtomicBoolean exit,
                                    Collection<Exception> exceptions) {
        // wait until queries done
        try {
            done.await(MAX_WAIT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        }

        // if wait timed out, set exit to abort running queries
        if (done.getCount() > 0) {
            exit.set(true);
            throw new RuntimeException("RTMMServices." + request + " request timed out");
        }

        // log exceptions
        if (!exceptions.isEmpty()) {
            for (Exception exception : exceptions) {
                log.error("RTMMServices." + request + " request error: "+exception, exception);
            }
            throw new RuntimeException("RTMMServices." + request + " request failed: "+exceptions);
        }
    }
}
