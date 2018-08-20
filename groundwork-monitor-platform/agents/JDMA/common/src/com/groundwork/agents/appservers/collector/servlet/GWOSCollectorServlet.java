package com.groundwork.agents.appservers.collector.servlet;

import com.groundwork.agents.appservers.collector.api.GWOSCollectorService;
import com.groundwork.agents.appservers.utils.PropertyFileBuilder;
import com.groundwork.agents.appservers.utils.StatUtils;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Servlet implementation class GWOSCollectorServlet
 */
public abstract class GWOSCollectorServlet extends HttpServlet {
    private static org.apache.log4j.Logger log = Logger.getLogger(GWOSCollectorServlet.class);
    //private static JDMALog log = new JDMALog();


    protected GWOSCollectorService gwosService = null;
    private static final long serialVersionUID = 1L;

    protected String[] staticProps = null;

    protected String[] defaultCheckList = null;

    private String appServerName = null;

    private String properties = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GWOSCollectorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see Servlet#init(ServletConfig)
     */
    public void init(ServletConfig config, String[] staticProps,
                     String[] defaultCheckList, String appServerName, String properties,
                     GWOSCollectorService gwosService) throws ServletException {
        super.init(config);
        this.staticProps = staticProps;
        this.defaultCheckList = defaultCheckList;
        this.appServerName = appServerName;
        this.properties = properties;
        if (gwosService != null) {
            log.info("Starting GWOS MBean Collector Service....");
            gwosService.start();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        log.error("Get not supported for JDMA");

    }

    /**
     * Perform post
     *
     * @param userName
     * @param password
     * @param appServerName
     * @param mbeanAtts
     * @param gwServerName
     * @return
     */
    private String performPost(String userName, String password,
                               String appServerName, String mbeanAtts, String gwServerName, boolean isGWServerSecured) {
        String protocol = "http";
        if (isGWServerSecured)
            protocol = "https";
        String response = null;
        DataOutputStream out = null;
        try {
            // connect
            URI uri = new URI(
                    protocol,
                    null,
                    "//"
                            + gwServerName
                            + "/api/uploadProfiles/upload",
                    null, null);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            TLSV12ClientConfiguration.configure(connection);
            if (log.isDebugEnabled()) {
                log.debug(url.toString());
            }

            // initialize the connection
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);

            connection.setRequestProperty("Content-type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Connection", "Keep-Alive");

            out = new DataOutputStream(connection.getOutputStream());

            out
                    .writeBytes(("username=" + userName + "&password="
                            + password + "&appServerName=" + appServerName
                            + "&mbeanAtts=" + URLEncoder.encode(mbeanAtts,
                            "UTF-8")));
            out.flush();
            out.close();
            BufferedReader inStream = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str = null;
            while (null != ((str = inStream.readLine()))) {
                sb.append(str);
            } // end while
            response = sb.toString();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Got Exception: " + e);
            response = "<code>6</code><message>" + e.getMessage() + " or Invalid GroundWork Server Name</message>";
        }

        return response;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        Object param = request.getParameter("action");
        RequestDispatcher rd = null;
        if (param != null) {
            String action = (String) param;

            if (action != null && !action.equalsIgnoreCase("")
                    && action.equalsIgnoreCase("create_from_ui_index_page")) {
                Object newButton = request.getParameter("new");
                if (newButton != null)
                    rd = getServletContext().getRequestDispatcher(
                            "/newConnectionWizard.jsp");
                else {
                    Properties prop = StatUtils.readProperties(properties);
                    if (prop != null) {
                        request.getSession().setAttribute("currentProperties", prop);
                    }
                    rd = getServletContext().getRequestDispatcher(
                            "/modifyConnectionWizard.jsp");
                }
            } // end if

            if (action != null && !action.equalsIgnoreCase("")
                    && action.equalsIgnoreCase("create_from_ui_conn_page")) {
                Object startDiscButton = request.getParameter("start");
                if (startDiscButton != null) {
                    Properties prop = createPropFileFromParams(request);
                    boolean testConnSuccess = gwosService
                            .testConnection(prop);
                    if (testConnSuccess) {
                        request.getSession().setAttribute("statProps", prop);
                        HashMap<String, Object> collector = gwosService
                                .autoDiscoverComponents(prop);
                        if (collector != null) {
                            List<String> myList = new ArrayList<String>(
                                    collector.keySet());
                            Collections.sort(myList);
                            request.setAttribute("services", myList);
                            request.setAttribute("defaultCheckList",
                                    defaultCheckList);
                        } // end if

                        rd = getServletContext().getRequestDispatcher(
                                "/selectServices.jsp");
                    } else {
                        request.setAttribute("message", "Connection Failed. Invalid connection parameters!");
                        request.setAttribute("styleClass", "error");
                        rd = getServletContext().getRequestDispatcher(
                                "/newConnectionWizard.jsp");
                    }
                } else {
                    Properties props = createPropFileFromParams(request);
                    boolean testConnSuccess = gwosService.testConnection(props);
                    if (testConnSuccess) {
                        request.setAttribute("message", "Connection Successful");
                        request.setAttribute("styleClass", "success");
                    }
                    else {
                        String message = props.getProperty(GWOSCollectorService.ERROR_MESSAGE_PROP, null);
                        message = (message == null || message.equals("null")) ? "Connection Failed" : "Connection Failed: " + message;
                        request.setAttribute("message", message);
                        request.setAttribute("styleClass", "error");
                        props.remove(GWOSCollectorService.ERROR_MESSAGE_PROP);
                    }

                    rd = getServletContext().getRequestDispatcher(
                            "/newConnectionWizard.jsp");
                }
            }
            if (action != null && !action.equalsIgnoreCase("")
                    && action.equalsIgnoreCase("create_from_ui_select_page")) {
                String selectedServices = request.getParameter("selectedServices");
                List<String> services = Arrays.asList(selectedServices.split(","));
                request.getSession().setAttribute("selectedComponents",
                        services);
                if (log.isDebugEnabled()) {
                    log.debug("# of selected components is : " + services.size());
                }
                rd = getServletContext().getRequestDispatcher(
                        "/assignThresholds.jsp");
            }
            if (action != null && !action.equalsIgnoreCase("")
                    && action.equalsIgnoreCase("create_from_ui_assign_page")) {

                // Alias
                String[] aliasArr = request.getParameterValues("alias");
                List<String> alias = Arrays.asList(aliasArr);

                request.getSession().setAttribute("selectedAliases", alias);

                // Critical threshold

                String[] critThresholdArr = request
                        .getParameterValues("critThreshold");
                List<String> critThresholds = Arrays.asList(critThresholdArr);

                request.getSession().setAttribute("selectedCritThresholds",
                        critThresholds);

                // Warning Threshold
                String[] warnThresholdArr = request
                        .getParameterValues("warnThreshold");
                List<String> warnThresholds = Arrays.asList(warnThresholdArr);

                request.getSession().setAttribute("selectedWarnThresholds",
                        warnThresholds);
                Properties prop = (Properties) request.getSession().getAttribute("statProps");
                Boolean exportProfile = (Boolean) prop.get("exportProfile");
                // Since boolean type property is not being writtine to the xml, convert to string while writing.
                if (exportProfile != null)
                    prop.setProperty("exportProfile", exportProfile.toString());
                if (exportProfile != null) {
                    if (exportProfile)
                        rd = getServletContext().getRequestDispatcher(
                                "/uploadProfile.jsp");
                    else {
                        this.createPropertyFile(request, "/tmp");
                        request.setAttribute("message", "No Profiles created on groundwork server. gwos_" + appServerName + ".xml file created on /tmp folder. Move the file to appserver classpath.");
                        rd = getServletContext().getRequestDispatcher(
                                "/confirm.jsp");
                    }
                } else {
                    rd = getServletContext().getRequestDispatcher(
                            "/uploadProfile.jsp");
                }
            } // end if
            if (action != null && !action.equalsIgnoreCase("")
                    && action.equalsIgnoreCase("create_from_ui_export_page")) {
                String serverName = request.getParameter("serverName");
                String userName = request.getParameter("userName");
                String password = request.getParameter("password");
                String propFile = request.getParameter("propFile");
                String sslEnabled = request.getParameter("sslEnabled");
                boolean isGWServerSecured = false;
                if (sslEnabled != null && sslEnabled.equals("on"))
                    isGWServerSecured = true;
                Object selectedCompObj = request.getSession().getAttribute(
                        "selectedComponents");
                Object selectedAliases = request.getSession().getAttribute(
                        "selectedAliases");
                if (selectedCompObj != null && selectedAliases != null) {
                    List<String> comps = (List<String>) selectedCompObj;

                    List<String> aliases = (List<String>) selectedAliases;

                    StringBuffer sb = new StringBuffer();
                    for (String alias : aliases) {
                        sb.append(alias);
                        sb.append(",");
                    } // end if
                    String mbeanAtts = sb.toString().substring(0,
                            sb.length() - 1);
                    String responseString = this.performPost(userName,
                            password, appServerName, mbeanAtts, serverName, isGWServerSecured);
                    log.debug(responseString);

                    int startIndex = responseString.indexOf("<message>");
                    int endIndex = responseString.indexOf("</message>");
                    String message = responseString.substring(startIndex + 9,
                            endIndex);

                    int statusBegIndex = responseString.indexOf("<code>");
                    int statusEndIndex = responseString.indexOf("</code>");
                    int code = Integer.parseInt(responseString.substring(
                            statusBegIndex + 6, statusEndIndex));
                    request.setAttribute("message", message);
                    if (code == 0) {


                        this.createPropertyFile(request, propFile);
                        rd = getServletContext().getRequestDispatcher(
                                "/confirm.jsp");
                    } else
                        rd = getServletContext().getRequestDispatcher(
                                "/uploadProfile.jsp");
                } // end if

            }
            if (rd != null) {
                rd.forward(request, response);
            } // end if
        } else {
            rd = getServletContext().getRequestDispatcher("/index.html");
            rd.forward(request, response);
        } // end if
    }


