package com.groundworkopensource.portal.model;

import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
@XmlRootElement(name = "customgroup_list")
public class CustomGroupList {
    private Collection<CustomGroup> list;
 
    public CustomGroupList() {
    }
 
    public CustomGroupList(Collection<CustomGroup> list) {
        this.list = list;
    }
    @XmlElement(name = "customgroup")
    public Collection<CustomGroup> getList() {
        return list;
    }
 
    public void setList(Collection<CustomGroup> list) {
        this.list = list;
    }
}