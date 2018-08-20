package com.gwos.statusservice.utils;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gwos.statusservice.beans.ResponseStatus;

public class ResponseHelper {

	private static Log log = LogFactory.getLog(ResponseHelper.class);

	/**
	 * Helper to buildStatus
	 * 
	 * @return
	 */
	public static String buildStatus(String statusCode, String message) {
		String response = null;
		ResponseStatus status = new ResponseStatus();
		status.setCode(statusCode);
		status.setMessage(message);
		try {
			StringWriter responseWriter = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(ResponseStatus.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(status, responseWriter);
			response = responseWriter.toString();
		} catch (JAXBException ioe) {
			log.error(ioe.getMessage());
		}
		return response;
	}

}
