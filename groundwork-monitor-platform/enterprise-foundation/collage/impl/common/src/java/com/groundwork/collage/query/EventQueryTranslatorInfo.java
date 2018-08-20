package com.groundwork.collage.query;

import java.util.Map;

public class EventQueryTranslatorInfo  extends AbstractQueryTranslatorInfo  implements QueryTranslatorInfo {

    public EventQueryTranslatorInfo() {
        super("LogMessage", "select distinct m", " from LogMessage m ", "select distinct count(*) from LogMessage m ", "m.propertyValues", "p");
    }

    protected void createBasicProperties(Map properties) {
        properties.put("deviceName".toLowerCase(), "m.device.identification");
        properties.put("device".toLowerCase(), "m.device.identification");
        properties.put("host".toLowerCase(), "m.hostStatus.host.hostName");
        properties.put("hostName".toLowerCase(), "m.hostStatus.host.hostName");
        properties.put("service".toLowerCase(), "m.serviceStatus.serviceDescription");
        properties.put("serviceStatus".toLowerCase(), "m.serviceStatus.serviceDescription");
        properties.put("serviceDescription".toLowerCase(), "m.serviceStatus.serviceDescription");
        properties.put("operation".toLowerCase(), "m.operationStatus.name");
        properties.put("operationStatus".toLowerCase(), "m.operationStatus.name");
        properties.put("monitor".toLowerCase(), "m.monitorStatus.name");
        properties.put("monitorStatus".toLowerCase(), "m.monitorStatus.name");
        properties.put("severity".toLowerCase(), "m.severity.name");
        properties.put("applicationSeverity".toLowerCase(), "m.applicationSeverity.name");
        properties.put("appSeverity".toLowerCase(), "m.applicationSeverity.name");
        properties.put("appType".toLowerCase(), "m.applicationType.name");
        properties.put("applicationType".toLowerCase(), "m.applicationType.name");
        properties.put("component".toLowerCase(), "m.component.name");
        properties.put("priority".toLowerCase(), "m.priority.name");
        properties.put("typeRule".toLowerCase(), "m.typeRule.name");
        properties.put("textMessage".toLowerCase(), "textMessage");
        properties.put("message".toLowerCase(), "textMessage");
        properties.put("msgCount".toLowerCase(), "msgCount");
        properties.put("count".toLowerCase(), "msgCount");
        properties.put("hostGroup".toLowerCase(), "m.hostStatus.host.hostGroups.name");
        properties.put("category.name".toLowerCase(), "m.serviceStatus.serviceStatusId");
        properties.put("category".toLowerCase(), "m.serviceStatus.serviceStatusId");
        properties.put("servicegroup".toLowerCase(), "m.serviceStatus.serviceStatusId");
        properties.put("id".toLowerCase(), "m.logMessageId");
        properties.put("lastInsertDate".toLowerCase(), "m.lastInsertDate");
        properties.put("firstInsertDate".toLowerCase(), "m.firstInsertDate");
        properties.put("reportDate".toLowerCase(), "m.reportDate");

    }

}
