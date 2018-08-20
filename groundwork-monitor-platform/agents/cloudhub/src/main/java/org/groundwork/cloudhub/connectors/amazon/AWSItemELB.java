package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;

import java.util.List;
import java.util.Map;

public class AWSItemELB extends AWSItem {
    private final static String CLOUDWATCH_DIMENSION_ELB = "LoadBalancerName";

    private LoadBalancerDescription loadBalancer;

    public AWSItemELB(LoadBalancerDescription loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public String getDisplayName() {
        return loadBalancer.getLoadBalancerName();
    }

    @Override
    public String getStatus() {
        return GwosStatus.UP.status;
    }

    @Override
    public void resolveRelationState(AWSInventory inventory) {
        for (String zone : loadBalancer.getAvailabilityZones()) {
            AWSItem azItem = inventory.get(zone, AWSItemAZ.class);
            if (azItem != null) {
                azItem.markInUse();
            }
        }
    }

    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribeLoadBalancersResult res = connection.getElbClient().describeLoadBalancers();
        for (LoadBalancerDescription balancer : res.getLoadBalancerDescriptions()) {
            inventory.put(balancer.getLoadBalancerName(), new AWSItemELB(balancer));
        }
    }

    @Override
    public void populateHostDataCenterInventory(DataCenterInventory inventory,
                                                InventoryOptions options, AWSInventory awsInventory) {
        VirtualMachineNode vmNode = new VirtualMachineNode(getDisplayName(), loadBalancer.getDNSName());
        vmNode.setStatus(getStatus());
        inventory.getVirtualMachines().put(vmNode.getName(), vmNode);
        if (options.isViewHypervisors()) {
            for (String azId : loadBalancer.getAvailabilityZones()) {
                AWSItem azItem = awsInventory.get(azId, AWSItemAZ.class);
                if (azItem != null) {
                    InventoryContainerNode azNode = inventory.getHypervisors().get(azItem.getDisplayName());
                    if (azNode != null) {
                        azNode.getVms().put(vmNode.getName(), vmNode);
                    }
                }
            }
        }
        if (options.isViewNetworks()) {
            AWSItem vpcItem = awsInventory.get(loadBalancer.getVPCId(), AWSItemVPC.class);
            if (vpcItem != null) {
                InventoryContainerNode vpcNode = inventory.getNetworks().get(vpcItem.getDisplayName());
                if (vpcNode != null) {
                    vpcNode.getVms().put(vmNode.getName(), vmNode);
                }
            }
        }
    }

    @Override
    public void collectHostMetrics(MonitoringState currentState, MonitoringState priorState,
                                   List<BaseQuery> awsQueries, List<BaseQuery> gwQueries, List<BaseQuery> customQueries,
                                   AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {

        String displayName = getDisplayName();
        BaseVM elbNode = new AmazonVM(displayName, AmazonVM.AmazonNodeType.ELB);
        BaseVM priorNode = priorVMMap.get(displayName);
        if (priorNode != null) {
            elbNode.setPrevRunState(priorNode.getRunState());
        }
        elbNode.setRunState(getStatus());
        populateAWSMetrics(awsQueries, elbNode, priorNode, connection, CLOUDWATCH_DIMENSION_ELB, loadBalancer.getLoadBalancerName(), SourceType.network);

        for (String azId : loadBalancer.getAvailabilityZones()) {
            AWSItem azItem = awsInventory.get(azId, AWSItemAZ.class);
            if (azItem != null) {
                BaseHost hostGroup = currentState.hosts().get(azItem.getDisplayName());
                if (hostGroup != null) {
                    hostGroup.putVM(displayName, elbNode);
                }
            }
        }

        AWSItem vpcItem = awsInventory.get(loadBalancer.getVPCId(), AWSItemVPC.class);
        if (vpcItem != null) {
            BaseHost hostGroup = currentState.hosts().get(vpcItem.getDisplayName());
            if (hostGroup != null) {
                hostGroup.putVM(displayName, elbNode);
            }
        }

    }
}