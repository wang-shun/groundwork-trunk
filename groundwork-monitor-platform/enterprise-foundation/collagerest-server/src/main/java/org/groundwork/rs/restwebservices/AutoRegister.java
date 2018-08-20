package org.groundwork.rs.restwebservices;

import com.google.common.collect.ImmutableMap;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;
import org.groundwork.rs.restwebservices.utils.ScriptRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Path("/autoRegister")
public class AutoRegister {

	@Context private HttpServletRequest httpRequest;
	@Context private HttpServletResponse httpResponse;

	private Log log = LogFactory.getLog(this.getClass());

	private static final String REGISTER_AGENT_BY_DISCOVERY_COMMAND_FILE = "/usr/local/groundwork/foundation/scripts/registerAgentByDiscovery.pl";
	private static final String REGISTER_AGENT_BY_DISCOVERY_COMMAND = REGISTER_AGENT_BY_DISCOVERY_COMMAND_FILE + " -f -";

	private final int REGISTER_AGENT_BY_DISCOVERY_TIMEOUT;

	AutoRegister() {
        CollageFactory service = CollageFactory.getInstance();
        Properties configuration = service.getFoundationProperties();
        REGISTER_AGENT_BY_DISCOVERY_TIMEOUT = NumberUtils.toInt(configuration.getProperty("autoregister.discovery.timeout"), 30);
	}

	// This method is called if XMLis request
	@POST
	@Path("/registerAgent")
	@Produces("application/xml")
	public String registerAgent(@FormParam("username") String username,
								@FormParam("password") String password,
								@FormParam("agent-type") String agentType,
								@FormParam("host-name") String hostName,
								@FormParam("host-ip") String hostIP,
								@FormParam("host-mac") String hostMac,
								@FormParam("operating-system") String operatingSystem,
								@FormParam("host-characteristic") String hostCharacteristic) {

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

		if (agentType == null) {
			response = ResponseHelper.buildStatus("2", "INVALID AGENTTYPE");
			return response;
		} // end if

		if (hostName == null) {
			response = ResponseHelper.buildStatus("3", "INVALID HOSTNAME");
			return response;
		} // end if

		if (hostIP == null) {
			response = ResponseHelper.buildStatus("4", "INVALID HOSTIP");
			return response;
		} // end if

		if (hostMac == null) {
			response = ResponseHelper.buildStatus("5", "INVALID HOSTMAC");
			return response;
		} // end if

		if (operatingSystem == null) {
			response = ResponseHelper.buildStatus("6",
					"INVALID OPERATING SYSTEM");
			return response;
		} // end if

		if (hostCharacteristic == null) {
			response = ResponseHelper.buildStatus("7",
					"INVALID HOST CHARACTERISTIC");
			return response;
		} // end if

		CollageFactory _collageFactory = CollageFactory.getInstance();
		ActionService service = _collageFactory.getActionService();
		FoundationQueryList list = service.getActionByApplicationType("GDMA",
				false);
		List<Action> results = list.getResults();
		int actionID = -1;
		for (Action action : results) {
			if (action.getName().equalsIgnoreCase("Register Agent")) {
				actionID = action.getActionId();
				break;
			} // end if
		} // end for
		List<ActionPerform> listActionPerforms = new ArrayList<ActionPerform>();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("agent-type", agentType);
		parameters.put("host-name", hostName);
		parameters.put("host-ip", hostIP);
		parameters.put("host-mac", hostMac);
		parameters.put("operating-system", operatingSystem);
		parameters.put("host-characteristic", hostCharacteristic);
		listActionPerforms.add(new ActionPerform(actionID, parameters));
		try {

			List<ActionReturn> returns = service
					.performActions(listActionPerforms);
			ActionReturn actReturn = returns.get(0);
			response = ResponseHelper.buildStatus(actReturn.getReturnCode(),
					actReturn.getReturnValue());
		} catch (CollageException e) {
			response = ResponseHelper.buildStatus("99",e.getMessage());
			log.error(e.getMessage());
		}
		return response;
	}


