package org.groundwork.cloudhub.netapp;

import netapp.manage.NaAPIFailedException;
import netapp.manage.NaAuthenticationException;
import netapp.manage.NaElement;
import netapp.manage.NaProtocolException;
import netapp.manage.NaServer;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dtaylor on 6/24/15.
 */
public class NetAppInternalsTest {

    public static final String NETAPP_SERVER = "gwos-netapp-colo";
    public static final String NETAPP_ADMIN_USER = "admin";
    public static final String NETAPP_ADMIN_PASSWORD = "m3t30r1t3";
    public static final int NETAPP_MAJOR_VERSION = 1;
    public static final int NETAPP_MINOR_VERSION = 15;
    public static final int NETAPP_PORT = 8088;

    // SERVER TYPES
    //      SERVER_TYPE_DFM -
    //      SERVER_TYPE_FILER - *DEFAULT*
    //      SERVER_TYPE_AGENT
    //      SERVER_TYPE_NETCACHE
    //      SERVER_TYPE_OCUM

    // TRANSPORT TYPES
    //      TRANSPORT_TYPE_HTTP
    //      TRANSPORT_TYPE_HTTPS

    // PORTS (DEFAULTS)
    //  TCP_PORT_FILER = 80;
    //  TCP_PORT_NETCACHE = 80;
    //  TCP_PORT_AGENT = 4092;
    //  TCP_PORT_DFM = 8088;
    //  TCP_PORT_OCUM = 443;

//    public static final int STYLE_LOGIN_PASSWORD = 1;
//    public static final int STYLE_HOSTSEQUIV = 2;
//    public static final int STYLE_RPC = 3;
//    public static final int STYLE_CERTIFICATE = 4;


    @Test
    public void netAppVolumeTest() throws Exception {
        NaServer server = null;

        try {
            // Initialize connection to server, and
            // request version 1.0 of the API set
            //
            server = new NaServer(NETAPP_SERVER, NETAPP_MAJOR_VERSION, NETAPP_MINOR_VERSION);
//            server.setStyle(NaServer.STYLE_LOGIN_PASSWORD);
            server.setTransportType(NaServer.TRANSPORT_TYPE_HTTP);
            server.setServerType(NaServer.SERVER_TYPE_FILER);
//            server.setPort(NETAPP_PORT);
            server.setAdminUser(NETAPP_ADMIN_USER, NETAPP_ADMIN_PASSWORD);

            listDisks(server);
            listNodes(server);
            listVolumes(server);
            systemMode(server);
            listVservers(server);

            // Invokes ONTAPI API to get the DFM server version
//            NaElement about = new NaElement("dfm-about");
//
//            NaElement response = server.invokeElem(about);
//            System.out.print("Hello world!  DFM Server version is: ");
//            System.out.println(response.getChildContent("version"));
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(1);
        }
        finally {
            if (server != null) {
                server.close();
            }
        }
    }

    public static void systemMode(NaServer server) throws Exception {
        NaElement xi = new NaElement("system-get-version");
        NaElement xo = server.invokeElem(xi);
        System.out.println("system-get-version: " + xo);
    }

    public static void listVolumes(NaServer server) throws NaProtocolException,
            NaAuthenticationException, NaAPIFailedException, IOException {
        NaElement in, out;
        String tag = "";
        String vserverName, volName, aggrName, volType, volState, size, availSize;

        while (tag != null) {
//            if (args.length > 3) {
//                if (args.length < 5 || !args[3].equals("-v")) {
//                    printUsageAndExit();
//                }
//                server.setVserver(args[4]);
//            }
            in = new NaElement("volume-get-iter");
            if (!tag.equals("")) {
                in.addNewChild("tag", tag);
            }
            out = server.invokeElem(in);
            if (out.getChildIntValue("num-records", 0) == 0) {
                System.out.println("No volume(s) information available\n");
                return;
            }
            tag = out.getChildContent("next-tag");
            List volList = out.getChildByName("attributes-list").getChildren();
            Iterator volIter = volList.iterator();
            System.out.println("----------------------------------------------------");
            while (volIter.hasNext()) {
                NaElement volInfo = (NaElement) volIter.next();
                vserverName = volName = aggrName = volType = volState = size = availSize = "";
                NaElement volIdAttrs = volInfo.getChildByName("volume-id-attributes");
                if (volIdAttrs != null) {
                    vserverName = volIdAttrs.getChildContent("owning-vserver-name");
                    volName = volIdAttrs.getChildContent("name");
                    aggrName = volIdAttrs.getChildContent("containing-aggregate-name");
                    volType = volIdAttrs.getChildContent("type");
                }
                System.out.println("Vserver Name            : " + (vserverName != null ? vserverName : ""));
                System.out.println("Volume Name             : " + (volName != null ? volName : ""));
                System.out.println("Aggregate Name          : " + (aggrName != null ? aggrName : ""));
                System.out.println("Volume type             : " + (volType != null ? volType : ""));
                NaElement volStateAttrs = volInfo.getChildByName("volume-state-attributes");
                if (volStateAttrs != null) {
                    volState = volStateAttrs.getChildContent("state");
                }
                System.out.println("Volume state            : " + (volState != null ? volState : ""));
                NaElement volSizeAttrs = volInfo.getChildByName("volume-space-attributes");
                if (volSizeAttrs != null) {
                    size = volSizeAttrs.getChildContent("size");
                    availSize = volSizeAttrs.getChildContent("size-available");
                }
                System.out.println("Size (bytes)            : " + (size != null ? size : ""));
                System.out.println("Available Size (bytes)  : " + (availSize != null ? availSize : ""));
                System.out.println("----------------------------------------------------");
            }
        }
    }

