package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.ConfigurationServiceImpl;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.foundation.ws.impl.ConfigurationDirectoryWatcher;
import org.groundwork.foundation.ws.impl.DirectoryWatcherNotificationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by dtaylor on 6/15/15.
 */
@Service(MonitorDeploymentListener.NAME)
public class MonitorDeploymentListener implements DirectoryWatcherNotificationListener {

    public static final String NAME = "MonitorDeploymentListener";
    protected static final String FAILED_FILE_EXTENSION = ".failed";

    private static Logger log = Logger.getLogger(MonitorDeploymentListener.class);

    @Resource(name= ConfigurationService.NAME)
    ConfigurationService configurationService;

    @Resource(name= ProfileService.NAME)
    ProfileService profileService;

    @Resource(name= MonitorAgentCollector.NAME)
    MonitorAgentCollector collector;

    @PostConstruct
    private void start() {
        // check for any files existing prior to starting directory watcher
        // deploy them if necessary
        File folder = new File(ConfigurationServiceImpl.CONFIG_DEPLOY_PATH);
        for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
                if (!configurationService.matchConfigurationFileName(file.getName())) {
                    if (log.isInfoEnabled()) {
                        log.info("Ignoring non-configuration file: " + file.toString());
                    }
                    continue;
                }
                processOneDeployment(file.toPath());
            }
        }
        ConfigurationDirectoryWatcher.registerListener(this, ConfigurationServiceImpl.CONFIG_DEPLOY_PATH);
    }

    @PreDestroy
    public void shutdown() {
        if (log.isInfoEnabled()) {
            log.info("shutting down Monitor Deployment Listener and directory watcher for Cloudhub ...");
        }
        ConfigurationDirectoryWatcher.unregisterListener(this, ConfigurationServiceImpl.CONFIG_DEPLOY_PATH);
        if (log.isInfoEnabled()) {
            log.info(String.format("Shutdown complete of Monitor Deployment Listener and directory watcher for Cloudhub"));
        }
    }

    @Override
    public void notifyChange(Path path, WatchEvent.Kind<Path> kind) {

        if (!configurationService.matchConfigurationFileName(path.getFileName().toString())) {
            if (log.isInfoEnabled()) {
                log.info("Ignoring non-configuration file: " + path.toString());
            }
            return;
        }

        if (kind == ENTRY_CREATE) {
            if (log.isInfoEnabled()) {
                log.info("Detected New agent for configuration : " + path.getFileName());
            }
        } else if (kind == ENTRY_MODIFY) {
            if (log.isInfoEnabled()) {
                log.info("Detected Modify agent for configuration : " + path.getFileName());
            }
        } else if (kind == ENTRY_DELETE) {
            if (log.isInfoEnabled()) {
                log.info("Detected deletion of agent deployment file for configuration : " + path.getFileName());
            }
            return;
        } else {
            if (log.isInfoEnabled()) {
                log.info("Detected unknown change of agent deployment file for configuration : " + path.getFileName());
            }
            return;
        }
        processOneDeployment(path);
    }

    protected void processOneDeployment(Path path) {
        // Read in configuration, check for agent id
        ConnectionConfiguration config = null;
        String deployPath = ConfigurationServiceImpl.CONFIG_DEPLOY_PATH + path.getFileName().toString();
        try {
            config = configurationService.readConfiguration(deployPath);
        } catch (CloudHubException e) {
            log.error("Auto Deploy Failure: reading configuration deployment: " + deployPath, e);
            moveToFailedFile(deployPath);
            return;
        }
        if (config == null) {
            log.error("Auto Deploy Failure: unknown error reading configuration deployment: " + deployPath);
            return;
        }
        try {
            VirtualSystem virtualSystem = configurationService.extractVirtualSystemFromFilename(deployPath);
            if (virtualSystem == null) {
                log.error("Auto Deploy Failure: unknown virtual system on fileName: " + deployPath);
                moveToFailedFile(deployPath);
                return;
            }

            String reuseAgent = config.getCommon().getAgentId();
            if (!StringUtils.isEmpty(reuseAgent) && StringUtils.isUUID(reuseAgent)) {
                CloudhubMonitorAgent agent = collector.lookup(reuseAgent);
                if (agent != null) {
                    // try to reuse agent
                    if (log.isInfoEnabled()) {
                        log.info("Re-establishing deployed agent: " + path.toString());
                    }
                    CloudhubAgentInfo agentInfo = (CloudhubAgentInfo)agent.getAgentInfo();
                    if (configurationService.doesConfigurationExist(agentInfo.getConfigurationPath())) {
                        ConnectionConfiguration existing = configurationService.readConfiguration(agentInfo.getConfigurationPath());
                        config.getCommon().setPathToConfigurationFile(existing.getCommon().getPathToConfigurationFile());
                        config.getCommon().setConfigurationFile(existing.getCommon().getConfigurationFile());
                        synchUiFields(config, deployPath);
                        configurationService.saveConfiguration(config);
                        if (config.getCommon().isServerSuspended() && !agent.isSuspended()) {
                            agent.suspend();
                        } else if (!config.getCommon().isServerSuspended() && agent.isSuspended()) {
                            agent.unsuspend();
                        }
                        collector.setConfigurationUpdated(path.toString());
                        return;
                    }
                }
            }
            String newAgentId = (!StringUtils.isEmpty(reuseAgent) && StringUtils.isUUID(reuseAgent)) ? reuseAgent : configurationService.generateAgentId();
            config.getCommon().setVirtualSystem(virtualSystem);
            config.getCommon().setAgentId(newAgentId);
            config.getCommon().setPathToConfigurationFile("");
            config.getCommon().setConfigurationFile("");
            synchUiFields(config, deployPath);
            configurationService.saveConfiguration(config);
            collector.startMonitoringConnection(config);
        }
        catch (Exception e) {
            log.error("Auto Deploy Failure: saving or starting configuration deployment: " + path.toString(), e);
        }
        finally {
            removeDeployedFile(deployPath);
        }
    }

    protected void synchUiFields(ConnectionConfiguration config, String deployPath) {
        try {
            if (StringUtils.isEmpty(config.getCommon().getDisplayName())) {
                config.getCommon().setDisplayName("Anonymous Import " + config.getCommon().getVirtualSystem().toString());
            }
            config.getCommon().setUiCheckIntervalMinutes(Integer.toString(config.getCommon().getCheckIntervalMinutes()));
            config.getCommon().setUiSyncIntervalMinutes(Integer.toString(config.getCommon().getSyncIntervalMinutes()));
            config.getCommon().setUiComaIntervalMinutes(Integer.toString(config.getCommon().getComaIntervalMinutes()));
            config.getCommon().setUiConnectionRetries(Integer.toString(config.getCommon().getConnectionRetries()));
        }
        catch (Exception e) {
            log.error("Auto Deploy Failure: failed to sync UI field for " + deployPath, e);
        }
    }

    protected void moveToFailedFile(String configPath) {
        try {
            Files.move(new File(configPath).toPath(), new File(configPath + FAILED_FILE_EXTENSION).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            log.error("Auto Deploy Failure: moving configuration deployment to failed file: " + configPath);
        }
    }

    protected void removeDeployedFile(String configPath) {
        try {
            File fileToDelete = new File(configPath);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        }
        catch (Exception e) {
            log.error("Auto Deploy Failure: deleting configuration deployment to failed file: " + configPath);
        }
    }


}
