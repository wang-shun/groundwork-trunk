 package com.groundwork.agents.vema.vmware.connector;

import com.groundwork.agents.vema.base.VemaBaseMetric;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseVM;

import org.apache.log4j.Logger;

public final class VemaVMwareVM extends VemaBaseVM
{
	private static org.apache.log4j.Logger log = Logger.getLogger( VemaVMwareVM.class );
	
    public VemaVMwareVM( String vmName )
    {
        super( vmName );
    }

    private static final VemaBaseQuery[] baseMetricList = 
    { 
new VemaBaseQuery( "summary.quickStats.balloonedMemory",        1000,    2000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.compressedMemory",       1000,    2000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.consumedOverheadMemory", 1000,    2000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.guestMemoryUsage",       3000,    5000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.hostMemoryUsage",        4000,    5000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.overallCpuDemand",       2000,    3000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.overallCpuUsage",        1000,    3000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.privateMemory",          1000,    2000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.sharedMemory",           1000,    2000, true, false ), //crit
// ssdSwappedMemory ... is TOXIC to VMware version 4 queries!
//new VemaBaseQuery( "summary.quickStats.ssdSwappedMemory",       1000,    2000, true, false ),
new VemaBaseQuery( "summary.quickStats.swappedMemory",          1000,    2000, true, false ), //crit
new VemaBaseQuery( "summary.quickStats.uptimeSeconds",       3197400, 6394800, false, false ), //crit

new VemaBaseQuery( "summary.runtime.bootTime",                     0,       0, false, false ), //crit
new VemaBaseQuery( "summary.runtime.connectionState",              0,       0, false, false ),
new VemaBaseQuery( "summary.runtime.host",                         0,       0, false, false ), //crit
new VemaBaseQuery( "summary.runtime.memoryOverhead",               0,       0, true, false ),
new VemaBaseQuery( "summary.runtime.maxCpuUsage",                  0,       0, true, false ),
new VemaBaseQuery( "summary.runtime.maxMemoryUsage",               0,       0, true, false ),
new VemaBaseQuery( "summary.runtime.powerState",                   0,       0, false, false ),

new VemaBaseQuery( "summary.storage.committed",                    0,       0, true, false ),
new VemaBaseQuery( "summary.storage.uncommitted",                  0,       0, true, false ),
    };

    private static final VemaBaseQuery[] baseConfigList =
    { 
new VemaBaseQuery( "name",                                         0,       0, false, false ),

new VemaBaseQuery( "guest.ipAddress",                              0,       0, false, false ), //crit
new VemaBaseQuery( "guest.net",                                    0,       0, false, false ), //crit
new VemaBaseQuery( "guest.guestState",                             0,       0, false, false ), //crit
//new VemaBaseQuery( "guest.hostName",                               0,       0, false, false ),
//new VemaBaseQuery( "summary.guest.hostName" ,                      0,       0, false, false ),
//new VemaBaseQuery( "summary.guest.ipAddress" ,                     0,       0, false, false ),
new VemaBaseQuery( "summary.config.memorySizeMB",                  0,       0, false, false ), //crit
new VemaBaseQuery( "summary.config.name",                          0,       0, false, false ), //crit
new VemaBaseQuery( "summary.config.numCpu",                        0,       0, false, false ), //crit
new VemaBaseQuery( "summary.config.numEthernetCards",              0,       0, false, false ), //crit
new VemaBaseQuery( "summary.config.numVirtualDisks",               0,       0, false, false ), //crit
    };

    private static final VemaBaseQuery[] baseSyntheticList = 
    {
new VemaBaseQuery( "syn.vm.mem.balloonToConfigMemSize.used",      10,       20, true, false ),
new VemaBaseQuery( "syn.vm.mem.compressedToConfigMemSize.used",   10,       20, true, false ),
new VemaBaseQuery( "syn.vm.mem.sharedToConfigMemSize.used",       25,       75, true, false ),
new VemaBaseQuery( "syn.vm.mem.swappedToConfigMemSize.used",      10,       20, true, false ),
new VemaBaseQuery( "syn.vm.mem.guestToConfigMemSize.used",        70,       85, true, false ),
new VemaBaseQuery( "syn.vm.cpu.cpuToMax.used",                    75,       95, true, false ),

//new VemaBaseQuery( "syn.vm.mem.balloonToConfigMemSize.unused",    90,       80, true, false ),
//new VemaBaseQuery( "syn.vm.mem.compressedToConfigMemSize.unused", 90,       80, true, false ),
//new VemaBaseQuery( "syn.vm.mem.sharedToConfigMemSize.unused",     75,       25, true, false ),
//new VemaBaseQuery( "syn.vm.mem.swappedToConfigMemSize.unused",    90,       80, true, false ),
//new VemaBaseQuery( "syn.vm.mem.guestToConfigMemSize.unused",      30,       15, true, false ),
//new VemaBaseQuery( "syn.vm.cpu.cpuToMax.unused",                  25,        5, true, false ),
    };

