package org.groundwork.cloudhub.web;

import com.groundwork.collage.model.AuditLog;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.connectors.BaseMonitorConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.groundwork.cloudhub.profile.ConfigServiceState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.groundwork.cloudhub.web.CloudHubUI.FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;

@Controller
public class HomeController extends HostController {

    private static Logger log = Logger.getLogger(HomeController.class);

    @Value("${loadtest.connector.visible}")
    private boolean loadTestConnectorVisible = false;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView navigateHome(HttpSession session) {
        return listAllCloudHubConfigurations(session);
    }

    @RequestMapping(value = "/home/listAllConfigurations", method = RequestMethod.GET)
    public ModelAndView listAllCloudHubConfigurations(HttpSession session) {
        List<WebConnectionConfiguration> webConfigurations = new LinkedList<>();
        try {
            List<ConnectionConfiguration> configurations = filterConfigurations(false);
            CloudhubMonitorAgent agent = null;

            for (ConnectionConfiguration config : configurations) {
                if (!isNetworkConfig(config)) {
                    agent = collectorService.lookup(config.getCommon().getConfigurationFile());
                    if (agent == null || agent.isSuspended()) {
                        config.getCommon().setServerSuspended(true);
                    } else {
                        config.getCommon().setServerSuspended(false);
                    }
                    webConfigurations.add(new WebConnectionConfiguration(config, (agent == null) ? 0 : agent.getMonitorExceptionCount()));
                }
            }

            checkGwVersionInstalled(session);
        } catch (CloudHubException che) {

            log.error("Exception occurred while fetching configurations list", che);

        } catch (Exception ex) {

            log.error("Exception occurred while fetching configurations list", ex);

        }

        Map<String,Object> models = new HashMap<String,Object>();
        models.put("configurations", webConfigurations);
        models.put("loadTestConnectorVisible", loadTestConnectorVisible);
        return new ModelAndView("index", models);
    }

    @RequestMapping(value = "/net", method = RequestMethod.GET)
    public ModelAndView listAllNetHubConfigurations(HttpSession session) {
        List<WebConnectionConfiguration> webConfigurations = new LinkedList<>();
        try {
            List<ConnectionConfiguration> configurations = filterConfigurations(true);
            CloudhubMonitorAgent agent = null;
            for (ConnectionConfiguration config : configurations) {
                agent = collectorService.lookup(config.getCommon().getConfigurationFile());
                if (agent == null || agent.isSuspended()) {
                    config.getCommon().setServerSuspended(true);
                } else {
                    config.getCommon().setServerSuspended(false);
                }
                webConfigurations.add(new WebConnectionConfiguration(config, (agent == null) ? 0 : agent.getMonitorExceptionCount()));
            }

            checkGwVersionInstalled(session);
        } catch (CloudHubException che) {

            log.error("Exception occurred while fetching configurations list", che);

        } catch (Exception ex) {

            log.error("Exception occurred while fetching configurations list", ex);

        }
        return new ModelAndView("nethub", "configurations", webConfigurations);
    }

    protected List<ConnectionConfiguration> filterConfigurations(boolean useNetworks) {
        List<ConnectionConfiguration> connections = configurationService.listAllConfigurations();
        List<ConnectionConfiguration> result = new ArrayList<ConnectionConfiguration>();
        for (ConnectionConfiguration config : connections) {
            boolean isNet = isNetworkConfig(config);
            if (isNet && useNetworks) {
                result.add(config);
            }
            else if (!isNet && !useNetworks) {
                result.add(config);
            }
        }
        return result;
    }

    protected boolean isNetworkConfig(ConnectionConfiguration configuration) {
        switch (configuration.getCommon().getVirtualSystem()) {
            case CISCO:
            case NSX:
            case OPENDAYLIGHT:
                return true;
        }
        return false;
    }

