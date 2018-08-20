package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoAvailability;
import org.groundwork.rs.dto.DtoStateStatistic;
import org.groundwork.rs.dto.DtoStateStatisticList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class StatisticsClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(StatisticsClient.class);
    private static final String API_ROOT_SINGLE = "/statistics";
        private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON = "Exception executing %s with status code of %d, reason: %s";

    public StatisticsClient(String deploymentUrl) {
            super(deploymentUrl);
    }

    public StatisticsClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoStateStatistic totalsByHosts() throws CollageRestException {
        return singleStatisticRequest(buildUrlWithPath(API_ROOT, "totals/hosts"), "total statistics by host");
    }

    public DtoStateStatistic totalsByServices() throws CollageRestException {
        return singleStatisticRequest(buildUrlWithPath(API_ROOT, "totals/services"), "total statistics by services");
    }

    public DtoStateStatistic getHostStatisticsByHostNames(List<String> hostNames) {
        return singleStatisticRequest(buildUrlWithPath(API_ROOT, "hosts/" + makeCommaSeparatedParamFromList(hostNames)),
                "host statistics by hostNames");
    }

    public List<DtoStateStatistic> getHostGroupStatistics() {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "hostgroups"), "host group statistics");
    }

    public List<DtoStateStatistic> getHostGroupStatisticsByHostGroupNames(List<String> hostGroupNames) {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "hostgroups/" + makeCommaSeparatedParamFromList(hostGroupNames)),
                "host group statistics by hostGroupNames");
    }

    public List<DtoStateStatistic> getServiceStatistics() {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "services"), "service statistics");
    }

    public List<DtoStateStatistic> getServiceStatisticsByHostNames(List<String> hostNames) {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "services/hosts/" + makeCommaSeparatedParamFromList(hostNames)),
                "services statistics by hostNames");
    }

    public List<DtoStateStatistic> getServiceStatisticsByHostGroupNames(List<String> hostGroupNames) {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "services/hostgroups/" + makeCommaSeparatedParamFromList(hostGroupNames)),
                "services statistics by hostGroupNames");
    }

    public List<DtoStateStatistic> getServiceStatisticsByServiceGroupNames(List<String> serviceGroupNames) {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "services/servicegroups/" + makeCommaSeparatedParamFromList(serviceGroupNames)),
                "services statistics by serviceGroupNames");
    }

    public List<DtoStateStatistic> getServiceStatisticsByServiceGroups() {
        return multiStatisticRequest(buildUrlWithPath(API_ROOT, "services/servicegroups"),
                "services statistics by serviceGroupNames");
    }

    public DtoAvailability hostAvailabilityByHostGroupName(String hostGroupName) throws CollageRestException {
        String[] names = {"hostGroup"};
        String[] values = {hostGroupName};
        String url = null;
        try {
            url = buildUrlWithPathAndQueryParams(API_ROOT, "availability/hosts", buildEncodedQueryParams(names, values));
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
        return availabilityRequest(url, "host availability by host group name");
    }

    public DtoAvailability serviceAvailabilityByHostGroupName(String hostGroupName) throws CollageRestException {
        String[] names = {"hostGroup"};
        String[] values = {hostGroupName};
        String url = null;
        try {
            url = buildUrlWithPathAndQueryParams(API_ROOT, "availability/services", buildEncodedQueryParams(names, values));
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
        return availabilityRequest(url, "service availability by host group name");
    }

    public DtoAvailability serviceAvailabilityByServiceGroupName(String serviceGroupName) throws CollageRestException {
        String[] names = {"serviceGroup"};
        String[] values = {serviceGroupName};
        String url = null;
        try {
            url = buildUrlWithPathAndQueryParams(API_ROOT, "availability/services", buildEncodedQueryParams(names, values));
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
        return availabilityRequest(url, "service availability by service group name");
    }

    public DtoAvailability availabilityByHost(String hostName) throws CollageRestException {
        return availabilityRequest(buildUrlWithPath(API_ROOT, "availability/hosts"), "total statistics by host");
    }

    private DtoStateStatistic singleStatisticRequest(String url, String description) throws CollageRestException {
        ClientResponse<DtoStateStatistic> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoStateStatistic>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoStateStatistic statistic = response.getEntity(new GenericType<DtoStateStatistic>() {});
                    return statistic;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format(EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON,
                description, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    private List<DtoStateStatistic> multiStatisticRequest(String url, String description) throws CollageRestException {
        ClientResponse<DtoStateStatisticList> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoStateStatisticList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoStateStatisticList statistics = response.getEntity(new GenericType<DtoStateStatisticList>() {});
                    return statistics.getStatistics();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format(EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON,
                description, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    private DtoAvailability availabilityRequest(String url, String description) throws CollageRestException {
        ClientResponse<DtoAvailability> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoAvailability>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoAvailability availability = response.getEntity(new GenericType<DtoAvailability>() {});
                    return availability;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format(EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON,
                description, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
