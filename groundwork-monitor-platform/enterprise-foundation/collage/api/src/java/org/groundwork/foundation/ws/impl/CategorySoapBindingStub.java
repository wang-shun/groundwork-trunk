package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.Call;

import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public class CategorySoapBindingStub extends GWSoapBindingStub
		implements WSCategory {

	private java.util.Vector cachedSerClasses = new java.util.Vector();
	private java.util.Vector cachedSerQNames = new java.util.Vector();
	private java.util.Vector cachedSerFactories = new java.util.Vector();
	private java.util.Vector cachedDeserFactories = new java.util.Vector();

	static org.apache.axis.description.OperationDesc[] _operations;

	static {
		_operations = new org.apache.axis.description.OperationDesc[6];
		_initOperationDesc1();
	}

	private static void _initOperationDesc1() {
		org.apache.axis.description.OperationDesc oper;
		org.apache.axis.description.ParameterDesc param;
		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getRootCategories");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "entityTypeName"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "startRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "endRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "orderBy"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"SortCriteria"),
				org.groundwork.foundation.ws.model.impl.SortCriteria.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "retrieveChildren"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "namePropertyOnly"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);

		oper.setReturnType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		oper
				.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
		oper.setReturnQName(new javax.xml.namespace.QName("",
				"getRootCategoriesReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"),
				"org.groundwork.foundation.ws.impl.WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[0] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getCategoryEntities");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "categoryName"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "entityName"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "startRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "endRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "orderBy"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"SortCriteria"),
				org.groundwork.foundation.ws.model.impl.SortCriteria.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "retrieveChildren"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "namePropertyOnly"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		oper
				.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
		oper.setReturnQName(new javax.xml.namespace.QName("",
				"getCategoryEntityReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"),
				"org.groundwork.foundation.ws.impl.WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[1] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getCategoryByName");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "categoryName"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "entityTypeName"),
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
				"getCategoryByNameReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"),
				"org.groundwork.foundation.ws.impl.WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[2] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getCategories");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "filter"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org", "Filter"),
				org.groundwork.foundation.ws.model.impl.Filter.class, false,
				false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "startRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "endRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "orderBy"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"SortCriteria"),
				org.groundwork.foundation.ws.model.impl.SortCriteria.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "retrieveChildren"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "namePropertyOnly"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);

		oper.setReturnType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		oper
				.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
		oper.setReturnQName(new javax.xml.namespace.QName("",
				"getCategoriesReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"),
				"org.groundwork.foundation.ws.impl.WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[3] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getCategoriesByEntityType");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "entityTypeName"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "startRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "endRange"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "orderBy"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"SortCriteria"),
				org.groundwork.foundation.ws.model.impl.SortCriteria.class,
				false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "retrieveChildren"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "namePropertyOnly"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, false, false);
		oper.addParameter(param);

		oper.setReturnType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		oper
				.setReturnClass(org.groundwork.foundation.ws.model.impl.WSFoundationCollection.class);
		oper.setReturnQName(new javax.xml.namespace.QName("",
				"getCategoriesByEntityTypeReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"),
				"org.groundwork.foundation.ws.impl.WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[4] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getCategoryById");
		param = new org.apache.axis.description.ParameterDesc(
				new javax.xml.namespace.QName("", "categoryId"),
				org.apache.axis.description.ParameterDesc.IN,
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org", "int"),
				int.class, false, false);
		oper.addParameter(param);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationCollection"));
		oper.setReturnClass(WSFoundationCollection.class);
		oper.setReturnQName(new javax.xml.namespace.QName("",
				"getCategoryByIdReturn"));
		oper.setStyle(org.apache.axis.constants.Style.RPC);
		oper.setUse(org.apache.axis.constants.Use.LITERAL);
		oper.addFault(new org.apache.axis.description.FaultDesc(
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), "WSFoundationException",
				new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"WSFoundationException"), true));
		_operations[5] = oper;

	}

	public CategorySoapBindingStub() throws org.apache.axis.AxisFault {
		this(null);
	}

	public CategorySoapBindingStub(java.net.URL endpointURL,
			javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
		this(service);
		super.cachedEndpoint = endpointURL;
	}

	public CategorySoapBindingStub(javax.xml.rpc.Service service)
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
		java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
		java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
		java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
		java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
		java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
		java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
		qName = new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"WSFoundationException");
		cachedSerQNames.add(qName);
		cls = org.groundwork.foundation.ws.model.impl.WSFoundationException.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

		qName = new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "SortCriteria");
		cachedSerQNames.add(qName);
		cls = org.groundwork.foundation.ws.model.impl.SortCriteria.class;
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
				"http://model.ws.foundation.groundwork.org", "Filter");
		cachedSerQNames.add(qName);
		cls = Filter.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);
		
		qName = new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "Category");
		cachedSerQNames.add(qName);
		cls = Category.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);
		
		qName = new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "CategoryEntity");
		cachedSerQNames.add(qName);
		cls = CategoryEntity.class;
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

	public WSFoundationCollection getRootCategories(String entityTypeName,
			int startRange, int endRange, SortCriteria orderBy,
			boolean retrieveChildren, boolean namePropertyOnly)
			throws WSFoundationException, RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[0]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getRootCategories"));
		
		setRequestHeaders(_call);
		setAttachments(_call);
		
		try {

			
			java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
					entityTypeName, startRange, endRange, orderBy,
					retrieveChildren, namePropertyOnly });
			
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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

	public WSFoundationCollection getCategoryEntities(String categoryName,
			String entityTypeName, int startRange, int endRange,
			SortCriteria orderBy, boolean retrieveChildren,
			boolean namePropertyOnly) throws java.rmi.RemoteException,
			WSFoundationException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[1]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getCategoryEntities"));

		setRequestHeaders(_call);
		setAttachments(_call);
		try {

			java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
					categoryName, entityTypeName, startRange, endRange,
					orderBy, retrieveChildren, namePropertyOnly });

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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

	public WSFoundationCollection getCategoryByName(
			java.lang.String categoryName, String entityTypeName)
			throws java.rmi.RemoteException, WSFoundationException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[2]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getCategoryByName"));

		setRequestHeaders(_call);
		setAttachments(_call);
		try {

			java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
					categoryName, entityTypeName });

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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

	public WSFoundationCollection getCategories(Filter filter,
			int startRange, int endRange,SortCriteria orderBy, boolean retrieveChildren,
			boolean namePropertyOnly) throws WSFoundationException,
			RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[3]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getCategories"));

		setRequestHeaders(_call);
		setAttachments(_call);
		try {

			java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
					filter,startRange, endRange, orderBy, retrieveChildren, namePropertyOnly });

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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

	public WSFoundationCollection getCategoriesByEntityType(
			String entityTypeName, int startRange, int endRange,SortCriteria orderBy,boolean retrieveChildren,
			boolean namePropertyOnly) throws WSFoundationException,
			RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[4]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getCategoryByEntityType"));

		setRequestHeaders(_call);
		setAttachments(_call);
		try {

			java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
					entityTypeName, startRange, endRange, orderBy, retrieveChildren, namePropertyOnly });

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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

	public WSFoundationCollection getCategoryById(int categoryId)
			throws RemoteException, WSFoundationException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[5]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("/foundation-webapp/services/wscategory");
		_call.setEncodingStyle(null);
		_call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		_call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName("urn:fws",
				"getCategoryById"));

		setRequestHeaders(_call);
		setAttachments(_call);
		try {

			java.lang.Object _resp = _call
					.invoke(new java.lang.Object[] { categoryId });

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

				if (axisFaultException.detail instanceof WSFoundationException) {
					throw (WSFoundationException) axisFaultException.detail;
				}
			}

			throw axisFaultException;
		}
	}

}
