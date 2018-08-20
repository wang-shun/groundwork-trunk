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

package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoSuggestions;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * SuggestionsClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class SuggestionsClient extends BaseRestClient {

    private static final String LIMIT_PARAM = "limit";

    /**
     * Create a Suggestions Client for performing queries related to Host,
     * Service, Host Group, Service Group, and/or Custom Group names.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public SuggestionsClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Suggestions Client for performing queries related to Host,
     * Service, Host Group, Service Group, and/or Custom Group names.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public SuggestionsClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Query service for suggestions matching name for entityTypes. Simple path wildcards, ('*'
     * and '?'), are supported in the query name. A null or blank query name results in all
     * possible suggestions being queried. If query limit is less than zero, no limit will be
     * applied. Suggestions for hosts include host identity host names, (aliases). Entity
     * types are specified using a list of strings delimited by common separators.
     *
     * @param name query name or null
     * @param limit query limit or -1
     * @param entityTypes entity types strings list
     * @return query suggestions
     */
    public DtoSuggestions query(String name, int limit, String entityTypes) {
        try {
            String requestParams = buildEncodedQueryParams(new String[]{LIMIT_PARAM}, new String[]{Integer.toString(limit)});
            String requestUrl = buildUrlWithPathsAndQueryParams("/suggestions/query/", encode(entityTypes),
                    (((name != null) && !name.isEmpty()) ? encode(name) : null), requestParams);
            String requestDescription = "suggestions query";
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoSuggestions>(){});
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Get service descriptions for a specified host name. Host name may be either a host
     * or host identity, (alias), hostname.
     *
     * @param hostName host name
     * @return list of service descriptions or empty list
     */
    public List<DtoName> hostServices(String hostName) {
        try {
            String requestUrl = buildUrlWithPath("/suggestions/services/", encode(hostName));
            String requestDescription = "suggestions services";
            DtoNamesList dtoNamesList = clientRequest(requestUrl, requestDescription, new GenericType<DtoNamesList>(){});
            return ((dtoNamesList != null) ? dtoNamesList.getNames() : Collections.EMPTY_LIST);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }


    /**
     * Get host names for a specified service description. Returned host names include both
     * host and host identity, (alias), host names. Mapping to canonical host name is returned
     * for host identities.
     *
     * @param serviceDescription service description
     * @return list of host names or empty list
     */
    public List<DtoName> serviceHosts(String serviceDescription) {
        try {
            String requestUrl = buildUrlWithPath("/suggestions/hosts/", encode(serviceDescription));
            String requestDescription = "suggestions hosts";
            DtoNamesList dtoNamesList = clientRequest(requestUrl, requestDescription, new GenericType<DtoNamesList>(){});
            return ((dtoNamesList != null) ? dtoNamesList.getNames() : Collections.EMPTY_LIST);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }
}
