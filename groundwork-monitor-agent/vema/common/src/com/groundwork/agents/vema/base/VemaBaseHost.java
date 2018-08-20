package com.groundwork.agents.vema.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.GWOSEntity;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.monitorAgent.MonitorAgentClient;
import com.groundwork.agents.vema.gwos.GWOSHostGroup;

public class VemaBaseHost extends GWOSEntity
{
	private static Logger log = Logger.getLogger(VemaBaseHost.class);

	private static final Date nullDate     = new Date(); // was ( 0L );
	private String  hostName               = null;
    private String  hostGroup              = null;
    private String  ipAddress              = null;
    private String  macAddress             = null;
    private Date    bootDate               = null;
    private Date    lastDate               = null;    // datetime of last update
    private long    upTime                 = 0;       // in milliseconds
    private long    biasTime               = 0;       // in milliseconds (clock bias)
    private String  hostDescription        = null;
    private String  currRunState           = null;    // synthetic 'up/down/warning/critical' stuff.
    private String  prevRunState           = "PENDING";  // important!
    private String  currRunStateExtra      = null;    // receiving extra State guidance
    private boolean autoClearStateChange   = true;
    private int     mergeSkipped           = 0;       // counts how many merges missed (seq.)
    private int     mergeCount             = 0;       // just counts number of merges. 
    
    
    private String	nextCheckTime		   = null;    // Next time the Hypervisor will be checked for status

    private ConcurrentHashMap<String, VemaBaseVM>         vmPool =
        new ConcurrentHashMap<String, VemaBaseVM>();

    private ConcurrentHashMap<String, VemaBaseMetric> metricPool =
        new ConcurrentHashMap<String, VemaBaseMetric>();

    private ConcurrentHashMap<String, VemaBaseMetric> configPool =
        new ConcurrentHashMap<String, VemaBaseMetric>();

    // constructors...
    public  VemaBaseHost()
    {
    }

    public VemaBaseHost(String host)
    {
        hostName = host;
    }

    // Accessors....
    public String         getHostName()        { return hostName;                 }
    public String         getIpAddress()       { return ipAddress;                }
    public String         getMacAddress()      { return macAddress;               }
    public String         getHostGroup()       { return hostGroup;                }
    public String         getDescription()     { return hostDescription;          }
    public String         getRunState()        { return currRunState;             }
    public String         getRunExtra()        { return currRunStateExtra;        }
    public String         getPrevRunState()    { return prevRunState;             }
    public int            getMergeCount()      { return mergeCount;               }
    public int            getSkipCount()       { return mergeSkipped;             }
    public VemaBaseMetric getMetric(String lu) { return metricPool.get( lu );     }
    public VemaBaseMetric getConfig(String lu) { return configPool.get( lu );     }
    public VemaBaseVM     getVM(String lu)     { return     vmPool.get( lu );     }


    public String getValueByKey( String key )
    {
        VemaBaseMetric vbmo = null;
        if( ( vbmo = getMetric( key ) ) == null
        &&  ( vbmo = getConfig( key ) ) == null )
            return null;

        return vbmo.getCurrValue();
    }

    public ConcurrentHashMap<String, VemaBaseVM>     getVMPool()     { return vmPool;     }
    public ConcurrentHashMap<String, VemaBaseMetric> getMetricPool() { return metricPool; }
    public ConcurrentHashMap<String, VemaBaseMetric> getConfigPool() { return configPool; }

    /* TODO: --RAVI -- Add add list of vm's to delete, and list of vms to add */

    public  long   getBootDateMillisec()
    {
        return bootDate == null ? nullDate.getTime() : bootDate.getTime();
    }

    public  String getBootDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

