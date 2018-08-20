package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.ManagedObjectReference;
import com.doublecloud.vim25.mo.Folder;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.ServiceInstance;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import java.util.Hashtable;
import java.util.Map;

import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_NAME;
import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_VM;

public class VIInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(VIInventoryBrowser.class);

    private Folder rootFolder;
    private InventoryNavigator navigator;

    public VIInventoryBrowser(ServiceInstance serviceInstance) {
        rootFolder = serviceInstance.getRootFolder();
        navigator = new InventoryNavigator(rootFolder);
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException {
        DataCenterInventory inventory = new DataCenterInventory(options);
        retrieveVirtualMachines(inventory.getVirtualMachines(), inventory.getSystemNameMap());
        if (options.isViewHypervisors()) {
            retrieveManagedObjects(InventoryType.HostSystem, inventory.getHypervisors(), inventory.getSystemNameMap());
        }
        if (options.isViewDatastores()) {
            retrieveManagedObjects(InventoryType.Datastore, inventory.getDatastores(), inventory.getSystemNameMap());
        }
        if (options.isViewNetworks()) {
            retrieveManagedObjects(InventoryType.Network, inventory.getNetworks(), inventory.getSystemNameMap());
        }
        if (options.isViewResourcePools()) {
            retrieveManagedObjects(InventoryType.ResourcePool, inventory.getResourcePools(), inventory.getSystemNameMap());
        }
        return inventory;
    }

    /**
     * Returns an Inventory of all virtual machines in system
     *
     * @return Map of name-->Virtual Machine
     */
    private Map<String, VirtualMachineNode> retrieveVirtualMachines(Map<String, VirtualMachineNode> vms,
                                                                    Map<String, String> systemNameMap)
            throws ConnectorException {


        try {
            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities(InventoryType.VirtualMachine.name());
            if (managedEntities == null || managedEntities.length == 0) {
                return vms;
            }
            Hashtable[] names = PropertyCollectorUtil.retrieveProperties(managedEntities, InventoryType.VirtualMachine.name(), new String[] {PROP_NAME});
            for (int ix = 0; ix < managedEntities.length; ix++) {
                VirtualMachineNode vm = new VirtualMachineNode((String)names[ix].get(PROP_NAME), managedEntities[ix].getMOR().getVal());
                systemNameMap.put(vm.getSystemName(), vm.getName());
                vms.put(vm.getName(), vm);
            }
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve virtual machines", e);
        }
        return vms;
    }


    private Map<String, InventoryContainerNode> retrieveManagedObjects(InventoryType inventoryType,
                                                                       Map<String, InventoryContainerNode> inventory,
                                                                       Map<String, String> systemNameMap)
            throws ConnectorException {
        try {
            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities(inventoryType.name());
            if (managedEntities == null || managedEntities.length == 0) {
                return inventory;
            }
            Hashtable[] properties = PropertyCollectorUtil.retrieveProperties(managedEntities, inventoryType.name(), new String[] {PROP_NAME, PROP_VM});
            for (int ix = 0; ix < managedEntities.length; ix++) {
                String name = (String)properties[ix].get(PROP_NAME);
                Object managedObject = properties[ix].get(PROP_VM);
                if (managedObject instanceof ManagedObjectReference[]) {
                    ManagedObjectReference[] vmRefList = (ManagedObjectReference[]) managedObject;
                    // Resource Pool only dupe check, use the largest resource pool, last in wins on equal size
                    if (inventoryType == InventoryType.ResourcePool) {
                        InventoryContainerNode dupe = inventory.get(name);
                        if (dupe != null) {
                            if (vmRefList.length < dupe.getVms().size()) {
                                continue;
                            }
                        }
                    }
                    InventoryContainerNode inventoryNode = new InventoryContainerNode(name);
                    inventory.put(inventoryNode.getName(), inventoryNode);

                    for (ManagedObjectReference vm : vmRefList) {
                        String systemName = vm.getVal();
                        String vmName = systemNameMap.get(systemName);
                        if (vmName != null) {
                            VirtualMachineNode vmNode = new VirtualMachineNode(vmName, systemName);
                            inventoryNode.putVM(vmName, vmNode);
                        }
                    }
                }
                else {
                    InventoryContainerNode inventoryNode = new InventoryContainerNode(name);
                    inventory.put(inventoryNode.getName(), inventoryNode);
                }
            }
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve managed objects for " + inventoryType.name(), e);
        }
        return inventory;
    }

}
