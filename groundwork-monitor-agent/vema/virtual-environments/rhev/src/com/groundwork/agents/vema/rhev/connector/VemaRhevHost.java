package com.groundwork.agents.vema.rhev.connector;

import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import org.apache.log4j.Logger;

public class VemaRhevHost extends VemaBaseHost
{
	private static org.apache.log4j.Logger log = Logger.getLogger( VemaRhevVM.class );

	public VemaRhevHost(String hostName)
	{
		super(hostName);
	}


    private static final VemaBaseQuery[] baseMetricList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new VemaBaseQuery( "nic[0].stat.data.current.rx.description",    0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.rx.id",             0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.rx.name",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.rx.type",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.rx.unit",           0,        0, false, false ),
  new VemaBaseQuery( "nic[0].stat.data.current.rx.value",          0,        0,  true,  true ),
//new VemaBaseQuery( "nic[0].stat.data.current.tx.description",    0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.tx.id",             0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.tx.name",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.tx.type",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.data.current.tx.unit",           0,        0, false, false ),
  new VemaBaseQuery( "nic[0].stat.data.current.tx.value",          0,        0,  true,  true ),
//new VemaBaseQuery( "nic[0].stat.errors.total.rx.description",    0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.rx.id",             0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.rx.name",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.rx.type",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.rx.unit",           0,        0, false, false ),
  new VemaBaseQuery( "nic[0].stat.errors.total.rx.value",          0,        0,  true,  true ),
//new VemaBaseQuery( "nic[0].stat.errors.total.tx.description",    0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.tx.id",             0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.tx.name",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.tx.type",           0,        0, false, false ),
//new VemaBaseQuery( "nic[0].stat.errors.total.tx.unit",           0,        0, false, false ),
  new VemaBaseQuery( "nic[0].stat.errors.total.tx.value",          0,        0,  true,  true ),
//new VemaBaseQuery( "stat.cpu.current.idle.description",          0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.idle.id",                   0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.idle.name",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.idle.type",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.idle.unit",                 0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.idle.value",                0,        0,  true,  true ),
//new VemaBaseQuery( "stat.cpu.current.system.description",        0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.system.id",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.system.name",               0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.system.type",               0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.system.unit",               0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.system.value",              0,        0,  true,  true ),
//new VemaBaseQuery( "stat.cpu.current.user.description",          0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.user.id",                   0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.user.name",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.user.type",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.user.unit",                 0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.user.value",                0,        0,  true,  true ),
//new VemaBaseQuery( "stat.cpu.load.avg.5m.description",           0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.load.avg.5m.id",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.load.avg.5m.name",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.load.avg.5m.type",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.load.avg.5m.unit",                  0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.load.avg.5m.value",                 0,        0,  true,  true ),
//new VemaBaseQuery( "stat.ksm.cpu.current.description",           0,        0, false, false ),
//new VemaBaseQuery( "stat.ksm.cpu.current.id",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.ksm.cpu.current.name",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.ksm.cpu.current.type",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.ksm.cpu.current.unit",                  0,        0, false, false ),
  new VemaBaseQuery( "stat.ksm.cpu.current.value",                 0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.buffers.description",            0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.buffers.id",                     0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.buffers.name",                   0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.buffers.type",                   0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.buffers.unit",                   0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.buffers.value",                  0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.cached.description",             0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.cached.id",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.cached.name",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.cached.type",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.cached.unit",                    0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.cached.value",                   0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.free.description",               0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.free.id",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.free.name",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.free.type",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.free.unit",                      0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.free.value",                     0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.shared.description",             0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.shared.id",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.shared.name",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.shared.type",                    0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.shared.unit",                    0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.shared.value",                   0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.total.description",              0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.total.id",                       0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.total.name",                     0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.total.type",                     0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.total.unit",                     0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.total.value",                    0,        0,  true,  true ),
//new VemaBaseQuery( "stat.memory.used.description",               0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.id",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.name",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.type",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.unit",                      0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.used.value",                     0,        0,  true,  true ),
//new VemaBaseQuery( "stat.swap.cached.description",               0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.cached.id",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.cached.name",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.cached.type",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.cached.unit",                      0,        0, false, false ),
  new VemaBaseQuery( "stat.swap.cached.value",                     0,        0,  true,  true ),
//new VemaBaseQuery( "stat.swap.free.description",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.free.id",                          0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.free.name",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.free.type",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.free.unit",                        0,        0, false, false ),
  new VemaBaseQuery( "stat.swap.free.value",                       0,        0,  true,  true ),
//new VemaBaseQuery( "stat.swap.total.description",                0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.total.id",                         0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.total.name",                       0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.total.type",                       0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.total.unit",                       0,        0, false, false ),
  new VemaBaseQuery( "stat.swap.total.value",                      0,        0,  true,  true ),
//new VemaBaseQuery( "stat.swap.used.description",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.used.id",                          0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.used.name",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.used.type",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.swap.used.unit",                        0,        0, false, false ),
  new VemaBaseQuery( "stat.swap.used.value",                       0,        0,  true,  true ),
    };

    private static final VemaBaseQuery[] baseConfigList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new VemaBaseQuery( "active",                                     0,        0, false, false ),
  new VemaBaseQuery( "address",                                    0,        0, false, false ),
