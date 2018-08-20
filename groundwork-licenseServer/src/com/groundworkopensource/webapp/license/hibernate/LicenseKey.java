package com.groundworkopensource.webapp.license.hibernate;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class LicenseKey implements Serializable {

    /** identifier field */
    private Integer licenseKeyId;

    /** persistent field */
    private String license;

    /** nullable persistent field */
    private Date creationDate;

    /** persistent field */
    private OrderInfo orderInfo;

    private String comment;

    /** full constructor */
    public LicenseKey(Integer licenseKeyId, String license, Date creationDate,
            OrderInfo orderInfo) {
        this.licenseKeyId = licenseKeyId;
        this.license = license;
        this.creationDate = creationDate;
        this.orderInfo = orderInfo;
    }

    /** default constructor */
    public LicenseKey() {
    }

    /** minimal constructor */
    public LicenseKey(Integer licenseKeyId, String license, OrderInfo orderInfo) {
        this.licenseKeyId = licenseKeyId;
        this.license = license;
        this.orderInfo = orderInfo;
    }

    public Integer getLicenseKeyId() {
        return this.licenseKeyId;
    }

    public void setLicenseKeyId(Integer licenseKeyId) {
        this.licenseKeyId = licenseKeyId;
    }

    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public OrderInfo getOrderInfo() {
        return this.orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String toString() {
        return new ToStringBuilder(this).append("licenseKeyId",
                getLicenseKeyId()).toString();
    }

    public boolean equals(Object other) {
        if (!(other instanceof LicenseKey))
            return false;
        LicenseKey castOther = (LicenseKey) other;
        return new EqualsBuilder().append(this.getLicenseKeyId(),
                castOther.getLicenseKeyId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getLicenseKeyId()).toHashCode();
    }

    /**
     * Sets the comment.
     * 
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment.
     * 
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

}
