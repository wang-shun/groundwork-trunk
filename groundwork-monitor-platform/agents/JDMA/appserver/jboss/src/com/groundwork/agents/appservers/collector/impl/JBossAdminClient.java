package com.groundwork.agents.appservers.collector.impl;

import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

import com.groundwork.agents.appservers.utils.StatUtils;

public class JBossAdminClient {

	public static String JBOSS_PROPERTIES = "gwos_jboss.xml";
	public static final String DEFAULT_JNDI_NAME = "jmx/invoker/RMIAdaptor";

	public static MBeanServerConnection createMBeanServerConnection()
			throws NamingException {
		InitialContext ctx;
		String adapterName = null;
		Properties properties = StatUtils
				.readProperties(JBossAdminClient.JBOSS_PROPERTIES);
		String serverURL = properties.getProperty("java.naming.provider.url");
		String contextFactory = properties
				.getProperty("java.naming.factory.initial");
		String factoryURLPkgs = properties
				.getProperty("java.naming.factory.url.pkgs");
		if (serverURL == null) {
			ctx = new InitialContext();
		} else {
			Hashtable props = new Hashtable(System.getProperties());
			props.put(Context.PROVIDER_URL, serverURL);
			props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
			props.put(Context.URL_PKG_PREFIXES, factoryURLPkgs);
			ctx = new InitialContext(props);
		}

		// if adapter is null, the use the default
		if (adapterName == null) {
			adapterName = DEFAULT_JNDI_NAME;
		}

		Object obj = ctx.lookup(adapterName);
		ctx.close();

		if (!(obj instanceof RMIAdaptor)) {
			throw new ClassCastException(
					"Object not of type: RMIAdaptorImpl, but: "
							+ (obj == null ? "not found" : obj.getClass()
									.getName()));
		}

		return (MBeanServerConnection) obj;
	}

	public static MBeanServerConnection createMBeanServerConnection(
			Properties properties) throws NamingException {
		InitialContext ctx;
		String adapterName = null;

		String serverURL = properties.getProperty("java.naming.provider.url");
		String contextFactory = properties
				.getProperty("java.naming.factory.initial");
		String factoryURLPkgs = properties
				.getProperty("java.naming.factory.url.pkgs");
		if (serverURL == null) {
			return null;
		} else {
			Hashtable props = new Hashtable(System.getProperties());
			props.put(Context.PROVIDER_URL, serverURL);
			props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
			props.put(Context.URL_PKG_PREFIXES, factoryURLPkgs);
			ctx = new InitialContext(props);
		}

		// if adapter is null, the use the default
		if (adapterName == null) {
			adapterName = DEFAULT_JNDI_NAME;
		}

		Object obj = ctx.lookup(adapterName);
		ctx.close();

		if (!(obj instanceof RMIAdaptor)) {
			throw new ClassCastException(
					"Object not of type: RMIAdaptorImpl, but: "
							+ (obj == null ? "not found" : obj.getClass()
									.getName()));
		}

		return (MBeanServerConnection) obj;
	}

}