//new VemaBaseQuery( "certificate.organization",                   0,        0, false, false ),
//new VemaBaseQuery( "cluster.id",                                 0,        0, false, false ),
  new VemaBaseQuery( "cluster.name",                               0,        0, false, false ),
  new VemaBaseQuery( "cpu.cores",                                  0,        0, false,  true ),
//new VemaBaseQuery( "cpu.id",                                     0,        0, false, false ),
//new VemaBaseQuery( "cpu.name",                                   0,        0, false, false ),
  new VemaBaseQuery( "cpu.speed",                                  0,        0, false,  true ),
  new VemaBaseQuery( "description",                                0,        0, false, false ),
//new VemaBaseQuery( "id",                                         0,        0, false, false ),
  new VemaBaseQuery( "max_sched_memory",                           0,        0, false,  true ),
  new VemaBaseQuery( "memory",                                     0,        0, false,  true ),
//new VemaBaseQuery( "migrating",                                  0,        0, false, false ),
  new VemaBaseQuery( "name",                                       0,        0, false, false ),
//new VemaBaseQuery( "nic[0].boot",                                0,        0, false, false ),
//new VemaBaseQuery( "nic[0].gateway",                             0,        0, false, false ),
//new VemaBaseQuery( "nic[0].id",                                  0,        0, false, false ),
  new VemaBaseQuery( "nic[0].ip",                                  0,        0, false,  true ),
  new VemaBaseQuery( "nic[0].mac",                                 0,        0, false,  true ),
//new VemaBaseQuery( "nic[0].mask",                                0,        0, false, false ),
  new VemaBaseQuery( "nic[0].name",                                0,        0, false, false ),
//new VemaBaseQuery( "nic[0].network.id",                          0,        0, false, false ),
  new VemaBaseQuery( "nic[0].speed",                               0,        0, false,  true ),
//new VemaBaseQuery( "nic[0].status.state",                        0,        0, false, false ),
//new VemaBaseQuery( "port",                                       0,        0, false, false ),
  new VemaBaseQuery( "status.detail",                              0,        0, false, false ),
  new VemaBaseQuery( "status.state",                               0,        0, false,  true ),
