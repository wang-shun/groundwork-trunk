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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;
import org.groundwork.foundation.ws.api.WSService;

public class WSServiceServiceLocator extends Service implements org.groundwork.foundation.ws.impl.WSServiceService {

    public WSServiceServiceLocator() {
    }


    public WSServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSServiceServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wsservice
    private String wsservice_address = "http://localhost:8080/foundation-webapp/services/wsservice";

    public String getwsserviceAddress() {
        return wsservice_address;
    }

    // The WSDD service name defaults to the port name.
    private String wsserviceWSDDServiceName = "wsservice";

    public String getwsserviceWSDDServiceName() {
        return wsserviceWSDDServiceName;
    }

    public void setwsserviceWSDDServiceName(String name) {
        wsserviceWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return getwsservice();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return getwsservice(portAddress);
    }
    
    public WSService getwsservice() throws ServiceException {
       URL endpoint;
        try {
            endpoint = new URL(wsservice_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException(e);
        }
        return getwsservice(endpoint);
    }

    public WSService getwsservice(URL portAddress) throws ServiceException {
        try {
            ServiceSoapBindingStub _stub = new ServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getwsserviceWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setwsserviceEndpointAddress(String address) {
        wsservice_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (WSService.class.isAssignableFrom(serviceEndpointInterface)) {
                ServiceSoapBindingStub _stub = new ServiceSoapBindingStub(new URL(wsservice_address), this);
                _stub.setPortName(getwsserviceWSDDServiceName());
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
        String inputPortName = portName.getLocalPart();
        if ("wsservice".equalsIgnoreCase(inputPortName)) {
            return getwsservice();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public QName getServiceName() {
        return new QName("urn:fws", "WSServiceService");
    }

    private HashSet ports = null;

    public Iterator getPorts() {
        if (ports == null) {
            ports = new HashSet();
            ports.add(new QName("urn:fws", "wsservice"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws ServiceException {
        
        if ("wsservice".equalsIgnoreCase(portName)) {
            setwsserviceEndpointAddress(address);
        }
        else 
        { // Unknown Port Name
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
