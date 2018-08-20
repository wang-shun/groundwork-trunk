package org.groundwork.cloudhub.openstack;

import org.groundwork.cloudhub.connectors.openstack.HypervisorState;
import org.groundwork.cloudhub.connectors.openstack.HypervisorStatus;
import org.groundwork.cloudhub.connectors.openstack.client.HypervisorInfo;
import org.groundwork.cloudhub.connectors.openstack.client.ServerLinkInfo;
import org.groundwork.cloudhub.connectors.openstack.client.TenantInfo;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 *
 *
 * openstack-cloud.groundwork.groundworkopensource.com

 admin / rhsummit

 Compute Node:

 agno

 --------------------
 * Remember Me
 * Filters on Page
 -------------------

 ssh root@openstack-cloud.groundwork.groundworkopensource.com

 vi packstack-answers-20140319-141348.txt

 CONFIG_KEYSTORE_ADMIN_TOKEN

 */
public class    OpenStackInternalsTest {

//    public static final String KEYSTONE_TOKEN_URL = "http://openstack-cloud.groundwork.groundworkopensource.com:5000/v2.0/tokens";
//    public static final String KEYSTONE_TENANTS_URL = "http://openstack-cloud.groundwork.groundworkopensource.com:5000/v2.0/tenants";
    public static final String KEYSTONE_TOKEN_URL = "http://agno.groundwork.groundworkopensource.com:5000/v2.0/tokens";
    public static final String KEYSTONE_TENANTS_URL = "http://agno.groundwork.groundworkopensource.com:5000/v2.0/tenants";

    //public static final String NOVA_SERVERS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/servers";
    public static final String NOVA_SERVERS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/servers/detail";
    public static final String NOVA_SERVERS_V3_URL = "http://agno.groundwork.groundworkopensource.com:8774/v3/servers";
    public static final String NOVA_SERVER_DETAILS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/servers/details";
    public static final String NOVA_SERVER_DETAILS_V3_URL = "http://agno.groundwork.groundworkopensource.com:8774/v3/servers/%s";
    public static final String NOVA_SERVER_QUOTAS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-quota-sets";
    public static final String NOVA_OS_HOSTS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-hosts";
    public static final String NOVA_OS_HOST_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-hosts/agno.groundwork.groundworkopensource.com";

    public static final String NOVA_SERVER_DIAGS_V2_URL = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/servers/%s/diagnostics";

    public static final String HYPERVISOR_STATISTICS = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-hypervisors/statistics";
    public static final String HYPERVISOR_SERVERS = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-hypervisors/%s/servers";
    public static final String HYPERVISOR_LIST = "http://agno.groundwork.groundworkopensource.com:8774/v2/%s/os-hypervisors";

    // Havana
    public static final String KEYSTONE_TOKEN_REQUEST_HAVANA = "{\"auth\":{\"passwordCredentials\":{\"username\": \"demo\", \"password\":\"55d794a346cf413a\"}, \"tenantName\":\"demo\"}}";
    // Mirantis
    public static final String KEYSTONE_TOKEN_REQUEST_MIRANTIS = "{\"auth\":{\"passwordCredentials\":{\"username\": \"admin\", \"password\":\"admin\"}, \"tenantName\":\"admin\"}}";
    // Amazon Juno
    public static final String KEYSTONE_TOKEN_REQUEST = "{\"auth\":{\"passwordCredentials\":{\"username\": \"demo\", \"password\":\"d7808e87038c4365\"}, \"tenantName\":\"demo\"}}";

    // admin password: 10e08681091c47e3

