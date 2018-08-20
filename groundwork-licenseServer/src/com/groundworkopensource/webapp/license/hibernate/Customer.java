package com.groundworkopensource.webapp.license.hibernate;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class Customer implements Serializable {

    /** identifier field */
    private Integer customerId;

    /** nullable persistent field */
    private String firstName;

    /** persistent field */
    private String lastName;

    /** nullable persistent field */
    private String company;

    /** persistent field */
    private Set orderInfos;

    /** full constructor */
    public Customer(Integer customerId, String firstName, String lastName, String company, Set orderInfos) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.orderInfos = orderInfos;
    }

    /** default constructor */
    public Customer() {
    }

    /** minimal constructor */
    public Customer(Integer customerId, String lastName, Set orderInfos) {
        this.customerId = customerId;
        this.lastName = lastName;
        this.orderInfos = orderInfos;
    }

    public Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return this.company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Set getOrderInfos() {
        return this.orderInfos;
    }

    public void setOrderInfos(Set orderInfos) {
        this.orderInfos = orderInfos;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("customerId", getCustomerId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Customer) ) return false;
        Customer castOther = (Customer) other;
        return new EqualsBuilder()
            .append(this.getCustomerId(), castOther.getCustomerId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getCustomerId())
            .toHashCode();
    }

}
