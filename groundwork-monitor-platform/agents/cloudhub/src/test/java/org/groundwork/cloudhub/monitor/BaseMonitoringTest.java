package org.groundwork.cloudhub.monitor;

import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.statistics.MonitoringStatistics;
import org.groundwork.cloudhub.statistics.MonitoringStatisticsService;
import org.groundwork.cloudhub.statistics.StatisticsQueries;

import javax.annotation.Resource;

public class BaseMonitoringTest extends AbstractAgentTest {

    @Resource(name = MonitoringStatisticsService.NAME)
    protected MonitoringStatisticsService statisticsService;

    protected void runQueries(MonitoringStatistics statistics) throws Exception {
        StatisticsQueries queries = new StatisticsQueries(statistics);
        queries.connect();
        try {
            statistics.getHostQueries().setHosts(queries.executeCountQuery("host", "applicationtypeid = 200"));
            statistics.getHostQueries().setHostStatuses(queries.executeCountQuery("hoststatus", "applicationtypeid = 200"));
            statistics.getHostQueries().setHostStatusProperty(queries.executeCountQuery("hoststatusproperty", null));
            statistics.getHostQueries().setHostStatusProperty1(queries.executeCountQuery("hoststatusproperty", "propertytypeid = 1"));
            statistics.getHostQueries().setHostStatusProperty2(queries.executeCountQuery("hoststatusproperty", "propertytypeid = 2"));
            statistics.getHostQueries().setHostStatusProperty3(queries.executeCountQuery("hoststatusproperty", "propertytypeid = 3"));
            statistics.getHostQueries().setHostGroups(queries.executeCountQuery("hostgroup", "applicationtypeid = 200"));

            statistics.getServiceQueries().setServices(queries.executeCountQuery("servicestatus", "applicationtypeid = 200"));
            statistics.getServiceQueries().setServicesCPU(queries.executeCountQuery("servicestatus", "applicationtypeid = 200 and servicedescription = 'syn.host.cpu.used'"));
            statistics.getServiceQueries().setServicesCPUToMax(queries.executeCountQuery("servicestatus", "applicationtypeid = 200 and servicedescription = 'syn.vm.cpu.cpuToMax.used'"));
            statistics.getServiceQueries().setServicesFreeSpace(queries.executeCountQuery("servicestatus", "applicationtypeid = 200 and servicedescription = 'summary.freeSpace'"));
            statistics.getServiceQueries().setServicesSwappedMemSize(queries.executeCountQuery("servicestatus", "applicationtypeid = 200 and servicedescription = 'syn.vm.mem.swappedToConfigMemSize.used'"));
            statistics.getServiceQueries().setServiceStatusProperty(queries.executeCountQuery("servicestatusproperty", null));
            statistics.getServiceQueries().setServiceStatusProperty1(queries.executeCountQuery("servicestatusproperty", "propertytypeid = 1"));
            statistics.getServiceQueries().setServiceStatusProperty53(queries.executeCountQuery("servicestatusproperty", "propertytypeid = 53"));

            statistics.getEventQueries().setEvents(queries.executeCountQuery("logmessage", "applicationtypeid = 200"));
            statistics.getEventQueries().setHostEvents(queries.executeCountQuery("logmessage", "applicationtypeid = 200 and servicestatusid is null"));
            statistics.getEventQueries().setServiceEvents(queries.executeCountQuery("logmessage", "applicationtypeid = 200 and servicestatusid is not null"));
            statistics.getEventQueries().setSetupEvents(queries.executeCountQuery("logmessage", "applicationtypeid = 200 and textmessage like 'Initial setup'"));
        }
        finally {
            queries.disconnect();
        }
    }

    protected MonitoringState runAgentClientCycle(String cycleName, CloudhubMonitorAgent agentClient,
                                                                          MonitoringState monitoredState)
            throws Exception {
        MonitoringStatistics statistics = statisticsService.create(agentClient.getAgentInfo().getName());
        if (agentClient instanceof CloudhubMonitorAgentClient) {
            CloudhubMonitorAgentClient monitorAgentClient = (CloudhubMonitorAgentClient)agentClient;
            DataCenterSyncResult syncResult = monitorAgentClient.synchronizeInventory();
            monitoredState = monitorAgentClient.collect(monitoredState);
            monitoredState = monitorAgentClient.filter(monitoredState);
            monitoredState = monitorAgentClient.synchronize(monitoredState, syncResult);
            monitorAgentClient.updateMonitor(monitoredState, syncResult);
        }
        runQueries(statistics);
        statisticsService.rename(statistics.getName(), cycleName);
        return monitoredState;
    }

    protected MonitoringState runAgentClientCycle2(String cycleName, CloudhubMonitorAgent agentClient,
                                                  MonitoringState monitoredState)
            throws Exception {
        if (agentClient instanceof CloudhubMonitorAgentClient) {
            CloudhubMonitorAgentClient monitorAgentClient = (CloudhubMonitorAgentClient)agentClient;
            monitoredState = monitorAgentClient.collect(monitoredState);
            monitoredState = monitorAgentClient.filter(monitoredState);
            DataCenterSyncResult syncResult = monitorAgentClient.synchronizeInventory();
            monitoredState = monitorAgentClient.synchronize(monitoredState, syncResult);
            monitorAgentClient.updateMonitor(monitoredState, syncResult);
        }
        return monitoredState;
    }

}
