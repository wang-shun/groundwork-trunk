package org.groundwork.foundation.ws.impl;

import java.net.URL;
import java.rmi.Remote;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.groundwork.foundation.ws.api.WSCategory;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Stub;

public class WSCategoryServiceLocator extends org.apache.axis.client.Service implements WSCategoryService {
	public WSCategoryServiceLocator() {
    }


    public WSCategoryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSCategoryServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wscategory
    private String wscategory_address = "http://localhost:8080/foundation-webapp/services/wscategory";

    public String getwscategoryAddress() {
        return wscategory_address;
    }

    // The WSDD service name defaults to the port name.
    private String wscategoryWSDDServiceName = "wscategory";

    public String getwscategoryWSDDServiceName() {
        return wscategoryWSDDServiceName;
    }

    public void setwscategoryWSDDServiceName(String name) {
        wscategoryWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return getwscategory();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return getwscategory(portAddress);
    }
    
    public WSCategory getwscategory() throws ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new URL(wscategory_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getwscategory(endpoint);
    }

    public WSCategory getwscategory(URL portAddress) throws ServiceException {
        try {
            CategorySoapBindingStub _stub = new CategorySoapBindingStub(portAddress, this);
            _stub.setPortName(getwscategoryWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setwscategoryEndpointAddress(String address) {
        wscategory_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (WSCategory.class.isAssignableFrom(serviceEndpointInterface)) {
                CategorySoapBindingStub _stub = new CategorySoapBindingStub(new URL(wscategory_address), this);
                _stub.setPortName(getwscategoryWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new ServiceException(t);
        }
        throw new ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public Remote getPort(QName portName, Class serviceEndpointInterface) throws ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("wscategory".equalsIgnoreCase(inputPortName)) {
            return getwscategory();
        }
        else  {
            Remote _stub = getPort(serviceEndpointInterface);
            ((Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public QName getServiceName() {
        return new QName("urn:fws", "WSCategoryService");
    }

    private HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new HashSet<QName>();
            ports.add(new QName("urn:fws", "wscategory"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws ServiceException {
        
        if ("wscategory".equalsIgnoreCase(portName)) {
            setwscategoryEndpointAddress(address);
        }
        else 
        { // Unknown Port Name
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port - " + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }
}
