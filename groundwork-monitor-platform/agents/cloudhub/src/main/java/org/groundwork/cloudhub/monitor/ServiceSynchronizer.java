package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.DeleteServicePrimaryInfo;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.inventory.HostServiceInventory;
import org.groundwork.cloudhub.inventory.ServiceContainerNode;
import org.groundwork.cloudhub.inventory.ServiceNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.rs.dto.DtoOperationResults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Service Synchronizer deletes stale services that exist in GWOS, but are not in the virtualization server.
 * The algorithm compares two collections of host metrics. The first collection of services
 * is from a previous (old) state on the GWOS. The second collection is the current/latest of MonitoringState (virtualization).
 * Any services that are found in the previous state, but not in the current MonitoringState, are deleted.
 * Typically, services do not go away. Exceptions to that case are VMware snapshots, which are sometimes
 * deleted by VMware administrators.
 * 
 * @since 7.2.0
 */
@Service
public class ServiceSynchronizer {

    private static Logger log = Logger.getLogger(ServiceSynchronizer.class);

    @Value("${synchronizer.services.enabled}")
    protected Boolean enabled = false;

    public Boolean isEnabled(ConfigurationProvider provider) {
        return enabled && provider.isSynchronizeServicesEnabled();
    }

    public List<DeleteServicePrimaryInfo> sync(GwosService gwosService, MonitoringState monitoringState) {
        List<DeleteServicePrimaryInfo> removedMetrics = new ArrayList<>();
        if (monitoringState == null) {
            return removedMetrics;
        }

        long startTime = System.currentTimeMillis();
        // build Virtual HostService inventory from MonitoringState
        HostServiceInventory virtualInventory = new HostServiceInventory();
        for (Map.Entry<String,BaseHost> hostEntry : monitoringState.hosts().entrySet()) {
            ServiceContainerNode hostNode = virtualInventory.addHost(new ServiceContainerNode(hostEntry.getKey(), hostEntry.getValue().getSystemName()));
            for (Map.Entry<String,BaseMetric> metricEntry : hostEntry.getValue().getMetricPool().entrySet()) {
                hostNode.addService(metricEntry.getValue().getServiceName(), metricEntry.getValue().getQuerySpec(), null);
            }
            for (Map.Entry<String,BaseVM> vmEntry : hostEntry.getValue().getVMPool().entrySet()) {
                ServiceContainerNode vmNode = virtualInventory.addHost(new ServiceContainerNode(vmEntry.getKey(), vmEntry.getValue().getSystemName()));
                for (Map.Entry<String, BaseMetric> metricEntry : vmEntry.getValue().getMetricPool().entrySet()) {
                    vmNode.addService(metricEntry.getValue().getServiceName(), metricEntry.getValue().getQuerySpec(),null);
                }
            }
        }

        // build GWOS HostService inventory from Sync depth query of host/services
        HostServiceInventory gwosInventory = gwosService.gatherHostServiceInventory();

        // process synchronization
        for (ServiceContainerNode gwosHost : gwosInventory.getHosts().values()) {
            ServiceContainerNode virtualHost = virtualInventory.lookupHost(gwosHost.getPrefixedName());
            if (virtualHost == null) {
                if (log.isInfoEnabled()) {
                    log.info("Host not found in virtualization inventory: " + gwosHost.getPrefixedName());
                }
                continue;
            }
//            if (log.isInfoEnabled()) {
//                log.info("HOST FOUND " + gwosHost.getPrefixedName());
//            }
            for (ServiceNode gwosMetric : gwosHost.getServices().values()) {
                ServiceNode virtualMetric =  virtualHost.getServices().get(gwosMetric.getName());
                if (virtualMetric == null) {
                    // not found, metric has been deleted
                    if (log.isInfoEnabled()) {
                        log.info("Service to be deleted " + gwosHost.getPrefixedName() + " : " + gwosMetric.getName());
                    }
                    removedMetrics.add(new DeleteServicePrimaryInfo(gwosHost.getPrefixedName(), gwosMetric.getName(), gwosMetric.getId()));
                }
                else {
//                    if (log.isInfoEnabled()) {
//                        log.info("Service FOUND " + gwosHost.getPrefixedName() + " : " + gwosMetric.getName());
//                    }
                }
            }
        }
        DtoOperationResults results = new DtoOperationResults();
        if (removedMetrics.size() > 0) {
            results = gwosService.deleteServices(removedMetrics);
        }

        long timeToExecuteMonitorSync = (System.currentTimeMillis() - startTime);
        if (log.isInfoEnabled()) {
            log.info("Time to execute SERVICE-SYNC operation ["
                    + timeToExecuteMonitorSync
                    + "] ms  : " + results.getCount() + " - successful " + results.getSuccessful());
        }
        return removedMetrics;
    }

}
