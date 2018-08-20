package com.groundwork.collage.impl.aop;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.impl.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStatusAfterAdvice implements AfterReturningAdvice {

    StatisticsService statisticsService = null;
    private Log log = LogFactory.getLog(this.getClass());

    private static final String METHOD_UPDATE_SERVICE_STATUS = "updateServiceStatus";
    private static final String METHOD_REMOVE_SERVICE_STATUS = "removeService";

    public ServiceStatusAfterAdvice(StatisticsService statService) {
        statisticsService = statService;
    }

    public void afterReturning(Object returnValue, Method method,
                               Object[] arg2, Object arg3) throws Throwable {
        String methodName = method.getName();
        log.info("Method name :" + methodName);
        ServiceNotify notify = null;
        boolean isDelete = false;
        boolean isNotifyAndPublish = true;
        ServiceStatus serviceStatus = null;
        Map<String, Object> notifyAttributes = new HashMap<String, Object>(2);
        if (methodName.equalsIgnoreCase(METHOD_UPDATE_SERVICE_STATUS)) {
            serviceStatus = (ServiceStatus) returnValue;
            if (serviceStatus == null) {
                // don't notify on no update
                return;
            }
            /* Filtering by MonitorStatus only doesn't trigger changes to isAck and isInDowntime in the RTMM
			 * Removing the check will generate more RTMM updates but on the other hand makes sure that all
			  * properties are propagated.*/
            // String oldMonitorStatus = serviceStatus.getLastMonitorStatus();
            // String newMonitorStatus = (serviceStatus.getMonitorStatus() == null) ? null : serviceStatus.getMonitorStatus().getName();
            // if ((oldMonitorStatus != null && newMonitorStatus != null) && oldMonitorStatus.equalsIgnoreCase(newMonitorStatus)) {
            //    isNotifyAndPublish = false;
            // }
            notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_ID,
                    serviceStatus.getServiceStatusId());
            notify = new ServiceNotify(
                    ServiceNotifyEntityType.SERVICESTATUS,
                    ServiceNotifyAction.UPDATE, notifyAttributes);
            log.info("AOP Advice -- ServiceStatus ["
                    + serviceStatus.getServiceDescription()
                    + "] updated..");
        } // end if
        if (methodName.equalsIgnoreCase(METHOD_REMOVE_SERVICE_STATUS)) {
            isDelete = true;
            if (returnValue == null || (Integer) returnValue == -1) {
                // don't notify on bad service status ids
                return;
            }
            notifyAttributes.put(StatisticsService.NOTIFY_ATTR_ENTITY_ID,
                    (Integer) returnValue);
            notify = new ServiceNotify(ServiceNotifyEntityType.SERVICESTATUS,
                    ServiceNotifyAction.DELETE, notifyAttributes);
            log.info("AOP Advice -- ServiceStatusID [" + (Integer) returnValue
                    + "] deleted.");
        } // end if
        log.info("About to notify the statisctics service...");
        statisticsService.notify(notify);
        try {
            if (isNotifyAndPublish) {
                this.publishEntity(notify, returnValue, isDelete);
                if (serviceStatus != null && serviceStatus.getHost() != null) {
                    Set<HostGroup> groups = serviceStatus.getHost().getHostGroups();
                    HostGroupPublisher.publish(groups);
                }
            }

        } catch (Exception e) {
            log.error("AOP Advisor ServiceStatus: Failed to update/delete host - ", e);
        }
    }

    /**
     * Publishes the Host or Service notifications only
     *
     * @param notify
     * @param returnValue
     */
    private void publishEntity(ServiceNotify notify, Object returnValue,
                               boolean isDelete) {
        log.info("Publishing ServiceStatus....");
        CollageFactory beanFactory = CollageFactory.getInstance();
        try {
            ConcurrentHashMap<String, String> distMap = beanFactory
                    .getEntityPublisher().getDistinctEntityMap();
            if (distMap != null) {
                int serviceId = -1;
                if (returnValue != null) {
                    if (isDelete) {
                        serviceId = ((Integer) returnValue).intValue();
                    } else {
                        serviceId = ((ServiceStatus) returnValue)
                                .getServiceStatusId().intValue();
                    } // end if
                } // end if
                StringBuffer sb = new StringBuffer();
                sb.append(notify.getAction());
                sb.append(":");
                sb.append(serviceId);
                sb.append(";");
                String existingValue = null;
                if (distMap.get(ServiceNotifyEntityType.SERVICESTATUS.getValue()) != null) {
                    existingValue = distMap
                            .get(ServiceNotifyEntityType.SERVICESTATUS.getValue());

                }

                String currentValue = sb.toString();
                StringBuilder builder = new StringBuilder();
                // If the hostgroup is already in the list, don't add a duplicate
                // one
                if (existingValue == null) {
                    builder.append(currentValue);
                } else {
                    if (existingValue.indexOf(currentValue) == -1) {
                        builder.append(existingValue);
                        builder.append(currentValue);
                    } else {
                        builder.append(existingValue);
                    } // end if

                } // end if
                distMap.put(ServiceNotifyEntityType.SERVICESTATUS.getValue(),
                        builder.toString());
            } // end if
        } catch (NoSuchBeanDefinitionException NSBDE) {
            log.warn("IGNORE FOR TEST RUNS!" + NSBDE.getMessage());
        }
    }

}
