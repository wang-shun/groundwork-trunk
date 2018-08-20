package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoApiSaveResultResponse;
import org.groundwork.cloudhub.api.dto.DtoCount;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.groundwork.cloudhub.api.dto.DtoVmwareConfiguration;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareConfigurationProvider;
import org.groundwork.cloudhub.connectors.vmware2.VmWareConnector2;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.profile.ProfileMetricGroup;
import org.groundwork.cloudhub.profile.UIMetric;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vmware")
public class VmWareResource extends BaseCloudHubResource {

    private static Logger log = Logger.getLogger(VmWareResource.class);

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
    DtoVmwareConfiguration retrieveConnectionConfiguration(
            @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
            @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
            HttpServletRequest request, HttpSession session) {
        VmwareConfiguration configuration = (VmwareConfiguration)
                readConnectionConfiguration(VirtualSystem.VMWARE, filePath, fileName, request, session);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
        return new DtoVmwareConfiguration(configuration, isConnected);
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
    DtoApiSaveResultResponse saveConnectionConfiguration(@RequestBody DtoVmwareConfiguration dto, HttpServletRequest request) {
        ConnectionConfiguration configuration = convertToConfiguration(dto);
        if (dto.getState().getViewsRemoved() != null && dto.getState().getViewsRemoved().size() > 0) {
            submitDeleteViewsForVmWare(dto, configuration, request);
            dto.getState().setViewsRemoved(new LinkedList<String>());
        }
        return writeConnectionConfiguration(
                VirtualSystem.VMWARE,
                configuration,
                request,
                dto.getState());
    }

    protected int submitDeleteViewsForVmWare(DtoVmwareConfiguration dto, ConnectionConfiguration configuration, HttpServletRequest request) {
        List<String> views = new LinkedList();
        List<String> sourceTypes = new LinkedList<>();
        List<String> groupViews = new LinkedList();
        if (dto.getState().getViewsRemoved().contains(STORAGE_VIEW)) {
            views.add(ConnectorConstants.PREFIX_VM_STORAGE);
            groupViews.add(ConnectorConstants.PREFIX_STORAGE);
            sourceTypes.add(SourceType.storage.name());
        }
        if (dto.getState().getViewsRemoved().contains(NETWORK_VIEW)) {
            views.add(ConnectorConstants.PREFIX_VM_NETWORK);
            groupViews.add(ConnectorConstants.PREFIX_NETWORK);
            sourceTypes.add(SourceType.network.name());
        }
        if (views.size() > 0) {
            deleteViewMetrics(VirtualSystem.VMWARE, views, groupViews, sourceTypes, configuration, getCurrentUser(request));
        }
        return views.size();
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
        DtoProfileView profileView = readMetrics(filePath, fileName, profileId, VirtualSystem.VMWARE, false);
        VmwareConfiguration configuration = (VmwareConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        if (configuration != null) {
            if (configuration.getCommon().isNetworkView() == false) {
                profileView.getViews().remove(VMwareConfigurationProvider.NETWORK);
            }
            if (configuration.getCommon().isStorageView() == false) {
                profileView.getViews().remove(VMwareConfigurationProvider.STORAGE);
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
        ProfileMetricGroup group = profileView.getViews().get(VMwareConfigurationProvider.STORAGE);
        if (group != null) {
            for (UIMetric metric : group.getMetrics()) {
                metric.setSourceType(metric.getServiceType());
            }
        }
        group = profileView.getViews().get(VMwareConfigurationProvider.NETWORK);
        if (group != null) {
            for (UIMetric metric : group.getMetrics()) {
                metric.setSourceType(metric.getServiceType());
            }
        }
        return writeMetrics(VirtualSystem.VMWARE, profileView, request);
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
    DtoApiResultResponse testGwosConnection(@RequestBody DtoVmwareConfiguration dto) {
        return testGWOSConnection(convertToConfiguration(dto));
    }

    /**
     * Test VMWare Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/connector", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testVmwareConnection(@RequestBody DtoVmwareConfiguration dto) {
        return testConnectorConnection(convertToConfiguration(dto));
    }

    @RequestMapping(value = "/metricnames", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<String> retrieveMetricNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        VmwareConfiguration configuration = (VmwareConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        VmWareConnector2 connector = (VmWareConnector2) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.VMWARE);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Vmware connector for /metricnames");
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
        return extractExpressionVariables(synthetics, VirtualSystem.VMWARE, expression, inputType, serviceType, profileId);
    }

    /**
     * Conversion of DTO to VmWare Config
     *
     * @param dto
     * @return new Vmware config
     */
    public VmwareConfiguration convertToConfiguration(DtoVmwareConfiguration dto) {
        VmwareConfiguration configuration = new VmwareConfiguration();
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
                             @RequestParam(value = "profile", required = false, defaultValue = "") String profileId) {
        return checkForUpdates(filePath + "/" + fileName, VirtualSystem.VMWARE);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    DtoProfileView update(@RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                             @RequestParam(value = "name", required = false, defaultValue = "") String fileName,
                             @RequestParam(value = "profile", required = false, defaultValue = "") String profileId) {

        updateProfile(filePath + "/" + fileName, VirtualSystem.VMWARE);
        DtoProfileView profileView = readMetrics(filePath, fileName, profileId, VirtualSystem.VMWARE, false);
        return profileView;
    }

}
