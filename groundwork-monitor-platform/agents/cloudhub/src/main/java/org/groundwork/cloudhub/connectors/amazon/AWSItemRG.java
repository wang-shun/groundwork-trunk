package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.util.List;

final class AWSItemRG extends AWSItem {

    private DBInstance replicaMaster;
    
    public AWSItemRG(DBInstance replicaMaster) {
        this.replicaMaster = replicaMaster;
    }
    
    @Override
    public String getDisplayName() {
        return AmazonConfigurationProvider.PREFIX_RDS_POOL + replicaMaster.getDBInstanceIdentifier();
    }

    @Override
    public void populateHostGroupDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {

        if (options.isViewResourcePools()) {
            InventoryContainerNode node = new InventoryContainerNode(getDisplayName());
            node.setStatus(getStatus());
            inventory.getResourcePools().put(node.getName(), node);
        }
    }
    
    @Override
    protected String getStatus() {
        String state = replicaMaster.getDBInstanceStatus().toLowerCase();
        if ("available".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("backing-up".equals(state) ||
                "modifying".equals(state) ||
                "rebooting".equals(state) ||
                "renaming".equals(state)||
                "resetting-master-credentials".equals(state)) {
            return GwosStatus.SCHEDULED_DOWN.status;
        } else if ("deleting".equals(state)) {
            return GwosStatus.UNREACHABLE.status;
        } else if ("creating".equals(state)) {
            return GwosStatus.PENDING.status;
        } else if ("failed".equals(state) ||
                "incompatible-network".equals(state) ||
                "incompatible-option-group".equals(state) ||
                "incompatible-parameters".equals(state) ||
                "incompatible-restore".equals(state)) {
            return GwosStatus.UNSCHEDULED_DOWN.status;
        } else if ("storage-full".equals(state)) {
            return GwosStatus.WARNING.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        for (;;) {
            DescribeDBInstancesResult rdsInstances = connection.getRDSClient().describeDBInstances(describeDBInstancesRequest);
            List<DBInstance> rdsInstanceList = rdsInstances.getDBInstances();
            for (DBInstance rdsInstance : rdsInstanceList) {
                List<String> replicas = rdsInstance.getReadReplicaDBInstanceIdentifiers();
                if (replicas != null && !replicas.isEmpty()) {
                    AWSItemRG itemRG = new AWSItemRG(rdsInstance);
                    inventory.put(rdsInstance.getDBInstanceIdentifier(), itemRG);
                }
            }
            
            String nextBatchId = rdsInstances.getMarker();
            if (nextBatchId == null) {
                break;
            }
            describeDBInstancesRequest.setMarker(nextBatchId);
        }
    }
}
