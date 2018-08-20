/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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

import org.apache.axis.client.Call;
import org.groundwork.foundation.ws.api.WSRRD;

import java.rmi.RemoteException;

public class WSRRDServiceLocator extends org.apache.axis.client.Service implements org.groundwork.foundation.ws.impl.WSRRDService 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String PORT_NAME = "wsrrd";
    
    public WSRRDServiceLocator() {
    }


    public WSRRDServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSRRDServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for rrd
    private java.lang.String rrd_address = "http://localhost:8080/foundation-webapp/services/wsrrd";

    public java.lang.String getrrdAddress() {
        return rrd_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String rrdWSDDServiceName = PORT_NAME;

    public java.lang.String getrrdWSDDServiceName() {
        return rrdWSDDServiceName;
    }

    public void setrrdWSDDServiceName(java.lang.String name) {
       rrdWSDDServiceName = name;
    }

    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException
    {
        return getrrd();
    }
    
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        return getrrd(portAddress);
    }
    
    public WSRRD getrrd() throws javax.xml.rpc.ServiceException {
        return getrrd(null, null);
    }

    public WSRRD getrrd(String user, String password) throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(rrd_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getrrd(endpoint, user, password);
    }

    public WSRRD getrrd(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        return getrrd(portAddress, null, null);
    }

    public WSRRD getrrd(final java.net.URL portAddress, final String user, final String password) throws javax.xml.rpc.ServiceException {
        try {
            org.groundwork.foundation.ws.impl.RRDSoapBindingStub _stub = new org.groundwork.foundation.ws.impl.RRDSoapBindingStub(portAddress, this) {
                @Override
                protected Call createCall() throws RemoteException {
                    Call call = super.createCall();
                    if ((user != null) && (password != null)) {
                        _call.setProperty(Call.USERNAME_PROPERTY, user);
                        _call.setProperty(Call.PASSWORD_PROPERTY, password);
                    }
                    return _call;
                }
            };
            _stub.setPortName(getrrdWSDDServiceName()); 
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setrrdEndpointAddress(java.lang.String address) {
        rrd_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (WSRRD.class.isAssignableFrom(serviceEndpointInterface)) {
                org.groundwork.foundation.ws.impl.RRDSoapBindingStub _stub = new org.groundwork.foundation.ws.impl.RRDSoapBindingStub(new java.net.URL(rrd_address), this);
                _stub.setPortName(getrrdWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if (PORT_NAME.equalsIgnoreCase(inputPortName)) {
            return getrrd();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:fws", "WSRRDService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:fws", PORT_NAME));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
        if (PORT_NAME.equalsIgnoreCase(portName)) {
            setrrdEndpointAddress(address);
        }
        else 
        { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
