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
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;
import org.groundwork.foundation.ws.api.WSHostGroup;

public class WSHostGroupServiceLocator extends Service implements WSHostGroupService {

    public WSHostGroupServiceLocator() {
    }


    public WSHostGroupServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public WSHostGroupServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wshostgroup
    private String wshostgroup_address = "http://localhost:8080/foundation-webapp/services/wshostgroup";

    public String getwshostgroupAddress() {
        return wshostgroup_address;
    }

    // The WSDD service name defaults to the port name.
    private String wshostgroupWSDDServiceName = "wshostgroup";

    public String getwshostgroupWSDDServiceName() {
        return wshostgroupWSDDServiceName;
    }

    public void setwshostgroupWSDDServiceName(String name) {
        wshostgroupWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return getwshostgroup();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return getwshostgroup(portAddress);
    }
   
    public WSHostGroup getwshostgroup() throws ServiceException {
       URL endpoint;
        try {
            endpoint = new URL(wshostgroup_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException(e);
        }
        return getwshostgroup(endpoint);
    }

    public WSHostGroup getwshostgroup(URL portAddress) throws ServiceException {
        try {
            HostgroupSoapBindingStub _stub = new HostgroupSoapBindingStub(portAddress, this);
            _stub.setPortName(getwshostgroupWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setwshostgroupEndpointAddress(String address) {
        wshostgroup_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (WSHostGroup.class.isAssignableFrom(serviceEndpointInterface)) {
                HostgroupSoapBindingStub _stub = new HostgroupSoapBindingStub(new URL(wshostgroup_address), this);
                _stub.setPortName(getwshostgroupWSDDServiceName());
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
        if ("wshostgroup".equalsIgnoreCase(inputPortName)) {
            return getwshostgroup();
        }
        else  {
            Remote _stub = getPort(serviceEndpointInterface);
            ((Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public QName getServiceName() {
        return new QName("urn:fws", "WSHostGroupService");
    }

    private HashSet<QName> ports = null;

    public Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new QName("urn:fws", "wshostgroup"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws ServiceException 
    {
        if ("wshostgroup".equalsIgnoreCase(portName)) {
            setwshostgroupEndpointAddress(address);
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
