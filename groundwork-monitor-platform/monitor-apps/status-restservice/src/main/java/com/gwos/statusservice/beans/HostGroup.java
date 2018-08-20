package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class HostGroup extends BaseEntity {

	private Host[] host = null;
	
	private String alias = "NA";

	public Host[] getHost() {
		return host;
	}

	public void setHost(Host[] host) {
		this.host = host;
	}

	@XmlAttribute
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
