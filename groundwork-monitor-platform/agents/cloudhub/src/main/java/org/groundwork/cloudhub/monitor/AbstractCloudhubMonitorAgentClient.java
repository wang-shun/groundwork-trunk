/*
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

package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.MonitorAgentInfo;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.agents.monitor.MonitorState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractCloudhubMonitorAgentClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class AbstractCloudhubMonitorAgentClient implements CloudhubMonitorAgent {

    private static Logger log = Logger.getLogger(AbstractCloudhubMonitorAgentClient.class);

    protected CloudhubAgentInfo agentInfo;
    protected ConnectionConfiguration configuration;
    protected MonitorState monitorState;

    protected AbstractCloudhubMonitorAgentClient(ConnectionConfiguration configuration,
                                                 CloudhubAgentInfo agentInfo) {
        this.configuration = configuration;
        agentInfo.setConfigurationPath(ConnectionConfiguration.makePath(configuration));
        this.agentInfo = agentInfo;
        this.monitorState = new MonitorState();
    }

    public String getName() {
        return agentInfo.getName();
    }

    @Override
    public void suspend() {
        if (log.isInfoEnabled()) {
            log.info("Cloudhub Suspending agent " + agentInfo.toString());
        }
        monitorState.setSuspended(true);
    }

    @Override
    public void unsuspend() {
        if (log.isInfoEnabled()) {
            log.info("Cloudhub Un-suspending agent " + agentInfo.toString());
        }
        monitorState.setForceSuspend(false);
        monitorState.setSuspended(false);
    }

    @Override
    public void shutdown() {
        if (log.isInfoEnabled()) {
            log.info("Cloudhub Shutting down agent " + agentInfo.toString());
        }
        monitorState.setForceShutdown(true);
    }

    @Override
    public boolean isRunning() {
        return monitorState.isRunning();
    }

    @Override
    public boolean isSuspended() {
        return monitorState.isSuspended();
    }

    @Override
    public MonitorAgentInfo getAgentInfo() {
        return agentInfo;
    }

    @Override
    public void setConfigurationUpdated() {
        monitorState.setConfigurationUpdated(true);
    }

    @Override
    public ConnectionConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void submitRequestToDeleteMonitoringData() {
        suspend();
        if (log.isInfoEnabled()) {
            log.info("Submitting request to delete monitoring data " + agentInfo.toString());
        }
        monitorState.setForceDelete(true);
    }

    @Override
    public void submitRequestToSuspend() {
        monitorState.setSuspended(true);
        if (log.isInfoEnabled()) {
            log.info("Submitting request to suspend monitoring " + agentInfo.toString());
        }
        monitorState.setForceSuspend(true);
    }

    @Override
    public void submitRequestToRenameHosts(String agentId, String oldPrefix, String newPrefix) {
        if (log.isInfoEnabled()) {
            log.info("Submitting request to rename/delete monitoring data " + agentId +
                    ", old prefix: " + oldPrefix + ", new prefix: " + newPrefix);
        }
        if (monitorState.isForceRename() && monitorState.getRenameOldPrefix() != null) {
            oldPrefix = monitorState.getRenameOldPrefix();
        }
        monitorState.startRename(agentId, oldPrefix, newPrefix);
    }

    @Override
    public void submitRequestToDeleteServices(
            MonitorChangeState changeState) {
        if (log.isInfoEnabled()) {
            log.info("Submitting request to delete services for agent " + agentInfo.getAgentId());
        }
        monitorState.startDeleteServices(changeState);
    }

    @Override
    public void submitRequestToDeleteView(MonitorChangeState changeState) {
        if (log.isInfoEnabled()) {
            log.info("Submitting request to delete views " + changeState.getViews() + " for agent " + agentInfo.getAgentId());
        }
        monitorState.startDeleteView(changeState);
    }

    @Override
    public void submitRequestToDeleteConnectorHost(MonitorChangeState changeState) {
        if (log.isInfoEnabled()) {
            log.info("Submitting request to delete connector host " + changeState.getConnectorHost() + " for agent " + agentInfo.getAgentId());
        }
        monitorState.startDeleteConnectorHost(changeState);
    }

    /**
     * Delete services from in state memory to sync with deletions from permanent storage
     * If services are not deleted from monitoring state, they will come back after being deleted from database
     *
     * @param monitoringState
     */
    protected void deleteServicesFromMonitoringState(MonitoringState monitoringState) {
        List<String> hostDeleteList = new ArrayList<>();
        for (BaseHost host : monitoringState.hosts().values()) {
            for (DeleteServiceInfo primaryMetric : monitorState.getServicesChangeState().getGroupedServices().getPrimary()) {
                for (BaseMetric metric : host.getMetricPool().values()) {
                    if (StringUtils.isEmpty(primaryMetric.getServiceType()) || primaryMetric.getServiceType().equals(metric.getMetricType())) {
                        if (primaryMetric.getName().equals(metric.getServiceName())) {
                            hostDeleteList.add(primaryMetric.getName());
                        }
                    }
                }
            }
            List<String> vmDeleteList = new ArrayList<>();
            for (BaseVM vm : host.getVMPool().values()) {
                // BugFix 7.2.1 CLOUDHUB-354, amazon connector uses non-standard metric collection, vms are stored in hypervisor (primary) collection
                // all deletions were coming back
                List<DeleteServiceInfo> deleteServiceInfos = (agentInfo.getVirtualSystem().equals(VirtualSystem.AMAZON)) ?
                        monitorState.getServicesChangeState().getGroupedServices().getPrimary()
                        : monitorState.getServicesChangeState().getGroupedServices().getSecondary();
                for (DeleteServiceInfo secondaryMetric : deleteServiceInfos) {
                    for (BaseMetric metric : vm.getMetricPool().values()) {
                        if (StringUtils.isEmpty(secondaryMetric.getServiceType()) || secondaryMetric.getServiceType().equals(metric.getMetricType())) {
                            if (secondaryMetric.getName().equals(metric.getServiceName())) {
                                vmDeleteList.add(secondaryMetric.getName());
                            }
                        }
                    }
                }
                for (String key : vmDeleteList) {
                    Object found = vm.getMetricPool().remove(key);
                    if (found != null && log.isInfoEnabled()) {
                        log.info("Removing from VM " + host.getHostName() + " Pool: " + key);
                    }
                }
            }
            for (String key : hostDeleteList) {
                Object found = host.getMetricPool().remove(key);
                if (found != null && log.isInfoEnabled()) {
                    log.info("Removing from Host " + host.getHostName() + " Pool: " + key);
                }
            }
        }
    }

}
