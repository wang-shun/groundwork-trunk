package com.groundworkopensource.portal.model;

import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
@XmlRootElement(name = "navigation_list")
public class NavigationList {
    private Collection<UserNavigation> list;
 
    public NavigationList() {
    }
 
    public NavigationList(Collection<UserNavigation> list) {
        this.list = list;
    }
    @XmlElement(name = "navigation")
    public Collection<UserNavigation> getList() {
        return list;
    }
 
    public void setList(Collection<UserNavigation> list) {
        this.list = list;
    }
}