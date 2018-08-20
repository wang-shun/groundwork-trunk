package org.groundwork.cloudhub.configuration;

/**
 * This wrapper is binded to /usr/local/groundwork/Info.txt.
 * The file contains the info about the groundwork installed.
 * 
 * @author Muhammad Yousaf
 * @version 1.0
 *
 */

public class GWProductInfo {

	private String name;
	private String version;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

}
