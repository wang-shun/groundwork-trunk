package org.groundwork.rs.restwebservices;

import com.groundwork.collage.CollageFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

@Path("/performanceData")
public class PerformanceData {

	private Log log = LogFactory.getLog(this.getClass());

	private Map<String, Properties> perfDataMap = null;

	// String constants
	private static final String DEFAULT_JNDI_FACTORY_CLASS = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String DEFAULT_JNDI_HOST = "localhost";
	private static final String DEFAULT_JNDI_PORT = "4447";
	private static final String DEFAULT_SERVER_CONTEXT = "jms/RemoteConnectionFactory";
	private static final String DEFAULT_QUEUE = "/queue/vema_perf_data";
	private static final String MESSAGE_DELIMITER = "\t";
	private static final String THRESHOLD_DELIMITER = ";";
	private JMSDestinationInfo jmsDestinationInfo = null;
    private JMSDestinationWriter writer = null;

	public PerformanceData() {
		String perfDataPropFilePath = "/usr/local/groundwork/config/perfdata.properties";
		perfDataMap = this.parseServicePerfDataConfig(perfDataPropFilePath);
		// Initialize API framework
		CollageFactory service = CollageFactory.getInstance();
		Properties configuration = service.getFoundationProperties();
        jmsDestinationInfo = new JMSDestinationInfoImpl(configuration.getProperty(
				"jndi.factory.initial", DEFAULT_JNDI_FACTORY_CLASS).trim(),
				configuration.getProperty("jndi.factory.host",
						DEFAULT_JNDI_HOST).trim(), configuration.getProperty(
						"jndi.factory.port", DEFAULT_JNDI_PORT).trim(),
				configuration.getProperty("jms.server.context.id",
						DEFAULT_SERVER_CONTEXT).trim(), configuration
						.getProperty("perfdata.vema.jms.queue.name", DEFAULT_QUEUE)
						.trim(),configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim(),configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim());
	}

	/**
	 * Posting performance data
	 * 
	 * @param username
	 * @param password
     * @param dataInJSONFormat
	 * @return
	 */
	@POST
	@Path("/post")
	@Produces("application/xml")
	public String postPerfData(@FormParam("username") String username,
			@FormParam("password") String password,
            @FormParam("appType") @DefaultValue("VEMA") String appType,
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

			JSONArray jsonMainArr = rootObj.getJSONArray("performance-data");
            if (writer == null) {
                try {
                    writer = new JMSDestinationWriterImpl();
                    writer.initialize(jmsDestinationInfo);
                }
                catch (Exception e) {
                    response = ResponseHelper.buildStatus("99", e.getMessage());
                    log.error("Failed to create JMS writer ", e);
                    if (writer != null) {
                        writer.unInitialize();
                        writer = null;
                    }
                }
            }
            if (writer != null) {
                for (int i = 0; i < jsonMainArr.size(); i++) {
                    JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
                    String serverName = childJSONObject.getString("server-name");
                    int serverTime = childJSONObject.getInt("server-time");
                    String serviceName = childJSONObject.getString("service-name");
                    String label = childJSONObject.getString("label");
                    String value = childJSONObject.getString("value");
                    String warning = childJSONObject.getString("warning");
                    String critical = childJSONObject.getString("critical");
                    StringBuilder message = new StringBuilder();
                    message.append(serverTime);
                    message.append(MESSAGE_DELIMITER);
                    message.append(serverName);
                    message.append(MESSAGE_DELIMITER);
                    message.append(serviceName);
                    message.append(MESSAGE_DELIMITER);
                    message.append(MESSAGE_DELIMITER);
                    message.append(label);
                    message.append("=");
                    message.append(value);
                    message.append(THRESHOLD_DELIMITER);
                    message.append(warning);
                    message.append(THRESHOLD_DELIMITER);
                    message.append(critical);
                    writer.writeMessageWithProperty(message.toString(), "appType", appType);
                }
                response = ResponseHelper.buildStatus("0", "SUCCESS");
                writer.commit();
            }
		} catch (Exception exc) {
			log.error(exc.getMessage());
			response = ResponseHelper.buildStatus("99", exc.getMessage());
            if (writer != null) {
                writer.unInitialize();
                writer = null;
            }
		}
		return response;
	}

	/**
	 * Parse the perfdata.properties and creates the Hashmap of perfdata source
	 * and its properties
	 * 
	 * @return Hashmap of perfdata source and its properties
	 */
	private Map<String, Properties> parseServicePerfDataConfig(
			String perfDataPropFilePath) {
		Map<String, Properties> perfData_source_map = new HashMap<String, Properties>();
		String service_perfdata_start_tag = "<service_perfdata_files>";
		String service_perfdata_end_tag = "</service_perfdata_files>";
		String perfdata_source_start_tag = "<perfdata_source";
		String perfdata_source_end_tag = "</perfdata_source>";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(perfDataPropFilePath));
			String sCurrentLine;
			boolean servicePerfStart = false;
			boolean perfSourceStart = false;
			String source = null;
			Properties prop = null;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.equalsIgnoreCase(service_perfdata_start_tag))
					servicePerfStart = true;
				if (servicePerfStart) {
					if (sCurrentLine != null
							&& !sCurrentLine.trim().startsWith("#")
							&& sCurrentLine.trim().startsWith(
									perfdata_source_start_tag)) {
						source = sCurrentLine
								.substring(
										sCurrentLine
												.indexOf(perfdata_source_start_tag) + 17,
										sCurrentLine.length() - 1);
						prop = new Properties();
						perfSourceStart = true;
					} // end if
					if (perfSourceStart) {
						if (sCurrentLine != null
								&& sCurrentLine.indexOf("=") != -1) {
							StringTokenizer stkn = new StringTokenizer(
									sCurrentLine, "=");
							if (prop != null)
								prop.put(stkn.nextToken().trim(), stkn
										.nextToken().trim()
										.replaceAll("\"", ""));
						} // end if
					} // end if
					if (sCurrentLine.trim().equalsIgnoreCase(
							perfdata_source_end_tag)) {
						perfSourceStart = false;
						perfData_source_map.put(source.trim(), prop);
					} // end if
				} // end if
				if (sCurrentLine != null
						&& sCurrentLine.trim().equalsIgnoreCase(
								service_perfdata_end_tag))
					servicePerfStart = false;
			} // end while

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} // end try/catch
			} // end if
		} // end finally
		return perfData_source_map;
	}

}