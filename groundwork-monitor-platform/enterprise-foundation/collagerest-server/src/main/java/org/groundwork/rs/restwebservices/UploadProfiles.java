package org.groundwork.rs.restwebservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.PerfConfigBuilder;
import org.groundwork.rs.restwebservices.utils.ProfileBuilder;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.StringTokenizer;
import java.util.TreeSet;

// POJO, no interface no extends

//Sets the path to base URL + /pluginUpdates
@Path("/uploadProfiles")
public class UploadProfiles {

	private Log log = LogFactory.getLog(this.getClass());

	// This method is called if XMLis request
	@POST
	@Path("/upload")
	@Produces("application/xml")
	public String upload(@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("appServerName") String appServerName,
			@FormParam("mbeanAtts") String mbeanAtts) {
		String profilesPath = "/usr/local/groundwork/core/profiles/";
		String response = null;
		if (username == null || password == null) {
			response = ResponseHelper.buildStatus("1", "INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (!LoginHelper.login(username, password)) {
			response = ResponseHelper.buildStatus("1", "INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (appServerName == null) {
			response = ResponseHelper.buildStatus("2", "INVALID APPSERVER NAME");
			return response;
		} // end if

		if (mbeanAtts == null) {
			response = ResponseHelper.buildStatus("3", "INVALID MBEAN ATTRIBUTES");
			return response;
		} // end if

		if (mbeanAtts != null && mbeanAtts.length() > 0) {
			StringTokenizer stkn = new StringTokenizer(mbeanAtts, ",");
			TreeSet<String> mbeanSet = new TreeSet<String>();
			while (stkn.hasMoreTokens()) {
				String mbeanAtt = stkn.nextToken();
				mbeanSet.add(mbeanAtt);
			} // end while
			this.createConfigFiles(profilesPath, mbeanSet, appServerName);
			response = ResponseHelper.buildStatus("0", "Profiles exported Successfully");
		} // end if

		return response;
	}

	/**
	 * Helper to create config files in the server
	 * 
	 * @param path
	 * @param appServerName
	 */
	private void createConfigFiles(String path, TreeSet<String> mbeanSet,
			String appServerName) {
		PerfConfigBuilder perfBuilder = new PerfConfigBuilder(path,
				appServerName);
		perfBuilder.build(mbeanSet);
		ProfileBuilder profBuilder = new ProfileBuilder(path, appServerName);
		profBuilder.build(mbeanSet);
	}

	

}
