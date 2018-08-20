package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statistic")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAvailability {

    @XmlAttribute
    private Double availability;

    @XmlAttribute
    private String queryBy;

    @XmlAttribute
    private String queryParam;

    @XmlAttribute
    private String queryValue;

    public DtoAvailability() {}

    public DtoAvailability(String queryBy, String queryParam, String queryValue, double availability) {
        this.queryBy = queryBy;
        this.queryParam = queryParam;
        this.queryValue = queryValue;
        this.availability = availability;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public String getQueryBy() {
        return queryBy;
    }

    public void setQueryBy(String queryBy) {
        this.queryBy = queryBy;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }

    public String getQueryValue() {
        return queryValue;
    }

    public void setQueryValue(String queryValue) {
        this.queryValue = queryValue;
    }
}
