package org.groundwork.cloudhub.connectors.openstack;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.connectors.openstack.client.HypervisorInfo;
import org.groundwork.cloudhub.connectors.openstack.client.NovaClient;
import org.groundwork.cloudhub.connectors.openstack.client.VmInfo;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenStackInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(OpenStackInventoryBrowser.class);

    private OpenStackConnection connection;
    private Map<String, String> hostMap = new ConcurrentHashMap<String, String>();

    public OpenStackInventoryBrowser(OpenStackConnection connection) {
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
        NovaClient nova = new NovaClient(connection);
        List<HypervisorInfo> hypervisors =  nova.listHypervisors();
        for (HypervisorInfo hypervisor : hypervisors) {
            if (hypervisor.state == HypervisorState.up) {
                InventoryContainerNode node = new InventoryContainerNode(hypervisor.name);
                node.setStatus(HypervisorState.convertToGroundworkStatus(hypervisor.state));
                inventory.getHypervisors().put(hypervisor.name, node);
            }
        }
    }

    public void retrieveVirtualMachines(DataCenterInventory inventory, String hypervisor) {
        NovaClient nova = new NovaClient(connection);
        List<VmInfo> vms =  nova.listVirtualMachines(hypervisor);
        for (VmInfo vm : vms) {
            VirtualMachineNode vmNode = new VirtualMachineNode(vm.name, vm.id);
            vmNode.setStatus(OpenStackStatus.convertToGroundworkStatus(vm.status));
            inventory.getVirtualMachines().put(vm.name, vmNode);
            InventoryContainerNode host = inventory.getHypervisors().get(vm.hypervisor);
            if (host != null)
                host.putVM(vm.name, vmNode);
        }
    }

}
