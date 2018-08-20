package org.groundwork.cloudhub.connectors.opendaylight;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.opendaylight.client.FlowClient;
import org.groundwork.cloudhub.connectors.opendaylight.client.ServerInfo;
import org.groundwork.cloudhub.connectors.opendaylight.client.VmInfo;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenDaylightInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(OpenDaylightInventoryBrowser.class);

    private Map<String, String> hostMap = new ConcurrentHashMap<String, String>();
    private OpenDaylightConnection connection = null;

    public OpenDaylightInventoryBrowser(OpenDaylightConnection connection) {
       this.connection = connection;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException
    {
        DataCenterInventory inventory = new DataCenterInventory(options);
        retrieveHypervisors(inventory);
        for (InventoryContainerNode node : inventory.getHypervisors().values()) {
            retrieveVirtualMachines(inventory, node.getName());
        }
        return inventory;
    }

    public void retrieveHypervisors(DataCenterInventory inventory) {
        FlowClient flowClient = new FlowClient(connection);
        List<ServerInfo> hypervisors =  flowClient.listHypervisors();
        for (ServerInfo hypervisor : hypervisors) {
            InventoryContainerNode node = new InventoryContainerNode(hypervisor.name);
            inventory.getHypervisors().put(hypervisor.name, node);
        }
    }

    public void retrieveVirtualMachines(DataCenterInventory inventory, String hypervisor) {
        FlowClient flowClient = new FlowClient(this.connection);
        List<VmInfo> vms =  flowClient.listVirtualMachines(hypervisor);
        for (VmInfo vm : vms) {
            VirtualMachineNode vmNode = new VirtualMachineNode(vm.name, vm.name);
            inventory.getVirtualMachines().put(vm.name, vmNode);
            InventoryContainerNode host = inventory.getHypervisors().get(vm.hypervisor);
            if (host != null)
                host.putVM(vm.name, vmNode);
        }

    }

}
