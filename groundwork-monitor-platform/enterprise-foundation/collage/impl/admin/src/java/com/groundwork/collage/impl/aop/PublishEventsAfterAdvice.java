/**
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.groundwork.collage.impl.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.events.EventService;
import org.springframework.aop.AfterReturningAdvice;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.LogMessage;

public class PublishEventsAfterAdvice implements AfterReturningAdvice {
    public void afterReturning(Object retValue, Method method, Object[] args,
            Object target) throws Throwable {
        String methodName = method.getName();
        if (retValue != null) {
            if (methodName != null && methodName.startsWith("updateLogMessage")) {
                if (retValue instanceof LogMessage) {
                    CollageFactory beanFactory = CollageFactory.getInstance();

                    LogMessage logMessage = (LogMessage) retValue;

                    Map<String, Object> attributes = new HashMap<String, Object>(
                            1);
                    attributes.put(EventService.NOTIFY_ATTR_ENTITY_ID,
                            logMessage.getLogMessageId());

                    ServiceNotify notify = new ServiceNotify(
                            ServiceNotifyEntityType.LOG_MESSAGE,
                            ServiceNotifyAction.UPDATE, attributes);
                    beanFactory.getEventService().publishEvent(notify);
                } // end if
            } // end if
            if (methodName != null
                    && methodName.startsWith("triggerAcknowledgeEventAOP")) {
                if (retValue instanceof ArrayList) {
                    ArrayList<Integer> messageIds = (ArrayList<Integer>) retValue;
                    // ////// JIRA 8732 start //////////
                    // process arguments in the collageadmin
                    // triggerAcknowledgeEventAOP
                    Object hostIdObj = args[1];
                    Object serviceIdObj = args[2]; // Third Argument in the
                    if (serviceIdObj == null) {
                        // Then host acknowledge
                        String hostId = (String) hostIdObj;
                        // Publish Host Acknowledgment to the topic.
                        publishEntity(new ServiceNotify(
                                ServiceNotifyEntityType.HOST,
                                ServiceNotifyAction.UPDATE_ACKNOWLEDGE, null),
                                hostId);

                    } else {
                        // Then Service acknowledge
                        String serviceId = (String) serviceIdObj;
                        // Publish Service Acknowledgment to the topic.
                        publishEntity(new ServiceNotify(
                                ServiceNotifyEntityType.SERVICESTATUS,
                                ServiceNotifyAction.UPDATE_ACKNOWLEDGE, null),
                                serviceId);
                    }
                    // ////// JIRA 8732 end ////////

                    for (int i = 0; i < messageIds.size(); i++) {
                        Map<String, Object> attributes = new HashMap<String, Object>(
                                1);
                        attributes.put(EventService.NOTIFY_ATTR_ENTITY_ID,
                                messageIds.get(i));

                        ServiceNotify notify = new ServiceNotify(
                                ServiceNotifyEntityType.LOG_MESSAGE,
                                ServiceNotifyAction.UPDATE, attributes);
                        CollageFactory beanFactory = CollageFactory
                                .getInstance();
                        beanFactory.getEventService().publishEvent(notify);
                    } // end for
                } // end if
            } // end if
        } // end if
    }

    /**
     * Publishes Host / Service entity notifications
     * 
     * @param notify
     * @param entityId
     */
    private void publishEntity(ServiceNotify notify, String entityId) {
        CollageFactory beanFactory = CollageFactory.getInstance();
        ConcurrentHashMap<String, String> distMap = beanFactory
                .getEntityPublisher().getDistinctEntityMap();

        StringBuffer sb = new StringBuffer();
        sb.append(notify.getAction());
        sb.append(":");
        sb.append(entityId);
        sb.append(";");
        String existingValue = null;
        if (distMap.get(notify.getEntityType().getValue()) != null) {
            existingValue = distMap.get(notify.getEntityType().getValue());
        }
        String currentValue = sb.toString();
        StringBuilder builder = new StringBuilder();
        // If the host is already in the list, don't add a duplicate one
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
        distMap.put(notify.getEntityType().getValue(), builder.toString());

    }
}
