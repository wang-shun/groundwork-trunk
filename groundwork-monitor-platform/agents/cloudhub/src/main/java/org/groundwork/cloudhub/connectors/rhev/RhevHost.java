package org.groundwork.cloudhub.connectors.rhev;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;

public class RhevHost extends BaseHost
{
	private static Logger log = Logger.getLogger( RhevVM.class );

	public RhevHost(String hostName)
	{
		super(hostName);
	}


    private static final BaseQuery[] baseMetricList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new BaseQuery( "nic[0].stat.data.current.rx.description",    0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.rx.id",             0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.rx.name",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.rx.type",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.rx.unit",           0,        0, false, false ),
  new BaseQuery( "nic[0].stat.data.current.rx.value",          0,        0,  true,  true ),
//new BaseQuery( "nic[0].stat.data.current.tx.description",    0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.tx.id",             0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.tx.name",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.tx.type",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.data.current.tx.unit",           0,        0, false, false ),
  new BaseQuery( "nic[0].stat.data.current.tx.value",          0,        0,  true,  true ),
//new BaseQuery( "nic[0].stat.errors.total.rx.description",    0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.rx.id",             0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.rx.name",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.rx.type",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.rx.unit",           0,        0, false, false ),
  new BaseQuery( "nic[0].stat.errors.total.rx.value",          0,        0,  true,  true ),
//new BaseQuery( "nic[0].stat.errors.total.tx.description",    0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.tx.id",             0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.tx.name",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.tx.type",           0,        0, false, false ),
//new BaseQuery( "nic[0].stat.errors.total.tx.unit",           0,        0, false, false ),
  new BaseQuery( "nic[0].stat.errors.total.tx.value",          0,        0,  true,  true ),
//new BaseQuery( "stat.cpu.current.idle.description",          0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.idle.id",                   0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.idle.name",                 0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.idle.type",                 0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.idle.unit",                 0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.idle.value",                0,        0,  true,  true ),
//new BaseQuery( "stat.cpu.current.system.description",        0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.system.id",                 0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.system.name",               0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.system.type",               0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.system.unit",               0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.system.value",              0,        0,  true,  true ),
//new BaseQuery( "stat.cpu.current.user.description",          0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.user.id",                   0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.user.name",                 0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.user.type",                 0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.user.unit",                 0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.user.value",                0,        0,  true,  true ),
//new BaseQuery( "stat.cpu.load.avg.5m.description",           0,        0, false, false ),
//new BaseQuery( "stat.cpu.load.avg.5m.id",                    0,        0, false, false ),
//new BaseQuery( "stat.cpu.load.avg.5m.name",                  0,        0, false, false ),
//new BaseQuery( "stat.cpu.load.avg.5m.type",                  0,        0, false, false ),
//new BaseQuery( "stat.cpu.load.avg.5m.unit",                  0,        0, false, false ),
  new BaseQuery( "stat.cpu.load.avg.5m.value",                 0,        0,  true,  true ),
//new BaseQuery( "stat.ksm.cpu.current.description",           0,        0, false, false ),
//new BaseQuery( "stat.ksm.cpu.current.id",                    0,        0, false, false ),
//new BaseQuery( "stat.ksm.cpu.current.name",                  0,        0, false, false ),
//new BaseQuery( "stat.ksm.cpu.current.type",                  0,        0, false, false ),
//new BaseQuery( "stat.ksm.cpu.current.unit",                  0,        0, false, false ),
  new BaseQuery( "stat.ksm.cpu.current.value",                 0,        0,  true,  true ),
//new BaseQuery( "stat.memory.buffers.description",            0,        0, false, false ),
//new BaseQuery( "stat.memory.buffers.id",                     0,        0, false, false ),
//new BaseQuery( "stat.memory.buffers.name",                   0,        0, false, false ),
//new BaseQuery( "stat.memory.buffers.type",                   0,        0, false, false ),
//new BaseQuery( "stat.memory.buffers.unit",                   0,        0, false, false ),
  new BaseQuery( "stat.memory.buffers.value",                  0,        0,  true,  true ),
//new BaseQuery( "stat.memory.cached.description",             0,        0, false, false ),
//new BaseQuery( "stat.memory.cached.id",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.cached.name",                    0,        0, false, false ),
//new BaseQuery( "stat.memory.cached.type",                    0,        0, false, false ),
//new BaseQuery( "stat.memory.cached.unit",                    0,        0, false, false ),
  new BaseQuery( "stat.memory.cached.value",                   0,        0,  true,  true ),
//new BaseQuery( "stat.memory.free.description",               0,        0, false, false ),
//new BaseQuery( "stat.memory.free.id",                        0,        0, false, false ),
//new BaseQuery( "stat.memory.free.name",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.free.type",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.free.unit",                      0,        0, false, false ),
  new BaseQuery( "stat.memory.free.value",                     0,        0,  true,  true ),
//new BaseQuery( "stat.memory.shared.description",             0,        0, false, false ),
//new BaseQuery( "stat.memory.shared.id",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.shared.name",                    0,        0, false, false ),
//new BaseQuery( "stat.memory.shared.type",                    0,        0, false, false ),
//new BaseQuery( "stat.memory.shared.unit",                    0,        0, false, false ),
  new BaseQuery( "stat.memory.shared.value",                   0,        0,  true,  true ),
//new BaseQuery( "stat.memory.total.description",              0,        0, false, false ),
//new BaseQuery( "stat.memory.total.id",                       0,        0, false, false ),
//new BaseQuery( "stat.memory.total.name",                     0,        0, false, false ),
//new BaseQuery( "stat.memory.total.type",                     0,        0, false, false ),
//new BaseQuery( "stat.memory.total.unit",                     0,        0, false, false ),
  new BaseQuery( "stat.memory.total.value",                    0,        0,  true,  true ),
//new BaseQuery( "stat.memory.used.description",               0,        0, false, false ),
//new BaseQuery( "stat.memory.used.id",                        0,        0, false, false ),
//new BaseQuery( "stat.memory.used.name",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.used.type",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.used.unit",                      0,        0, false, false ),
  new BaseQuery( "stat.memory.used.value",                     0,        0,  true,  true ),
//new BaseQuery( "stat.swap.cached.description",               0,        0, false, false ),
//new BaseQuery( "stat.swap.cached.id",                        0,        0, false, false ),
//new BaseQuery( "stat.swap.cached.name",                      0,        0, false, false ),
//new BaseQuery( "stat.swap.cached.type",                      0,        0, false, false ),
//new BaseQuery( "stat.swap.cached.unit",                      0,        0, false, false ),
  new BaseQuery( "stat.swap.cached.value",                     0,        0,  true,  true ),
//new BaseQuery( "stat.swap.free.description",                 0,        0, false, false ),
//new BaseQuery( "stat.swap.free.id",                          0,        0, false, false ),
//new BaseQuery( "stat.swap.free.name",                        0,        0, false, false ),
//new BaseQuery( "stat.swap.free.type",                        0,        0, false, false ),
//new BaseQuery( "stat.swap.free.unit",                        0,        0, false, false ),
  new BaseQuery( "stat.swap.free.value",                       0,        0,  true,  true ),
//new BaseQuery( "stat.swap.total.description",                0,        0, false, false ),
//new BaseQuery( "stat.swap.total.id",                         0,        0, false, false ),
//new BaseQuery( "stat.swap.total.name",                       0,        0, false, false ),
//new BaseQuery( "stat.swap.total.type",                       0,        0, false, false ),
//new BaseQuery( "stat.swap.total.unit",                       0,        0, false, false ),
  new BaseQuery( "stat.swap.total.value",                      0,        0,  true,  true ),
//new BaseQuery( "stat.swap.used.description",                 0,        0, false, false ),
//new BaseQuery( "stat.swap.used.id",                          0,        0, false, false ),
//new BaseQuery( "stat.swap.used.name",                        0,        0, false, false ),
//new BaseQuery( "stat.swap.used.type",                        0,        0, false, false ),
//new BaseQuery( "stat.swap.used.unit",                        0,        0, false, false ),
  new BaseQuery( "stat.swap.used.value",                       0,        0,  true,  true ),
    };

    private static final BaseQuery[] baseConfigList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new BaseQuery( "active",                                     0,        0, false, false ),
  new BaseQuery( "address",                                    0,        0, false, false ),
