package org.groundwork.rs.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoVersion;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement(name = "version")
@Path("/version")
public class VersionResource {
    public static final String RESOURCE_PREFIX = "/version/";
    static final String ORG_GROUNDWORK_RS_REST_PROPERTIES = "/org/groundwork/rs/rest.properties";
    static final String SNAPSHOT = "-SNAPSHOT";
    static final String REST_VERSION_PROPERTY = "rest.version";
    protected static Log log = LogFactory.getLog(VersionResource.class);

    private static DtoVersion currentVersion = null;
    private static Object semaphore = new Object();

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoVersion getVersion() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /version");
            }
            if (currentVersion == null) {
                synchronized (semaphore) {
                    currentVersion = new DtoVersion(lookupVersion());
                }
            }
            return currentVersion;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for version.").build());
        }
        finally {
        }
    }

    private String lookupVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream(ORG_GROUNDWORK_RS_REST_PROPERTIES));
            String version = properties.getProperty(REST_VERSION_PROPERTY);
            if (version == null)
                return "NOT FOUND";
            int pos = version.indexOf(SNAPSHOT);
            if (pos > -1) {
                version = version.substring(0, pos);
            }
            if (log.isInfoEnabled()) {
                log.info("Rest Version = " + version);
            }
            return version;
        }
        catch (Exception e) {
            log.error("failed to lookup version number ", e);
            return "UNKNOWN";
        }
    }
}
