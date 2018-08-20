package org.groundwork.rs.client.clienttestingtoolbox;

import org.groundwork.rs.client.ApplicationTypeClient;
import org.groundwork.rs.dto.*;

import java.util.*;

/**
 * Created by rphillips on 3/23/16.
 */
public class ToolBox {

    public DtoHostList CreateHost(int numOfHosts, String hostNamePrefix, String serverPrefix, String agentIdPrefix, String monitorStatus,
                                        String appType, String deviceId, String monitorServer, String deviceDisplayName,
                                        HashMap<String, String> hostProps) throws Exception
    {

        DtoHostList hosts = new DtoHostList();
        for(int x=0; x < numOfHosts; x++ ){

            DtoHost host = new DtoHost();
            host.setHostName(hostNamePrefix + x);
            host.setDescription(serverPrefix + x);
            host.setAgentId(agentIdPrefix + x);
            host.setMonitorStatus(monitorStatus);
            host.setAppType(appType);
            host.setDeviceIdentification(deviceId + x);
            host.setMonitorServer(monitorServer);
            host.setDeviceDisplayName(deviceDisplayName + x);

            for (String key: hostProps.keySet()) {
                if (key == "Latency"){
                    host.putProperty(key, Double.parseDouble(hostProps.get(key)));
                }
                else{
                    host.putProperty(key, hostProps.get(key));
                }
            }

            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            host.putProperty("LastStateChange", last);
            hosts.add(host);

            System.out.println("**** Generate Host: " + "Test-Server-Dev-" + x);


        }

        return hosts;
    }

    public void CreateAppType(String currentClientTest, String baseUrl, String appTypeName, String description, HashMap<String,String> EntityProp,
                              String displayName, int appTypeId, String stateTransitionCriteria) throws Exception
    {
        System.out.println(String.format("**** Generate AppType to support %d Testing.", currentClientTest));
        System.out.println("-----------------------------------------------------------------");

        DtoApplicationType apptype = new DtoApplicationType();
        DtoApplicationTypeList apptypelist = new DtoApplicationTypeList();
        int EntityPropOrder = 0;

        for (String key: EntityProp.keySet()) {
            DtoEntityProperty prop = new DtoEntityProperty(key,EntityProp.get(key),EntityPropOrder);
            apptypelist.add(apptype);
            EntityPropOrder++;
        }

        apptype.setDescription(description);
        apptype.setDisplayName(displayName);
        apptype.setId(appTypeId);
        apptype.setName(appTypeName);
        apptype.setStateTransitionCriteria(stateTransitionCriteria);

        ApplicationTypeClient appclient = new ApplicationTypeClient(baseUrl);

        appclient.post(apptypelist);




    }
}
