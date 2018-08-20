package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoAvailability;
import org.groundwork.rs.dto.DtoStateStatistic;
import org.groundwork.rs.dto.DtoStatistic;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StatisticClientTest extends AbstractClientTest  {

    @Test
    public void testHostTotalStatistics() throws Exception {
        if (serverDown) return;
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        DtoStateStatistic stat = client.totalsByHosts();
        assertEquals(20, stat.getTotalHosts().intValue());
        assertEquals(131, stat.getTotalServices().intValue());
        assertEquals(75.0, stat.getAvailability(), 0.001);
        for (DtoStatistic ds : stat.getProperties()) {
            //switch (ds.getName()) {
            if (ds.getName().equals("UP"))
                assertEquals(15, ds.getCount().intValue());
            else if (ds.getName().equals("UNSCHEDULED DOWN"))
                assertEquals(5, ds.getCount().intValue());
            else {
                assertEquals(0, ds.getCount().intValue());
            }
        }
    }

    @Test
    public void testHostListStatistics() throws Exception {
        if (serverDown) return;
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("localhost");
        names.add("malbec");
        names.add("demo");
        DtoStateStatistic stat = client.getHostStatisticsByHostNames(names);
        assertEquals(3, stat.getTotalHosts().intValue());
        for (DtoStatistic ds : stat.getProperties()) {
            if (ds.getName().equals("UP"))
                assertEquals(3, ds.getCount().intValue());
            else {
                assertEquals(0, ds.getCount().intValue());
            }
        }
    }

    @Test
    public void testHostGroupStatistics() throws Exception {
        if (serverDown) return;
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<DtoStateStatistic> stats = client.getHostGroupStatistics();
        assertEquals(7, stats.size());
    }

    @Test
    public void testHostGroupListStatistics() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("Support");
        names.add("Engineering");
        names.add("IT");
        List<DtoStateStatistic> stats = client.getHostGroupStatisticsByHostGroupNames(names);
        assertEquals(3, stats.size());
    }

    @Test
    public void testServicesStatistics() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<DtoStateStatistic> stats = client.getServiceStatistics();
        assertEquals(7, stats.size());
    }

    @Test
    public void testServicesStatisticsByHostNames() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("localhost");
        names.add("172.28.113.151");
        List<DtoStateStatistic> stats = client.getServiceStatisticsByHostNames(names);
        assertEquals(2, stats.size());
    }

    @Test
    public void testServicesStatisticsByHostNames2() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("host with+funny name");
        names.add("172.28.113.151");
        List<DtoStateStatistic> stats = client.getServiceStatisticsByHostNames(names);
        assertEquals(2, stats.size());
    }

    @Test
    public void testServicesStatisticsByHostGroupNames() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("Support");
        names.add("Engineering");
        names.add("IT");
        List<DtoStateStatistic> stats = client.getServiceStatisticsByHostGroupNames(names);
        assertEquals(3, stats.size());
    }

    @Test
    public void testServicesStatisticsByServiceGroupNames() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<String> names = new ArrayList<String>();
        names.add("SG1");
        List<DtoStateStatistic> stats = client.getServiceStatisticsByServiceGroupNames(names);
        assertEquals(1, stats.size());
    }

    @Test
    public void testServicesStatisticsByServiceGroupAll() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        List<DtoStateStatistic> stats = client.getServiceStatisticsByServiceGroups();
        assertEquals(1, stats.size());
    }

    @Test
    public void testServicesTotalStatistics() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        DtoStateStatistic stat = client.totalsByServices();
        assertEquals(131, stat.getTotalServices().intValue());
    }

    @Test
    public void testAvailability() throws Exception {
        StatisticsClient client = new StatisticsClient(getDeploymentURL());
        DtoAvailability avail = client.hostAvailabilityByHostGroupName("Support");
        assertEquals(75.0, avail.getAvailability(), 0.01);
        avail = client.serviceAvailabilityByHostGroupName("Engineering");
        assertEquals(88.0, avail.getAvailability(), 0.01);
        avail = client.serviceAvailabilityByServiceGroupName("SG1");
        assertEquals(50.0, avail.getAvailability(), 0.01);
    }

}
