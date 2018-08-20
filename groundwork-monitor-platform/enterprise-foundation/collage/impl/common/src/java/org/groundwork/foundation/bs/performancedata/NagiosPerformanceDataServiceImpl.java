/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.bs.performancedata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.exception.BusinessServiceException;

public class NagiosPerformanceDataServiceImpl implements
		NagiosPerformanceDataService {

	protected static Log log = LogFactory
			.getLog(NagiosPerformanceDataServiceImpl.class);
	private String lastCheckTime = null;
	private String serviceLatencyMin = null;
	private String serviceLatencyMax = null;
	private String serviceLatencyAvg = null;

	private String serviceExecTimeMin = null;
	private String serviceExecTimeMax = null;
	private String serviceExecTimeAvg = null;

	private String hostExecTimeMin = null;
	private String hostExecTimeMax = null;
	private String hostExecTimeAvg = null;

	private String activeServiceCheckMin1 = null;
	private String activeServiceCheckMin5 = null;
	private String activeServiceCheckMin15 = null;

	private String passiveServiceCheckMin1 = null;
	private String passiveServiceCheckMin5 = null;
	private String passiveServiceCheckMin15 = null;

	private final String US_DATETIME_PATTERN = "MM/dd/yyyy h:mm:ss a";
	private final String ACTIVE_SERVICE_EXEC_TIME = "Active Service Execution Time";
	private final String ACTIVE_SERVICE_LATENCY = "Active Service Latency";
	private final String ACTIVE_HOST_EXEC_TIME = "Active Host Execution Time";
	private final String ACTIVE_SERVICES_CHECKS_LAST_1_5_15 = "Active Service Checks Last 1/5/15 min";
	private final String PASSIVE_SERVICES_CHECKS_LAST_1_5_15 = "Passive Service Checks Last 1/5/15 min";
	private final String NAGIOS_STATS_CMD = "/usr/local/groundwork/nagios/bin/nagiostats";

	/**
	 * Fetches the nagios performance data
	 */
	public String fetchPerformanceData() throws BusinessServiceException {
		String xml = null;
		DateFormat sdf = new SimpleDateFormat(US_DATETIME_PATTERN);
		sdf.setLenient(true);
		lastCheckTime = sdf.format(Calendar.getInstance().getTime());
		Calendar.getInstance().getTime().toString();
		String[] command = { NAGIOS_STATS_CMD };

		String[] fields = { ACTIVE_SERVICE_EXEC_TIME, ACTIVE_SERVICE_LATENCY,
				ACTIVE_HOST_EXEC_TIME, ACTIVE_SERVICES_CHECKS_LAST_1_5_15,
				PASSIVE_SERVICES_CHECKS_LAST_1_5_15 };

		InputStream reader = null;
		BufferedReader br = null;
		Process process = null;
		StringBuffer sb = null;
		try {
			/**
			 * GWPORTAL-79 Review fix. Make sure Streams are closed and process
			 * is released Not closing the reader and buffer resulted in a : no
			 * more file handlers error!
			 */
			process = Runtime.getRuntime().exec(command);
			reader = process.getInputStream();
			br = new BufferedReader(new InputStreamReader(reader));
			sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				for (int i = 0; i < fields.length; i++) {
					if (line.startsWith(fields[i]))
						sb.append(line).append("\n");
				} // end if
			}
		} catch (Exception exc) {
			log.error(exc.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				log.error(ioe.getMessage());
			}
			try {
				if (process != null)
					process.exitValue();
			} catch (IllegalThreadStateException is) {
				if (process != null)
					process.destroy();
			}
		}

		if (sb != null) {
			String output = sb.toString();
			log.debug(output);
			StringTokenizer stkn = new StringTokenizer(output, "\n");
			while (stkn.hasMoreTokens()) {
				String outLine = stkn.nextToken();
				if (outLine.startsWith(ACTIVE_SERVICE_LATENCY)) {
					StringTokenizer innerStkn = new StringTokenizer(
							outLine.substring(outLine.indexOf(":") + 1), "/");
					while (innerStkn.hasMoreTokens()) {
						serviceLatencyMin = innerStkn.nextToken().trim();
						serviceLatencyMax = innerStkn.nextToken().trim();
						String avg = innerStkn.nextToken().trim();
						serviceLatencyAvg = avg.substring(0, avg.indexOf(" "));
					} // end while
				} else if (outLine.startsWith(ACTIVE_SERVICE_EXEC_TIME)) {
					StringTokenizer innerStkn = new StringTokenizer(
							outLine.substring(outLine.indexOf(":") + 1), "/");
					while (innerStkn.hasMoreTokens()) {
						serviceExecTimeMin = innerStkn.nextToken().trim();
						serviceExecTimeMax = innerStkn.nextToken().trim();
						String avg = innerStkn.nextToken().trim();
						serviceExecTimeAvg = avg.substring(0, avg.indexOf(" "));
					} // end while
				} else if (outLine
						.startsWith(ACTIVE_SERVICES_CHECKS_LAST_1_5_15)) {
					StringTokenizer innerStkn = new StringTokenizer(
							outLine.substring(outLine.indexOf(":") + 1), "/");
					while (innerStkn.hasMoreTokens()) {
						activeServiceCheckMin1 = innerStkn.nextToken().trim();
						activeServiceCheckMin5 = innerStkn.nextToken().trim();
						activeServiceCheckMin15 = innerStkn.nextToken().trim();

					} // end while
				} else if (outLine
						.startsWith(PASSIVE_SERVICES_CHECKS_LAST_1_5_15)) {
					StringTokenizer innerStkn = new StringTokenizer(
							outLine.substring(outLine.indexOf(":") + 1), "/");
					while (innerStkn.hasMoreTokens()) {
						passiveServiceCheckMin1 = innerStkn.nextToken().trim();
						passiveServiceCheckMin5 = innerStkn.nextToken().trim();
						passiveServiceCheckMin15 = innerStkn.nextToken().trim();

					} // end while
				} else if (outLine.startsWith(ACTIVE_HOST_EXEC_TIME)) {
					StringTokenizer innerStkn = new StringTokenizer(
							outLine.substring(outLine.indexOf(":") + 1), "/");
					while (innerStkn.hasMoreTokens()) {
						hostExecTimeMin = innerStkn.nextToken().trim();
						hostExecTimeMax = innerStkn.nextToken().trim();
						String avg = innerStkn.nextToken().trim();
						hostExecTimeAvg = avg.substring(0, avg.indexOf(" "));
					} // end while
				}// end if
			} // end while

			xml = this.generateXML();
		} // end if

		return xml;
	}

	/**
	 * Generates the XML
	 * 
	 * @return
	 */
	private String generateXML() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<NagiosPerformanceInfo>" + "<Statistics>"
				+ "<LastCheckTime>"
				+ lastCheckTime
				+ "</LastCheckTime>"
				+ "<ServiceCheckExecutionTime>"
				+ "<Min>"
				+ serviceExecTimeMin
				+ "</Min>"
				+ "<Max>"
				+ serviceExecTimeMax
				+ "</Max>"
				+ "<Average>"
				+ serviceExecTimeAvg
				+ "</Average>"
				+ "</ServiceCheckExecutionTime>"
				+ "<ServiceCheckLatency>"
				+ "<Min>"
				+ serviceLatencyMin
				+ "</Min>"
				+ "<Max>"
				+ serviceLatencyMax
				+ "</Max>"
				+ "<Average>"
				+ serviceLatencyAvg
				+ "</Average>"
				+ "</ServiceCheckLatency>"
				+ "<HostCheckExecutionTime>"
				+ "<Min>"
				+ hostExecTimeMin
				+ "</Min>"
				+ "<Max>"
				+ hostExecTimeMax
				+ "</Max>"
				+ "<Average>"
				+ hostExecTimeAvg
				+ "</Average>"
				+ "</HostCheckExecutionTime>"
				+ "</Statistics>"
				+ "<ServiceCheck>"
				+ "<Active>"
				+ "<Min1>"
				+ activeServiceCheckMin1
				+ "</Min1>"
				+ "<Min5>"
				+ activeServiceCheckMin5
				+ "</Min5>"
				+ "<Min15>"
				+ activeServiceCheckMin15
				+ "</Min15>"
				+ "</Active>"
				+ "<Passive>"
				+ "<Min1>"
				+ passiveServiceCheckMin1
				+ "</Min1>"
				+ "<Min5>"
				+ passiveServiceCheckMin5
				+ "</Min5>"
				+ "<Min15>"
				+ passiveServiceCheckMin15
				+ "</Min15>"
				+ "</Passive>"
				+ "</ServiceCheck>" + "</NagiosPerformanceInfo>";

		return xml;
	}

}
