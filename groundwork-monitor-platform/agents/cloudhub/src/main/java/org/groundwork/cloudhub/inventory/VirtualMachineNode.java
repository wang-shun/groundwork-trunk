package org.groundwork.cloudhub.inventory;

public class VirtualMachineNode extends InventoryNode {

    private String systemName;

    public VirtualMachineNode(String name, String systemName) {
        super(name);
        this.systemName = systemName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
