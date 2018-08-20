package com.groundworkopensource.webapp.license.bean;

import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.groundworkopensource.webapp.license.hibernate.Customer;
import com.groundworkopensource.webapp.license.hibernate.OrderInfo;
import com.groundworkopensource.webapp.license.manager.OrderManager;

/**
 * Bean to generate the license.
 * 
 * @author arul
 * 
 */
public class OrderManagementBean {

    private String orderID = null;

    private String customerFirstName = null;

    private String customerLastName = null;

    private String customerCompany = null;

    private int customerId;

    private Date orderDate = null;

    private Date startDate = null;

    private Date expiryDate = null;

    private Date hardLimitExpiryDate = null;
    private String sku = "FLXQS1A";

    private short softLimitDevice = 50;
    private short hardLimitDevice = 60;
    private String productVersion = "7.0";
    private String productName = "GroundWork Monitor Enterprise Edition Quickstart";
    private boolean networkServiceReqd = true;
    private String bitRockInstallID = null;
    private String createdBy = null;
    private SelectItem[] networkServiceReqdItems = null;

    public OrderManagementBean() {
        networkServiceReqdItems = new SelectItem[2];
        SelectItem item1 = new SelectItem();
        item1.setLabel("Yes");
        item1.setValue("true");
        SelectItem item2 = new SelectItem();
        item2.setLabel("No");
        item2.setValue("false");
        networkServiceReqdItems[0] = item1;
        networkServiceReqdItems[1] = item2;
        startDate = Calendar.getInstance().getTime();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 336);
        expiryDate = now.getTime();
        now.add(Calendar.DAY_OF_MONTH, 30);
        hardLimitExpiryDate = now.getTime();
    }

    /**
     * Creates new order
     * 
     * @param event
     */
    public void findOrder(ActionEvent event) {
        if (orderID != null && customerLastName != null && orderID.trim() != ""
                && customerLastName.trim() != "") {

        } else {
            FacesContext
                    .getCurrentInstance()
                    .addMessage(
                            "InvalidInput",
                            new FacesMessage(
                                    "Invalid Order Details.Atleast Order ID and Last Name is required to lookup an Order!",
                                    "Order Info is Invalid !"));
        } // end if
    }

    /**
     * Creates new order
     * 
     * @param event
     */
    public void createOrder(ActionEvent event) {
        if (orderID != null && customerLastName != null && orderID.trim() != ""
                && customerLastName.trim() != "") {
            OrderInfo existingOrder = OrderManager.findOrder(orderID);
            // If order already exists, then throw error message
            if (existingOrder != null) {
                FacesContext.getCurrentInstance().addMessage(
                        "Order already exists for Order # : " + orderID,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Order already exists for Order # :  "
                                        + orderID,
                                "Order already exists for Order # :  "
                                        + orderID));
                return;
            } // end if
            OrderInfo order = new OrderInfo();
            order.setOrderInfoId(orderID);
            Customer cust = new Customer();
            cust.setFirstName(customerFirstName);
            cust.setLastName(customerLastName);
            cust.setCompany(customerCompany);
            order.setCustomer(cust);
            order.setProductVersion(productVersion);
            order.setProductName(productName);
            order.setSku(sku);
            order.setOrderDate(Calendar.getInstance().getTime());
            order.setStartDate(startDate);
            order.setExpiryDate(expiryDate);
            order.setSoftLimitDevice(softLimitDevice);
            order.setHardLimitDevice(hardLimitDevice);
            order.setHardLimitExpiryDate(hardLimitExpiryDate);
            order.setBitRockInstallId(bitRockInstallID);
            Byte networkServiceRequired = 1;
            if (!networkServiceReqd) {
                networkServiceRequired = 0;
            }
            order.setNetworkServiceRequired(networkServiceRequired);
            order.setModifiedDate(Calendar.getInstance().getTime());
            OrderManager.createOrder(order);
            FacesContext.getCurrentInstance().addMessage(
                    "Order Created Successfully. Order # : "
                            + order.getOrderInfoId(),
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Order Created Successfully. Order # : "
                                    + order.getOrderInfoId(),
                            "Order Created Successfully. Order # : "
                                    + order.getOrderInfoId()));
        } else {
            FacesContext
                    .getCurrentInstance()
                    .addMessage(
                            "InvalidInput",
                            new FacesMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    "Invalid Order Details.Atleast Order ID and Last Name is required to create an Order!",
                                    "Order Info is Invalid !"));
        } // end if
    }

    /**
     * Resets the input fields
     * 
     * @param event
     */
    public void reset(ActionEvent event) {
        this.orderID = null;
        this.customerFirstName = null;
        this.customerLastName = null;
        this.customerCompany = null;
        this.orderDate = null;
        this.startDate = Calendar.getInstance().getTime();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 336);
        this.expiryDate = now.getTime();
        now.add(Calendar.DAY_OF_MONTH, 30);
        this.hardLimitExpiryDate = now.getTime();
        this.sku = "FLXQS1A";
        this.softLimitDevice = 50;
        this.hardLimitDevice = 60;
        this.productVersion = "7.0";
        this.productName = "GroundWork Monitor Enterprise Edition Quickstart";
        this.networkServiceReqd = true;
        this.bitRockInstallID = null;
        this.createdBy = null;
    }

    /**
     * @return
     */
    public String getOrderID() {
        return orderID;
    }

    /**
     * @param orderID
     */
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    /**
     * @return
     */
    public String getCustomerFirstName() {
        return customerFirstName;
    }

    /**
     * @param customerFirstName
     */
    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    /**
     * @return
     */
    public String getCustomerLastName() {
        return customerLastName;
    }

    /**
     * @param customerLastName
     */
    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    /**
     * @return
     */
    public String getCustomerCompany() {
        return customerCompany;
    }

    /**
     * @param customerCompany
     */
    public void setCustomerCompany(String customerCompany) {
        this.customerCompany = customerCompany;
    }

    /**
     * @return
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * @param orderDate
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * @return
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startdate
     */
    public void setStartDate(Date startdate) {
        this.startDate = startdate;
    }

    /**
     * @return
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * @param expiryDate
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return
     */
    public Date getHardLimitExpiryDate() {
        return hardLimitExpiryDate;
    }

    /**
     * @param hardLimitExpiryDate
     */
    public void setHardLimitExpiryDate(Date hardLimitExpiryDate) {
        this.hardLimitExpiryDate = hardLimitExpiryDate;
    }

    /**
     * @return
     */
    public String getSku() {
        return sku;
    }

    /**
     * @param sku
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * @return
     */
    public short getSoftLimitDevice() {
        return softLimitDevice;
    }

    /**
     * @param softLimitDevice
     */
    public void setSoftLimitDevice(short softLimitDevice) {
        this.softLimitDevice = softLimitDevice;
    }

    /**
     * @return
     */
    public short getHardLimitDevice() {
        return hardLimitDevice;
    }

    /**
     * @param hardLimitDevice
     */
    public void setHardLimitDevice(short hardLimitDevice) {
        this.hardLimitDevice = hardLimitDevice;
    }

    /**
     * @return
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion
     */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * @return
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return
     */
    public boolean isNetworkServiceReqd() {
        return networkServiceReqd;
    }

    /**
     * @param networkServiceReqd
     */
    public void setNetworkServiceReqd(boolean networkServiceReqd) {
        this.networkServiceReqd = networkServiceReqd;
    }

    /**
     * returns bitRockInstallID
     * 
     * @return String
     */
    public String getBitRockInstallID() {
        return bitRockInstallID;
    }

    /**
     * sets the bitRockInstallID
     * 
     * @param bitRockInstallID
     */
    public void setBitRockInstallID(String bitRockInstallID) {
        this.bitRockInstallID = bitRockInstallID;
    }

    /**
     * returns createdBy
     * 
     * @return String
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * sets the createdBy
     * 
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * return networkServiceReqdItems.
     * 
     * @return SelectItem
     */
    public SelectItem[] getNetworkServiceReqdItems() {
        return networkServiceReqdItems;
    }

    /**
     * Sets the networkServiceReqdItems.
     * 
     * @param networkServiceReqdItems
     */
    public void setNetworkServiceReqdItems(SelectItem[] networkServiceReqdItems) {
        this.networkServiceReqdItems = networkServiceReqdItems;
    }

    /**
     * @param event
     */
    public void orderIdChangeListener(ValueChangeEvent event) {
        String newOrderID = (String) event.getNewValue();
        if (newOrderID == null || newOrderID.trim() == "") {
            FacesContext.getCurrentInstance().addMessage(
                    "Invalid OrderID",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Invalid Order Details.please select Order ID",
                            "Order Info is Invalid !"));
            return;
        }
        OrderInfo existingOrder = OrderManager.findOrder(newOrderID);
        if (existingOrder == null) {
            FacesContext.getCurrentInstance().addMessage(
                    "OrderId is Invalid!",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "OrderId not found!", "Order not found !"));
        } else {
            Customer customer = existingOrder.getCustomer();
            this.setCustomerId(customer.getCustomerId());
            this.setCustomerFirstName(customer.getFirstName());
            this.setCustomerCompany(customer.getCompany());
            this.setCustomerLastName(customer.getLastName());
            this.setBitRockInstallID(existingOrder.getBitRockInstallId());
            this.setExpiryDate(existingOrder.getExpiryDate());
            this.setHardLimitDevice(existingOrder.getHardLimitDevice());
            this.setHardLimitExpiryDate(existingOrder.getHardLimitExpiryDate());
            if (existingOrder.getNetworkServiceRequired() == 1) {
                this.setNetworkServiceReqd(true);
            } else {
                this.setNetworkServiceReqd(false);
            }

            this.setProductName(existingOrder.getProductName());
            this.setProductVersion(existingOrder.getProductVersion());
            this.setSku(existingOrder.getSku());
            this.setSoftLimitDevice(existingOrder.getSoftLimitDevice());
            this.setStartDate(existingOrder.getStartDate());

        }

    }

    /**
     * Update order
     * 
     * @param event
     */
    public void updateOrder(ActionEvent event) {
        if (orderID != null && customerLastName != null && orderID.trim() != ""
                && customerLastName.trim() != "") {

            OrderInfo order = new OrderInfo();
            order.setOrderInfoId(orderID);
            Customer cust = new Customer();
            cust.setFirstName(customerFirstName);
            cust.setLastName(customerLastName);
            cust.setCompany(customerCompany);
            cust.setCustomerId(customerId);
            order.setCustomer(cust);
            order.setProductVersion(productVersion);
            order.setProductName(productName);
            order.setSku(sku);
            order.setOrderDate(Calendar.getInstance().getTime());
            order.setStartDate(startDate);
            order.setExpiryDate(expiryDate);
            order.setSoftLimitDevice(softLimitDevice);
            order.setHardLimitDevice(hardLimitDevice);
            order.setHardLimitExpiryDate(hardLimitExpiryDate);
            order.setBitRockInstallId(bitRockInstallID);
            Byte networkServiceRequired = 1;
            if (!networkServiceReqd) {
                networkServiceRequired = 0;
            }
            order.setNetworkServiceRequired(networkServiceRequired);
            order.setModifiedDate(Calendar.getInstance().getTime());
            OrderManager.updateOrder(order);
            FacesContext.getCurrentInstance().addMessage(
                    "Order updated Successfully. Order # : "
                            + order.getOrderInfoId(),
                    new FacesMessage("Order updated Successfully. Order # : "
                            + order.getOrderInfoId(),
                            "Order updated Successfully. Order # : "
                                    + order.getOrderInfoId()));
        } else {
            FacesContext
                    .getCurrentInstance()
                    .addMessage(
                            "InvalidInput",
                            new FacesMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    "Invalid Order Details.Atleast Order ID and Last Name is required to create an Order!",
                                    "Order Info is Invalid !"));
        } // end if

    }

    /**
     * Sets the customerId.
     * 
     * @param customerId
     *            the customerId to set
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the customerId.
     * 
     * @return the customerId
     */
    public int getCustomerId() {
        return customerId;
    }

}