    // this TOKEN is only good for 24 hours
    public static final String TOKEN =  "MIINfwYJKoZIhvcNAQcCoIINcDCCDWwCAQExCTAHBgUrDgMCGjCCC9UGCSqGSIb3DQEHAaCCC8YEggvCeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxMy0xMi0yMFQyMjozNTo0MC43NjkyNjUiLCAiZXhwaXJlcyI6ICIyMDEzLTEyLTIxVDIyOjM1OjQwWiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogbnVsbCwgImVuYWJsZWQiOiB0cnVlLCAiaWQiOiAiOWYxMmU5MjAxY2E1NGVhNThiZjdiZGMxZjg0MTM5YzkiLCAibmFtZSI6ICJkZW1vIn19LCAic2VydmljZUNhdGFsb2ciOiBbeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc0L3YyLzlmMTJlOTIwMWNhNTRlYTU4YmY3YmRjMWY4NDEzOWM5IiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi85ZjEyZTkyMDFjYTU0ZWE1OGJmN2JkYzFmODQxMzljOSIsICJpZCI6ICIyM2Q3NTJlNjRhYTk0MjA4YWZhZDI4NDY3MDA3NDAzOSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi85ZjEyZTkyMDFjYTU0ZWE1OGJmN2JkYzFmODQxMzljOSJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJjb21wdXRlIiwgIm5hbWUiOiAibm92YSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjIvOWYxMmU5MjAxY2E1NGVhNThiZjdiZGMxZjg0MTM5YzkiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YyLzlmMTJlOTIwMWNhNTRlYTU4YmY3YmRjMWY4NDEzOWM5IiwgImlkIjogIjNiMmQ0NmIwMzA1YzQzNGFhMWM5MmQxMDc0NzQ4ZjA4IiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YyLzlmMTJlOTIwMWNhNTRlYTU4YmY3YmRjMWY4NDEzOWM5In1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogInZvbHVtZXYyIiwgIm5hbWUiOiAiY2luZGVyIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92MyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzQvdjMiLCAiaWQiOiAiNjM0ZTk2OGVlNGZlNGM2NGE0YjY5Y2Y4MzA1ZTU3NzciLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzQvdjMifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiY29tcHV0ZXYzIiwgIm5hbWUiOiAibm92YSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjMzMzMiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTozMzMzIiwgImlkIjogIjI0MWQyYjBlYjJhNDQ3YzJiOGU2ZTM2YzNiYmNiYzM2IiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTozMzMzIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogInMzIiwgIm5hbWUiOiAiczMifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo5MjkyIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTI5MiIsICJpZCI6ICIxZDRlZGE5ZjRiY2Y0OTk4OGMzNDEwMTQ0M2MxYTc3NiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTI5MiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJpbWFnZSIsICJuYW1lIjogImdsYW5jZSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjEvOWYxMmU5MjAxY2E1NGVhNThiZjdiZGMxZjg0MTM5YzkiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YxLzlmMTJlOTIwMWNhNTRlYTU4YmY3YmRjMWY4NDEzOWM5IiwgImlkIjogIjIyY2E1ZDg1Y2EzYzRjYzdiZmYxZTExN2RkZTg5ZTFiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YxLzlmMTJlOTIwMWNhNTRlYTU4YmY3YmRjMWY4NDEzOWM5In1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogInZvbHVtZSIsICJuYW1lIjogImNpbmRlciJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzMvc2VydmljZXMvQWRtaW4iLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4NzczL3NlcnZpY2VzL0Nsb3VkIiwgImlkIjogIjBjOWJlYzUxYTQ1NzQ0NTU4OGQxYzhjM2M0MWQyN2JkIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4NzczL3NlcnZpY2VzL0Nsb3VkIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImVjMiIsICJuYW1lIjogImVjMiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjM1MzU3L3YyLjAiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo1MDAwL3YyLjAiLCAiaWQiOiAiMzE1MWRjNWJjM2NlNDFmNzhlZjYxYzI4NWU1YTY0ZDUiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjUwMDAvdjIuMCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJpZGVudGl0eSIsICJuYW1lIjogImtleXN0b25lIn1dLCAidXNlciI6IHsidXNlcm5hbWUiOiAiYWRtaW4iLCAicm9sZXNfbGlua3MiOiBbXSwgImlkIjogIjJhMWQ0ZThjYmMxNTRiZTE4YTkyNzA0OTU1NDUyMzA4IiwgInJvbGVzIjogW3sibmFtZSI6ICJhZG1pbiJ9XSwgIm5hbWUiOiAiYWRtaW4ifSwgIm1ldGFkYXRhIjogeyJpc19hZG1pbiI6IDAsICJyb2xlcyI6IFsiOWJlYjQ5ZDhmYzc2NDFhYWExMDJlZDA5MTdmNDA1MzAiXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgMBVVuc2V0MQ4wDAYDVQQHDAVVbnNldDEOMAwGA1UECgwFVW5zZXQxGDAWBgNVBAMMD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQDKYCTI6LRaFpDZsTuNnrpCBiwzQJVxSweIf3oINfwjRUunUnvYU61pspcZ5kB9P-NJvWJJ+Y9oHLSek3sgIC3UCPRmQ7v79qh3z+Ik3+If5+3UMisiRqBYEOYvba5lbwsTnDbsk1axnLU0xrnwGxRyaPlNiF7-hARhghdsuH64ooniFWFqbcf3BDtGSZ9Ycmr7dz1rNh5320QBQaQjSL6zylw7opTsT4Kta0k3setejmoDoN79B8ujflenglAA12LW8faHJt79f37kZ1UVy-MT0RUzpfEO9QO1USZhsEQHwbnQny7mm8Yp786Iv1wqZY+Wp-8CnB0qLXbh4bdCJas+";
    public static final String KEYSTONE_ADMIN_TOKEN = "cb5934059e204423acebca462fc893a7";
    //public static final String TENANT_ID = "9f12e9201ca54ea58bf7bdc1f84139c9";
    //public static final String TENANT_NAME = "demo";