    public static void listVservers(NaServer server) throws NaProtocolException,
            NaAuthenticationException, NaAPIFailedException, IOException {
        NaElement in, out;
        String rootVol, rootVolAggr, secStyle, state;
        String tag = "";

        while (tag != null) {
            in = new NaElement("vserver-get-iter");
//            if (args.length > 3) {
//                if (args.length < 5 || !args[3].equals("-v")) {
//                    printUsageAndExit();
//                }
//                server.setVserver(args[4]);
//            }
            if (!tag.equals("")) {
                in.addNewChild("tag", tag);
            }
            out = server.invokeElem(in);
            if (out.getChildIntValue("num-records", 0) == 0) {
                System.out.println("No vserver(s) information available\n");
                return;
            }
            tag = out.getChildContent("next-tag");
            List vserverList = out.getChildByName("attributes-list").getChildren();
            Iterator vserverIter = vserverList.iterator();
            System.out.println("----------------------------------------------------");
            while(vserverIter.hasNext()) {
                NaElement vserverInfo =(NaElement)vserverIter.next();
                System.out.println("Name                    : " + vserverInfo.getChildContent("vserver-name"));
                System.out.println("Type                    : " + vserverInfo.getChildContent("vserver-type"));
                rootVolAggr = vserverInfo.getChildContent("root-volume-aggregate");
                rootVol = vserverInfo.getChildContent("root-volume");
                secStyle = vserverInfo.getChildContent("root-volume-security-style");
                state = vserverInfo.getChildContent("state");
                System.out.println("Root volume aggregate   : " + (rootVolAggr != null ? rootVolAggr : ""));
                System.out.println("Root volume             : " + (rootVol != null ? rootVol : ""));
                System.out.println("Root volume sec style   : " + (secStyle != null ? secStyle : ""));
                System.out.println("UUID                    : " + vserverInfo.getChildContent("uuid"));
                System.out.println("State                   : " + (state != null ? state : ""));
                NaElement allowedProtocols = null;
                System.out.print("Allowed protocols       : ");
                if ((allowedProtocols = vserverInfo.getChildByName("allowed-protocols")) != null) {
                    List allowedProtocolsList = allowedProtocols.getChildren();
                    Iterator allowedProtocolsIter = allowedProtocolsList.iterator();
                    while(allowedProtocolsIter.hasNext()){
                        NaElement protocol = (NaElement) allowedProtocolsIter.next();
                        System.out.print(protocol.getContent() + " ");
                    }
                }
                System.out.print("\nName server switch      : ");
                NaElement nameServerSwitch = null;
                if ((nameServerSwitch = vserverInfo.getChildByName("name-server-switch")) != null) {
                    List nsSwitchList = nameServerSwitch.getChildren();
                    Iterator nsSwitchIter = nsSwitchList.iterator();
                    while(nsSwitchIter.hasNext()){
                        NaElement nsSwitch = (NaElement) nsSwitchIter.next();
                        System.out.print(nsSwitch.getContent() + " ");
                    }
                }
                System.out.println("\n----------------------------------------------------");
            }
        }
    }

    public static void listNodes(NaServer server) throws NaProtocolException,
            NaAuthenticationException, NaAPIFailedException, IOException {
        NaElement in, out;
        String tag = "";

        while (tag != null) {
            in = new NaElement("system-node-get-iter");
            if (!tag.equals("")) {
                in.addNewChild("tag", tag);
            }
            out = server.invokeElem(in);
            if (out.getChildIntValue("num-records", 0) == 0) {
                System.out.println("No Nodes(s) information available\n");
                return;
            }
            tag = out.getChildContent("next-tag");
            List nodeList = out.getChildByName("attributes-list").getChildren();
            Iterator nodeIterator = nodeList.iterator();
            System.out.println("----------------------------------------------------");
            while (nodeIterator.hasNext()) {
                NaElement nodeInfo = (NaElement) nodeIterator.next();
                String nodeName = nodeInfo.getChildContent("node");
                Integer fan = nodeInfo.getChildIntValue("env-failed-fan-count", 0);
                Long upTime = nodeInfo.getChildLongValue("node-uptime", 0L);
                System.out.printf("Node: %s, fan: %d, upTime: %d\n", nodeName, fan, upTime);
            }
        }
    }

    public static void listDisks(NaServer server) throws Exception
    {
            NaElement api = new NaElement("disk-sanown-list-info");
            //NaElement api = new NaElement("disk-sanown-filer-list-info");
            //api.addNewChild("disk","<disk>");
            api.addNewChild("node","gwos-netapp-colo-01");
            api.addNewChild("ownership-type","all");

            NaElement xo = server.invokeElem(api);
            System.out.println(xo.toPrettyString(""));

    }


}
