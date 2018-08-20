package com.groundwork.agents.vema.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.GWOSEntity;
import com.groundwork.agents.vema.api.VemaConstants;

public class VemaBaseNetwork extends GWOSEntity
{
    private static Logger              log                 = Logger.getLogger( VemaBaseHost.class );
    private static final Date          nullDate            = new Date();

    private String                     networkName         = null;
    private String                     hypervisor          = null;
    private String                     macAddress          = null;
    private String                     ipAddress           = null;
    private String                     guestState          = null;
    private Date                       bootDate            = null;
    private Date                       lastDate            = null;    
    private long                       upTime              = 0;    
    private long                       biasTime            = 0;        
    private String                     currRunState        = null;
    private String                     prevRunState        = "PENDING";
    private String                     currRunStateExtra   = null; 

    private boolean                   autoClearStateChange = true;
    private ConcurrentHashMap<String, VemaBaseMetric>    
                                       metricPool          = new ConcurrentHashMap<String, VemaBaseMetric>();
    private ConcurrentHashMap<String, VemaBaseMetric>    
                                       configPool          = new ConcurrentHashMap<String, VemaBaseMetric>();
    private int                        mergeSkipped        = 0; 
    private int                        mergeCount          = 0; 
    private String                     nextCheckTime       = null; 
    private String                     hostGroup           = null; 

	// -------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------
	protected VemaBaseNetwork()              { /* nothing to do! */ }
	protected VemaBaseNetwork( String name ) { networkName = name; }

	public String getHypervisor()   { return hypervisor; } 
	public String getNetworkName()  { return networkName; } 
	public String getMacAddress()   { return macAddress; } 
	public String getIpAddress()    { return ipAddress; } 
	public String getRunState()     { return currRunState; } 
	public String getRunExtra()     { return currRunStateExtra; } 
	public String getPrevRunState() { return prevRunState; } 
	public String getGuestState()   { return guestState; } 
	public int    getMergeCount()   { return mergeCount; } 
	public int    getSkipCount()    { return mergeSkipped; } 
	public String getHostGroup()    { return hostGroup; } 

	public void   setHostGroup( String group ) { hostGroup = group; }

	public long getBootDateMillisec()
	{
		return bootDate == null 
            ? nullDate.getTime() 
            : bootDate.getTime();
	}

	public String getBootDate()
	{
		SimpleDateFormat sdf = new SimpleDateFormat(
				VemaConstants.gwosDateFormat );

		return bootDate == null 
            ? sdf.format( nullDate ).toString() 
            : sdf.format( bootDate ).toString();
	}

	public long getLastUpdateMillisec()
	{
		return lastDate.getTime();
	}

	public String getLastUpdate()
	{
		SimpleDateFormat sdf = new SimpleDateFormat(
				VemaConstants.gwosDateFormat );

		if (bootDate != null)
		{
			if (biasTime == 0)
				biasTime = System.currentTimeMillis() - (bootDate.getTime() + upTime);

			Date d = new Date( bootDate.getTime() + upTime + biasTime );
			lastDate = d;
			return sdf.format( lastDate ).toString();
		}
		else
			return sdf.format( nullDate ).toString();
	}

	public ConcurrentHashMap<String, VemaBaseMetric> getMetricPool() { return metricPool; } 
	public ConcurrentHashMap<String, VemaBaseMetric> getConfigPool() { return configPool; }

	public VemaBaseMetric getMetric( String key ) { return metricPool.get( key ); } 
	public VemaBaseMetric getConfig( String key ) { return configPool.get( key ); }

	public String getNextCheckTime()
	{
		return nextCheckTime;
	}

	public void setNextCheckTime( String nextCheckTime )
	{
		this.nextCheckTime = nextCheckTime;
	}

	public String getValueByKey( String key )
	{
		VemaBaseMetric vbmo;

		if ((vbmo = getMetric( key )) == null
				&& (vbmo = getConfig( key )) == null)
			return null;

		return vbmo.getCurrValue();
	}

	// -------------------------------------------------------------------
	// setters
	// -------------------------------------------------------------------
	public void setRunExtra( String extra )
	{
		currRunStateExtra = extra;
	}

	public void setRunState( String state )
	{
		if (autoClearStateChange && (currRunState != null))
			prevRunState = currRunState;

		currRunState = (state == null) ? "" : state;
	}

	public boolean isStateChange()
	{
		// BEWARE that little exclamation mark (!)... very important logic
		// inversion

		if (currRunState != null) // and...
			if (prevRunState != null)  return !currRunState.equalsIgnoreCase( prevRunState );
			else                       return true;
		else if (prevRunState != null) return true;
		else                           return false;
	}

