package org.groundwork.foundation.bs.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;

import com.groundwork.collage.model.Plugin;
import com.groundwork.collage.model.PluginPlatform;

/** @author Hibernate CodeGenerator */
public class PluginServiceImpl extends EntityBusinessServiceImpl implements
		PluginService {

	private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

	/** Business Services used within Plugin */
	private MetadataService metadataService = null;

	public PluginServiceImpl(FoundationDAO foundationDAO, MetadataService mds) {
		super(foundationDAO, Plugin.INTERFACE_NAME, Plugin.COMPONENT_NAME);
		metadataService = mds;

	}

	/**
	 * 
	 * @param name
	 * @param url
	 * @param platform
	 * @param parent
	 * @return
	 * @throws BusinessServiceException
	 */
	public Plugin createPlugin(String name, String url,
			PluginPlatform platform, String dependencies, String checksum,
			String lastUpdatedBy) throws BusinessServiceException {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException(
					"Plugin name must not be empty or null.");
		}
		Plugin plugin = getPluginByName(name, platform);
		if (plugin == null) plugin = (Plugin) this.create();
		plugin.setName(name);
		if (url != null && url.length() > 0)
			plugin.setUrl(url);
		plugin.setPluginPlatform(platform);
		plugin.setDependencies(dependencies);
		plugin.setLastUpdateTimestamp(new Date());
		plugin.setChecksum(checksum);
		plugin.setLastUpdatedBy(lastUpdatedBy);
		this.save(plugin);
		return plugin;
	}

	/**
	 * 
	 * @param plugin
	 * @return
	 * @throws BusinessServiceException
	 */
	public void savePlugin(Plugin plugin) throws BusinessServiceException {
		if (plugin == null)
			throw new IllegalArgumentException("Category is null.");

		this.save(plugin);
	}

	/**
	 * 
	 * @param pluginID
	 * @return
	 * @throws BusinessServiceException
	 */
	public void deletePlugin(int pluginID) throws BusinessServiceException {
		if (pluginID < 1) {
			throw new IllegalArgumentException("Invalid pluginID.");
		}
		this.delete(pluginID);
	}

	/**
	 * 
	 * @param platform
	 * @param lasUpdateDate
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<Plugin> getPluginsByPlatform(String platform, int arch,
			String lastUpdateTimestamp) throws BusinessServiceException {
		Collection<Plugin> multiplatformPluginList = new ArrayList<Plugin>();
		if (platform == null || arch < 32 || lastUpdateTimestamp == null) {
			throw new BusinessServiceException(
					"Invalid platform or architecture or lastUpdateDate");
		} // end if
		/*
		 * Match the "arch" parameter in the request, and form a list of all the
		 * multiplatform plugins for that arch. When you get the list of
		 * platform-specific plugins for that arch, if any of them have the same
		 * name as one of those multiplatform plugins, drop the multiplatform
		 * copy from the list. Return the list of what is left over: all plugins
		 * for that platform+arch, plus all multiplatform+arch that were not
		 * overridden by a platform+arch plugin of the same name.
		 */

		FilterCriteria multiplatformFilter = FilterCriteria.ilike(
				"pluginPlatform.name", "Multiplatform".toLowerCase());
		if (lastUpdateTimestamp != null && lastUpdateTimestamp.length() > 0) {
			Date query_timestamp = new Date(new Long(lastUpdateTimestamp)
					.longValue() * 1000);
			FilterCriteria lastUpdateTimestampCriteria = FilterCriteria.ge(
					"lastUpdateTimestamp", query_timestamp);
			multiplatformFilter.and(lastUpdateTimestampCriteria);
		} // end if
		multiplatformFilter.and(FilterCriteria.eq("pluginPlatform.arch", arch));
		multiplatformPluginList = this.query(multiplatformFilter, null);

		Collection<Plugin> platSpecificPluginList = new ArrayList<Plugin>();
		FilterCriteria platSpecificFilter = FilterCriteria.ilike(
				"pluginPlatform.name", platform.toLowerCase());
		if (lastUpdateTimestamp != null && lastUpdateTimestamp.length() > 0) {
			Date query_timestamp = new Date(new Long(lastUpdateTimestamp)
					.longValue() * 1000);
			FilterCriteria lastUpdateTimestampCriteria = FilterCriteria.ge(
					"lastUpdateTimestamp", query_timestamp);
			platSpecificFilter.and(lastUpdateTimestampCriteria);
		} // end if
		platSpecificFilter.and(FilterCriteria.eq("pluginPlatform.arch", arch));
		platSpecificPluginList = this.query(platSpecificFilter, null);
		Collection<Plugin> tempPluginList = new ArrayList<Plugin>();
		tempPluginList.addAll(multiplatformPluginList);
		for (Plugin platSpecificPlugin : platSpecificPluginList) {
			for (Plugin multiplatformPlugin : multiplatformPluginList) {
				if (platSpecificPlugin.getName().equalsIgnoreCase(
						multiplatformPlugin.getName()))
					tempPluginList.remove(multiplatformPlugin);
			} // end for
		} // end for
		platSpecificPluginList.addAll(tempPluginList);
		return platSpecificPluginList;
	}

	/**
	 * 
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<PluginPlatform> getPlatforms() {

		String sql = "select PlatformID,Name,Arch,Description from PluginPlatform";
		List l = _foundationDAO.sqlQuery(sql);
		Iterator itPlat = l.iterator();

		Collection<PluginPlatform> plaformList = new ArrayList<PluginPlatform>();

		while (itPlat.hasNext()) {
			Object[] vals = (Object[]) itPlat.next();
			int platformId = ((Integer) vals[0]).intValue();
			String name = (String) vals[1];
			int arch = ((Integer) vals[2]).intValue();
			String desc = (String) vals[3];
			PluginPlatform platform = new com.groundwork.collage.model.impl.PluginPlatform(
					platformId, name, arch, desc);
			plaformList.add(platform);
		}
		return plaformList;
	}

	/**
	 * 
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<Plugin> getAllPlugins() throws BusinessServiceException {
		Collection<Plugin> pluginList = new ArrayList<Plugin>();
		pluginList = this.query(null, null);
		return pluginList;
	}

	private Plugin getPluginByName(String name, PluginPlatform platform) throws BusinessServiceException {
		FilterCriteria criteria = FilterCriteria.eq("name", name);
		criteria.and(FilterCriteria.eq("pluginPlatform", platform));
		List results = query(criteria, null);
		return ((results != null) && !results.isEmpty()) ? (Plugin) results.get(0) : null;
	}

}
