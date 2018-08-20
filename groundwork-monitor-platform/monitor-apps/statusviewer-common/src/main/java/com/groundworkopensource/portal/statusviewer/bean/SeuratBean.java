package com.groundworkopensource.portal.statusviewer.bean;

import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.SeuratStatusEnum;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This Class represents the basic lightweight entity, It is used as generic
 * entity to populate lists for network objects like hosts and services. It also
 * implements sorting on those entities.
 * 
 * @author nitin_jadhav
 * 
 */
/**
 * @author nitin_jadhav
 * 
 */
/**
 * @author nitin_jadhav
 * 
 */
public class SeuratBean {

    /**
     * Default Date format to be used in SimpleDateFormat Class
     */
    private static final String DEFAULT_DATE_FORMAT_STRING = "dd/MM/yy HH:mm:ss";

    /**
     * Custom Date format to be used in SimpleDateFormat Class.
     */
    private String dateFormatString;
    /**
     * NOT_AVAILABLE constant
     */
    private static final String NOT_AVAILABLE = "Not Available";

    /**
     * Object ID
     */
    private int objectId;



    /**
     * name
     */
    private String name;


    public String getReadabletooltipstatustext() {
        return readabletooltipstatustext;
    }

    public void setReadabletooltipstatustext(String readabletooltipstatustext) {
        this.readabletooltipstatustext = readabletooltipstatustext;
    }

    private String readabletooltipstatustext;
    private String dynamicicon;
    private String lastavailableiconstatus;

    public String getLaststatustime() {
        return laststatustime;
    }

    public void setLaststatustime(String laststatustime) {
        this.laststatustime = laststatustime;
    }

    private String laststatustime;

    public String getLastavailableiconstatus() {
        return lastavailableiconstatus;
    }

    public void setLastavailableiconstatus(String lastavailableiconstatus) {
        this.lastavailableiconstatus = lastavailableiconstatus;
    }

    public String getDynamicicon() {
        return dynamicicon;
    }

    public void setDynamicicon(String dynamicicon) {
        this.dynamicicon = dynamicicon;
    }

    /**
     * status of node
     */
    private NetworkObjectStatusEnum status;

    /**
     * type of node
     */
    private NodeType type;

    /**
     * **optional** seurat status of node
     */
    private SeuratStatusEnum seuratStatus;

    /**
     * tool tip text
     */
    private List<String> toolTip;

    /**
     * Alias of this entity
     */
    private String alias;

    /**
     * whether or not this entity is acknowledged. false by default.
     */
    private boolean acknowledged = false;

    /**
     * The difference between current date/time and date/time when the host
     * changed status. used for sorting according to last state change time.
     */
    private long lastStateChange = 0;

    /**
     * Service application type
     */
    private String applicationType;

    /** reference model for building tree. */
    private ReferenceTreeMetaModel referenceTreeModel;

    /**
     * Localized String for "Yes"
     */
    private String yesString = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_yes");

    /**
     * Localized String for "No"
     */
    private String noString = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_no");

    /**
     * Localized String for "N/A"
     */
    private String notAvailableString = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_NA");

    /**
     * Constructor
     * 
     * @param objectId
     * @param name
     * @param status
     * @param lastStateChange
     * @param nodeType
     * @param hostGroupName
     * @param acknowledged,
     * @param applicationType
     * @param referenceTree
     * @param childList 
     */
    public SeuratBean(int objectId, String name, SeuratStatusEnum status,
                      long lastStateChange, NodeType nodeType, String hostGroupName, boolean acknowledged,
                      String applicationType, List<Integer> childList, ReferenceTreeMetaModel referenceTree) {
        super();
        this.objectId = objectId;
        this.name = name;
        this.seuratStatus = status;
        this.lastStateChange = lastStateChange;
        type = nodeType;
        this.acknowledged=acknowledged;
        this.childNodeList=childList;
        this.referenceTreeModel=referenceTree;
        // set up URL for this node
        url = NodeURLBuilder.buildNodeURL(type, objectId, name, hostGroupName);
    }

    public SeuratBean(int objectId, String name, SeuratStatusEnum status,
                      long lastStateChange, NodeType nodeType, String hostGroupName, boolean acknowledged,
                      String applicationType, List<Integer> childList, ReferenceTreeMetaModel referenceTree, String icontoassign, String tooltiplastupdated,
                      String readabletooltiptextvalue) {
        super();
        this.objectId = objectId;
        this.name = name;
        this.seuratStatus = status;
        this.lastStateChange = lastStateChange;
        type = nodeType;
        this.acknowledged=acknowledged;
        this.childNodeList=childList;
        this.referenceTreeModel=referenceTree;
        this.laststatustime = tooltiplastupdated;
        this.dynamicicon = icontoassign;
        this.readabletooltipstatustext = readabletooltiptextvalue;
        // set up URL for this node
        url = NodeURLBuilder.buildNodeURL(type, objectId, name, hostGroupName);
    }

    /**
     * Constructor
     *
     * @param objectId
     * @param name
     * @param seuratStatusEnum
     * @param isAcknowledged
     * @param applicationType
     * @param hostGroupName
     */
    public SeuratBean(int objectId, String name, SeuratStatusEnum seuratStatusEnum, boolean isAcknowledged,
                      String applicationType, String hostGroupName) {
        this.objectId = objectId;
        this.name = name;
        this.seuratStatus = seuratStatusEnum;
        this.acknowledged = isAcknowledged;
        this.applicationType = applicationType;
        // set up URL for this node
        url = NodeURLBuilder.buildNodeURL(NodeType.HOST, objectId, name,
                hostGroupName);
    }

    /**
     * Date/time when last checked
     */
    private Date lastCheckDateTime;

