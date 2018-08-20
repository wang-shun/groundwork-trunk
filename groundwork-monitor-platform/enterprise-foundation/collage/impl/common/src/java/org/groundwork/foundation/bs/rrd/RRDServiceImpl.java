/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2009
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.groundwork.foundation.bs.rrd;

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.TLSV12ClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.gatein.sso.agent.josso.GateInSSOAgent;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.ws.api.WSRRD;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.foundation.ws.impl.WSRRDServiceLocator;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.josso.agent.Lookup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author rogerrut
 */
public class RRDServiceImpl extends EntityBusinessServiceImpl implements
        RRDService, RejectedExecutionHandler {

    /**
     * Execute the Java commands
     */
    private ThreadPoolExecutor executor = null;

    /**
     * Property for thread timeout in seconds
     */
    private long threadInterruptSec = 2;

    /**
     * Property for pool size
     */
    private int threadPoolSize = 2;

    /**
     * RRD tool path
     */
    private String rrdToolPath = null;

    /**
     * Enable Logging *
     */
    protected static Log log = LogFactory.getLog(RRDServiceImpl.class);

    /**
     * Business Services used within RRDService
     */
    private HostIdentityService hostIdentityService = null;

    /**
     * The service status service.
     */
    private StatusService serviceStatusService = null;

    /**
     * The cacti interface delimiter.
     */
    private String cactiInterfaceDelimiter = null;

    /**
     * The cacti interface label url delemiter.
     */
    private String cactiInterfaceLabelURLDelemiter = null;
    /**
     * Authorization Encoding String
     */
    private String authorizationEncodingString;

    private String portalProxyUserName = null;

    private String portalProxyPassword = null;

    /**
     * WSRRDServiceLocator
     */
    private WSRRDServiceLocator rrdLocator = null;

    /**
     * CACTI_GRAPH_WIDTH_ADJUSTMENT_CONSTANT
     */
    private static final int CACTI_GRAPH_WIDTH_ADJUSTMENT_CONSTANT = 22;

    /**
     * Remote RRD foundation configuration property names.
     */
    private static final String REMOTE_RRD_CONFIG_PROPERTY_PREFIX = "remote.rrd.";
    private static final String REMOTE_RRD_CONFIG_HOST_SUFFIX = ".host";
    private static final String REMOTE_RRD_CONFIG_CREDENTIALS_SUFFIX = ".credentials";
    private static final String REMOTE_RRD_CONFIG_CREDENTIALS_ENCRYPTED_SUFFIX = ".credentials.encryption.enabled";
    /**
     * Remote RRD foundation configuration property separator.
     */
    private static final String REMOTE_RRD_CONFIG_CREDENTIALS_SEPARATOR = "/";

    /**
     * Constructor Will initialize the FOundation Service around the Host
     * interface since this is how the parameters for RRD are extracted
     *
     * @param foundationDAO the foundation dao
     * @param his           the his
     * @param ss            the ss
     * @param configuration the configuration
     */
    public RRDServiceImpl(FoundationDAO foundationDAO, HostIdentityService his,
                          StatusService ss, Properties configuration) {
        super(foundationDAO, Host.INTERFACE_NAME, Host.COMPONENT_NAME);

        this.hostIdentityService = his;
        this.serviceStatusService = ss;

		/* Reading the timeout configuration for the RRDTool thread */
        if (configuration != null) {
            /*
			 * Read the path to RRD tool from the property. If not defined use
			 * default
			 */
            this.rrdToolPath = configuration.getProperty(RRD_TOOL_PATH,
                    RRDToolCall.RRD_TOOL_PATH);

            String val = configuration.getProperty(RRD_THREAD_TIMEOUT, "2");
            try {
                threadInterruptSec = Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                log.warn("Invalid configuration property value for "
                        + RRD_THREAD_TIMEOUT + " = " + val);
            }

            val = configuration.getProperty(RRD_THREAD_POOL_SIZE,
                    DEFAULT_MAX_THERADPOOL_SIZE);
            try {
                threadPoolSize = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                log.warn("Invalid configuration property value for "
                        + RRD_THREAD_POOL_SIZE + " = " + val);
                threadPoolSize = Integer.parseInt(DEFAULT_MAX_THERADPOOL_SIZE);
            }

            // Read the cacti delimiters from the properties file.
            this.cactiInterfaceDelimiter = configuration.getProperty(
                    CACTI_INTERFACE_DELIMITER, "!!");
            this.cactiInterfaceLabelURLDelemiter = configuration.getProperty(
                    CACTI_INTERFACE_LABELURL_DELIMITER, ";;");

            portalProxyUserName = configuration.getProperty(
                    "portal.proxy.user", "user").trim();
            portalProxyPassword = configuration.getProperty(
                    "portal.proxy.password", "user").trim();
            portalProxyPassword = JasyptUtils.jasyptDecrypt(portalProxyPassword);

            authorizationEncodingString = "Basic "
                    + new sun.misc.BASE64Encoder().encode((portalProxyUserName
                    + ":" + portalProxyPassword).getBytes());
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.BusinessServiceImpl#initialize()
     */
    @Override
    public void initialize() throws BusinessServiceException {
        // Create execution thread
        executor = new ThreadPoolExecutor(
                5,
                this.threadPoolSize,
                threadPoolSize * 2,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(this.threadPoolSize * 2, true),
                this);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.BusinessServiceImpl#uninitialize()
     */
    @Override
    public void uninitialize() throws BusinessServiceException {
        if (executor != null) {
            try {
                executor.shutdown();
                executor.awaitTermination(5000, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Error shutting down RRDService Thread Pool.", e);
            }

        }
    }

    /**
     * (non-Javadoc)
     *
     * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable,
     * java.util.concurrent.ThreadPoolExecutor)
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.error("RRDService Executor - Cannot execute action task b/c all threads are active and the queue is full.");
    }

    /**
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.rrd.RRDService#generateGraph(java.lang.String,
     * java.lang.String, java.lang.String, long, long, int)
     */
    public Collection<RRDGraph> generateGraph(String applicationType,
                                              String hostName, String serviceName, long startDate, long endDate,
                                              int graphWidth) throws BusinessServiceException {

        Collection<Callable<RRDGraph>> tasks = null;
        Collection<RRDGraph> listReturn = null;

		/* Make sure that we don't get a valid length */
        int graphWidthSize = graphWidth;
        if (graphWidthSize == 0)
            graphWidthSize = DEFAULT_RRD_WIDTH;
        else
            graphWidthSize = graphWidth;

        // Get all the services for the host
        Host host = this.hostIdentityService.getHostByIdOrHostName(hostName);
        ServiceStatus service = null;
        String value = null;

        if (host != null) {
            Set statuses = host.getServiceStatuses();
            if (statuses != null) {
				/* initialize lists */
                tasks = new ArrayList<Callable<RRDGraph>>(statuses.size());
                listReturn = new ArrayList<RRDGraph>();

				/* Graphs for all Hosts or just services */
                if (serviceName == null || serviceName.length() == 0) {
					/* All services for Host */

					/* Create a graph for just the first RRD command */
                    boolean isFirstCommand = true;
                    boolean isRemoteRRDCommandSet = false;
                    String remoteRRDCommand = null;

                    StringBuilder rrdFullLabel = new StringBuilder();
                    Iterator itService = statuses.iterator();
                    while (itService.hasNext()) {
                        service = (ServiceStatus) itService.next();
                        if (service != null) {

							/*
							 * check for RemoteRRDCommand property. If set, then
							 * do a remote WS call
							 */
                            remoteRRDCommand = (String) service
                                    .getProperty(REMOTE_RRD_PROPERTY_COMMAND);
                            if (remoteRRDCommand != null
                                    && remoteRRDCommand.trim().length() > 0) {
                                isRemoteRRDCommandSet = true;
                                break;
                            }

							/* Make sure service has an RRD path defined */
                            value = (String) service
                                    .getProperty(RRD_PROPERTY_COMMAND);

                            // BUild label that includes service name
                            rrdFullLabel
                                    .append(service.getServiceDescription())
                                    .append(": ")
                                    .append((String) service
                                            .getProperty(RRD_PROPERTY_LABEL));

                            // if the value is set add it to the list to create
                            if (value != null && value.length() > 0) {
                                if (isFirstCommand) {
                                    tasks.add(new RRDToolCallImpl(
                                            this.rrdToolPath,
                                            value,
                                            (String) service
                                                    .getProperty(RRD_PROPERTY_COMMAND),
                                            service.getServiceDescription(),
                                            rrdFullLabel.toString(), startDate,
                                            endDate, graphWidth));
                                    // Generate cacti graph here
                                    Collection<RRDGraph> cactiGraphs = generateCactiGraph(
                                            host, null, startDate, endDate,
                                            graphWidthSize);
                                    if (cactiGraphs != null) {
                                        listReturn.addAll(cactiGraphs);
                                    }
                                    isFirstCommand = false;
                                } else {
									/*
									 * Set the custom command to null which
									 * signals that the RRD should not be
									 * rendered but the object should be
									 * returned
									 */
                                    tasks.add(new RRDToolCallImpl(
                                            this.rrdToolPath, value, null,
                                            service.getServiceDescription(),
                                            rrdFullLabel.toString(), startDate,
                                            endDate, graphWidth));
                                }

                            }
                            rrdFullLabel.delete(0, rrdFullLabel.length());
                        }

                    }

                    // if RemoteRRDCommand property is set, then do a remote WS
                    // call
                    if (isRemoteRRDCommandSet) {
                        invokeRemoteRRDWebService(applicationType, hostName,
                                serviceName, startDate, endDate, listReturn,
                                graphWidthSize, remoteRRDCommand);
                        // Generate cacti graph here
                        Collection<RRDGraph> cactiGraphs = generateCactiGraph(
                                host, null, startDate, endDate, graphWidthSize);
                        if (cactiGraphs != null) {
                            listReturn.addAll(cactiGraphs);
                        }
                        return listReturn;
                    }

                    if (tasks.isEmpty()) { // if there are no RRD graphs, then
                        // just generate cactis
                        // Generate cacti graph here
                        Collection<RRDGraph> cactiGraphs = generateCactiGraph(
                                host, null, startDate, endDate, graphWidthSize);
                        if (cactiGraphs != null) {
                            listReturn.addAll(cactiGraphs);
                        }
                    }
                } else {
                    // Generate the graph for a specific service
                    service = host.getServiceStatus(serviceName);
                    if (service != null) {

						/*
						 * check for RemoteRRDCommand property. If set, then do
						 * a remote WS call
						 */
                        String remoteRRDCommand = (String) service
                                .getProperty(REMOTE_RRD_PROPERTY_COMMAND);
                        if (remoteRRDCommand != null
                                && remoteRRDCommand.trim().length() > 0) {
                            return invokeRemoteRRDWebService(applicationType,
                                    hostName, serviceName, startDate, endDate,
                                    listReturn, graphWidthSize,
                                    remoteRRDCommand);
                        }

						/* Make sure service has an RRD path defined */
                        value = (String) service
                                .getProperty(RRD_PROPERTY_COMMAND);

                        // if the value is set add it to the list to create
                        if (value != null && value.length() > 0) {
                            tasks.add(new RRDToolCallImpl(this.rrdToolPath,
                                    value, (String) service
                                    .getProperty(RRD_PROPERTY_COMMAND),
                                    service.getServiceDescription(),
                                    (String) service
                                            .getProperty(RRD_PROPERTY_LABEL),
                                    startDate, endDate, graphWidth));

                        } else {
                            log.info("Service [" + serviceName
                                    + "] has no performance data attached");

                            // Return empty graph not available message.
                            RRDGraph graph = new RRDGraph(
                                    RRDGraph.CODE_NOTHING_RETURNED,
                                    RRDGraph.CODE_NOTHING_RETURNED.getBytes());
                            listReturn = new ArrayList<RRDGraph>(1);
                            listReturn.add(graph);

                            return listReturn;
                        }
                    } else {
                        // Generate cacti graph here
                        Collection<RRDGraph> cactiGraphs = generateCactiGraph(
                                host, serviceName, startDate, endDate,
                                graphWidthSize);
                        if (cactiGraphs != null) {
                            listReturn.addAll(cactiGraphs);
                        }
                    }
                }
            } else {
                log.warn("Host ["
                        + hostName
                        + "] doesn't have any services. Can't generate graph for Host");
                return null;
            }
        } else {
            log.warn("Host [" + hostName
                    + "] doesn't exist. Can't generate graph for Host");
            return null;
        }

        try {
            List<Future<RRDGraph>> futures = executor.invokeAll(tasks,
                    this.threadInterruptSec * tasks.size(), TimeUnit.SECONDS);

            if (futures != null) {
                log.info("RRDTool returned [" + futures.size() + "] RRDGraphs");

                Iterator<Future<RRDGraph>> itFutures = futures.iterator();
                while (itFutures.hasNext()) {
                    Future<RRDGraph> fut = itFutures.next();
                    listReturn.add(fut.get());
                }

                return listReturn;
            } else {
                // No graphs generated return null
                return null;
            }

        } catch (Exception e) {
            String msg = "Unable to instanstiate or perform RRD Graph command";
            log.error(msg, e);
        }

        return null;
    }

    /**
     * Fetches the RRD graphs by calling remote RRD web service. Invokes
     * foundation WS defined at "remote host name or IP" defined in
     * "remoteRRDCommand" parameter.
     *
     * @param applicationType
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
     * @param listReturn
     * @param graphWidthSize
     * @param remoteRRDCommand
     * @return Collection of RRDGrpahs returned by remote RRD WS.
     */
    private Collection<RRDGraph> invokeRemoteRRDWebService(
            String applicationType, String hostName, String serviceName,
            long startDate, long endDate, Collection<RRDGraph> listReturn,
            int graphWidthSize, String remoteRRDCommand) {
        try {
            // get the RRD WS Locator
            if (null == rrdLocator) {
                rrdLocator = new WSRRDServiceLocator();
            }
            // lookup remote credentials if specified in foundation configuration
            String remoteRDDUser = null;
            String remoteRDDPassword = null;
            for (int i = 1; true; i++) {
                String remoteRRDConfigHost =
                        FoundationConfiguration.getProperty(REMOTE_RRD_CONFIG_PROPERTY_PREFIX+i+REMOTE_RRD_CONFIG_HOST_SUFFIX);
                if (remoteRRDConfigHost == null) {
                    break;
                }
                if (!remoteRRDConfigHost.equalsIgnoreCase(remoteRRDCommand)) {
                    continue;
                }
                String remoteRRDConfigCredentials =
                        FoundationConfiguration.getProperty(REMOTE_RRD_CONFIG_PROPERTY_PREFIX+i+REMOTE_RRD_CONFIG_CREDENTIALS_SUFFIX);
                if (remoteRRDConfigCredentials == null) {
                    break;
                }
                String [] parsedRemoteRRDConfigCredentials = remoteRRDConfigCredentials.split(REMOTE_RRD_CONFIG_CREDENTIALS_SEPARATOR);
                if (parsedRemoteRRDConfigCredentials.length == 2) {
                    remoteRDDUser = parsedRemoteRRDConfigCredentials[0];
                    remoteRDDPassword = parsedRemoteRRDConfigCredentials[1];
                    String remoteRRDConfigCredentialsEncrypted  =
                            FoundationConfiguration.getProperty(REMOTE_RRD_CONFIG_PROPERTY_PREFIX+i+REMOTE_RRD_CONFIG_CREDENTIALS_ENCRYPTED_SUFFIX);
                    if ((remoteRRDConfigCredentialsEncrypted == null) || Boolean.parseBoolean(remoteRRDConfigCredentialsEncrypted)) {
                        remoteRDDPassword = JasyptUtils.jasyptDecrypt(remoteRDDPassword);
                    }
                    break;
                }
            }
            // construct the WD endpoint by replacing "remote host name or IP"
            remoteRRDCommand = REMOTE_RRD_ENDPOINT.replace(REMOTE_RRD_HOST,
                    remoteRRDCommand);
            rrdLocator.setEndpointAddress(FOUNDATION_END_POINT_RRD,
                    remoteRRDCommand);
            // get the RRD binding and invoke getGraph()
            WSRRD rrdBinding = rrdLocator.getrrd(remoteRDDUser, remoteRDDPassword);
            WSFoundationCollection graphCollection = rrdBinding.getGraph(
                    hostName, serviceName, startDate, endDate, applicationType,
                    graphWidthSize);
            RRDGraph[] rrdGraphs = graphCollection.getRrdGraph();
            if (rrdGraphs != null && rrdGraphs.length > 0) {
                for (int i = 0; i < rrdGraphs.length; i++) {
                    listReturn.add(rrdGraphs[i]);
                }
            }
            return listReturn;
        } catch (Exception e) {
            log.error(
                    "Unable to instanstiate or perform Remote RRD Graph command",
                    e);
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.rrd.RRDService#generateGraph(java.lang.String,
     * java.lang.String, java.lang.String, long)
     */
    public Collection<RRDGraph> generateGraph(String applicationType,
                                              String hostName, String serviceName, long timeInterval)
            throws BusinessServiceException {

		/* Current */
        long endDate = (System.currentTimeMillis() / 1000);
        long startDate = endDate - timeInterval;

        return generateGraph(applicationType, hostName, serviceName, startDate,
                endDate, DEFAULT_RRD_WIDTH);
    }

    /**
     * Generates the cacti graph for the given period for the host
     *
     * @param host
     * @param serviceName
     * @param startDate
     * @param endDate
     * @param graphWidth
     * @return cacti graph for the given period for the host
     */
    private Collection<RRDGraph> generateCactiGraph(Host host,
                                                    String serviceName, long startDate, long endDate, int graphWidth) {
        if (graphWidth != 0
                && graphWidth > CACTI_GRAPH_WIDTH_ADJUSTMENT_CONSTANT) {
            graphWidth = graphWidth - CACTI_GRAPH_WIDTH_ADJUSTMENT_CONSTANT;
        }

        Collection<RRDGraph> graphs = new ArrayList<RRDGraph>();
        // Generate Cacti graphs here
        String cactiValue = (String) host.getHostStatus().getProperty(
                CACTI_PROPERTY_COMMAND);

        DefaultHttpClient httpclient = null;

        try {
            // allocate HTTP client with the ability to handle circular
            // redirects that may occur when redirected from http to https
            // when portal authentication is configured with SSL
            httpclient = new DefaultHttpClient();
            TLSV12ClientConfiguration.configure(httpclient);
            httpclient.getParams().setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            HttpPost httpost = new HttpPost(this.getLoginURL());

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("josso_username",
                    portalProxyUserName));
            nvps.add(new BasicNameValuePair("josso_password",
                    portalProxyPassword));
            nvps.add(new BasicNameValuePair("josso_cmd", "login"));

            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = httpclient.execute(httpost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.consumeContent();
            }

            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            Cookie jossoCookie = this.populateJOSSOCookie(cookies);
            if (jossoCookie == null) {
                throw new RuntimeException("Missing JOSSO login authentication cookie, (login failed)");
            }

            // if the value is set add it to the list to create
            if (cactiValue != null && cactiValue.length() > 0) {
                StringTokenizer stkn = new StringTokenizer(cactiValue,
                        cactiInterfaceDelimiter);
                while (stkn.hasMoreTokens()) {
                    StringTokenizer graphInfo = new StringTokenizer(
                            stkn.nextToken(), cactiInterfaceLabelURLDelemiter);
                    String label = graphInfo.nextToken();
                    String urlString = graphInfo.nextToken();

                    InputStream is = null;
                    byte[] baResult = null;
                    ByteArrayOutputStream byteArrayOutputStream = null;
                    try {
                        // Construct data
                        String data = "&"
                                + URLEncoder.encode("graph_start", "UTF-8")
                                + "="
                                + URLEncoder.encode(Long.toString(startDate),
                                "UTF-8");
                        data += "&"
                                + URLEncoder.encode("graph_end", "UTF-8")
                                + "="
                                + URLEncoder.encode(Long.toString(endDate),
                                "UTF-8");
                        data += "&"
                                + URLEncoder.encode("graph_width", "UTF-8")
                                + "="
                                + URLEncoder.encode(
                                Integer.toString(graphWidth), "UTF-8");

                        urlString = urlString.replaceFirst(":443", "");
                        urlString = urlString.replaceFirst(
                                "localhost", jossoCookie.getDomain()) + data;
                        log.debug("Cacti URL===>" + urlString);
                        HttpGet rrdGet = new HttpGet(urlString);


                        response = httpclient.execute(rrdGet);

                        int responseCode = response.getStatusLine()
                                .getStatusCode();
                        log.debug("HTTP status code for RRD==>" + responseCode);
                        // check for response code 302(document found but moved
                        // to
                        // another place).
                        // TODO: Once HTTPS is implemented, uncomment this and
                        // test it
						/*
						 * if (responseCode == 302) { urlString = "https://" +
						 * url.getHost() + url.getFile();
						 * 
						 * // for Https Certificates
						 * trustAllHttpsCertificates();
						 * 
						 * // Verify the host name and always return ture to //
						 * fix // error-HTTPS hostname wrong: should be <server
						 * // name>. HostnameVerifier hv = new
						 * HostnameVerifier() { public boolean verify(String
						 * urlHostName, SSLSession session) { return true; } };
						 * HttpsURLConnection.setDefaultHostnameVerifier(hv);
						 * url = new URL(urlString); conn =
						 * url.openConnection();
						 * conn.setRequestProperty("Authorization",
						 * authorizationEncodingString);
						 * conn.setRequestProperty("Cookie",
						 * jossoCookie.toString()); conn.setDoOutput(true);
						 * 
						 * }
						 */
                        HttpEntity rrdentity = response.getEntity();
                        is = rrdentity.getContent();

                        byteArrayOutputStream = new ByteArrayOutputStream();
                        int iRead = 0;
                        byte[] bytes = new byte[4096];
                        try {
                            while ((iRead = is.read(bytes)) > 0) {
                                byteArrayOutputStream.write(bytes, 0, iRead);
                            }
                        } catch (IOException e) {
                            log.error("exception while getting byte array");
                            throw new IOException(
                                    "exception while getting byte array");
                        }
                        baResult = byteArrayOutputStream.toByteArray();

                    } catch (Exception e) {
                        log.error("Unexpected error reading "+host.getHostName()+" host RRD Graph "+label+
                                " from Cacti at "+urlString+": "+e, e);
                    } finally {
                        try {

                            if (is != null)
                                is.close();
                            if (byteArrayOutputStream != null)
                                byteArrayOutputStream.close();
                        } catch (IOException ioe) {
                            log.error("Unable to close connections to "+host.getHostName()+" host RRD Graph "+label+
                                    " from Cacti at "+urlString+": "+ioe, ioe);
                        }
                    }
                    if (serviceName != null
                            && serviceName.equalsIgnoreCase(label)) {
                        RRDGraph graph = new RRDGraph(label, baResult);
                        graphs.add(graph);
                        return graphs;
                    } else {
                        if (null != baResult) {
                            RRDGraph graph = new RRDGraph(label, baResult);
                            graphs.add(graph);
                        }
                    }
                }
            }
        } catch (Exception exc) {
            log.error("Error authenticating portal proxy login to Cacti at "+getLoginURL()+" for host "+
                    host.getHostName()+" or parsing Cacti RRD command, ("+cactiValue+"): "+exc, exc);
        } finally {
            if (httpclient != null)
                httpclient.getConnectionManager().shutdown();
        }
        return graphs;
    }

    /**
     * Create a trust manager that does not validate certificate chains
     *
     * @throws Exception
     */
    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager trustManager = new RRDTrustManager();
        trustAllCerts[0] = trustManager;
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext
                .getInstance("SSL");
        sslContext.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
                .getSocketFactory());
    }

    /**
     * RRDTrustManager which implements javax.net.ssl.TrustManager and
     * javax.net.ssl.X509TrustManager
     *
     * @author manish_kjain
     */
    public static class RRDTrustManager implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        /**
         * (non-Javadoc)
         *
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        /**
         * @param certs
         * @return boolean
         */
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        /**
         * @param certs
         * @return boolean
         */
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        /**
         * (non-Javadoc)
         *
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         * java.lang.String)
         */
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        /**
         * (non-Javadoc)
         *
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         * java.lang.String)
         */
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    /**
     * Get Login URL from Gatein josso-agent-config.xml
     *
     * @return
     */
    private String getLoginURL() {
        String login_url = null;
        try {
            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml");
            GateInSSOAgent _agent = (GateInSSOAgent) lookup.lookupSSOAgent();
            String login_base_url = _agent.getGatewayLoginUrl();
            login_url = (login_base_url.replaceAll("login.do",
                    "usernamePasswordLogin.do")).trim();
            log.debug("Login URL : " + login_url);
        } catch (Exception exc) {
            log.error("Unable to access portal JOSSO login URL for RDD Graph Catci authentication: "+exc, exc);
        } // end try/catch
        return login_url;
    }

    /**
     * Helper to get the JOSSO Cookie
     *
     * @param cookies
     * @return
     */
    private Cookie populateJOSSOCookie(List<Cookie> cookies) {
        Cookie jossoCookie = null;
        if (cookies.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                log.debug("- " + cookies.get(i).toString());
                if (cookies.get(i).getName()
                        .equalsIgnoreCase("JOSSO_SESSIONID_josso")) {
                    jossoCookie = cookies.get(i);
                    break;
                }
            }
        }
        return jossoCookie;
    }
}
