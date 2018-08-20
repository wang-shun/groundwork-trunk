/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.groundwork.rs.restwebservices.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is the utility to generate PerfConfig files for monarch
 * 
 * @author Arul Shanmugam
 * 
 */
public class PerfConfigBuilder {

	private Log log = LogFactory.getLog(this.getClass());

	private String path = null;
	private String appServerName = null;

	private StringBuffer part_1 = null;
	private StringBuffer part_2 = null;
	private StringBuffer part_3 = null;
	private StringBuffer part_4 = null;
	private StringBuffer part_5 = null;

	public PerfConfigBuilder(String path, String appServerName) {
		this.path = path;
		this.appServerName = appServerName;
		init();
	}

	private void init() {
		part_1 = new StringBuffer();
		// part_1.append("<groundwork_performance_configuration>\n");
		part_1.append("<service_profile name=\"");
		part_2 = new StringBuffer();
		part_2.append("\">\n");
		part_2.append("<graph name=\"graph\">\n");
		part_2.append("<host>*</host>\n");
		part_2.append("<service regx=\"0\"><![CDATA[");
		part_3 = new StringBuffer();
		part_3.append("]]></service>\n");
		part_3.append("<type>nagios</type>\n");
		part_3.append("<enable>1</enable>\n");
		part_3.append("<label>");
		part_4 = new StringBuffer();
		part_4.append("</label>\n");
		part_4
				.append("<rrdname><![CDATA[/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd]]></rrdname>\n");
		part_4
				.append("<rrdcreatestring><![CDATA[$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:");
		part_5 = new StringBuffer();
		part_5
				.append(":GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480]]></rrdcreatestring>\n");
		part_5
				.append("<rrdupdatestring><![CDATA[$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1]]></rrdupdatestring>\n");
		part_5.append("<graphcgi><![CDATA['']]></graphcgi>\n");
		part_5.append("<parseregx first=\"0\"><![CDATA[]]></parseregx>\n");
		part_5.append("<perfidstring></perfidstring>\n");
		part_5.append("</graph>\n");
		part_5.append("</service_profile>\n");
		// part_5.append("</groundwork_performance_configuration>\n");
	}

	public void build(Set<String> serviceNames) {

		Iterator<String> iterator = serviceNames.iterator();
		StringBuffer finalConfig = new StringBuffer();
		finalConfig.append("<groundwork_performance_configuration>\n");
		while (iterator.hasNext()) {

			String serviceName = (String) iterator.next();
			String label = serviceName
					.substring(serviceName.lastIndexOf(".") + 1);
			finalConfig.append(part_1.toString());
			finalConfig.append(serviceName);
			finalConfig.append(part_2.toString());
			finalConfig.append(serviceName);
			finalConfig.append(part_3.toString());
			// Label can be upto 19 chars
			if (label.length() >= 19)
				finalConfig.append(label.substring(0, 17));
			else
				finalConfig.append(label);
			finalConfig.append(part_4.toString());
			finalConfig.append(label);
			finalConfig.append(part_5.toString());
			// System.out.println(finalConfig);
			// System.out.println("************************************");

		} // end while
		finalConfig.append("</groundwork_performance_configuration>\n");
		this.write(finalConfig.toString());
	}

	/**
	 * Helper to write file
	 * 
	 * @param finalConfig
	 */
	private void write(String finalConfig) {
		BufferedWriter out = null;
		try {
			String fileName = path + "perfconfig-" + appServerName;
			String extension = ".xml";
			File file = new File(fileName + extension);
			int i = 1;
			while (file.exists()) {
				file = new File(fileName + "_" + i + extension);
				i = i + 1;
			} // end if

			out = new BufferedWriter(new FileWriter(file));
			out.write(finalConfig.toString());
			
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			} // end try/catch
		}
	}

}
