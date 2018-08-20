package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;

import java.util.List;
import java.util.Map;

final class AWSItemAZ extends AWSItem {

    private AvailabilityZone az;
    
    public AWSItemAZ(AvailabilityZone az) {
        this.az = az;
    }
    
    @Override
    public String getDisplayName() {
        return az.getZoneName();
    }
    
    @Override
    public void populateHostGroupDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {
        
        if (options.isViewHypervisors() && isInUse()) {
            InventoryContainerNode node = new InventoryContainerNode(getDisplayName());
            node.setStatus(getStatus());
            node.setTransient(true);
            inventory.getHypervisors().put(node.getName(), node);
        }
    }
    
    @Override
    public void collectHostGroupMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {

        if (isInUse()) {
            String displayName = getDisplayName();
            BaseHost azNode = new BaseHost(displayName);
            azNode.setTransient(true);
            BaseHost priorNode = priorState.hosts().get(displayName);
            if (priorNode != null) {
                azNode.setPrevRunState(priorNode.getRunState());
                priorVMMap.putAll(priorNode.getVMPool());
            }
            
            azNode.setRunExtra(az.getState());
            azNode.setRunState(getStatus());
            currentState.hosts().put(azNode.getHostName(), azNode);
        }
    }
    
    @Override
    public String getStatus() {
        if ("available".equals(az.getState().toLowerCase())) {
            return GwosStatus.UP.status;
        }
        return GwosStatus.UNSCHEDULED_DOWN.status;
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribeAvailabilityZonesResult azs = connection.getEC2Client().describeAvailabilityZones();
        for (AvailabilityZone az : azs.getAvailabilityZones()) {
            AWSItemAZ itemAZ = new AWSItemAZ(az);
            inventory.put(az.getZoneName(), itemAZ);
        }
    }
}
