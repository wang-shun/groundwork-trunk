package com.groundwork.collage.query;

import java.util.Map;

public class HostGroupQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public HostGroupQueryTranslatorInfo() {
        super("HostGroup", "select distinct h", " from HostGroup h ", "select distinct count(*) from HostGroup h ",
                // NOTE: Hibernate is choking on the propertyValues path, thus queries on properties not possible
                //"h.hosts.hostStatus.propertyValues", "p" );
                "", "" );
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "h.hostGroupId");
        properties.put("appType".toLowerCase(), "h.applicationType.name");
        properties.put("applicationType".toLowerCase(), "h.applicationType.name");
    }

}
