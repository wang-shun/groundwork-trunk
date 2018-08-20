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

import java.rmi.RemoteException;

import javax.xml.rpc.Call;

import org.groundwork.foundation.ws.api.WSDevice;
import org.groundwork.foundation.ws.model.DeviceQueryType;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.CheckType;
import org.groundwork.foundation.ws.model.impl.Component;
import org.groundwork.foundation.ws.model.impl.DateProperty;
import org.groundwork.foundation.ws.model.impl.Device;
import org.groundwork.foundation.ws.model.impl.DoubleProperty;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostStatus;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.groundwork.foundation.ws.model.impl.LogMessage;
import org.groundwork.foundation.ws.model.impl.LongProperty;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.OperationStatus;
import org.groundwork.foundation.ws.model.impl.Priority;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.Severity;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.StateType;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.TimeProperty;
import org.groundwork.foundation.ws.model.impl.TypeRule;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

public class DeviceSoapBindingStub extends GWSoapBindingStub implements WSDevice {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[3];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDevice");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "type"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DeviceQueryType"), org.groundwork.foundation.ws.model.impl.DeviceQueryType.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "startRange"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "endRange"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "orderedBy"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortCriteria"), org.groundwork.foundation.ws.model.impl.SortCriteria.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDeviceReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[0] = oper;
        
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDeviceByString");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "type"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "startRange"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "endRange"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sortOrder"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sortField"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDeviceReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[1] = oper;      
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDevicesByCriteria");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "filter"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter"), org.groundwork.foundation.ws.model.impl.Filter.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sort"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Sort"), org.groundwork.foundation.ws.model.impl.Sort.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "firstResult"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "maxResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDevicesByCriteriaReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[2] = oper;            
    }

    public DeviceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public DeviceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public DeviceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.WSFoundationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "BooleanProperty");
            cachedSerQNames.add(qName);
            cls = BooleanProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "CheckType");
            cachedSerQNames.add(qName);
            cls = CheckType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Component");
            cachedSerQNames.add(qName);
            cls = Component.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DateProperty");
            cachedSerQNames.add(qName);
            cls = DateProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Device");
            cachedSerQNames.add(qName);
            cls = Device.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DeviceQueryType");
            cachedSerQNames.add(qName);
            cls = DeviceQueryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DoubleProperty");
            cachedSerQNames.add(qName);
            cls = DoubleProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ExceptionType");
            cachedSerQNames.add(qName);
            cls = ExceptionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host");
            cachedSerQNames.add(qName);
            cls = Host.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroup");
            cachedSerQNames.add(qName);
            cls = HostGroup.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostStatus");
            cachedSerQNames.add(qName);
            cls = HostStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "IntegerProperty");
            cachedSerQNames.add(qName);
            cls = IntegerProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LogMessage");
            cachedSerQNames.add(qName);
            cls = LogMessage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LongProperty");
            cachedSerQNames.add(qName);
            cls = LongProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus");
            cachedSerQNames.add(qName);
            cls = MonitorStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "OperationStatus");
            cachedSerQNames.add(qName);
            cls = OperationStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Priority");
            cachedSerQNames.add(qName);
            cls = Priority.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding");
            cachedSerQNames.add(qName);
            cls = PropertyTypeBinding.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ServiceStatus");
            cachedSerQNames.add(qName);
            cls = ServiceStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Severity");
            cachedSerQNames.add(qName);
            cls = Severity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortCriteria");
            cachedSerQNames.add(qName);
            cls = SortCriteria.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StateType");
            cachedSerQNames.add(qName);
            cls = StateType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticProperty");
            cachedSerQNames.add(qName);
            cls = StatisticProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty");
            cachedSerQNames.add(qName);
            cls = StringProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TimeProperty");
            cachedSerQNames.add(qName);
            cls = TimeProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TypeRule");
            cachedSerQNames.add(qName);
            cls = TypeRule.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection");
            cachedSerQNames.add(qName);
            cls = WSFoundationCollection.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter");
            cachedSerQNames.add(qName);
            cls = Filter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);
            
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "FilterOperator");
            cachedSerQNames.add(qName);
            cls = FilterOperator.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);            
            
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Sort");
            cachedSerQNames.add(qName);
            cls = Sort.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);
            
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortItem");
            cachedSerQNames.add(qName);
            cls = SortItem.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);    
    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super.createCall();
        	if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public WSFoundationCollection getDevice(org.groundwork.foundation.ws.model.DeviceQueryType type, java.lang.String value, int startRange, int endRange, org.groundwork.foundation.ws.model.impl.SortCriteria orderedBy) throws java.rmi.RemoteException, org.groundwork.foundation.ws.model.impl.WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsdevice");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getDevice"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
             java.lang.Object _resp = _call.invoke(new java.lang.Object[] {type, value, new java.lang.Integer(startRange), new java.lang.Integer(endRange), orderedBy});
    
            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                      throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                
                if (axisFaultException.detail instanceof WSFoundationException) {
                  throw (WSFoundationException) axisFaultException.detail;
                }
            }
      
            throw axisFaultException;
        }
    }
    
    /**
     * Parameter conversion method allowing string parameters.  Specifically used by custom reporting
     * data extension.
     */
    public WSFoundationCollection getDeviceByString(String type, 
                                            String value,
                                            String startRange, 
                                            String endRange, 
                                            String sortOrder, 
                                            String sortField) 
    throws WSFoundationException, RemoteException
    {
        // Do conversion then delegate
        DeviceQueryType queryType = org.groundwork.foundation.ws.model.impl.DeviceQueryType.ALL;
        
        if (type != null) 
        {
            queryType = org.groundwork.foundation.ws.model.impl.DeviceQueryType.fromValue(type);
        }
        
        int intStartRange = 0;
        int intEndRange = 0;
        
        if (startRange != null && startRange.length() > 0)
        {
            try {
                intStartRange = Integer.parseInt(startRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        if (endRange != null && endRange.length() > 0)
        {
            try {
                intEndRange = Integer.parseInt(endRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        SortCriteria sortCriteria = null;
        if (sortOrder != null && sortOrder.trim().length() > 0 &&
            sortField != null && sortField.trim().length() > 0)
        {
            sortCriteria = new SortCriteria(sortOrder, sortField);
        }
        
        return getDevice(queryType, 
                        value,
                        intStartRange, 
                        intEndRange, 
                        sortCriteria);
    }
    
    public WSFoundationCollection getDevicesByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException
    {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsdevice");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getDevicesByCriteria"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
             java.lang.Object _resp = _call.invoke(new java.lang.Object[] {filter, sort, firstResult, maxResults});
        
             if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                      throw (java.rmi.RemoteException) axisFaultException.detail;
                 }
                
                if (axisFaultException.detail instanceof WSFoundationException) {
                      throw (WSFoundationException) axisFaultException.detail;
                 }
            }
          throw axisFaultException;
        }    	
    }    
}
