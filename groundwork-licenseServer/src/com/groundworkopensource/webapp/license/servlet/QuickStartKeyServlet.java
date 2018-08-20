package com.groundworkopensource.webapp.license.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.padlocksoftware.key.KeyManager;
import net.padlocksoftware.license.License;
import net.padlocksoftware.license.LicenseIO;

import org.apache.commons.codec.binary.Hex;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.groundworkopensource.webapp.license.hibernate.Customer;
import com.groundworkopensource.webapp.license.hibernate.LicenseKey;
import com.groundworkopensource.webapp.license.hibernate.OrderInfo;
import com.groundworkopensource.webapp.license.manager.LicenseKeyManager;
import com.groundworkopensource.webapp.license.manager.OrderManager;

/**
 * @author manish_kjain
 * 
 */
public class QuickStartKeyServlet extends HttpServlet {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String customerFirstName = null;

    private String customerLastName = null;

    private String customerCompany = null;

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
    

    /* License values Obfuscation variables */
    private static char[] xchars = { 0xA5, 0xD2, 0x69, 0xB4, 0x5A, 0x2D, 0x96,
            0x4B, 0 };

    // Hex encoding.
    // Why use 0123456789ABCDEF when lots more entropy is available?
    private static char[] echars = { 'n', 'b', 'T', 'F', 'm', 'H', 's', 'a',
            'L', 'd', 'J', 'i', 'Y', 'V', 'R', 'w' };
    
    /*Eval customer counter */
    private UUID uniqueID = UUID.randomUUID();
    public AtomicInteger evalCutomerCounter = null;


    private short evalSoftDevice = 50;
    private short evalHardDevice = 60;
    
    private static final Logger LOGGER = Logger
	.getLogger(QuickStartKeyServlet.class.getName());
    
    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
    	if (uniqueID.hashCode() < 0)
    		evalCutomerCounter = new AtomicInteger((-1)*uniqueID.hashCode());
    	else
    		evalCutomerCounter = new AtomicInteger(uniqueID.hashCode());
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        
    	request.setCharacterEncoding("UTF-8");
    	response.setContentType("text/xml;charset=UTF-8"); //this is redundant
    	
