/**
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
package org.itgroundwork.foundation.pagebeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;
import org.groundwork.foundation.dao.FoundationQueryList;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.ConsolidationCriteria;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;

/**
 * @author rogerrut Bean to facilitate calls from the WEB UI to configure the
 *         Foundation Data Model
 * 
 */
public class AdminBean {
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(AdminBean.class);

	private static final String PROPERTY_PREFIX = "prop_";

	private CollageFactory service = null;
	private CollageAdminInfrastructure admin = null;
	private CollageAdminMetadata adminMeta = null;

	/* DAO's */
	private MetadataService metadataService = null;

	/* Internal structures */
	private String[] appTypes = null;
	private String[] entityTypes = null;
	private String[] foundationProperties = null;
	private String[] hostGroups = null;
	private String[] hosts = null;
	private String[] selectedHosts = null;

	public AdminBean() {
		service = CollageFactory.getInstance();
		metadataService = service.getMetadataService();
	}

	/*
	 * Query methods for the UI
	 */

	public int getAppTypeCount() {
		this.loadAppTypes();
		return appTypes.length;
	}

	public String getApplicationTypesByIndex(int index) {
		this.loadAppTypes();
		if (appTypes.length >= index)
			return appTypes[index];
		else
			return "";
	}

	public int getEntityTypeCount() {
		this.loadEntityTypes();
		return entityTypes.length;
	}

	public String getEntityTypesByIndex(int index) {
		this.loadEntityTypes();
		if (entityTypes.length >= index)
			return entityTypes[index];
		else
			return "";
	}

	public int getHostGroupCount() {
		this.loadHostgroups();
		return hostGroups.length;
	}

	public String getHostGroupByIndex(int index) {
		this.loadHostgroups();
		if (hostGroups.length >= index)
			return hostGroups[index];
		else
			return "";
	}

	public int getHostCount() {
		this.loadHosts();
		return hosts.length;
	}

	public String getHostByIndex(int index) {
		this.loadHosts();
		if (hosts.length >= index)
			return hosts[index];
		else
			return "";
	}

	public int getselectedHostCount() {
		if (selectedHosts == null)
			return 0;
		else
			return selectedHosts.length;
	}

	public String getSelectedHostByIndex(int index) {
		if (selectedHosts == null) {
			return "";
		} else {
			if (hosts.length >= index)
				return selectedHosts[index];
			else
				return "";
		}
	}

	public int getPropertiesCount() {
		this.loadProperties();
		return this.foundationProperties.length;
	}

	public String getPropertyByIndex(int index) {
		this.loadProperties();
		if (foundationProperties.length >= index)
			return foundationProperties[index];
		else
			return "";
	}

	/*
	 * Admin methods for the UI
	 */

	public void addHostGroup(String applicationType, String hsotGroupName) {
		String appTypeName = applicationType;

		/* Input validation */
		if (applicationType == null || applicationType.length() < 1) {
			// Assign default value
			appTypeName = "NAGIOS";
		}

		loadAdminBeans();

		if (this.admin != null) {
			this.admin
					.addHostsToHostGroup(applicationType, hsotGroupName, null);
		}

		hostGroups = null;
		this.loadHostgroups();
	}

	public void addHostToHostgroup(String applicationType,
			String hostGroupName, String hostName) {

		String appTypeName = applicationType;

		/* Input validation */
		if (applicationType == null || applicationType.length() < 1) {
			// Assign default value
			appTypeName = "NAGIOS";
		}

		loadAdminBeans();

		if (this.admin != null) {
			List<String> hostNames = new ArrayList<String>(1);
			hostNames.add(hostName);

			this.admin.addHostsToHostGroup(appTypeName, hostGroupName,
					hostNames);
		}

	}

	public void removeHostFromHostGroup(String hostGroupName, String host) {
		loadAdminBeans();

		if (this.admin != null) {
			List<String> hostNames = new ArrayList<String>(1);
			hostNames.add(host);

			this.admin.removeHostsFromHostGroup(hostGroupName, hostNames);
		}
	}

	public void updateHostSelection(String hg) {
		HostService hostService = service.getHostService();
		FoundationQueryList hosts = hostService.getHostsByHostGroupName(hg,
				null, null, -1, -1);
		if (hosts != null) {
			selectedHosts = null;

			Iterator itData = hosts.iterator();
			Host host = null;

			selectedHosts = new String[hosts.size()];
			int count = 0;
			while (itData.hasNext()) {
				host = (Host) itData.next();
				selectedHosts[count++] = new String(host.getHostName());
			}
		}
	}

