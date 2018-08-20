package org.groundwork.cloudhub.connectors.amazon;

import org.groundwork.cloudhub.metrics.BaseVM;

public class AmazonVM extends BaseVM {

    public enum AmazonNodeType {
        EC2,
        RDS,
        ELB
    }

    private AmazonNodeType nodeType;

    public AmazonVM(String name, AmazonNodeType nodeType) {
        super(name);
        this.nodeType = nodeType;
    }

    public AmazonNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(AmazonNodeType nodeType) {
        this.nodeType = nodeType;
    }
}
