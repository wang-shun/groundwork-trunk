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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.api.WSStatistics;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

public class StatisticsSoapBindingStub extends GWSoapBindingStub implements WSStatistics {
	Log log = LogFactory.getLog(this.getClass());
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[8];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getStatistics");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "statisticQueryType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticQueryType"), StatisticQueryType.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "applicationType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "org.groundwork.foundation.ws.impl.WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", ">WSFoundationException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getNagiosStatistics");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "nagiosStatisticsQueryType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "NagiosStatisticQueryType"), org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "org.groundwork.foundation.ws.impl.WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[1] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getStatisticsByString");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "type"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "appType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);        
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsByStringReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[2] = oper;    
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getNagiosStatisticsByString");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "type"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "value"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsByStringReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[3] = oper;         
        
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getHostAvailabilityForHostgroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "hostGroupName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        oper.setReturnClass(java.lang.Double.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsByDoubleReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[4] = oper;  
        
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceAvailabilityForHostgroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "hostGroupName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        oper.setReturnClass(java.lang.Double.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatisticsByDoubleReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[5] = oper; 
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceAvailabilityForServiceGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "serviceGroupName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        oper.setReturnClass(java.lang.Double.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getServiceAvailabilityForServiceGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"), 
                      true
                     ));
        _operations[6] = oper; 
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getGroupStatistics");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "statisticQueryType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticQueryType"), StatisticQueryType.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "filter"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter"), Filter.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "groupName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "applicationType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"));
        oper.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getGroupStatisticsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException"),
                      "org.groundwork.foundation.ws.impl.WSFoundationException",
                      new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", ">WSFoundationException"), 
                      true
                     ));
        _operations[7] = oper;
    }

    public StatisticsSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public StatisticsSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public StatisticsSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", ">WSFoundationException");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.WSFoundationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "BooleanProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.BooleanProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "CheckType");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.CheckType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Component");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.Component.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DateProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.DateProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Device");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.Device.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DoubleProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.DoubleProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ExceptionType");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.ExceptionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.Host.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroup");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.HostGroup.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostStatus");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.HostStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "IntegerProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.IntegerProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LogMessage");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.LogMessage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LongProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.LongProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.MonitorStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "NagiosStatisticProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "NagiosStatisticQueryType");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "OperationStatus");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.OperationStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Priority");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.Priority.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.PropertyTypeBinding.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ServiceStatus");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.ServiceStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Severity");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.Severity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StateType");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.StateType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.StatisticProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroupStatisticProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.HostGroupStatisticProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);
            
            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticQueryType");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.StatisticQueryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.StringProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TimeProperty");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.TimeProperty.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TypeRule");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.TypeRule.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection");
            cachedSerQNames.add(qName);
            cls = org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class;
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

    public WSFoundationCollection getStatistics(org.groundwork.foundation.ws.model.StatisticQueryType statisticQueryType, java.lang.String value, java.lang.String applicationType) throws java.rmi.RemoteException, org.groundwork.foundation.ws.model.impl.WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getStatistics"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {statisticQueryType, value, applicationType});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }
    }
    
    public WSFoundationCollection getGroupStatistics(org.groundwork.foundation.ws.model.StatisticQueryType statisticQueryType,Filter filter, java.lang.String groupName, java.lang.String applicationType) throws java.rmi.RemoteException, org.groundwork.foundation.ws.model.impl.WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getGroupStatistics"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {statisticQueryType, filter,groupName, applicationType});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }
    }

    public org.groundwork.foundation.ws.model.impl.WSFoundationCollection getNagiosStatistics(org.groundwork.foundation.ws.model.NagiosStatisticQueryType nagiosStatisticsQueryType, java.lang.String value) throws java.rmi.RemoteException, org.groundwork.foundation.ws.model.impl.WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getNagiosStatistics"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {       
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {nagiosStatisticsQueryType, value});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }
    }

    /**
     * Parameter conversion method allowing string parameters.  Specifically used by custom reporting
     * data extension.
     */
    public WSFoundationCollection getStatisticsByString(String type, 
            String value,
            String applicationType) 
    throws RemoteException, WSFoundationException
    {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        
        // Note:  We don't delegate to getStatistics b/c the ByString version returns a
    	// flatened set of StatisticProperty instances instead of a StateStatistics[]
        
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getStatisticsByString"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {type, value, applicationType});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }            
    }  
    
    public WSFoundationCollection getNagiosStatisticsByString(String type, 
            String value) 
    throws RemoteException, WSFoundationException
    {
        // Do parameter conversion then delegate
        org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType queryType = 
            org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.HOSTGROUPNAME;
        
        if (type != null) 
        {
            queryType = org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.fromValue(type);
        }       
        
        
        return getNagiosStatistics(queryType, value);        
    }      
    
    
    
    
    
    
    public double getHostAvailabilityForHostgroup(String hostGroupName) 
    throws RemoteException, WSFoundationException
    {
    	log.debug("StatisticsSoapBindingStub.getHostAvailabilityForHostgroup hostGroupName ["+hostGroupName+"]");
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        
        // Note:  We don't delegate to getStatistics b/c the ByString version returns a
    	// flatened set of StatisticProperty instances instead of a StateStatistics[]
        
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getHostAvailabilityForHostgroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {hostGroupName});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                	Double result = (Double) _resp;
                	
                    return  result.doubleValue();
                } catch (java.lang.Exception _exception) {
                    //return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                	throw new org.apache.axis.AxisFault("Error extracting getHostAvailabilityForHostgroup(["+hostGroupName+"])");
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }            
    }  
    
    

    
    public double getServiceAvailabilityForHostgroup(String hostGroupName) 
    throws RemoteException, WSFoundationException
    {
    	log.debug("StatisticsSoapBindingStub.getServiceAvailabilityForHostgroup hostGroupName ["+hostGroupName+"]");
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        
        // Note:  We don't delegate to getStatistics b/c the ByString version returns a
    	// flatened set of StatisticProperty instances instead of a StateStatistics[]
        
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getServiceAvailabilityForHostgroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {hostGroupName});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                	Double result = (Double) _resp;
                	
                    return  result.doubleValue();
                } catch (java.lang.Exception _exception) {
                    //return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                	throw new org.apache.axis.AxisFault("Error extracting getServiceAvailabilityForHostgroup(["+hostGroupName+"])");
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }            
    }  
    
    public double getServiceAvailabilityForServiceGroup(String serviceGroupName) 
    throws RemoteException, WSFoundationException
    {
    	log.debug("StatisticsSoapBindingStub.getServiceAvailabilityForServiceGroup serviceGroupName ["+serviceGroupName+"]");
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        
        // Note:  We don't delegate to getStatistics b/c the ByString version returns a
    	// flatened set of StatisticProperty instances instead of a StateStatistics[]
        
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wsstatistics");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws", "getServiceAvailabilityForServiceGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {        
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {serviceGroupName});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException)_resp;
            }
            else {
                extractAttachments(_call);
                try {
                	Double result = (Double) _resp;
                	
                    return  result.doubleValue();
                } catch (java.lang.Exception _exception) {
                    //return (org.groundwork.foundation.ws.model.impl.WSFoundationCollection) org.apache.axis.utils.JavaUtils.convert(_resp, org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
                	throw new org.apache.axis.AxisFault("Error extracting getServiceAvailabilityForServiceGroup(["+serviceGroupName+"])");
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                  throw (java.rmi.RemoteException) axisFaultException.detail;
                }
                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.model.impl.WSFoundationException) {
                  throw (org.groundwork.foundation.ws.model.impl.WSFoundationException) axisFaultException.detail;
                }
            }
            throw axisFaultException;
        }            
    }  
    
    
    
    
}