	public void assignProperty(String applicationType, String EntityType,
			String propertyName) {
		loadAdminBeans();

		if (this.adminMeta != null) {
			// Assign it to Application Type and EntityType
			adminMeta.assignPropertyType(applicationType, EntityType,
					propertyName);
		}
	}

	public void addProperty(String propertyName, String propertyPrimitive) {
		loadAdminBeans();

		String propertyPrimitiveType = propertyPrimitive;

		if (propertyPrimitive == null || propertyPrimitive.length() < 1) {
			// Set default
			propertyPrimitiveType = "STRING";

		}

		if (this.adminMeta != null) {
			// Add property
			adminMeta.createOrUpdatePropertyType(propertyName, propertyName,
					propertyPrimitiveType);

			// Reload
			foundationProperties = null;
			this.loadProperties();
		}
	}

	public void addApplicationType(String applicationType) {
		// Insert new applicationType
		loadAdminBeans();

		if (this.adminMeta != null) {
			this.adminMeta.createApplicationType(applicationType,
					applicationType);
			// Reload Application Types
			appTypes = null;
			this.loadAppTypes();
		}
	}

	public File[] getPropertyFiles(String directory) {
		File root = new File(directory);

		File[] files = root.listFiles(new PropertiesFileFilter());

		if (files != null && files.length > 0) {
			Arrays.sort(files, new FileComparator());
		}

		return files;
	}

	public SortedMap getConfigurationProperties(String filename)
			throws IOException {
		if (filename == null || filename.length() == 0)
			return null;

		Properties props = new Properties();
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(filename);

			props.load(fis);
		} finally {
			if (fis != null)
				fis.close();
		}

		// Sort Properties
		SortedMap sortedProps = new TreeMap();
		sortedProps.putAll(props);

