package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HostGroupClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(HostGroupClient.class);
    private static final String API_ROOT_SINGLE = "/hostgroups";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String RPC_AUTOCOMPLETE = "autocomplete";

    public HostGroupClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public HostGroupClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoHostGroup lookup(String hostGroupName) throws CollageRestException {
        return lookup(hostGroupName, DtoDepthType.Shallow);
    }

    public DtoHostGroup lookup(String hostGroupName, DtoDepthType depthType) throws CollageRestException {
        try {
            String requestUrl = buildLookupWithDepth(API_ROOT, hostGroupName, depthType);
            String requestDescription = String.format("lookup hostGroup [%s]", hostGroupName);
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoHostGroup>(){});
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public List<DtoHostGroup> query(String query) throws CollageRestException {
        return query(query, DtoDepthType.Shallow);
    }

    public List<DtoHostGroup> query(String query, DtoDepthType depthType) throws CollageRestException {
        return query(query, depthType, -1, -1);
    }

    public List<DtoHostGroup> query(String query, int first, int count) throws CollageRestException {
        return query(query, DtoDepthType.Shallow, first, count);
    }

    public List<DtoHostGroup> query(String query, DtoDepthType depthType, int first, int count) throws CollageRestException {
        try {
            String requestUrl = buildEncodedQuery(API_ROOT, query, depthType, first, count);
            String requestDescription = String.format("%s hostgroups [%s]", (query == null) ? "list" : "query",  (query == null) ? "-" : query);
            DtoHostGroupList dtoHostGroups = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostGroupList>(){});
            return ((dtoHostGroups != null) ? dtoHostGroups.getHostGroups() : Collections.EMPTY_LIST);
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public List<DtoHostGroup> list(DtoDepthType depthType, int first, int count) throws CollageRestException {
        return query(null, depthType, first, count);
    }

    public List<DtoHostGroup> list() throws CollageRestException {
        return query(null, DtoDepthType.Shallow, -1, -1);
    }

    public List<DtoHostGroup> list(DtoDepthType depthType) throws CollageRestException {
        return query(null, depthType, -1, -1);
    }

    public DtoOperationResults post(DtoHostGroupList updates) throws CollageRestException {
        String requestUrl = build(API_ROOT_SINGLE); 
        String requestDescription = "posting hostgroups";
        DtoOperationResults results = clientRequest(HttpMethod.POST, requestUrl, updates, requestDescription, new GenericType<DtoOperationResults>() {});
        return (results != null) ? results : new DtoOperationResults();
    }

    public DtoOperationResults clear(List<String> hostGroupNamesList) throws CollageRestException {
        try {
            String hostGroupNames = makeCommaSeparatedParamFromList(hostGroupNamesList);
            String[] names = {"clear"};
            String[] values = {"true"};
            String requestUrl = buildUrlWithPathAndQueryParams(API_ROOT, hostGroupNames, buildEncodedQueryParams(names, values));
            String requestDescription = "clear hostgroups";
            DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, requestDescription, new GenericType<DtoOperationResults>() {
            });
            return (results != null) ? results : new DtoOperationResults();
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public DtoOperationResults clear(DtoHostGroupList clears) throws CollageRestException {
        try {
            String[] names = {"clear"};
            String[] values = {"true"};
            String requestUrl = buildUrlWithPathAndQueryParams(API_ROOT, null, buildEncodedQueryParams(names, values));
            String requestDescription = "clear hostgroups";
            DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, clears, requestDescription, new GenericType<DtoOperationResults>() {
            });
            return (results != null) ? results : new DtoOperationResults();
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public DtoOperationResults delete(DtoHostGroupList deletes) throws CollageRestException {
        String requestUrl = build(API_ROOT_SINGLE);
        String requestDescription = "delete hostgroups";
        DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, deletes, requestDescription, new GenericType<DtoOperationResults>(){});
        return (results != null) ? results : new DtoOperationResults();
    }

    public DtoOperationResults delete(String hostGroupName) throws CollageRestException {
        List<String> names = new ArrayList<String>();
        names.add(hostGroupName);
        return delete(names);
    }


    public DtoOperationResults delete(List<String> hostGroupNamesList) throws CollageRestException {
        String requestUrl = buildUrlWithPath(API_ROOT, makeCommaSeparatedParamFromList(hostGroupNamesList));
        String requestDescription = "delete hostgroups";
        DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, requestDescription, new GenericType<DtoOperationResults>(){});
        return (results != null) ? results : new DtoOperationResults();
    }

    /**
     * Lookup host group name autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix host group name prefix
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix) {
        return autoComplete(prefix, API_ROOT);
    }

    /**
     * Lookup host group name autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix host group name prefix
     * @param limit unique suggestions limit, (-1 for unlimited)
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix, int limit) {
        return autoComplete(prefix, API_ROOT, limit);
    }
}
