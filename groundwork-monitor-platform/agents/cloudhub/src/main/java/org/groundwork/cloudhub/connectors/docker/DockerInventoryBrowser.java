package org.groundwork.cloudhub.connectors.docker;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.docker.client.ContainerInfo;
import org.groundwork.cloudhub.connectors.docker.client.DockerEngineInfo;
import org.groundwork.cloudhub.connectors.docker.client.InventoryClient;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DockerInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(DockerInventoryBrowser.class);

    private Map<String, String> hostMap = new ConcurrentHashMap<String, String>();
    private DockerConnection connection = null;
    private int apiLevel = 2;

    public DockerInventoryBrowser(DockerConnection connection, int apiLevel) {
        this.connection = connection;
        this.apiLevel = apiLevel;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException {
        DataCenterInventory inventory = new DataCenterInventory(options);
        retrieveHypervisors(inventory);
        for (InventoryContainerNode node : inventory.getHypervisors().values()) {
            retrieveVirtualMachines(inventory, node.getName());
        }
        return inventory;
    }

    public void retrieveHypervisors(DataCenterInventory inventory) {
        InventoryClient inventoryClient = new InventoryClient(connection, apiLevel);
        List<DockerEngineInfo> engines = inventoryClient.listDockerEngines();
        for (DockerEngineInfo engine : engines) {
            InventoryContainerNode node = new InventoryContainerNode(engine.name);
            inventory.getHypervisors().put(engine.name, node);
        }
    }

    public void retrieveVirtualMachines(DataCenterInventory inventory, String hypervisor) {
        InventoryClient inventoryClient = new InventoryClient(this.connection, apiLevel);
        List<ContainerInfo> containers = inventoryClient.listContainers(hypervisor);
        for (ContainerInfo container : containers) {
            VirtualMachineNode vmNode = new VirtualMachineNode(container.name, container.id);
            inventory.getVirtualMachines().put(container.name, vmNode);
            InventoryContainerNode host = inventory.getHypervisors().get(container.engine);
            if (host != null)
                host.putVM(container.name, vmNode);
        }

    }

}
