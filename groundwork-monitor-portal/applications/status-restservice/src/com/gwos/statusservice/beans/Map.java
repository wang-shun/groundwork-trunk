package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;


public class Map {
	
	
	private String name = null;

	private HostGroup[] hostgroup = null;

	private Host[] host = null;

	private ServiceGroup[] servicegroup = null;

	private Service[] service = null;

	private SubMap[] submap = null;

	public HostGroup[] getHostgroup() {
		return hostgroup;
	}

	public void setHostgroup(HostGroup[] hostgroup) {
		this.hostgroup = hostgroup;
	}

	public Host[] getHost() {
		return host;
	}

	public void setHost(Host[] host) {
		this.host = host;
	}

	public ServiceGroup[] getServicegroup() {
		return servicegroup;
	}

	public void setServicegroup(ServiceGroup[] servicegroup) {
		this.servicegroup = servicegroup;
	}

	public Service[] getService() {
		return service;
	}

	public void setService(Service[] service) {
		this.service = service;
	}

	public SubMap[] getSubmap() {
		return submap;
	}

	public void setSubmap(SubMap[] submap) {
		this.submap = submap;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
