package org.groundwork.rs.plugins;

import java.util.Vector;

public class Plugin implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9155650889267575609L;

	private String name = null;

	private String url = null;
	
	private String arch = null;
	
	private String lastUpdateDate = null;
	
	private String lastUpdateTimestamp = null;
	
	private String checksum = null;
	
	private String lastUpdatedBy = null;

	private Vector<Dependency> dependency = null;

	public Plugin() {
		dependency = new Vector<Dependency>();
	}

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

	public Dependency[] getDependency() {
		Dependency[] retDependencies = new Dependency[dependency.size()];
        if (dependency.size() > 0) dependency.copyInto(retDependencies);
        return retDependencies;
	}

	public void setDependency(Dependency[] newDependencies) {
		dependency = new Vector<Dependency>(newDependencies.length);
        for (int i=0; i < newDependencies.length; i++)
        {
        	dependency.addElement(newDependencies[i]);
        }
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(String lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	

}