        return bootDate == null 
        		? sdf.format( nullDate ).toString() 
        		: sdf.format( bootDate ).toString();
    }

    public  long   getLastUpdateMillisec()
    {
        return lastDate.getTime();
    }

    public  String getLastUpdate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

        if( bootDate != null )
        {
        	// --------------------------------------------------------------------
        	// This is to compute a "bias time" for each host, based on the host's
        	// feeling of a "boot date".  Which might be way off.  Doing this once
        	// should retain the granularity of a host's self-sample period, which
        	// doing the bias computation EVERY time would remove. 
        	// --------------------------------------------------------------------
        	if( biasTime == 0 )
            	biasTime = System.currentTimeMillis() - (bootDate.getTime() + upTime);

            Date d = new Date( bootDate.getTime() + upTime + biasTime );
            lastDate = d;
            return sdf.format( lastDate ).toString();
        }
        else
            return sdf.format( nullDate ).toString();
    }
    
    public String getNextCheckTime()
    {
    	return this.nextCheckTime;
    }
    
    public void setNextCheckTime(String nextCheckTime)
    {
    	this.nextCheckTime = nextCheckTime;
    }

    // Setters...
    public void setRunState( String state )
    {
        if(( autoClearStateChange )
        && ( currRunState != null ))
            prevRunState = currRunState;

        currRunState = (state == null) ? "" : state;
    }

    public void setRunExtra( String extra )
    {
    	currRunStateExtra = extra;
    }

    public boolean isStateChange()
    {
    	// BEWARE that little exclamation mark (!)... very important logic inversion
    	
        if(      currRunState != null )
            if(  prevRunState != null ) return !currRunState.equalsIgnoreCase( prevRunState );
            else                        return true;
        else if( prevRunState != null ) return true;
        else                            return false;
    }

    public boolean isStale( int minSkipped, int minTime )
    {
        int nowTime = (int)((new Date()).getTime() / 1000);    // seconds
        int lasTime = (lastDate != null) 
    			? (int)(lastDate.getTime() / 1000)
    			: nowTime;
        int deltaT  = nowTime - lasTime;

        if( deltaT < 0 )
            deltaT = 0;

        return( mergeSkipped > minSkipped && deltaT > minTime );
    }

    public void clearStateChange()
    {
        if( currRunState != null )
        	prevRunState = currRunState;

        autoClearStateChange = false;   // if caller manually clears once, then always has to.
    }
    
    public void incSkipped()                        { this.mergeSkipped++; }

    public void setDescription(String description ) { hostDescription = description; }
    public void setHostGroup(  String group       ) { hostGroup       = group;       }
    public void setIpAddress(  String address     ) { ipAddress       = address;     }
    public void setMacAddress( String address     ) { macAddress      = address;     }

    public void setBootDate(String textDate, String dateFormat )
    {
        if( textDate   == null 
        ||  dateFormat == null
        ||  textDate.isEmpty()
        ||  dateFormat.isEmpty() )
            return;

        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat );
                                                        // times...
        try
        {
            String base = new String( textDate.substring( 0, textDate.length() - 6 ) );
            String zone = new String( textDate.substring(    textDate.length() - 6 ) );

            sdf.setTimeZone(TimeZone.getTimeZone( zone )); // from VIM25 come Zulu

            Date d = sdf.parse( base ); // this gets caught
            bootDate = d; // if it doesn't work, this won't get set!
        }
        catch (Exception e)
        {
            // toss the exception (for now)
            // log.info( "date '" + textDate + "' didn't parse with '" + dateFormat + "' (e=" + e + ")" );
        }
    }

    // -------------------------------------------------------------------
    // converts 'seconds' to milliseconds (which needs to be a long)
    // then using that, converts the basetime to the last-update-time
    // and assigns it to the holder object for later use.
    // -------------------------------------------------------------------
    public void setLastUpdate()
    {
        if( bootDate == null )
            bootDate = new Date( System.currentTimeMillis() );

        upTime = System.currentTimeMillis() - bootDate.getTime();
    }

    public  void   setLastUpdate( String uptimeSeconds )
    {
        if( uptimeSeconds == null )
            upTime = 0;
        else if( uptimeSeconds.isEmpty() )
            upTime = 0;
        else
            upTime = 1000 * Integer.decode( uptimeSeconds ).longValue();
    }

    public void clearVM()                            { vmPool.clear();     }
    public void clearMetric()                        { metricPool.clear(); }
    public void clearConfig()                        { configPool.clear(); }

    public void putVM(String name, VemaBaseVM vm)
    {
        if( name.startsWith( "-" ) ) vmPool.remove( name.substring( 1 ) );
        else                         vmPool.put( name, vm );
    }

    public void putMetric(String name, VemaBaseMetric value)
    {
        if( name.startsWith( "-" ) ) metricPool.remove( name.substring( 1 ) );
        else                         metricPool.put( name, value );
    }

    public void putConfig(String name, VemaBaseMetric value)
    {
        if( name.startsWith( "-" ) ) configPool.remove( name.substring( 1 ) );
        else                         configPool.put( name, value );
    }

    public void renameVM( String oldname, String newname )
    {
        VemaBaseVM vmo = this.vmPool.get( oldname );
        if( vmo != null )
        {
            this.vmPool.remove( oldname );
            this.vmPool.put( newname, vmo );
        }
        return;
    }

    public void mergeInNew( VemaBaseHost update )
    {
        if( update == null )    // nothing to do, for now
            return;

        this.hostName             = update.hostName;
        this.hostGroup            = update.hostGroup;
        this.ipAddress            = update.ipAddress;
        this.macAddress           = update.macAddress;
        this.bootDate             = update.bootDate;
        this.lastDate             = update.lastDate;
        this.upTime               = update.upTime;
        this.hostDescription      = update.hostDescription;
        this.prevRunState         = this.currRunState == null 
                                  ? "" 
                                  : this.currRunState;
        this.currRunState         = update.currRunState;
        this.currRunStateExtra    = update.currRunStateExtra;
        this.autoClearStateChange = update.autoClearStateChange;
        this.mergeSkipped         = 0;
        this.mergeCount++;
        
        for( String upvm : update.vmPool.keySet() )
        {
            // log.info( "update vm: '" + upvm + "'" );
            VemaBaseVM upvmObj = update.vmPool.get( upvm );
            if( upvmObj == null )
                continue;

            VemaBaseVM myvmObj = this.vmPool.get( upvm );
            if( myvmObj == null )   // have to create a receiving one?
                continue;           // but can't!  This is a superclass.

            // log.info( "update vm2 '" + upvm + "'" );
            myvmObj.mergeInNew( upvmObj );
        }

        for( String upMetric : update.metricPool.keySet() )
        {
            // log.info( "update metric: '" + upMetric + "'" );
            VemaBaseMetric upMetricObj = update.metricPool.get( upMetric );
            if( upMetricObj == null )
                continue;

            VemaBaseMetric myMetricObj = this.metricPool.get( upMetric );
            if( myMetricObj == null )
                this.metricPool.put( upMetric, myMetricObj =
                    new VemaBaseMetric(
                        upMetric,
                        upMetricObj.getThresholdWarning(),
                        upMetricObj.getThresholdCritical(),
                        upMetricObj.isGraphed(),
                        upMetricObj.isMonitored() ) );

            myMetricObj.mergeInNew( upMetricObj );
        }

        for( String upConfig : update.configPool.keySet() )
        {
            // log.info( "update config: '" + upConfig + "'" );
            VemaBaseMetric upConfigObj = update.configPool.get( upConfig );
            if( upConfigObj == null )
                continue;

            VemaBaseMetric myConfigObj = this.configPool.get( upConfig );
            if( myConfigObj == null )
                this.configPool.put( upConfig, myConfigObj =
                    new VemaBaseMetric(
                        upConfig,
                        upConfigObj.getThresholdWarning(),
                        upConfigObj.getThresholdCritical(),
                        upConfigObj.isGraphed(),
                        upConfigObj.isMonitored() ) );

            myConfigObj.mergeInNew( upConfigObj );
        }
            // log.info( "update done:" );
    }

    public  String  formatSelf()
    {
        return formatSelf( this );
    }

    public  String  formatSelf( VemaBaseHost o )
    {
        StringBuilder s = new StringBuilder();

        s.append( String.format( "%-40s: '%s'\n", "hostName",             o.hostName ));
        s.append( String.format( "%-40s: '%s'\n", "hostGroup",            o.hostGroup ));
        s.append( String.format( "%-40s: '%s'\n", "ipAddress",            o.ipAddress ));
        s.append( String.format( "%-40s: '%s'\n", "macAddress",           o.macAddress ));
        s.append( String.format( "%-40s: '%s'\n", "bootDate",             o.bootDate == null 
                                                                   ? "" : o.bootDate.toString() ));
        s.append( String.format( "%-40s: '%s'\n", "lastDate",             o.lastDate == null 
                                                                   ? "" : o.lastDate.toString() ));
        s.append( String.format( "%-40s: '%d'\n", "upTime",               o.upTime ));
        s.append( String.format( "%-40s: '%d'\n", "biasTime",             o.biasTime ));
        s.append( String.format( "%-40s: '%s'\n", "hostDescription",      o.hostDescription ));
        s.append( String.format( "%-40s: '%s'\n", "currRunState",         o.currRunState ));
        s.append( String.format( "%-40s: '%s'\n", "prevRunState",         o.prevRunState ));
        s.append( String.format( "%-40s: '%s'\n", "currRunStateExtra",    o.currRunStateExtra ));
        s.append( String.format( "%-40s: '%s'\n", "autoClearStateChange", o.autoClearStateChange ? "true" : "false" ));
        s.append( String.format( "%-40s: '%d'\n", "mergeSkipped",         o.mergeSkipped ));
        s.append( String.format( "%-40s: '%s'\n", "mergeCount",           o.mergeCount ));
        s.append( String.format( "%-40s: '%s'\n", "nextCheckTime",        o.nextCheckTime ));

        if( o.vmPool     != null ) 
            for( String key : o.vmPool.keySet() )
                s.append( String.format( "\n%-40s: (vmpool (host))\n%s",     
                    key, o.vmPool.get( key ).formatSelf() ));

        if( o.metricPool != null )
            for( String key : o.metricPool.keySet() )
                s.append( String.format( "\n%-40s: (metricpool (host))\n%s", 
                    key, o.metricPool.get( key ).formatSelf() ));

        if( o.configPool != null )
            for( String key : o.configPool.keySet() )
                s.append( String.format( "\n%-40s: (configpool (host))\n%s", 
                    key, o.configPool.get( key ).formatSelf() ));

        return s.toString();
    }

    public String getXML(String action)
    {
    	// Add all attributes
    	setXmlHead(GWOSEntity.XML_HOST_HEAD);
		
		// Add attributes for Host
		addAttribute("Host",        hostName);
		addAttribute("Description", hostName); // set up defaults of all being same
		addAttribute("DisplayName", hostName); // which is overridden by subsequent setDescription() calls
		
		// Add attribute for device
		if(          macAddress      != null )  // and...
            if(      macAddress.length() > 0 ) addAttribute("Device", macAddress);
		    else if( ipAddress .length() > 0 ) addAttribute("Device", ipAddress);
		    else                               addAttribute("Device", hostName);
        else                                   addAttribute("Device", hostName);
		
		if ( action.equals(GWOSEntity.ACTION_ADD))
			;

        else if (action.equals(GWOSEntity.ACTION_MODIFY))
        {
            addAttribute(     "LastCheckTime",    getLastUpdate() );
            addAttribute(     "MonitorStatus",    getRunState() );
            addAttribute(     "LastPluginOutput", "Status " + getRunState()
            										+ " "   + getLastUpdate()
            										+ "/"   + getRunExtra() );
            if( this.isStateChange() )
                addAttribute( "LastStateChange",  getLastUpdate() );
            
            if (this.getNextCheckTime() != null)
            	addAttribute("NextCheckTime", this.getNextCheckTime());
            
		}
        else if (action.equals(GWOSEntity.ACTION_DELETE))
        	;
		// Un-recognized action return blank
        else
            return "";
		
		return super.getXML();  // getXML(void) in superclass.
    }
}
