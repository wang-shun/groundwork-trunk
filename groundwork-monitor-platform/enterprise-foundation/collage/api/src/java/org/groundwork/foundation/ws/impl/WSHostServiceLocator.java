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

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;
import org.groundwork.foundation.ws.api.WSHost;

public class WSHostServiceLocator extends Service implements WSHostService {

    public WSHostServiceLocator() {
    }


    public WSHostServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public WSHostServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for host
    private String host_address = "http://localhost:8080/foundation-webapp/services/wshost";

    public String gethostAddress() {
        return host_address;
    }

    // The WSDD service name defaults to the port name.
    private String hostWSDDServiceName = "wshost";

    public String gethostWSDDServiceName() {
        return hostWSDDServiceName;
    }

    public void sethostWSDDServiceName(String name) {
        hostWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return gethost();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return gethost(portAddress);
    }
    
    public WSHost gethost() throws ServiceException {
       URL endpoint;
        try {
            endpoint = new URL(host_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException(e);
        }
        return gethost(endpoint);
    }

    public WSHost gethost(URL portAddress) throws ServiceException {
        try {
            HostSoapBindingStub _stub = new HostSoapBindingStub(portAddress, this);
            _stub.setPortName(gethostWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void sethostEndpointAddress(String address) {
        host_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (WSHost.class.isAssignableFrom(serviceEndpointInterface)) {
               HostSoapBindingStub _stub = new HostSoapBindingStub(new java.net.URL(host_address), this);
                _stub.setPortName(gethostWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
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
        if ("wshost".equalsIgnoreCase(inputPortName)) {
            return gethost();
        }
        else  {
            Remote _stub = getPort(serviceEndpointInterface);
            ((Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public QName getServiceName() {
        return new QName("urn:fws", "WSHostService");
    }

    private HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new QName("urn:fws", "wshost"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws ServiceException {
        
        if ("wshost".equalsIgnoreCase(portName)) {
            sethostEndpointAddress(address);
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
