package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoApiSaveResultResponse;
import org.groundwork.cloudhub.api.dto.DtoAzureConfiguration;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.groundwork.cloudhub.configuration.AzureConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.groundwork.cloudhub.connectors.azure.AzureConnector;
import org.groundwork.cloudhub.connectors.base.DiscoveryConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/azure")
public class AzureResource extends BaseCloudHubResource {

    private static Logger log = Logger.getLogger(AzureResource.class);

    private static String UPLOADED_FOLDER = "/usr/local/groundwork/config/cloudhub/azure/";

    /**
     * Retrieve Connection
     *
     * @param filePath
     * @param fileName
     * @param request
     * @param session
     * @return
     */
    @RequestMapping(value = "/config", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoAzureConfiguration retrieveConnectionConfiguration(
                                                @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                                @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
                                                HttpServletRequest request, HttpSession session) {
        AzureConfiguration configuration = (AzureConfiguration)
                readConnectionConfiguration(VirtualSystem.AZURE, filePath, fileName, request, session);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
        return new DtoAzureConfiguration(configuration, isConnected);
    }

    /**
     * Save Connection
     *
     * @param dto
     * @param request
     * @return
     */
    @RequestMapping(value = "/config", consumes = "application/json", produces = "application/json", method = RequestMethod.POST)
    public
    @ResponseBody
    DtoApiSaveResultResponse saveConnectionConfiguration(@RequestBody DtoAzureConfiguration dto, HttpServletRequest request) {
        ConnectionConfiguration configuration = convertToConfiguration(dto);
        DtoApiSaveResultResponse response = writeConnectionConfiguration(
                VirtualSystem.AZURE,
                configuration,
                request,
                dto.getState(),
                AzureConfigurationProvider.AZURE_HOST);
        if (dto.getState().getViewsAdded().size() > 0 || dto.getState().getViewsRemoved().size() > 0) {
            // add metrics for any views added
            AzureConnector connector = (AzureConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.AZURE);
            if (connector == null) {
                throw new CloudHubException("Could not retrieve Azure connector for /metricnames");
            }
            CloudHubProfile profile = profileService.readCloudProfile(configuration.getCommon().getVirtualSystem(), configuration.getCommon().getAgentId());
            if (profile == null) {
                throw new CloudHubException("profile could not be read");
            }
            for (String view : dto.getState().getViewsAdded()) {
                List<String> metricNames = connector.listMetricNames(view, (AzureConfiguration)configuration);
                if (metricNames.size() == 0) {
                    log.debug("no metrics to add for view: " + view);
                    continue; // nothing to add
                }
                for (String metricName : metricNames) {
                    Metric metric = new Metric();
                    metric.setName(metricName);
                    metric.setMonitored(false);
                    metric.setGraphed(false);
                    metric.setServiceType(view);
                    profile.getVm().addMetric(metric);
                }
            }
            List<String> removed = dto.getState().getViewsRemoved();
            if (removed != null && removed.size()  > 0) {
                Set<String> removals = new HashSet<>();
                for (String remove : removed) {
                    removals.add(remove);
                }
                List<Metric> newList = new ArrayList<>();
                for (Metric metric : profile.getVm().getMetrics()) {
                    if (!removals.contains(metric.getServiceType().toLowerCase())) {
                        newList.add(metric);
                    }
                }
                profile.getVm().setMetrics(newList);
            }
            profileService.saveProfile(profile);
        }
        // if we disabled Resource Groups, they need to be deleted
        if (dto.getState().getResourceGroupsChanged() && dto.getConnection().getEnableResourceGroups() == false) {
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                List<String> groups = new ArrayList<>();
                groups.add(AzureConfigurationProvider.PREFIX_RESOURCE_GROUP);
                agent.submitRequestToDeleteView(new MonitorChangeState(getCurrentUser(request), new ArrayList<String>(), groups, new ArrayList<String>()));
            }
        }
        return response;
    }



