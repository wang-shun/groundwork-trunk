package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.*;
import org.groundwork.cloudhub.configuration.ClouderaConfiguration;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConfigurationProvider;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.synthetics.SyntheticContext;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cloudera")
public class ClouderaResource extends BaseCloudHubResource {

    private static Logger log = Logger.getLogger(ClouderaResource.class);

    @Autowired
    private Synthetics synthetics;

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
    DtoClouderaConfiguration retrieveConnectionConfiguration(
                                                @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                                @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
                                                HttpServletRequest request, HttpSession session) {
        ClouderaConfiguration configuration = (ClouderaConfiguration)
                readConnectionConfiguration(VirtualSystem.CLOUDERA, filePath, fileName, request, session);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
        return new DtoClouderaConfiguration(configuration, isConnected);
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
    DtoApiSaveResultResponse saveConnectionConfiguration(@RequestBody DtoClouderaConfiguration dto, HttpServletRequest request) {
        ConnectionConfiguration configuration = convertToConfiguration(dto);
        return writeConnectionConfiguration(
                VirtualSystem.CLOUDERA,
                configuration,
                request,
                dto.getState(),
                ClouderaConfigurationProvider.CLOUDERA_HOST);
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
        return readMetrics(filePath, fileName, profileId, VirtualSystem.CLOUDERA, true);
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
        return writeMetrics(VirtualSystem.CLOUDERA, profileView, request);
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
    DtoApiResultResponse testGwosConnection(@RequestBody DtoClouderaConfiguration dto) {
        return testGWOSConnection(convertToConfiguration(dto));
    }

    /**
     * Test Cloudera Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/connector", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testClouderaConnection(@RequestBody DtoClouderaConfiguration dto) {
        return testConnectorConnection(convertToConfiguration(dto));
    }

    @RequestMapping(value = "/metricnames", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<String> retrieveMetricNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        ClouderaConfiguration configuration = (ClouderaConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        ClouderaConnector connector = (ClouderaConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.CLOUDERA);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Cloudera connector for /metricnames");
        }
        return connector.listMetricNames(serviceType, configuration);
    }

    @RequestMapping(value = "/healthchecknames", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<String> retrieveHeathCheckNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        ClouderaConfiguration configuration = (ClouderaConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        ClouderaConnector connector = (ClouderaConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.CLOUDERA);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Cloudera connector for /metricnames");
        }
        return connector.listHealthCheckNames(serviceType);
    }

    @RequestMapping(value = "/gwfunctions", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<String> retrieveGroundworkFunctions() {
        return synthetics.listGroundworkFunction();
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse evaluateExpression(@RequestBody DtoEvaluateContext dto) {
        ClouderaConfiguration configuration = (ClouderaConfiguration) configurationService.readConfiguration(DEFAULT_CONFIG_PATH + "/" + dto.getConfigName());
        ClouderaConnector connector = (ClouderaConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.CLOUDERA);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Cloudera connector for /evaluate");
        }
        SyntheticContext context = synthetics.createContext(dto.getInputs());
        Number number = synthetics.evaluate(context, dto.getExpression());
        String result = synthetics.format(number, dto.getFormat());
        return new DtoApiResultResponse().setResult(result);
    }

    @RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, Number> extractVariables(@RequestParam(value = "expression", required = true) String expression,
                                         @RequestParam(value = "inputType", required = true) String inputType,
                                         @RequestParam(value = "serviceType", required=true) String serviceType,
                                         @RequestParam(value = "profile", required = true) String profileId) {
        return extractExpressionVariables(synthetics, VirtualSystem.CLOUDERA, expression, inputType, serviceType, profileId);
    }

    /**
     * Conversion of DTO to Cloudera Config
     *
     * @param dto
     * @return new cloudera config
     */
    public ClouderaConfiguration convertToConfiguration(DtoClouderaConfiguration dto) {
        ClouderaConfiguration configuration = new ClouderaConfiguration();
        configuration.setGwos(dto.getGwos());
        configuration.setConnection(dto.getConnection());
        configuration.setCommon(dto.getCommon());
        configuration.setViews(dto.getViews());
        return configuration;
    }


}