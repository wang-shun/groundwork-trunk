package com.groundwork.agents.vema.vmware.connector;

import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import org.apache.log4j.Logger;

public class VemaVMwareHost extends VemaBaseHost
{
	private static org.apache.log4j.Logger log = Logger.getLogger( VemaVMwareVM.class );

	public VemaVMwareHost(String hostName)
	{
		super(hostName);
	}

    private static final VemaBaseQuery[] baseMetricList =
    { 
new VemaBaseQuery( "summary.quickStats.overallCpuUsage",      2000,     3000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.overallMemoryUsage",   2000,     3000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.uptime",            3197400,  6000000, false, false ), //crit
//new VemaBaseQuery( "runtime.powerState",                     0,        0,  false, false     ),
new VemaBaseQuery( "summary.runtime.bootTime",                0,        0,  false, false), //crit
new VemaBaseQuery( "summary.runtime.connectionState",         0,        0,  false, false),
new VemaBaseQuery( "summary.runtime.powerState",              0,        0,  false, false), //crit
    };

    private static final VemaBaseQuery[] baseConfigList =
    { 
new VemaBaseQuery( "name",                                    0,        0, false, false     ), //crit
//new VemaBaseQuery( "hardware.memorySize",                    0,        0, false, false ),
//new VemaBaseQuery( "hardware.cpuInfo.hz",                    0,        0, false, false ),
//new VemaBaseQuery( "hardware.cpuInfo.numCpuThreads",         0,        0, false, false ),
//new VemaBaseQuery( "hardware.systemInfo.model",              0,        0, false, false ),
//new VemaBaseQuery( "hardware.systemInfo.vendor",             0,        0, false, false ),
new VemaBaseQuery( "summary.hardware.cpuMhz",                0,        0, false, false ), //crit
new VemaBaseQuery( "summary.hardware.cpuMhz.scaled",         0,        0, false, false ), //crit
//new VemaBaseQuery( "summary.hardware.vendor",                0,        0, false, false ),
//new VemaBaseQuery( "summary.hardware.numCpuThreads",         0,        0, false, false ),
//new VemaBaseQuery( "summary.hardware.numCpuPkgs",            0,        0, false, false ),
new VemaBaseQuery( "summary.hardware.numCpuCores",           0,        0, false, false ), //crit
new VemaBaseQuery( "summary.hardware.memorySize",            0,        0, false, false ), //crit
new VemaBaseQuery( "summary.hardware.model",                 0,        0, false, false ), //crit
new VemaBaseQuery( "vm",                                     0,        0, false, false ), //crit
    };

    private static final VemaBaseQuery[] baseSyntheticList = 
    {
new VemaBaseQuery( "syn.host.cpu.used",                     75,       90, false, false ),
//new VemaBaseQuery( "syn.host.cpu.unused",                   25,       10    ),
new VemaBaseQuery( "syn.host.mem.used",                     80,       95, false, false ),
//new VemaBaseQuery( "syn.host.mem.unused",                   20,        5    ),
    };

    private static VemaBaseSynthetic[] baseSyntheticMaster = 
    {
//        new VemaBaseSynthetic(  "syn.host.cpu.used",
//                                "summary.quickStats.overallCpuUsage", 1.0, 
//                                "summary.hardware.cpuMhz", false, true ),
//
//        new VemaBaseSynthetic(  "syn.host.cpu.unused",
//                                "summary.quickStats.overallCpuUsage", 1.0,
//                                "summary.hardware.cpuMhz", true, true ),
//
        new VemaBaseSynthetic(  "syn.host.cpu.used",
                                "summary.quickStats.overallCpuUsage", 1.0, 
                                "summary.hardware.cpuMhz.scaled", false, true ),

        new VemaBaseSynthetic(  "syn.host.cpu.unused",
                                "summary.quickStats.overallCpuUsage", 1.0,
                                "summary.hardware.cpuMhz.scaled", true, true ),

        new VemaBaseSynthetic(  "syn.host.mem.used",
                                "summary.quickStats.overallMemoryUsage", 1024.0 * 1024.0,
                                "summary.hardware.memorySize", false, true ),

        new VemaBaseSynthetic(  "syn.host.mem.unused",
                                "summary.quickStats.overallMemoryUsage", 1024.0 * 1024.0,
                                "summary.hardware.memorySize",  true, true ),
 };

    public VemaBaseQuery[] getDefaultSyntheticList() { return baseSyntheticList; }
    public VemaBaseQuery[] getDefaultMetricList()    { return baseMetricList; }
    public VemaBaseQuery[] getDefaultConfigList()    { return baseConfigList; }
    public VemaBaseSynthetic[] getSyntheticMaster()  { return baseSyntheticMaster; }

    public VemaBaseSynthetic   getSynthetic( String handle )
    {
        // I know... sequential searching sucks.  But it should always be short list.
        // ......... in future, make it a hash, I believe.
        //
        for( VemaBaseSynthetic v : baseSyntheticMaster )
            if( v.getHandle().equals( handle ) )
                return v;

        return null;
    }
    
    /**
     * getMonitorState()        ALERT: needs to be run after other variables are set!
     *
     * turns a number of retrieved VIM25 properties into a 'runtime state' 
     * of the virtual machine.  Kind of defensive code, that could be made 
     * more trim, and less suspicious.
     * 
     * MODES:
     * powerState    connectionState     result    extra
     * ----------------------------------------------------------
     *     null                  x       UNKNOWN   powerState not set
     *        x               null       UNKNOWN   connectionState not set
     * ----------------------------------------------------------
     *  powered_on        connected      UP
     *  powered_on        suspended      SUSPENDED
     *  powered_on              ?        DOWN      {connectionState}
     * ----------------------------------------------------------
     *  powered_off             ?        DOWN
     *        ?                 ?        UNKNOWN   {powerState}
     * ----------------------------------------------------------
     * @return
    */
    private static final String sUnknown     = "UNKNOWN";
    private static final String sUp          = "UP";
    private static final String sSuspended   = "SUSPENDED";
    private static final String sUnschedDown = "UNSCHEDULED DOWN";
    private static final String sSchedDown   = "SCHEDULED DOWN";

   public String getMonitorState()
    {
    	String    connectionState = null;
    	String         powerState = null;
    	VemaBaseMetric     metric = null; 
    	String                  r = null;
    	String                  x = null;
    	
    	if((metric = getMetric( "summary.runtime.connectionState" )) != null )
    		connectionState = metric.getCurrValue();
    	
    	if((metric = getMetric( "summary.runtime.powerState" )) != null )
    		powerState = metric.getCurrValue();
    	
    	if(      powerState      == null )                          r = sUnknown;
    	else if( connectionState == null )                          r = sUnknown;
    	else if( powerState.equalsIgnoreCase( "powered_on" ))
            if( connectionState.equalsIgnoreCase("connected"))      r = sUp;
            else if( connectionState.equalsIgnoreCase("suspended")) r = sSuspended;
            else                                                    r = sUnschedDown;
        else if( powerState.equalsIgnoreCase( "powered_off" ))      r = sSchedDown;
        else	                                                    r = sUnknown;

        x = "pwr=" + powerState       + " "
          + "con=" + connectionState  + " ";
    	
    	log.debug( "HOST MonState " + this.getHostName() + " : " + r );
        this.setRunExtra( x == null ? "" : x );

   	   	return r;
    }
}
