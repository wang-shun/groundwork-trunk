package com.groundworkopensource.webapp.license.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import net.padlocksoftware.key.KeyManager;
import net.padlocksoftware.license.License;
import net.padlocksoftware.license.LicenseIO;

import org.apache.commons.codec.binary.Hex;

import com.groundworkopensource.webapp.license.hibernate.Customer;
import com.groundworkopensource.webapp.license.hibernate.LicenseKey;
import com.groundworkopensource.webapp.license.hibernate.OrderInfo;
import com.groundworkopensource.webapp.license.manager.LicenseKeyManager;
import com.groundworkopensource.webapp.license.manager.OrderManager;
import com.icesoft.faces.component.paneltabset.TabChangeEvent;

/**
 * Bean to generate the license.
 * 
 * @author arul
 * 
 */
public class LicenseManagementBean {

    /* License values Obfuscation variables */
    private static char[] xchars = { 0xA5, 0xD2, 0x69, 0xB4, 0x5A, 0x2D, 0x96,
            0x4B, 0 };

    // Hex encoding.
    // Why use 0123456789ABCDEF when lots more entropy is available?
    private static char[] echars = { 'n', 'b', 'T', 'F', 'm', 'H', 's', 'a',
            'L', 'd', 'J', 'i', 'Y', 'V', 'R', 'w' };

    private OrderManagementBean orderBean = null;

    private String key = null;

    private String generationType = "external";

    private Parameter[] paramsList = null;

    private String[] textboxParamNames = { "Product Version", "Product Name",
            "Install GUID", "SoftLimit Devices", "HardLimit Devices", "SKU" };

    private String[] dateParamNames = { "Start Date", "Expiry Date",
            "HardLimit Expiry Date" };

    private String[] booleanParamNames = { "IsNetworkServiceReqd" };

    /**
     * UI SelectItem list for OrderID
     */
    private ArrayList<SelectItem> orderIDList = new ArrayList<SelectItem>();
    /**
     * selectedOrderID
     */
    private String selectedOrderID;

    /**
     * networkServiceReqdItems
     */
    private SelectItem[] networkServiceReqdItems = null;

    /**
     * bean to dispaly order info on UI
     */
    DispalyOrder dispalyOrder = new DispalyOrder();

    /**
     * bean to dispaly license key on UI
     */
    private String displayLicenseKey = null;
    /**
     * supportKeyComment
     */
    private String supportKeyComment;

    /**
     * constructor
     */
    public LicenseManagementBean() {
        initParams();

    }

    private void initParams() {
        paramsList = new Parameter[textboxParamNames.length
                + dateParamNames.length + booleanParamNames.length];
        for (int i = 0; i < textboxParamNames.length; i++) {
            Parameter param = new Parameter();
            param.setName(textboxParamNames[i]);
            param.setType("textbox");
            paramsList[i] = param;
        }

        for (int i = 0; i < dateParamNames.length; i++) {
            Parameter param = new Parameter();
            param.setName(dateParamNames[i]);
            param.setType("date");
            paramsList[textboxParamNames.length + i] = param;
        }
        for (int i = 0; i < booleanParamNames.length; i++) {
            Parameter param = new Parameter();
            param.setName(booleanParamNames[i]);
            param.setType("boolean");
            paramsList[textboxParamNames.length + dateParamNames.length + i] = param;
        }

        networkServiceReqdItems = new SelectItem[2];
        SelectItem item1 = new SelectItem();
        item1.setLabel("Yes");
        item1.setValue("true");
        SelectItem item2 = new SelectItem();
        item2.setLabel("No");
        item2.setValue("false");
        networkServiceReqdItems[0] = item1;
        networkServiceReqdItems[1] = item2;
    }