    /**
     * @param lastCheckDateTime
     *            the lastCheckDateTime to set
     */
    public void setLastCheckDateTime(Date lastCheckDateTime) {
        this.lastCheckDateTime = lastCheckDateTime;
    }

    /**
     * @return the lastCheckDateTime
     */
    public Date getLastCheckDateTime() {
        return (Date) lastCheckDateTime.clone();
    }

    /**
     * list of children nodes
     */
    private List<Integer> childNodeList;

    /**
     * URL associated with this node
     */
    private String url;

    /**
     * get object ID of this node
     * 
     * @return object Id
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * sets object Id of this node
     * 
     * @param objectId
     */
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the name of node.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of node.
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the status of this node.
     * 
     * @return the status
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the status of this node.
     * 
     * @param status
     *            the status to set
     */
    public void setStatus(NetworkObjectStatusEnum status) {
        this.status = status;
    }

    /**
     * Returns the childNodeList of this node.
     * 
     * @return the childNodeList
     */
    public List<Integer> getChildNodeList() {
        return childNodeList;
    }

    /**
     * Sets the childNodeList of this node.
     * 
     * @param childNodeList
     *            the childNodeList to set
     */
    public void setChildNodeList(List<Integer> childNodeList) {
        this.childNodeList = childNodeList;
    }

    /**
     * Returns Formatted String of last check date/time
     * 
     * @return String of last check date/time
     * @throws ParseException
     */
    public String getFormattedLastCheckForSearch() throws ParseException {
        // Required Format: : 12/15/08 17:43:43
        if (lastCheckDateTime == null) {
            return NOT_AVAILABLE;
        }
        // formatting date: if dateFormatString is s set by some one (say search
        // handler), use this format. otherwise use default date format
        SimpleDateFormat dateFormat;
        if (dateFormatString != null && dateFormatString != "") {
            dateFormat = new SimpleDateFormat(dateFormatString);
        } else {
            dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);
        }
        return dateFormat.format(lastCheckDateTime);
    }

    /**
     * returns Children Count
     * 
     * @return Children Count
     */
    public int getChildrenCount() {
        if (childNodeList != null) {
            return childNodeList.size();
        }
        return 0;
    }

    /**
     * Sets the toolTip of this node.
     * 
     * @param toolTip
     *            the toolTip to set
     */
    public void setToolTip(List<String> toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * Returns the toolTip of this node.
     * 
     * @return the toolTip
     */
    public List<String> getToolTip() {
        return toolTip;
    }

    /**
     * Sets the alias of this node.
     * 
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the alias of this node.
     * 
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the dateFormatString.
     * 
     * @param dateFormatString
     *            the dateFormatString to set
     */
    public void setDateFormatString(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    /**
     * Returns the dateFormatString.
     * 
     * @return the dateFormatString
     */
    public String getDateFormatString() {
        return dateFormatString;
    }

    /**
     * Sets the seuratStatus.
     * 
     * @param seuratStatus
     *            the seuratStatus to set
     */
    public void setSeuratStatus(SeuratStatusEnum seuratStatus) {
        this.seuratStatus = seuratStatus;
    }

    /**
     * Returns the seuratStatus.
     * 
     * @return the seuratStatus
     */
    public SeuratStatusEnum getSeuratStatus() {
        return seuratStatus;
    }

    /**
     * Sets the acknowledged.
     * 
     * @param acknowledged
     *            the acknowledged to set
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    /**
     * Returns the acknowledged.
     * 
     * @return the acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets the lastStateChange.
     * 
     * @param lastStateChange
     *            the lastStateChange to set
     */
    public void setLastStateChange(long lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    /**
     * Returns the lastStateChange.
     * 
     * @return the lastStateChange
     */
    public long getLastStateChange() {
        return lastStateChange;
    }

    /**
     * build the URL for this Entity using NodeURLBuilder class and return.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            the type to set
     */
    public void setType(NodeType type) {
        this.type = type;
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Returns the ackText.
     * 
     * @return the ackText
     */
    public String getAckText() {
        if (status == NetworkObjectStatusEnum.SERVICE_OK
                || status == NetworkObjectStatusEnum.SERVICE_PENDING) {
            return notAvailableString;
        } else if (acknowledged) {
            return yesString;
        } else {
            return noString;
        }
    }
    
    /**
     * Returns true is any service under host is still unacknowledged.
     * 
     * @return true is any service under host is still unacknowledged.
     */

    public boolean isAnyServiceUnacknowledged() {
        for (Integer serviceId : childNodeList) {
            NetworkMetaEntity serviceById = referenceTreeModel
                    .getServiceById(serviceId);
            if (null != serviceById && !serviceById.isAcknowledged()) {
                NetworkObjectStatusEnum status = serviceById.getStatus();
                if (!status.equals(NetworkObjectStatusEnum.SERVICE_OK)
                        && !status
                                .equals(NetworkObjectStatusEnum.SERVICE_PENDING)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if Host In UP Or Pending State (Monitor Status).
     * 
     * @return true if Host In UP Or Pending State (Monitor Status)
     */

    public boolean isHostInUpOrPendingState() {
        switch (seuratStatus) {
            case SEURAT_HOST_UP:
            case SEURAT_HOST_TROUBLED_100P:
            case SEURAT_HOST_TROUBLED_75P:
            case SEURAT_HOST_TROUBLED_50P:
            case SEURAT_HOST_TROUBLED_25P:
            case SEURAT_HOST_PENDING:
                return true;

            default:
                return false;
        }
    }

    /**
     * Returns the applicationType.
     *
     * @return the applicationType
     */
    public String getApplicationType() {
        return applicationType;
    }

    /**
     * Sets the applicationType.
     *
     * @param applicationType
     *            the applicationType to set
     */
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }
}
