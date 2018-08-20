package org.groundwork.cloudhub.api;

import com.groundwork.collage.model.AuditLog;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.GroupServiceInfo;
import org.groundwork.agents.monitor.GroupedServices;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.api.dto.*;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.DiscoveryConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.groundwork.cloudhub.monitor.ConnectorMonitor;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.cloudhub.synthetics.SyntheticContext;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.groundwork.cloudhub.web.HostController;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Excludes;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.ProfileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dtaylor on 5/31/17.
 */
public abstract class BaseCloudHubResource extends HostController {

    public static final String DEFAULT_CONFIG_PATH = "/usr/local/groundwork/config/cloudhub";
    public static final String STORAGE_VIEW = "storageView";
    public static final String NETWORK_VIEW = "networkView";
    public static final String RESOURCE_POOL_VIEW = "resourcePoolView";
    public static final String CUSTOM_VIEW = "customView";

    @Autowired
    protected Synthetics synthetics;

    /**
     * Global CloudHub Rest API Resource Exception Handler
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public DtoApiResultResponse handle(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            message = ExceptionUtils.getRootCauseMessage(e);
        }
        DtoApiResultResponse errResponse = new DtoApiResultResponse(message);
        return errResponse;
    }


    protected ConnectionConfiguration readConnectionConfiguration(VirtualSystem virtualSystem,
                                                                      String filePath, String fileName,
                                                                      HttpServletRequest request, HttpSession session) {
        try {
            ConnectionConfiguration configuration;
            if (fileName.isEmpty() || fileName.equals("undefined")) {
                // create a new configuration template
                configuration = configurationService.createConfiguration(virtualSystem);
                setGwVersion(configuration, session);
                setConfigDefaultsByVersion(configuration, request);
            } else {
                configuration = configurationService.readConfiguration(filePath + "/" + fileName);
            }
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
            boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
            return configuration;
        } catch (Exception ex) {
            log.error("Exception occurred while navigating to update config", ex);
            throw new CloudHubException(ex.getMessage(), ex);
        }
    }

    protected DtoApiSaveResultResponse writeConnectionConfiguration(VirtualSystem virtualSystem,
                                                                    ConnectionConfiguration configuration,
                                                                    HttpServletRequest request,
                                                                    DtoConfigurationState state
                                                                    ) {
        return writeConnectionConfiguration(virtualSystem, configuration, request, state, null);
    }

    protected DtoApiSaveResultResponse writeConnectionConfiguration(VirtualSystem virtualSystem,
                                                                    ConnectionConfiguration configuration,
                                                                    HttpServletRequest request,
                                                                    DtoConfigurationState state,
                                                                    String hostSpecialCase) {
        SaveConfigurationResult saveResult = saveConfiguration(configuration, request);
        if (!saveResult.isSuccess()) {
            return new DtoApiSaveResultResponse(saveResult.getMessage());
        }

        // Did the user change the cluster prefix?
        boolean prefixNameChangeProcessed = false;
        if (state.getPrefixServiceNamesChanged()) {
            if (!saveResult.isNew()) {
                if (saveResult.getOldConfig().getCommon().isPrefixServiceNames() && !configuration.getCommon().isPrefixServiceNames() ||
                        !saveResult.getOldConfig().getCommon().isPrefixServiceNames() && configuration.getCommon().isPrefixServiceNames()) {
                    // transition from non-prefixed to prefixed or prefixed to non-prefixed, either way they are all deleted
                    MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
                    List <String> allServices = new ArrayList<>();
                    if (connector instanceof DiscoveryConnector) {
                        DiscoveryConnector discoveryConnector = (DiscoveryConnector) connector;
                        allServices.addAll(discoveryConnector.listServices(configuration.getConnection()));
                    }
                    deleteResourceViewMetrics(virtualSystem, allServices, configuration, getCurrentUser(request), true, hostSpecialCase);
                    prefixNameChangeProcessed = true;
                }
            }
        }

        // Did the user disable any Service views? Add or remove views and their services ...
        if (prefixNameChangeProcessed == false && state.getViewsRemoved().size() > 0) {
            deleteResourceViewMetrics(virtualSystem, state.getViewsRemoved(), configuration, getCurrentUser(request), false, hostSpecialCase);
        }

        // did the monitor status change from monitoring to non-monitoring? if yes, we have to delete the connector host and its services
        // but only delete the host if its not owned by NAGIOS and not referenced by other connectors
        boolean submittedOnce = false;
        if (state.getMonitorChanged() && configuration.getGwos().getMonitor() == false && !saveResult.isNew()) {
            submittedOnce = submitDeleteMonitoredHost(configuration, saveResult, request);
        }

        // did the GWOS Server change, and are we monitoring the connector? If yes, delete the host (if we own it)
        if (submittedOnce == false && state.getGwosServerChanged() && !saveResult.isNew()) {
            submittedOnce = submitDeleteMonitoredHost(configuration, saveResult, request);
        }
            // did the display name change? need to delete services for connector monitoring
        if (submittedOnce == false && state.getDisplayNameChanged() && !saveResult.isNew()) {
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                submitDeleteServices(agent, saveResult, request);
            }
        }

        if (state.getHostPrefixChanged() && !saveResult.isNew()) {
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                submitRequestToRenameHosts(agent, saveResult, request);
            }
        }
        return new DtoApiSaveResultResponse(saveResult.getNewConfig());
    }

    private boolean submitDeleteMonitoredHost(ConnectionConfiguration configuration, SaveConfigurationResult saveResult, HttpServletRequest request) {
        boolean submittedOnce = false;
        CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
        if (agent != null) {
            String hostName = configuration.getGwos().getGwosServer();
            agent.submitRequestToDeleteConnectorHost(new MonitorChangeState(hostName));
            submitDeleteServices(agent, saveResult, request);
            submittedOnce = true;
        }
        return submittedOnce;
    }

    private void submitDeleteServices(CloudhubMonitorAgent agent,SaveConfigurationResult saveResult, HttpServletRequest request) {
        String serviceToDelete = ConnectorMonitor.buildServiceName((CloudhubAgentInfo)agent.getAgentInfo(), saveResult.getOldConfig());
        List<GroupServiceInfo> services = new ArrayList<>();
        services.add(new GroupServiceInfo(serviceToDelete));
        GroupedServices groupsDeleted = new GroupedServices(services);
        agent.submitRequestToDeleteServices(new MonitorChangeState(groupsDeleted, getCurrentUser(request)));
    }

    private void submitRequestToRenameHosts(CloudhubMonitorAgent agent, SaveConfigurationResult saveResult, HttpServletRequest request) {
        if (saveResult.getOldConfig() == null) {
            return;
        }
        if (saveResult.getOldConfig() instanceof DockerConfiguration) {
            String oldPrefix = ((DockerConnection)saveResult.getOldConfig().getConnection()).getPrefix();
            String newPrefix = ((DockerConnection)saveResult.getNewConfig().getConnection()).getPrefix();
            if (newPrefix != null && !newPrefix.equals(oldPrefix)) {
                agent.submitRequestToRenameHosts(agent.getAgentInfo().getAgentId(), oldPrefix, newPrefix);
            }
        }

    }


    /**
     * Core logic to save a configuration. Common to all Rest connectors
     *
     * @param configBean
     * @param request
     * @return on success returns an empty string. on error returns an error message
     * @throws CloudHubException
     */
    protected SaveConfigurationResult saveConfiguration(ConnectionConfiguration configBean, HttpServletRequest request) throws CloudHubException {
        boolean isNew = false;
        ConnectionConfiguration oldConfig = null;
        try {
            String filePath = configBean.getCommon().getPathToConfigurationFile();
            if (filePath == null || filePath.trim().length() == 0) {
                isNew = true;
            }
            if (!isNew) {
                oldConfig = configurationService.readConfiguration(configBean.getCommon().getPathToConfigurationFile() +
                        configBean.getCommon().getConfigurationFile());
            }
            ConnectionConfiguration newConfiguration = configurationService.saveConfiguration(configBean);
            createNewProfile(configBean);

            String configName = configBean.getCommon().getConfigurationFile();
            if (!StringUtils.isEmpty(configName)) {
                CloudhubMonitorAgent agent = collectorService.lookup(configName);
                if (agent != null) {
                    agent.setConfigurationUpdated();
                }
            }
            String diffMessage = "(none)";
            diffMessage = (isNew) ? diffAddConfiguration(configBean) : diffModifyConfiguration(oldConfig, configBean);
            String message = trimAuditMessage(formatCommonConnection("Hub config", configBean.getCommon()) + " - " + diffMessage);
            getGwosService(configBean).auditLogHost(configBean.getCommon().getVirtualSystem(),
                    configBean.getConnection().getHostName(),
                    (isNew) ? AuditLog.Action.ADD.name() : AuditLog.Action.MODIFY.name(),
                    message,
                    getCurrentUser(request));
            return new SaveConfigurationResult(isNew, oldConfig, newConfiguration, "");
        } catch (Exception e) {
            String info = "Sorry Configuration could not be saved";
            log.error("Exception occurred while saving connection", e);
            String message = (e.getMessage() == null) ? info : info + ": " + e.getMessage();
            return new SaveConfigurationResult(isNew, oldConfig, null, message);
        }
    }

