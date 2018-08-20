package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoServiceKeyList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="serviceKeyList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceKeyList {

    @XmlElement(name="serviceKey")
    @JsonProperty("serviceKeyList")
    private List<DtoServiceKey> serviceKeys = new ArrayList<>();

    public DtoServiceKeyList() {}
    public DtoServiceKeyList(List<DtoServiceKey> serviceKeys) {this.serviceKeys = serviceKeys;}

    public List<DtoServiceKey> getServiceKeys() {
        return serviceKeys;
    }

    public void add(DtoServiceKey serviceKey) {
        serviceKeys.add(serviceKey);
    }

    public int size() {
        return serviceKeys.size();
    }
}
