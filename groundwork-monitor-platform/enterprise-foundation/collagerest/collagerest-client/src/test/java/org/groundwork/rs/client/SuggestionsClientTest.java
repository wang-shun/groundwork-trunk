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
import org.groundwork.rs.dto.DtoSuggestion;
import org.groundwork.rs.dto.DtoSuggestions;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SuggestionsClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class SuggestionsClientTest extends AbstractClientTest {

    @Test
    public void testSuggestionsQueries() {
        if (serverDown) return;
        SuggestionsClient client = new SuggestionsClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        testSuggestionsQueries(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testSuggestionsQueries(client);
    }

    public void testSuggestionsQueries(SuggestionsClient client) {
        // test suggestions query
        DtoSuggestions suggestions = client.query("local*", -1, "HOST,SERVICE");
        assert suggestions != null;
        assert suggestions.getCount() >= 2;
        assert suggestions.getSuggestions() != null;
        assert suggestions.getSuggestions().size() >= 2;
        List<DtoSuggestion> assertSuggestions = new ArrayList<DtoSuggestion>();
        assertSuggestions.add(new DtoSuggestion("localhost", "HOST"));
        assertSuggestions.add(new DtoSuggestion("local_cpu_httpd", "SERVICE"));
        for (DtoSuggestion suggestion : suggestions.getSuggestions()) {
            for (Iterator<DtoSuggestion> assertSuggestionIter = assertSuggestions.iterator(); assertSuggestionIter.hasNext();) {
                DtoSuggestion assertSuggestion = assertSuggestionIter.next();
                if (assertSuggestion.getName().equals(suggestion.getName()) && assertSuggestion.getEntityType().equals(suggestion.getEntityType())) {
                    assertSuggestionIter.remove();
                    break;
                }
            }
        }
        assert assertSuggestions.isEmpty();
        suggestions = client.query("localhost", 1, "HOST");
        assert suggestions != null;
        assert suggestions.getCount() >= 1;
        assert suggestions.getSuggestions() != null;
        assert suggestions.getSuggestions().size() == 1;
        assert suggestions.getSuggestions().get(0).getName().equals("localhost");
        assert suggestions.getSuggestions().get(0).getEntityType().equals("HOST");
        suggestions = client.query(null, 1, "HOST");
        assert suggestions != null;
        assert suggestions.getCount() >= 1;
        assert suggestions.getSuggestions() != null;
        assert suggestions.getSuggestions().size() == 1;

        // test services
        List<DtoName> serviceDescriptions = client.hostServices("localhost");
        assert serviceDescriptions != null;
        assert !serviceDescriptions.isEmpty();
        assert serviceDescriptions.contains(new DtoName("local_cpu_httpd"));

        // test hosts
        List<DtoName> hostNames = client.serviceHosts("local_cpu_httpd");
        assert hostNames != null;
        assert !hostNames.isEmpty();
        assert hostNames.contains(new DtoName("localhost", "localhost"));
    }
}
