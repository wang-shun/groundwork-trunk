/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.collector.CollectorConfigService;
import org.groundwork.rs.dto.DtoFileList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CollectorConfigResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/collectors")
public class CollectorConfigResource extends AbstractResource {

    private static Log log = LogFactory.getLog(CollectorConfigResource.class);

    private static final String AGENT_TYPE = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY;
    private static final String PREFIX = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY;
    private static final String HOST_NAME = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_HOST_NAME_KEY;
    private static final String IPV4 = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_IPV4_KEY;
    private static final String IPV6 = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_IPV6_KEY;
    private static final String MAC = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_MAC_KEY;
    private static final String CHARACTERISTIC = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_CHARACTERISTIC_KEY;

    /**
     * Get collector configuration file names. File names include the
     * ".yaml" extension and are returned in case insensitive sorted order.
     *
     * @return DTO file list instance
     */
    @GET
    @Path("/configurations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoFileList getCollectorConfigs() {
        if (log.isDebugEnabled()) {
            log.debug("processing GET on /configurations");
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            List<String> fileNames = collectorConfigService.getCollectorConfigFileNames();
            if ((fileNames == null) || fileNames.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity("Collector configurations not found").build());
            }
            return new DtoFileList(fileNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("An error occurred processing get request for collector configurations").build());
        }
    }

    /**
     * Get YAML collector configuration from file. File name must include
     * the ".yaml" extension. File is returned using the UTF-8 character set.
     *
     * @param fileName collector configuration file name
     * @return UTF-8 YAML collector configuration
     */
    @GET
    @Path("/configurations/{fileName}")
    @Produces("text/yaml")
    public Response getCollectorConfig(@PathParam("fileName") String fileName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing GET on /configurations/%s", fileName));
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            String content = collectorConfigService.getCollectorConfig(fileName);
            if ((content == null) || content.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity(String.format("Collector configuration [%s] not found", fileName)).build());
            }
            return Response.ok().header("Content-Type", "text/yaml; charset=UTF-8").
                    entity(content.getBytes("UTF-8")).build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration [%s] error: %s", fileName, iae.getMessage())).build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing get request for collector configuration [%s]",
                            fileName)).build());
        }
    }

    /**
     * Create collector configuration file from collector configuration
     * template selected by agent type and prefix identity properties.
     * File name must include the ".yaml" extension. All supplied identity
     * properties are merged into the template on create. Created YAML
     * collector configuration is returned using the UTF-8 character set.
     *
     * @param fileName collector configuration file name
     * @param agentType agent type identity property, (selects template, required)
     * @param prefix prefix identity property, (selects template, required)
     * @param hostName host name identity property, (required)
     * @param ipv4 ipv4 identity property
     * @param ipv6 ipv6 identity property
     * @param mac mac identity property
     * @param characteristic characteristic identity property
     * @return UTF-8 YAML collector configuration
     */
    @POST
    @Path("/configurations/{fileName}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/yaml")
    public Response createCollectorConfig(@PathParam("fileName") String fileName,
                                          @FormParam(AGENT_TYPE) String agentType,
                                          @FormParam(PREFIX) String prefix,
                                          @FormParam(HOST_NAME) String hostName,
                                          @FormParam(IPV4) String ipv4,
                                          @FormParam(IPV6) String ipv6,
                                          @FormParam(MAC) String mac,
                                          @FormParam(CHARACTERISTIC) String characteristic) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing POST on /configurations/%s", fileName));
        }
        if ((agentType == null) || agentType.isEmpty() || (prefix == null) || prefix.isEmpty() || (hostName == null) ||
                hostName.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration [%s] create missing properties", fileName)).build());
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            // construct identity properties
            Map<String,String> identityProperties = new HashMap<String,String>();
            identityProperties.put(AGENT_TYPE, agentType);
            identityProperties.put(PREFIX, prefix);
            identityProperties.put(HOST_NAME, hostName);
            if ((ipv4 != null) && !ipv4.isEmpty()) {
                identityProperties.put(IPV4, ipv4);
            }
            if ((ipv6 != null) && !ipv6.isEmpty()) {
                identityProperties.put(IPV6, ipv6);
            }
            if ((mac != null) && !mac.isEmpty()) {
                identityProperties.put(MAC, mac);
            }
            if ((characteristic != null) && !characteristic.isEmpty()) {
                identityProperties.put(CHARACTERISTIC, characteristic);
            }
            // find collector config template
            String templateFileName = collectorConfigService.findCollectorConfigTemplate(identityProperties);
            if (templateFileName == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity(String.format("Collector configuration [%s] create template not found", fileName)).build());
            }
            // create collector config
            String content = collectorConfigService.createCollectorConfig(templateFileName, identityProperties, fileName);
            if ((content == null) || content.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                        entity(String.format("Collector configuration [%s] create failed", fileName)).build());
            }
            return Response.ok().header("Content-Type", "text/yaml; charset=UTF-8").
                    entity(content.getBytes("UTF-8")).build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration [%s] error: %s", fileName, iae.getMessage())).build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing create request for collector configuration [%s]",
                            fileName)).build());
        }
    }

    /**
     * Put YAML collector configuration to file. File name must include
     * the ".yaml" extension. File must be specified using the UTF-8
     * character set.
     *
     * @param fileName collector configuration file name
     * @param contentType HTTP content type from header to validate
     * @param content UTF-8 YAML collector configuration
     */
    @PUT
    @Path("/configurations/{fileName}")
    @Consumes({"text/yaml", "application/x-yaml"})
    public Response putCollectorConfig(@PathParam("fileName") String fileName,
                                       @HeaderParam("Content-Type") String contentType, byte [] content) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing PUT on /configurations/%s", fileName));
        }
        String charset = (((contentType != null) && contentType.toLowerCase().contains("charset=")) ?
                contentType.substring(contentType.toLowerCase().indexOf("charset=")+8) : null);
        if ((charset != null) && !charset.equalsIgnoreCase("UTF-8")) {
            throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            if (!collectorConfigService.putCollectorConfig(fileName, new String(content, "UTF-8"))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                        entity(String.format("Collector configuration [%s] put failed", fileName)).build());
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration [%s] error: %s", fileName, iae.getMessage())).build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing put request for collector configuration [%s]",
                            fileName)).build());
        }
    }

    /**
     * Delete collector configuration file. File name must include
     * the ".yaml" extension.
     *
     * @param fileName collector configuration file name
     */
    @DELETE
    @Path("/configurations/{fileName}")
    public Response deleteCollectorConfig(@PathParam("fileName") String fileName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /configurations/%s", fileName));
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            if (!collectorConfigService.deleteCollectorConfig(fileName)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                        entity(String.format("Collector configuration [%s] delete failed", fileName)).build());
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration [%s] error: %s", fileName, iae.getMessage())).build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing delete request for collector configuration [%s]",
                            fileName)).build());
        }
    }

    /**
     * Get collector configuration template file names. File names include
     * the ".yaml" extension and are returned in case insensitive sorted order.
     *
     * @return DTO file list
     */
    @GET
    @Path("/templates")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoFileList getCollectorConfigTemplates() {
        if (log.isDebugEnabled()) {
            log.debug("processing GET on /templates");
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            List<String> fileNames = collectorConfigService.getCollectorConfigTemplateFileNames();
            if ((fileNames == null) || fileNames.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity("Collector configuration templates not found").build());
            }
            return new DtoFileList(fileNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("An error occurred processing get request for collector configuration templates").build());
        }
    }

    /**
     * Get YAML collector configuration template from file. File name must
     * include the ".yaml" extension. File is returned using the UTF-8
     * character set.
     *
     * @param fileName collector configuration template file name
     * @return UTF-8 YAML collector configuration template
     */
    @GET
    @Path("/templates/{fileName}")
    @Produces("text/yaml")
    public Response getCollectorConfigTemplate(@PathParam("fileName") String fileName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing GET on /templates/%s", fileName));
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            String content = collectorConfigService.getCollectorConfigTemplate(fileName);
            if ((content == null) || content.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity(String.format("Collector configuration template [%s] not found", fileName)).build());
            }
            return Response.ok().header("Content-Type", "text/yaml; charset=UTF-8").
                    entity(content.getBytes("UTF-8")).build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration template [%s] error: %s", fileName, iae.getMessage())).
                    build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing get request for collector configuration template [%s]",
                            fileName)).build());
        }
    }

    /**
     * Put YAML collector configuration template to file. File name must
     * include the ".yaml" extension. File must be specified using the
     * UTF-8 character set.
     *
     * @param fileName collector configuration template file name
     * @param contentType HTTP content type from header to validate
     * @param content UTF-8 YAML collector configuration template
     */
    @PUT
    @Path("/templates/{fileName}")
    @Consumes({"text/yaml", "application/x-yaml"})
    public Response putCollectorConfigTemplate(@PathParam("fileName") String fileName,
                                               @HeaderParam("Content-Type") String contentType, byte [] content) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing PUT on /templates/%s", fileName));
        }
        String charset = (((contentType != null) && contentType.toLowerCase().contains("charset=")) ?
                contentType.substring(contentType.toLowerCase().indexOf("charset=")+8) : null);
        if ((charset != null) && !charset.equalsIgnoreCase("UTF-8")) {
            throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            if (!collectorConfigService.putCollectorConfigTemplate(fileName, new String(content, "UTF-8"))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                        entity(String.format("Collector configuration template [%s] put failed", fileName)).build());
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration template [%s] error: %s", fileName, iae.getMessage())).
                    build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing put request for collector configuration template [%s]",
                            fileName)).build());
        }
    }

    /**
     * Delete collector configuration template file. File name must
     * include the ".yaml" extension.
     *
     * @param fileName collector configuration template file name
     */
    @DELETE
    @Path("/templates/{fileName}")
    public Response deleteCollectorConfigTemplate(@PathParam("fileName") String fileName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /templates/%s", fileName));
        }
        CollectorConfigService collectorConfigService = CollageFactory.getInstance().getCollectorConfigService();
        try {
            if (!collectorConfigService.deleteCollectorConfigTemplate(fileName)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                        entity(String.format("Collector configuration template [%s] delete failed", fileName)).build());
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).
                    entity(String.format("Collector configuration template [%s] error: %s", fileName, iae.getMessage())).
                    build());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(String.format("An error occurred processing delete request for collector configuration template [%s]",
                            fileName)).build());
        }
    }
}
