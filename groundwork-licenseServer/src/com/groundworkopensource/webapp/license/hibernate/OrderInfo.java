package com.groundworkopensource.webapp.license.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class OrderInfo implements Serializable {

    /** identifier field */
    private String orderInfoId;

    /** nullable persistent field */
    private Date orderDate;

    /** nullable persistent field */
    private Date startDate;

    /** nullable persistent field */
    private Date expiryDate;

    /** nullable persistent field */
    private Date hardLimitExpiryDate;

    /** nullable persistent field */
    private String sku;

    /** nullable persistent field */
    private Short softLimitDevice;

    /** nullable persistent field */
    private Short hardLimitDevice;

    /** nullable persistent field */
    private String productVersion;

    /** nullable persistent field */
    private String productName;

    /** nullable persistent field */
    private Byte networkServiceRequired;

    /** nullable persistent field */
    private String bitRockInstallId;

    /** nullable persistent field */
    private Date modifiedDate;

    /** persistent field */
    private Customer customer;

    /** persistent field */
    private Set licenseKeys;

    /** full constructor */
    public OrderInfo(String orderInfoId, Date orderDate, Date startDate, Date expiryDate, Date hardLimitExpiryDate, String sku, Short softLimitDevice, Short hardLimitDevice, String productVersion, String productName, Byte networkServiceRequired, String bitRockInstallId, Date modifiedDate, Customer customer, Set licenseKeys) {
        this.orderInfoId = orderInfoId;
        this.orderDate = orderDate;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.hardLimitExpiryDate = hardLimitExpiryDate;
        this.sku = sku;
        this.softLimitDevice = softLimitDevice;
        this.hardLimitDevice = hardLimitDevice;
        this.productVersion = productVersion;
        this.productName = productName;
        this.networkServiceRequired = networkServiceRequired;
        this.bitRockInstallId = bitRockInstallId;
        this.modifiedDate = modifiedDate;
        this.customer = customer;
        this.licenseKeys = licenseKeys;
    }

    /** default constructor */
    public OrderInfo() {
    }

    /** minimal constructor */
    public OrderInfo(String orderInfoId, Customer customer, Set licenseKeys) {
        this.orderInfoId = orderInfoId;
        this.customer = customer;
        this.licenseKeys = licenseKeys;
    }

    public String getOrderInfoId() {
        return this.orderInfoId;
    }

    public void setOrderInfoId(String orderInfoId) {
        this.orderInfoId = orderInfoId;
    }

    public Date getOrderDate() {
        return this.orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getHardLimitExpiryDate() {
        return this.hardLimitExpiryDate;
    }

    public void setHardLimitExpiryDate(Date hardLimitExpiryDate) {
        this.hardLimitExpiryDate = hardLimitExpiryDate;
    }

    public String getSku() {
        return this.sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Short getSoftLimitDevice() {
        return this.softLimitDevice;
    }

    public void setSoftLimitDevice(Short softLimitDevice) {
        this.softLimitDevice = softLimitDevice;
    }

    public Short getHardLimitDevice() {
        return this.hardLimitDevice;
    }

    public void setHardLimitDevice(Short hardLimitDevice) {
        this.hardLimitDevice = hardLimitDevice;
    }

    public String getProductVersion() {
        return this.productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Byte getNetworkServiceRequired() {
        return this.networkServiceRequired;
    }

    public void setNetworkServiceRequired(Byte networkServiceRequired) {
        this.networkServiceRequired = networkServiceRequired;
    }

    public String getBitRockInstallId() {
        return this.bitRockInstallId;
    }

    public void setBitRockInstallId(String bitRockInstallId) {
        this.bitRockInstallId = bitRockInstallId;
    }

    public Date getModifiedDate() {
        return this.modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set getLicenseKeys() {
        return this.licenseKeys;
    }

    public void setLicenseKeys(Set licenseKeys) {
        this.licenseKeys = licenseKeys;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("orderInfoId", getOrderInfoId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof OrderInfo) ) return false;
        OrderInfo castOther = (OrderInfo) other;
        return new EqualsBuilder()
            .append(this.getOrderInfoId(), castOther.getOrderInfoId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getOrderInfoId())
            .toHashCode();
    }

}
