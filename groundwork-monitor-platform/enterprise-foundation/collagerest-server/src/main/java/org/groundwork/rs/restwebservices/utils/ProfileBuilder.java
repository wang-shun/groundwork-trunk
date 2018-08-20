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
 * This class is the utility to generate Profile files for monarch
 * 
 * @author Arul Shanmugam
 * 
 */
public class ProfileBuilder {

	private Log log = LogFactory.getLog(this.getClass());

	private String path = null;
	private String appServerName = null;

	public ProfileBuilder(String path, String appServerName) {
		this.path = path;
		this.appServerName = appServerName;

	}

	public void build(Set<String> serviceNames) {
		StringBuffer finalConfig = new StringBuffer();
		finalConfig.append("<?xml version=\"1.0\" ?>\n");
		finalConfig.append("<profile>\n");
		finalConfig.append("<service_profile>\n");
		finalConfig.append("<prop name=\"name\"><![CDATA[");
		finalConfig.append(appServerName);
		finalConfig.append("]]></prop>\n");
		finalConfig
				.append("<prop name=\"description\"><![CDATA[service-profile-");
		finalConfig.append(appServerName);
		finalConfig.append("]]></prop>\n");

		Iterator<String> iterator = serviceNames.iterator();
		while (iterator.hasNext()) {
			String serviceName = (String) iterator.next();
			finalConfig.append(headerBuilder(serviceName));
		} // end if
		finalConfig.append(bodyBuilder());

		Iterator<String> iterator_2 = serviceNames.iterator();
		while (iterator_2.hasNext()) {
			String serviceName = (String) iterator_2.next();
			finalConfig.append(footerBuilder(serviceName));
		} // end if
		finalConfig.append("</service_profile>\n");
		finalConfig.append("</profile>");
		this.write(finalConfig.toString());

	}

	private String headerBuilder(String serviceName) {
		StringBuffer header = new StringBuffer();
		header.append("<prop name=\"service\"><![CDATA[");
		header.append(serviceName);
		header.append("]]></prop>\n");
		return header.toString();
	}

	private String bodyBuilder() {
		StringBuffer body = new StringBuffer();
		body.append("<command>\n");
		body.append("<prop name=\"name\"><![CDATA[check_alive]]></prop>\n");
		body.append("<prop name=\"type\"><![CDATA[check]]></prop>\n");
		body
				.append("<prop name=\"command_line\"><![CDATA[$USER1$/check_icmp -H $HOSTADDRESS$ -w 3000.0,80% -c 5000.0,100% -n 1]]></prop>\n");
		body.append("</command>\n");
		body.append("<service_template>\n");
		body
				.append("<prop name=\"name\"><![CDATA[generic-service-volatile]]></prop>\n");
		body
				.append("<prop name=\"template\"><![CDATA[generic-service]]></prop>\n");
		body.append("<prop name=\"is_volatile\"><![CDATA[1]]></prop>\n");
		body.append("</service_template>\n");
		body.append("<time_period>\n");
		body
				.append("<prop name=\"comment\"><![CDATA[All day, every day.]]></prop>\n");
		body.append("<prop name=\"name\"><![CDATA[24x7]]></prop>\n");
		body
				.append("<prop name=\"alias\"><![CDATA[24 Hours A Day, 7 Days A Week]]></prop>\n");
		body.append("<prop name=\"tuesday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("<prop name=\"friday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("<prop name=\"sunday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("<prop name=\"saturday\"><![CDATA[00:00-24:00]]></prop>\n");
		body
				.append("<prop name=\"wednesday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("<prop name=\"monday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("<prop name=\"thursday\"><![CDATA[00:00-24:00]]></prop>\n");
		body.append("</time_period>\n");
		body.append("<service_template>\n");
		body
				.append("<prop name=\"retry_check_interval\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"flap_detection_enabled\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"event_handler_enabled\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"notifications_enabled\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"active_checks_enabled\"><![CDATA[1]]></prop>\n");
		body.append("<prop name=\"process_perf_data\"><![CDATA[1]]></prop>\n");
		body.append("<prop name=\"check_period\"><![CDATA[24x7]]></prop>\n");
		body
				.append("<prop name=\"passive_checks_enabled\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"notification_period\"><![CDATA[24x7]]></prop>\n");
		body.append("<prop name=\"max_check_attempts\"><![CDATA[3]]></prop>\n");
		body
				.append("<prop name=\"retain_status_information\"><![CDATA[1]]></prop>\n");
		body.append("<prop name=\"parallelize_check\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"notification_options\"><![CDATA[u,c,w,r]]></prop>\n");
		body
				.append("<prop name=\"retain_nonstatus_information\"><![CDATA[1]]></prop>\n");
		body.append("<prop name=\"name\"><![CDATA[generic-service]]></prop>\n");
		body
				.append("<prop name=\"comment\"><![CDATA[# Generic service definition template - This is NOT a real service, just a template!]]></prop>\n");
		body
				.append("<prop name=\"normal_check_interval\"><![CDATA[10]]></prop>\n");
		body
				.append("<prop name=\"obsess_over_service\"><![CDATA[1]]></prop>\n");
		body
				.append("<prop name=\"notification_interval\"><![CDATA[60]]></prop>\n");
		body.append("</service_template>\n");
		return body.toString();
	}

	private String footerBuilder(String serviceName) {
		StringBuffer builder = new StringBuffer();
		builder.append("<service_name>\n");
		builder
				.append("<prop name=\"active_checks_enabled\"><![CDATA[0]]></prop>\n");
		builder
				.append("<prop name=\"max_check_attempts\"><![CDATA[1]]></prop>\n");
		builder
				.append("<prop name=\"parallelize_check\"><![CDATA[0]]></prop>\n");
		builder
				.append("<prop name=\"template\"><![CDATA[generic-service-volatile]]></prop>\n");
		builder.append("<prop name=\"name\"><![CDATA[");
		builder.append(serviceName);
		builder.append("]]></prop>\n");
		builder
				.append("<prop name=\"check_command\"><![CDATA[check_alive]]></prop>\n");
		builder.append("</service_name>\n");
		return builder.toString();
	}

	/**
	 * Helper to create file
	 * 
	 * @param finalConfig
	 */
	private void write(String finalConfig) {
		BufferedWriter out = null;
		try {
			String fileName = path + "service-profile-" + appServerName;
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
