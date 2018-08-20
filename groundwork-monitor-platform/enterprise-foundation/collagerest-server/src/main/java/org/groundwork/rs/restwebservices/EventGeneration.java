package org.groundwork.rs.restwebservices;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.LogMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("/eventGeneration")
public class EventGeneration {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * REST webservices to generate an event
	 * 
	 * @param username
	 * @param password
     * @param dataInJSONFormat
	 * @return
	 */
	@POST
	@Path("/generateBulkEvents")
	@Produces("application/xml")
	public String generateBulkEvents(@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("dataInJSONFormat") String dataInJSONFormat) {

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

		if (dataInJSONFormat == null) {
			response = ResponseHelper.buildStatus("2", "INVALID DATA POSTED");
			return response;
		} // end if

		try {
			JSONObject rootObj = (JSONObject) JSONSerializer
					.toJSON(dataInJSONFormat);

			JSONArray jsonMainArr = rootObj.getJSONArray("log-message");

			for (int i = 0; i < jsonMainArr.size(); i++) {
				JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
				String hostName = childJSONObject.getString("host");
				String serviceDescription = childJSONObject
						.getString("service-description");
				String type = childJSONObject.getString("type");
				String status = childJSONObject.getString("status");
				String severity = childJSONObject.getString("severity");
				String message = childJSONObject.getString("message");
				String deviceIdentification = null;
				Map<String, String> properties = new HashMap<String, String>();
				CollageFactory _collageFactory = CollageFactory.getInstance();
				CollageAdminInfrastructure admin = (CollageAdminInfrastructure) _collageFactory
						.getAPIObject(CollageFactory.ADMIN_SERVICE);
				if (type != null
						&& (type.equalsIgnoreCase("VEMA") || type
								.equalsIgnoreCase("NAGIOS"))) {
					HostIdentityService hostIdentityService = _collageFactory.getHostIdentityService();
					Collection<Host> hostList = hostIdentityService.getHostsByIdOrHostNamesLookup(hostName);
					if (hostList != null && !hostList.iterator().hasNext()) {
						response = ResponseHelper.buildStatus("7",
								"INVALID HOST! HOST NOT FOUND IN FOUNDATION");
					} else {
						Host host = null;
						if (hostList != null) {
							host = hostList.iterator().next();
						}
						if (host != null) {
							deviceIdentification = host.getDevice()
									.getIdentification();
							properties.put(LogMessage.EP_HOST_NAME,
									host.getHostName());
						} // end if
					} // end if
				} // end if
				String monitorServerName = "localhost"; // there is only one
				// monitor server
				Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String NOW = formatter.format(new Date());

				properties.put(LogMessage.EP_MONITOR_STATUS_NAME, status);

				properties.put(LogMessage.EP_SERVICE_STATUS_DESCRIPTION,
						serviceDescription.equals("") ? null
								: serviceDescription);
				properties.put(LogMessage.EP_REPORT_DATE, NOW);
				admin.updateLogMessage(monitorServerName, type,
						deviceIdentification, severity, message, properties);
				response = ResponseHelper.buildStatus("0", "SUCCESS");
			}
		} catch (Exception exc) {
			log.error(exc.getMessage());
			response = ResponseHelper.buildStatus("99", exc.getMessage());
		}
		return response;
	}

}
