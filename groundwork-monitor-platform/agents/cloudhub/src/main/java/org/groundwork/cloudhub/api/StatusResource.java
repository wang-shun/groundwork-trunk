package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.MonitorAgentInfo;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoConnectorStatus;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/status")
public class StatusResource extends BaseCloudHubResource {

    protected static Logger log = Logger.getLogger(StatusResource.class);

    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Error: Connector %s not found";
    protected static final String CONFIG_READ_ERROR_MESSAGE = "Error: Connector %s could not be read";

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    DtoConnectorStatus connectorsStatusCheck(@RequestParam("name") String name) {
        try {
            CloudhubMonitorAgent agent = collectorService.lookup(name);
            if (agent == null) {
                ConnectionConfiguration configuration = configurationService.readConfiguration("/usr/local/groundwork/config/cloudhub/" + name);
                if (configuration == null) {
                    throw new CloudHubException(String.format(CONFIG_NOT_FOUND_MESSAGE, name));
                }
                return getConnectorStatusFromConfiguration(name, configuration);
            }
            return new DtoConnectorStatus()
                            .name(agent.getAgentInfo().getName())
                            .displayName(agent.getConfiguration().getCommon().getDisplayName())
                            .agentId(agent.getAgentInfo().getAgentId())
                            .applicationType(agent.getAgentInfo().getApplicationType())
                            .connectorType(agent.getAgentInfo().getConnectorName())
                            .isSuspended(agent.isSuspended())
                            .connectionState(agent.getConnectionState())
                            .groundworkServer(agent.getConfiguration().getGwos().getGwosServer())
                            .mergeHosts(agent.getConfiguration().getGwos().isMergeHosts())
                            .checkIntervalMinutes(agent.getConfiguration().getCommon().getCheckIntervalMinutes())
                            .connectionRetries(agent.getConfiguration().getCommon().getConnectionRetries())
                            .monitorServer(agent.getConfiguration().getConnection().getServer())
                            .lastError(agent.getAgentInfo().getLastError())
                            .errors(agent.getAgentInfo().getAllErrors())
                            .groundworkExceptionCount(agent.getGroundworkExceptionCount())
                            .monitorExceptionCount(agent.getMonitorExceptionCount());
        } catch (Exception e) {
            log.error("Exception occurred while retrieving CloudHub status", e);
            throw new CloudHubException(e.getMessage(), e);
        }

    }

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<DtoConnectorStatus> allConnectorsStatusCheck() {
        try {
            List<DtoConnectorStatus> result = new ArrayList<>();
            Set<String> collectorNames = new HashSet<>();
            for (CloudhubMonitorAgent agent :  collectorService.list()) {
                result.add(
                        new DtoConnectorStatus()
                                .name(agent.getAgentInfo().getName())
                                .displayName(agent.getConfiguration().getCommon().getDisplayName())
                                .agentId(agent.getAgentInfo().getAgentId())
                                .applicationType(agent.getAgentInfo().getApplicationType())
                                .connectorType(agent.getAgentInfo().getConnectorName())
                                .isSuspended(agent.isSuspended())
                                .connectionState(agent.getConnectionState())
                                .groundworkServer(agent.getConfiguration().getGwos().getGwosServer())
                                .mergeHosts(agent.getConfiguration().getGwos().isMergeHosts())
                                .checkIntervalMinutes(agent.getConfiguration().getCommon().getCheckIntervalMinutes())
                                .connectionRetries(agent.getConfiguration().getCommon().getConnectionRetries())
                                .monitorServer(agent.getConfiguration().getConnection().getServer())
                                .lastError(agent.getAgentInfo().getLastError())
                                .errors(agent.getAgentInfo().getAllErrors())
                                .groundworkExceptionCount(agent.getGroundworkExceptionCount())
                                .monitorExceptionCount(agent.getMonitorExceptionCount()));
                collectorNames.add(agent.getAgentInfo().getName());
            }
            // check for any uninitialized connectors
            List<ConnectionConfiguration> unintializedConfigs = configurationService.listAllConfigurations();
            for (ConnectionConfiguration config : unintializedConfigs) {
                if (!collectorNames.contains(config.getCommon().getConfigurationFile())) {
                    result.add(getConnectorStatusFromConfiguration(config.getCommon().getConfigurationFile(), config));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Exception occurred while retrieving CloudHub status", e);
            throw new CloudHubException(e.getMessage(), e);
        }
    }

    /**
     * Unsuspends the connector, starting metric connection on the agent thread
     *
     * @param name can be either an agent name like cloudhub-vmware-3.xml, or a agent UUID
     * @return a DtoApiResultResponse with success flag, general result message and optional error message
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    DtoApiResultResponse startConnector(@RequestParam("name") String name) {
        ConnectionConfigurationResult result = findConnectionConfiguration(name);
        if (!result.success()) {
            return result.dto;
        }
        CloudhubMonitorAgent agent = collectorService.lookup(name);
        // save the configuration
        ConnectionConfiguration configuration = result.configuration;
        configuration.getCommon().setServerSuspended(false);
        configurationService.saveConfiguration(configuration);
        if (agent == null) {
            agent = collectorService.startMonitoringConnection(configuration);
        }
        else {
            agent.setConfigurationUpdated();
        }
        agent.unsuspend();
        return new DtoApiResultResponse().setResult("Connector " + name + " has been started");
    }

    /**
     * Unsuspends the connector, starting metric connection on the agent thread
     *
     * @param name can be either an agent name like cloudhub-vmware-3.xml, or a agent UUID
     * @return a DtoApiResultResponse with success flag, general result message and optional error message
     */
    @RequestMapping(value = "/stop", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    DtoApiResultResponse stopConnector(@RequestParam("name") String name) {
        ConnectionConfigurationResult result = findConnectionConfiguration(name);
        if (!result.success()) {
            return result.dto;
        }
        CloudhubMonitorAgent agent = collectorService.lookup(name);
        // save the configuration
        ConnectionConfiguration configuration = result.configuration;
        configuration.getCommon().setServerSuspended(true);
        configurationService.saveConfiguration(configuration);
        if (agent != null) {
            agent.setConfigurationUpdated();
            agent.suspend();
        }
        return new DtoApiResultResponse().setResult("Connector " + name + " has been stopped");
    }

    private ConnectionConfigurationResult findConnectionConfiguration(String name) {
        ConnectionConfiguration configuration = null;
        try {
            Boolean isAgentId = StringUtils.isUUID(name);
            if (isAgentId) {
                List<ConnectionConfiguration> configurations = configurationService.listAllConfigurations();
                for (ConnectionConfiguration cc : configurations) {
                    String configId = cc.getCommon().getAgentId();
                    if (configId != null && configId.equals(name)) {
                        configuration = cc;
                        break;
                    }
                }
                if (configuration == null) {
                    return new ConnectionConfigurationResult(new DtoApiResultResponse(String.format(CONFIG_NOT_FOUND_MESSAGE, name)), null);
                }
            } else {
                String configurationPath = DEFAULT_CONFIG_PATH + "/" + name;
                configuration = configurationService.readConfiguration(configurationPath);
            }
            return new ConnectionConfigurationResult(null, configuration);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return new ConnectionConfigurationResult(new DtoApiResultResponse(String.format(CONFIG_READ_ERROR_MESSAGE, name)), null);
        }
    }

    private static class ConnectionConfigurationResult {
        private DtoApiResultResponse dto;
        private ConnectionConfiguration configuration;

        private ConnectionConfigurationResult(DtoApiResultResponse dto, ConnectionConfiguration connectionConfiguration) {
            this.dto = dto;
            this.configuration = connectionConfiguration;
        }

        private boolean success() {
            return dto == null;
        }
    }

    private DtoConnectorStatus getConnectorStatusFromConfiguration(String name, ConnectionConfiguration configuration) {
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
        String applicationType, connectorType;
        if (provider != null) {
            applicationType = provider.getApplicationType();
            connectorType = provider.getConnectorName();
        }
        else {
            applicationType = configuration.getCommon().getApplicationType();
            connectorType = configuration.getCommon().getApplicationType();
        }
        return new DtoConnectorStatus().name(name)
                .displayName(configuration.getCommon().getDisplayName())
                .agentId(configuration.getCommon().getAgentId())
                .applicationType(applicationType)
                .connectorType(connectorType)
                .isSuspended(true)
                .connectionState(ConnectionState.NASCENT)
                .groundworkServer(configuration.getGwos().getGwosServer())
                .mergeHosts(configuration.getGwos().isMergeHosts())
                .checkIntervalMinutes(configuration.getCommon().getCheckIntervalMinutes())
                .connectionRetries(configuration.getCommon().getConnectionRetries())
                .monitorServer(configuration.getConnection().getServer())
                //.lastError(agentx.getAgentInfo().getLastError())
                .errors(new ArrayList<MonitorAgentInfo.ErrorInfo>())
                .groundworkExceptionCount(0)
                .monitorExceptionCount(0);
    }
}
