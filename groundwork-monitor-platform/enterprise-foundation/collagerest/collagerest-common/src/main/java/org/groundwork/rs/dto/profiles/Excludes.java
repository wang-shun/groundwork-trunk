package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "excludes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Excludes {
    @XmlElement(name="exclude")
    private List<String> excludes;

    public Excludes() {
        excludes = new LinkedList<>();
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public void addExclude(String exclude) {
        if (excludes == null) {
            excludes = new LinkedList<String>();
        }
        excludes.add(exclude);
    }

}
