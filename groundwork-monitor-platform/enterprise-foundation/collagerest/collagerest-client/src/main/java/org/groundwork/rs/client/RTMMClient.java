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

import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoCustomGroupList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceGroupList;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * RTMMClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMClient extends BaseRestClient {

    /**
     * Create a RTMM REST Client for performing Host, HostGroup, ServiceGroup,
     * and CustomGroup lookup and list operations.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public RTMMClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a RTMM REST Client for performing Host, HostGroup, ServiceGroup,
     * and CustomGroup lookup and list operations.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public RTMMClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * List all hosts. Returns hosts optimized depth-wise for RTMM use.
     *
     * @return list of DTO host instances or empty list
     */
    public List<DtoHost> listHosts() {
        String requestUrl = build("/rtmm/hosts");
        String requestDescription = "list hosts";
        DtoHostList dtoHostList = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostList>(){});
        return ((dtoHostList != null) ? dtoHostList.getHosts() : Collections.EMPTY_LIST);
    }

    /**
     * Lookup host by id. Returns host optimized depth-wise for RTMM use.
     *
     * @param hostId host id
     * @return DTO host instance or null
     */
    public DtoHost lookupHost(int hostId) {
        String requestUrl = buildUrlWithPath("/rtmm/hosts/", Integer.toString(hostId));
        String requestDescription = String.format("lookup host [%d]", hostId);
        DtoHostList dtoHost = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostList>(){});
        return (((dtoHost != null) && (dtoHost.size() == 1)) ? dtoHost.getHosts().get(0) : null);
    }

    /**
     * Lookup hosts by id. Returns hosts optimized depth-wise for RTMM use.
     *
     * @param hostIds host ids
     * @return list of DTO host instances or empty list
     */
    public List<DtoHost> lookupHosts(List<Integer> hostIds) {
        String requestUrl = buildUrlWithPath("/rtmm/hosts/", makeCommaSeparatedParamFromList(hostIds));
        String requestDescription = String.format("lookup hosts [%s]", hostIds.toString());
        DtoHostList dtoHostList = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostList>(){});
        return ((dtoHostList != null) ? dtoHostList.getHosts() : Collections.EMPTY_LIST);
    }

    /**
     * List all host groups. Returns host groups optimized depth-wise for
     * RTMM use.
     *
     * @return list of DTO host group instances or empty list
     */
    public List<DtoHostGroup> listHostGroups() {
        String requestUrl = build("/rtmm/hostgroups");
        String requestDescription = "list host groups";
        DtoHostGroupList dtoHostGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoHostGroupList>(){});
        return ((dtoHostGroupList != null) ? dtoHostGroupList.getHostGroups() : Collections.EMPTY_LIST);
    }

    /**
     * Lookup host group by id. Returns host group optimized depth-wise
     * for RTMM use.
     *
     * @param hostGroupId host group id
     * @return DTO host group instance or null
     */
    public DtoHostGroup lookupHostGroup(int hostGroupId) {
        String requestUrl = buildUrlWithPath("/rtmm/hostgroups/", Integer.toString(hostGroupId));
        String requestDescription = String.format("lookup host group [%d]", hostGroupId);
        DtoHostGroupList dtoHostGroup = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoHostGroupList>(){});
        return (((dtoHostGroup != null) && (dtoHostGroup.size() == 1)) ? dtoHostGroup.getHostGroups().get(0) : null);
    }

    /**
     * Lookup host groups by id. Returns host groups optimized depth-wise for
     * RTMM use.
     *
     * @param hostGroupIds host group ids
     * @return list of DTO host group instances or empty list
     */
    public List<DtoHostGroup> lookupHostGroups(List<Integer> hostGroupIds) {
        String requestUrl = buildUrlWithPath("/rtmm/hostgroups/", makeCommaSeparatedParamFromList(hostGroupIds));
        String requestDescription = String.format("lookup host groups [%s]", hostGroupIds.toString());
        DtoHostGroupList dtoHostGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoHostGroupList>(){});
        return ((dtoHostGroupList != null) ? dtoHostGroupList.getHostGroups() : Collections.EMPTY_LIST);
    }

    /**
     * List all service groups. Returns service groups optimized depth-wise
     * for RTMM use.
     *
     * @return list of DTO service group instances or empty list
     */
    public List<DtoServiceGroup> listServiceGroups() {
        String requestUrl = build("/rtmm/servicegroups");
        String requestDescription = "list service groups";
        DtoServiceGroupList dtoServiceGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoServiceGroupList>(){});
        return ((dtoServiceGroupList != null) ? dtoServiceGroupList.getServiceGroups() : Collections.EMPTY_LIST);
    }

    /**
     * Lookup service group by id. Returns service group optimized depth-wise
     * for RTMM use.
     *
     * @param serviceGroupId service group id
     * @return DTO service group instance or null
     */
    public DtoServiceGroup lookupServiceGroup(int serviceGroupId) {
        String requestUrl = buildUrlWithPath("/rtmm/servicegroups/", Integer.toString(serviceGroupId));
        String requestDescription = String.format("lookup service group [%d]", serviceGroupId);
        DtoServiceGroupList dtoServiceGroup = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoServiceGroupList>(){});
        return (((dtoServiceGroup != null) && (dtoServiceGroup.size() == 1)) ?
                dtoServiceGroup.getServiceGroups().get(0) : null);
    }

    /**
     * Lookup service groups by id. Returns service groups optimized depth-wise
     * for RTMM use.
     *
     * @param serviceGroupIds service group ids
     * @return list of DTO service group instances or empty list
     */
    public List<DtoServiceGroup> lookupServiceGroups(List<Integer> serviceGroupIds) {
        String requestUrl = buildUrlWithPath("/rtmm/servicegroups/", makeCommaSeparatedParamFromList(serviceGroupIds));
        String requestDescription = String.format("lookup service groups [%s]", serviceGroupIds.toString());
        DtoServiceGroupList dtoServiceGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoServiceGroupList>(){});
        return ((dtoServiceGroupList != null) ? dtoServiceGroupList.getServiceGroups() : Collections.EMPTY_LIST);
    }

    /**
     * List all custom groups. Returns custom groups optimized depth-wise for
     * RTMM use.
     *
     * @return list of DTO custom group instances or empty list
     */
    public List<DtoCustomGroup> listCustomGroups() {
        String requestUrl = build("/rtmm/customgroups");
        String requestDescription = "list custom groups";
        DtoCustomGroupList dtoCustomGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoCustomGroupList>(){});
        return ((dtoCustomGroupList != null) ? dtoCustomGroupList.getCustomGroups() : Collections.EMPTY_LIST);
    }

    /**
     * Lookup custom group by id. Returns custom group optimized depth-wise
     * for RTMM use.
     *
     * @param customGroupId custom group id
     * @return DTO custom group instance or null
     */
    public DtoCustomGroup lookupCustomGroup(int customGroupId) {
        String requestUrl = buildUrlWithPath("/rtmm/customgroups/", Integer.toString(customGroupId));
        String requestDescription = String.format("lookup custom group [%d]", customGroupId);
        DtoCustomGroupList dtoCustomGroup = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoCustomGroupList>(){});
        return (((dtoCustomGroup != null) && (dtoCustomGroup.size() == 1)) ? dtoCustomGroup.getCustomGroups().get(0) :
                null);
    }

    /**
     * Lookup custom groups by id. Returns custom groups optimized depth-wise
     * for RTMM use.
     *
     * @param customGroupIds custom group ids
     * @return list of DTO custom group instances or empty list
     */
    public List<DtoCustomGroup> lookupCustomGroups(List<Integer> customGroupIds) {
        String requestUrl = buildUrlWithPath("/rtmm/customgroups/", makeCommaSeparatedParamFromList(customGroupIds));
        String requestDescription = String.format("lookup custom groups [%s]", customGroupIds.toString());
        DtoCustomGroupList dtoCustomGroupList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoCustomGroupList>(){});
        return ((dtoCustomGroupList != null) ? dtoCustomGroupList.getCustomGroups() : Collections.EMPTY_LIST);
    }
}
