package org.groundwork.cloudhub.api;

import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.api.dto.DtoApiResultResponse;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 * TODO: This controller does not work in our current version of Spring. Would need to upgrade Spring to get GenericEntities to work
 */
@Controller
@RequestMapping("/api/generic")
public class GenericEntityResource extends BaseCloudHubResource {


    @RequestMapping(value = "/metricsNotUsed", method = RequestMethod.GET, produces="application/json")
    public Response retrieveMetrics(@RequestParam(value="path", required=false, defaultValue="/usr/local/groundwork/config/cloudhub/") String filePath,
                                                            @RequestParam("name") String fileName,
                                                            @RequestParam("profile") String agent)
    {
        String configPath = filePath + fileName;
        try {
            ConnectionConfiguration configuration = configurationService.readConfiguration(configPath);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
            boolean isConnected = connector.getConnectionState().equals(ConnectionState.CONNECTED);
            CloudHubProfile localCloudHubProfile = profileService.readCloudProfile(VirtualSystem.CLOUDERA, agent);
            DtoProfileView profile = new DtoProfileView(localCloudHubProfile, configuration.getCommon(), isConnected);
            GenericEntity entity = new GenericEntity<DtoProfileView>(profile){};
            return Response.ok(entity).build();
        }
        catch (Exception e) {
            log.error("Failed to read profile", e);
            DtoApiResultResponse error = new DtoApiResultResponse(e.getLocalizedMessage());
            GenericEntity entity = new GenericEntity<DtoApiResultResponse>(error){};
            return Response.ok(entity).build();

        }
    }

}