    	PrintWriter out = response.getWriter();
        try {
            String orderID = request.getParameter("orderid");
            String customerName = request.getParameter("customer");
            
            if (customerName != null)
            {
            	customerName = new String(customerName.getBytes("ISO-8859-1"), "UTF-8");
            	LOGGER.error("Customer name converted [" + customerName + "]");
            }
            
            //LOGGER.error("Customer name UTF-8 [" + new String(customerName.getBytes(),"UTF-8") + "]");
            //LOGGER.error("Customer name ASCII[" + new String(customerName.getBytes("ISO-8859-1"), "UTF-8") );
            
            if (orderID != null && !orderID.trim().equalsIgnoreCase("")) {
                OrderInfo order = OrderManager.findOrder(orderID);
                if (order == null) {
                    // create new order
                    if (customerName != null) {
                        String[] result = customerName.split("\\s");
                        if (result != null) {
                            if (result.length > 1) {
                                customerFirstName = result[0];
                                customerLastName = result[1];

                            } else if (result.length == 1) {
                                customerLastName = result[0];
                            }

                        }

                    }
                    if (customerLastName != null) {
                    	OrderInfo orderInfo = new OrderInfo();
                        startDate = Calendar.getInstance().getTime();
                        Calendar now = Calendar.getInstance();
                        if (orderID.compareToIgnoreCase("EVAL0") == 0) {
                        	now.add(Calendar.DAY_OF_MONTH, 12);
                            expiryDate = now.getTime();
                            now.add(Calendar.DAY_OF_MONTH, 3);
                            hardLimitExpiryDate = now.getTime();
                            evalCutomerCounter.addAndGet(1);
                            orderID = orderID + evalCutomerCounter.toString();
                            orderInfo.setSku("FLXQS3T");        
                            orderInfo.setSoftLimitDevice(evalSoftDevice);
                            orderInfo.setHardLimitDevice(evalHardDevice);
                        }
                        else
                        {
                        	now.add(Calendar.DAY_OF_MONTH, 3650);
                            expiryDate = now.getTime();
                            now.add(Calendar.DAY_OF_MONTH, 30);
                            hardLimitExpiryDate = now.getTime();
                            orderInfo.setSku(sku);
                            orderInfo.setSoftLimitDevice(softLimitDevice);
                            orderInfo.setHardLimitDevice(hardLimitDevice);
                        }
                        orderInfo.setOrderInfoId(orderID);
                        Customer cust = new Customer();
                        cust.setFirstName(customerFirstName);
                        cust.setLastName(customerLastName);
                        cust.setCompany(customerCompany);
                        orderInfo.setCustomer(cust);
                        orderInfo.setProductVersion(productVersion);
                        orderInfo.setProductName(productName);
                        orderInfo
                                .setOrderDate(Calendar.getInstance().getTime());
                        orderInfo.setStartDate(startDate);
                        orderInfo.setExpiryDate(expiryDate);
                        orderInfo.setHardLimitExpiryDate(hardLimitExpiryDate);
                        orderInfo.setBitRockInstallId(bitRockInstallID);
                        Byte networkServiceRequired = 1;
                        if (!networkServiceReqd) {
                            networkServiceRequired = 0;
                        }
                        orderInfo
                                .setNetworkServiceRequired(networkServiceRequired);
                        orderInfo.setModifiedDate(Calendar.getInstance()
                                .getTime());
                        OrderManager.createOrder(orderInfo);

                    }
                }
                // create License Key
                String generatedLicenseKey = generateLicenseKey(orderID);
                // generate XML response
                this.GenerateXmlResponse(orderID, customerName,
                        generatedLicenseKey, out);

            } else {
                System.err.println("orderid is null");

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            out.close();
            out.flush();
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Generates the license key
     * 
     * @param orderID
     * @return String
     */
    public String generateLicenseKey(String orderID) {

        String key = null;
        if (orderID != null && orderID.trim() != "") {
        	/* Standard limitations: Date, product,devices */
//            StringBuffer validationRules = new StringBuffer(
//                    "param_1;param_3;param_5;param_6;param_7;param_8;param_12");
        	
            /* Core license key has no expiration date. Only limit to devices */
        	StringBuffer validationRules = new StringBuffer(
                    "param_1;param_3;param_5;param_6;param_12");


            try {
                OrderInfo order = OrderManager.findOrder(orderID);
                if (order == null) {

                    return null;
                } else {
                    String sku = "FLXQS1A";
                    String GUID = "XXXX";
                    Date startDate = Calendar.getInstance().getTime();
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_MONTH, 3650);
                    Date expiryDate = now.getTime();
                    now.add(Calendar.DAY_OF_MONTH, 30);
                    Date hardLimitExpiryDate = now.getTime();
                    String productName = "GroundWork Monitor Enterprise Edition Quickstart";
                    short softLimitDevices = 50;
                    short hardLimitDevices = 60;
                    Byte isNetworkServiceRequired = 1;
                    String productVersion = "7.0";
                    // boolean isSkeletonKey = true;
                    KeyPair kp = KeyManager.createKeyPair(1024);
                    byte[] publicKey = kp.getPublic().getEncoded();

                    if (order.getProductVersion() != null) {
                        productVersion = order.getProductVersion();
                    }
                    if (order.getProductName() != null) {
                        productName = order.getProductName();

                    }
                    if (order.getBitRockInstallId() != null) {
                        GUID = order.getBitRockInstallId();
                    }
                    softLimitDevices = order.getSoftLimitDevice();
                    hardLimitDevices = order.getHardLimitDevice();

                    if (order.getSku() != null) {
                        sku = order.getSku();

                    }

                    if (order.getStartDate() != null) {
                        startDate = order.getStartDate();

                    }

                    if (order.getExpiryDate() != null) {
                        expiryDate = order.getExpiryDate();

                    }

                    if (order.getHardLimitExpiryDate() != null) {
                        hardLimitExpiryDate = order.getHardLimitExpiryDate();

                    }

                    if (order.getNetworkServiceRequired() != null) {
                        isNetworkServiceRequired = order
                                .getNetworkServiceRequired();

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
                    l.addProperty("orderID", order.getOrderInfoId());
                    l.sign(kp.getPrivate());
                    key = this.generateLicenseString(l);
                    LicenseKey licKey = new LicenseKey();
                    licKey.setLicense(key);
                    licKey.setOrderInfo(order);
                    licKey.setCreationDate(Calendar.getInstance().getTime());
                    licKey.setComment(Calendar.getInstance().getTime()
                            .toString());
                    // TODO:check create operation is Successful or not
                    LicenseKeyManager.create(licKey);

                } // end if

            } catch (Exception exc) {
                exc.printStackTrace();
            } // end try/catch
        } else {
            System.err.println("Order id is not valid");
        } // end if

        return key;
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
        //return new String(buf) + "*Not for production*";
        return new String(buf);
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
                if (licString != null)
                    licString = licString.trim();
            } catch (IOException exc) {
                // TODO:
                exc.printStackTrace();
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
     * Generate XML response
     * 
     * @param orderId
     * @param customerName
     * @param key
     * @param out
     */
    private void GenerateXmlResponse(String orderId, String customerName,
            String key, PrintWriter out) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("LicenseKeyDetail");
            document.appendChild(rootElement);
            Element orderIdElement = document.createElement("OrderId");
            orderIdElement.appendChild(document.createTextNode(orderId));
            rootElement.appendChild(orderIdElement);
            
            String strUTF8Name = null;
            try{
            	strUTF8Name = new String(customerName.getBytes("UTF-8"),"UTF-8");         	
            }
            catch(UnsupportedEncodingException uce)
            {
            	strUTF8Name=customerName;
            }

            Element customerNameElement = document
                    .createElement("CustomerName");
            customerNameElement.appendChild(document
                    .createTextNode(strUTF8Name) );
            rootElement.appendChild(customerNameElement);

            Element keyElement = document.createElement("LicenseKey");
            keyElement.appendChild(document.createTextNode(key));
            rootElement.appendChild(keyElement);

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer;

            transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}