	public boolean isStale( int minSkipped, int minTime )
	{
		int nowTime = (int) ((new Date()).getTime() / 1000); // seconds
		int lasTime = (lastDate != null) ? (int) (lastDate.getTime() / 1000)
				: nowTime;
		int deltaT = nowTime - lasTime;

		if (deltaT < 0)
			deltaT = 0;

		return (mergeSkipped > minSkipped && deltaT > minTime);
	}

	public void clearStateChange()
	{
		prevRunState = currRunState;
		autoClearStateChange = false; // if caller manually clears once, then
										// always has to.
	}

	public void incSkipped()                    { this.mergeSkipped++; } 
	public void setHypervisor(  String host   ) { hypervisor  = host; } 
	public void setNetworkName( String name   ) { networkName = name; } 
	public void setMacAddress(  String mac    ) { macAddress  = mac; } 
	public void setIpAddress(   String ip     ) { ipAddress   = ip; } 
	public void setGuestState(  String state  ) { guestState  = state; }

	public void setBootDate( String textDate, String dateFormat )
	{
		if (textDate == null || dateFormat == null || textDate.isEmpty()
				|| dateFormat.isEmpty())
			return;

		SimpleDateFormat sdf = new SimpleDateFormat( dateFormat );
		// times...
		try
		{
			String base = new String( textDate.substring( 0,
					textDate.length() - 6 ) );
			String zone = new String(
					textDate.substring( textDate.length() - 6 ) );

			sdf.setTimeZone( TimeZone.getTimeZone( zone ) ); // from VIM25 come
																// Zulu

			Date d = sdf.parse( base ); // this gets caught
			bootDate = d; // if it doesn't work, this won't get set!
		}
		catch( Exception e )
		{
			// toss the exception (for now)
			// log.info( "date '" + textDate + "' didn't parse with '" +
			// dateFormat + "' (e=" + e + ")" );
		}
	}

	// -------------------------------------------------------------------
	// converts 'seconds' to milliseconds (which needs to be a long)
	// then using that, converts the basetime to the last-update-time
	// and assigns it to the holder object for later use.
	// -------------------------------------------------------------------
	public void setLastUpdate()
	{
		if (bootDate == null)
			bootDate = new Date( System.currentTimeMillis() );

		upTime = System.currentTimeMillis() - bootDate.getTime();
	}

	public void setLastUpdate( String uptimeSeconds )
	{
		if (uptimeSeconds == null)
			upTime = 0;
		else if (uptimeSeconds.isEmpty())
			upTime = 0;
		else
			upTime = 1000 * Integer.decode( uptimeSeconds ).longValue();
	}

	public void putMetric( String key, VemaBaseMetric value )
	{
		if (key.startsWith( "-" ))
			metricPool.remove( key.substring( 1 ) );
		else
			metricPool.put( key, value );
	}

	public void putConfig( String key, VemaBaseMetric value )
	{
		if (key.startsWith( "-" ))
			configPool.remove( key.substring( 1 ) );
		else
			configPool.put( key, value );
	}

	public void mergeInNew( VemaBaseNetwork update )
	{
		if (update == null)
			return;

		if (this.networkName == null || update.networkName != null)
			this.networkName = update.networkName;

		this.hypervisor           = update.hypervisor;
		this.guestState           = update.guestState;
		this.ipAddress            = update.ipAddress;
		this.macAddress           = update.macAddress;
		this.bootDate             = update.bootDate;
		this.lastDate             = update.lastDate;
		this.upTime               = update.upTime;
		this.prevRunState         = this.currRunState == null ? "" : this.currRunState;
		this.currRunState         = update.currRunState;
		this.currRunStateExtra    = update.currRunStateExtra;
		this.autoClearStateChange = update.autoClearStateChange;
		this.mergeSkipped         = 0;
		this.mergeCount++;

		for( String upMetric : update.metricPool.keySet() )
		{
			VemaBaseMetric upMetricObj = update.metricPool.get( upMetric );
			if (upMetricObj == null)
				continue;

			VemaBaseMetric myMetricObj = this.metricPool.get( upMetric );
			if (myMetricObj == null)
				this.metricPool.put(
						upMetric,
						myMetricObj = new VemaBaseMetric( upMetric, upMetricObj
								.getThresholdWarning(), upMetricObj
								.getThresholdCritical(), upMetricObj
								.isGraphed(), upMetricObj.isMonitored() ) );

			myMetricObj.mergeInNew( upMetricObj );
		}

		for( String upConfig : update.configPool.keySet() )
		{
			VemaBaseMetric upConfigObj = update.configPool.get( upConfig );
			if (upConfigObj == null)
				continue;

			VemaBaseMetric myConfigObj = this.configPool.get( upConfig );
			if (myConfigObj == null)
				this.configPool.put(
						upConfig,
						myConfigObj = new VemaBaseMetric( upConfig, upConfigObj
								.getThresholdWarning(), upConfigObj
								.getThresholdCritical(), upConfigObj
								.isGraphed(), upConfigObj.isMonitored() ) );

			myConfigObj.mergeInNew( upConfigObj );
		}
	}

