/**
 * 
 */
package com.groundworkopensource.portal.statusviewer.common;

/**
 * List of Different Sorting options available for search results.
 * 
 * @author nitin_jadhav
 * 
 */
public enum SortTypeEnum {

	/**
	 * alphabetic sort
	 */
	ALPHABETIC("Alphabetic"),
	/**
	 * HOST_HOSTGROUP_SERVICE_SERVICEGROUP
	 */
	HOST_HOSTGROUP_SERVICE_SERVICEGROUP("Host"),
	/**
	 * HOSTGROUP_HOST_SERVICEGROUP_SERVICE
	 */
	HOSTGROUP_HOST_SERVICEGROUP_SERVICE("HostGroup"),
	/**
	 * SERVICE_SERVICEGROUP_HOST_HOSTGROUP
	 */
	SERVICE_SERVICEGROUP_HOST_HOSTGROUP("Service"),
	/**
	 * SERVICEGROUP_SERVICE_HOSTGROUP_HOST
	 */
	SERVICEGROUP_SERVICE_HOSTGROUP_HOST("ServiceGroup");

	/**
	 * sort option to display on GUI and to compare while processing that option
	 */
	private String displayName;

	/**
	 * @param displayName
	 */
	private SortTypeEnum(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

}
