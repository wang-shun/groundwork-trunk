package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.DtoAmazonConfiguration;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoApiSaveResultResponse;
import org.groundwork.cloudhub.api.dto.DtoCount;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.connectors.amazon.AmazonConnector;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.cloudhub.profile.ProfileMetricGroup;
import org.groundwork.cloudhub.profile.UIMetric;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/amazon")
public class AmazonResource extends BaseCloudHubResource {

    private static Logger log = Logger.getLogger(AmazonResource.class);

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
    DtoAmazonConfiguration retrieveConnectionConfiguration(
            @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
            @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
            HttpServletRequest request, HttpSession session) {
        AmazonConfiguration configuration = (AmazonConfiguration)
                readConnectionConfiguration(VirtualSystem.AMAZON, filePath, fileName, request, session);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
        return new DtoAmazonConfiguration(configuration, isConnected);
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
    DtoApiSaveResultResponse saveConnectionConfiguration(@RequestBody DtoAmazonConfiguration dto, HttpServletRequest request) {
        ConnectionConfiguration configuration = convertToConfiguration(dto);
        if (dto.getState().getViewsRemoved() != null && dto.getState().getViewsRemoved().size() > 0) {
            submitDeleteViewsForAmazon(dto, configuration, request);
            dto.getState().setViewsRemoved(new LinkedList<String>());
        }
        return writeConnectionConfiguration(
                VirtualSystem.AMAZON,
                configuration,
                request,
                dto.getState());
    }

    protected int submitDeleteViewsForAmazon(DtoAmazonConfiguration dto, ConnectionConfiguration configuration, HttpServletRequest request) {
        List<String> views = new LinkedList();
        List<String> sourceTypes = new LinkedList<>();
        List<String> groupViews = new LinkedList();
        if (dto.getState().getViewsRemoved().contains(STORAGE_VIEW)) {
            views.add(AmazonConfigurationProvider.PREFIX_HOST_STORAGE);
            groupViews.add(AmazonConfigurationProvider.PREFIX_AMAZON_STORAGE);
            sourceTypes.add(SourceType.storage.name());
        }
        if (dto.getState().getViewsRemoved().contains(NETWORK_VIEW)) {
            views.add(AmazonConfigurationProvider.PREFIX_HOST_NETWORK);
            groupViews.add(AmazonConfigurationProvider.PREFIX_AMAZON_NETWORK);
            sourceTypes.add(SourceType.network.name());
        }
        if (dto.getState().getViewsRemoved().contains(CUSTOM_VIEW)) {
            sourceTypes.add(SourceType.custom.name());
        }
        if (views.size() > 0 || sourceTypes.size() > 0) {
            deleteViewMetrics(VirtualSystem.AMAZON, views, groupViews, sourceTypes, configuration, getCurrentUser(request), true);
        }
        return views.size() + sourceTypes.size();
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

        DtoProfileView profileView = readMetrics(filePath, fileName, profileId, VirtualSystem.AMAZON, false);
        AmazonConfiguration configuration = (AmazonConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        if (configuration != null) {
            if (configuration.getCommon().isNetworkView() == false) {
                profileView.getViews().remove(AmazonConfigurationProvider.NETWORK);
            }
            if (configuration.getCommon().isStorageView() == false) {
                profileView.getViews().remove(AmazonConfigurationProvider.STORAGE);
            }
            if (configuration.getCommon().isCustomView() == false) {
                profileView.getViews().remove(AmazonConfigurationProvider.CUSTOM);
            }
        }
        return profileView;
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
        return writeMetrics(VirtualSystem.AMAZON, profileView, request);
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
    DtoApiResultResponse testGwosConnection(@RequestBody DtoAmazonConfiguration dto) {
        return testGWOSConnection(convertToConfiguration(dto));
    }

    /**
     * Test Amazon Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/connector", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testAmazonConnection(@RequestBody DtoAmazonConfiguration dto) {
        return testConnectorConnection(convertToConfiguration(dto));
    }

    @RequestMapping(value = "/metricnames", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<String> retrieveMetricNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        AmazonConfiguration configuration = (AmazonConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        AmazonConnector connector = (AmazonConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.AMAZON);
        return connector.listMetricNames(serviceType, configuration);
    }


    @RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, Number> extractVariables(@RequestParam(value = "expression", required = true) String expression,
                                         @RequestParam(value = "inputType", required = true) String inputType,
                                         @RequestParam(value = "serviceType", required=true) String serviceType,
                                         @RequestParam(value = "profile", required = true) String profileId) {
        return extractExpressionVariables(synthetics, VirtualSystem.AMAZON, expression, inputType, serviceType, profileId);
    }


    /**
     * Conversion of DTO to Amazon Config
     *
     * @param dto
     * @return new Amazon config
     */
    public AmazonConfiguration convertToConfiguration(DtoAmazonConfiguration dto) {
        AmazonConfiguration configuration = new AmazonConfiguration();
        configuration.setGwos(dto.getGwos());
        configuration.setConnection(dto.getConnection());
        configuration.setCommon(dto.getCommon());
        setAdaptorViews(dto.getViews(), configuration);
        return configuration;
    }

    @RequestMapping(value = "/checkforupdates", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoCount checkForUpdates(@RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                            @RequestParam(value = "name", required = false, defaultValue = "") String fileName,
                                            @RequestParam(value = "profile", required = false, defaultValue = "") String profileId,
                                            HttpServletRequest request) {
        return checkForUpdates(filePath + "/" + fileName, VirtualSystem.AMAZON);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoProfileView update(@RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                          @RequestParam(value = "name", required = false, defaultValue = "") String fileName,
                          @RequestParam(value = "profile", required = false, defaultValue = "") String profileId) {

        updateProfile(filePath + "/" + fileName, VirtualSystem.AMAZON);
        DtoProfileView profileView = readMetrics(filePath, fileName, profileId, VirtualSystem.AMAZON, false);
        return profileView;
    }

    @RequestMapping(value = "/refreshcustommetrics", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoProfileView refreshCustomMetrics(@RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                          @RequestParam(value = "name", required = false, defaultValue = "") String fileName,
                          @RequestParam(value = "profile", required = false, defaultValue = "") String profileId) {

        ConnectionConfiguration configuration = configurationService.readConfiguration(filePath + "/" + fileName);
        DtoProfileView profileView = readMetrics(filePath, fileName, profileId, VirtualSystem.AMAZON, false);
        if (!configuration.getCommon().isCustomView()) {
            return profileView;
        }
        // custom metrics enabled...
        ProfileMetricGroup customGroup = profileView.getViews().get(MetricType.custom.name());
        if (customGroup == null) {
            customGroup = new ProfileMetricGroup(MetricType.custom.name(), MetricType.custom, "CloudWatch Custom Metrics");
            customGroup.setMetrics(new ArrayList<UIMetric>());
            profileView.getViews().put(MetricType.custom.name(), customGroup);
        }
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        connector.connect(configuration.getConnection());
        List<Metric> customMetrics = connector.retrieveCustomMetrics();
        List<UIMetric> uiCustomMetrics = mergeCustomMetrics(customGroup.getMetrics(), customMetrics);
        customGroup.setMetrics(uiCustomMetrics);
        return profileView;
    }

}
