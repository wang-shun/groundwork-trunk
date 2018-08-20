package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ManagedObjectReference;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.Map;

@Component
public class HostMetricConverter extends VmWareBaseConverter  {

    @Autowired
    protected MetricsUtils metricsUtils;

    public VMwareHost convert(MetricCollectorInstance instance, Map<String, BaseQuery> queryPool, Map<String, String> customNames, Map<String, VMwareVM> vmPool) {
        String name = (String) instance.getProperty(PROP_NAME);
        VMwareHost host = new VMwareHost(name);
        host.setSystemName(instance.getName());

        // Host VMs association
        ArrayOfManagedObjectReference property = (ArrayOfManagedObjectReference)instance.getProperty(PROP_VM);
        if (property instanceof ArrayOfManagedObjectReference) {
            List<ManagedObjectReference> morList = ((ArrayOfManagedObjectReference) property).getManagedObjectReference();
            for (ManagedObjectReference reference : morList) {
                VMwareVM pooledVM = vmPool.get(reference.getValue());
                host.putVM(pooledVM.getVMName(), pooledVM);
            }
        }
        // Boot Time
        Object bootTime = instance.getProperty(PROP_BOOTTIME);
        if (bootTime != null && bootTime instanceof XMLGregorianCalendar) {
            host.setBootDate(((XMLGregorianCalendar) bootTime).toGregorianCalendar());
        }
        // Up Time
        Integer upTime = (Integer) instance.getProperty(PROP_HOST_UPTIME);
        if (upTime != null) {
            host.setLastUpdate(upTime.toString());
        }
        // Hardware Model
        String hardwareModel = (String) instance.getProperty(PROP_HOST_MODEL);
        if (hardwareModel != null) {
            host.setDescription(hardwareModel);
        }

        // Convert all properties to GW metrics
        metricsUtils.processMetrics(host, instance, queryPool, customNames);

        // Process Synthetics
        metricsUtils.processSynthetics(instance, host, null, queryPool, customNames);
        
        // TODO: improve on getMonitorState, there are properties being processed outside converter
        host.setRunningState(host.getMonitorState());

        return host;
    }


}