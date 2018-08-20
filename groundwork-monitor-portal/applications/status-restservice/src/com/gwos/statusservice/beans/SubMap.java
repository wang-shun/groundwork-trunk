package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class SubMap extends Map {

	private String status = null;
	
	private int ack = 0;
	
	private int in_downtime = 0;
	
	private String output = null;

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