	public String formatSelf()
	{
		return formatSelf( this );
	}

	public String formatSelf( VemaBaseNetwork o )
	{
		StringBuilder s = new StringBuilder();

		s.append( String.format( "%-40s: %s\n", "networkName", o.networkName ) );
		s.append( String.format( "%-40s: %s\n", "hypervisor", o.hypervisor ) );
		s.append( String.format( "%-40s: %s\n", "macAddress", o.macAddress ) );
		s.append( String.format( "%-40s: %s\n", "ipAddress", o.ipAddress ) );
		s.append( String.format( "%-40s: %s\n", "guestState", o.guestState ) );
		s.append( String.format( "%-40s: %s\n", "bootDate", o.bootDate == null ? "" : o.bootDate.toString() ) );
		s.append( String.format( "%-40s: %s\n", "lastDate", o.lastDate == null ? "" : o.lastDate.toString() ) );
		s.append( String.format( "%-40s: %d\n", "upTime", o.upTime ) );
		s.append( String.format( "%-40s: %d\n", "biasTime", o.biasTime ) );
		s.append( String.format( "%-40s: %s\n", "currRunState", o.currRunState ) );
		s.append( String.format( "%-40s: %s\n", "prevRunState", o.prevRunState ) );
		s.append( String.format( "%-40s: %s\n", "currRunStateExtra", o.currRunStateExtra ) );
		s.append( String.format( "%-40s: %s\n", "autoClearStateChange", o.autoClearStateChange ? "true" : "false" ) );
		s.append( String.format( "%-40s: %d\n", "mergeSkipped", o.mergeSkipped ) );
		s.append( String.format( "%-40s: %d\n", "mergeCount", o.mergeCount ) );
		s.append( String.format( "%-40s: %s\n", "nextCheckTime", o.nextCheckTime ) );
		s.append( String.format( "%-40s: %s\n", "hostGroup", o.hostGroup ) );

		if (o.metricPool != null)
			for( String key : metricPool.keySet() )
				s.append( String.format( "\n%-40s: (metricpool (Network))\n%s",
						key, metricPool.get( key ).formatSelf() ) );

		if (o.configPool != null)
			for( String key : configPool.keySet() )
				s.append( String.format( "\n%-40s: (configpool (Network))\n%s",
						key, configPool.get( key ).formatSelf() ) );

		return s.toString();
	}

	public String getXML( String action )
	{
		// Add all attributes
		setXmlHead( GWOSEntity.XML_HOST_HEAD );

		// Add attributes for Host
		addAttribute( "Host", this.networkName );
		addAttribute( "Description", this.networkName );
		addAttribute( "DisplayName", this.networkName );

		// addAttribute("LastStateChange", this.lastStateChange);

		// Add attribute for device
		if (this.macAddress != null) // and...
			if (this.macAddress.length() > 0)
				addAttribute( "Device", this.macAddress );
			else if (this.ipAddress.length() > 0)
				addAttribute( "Device", this.ipAddress );
			else
				addAttribute( "Device", this.networkName );
		else
			addAttribute( "Device", this.networkName );

		if (action.equals( ACTION_ADD ))
			return super.getXML();

		else if (action.equals( GWOSEntity.ACTION_MODIFY ))
		{
			addAttribute( "LastCheckTime", getLastUpdate() );
			addAttribute( "MonitorStatus", getRunState() );
			addAttribute( "LastPluginOutput", "Status " + getRunState() + " "
					+ getLastUpdate() + "/" + getRunExtra() );

			if (this.isStateChange())
				addAttribute( "LastStateChange", getLastUpdate() );

			if (this.getNextCheckTime() != null)
				addAttribute( "NextCheckTime", this.getNextCheckTime() );

			/* If NETWORK is powered down or suspended auto acknowledge */
			if (this.getMetricPool().size() == 0)
				addAttribute( "isAcknowledged", "1" );

			return super.getXML();
		}
		else if (action.equals( GWOSEntity.ACTION_DELETE ))
		{
			return super.getXML();
		}
		// Un-recognized action return blank
		else
			return "";
	}
}