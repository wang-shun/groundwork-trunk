package org.groundwork.cloudhub.connectors.netapp;

import org.groundwork.cloudhub.inventory.VirtualMachineNode;

/**
 * Created by dtaylor on 6/30/15.
 */
public class NetAppNode extends VirtualMachineNode {

    private String controller;
    private String aggregate;
    private NetAppNodeType nodeType;

    public enum NetAppNodeType {
        Volume,
        NetAppNodeType, Aggregate
    };

    public NetAppNode(NetAppNodeType nodeType, String name, String systemName) {
        super(name, systemName);
        this.nodeType = nodeType;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }

    public NetAppNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NetAppNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isVolume() {
        return nodeType == NetAppNodeType.Volume;
    }

    public boolean isAggregate() {
        return nodeType == NetAppNodeType.Aggregate;
    }
}
