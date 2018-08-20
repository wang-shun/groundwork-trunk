package org.groundwork.rs.restwebservices;

import com.wutka.jox.JOXBeanInputStream;
import com.wutka.jox.JOXBeanOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.profile.VemaMonitoring;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// POJO, no interface no extends

//Sets the path to base URL + /pluginUpdates
@Path("/vemaProfile")
public class VEMAProfile {

	private Log log = LogFactory.getLog(this.getClass());

	// This method is called if XMLis request
	@POST
	@Path("/checkUpdates")
	@Produces("application/xml")
	public String checkUpdates(@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("vmtype") String vmType,
			@FormParam("client-monitoring-profile") String clientProfile) {
		String profilesPath = "/usr/local/groundwork/core/vema/profiles/";
		String response = null;
		if (username == null || password == null) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (!LoginHelper.login(username, password)) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (vmType == null) {
			response = ResponseHelper.buildStatus("2", "INVALID VM TYPE");
			return response;
		} // end if
		VemaMonitoring vemaBean = this.xml2Bean(profilesPath + vmType + "_monitoring_profile.xml", true);

		// Generate the delta XML file
		response = this.generateResponse(vemaBean);

		return response;
	}

	/**
	 * Generates the response XML string
	 * 
	 * @param vemaBean
	 * @return
	 */
	private String generateResponse(VemaMonitoring vemaBean) {
		String response = null;
		JOXBeanOutputStream joxOut = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			joxOut = new JOXBeanOutputStream(baos, true);
			joxOut.writeObject("vema-monitoring", vemaBean);
			response = baos.toString();
		} catch (IOException ioe) {
			response = ResponseHelper.buildStatus("4",
					"INTERNAL ERROR-UNABLE TO GENERATE RESPONSE XML");
			log.error(ioe.getMessage());
		} finally {
			try {
				if (baos != null)
					baos.close();
				if (joxOut != null)
					joxOut.close();
			} catch (IOException ioe) {
				response = ResponseHelper.buildStatus("4",
						"INTERNAL ERROR-UNABLE TO GENERATE RESPONSE XML");
				log.error(ioe.getMessage());
			} // end try/catch
		}
		return response;
	}

	/**
	 * Parses the given xml file
	 */
	private VemaMonitoring xml2Bean(String xml, boolean isServer) {
		VemaMonitoring vemaBean = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(xml);
			JOXBeanInputStream joxIn = new JOXBeanInputStream(in);
			vemaBean = (VemaMonitoring) joxIn.readObject(VemaMonitoring.class);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				} // end try/catch
			} // end if
		}
		return vemaBean;
	}
}
