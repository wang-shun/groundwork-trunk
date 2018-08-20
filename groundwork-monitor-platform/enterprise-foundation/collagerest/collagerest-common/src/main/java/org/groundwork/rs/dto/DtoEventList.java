package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEventList {

    @XmlElement(name="event")
    @JsonProperty("events")
    private List<DtoEvent> events = new ArrayList<DtoEvent>();

    public DtoEventList() {}
    public DtoEventList(List<DtoEvent> events) { this.events = events; }

    public List<DtoEvent> getEvents() {
        return events;
    }

    public void add(DtoEvent event) {
        events.add(event);
    }

    public int size() {
        return events.size();
    }
}
