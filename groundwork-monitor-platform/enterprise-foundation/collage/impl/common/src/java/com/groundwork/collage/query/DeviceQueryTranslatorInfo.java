package com.groundwork.collage.query;

import java.util.Map;

public class DeviceQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public DeviceQueryTranslatorInfo() {
        super("Device", "select distinct d", " from Device d ", "select distinct count(*) from Device d ", "d.propertyValues", "p");
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "deviceId");
        properties.put("device".toLowerCase(), "identification");
        properties.put("name".toLowerCase(), "displayName");
        properties.put("server".toLowerCase(), "d.monitorServer.monitorServerName");
        properties.put("ip".toLowerCase(), "d.monitorServer.ip");
    }
}
