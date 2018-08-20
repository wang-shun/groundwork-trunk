package com.groundwork.collage.query;

import com.groundwork.collage.model.Host;
import com.groundwork.collage.test.AbstractSpringAssembledTest;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class QueryTranslatorTest extends AbstractSpringAssembledTest {

    public QueryTranslatorTest(String x) {
        super(x);
    }

    // WARNING: these tests depend on a special data set. TODO: integrate this data set into test suite


    @Test
    public void testQueryTranslator() {
        beginTransaction();
        QueryTranslator queryTranslator = collage.getQueryTranslator();
        HostService hostService = collage.getHostService();

        // test pass through
        String categoryQuery = "pass:select distinct c,ce from Category c inner join c.categoryEntities as ce where ce.entityType.name = 'SERVICE_STATUS'";
        QueryTranslation translation2 = queryTranslator.translate(categoryQuery, QueryTranslator.CATEGORY_KEY);
        assertEquals(translation2.getHql(), "select distinct c,ce from Category c inner join c.categoryEntities as ce where ce.entityType.name = 'SERVICE_STATUS'");
        
        // test hostName in services and no white space
        String servicesQuery = "hostName='localhost'";
        QueryTranslation translation = queryTranslator.translate(servicesQuery, QueryTranslator.SERVICE_KEY);
        System.out.println("hql = " + translation.getHql());

        // test no white space
        String noWhiteSpace = "((property.ExecutionTime>4.0 or hostName>='localhost')and(monitorStatus='UP' or monitorStatus<>'DOWN'))";
        translation = queryTranslator.translate(noWhiteSpace, QueryTranslator.HOST_KEY);
        assertEquals("select distinct h,p0 from Host h  inner join h.hostStatus.propertyValues as p0  where ((p0.propertyTypeId = 26 and p0.valueDouble > 4.0 or hostName >= 'localhost') and(monitorStatus = 'UP' or h.hostStatus.hostMonitorStatus.name <> 'DOWN'))", translation.getHql());

        // test category.name parsing
        String catQuery = "category.name = 'SG1'"; // web-svr, SG1
        translation = queryTranslator.translate(catQuery, QueryTranslator.EVENT_KEY);
        System.out.println("hql = " + translation.getHql());
        assertEquals("select distinct m from LogMessage m  where m.serviceStatus.serviceStatusId in $1", translation.getHql());

        // in clause parsing
        String inQuery = "host in ('qa-load-xp-1','qa-sles-11-64','do-win7-1')";
        translation = queryTranslator.translate(inQuery, QueryTranslator.EVENT_KEY);
        assertEquals("select distinct m from LogMessage m  where m.hostStatus.host.hostName in ('qa-load-xp-1','qa-sles-11-64','do-win7-1')", translation.getHql());

        // Parsing test
        String testDontTouchLiterals = "'(   UP DOWN   )  '";
        String query = String.format("(( hostName like 'do%%' and property.ExecutionTime > 4.0) and (monitorStatus = %s))", testDontTouchLiterals);
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        String hql = translation.getHql();
        assertTrue(hql.indexOf(testDontTouchLiterals) > 0);
        assertTrue(hql.indexOf("h.hostStatus.propertyValues") > 0);
        assertTrue(hql.indexOf("p0.valueDouble > 4.0") > 0);

        // simple like query test
        query = "h.hostName like 'do%'";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        List<Host> hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(3, hosts.size());
        for (Host host : hosts) {
            System.out.format("host = %s, %s\n", host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName());
            assertTrue(host.getHostName().startsWith("do"));
        }
        System.out.println("-----");

        // Monitor Status Query with sort
        query = "monitorStatus = 'UP' order by hostName";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(15, hosts.size());
        String lastHost = "";
        for (Host host : hosts) {
            System.out.format("host = %s, %s\n", host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName());
            assertTrue(lastHost.compareTo(host.getHostName()) < 0); // asserting sort order
        }
        System.out.println("-----");


        // Range query with sort
        query = "property.ExecutionTime between 1 and 10 order by property.ExecutionTime asc";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        double lastExecTime = 0.0;
        assertEquals(9, hosts.size());
        for (Host host : hosts) {
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            System.out.format("host = %s, %s, exec = %f\n", host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), execTime);
            assertTrue(execTime >= 1.0 && execTime <= 10.0);
            assertTrue(lastExecTime < execTime); // asserting sort order
        }
        System.out.println("-----");


        // Greater than query plus monitor status
        query = "(property.ExecutionTime > 30 and monitorStatus <> 'UP')";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(5, hosts.size());
        for (Host host : hosts) {
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            System.out.format("host = %s, %s, %s, exec = %f\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(), execTime);
            assert(host.getHostStatus().getHostMonitorStatus().getName().equalsIgnoreCase("UNSCHEDULED DOWN"));
        }
        System.out.println("-----");

        // Application Type and Device Identification
        query = "(appType = 'NAGIOS' and device like '172.28.113%')";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(19, hosts.size());
        for (Host host : hosts) {
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            System.out.format("host = %s, %s, %s, %s, %f\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(),
                    host.getApplicationType().getName(), host.getDevice().getIdentification(), execTime);
            //dumpProperties(host);
            assert(host.getApplicationType().getName().equals("NAGIOS"));
            assert(host.getDevice().getIdentification().startsWith("172.28.113"));
        }
        System.out.println("-----");

        // Each property requires an additional inner join
        query = "(property.ExecutionTime < 10 and property.Latency between 800 and 900) order by property.Latency";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(3, hosts.size());
        for (Host host : hosts) {
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            Double latency = (Double)host.getHostStatus().getProperty("Latency");
            System.out.format("host = %s, %s, %s, exec = %f, latency = %f\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    execTime, latency);
            assert(execTime < 10.0);
            assert(latency >= 800.0 && latency <= 900.0);
        }
        System.out.println("-----");

        // Booleans isChecksEnabled
        query = "property.isChecksEnabled = true";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(20, hosts.size());
        for (Host host : hosts) {
            boolean isChecksEnabled = (Boolean)host.getHostStatus().getProperty("isChecksEnabled");
            System.out.format("host = %s, %s, %s, %b\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    isChecksEnabled);
            assert(isChecksEnabled);
        }
        System.out.println("-----");

        // Dates LastStateChange note: functions all available current_date() day(prop) == day(current_time())
        query = "property.LastStateChange > '2013-05-17 09:33:00'";
        translation  = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(8, hosts.size());
        for (Host host : hosts) {
            Date lastStateChange = (Date)host.getHostStatus().getProperty("LastStateChange");
            String dateString = String.format("Date: %1$te/%1$tm/%1$tY at %1$tH:%1$tM:%1$tS", lastStateChange);
            System.out.format("host = %s, %s, %s, %s\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    dateString);
        }
        System.out.println("-----");
        query = "(property.LastStateChange between '2013-05-20' and '2013-05-22') and monitorStatus = 'UP'";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(1, hosts.size());
        for (Host host : hosts) {
            Date lastStateChange = (Date)host.getHostStatus().getProperty("LastStateChange");
            String dateString = String.format("Date: %1$te/%1$tm/%1$tY at %1$tH:%1$tM:%1$tS", lastStateChange);
            System.out.format("host = %s, %s, %s, %s\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    dateString);
        }
        System.out.println("-----");

        // functions
        query = "day(property.LastStateChange) = 18";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(2, hosts.size());
        for (Host host : hosts) {
            Date lastStateChange = (Date)host.getHostStatus().getProperty("LastStateChange");
            String dateString = String.format("Date: %1$te/%1$tm/%1$tY at %1$tH:%1$tM:%1$tS", lastStateChange);
            System.out.format("host = %s, %s, %s, %s\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    dateString);
        }
        System.out.println("-----");

        // functions
        query = "day(lastCheckTime) = 22 and month(lastCheckTime) = 5 and minute(lastCheckTime) > 43 order by lastCheckTime";
        translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        hql = translation.getHql();
        hosts = hostService.queryHosts(hql, translation.getCountHql(), -1, -1).getResults();
        assertEquals(8, hosts.size());
        for (Host host : hosts) {
            Date lastCheckTime = (Date)host.getHostStatus().getLastCheckTime();
            String dateString = String.format("Date: %1$te/%1$tm/%1$tY at %1$tH:%1$tM:%1$tS", lastCheckTime);
            System.out.format("host = %s, %s, %s, %s\n",
                    host.getHostName(), host.getHostStatus().getHostMonitorStatus().getName(), host.getApplicationType().getName(),
                    dateString);
        }
        System.out.println("-----");

        HostGroupService hostGroupService = collage.getHostGroupService();
        query = "hosts.hostName in ('localhost','malbec')";
        translation = queryTranslator.translate(query, QueryTranslator.HOSTGROUP_KEY);
        System.out.println("hql = " + translation.getHql());
        List<Host> groups = hostGroupService.queryHostGroups(translation.getHql(), translation.getCountHql(), -1, -1).getResults();
        assertEquals(3, groups.size());

        rollbackTransaction();
    }

    @Test
    public void testQueryAuditTranslator() {
        QueryTranslator queryTranslator = collage.getQueryTranslator();

        String q = "hostGroupName = 'host_group' ORDER BY timestamp DESC, auditLogId DESC";
        QueryTranslation translation = queryTranslator.translate(q, QueryTranslator.AUDIT_LOG_KEY);
        System.out.println("hql = " + translation.getHql());

        String q2 = "serviceGroupName = 'service_group' ORDER BY timestamp DESC, auditLogId DESC";
        QueryTranslation translation2 = queryTranslator.translate(q2, QueryTranslator.AUDIT_LOG_KEY);
        System.out.println("hql = " + translation2.getHql());
        assert !translation2.getHql().contains("$1");
    }

/**
    public void testQueryTranslator2() {
        beginTransaction();
        QueryTranslator queryTranslator = collage.getQueryTranslator();
        HostService hostService = collage.getHostService();
        HostGroupService hostGroupService = collage.getHostGroupService();
        String query = "hosts.hostName in ('localhost','malbec')";
        //String q = "select distinct h.hostGroupId, h from HostGroup h inner join h.hosts hh where hh.hostName in ('localhost','malbec') order by h.name";
        //String q = "select distinct h.hostGroupId, h from HostGroup h  where h.hosts.hostName in ('localhost','malbec') order by h.name";
        String q = "select distinct h from HostGroup h  where h.hosts.hostName = 'localhost' or h.hosts.hostName = 'malbec' order by h.name";
        //QueryTranslation translation = queryTranslator.translate(q, QueryTranslator.HOSTGROUP_KEY);
        //System.out.println("hql = " + translation.getHql());
        List<HostGroup> groups = hostGroupService.queryHostGroups(q, null, -1, -1).getResults();
        assertEquals(3, groups.size());
        HostGroup host = groups.get(0);

        rollbackTransaction();

    }

    public void testQueryTranslator3() {
        beginTransaction();
        QueryTranslator queryTranslator = collage.getQueryTranslator();
        HostService hostService = collage.getHostService();
        String query = "select distinct h,h.hostStatus from Host h  where day(h.hostStatus.lastCheckTime) = 22 and month(h.hostStatus.lastCheckTime) = 5 and minute(h.hostStatus.lastCheckTime) > 43 order by h.hostStatus.lastCheckTime";
        List<Host> hosts = hostService.queryHosts(query, null, -1, -1).getResults();
        assertEquals(8, hosts.size());
        for (Host host : hosts) {
            System.out.println(host.getHostName() + ", " + host.getHostStatus().getProperty("ExecutionTime")) ;
        }
    }

    public void testQueryTranslator4() {
        beginTransaction();
        QueryTranslator queryTranslator = collage.getQueryTranslator();
        HostService hostService = collage.getHostService();
        String query = "select distinct h, p0 from Host h inner join h.hostStatus.propertyValues as p0 where h.hostGroups.name in ('IT','HG1','Support') and p0.propertyTypeId = 26 and p0.valueDouble > 0.0 order by p0.valueDouble desc";
        //String query = "select h from Host h inner join h.hostStatus.propertyValues as p0 where ((p0.propertyTypeId = 26 and p0.valueDouble > 4.0 or hostName >= 'localhost') and(monitorStatus = 'UP' or h.hostStatus.hostMonitorStatus.name <> 'DOWN'))\", translation.getHql());"
        //QueryTranslation translation = queryTranslator.translate(q, QueryTranslator.HOSTGROUP_KEY);
        //System.out.println("hql = " + translation.getHql());
        List<Host> hosts = hostService.queryHosts(query, null, -1, -1).getResults();
        assertEquals(6, hosts.size());
        for (Host host : hosts) {
            System.out.println(host.getHostName() + ", " + host.getHostStatus().getProperty("ExecutionTime")) ;
        }

        rollbackTransaction();

    }
   **/
    private void dumpProperties(Host host) {
        Map<String, Object> props = host.getHostStatus().getProperties(true);
        for (Map.Entry prop : props.entrySet()) {
            System.out.format("key: %s, value: %s\n", prop.getKey(), prop.getValue());
        }
    }
}