//new BaseQuery( "certificate.organization",                   0,        0, false, false ),
//new BaseQuery( "cluster.id",                                 0,        0, false, false ),
  new BaseQuery( "cluster.name",                               0,        0, false, false ),
  new BaseQuery( "cpu.cores",                                  0,        0, false,  true ),
//new BaseQuery( "cpu.id",                                     0,        0, false, false ),
//new BaseQuery( "cpu.name",                                   0,        0, false, false ),
  new BaseQuery( "cpu.speed",                                  0,        0, false,  true ),
  new BaseQuery( "description",                                0,        0, false, false ),
//new BaseQuery( "id",                                         0,        0, false, false ),
  new BaseQuery( "max_sched_memory",                           0,        0, false,  true ),
  new BaseQuery( "memory",                                     0,        0, false,  true ),
//new BaseQuery( "migrating",                                  0,        0, false, false ),
  new BaseQuery( "name",                                       0,        0, false, false ),
//new BaseQuery( "nic[0].boot",                                0,        0, false, false ),
//new BaseQuery( "nic[0].gateway",                             0,        0, false, false ),
//new BaseQuery( "nic[0].id",                                  0,        0, false, false ),
  new BaseQuery( "nic[0].ip",                                  0,        0, false,  true ),
  new BaseQuery( "nic[0].mac",                                 0,        0, false,  true ),
