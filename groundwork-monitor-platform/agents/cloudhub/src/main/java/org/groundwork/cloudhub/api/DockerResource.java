package org.groundwork.cloudhub.api;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoApiSaveResultResponse;
import org.groundwork.cloudhub.api.dto.DtoDockerConfiguration;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.docker.DockerConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/docker")
public class DockerResource extends BaseCloudHubResource {

    private static Logger log = Logger.getLogger(DockerResource.class);

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
    DtoDockerConfiguration retrieveConnectionConfiguration(
                                                @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                                @RequestParam(value = "name", required = false, defaultValue = "undefined") String fileName,
                                                HttpServletRequest request, HttpSession session) {
        DockerConfiguration configuration = (DockerConfiguration)
                readConnectionConfiguration(VirtualSystem.DOCKER, filePath, fileName, request, session);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
        boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
        return new DtoDockerConfiguration(configuration, isConnected);
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
    DtoApiSaveResultResponse saveConnectionConfiguration(@RequestBody DtoDockerConfiguration dto, HttpServletRequest request) {
        ConnectionConfiguration configuration = convertToConfiguration(dto);
        return writeConnectionConfiguration(
                VirtualSystem.DOCKER,
                configuration,
                request,
                dto.getState());
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
        return readMetrics(filePath, fileName, profileId, VirtualSystem.DOCKER, false);
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
        return writeMetrics(VirtualSystem.DOCKER, profileView, request);
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
    DtoApiResultResponse testGwosConnection(@RequestBody DtoDockerConfiguration dto) {
        return testGWOSConnection(convertToConfiguration(dto));
    }

    /**
     * Test Docker Connection
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/test/connector", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public
    @ResponseBody
    DtoApiResultResponse testDockerConnection(@RequestBody DtoDockerConfiguration dto) {
        return testConnectorConnection(convertToConfiguration(dto));
    }

    @RequestMapping(value = "/metricnames", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<String> retrieveMetricNames(@RequestParam(value = "serviceType", required = true) String serviceType,
                                     @RequestParam(value = "path", required = false, defaultValue = DEFAULT_CONFIG_PATH) String filePath,
                                     @RequestParam(value = "name", required = true) String fileName) {

        DockerConfiguration configuration = (DockerConfiguration) configurationService.readConfiguration(filePath + "/" + fileName);
        DockerConnector connector = (DockerConnector) connectorFactory.getMonitoringConnector(configuration.getCommon().getAgentId(), VirtualSystem.DOCKER);
        if (connector == null) {
            throw new CloudHubException("Could not retrieve Docker connector for /metricnames");
        }
        return connector.listMetricNames(serviceType, configuration);
    }

    @RequestMapping(value = "/gwfunctions", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<String> retrieveGroundworkFunctions() {
        return synthetics.listGroundworkFunction();
    }

    @RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, Number> extractVariables(@RequestParam(value = "expression", required = true) String expression,
                                         @RequestParam(value = "inputType", required = true) String inputType,
                                         @RequestParam(value = "serviceType", required=true) String serviceType,
                                         @RequestParam(value = "profile", required = true) String profileId) {
        return extractExpressionVariables(synthetics, VirtualSystem.DOCKER, expression, inputType, serviceType, profileId);
    }

    /**
     * Conversion of DTO to Docker Config
     *
     * @param dto
     * @return new docker config
     */
    public DockerConfiguration convertToConfiguration(DtoDockerConfiguration dto) {
        DockerConfiguration configuration = new DockerConfiguration();
        configuration.setGwos(dto.getGwos());
        configuration.setConnection(dto.getConnection());
        configuration.setCommon(dto.getCommon());
        return configuration;
    }



}