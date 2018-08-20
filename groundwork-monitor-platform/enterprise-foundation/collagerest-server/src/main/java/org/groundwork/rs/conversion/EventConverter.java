package org.groundwork.rs.conversion;

import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.util.DateTime;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.PropertiesSupport;

import java.util.HashMap;
import java.util.Map;

public class EventConverter {

    public final static DtoEvent convert(LogMessage event) {
        DtoEvent dto = new DtoEvent();
        dto.setId(event.getLogMessageId());
        if (event.getDevice() != null) {
            dto.setDevice(event.getDevice().getIdentification());
        }
        if (event.getHostStatus() != null) {
            dto.setHost(event.getHostStatus().getHostName());
        }
        if (event.getServiceStatus() != null) {
            dto.setService(event.getServiceStatus().getServiceDescription());
        }
        if (event.getOperationStatus() != null) {
            dto.setOperationStatus(event.getOperationStatus().getName());
        }
        if (event.getMonitorStatus() != null) {
            dto.setMonitorStatus(event.getMonitorStatus().getName());
        }
        if (event.getSeverity() != null) {
            dto.setSeverity(event.getSeverity().getName());
        }
        if (event.getApplicationSeverity() != null) {
            dto.setApplicationSeverity(event.getApplicationSeverity().getName());
        }
        if (event.getComponent() != null) {
            dto.setComponent(event.getApplicationSeverity().getName());
        }
        if (event.getPriority() != null) {
            dto.setPriority(event.getPriority().getDescription());
        }
        if (event.getTypeRule() != null) {
            dto.setTypeRule(event.getTypeRule().getName());
        }
        if (event.getApplicationType() != null) {
            dto.setAppType(event.getApplicationType().getName());
        }
        dto.setTextMessage(event.getTextMessage());
        dto.setFirstInsertDate(event.getFirstInsertDate());
        dto.setLastInsertDate(event.getLastInsertDate());
        dto.setReportDate(event.getReportDate());
        dto.setMsgCount(event.getMsgCount());
        dto.setStateChanged(event.getStateChanged());
        dto.setProperties(PropertiesSupport.createDtoPropertyMap(event.getProperties(true)));
        return dto;
    }

    /**
     * All posted event properties are name value pairs of type string
     * Convert to a more specific map of name value pairs with value of type object
     *
     * @param properties a map of name value pairs of type string, string
     * @return a converted map of name value pairs
     */
    public final static Map<String, Object> convertEventMap(Map<String, String> properties) {
        Map<String, Object> converted = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equalsIgnoreCase(LogMessage.EP_MSG_COUNT)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_FIRST_INSERT_DATE)) {
                converted.put(key, DateTime.parse(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_LAST_INSERT_DATE)) {
                converted.put(key, DateTime.parse(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_REPORT_DATE)) {
                converted.put(key, DateTime.parse(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_APP_SEVERITY_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_COMPONENT_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_PRIORITY_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_OPERATION_STATUS_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_TYPE_RULE_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_STATE_CHANGED)) {
                converted.put(key, Boolean.parseBoolean(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_MONITOR_STATUS_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_SEVERITY_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_APPLICATION_TYPE_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_HOST_STATUS_ID)) {
                converted.put(key, Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(LogMessage.EP_SERVICE_STATUS_ID)) {
                converted.put(key, Integer.parseInt(value));
            }
            else {
                converted.put(key, value);
            }
        }
        return converted;
    }
}