		return sortedProps;
	}

	public void saveConfigurationProperties(String filename,
			ServletRequest request) throws IOException {
		if (filename == null || filename.length() == 0)
			throw new RuntimeException(
					"Invalid null / empty file name.  Unable to save configuration properties");

		// First Backup existing configuration and delete original
		Calendar now = new GregorianCalendar();
		String tmpFileName = String.format(
				"%1$s_%2$d_%3$d_%4$d_%5$d_%6$d_%7$d.bak", filename, now
						.get(Calendar.MONTH) + 1, now.get(Calendar.DATE), now
						.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY), now
						.get(Calendar.MINUTE), now.get(Calendar.SECOND));

		copyFile(filename, tmpFileName, true);

		Properties props = new Properties();

		String key = null;
		String value = null;

		Map paramMap = request.getParameterMap();

		Set keySet = paramMap.keySet();
		if (keySet == null)
			return;

		Iterator it = keySet.iterator();
		while (it.hasNext()) {
			key = (String) it.next();
			if (key == null || key.startsWith(PROPERTY_PREFIX) == false)
				continue;

			// get value from request form
			value = request.getParameter(key);
			if (value == null)
				continue;

			// Update value
			props.put(key.substring(PROPERTY_PREFIX.length()), value.trim());
		}

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(filename, false);
			props.store(fos, null);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	public void addConfigurationProperty(String filename, String key,
			String value) throws IOException {
		if (filename == null || filename.length() == 0)
			throw new RuntimeException(
					"Invalid null / empty file name.  Unable to add configuration property");

		if (key == null || key.length() == 0)
			throw new RuntimeException(
					"Invalid null / empty property name.  Unable to add configuration property");

		if (value == null || value.length() == 0)
			throw new RuntimeException(
					"Invalid null / empty value.  Unable to add configuration property");

		// First Backup existing configuration and delete original
		Calendar now = new GregorianCalendar();
		String tmpFileName = String.format(
				"%1$s_%2$d_%3$d_%4$d_%5$d_%6$d_%7$d.bak", filename, now
						.get(Calendar.MONTH) + 1, now.get(Calendar.DATE), now
						.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY), now
						.get(Calendar.MINUTE), now.get(Calendar.SECOND));

		copyFile(filename, tmpFileName, false);

		Properties props = new Properties();
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(filename);

			props.load(fis);
		} finally {
			if (fis != null)
				fis.close();
		}

		props.setProperty(key, value.trim());

		// Save new file
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename, false);
			props.store(fos, null);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	private static void copyFile(String fromFileName, String toFileName,
			boolean bDeleteFromFile) throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists() && (toFile.canWrite() == false)) {
			throw new IOException("FileCopy: "
					+ "destination file is unwriteable: " + toFileName);
		}

		String parent = toFile.getParent();
		if (parent == null)
			parent = System.getProperty("user.dir");

		File dir = new File(parent);
		if (!dir.exists())
			throw new IOException("FileCopy: "
					+ "destination directory doesn't exist: " + parent);

		if (dir.isFile())
			throw new IOException("FileCopy: "
					+ "destination is not a directory: " + parent);

		if (!dir.canWrite())
			throw new IOException("FileCopy: "
					+ "destination directory is unwriteable: " + parent);

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
					;
				}
		}

		if (bDeleteFromFile)
			fromFile.delete();
	}

	/*
	 * Private methods to get Application and Entity data from the System
	 */
	private void loadAdminBeans() {
		// Load on demand
		if (this.admin == null) {
			admin = (CollageAdminInfrastructure) service
					.getAPIObject("com.groundwork.collage.CollageAdmin");
		}

		if (this.adminMeta == null) {
			adminMeta = (CollageAdminMetadata) service
					.getAPIObject("com.groundwork.collage.CollageAdminMetadata");
		}
	}

	private boolean loadAppTypes() {
		if (appTypes == null) {
			// load App Types
			FoundationQueryList data = this.metadataService
					.getApplicationTypes(null, null, -1, -1);
			Iterator itData = data.iterator();
			ApplicationType appType = null;

			appTypes = new String[data.size()];
			int count = 0;
			while (itData.hasNext()) {
				appType = (ApplicationType) itData.next();
				appTypes[count++] = new String(appType.getName());
			}
		}

		return true;
	}

	private boolean loadEntityTypes() {
		if (entityTypes == null) {
			// load Entity Types
			FoundationQueryList data = this.metadataService.getEntityTypes(
					null, null, -1, -1);
			Iterator itData = data.iterator();
			EntityType entityType = null;

			entityTypes = new String[data.size()];
			int count = 0;
			while (itData.hasNext()) {
				entityType = (EntityType) itData.next();
				entityTypes[count++] = new String(entityType.getName());
			}
		}

		return true;
	}

	private boolean loadProperties() {
		if (foundationProperties == null) {
			loadAdminBeans();

			// load properties Types
			foundationProperties = this.adminMeta.getPropertyTypeNames();
		}

		return true;
	}

	private boolean loadHostgroups() {
		if (hostGroups == null) {
			loadAdminBeans();

			HostGroupService hgService = service.getHostGroupService();
			FoundationQueryList list = hgService.getHostGroups(null, null, -1,
					-1);

			Iterator itData = list.iterator();
			HostGroup hg = null;

			hostGroups = new String[list.size()];
			int count = 0;
			while (itData.hasNext()) {
				hg = (HostGroup) itData.next();
				hostGroups[count++] = new String(hg.getName());
			}
		}

		return true;
	}

	private boolean loadHosts() {
		if (hosts == null) {

			loadAdminBeans();

			HostService hostService = service.getHostService();
			FoundationQueryList list = hostService.getHosts(null, null, -1, -1);

			Iterator itData = list.iterator();
			Host host = null;

			hosts = new String[list.size()];
			int count = 0;
			while (itData.hasNext()) {
				host = (Host) itData.next();
				hosts[count++] = new String(host.getHostName());
			}
		}

		return true;
	}

	protected class PropertiesFileFilter implements FileFilter {
		private String PROPERTY_FILE_EXT = "properties";
		private String DOT = ".";

		public PropertiesFileFilter() {
		}

		public boolean accept(File file) {
			// All Directories are excepted
			if (file.isDirectory() == true) {
				return false;
			}

			String fileName = file.getName();

			int pos = fileName.lastIndexOf(DOT);

			// No extension and we are not allowing hidden files (.*)
			if (pos < 1 || pos == (fileName.length() - 1)) {
				return false;
			}

			String ext = fileName.substring(pos + 1);

			// Search for ext in our list
			return ext.equals(PROPERTY_FILE_EXT);
		}
	}

	protected static class FileComparator implements Comparator<File> {
		private Collator c = Collator.getInstance();

		public int compare(File file1, File file2) {
			if (file1 == file2)
				return 0;

			if (file1.isDirectory() && file2.isFile())
				return -1;

			if (file1.isFile() && file2.isDirectory())
				return 1;

			return c.compare(file1.getName(), file2.getName());
		}
	}

	public String getPerformanceData() {
		PerformanceDataService performanceService = (PerformanceDataService) service
				.getPerformanceDataService();
		return performanceService.getPerformanceDataLabel();
	}

	public void updatePerformanceData(HttpServletRequest req) {
		PerformanceDataService performanceService = (PerformanceDataService) service
				.getPerformanceDataService();
		String[] rows = req.getParameterValues("performanceDataLabelID");
		if (rows != null && rows.length > 0)
			for (int i = 0; i < rows.length; i++) {
				int k = Integer.parseInt(rows[i]);
				performanceService.updatePerformanceDataLabelEntry(k, req
						.getParameter("serviceDisplayName" + "." + k), req
						.getParameter("metricLabel" + "." + k), req
						.getParameter("unit" + "." + k));

			}

	}

	public String getConsolidationCriterias() {
		ConsolidationService consolidationService = (ConsolidationService) service
				.getConsolidationService();
		return consolidationService.getConsolidationCriterias();
	}

	public void updateConsolidationCriteria(HttpServletRequest req) {
		ConsolidationService consolidationService = (ConsolidationService) service
				.getConsolidationService();
		String[] rows = req.getParameterValues("consolidationCriteriaID");
		if (rows != null && rows.length > 0)
			for (int i = 0; i < rows.length; i++) {
				int k = Integer.parseInt(rows[i]);
				consolidationService.updateConsolidationCriteriaEntry(k, req
						.getParameter("name" + "." + k), req
						.getParameter("criteria" + "." + k));

			}

	}

	/**
	 * Adds the consolidationcriteria
	 * 
	 * @param req
	 */
	public void addConsolidationCriteria(HttpServletRequest req) {
		log.debug("addConsolidationCriteria.....");
		try {
			ConsolidationService consolidationService = (ConsolidationService) service
					.getConsolidationService();
			String name = req.getParameter("new-name");
			String criteria = req.getParameter("new-criteria");
			if (name == null || name.length()==0 || criteria== null || criteria.length()==0)
			{
				req.setAttribute("error","Cannot create ConsolidationCriteria.Invalid data..!" );
				log.error("Cannot create ConsolidationCriteria.Invalid data..!");
			} 
			else
			{
				log.debug("addConsolidationCriteria....." + name + "," + criteria);
				ConsolidationCriteria conCriteria = consolidationService.createConsolidationCriteria(name, criteria);
				consolidationService.saveConsolidationCriteria(conCriteria);
			}
		} catch (Exception e) {
			req.setAttribute("error","Cannot create ConsolidationCriteria.Invalid data..!" );
			log.error("Cannot create ConsolidationCriteria.Invalid data..!");
		}
	}

	/**
	 * Restores to the default criterias
	 * 
	 * @param req
	 */
	public void restoreDefaultConsolidationCriteria(HttpServletRequest req) {
		log.debug("Restoring defaults.....");
		try {
			ConsolidationService consolidationService = (ConsolidationService) service
					.getConsolidationService();
			String[] sqlFiles = {
					"/usr/local/groundwork/foundation/database/nagios-properties.sql",
					"/usr/local/groundwork/foundation/database/system-properties.sql" };
			ArrayList<String> criteriaSQLs = new ArrayList<String>();

			for (int i = 0; i < sqlFiles.length; i++) {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader(sqlFiles[i]));
					String str = null;
					while ((str = in.readLine()) != null) {
						if (str.startsWith("INSERT INTO ConsolidationCriteria")
								|| str
										.startsWith("REPLACE INTO ConsolidationCriteria")) {
							criteriaSQLs.add(str);
						} // end if
					} // end if
				} catch (IOException e) {
					log.error(e.getMessage());
				} finally {
					try {
						if (in != null)
							in.close();
					} catch (IOException e) {
						log.error(e.getMessage());
					}
				}
			} // end if
			consolidationService.deleteAll();
			// Now process the SQLs
			for (String criteriaSQL : criteriaSQLs) {
				int openBracesIndex = criteriaSQL.lastIndexOf("(");
				int closeBracesIndex = criteriaSQL.lastIndexOf(")");
				String nameCriteria = criteriaSQL.substring(
						openBracesIndex + 1, closeBracesIndex);
				String name = nameCriteria.split(",")[0].replaceAll("'", "")
						.replaceAll("\"", "");
				String criteria = nameCriteria.split(",")[1]
						.replaceAll("'", "").replaceAll("\"", "");
				log.debug(name + "-----------" + criteria);
				ConsolidationCriteria conCriteria = consolidationService
						.createConsolidationCriteria(name, criteria);
				consolidationService.saveConsolidationCriteria(conCriteria);
			} // end for
		} catch (Exception e) {
			req.setAttribute("error","Cannot restore default ConsolidationCriteria.Invalid data..!" );
			log
					.error("Cannot restore default ConsolidationCriteria.Invalid data..!");
		}
	}

}
