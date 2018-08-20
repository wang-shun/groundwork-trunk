package com.groundworkopensource.portal.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
@XmlRootElement(name = "extendedrole_list")
public class ExtendedRoleList {
    private List<ExtendedUIRole> list;
 
    public ExtendedRoleList() {
    }
 
    public ExtendedRoleList(List<ExtendedUIRole> list) {
        this.list = list;
    }
    @XmlElement(name = "extendedrole")
    public List<ExtendedUIRole> getList() {
        return list;
    }
 
    public void setList(List<ExtendedUIRole> list) {
        this.list = list;
    }
}