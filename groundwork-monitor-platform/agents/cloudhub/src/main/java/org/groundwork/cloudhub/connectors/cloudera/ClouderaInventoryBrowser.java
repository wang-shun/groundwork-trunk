package org.groundwork.cloudhub.connectors.cloudera;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiHost;
import com.cloudera.api.model.ApiHostList;
import com.cloudera.api.v14.RootResourceV14;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.util.List;

public class ClouderaInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(ClouderaInventoryBrowser.class);

    private RootResourceV14 rootResource;
    private List<ConfigurationView> views;

    public ClouderaInventoryBrowser(RootResourceV14 rootResource, List<ConfigurationView> views) {
        this.rootResource = rootResource;
        this.views = views;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException {
        DataCenterInventory inventory = new DataCenterInventory(options);
        boolean enabled = false;
        if (enabled) {
            ConfigurationView clusterView = ClouderaConfigurationProvider.getView(views, ClouderaConfigurationProvider.CLOUDERA_CLUSTER);
            if (clusterView.isEnabled()) {
                ApiClusterList clusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
                for (ApiCluster cluster : clusters) {
                    inventory.getHypervisors().put(cluster.getName(), new InventoryContainerNode(cluster.getName(), cluster.getName()));
                    //ApiServiceList services = v14.getClustersResource().getServicesResource(cluster.getName()).readServices(DataView.SUMMARY);
                }
            }
            ConfigurationView hostView = ClouderaConfigurationProvider.getView(views, ClouderaConfigurationProvider.CLOUDERA_HOST);
            if (hostView.isEnabled()) {
                ApiHostList hosts = rootResource.getHostsResource().readHosts(DataView.SUMMARY);
                for (ApiHost host : hosts) {
                    System.out.println("host: " + host.toString());
                    System.out.println("health: " + host.getHealthSummary().toString());
                    //v12.getHostsResource().getMetrics()
                }
            }
        }
        return inventory;
    }

}
