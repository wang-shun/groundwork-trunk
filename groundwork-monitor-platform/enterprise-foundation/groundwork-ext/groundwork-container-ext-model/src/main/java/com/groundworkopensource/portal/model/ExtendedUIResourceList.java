package com.groundworkopensource.portal.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by ArulShanmugam on 12/23/14.
 */
@XmlRootElement(name = "resource_list")
public class ExtendedUIResourceList implements Serializable {

    public List<ExtendedUIResource> getList() {
        return list;
    }

    public void setList(List<ExtendedUIResource> list) {
        this.list = list;
    }

    private List<ExtendedUIResource> list;
}