    /**
     * Generates the license key
     * 
     * @param event
     */
    public void generateFlexLicenseKey(ActionEvent event) {

        if (orderBean.getOrderID() != null
                && orderBean.getOrderID().trim() != "") {
            StringBuffer validationRules = new StringBuffer();

            if (this.generationType.equals("external")) {
                // Validation for external is always all params. So build all
                validationRules
                        .append("param_1;param_3;param_5;param_6;param_7;param_8;param_12");
            }

            try {
                OrderInfo order = OrderManager
                        .findOrder(orderBean.getOrderID());
                if (order == null) {
                    FacesContext.getCurrentInstance().addMessage(
                            "OrderId is Invalid!",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "OrderId not found!", "Order not found !"));
                    return;
                } else {
                    // boolean isSkeletonKey = true;
                    KeyPair kp = KeyManager.createKeyPair(1024);
                    byte[] publicKey = kp.getPublic().getEncoded();
                    // Initialize with some junk
                    String productVersion = "7.0";
                    String productName = "GroundWork Monitor Enterprise Edition Quickstart";
                    String sku = "FLXQS1A";
                    String GUID = "XXXX";
                    // short devicesLimit = 0;
                    boolean netServiceReqd = true;
                    short softLimitDevices = 50;
                    short hardLimitDevices = 60;

                    Date startDate = Calendar.getInstance().getTime();
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_MONTH, 336);
                    Date expiryDate = now.getTime();
                    now.add(Calendar.DAY_OF_MONTH, 30);
                    Date hardLimitExpiryDate = now.getTime();
                    Byte isNetworkServiceRequired = 0;

                    if (this.generationType.equals("internal")) {

                        for (int i = 0; i < paramsList.length; i++) {
                            Parameter param = (Parameter) paramsList[i];
                            if (param.isSelected()) {
                                String paramName = param.getName();
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("Product Version")) {
                                    productVersion = (String) param.getValue();
                                    validationRules.append("param_1;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("Product Name")) {
                                    productName = (String) param.getValue();
                                    validationRules.append("param_4;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("Install GUID")) {
                                    GUID = (String) param.getValue();
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("SoftLimit Devices")) {
                                    softLimitDevices = (short) Integer
                                            .parseInt((String) param.getValue());
                                    validationRules.append("param_5;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("HardLimit Devices")) {
                                    hardLimitDevices = (short) Integer
                                            .parseInt((String) param.getValue());
                                    validationRules.append("param_6;");
                                }
                                if (paramName != null
                                        && paramName.equalsIgnoreCase("SKU")) {
                                    sku = (String) param.getValue();
                                    validationRules.append("param_2;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("Start Date")) {
                                    startDate = (Date) param.getValue();
                                    validationRules.append("param_12;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("Expiry Date")) {
                                    expiryDate = (Date) param.getValue();
                                    validationRules.append("param_7;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("HardLimit Expiry Date")) {
                                    hardLimitExpiryDate = (Date) param
                                            .getValue();
                                    validationRules.append("param_8;");
                                }
                                if (paramName != null
                                        && paramName
                                                .equalsIgnoreCase("IsNetworkServiceReqd")) {
                                    System.out.println(param.getValue());
                                    if (param.getValue() != null) {
                                        netServiceReqd = Boolean
                                                .parseBoolean(param.getValue()
                                                        .toString());
                                    }
                                    if (netServiceReqd)
                                        isNetworkServiceRequired = 1;
                                    validationRules.append("param_3;");
                                }
                            }
                        }
                    } else {
                        /* Default Quick Start order */
                        productVersion = orderBean.getProductVersion();
                        if (orderBean.isNetworkServiceReqd())
                            isNetworkServiceRequired = 1;
                        productName = orderBean.getProductName();
                        softLimitDevices = orderBean.getSoftLimitDevice();
                        hardLimitDevices = orderBean.getHardLimitDevice();
                        Date today = Calendar.getInstance().getTime();
                        startDate = today;
                        expiryDate = orderBean.getExpiryDate();
                        hardLimitExpiryDate = orderBean
                                .getHardLimitExpiryDate();
                        sku = orderBean.getSku();
                        GUID = orderBean.getBitRockInstallID();
                    }
                    License l = new License();

                    l.setStartDate(startDate);

                    l.setExpirationDate(hardLimitExpiryDate);
                    l.setExpireAfterFirstRun(hardLimitExpiryDate.getTime());
                    // param_1 is Version
                    l.addProperty("param_1", this.encrypt(productVersion));
                    // param_2 is Devices
                    l.addProperty("param_2", this.encrypt(sku));
                    // param_3 is NetworkServiceReqd
                    l.addProperty("param_3", this
                            .encrypt(isNetworkServiceRequired.toString()));
                    // param_4 is Product
                    l.addProperty("param_4", this.encrypt(productName));
                    // param_5 is SoftLimitDevices
                    l.addProperty("param_5", this.encrypt(String
                            .valueOf(softLimitDevices)));
                    // param_6 is hardLimitDevices
                    l.addProperty("param_6", this.encrypt(String
                            .valueOf(hardLimitDevices)));

                    // param_7 is SoftLimitExpirationDate
                    l.addProperty("param_7", this
                            .encrypt(DateFormat.getDateTimeInstance().format(
                                    expiryDate.getTime())));
                    // param_8 is HardLimitExpirationDate
                    l.addProperty("param_8", this.encrypt(DateFormat
                            .getDateTimeInstance().format(
                                    hardLimitExpiryDate.getTime())));
                    // param_9 is BitRockInstallID
                    l.addProperty("param_9", this.encrypt(GUID));
                    String encodedString = new String(Hex.encodeHex(publicKey));
                    // param_10 is PubKey
                    l.addProperty("param_10", encodedString);

                    // If no rules set, then create a expiration date rule
                    if (validationRules != null
                            && validationRules.toString().length() <= 0) {
                        validationRules.append("param_7;");
                    }
                    // param_11 is ValidationRules
                    l.addProperty("param_11", this.encrypt(validationRules
                            .toString()));

                    l
                            .addProperty("param_12", this.encrypt(DateFormat
                                    .getDateTimeInstance().format(
                                            startDate.getTime())));
                    l.addProperty("orderID", orderBean.getOrderID());
                    l.sign(kp.getPrivate());
                    this.key = this.generateLicenseString(l);
                    LicenseKey licKey = new LicenseKey();
                    licKey.setLicense(key);
                    order.setOrderInfoId(orderBean.getOrderID());
                    order.setProductVersion(orderBean.getProductVersion());
                    order.setProductName(orderBean.getProductName());
                    order.setSku(orderBean.getSku());
                    order.setExpiryDate(orderBean.getExpiryDate());
                    order.setSoftLimitDevice(orderBean.getSoftLimitDevice());
                    order.setHardLimitDevice(orderBean.getHardLimitDevice());
                    order.setHardLimitExpiryDate(orderBean
                            .getHardLimitExpiryDate());
                    order.setBitRockInstallId(orderBean.getBitRockInstallID());
                    Byte networkServiceRequired = 1;
                    if (!orderBean.isNetworkServiceReqd()) {
                        networkServiceRequired = 0;
                    }
                    order.setNetworkServiceRequired(networkServiceRequired);

                    licKey.setOrderInfo(order);
                    licKey.setCreationDate(Calendar.getInstance().getTime());
                    licKey.setComment(Calendar.getInstance().getTime()
                            .toString());
                    LicenseKeyManager.create(licKey);
                    FacesContext.getCurrentInstance().addMessage(
                            "License Key Created Successfully ",
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "License Key Created Successfully",
                                    "License Key Created Successfully "));

                } // end if

            } catch (Exception exc) {
                exc.printStackTrace();
            } // end try/catch
        } else {
            FacesContext.getCurrentInstance().addMessage(
                    "Please Select Order ID",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please Select Order ID", "Order Id is Invalid !"));
        } // end if
    }

    /**
     * Resets the input fields
     * 
     * @param event
     */
    public void reset(ActionEvent event) {
        this.orderBean.setOrderID(null);
        this.orderBean.setCustomerLastName(null);
        this.key = null;
        initParams();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Generates the license string
     * 
     * @param lic
     * @return
     */
    private String generateLicenseString(License lic) {
        String licString = null;
        if (lic != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                LicenseIO.exportLicense(lic, baos);
                licString = new String(baos.toByteArray(), "UTF-8");
            } catch (IOException exc) {
                FacesContext
                        .getCurrentInstance()
                        .addMessage(
                                "Error",
                                new FacesMessage(
                                        FacesMessage.SEVERITY_ERROR,
                                        "Error occurred while generating license. Please try again!",
                                        "Cannot export License"));
            } finally {
                try {
                    if (baos != null)
                        baos.close();
                } catch (IOException exc) {
                    exc.printStackTrace();
                } // end try/catch
            } // end finally
        } // end if
        return licString;
    }

    /**
     * Encrypts the properties
     * 
     * @param base
     * @return
     */
    public String encrypt(String base) {
        /*
         * char[] chars = base.toCharArray(); StringBuffer output = new
         * StringBuffer(); for (int i = 0; i < chars.length; i++) {
         * output.append(Integer.toHexString((int) chars[i])); } return
         * output.toString();
         */

        int len = base.length();
        int buflen = len * 2;
        char[] str = base.toCharArray();
        char[] buf = new char[buflen];
        int s;
        int x = 0;
        for (s = 0; s < len; ++s) {
            str[s] = (char) ((str[s] += str[s] << 4) ^ xchars[x++]);
            if (xchars[x] == 0)
                x = 0;
        }
        for (s = len - 1; --s >= 0;) {
            str[s] += str[s + 1];
        }
        for (s = 1; s < len; ++s) {
            str[s] += str[s - 1];
        }
        int l = len;
        int h = buflen;
        for (s = len; --s >= 0;) {
            buf[--l] = echars[str[s] & 0x000f];
            buf[--h] = echars[(str[s] & 0x00f0) >> 4];
        }
        return new String(buf);
    }

    public OrderManagementBean getOrderBean() {
        return orderBean;
    }

    public void setOrderBean(OrderManagementBean orderBean) {
        this.orderBean = orderBean;
    }

    public String getGenerationType() {
        return generationType;
    }

    public void setGenerationType(String generationType) {
        this.generationType = generationType;
    }

    public Parameter[] getParamsList() {
        return paramsList;
    }

    public void setParamsList(Parameter[] paramsList) {
        this.paramsList = paramsList;
    }

    public void genTypeSetter(FacesContext context, UIComponent validate,
            Object value) throws ValidatorException {
        this.generationType = (String) value;
    }

    /**
     * Sets the orderIDList.
     * 
     * @param orderIDList
     *            the orderIDList to set
     */
    public void setOrderIDList(ArrayList<SelectItem> orderIDList) {
        this.orderIDList = orderIDList;
    }

    /**
     * Returns the orderIDList.
     * 
     * @return the orderIDList
     */
    public ArrayList<SelectItem> getOrderIDList() {

        return orderIDList;
    }

    /**
     * 
     * @param e
     */
    public void tabSelection(TabChangeEvent e) {
        int newTabIndex = e.getNewTabIndex();
        // reset old values
        if (orderBean != null) {
            orderBean.reset(null);
        }
        // reset license management bean value
        this.reset(null);

        if (newTabIndex == 2 || newTabIndex == 1 || newTabIndex == 3) {
            orderIDList.clear();
            List<String> allOrderID = OrderManager.getAllOrderID();
            if (allOrderID != null) {
                Collections.sort(allOrderID);
            }
            for (Iterator<String> it = allOrderID.listIterator(); it.hasNext();) {
                Object next = it.next();
                orderIDList
                        .add(new SelectItem(next.toString(), next.toString()));

            }

        }
    }

    /**
     * Sets the selectedOrderID.
     * 
     * @param selectedOrderID
     *            the selectedOrderID to set
     */
    public void setSelectedOrderID(String selectedOrderID) {
        this.selectedOrderID = selectedOrderID;
    }

    /**
     * Returns the selectedOrderID.
     * 
     * @return the selectedOrderID
     */
    public String getSelectedOrderID() {
        return selectedOrderID;
    }

    /**
     * Generates the license key
     * 
     * @param event
     */
    public void generateLicenseKey(ActionEvent event) {

        if (selectedOrderID != null && selectedOrderID.trim() != "") {
            StringBuffer validationRules = new StringBuffer();

            try {
                OrderInfo order = OrderManager.findOrder(selectedOrderID);
                if (order == null) {
                    FacesContext.getCurrentInstance().addMessage(
                            "OrderId is Invalid!",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "OrderId not found!", "Order not found !"));
                    return;
                } else {
                    // boolean isSkeletonKey = true;
                    KeyPair kp = KeyManager.createKeyPair(1024);
                    byte[] publicKey = kp.getPublic().getEncoded();
                    // Initialize with some junk
                    String productVersion = "7.0";
                    String productName = "GroundWork Monitor Enterprise Edition Quickstart";
                    String sku = "FLXQS1A";
                    String GUID = "XXXX";
                    // short devicesLimit = 0;
                    boolean netServiceReqd = true;
                    short softLimitDevices = 50;
                    short hardLimitDevices = 60;
                    Date startDate = Calendar.getInstance().getTime();
                    Date expiryDate = Calendar.getInstance().getTime();
                    Date hardLimitExpiryDate = Calendar.getInstance().getTime();
                    Byte isNetworkServiceRequired = 0;

                    /* Quick start/Flex validation rules */
                    validationRules
                            .append("param_1;param_3;param_5;param_6;param_7;param_8;param_12");

                    productVersion = order.getProductVersion();

                    productName = order.getProductName();

                    if (order.getBitRockInstallId() != null)
                        GUID = order.getBitRockInstallId();

                    softLimitDevices = order.getSoftLimitDevice();
                    hardLimitDevices = order.getHardLimitDevice();

                    if (order.getSku() != null)
                        sku = order.getSku();

                    startDate = order.getStartDate();

                    expiryDate = order.getExpiryDate();

                    hardLimitExpiryDate = order.getHardLimitExpiryDate();

                    isNetworkServiceRequired = order
                            .getNetworkServiceRequired();

                    License l = new License();

                    l.setStartDate(startDate);

                    l.setExpirationDate(hardLimitExpiryDate);
                    l.setExpireAfterFirstRun(hardLimitExpiryDate.getTime());
                    // param_1 is Version
                    l.addProperty("param_1", this.encrypt(productVersion));
                    // param_2 is Devices
                    l.addProperty("param_2", this.encrypt(sku));
                    // param_3 is NetworkServiceReqd
                    l.addProperty("param_3", this
                            .encrypt(isNetworkServiceRequired.toString()));
                    // param_4 is Product
                    l.addProperty("param_4", this.encrypt(productName));
                    // param_5 is SoftLimitDevices
                    l.addProperty("param_5", this.encrypt(String
                            .valueOf(softLimitDevices)));
                    // param_6 is hardLimitDevices
                    l.addProperty("param_6", this.encrypt(String
                            .valueOf(hardLimitDevices)));

                    // param_7 is SoftLimitExpirationDate
                    l.addProperty("param_7", this
                            .encrypt(DateFormat.getDateTimeInstance().format(
                                    expiryDate.getTime())));
                    // param_8 is HardLimitExpirationDate
                    l.addProperty("param_8", this.encrypt(DateFormat
                            .getDateTimeInstance().format(
                                    hardLimitExpiryDate.getTime())));
                    // param_9 is BitRockInstallID
                    l.addProperty("param_9", this.encrypt(GUID));
                    String encodedString = new String(Hex.encodeHex(publicKey));
                    // param_10 is PubKey
                    l.addProperty("param_10", encodedString);

                    // If no rules set, then create a expiration date rule
                    if (validationRules != null
                            && validationRules.toString().length() <= 0) {
                        validationRules.append("param_7;");
                    }
                    // param_11 is ValidationRules
                    l.addProperty("param_11", this.encrypt(validationRules
                            .toString()));

                    l
                            .addProperty("param_12", this.encrypt(DateFormat
                                    .getDateTimeInstance().format(
                                            startDate.getTime())));
                    // add order ID
                    l.addProperty("orderID", order.getOrderInfoId());
                    l.sign(kp.getPrivate());
                    String generatedkey = this.generateLicenseString(l);
                    LicenseKey licKey = new LicenseKey();
                    licKey.setLicense(generatedkey);
                    // order.setOrderInfoId(order.getOrderID());
                    // order.setProductVersion(order.getProductVersion());
                    // order.setProductName(order.getProductName());
                    // order.setSku(order.getSku());
                    // order.setExpiryDate(order.getExpiryDate());
                    // order.setSoftLimitDevice(order.getSoftLimitDevice());
                    // order.setHardLimitDevice(order.getHardLimitDevice());
                    // order.setHardLimitExpiryDate(order
                    // .getHardLimitExpiryDate());
                    // order.setBitRockInstallId(order.getBitRockInstallId());
                    // Byte networkServiceRequired = 1;
                    // if (!orderBean.isNetworkServiceReqd()) {
                    // networkServiceRequired = 0;
                    // }
                    // order.setNetworkServiceRequired(networkServiceRequired);

                    licKey.setOrderInfo(order);
                    dispalyOrder.setLicenseKey(generatedkey);
                    licKey.setCreationDate(Calendar.getInstance().getTime());
                    licKey.setComment(Calendar.getInstance().getTime()
                            .toString());
                    LicenseKeyManager.create(licKey);
                    setDisplayOrderInfo(order);
                    FacesContext.getCurrentInstance().addMessage(
                            "License Key Created Successfully ",
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "License Key Created Successfully",
                                    "License Key Created Successfully "));
                } // end if

            } catch (Exception exc) {
                exc.printStackTrace();
            } // end try/catch
        } else {
            FacesContext.getCurrentInstance().addMessage(
                    "Please select Order ID",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select Order ID", "Order Id is Invalid !"));
        } // end if
    }

    /**
     * @param order
     */
    private void setDisplayOrderInfo(OrderInfo order) {
        dispalyOrder.setStartDate(order.getStartDate().toString());
        dispalyOrder.setExpiryDate(order.getExpiryDate().toString());
        dispalyOrder.setHardLimitDevice(order.getHardLimitDevice());
        dispalyOrder.setHardLimitExpiryDate(order.getHardLimitExpiryDate()
                .toString());

        dispalyOrder.setProductVersion(order.getProductVersion());
        dispalyOrder.setSoftLimitDevice(order.getSoftLimitDevice());
        if (order.getNetworkServiceRequired() == 1) {
            dispalyOrder.setNetworkServiceRequired("Yes");
        } else {
            dispalyOrder.setNetworkServiceRequired("No");
        }

    }

    /**
     * Returns the dispalyOrder.
     * 
     * @return the dispalyOrder
     */
    public DispalyOrder getDispalyOrder() {
        return dispalyOrder;
    }

    /**
     * Sets the dispalyOrder.
     * 
     * @param dispalyOrder
     *            the dispalyOrder to set
     */
    public void setDispalyOrder(DispalyOrder dispalyOrder) {
        this.dispalyOrder = dispalyOrder;
    }

    /**
     * Sets the networkServiceReqdItems.
     * 
     * @param networkServiceReqdItems
     *            the networkServiceReqdItems to set
     */
    public void setNetworkServiceReqdItems(SelectItem[] networkServiceReqdItems) {
        this.networkServiceReqdItems = networkServiceReqdItems;
    }

    /**
     * Returns the networkServiceReqdItems.
     * 
     * @return the networkServiceReqdItems
     */
    public SelectItem[] getNetworkServiceReqdItems() {
        return networkServiceReqdItems;
    }

    /**
     * @param event
     */
    public void flexOrderIdChangeListener(ValueChangeEvent event) {
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
            if (orderBean != null) {
                orderBean.setCustomerId(customer.getCustomerId());
                orderBean.setCustomerFirstName(customer.getFirstName());
                orderBean.setCustomerCompany(customer.getCompany());
                orderBean.setCustomerLastName(customer.getLastName());
                orderBean.setBitRockInstallID(existingOrder
                        .getBitRockInstallId());
                orderBean.setExpiryDate(existingOrder.getExpiryDate());
                orderBean
                        .setHardLimitDevice(existingOrder.getHardLimitDevice());
                orderBean.setHardLimitExpiryDate(existingOrder
                        .getHardLimitExpiryDate());
                if (existingOrder.getNetworkServiceRequired() == 1) {
                    orderBean.setNetworkServiceReqd(true);
                } else {
                    orderBean.setNetworkServiceReqd(false);
                }

                orderBean.setProductName(existingOrder.getProductName());
                orderBean.setProductVersion(existingOrder.getProductVersion());
                orderBean.setSku(existingOrder.getSku());
                orderBean
                        .setSoftLimitDevice(existingOrder.getSoftLimitDevice());
                orderBean.setStartDate(existingOrder.getStartDate());
                Parameter parameter = paramsList[0];
                parameter.setValue(existingOrder.getProductVersion());
                paramsList[0] = parameter;
            }
            // setting parameter value
            int paramListLength = textboxParamNames.length
                    + dateParamNames.length + booleanParamNames.length;
            if (paramsList != null && paramsList.length == paramListLength) {
                paramsList[0].setValue(existingOrder.getProductVersion());
                paramsList[1].setValue(existingOrder.getProductName());
                paramsList[2].setValue(existingOrder.getBitRockInstallId());
                paramsList[3].setValue(existingOrder.getSoftLimitDevice());
                paramsList[4].setValue(existingOrder.getHardLimitDevice());
                paramsList[5].setValue(existingOrder.getSku());
                paramsList[6].setValue(existingOrder.getStartDate());
                paramsList[7].setValue(existingOrder.getExpiryDate());
                paramsList[8].setValue(existingOrder.getHardLimitExpiryDate());
                if (existingOrder.getNetworkServiceRequired() == 1) {
                    paramsList[9].setValue("true");
                } else {
                    paramsList[9].setValue("false");
                }

            }
        }

    }

    /**
     * Generates the license key
     * 
     * @param event
     */
    public void generatesupportLicenseKey(ActionEvent event) {

        StringBuffer validationRules = new StringBuffer();
        OrderInfo order = null;
        try {

            // find order ID for Support key generation
            order = OrderManager.findOrder("support");
            // if order is null then create new order with orderID as support
            // and
            // customer last name as support.
            if (order == null) {
                order = new OrderInfo();
                order.setOrderInfoId("support");
                Customer cust = new Customer();
                cust.setLastName("support");
                order.setCustomer(cust);
                OrderManager.createOrder(order);
            }
            // boolean isSkeletonKey = true;
            KeyPair kp = KeyManager.createKeyPair(1024);
            byte[] publicKey = kp.getPublic().getEncoded();
            // Initialize with some junk
            String productVersion = "7.0";
            String productName = "GroundWork Monitor Enterprise Edition Quickstart";
            String sku = "FLXQS1A";
            String GUID = "XXXX";
            // short devicesLimit = 0;
            // boolean netServiceReqd = true;
            short softLimitDevices = 50;
            short hardLimitDevices = 60;
            Calendar now = Calendar.getInstance();
            // Start date: Today -1
            now.add(Calendar.DAY_OF_MONTH, -1);
            Date startDate = now.getTime();
            // End day soft: Today + 10
            now.add(Calendar.DAY_OF_MONTH, 11);
            Date expiryDate = now.getTime();
            // End day Hard: Today + 15
            now.add(Calendar.DAY_OF_MONTH, 5);
            Date hardLimitExpiryDate = now.getTime();
            Byte isNetworkServiceRequired = 0;

            /* Quick start/Flex validation rules */
            validationRules.append("param_7;param_8;param_12");

            License l = new License();

            l.setStartDate(startDate);

            l.setExpirationDate(hardLimitExpiryDate);
            l.setExpireAfterFirstRun(hardLimitExpiryDate.getTime());
            // param_1 is Version
            l.addProperty("param_1", this.encrypt(productVersion));
            // param_2 is Devices
            l.addProperty("param_2", this.encrypt(sku));
            // param_3 is NetworkServiceReqd
            l.addProperty("param_3", this.encrypt(isNetworkServiceRequired
                    .toString()));
            // param_4 is Product
            l.addProperty("param_4", this.encrypt(productName));
            // param_5 is SoftLimitDevices
            l.addProperty("param_5", this.encrypt(String
                    .valueOf(softLimitDevices)));
            // param_6 is hardLimitDevices
            l.addProperty("param_6", this.encrypt(String
                    .valueOf(hardLimitDevices)));

            // param_7 is SoftLimitExpirationDate
            l.addProperty("param_7", this.encrypt(DateFormat
                    .getDateTimeInstance().format(expiryDate.getTime())));
            // param_8 is HardLimitExpirationDate
            l.addProperty("param_8", this.encrypt(DateFormat
                    .getDateTimeInstance()
                    .format(hardLimitExpiryDate.getTime())));
            // param_9 is BitRockInstallID
            l.addProperty("param_9", this.encrypt(GUID));
            String encodedString = new String(Hex.encodeHex(publicKey));
            // param_10 is PubKey
            l.addProperty("param_10", encodedString);

            // If no rules set, then create a expiration date rule
            if (validationRules != null
                    && validationRules.toString().length() <= 0) {
                validationRules.append("param_7;");
            }
            // param_11 is ValidationRules
            l.addProperty("param_11", this.encrypt(validationRules.toString()));

            l.addProperty("param_12", this.encrypt(DateFormat
                    .getDateTimeInstance().format(startDate.getTime())));
            // add order ID
            l.addProperty("orderID", order.getOrderInfoId());
            l.sign(kp.getPrivate());
            String generatedkey = this.generateLicenseString(l);
            LicenseKey licKey = new LicenseKey();
            licKey.setLicense(generatedkey);
            licKey.setOrderInfo(order);
            licKey.setCreationDate(Calendar.getInstance().getTime());
            if (supportKeyComment == null
                    || supportKeyComment.trim().equalsIgnoreCase("")) {

                licKey.setComment(Calendar.getInstance().getTime().toString());
            } else {
                licKey.setComment(supportKeyComment);
            }

            LicenseKeyManager.create(licKey);
            this.setDisplayLicenseKey(generatedkey);
            // setDisplayOrderInfo(order);
            FacesContext.getCurrentInstance().addMessage(
                    "License Key Created Successfully ",
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "License Key Created Successfully",
                            "License Key Created Successfully "));

        } catch (Exception exc) {
            exc.printStackTrace();
        } // end try/catch

    }

    /**
     * Sets the displayLicenseKey.
     * 
     * @param displayLicenseKey
     *            the displayLicenseKey to set
     */
    public void setDisplayLicenseKey(String displayLicenseKey) {
        this.displayLicenseKey = displayLicenseKey;
    }

    /**
     * Returns the displayLicenseKey.
     * 
     * @return the displayLicenseKey
     */
    public String getDisplayLicenseKey() {
        return displayLicenseKey;
    }

    /**
     * Sets the supportKeyComment.
     * 
     * @param supportKeyComment
     *            the supportKeyComment to set
     */
    public void setSupportKeyComment(String supportKeyComment) {
        this.supportKeyComment = supportKeyComment;
    }

    /**
     * Returns the supportKeyComment.
     * 
     * @return the supportKeyComment
     */
    public String getSupportKeyComment() {
        return supportKeyComment;
    }

}