    @RequestMapping(value = "/updateConfiguration", method = RequestMethod.GET)
    public ModelAndView updateConfiguration(@RequestParam("filePath") String filePath, @RequestParam("fileName") String fileName, HttpServletRequest request, HttpSession session) {

        ModelAndView modelAndView = null;
        ConnectionConfiguration connectionConfig = null;

        try {
            connectionConfig = configurationService.readConfiguration(filePath + "/" + fileName);
            connectionConfig.getCommon().setTestConnectionDisabled(false);
            connectionConfig.getCommon().setCreateProfileDisabled(false);
            if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.REDHAT)) {
                ConfigServiceState state = new ConfigServiceState();
                state.setView(ConfigServiceState.ConfigView.ViewStorage, connectionConfig.getCommon().isStorageView());
                state.setView(ConfigServiceState.ConfigView.ViewNetwork, connectionConfig.getCommon().isNetworkView());
                state.setView(ConfigServiceState.ConfigView.ViewPool, connectionConfig.getCommon().isResourcePoolView());
                request.getSession(true).setAttribute(RhevController.CONFIG_STATE, state);
                modelAndView = new ModelAndView("rhev/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.OPENSTACK)) {
                modelAndView = new ModelAndView("openstack/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.OPENSHIFT)) {
                modelAndView = new ModelAndView("openshift/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.DOCKER)) {
                modelAndView = new ModelAndView("docker/create-connection", "configBean", connectionConfig);
                DockerConnection dockerConnection = (DockerConnection)connectionConfig.getConnection();
                request.getSession(true).setAttribute(DockerController.DOCKER_PREFIX_STATE,
                        dockerConnection.getPrefix() == null ? "" : dockerConnection.getPrefix());
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.VMWARE)) {
                ConfigServiceState state = new ConfigServiceState();
                state.setView(ConfigServiceState.ConfigView.ViewStorage, connectionConfig.getCommon().isStorageView());
                state.setView(ConfigServiceState.ConfigView.ViewNetwork, connectionConfig.getCommon().isNetworkView());
                state.setView(ConfigServiceState.ConfigView.ViewPool, connectionConfig.getCommon().isResourcePoolView());
                request.getSession(true).setAttribute(VmWareController.CONFIG_STATE, state);
                modelAndView = new ModelAndView("vmware/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.OPENDAYLIGHT)) {
                modelAndView = new ModelAndView("opendaylight/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.CISCO)) {
                modelAndView = new ModelAndView("cisco/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.AMAZON)) {
                ConfigServiceState state = new ConfigServiceState();
                state.setView(ConfigServiceState.ConfigView.ViewStorage, connectionConfig.getCommon().isStorageView());
                state.setView(ConfigServiceState.ConfigView.ViewNetwork, connectionConfig.getCommon().isNetworkView());
                state.setView(ConfigServiceState.ConfigView.ViewPool, connectionConfig.getCommon().isResourcePoolView());
                state.setView(ConfigServiceState.ConfigView.ViewCustom, connectionConfig.getCommon().isCustomView());
                request.getSession(true).setAttribute(AmazonController.CONFIG_STATE, state);
                modelAndView = new ModelAndView("amazon2/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.LOADTEST)) {
                modelAndView = new ModelAndView("loadtest/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.NETAPP)) {
                ConfigServiceState state = new ConfigServiceState();
                state.setView(ConfigServiceState.ConfigView.ViewStorage, connectionConfig.getCommon().isStorageView());
                state.setView(ConfigServiceState.ConfigView.ViewNetwork, connectionConfig.getCommon().isNetworkView());
                state.setView(ConfigServiceState.ConfigView.ViewPool, connectionConfig.getCommon().isResourcePoolView());
                request.getSession(true).setAttribute(NetAppController.CONFIG_STATE, state);
                modelAndView = new ModelAndView("netapp/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.CLOUDERA)) {
                ConfigServiceState state = new ConfigServiceState();
                state.setView(ConfigServiceState.ConfigView.ViewStorage, connectionConfig.getCommon().isStorageView());
                state.setView(ConfigServiceState.ConfigView.ViewNetwork, connectionConfig.getCommon().isNetworkView());
                state.setView(ConfigServiceState.ConfigView.ViewPool, connectionConfig.getCommon().isResourcePoolView());
                request.getSession(true).setAttribute(ClouderaController.CONFIG_STATE, state);
                modelAndView = new ModelAndView("cloudera/create-connection", "configBean", connectionConfig);
            } else if (connectionConfig.getCommon().getVirtualSystem().equals(VirtualSystem.ICINGA2)) {
                modelAndView = new ModelAndView("icinga2/create-connection", "configBean", connectionConfig);
            } else {
                modelAndView = new ModelAndView("nsx/create-connection", "configBean", connectionConfig);
            }
            request.getSession(true).setAttribute(HOSTNAME_STATE,
                    connectionConfig.getGwos().getGwosServer() == null ? "" : connectionConfig.getGwos().getGwosServer());
            setGwVersion(connectionConfig, session);
            request.setAttribute(DEFAULT_USERNAME, connectionConfig.getGwos().getWsUsername());
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(connectionConfig.getCommon().getVirtualSystem());
            request.setAttribute(DEFAULT_APPLICATION_TYPE, provider.getApplicationType());
        } catch (CloudHubException che) {

            log.error("Exception occurred while navigating to update config", che);

        } catch (Exception ex) {

            log.error("Exception occurred while navigating to update config", ex);

        }

        return modelAndView;
    }

    @RequestMapping(value = "/deleteConfiguration", method = RequestMethod.GET)
    public String deleteConfiguration(@RequestParam("filePath") String filePath, @RequestParam("fileName") String fileName, HttpServletRequest request, HttpSession session,
                                      final RedirectAttributes redirectAttributes) {
        String result = "Configuration deleted successfully!";
        boolean isNetwork = false;
        try {
            ConnectionConfiguration connectionConfig = configurationService.readConfiguration(filePath + "/" + fileName);
            if (connectionConfig != null) {
                isNetwork = isNetworkConfig(connectionConfig);
                String agentIdentifier = connectionConfig.getCommon().getConfigurationFile();
                CloudhubMonitorAgent agent = collectorService.lookup(agentIdentifier);
                if (agent != null) {
                    agent.submitRequestToDeleteMonitoringData();
                    //collectorService.remove(agentIdentifier);
                }
                else {
                    // agent was never started, start it so we can submit a deletion
                    connectionConfig.getCommon().setServerSuspended(true);
                    collectorService.startMonitoringConnection(connectionConfig);
                    agent = collectorService.lookup(agentIdentifier);
                    if (agent != null) {
                        agent.submitRequestToDeleteMonitoringData();
                        Thread.sleep(1000);
                        //collectorService.remove(agentIdentifier);
                    }
                }
                // delete configuration from file system
                configurationService.deleteConfiguration(connectionConfig);
                // delete corresponding profile from file system
                // GWMON-12859: do not delete profiles for connectors which do not support profiles
                ConfigurationProvider provider = connectorFactory.getConfigurationProvider(connectionConfig.getCommon().getVirtualSystem());
                if (provider.supports(ConfigurationProvider.SupportsFeature.Profiles)) {
                    profileService.removeProfile(connectionConfig.getCommon().getVirtualSystem(), connectionConfig.getCommon().getAgentId());
                }

                getGwosService(connectionConfig).auditLogHost(connectionConfig.getCommon().getVirtualSystem(),
                        connectionConfig.getConnection().getHostName(),
                        AuditLog.Action.DELETE.name(),
                        formatCommonConnection("Deleting Hub configuration", connectionConfig.getCommon()),
                        getCurrentUser(request));

            }
        } catch (CloudHubException che) {

            result = "Sorry some problem occurred in deleting the configuration.";
            log.error("Exception occurred navigating deleting vmware connection", che);

        } catch (Exception ex) {

            result = "Sorry some problem occurred in deleting the configuration.";
            log.error("Exception occurred navigating deleting vmware connection", ex);
        }

        request.setAttribute(RESULT, result);
        redirectAttributes.addFlashAttribute(RESULT, result);
        return (isNetwork) ? "redirect:/mvc/net" : "redirect:/mvc/";
    }

    @RequestMapping(value = "/changeServerStatus", method = RequestMethod.GET)
    public
    @ResponseBody
    String changeServerStatus(@RequestParam("fileName") String fileName, @RequestParam("filePath") String filePath, @RequestParam("currentStatus") String currentStatus,
                              HttpServletRequest request) {

        String result = SUCCESS;

        try {
            ConnectionConfiguration connectionConfig = configurationService.readConfiguration(filePath + "/" + fileName);
            GwosService gwosService = getGwosService(connectionConfig);

            if (currentStatus.equalsIgnoreCase("start")) {

                // CLOUDHUB-191: don't allow for untested agents to start
                try {
                    boolean success = gwosService.testConnection(connectionConfig);
                    if (success) {

                        BaseMonitorConnector connector = connectorFactory.getMonitoringConnector(connectionConfig);
                        if (connector == null) {
                            connector = connectorFactory.getMonitorConnector(connectionConfig);
                        }

                        // @since 7.1.1 - force close of connection on stop - needs testing on all connectors
                        if (connector.getConnectionState() == ConnectionState.CONNECTED) {
                            try {
                                connector.disconnect();
                            }
                            catch (Exception disconnectException) {
                                log.error("Exception occurred while disconnectiong virtualization connection from start button", disconnectException);
                            }
                        }

                        connector.connect(connectionConfig.getConnection());

                        CloudhubMonitorAgent agent = collectorService.lookup(fileName);
                        if (agent == null) {
                            collectorService.unsuspend(fileName);
                            collectorService.setConfigurationUpdated(fileName);
                            agent = collectorService.startMonitoringConnection(connectionConfig);
                        }
                        agent.unsuspend();
                        connectionConfig.getCommon().setServerSuspended(false);
                        gwosService.auditLogHost(connectionConfig.getCommon().getVirtualSystem(),
                                connectionConfig.getConnection().getHostName(), AuditLog.Action.ENABLE.name(),
                                "Starting Hub Agent " + connectionConfig.getCommon().getConfigurationFile(), getCurrentUser(request));
                    }
                    else {
                        return "Unknown error testing connection. Please test connection from configuration screen";
                    }
                } catch (ConnectorException vex) {
                    log.error("Exception occurred while testing virtualization connection from start button", vex);
                    result = "Virtualization error: " + vex.getMessage();
                    return result;
                } catch (CloudHubException cex) {
                    log.error("Exception occurred while testing GW Server connection from start button", cex);
                    result = "GW Server error: " + cex.getMessage();
                    return result;
                }
                catch (Exception e) {
                        log.error("Exception occurred while testing connection from start button", e);
                        result = e.getMessage();
                        return result;
                }
            } else {   // stopping connector
                    CloudhubMonitorAgent agent = collectorService.lookup(fileName);
                if (agent == null) {
                    collectorService.unsuspend(fileName);
                    collectorService.setConfigurationUpdated(fileName);
                    agent = collectorService.startMonitoringConnection(connectionConfig);
                }
                agent.submitRequestToSuspend();
                connectionConfig.getCommon().setServerSuspended(true);
                gwosService.auditLogHost(connectionConfig.getCommon().getVirtualSystem(),
                        connectionConfig.getConnection().getHostName(), AuditLog.Action.DISABLE.name(),
                        "Suspending Hub Agent " + connectionConfig.getCommon().getConfigurationFile(), getCurrentUser(request));
            }
            configurationService.saveConfiguration(connectionConfig);
        } catch (CloudHubException che) {

            result = FAILURE;
            log.error("Exception occurred changing monitor status", che);

        } catch (Exception ex) {

            result = FAILURE;
            log.error("Exception occurred changing monitor status", ex);
        }

        return result;
    }

}