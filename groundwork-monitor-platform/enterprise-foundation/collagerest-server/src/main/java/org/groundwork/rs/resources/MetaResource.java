package org.groundwork.rs.resources;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/meta")
public class MetaResource {

    @GET
    @Path("/wadl")
    @Produces("application/xml")
    public Response downloadWADL(@Context ServletContext context) {

        String fileName = context.getRealPath("/application.wadl");
        File file = new File(fileName);
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition",
                "attachment; filename=application.wadl");
        return response.build();
    }

    @GET
    @Path("/xsd")
    @Produces("application/xml")
    public Response downloadXSD(@Context ServletContext context) {

        String fileName = context.getRealPath("/foundation-rest.xsd");
        File file = new File(fileName);
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition",
                "attachment; filename=foundation-rest.xsd");
        return response.build();
    }

}
