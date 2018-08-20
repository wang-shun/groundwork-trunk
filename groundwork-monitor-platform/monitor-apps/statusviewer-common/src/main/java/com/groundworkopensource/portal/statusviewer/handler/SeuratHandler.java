package com.groundworkopensource.portal.statusviewer.handler;

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

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.AcknowledgePopupBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.SeuratBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.CommonUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.ServiceMonitorStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.SeuratBeanComparator;
import com.groundworkopensource.portal.statusviewer.common.SeuratSortType;
import com.groundworkopensource.portal.statusviewer.common.SeuratStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.CommandDescriptionConstants;
import com.icesoft.faces.component.datapaginator.DataPaginator;
import com.icesoft.faces.component.ext.HtmlPanelGroup;
import com.icesoft.faces.context.effects.JavascriptContext;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is functional hub for seurat view portlet.
 * 
 * @author nitin_jadhav
 */
public class SeuratHandler extends ServerPush {

    /** serialVersionUID. */
    private static final long serialVersionUID = 5153928914836167330L;

    /** ERROR_NO_HOSTS_FOUND Constant. */
    private static final String ERROR_NO_HOSTS_FOUND = "com_groundwork_portal_statusviewer_seurat_error_noHostsFound";

    /** HOST_NAME Constant. */
    private static final String HOST_NAME = "contextForm:hostName";

    /** Message Host Group Does not Exist. */
    private static final String MSG_HG_NOT_EXIST = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable");

    /** RECENT_RECOVERY_TIME_IN_MINUTES_PROPERTY Constant. */
    private static final String RECENT_RECOVERY_TIME_IN_MINUTES_PROPERTY = "portal.statusviewer.seurat.recentRecoveryTimeInMinutes";

    /** DATE_FORMAT_PROPERTY constant. */
    private static final String DATE_FORMAT_PROPERTY = "portal.statusviewer.seurat.dateFormatString";

    /**
     * The maximum time in minutes, which will be used to decide if the host is
     * recently recovered or not. will be read from property file.
     */
    private int recentRecoveredTimeinMinutes;

    /** DEFAULT_RECENT_RECOVERY_TIME_MINUTS constant. */
    private static final int DEFAULT_RECENT_RECOVERY_TIME_MINUTS = 15;

    /**
     * The maximum time in milliseconds, which will be used to decide if the
     * host is recently recovered or not. will be calculated from
     * 'recentRecoveredTimeinMinutes' field.
     */
    private long recentRecoveredTimeinMillisecs;

