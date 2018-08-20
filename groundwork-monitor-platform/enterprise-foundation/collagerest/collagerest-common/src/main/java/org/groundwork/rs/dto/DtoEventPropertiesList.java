package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEventPropertiesList {

    @XmlElement(name="event")
    @JsonProperty("events")
    private List<DtoEventProperties> events = new ArrayList<DtoEventProperties>();

    public DtoEventPropertiesList() {}
    public DtoEventPropertiesList(List<DtoEventProperties> events) { this.events = events; }

    public List<DtoEventProperties> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addEvent(DtoEventProperties event) {
        events.add(event);
    }

    public int size() {
        return events.size();
    }
}

