package com.groundworkopensource.webapp.console;

import java.io.Serializable;

public class DynaProperty implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String propName = null;
	private String propValue=null;
	private String dataType=null;
	private String operator = null;
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	public String getPropValue() {
		return propValue;
	}
	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}

}
