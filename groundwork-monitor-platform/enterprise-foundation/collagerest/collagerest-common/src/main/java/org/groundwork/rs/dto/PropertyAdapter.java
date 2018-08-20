package org.groundwork.rs.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class PropertyAdapter extends XmlAdapter<DtoProperties, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(DtoProperties in) throws Exception {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (DtoProperty entry : in.entries()) {
            hashMap.put(entry.getName(), entry.getValue());
        }
        return hashMap;
    }

    @Override
    public DtoProperties marshal(Map<String, String> map) throws Exception {
        if (map == null)
            return null;
        DtoProperties props = new DtoProperties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            props.addEntry(new DtoProperty(entry.getKey(), entry.getValue()));
        }
        return props;
    }

}