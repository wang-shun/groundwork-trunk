/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
 */

package com.groundwork.agents.vema.configuration;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.exception.VEMAConfigurationException;
import com.groundwork.agents.vema.api.VemaConstants;

/**
 * 
 * @author rruttimann@gwos.com Helper class to maintain a list of active
 *         configuration metrics. The class maintains a list of metrics for
 *         hypervisors and vm (guests) that are actively measured.
 * 
 */

public class VEMAMonitoringProfile
{
	/**
	 * Data structures to store lsit of metrics for Hypervisors and VM
	 */

	private ConcurrentHashMap<String, ConfigurationSettings>	metricsForHypervisors		= new ConcurrentHashMap<String, ConfigurationSettings>();
	private ConcurrentHashMap<String, ConfigurationSettings>	metricsForVM				= new ConcurrentHashMap<String, ConfigurationSettings>();

	public static String										TYPE_HYPERVISOR				= "hypervisor";
	public static String										TYPE_VM						= "virtual-machine";

	private static String										DEFAULT_PROFILE_PATH		= "/usr/local/groundwork/config";

	/* Private members */
	private String												pathToMonitoringProfile		= DEFAULT_PROFILE_PATH;
	private String												monitorProfileName			= null;

	private static org.apache.log4j.Logger						log							= Logger.getLogger( VEMAMonitoringProfile.class );

	/*
	 * Default constructor*
	 */
	public VEMAMonitoringProfile()
	{

	}

	public VEMAMonitoringProfile( String pathToProfile, String profileName )
	{
		this.pathToMonitoringProfile = pathToProfile;
		this.monitorProfileName      = profileName;

		if (this.pathToMonitoringProfile == null)
			this.pathToMonitoringProfile = DEFAULT_PROFILE_PATH;
	}

	/**
	 * loadConfiguration Load the Monitoring Profile from the file. All
	 * structures of this class will be loaded.
	 * 
	 * @throws VEMAConfigurationException
	 */
	public void loadConfiguration() throws VEMAConfigurationException
	{
	}

	/**
	 * Accessor methods to store and get values
	 */

	/**
	 * addMetricToList The configuration object (ConfigurationSettings) will be
	 * added to the monitoring list if the check is enabled in the monitoring
	 * profile (isMonitored).
	 * 
	 * @param metricType
	 * @param metricName
	 * @param config
	 * @throws VEMAConfigurationException
	 */
	public void addMetricToList( String metricType, String metricName,
			ConfigurationSettings config ) throws VEMAConfigurationException
	{

		/* Input validation */
		if (metricType == null || metricType.length() < 1)
			throw new VEMAConfigurationException(
					"Error adding metric to the monitoring list. Error: metric type is null or empty. Valid types for metric type are: "
							+ TYPE_HYPERVISOR + " or " + TYPE_VM );
		if (metricName == null || metricName.length() < 1 || config == null)
			throw new VEMAConfigurationException(
					"Error adding metric to the monitor list. Metric name or config object can't be null or empty" );

		if (config.isMonitored())
		{
			/* Add config object to correct list */
			if (metricType.equalsIgnoreCase( TYPE_HYPERVISOR ))
			{
				this.metricsForHypervisors.put( metricName, config );
			}
			else if (metricType.equalsIgnoreCase( TYPE_VM ))
			{
				this.metricsForVM.put( metricName, config );
			}
			else
			{
				throw new VEMAConfigurationException(
						"Error adding metric to the monitor list. Unrecognized Metric type: "
								+ metricType
								+ " .Valid types for metric type are: "
								+ TYPE_HYPERVISOR + " or " + TYPE_VM );

			}
			log.info( "Metric type " + metricType + " Metric " + metricName
					+ " is being monitored by CloudHub Agent." );
		}
		else
		{
			log.info( "Metric type "
					+ metricType
					+ " Metric "
					+ metricName
					+ " is NOT monitored. Update Monitoring profile to add to the metrics to monitor." );
		}
	}

	/**
	 * getMetricConfiguration Extracts the metric configuration of an actively
	 * monitored metric for a given type.
	 * 
	 * @param metricType
	 * @param metricName
	 * @return
	 * @throws VEMAConfigurationException
	 */
	public ConfigurationSettings getMetricConfiguration( String metricType,
			String metricName ) throws VEMAConfigurationException
	{

		/* Input validation */
		if (metricType == null || metricType.length() < 1)
			throw new VEMAConfigurationException(
					"Error getting metric configuration. Error: metric type is null or empty. Valid types for metric type are: "
							+ TYPE_HYPERVISOR + " or " + TYPE_VM );
		if (metricName == null || metricName.length() < 1)
			throw new VEMAConfigurationException(
					"Error getting metric configuration. Metric name can't be null or empty" );

		/* Extract the metric configuration */
		if (metricType.equalsIgnoreCase( TYPE_HYPERVISOR ))
		{
			return this.metricsForHypervisors.get( metricName );
		}
		else if (metricType.equalsIgnoreCase( TYPE_VM ))
		{
			return this.metricsForVM.get( metricName );
		}
		else
		{
			throw new VEMAConfigurationException(
					"Error reading metric configuration. Unrecognized Metric type: "
							+ metricType
							+ " .Valid types for metric type are: "
							+ TYPE_HYPERVISOR + " or " + TYPE_VM );
		}
	}

	/**
	 * 
	 * @param metricType
	 * @return
	 * @throws VEMAConfigurationException
	 */

	public ConcurrentHashMap<String, ConfigurationSettings> getListOfMonitoredMetrics(
			String metricType ) throws VEMAConfigurationException
	{
		/* Input validation */
		if (metricType == null || metricType.length() < 1)
			throw new VEMAConfigurationException(
					"Error getting metric configuration. Error: metric type is null or empty. Valid types for metric type are: "
							+ TYPE_HYPERVISOR + " or " + TYPE_VM );

		/* Extract the metric configuration */
		if (metricType.equalsIgnoreCase( TYPE_HYPERVISOR ))
		{
			return this.metricsForHypervisors;
		}
		else if (metricType.equalsIgnoreCase( TYPE_VM ))
		{
			return this.metricsForVM;
		}
		else
		{
			throw new VEMAConfigurationException(
					"Error reading metric configuration. Unrecognized Metric type: "
							+ metricType
							+ " .Valid types for metric type are: "
							+ TYPE_HYPERVISOR + " or " + TYPE_VM );
		}

	}
}
