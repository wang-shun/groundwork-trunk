package org.groundwork.rs.restwebservices.utils;

import com.wutka.jox.JOXBeanOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.profile.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseHelper {
	
	private static Log log = LogFactory.getLog(ResponseHelper.class);
	
	/**
	 * Helper to buildStatus
	 * 
	 * @return
	 */
	public static String buildStatus(String statusCode, String message) {
		JOXBeanOutputStream joxOut = null;
		String response = null;
		Status status = new Status();
		status.setCode(statusCode);
		status.setMessage(message);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		joxOut = new JOXBeanOutputStream(baos);
		try {
			joxOut.writeObject("Status", status);
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		} finally {
			try {
				if (baos != null)
					baos.close();
				if (joxOut != null)
					joxOut.close();
			} catch (IOException ioe) {
				log.error(ioe.getMessage());
			} // end try/catch
		} // end try/catch
		response = baos.toString();
		return response;
	}

}
