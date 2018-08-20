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

package org.groundwork.cloudhub.connectors.icinga2.client;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.groundwork.cloudhub.configuration.Icinga2Connection;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Icinga2EventsClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2EventsClient extends BaseIcinga2Client {

    private static Logger log = Logger.getLogger(Icinga2EventsClient.class);

    private static final String EVENTS_API = "/v1/events";
    private static final String QUEUE_PARAM_NAME = "queue";
    private static final String EVENT_TYPES_PARAM_NAME = "types";
    private static final String [] EVENT_TYPES_NAMES = new String[] {
            "CheckResult",
            "StateChange",
            "Notification",
            "AcknowledgementSet",
            "AcknowledgementCleared",
            "CommentAdded",
            "CommentRemoved",
            "DowntimeAdded",
            "DowntimeRemoved",
            "DowntimeTriggered"};

    private static final long INITIAL_BACKOFF_INTERVAL = 2000;
    private static final long STOP_MAX_WAIT = 10000;

    private String queue;
    private String eventsUrl;
    private Icinga2EventsClientListener listener;

    private interface CloseableRunnable extends Runnable {
        void close();
    }
    private CloseableRunnable eventsClient;
    private Thread eventsClientThread;

    /**
     * Construct Icinga2 events client.
     *
     * @param server server host name
     * @param port server port
     * @param user authentication user
     * @param password authentication password
     * @param trustSSLCACertificate trusted SSL CA certificate
     * @param trustSSLCACertificateKeystore trusted SSL CA certificate keystore
     * @param trustSSLCACertificateKeystorePassword trusted SSL CA certificate keystore password
     * @param trustAllSSL trust all SSL certificates
     * @param queue events queue name
     * @param listener events listener
     */
    public Icinga2EventsClient(String server, int port, String user, String password, File trustSSLCACertificate,
                               File trustSSLCACertificateKeystore, String trustSSLCACertificateKeystorePassword,
                               boolean trustAllSSL, String queue, Icinga2EventsClientListener listener) {
        super(server, port, user, password, trustSSLCACertificate, trustSSLCACertificateKeystore,
                trustSSLCACertificateKeystorePassword, trustAllSSL);
        this.queue = queue;
        this.eventsUrl = buildEventsUrl();
        this.listener = listener;
    }

    /**
     * Construct Icinga2 events client from connection configuration.
     *
     * @param connection connection configuration
     * @param queue events queue name
     * @param listener events listener
     */
    public Icinga2EventsClient(Icinga2Connection connection, String queue, Icinga2EventsClientListener listener) {
        super(connection);
        this.queue = queue;
        this.eventsUrl = buildEventsUrl();
        this.listener = listener;
    }

    /**
     * Start client.
     */
    public void start() {
        eventsClient = new CloseableRunnable() {
            private volatile boolean closed;
            private volatile HttpPost eventsHttpRequest;
            private long backoffInterval = INITIAL_BACKOFF_INTERVAL;
            private ObjectMapper jsonObjectMapper = new Icinga2ObjectMapper();

            @Override
            public void run() {
                // get configured http client for events stream, (w/o read timeout)
                HttpClient httpClient = createHttpClient(false);
                do {
                    // save events input stream to release
                    InputStream eventsInputStream = null;
                    try {
                        // connect to events stream API
                        eventsHttpRequest = new HttpPost(eventsUrl);
                        eventsHttpRequest.setHeader("Accept", MediaType.APPLICATION_JSON_TYPE.toString());
                        HttpResponse eventsHttpResponse = httpClient.execute(eventsHttpRequest);
                        int httpStatus = eventsHttpResponse.getStatusLine().getStatusCode();
                        if (httpStatus != HttpStatus.SC_OK) {
                            throw new RuntimeException("Endpoint return status: " + httpStatus);
                        }
                        // read from events response stream
                        eventsInputStream = eventsHttpResponse.getEntity().getContent();
                        BufferedReader eventsReader = new BufferedReader(new InputStreamReader(eventsInputStream, "UTF-8"));
                        for (String eventLine = eventsReader.readLine(); (eventLine != null); eventLine = eventsReader.readLine()) {
                            // read event as JSON object
                            eventLine = eventLine.trim();
                            if (!eventLine.isEmpty()) {
                                try {
                                    // parse event and notify listener
                                    JsonNode jsonEvent = jsonObjectMapper.readTree(eventLine);
                                    listener.eventReceived(jsonEvent);
                                } catch (Exception e) {
                                    log.error("Unable to read or process Icinga2 event line \"" + eventLine + "\": " + e, e);
                                }
                            }
                            // successfully read event line, reset backoff interval
                            backoffInterval = INITIAL_BACKOFF_INTERVAL;
                        }
                    } catch (Exception e) {
                        // log unexpected errors when not closed, (abort throws exceptions)
                        if (!closed) {
                            log.error("Unexpected Icinga2 events endpoint error: " + e, e);
                        }
                        // backoff on exception waiting for possible resolution
                        synchronized (this) {
                            if (!closed) {
                                try {
                                    // wait and extend next backoff interval
                                    wait(backoffInterval);
                                    backoffInterval *= 2;
                                } catch (InterruptedException ie) {
                                }
                            }
                        }
                    } finally {
                        // release events input stream and http request
                        if (eventsInputStream != null) {
                            try {
                                eventsInputStream.close();
                            } catch (IOException ioe) {
                            }
                        }
                        eventsHttpRequest = null;
                    }
                    // retry to establish events stream
                } while (!closed);
                // notify listener client closed
                listener.closed();
            }

            @Override
            public synchronized void close() {
                if (!closed) {
                    // close and abort events http request
                    closed = true;
                    HttpPost abortHttpRequest = eventsHttpRequest;
                    if (abortHttpRequest != null) {
                        abortHttpRequest.abort();
                    }
                    // notify waiting thread
                    notifyAll();
                }
            }
        };
        eventsClientThread = new Thread(eventsClient, "Icinga2EventsClientThread");
        eventsClientThread.setDaemon(true);
        eventsClientThread.start();
    }

    /**
     * Stop client.
     */
    public void stop() {
        if ((eventsClientThread != null) && (eventsClient != null)) {
            eventsClient.close();
            try {
                eventsClientThread.join(STOP_MAX_WAIT);
            } catch (InterruptedException ie) {
            }
        }
        eventsClient = null;
        eventsClientThread = null;
    }

    /**
     * Build events API url.
     *
     * @return events API url
     */
    private String buildEventsUrl() {
        List<String> paramNames = new ArrayList<>();
        List<String> paramValues = new ArrayList<>();
        paramNames.add(QUEUE_PARAM_NAME);
        paramValues.add(queue);
        for (String eventTypesName : EVENT_TYPES_NAMES) {
            paramNames.add(EVENT_TYPES_PARAM_NAME);
            paramValues.add(eventTypesName);
        }
        String params = buildEncodedQueryParams(paramNames.toArray(new String[paramNames.size()]),
                paramValues.toArray(new String[paramValues.size()]));
        return buildUrlWithQueryParams(EVENTS_API, params);
    }
}
