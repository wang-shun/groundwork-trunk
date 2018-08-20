package org.groundwork.connectors.solarwinds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Enumeration;

@Path("/tests")
public class SolarWindsTestResource extends AbstractBridgeResource {

    protected static Log log = LogFactory.getLog(SolarWindsTestResource.class);

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/form")
    public String postHostsForm() {
        Enumeration<String> formParams = request.getParameterNames();
        System.out.println("-------------------------------------------");
        System.out.println("Form Parameters:");
        while (formParams.hasMoreElements()) {
            String name = formParams.nextElement();
            String value = request.getParameter(name);
            System.out.println(String.format(" %s : %s", name, value));
        }
        return "OK";
    }

    @POST
    @Path("/text")
    @Consumes(MediaType.TEXT_PLAIN)
    public String postHosts(String body) {

        System.out.println("body = " + body);
        return "OK";
    }
}