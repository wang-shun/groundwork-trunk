package org.groundwork.cloudhub.inventory;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.gwos.GWOSHost;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataCenterInventory {

    private static Logger log = Logger.getLogger(DataCenterInventory.class);

    private InventoryOptions options;
    private Map<String, String> systemNameMap = new ConcurrentHashMap<String, String>();
    private Map<String, VirtualMachineNode> vms = new ConcurrentHashMap<String, VirtualMachineNode>();
    private Map<String, InventoryContainerNode> hypervisors = new ConcurrentHashMap<String, InventoryContainerNode>();
    private Map<String, InventoryContainerNode> networks = new ConcurrentHashMap<String, InventoryContainerNode>();
    private Map<String, InventoryContainerNode> datastores = new ConcurrentHashMap<String, InventoryContainerNode>();
    private Map<String, InventoryContainerNode> resourcePools = new ConcurrentHashMap<String, InventoryContainerNode>();
    private Map<String, InventoryContainerNode> taggedGroups = new ConcurrentHashMap<String, InventoryContainerNode>();
    private Map<String, GWOSHost> allHosts = new HashMap<>();

    public DataCenterInventory(InventoryOptions options) {
        this.options = options;
    }

    public InventoryOptions getOptions() {
        return options;
    }

    public Map<String, String> getSystemNameMap() {
        return systemNameMap;
    }

    public Map<String, VirtualMachineNode> getVirtualMachines() {
        return vms;
    }

    public Map<String, InventoryContainerNode> getHypervisors() {
        return hypervisors;
    }

    public Map<String, InventoryContainerNode> getNetworks() {
        return networks;
    }

    public Map<String, InventoryContainerNode> getDatastores() {
        return datastores;
    }

    public Map<String, InventoryContainerNode> getResourcePools() {
        return resourcePools;
    }

    public Map<String, InventoryContainerNode> getTaggedGroups() {
        return taggedGroups;
    }

    public void debug() {
        log.info("================== Begin Debugging VmWareInventory ==============");
        log.info("------------------ Virtual Machines -----------------------------");
        for (String key : getVirtualMachines().keySet()) {
            VirtualMachineNode vm = getVirtualMachines().get(key);
            System.out.format("Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
        }
        log.info("Total Virtual Machines: " + getVirtualMachines().size());
        log.info("------------------ System Name Map ------------------------------");
        for (Map.Entry<String,String> entry : getSystemNameMap().entrySet()) {
            System.out.format("System Name Map - key: %s, value: %s\n", entry.getKey(), entry.getValue());
        }
        log.info("Total Map Entries: " + getSystemNameMap().size());
        log.info("------------------ Hypervisors ----------------------------------");
        for (InventoryContainerNode node : getHypervisors().values()) {
            System.out.format("Hypervisor Host - name: %s, vm count: %d\n", node.getName(), node.getVms().size());
            for (String key : node.getVms().keySet()) {
                VirtualMachineNode vm = node.getVms().get(key);
                System.out.format("\t ... Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
            }
        }
        log.info("------------------ Networks   ----------------------------------");
        for (InventoryContainerNode node : getNetworks().values()) {
            System.out.format("Network - name: %s, vm count: %d\n", node.getName(), node.getVms().size());
            for (String key : node.getVms().keySet()) {
                VirtualMachineNode vm = node.getVms().get(key);
                System.out.format("\t ... Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
            }
        }
        log.info("------------------ Datastores ----------------------------------");
        for (InventoryContainerNode node : getDatastores().values()) {
            System.out.format("Datastore - name: %s, vm count: %d\n", node.getName(), node.getVms().size());
            for (String key : node.getVms().keySet()) {
                VirtualMachineNode vm = node.getVms().get(key);
                System.out.format("\t ... Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
            }
        }
        log.info("------------------ ResourcePools -------------------------------");
        for (InventoryContainerNode node : getResourcePools().values()) {
            System.out.format("Resource Pool - name: %s, vm count: %d\n", node.getName(), node.getVms().size());
            for (String key : node.getVms().keySet()) {
                VirtualMachineNode vm = node.getVms().get(key);
                System.out.format("\t ... Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
            }
        }
        log.info("------------------ TaggedGroups -------------------------------");
        for (InventoryContainerNode node : getTaggedGroups().values()) {
            System.out.format("Tagged Group - name: %s, vm count: %d\n", node.getName(), node.getVms().size());
            for (String key : node.getVms().keySet()) {
                VirtualMachineNode vm = node.getVms().get(key);
                System.out.format("\t ... Virtual Machine - key: %s, name: %s, sys.name: %s\n", key, vm.getName(), vm.getSystemName());
            }
        }
        log.info("================== End Debugging VmWareInventory ==============");
    }

    public Map<String, GWOSHost> getAllHosts() {
        return allHosts;
    }

    public void setAllHosts(Map<String, GWOSHost> allHosts) {
        this.allHosts = allHosts;
    }
}
