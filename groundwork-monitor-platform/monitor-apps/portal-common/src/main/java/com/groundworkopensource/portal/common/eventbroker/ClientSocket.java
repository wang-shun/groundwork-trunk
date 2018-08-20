/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.common.eventbroker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;

/**
 * This class wraps the functionality for creating a socket to connect to event
 * broker,encrypts the nagios commands and send them to event broker through
 * socket.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ClientSocket {
    /**
     * Client socket to connect to Event Broker
     */
    private Socket requesterSocket;

    /**
     * InputStream for requesterSocket
     */
    private DataInputStream in;

    /**
     * OutputStream for requesterSocket
     */
    private DataOutputStream out;

    /**
     * Boolean variable indicating if the event broker server is listening for
     * nagios commands or not.
     */
    private boolean nagiosDown = false;

    /**
     * 
     * @return nagiosDown
     */
    public boolean isNagiosDown() {
        return nagiosDown;
    }

    /**
     * 
     * @param nagiosDown
     */
    public void setNagiosDown(boolean nagiosDown) {
        this.nagiosDown = nagiosDown;
    }

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientSocket.class
            .getName());

    /**
     * Points to the server where event broker is setup
     */
    private static String eventBrokerServer;

    /**
     * Port on which action portlet should connect to for sending nagios
     * commands
     */
    private static int eventBrokerPort;

    /**
     * Encryption algorithm to be used when sending nagios commands from actions
     * portlet to the event broker
     */
    private static String encryptionAlgorithm;

    /**
     * integer constant for 8
     */
    public static final int EIGHT = 8;

    /**
     * Static initializer that reads the event broker related configuration
     * parameters (port,server,encryption algorithm)
     */
    static {
        String appType = null;
        try{
            appType = FacesUtils
                    .getContextParam(CommonConstants.APPLICATION_TYPE_CONTEXT_PARAM_NAME);
        }
        catch (Exception e){
            LOGGER.debug(e);

        }
        try {
            //appType can be null still, getApplicationType will return a default value
            ApplicationType applicationType = ApplicationType.getApplicationType(appType);
            /*
             * read server where event broker is setup from application specific
             * properties file
             */
            eventBrokerServer = PropertyUtils.getProperty(applicationType,
                    CommonConstants.EVENT_BROKER_SERVER);
            /*
             * Read port number where event broker is setup from application
             * specific properties file
             */
            eventBrokerPort = Integer.parseInt(PropertyUtils.getProperty(
                    applicationType, CommonConstants.EVENT_BROKER_PORT));
            /*
             * Read the encryption algorithm from application specific
             * properties file
             */
            encryptionAlgorithm = PropertyUtils.getProperty(applicationType,
                    CommonConstants.EVENT_BROKER_ENCRYPTION_ALGORITHM);

            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug(new StringBuilder(
                                "eventBroker properties from default properties file ==> Server [")
                                .append(eventBrokerServer).append(
                                        "], eventBroker port [ ").append(
                                        eventBrokerPort).append(
                                        "],  encryption algorithm [ ").append(
                                        encryptionAlgorithm).toString());
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * byte array to store IV.
     */
    private byte[] ivData;

    /**
     * This method reads IV send by event broker and stores it in 'ivData' byte
     * array
     * 
     * @return ivData
     * @throws GWPortalGenericException
     */
    public byte[] getIvData() throws GWPortalGenericException {
        try {
            if (EIGHT == in.read(ivData)) {
                LOGGER.debug("Successfully read IV of 8 bytes.");
            }
        } catch (IOException ioEx) {
            LOGGER.error(ioEx);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        }
        return ivData;
    }

    /**
     * @param ivData
     */
    public void setIvData(byte[] ivData) {
        this.ivData = ivData;
    }

    /**
     * Constructor
     */
    public ClientSocket() {
        ivData = new byte[EIGHT];
    }

    /**
     * This method creates a Socket and retrieves its output and input streams.
     * 
     * @throws GWPortalGenericException
     */
    public void createSocket() throws GWPortalGenericException {
        try {
            // 1. creating a socket to connect to the server
            InetAddress inetAddr = InetAddress.getByName(eventBrokerServer);
            requesterSocket = new Socket(inetAddr, eventBrokerPort);

            if (requesterSocket != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER
                            .info("Connected to "
                                    + requesterSocket.getInetAddress()
                                            .getHostAddress());
                }
            }
            // 2. get Input and Output streams
            out = new DataOutputStream(requesterSocket.getOutputStream());
            out.flush();
            in = new DataInputStream(requesterSocket.getInputStream());
        } catch (UnknownHostException e) {
            LOGGER.error(e);
            throw new GWPortalGenericException(
                    "com_groundwork_portal_statusviewer_actionsPortlet_unknown_host");
        } catch (IOException e) {
            LOGGER.error(e);
            setNagiosDown(true);
            throw new GWPortalGenericException(
                    "com_groundwork_portal_statusviewer_actionsPortlet_event_broker_not_listening");
        }
    }

    /**
     * This method closes all open streams and sockets.
     * 
     * @throws GWPortalGenericException
     */
    public void cleanup() throws GWPortalGenericException {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (requesterSocket != null) {
                requesterSocket.close();
            }
            LOGGER
                    .debug("Cleaned up requesterSocket and its input,output streams");
        } catch (IOException e) {
            LOGGER.error(e);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        }
    }

    /**
     * This method 1) creates a socket,connects to the event broker server 2)
     * Reads the IV sent by event broker 3) Encrypts the nagios commands if at
     * all encryption algorithm is specified in the statusviewer.properties file
     * 4) Send the encrypted commands to event broker
     * 
     * @param command
     * @throws GWPortalGenericException
     * 
     */
    public void run(String command) throws GWPortalGenericException {
        // creating a socket to connect to the server
        try {
            createSocket();
        } catch (GWPortalGenericException ex) {
            LOGGER.error(ex);
            throw ex;
        }
        try {
            // Create an instance of Encryptor class.
            Encryptor encryptor = new Encryptor();

            // Check the encryption algorithm
            if (null != encryptionAlgorithm) {
                // for DES
                if (Encryptor.DES.equals(encryptionAlgorithm)) {
                    // Encrypt the data using IV sent by Event Broker
                    byte[] encryptedData = encryptor.encrypt(command,
                            getIvData());
                    // send encrypted data to Event Broker
                    sendData(encryptedData);
                } else {
                    // Plain text
                    sendData(command.getBytes());
                }
            }
        } catch (Exception ioEx) {
            LOGGER.error(ioEx);
            throw new GWPortalGenericException(
                    "com_groundwork_portal_statusviewer_actionsPortlet_socketError");
        } finally {
            // Closing connection
            cleanup();
        }
    }

    /**
     * This method writes the input bytes on the socket's output stream.
     * 
     * @param encryptedBytes
     * @throws GWPortalGenericException
     */
    public void sendData(byte[] encryptedBytes) throws GWPortalGenericException {
        try {
            out.write(encryptedBytes);
            out.flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        }
    }
}