    public static final String ADMIN_TENANT_ID = "0afcf7f80e8149269b83af3a9b940839";
    public static final String ADMIN_TENANT_NAME = "admin";

    public static final String SERVICES_TENANT_ID = "53a78f73d5e74ce282871c273a8e8bab";
    public static final String SERVICES_TENANT_NAME = "services";

//    public static String TENANT_ID = ADMIN_TENANT_ID;
//    public static String TENANT_NAME = ADMIN_TENANT_NAME;

    //public static String TENANT_ID = "70fde053f5244d1c89b23ce7ce80c74a";

    public static String TENANT_ID = "743641cfa0714cf3adb62ba7bcc7fd7b"; //"6fd89a41705441a5b718aebe07a763d0";
    public static String TENANT_NAME = "admin";


    //public static String MYTOKEN = "MIIQRAYJKoZIhvcNAQcCoIIQNTCCEDECAQExCTAHBgUrDgMCGjCCDpoGCSqGSIb3DQEHAaCCDosEgg6HeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wNC0wOFQxODoxNDo0OS44NTkxMzMiLCAiZXhwaXJlcyI6ICIyMDE0LTA0LTA5VDE4OjE0OjQ5WiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogImRlZmF1bHQgdGVuYW50IiwgImVuYWJsZWQiOiB0cnVlLCAiaWQiOiAiNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQiLCAibmFtZSI6ICJkZW1vIn19LCAic2VydmljZUNhdGFsb2ciOiBbeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc0L3YyLzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJpZCI6ICI0NmEwMTgyMTVhZDk0ODc4OWE0ZTA3ZjZiMDgyNmUwNSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJjb21wdXRlIiwgIm5hbWUiOiAibm92YSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojk2OTYvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTY5Ni8iLCAiaWQiOiAiMzE3YjczNTllYmY2NGRlYmFjZWMwMmVkMGE3NTkxZWIiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojk2OTYvIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogIm5ldHdvcmsiLCAibmFtZSI6ICJuZXV0cm9uIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjIvNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQiLCAiaWQiOiAiM2M0MWFhODQzODQxNDk0MTgzNjhmNTM5YThkYjU4YTUiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjIvNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAidm9sdW1ldjIiLCAibmFtZSI6ICJjaW5kZXJfdjIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4MDgwIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MCIsICJpZCI6ICIyZjZjOTdhYjM0YzQ0MmRhYjZiOTAwMWQ1ZTIzOTBlMyIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJzMyIsICJuYW1lIjogInN3aWZ0X3MzIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTI5MiIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjkyOTIiLCAiaWQiOiAiM2MyZWY0Y2U3M2I2NDM4ZWJiNmM2MDJjYTliYzNiMTgiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjkyOTIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaW1hZ2UiLCAibmFtZSI6ICJnbGFuY2UifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc3IiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NyIsICJpZCI6ICIyODZlMGU5NDU2NTE0YTcwOTEwZDhhZTc5Y2RiMmE2NSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NyJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJtZXRlcmluZyIsICJuYW1lIjogImNlaWxvbWV0ZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YxLzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92MS82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJpZCI6ICIxNjJiNGM1OGViYmY0MDZhOThkNTJjZGI5OTAwM2RlNiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92MS82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJ2b2x1bWUiLCAibmFtZSI6ICJjaW5kZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4NzczL3NlcnZpY2VzL0FkbWluIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3My9zZXJ2aWNlcy9DbG91ZCIsICJpZCI6ICIyMWU0ZWRiZWRhMGU0YTAzYWU2M2JkZDUzNjhhZDE4YiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3My9zZXJ2aWNlcy9DbG91ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJlYzIiLCAibmFtZSI6ICJub3ZhX2VjMiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjgwODAvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MC92MS9BVVRIXzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgImlkIjogIjE2ZDg0MTk0NGMyMzQzOTY5NmVkZTE4MjMzZTRiMjdjIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4MDgwL3YxL0FVVEhfNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAib2JqZWN0LXN0b3JlIiwgIm5hbWUiOiAic3dpZnQifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTozNTM1Ny92Mi4wIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6NTAwMC92Mi4wIiwgImlkIjogIjFmOWJiNTg1YjUyNTRhMzk4YmQwZTgyZTNmMGJjZDNiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo1MDAwL3YyLjAifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaWRlbnRpdHkiLCAibmFtZSI6ICJrZXlzdG9uZSJ9XSwgInVzZXIiOiB7InVzZXJuYW1lIjogImRlbW8iLCAicm9sZXNfbGlua3MiOiBbXSwgImlkIjogImNmMWVjNTdlZjVlOTQyZjY5ZmY1OTUwODhjNzQzZmFjIiwgInJvbGVzIjogW3sibmFtZSI6ICJhZG1pbiJ9LCB7Im5hbWUiOiAiTWVtYmVyIn1dLCAibmFtZSI6ICJkZW1vIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbImE4NmUyZmE2NjQxODQwMmFhYmRiNTYzODlhNDExZGMxIiwgIjE5YWMyZjVlOGY5YzRmMzA5ZWZiOGM5YjExZWY4MDg4Il19fX0xggGBMIIBfQIBATBcMFcxCzAJBgNVBAYTAlVTMQ4wDAYDVQQIDAVVbnNldDEOMAwGA1UEBwwFVW5zZXQxDjAMBgNVBAoMBVVuc2V0MRgwFgYDVQQDDA93d3cuZXhhbXBsZS5jb20CAQEwBwYFKw4DAhowDQYJKoZIhvcNAQEBBQAEggEAeNygbtqVRdrBf2+z0j11zjVVOCbwizJaDXJGZ2z0EtmKwSImaW5CPHGPsr4tKJ99m6HBwATJoDM37yNsCOX8wX38-4N7F+lCYnW1UGFUFyZd4BAQWmudeYJ73HGMxXg1vhGS3-j+6a4T9IWIiqGmb-+4KYZCxOeY2tC8kpEQWnyqGodRtpUXC3X1W4nCMlHblKsmFuEZszJeVodBIjktGDoxIdxlQksRCHiG9Ow+ukFvkR5sIB4ZK-xzrKkRz+ZfyIr9QqpiXJAuM0h-zsXi-uvDTEzMSKGYcU6j6XbqLqncEmgDt9bN2cdNQ6scdy-giSzME+eyzDaKFxyIhZKDyQ==";
    public static String MYTOKEN = "MIIQRAYJKoZIhvcNAQcCoIIQNTCCEDECAQExCTAHBgUrDgMCGjCCDpoGCSqGSIb3DQEHAaCCDosEgg6HeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wNC0wOVQxODoyMzowOS42NjY5MTMiLCAiZXhwaXJlcyI6ICIyMDE0LTA0LTEwVDE4OjIzOjA5WiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogImRlZmF1bHQgdGVuYW50IiwgImVuYWJsZWQiOiB0cnVlLCAiaWQiOiAiNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQiLCAibmFtZSI6ICJkZW1vIn19LCAic2VydmljZUNhdGFsb2ciOiBbeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc0L3YyLzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJpZCI6ICI0NmEwMTgyMTVhZDk0ODc4OWE0ZTA3ZjZiMDgyNmUwNSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NC92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJjb21wdXRlIiwgIm5hbWUiOiAibm92YSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojk2OTYvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTY5Ni8iLCAiaWQiOiAiMzE3YjczNTllYmY2NGRlYmFjZWMwMmVkMGE3NTkxZWIiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojk2OTYvIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogIm5ldHdvcmsiLCAibmFtZSI6ICJuZXV0cm9uIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92Mi82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjIvNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQiLCAiaWQiOiAiM2M0MWFhODQzODQxNDk0MTgzNjhmNTM5YThkYjU4YTUiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5Ojg3NzYvdjIvNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAidm9sdW1ldjIiLCAibmFtZSI6ICJjaW5kZXJfdjIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4MDgwIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MCIsICJpZCI6ICIyZjZjOTdhYjM0YzQ0MmRhYjZiOTAwMWQ1ZTIzOTBlMyIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJzMyIsICJuYW1lIjogInN3aWZ0X3MzIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6OTI5MiIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjkyOTIiLCAiaWQiOiAiM2MyZWY0Y2U3M2I2NDM4ZWJiNmM2MDJjYTliYzNiMTgiLCAicHVibGljVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjkyOTIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaW1hZ2UiLCAibmFtZSI6ICJnbGFuY2UifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc3IiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NyIsICJpZCI6ICIyODZlMGU5NDU2NTE0YTcwOTEwZDhhZTc5Y2RiMmE2NSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3NyJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJtZXRlcmluZyIsICJuYW1lIjogImNlaWxvbWV0ZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4Nzc2L3YxLzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92MS82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCIsICJpZCI6ICIxNjJiNGM1OGViYmY0MDZhOThkNTJjZGI5OTAwM2RlNiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3Ni92MS82ODE1OTVmNDFhZjE0MzdmODcyZGQ1YTdiMDNlNTM2ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJ2b2x1bWUiLCAibmFtZSI6ICJjaW5kZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4NzczL3NlcnZpY2VzL0FkbWluIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3My9zZXJ2aWNlcy9DbG91ZCIsICJpZCI6ICIyMWU0ZWRiZWRhMGU0YTAzYWU2M2JkZDUzNjhhZDE4YiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODc3My9zZXJ2aWNlcy9DbG91ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJlYzIiLCAibmFtZSI6ICJub3ZhX2VjMiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xNzIuMjguMTExLjM5OjgwODAvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6ODA4MC92MS9BVVRIXzY4MTU5NWY0MWFmMTQzN2Y4NzJkZDVhN2IwM2U1MzZkIiwgImlkIjogIjE2ZDg0MTk0NGMyMzQzOTY5NmVkZTE4MjMzZTRiMjdjIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo4MDgwL3YxL0FVVEhfNjgxNTk1ZjQxYWYxNDM3Zjg3MmRkNWE3YjAzZTUzNmQifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAib2JqZWN0LXN0b3JlIiwgIm5hbWUiOiAic3dpZnQifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTozNTM1Ny92Mi4wIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE3Mi4yOC4xMTEuMzk6NTAwMC92Mi4wIiwgImlkIjogIjFmOWJiNTg1YjUyNTRhMzk4YmQwZTgyZTNmMGJjZDNiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTcyLjI4LjExMS4zOTo1MDAwL3YyLjAifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaWRlbnRpdHkiLCAibmFtZSI6ICJrZXlzdG9uZSJ9XSwgInVzZXIiOiB7InVzZXJuYW1lIjogImRlbW8iLCAicm9sZXNfbGlua3MiOiBbXSwgImlkIjogImNmMWVjNTdlZjVlOTQyZjY5ZmY1OTUwODhjNzQzZmFjIiwgInJvbGVzIjogW3sibmFtZSI6ICJhZG1pbiJ9LCB7Im5hbWUiOiAiTWVtYmVyIn1dLCAibmFtZSI6ICJkZW1vIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbImE4NmUyZmE2NjQxODQwMmFhYmRiNTYzODlhNDExZGMxIiwgIjE5YWMyZjVlOGY5YzRmMzA5ZWZiOGM5YjExZWY4MDg4Il19fX0xggGBMIIBfQIBATBcMFcxCzAJBgNVBAYTAlVTMQ4wDAYDVQQIDAVVbnNldDEOMAwGA1UEBwwFVW5zZXQxDjAMBgNVBAoMBVVuc2V0MRgwFgYDVQQDDA93d3cuZXhhbXBsZS5jb20CAQEwBwYFKw4DAhowDQYJKoZIhvcNAQEBBQAEggEArHGEe7GcKwC+o3-2fLRQ9fsuToirRWlGyQ1hJmIxHAZgpeR251EJvJ14bDbVDlm2gxRlxFM6QwpOpUGqI1ZPv6uX2qJZzw+wqetMv2VYlLi6hGFsbWBZ93TaK6owIpMaiS9ojwO-HXPEJ3EYrXObd+so3K2rPrBN9glfA6pJOUR220M4MfJJla65VtK2h0NL2uztsNckGVfXosoqtbuqoenFkjNuYp1uinlOC+mf6P7s-LMIbLMN9Jg7uw8MrDx6hmaOwlwNCmdyjbagjnK1wxbn-D4jJNkjSAYzn2qfDabC1fV2sQfyU2TfmbtHKOep1xEUZZx+6JGLB4NbPpEHeQ==";

