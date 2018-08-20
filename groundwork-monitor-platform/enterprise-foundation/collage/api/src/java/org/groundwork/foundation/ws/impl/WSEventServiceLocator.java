/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl;

import java.net.URL;
import java.rmi.Remote;
import java.util.HashSet;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.groundwork.foundation.ws.api.WSEvent;

public class WSEventServiceLocator extends org.apache.axis.client.Service implements WSEventService {

    public WSEventServiceLocator() {
    }


    public WSEventServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSEventServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wsevent
    private String wsevent_address = "http://localhost:8080/foundation-webapp/services/wsevent";

    public String getwseventAddress() {
        return wsevent_address;
    }

    // The WSDD service name defaults to the port name.
    private String wseventWSDDServiceName = "wsevent";

    public String getwseventWSDDServiceName() {
        return wseventWSDDServiceName;
    }

    public void setwseventWSDDServiceName(String name) {
        wseventWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return getwsevent();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return getwsevent(portAddress);
    }
    
    public WSEvent getwsevent() throws ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new URL(wsevent_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getwsevent(endpoint);
    }

    public WSEvent getwsevent(URL portAddress) throws ServiceException {
        try {
            EventSoapBindingStub _stub = new EventSoapBindingStub(portAddress, this);
            _stub.setPortName(getwseventWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setwseventEndpointAddress(String address) {
        wsevent_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (WSEvent.class.isAssignableFrom(serviceEndpointInterface)) {
                EventSoapBindingStub _stub = new EventSoapBindingStub(new URL(wsevent_address), this);
                _stub.setPortName(getwseventWSDDServiceName());
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
        if ("wsevent".equalsIgnoreCase(inputPortName)) {
            return getwsevent();
        }
        else  {
            Remote _stub = getPort(serviceEndpointInterface);
            ((Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public QName getServiceName() {
        return new QName("urn:fws", "WSEventService");
    }

    private HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new HashSet<QName>();
            ports.add(new QName("urn:fws", "wsevent"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws ServiceException {
        
        if ("wsevent".equalsIgnoreCase(portName)) {
            setwseventEndpointAddress(address);
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
