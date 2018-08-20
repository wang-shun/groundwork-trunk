package org.groundwork.cloudhub.connectors.netapp;

import netapp.manage.NaServer;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.netapp.client.InventoryClient;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetAppInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(NetAppInventoryBrowser.class);

    private Map<String, String> hostMap = new ConcurrentHashMap<String, String>();

    private NaServer server = null;

    public NetAppInventoryBrowser(NaServer server) {
       this.server = server;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException
    {
        DataCenterInventory inventory = new DataCenterInventory(options);
        InventoryClient inventoryClient = new InventoryClient(server);
        List<InventoryContainerNode> controllers = inventoryClient.listControllers();
        for (InventoryContainerNode controller : controllers) {
            inventory.getHypervisors().put(controller.getName(), controller);
        }
        List<InventoryContainerNode> vServers = inventoryClient.listVServers();
        for (InventoryContainerNode vServer : vServers) {
            inventory.getHypervisors().put(vServer.getName(), vServer);
        }
        List<NetAppNode> volumes = inventoryClient.listVolumes();
        if (options.isViewDatastores() && volumes.size() > 0) {
            inventory.getDatastores().put(NetAppConfigurationProvider.NETAPP_VOLUMES_HOSTGROUP, new InventoryContainerNode(NetAppConfigurationProvider.NETAPP_VOLUMES_HOSTGROUP));
        }
        for (NetAppNode volume : volumes) {
            inventory.getVirtualMachines().put(volume.getName(), volume);
            InventoryContainerNode controller = inventory.getHypervisors().get(volume.getController());
            if (controller != null) {
                controller.getVms().put(volume.getName(), volume);
            }
            if (options.isViewDatastores() && volumes.size() > 0) {
                inventory.getDatastores().get(NetAppConfigurationProvider.NETAPP_VOLUMES_HOSTGROUP).getVms().put(volume.getName(), volume);
            }
        }
        List<NetAppNode> aggregates = inventoryClient.listAggregates();
        if (options.isViewNetworks() && aggregates.size() > 0) {
            inventory.getNetworks().put(NetAppConfigurationProvider.NETAPP_AGGREGATE_HOSTGROUP, new InventoryContainerNode(NetAppConfigurationProvider.NETAPP_AGGREGATE_HOSTGROUP));
        }
        for (NetAppNode aggregate : aggregates) {
            inventory.getVirtualMachines().put(aggregate.getName(), aggregate);
            InventoryContainerNode controller = inventory.getHypervisors().get(aggregate.getController());
            if (controller != null) {
                controller.getVms().put(aggregate.getName(), aggregate);
            }
            if (options.isViewNetworks() && aggregates.size() > 0) {
                inventory.getNetworks().get(NetAppConfigurationProvider.NETAPP_AGGREGATE_HOSTGROUP).getVms().put(aggregate.getName(), aggregate);
            }
        }
        return inventory;
    }

}