    @Test
    public void openStackTest() throws Exception {

        boolean isTokenValid = false;
        TenantInfo tenant = new TenantInfo(MYTOKEN, TENANT_ID, TENANT_NAME);
        //TenantInfo tenant = new TenantInfo(KEYSTONE_ADMIN_TOKEN, TENANT_ID, TENANT_NAME);
        //TenantInfo tenant = new TenantInfo(TOKEN, TENANT_ID, TENANT_NAME);
        if (!isTokenValid) {
            tenant = retrieveNewKeystoneToken();
            System.out.println("[" + tenant.accessToken + "]");
        }
        listTenants(tenant);
        listApis(tenant);
        getNovaHosts(tenant);
//        getNovaQuotas(tenant);
//
//        String tenants = listTenants(tenant);
//        System.out.println("tenant : " + tenants);

        List<HypervisorInfo> hypervisors = listNovaHypervisors(tenant);
        for (HypervisorInfo hypervisor : hypervisors) {
            System.out.format("hypervisor : %s : %s\n", hypervisor.id, hypervisor.name);
            //String info = getNovaServerDetails(tenant, server);
            List<ServerLinkInfo> links = listHypervisorServerLinks(tenant, hypervisor.name);
            for (ServerLinkInfo link : links) {
                String info = getNovaServerDiagnostics(tenant, link);
                System.out.println(info);
            }
        }

    }

