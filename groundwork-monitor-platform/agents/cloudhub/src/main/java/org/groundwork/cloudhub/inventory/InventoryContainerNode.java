package org.groundwork.cloudhub.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryContainerNode extends InventoryNode {

    private Map<String, VirtualMachineNode> vms = new ConcurrentHashMap<String, VirtualMachineNode>();
    private String prefixedName;
    private boolean isTransient = false;

    public InventoryContainerNode(String name) {
        super(name);
    }

    public InventoryContainerNode(String name, String prefixedName) {
        super(name);
        this.prefixedName = prefixedName;
    }

    public String getPrefixedName() {
        return prefixedName;
    }

    public void setPrefixedName(String prefixedName) {
        this.prefixedName = prefixedName;
    }

    public Map<String, VirtualMachineNode> getVms() {
        return vms;
    }

    public VirtualMachineNode lookupVM(String name) {
        return vms.get(name);
    }

    public void putVM(String name, VirtualMachineNode vm) {
        vms.put(name, vm);
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean aTransient) {
        isTransient = aTransient;
    }
}
