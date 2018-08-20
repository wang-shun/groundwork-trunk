package com.gwos.statusservice.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "maps")
public class Maps implements Serializable {
	
	//@XmlAttribute(name = "map")
	//@XmlElementWrapper(name = "mapList")
	private Map[] map = null;

	public Map[] getMap() {
		return map;
	}

	public void setMap(Map[] map) {
		this.map = map;
	}
	
	
}
