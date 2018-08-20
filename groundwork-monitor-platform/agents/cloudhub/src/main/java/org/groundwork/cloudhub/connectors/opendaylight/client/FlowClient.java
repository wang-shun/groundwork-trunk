package org.groundwork.cloudhub.connectors.opendaylight.client;

import org.apache.commons.codec.binary.Base64;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.opendaylight.controller.statistics.northbound.AllFlowStatistics;
import org.opendaylight.controller.statistics.northbound.FlowStatistics;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlowClient extends BaseOpenDaylightClient {


    public FlowClient(OpenDaylightConnection connection) {
        super(connection);
    }

    public List<ServerInfo> listHypervisors() throws ConnectorException {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        String name = "unknown-host";
        try {
            URL url = new URL(makeFlowConnection());
            name = url.getHost();
        }
        catch (Exception e) {
            throw new ConnectorException(e);

        }
        servers.add(new ServerInfo(name));
        return servers;
    }

    public List<VmInfo> listVirtualMachines(String hypervisor) throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        List<VmInfo> vms = new ArrayList<VmInfo>();
        try {
            String authString = connection.getUsername() + ":" + connection.getPassword();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            java.net.URL url = new java.net.URL(makeFlowConnection());
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            urlConnection.setRequestProperty("Accept", "application/xml");
            urlConnection.connect();
            JAXBContext context = JAXBContext.newInstance(AllFlowStatistics.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            stream = urlConnection.getInputStream();
            AllFlowStatistics flows = (AllFlowStatistics) unmarshaller.unmarshal(stream);
            for (FlowStatistics flow : flows.getFlowStatistics()) {
                VmInfo vm = new VmInfo(flow.getNode().getNodeIDString(), hypervisor);
                vms.add(vm);
            }
        }
        catch (Exception e) {
            throw new ConnectorException(e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                ((HttpURLConnection)urlConnection).disconnect();
            }
        }
        return vms;
    }

}
