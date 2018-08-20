package org.groundwork.cloudhub.connectors.netapp.client;

import netapp.manage.NaElement;
import netapp.manage.NaServer;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.groundwork.cloudhub.connectors.netapp.NetAppHost;
import org.groundwork.cloudhub.connectors.netapp.NetAppNode;
import org.groundwork.cloudhub.connectors.netapp.NetAppVM;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dtaylor on 7/1/15.
 */
public class MetricsClient extends BaseNetAppClient  {

    private static Logger log = Logger.getLogger(org.groundwork.cloudhub.connectors.netapp.client.MetricsClient.class);
    private List<MetricFaultInfo> metricFaults = new LinkedList<>();
    private DocumentBuilderFactory factory = null;
    private XPathFactory xpathFactory = null;
    private Map<String,XPathExpression> expressions = new ConcurrentHashMap<>();
    public static final String FAILED_DISK_METRIC = "computed-failed-disks";

    public MetricsClient(NaServer server) {
        super(server);
    }

    public Map<String, NetAppVM> gatherVolumeMetrics(Map<String, BaseQuery> queries) throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("volume-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            Map<String, NetAppVM> volumes = new HashMap<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return volumes;
            }
            DocumentBuilderFactory factory = getDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            for (NaElement node : nodes) {
                String xml = node.toString();
                InputSource is = new InputSource(new StringReader(xml));
                Document doc = builder.parse(is);
                NaElement idNode = node.getChildByName("volume-id-attributes");
                NaElement stateNode = node.getChildByName("volume-state-attributes");
                String name = idNode.getChildContent("name");
                NetAppVM volume = new NetAppVM(name, NetAppNode.NetAppNodeType.Volume);
                volume.setRunState(determineVolumeStatus(stateNode));
                volume.setController(idNode.getChildContent("owning-vserver-name"));
                volume.setAggregate(idNode.getChildContent("containing-aggregate-name"));
                for (BaseQuery query : queries.values()) {
                    if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX) &&
                            query.getQuery().startsWith("volume-")) {
                        BaseMetric metric = new BaseMetric(query, query.getQuery());
                        metric.setCustomName(query.getCustomName());
                        metric.setValue(lookupMetric(doc, query.getQuery(), name, "/volume-attributes/"));
                        volume.putMetric(query.getQuery(), metric);
                    }
                }
                volumes.put(name, volume);
                builder.reset();
            }
            return volumes;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Metrics for all Volumes: " + e.getMessage(), e);
        }
    }

    public Map<String, NetAppVM> gatherAggregateMetrics(Map<String, BaseQuery> queries) throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("aggr-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            Map<String, NetAppVM> volumes = new HashMap<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return volumes;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            for (NaElement node : nodes) {
                String xml = node.toString();
                InputSource is = new InputSource(new StringReader(xml));
                Document doc = builder.parse(is);
                NaElement ownerNode = node.getChildByName("aggr-ownership-attributes");
                NaElement stateNode = node.getChildByName("aggr-raid-attributes");
                String name = node.getChildContent("aggregate-name");
                NetAppVM aggregate = new NetAppVM(name, NetAppNode.NetAppNodeType.Aggregate);
                aggregate.setRunState(determineAggregateStatus(stateNode));
                aggregate.setController(ownerNode.getChildContent("owner-name"));
                for (BaseQuery query : queries.values()) {
                    if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX) &&
                            query.getQuery().startsWith("aggr-")) {
                        BaseMetric metric = new BaseMetric(query, query.getQuery());
                        metric.setCustomName(query.getCustomName());
                        metric.setValue(lookupMetric(doc, query.getQuery(), name, "/aggr-attributes/"));
                        aggregate.putMetric(query.getQuery(), metric);
                    }
                }
                volumes.put(name, aggregate);
                builder.reset();
            }
            return volumes;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Metrics for all Aggregates: " + e.getMessage(), e);
        }
    }

    private String lookupMetric(Document doc, String query, String vm, String prefix) {
        try {
            String xpathQuery = prefix + query.replace(".", "/");
            XPath xpath = getXPathFactory().newXPath();

            XPathExpression expression = expressions.get(xpathQuery);
            if (expression == null) {
                expression = xpath.compile(xpathQuery);
                expressions.put(xpathQuery, expression);
            }
            String value = (String)expression.evaluate(doc, XPathConstants.STRING);
            if (value == null || value.trim().equals("")) {
                value = "0";
            }
            return value;
        }
        catch (Exception e) {
            log.error("Failed parsing value for query expression: " + query + ", error: " + e.getMessage(), e);
            metricFaults.add(new MetricFaultInfo(query, vm));
            return null;
        }
    }

    public Map<String, NetAppHost> gatherControllerMetrics(Map<String, BaseQuery> queries) throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("system-node-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            Map<String, NetAppHost> controllers = new HashMap<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return controllers;
            }
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            for (NaElement node : nodes) {
                String xml = node.toString();
                InputSource is = new InputSource(new StringReader(xml));
                Document doc = builder.parse(is);
                String name = node.getChildContent("node");
                NetAppHost controller = new NetAppHost(name);
                controller.setRunState(determineControllerStatus(node));
                for (BaseQuery query : queries.values()) {
                    if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                        BaseMetric metric = new BaseMetric(query, query.getQuery());
                        metric.setCustomName(query.getCustomName());
                        if (query.getQuery().equals(FAILED_DISK_METRIC)) {
                            metric.setValue(Integer.toString(computeFailedControllerDisks(name)));
                        }
                        else {
                            metric.setValue(lookupMetric(doc, query.getQuery(), name, "/node-details-info/"));
                        }
                        controller.putMetric(query.getQuery(), metric);
                    }
                }
                controllers.put(name, controller);
                builder.reset();
            }
            return controllers;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Metrics for all Volumes: " + e.getMessage(), e);
        }
    }

    public int computeFailedControllerDisks(String controllerNode) throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("disk-sanown-list-info");
            nodeApi.addNewChild("node", controllerNode);
            nodeApi.addNewChild("ownership-type","all");

            NaElement root = server.invokeElem(nodeApi);
            Map<String, NetAppHost> controllers = new HashMap<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return 0;
            }
            List<NaElement> nodes = root.getChildByName("disk-sanown-details").getChildren();
            int failedCount = 0;
            int successCount = 0;
            for (NaElement node : nodes) {
                String failed = node.getChildContent("is_failed");
                if (failed == null) {
                    failedCount++;
                    continue;
                }
                if (Boolean.parseBoolean(failed) == true) {
                    successCount++;
                }
                else {
                    failedCount++;
                }
            }
            return failedCount;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Metrics for Disks: " + e.getMessage(), e);
        }
    }

    public Map<String, NetAppHost> gatherVServerMetrics() throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("vserver-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            Map<String, NetAppHost> vServers = new HashMap<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return vServers;
            }
            List<NaElement> servers = root.getChildByName("attributes-list").getChildren();
            for (NaElement server : servers) {
                if (server.getChildContent("vserver-type").equals("data")) {
                    String name = server.getChildContent("vserver-name");
                    NetAppHost vServer = new NetAppHost(name);
                    vServer.setRunState(determineServerStatus(server));
                    vServers.put(name, vServer);
                }
            }
            return vServers;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Metrics for all vServers: " + e.getMessage(), e);
        }
    }

    private synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
        if (factory == null) {
            factory = DocumentBuilderFactory.newInstance();
        }
        return factory;
    }

    private synchronized XPathFactory getXPathFactory() {
        if (xpathFactory == null) {
            xpathFactory = XPathFactory.newInstance();
        }
        return xpathFactory;

    }

    public List<MetricFaultInfo> getMetricFaults() {
        return metricFaults;
    }
}