//new VemaBaseQuery( "total",                                      0,        0, false, false ),
//new VemaBaseQuery( "type",                                       0,        0, false, false ),
    };

    private static final VemaBaseQuery[] baseSyntheticList = 
    {
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new VemaBaseQuery( "syn.host.cpu.used",                          75,       90, false, false ),
//new VemaBaseQuery( "syn.host.cpu.unused",                        25,       10, false, false ),
  new VemaBaseQuery( "syn.host.mem.used",                          80,       95, false, false ),
//new VemaBaseQuery( "syn.host.mem.buffers",                       80,       95, false, false ),
  new VemaBaseQuery( "syn.host.mem.cached",                        80,       95, false, false ),
  new VemaBaseQuery( "syn.host.mem.free",                          80,       95, false, false ),
  new VemaBaseQuery( "syn.host.mem.shared",                        80,       95, false, false ),
  new VemaBaseQuery( "syn.host.swap.used",                         80,       95, false, false ),
//new VemaBaseQuery( "syn.host.swap.free",                         80,       95, false, false ),
//new VemaBaseQuery( "syn.host.swap.cached",                       80,       95, false, false ),
    };

    private static VemaBaseSynthetic[] baseSyntheticMaster = 
    {
        new VemaBaseSynthetic(  "syn.host.mem.used",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.mem.buffers",
                                "stat.memory.buffers.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.mem.cached",
                                "stat.memory.cached.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.mem.free",
                                "stat.memory.free.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.mem.shared",
                                "stat.memory.shared.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.swap.used",
                                "stat.swap.used.value",      1.0,
                                "stat.swap.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.swap.free",
                                "stat.swap.free.value",      1.0,
                                "stat.swap.total.value",     false, true ),

        new VemaBaseSynthetic(  "syn.host.swap.cached",
                                "stat.swap.cached.value",      1.0,
                                "stat.swap.total.value",     false, true ),
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
    private static final String sPending     = "PENDING";
    private static final String sUnschedDown = "UNSCHEDULED DOWN";
    private static final String sSchedDown   = "SCHEDULED DOWN";

    public String getMonitorState()
    {
    	String       statusDetail = null;
    	String        statusState = null;
    	VemaBaseMetric     metric = null; 
    	String                  r = null;
    	String                  x = null;
    	
    	if((metric = getConfig( "status.state" )) != null     // take no chances!
        || (metric = getMetric( "status.state" )) != null )   // could be in either pot
    		statusState = metric.getCurrValue();
    	
    	if((metric = getConfig( "status.detail" )) != null    // same here
        || (metric = getMetric( "status.detail" )) != null )
    		statusDetail = metric.getCurrValue();
        else
            statusDetail = "";
    	
    	if(      statusState     == null )                           { r = sUnknown;     x = "null state"; }
    	else if( statusDetail    == null )                           { r = sUnknown;     x = "null detail"; }
        else if( statusState.equalsIgnoreCase( "down" ))             { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "error" ))            { r = sUnschedDown; x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "initializing" ))     { r = sPending;     x = "Initializing";  }
        else if( statusState.equalsIgnoreCase( "installing" ))       { r = sPending;     x = "Installing";  }
        else if( statusState.equalsIgnoreCase( "install_failed" ))   { r = sUnschedDown; x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "maintenance" ))      { r = sSchedDown;   x = "Device in Maintenance Mode";  }
        else if( statusState.equalsIgnoreCase( "non_operational" ))  { r = sUnschedDown; x = "Non-operational";  }
        else if( statusState.equalsIgnoreCase( "non_responsive" ))   { r = sUnknown;     x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "pending_approval" )) { r = sPending;     x = "Pending approval";  }
        else if( statusState.equalsIgnoreCase( 
                                      "preparing_for_maintenance" )) { r = sPending;     x = "Preparing for maintenance";  }
        else if( statusState.equalsIgnoreCase( "connecting" ))       { r = sPending;     x = "Connecting";  }
        else if( statusState.equalsIgnoreCase( "reboot" ))           { r = sPending;     x = "In reboot";  }
        else if( statusState.equalsIgnoreCase( "unassigned" ))       { r = sSchedDown;   x = "Unassigned";  }
    	else if( statusState.equalsIgnoreCase( "up" ))               { r = sUp;          x = "";  }
        else	                                                     { r = sUnknown;     x = statusDetail;  }

    	log.debug( "HOST MonState (" + x + ") " + this.getHostName() + " : " + r );
        this.setRunExtra( x == null ? "" : x );

   	   	return r;
    }
}
