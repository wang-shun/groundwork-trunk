package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPropertiesBase {

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertyAdapter.class)
    @JsonIgnore
    protected Map<String, String> properties;

    public DtoPropertiesBase() {
    }

    private Map<String,String> create() {
        properties = new HashMap<String, String>();
        return properties;
    }

    @JsonProperty("properties")
    public Map<String, String> getProperties() {
        if (properties == null)
            create();
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String name) {
        if (properties == null)
            create();
        return properties.get(name);
    }

    public void putProperty(String name, Object value) {
        if (properties == null)
            create();
        properties.put(name, PropertiesSupport.convertProperty(value));
    }

    public Double getPropertyDouble(String name) {
        if (properties == null)
            create();
        String prop = this.getProperty(name);
        Double result = 0.0;
        if (prop != null) {
            try {
                result = Double.parseDouble(prop);
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    public Long getPropertyLong(String name) {
        if (properties == null)
            create();
        String prop = this.getProperty(name);
        Long result = 0L;
        if (prop != null) {
            try {
                result = Long.parseLong(prop);
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    public Integer getPropertyInteger(String name) {
        if (properties == null)
            create();
        String prop = this.getProperty(name);
        Integer result = 0;
        if (prop != null) {
            try {
                result = Integer.parseInt(prop);
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    public boolean getPropertyBoolean(String name) {
        if (properties == null)
            create();
        String prop = this.getProperty(name);
        Boolean result = false;
        if (prop != null) {
            try {
                result = Boolean.parseBoolean(prop);
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    public static final String DATE_FORMAT =  "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public Date getPropertyDate(String name) {
        if (properties == null)
            create();
        String prop = this.getProperty(name);
        Date result = null;
        if (prop != null) {
            try {
                DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                result = formatter.parse(prop);
            }
            catch (Exception e) {
                DateFormat dateFormat = new SimpleDateFormat(UTC_DATE_FORMAT);
                try {
                    return dateFormat.parse(prop);
                }
                catch (Exception e2) {
                    return new Date();
                }
            }
        }
        else {
            result = new Date();
        }
        return result;
    }

}
