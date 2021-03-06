package com.gwos.statusservice.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class BaseEntity implements Serializable {

	private String name = null;

	private String status = null;
	
	
	private int ack = -1;
	
	private int in_downtime = -1;
	
	private String output = null;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlAttribute
	public int getAck() {
		return ack;
	}

	public void setAck(int ack) {
		this.ack = ack;
	}

	@XmlAttribute
	public int getIn_downtime() {
		return in_downtime;
	}

	public void setIn_downtime(int in_downtime) {
		this.in_downtime = in_downtime;
	}

	@XmlAttribute
	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
