package com.groundworkopensource.portal.statusviewer.bean.nagios;

import static com.groundworkopensource.portal.statusviewer.common.Constant.STRING_ZERO;

import java.io.Serializable;

import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * Client side bean which encapsulates properties like display
 * icon,label,tooltip etc.
 * 
 * @author shivangi_walvekar
 * 
 */
public class NagiosStatisticsProperty implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5976282269123061653L;

    /**
     * String property for count hosts for whom host statistics is enabled
     */
    private String hostStatisticEnabled = "0";

    /**
     * String property for count hosts for whom host statistics is disabled
     */
    private String hostStatisticDisabled = "0";

    /**
     * String property for count service for whom service statistics is enabled
     */
    private String serviceStatisticEnabled = "0";

    /**
     * String property for count service for whom service statistics is disabled
     */
    private String serviceStatisticDisabled = "0";

    /**
     * String property for name of the monitoring option
     */
    private String propertyName = Constant.EMPTY_STRING;

    /**
     * String property for icon of the monitoring option
     */
    private String icon = Constant.EMPTY_STRING;

    /**
     * String property for tool-tip text for a monitoring option.
     */
    private String tooltip = Constant.EMPTY_STRING;

    /**
     * String property for Row style class
     */
    private String cssClass = Constant.CSS_NAGIOS_GREEN_HEADER;

    /**
     * String property for style class
     */
    private String styleClass = Constant.ICE_DAT_TBL_TYP_D;

    /**
     * String property for Header style class iceDatTblRow1_typE
     */
    private String headerClass = Constant.ICE_DAT_TBL_COL_HDR1_TYP_D;

    /**
     * String property for Row style class
     */
    private String rowClass = Constant.ICE_DAT_TBL_COL_HDR1_TYP_D;

    /**
     * String property for column style class
     */
    private String columnClass = Constant.ICE_DAT_TBL_COL1_TYP_D;

    /**
     * Returns the CSS class to be applied to the panel grid for the monitoring
     * option panel group header.
     * 
     * @return cssClass
     */
    public String getCssClass() {
        if (isMonitoringOptionDisabled()) {
            cssClass = Constant.CSS_NAGIOS_GRAY_HEADER;
        } else {
            cssClass = Constant.CSS_NAGIOS_GREEN_HEADER;
        }
        return cssClass;
    }

    /**
     * @param cssClass
     */
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * Returns the styleClass.
     * 
     * @return the styleClass
     */
    public String getStyleClass() {
        if (isMonitoringOptionDisabled()) {
            styleClass = Constant.ICE_DAT_TBL_TYP_E;
        } else {
            styleClass = Constant.ICE_DAT_TBL_TYP_D;
        }

        return styleClass;
    }

    /**
     * Sets the styleClass.
     * 
     * @param styleClass
     *            the styleClass to set
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * @return tooltip
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * @param tooltip
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * boolean property to make the 'Disabled Hosts' link visible or invisible.
     */
    private boolean linkVisibleHosts = true;

    /**
     * boolean property to make the 'Disabled Services' link visible or
     * invisible.
     */
    private boolean linkVisibleServices = true;

    /**
     * boolean property indicating if the particular monitoring option is
     * enabled or disabled.
     */
    private boolean monitoringOptionDisabled;

    /**
     * @return monitoringOptionDisabled
     */
    public boolean isMonitoringOptionDisabled() {
        if (hostStatisticEnabled.equals(STRING_ZERO)
                && serviceStatisticEnabled.equals(STRING_ZERO)) {
            setMonitoringOptionDisabled(true);
            setIcon(NagiosStatisticsEnum.DISABLED.getIconPath());
            setCssClass(Constant.CSS_NAGIOS_GRAY_HEADER);
        }

        return monitoringOptionDisabled;
    }

    /**
     * @param monitoringOptionDisabled
     */
    public void setMonitoringOptionDisabled(boolean monitoringOptionDisabled) {
        this.monitoringOptionDisabled = monitoringOptionDisabled;
    }

    /**
     * @return linkVisibleHosts
     */
    public boolean isLinkVisibleHosts() {
        return linkVisibleHosts;
    }

    /**
     * @param linkVisibleHosts
     */
    public void setLinkVisibleHosts(boolean linkVisibleHosts) {
        this.linkVisibleHosts = linkVisibleHosts;
    }

    /**
     * @return linkVisibleServices
     */
    public boolean isLinkVisibleServices() {
        return linkVisibleServices;
    }

    /**
     * @param linkVisibleServices
     */
    public void setLinkVisibleServices(boolean linkVisibleServices) {
        this.linkVisibleServices = linkVisibleServices;
    }

    /**
     * @return icon
     */
    public String getIcon() {
        if (isMonitoringOptionDisabled()) {
            setIcon(NagiosStatisticsEnum.DISABLED.getIconPath());
        } else {
            setIcon(NagiosStatisticsEnum.ENABLED.getIconPath());
        }
        return icon;
    }

    /**
     * @param icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return hostStatisticEnabled\
     */
    public String getHostStatisticEnabled() {
        return hostStatisticEnabled;
    }

    /**
     * @param hostStatisticEnabled
     */
    public void setHostStatisticEnabled(String hostStatisticEnabled) {
        this.hostStatisticEnabled = hostStatisticEnabled;
    }

    /**
     * @return hostStatisticDisabled
     */
    public String getHostStatisticDisabled() {
        return hostStatisticDisabled;
    }

    /**
     * @param hostStatisticDisabled
     */
    public void setHostStatisticDisabled(String hostStatisticDisabled) {
        this.hostStatisticDisabled = hostStatisticDisabled;
    }

    /**
     * @return serviceStatisticEnabled
     */
    public String getServiceStatisticEnabled() {
        return serviceStatisticEnabled;
    }

    /**
     * @param serviceStatisticEnabled
     */
    public void setServiceStatisticEnabled(String serviceStatisticEnabled) {
        this.serviceStatisticEnabled = serviceStatisticEnabled;
    }

    /**
     * @return serviceStatisticDisabled
     */
    public String getServiceStatisticDisabled() {
        return serviceStatisticDisabled;
    }

    /**
     * @param serviceStatisticDisabled
     */
    public void setServiceStatisticDisabled(String serviceStatisticDisabled) {
        this.serviceStatisticDisabled = serviceStatisticDisabled;
    }

    /**
     * 
     */
    public NagiosStatisticsProperty() {
        // default constructor
    }

    /**
     * Sets the headerClass.
     * 
     * @param headerClass
     *            the headerClass to set
     */
    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
    }

    /**
     * Returns the headerClass.
     * 
     * @return the headerClass
     */
    public String getHeaderClass() {
        if (isMonitoringOptionDisabled()) {
            headerClass = Constant.ICE_DAT_TBL_COL_HDR1_TYP_E;
        } else {
            headerClass = Constant.ICE_DAT_TBL_COL_HDR1_TYP_D;
        }

        return headerClass;
    }

    /**
     * Sets the rowClass.
     * 
     * @param rowClass
     *            the rowClass to set
     */
    public void setRowClass(String rowClass) {
        this.rowClass = rowClass;
    }

    /**
     * Returns the rowClass.
     * 
     * @return the rowClass
     */
    public String getRowClass() {
        if (isMonitoringOptionDisabled()) {
            rowClass = Constant.ICE_DAT_TBL_ROW1_TYP_E;
        } else {
            rowClass = Constant.ICE_DAT_TBL_ROW1_TYP_D;
        }
        return rowClass;
    }

    /**
     * Sets the columnClass.
     * 
     * @param columnClass
     *            the columnClass to set
     */
    public void setColumnClass(String columnClass) {
        this.columnClass = columnClass;
    }

    /**
     * Returns the columnClass.
     * 
     * @return the columnClass
     */
    public String getColumnClass() {
        if (isMonitoringOptionDisabled()) {
            columnClass = Constant.ICE_DAT_TBL_COL1_TYP_E;
        } else {
            columnClass = Constant.ICE_DAT_TBL_COL1_TYP_D;
        }
        return columnClass;
    }
}
