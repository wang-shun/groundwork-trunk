package org.groundwork.cloudhub.connectors.vmware;

import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VMwareInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(VMwareInventoryBrowser.class);

    public enum InventoryType {
        DataCenter,
        VirtualMachine,
        Datastore,
        Folder,
        Network,
        HostSystem,
        ResourcePool,
        ClusterComputeResource
    }

    private static final String PROP_NAME = "name";

    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private ManagedObjectReference rootFolder;

    public VMwareInventoryBrowser(VimPortType vimPort, ServiceContent serviceContent) {
        this.vimPort = vimPort;
        this.serviceContent = serviceContent;
        this.rootFolder = serviceContent.getRootFolder();
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException {
        DataCenterInventory inventory = new DataCenterInventory(options);
        retrieveVirtualMachines(inventory.getVirtualMachines(), inventory.getSystemNameMap());
        if (options.isViewHypervisors()) {
            retrieveManagedObjects(rootFolder, InventoryType.HostSystem, inventory.getHypervisors(), inventory.getSystemNameMap());
        }
        if (options.isViewDatastores()) {
            retrieveManagedObjects(rootFolder, InventoryType.Datastore, inventory.getDatastores(), inventory.getSystemNameMap());
        }
        if (options.isViewNetworks()) {
            retrieveManagedObjects(rootFolder, InventoryType.Network, inventory.getNetworks(), inventory.getSystemNameMap());
        }
        if (options.isViewResourcePools()) {
            retrieveManagedObjects(rootFolder, InventoryType.ResourcePool, inventory.getResourcePools(), inventory.getSystemNameMap());
        }
        return inventory;
    }

    /**
     * Returns an Inventory of all the ManagedObjects for a given folder
     *
     * @param folder        {@link ManagedObjectReference} of the folder to begin the search
     *                      from
     * @param inventoryType Type of the managed entity that needs to be searched
     * @param inventory     collection of managed objects to be added to
     * @return Map of name-->InventoryNode of the managed objects present. If none
     * exist then empty Map is returned
     */
    private Map<String, InventoryContainerNode> retrieveManagedObjects(ManagedObjectReference folder,
                                                                       InventoryType inventoryType,
                                                                       Map<String, InventoryContainerNode> inventory,
                                                                       Map<String, String> systemNameMap)
            throws ConnectorException {

        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            containerView = vimPort.createContainerView(viewManager, folder, Arrays.asList(inventoryType.name()), true);

            // Create Property Spec
            PropertySpec namePropertySpec = new PropertySpec();
            namePropertySpec.setAll(Boolean.FALSE);
            namePropertySpec.setType(inventoryType.name());
            namePropertySpec.getPathSet().add(PROP_NAME);

            PropertySpec vmPropertySpec = new PropertySpec();
            vmPropertySpec.setAll(Boolean.FALSE);
            vmPropertySpec.setType(inventoryType.name());
            vmPropertySpec.getPathSet().add("vm");

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            // Create PropertyFilterSpec using the PropertySpec and ObjectPec
            // created above.
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(namePropertySpec);
            propertyFilterSpec.getPropSet().add(vmPropertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());
            List<ObjectContent> objectContents = vimPort.retrieveProperties(propertyCollector, propertyFilterSpecs);
            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    ManagedObjectReference mr = oc.getObj();
                    String entityName = null;
                    List<DynamicProperty> dps = oc.getPropSet();
                    InventoryNode node = null;
                    List<ManagedObjectReference> vmRefList = null;
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equals("name")) {
                                entityName = (String) dp.getVal();
                                //systemName = mr.getValue();
                            } else if (dp.getName().equals("vm")) {
                                if (dp.getVal() instanceof ArrayOfManagedObjectReference) {
                                    vmRefList = ((ArrayOfManagedObjectReference) dp.getVal()).getManagedObjectReference();
                                }
                            }
                        }
                    }
                    if (entityName != null) {
                        // Resource Pool only dupe check, use the largest resource pool, last in wins on equal size
                        if (inventoryType == InventoryType.ResourcePool) {
                            InventoryContainerNode dupe = inventory.get(entityName);
                            if (dupe != null) {
                                if (vmRefList.size() < dupe.getVms().size()) {
                                    continue;
                                }
                            }
                        }
                        InventoryContainerNode inventoryNode = new InventoryContainerNode(entityName);
                        if (vmRefList != null) {
                            for (ManagedObjectReference vm : vmRefList) {
                                String systemName = vm.getValue();
                                String vmName = systemNameMap.get(systemName);
                                if (vmName != null) {
                                    VirtualMachineNode vmNode = new VirtualMachineNode(vmName, systemName);
                                    inventoryNode.putVM(vmName, vmNode);
                                }
                            }
                        }
                        inventory.put(entityName, inventoryNode);
                    }
                }
            }
        } catch (Exception e) {
            throw new ConnectorException("Failed to retrieve managed objects for " + inventoryType.name(), e);
        } finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }
        return inventory;
    }

    /**
     * Returns an Inventory of all virtual machines in system
     *
     * @return Map of name-->Virtual Machine
     */
    private Map<String, VirtualMachineNode> retrieveVirtualMachines(Map<String, VirtualMachineNode> vms,
                                                                    Map<String, String> systemNameMap)
            throws ConnectorException {

        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            containerView = vimPort.createContainerView(viewManager, this.rootFolder,
                    Arrays.asList(InventoryType.VirtualMachine.name()), true);

            // Create Property Spec
            PropertySpec namePropertySpec = new PropertySpec();
            namePropertySpec.setAll(Boolean.FALSE);
            namePropertySpec.setType(InventoryType.VirtualMachine.name());
            namePropertySpec.getPathSet().add(PROP_NAME);

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            // Create PropertyFilterSpec using the PropertySpec and ObjectPec
            // created above.
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(namePropertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs =
                    new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());
            List<ObjectContent> oCont = vimPort.retrieveProperties(propertyCollector, propertyFilterSpecs);
            if (oCont != null) {
                for (ObjectContent oc : oCont) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    VirtualMachineNode vm = null;
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equals("name")) {
                                String vmName = (String) dp.getVal();
                                vm = new VirtualMachineNode(vmName, mr.getValue());
                                systemNameMap.put(vm.getSystemName(), vmName);
                            }
                        }
                    }
                    if (vm != null) {
                        vms.put(vm.getName(), vm);
                    }
                }
            }
        } catch (Exception e) {
            throw new ConnectorException("Failed to retrieve virtual machines", e);
        } finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }
        return vms;
    }

}
