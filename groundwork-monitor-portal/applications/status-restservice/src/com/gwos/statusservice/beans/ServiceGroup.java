package com.gwos.statusservice.beans;

public class ServiceGroup extends BaseEntity {
	
	private Service[] service = null;

	public Service[] getService() {
		return service;
	}

	public void setService(Service[] service) {
		this.service = service;
	}

}
