/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.Call;

import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.ApplicationType;
import org.groundwork.foundation.ws.model.impl.EntityType;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

public class CommonSoapBindingStub extends GWSoapBindingStub
        implements org.groundwork.foundation.ws.api.WSCommon {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[14];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("login");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "userName"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "password"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "realUserName"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "loginReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("logout");
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAttributeData");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "type"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org",
                        "AttributeQueryType"),
                org.groundwork.foundation.ws.model.impl.AttributeQueryType.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getAttributeDataReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAttributeDataByString");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "type"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getAttributeDataByStringReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[3] = oper;

        /* canceQuery() and executeQuery() */
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelQuery");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "sessionID"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "cancelQueryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("executeQuery");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "sessionID"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "executeQueryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getEntityTypeProperties");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "entityType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "appType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "componentProperties"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "boolean"),
                Boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getEntityTypePropertiesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getEntityTypes");
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getEntityTypesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("performEntityQuery");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "entityType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "filter"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org", "Filter"),
                org.groundwork.foundation.ws.model.impl.Filter.class, false,
                false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "sort"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org", "Sort"),
                org.groundwork.foundation.ws.model.impl.Sort.class, false,
                false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "firstResult"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "maxResults"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper
                .setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "performEntityQueryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("performEntityCountQuery");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "entityType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "filter"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org", "Filter"),
                org.groundwork.foundation.ws.model.impl.Filter.class, false,
                false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(Integer.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "performEntityCountQueryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[9] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getActionsByApplicationType");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "appType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "inlcudeSystem"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "boolean"),
                boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getActionsByApplicationTypeReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getActionsByCriteria");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "filter"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org", "Filter"),
                org.groundwork.foundation.ws.model.impl.Filter.class, false,
                false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "sort"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org", "Sort"),
                org.groundwork.foundation.ws.model.impl.Sort.class, false,
                false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "firstResult"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "maxResults"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "getActionsByCriteriaReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("performActions");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "actionPerforms"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://model.ws.foundation.groundwork.org",
                        "ActionPerform[]"),
                org.groundwork.foundation.ws.model.impl.ActionPerform[].class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "performActionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("searchEntity");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "text"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "maxresults"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "extRoleServiceGroupList"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName("", "extRoleHostGroupList"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection"));
        oper.setReturnClass(WSFoundationCollection.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
                "searchEntityReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper
                .addFault(new org.apache.axis.description.FaultDesc(
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"),
                        "org.groundwork.foundation.ws.model.impl.WSFoundationException",
                        new javax.xml.namespace.QName(
                                "http://model.ws.foundation.groundwork.org",
                                "WSFoundationException"), true));
        _operations[13] = oper;
    }

    public CommonSoapBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public CommonSoapBindingStub(java.net.URL endpointURL,
            javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public CommonSoapBindingStub(javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service) super.service)
                .setTypeMappingVersion("1.2");
        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationException");
        cachedSerQNames.add(qName);
        cls = org.groundwork.foundation.ws.model.impl.WSFoundationException.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "WSFoundationCollection");
        cachedSerQNames.add(qName);
        cls = WSFoundationCollection.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "ExceptionType");
        cachedSerQNames.add(qName);
        cls = ExceptionType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "AttributeData");
        cachedSerQNames.add(qName);
        cls = org.groundwork.foundation.ws.model.impl.AttributeData.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "AttributeQueryType");
        cachedSerQNames.add(qName);
        cls = org.groundwork.foundation.ws.model.impl.AttributeQueryType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "PropertyDataType");
        cachedSerQNames.add(qName);
        cls = org.groundwork.foundation.ws.model.impl.PropertyDataType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "EntityType");
        cachedSerQNames.add(qName);
        cls = EntityType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "EntityTypeProperty");
        cachedSerQNames.add(qName);
        cls = EntityTypeProperty.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "Filter");
        cachedSerQNames.add(qName);
        cls = Filter.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "FilterOperator");
        cachedSerQNames.add(qName);
        cls = FilterOperator.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "Sort");
        cachedSerQNames.add(qName);
        cls = Sort.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "SortItem");
        cachedSerQNames.add(qName);
        cls = SortItem.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "PropertyTypeBinding");
        cachedSerQNames.add(qName);
        cls = PropertyTypeBinding.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "ApplicationType");
        cachedSerQNames.add(qName);
        cls = ApplicationType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "Action");
        cachedSerQNames.add(qName);
        cls = Action.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "StringProperty");
        cachedSerQNames.add(qName);
        cls = StringProperty.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "ActionPerform");
        cachedSerQNames.add(qName);
        cls = ActionPerform.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org", "ActionReturn");
        cachedSerQNames.add(qName);
        cls = ActionReturn.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall()
            throws java.rmi.RemoteException {
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
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses
                                .get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames
                                .get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class) cachedSerFactories
                                    .get(i);
                            java.lang.Class df = (java.lang.Class) cachedDeserFactories
                                    .get(i);
                            _call
                                    .registerTypeMapping(cls, qName, sf, df,
                                            false);
                        } else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory) cachedSerFactories
                                    .get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory) cachedDeserFactories
                                    .get(i);
                            _call
                                    .registerTypeMapping(cls, qName, sf, df,
                                            false);
                        }
                    }
                }
            }
            return _call;
        } catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault(
                    "Failure trying to get the Call object", _t);
        }
    }

    public java.lang.String login(java.lang.String userName,
            java.lang.String password, java.lang.String realUserName)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call
                .setOperationName(new javax.xml.namespace.QName("urn:fws",
                        "login"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    userName, password, realUserName });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (java.lang.String) _resp;
                } catch (java.lang.Exception _exception) {
                    return (java.lang.String) org.apache.axis.utils.JavaUtils
                            .convert(_resp, java.lang.String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public void logout() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "logout"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            }
            extractAttachments(_call);
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public WSFoundationCollection getAttributeData(
            org.groundwork.foundation.ws.model.impl.AttributeQueryType type)
            throws WSFoundationException, RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "getAttributeData"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call
                    .invoke(new java.lang.Object[] { type });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection getAttributeDataByString(String type)
            throws WSFoundationException, RemoteException {
        // Do conversion then delegate
        org.groundwork.foundation.ws.model.impl.AttributeQueryType queryType = org.groundwork.foundation.ws.model.impl.AttributeQueryType.APPLICATION_TYPES;

        if (type != null) {
            queryType = org.groundwork.foundation.ws.model.impl.AttributeQueryType
                    .fromValue(type);
        }

        return getAttributeData(queryType);
    }

    /* cancelQuery() and executeQuery() */
    public String cancelQuery(int sessionID) throws WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "cancelQuery"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call
                    .invoke(new java.lang.Object[] { sessionID });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (java.lang.String) _resp;
                } catch (java.lang.Exception _exception) {
                    return (java.lang.String) org.apache.axis.utils.JavaUtils
                            .convert(_resp, java.lang.String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public WSFoundationCollection executeQuery(int sessionID)
            throws WSFoundationException, RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "executeQuery"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call
                    .invoke(new java.lang.Object[] { sessionID });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean componentProperties)
            throws org.groundwork.foundation.ws.api.WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "getEntityTypeProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    entityType, appType, componentProperties });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection getEntityTypes()
            throws org.groundwork.foundation.ws.api.WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "getEntityTypes"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection performEntityQuery(String entityType,
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws org.groundwork.foundation.ws.api.WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "performEntityQuery"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    entityType, filter, sort, firstResult, maxResults });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public int performEntityCountQuery(String entityType, Filter filter)
            throws org.groundwork.foundation.ws.api.WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "performEntityCountQuery"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    entityType, filter });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (Integer) _resp;
                } catch (java.lang.Exception _exception) {
                    return (Integer) org.apache.axis.utils.JavaUtils.convert(
                            _resp, Integer.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    /**
     * Returns all actions related to the specified application type
     * 
     * @param appType
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean includeSystem) throws WSFoundationException,
            RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "getActionsByApplicationType"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    appType, includeSystem });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    /**
     * Returns all actions for the specified criteria and pagination parameters.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getActionsByCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "getActionsByCriteria"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    filter, sort, firstResult, maxResults });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws RemoteException, WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "performActions"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call
                    .invoke(new java.lang.Object[] { actionPerforms });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }

    public WSFoundationCollection searchEntity(String text, int maxresults,
            String extRoleServiceGroupList, String extRoleHostGroupList)
            throws RemoteException, WSFoundationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("/foundation-webapp/services/wscommon");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:fws",
                "searchEntity"));

        setRequestHeaders(_call);
        setAttachments(_call);

        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    text, maxresults, extRoleServiceGroupList,
                    extRoleHostGroupList });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (WSFoundationCollection) _resp;
                } catch (java.lang.Exception _exception) {
                    return (WSFoundationCollection) org.apache.axis.utils.JavaUtils
                            .convert(_resp, WSFoundationCollection.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            if (axisFaultException.detail != null) {
                if (axisFaultException.detail instanceof java.rmi.RemoteException) {
                    throw (java.rmi.RemoteException) axisFaultException.detail;
                }

                if (axisFaultException.detail instanceof org.groundwork.foundation.ws.api.WSFoundationException) {
                    throw (org.groundwork.foundation.ws.api.WSFoundationException) axisFaultException.detail;
                }
            }

            throw axisFaultException;
        }
    }
}
