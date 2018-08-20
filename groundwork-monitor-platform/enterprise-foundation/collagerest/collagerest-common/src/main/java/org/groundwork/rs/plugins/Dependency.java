package org.groundwork.rs.plugins;

public class Dependency implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3327131682988133436L;

	private String name = null;
	
	private String url = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	

}
