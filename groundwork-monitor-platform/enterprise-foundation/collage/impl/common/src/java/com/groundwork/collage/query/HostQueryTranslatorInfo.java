package com.groundwork.collage.query;

import java.util.Map;

public class HostQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public HostQueryTranslatorInfo() {
        super("Host", "select distinct h", " from Host h ", "select distinct count(*) from Host h ", "h.hostStatus.propertyValues", "p");
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "h.hostId");
        properties.put("name".toLowerCase(), "hostName");
        properties.put("host".toLowerCase(), "hostName");
        properties.put("monitorStatus".toLowerCase(), "h.hostStatus.hostMonitorStatus.name");
        properties.put("monitor".toLowerCase(), "h.hostStatus.hostMonitorStatus.name");
        properties.put("appType".toLowerCase(), "h.applicationType.name");
        properties.put("applicationType".toLowerCase(), "h.applicationType.name");
        properties.put("device".toLowerCase(), "h.device.identification");
        properties.put("deviceIdentification".toLowerCase(), "h.device.identification");
        properties.put("deviceName".toLowerCase(), "h.device.displayName");
        properties.put("deviceDisplayName".toLowerCase(), "h.device.displayName");
        properties.put("lastCheckTime".toLowerCase(), "h.hostStatus.lastCheckTime");
        properties.put("hostGroup".toLowerCase(), "h.hostGroups.name");
        properties.put("stateType".toLowerCase(), "s.hostStatus.stateType.name");
        properties.put("checkType".toLowerCase(), "s.hostStatus.checkType.name");
    }

}