    protected DtoProfileView readMetrics(String filePath, String fileName, String profileId, VirtualSystem virtualSystem, boolean useServices) {
        if (fileName.isEmpty()) {
            throw new CloudHubException("A configuration name parameter must be provided");
        }
        String configPath = filePath + "/" + fileName;
        try {
            ConnectionConfiguration configuration = configurationService.readConfiguration(configPath);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
            boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
            CloudHubProfile localCloudHubProfile = profileService.readCloudProfile(virtualSystem, profileId);
            CloudHubProfile remoteCloudHubProfile = profileService.readRemoteCloudProfile(virtualSystem, configuration.getGwos());
            CloudHubProfile merged = null;
            // merge the profiles
            if (localCloudHubProfile == null && remoteCloudHubProfile == null) {
                merged = profileService.createCloudProfile(virtualSystem, configuration.getCommon().getAgentId());
            } else {
                if (remoteCloudHubProfile == null) {
                    throw new CloudHubException("Remote profile not found");
                } else if (localCloudHubProfile == null) {
                    merged = profileService.mergeCloudProfiles(virtualSystem, remoteCloudHubProfile, localCloudHubProfile);
                } else {
                    // NOTE: with Discovery Connectors, we are no longer merging from remote on existing configs, since we support Metric delete operation from UI
                    return new DtoProfileView(localCloudHubProfile, configuration.getCommon(), isConnected, useServices, getMetricViewDisplayNames(localCloudHubProfile));
                }
            }
            return new DtoProfileView(merged, configuration.getCommon(), isConnected, useServices, getMetricViewDisplayNames((localCloudHubProfile == null) ? merged : localCloudHubProfile));
        } catch (Exception e) {
            log.error("Failed to read profile", e);
            throw new CloudHubException(e.getMessage(), e);
        }
    }

