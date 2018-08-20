package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "responseStatus")
public class ResponseStatus  implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String code = null;
	
	private String message = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}	

}
