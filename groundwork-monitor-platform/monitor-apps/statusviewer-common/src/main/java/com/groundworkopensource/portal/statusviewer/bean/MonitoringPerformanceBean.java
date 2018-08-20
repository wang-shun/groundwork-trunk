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

package com.groundworkopensource.portal.statusviewer.bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.listener.NagiosPerformanceSubscriber;
import com.icesoft.faces.async.render.SessionRenderer;

/**
 * Class denoting the backing bean for the monitoring performance portlet.
 * 
 * @author mridu_narang
 */

public class MonitoringPerformanceBean extends NagiosPerformanceSubscriber
        implements Serializable {

    /** serialVersionUID. */
    private static final long serialVersionUID = 992111034637334843L;

    // /** Purposely added for debugging purpose. TODO Remove afterwards. */
    // private String myName = null;

    /** Logger. */
    private static Logger logger = Logger
            .getLogger(MonitoringPerformanceBean.class.getName());

    // CHART BYTE ARRAYS
    /** Byte array to store Latency Chart Image. */
    private byte[] latencyChartImage;

    /** Byte array to store Execution Chart Image. */
    private byte[] executionChartImage;

    /** Byte array to store Service Checks Chart Image. */
    private byte[] checksChartImage;

    // FLOAT VALUES FOR CHARTS
    /** Average value for dataset - Service Latency. */
    private float serviceLatencyAverage;

    /** Average value for dataset - Service Execution. */
    private float serviceExecutionAverage;

    /** Maximum value for dataset - Service Execution. */
    private float serviceExecutionMaximum;

    /** Average value for dataset - Service checks. */
    private float serviceChecksAverage;

    /** XML Document. */
    private Document document;

    // TABLE FIELDS
    /**
     * List of objects where each object has - name_of_check, min_val, avg_val,
     * max_val for table 1 - Statistics.
     */
    private List<PerformanceStatisticsBean> statisticChecks = new ArrayList<PerformanceStatisticsBean>();

    /**
     * List of objects where each object has - name_of_check, min_val, avg_val,
     * max_val for table 2 - Service Checks.
     */
    private List<ServiceChecksBean> serviceChecks = new ArrayList<ServiceChecksBean>();

    // DIAL CHART CONSTANTS - DISPLAY PROPERTIES CONSTANTS
    /** The Constant WIDTH_RADIUS. */
    private static final double WIDTH_RADIUS = 0.01;

    /** The Constant CAP_RADIUS. */
    private static final double CAP_RADIUS = 0.02;

    /** The Constant ANNOTATION_RADIUS. */
    private static final double ANNOTATION_RADIUS = 0.65;

    /** The Constant FONT_12. */
    private static final int FONT_12 = 12;

    /** The Constant TICK_RADIUS. */
    private static final double TICK_RADIUS = 0.91;

    /** The Constant TICK_LABEL_OFFSET. */
    private static final double TICK_LABEL_OFFSET = 0.22;

    /** The Constant ANGLE_NEG_135. */
    private static final int ANGLE_NEG_135 = -135;

    /** The Constant ANGLE_NEG_180. */
    private static final int ANGLE_NEG_180 = -180;

    /** The Constant ANGLE_NEG_45. */
    private static final int ANGLE_NEG_45 = -45;

    /** The Constant ANGLE_NEG_270. */
    private static final int ANGLE_NEG_270 = -270;

    /** The Constant VAL_150. */
    private static final int VAL_150 = 150;

    /** The Constant SCALE_TICK_RADIUS. */
    private static final double SCALE_TICK_RADIUS = 0.9;

    /** The Constant MAJOR_TICK_LENGTH. */
    private static final double MAJOR_TICK_LENGTH = 0.015;

    /** The Constant TICK_LABEL_OFFSET_SCALE1. */
    private static final double TICK_LABEL_OFFSET_SCALE1 = 0.25;

    /** The Constant VAL_600. */
    private static final int VAL_600 = 600;

    /** The Constant VAL_75. */
    private static final int VAL_75 = 75;

    /** The Constant ANNOTATION_FONT. */
    private static final int ANNOTATION_FONT = 12;

    /** DIAL_TEXT_ANNOTATION "per minute". */
    private static final String DIAL_TEXT_ANNOTATION = "per minute";

    /** The Constant AVG_SECONDS. */
    private static final String AVG_SECONDS = "avg seconds";

    /** FONT_DIALOG. */
    private static final String FONT_DIALOG = "Dialog";

    // XML CONSTANTS - BASED ON XML SCHEMA
    /** The Constant SERVICE_CHECK. */
    private static final String SERVICE_CHECK = "ServiceCheck";

    /** The Constant ACTIVE. */
    private static final String ACTIVE = "Active";

    /** The Constant PASSIVE. */
    private static final String PASSIVE = "Passive";

    /** The Constant MIN1. */
    private static final String MIN1 = "Min1";

    /** The Constant MIN5. */
    private static final String MIN5 = "Min5";

    /** The Constant MIN15. */
    private static final String MIN15 = "Min15";

    /** The Constant ACTIVE_SERVICE_CHECKS. */
    private static final String ACTIVE_SERVICE_CHECKS = "Active Service Checks";

    /** The Constant PASSIVE_SERVICE_CHECKS. */
    private static final String PASSIVE_SERVICE_CHECKS = "Passive Service Checks";

    /** The Constant STATISTICS. */
    private static final String STATISTICS = "Statistics";

    /** The Constant SERVICE_CHECK_EXECUTIION_TIME. */
    private static final String SERVICE_CHECK_EXECUTIION_TIME = "ServiceCheckExecutionTime";

    /** The Constant CHECK_EXECUTION_TIME. */
    private static final String CHECK_EXECUTION_TIME = "HostCheckExecutionTime";

    /** The Constant CHECK_LATENCY. */
    private static final String CHECK_LATENCY = "Service Check Latency";

    /** The Constant SERVICE_CHECK_LATENCY. */
    private static final String SERVICE_CHECK_LATENCY = "ServiceCheckLatency";

    /** The Constant HOST_CHECK_EXECUTION_TIME. */
    private static final String HOST_CHECK_EXECUTION_TIME = "Host Check Execution Time";

    /** The Constant SERVICE_CHECK_EXECUTION_TIME. */
    private static final String SERVICE_CHECK_EXECUTION_TIME = "Service Check Execution Time";

    /** The Constant MAX. */
    private static final String MAX = "Max";

    /** The Constant AVERAGE. */
    private static final String AVERAGE = "Average";

    /** The Constant MIN. */
    private static final String MIN = "Min";

    /** The Constant UTF_8. */
    private static final String UTF_8 = "UTF-8";

    /** Annotation for service execution chart - Unit in seconds. */
    private static final String SECONDS = "seconds";

    /** The Constant VAL_20. */
    private static final double VAL_20 = 20;

    /** The Constant VAL_120. */
    private static final double VAL_120 = 120;

    /** The Constant VAL_30. */
    private static final int VAL_30 = 30;

    /** The Constant VAL_5250. */
    private static final double VAL_5250 = 5250;

    /** The Constant VAL_750. */
    private static final double VAL_750 = 750;

    /** The Constant FONT_11. */
    private static final int FONT_11 = 11;

    /** The Constant FONT_10. */
    private static final int FONT_10 = 10;

    /** The Constant TICK_STROKE_4F. */
    private static final float TICK_STROKE_4F = 4f;

    // XML Tag Strings for 3 parameters
    /** The tag1 string. */
    private String tag1String = "";

    /** The tag2 string. */
    private String tag2String = "";

    /** The tag3 string. */
    private String tag3String = "";

    /** Error boolean to set if error occurred. */
    private boolean error = false;

    /** Error message to show on UI. */
    private String errorMessage;

    /**
     * info boolean to set if information occurred
     */
    private boolean info = false;

    /**
     * boolean variable message set true when display any type of messages
     * (Error,info or warning) in UI
     */
    private boolean message = false;

    /**
     * information message to show on UI
     */
    private String infoMessage;

    /** The Constant ZERO. */
    private static final float ZERO = 0.0f;

    /** The Constant ZERO_S. */
    private static final String ZERO_S = "0.0";

    /** The Constant darkOrange. */
    private static final Color darkOrange = new Color(255, 153,  51);

    /** The Constant lightOrange. */
    private static final Color lightOrange = new Color(251, 193, 141);

    /**
     * UserExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Default constructor.
     */
    public MonitoringPerformanceBean() {
        logger
                .debug("MonitoringPerformanceBean(): Initializing monitoring performance backing bean");

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // check if user has permission to view this portlet as per the extended
        // roles defined for him
        if (!checkAgainstExtendedRoles()) {
            return;
        }
        // Error in NagiosPerformanceSubscriber reflected in bean
        // this.error = super.isInitError();
        // myName = "MonitoringPerformance_"
        // + String.valueOf(new Random().nextInt()) + "_";

        // initialize dial charts with empty data. This will avoid null
        // initialization of portlet when topic returns null values.
        initializeToEmpty();
    }

    /**
     * checks against Extended Roles for the user
     * 
     * @return false if user do not have permission to view this portlet as per
     *         the extended roles defined for him
     * 
     */
    private boolean checkAgainstExtendedRoles() {
        if (!userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()
                || !userExtendedRoleBean.getExtRoleServiceGroupList().isEmpty()) {
            // user does not have access to entire network
            String inadequatePermissionsMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                    + " [ Entire Network ] data.";
            handleInfo(inadequatePermissionsMessage);
            return false;
        }

        return true;
    }

    /**
     * Initialize dial charts with empty data. This will avoid null
     * initialization of portlet when topic returns null values.
     */
    private void initializeToEmpty() {
        this.setServiceChecksAverage(ZERO);
        this.setServiceExecutionAverage(ZERO);
        this.setServiceExecutionMaximum(ZERO);
        this.setServiceLatencyAverage(ZERO);

        ServiceChecksBean serviceChecksBean = new ServiceChecksBean(
                ACTIVE_SERVICE_CHECKS, ZERO_S, ZERO_S, ZERO_S);
        serviceChecks.add(serviceChecksBean);

        serviceChecksBean = new ServiceChecksBean(PASSIVE_SERVICE_CHECKS,
                ZERO_S, ZERO_S, ZERO_S);
        serviceChecks.add(serviceChecksBean);
        this.setServiceChecks(serviceChecks);

        PerformanceStatisticsBean performanceStatisticsBean = new PerformanceStatisticsBean(
                SERVICE_CHECK_EXECUTION_TIME, ZERO_S, ZERO_S, ZERO_S);
        statisticChecks.add(performanceStatisticsBean);

        performanceStatisticsBean = new PerformanceStatisticsBean(
                CHECK_LATENCY, ZERO_S, ZERO_S, ZERO_S);
        statisticChecks.add(performanceStatisticsBean);

        performanceStatisticsBean = new PerformanceStatisticsBean(
                HOST_CHECK_EXECUTION_TIME, ZERO_S, ZERO_S, ZERO_S);
        statisticChecks.add(performanceStatisticsBean);
        this.setStatisticChecks(statisticChecks);

        /* Call chart drawing methods here */
        drawLatencyChartImage();
        drawExecutionChartImage();
        drawChecksChartImage();

        /* Re-render session */
        SessionRenderer.render(this.groupRenderName);
    }

    /**
     * (non-Javadoc).
     * 
     * @param xml
     *            the xml
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    // public void onMessage(Message message) {}
    public void refresh(String xml) {

        // TextMessage textMessage = (TextMessage) message;
        try {
            // logger
            // .debug("#### MONITORING PERFORMANCE --------- onMessage() - Incoming message : "
            // + xml);
            InputStream inputStream = new ByteArrayInputStream(xml
                    .getBytes(UTF_8));
            parseXML(inputStream);

            /* Call chart drawing methods here */
            drawLatencyChartImage();
            drawExecutionChartImage();
            drawChecksChartImage();
            /* Re-render session */
            // logger.error("#### MON PERF SessionRenderer.render() BEFORE");
            SessionRenderer.render(this.groupRenderName);
            // logger.debug("#### MON PERF SessionRenderer.render() AFTER");
        } catch (UnsupportedEncodingException e) {
            /* Ignore */
            logger
                    .debug("onMessage(): Invalid data received - unsupported encoding. "
                            + e.getMessage());
        } catch (NumberFormatException numberFormatException) {
            // ignore the exception - as sometimes on some configurations,
            // Nagios JMS publisher returns null values for required values.
            // OK to eat.
        } catch (Exception e) {
            // ignore the exception
            // OK to eat.
        }
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     *            the event
     */
    public void reloadPage(ActionEvent event) {

        if (!checkAgainstExtendedRoles()) {
            return;
        }

        // re-initialize the bean so as to reload UI
        this.error = false;
        this.message = false;
        this.info = false;

        initializeToEmpty();
        // try {
        // super.initJMS();
        // super.setInitError(false);
        // } catch (JMSException e) {
        // logger
        // .debug("reloadPage(): Error re-initializing monitoring performance portlet. "
        // + e.getMessage());
        // this.error = true;
        // this.errorMessage = e.getMessage();
        // }
    }

    /**
     * Parses given input-stream.
     * 
     * @param inputStream
     *            the input stream
     */
    private void parseXML(InputStream inputStream) {

        /* Initialize XML file parser */
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = docBuilderFactory.newDocumentBuilder();
            /* Validate */
            this.document = documentBuilder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            /* Error initializing XML Parser - Portlet cannot recover */
            logger.error("parseXML(): Error initializing XML Parser");
        } catch (SAXException e) {
            /*
             * Error validating XML Message - Portlet should not be affected -
             * can get garbage data, so ignore altogether
             */
            logger.debug("parseXML(): Error validating XML Message");
            return;
        } catch (IOException e) {
            /*
             * Error accessing XML Message input stream - Portlet should not be
             * affected - can get garbage data, so ignore altogether
             */
            logger.debug("parseXML(): Error accessing XML Message");
            return;
        }

        /*
         * Normalize text representation - Puts all Text nodes in the full depth
         * of the sub-tree underneath this Node.
         */
        this.document.getDocumentElement().normalize();

        /* Retrieve Statistics tag data */
        retrieveStatistics();

        /* Retrieve Service Checks tag data */
        retrieveServiceChecks();
    }

    /**
     * Method to retrieve statistics.
     */
    private void retrieveStatistics() {

        logger.debug("retrieveStatistics(): Retrieving statistics..");
        List<PerformanceStatisticsBean> statisticChecksTemp = new ArrayList<PerformanceStatisticsBean>();

        // FOR STATISTICS
        NodeList listOfStatistics = this.document
                .getElementsByTagName(STATISTICS);
        // Read internal elements of each node
        Node theOnlyNode = listOfStatistics.item(0);
        // Node can be read only if it is of type element
        // i.e. Statistics node itself has no element, but has internal
        // nodes are having elements
        if (theOnlyNode.getNodeType() == Node.ELEMENT_NODE) {
            // Read elements of aggregator one by one
            Element theOnlyElement = (Element) theOnlyNode;

            // ---- Retrieve ServiceCheckExecutionTime ----
            NodeList serviceChkExeTimeList = theOnlyElement
                    .getElementsByTagName(SERVICE_CHECK_EXECUTIION_TIME);
            Element serviceCheckExecutionTimeElement = (Element) serviceChkExeTimeList
                    .item(0);
            retrieveList(serviceCheckExecutionTimeElement, MIN, AVERAGE, MAX);

            setServiceExecutionAverage(Float.parseFloat(this.tag2String));

            setServiceExecutionMaximum(Float.parseFloat(this.tag3String));

            PerformanceStatisticsBean performanceStatisticsBean = new PerformanceStatisticsBean(
                    SERVICE_CHECK_EXECUTION_TIME, this.tag1String,
                    this.tag2String, this.tag3String);
            statisticChecksTemp.add(performanceStatisticsBean);

            // ---- Retrieve ServiceCheckLatency ----
            NodeList serviceChkLatencyTimeList = theOnlyElement
                    .getElementsByTagName(SERVICE_CHECK_LATENCY);
            Element serviceCheckLatencyTimeElement = (Element) serviceChkLatencyTimeList
                    .item(0);
            retrieveList(serviceCheckLatencyTimeElement, MIN, AVERAGE, MAX);

            setServiceLatencyAverage(Float.parseFloat(this.tag2String));
            performanceStatisticsBean = new PerformanceStatisticsBean(
                    CHECK_LATENCY, this.tag1String, this.tag2String,
                    this.tag3String);
            statisticChecksTemp.add(performanceStatisticsBean);

            // ---- Retrieve Host Check Execution Time ----
            NodeList hostCheckExecutionTimeList = theOnlyElement
                    .getElementsByTagName(CHECK_EXECUTION_TIME);
            Element hostCheckExecutionTimeElement = (Element) hostCheckExecutionTimeList
                    .item(0);
            retrieveList(hostCheckExecutionTimeElement, MIN, AVERAGE, MAX);

            performanceStatisticsBean = new PerformanceStatisticsBean(
                    HOST_CHECK_EXECUTION_TIME, this.tag1String,
                    this.tag2String, this.tag3String);
            statisticChecksTemp.add(performanceStatisticsBean);

            // Set the List
            setStatisticChecks(statisticChecksTemp);
        }
    }

    /**
     * Method to retrieve service checks.
     */

    private void retrieveServiceChecks() {

        logger.debug("retrieveServiceChecks(): Retrieving service checks..");

        // FOR SERVICE CHECK NODE
        List<ServiceChecksBean> serviceChecksTemp = new ArrayList<ServiceChecksBean>();

        NodeList listOfServiceChecks = this.document
                .getElementsByTagName(SERVICE_CHECK);
        Node theOnlyNode = listOfServiceChecks.item(0);
        if (theOnlyNode.getNodeType() == Node.ELEMENT_NODE) {
            Element theOnlyElement = (Element) theOnlyNode;

            // ---- Retrieve Active Service Checks ----
            NodeList activeServiceChecksList = theOnlyElement
                    .getElementsByTagName(ACTIVE);
            Element activeServiceChecksElement = (Element) activeServiceChecksList
                    .item(0);

            retrieveList(activeServiceChecksElement, MIN1, MIN5, MIN15);
            this.serviceChecksAverage = Float.parseFloat(this.tag1String);

            ServiceChecksBean serviceChecksBean = new ServiceChecksBean(
                    ACTIVE_SERVICE_CHECKS, this.tag1String, this.tag2String,
                    this.tag3String);
            serviceChecksTemp.add(serviceChecksBean);

            // ---- Retrieve Passive Service Checks ----
            NodeList passiveServiceChecksList = theOnlyElement
                    .getElementsByTagName(PASSIVE);
            Element passiveServiceChecksElement = (Element) passiveServiceChecksList
                    .item(0);

            retrieveList(passiveServiceChecksElement, MIN1, MIN5, MIN15);

            serviceChecksBean = new ServiceChecksBean(PASSIVE_SERVICE_CHECKS,
                    this.tag1String, this.tag2String, this.tag3String);
            this.serviceChecksAverage += Float.parseFloat(this.tag1String);

            serviceChecksTemp.add(serviceChecksBean);

            // Set the List
            setServiceChecks(serviceChecksTemp);
        }
    }

    /**
     * Retrieves service check values for 1, 5 & 15 minutes or MIN AVG MAX.
     * 
     * @param serviceChecksElement
     *            the service checks element
     * @param tag1
     *            the tag1
     * @param tag2
     *            the tag2
     * @param tag3
     *            the tag3
     */
    private void retrieveList(Element serviceChecksElement, String tag1,
            String tag2, String tag3) {

        // -- MIN 1 or MIN --
        NodeList min1List = serviceChecksElement.getElementsByTagName(tag1);
        this.tag1String = getTagString(min1List);

        // -- MIN 5 or AVG --
        NodeList min5List = serviceChecksElement.getElementsByTagName(tag2);
        this.tag2String = getTagString(min5List);

        // -- MIN 15 or MAX --
        NodeList min15List = serviceChecksElement.getElementsByTagName(tag3);
        this.tag3String = getTagString(min15List);
    }

    /**
     * Gets the tag string.
     * 
     * @param listName
     *            the list name
     * 
     * @return the tag string
     */
    private String getTagString(NodeList listName) {

        /* If element by that tag name does not exist */
        if (listName == null) {
            return "";
        }
        Element tagElement = (Element) listName.item(0);
        NodeList textList = tagElement.getChildNodes();
        return (textList.item(0)).getNodeValue().trim();
    }

    // METHODS TO DRAW JFREECHARTS
    /**
     * Method to draw dial chart for Latency Dial.
     */
    private void drawLatencyChartImage() {

        // Value Data set
        DefaultValueDataset serviceLatencyDataSet;
        JFreeChart serviceLatencyChart;
        DialPlot plot = new DialPlot();

        float averageData = getServiceLatencyAverage();

        // Retrieve data set value
        serviceLatencyDataSet = new DefaultValueDataset(averageData);
        plot.setDataset(0, serviceLatencyDataSet);

        // Create a dial frame layer
        StandardDialFrame dialFrame = new StandardDialFrame();
        dialFrame.setBackgroundPaint(Color.lightGray);
        dialFrame.setForegroundPaint(Color.white);
        plot.setDialFrame(dialFrame);

        // Set a dial-background
        DialBackground db = new DialBackground(Color.white);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.VERTICAL));
        plot.setBackground(db);

        // Create dial scale
        StandardDialScale scale1 = new StandardDialScale(0D, VAL_600,
                ANGLE_NEG_135, ANGLE_NEG_270, VAL_75, 0);
        plot.addScale(0, scale1);
        scale1.setMajorTickStroke(new BasicStroke(1f));
        scale1.setTickLabelFormatter(NumberFormat.getIntegerInstance());
        scale1.setTickRadius(SCALE_TICK_RADIUS);
        scale1.setTickLabelFont(new Font(FONT_DIALOG, Font.PLAIN, FONT_12));
        scale1.setTickLabelOffset(TICK_LABEL_OFFSET);

        // Generate a dial pointer
        DialPointer.Pointer p = new DialPointer.Pointer();
        p.setFillPaint(Color.BLACK);
        p.setWidthRadius(WIDTH_RADIUS);
        p.setDatasetIndex(0);
        plot.addPointer(p);

        // generate a dial cap
        DialCap cap = new DialCap();
        cap.setRadius(CAP_RADIUS);
        plot.setCap(cap);

        // set annotation
        DialTextAnnotation annotation1 = new DialTextAnnotation(AVG_SECONDS);
        annotation1.setFont(new Font(FONT_DIALOG, Font.PLAIN, ANNOTATION_FONT));
        annotation1.setRadius(ANNOTATION_RADIUS);
        plot.addLayer(annotation1);

        // Plot in chart
        serviceLatencyChart = new JFreeChart(plot);
        serviceLatencyChart.setBackgroundPaint(darkOrange);

        // chart to bytes
        try {
            byte[] encodeAsPNG = ChartUtilities.encodeAsPNG(serviceLatencyChart
                    .createBufferedImage(Constant.DIAL_WIDTH,
                            Constant.DIAL_HEIGHT));
            setLatencyChartImage(encodeAsPNG);
        } catch (IOException e) {
            /*
             * Error converting image to bytes - Portlet can show just table, no
             * image - can recover
             */
            logger
                    .error("drawLatencyChartImage(): Error generating dial chart");
        }
    }

    /**
     * Method to draw dial chart for Execution Dial.
     */
    private void drawExecutionChartImage() {

        JFreeChart serviceExecutionChart;
        DialPlot plot = new DialPlot();

        float averageData = getServiceExecutionAverage();
        float maximumData = getServiceExecutionMaximum();

        // Create data-set 1 and 2
        DefaultValueDataset dataset1 = new DefaultValueDataset(averageData);
        DefaultValueDataset dataset2 = new DefaultValueDataset(maximumData);
        // Set 2 data-sets
        plot.setDataset(0, dataset1);
        plot.setDataset(1, dataset2);

        // Create a dial frame layer
        StandardDialFrame dialFrame = new StandardDialFrame();
        dialFrame.setBackgroundPaint(Color.lightGray);
        dialFrame.setForegroundPaint(Color.white);
        plot.setDialFrame(dialFrame);

        // Set a dial-background
        DialBackground db = new DialBackground(Color.white);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.VERTICAL));
        plot.setBackground(db);

        // Generate first dial scale for average value
        StandardDialScale scale1 = new StandardDialScale(0D, VAL_120,
                ANGLE_NEG_180, ANGLE_NEG_180, VAL_20, 0);
        scale1.setTickLabelFont(new Font(FONT_DIALOG, Font.PLAIN, FONT_12));
        plot.addScale(0, scale1);
        scale1.setMajorTickStroke(new BasicStroke(1f));
        scale1.setTickRadius(SCALE_TICK_RADIUS);
        scale1.setTickLabelOffset(TICK_LABEL_OFFSET);
        scale1.setTickLabelFormatter(NumberFormat.getIntegerInstance());
        scale1.setTickLabelsVisible(true);

        // Generate second dial scale for maximum value
        StandardDialScale scale2 = new StandardDialScale(0D, VAL_120,
                ANGLE_NEG_180, ANGLE_NEG_180, VAL_20, 0);
        scale2.setTickLabelFont(new Font(FONT_DIALOG, Font.PLAIN, FONT_12));
        plot.addScale(1, scale2);
        scale2.setTickRadius(SCALE_TICK_RADIUS);
        scale2.setVisible(false);
        scale2.setTickLabelOffset(TICK_LABEL_OFFSET);
        scale2.setTickLabelFormatter(NumberFormat.getIntegerInstance());
        scale2.setMajorTickStroke(new BasicStroke(1f));

        // Generate third dial scale - red zone
        StandardDialScale scale3 = new StandardDialScale(VAL_120, VAL_150, 0,
                ANGLE_NEG_45, VAL_30, VAL_30);
        scale3.setFirstTickLabelVisible(false);
        scale3.setMajorTickLength(MAJOR_TICK_LENGTH);
        scale3.setTickRadius(TICK_RADIUS);
        scale3.setMajorTickStroke(new BasicStroke(TICK_STROKE_4F));
        scale3.setMajorTickPaint(Color.red);
        scale3.setTickLabelOffset(TICK_LABEL_OFFSET);
        scale3.setTickLabelFormatter(NumberFormat.getIntegerInstance());
        scale3.setTickLabelsVisible(false);
        scale3.setMinorTickPaint(Color.red);
        scale3.setMinorTickStroke(new BasicStroke(TICK_STROKE_4F));
        scale3.setVisible(true);
        scale3.setTickLabelPaint(Color.red);
        plot.addScale(2, scale3);

        // Map data set to scale
        plot.mapDatasetToScale(0, 0);
        plot.mapDatasetToScale(1, 1);

        // Generate a dial pointer - For Average value
        DialPointer.Pointer needle1 = new DialPointer.Pointer();
        needle1.setOutlinePaint(Color.BLACK);
        needle1.setWidthRadius(WIDTH_RADIUS);
        needle1.setDatasetIndex(0);
        plot.addPointer(needle1);

        // Generate a dial pointer - For Maximum value
        DialPointer.Pointer needle2 = new DialPointer.Pointer(); // datasetindex
        needle2.setOutlinePaint(Color.RED);
        needle2.setWidthRadius(WIDTH_RADIUS);
        needle2.setDatasetIndex(1);
        plot.addPointer(needle2);

        // generate a dial cap
        DialCap cap = new DialCap();
        cap.setRadius(CAP_RADIUS);
        plot.setCap(cap);

        // set annotation
        DialTextAnnotation annotation1 = new DialTextAnnotation(SECONDS);
        annotation1.setFont(new Font(FONT_DIALOG, Font.PLAIN, ANNOTATION_FONT));
        annotation1.setRadius(ANNOTATION_RADIUS);
        plot.addLayer(annotation1);

        // Plot in chart
        serviceExecutionChart = new JFreeChart(plot);
        serviceExecutionChart.setBackgroundPaint(darkOrange);

        // chart to bytes
        try {
            byte[] encodeAsPNG = ChartUtilities
                    .encodeAsPNG(serviceExecutionChart.createBufferedImage(
                            Constant.DIAL_WIDTH, Constant.DIAL_HEIGHT));
            setExecutionChartImage(encodeAsPNG);
        } catch (IOException e) {
            /*
             * Error converting image to bytes - Portlet can show just table, no
             * image - can recover
             */
            logger
                    .error("drawExecutionChartImage(): Error generating dial chart");
        }
    }

    /**
     * Method to draw chart for Service Checks Dial.
     */
    private void drawChecksChartImage() {

        DefaultValueDataset averageServiceLatencyDataSet;
        JFreeChart averageServiceCheckChart;
        DialPlot plot = new DialPlot();
        float averageData = getServiceChecksAverage();

        // Create data-set
        averageServiceLatencyDataSet = new DefaultValueDataset(averageData);
        plot.setDataset(0, averageServiceLatencyDataSet);

        // Create a dial frame layer
        StandardDialFrame dialFrame = new StandardDialFrame();
        dialFrame.setBackgroundPaint(Color.lightGray);
        dialFrame.setForegroundPaint(Color.white);
        plot.setDialFrame(dialFrame);

        // Set a dial-background
        DialBackground db = new DialBackground(Color.white);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.VERTICAL));
        plot.setBackground(db);

        // Dial Scale
        StandardDialScale scale1 = new StandardDialScale(0D, VAL_5250,
                ANGLE_NEG_135, ANGLE_NEG_270, VAL_750, 0);
        scale1.setTickLabelFont(new Font(FONT_DIALOG, Font.PLAIN, FONT_10));
        scale1.setTickRadius(SCALE_TICK_RADIUS);
        scale1.setTickLabelOffset(TICK_LABEL_OFFSET_SCALE1);
        scale1.setMajorTickStroke(new BasicStroke(1f));
        scale1.setTickLabelFormatter(NumberFormat.getIntegerInstance());
        plot.addScale(0, scale1);

        // Generate a dial pointer
        DialPointer.Pointer p = new DialPointer.Pointer();
        p.setFillPaint(Color.BLACK);
        p.setWidthRadius(WIDTH_RADIUS);
        plot.addPointer(p);

        // generate a dial cap
        DialCap cap = new DialCap();
        cap.setRadius(CAP_RADIUS);
        plot.setCap(cap);

        // set annotation
        DialTextAnnotation annotation1 = new DialTextAnnotation(
                DIAL_TEXT_ANNOTATION);
        annotation1.setFont(new Font(FONT_DIALOG, Font.PLAIN, ANNOTATION_FONT));
        annotation1.setRadius(ANNOTATION_RADIUS);
        plot.addLayer(annotation1);

        // Plot in chart
        averageServiceCheckChart = new JFreeChart(plot);
        averageServiceCheckChart.setBackgroundPaint(lightOrange);

        // chart to bytes
        try {
            byte[] encodeAsPNG = ChartUtilities
                    .encodeAsPNG(averageServiceCheckChart.createBufferedImage(
                            Constant.DIAL_WIDTH, Constant.DIAL_HEIGHT));

            setChecksChartImage(encodeAsPNG);

        } catch (IOException e) {
            /*
             * Error converting image to bytes - Portlet can show just table, no
             * image - can recover
             */
            logger.error("drawChecksChartImage(): Error generating dial chart");
        }
    }

    // GETTERS & SETTERS
    /**
     * Sets the latencyChartImage.
     * 
     * @param latencyChartImage
     *            the latencyChartImage to set
     */
    public void setLatencyChartImage(byte[] latencyChartImage) {
        this.latencyChartImage = latencyChartImage;
    }

    /**
     * Returns the latencyChartImage.
     * 
     * @return the latencyChartImage
     */
    public byte[] getLatencyChartImage() {
        return this.latencyChartImage;
    }

    /**
     * Sets the executionChartImage.
     * 
     * @param executionChartImage
     *            the executionChartImage to set
     */
    public void setExecutionChartImage(byte[] executionChartImage) {
        this.executionChartImage = executionChartImage;
    }

    /**
     * Returns the executionChartImage.
     * 
     * @return the executionChartImage
     */
    public byte[] getExecutionChartImage() {
        return this.executionChartImage;
    }

    /**
     * Sets the checksChartImage.
     * 
     * @param checksChartImage
     *            the checksChartImage to set
     */
    public void setChecksChartImage(byte[] checksChartImage) {
        this.checksChartImage = checksChartImage;
    }

    /**
     * Returns the checksChartImage.
     * 
     * @return the checksChartImage
     */
    public byte[] getChecksChartImage() {
        return this.checksChartImage;
    }

    /**
     * Returns the error.
     * 
     * @return the error
     */
    public boolean isError() {
        return this.error;
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Returns the statisticChecks.
     * 
     * @return the statisticChecks
     */
    public List<PerformanceStatisticsBean> getStatisticChecks() {
        return this.statisticChecks;
    }

    /**
     * Sets the statistic checks.
     * 
     * @param statisticChecks
     *            the statistic checks
     */
    public void setStatisticChecks(
            List<PerformanceStatisticsBean> statisticChecks) {
        this.statisticChecks = statisticChecks;
    }

    /**
     * Sets the serviceLatencyAverage.
     * 
     * @param serviceLatencyAverage
     *            the serviceLatencyAverage to set
     */
    public void setServiceLatencyAverage(float serviceLatencyAverage) {
        this.serviceLatencyAverage = serviceLatencyAverage;
    }

    /**
     * Returns the serviceLatencyAverage.
     * 
     * @return the serviceLatencyAverage
     */
    public float getServiceLatencyAverage() {
        return this.serviceLatencyAverage;
    }

    /**
     * Sets the serviceExecutionAverage.
     * 
     * @param serviceExecutionAverage
     *            the serviceExecutionAverage to set
     */
    public void setServiceExecutionAverage(float serviceExecutionAverage) {
        this.serviceExecutionAverage = serviceExecutionAverage;
    }

    /**
     * Returns the serviceChecks.
     * 
     * @return the serviceChecks
     */
    public List<ServiceChecksBean> getServiceChecks() {
        return this.serviceChecks;
    }

    /**
     * Sets the service checks.
     * 
     * @param serviceChecks
     *            the service checks
     */
    public void setServiceChecks(List<ServiceChecksBean> serviceChecks) {

        this.serviceChecks = serviceChecks;
    }

    /**
     * Returns the serviceExecutionAverage.
     * 
     * @return the serviceExecutionAverage
     */
    public float getServiceExecutionAverage() {
        return this.serviceExecutionAverage;
    }

    /**
     * Sets the serviceExecutionMaximum.
     * 
     * @param serviceExecutionMaximum
     *            the serviceExecutionMaximum to set
     */
    public void setServiceExecutionMaximum(float serviceExecutionMaximum) {
        this.serviceExecutionMaximum = serviceExecutionMaximum;
    }

    /**
     * Returns the serviceExecutionMaximum.
     * 
     * @return the serviceExecutionMaximum
     */
    public float getServiceExecutionMaximum() {
        return this.serviceExecutionMaximum;
    }

    /**
     * Sets the serviceChecksAverage.
     * 
     * @param serviceChecksAverage
     *            the serviceChecksAverage to set
     */
    public void setServiceChecksAverage(float serviceChecksAverage) {
        this.serviceChecksAverage = serviceChecksAverage;
    }

    /**
     * Returns the serviceChecksAverage.
     * 
     * @return the serviceChecksAverage
     */
    public float getServiceChecksAverage() {
        return this.serviceChecksAverage;
    }

    /**
     * Returns the errorMessage.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Sets the errorMessage.
     * 
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Sets the message.
     * 
     * @param message
     *            the message to set
     */
    public void setMessage(boolean message) {
        this.message = message;
    }

    /**
     * Returns the message.
     * 
     * @return the message
     */
    public boolean isMessage() {
        return message;
    }

    /**
     * Sets the info.
     * 
     * @param info
     *            the info to set
     */
    public void setInfo(boolean info) {
        this.info = info;
    }

    /**
     * Returns the info.
     * 
     * @return the info
     */
    public boolean isInfo() {
        return info;
    }

    /**
     * Sets the infoMessage.
     * 
     * @param infoMessage
     *            the infoMessage to set
     */
    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    /**
     * Returns the infoMessage.
     * 
     * @return the infoMessage
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * Handles Info : sets Info flag and message.
     */
    private void handleInfo(String infoMessage) {
        setMessage(true);
        setInfo(true);
        setInfoMessage(infoMessage);
    }

}
