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

package com.groundwork.collage.biz;

import com.groundwork.collage.biz.model.SuggestionEntityType;
import com.groundwork.collage.biz.model.Suggestions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SuggestionsService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface SuggestionsService {

    public final static String SERVICE = "com.groundwork.collage.biz.SuggestionsService";

    /**
     * Query service for suggestions matching name for entityType. Simple path wildcards, ('*'
     * and '?'), are supported in the query name. A null or blank query name results in all
     * possible suggestions being queried. If query limit is less than zero, no limit will be
     * applied. Suggestions for hosts include host identity host names, (aliases).
     *
     * @param name query name or null
     * @param limit query limit or -1
     * @param entityType entity type
     * @return query suggestions
     * @throws Exception on query error
     */
    Suggestions querySuggestions(String name, int limit, SuggestionEntityType entityType) throws Exception;

    /**
     * Query service for suggestions matching name for entityTypes. Simple path wildcards, ('*'
     * and '?'), are supported in the query name. A null or blank query name results in all
     * possible suggestions being queried. If query limit is less than zero, no limit will be
     * applied. Suggestions for hosts include host identity host names, (aliases).
     *
     * @param name query name or null
     * @param limit query limit or -1
     * @param entityTypes entity types
     * @return query suggestions
     * @throws Exception on query error
     */
    Suggestions querySuggestions(String name, int limit, Set<SuggestionEntityType> entityTypes) throws Exception;

    /**
     * Get service descriptions for a specified host name. Host name may be either a host
     * or host identity, (alias), hostname.
     *
     * @param hostName host name
     * @return service descriptions
     */
    List<String> hostServiceDescriptions(String hostName);

    /**
     * Get host names for a specified service description. Returned host names include both
     * host and host identity, (alias), host names. Mapping to canonical host name is returned
     * for host identities.
     *
     * @param serviceDescription service description
     * @return host canonical names map
     */
    Map<String,String> serviceHostNames(String serviceDescription);
}
