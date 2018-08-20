package com.groundwork.collage.query;

import java.util.Map;

public class ServiceQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public ServiceQueryTranslatorInfo() {
        super("ServiceStatus", "select distinct s", " from ServiceStatus s ", "select distinct count(*) from ServiceStatus s ", "s.propertyValues", "p");
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "s.serviceStatusId");
        properties.put("name".toLowerCase(), "serviceDescription");
        properties.put("description".toLowerCase(), "serviceDescription");
        properties.put("host".toLowerCase(), "s.host.hostName");
        properties.put("hostName".toLowerCase(), "s.host.hostName");
        properties.put("monitorStatus".toLowerCase(), "s.monitorStatus.name");
        properties.put("monitor".toLowerCase(), "s.monitorStatus.name");
        properties.put("appType".toLowerCase(), "s.applicationType.name");
        properties.put("applicationType".toLowerCase(), "s.applicationType.name");
        properties.put("hostGroup".toLowerCase(), "s.host.hostGroups.name");
        properties.put("stateType".toLowerCase(), "s.stateType.name");
        properties.put("checkType".toLowerCase(), "s.checkType.name");
        properties.put("lastHardState".toLowerCase(), "s.lastHardState.name");
        properties.put("agentId".toLowerCase(), "s.agentId");
        properties.put("hostAgentId".toLowerCase(), "s.host.agentId");
    }
    
}
