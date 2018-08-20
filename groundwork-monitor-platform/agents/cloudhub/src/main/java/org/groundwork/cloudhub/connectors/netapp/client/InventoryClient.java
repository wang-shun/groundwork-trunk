package org.groundwork.cloudhub.connectors.netapp.client;

import netapp.manage.NaElement;
import netapp.manage.NaServer;
import org.groundwork.cloudhub.connectors.netapp.NetAppNode;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;

import java.util.ArrayList;
import java.util.List;

public class InventoryClient extends BaseNetAppClient {


    public InventoryClient(NaServer server) {
        super(server);
    }

    public List<InventoryContainerNode> listControllers() throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("system-node-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            String status = root.getAttr("status");
            List<InventoryContainerNode> controllers = new ArrayList<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return controllers;
            }
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            for (NaElement node : nodes) {
                InventoryContainerNode controller =
                        new InventoryContainerNode(node.getChildContent("node"), node.getChildContent("node-uuid") );
                controller.setStatus(determineControllerStatus(node));
                controllers.add(controller);
            }
            return controllers;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Nodes (controllers): " + e.getMessage(), e);
        }
    }

    public List<InventoryContainerNode> listVServers() throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("vserver-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            String status = root.getAttr("status");
            List<InventoryContainerNode> vServers = new ArrayList<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return vServers;
            }
            List<NaElement> servers = root.getChildByName("attributes-list").getChildren();
            for (NaElement server : servers) {
                if (server.getChildContent("vserver-type").equals("data")) {
                    InventoryContainerNode vServer =
                            new InventoryContainerNode(server.getChildContent("vserver-name"), server.getChildContent("uuid"));
                    vServer.setStatus(determineServerStatus(server));
                    vServers.add(vServer);
                }
            }
            return vServers;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp vServers: " + e.getMessage(), e);
        }
    }

    public List<NetAppNode> listVolumes() throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("volume-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            String status = root.getAttr("status");
            List<NetAppNode> volumes = new ArrayList<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return volumes;
            }
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            for (NaElement node : nodes) {
                NaElement idNode = node.getChildByName("volume-id-attributes");
                NaElement stateNode = node.getChildByName("volume-state-attributes");
                NetAppNode volume = new NetAppNode(NetAppNode.NetAppNodeType.Volume, idNode.getChildContent("name"),
                                idNode.getChildContent("uuid") );
                volume.setStatus(determineVolumeStatus(stateNode));
                volume.setController(idNode.getChildContent("owning-vserver-name"));
                volume.setAggregate(idNode.getChildContent("containing-aggregate-name"));
                volumes.add(volume);
            }
            return volumes;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Volumes: " + e.getMessage(), e);
        }
    }

    public List<NetAppNode> listAggregates() throws ConnectorException {
        try {
            NaElement nodeApi = new NaElement("aggr-get-iter");
            NaElement root = server.invokeElem(nodeApi);
            String status = root.getAttr("status");
            List<NetAppNode> volumes = new ArrayList<>();
            if (root.getChildIntValue("num-records", 0) == 0) {
                return volumes;
            }
            List<NaElement> nodes = root.getChildByName("attributes-list").getChildren();
            for (NaElement node : nodes) {
                NaElement ownerNode = node.getChildByName("aggr-ownership-attributes");
                NaElement stateNode = node.getChildByName("aggr-raid-attributes");
                NetAppNode volume =
                        new NetAppNode(NetAppNode.NetAppNodeType.Aggregate, node.getChildContent("aggregate-name"),
                                node.getChildContent("aggregate-uuid") );
                volume.setStatus(determineAggregateStatus(stateNode));
                volume.setController(ownerNode.getChildContent("owner-name"));
                volumes.add(volume);
            }
            return volumes;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve NetApp Volumes: " + e.getMessage(), e);
        }
    }

}
