package org.groundwork.rs.utils;

public class LicenseInfo {

    /** The order id. */
    private String orderID = null;

    /** The install guid. */
    private String installGUID = null;

    /** The product name. */
    private String productName = null;

    /** The version. */
    private String version = null;

    /** The soft limit devices. */
    private String softLimitDevices = null;

    /** The hard limit devices. */
    private String hardLimitDevices = null;

    /** The start date. */
    private String startDate = null;

    /** The soft limit expiration date. */
    private String softLimitExpirationDate = null;

    /** The hard limit expiration date. */
    private String hardLimitExpirationDate = null;

    /** The validation rules. */
    private String validationRules = null;

    /** The network service reqd. */
    private String networkServiceReqd = null;

    /** The pub key. */
    private String pubKey = null;

    /** The sku. */
    private String sku = null;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getInstallGUID() {
        return installGUID;
    }

    public void setInstallGUID(String installGUID) {
        this.installGUID = installGUID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSoftLimitDevices() {
        return softLimitDevices;
    }

    public void setSoftLimitDevices(String softLimitDevices) {
        this.softLimitDevices = softLimitDevices;
    }

    public String getHardLimitDevices() {
        return hardLimitDevices;
    }

    public void setHardLimitDevices(String hardLimitDevices) {
        this.hardLimitDevices = hardLimitDevices;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getSoftLimitExpirationDate() {
        return softLimitExpirationDate;
    }

    public void setSoftLimitExpirationDate(String softLimitExpirationDate) {
        this.softLimitExpirationDate = softLimitExpirationDate;
    }

    public String getHardLimitExpirationDate() {
        return hardLimitExpirationDate;
    }

    public void setHardLimitExpirationDate(String hardLimitExpirationDate) {
        this.hardLimitExpirationDate = hardLimitExpirationDate;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public String getNetworkServiceReqd() {
        return networkServiceReqd;
    }

    public void setNetworkServiceReqd(String networkServiceReqd) {
        this.networkServiceReqd = networkServiceReqd;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getHardLimit() {
        return Integer.parseInt(this.getHardLimitDevices());
    }
    public int getSoftLimit() {
        return Integer.parseInt(this.getSoftLimitDevices());
    }
}