    public void listApis(TenantInfo tenant) throws Exception {
        //ClientRequest request = new ClientRequest("http://agno:8774/v3/");
        ClientRequest request = new ClientRequest("http://agno.groundwork.groundworkopensource.com:8774/v2/");
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            System.out.println(response.getEntity());
            return;
        }
        System.out.println("entity = " + response.getEntity(String.class));
        //throw new IOException("Failed to get API list: " + response.getResponseStatus());
    }


    public String getNovaQuotas(TenantInfo tenant) throws Exception {
        String url = String.format(NOVA_SERVER_QUOTAS_V2_URL, tenant.tenantId);
        ClientRequest request = new ClientRequest(url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            System.out.println("quotas " + payload);
            return payload;
        }
        else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
            System.out.println("... getNovaHosts, no hosts found");
            return "";
        }
        System.out.println("entity = " + response.getEntity(String.class));
        throw new IOException("Failed to connect to Nova Quotas: " + response.getResponseStatus());
    }

    public String getNovaHosts(TenantInfo tenant) throws Exception {
        String url = String.format(NOVA_OS_HOST_V2_URL, tenant.tenantId);
        ClientRequest request = new ClientRequest(url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            System.out.println("os hosts " + payload);
            return payload;
        }
        else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
            System.out.println("... getNovaHosts, no hosts found");
            return "";
        }
        throw new IOException("Failed to connect to OS Hosts: " + response.getResponseStatus());
    }


    public String getNovaServerDetails(TenantInfo tenant, HypervisorInfo info) throws Exception {
        String url = String.format(NOVA_SERVER_DETAILS_V2_URL, info.id);
        ClientRequest request = new ClientRequest(url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            return payload;
        }
        throw new IOException("Failed to connect to Nova Server Info: " + response.getResponseStatus());
    }

    public String getNovaServerDiagnostics(TenantInfo tenant, ServerLinkInfo link) throws Exception {    // pass in server(VM), not hypervisor
        String url = String.format(NOVA_SERVER_DIAGS_V2_URL, tenant.tenantId, link.uuid);
        ClientRequest request = new ClientRequest(url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            return payload;
        }
        return response.getEntity();
        //throw new IOException("Failed to connect to Nova Server Info: " + response.getResponseStatus());
    }

    public List<HypervisorInfo> listNovaServers(TenantInfo tenant) throws Exception {
        String url = String.format(NOVA_SERVERS_V2_URL, tenant.tenantId);
        ClientRequest request = new ClientRequest(url); //NOVA_SERVERS_V3_URL); //url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject object = reader.readObject();
            JsonArray servers = object.getJsonArray("servers");
            List<HypervisorInfo> result = new ArrayList<>();
            for (int ix = 0; ix < servers.size(); ix++) {
                JsonObject server = servers.getJsonObject(ix);
                HypervisorInfo info = new HypervisorInfo(server.getString("id"), server.getString("name"),
                        server.getString("status"), server.getString("state"));
                // TODO: retrieve links
                result.add(info);
            }
            return result;
        }
        else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
            System.out.println("... list sNova Servers, no servers found");
            return new ArrayList<HypervisorInfo>();
        }
        System.out.println("entity = " + response.getEntity(String.class));
        throw new IOException("Failed to connect to Nova Servers: " + response.getResponseStatus());
    }

    public List<HypervisorInfo> listNovaHypervisors(TenantInfo tenant) throws Exception {
        String url = String.format(HYPERVISOR_LIST, tenant.tenantId);
        ClientRequest request = new ClientRequest(url); //NOVA_SERVERS_V3_URL); //url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject object = reader.readObject();
            JsonArray servers = object.getJsonArray("hypervisors");
            List<HypervisorInfo> result = new ArrayList<>();
            for (int ix = 0; ix < servers.size(); ix++) {
                JsonObject server = servers.getJsonObject(ix);
                String name = server.getString("hypervisor_hostname");
                String status = server.getString("status");
                String state = server.getString("state");
                if (status.equals("enabled") && state.equals("up")) {
                    HypervisorInfo info = new HypervisorInfo(name, name, HypervisorState.up, HypervisorStatus.enabled);
                    result.add(info);
                }
            }
            return result;
        }
        else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
            System.out.println("... list sNova Hypervisors, no servers found");
            return new ArrayList<HypervisorInfo>();
        }
        System.out.println("entity = " + response.getEntity(String.class));
        throw new IOException("Failed to connect to Nova Hypervisors: " + response.getResponseStatus());
    }

    public List<ServerLinkInfo> listHypervisorServerLinks(TenantInfo tenant, String hypervisorName) throws Exception {
        String url = String.format(HYPERVISOR_SERVERS, tenant.tenantId, hypervisorName);
        ClientRequest request = new ClientRequest(url); //NOVA_SERVERS_V3_URL); //url);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject object = reader.readObject();
            JsonArray hypervisors = object.getJsonArray("hypervisors");
            List<ServerLinkInfo> result = new ArrayList<>();
            if (hypervisors != null && hypervisors.size() > 0) {
                JsonObject hypervisor = hypervisors.getJsonObject(0);
                if (hypervisor != null) {
                    JsonArray servers = hypervisor.getJsonArray("servers");
                    if (servers != null) {
                        for (int ix = 0; ix < servers.size(); ix++) {
                            JsonObject server = servers.getJsonObject(ix);
                            ServerLinkInfo link = new ServerLinkInfo(server.getString("uuid"), server.getString("name"));
                            result.add(link);
                        }
                    }
                }
            }
            return result;
        }
        else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
            System.out.println("... list sNova Servers, no servers found");
            return new ArrayList<ServerLinkInfo>();
        }
        System.out.println("entity = " + response.getEntity(String.class));
        throw new IOException("Failed to connect to Nova Servers: " + response.getResponseStatus());
    }


    public String listTenants(TenantInfo tenant) throws Exception {
        ClientRequest request = new ClientRequest(KEYSTONE_TENANTS_URL);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String entity = response.getEntity(String.class);
            System.out.println("entity = " + entity);
            return entity;
        }
        System.out.println("entity = " + response.getEntity(String.class));
        throw new IOException("Failed to connect to Keystone Tenants: " + response.getResponseStatus());
    }



    public TenantInfo retrieveNewKeystoneToken() throws Exception {

        ClientRequest request = new ClientRequest(KEYSTONE_TOKEN_URL);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON, KEYSTONE_TOKEN_REQUEST);
        ClientResponse<String> response = request.post();
        String payload;
        if (response.getResponseStatus() == Response.Status.OK) {
            payload = response.getEntity(String.class);
        }
        else {
            throw new IOException("Failed to connect to Keystone Token: " + response.getResponseStatus());
        }
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        TenantInfo tenant = new TenantInfo();
        tenant.accessToken = object.getJsonObject("access").getJsonObject("token").getJsonString("id").getString(); //.toString();
        tenant.tenantId = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("id").getString();
        tenant.tenantName = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("name").getString();
        reader.close();
        return tenant;
    }

    @Test
    public void jsonTest() throws Exception {
        Reader fileReader = new FileReader("./src/test/testdata/openstack-accessToken.json");
        assertNotNull(fileReader);
        JsonReader reader = Json.createReader(fileReader);
        assertNotNull(reader);
        JsonObject object = reader.readObject();
        JsonString token = object.getJsonObject("access").getJsonObject("token").getJsonString("id");
        System.out.println("accessToken = " + token);
        reader.close();
        fileReader.close();
    }
}