//new BaseQuery( "nic[0].mask",                                0,        0, false, false ),
  new BaseQuery( "nic[0].name",                                0,        0, false, false ),
//new BaseQuery( "nic[0].network.id",                          0,        0, false, false ),
  new BaseQuery( "nic[0].speed",                               0,        0, false,  true ),
//new BaseQuery( "nic[0].status.state",                        0,        0, false, false ),
//new BaseQuery( "port",                                       0,        0, false, false ),
  new BaseQuery( "status.detail",                              0,        0, false, false ),
  new BaseQuery( "status.state",                               0,        0, false,  true ),
//new BaseQuery( "total",                                      0,        0, false, false ),
//new BaseQuery( "type",                                       0,        0, false, false ),
    };

    private static final BaseQuery[] baseSyntheticList =
    {
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new BaseQuery( "syn.host.cpu.used",                          75,       90, false, false ),
//new BaseQuery( "syn.host.cpu.unused",                        25,       10, false, false ),
  new BaseQuery( "syn.host.mem.used",                          80,       95, false, false ),
//new BaseQuery( "syn.host.mem.buffers",                       80,       95, false, false ),
  new BaseQuery( "syn.host.mem.cached",                        80,       95, false, false ),
  new BaseQuery( "syn.host.mem.free",                          80,       95, false, false ),
  new BaseQuery( "syn.host.mem.shared",                        80,       95, false, false ),
  new BaseQuery( "syn.host.swap.used",                         80,       95, false, false ),
//new BaseQuery( "syn.host.swap.free",                         80,       95, false, false ),
//new BaseQuery( "syn.host.swap.cached",                       80,       95, false, false ),
    };

    private static BaseSynthetic[] baseSyntheticMaster =
    {
        new BaseSynthetic(  "syn.host.mem.used",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.mem.buffers",
                                "stat.memory.buffers.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.mem.cached",
                                "stat.memory.cached.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.mem.free",
                                "stat.memory.free.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.mem.shared",
                                "stat.memory.shared.value",      1.0,
                                "stat.memory.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.swap.used",
                                "stat.swap.used.value",      1.0,
                                "stat.swap.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.swap.free",
                                "stat.swap.free.value",      1.0,
                                "stat.swap.total.value",     false, true ),

        new BaseSynthetic(  "syn.host.swap.cached",
                                "stat.swap.cached.value",      1.0,
                                "stat.swap.total.value",     false, true ),
 };

    public BaseQuery[] getDefaultSyntheticList() { return baseSyntheticList; }
    public BaseQuery[] getDefaultMetricList()    { return baseMetricList; }
    public BaseQuery[] getDefaultConfigList()    { return baseConfigList; }
    public BaseSynthetic[] getSyntheticMaster()  { return baseSyntheticMaster; }

    public BaseSynthetic   getSynthetic( String handle )
    {
        // I know... sequential searching sucks.  But it should always be short list.
        // ......... in future, make it a hash, I believe.
        //
        for( BaseSynthetic v : getSyntheticMaster() )
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
     *     null                  x       UNREACHABLE   powerState not set
     *        x               null       UNREACHABLE   connectionState not set
     * ----------------------------------------------------------
     *  powered_on        connected      UP
     *  powered_on        suspended      SUSPENDED
     *  powered_on              ?        DOWN      {connectionState}
     * ----------------------------------------------------------
     *  powered_off             ?        DOWN
     *        ?                 ?        UNREACHABLE   {powerState}
     * ----------------------------------------------------------
     * @return
    */
    public static final String sUnreachable = "UNREACHABLE";
    public static final String sUp          = "UP";
    public static final String sPending     = "PENDING";
    public static final String sUnschedDown = "UNSCHEDULED DOWN";
    public static final String sSchedDown   = "SCHEDULED DOWN";

    public String getMonitorState()
    {
    	String       statusDetail = null;
    	String        statusState = null;
    	BaseMetric metric = null;
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
    	
    	if(      statusState     == null )                           { r = sUnreachable;     x = "null state"; }
    	else if( statusDetail    == null )                           { r = sUnreachable;     x = "null detail"; }
        else if( statusState.equalsIgnoreCase( "down" ))             { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "error" ))            { r = sUnschedDown; x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "initializing" ))     { r = sPending;     x = "Initializing";  }
        else if( statusState.equalsIgnoreCase( "installing" ))       { r = sPending;     x = "Installing";  }
        else if( statusState.equalsIgnoreCase( "install_failed" ))   { r = sUnschedDown; x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "maintenance" ))      { r = sSchedDown;   x = "Device in Maintenance Mode";  }
        else if( statusState.equalsIgnoreCase( "non_operational" ))  { r = sUnschedDown; x = "Non-operational";  }
        else if( statusState.equalsIgnoreCase( "non_responsive" ))   { r = sUnreachable;     x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "pending_approval" )) { r = sPending;     x = "Pending approval";  }
        else if( statusState.equalsIgnoreCase( 
                                      "preparing_for_maintenance" )) { r = sPending;     x = "Preparing for maintenance";  }
        else if( statusState.equalsIgnoreCase( "connecting" ))       { r = sPending;     x = "Connecting";  }
        else if( statusState.equalsIgnoreCase( "reboot" ))           { r = sPending;     x = "In reboot";  }
        else if( statusState.equalsIgnoreCase( "unassigned" ))       { r = sSchedDown;   x = "Unassigned";  }
    	else if( statusState.equalsIgnoreCase( "up" ))               { r = sUp;          x = "";  }
        else	                                                     { r = sUnreachable;     x = statusDetail;  }

    	log.debug( "HOST MonState (" + x + ") " + this.getHostName() + " : " + r );
        this.setRunExtra( x == null ? "" : x );

   	   	return r;
    }

    public boolean isMetric(String path) {
        for (BaseQuery query : getDefaultMetricList()) {
            if (path.equals(query.getQuery()))
                return true;
        }
        return false;
    }

    public boolean isConfig(String path) {
        for (BaseQuery query : getDefaultConfigList()) {
            if (path.equals(query.getQuery()))
                return true;
        }
        return false;
    }

}