	// This method is called if XMLis request
	@POST
	@Path("/registerAgentByProfile")
	@Produces("application/xml")
	public String registerAgentByProfile(@FormParam("username") String username,
										 @FormParam("password") String password,
										 @FormParam("agent-type") String agentType,
										 @FormParam("host-name") String hostName,
										 @FormParam("host-ip") String hostIP,
										 @FormParam("host-mac") String hostMac,
										 @FormParam("operating-system") String operatingSystem,
										 @FormParam("host-profile-name") String hostProfileName,
										 @FormParam("service-profile-name") String serviceProfileName) {

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

		if (agentType == null) {
			response = ResponseHelper.buildStatus("2", "INVALID AGENTTYPE");
			return response;
		} // end if

		if (hostName == null) {
			response = ResponseHelper.buildStatus("3", "INVALID HOSTNAME");
			return response;
		} // end if

		if (hostIP == null) {
			response = ResponseHelper.buildStatus("4", "INVALID HOSTIP");
			return response;
		} // end if

		if (hostMac == null) {
			response = ResponseHelper.buildStatus("5", "INVALID HOSTMAC");
			return response;
		} // end if

		if (operatingSystem == null) {
			response = ResponseHelper.buildStatus("6",
					"INVALID OPERATING SYSTEM");
			return response;
		} // end if

		if (hostProfileName == null) {
			response = ResponseHelper.buildStatus("7",
					"INVALID HOST PROFILE NAME");
			return response;
		} // end if

		if (serviceProfileName == null) {
			response = ResponseHelper.buildStatus("8",
					"INVALID SERVICE PROFILE NAME");
			return response;
		} // end if

		CollageFactory _collageFactory = CollageFactory.getInstance();
		ActionService service = _collageFactory.getActionService();
		FoundationQueryList list = service.getActionByApplicationType("GDMA",
				false);
		List<Action> results = list.getResults();
		int actionID = -1;
		for (Action action : results) {
			if (action.getName().equalsIgnoreCase("Register Agent by Profile")) {
				actionID = action.getActionId();
				break;
			} // end if
		} // end for
		List<ActionPerform> listActionPerforms = new ArrayList<ActionPerform>();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("agent-type", agentType);
		parameters.put("host-name", hostName);
		parameters.put("host-ip", hostIP);
		parameters.put("host-mac", hostMac);
		parameters.put("operating-system", operatingSystem);
		parameters.put("host-profile-name", hostProfileName);
		parameters.put("service-profile-name", serviceProfileName);
		listActionPerforms.add(new ActionPerform(actionID, parameters));
		try {

			List<ActionReturn> returns = service
					.performActions(listActionPerforms);
			ActionReturn actReturn = returns.get(0);
			response = ResponseHelper.buildStatus(actReturn.getReturnCode(),
					actReturn.getReturnValue());
		} catch (CollageException e) {
			response = ResponseHelper.buildStatus("99",e.getMessage());
			log.error(e.getMessage());
		}
		return response;
	}

	// To help simplify the interface between registerAgentByDiscovery, a protocol was invented to convert exit codes
	// to http status codes.
	private static int exitCodeToStatus(int exitCode) {
		return EXIT_CODE_TO_STATUS.containsKey(exitCode) ? EXIT_CODE_TO_STATUS.get(exitCode) : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
	private static final Map<Integer, Integer> EXIT_CODE_TO_STATUS = ImmutableMap.<Integer, Integer> builder()
			.put(22, HttpServletResponse.SC_ACCEPTED)
			.put(42, HttpServletResponse.SC_NO_CONTENT)
			.put(43, HttpServletResponse.SC_NOT_MODIFIED)
			.put(4, HttpServletResponse.SC_BAD_REQUEST)
			.put(14, HttpServletResponse.SC_UNAUTHORIZED)
			.put(34, HttpServletResponse.SC_FORBIDDEN)
			.put(44, HttpServletResponse.SC_NOT_FOUND)
			.put(84, HttpServletResponse.SC_REQUEST_TIMEOUT)
			.put(94, HttpServletResponse.SC_CONFLICT)
			.put(104, HttpServletResponse.SC_GONE)
			.put(134, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE)
			.put(234, 423) // Unfortunately there is no SC_LOCKED enum
			.put(5, HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
			.put(15, HttpServletResponse.SC_NOT_IMPLEMENTED)
			.put(35, HttpServletResponse.SC_SERVICE_UNAVAILABLE)
			.build();

	@POST
	@Path("/registerAgentByDiscovery")
	public String registerAgentByDiscovery(String input) {

		if (!LoginHelper.login(httpRequest.getHeader(HttpHeaders.AUTHORIZATION))) {
		    String errorMessage = "Invalid credentials";
			if (log.isDebugEnabled()) log.debug(errorMessage);
			throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity(errorMessage).build());
		}

		try (ScriptRunner scriptRunner = new ScriptRunner(REGISTER_AGENT_BY_DISCOVERY_COMMAND, input, REGISTER_AGENT_BY_DISCOVERY_TIMEOUT, TimeUnit.SECONDS)) {
			scriptRunner.run();
			String output = scriptRunner.getOutput() + scriptRunner.getError();
			int exitValue = scriptRunner.getExitValue();
			if (exitValue != 0) {
				log.error("Error (" + exitValue + ") returned from command " + REGISTER_AGENT_BY_DISCOVERY_COMMAND + ": " + output);
				httpResponse.setStatus(exitCodeToStatus(exitValue));
				// Clear the response buffer manually to ensure that the default html-formatted response is discarded
				httpResponse.resetBuffer();
				httpResponse.flushBuffer();
			}
			return output;
		} catch (IOException | RuntimeException e) {
			log.error("Error running " + REGISTER_AGENT_BY_DISCOVERY_COMMAND + ": " + e.getMessage());
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("registerAgentByDiscovery had an exception.  Please consult log files at " + new Date()).build());
		}
	}

}
