/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package com.groundworkopensource.webapp.console;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

public class SearchBean {

	private String host;
	private String message;
	private String ageType = "preset";
	private Date ageValueFrom;
	private Date ageValueTo;
	private String presetValue;
	private boolean presetRendered = true;
	private boolean customRendered;
	public static final String PRESET_NONE="none";
	public static final String PRESET_LAST6HR="last6hr";
	public static final String PRESET_LAST12HR="last12hr";
	public static final String PRESET_LAST24HR="last24hr";
	public static final String PRESET_LASTHR="lasthr";
	public static final String PRESET_LAST10MINS="last10min";
	public static final String PRESET_LAST30MINS="last30min";

	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAgeType() {
		return ageType;
	}

	public void setAgeType(String ageType) {
		this.ageType = ageType;
	}

	public Date getAgeValueFrom() {
		return ageValueFrom;
	}

	public void setAgeValueFrom(Date ageValueFrom) {
		this.ageValueFrom = ageValueFrom;
	}

	public Date getAgeValueTo() {
		return ageValueTo;
	}

	public void setAgeValueTo(Date ageValueTo) {
		this.ageValueTo = ageValueTo;
	}

	public String getPresetValue() {
		return presetValue;
	}

	public void setPresetValue(String presetValue) {
		this.presetValue = presetValue;
	}

	public void selectionChanged(ValueChangeEvent event) {
		if (ageType.equals("preset")) {
			ageType = "custom";
		} else {
			ageType = "preset";
		} // end if
	}

	public boolean isPresetRendered() {
		return presetRendered;
	}

	public void setPresetRendered(boolean presetRendered) {
		this.presetRendered = presetRendered;
	}

	public boolean isCustomRendered() {
		return customRendered;
	}

	public void setCustomRendered(boolean customRendered) {
		this.customRendered = customRendered;
	}

	public void reset() {
		host = null;
		message = null;
		ageType = "preset";
		ageValueFrom = null;
		ageValueTo = null;
		presetValue = null;
		presetRendered = true;
		customRendered = false;
	}

	

}
