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

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.MonitorAgentResult;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.agents.monitor.MonitorTimer;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitorConnector;
import org.groundwork.cloudhub.connectors.MonitorConnectorListener;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.gwos.messages.SuspendedStatusMessages;
import org.groundwork.cloudhub.gwos.messages.UnreachableStatusMessages;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.inventory.MonitorInventoryDifference;
import org.groundwork.rs.client.CollageRestException;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CloudhubMonitorAgentMonitorClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Service(CloudhubMonitorAgentMonitorClient.NAME)
@Scope("prototype")
public class CloudhubMonitorAgentMonitorClient extends AbstractCloudhubMonitorAgentClient implements CloudhubMonitorAgent, MonitorConnectorListener {

    public final static String NAME = "CloudhubMonitorAgentMonitorClient";

    private static Logger log = Logger.getLogger(CloudhubMonitorAgentMonitorClient.class);

    private static final int EVENTS_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors()*3;

    @Resource(name = ConfigurationService.NAME)
    private ConfigurationService configurationService;
    @Resource(name = GwosServiceFactory.NAME)
    private GwosServiceFactory gwosServiceFactory;
    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;
    @Resource(name = MonitorAgentCollector.NAME)
    private MonitorAgentCollector collector;

    private MonitorConnector monitorConnector;
    private ManagementConnector managementConnector;
    private MonitorTimer syncTimer;
    private ExecutorService eventsExecutor;

    private Lock inventoryLock = new ReentrantLock();
    private volatile boolean inventorySynchronized = false;
    private volatile int monitorExceptionCount = 0;
    private volatile int gwosExceptionCount = 0;

    public CloudhubMonitorAgentMonitorClient(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        super(configuration, agentInfo);
    }

    public ConnectionState getConnectionState() {
        if (managementConnector != null) {
            return managementConnector.getConnectionState();
        }
        return ConnectionState.DISCONNECTED;
    }

    public Integer getGroundworkExceptionCount() {
        return this.gwosExceptionCount;
    }

    public Integer getMonitorExceptionCount() {
        return this.monitorExceptionCount;
    }

    @PostConstruct
    public void initialize() {
        // initialize monitor
        monitorConnector = connectorFactory.getMonitorConnector(configuration);
        managementConnector = connectorFactory.getManagementConnector(configuration);
        syncTimer = new MonitorTimer("sync", configuration.getCommon().getSyncIntervalMinutes(), 0);
    }

