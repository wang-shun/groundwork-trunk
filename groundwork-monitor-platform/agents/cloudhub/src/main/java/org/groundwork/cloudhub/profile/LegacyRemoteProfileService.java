package org.groundwork.cloudhub.profile;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.gwos.BaseGwosService;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.NetHubProfile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Handles 7.0.x Remote Profile Retrieval
 */
public class LegacyRemoteProfileService {

    private static Logger log = Logger.getLogger(LegacyRemoteProfileService.class);


    public static CloudHubProfile readRemoteProfile(VirtualSystem virtualSystem, String userName, String password, String gwServerName, boolean isGWServerSecured) {
        String vmType = (virtualSystem == VirtualSystem.VMWARE) ? ConnectorConstants.LEGACY_CONNECTOR_VMWARE : ConnectorConstants.LEGACY_CONNECTOR_RHEV;
        String deltaMonitoringStub = null;
        CloudHubProfile profile = null;
        String response = null;
        DataOutputStream out = null;
        BufferedReader inStream = null;
        HttpURLConnection connection = null;
        try {   // connect
            URI uri = new URI(
                    isGWServerSecured ? "https" : "http",
                    null,
                    "//"
                            + gwServerName
                            + BaseGwosService.RS_LEGACY_ENDPOINT_BASE_DEFAULT
                            + "/vemaProfile/checkUpdates",
                    null,
                    null);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Connection", "Keep-Alive");
            out = new DataOutputStream(connection.getOutputStream());
            String message =
                    "username=" + userName
                            + "&password=" + password
                            + "&vmtype=" + vmType
                            + "&client-monitoring-profile="
                            + (deltaMonitoringStub != null
                            ? URLEncoder.encode(deltaMonitoringStub, "UTF-8")
                            : "");
            out.writeBytes(message);
            out.flush();
            out.close();
            out = null;
            inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer remoteProfileXML = new StringBuffer();
            String line = null;
            while ((line = inStream.readLine()) != null) {
                remoteProfileXML.append(line);
            }
            response = remoteProfileXML.toString();
            JAXBContext jaxbContext = JAXBContext.newInstance(CloudHubProfile.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            profile = (CloudHubProfile) unmarshaller.unmarshal(new StringReader(response));
            profile.setProfileType(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem));
        } catch (Exception e) {
            log.error("Failed to read remote profile", e);
            response = "<code>6</code>"
                    + "<message>"
                    + e.getMessage() + " (or Invalid GroundWork Server Name)"
                    + "</message>";
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
                if (out != null)
                    out.close();
                if (connection != null)
                    connection.disconnect();
            }
            catch (Exception e) {
            }
        }
        return profile;
    }

    public static ContainerProfile convertToContainerHub(CloudHubProfile cloudHubProfile) {
        ContainerProfile profile = new ContainerProfile(cloudHubProfile.getProfileType(), "");
        for (Metric metric : cloudHubProfile.getHypervisor().getMetrics()) {
            profile.getEngine().addMetric(metric);
        }
        for (Metric metric : cloudHubProfile.getVm().getMetrics()) {
            profile.getEngine().addMetric(metric);
        }
        return profile;
    }

    public static NetHubProfile convertToNetHub(CloudHubProfile cloudHubProfile) {
        NetHubProfile profile = new NetHubProfile(cloudHubProfile.getProfileType(), "");
        for (Metric metric : cloudHubProfile.getHypervisor().getMetrics()) {
            profile.getController().addMetric(metric);
        }
        for (Metric metric : cloudHubProfile.getVm().getMetrics()) {
            profile.getSwitch().addMetric(metric);
        }
        return profile;
    }

}
