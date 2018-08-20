package com.groundwork.collage.impl.aop;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.HostGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HostGroupPublisher {

    private static Log log = LogFactory.getLog(HostGroupPublisher.class);

    public static void publish(Set<HostGroup> groups) {
        if (groups != null) {
            Map<String, Object> notifyAttributes = new HashMap<String, Object>();
            for (HostGroup hostGroup : groups) {
                // Add HostGroup Name
                notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, hostGroup.getName());
                ServiceNotify notify = new ServiceNotify(ServiceNotifyEntityType.HOSTGROUP,
                        ServiceNotifyAction.UPDATE, notifyAttributes);
                publishHostGroup(notify, hostGroup);
            }
        }
    }

    /**
     * Publishes the Host notifications
     *
     * @param notify
     * @param returnValue
     */
     public static void publishHostGroup(ServiceNotify notify, Object returnValue) {

        log.info("Publishing HostGroup....");
        if (returnValue == null)
            return;

        CollageFactory beanFactory = CollageFactory.getInstance();
        ConcurrentHashMap<String, String> distMap = beanFactory
                .getEntityPublisher().getDistinctEntityMap();
        int hostGroupId = -1;
        if (returnValue instanceof HostGroup) {
            log.debug("Distinct Map : " + distMap);
            if (distMap != null) {
                if (returnValue != null) {
                    hostGroupId = ((HostGroup) returnValue).getHostGroupId()
                            .intValue();
                } // end if
            } // end if
        } else if (returnValue instanceof Integer) {
            hostGroupId = ((Integer) returnValue).intValue();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(notify.getAction());
        sb.append(":");
        sb.append(hostGroupId);
        sb.append(";");
        String existingValue = null;
        if (distMap.get(ServiceNotifyEntityType.HOSTGROUP.getValue()) != null) {
            existingValue = distMap.get(ServiceNotifyEntityType.HOSTGROUP
                    .getValue());
        } // end if
        String currentValue = sb.toString();
        StringBuilder builder = new StringBuilder();
        // If the hostgroup is already in the list, don't add a duplicate one
        if (existingValue == null) {
            builder.append(currentValue);
        } else {
            if (existingValue.indexOf(currentValue) == -1) {
                builder.append(existingValue);
                builder.append(currentValue);
            } else {
                builder.append(existingValue);
            } // end if
        }
        distMap.put(ServiceNotifyEntityType.HOSTGROUP.getValue(), builder
                .toString());
    } // end method

}
