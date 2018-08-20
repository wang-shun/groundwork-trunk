package com.groundworkopensource.portal.statusviewer.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * This Class represents the basic lightweight entity, that is used to build
 * Tree MetaModel. This model is used to build sub page specific Tree models,
 * storing and restoring states of these models.
 * 
 * @author nitin_jadhav
 * 
 */
public class NetworkMetaEntity implements Comparable<NetworkMetaEntity> {

    /**
     * Default Date format to be used in SimpleDateFormat Class
     */
    private static final String DEFAULT_DATE_FORMAT_STRING = "dd/MM/yy HH:mm:ss";

    /**
     * NOT_AVAILABLE constant
     */
    private static final String NOT_AVAILABLE = "Not Available";

    /**
     * Separator used between prefix and name to generate prefixed name.
     */
    private static final String PREFIX_NAME_SEPARATOR = ":";

    /**
     * status of node used for tree portlet
     * 
     * BUBBLE UP status in case of host portlet.
     * 
     */
    private NetworkObjectStatusEnum status;

    /**
     * type of node
     */
    private NodeType type;

    /**
     * Object ID
     */
    private Integer objectId;

    /**
     * list of children nodes
     */
    private List<Integer> childNodeList;

    /**
     * Custom Date format to be used in SimpleDateFormat Class.
     */
    private String dateFormatString;

    /**
     * application type name
     */
    private String appType;

    /**
     * display name prefix
     */
    private String prefix;

    /**
     * name
     */
    private String name;

    /**
     * ""MONITOR" status of node; as returned by web services.
     */
    private String monitorStatus;

    /**
     * tool tip text
     */
    private String toolTip;

    /**
     * Alias of this entity
     */
    private String alias;

    /**
     * *optional field* URL of this entity, to show on UI
     */
    private String url = Constant.EMPTY_STRING;

    /**
     * name of entity with extended info like name of service with host appended
     */
    private String extendedName;

    /**
     * Service Availability (for host only)
     */
    private double serviceAvailablityForHost;

    /**
     * is host/service acknowledged?
     */
    private boolean acknowledged;

    /**
     * Last state change date for host only)
     */
    private Date lastStateChange;

    /**
     * Date/time when last checked
     */
    private Date lastCheckDateTime;

    /**
     * Id of the parent. For example Host Id in case of Service.
     */
    private Integer parentId;
    
    /**
     *Comma-separated string of parent. For example in case of service local_mem_java on localhost, "Linux Servers,localhost"
     */
    private String parentListString;
    
    private boolean custom = false;
    
    private String summary = null;
    
    private int inScheduledDown = 0;
    
    private Date nextCheckDateTime;
    
    private String lastPluginOutputString = null;


    public String getLastPluginOutputString() {
		return lastPluginOutputString;
	}

	public void setLastPluginOutputString(String lastPluginOutputString) {
		this.lastPluginOutputString = lastPluginOutputString;
	}

	public int getInScheduledDown() {
		return inScheduledDown;
	}

	public void setInScheduledDown(int inScheduledDown) {
		this.inScheduledDown = inScheduledDown;
	}

	/**
     * Logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor without alias
     * 
     * @param objectId
     * @param prefix
     * @param name
     * @param appType
     * @param status
     * @param type
     * @param toolTip
     * @param lastCheckDateTime
     * @param childNodeList
     */
    public NetworkMetaEntity(Integer objectId, String prefix, String name,
            String appType, NetworkObjectStatusEnum status, NodeType type, String toolTip,
            Date lastCheckDateTime, List<Integer> childNodeList) {
        this.objectId = objectId;
        this.prefix = prefix;
        this.name = name;
        this.appType = appType;
        this.status = status;
        this.type = type;
        this.setToolTip(toolTip);
        this.lastCheckDateTime = lastCheckDateTime;
        this.childNodeList = childNodeList;
    }