    /**
     * Retrieve Metrics
     *
     * @param filePath
     * @param fileName
     * @param profileId
     * @return
     */
    @RequestMapping(value = "/metrics", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoProfileView retrieveMetrics(@RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                   @RequestParam(value = "name", required = false, defaultValue = "") String fileName,
                                   @RequestParam(value = "profile", required = false, defaultValue = "") String profileId) {
        return readMetrics(filePath, fileName, profileId, VirtualSystem.AZURE, true);
    }

    /**
     * Save Metrics
     *
     * @param profileView
     * @return
     */
    @RequestMapping(value = "/metrics", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse saveMetrics(@RequestBody DtoProfileView profileView, HttpServletRequest request) {
        return writeMetrics(VirtualSystem.AZURE, profileView, request);
    }

    /**
     * Test GWOS Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/groundwork", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testGwosConnection(@RequestBody DtoAzureConfiguration dto) {
        return testGWOSConnection(convertToConfiguration(dto));
    }

    /**
     * Test Azure Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/connector", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testAzureConnection(@RequestBody DtoAzureConfiguration dto) {
        return testConnectorConnection(convertToConfiguration(dto));
    }

    @RequestMapping(value = "/metricnames", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<String> retrieveMetricNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        AzureConfiguration configuration = (AzureConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        AzureConnector connector = (AzureConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.AZURE);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Azure connector for /metricnames");
        }
        return connector.listMetricNames(serviceType, configuration);
    }

    @RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, Number> extractVariables(@RequestParam(value = "expression", required = true) String expression,
                                         @RequestParam(value = "inputType", required = true) String inputType,
                                         @RequestParam(value = "serviceType", required=true) String serviceType,
                                         @RequestParam(value = "profile", required = true) String profileId) {
        return extractExpressionVariables(synthetics, VirtualSystem.AZURE, expression, inputType, serviceType, profileId);
    }

    /**
     * Discovery of Azure Services dialog
     *
     * @param filePath
     * @param fileName
     * @param request
     * @param session
     * @return
     */
    @RequestMapping(value = "/discovery", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody List<ConfigurationView> retrieveDiscoveryViews(
            @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
            @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
            HttpServletRequest request, HttpSession session) {
        if (fileName.isEmpty() || fileName.equals("undefined")) {
            throw new CloudHubException("Please create and test a valid connection prior to discovery");
        }
        AzureConfiguration configuration = (AzureConfiguration)
                readConnectionConfiguration(VirtualSystem.AZURE, filePath, fileName, request, session);
        DiscoveryConnector connector = (DiscoveryConnector)connectorFactory.getMonitoringConnector(configuration);
        Set<String> services = connector.listServices(configuration.getConnection());
        List<ConfigurationView> views = new ArrayList(configuration.getViews());
        Map<String, ConfigurationView> currentViewsMap = new HashMap<>();
        for (ConfigurationView view : views) {
            currentViewsMap.put(view.getName(), view);
        }
        for (String service : services) {
            ConfigurationView view = currentViewsMap.get(service);
            if (view == null) {
                views.add(new ConfigurationView(service, false, true));
            }
        }
        return views;
    }

    /**
     * Save checked discovered services
     *
     * @param dto
     * @param request
     * @return
     */
//    @RequestMapping(value = "/discovery", consumes = "application/json", produces = "application/json", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    DtoApiSaveResultResponse saveViews(@RequestBody List<ConfigurationView> dto, HttpServletRequest request, HttpSession session,
//                                       @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
//                                       @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName
//                                       ) {
//        AzureConfiguration configuration = (AzureConfiguration)
//                readConnectionConfiguration(VirtualSystem.AZURE, filePath, fileName, request, session);
//        configuration.setAdaptorViews(dto);
//        DtoApiSaveResultResponse response = writeConnectionConfiguration(VirtualSystem.AZURE, configuration,
//                request, true, dto.getState().getViewsRemoved(),
//                AzureConfigurationProvider.AZURE_HOST);
//        return response;
//    }


    /**
     * Conversion of DTO to Azure Config
     *
     * @param dto
     * @return new azure config
     */
    public AzureConfiguration convertToConfiguration(DtoAzureConfiguration dto) {
        AzureConfiguration configuration = new AzureConfiguration();
        configuration.setGwos(dto.getGwos());
        configuration.setConnection(dto.getConnection());
        configuration.setCommon(dto.getCommon());
        configuration.setViews(dto.getViews());
        return configuration;
    }


    @RequestMapping(value="/upload", method= RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {


        if (uploadfile.isEmpty()) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        ensureAuthFileDirectoryExists();

        try {

            saveUploadedFiles(Arrays.asList(uploadfile));

        } catch (IOException e) {
            throw new CloudHubException(e.getMessage(), e);
        }

        String result = UPLOADED_FOLDER + uploadfile.getOriginalFilename();
        return new ResponseEntity(result, new HttpHeaders(), HttpStatus.OK);

    }

    //save file
    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
        }

    }

    private void ensureAuthFileDirectoryExists() throws CloudHubException {
        try {
            File dir = new File(UPLOADED_FOLDER);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create Azure credentials store: " + UPLOADED_FOLDER);
                }
            }
        } catch (SecurityException e) {
            throw new CloudHubException("Failed to access Azure credentials store: " + UPLOADED_FOLDER, e);
        }

    }
}