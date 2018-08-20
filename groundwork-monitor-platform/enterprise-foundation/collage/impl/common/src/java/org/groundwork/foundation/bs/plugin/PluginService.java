package org.groundwork.foundation.bs.plugin;

import java.util.Collection;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;

import com.groundwork.collage.model.Plugin;
import com.groundwork.collage.model.PluginPlatform;

/** @author Hibernate CodeGenerator */
public interface PluginService extends BusinessService {
	
	/**
	 * 
	 * @param name
	 * @param url
	 * @param platform
	 * @param parent
	 * @return
	 * @throws BusinessServiceException
	 */
	Plugin createPlugin(String name, String url,PluginPlatform platform,  String dependencies, String checksum, String lastUpdateBy) throws BusinessServiceException;
	
	/**
	 * 
	 * @param plugin
	 * @return
	 * @throws BusinessServiceException
	 */
	void savePlugin(Plugin plugin) throws BusinessServiceException;
	
	/**
	 * 
	 * @param pluginID
	 * @return
	 * @throws BusinessServiceException
	 */
	void deletePlugin(int pluginID) throws BusinessServiceException;
	
	/**
	 * 
	 * @param platform
	 * @param lasUpdateDate
	 * @return
	 * @throws BusinessServiceException
	 */
	Collection<Plugin> getPluginsByPlatform(String  platform, int arch, String lastUpdateDate) throws BusinessServiceException;

	/**
	 * 
	 * @return
	 * @throws BusinessServiceException
	 */
	Collection<Plugin> getAllPlugins() throws BusinessServiceException;

	
	/**
	 * 
	 * @param platform
	 * @param lasUpdateDate
	 * @return
	 * @throws BusinessServiceException
	 */
	Collection<PluginPlatform> getPlatforms() ;
 
}
