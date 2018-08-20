package org.groundwork.cloudhub.connectors.netapp.client;

import netapp.manage.NaElement;
import netapp.manage.NaServer;
import org.groundwork.cloudhub.connectors.netapp.NetAppSystemInfo;
import org.groundwork.cloudhub.exceptions.ConnectorException;

public class SystemClient extends BaseNetAppClient {

    public SystemClient(NaServer server) {
        super(server);
    }

    public NetAppSystemInfo getSystemInfo() throws ConnectorException {
        try {
            NaElement api = new NaElement("system-get-version");
            NaElement root = server.invokeElem(api);
            NetAppSystemInfo info = new NetAppSystemInfo();
            info.setStatus(root.getAttr("status"));
            info.setBuildTimeStamp(root.getChildContent("build-timestamp"));
            info.setClustered(new Boolean(root.getChildContent("is-clustered")));
            info.setVersion(root.getChildContent("version"));
            NaElement versionTuple = root.getChildByName("version-tuple");
            if (versionTuple != null) {
                NaElement systemVersionTuple = versionTuple.getChildByName("system-version-tuple");
                if (systemVersionTuple != null) {
                    info.setGeneration(systemVersionTuple.getChildContent("generation"));
                    info.setMajorVersion(systemVersionTuple.getChildContent("major"));
                    info.setMinorVersion(systemVersionTuple.getChildContent("minor"));
                }
            }
            return info;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp System info: " + e.getMessage(), e);
        }
    }

}