    private static final VemaBaseSynthetic[] baseSyntheticMaster = 
    {
    new VemaBaseSynthetic(  "syn.vm.mem.balloonToConfigMemSize.used",
                            "summary.quickStats.balloonedMemory", 1.0,
                            "summary.config.memorySizeMB", false, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.balloonToConfigMemSize.unused", 
                            "summary.quickStats.balloonedMemory", 1.0, 
                            "summary.config.memorySizeMB",  true, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.compressedToConfigMemSize.used", 
                            "summary.quickStats.compressedMemory", 1.0, 
                            "summary.config.memorySizeMB", false, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.compressedToConfigMemSize.unused", 
                            "summary.quickStats.compressedMemory", 1.0, 
                            "summary.config.memorySizeMB",  true, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.swappedToConfigMemSize.used", 
                            "summary.quickStats.swappedMemory", 1.0, 
                            "summary.config.memorySizeMB", false, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.swappedToConfigMemSize.unused", 
                            "summary.quickStats.swappedMemory", 1.0, 
                            "summary.config.memorySizeMB",  true, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.sharedToConfigMemSize.used", 
                            "summary.quickStats.sharedMemory", 1.0, 
                            "summary.config.memorySizeMB", false, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.sharedToConfigMemSize.unused", 
                            "summary.quickStats.sharedMemory", 1.0, 
                            "summary.config.memorySizeMB",  true, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.guestToConfigMemSize.used", 
                            "summary.quickStats.guestMemory", 1.0, 
                            "summary.config.memorySizeMB", false, true ),

    new VemaBaseSynthetic(  "syn.vm.mem.guestToConfigMemSize.unused", 
                            "summary.quickStats.guestMemory", 1.0, 
                            "summary.config.memorySizeMB",  true, true ),

    new VemaBaseSynthetic(  "syn.vm.cpu.cpuToMax.used", 
                            "summary.quickStats.overallCpuDemand", 1.0, 
                            "summary.runtime.maxCpuUsage", false, true ),

    new VemaBaseSynthetic(  "syn.vm.cpu.cpuToMax.unused",
                            "summary.quickStats.overallCpuDemand", 1.0, 
                            "summary.runtime.maxCpuUsage",  true, true ),
    };

    public VemaBaseQuery[] getDefaultSyntheticList() { return baseSyntheticList;   }
    public VemaBaseQuery[] getDefaultMetricList()    { return baseMetricList;      }
    public VemaBaseQuery[] getDefaultConfigList()    { return baseConfigList;      }
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
     * getMonitorState()
     * 
     * turns a number of retrieved VIM25 properties into a 'runtime state' 
     * of the virtual machine.  Kind of defensive code, that could be made 
     * more trim, and less suspicious.
     * 
     * MODES:
     * powerState   guestState   connectionState     result    extra
     * ----------------------------------------------------------------------
     *     null           x                  x       UNKNOWN   no powerstate
     *        x        null                  x       UNKNOWN   no gueststate
     *        x           x               null       UNKNOWN   no connectionstate
     * ----------------------------------------------------------------------
     *  powered_on   running          connected      UP
     *  powered_on   suspended        connected      SUSPENDED
     *  powered_on        ?           connected      UP        {guestState}
     *  powered_on        ?           suspended      SUSPENDED
     *  powered_on   suspended              ?        SUSPENDED
     *  powered_on   notrunning             ?        UNSCHED DOWN
     *  powered_on        ?                 ?        UNSCHED DOWN      {guestState}
     * ----------------------------------------------------------------------
     *  suspended         ?                 ?        SUSPENDED
     *  powered_off       ?                 ?        SCHEDULED_DOWN
     *        ?           ?                 ?        UNKNOWN   {powerState}
     * ----------------------------------------------------------------------
     * @return
     */
    private static final String sUnknown     = "UNKNOWN";
    private static final String sUp          = "UP";
    private static final String sSuspended   = "SUSPENDED";
    private static final String sUnschedDown = "UNSCHEDULED DOWN";
    private static final String sSchedDown   = "SCHEDULED DOWN";

    public String getMonitorState()
    {
    	String       connectionState = null; // convenient variables
    	String            powerState = null; // which make for more readable
    	String            guestState = null; // code.
    	VemaBaseMetric        metric = null; 
    	String                     r = null; // receives       state information
    	String                     x = null; // receives extra state information
    	
    	if((metric = getMetric( "summary.runtime.connectionState" )) != null )
    		connectionState = metric.getCurrValue();
    	
    	if((metric = getMetric( "summary.runtime.powerState" )) != null )
    		powerState = metric.getCurrValue();
    	
    	if((metric = getConfig( "guest.guestState" )) != null )
    		guestState = metric.getCurrValue();
    	
    	if(      powerState      == null )                            r = sUnknown;
    	else if( guestState      == null )                            r = sUnknown;
    	else if( connectionState == null )                            r = sUnknown;
    	else if( powerState.equalsIgnoreCase( "powered_on" ))
    		if( connectionState.equalsIgnoreCase("connected"))
    			if(      guestState.equalsIgnoreCase( "running" ))    r = sUp;
    			else if( guestState.equalsIgnoreCase( "suspended" ))  r = sSuspended;
    			else if( guestState.equalsIgnoreCase( "notrunning" )) r = sUnschedDown;
    			else                                                  r = sUp;
    		else if( connectionState.equalsIgnoreCase("suspended"))   r = sSuspended;
    		else if( connectionState.equalsIgnoreCase("notrunning"))  r = sUnschedDown;
    		else if( guestState.equalsIgnoreCase("suspended"))        r = sSuspended;
    		else                                                      r = sUnschedDown;
    	else if( powerState.equalsIgnoreCase("powered_off"))          r = sSchedDown;
    	else if( powerState.equalsIgnoreCase("suspended"))            r = sSuspended;
    	else	                                                      r = sUnknown;
    	
        x = "pwr="   + powerState       + " "
          + "con="   + connectionState  + " "
          + "guest=" + guestState               ;

    	////log.debug( "VM MonState " + this.getVMName() + " : " + r );
    	
    	this.setRunExtra( x == null ? "" : x );
    	return r;
    }
}
