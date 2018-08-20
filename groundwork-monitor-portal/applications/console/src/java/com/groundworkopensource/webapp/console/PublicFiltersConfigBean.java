/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Vector;

public class PublicFiltersConfigBean {
	private Vector<FilterConfigBean> filterConfigs;

	public PublicFiltersConfigBean() {
		filterConfigs = new Vector<FilterConfigBean>();
	}

	public Vector<FilterConfigBean> getFilterConfigs() {
		return filterConfigs;
	}

	public void addFilterConfigs(FilterConfigBean filterConfig) {
		filterConfigs.add(filterConfig);
	}

}