    /**
     * Helper to create property file
     *
     */
    private void createPropertyFile(HttpServletRequest request, String propFile) {
        Object selectedCompObj = request.getSession().getAttribute(
                "selectedComponents");
        Object selectedAliases = request.getSession().getAttribute(
                "selectedAliases");
        if (selectedCompObj != null && selectedAliases != null) {
            List<String> comps = (List<String>) selectedCompObj;

            List<String> aliases = (List<String>) selectedAliases;

            Object selectedWarningObj = request.getSession()
                    .getAttribute("selectedWarnThresholds");
            Object selectedCritThresholds = request.getSession()
                    .getAttribute("selectedCritThresholds");
            List<String> warnings = (List<String>) selectedWarningObj;
            List<String> criticals = (List<String>) selectedCritThresholds;
            Properties statProps = (Properties) request.getSession().getAttribute("statProps");

            if (propFile != null && !propFile.endsWith("/"))
                propFile = propFile + "/";
            PropertyFileBuilder propBuilder = new PropertyFileBuilder(propFile,
                    appServerName, properties, statProps);
            propBuilder.build(comps, aliases, warnings, criticals);
        }
    }

    /**
     * @see Servlet#destroy()
     */
    public void destroy() {
        super.destroy();
        log.info("Shuting down GWOS MBean Collector Service....");
        gwosService.shutdown();
    }

    public abstract Properties createPropFileFromParams(
            HttpServletRequest request);


}
