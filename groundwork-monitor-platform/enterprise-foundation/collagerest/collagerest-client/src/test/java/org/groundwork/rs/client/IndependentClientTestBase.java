package org.groundwork.rs.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*;
import java.lang.*;
import org.apache.commons.codec.binary.Base64;

import static org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties._hostsToGenerate;
import static org.junit.Assert.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoHost;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.Response;


/**
 * Created by rphillips on 3/1/16.
 */



public class IndependentClientTestBase {

    public String restUserID = IndependentGeneralProperties._restUserID;
    public String restPassword = IndependentGeneralProperties._restPassword;
    public String token = "";
    public String appName = IndependentGeneralProperties._appName;

    public String authHttpAcceptHeader = IndependentGeneralProperties._authHttpAcceptHeader;
    public String authHttpContentType = IndependentGeneralProperties._authHttpContentType;
    public String authUri = IndependentGeneralProperties._authUri;
    public String baseUrl = IndependentGeneralProperties._baseUrl;
    protected static Log log = LogFactory.getLog(IndependentClientTestBase.class);


    @Before
    @Category(org.groundwork.rs.client.IndependentClientTestBase.class)
    public void setUp() throws Exception {

        doauth();
        cleanupAnyExistingHostFromFailedRun();
    }

    @After
    @Category(org.groundwork.rs.client.IndependentClientTestBase.class)
    public void cleanupAnyExistingHostFromFailedRun() throws Exception
    {
        System.out.println("**** Begin Final Cleanup");
        System.out.println("-----------------------------------------------------------------");

        Integer hostsmissed = 0;
        Integer devicesmissed = 0;
        Integer eventsmissed = 0;

        for(int x=0; x < _hostsToGenerate; x++ ){
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost host = client.lookup("Test-Server-Dev-" + x + 100);
            if (host != null) {
                String hostIds = "Test-Server-Dev-" + x + 100;
                System.out.println("**** CleanUp Hosts: " + hostIds);
                List<String> ids = new ArrayList<String>();
                Collections.addAll(ids, hostIds.split(","));
                client.delete(ids);
                hostsmissed++;
            }
        }

        for(int x=0; x < _hostsToGenerate; x++ ){
            String hostIds = "Test-Server-Dev-" + x + "_" + x;
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost host = client.lookup(hostIds);
            if (host != null) {
                hostsmissed++;
                System.out.println("**** CleanUp Hosts: " + hostIds);
                List<String> ids = new ArrayList<String>();
                Collections.addAll(ids, hostIds.split(","));
                client.delete(ids);
            }
        }

        for(int x=0; x < 1; x++ ){
            String hostIds = "Test-Server-Dev-Ack";
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost host = client.lookup(hostIds);
            if (host != null) {
                hostsmissed++;
                System.out.println("**** CleanUp Hosts: " + hostIds);
                List<String> ids = new ArrayList<String>();
                Collections.addAll(ids, hostIds.split(","));
                client.delete(ids);
            }
        }


        for(int x=0; x < _hostsToGenerate; x++ ){

            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost host = client.lookup("Test-Server-Dev-" + x);
            if (host != null) {
                String hostIds = "Test-Server-Dev-" + x;
                System.out.println("**** CleanUp Hosts: " + hostIds);
                List<String> ids = new ArrayList<String>();
                Collections.addAll(ids, hostIds.split(","));
                client.delete(ids);
                hostsmissed++;
            }
        }

        for(int x=0; x < _hostsToGenerate; x++ ){
            DeviceClient deviceClient = new DeviceClient(IndependentGeneralProperties._baseUrl);
            DtoDevice device = deviceClient.lookup("000.000.000." + x);

            if (device != null)
            {
                devicesmissed++;
                deviceClient.delete("000.000.000." + x);
                System.out.println("Cleaning up device: " + "000.000.000." + x);
            }

        }

        for(int x=0; x < _hostsToGenerate; x++ ){
            DeviceClient deviceClient = new DeviceClient(IndependentGeneralProperties._baseUrl);
            DtoDevice device = deviceClient.lookup("000.000.000." + x + 100);

            if (device != null)
            {
                devicesmissed++;
                deviceClient.delete("000.000.000." + x + 100);

                System.out.println("Cleaning up device: " + "000.000.000." + x + 100);
            }

        }

        EventClient client1 = new EventClient(baseUrl);

        List<DtoEvent> events2 = client1.query("host = 'Test-Server-Dev-0'");

        if (events2 != null) {
            for (DtoEvent event : events2) {
                client1.delete(event.getId().toString());
                System.out.println("Deleted Event: " + event.getId());
                eventsmissed++;
            }
        }

        EventClient client2 = new EventClient(baseUrl);

        List<DtoEvent> events3 = client2.query("host = 'Test-Server-Dev-Ack'");

        if (events3 != null) {
            for (DtoEvent event : events3) {
                client2.delete(event.getId().toString());
                System.out.println("Deleted Event: " + event.getId());
                eventsmissed++;
            }
        }

        EventClient client3 = new EventClient(baseUrl);

        List<DtoEvent> events4 = client3.query("host = 'localhost'");
        if (events4 != null) {
            for (DtoEvent event : events4) {
                client3.delete(event.getId().toString());
                System.out.println("Deleted Event: " + event.getId());
                eventsmissed++;
            }
        }


        System.out.println(String.format("**** Found total of %d items missed during testing cleanup.", hostsmissed+devicesmissed+eventsmissed));
        System.out.println("-----------------------------------------------------------------");

        System.out.println("Cleaned Hosts: " + hostsmissed);
        System.out.println("Cleaned Devices: " + devicesmissed);
        System.out.println("Cleaned Events: " + eventsmissed);

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** End Final Cleanup");

    }



    public void doauth() throws Exception {
        System.out.println("**** @Before Unit Test Authentication with URL: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        String userName = restUserID;
        String password = restPassword;
        String app = appName;
        if (userName != null && password != null) {
            AuthClient authClient = new AuthClient(baseUrl);
            AuthClient.Response response = authClient.login(userName, password, app);
            assert response.getStatus() == Response.Status.OK;
            System.out.println("-----------------------------------------------------------------");
            System.out.println("**** Authentication Complete with Status: " + response.getStatus());
        }
    }


}