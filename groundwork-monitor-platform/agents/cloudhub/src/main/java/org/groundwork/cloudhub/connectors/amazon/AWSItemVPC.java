package org.groundwork.cloudhub.connectors.amazon;

import java.util.List;
import java.util.Map;

import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;

import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Vpc;

final class AWSItemVPC extends AWSItem {

    private Vpc vpc;
    
    public AWSItemVPC(Vpc vpc) {
        this.vpc = vpc;
    }
    
    @Override
    public String getDisplayName() {
        return vpc.getVpcId();
    }
    
    @Override
    public void populateHostGroupDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {
        
        if (options.isViewNetworks()) {
            InventoryContainerNode node = new InventoryContainerNode(getDisplayName());
            node.setStatus(getStatus());
            inventory.getNetworks().put(node.getName(), node);
        }
    }
    
    @Override
    public void collectHostGroupMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {

        String displayName = getDisplayName();
        BaseHost vpcNode = new BaseHost(displayName);
        BaseHost priorNode = priorState.hosts().get(displayName);
        if (priorNode != null) {
            vpcNode.setPrevRunState(priorNode.getRunState());
            priorVMMap.putAll(priorNode.getVMPool());
        }
        
        vpcNode.setRunExtra(vpc.getState());
        vpcNode.setRunState(getStatus());
        currentState.hosts().put(vpcNode.getHostName(), vpcNode);
    }
    
    @Override
    protected String getStatus() {
        String state = vpc.getState().toLowerCase();
        if ("available".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("pending".equals(state)) {
            return GwosStatus.PENDING.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        DescribeVpcsResult vpcs = connection.getEC2Client().describeVpcs(describeVpcsRequest);
        for (Vpc vpc : vpcs.getVpcs()) {
            AWSItemVPC itemVPC = new AWSItemVPC(vpc);
            inventory.put(vpc.getVpcId(), itemVPC);
        }
    }

}