    public DtoApiResultResponse writeMetrics(VirtualSystem virtualSystem, DtoProfileView profileView, HttpServletRequest request) {
        try {
            CloudHubProfile previousProfile = profileService.readCloudProfile(virtualSystem, profileView.getAgent());
            CloudHubProfile cloudHubProfile = profileView.mergeToProfile(profileService.createCloudProfile(virtualSystem, profileView.getAgent()));
            if (previousProfile != null && previousProfile.getExcludes() != null && cloudHubProfile.getExcludes() == null) {
                cloudHubProfile.setExcludes(new Excludes());
                for (String exclude : previousProfile.getExcludes().getExcludes()) {
                    cloudHubProfile.getExcludes().addExclude(exclude);
                }
            }
            profileService.saveProfile(cloudHubProfile);
            ConnectionConfiguration configuration = configurationService.readConfiguration(profileView.getConfigFilePath() + "/" + profileView.getConfigFileName());
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }
            processDeletedMetrics(previousProfile, cloudHubProfile, configuration, getCurrentUser(request));
        } catch (Exception e) {
            log.error("Exception occurred while saving cloudhub profile", e);
            throw new CloudHubException(e.getMessage(), e);
        }
        return new DtoApiResultResponse();
    }

    public  Map<String, Number> extractExpressionVariables(Synthetics synthetics,
                                                           VirtualSystem virtualSystem,
                                                           String expression, String inputType,
                                                           String serviceType, String profileId) {
        CloudHubProfile profile = profileService.readCloudProfile(virtualSystem, profileId);
        if (profile == null) {
            throw new CloudHubException("Profile not found");
        }
        Map<String,Number> result = new HashMap<>();
        List<String> variables = synthetics.extractVariables(expression);
        if (variables.size() == 0) {
            return result;
        }
        if (inputType.equalsIgnoreCase("override")) {
            for (String var : variables) {
                result.put(var, -1);
            }
            return result;
        }
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(virtualSystem);
        List<Metric> metrics = (provider.isPrimaryMetric(serviceType)) ? profile.getHypervisor().getMetrics() : profile.getVm().getMetrics();
        for (String var : variables) {
            Metric metric = findMetric(metrics, var);
            if (metric != null) {
                result.put(var, (inputType.equalsIgnoreCase("critical") ? metric.getCriticalThreshold() : metric.getWarningThreshold()));
            }
        }
        return result;
    }

    /**
     * Test GWOS Connection, same for all Rest Connectors
     *
     * @param configuration
     * @return
     */
    public DtoApiResultResponse testGWOSConnection(ConnectionConfiguration configuration) {
        String message = "";
        try {
            GwosService gwosService = getGwosService(configuration);
            boolean success = gwosService.testConnection(configuration);
            if (!success) throw new CloudHubException("GWOS Server not available");
        } catch (Exception e) {
            message = "Could not connect to Groundwork server: " + e.getMessage();
            log.error("GWOS Connecting failed " + configuration.getCommon().getApplicationType() + ": " + e.getMessage(), e);
        }
        return new DtoApiResultResponse(message);
    }

    /**
     * Test Connector Connection, same for all Rest Connectors
     *
     * @param configuration
     * @return
     */
    public DtoApiResultResponse testConnectorConnection(ConnectionConfiguration configuration) {
        String message = "";
        try {
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
            connector.testConnection(configuration.getConnection());
        } catch (Exception e) {
            message = "Connector could not connect: " + e.getMessage();
            log.error("Connecting failed " + configuration.getCommon().getApplicationType() + ": " + e.getMessage(), e);
        }
        return new DtoApiResultResponse(message);
    }

    protected List<String> buildDeleteServicesList(List<String> deleteServices, List<Metric> metrics, List<String> serviceTypes) {
        for (Metric metric : metrics) {
            if (metric.getServiceType() != null && serviceTypes.contains(metric.getServiceType())) {
                deleteServices.add(metric.getServiceName());
            }
        }
        return deleteServices;
    }

    /**
     * Submit removal of any deleted metrics determined by Metrics UI
     * This method lets the client side calculate the deleted list to submit.
     * It is deprecated in favor of calculating the deleted list on the server side
     * @param profileView
     * @param configuration
     * @param currentUser
     * @Deprecated
     */
    protected void processDeletedMetricsFromClient(DtoProfileView profileView, ConnectionConfiguration configuration, String currentUser) {
        if (null == profileView.getState()) return;
        if (null == profileView.getState().getMetricsRemoved()) return;

        if (profileView.getState().getMetricsRemoved().size() > 0) {
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
                GroupedServices groupsDeleted = new GroupedServices(convertDtoToGroup(profileView.getState().getMetricsRemoved(), provider));
                agent.submitRequestToDeleteServices(new MonitorChangeState(groupsDeleted, currentUser));
            }
        }
    }

    /**
     * Submit removal of any deleted metrics determined by Metrics UI
     * Any metrics that were in the previous list state, but not in the current list state are considered deleted
     * This algorithm uses Metric.getServiceName to determine the service(metric) name, which will first look at
     * metric.getCustomName, and then fallback to metric.getName if custom name is empty
     *
     * @param previousProfile
     * @param currentProfile
     * @param configuration
     * @param currentUser
     * @Deprecated
     */
    protected void processDeletedMetrics(CloudHubProfile previousProfile, CloudHubProfile currentProfile, ConnectionConfiguration configuration, String currentUser) {
        if (previousProfile == null) {
            return;
        }
        List<DtoMetricRemoved> removes = new ArrayList<>();
        Map<String,Map<String,DtoMetricRemoved>> previous = buildServiceTypeNameMap(previousProfile.getHypervisor().getMetrics());
        Map<String,Map<String,DtoMetricRemoved>> current = buildServiceTypeNameMap(currentProfile.getHypervisor().getMetrics());
        addToDeleteList(previous, current, removes);
        previous = buildServiceTypeNameMap(previousProfile.getVm().getMetrics());
        current = buildServiceTypeNameMap(currentProfile.getVm().getMetrics());
        addToDeleteList(previous, current, removes);
        if (removes.size() > 0) {
            if (log.isInfoEnabled()) {
                log.info("Queued up for metrics to delete: " + removes);
            }
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
                GroupedServices groupsDeleted = new GroupedServices(convertDtoToGroup(removes, provider));
                agent.submitRequestToDeleteServices(new MonitorChangeState(groupsDeleted, currentUser));
            }
        }
    }


    /**
     * Adds unmatched metrics not found in current profile but found in previous profile to the delete list
     * Any metrics that were in the previous list, but not in the current list are considered deleted
     *
     * @param previous the previous state
     * @param current the current state
     * @param removes modified list of metrics to be removed
     */
    protected void addToDeleteList( Map<String,Map<String,DtoMetricRemoved>> previous,
                                    Map<String,Map<String,DtoMetricRemoved>> current, List<DtoMetricRemoved> removes) {
        for (String serviceType : previous.keySet()) {
            Map<String,DtoMetricRemoved> prevTypeMap = previous.get(serviceType);
            Map<String,DtoMetricRemoved> currentTypeMap = current.get(serviceType);
            if (currentTypeMap != null) {
                for (String serviceName : prevTypeMap.keySet()) {
                    DtoMetricRemoved metric = prevTypeMap.get(serviceName);
                    DtoMetricRemoved match = currentTypeMap.get(serviceName);
                    if (match == null || (metric.getMonitored() && !match.getMonitored())) {
                        removes.add(metric);
                    }
                }
            }
        }

    }

    /**
     * Given a list of metrics, builds a map of Service Type Names to a map of of metrics.
     * This is a helper function used in determining how many metrics were deleted by
     * diffing the before and after state of a profile's metrics
     *
     * @param metrics the list of metrics to be put in a map of maps
     * @return the map of maps in a format consumable by processDeleteMetrics
     */
    protected Map<String,Map<String,DtoMetricRemoved>> buildServiceTypeNameMap(List<Metric> metrics) {
        Map<String,Map<String,DtoMetricRemoved>> result = new HashMap<>();
        for (Metric metric : metrics) {
            Map<String,DtoMetricRemoved> serviceTypeMap = result.get(metric.getServiceType());
            if (serviceTypeMap == null) {
                serviceTypeMap = new HashMap<>();
                result.put(metric.getServiceType(), serviceTypeMap);
            }
            DtoMetricRemoved removed = new DtoMetricRemoved(metric.getServiceName(), metric.getServiceType(), metric.isMonitored());
            serviceTypeMap.put(metric.getServiceName(), removed);
        }
        return result;
    }

    /**
     * Given a list of removed metrics in serviceType, serviceName pairs, builds a list of GroupServiceInfo objects
     * consumable by the CloudHub monitor engine. This returned list is queued up for deletion on a given agent.
     *
     * @param removedList list of metrics to be removed
     * @return a list of GroupServiceInfo providing metric name, service Type, and metric Type info for deletion
     */
    protected List<GroupServiceInfo> convertDtoToGroup(List<DtoMetricRemoved> removedList, ConfigurationProvider configurationProvider) {
        List<GroupServiceInfo> services = new ArrayList<>();
        for (DtoMetricRemoved dto : removedList) {
            GroupServiceInfo.MetricCategory category =
                    configurationProvider.isPrimaryMetric(dto.getServiceType())
                            ? GroupServiceInfo.MetricCategory.primary : GroupServiceInfo.MetricCategory.secondary;
            GroupServiceInfo info = new GroupServiceInfo(dto.getMetric(), dto.getServiceType(), category);
            services.add(info);
        }
        return services;
    }

    protected Metric findMetric(List<Metric> metrics, String name) {
        for (Metric metric : metrics) {
            if (metric.getServiceName().equals(name)) {
                return metric;
            }
        }
        return null;
    }


    /**
     * Deletes views as a result of checking or unchecking View checkboxes in UI.
     * In addition to service views, Host views can be deleted too
     * If the prefixChanged flag is present, all Service views should be deleted, but not hosts
     * When the prefix changes, the actual name of the hosts in GW representing the Services have changed,
     * requiring a reset of all ServicesViews
     *
     * @param virtualSystem
     * @param views
     * @param configuration
     * @param userName
     * @param prefixChanged
     * @return
     */
    protected int deleteResourceViewMetrics(VirtualSystem virtualSystem, List<String> views,
                                    ConnectionConfiguration configuration, String userName, boolean prefixChanged,
                                    String hostSpecialCase) {

        try {
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
            CloudHubProfile localCloudHubProfile = profileService.readCloudProfile(virtualSystem, configuration.getCommon().getAgentId());
            if (localCloudHubProfile == null) {
                return 0;
            }
            // Prep the views as lowercase host names
            int index = 0;
            List<String> finalViews = new ArrayList<>();
            List<String> hostsToDelete = new ArrayList<>();
            boolean isDiscoveryCapable = connector instanceof DiscoveryConnector;
            for (String view : views) {
                if (isDiscoveryCapable && hostSpecialCase != null && view.equalsIgnoreCase(hostSpecialCase)) {
                    List<String> hosts = ((DiscoveryConnector)connector).listHosts(configuration.getConnection());
                    for (String host : hosts) {
                        hostsToDelete.add(host);
                    }
                }
                views.set(index, (isDiscoveryCapable) ? view.toLowerCase() : view);
                index++;
            }
            if (hostsToDelete.size() > 0) {
                views.remove(hostSpecialCase.toLowerCase());
            }

            // add in the cluster prefixes
            if (configuration.getCommon().isPrefixServiceNames() || prefixChanged) {
                if (isDiscoveryCapable) {
                    List<String> clusters = ((DiscoveryConnector)connector).listClusters(configuration.getConnection());
                    for (String cluster : clusters) {
                        for (String view : views) {
                            // prefix the view name with cluster name
                            String prefixView = cluster + "-" + view;
                            finalViews.add(prefixView.toLowerCase());
                        }
                    }
                }
                // delete both prefixed and unprefixed to catch all
                if (prefixChanged) {
                    finalViews.addAll(views);
                }
            } else {
                finalViews.addAll(views);
            }

            if (hostsToDelete.size() > 0) {
                finalViews.addAll(hostsToDelete);
            }
            List<String> deleteServices = new ArrayList<>();
            List<String> groupViews;
            if (!isDiscoveryCapable) {
                groupViews = new ArrayList<>(finalViews);
                finalViews = new ArrayList<>();
            }
            else {
                groupViews = new ArrayList<>();
            }
            buildDeleteServicesList(deleteServices, localCloudHubProfile.getHypervisor().getMetrics(), finalViews);
            buildDeleteServicesList(deleteServices, localCloudHubProfile.getVm().getMetrics(), finalViews);

            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                agent.submitRequestToDeleteView(new MonitorChangeState(userName, finalViews, groupViews, deleteServices, finalViews));
            }
            return deleteServices.size();
        } catch (Exception e) {
            log.error("Failed to delete metrics view for " + views.toString(), e);
        }
        return -1;
    }

    public void setAdaptorViews(List<ConfigurationView> views, ConnectionConfiguration configuration) {
        for (ConfigurationView view : views) {
            if (view.getName().equals(DtoViewAdaptor.STORAGE_VIEW)) {
                configuration.getCommon().setStorageView(view.isEnabled());
            }
            else if (view.getName().equals(DtoViewAdaptor.NETWORK_VIEW)) {
                configuration.getCommon().setNetworkView(view.isEnabled());
            }
            else if (view.getName().equals(DtoViewAdaptor.RESOURCE_POOL_VIEW)) {
                configuration.getCommon().setResourcePoolView(view.isEnabled());
            }
            else if (view.getName().equals(DtoViewAdaptor.CUSTOM_VIEW)) {
                configuration.getCommon().setCustomView(view.isEnabled());
            }
        }
    }

    @RequestMapping(value = "/gwfunctions", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<String> retrieveGroundworkFunctions() {
        return synthetics.listGroundworkFunction();
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse evaluateExpression(@RequestBody DtoEvaluateContext dto) {
        SyntheticContext context = synthetics.createContext(dto.getInputs());
        Number number = synthetics.evaluate(context, dto.getExpression());
        String result = synthetics.format(number, dto.getFormat());
        return new DtoApiResultResponse().setResult(result);
    }

    protected DtoCount checkForUpdates(String path, VirtualSystem virtualSystem) {
        ConnectionConfiguration configuration = configurationService.readConfiguration(path);
        CloudHubProfile local = profileService.readCloudProfile(virtualSystem, configuration.getCommon().getAgentId());
        CloudHubProfile remote = profileService.readRemoteCloudProfile(virtualSystem, configuration.getGwos());
        if (remote == null) {
            remote = (CloudHubProfile)profileService.readProfileTemplate(virtualSystem);
        }
        int count = profileService.checkForNewMetrics(remote, local);
        return new DtoCount(count);
    }

    protected void updateProfile(String path, VirtualSystem virtualSystem) {
        ConnectionConfiguration configuration = configurationService.readConfiguration(path);
        CloudHubProfile local = profileService.readCloudProfile(virtualSystem, configuration.getCommon().getAgentId());
        CloudHubProfile remote = profileService.readRemoteCloudProfile(virtualSystem, configuration.getGwos());
        if (remote == null) {
            remote = (CloudHubProfile)profileService.readProfileTemplate(virtualSystem);
        }
        CloudHubProfile merged = profileService.mergeCloudProfiles(virtualSystem, remote, local);
        if (remote.getExcludes() != null) {
            merged.setExcludes(new Excludes());
            for (String exclude : remote.getExcludes().getExcludes()) {
                merged.getExcludes().addExclude(exclude);
            }
        }
        profileService.saveProfile(merged);
    }

    protected static final Map<String,String> DOCKER_DISPLAY_NAMES = new HashMap<String,String>() {{
        put(MetricType.hypervisor.name(), "Docker Engine");
        put(MetricType.vm.name(), "Container");
    }};

    protected static final Map<String,String> AMAZON_DISPLAY_NAMES = new HashMap<String,String>() {{
        put(MetricType.hypervisor.name(), "EC2 Compute");
        put("storage", "RDS/EBS Storage");
        put("custom", "CloudWatch Custom Metrics");
    }};

    protected Map<String,String> getMetricViewDisplayNames(CloudHubProfile profile) {
        if (profile.getProfileType().equals(ProfileType.docker)) {
            return DOCKER_DISPLAY_NAMES;
        }
        else if (profile.getProfileType().equals(ProfileType.amazon)) {
            return AMAZON_DISPLAY_NAMES;
        }
        return null;
    }

}
