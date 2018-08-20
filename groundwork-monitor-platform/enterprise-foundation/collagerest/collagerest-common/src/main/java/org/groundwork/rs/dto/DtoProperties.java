package org.groundwork.rs.dto;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DtoProperties {
    @XmlElement(name = "property")
    private List<DtoProperty> entries = new ArrayList<DtoProperty>();

    List<DtoProperty> entries() {
        return Collections.unmodifiableList(entries);
    }

    void addEntry(DtoProperty entry) {
        entries.add(entry);
    }
}