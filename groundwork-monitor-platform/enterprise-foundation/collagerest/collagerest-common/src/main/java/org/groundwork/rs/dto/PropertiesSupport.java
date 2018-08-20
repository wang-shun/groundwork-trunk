package org.groundwork.rs.dto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesSupport {

    static final JAXBDateAdapter dateAdapter = new JAXBDateAdapter();

    public static final Map<String, String> convertToMap(List<DtoProperty> properties) {
        Map<String, String> map = new HashMap<String, String>();
        if (properties != null) {
            for (DtoProperty property : properties) {
                map.put(property.getName(), property.getValue()) ;
            }
        }
        return map;
    }

    public static final List<DtoProperty> createDtoPropertyList(Map<String, Object> collageProperties) {
        List<DtoProperty> properties = new ArrayList<DtoProperty>();
        if (collageProperties != null) {
            if (collageProperties != null) {
                for (String key : collageProperties.keySet()) {
                    Object value = collageProperties.get(key);
                    properties.add(new DtoProperty(key, value));
                }
            }
        }
        return properties;
    }

    /**
     * Convert Map of Collage entity properties into an unfiltered
     * DTO property Map.
     *
     * @param collageProperties Collage entity properties
     * @return DTO property Map
     */
    public static final Map<String, String> createDtoPropertyMap(Map<String,Object> collageProperties) {
        return createDtoPropertyMap(collageProperties, null);
    }

    /**
     * Convert Map of Collage entity properties into a DTO property
     * Map. Properties converted can be filtered by key.
     *
     * @param collageProperties Collage entity properties
     * @param filters property key filter
     * @return DTO property Map
     */
    public static final Map<String, String> createDtoPropertyMap(Map<String,Object> collageProperties,
                                                                 String [] filters) {
        Map<String,String> properties = new HashMap<String,String>();
        if (collageProperties != null) {
            for (String key : collageProperties.keySet()) {
                // check properties filters
                if (filters != null) {
                    boolean filtered = true;
                    for (String filter : filters) {
                        if (filter.equalsIgnoreCase(key)) {
                            filtered = false;
                            break;
                        }
                    }
                    if (filtered) {
                        continue;
                    }
                }
                // include property
                Object value = collageProperties.get(key);
                properties.put(key, convertProperty(value));
            }
        }
        return properties;
    }

    public static final String convertProperty(Object property) {
        String value;
        if (property instanceof String) {
            if (property == null) {
                value = "";
            }
            else {
                value = (String) property;
            }
        }
        else if (property instanceof Calendar) {
            if (property == null) {
                value = "";
            }
            else {
                value = dateAdapter.marshal(((Calendar) property).getTime());
            }
        }
        else if (property instanceof Date)  {
            if (property == null) {
                value = "";
            }
            else {
                value = dateAdapter.marshal((Date) property);
            }
        }
        else if (property instanceof Boolean) {
            if (property == null) {
                value = Boolean.FALSE.toString();
            }
            else {
                value = ((Boolean) property).toString();
            }
        }
        else if (property instanceof Integer) {
            if (property == null) {
                value = "0";
            }
            else {
                value = ((Integer) property).toString();
            }
        }
        else if (property instanceof Long)  {
            if (property == null) {
                value = "0";
            }
            else {
                value = ((Long) property).toString();
            }
        }
        else if (property instanceof Double) {
            if (property == null) {
                value = "0";
            }
            else {
                DecimalFormat format = new DecimalFormat("###.##");
                value = format.format((Double) property);
            }
        }
        else
            value = "";
        return value;
    }
}
