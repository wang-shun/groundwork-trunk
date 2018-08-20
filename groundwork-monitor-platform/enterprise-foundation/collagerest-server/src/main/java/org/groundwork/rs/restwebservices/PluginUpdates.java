package org.groundwork.rs.restwebservices;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.plugin.PluginService;
import org.groundwork.rs.plugins.Dependency;
import org.groundwork.rs.plugins.PluginUpdate;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Plugin;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;
import com.wutka.jox.JOXBeanOutputStream;

// POJO, no interface no extends

//Sets the path to base URL + /pluginUpdates
@Path("/pluginUpdates")
public class PluginUpdates {

	@Context
	private HttpServletRequest httpRequest;

	private Log log = LogFactory.getLog(this.getClass());

	// This method is called if XMLis request
	@GET
	@Path("/findUpdates")
	@Produces("application/xml")
	public String findUpdates(@QueryParam("platform") String platform,
			@QueryParam("arch") int arch,
			@QueryParam("lastUpdateTimestamp") String lastUpdateTimestamp) {
		String gdmaDtdPath = "/usr/local/groundwork/config/"; 
		CollageFactory factory = CollageFactory.getInstance();
		PluginService pluginService = factory.getPluginService();
		Collection<Plugin> plugins = pluginService.getPluginsByPlatform(
				platform, arch, lastUpdateTimestamp);
		Collection<Plugin> allPlugins = pluginService.getAllPlugins();
		HashMap<String, String> pluginMap = new HashMap<String, String>();
		for (com.groundwork.collage.model.Plugin hbPlugin : allPlugins) {
			pluginMap.put(hbPlugin.getName(), hbPlugin.getUrl());
		}
		org.groundwork.rs.plugins.Plugin[] pluginArr = new org.groundwork.rs.plugins.Plugin[plugins
				.size()];
		String response = null;
		FileReader reader = null;
		JOXBeanOutputStream joxOut = null;
		try {
			PluginUpdate update = new PluginUpdate();
			update.setPlatform(platform);
			int i = 0;
			
			for (com.groundwork.collage.model.Plugin hbPlugin : plugins) {
				org.groundwork.rs.plugins.Plugin plugin = new org.groundwork.rs.plugins.Plugin();
				plugin.setName(hbPlugin.getName());
				plugin.setArch(String.valueOf(hbPlugin.getPluginPlatform()
						.getArch()));
				plugin.setUrl(hbPlugin.getExternalUrl(httpRequest));
				String lastUpdateTimestampStr = hbPlugin.getLastUpdateTimestamp().toString();
				plugin.setLastUpdateDate(lastUpdateTimestampStr);
				plugin.setLastUpdateTimestamp(String.valueOf(hbPlugin.getLastUpdateTimestamp().getTime()/1000));
				plugin.setChecksum(hbPlugin.getChecksum());
				plugin.setLastUpdatedBy(hbPlugin.getLastUpdatedBy());
				String dependencies = hbPlugin.getDependencies();
				
				if (dependencies != null
						&& !dependencies.equalsIgnoreCase("None")) {
					StringTokenizer stkn = new StringTokenizer(dependencies,
							",");
					Dependency[] dependencyArr = new Dependency[stkn
							.countTokens()];
					int j = 0;
					while (stkn.hasMoreTokens()) {
						String dependency = stkn.nextToken();
						Dependency dep = new Dependency();
						dep.setName(dependency);
						dep.setUrl(pluginMap.get(dep.getName()));
						dependencyArr[j] = dep;
						j++;
					}
					plugin.setDependency(dependencyArr);
				}
				pluginArr[i] = plugin;
				i++;
			}
			update.setPlugin(pluginArr);
			reader = new FileReader(gdmaDtdPath + "gdma_plugin_update.dtd");
			DTDParser dtdParser = new DTDParser(reader);

			DTD dtd = dtdParser.parse();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			joxOut = new JOXBeanOutputStream(dtd, baos);
			joxOut.writeObject(update.getClass().getSimpleName(), update);

			response = baos.toString();

		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		} finally {
			try {
				if (reader != null)
				reader.close();
				if (joxOut != null)
				joxOut.close();
			} catch (IOException ioe) {
				log.error(ioe.getMessage());
			}
		}

		return response;
	}
}