    @Override
    public void connect() {
        // clear exception counts
        clearExceptionCounts();
        // setup events executor
        eventsExecutor = Executors.newFixedThreadPool(EVENTS_THREAD_POOL_SIZE, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable task) {
                Thread thread = new Thread(task, "CloudhubMonitorAgentMonitorClientEvents");
                thread.setDaemon(true);
                return thread;
            }
        });
        // connect monitor
        monitorConnector.connect(configuration.getConnection(), this);
    }

    @Override
    public void disconnect() {
        // clear exception counts
        clearExceptionCounts();
        // disconnect monitor
        monitorConnector.disconnect();
        // shutdown events executor
        if (eventsExecutor != null) {
            eventsExecutor.shutdown();
            boolean terminated = false;
            try {
                terminated = eventsExecutor.awaitTermination(agentInfo.getMsAgentSleep(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
            } finally {
                if (!terminated) {
                    eventsExecutor.shutdownNow();
                }
                eventsExecutor = null;
            }
        }
        // release connector
        monitorConnector.releaseThreadResources();
    }

    @Override
    public void monitor() {
        throw new UnsupportedOperationException("CloudhubMonitorAgentMonitorClient.monitor() not supported");
    }

    @Override
    public MonitorAgentResult call() throws Exception {
        long start = System.currentTimeMillis();
        log.info("Cloudhub starting agent monitor client: " + agentInfo.getName());
        agentInfo.clearErrors();
        clearExceptionCounts();
        inventorySynchronized = false;
        boolean interrupted = false;
        boolean connected = false;
        monitorState.setRunning(true);
        while (monitorState.isRunning() && !monitorState.isForceShutdown() && !interrupted) {
            try {
                // check for interrupt
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // check for force delete
                if (monitorState.isForceDelete()) {
                    log.info("Cloudhub commencing with deletion of all agent monitor client: " + agentInfo.getName() +
                            ", (" + agentInfo.getAgentId() + ")");
                    monitorState.setRunning(false);
                    monitorState.setSuspended(true);
                    inventorySynchronized = false;
                    deleteInventory();
                    break;
                }
                // check for force shutdown
                if (monitorState.isForceShutdown()) {
                    log.info("Cloudhub commencing with forced shutdown of agent monitor client: " + agentInfo.getName());
                    monitorState.setRunning(false);
                    break;
                }
                // check for force suspend
                if (monitorState.isForceSuspend()) {
                    log.info("Cloudhub forced suspend agent monitor client: " + agentInfo.getName());
                    monitorState.setForceSuspend(false);
                    inventorySynchronized = false;
                    suspendInventory();
                    continue;
                }
                // check for force suspend
                if (monitorState.isSuspended()) {
                    // suspend monitor connector
                    if (monitorConnector.getConnectionState() == ConnectionState.CONNECTED) {
                        monitorConnector.suspend();
                    }
                    // wait for monitor interval
                    Thread.sleep(agentInfo.getMsAgentSleep());
                    // clear exception counts
                    clearExceptionCounts();
                    continue;
                } else {
                    // unsuspend monitor connector
                    if (inventorySynchronized && (monitorConnector.getConnectionState() == ConnectionState.SEMICONNECTED)) {
                        monitorConnector.unsuspend();
                    }
                }
                // check for interrupt
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // check for configuration change
                if (monitorState.isConfigurationUpdated()) {
                    log.info("Cloudhub refreshing configuration for agent monitor client: " + agentInfo.getName());
                    // disconnect monitor client
                    connected = false;
                    disconnect();
                    // reconfigure
                    configuration = configurationService.readConfiguration(agentInfo.getConfigurationPath());
                    syncTimer = new MonitorTimer("sync", configuration.getCommon().getSyncIntervalMinutes(), 0);
                    agentInfo.setConnectionRetries(configuration.getCommon().getConnectionRetries());
                    monitorState.setConfigurationUpdated(false);
                }
                // check for monitor client connect/reconnect
                if (!connected || ((monitorConnector.getConnectionState() != ConnectionState.SEMICONNECTED) &&
                        (monitorConnector.getConnectionState() != ConnectionState.CONNECTED))) {
                    connect();
                    connected = true;
                }
                // check for interrupt
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // check inventory synchronized
                if (!inventorySynchronized || syncTimer.isReadyAndReset()) {
                    log.info("Cloudhub synchronizing inventory for agent monitor client: " + agentInfo.getName());
                    synchronizeInventory();
                    inventorySynchronized = true;
                    syncTimer.reset();
                    // clear exception counts
                    clearExceptionCounts();
                }
                // check for interrupt
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // wait for monitor interval
                Thread.sleep(agentInfo.getMsAgentSleep());
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    // handle shutdown interrupt
                    String message = "Interrupted agent monitor client: " + agentInfo.getName();
                    log.error(message, e);
                    agentInfo.addError(message);
                    interrupted = true;
                } else {
                    // handle monitor and GWOS exceptions
                    if (e instanceof ConnectorException) {
                        monitorExceptionCount++;
                    } else if ((e instanceof CollageRestException) || (e instanceof CloudHubException)) {
                        gwosExceptionCount++;
                    } else {
                        monitorExceptionCount++;
                    }
                    String message = "Agent monitor client: " + agentInfo.getName() + ", error: (counts:"
                            + monitorExceptionCount + "," + gwosExceptionCount + ") " + e.getMessage();
                    log.error(message, e);
                    agentInfo.addError(message);
                }
            } finally {
                // check exception counts
                checkExceptionCounts(interrupted);
            }
        }
        // disconnect
        if (monitorConnector.getConnectionState() != ConnectionState.DISCONNECTED) {
            disconnect();
        }
        // exit return
        log.info("Cloudhub exiting agent monitor client: " + agentInfo.getName());
        collector.remove(agentInfo.getName());
        return new MonitorAgentResult(this, !interrupted, (System.currentTimeMillis()-start), false);
    }

    @Override
    public void submitRequestToRenameHosts(String agentId, String oldPrefix, String newPrefix) {
        throw new UnsupportedOperationException("CloudhubMonitorAgentMonitorClient.submitRequestToRenameHosts() not supported");
    }

    @Override
    public void submitRequestToDeleteServices(MonitorChangeState changeState) {
        throw new UnsupportedOperationException("CloudhubMonitorAgentMonitorClient.submitRequestToDeleteServices() not supported");
    }

    @Override
    public void submitRequestToDeleteView(MonitorChangeState changeState) {
        throw new UnsupportedOperationException("CloudhubMonitorAgentMonitorClient.submitRequestToDeleteView() not supported");
    }

    @Override
    public void submitRequestToDeleteConnectorHost(MonitorChangeState changeState) {
        throw new UnsupportedOperationException("CloudhubMonitorAgentMonitorClient.submitRequestToDeleteConnectorHost() not supported");
    }

    @Override
    public void eventReceived(final Collection<Object> dtoEventInventory) {
        eventsExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (inventorySynchronized) {
                    try {
                        // modify event inventory
                        GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
                        try {
                            inventoryLock.lock();
                            if (log.isInfoEnabled()) {
                                int hostsModified = 0;
                                int servicesModified = 0;
                                for (Object dtoObject : dtoEventInventory) {
                                    if (dtoObject instanceof DtoHost) {
                                        hostsModified++;
                                    } else if (dtoObject instanceof DtoService) {
                                        servicesModified++;
                                    }
                                }
                                log.info("Cloudhub received modified inventory " + hostsModified + " host(s) and " +
                                        servicesModified + " services(s) for agent monitor client: " + agentInfo.getName());
                            }
                            gwosService.modifyEventInventory(dtoEventInventory);
                        } finally {
                            inventoryLock.unlock();
                        }
                    } catch (Exception e) {
                        // handle GWOS exceptions
                        String message = "Exception processing received event inventory for agent monitor client: " + agentInfo.getName();
                        log.error(message, e);
                        agentInfo.addError(message);
                        gwosExceptionCount++;
                        checkExceptionCounts(false);
                    }
                }
            }
        });
    }

    /**
     * Clear exception counts.
     */
    private void clearExceptionCounts() {
        monitorExceptionCount = 0;
        gwosExceptionCount = 0;
    }

    /**
     * Check exception counts suspending monitor if exceeded unless interrupted.
     *
     * @param interrupted interrupted flag.
     */
    private void checkExceptionCounts(boolean interrupted) {
        if (monitorState.isRunning() && !monitorState.isForceShutdown() && !interrupted) {
            if (agentInfo.getConnectionRetries() > -1) {
                if (monitorExceptionCount >= agentInfo.getConnectionRetries()) {
                    log.error("Monitor exception count exceeded. Suspending agent monitor client: " + agentInfo.getName());
                    monitorState.setSuspended(true);
                }
                if (gwosExceptionCount >= agentInfo.getConnectionRetries()) {
                    log.error("GWOS exception count exceeded. Suspending agent monitor client: " + agentInfo.getName());
                    monitorState.setSuspended(true);
                }
            }
            if (((monitorConnector.getConnectionState() == ConnectionState.NASCENT) ||
                    (monitorConnector.getConnectionState() == ConnectionState.DISCONNECTED)) &&
                    (monitorExceptionCount > 0)) {
                log.error("Monitor not connected. Suspending agent monitor client: " + agentInfo.getName());
                monitorState.setSuspended(true);
            }
        }
    }

    /**
     * Synchronize connector and GWOS monitor inventory.
     */
    private void synchronizeInventory() {
        // return if not connected
        if ((monitorConnector.getConnectionState() != ConnectionState.SEMICONNECTED) &&
                (monitorConnector.getConnectionState() != ConnectionState.CONNECTED)) {
            return;
        }
        final GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
        // migrate application types from older versions
        gwosService.migrateApplicationTypes();
        // compute differences between the GWOS and connector monitor inventories
        MonitorConnector.ValidateHost hostValidator = null;
        if (gwosService.isFeatureEnabled(GwosService.GroundworkFeature.BlackListFilter)) {
            hostValidator = new MonitorConnector.ValidateHost() {
                @Override
                public boolean validateHost(String hostName) {
                    // connector monitor inventory hosts are valid if not blacklisted
                    return !gwosService.isHostNameBlackListed(hostName);
                }
            };
        }
        MonitorInventory connectorInventory = monitorConnector.gatherMonitorInventory(agentInfo, hostValidator);
        try {
            inventoryLock.lock();
            MonitorInventory gwosInventory = gwosService.gatherMonitorInventory(connectorInventory);
            Collection<MonitorInventoryDifference.Difference> differences =
                    MonitorInventoryDifference.difference(gwosInventory, connectorInventory);
            MonitorInventory addGwosInventory = new MonitorInventory(gwosInventory);
            Collection<Object> addEventInventory = new ArrayList<Object>();
            MonitorInventory updateGwosInventory = new MonitorInventory(gwosInventory);
            Collection<Object> updateEventInventory = new ArrayList<Object>();
            MonitorInventory deleteGwosInventory = new MonitorInventory(gwosInventory);
            for (MonitorInventoryDifference.Difference difference : differences) {
                switch (difference.type) {
                    case ADD:
                        switch (difference.inventory) {
                            case HOST:
                                DtoHost dtoHost = connectorInventory.getHosts().get(difference.name);
                                if (difference.statusChanged) {
                                    dtoHost.setMonitorStatus(difference.monitorStatus);
                                    addEventInventory.add(connectorInventory.buildDtoEventInventory(dtoHost));
                                }
                                addGwosInventory.getHosts().put(difference.name, dtoHost);
                                break;
                            case HOST_GROUP:
                                DtoHostGroup dtoHostGroup = connectorInventory.getHostGroups().get(difference.name);
                                addGwosInventory.getHostGroups().put(difference.name, dtoHostGroup);
                                break;
                            case SERVICE:
                                DtoService dtoService = connectorInventory.getServices().get(difference.name);
                                if (difference.statusChanged) {
                                    dtoService.setMonitorStatus(difference.monitorStatus);
                                    addEventInventory.add(connectorInventory.buildDtoEventInventory(dtoService));
                                }
                                addGwosInventory.getServices().put(difference.name, dtoService);
                                break;
                            case SERVICE_GROUP:
                                DtoServiceGroup dtoServiceGroup = connectorInventory.getServiceGroups().get(difference.name);
                                addGwosInventory.getServiceGroups().put(difference.name, dtoServiceGroup);
                                break;
                        }
                        break;
                    case DIFFERENCE:
                        switch (difference.inventory) {
                            case HOST:
                                DtoHost dtoHost = connectorInventory.getHosts().get(difference.name);
                                if (difference.statusChanged) {
                                    dtoHost.setMonitorStatus(difference.monitorStatus);
                                    updateEventInventory.add(connectorInventory.buildDtoEventInventory(dtoHost));
                                    if (difference.notifyStatusChanged) {
                                        updateEventInventory.add(connectorInventory.buildDtoNotificationInventory(dtoHost));
                                    }
                                }
                                updateGwosInventory.getHosts().put(difference.name, dtoHost);
                                break;
                            case HOST_GROUP:
                                DtoHostGroup dtoHostGroup = connectorInventory.getHostGroups().get(difference.name);
                                updateGwosInventory.getHostGroups().put(difference.name, dtoHostGroup);
                                break;
                            case SERVICE:
                                DtoService dtoService = connectorInventory.getServices().get(difference.name);
                                if (difference.statusChanged) {
                                    dtoService.setMonitorStatus(difference.monitorStatus);
                                    updateEventInventory.add(connectorInventory.buildDtoEventInventory(dtoService));
                                    if (difference.notifyStatusChanged) {
                                        updateEventInventory.add(connectorInventory.buildDtoNotificationInventory(dtoService));
                                    }
                                }
                                updateGwosInventory.getServices().put(difference.name, dtoService);
                                break;
                            case SERVICE_GROUP:
                                DtoServiceGroup dtoServiceGroup = connectorInventory.getServiceGroups().get(difference.name);
                                updateGwosInventory.getServiceGroups().put(difference.name, dtoServiceGroup);
                                break;
                        }
                        break;
                    case REMOVE:
                        switch (difference.inventory) {
                            case HOST:
                                DtoHost dtoHost = gwosInventory.getHosts().get(difference.name);
                                deleteGwosInventory.getHosts().put(difference.name, dtoHost);
                                break;
                            case HOST_GROUP:
                                DtoHostGroup dtoHostGroup = gwosInventory.getHostGroups().get(difference.name);
                                deleteGwosInventory.getHostGroups().put(difference.name, dtoHostGroup);
                                break;
                            case SERVICE:
                                DtoService dtoService = gwosInventory.getServices().get(difference.name);
                                deleteGwosInventory.getServices().put(difference.name, dtoService);
                                break;
                            case SERVICE_GROUP:
                                DtoServiceGroup dtoServiceGroup = gwosInventory.getServiceGroups().get(difference.name);
                                deleteGwosInventory.getServiceGroups().put(difference.name, dtoServiceGroup);
                                break;
                        }
                        break;
                }
            }
            // synchronize GWOS monitor inventory
            StringBuilder synchronizeInfoMessage = (log.isInfoEnabled() ? new StringBuilder("Cloudhub synchronized inventory") : null);
            if (!addGwosInventory.isEmpty()) {
                if (log.isInfoEnabled()) {
                    if (!addGwosInventory.getHosts().isEmpty()) {
                        synchronizeInfoMessage.append(" adding ").append(addGwosInventory.getHosts().size()).append(" host(s)") ;
                    }
                    if (!addGwosInventory.getHostGroups().isEmpty()) {
                        synchronizeInfoMessage.append(" adding ").append(addGwosInventory.getHostGroups().size()).append(" host group(s)") ;
                    }
                    if (!addGwosInventory.getServices().isEmpty()) {
                        synchronizeInfoMessage.append(" adding ").append(addGwosInventory.getServices().size()).append(" service(s)") ;
                    }
                    if (!addGwosInventory.getServiceGroups().isEmpty()) {
                        synchronizeInfoMessage.append(" adding ").append(addGwosInventory.getServiceGroups().size()).append(" service group(s)") ;
                    }
                }
                gwosService.addMonitorInventory(addGwosInventory);
                if (!addEventInventory.isEmpty()) {
                    gwosService.modifyEventInventory(addEventInventory);
                }
            }
            if (!updateGwosInventory.isEmpty()) {
                if (log.isInfoEnabled()) {
                    if (!updateGwosInventory.getHosts().isEmpty()) {
                        synchronizeInfoMessage.append(" updating ").append(updateGwosInventory.getHosts().size()).append(" host(s)") ;
                    }
                    if (!updateGwosInventory.getHostGroups().isEmpty()) {
                        synchronizeInfoMessage.append(" updating ").append(updateGwosInventory.getHostGroups().size()).append(" host group(s)") ;
                    }
                    if (!updateGwosInventory.getServices().isEmpty()) {
                        synchronizeInfoMessage.append(" updating ").append(updateGwosInventory.getServices().size()).append(" service(s)") ;
                    }
                    if (!updateGwosInventory.getServiceGroups().isEmpty()) {
                        synchronizeInfoMessage.append(" updating ").append(updateGwosInventory.getServiceGroups().size()).append(" service group(s)") ;
                    }
                }
                gwosService.updateMonitorInventory(updateGwosInventory);
                if (!updateEventInventory.isEmpty()) {
                    gwosService.modifyEventInventory(updateEventInventory);
                }
            }
            if (!deleteGwosInventory.isEmpty()) {
                if (!deleteGwosInventory.getHosts().isEmpty()) {
                    synchronizeInfoMessage.append(" deleting ").append(deleteGwosInventory.getHosts().size()).append(" host(s)") ;
                }
                if (!deleteGwosInventory.getHostGroups().isEmpty()) {
                    synchronizeInfoMessage.append(" deleting ").append(deleteGwosInventory.getHostGroups().size()).append(" host group(s)") ;
                }
                if (!deleteGwosInventory.getServices().isEmpty()) {
                    synchronizeInfoMessage.append(" deleting ").append(deleteGwosInventory.getServices().size()).append(" service(s)") ;
                }
                if (!deleteGwosInventory.getServiceGroups().isEmpty()) {
                    synchronizeInfoMessage.append(" deleting ").append(deleteGwosInventory.getServiceGroups().size()).append(" service group(s)") ;
                }
                gwosService.deleteMonitorInventory(deleteGwosInventory);
            }
            if (log.isInfoEnabled()) {
                synchronizeInfoMessage.append(" for agent monitor client: ").append(agentInfo.getName());
                log.info(synchronizeInfoMessage.toString());
            }
        } finally {
            inventoryLock.unlock();
        }
    }

    /**
     * Delete GWOS monitor inventory.
     */
    private void deleteInventory() {
        try {
            inventoryLock.lock();
            final GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
            // GWMON-12859: delete by agent
            int count = configurationService.countByHostName(configuration.getGwos().getGwosServer());
            DtoOperationResults results = gwosService.deleteByAgent(configuration, count);
            log.info(String.format("Deletion of agent data complete for agent %s : success: %d failure %d ",
                    agentInfo.getName(), results.getSuccessful(), results.getFailed()));
        } catch (Exception e) {
            log.debug("Failed to delete by agent id: " + e.getMessage(), e);
        } finally {
            inventoryLock.unlock();
        }
    }

    /**
     * Suspend GWOS monitor inventory.
     */
    private void suspendInventory() {
        GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
        try {
            inventoryLock.lock();
            switch (monitorConnector.getConnectionState()) {
                case NASCENT:
                case DISCONNECTED:
                    // update status: unreachable monitor hosts, unknown monitor services
                    gwosService.updateMonitorInventoryStatus(agentInfo, MonitorStatusBubbleUp.UNREACHABLE,
                            MonitorStatusBubbleUp.UNKNOWN, new UnreachableStatusMessages());
                    break;
                case CONNECTED:
                case SEMICONNECTED:
                    // update status: suspended monitor hosts, unknown monitor services
                    gwosService.updateMonitorInventoryStatus(agentInfo, MonitorStatusBubbleUp.UNREACHABLE,
                            MonitorStatusBubbleUp.UNKNOWN, new SuspendedStatusMessages());
                    break;
            }
        } finally {
            inventoryLock.unlock();
        }
    }
}
