package org.groundwork.cloudhub.connectors.opendaylight.client;

import org.groundwork.cloudhub.connectors.base.BaseConnectorClient;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;

public abstract class BaseOpenDaylightClient extends BaseConnectorClient {

    protected OpenDaylightConnection connection = null;
    public final static String REST_END_POINT = "/controller/nb/v2";
    public final static String REST_STATISTICS = "/statistics";
    public final static String REST_TOPOLOGY = "/topology";
    public final static String REST_FLOW_API = "/flow";
    public final static String REST_PORT_API = "/port";

    public BaseOpenDaylightClient(OpenDaylightConnection connection) {
        this.connection = connection;
    }

    protected String makeFlowConnection() {
        return makeStatisticsConnection(REST_FLOW_API);
    }

    protected String makePortConnection() {
        return makeStatisticsConnection(REST_PORT_API);
    }

    protected String makeStatisticsConnection(String api) {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(PATH_SEPARATOR);
        containerApi.append(connection.getContainer());
        containerApi.append(api);
        return makeConnectionString(connection.getServer(), REST_STATISTICS, containerApi.toString(), connection.isSslEnabled());
    }

    protected String makeTopologyConnection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(PATH_SEPARATOR);
        containerApi.append(connection.getContainer());
        return makeConnectionString(connection.getServer(), REST_TOPOLOGY, containerApi.toString(), connection.isSslEnabled());
    }

    /**
     * Join a base URL with a relative path to create a full URL
     *
     * @param hostName the hostName such as localhost or localhost:8080
     * @param subPath the api group such as /statistics or /topology
     * @param api the final api such as /flow or /port
     * @return the correctly joined path respecting path separators
     */
    protected String makeConnectionString(String hostName, String subPath, String api, boolean isSecure) {
        StringBuilder result = new StringBuilder();
        if (hostName == null) hostName = "";
        if (subPath == null) subPath = "";
        if (api == null) api = "";
        String scheme = (isSecure) ? "https://" : "http://";
        result.append(scheme);
        result.append(hostName);
        result.append(concatenatePaths(result.toString(), REST_END_POINT));
        result.append(concatenatePaths(result.toString(), subPath));
        result.append(concatenatePaths(result.toString(), api));
        return result.toString();
    }

}