    /**
     * Constructor for *host*, which includes additional service availability
     * and acknowledged fields.
     * 
     * @param objectId
     * @param prefix
     * @param name
     * @param appType
     * @param status
     * @param type
     * @param toolTip
     * @param lastCheckDateTime
     * @param childNodeList
     * @param serviceAvailability
     * @param acknowledged
     * @param lastStateChange
     */
    public NetworkMetaEntity(Integer objectId, String prefix, String name,
            String appType, NetworkObjectStatusEnum status, NodeType type, String toolTip,
            Date lastCheckDateTime, List<Integer> childNodeList,
            double serviceAvailability, boolean acknowledged,
            Date lastStateChange) {
        this.objectId = objectId;
        this.prefix = prefix;
        this.name = name;
        this.appType = appType;
        this.status = status;
        this.type = type;
        this.setToolTip(toolTip);
        this.lastCheckDateTime = lastCheckDateTime;
        this.childNodeList = childNodeList;
        this.serviceAvailablityForHost = serviceAvailability;
        this.acknowledged = acknowledged;
        this.lastStateChange = lastStateChange;
    }

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
    	if (lastCheckDateTime != null)
    		return (Date) lastCheckDateTime.clone();
    	else
    		return null;
    }

    /**
     * get object ID of this node
     * 
     * @return object Id
     */
    public Integer getObjectId() {
        return objectId;
    }

    /**
     * sets object Id of this node
     * 
     * @param objectId
     */
    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the application type of node.
     *
     * @return the application type or null
     */
    public String getAppType() {
        return appType;
    }

    /**
     * Sets the application type of node.
     *
     * @param appType application type or null
     */
    public void setAppType(String appType) {
        this.appType = appType;
    }

    /**
     * Returns the display name prefix of node.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
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
     * Returns prefixed name of node.
     *
     * @return prefixed name
     */
    public String getPrefixedName() {
        return ((name != null) ? ((prefix != null) ? prefix + PREFIX_NAME_SEPARATOR + name : name) : null);
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
     * Returns the type of this node.
     * 
     * @return the type
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Sets the type of this node.
     * 
     * @param type
     *            the type to set
     */
    public void setType(NodeType type) {
        this.type = type;
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
     * Comparison method for sorting collection of NetworkMetaEntity objects
     * alphabetically
     * 
     * @return comparison integer
     * @param networkMetaEntity
     * 
     *            (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(NetworkMetaEntity networkMetaEntity) {
        if (networkMetaEntity != null) {
            return this.name.compareToIgnoreCase(networkMetaEntity.getName());
        }
        logger.debug("null object send to compareTo method, returning 0");
        return 0;
    }

    /**
     * equals method (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof NetworkMetaEntity) {
            if (this.objectId.equals(((NetworkMetaEntity) object).objectId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hashcode (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * toString method (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
                .append("status", status)
                .append("type", type)
                .append("objectId", objectId)
                .append("childNodeList", childNodeList)
                .append("dateFormatString", dateFormatString)
                .append("prefix", prefix)
                .append("name", name)
                .append("monitorStatus", monitorStatus)
                .append("toolTip", toolTip)
                .append("alias", alias)
                .append("url", url)
                .append("extendedName", extendedName)
                .append("serviceAvailablityForHost", serviceAvailablityForHost)
                .append("acknowledged", acknowledged)
                .append("lastStateChange", lastStateChange)
                .append("lastCheckDateTime", lastCheckDateTime)
                .append("parentId", parentId)
                .append("parentListString", parentListString)
                .append("custom", custom)
                .append("summary", summary)
                .append("inScheduledDown", inScheduledDown)
                .append("nextCheckDateTime", nextCheckDateTime)
                .append("lastPluginOutputString", lastPluginOutputString)
                .toString();
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
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * Returns the toolTip of this node.
     * 
     * @return the toolTip
     */
    public String getToolTip() {
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
     * Sets the url.
     * 
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * build the URL for this Entity using NodeURLBuilder class and return.
     * 
     * @return the url
     */
    public String getUrl() {
        url = NodeURLBuilder.buildNodeURL(type, objectId, name, parentListString);
        return url;
    }

    /**
     * Sets the extendedName.
     * 
     * @param extendedName
     *            the extendedName to set
     */
    public void setExtendedName(String extendedName) {
        this.extendedName = extendedName;
    }

    /**
     * Returns the extendedName.
     * 
     * @return the extendedName
     */
    public String getExtendedName() {
        return extendedName;
    }

    /**
     * Returns prefixed extended name of node.
     *
     * @return prefixed extended name
     */
    public String getPrefixedExtendedName() {
        return ((extendedName != null) ? ((prefix != null) ? prefix + PREFIX_NAME_SEPARATOR + extendedName : extendedName) : null);
    }

    /**
     * Sets the serviceAvailablityForHost.
     * 
     * @param serviceAvailablityForHost
     *            the serviceAvailablityForHost to set
     */
    public void setServiceAvailablityForHost(double serviceAvailablityForHost) {
        this.serviceAvailablityForHost = serviceAvailablityForHost;
    }

    /**
     * Returns the serviceAvailablityForHost.
     * 
     * @return the serviceAvailablityForHost
     */
    public double getServiceAvailablityForHost() {
        return serviceAvailablityForHost;
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
    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    /**
     * Returns the lastStateChange.
     * 
     * @return the lastStateChange
     */
    public Date getLastStateChange() {
        return lastStateChange;
    }

    /**
     * Returns the ackText.
     * 
     * @return the ackText
     */
    public String getAckText() {
        if (status == NetworkObjectStatusEnum.SERVICE_OK
                || status == NetworkObjectStatusEnum.SERVICE_PENDING) {
            return Constant.NOT_AVAILABLE_STRING;
        } else if (acknowledged) {
            return Constant.YES_STRING;
        } else {
            return Constant.NO_STRING;
        }
    }

    /**
     * Sets the monitorStatus.
     * 
     * @param monitorStatus
     *            the monitorStatus to set
     */
    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }

    /**
     * Returns the monitorStatus.
     * 
     * @return the monitorStatus
     */
    public String getMonitorStatus() {
        return monitorStatus;
    }

    /**
     * Sets the parentId.
     * 
     * @param parentId
     *            the parentId to set
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the parentId.
     * 
     * @return the parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * Sets the parentListString.
     * @param parentListString the parentListString to set
     */
    public void setParentListString(String parentListString) {
        this.parentListString = parentListString;
    }

    /**
     * Returns the parentListString.
     * @return the parentListString
     */
    public String getParentListString() {
        return parentListString;
    }
    
    /**
     * Sets the custom flag.
     * @param custom the customflag to set
     */
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    /**
     * Returns the customflag.
     * @return the customflag
     */
    public boolean isCustom() {
        return custom;
    }

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Date getNextCheckDateTime() {
		return nextCheckDateTime;
	}

	public void setNextCheckDateTime(Date nextCheckDateTime) {
		this.nextCheckDateTime = nextCheckDateTime;
	}

	

}
