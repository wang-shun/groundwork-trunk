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
import com.groundwork.collage.biz.SuggestionsService;
import com.groundwork.collage.biz.model.Suggestion;
import com.groundwork.collage.biz.model.SuggestionEntityType;
import com.groundwork.collage.biz.model.Suggestions;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoSuggestion;
import org.groundwork.rs.dto.DtoSuggestions;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SuggestionsResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/suggestions")
public class SuggestionsResource extends AbstractResource {

    @GET
    @Path("/query/{entityTypes}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoSuggestions query(@PathParam("entityTypes") String entityTypes,
                                @QueryParam("limit") @DefaultValue("10") int limit) {
        return query(entityTypes, null, limit);
    }

    @GET
    @Path("/query/{entityTypes}/{pattern}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoSuggestions query(@PathParam("entityTypes") String entityTypes,
                                @PathParam("pattern") String pattern,
                                @QueryParam("limit") @DefaultValue("10") int limit) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /query/%s/%s limit=%d", entityTypes, pattern, limit));
        }
        try {
            Set<SuggestionEntityType> suggestionEntityTypes = new HashSet<SuggestionEntityType>();
            for (String entityType : entityTypes.split("[ ,:;|]")) {
                try {
                    suggestionEntityTypes.add(SuggestionEntityType.valueOf(entityType));
                } catch (IllegalArgumentException iae) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Illegal suggestion entity type: "+entityType).build());
                }
            }
            if (suggestionEntityTypes.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Suggestion entity types required").build());
            }
            SuggestionsService suggestionsService = (SuggestionsService)CollageFactory.getInstance().getAPIObject(SuggestionsService.SERVICE);
            Suggestions suggestions = suggestionsService.querySuggestions(pattern, limit, suggestionEntityTypes);
            if (suggestions == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No suggestions found").build());
            }
            List<DtoSuggestion> dtoSuggestions = new ArrayList<DtoSuggestion>(suggestions.getSuggestions().size());
            for (Suggestion suggestion : suggestions.getSuggestions()) {
                dtoSuggestions.add(new DtoSuggestion(suggestion.getName(), suggestion.getEntityType().name()));
            }
            return new DtoSuggestions(suggestions.getCount(), dtoSuggestions);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for query [%s, %s].", entityTypes, pattern)).build());
        }
    }

    @GET
    @Path("/services/{hostName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoNamesList hostServices(@PathParam("hostName") String hostName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /services/%s", hostName));
        }
        try {
            SuggestionsService suggestionsService = (SuggestionsService)CollageFactory.getInstance().getAPIObject(SuggestionsService.SERVICE);
            List<String> serviceDescriptions = suggestionsService.hostServiceDescriptions(hostName);
            if (serviceDescriptions.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Host names not found").build());
            }
            List<DtoName> dtoNames = new ArrayList<DtoName>();
            for (String serviceDescription : serviceDescriptions) {
                dtoNames.add(new DtoName(serviceDescription));
            }
            return new DtoNamesList(dtoNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for services [%s].", hostName)).build());
        }
    }

    @GET
    @Path("/hosts/{serviceDescription}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoNamesList serviceHosts(@PathParam("serviceDescription") String serviceDescription) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /hosts/%s", serviceDescription));
        }
        try {
            SuggestionsService suggestionsService = (SuggestionsService)CollageFactory.getInstance().getAPIObject(SuggestionsService.SERVICE);
            Map<String,String> hostNames = suggestionsService.serviceHostNames(serviceDescription);
            if (hostNames.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service descriptions not found").build());
            }
            List<DtoName> dtoNames = new ArrayList<DtoName>();
            for (Map.Entry<String,String> hostName : hostNames.entrySet()) {
                dtoNames.add(new DtoName(hostName.getKey(), hostName.getValue()));
            }
            return new DtoNamesList(dtoNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for hosts [%s].", serviceDescription)).build());
        }
    }
}
