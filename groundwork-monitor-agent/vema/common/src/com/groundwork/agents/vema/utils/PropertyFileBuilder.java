/*
 * 
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundwork.agents.vema.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * This class is the utility to generate Propery file for appserver
 * 
 * @author Arul Shanmugam
 * 
 */
public class PropertyFileBuilder {

	private static org.apache.log4j.Logger log = Logger
			.getLogger(PropertyFileBuilder.class);

	private String path = null;
	private String appServerName = null;
	private Properties staticProps = null;

	// private ResourceBundle resBundle;

	public PropertyFileBuilder(String path, String appServerName,
			String propFile, Properties staticProps) {
		if (path != null && appServerName != null && staticProps != null
				&& propFile != null) {
			this.path = path;
			this.appServerName = appServerName;
			// resBundle = StatUtils.getResourceBundle(propFile);
			this.staticProps = staticProps;
		} else {
			log
					.error("Invalid path or appservername or static properties or propFile");
		}

	}

	/**
	 * Builds the property file
	 * 
	 * @param serviceNames
	 */
	public void build(List<String> serviceNames, List<String> aliases,
			List<String> warnings, List<String> criticals) {
		for (int i = 0; i < serviceNames.size(); i++) {
			String serviceName = serviceNames.get(i);
			String alias = aliases.get(i);
			String warning = warnings.get(i);
			String critical = criticals.get(i);
			staticProps
					.put(serviceName, alias + ";" + warning + ";" + critical);
		} // end while

		// First backup the file
		String fileName = path + "gwos_" + appServerName;
		String extension = ".xml";
		File file = new File(fileName + extension);
		if (file.exists()) {
			file.renameTo(new File(fileName + extension + "_old" + ".backup"));
		} // end if

		Properties tmp = new Properties() {

			@Override
			public Set<Object> keySet() {
				return Collections.unmodifiableSet(new TreeSet<Object>(super
						.keySet()));
			}

		};
		tmp.putAll(staticProps);

		FileOutputStream fos = null;
		try
        {
			fos = new FileOutputStream(path + "gwos_" + appServerName + ".xml");
			tmp.storeToXML( fos,
							"JDMA Configuration.AppServer admin connector and Nagios Settings",
							"UTF-8");
		}
        catch (IOException ioe)
        {
			log.error(ioe.getMessage());
		}
        finally
        {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
					log.error(ioe.getMessage());
				} // end if
			} // end if
		} // end finally
	}
}
