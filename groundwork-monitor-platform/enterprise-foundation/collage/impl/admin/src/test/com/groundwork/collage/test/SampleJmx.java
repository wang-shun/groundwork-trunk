/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.StateType;

/**
 * This is a utility class for storing static strings and convenience methods
 * for testing the SAMPLE_JMX ApplicationType; this class assumes that sample
 * data for the SAMPLE_JMX application has been setup in a certain way in the
 * database. 
 * This class is comparable in its useage to {@link com.groundwork.collage.util.Nagios}, 
 * but we placed it in the test package because it will never be used in a
 * production environment, unlike the Nagios utility class.
 * 
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 7205 $ - $Date: 2007-07-05 13:15:48 -0700 (Thu, 05 Jul 2007) $
 *
 */
public class SampleJmx
{
	public final static String APPLICATION_TYPE = "SAMPLE_JMX";

	public final static String DATE1    = "JmxDate1";
	public final static String DATE2    = "JmxDate2";
	public final static String BOOLEAN1 = "JmxBoolean1";
	public final static String BOOLEAN2 = "JmxBoolean2";
	public final static String STRING1  = "JmxString1";
	public final static String STRING2  = "JmxString2";
	public final static String INTEGER1 = "JmxInteger1";
	public final static String INTEGER2 = "JmxInteger2";
	public final static String LONG1    = "JmxLong1";
	public final static String LONG2    = "JmxLong2";
	public final static String DOUBLE1  = "JmxDouble1";
	public final static String DOUBLE2  = "JmxDouble2";
	public final static String THIRTY_DAY_MOVING_AVG = "30DayMovingAvg";


	public static Map createHostStatusProps(
			MonitorStatus monStatus, Date lastCheckTime,
			Date date1, Date date2,
			Boolean bool1, Boolean bool2, 
			String string1, String string2,
			Integer int1, Long long1, Double double1, Double ma30d)
	{
		Map props = new HashMap();
		props.put(HostStatus.EP_MONITOR_STATUS_NAME,  monStatus);
		props.put(HostStatus.EP_LAST_CHECK_TIME, lastCheckTime);
		props.put(DATE1,    date1);
		props.put(DATE1,    date1);
		props.put(DATE1,    date1);
		props.put(DATE2,    date2);
		props.put(BOOLEAN1, bool1);
		props.put(BOOLEAN2, bool2);
		props.put(STRING1,  string1);
		props.put(STRING2,  string2);
		props.put(INTEGER1, int1);
		props.put(LONG1,    long1);
		props.put(DOUBLE1,  double1);
		props.put(THIRTY_DAY_MOVING_AVG, ma30d);

		return props;
	}


	public static Map createServiceStatusProps(
			MonitorStatus monStatus, Date lastCheckTime, Date nextCheckTime,
			Date lastStateChange, MonitorStatus lastHardState,
			StateType stateType, CheckType checkType,
			Date date2, Boolean bool2, String string2,
			Integer int1, Integer int2,
			Long long1, Long long2,
			Double double1, Double double2, Double ma30d)
	{
		Map props = new HashMap();
		props.put(ServiceStatus.EP_MONITOR_STATUS_NAME,   monStatus);
		props.put(ServiceStatus.EP_LAST_CHECK_TIME,   	lastCheckTime);
		props.put(ServiceStatus.EP_NEXT_CHECK_TIME,   	nextCheckTime);
		props.put(ServiceStatus.EP_LAST_STATE_CHANGE, 	lastStateChange);
		props.put(ServiceStatus.EP_LAST_HARD_STATE_NAME,  lastHardState);
		props.put(ServiceStatus.EP_STATE_TYPE_NAME,       stateType);
		props.put(ServiceStatus.EP_CHECK_TYPE_NAME,       checkType);
		props.put(DATE2,    date2);
		props.put(BOOLEAN2, bool2);
		props.put(STRING2,  string2);
		props.put(INTEGER1, int1);
		props.put(INTEGER2, int2);
		props.put(LONG1,    long1);
		props.put(LONG2,    long2);
		props.put(DOUBLE1,  double1);
		props.put(DOUBLE2,  double2);
		props.put(THIRTY_DAY_MOVING_AVG, ma30d);

		return props;
	}

} // end class
