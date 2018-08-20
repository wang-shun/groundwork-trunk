package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.ec2.model.DescribePlacementGroupsResult;
import com.amazonaws.services.ec2.model.PlacementGroup;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.util.List;

final class AWSItemPG extends AWSItem {

    private PlacementGroup pg;
    
    public AWSItemPG(PlacementGroup pg) {
        this.pg = pg;
    }
    
    @Override
    public String getDisplayName() {
        return AmazonConfigurationProvider.PREFIX_EC2_POOL + pg.getGroupName();
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
    public String getStatus() {
        String state = pg.getState().toLowerCase();
        if ("available".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("deleted".equals(state)) {
            return GwosStatus.SCHEDULED_DOWN.status;
        } else if ("deleting".equals(state)) {
            // While stuck in the "Deleting" state, we consider it unreachable.
            return GwosStatus.UNREACHABLE.status;
        } else if ("pending".equals(state)) {
            return GwosStatus.PENDING.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribePlacementGroupsResult placementGroups = connection.getEC2Client().describePlacementGroups();
        List<PlacementGroup> placementGroupList = placementGroups.getPlacementGroups();
        for (PlacementGroup placementGroup : placementGroupList) {
            AWSItemPG itemPG = new AWSItemPG(placementGroup);
            inventory.put(placementGroup.getGroupName(), itemPG);
        }
    }
}
