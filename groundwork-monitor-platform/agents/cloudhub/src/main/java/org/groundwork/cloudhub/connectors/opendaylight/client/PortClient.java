package org.groundwork.cloudhub.connectors.opendaylight.client;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.opendaylight.controller.sal.reader.NodeConnectorStatistics;
import org.opendaylight.controller.statistics.northbound.AllPortStatistics;
import org.opendaylight.controller.statistics.northbound.PortStatistics;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PortClient extends BaseOpenDaylightClient  {

    private static Logger log = Logger.getLogger(PortClient.class);

    public PortClient(OpenDaylightConnection connection) {
        super(connection);
    }

    public List<MetricInfo> getHyperVisorStatistics(String hypervisor) {
        return new ArrayList<MetricInfo>();
    }

    public List<MetricInfo> retrieveMetrics(Set<String> queries) throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        List<MetricInfo> metrics = new ArrayList<MetricInfo>();
        try {
            String authString = connection.getUsername() + ":" + connection.getPassword();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            java.net.URL url = new java.net.URL(makePortConnection());
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            urlConnection.setRequestProperty("Accept", "application/xml");
            urlConnection.connect();
            JAXBContext context = JAXBContext.newInstance(AllPortStatistics.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            stream = urlConnection.getInputStream();
            AllPortStatistics ports = (AllPortStatistics) unmarshaller.unmarshal(stream);
            String now = now();
            for (PortStatistics port : ports.getPortStatistics()) {
                String nodeID = port.getNode().getNodeIDString();
                List<NodeConnectorStatistics> statistics = port.getPortStatistic();
                for (NodeConnectorStatistics statistic : statistics) {
                    for (String query : queries) {
                        if (!query.startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                            long metricValue = getMetricValue(statistic, query);
                            if (metricValue > -1) {
                                String fullName = nodeID + "-" + statistic.getNodeConnector().getNodeConnectorIDString() + "-" + query;
                                metrics.add(new MetricInfo(fullName, nodeID, now, metricValue, determineMetricType(query), query,
                                        statistic.getNodeConnector().getNodeConnectorIDString()));
                            }
                        }
                    }
                }
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
        return metrics;
    }

    protected long getMetricValue(NodeConnectorStatistics statistics, String query) {
        try {
            String value = BeanUtils.getProperty(statistics, query);
            return Long.parseLong(value);
        }
        catch (NumberFormatException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to load method for query [" + query + "]: " + e.getMessage(), e);
        }
        return -1; // -1 indicates failed to retrieve method
    }

    protected String determineMetricType(String query) {
        if (query.toLowerCase().contains("byte")) {
            return "bytes";
        }
        return "count";
    }

}