    /** Milliseconds in one minute. */
    private static final long MINUTS_MILLISECONDS = 60000;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SeuratHandler.class
            .getName());

    /** reference model for building tree. */
    private ReferenceTreeMetaModel referenceTreeModel;

    /** "Last Updated" field on UI. */
    private Date lastUpdatedDate;

    /**
     * number of host columns in seurat portlet. this will be read from property
     * file.
     */
    private int columns;

    /**
     * Date format of "Last Updated" field. this will be read from property
     * file.
     */
    private String lastUpdatedDateFormat;

    /**
     * Date format of "Last Updated" field. this will be read from property
     * file.
     */
    private static final String DEFAULT_LAST_UPDATED_DATE_FORMAT = "MM/dd/yy HH:mm:ss";

    /** List of hosts, that is bind to UI in order to show seurat hosts. */
    private List<SeuratBean> hostList = Collections
            .synchronizedList(new ArrayList<SeuratBean>());

    /** foundationWSFacade Object to call web services. */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * Map for retrieving "Seurat status" (which includes "recently recovered"
     * and "n% services troubled" states) of hosts from there monitor status,
     * provided by web services.
     */
    private Map<String, SeuratStatusEnum> entityStatusMap;

    /** total number of hosts in seurat portlet. */
    private int hostCount;

    /** boolean variable to used to open and close services pop up window. */
    private boolean popupVisible = false;

    /**
     * this is the current host, on which user right-clicks for showing details.
     */
    private SeuratBean selectedHost;

    /**
     * boolean variable to used to decide whether or not to enable the
     * "acknowledge host" button on popup window.
     */
    private boolean acknowledgeHostButtonEnabled;

    /**
     * service monitor status map
     */
    private Map<String, ServiceMonitorStatusEnum> serviceStatusMap;

    /**
     * Array of SeuratStatusEnum items, that are up, but have some % troubled
     * services.
     */
    private SeuratStatusEnum[] serviceAvailabilityStates = new SeuratStatusEnum[] {
            SeuratStatusEnum.SEURAT_HOST_TROUBLED_25P,
            SeuratStatusEnum.SEURAT_HOST_TROUBLED_50P,
            SeuratStatusEnum.SEURAT_HOST_TROUBLED_75P,
            SeuratStatusEnum.SEURAT_HOST_TROUBLED_100P };

    /**
     * This list contains the temporary data of services, of host on which user
     * right-clicked to show service details.
     */
    private List<NetworkMetaEntity> serviceList = new ArrayList<NetworkMetaEntity>();

    /** Simple date format, required to format dates. */
    private SimpleDateFormat dateFormat;

    /** currently selected sort option. */
    private String currentSortOption = SeuratSortType.ALPHA.getOptionName();

    /**
     * previously selected sort option, which will be applied in case of
     * LAST_STATE_CHANGE sort option.
     */
    private SeuratSortType lastSortOption;

    /** Error boolean to set if error occurred. */
    private boolean error = false;

    /** info boolean to set if information occurred. */
    private boolean info = false;

    /**
     * boolean variable message set true when display any type of messages
     * (Error,info or warning) in UI.
     */
    private boolean message = false;

    /** information message to show on UI. */
    private String infoMessage;

    /** Error message to show on UI. */
    private String errorMessage;

    /** String property for the column to sort. */
    private String sortColumn;

    /** boolean property indicating the ascending sort order. */
    private boolean ascending = true;

    /** SelectItem list for sorting options such as Host name and Status. */
    private final ArrayList<SelectItem> sortingOptions = new ArrayList<SelectItem>();

    /** Comparator, used to sort the hostList. */
    private SeuratBeanComparator comparator = new SeuratBeanComparator();

    /** get host group name from preference. */
    private String hostGroupName = null;

    /**
     * old host group name for comparison and detecting if the host group in
     * preferences is changed.
     */
    private String oldHostGroupName = Constant.EMPTY_STRING;

    /** get host group id from preference. */
    private int hostGroupId = -1;

    /** decides whether to show acknowledge all services button on screen. */
    private boolean ackAllServicesButtonEnabled;

    /** This field is bound to seurat grid panelGroup on page. */
    private HtmlPanelGroup seuratGrid;

    /** hidden field. */
    private String hiddenField = Constant.HIDDEN;

    /**
     * dynamic form id
     */
    private String frmId;

    /**
     * UserRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    HashMap<String, String> itemsList = new HashMap<String, String>();
    HashMap<String, DateTime> holdblinklist = new HashMap<String, DateTime>();
    private static final String SV_PROP_PATH = "/usr/local/groundwork/config/status-viewer.properties";

    private String SEURAT_HOST_DOWN_UNSCHEDULED_MINS = "portal.statusviewer.seurat.host.unscheduleddown";
    private String SEURAT_HOST_UNREACHABLE_MINS = "portal.statusviewer.seurat.host.unreachable";
    private String SEURAT_HOST_TROUBLED_100P_MINS = "portal.statusviewer.seurat.host.troubled100p";
    private String SEURAT_HOST_TROUBLED_75P_MINS = "portal.statusviewer.seurat.host.troubled75p";
    private String SEURAT_HOST_TROUBLED_50P_MINS = "portal.statusviewer.seurat.host.troubled50p";
    private String SEURAT_HOST_TROUBLED_25P_MINS = "portal.statusviewer.seurat.host.troubled25p";
    private String SEURAT_HOST_DOWN_SCHEDULED_MINS = "portal.statusviewer.seurat.host.scheduleddown";
    private String SEURAT_HOST_PENDING_MINS = "portal.statusviewer.seurat.host.pending";
    private String SEURAT_HOST_RECENTLY_RECOVERED_MINS = "portal.statusviewer.seurat.host.recentlyrecovered";
    private String SEURAT_HOST_UP_MINS = "portal.statusviewer.seurat.host.up";
    private String SEURAT_HOST_SUSPENDED_MINS = "portal.statusviewer.seurat.host.suspended";
    private String NO_STATUS_MINS = "portal.statusviewer.seurat.host.nostatus";

    private String SEURAT_HOST_DOWN_UNSCHEDULED_MINS_VAL = "";
    private String SEURAT_HOST_UNREACHABLE_MINS_VAL = "";
    private String SEURAT_HOST_TROUBLED_100P_MINS_VAL = "";
    private String SEURAT_HOST_TROUBLED_75P_MINS_VAL = "";
    private String SEURAT_HOST_TROUBLED_50P_MINS_VAL = "";
    private String SEURAT_HOST_TROUBLED_25P_MINS_VAL = "";
    private String SEURAT_HOST_DOWN_SCHEDULED_MINS_VAL = "";
    private String SEURAT_HOST_PENDING_MINS_VAL = "";
    private String SEURAT_HOST_RECENTLY_RECOVERED_MINS_VAL = "";
    private String SEURAT_HOST_UP_MINS_VAL = "";
    private String SEURAT_HOST_SUSPENDED_MINS_VAL = "";
    private String NO_STATUS_MINS_VAL = "";

    /** service comparator. */
    private Comparator<NetworkMetaEntity> servicecomparator = new Comparator<NetworkMetaEntity>() {
        public int compare(NetworkMetaEntity entity1, NetworkMetaEntity entity2) {
            String name1 = entity1.getName();
            String name2 = entity2.getName();
            int result = 0;
            // For sort order ascending -
            if (isAscending()) {
                result = name1.compareTo(name2);
            } else {
                // Descending
                result = name2.compareTo(name1);
            }
            return result;
        }
    };

    /** service comparator. */
    private Comparator<NetworkMetaEntity> serviceStatuscomparator = new Comparator<NetworkMetaEntity>() {
        public int compare(NetworkMetaEntity entity1, NetworkMetaEntity entity2) {
            String monitorStatus1 = entity1.getStatus().getStatus();
            String monitorStatus2 = entity2.getStatus().getStatus();
            return getServiceStatusEnumFromMonitorStatus(
                    monitorStatus1.toLowerCase()).compareTo(
                    getServiceStatusEnumFromMonitorStatus(monitorStatus2
                            .toLowerCase()));

        }
    };

    /**
     * empty constructor.
     */
    public SeuratHandler() {

        // set faces context
        if (FacesContext.getCurrentInstance() != null) {
            referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                    .getManagedBean(Constant.REFERENCE_TREE);

            FacesUtils.setFacesContext(FacesContext.getCurrentInstance());
        }

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // get values from application's property file and set to fields
        setValuesFromPropertyFile();
        initEntityStatusMap();
        initServiceStatusMap();
        // This date format will be used to format date and display on screen as
        // "last updated"
        dateFormat = new SimpleDateFormat(lastUpdatedDateFormat);

        // insert values from SeuratSortType to sortOptions list, for displaying
        // on UI
        populateSelectMenu();

        lastSortOption = SeuratSortType.ALPHA;

        int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
        // Unique id for form UI component
        frmId = "SVfrmSeuratView" + randomID;
    }

    /**
     * Gets the hidden field.
     * 
     * @return hiddenField
     */
    public String getHiddenField() {
        if (isIntervalRender()) {
            refreshSeurat();
        }
        setIntervalRender(false);
        return hiddenField;
    }

    /**
     * Sets the hidden field.
     * 
     * @param hiddenField
     *            the hidden field
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * insert values from SeuratSortType to sortOptions list, for displaying on
     * UI.
     */
    private void populateSelectMenu() {
        // Iterate thru all values of seuratSortType enumerator and add its
        // "optionName" property as label as well as value in selectOneMenu.
        SeuratSortType[] values = SeuratSortType.values();
        for (SeuratSortType sortType : values) {
            getSortingOptions().add(
                    new SelectItem(sortType.getOptionName(), sortType
                            .getOptionName()));
        }
    }

    /**
     * Populate the map, which will be used for obtaining seurat-status from
     * host's monitor status.
     */
    private void initEntityStatusMap() {
        entityStatusMap = new HashMap<String, SeuratStatusEnum>();
        // Iterate thru all values of statusStates enumerator and add its
        // "monitorStatus" and the state itself to map. this will be later used
        // to lookup for status by providing monitorStatus as key
        SeuratStatusEnum[] statusStates = SeuratStatusEnum.values();
        for (SeuratStatusEnum state : statusStates) {
            entityStatusMap.put(state.getMonitorStatus(), state);
        }
    }

    /**
     * Populate the service status map
     */
    private void initServiceStatusMap() {
        serviceStatusMap = new HashMap<String, ServiceMonitorStatusEnum>();
        ServiceMonitorStatusEnum[] serviceStatus = ServiceMonitorStatusEnum
                .values();
        for (ServiceMonitorStatusEnum state : serviceStatus) {
            serviceStatusMap.put(state.getMonitorStatus().toLowerCase(), state);
        }
    }


    /**
     * populate host list, by fetching all hosts with "getHostsUnderHostGroup"
     * web service query. Decide its status from monitorStatus. if its "UP", it
     * might be recently recovered or it might have some troubled services
     * (yellow icons). We will be using "SeuratBean" objects for storing these
     * hosts in list.
     * 
     * synchronized to tackle bug #GWMON-7082
     * 
     * @return false when no hosts found under specified host group
     * 
     * @throws PreferencesException
     *             the preferences exception
     * @throws GWPortalException
     *             the GW portal exception
     * @throws WSDataUnavailableException
     *             the WS data unavailable exception
     */
    private boolean populateList() throws PreferencesException, GWPortalException, WSDataUnavailableException {

        loadPropertiesLocal();

        // check for null faces context
        if (FacesContext.getCurrentInstance() != null) {

            // set reference of reference tree!!
            if (referenceTreeModel == null) {
                referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                        .getManagedBean(Constant.REFERENCE_TREE);
            }

            String entireNetworkPreference = FacesUtils
                    .getPreference(Constant.SEURAT_ENTIRENETWORK_PREF);

            Iterator<NetworkMetaEntity> hosts = null;
            if (null != entireNetworkPreference) {
                /*
                 * we will need hostGroupId in JMS Push for comparison -
                 * retrieve here by fetching HostGroup object by its name.
                 */
                boolean showEntireNetworkData = false;
                List<String> extRoleHostGroupList = userExtendedRoleBean
                        .getExtRoleHostGroupList();
                if (extRoleHostGroupList
                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    String inadequatePermissionsMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                            + " [ Host Groups ] data";
                    handleInfo(inadequatePermissionsMessage);
                    return false;
                }
                
                if (entireNetworkPreference.equals(Constant.TRUE)) {
                	showEntireNetworkData = true;
                	if (extRoleHostGroupList.isEmpty()) {
                		// For entire Network, check if extended role lists are
                		// empty
                		hosts = referenceTreeModel.getAllHosts();
                    }
                	else {
                		Iterator<NetworkMetaEntity> authorizedHostGroups= referenceTreeModel.getExtRoleHostGroups(extRoleHostGroupList);
                		ArrayList<NetworkMetaEntity>  tmpHostList = new ArrayList<NetworkMetaEntity>();              			
            		
                		while (authorizedHostGroups.hasNext()) {
                			NetworkMetaEntity authorizedHostGroup = authorizedHostGroups.next();
                			List<Integer> authorizedHosts = authorizedHostGroup. getChildNodeList();
                			for (int hostId : authorizedHosts) {
                				NetworkMetaEntity authorizedHost = referenceTreeModel.getHostById(hostId);
                				tmpHostList.add(authorizedHost);
                				hosts = tmpHostList.iterator();
                			} // end if
                		} // end while                		
                	} // end if/else
                }

                if (!showEntireNetworkData) {
                    hostGroupName = FacesUtils
                            .getPreference(PreferenceConstants.DEFAULT_HOST_GROUP_PREF);
                    if (null == hostGroupName
                            || hostGroupName.equals(Constant.EMPTY_STRING)) {
                        hostGroupName = userExtendedRoleBean
                                .getDefaultHostGroup();
                    }
                    if (hostGroupName != oldHostGroupName) {
                        HostGroup hostGroup;
                        try {
                            hostGroup = foundationWSFacade
                                    .getHostGroupsByName(hostGroupName);
                            if (null != hostGroup) {
                                hostGroupId = hostGroup.getHostGroupID();
                                oldHostGroupName = hostGroupName;

                                // check for extended role permissions
                                if (!userExtendedRoleBean
                                        .getExtRoleHostGroupList().isEmpty()
                                        && !referenceTreeModel
                                                .checkNodeForExtendedRolePermissions(
                                                        hostGroupId,
                                                        NodeType.HOST_GROUP,
                                                        hostGroupName,
                                                        userExtendedRoleBean
                                                                .getExtRoleHostGroupList(),
                                                        userExtendedRoleBean
                                                                .getExtRoleServiceGroupList())) {
                                    String inadequatePermissionsMessage = ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                            + " [" + hostGroupName + "]";
                                    handleInfo(inadequatePermissionsMessage);
                                    return false;
                                }
                                // oldHostGroupId = hostGroupId;
                            } else {
                                throw new WSDataUnavailableException();
                            }
                        } catch (Exception dataUnavailableException) {
                            handleInfo(MSG_HG_NOT_EXIST + Constant.SPACE
                                    + Constant.OPENING_SQUARE_BRACKET
                                    + hostGroupName
                                    + Constant.CLOSING_SQUARE_BRACKET);
                            return false;
                        }
                    }
                    Iterator<Integer> ithosts = referenceTreeModel
                            .getHostsUnderHostGroup(hostGroupId);

                    ArrayList<NetworkMetaEntity> list = new ArrayList<NetworkMetaEntity>();
                    if (ithosts != null) {
                        while (ithosts.hasNext()) {
                            list.add(referenceTreeModel.getHostById(ithosts
                                    .next()));
                        }
                    }
                    hosts = list.iterator();
                }
            }

            if (hosts == null) {
                handleInfo(ResourceUtils
                        .getLocalizedMessage(ERROR_NO_HOSTS_FOUND)
                        + " [" + hostGroupName + "]");
                return false;
            }

            List<SeuratBean> tmpHostList = new ArrayList<SeuratBean>();

            while (hosts.hasNext()) {
                NetworkMetaEntity host = hosts.next();



                // iterate through all the hosts to create seurat-grid
                if (host != null) {
                    SeuratStatusEnum status = getSeuratStatusFromMonitorStatus(host
                            .getMonitorStatus());

                    // if host is up, check whether if its recently recovered
                    long differenceInTime = 0;
                    Date lastStateChange = host.getLastStateChange();
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
                    DateTime blinkdate = lastStateChange == null ? null : new DateTime(lastStateChange);
                    Boolean blindateisnull = false;

                    if (blinkdate == null)
                    {
                        blinkdate = DateTime.parse("08/19/2016 12:12:20", fmt);
                        blindateisnull = true;
                    }

                    LocalDateTime blinkdatelocal = blinkdate.toLocalDateTime();

                    String blinkdateformat = fmt.print(blinkdatelocal);

                    if (blindateisnull)
                    {
                        blinkdateformat = " No Date Available at this time ";

                    }

                    if (status == SeuratStatusEnum.SEURAT_HOST_UP) {
                        if (lastStateChange != null) {
                            long changeTime = lastStateChange.getTime();
                            // calculate difference in current time and last
                            // state change time of host. if the difference <
                            // allowed time, Host is recently recovered
                            differenceInTime = new Date().getTime()
                                    - changeTime;
                            if (differenceInTime < recentRecoveredTimeinMillisecs) {
                                status = SeuratStatusEnum.SEURAT_HOST_RECENTLY_RECOVERED;
                            }
                        }
                        if (status != SeuratStatusEnum.SEURAT_HOST_RECENTLY_RECOVERED) {
                            // Host is up but not recently recovered. decide
                            // its status according to non-availability of
                            // services
                            double unavailableServicesPercent = 100.0-host.getServiceAvailablityForHost();
                            unavailableServicesPercent = Math.max(unavailableServicesPercent, 0.0);
                            unavailableServicesPercent = Math.min(unavailableServicesPercent, 100.0);

                            if (unavailableServicesPercent > 0.0) {
                                // based on availability, decide the bucket on
                                // which it falls into
                                for (SeuratStatusEnum availabilityStatus : serviceAvailabilityStates) {
                                    if (unavailableServicesPercent > availabilityStatus
                                            .getServiceLowerBound()
                                            && unavailableServicesPercent <= availabilityStatus
                                                    .getServiceUpperBound()) {
                                        status = availabilityStatus;
                                        break;
                                    }
                                }
                            }
                        }
                    }


                    SeuratBean seuratBean = new SeuratBean(host.getObjectId(),
                            host.getName(), status, differenceInTime,
                            NodeType.HOST, hostGroupName,
                            host.isAcknowledged(), host.getAppType(), host.getChildNodeList(),
                            referenceTreeModel, setIconStatus(status, host.getName(), blinkdate),
                            blinkdateformat, friendlystatusconversion(status.name(), blinkdateformat));


                    itemsList.put(host.getName(), status.getMonitorStatus());

                    tmpHostList.add(seuratBean);
                    seuratBean = null;
                }
            }

            synchronized (hostList) {
                // clear the list, trick for garbage collection!
                hostList.clear();
                hostList = tmpHostList;
            }
            setHostCount(hostList.size());
            // set lastUpdateDate
            lastUpdatedDate = new Date();
        }
        return true;
    }



    private String setIconStatus(SeuratStatusEnum status, String hostname, DateTime blinktime)
    {


        String path = "";
        String oldmonitorstat = "";

        if (itemsList.get(hostname) != null){
            oldmonitorstat = itemsList.get(hostname);
            itemsList.remove(hostname);
        }
        else{
            oldmonitorstat = "newhost";
        }

        if (status.getMonitorStatus() == oldmonitorstat){
            path = status.getIconPath();
        }
        else{
            holdblinklist.put(hostname, new DateTime(DateTimeZone.UTC));
            path = status.getBlinkIconPath();

        }

        if (holdblinklist.containsKey(hostname))

        {
            boolean iconshouldstillblink = blinkiscomplete(hostname, status, blinktime);

            if (iconshouldstillblink) {
                path = status.getBlinkIconPath();

            } else {
                path = status.getIconPath();
                holdblinklist.remove(hostname);
            }
        }

        return path;

    }

    private boolean blinkiscomplete(String hostname, SeuratStatusEnum status, DateTime blinktime)
    {
        String currentstatus = status.name();

        DateTime holdblinktime = blinktime.withZone(DateTimeZone.UTC);
        DateTime currentdate = new DateTime(DateTimeZone.UTC);
        long diffInMillis = currentdate.getMillis() - holdblinktime.getMillis();

        int minutes = (int) (diffInMillis / 1000)  / 60;

        if (minutes < getstatusmins(currentstatus))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    /**
     * Loads properties from file path.
     *
     * @param filePath
     * @return Properties
     */
    public static Properties loadPropertiesFromFilePath(String filePath) {
        FileInputStream defaultFS = null;
        Properties defaultProps = new Properties();
        try {
            defaultFS = new FileInputStream(filePath);
            defaultProps.load(defaultFS);
        } catch (Exception e) {
            LOGGER.info("Unable to find properties file [" + filePath
                    + "]. Using default");
        } finally {
            try {
                if (defaultFS != null) {
                    defaultFS.close();
                }
            } catch (IOException ioe) {
                LOGGER.warn("Unable to close the input stream for properties file ["
                        + filePath + "]. Exception is - " + ioe.getMessage());
            }
        }
        return defaultProps;
    }

    private void loadPropertiesLocal()
    {
        Properties setseurattimeprops = loadPropertiesFromFilePath(SV_PROP_PATH);
        SEURAT_HOST_DOWN_UNSCHEDULED_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_DOWN_UNSCHEDULED_MINS, "5");
        SEURAT_HOST_UNREACHABLE_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_UNREACHABLE_MINS, "5");
        SEURAT_HOST_TROUBLED_100P_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_TROUBLED_100P_MINS, "5");
        SEURAT_HOST_TROUBLED_75P_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_TROUBLED_75P_MINS, "5");
        SEURAT_HOST_TROUBLED_50P_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_TROUBLED_50P_MINS, "5");
        SEURAT_HOST_TROUBLED_25P_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_TROUBLED_25P_MINS, "5");
        SEURAT_HOST_DOWN_SCHEDULED_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_DOWN_SCHEDULED_MINS, "5");
        SEURAT_HOST_PENDING_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_PENDING_MINS, "5");
        SEURAT_HOST_RECENTLY_RECOVERED_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_RECENTLY_RECOVERED_MINS, "5");
        SEURAT_HOST_UP_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_UP_MINS, "5");
        SEURAT_HOST_SUSPENDED_MINS_VAL = setseurattimeprops.getProperty(SEURAT_HOST_SUSPENDED_MINS, "5");
        NO_STATUS_MINS_VAL = setseurattimeprops.getProperty(NO_STATUS_MINS, "5");

    }

    private int getstatusmins(String statusname)
    {
        if (statusname == "SEURAT_HOST_DOWN_UNSCHEDULED") { return Integer.parseInt(SEURAT_HOST_DOWN_UNSCHEDULED_MINS_VAL); }
        if (statusname == "SEURAT_HOST_UNREACHABLE") { return Integer.parseInt(SEURAT_HOST_UNREACHABLE_MINS_VAL); }
        if (statusname == "SEURAT_HOST_TROUBLED_100P") { return Integer.parseInt(SEURAT_HOST_TROUBLED_100P_MINS_VAL); }
        if (statusname == "SEURAT_HOST_TROUBLED_75P") { return Integer.parseInt(SEURAT_HOST_TROUBLED_75P_MINS_VAL); }
        if (statusname == "SEURAT_HOST_TROUBLED_50P") { return Integer.parseInt(SEURAT_HOST_TROUBLED_50P_MINS_VAL); }
        if (statusname == "SEURAT_HOST_TROUBLED_25P") { return Integer.parseInt(SEURAT_HOST_TROUBLED_25P_MINS_VAL); }
        if (statusname == "SEURAT_HOST_PENDING") { return Integer.parseInt(SEURAT_HOST_PENDING_MINS_VAL); }
        if (statusname == "SEURAT_HOST_DOWN_SCHEDULED") { return Integer.parseInt(SEURAT_HOST_DOWN_SCHEDULED_MINS_VAL); }
        if (statusname == "SEURAT_HOST_RECENTLY_RECOVERED") { return Integer.parseInt(SEURAT_HOST_RECENTLY_RECOVERED_MINS_VAL); }
        if (statusname == "SEURAT_HOST_UP") { return Integer.parseInt(SEURAT_HOST_UP_MINS_VAL); }
        if (statusname == "SEURAT_HOST_SUSPENDED") { return Integer.parseInt(SEURAT_HOST_SUSPENDED_MINS_VAL); }
        if (statusname == "NO_STATUS") { return Integer.parseInt(NO_STATUS_MINS_VAL); }

        return 0;
    }

    private String friendlystatusconversion(String statusname, String friendlytime)
    {
        if (statusname == "SEURAT_HOST_DOWN_UNSCHEDULED") { return "has been Down since " + friendlytime; }
        if (statusname == "SEURAT_HOST_UNREACHABLE") { return "is Unreachable, no status time available."; }
        if (statusname == "SEURAT_HOST_TROUBLED_100P") { return "has been 76% - 100% Services Not OK since " + friendlytime; }
        if (statusname == "SEURAT_HOST_TROUBLED_75P") { return "has been 51% - 75% Services Not OK since " + friendlytime; }
        if (statusname == "SEURAT_HOST_TROUBLED_50P") { return "has been 26% - 50% Services Not OK since " + friendlytime; }
        if (statusname == "SEURAT_HOST_TROUBLED_25P") { return "has been 1% - 25% Services Not OK since " + friendlytime; }
        if (statusname == "SEURAT_HOST_PENDING") { return "has been Pending since " + friendlytime; }
        if (statusname == "SEURAT_HOST_DOWN_SCHEDULED") { return "has been Scheduled Down since " + friendlytime; }
        if (statusname == "SEURAT_HOST_RECENTLY_RECOVERED") { return "has been Recently Recovered since " + friendlytime; }
        if (statusname == "SEURAT_HOST_UP") { return "has been Up since " + friendlytime; }
        if (statusname == "SEURAT_HOST_SUSPENDED") { return "has been Suspended since " + friendlytime; }
        if (statusname == "NO_STATUS") { return "has No Status since " + friendlytime; }

        return "";
    }

    /**
     * get values from application's property file and set to appropriate
     * fields. In case values not found, set 'em to DEFAULT values declared in
     * this class.
     */
    private void setValuesFromPropertyFile() {
        // Read last Updated Date Format from properties here
        lastUpdatedDateFormat = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, DATE_FORMAT_PROPERTY);
        if (lastUpdatedDateFormat == null
                || lastUpdatedDateFormat.equals(CommonConstants.EMPTY_STRING)) {
            lastUpdatedDateFormat = DEFAULT_LAST_UPDATED_DATE_FORMAT;
        }

        // Read recent recovery time from properties
        String recentlyRecoveredProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                RECENT_RECOVERY_TIME_IN_MINUTES_PROPERTY);
        if (recentlyRecoveredProperty != null
                && !recentlyRecoveredProperty
                        .equals(CommonConstants.EMPTY_STRING)) {
            try {
                recentRecoveredTimeinMinutes = Integer
                        .parseInt(recentlyRecoveredProperty);
            } catch (NumberFormatException e) {
                LOGGER.warn("Found incorrect property value for property: "
                        + RECENT_RECOVERY_TIME_IN_MINUTES_PROPERTY);
                recentRecoveredTimeinMinutes = DEFAULT_RECENT_RECOVERY_TIME_MINUTS;
            }
        }
        recentRecoveredTimeinMillisecs = recentRecoveredTimeinMinutes
                * MINUTS_MILLISECONDS;
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * It basically rebuilds the list and data models to show on UI.
     * 
     * @param event
     *            the event
     */
    public void reloadPage(ActionEvent event) {
        // re-initialize the handler so as to reload UI
        setError(false);
        setInfo(false);
        setMessage(false);

        try {
            // populate Host list with hosts
            if (!populateList()) {
                return;
            }

            // sort according to selected sort type option
            if (currentSortOption.equals(CommonConstants.EMPTY_STRING)) {
                sortList(SeuratSortType.ALPHA);
            } else {
                SeuratSortType[] values = SeuratSortType.values();
                for (SeuratSortType sortType : values) {
                    if (sortType.getOptionName().equals(currentSortOption)) {
                        sortList(sortType);
                    }
                }
            }

        } catch (PreferencesException e) {
            handleInfo(e.getMessage());
            return;
        } catch (GWPortalException e) {
            handleError(e.getMessage());
            return;
        } catch (WSDataUnavailableException e) {
            handleError(e.getMessage());
            return;
        }
    }

    /**
     * Sorts the serviceList on service name.
     * 
     * @param event
     *            the event
     */
    public void sort(ActionEvent event) {
        // sort the serviceList
        ascending = !ascending;
        Collections.sort(serviceList, servicecomparator);
    }

    /**
     * Sets the lastUpdatedDate.
     * 
     * @param lastUpdatedDate
     *            the lastUpdatedDate to set
     */
    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    /**
     * Returns the lastUpdatedDate.
     * 
     * @return the lastUpdatedDate
     */
    public String getLastUpdatedDate() {
        String date = CommonConstants.EMPTY_STRING;
        if (lastUpdatedDate != null) {
            date = dateFormat.format(lastUpdatedDate);
        }
        return date;
    }

    /**
     * Returns the hostList.
     * 
     * @return the hostList
     */
    public List<SeuratBean> getHostList() {
        return hostList;
    }

    /**
     * Sets the hostList.
     * 
     * @param hostList
     *            the hostList to set
     */
    public void setHostList(List<SeuratBean> hostList) {
        this.hostList = hostList;
    }

    /**
     * Returns the columns.
     * 
     * @return the columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Sets the hostCount.
     * 
     * @param hostCount
     *            the hostCount to set
     */
    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    /**
     * Returns the hostCount.
     * 
     * @return the hostCount
     */
    public int getHostCount() {
        return hostCount;
    }

    /**
     * Shows pop-up of host details on context menu click.
     * 
     * @param event
     *            the event
     */
    public void showHostDetailsPopup(ActionEvent event) {
        // This is to reset the sorting arrow image at top of column, resolution
        // of bug #GWMON-6582
        sortColumn = null;
        ascending = true;
        // get parameters from the request
        String hostNameParam = FacesUtils.getRequestParameter(HOST_NAME);

        try {
            if (hostNameParam != null) {
                Host host = foundationWSFacade.getHostsByName(hostNameParam);
                if (host != null) {
                    int hostId = host.getHostID();
                    SeuratStatusEnum status = getSeuratStatusFromMonitorStatus(host.getMonitorStatus().getName());
                    PropertyTypeBinding propertyTypeBinding = host.getPropertyTypeBinding();
                    BooleanProperty acknowledgedProperty = ((propertyTypeBinding != null) ? propertyTypeBinding
                            .getBooleanProperty("isAcknowledged") : null);
                    boolean isAcknowledged = ((acknowledgedProperty != null) && acknowledgedProperty.isValue());
                    if (status == SeuratStatusEnum.SEURAT_HOST_UP
                            || status == SeuratStatusEnum.SEURAT_HOST_PENDING) {
                        isAcknowledged = true;
                    }
                    String applicationType = CommonUtils.getApplicationNameByID(host.getApplicationTypeID());
                    selectedHost = new SeuratBean(hostId, hostNameParam,
                            status, isAcknowledged, applicationType, hostGroupName);
                    selectedHost.setType(NodeType.HOST);

                    // populate pop-up with data
                    populatePopup(hostId, host);

                    popupVisible = true;
                }
            }
        } catch (GWPortalException e) {
            handleError(e.getMessage());
            return;
        } catch (WSDataUnavailableException e) {
            handleError(e.getMessage());
            return;
        }

    }

    /**
     * Returns corresponding Seurat status according to given status string If
     * not found, it returns NO_STATUS.
     * 
     * @param seuratMonitorStatus
     *            the seurat monitor status
     * 
     * @return the seurat status from monitor status
     */
    private SeuratStatusEnum getSeuratStatusFromMonitorStatus(
            String seuratMonitorStatus) {
        SeuratStatusEnum seuratStatusEnum = entityStatusMap
                .get(seuratMonitorStatus);
        if (seuratStatusEnum != null) {
            return seuratStatusEnum;
        }
        return SeuratStatusEnum.NO_STATUS;
    }

    /**
     * Returns corresponding service status enum according to given status
     * string If not found, it returns NO_STATUS.
     */
    private ServiceMonitorStatusEnum getServiceStatusEnumFromMonitorStatus(
            String monitorStatus) {
        ServiceMonitorStatusEnum seuratStatusEnum = serviceStatusMap
                .get(monitorStatus);
        if (seuratStatusEnum != null) {
            return seuratStatusEnum;
        }
        return ServiceMonitorStatusEnum.NO_STATUS;
    }

    /**
     * This is a wrapper method , called on click of sort-options "apply" button
     * on seurat portlet. It calls sortList(SortType) method.
     * 
     * @param event
     *            the event
     */
    public void sortList(ActionEvent event) {

        if (SeuratSortType.ALPHA.getOptionName().equals(currentSortOption)) {
            // copy current sort option in lastSortOption so
            // as to use it in LAST STATE CHANGE sorting, if
            // applied.
            lastSortOption = SeuratSortType.ALPHA;
            sortList(SeuratSortType.ALPHA);
        } else if (SeuratSortType.SEVERITY.getOptionName().equals(
                currentSortOption)) {
            // copy current sort option in lastSortOption so
            // as to use it in LAST STATE CHANGE sorting, if
            // applied.
            lastSortOption = SeuratSortType.SEVERITY;
            sortList(SeuratSortType.SEVERITY);
        } else {
            // else it is STATE_CHANGE
            sortList(SeuratSortType.STATE_CHANGE);
        }

        // This call is used to initialize right click functionality on hosts.
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                "initSeurat();");

    }

    /**
     * Used to sort HostList as per argument. We will compare each sort option's
     * text with selected option text and set the matching sort option as
     * current sort option to process.
     * 
     * @param sortType
     *            the sort type
     */
    private synchronized void sortList(SeuratSortType sortType) {
        comparator.setSortType(sortType);
        Collections.sort(hostList, comparator);
    }

    /**
     * populate service list on pop-up with data.
     * 
     * @param hostId
     *            the host id
     * @param host
     *            the host
     * 
     * @throws WSDataUnavailableException
     *             the WS data unavailable exception
     * @throws GWPortalException
     *             the GW portal exception
     */
    private void populatePopup(int hostId, Host host)
            throws GWPortalException, WSDataUnavailableException {
        // remove all elements from list
        serviceList.clear();
        ackAllServicesButtonEnabled = false;

        ServiceStatus[] servicesUnderHost = foundationWSFacade.getServicesByHostName(host.getName());
        if (servicesUnderHost != null
                && servicesUnderHost.length != 0) {
            for (ServiceStatus service : servicesUnderHost) {
                // Construct new SeuratService object and insert into list
                int serviceId = service.getServiceStatusID();
                NetworkMetaEntity serviceEntity = referenceTreeModel
                        .getServiceById(serviceId);

                if (serviceEntity != null) {
                    // update service entity with latest acknowledgment status
                    PropertyTypeBinding propertyTypeBinding = service.getPropertyTypeBinding();
                    BooleanProperty acknowledgedProperty = ((propertyTypeBinding != null) ? propertyTypeBinding
                            .getBooleanProperty("isAcknowledged") : null);
                    boolean isAcknowledged = ((acknowledgedProperty != null) && acknowledgedProperty.isValue());
                    serviceEntity.setAcknowledged(isAcknowledged);

                    if (!serviceEntity.isAcknowledged()
                            && serviceEntity.getStatus() != NetworkObjectStatusEnum.SERVICE_OK
                            && serviceEntity.getStatus() != NetworkObjectStatusEnum.SERVICE_PENDING
                            && "NAGIOS".equals(serviceEntity.getAppType())) {
                        ackAllServicesButtonEnabled = true;
                    }

                    serviceEntity.setParentListString(hostGroupName
                            + Constant.COMMA + host.getName());
                    serviceList.add(serviceEntity);
                    serviceEntity = null;
                }
            }

            Collections.sort(serviceList, serviceStatuscomparator);

        }

    }

    /**
     * Method called when "acknowledge host" button is clicked in Host List
     * table.
     * 
     * @param event
     *            the event
     */
    public void showAcknowledgementPopup(ActionEvent event) {

        resetDataPaginator(event);
        // Get parameters for acknowledging
        String userName = FacesUtils.getLoggedInUser();

        // Set parameters in acknowledge popup bean
        AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
                .getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);
        if (acknowledgePopupBean == null) {
            LOGGER
                    .debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
            return;
        }
        // Indicates this is acknowledge command for service
        acknowledgePopupBean.setHostAck(true);
        acknowledgePopupBean.setAcknowledgeAllServices(false);
        acknowledgePopupBean.setHostName(selectedHost.getName());
        acknowledgePopupBean.setAuthor(userName);
        acknowledgePopupBean.setUserName(userName);
        acknowledgePopupBean.setAcknowledgeServicesCheckboxDisabled(false);
        acknowledgePopupBean.setTitle("Acknowledge Host");

        // from seurat view
        acknowledgePopupBean.setFromPortlet(Constant.SEURAT);

        // set if in dashboard or in status viewer
        boolean inDashbord = PortletUtils.isInDashbord();
        acknowledgePopupBean.setInStatusViewer(!inDashbord);
        if (inDashbord) {
            acknowledgePopupBean
                    .setPopupStyle(Constant.ACK_POPUP_DASHBOARD_STYLE);
        }

        // Set pop-up visible
        acknowledgePopupBean.setVisible();
        setPopupVisible(false);
    }

    /**
     * Method called when "acknowledge all services" button is clicked in Host
     * List table.
     * 
     * @param event
     *            the event
     */
    public void showAcknowledgementAllServicesPopup(ActionEvent event) {

        resetDataPaginator(event);
        // Get parameters for acknowledging
        String userName = FacesUtils.getLoggedInUser();

        // Set parameters in acknowledge popup bean
        AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
                .getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);
        if (acknowledgePopupBean == null) {
            LOGGER
                    .debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
            return;
        }
        acknowledgePopupBean.setHostAck(false);
        acknowledgePopupBean.setAcknowledgeAllServices(true);
        acknowledgePopupBean.setHostName(selectedHost.getName());
        acknowledgePopupBean.setAuthor(userName);
        acknowledgePopupBean.setUserName(userName);
        acknowledgePopupBean.setAcknowledgeServicesCheckboxDisabled(true);
        acknowledgePopupBean.setTitle("Acknowledge All Services");

        // from seurat view
        acknowledgePopupBean.setFromPortlet(Constant.SEURAT);
        // Set pop-up visible
        acknowledgePopupBean.setVisible();

        // set description of command
        acknowledgePopupBean
                .setCommandDescription(CommandDescriptionConstants.ACK_ALL_SERVICES_PROB);

        // make "acknowledge services check box" invisible
        // acknowledgePopupBean.setAckAllServicesCheckboxInvisible(true);

        // hide current pop up
        setPopupVisible(false);
    }

    /**
     * Handles error : sets error flag and message.
     * 
     * @param errorMsg
     *            the error msg
     */
    private void handleError(String errorMsg) {
        setMessage(true);
        setError(true);
        setErrorMessage(errorMsg);
    }

    /**
     * Handles Info : sets Info flag and message.
     * 
     * @param infoMsg
     *            the info msg
     */
    private void handleInfo(String infoMsg) {
        setMessage(true);
        setInfo(true);
        setInfoMessage(infoMsg);
    }

    /**
     * Sets the popupVisible.
     * 
     * @param popupVisible
     *            the popupVisible to set
     */
    public void setPopupVisible(boolean popupVisible) {
        this.popupVisible = popupVisible;
    }

    /**
     * Returns the popupVisible.
     * 
     * @return the popupVisible
     */
    public boolean isPopupVisible() {
        return popupVisible;
    }

    /**
     * Method called when close button on UI is called.
     * 
     * @param event
     *            the event
     */
    public void closePopup(ActionEvent event) {
        setPopupVisible(false);
        resetDataPaginator(event);
    }

    /**
     * @param event
     */
    private void resetDataPaginator(ActionEvent event) {
        // reset data Paginator component
        DataPaginator dataPaginator = (DataPaginator) event.getComponent()
                .findComponent("SPserviceDataPaginator");
        if (null != dataPaginator) {
            dataPaginator.gotoFirstPage();
        }
    }

    /**
     * Sets the serviceList.
     * 
     * @param serviceList
     *            the serviceList to set
     */
    public void setServiceList(List<NetworkMetaEntity> serviceList) {
        this.serviceList = serviceList;
    }

    /**
     * Returns the serviceList.
     * 
     * @return the serviceList
     */
    public List<NetworkMetaEntity> getServiceList() {
        return serviceList;
    }

    /**
     * Returns the selectedHost.
     * 
     * @return the selectedHost
     */
    public SeuratBean getSelectedHost() {
        return selectedHost;
    }

    /**
     * Sets the selectedHost.
     * 
     * @param selectedHost
     *            the selectedHost to set
     */
    public void setSelectedHost(SeuratBean selectedHost) {
        this.selectedHost = selectedHost;
    }

    /**
     * Sets the currentSortOption.
     * 
     * @param currentSortOption
     *            the currentSortOption to set
     */
    public void setCurrentSortOption(String currentSortOption) {
        this.currentSortOption = currentSortOption;
    }

    /**
     * Returns the currentSortOption.
     * 
     * @return the currentSortOption
     */
    public String getCurrentSortOption() {
        return currentSortOption;
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
     * Returns the error.
     * 
     * @return the error
     */
    public boolean isError() {
        return error;
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
     * Returns the errorMessage.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
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
     * Sets the sortColumn.
     * 
     * @param sortColumn
     *            the sortColumn to set
     */
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * Returns the sortColumn.
     * 
     * @return the sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * Sets the ascending.
     * 
     * @param ascending
     *            the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Returns the ascending.
     * 
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Returns the sortingOptions.
     * 
     * @return the sortingOptions
     */
    public ArrayList<SelectItem> getSortingOptions() {
        return sortingOptions;
    }

    /**
     * Sets the acknowledgeHostButtonEnabled.
     * 
     * @param acknowledgeHostButtonEnabled
     *            the acknowledgeHostButtonEnabled to set
     */
    public void setAcknowledgeHostButtonEnabled(
            boolean acknowledgeHostButtonEnabled) {
        this.acknowledgeHostButtonEnabled = acknowledgeHostButtonEnabled;
    }

    /**
     * Returns the acknowledgeHostButtonEnabled.
     * 
     * @return the acknowledgeHostButtonEnabled
     */
    public boolean isAcknowledgeHostButtonEnabled() {
        return acknowledgeHostButtonEnabled;
    }

    /**
     * Refreshes the seurat view based on the JMS message. Parsing is required
     * here since there is a user preference.
     * 
     * @param topicXML
     *            the topic xml
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String topicXML) {

        //LOGGER.debug("Started Processing request for thread :============> ");
        // if (topicXML == null) {
        // LOGGER
        // .debug("refresh() of Seurat Portlet : Received null XML Message.");
        // return;
        // }
        //
        // /*
        // * Get the JMS updates for xmlMessage & particular nodeType [For
        // * comments-portlet will be only HOST or SERVICE].
        // *
        // * Update messages each indicating - action, id , node-type.
        // */
        // List<JMSUpdate> jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(
        // topicXML, NodeType.HOST_GROUP);
        // if (jmsUpdates == null || jmsUpdates.isEmpty()) {
        // // no JMS Updates for selectedNodeType - return from here
        // return;
        // }
        //
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // /*
        // * If the nodeId matches with the enitiyID from jmsUpdates list,
        // * then only reload the data.
        // */
        // if (update.getId() == hostGroupId) {
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER
        // .debug("Refreshing Seurat Portlet data for HostGroup with Id ["
        // + hostGroupId + "]");
        // }
        // // refresh the seurat portlet
        // refreshSeurat();
        // /*
        // * Important: break from here - do not iterate on further
        // * updates from JMS as requirement has already been
        // * satisfied with one.
        // */
        // break;
        // } // end of if (update.getId() == selectedNodeId)
        // } // end of if (update != null)
        // } // end of for (JMSUpdate update : jmsUpdates)
    }

    /**
     * Helper to refresh the seurat.
     */
    private void refreshSeurat() {
        try {
            // populate Host list with hosts
            if (!populateList()) {
                return;
            }

            // sort list by selected order
            if (currentSortOption.equals(SeuratSortType.ALPHA.getOptionName())) {
                sortList(SeuratSortType.ALPHA);
            } else if (currentSortOption.equals(SeuratSortType.SEVERITY
                    .getOptionName())) {
                sortList(SeuratSortType.SEVERITY);
            } else {
                // else it is sort by LAST STATE CHANGE
                // -------------------------------------
                // Attention : if you are adding new sort
                // option, add one more condition here!

                /*
                 * first sorting by last sort option and then by selected option
                 * is done to avoid the unexpected "flickered" rearrangement
                 * (sorting) of hosts when non-alpha sort option is selected and
                 * JMS push happens. (bug #GWMON-6749)
                 */
                sortList(lastSortOption);
                sortList(SeuratSortType.STATE_CHANGE);
            }

        } catch (PreferencesException e) {
            handleInfo(e.getMessage());
            return;
        } catch (GWPortalException e) {
            handleError(e.getMessage());
            return;
        } catch (WSDataUnavailableException e) {
            handleError(e.getMessage());
            return;
        }
        // SessionRenderer.render(groupRenderName);
    }

    /**
     * Sets the ackAllServicesButtonEnabled.
     * 
     * @param ackAllServicesButtonEnabled
     *            the ackAllServicesButtonEnabled to set
     */
    public void setAckAllServicesButtonEnabled(
            boolean ackAllServicesButtonEnabled) {
        this.ackAllServicesButtonEnabled = ackAllServicesButtonEnabled;
    }

    /**
     * Returns the ackAllServicesButtonEnabled.
     * 
     * @return the ackAllServicesButtonEnabled
     */
    public boolean isAckAllServicesButtonEnabled() {
        return ackAllServicesButtonEnabled;
    }

    /**
     * Sets the seuratGrid.
     * 
     * @param seuratGrid
     *            the seuratGrid to set
     */
    public void setSeuratGrid(HtmlPanelGroup seuratGrid) {
        this.seuratGrid = seuratGrid;
    }

    /**
     * Returns the seuratGrid.
     * 
     * @return the seuratGrid
     */
    public HtmlPanelGroup getSeuratGrid() {
        return seuratGrid;
    }

    /**
     * Sets the reference tree model.
     * 
     * @param referenceTreeModel
     *            the new reference tree model
     */
    public void setReferenceTreeModel(ReferenceTreeMetaModel referenceTreeModel) {
        this.referenceTreeModel = referenceTreeModel;
    }

    /**
     * Gets the reference tree model.
     * 
     * @return the reference tree model
     */
    public ReferenceTreeMetaModel getReferenceTreeModel() {
        return referenceTreeModel;
    }

    /**
     * Sets the frmId.
     * 
     * @param frmId
     *            the frmId to set
     */
    public void setFrmId(String frmId) {
        this.frmId = frmId;
    }

    /**
     * Returns the frmId.
     * 
     * @return the frmId
     */
    public String getFrmId() {
        return frmId;
    }

    // unused methods

    /**
     * Returns corresponding Seurat status according to given
     * NetworkObjectStatusEnum If not found, it returns NO_STATUS.
     * 
     * @param networkObjectStatusEnum
     * 
     * @param monitorStatus
     * @return SeuratStatusEnum
     */
    // private SeuratStatusEnum getSeuratStatusFromMonitorStatus(
    // NetworkObjectStatusEnum status) {
    //
    // if (status == NetworkObjectStatusEnum.HOST_PENDING) {
    // return SeuratStatusEnum.SEURAT_HOST_PENDING;
    // }
    //
    // SeuratStatusEnum seuratStatusEnum = entityStatusMap.get(status
    // .getMonitorStatusName());
    // if (seuratStatusEnum != null) {
    // return seuratStatusEnum;
    // }
    // return SeuratStatusEnum.NO_STATUS;
    // }
}
