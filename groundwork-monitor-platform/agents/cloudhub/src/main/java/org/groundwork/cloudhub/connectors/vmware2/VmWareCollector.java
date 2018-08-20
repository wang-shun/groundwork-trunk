package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.vmware.VMwareConnector;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VmWareCollector {
    
    private static Logger log = Logger.getLogger(VmWareCollector.class);
    
    public MetricCollectionResult collectMetrics(List<PropertyCollectorSpec> specs, ServiceContent serviceContent, VimPortType vimPort) {

        ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
        MetricCollectionResult result = new MetricCollectionResult();

        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;
        try {

            // Build Container View
            List<String> listContainers = new ArrayList<String>();
            for (PropertyCollectorSpec spec : specs) {
                listContainers.add(spec.getInventoryType().name());
            }
            containerView = vimPort.createContainerView(
                    viewMgrRef,
                    serviceContent.getRootFolder(),
                    listContainers,
                    true);

            // Build a traversal spec
            TraversalSpec tSpec = new TraversalSpec();
            tSpec.setName("traverseEntities");
            tSpec.setType("ContainerView");
            tSpec.setPath("view");
            tSpec.setSkip(false);

            // create an object spec to define the beginning of the traversal;
            ObjectSpec oSpec = new ObjectSpec();
            oSpec.setObj(containerView);
            oSpec.setSkip(true);
            oSpec.getSelectSet().add(tSpec);

            List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();

            // Build Property Specs
            for (PropertyCollectorSpec spec : specs) {
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.setType(spec.getInventoryType().name());
                // walk thru metric specs to build property spec for each property in profile
                for (String metric : spec.getMetrics()) {
                    propertySpec.getPathSet().add(metric);
                }
                PropertyFilterSpec filterSpec = new PropertyFilterSpec();
                filterSpec.getObjectSet().add(oSpec);
                filterSpec.getPropSet().add(propertySpec);
                fSpecList.add(filterSpec);
            }

            RetrieveOptions ro = new RetrieveOptions();
            ArrayList<ObjectContent> ocList = new ArrayList<ObjectContent>();
            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());

            try {
                boolean firstround = true;
                RetrieveResult retrievalResult = null;
                int i = 0;

                while (true) {
                    if (firstround) {
                        retrievalResult = vimPort.retrievePropertiesEx(propertyCollector, fSpecList, ro);
                    }
                    else if (retrievalResult.getToken() != null) {
                        retrievalResult = vimPort.continueRetrievePropertiesEx(propertyCollector, retrievalResult.getToken());
                    }
                    else {
                        break;
                    }
                    firstround = false;
                    if (retrievalResult != null) {
                        ocList.addAll(retrievalResult.getObjects());
                    }
                }
                for (ObjectContent oc : ocList) {
                    String ocType = oc.getObj().getType();
                    String ocValue = oc.getObj().getValue();
                    result.addInstance(ocType, ocValue, oc.getPropSet());
                }
            } catch (InvalidPropertyFaultMsg e) {
                log.error(
                        "\n"
                                + "retrievePropertiesEx() error ='" + e + "'" + "\n"
                                + "Localized Message            ='" + e.getLocalizedMessage() + "'" + "\n"
                                + "Message                      ='" + e.getMessage() + "'" + "\n"
                                + "Cause                        ='" + e.getCause() + "'" + "\n"
                                + "FaultInfo                    ='" + e.getFaultInfo() + "'" + "\n"
                                + "FaultInfo.Name               ='" + e.getFaultInfo().getName() + "'" + "\n"
                                + "FaultInfo.Cause              ='" + e.getFaultInfo().getFaultCause() + "'" + "\n"
                                + "-----------------------------------------------------------------------------" + "\n"
                                + "Disable '" + e.getFaultInfo().getName() + "' (to not graphed/monitored) in the CloudHub config" + "\n"
                                + "... or remove from the '"
                                + ConnectorConstants.CONFIG_FILE_PATH
                                + ConnectorConstants.CONFIG_FILE_EXTN
                                + "' file\n"
                                + "-----------------------------------------------------------------------------" + "\n"
                );
            } catch (Exception e) {
                String message = "vimPort.retrievePropertiesEx(.2.) error";
                log.error(message + e.toString());
                log.error("localized msg: '" + e.getLocalizedMessage() + "', msg: '" + e.getMessage() + "', cause='" + e.getCause() + "'");
                throw new ConnectorException(e);
            }

        } catch (Exception e) {
            String message = "vimPort.retrievePropertiesEx(.2.) error";
            log.error(message + e.toString());
            log.error("localized msg: '" + e.getLocalizedMessage() + "', msg: '" + e.getMessage() + "', cause='" + e.getCause() + "'");
            throw new ConnectorException(e);
        }
        finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }
        return result;
    }
}
